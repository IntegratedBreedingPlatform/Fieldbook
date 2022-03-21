/*global angular */

(function () {
    'use strict';

    var manageTrialAppModule = angular.module('manageTrialApp');

    manageTrialAppModule.factory('studyEntryObservationService', ['$http', 'serviceUtilities', 'studyContext', function ($http, serviceUtilities, studyContext) {

		var BASE_URL = '/bmsapi/crops/' + studyContext.cropName + '/programs/' + studyContext.programId + '/studies/' + studyContext.studyId + '/observations';

        var studyEntryObservationService = {};

        var successHandler = serviceUtilities.restSuccessHandler,
            failureHandler = serviceUtilities.restFailureHandler;

		studyEntryObservationService.countObservationsByVariables = function (variableIds) {
            return $http.head(BASE_URL + "?", {
				params: {
					variableIds: variableIds.join(",")
				}
			});
            // return request.then(successHandler, failureHandler);
        };

        return studyEntryObservationService;

    }
    ]);


})();
