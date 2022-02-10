(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.controller('LotCreationCtrl', ['$scope', '$q', 'studyContext', '$uibModalInstance', 'searchResultDbId', 'searchOrigin',
		function ($scope, $q, studyContext, $uibModalInstance, searchResultDbId, searchOrigin) {

			$scope.url = '/ibpworkbench/controller/jhipster#/lot-creation-dialog?restartApplication' +
				'&cropName=' + studyContext.cropName +
				'&programUUID=' + studyContext.programId +
				'&searchRequestId=' + searchResultDbId +
				'&studyId=' + studyContext.studyId +
				'&searchOrigin=' + searchOrigin;
			
			$scope.cancel = function () {
				$uibModalInstance.close(null);
			};

		}]);

})();
