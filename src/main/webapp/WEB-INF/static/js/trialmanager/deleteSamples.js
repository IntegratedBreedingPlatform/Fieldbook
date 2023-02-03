/*globals $,showErrorMessage, console  */
var DeleteSamples = {};
(function () {
	'use strict';

	var baseurl = '/bmsapi/crops/' + cropName + '/sample-lists';
	var xAuthToken = JSON.parse(localStorage["bms.xAuthToken"]).token;
	var selectedEntries = [];

	DeleteSamples.removeSelectedEntries = function (listId, listName) {
		'use strict';

		$('input:checkbox[name=sample-entry-' + listId + ']:checked').each(function(){
			selectedEntries.push($(this).val());
		});

		if(selectedEntries.length < 1) {
			showErrorMessage('', 'Please select at least one entry.');
			return;
		}

		return $.ajax({
			url: baseurl + '/' + listId + '/entries?programUUID='+ currentProgramId + "&selectedEntries="
				+ selectedEntries.join(","),
			contentType: 'application/json',
			type: 'DELETE',
			beforeSend: function (xhr) {
				xhr.setRequestHeader('X-Auth-Token', xAuthToken);
			},
			success: function(resp) {
				showSuccessfulMessage('', removeSampleListEntriesSuccessfullyMessage);
				displaySampleList(listId, listName, false);
			}
		});
	}
})();
