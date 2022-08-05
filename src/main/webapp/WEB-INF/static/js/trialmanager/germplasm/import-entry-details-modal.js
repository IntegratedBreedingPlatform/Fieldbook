(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.controller('ImportEntryDetailsController', ['$scope', '$rootScope', 'studyContext', '$uibModalInstance',
		function ($scope, $rootScope, studyContext, $uibModalInstance) {

			$scope.url = '/ibpworkbench/controller/jhipster#/import-entry-details?restartApplication' +
				'&cropName=' + studyContext.cropName +
				'&programUUID=' + studyContext.programId +
				'&studyId=' + studyContext.studyId +
				'&selectedProjectId=' + studyContext.selectedProjectId +
				'&loggedInUserId=' + studyContext.loggedInUserId;

			window.closeModal = function() {
				$uibModalInstance.close(null);
			};

			window.handleImportSuccess = function() {
				$uibModalInstance.close();
				showSuccessfulMessage('', $.germplasmMessages.importEntrySuccess);
				$rootScope.$emit("reloadStudyEntryTableData", {});
			};

			$scope.cancel = function() {
				$uibModalInstance.close(null);
			};

		}]);

})();
