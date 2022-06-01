/*global angular, showAlertMessage, showErrorMessage, selectContinueAdvancing*/
(function() {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('CrossingBreedingMethodModalCtrl', ['$scope', '$rootScope', 'methodService', function ($scope, $rootScope,
		methodService) {

			$scope.isCrossesImport = true;
			$scope.targetkey = 'methodCode';
			$scope.valuecontainer = {methodCode : null};
			$scope.hybridMethods = null;
			$scope.selectedBreedingMethodId = null;
			$scope.hasHybridMethod = false;
			$scope.applyGroupingToNewCrossesOnly = false;

			$scope.methodChanged = function (item, model) {
				var methodCodes = [$scope.valuecontainer.methodCode];
				methodService.getMethods(null, false, methodCodes).then(function (response) {
					//get the value from index = 1 since the get methods adds a place holder in the zeroth index
					$scope.selectedBreedingMethodId = response.data[1].mid;
					$scope.retrieveHybridMethods();
				});
			};

			$scope.enableDisableMethodsDropdown = function () {
				var enableMethodsSelect = $('#selectMethodForAllCrosses').prop('checked');
				$rootScope.$emit('enableDisableMethodsSelect', enableMethodsSelect);
				if(!enableMethodsSelect) {
					$scope.valuecontainer.methodCode = null;
					$scope.selectedBreedingMethodId = null;
				}

				if ($('#selectMethodInImportFile').prop('checked') && ImportCrosses.hasHybridMethod) {
					$scope.hasHybridMethod = true;
				} else {
					$scope.hasHybridMethod = false;
				}
			};

			$scope.retrieveHybridMethods = function () {
				if ($scope.hybridMethods === null) {
					$.ajax({
						url: ImportCrosses.CROSSES_URL + '/getHybridMethods',
						type: 'GET',
						cache: false,
						success: function (data) {
							$scope.hybridMethods = data;
						}
					}).done($scope.showOrHideApplyGroupingOptionDiv);
				} else {
					$scope.showOrHideApplyGroupingOptionDiv();
				}
			};

			$scope.showOrHideApplyGroupingOptionDiv = function () {
				if(!$scope.hybridMethods.includes(parseInt($scope.selectedBreedingMethodId))) {
					$scope.hasHybridMethod = false;
				} else {
					$scope.hasHybridMethod = true;
				}
			};

			$scope.isBreedingMethodSelectedValid = function() {
				'use strict';
				var radioValue = $('#selectMethodForAllCrosses').prop('checked');
				if (radioValue && (!$scope.selectedBreedingMethodId || $scope.selectedBreedingMethodId === '')) {
					showErrorMessage('', $.fieldbookMessages.errorMethodMissing);
					return false;
				} else if($('#selectMethodInImportFile').prop('checked') || $('#selectMethodForAllCrosses').prop('checked')) {
					var valid = true;
					var validateBreedingMethodUrl = $('#selectMethodInImportFile').prop('checked') ? '/validateBreedingMethods':
						'/validateBreedingMethods?breedingMethodId=' + $scope.selectedBreedingMethodId;
					$.ajax({
						url: ImportCrosses.CROSSES_URL +  validateBreedingMethodUrl,
						type: 'GET',
						cache: false,
						async: false,
						success: function(data) {
							if (data.error) {
								showErrorMessage('', data.error);
								valid = false;
							}

						},
						error: function(jqXHR, textStatus, errorThrown) {
							console.log('The following error occured: ' + textStatus, errorThrown);
						}
					});
					return valid;

				} else {
					return true;
				}
			};

			$scope.init = function (isCrossesImport) {
				if (isCrossesImport) {
					$('#selectMethodInImportFile').prop('checked',true);
					$('#selectUseParentalStatus').prop('checked',false);

				} else {
					$('#selectMethodInImportFile').prop('checked',false);
					$('#selectUseParentalStatus').prop('checked',true);
				}

				$scope.isCrossesImport = isCrossesImport;
				$scope.hasHybridMethod = ImportCrosses.hasHybridMethod;
				$scope.valuecontainer = {methodCode : null};
				$scope.selectedBreedingMethodId = null;
				$('#crossSettingsModal').one('show.bs.modal', function() {
					ImportCrosses.resetCrossSettingsModal();
				});
			};

			$scope.goBackToImportCrosses = function () {
				ImportCrosses.goBackToPage('#crossingBreedingMethodModal', '.import-crosses-section .modal');

			};

			$scope.goToNamingModal = function () {
				if ($scope.isBreedingMethodSelectedValid()) {
					$('#crossingBreedingMethodModal').modal('hide');
					$rootScope.breedingMethodSetting = {
						methodId : $scope.selectedBreedingMethodId,
						basedOnStatusOfParentalLines : $('#selectUseParentalStatus').prop('checked'),
						basedOnImportFile : $('#selectMethodInImportFile').prop('checked')
					};
					$rootScope.applyGroupingToNewCrossesOnly = $scope.applyGroupingToNewCrossesOnly;
					setTimeout(ImportCrosses.showImportSettingsPopup, 500);
				}
			};

		}]);
})();
