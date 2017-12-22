/*global angular*/
/*global showBaselineTraitDetailsModal */
/* global openManageLocations*/
(function() {
	'use strict';

	angular.OrderedHash = (function() {
		function OrderedHash() {
			this.m_keys = [];
			this.m_vals = {};
		}

		OrderedHash.prototype.addList = function(list, keyExtract) {
			if (list) {
				for (var i = 0; i < list.length; i++) {
					var _key = !isNaN(keyExtract(list[i])) ? Number(keyExtract(list[i])) : keyExtract(list[i]);

					this.m_keys.push(_key);
					this.m_vals[_key] = list[i];
				}
			}
		};

		OrderedHash.prototype.push = function(k, v) {
			var _key = !isNaN(k) ? Number(k) : k;
			if (!this.m_vals[k]) {
				this.m_keys.push(_key);
			}
			this.m_vals[_key] = v;
			return v;
		};

		OrderedHash.prototype.length = function() {
			return this.m_keys.length;
		};

		OrderedHash.prototype.keys = function() {
			return this.m_keys;
		};

		OrderedHash.prototype.val = function(k) {
			var _key = !isNaN(k) ? Number(k) : k;

			return this.m_vals[_key];
		};

		OrderedHash.prototype.vals = function() {
			return this.m_vals;
		};

		OrderedHash.prototype.remove = function(key) {
			var _key = !isNaN(key) ? Number(key) : key;

			this.m_keys.splice(this.m_keys.indexOf(_key), 1);
			delete this.m_vals[_key];

		};

		OrderedHash.prototype.removeAll = function() {
			while (this.m_keys.length > 0) {
				var _key = this.m_keys.pop();
				delete this.m_vals[_key];
			}
		};

		return OrderedHash;

	})();

	angular.module('fieldbook-utils', ['ui.select2'])
		.constant('VARIABLE_SELECTION_MODAL_SELECTOR', '.vs-modal')
		.constant('VARIABLE_SELECTED_EVENT_TYPE', 'variable-select')
		.directive('displaySettings', ['TrialManagerDataService', '$filter', '_', function(TrialManagerDataService, $filter, _) {
			return {
				restrict: 'E',
				scope: {
					settings: '=',
					hideDelete: '=',
					predeleteFunction: '&'
				},
				templateUrl: '/Fieldbook/static/angular-templates/displaySettings.html',
				controller: function($scope, $element, $attrs) {
					$scope.variableType = $attrs.variableType;
					$scope.options = {
						selectAll: false
					};

					// when the selectAll checkbox is clicked, do this
					$scope.doSelectAll = function() {
						var filteredVariables = $filter('removeHiddenAndDeletablesVariableFilter')($scope.settings.keys(), $scope.settings.vals());

						_.each(filteredVariables, function(cvTermID) {
							$scope.settings.val(cvTermID).isChecked = $scope.options.selectAll;
						});

					};

					// when the delete button is clicked do this
					$scope.removeSettings = function() {

						if (typeof $scope.predeleteFunction() === 'undefined') {
							$scope.doDeleteSelectedSettings();
						} else {
							var checkedVariableTermIds = $scope.retrieveCheckedVariableTermIds($scope.settings);
							var promise = $scope.predeleteFunction()($attrs.variableType, checkedVariableTermIds);
							promise.then(function(doContinue) {
								if (doContinue) {
									$scope.doDeleteSelectedSettings();
								}
							});
						}
						//Redraw the environments table to make sure that it's properly displayed
						if ($('.fbk-datatable-environments').length !== 0 && $('.fbk-datatable-environments').DataTable() !== null) {
							setTimeout(function() {
								$('.fbk-datatable-environments').DataTable().columns.adjust().draw();
							}, 1000);
						}
					};

					$scope.retrieveCheckedVariableTermIds = function(_settings) {
						var checkedCvtermIds = _.pairs(_settings.vals())
							.filter(function(val) {
								return _.last(val).isChecked;
							})
							.map(function(val) {
								return parseInt(_.first(val));
							});
						return checkedCvtermIds;
					};

					$scope.doDeleteSelectedSettings = function() {
						TrialManagerDataService.removeSettings($attrs.variableType, $scope.settings).then(function(data) {
							if (data.length > 0) {
								$scope.$emit('deleteOccurred');
							}

							$scope.options.selectAll = false;
						});

					};

					$scope.size = function() {
						return Object.keys($scope.settings).length;
					};
				}
			};
		}])
		.directive('validNumber', function() {

			return {
				require: '?ngModel',
				link: function(scope, element, attrs, ngModelCtrl) {
					if (!ngModelCtrl) {
						return;
					}

					ngModelCtrl.$parsers.push(function(val) {
						var clean = val.replace(/[^0-9]+/g, '');
						if (val !== clean) {
							ngModelCtrl.$setViewValue(clean);
							ngModelCtrl.$render();
						}
						return clean;
					});

					element.bind('keypress', function(event) {
						if (event.keyCode === 32) {
							event.preventDefault();
						}
					});
				}
			};
		})
		.directive('validDecimal', function() {

			return {
				require: '?ngModel',
				link: function(scope, element, attrs, ngModelCtrl) {
					if (!ngModelCtrl) {
						return;
					}

					ngModelCtrl.$parsers.push(function(val) {
						var clean = val.replace(/[^0-9.]+/g, '');
						if (val !== clean) {
							ngModelCtrl.$setViewValue(clean);
							ngModelCtrl.$render();
						}
						return clean;
					});

					element.bind('keypress', function(event) {
						if (event.keyCode === 32) {
							event.preventDefault();
						}
					});
				}
			};
		})
		.directive('selectStandardVariable', ['VARIABLE_SELECTION_MODAL_SELECTOR', 'VARIABLE_SELECTED_EVENT_TYPE', 'TrialSettingsManager', 'TrialManagerDataService',
			function(VARIABLE_SELECTION_MODAL_SELECTOR, VARIABLE_SELECTED_EVENT_TYPE, TrialSettingsManager, TrialManagerDataService) {
				return {
					restrict: 'A',
					scope: {
						modeldata: '=',
						callback: '&'
					},

					link: function(scope, elem, attrs) {

						scope.processData = function(data) {
							scope.$apply(function() {
								var out = {};

								if (data.responseData) {
									data = data.responseData;
								}
								if (data) {

									// if retrieved data is an array of values
									if (data.length && data.length > 0) {
										$.each(data, function(key, value) {
											scope.modeldata.push(value.variable.cvTermId, TrialManagerDataService.transformViewSettingsVariable(value));
											out[value.variable.cvTermId] = value;
											scope.callback({ result: out });

										});
									} else {
										// if retrieved data is a single object
										scope.modeldata.push(data.variable.cvTermId, data);
									}

								}

								scope.$emit('variableAdded', out);
							});
						};

						elem.on('click', function() {

							var params = {
								variableType: attrs.variableType,
								retrieveSelectedVariableFunction: function() {
									var allSettings = TrialManagerDataService.getSettingsArray();
									var selected = {};

									angular.forEach(allSettings, function(tabSettings) {
										angular.forEach(tabSettings.vals(), function(value) {
											selected[value.variable.cvTermId] = value.variable.name;
										});
									});

									return selected;
								}
							};

							$(VARIABLE_SELECTION_MODAL_SELECTOR).off(VARIABLE_SELECTED_EVENT_TYPE);
							$(VARIABLE_SELECTION_MODAL_SELECTOR).on(VARIABLE_SELECTED_EVENT_TYPE, scope.processData);

							TrialSettingsManager.openVariableSelectionDialog(params);
						});
					}
				};
			}])
		.directive('showSettingFormElement', ['_', function(_) {
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

				templateUrl: '/Fieldbook/static/angular-templates/showSettingFormElement.html',
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

                    $scope.isBreedingMethod = parseInt(BREEDING_METHOD_ID, 10) === parseInt($scope.variableDefinition.variable.cvTermId, 10) ||
						parseInt(BREEDING_METHOD_CODE, 10) === parseInt($scope.variableDefinition.variable.cvTermId, 10);

					$scope.localData = {};
					var showAll = $scope.valuecontainer[$scope.targetkey];
					$scope.localData.useFavorites = !showAll;
					$scope.lookupLocation =  showAll ? 2 : 1;
					
					$scope.updateDropdownValuesFavorites = function() {
						if ($scope.localData.useFavorites) {
							if ($scope.lookupLocation == 1) {
								$scope.dropdownValues = $scope.variableDefinition.possibleValuesFavorite;
							} else {
								$scope.dropdownValues = $scope.variableDefinition.allFavoriteValues;
							}
						} else {
							if ($scope.lookupLocation == 1) {
								$scope.dropdownValues = $scope.variableDefinition.possibleValues;
							} else {
								$scope.dropdownValues = $scope.variableDefinition.allValues;
							}
						}
					};
			
					$scope.updateDropdownValuesBreedingLocation = function() { // Change state for breeding
						$scope.dropdownValues = ($scope.localData.useFavorites) ? $scope.variableDefinition.possibleValuesFavorite : $scope.variableDefinition.possibleValues;
						$scope.lookupLocation = 1;
					};
					
					$scope.updateDropdownValuesAllLocation = function() { // Change state for all locations radio
						$scope.dropdownValues = ($scope.localData.useFavorites) ? $scope.variableDefinition.allFavoriteValues : $scope.variableDefinition.allValues;
						$scope.lookupLocation = 2;
					};

					// if the value of the dropdown from existing data matches from the list of favorites, we set the checkbox as true
					var useFavorites = function(currentVal) {

						if (currentVal) {
							return false;
						} else if ($scope.variableDefinition.possibleValuesFavorite) {
							return $scope.variableDefinition.possibleValuesFavorite.length > 0;
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
							if($scope.dropdownValues != null)
								return ($scope.dropdownValues.length > 0) ? 20 : -1;
							return -1;
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
										if ($scope.isLocation){
											selectedLocation(value, $scope.dropdownValues);
										}
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
							if (!$scope.variableDefinition.locationUpdated) {
								$http
									.get('/Fieldbook/locations/getLocations')
									.then(
										function(returnVal) {
											if (returnVal.data.success === '1') {
												$scope.variableDefinition.locationUpdated = true;
												// clear and copy of array is performed so as to preserve previous reference
												// and have changes applied to all components with a copy of the previous
												// reference
												$scope.clearArray($scope.variableDefinition.allValues);
												$scope.clearArray($scope.variableDefinition.possibleValues);
												$scope.clearArray($scope.variableDefinition.possibleValuesFavorite);
												$scope.clearArray($scope.variableDefinition.allFavoriteValues);
	
												$scope.variableDefinition.allValues.push.apply(
													$scope.variableDefinition.allValues, $scope
														.convertLocationsToPossibleValues(returnVal.data.allLocations));
												$scope.variableDefinition.possibleValues.push.apply(
													$scope.variableDefinition.possibleValues, $scope
														.convertLocationsToPossibleValues(returnVal.data.allBreedingLocations));
												$scope.variableDefinition.allFavoriteValues.push.apply(
													$scope.variableDefinition.allFavoriteValues, $scope
														.convertLocationsToPossibleValues(returnVal.data.favoriteLocations));
												$scope.variableDefinition.possibleValuesFavorite.push
													.apply(
														$scope.variableDefinition.possibleValuesFavorite,
														$scope
															.convertLocationsToPossibleValues(returnVal.data.allBreedingFavoritesLocations));
												$scope.updateDropdownValuesFavorites();
											}
										});
	
							}
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

						$scope.initiateManageLocationModal = function() {
							$scope.variableDefinition.locationUpdated = false;
							openManageLocations();
						};

						$(document).off('location-update');
						$(document).on('location-update', $scope.updateLocationValues);
					}
				}
			};
		}])

		.directive('jqDatepicker', function() {
			return function(scope, element) {
				$(element).placeholder();
			};
		})

		.directive('truncateAndTooltip', ['$compile', function($compile) {
			return {
				restrict: 'A',
				link: function(scope, element, attrs) {
					var length = 30;
					scope.$watch(attrs.truncateAndTooltip, function(newValue) {
						if (newValue.length > length) {
							element.attr('tooltip', newValue);
							element.attr('tooltip-placement', 'right');
							element.attr('tooltip-append-to-body', true);
							element.html(newValue.substring(0, length) + '...');

						} else {
							element.html(newValue);
						}

						// remove truncateAndTooltip attr so no infinite loop
						element.removeAttr('truncate-and-tooltip');

						$compile(element)(scope);
					});
				}
			};
		}])

		.directive('inputType', function() {
			return {
				require: 'ngModel',
				link: function(scope, elem, attrs, ctrl) {
					// Custom number validation logic.
					if (attrs.inputType === 'number') {
						elem.attr('type', 'text');

						return ctrl.$parsers.push(function(value) {
							var valid = value === null || isFinite(value);

							ctrl.$setValidity('number', valid);

							return valid && value !== null ? Number(value) : undefined;
						});
					}

					// Fallback to setting the default `type` attribute.
					return elem.attr('type', attrs.inputType);
				}
			};
		})

		.directive('minVal', function() {
			return {
				require: 'ngModel',
				link: function(scope, elem, attrs, ctrl) {
					return ctrl.$parsers.push(function(value) {
						var valid = value === null || Number(value) >= Number(attrs.minVal);

						ctrl.$setValidity('min', valid);

						return valid ? value : undefined;
					});
				}
			};
		})

		.directive('maxVal', function() {
			return {
				require: 'ngModel',
				link: function(scope, elem, attrs, ctrl) {
					return ctrl.$parsers.push(function(value) {
						var valid = value === null || Number(value) <= Number(attrs.maxVal);

						ctrl.$setValidity('max', valid);

						return valid ? value : undefined;
					});
				}
			};
		})

		// filters
		.filter('range', function() {
			return function(input, total) {
				total = parseInt(total);
				for (var i = 0; i < total; i++) {
					input.push(i);
				}

				return input;
			};
		})

		.filter('removeHiddenVariableFilter', function() {
			return function(settingKeys, settingVals) {
				var keys = [];

				angular.forEach(settingKeys, function(val) {
					if (!settingVals[val].hidden) {
						keys.push(val);
					}
				});

				return keys;
			};
		})

		.filter('removeHiddenAndDeletablesVariableFilter', function() {
			return function(settingKeys, settingVals) {
				var keys = [];

				angular.forEach(settingKeys, function(val) {
					if (!settingVals[val].hidden && settingVals[val].deletable) {
						keys.push(val);
					}
				});

				return keys;
			};
		});

}
)();
