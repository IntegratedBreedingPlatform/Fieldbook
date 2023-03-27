(function () {
    'use strict';

    var module = angular.module('manageTrialApp');

    module.factory('sampleListService', ['$http', '$q', 'studyContext', 'serviceUtilities',
        function ($http, $q, studyContext, serviceUtilities) {

            var BASE_URL = '/bmsapi/crops/' + studyContext.cropName + '/programs/' + studyContext.programId +
                '/studies' + studyContext.studyId;

            var successHandler = serviceUtilities.restSuccessHandler,
                failureHandler = serviceUtilities.restFailureHandler;

            var sampleListService = {};

            sampleListService.getSampleListsInStudy = function (withGenotypesOnly) {
                var request = $http.get(BASE_URL + '/sample-lists?withGenotypesOnly=' + withGenotypesOnly);
                return request.then(successHandler, failureHandler);
            };

            return sampleListService;

        }]);

})();
