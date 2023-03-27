(function () {
    'use strict';

    var sampleGenotypeModule = angular.module('sample-genotype-tab');

    sampleGenotypeModule.factory('sampleGenotypeService', ['$http', '$q', 'studyContext', 'serviceUtilities',
        function ($http, $q, studyContext, serviceUtilities) {

            var BASE_URL = '/bmsapi/crops/' + studyContext.cropName + '/programs/' + studyContext.programId
                + '/studies/' + studyContext.studyId + '/samples/genotypes/table';
            var successHandler = serviceUtilities.restSuccessHandler,
                failureHandler = serviceUtilities.restFailureHandler;

            var sampleGenotypeService = {};

            sampleGenotypeService.searchSampleGenotypes = function (genotypeSearchRequest, page, pageSize) {
                genotypeSearchRequest.studyId = studyContext.studyId;
                return $http.post(BASE_URL + ((page !== undefined && pageSize !== undefined) ? '?page=' + page + '&size=' + pageSize : ''), genotypeSearchRequest)
                    .then(successHandler, failureHandler);
            };

			sampleGenotypeService.getColumns = function (sampleListIds) {
				var request = $http.get(BASE_URL + '/columns?sampleListIds=' + sampleListIds.join(','));
				return request.then(successHandler, failureHandler);
			};

            return sampleGenotypeService;

        }]);

})();
