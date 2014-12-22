/**
 * Created by cyrus on 7/2/14.
 */

/* global angular */
(function () {
    'use strict';

    angular.module('manageTrialApp').controller('MeasurementsCtrl',
        ['$scope', 'TrialManagerDataService', '$modal', '$q','debounce','$http',
            function ($scope, TrialManagerDataService, $modal, $q,debounce,$http) {

                $scope.settings = TrialManagerDataService.settings.measurements;

                $scope.$watch(function () {
                    return TrialManagerDataService.settings.measurements;
                }, function (newValue) {
                    if ($scope.settings !== newValue) {
                        angular.copy(newValue, $scope.settings);
                    }
                });
                
                $scope.deletedEnvironment = 0;
                
				$scope.$watch(function(){
					return TrialManagerDataService.deletedEnvironment;
				}, function(newValue){
					if(newValue !== 0){
						$scope.deletedEnvironment = TrialManagerDataService.deletedEnvironment;
						if ($('#measurement-table').length !== 0) {
							if(TrialManagerDataService.trialMeasurement.hasMeasurement){
								$scope.reloadMeasurementPage();
								TrialManagerDataService.clearUnappliedChangesFlag();
                                TrialManagerDataService.applicationData.unsavedGeneratedDesign = true;
							} else {
								TrialManagerDataService.refreshMeasurementTableAfterDeletingEnvironment();
							}
						}
						
						TrialManagerDataService.deletedEnvironment = 0;
						$scope.updateOccurred = true;
					}
				});
				
                $scope.beforeDelete = function(variableType,variableIds) {
                    var deferred = $q.defer();

                    $http.post('/Fieldbook/manageSettings/hasMeasurementData/' + variableType,variableIds,{cache: false})
                        .success(function(data, status, headers, config) {
                            if ('true' === data) {
                                var modalInstance = $modal.open({
                                    templateUrl: '/Fieldbook/static/angular-templates/confirmModal.html',
                                    controller: 'ConfirmModalController',
                                    resolve: {
                                        MODAL_TITLE: function () {
                                            return modalConfirmationTitle;
                                        },
                                        MODAL_TEXT: function () {
                                            return measurementModalConfirmationText;
                                        },
                                        CONFIRM_BUTTON_LABEL: function () {
                                            return environmentConfirmLabel;
                                        }
                                    }
                                });

                                modalInstance.result.then(deferred.resolve);

                            } else {
                                deferred.resolve(true);
                            }
                        });

                    return deferred.promise;
                };

                $scope.isHideDelete = false;

                $scope.updateOccurred = false;

                $scope.addVariable = true;

                $scope.$on('deleteOccurred', function () {
                    $scope.updateOccurred = true;

                    $scope.reloadOnDebounce();

                    TrialManagerDataService.applicationData.unsavedTraitsAvailable = true;
                });

                $scope.$on('variableAdded', function () {
                    $scope.updateOccurred = true;

                    $scope.reloadOnDebounce();

                    TrialManagerDataService.applicationData.unsavedTraitsAvailable = true;
                });

                $scope.reloadMeasurementPage = function () {

                    if ($('#measurement-table').length !== 0) {
                        //we reload
                        $.ajax({
                            url: '/Fieldbook/TrialManager/openTrial/load/dynamic/change/measurement',
                            type: 'POST',
                            data: 'traitsList=' + TrialManagerDataService.settings.measurements.m_keys + "&deletedEnvironment="+$scope.deletedEnvironment,
                            cache: false,
                            success: function (html) {
                                $('#measurementsDiv').html(html);
                                $('body').data('needToSave', '1');
                            }
                        });
                    }
                };

                var DELAY = 1500; // 1.5 secs
                $scope.reloadOnDebounce = debounce($scope.reloadMeasurementPage,DELAY,false);

            }]);
})();