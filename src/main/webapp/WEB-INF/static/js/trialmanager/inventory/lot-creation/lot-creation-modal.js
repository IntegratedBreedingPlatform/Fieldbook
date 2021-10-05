(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.controller('LotCreationCtrl', ['$scope', '$q', 'studyContext', '$uibModalInstance', 'searchResultsDbId',
		function ($scope, $q, studyContext, $uibModalInstance, searchResultsDbId) {

			$scope.url = '/ibpworkbench/controller/jhipster#/lot-creation-dialog?restartApplication' +
				'&cropName=' + studyContext.cropName +
				'&programUUID=' + studyContext.programId +
				'&searchRequestId=' + searchResultsDbId +
				'&studyId=' + studyContext.studyId +
				'&searchOrigin=MANAGE_STUDY';
			
			$scope.cancel = function () {
				$uibModalInstance.close(null);
			};

		}]);

})();
