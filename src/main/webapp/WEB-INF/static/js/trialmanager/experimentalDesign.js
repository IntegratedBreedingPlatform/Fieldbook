/* global angular, showErrorMessage, showSuccessfulMessage, expDesignMsgs */

(function() {
		'use strict';

		angular.module('manageTrialApp')
			.constant('EXP_DESIGN_MSGS', expDesignMsgs)
			.constant('EXPERIMENTAL_DESIGN_PARTIALS_LOC', '/Fieldbook/static/angular-templates/experimentalDesignPartials/')
			.controller('ExperimentalDesignCtrl', ['$scope', '$state', 'EXPERIMENTAL_DESIGN_PARTIALS_LOC', 'TrialManagerDataService', '$http',
				'EXP_DESIGN_MSGS', '_', '$q', 'Messages', function($scope, $state, EXPERIMENTAL_DESIGN_PARTIALS_LOC, TrialManagerDataService, $http, EXP_DESIGN_MSGS, _, $q, Messages) {

					$scope.applicationData = TrialManagerDataService.applicationData;
					$scope.studyID = TrialManagerDataService.currentData.basicDetails.studyID;

					$scope.designTypes = TrialManagerDataService.applicationData.designTypes;
					$scope.designTypeView = [];

					$scope.generateDesignView = function() {
						// BV design
						$.each($scope.designTypes, function(index, designType){
							if(!designType.isPreset && designType.name !== 'Custom Import Design'){
								$scope.designTypeView.push(designType);
							}
						});

						// separator
						if($scope.designTypes.length  > 4){
							$scope.designTypeView.push({id:	null, name: '----------------------------------------------', isDisabled: true });
						}

						// Preset
						$.each($scope.designTypes, function(index, designType){
							if(designType.isPreset && designType.name !== 'Custom Import Design'){
								$scope.designTypeView.push(designType);
							}
						});
					};

					$scope.generateDesignView();

					$scope.$watch(function() {
						return $scope.data.designType;
					}, function(newValue) {
						// If Design Type is Preset Design
						$scope.currentDesignType = $scope.designTypes[newValue];
					});

					// TODO : re run computeLocalData after loading of previous trial as template
					$scope.computeLocalData = function() {
						$scope.data.designType = TrialManagerDataService.currentData.experimentalDesign.designType;

						$scope.settings = TrialManagerDataService.specialSettings.experimentalDesign;
						$scope.settings.treatmentFactors = TrialManagerDataService.settings.treatmentFactors.details;

						// user has a treatment factor, if previous exp design is not RCBD, then set selection to RCBD
						// may need to clear non RCBD input
						if (TrialManagerDataService.settings.treatmentFactors.details.keys().length > 0) {
							$scope.data.designType = $scope.designTypes[0].id;
						}

						if ($scope.data.designType != null && $scope.data.designType !== '') {
							$scope.currentDesignType = $scope.designTypes[$scope.data.designType];

							if ($scope.currentDesignType.params !== null) {
								$scope.currentParams = EXPERIMENTAL_DESIGN_PARTIALS_LOC + $scope.currentDesignType.params;
							} else {
								$scope.currentParams = null;
							}

							if (!$scope.settings.showAdvancedOptions[$scope.data.designType]) {
								$scope.settings.showAdvancedOptions[$scope.data.designType] = $scope.data.useLatenized;
							}

							// loading for existing trial
							if($scope.studyID != null && !TrialManagerDataService.applicationData.unappliedChangesAvailable){
								$scope.applicationData.hasGeneratedDesignPreset = $scope.designTypes[$scope.data.designType].isPreset
									&& $scope.studyID != null && TrialManagerDataService.trialMeasurement.count > 0;
							}

							if ($scope.currentDesignType !== null && $scope.currentDesignType.name === 'Custom Import Design') {
								$http.get('/Fieldbook/DesignImport/getCustomImportDesignTypeDetails').then(function(result) {
									$scope.currentDesignType.templateName = result.data.templateName;
								});
							}
						}

						$scope.germplasmDescriptorSettings = TrialManagerDataService.settings.germplasm;
						$scope.measurementDetails = TrialManagerDataService.trialMeasurement;
						$scope.data.noOfEnvironments = TrialManagerDataService.currentData.environments.noOfEnvironments ?
							TrialManagerDataService.currentData.environments.noOfEnvironments : 0;
						$scope.data.treatmentFactors = TrialManagerDataService.settings.treatmentFactors.details;
						$scope.data.treatmentFactorsData = TrialManagerDataService.currentData.treatmentFactors.currentData;

						$scope.data.hasMeasurementData = TrialManagerDataService.trialMeasurement.hasMeasurement;

					};

					$scope.disableGenerateDesign = TrialManagerDataService.trialMeasurement.hasMeasurement && !TrialManagerDataService.applicationData.unappliedChangesAvailable;

					//FIXME: cheating a bit for the meantime.
					var totalGermplasms = countGermplasms();
					if (!TrialManagerDataService.applicationData.germplasmListCleared) {
						$scope.totalGermplasmEntryListCount = TrialManagerDataService.specialSettings.experimentalDesign.
							germplasmTotalListCount = totalGermplasms ? totalGermplasms :
							TrialManagerDataService.specialSettings.experimentalDesign.germplasmTotalListCount;
					} else {
						$scope.totalGermplasmEntryListCount = TrialManagerDataService.specialSettings.experimentalDesign.
							germplasmTotalListCount = totalGermplasms;
					}

					if (isNaN($scope.totalGermplasmEntryListCount)) {
						$scope.totalGermplasmEntryListCount = TrialManagerDataService.specialSettings.
							experimentalDesign.germplasmTotalListCount = 0;
					}

					$scope.data = TrialManagerDataService.currentData.experimentalDesign;

					// the property "startingEntryNo" is at least part of the data object here when the germplasm tab is loaded first
					if (!$scope.data || Object.keys($scope.data).length <= 1) {
						angular.copy({
							totalGermplasmListCount: $scope.totalGermplasmEntryListCount,
							designType: null,
							replicationsCount: null,
							blockSize: null,
							useLatenized: false,
							nblatin: null,
							replicationsArrangement: null,
							rowsPerReplications: null,
							colsPerReplications: null,
							nrlatin: null,
							nclatin: null,
							replatinGroups: '',
							startingPlotNo: 1,
							startingEntryNo: (typeof $scope.data.startingEntryNo !== 'undefined')? parseInt($scope.data.startingEntryNo,10) : 1 ,
							hasMeasurementData: TrialManagerDataService.trialMeasurement.hasMeasurement
						}, $scope.data);
					}

					TrialManagerDataService.specialSettings.experimentalDesign.data = $scope.data;

					$scope.computeLocalData();

					$scope.replicationsArrangementGroupsOpts = {
						1: 'In a single column',
						2: 'In a single row',
						3: 'In adjacent columns'
					};
					$scope.onSwitchDesignTypes = function(newId) {
						if (newId !== '') {
							$scope.currentDesignType = $scope.designTypes[newId];
							$scope.currentParams = EXPERIMENTAL_DESIGN_PARTIALS_LOC + $scope.currentDesignType.params;
							$scope.data.designType = $scope.currentDesignType.id;
							TrialManagerDataService.currentData.experimentalDesign.designType = $scope.data.designType;
							$scope.applicationData.unappliedChangesAvailable = true;

							if ($scope.designTypes[newId].isPreset) {
								showAlertMessage('', ImportDesign.getMessages().OWN_DESIGN_SELECT_WARNING, 5000);
							}
						} else {
							$scope.currentDesignType = null;
							$scope.data.designType = '';
							$scope.currentParams = '';
						}

						$scope.applicationData.hasGeneratedDesignPreset = false;
					};

					$scope.toggleIsPresetWithGeneratedDesign = function() {
						$scope.applicationData.hasGeneratedDesignPreset =  $scope.applicationData.unsavedGeneratedDesign && $scope.designTypes[$scope.data.designType].isPreset;
					};

					$scope.updateAfterGeneratingDesignSuccessfully = function() {
						//we show the preview
						showSuccessfulMessage('', $.experimentDesignMessages.experimentDesignGeneratedSuccessfully);
						TrialManagerDataService.clearUnappliedChangesFlag();
						TrialManagerDataService.applicationData.unsavedGeneratedDesign = true;
						$('#chooseGermplasmAndChecks').data('replace', '1');
						$('body').data('expDesignShowPreview', '1');
						$scope.toggleIsPresetWithGeneratedDesign();
					};

					// on click generate design button
					$scope.generateDesign = function() {
						if (!$scope.doValidate()) {
							return;
						}

						var environmentData = angular.copy($scope.data);
						environmentData.startingEntryNo = TrialManagerDataService.currentData.experimentalDesign.startingEntryNo;

						// transform ordered has of treatment factors if existing to just the map
						if (environmentData && environmentData.treatmentFactors) {
							environmentData.treatmentFactors = $scope.data.treatmentFactors.vals();
						}
						
						// non-preset design type
						if (!$scope.designTypes[$scope.data.designType].isPreset) {
							TrialManagerDataService.generateExpDesign(environmentData).then(
								function(response) {
									if (response.valid === true) {
										$scope.updateAfterGeneratingDesignSuccessfully();
									} else {
										showErrorMessage('', response.message);
									}
								}
							);
						} else {
							TrialManagerDataService.generatePresetExpDesign($scope.data.designType).then(function() {
								$scope.updateAfterGeneratingDesignSuccessfully();
							}, function(data) {
								showErrorMessage('', data.error[0]);
							});
						}
					};

					// Register designImportGenerated handler that will activate when an importDesign is generated
					$scope.$on('designImportGenerated', function() {
						var summaryPromise = $http.get('/Fieldbook/DesignImport/getMappingSummary');
						var designTypeDetailsPromise = $http.get('/Fieldbook/DesignImport/getCustomImportDesignTypeDetails');

						$q.all([summaryPromise, designTypeDetailsPromise]).then(function(results) {
							$scope.applicationData.importDesignMappedData = results[0].data;
							$scope.currentDesignType.templateName = results[1].data.templateName;
						});
					});

					$scope.showConfirmResetDesign = function() {

						var deferred = $q.defer();

						bootbox.dialog({
							title: Messages.DESIGN_IMPORT_PRESET_DESIGN_CHANGE_DESIGN,
							message: Messages.DESIGN_IMPORT_CHANGE_DESIGN_DESCRIPTION_TRIAL,
							closeButton: false,
							onEscape: false,
							buttons: {
								yes: {
									label: Messages.YES,
									className: 'btn-primary',
									callback: function() {
										deferred.resolve(true);
									}
								},
								no: {
									label: Messages.NO,
									className: 'btn-default',
									callback: function() {
										deferred.reject(false);
									}
								}
							}
						});

						return deferred.promise;
					};

					$scope.resetExperimentalDesign = function() {

						$scope.showConfirmResetDesign().then(function(result) {
							// the following reset the data used for the experimental design, allowing the user to select another design again
							$scope.applicationData.hasGeneratedDesignPreset = false;
							$scope.applicationData.isGeneratedOwnDesign = false;
							$scope.currentDesignType = null;
							$scope.data.designType = '';

							// the following prevents the user from saving before re-generating the design, to avoid having invalid measurement data
							if (TrialManagerDataService.trialMeasurement.count > 0) {
								TrialManagerDataService.applicationData.unappliedChangesAvailable = true;
							}
						});
					};

					$scope.toggleDesignView = function() {
						return !$scope.applicationData.unappliedChangesAvailable && ($scope.applicationData.isGeneratedOwnDesign
							|| ($scope.data.designType != null
							&& $scope.data.designType !== ''
							&& $scope.designTypes[$scope.data.designType].name === 'Custom Import Design')
							|| $scope.applicationData.hasGeneratedDesignPreset);
					};

					$scope.isImportedDesign = function() {
						return $scope.data.designType != null
							&& $scope.data.designType !== ''
							&& $scope.designTypes[$scope.data.designType].name === 'Custom Import Design';
					};

					$scope.isBVDesign = function() {
						var selectedDesignType = $scope.designTypes[$scope.data.designType];
						return $scope.data.designType != null
							&& $scope.data.designType !== ''
							&& !selectedDesignType.isPreset && selectedDesignType.name !== 'Custom Import Design';
					};

					$scope.doValidate = function() {

						switch ($scope.currentDesignType.id) {
							case 0:
							{
								if (!$scope.data.replicationsCount || $scope.expDesignForm.replicationsCount.$invalid) {
									showErrorMessage('page-message', EXP_DESIGN_MSGS[4]);
									return false;
								}

								if (!$scope.settings.treatmentFactors || !TrialManagerDataService.currentData.treatmentFactors.currentData) {
									showErrorMessage('page-message', EXP_DESIGN_MSGS[18]);
									return false;
								}

								var isValidTreatmentVars = TrialManagerDataService.validateAllTreatmentFactorLabels({});
								if (!!isValidTreatmentVars && isValidTreatmentVars.hasError) {
									showErrorMessage(isValidTreatmentVars.customHeader, isValidTreatmentVars.customMessage);
									return false;
								}

								var errorCode = TrialManagerDataService.treatmentFactorDataInvalid();

								if (errorCode === 1) {
									showErrorMessage('page-message', EXP_DESIGN_MSGS[24]);
									return false;
								} else if (errorCode === 2) {
									showErrorMessage('page-message', EXP_DESIGN_MSGS[25]);
									return false;
								}

								break;
							}
							case 1:
							{

								if (!$scope.data.replicationsCount || $scope.expDesignForm.replicationsCount.$invalid) {
									showErrorMessage('page-message', EXP_DESIGN_MSGS[5]);
									return false;
								}

								if (!$scope.data.blockSize || $scope.expDesignForm.blockSize.$invalid) {
									showErrorMessage('page-message', EXP_DESIGN_MSGS[8]);
									return false;
								}

								if ($scope.totalGermplasmEntryListCount % $scope.data.blockSize > 0) {
									showErrorMessage('page-message', EXP_DESIGN_MSGS[13]);
									return false;
								}

								// latinized
								if ($scope.data.useLatenized) {
									if ($scope.data.nblatin === null) {
										showErrorMessage('page-message', EXP_DESIGN_MSGS[27]);
										return false;
									}
									if ($scope.data.nblatin >= ($scope.totalGermplasmEntryListCount / $scope.data.blockSize)) {
										showErrorMessage('page-message', EXP_DESIGN_MSGS[11]);
										return false;
									}

									if ($scope.data.nblatin >= Number($scope.data.replicationsCount)) {
										showErrorMessage('page-message', EXP_DESIGN_MSGS[23]);
										return false;
									}

									if (Number($scope.data.replicationsArrangement) <= 0) {
										showErrorMessage('page-message', EXP_DESIGN_MSGS[21]);
										return false;

									}
									if (Number($scope.data.replicationsArrangement) === 3) {
										if (!$scope.data.replatinGroups || $scope.expDesignForm.replatinGroups.$invalid) {
											showErrorMessage('page-message', EXP_DESIGN_MSGS[22]);
											return false;
										}

										// validate sum of replatinGroups
										var sum = 0;
										var arrGroups = $scope.data.replatinGroups.split(',');

										for (var i = 0; i < arrGroups.length; i++) {
											sum += Number(arrGroups[i]);
										}

										if (sum !== Number($scope.data.replicationsCount)) {
											showErrorMessage('page-message', EXP_DESIGN_MSGS[12]);
											return false;
										}
									}
								}

								break;
							}
							case 2:
							{
								if (!$scope.data.replicationsCount && $scope.expDesignForm.replicationsCount.$invalid) {
									showErrorMessage('page-message', EXP_DESIGN_MSGS[5]);
									return false;
								}

								if ($scope.data.rowsPerReplications * $scope.data.colsPerReplications !== $scope.totalGermplasmEntryListCount) {
									showErrorMessage('page-message', EXP_DESIGN_MSGS[6]);
									return false;
								}

								if ($scope.data.useLatenized) {

									if ($scope.data.nrlatin >= $scope.data.replicationsCount) {
										showErrorMessage('page-message', EXP_DESIGN_MSGS[15]);
										return false;
									}

									if ($scope.data.nclatin >= $scope.data.replicationsCount) {
										showErrorMessage('page-message', EXP_DESIGN_MSGS[16]);
										return false;
									}

									if ($scope.data.nrlatin <= 0 || $scope.data.nrlatin >= $scope.data.rowsPerReplications) {
										showErrorMessage('page-message', EXP_DESIGN_MSGS[14]);
										return false;

									}

									if ($scope.data.nclatin <= 0 || $scope.data.nclatin >= $scope.data.colsPerReplications) {
										showErrorMessage('page-message', EXP_DESIGN_MSGS[17]);
										return false;

									}

									if ($scope.data.replicationsArrangement <= 0) {
										showErrorMessage('page-message', EXP_DESIGN_MSGS[21]);
										return false;
									}

									if (Number($scope.data.replicationsArrangement) === 3) {
										if (!$scope.data.replatinGroups || $scope.expDesignForm.replatinGroups.$invalid) {
											showErrorMessage('page-message', EXP_DESIGN_MSGS[22]);
											return false;
										}

										// validate sum of replatinGroups
										var _sum = 0;
										var _arrGroups = $scope.data.replatinGroups.split(',');

										for (var j = 0; j < _arrGroups.length; j++) {
											_sum += Number(_arrGroups[j]);
										}

										if (_sum !== Number($scope.data.replicationsCount)) {
											showErrorMessage('page-message', EXP_DESIGN_MSGS[12]);
											return false;
										}
									}

								}

								break;
							}
							case 4:
							case 5:
							case 6:
							{
								var actualNoOfGermplasmListEntries = $scope.currentDesignType.totalNoOfEntries;
								if ($scope.totalGermplasmEntryListCount > 0 && $scope.totalGermplasmEntryListCount !== actualNoOfGermplasmListEntries) {
									showErrorMessage('page-message', EXP_DESIGN_MSGS[28]);
									return false;
								}
								break;
							}
						}

						if ($scope.totalGermplasmEntryListCount <= 0) {
							showErrorMessage('page-message', EXP_DESIGN_MSGS[26]);
							return false;
						}

						return true;
					};

				}])

			// FILTERS USED FOR EXP DESIGN

			.filter('filterFactors', ['_', function(_) {
				return function(factorList, designTypeIndex) {

					var excludes = [
						[8230, 8220, 8581, 8582],
						[8581, 8582],
						[8220, 8200]
					];

					return _.filter(factorList, function(value) {
						return !_.contains(excludes[designTypeIndex], value);
					});

				};
			}]);

	}
)();
