/*global angular, openListTree, displaySelectedGermplasmDetails*/

(function () {
	'use strict';

	var manageTrialAppModule = angular.module('manageTrialApp');

	manageTrialAppModule.controller('GermplasmCtrl',
		['$scope', '$rootScope', '$q', '$compile', 'TrialManagerDataService', 'DTOptionsBuilder', 'studyStateService', 'studyEntryService', 'germplasmStudySourceService',
			'datasetService', '$timeout', '$uibModal', 'germplasmDetailsModalService', 'studyEntryObservationService', 'DATASET_TYPES', 'VARIABLE_TYPES', '$http', 'studyContext',
			function ($scope, $rootScope, $q, $compile, TrialManagerDataService, DTOptionsBuilder, studyStateService, studyEntryService, germplasmStudySourceService,
					  datasetService, $timeout, $uibModal, germplasmDetailsModalService, studyEntryObservationService, DATASET_TYPES, VARIABLE_TYPES, $http, studyContext) {

				$scope.settings = TrialManagerDataService.settings.germplasm;
				$scope.entryDetails = TrialManagerDataService.settings.entryDetails;
				$scope.isLockedStudy = TrialManagerDataService.isLockedStudy;
				$scope.trialMeasurement = {hasMeasurement: studyStateService.hasGeneratedDesign()};
				$scope.isHideDelete = studyStateService.hasGeneratedDesign();
				$scope.addVariable = !studyStateService.hasGeneratedDesign() && TrialManagerDataService.applicationData.germplasmListSelected;
				$scope.showAddColumns = TrialManagerDataService.applicationData.germplasmListSelected;
				$scope.selectedItems = [];
				$scope.numberOfEntries = 0;
				$scope.entryTableColumns = [];

				var initResolve;
				$scope.initPromise = new Promise(function (resolve) {
					initResolve = resolve;
				});
				var dtColumnsPromise = $q.defer();
				var dtColumnDefsPromise = $q.defer();

				$scope.dtColumns = dtColumnsPromise.promise;
				$scope.dtColumnDefs = dtColumnDefsPromise.promise;
				$scope.dtOptions = null;

				$scope.isCategoricalDescriptionView = window.isCategoricalDescriptionView;

				var tableRenderedResolve;
				$scope.tableRenderedPromise = new Promise(function (resolve) {
					tableRenderedResolve = resolve;
				});

				$scope.generationLevel = TrialManagerDataService.generationLevel() ? TrialManagerDataService.generationLevel() : 1;
				$scope.generationLevels = Array.from(Array(10).keys()).map((k) => k + 1);

				loadTable();
				loadStudyEntryColumns();

				$rootScope.$on("reloadStudyEntryTableData", function(setShowValues){
					$scope.reloadStudyEntryTableData(setShowValues);
				});

				function loadStudyEntryColumns() {
					studyEntryService.getStudyEntriesColumns().then(function (columnsData) {
						$scope.entryTableColumns = columnsData;
					});
				}

				function table() {
					return $scope.nested.dtInstance.DataTable;
				}

				function getDtOptions() {
					return addCommonOptions(DTOptionsBuilder.newOptions()
						.withOption('ajax',
							function (d, callback) {
								$.ajax({
									type: 'POST',
									url: studyEntryService.getStudyEntriesUrl() + getPageQueryParameters(d),
									dataSrc: '',
									success: function (res, status, xhr) {
										let json = {recordsTotal: 0, recordsFiltered: 0};
										json.recordsTotal = json.recordsFiltered = xhr.getResponseHeader('X-Total-Count');
										json.data = res;
										setNumberOfEntries(json.recordsTotal);
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
						.withOption('drawCallback', drawCallback)
					);
				}

				function initCompleteCallback() {
					table().columns().every(function () {
						$(this.header())
							.append($compile('<span class="glyphicon glyphicon-edit" ' +
								' style="cursor:pointer; padding-left: 5px;"' +
								' popover-placement="bottom"' +
								' popover-append-to-body="true"' +
								' popover-trigger="\'outsideClick\'"' +
								// does not work with outsideClick
								' ng-if="checkColumnByTermId(' + this.index() + ', 8377)"' +
								' ng-show="!isLockedStudy()"' +
								' uib-popover-template="\'crossOptionsPopover.html\'"></span>')($scope))
					});
					adjustColumns();
					tableRenderedResolve();
				}

				$scope.checkColumnByTermId = function (index, termId) {
					return $scope.columnsData[index].termId === termId;
				};

				function drawCallback() {
					addCellClickHandler();
					adjustColumns();
				}

				function addCellClickHandler() {
					var $table = angular.element('#germplasm-table');

					addClickHandler();

					function addClickHandler() {
						$table.off('click').on('click', 'td.entry-details.editable:not([disabled])', clickHandler);
					}

					function clickHandler() {
						var cell = this;

						var table = $table.DataTable();
						var dtRow = table.row(cell.parentNode);
						var rowData = dtRow.data();
						var dtCell = table.cell(cell);
						var cellData = dtCell.data();
						var index = table.colReorder.transpose(table.column(cell).index(), 'toOriginal');
						var columnData = $scope.columnsObj.columns[index].columnData;
						var termId = columnData.termId;

						if (!termId) return;

						/**
						 * Remove handler to not interfere with inline editor
						 * will be restored after fnUpdate
						 */
						$table.off('click');

						$scope.$apply(function () {

							var $inlineScope = $scope.$new(true);

							$inlineScope.observation = {
								value: $scope.isPendingView ? cellData.draftValue : cellData.value,
								change: function () {
									updateInline();
								},
								cancel: function () {
									cancelUpdateInline();
								},
								// FIXME altenative to blur bug https://github.com/angular-ui/ui-select/issues/499
								onOpenClose: function (isOpen) {
									if (!isOpen) updateInline();
								},
								newInlineValue: function (newValue) {
									return {name: newValue};
								}
							};

							$inlineScope.columnData = columnData;
							$inlineScope.isCategoricalDescriptionView = $scope.isCategoricalDescriptionView;

							$(cell).html('');
							var editor = $compile(
								' <observation-inline-editor ' +
								' is-categorical-description-view="isCategoricalDescriptionView" ' +
								' column-data="columnData" ' +
								' observation="observation"></observation-inline-editor> '
							)($inlineScope);

							$(cell).append(editor);

							function updateInline() {

								function doAjaxUpdate() {
									if ((!$scope.isPendingView && cellData.value === $inlineScope.observation.value)
										|| ($scope.isPendingView && cellData.draftValue === $inlineScope.observation.value)) {
										return $q.resolve(cellData);
									}

									var value = cellData.value;
									var draftValue = cellData.draftValue;

									if ($scope.isPendingView) {
										draftValue = $inlineScope.observation.value;
									} else {
										value = $inlineScope.observation.value;
									}

									if (cellData.studyEntryPropertyId) {
										if (!value && !$scope.isPendingView) {
											if (cellData.draftValue) {
												value = null;
											} else {
												return studyEntryObservationService.deleteObservation(cellData.studyEntryPropertyId);
											}
										}

										return confirmOutOfBoundData(value, columnData).then(function (doContinue) {
											if (!doContinue) {
												$inlineScope.observation.value = cellData.value;
												return {studyEntryPropertyId: cellData.studyEntryPropertyId};
											}
											return studyEntryObservationService.updateObservation({
												stockId: rowData.entryId,
												variableId: termId,
												value: value,
												categoricalValueId: getCategoricalValueId(value, columnData)
											});
										});
									}

									if (value) {
										return confirmOutOfBoundData(value, columnData).then(function (doContinue) {
											if (!doContinue) {
												$inlineScope.observation.value = cellData.value;
												$inlineScope.observation.value = cellData.value;
												return {observationId: cellData.observationId};
											}
											return studyEntryObservationService.addObservation({
												stockId: rowData.entryId,
												variableId: termId,
												value: value,
												categoricalValueId: getCategoricalValueId(value, columnData)
											});
										});
									}

									return $q.resolve(cellData);
								} // doAjaxUpdate

								var promise = doAjaxUpdate();

								promise.then(function (data) {
									var valueChanged = false;
									if (cellData.value !== $inlineScope.observation.value) {
										valueChanged = true;
									}

									if ($scope.isPendingView) {
										cellData.draftValue = $inlineScope.observation.value;
									} else {
										cellData.value = $inlineScope.observation.value;
									}

									cellData.observationId = data.observationId;
									cellData.status = data.status;

									$inlineScope.$destroy();
									editor.remove();

									/**
									 * We are updating the cell value and the target if the trait is input of a formula
									 * to avoid reloading the page. It has these advantages:
									 * - Make the inline edition more dynamic and fast
									 * - Don't reset the table scroll
									 *
									 * The alternative would be:
									 *
									 *     table.ajax.reload(function () {
									 *         // Restore handler
									 *         $table.off('click').on('click', 'td.entry-details', clickHandler);
									 *     }, false);
									 */
									dtCell.data(cellData);
									processCell(cell, cellData, rowData, columnData);

									if (valueChanged && $scope.columnDataByInputTermId[termId]) {
										angular.forEach($scope.columnDataByInputTermId[termId], function (targetColumnData) {
											var targetColIndex = table.colReorder.transpose(targetColumnData.index, 'toCurrent');
											var targetDtCell = table.cell(dtRow.node(), targetColIndex);
											var targetCellData = targetDtCell.data();
											targetCellData.status = 'OUT_OF_SYNC';
											processCell(targetDtCell.node(), targetCellData, rowData, targetColumnData);
										});
									}

									// Restore handler
									addClickHandler();
									adjustColumns();
								}, function (response) {
									if (!response) {
										// no ajax, local reject / cancel (e.g await modal confirm)
										// keeps inline editor open
										return;
									}
									if (response.errors) {
										showErrorMessage('', response.errors[0].message);
									} else {
										showErrorMessage('', ajaxGenericErrorMsg);
									}
								});

							} // updateInline

							function cancelUpdateInline() {
								$inlineScope.$destroy();
								editor.remove();
								dtCell.data(cellData);
								processCell(cell, cellData, rowData, columnData);
								// Restore handler
								addClickHandler();
								adjustColumns();
							}

							if (columnData.dataTypeCode === 'D') {
								$(cell).one('click', 'input', function () {
									var initialValue;
									try {
										initialValue = $.datepicker.formatDate("yy-mm-dd", $.datepicker.parseDate('yymmdd', $(this).val()));
									} catch (e) {
									}

									$(this).on('keydown', function (e) {
										if (e.keyCode === 13) {
											e.stopImmediatePropagation();
										}
									}).datepicker({
										format: 'yyyymmdd',
										todayHighlight: true,
										todayBtn: true,
										forceParse: false
									}).on('hide', function () {
										updateInline();
									}).datepicker("show").datepicker('update', initialValue)
								});
							}

							// FIXME show combobox for categorical traits
							$(cell).css('overflow', 'visible');

							$timeout(function () {
								/**
								 * Initiate interaction with the input so that clicks on other parts of the page
								 * will trigger blur immediately. Also necessary to initiate datepicker
								 * This also avoids temporary click handler on body
								 * FIXME is there a better way?
								 */
								$(cell).find('a.ui-select-match, input').click().focus();
							}, 100);
						});
					} // clickHandler
				}

				function getCategoricalValueId(cellDataValue, columnData) {
					if (columnData.possibleValues
						&& cellDataValue !== 'missing') {

						var categoricalValue = columnData.possibleValues.find(function (possibleValue) {
							return possibleValue.name === cellDataValue;
						});
						if (categoricalValue !== undefined) {
							return categoricalValue.id;
						}

					}
					return null;
				}

				function confirmOutOfBoundData(cellDataValue, columnData) {
					var deferred = $q.defer();

					if ($scope.isPendingView) {
						deferred.resolve(true);
						return deferred.promise;
					}

					var invalid = validateDataOutOfRange(cellDataValue, columnData);

					if (invalid) {
						var confirmModal = $scope.openConfirmModal(observationOutOfRange, keepLabel, discardLabel);
						confirmModal.result.then(deferred.resolve);
					} else {
						deferred.resolve(true);
					}

					return deferred.promise;
				}

				function adjustColumns() {
					if ($scope.hasInstances) {
						$timeout(function () {
							table().columns.adjust();
						});
					}
				}

				$scope.reloadEntryDetails = function () {
					$http.get('/Fieldbook/TrialManager/createTrial/useExistingStudy?studyId=' + studyContext.studyId).success(function (data) {
						TrialManagerDataService.updateSettings('entryDetails', TrialManagerDataService.extractSettings(
							data.entryDetailsData));
					});

				}

				function getPageQueryParameters(data) {
					var order = data.order && data.order[0];
					var pageQuery = '?size=' + data.length
						+ '&page=' + ((data.length === 0) ? 0 : data.start / data.length);
					// FIXME: Until now the sort works with entryNumber when will implements by specific column we need replace the code by the commented.
					/*if ($scope.columnsData[order.column]) {
						pageQuery += '&sort=' + $scope.columnsData[order.column].termId + ',' + order.dir;
					}*/
					pageQuery += '&sort=8230' + ',' + order.dir;

					return pageQuery;
				}

				function setNumberOfEntries(numberOfEntries) {
					$scope.numberOfEntries = numberOfEntries;
					TrialManagerDataService.specialSettings.experimentalDesign.
						germplasmTotalListCount = $scope.numberOfEntries;
					$rootScope.$apply();
				}

				function addCommonOptions(options) {
					return options
						.withOption('processing', true)
						.withOption('lengthMenu', [[50, 75, 100], [50, 75, 100]])
						.withOption('scrollY', '500px')
						.withOption('scrollCollapse', true)
						.withOption('scrollX', '100%')
						.withOption('deferRender', true)
						.withOption('language', {
							processing: '<span class="throbber throbber-2x"></span>',
							lengthMenu: 'Records per page: _MENU_',
							paginate: {
								next: '>',
								previous: '<',
								first: '<<',
								last: '>>'
							}
						}).withDOM('<"row"<"col-sm-6"l>>' +
							'<"row"<"col-sm-12"tr>>' +
							'<"row"<"col-sm-5"i><"col-sm-7">>' +
							'<"row"<"col-sm-12"p>>')
						.withPaginationType('full_numbers');
				}

				function loadTable() {
					$scope.nested = {};
					$scope.nested.dtInstance = null;
					var dtColumnsPromise = $q.defer();
					var dtColumnDefsPromise = $q.defer();
					$scope.dtColumns = dtColumnsPromise.promise;
					$scope.dtColumnDefs = dtColumnDefsPromise.promise;
					$scope.dtOptions = null;

					return loadTableHeader().then(function (columnsObj) {
						$scope.selectedItems = [];
						$scope.dtOptions = getDtOptions();
						dtColumnsPromise.resolve(columnsObj.columns);
						dtColumnDefsPromise.resolve(columnsObj.columnsDef);
						initResolve();
					});
				}

				function loadTableHeader() {
					return studyEntryService.getEntryTableColumns().then(function (columnsData) {
						$scope.columnsData = addCheckBoxColumn(columnsData);

						var columnsObj = $scope.columnsObj = mapColumns($scope.columnsData);
						return columnsObj;
					});
				}

				function addCheckBoxColumn(columnsData) {
					// copy array to avoid modifying the parameter (unit test might reuse the same object)
					var columns = columnsData.slice();
					columns.unshift({
						alias: "",
						factor: true,
						name: "CHECK",
						termId: -6
					});
					return columns;
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
						columnData.index = index;

						function isObservationEditable() {
							return columnData.termId !== 8230 && !studyStateService.hasGeneratedDesign();
						}

						function getClassName() {
							var className = '';

							if (columnData.variableType === 'ENTRY_DETAIL') {
								className += 'entry-details ';

								if (isObservationEditable()) {
									className += ' editable ';
								}
							}

							// include data type for determining when to show context menu option/s
							className += 'datatype-' + columnData.dataTypeId;
							className += ' termId-' + columnData.termId;
							// avoid wrapping filter icon
							className += ' dt-head-nowrap';

							return className;
						}

						columns.push({
							title: columnData.alias,
							name: columnData.alias,
							data: function (row) {
								return row.properties[columnData.termId];
							},
							defaultContent: '',
							className: getClassName(),
							columnData: columnData
						});

						// CheckBox Column
						if (columnData.index === 0) {
							columnsDef.push({
								targets: columns.length - 1,
								orderable: false,
								createdCell: function (td, cellData, rowData, rowIndex, colIndex) {
									$(td).append($compile('<span><input type="checkbox" ng-click="toggleSelect(' + rowData.entryId + ')"></span>')($scope));
								}
							});
						} else if (columnData.termId === 8240 || columnData.termId === 8250) {
							// GID or DESIGNATION
							columnsDef.push({
								targets: columns.length - 1,
								orderable: false,
								createdCell: function (td, cellData, rowData, rowIndex, colIndex) {
									$(td).val("");
									var value = columnData.termId === 8240 ? rowData.properties['8240'].value : rowData.properties['8250'].value;
									$(td).append($compile('<a class="gid-link" href="javascript: void(0)" ' +
										'ng-click="openGermplasmDetailsModal(\'' + rowData.gid + '\')">' + value + '</a>')($scope));
								},
								render: function (data, type, full, meta) {
									return '';
								}
							});
						} else if (columnData.termId === 8377) {
							// CROSS
							columnsDef.push({
								targets: columns.length - 1,
								orderable: false,
								render: function (data, type, full, meta) {
									return full.cross;
								}
							});
						} else if (columnData.termId === 8330) {
							// GROUPGID
							columnsDef.push({
								targets: columns.length - 1,
								orderable: false,
								render: function (data, type, full, meta) {
									return full.groupGid ? full.groupGid : '-';
								}
							});
						} if (columnData.termId === -3) {
							//ACTIVE LOT
							columnsDef.push({
								targets: columns.length - 1,
								orderable: false,
								createdCell: function (td, cellData, rowData, rowIndex, colIndex) {
									if (rowData.lotCount === 0) {
										$(td).append('<span>-</span>');
									} else {
										$(td).html($compile('<a href ' +
											` ng-click="openInventoryDetailsModal('${rowData.gid}')"> ` +
											rowData.lotCount + '</a>')($rootScope));
										$rootScope.$apply();
									}
								}
							});

						} else if (columnData.termId === -4) {
							// AVAILABLE
							columnsDef.push({
								targets: columns.length - 1,
								orderable: false,
								render: function (data, type, full, meta) {
									return full.availableBalance;
								}
							});

						} else if (columnData.termId === -5) {
							// UNIT
							columnsDef.push({
								targets: columns.length - 1,
								orderable: false,
								render: function (data, type, full, meta) {
									return full.unit;
								}
							});
						} else if (columnData.variableType === 'ENTRY_DETAIL' && columnData.termId !== 8230) {
							columnsDef.push({
								targets: columns.length - 1,
								orderable: false,
								createdCell: function (td, cellData, rowData, rowIndex, colIndex) {
									processCell(td, cellData, rowData, columnData);
								},
								render: function (data, type, full, meta) {

									if (!data) {
										return '';
									}

									function renderByDataType(value, columnData) {
										if (columnData.dataTypeId === 1130) {
											return renderCategoricalValue(value, columnData);
										} else if (columnData.dataTypeId === 1110) {
											return getDisplayValueForNumericalValue(value);
										} else {
											return EscapeHTML.escape(value);
										}
									}

									var value = renderByDataType(data.value, columnData);
									if ($scope.isPendingView && data.draftValue !== null && data.draftValue !== undefined) {
										var existingValue = value;
										value = renderByDataType(data.draftValue, columnData);
										if (existingValue || existingValue === 0) {
											value += " (" + existingValue + ")";
										}
									}
									if ($scope.isFileStorageConfigured
										&& full
										&& full.fileVariableIds
										&& full.fileVariableIds.length
										&& full.fileVariableIds.includes(columnData.termId.toString())) {

										if (value === undefined) {
											value = '';
										}
										value +=  '<i onclick="showFiles(\'' + full.variables['OBS_UNIT_ID'].value + '\''
											+ ', \'' + columnData.name + '\')" '
											+ ' class="glyphicon glyphicon-duplicate text-info" '
											+ ' title="click to see associated files"'
											+ ' style="font-size: 1.2em; margin-left: 10px; cursor: pointer"></i>';
									}

									return value;
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

									return EscapeHTML.escape(data.value);
								}
							});
						}
					});

					return {
						columns: columns,
						columnsDef: columnsDef
					};
				}

				function processCell(td, cellData, rowData, columnData) {
					var $td = $(td);
					$td.removeClass('accepted-value');
					$td.removeClass('invalid-value');
					$td.removeClass('manually-edited-value');

					if ($scope.isPendingView) {
						if (cellData.draftValue === null || cellData.draftValue === undefined) {
							$td.text('');
							$td.attr('disabled', true);
							return;
						}
						var invalid = validateDataOutOfRange(cellData.draftValue, columnData);

						if (invalid) {
							$td.addClass('invalid-value');
						}

						return;
					}

					if (cellData.value || cellData.value === 0) {
						var invalid = validateDataOutOfRange(cellData.value, columnData);

						if (invalid) {
							$td.addClass('accepted-value');
						}
					}
					if (cellData.status) {
						var status = cellData.status;
						if (!cellData.studyEntryPropertyId) {
							return;
						}
						$td.removeAttr('title');
						$td.removeClass('manually-edited-value');
						$td.removeClass('out-of-sync-value');
						var toolTip = 'GID: ' + rowData.variables.GID.value + ' Designation: ' + rowData.variables.DESIGNATION.value;
						if (status === 'MANUALLY_EDITED') {
							$td.attr('title', toolTip + ' manually-edited-value');
							$td.addClass('manually-edited-value');
						} else if (status === 'OUT_OF_SYNC') {
							$td.attr('title', toolTip + ' out-of-sync-value');
							$td.addClass('out-of-sync-value');
						}
					}
				}

				function validateNumericRange(minVal, maxVal, value, invalid) {
					if (parseFloat(value) < parseFloat(minVal) || parseFloat(value) > parseFloat(maxVal)) {

						invalid = true;
					}
					return invalid;
				}

				function validateCategoricalValues(columnData, cellDataValue, invalid) {
					if (columnData.possibleValues
						&& columnData.possibleValues.find(function (possibleValue) {
							return possibleValue.name === cellDataValue;
						}) === undefined
						&& cellDataValue !== 'missing') {

						invalid = true;
					}
					return invalid;
				}

				function validateDataOutOfRange(cellDataValue, columnData) {
					var invalid = false;

					var value = cellDataValue;
					var minVal = columnData.minRange;
					var maxVal = columnData.maxRange;

					invalid = validateNumericRange(minVal, maxVal, value, invalid);
					invalid = validateCategoricalValues(columnData, cellDataValue, invalid);
					return invalid;
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
						&& possibleValue.displayDescription) {

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

				function getDisplayValueForNumericalValue(numericValue) {
					if (numericValue === "missing" || numericValue === "") {
						return numericValue;
					} else {
						return EscapeHTML.escape(numericValue ? Number(Math.round(numericValue + 'e4') + 'e-4') : '');
					}
				}

				function getCategoricalValue(value, columnData) {
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

					if (possibleValue && possibleValue.displayDescription) {
						return possibleValue.description;
					}
					return value;
				}

				$scope.showImportListBrowser = !TrialManagerDataService.applicationData.germplasmListSelected;

				$scope.showStudyEntriesTable = TrialManagerDataService.applicationData.germplasmListSelected;

				$scope.showClearList = TrialManagerDataService.applicationData.germplasmListSelected && !studyStateService.hasGeneratedDesign();

				$scope.showUpdateImportListButton = TrialManagerDataService.applicationData.germplasmListSelected && !studyStateService.hasGeneratedDesign() && !$scope.showImportListBrowser;

				$scope.labels = {};
				$scope.labels.germplasmFactors = {
					label: 'Temp label here',
					placeholderLabel: 'Temp placeholder here'
				};

				$scope.updateOccurred = false;

				$scope.$on('deleteOccurred', function () {
					$scope.updateOccurred = true;
				});

				$scope.$on('variableAdded', function () {
					$scope.updateOccurred = true;
				});

				$scope.handleSaveEvent = function () {
					$scope.updateOccurred = false;
					TrialManagerDataService.specialSettings.experimentalDesign.germplasmTotalListCount = $scope.numberOfEntries;
				};

				// function called whenever the user has successfully selected a germplasm list
				$scope.germplasmListSelected = function () {
					// validation requiring user to re-generate experimental design after selecting new germplasm list is removed as per new maintain germplasm list functionality
					$scope.updateOccurred = false;
				};

				$scope.germplasmListCleared = function () {
					TrialManagerDataService.applicationData.germplasmListCleared = true;
					TrialManagerDataService.applicationData.germplasmListSelected = false;
				};

				$(document).on('germplasmListUpdated', function () {
					TrialManagerDataService.applicationData.germplasmListSelected = true;
				});

				$scope.openGermplasmTree = function () {
					openListTree(1, $scope.germplasmListSelected);
				};

				$scope.updateModifyList = function () {
					$scope.showImportListBrowser = true;
					$scope.showUpdateImportListButton = false;
					showGermplasmDetailsSection();
				};

				$scope.showUpdateImportList = function () {
					return $scope.showUpdateImportListButton;
				};

				$scope.showClearListButton = function() {
					return $scope.showClearList;
				}

				$scope.showImportList = function () {
					return $scope.showImportListBrowser;
				};

				$scope.showStudyTable = function() {
					return $scope.showStudyEntriesTable;
				}

				TrialManagerDataService.registerSaveListener('germplasmUpdate', $scope.handleSaveEvent);

				$scope.hasGeneratedDesign = function () {
					return studyStateService.hasGeneratedDesign();
				};

				$scope.disableAddButton = function () {
					return studyStateService.hasGeneratedDesign();
				};

				$scope.validateGermplasmForReplacement = function () {
					germplasmStudySourceService.searchGermplasmStudySources({}, 0, 1).then((germplasmStudySourceTable) => {

						// Check if study has advance or cross list
						if (germplasmStudySourceTable.length > 0) {
							showAlertMessage('', $.germplasmMessages.studyHasCrossesOrSelections);
						} else {
							if ($scope.selectedItems.length === 0) {
								showAlertMessage('', $.germplasmMessages.selectEntryForReplacement);
							} else if ($scope.selectedItems.length !== 1) {
								showAlertMessage('', $.germplasmMessages.selectOnlyOneEntryForReplacement);
							} else {
								$scope.replaceGermplasm($scope.selectedItems[0]);
							}
						}
					});
				};

				$scope.toggleSelect = function (data) {
					var idx = $scope.selectedItems.indexOf(data);
					if (idx > -1) {
						$scope.selectedItems.splice(idx, 1)
					} else {
						$scope.selectedItems.push(data);
					}
				};

				$scope.openReplaceGermplasmModal = function(entryId) {
					$rootScope.openGermplasmSelectorModal(false).then((gids) => {
						if (gids != null) {
							// if there are multiple entries selected, get only the first entry for replacement
							studyEntryService.replaceStudyGermplasm(entryId, gids[0]).then(function (response) {
								showSuccessfulMessage('', $.germplasmMessages.replaceGermplasmSuccessful);
								$rootScope.$emit("reloadStudyEntryTableData", {});
							}, function(errResponse) {
								showErrorMessage($.fieldbookMessages.errorServerError,  errResponse.errors[0].message);
							});
						}
					});
				};


				$scope.replaceGermplasm = function(entryId) {
					if (studyStateService.hasGeneratedDesign()) {
						var modalConfirmReplacement = $scope.openConfirmModal($.germplasmMessages.replaceGermplasmWarning, 'Yes','No');
						modalConfirmReplacement.result.then(function (shouldContinue) {
							if (shouldContinue) {
								$scope.openReplaceGermplasmModal(entryId);
							}
						});
					} else {
						$scope.openReplaceGermplasmModal(entryId);
					}

				};

				$scope.saveStudyEntries = function (listId) {

					studyEntryService.deleteEntries().then(function () {
						studyEntryService.saveStudyEntriesList(listId).then(function(res){
							TrialManagerDataService.applicationData.germplasmListSelected = true;
							$scope.reloadStudyEntryTableData();
							$scope.showImportListBrowser = false;
							$scope.showUpdateImportListButton = true;
							$scope.showStudyEntriesTable = true;
							$scope.showClearList = true;
						});
					});
				};

				$scope.resetStudyEntries = function() {
					var modalConfirmCancellation = $scope.openConfirmModal($.fieldbookMessages.confirmResetStudyEntries, 'Confirm', 'Cancel');
					modalConfirmCancellation.result.then(function (shouldContinue) {
						if (shouldContinue) {
							studyEntryService.deleteEntries().then(function () {
								$scope.numberOfEntries = 0;
								$scope.showImportListBrowser = true;
								$scope.showUpdateImportListButton = false;
								$scope.showStudyEntriesTable = false;
								$scope.showClearList = false;
								$scope.reloadStudyEntryTableData();
								TrialManagerDataService.applicationData.germplasmListSelected = false;
							});
						}
					});
				}

				$scope.onRemoveVariable = async function (variableType, variableIds) {
					if(variableIds && variableIds.length !== 0) {

						if (variableType === VARIABLE_TYPES.ENTRY_DETAIL.toString()) {
							let doContinue = await $scope.checkVariableHasMeasurementData(variableIds);
							if (!doContinue) {
								return;
							}
						}

						var deferred = $q.defer();
						datasetService.getDatasets([4]).then(function (data) {
							angular.forEach(data, function (dataset) {
								datasetService.removeVariables(dataset.datasetId, variableIds).then(function () {
									deferred.resolve(true);
									showSuccessfulMessage('', $.germplasmMessages.removeVariableSuccess);
									$rootScope.navigateToTab('germplasm', {reload: true});
								}, function (response) {
									if (response.errors && response.errors.length) {
										showErrorMessage('', response.errors[0].message);
									} else {
										showErrorMessage('', ajaxGenericErrorMsg);
									}
								});
							});
						});
						return deferred.promise;
					} else {
						showAlertMessage('', $.germplasmMessages.removeVariableWarning);
					}
				};

				$scope.checkVariableHasMeasurementData = function (deleteVariables) {
					var deferred = $q.defer();
					if (deleteVariables.length != 0) {
						studyEntryObservationService.countObservationsByVariables(deleteVariables).then(function (response) {
							var count = response.headers('X-Total-Count');
							if (count > 0) {
								// TODO: confirm message text
								var message = observationVariableDeleteConfirmationText;
								var modalInstance = $scope.openConfirmModal(message, environmentConfirmLabel);
								modalInstance.result.then(deferred.resolve);
							} else {
								deferred.resolve(true);
							}
						});
					}
					return deferred.promise;
				};

				$scope.onAddVariable = function(result, variableType) {
					var variable = undefined;
					angular.forEach(result, function (val) {
						variable = val.variable;
					});
					datasetService.getDatasets([DATASET_TYPES.PLOT_OBSERVATIONS]).then(function (data) {
						angular.forEach(data, function (dataset) {
							var variableName = variable.alias ? variable.alias : variable.name;
							datasetService.addVariables(dataset.datasetId, {
								variableTypeId: variableType,
								variableId: variable.cvTermId,
								studyAlias: variableName
							}).then(function () {
								showSuccessfulMessage('', $.germplasmMessages.addVariableSuccess.replace("{0}", variableName));
							})
						});
					});
				};

				$scope.onHideCallback = function () {
					$rootScope.navigateToTab('germplasm', {reload: true});
				};

				$scope.showPopOverCheck = function(entryIds, currentValue) {
					if(!studyStateService.hasGeneratedDesign()) {
						if(!Array.isArray(entryIds)) {
							entryIds = [parseInt(entryIds)];
						}
						$uibModal.open({
							templateUrl: '/Fieldbook/static/angular-templates/germplasm/changeStudyEntryTypeModal.html',
							controller: "changeStudyEntryTypeCtrl",
							size: 'md',
							resolve: {
								entryIds: function () {
									return entryIds;
								},
								currentValue: function () {
									return currentValue;
								}
							},
							controllerAs: 'ctrl'
						});
					}
				};

				$scope.setEntryTypeByBatch = function() {
					if ($scope.selectedItems.length === 0) {
						showAlertMessage('', $.germplasmMessages.setEntryTypeSelectEntry);
					} else {
						$scope.showPopOverCheck($scope.selectedItems, null);
					}
				};

				$scope.showAddEntriesModal = function() {
					$uibModal.open({
						templateUrl: '/Fieldbook/static/angular-templates/germplasm/addNewEntriesModal.html',
						controller: "AddNewEntriesController",
						size: 'md'
					});

				};

				$scope.reloadStudyEntryTableData = function(setShowValues) {
					$scope.selectedItems = [];
					$scope.reloadEntryDetails();
					$rootScope.navigateToTab('germplasm', {reload: true});
					if(setShowValues) {
						$scope.showImportListBrowser = false;
						$scope.showStudyEntriesTable = true;
						$scope.showClearList = !studyStateService.hasGeneratedDesign();
						$scope.showUpdateImportListButton = !studyStateService.hasGeneratedDesign();
					}
				};

				$scope.openGermplasmDetailsModal = function (gid) {
					germplasmDetailsModalService.openGermplasmDetailsModal(gid, null);
				};

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

				$scope.fillWithCrossExpansion = function () {
					studyEntryService.fillWithCrossExpansion($scope.generationLevel).then(function (response) {
						TrialManagerDataService.updateGenerationLevel($scope.generationLevel);
						$scope.reloadStudyEntryTableData();
					}, function(errResponse) {
						$uibModalInstance.close();
						showErrorMessage($.fieldbookMessages.errorServerError,  errResponse.errors[0].message);
					});
				};

				$scope.changeGenerationLevel = function(level) {
					$scope.generationLevel = level;
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
				};

				$scope.selectEntryTableColumns = function() {
					var selectedPropertyIds = this.entryTableColumns.filter((column) => column.selected).map((column) => column.id);
					datasetService.updateDatasetProperties(selectedPropertyIds).then(function () {
						$rootScope.navigateToTab('germplasm', {reload: true});
					});
				}

			}]);

})();

// README IMPORTANT: Code unmanaged by angular should go here

/* This will be called when germplasm details page is loaded */
(function() {
	'use strict';

	document.onLoadGermplasmDetails = function() {

		displayGermplasmListTreeTable('germplasmTree');

		changeBrowseGermplasmButtonBehavior(false);

		$('#listTreeModal').off('hide.bs.modal');
		$('#listTreeModal').on('hide.bs.modal', function() {
			TreePersist.saveGermplasmTreeState(true, '#germplasmTree');
			displayGermplasmListTreeTable('germplasmTree');
			changeBrowseGermplasmButtonBehavior(false);
			$(getDisplayedModalSelector() + ' #addGermplasmFolderDiv').hide();
			$(getDisplayedModalSelector() + ' #renameGermplasmFolderDiv').hide();
		});
	};

})();
