(function () {
	'use strict';

	angular.module('manageTrialApp').factory('locationService', ['$http', '$q', 'studyContext', 'serviceUtilities',
		function ($http, $q, studyContext, serviceUtilities) {

			var BASE_URL = '/bmsapi/crops/' + studyContext.cropName;

			var failureHandler = serviceUtilities.restFailureHandler;

			var locationService = {};

			locationService.getLocations = function (locationTypes, favoritesOnly, name, page, size) {

				var data = {
					locationNameFilter: {
						type: 'STARTSWITH',
						value: name
					},
					locationTypeIds: locationTypes
				}
				if (favoritesOnly) {
					data['filterFavoriteProgramUUID'] = true;
					data['favoriteProgramUUID'] = studyContext.programId;
				}

				var request = $http.post(BASE_URL + '/locations/search?programUUID=' + studyContext.programId + '&page=' + page + '&size=' + size, JSON.stringify(data));
				return request.then(((response) => {
					return response;
				}), failureHandler);
			};

			locationService.getBreedingLocationDefault = function () {
				var request = $http.get(BASE_URL + '/programs/' + studyContext.programId + '/locations/default/BREEDING_LOCATION');
				return request.then(((response) => {
					return response;
				}), failureHandler);
			}

			return locationService;

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
				$scope.defaultLocation = null;

				$scope.init = function () {
					// Get the default location in the program
					// and set it as default selected item in the dropdown.
					if ($scope.valuecontainer[$scope.targetkey] === null) {
						locationService.getBreedingLocationDefault().then((response) => {
							if (response && response.data) {
								$scope.defaultLocation = response.data;
								$scope.$applyAsync(function () {
									// Use applyAsync so that the model is updated
									$scope.valuecontainer[$scope.targetkey] = $scope.defaultLocation.id;
								});
							}
						});
					}
				};

				$scope.fetch = function ($select, $event) {
					// no event means first load!
					if (!$event) {
						$scope.locationPage = 0;
						$scope.locationItems = [];
						if ($scope.defaultLocation) {
							$scope.locationItems.push($scope.defaultLocation);
						}
					} else {
						$event.stopPropagation();
						$event.preventDefault();
						$scope.locationPage++;
					}

					locationService.getLocations($scope.localData.locationLookup == 1 ? BREEDING_LOCATION : [], $scope.localData.useFavorites, $select ? $select.search : '', $scope.locationPage, 500).then(function (response) {
						$scope.locationItems = $scope.locationItems.concat(response.data);
						$scope.loadMore = ($scope.locationPage + 1) * 500 < response.headers()['x-total-count'];
					});
				};

				$scope.init();
			}]
		};
	}]);
})();
