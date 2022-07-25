(function () {
	'use strict';

	var TRIAL_INSTANCE = 8170,
		GID = 8240;
	var hiddenColumns = [TRIAL_INSTANCE];

	var analysisResultsModule = angular.module('analysis-results', []);

	analysisResultsModule.controller('AnalysisResultsCtrl',
		['$scope',
			'DTOptionsBuilder', '$q', '$compile', 'datasetService', '$timeout', 'studyContext', 'germplasmDetailsModalService',
			function ($scope, DTOptionsBuilder, $q, $compile, datasetService, $timeout, studyContext, germplasmDetailsModalService) {

				// used also in tests - to call $rootScope.$apply()
				var tableLoadedResolve;
				$scope.tableLoadedPromise = new Promise(function (resolve) {
					tableLoadedResolve = resolve;
				});

				var tableRenderedResolve;
				$scope.tableRenderedPromise = new Promise(function (resolve) {
					tableRenderedResolve = resolve;
				});

				$scope.nested = {};
				$scope.dataset = {};
				$scope.columnsData = {};
				$scope.nested.dtInstance = null;

				var dtColumnsPromise = $q.defer();
				var dtColumnDefsPromise = $q.defer();

				$scope.dtColumns = dtColumnsPromise.promise;
				$scope.dtColumnDefs = dtColumnDefsPromise.promise;
				$scope.dtOptions = null;

				$scope.totalItems = 0;
				$scope.selectedItems = [];
				$scope.isAllPagesSelected = false;
				$scope.activeTab = 1;
				$scope.datasetId = studyContext.meansDatasetId;

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

				$scope.changeEnvironment = function () {
					table().columns('.termId-'+TRIAL_INSTANCE).visible($scope.nested.selectedEnvironment === $scope.environments[0]);
					table().ajax.reload();
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

				$scope.showSSATab = function (tab) {
					$scope.activeTab = tab;

					$scope.datasetId = $scope.activeTab === 1 ? studyContext.summaryStatisticsDatasetId: studyContext.meansDatasetId;

					datasetService.getDataset($scope.datasetId).then(function (dataset) {
						$scope.dataset = dataset;
						$scope.environments = [{
							instanceNumber: null,
							locationName: 'All environments'
						}].concat(dataset.instances);

						$scope.nested.selectedEnvironment = $scope.activeTab === 1 ? $scope.environments[0] : $scope.environments[1];

						loadTable();
					});
				};

				$scope.showSSATab(1);


				function table() {
					return $scope.nested.dtInstance.DataTable;
				}

				function getFilter() {
					return {
						filterColumns: [],
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

					// GID should be treated as numeric
					if (columnData.termId === GID) {
						return false;
					}

					if (columnData.dataType === 'Categorical' || columnData.dataType === 'Numeric'
						|| columnData.dataType === 'Date') {
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
									url: datasetService.getObservationTableUrl($scope.datasetId) + getPageQueryParameters(d),
									data: JSON.stringify({
										draw: d.draw,
										instanceId: $scope.nested.selectedEnvironment.instanceId,
										draftMode: $scope.isPendingView,
										filter: getFilter()
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
						.withOption('drawCallback', drawCallback));
				}

				function getPageQueryParameters(data) {
					var order = data.order && data.order[0];
					var pageQuery = '?size=' + data.length
						+ '&page=' + ((data.length === 0) ? 0 : data.start / data.length);
					if ($scope.columnsData[order.column]) {
						pageQuery += '&sort=' + $scope.columnsData[order.column].termId + ',' + order.dir;
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
							text: '<i class="glyphicon glyphicon-th"></i>',
							columns: ':gt(0)'
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
				}

				function drawCallback() {
					adjustColumns();
				}

				function adjustColumns() {
					$timeout(function () {
						table().columns.adjust();
					});
				}

				function loadTable() {
					var dtColumnsPromise = $q.defer();
					var dtColumnDefsPromise = $q.defer();
					$scope.dtColumns = dtColumnsPromise.promise;
					$scope.dtColumnDefs = dtColumnDefsPromise.promise;
					$scope.dtOptions = null;

					return loadColumns().then(function (columnsObj) {
						$scope.dtOptions = getDtOptions();
						angular.forEach(columnsObj.columns, function (column, index) {
							// "ENTRY_NO"
							if (column.columnData.termId === 8230) {
								$scope.dtOptions.withOption('order', [index, 'asc']);
							}
						});

						dtColumnsPromise.resolve(columnsObj.columns);
						dtColumnDefsPromise.resolve(columnsObj.columnsDef);

						tableLoadedResolve();
					});
				}

				function loadColumns() {
					return datasetService.getColumns($scope.datasetId, false).then(function (columnsData) {
						$scope.columnsData = columnsData;
						var columnsObj = $scope.columnsObj = mapColumns($scope.columnsData);
						return columnsObj;
					});
				}
				
				function mapColumns(columnsData) {
					var columns = [],
						columnsDef = [];

					angular.forEach(columnsData, function (columnData, index) {
						if (columnData) {
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

							function getClassName() {
								var className = columnData.factor === true ? 'factors' : 'variates';
								// include data type for determining when to show context menu option/s
								className += ' datatype-' + columnData.dataTypeId;
								className += ' termId-' + columnData.termId;
								// avoid wrapping filter icon
								className += ' dt-head-nowrap';
								return className;
							}

							function isColumnVisible() {
								if (columnData.termId === TRIAL_INSTANCE) {
									return $scope.nested.selectedEnvironment === $scope.environments[0];
								}
								return hiddenColumns.indexOf(columnData.termId) < 0;
							}

							columns.push({
								title: columnData.alias,
								name: columnData.alias,
								data: function (row) {
									return row.variables[columnData.name];
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
											full.gid + '\')">' + EscapeHTML.escape(data.value) + '</a>';
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
						}
					});

					return {
						columns: columns,
						columnsDef: columnsDef
					};
				}

				$scope.openGermplasmDetailsModal = function (gid) {
					germplasmDetailsModalService.openGermplasmDetailsModal(gid, null);
				}

				function renderCategoricalValue(value, columnData) {
					var possibleValue = null;

					if (columnData.possibleValues) {
						possibleValue = columnData.possibleValuesByName[value] || columnData.possibleValuesById[value];
					}

					if (possibleValue && possibleValue.description) {
						value = '<span class="fbk-measurement-categorical-desc"'
							+ ' >'
							+ EscapeHTML.escape(possibleValue.description) + '</span>';
					}
					return value;
				}

			}]);
})();
