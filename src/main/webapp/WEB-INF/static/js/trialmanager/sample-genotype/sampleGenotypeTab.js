(function () {
	'use strict';

	var sampleGenotypeModule = angular.module('sample-genotype-tab', []);

	var TRIAL_INSTANCE = 8170,
		GID = 8240,
		OBS_UNIT_ID = 8201;
	var SAMPLE_NAME = -12,
		SAMPLE_UUID = -13,
		SAMPLING_DATE = -14,
		TAKEN_BY = -15;
	var hiddenColumns = [OBS_UNIT_ID, TRIAL_INSTANCE];

	sampleGenotypeModule.controller('SampleGenotypeCtrl',
		['$rootScope', '$scope', '$q', '$compile', '$uibModal', '$timeout', 'studyContext', 'DTOptionsBuilder', 'sampleGenotypeService', 'sampleListService',
			function ($rootScope, $scope, $q, $compile, $uibModal, $timeout, studyContext, DTOptionsBuilder, sampleGenotypeService, sampleListService) {


				// used also in tests - to call $rootScope.$apply()
				var tableLoadedResolve;
				$scope.tableLoadedPromise = new Promise(function (resolve) {
					tableLoadedResolve = resolve;
				});

				var tableRenderedResolve;
				$scope.tableRenderedPromise = new Promise(function (resolve) {
					tableRenderedResolve = resolve;
				});

				$scope.columnsObj = [];
				$scope.columnsData = [];
				$scope.nested = {};
				$scope.nested.dtInstance = null;
				$scope.nested.sampleLists = [];
				$scope.nested.selectedSampleListId = null;
				$scope.isCategoricalDescriptionView = window.isCategoricalDescriptionView;

				var dtColumnsPromise = $q.defer();
				var dtColumnDefsPromise = $q.defer();

				$scope.dtColumns = dtColumnsPromise.promise;
				$scope.dtColumnDefs = dtColumnDefsPromise.promise;
				$scope.dtOptions = null;

				$scope.totalItems = 0;
				$scope.selectedItems = [];
				$scope.isAllPagesSelected = false;

				$scope.columnFilter = {
					selectAll: function () {
						this.columnData.possibleValues.forEach(function (value) {
							value.isSelectedInFilters = this.columnData.isSelectAll;
						}.bind(this));
					},
					selectOption: function (selected) {
						if (!selected) {
							this.columnData.isSelectAll = false;
						}
					},
					search: function (item) {
						var query = $scope.columnFilter.columnData.query;
						if (!query) {
							return true;
						}
						if (item.name.indexOf(query) !== -1 || item.displayDescription.indexOf(query) !== -1) {
							return true;
						}
						return false;
					}
				};
				$scope.datepickerOptions = {
					showWeeks: false
				};
				$scope.selectedStatusFilter = "1";

				loadSampleLists();

				$scope.selectVariableCallback = function (responseData) {
					// just override default callback (see VariableSelection.prototype._selectVariable)
				};

				$scope.onHideCallback = function () {
					adjustColumns();
				};

				$scope.openColumnFilter = function (index) {
					$scope.columnFilter.index = index;
					$scope.columnFilter.columnData = $scope.columnsObj.columns[index].columnData;
					if ($scope.columnFilter.columnData.sortingAsc != null && !table().order().some(function (order) {
						return order[0] === index;
					})) {
						$scope.columnFilter.columnData.sortingAsc = null;
					}
				};


				$scope.hasDataFiltered = function () {
					return $scope.nested.dtInstance && //
						$scope.nested.dtInstance.DataTable && //
						$scope.nested.dtInstance.DataTable.data() && //
						$scope.nested.dtInstance.DataTable.data().length > 0 || false;
				};

				$scope.filterByColumn = function () {
					table().ajax.reload();
				};

				$scope.resetFilterByColumn = function (index) {
					resetFilterByColumnByIndex(index);
					table().ajax.reload();
				};

				$scope.sortColumn = function (asc) {
					$scope.columnFilter.columnData.sortingAsc = asc;
					table().order([$scope.columnFilter.index, asc ? 'asc' : 'desc']).draw();
				};

				$scope.getFilteringByClass = function (index) {
					if (!$scope.columnsObj.columns[index]) {
						return;
					}
					var columnData = $scope.columnsObj.columns[index].columnData;
					if (columnData.isFiltered) {
						return 'filtering-by';
					}
				};

				$scope.changeSelectedSampleList = function () {
					loadTable();
				}

				$scope.showColumnSorting = function (termId) {
					return termId !== TAKEN_BY;
				}

				const reloadDataTableWithTimeout = {
					reload() {
						table().ajax.reload(null, false);
					},

					setup(timeout) {
						if (typeof this.timeoutID === 'number') {
							this.cancel();
						}

						this.timeoutID = setTimeout((msg) => {
							this.reload();
						}, timeout);
					},

					cancel() {
						clearTimeout(this.timeoutID);
					}
				};

				function resetFilterByColumnByIndex(index) {
					var columnData = $scope.columnsObj.columns[index].columnData;
					columnData.query = '';
					columnData.sortingAsc = null;
					if (columnData && columnData.possibleValues) {
						columnData.possibleValues.forEach(function (value) {
							value.isSelectedInFilters = false;
						});
						columnData.isSelectAll = false;
					}
				}

				function table() {
					return $scope.nested.dtInstance.DataTable;
				}

				function getVisibleColumns() {
					var visibleColumns = [];
					$scope.columnsObj.columns.forEach(function (column, index) {
						if (column && column.name) {
							// If datatable is already initialized and loaded, get the visibility value from the datatable (ColVis),
							// else return the default visibility of the column
							var isVisible = $scope.nested.dtInstance ? table().columns().visible()[index] : column.visible;
							if (isVisible) {
								// Add the actual name of the column, not the alias.
								visibleColumns.push(column.columnData.name);
							}
						}
					});
					return visibleColumns;
				}

				function getFilter() {
					return {
						sampleListIds: [$scope.nested.selectedSampleListId],
						filteredValues: $scope.columnsObj.columns.reduce(function (map, column) {
							var columnData = column.columnData;
							columnData.isFiltered = false;

							if (isTextFilter(columnData)) {
								return map;
							}

							if (columnData.possibleValues) {
								columnData.possibleValues.forEach(function (value) {
									if (value.isSelectedInFilters) {
										if (!map[columnData.termId]) {
											map[columnData.termId] = [];
										}
										map[columnData.termId].push(value.name);
									}
								});
								if (!map[columnData.termId] && columnData.query) {
									map[columnData.termId] = [columnData.query];
								}
							} else if (columnData.query) {
								map[columnData.termId] = [(columnData.query)];
							}

							if (map[columnData.termId]) {
								columnData.isFiltered = true;
							}
							return map;
						}, {}),
						filteredTextValues: $scope.columnsObj.columns.reduce(function (map, column) {
							var columnData = column.columnData;
							if (!isTextFilter(columnData)) {
								return map;
							}
							if (columnData.query) {
								if (columnData.dataType === 'Date') {
									map[columnData.termId] = ($.datepicker.formatDate("yy-mm-dd", columnData.query));
								} else {
									map[columnData.termId] = columnData.query;
								}
								columnData.isFiltered = true;
							}
							return map;
						}, {}),
						variableTypeMap: $scope.columnsObj.columns.reduce(function (map, column) {
							map[column.columnData.termId] = column.columnData.variableType;
							return map;
						}, {})
					};
				}

				function isTextFilter(columnData) {

					// Factors like GID should be treated as numeric
					if (columnData.termId === GID) {
						return false;
					}

					if (columnData.dataType === 'Categorical' || columnData.dataType === 'Numeric') {
						return false;
					}

					return true;

				}

				function getDtOptions() {
					return addCommonOptions(DTOptionsBuilder.newOptions()
						.withOption('ajax',
							function (d, callback) {
								$.ajax({
									type: 'POST',
									url: sampleGenotypeService.getSampleGenotypesTableUrl() + getPageQueryParameters(d),
									data: JSON.stringify({
										draw: d.draw,
										filter: getFilter(),
										studyId: studyContext.studyId,
										visibleColumns: getVisibleColumns()
									}),
									success: function (res, status, xhr) {
										let json = {recordsTotal: 0, recordsFiltered: 0}
										json.recordsTotal = xhr.getResponseHeader('X-Total-Count');
										json.recordsFiltered = xhr.getResponseHeader('X-Filtered-Count');
										json.data = res;
										callback(json);
									},
									contentType: 'application/json',
									beforeSend: function (xhr) {
										xhr.setRequestHeader('X-Auth-Token', JSON.parse(localStorage['bms.xAuthToken']).token);
									},
								});
							})
						.withDataProp('data')
						.withOption('serverSide', true)
						.withOption('initComplete', initCompleteCallback)
						.withOption('headerCallback', headerCallback)
						.withOption('drawCallback', drawCallback));
				}

				function getPageQueryParameters(data) {
					var order = data.order && data.order[0];
					var pageQuery = '?size=' + data.length
						+ '&page=' + ((data.length === 0) ? 0 : data.start / data.length);
					var columnData = $scope.columnsData[order.column];
					if (columnData) {
						var sort = columnData.name;
						pageQuery += '&sort=' + sort + ',' + order.dir;
					}
					return pageQuery;
				}

				function addCommonOptions(options) {
					return options
						.withOption('processing', true)
						.withOption('lengthMenu', [[50, 75, 100], [50, 75, 100]])
						.withOption('scrollY', '500px')
						.withOption('scrollCollapse', true)
						.withOption('scrollX', '100%')
						.withOption('language', {
							processing: '<span class="throbber throbber-2x"></span>',
							lengthMenu: 'Records per page: _MENU_',
							paginate: {
								next: '>',
								previous: '<',
								first: '<<',
								last: '>>'
							}
						})
						.withDOM('<"pull-left fbk-left-padding"r>' + //
							'<"pull-right"B>' + //
							'<"clearfix">' + //
							'<"row add-top-padding-small"<"col-sm-12"t>>' + //
							'<"row"<"col-sm-12 paginate-float-center"<"pull-left"i><"pull-right"l>p>>')
						.withButtons([{
							extend: 'colvis',
							className: 'fbk-buttons-no-border fbk-colvis-button',
							text: '<i class="glyphicon glyphicon-th"></i>'
						}])
						.withColReorder()
						.withColReorderCallback(colReorderCallback)
						.withColReorderOption('iFixedColumnsLeft', $scope.isFileStorageConfigured ? 2 : 1)
						.withPaginationType('full_numbers');
				}

				function colReorderCallback() {
					table().ajax.reload();
				}

				function initCompleteCallback() {
					table().columns().every(function () {
						$(this.header())
							.prepend($compile('<span class="glyphicon glyphicon-bookmark" style="margin-right: 10px; color:#1b95b2;"' +
								' ng-if="isVariableBatchActionSelected(' + this.index() + ')"> </span>')($scope))
							.append($compile('<span class="glyphicon glyphicon-filter" ' +
								' style="cursor:pointer; padding-left: 5px;"' +
								' popover-placement="bottom"' +
								' ng-class="getFilteringByClass(' + this.index() + ')"' +
								' popover-append-to-body="true"' +
								' popover-trigger="\'outsideClick\'"' +
								// does not work with outsideClick
								// ' popover-is-open="columnFilter.isOpen"' +
								' ng-click="openColumnFilter(' + this.index() + ')"' +
								' uib-popover-template="\'columnFilterPopoverTemplate.html\'"></span>')($scope))
					});
					adjustColumns();
					tableRenderedResolve();
					addColumnVisibilityCallback();
				}

				function headerCallback(thead, data, start, end, display) {
					table().columns().every(function () {
						var column = $scope.columnsObj.columns[this.index()];
						if (column.columnData.formula) {
							$(this.header()).addClass('derived-trait-column-header');
						}
					});
				}

				function drawCallback() {
					adjustColumns();
				}

				function adjustColumns() {
					$timeout(function () {
						table().columns.adjust();
					});
				}

				$scope.allTableItems = function () {
					return table().context[0].json && table().context[0].json['recordsFiltered'];
				};

				function addColumnVisibilityCallback() {
					table().on('column-visibility.dt', function (e, settings, columnIndex, isVisible) {
						// If the column is not visible, we should reset the column filter
						if (!isVisible) {
							resetFilterByColumnByIndex(columnIndex);
						}
						// When this callback function is executed multiple times consecutively in a span of 1500 milliseconds
						// Then make sure that the datatables is only reloaded once
						reloadDataTableWithTimeout.setup(1500);
					});
				}

				function loadSampleLists() {
					sampleListService.getSampleListsInStudy(true).then(function (sampleLists) {
						$scope.nested.sampleLists = sampleLists;
						$scope.nested.selectedSampleListId = sampleLists[0].listId;
						loadTable();
					});
				}

				function loadTable() {
					/**
					 * We need to reinitilize all this because
					 * if we use column.visible an change the columns with just
					 *        $scope.dtColumns = columnsObj.columns;
					 * datatables is breaking with error:
					 * Cannot read property 'clientWidth' of null
					 */
					var dtColumnsPromise = $q.defer();
					var dtColumnDefsPromise = $q.defer();
					$scope.dtColumns = dtColumnsPromise.promise;
					$scope.dtColumnDefs = dtColumnDefsPromise.promise;
					$scope.dtOptions = null;

					return loadColumns().then(function (columnsObj) {
						$scope.dtOptions = getDtOptions();
						angular.forEach(columnsObj.columns, function (column, index) {
							// "PLOT_NO"
							if (column.columnData.termId === 8200) {
								$scope.dtOptions.withOption('order', [index, 'asc']);
							}
						});

						dtColumnsPromise.resolve(columnsObj.columns);
						dtColumnDefsPromise.resolve(columnsObj.columnsDef);

						tableLoadedResolve();
					});
				}

				function loadColumns() {
					return sampleGenotypeService.getColumns([$scope.nested.selectedSampleListId]).then(function (columnsData) {
						$scope.columnsData = columnsData.slice();
						var columnsObj = $scope.columnsObj = mapColumns(columnsData);
						return columnsObj;
					});
				}

				function mapColumns(columnsData) {
					$scope.columnDataByInputTermId = {};

					var columns = [],
						columnsDef = [];

					angular.forEach(columnsData, function (columnData, index) {
						if (columnData.possibleValues) {
							columnData.possibleValuesByName = {};
							columnData.possibleValuesById = {};
							angular.forEach(columnData.possibleValues, function (possibleValue) {
								// so we can use "Please Choose"=empty value
								possibleValue.displayValue = possibleValue.name;
								// convenience map to avoid looping later
								columnData.possibleValuesByName[possibleValue.name] = possibleValue;
								columnData.possibleValuesById[possibleValue.id] = possibleValue;
							});
							// waiting for https://github.com/angular-ui/ui-select/issues/152
							columnData.possibleValues.unshift({
								name: '', displayValue: 'Please Choose', displayDescription: 'Please Choose'
							});
						}

						// store formula info to update out-of-sync status after edit
						if (columnData.formula && columnData.formula.inputs) {
							columnData.formula.inputs.forEach(function (input) {
								if (!$scope.columnDataByInputTermId[input.id]) {
									$scope.columnDataByInputTermId[input.id] = [];
								}
								$scope.columnDataByInputTermId[input.id].push(columnData);
							});
						}
						columnData.index = index;


						function isColumnVisible() {
							return hiddenColumns.indexOf(columnData.termId) < 0;
						}

						function getClassName() {
							var className = columnData.factor === true ? 'factors' : 'variates';
							// include data type for determining when to show context menu option/s
							className += ' datatype-' + columnData.dataTypeId;
							className += ' termId-' + columnData.termId;
							// avoid wrapping filter icon
							className += ' dt-head-nowrap';
							return className;
						}

						columns.push({
							title: columnData.alias,
							name: columnData.alias,
							data: function (row) {
								return row.genotypeDataMap[columnData.name];
							},
							visible: isColumnVisible(),
							defaultContent: '',
							className: getClassName(),
							columnData: columnData
						});

						if (columnData.termId === 8240 || columnData.termId === 8250) {
							// GID or DESIGNATION
							columnsDef.push({
								targets: columns.length - 1,
								orderable: false,
								render: function (data, type, full, meta) {
									return '<a class="gid-link" href="javascript: void(0)" ' +
										'onclick="openGermplasmDetailsPopup(\'' +
										full.genotypeDataMap['GID'].value + '\')">' + EscapeHTML.escape(data.value) + '</a>';
								}
							});
						} else if (columnData.termId === SAMPLE_NAME) {
							columnsDef.push({
								targets: columns.length - 1,
								orderable: false,
								render: function (data, type, full, meta) {
									var escapedValue = EscapeHTML.escape(full.sampleName);
									return '<span class="text-ellipsis" title="' + escapedValue + '">' + escapedValue + '</span>';
								}
							});
						} else if (columnData.termId === SAMPLE_UUID) {
							columnsDef.push({
								targets: columns.length - 1,
								orderable: false,
								render: function (data, type, full, meta) {
									var escapedValue = EscapeHTML.escape(full.sampleUUID);
									return '<span class="text-ellipsis" title="' + escapedValue + '">' + escapedValue + '</span>';
								}
							});
						} else if (columnData.termId === SAMPLING_DATE) {
							columnsDef.push({
								targets: columns.length - 1,
								orderable: false,
								render: function (data, type, full, meta) {
									var escapedValue = EscapeHTML.escape(full.samplingDate);
									return '<span class="text-ellipsis" title="' + escapedValue + '">' + escapedValue + '</span>';
								}
							});
						} else if (columnData.termId === TAKEN_BY) {
							columnsDef.push({
								targets: columns.length - 1,
								orderable: false,
								render: function (data, type, full, meta) {
									var escapedValue = EscapeHTML.escape(full.takenBy);
									return '<span class="text-ellipsis" title="' + escapedValue + '">' + escapedValue + '</span>';
								}
							});
						} else {
							columnsDef.push({
								targets: columns.length - 1,
								orderable: false,
								render: function (data, type, full, meta) {

									if (!data) {
										return '';
									}

									if (columnData.dataTypeId === 1130) {
										return renderCategoricalValue(data.value, columnData);
									}

									var escapedValue = EscapeHTML.escape(data.value);
									return '<span class="text-ellipsis" title="' + escapedValue + '">' + escapedValue + '</span>';
								}
							});
						}
					});

					return {
						columns: columns,
						columnsDef: columnsDef
					};
				}

				function renderCategoricalValue(value, columnData) {
					var possibleValue = null;

					if (columnData.possibleValues) {
						/* FIXME fix data model
						 *  Some variables don't store the cvterm.name (like traits in phenotype)
						 *  but the cvterm.cvterm_id (like treatment factors in nd_experimentprop).
						 *  This workaround will work most of the time with exception of out-of-bound categorical values that coincides
						 *  with the cvterm_id, though it's unlikely because the ids are not small numbers and it's not possible now to insert
						 *  outliers for categorical variables.
						 */
						possibleValue = columnData.possibleValuesByName[value] || columnData.possibleValuesById[value];
					}

					if (possibleValue
						&& possibleValue.displayDescription
						&& value !== 'missing') {

						var categoricalNameDom = '<span class="fbk-measurement-categorical-name"'
							+ ($scope.isCategoricalDescriptionView ? ' style="display: none; "' : '')
							+ ' >'
							+ EscapeHTML.escape(possibleValue.name) + '</span>';
						var categoricalDescDom = '<span class="fbk-measurement-categorical-desc"'
							+ (!$scope.isCategoricalDescriptionView ? ' style="display: none; "' : '')
							+ ' >'
							+ EscapeHTML.escape(possibleValue.displayDescription) + '</span>';

						value = categoricalNameDom + categoricalDescDom;
					}
					return value;
				}

				$scope.toggleShowCategoricalDescription = function () {
					switchCategoricalView().done(function () {
						$('.fbk-measurement-categorical-name').toggle();
						$('.fbk-measurement-categorical-desc').toggle();

						$scope.$apply(function () {
							$scope.isCategoricalDescriptionView = window.isCategoricalDescriptionView;
							adjustColumns();
						});
					});
				};

				function switchCategoricalView(showCategoricalDescriptionView) {
					'use strict';

					if (typeof showCategoricalDescriptionView === 'undefined') {
						showCategoricalDescriptionView = null;
					}

					return $.get('/Fieldbook/TrialManager/openTrial/setCategoricalDisplayType', {showCategoricalDescriptionView: showCategoricalDescriptionView})
						.done(function (result) {
							window.isCategoricalDescriptionView = result;

							$('.fbk-toggle-categorical-display').text(result ? window.measurementObservationMessages.hideCategoricalDescription :
								window.measurementObservationMessages.showCategoricalDescription);

						});
				}

			}]);
})();
