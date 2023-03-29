(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.controller('SampleGenotypeImportController', ['$scope', '$rootScope', 'studyContext', '$uibModalInstance', 'studyStateService', 'listId', 'fromFile',
		function ($scope, $rootScope, studyContext, $uibModalInstance, studyStateService, listId, fromFile) {
			const route = fromFile? 'genotype-import-file': 'genotype-import';
			$scope.url = '/ibpworkbench/controller/jhipster#/' + route + '/' + listId + '?restartApplication' +
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

			genotypeModalService.openImportGenotypes = function (listId, fromFile) {
				$uibModal.open({
					templateUrl: '/Fieldbook/static/angular-templates/sample-genotype/sample-genotype-modal.html',
					controller: "SampleGenotypeImportController",
					size: 'lg',
					resolve: {
						fromFile: function () {
							return fromFile;
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
