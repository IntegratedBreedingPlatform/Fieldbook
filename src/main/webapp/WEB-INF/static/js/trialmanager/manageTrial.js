/*global angular, openStudyTree, showErrorMessage, operationMode, resetGermplasmList,
showAlertMessage,showMeasurementsPreview,createErrorNotification,errorMsgHeader, ImportDesign, isOpenStudy, InventoryPage*/
//TODO move this messages under a namespace
/* global addEnvironmentsImportDesignMessage, importSaveDataWarningMessage*/

(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp', ['designImportApp', 'leafnode-utils', 'fieldbook-utils', 'subObservation',
		'ui.router', 'ui.bootstrap', 'ngLodash', 'ngResource', 'ngStorage', 'datatables', 'datatables.buttons', 'datatables.colreorder',
		'ngSanitize', 'ui.select', 'ngMessages', 'blockUI', 'datasets-api', 'auth', 'bmsAuth', 'studyState',
		'export-study', 'import-study', 'create-sample', 'derived-variable', 'importObservationsApp', 'germplasm-study-source', 'sample-genotype-tab',
		'germplasmDetailsModule', 'pascalprecht.translate']);

	manageTrialApp.config(['$httpProvider', function ($httpProvider) {
		$httpProvider.interceptors.push('authInterceptor');
		$httpProvider.interceptors.push('authExpiredInterceptor');
	}]);

	manageTrialApp.config(['localStorageServiceProvider', function (localStorageServiceProvider) {
		localStorageServiceProvider.setPrefix('bms');
	}]);

	manageTrialApp.config(['blockUIConfig', function (blockUIConfig) {
		blockUIConfig.templateUrl = '/Fieldbook/static/angular-templates/blockUiTemplate.html';
	}]);

	/*** Added to prevent Unsecured HTML error
	 It is used by ng-bind-html ***/
	manageTrialApp.config(function ($sceProvider) {
		$sceProvider.enabled(false);
	});

	manageTrialApp.config(function ($translateProvider) {
		$translateProvider.useSanitizeValueStrategy('sanitize');
		$translateProvider.translations('en', {
			'study.studydetails.action.deprecated.advance.sample': 'Advance sampled plants from plots',
			'study.studydetails.action.deprecated.advance.study': 'Advance study',
			'study.studydetails.action.new.advance.study': 'New Advance Study',
			'advancing.study.mandatory.fields': 'indicates a mandatory field',
			'advancing.study.method': 'METHODS',
			'advancing.study.breeding.method.the.same': 'Breeding Method is the same for each advance',
			'study.advance.sample.breeding.method.label': 'Breeding method',
			'advancing.study.manage.method': 'Manage Methods',
			'advancing.study.method.variate': 'Choose a variate that defines the breeding method for each advance',
			'advancing.study.lines': 'LINES',
			'advancing.study.line.the.same': 'Same number of lines is selected for each plot',
			'advancing.study.number.of.samples.plot': 'Lines Selected per Plot',
			'advancing.study.lines.variate': 'Choose a variate that defines the number of lines selected from each plot',
			'advancing.study.bulks': 'BULKS',
			'advancing.study.all.plots.selected': 'All plots are selected',
			'advancing.study.bulks.variate': 'Choose a variate that defines which plots were selected',
			'study.advance.plants.label': 'PLANTS',
			'study.advance.plants.all.radio': 'All plants are selected',
			'advancing.study.reps': 'REPS',
			'advancing.study.select.all': 'Select All',
			'advancing.study.harvest.information': 'HARVEST DETAILS',
			'advancing.study.harvest.date': 'Harvest Date:',
			'advancing.study.location.information': 'LOCATION DETAILS',
			'common.form.back.text': 'Back',
			'common.form.finish.text': 'Finish'
		});
		$translateProvider.preferredLanguage('en');
	});

	// routing configuration
	// TODO: if possible, retrieve the template urls from the list of constants
	manageTrialApp.config(function ($uiRouterProvider, $stateProvider, $urlRouterProvider) {

		var StickyStates = window['@uirouter/sticky-states'];
		var DSRPlugin = window['@uirouter/dsr'].DSRPlugin;
		$uiRouterProvider.plugin(StickyStates.StickyStatesPlugin);
		$uiRouterProvider.plugin(DSRPlugin);

		$urlRouterProvider.otherwise('/trialSettings');
		$stateProvider

			.state('trialSettings', {
				url: '/trialSettings',
				templateUrl: '/Fieldbook/TrialManager/createTrial/trialSettings',
				controller: 'TrialSettingsCtrl'
			})

			.state('treatment', {
				url: '/treatment',
				templateUrl: '/Fieldbook/TrialManager/createTrial/treatment',
				controller: 'TreatmentCtrl'
			})

			.state('environment', {
				url: '/environment?addtlNumOfEnvironments&displayWarningMessage&timestamp',
				views: {
					environment: {
						controller: 'EnvironmentCtrl',
						templateUrl: '/Fieldbook/TrialManager/createTrial/environment'
					}
				},
				deepStateRedirect: true, sticky: true
			})

			.state('experimentalDesign', {
				url: '/experimentalDesign',
				templateUrl: '/Fieldbook/TrialManager/createTrial/experimentalDesign',
				controller: 'ExperimentalDesignCtrl'
			})

			.state('germplasm', {
				url: '/germplasm',
				views: {
					germplasm: {
						controller: 'GermplasmCtrl',
						templateUrl: '/Fieldbook/TrialManager/createTrial/germplasm'
					}
				},
			})

			.state('germplasmStudySource', {
				url: '/germplasmStudySource',
				views: {
					germplasmStudySource: {
						controller: 'GermplasmStudySourceCtrl',
						templateUrl: '/Fieldbook/static/js/trialmanager/germplasm-study-source/germplasm-study-source-tab.html'
					}
				}
			})

			.state('inventory', {
				url: '/inventory',
				views: {
					inventory: {
						controller: 'InventoryTabCtrl',
						templateUrl: '/Fieldbook/static/js/trialmanager/inventory/inventory-tab.html'
					}
				}
			})

			.state('sampleGenotypes', {
				url: '/sampleGenotypes',
				views: {
					genotype: {
						controller: 'SampleGenotypeCtrl',
						templateUrl: '/Fieldbook/static/js/trialmanager/sample-genotype/sample-genotype-tab.html'
					}
				}
			})

			.state('analysisResults', {
				url: '/analysisResults',
				views: {
					analysisResults: {
						controller: 'AnalysisResultsCtrl',
						templateUrl: '/Fieldbook/static/js/trialmanager/analysis-results/analysis-results-tab.html'
					}
				}
			})

			.state('subObservationTabs', {
				url: '/subObservationTabs/:subObservationTabId',
				views: {
					subObservationTab: {
						controller: 'SubObservationTabCtrl',
						templateUrl: '/Fieldbook/TrialManager/openTrial/subObservationTab'
					}
				},
				params: {
					subObservationTab: null,
					isPendingView: null
				},
				redirectTo: function (trans) {
					var tab = trans.params().subObservationTab;
					if (tab && tab.subObservationSets.length) {
						var subObservationSet = tab.subObservationSets[0];
						return {
							state: 'subObservationTabs.subObservationSets',
							params: {
								subObservationTabId: tab.id,
								subObservationTab: tab,
								subObservationSetId: subObservationSet.id,
								subObservationSet: subObservationSet,
								isPendingView: trans.params().isPendingView
							}
						}
					}
				}
				// , deepStateRedirect: { params: true } // TODO
			})
			.state('subObservationTabs.subObservationSets', {
				url: '/subObservationSets/:subObservationSetId',
				controller: 'SubObservationSetCtrl',
				templateUrl: '/Fieldbook/TrialManager/openTrial/subObservationSet',
				params: {
					subObservationSet: null,
					isPendingView: null
				},
			})
		;

	});

	manageTrialApp.config(['$provide', function ($provide) {
		$provide.decorator('$locale', function ($delegate) {
			var value = $delegate.DATETIME_FORMATS;
			value.SHORTDAY = ["Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"];
			return $delegate;
		});
	}]);

	// common filters
	manageTrialApp.filter('range', function () {
		return function (input, total) {
			total = parseInt(total);
			for (var i = 0; i < total; i++) {
				input.push(i);
			}

			return input;
		};
	});

	//Create a filter for trust url
	manageTrialApp.filter('trustAsResourceUrl', ['$sce', function($sce) {
		return function(val) {
			return $sce.trustAsResourceUrl(val);
		};
	}]);

	// do not switch tab if we have newly imported measurements
	function isTabChangeDisabled() {
		return $('.import-study-data').data('data-import') === '1';
	}

	manageTrialApp.run(
		['$rootScope', '$state', '$stateParams', 'uiSelect2Config', 'VARIABLE_TYPES', '$transitions',
			function ($rootScope, $state, $stateParams, uiSelect2Config, VARIABLE_TYPES, $transitions) {
				$rootScope.VARIABLE_TYPES = VARIABLE_TYPES;

				$transitions.onStart({},
					function (transition) {
						if (isTabChangeDisabled()) {
							transition.abort();
						}
					});

				// It's very handy to add references to $state and $stateParams to the $rootScope
				// so that you can access them from any scope within your applications.For example,
				// <li ui-sref-active="active }"> will set the <li> // to active whenever
				// 'contacts.list' or one of its decendents is active.
				$rootScope.$state = $state;
				$rootScope.$stateParams = $stateParams;

				uiSelect2Config.placeholder = 'Please Choose';
				uiSelect2Config.minimumResultsForSearch = 20;
				uiSelect2Config.allowClear = false;
			}
		]
	);

	let inventoryChangedDeRegister = () => {
	};
	let germplasmListSavedRegister = () => {
	};
	let genotypesImportSavedRegister = () => {
	};

	// THE parent controller for the manageTrial (create/edit) page
	manageTrialApp.controller('manageTrialCtrl', ['$scope', '$rootScope', 'studyStateService', 'TrialManagerDataService', '$http',
		'$timeout', '_', '$localStorage', '$state', '$window', '$location', 'HasAnyAuthorityService', 'derivedVariableService', 'exportStudyModalService',
		'importStudyModalService', 'createSampleModalService', 'derivedVariableModalService', '$uibModal', '$q', 'datasetService', 'InventoryService',
		'studyContext', 'PERMISSIONS', 'LABEL_PRINTING_TYPE', 'HAS_LISTS_OR_SUB_OBS', 'HAS_GENERATED_DESIGN', 'germplasmStudySourceService', 'sampleGenotypeService',
		'studyEntryService', 'HAS_MEANS_DATASET', 'advanceStudyModalService', 'STABRAPP_URL', 'DS_BRAPP_URL', 'FEEDBACK_ENABLED',
		function ($scope, $rootScope, studyStateService, TrialManagerDataService, $http, $timeout, _, $localStorage, $state, $window, $location, HasAnyAuthorityService,
				  derivedVariableService, exportStudyModalService, importStudyModalService, createSampleModalService, derivedVariableModalService, $uibModal, $q, datasetService, InventoryService,
				  studyContext, PERMISSIONS, LABEL_PRINTING_TYPE, HAS_LISTS_OR_SUB_OBS, HAS_GENERATED_DESIGN, germplasmStudySourceService, sampleGenotypeService, studyEntryService, HAS_MEANS_DATASET, advanceStudyModalService,
				  STABRAPP_URL, DS_BRAPP_URL, FEEDBACK_ENABLED) {

			$scope.dsBrappURL = DS_BRAPP_URL;
			$scope.staBrappURL = STABRAPP_URL;

			$scope.germplasmDetailsHasChanges = false;
			$window.addEventListener("message", (event) => {
				if (event.data === 'germplasm-details-changed') {
					// If any germplasm info is changed in germplasm details popup (basic-details, name, attribute, pedigree)
					// set the germplasmDetailsHasChanges flag to true
					$scope.germplasmDetailsHasChanges = true;

					// Reload Germplasm Details Modal
					const germplasmDetailsModalService = angular.element('#mainApp').injector().get('germplasmDetailsModalService');
					if (germplasmDetailsModalService.modal) {
						germplasmDetailsModalService.updateGermplasmDetailsModal();
					}

					if ($scope.tabSelected == $scope.crossesAndSelectionsTab.state) {
						// Reload Crosses and Selection if selected
						$rootScope.navigateToTab('germplasmStudySource', {reload: true});
					}

				} else if (event.data === 'observations-changed') {
					$rootScope.$broadcast('observationsChanged');
				}
			}, false);

			$scope.reloadViewIfGermplasmDetailsChanged = function () {
				// If any germplasm info is changed in germplasm details popup (basic-details, name, attribute, pedigree)
				// then refresh the current view to immediate reflect the changes in germplasm.
				if ($scope.germplasmDetailsHasChanges) {
					$state.reload();
					$scope.germplasmDetailsHasChanges = false;
				}
			}

			$scope.trialTabs = [];
			$scope.subObservationTabs = [];
			$scope.tabSelected = '';
			$scope.isSettingsTab = false;
			$scope.sampleTabsData = [];
			$scope.sampleTabs = [];
			$scope.isOpenStudy = TrialManagerDataService.isOpenStudy;
			$scope.isLockedStudy = TrialManagerDataService.isLockedStudy;
			$scope.studyTypes = [];
			$scope.studyTypeSelected = undefined;
			$scope.isChoosePreviousStudy = false;
			$scope.STABRAPP_URL = STABRAPP_URL;
			$scope.FEEDBACK_ENABLED = FEEDBACK_ENABLED;

			$scope.hasAnyAuthority = HasAnyAuthorityService.hasAnyAuthority;
			$scope.PERMISSIONS = PERMISSIONS;
			$scope.hasDesignGenerated = HAS_GENERATED_DESIGN;

			$scope.returnToManageStudiesURL = "/ibpworkbench/controller/jhipster#/study-manager?cropName=" + studyContext.cropName + "&programUUID=" + studyContext.programId;

			$scope.crossesAndSelectionsTab = {
				name: 'Crosses and Selections',
				state: 'germplasmStudySource',
				hidden: true,
				permission: PERMISSIONS.VIEW_CROSSES_AND_SELECTIONS_PERMISSIONS
			}
			$scope.sampleGenotypesTab = {
				name: 'Sample Genotypes',
				state: 'sampleGenotypes',
				hidden: true,
				permission: PERMISSIONS.VIEW_SAMPLE_GENOTYPES_PERMISSIONS
			}
			$scope.inventoryTab = {
				name: 'Inventory',
				state: 'inventory',
				hidden: true,
				permission: PERMISSIONS.VIEW_INVENTORY_PERMISSIONS
			};
			$scope.analysisResultsTab = {
				name: 'SSA Results',
				state: 'analysisResults',
				hidden: true,
				permission: PERMISSIONS.VIEW_SINGLE_SITE_ANALYSIS_RESULTS_PERMISSIONS
			};

			$scope.trialTabs.push(
				{
					name: 'Settings',
					state: 'trialSettings',
					permission: PERMISSIONS.VIEW_STUDY_SETTINGS_PERMISSIONS
				}
			);

			if ($scope.isOpenStudy()) {
				$scope.trialTabs.push({
					name: 'Germplasm & Checks',
					state: 'germplasm',
					permission: PERMISSIONS.VIEW_GERMPLASM_AND_CHECKS_PERMISSIONS

				});
				$scope.trialTabs.push({
					name: 'Treatment Factors',
					state: 'treatment',
					permission: PERMISSIONS.VIEW_TREATMENT_FACTORS_PERMISSIONS
				});
				$scope.trialTabs.push({
					name: 'Environments',
					state: 'environment',
					permission: PERMISSIONS.VIEW_ENVIRONMENT_PERMISSIONS
				});
				$scope.trialTabs.push({
					name: 'Experimental Design',
					state: 'experimentalDesign',
					permission: PERMISSIONS.VIEW_EXPERIMENTAL_DESIGN_PERMISSIONS
				});

				$scope.trialTabs.push($scope.inventoryTab);
				$scope.trialTabs.push($scope.analysisResultsTab);

				loadCrossesAndSelectionsTab();
				loadSampleGenotypesTab();
				loadInventoryTab();
				loadAnalysisResultsTab();

				studyStateService.updateHasListsOrSubObs(HAS_LISTS_OR_SUB_OBS);
				studyStateService.updateGeneratedDesign(HAS_GENERATED_DESIGN);
				studyStateService.updateHasMeansDataset(HAS_MEANS_DATASET);

				if (HAS_GENERATED_DESIGN) {
					studyEntryService.getStudyEntriesMetadata().then(function (metadata) {
						if (metadata.hasUnassignedEntries) {
							showAlertMessage('', $.fieldbookMessages.studyEntryUnassignedWarning);
						}
					});
				}
			}

			inventoryChangedDeRegister();
			inventoryChangedDeRegister = $rootScope.$on("inventoryChanged", function () {
				if ($scope.hasAnyAuthority(PERMISSIONS.VIEW_INVENTORY_PERMISSIONS)) {
					loadInventoryTab();
				}
			});

			germplasmListSavedRegister();
			germplasmListSavedRegister = $rootScope.$on("germplasmListSaved", function () {
				loadCrossesAndSelectionsTab();
			});

			genotypesImportSavedRegister();
			genotypesImportSavedRegister = $rootScope.$on("genotypesImportSaved", function () {
				loadSampleGenotypesTab();
			});

			function loadCrossesAndSelectionsTab() {
				germplasmStudySourceService.searchGermplasmStudySources({}, 0, 1).then((germplasmStudySourceTable) => {
					if (germplasmStudySourceTable.length) {
						$scope.crossesAndSelectionsTab.hidden = false;
						if ($scope.tabSelected === '' && $scope.hasAnyAuthority($scope.crossesAndSelectionsTab.permission)) {
							$scope.tabSelected = $scope.crossesAndSelectionsTab.state;
							$rootScope.navigateToTab($scope.tabSelected, {reload: true});

						}
					}
				});
			}

			function loadSampleGenotypesTab() {
				sampleGenotypeService.searchSampleGenotypes({}, 0, 1).then((sampleGenotypesTable) => {
					if (sampleGenotypesTable.length) {
						$scope.sampleGenotypesTab.hidden = false;
					}
				});
			}

			function loadInventoryTab() {
				InventoryService.searchStudyTransactions({}, 0, 1).then((transactionsTable) => {
					$scope.safeApply(function () {
						$scope.inventoryTab.hidden = !transactionsTable.length;
						// If the Inventory tab becomes hidden, if no transactions left, navigate to Observations tab to show its content
						if ($scope.inventoryTab.hidden && $scope.tabSelected === 'inventory') {
							$scope.navigateToSubObsTab(studyContext.measurementDatasetId);
						} else if ($scope.tabSelected === '' &&
							!$scope.inventoryTab.hidden &&
							$scope.hasAnyAuthority($scope.inventoryTab.permission)) {
							$scope.tabSelected = $scope.inventoryTab.state;
							$rootScope.navigateToTab($scope.tabSelected, {reload: true});
						}
					});
				});
			}

			function loadAnalysisResultsTab() {
				if (HAS_MEANS_DATASET) {
					$scope.analysisResultsTab.hidden = false;
					if ($scope.tabSelected === '' &&
						!$scope.analysisResultsTab.hidden &&
						$scope.hasAnyAuthority($scope.analysisResultsTab.permission)) {
						$scope.tabSelected = $scope.analysisResultsTab.state;
						$rootScope.navigateToTab($scope.tabSelected, {reload: true});
					}
				}
			}

			$http.get('/bmsapi/crops/' + cropName + '/study-types/visible?programUUID=' + studyContext.programId).success(function (data) {
				$scope.studyTypes = data;

			}).error(function (data) {
				showErrorMessage('', data.error.message);
			});

			$scope.changeSelectStudyType = function (studyTypeSelected) {
				angular.forEach($scope.studyTypes, function (studyType) {
					if (studyType.id == studyTypeSelected) {
						$scope.data.studyType = studyType.name;
						return;
					}
				});
			};

			$scope.toggleChoosePreviousStudy = function () {
				$scope.isChoosePreviousStudy = !$scope.isChoosePreviousStudy;
			};

			$scope.resetTabsData = function () {
				if ($localStorage.serviceBackup) {
					// reset the service data to initial state (for untick of user previous study)
					_.each(_.keys($localStorage.serviceBackup.settings), function (key) {
						if ('basicDetails' !== key) {
							TrialManagerDataService.updateSettings(key, angular.copy($localStorage.serviceBackup.settings[key]));
						}
					});

					_.each(_.keys($localStorage.serviceBackup.currentData), function (key) {
						if ('basicDetails' !== key) {
							TrialManagerDataService.updateCurrentData(key, angular.copy($localStorage.serviceBackup.currentData[key]));
						}
					});

					TrialManagerDataService.applicationData = angular.copy($localStorage.serviceBackup.applicationData);
				}

				// perform other cleanup tasks
				$http({
					url: '/Fieldbook/TrialManager/createTrial/clearSettings',
					method: 'GET',
					transformResponse: undefined
				}).then(function (response) {
					if (response.data !== 'success' || response.status !== 200) {
						showErrorMessage('', 'Your study settings could not be cleared at the moment. Please try again later.');
					}
				});

				if (typeof resetGermplasmList !== 'undefined') {
					resetGermplasmList();
				}
				TrialManagerDataService.resetServiceBackup();
			};

			// To apply scope safely
			$scope.safeApply = function (fn) {
				var phase = this.$root.$$phase;
				if (phase === '$apply' || phase === '$digest') {
					if (fn && (typeof (fn) === 'function')) {
						fn();
					}
				} else {
					this.$apply(fn);
				}
			};
			$scope.data = TrialManagerDataService.currentData.basicDetails;

			$scope.warnMissingInputData = function (response) {
				var deferred = $q.defer();
				if (response && response.data.length > 0) {
					$uibModal.open({
						animation: true,
						templateUrl: '/Fieldbook/static/angular-templates/derivedTraitsValidationModal.html',
						size: 'md',
						controller: function ($scope, $uibModalInstance) {
							$scope.dependencyVariables = response.data;
							$scope.continue = function () {
								$uibModalInstance.close();
								deferred.resolve();
							};
						}
					});
				} else {
					deferred.resolve();
				}
				return deferred.promise;
			};

			$scope.saveCurrentTrialData = function () {
				TrialManagerDataService.saveCurrentData();
			};

			$scope.selectPreviousStudy = function () {
				openStudyTree(3, $scope.useExistingStudy);
			};

			$scope.changeFolderLocation = function () {
				openStudyTree(2, TrialManagerDataService.updateSelectedFolder);
			};

			$scope.useExistingStudy = function (existingStudyId) {
				$http.get('/Fieldbook/TrialManager/createTrial/useExistingStudy?studyId=' + existingStudyId).success(function (data) {
					// update data and settings
					if (data.createTrialForm !== null && data.createTrialForm.hasError === true) {
						$scope.resetTabsData();
						createErrorNotification(errorMsgHeader, data.createTrialForm.errorMessage);
					} else {
						TrialManagerDataService.storeInitialValuesInServiceBackup();
						var instanceInfo = TrialManagerDataService.extractData(data.environmentData);
						var environmentSettings = TrialManagerDataService.extractSettings(data.environmentData);

						if (instanceInfo.numberOfInstances > 0 && instanceInfo.instances.length === 0) {
							while (instanceInfo.instances.length !== instanceInfo.numberOfInstances) {
								instanceInfo.instances.push({
									managementDetailValues: TrialManagerDataService.constructDataStructureFromDetails(
										environmentSettings.managementDetails),
									trialDetailValues: TrialManagerDataService.constructDataStructureFromDetails(
										environmentSettings.trialConditionDetails)
								});
							}
						}

						// update Select StudyType.
						angular.forEach($scope.studyTypes, function (studyType) {
								if (studyType.label === data.createTrialForm.studyTypeName) {
									$scope.changeSelectStudyType(studyType.id);
									$('#studyTypeId').val("number:" + studyType.id.toString());
									return;
								}
							}
						);

						TrialManagerDataService.updateCurrentData('trialSettings',
							TrialManagerDataService.extractData(data.trialSettingsData));
						TrialManagerDataService.updateCurrentData('instanceInfo', instanceInfo);
						TrialManagerDataService.updateCurrentData('treatmentFactors', TrialManagerDataService.extractData(
							data.treatmentFactorsData));

						//Added-selectionVariates
						TrialManagerDataService.updateSettings('trialSettings', TrialManagerDataService.extractSettings(
							data.trialSettingsData));
						TrialManagerDataService.updateSettings('environments', environmentSettings);
						TrialManagerDataService.updateSettings('germplasm', TrialManagerDataService.extractSettings(data.germplasmData));
						TrialManagerDataService.updateSettings('treatmentFactors', TrialManagerDataService.extractTreatmentFactorSettings(
							data.treatmentFactorsData));
					}
				});
			};

			$scope.refreshTabAfterImport = function () {
				$http.get('/Fieldbook/TrialManager/createTrial/refresh/settings/tab').success(function (data) {
					// update data and settings

					var environmentData = TrialManagerDataService.extractData(data.environmentData);
					TrialManagerDataService.updateCurrentData('trialSettings', TrialManagerDataService.extractData(data.trialSettingsData));
					TrialManagerDataService.updateCurrentData('environments', environmentData);
				});
			};

			$scope.temp = {
				noOfEnvironments: 0
			};

			$scope.refreshEnvironmentsAndExperimentalDesign = function () {
				var currentDesignType = TrialManagerDataService.currentData.experimentalDesign.designType;
				var showIndicateUnappliedChangesWarning = true;

				var designTypes = TrialManagerDataService.applicationData.designTypes;

				if (TrialManagerDataService.getDesignTypeById(currentDesignType, designTypes).name === 'Custom Import Design') {
					showIndicateUnappliedChangesWarning = false;
					ImportDesign.showPopup(ImportDesign.hasGermplasmListSelected());
					showAlertMessage('', addEnvironmentsImportDesignMessage, 5000);
				}

				$state.go('environment', {
					addtlNumOfEnvironments: $scope.temp.noOfEnvironments,
					displayWarningMessage: showIndicateUnappliedChangesWarning, timestamp: new Date()
				});

				TrialManagerDataService.applicationData.hasNewInstanceAdded = true;

				$state.go('environment', {addtlNumOfEnvironments: $scope.temp.noOfEnvironments, timestamp: new Date()});
				$scope.performFunctionOnTabChange('environment');

			};

			$scope.loadMeasurementsTabInBackground = function () {
				if (isOpenStudy()) {
					$state.go('editMeasurements', {}, {location: false});
				}

			};

			$scope.hasGermplasmListSelected = function () {
				return TrialManagerDataService.applicationData.germplasmListSelected;
			};

			$scope.showPreparePlantingInventoryAction = function () {
				return $scope.hasDesignGenerated && $scope.hasAnyAuthority(PERMISSIONS.PREPARE_PLANTING_PERMISSIONS);
			}

			$scope.showPrintingLabelsAction = function () {
				return $scope.hasDesignGenerated && $scope.hasAnyAuthority(PERMISSIONS.CREATE_PLANTING_LABELS_PERMISSIONS);
			}

			$scope.showImportOwnDesignAction = function () {
				return $scope.hasGermplasmListSelected() && $scope.hasAnyAuthority(PERMISSIONS.GENERATE_EXPERIMENTAL_DESIGN_PERMISSIONS);
			}

			$scope.showExportDesignTemplateAction = function () {
				return $scope.hasAnyAuthority(PERMISSIONS.EXPORT_DESIGN_TEMPLATE_PERMISSIONS);
			}

			$scope.showLockStudyAction = function () {
				return !$scope.isLockedStudy() && $scope.userHasLockPermission() && $scope.hasAnyAuthority(PERMISSIONS.LOCK_STUDY_PERMISSIONS);
			}

			$scope.showLockUnStudyAction = function () {
				return $scope.isLockedStudy() && $scope.userHasLockPermission() && $scope.hasAnyAuthority(PERMISSIONS.LOCK_STUDY_PERMISSIONS);
			}

			$scope.showDesignAndPlanningOptions = function () {
				return $scope.showExportDesignTemplateAction() || //
					$scope.showImportOwnDesignAction() || //
					$scope.showPrintingLabelsAction() || //
					$scope.showPreparePlantingInventoryAction(); //
			}

			$scope.showCreateLotsAction = function () {
				return $scope.hasDesignGenerated &&
					$scope.hasAnyAuthority(PERMISSIONS.CREATE_LOTS_PERMISSIONS);
			}

			$scope.displayExecuteCalculatedVariableOnlyActions = function () {
				return $scope.hasAnyAuthority(PERMISSIONS.EXECUTE_CALCULATED_VARIABLES_PERMISSIONS) && derivedVariableService.isStudyHasCalculatedVariables && $scope.hasDesignGenerated;
			};


			// Programatically navigate to specified tab state
			$rootScope.navigateToTab = function (targetState, options) {
				$state.go(targetState, {}, {reload: options && options.reload});
				$scope.performFunctionOnTabChange(targetState);

			};

			$rootScope.navigateToSubObsTab = function (datasetId, options) {
				var subObsTab = undefined;
				var subObsSet = undefined;
				angular.forEach($scope.subObservationTabs, function (subObservationTab) {
					angular.forEach(subObservationTab.subObservationSets, function (subObservationSet) {
						if (subObservationSet.id === datasetId) {
							subObsSet = subObservationSet;
							subObsTab = subObservationTab;
						}
					});
				});

				$scope.isSettingsTab = false;
				$scope.tabSelected = subObsTab.state;
				return $state.transitionTo('subObservationTabs.subObservationSets', {
					subObservationTabId: subObsTab.id,
					subObservationTab: subObsTab,
					subObservationSetId: subObsSet.id,
					subObservationSet: subObsSet,
					isPendingView: options && options.isPendingView
				}, {
					reload: options && options.reload, inherit: false, notify: true
				});
			};

			$scope.performFunctionOnTabChange = function (targetState) {
				if (isTabChangeDisabled()) {
					showAlertMessage('', importSaveDataWarningMessage);
					return;
				}

				$scope.isSettingsTab = true;
				$scope.tabSelected = targetState;

				// we need to redraw the columns of the table on tab change as they appear all to be squeezed to the left corner
				// of the table if we do not do that
				function adjustColumns($table) {
					if ($table.length !== 0 && $table.dataTable()) {
						$timeout(function () {
							$table.dataTable().fnAdjustColumnSizing();
						});
					}
				}

				if (targetState === 'germplasm') {
					adjustColumns($('#tableForGermplasm'));
				} else if (targetState === 'environment') {
					adjustColumns($('#environment-table .fbk-datatable-environments'));
				} else if (targetState.indexOf('/subObservationTabs/') === 0) {
					$rootScope.$broadcast('subObsTabSelected');
				} else if (targetState === 'inventory') {
					$rootScope.$broadcast('inventoryChanged');
				}
			};

			$scope.addSampleTabData = function (tabId, tabData, listName, isPageLoading) {
				studyStateService.updateHasListsOrSubObs(true);

				var isSwap = false;
				var isUpdate = false;
				if (isPageLoading === undefined) {
					isPageLoading = false;
				}
				angular.forEach($scope.sampleTabs, function (value, index) {
						if (!isUpdate && value.name === listName && parseInt(value.id) === parseInt(tabId)) {
							isUpdate = true;
							$scope.sampleTabsData[index].data = tabData;

						}
					}
				);

				if (!isSwap && !isUpdate) {
					$scope.sampleTabs.push({
						name: listName,
						state: 'sample-list' + tabId + '-li',
						id: tabId,
						displayName: 'Sample List: [' + listName + ']'
					});
					$scope.sampleTabsData.push({
						name: 'sample-list' + tabId + '-li',
						data: tabData,
						id: 'sample-list' + tabId + '-li'
					});
					if (isPageLoading !== true) {
						$scope.tabSelected = 'sample-list' + tabId + '-li';
						$scope.isSettingsTab = false;
						$rootScope.$broadcast('sampleListCreated');
					}
				}
			};

			$scope.addSubObservationTabData = function (id, name, datasetTypeId, parentDatasetId) {
				var datasetType = datasetService.getDatasetType(datasetTypeId);
				studyStateService.updateHasListsOrSubObs(true);

				var newSubObsTab = {
					id: id,
					name: name,
					datasetType: datasetType,
					state: '/subObservationTabs/' + id, // arbitrary prefix to filter tab content
					subObservationSets: [{
						id: id,
						name: name,
						datasetTypeId: datasetTypeId,
						parentDatasetId: parentDatasetId
					}]
				};

				$scope.subObservationTabs.push(newSubObsTab);
				var params = {subObservationTabId: id, subObservationTab: newSubObsTab};

				$scope.isSettingsTab = false;
				$scope.tabSelected = newSubObsTab.state;
				$state.go('subObservationTabs', params);

			};

			datasetService.getDatasets().then(function (data) {
				/**
				 * Restructure list from server based on parentDatasetId (can be null)
				 * Example:
				 *
				 *         plotdata+--------------------+
				 *            +                         |
				 *            v                         v
				 *    plants-dataset+---+        timeseries-dataset
				 *            +         |
				 *            v         v
				 *  fruits-dataset    leafs-datasets
				 *
				 *                          +
				 *                          |   transform into tabs
				 *                          v
				 *
				 * +-------------+-----------------+---------------------+
				 * |   plotdata  |  plants-dataset | timeseries-dataset  |
				 * +-------------+----------+------+---------------------+
				 *                          |
				 *  +-----------------------+
				 *  |
				 * +v--------------+-----------------+----------------+
				 * |plants-dataset |  fruits-dataset | leafs-datasets |
				 * +---------------+-----------------+----------------+
				 *
				 */

					// utility maps to easily get what we want
				var datasetByParent = {};
				var datasetById = {};
				angular.forEach(data, function (dataset) {
					datasetByParent[dataset.parentDatasetId] = dataset;
					datasetById[dataset.datasetId] = dataset;
				});

				// restructure in tabs - a second iteration is needed once we have the full byParent map
				var datasetByTabs = {};
				angular.forEach(data, function (dataset) {
					var parent = dataset;
					// subobservation sets can be nested
					while (parent.parentDatasetId && datasetById[parent.parentDatasetId]) {
						parent = datasetById[parent.parentDatasetId];
					}
					datasetByTabs[parent.datasetId] = datasetByTabs[parent.datasetId] || [];
					datasetByTabs[parent.datasetId].push(dataset);
				});

				var observationTabs = data.filter(function (dataset) {
					// those whose parent is not in the list are considered roots
					return !datasetById[dataset.parentDatasetId];
				});

				angular.forEach(observationTabs, function (datasetTab) {
					var datasetType = datasetService.getDatasetType(datasetTab.datasetTypeId);
					$scope.subObservationTabs.push({
						id: datasetTab.datasetId,
						name: datasetTab.name,
						datasetType: datasetType,
						hasPendingData: datasetTab.hasPendingData,
						state: '/subObservationTabs/' + datasetTab.datasetId, // arbitrary prefix to filter tab content
						subObservationSets: datasetByTabs[datasetTab.datasetId].map(function (dataset) {
							return {
								id: dataset.datasetId,
								name: dataset.name,
								hasPendingData: dataset.hasPendingData,
								datasetTypeId: dataset.datasetTypeId,
								parentDatasetId: dataset.parentDatasetId
							}
						})
					});
				});
				const trialTabSelected =  $scope.trialTabs.find((tab) => !tab.hidden && $scope.hasAnyAuthority(tab.permission));
				if (!!trialTabSelected) {
					$scope.tabSelected = trialTabSelected.state
					$scope.isSettingsTab = $scope.tabSelected === 'trialSettings';
					$rootScope.navigateToTab($scope.tabSelected, {reload: true});
				} else if (!!$scope.subObservationTabs.length && $scope.hasAnyAuthority(PERMISSIONS.VIEW_OBSERVATIONS_PERMISSIONS)) {
					$scope.isSettingsTab = false;
					$scope.navigateToSubObsTab(studyContext.measurementDatasetId);
				} else if (!!$scope.sampleTabsData.length && $scope.hasAnyAuthority(PERMISSIONS.VIEW_SAMPLE_LISTS_PERMISSIONS)) {
					$scope.tabSelected = $scope.sampleTabs[0].state;
					$scope.isSettingsTab = false;
					$scope.listTabChange($scope.sampleTabs[0].state);
					$timeout(function () {
						$('#sample-list-' + $scope.sampleTabs[0].id).dataTable().fnAdjustColumnSizing();
					}, 1);
				}
			}, function (response) {
				if (response.errors[0] && response.errors[0].message) {
					showErrorMessage('', response.errors[0].message);
				} else {
					showErrorMessage('', ajaxGenericErrorMsg);
				}
			});

			$scope.sampleList = TrialManagerDataService.settings.sampleList;

			angular.forEach($scope.sampleList, function (value) {
				displaySampleList(value.listId, value.listName, true);
			});

			$scope.listTabChange = function (selectedTab) {
				if (isTabChangeDisabled()) {
					showAlertMessage('', importSaveDataWarningMessage);
					return;
				}
				$scope.tabSelected = selectedTab;
				$scope.isSettingsTab = false;

			};

			$scope.closeSampleListTab = function (tab) {
				var index = $scope.findIndexByKeyValue($scope.sampleTabs, 'state', tab);
				$scope.sampleTabs.splice(index, 1);
				$scope.sampleTabsData.splice(index, 1);
				$scope.tabSelected = 'trialSettings';
				$scope.isSettingsTab = true;
				$rootScope.navigateToTab('trialSettings', {reload: true});
			};

			$scope.initSampleTab = function (tab) {
				$timeout(function () {
					$('#sample-list-' + tab.id).dataTable().fnAdjustColumnSizing();
				}, 1);
			};

			$scope.userHasLockPermission = function () {
				return $scope.data.userID === currentCropUserId || isSuperAdmin;
			};

			$scope.changeLockedStatus = function (doLock) {
				TrialManagerDataService.changeLockedStatus(doLock);
			};

			$scope.isSaveDisabled = function () {
				return !$scope.hasAnyAuthority(PERMISSIONS.FULL_MANAGE_STUDIES_PERMISSIONS)
				|| (!$scope.isSaveEnabled() && !studyStateService.hasUnsavedData());
			};

			$scope.hasUnsavedData = function () {
				return $scope.hasAnyAuthority(PERMISSIONS.FULL_MANAGE_STUDIES_PERMISSIONS) && studyStateService.hasUnsavedData();
			}

			$scope.isSaveEnabled = function () {
				return $scope.hasAnyAuthority(PERMISSIONS.FULL_MANAGE_STUDIES_PERMISSIONS)
					&& $scope.tabSelected && ([
						"trialSettings",
						"treatment"
					].indexOf($scope.tabSelected) >= 0) || !studyContext.studyId;
			};

			$scope.showCreateSubObservationUnitsAction = function () {
				return $scope.hasAnyAuthority(PERMISSIONS.CREATE_SUB_OBSERVATION_UNITS_PERMISSIONS) && $scope.hasDesignGenerated;
			}

			$scope.showChangePlotEntryAction = function () {
				return $scope.hasAnyAuthority(PERMISSIONS.CHANGE_PLOT_ENTRY_PERMISSIONS) && $scope.hasDesignGenerated;
			}

			$scope.showSubObservationUnitOptionsAction = function () {
				return $scope.showCreateSubObservationUnitsAction() || $scope.showChangePlotEntryAction();
			}

			$scope.showExportStudyBookAction = function () {
				return $scope.hasAnyAuthority(PERMISSIONS.EXPORT_STUDY_BOOK_PERMISSIONS) && $scope.hasDesignGenerated;
			}

			$scope.showExportStudyEntriesAction = function () {
				return $scope.hasAnyAuthority(PERMISSIONS.EXPORT_STUDY_ENTRIES_PERMISSIONS) && $scope.hasGermplasmListSelected();
			}

			$scope.showImportObservationsAction = function () {
				return $scope.hasAnyAuthority(PERMISSIONS.MANAGE_PENDING_OBSERVATION_VALUES_PERMISSIONS) && $scope.hasDesignGenerated;
			}

			$scope.showDataCollectionAction = function () {
				return $scope.showExportStudyBookAction() || //
					$scope.showImportObservationsAction() || //
					$scope.showExportStudyEntriesAction(); //
			}


			$scope.showCreateGenotypingSamplesAction = function () {
				return $scope.hasAnyAuthority(PERMISSIONS.CREATE_GENOTYPING_SAMPLES_PERMISSIONS) && $scope.hasDesignGenerated;
			}

			$scope.showAdvancesAction = function () {
				return $scope.hasAnyAuthority(PERMISSIONS.ADVANCES_PERMISSIONS) && $scope.hasDesignGenerated;
			}

			$scope.showAdvanceStudyAction = function () {
				return $scope.hasAnyAuthority(PERMISSIONS.ADVANCE_STUDY_PERMISSIONS) && $scope.hasDesignGenerated;
			}

			$scope.showAdvanceStudyForPlantsAction = function () {
				return $scope.hasAnyAuthority(PERMISSIONS.ADVANCE_STUDY_FOR_PLANTS_PERMISSIONS) && $scope.hasDesignGenerated;
			}

			$scope.showAnalyzeWithStarBrappAction = function () {
				return $scope.staBrappURL && $scope.hasAnyAuthority(PERMISSIONS.ANALYZE_WITH_STA_BRAPP_PERMISSIONS);
			}

			$scope.showAnalyzeWithDecisionSupportAction = function () {
				return $scope.dsBrappURL && $scope.hasAnyAuthority(PERMISSIONS.ANALYZE_WITH_DECISION_SUPPORT_PERMISSIONS);
			}

			$scope.showFieldMapOptionsAction = function () {
				return $scope.showMakeFieldMapAction() || //
					$scope.showViewFieldMapAction() || //
					$scope.showDeleteFieldMapAction() || //
					$scope.showCreateGeoreferenceAction() || //
					$scope.showEditGeoreferenceAction();
			}

			$scope.showMakeFieldMapAction = function () {
				return $scope.hasAnyAuthority(PERMISSIONS.MAKE_FIELD_MAP_PERMSISSION) && $scope.hasDesignGenerated;
			}

			$scope.showViewFieldMapAction = function () {
				return $scope.hasAnyAuthority(PERMISSIONS.VIEW_FIELD_MAP_PERMSISSION) && $scope.hasDesignGenerated;
			}

			$scope.showDeleteFieldMapAction = function () {
				return $scope.hasAnyAuthority(PERMISSIONS.DELETE_FIELD_MAP_PERMSISSION) && $scope.hasDesignGenerated;
			}

			$scope.showCreateGeoreferenceAction = function () {
				return $scope.hasAnyAuthority(PERMISSIONS.CREATE_GEOREFERENCE_PERMSISSION) && $scope.hasDesignGenerated;
			}

			$scope.showEditGeoreferenceAction = function () {
				return $scope.hasAnyAuthority(PERMISSIONS.EDIT_GEOREFERENCE_PERMSISSION) && $scope.hasDesignGenerated;
			}

			$scope.showCrossingOptionsAction = function () {
				return $scope.showExportCrossingTemplateAction() ||
					$scope.showImportCrossesAction() ||
					$scope.showDesignNewCrossesAction();
			}

			$scope.showExportCrossingTemplateAction = function () {
				return $scope.hasAnyAuthority(PERMISSIONS.EXPORT_CROSSING_TEMPLATE_PERMISSIONS) && $scope.hasDesignGenerated;
			}

			$scope.showImportCrossesAction = function () {
				return $scope.hasAnyAuthority(PERMISSIONS.IMPORT_CROSSES_PERMISSIONS) && $scope.hasDesignGenerated;
			}

			$scope.showDesignNewCrossesAction = function () {
				return $scope.hasAnyAuthority(PERMISSIONS.DESIGN_NEW_CROSSES_PERMISSIONS) && $scope.hasDesignGenerated;
			}

			$scope.showStudyAction = function () {
				return $scope.hasAnyAuthority(PERMISSIONS.CLOSE_STUDY_PERMISSIONS) ||
					$scope.hasAnyAuthority(PERMISSIONS.DELETE_STUDY_PERMISSIONS) ||
					$scope.showDesignAndPlanningOptions() ||
					$scope.showCrossingOptionsAction() ||
					$scope.showSubObservationUnitOptionsAction() ||
					$scope.showFieldMapOptionsAction() ||
					$scope.showDataCollectionAction() ||
					$scope.displayExecuteCalculatedVariableOnlyActions() ||
					$scope.showCreateGenotypingSamplesAction() ||
					$scope.showAdvancesAction() ||
					$scope.showCreateLotsAction() ||
					$scope.showLockStudyAction() ||
					$scope.showLockUnStudyAction() ||
					$scope.showAnalyzeWithStarBrappAction() ||
					$scope.showAnalyzeWithDecisionSupportAction();
			}

			$('body').on('DO_AUTO_SAVE', function () {
				TrialManagerDataService.saveCurrentData();
			});
			$('body').on('REFRESH_AFTER_IMPORT_SAVE', function () {
				$scope.refreshTabAfterImport();
			});
			$scope.findIndexByKeyValue = function (arraytosearch, key, valuetosearch) {
				for (var i = 0; i < arraytosearch.length; i++) {
					if (arraytosearch[i][key] === valuetosearch) {
						return i;
					}
				}
				return null;
			};

			$rootScope.openConfirmModal = function (message, confirmButtonLabel, cancelButtonLabel) {

				var modalInstance = $uibModal.open({
					animation: true,
					templateUrl: '/Fieldbook/static/angular-templates/confirmModal.html',
					windowClass: 'force-zindex', // make sure that the modal is always in front of all modals
					controller: function ($scope, $uibModalInstance) {
						$scope.text = message;
						$scope.confirmButtonLabel = confirmButtonLabel || okLabel;
						$scope.cancelButtonLabel = cancelButtonLabel || cancelLabel;

						$scope.confirm = function () {
							$uibModalInstance.close(true);
						};

						$scope.cancel = function () {
							$uibModalInstance.close(false);
						};
					}
				});
				return modalInstance;
			};

			$rootScope.openGermplasmSelectorModal = function (selectMultiple) {
				return $uibModal.open({
					templateUrl: '/Fieldbook/static/js/trialmanager/germplasm-selector/germplasm-selector-modal.html',
					controller: "GermplasmSelectorCtrl",
					windowClass: 'modal-very-huge',
					resolve: {
						selectMultiple: function () {
							return selectMultiple;
						}
					}
				}).result;
			};

			$scope.showExportStudyModal = function () {
				exportStudyModalService.openDatasetOptionModal();
			}

			$scope.showImportStudyModal = function () {
				importStudyModalService.openDatasetOptionModal();
			}

			$scope.printLabels = function () {
				$uibModal.open({
					template: '<dataset-option-modal modal-title="modalTitle" message="message"' +
						' selected="selected" on-continue="forkPrintLabelFlows()"></dataset-option-modal>',
					size: 'md',
					controller: ['$scope', 'studyContext', function (scope, studyContext) {

						scope.modalTitle = 'Create planting labels';
						scope.message = 'Please choose the dataset you would like to print from:';
						scope.selected = {datasetId: studyContext.measurementDatasetId};

						scope.forkPrintLabelFlows = function () {
							if (studyContext.measurementDatasetId === scope.selected.datasetId) {
								window.location.href = '/ibpworkbench/controller/jhipster#label-printing' +
									'?cropName=' + studyContext.cropName +
									'&programUUID=' + studyContext.programId +
									'&datasetId=' + scope.selected.datasetId +
									'&studyId=' + studyContext.studyId +
									'&printingLabelType=' + LABEL_PRINTING_TYPE.OBSERVATION_DATASET;
							} else {
								window.location.href = '/ibpworkbench/controller/jhipster#label-printing' +
									'?cropName=' + studyContext.cropName +
									'&programUUID=' + studyContext.programId +
									'&datasetId=' + scope.selected.datasetId +
									'&studyId=' + studyContext.studyId +
									'&printingLabelType=' + LABEL_PRINTING_TYPE.SUBOBSERVATION_DATASET;
							}
						};
					}]
				});
			};

			$scope.exportStudyEntriesPrintingLabels = function () {
				window.location.href = '/ibpworkbench/controller/jhipster#label-printing' +
					'?cropName=' + studyContext.cropName +
					'&programUUID=' + studyContext.programId +
					'&datasetId=' + studyContext.measurementDatasetId +
					'&studyId=' + studyContext.studyId +
					'&printingLabelType=' + LABEL_PRINTING_TYPE.STUDY_ENTRIES;
			}

			$scope.showGeoJSONModal = function (isViewGeoJSON) {
				datasetService.getDatasetInstances(studyContext.measurementDatasetId).then((datasetInstances) => {
					let instances = [];

					if (isViewGeoJSON) {
						instances = datasetInstances.filter((instance) => instance.hasGeoJSON)
						if (!instances.length) {
							return showErrorMessage('', geoReferenceViewNotAvailableError);
						}
					} else {
						instances = datasetInstances.filter((instance) => instance.hasFieldLayout);
						if (!instances || !instances.length) {
							return showErrorMessage('', noLayoutError);
						}
						instances = instances.filter((instance) => !instance.hasGeoJSON)
						if (!instances.length) {
							return showErrorMessage('', geoReferenceCreateNotAvailableError);
						}
					}

					$uibModal.open({
						template: '<single-instance-selector-modal instances="instances" ' +
							' instance-id-property="instanceId" ' +
							' selected="selected" ' +
							' on-select-instance="onSelectInstance" ' +
							' on-continue="onContinue" ' +
							' ></single-instance-selector-modal>',
						controller: function ($scope, $uibModalInstance) {
							$scope.selected = {};
							$scope.instances = instances;

							$scope.onContinue = function () {
								$uibModal.open({
									templateUrl: '/Fieldbook/static/angular-templates/geojson/geojson-modal.html',
									size: 'lg',
									controller: 'GeoJSONModalCtrl',
									resolve: {
										isViewGeoJSON: function () {
											return Boolean(isViewGeoJSON);
										},
										instanceId: function () {
											return $scope.selected.instanceId;
										},
										helpModule: function () {
											return 'MANAGE_STUDIES_FIELDMAP_GEOREFERENCE';
										}
									}
								});
							};
						}

					});
				});
			};

			$scope.preparePlanting = function () {
				$scope.navigateToSubObsTab(studyContext.measurementDatasetId).then(function () {
					$rootScope.$broadcast('startPlantingPreparation');
				});
			}

			$scope.ChangePlotEntry = function () {
				$scope.navigateToSubObsTab(studyContext.measurementDatasetId).then(function () {
					$rootScope.$broadcast('changePlotEntry');
				});
			}

			$rootScope.openInventoryDetailsModal = function (gid) {

				$uibModal.open({
					templateUrl: '/Fieldbook/static/js/trialmanager/inventory/details/inventory-details-modal.html',
					controller: 'InventoryDetailsCtrl',
					windowClass: 'modal-very-huge',
					resolve: {
						gid: function () {
							return gid;
						}
					}
				});
			}

			$scope.showCreateSampleListModal = function () {
				createSampleModalService.openDatasetOptionModal();
			}

			$scope.showCalculatedVariableModal = function () {
				derivedVariableModalService.openDatasetOptionModal();
			}

			$scope.selectDataset = function () {
				advanceStudyModalService.openSelectDatasetModal();
			}

			$scope.selectEnvironment = function (advanceType, isBeta) {
				advanceStudyModalService.selectEnvironment(advanceType, isBeta, null);
			}

			$scope.analyzeWithBrapp = function (brappURL) {
				datasetService.getDatasetInstances(studyContext.measurementDatasetId).then((datasetInstances) => {
					$uibModal.open({
						template: '<multiple-instance-selector-modal instances="instances" ' +
							' instance-id-property="instanceId" ' +
							' selected-instances="selectedInstances" ' +
							' on-select-instance="onSelectInstance" ' +
							' on-continue="onContinue" ' +
							' modal-title="modalTitle" ' +
							' ></multiple-instance-selector-modal>',
						controller: function ($scope) {
							$scope.selectedInstances = {};
							$scope.instances = datasetInstances;
							$scope.isEmptySelection = false;
							if (brappURL === DS_BRAPP_URL) {
								$scope.modalTitle = 'Decision Support Tool(Beta)';
							} else if (brappURL === STABRAPP_URL) {
								$scope.modalTitle = 'STA BrAPP(Beta)';
							}

							$scope.onContinue = function () {
								const instanceIds = Object.entries($scope.selectedInstances)
										.filter(([key, isSelected]) => isSelected)
										.map(([key, value]) => key);

								window.open(brappURL +
									'?cropDb=' + studyContext.cropName +
									'&token=' + JSON.parse(localStorage['bms.xAuthToken']).token +
									'&apiURL=' + window.location.origin + '/bmsapi' +
									'&studyDbIds=' + instanceIds
									, '_blank');
							};
						}
					});
				});
			};

			$scope.init = function () {
				derivedVariableService.displayExecuteCalculateVariableMenu();
			}

			$scope.init();

			$scope.openLotCreationModal = function () {
				$scope.navigateToSubObsTab(studyContext.measurementDatasetId).then(function () {
					$rootScope.$broadcast('createLotsFromSubObsRegister');
				});
			}
		}]);

	manageTrialApp.factory('studyService', ['$http', '$q', 'studyContext', 'serviceUtilities',
		function ($http, $q, studyContext, serviceUtilities) {

			var BASE_URL = '/bmsapi/crops/' + studyContext.cropName;

			var failureHandler = serviceUtilities.restFailureHandler;

			var studyService = {};

			studyService.studyHasSamples = function () {
				var request = $http.get(BASE_URL + '/programs/' + studyContext.programId + '/studies/' + studyContext.studyId + '/sampled');
				return request.then(((response) => {
					return response;
				}), failureHandler);
			};

			return studyService;

		}]);

	manageTrialApp.filter('filterMeasurementState', function () {
		return function (tabs, isOpenStudy) {
			var filtered = angular.copy(tabs);

			for (var i = 0; i < filtered.length; i++) {
				if (filtered[i].state === 'editMeasurements' && isOpenStudy) {
					filtered.splice(i, 1);

					break;
				} else if (filtered[i].state === 'openMeasurements' && !isOpenStudy) {
					filtered.splice(i, 1);

					break;
				}
			}

			return filtered;
		};
	});

	manageTrialApp.filter('orderObjectBy', function () {
		return function (items, field, reverse) {
			var filtered = [];
			angular.forEach(items, function (item) {
				filtered.push(item);
			});
			filtered.sort(function (a, b) {
				return (a[field] > b[field] ? 1 : -1);
			});
			if (reverse) {
				filtered.reverse();
			}
			return filtered;
		};
	});

	// README IMPORTANT: Code unmanaged by angular should go here
	document.onInitManageTrial = function () {
		// do nothing for now
		$('body').data('trialStatus', operationMode);
	};

})();
