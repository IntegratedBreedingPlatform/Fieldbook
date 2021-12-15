(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.controller('StaBrappModalCtrl', ['$scope', '$q', 'studyContext', '$uibModalInstance', 'STABRAPP_URL', 'instanceIds',
		function ($scope, $q, studyContext, $uibModalInstance, STABRAPP_URL, instanceIds) {

			$scope.stabrappUrl = STABRAPP_URL;

			$scope.cancel = function () {
				$uibModalInstance.close(null);
			};

			$scope.load = function () {
				// iframe blocked by content-security policy, so opening in new window
				window.open($scope.stabrappUrl +
					'?cropDb=' + studyContext.cropName +
					'&token=' + JSON.parse(localStorage['bms.xAuthToken']).token +
					'&apiURL=' + window.location.origin + '/bmsapi' +
					'&studyDbIds=' + instanceIds
				, '_blank');
			};

		}]);

})();
