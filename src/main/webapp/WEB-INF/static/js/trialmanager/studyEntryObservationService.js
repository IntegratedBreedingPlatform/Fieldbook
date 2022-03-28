/*global angular */

(function () {
    'use strict';

    var manageTrialAppModule = angular.module('manageTrialApp');

    manageTrialAppModule.factory('studyEntryObservationService', ['$http', 'serviceUtilities', 'studyContext', function ($http, serviceUtilities, studyContext) {

		var BASE_URL = '/bmsapi/crops/' + studyContext.cropName + '/programs/' + studyContext.programId + '/studies/' + studyContext.studyId + '/observations';

        var studyEntryObservationService = {};

        var successHandler = serviceUtilities.restSuccessHandler,
            failureHandler = serviceUtilities.restFailureHandler;

		studyEntryObservationService.addObservation = function (observation) {
			var request = $http.post(BASE_URL, observation);
			return request.then(successHandler, failureHandler);
		};

		studyEntryObservationService.updateObservation = function (observation) {
			var request = $http.patch(BASE_URL, observation);
			return request.then(successHandler, failureHandler);
		};

		studyEntryObservationService.deleteObservation = function (observationId) {
			var request = $http.delete(BASE_URL + '/' + observationId);
			return request.then(successHandler, failureHandler);
		};

		studyEntryObservationService.countObservationsByVariables = function (variableIds) {
            return $http.head(BASE_URL + "?", {
				params: {
					variableIds: variableIds.join(",")
				}
			});
        };

        return studyEntryObservationService;

    }
    ]);


})();
