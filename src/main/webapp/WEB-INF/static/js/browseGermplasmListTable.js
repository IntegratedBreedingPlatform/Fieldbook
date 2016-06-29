/*exported startIndex,germplasmStartingEntry,importLocationUrl,importIframeOpened,overwriteChecksList*/
/*exported confirmReplaceList,resetGermplasmList,removeCheckFromList,openListTree,viewGermplasmLitDetails */
/*exported additionalLazyLoadUrl, chooseList*/
/*globals angular, isNursery*/

var addedGid = {},
	listId = 0,
	makeDraggableBool = true,

	startIndex = 0,
	lastDraggedPrimaryList = 0,
	lastDraggedChecksList = 0,

	itemsToAdd = [],
	checksFromPrimary = 0,

	importIframeOpened = false,
	itemsIndexAdded = [];

var makeCheckDraggableBool = true;

//to be use as reference to the data table object
var germplasmDataTable = null,
		selectedCheckListDataTable = null;

function resetGermplasmList() {
	'use strict';
	$('#imported-germplasm-list').html('<h3></h3>');
	$('#imported-germplasm-list-reset-button').css('opacity', '0');
	$('#entries-details').css('display', 'none');
	$('#numberOfEntries').text('');
	listId = 0;
	lastDraggedPrimaryList = 0;

	if (!isNursery()) {
		var trialManagerDataService = angular.element('#mainApp').injector().get('TrialManagerDataService');
		trialManagerDataService.indicateUnappliedChangesAvailable(true);
	}

	$.ajax({
		url: '/Fieldbook/NurseryManager/importGermplasmList/resetNurseryGermplasmDetails',
		type: 'GET',
		cache: false,
		async: false,
		success: function() {
		}
	});
	if (isNursery()) {
		addFakeCheckTable();

		// hide Import Crosses in Actions menu
		if ($('#createNurseryMainForm #studyId').length === 1) {
			$('#import-crosses').css('display', 'none');
		} else {
			$('#main-actions-btn').addClass('fbk-hide');
		}
	} else {
		if (angular && angular.element("#manage-trial-tabs [ui-view='germplasm']").length === 1) {
			var _scope = angular.element("#manage-trial-tabs [ui-view='germplasm']").scope();

			setTimeout(function() {
				_scope.$apply(function() {
					_scope.germplasmListCleared();
					_scope.updateOccurred = false;
				});
			}, 1);

		}
	}

}

function showPopoverCheck(index, sectionContainer, bodyContainer) {
	'use strict';
	//if replace has been clicked or if new nursery/trial or if there are no measurement rows saved yet for trial
	var isShowPopOver = ($('#chooseGermplasmAndChecks').data('replace') && parseInt($('#chooseGermplasmAndChecks').data('replace')) === 1)
		|| ($('#studyId').length === 0 && isNursery()) || (!isOpenTrial() && !isNursery())
		|| (isOpenTrial() && $('body').data('service.trialMeasurement.count') === 0)
		|| ($('#studyId').length !== 0 && isNursery() && measurementRowCount === 0);

	if (isShowPopOver) {
		var currentCheckVal = $(sectionContainer + ' #selectedCheck' + index).val(),
			realIndex = index,
			suffix = '/N',
			popoverOptions = {},
			listDataTable = selectedCheckListDataTable;
		if (!isNursery()) {
			suffix = '/T';
			listDataTable = germplasmDataTable;
		}

		//we need to get the real index of the check

		if (listDataTable != null) {
			listDataTable.getDataTable().$('.check-hidden').each(function(indexCount) {
				if ($(this).attr('id') === 'selectedCheck' + index) {
					realIndex = indexCount;
					return;
				}
			});
		}
		$.ajax({
			url: '/Fieldbook/NurseryManager/GermplasmList/edit/check/' + index + '/' + realIndex + suffix,
			type: 'GET',
			data: 'currentVal=' + currentCheckVal,
			cache: false,
			success: function(data) {

				if (!isNursery()) {
					popoverOptions = {
						html: true,
						title: 'Edit Check',
						content: data,
						trigger: 'manual',
						placement: 'right',
						container: 'body'
					};
				} else {
					popoverOptions = {
						html: true,
						title: 'Edit Check',
						content: data,
						trigger: 'manual',
						placement: 'left',
						container: 'body'
					};
				}
				$('body .popover').remove();
				$(sectionContainer + ' .edit-check' + index).popover('destroy');
				$(sectionContainer + ' .edit-check' + index).popover(popoverOptions);
				$(sectionContainer + ' .edit-check' + index).popover('show');
			}
		});
	}
}
