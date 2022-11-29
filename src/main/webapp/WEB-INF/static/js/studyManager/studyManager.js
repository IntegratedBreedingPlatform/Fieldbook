/*global getJquerySafeId, showErrorMessage, oldLineSelected, changeAdvanceBreedingMethod, setCorrecMethodValues, oldMethodSelected, msgSamplePlotError, msgHarvestDateError, methodSuggestionsFav_obj, isInt, breedingMethodId, oldMethodSelected*/
/*global isStudyNameUnique, validateStartDateEndDateBasic*/

function sortByKey(array, key) {
	return array.sort(function(a, b) {
		var x = a[key].toLowerCase();
		var y = b[key].toLowerCase();
		return ((x < y) ? -1 : ((x > y) ? 1 : 0));
	});
}

function initializeDateAndSliderInputs() {
	if ($('.date-input').length > 0) {
		$('.date-input').placeholder().each(function() {
			$(this).datepicker({
				'format': 'yyyy-mm-dd'
			}).on('changeDate', function(ev) {
				$(this).datepicker('hide');
			});
		});
	}
	if ($('.datepicker img').length > 0) {
		$('.datepicker img').on('click', function() {
			$(this).parent().parent().find('.date-input').datepicker('show');
		});
	}
	if ($('.spinner-input').length > 0) {

		$('.spinner-input').each(
				function() {
					var currentVal = $(this).val() == '' ? parseFloat($(this)
							.data('min')) : parseFloat($(this).val());
					$(this).spinedit({
						minimum: parseFloat($(this).data('min')),
						maximum: parseFloat($(this).data('max')),
						step: parseFloat($(this).data('step')),
						value: currentVal,
						numberOfDecimals: 4
					});
				});
	}
}

function displaySaveSuccessMessage(idDomSelector, messageToDisplay) {
	'use strict';
	createSuccessNotification(successMsgHeader, messageToDisplay);
}

function discardImportedData() {
	$('#discardImportDataConfirmation').modal({
		backdrop: 'static',
		keyboard: true
	});
}

