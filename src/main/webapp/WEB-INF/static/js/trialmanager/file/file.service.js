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

			return fileService;
		}]);

}());
