/*global angular, openListTree, displaySelectedGermplasmDetails*/

(function () {
	'use strict';

	var manageTrialAppModule = angular.module('manageTrialApp');

	manageTrialAppModule.controller('GermplasmCtrl',
		['$scope', '$rootScope', '$q', '$compile', 'TrialManagerDataService', 'DTOptionsBuilder', 'studyStateService', 'studyEntryService', 'germplasmStudySourceService',
			'datasetService', '$timeout', '$uibModal', 'germplasmDetailsModalService', 'studyEntryObservationService', 'feedbackService', 'DATASET_TYPES', 'VARIABLE_TYPES',
			'$http', 'studyContext', 'breedingMethodModalService', 'HasAnyAuthorityService', 'PERMISSIONS',
			function ($scope, $rootScope, $q, $compile, TrialManagerDataService, DTOptionsBuilder, studyStateService, studyEntryService, germplasmStudySourceService,
					  datasetService, $timeout, $uibModal, germplasmDetailsModalService, studyEntryObservationService, feedbackService, DATASET_TYPES, VARIABLE_TYPES,
					  $http, studyContext, breedingMethodModalService, HasAnyAuthorityService, PERMISSIONS) {

				var GID = 8240,
					GROUPGID = 8330;
				$scope.hasManageStudiesPermission = HasAnyAuthorityService.hasAnyAuthority(PERMISSIONS.MANAGE_STUDIES_PERMISSIONS);

				$scope.entryDetails = TrialManagerDataService.settings.entryDetails;
				$scope.isLockedStudy = TrialManagerDataService.isLockedStudy;
				$scope.trialMeasurement = {hasMeasurement: studyStateService.hasGeneratedDesign()};
				$scope.addVariable = TrialManagerDataService.applicationData.germplasmListSelected && $scope.hasManageStudiesPermission;
				$scope.showAddColumns = TrialManagerDataService.applicationData.germplasmListSelected;
				$scope.selectedItems = [];
				$scope.numberOfEntries = 0;

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

				$scope.datepickerOptions = {
					showWeeks: false
				};

				$scope.generationLevel = getGenerationLevel();
				$scope.generationLevels = Array.from(Array(10).keys()).map((k) => k + 1);

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

				$scope.openColumnFilter = function (index) {
					$scope.columnFilter.index = index;
					$scope.columnFilter.columnData = $scope.columnsObj.columns[index].columnData;
					if ($scope.columnFilter.columnData.sortingAsc != null && !table().order().some(function (order) {
						return order[0] === index;
					})) {
						$scope.columnFilter.columnData.sortingAsc = null;
					}
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

				$scope.isCheckBoxColumn = function (index) {
					return index === 0;
				};

				$scope.filterByColumn = function () {
					table().ajax.reload();
				};

				$scope.resetFilterByColumn = function () {
					$scope.columnFilter.columnData.query = '';
					$scope.columnFilter.columnData.sortingAsc = null;
					if ($scope.columnFilter.columnData.possibleValues) {
						$scope.columnFilter.columnData.possibleValues.forEach(function (value) {
							value.isSelectedInFilters = false;
						});
						$scope.columnFilter.columnData.isSelectAll = false;
					}
					table().ajax.reload();
				};

				loadTable();
				openFeedbackSurvey($scope.FEEDBACK_ENABLED, 'GERMPLASM_AND_CHECKS', feedbackService);

				$rootScope.$on("reloadStudyEntryTableData", function (setShowValues) {
					$scope.reloadStudyEntryTableData(setShowValues);
				});

				function getGenerationLevel() {
					studyEntryService.getCrossLevelGeneration().then(function (level) {
						$scope.generationLevel = level ? level : 1;
					});
				}

				function getFilter() {
					return {
						entryNumbers: [],
						entryIds: [],
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
								if (columnData.dataType === 'Date') {
									map[columnData.termId] = [($.datepicker.formatDate("yymmdd", columnData.query))];
								} else {
									map[columnData.termId] = [(columnData.query)];
								}
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
								map[columnData.termId] = columnData.query;
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

					// Factors like GID, GROUPGID have 'Germplasm List' datatype but they should be treated as numeric
					if (columnData.termId === GID || columnData.termId === GROUPGID) {
						return false;
					}

					if (columnData.dataType === 'Categorical' || columnData.dataType === 'Numeric'
						|| columnData.dataType === 'Date') {
						return false;
					}

					return true;

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
									data: JSON.stringify({
										filter: getFilter()
									}),
									success: function (res, status, xhr) {
										let json = {recordsTotal: 0, recordsFiltered: 0};
										json.recordsTotal = xhr.getResponseHeader('X-Total-Count');
										json.recordsFiltered = xhr.getResponseHeader('X-Filtered-Count');
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

						// Skip if column is checkbox column.
						if ($scope.isCheckBoxColumn(this.index())) {
							return;
						}

						// Add Crossing Options popover
						$(this.header())
							.append($compile('<span class="glyphicon glyphicon-edit" ' +
								' style="cursor:pointer; padding-left: 5px;"' +
								' popover-placement="bottom"' +
								' popover-append-to-body="true"' +
								' popover-trigger="\'outsideClick\'"' +
								// does not work with outsideClick
								' ng-if="checkColumnByTermId(' + this.index() + ', 8377)"' +
								' ng-show="!isLockedStudy()"' +
								' uib-popover-template="\'crossOptionsPopover.html\'"></span>')($scope));
						// Add Sorting and Filter popover
						$(this.header())
							.append($compile('<span class="glyphicon glyphicon-filter" ' +
								' style="cursor:pointer; padding-left: 5px;"' +
								' popover-placement="bottom"' +
								' ng-class="getFilteringByClass(' + this.index() + ')"' +
								' popover-append-to-body="true"' +
								' popover-trigger="\'outsideClick\'"' +
								// does not work with outsideClick
								' ng-click="openColumnFilter(' + this.index() + ')"' +
								' uib-popover-template="\'columnFilterPopoverTemplate.html\'"></span>')($scope))
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
										return $q.resolve(cellData ? cellData.studyEntryPropertyId : null);
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
												return cellData.studyEntryPropertyId;
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
												return cellData.studyEntryPropertyId;
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

								promise.then(function (studyEntryPropertyId) {
									var valueChanged = false;
									if (cellData.value !== $inlineScope.observation.value) {
										valueChanged = true;
									}

									if ($scope.isPendingView) {
										cellData.draftValue = $inlineScope.observation.value;
									} else {
										cellData.value = $inlineScope.observation.value;
									}

									cellData.studyEntryPropertyId = Number.isFinite(studyEntryPropertyId) ? studyEntryPropertyId : undefined;

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
					$timeout(function () {
						table().columns.adjust();
					}, 100);
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
					var columnData = $scope.columnsData[order.column];
					if (columnData) {
						var sort = columnData.termId;
						if (columnData.variableType === 'GERMPLASM_PASSPORT' ||
							columnData.variableType === 'GERMPLASM_ATTRIBUTE') {
							sort = 'VARIABLE_' + sort;
						}
						pageQuery += '&sort=' + sort + ',' + order.dir;
					}
					return pageQuery;
				}

				function setNumberOfEntries(numberOfEntries) {
					$scope.numberOfEntries = numberOfEntries;
					TrialManagerDataService.specialSettings.experimentalDesign.germplasmTotalListCount = $scope.numberOfEntries;
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
						// Set table default order: ENTRY_NO ascending
						$scope.dtOptions.withOption('order', [1, 'asc']);
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
							if (columnData.termId !== 8255) {
								columnData.possibleValues.unshift({
									name: '', displayValue: 'Please Choose', displayDescription: 'Please Choose'
								});
							}
						}
						columnData.index = index;

						function isObservationEditable() {
							return columnData.termId !== 8230 &&
								(!columnData.systemVariable || (columnData.systemVariable && !studyStateService.hasGeneratedDesign()));
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
						} else if (columnData.termId === 8342 || columnData.termId === 8343) {
							// FEMALE_PARENT_GID or FEMALE_PARENT_NAME
							columnsDef.push({
								targets: columns.length - 1,
								orderable: false,
								createdCell: function (td, cellData, rowData, rowIndex, colIndex) {
									$(td).val("");
									var value;
									if (columnData.termId === 8342 && rowData.properties['8342']) {
										value = rowData.properties['8342'].value;
									} else if (columnData.termId === 8343 && rowData.properties['8343']) {
										value = rowData.properties['8343'].value;
									}
									if (value) {
										if (value !== 'UNKNOWN') {
											$(td).append($compile('<a class="gid-link" href="javascript: void(0)" ' +
												'ng-click="openGermplasmDetailsModal(\'' + rowData.properties['8342'].value + '\')">' + value + '</a>')($scope));
										} else {
											$(td).append(value);
										}
									}
								},
								render: function (data, type, full, meta) {
									return '';
								}
							});
						} else if (columnData.termId === 8345 || columnData.termId === 8346) {
							// MALE_PARENT_GID or MALE_PARENT_NAME
							columnsDef.push({
								targets: columns.length - 1,
								orderable: false,
								createdCell: function (td, cellData, rowData, rowIndex, colIndex) {
									$(td).val("");
									var value;
									if (columnData.termId === 8345 && rowData.properties['8345']) {
										value = rowData.properties['8345'].value;
									} else if (columnData.termId === 8346 && rowData.properties['8346']) {
										value = rowData.properties['8346'].value;
									}
									if (value) {
										if (value !== 'UNKNOWN') {
											$(td).append($compile('<a class="gid-link" href="javascript: void(0)" ' +
												'ng-click="openGermplasmDetailsModal(\'' + rowData.properties['8345'].value + '\')">' + value + '</a>')($scope));
										} else {
											$(td).append(value);
										}
									}
								},
								render: function (data, type, full, meta) {
									return '';
								}
							});
						} else if (columnData.termId === 8254) {
							// BREEDING_METHOD_ABBR
							columnsDef.push({
								targets: columns.length - 1,
								orderable: false,
								createdCell: function (td, cellData, rowData, rowIndex, colIndex) {
									$(td).val("");
									var value = rowData.properties['8254'].value;
									if (value) {
										$(td).append($compile('<a class="gid-link" href="javascript: void(0)" ' +
											'ng-click="openBreedingMethodModal(\'' + value + '\')">' + value + '</a>')($scope));
									}
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
						} else if (columnData.termId === 8235) {
							// GUID
							columnsDef.push({
								targets: columns.length - 1,
								orderable: false,
								render: function (data, type, full, meta) {
									return full.guid;
								}
							});
						}
						if (columnData.termId === -3) {
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
											var escapedValue = EscapeHTML.escape(value);
											return '<span class="text-ellipsis" title="' + escapedValue + '">' + escapedValue + '</span>';
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
										value += '<i onclick="showFiles(\'' + full.variables['OBS_UNIT_ID'].value + '\''
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
						return EscapeHTML.escape(numericValue ? Number(numericValue) : '');
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
					TrialManagerDataService.applicationData.germplasmListSelected = true;
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
					return $scope.hasManageStudiesPermission && $scope.showUpdateImportListButton;
				};

				$scope.showClearListButton = function () {
					return $scope.hasManageStudiesPermission && $scope.showClearList;
				}

				$scope.showImportList = function () {
					return $scope.hasManageStudiesPermission && $scope.showImportListBrowser;
				};

				$scope.showStudyTable = function () {
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

				$scope.openReplaceGermplasmModal = function (entryId) {
					$rootScope.openGermplasmSelectorModal(false).then((gids) => {
						if (gids != null) {
							// if there are multiple entries selected, get only the first entry for replacement
							studyEntryService.replaceStudyGermplasm(entryId, gids[0]).then(function (response) {
								showSuccessfulMessage('', $.germplasmMessages.replaceGermplasmSuccessful);
								$rootScope.$emit("reloadStudyEntryTableData", {});
							}, function (errResponse) {
								showErrorMessage($.fieldbookMessages.errorServerError, errResponse.errors[0].message);
							});
						}
					});
				};


				$scope.replaceGermplasm = function (entryId) {
					if (studyStateService.hasGeneratedDesign()) {
						var modalConfirmReplacement = $scope.openConfirmModal($.germplasmMessages.replaceGermplasmWarning, 'Yes', 'No');
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
						studyEntryService.saveStudyEntriesList(listId).then(function (res) {
							TrialManagerDataService.applicationData.germplasmListSelected = true;
							$scope.reloadStudyEntryTableData();
							$scope.showImportListBrowser = false;
							$scope.showUpdateImportListButton = true;
							$scope.showStudyEntriesTable = true;
							$scope.showClearList = true;
						}, function (errResponse) {
							showErrorMessage($.fieldbookMessages.errorServerError, errResponse.errors[0].message);
						});
					}, function (errResponse) {
						showErrorMessage($.fieldbookMessages.errorServerError, errResponse.errors[0].message);
					});
				};

				$scope.resetStudyEntries = function () {
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
							}, function (errResponse) {
								showErrorMessage($.fieldbookMessages.errorServerError, errResponse.errors[0].message);
							});
						}
					});
				}

				$scope.onRemoveVariable = async function (variableType, variableIds) {
					if (variableIds && variableIds.length !== 0) {

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

				$scope.onAddVariable = function (result, variableType) {
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
							}, function (response) {
								if (response.errors && response.errors.length) {
									showErrorMessage('', response.errors[0].message);
								} else {
									showErrorMessage('', ajaxGenericErrorMsg);
								}
							});
						});
					});
				};

				$scope.onHideCallback = function () {
					$rootScope.navigateToTab('germplasm', {reload: true});
				};

				$scope.showPopOverCheck = function (entryIds, currentValue) {
					if (!studyStateService.hasGeneratedDesign()) {
						if (!Array.isArray(entryIds)) {
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

				$scope.setEntryTypeByBatch = function () {
					if ($scope.selectedItems.length === 0) {
						showAlertMessage('', $.germplasmMessages.setEntryTypeSelectEntry);
					} else {
						$scope.showPopOverCheck($scope.selectedItems, null);
					}
				};

				$scope.showAddEntriesModal = function () {
					$uibModal.open({
						templateUrl: '/Fieldbook/static/angular-templates/germplasm/addNewEntriesModal.html',
						controller: "AddNewEntriesController",
						size: 'md'
					});

				};

				$scope.openImportEntryDetailsModal = function() {
					$uibModal.open({
						templateUrl: '/Fieldbook/static/angular-templates/germplasm/importEntryDetailsModal.html',
						controller: "ImportEntryDetailsController",
						size: 'lg'
					});

				};

				$scope.reloadStudyEntryTableData = function (setShowValues) {
					$scope.selectedItems = [];
					$scope.reloadEntryDetails();
					$rootScope.navigateToTab('germplasm', {reload: true});
					if (setShowValues) {
						$scope.showImportListBrowser = false;
						$scope.showStudyEntriesTable = true;
						$scope.showClearList = !studyStateService.hasGeneratedDesign();
						$scope.showUpdateImportListButton = !studyStateService.hasGeneratedDesign();
					}
				};

				$scope.openGermplasmDetailsModal = function (gid) {
					germplasmDetailsModalService.openGermplasmDetailsModal(gid, null);
				};

				$scope.openBreedingMethodModal = function (methodId) {
					breedingMethodModalService.openBreedingMethodModal(methodId, null);
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
					}, function (errResponse) {
						$uibModalInstance.close();
						showErrorMessage($.fieldbookMessages.errorServerError, errResponse.errors[0].message);
					});
				};

				$scope.changeGenerationLevel = function (level) {
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

				// Entry table columns dropdown
				$scope.germplasmDescriptorColumns = [];
				$scope.passportColumns = [];
				$scope.attributesColumns = [];
				$scope.namesColumns = [];

				// This is being used as a workaround to manually close the 'Columns' dropdown when the 'Actions' dropdown is clicked.
				$scope.entryColumnsPopover = {
					isOpen: false,

					close: function close() {
						$scope.entryColumnsPopover.isOpen = false;
					}
				}

				$scope.entryColumnsButtonClicked = function() {
					if (!$scope.entryColumnsPopover.isOpen) {
						resetColumns();
						loadStudyEntryColumns();
					}
				}

				$scope.selectEntryTableColumns = function () {
					var selectedPropertyIds = concatAllColumns().filter((column) => column.selected && column.typeId !== null).map((column) => column.id);
					var selectedNameTypeIds = concatAllColumns().filter((column) => column.selected && column.typeId === null).map((column) => column.id);
					datasetService.updateDatasetProperties(
						{
							variableIds: selectedPropertyIds,
							nameTypeIds: selectedNameTypeIds
						}
							).then(function () {
						$rootScope.navigateToTab('germplasm', {reload: true});
					}, function (response) {
						if (response.errors && response.errors.length) {
							showErrorMessage('', response.errors[0].message);
						} else {
							showErrorMessage('', ajaxGenericErrorMsg);
						}
					});
				}

				$scope.onSearchColumn = function(evt) {
					const searchString = evt.target.value.toLowerCase();

					if (searchString) {
						filterColumns(concatAllColumns(), searchString);
					} else {
						showAllColumns();
					}
				}

				$scope.checkAreColumnsVisible = function(columns) {
					return columns.filter((column) => column.visible).length > 0;
				}

				function resetColumns() {
					$scope.germplasmDescriptorColumns = [];
					$scope.passportColumns = [];
					$scope.attributesColumns = [];
					$scope.namesColumns = [];
				}

				function loadStudyEntryColumns() {
					studyEntryService.getStudyEntriesColumns().then(function (columns) {
						columns.forEach((column) => {
							column.displayName = column.alias ? `${column.alias} (${column.name}) ` : column.name;

							if (column.typeId === VARIABLE_TYPES.GERMPLASM_DESCRIPTOR) {
								$scope.germplasmDescriptorColumns.push(column);
							} else if (column.typeId === VARIABLE_TYPES.GERMPLASM_PASSPORT) {
								$scope.passportColumns.push(column);
							} else if (column.typeId === VARIABLE_TYPES.GERMPLASM_ATTRIBUTE) {
								$scope.attributesColumns.push(column);
							} else if (column.typeId === null) {
								$scope.namesColumns.push(column);
							}
						});

						showAllColumns();
					});
				}

				function filterColumns(columns, searchString) {
					columns.forEach((column) => {
						if (!column.displayName.toLowerCase().includes(searchString)) {
							column.visible = false;
						}
					});
				}

				function showAllColumns() {
					concatAllColumns().forEach((column) => column.visible = true);
				}

				function concatAllColumns() {
					return [].concat($scope.germplasmDescriptorColumns, $scope.passportColumns, $scope.attributesColumns, $scope.namesColumns);
				}

			}]);

})();

// README IMPORTANT: Code unmanaged by angular should go here

/* This will be called when germplasm details page is loaded */
(function () {
	'use strict';

	document.onLoadGermplasmDetails = function () {

		displayGermplasmListTreeTable('germplasmTree');

		changeBrowseGermplasmButtonBehavior(false);

		$('#listTreeModal').off('hide.bs.modal');
		$('#listTreeModal').on('hide.bs.modal', function () {
			TreePersist.saveGermplasmTreeState(true, '#germplasmTree');
			displayGermplasmListTreeTable('germplasmTree');
			changeBrowseGermplasmButtonBehavior(false);
			$(getDisplayedModalSelector() + ' #addGermplasmFolderDiv').hide();
			$(getDisplayedModalSelector() + ' #renameGermplasmFolderDiv').hide();
		});
	};

})();
