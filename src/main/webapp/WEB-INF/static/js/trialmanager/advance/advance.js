(function () {
	'use strict';

	const manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.factory('advanceStudyModalService', ['$uibModal', 'studyService', 'studyContext',
		function ($uibModal, studyService, studyContext) {

			var advanceStudyModalService = {};

			advanceStudyModalService.openSelectDatasetModal  = function () {
				$uibModal.open({
					template: '<dataset-option-modal modal-title="modalTitle" message="message"' +
						'selected="selected" on-continue="selectInstances()" supported-dataset-types="supportedDatasetTypes"></dataset-option-modal>',
					controller: 'AdvanceSelectDatasetCtrl',
					size: 'md'
				});
			};

			advanceStudyModalService.selectEnvironment = function (advanceType, isBeta, selectedDatasetId) {
				if (advanceType === 'deprecatedSample' || advanceType === 'samples') {
					studyService.studyHasSamples().then(function (response) {
						if (response && response.data) {
							advanceStudyModalService.openSelectEnvironmentModal(advanceType, isBeta, selectedDatasetId);
						} else {
							showErrorMessage('page-advance-modal-message', advanceSamplesError);
						}
					});
				} else {
					advanceStudyModalService.openSelectEnvironmentModal(advanceType, isBeta, selectedDatasetId);
				}
			};

			advanceStudyModalService.openSelectEnvironmentModal = function (advanceType, isBeta, selectedDatasetId) {
				$uibModal.open({
					templateUrl: '/Fieldbook/StudyManager/advance/study/selectEnvironmentModal',
					controller: "selectEnvironmentModalCtrl",
					size: 'md',
					resolve: {
						advanceType: function () {
							return advanceType;
						},
						isBeta: function () {
							return isBeta;
						},
						selectedDatasetId: function () {
							return selectedDatasetId;
						}
					}
				});
			};

			// Deprecated
			advanceStudyModalService.openDeprecatedAdvanceStudyModal = function (trialInstances, noOfReplications, locationsSelected, advanceType, values) {

				$uibModal.open({
					templateUrl: '/Fieldbook/static/angular-templates/advance/advanceStudyModal.html',
					controller: "advanceStudyModalController",
					size: 'lg',
					resolve: {
						advanceType: function () {
							return advanceType;
						},
						locationsSelected: function () {
							return locationsSelected;
						},
						trialInstances: function () {
							return trialInstances;
						},
						noOfReplications: function () {
							return noOfReplications;
						},
						values: function () {
							return values;
						},
					}
				});
			};

			advanceStudyModalService.openAdvanceModal = function (selectedTrialInstances, advanceType, noOfReplications, selectedDatasetId) {
				$uibModal.open({
					templateUrl: '/Fieldbook/static/js/trialmanager/advance/advanceIframeContainer.html',
					controller: "AdvanceModalCtrl",
					size: 'lg',
					resolve: {
						advanceType: function () {
							return advanceType;
						},
						trialInstances: function () {
							return selectedTrialInstances;
						},
						noOfReplications: function () {
							return noOfReplications;
						},
						selectedDatasetId: function () {
							return selectedDatasetId;
						}
					}
				}).result.finally(function () {
					$rootScope.navigateToTab('crossesAndSelectionsTab', {reload: true});
				});
			};

			return advanceStudyModalService;

		}
	]);

	// Deprecated
	manageTrialApp.controller('advanceStudyModalController', ['$scope', '$uibModalInstance', 'studyContext', 'advanceType', 'advanceStudyModalService', 'locationsSelected',
		'datasetService', 'trialInstances', 'noOfReplications', 'values', 'helpLinkService',
		function ($scope, $uibModalInstance, studyContext, advanceType, advanceStudyModalService, locationsSelected, datasetService,
				  trialInstances, noOfReplications, values, helpLinkService) {


			if (values) {
				// Persist the state of the values
				$scope.valueContainer = values;
			} else {
				$scope.valueContainer = {
					selectedBreedingMethod: null,
					methodChoice: true,
					lineChoice: true,
					allPlotsChoice: true,
					linesValue: 1,
					methodVariateId: '',
					plotVariateId: '',
					lineVariateId: '',
					studyId: studyContext.studyId,
					checkall: false,
					replicationsOptions: []
				};
			}


			$scope.currentYear = new Date().getFullYear() + '';
			$scope.currentMonth = pad(new Date().getMonth());
			$scope.harvestYearOptions = [];
			$scope.harvestMonthOptions = [];
			$scope.noOfReplications = noOfReplications;
			$scope.selectionMethodVariables = [];
			$scope.selectionPlantVariables = [];
			$scope.locationsSelected = locationsSelected;
			$scope.advanceType = advanceType;
			$scope.selectedTrialsString = trialInstances.join(",")

			helpLinkService.helpLink('MANAGE_CROP_BREEDING_METHODS').then(function (url) {
				$scope.breedingMethodHelpLink = url;
			});

			$scope.isBulkingMethod = function () {
				return $scope.valueContainer.selectedBreedingMethod && $scope.valueContainer.selectedBreedingMethod.isBulkingMethod;
			};

			$scope.methodChanged = function (item, model) {
				$scope.valueContainer.selectedBreedingMethod = item;
			};

			$scope.advanceStudy = function () {
				var lines = $scope.valueContainer.linesValue + '';

				var repsSectionIsDisplayed = $('#reps-section').length;
				if (repsSectionIsDisplayed) {
					var selectedReps = [];
					$('#replications input:checked').each(function () {
						selectedReps.push($(this).val());
					});

					if (selectedReps.length == 0) {
						showErrorMessage('page-advance-modal-message', noReplicationSelectedError);
						return false;
					}
				}

				if ($scope.valueContainer.methodChoice && !$scope.valueContainer.selectedBreedingMethod) {
					showErrorMessage('page-advance-modal-message', msgMethodError);
					return false;
				} else if (lines && !lines.match(/^\s*(\+|-)?\d+\s*$/)) {
					showErrorMessage('page-advance-modal-message', linesNotWholeNumberError);
					return false;
				} else if ($scope.validatePlantsSelected()) {
					SaveAdvanceList.doAdvanceStudy(trialInstances, noOfReplications, locationsSelected, advanceType, $scope.valueContainer);
					$scope.close();
				}
			};

			$scope.validatePlantsSelected = function () {
				var ids = '', isMixed = false, hasBulk = false,
					valid;

				if (!$scope.valueContainer.methodChoice || $scope.isBulkingMethod()) {
					if (!$scope.valueContainer.allPlotsChoice) {
						ids = ids + $scope.valueContainer.plotVariateId;
					}
					hasBulk = true;
				}
				if (!$scope.valueContainer.methodChoice || !$scope.isBulkingMethod()) {
					if (!$scope.valueContainer.lineChoice) {
						if (ids !== '') {
							ids = ids + ',';
						}
						ids = ids + $scope.valueContainer.lineVariateId;
					}
					if (hasBulk) {
						isMixed = true;
					}
				}

				valid = true;
				if (valid) {
					valid = $scope.checkBreedingMethodVariate();
				}
				if (valid && ids !== '') {
					$.ajax({
						url: '/Fieldbook/StudyManager/advance/study/countPlots/' + ids,
						type: 'GET',
						cache: false,
						async: false,
						success: function (data) {
							var choice,
								lineSameForAll;

							if (isMixed) {
								if (data == 0) {
									var param = $scope.valueContainer.lineVariateId + ' and/or ' + $scope.valueContainer.plotVariateId;
									var newMessage = msgEmptyListErrorTrial.replace(new RegExp(/\{0\}/g), param);
									showErrorMessage('page-advance-modal-message', newMessage);
									valid = false;
								}
							} else if ($scope.isBulkingMethod()) {
								if (data == '0') {
									var newMessage = msgEmptyListErrorTrial.replace(new RegExp(/\{0\}/g), $scope.valueContainer.plotVariateId);
									showErrorMessage('page-advance-modal-message', newMessage);
									valid = false;
								}
							} else {
								if (!$scope.valueContainer.lineChoice && data == '0') {
									var newMessage = msgEmptyListErrorTrial.replace(new RegExp(/\{0\}/g), $scope.valueContainer.lineVariateId);
									showErrorMessage('page-advance-modal-message', newMessage);
									valid = false;
								}
							}
						}.bind(this),
						error: function (jqXHR, textStatus, errorThrown) {
							console.log('The following error occured: ' + textStatus, errorThrown);
						}
					});
				}
				if (valid && isMixed) {
					return valid;
				}
				return valid;
			};

			$scope.checkBreedingMethodVariate = function () {
				var valid = true;

				if (!$scope.valueContainer.methodChoice && $scope.valueContainer.methodVariateId) {
					// TODO: Use $http
					$.ajax({
						url: '/Fieldbook/StudyManager/advance/study/countPlots/' + $scope.valueContainer.methodVariateId,
						type: 'GET',
						cache: false,
						async: false,
						success: function (data) {

							if (data == 0) {
								var newMessage = noMethodValueErrorTrial.replace(new RegExp(/\{0\}/g), $scope.valueContainer.methodVariateId);
								showErrorMessage('page-advance-modal-message', newMessage);

								valid = false;
							} else {
								valid = validateBreedingMethodValues($scope.valueContainer.methodVariateId);
							}

						}.bind(this),
						error: function (jqXHR, textStatus, errorThrown) {
							console.log('The following error occured: ' + textStatus, errorThrown);
						}
					});
				}
				return valid;
			};

			$scope.back = function () {
				advanceStudyModalService.openSelectEnvironmentModal(advanceType);
				$scope.close();
			};

			$scope.close = function () {
				$uibModalInstance.close();
			};

			$scope.validateMethodChoice = function () {
				if ($scope.selectionMethodVariables.length === 0) {
					$scope.valueContainer.methodChoice = true;
					showErrorMessage('', noMethodVariatesErrorTrial);
				}
			};

			$scope.validateLineChoice = function () {
				if ($scope.selectionPlantVariables.length === 0) {
					$scope.valueContainer.lineChoice = true;
					showErrorMessage('', noLineVariatesErrorTrial);
				}
			};

			$scope.validatePlotChoice = function () {
				if ($scope.selectionPlantVariables.length === 0) {
					$scope.valueContainer.allPlotsChoice = true;
					showErrorMessage('', noLineVariatesErrorTrial);
				}
			};

			$scope.checkUncheckAll = function () {
				$scope.valueContainer.replicationsOptions.forEach((repOption) => {
					repOption.selected = $scope.valueContainer.checkall;
				});
			};

			$scope.init = function () {
				const SELECTION_VARIABLE_TYPE = 1807;
				datasetService.getColumns(studyContext.measurementDatasetId, false).then(function (data) {
					$scope.selectionMethodVariables = data.filter(item => item.variableType === 'SELECTION_METHOD' && item.property === 'Breeding method');
					$scope.selectionPlantVariables = data.filter(item => item.variableType === 'SELECTION_METHOD' && item.property === 'Selections');
					$scope.valueContainer.methodVariateId = String($scope.selectionMethodVariables.length !== 0 ? $scope.selectionMethodVariables[0].termId : 0);
					$scope.valueContainer.lineVariateId = String($scope.selectionPlantVariables.length !== 0 ? $scope.selectionPlantVariables[0].termId : 0);
					$scope.valueContainer.plotVariateId = String($scope.selectionPlantVariables.length !== 0 ? $scope.selectionPlantVariables[0].termId : 0);
				});
				$scope.initializeHarvestYearOptions();
				$scope.initializeHarvestMonthOptions();
				$scope.initializeReplicationsOptions();
			};

			$scope.initializeHarvestYearOptions = function () {
				var year = new Date().getFullYear() - 10;
				var endYear = year + 20;
				for (year; year <= endYear; year++) {
					$scope.harvestYearOptions.push(year + '');
				}
			};

			$scope.initializeHarvestMonthOptions = function () {
				for (var month = 1; month <= 12; month++) {
					$scope.harvestMonthOptions.push(pad(month));
				}
			};

			$scope.initializeReplicationsOptions = function () {
				if ($scope.valueContainer.replicationsOptions.length === 0) {
					for (var rep = 1; rep <= $scope.noOfReplications; rep++) {
						$scope.valueContainer.replicationsOptions.push({repIndex: rep, selected: rep == 1});
					}
				}
			};

			function pad(n) {
				return n < 10 ? '0' + n : n
			}

			$scope.init();
		}
	]);

	manageTrialApp.controller('selectEnvironmentModalCtrl', ['$scope', '$uibModalInstance', 'TrialManagerDataService', 'studyInstanceService',
		'$timeout', 'studyContext', 'datasetService', 'advanceStudyModalService', 'advanceType', 'isBeta', 'selectedDatasetId', 'DESIGN_TYPE',
		function ($scope, $uibModalInstance, TrialManagerDataService, studyInstanceService, $timeout, studyContext, datasetService,
				  advanceStudyModalService, advanceType, isBeta, selectedDatasetId, DESIGN_TYPE) {

			$scope.settings = TrialManagerDataService.settings.environments;
			if (Object.keys($scope.settings).length === 0) {
				$scope.settings = {};
				$scope.settings.managementDetails = [];
				$scope.settings.trialConditionDetails = [];
			}

			$scope.TRIAL_LOCATION_NAME_INDEX = 8180;
			$scope.TRIAL_LOCATION_ABBR_INDEX = 8189;
			$scope.LOCATION_NAME_ID = 8190;
			$scope.applicationData = TrialManagerDataService.applicationData;
			$scope.instanceInfo = studyInstanceService.instanceInfo;

			$scope.applicationData.advanceType = advanceType;
			$scope.isBeta = isBeta;

			$scope.$on('changeEnvironments', function () {
				$scope.instanceInfo = studyInstanceService.instanceInfo;

				//create a map for location dropdown values
				var locationMap = {};
				angular.forEach($scope.settings.managementDetails.vals()[$scope.LOCATION_NAME_ID].allValues, function (locationVariable) {
					locationMap[locationVariable.id] = locationVariable;
				});

				angular.forEach($scope.instanceInfo.instances, function (instance) {
					if (locationMap[instance.managementDetailValues[$scope.LOCATION_NAME_ID]]) {

						// Ensure that the location id and location name details of the $scope.instanceInfo.instances
						// are updated with values from Location json object
						instance.managementDetailValues[$scope.LOCATION_NAME_ID]
							= locationMap[instance.managementDetailValues[$scope.LOCATION_NAME_ID]].id;
						instance.managementDetailValues[$scope.TRIAL_LOCATION_NAME_INDEX]
							= locationMap[instance.managementDetailValues[$scope.LOCATION_NAME_ID]].name;
					}
				});
			});

			$scope.noOfReplications = TrialManagerDataService.currentData.experimentalDesign.designType === DESIGN_TYPE.P_REP ? 0
				: TrialManagerDataService.currentData.experimentalDesign.replicationsCount;

			$scope.instances = [];
			$scope.selectedInstances = {};
			$scope.isEmptySelection = false;

			//NOTE: Continue action for navigate from Locations to Advance Study Modal
			$scope.selectInstanceContinue = function () {

				// Do not go ahead for Advancing unless study has experimental design & number of replications variables
				if (TrialManagerDataService.currentData.experimentalDesign.designType === null) {
					showAlertMessage('', $.fieldbookMessages.advanceListUnableToGenerateWarningMessage);
					return;
				}

				var selectedTrialInstances = [];
				var selectedLocationDetails = [];
				var locationAbbr = false;

				if ($scope.isEmptySelection) {
					showErrorMessage('', $.fieldbookMessages.errorNotSelectedInstance);
				} else {
					if ($scope.settings.managementDetails.val($scope.TRIAL_LOCATION_ABBR_INDEX)) {
						selectedLocationDetails
							.push($scope.settings.managementDetails.val($scope.TRIAL_LOCATION_ABBR_INDEX).variable.name);
						locationAbbr = true;
					} else {
						selectedLocationDetails
							.push($scope.settings.managementDetails.val($scope.LOCATION_NAME_ID).variable.name);
					}

					angular.forEach($scope.instances, function (instance) {
						var isSelected = $scope.selectedInstances[instance.instanceNumber];
						if (isSelected) {
							selectedTrialInstances.push(instance.instanceNumber);
							if (locationAbbr) {
								selectedLocationDetails.push(instance.customLocationAbbreviation);
							} else {
								selectedLocationDetails.push(instance.locationName);
							}
						}
					});

					if ($scope.applicationData.advanceType === 'study' || $scope.applicationData.advanceType === 'samples') {
						advanceStudyModalService.openAdvanceModal(selectedTrialInstances, $scope.applicationData.advanceType, $scope.noOfReplications, selectedDatasetId);
						$uibModalInstance.close();
					} else {
						advanceStudyModalService.openDeprecatedAdvanceStudyModal(selectedTrialInstances, $scope.noOfReplications, selectedLocationDetails,
							$scope.applicationData.advanceType, null);
							$uibModalInstance.close();
					}

				}
			};

			$scope.close = function () {
				$uibModalInstance.close();
			}

			$scope.back = function() {
				advanceStudyModalService.openSelectDatasetModal(selectedDatasetId);
				$scope.close();
			}

			$scope.init = function () {
				datasetService.getDatasetInstances(studyContext.measurementDatasetId).then(function (datasetInstances) {
					$scope.instances = datasetInstances;
				});
			};

			$scope.showBackButton = function () {
				return advanceType === 'study' && isBeta;
			}

			$scope.init();
		}]);

	manageTrialApp.controller('AdvanceModalCtrl', ['$scope', '$q', 'studyContext', '$uibModalInstance', 'trialInstances', 'advanceType',
		'noOfReplications', 'selectedDatasetId', 'advanceStudyModalService','$window', '$rootScope', 'EVENTS', 'HasAnyAuthorityService', 'PERMISSIONS',
		function ($scope, $q, studyContext, $uibModalInstance, trialInstances, advanceType, noOfReplications, selectedDatasetId,
				  advanceStudyModalService, $window, $rootScope, EVENTS, HasAnyAuthorityService, PERMISSIONS) {

			$scope.hasAnyAuthority = HasAnyAuthorityService.hasAnyAuthority;
			$scope.PERMISSIONS = PERMISSIONS;

			$scope.url = `/ibpworkbench/controller/jhipster#/advance-${advanceType}?restartApplication` +
				'&cropName=' + studyContext.cropName +
				'&programUUID=' + studyContext.programId +
				'&studyId=' + studyContext.studyId +
				'&noOfReplications=' + noOfReplications +
				'&trialInstances=' + trialInstances +
				'&selectedDatasetId=' + selectedDatasetId;

			$scope.advanceType = advanceType;

			$scope.advanceSuccess = false;

			$window.addEventListener("message", onMessage);

			$scope.cancel = function() {
				$uibModalInstance.close(null);

				if ($scope.advanceSuccess) {
					redirectToCrossesAndSelectionsTab();
				}
			};

			// Clean the listener previously added using addEventListener. This a workaround to avoid having a listener each time this controller is being created.
			$scope.$on('$destroy', function() {
				$window.removeEventListener("message" , onMessage);
			});

			function onMessage(event) {
				if (event.data.name === EVENTS.SELECT_INSTANCES) {
					$uibModalInstance.close(null);
					advanceStudyModalService.selectEnvironment(event.data.advanceType, true, event.data.selectedDatasetId);
				}

				if (event.data.name === EVENTS.GERMPLASM_LIST_CREATED) {
					$uibModalInstance.close(null);
					showSuccessfulMessage('', saveListSuccessfullyMessage);

					redirectToCrossesAndSelectionsTab();
				}

				if (event.data.name === EVENTS.ADVANCE_SUCCESS) {
					$scope.advanceSuccess = true;
				}

				if (event.data.name === EVENTS.TREE_STATE_PERSISTED) {
					$uibModalInstance.close(null);
					if ($scope.hasAnyAuthority(PERMISSIONS.VIEW_CROSSES_AND_SELECTIONS_PERMISSIONS)) {
						redirectToCrossesAndSelectionsTab();
					}
				}
			}

			function redirectToCrossesAndSelectionsTab() {
				// Notify the application that germplasm has been saved. This will display the 'Crosses and Selections'
				// tab if germplasm is already created within the study.
				$rootScope.$broadcast('germplasmListSaved');

				// Refresh and show the 'Crosses and Selections' tab after saving the germplasm list
				$rootScope.navigateToTab('germplasmStudySource', {reload: true});
			}

		}]);

	manageTrialApp.controller('AdvanceSelectDatasetCtrl', ['$scope', '$uibModal', '$uibModalInstance', 'studyContext', 'advanceStudyModalService',
		'DATASET_TYPES',
		function ($scope, $uibModal, $uibModalInstance, studyContext, advanceStudyModalService, DATASET_TYPES) {

			$scope.modalTitle = 'Advance Study (Beta)';
			$scope.message = 'Please choose the dataset you want to take as a source for the advancement process:';
			$scope.measurementDatasetId = studyContext.measurementDatasetId;
			$scope.selected = {datasetId: $scope.measurementDatasetId};
			$scope.supportedDatasetTypes = [DATASET_TYPES.PLOT_OBSERVATIONS, DATASET_TYPES.PLANT_SUBOBSERVATIONS];

			$scope.selectInstances = function () {
				advanceStudyModalService.selectEnvironment('study', true, this.selected.datasetId);
			};

		}]);

})();
