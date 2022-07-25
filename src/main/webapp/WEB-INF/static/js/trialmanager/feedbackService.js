/*global angular */

(function () {
    'use strict';

    var manageTrialAppModule = angular.module('manageTrialApp');

    manageTrialAppModule.factory('feedbackService', ['$http', 'serviceUtilities', function ($http, serviceUtilities) {

		var BASE_URL = '/bmsapi/feedback/';

        var feedbackService = {};

        var successHandler = serviceUtilities.restSuccessHandler,
            failureHandler = serviceUtilities.restFailureHandler;

		feedbackService.shouldShowFeedback = function (feature) {
			var request = $http.get(BASE_URL + feature + '/should-show');
			return request.then(successHandler, failureHandler);
		};

        return feedbackService;

    }
    ]);


})();
