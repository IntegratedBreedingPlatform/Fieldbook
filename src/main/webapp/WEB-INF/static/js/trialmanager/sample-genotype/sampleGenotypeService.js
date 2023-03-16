(function () {
    'use strict';

    var sampleGenotypeModule = angular.module('sample-genotype-tab');

    sampleGenotypeModule.factory('sampleGenotypeService', ['$http', '$q', 'studyContext', 'serviceUtilities',
        function ($http, $q, studyContext, serviceUtilities) {

            var BASE_URL = '/bmsapi/crops/' + studyContext.cropName + '/programs/' + studyContext.programId;
            var SAMPLE_GENOTYPES_URL = '/samples/genotypes/table';
            var successHandler = serviceUtilities.restSuccessHandler,
                failureHandler = serviceUtilities.restFailureHandler;

            var sampleGenotypeService = {};

            sampleGenotypeService.getSampleGenotypesTableUrl = function () {
                return BASE_URL + SAMPLE_GENOTYPES_URL;
            };

            sampleGenotypeService.searchSampleGenotypes = function (genotypeSearchRequest, page, pageSize) {
                genotypeSearchRequest.studyId = studyContext.studyId;
                return $http.post(sampleGenotypeService.getSampleGenotypesTableUrl() + ((page && pageSize) ? '?page=' + page + '&size=' + pageSize : ''), genotypeSearchRequest)
                    .then(successHandler, failureHandler);
            };

            sampleGenotypeService.getColumns = function () {
                var request = $http.get(BASE_URL + '/studies/' + studyContext.studyId + SAMPLE_GENOTYPES_URL + '/columns');
                return request.then(successHandler, failureHandler);
            };

            return sampleGenotypeService;

        }]);

})();
