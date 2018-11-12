(function () {
	'use strict';


	var exportStudyModule = angular.module('export-study', ['ui.bootstrap', 'datasets-api']);

	exportStudyModule.factory('exportStudyModalService', ['$uibModal', function ($uibModal) {

		var BASE_URL = '/Fieldbook/static/angular-templates/exportStudy/';
		var exportStudyModalService = {};

		exportStudyModalService.openDatasetOptionModal = function () {
			$uibModal.open({
				templateUrl: BASE_URL + 'exportDatasetOptionModal.html',
				controller: 'exportDatasetOptionCtrl',
				size: 'md'
			});
		};

		exportStudyModalService.openExportStudyModal = function () {
			$uibModal.open({
				templateUrl: BASE_URL + "exportStudyModal.html",
				controller: "exportStudyCtrl",
				size: 'md'
			});
		};

		exportStudyModalService.redirectToOldExportModal = function () {
			// Call the global function to show the old export study modal
			showExportStudyModal();
		};

		return exportStudyModalService;

	}]);

	exportStudyModule.controller('exportDatasetOptionCtrl', ['$scope', '$uibModal', '$uibModalInstance', 'studyContext', 'exportStudyModalService',
		'datasetService', function ($scope, $uibModal, $uibModalInstance, studyContext, exportStudyModalService, datasetService) {

			var ctrl = this;

			$scope.measurementDatasetId = studyContext.measurementDatasetId
			$scope.selectedDatasetId = $scope.measurementDatasetId;
			$scope.datasets;

			ctrl.init = function () {
				datasetService.getDatasets().then(function (datasets) {
					$scope.datasets = datasets;
				});
			};

			$scope.cancel = function () {
				$uibModalInstance.dismiss();
			};

			$scope.continue = function () {
				$scope.showExportOptions();
				$uibModalInstance.close();
			};

			$scope.showExportOptions = function () {

				if ($scope.measurementDatasetId === $scope.selectedDatasetId) {
					// If the selected dataset is a PLOT OBSERVATION, then use the old
					// export study modal (non-Angular)
					exportStudyModalService.redirectToOldExportModal();
				} else {
					exportStudyModalService.openExportStudyModal();
				}

			};

			$scope.getDatasetType = datasetService.getDatasetType;

			ctrl.init();

		}]);

	exportStudyModule.controller('exportStudyCtrl', ['$scope', '$uibModalInstance', function ($scope, $uibModalInstance) {

		$scope.cancel = function () {
			$uibModalInstance.dismiss();
		}

		$scope.export = function () {
			$uibModalInstance.close();
		}

	}]);


})();