/**
 * @module measurements-datatable
 */
if (typeof (BMS) === 'undefined') {
	BMS = {};
}
if (typeof (BMS.Fieldbook) === 'undefined') {
	BMS.Fieldbook = {};
}

 BMS.Fieldbook.checkPagination = function(parentDiv) {
	'use strict';
	$(parentDiv + ' .dataTables_length select').on('change', function() {
		if ($(parentDiv + ' .fbk-page-div ul.pagination li').length > 3) {
			$(parentDiv + ' .fbk-page-div').removeClass('fbk-hide');
		}else {
			$(parentDiv + ' .fbk-page-div').addClass('fbk-hide');
		}
	});
 };

BMS.Fieldbook.MeasurementsTable = {
	getColumnOrdering: function(tableName, forceGet) {
		var orderedColumns = [];
		var hasOrderingChange = false;
		if ($('body').data('columnReordered') === '1') {
			hasOrderingChange = true;
		}
		if ($('#' + tableName).dataTable() !== null &&  $('#' + tableName).dataTable().fnSettings() !== null) {
			var cols = $('#' + tableName).dataTable().fnSettings().aoColumns;
			$(cols).each(function(index) {
				var termId = $($(cols[index].nTh)[0]).attr('data-term-id');
				var prevIndex = $('#' + tableName).dataTable().fnSettings().aoColumns[index]._ColReorder_iOrigCol;
				if (termId != 'Action') {
					if (index != prevIndex) {
						hasOrderingChange = true;
					}
					orderedColumns[orderedColumns.length] = termId;
				}
			});
		}
		if (forceGet || hasOrderingChange) {
			return orderedColumns;
		}
		//we return blank if there is no ordering change
		return [];
	}
};

BMS.Fieldbook.MeasurementsDataTable = (function($) {
	
	/**
	 * Creates a new MeasurementsDataTable.
	 *
	 * @constructor
	 * @alias module:fieldbook-datatable
	 * @param {string} tableIdentifier the id of the table container
	 * @param {string} ajaxUrl the URL from which to retrieve table data
	 */
	var dataTableConstructor = function MeasurementsDataTable(tableIdentifier, dataList) {
		'use strict';

		var columns = [],
			columnsDef = [],
			table;

		$(tableIdentifier + ' thead tr th').each(function() {
			columns.push({
				data: $(this).html(),
				defaultContent: ''
			});
			if ($(this).data('term-data-type-id') === 1110) {
				// Column definition for Numeric data type

				var minVal = ($(this).data('min-range'));
				var maxVal = ($(this).data('max-range'));
				var termId = $(this).data('term-id');
				var isVariates = $(this).hasClass('variates');
				columnsDef.push({
					defaultContent: '',
					targets: columns.length - 1,
					createdCell: function(td, cellData, rowData, row, col) {
						if (isVariates) {
							$(td).addClass('numeric-variable');
							var cellText = $(td).text();
							if (minVal != null && maxVal != null && (parseFloat(minVal) > parseFloat(cellText) || parseFloat(cellText) > parseFloat(maxVal))) {
								$(td).removeClass('accepted-value');
								$(td).removeClass('invalid-value');
								if ($(td).text() !== 'missing') {
									if ($(td).find("input[type='hidden']").val() === 'true') {
										$(td).addClass('accepted-value');
									} else {
										$(td).addClass('invalid-value');
									}
								}
							}
						}
						$(td).data('term-id', termId);
					},
					render: function(data, type, full, meta) {
						var displayData = EscapeHTML.escape(data[0] != null ? data[0] : '');
						var hiddenData = EscapeHTML.escape(data[1]);

						if (data !== undefined) {
							return displayData + '<input type="hidden" value="' + hiddenData + '" />';
						}
					}
				});
			} else if ($(this).data('term-data-type-id') === 1120) {
				// Column definition for Character data type


				columnsDef.push({
					defaultContent: '',
					targets: columns.length - 1,
					render: function(data, type, full, meta) {
						return EscapeHTML.escape(data);
					}
				});
			} else if ($(this).data('term-data-type-id') === 1130) {
				// Column definition for Categorical data type

				if ($(this).data('term-valid-values') == null) {
					$(this).data('term-valid-values', '');
				}
				var possibleValues = $(this).data('term-valid-values').split('|');
				var termId = $(this).data('term-id');
				var isVariates = $(this).hasClass('variates');
				columnsDef.push({
					defaultContent: '',
					targets: columns.length - 1,
					createdCell: function(td, cellData, rowData, row, col) {
						if (isVariates) {
							// cellData[0] : categorical name
							// cellData[1] : categorical display description

							// current measurementData has no value thus no need to check if out-of-bounds
							if (cellData[1] === '') {
								return;
							}

							// look for that description in the list of possible values
							var found = $.grep(possibleValues, function(value, i) {
								if (value === cellData[1]) {
									// this is the case where a=x format is retrieved directly from ontology DB

									return true;
								} else if (value !== '' && value.indexOf('=') === -1) {
									// this is the case where categorical ref values (possibleValues) retrieved is not in a=x format

									// since currentValue contains both name and description, we need to retrieve
									// only the description by splitting from the first occurrence of the separator
									var currentValue = cellData[1].substring(cellData[1].indexOf('=') + 1).trim();

									return value === currentValue;
								}

								return false;
							}).length;

							// if not found we may change its class as accepted (blue) or invalid (red)
							// depending on the data
							if (found <= 0) {
								$(td).removeClass('accepted-value');
								$(td).removeClass('invalid-value');
								if ($(td).text() !== 'missing') {
									if ($(td).find("input[type='hidden']").val() === 'true') {
										$(td).addClass('accepted-value');
									} else {
										$(td).addClass('invalid-value');
									}
								}
							}
						}
						$(td).data('term-id', termId);
					},
					render: function(data, type, full, meta) {
						if (data !== undefined) {
							// Use knowledge from session.isCategoricalDisplayView to render correct data
							// data[0] = name, data[1] = description, data[2] = accepted value
							var showDescription = window.isCategoricalDescriptionView ? 'style="display:none"' : '';
							var showName = !window.isCategoricalDescriptionView ? 'style="display:none"' : '';

							var categoricalNameDom = '<span class="fbk-measurement-categorical-name" '+ showName  + '>' + EscapeHTML.escape(data[1]) + '</span>';
							var categoricalDescDom = '<span class="fbk-measurement-categorical-desc" '+ showDescription  + '>' + EscapeHTML.escape(data[0]) + '</span>';

							return (isVariates ? categoricalNameDom + categoricalDescDom : EscapeHTML.escape(data[1])) +
								'<input type="hidden" value="' + EscapeHTML.escape(data[2]) + '" />';
						}
					}
				});
			}

			if ($(this).data('term-id') == '8240') {
				// For GID
				columnsDef.push({
					defaultContent: '',
					targets: columns.length - 1,
					data: $(this).html(),
					width: '100px',
					render: function(data, type, full, meta) {
						return '<a class="gid-link" href="javascript: void(0)" ' +
							'onclick="openGermplasmDetailsPopopWithGidAndDesig(&quot;' +
							full.GID + '&quot;,&quot;' + full.DESIGNATION + '&quot;)">' + EscapeHTML.escape(data) + '</a>';
					}
				});
			} else if ($(this).data('term-id') == '8250') {
				// For designation
				columnsDef.push({
					defaultContent: '',
					targets: columns.length - 1,
					data: $(this).html(),
					render: function(data, type, full, meta) {
						return '<a class="desig-link" href="javascript: void(0)" ' +
							'onclick="openGermplasmDetailsPopopWithGidAndDesig(&quot;' +
							full.GID + '&quot;,&quot;' + full.DESIGNATION + '&quot;)">' + EscapeHTML.escape(data) + '</a>';
					}
				});
			} else if ($(this).data('term-id') == 'Action') {
				// For designation
				columnsDef.push({
					defaultContent: '',
					targets: columns.length - 1,
					data: $(this).html(),
					width: '50px',
					render: function(data, type, full, meta) {
						return '<a href="javascript: editExperiment(&quot;' + tableIdentifier + '&quot;,' +
							EscapeHTML.escape(data) + ',' + meta.row + ')" class="fbk-edit-experiment"></a>';
					}
				});
			}
		});
		
		table = $(tableIdentifier).DataTable({
			destroy: true,
			data: dataList,
			columns: columns,
			scrollY: '500px',
			scrollX: '100%',
			scrollCollapse: true,
			columnDefs: columnsDef,
			lengthMenu: [[50, 75, 100, -1], [50, 75, 100, 'All']],
			bAutoWidth: true,
			iDisplayLength: 100,
			fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {

				var toolTip = 'GID: ' + aData.GID + ' Designation: ' + aData.DESIGNATION;
				// Assuming ID is in last column
				$(nRow).attr('id', aData.experimentId);
				$(nRow).data('row-index', this.fnGetPosition(nRow));
				$(nRow).attr('title', toolTip);
				$('td', nRow).attr('nowrap', 'nowrap');

				$(nRow).find('.accepted-value, .invalid-value, .numeric-variable').each(function() {

					var termId = $(this).data('term-id');
					var cellData = $(this).text();
					if (termId != undefined) {
						var possibleValues = $(tableIdentifier + " thead tr th[data-term-id='" + termId + "']").data('term-valid-values');
						var dataTypeId = $(tableIdentifier + " thead tr th[data-term-id='" + termId + "']").data('term-data-type-id');
						if (dataTypeId == '1110') {
							var minVal = ($(tableIdentifier + " thead tr th[data-term-id='" + termId + "']").data('min-range'));
							var maxVal = ($(tableIdentifier + " thead tr th[data-term-id='" + termId + "']").data('max-range'));
							var isVariates =  $(tableIdentifier + " thead tr th[data-term-id='" + termId + "']").hasClass('variates');
							if (isVariates) {
								$(this).removeClass('accepted-value');
								$(this).removeClass('invalid-value');
								if (minVal != null && maxVal != null && (parseFloat(minVal) > parseFloat(cellData) || parseFloat(cellData) > parseFloat(maxVal))) {
									if (cellData !== 'missing') {

										if ($(this).find("input[type='hidden']").val() === 'true') {
											$(this).addClass('accepted-value');
										} else {
											$(this).addClass('invalid-value');
										}
									}
								}
							}
						}else if (possibleValues != undefined) {
							var values = possibleValues.split('|');

							$(this).removeClass('accepted-value');
							$(this).removeClass('invalid-value');

							if (cellData !== '' && cellData !== 'missing') {
								if ($.inArray(cellData, values) === -1 && $(this).find("input[type='hidden']").val() !== 'true') {
									if ($(this).data('is-accepted') === '1') {
										$(this).addClass('accepted-value');
									}else if ($(this).data('is-accepted') === '0') {
										$(this).removeClass('invalid-value').removeClass('accepted-value');
									} else {
										$(this).addClass('invalid-value');
									}
									$(this).data('term-id', $(this).data('term-id'));
								} else {
									$(this).addClass('accepted-value');
								}
							}
						}
					}
				});
				return nRow;
			},
			fnInitComplete: function(oSettings, json) {
				$(tableIdentifier + '_wrapper .dataTables_length select').select2({minimumResultsForSearch: 10});
				oSettings.oInstance.fnAdjustColumnSizing();
				oSettings.oInstance.api().colResize.init(oSettings.oInit.colResize);
				if (this.$('.invalid-value').length !== 0) {
					$('#review-out-of-bounds-data-list').show();
				} else {
					$('#review-out-of-bounds-data-list').hide();
				}
			},
			language: {
				search: '<span class="mdt-filtering-label">Search:</span>'
			},
			dom: 'R<"mdt-header"rli<"mdt-filtering">r>tp',
			// For column visibility
			colVis: {
				exclude: [0],
				restore: 'Restore',
				showAll: 'Show all'
			},
			// Problem with reordering plugin and fixed column for column re-ordering
			colReorder: {
				fixedColumns: 1
			}
		});
		

		if ($('#studyId').val() != '') {
			// Activate an inline edit on click of a table cell
			$(tableIdentifier).on('click', 'tbody td:not(:first-child)', function(e) {
				if (isAllowedEditMeasurementDataCell()) {
					var $tdCell = $(this);
					var cellTdIndex =  $(this).index();
					var rowIndex = $(this).parent('tr').data('row-index');

					var $colHeader = $('#measurementsDiv .dataTables_scrollHead table th:eq(' + cellTdIndex + ')');
					$(tableIdentifier).data('show-inline-edit', '1');
					if ($colHeader.hasClass('variates')) {
						$('body').data('last-td-time-clicked', new Date().getTime());
					}
					if ($colHeader.hasClass('factors')) {
						//we should now submit it
						processInlineEditInput();
					} else if ($colHeader.hasClass('variates') && $tdCell.data('is-inline-edit') !== '1') {
						processInlineEditInput();
						if ($('#measurement-table').data('show-inline-edit') === '1') {
							$.ajax({
								url: '/Fieldbook/Common/addOrRemoveTraits/update/experiment/cell/' + rowIndex + '/' + $colHeader.data('term-id'),
								type: 'GET',
								success: function(data) {
									$tdCell.html(data);
									$tdCell.data('is-inline-edit', '1');
								},
								error: function() {
									//TODO localise the message
									showErrorMessage('Server Error', 'Could not update the measurement');
								}
							});
						}
					}
				}
			});
		}
		$(tableIdentifier).dataTable().bind('sort', function() {
			$(tableIdentifier).dataTable().fnAdjustColumnSizing();
		});
		$('#measurementsDiv .mdt-columns').detach().insertBefore('.mdt-filtering');
		$('.measurement-dropdown-menu a').click(function(e) {
			var column;

			e.stopPropagation();
			if ($(this).parent().hasClass('fbk-dropdown-select-fade')) {
				$(this).parent().removeClass('fbk-dropdown-select-fade');
				$(this).parent().addClass('fbk-dropdown-select-highlight');

			} else {
				$(this).parent().addClass('fbk-dropdown-select-fade');
				$(this).parent().removeClass('fbk-dropdown-select-highlight');
			}
			// Get the column API object
			var colIndex = $(this).attr('data-index');

			var cols = $(tableIdentifier).dataTable().fnSettings().aoColumns;
			$(cols).each(function(index) {
				var prevIndex = $(tableIdentifier).dataTable().fnSettings().aoColumns[index]._ColReorder_iOrigCol;
				if (colIndex == prevIndex) {
					column = table.column(index);
					// Toggle the visibility
					column.visible(!column.visible());
				}
			});
		});
	};
	return dataTableConstructor;

})(jQuery);

BMS.Fieldbook.ReviewDetailsOutOfBoundsDataTable = (function($) {
	
	/**
	 * Creates a new ReviewDetailsOutOfBoundsDataTable.
	 *
	 * @constructor
	 * @alias module:fieldbook-datatable
	 * @param {string} tableIdentifier the id of the table container
	 * @param {string} ajaxUrl the URL from which to retrieve table data
	 */
	var dataTableConstructor = function ReviewDetailsOutOfBoundsDataTable(tableIdentifier, dataList) {
		'use strict';

		var columns = [],
			columnsDef = [],
			table;

		$(tableIdentifier + ' thead tr th').each(function() {

			if (($(this).data('term-id') === 'Check')) {
				columns.push({
					data:   'active',
					defaultContent: '',
					render: function(data, type, row) {
						return '<input data-row-index="' + row.MEASUREMENT_ROW_INDEX + '" type="checkbox" class="editor-active" data-binding>';
					},
					className:'fbk-center'
				});
			} else if (($(this).data('term-id') === 'NewValue')) {
				columns.push({
					data:   'newValue',
					defaultContent: '',
					render: function(data, type, row) {
						return '<input data-row-index="' + row.MEASUREMENT_ROW_INDEX + '" type="text" class="form-control" data-binding />';
					}
				});
			} else {
				columns.push({
					data: $(this).html(),
					defaultContent: '',
					render: function(data, type, row) {
                        if(data && Array === data.constructor){
                            return data[0];
                        } else {
                            return data;
                        }
					}
				});
			}

			if ($(this).data('term-data-type-id') == '1130' || $(this).data('term-data-type-id') == '1110') {
				columnsDef.push({
					targets: columns.length - 1,
					defaultContent: '',
					render: function(data, type, full, meta) {
						return EscapeHTML.escape((data[0] != null) ? data[0] :  '');
					}
				});
			}
		});

		if ($.fn.dataTable.isDataTable($(tableIdentifier))) {
			table = $(tableIdentifier).DataTable();
			table.clear();
			table.rows.add(dataList).draw();
		} else {
			table = $(tableIdentifier).DataTable({
				data: dataList,
				columns: columns,
				retrieve: true,
				scrollY: '400px',
				scrollX: '100%',
				scrollCollapse: true,
				columnDefs: columnsDef,
				lengthMenu: [[50, 75, 100, -1], [50, 75, 100, 'All']],
				bAutoWidth: true,
				iDisplayLength: 100,
				fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {

					// Assuming ID is in last column
					$(nRow).attr('id', aData.experimentId);
					$(nRow).data('row-index', this.fnGetPosition(nRow));

					$('td', nRow).attr('nowrap', 'nowrap');
					$('td', nRow).attr('nowrap', 'nowrap');

					return nRow;
				},
				fnInitComplete: function(oSettings, json) {
					$(tableIdentifier + '_wrapper .dataTables_length select').select2({minimumResultsForSearch: 10});
					oSettings.oInstance.fnAdjustColumnSizing();
					oSettings.oInstance.api().colResize.init(oSettings.oInit.colResize);
				},
				language: {
					search: '<span class="mdt-filtering-label">Search:</span>'
				},
				dom: 'R<<"mdt-header"rli<"mdt-filtering">r><t>p>',
				// Problem with reordering plugin and fixed column for column re-ordering
				colReorder: {
					fixedColumns: 1
				}
			});
		}

		$(tableIdentifier).dataTable().bind('sort', function() {
			$(tableIdentifier).dataTable().fnAdjustColumnSizing();
		});

	};

	return dataTableConstructor;

})(jQuery);

BMS.Fieldbook.PreviewCrossesDataTable = (function($) {
	
	/**
	 * Creates a new PreviewCrossesDataTable.
	 *
	 * @constructor
	 * @alias module:fieldbook-datatable
	 * @param {string} tableIdentifier the id of the table container
	 * @param {string} ajaxUrl the URL from which to retrieve table data
	 */
	var dataTableConstructor = function PreviewCrossesDataTable(tableIdentifier, dataList, tableHeaderList) {
		'use strict';

		var columns = [],
			columnsDef = [],
			table;
		
		$.each( tableHeaderList, function( index, value ){
			columns.push({
				data: value,
				defaultContent: '',
			});
		});

		$(tableIdentifier + ' thead tr th').each(function(index) {
			if ($(this).html() === 'DUPLICATE') {
				columnsDef.push({
					defaultContent: '',
					targets: columns.length - 1,
					createdCell: function(td, cellData, rowData, row, col) {

						if ($(td).text().indexOf('Plot Dupe') != -1) {
							$(td).addClass('plotDupe');
						} else if ($(td).text().indexOf('Pedigree Dupe') != -1) {
							$(td).addClass('pedigreeDupe');
						} else if ($(td).text().indexOf('Plot Recip') != -1) {
							$(td).addClass('plotRecip');
						} else if ($(td).text().indexOf('Pedigree Recip') != -1) {
							$(td).addClass('pedigreeRecip');
						}

					}
				});
			}
			//update header with the correct ontology name
			$(this).html(columns[index].data);
		});

		if ($.fn.dataTable.isDataTable($(tableIdentifier))) {
			table = $(tableIdentifier).DataTable();
			table.clear();
			table.rows.add(dataList).draw();
		} else {
			table = $(tableIdentifier).DataTable({
				data: dataList,
				columns: columns,
				retrieve: true,
				scrollY: '400px',
				scrollX: '100%',
				scrollCollapse: true,
				columnDefs: columnsDef,
				lengthMenu: [[50, 75, 100, -1], [50, 75, 100, 'All']],
				bAutoWidth: true,
				iDisplayLength: 100,
				fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {

					// Assuming ID is in last column
					$(nRow).attr('id', aData.experimentId);
					$(nRow).data('row-index', this.fnGetPosition(nRow));

					$('td', nRow).attr('nowrap', 'nowrap');
					$('td', nRow).attr('nowrap', 'nowrap');

					return nRow;
				},
				fnInitComplete: function(oSettings, json) {
					$(tableIdentifier + '_wrapper .dataTables_length select').select2({minimumResultsForSearch: 10});
					oSettings.oInstance.fnAdjustColumnSizing();
					oSettings.oInstance.api().colResize.init(oSettings.oInit.colResize);
				},
				language: {
					search: '<span class="mdt-filtering-label">Search:</span>'
				},
				dom: 'R<<"mdt-header"rli<"mdt-filtering">r><t>p>',
				// Problem with reordering plugin and fixed column for column re-ordering
				colReorder: {
					fixedColumns: 1
				}
			});
		}

		$(tableIdentifier).dataTable().bind('sort', function() {
			$(tableIdentifier).dataTable().fnAdjustColumnSizing();
		});

	};

	return dataTableConstructor;

})(jQuery);

BMS.Fieldbook.GermplasmListDataTable = (function($) {

	
	/**
	 * Creates a new MeasurementsDataTable.
	 *
	 * @constructor
	 * @alias module:fieldbook-datatable
	 * @param {string} tableIdentifier the id of the table container
	 * @param {string} parentDiv parentdiv of that contains the table
	 * @param {dataList} json representation of the data to be displayed
	 */
	var dataTableConstructor = function GermplasmListDataTable(tableIdentifier, parentDiv, dataList) {
		'use strict';

		var columns = [],
		columnsDef = [],
		germplasmDataTable;

		$(tableIdentifier + ' thead tr th').each(function() {
			columns.push({data: $(this).data('col-name')});
			if ($(this).data('col-name') == 'gid') {
				// For GID
				columnsDef.push({
					targets: columns.length - 1,
					data: $(this).html(),
					width: '100px',
					render: function(data, type, full, meta) {
						return '<a class="gid-link" href="javascript: void(0)" ' +
							'onclick="openGermplasmDetailsPopopWithGidAndDesig(&quot;' +
							full.gid + '&quot;,&quot;' + full.desig + '&quot;)">' + data + '</a>';
					}
				});
			} else if ($(this).data('col-name') == 'desig') {
				// For designation
				columnsDef.push({
					targets: columns.length - 1,
					data: $(this).html(),
					render: function(data, type, full, meta) {
						return '<a class="desig-link" href="javascript: void(0)" ' +
							'onclick="openGermplasmDetailsPopopWithGidAndDesig(&quot;' +
							full.gid + '&quot;,&quot;' + full.desig + '&quot;)">' + data + '</a>';
					}
				});
			}
		});

		this.germplasmDataTable = $(tableIdentifier).dataTable({
			data: dataList,
			columns: columns,
			columnDefs: columnsDef,
			scrollY: '500px',
			scrollX: '100%',
			scrollCollapse: true,
			dom: 'R<t><"fbk-page-div"p>',
			iDisplayLength: 100,
			fnDrawCallback: function(oSettings) {
				makeGermplasmListDraggable(true);
			},
			fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
				$(nRow).data('entry', aData.entry);
				$(nRow).data('gid', aData.gid);
				$(nRow).data('index', aData.position);

					$(nRow).addClass('draggable primaryRow');
					$('td', nRow).attr('nowrap', 'nowrap');
					return nRow;
				},
				fnInitComplete: function(oSettings, json) {

					var totalPages = oSettings._iDisplayLength === -1 ? 0 : Math.ceil(oSettings.fnRecordsDisplay() / oSettings._iDisplayLength);
					if (totalPages === 1) {
						$(parentDiv + ' .fbk-page-div').addClass('fbk-hide');
					}
					$(parentDiv).removeClass('fbk-hide-opacity');
					oSettings.oInstance.fnAdjustColumnSizing();
					oSettings.oInstance.api().colResize.init(oSettings.oInit.colResize);
					oSettings.oInstance.fnAdjustColumnSizing();
				}
		});

		GermplasmListDataTable.prototype.getDataTable = function() {
			return this.germplasmDataTable;
		};
	};

	return dataTableConstructor;

})(jQuery);

BMS.Fieldbook.TrialGermplasmListDataTable = (function($) {

	var dataTableConstructor = function TrialGermplasmListDataTable(tableIdentifier, parentDiv, dataList) {
		'use strict';

		var columns = [],
		columnsDef = [],
		defaultOrdering = [],
		table;
		$(tableIdentifier + ' thead tr th').each(function() {
			columns.push({data: $(this).data('col-name')});
			if ($(this).data('col-name') == '8230-key') {
				defaultOrdering = [columns.length - 1, 'asc'];
			} else if ($(this).data('col-name') == '8240-key') {
				// For GID
				columnsDef.push({
					targets: columns.length - 1,
					data: $(this).data('col-name'),
					render: function(data, type, full, meta) {
						return '<a class="gid-link" href="javascript: void(0)" ' +
							'onclick="openGermplasmDetailsPopopWithGidAndDesig(&quot;' +
							full.gid + '&quot;,&quot;' + full.desig + '&quot;)">' + data + '</a>';
					}
				});
			} else if ($(this).data('col-name') == '8250-key') {
				// For designation
				columnsDef.push({
					targets: columns.length - 1,
					data: $(this).data('col-name'),
					render: function(data, type, full, meta) {
						return '<a class="desig-link" href="javascript: void(0)" ' +
							'onclick="openGermplasmDetailsPopopWithGidAndDesig(&quot;' +
							full.gid + '&quot;,&quot;' + full.desig + '&quot;)">' + data + '</a>';
					}
				});
			}else if ($(this).data('col-name') == '8255-key') {
				// For check
				columnsDef.push({
					targets: columns.length - 1,
					data: $(this).data('col-name'),
					render: function(data, type, full, meta) {
						var fieldName = 'selectedCheck',
							count = 0,
							actualVal = '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;',
							actualCode = '',
							domElem = '';
						for (count = 0 ; count < full.checkOptions.length ; count++) {
							if (full.checkOptions[count].id == full['8255-key']) {
								actualVal = full.checkOptions[count].description;
								actualCode = full.checkOptions[count].name;
								domElem = '<input class="check-hidden" type="hidden"  data-code="' + actualCode + '" value="' + full['8255-key'] + '" id="selectedCheck' + (meta.row) + '" name="' + fieldName + '">';
								break;
							}
						}
						if (domElem === '') {
							domElem = '<input data-index="' + meta.row + '" class="check-hidden" type="hidden"  data-code="' + actualCode + '" value="' + full['8255-key'] + '" id="selectedCheck' + (meta.row) + '" name="' + fieldName + '">';
						}

						return '<a data-index="' + meta.row + '" class="check-href edit-check' + meta.row + '" data-code="' + actualCode + '" href="javascript: showPopoverCheck(&quot;' + (meta.row) + '&quot;, &quot;.germplasm-list-items&quot;, &quot;edit-check' + meta.row + '&quot;)">' + actualVal + '</a>' + domElem;
					}
				});
			}
		});

		if ($.fn.dataTable.isDataTable($(tableIdentifier))) {
			this.table = $(tableIdentifier).DataTable();
			this.table.clear();
			this.table.rows.add(dataList).draw();
		} else {
			this.table = $(tableIdentifier).dataTable({
				data: dataList,
				columns: columns,
				columnDefs: columnsDef,
				retrieve: true,
				scrollY: '500px',
				scrollX: '100%',
				scrollCollapse: true,
				order: defaultOrdering,
				// Problem with reordering plugin and fixed column for column re-ordering
				colReorder: {
					fixedColumns: 1
				},
				dom: 'R<t><"fbk-page-div"p>',
				iDisplayLength: 100,
				fnDrawCallback: function(oSettings) {

				},
				fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
					$(nRow).data('entry', aData.entry);
					$(nRow).data('gid', aData.gid);
					$(nRow).data('index', aData.position);

					$(nRow).addClass('primaryRow');
					$('td', nRow).attr('nowrap', 'nowrap');
					return nRow;
				},
				fnInitComplete: function(oSettings, json) {
					var totalPages = oSettings._iDisplayLength === -1 ? 0 : Math.ceil(oSettings.fnRecordsDisplay() / oSettings._iDisplayLength);
					if (totalPages === 1) {
						$(parentDiv + ' .fbk-page-div').addClass('fbk-hide');
					}
					$(parentDiv).removeClass('fbk-hide-opacity');
					oSettings.oInstance.fnAdjustColumnSizing();
					oSettings.oInstance.api().colResize.init(oSettings.oInit.colResize);
					oSettings.oInstance.fnAdjustColumnSizing();
				}
			});
		}

		TrialGermplasmListDataTable.prototype.getDataTable = function() {
			return this.table;
		};

		TrialGermplasmListDataTable.prototype.getDataTableColumnIndex = function(colName)
		{
			var colNames = this.table.fnSettings().aoColumns;
			for (var counter = 0 ; counter < colNames.length ; counter++) {
				if (colNames[counter].data === colName) {
					return colNames[counter].idx;
				}
			}
			return -1;
		};

		TrialGermplasmListDataTable.prototype.getDataTableColumn = function(colName) {
			var colNames = this.table.fnSettings().aoColumns;
			for (var counter = 0 ; counter < colNames.length ; counter++) {
				if (colNames[counter].data === colName) {
					return colNames[counter];
				}
			}
			return null;
		};

		$('.col-show-hide').html('');
		$('.col-show-hide').html($(parentDiv + ' .mdt-columns').clone().removeClass('fbk-hide'));

		$('.germplasm-dropdown-menu a').click(function(e) {
			e.stopPropagation();
			if ($(this).parent().hasClass('fbk-dropdown-select-fade')) {
				$(this).parent().removeClass('fbk-dropdown-select-fade');
				$(this).parent().addClass('fbk-dropdown-select-highlight');

			} else {
				$(this).parent().addClass('fbk-dropdown-select-fade');
				$(this).parent().removeClass('fbk-dropdown-select-highlight');
			}

			// hide germplasm column
			(function(colName) {
				var column = null;
				// Get the column API object
				if (germplasmDataTable != null) {
					column = germplasmDataTable.getDataTableColumn(colName);
					// Toggle the visibility
					if (column !== null) {
						germplasmDataTable.getDataTable().fnSetColumnVis(column.idx, !column.bVisible);
					}
				}
			})($(this).attr('data-column-name'));

		});
	};

	return dataTableConstructor;

})(jQuery);

BMS.Fieldbook.SelectedCheckListDataTable = (function($) {

	
	/**
	 * Creates a new MeasurementsDataTable.
	 *
	 * @constructor
	 * @alias module:fieldbook-datatable
	 * @param {string} tableIdentifier the id of the table container
	 * @param {string} parentDiv parentdiv of that contains the table
	 * @param {dataList} json representation of the data to be displayed
	 */
	var dataTableConstructor = function SelectedCheckListDataTable(tableIdentifier, parentDiv, dataList) {
		'use strict';

		var columns = [],
		columnsDef = [],
		checkDataTable;

		$(tableIdentifier + ' thead tr th').each(function() {
			columns.push({data: $(this).data('col-name')});
			if ($(this).data('col-name') == 'desig') {
				// For designation
				columnsDef.push({
					targets: columns.length - 1,
					data: $(this).html(),
					render: function(data, type, full, meta) {
						return '<a class="desig-link" href="javascript: void(0)" ' +
							'onclick="openGermplasmDetailsPopopWithGidAndDesig(&quot;' +
							full.gid + '&quot;,&quot;' + full.desig + '&quot;)">' + data + '</a>';
					}
				});
			}else if ($(this).data('col-name') == 'check') {
				// For designation
				columnsDef.push({
					targets: columns.length - 1,
					data: $(this).html(),
					render: function(data, type, full, meta) {
						var fieldName = 'selectedCheck',
							count = 0,
							isSelected = '',
							actualVal = '',
							actualCode = '',
							domElem = '';

						for (count = 0 ; count < full.checkOptions.length ; count++) {
							isSelected = '';
							if (full.checkOptions[count].id == full.check) {
								actualVal = full.checkOptions[count].description;
								actualCode = full.checkOptions[count].name;
								domElem = '<input data-index="' + meta.row + '" class="check-hidden" type="hidden"  data-code="' + actualCode + '" value="' + full.check + '" id="selectedCheck' + (meta.row) + '" name="' + fieldName + '">';
								break;
							}
						}

						return '<a data-index="' + meta.row + '" class="check-href edit-check' + meta.row + '" data-code="' + actualCode + '" href="javascript: showPopoverCheck(&quot;' + (meta.row) + '&quot;, &quot;.check-germplasm-list-items&quot;, &quot;edit-check' + meta.row + '&quot;)">' + actualVal + '</a>' + domElem;
					}
				});
			} else if ($(this).data('col-name') == 'action') {
				// For delete
				columnsDef.push({
					targets: columns.length - 1,
					width: '20px',
					data: $(this).html(),
					render: function(data, type, full, meta) {
						return '<span class="delete-icon delete-check" data-index="' + meta.row + '"></span>';
					}
				});
			}
		});

		if ($.fn.dataTable.isDataTable($(tableIdentifier))) {
			this.checkDataTable = $(tableIdentifier).DataTable();
			this.checkDataTable.clear();
			this.checkDataTable.rows.add(dataList).draw();
		} else {
			this.checkDataTable = $(tableIdentifier).dataTable({
				data: dataList,
				columns: columns,
				columnDefs: columnsDef,
				retrieve: true,
				scrollY: '500px',
				scrollX: '100%',
				bSort: false,
				scrollCollapse: true,
				dom: 'R<t><"fbk-page-div"p>',
				iDisplayLength: 100,
				fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
					$(nRow).addClass('checkRow');
					$(nRow).data('entry', aData.entry);
					$(nRow).data('gid', aData.gid);
					$(nRow).data('index', aData.index);

					$('td', nRow).attr('nowrap', 'nowrap');
					setTimeout(function() {makeCheckDraggable(makeCheckDraggableBool);}, 300);
					return nRow;
				},
				fnInitComplete: function(oSettings, json) {

					var totalPages = oSettings._iDisplayLength === -1 ? 0 : Math.ceil(oSettings.fnRecordsDisplay() / oSettings._iDisplayLength);
					if (totalPages === 1) {
						$(parentDiv + ' .fbk-page-div').addClass('fbk-hide');
					}
					setTimeout(function() {oSettings.oInstance.fnAdjustColumnSizing();}, 1);
					//hide delete icon for read only view
					if ($('#chooseGermplasmAndChecks').data('replace') !== undefined && parseInt($('#chooseGermplasmAndChecks').data('replace')) === 0 && measurementRowCount > 0) {
						oSettings.oInstance.$('.delete-check').hide();
					}

				}
			});
		}
		$(parentDiv + ' div.dataTables_scrollBody').scroll(
				function() {
					$(parentDiv + ' .popover').remove();
				});
		this.checkDataTable.$('.delete-check').on('click', function() {
			var entryNumber = $(this).parent().parent().data('entry'),
			gid = '' + $(this).parent().parent().data('gid');
			deleteCheckGermplasmList(entryNumber, gid, $(this).parent().parent());
		});
		SelectedCheckListDataTable.prototype.getDataTable = function()
		{
			return this.checkDataTable;
		};
		SelectedCheckListDataTable.prototype.getDataTableColumnIndex = function(colName)
		{
			var colNames = this.checkDataTable.fnSettings().aoColumns;
			for (var counter = 0 ; counter < colNames.length ; counter++) {
				if (colNames[counter].data === colName) {
					return colNames[counter].idx;
				}
			}
			return -1;
		};
	};

	return dataTableConstructor;

})(jQuery);

BMS.Fieldbook.AdvancedGermplasmListDataTable = (function($) {

	
	/**
	 * Creates a new AdvancedGermplasmListDataTable. This Datatable is the summary table view of the Advanced Germplasm list
	 *
	 * @constructor
	 * @alias module:fieldbook-datatable
	 * @param {string} tableIdentifier the id of the table container
	 * @param {string} parentDiv parentdiv of that contains the table
	 * @param {dataList} json representation of the data to be displayed
	 */
	var dataTableConstructor = function AdvancedGermplasmListDataTable(tableIdentifier, parentDiv, dataList) {
		'use strict';

		var germplasmDataTable;
		var _columnDefs = [
			// Column defs for trialInstanceNumber and replicationNumber (hide if current study is nursery)
			// From Datatable API, using negative index counts from the last index of the columns (n-1)
			{
				targets: [ -1, -2 ],
				visible: !isNursery()
			},
			// column defs for the entry checkbox selection, fix width
			{
				targets: [0],
				width: '38px'
			}
		];

		if ($.fn.dataTable.isDataTable($(tableIdentifier))) {
			this.germplasmDataTable = $(tableIdentifier).DataTable();
			this.germplasmDataTable.clear();
			this.germplasmDataTable.rows.add(dataList).draw();
		} else {
			this.germplasmDataTable = $(tableIdentifier).dataTable({
				columnDefs: _columnDefs,
				autoWidth: false,
				retrieve: true,
				scrollY: '500px',
				scrollX: '100%',
				scrollCollapse: true,
				dom: 'R<t><"fbk-page-div"p>',
				iDisplayLength: 100,
				fnDrawCallback: function(oSettings) {
					makeGermplasmListDraggable(true);
				},

				fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
					$(nRow).data('entry', aData.entry);
					$(nRow).data('gid', aData.gid);
					$('td', nRow).attr('nowrap', 'nowrap');
					return nRow;
				},
				fnInitComplete: function(oSettings, json) {
					var totalPages = oSettings._iDisplayLength === -1 ? 0 : Math.ceil(oSettings.fnRecordsDisplay() / oSettings._iDisplayLength);
					if (totalPages === 1) {
						$(parentDiv + ' .fbk-page-div').addClass('fbk-hide');
					}
					$(parentDiv).removeClass('fbk-hide-opacity');
					oSettings.oInstance.fnAdjustColumnSizing();
					oSettings.oInstance.api().colResize.init(oSettings.oInit.colResize);
					oSettings.oInstance.fnAdjustColumnSizing();
				}
			});
		}

		AdvancedGermplasmListDataTable.prototype.getDataTable = function()
		{
			return this.germplasmDataTable;
		};
	};
	return dataTableConstructor;
})(jQuery);

BMS.Fieldbook.FinalAdvancedGermplasmListDataTable = (function($) {

	
	/**
	 * Creates a new AdvancedGermplasmListDataTable.
	 *
	 * @constructor
	 * @alias module:fieldbook-datatable
	 * @param {string} tableIdentifier the id of the table container
	 * @param {string} parentDiv parentdiv of that contains the table
	 * @param {dataList} json representation of the data to be displayed
	 */
	var dataTableConstructor = function FinalAdvancedGermplasmListDataTable(tableIdentifier, parentDiv, dataList, tableAutoWidth) {
		'use strict';

		var columns = [],
		columnsDef = [],
		aoColumnsDef = [],
		germplasmDataTable;

		$(tableIdentifier + ' thead tr th').each(function(index) {
			columns.push({data: $(this).data('col-name')});
			if (index === 0) {
				aoColumnsDef.push({bSortable: false});
			} else {
				aoColumnsDef.push(null);
			}

			if ($(this).data('col-name') == 'gid') {
				// For GID
				columnsDef.push({
					targets: columns.length - 1,
					data: $(this).html(),
					width: '100px',
					render: function(data, type, full, meta) {
						return '<a class="gid-link" href="javascript: void(0)" ' +
							'onclick="openGermplasmDetailsPopopWithGidAndDesig(&quot;' +
							full.gid + '&quot;,&quot;' + full.desig + '&quot;)">' + data + '</a>';
					}
				});
			} else if ($(this).data('col-name') == 'desig') {
				// For designation
				columnsDef.push({
					targets: columns.length - 1,
					data: $(this).html(),
					render: function(data, type, full, meta) {
						return '<a class="desig-link" href="javascript: void(0)" ' +
							'onclick="openGermplasmDetailsPopopWithGidAndDesig(&quot;' +
							full.gid + '&quot;,&quot;' + full.desig + '&quot;)">' + data + '</a>';
					}
				});
			}
		});

		if ($.fn.dataTable.isDataTable($(tableIdentifier))) {
			this.germplasmDataTable = $(tableIdentifier).DataTable();
			this.germplasmDataTable.clear();
			this.germplasmDataTable.rows.add(dataList).draw();
		} else {
			this.germplasmDataTable = $(tableIdentifier).dataTable({
				autoWidth: tableAutoWidth,
				retrieve: true,
				scrollY: '500px',
				scrollX: '100%',
				scrollCollapse: true,
				aoColumns: aoColumnsDef,
				lengthMenu: [[50, 75, 100, -1], [50, 75, 100, 'All']],
				dom: 'R<"mdt-header" rli><t><"fbk-page-div"p>',

				iDisplayLength: 100,
				fnDrawCallback: function(oSettings) {
					$(parentDiv + ' #selectAllAdvance').prop('checked', false);
					$(parentDiv + ' #selectAllAdvance').change();
					$(parentDiv + ' input.advancingListGid:checked').parent().parent().addClass('selected');
					$(parentDiv + ' .numberOfAdvanceSelected').html($(parentDiv + ' tr.primaryRow.selected').length);
				},
				fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
					$(nRow).data('entry', aData.entry);
					$(nRow).data('gid', aData.gid);
					$('td', nRow).attr('nowrap', 'nowrap');
					return nRow;
				},
				fnInitComplete: function(oSettings, json) {
					var totalPages = oSettings._iDisplayLength === -1 ? 0 : Math.ceil(oSettings.fnRecordsDisplay() / oSettings._iDisplayLength);
					if (totalPages === 1) {
						$(parentDiv + ' .fbk-page-div').addClass('fbk-hide');
					}
					BMS.Fieldbook.checkPagination(parentDiv);
					$(parentDiv).removeClass('fbk-hide-opacity');
					oSettings.oInstance.fnAdjustColumnSizing();
					oSettings.oInstance.api().colResize.init(oSettings.oInit.colResize, tableIdentifier);
					$(parentDiv + ' .dataTables_length select').select2({minimumResultsForSearch: 10});
					oSettings.oInstance.fnAdjustColumnSizing();
				}
			});
		}

		FinalAdvancedGermplasmListDataTable.prototype.getDataTable = function()
		{
			return this.germplasmDataTable;
		};
	};

	return dataTableConstructor;

})(jQuery);

BMS.Fieldbook.StockListDataTable = (function($) {

	/**
	 * Creates a new StockListDataTable.
	 *
	 * @constructor
	 * @alias module:fieldbook-datatable
	 * @param {string} tableIdentifier the id of the table container
	 * @param {string} parentDiv parentdiv of that contains the table
	 * @param {dataList} json representation of the data to be displayed
	 */
	var dataTableConstructor = function StockListDataTable(tableIdentifier, parentDiv, dataList, tableAutoWidth) {
		'use strict';

		var columns = [],
		aoColumnsDef = [],
		stockTable;

		$(tableIdentifier + ' thead tr th').each(function(index) {
			columns.push({data: $(this).data('col-name')});
			if (index === 0) {
				aoColumnsDef.push({bSortable: false});
			} else {
				aoColumnsDef.push(null);
			}


		});
		this.stockTable = $(tableIdentifier).dataTable({
			autoWidth: tableAutoWidth,
			scrollY: '500px',
			scrollX: '100%',
			scrollCollapse: true,
            retrieve: true,
			aoColumns: aoColumnsDef,
			lengthMenu: [[50, 75, 100, -1], [50, 75, 100, 'All']],
			dom: 'R<"mdt-header" rli><t><"fbk-page-div"p>',

			iDisplayLength: 100,
			fnDrawCallback: function(oSettings) {
				
				var selectedRowCount = 0;
				$(oSettings.oInstance.fnGetNodes()).each(function(i, row){
						if ($('input.stockListEntryId:checked', row).length !== 0){
							$(row).addClass('selected');
							selectedRowCount++;
						}
					}
				);
				
				$(parentDiv + ' .numberOfAdvanceSelected').html(selectedRowCount);
			},
			fnInitComplete: function(oSettings, json) {

				var totalPages = oSettings._iDisplayLength === -1 ? 0 : Math.ceil(oSettings.fnRecordsDisplay() / oSettings._iDisplayLength);
				if (totalPages === 1) {
					$(parentDiv + ' .fbk-page-div').addClass('fbk-hide');
				}
				BMS.Fieldbook.checkPagination(parentDiv);
				$(parentDiv).removeClass('fbk-hide-opacity');
				oSettings.oInstance.fnAdjustColumnSizing();
				oSettings.oInstance.api().colResize.init(oSettings.oInit.colResize, tableIdentifier);
				$(parentDiv + ' .dataTables_length select').select2({minimumResultsForSearch: 10});
				oSettings.oInstance.fnAdjustColumnSizing();
			}
		});

		StockListDataTable.prototype.getDataTable = function()
		{
			return this.stockTable;
		};
	};

	return dataTableConstructor;

})(jQuery);

BMS.Fieldbook.PreviewDesignMeasurementsDataTable = (function($) {
	
	/**
	 * Creates a new PreviewDesignMeasurementsDataTable.
	 *
	 * @constructor
	 * @alias module:fieldbook-datatable
	 * @param {string} tableIdentifier the id of the table container
	 * @param {string} ajaxUrl the URL from which to retrieve table data
	 */
	var dataTableConstructor = function PreviewDesignMeasurementsDataTable(tableIdentifier, dataList) {
		'use strict';

		var columns = [],
			columnsDef = [],
			table;

		$(tableIdentifier + ' thead tr th').each(function() {
			// The undefined data upsets the datatable library and it gives a warning message -
			// "DataTables warning: table id={id} - Requested unknown parameter '{parameter}' for row {row-index}, column{column-index}`"
			// See http://datatables.net/manual/tech-notes/4
			// we need to set the {{defaultContent}} option so that nulls and undefined values were shown as empty string
			columns.push({
				data: $(this).html(),
				defaultContent: ''
			});
			if ($(this).data('term-id') == '8240') {
				// For GID
				columnsDef.push({
					targets: columns.length - 1,
					data: $(this).html(),
					width: '100px',
					render: function(data, type, full, meta) {
						return '<a class="gid-link" href="javascript: void(0)" ' +
							'onclick="openGermplasmDetailsPopopWithGidAndDesig(&quot;' +
							full.GID + '&quot;,&quot;' + full.DESIGNATION + '&quot;)">' + EscapeHTML.escape(data) + '</a>';
					}
				});
			} else if ($(this).data('term-id') == '8250') {
				// For designation
				columnsDef.push({
					targets: columns.length - 1,
					data: $(this).html(),
					render: function(data, type, full, meta) {
						return '<a class="desig-link" href="javascript: void(0)" ' +
							'onclick="openGermplasmDetailsPopopWithGidAndDesig(&quot;' +
							full.GID + '&quot;,&quot;' + full.DESIGNATION + '&quot;)">' + EscapeHTML.escape(data) + '</a>';
					}
				});
			} else {
				columnsDef.push({
					targets: columns.length - 1,
					render: function(data, type, full, meta) {
						if (data !== undefined) {
							if (Array.isArray(data)) {
								return EscapeHTML.escape((data[0] != null) ? data[0] :  '');
							} else {
								return EscapeHTML.escape(data);
							}
						}
					}
				});
			}

		});

		if ($.fn.dataTable.isDataTable($(tableIdentifier))) {
			table = $(tableIdentifier).DataTable();
			table.clear();
			table.rows.add(dataList).draw();
		} else {
			table = $(tableIdentifier).DataTable({
				data: dataList,
				columns: columns,
				retrieve: true,
				scrollY: '600px',
				scrollX: '100%',
				scrollCollapse: true,
				columnDefs: columnsDef,
				lengthMenu: [[50, 75, 100, -1], [50, 75, 100, 'All']],
				bAutoWidth: true,
				iDisplayLength: 100,
				dom: 'R<<"mdt-header"rli<"mdt-filtering">r><t>p>',
				// For column visibility
				colVis: {
					exclude: [0],
					restore: 'Restore',
					showAll: 'Show all'
				},
				// Problem with reordering plugin and fixed column for column re-ordering
				colReorder: {
					fixedColumns: 1
				},
				fnInitComplete: function(oSettings, json) {
					oSettings.oInstance.fnAdjustColumnSizing();
				}
			});
		}
	};
	return dataTableConstructor;

})(jQuery);

$(function() {
	$(document).contextmenu({
		delegate: ".dataTable td[class*='invalid-value']",
		menu: [
			{title: 'Accept Value', cmd: 'markAccepted'},
			{title: 'Mark Missing', cmd: 'markMissing'}
		],
		select: function(event, ui) {
			var colvindex = $(ui.target.parent()).data('row-index');
			var termId = $(ui.target).data('term-id');
			if (termId == null) {
				termId = $(ui.target).parents('td').data('term-id');
				colvindex = $(ui.target).parents('tr').data('row-index');
				ui.target = $(ui.target).parents('td');
			}
			switch (ui.cmd) {
				case 'markAccepted':
					markCellAsAccepted(colvindex, termId, ui.target);
					break;
				case 'markMissing':
					markCellAsMissing(colvindex, termId, 'missing', 1, ui.target);
					break;
			}
		},
		beforeOpen: function(event, ui) {
			var $menu = ui.menu,
				$target = ui.target,
				extraData = ui.extraData;
			ui.menu.zIndex(9999);
		}
	});
});
