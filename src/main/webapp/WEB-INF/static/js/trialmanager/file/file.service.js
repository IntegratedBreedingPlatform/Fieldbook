(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	angular.module('manageTrialApp').factory('fileService', ['$http', '$q', 'studyContext', 'serviceUtilities', '$uibModal',
		'fileDownloadHelper',
		function ($http, $q, studyContext, serviceUtilities, $uibModal, fileDownloadHelper) {

			var BASE_URL = '/bmsapi/crops/' + studyContext.cropName;

			var successHandler = serviceUtilities.restSuccessHandler,
				failureHandler = serviceUtilities.restFailureHandler;

			// TODO remove completely if not needed anymore
			var fileService = {};

			fileService.getFileStorageStatus = function () {
				return $http.get(BASE_URL + '/filestorage/status').then(successHandler, failureHandler);
			};

			fileService.getFileCount = function (variableIds, datasetId, instanceId) {
				return $http.head(BASE_URL + '/filemetadata', {
					params: {
						variableIds,
						datasetId,
						instanceId
					}
				});
			};

			fileService.detachFiles = function (variableIds, datasetId, instanceId) {
				return $http.delete(BASE_URL + '/filemetadata/variables', {
					params: {
						variableIds,
						datasetId,
						instanceId
					}
				}).then(successHandler, failureHandler);
			};

			fileService.removeFiles = function (variableIds, datasetId, instanceId) {
				return $http.delete(BASE_URL + '/filemetadata', {
					params: {
						variableIds,
						datasetId,
						instanceId
					}
				}).then(successHandler, failureHandler);
			};

			return fileService;
		}]);

}());
