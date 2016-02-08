$(function() {
	'use strict';

	// attach spinner operations to ajax events
	$(document).ajaxStart(function() {
		SpinnerManager.addActive();
	}).ajaxStop(function() {
		SpinnerManager.resolveActive();
	}).ajaxError(function(xhr, error) {
        if(error.status == 500) {
            showErrorMessage('', ajaxGenericErrorMsg);
        } else {
            showErrorMessage('INVALID INPUT', error.responseText);
        }

        SpinnerManager.resolveActive();
	});

	if (typeof convertToSelect2 === 'undefined' || convertToSelect2) {
		// Variable is undefined
		$('select').each(function() {
			$(this).select2({minimumResultsForSearch: 20});
		});
	}

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
		outer.appendChild (inner);

		document.body.appendChild (outer);
		var w1 = inner.offsetWidth;
		outer.style.overflow = 'scroll';
		var w2 = inner.offsetWidth;
		if (w1 == w2) {
			w2 = outer.clientWidth;
		}

		document.body.removeChild (outer);

		return (w1 - w2);
	}

	$(document.body)
		.on('show.bs.modal', function() {
			if (this.clientHeight < window.innerHeight) {return;}

			var scrollbarWidth = measureScrollBar();
			if (scrollbarWidth) {
				$(document.body).css('padding-right', scrollbarWidth);
			}
		})
		.on('hidden.bs.modal', function() {
			$(document.body).css('padding-right', 0);
		});

	$('.page-header')
		.on('click','.fbk-help',function() {
		   var helpModule = $(this).data().helpLink;
			$.get('/ibpworkbench/controller/help/getUrl/' + helpModule).success(function(helpUrl) {
				if (!helpUrl || !helpUrl.length) {
					$.when(
						$.get('/ibpworkbench/controller/help/headerText'),
						$.get('/ibpworkbench/VAADIN/themes/gcp-default/layouts/help_not_installed.html')
					).done(function(headerText,helpHtml) {
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
		success: function(data) {
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

function doAjaxMainSubmit(pageMessageDivId, successMessage, overrideAction) {
	'use strict';

	var form = $('form'),
		action = form.attr('action'),
		serializedData = form.serialize();

	if (overrideAction) {
		action = overrideAction;
	}

	$.ajax({
		url: action,
		type: 'POST',
		data: serializedData,
		success: function(html) {
			// Paste the whole html
			$('.container .row').first().html(html);
			if (pageMessageDivId) {
				showSuccessfulMessage(pageMessageDivId, successMessage);
			}
		}
	});
}

function showPage(paginationUrl, pageNum, sectionDiv) {
	'use strict';

	$.ajax({
		url: paginationUrl + pageNum,
		type: 'GET',
		data: '',
		cache: false,
		success: function(html) {

			var tableId,
				gid,
				idVal,
				indexItems,
				rowIndex;

			$('#' + sectionDiv).html(html);

			if (sectionDiv === 'trial-details-list' || sectionDiv === 'nursery-details-list') {
				// We highlight the previously clicked
				for (tableId in selectedTableIds) {
					idVal = selectedTableIds[tableId];
					if (idVal != null) {
						// We need to highlight
						$('tr.data-row#' + idVal).addClass('field-map-highlight');
					}
				}
			} else if (sectionDiv === 'inventory-germplasm-list') {
				// We highlight the previously clicked
				for (gid in selectedGids) {
					idVal = selectedGids[gid];
					if (idVal !== null) {
						// We need to highlight
						$('tr.primaryRow[data-gid=' + idVal + ']').addClass('field-map-highlight');
					}
				}
			}

			if (sectionDiv === 'imported-germplasm-list') {
				makeDraggable(makeDraggableBool);

				// Highlight
				if (itemsIndexAdded && itemsIndexAdded.length > 0) {
					for (indexItems = 0; indexItems < itemsIndexAdded.length ; indexItems++) {
						if (itemsIndexAdded[indexItems] != null) {
							rowIndex = itemsIndexAdded[indexItems].index;
							if ($('.primaryRow[data-index=""+rowIndex+""]').length !== 0) {
								$('.primaryRow[data-index=""+rowIndex+""]').css('opacity', '0.5');
							}
						}
					}
				}
			}
		}
	});
}

function showMultiTabPage(paginationUrl, pageNum, sectionDiv, sectionContainerId, paginationListIdentifier) {
	'use strict';

	$.ajax({
		url: paginationUrl + pageNum + '?listIdentifier=' + paginationListIdentifier,
		type: 'GET',
		data: '',
		cache: false,
		success: function(html) {
			var paginationDiv = '#' + sectionContainerId + ' #' + sectionDiv;
			$(paginationDiv + ':eq(0)').html('');
			$(paginationDiv + ':eq(0)').html(html);
		}
	});
}

function showPostPage(paginationUrl, previewPageNum, pageNum, sectionDiv, formName) {
	'use strict';
	var $form,
		completeSectionDivName,
		serializedData;

	if (formName.indexOf('#') > -1) {
		$form = $(formName);
	} else {
		$form = $('#' + formName);
	}

	if (sectionDiv.indexOf('#') > -1) {
		completeSectionDivName = sectionDiv;
	} else {
		completeSectionDivName = '#' + sectionDiv;
	}

	serializedData = $form.serialize();

	$.ajax({
		url: paginationUrl + pageNum + '/' + previewPageNum + '?r=' + (Math.random() * 999),
		type: 'POST',
		data: serializedData,
		cache: false,
		timeout: 70000,
		success: function(html) {
			$(completeSectionDivName).empty().append(html);

			if (sectionDiv == 'trial-details-list' || sectionDiv == 'nursery-details-list') {
				// We highlight the previously clicked
				for (var index in selectedTableIds) {
					var idVal = selectedTableIds[index];
					if (idVal != null) {
						// We need to highlight
						$('tr.data-row#' + idVal).addClass('field-map-highlight');
					}
				}
			}

			if (sectionDiv == 'check-germplasm-list') {
				makeCheckDraggable(makeCheckDraggableBool);
			}

		}
	});
}

function triggerFieldMapTableSelection(tableName) {

	var id;

	$('#' + tableName + ' tr.data-row').on('click', function() {
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

function createFieldMap(tableName) {

	if ($('.import-study-data').data('data-import') === '1') {
		showErrorMessage('', needSaveImportDataError);
		return;
	}
	var id = '',
	name = '';
	if ($('#createNurseryMainForm #studyId').length  === 1) {
		id = $('#createNurseryMainForm #studyId').val();
		name = $('.fieldmap-study-name').html();
	}else if ($('#createTrialMainForm #studyId').length  === 1) {
		id = $('#createTrialMainForm #studyId').val();
		name = $('.fieldmap-study-name').html();
	} else {
		id = getCurrentStudyIdInTab();
		name = $('#div-study-tab-' + getCurrentStudyIdInTab() + ' .fieldmap-study-name').html();
	}
	openStudyFieldmapTree(id, name);
}

// FIXME obsolete
function checkTrialOptions(id) {
	$.ajax({
		url: '/Fieldbook/Fieldmap/enterFieldDetails/createFieldmap/' + id,
		type: 'GET',
		data: '',
		cache: false,
		success: function(data) {
			if (data.nav == '0') {
				$('#manageTrialConfirmation').modal('show');
			} else if (data.nav == '1') {
				var fieldMapHref = $('#fieldmap-url').attr('href');
				location.href = fieldMapHref + '/' + id;
			}
		}
	});
}

// FIXME obsolete
function createNurseryFieldmap(id) {
	$.ajax({
		url: '/Fieldbook/Fieldmap/enterFieldDetails/createNurseryFieldmap/' + id,
		type: 'GET',
		data: '',
		cache: false,
		success: function(data) {
			if (data.nav == '0') {
				$('#manageTrialConfirmation').modal('show');
				$('#fieldmapDatasetId').val(data.datasetId);
				$('#fieldmapGeolocationId').val(data.geolocationId);
			} else if (data.nav == '1') {
				var fieldMapHref = $('#fieldmap-url').attr('href');
				location.href = fieldMapHref + '/' + id;
			}
		}
	});
}

function proceedToCreateFieldMap() {
	$('#manageTrialConfirmation').modal('hide');
	var fieldMapHref = $('#fieldmap-url').attr('href');
	location.href = fieldMapHref + '/' + $('#fieldmapStudyId').val();
}

function proceedToGenerateFieldMap() {
	$('#manageTrialConfirmation').modal('hide');
	location.href = '/Fieldbook/Fieldmap/generateFieldmapView/viewFieldmap/nursery/' +
		$('#fieldmapDatasetId').val() + '/' + $('#fieldmapGeolocationId').val();
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
		return(false);
	}
	var isNumber = !isNaN(Number(val));
	if (isNumber) {
		if (val.indexOf('.') != -1) {
			return(true);
		} else {
			return isInt(val);
		}
	} else {
		return(false);
	}
}

function selectTrialInstance() {
	if (!isNursery()) {
		$.ajax({
			url: '/Fieldbook/Fieldmap/enterFieldDetails/selectTrialInstance',
			type: 'GET',
			cache: false,
			data: '',
			success: function(data) {
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
						var datasetId = data.datasetId;
						var geolocationId = data.geolocationId;
						location.href = '/Fieldbook/Fieldmap/generateFieldmapView/viewFieldmap/trial/' + datasetId + '/' + geolocationId;
					}
				}
			}
		});
	} else {
		// Redirect to step 3 for nursery
		var datasetId = $('#fieldmapDatasetId').val();
		var geolocationId = $('#fieldmapGeolocationId').val();
		location.href = '/Fieldbook/Fieldmap/generateFieldmapView/viewFieldmap/nursery/' + datasetId + '/' + geolocationId;
	}
}

function selectTrialInstanceCreate() {
	$.ajax({
		url: '/Fieldbook/Fieldmap/enterFieldDetails/selectTrialInstance',
		type: 'GET',
		async: false,
		cache: false,
		data: '',
		success: function(data) {
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
	createHeader(hasFieldMap);
	$.each(fieldMapInfoList, function(index, fieldMapInfo) {
		createRow(getPrefixName('study', fieldMapInfo.fieldbookId), '', fieldMapInfo.fieldbookName, fieldMapInfo.fieldbookId, hasFieldMap, hasOneInstance);
		$.each(fieldMapInfo.datasets, function(index, value) {
			hasOneInstance = fieldMapInfoList.length === 1 && fieldMapInfoList[0].datasets.length === 1 && fieldMapInfoList[0].datasets[0].trialInstances.length === 1;
			if (!isNursery()) {
				// Create trial study tree up to instance level
				createRow(getPrefixName('dataset', value.datasetId), getPrefixName('study', fieldMapInfo.fieldbookId), value.datasetName, value.datasetId, hasFieldMap, hasOneInstance);
				$.each(value.trialInstances, function(index, childValue) {
					if ((hasFieldMap && childValue.hasFieldMap) || !hasFieldMap) {
						createRow(getPrefixName('trialInstance', childValue.geolocationId), getPrefixName('dataset', value.datasetId), childValue, childValue.geolocationId, hasFieldMap, hasOneInstance);
					}
				});
			} else {
				// If dataset has an instance, show up to the dataset level
				if (value.trialInstances.length > 0) {
					$.each(value.trialInstances, function(index, childValue) {
						createRowForNursery(getPrefixName('trialInstance', childValue.geolocationId),
								getPrefixName('study', fieldMapInfo.fieldbookId), childValue, childValue.geolocationId,
								hasFieldMap, value.datasetName, value.datasetId, hasOneInstance);
					});
				}
			}
		});
	});

	// Set bootstrap ui
	$('.tree').treegrid();

	$('.tr-expander').on('click', function() {
		triggerExpanderClick($(this));
	});
	$('.treegrid-expander').on('click', function() {
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
		if (trial) {
			newRow = newRow + '<th style="width:45%">' + trialName + '</th>' +
				'<th style="width:10%">' + entryLabel + '</th>' +
				'<th style="width:10%">' + repLabel + '</th>' +
				'<th style="width:20%">' + plotLabel + '</th>';
		} else {
			newRow = newRow + '<th style="width:65%">' + nurseryName + '</th>' +
			'<th style="width:20%">' + entryPlotLabel + '</th>';
		}
		newRow = newRow + '<th style="width:15%">' + fieldmapLabel + '</th>';
	} else {
		if (trial) {
			newRow = newRow + '<th style="width:40%"></th>' +
				'<th style="width:20%">' + entryLabel + '</th>' +
				'<th style="width:20%">' + repLabel + '</th>' +
				'<th style="width:20%">' + plotLabel + '</th>';
		} else {
			newRow = newRow + '<th style="width:60%"></th>' +
			'<th style="width:40%">' + entryPlotLabel + '</th>';
		}
	}
	newRow = newRow + '</tr></thead>';
	$('#studyFieldMapTree').append(newRow + '<tbody></tbody>');
}

function createRowForNursery(id, parentClass, value, realId, withFieldMap, datasetName, datasetId, hasOneInstance) {
	var genClassName = 'treegrid-',
		genParentClassName = '',
		newRow = '',
		newCell = '',
		hasFieldMap,
		disabledString,
		checkBox;

	if (parentClass !== '') {
		genParentClassName = 'treegrid-parent-' + parentClass;
	}

	// For create new fieldmap
	hasFieldMap = value.hasFieldMap ? 'Yes' : 'No';
	disabledString = value.hasFieldMap ? 'disabled' : '';
	var checked = hasOneInstance ? 'checked' : '';

	newRow = '<tr class="data-row trialInstance ' + genClassName + id + ' ' + genParentClassName + '">';
	checkBox = '<input ' + disabledString + ' class="checkInstance" type="checkbox" id="' + datasetId + '|' + realId + '" ' + checked + ' /> &nbsp;&nbsp;';
	newCell = '<td>' + checkBox + '&nbsp;' + datasetName + '</td><td>' + value.plotCount + '</td>';
	newCell = newCell + '<td class="hasFieldMap">' + hasFieldMap + '</td>';
	$('#studyFieldMapTree').append(newRow + newCell + '</tr>');
}

function createRow(id, parentClass, value, realId, withFieldMap, hasOneInstance) {
	var genClassName = 'treegrid-',
		genParentClassName = '',
		newRow = '',
		newCell = '',
		hasFieldMap,
		disabledString,
		checkBox;

	if (parentClass !== '') {
		genParentClassName = 'treegrid-parent-' + parentClass;
	}

	if (id.indexOf('study') > -1 || id.indexOf('dataset') > -1) {
		// Study and dataset level
		newRow = '<tr id="' + realId + '" class="tr-expander ' + genClassName + id + ' ' + genParentClassName + '">';

		if (trial) {
			newCell = newCell + '<td>' + value + '</td><td></td><td></td><td></td>';
		} else {
			newCell = newCell + '<td>' + value + '</td><td></td>';
		}
		if (!withFieldMap) {
			newCell = newCell + '<td></td>';
		}
	} else {
		// Trial instance level
		if (withFieldMap) {
			// For view fieldmap
			newRow = '<tr id="' + realId + '" class="data-row trialInstance ' + genClassName + id + ' ' + genParentClassName + '">';
			newCell = '<td>' + value.trialInstanceNo + '</td><td>' + value.plotCount + '</td>';
			if (trial) {
				newCell = newCell + '<td>' + value.repCount + '</td><td>' + value.plotCount + '</td>';
			}
		} else {
			// For create new fieldmap
			hasFieldMap = value.hasFieldMap ? 'Yes' : 'No';
			disabledString = value.hasFieldMap ? 'disabled' : '';
			var checked = hasOneInstance ? 'checked' : '';

			newRow = '<tr class="data-row trialInstance ' + genClassName + id + ' ' + genParentClassName + '">';
			checkBox = '<input ' + disabledString + ' class="checkInstance" type="checkbox" id="' + realId + '" ' + checked + ' /> &nbsp;&nbsp;';
			newCell = '<td>' + checkBox + '&nbsp;' + value.trialInstanceNo + '</td><td>' + value.plotCount + '</td>';
			if (trial) {
				newCell = newCell + '<td>' + value.repCount + '</td><td>' + value.plotCount + '</td>';
			}
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

	if ($('#createNurseryMainForm #studyId').length === 1) {
		idVal = ($('#createNurseryMainForm #studyId').val());
		count++;
	}else if ($('#createTrialMainForm #studyId').length === 1) {
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
		labelPrintingHref = $('#label-printing-url').attr('href');
		id = idVal;
		location.href = labelPrintingHref + '/' + id;

	} else {
		type = 'Trial';
		if (tableName === 'nursery-table') {
			type = 'Nursery';
		}
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
			showMessage(isNursery() ? fieldMapOneStudyErrorMsg : fieldMapOneStudyErrorMsgTrial);
		} else {
			$('#page-message').html('');
			showFieldMapPopUp(tableName, idVal);
		}
	} else {
		showMessage(isNursery() ? fieldMapStudyRequired : fieldMapStudyRequiredTrial);
	}
}

// Show popup to select instances for field map creation
function showFieldMapPopUpCreate(ids) {
	var link = '';
	if (!isNursery()) {
		link = '/Fieldbook/Fieldmap/enterFieldDetails/createFieldmap/';
		trial = true;
	} else {
		link = '/Fieldbook/Fieldmap/enterFieldDetails/createNurseryFieldmap/';
		trial = false;
	}
	$.ajax({
		url: link + encodeURIComponent(ids),
		type: 'GET',
		data: '',
		success: function(data) {
			selectTrialInstanceCreate();
		},
		error: function(jqXHR, textStatus, errorThrown) {
			console.log('The following error occured: ' + textStatus, errorThrown);
		},
		complete: function() {
		}
	});
}

// Show popup to select field map to display
function showFieldMapPopUp(tableName, id) {
	var link = '';
	if (tableName == 'trial-table') {
		link = '/Fieldbook/Fieldmap/enterFieldDetails/createFieldmap/';
	} else {
		link = '/Fieldbook/Fieldmap/enterFieldDetails/createNurseryFieldmap/';
	}
	$.ajax({
		url: link + id,
		type: 'GET',
		data: '',
		success: function(data) {
			if (data.nav == '0') {
				if (tableName == 'nursery-table') {
					$('#fieldmapDatasetId').val(data.datasetId);
					$('#fieldmapGeolocationId').val(data.geolocationId);
				}
				selectTrialInstance(tableName);
			} else if (data.nav == '1') {
				showMessage(isNursery() ? noFieldMapExists : noFieldMapExistsTrial);
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
			var datasetId = $('#studyFieldMapTree .field-map-highlight').treegrid('getParentNode').attr('id');
			location.href = '/Fieldbook/Fieldmap/generateFieldmapView/viewFieldmap/trial/' + datasetId + '/' + id;
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

	if ($('#studyFieldMapTree .checkInstance:checked').attr('id')) {
		selectedWithFieldMap = false;
		fieldmapIds = [];
		$('#studyFieldMapTree .checkInstance:checked').each(function() {
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
			if (trial) {
				hasFieldMap = $(this).parent().next().next().next().next().html();
			} else {
				hasFieldMap = $(this).parent().next().next().html();
			}

			// Build id list of selected trials instances
			fieldmapIds.push(studyId + '|' + datasetId + '|' + id);

			if (hasFieldMap == 'Yes') {
				selectedWithFieldMap = true;
			}
		});
		// This is to disable the 2nd popup
		if (selectedWithFieldMap) {
			showMessage(hasFieldmapError);
		} else {
			// Redirect to step 1
			redirectToFirstPage();
		}
	} else {
		// No trial instance is selected
		showMessage(noSelectedTrialInstance);
	}
}

function redirectToFirstPage() {
	location.href = $('#fieldmap-url').attr('href') + '/' + encodeURIComponent(fieldmapIds.join(','));
}

function setSelectedTrialsAsDraggable() {
	$('#selectedTrials').tableDnD();

	$('#selectedTrials').tableDnD({
		onDragClass: 'myDragClass',
		onDrop: function(table, row) {
			setSelectTrialOrderValues();
		}
	});

	setSelectTrialOrderValues();
	styleDynamicTree('selectedTrials');
}

function setSelectTrialOrderValues() {
	var i = 0;
	$('#selectedTrials .orderNo').each(function() {
		$(this).text(i + 1);
		$(this).parent().parent().attr('id', i + 1);
		i++;
	});
	styleDynamicTree('selectedTrials');
}

function styleDynamicTree(treeName) {
	var count = 0;

	if ($('#' + treeName) != null) {
		$('#' + treeName + ' tr').each(function() {
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

function openStudyOldFb() {
	'use strict';
	//for opening old fb
	var openStudyHref = '/Fieldbook/TrialManager/createTrial/open';
	$.ajax({
		url: openStudyHref,
		type: 'GET',
		data: '',
		cache: false,
		success: function() {
		}
	});
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

function openTreeStudy(id) {
	'use strict';
	if (isNursery()) {
		location.href = '/Fieldbook/NurseryManager/editNursery/' + id;
	} else {
		location.href = '/Fieldbook/TrialManager/openTrial/' + id;
	}
}

function openDeleteConfirmation() {
	'use strict';
	var deleteConfirmationText;
	if (isNursery() && !$('.edit-trial-page-identifier').length) {
		$('#delete-nursery-heading-modal').text(deleteNurseryTitle);
		deleteConfirmationText = deleteNurseryConfirmation;
	} else {
		$('#delete-nursery-heading-modal').text(deleteTrialTitle);
		deleteConfirmationText = deleteTrialConfirmation;
	}
	$('#deleteStudyModal').modal({ backdrop: 'static', keyboard: true });
	var idVal = getCurrentStudyIdInTab();
	if (!idVal) {
		idVal = $('#studyId').val();
	}
	var name = $('#study' + idVal + ' .fieldmap-study-name').text();
	if (!name) {
		name = $('.nursery-name-display .fieldmap-study-name').text();
	}
	$('#delete-study-confirmation').html(deleteConfirmationText + ' ' + name + '?');
}

function deleteNursery() {
	'use strict';

	if ($('.review-nursery-page-identifier').length) {
		deleteNurseryInReview();
	} else if ($('.edit-nursery-page-identifier').length) {
		deleteNurseryInEdit();
	} else if ($('.review-trial-page-identifier').length) {
		deleteNurseryInReview();
	} else if ($('.edit-trial-page-identifier').length) {
		deleteNurseryInEdit();
	}
}

function deleteNurseryInReview() {
	'use strict';

	var idVal = getCurrentStudyIdInTab();
	doDeleteNursery(idVal, function(data) {
		$('#deleteStudyModal').modal('hide');
		if (data.isSuccess === '1') {
			setTimeout(function() {
				//simulate close tab
				$('#' + idVal).trigger('click');
				//remove it from the tree
				if ($('#studyTree').dynatree('getTree').getNodeByKey(idVal)) {
					$('#studyTree').dynatree('getTree').getNodeByKey(idVal).remove();
				}
				showSuccessfulMessage('', isNursery() ? deleteNurserySuccessful : deleteTrialSuccessful);
			}, 500);
		} else {
			showErrorMessage('', data.message);
		}
	});
}

function deleteNurseryInEdit() {
	'use strict';
	var idVal = $('#studyId').val();
	doDeleteNursery(idVal, function(data) {
		$('#deleteStudyModal').modal('hide');
			if (data.isSuccess === '1') {
			showSuccessfulMessage('', isNursery() ? deleteNurserySuccessful : deleteTrialSuccessful);
			setTimeout(function() {
				//go back to review nursery page
				location.href = $('#delete-success-return-url').attr('href');
			}, 500);
		} else {
			showErrorMessage('', data.message);
        }
	});
}

function advanceNursery(tableName) {
	'use strict';

	var count = 0,
		idVal = $('#createNurseryMainForm #studyId').val();

	if ($('.import-study-data').data('data-import') === '1') {
		showErrorMessage('', needSaveImportDataError);
		return;
	}

	count++;
	if (count !== 1) {
		showMessage(advanceStudyError);
		return;
	}

	var advanceStudyHref = '/Fieldbook/NurseryManager/advance/nursery';

	if (tableName == 'nursery-table') {
		if (idVal != null) {

			$.ajax({
				url: advanceStudyHref + '/' + encodeURIComponent(idVal),
				type: 'GET',
				aysnc: false,
				success: function(html) {
					$('#advance-nursery-modal-div').html(html);
					$('#advanceNurseryModal').modal({ backdrop: 'static', keyboard: true });

					$('#advanceNurseryModal select').not('.fbk-harvest-year').each(function() {
						$(this).select2({minimumResultsForSearch: $(this).find('option').length == 0 ? -1 : 20});
					});
					$('#advanceNurseryModal select.fbk-harvest-year').each(function() {
						$(this).select2({minimumResultsForSearch: -1});
					});
				}
			});
		}
	}
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

function hideErrorMessage() {
	$('#page-message .alert-danger').fadeOut(1000);
}

function initializeHarvestLocationSelect2(locationSuggestions, locationSuggestionsObj) {

	$.each(locationSuggestions, function(index, value) {
		var locNameDisplay = value.lname;
		if (value.labbr != null && value.labbr != '') {
			locNameDisplay  += ' - (' + value.labbr + ')';
		}
		locationSuggestionsObj.push({
			id: value.locid,
			text: locNameDisplay,
			abbr: value.labbr
		});
	});

	// If combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
	$('#' + getJquerySafeId('harvestLocationIdAll')).select2({
		query: function(query) {
			var data = {results: locationSuggestionsObj}, i, j, s;
			// Return the array that matches
			data.results = $.grep(data.results, function(item, index) {
				return ($.fn.select2.defaults.matcher(query.term, item.text));
			});
			query.callback(data);
		}
	}).on('change', function() {
		$('#' + getJquerySafeId('harvestLocationId')).val($('#' + getJquerySafeId('harvestLocationIdAll')).select2('data').id);
		$('#' + getJquerySafeId('harvestLocationName')).val($('#' + getJquerySafeId('harvestLocationIdAll')).select2('data').text);
		$('#' + getJquerySafeId('harvestLocationAbbreviation')).val($('#' + getJquerySafeId('harvestLocationIdAll')).select2('data').abbr);
		if ($('#harvestloc-tooltip')) {
			$('#harvestloc-tooltip').attr('title', locationTooltipMessage + $('#' + getJquerySafeId('harvestLocationIdAll')).select2('data').abbr);
			$('.help-tooltip-nursery-advance').tooltip('destroy');
			$('.help-tooltip-nursery-advance').tooltip();
		}
	});
}

function initializeHarvestLocationFavSelect2(locationSuggestionsFav, locationSuggestionsFavObj) {

	$.each(locationSuggestionsFav, function(index, value) {
		locationSuggestionsFavObj.push({
			id: value.locid,
			text: value.lname,
			abbr: value.labbr
		});
	});

	// If combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
	$('#' + getJquerySafeId('harvestLocationIdFavorite')).select2({
		minimumResultsForSearch: locationSuggestionsFavObj.length == 0 ? -1 : 20,
		query: function(query) {
			var data = {results: locationSuggestionsFavObj}, i, j, s;
			// Return the array that matches
			data.results = $.grep(data.results, function(item, index) {
				return ($.fn.select2.defaults.matcher(query.term, item.text));
			});
			query.callback(data);
		}
	}).on('change', function() {
		$('#' + getJquerySafeId('harvestLocationId')).val($('#' + getJquerySafeId('harvestLocationIdFavorite')).select2('data').id);
		$('#' + getJquerySafeId('harvestLocationName')).val($('#' + getJquerySafeId('harvestLocationIdFavorite')).select2('data').text);
		$('#' + getJquerySafeId('harvestLocationAbbreviation')).val($('#' + getJquerySafeId('harvestLocationIdFavorite')).select2('data').abbr);
		if ($('#harvestloc-tooltip')) {
			$('#harvestloc-tooltip').attr('title', locationTooltipMessage + $('#' + getJquerySafeId('harvestLocationIdFavorite')).select2('data').abbr);
			$('.help-tooltip-nursery-advance').tooltip('destroy');
			$('.help-tooltip-nursery-advance').tooltip();
		}
	});
}

function initializeMethodSelect2(methodSuggestions, methodSuggestionsObj) {

	$.each(methodSuggestions, function(index, value) {
		methodSuggestionsObj.push({
			id: value.mid,
			text: value.mname + ' - ' + value.mcode,
			tooltip: value.mdesc
		});
	});

	// If combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
	$('#' + getJquerySafeId('methodIdAll')).select2({
		minimumResultsForSearch: methodSuggestionsObj.length == 0 ? -1 : 20,
		query: function(query) {
			var data = {results: methodSuggestionsObj}, i, j, s;
			// Return the array that matches
			data.results = $.grep(data.results, function(item, index) {
				return ($.fn.select2.defaults.matcher(query.term, item.text));
			});
			query.callback(data);
		}

	}).on('change', function() {
		if ($('#' + getJquerySafeId('advanceBreedingMethodId')).length !== 0) {
			$('#' + getJquerySafeId('advanceBreedingMethodId')).val($('#' + getJquerySafeId('methodIdAll')).select2('data').id);
			if ($('#method-tooltip')) {
				$('#method-tooltip').attr('title', $('#' + getJquerySafeId('methodIdAll')).select2('data').tooltip);
				$('.help-tooltip-nursery-advance').tooltip('destroy');
				$('.help-tooltip-nursery-advance').tooltip();
			}
			$('#' + getJquerySafeId('advanceBreedingMethodId')).trigger('change');
		}
	});
}

function initializeMethodFavSelect2(methodSuggestionsFav, methodSuggestionsFavObj) {

	$.each(methodSuggestionsFav, function(index, value) {
		methodSuggestionsFavObj.push({
			id: value.mid,
			text: value.mname + ' - ' + value.mcode,
			tooltip: value.mdesc
		});
	});

	// If combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
	$('#' + getJquerySafeId('methodIdFavorite')).select2({
		minimumResultsForSearch: methodSuggestionsFavObj.length == 0 ? -1 : 20,
		query: function(query) {
			var data = {results: methodSuggestionsFavObj}, i, j, s;
			// Return the array that matches
			data.results = $.grep(data.results, function(item, index) {
				return ($.fn.select2.defaults.matcher(query.term, item.text));
			});
			query.callback(data);
		}
	}).on('change', function() {
		if ($('#' + getJquerySafeId('advanceBreedingMethodId')).length !== 0) {
			$('#' + getJquerySafeId('advanceBreedingMethodId')).val($('#' + getJquerySafeId('methodIdFavorite')).select2('data').id);
			if ($('#method-tooltip')) {
				$('#method-tooltip').attr('title', $('#' + getJquerySafeId('methodIdFavorite')).select2('data').tooltip);
				$('.help-tooltip-nursery-advance').tooltip('destroy');
				$('.help-tooltip-nursery-advance').tooltip();
			}
			$('#' + getJquerySafeId('advanceBreedingMethodId')).trigger('change');
		}
	});
}

function exportTrial(type) {

	var numberOfInstances;

	$('#page-modal-choose-instance-message-r').html('');
	$('#page-modal-choose-instance-message').html('');
	$('.instanceNumber:first').click();
	numberOfInstances = $('#numberOfInstances').val();
	$('.spinner-input').spinedit({
		minimum: 1,
		maximum: parseInt(numberOfInstances),
		value: 1
	});
	$('#exportTrialType').val(type);
	initTrialModalSelection();
	if (type == 2) {
		$('#chooseInstance').detach().appendTo('#importRChooseInstance');
		$('#importRModal').modal('show');
	} else {
		$('#chooseInstance').detach().appendTo('#exportChooseInstance');
		$('#trialModalSelection').modal('show');
	}
}

function initTrialModalSelection() {
	$('#xportInstanceType').val(1);
	$('#exportTrialInstanceNumber').val(1);
	$('#exportTrialInstanceNumber').spinedit('setValue', 1);
	$('#exportTrialInstanceStart').val(1);
	$('#exportTrialInstanceStart').spinedit('setValue', 1);
	$('#exportTrialInstanceEnd').val(1);
	$('#exportTrialInstanceEnd').spinedit('setValue', 1);
	$('#selectedRTrait').prop('selectedIndex', 0);
}

function exportGermplasmList() {
	'use strict';
	var submitExportUrl = '/Fieldbook/ExportManager/exportGermplasmList/',
		formName = '#exportGermplasmListForm',
		type = $('#exportGermplasmListFormat').val();
	var exportOptions = {
		dataType: 'text',
		success: showGermplasmExportResponse // post-submit callback
	};
	if (type === '0') {
		showMessage('Please choose export format');
		return false;
	}
	submitExportUrl = submitExportUrl + type;
	if (isNursery()) {
		submitExportUrl = submitExportUrl + '/N';
	}else {
		submitExportUrl = submitExportUrl + '/T';
	}
	$(formName).attr('action', submitExportUrl);
	$(formName).ajaxForm(exportOptions).submit();
}

function exportStudy() {
	'use strict';
	var type = $('#exportType').val();
	if (type === '0') {
		showMessage('Please choose export type');
		return false;
	}

	if (type === '2') {
		exportStudyToR(type);
	} else {
		doExportContinue(type, isNursery());
	}
}

function exportStudyToR(type) {
	'use strict';
	doExportContinue(type + '/' + $('#selectedRTrait').val(), isNursery());
}
function getExportCheckedAdvancedList() {
	'use strict';
	var advancedLists = [];
	$('.export-advance-germplasm-lists-checkbox').each(function() {
		if ($(this).is(':checked')) {
			advancedLists.push($(this).data('advance-list-id'));
		}
	});
	return advancedLists;
}
function getExportCheckedInstances() {
	'use strict';
	var checkedInstances = [];
	$('.trial-instance-export').each(function() {
		if ($(this).is(':checked')) {
			checkedInstances.push({'instance': $(this).data('instance-number'), 'hasFieldmap':  $(this).data('has-fieldmap')});
		}
	});
	return checkedInstances;
}
function validateTrialInstance() {
	'use strict';
	var checkedInstances = getExportCheckedInstances(),
		counter = 0,
		additionalParam = '';
	if (checkedInstances !== null && checkedInstances.length !== 0) {

		for (counter = 0 ; counter < checkedInstances.length ; counter++) {
			if (additionalParam !== '') {
				additionalParam += '|';
			}
			additionalParam += checkedInstances[counter].instance;
		}
	}
	return additionalParam;
}
function exportAdvanceStudyList(advancedListIdParams) {
	'use strict';
	$('#exportAdvanceStudyForm #exportAdvanceListGermplasmIds').val(advancedListIdParams);
	$('#exportAdvanceStudyForm #exportAdvanceListGermplasmType').val($('#exportAdvancedType').val());
	$('#exportAdvanceStudyForm').ajaxForm(exportAdvanceOptions).submit();
}
function doExportContinue(paramUrl, isNursery) {
	var currentPage = $('#measurement-data-list-pagination .pagination .active a').html(),
		additionalParams = '',
		formname,
		$form,
		serializedData,
		exportWayType;

	if (isNursery) {
		formname = '#addVariableForm';
	} else {
		formname = '#addVariableForm, #addVariableForm2';
	}
	$form = $(formname);
	serializedData = $form.serialize();
	if (!isNursery) {
		additionalParams = validateTrialInstance();
		if (additionalParams == 'false') {
			return false;
		}
	}
	exportWayType = '/' + $('#exportWayType').val();

	doFinalExport(paramUrl, additionalParams, exportWayType, isNursery);
}

function doFinalExport(paramUrl, additionalParams, exportWayType, isNursery) {
	var action = submitExportUrl,
		newAction = '',
		studyId = '0',
		visibleColumns = '';
	if (isNursery) {
		newAction = action + 'export/' + paramUrl;
	} else {
		// Meaning its trial
		newAction = action + 'exportTrial/' + paramUrl + '/' + additionalParams;
	}
	newAction += exportWayType;
	if ($('#browser-nurseries').length !== 0) {
		// Meaning we are on the landing page
		studyId = getCurrentStudyIdInTab();
	} else {
		// the nursery/trial is opened
		visibleColumns = getMeasurementTableVisibleColumns(isNursery);
		var exportType = $('#exportType').val();
		// excel or csv
		if ((exportType == 7 || exportType == 3) && visibleColumns.length !== 0) {
			showWarningMessageForRequiredColumns(visibleColumns);
		}
	}
	var columnOrders = '';
	if ($('.review-nursery-details').length == 0) {
		var columnsOrder = BMS.Fieldbook.MeasurementsTable.getColumnOrdering('measurement-table', true);
		columnOrders = (JSON.stringify(columnsOrder));
	}
	$.ajax(newAction, {
		headers: {
			'Accept': 'application/json',
			'Content-Type': 'application/json'
		},
		data: JSON.stringify({
			'visibleColumns': visibleColumns,
			'columnOrders': columnOrders,
			'studyExportId': studyId
		}),
		type: 'POST',
		dataType: 'text',
		success: function(data) {
			showExportResponse(data);
		}
	});
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

function getMeasurementTableVisibleColumns(isNursery) {
	'use strict';
	var visibleColumns = '';
	if (!isNursery && $('[ui-view="editMeasurements"]').text().length === 0) {
		return visibleColumns;
	}
	var headers = $('#measurement-table_wrapper .dataTables_scrollHead [data-term-id]');
	var headerCount = headers.size();
	var i = 0;
	for (i = 0; i < headerCount; i++) {
		var headerId = $('#measurement-table_wrapper .dataTables_scrollHead [data-term-id]:eq(' + i + ')').attr('data-term-id');
		if ($.isNumeric(headerId)) {
			if (visibleColumns.length == 0) {
				visibleColumns = headerId;
			} else {
				visibleColumns = visibleColumns + ',' + headerId;
			}
		}
	}
	return visibleColumns;
}

function importNursery(type) {
	'use strict';
	var action = '/Fieldbook/ImportManager/import/' + (isNursery() ? 'Nursery' : 'Trial') + '/' + type,
		formName = '#importStudyUploadForm';

	$(formName).attr('action', action);
}

function submitImportStudy() {
	'use strict';
	if ($('#importType').val() === '0') {
		showErrorMessage('page-import-study-message-modal', 'Please choose import type');
		return false;
	}

	if ($('#fileupload').val() === '') {
		showErrorMessage('page-import-study-message-modal', 'Please choose a file to import');
		return false;
	}

	if ($('.import-study-data').data('data-import') === '1') {
		setTimeout(function() {$('#importOverwriteConfirmation').modal({ backdrop: 'static', keyboard: true });}, 300);
	} else {
		continueStudyImport(false);
	}
}
function continueStudyImport(doDataRevert) {
	'use strict';
	if (doDataRevert) {
		revertData(false);
		$('#importOverwriteConfirmation').modal('hide');
	}

	$('#importStudyUploadForm').ajaxForm(importOptions).submit();
}

function showImportOptions() {
	'use strict';
	$('#fileupload').val('');
	$('#importStudyModal').modal({ backdrop: 'static', keyboard: true });
	if (isNursery()) {
		$('li#nursery-measurements-li a').tab('show');
	} else {
		window.location.hash = '#/editMeasurements';
	}
	if ($('.import-study-data').data('data-import') === '1') {
		showAlertMessage('', importDataWarningNotification);
	}
}
function goBackToImport() {
	'use strict';
	revertData(false);
	$('#importStudyConfirmationModal').modal('hide');
	$('#importStudyDesigConfirmationModal').modal('hide');
	$('#importOverwriteConfirmation').modal('hide');
	setTimeout(function() {$('#importStudyModal').modal({ backdrop: 'static', keyboard: true });}, 300);

}

function isFloat(value) {
	'use strict';
	return !isNaN(parseInt(value, 10)) && (parseFloat(value, 10) == parseInt(value, 10));
}

function moveToTopScreen() {

}

function openImportGermplasmList(type) {
	'use strict';
	$('.germplasmAndCheckSection').data('import-from', type);
	$.ajax({
		url: '/Fieldbook/ListTreeManager/germplasm/import/url',
		type: 'GET',
		data: '',
		cache: false,
		success: function(html) {
			setTimeout(function() {
				$('#importFrame').attr('src', html);
				$('#importGermplasmModal').modal({ backdrop: 'static', keyboard: true });
			}, 500);
		}
	});
}

function doTreeHighlight(treeName, nodeKey) {

	var count = 0,
		key = '',
		elem;

	$('#' + treeName).dynatree('getTree').activateKey(nodeKey);
	$('#' + treeName).find('*').removeClass('highlight');

	// Then we highlight the nodeKey and its parents
	elem = nodeKey.split('_');
	for (count = 0 ; count < elem.length ; count++) {
		if (key != '') {
			key = key + '_';
		}
		key = key + elem[count];
		$('.' + key).addClass('highlight');
	}
}

function addCreateNurseryRequiredAsterisk() {
	var requiredText = '<span class="required">*</span>',
		i,
		cvTermId;

	for (i = 0; i < requiredFields.length; i++) {
		cvTermId = requiredFields[i];
		if ($('.cvTermIds[value=""+cvTermId+""]').length !== 0) {
			$('.cvTermIds[value=""+cvTermId+""]').parent().parent().find('.nursery-level-label').parent().append(requiredText);
		}
	}
}

function addCreateTrialRequiredAsterisk() {
	var requiredText = '<span class="required">*</span>',
		i,
		cvTermId;

	for (i = 0; i < requiredFields.length; i++) {
		cvTermId = requiredFields[i];
		if ($('.cvTermIds[value=""+cvTermId+""]').length !== 0) {
			$('.cvTermIds[value=""+cvTermId+""]').parent().parent().find('.trial-level-label').parent().append(requiredText);
		}
	}
}

function getDateRowIndex(divName, dateCvTermId) {

	var rowIndex = -1;

	$('.' + divName + ' .cvTermIds').each(function(index) {
		if ($(this).val() == parseInt(dateCvTermId)) {
			rowIndex = index;
		}
	});
	return rowIndex;
}

function validateStartEndDate(divName) {
	//8050 - start
	var startDateIndex = getDateRowIndex(divName, startDateId),
		endDateIndex = getDateRowIndex(divName, endDateId),
		startDate = $('#' + getJquerySafeId('studyLevelVariables' + startDateIndex + '.value')).val(),
		endDate = $('#' + getJquerySafeId('studyLevelVariables' + endDateIndex + '.value')).val();

	startDate = startDate == null ? '' : startDate;
	endDate = endDate == null ? '' : endDate;

	if (startDate === '' && endDate === '') {
		return true;
	} else if (startDate !== '' && endDate === '') {
		return true;
	} else if (startDate === '' && endDate !== '') {
		showErrorMessage('page-message', startDateRequiredError);
		return false;
	} else if (parseInt(startDate) > parseInt(endDate)) {
		showErrorMessage('page-message', startDateRequiredEarlierError);
		return false;
	}
	return true;
}

function getIEVersion() {
	var myNav = navigator.userAgent.toLowerCase();
	return (myNav.indexOf('msie') != -1) ? parseInt(myNav.split('msie')[1]) : false;
}

function validatePlantsSelected() {
	var ids = '',
		isMixed = false,
		isBulk = false,
		valid;

	if ($('.bulk-section').is(':visible')) {
		if ($('input[type=checkbox][name=allPlotsChoice]:checked').val() !== '1') {
			ids = ids + $('#plotVariateId').val();
		}
		isBulk = true;
	}
	if ($('.lines-section').is(':visible')) {
		if ($('input[type=checkbox][name=lineChoice]:checked').val() !== '1') {
			if (ids !== '') {
				ids = ids + ',';
			}
			ids = ids + $('#lineVariateId').val();
		}
		if (isBulk) {
			isMixed = true;
		}
	}

	valid = true;
	if ($('input[type=checkbox][name=methodChoice]:checked').val() === '1'
		&& $('#namingConvention').val() !== '1'
		&& $('#advanceBreedingMethodId').val() === '') {
		showErrorMessage('page-advance-modal-message', msgMethodError);
		valid = false;
	}
	if(valid){
		valid = validateBreedingMethod();
	}
	if (valid && ids !== '')	{
		$.ajax({
			url: '/Fieldbook/NurseryManager/advance/nursery/countPlots/' + ids,
			type: 'GET',
			cache: false,
			async: false,
			success: function(data) {
				var choice,
					lineSameForAll;

				if (isMixed) {
					if (data == 0) {
						var param = $('lineVariateId').select2('data').text + ' and/or ' + $('#plotVariateId').select2('data').text;
						var newMessage = msgEmptyListError.replace(new RegExp(/\{0\}/g), param);
						showErrorMessage('page-advance-modal-message', newMessage);
						valid = false;
					}
				} else if (isBulk) {
					choice = !$('#plot-variates-section').is(':visible');
					if (choice == false && data == '0') {
						var param = $('#plotVariateId').select2('data').text;
						var newMessage = msgEmptyListError.replace(new RegExp(/\{0\}/g), param);
						showErrorMessage('page-advance-modal-message', newMessage);
						valid = false;
					}
				} else {
					choice = !$('#line-variates-section').is(':visible');
					lineSameForAll = $('input[type=checkbox][name=lineChoice]:checked').val() == 1;
					if (lineSameForAll == false && choice == false && data == '0') {
						var param = $('#lineVariateId').select2('data').text;
						var newMessage = msgEmptyListError.replace(new RegExp(/\{0\}/g), param);
						showErrorMessage('page-advance-modal-message', newMessage);
						valid = false;
					}
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				console.log('The following error occured: ' + textStatus, errorThrown);
			},
			complete: function() {
			}
		});
	}
	if (valid && isMixed) {
		return valid;
	}
	return valid;
}

function callAdvanceNursery() {
	var lines = $('#lineSelected').val();

	if (!lines.match(/^\s*(\+|-)?\d+\s*$/)) {
		showErrorMessage('page-advance-modal-message', linesNotWholeNumberError);
		return false;
	} else if (validatePlantsSelected()) {
		SaveAdvanceList.doAdvanceNursery();
	}
}

function showSelectedAdvanceTab(uniqueId) {
	showSelectedTab('advance-list' + uniqueId);
}

function closeAdvanceListTab(uniqueId) {
	'use strict';
	$('li#advance-list' + uniqueId + '-li').remove();
	$('.info#advance-list' + uniqueId).remove();
	if ($('#list' + uniqueId).length === 1) {
		$('#list' + uniqueId).remove();
	}

	setTimeout(function() {
		$('#create-nursery-tab-headers li:eq(0) a').tab('show');
		$('.nav-tabs').tabdrop('layout');
	}, 100);
}

function displayAdvanceList(uniqueId, germplasmListId, listName, isDefault, advancedGermplasmListId) {
	'use script';
	var id = advancedGermplasmListId ? advancedGermplasmListId : germplasmListId;
	var url = '/Fieldbook/germplasm/list/advance/' + id;
	if (!isDefault) {
		$('#advanceHref' + id + ' .fbk-close-tab').before(': [' + listName + ']');
		url += '?isSnapshot=0';
	} else {
		url += '?isSnapshot=1';
	}
	$.ajax({
		url: url,
		type: 'GET',
		cache: false,
		success: function(html) {
			$('#advance-list' + id).html(html);
			//we just show the button
			$('.export-advance-list-action-button').removeClass('fbk-hide');
			$('#advance-list' + id + '-li').addClass('advance-germplasm-items');
			$('#advance-list' + id + '-li').data('advance-germplasm-list-id', advancedGermplasmListId);
		}
	});
}

function validateBreedingMethod() {
	var id = $('#methodVariateId').val(),
		valid = true;

	if ($('input[type=checkbox][name=methodChoice]:checked').val() !== '1' && id) {
		$.ajax({
			url: '/Fieldbook/NurseryManager/advance/nursery/countPlots/' + id,
			type: 'GET',
			cache: false,
			async: false,
			success: function(data) {
				if (data == 0) {
					var newMessage = noMethodValueError.replace(new RegExp(/\{0\}/g), $('#methodVariateId').select2('data').text);
					showErrorMessage('page-advance-modal-message', newMessage);
					valid = false;
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				console.log('The following error occured: ' + textStatus, errorThrown);
			},
			complete: function() {
			}
		});
	}
	return valid;
}

function showBaselineTraitDetailsModal(id) {
	'use strict';

	if (id !== '') {
		$.ajax({
			url: '/Fieldbook/manageSettings/settings/details/' + id,
			type: 'GET',
			cache: false,
			success: function(html) {
				$('.variable-details-section').empty().append(html);
				if ($('#selectedStdVarId').length != 0) {
					$('#selectedStdVarId').val(id);
				}
				$('#variableDetailsModal').modal('toggle');
				if ($('#variableDetailsModal')) {
					var variableName = $('#ontology-tabs').data('selectedvariablename');
					$('#variableDetailsModal .modal-title').html(variableDetailsHeader + ' ' + variableName);
				}
			}
		});
	}
}

function showBaselineTraitDetailsModal(id, variableTypeId) {
	'use strict';

	if (id !== '') {
		$.ajax({
			url: '/Fieldbook/manageSettings/settings/details/' + variableTypeId + '/' + id,
			type: 'GET',
			cache: false,
			success: function (html) {
				$('.variable-details-section').empty().append(html);
				if ($('#selectedStdVarId').length != 0) {
					$('#selectedStdVarId').val(id);
				}
				$('#variableDetailsModal').modal('toggle');
				if ($('#variableDetailsModal')) {
					var variableName = $('#ontology-tabs').data('selectedvariablename');
					$('#variableDetailsModal .modal-title').html(variableDetailsHeader + ' ' + variableName);
				}
			}
		});
	}
}

function populateVariableDetails(standardVariable) {
	if (standardVariable != null) {
		$('#traitClass').html(checkIfNull(standardVariable.traitClass));
		$('#property').html(checkIfNull(standardVariable.property));
		$('#method').html(checkIfNull(standardVariable.method));
		$('#scale').html(checkIfNull(standardVariable.scale));
		$('#dataType').html(checkIfNull(standardVariable.dataType));
		$('#role').html(checkIfNull(standardVariable.role));
		$('#cropOntologyId').html(checkIfNull(standardVariable.cropOntologyId));
		$('#variableDetailsModal .modal-title').html(variableDetailsHeader + ' ' + checkIfNull(standardVariable.name));
	} else {
		$('#traitClass').html('');
		$('#property').html('');
		$('#method').html('');
		$('#scale').html('');
		$('#dataType').html('');
		$('#role').html('');
		$('#cropOntologyId').html('');
		$('#variableDetailsModal .modal-title').html();
	}
}

function checkIfNull(object) {
	if (object != null) {
		return object;
	} else {
		return '';
	}
}

function recreateMethodCombo() {
	var selectedMethodAll = $('#methodIdAll').val(),
		selectedMethodFavorite = $('#methodIdFavorite').val();
	var createGermplasm = false;
	var createGermplasmOpened = false;

	if ($('#importStudyDesigConfirmationModal').length !== 0) {
		createGermplasm = true;
		if ($('#importStudyDesigConfirmationModal').hasClass('in') || $('#importStudyDesigConfirmationModal').data('open') === '1') {
			createGermplasmOpened = true;
		}
	}

	$.ajax({
		url: '/Fieldbook/NurseryManager/advance/nursery/getBreedingMethods',
		type: 'GET',
		cache: false,
		data: '',
		async: false,
		success: function(data) {
			if (data.success == '1') {
				if (createGermplasmOpened) {
					refreshImportMethodCombo(data);
					refreshMethodComboInSettings(data);
				} else if (selectedMethodAll != null) {

					//recreate the select2 combos to get updated list of methods
					recreateMethodComboAfterClose('methodIdAll', $.parseJSON(data.allNonGenerativeMethods));
					recreateMethodComboAfterClose('methodIdFavorite', $.parseJSON(data.favoriteNonGenerativeMethods));
					showCorrectMethodCombo();
					//set previously selected value of method
					if ($('#showFavoriteMethod').prop('checked')) {
						setComboValues(methodSuggestionsFavObj, selectedMethodFavorite, 'methodIdFavorite');
					} else {
						setComboValues(methodSuggestionsObj, selectedMethodAll, 'methodIdAll');
					}

					if ($('#advanceNurseryModal').length > 0) {
						refreshMethodComboInSettings(data);
					}
				} else {
					if ($('.hasCreateGermplasm').length === 0 || ($('.hasCreateGermplasm').length > 0 && $('.hasCreateGermplasm').val() === '0')) {
						refreshMethodComboInSettings(data);
					}
					if (createGermplasm) {
						refreshImportMethodCombo(data);
					}
				}
			} else {
				showErrorMessage('page-message', data.errorMessage);
			}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			console.log('The following error occured: ' + textStatus, errorThrown);
		},
		complete: function() {
		}
	});
}

function refreshImportMethodCombo(data) {
	var selectedValue = null;
	if ($('#importMethodId').select2('data')) {
		selectedValue = $('#importMethodId').select2('data').id;
	}
	if ($('#importFavoriteMethod').is(':checked')) {

		initializePossibleValuesCombo($.parseJSON(data.favoriteMethods),
				'#importMethodId', false, selectedValue);
	} else {
		initializePossibleValuesCombo($.parseJSON(data.allMethods),
				'#importMethodId', false, selectedValue);
	}
	replacePossibleJsonValues(data.favoriteMethods, data.allMethods, 'Method');
}

function refreshImportLocationCombo(data) {
	var selectedValue = null;
	if ($('#importLocationId').select2('data')) {
		selectedValue = $('#importLocationId').select2('data').id;
	}
	if ($('#importFavoriteLocation').is(':checked')) {
		initializePossibleValuesCombo($.parseJSON(data.favoriteLocations),
				'#importLocationId', true, selectedValue);
	} else {
		initializePossibleValuesCombo($.parseJSON(data.allBreedingLocations),
				'#importLocationId', true, selectedValue);
	}
	replacePossibleJsonValues(data.favoriteLocations, data.allBreedingLocations, 'Location');
}

function generateGenericLocationSuggestions(genericLocationJson) {
	var genericLocationSuggestion = [];
	$.each(genericLocationJson, function(index, value) {
		var locNameDisplay = value.lname;
		if (value.labbr != null && value.labbr != '') {
			locNameDisplay  += ' - (' + value.labbr + ')';
		}
		genericLocationSuggestion.push({
			'id': value.locid,
			'text': locNameDisplay
		});
	});
	return genericLocationSuggestion;
}
function recreateLocationCombo() {
	var selectedLocationAll = $('#harvestLocationIdAll').val();
	var selectedLocationFavorite = $('#harvestLocationIdFavorite').val();

	var inventoryPopup = false;
	var advancePopup = false;
	var fieldmapScreen = false;
	var createGermplasm = false;
	var hasCreateGermplasm = false;
	var createGermplasmOpened = false;

	if ($('#addLotsModal').length !== 0 && ($('#addLotsModal').data('open') === '1' ||  $('#addLotsModal').hasClass('in'))) {
		inventoryPopup = true;
	} else if ($('#advanceNurseryModal').length !== 0 && ($('#advanceNurseryModal').data('open') === '1' ||  $('#advanceNurseryModal').hasClass('in'))) {
		advancePopup = true;
	} else if ($('#enterFieldDetailsForm').length !== 0) {
		fieldmapScreen = true;
	}

	if ($('#importStudyDesigConfirmationModal').length !== 0) {
		createGermplasm = true;
		if ($('#importStudyDesigConfirmationModal').hasClass('in') || $('#importStudyDesigConfirmationModal').data('open') === '1') {
			createGermplasmOpened = true;
		}
	}

	if ($('.hasCreateGermplasm').length === 0 || ($('.hasCreateGermplasm').length > 0 && $('.hasCreateGermplasm').val() === '0')) {
		hasCreateGermplasm = true;
	}

	if (inventoryPopup || advancePopup || fieldmapScreen || createGermplasm || hasCreateGermplasm || createGermplasmOpened) {
		$.ajax({
			url: '/Fieldbook/NurseryManager/advance/nursery/getLocations',
			type: 'GET',
			cache: false,
			data: '',
			async: false,
			success: function(data) {
				if (data.success == '1') {
					if (createGermplasmOpened) {
						refreshImportLocationCombo(data);
						refreshLocationComboInSettings(data);
					} else if (inventoryPopup) {
						recreateLocationComboAfterClose('inventoryLocationIdAll', $.parseJSON(data.allSeedStorageLocations));
						recreateLocationComboAfterClose('inventoryLocationIdFavorite', $.parseJSON(data.favoriteLocations));
						showCorrectLocationInventoryCombo();
						// set previously selected value of location
						if ($('#showFavoriteLocationInventory').prop('checked')) {
							setComboValues(generateGenericLocationSuggestions($.parseJSON(data.favoriteLocations)), $('#inventoryLocationIdFavorite').val(), 'inventoryLocationIdFavorite');
						} else {
							setComboValues(generateGenericLocationSuggestions($.parseJSON(data.allSeedStorageLocations)), $('#inventoryLocationIdAll').val(), 'inventoryLocationIdAll');
						}
						refreshLocationComboInSettings(data);
					} else if (advancePopup === true
						|| selectedLocationAll != null) {
						// recreate the select2 combos to get updated list
						// of locations
						recreateLocationComboAfterClose('harvestLocationIdAll', $.parseJSON(data.allBreedingLocations));
						recreateLocationComboAfterClose('harvestLocationIdFavorite', $.parseJSON(data.favoriteLocations));
						showCorrectLocationCombo();
						// set previously selected value of location
						if ($('#showFavoriteLocation').prop('checked')) {
							setComboValues(locationSuggestionsFav_obj, selectedLocationFavorite, 'harvestLocationIdFavorite');
						} else {
							setComboValues(locationSuggestions_obj, selectedLocationAll, 'harvestLocationIdAll');
						}
						refreshLocationComboInSettings(data);

					} else if (fieldmapScreen === true) {
						//recreate the select2 combos to get updated list of locations
						recreateLocationComboAfterClose('fieldLocationIdAll', $.parseJSON(data.allBreedingLocations));
						recreateLocationComboAfterClose('fieldLocationIdFavorite', $.parseJSON(data.favoriteLocations));
						showCorrectLocationCombo();
						//set previously selected value of location
						if ($('#showFavoriteLocation').prop('checked')) {
							setComboValues(locationSuggestionsFav_obj, $('#fieldLocationIdFavorite').val(), 'fieldLocationIdFavorite');
						} else {
							setComboValues(locationSuggestions_obj, $('#fieldLocationIdAll').val(), 'fieldLocationIdAll');
						}
					} else {
						if (hasCreateGermplasm) {
							refreshLocationComboInSettings(data);
						}
						if (createGermplasm) {
							refreshImportLocationCombo(data);
						}
					}
				} else {
					showErrorMessage('page-message', data.errorMessage);
				}

			}
		});
	}
}

function refreshMethodComboInSettings(data) {
	//get index of breeding method row
	var index = getBreedingMethodRowIndex(), selectedVal = null;
	if (index > -1) {
		var pleaseChoose = '{"mid":0,"mname":"Please Choose","mdesc":"Please Choose"}';
		if ($.parseJSON(data.favoriteNonGenerativeMethods).length == 0) {
			data.favoriteNonGenerativeMethods = '[' + pleaseChoose + ']';
		} else {
			data.favoriteNonGenerativeMethods = '[' + pleaseChoose + ',' + data.favoriteNonGenerativeMethods.substring(1);
		}

		if ($.parseJSON(data.allNonGenerativeMethods).length == 0) {
			data.allNonGenerativeMethods = '[' + pleaseChoose + ']';
		} else {
			data.allNonGenerativeMethods = '[' + pleaseChoose + ', ' + data.allNonGenerativeMethods.substring(1);
		}

		if ($('#' + getJquerySafeId('studyLevelVariables' + index + '.value')).select2('data')) {
			selectedVal = $('#' + getJquerySafeId('studyLevelVariables' + index + '.value')).select2('data').id;
		}

		//recreate select2 of breeding method
		initializePossibleValuesCombo([],
				'#' + getJquerySafeId('studyLevelVariables' + index + '.value'), false, selectedVal);

		//update values of combo
		if ($('#' + getJquerySafeId('studyLevelVariables' + index + '.favorite1')).is(':checked')) {
			initializePossibleValuesCombo($.parseJSON(data.favoriteNonGenerativeMethods),
					'#' + getJquerySafeId('studyLevelVariables' + index + '.value'), false, selectedVal);
		} else {
			initializePossibleValuesCombo($.parseJSON(data.allNonGenerativeMethods),
					'#' + getJquerySafeId('studyLevelVariables' + index + '.value'), false, selectedVal);
		}

		replacePossibleJsonValues(data.favoriteNonGenerativeMethods, data.allNonGenerativeMethods, index);
	}
}

function refreshLocationComboInSettings(data) {
	var selectedVal = null;
	var index = getLocationRowIndex();
	if (index > -1) {
		if ($('#' + getJquerySafeId('studyLevelVariables' + index + '.value')).select2('data')) {
			selectedVal = $('#' + getJquerySafeId('studyLevelVariables' + index + '.value')).select2('data').id;
		}
		initializePossibleValuesCombo([], '#' + getJquerySafeId('studyLevelVariables' + index + '.value'), true, selectedVal);

		// update values in combo
		if ($('#' + getJquerySafeId('studyLevelVariables' + index + '.favorite1')).is(':checked')) {
			initializePossibleValuesCombo($.parseJSON(data.favoriteLocations), '#' + getJquerySafeId('studyLevelVariables' + index + '.value'), false, selectedVal);
		} else {
			initializePossibleValuesCombo($.parseJSON(data.allBreedingLocations), '#' + getJquerySafeId('studyLevelVariables' + index + '.value'), true, selectedVal);
		}

		replacePossibleJsonValues(data.favoriteLocations, data.allBreedingLocations, index);
	}
}

function recreateLocationComboAfterClose(comboName, data) {
	if (comboName == 'harvestLocationIdAll') {
		//clear all locations dropdown
		locationSuggestions = [];
		locationSuggestionsObj = [];
		initializeHarvestLocationSelect2(locationSuggestions, locationSuggestionsObj);
		//reload the data retrieved
		locationSuggestions = data;
		initializeHarvestLocationSelect2(locationSuggestions, locationSuggestionsObj);
	} else if (comboName == 'inventoryLocationIdAll') {
		//clear all locations dropdown
		initializePossibleValuesComboInventory(data, '#inventoryLocationIdAll', true, null);
	} else if (comboName == 'inventoryLocationIdFavorite') {
		initializePossibleValuesComboInventory(data, '#inventoryLocationIdFavorite', false, null);
	} else {
		//clear the favorite locations dropdown
		locationSuggestionsFav = [];
		locationSuggestionsFavObj = [];
		initializeHarvestLocationFavSelect2(locationSuggestionsFav, locationSuggestionsFavObj);
		//reload the data
		locationSuggestionsFav = data;
		initializeHarvestLocationFavSelect2(locationSuggestionsFav, locationSuggestionsFavObj);
	}

}

function recreateMethodComboAfterClose(comboName, data) {
	if (comboName == 'methodIdAll') {
		//clear the all methods dropdown
		methodSuggestions = [];
		methodSuggestionsObj = [];
		initializeMethodSelect2(methodSuggestions, methodSuggestionsObj);
		//reload the data
		methodSuggestions = data;
		initializeMethodSelect2(methodSuggestions, methodSuggestionsObj);
	} else {
		//clear the favorite methods dropdown
		methodSuggestionsFav = [];
		methodSuggestionsFavObj = [];
		initializeMethodFavSelect2(methodSuggestionsFav, methodSuggestionsFavObj);
		//reload the data
		methodSuggestionsFav = data;
		initializeMethodFavSelect2(methodSuggestionsFav, methodSuggestionsFavObj);
	}
}

function changeBuildOption() {
	'use strict';
	if ($('#studyBuildOption').is(':checked')) {
		$('#choosePreviousStudy').removeClass('fbk-hide');
		$('#choosePreviousStudy').addClass('fbk-show-inline');
	} else {
		$('#choosePreviousStudy').addClass('fbk-hide');
		$('#choosePreviousStudy').removeClass('fbk-show-inline');
		clearSettings();
	}
}

function createFolder() {
	'use strict';

	var folderName = $.trim($('#addFolderName').val()),
		parentFolderId;

	if (folderName === '') {
		showErrorMessage('page-add-study-folder-message-modal', folderNameRequiredMessage);
		return false;
	} else if (! isValidInput(folderName)) {
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
			success: function(data) {
				var node;

				if (data.isSuccess == 1) {
					node = $('#studyTree').dynatree('getTree').getActiveNode();
					doStudyLazyLoad(node, data.newFolderId);
					node.focus();
					node.expand();
					$('#addFolderDiv').slideUp();
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

	var currentFolderName,
		isFolder = $('#studyTree').dynatree('getTree').getActiveNode().data.isFolder,
		deleteConfirmationText;

	if (!$(object).hasClass('disable-image')) {
		if (isFolder) {
			$('#delete-heading-modal').text(deleteFolderTitle);
			deleteConfirmationText = deleteConfirmation;
		} else {
			if (isNursery()) {
				$('#delete-heading-modal').text(deleteNurseryTitle);
				deleteConfirmationText = deleteNurseryConfirmation;
			} else {
				$('#delete-heading-modal').text(deleteTrialTitle);
				deleteConfirmationText = deleteTrialConfirmation;
			}
		}
		$('#deleteStudyFolder').modal('show');
		hideAddFolderDiv();
		hideRenameFolderDiv();
		currentFolderName = $('#studyTree').dynatree('getTree').getActiveNode().data.title;
		$('#delete-confirmation').html(deleteConfirmationText + ' ' + currentFolderName + '?');
		$('#page-delete-study-folder-message-modal').html('');
	}
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
			success: function(data) {
				var node;
				if (data.isSuccess === '1') {
					$('#deleteStudyFolder').modal('hide');
					node = $('#studyTree').dynatree('getTree').getActiveNode();
					if (node != null) {
						node.remove();
					}
					changeBrowseNurseryButtonBehavior(false);
					showSuccessfulMessage('', deleteFolderSuccessful);
				} else {
					showErrorMessage('page-delete-study-folder-message-modal', data.message);
				}
			}
		});
	} else {
		doDeleteNursery(folderId, function(data) {
			var node;
			$('#deleteStudyFolder').modal('hide');
			if (data.isSuccess === '1') {
				node = $('#studyTree').dynatree('getTree').getActiveNode();
				if (node != null) {
					node.remove();
				}
				changeBrowseNurseryButtonBehavior(false);
				showSuccessfulMessage('', isNursery() ? deleteNurserySuccessful : deleteTrialSuccessful);
			} else {
				showErrorMessage('', data.message);
			}
		});
	}
}

function moveStudy(sourceNode, targetNode) {
	'use strict';
	var sourceId = sourceNode.data.key,
		targetId = targetNode.data.key,
		isStudy = sourceNode.data.isFolder === true ? 0 : 1,
		title;

	if (targetId === 'LOCAL') {
		targetId = 1;
	}

	$.ajax({
		url: '/Fieldbook/StudyTreeManager/moveStudyFolder',
		type: 'POST',
		data: 'sourceId=' + sourceId + '&targetId=' + targetId + '&isStudy=' + isStudy,
		cache: false,
		success: function(data) {
			var node = targetNode;
			sourceNode.remove();
			doStudyLazyLoad(node);
			node.focus();
		}
	});
}

function deleteGermplasmFolder(object) {
	'use strict';

	var currentFolderName;

	if (!$(object).hasClass('disable-image')) {
		$('#deleteGermplasmFolder').modal('show');
		$('#addGermplasmFolderDiv').slideUp('fast');
		$('#renameGermplasmFolderDiv').slideUp('fast');
		currentFolderName = $('#' + getDisplayedTreeName()).dynatree('getTree').getActiveNode().data.title;
		$('#delete-folder-confirmation').html(deleteConfirmation + ' ' + currentFolderName + '?');

		$('#page-delete-germplasm-folder-message-modal').html('');
	}
}

function submitDeleteGermplasmFolder() {
	'use strict';

	var folderId = $('#' + getDisplayedTreeName()).dynatree('getTree').getActiveNode().data.key;

	$.ajax({
		url: '/Fieldbook/ListTreeManager/deleteGermplasmFolder',
		type: 'POST',
		data: 'folderId=' + folderId,
		cache: false,
		success: function(data) {
			var node;
			if (data.isSuccess === '1') {
				$('#deleteGermplasmFolder').modal('hide');
				node = $('#' + getDisplayedTreeName()).dynatree('getTree').getActiveNode();
				node.remove();
				showSuccessfulMessage('', deleteItemSuccessful);
			} else {
				showErrorMessage('page-delete-germplasm-folder-message-modal', data.message);
			}
		}
	});
}

function moveGermplasm(sourceNode, targetNode) {
	'use strict';
	var sourceId = sourceNode.data.key,
		targetId = targetNode.data.key,
		title;

	if (targetId === 'LOCAL') {
		targetId = 1;
	}

	$.ajax({
		url: '/Fieldbook/ListTreeManager/moveGermplasmFolder',
		type: 'POST',
		data: 'sourceId=' + sourceId + '&targetId=' + targetId,
		cache: false,
		success: function(data) {
			var node = targetNode;
			sourceNode.remove();
			doGermplasmLazyLoad(node);
			node.focus();
		}
	});
}

function closeModal(modalId) {
	'use strict';
	$('#' + modalId).modal('hide');
}

function openGermplasmDetailsPopopWithGidAndDesig(gid, desig) {
	'use strict';
	$.ajax({
		url: '/Fieldbook/ListTreeManager/germplasm/detail/url',
		type: 'GET',
		data: '',
		cache: false,
		success: function(html) {
			var germplasmDetailsUrl = html;
			$('#openGermplasmFrame').attr('src', germplasmDetailsUrl + gid);
			$('#openGermplasmModal .modal-title').html(headerMsg1 + ' ' + desig + ' (' + headerMsg2 + ' ' + gid + ')');
			$('#openGermplasmModal').modal({ backdrop: 'static', keyboard: true });
		}
	});
}

function editExperiment(tableIdentifier, expId, rowIndex) {
	'use strict';
	var canEdit = $('body').data('needGenerateExperimentalDesign') === '1' ? false : true,
		needToSaveFirst = $('body').data('needToSave') === '1' ? true : false;
	if (isNursery() || canEdit) {
		// We show the ajax page here
		if (needToSaveFirst) {
			showAlertMessage('', measurementsTraitsChangeWarning);
		} else {
			$.ajax({
				url: '/Fieldbook/Common/addOrRemoveTraits/update/experiment/' + rowIndex,
				type: 'GET',
				cache: false,
				success: function(dataResp) {
					$('.edit-experiment-section').html(dataResp);
					$('.updateExperimentModal').modal({ backdrop: 'static', keyboard: true });
				}
			});
		}
	} else {
		showAlertMessage('', measurementWarningNeedGenExpDesign);
	}
}

function isAllowedEditMeasurementDataCell(isShowErrorMessage) {
	'use strict';
	var canEdit = $('body').data('needGenerateExperimentalDesign') === '1' ? false : true,
			needToSaveFirst = $('body').data('needToSave') === '1' ? true : false;
	if (isNursery() || canEdit) {
		// We show the ajax page here
		if (needToSaveFirst) {
			if (isShowErrorMessage) {
				showAlertMessage('', measurementsTraitsChangeWarning);
			}
		} else {
			return true;
		}
	} else {
		if (showErrorMessage) {
			showAlertMessage('', measurementWarningNeedGenExpDesign);
		}
	}
	return false;
}

function showListTreeToolTip(node, nodeSpan) {
	'use strict';
	$.ajax({
		url: '/Fieldbook/ListTreeManager/germplasm/list/header/details/' + node.data.key,
		type: 'GET',
		cache: false,
		success: function(data) {
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
			}).hover(function() {
				$('.popover').hide();
				$(this).popover('show');
			}, function() {
				$(this).popover('hide');
			});
			$('.popover').hide();
			$(nodeSpan).find('a.dynatree-title').popover('show');
		}
	});
}
function truncateStudyVariableNames(domSelector, charLimit) {
	'use strict';
	$(domSelector).each(function() {
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
	$('.variable-tooltip').each(function() {
		$(this).data('toggle', 'tooltip');
		if ($(this).data('placement') === undefined) {
			$(this).data('placement', 'right');
		}
		$(this).data('container', 'body');
		$(this).tooltip();
	});
}
function checkTraitsAndSelectionVariateTable(containerDiv, isLandingPage) {
	'use strict';
	if ($(containerDiv + ' .selection-variate-table tbody tr').length > 0) {
		$(containerDiv + ' .selection-variate-table').removeClass('fbk-hide');
	} else {
		$(containerDiv + ' .selection-variate-table').addClass('fbk-hide');
		if (isLandingPage) {
			$(containerDiv + ' .selection-variate-table').parent().prev().addClass('fbk-hide');
		}
	}
	if ($(containerDiv + ' .traits-table tbody tr').length > 0) {
		$(containerDiv + ' .traits-table').removeClass('fbk-hide');
	} else {
		$(containerDiv + ' .traits-table ').addClass('fbk-hide');
		if (isLandingPage) {
			$(containerDiv + ' .traits-table').parent().prev().addClass('fbk-hide');
		}
	}
}

function isValidInput(input) {
	'use strict';
	var invalidInput = /[<>&=%;?]/.test(input);
	return !invalidInput;
}

function doDeleteNursery(id, callback) {
	'use strict';
	$.ajax({
		url: '/Fieldbook/NurseryManager/deleteNursery/' + id,
		type: 'POST',
		cache: false,
		success: function(data) {
			callback(data);
		}
	});
}
function changeBrowseNurseryButtonBehavior(isEnable) {
	'use strict';
	if (isEnable) {
		$('.browse-nursery-action').removeClass('disable-image');
	} else {
		$('.browse-nursery-action').addClass('disable-image');
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
function showManageCheckTypePopup() {
	'use strict';
	$('#page-check-message-modal').html('');
	$('.check-germplasm-list-items .popover').remove();
	resetButtonsAndFields();
	$('#manageCheckTypesModal').modal({
		backdrop: 'static',
		keyboard: false
	});
}
function showExportGermplasmListPopup() {
	'use strict';
	$('.check-germplasm-list-items .popover').remove();
	$('#exportGermplasmListModal').modal({
		backdrop: 'static',
		keyboard: false
	});
	var visibleColumnTermIds = [];
	$('#imported-germplasm-list th[aria-label!=\'\']').each(
		function() {
			var termId = $(this).attr('data-col-name').split('-')[0];
			if ($.inArray(termId, visibleColumnTermIds) === -1) {
				visibleColumnTermIds.push(termId);
			}
		}
	);
	if (!isNursery() && $('#imported-germplasm-list').size() !== 0) {
		if ($.inArray(gidTermId + '', visibleColumnTermIds) === -1
			|| $.inArray(entryNoTermId + '', visibleColumnTermIds) === -1
			|| $.inArray(desigTermId + '', visibleColumnTermIds) === -1) {
			showAlertMessage('', requiredGermplasmColumnsMessage);
		}
	}
}
function addUpdateCheckType(operation) {
	'use strict';
	if (validateCheckFields()) {
		var $form = $('#manageCheckValue,#comboCheckCode');
		var serializedData = $form.serialize();
		$.ajax({
			url: '/Fieldbook/NurseryManager/importGermplasmList/addUpdateCheckType/'
					+ operation,
			type: 'POST',
			data: serializedData,
			cache: false,
			success: function(data) {
				if (data.success == '1') {
					// reload dropdown
					reloadCheckTypeList(data.checkTypes, operation);
					showCheckTypeMessage(data.successMessage);
					$('#comboCheckCode').select2('data', [{id:'', text:'', description:'' }]);
					$('#comboCheckCode').select2('val', '');
					$('#manageCheckValue').val('');
					$('#manageCheckTypesModal').modal('hide');
				} else {
					showCheckTypeErrorMessage(data.error);
				}
			}
		});
	}
}

function validateCheckFields() {
	'use strict';
	if (checkTypesObj.length === 0 && checkTypes != null) {
		$.each(checkTypes, function(index, item) {
			checkTypesObj.push({
				'id': item.id,
				'text': item.name,
				'description': item.description
			});
		});
	}

	if (!$('#comboCheckCode').select2('data')) {
		showInvalidInputMessage(codeRequiredError);
		return false;
	} else if ($('#manageCheckValue').val() === '') {
		showInvalidInputMessage(valueRequiredError);
		return false;
	} else if (!isValueUnique()) {
		showInvalidInputMessage(valueNotUniqueError);
		return false;
	}

	return true;
}

function isValueUnique() {
	'use strict';
	var isUnique = true;
	$.each(checkTypesObj, function(index, item) {
		if (item.description == $('#manageCheckValue').val()
				&& item.id != $('#comboCheckCode').select2('data').id) {
			isUnique = false;
			return false;
		}
	});
	return isUnique;
}
function resetButtonsAndFields() {
	'use strict';
	$('#manageCheckValue').val('');
	$('#comboCheckCode').select2('val', '');
	$('#updateCheckTypes').hide();
	$('#deleteCheckTypes').hide();
	$('#addCheckTypes').show();
}

function showCheckTypeErrorMessage(message) {
	'use strict';
	showErrorMessage('', message);
}

function showCheckTypeMessage(message) {
	'use strict';
	showSuccessfulMessage('', message);
}

function deleteCheckType() {
	'use strict';
	var isFound = false;
	if ($('manageCheckCode').select2('data')) {
		// we need to check here if it neing used in current
		if ($('.check-germplasm-list-items tbody tr').length != 0 && selectedCheckListDataTable !== null && selectedCheckListDataTable.getDataTable() !== null) {
			var currentId = $('#comboCheckCode').select2('data').id;
			selectedCheckListDataTable.getDataTable().$('.check-hidden').each(function() {
				if ($(this).val() == currentId) {
					isFound = true;
				}
			});
		}
		if (isFound) {
			showCheckTypeErrorMessage(checkTypeCurrentlyUseError);
			return false;
		}

		var $form = $('#manageCheckValue,#comboCheckCode');
		var serializedData = $form.serialize();
		$
				.ajax({
					url: '/Fieldbook/NurseryManager/importGermplasmList/deleteCheckType',
					type: 'POST',
					data: serializedData,
					cache: false,
					success: function(data) {
						if (data.success == '1') {
							reloadCheckTypeList(data.checkTypes, 3);
							showCheckTypeMessage(data.successMessage);
							resetButtonsAndFields();
							$('#comboCheckCode').select2('data', [{id:'', text:'', description:'' }]);
							$('#comboCheckCode').select2('val', '');
							$('#manageCheckValue').val('');
							$('#manageCheckTypesModal').modal('hide');
						} else {
							showCheckTypeErrorMessage(data.error);
						}
					}
				});
	} else {
		showCheckTypeErrorMessage(noCheckSelected);
	}
}

function reloadCheckTypeList(data, operation) {
	'use strict';
	var selectedValue = 0;

	checkTypesObj = [];

	if (data != null) {
		$.each($.parseJSON(data), function(index, value) {
			checkTypesObj.push({
				'id': value.id,
				'text': value.name,
				'description': value.description
			});
		});
	}

	if (operation == 2) {
		// update
		selectedValue = getIdOfValue($('#manageCheckValue').val());
	}

	$('#manageCheckValue').val('');
	initializeCheckTypeSelect2(null, [], false, 0, 'comboCheckCode');
	initializeCheckTypeSelect2(null, checkTypesObj, false, selectedValue,
			'comboCheckCode');
}

function getIdOfValue(value) {
	'use strict';
	var id = 0;
	$.each(checkTypesObj, function(index, item) {
		if (item.description == value) {
			id = item.id;
			return false;
		}
	});
	return id;
}

function reloadCheckTypeDropDown(addOnChange, select2ClassName) {
	'use strict';
	var currentCheckId = $('#checkId').val();
	$.ajax({
		url: '/Fieldbook/NurseryManager/importGermplasmList/getAllCheckTypes',
		type: 'GET',
		cache: false,
		data: '',
		success: function(data) {
			initializeCheckTypeSelect2($.parseJSON(data.allCheckTypes), [],
					addOnChange, currentCheckId, select2ClassName);
		}
	});
}

function initializeCheckTypeSelect2(suggestions, suggestions_obj, addOnChange,
		currentFieldId, comboName) {
	var defaultData = null;

	if (suggestions_obj.length === 0) {
		$
				.ajax({
					url: '/Fieldbook/NurseryManager/importGermplasmList/getAllCheckTypes',
					type: 'GET',
					cache: false,
					data: '',
					async: false,
					success: function(data) {
						checkTypes = $.parseJSON(data.allCheckTypes);
						suggestions = checkTypes;
					}
				});
	}

	if (suggestions != null) {
		$.each(suggestions, function(index, value) {
			if (comboName === 'comboCheckCode') {
				dataObj = {
					'id': value.id,
					'text': value.name,
					'description': value.description,
					'originalText': value.name
				};
			} else {
				dataObj = {
					'id': value.id,
					'text': value.description,
					'description': value.description,
					'originalText' : value.name
				};
			}
			suggestions_obj.push(dataObj);
		});
	} else {
		$.each(suggestions_obj, function(index, value) {
			if (currentFieldId != '' && currentFieldId == value.id) {
				defaultData = value;
			}
		});
	}
	// if combo to create is one of the ontology combos, add an onchange event
	// to populate the description based on the selected value
	if (comboName === 'comboCheckCode') {
		$('#' + comboName)
				.select2(
						{
							query: function(query) {
								var data = {
									results: sortByKey(suggestions_obj, 'text')
								};
								// return the array that matches
								data.results = $.grep(data.results, function(
										item, index) {
									if (item.text.toUpperCase().indexOf(query.term.toUpperCase()) === 0) {
										return true;
									}
									return false;
								});
								if (data.results.length === 0 || data.results[0].text.toUpperCase() != query.term.toUpperCase()) {
									data.results.unshift({
										id: query.term,
										text: query.term
									});
								}
								query.callback(data);
							},
							dropdownCssClass: 's2-nosearch-icon'
						})
				.on(
						'change',
						function() {
							if ($('#comboCheckCode').select2('data')) {
								if ($('#comboCheckCode').select2('data').id == $('#comboCheckCode').select2('data').text) {
									$('#manageCheckValue').val('');
									$('#updateCheckTypes').hide();
									$('#deleteCheckTypes').hide();
									$('#addCheckTypes').show();
								} else {
									$('#manageCheckValue').val($('#comboCheckCode').select2('data').description);
									$('#updateCheckTypes').show();
									$('#deleteCheckTypes').show();
									$('#addCheckTypes').hide();
								}
							}
						});
	} else {

		if ($('.check-table-popover tbody tr').length != 0) {
			reloadCheckListTable();
			//we need to get the real index of the check
		}
	}
}

function hideClearChecksButton() {
	if ($('.check-germplasm-list-items tbody tr').length === 0
		|| ($('#studyId') != null && $('#chooseGermplasmAndChecks').data('replace') !== undefined
					&& parseInt($('#chooseGermplasmAndChecks').data('replace')) === 0)) {
		$('#check-germplasm-list-reset-button').hide();
	}
}
function reloadCheckListTable() {
	'use strict';
	if (isNursery()) {
		$.ajax({
			url: '/Fieldbook/ListManager/GermplasmList/reload/check/list/N',
			type: 'GET',
			data: '',
			async: false,
			success: function(data) {
				$('#check-germplasm-list').html(data);
				hideClearChecksButton();
			}
		});
	} else {
		$.ajax({
			url: '/Fieldbook/ListManager/GermplasmList/refreshListDetails',
			type: 'GET',
			cache: false,
			data: ''
		}).success(function(html) {
			$('#imported-germplasm-list').html(html);
			$('#entries-details').css('display', 'block');
			$('#numberOfEntries').html($('#totalGermplasms').val());
		});
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
	if ($('#create-nursery #studyTree').length !== 0) {
		$('#studyTree').dynatree('destroy');
		displayStudyListTree('studyTree', 'N', type, selectStudyFunction, isPreSelect);
		changeBrowseNurseryButtonBehavior(false);
	} else if ($('#create-trial #studyTree').length !== 0) {
		$('#studyTree').dynatree('destroy');
		displayStudyListTree('studyTree', 'T', type, selectStudyFunction, isPreSelect);
		changeBrowseNurseryButtonBehavior(false);
	}

	$('#studyTreeModal').modal({
		backdrop: 'static',
		keyboard: true
	});
	$('#studyTreeModal').off('hide.bs.modal');
	$('#studyTreeModal').on('hide.bs.modal', function() {
			TreePersist.saveStudyTreeState(false, '#studyTree');
	});
	choosingType = type;
	if (isNursery()) {
		$('.fbk-study-tree-title.nursery').removeClass('fbk-hide');
	} else {
		$('.fbk-study-tree-title.trial').removeClass('fbk-hide');
	}
	TreePersist.preLoadStudyTreeState('#studyTree');
}

function isNursery() {
	'use strict';
	if ($('#check-germplasm-list').length != 0 || $('.nursery-header').length != 0) {
		return true;
	} else {
		return false;
	}
}

function isOpenTrial() {
	'use strict';
	var trialStatus = $('body').data('trialStatus');
	return (trialStatus && trialStatus === 'OPEN');
}

function addStudyTreeHighlight(node) {
	$(node.span).addClass('fbtree-focused');
}

function initializeStudyTabs() {
	'use strict';
	$('.nav-tabs').tabdrop({position: 'left'});
	$('.nav-tabs').tabdrop('layout');
	$('#study-tab-headers .fbk-close-tab').on('click', function() {
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
function addDetailsTab(studyId, title) {
	// if the study is already existing, we show that tab
	'use strict';

	if ($('li#li-study' + studyId).length !== 0) {
		$('li#li-study' + studyId + ' a').tab('show');
	} else {
		var studyType = isNursery() ? 'N' : 'T';
		$.ajax({
			url: '/Fieldbook/StudyManager/reviewStudyDetails/show/' + studyType + '/' + studyId,
			type: 'GET',
			cache: false,
			success: function(data) {
				var close = '<i class="glyphicon glyphicon-remove fbk-close-tab" id="' + studyId + '"></i>';
				$('#study-tab-headers').append(
						'<li id="li-study' + studyId + '"><a href="#study' + studyId + '" role="tab" data-toggle="tab"><span class="review-study-name">'
								+ title + '</span>' + close + '</a></li>');
				$('#study-tabs').append(
					'<div class="info tab-pane" id="study' + studyId + '">' + data + '</div>');
				if ($('#review-study-error-' + studyId).val() !== '') {
					createErrorNotification(errorMsgHeader, $('#review-study-error-' + studyId).val());
					$('#study-tab-headers li#li-study' + studyId).remove();
					$('#study-tabs div#study' + studyId).remove();
				} else {
					initializeStudyTabs();
					$('li#li-study' + studyId + ' a').tab('show');
					$('.info#study' + studyId + ' select').each(function() {
						$(this).select2({minimumResultsForSearch: 20});
					});
					truncateStudyVariableNames('#study' + studyId + ' .review-study-name', 20);
					reviewLandingSetup();
				}
			}
		});
	}
	determineIfShowCloseAllStudyTabs();
	// if not we get the info
}

function determineIfShowCloseAllStudyTabs() {
	'use strict';
	if ($('#study-tab-headers li').length > 1) {
		$('.review-nursery-details').removeClass('fbk-hide');
	} else {
		$('.review-nursery-details').addClass('fbk-hide');
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
		success: function(data) {
			var i = 0;
			for (i = 0; i < data.length; i++) {
				optionTag.append(new Option(data[i].name, data[i].id));
			}
			$('#study' + getCurrentStudyIdInTab() + ' #dataset-selection').val('');
		},
		error: function(jqXHR, textStatus, errorThrown) {
			console.log('The following error occured: ' + textStatus,
					errorThrown);
		},
		complete: function() {
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
	var currentStudyId = getCurrentStudyIdInTab(),
		studyType = isNursery() ? 'N' : 'T';
	if (datasetId == 'Please Choose'
			|| $('#' + getJquerySafeId('dset-tab-') + datasetId).length !== 0) {
		return;
	}
	$.ajax({
		url: '/Fieldbook/NurseryManager/addOrRemoveTraits/viewStudyAjax/' + studyType + '/' + datasetId,
		type: 'GET',
		cache: false,
		success: function(html) {
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

function showSelectedTab(selectedTabName) {
	'use strict';
	$('#ontology-tab-headers').show();
	var tabs = $('#ontology-tabs').children();
	for (var i = 0; i < tabs.length; i++) {
		if (tabs[i].id === selectedTabName) {
			$('#' + tabs[i].id + '-li').addClass('active');
			$('#' + tabs[i].id).show();
			if (selectedTabName === 'ontology-usage-tab' && parseInt($('#ontology-usage-tab').data('usageloaded')) === 0) {
				getUsageDetails();
				$('#ontology-usage-tab').data('usageloaded', '1');
			}
		} else {
			$('#' + tabs[i].id + '-li').removeClass('active');
			$('#' + tabs[i].id).hide();
		}
	}
}

function getUsageDetails() {
	var id = $('#ontology-tabs').data('selectedvariableid');
	var variableTypeId = $('#ontology-tabs').data('selectedvariabletypeid');
	$('#ontology-usage-tab').html('');
	$.ajax({
		url: '/Fieldbook/manageSettings/settings/details/usage/' + variableTypeId + '/' + id,
		type: 'GET',
		async: true,
		success: function(html) {
			$('#ontology-usage-tab').html(html);
		},
		error: function(jqXHR, textStatus, errorThrown) {
			console.log('The following error occured: ' + textStatus, errorThrown);
		},
		complete: function() {
		}
	});
}

function showSelectedTabNursery(selectedTabName) {
	'use strict';
	if ($('.import-study-data').data('data-import') === '1') {
		showAlertMessage('', importSaveDataWarningMessage);
		return;
	}
	
	if (stockListImportNotSaved) {
		showAlertMessage('', importSaveDataWarningMessage);
		e.preventDefault();
	}

	$('#create-nursery-tab-headers').show();
	var tabs = $('#create-nursery-tabs').children();
	for (var i = 0; i < tabs.length; i++) {
		if (tabs[i].id == selectedTabName) {
			$('#' + tabs[i].id + '-li').addClass('active');
			$('#' + tabs[i].id).show();
		} else {
			$('#' + tabs[i].id + '-li').removeClass('active');
			$('#' + tabs[i].id).hide();
		}
	}

	if (selectedTabName === 'nursery-measurements' || selectedTabName === 'trial-measurements') {
		var dataTable = $('#measurement-table').dataTable();
		if (dataTable.length !== 0) {
			dataTable.fnAdjustColumnSizing();
		}
	}

}

function showStudyInfo() {
	$('#folderBrowserModal').modal('show');
}

function initializeReviewDatasetTabs(datasetId) {
	'use strict';
	$('#dataset-li' + datasetId).on('click', function() {
		$('#study' + getCurrentStudyIdInTab() + ' #dataset-selection option:selected').prop('selected', false);
		$('#study' + getCurrentStudyIdInTab() + ' #dataset-selection option').each(function(index) {
			if ($(this).val() === datasetId) {
				$(this).prop('selected', true);
			}
		});
		$('#study' + getCurrentStudyIdInTab() + ' #dataset-selection').change();
	});

	$('#dataset-li' + datasetId + ' .fbk-close-dataset-tab').on('click', function() {
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

function displayEditFactorsAndGermplasmSection() {
	'use strict';
	if ($('#measurementDataExisting').length !== 0) {
		displayCorrespondingGermplasmSections();

		//enable/disable adding of factors if nursery has measurement data
		if ($('#measurementDataExisting').val() === 'true') {
			$('.chs-add-variable-factor').hide();
			$.each($('#plotLevelSettings tbody tr'), function(index, row) {
				$(row).find('.delete-icon').hide();
			});
		} else {
			$('.chs-add-variable-factor').show();
			$.each($('#plotLevelSettings tbody tr'), function(index, row) {
				$(row).find('.delete-icon').show();
			});
		}
	} else {
		displayCorrespondingGermplasmSections();
		if ($('#measurementDataExisting').val() === 'true') {
			$('.chs-add-variable-factor').hide();
		} else {
			$('.chs-add-variable-factor').show();
		}
	}
}

// Function to enable/disable & show/hide controls as per Clear list button's visibility
function toggleControlsForGermplasmListManagement(value) {
    if(value) {
        $('#imported-germplasm-list-reset-button').show();
        $('#txtStartingEntryNo').prop('title', '');
        $('#txtStartingPlotNo').prop('title', '');
    } else {
        $('#imported-germplasm-list-reset-button').hide();
        if (isNursery()) {
            $('#txtStartingEntryNo').prop('title', 'Click Replace button to edit entry number');
            $('#txtStartingPlotNo').prop('title', 'Click Replace button to edit plot number');
        } else {
            $('#txtStartingEntryNo').prop('title', 'Click Modify List button to edit entry number');
        }
    }

    $('#txtStartingEntryNo').prop('disabled', !value);
    $('#txtStartingPlotNo').prop('disabled', !value);
}

function showGermplasmDetailsSection() {
	'use strict';
	$('.observation-exists-notif').hide();
	$('.overwrite-germplasm-list').hide();
	$('.browse-import-link').show();
	if ($('.germplasm-list-items tbody tr').length > 0) {
        toggleControlsForGermplasmListManagement(true);
	}
	//flag to determine if existing measurements should be deleted
	$('#chooseGermplasmAndChecks').data('replace', '1');
	if (isNursery()) {
		//enable drag and drop
		makeDraggable(true);
		makeCheckDraggable(true);
		if ($('.check-germplasm-list-items tbody tr').length > 0) {
			//show clear button and specify checks section
			$('#check-germplasm-list-reset-button').show();
			$('#specifyCheckSection').show();
			//enable deletion of checks
			if (selectedCheckListDataTable !== null && selectedCheckListDataTable.getDataTable() !== null) {
				selectedCheckListDataTable.getDataTable().$('.delete-check').show();
			}
		}
		disableCheckVariables(false);
	}
}

function displayCorrespondingGermplasmSections() {
	'use strict';
	var hasData = $('#measurementDataExisting').val() === 'true' ? true : false;
	displayStudyGermplasmSection(hasData, measurementRowCount);
}

function hasMeasurementData() {
	'use strict';

	if (isNursery()) {
		return $('#measurementDataExisting').val() === 'true' ? true : false;
	} else {
		return angular.element('#mainApp').injector().get('TrialManagerDataService').trialMeasurement.hasMeasurement;
	}
}

function displayStudyGermplasmSection(hasData, observationCount) {
	'use strict';
	if (hasData) {
		$('.overwrite-germplasm-list').hide();
		$('.observation-exists-notif').show();
		$('.browse-import-link').hide();
		if (isNursery()) {
			disableCheckVariables(true);
		}
	} else if (observationCount > 0) {
		$('.observation-exists-notif').hide();
		$('.overwrite-germplasm-list').show();
		$('#imported-germplasm-list').show();
		$('.browse-import-link').hide();
		if (isNursery()) {
			disableCheckVariables(true);
		}
	} else {
		$('.observation-exists-notif').hide();
		$('.overwrite-germplasm-list').hide();
	}
}

function disableCheckVariables(isDisabled) {

	$('#' + getJquerySafeId('checkVariables2.value')).select2('destroy');
	$('#' + getJquerySafeId('checkVariables2.value')).prop('disabled', isDisabled);
	$('#' + getJquerySafeId('checkVariables2.value')).select2();
	$('#specifyCheckSection').find('input,select').prop('disabled', isDisabled);

}

function showMeasurementsPreview() {
	'use strict';
	var domElemId = '#measurementsDiv';
	$.ajax({
		url: '/Fieldbook/TrialManager/openTrial/load/preview/measurement',
		type: 'GET',
		data: '',
		cache: false,
		success: function(html) {
			$(domElemId).html(html);
			$('body').data('expDesignShowPreview', '0');
		}
	});
}

function loadInitialMeasurements() {
	'use strict';
	var domElemId = '#measurementsDiv';
	$.ajax({
		url: '/Fieldbook/TrialManager/openTrial/load/measurement',
		type: 'GET',
		data: '',
		cache: false,
		success: function(html) {
			$(domElemId).html(html);
			$('body').data('expDesignShowPreview', '0');
		}
	});
}

function displaySelectedCheckGermplasmDetails() {
	$.ajax({
		url: '/Fieldbook/NurseryManager/importGermplasmList/displaySelectedCheckGermplasmDetails',
		type: 'GET',
		cache: false,
		async: false,
		success: function(html) {
			$('#check-germplasm-list').html(html);
			setSpinnerMaxValue();
			itemsIndexAdded = [];
			$('#check-details').removeClass('fbk-hide');
			//hide clear button, set list id used fror checks if from list, and set checksFromPrimary value based on checks
			$('#check-germplasm-list-reset-button').hide();
			lastDraggedChecksList = $('#lastDraggedChecksList').val();
			if (lastDraggedChecksList.toString() === '' || lastDraggedChecksList.toString() === '0') {
				checksFromPrimary = $('.check-germplasm-list-items tbody tr').length;
			} else {
				checksFromPrimary = 0;
			}
		}
	});
}

function displaySelectedGermplasmDetails() {
	var url = '/Fieldbook/NurseryManager/importGermplasmList/displaySelectedGermplasmDetails';
	if (isNursery()) {
		url = url + '/N';
	} else {
		url = url + '/T';
	}
	$.ajax({
		url: url,
		type: 'GET',
		data: '',
		cache: false,
		async: false,
		success: function(html) {
			$('#imported-germplasm-list').css('display', 'block');
			$('#imported-germplasm-list').html(html);
			if (parseInt($('#totalGermplasms').val()) !== 0) {
				$('#entries-details').css('display', 'block');
			}
			$('#numberOfEntries').html($('#totalGermplasms').val());
			$('#imported-germplasm-list-reset-button').css('opacity', '1');
			if (isNursery()) {
				itemsIndexAdded = [];
				setSpinnerMaxValue();
				makeDraggable(false);
				lastDraggedPrimaryList = $('#lastDraggedPrimaryList').val();
			} else {
				$(document).trigger('germplasmListUpdated');
			}
			listId = $('#lastDraggedPrimaryList').val();
			if (listId === '') {
				$('.view-header').hide();
                // Hide Numbering section if germplasm list is not available
                $('#specify-numbering-section').hide();
			} else {
				$('.view-header').show();
			}
            toggleControlsForGermplasmListManagement(false);
		}
	});
}
function showAddEnvironmentsDialog() {
	'use strict';
	$('#numberOfEnvironments').val('');
	$('#addEnvironmentsModal').modal({ backdrop: 'static', keyboard: true });
}

function checkBeforeAdvanceExport() {
	'use strict';
	var checkedAdvancedLists = getExportCheckedAdvancedList(),
	counter = 0,
	additionalAdvanceExportParams = '';

	if (checkedAdvancedLists !== null && checkedAdvancedLists.length === 0) {
		showErrorMessage('', 'Please select at least 1 advance list');
		return false;
	}

	if (checkedAdvancedLists !== null && checkedAdvancedLists.length !== 0) {

		for (counter = 0 ; counter < checkedAdvancedLists.length ; counter++) {
			if (additionalAdvanceExportParams !== '') {
				additionalAdvanceExportParams += '|';
			}
			additionalAdvanceExportParams += checkedAdvancedLists[counter];
		}
	}
	exportAdvanceStudyList(additionalAdvanceExportParams);
}

function showExportAdvanceOptions() {
	'use strict';
	var studyId = $('#studyId').val();
	$.ajax({
		url: '/Fieldbook/ExportManager/retrieve/advanced/lists/' + studyId,
		type: 'GET',
		cache: false,
		success: function(data) {
			$('.export-advance-germplasm-list .advances-list').html(data);
			$('.export-advance-germplasm-list').removeClass('fbk-hide');
		}
	});
	$('#exportAdvancedType').select2('destroy');
	$('#exportAdvancedType').val('1');
	$('#exportAdvanceListModal select').select2({width: 'copy', minimumResultsForSearch: 20});
	$('#exportAdvanceListModal').modal({ backdrop: 'static', keyboard: true });

}
function showExportAdvanceResponse(responseText, statusText, xhr, $form) {
	'use strict';
	var resp = $.parseJSON(responseText);
	$('#exportAdvanceStudyDownloadForm #outputFilename').val(resp.outputFilename);
	$('#exportAdvanceStudyDownloadForm #filename').val(resp.filename);
	$('#exportAdvanceStudyDownloadForm #contentType').val(resp.contentType);
	$('#exportAdvanceStudyDownloadForm').submit();
	$('#exportAdvanceListModal').modal('hide');
}
function processInlineEditInput() {
	'use strict';
	if ($('.inline-input').length !== 0) {
		var indexElem = $('.data-hidden-value-index').val();
		var indexTermId = $('.data-hidden-value-term-id').val();
		var indexDataVal = '';
		var isNew = '0';
		if ($('.data-value').hasClass('variates-select')) {
			if ($('.data-value').select2('data') != null) {
				indexDataVal = $('.data-value').select2('data').id;
				isNew  = $('.data-value').select2('data').status;
			} else {
				indexDataVal = '';
			}
		} else if ($('.data-value').hasClass('numeric-value')) {
			var minVal = ($('.data-value').data('min-range'));
			var maxVal = ($('.data-value').data('max-range'));
			var cellText = $('.data-value').val();
			if ($.trim(cellText.toLowerCase()) == 'missing') {
				if (minVal != null && maxVal != null) {
					isNew = '1';
				}else {
					isNew = '0';
				}
				$('.data-value').val('missing');
			}else if (minVal != null && maxVal != null && (parseFloat(minVal) > parseFloat(cellText) || parseFloat(cellText) > parseFloat(maxVal))) {
				isNew = '1';
			}
			indexDataVal =  $('.data-value').val();
		} else {
			indexDataVal =  $('.data-value').val();
		}

		var currentInlineEdit = {'index': indexElem, 'termId': indexTermId, 'value': indexDataVal, 'isNew': isNew};
		$('#measurement-table').data('json-inline-edit-val', JSON.stringify(currentInlineEdit));
		if (isNew === '1') {
			$('#inlineEditConfirmationModal').modal({
				backdrop: 'static',
				keyboard: true
			});
			$('#measurement-table').data('show-inline-edit', '0');
			return false;
		}else {
			saveInlineEdit(0);
		}
	}
	return true;
}
function saveInlineEdit(isDiscard) {
	'use strict';

	$.ajax({
		url: '/Fieldbook/Common/addOrRemoveTraits/update/experiment/cell/data?isDiscard=' + isDiscard,
		type: 'POST',
		async: false,
		data:   $('#measurement-table').data('json-inline-edit-val'),
		contentType: 'application/json',
		success: function(data) {
			var jsonData = $.parseJSON($('#measurement-table').data('json-inline-edit-val'));
			if (isDiscard == 0 && jsonData.isNew === '1' && jsonData.value !== 'missing') {
				$('.inline-input').parent('td').addClass('accepted-value').removeClass('invalid-value');
				$('.inline-input').parent('td').data('is-accepted', '1');
			}else if (jsonData.isNew == '0') {
				$('.inline-input').parent('td').removeClass('accepted-value').removeClass('invalid-value');
				$('.inline-input').parent('td').data('is-accepted', '0');
			}
			if (data.success === '1') {
				$('.inline-input').parent('td').data('is-inline-edit', '0');

				var oTable = $('#measurement-table').dataTable();
				oTable.fnUpdate(data.data, data.index, null, false); // Row
				oTable.fnAdjustColumnSizing();
				$('body').off('click');
			} else {
				$('#measurement-table').data('show-inline-edit', '0');
				showErrorMessage('page-update-experiment-message-modal', data.errorMessage);
			}
		}
	});
}
function markCellAsMissing(indexElem, indexTermId, indexDataVal, isNew, elem) {
	'use strict';
	var data = {
		'index':indexElem,
		'termId':indexTermId,
		'value':indexDataVal,
		'isNew': isNew
	};

	$.ajax({
		headers: {
			'Accept': 'application/json',
			'Content-Type': 'application/json'
		},
		url: '/Fieldbook/Common/addOrRemoveTraits/update/experiment/cell/data?isDiscard=0',
		type: 'POST',
		async: false,
		data:   JSON.stringify(data),
		contentType: 'application/json',
		success: function(data) {
			if (data.success === '1') {
				var oTable = $('#measurement-table').dataTable();
				oTable.fnUpdate(data.data, data.index, null, false); // Row
				$(elem).removeClass('invalid-value');
			} else {
				showErrorMessage('page-update-experiment-message-modal', data.errorMessage);
			}
		}
	});
}
function markCellAsAccepted(indexElem, indexTermId, elem) {
	'use strict';

	var data = {
		'index':indexElem,
		'termId':indexTermId
	};

	$.ajax({
		headers: {
			'Accept': 'application/json',
			'Content-Type': 'application/json'
		},
		url: '/Fieldbook/Common/addOrRemoveTraits/update/experiment/cell/accepted',
		type: 'POST',
		async: false,
		data:   JSON.stringify(data),
		contentType: 'application/json',
		success: function(data) {
			if (data.success === '1') {
				var oTable = $('#measurement-table').dataTable();
				oTable.fnUpdate(data.data, data.index, null, false); // Row
				$(elem).removeClass('invalid-value');
				$(elem).addClass('accepted-value');
			} else {
				showErrorMessage('page-update-experiment-message-modal', data.errorMessage);
				$('#measurement-table').data('show-inline-edit', '0');
			}
		}
	});
}
function markAllCellAsAccepted() {
	'use strict';

	$.ajax({
		url: '/Fieldbook/Common/addOrRemoveTraits/update/experiment/cell/accepted/all',
		type: 'GET',
		async: false,
		contentType: 'application/json',
		success: function(data) {
			if (data.success === '1') {
				reloadMeasurementTable();
				$('#reviewOutOfBoundsDataModal').modal('hide');
			} else {
				showErrorMessage('page-review-out-of-bounds-data-message-modal', data.errorMessage);
			}
		}
	});
}
function markAllCellAsMissing() {
	'use strict';

	$.ajax({
		url: '/Fieldbook/Common/addOrRemoveTraits/update/experiment/cell/missing/all',
		type: 'GET',
		async: false,
		contentType: 'application/json',
		success: function(data) {
			if (data.success === '1') {
				reloadMeasurementTable();
				$('#reviewOutOfBoundsDataModal').modal('hide');
			} else {
				showErrorMessage('page-review-out-of-bounds-data-message-modal', data.errorMessage);
			}
		}
	});
}
function reloadMeasurementTable() {
	'use strict';
	if ($('#measurement-table').length !== 0) {
		$.ajax({
			url: '/Fieldbook/ImportManager/import/preview',
			type: 'POST',
			success: function(html) {
				$('#measurementsDiv').html(html);
				$('.import-study-data').data('data-import', '1');
			}
		});
	}
}
function hasMeasurementsInvalidValue() {
	'use strict';
	if ($('#measurement-table').dataTable().$('.invalid-value').length === 0) {
		return false;
	}
	return true;
}

function reviewOutOfBoundsData() {
	'use strict';
	$('#reviewOutOfBoundsDataModal').modal({ backdrop: 'static', keyboard: true });
}

function displayDetailsOutOfBoundsData() {
	'use strict';

	removeDetailsOutOfBoundDataInSessionStorage();

	if ($('#reviewDetailsOutOfBoundsDataModalBody').length !== 0) {
		$.ajax({
			url: '/Fieldbook/Common/ReviewDetailsOutOfBounds/showDetails',
			type: 'GET',
			success: function(html) {
				$('#reviewOutOfBoundsDataModal').modal('hide');
				$('#reviewDetailsOutOfBoundsDataModalBody').html(html);
				$('#reviewDetailsOutOfBoundsDataModal').one('shown.bs.modal', function() {
					$('body').addClass('modal-open');
				}).modal({
					backdrop: 'static',
					keyboard: true
				});
			}
		});
	}
}

function removeDetailsOutOfBoundDataInSessionStorage() {
	'use strict';
	if (sessionStorage) {
		for (var i in sessionStorage)
		{
			if (i.indexOf('reviewDetailsFormData') === 0) {
				sessionStorage.removeItem(i);
			}
		}
	}

}

function proceedToReviewOutOfBoundsDataAction() {
	var action = $('#review-out-of-bounds-data-action').select2('data').id;
	if (action === '0') {
		showErrorMessage('page-review-out-of-bounds-data-message-modal', reviewOutOfBoundsDataActionRequiredError);
	} else if (action === '1') {
		displayDetailsOutOfBoundsData();
	} else if (action === '2') {
		markAllCellAsAccepted();
	} else if (action === '3') {
		markAllCellAsMissing();
	}
}

function exportDesignTemplate() {
	$.ajax({
		url: '/Fieldbook/DesignTemplate/export',
		type: 'GET',
		cache: false,
		success: function(result) {
			if(result.isSuccess === 1){
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