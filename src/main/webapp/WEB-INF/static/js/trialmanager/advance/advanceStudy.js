(function () {
	'use strict';

	const module = angular.module('manageTrialApp');
	module.controller('AdvanceStudyModalCtrl', ['$scope', '$q', 'studyContext', '$uibModalInstance', 'trialInstances', 'advanceType', 'noOfReplications', 'advanceStudyModalService',
		function ($scope, $q, studyContext, $uibModalInstance, trialInstances, advanceType, noOfReplications, advanceStudyModalService) {

			$scope.url = '/ibpworkbench/controller/jhipster#/advance-study?restartApplication' +
				'&cropName=' + studyContext.cropName +
				'&programUUID=' + studyContext.programId +
				'&studyId=' + studyContext.studyId +
				'&trialDatasetId=' + studyContext.trialDatasetId +
				'&noOfReplications=' + noOfReplications +
				'&trialInstances=' + trialInstances;

			window.closeModal = function() {
				$uibModalInstance.close(null);
				advanceStudyModalService.startAdvance('Study')
			};

			$scope.cancel = function() {
				$uibModalInstance.close(null);
			};

		}]);
})();
