/*global angular*/
'use strict';

(function() {
	var showSettingFormElementNew = angular.module('showSettingFormElementNew', []);

	showSettingFormElementNew.directive('showSettingFormElementNew', ['_', function(_) {
    			return {
    				require: '?uiSelect2, ?ngModel',
    				restrict: 'E',
    				scope: {
    					settings: '=',
    					targetkey: '@targetkey',
    					settingkey: '@',
    					valuecontainer: '=',
    					changefunction: '&',
    					blockInput: '='
    				},

    				templateUrl: '/Fieldbook/static/angular-templates/showSettingFormElementNew.html',
    				compile: function(tElement, tAttrs, transclude, uiSelect2) {
    					if (uiSelect2) {
    						uiSelect2.compile(tElement, tAttrs);
    					}
    				},
    				controller: function($scope, LOCATION_ID, BREEDING_METHOD_ID, BREEDING_METHOD_CODE, $http) {
    					if ($scope.settingkey === undefined) {
    						$scope.settingkey = $scope.targetkey;
    					}

    					if (!$scope.changefunction) {
    						$scope.changefunction = function() {
    						};
    					}

    					$scope.variableDefinition = $scope.settings.val($scope.settingkey);
    					$scope.widgetType = $scope.variableDefinition.variable.widgetType.$name ?
    						$scope.variableDefinition.variable.widgetType.$name : $scope.variableDefinition.variable.widgetType;
    					$scope.hasDropdownOptions = $scope.widgetType === 'DROPDOWN';

    					$scope.isLocation = parseInt(LOCATION_ID, 10) === parseInt($scope.variableDefinition.variable.cvTermId, 10);
                        if($scope.isLocation){
                            selectedLocation('', $scope.variableDefinition.possibleValues);
                            angular.forEach($scope.variableDefinition.possibleValues, function(val, index){
                                if(val.id == $scope.valuecontainer[$scope.targetkey]){
                                    selectedLocation(val , $scope.variableDefinition.possibleValues);
                                }
                            });
                        }
    					$scope.isBreedingMethod = parseInt(BREEDING_METHOD_ID, 10) === parseInt($scope.variableDefinition.variable.cvTermId, 10) ||
    						parseInt(BREEDING_METHOD_CODE, 10) === parseInt($scope.variableDefinition.variable.cvTermId, 10);

    					$scope.localData = {};
    					$scope.localData.useFavorites = false;
						$scope.locationLookup = 1;

						
						$scope.updateDropdownValuesFavorites = function() { // Change state for favorite checkbox
						if($scope.localData.useFavorites){
							if($scope.locationLookup ==1){
								$scope.dropdownValues = $scope.variableDefinition.possibleValuesFavorite;
							}else{
								$scope.dropdownValues = $scope.variableDefinition.allFavoriteValues;
							}
							
						}else{
							if($scope.locationLookup ==1){
								$scope.dropdownValues = $scope.variableDefinition.possibleValues;
							}else{
								$scope.dropdownValues = $scope.variableDefinition.allValues;
							}
						}
					};
					

					$scope.updateDropdownValuesBreedingLocation = function() { // Change state for breeding
																												// location radio
								$scope.dropdownValues = ($scope.localData.useFavorites) ? $scope.variableDefinition.possibleValuesFavorite
										: $scope.variableDefinition.possibleValues;
								$scope.locationLookup = 1;
					};

					$scope.updateDropdownValuesAllLocation = function() { // Change state for all locations radio
								$scope.dropdownValues = ($scope.localData.useFavorites) ? $scope.variableDefinition.allFavoriteValues
										: $scope.variableDefinition.allValues;
								$scope.locationLookup = 2;
					};
					
					// if the value of the dropdown from existing data matches from the list of favorites, we set the checkbox as true
    					var useFavorites = function(currentVal) {

    						if (!$scope.variableDefinition.existingData && null !== $scope.variableDefinition.possibleValuesFavorite) {
    							return $scope.variableDefinition.possibleValuesFavorite.length > 0;
    						} else if (currentVal !== null && !isNaN(currentVal) && null !== $scope.variableDefinition.possibleValuesFavorite) {
    							return $scope.localData.useFavorites || _.where($scope.variableDefinition.possibleValuesFavorite, {'id':parseInt(currentVal, 10)}).length > 0;
    						}

    						return $scope.localData.useFavorites;
    					};

    					if ($scope.hasDropdownOptions) {
    						var currentVal = $scope.valuecontainer[$scope.targetkey];

    						// lets fix current val if its an object so that valuecontainer only contains the id
    						if (typeof currentVal !== 'undefined' && currentVal !== null && typeof currentVal.id !== 'undefined' && currentVal.id) {
    							currentVal = currentVal.id;
    							$scope.valuecontainer[$scope.targetkey] = currentVal;
    						}

    						$scope.localData.useFavorites = useFavorites(currentVal);

    						$scope.updateDropdownValuesFavorites();

    						$scope.computeMinimumSearchResults = function() {
    							return ($scope.dropdownValues.length > 0) ? 20 : -1;
    						};

    						$scope.dropdownOptions = {
    							data: function() {
    								return {results: $scope.dropdownValues};
    							},
    							formatResult: function(value) {
    								// TODO: add code that can handle display of methods
    								return value.description;
    							},
    							formatSelection: function(value) {
    								// TODO: add code that can handle display of methods
    								return value.description;
    							},
    							minimumResultsForSearch: $scope.computeMinimumSearchResults(),
    							query: function(query) {
    								var data = {
    									results: $scope.dropdownValues
    								};

    								// return the array that matches
    								data.results = $.grep(data.results, function(item) {
    									return ($.fn.select2.defaults.matcher(query.term,
    										item.name));

    								});

    								query.callback(data);
    							}

    						};

    						if ($scope.valuecontainer[$scope.targetkey]) {
    							$scope.dropdownOptions.initSelection = function(element, callback) {
    								angular.forEach($scope.dropdownValues, function(value) {
    									var idNumber;

    									if (!isNaN($scope.valuecontainer[$scope.targetkey])) {
    										idNumber = parseInt($scope.valuecontainer[$scope.targetkey]);
    									}

    									if (value.description === $scope.valuecontainer[$scope.targetkey] ||
    										value.id === idNumber) {
    										callback(value);
    										return false;
    									}
    								});
    							};
    						}
    					}


    					// TODO: add code that can handle display of favorite methods, as well as update of possible values in case of click of manage methods
    					if ($scope.isLocation) {
    						$scope.clearArray = function(targetArray) {
    							// current internet research suggests that this is the fastest way of clearing an array
    							while (targetArray.length > 0) {
    								targetArray.pop();
    							}
    						};

    						$scope.updateLocationValues = function() {
    								$http.get('/Fieldbook/locations/getLocations').then(function(returnVal) {
    									if (returnVal.data.success === '1') {
    										$scope.variableDefinition.locationUpdated = true;
    										// clear and copy of array is performed so as to preserve previous reference
    										// and have changes applied to all components with a copy of the previous reference
    										$scope.clearArray($scope.variableDefinition.possibleValues);
    										$scope.clearArray($scope.variableDefinition.possibleValuesFavorite);
											$scope.clearArray($scope.variableDefinition.allFavoriteValues);
											$scope.clearArray($scope.variableDefinition.allValues);
    										

    										$scope.variableDefinition.possibleValues.push.apply($scope.variableDefinition.possibleValues,
    											$scope.convertLocationsToPossibleValues(returnVal.data.allBreedingLocations));
    										$scope.variableDefinition.possibleValuesFavorite.push.apply(
    											$scope.variableDefinition.possibleValuesFavorite,
    											$scope.convertLocationsToPossibleValues(returnVal.data.allBreedingFavoritesLocations));
    										$scope.variableDefinition.allFavoriteValues.push.apply(
    											$scope.variableDefinition.allFavoriteValues,
    											$scope.convertLocationsToPossibleValues(returnVal.data.favoriteLocations));	
    										$scope.variableDefinition.allValues.push.apply(
    											$scope.variableDefinition.allValues,
    											$scope.convertLocationsToPossibleValues(returnVal.data.allLocations));													
    										$scope.updateDropdownValuesFavorites();
    									}
    								});
    						};

    						$scope.convertLocationsToPossibleValues = function(locations) {
    							var possibleValues = [];

    							$.each(locations, function(key, value) {
    								var locNameDisplay = value.lname;
    								if (value.labbr != null && value.labbr != '') {
    									locNameDisplay  += ' - (' + value.labbr + ')';
    								}

    								possibleValues.push({
    									id: value.locid,
    									name: locNameDisplay,
    									description: value.lname
    								});
    							});

    							return possibleValues;
    						};

    						$(document).off('location-update');
    						$(document).on('location-update', $scope.updateLocationValues);
    					}
    				}
    			};
    		}])
}());
