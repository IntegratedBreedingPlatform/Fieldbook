/*global angular*/
/*global showBaselineTraitDetailsModal */
/* global openManageLocations*/
(function () {
    'use strict';

    angular.OrderedHash = (function () {
        function OrderedHash() {
            this.m_keys = [];
            this.m_vals = {};
        }

        OrderedHash.prototype.addList = function (list, keyExtract) {
            if (list) {
                for (var i = 0; i < list.length; i++) {
                    var _key = !isNaN(keyExtract(list[i])) ? Number(keyExtract(list[i])) : keyExtract(list[i]);

                    this.m_keys.push(_key);
                    this.m_vals[_key] = list[i];
                }
            }
        };


        OrderedHash.prototype.push = function (k, v) {
            var _key = !isNaN(k) ? Number(k) : k;
            if (!this.m_vals[k]) {
                this.m_keys.push(_key);
            }
            this.m_vals[_key] = v;
            return v;
        };

        OrderedHash.prototype.length = function () {
            return this.m_keys.length;
        };

        OrderedHash.prototype.keys = function () {
            return this.m_keys;
        };

        OrderedHash.prototype.val = function (k) {
            var _key = !isNaN(k) ? Number(k) : k;

            return this.m_vals[_key];
        };

        OrderedHash.prototype.vals = function () {
            return this.m_vals;
        };

        OrderedHash.prototype.remove = function (key) {
            var _key = !isNaN(key) ? Number(key) : key;

            this.m_keys.splice(this.m_keys.indexOf(_key), 1);
            delete this.m_vals[_key];

        };

        return OrderedHash;

    })();

    angular.module('fieldbook-utils', ['ui.select2'])
        .constant('VARIABLE_SELECTION_MODAL_SELECTOR', '.vs-modal')
        .constant('VARIABLE_SELECTED_EVENT_TYPE', 'variable-select')
        .directive('displaySettings', function () {
            return {
                restrict: 'E',
                scope: {
                    settings: '=',
                    hideDelete: '=',
                    predeleteFunction: '&'
                },
                templateUrl: '/Fieldbook/static/angular-templates/displaySettings.html',
                controller: function ($scope, $element, $attrs) {
                    $scope.removeSetting = function (key) {

                        var promise = $scope.predeleteFunction({
                            variableType: $attrs.variableType,
                            key: key});

                        if (promise !== undefined) {
                            promise.then(function (shouldContinue) {
                                if (shouldContinue) {
                                    $scope.performDelete(key);
                                }
                            });
                        } else {
                            $scope.performDelete(key);
                        }
                    };

                    $scope.performDelete = function (key) {
                        $scope.settings.remove(key);
                        $.ajax({
                            url: '/Fieldbook/manageSettings/deleteVariable/' + $attrs.variableType + '/' + key,
                            type: 'POST',
                            cache: false,
                            data: '',
                            contentType: 'application/json',
                            success: function () {
                            }
                        });

                        $scope.$emit('deleteOccurred');
                    };

                    $scope.showDetailsModal = function (setting) {
                        // this function is currently defined in the fieldbook-common.js, loaded globally for the page
                        // TODO : move away from global function definitions
                        showBaselineTraitDetailsModal(setting.variable.cvTermId);
                    };

                    $scope.size = function () {
                        return Object.keys($scope.settings).length;
                    };
                }
            };
        })
        .directive('showDetailsModal', function () {
            return {
                scope: {
                    showDetailsModal: '='
                },

                link: function (scope, elem, attrs) {
                    elem.css({ cursor: "pointer" });
                    elem.on('click', function () {
                        showBaselineTraitDetailsModal(scope.showDetailsModal);
                    });
                }
            };
        })
        .directive('validNumber', function () {

            return {
                require: '?ngModel',
                link: function (scope, element, attrs, ngModelCtrl) {
                    if (!ngModelCtrl) {
                        return;
                    }

                    ngModelCtrl.$parsers.push(function (val) {
                        var clean = val.replace(/[^0-9]+/g, '');
                        if (val !== clean) {
                            ngModelCtrl.$setViewValue(clean);
                            ngModelCtrl.$render();
                        }
                        return clean;
                    });

                    element.bind('keypress', function (event) {
                        if (event.keyCode === 32) {
                            event.preventDefault();
                        }
                    });
                }
            };
        })
        .directive('validDecimal', function () {

            return {
                require: '?ngModel',
                link: function (scope, element, attrs, ngModelCtrl) {
                    if (!ngModelCtrl) {
                        return;
                    }

                    ngModelCtrl.$parsers.push(function (val) {
                        var clean = val.replace(/[^0-9.]+/g, '');
                        if (val !== clean) {
                            ngModelCtrl.$setViewValue(clean);
                            ngModelCtrl.$render();
                        }
                        return clean;
                    });

                    element.bind('keypress', function (event) {
                        if (event.keyCode === 32) {
                            event.preventDefault();
                        }
                    });
                }
            };
        })
        .directive('selectStandardVariable', ['VARIABLE_SELECTION_MODAL_SELECTOR', 'VARIABLE_SELECTED_EVENT_TYPE', 'TrialSettingsManager', 'TrialManagerDataService',
            function (VARIABLE_SELECTION_MODAL_SELECTOR, VARIABLE_SELECTED_EVENT_TYPE, TrialSettingsManager, TrialManagerDataService) {
                return {
                    restrict: 'A',
                    scope: {
                        modeldata: '=',
                        callback: '&'
                    },

                    link: function (scope, elem, attrs) {

                        scope.processData = function (data) {
                            scope.$apply(function () {
                                if (data.responseData) {
                                    data = data.responseData;
                                }
                                if (data) {
                                    var out = {};
                                    // if retrieved data is an array of values
                                    if (data.length && data.length > 0) {
                                        $.each(data, function (key, value) {
                                            scope.modeldata.push(value.variable.cvTermId, value);

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

                        elem.on('click', function () {

                            var params = {
                                variableType: attrs.variableType,
                                retrieveSelectedVariableFunction: function () {
                                    var allSettings = TrialManagerDataService.getSettingsArray();
                                    var selected = {};

                                    angular.forEach(allSettings, function (tabSettings) {
                                        angular.forEach(tabSettings.vals(), function (value) {
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
        .directive('showSettingFormElement', function () {
            return {
                require: '?uiSelect2, ?ngModel',
                restrict: 'E',
                scope: {
                    settings: '=',
                    targetkey: '@targetkey',
                    settingkey: '@',
                    valuecontainer: '=',
                    changefunction: '&',
                    disabled: '='
                },

                templateUrl: '/Fieldbook/static/angular-templates/showSettingFormElement.html',
                compile: function (tElement, tAttrs, transclude, uiSelect2) {
                    if (uiSelect2) {
                        uiSelect2.compile(tElement, tAttrs);
                    }
                },
                controller: function ($scope, LOCATION_ID, BREEDING_METHOD_ID, BREEDING_METHOD_CODE, $http) {
                    if ($scope.settingkey === undefined) {
                        $scope.settingkey = $scope.targetkey;
                    }

                    if (!$scope.changefunction) {
                        $scope.changefunction = function () {
                        };
                    }

                    $scope.variableDefinition = $scope.settings.val($scope.settingkey);
                    $scope.widgetType = $scope.variableDefinition.variable.widgetType.$name ?
                        $scope.variableDefinition.variable.widgetType.$name : $scope.variableDefinition.variable.widgetType;
                    $scope.hasDropdownOptions = $scope.widgetType === 'DROPDOWN';


                    $scope.isLocation = $scope.variableDefinition.variable.cvTermId == LOCATION_ID;

                    $scope.isBreedingMethod = ($scope.variableDefinition.variable.cvTermId == BREEDING_METHOD_ID ||
                        $scope.variableDefinition.variable.cvTermId == BREEDING_METHOD_CODE);

                    $scope.localData = {};
                    $scope.localData.useFavorites = false;

                    $scope.updateDropdownValues = function () {
                        if ($scope.localData.useFavorites) {
                            $scope.dropdownValues = $scope.variableDefinition.possibleValuesFavorite;
                        } else {
                            $scope.dropdownValues = $scope.variableDefinition.possibleValues;
                        }
                    };

                    if ($scope.hasDropdownOptions) {
                        // TODO : add code that will recognize categorical variable dropdowns and change the displayed text accordingly
                        $scope.dropdownValues = $scope.variableDefinition.possibleValues;

                        $scope.computeMinimumSearchResults = function () {
                            return ($scope.dropdownValues.length > 0) ? 20 : -1;
                        };

                        $scope.dropdownOptions = {
                            data: function () {
                                return {results: $scope.dropdownValues};
                            },
                            formatResult: function (value) {
                                // TODO : add code that can handle display of methods
                                return value.description;
                            },
                            formatSelection: function (value) {
                                // TODO : add code that can handle display of methods
                                return value.description;
                            },
                            minimumResultsForSearch: $scope.computeMinimumSearchResults(),
                            query: function (query) {
                                var data = {
                                    results: $scope.dropdownValues
                                };

                                // return the array that matches
                                data.results = $.grep(data.results, function (item) {
                                    return ($.fn.select2.defaults.matcher(query.term,
                                        item.name));

                                });

                                query.callback(data);
                            }

                        };

                        if ($scope.valuecontainer[$scope.targetkey]) {
                            var currentVal = $scope.valuecontainer[$scope.targetkey];

                            // check if the value currently stored is an object
                            if (currentVal.id) {
                                $scope.valuecontainer[$scope.targetkey] = currentVal.id;
                            }

                            $scope.dropdownOptions.initSelection = function (element, callback) {
                                angular.forEach($scope.dropdownValues, function (value) {
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

                    // TODO : add code that can handle display of favorite methods, as well as update of possible values in case of click of manage methods
                    if ($scope.isLocation) {
                        $scope.clearArray = function (targetArray) {
                            // current internet research suggests that this is the fastest way of clearing an array
                            while (targetArray.length > 0) {
                                targetArray.pop();
                            }
                        };

                        $scope.updateLocationValues = function () {
                            if (!$scope.variableDefinition.locationUpdated) {
                                $http.get('/Fieldbook/NurseryManager/advance/nursery/getLocations').then(function (returnVal) {
                                    if (returnVal.data.success === '1') {
                                        $scope.variableDefinition.locationUpdated = true;
                                        // clear and copy of array is performed so as to preserve previous reference and have changes applied to all components with a copy of the previous reference
                                        $scope.clearArray($scope.variableDefinition.possibleValues);
                                        $scope.clearArray($scope.variableDefinition.possibleValuesFavorite);

                                        $scope.variableDefinition.possibleValues.push.apply($scope.variableDefinition.possibleValues,
                                            $scope.convertLocationsToPossibleValues($.parseJSON(returnVal.data.allBreedingLocations)));
                                        $scope.variableDefinition.possibleValuesFavorite.push.apply($scope.variableDefinition.possibleValuesFavorite,
                                            $scope.convertLocationsToPossibleValues($.parseJSON(returnVal.data.favoriteLocations)));
                                        $scope.updateDropdownValues();
                                    }
                                });

                            }
                        };

                        $scope.convertLocationsToPossibleValues = function (locations) {
                            var possibleValues = [];

                            $.each(locations, function (key, value) {
                                possibleValues.push({
                                    id: value.locid,
                                    name: value.lname,
                                    description: value.lname
                                });
                            });

                            return possibleValues;
                        };

                        $scope.initiateManageLocationModal = function () {
                            $scope.variableDefinition.locationUpdated = false;
                            openManageLocations();
                        };

                        $(document).off('location-update');
                        $(document).on('location-update', $scope.updateLocationValues);
                    }
                }
            };
        })

        .directive('sectionContainer', ['$parse', function ($parse) {
            return {
                restrict: 'E',
                scope: {
                    heading: '@',
                    reminder: '@',
                    helpTooltip: '@',
                    icon: '@',
                    iconImg: '@',
                    iconSize: '@',
                    modelData: '=',
                    variableType: '@',
                    showReminder: '=',
                    enableUpdate: '=',
                    onUpdate: '&',
                    callback: '&',
                    hideVariable: '=',
                    useExactProperties: '@',
                    collapsible: '='

                },
                transclude: true,
                templateUrl: '/Fieldbook/static/angular-templates/sectionContainer.html',
                link: function (scope, elem, attrs) {
                    scope.addVariable = $parse(attrs.addVariable)();


                    attrs.$observe('helpTooltip', function (value) {
                        if (value) {
                            scope.hasHelpTooltip = true;
                        }
                    });


                },
                controller: ['$scope', '$attrs', function ($scope, $attrs) {
                    $scope.toggleCollapse = false;
                    $scope.toggleSection = $attrs.startCollapsed && $attrs.startCollapsed === 'true';
                    $scope.doCollapse = function () {
                        if ($scope.collapsible) {
                            $scope.toggleSection = !$scope.toggleSection;
                        }
                    };

                    $scope.doClick = function () {
                        $scope.onUpdate({});
                    };


                    $scope.onAdd = function (result) {
                        $scope.callback({ result: result });
                    };
                }]

            };
        }])

        .directive('truncateAndTooltip', ['$compile', function ($compile) {
            return {
                restrict: 'A',
                link: function (scope, element, attrs) {
                    var length = 30;
                    scope.$watch(attrs.truncateAndTooltip, function (newValue, oldValue) {
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


        .directive('showBaselineTraitDetailsModalLink', function () {
            return {
                scope: {
                    showBaselineTraitDetailsModalLink: '@'
                },
                link: function (scope, elem, attrs) {
                    elem.click(function (e) {
                        showBaselineTraitDetailsModal(scope.cvTermId);
                    });
                }
            };
        })


        // filters
        .filter('range', function () {
            return function (input, total) {
                total = parseInt(total);
                for (var i = 0; i < total; i++) {
                    input.push(i);
                }

                return input;
            };
        })

        .filter('removeHiddenVariableFilter', function () {
            return function (settingKeys, settingVals) {
                var keys = [];

                angular.forEach(settingKeys, function (val, key) {
                    if (!settingVals[val].hidden) {
                        keys.push(val);
                    }
                });

                return keys;
            };
        })

        .filter('filterExperimentalDesignType', function(TrialManagerDataService) {
            return function(designTypes) {
                var result = [];
                if (TrialManagerDataService.settings.treatmentFactors.details.keys().length > 0) {
                    result.push(designTypes[0]);
                } else {
                    result = designTypes;
                }

                return result;
            };
        });

})();
