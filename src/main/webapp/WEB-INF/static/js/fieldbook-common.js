$(function () {
	'use strict';

	// attach spinner operations to ajax events
	jQuery.ajaxSetup({
		beforeSend: function () {
			SpinnerManager.addActive();
		},
		complete: function () {
			SpinnerManager.resolveActive();
		},
		success: function () {
		},
		error: function (jqXHR, textStatus, errorThrown) {
			if (jqXHR.status === 500) {
				showErrorMessage('', ajaxGenericErrorMsg);
			} else {
				showErrorMessage('INVALID INPUT', jqXHR.responseText);
			}
		}
	});

	function measureScrollBar() {
		// david walsh
		var inner = document.createElement('p');
		inner.style.width = '100%';
		inner.style.height = '200px';

		var outer = document.createElement('div');
		outer.style.position = 'absolute';
		outer.style.top = '0px';
		outer.style.left = '0px';
		outer.style.visibility = 'hidden';
		outer.style.width = '200px';
		outer.style.height = '150px';
		outer.style.overflow = 'hidden';
		outer.appendChild(inner);

		document.body.appendChild(outer);
		var w1 = inner.offsetWidth;
		outer.style.overflow = 'scroll';
		var w2 = inner.offsetWidth;
		if (w1 == w2) {
			w2 = outer.clientWidth;
		}

		document.body.removeChild(outer);

		return (w1 - w2);
	}


	$(document.body)
		.on('show.bs.modal', function () {
			if (this.clientHeight < window.innerHeight) {
				return;
			}

			var scrollbarWidth = measureScrollBar();
			if (scrollbarWidth) {
				$(document.body).css('padding-right', scrollbarWidth);
			}
		})
		.on('shown.bs.modal', function () {
			/**
			 * XXX Multiple modals are not supported in bootstrap.
			 * https://bootstrapdocs.com/v3.3.6/docs/javascript/#callout-stacked-modals
			 * If we are opening two modals at the same time or chaining one after another,
			 * the closing modal will remove
			 * the modal-open class from the body, making the scrollbar disappear
			 */
			$(this).addClass('modal-open');
		})
		.on('hidden.bs.modal', function () {
			$(document.body).css('padding-right', 0);

			// is there any other modal open?
			if ($('.modal.in').length > 0) {
				/**
				 * Bootstrap will remove modal-open on hide:
				 * https://github.com/twbs/bootstrap/blob/81df608a40bf0629a1dc08e584849bb1e43e0b7a/dist/js/bootstrap.js#L1081
				 * causing issues with the scroll of other modals
				 * This is to avoid that
				 */
				$(document.body).addClass('modal-open');
			}
		});

	$('.fbk-help')
		.click(function () {
			var helpModule = $(this).data().helpLink;
			$.get('/ibpworkbench/controller/help/getUrl/' + helpModule).success(function (helpUrl) {
				if (!helpUrl || !helpUrl.length) {
					$.when(
						$.get('/ibpworkbench/controller/help/headerText'),
						$.get('/ibpworkbench/VAADIN/themes/gcp-default/layouts/help_not_installed.html')
					).done(function (headerText, helpHtml) {
						bootbox.dialog({
							title: headerText[0],
							message: helpHtml[0],
							className: 'help-box',
							onEscape: true
						});
					});
				} else {
					window.open(helpUrl);
				}
			});
		});

});

function isStudyNameUnique(studyName, studyId) {
	'use strict';
	if (!studyId) {
		studyId = 0;
	}

	var isUnique = true;
	$.ajax({
		url: '/Fieldbook/StudyTreeManager/isNameUnique',
		type: 'POST',
		data: 'studyId=' + studyId + '&name=' + studyName,
		cache: false,
		async: false,
		success: function (data) {
			if (data.isSuccess == 1) {
				isUnique = true;
			} else {
				isUnique = false;
			}
		}
	});
	return isUnique;
}

function validateStartEndDateBasic(startDate, endDate) {

	'use strict';

	startDate = startDate == null ? '' : startDate.replace(/-/g, '');
	endDate = endDate == null ? '' : endDate.replace(/-/g, '');

	if (startDate === '' && endDate === '') {
		return true;
	} else if (startDate !== '' && endDate === '') {
		return true;
	} else if (startDate === '' && endDate !== '') {
		return startDateRequiredError;
	} else if (parseInt(startDate) > parseInt(endDate)) {
		return startDateRequiredEarlierError;
	}

	return true;

}

function showMultiTabPage(paginationUrl, pageNum, sectionDiv, sectionContainerId, paginationListIdentifier) {
	'use strict';
	$.ajax({
		url: paginationUrl + pageNum + '?listIdentifier=' + paginationListIdentifier,
		type: 'GET',
		data: '',
		cache: false,
		success: function (html) {
			var paginationDiv = '#' + sectionContainerId + ' #' + sectionDiv;
			$(paginationDiv + ':eq(0)').html('');
			$(paginationDiv + ':eq(0)').html(html);
		}
	});
}

function triggerFieldMapTableSelection(tableName) {

	var id;

	$('#' + tableName + ' tr.data-row').on('click', function () {
		if (tableName == 'studyFieldMapTree') {
			$(this).toggleClass('trialInstance');
			$(this).toggleClass('field-map-highlight');

		} else {
			$(this).toggleClass('field-map-highlight');
			id = $(this).attr('id') + '';
			if ($(this).hasClass('field-map-highlight')) {
				selectedTableIds[id] = id;
			} else {
				selectedTableIds[id] = null;
			}
		}
	});
}

function createFieldMap() {

	if ($('.import-study-data').data('data-import') === '1') {
		showErrorMessage('', needSaveImportDataError);
		return;
	}

	if (($('.review-trial-page-identifier').length)) {
		var mode = '.active .review-trial-page-identifier';
		var active = '.active';
	} else {
		var mode = '#createTrialMainForm';
		var active = '';
	}
	var id = $(mode + ' #studyId').val(),
		name = $(active + ' #studyName').val();
	showFieldMapPopUpCreate(id);
}


function $safeId(fieldId) {
	return $(getJquerySafeId(fieldId));
}

function getJquerySafeId(fieldId) {
	return replaceall(fieldId, '.', '\\.');
}

function replaceall(str, replace, withThis) {
	var strHasil = '',
		temp;

	for (var i = 0; i < str.length; i++) { // not need to be equal. it causes the last change: undefined..
		if (str[i] == replace) {
			temp = withThis;
		} else {
			temp = str[i];
		}
		strHasil += temp;
	}
	return strHasil;
}

function isInt(value) {
	if ((undefined === value) || (null === value) || (value === '')) {
		return false;
	}
	return value % 1 === 0;
}

function isFloatNumber(val) {
	if (!val || (typeof val != 'string' || val.constructor != String)) {
		return (false);
	}
	var isNumber = !isNaN(Number(val));
	if (isNumber) {
		if (val.indexOf('.') != -1) {
			return (true);
		} else {
			return isInt(val);
		}
	} else {
		return (false);
	}
}

function selectTrialInstance() {
	$.ajax({
		url: '/Fieldbook/Fieldmap/enterFieldDetails/selectTrialInstance',
		type: 'GET',
		cache: false,
		data: '',
		success: function (data) {
			if (data.fieldMapInfo != null && data.fieldMapInfo != '') {
				if (parseInt(data.size) > 1) {
					// Show popup to select fieldmap to display
					clearStudyTree();
					isViewFieldmap = true;
					createStudyTree($.parseJSON(data.fieldMapInfo), isViewFieldmap);
					$('#selectTrialInstanceModal').modal('toggle');
				} else {
					// Redirect to step 3
					var fieldMapInfo = $.parseJSON(data.fieldMapInfo);
					isFieldMapHasInvalidValues = data.hasInvalidValues == 'true';
					if (!isFieldMapHasInvalidValues) {
						var datasetId = data.datasetId;
						var environmentId = data.environmentId;
						location.href = '/Fieldbook/Fieldmap/generateFieldmapView/viewFieldmap/trial/' + datasetId + '/' + environmentId;
					} else {
						showErrorMessage('', invalidFieldMapCoordinates);
					}

				}
			}
		}
	});
}

function selectTrialInstanceCreate() {
	$.ajax({
		url: '/Fieldbook/Fieldmap/enterFieldDetails/selectTrialInstance',
		type: 'GET',
		async: false,
		cache: false,
		data: '',
		success: function (data) {
			if (data.fieldMapInfo != null && data.fieldMapInfo != '') {
				// Show popup to select instances to create field map
				clearStudyTree();
				isViewFieldmap = false;
				createStudyTree($.parseJSON(data.fieldMapInfo), isViewFieldmap);
				$('#selectTrialInstanceModal').modal('toggle');
			}
		}
	});
}

function createStudyTree(fieldMapInfoList, hasFieldMap) {
	var hasOneInstance = false;
	isFieldMapHasInvalidValues = new Map();
	createHeader(hasFieldMap);
	$.each(fieldMapInfoList, function (index, fieldMapInfo) {
		createRow(getPrefixName('study', fieldMapInfo.fieldbookId), '', fieldMapInfo.fieldbookName, fieldMapInfo.fieldbookId, hasFieldMap, hasOneInstance);
		$.each(fieldMapInfo.datasets, function (index, value) {
			hasOneInstance = fieldMapInfoList.length === 1 && fieldMapInfoList[0].datasets.length === 1 && fieldMapInfoList[0].datasets[0].trialInstances.length === 1;
			// Create study tree up to instance level
			createRow(getPrefixName('dataset', value.datasetId), getPrefixName('study', fieldMapInfo.fieldbookId), value.datasetName, value.datasetId, hasFieldMap, hasOneInstance);
			$.each(value.trialInstances, function (index, childValue) {
				if ((hasFieldMap && childValue.hasFieldMap) || !hasFieldMap) {
					isFieldMapHasInvalidValues[childValue.instanceId] = childValue.hasInValidValue;
					createRow(getPrefixName('trialInstance', childValue.environmentId), getPrefixName('dataset', value.datasetId), childValue, childValue.instanceId, hasFieldMap, hasOneInstance);
				}
			});
		});
	});

	// Set bootstrap ui
	$('.tree').treegrid();

	$('.tr-expander').on('click', function () {
		triggerExpanderClick($(this));
	});
	$('.treegrid-expander').on('click', function () {
		triggerExpanderClick($(this).parent().parent());

	});

	// Set as highlightable
	if (hasFieldMap) {
		triggerFieldMapTableSelection('studyFieldMapTree');

	}
	styleDynamicTree('studyFieldMapTree');
}

function getPrefixName(cat, id) {
	if (parseInt(id) > 0) {
		return cat + id;
	} else {
		return cat + 'n' + (parseInt(id) * -1);
	}
}

function triggerExpanderClick(row) {
	if (row.treegrid('isExpanded')) {
		row.treegrid('collapse');
	} else {
		row.treegrid('expand');
	}
}

function createHeader(hasFieldMap) {
	var newRow = '<thead><tr>';

	if (!hasFieldMap) {
		newRow = newRow + '<th style="width:35%">' + studyName + '</th>' +
			'<th style="width:15%">' + locationLabel + '</th>' +
			'<th style="width:15%">' + entryLabel + '</th>' +
			'<th style="width:10%">' + repLabel + '</th>' +
			'<th style="width:20%">' + plotLabel + '</th>';
		newRow = newRow + '<th style="width:15%">' + fieldmapLabel + '</th>';
	} else {
		newRow = newRow + '<th style="width:40%"></th>' +
			'<th style="width:15%">' + locationLabel + '</th>' +
			'<th style="width:20%">' + entryLabel + '</th>' +
			'<th style="width:20%">' + repLabel + '</th>' +
			'<th style="width:20%">' + plotLabel + '</th>';

	}
	newRow = newRow + '</tr></thead>';
	$('#studyFieldMapTree').append(newRow + '<tbody></tbody>');
}

function createRow(id, parentClass, value, realId, withFieldMap, hasOneInstance) {
	var genClassName = 'treegrid-',
		genParentClassName = '',
		newRow = '',
		newCell = '',
		hasFieldMap,
		disabledString,
		checkBox;
	var locationName = value.trialInstanceNo + "-" + value.locationName;

	if (parentClass !== '') {
		genParentClassName = 'treegrid-parent-' + parentClass;
	}

	if (id.indexOf('study') > -1 || id.indexOf('dataset') > -1) {
		// Study and dataset level
		newRow = '<tr id="' + realId + '" class="tr-expander ' + genClassName + id + ' ' + genParentClassName + '">';
		newCell = newCell + '<td>' + value + '</td><td></td><td></td><td></td><td></td>';

		if (!withFieldMap) {
			newCell = newCell + '<td></td>';
		}
	} else {
		// Trial instance level
		if (withFieldMap) {
			// For view fieldmap
			newRow = '<tr id="' + realId + '" class="data-row trialInstance ' + genClassName + id + ' ' + genParentClassName + '">';
			newCell = '<td>' + value.trialInstanceNo + '</td><td>' + locationName + '</td><td>' + value.entryCount + '</td>';
			newCell = newCell + '<td>' + value.repCount + '</td><td>' + value.plotCount + '</td>';
		} else {
			if (value.hasFieldMap) {
				instancesWithFieldmap.push(realId);
			}
			// For create new fieldmap
			hasFieldMap = value.hasFieldMap ? 'Yes' : 'No';
			var isDisabled = value.hasFieldMap && (value.hasGeoJSON || value.hasMeansData);
			disabledString = isDisabled ? 'disabled' : '';
			var checked = hasOneInstance && !isDisabled ? 'checked' : '';

			newRow = '<tr class="data-row trialInstance ' + genClassName + id + ' ' + genParentClassName + '">';
			checkBox = '<input ' + disabledString + ' class="checkInstance" type="checkbox" id="' + realId + '" ' + checked + ' /> &nbsp;&nbsp;';
			newCell = '<td>' + checkBox + '&nbsp;' + value.trialInstanceNo + '</td><td>' + locationName + '</td><td>' + value.entryCount + '</td>';
			newCell = newCell + '<td>' + value.repCount + '</td><td>' + value.plotCount + '</td>';
			newCell = newCell + '<td class="hasFieldMap">' + hasFieldMap + '</td>';
		}
	}
	$('#studyFieldMapTree').append(newRow + newCell + '</tr>');
}

function clearStudyTree() {
	$('#studyFieldMapTree').empty();
}

function showMessage(message) {
	createErrorNotification(errorMsgHeader, message);
}

function createLabelPrinting(tableName) {

	var count = 0,
		idVal = null,
		index,
		tempVal,
		labelPrintingHref,
		id,
		type;

	if ($('.import-study-data').data('data-import') === '1') {
		showErrorMessage('', needSaveImportDataError);
		return;
	}

	if ($('#createTrialMainForm #studyId').length === 1) {
		idVal = ($('#createTrialMainForm #studyId').val());
		count++;
	} else {
		idVal = getCurrentStudyIdInTab();
		count++;
	}

	if (count !== 1) {
		showMessage(createLabelErrorMsg);
		return;
	}

	if (idVal !== null) {
		window.location.href = '/ibpworkbench/controller/jhipster#label-printing' +
			'?cropName=' + $('#cropName').val() +
			'&programUUID=' + $('#currentProgramId').val() +
			'&datasetId=' + $('#plotDataset').val() +
			'&studyId=' + $('#studyId').val() +
			'&printingLabelType=ObservationDataset';

	} else {
		type = 'Trial';
		showMessage(createLabelErrorMsg);
	}
}

function showFieldMap(tableName) {
	'use strict';
	var count = 0,
		idVal = null;

	//edit study
	if ($('.review-landing-page').length !== 0) {
		//meaning we are in the landing page
		idVal = getCurrentStudyIdInTab();
	} else if ($('#studyId')) {
		idVal = $('#studyId').val();
	}

	if (idVal != null) {
		if (count > 1) {
			showMessage(fieldMapOneStudyErrorMsg);
		} else {
			$('#page-message').html('');
			showFieldMapPopUp(tableName, idVal);
		}
	} else {
		showMessage(fieldMapStudyRequired);
	}
}

// Show popup to select instances for field map creation
function showFieldMapPopUpCreate(ids) {

	var link = '/Fieldbook/Fieldmap/enterFieldDetails/createFieldmap/';
	$.ajax({
		url: link + encodeURIComponent(ids),
		type: 'GET',
		data: '',
		success: function (data) {
			selectTrialInstanceCreate();
		},
		error: function (jqXHR, textStatus, errorThrown) {
			console.log('The following error occured: ' + textStatus, errorThrown);
		}
	});
}

// Show popup to select field map to display
function showFieldMapPopUp(tableName, id) {
	link = '/Fieldbook/Fieldmap/enterFieldDetails/createFieldmap/';
	$.ajax({
		url: link + id,
		type: 'GET',
		data: '',
		success: function (data) {
			if (data.nav == '0') {
				selectTrialInstance(tableName);
			} else if (data.nav == '1') {
				showMessage(noFieldMapExists);
			}
		}
	});
}

function viewFieldMap() {
	if (isViewFieldmap) {
		showGeneratedFieldMap();
	} else {
		showCreateFieldMap();
	}
}

// Redirect to step 3
function showGeneratedFieldMap() {
	if ($('#studyFieldMapTree .field-map-highlight').attr('id')) {
		if ($('#studyFieldMapTree .field-map-highlight').size() == 1) {
			$('#selectTrialInstanceModal').modal('toggle');
			var id = $('#studyFieldMapTree .field-map-highlight').attr('id');
			if (!isFieldMapHasInvalidValues[id]) {
				var datasetId = $('#studyFieldMapTree .field-map-highlight').treegrid('getParentNode').attr('id');
				location.href = '/Fieldbook/Fieldmap/generateFieldmapView/viewFieldmap/trial/' + datasetId + '/' + id;
			} else {
				showErrorMessage('', invalidFieldMapCoordinates);
			}

		} else {
			showMessage(multipleSelectError);
		}
	} else {
		showMessage(noSelectedTrialInstance);
	}
}

function showCreateFieldMap() {
	var selectedWithFieldMap,
		id,
		dataset,
		studyId,
		hasFieldMap;

	if (!validateLocationMatch()) {
		showMessage(msgLocationNotMatched);
		return;
	}

	if ($('#studyFieldMapTree .checkInstance:checked').attr('id')) {
		selectedWithFieldMap = false;
		fieldmapIds = [];
		instanceIds = [];
		$('#studyFieldMapTree .checkInstance:checked').each(function () {
			id = this.id;
			if (id.indexOf('|') > -1) {
				datasetId = id.split('|')[0];
				id = id.split('|')[1];
				studyId = $(this).parent().parent().treegrid('getParentNode').attr('id');
			} else {
				datasetId = $(this).parent().parent().treegrid('getParentNode').attr('id');
				studyId = $(this).parent().parent().treegrid('getParentNode').treegrid('getParentNode').attr('id');
			}
			// Get value hasfieldmap column
			hasFieldMap = $(this).parent().next().next().next().next().next().html();
			// Build id list of selected trials instances
			fieldmapIds.push(studyId + '|' + datasetId + '|' + id);
			instanceIds.push(parseInt(id));
			if (hasFieldMap == 'Yes') {
				selectedWithFieldMap = true;
			}
		});

		// Confirm to delete existing fieldmap
		if (selectedWithFieldMap) {
			openDeleteFieldmapConfirmation(instanceIds, datasetId);
		} else {
			redirectToFirstPage();
		}

	} else {
		// No study instance is selected
		showMessage(noSelectedTrialInstance);
	}
}

function openDeleteFieldmapConfirmation(instanceIds, datasetId) {
	'use strict';

	$('#deleteFieldmapModal').modal({backdrop: 'static', keyboard: true});
	$('#delete-fieldmap-confirmation').html(confirmDeleteFieldmap);
	$('#selectedInstanceIds').val(JSON.stringify(instanceIds));
	$('#datasetId').val(datasetId);
}

function deleteFieldmap() {
	'use strict';
	var instanceIds = JSON.parse($('#selectedInstanceIds').val());
	var datasetId = $('#datasetId').val();
	var allExistingFieldmapSelected = instancesWithFieldmap.every(val => instanceIds.includes(val))? true : false;
	var xAuthToken = JSON.parse(localStorage["bms.xAuthToken"]).token;
	var params = {
		allExistingFieldmapSelected: allExistingFieldmapSelected,
		instanceIds: instanceIds,
		datasetId: datasetId
	}
	$.ajax({
		url: '/Fieldbook/Fieldmap/enterFieldDetails/deletion',
		data: JSON.stringify(params),
		contentType: 'application/json',
		type: 'POST',
		beforeSend: function(xhr) {
			xhr.setRequestHeader('X-Auth-Token', xAuthToken);
		},
		success: function (data) {
			redirectToFirstPage();
		},
		error: function (jqxhr, textStatus, error) {
			if (jqxhr.status == 401) {
				bmsAuth.handleReAuthentication();
			}
			showErrorMessage('', hasFieldmapError);

		}
	});
}

function redirectToFirstPage() {
	var mode = ($('.review-trial-page-identifier').length) ? '.active .review-trial-page-identifier' : '#createTrialMainForm';
	var studyId = $(mode + ' #studyId').val();
	location.href = $('#fieldmap-url').attr('href') + '/' + studyId + '/' + encodeURIComponent(fieldmapIds.join(','));
}

function setSelectedTrialsAsDraggable() {
	$('#selectedTrials').tableDnD();

	$('#selectedTrials').tableDnD({
		onDragClass: 'myDragClass',
		onDrop: function (table, row) {
			setSelectTrialOrderValues();
		}
	});

	setSelectTrialOrderValues();
	styleDynamicTree('selectedTrials');
}

function setSelectTrialOrderValues() {
	var i = 0;
	$('#selectedTrials .orderNo').each(function () {
		$(this).text(i + 1);
		$(this).parent().parent().attr('id', i + 1);
		i++;
	});
	styleDynamicTree('selectedTrials');
}

function styleDynamicTree(treeName) {
	var count = 0;

	if ($('#' + treeName) != null) {
		$('#' + treeName + ' tr').each(function () {
			count++;
			var className = '';
			if (count % 2 == 1) {
				className = 'odd';
			} else {
				className = 'even';
			}
			$(this).find('td').removeClass('odd');
			$(this).find('td').removeClass('even');
			$(this).find('td').addClass(className);

			$(this).find('th').removeClass('odd');
			$(this).find('th').removeClass('even');
			$(this).find('th').addClass('table-header');
		});
	}
}

function openStudy(tableName) {
	'use strict';
	var count = 0,
		idVal = getCurrentStudyIdInTab();
	count++;

	if (count !== 1) {
		showMessage(openStudyError);
		return;
	}

	var openStudyHref = $('#open-study-url').attr('href');

	if (idVal != null) {
		location.href = openStudyHref + '/' + idVal;
	}
}

function openDeleteConfirmation() {
	'use strict';
	var deleteConfirmationText;

	$('#delete-study-heading-modal').text(deleteStudyTitle);
	deleteConfirmationText = deleteStudyConfirmation;

	$('#deleteStudyModal').modal({backdrop: 'static', keyboard: true});
	var active = ($('.review-trial-page-identifier').length) ? '.active' : '';
	var name = $(active + ' #studyName').val();
	$('#delete-study-confirmation').html(deleteConfirmationText + ' ' + name + '?');
}

function deleteStudy() {
	'use strict';
	if ($('.review-trial-page-identifier').length) {
		deleteStudyInReview();
	} else if ($('.edit-trial-page-identifier').length) {
		deleteStudyInEdit();
	}
}

function deleteStudyInReview() {
	'use strict';

	var idVal = getCurrentStudyIdInTab();
	$('#deleteStudyModal').modal('hide');

	doDeleteStudy(idVal, function (data) {
		setTimeout(function () {
			//simulate close tab
			$('#' + idVal).trigger('click');
			//remove it from the tree
			if ($('#studyTree').dynatree('getTree').getNodeByKey(idVal)) {
				$('#studyTree').dynatree('getTree').getNodeByKey(idVal).remove();
			}
			showSuccessfulMessage('', deleteStudySuccessful);
		}, 500);
	});
}

function deleteStudyInEdit() {
	'use strict';
	var idVal = $('#studyId').val();
	$('#deleteStudyModal').modal('hide');
	doDeleteStudy(idVal, function (data) {
		showSuccessfulMessage('', deleteStudySuccessful);
		setTimeout(function () {
			//go back to review study page
			location.href = $('#delete-success-return-url').attr('href');
		}, 500);
	});
}

/* CREATE SUB OBSERVATION UNIT SPECIFIC FUNCTIONS */

function subObservationUnitDatasetSelector() {
	'use strict';
	var $scope = angular.element('#SubObservationUnitDatasetSelectorModal').scope();
	$scope.$apply(function () {
		$scope.init();
	});

}

/* END SUB OBSERVATION UNIT SPECIFIC FUNCTIONS */

function openSampleSummary(obsUnitId, plotNumber, programUUID) {
	'use strict';
	BMS.Fieldbook.SamplesSummaryDataTable('#samples-summary-table', obsUnitId, plotNumber, programUUID);
	$('#samplesSummaryModal').modal({backdrop: 'static', keyboard: true});
	$('#samples-summary-table').wrap('<div style="overflow-x: auto" />');
}

function showInvalidInputMessage(message) {
	'use strict';
	createErrorNotification(invalidInputMsgHeader, message);
}

function showErrorMessage(messageDivId, message) {
	'use strict';
	createErrorNotification(errorMsgHeader, message);
}

function showSuccessfulMessage(messageDivId, message) {
	'use strict';
	createSuccessNotification(successMsgHeader, message);
}

function showAlertMessage(messageDivId, message, duration) {
	'use strict';
	createWarningNotification(warningMsgHeader, message, duration);
}

function getExportCheckedAdvancedList() {
	'use strict';
	var advancedLists = [];
	$('.export-advance-germplasm-lists-checkbox').each(function () {
		if ($(this).is(':checked')) {
			advancedLists.push($(this).data('advance-list-id'));
		}
	});
	return advancedLists;
}

function getExportCheckedInstances() {
	'use strict';
	var checkedInstances = [];
	$('.trial-instance-export').each(function () {
		if ($(this).is(':checked')) {
			checkedInstances.push($(this).data('geolocation-id'));
		}
	});
	return checkedInstances;
}

function doFinalExport(exportParameters) {
	var xAuthToken = JSON.parse(localStorage['bms.xAuthToken']).token;
	var xhr = new XMLHttpRequest();
	var node = $('#studyTree').dynatree('getTree').getActiveNode();
	xhr.open('GET', '/bmsapi/crops/' + exportParameters.cropName + '/programs/' + node.data.programUUID + '/studies/' + exportParameters.studyId + '/datasets/' + exportParameters.plotData + '/' + exportParameters.fileFormat + '?instanceIds=' + exportParameters.instanceIds + '&collectionOrderId=' + exportParameters.collectionOrderId + '&singleFile=' + exportParameters.singleFile, true);
	xhr.responseType = 'blob';
	xhr.setRequestHeader('Content-Type', 'application/json');
	xhr.setRequestHeader('X-Auth-Token', xAuthToken);
	xhr.onload = function (e) {
		if (this.status == 200) {
			const contentDisposition = this.getResponseHeader('content-disposition') || '';
			const matches = /filename=([^;]+)/ig.exec(contentDisposition);
			const fileName = (matches[1] || 'untitled').trim();
			const blob = this.response;
			const url = window.URL.createObjectURL(blob);

			// For IE 10 or later
			if (window.navigator && window.navigator.msSaveOrOpenBlob) {
				window.navigator.msSaveOrOpenBlob(url, fileName);
			} else { // For Chrome/Safari/Firefox and other browsers with HTML5 support
				var link = document.createElement('a');
				link.href = url;
				link.download = fileName;
				document.body.appendChild(link);
				link.click();
				document.body.removeChild(link);
			}
			$('#exportStudyModal').modal('hide');
		} else {
			showErrorMessage('', 'Something went wrong exporting the file.');
		}
	};
	xhr.send();
}

function hasRequiredColumnsHiddenInMeasurementDataTable(visibleColumns) {
	'use strict';
	var requiredColumns = [plotNoTermId, entryNoTermId, desigTermId];
	var i = 0;
	var noOfRequiredColumns = 0;
	for (i = 0; i < requiredColumns.length; i++) {
		if (visibleColumns.indexOf(requiredColumns[i]) >= 0) {
			noOfRequiredColumns++;
		}
	}
	return !(noOfRequiredColumns == requiredColumns.length);
}

function showWarningMessageForRequiredColumns(visibleColumns) {
	'use strict';
	var warningMessage = 'The export file will leave out contain columns that you have marked ' +
		'as hidden in the table view, with the exception of key columns that are ' +
		'necessary to identify your data when you import it back into the system.';
	if (hasRequiredColumnsHiddenInMeasurementDataTable(visibleColumns)) {
		showAlertMessage('', warningMessage);
	}
}

function getMeasurementTableVisibleColumns(addObsUnitId) {
	'use strict';
	var visibleColumns = '';
	if ($('[ui-view="editMeasurements"]').text().length === 0) {
		return visibleColumns;
	}
	var headers = $('#measurement-table_wrapper .dataTables_scrollHead [data-term-id]');
	var headerCount = headers.size();
	var i = 0;
	var obsUnitIdFound = false;
	for (i = 0; i < headerCount; i++) {
		var headerId = $('#measurement-table_wrapper .dataTables_scrollHead [data-term-id]:eq(' + i + ')').attr('data-term-id');
		if ($.isNumeric(headerId)) {
			if (headerId == '8201') {
				obsUnitIdFound = true;
			}
			if (visibleColumns.length == 0) {
				visibleColumns = headerId;
			} else {
				visibleColumns = visibleColumns + ',' + headerId;
			}
		}
	}
	if (addObsUnitId && !obsUnitIdFound) {
		visibleColumns = visibleColumns + ',' + '8201';
	}
	return visibleColumns;
}

function isFloat(value) {
	'use strict';
	return !isNaN(parseInt(value, 10)) && (parseFloat(value, 10) == parseInt(value, 10));
}

function moveToTopScreen() {

}

function doTreeHighlight(treeName, nodeKey) {

	var count = 0,
		key = '',
		elem;

	$('#' + treeName).dynatree('getTree').activateKey(nodeKey);
	$('#' + treeName).find('*').removeClass('highlight');

	// Then we highlight the nodeKey and its parents
	elem = nodeKey.split('_');
	for (count = 0; count < elem.length; count++) {
		if (key != '') {
			key = key + '_';
		}
		key = key + elem[count];
		$('.' + key).addClass('highlight');
	}
}


function displaySampleList(id, listName, isPageLoading) {
	'use script';

	var url = '/Fieldbook/sample/list/sampleList/' + id;

	$.ajax({
		url: url,
		type: 'GET',
		cache: false,
		success: function (html) {
			var element = angular.element(document.getElementById("mainApp")).scope();
			// To apply scope safely
			element.safeApply(function () {
				element.addSampleTabData(id, html, listName, isPageLoading);
			});
		}
	});
}

function validateBreedingMethodValues(id) {
	var valid = true;
	var trialInstances = $('#selectedTrialInstances').val();
	$.ajax({
		url: '/Fieldbook/StudyManager/advance/study/checkForNonMaintenanceAndDerivativeMethods/' + id
			+ '?trialInstances=' + encodeURIComponent(trialInstances),
		type: 'GET',
		cache: false,
		async: false,
		success: function (data) {
			if (data.errors) {
				showErrorMessage('page-advance-modal-message', data.errors);
				valid = false;
			}

		},
		error: function (jqXHR, textStatus, errorThrown) {
			console.log('The following error occured: ' + textStatus, errorThrown);
		}
	});
	return valid;
}

function showBaselineTraitDetailsModal(id) {
	'use strict';

	if (id !== '') {

		const crop = $('#cropName').val() ? $('#cropName').val() : cropName;
		const programUUID = $('#currentProgramId').val() ? $('#currentProgramId').val() : currentProgramId;
		const url = '/ibpworkbench/controller/jhipster#/variable-details?restartApplication' +
			'&cropName=' + crop +
			'&programUUID=' + programUUID +
			'&variableId=' + id + '&modal';


		$('.variable-details-section').html('');
		$('<iframe>', {
			src: url,
			id:  'myFrame',
			frameborder: 0,
			width: '100%',
			height: 520
		}).appendTo('.variable-details-section');
		$('#variableDetailsModal').modal('toggle');

		window.closeModal = function() {
			$('#variableDetailsModal').modal('hide');
		}
	}
}

function createFolder() {
	'use strict';

	var folderName = $.trim($('#addFolderName', '#studyTreeModal').val()),
		parentFolderId;

	if (folderName === '') {
		showErrorMessage('page-add-study-folder-message-modal', folderNameRequiredMessage);
		return false;
	} else if (!isValidInput(folderName)) {
		showErrorMessage('page-add-study-folder-message-modal', invalidFolderNameCharacterMessage);
		return false;
	} else {
		var activeStudyNode = $('#studyTree').dynatree('getTree').getActiveNode();

		if (activeStudyNode == null || activeStudyNode.data.isFolder === false) {
			showErrorMessage('', studyProgramFolderRequired);
			return false;
		}

		parentFolderId = activeStudyNode.data.key;
		if (parentFolderId === 'LOCAL') {
			parentFolderId = 1;
		}

		$.ajax({
			url: '/Fieldbook/StudyTreeManager/addStudyFolder',
			type: 'POST',
			data: 'parentFolderId=' + parentFolderId + '&folderName=' + folderName,
			cache: false,
			success: function (data) {
				var node;

				if (data.isSuccess == 1) {
					node = $('#studyTree').dynatree('getTree').getActiveNode();
					doStudyLazyLoad(node, data.newFolderId);
					node.focus();
					node.expand();
					hideAddFolderSection();
					showSuccessfulMessage('', addFolderSuccessful);
				} else {
					showErrorMessage('page-add-study-folder-message-modal', data.message);
				}
			}
		});
	}
	return false;
}

function deleteFolder(object) {
	'use strict';
	if (!$(object).hasClass('disable-image')) {
		var node = $('#studyTree').dynatree('getTree').getActiveNode();
		var currentFolderName = node.data.title,
			isFolder = node.data.isFolder,
			deleteConfirmationText,
			folderId = node.data.key,
			folderName = JSON.stringify({'folderName': currentFolderName});

		if (isFolder) {
			$.ajax({
				url: '/Fieldbook/StudyTreeManager/isFolderEmpty/' + folderId,
				headers: {
					'Accept': 'application/json',
					'Content-Type': 'application/json'
				},
				type: 'POST',
				data: folderName,
				cache: false,
				success: function (data) {
					var node;
					if (data.isSuccess === '1') {
						$('#delete-heading-modal').text(deleteFolderTitle);
						deleteConfirmationText = deleteConfirmation;
						showDeleteStudyFolderDiv(deleteConfirmationText);
					} else {
						hideAddFolderDiv();
						hideRenameFolderDiv();
						$('#cant-delete-heading-modal').text(deleteFolderTitle);
						$('#cant-delete-message').html(data.message);
						$('#cantDeleteFolder').modal('show');
					}
				}
			});
		} else {
			$('#delete-heading-modal').text(deleteStudyTitle);
			deleteConfirmationText = deleteStudyConfirmation;
			showDeleteStudyFolderDiv(deleteConfirmationText);
		}
	}
}

function showDeleteStudyFolderDiv(deleteConfirmationText) {
	hideAddFolderDiv();
	hideRenameFolderDiv();
	var currentFolderName = $('#studyTree').dynatree('getTree').getActiveNode().data.title;
	$('#delete-confirmation').html(deleteConfirmationText + ' ' + currentFolderName + '?');
	$('#deleteStudyFolder').modal('show');
	$('#page-delete-study-folder-message-modal').html('');
}

function submitDeleteFolder() {
	'use strict';

	var folderId = $('#studyTree').dynatree('getTree').getActiveNode().data.key;
	var isFolder = $('#studyTree').dynatree('getTree').getActiveNode().data.isFolder;

	if (isFolder) {
		$.ajax({
			url: '/Fieldbook/StudyTreeManager/deleteStudyFolder',
			type: 'POST',
			data: 'folderId=' + folderId,
			cache: false,
			success: function (data) {
				var node;
				if (data.isSuccess === '1') {
					$('#deleteStudyFolder').modal('hide');
					node = $('#studyTree').dynatree('getTree').getActiveNode();
					if (node != null) {
						node.remove();
					}
					changeBrowseStudyButtonBehavior(false);
					showSuccessfulMessage('', deleteFolderSuccessful);
				} else {
					showErrorMessage('page-delete-study-folder-message-modal', data.message);
				}
			}
		});
	} else {
		$('#deleteStudyFolder').modal('hide');
		doDeleteStudy(folderId, function (data) {
			var node;
			node = $('#studyTree').dynatree('getTree').getActiveNode();
			if (node != null) {
				node.remove();
			}
			changeBrowseStudyButtonBehavior(false);
			showSuccessfulMessage('', deleteStudySuccessful);
		});
	}
}

function moveStudy(sourceNode, targetNode) {
	'use strict';
	var sourceId = sourceNode.data.key,
		targetId = targetNode.data.key,
		title;

	if (targetId === 'LOCAL') {
		targetId = 1;
	}

	$.ajax({
		url: '/Fieldbook/StudyTreeManager/moveStudyFolder',
		type: 'POST',
		data: 'sourceId=' + sourceId + '&targetId=' + targetId,
		cache: false,
		success: function (data) {
			if (data.isSuccess === '1') {
				var node = targetNode;
				sourceNode.remove();
				doStudyLazyLoad(node);
				node.focus();
			} else {
				showErrorMessage('page-rename-message-modal', data.message);
			}
		}
	});
}

function moveGermplasm(sourceNode, targetNode) {
	'use strict';
	var sourceId = sourceNode.data.key,
		targetId = targetNode.data.key;


	var xAuthToken = JSON.parse(localStorage["bms.xAuthToken"]).token;
	$.ajax({
		url: '/bmsapi/crops/' + cropName + '/germplasm-list-folders/' + sourceId + '/move?newParentId=' + targetId + '&programUUID=' + currentProgramId,
		type: 'PUT',
		beforeSend: function (xhr) {
			xhr.setRequestHeader('X-Auth-Token', xAuthToken);
		},
		success: function () {
			var node = targetNode;
			sourceNode.remove();
			doGermplasmLazyLoad(node);
			node.focus();
		}
	});
}

function moveSamplesListFolder(sourceNode, targetNode) {
	'use strict';
	var sourceId = sourceNode.data.key,
		targetId = targetNode.data.key;
	var isCropList = false;

	if (targetId === 'CROPLISTS') {
		isCropList = true;
	}

	if (targetId === 'LISTS' || targetId === 'CROPLISTS') {
		targetId = 0;
	}

	var xAuthToken = JSON.parse(localStorage["bms.xAuthToken"]).token;

	return $.ajax({
		url: '/bmsapi/crops/' + cropName + '/programs/' + currentProgramId + '/sample-list-folders/' + sourceId + '/move?newParentId=' + targetId
			+ '&isCropList=' + isCropList,
		type: 'PUT',
		beforeSend: function (xhr) {
			xhr.setRequestHeader('X-Auth-Token', xAuthToken);
		},
		error: function (data) {
			if (data.status == 401) {
				bmsAuth.handleReAuthentication();
			} else {
				showErrorMessage('page-rename-message-modal', data.responseJSON.errors[0].message);
			}
		}
	});
}

function closeModal(modalId) {
	'use strict';
	$('#' + modalId).modal('hide');
}

function openGermplasmDetailsFromImportOwnDesign(gid) {
	$('#reviewDesignModal').modal('hide');
	openGermplasmDetailsPopup(gid, function () {
		$('#reviewDesignModal').modal('show');
	});
}

function openGermplasmDetailsPopup(gid, callback) {
	const germplasmDetailsModalService = angular.element('#mainApp').injector().get('germplasmDetailsModalService');
	germplasmDetailsModalService.openGermplasmDetailsModal(gid, callback);
}

function showListTreeToolTip(node, nodeSpan) {
	'use strict';
	$.ajax({
		url: '/Fieldbook/ListTreeManager/germplasm/list/header/details/' + node.data.key,
		type: 'GET',
		cache: false,
		success: function (data) {
			var listDetails = $('.list-details').clone(),
				notes;

			$(listDetails).find('#list-name').html(data.name);
			$(listDetails).find('#list-description').html(data.description);
			$(listDetails).find('#list-status').html(data.status);
			$(listDetails).find('#list-date').html(data.date);
			$(listDetails).find('#list-owner').html(data.owner);
			$(listDetails).find('#list-type').html(data.type);
			$(listDetails).find('#list-total-entries').html(data.totalEntries);
			notes = data.notes == null ? '-' : data.notes;
			$(listDetails).find('#list-notes').html(notes);

			$(nodeSpan).find('a.dynatree-title').popover({
				html: true,
				title: 'List Details',
				content: $(listDetails).html(),
				trigger: 'manual',
				placement: 'right',
				container: '.modal-popover'
			}).hover(function () {
				$('.popover').hide();
				$(this).popover('show');
			}, function () {
				$(this).popover('hide');
			});
			$('.popover').hide();
			$(nodeSpan).find('a.dynatree-title').popover('show');
		}
	});
}

function truncateStudyVariableNames(domSelector, charLimit) {
	'use strict';
	$(domSelector).each(function () {
		var htmlString = $(this).html();
		if ($(this).data('truncate-limit') !== undefined) {
			charLimit = parseInt($(this).data('truncate-limit'), 10);
		}

		if (htmlString.length > charLimit) {
			if (!$(this).hasClass('variable-tooltip')) {
				$(this).addClass('variable-tooltip');
				$(this).attr('title', htmlString);

				if ($(this).data('truncate-placement') !== undefined) {
					$(this).data('placement', $(this).data('truncate-placement'));
				}

				htmlString = htmlString.substring(0, charLimit) + '...';

			}
			$(this).html(htmlString);
		}

	});
	$('.variable-tooltip').each(function () {
		$(this).data('toggle', 'tooltip');
		if ($(this).data('placement') === undefined) {
			$(this).data('placement', 'right');
		}
		$(this).data('container', 'body');
		$(this).tooltip();
	});
}

function isValidInput(input) {
	'use strict';
	var invalidInput = /[<>&=%;?]/.test(input);
	return !invalidInput;
}

function doDeleteStudy(id, callback) {
	'use strict';
	var xAuthToken = JSON.parse(localStorage["bms.xAuthToken"]).token;
	$.ajax({
		url: '/bmsapi/crops/' + cropName + '/programs/' + currentProgramId + '/studies/' + id,
		type: 'DELETE',
		beforeSend: function(xhr) {
			xhr.setRequestHeader('X-Auth-Token', xAuthToken);
		},
		success: function (data) {
			callback();
		},
		error: function (jqxhr, textStatus, error) {
			if (jqxhr.status == 401) {
				bmsAuth.handleReAuthentication();
			}
			showErrorMessage('', jqxhr.responseJSON.errors[0].message);

		}
	});
}

function changeBrowseStudyButtonBehavior(isEnable) {
	'use strict';
	if (isEnable) {
		$('.browse-study-action').removeClass('disable-image');
	} else {
		$('.browse-study-action').addClass('disable-image');
	}
}

function changeBrowseGermplasmButtonBehavior(isEnable) {
	'use strict';
	if (isEnable) {
		$('.browse-germplasm-action').removeClass('disable-image');
	} else {
		$('.browse-germplasm-action').addClass('disable-image');
	}
}

function changeBrowseSampleButtonBehavior(isEnable) {
	'use strict';
	if (isEnable) {
		$('.browse-sample-action').removeClass('disable-image');
	} else {
		$('.browse-sample-action').addClass('disable-image');
	}
}


function openStudyTree(type, selectStudyFunction, isPreSelect) {
	'use strict';
	if (isPreSelect) {
		$('body').data('doAutoSave', '1');
	} else {
		$('body').data('doAutoSave', '0');
	}
	$('#page-study-tree-message-modal').html('');
	$('#addFolderDiv').hide();
	$('#renameFolderDiv').hide();

	if ($('#create-study #studyTree').length !== 0) {
		$('#studyTree').dynatree('destroy');
		displayStudyListTree('studyTree', type, selectStudyFunction, isPreSelect);
		changeBrowseStudyButtonBehavior(false);
		// Reset study filter to show all studies
	} else {
		$('#studyTypeFilter').val("All");
		filterByStudyType();
	}

	$('#studyTreeModal').modal({
		backdrop: 'static',
		keyboard: true
	});
	$('#studyTreeModal').off('hide.bs.modal');
	$('#studyTreeModal').on('hide.bs.modal', function () {
		TreePersist.saveStudyTreeState(false, '#studyTree');
	});
	choosingType = type;

	$('.fbk-study-tree-title.trial').removeClass('fbk-hide');
	TreePersist.preLoadStudyTreeState('#studyTree');
}

function makeGermplasmListDraggable(isDraggable) {
	'use strict';
	// isDraggable is always false, analyze to refactor or remove this
	isDraggable = isDraggable
		&& (($('#chooseGermplasmAndChecks').data('replace') && parseInt($('#chooseGermplasmAndChecks').data('replace')) === 1
			|| ($('#studyId').length === 0 && false))
			|| $('#studyId').length > 0 && false && !hasGeneratedDesign());
	if (isDraggable) {
		$('.germplasm-list-items tbody  tr').draggable({

			helper: function (/*event, ui*/) {
				var width = $(this)[0].offsetWidth,
					selected = $('.germplasm-list-items tr.germplasmSelectedRow'),
					container;

				if (selected.length === 0) {
					selected = $(this).addClass('germplasmSelectedRow');
				}

				container = $('<table style="width:' + width + 'px; background-color:green;" />').attr('id', 'draggingContainer');
				container.append(selected.clone().removeClass('germplasmSelectedRow'));

				return container;
			},

			revert: 'invalid',

			start: function (/*event, ui*/) {
				var selected = $('.germplasm-list-items tr.germplasmSelectedRow');
			},

			stop: function (/*event, ui*/) {
				var selected = $('.germplasm-list-items tr.germplasmSelectedRow');
				$(selected).css('opacity', '1');
			},

			zIndex: 9999,

			appendTo: '#chooseGermplasmAndChecks'
		});

		$('.germplasm-list-items tbody tr').off('click').on('click', function () {
			$(this).toggleClass('germplasmSelectedRow');
		});

	} else {
		if ($('.germplasm-list-items .ui-draggable').length !== 0) {
			$('.germplasm-list-items tbody  tr').draggable('destroy');
		}
		$('.germplasm-list-items tbody tr').off('click');
	}

	SaveAdvanceList.setSelectedEntries();
	// Change background of selected rows
	$('.germplasm-list-items tr.germplasmSelectedRow').removeClass('germplasmSelectedRow');
}

function isOpenStudy() {
	'use strict';
	var trialStatus = $('body').data('trialStatus');
	return (trialStatus && trialStatus === 'OPEN');
}

function initializeStudyTabs() {
	'use strict';
	$('.nav-tabs').tabdrop({position: 'left'});
	$('.nav-tabs').tabdrop('layout');
	$('#study-tab-headers .fbk-close-tab').on('click', function () {
		var studyId = $(this).attr('id');
		$('li#li-study' + studyId).remove();
		$('.info#study' + studyId).remove();
		if ($('#study-tab-headers li').length > 1) {
			var studyIdString = $('#study-tab-headers li:eq(0)').attr('id');
			$('li#' + studyIdString + ' a').tab('show');
		}
		determineIfShowCloseAllStudyTabs();
		$('.nav-tabs').tabdrop('layout');
	});
	determineIfShowCloseAllStudyTabs();
}

function determineIfShowCloseAllStudyTabs() {
	'use strict';
	if ($('#study-tab-headers li').length > 1) {
		$('.review-study-details').removeClass('fbk-hide');
	} else {
		$('.review-study-details').addClass('fbk-hide');
	}
}

function closeAllStudyTabs() {
	'use strict';
	$('#study-tab-headers').html('');
	$('#study-tabs').html('');
	determineIfShowCloseAllStudyTabs();
}

function loadDatasetDropdown(optionTag) {
	'use strict';
	if ($('#study' + getCurrentStudyIdInTab() + ' #dataset-selection option').length > 1) {
		return;
	}
	$.ajax({
		url: '/Fieldbook/StudyManager/reviewStudyDetails/datasets/'
			+ getCurrentStudyIdInTab(),
		type: 'GET',
		cache: false,
		success: function (data) {
			var i = 0;
			for (i = 0; i < data.length; i++) {
				optionTag.append(new Option(data[i].name, data[i].id));
			}
			$('#study' + getCurrentStudyIdInTab() + ' #dataset-selection').val('');
		},
		error: function (jqXHR, textStatus, errorThrown) {
			console.log('The following error occured: ' + textStatus,
				errorThrown);
		}
	});
}

function getCurrentStudyIdInTab() {
	'use strict';
	if ($('#study-tab-headers .tabdrop').hasClass('active')) {
		//means the active is in the tab drop
		return $('#study-tab-headers .tabdrop li.active .fbk-close-tab').attr('id');
	} else {
		return $('#study-tab-headers li.active .fbk-close-tab').attr('id');
	}
}

function loadDatasetMeasurementRowsViewOnly(datasetId, datasetName) {
	'use strict';
	var currentStudyId = getCurrentStudyIdInTab();
	if (datasetId == 'Please Choose' || $('#' + getJquerySafeId('dset-tab-') + datasetId).length !== 0) {
		return;
	}
	$.ajax({
		url: '/Fieldbook/StudyManager/reviewStudyDetails/measurements/viewStudyAjax/' + datasetId + '/' + currentStudyId,
		type: 'GET',
		cache: false,
		success: function (html) {
			var close = '<i class="glyphicon glyphicon-remove fbk-close-dataset-tab" id="' + datasetId + '"></i>';
			$('#study' + currentStudyId + ' #measurement-tab-headers').append(
				'<li class="active" id="dataset-li' + datasetId + '"><a><span class="review-dataset-name">'
				+ datasetName + '</span>' + close + '</a> ' + '</li>');
			$('#study' + currentStudyId + ' #measurement-tabs').append(
				'<div class="review-info" id="dset-tab-' + datasetId + '">' + html + '</div>');
			$('#study' + currentStudyId + ' .measurement-section').show();
			truncateStudyVariableNames('#dataset-li' + datasetId + ' .review-dataset-name', 40);
			initializeReviewDatasetTabs(datasetId);
		}
	});
}

function initializeReviewDatasetTabs(datasetId) {
	'use strict';
	$('#dataset-li' + datasetId).on('click', function () {
		$('#study' + getCurrentStudyIdInTab() + ' #dataset-selection option:selected').prop('selected', false);
		$('#study' + getCurrentStudyIdInTab() + ' #dataset-selection option').each(function (index) {
			if ($(this).val() === datasetId) {
				$(this).prop('selected', true);
			}
		});
		$('#study' + getCurrentStudyIdInTab() + ' #dataset-selection').change();
	});

	$('#dataset-li' + datasetId + ' .fbk-close-dataset-tab').on('click', function () {
		var datasetId = $(this).attr('id'),
			showFirst = false;
		if ($(this).parent().parent().hasClass('active')) {
			showFirst = true;
		}
		$('li#dataset-li' + datasetId).remove();
		$('#measurement-tabs #dset-tab-' + datasetId).remove();
		if (showFirst && $('#measurement-tab-headers li').length > 0) {
			var datasetIdString = $('#measurement-tab-headers li:eq(0) .fbk-close-dataset-tab').attr('id');
			$('li#dataset-li' + datasetIdString).addClass('active');
			$('#measurement-tabs #dset-tab-' + datasetIdString).show();
		}
	});
}

// Function to enable/disable & show/hide controls as per Clear list button's visibility
function toggleControlsForGermplasmListManagement(value) {
	'use strict';
	if (value) {
		$('#imported-germplasm-list-reset-button').show();
	} else {
		$('#imported-germplasm-list-reset-button').hide();
	}

	$('#txtStartingPlotNo').prop('readOnly', !value);
}

function showGermplasmDetailsSection() {
	'use strict';
	if ($('.germplasm-list-items tbody tr').length > 0) {
		toggleControlsForGermplasmListManagement(true);
	}
}

function hasGeneratedDesign() {
	'use strict';
	return angular.element('#mainApp').injector().get('studyStateService').hasGeneratedDesign();
}

function hasListOrSubObs() {
	'use strict';
	return angular.element('#mainApp').injector().get('studyStateService').hasListOrSubObs();
}

function countGermplasms() {
	var totalGermplasms = parseInt($('#totalGermplasms').val());
	return totalGermplasms ? totalGermplasms : 0;
}

function exportDesignTemplate() {
	$.ajax({
		url: '/Fieldbook/DesignTemplate/export',
		type: 'GET',
		cache: false,
		success: function (result) {
			if (result.isSuccess === 1) {
				$.fileDownload('/Fieldbook/crosses/download/file', {
					httpMethod: 'POST',
					data: result
				});
			} else {
				showErrorMessage('page-review-out-of-bounds-data-message-modal', result.errorMessage);
			}
		}
	});
}

function setSpinnerMaxValue() {
	'use strict';
	if ($('#' + getJquerySafeId('checkVariables0.value')).val() === null || $('#' + getJquerySafeId('checkVariables0.value')).val() === '') {
		$('#' + getJquerySafeId('checkVariables0.value')).val(1);
	}
}

function ValidateValueCheckBoxFavorite(checkFavorite, data) {

	if (checkFavorite === 'showFavoriteLocationInventory') {
		if (data.allSeedStorageFavoritesLocations.length !== 0) {
			$('#' + checkFavorite).prop('checked', true);
		}
	}

	if (checkFavorite === 'importFavoriteMethod') {
		if (data.favoriteNonGenerativeMethods.length !== 0) {
			$('#' + checkFavorite).prop('checked', true);
		}
	}

	if (checkFavorite === 'importFavoriteLocation') {
		if (data.allBreedingFavoritesLocations.length !== 0) {
			$('#' + checkFavorite).prop('checked', true);
		}
	}
}

/**
 * The following contructor contains utility functions for escaping html content from a string
 * Logic is extracted from lodash 4.11.1 source: https://github.com/lodash/lodash/blob/master/dist/lodash.core.js
 * @constructor
 */
function EscapeUtilityConstructor() {
	/** Used to match HTML entities and HTML characters. */
	this.unescapedHtmlRegEx = /[&<>"'`]/g;
	this.hasUnescapedHtmlRegEx = RegExp(this.unescapedHtmlRegEx.source);
}

/**
 * Converts `value` to a string. An empty string is returned for `null`
 * and `undefined` values. The sign of `-0` is preserved.
 *
 * @param {*} value The value to process.
 * @returns {string} Returns the string.
 * @example
 *
 * toString(null);
 * // => ''
 *
 * toString(-0);
 * // => '-0'
 *
 * toString([1, 2, 3]);
 * // => '1,2,3'
 */

EscapeUtilityConstructor.prototype.toString = function (value) {
	if (typeof value == 'string') {
		return value;
	}
	return value == null ? '' : (value + '');
};

/**
 * Used to convert characters to HTML entities.
 *
 * @private
 * @param {string} chr The matched character to escape.
 * @returns {string} Returns the escaped character.
 */
EscapeUtilityConstructor.prototype.escape = function (string) {
	var htmlEscapes = {
		'&': '&amp;',
		'<': '&lt;',
		'>': '&gt;',
		'"': '&quot;',
		"'": '&#39;',
		'`': '&#96;'
	};

	string = this.toString(string);

	return (string && this.hasUnescapedHtmlRegEx.test(string))
		? string.replace(this.unescapedHtmlRegEx, function (chr) {
			return htmlEscapes[chr];
		}) : string;
};

/* make a global instance of EscapeUtility usable to all Fieldbook modules */
var EscapeHTML = new EscapeUtilityConstructor();

function getDatasetInstances(cropName, currentProgramId, studyId, datasetId) {
	var BASE_URL = '/bmsapi/crops/' + cropName + '/programs/' + currentProgramId + '/studies/';
	// Validate if there is something to advance
	var xAuthToken = JSON.parse(localStorage["bms.xAuthToken"]).token;
	var dataSets;
	return $.ajax({
		url: BASE_URL + studyId + '/datasets/' + datasetId + '/instances',
		type: 'GET',
		cache: false,
		beforeSend: function (xhr) {
			xhr.setRequestHeader('X-Auth-Token', xAuthToken);
		},
		success: function (data) {
		}
	});
}

function validateLocationMatch() {
	var isMatched = true;
	var prev = '';
	$('#studyFieldMapTree tr:has(:checkbox:checked) td:nth-child(2)').each(function () {
		let temp = $(this).text().split('-');
		let txt = $(this).text();
		if (temp.length > 1) {
			txt = temp[1];
		}
		if (prev != '') {
			isMatched = prev == txt;
		}
		prev = txt;
	});
	return isMatched;
}

function showAdvanceStudyModal(trialInstances, noOfReplications, locationsSelected, advanceType, values) {
	'use strict';
	return angular.element('#mainApp').injector().get('advanceStudyModalService').openAdvanceStudyModal(trialInstances, noOfReplications, locationsSelected, advanceType, values);
}

function openFeedbackSurvey(feedbackEnabled, feature, feedbackService) {
	if (feedbackEnabled && feature) {
		feedbackService.shouldShowFeedback(feature).then((shouldShow) => {
			if (shouldShow) {
				const crop = $('#cropName').val() ? $('#cropName').val() : cropName;
				const programUUID = $('#currentProgramId').val() ? $('#currentProgramId').val() : currentProgramId;
				const url = '/ibpworkbench/controller/jhipster#/feedback-dialog?restartApplication' +
					'&cropName=' + crop +
					'&programUUID=' + programUUID +
					'&feature=' + feature + '&modal';

				$('.feedback-section').html('');
				$('<iframe>', {
					src: url,
					id: 'myFrame',
					frameborder: 0,
					width: '100%',
					height: 750
				}).appendTo('.feedback-section');
				$('#feedbackModal').modal('toggle');

				window.closeModal = function () {
					$('#feedbackModal').modal('hide');
				}
			}
		})
	}
}
