(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.controller('GermplasmSelectorCtrl', ['$scope', '$rootScope', '$q', 'studyContext', '$uibModalInstance', 'selectMultiple',
		function ($scope, $rootScope, $q, studyContext, $uibModalInstance, selectMultiple) {

			$scope.url = '/ibpworkbench/controller/jhipster#/germplasm-selector?restartApplication' +
				'&cropName=' + studyContext.cropName +
				'&programUUID=' + studyContext.programId +
				'&selectedProjectId=' + studyContext.selectedProjectId +
				'&loggedInUserId=' + studyContext.loggedInUserId +
				'&selectMultiple=' + selectMultiple;

			window.closeModal = function() {
				$uibModalInstance.close(null);
			};

			window.onGidsSelected = function(gids) {
				$uibModalInstance.close(gids);
			};

			$scope.cancel = function() {
				$uibModalInstance.close(null);
			};

		}]);

})();
