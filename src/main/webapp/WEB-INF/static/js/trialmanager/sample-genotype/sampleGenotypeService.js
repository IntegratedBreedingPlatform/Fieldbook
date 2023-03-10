(function () {
    'use strict';

    var sampleGenotypeModule = angular.module('sample-genotype-tab');

    sampleGenotypeModule.factory('sampleGenotypeService', ['$http', '$q', 'studyContext', 'serviceUtilities',
        function ($http, $q, studyContext, serviceUtilities) {

            var BASE_URL = '/bmsapi/crops/' + studyContext.cropName + '/programs/' + studyContext.programId + '/samples/genotypes/table';
            var successHandler = serviceUtilities.restSuccessHandler,
                failureHandler = serviceUtilities.restFailureHandler;

            var sampleGenotypeService = {};

            sampleGenotypeService.getSampleGenotypesTableUrl = function () {
                return BASE_URL;
            };

            sampleGenotypeService.searchSampleGenotypes = function (genotypeSearchRequest, page, pageSize) {
                genotypeSearchRequest.studyId = studyContext.studyId;
                return $http.post(BASE_URL + ((page && pageSize) ? '?page=' + page + '&size=' + pageSize : ''), genotypeSearchRequest)
                    .then(successHandler, failureHandler);
            };

            return sampleGenotypeService;

        }]);

})();
