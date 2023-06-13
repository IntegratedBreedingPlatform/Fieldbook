(function () {
	'use strict';

	const manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.factory('advanceStudyModalService', ['$uibModal', 'studyService', 'studyContext', 'feedbackService',
		function ($uibModal, studyService, studyContext, feedbackService) {

			var advanceStudyModalService = {};

			advanceStudyModalService.openSelectDatasetModal = function () {
				$uibModal.open({
					template: '<dataset-option-modal modal-title="modalTitle" message="message"' +
						'selected="selected" on-continue="selectInstances()" supported-dataset-types="supportedDatasetTypes"></dataset-option-modal>',
					controller: 'AdvanceSelectDatasetCtrl',
					size: 'md'
				});
			};

			advanceStudyModalService.selectEnvironment = function (advanceType, selectedDatasetId) {
				if (advanceType === 'samples') {
					studyService.studyHasSamples().then(function (response) {
						if (response && response.data) {
							advanceStudyModalService.openSelectEnvironmentModal(advanceType, selectedDatasetId);
						} else {
							showErrorMessage('page-advance-modal-message', advanceSamplesError);
						}
					});
				} else {
					advanceStudyModalService.openSelectEnvironmentModal(advanceType, selectedDatasetId);
				}
			};

			advanceStudyModalService.openSelectEnvironmentModal = function (advanceType, selectedDatasetId) {
				$uibModal.open({
					templateUrl: '/Fieldbook/StudyManager/advance/study/selectEnvironmentModal',
					controller: "selectEnvironmentModalCtrl",
					size: 'md',
					resolve: {
						advanceType: function () {
							return advanceType;
						},
						selectedDatasetId: function () {
							return selectedDatasetId;
						}
					}
				});
			};

			advanceStudyModalService.openAdvanceModal = function (selectedTrialInstances, advanceType, noOfReplications, selectedDatasetId,
																  isFeedbackEnabled) {
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

	manageTrialApp.controller('selectEnvironmentModalCtrl', ['$scope', '$uibModalInstance', 'TrialManagerDataService', 'studyInstanceService',
		'$timeout', 'studyContext', 'datasetService', 'advanceStudyModalService', 'advanceType', 'selectedDatasetId', 'DESIGN_TYPE', 'FEEDBACK_ENABLED',
		function ($scope, $uibModalInstance, TrialManagerDataService, studyInstanceService, $timeout, studyContext, datasetService,
				  advanceStudyModalService, advanceType, selectedDatasetId, DESIGN_TYPE, FEEDBACK_ENABLED) {

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
						advanceStudyModalService.openAdvanceModal(selectedTrialInstances, $scope.applicationData.advanceType, $scope.noOfReplications,
							selectedDatasetId, FEEDBACK_ENABLED);
						$uibModalInstance.close();
					}

				}
			};

			$scope.close = function () {
				$uibModalInstance.close();
			}

			$scope.back = function () {
				advanceStudyModalService.openSelectDatasetModal(selectedDatasetId);
				$scope.close();
			}

			$scope.init = function () {
				datasetService.getDatasetInstances(studyContext.measurementDatasetId).then(function (datasetInstances) {
					$scope.instances = datasetInstances;
				});
			};

			$scope.showBackButton = function () {
				return advanceType === 'study';
			}

			$scope.init();
		}]);

	manageTrialApp.controller('AdvanceModalCtrl', ['$scope', '$q', 'studyContext', '$uibModalInstance', 'trialInstances', 'advanceType',
		'noOfReplications', 'selectedDatasetId', 'advanceStudyModalService', '$window', '$rootScope', 'EVENTS', 'HasAnyAuthorityService', 'PERMISSIONS', 'FEEDBACK_ENABLED', 'feedbackService',
		function ($scope, $q, studyContext, $uibModalInstance, trialInstances, advanceType, noOfReplications, selectedDatasetId,
				  advanceStudyModalService, $window, $rootScope, EVENTS, HasAnyAuthorityService, PERMISSIONS, FEEDBACK_ENABLED, feedbackService) {

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

			$scope.cancel = function () {
				$uibModalInstance.close(null);

				if ($scope.advanceSuccess) {
					openFeedbackSurvey(FEEDBACK_ENABLED, 'ADVANCE_GERMPLASM', feedbackService);
					redirectToCrossesAndSelectionsTab();
				}
			};

			// Clean the listener previously added using addEventListener. This a workaround to avoid having a listener each time this controller is being created.
			$scope.$on('$destroy', function () {
				$window.removeEventListener("message", onMessage);
			});

			function onMessage(event) {
				if (event.data.name === EVENTS.SELECT_INSTANCES) {
					$uibModalInstance.close(null);
					advanceStudyModalService.selectEnvironment(event.data.advanceType, true, event.data.selectedDatasetId);
				}

				if (event.data.name === EVENTS.GERMPLASM_LIST_CREATED) {
					$uibModalInstance.close(null);
					showSuccessfulMessage('', saveListSuccessfullyMessage);
					openFeedbackSurvey(FEEDBACK_ENABLED, 'ADVANCE_GERMPLASM', feedbackService);
					redirectToCrossesAndSelectionsTab();
				}

				if (event.data.name === EVENTS.ADVANCE_SUCCESS) {
					$scope.advanceSuccess = true;
				}

				if (event.data.name === EVENTS.TREE_STATE_PERSISTED) {
					$uibModalInstance.close(null);
					openFeedbackSurvey(FEEDBACK_ENABLED, 'ADVANCE_GERMPLASM', feedbackService);
					redirectToCrossesAndSelectionsTab();
				}
			}

			function redirectToCrossesAndSelectionsTab() {
				if ($scope.hasAnyAuthority(PERMISSIONS.VIEW_CROSSES_AND_SELECTIONS_PERMISSIONS)) {
					// Notify the application that germplasm has been saved. This will display the 'Crosses and Selections'
					// tab if germplasm is already created within the study.
					$rootScope.$broadcast('germplasmListSaved');

					// Refresh and show the 'Crosses and Selections' tab after saving the germplasm list
					$rootScope.navigateToTab('germplasmStudySource', {reload: true});
				}
			}

		}]);

	manageTrialApp.controller('AdvanceSelectDatasetCtrl', ['$scope', '$uibModal', '$uibModalInstance', 'studyContext', 'advanceStudyModalService',
		'DATASET_TYPES',
		function ($scope, $uibModal, $uibModalInstance, studyContext, advanceStudyModalService, DATASET_TYPES) {

			$scope.modalTitle = 'Advance Study';
			$scope.message = 'Please choose the dataset you want to take as a source for the advancement process:';
			$scope.measurementDatasetId = studyContext.measurementDatasetId;
			$scope.selected = {datasetId: $scope.measurementDatasetId};
			$scope.supportedDatasetTypes = [DATASET_TYPES.PLOT_OBSERVATIONS, DATASET_TYPES.PLANT_SUBOBSERVATIONS];

			$scope.selectInstances = function () {
				advanceStudyModalService.selectEnvironment('study', this.selected.datasetId);
			};

		}]);

})();
