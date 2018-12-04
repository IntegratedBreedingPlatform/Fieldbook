/*global angular*/

(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.factory('environmentService', ['$rootScope', 'TrialManagerDataService', '$http', 'serviceUtilities', 'studyContext', function ($rootScope, TrialManagerDataService, $http, serviceUtilities, studyContext) {

		var environmentService = {};
		var successHandler = serviceUtilities.restSuccessHandler,
			failureHandler = serviceUtilities.restFailureHandler;

		environmentService.getEnvironments = function () {
			var request = $http.get('/bmsapi/study/' + studyContext.cropName + '/' + studyContext.studyId + '/instances');
			return request.then(successHandler, failureHandler);
		};

		environmentService.environments = TrialManagerDataService.currentData.environments;

		environmentService.changeEnvironments = function () {
			this.broadcastEnvironments();
		};

		environmentService.broadcastEnvironments = function () {
			$rootScope.$broadcast('changeEnvironments');
		};

		return environmentService;

	}]);
})();
