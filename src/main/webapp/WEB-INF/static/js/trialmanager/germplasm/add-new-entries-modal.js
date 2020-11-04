(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.controller('AddNewEntriesController', ['$scope', '$rootScope', '$uibModal', '$uibModalInstance', 'studyEntryService',
		function ($scope, $rootScope, $uibModal, $uibModalInstance, studyEntryService) {

			const CHECK_ENTRY_ID = 10180;

			$scope.selected = {};
			$scope.entryTypes = [];
			$scope.selectedGids = [];

			$scope.init = function () {
				studyEntryService.getEntryTypes().then(function (entryTypes) {
					buildEntryTypes(entryTypes);
				})
			};

			$scope.cancel = function () {
				$uibModalInstance.dismiss();
			};

			$scope.openSelectGermplasmForAddingEntries = function() {
				$uibModal.open({
					templateUrl: '/Fieldbook/static/js/trialmanager/germplasm-selector/germplasm-selector-modal.html',
					controller: "GermplasmSelectorCtrl",
					windowClass: 'modal-very-huge',
				}).result.then((gids) => {
					if (gids != null) {
					$scope.selectedGids = gids;
				}
			});
			};

			$scope.addNewEntries = function () {
				if(validateSelectedGIDs()) {
					studyEntryService.getStudyEntries().then(function (studyEntries) {
						var gids = [];
						studyEntries.forEach(function (studyEntry) {
							gids.push(String(studyEntry.gid));
						});
						var existingGids = $scope.selectedGids.filter(function (gid) {
							return gids.includes(String(gid));
						});
						if(existingGids.length === 0) {
							proceedWithAddingNewEntries();
						} else {
							var message = $.germplasmMessages.addEntriesExistingGids.replace('{0}', existingGids);
							var modalConfirmDelete = $scope.openConfirmModal(message, 'Yes', 'No');
							modalConfirmDelete.result.then(function (shouldContinue) {
								if (shouldContinue) {
									proceedWithAddingNewEntries();
								}
							});
						}
					});
				}
			};

			function proceedWithAddingNewEntries() {
				studyEntryService.saveStudyEntries(null, $scope.selected.entryType.id, $scope.selectedGids).then(function () {
					showSuccessfulMessage('', $.germplasmMessages.addEntriesSuccess);
					$uibModalInstance.close();
					$rootScope.$emit("reloadStudyEntryTableData", {});
				});
			}

			function validateSelectedGIDs() {
				var selectedGidsString = String($scope.selectedGids).replace('[', '').replace(']', '');
				//remove whiteSpace
				selectedGidsString = selectedGidsString.split(/\s/).join('');

				let regex = /[0-9]+(,[0-9]+)*/g;
				if(regex.test(selectedGidsString)) {
					var selectedGidArray = selectedGidsString.split(',');
					$scope.selectedGids = [];
					selectedGidArray.forEach(function(value) {
						$scope.selectedGids.push(value);
					});
					return true;
				} else {
					showErrorMessage('', $.germplasmMessages.addEntriesGidsError);
				}
			}

			function buildEntryTypes(entryTypes) {
				entryTypes.forEach(function (entryType) {
					$scope.entryTypes.push(entryType);
					//Set Check Entry as default value
					if(entryType.id === CHECK_ENTRY_ID) {
						$scope.selected.entryType = entryType;
					}
				});
			}

			$scope.init();
		}
	]);

})();
