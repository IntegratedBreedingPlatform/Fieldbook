(function () {
	'use strict';

	angular.module('manageTrialApp').factory('locationService', ['$http', '$q', 'studyContext', 'serviceUtilities',
		function ($http, $q, studyContext, serviceUtilities) {

			var BASE_URL = '/bmsapi/crops/' + studyContext.cropName;

			var failureHandler = serviceUtilities.restFailureHandler;

			var locationService = {};

			locationService.getLocations = function (locationTypes, favoritesOnly, name, page, size) {

				var request = $http.get(BASE_URL + '/locations?programUUID=' + studyContext.programId + '&favoritesOnly=' + favoritesOnly
					+ '&name=' + name + '&locationTypes=' + locationTypes + '&page=' + page + '&size=' + size);
				return request.then(((response) => {
					return response;
				}), failureHandler);
			};

			return locationService;

		}]);

	angular.module('manageTrialApp').factory('locationModalService', ['$http', 'studyContext', '$uibModal',
		function ($http, studyContext, $uibModal) {

			//url.program.breeding.methods=ibpworkbench/content/ProgramMethods?programId=

			var locationModalService = {};

			locationModalService.openManageLocations = function () {
				$uibModal.open({
					templateUrl: '/Fieldbook/static/angular-templates/location/manageLocationsModal.html',
					windowClass: 'force-zindex', // make sure that the modal is always in front of all modals
					controller: function ($scope, $uibModalInstance) {
						$scope.iframeUrl = '/ibpworkbench/content/ProgramLocations?programId=' + studyContext.programId;

						$scope.close = function () {
							$uibModalInstance.close();
						}
					},
					size: 'window-width'
				});
			};

			return locationModalService;

		}]);

	angular.module('manageTrialApp').directive('locationsSelect', ['locationService', function (locationService) {
		return {
			restrict: 'EA',
			scope: {
				targetkey: '=',
				valuecontainer: '=',
				onLocationSelect: ' &'
			},
			templateUrl: '/Fieldbook/static/angular-templates/location/locationSelect.html',
			link: function (scope, element, attrs, paginationCtrl) {
			},
			controller: ['$scope', 'locationService', function ($scope, locationService) {
				var BREEDING_LOCATION = [410, 411];

				$scope.locationItems = [];
				$scope.locationPage = 0;
				$scope.loadMore = true;
				$scope.localData = {locationLookup: 1, useFavorites: false};

				$scope.fetch = function ($select, $event) {
					// no event means first load!
					if (!$event) {
						$scope.locationPage = 0;
						$scope.locationItems = [];
					} else {
						$event.stopPropagation();
						$event.preventDefault();
						$scope.locationPage++;
					}

					locationService.getLocations($scope.localData.locationLookup == 1 ? BREEDING_LOCATION : [], $scope.localData.useFavorites, $select ? $select.search : '', $scope.locationPage, 500).then(function (response) {
						$scope.locationItems = $scope.locationItems.concat(response.data);
						$scope.loadMore = ($scope.locationPage + 1) * 500 < response.headers()['x-filtered-count'];
					});
				}
			}]
		};
	}]);
})();
