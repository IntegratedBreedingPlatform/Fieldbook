/*global angular, showAlertMessage, showErrorMessage*/
(function () {
	'use strict';

	var derivedVariableModule = angular.module('derived-variable', ['ui.bootstrap', 'datasets-api', 'datasetOptionModal', 'fieldbook-utils']);

	derivedVariableModule.factory('derivedVariableService', ['$http', '$q', 'studyContext', function ($http, $q, studyContext) {

		var derivedVariableService = {};

		var FIELDBOOK_BASE_URL = '/Fieldbook/DerivedVariableController/';
		var BMSAPI_BASE_URL = '/bmsapi/crops/' + studyContext.cropName + '/studies/';

		var successHandler = function (response) {

			if (response.data && response.data.inputMissingData) {
				showAlertMessage('', response.data.inputMissingData, 15000);
			}
			if (response.data && response.data.hasDataOverwrite) {
				return response.data.hasDataOverwrite;
			}
		};
		var failureHandler = function (response) {
			if (response.data.errorMessage) {
				showErrorMessage('', response.data.errorMessage);
			} else {
				showErrorMessage('', ajaxGenericErrorMsg);
			}
		};


		derivedVariableService.getDependencies = function () {
			return $http.get(FIELDBOOK_BASE_URL + 'derived-variable/dependencies');
		};

		derivedVariableService.hasMeasurementData = function (variableIds) {
			return $http.post(FIELDBOOK_BASE_URL + 'derived-variable/dependencyVariableHasMeasurementData/',
				variableIds, {cache: false});
		};

		derivedVariableService.calculateVariableForObservation = function (calculateData) {
			var request = $http.post(FIELDBOOK_BASE_URL + 'derived-variable/execute', calculateData);
			return request.then(successHandler, failureHandler);
		};

		derivedVariableService.calculateVariableForSubObservation = function (datasetId, calculateData) {
			var request = $http.post(BMSAPI_BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/derived-variable/calculate', calculateData);
			return request.then(successHandler, failureHandler);
		};

		return derivedVariableService;

	}]);


	derivedVariableModule.factory('derivedVariableModalService', ['$uibModal', function ($uibModal) {

		var derivedVariableModalService = {};

		derivedVariableModalService.openDatasetOptionModal = function () {
			$uibModal.open({
				template: '<dataset-option-modal modal-title="modalTitle" message="message"' +
					'selected="selected" on-continue="next()"></dataset-option-modal>',
				controller: 'executeCalculatedVariableDatasetOptionCtrl',
				size: 'md'
			});
		};

		derivedVariableModalService.openExecuteCalculatedVariableModal = function (datasetId) {
			$uibModal.open({
				templateUrl: '/Fieldbook/static/angular-templates/derivedVariable/executeCalculatedVariableModal.html',
				controller: "executeCalculatedVariableModalCtrl",
				size: 'md',
				resolve: {
					datasetId: function () {
						return datasetId;
					}
				},
				controllerAs: 'ctrl'
			});
		};

		derivedVariableModalService.confirmOverrideCalculatedVariableModal = function (datasetId, selectedVariable) {
			$uibModal.open({
				templateUrl: '/Fieldbook/static/angular-templates/derivedVariable/confirmOverrideCalculatedVariableModal.html',
				controller: "confirmOverrideCalculatedVariableModalCtrl",
				size: 'md',
				resolve: {
					datasetId: function () {
						return datasetId;
					},
					selectedVariable: function () {
						return selectedVariable;
					}
				},
				controllerAs: 'ctrl'
			});
		};

		return derivedVariableModalService;

	}]);

	derivedVariableModule.controller('executeCalculatedVariableDatasetOptionCtrl', ['$rootScope', '$scope', '$uibModal', '$uibModalInstance', 'studyContext', 'derivedVariableModalService',
		function ($rootScope, $scope, $uibModal, $uibModalInstance, studyContext, derivedVariableModalService) {

			$scope.modalTitle = 'Execute Calculations';
			$scope.message = 'Please choose the dataset where you would like to execute the calculation from:';
			$scope.measurementDatasetId = studyContext.measurementDatasetId;
			$scope.selected = {datasetId: $scope.measurementDatasetId};

			$scope.next = function () {

				if ($scope.selected.datasetId === $scope.measurementDatasetId) {
					$rootScope.navigateToTab('editMeasurements');
				} else {
					$rootScope.navigateToSubObsTab($scope.selected.datasetId);
				}

				derivedVariableModalService.openExecuteCalculatedVariableModal($scope.selected.datasetId);
				$uibModalInstance.close();
			};

		}]);

	derivedVariableModule.controller('executeCalculatedVariableModalCtrl',
		['$rootScope', '$scope', '$http', '$uibModalInstance', 'datasetService', 'derivedVariableModalService', 'derivedVariableService', 'datasetId', 'studyContext',
			function ($rootScope, $scope, $http, $uibModalInstance, datasetService, derivedVariableModalService, derivedVariableService, datasetId, studyContext) {

				$scope.instances = [];
				$scope.selectedInstances = {};
				$scope.isEmptySelection = false;
				$scope.selected = {variable: undefined};

				$scope.init = function () {
					datasetService.getDataset(datasetId).then(function (dataset) {
						$scope.variableListView = buildVariableListView(dataset.variables);
						$scope.instances = dataset.instances;
					});
				};

				$scope.cancel = function () {
					$uibModalInstance.close();
				};

				$scope.reloadObservation = function () {
					$('.import-study-data').data('data-import', '1');
					$('body').addClass('import-preview-measurements');

					var columnsOrder = BMS.Fieldbook.MeasurementsTable.getColumnOrdering('measurement-table');
					new BMS.Fieldbook.ImportPreviewMeasurementsDataTable('#import-preview-measurement-table', JSON.stringify(columnsOrder));
					$('.fbk-discard-imported-data').removeClass('fbk-hide');

					showSuccessfulMessage('', 'Calculated values for ' + $scope.selected.variable.name + ' were added successfully.');
				};

				$scope.reloadSubObservation = function () {
					$rootScope.navigateToSubObsTab(datasetId);
					showSuccessfulMessage('', 'Calculated values for ' + $scope.selected.variable.name + ' were added successfully.');
				};

				$scope.execute = function () {
					var geoLocationIds = [];

					Object.keys($scope.selectedInstances).forEach(function (instanceDbId) {
						var isSelected = $scope.selectedInstances[instanceDbId];
						if (isSelected) {
							geoLocationIds.push(instanceDbId);
						}
					});

					var calculateData = {
						variableId: $scope.selected.variable.cvTermId
						, geoLocationIds: geoLocationIds
					};

					// If selected dataset is PLOT DATA
					if (datasetId === studyContext.measurementDatasetId) {
						derivedVariableService.calculateVariableForObservation(calculateData)
							.then(function (hasDataOverwrite) {
								if (hasDataOverwrite) {
									derivedVariableModalService.confirmOverrideCalculatedVariableModal(datasetId, $scope.selected.variable);
								} else {
									$scope.reloadObservation();
								}
								$uibModalInstance.close();
							});
					} else {
						derivedVariableService.calculateVariableForSubObservation(datasetId, calculateData)
							.then(function (hasDataOverwrite) {
								$scope.reloadSubObservation();
								$uibModalInstance.close();
							});
					}


				};

				function buildVariableListView(variables) {
					var variableListView = [];
					angular.forEach(variables, function (variable) {
						if (variable.formula) {
							variableListView.push({name: variable.name, cvTermId: variable.termId});//termId
						}
					});
					return variableListView;
				};


				$scope.init();

			}]);

	derivedVariableModule.controller('confirmOverrideCalculatedVariableModalCtrl', ['$scope', '$http', '$uibModalInstance', 'derivedVariableModalService', 'selectedVariable', 'datasetId',
		function ($scope, $http, $uibModalInstance, derivedVariableModalService, selectedVariable, datasetId) {

			$scope.goBack = function () {
				$http.get('/Fieldbook/ImportManager/revert/data')
					.then(function (response) {
						$scope.revertData();
						$uibModalInstance.close();
						derivedVariableModalService.openExecuteCalculatedVariableModal(datasetId);
					});

			};

			$scope.revertData = function () {
				$('body').removeClass('import-preview-measurements');
				showSuccessfulMessage('', 'Discarded data successfully');

				if ($('#measurement-table').length !== 0 && $('#measurement-table').dataTable()) {
					$('#measurement-table').dataTable().fnAdjustColumnSizing();
				}
				$('#review-out-of-bounds-data-list').hide();
				$('.fbk-discard-imported-data').addClass('fbk-hide');
				$('.import-study-data').data('data-import', '0');
			};

			$scope.proceed = function () {

				$('.import-study-data').data('data-import', '1');
				$('body').addClass('import-preview-measurements');

				var columnsOrder = BMS.Fieldbook.MeasurementsTable.getColumnOrdering('measurement-table');
				new BMS.Fieldbook.ImportPreviewMeasurementsDataTable('#import-preview-measurement-table', JSON.stringify(columnsOrder));
				$('.fbk-discard-imported-data').removeClass('fbk-hide');

				showSuccessfulMessage('', 'Calculated values for ' + selectedVariable.name + ' were added successfully.');

				$uibModalInstance.close();

			};

		}]);
})();
