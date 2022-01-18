/*global showErrorMessage, createErrorNotification, crossingImportErrorHeader, isInt, crossingExportErrorHeader, invalidImportedFile,
getJquerySafeId, SaveAdvanceList, BreedingMethodsFunctions */
var ImportCrosses = {
	CROSSES_URL: '/Fieldbook/crosses',
	showFavoriteLocationsOnly: true,
	preservePlotDuplicates: false,
	isFileCrossesImport: true,
	hasHybridMethod: false,
	hybridMethods: null,
	showPopup: function() {
		'use strict';
		$('#fileupload-import-crosses').val('');
		$('.import-crosses-section .modal').modal({ backdrop: 'static', keyboard: true });
		$('.import-crosses-section .modal .fileupload-exists').click();
		ImportCrosses.showFavoriteLocationsOnly = true;
	},

	doSubmitImport: function() {
		'use strict';

		if ($('#fileupload-import-crosses').val() === '') {
			showErrorMessage('', $.fieldbookMessages.errorNoFileSelectedForImport);
			return false;
		}

		ImportCrosses.submitImport($('.import-crosses-section')).done(function(resp) {

			if (!resp.isSuccess) {
				createErrorNotification(crossingImportErrorHeader, resp.error.join('<br/>'));
				return;
			}

			if (resp.warnings) {
				createWarningNotification(warningMsgHeader, resp.warnings.join('<br/>'), 10000);
			}

			ImportCrosses.preservePlotDuplicates = false;
			$('.import-crosses-section .modal').modal('hide');
			$('#openCrossesListModal').data('hasPlotDuplicate', resp.hasPlotDuplicate);
			// show review crosses page

			ImportCrosses.isFileCrossesImport = true;
			createdCrossesListId = null;

			ImportCrosses.hasHybridMethod = resp.hasHybridMethod;


			if (resp.isChoosingListOwnerNeeded) {
				$('#chooseListOwner').modal({ backdrop: 'static', keyboard: true });
				$('#chooseListOwner').addClass('import-crosses-from-file');

				$('#goBackToImportFileCrossesButton').off('click');
				$('#goBackToImportFileCrossesButton').on('click', function() {
					ImportCrosses.goBackToPage('#chooseListOwner', '.import-crosses-section .modal');
				});

				$('#chooseListOwnerNextButton').on('click', function() {
					$('#chooseListOwner').modal('hide');
					setTimeout(ImportCrosses.showPlotDuplicateConfirmation, 500);

				});

			} else {
				setTimeout(ImportCrosses.showPlotDuplicateConfirmation, 500);
			}

		});

	},

	openBreedingModal: function() {
		'use strict';
		$('#crossingBreedingMethodModal').modal({ backdrop: 'static', keyboard: true });
		var $scope = angular.element('#crossingBreedingMethodModal').scope();
		$scope.init(createdCrossesListId == null || createdCrossesListId == 0);
		$scope.$apply();
	},

	resetCrossSettingsModal: function () {
		$('#crossPrefix').val('');
		$('#sequenceNumberDigits').select2('val', '');
		$('#crossSuffix').val('');
		$('input:radio[name=hasPrefixSpace][value=' + false + ']').prop('checked', true);
		$('input:radio[name=hasSuffixSpace][value=' +false + ']').prop('checked', true);
		$('input:radio[name=manualNamingSettings][value=' +false + ']').prop('checked', true);
		$('input:radio[name=hasParentageDesignationName][value=' +false + ']').prop('checked', true);
		$('#parentageDesignationSeparator').val('/');
		$('#startingSequenceNumber').val('');
		$('#locationDropdown').select2('val', '');
		$('#importNextSequenceName').text('');
		$('#presetName').val('');
		$('#checkExistingCrosses').prop('checked', false);
		$('#showOnlyRecordsWithAlerts').prop('checked', false);
		$('#omitAlertedCrossesCheckbox').prop('checked', false);
	},

	hasPlotDuplicate: function() {
		'use strict';
		if ($('#openCrossesListModal').data('hasPlotDuplicate') === true) {
			return true;
		}
		return false;
	},

	openCrossesList: function(createdCrossesListId) {
		'use strict';


		$('#openCrossesListModal').one('shown.bs.modal', function() {
			// After the modal window is shown, make sure that the table header is properly adjusted.
			$('#preview-crosses-table').resize();
		}).modal({ backdrop: 'static', keyboard: true });

		if (ImportCrosses.isFileCrossesImport) {
			$('#openCrossesListModal').addClass('import-crosses-from-file');
		}

		$('#openCrossesListModal').on('hidden.bs.modal', function() {
			$('#openCrossesListModal').removeClass('import-crosses-from-file');
		});

		$('#crossSettingsModal').on('hidden.bs.modal', function() {
			//we should clear the form and form fields on closing as we are going to reuse it later
			//TODO clear other fields as well, some of them need to set the default value back
			$('#crossSettingsModal').removeClass('import-crosses-from-file');
		});


		$('#openCrossListNextButton').off('click');
		$('#openCrossListNextButton').on('click', function() {
			$('#openCrossesListModal').modal('hide');
			// delete temporary list created from Design Crosses
			ImportCrosses.deleteCrossList(createdCrossesListId)
				.done(function () {
					var omitAlertedCrosses = $('#omitAlertedCrossesCheckbox').is(':checked');
					ImportCrosses.openSaveListModal(omitAlertedCrosses);
				})
				.fail(function () {
					showErrorMessage('', 'Could not delete cross list');
				});
		});

		$('#goBackToNamingModal').off('click');
		$('#goBackToNamingModal').on('click', function() {
			ImportCrosses.goBackToPage('#openCrossesListModal', '#crossSettingsModal');
		});

		return ImportCrosses.getImportedCrossesTable(createdCrossesListId).done(function(response) {
			if (response.isSuccess === 0) {
				showErrorMessage('', response.error);
				return;
			}
			var checkExistingCrosses = $('#checkExistingCrosses').is(':checked');
			new  BMS.Fieldbook.PreviewCrossesDataTable('#preview-crosses-table', response.listDataTable, response.tableHeaderList,
				response.isImport, checkExistingCrosses);
		}).fail(function (jqXHR, textStatus) {
			showErrorMessage('', textStatus);
		});
	},

	viewExistingCrosses: function(femaleGID, maleGIDs, cross, breedingMethodId, gid) {
		$('#openCrossesListModal').modal('hide');
		$('#existingCrossesModal').one('shown.bs.modal', function() {
			$('#existingCrossesModal .modal-title').html(cross);
			$('#existingCrossesModal .existing-cross-label').html('<i>Cross <b>' + cross + '</b> already exists in the database.</i>');
			// After the modal window is shown, make sure that the table header is properly adjusted.
			$('#existing-crosses-table').resize();
		}).modal({ backdrop: 'static', keyboard: true });

		$('#existingCrossesModal').on('hidden.bs.modal', function () {
			$('#existingCrossesModal').modal ('hide');
			$('#openCrossesListModal').one('shown.bs.modal', function() {
				// After the modal window is shown, make sure that the table header is properly adjusted.
				$('#preview-crosses-table').resize();
			}).modal({ backdrop: 'static', keyboard: true });
		});

		return ImportCrosses.getExistingCrossesTable(femaleGID, maleGIDs, breedingMethodId, gid).done(function(response) {
			if (response.isSuccess === 0) {
				showErrorMessage('', response.error);
				return;
			}
			new  BMS.Fieldbook.ExistingCrossesDataTable('#existing-crosses-table', response.listDataTable, response.tableHeaderList);
		}).fail(function (jqXHR, textStatus) {
			showErrorMessage('', textStatus);
		});

	},

	deleteCrossList: function (createdCrossesListId) {
		if (!createdCrossesListId) {
			return $.Deferred().resolve();
		}
		return $.ajax({
			url: ImportCrosses.CROSSES_URL + '/deleteCrossList/' + createdCrossesListId,
			type: 'DELETE',
			cache: false,
			global: false
		});
	},

	goBackToPage: function(hiddenModalSelector, shownModalSelector) {
		'use strict';
		$(hiddenModalSelector).modal('hide');
		$(shownModalSelector).one('shown.bs.modal', function() {
			// The bootstrap modal intermittently adds extra padding from the left that makes the layout of dialog content inconsistent when loading.
			// We add this line to prevent that inconsistency with padding.
			$(shownModalSelector).addClass('remove-excess-padding');
		}).modal({ backdrop: 'static', keyboard: true });
	},

	getImportedCrossesTable: function(createdCrossesListId) {
		'use strict';
		var checkExistingCrosses = $('#checkExistingCrosses').is(':checked');
		var crossesURL = ImportCrosses.CROSSES_URL + '/getImportedCrossesList' + '/' + checkExistingCrosses + '/' + (createdCrossesListId &&
		createdCrossesListId.length > 0 ? createdCrossesListId : '');
		return $.ajax(
			{
				url: crossesURL,
				type: 'GET',
				cache: false,
				global: false
			});
	},

	getExistingCrossesTable: function(femaleGID, maleGIDs, breedingMethodId, gid) {
		'use strict';
		var crossesURL = ImportCrosses.CROSSES_URL + '/getExistingCrossesList' + '/' + femaleGID + '/' + maleGIDs + '/' + gid;
		return $.ajax(
			{
				url: crossesURL,
				type: 'GET',
				cache: false,
				global: false
			});
	},

	submitImport: function($importCrossesForm) {
		'use strict';
		var deferred = $.Deferred();
		$importCrossesForm.ajaxForm({
			dataType: 'json',
			success: function(response) {
				deferred.resolve(response);
			},
			error: function(response) {
				createErrorNotification(crossingImportErrorHeader, invalidImportedFile);
				deferred.reject(response);
			}
		}).submit();

		return deferred.promise();
	},


	showPlotDuplicateConfirmation: function() {
		'use strict';
		if (ImportCrosses.hasPlotDuplicate()) {
			/** Functionality temporarily suppress by the v4 public release.  See issue: BMS-3514 **/
			/*	//show the confirmation now
				$('#duplicate-crosses-modal input[type=checkbox]').prop('checked', ImportCrosses.preservePlotDuplicates);

				$('#duplicate-crosses-modal').modal({ backdrop: 'static', keyboard: true });

				$('#continue-duplicate-crosses').off('click');
				$('#continue-duplicate-crosses').on('click', function() {
					//get the value of the checkbox
					ImportCrosses.preservePlotDuplicates = $('#duplicate-crosses-modal input[type=checkbox]').is(':checked');
					$('#duplicate-crosses-modal').modal('hide');
					setTimeout(ImportCrosses.showImportSettingsPopup, 500);
				});
				*/
			/** End Functionality temporarily suppress **/
			/** Palliative for BMS-3514 **/
			ImportCrosses.preservePlotDuplicates = true;
			ImportCrosses.openBreedingModal();
			/** End Palliative **/
		} else {
			ImportCrosses.openBreedingModal();
		}
	},

	showImportSettingsPopup: function() {
		'use strict';
		var crossSettingsPopupModal = $('#crossSettingsModal');
		crossSettingsPopupModal.modal({ backdrop: 'static', keyboard: true });

		$('#presetSettingsDelete').off('click');
		$('#presetSettingsDelete').on('click', function () {

			var data = $('#presetSettingsDropdown').select2('data');
			if (!(data && data.programPresetId)) {
				return;
			}

			crossSettingsPopupModal.modal('hide');
			crossSettingsPopupModal.data('open', '1');

			var deleteModalElm = $('#fbk-delete-import-settings-confirm');
			$('#fbk-delete-import-settings-confirm .yes').on('click', function() {
				deleteModalElm.modal('hide');
				deleteModalElm.data('open', '1');
				setTimeout(function () {
					crossSettingsPopupModal.modal({ backdrop: 'static', keyboard: true })
					ImportCrosses.onDeleteSettingOk(data);
				}, 500)
			});

			$('#fbk-delete-import-settings-confirm .no').on('click', function() {
				deleteModalElm.modal('hide');
				deleteModalElm.data('open', '1');
				setTimeout(function () {
					crossSettingsPopupModal.modal({ backdrop: 'static', keyboard: true })
				}, 500)
			});
			deleteModalElm.modal({ backdrop: 'static', keyboard: true });
		});


		$('#goBackToSelectBreedingMethodModal').off('click');
		$('#goBackToSelectBreedingMethodModal').on('click', function() {
			ImportCrosses.showFavoriteLoationsOnly = $('#locationFavoritesOnlyCheckbox').is(':checked');
			ImportCrosses.showAllLocationOnly = $('#showAllLocationOnlyRadio').is(':checked');
			ImportCrosses.goBackToPage('#crossSettingsModal', '#crossingBreedingMethodModal');
		});
	},

	onDeleteSettingOk: function(data) {

	},

	updateSampleParentageDesignation: function() {
		'use strict';
		var value = $('#parentageDesignationSeparator').val();
		$('#sampleParentageDesignation').text('FEMALE-123' + value + 'MALE-456');
	},

	deleteImportSettings: function(programPresetId) {
		'use strict';
		return $.ajax({
			url: ImportCrosses.CROSSES_URL + '/deleteSetting/' + programPresetId,
			type: 'DELETE',
			cache: false,
			error: function (jqXHR, textStatus, errorThrown) {
				console.log('The following error occurred: ' + textStatus, errorThrown);
			},
			complete: function () {
			}
		});
	},

	exportCrosses: function() {
		'use strict';
		//TODO handle errors for ajax request
		return $.ajax({
			url: ImportCrosses.CROSSES_URL + '/export',
			type: 'GET',
			cache: false
		});
	},

	downloadCrosses: function() {
		'use strict';
		ImportCrosses.exportCrosses().done(function(result) {
			if (result.isSuccess) {
				var downloadUrl = ImportCrosses.CROSSES_URL + '/download/file';

				$.fileDownload(downloadUrl, {
					httpMethod: 'POST',
					data: result
				});
			} else {
				createErrorNotification(crossingExportErrorHeader, result.errorMessage);
			}
		});
	},
	openSaveListModal: function(omitAlertedCrosses) {
		'use strict';
		var  germplasmTreeNode = $('#germplasmFolderTree').dynatree('getTree');
		//TODO handle errors for ajax request
		$.ajax({
			url: '/Fieldbook/ListTreeManager/saveCrossesList/',
			type: 'GET',
			cache: false,
			success: function(html) {
				$('#saveGermplasmRightSection').html(html);
				$('#omitAlertedCrosses').val(omitAlertedCrosses);
				$('#saveListTreeModal').modal({
					show: true,
					keyboard: true,
					backdrop: 'static'
				});
				$('#saveListTreeModal').data('is-save-crosses', '1');

				TreePersist.preLoadGermplasmTreeState(false, '#germplasmFolderTree', true);

				//we preselect the program lists
				if (germplasmTreeNode !== null && germplasmTreeNode.getNodeByKey('LOCAL') !== null) {
					germplasmTreeNode.getNodeByKey('LOCAL').activate();
				}
			},
			error: function() {
				//TODO process errors
			}
		});
	},

	openGermplasmModalFromExistingCrossesView : function (gid) {
		'use strict';
		$('#existingCrossesModal').off('hidden.bs.modal');
		$('#existingCrossesModal').off('shown.bs.modal');
		$('#existingCrossesModal').modal('hide');

		const callback = function () {
			$('#existingCrossesModal').on('hidden.bs.modal', function () {
				$('#existingCrossesModal').modal ('hide');
				$('#openCrossesListModal').one('shown.bs.modal', function() {
					// After the modal window is shown, make sure that the table header is properly adjusted.
					$('#preview-crosses-table').resize();
				}).modal({ backdrop: 'static', keyboard: true });
			});
			$('#existingCrossesModal').one('shown.bs.modal', function() {
				// After the modal window is shown, make sure that the table header is properly adjusted.
				$('#existing-crosses-table').resize();
			}).modal({ backdrop: 'static', keyboard: true });
		}
		openGermplasmDetailsPopup(gid, callback);
	},

	openGermplasmModal : function (gid) {
		'use strict';
		$('#openCrossesListModal').off('hidden.bs.modal');
		$('#openCrossesListModal').off('shown.bs.modal');
		$('#openCrossesListModal').modal('hide');

		const callback = function() {
			$('#openCrossesListModal').one('shown.bs.modal', function() {
				// After the modal window is shown, make sure that the table header is properly adjusted.
				$('#preview-crosses-table').resize();
			}).modal({ backdrop: 'static', keyboard: true });
		}

		openGermplasmDetailsPopup(gid, callback);
	}
};

$(document).ready(function() {
	'use strict';
	$('.import-crosses').off('click');
	$('.btn-import-crosses').off('click');
	$('.import-crosses').on('click', ImportCrosses.showPopup);
	$('.btn-import-crosses').on('click', ImportCrosses.doSubmitImport);
	$('.import-crosses-section .modal').on('hide.bs.modal', function() {
		$('div.import-crosses-file-upload').parent().parent().removeClass('has-error');
	});

	// handler for the showing on records with Alerts filtering
	$('#showOnlyRecordsWithAlerts').on('change', function() {
		if ( $('#showOnlyRecordsWithAlerts').is(':checked')) {
			$('#preview-crosses-table').DataTable().column(0).search('View Existing Crosses', true, false).draw();
		} else {
			$('#preview-crosses-table').DataTable().column(0).search('', true, false).draw();
		}
	});

	$('#checkExistingCrosses').on('change', function() {
		if ( $('#checkExistingCrosses').is(':checked')) {
			$('#showOnlyRecordsWithAlertsDiv').removeClass('fbk-hide');
			$('#omitAlertedCrossesDiv').removeClass('fbk-hide');
		} else {
			$('#showOnlyRecordsWithAlertsDiv').addClass('fbk-hide');
			$('#omitAlertedCrossesDiv').addClass('fbk-hide');
		}
	});
});
