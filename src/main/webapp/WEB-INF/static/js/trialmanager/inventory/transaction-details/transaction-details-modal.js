(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.controller('TransactionDetailsCtrl', ['$scope', '$q', 'studyContext', '$uibModalInstance', 'gid','lotId',
		function ($scope, $q, studyContext, $uibModalInstance, gid,lotId) {

			$scope.url = '/ibpworkbench/controller/jhipster#/transaction-details-dialog?restartApplication' +
				'&cropName=' + studyContext.cropName +
				'&programUUID=' + studyContext.programId +
				'&gid=' + gid + '&lotId=' + lotId + '&modal';

			window.closeModal = function() {
				$uibModalInstance.close(null);
			};

			$scope.cancel = function() {
				$uibModalInstance.close(null);
			};
		}]);

})();
