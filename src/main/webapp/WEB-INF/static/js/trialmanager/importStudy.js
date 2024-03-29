(function () {
	'use strict';

	var importObservationAfterMappingVariateGroupDeRegister = function () {};

	var importStudyModule = angular.module('import-study', ['ui.bootstrap', 'datasets-api', 'datasetOptionModal',
		'fieldbook-utils']);

	importStudyModule.factory('importStudyModalService', ['$uibModal',
		function ($uibModal) {

			var importStudyModalService = {};

			importStudyModalService.openDatasetOptionModal = function () {
				$uibModal.open({
					template: '<dataset-option-modal modal-title="modalTitle" message="message" selected="selected"' +
						' supported-dataset-types="supportedDatasetTypes" on-continue="showImportOptions()"></dataset-option-modal>',
					controller: 'importDatasetOptionCtrl',
					size: 'md'
				});
			};

			importStudyModalService.openImportStudyModal = function (datasetId) {
				$uibModal.open({
					templateUrl: '/Fieldbook/static/angular-templates/importStudy/ImportStudyModal.html',
					controller: "importStudyCtrl",
					size: 'md',
					resolve: {
						datasetId: function () {
							return datasetId;
						}
					},
					controllerAs: 'ctrl'
				});
			};

			importStudyModalService.redirectToOldImportModal = function () {
				// Call the global function to show the old import study modal
				setTimeout(function () {
					showImportOptions();
				});
			};

			importStudyModalService.showAlertMessage = function (title, message) {
				// Call the global function to show alert message
				showAlertMessage(title, message);
			};

			importStudyModalService.showWarningMessage = function (header, title, warnings, question, confirmButtonLabel, cancelButtonLabel) {
				var modalInstance = $uibModal.open({
					animation: true,
					templateUrl: '/Fieldbook/static/angular-templates/warningModal.html',
					controller: function ($scope, $uibModalInstance) {
						$scope.header = header;
						$scope.title = title;
						$scope.warnings = warnings;
						$scope.question = question;
						$scope.confirmButtonLabel = confirmButtonLabel;
						$scope.cancelButtonLabel = cancelButtonLabel;

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

			return importStudyModalService;

		}]);

	importStudyModule.controller('importDatasetOptionCtrl', ['$scope', '$uibModal', '$uibModalInstance', 'studyContext', 'importStudyModalService', 'DATASET_TYPES_OBSERVATION_IDS',
		'DATASET_TYPES', 'HAS_GENERATED_DESIGN', function ($scope, $uibModal, $uibModalInstance, studyContext, importStudyModalService, DATASET_TYPES_OBSERVATION_IDS, DATASET_TYPES, HAS_GENERATED_DESIGN) {

			$scope.modalTitle = 'Import Study Book';
			$scope.message = 'Please choose the dataset you would like to import:';
			$scope.measurementDatasetId = studyContext.measurementDatasetId;
			$scope.supportedDatasetTypes = [DATASET_TYPES.SUMMARY_DATA];
			$scope.selected = {datasetId: studyContext.trialDatasetId};
			if (HAS_GENERATED_DESIGN) {
				$scope.selected = {datasetId: $scope.measurementDatasetId};
				$scope.supportedDatasetTypes.push(...DATASET_TYPES_OBSERVATION_IDS);
			}

			$scope.showImportOptions = function () {
				importStudyModalService.openImportStudyModal($scope.selected.datasetId);
			};

		}]);

	importStudyModule.controller('importStudyCtrl', ['datasetId', '$scope', '$rootScope', '$uibModalInstance', 'datasetService', 'importStudyModalService', 'HasAnyAuthorityService', 'PERMISSIONS',
		'studyContext', function (datasetId, $scope, $rootScope, $uibModalInstance, datasetService, importStudyModalService, HasAnyAuthorityService, PERMISSIONS, studyContext) {

			$scope.modalTitle = 'Import Study Book';
			$scope.file = null;
			$scope.importedData = null;
			var ctrl = this;
			ctrl.isEnvironmentsImport = studyContext.trialDatasetId === datasetId;

			// Deregister previously create listeners
			importObservationAfterMappingVariateGroupDeRegister();

			importObservationAfterMappingVariateGroupDeRegister = $rootScope.$on('importObservationAfterMappingVariateGroup', function (event) {
				if (ctrl.isEnvironmentsImport) {
					$scope.importEnvironmentVariableValues();
				} else {
					$scope.importObservations(true);
				}
			});

			ctrl.importFormats = [
				{name: 'CSV', extension: '.csv', isVisible: true},
				{name: 'Excel', extension: '.xls,.xlsx', isVisible: true},
				{name: 'KSU fieldbook CSV', extension: '.csv', isVisible: !ctrl.isEnvironmentsImport},
				{name: 'KSU fieldbook Excel', extension: '.xls,.xlsx', isVisible: !ctrl.isEnvironmentsImport}
			];

			$scope.backToDatasetOptionModal = function () {
				$uibModalInstance.close();
				importStudyModalService.openDatasetOptionModal();
			};

			$scope.clearSelectedFile = function () {
				$scope.file = null;
				$scope.importedData = null;
			};

			$scope.submitImport = function () {
				$scope.validateNewVariables().then(function (result) {
					$rootScope.importedData  = $scope.importedData;
					if (result.length > 0) {
						if (!ctrl.isEnvironmentsImport && (!HasAnyAuthorityService.hasAnyAuthority(PERMISSIONS.ADD_OBSERVATION_TRAIT_VARIABLES_PERMISSIONS) ||
							!HasAnyAuthorityService.hasAnyAuthority(PERMISSIONS.ADD_OBSERVATION_SELECTION_VARIABLES_PERMISSIONS))) {
							$uibModalInstance.close();
							showErrorMessage('', messagerErrorImportObservationWithVariables);
						} else if (ctrl.isEnvironmentsImport && (!HasAnyAuthorityService.hasAnyAuthority(PERMISSIONS.ADD_ENVIRONMENT_DETAILS_VARIABLES_PERMISSIONS) ||
							!HasAnyAuthorityService.hasAnyAuthority(PERMISSIONS.ADD_ENVIRONMENTAL_CONDITIONS_VARIABLES_PERMISSIONS))) {
							$uibModalInstance.close();
							showErrorMessage('', messagerErrorImportEnvironmentsWithVariables);
						}
						ctrl.showAddVariableConfirmModal(result, datasetId);
					} else {
						if (ctrl.isEnvironmentsImport) {
							$scope.importEnvironmentVariableValues();
						} else {
							$scope.importObservations(true);
						}
					}
				});
			};

			$scope.validateNewVariables = function () {
				return datasetService.getAllProperties(datasetId).then(function (projectProperties) {
					var importedData = $scope.importedData[0];
					var newVariables = [];
					var existingVariableNames = [];
					var existingVariableAliases = [];
					var existingNameTypes = [];

					$.each(projectProperties.variables, function (i, e) {
						existingVariableAliases.push(e.alias)
						existingVariableNames.push(e.name)
					});

					$.each(projectProperties.nameTypes, function (i, e) {
						existingNameTypes.push(e.code)
					});

					for (var i = 0; i < importedData.length; i++) {
						if (!existingVariableAliases.includes(importedData[i]) &&
							!existingVariableNames.includes(importedData[i]) &&
							!existingNameTypes.includes(importedData[i])) {
							newVariables.push(importedData[i]);
						}
					}
					return newVariables;
				});
			};

			$scope.initMapPopup = function(result, datasetId) {

				// get your angular element
				var elem = angular
					.element('#importMapModal .modal-content[ng-controller=importObservationsCtrl]');

				// get the injector.
				var injector = elem.injector();

				// get the service.
				var myService = injector.get('ImportMappingService');

				myService.datasetId = datasetId;
				myService.isEnvironmentsImportBoolean = ctrl.isEnvironmentsImport;

				var scope = elem.scope();
				scope.datasetId = myService.datasetId;

				// retrieve initial data from the service
				$.getJSON('/Fieldbook/etl/workbook/importObservations/getMappingData/' + result + '/isEnvironmentsImport/' + ctrl.isEnvironmentsImport).done(
					function(data) {

						myService.data = data;
						scope.data = myService.data;

						// apply the changes to the scope.
						scope.$apply();
					});
			};

			$scope.importObservations = function (processWarnings) {
				datasetService.importObservations(datasetId, $rootScope.importedData, processWarnings).then(function () {
					displaySaveSuccessMessage('page-message', 'Your data was successfully imported and may need confirmation.');
					$rootScope.navigateToSubObsTab(datasetId, {reload: true});
					$scope.close();
				}, function (response) {
					if (response.status == 400) {
						showErrorMessage('', response.data.errors[0].message);
					} else if (response.status == 412) {
						ctrl.showConfirmModal(response.data.errors);
					} else {
						showErrorMessage('', ajaxGenericErrorMsg);
					}
				});
			};

			$scope.importEnvironmentVariableValues = function () {
				datasetService.importEnvironmentVariableValues(datasetId, $rootScope.importedData).then(function () {
					displaySaveSuccessMessage('page-message', 'Your data was successfully imported.');
					window.location = '/Fieldbook/TrialManager/openTrial/' + studyContext.studyId;
				}, function (response) {
					if (response.status == 400 || response.status == 412) {
						showErrorMessage('', response.data.errors[0].message);
					} else {
						showErrorMessage('', ajaxGenericErrorMsg);
					}
				});
			};

			$scope.close = function () {
				$uibModalInstance.close();
			};

			ctrl.showConfirmModal = function (warnings) {
				$uibModalInstance.close();
				var warningMessages = [];
				for (var i = 0; i < warnings.length; i++) {
					warningMessages.push(warnings[i].message);
				}

				var modalWarningMessage = importStudyModalService.showWarningMessage('Confirmation', 'Some observations were found in the imported file:', warningMessages, 'Would you like to proceed with the import ?', 'Proceed', 'Back');
				modalWarningMessage.result.then(function (shouldContinue) {
					if (shouldContinue) {
						$scope.importObservations(false);
					} else {
						importStudyModalService.openImportStudyModal(datasetId);
					}
				});
			};

			ctrl.showDesignMapPopup = function (result, datasetId) {
				$('#importMapModal').one('show.bs.modal', function () {
						$scope.initMapPopup(result, datasetId);
					}).modal();
			};

			ctrl.showAddVariableConfirmModal = function (result, datasetId) {
				$uibModalInstance.close();
				var warningMessages = [];

				var modalWarningMessage = importStudyModalService.showWarningMessage('Confirmation',
					'Some of the variables that you are trying to import are not present in this dataset.', warningMessages,
					'Would you like to add them? ', 'Yes', 'No');
				modalWarningMessage.result.then(function (shouldContinue) {
					if (shouldContinue) {
						ctrl.showDesignMapPopup(result, datasetId);
					} else {
						if (ctrl.isEnvironmentsImport) {
							$scope.importEnvironmentVariableValues();
						} else {
							$scope.importObservations(true);
						}
					}
				});
			};

			ctrl.init = function () {
				$scope.file = null;
				$scope.importedData = null;
				ctrl.format = {selected: ctrl.importFormats[1]};
			};

			ctrl.init();

		}])
		.directive('importObservation', function () {
			return {
				restrict: 'AE',
				scope: {
					importedFile: '=',
					importedData: '='
				},
				link: function (scope, elem, attrs) {
					elem.on('change', function (changeEvent) {
						var reader = new FileReader();

						reader.onload = function (e) {
							/* read workbook */
							var bstr = e.target.result;
							var wb = XLSX.read(bstr, {type: 'binary', raw: true});

							/* grab first sheet */
							var wsname = wb.SheetNames[0];
							if (wb.SheetNames.length > 1) {
								wsname = 'Observation';

								if (!wb.Sheets[wsname]) {
									showErrorMessage('', 'Wrong name for Observation sheet - please remedy in spreadsheet and try again');
									return;
								}
							}

							// read imported file with raw parameter set to false(default) to correctly check if imported file is transposed
							var workbook = XLSX.read(bstr, {type: 'binary'});
							var parseData;
							var isTransposed = false;
							if (wsname === 'Observation' && isImportedFileTransposed(workbook)) {
								parseData = parseFile(workbook, wsname);
								isTransposed = true;

								var hasFileIssues = false;
								var errorMessage = '';
								parseData = reverseTransposedData(parseData, hasFileIssues, errorMessage);
								if (hasFileIssues) {
									showErrorMessage('', errorMessage);
									return;
								}
							} else {
								parseData = parseFile(wb, wsname);
							}

							/* update scope */
							scope.$apply(function () {
								var length = 20;
								scope.importedData = parseData;
								scope.importedFile = changeEvent.target.files[0];
								scope.importedFile.abbrName = scope.importedFile.name;
								scope.isTransposed = isTransposed;

								if (scope.importedFile.name.length > length) {
									scope.importedFile.abbrName = scope.importedFile.abbrName.substring(0, length) + '...';
								}

								var fileElement = angular.element('#file_upload');
								angular.element(fileElement).val(null);
							});

						};
						reader.readAsBinaryString(changeEvent.target.files[0]);

						function parseFile(wb, wsname) {
							var ws = wb.Sheets[wsname];
							/* grab first row and generate column headers */
							return  XLSX.utils.sheet_to_json(ws, {header: 1, raw: false, defval: ""});
						};

						function isImportedFileTransposed(wb) {
							var worksheet = wb.Sheets['Observation'];
							return worksheet['!merges'];
						};

						function reverseTransposedData(parsedData, hasFileIssues, errorMessage) {
							const reversedData = [];
							if (parsedData.length < 2) {
								hasFileIssues = true;
								errorMessage = 'Invalid Transposed file headers - please remedy in spreadsheet and try again';
							}

							var observationUnitVarIndex = parsedData[1].findIndex((element) => {
									return element !== null && element !== '';
								}) - 1;
							reversedData.push(parseHeaders(parsedData, observationUnitVarIndex));
							parseObservations(reversedData, parsedData, observationUnitVarIndex);
							return reversedData;
						}

						function parseHeaders(parsedData, observationUnitVarIndex) {
							const headers = [];
							headers.push(...parsedData[0].slice(0, observationUnitVarIndex + 1));
							const nonFactorHeaders = getNonFactorHeaders(parsedData);
							if(nonFactorHeaders.length > 0) {
								headers.push(...nonFactorHeaders);
							}
							return headers;
						}

						function parseObservations(reversedData, parsedData, observationUnitVarIndex) {
							var subObservationsPerPlot = getSubObservationsPerPlot(parsedData[0]);
							const numberOfSubObsPerPlot =  parseInt(subObservationsPerPlot);
							var numberOfObservations = parsedData.length;
							const numberOfNonFactors = getNonFactorHeaders(parsedData).length;
							for (let i=2; i<numberOfObservations; i++) {
								const factorsValues = parsedData[i].slice(0, observationUnitVarIndex);
								for(let observationUnitValue =  1; observationUnitValue<= numberOfSubObsPerPlot; observationUnitValue++) {
									const observationDataRow = [...factorsValues];
									observationDataRow.push(observationUnitValue.toString());
									if (numberOfNonFactors > 0) {
										const startOfValuesForSubOs = observationUnitVarIndex + 1 + ((observationUnitValue-1)*numberOfNonFactors);
										const endOfValuesForSubOs = numberOfNonFactors + startOfValuesForSubOs;
										const nonFactorValues = parsedData[i].slice(startOfValuesForSubOs, endOfValuesForSubOs);
										observationDataRow.push(...nonFactorValues);
									}
									reversedData.push(observationDataRow);
								}
							}

						};

						function getNonFactorHeaders(parsedData) {
							return parsedData[1].filter((element, index, array) => {
								return element !== '' && array.indexOf(element) === index;
							});
						}

						function getSubObservationsPerPlot(headers) {
							for (let i = headers.length - 1; i >= 0; i--) {
								if (headers[i] !== null && headers[i] !== '') {
									return headers[i];
								}
							}
						}
					});
				}
			};
		});
})();
