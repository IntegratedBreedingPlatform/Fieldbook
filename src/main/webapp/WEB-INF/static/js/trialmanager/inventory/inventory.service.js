(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.factory('InventoryService', ['$http', 'serviceUtilities', 'studyContext',
		function ($http, serviceUtilities, studyContext) {
			var successHandler = serviceUtilities.restSuccessHandler,
				failureHandler = serviceUtilities.restFailureHandler;

			var BASE_URL = '/bmsapi/crops/' + studyContext.cropName;

			var service = {};

			service.queryUnits = function () {
				return $http.get(`${BASE_URL}/inventory-units`)
					.then(successHandler, failureHandler);
			}

			service.queryTransactionTypes = function () {
				return $http.get(`${BASE_URL}/transaction-types`)
					.then(successHandler, failureHandler);
			};

			service.queryTransactionStatusTypes = function () {
				return $http.get(`${BASE_URL}/transaction-status-types`)
					.then(successHandler, failureHandler);
			};

			service.searchStudyTransactions = function (searchRequest) {
				return $http.post(service.getSearchStudyTransactionsUrl(), searchRequest)
					.then(successHandler, failureHandler);
			}

			service.getSearchStudyTransactionsUrl = function () {
				let programUUID = studyContext.programId;
				let studyId = studyContext.studyId;
				return `${BASE_URL}/programs/${programUUID}/studies/${studyId}/transactions/search`;
			};

			return service;
		}
	]);
})();
