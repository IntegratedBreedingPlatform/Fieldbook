
/*global angular, modalConfirmationTitle, openManageLocations,
environmentModalConfirmationText, environmentConfirmLabel, showAlertMessage, showErrorMessage*/

(function() {
	'use strict';

	angular.module('manageTrialApp').controller('EnvironmentCtrl', ['$scope', 'TrialManagerDataService', '$uibModal', '$stateParams',
	'$http', 'DTOptionsBuilder', 'LOCATION_ID', '$timeout', 'environmentService','$rootScope',
		function($scope, TrialManagerDataService, $uibModal, $stateParams, $http, DTOptionsBuilder, LOCATION_ID, $timeout, environmentService, $rootScope) {

			// preload the measurements tab, if the measurements tab is not yet loaded 
			// to make sure deleting environments will still works
		    // since environments are directly correlated to their measurement rows
			// NOTE: $rootScope.stateSuccessfullyLoaded will only have value once the specific tab is successfully loaded
		    if( $rootScope.stateSuccessfullyLoaded['createMeasurements'] === undefined 
		    		&& $rootScope.stateSuccessfullyLoaded['editMeasurements'] === undefined){
				$scope.loadMeasurementsTabInBackground();
			}	

			// at least one environment should be in the datatable, so we are prepopulating the table with the first environment
			var populateDatatableWithDefaultValues = function() {
				$scope.data = TrialManagerDataService.currentData.environments;

				if (!$scope.data.environments) {
					$scope.data.environments = [];
				}
				if ($scope.data.environments.length === 0) {
					$scope.data.environments.push({});
				}
				if (!$scope.data.environments[0].managementDetailValues) {
					$scope.data.environments[0].managementDetailValues = {};
				}
				if (!$scope.data.environments[0].managementDetailValues[$scope.TRIAL_INSTANCE_NO_INDEX]) {
					$scope.data.environments[0].managementDetailValues[$scope.TRIAL_INSTANCE_NO_INDEX] = 1;
				}
			};

			$scope.TRIAL_INSTANCE_NO_INDEX = 8170;

			$scope.data = TrialManagerDataService.currentData.environments;
			$scope.nested = {};
			$scope.nested.dtInstance = {};
			$scope.isHideDelete = false;
			$scope.temp = {
				settingMap: {},
				noOfEnvironments: $scope.data.noOfEnvironments
			};

			$scope.settings = TrialManagerDataService.settings.environments;
			if (Object.keys($scope.settings).length === 0) {
				$scope.settings = {};
				$scope.settings.managementDetails = [];
				$scope.settings.trialConditionDetails = [];
			}

			$scope.ifLocationAddedToTheDataTable = function () {
				return $scope.settings.managementDetails.keys().indexOf(parseInt(LOCATION_ID)) > -1;
			};

			//the flag to determine if we have a location variable in the datatable
			$scope.isLocation = $scope.ifLocationAddedToTheDataTable();

			$scope.onLocationChange = function(data){
				environmentService.changeEnvironments(data);
			}

			$scope.buttonsTopWithLocation = [{
				//TODO disable?
				text: $.fieldbookMessages.nurseryManageSettingsManageLocation,
				className: 'fbk-buttons-no-border fbk-buttons-link',
				action: function() {
					$scope.initiateManageLocationModal();
				}
			},
			{
				extend:'colvis',
				className: 'fbk-buttons-no-border fbk-colvis-button',
				text:'<i class="glyphicon glyphicon-th dropdown-toggle fbk-show-hide-grid-column"></i>',
				columns: ':gt(0):not(.ng-hide)'
			}];

			$scope.buttonsTop = [{
				extend:'colvis',
				className: 'fbk-buttons-no-border fbk-colvis-button',
				text:'<i class="glyphicon glyphicon-th dropdown-toggle fbk-show-hide-grid-column"></i>',
				columns: ':gt(0):not(.ng-hide)'
			}];

			$scope.dtOptions = DTOptionsBuilder.newOptions().withDOM('<"fbk-datatable-panel-top"liB>rtp')
				.withButtons($scope.isLocation ? $scope.buttonsTopWithLocation.slice() : $scope.buttonsTop.slice())
				.withOption('scrollX', true)
				.withOption('scrollCollapse', true)
				.withOption('deferRender', true);

			$scope.dtOptions.drawCallback =  function() {
				var api = $(this).DataTable();

				//temporary fix in buttons disappear bug,
				//see https://github.com/l-lin/angular-datatables/issues/502#issuecomment-161166246
				if (api) {
					// remove old set of buttons before recreating them
					if (api.buttons()) {
						api.buttons().remove();
					}
					new $.fn.dataTable.Buttons(api, {
						buttons: $scope.isLocation ? $scope.buttonsTopWithLocation.slice() : $scope.buttonsTop.slice()
					});

					$(this).parents('.dataTables_wrapper').find('.dt-buttons').replaceWith(api.buttons().container());
				}
			};

			$scope.onAddVariable = function() {
				$scope.nested.dtInstance.rerender();
				// update the location flag, as it could have been added
				$scope.isLocation = $scope.ifLocationAddedToTheDataTable();
			};

			$scope.$on('deleteOccurred', function() {
				$scope.nested.dtInstance.rerender();
				// update the location flag, as it could have been deleted
				$scope.isLocation = $scope.ifLocationAddedToTheDataTable();
			});
			
			$scope.$on('rerenderEnvironmentTable', function(event, args) {
				$scope.nested.dtInstance.rerender();
			});

			$scope.initiateManageLocationModal = function() {
				//TODO $scope.variableDefinition.locationUpdated = false;
				openManageLocations();
			};

			//prepopulate the datatable
			populateDatatableWithDefaultValues();

			TrialManagerDataService.onUpdateData('environments', function() {
				$scope.temp.noOfEnvironments = $scope.data.noOfEnvironments;
			});

			/* Scope Functions */
			$scope.shouldDisableEnvironmentCountUpdate = function() {
				return TrialManagerDataService.trialMeasurement.hasMeasurement;
			};

			$scope.getModalInstance = function() {
				return $uibModal.open({
					templateUrl: '/Fieldbook/static/angular-templates/confirmModal.html',
					controller: 'ConfirmModalController',
					resolve: {
						MODAL_TITLE: function() {
							return modalConfirmationTitle;
						},
						MODAL_TEXT: function() {
							return environmentModalConfirmationText;
						},
						CONFIRM_BUTTON_LABEL: function() {
							return environmentConfirmLabel;
						}
					}
				});
			};

			$scope.updateEnvironmentCount = function() {
				if ($scope.temp.noOfEnvironments > $scope.data.environments.length) {
					$scope.data.noOfEnvironments = $scope.temp.noOfEnvironments;
				} else if ($scope.temp.noOfEnvironments < $scope.data.environments.length) {
					var modalInstance = $scope.getModalInstance();
					modalInstance.result.then(function(shouldContinue) {
						if (shouldContinue) {
							$scope.data.noOfEnvironments = $scope.temp.noOfEnvironments;
						}
					});
				}
			};

			$scope.deleteEnvironment = function(index) {
				if (!TrialManagerDataService.isOpenTrial()) {
					// For New Trial
					confirmDeleteEnvironment(index);

				} else {
					// For Existing Trial
					hasMeasurementDataOnEnvironment(index);
				}
			};

			$scope.updateTrialInstanceNo = function(environments, index) {
				for (var i = 0; i <  environments.length; i++) {
					var environment = environments[i];
					var trialInstanceNo = environment.managementDetailValues[$scope.TRIAL_INSTANCE_NO_INDEX];
					if (trialInstanceNo > index) {
						trialInstanceNo -= 1;
						environment.managementDetailValues[$scope.TRIAL_INSTANCE_NO_INDEX] = trialInstanceNo;
					}
				}
			};

			$scope.addVariable = true;
			$scope.findSetting = function(targetKey, type) {
				if (! $scope.temp.settingMap[targetKey]) {
					var targetSettingList = null;

					if (type === 'managementDetails') {
						targetSettingList = $scope.settings.managementDetails;
					} else if (type === 'trialConditionDetails') {
						targetSettingList = $scope.settings.trialConditionDetails;
					}

					$.each(targetSettingList, function(key, value) {
						if (value.variable.cvTermId === targetKey) {
							$scope.temp.settingMap[targetKey] = value;
							return false;
						}
					});
				}

				return $scope.temp.settingMap[targetKey];
			};

			/* Watchers */
			$scope.$watch('data.noOfEnvironments', function(newVal, oldVal) {
				$scope.temp.noOfEnvironments = newVal;
				if (Number(newVal) < Number(oldVal)) {
					// if new environment count is less than previous value, splice array
					while ($scope.data.environments.length > newVal) {
						$scope.data.environments.pop();
					}

					// Regenerate experimental design and measurement table when the trial is not saved yet
					if (!TrialManagerDataService.isOpenTrial() && TrialManagerDataService.currentData.experimentalDesign.noOfEnvironments !== undefined) {
						refreshMeasurementTableAfterDeletingEnvironment();
					}

					TrialManagerDataService.applicationData.hasNewEnvironmentAdded = false;
				} else if (Number(newVal) > Number(oldVal)) {
					addNewEnvironments(newVal - oldVal);
					TrialManagerDataService.applicationData.hasNewEnvironmentAdded = true;
				}
			});

			$scope.$watch('settings.managementDetails', function(newVal, oldVal) {
				updateEnvironmentVariables('managementDetails', newVal.length > oldVal.length);
			}, true);

			$scope.$watch('settings.trialConditionDetails', function(newVal, oldVal) {
				updateEnvironmentVariables('trialConditionDetails', newVal.length > oldVal.length);
			}, true);

			/* Controller Utility functions */
			function confirmDeleteEnvironment(index) {
				// Existing Trial with measurement data
				var modalInstance = $scope.getModalInstance();
				modalInstance.result.then(function(shouldContinue) {
					if (shouldContinue) {
						updateDeletedEnvironment(index);
					}
				});
			}

			function hasMeasurementDataOnEnvironment(environmentNo) {
				var variableIds = TrialManagerDataService.settings.measurements.keys();
				var dfd = $.Deferred();
				$.ajax({
					url: '/Fieldbook/trial/measurements/instanceMetadata/' + $('#studyId').val(),
					success: function (data) {
						var envList;
						envList = data;
						if (envList[environmentNo] == undefined) {
							confirmDeleteEnvironment(environmentNo);
						}
						else {
							$http.post('/Fieldbook/manageSettings/hasMeasurementData/environmentNo/' +
								envList[environmentNo].instanceDbId, variableIds, {cache: false}).success(function (data) {
								if (true === data) {
									var warningMessage = 'This environment cannot be removed because it contains measurement data.';
									showAlertMessage('', warningMessage);
								} else {
									confirmDeleteEnvironment(environmentNo);
								}
								dfd.resolve();
							});
						}

					}
				});
				return dfd.promise();
			}

			// on click generate design button
			function refreshMeasurementTableAfterDeletingEnvironment() {
				$rootScope.$broadcast('previewMeasurements');
                $('body').addClass('preview-measurements-only');
				// Make sure that the measurement table will only refresh if there is a selected design type for the current trial
				var designTypeId = TrialManagerDataService.currentData.experimentalDesign.designType;
				var designTypes = TrialManagerDataService.applicationData.designTypes;
				if (designTypeId !== null && TrialManagerDataService.getDesignTypeById(designTypeId, designTypes).isPreset) {
					TrialManagerDataService.generatePresetExpDesign(designTypeId).then(function() {
						TrialManagerDataService.updateAfterGeneratingDesignSuccessfully();
						TrialManagerDataService.applicationData.hasGeneratedDesignPreset = true;
					});
				} else {
					var noOfEnvironments = TrialManagerDataService.currentData.environments.noOfEnvironments;
					var data = TrialManagerDataService.currentData.experimentalDesign;
					//update the no of environments in experimental design tab
					data.noOfEnvironments = noOfEnvironments;

					TrialManagerDataService.generateExpDesign(data).then(
						function(response) {
							if (response.valid === true) {
								TrialManagerDataService.clearUnappliedChangesFlag();
								TrialManagerDataService.applicationData.unsavedGeneratedDesign = true;
								$('#chooseGermplasmAndChecks').data('replace', '1');
							} else {
								showErrorMessage('', response.message);
								$body.removeClass('preview-measurements-only');
							}
						}, function(errResponse) {
                            showErrorMessage($.fieldbookMessages.errorServerError, $.fieldbookMessages.errorDesignGenerationFailed);
                            $body.removeClass('preview-measurements-only');
                        }
					);
				}
			}

			function addNewEnvironments(noOfEnvironments, displayWarningMessage) {
				for (var ctr = 0; ctr < noOfEnvironments; ctr++) {
					$scope.data.environments.push({
						managementDetailValues: TrialManagerDataService.constructDataStructureFromDetails(
							$scope.settings.managementDetails),
						trialDetailValues: TrialManagerDataService.constructDataStructureFromDetails($scope.settings.trialConditionDetails)
					});
				}
				// we need to assign the TrialInstanceNumber and set it equal to index when new environments were added to the list
				for (var i = 0; i <  $scope.data.environments.length; i++) {
					var environment = $scope.data.environments[i];
					if (!environment.managementDetailValues[$scope.TRIAL_INSTANCE_NO_INDEX]) {
						environment.managementDetailValues[$scope.TRIAL_INSTANCE_NO_INDEX] = i + 1;
					}
				}
				TrialManagerDataService.indicateUnappliedChangesAvailable(displayWarningMessage);
			}

			function updateEnvironmentVariables(type, entriesIncreased) {

				var settingDetailSource = null;
				var targetKey = null;

				if (type === 'managementDetails') {
					settingDetailSource = $scope.settings.managementDetails;
					targetKey = 'managementDetailValues';
				} else if (type === 'trialConditionDetails') {
					settingDetailSource = $scope.settings.trialConditionDetails;
					targetKey = 'trialDetailValues';
				}

				$.each($scope.data.environments, function(key, value) {
					var subList = value[targetKey];

					if (entriesIncreased) {
						$.each(settingDetailSource.keys(), function(key, value) {
							if (subList[value] === undefined) {
								subList[value] = null;
							}
						});
					} else {
						$.each(subList, function(idKey) {
							if (!settingDetailSource.vals().hasOwnProperty(idKey)) {
								delete subList[idKey];
							}
						});
					}
				});
			}

			function updateDeletedEnvironment(index) {
				// remove 1 environment
				$scope.temp.noOfEnvironments -= 1;
				$scope.data.environments.splice(index, 1);
				$scope.updateTrialInstanceNo($scope.data.environments, index);
				$scope.data.noOfEnvironments -= 1;

				//update the no of environments in experimental design tab
				if (TrialManagerDataService.currentData.experimentalDesign.noOfEnvironments !== undefined) {
					TrialManagerDataService.currentData.experimentalDesign.noOfEnvironments = $scope.temp.noOfEnvironments;
				}

				TrialManagerDataService.deleteEnvironment(index + 1);
			}

			// init
			if ($stateParams && $stateParams.addtlNumOfEnvironments && !isNaN(parseInt($stateParams.addtlNumOfEnvironments))) {
				var addtlNumOfEnvironments = parseInt($stateParams.addtlNumOfEnvironments, 10);
				$scope.temp.noOfEnvironments = parseInt($scope.temp.noOfEnvironments, 10) + addtlNumOfEnvironments;
				$scope.data.noOfEnvironments = $scope.temp.noOfEnvironments;
				addNewEnvironments(addtlNumOfEnvironments, 'true');
			}
		}]).factory('DTLoadingTemplate', function() {
			return {
				html: '<span class="throbber throbber-2x"></span>'
			};
		});
})();
