(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.controller('SampleGenotypeImportController', ['$scope', '$rootScope', 'studyContext', '$uibModalInstance', 'studyStateService', 'listId', 'routeString',
		function ($scope, $rootScope, studyContext, $uibModalInstance, studyStateService, listId, routeString) {
			$scope.url = '/ibpworkbench/controller/jhipster#/' + routeString + '/' + listId + '?restartApplication' +
				'&cropName=' + studyContext.cropName +
				'&programUUID=' + studyContext.programId +
				'&studyId=' + studyContext.studyId +
				'&selectedProjectId=' + studyContext.selectedProjectId +
				'&loggedInUserId=' + studyContext.loggedInUserId +
				'&hasGeneratedDesign=' + studyStateService.hasGeneratedDesign();

			window.closeModal = function () {
				$uibModalInstance.close(null);
			};

			window.handleImportGenotypesSuccess = function () {
				$('#importGenotypesAction' + listId).hide();

				// TODO: i18n
				showSuccessfulMessage('', 'Genotype data for samples is successfully saved.');

				// Notify the application that sample genotypes data has been saved.
				$rootScope.$broadcast('genotypesImportSaved');

				// Refresh and show the 'Sample Genotypes' tab after sample genotypes import is successful.
				$rootScope.navigateToTab('sampleGenotypes', {reload: true});

				$uibModalInstance.close();
			};

			$scope.cancel = function () {
				$uibModalInstance.close(null);
			};

		}]);

	module.factory('sampleGenotypeModalService', ['$uibModal',
		function ($uibModal) {

			var genotypeModalService = {};

			genotypeModalService.openImportGenotypesFromFile = function (listId) {
				$uibModal.open({
					templateUrl: '/Fieldbook/static/angular-templates/sample-genotype/sample-genotype-modal.html',
					controller: "SampleGenotypeImportController",
					size: 'lg',
					resolve: {
						routeString: function () {
							return 'genotype-import-file';
						},
						listId: function () {
							return listId;
						}
					},
				});
			};

			genotypeModalService.openImportGenotypesFromGigwa = function (listId) {
				$uibModal.open({
					templateUrl: '/Fieldbook/static/angular-templates/sample-genotype/sample-genotype-modal.html',
					controller: "SampleGenotypeImportController",
					size: 'lg',
					resolve: {
						routeString: function () {
							return 'genotype-import';
						},
						listId: function () {
							return listId;
						}
					},
				});
			};

			return genotypeModalService;

		}]);

})();
