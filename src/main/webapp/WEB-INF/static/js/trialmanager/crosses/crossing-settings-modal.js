/*global angular, showAlertMessage, showErrorMessage, */
(function() {
	'use strict';
	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('CrossingSettingsCtrl', ['$scope', '$rootScope', function ($scope, $rootScope) {
		$scope.nextSequenceName = '';
		$scope.sampleParentageDesignation = '';
		$scope.harvestYears = [];
		$scope.harvestMonths = [];
		$scope.settingPresets = [];
		$scope.useManualNaming = 'false';
		$scope.checkExistingCrosses = false;
		$scope.selectedPresetId = '';
		$scope.settingObject = {
			name : '',
			breedingMethodSetting : {
				methodId : null,
				basedOnStatusOfParentalLines : false,
				basedOnImportFile : false
			},
			crossNameSetting : {
				prefix : '',
				suffix : '',
				addSpaceBetweenPrefixAndCode : false,
				addSpaceBetweenSuffixAndCode : false,
				numOfDigits : null,
				separator : '/',
				startNumber : 1,
				saveParentageDesignationAsAString : false
			},
			preservePlotDuplicate : true,
			applyNewGroupToPreviousCrosses : false,
			isUseManualSettingsForNaming : false,
			additionalDetailsSetting : {
				harvestLocationId : null,
				harvestYear : null,
				harvestMonth : null,
				harvestDate : null
			}
		};

		$scope.targetkey = 'selectedLocation';
		$scope.valuecontainer = {selectedLocation : 1};

		function init() {
			$scope.settingObject.crossNameSetting.addSpaceBetweenSuffixAndCode = false;
			$scope.settingObject.crossNameSetting.addSpaceBetweenPrefixAndCode = false;
			retrieveLocationIdFromFirstEnviroment().done(function (locationId) {
				$scope.valuecontainer.selectedLocation = locationId;
				$scope.settingObject.additionalDetailsSetting.harvestLocationId = locationId;
			});
		}

		init();

		$scope.isUseManualNaming = function () {
			return $scope.settingObject.isUseManualSettingsForNaming === 'true';
		}

		$scope.locationChanged = function () {
			$scope.settingObject.additionalDetailsSetting.harvestLocationId = $scope.valuecontainer.selectedLocation;
		}

		$scope.continue = function () {
			if (isCrossImportSettingsValid($scope.settingObject)) {
				$scope.settingObject.breedingMethodSetting = $rootScope.breedingMethodSetting;
				$scope.settingObject.applyNewGroupToPreviousCrosses = !$rootScope.applyGroupingToNewCrossesOnly;
				retrieveNextNameInSequence(function(data){
					if (data.success === '1') {
						$scope.showCrossListPopup();
					} else {
						showErrorMessage('', data.error);
					}
				}, function(){ showErrorMessage('', $.fieldbookMessages.errorNoNextNameInSequence)} );
			}
		}

		$scope.showCrossListPopup = function() {
			$('#crossSettingsModal').modal('hide');
			setTimeout(function () {
				$scope.submitCrossImportSettings().then(function () {
					// createdCrossesListId (global) will be null for import
					return ImportCrosses.openCrossesList(createdCrossesListId);
				});
			}, 500);
		}

		$scope.submitCrossImportSettings = function() {
			'use strict';

			var targetURL = ImportCrosses.CROSSES_URL + '/submit';
			var settingsForSaving = false;

			if ($scope.settingObject.name && $scope.settingObject.name.trim() !== '') {
				targetURL = ImportCrosses.CROSSES_URL + '/submitAndSaveSetting';
				settingsForSaving = true;
			}

			return $.ajax({
				headers: {
					Accept: 'application/json',
					'Content-Type': 'application/json'
				},
				url: targetURL,
				type: 'POST',
				cache: false,
				data: JSON.stringify($scope.settingObject),
				success: function(data) {
					if (data.success === '0') {
						showErrorMessage('', $.fieldbookMessages.errorImportFailed);
					} else {
						$('#crossSettingsModal').modal('hide');

						if (settingsForSaving) {
							// as per UI requirements, we also display a success message regarding the saving of the settings
							// if an error in the settings saving has occurred, program flow would have continued in the data.success === '0' branch
							// hence, we can safely assume that settings have been properly saved at this point
							showSuccessfulMessage('', crossingSettingsSaved);
						}
					}
				},
				error: function() {
					showErrorMessage('', $.fieldbookMessages.errorImportCrossesSettingsFailed);
				}
			});
		}

		$scope.toggleNamingSection = function () {
			$scope.settingObject.isUseManualSettingsForNaming = $scope.useManualNaming === 'true';
		}

		$scope.updateDisplayedSequenceNameValue = function () {
			if(validateStartingSequenceNumber($scope.settingObject.crossNameSetting.startNumber)) {
				retrieveNextNameInSequence(updateNextSequenceName
					, function() { showErrorMessage('', $.fieldbookMessages.errorNoNextNameInSequence); });
			}
		}


		$scope.updateSampleParentageDesignation = function () {
			$scope.sampleParentageDesignation = 'FEMALE-123' + $scope.settingObject.crossNameSetting.separator + 'MALE-456';
		}

		$scope.fetchPresets = function($select, $event) {
			// no event means first load!
			if (!$event) {
				$scope.settingPresets = [];
			} else {
				$event.stopPropagation();
				$event.preventDefault();
			}
			retrieveAvailableImportSettings().done(function(presets) {
				$scope.settingPresets = $scope.settingPresets.concat(presets);
			});
		}

		$scope.fetchMonths = function ($select, $event) {
			// no event means first load!
			if (!$event) {
				$scope.harvestMonths = [];
			} else {
				$event.stopPropagation();
				$event.preventDefault();
			}
			retrieveHarvestMonths().done(function(monthData) {
				$scope.harvestMonths = $scope.harvestMonths.concat(monthData);
			});
		};

		$scope.fetchYears = function ($select, $event) {
			// no event means first load!
			if (!$event) {
				$scope.harvestYears = [];
			} else {
				$event.stopPropagation();
				$event.preventDefault();
			}
			retrieveHarvestYears().done(function(yearData) {
				$scope.harvestYears = $scope.harvestYears.concat(yearData);
				//select the current year; the current year is the middle with the options as -10, current, +10 years
				var currentYearIndex = parseInt(yearData.length/2);
				$scope.settingObject.additionalDetailsSetting.harvestYear = yearData[currentYearIndex];
			});
		};

		$scope.updateHarvestDate = function () {
			$scope.settingObject.additionalDetailsSetting.harvestDate =
				$scope.settingObject.additionalDetailsSetting.harvestYear + '-' + $scope.settingObject.additionalDetailsSetting.harvestMonth + '-01';
		}

		$scope.deletePreset = function() {
			if($scope.selectedPresetId && $scope.selectedPresetId !== '') {
				this.deleteImportSettings().done(function () {
					showSuccessfulMessage('', crossingSettingsDeleted);
					$scope.fetchYears();
				})
				.fail(function () {
					showErrorMessage('', crossingSettingsDeleteFailed);
				});
			}
		}

		$scope.applySettingsPreset = function (selected) {
			$scope.selectedPresetId = selected.programPresetId;
			$scope.settingObject.name = selected.name;
			$scope.settingObject.crossNameSetting.prefix = selected.crossPrefix;
			$scope.settingObject.crossNameSetting.suffix = selected.crossSuffix;
			$scope.settingObject.crossNameSetting.addSpaceBetweenPrefixAndCode = selected.hasPrefixSpace;
			$scope.settingObject.crossNameSetting.addSpaceBetweenSuffixAndCode = selected.hasSuffixSpace;
			$scope.settingObject.crossNameSetting.numOfDigits = selected.sequenceNumberDigits;
			$scope.settingObject.crossNameSetting.separator = selected.parentageDesignationSeparator;
			$scope.settingObject.crossNameSetting.startNumber = selected.startingSequenceNumber;
			$scope.settingObject.crossNameSetting.saveParentageDesignationAsAString = selected.hasParentageDesignationName;
			$scope.settingObject.additionalDetailsSetting.harvestLocationId = $scope.valuecontainer.selectedLocation = selected.locationId;
			this.updateDisplayedSequenceNameValue();
			this.updateSampleParentageDesignation();

		}

		function retrieveAvailableImportSettings() {
			'use strict';
			return $.ajax({
				url: ImportCrosses.CROSSES_URL + '/retrieveSettings',
				type: 'GET',
				cache: false,success: function (data) {
				},
				error: function (jqXHR, textStatus, errorThrown) {
					console.log('The following error occurred: ' + textStatus, errorThrown);
				},
				complete: function () {
				}
			});
		}

		function retrieveLocationIdFromFirstEnviroment() {
			return $.ajax({
				url: '/Fieldbook/crosses/getLocationIdFromFirstEnviroment',
				type: 'GET',
				cache: false,
				async: false,
				success: function (data) {
				},
				error: function (jqXHR, textStatus, errorThrown) {
					console.log('The following error occurred: ' + textStatus, errorThrown);
				},
				complete: function () {
				}
			});
		}
		function validateStartingSequenceNumber(value) {
			'use strict';
			if (value !== null && value !== '' && (!isInt(value))) {
				createErrorNotification(invalidInputMsgHeader, invalidStartingNumberErrorMsg);
				return false;
			}
			return true;
		}

		function updateNextSequenceName(data) {
			if (data.success === '1') {
				$scope.nextSequenceName = data.sequenceValue;
			} else {
				showErrorMessage('', data.error);
			}
		}

		function retrieveNextNameInSequence(success, fail) {
			'use strict';
			$.ajax({
				headers: {
					Accept: 'application/json',
					'Content-Type': 'application/json'
				},
				url: '/Fieldbook/crosses/generateSequenceValue',
				type: 'POST',
				data: JSON.stringify($scope.settingObject),
				cache: false
			}).done(function(data) {
				success(data);
			}).fail(function() {
				fail();
			});
		}

		function retrieveHarvestMonths() {
			'use strict';
			//TODO handle errors for ajax request
			return $.ajax({
				url: '/Fieldbook/crosses/getHarvestMonths',
				type: 'GET',
				cache: false
			});
		}

		function retrieveHarvestYears() {
			'use strict';
			//TODO handle errors for ajax request
			return $.ajax({
				url: '/Fieldbook/crosses/getHarvestYears',
				type: 'GET',
				cache: false
			});
		}

		function deleteImportSettings() {
			'use strict';
			return $.ajax({
				url: ImportCrosses.CROSSES_URL + '/deleteSetting/' + $scope.selectedPresetId,
				type: 'DELETE',
				cache: false,
				global: false
			});
		}

		function isCrossImportSettingsValid() {
			'use strict';
			if(!$scope.settingObject.additionalDetailsSetting.harvestMonth || $scope.settingObject.additionalDetailsSetting.harvestMonth === '') {
				showErrorMessage('', $.fieldbookMessages.errorNoHarvestMonth);
				return false;
			}

			if (!$scope.settingObject.additionalDetailsSetting.harvestLocationId || $scope.settingObject.additionalDetailsSetting.harvestLocationId === '') {
				showErrorMessage('', $.fieldbookMessages.errorNoHarvestLocation);
				return false;
			}
			if ($scope.settingObject.isUseManualSettingsForNaming) {
				if (!$scope.settingObject.crossNameSetting.prefix || $scope.settingObject.crossNameSetting.prefix === '') {
					showErrorMessage('', $.fieldbookMessages.errorNoNamePrefix);
					return false;
				} else if (!$scope.settingObject.crossNameSetting.separator || $scope.settingObject.crossNameSetting.separator === '') {
					showErrorMessage('', $.fieldbookMessages.errorNoParentageDesignationSeparator);
					return false;
				}
				if (!validateStartingSequenceNumber($scope.settingObject.crossNameSetting.startNumber)) {
					return false;
				}
			}
			return true;
		}
	}]);
})();
