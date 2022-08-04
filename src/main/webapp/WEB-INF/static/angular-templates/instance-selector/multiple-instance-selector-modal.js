(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.directive('multipleInstanceSelectorModal', [
		function () {
			return {
				restrict: 'E',
				scope: {
					instances: '=',
					selectedInstances: '=',
					instanceIdProperty: '@',
					onContinue: '=',
					modalTitle: '=',
				},
				templateUrl: '/Fieldbook/static/angular-templates/instance-selector/multiple-instance-selector-modal.html',
				controller: function ($scope) {
					$scope.cancel = function () {
						$scope.$parent.$dismiss();
					};

					$scope.continue = function () {
						$scope.onContinue();
						$scope.$parent.$close();
					};
				}
			}
		}
	]);

})();
