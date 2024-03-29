/*global angular, environmentModalConfirmationText, environmentConfirmLabel, showAlertMessage, showErrorMessage*/
(function () {
	'use strict';

	angular.module('manageTrialApp').controller('EnvironmentCtrl', ['$scope', '$q', 'TrialManagerDataService', '$uibModal', '$stateParams',
		'$http', 'DTOptionsBuilder', 'LOCATION_ID', 'PROGRAM_DEFAULT_LOCATION_ID', '$timeout', 'studyInstanceService', 'studyStateService', 'derivedVariableService', 'studyContext',
		'datasetService', '$compile', 'fileService', 'HasAnyAuthorityService', 'PERMISSIONS',
		function ($scope, $q, TrialManagerDataService, $uibModal, $stateParams, $http, DTOptionsBuilder, LOCATION_ID, PROGRAM_DEFAULT_LOCATION_ID, $timeout, studyInstanceService,
				  studyStateService, derivedVariableService, studyContext, datasetService, $compile, fileService, HasAnyAuthorityService, PERMISSIONS) {

			var ctrl = this;
			var tableId = '#environment-table';

			$scope.TRIAL_INSTANCE_NO_INDEX = 8170;
			$scope.hasAnyAuthority = HasAnyAuthorityService.hasAnyAuthority;
			$scope.instanceInfo = studyInstanceService.instanceInfo;
			$scope.nested = {};
			$scope.nested.dataTable = {};
			$scope.isDisableAddInstance = false;
			$scope.temp = {
				settingMap: {},
				numberOfInstances: $scope.instanceInfo.numberOfInstances
			};

			$scope.settings = TrialManagerDataService.settings.environments;
			fileService.getFileStorageStatus().then((map) => {
				$scope.isFileStorageConfigured = map.status;
				const table = angular.element(tableId);
				if (table.length !== 0 && table.dataTable()) {
					$timeout(function () {
						table.dataTable().fnAdjustColumnSizing();
					});
				}
			});

			$scope.onRemoveVariable = function (variableType, variableIds) {
				return $scope.checkVariableIsUsedInCalculatedVariable(variableType, variableIds);
			};

			$scope.onAddVariable = function (result, variableTypeId) {
				var variable = undefined;
				angular.forEach(result, function (val) {
					variable = val.variable;
				});

				datasetService.addVariables(studyContext.trialDatasetId, {
					variableTypeId: variableTypeId,
					variableId: variable.cvTermId,
					studyAlias: variable.alias ? variable.alias : variable.name
				}).then(function () {
					$scope.nested.dataTable.rerender();
					ctrl.initializePossibleValuesMap();
				}, function (response) {
					if (response.errors && response.errors.length) {
						showErrorMessage('', response.errors[0].message);
					} else {
						showErrorMessage('', ajaxGenericErrorMsg);
					}
				});
			};

			$scope.checkVariableIsUsedInCalculatedVariable = function (variableType ,deleteVariables) {
				var deferred = $q.defer();
				if(deleteVariables && deleteVariables.length !== 0) {
					var variableIsUsedInOtherCalculatedVariable;

					// Retrieve all formula variables in study
					derivedVariableService.getFormulaVariables(studyContext.measurementDatasetId).then(function (response) {
						//response is null if study is not yet saved
						if (response) {
							var formulaVariables = response.data;
							// Check if any of the deleted variables are formula variables
							angular.forEach(formulaVariables, function (formulaVariable) {
								if (deleteVariables.indexOf(formulaVariable.id) > -1) {
									variableIsUsedInOtherCalculatedVariable = true;
								}
							});
						}

						if (variableIsUsedInOtherCalculatedVariable) {
							var modalInstance = $scope.openConfirmModal(removeVariableDependencyConfirmationText, environmentConfirmLabel);
							modalInstance.result.then((isOK) => {
								if (isOK) {
									$scope.detachOrDeleteFilesIfAny(variableType, deleteVariables).then(deferred.resolve);
								} else {
									deferred.resolve();
								}
							});
						} else {
							$scope.detachOrDeleteFilesIfAny(variableType, deleteVariables).then(deferred.resolve);
						}
					});
				}
				return deferred.promise;
			};

			$scope.detachOrDeleteFilesIfAny = async function (variableType, variableIds) {
				var deferred = $q.defer();
				if ($scope.isFileStorageConfigured) {
					const fileCountResp = await fileService.getFileCount(variableIds, studyContext.trialDatasetId, null);
					const fileCount = parseInt(fileCountResp.headers('X-Total-Count'));

					if (fileCount > 0) {
						const modalInstance = $scope.showFileDeletionOptions(fileCount);
						let doRemoveFiles;
						try {
							doRemoveFiles = await modalInstance.result;
						} catch (e) {
							deferred.resolve();
							return deferred.promise;
						}
						if (doRemoveFiles) {
							await fileService.removeFiles(variableIds, studyContext.trialDatasetId)
								.then(async function () {
									await $scope.updateFilesData();
									datasetService.removeVariables(studyContext.trialDatasetId, variableType, variableIds).then(() => {
										$scope.nested.dataTable.rerender();
									});
								});
						} else {
							await fileService.detachFiles(variableIds, studyContext.trialDatasetId)
								.then(async function () {
									await $scope.updateFilesData();
									datasetService.removeVariables(studyContext.trialDatasetId, variableType, variableIds).then(() => {
										$scope.nested.dataTable.rerender();
									});
								});
						}
					} else {
						datasetService.removeVariables(studyContext.trialDatasetId, variableType, variableIds).then(() => {
							$scope.nested.dataTable.rerender();
						});
					}
				} else {
					datasetService.removeVariables(studyContext.trialDatasetId, variableType, variableIds).then(() => {
						$scope.nested.dataTable.rerender();
					});
				}
				deferred.resolve(true);
				return deferred.promise;
			};

			$scope.updateFilesData = async function (instanceId) {
				const instanceIds = [];
				if (instanceId) {
					instanceIds.push(parseInt(instanceId));
				} else {
					$scope.instanceInfo.instances.forEach(instance => instanceIds.push(parseInt(instance.instanceId)));
				}
				await fileService.getFiles(instanceIds).then(function (files) {
					const filesMap = new Map();
					const fileVariableIdsMap = new Map();
					if (files && files.length) {
						files.forEach(function (file) {
							const fileInstanceId = parseInt(file.instanceId);
							if (!filesMap.has(fileInstanceId)) {
								filesMap.set(fileInstanceId, []);
								fileVariableIdsMap.set(fileInstanceId, []);
							}
							filesMap.get(fileInstanceId).push(file);

							if (file.variables && file.variables.length) {
								file.variables.forEach(variable => fileVariableIdsMap.get(fileInstanceId).push(variable.id.toString()));
							}
						});
					}

					$scope.instanceInfo.instances.forEach(function (instance) {
						const currentInstanceId = parseInt(instance.instanceId);
						if (instanceIds.includes(currentInstanceId)) {
							instance.fileCount = filesMap.has(currentInstanceId) ? filesMap.get(currentInstanceId).length : null;
							instance.fileVariableIds = fileVariableIdsMap.has(currentInstanceId) ?
								fileVariableIdsMap.get(currentInstanceId) : null;
						}
					});

					$scope.nested.dataTable.rerender();
				});
			};

			$scope.onLocationChange = function (data) {
				studyInstanceService.changeEnvironments(data);
			}

			$scope.buttonsTop = [
				{
					extend: 'colvis',
					className: 'fbk-buttons-no-border fbk-colvis-button',
					text: '<i class="glyphicon glyphicon-th dropdown-toggle fbk-show-hide-grid-column"></i>',
					columns: ':gt(0):not(.ng-hide)'
				}];

			$scope.dtOptions = DTOptionsBuilder.newOptions().withDOM('<"fbk-datatable-panel-top"liB>rtp')
				.withButtons($scope.buttonsTop.slice())
				.withOption('scrollX', true)
				.withOption('scrollCollapse', true)
				.withOption('deferRender', true)
				.withOption('stateSave', true);

			$scope.dtOptions.drawCallback = function () {
				var api = $(this).DataTable();

				addCellClickHandler();

				//temporary fix in buttons disappear bug,
				//see https://github.com/l-lin/angular-datatables/issues/502#issuecomment-161166246
				if (api) {
					// remove old set of buttons before recreating them
					if (api.buttons()) {
						api.buttons().remove();
					}
					new $.fn.dataTable.Buttons(api, {
						buttons: $scope.buttonsTop.slice()
					});

					$(this).parents('.dataTables_wrapper').find('.dt-buttons').replaceWith(api.buttons().container());
				}
			};

			$scope.showFiles = function (instanceId, variableName) {

				const showFilesModal = $uibModal.open({
					template: '<iframe ng-src="{{url}}"' +
						' style="width:100%; height: 590px; border: 0" />',
					size: 'lg',
					controller: function ($scope, $uibModalInstance) {
						$scope.url = '/ibpworkbench/controller/jhipster#file-manager'
							+ '?cropName=' + studyContext.cropName
							+ '&programUUID=' + studyContext.programId
							+ '&instanceId=' + instanceId
							+ '&datasetId=' + studyContext.trialDatasetId
							+ '&variableName=' + (variableName || '');

						window.closeModal = function () {
							$uibModalInstance.close();
						}
					},
				});

				showFilesModal.result.then(() => $scope.updateFilesData(instanceId));
			};

			// global handle for inline cell html
			window.showFiles = function (instanceId, variableName) {
				event.stopPropagation();
				$scope.showFiles(instanceId, variableName);
			};

			$scope.showFileIcon = function (fileVariableIds, cvTermId) {
				return $scope.isFileStorageConfigured
					&& fileVariableIds
					&& fileVariableIds.length
					&& fileVariableIds.includes(cvTermId.toString());
			}

			$scope.renderDisplayValue = function (settingVariable, value) {

				return renderByDataType(settingVariable, value);

				function renderByDataType(settingVariable, value) {

					var categoricalDataTypeId = 1130;
					var personDataTypeId = 1131;

					// If variable is LOCATION_ID variable, person or categorical, show the description of the selected possible value.
					if (settingVariable.variable.dataTypeId === categoricalDataTypeId ||
						settingVariable.variable.dataTypeId === personDataTypeId ||
						settingVariable.variable.cvTermId === parseInt(LOCATION_ID)) {
						return renderCategoricalValue(settingVariable, value);
					} else {
						return EscapeHTML.escape(value);
					}
				}

				function renderCategoricalValue(settingVariable, value) {
					var displayValue = '';
					if (settingVariable.possibleValuesById && settingVariable.possibleValuesById[value]) {
						displayValue = settingVariable.possibleValuesById[value].description;
					}
					return displayValue;
				}

			}

			TrialManagerDataService.onUpdateData('environments', function () {
				$scope.temp.numberOfInstances = $scope.instanceInfo.numberOfInstances;
			});

			/* Scope Functions */
			$scope.isDesignAlreadyGenerated = function () {
				return studyStateService.hasGeneratedDesign() || studyStateService.hasListOrSubObs();
			};

			$scope.updateInstanceCount = function () {

				if (!$scope.temp.numberOfInstances || parseInt($scope.temp.numberOfInstances) === 0) {
					showErrorMessage('', $.environmentMessages.studyShouldHaveAtLeastOneEnvironment);
				} else if ($scope.temp.numberOfInstances > $scope.instanceInfo.instances.length) {
					$scope.instanceInfo.numberOfInstances = $scope.temp.numberOfInstances;
					$scope.addInstances($scope.temp.numberOfInstances - $scope.instanceInfo.instances.length);
				} else if ($scope.temp.numberOfInstances < $scope.instanceInfo.instances.length) {
					// if new instance count is less than previous value, splice array
					var countDiff = $scope.instanceInfo.instances.length - $scope.temp.numberOfInstances;
					var message = $.environmentMessages.decreaseEnvironmentNoData.replace('{0}', countDiff).replace('{1}', (countDiff > 1 ? 's' : ''));
					var modalConfirmDelete = $scope.openConfirmModal(message, 'Yes', 'No');
					modalConfirmDelete.result.then(function (shouldContinue) {
						$scope.instanceInfo.numberOfInstances = $scope.temp.numberOfInstances;
						var instanceIds = [];
						if (shouldContinue) {
							while ($scope.instanceInfo.instances.length > $scope.temp.numberOfInstances) {
								var instance = $scope.instanceInfo.instances.pop();
								instanceIds.push(instance.instanceId);
							}
							studyInstanceService.deleteStudyInstances(instanceIds);
						}
					});
				}
			};

			$scope.deleteInstance = function (index, instanceId) {

				var deferred = $q.defer();

				studyInstanceService.getStudyInstance(instanceId).then(async function (studyInstance) {
					const fileCountResp = await fileService.getFileCount(null, studyContext.trialDatasetId, null);
					const fileCount = parseInt(fileCountResp.headers('X-Total-Count'));

					// Show error if instance cannot be deleted
					if (!studyInstance.canBeDeleted) {
						showErrorMessage('', $.environmentMessages.environmentCannotBeDeleted);
						return;

						// Show confirmation message for overwriting measurements and/or fieldmap
					} else {
						var message = $.environmentMessages.deleteEnvironmentNoData;
						if (fileCount > 0 || studyInstance.hasMeasurements || studyInstance.hasFieldmap || studyInstance.hasExperimentalDesign) {
							message = $.environmentMessages.environmentHasDataThatWillBeLost;
						}
						var modalConfirmDelete = $scope.openConfirmModal(message, 'Yes', 'No');
						modalConfirmDelete.result.then(async function (shouldContinue) {
							if (shouldContinue) {
								$scope.continueInstanceDeletion(index, [instanceId]);
							}
						});
					}
					deferred.resolve();
				}, function (errResponse) {
					showErrorMessage($.fieldbookMessages.errorServerError, errResponse.errors[0].message);
					deferred.resolve();
				});

				return deferred.promise;

			};

			// Proceed deleting existing instance
			$scope.continueInstanceDeletion = function (index, instanceIds) {
				studyInstanceService.deleteStudyInstances(instanceIds).then(function () {
						updateDeletedInstances(index);
						showSuccessfulMessage('', $.environmentMessages.environmentDeletedSuccessfully);
					}, function (errResponse) {
						showErrorMessage($.fieldbookMessages.errorServerError, errResponse.errors[0].message);
					}
				);
			};


			$scope.updateTrialInstanceNo = function (environments, index) {
				for (var i = 0; i < environments.length; i++) {
					var environment = environments[i];
					var expectedTrialInstanceNo = i + 1;
					var trialInstanceNo = environment.managementDetailValues[$scope.TRIAL_INSTANCE_NO_INDEX];
					if (trialInstanceNo > expectedTrialInstanceNo) {
						environment.managementDetailValues[$scope.TRIAL_INSTANCE_NO_INDEX] = expectedTrialInstanceNo;
					}
				}
			};

			/* Watchers */
			$scope.$watch('instanceInfo.numberOfInstances', function (newVal, oldVal) {
				$scope.temp.numberOfInstances = newVal;
				if (Number(newVal) < Number(oldVal)) {
					TrialManagerDataService.applicationData.hasNewInstanceAdded = false;
				} else if (Number(newVal) > Number(oldVal)) {
					TrialManagerDataService.applicationData.hasNewInstanceAdded = true;
					addCellClickHandler();
				}
			});

			$scope.$watch('settings.managementDetails', function (newVal, oldVal) {
				ctrl.updateInstanceVariables('managementDetails', newVal.keys().length > oldVal.keys().length);
			}, true);

			$scope.$watch('settings.trialConditionDetails', function (newVal, oldVal) {
				ctrl.updateInstanceVariables('trialConditionDetails', newVal.keys().length > oldVal.keys().length);
			}, true);

			$scope.addInstance = function () {

				$scope.isDisableAddInstance = true;

				// create and save the environment in the server
				studyInstanceService.createStudyInstances(1).then(function (studyInstances) {
					angular.forEach(studyInstances, function (studyInstance) {
						// update the environment table
						$scope.createInstance(studyInstance);
						$scope.instanceInfo.numberOfInstances++;
					});
					$scope.isDisableAddInstance = false;
				}, function (response) {
					if (response.errors) {
						showErrorMessage('', response.errors[0].message);
					} else {
						showErrorMessage('', ajaxGenericErrorMsg);
					}
				});

			};

			$scope.addInstances = function (numberOfEnvironments) {
				// create and save the environment in the server
				studyInstanceService.createStudyInstances(numberOfEnvironments).then(function (studyInstances) {
					angular.forEach(studyInstances, function (studyInstance) {
						// update the environment table
						$scope.createInstance(studyInstance);
					});
				});
			};

			$scope.createInstance = function (studyInstance) {
				var instance = {
					instanceId: studyInstance.instanceId,
					managementDetailValues: TrialManagerDataService.constructDataStructureFromDetails(
						$scope.settings.managementDetails),
					trialDetailValues: TrialManagerDataService.constructDataStructureFromDetails($scope.settings.trialConditionDetails),
					managementDetailDataIdMap: {8190: studyInstance.locationDescriptorDataId},
					trialConditionDataIdMap: {},
					experimentId: studyInstance.experimentId
				};
				instance.managementDetailValues[LOCATION_ID] = PROGRAM_DEFAULT_LOCATION_ID;
				instance.managementDetailValues[$scope.TRIAL_INSTANCE_NO_INDEX] = studyInstance.instanceNumber;
				$scope.instanceInfo.instances.push(instance);
			};

			$scope.showFileDeletionOptions = function (fileCount) {
				return $uibModal.open({
					animation: true,
					templateUrl: '/Fieldbook/static/js/trialmanager/file/fileDeletionOptions.html',
					windowClass: 'force-zindex',
					controller: function ($scope, $uibModalInstance) {
						$scope.fileCount = fileCount;
						$scope.removeFiles = function () {
							$uibModalInstance.close(true);
						};

						$scope.detachFiles = function () {
							$uibModalInstance.close(false);
						};

						$scope.cancel = function () {
							$uibModalInstance.dismiss();
						};
					}
				});
			};

			ctrl.updateInstanceVariables = function (type, entriesIncreased) {

				var settingDetailSource = null;
				var valuesPropertyKey = null;
				var idsPropertyKey = null;

				if (type === 'managementDetails') {
					settingDetailSource = $scope.settings.managementDetails;
					valuesPropertyKey = 'managementDetailValues';
					idsPropertyKey = 'managementDetailDataIdMap';
				} else if (type === 'trialConditionDetails') {
					settingDetailSource = $scope.settings.trialConditionDetails;
					valuesPropertyKey = 'trialDetailValues';
					idsPropertyKey = 'trialConditionDataIdMap';
				}

				angular.forEach($scope.instanceInfo.instances, function (instance) {
					var valuesList = instance[valuesPropertyKey];
					var idList = instance[idsPropertyKey];

					if (entriesIncreased) {
						angular.forEach(settingDetailSource.keys(), function (settingDetailKey) {
							if (valuesList[settingDetailKey] === undefined) {
								valuesList[settingDetailKey] = null;
							}
						});
					} else {
						angular.forEach(valuesList, function (value, settingDetailKey) {
							if (!settingDetailSource.vals().hasOwnProperty(settingDetailKey)) {
								delete valuesList[settingDetailKey];
								delete idList[settingDetailKey];
							}
						});
					}
				});
			};

			ctrl.initializePossibleValuesMap = function initializePossibleValuesMap() {

				if ($scope.settings.managementDetails) {
					angular.forEach($scope.settings.managementDetails.vals(), function (settingVariable) {
						ctrl.createPossibleValuesById(settingVariable);
					});
				}

				if ($scope.settings.trialConditionDetails) {
					angular.forEach($scope.settings.trialConditionDetails.vals(), function (settingVariable) {
						ctrl.createPossibleValuesById(settingVariable);
					});
				}

			};

			ctrl.createPossibleValuesById = function (settingVariable) {
				if (settingVariable.allValues && settingVariable.allValues.length > 0) {
					settingVariable.possibleValuesById = {};
					angular.forEach(settingVariable.allValues, function (possibleValue) {
						settingVariable.possibleValuesById[possibleValue.id] = possibleValue;
					});
				}
			}

			// Wrap 'showAlertMessage' global function to a controller function so that
			// we can mock it in unit test.
			ctrl.showAlertMessage = function (title, message) {
				showAlertMessage(title, message);
			};

			ctrl.initializePossibleValuesMap();

			function updateDeletedInstances(index) {
				// remove 1 environment
				$scope.temp.numberOfInstances -= 1;
				$scope.instanceInfo.instances.splice(index, 1);
				if (!$scope.isDesignAlreadyGenerated()) {
					$scope.updateTrialInstanceNo($scope.instanceInfo.instances, index);
				}
				$scope.instanceInfo.numberOfInstances -= 1;

				TrialManagerDataService.deleteInstance(index + 1);
				deleteExperimentalDesignIfApplicable();
			}

			function deleteExperimentalDesignIfApplicable() {

				datasetService.countObservationUnits(studyContext.measurementDatasetId).then(function (response) {
					var count = response.headers('X-Dataset-Observation-Unit');
					if (count == '0') {
						studyStateService.updateGeneratedDesign(false);
						TrialManagerDataService.currentData.experimentalDesign.designType = '';
					}
				});
			}

			function addCellClickHandler() {
				var $table = angular.element(tableId);

				addCellClickHandler();

				function addCellClickHandler() {
					$table.off('click').on('click', 'td.instance-editable-cell', cellClickHandler);
				}

				function cellClickHandler() {
					var cell = this;
					var table = $table.DataTable();
					var dtRow = table.row(cell.parentNode);
					var rowIndex = dtRow.index();
					var dtCell = table.cell(cell);
					var cellIndex = table.colReorder.transpose(table.column(cell).index(), 'toOriginal');

					var variableId = $table.find('th:eq(' + cellIndex + ')').data('termid');
					var instance = $scope.instanceInfo.instances[rowIndex];

					createInlineEditor($table, dtCell, cell, instance, variableId);
				}

				function createInlineEditor($table, dtCell, cell, instance, variableId) {

					var oldValue = dtCell.data();
					var isManagementDetailVariable = instance.managementDetailValues.hasOwnProperty(variableId);
					var instanceId = instance.instanceId;
					var variableSettings;
					var valueContainer;
					var instanceDataIdMap;

					if (isManagementDetailVariable) {
						variableSettings = $scope.settings.managementDetails;
						valueContainer = instance.managementDetailValues;
						instanceDataIdMap = instance.managementDetailDataIdMap;
					} else {
						variableSettings = $scope.settings.trialConditionDetails;
						valueContainer = instance.trialDetailValues;
						instanceDataIdMap = instance.trialConditionDataIdMap;
					}

					var $inlineScope = $scope.$new(true);
					$inlineScope.settings = variableSettings;
					$inlineScope.valueContainer = valueContainer;
					$inlineScope.targetKey = variableId;
					$inlineScope.instance = {
						change: function () {
							updateInline();
						},
						// FIXME altenative to blur bug https://github.com/angular-ui/ui-select/issues/499
						onOpenClose: function (isOpen) {
							if (!isOpen) updateInline();
						}
					};

					$(cell).html('');

					var editor = $compile(
						'<instance-inline-editor ' +
						'instance="instance" ' +
						'settings="settings" ' +
						'targetkey="targetKey"' +
						'settingkey="targetKey"' +
						'valuecontainer="valueContainer"' +
						'</instance-inline-editor>'
					)($inlineScope);

					$(cell).append(editor);

					function updateInline() {

						var newValue = valueContainer[variableId];

						// Do not update if data did not change or value is empty
						if (!newValue || angular.equals(oldValue, newValue)) {
							refreshDisplay();
							return;
						}

						if (!instanceDataIdMap[variableId]) {

							if (isManagementDetailVariable) {
								studyInstanceService.addInstanceDescriptorData({
									instanceId: instanceId,
									variableId: variableId,
									value: newValue
								}).then(function (instanceDescriptorData) {

									// Add the created instanceDescriptorDataId from the server to the map
									// so that it can be used to update the instance descriptor later.
									instanceDataIdMap[variableId] = instanceDescriptorData.instanceDescriptorDataId;
									refreshDisplay();
								}, function (errResponse) {
									showErrorMessage($.fieldbookMessages.errorServerError, errResponse.errors[0].message);
								});
							} else {
								studyInstanceService.addInstanceObservation({
									instanceId: instanceId,
									variableId: variableId,
									value: newValue
								}).then(function (instanceObservationData) {

									// Add the created observationDataId from the server to the map
									// so that it can be used to update the instance observation later.
									instanceDataIdMap[variableId] = instanceObservationData.instanceObservationId;
									refreshDisplay();
								}, function (errResponse) {
									showErrorMessage($.fieldbookMessages.errorServerError, errResponse.errors[0].message);
								});
							}
						} else {
							if (isManagementDetailVariable) {
								studyInstanceService.updateInstanceDescriptorData({
									instanceId: instanceId,
									variableId: variableId,
									instanceDescriptorDataId: instanceDataIdMap[variableId],
									value: newValue
								}).then(function (descriptorData) {
									// Restore handler
									refreshDisplay();
								}, function (errResponse) {
									showErrorMessage($.fieldbookMessages.errorServerError, errResponse.errors[0].message);
								});
							} else {
								studyInstanceService.updateInstanceObservation({
									instanceId: instanceId,
									variableId: variableId,
									instanceObservationId: instanceDataIdMap[variableId],
									value: newValue
								}).then(function (observationData) {
									// Restore handler
									refreshDisplay();
								}, function (errResponse) {
									showErrorMessage($.fieldbookMessages.errorServerError, errResponse.errors[0].message);
								});
							}

						}
					}

					async function refreshDisplay() {
						$inlineScope.$destroy();
						editor.remove();
						dtCell.data($scope.renderDisplayValue(variableSettings.vals()[variableId], valueContainer[variableId]));
						const showFilesButton = await $compile(
							'<i	ng-show="showFileIcon(\'' + instance.fileVariableIds + '\', \'' + variableId + '\')"'
							+ ' ng-click="showFiles(\'' + instance.instanceId + '\', \'' + variableSettings.vals()[variableId].variable.name + '\')"'
							+ ' class="glyphicon glyphicon-duplicate text-info"'
							+ ' title="click to see associated files"'
							+ ' style="font-size: 1.2em; margin-left: 10px; cursor: pointer"></i>'
						)($scope);
						$(cell).append(showFilesButton);
						// Restore handler
						addCellClickHandler();
						if ($table.length !== 0 && $table.dataTable()) {
							$timeout(function () {
								$table.dataTable().fnAdjustColumnSizing();
							});
						}
					}

					$timeout(function () {
						if (variableId === parseInt(LOCATION_ID)) {
							/** Remove the inline editor for Location when the other part of the environment tab is clicked. We can't apply onblur event on location
							 * combobox because the user would need to use the location filter ('breeding location/all location' radio option and 'use favorite' checkbox). **/
							$("[ui-view*='environment']").off('click').on('click', () => {
								refreshDisplay();
								$("[ui-view*='environment']").off('click');
							});
						} else {
							/**
							 * Initiate interaction with the input so that clicks on other parts of the page
							 * will trigger blur immediately. Also necessary to initiate datepicker
							 * This also avoids temporary click handler on body
							 * FIXME is there a better way?
							 */
							$(cell).find('a.ui-select-match, input:not([type=radio], [type=checkbox])').click().focus();
						}
					}, 100);
				}
			}

		}]).directive('instanceInlineEditor', ['_', function (_) {
		return {
			require: [],
			restrict: 'E',
			scope: {
				instance: '=',
				settings: '=',
				targetkey: '=',
				settingkey: '=',
				valuecontainer: '='
			},
			templateUrl: '/Fieldbook/static/angular-templates/instanceInlineEditor.html',
			link: function ($scope, element, attrs) {
				$scope.initializeDropdownForCategoricalVariable();
				// Stop bubbling of click event so to not interfere with
				// the container's click event.
				$(element).click(function (event) {
					event.stopPropagation();
				});
			},
			controller: function ($scope, LOCATION_ID, PROGRAM_DEFAULT_LOCATION_ID, BREEDING_METHOD_ID, BREEDING_METHOD_CODE, $http, locationService) {
				$scope.localData = {};
				$scope.variableDefinition = $scope.settings.val($scope.settingkey);
				$scope.widgetType = $scope.variableDefinition.variable.widgetType.$name ?
					$scope.variableDefinition.variable.widgetType.$name : $scope.variableDefinition.variable.widgetType;
				$scope.hasDropdownOptions = $scope.widgetType === 'DROPDOWN';

				$scope.isLocation = parseInt(LOCATION_ID, 10) === parseInt($scope.variableDefinition.variable.cvTermId, 10);
				$scope.isBreedingMethod = parseInt(BREEDING_METHOD_ID, 10) === parseInt($scope.variableDefinition.variable.cvTermId, 10) ||
					parseInt(BREEDING_METHOD_CODE, 10) === parseInt($scope.variableDefinition.variable.cvTermId, 10);

				$scope.locationChanged = function () {
					$scope.instance.change();
				}

				$scope.initializeDropdownForCategoricalVariable = function () {
					if ($scope.hasDropdownOptions && !$scope.isLocation && !$scope.isBreedingMethod) {
						$scope.localData.dropdownValues = $scope.variableDefinition.allValues;
					}
				};

			}
		};
	}]).directive('instanceDatepicker', function () {
		return {
			require: '^?ngModel',
			scope: {
				instance: '='
			},
			link: function (scope, el, attr, ngModel) {
				$(el).datepicker({
					format: 'yyyymmdd',
					todayHighlight: true,
					todayBtn: true
				}).on('changeDate', function () {
					scope.$apply(function () {
						ngModel.$setViewValue(el.val());
					});
					$(this).datepicker('hide');
				}).on('hide', function () {
					scope.instance.change();
				});
				ngModel.$render = function () {
					var parsedDate;
					try {
						parsedDate = $.datepicker.formatDate("yy-mm-dd", $.datepicker.parseDate('yymmdd', ngModel.$viewValue));
					} catch (e) {
					}
					$(el).datepicker('setDate', parsedDate);
				};

				// Stop bubbling of click event on datepicker selector so to not interfere with the container's
				// click event.
				$("#ui-datepicker-div").click(function (event) {
					event.stopPropagation();
				});
			}
		};
	}).factory('DTLoadingTemplate', function () {
		return {
			html: '<span class="throbber throbber-2x"></span>'
		};
	});
})();
