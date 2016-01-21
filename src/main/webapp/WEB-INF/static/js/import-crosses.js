var ImportCrosses = {
		CROSSES_URL : '/Fieldbook/crosses',
		showFavoriteMethodsOnly: true,
		showFavoriteLocationsOnly: true,
		preservePlotDuplicates: false,
		showPopup : function(){
			$('#fileupload-import-crosses').val('');
			$('.import-crosses-section .modal').one('shown.bs.modal', function() {
				$('body').addClass('modal-open');
			}).modal({ backdrop: 'static', keyboard: true });
			$('.import-crosses-section .modal .fileupload-exists').click();
			ImportCrosses.showFavoriteMethodsOnly = true;
			ImportCrosses.showFavoriteLocationsOnly = true;
		},

		doSubmitImport : function() {
			'use strict';

			if ($('#fileupload-import-crosses').val() === '') {
				showErrorMessage('', 'Please choose a file to import');
				return false;
			}

			ImportCrosses.submitImport($('.import-crosses-section')).done(function(resp) {

				if (!resp.isSuccess) {
					createErrorNotification(crossingImportErrorHeader,resp.error.join('<br/>'));
					return;
				}
				ImportCrosses.preservePlotDuplicates = false;
				$('.import-crosses-section .modal').modal('hide');
				$('#openCrossesListModal').data('hasPlotDuplicate', resp.hasPlotDuplicate);
				// show review crosses page
				setTimeout(ImportCrosses.openCrossesList, 500);

			});

		},
		hasPlotDuplicate : function(){
			'use strict';
			if($('#openCrossesListModal').data('hasPlotDuplicate') === true){
				return true;
			}
			return false;
		},
		openCrossesList : function() {
			'use strict';
			$('#openCrossesListModal').one('shown.bs.modal', function() {
				$('body').addClass('modal-open');
			}).modal({ backdrop: 'static', keyboard: true });

			ImportCrosses.getImportedCrossesTable().done(function(response) {
				setTimeout(function() {
					new  BMS.Fieldbook.PreviewCrossesDataTable('#preview-crosses-table', response);
				},240);
			});

			$('#openCrossListNextButton').off('click');
			$('#openCrossListNextButton').on('click', function() {
				$('#openCrossesListModal').modal('hide');
				setTimeout(ImportCrosses.showPlotDuplicateConfirmation, 500);
			});

			$('#goBackToImportCrossesButton').off('click');
			$('#goBackToImportCrossesButton').on('click', function() {
				ImportCrosses.goBackToPage('#openCrossesListModal','.import-crosses-section .modal');
			});

		},

		goBackToPage: function(hiddenModalSelector,shownModalSelector) {
			$(hiddenModalSelector).modal('hide');
			$(shownModalSelector).one('shown.bs.modal', function() {
				$('body').addClass('modal-open');
			}).modal({ backdrop: 'static', keyboard: true });
		},

		getImportedCrossesTable : function(){
			'use strict';
			return $.ajax(
			{
				url: ImportCrosses.CROSSES_URL + '/getImportedCrossesList',
				type: 'GET',
				cache: false
			});
		},

		submitImport : function($importCrossesForm) {
			'use strict';
			var deferred = $.Deferred();
			$importCrossesForm.ajaxForm({
				dataType: 'json',
				success: function(response) {
					deferred.resolve(response);
				},
				error: function(response) {

                    createErrorNotification(crossingImportErrorHeader,invalidImportedFile);

                    deferred.reject(response);
				}
			}).submit();

			return deferred.promise();
		},
		displayCrossesGermplasmDetails: function (listId) {
			'use strict';
			$.ajax({
				url: '/Fieldbook/germplasm/list/crosses/' + listId,
				type: 'GET',
				cache: false,
				success: function(html) {
					$('.crosses-list' + getCurrentAdvanceTabTempIdentifier()).html(html);
				}
			});
		},
		showPlotDuplicateConfirmation : function(){
			'use strict';
			if(ImportCrosses.hasPlotDuplicate()){
				//show the confirmation now
				$('#duplicate-crosses-modal input[type=checkbox]').prop('checked',ImportCrosses.preservePlotDuplicates);
				
				$('#duplicate-crosses-modal').one('shown.bs.modal', function() {
					$('body').addClass('modal-open');
				}).modal({ backdrop: 'static', keyboard: true });
				
				$('#continue-duplicate-crosses').off('click');
				$('#continue-duplicate-crosses').on('click', function() {
					//get the value of the checkbox
					ImportCrosses.preservePlotDuplicates = $('#duplicate-crosses-modal input[type=checkbox]').is(':checked');
					$('#duplicate-crosses-modal').modal('hide');				
					setTimeout(ImportCrosses.showImportSettingsPopup, 500);
				});
			}else{
				ImportCrosses.showImportSettingsPopup();
			}			
		},
		showImportSettingsPopup : function() {
			var crossSettingsPopupModal = $('#crossSettingsModal');
			crossSettingsPopupModal.one('shown.bs.modal', function() {
				$('body').addClass('modal-open');
			}).modal({ backdrop: 'static', keyboard: true });

			BreedingMethodsFunctions.processMethodDropdownAndFavoritesCheckbox('breedingMethodDropdown', 'showFavoritesOnlyCheckbox', ImportCrosses.showFavoriteMethodsOnly);
			LocationsFunctions.processLocationDropdownAndFavoritesCheckbox('locationDropdown', 'locationFavoritesOnlyCheckbox', ImportCrosses.showFavoriteLoationsOnly);
			ImportCrosses.processImportSettingsDropdown('presetSettingsDropdown', 'loadSettingsCheckbox');
			ImportCrosses.updateSampleParentageDesignation();
			
			$('.cross-import-name-setting').off('change');
			$('.cross-import-name-setting').on('change', ImportCrosses.updateDisplayedSequenceNameValue);

			$('#parentageDesignationSeparator').off('change');
			$('#parentageDesignationSeparator').on('change', ImportCrosses.updateSampleParentageDesignation);

			ImportCrosses.populateHarvestMonthDropdown('harvestMonthDropdown');
			ImportCrosses.populateHarvestYearDropdown('harvestYearDropdown');

			$('#settingsNextButton').click(ImportCrosses.submitCrossImportSettings);

			$('#goBackToOpenCrossesButton').off('click');
			$('#goBackToOpenCrossesButton').on('click', function() {
				ImportCrosses.showFavoriteMethodsOnly = $('#showFavoritesOnlyCheckbox').is(':checked');
				ImportCrosses.showFavoriteLoationsOnly = $('#locationFavoritesOnlyCheckbox').is(':checked');
				ImportCrosses.goBackToPage('#crossSettingsModal','#openCrossesListModal');
			});
		},
		
		validateStartingSequenceNumber : function(value) {
			if(value != null && value != '' && (value.indexOf('.') >= 0 || !isInt(value))){
				createErrorNotification(invalidInputMsgHeader, invalidStartingNumberErrorMsg);
				return false;
			}
			return true;
		},
		
		updateSampleParentageDesignation : function() {
			var value = $('#parentageDesignationSeparator').val();
			$('#sampleParentageDesignation').text('ABC-123' + value + 'DEF-456');
		},

		processImportSettingsDropdown : function(dropdownID, useSettingsCheckboxID) {

			ImportCrosses.retrieveAvailableImportSettings().done(function(settingList) {
				ImportCrosses.createAvailableImportSettingsDropdown(dropdownID, settingList);

				$('#' + getJquerySafeId(dropdownID)).on('change', function() {
					ImportCrosses.triggerImportSettingUpdate(settingList, dropdownID, useSettingsCheckboxID);
					// update the displayed sequence name value so that it makes use of the possibly new settings
					ImportCrosses.updateDisplayedSequenceNameValue();
					ImportCrosses.updateSampleParentageDesignation();
				});

				$('#' + getJquerySafeId(useSettingsCheckboxID)).on('change', function() {
					ImportCrosses.triggerImportSettingUpdate(settingList, dropdownID, useSettingsCheckboxID);

					// update the displayed sequence name value so that it makes use of the possibly new settings
					ImportCrosses.updateDisplayedSequenceNameValue();
					ImportCrosses.updateSampleParentageDesignation();
				});
			}).fail(function(data) {
			});
		},

		triggerImportSettingUpdate : function(settingList, dropdownID, useSettingsCheckboxID) {
			if ($('#' + getJquerySafeId(useSettingsCheckboxID)).is(':checked')) {
				var currentSelectedItem = $('#' + dropdownID).select2('val');

				$.each(settingList, function (index, setting) {
					if (setting.name === currentSelectedItem) {
						ImportCrosses.updateImportSettingsFromSavedSetting(setting);
					}
				});
			}
		},

		updateImportSettingsFromSavedSetting : function(setting) {

			$('#presetName').val(setting.name);
			$('#breedingMethodDropdown').select2('val', setting.breedingMethodID);
			$('#useSelectedMethodCheckbox').prop('checked', !setting.basedOnStatusOfParentalLines);

			$('#crossPrefix').val(setting.crossPrefix);
			$('#crossSuffix').val(setting.crossSuffix);
			$('input:radio[name=hasPrefixSpace][value=' + setting.hasPrefixSpace + ']').prop('checked', true);
			$('input:radio[name=hasSuffixSpace][value=' + setting.hasSuffixSpace + ']').prop('checked', true);
			$('input:radio[name=hasParentageDesignationName][value=' + setting.hasParentageDesignationName + ']').prop('checked', true);
			$('#sequenceNumberDigits').select2('val', setting.sequenceNumberDigits);
			$('#parentageDesignationSeparator').val(setting.parentageDesignationSeparator);
			$('#startingSequenceNumber').val(setting.startingSequenceNumber);
			$('#locationDropdown').select2('val', setting.locationID);
		},

		openBreedingMethodsModal: function () {
			var crossSettingsPopupModal = $('#crossSettingsModal');
			crossSettingsPopupModal.modal('hide');
			crossSettingsPopupModal.data('open', '1');

			BreedingMethodsFunctions.openMethodsModal();
		},

		openLocationsModal: function () {
			var crossSettingsPopupModal = $('#crossSettingsModal');
			crossSettingsPopupModal.modal('hide');
			crossSettingsPopupModal.data('open', '1');

			LocationsFunctions.openLocationsModal();
		},

		createAvailableImportSettingsDropdown : function(dropdownID, settingList) {
			var possibleValues = [];
			$.each(settingList, function(index, setting) {
				possibleValues.push(ImportCrosses.convertSettingToSelect2Item(setting));
			});

			$('#' + getJquerySafeId(dropdownID)).select2(
				{
					initSelection: function (element, callback) {
						$.each(possibleValues, function (index, value) {
							if (value.id == element.val()) {
								callback(value);
							}
						});
					},
					query: function (query) {
						var data = {
							results: possibleValues
						};
						// return the array that matches
						data.results = $.grep(data.results, function (item) {
							return ($.fn.select2.defaults.matcher(query.term,
								item.text));

						});
						query.callback(data);
					}
				});
		},

		convertSettingToSelect2Item : function(setting) {
			return {
				'id': setting.name,
				'text': setting.name,
				'description': setting.name
			};
		},

		retrieveAvailableImportSettings : function() {
			return $.ajax({
				url: ImportCrosses.CROSSES_URL + '/retrieveSettings',
				type: 'GET',
				cache: false
			});
		},

		submitCrossImportSettings : function() {
			var settingData = ImportCrosses.constructSettingsObjectFromForm();

			if (ImportCrosses.isCrossImportSettingsValid(settingData)) {
				var targetURL;
				if ($('#presetName').val().trim() !== '') {
					targetURL = ImportCrosses.CROSSES_URL + '/submitAndSaveSetting';
				} else {
					targetURL = ImportCrosses.CROSSES_URL + '/submit';
				}

				$.ajax({
					headers: {
						'Accept': 'application/json',
						'Content-Type': 'application/json'
					},
					url: targetURL,
					type: 'POST',
					cache: false,
					data: JSON.stringify(settingData),
					success: function (data) {
						if (data.success == '0') {
							showErrorMessage('', 'Import failed');
						} else {
							$('#crossSettingsModal').modal('hide');
							ImportCrosses.openSaveListModal();
						}
					}
				});
			}

		},

		isCrossImportSettingsValid : function(importSettings) {
			var valid = true;
			if (!importSettings.crossNameSetting.prefix || importSettings.crossNameSetting.prefix === '') {
				valid = false;
				showErrorMessage('', 'Cross name prefix is required');
			}

			if (!importSettings.crossNameSetting.separator || importSettings.crossNameSetting.separator === '') {
				valid = false;
				showErrorMessage('', 'Separator for parentage designation is required');
			}

			if(!ImportCrosses.validateStartingSequenceNumber(importSettings.crossNameSetting.startNumber)){
				return false;
			}

			return valid;
		},

		updateDisplayedSequenceNameValue : function() {
			var value = $('#startingSequenceNumber').val();
			if (ImportCrosses.validateStartingSequenceNumber(value)){
				ImportCrosses.retrieveNextNameInSequence().done(function(data){
					if (data.success === '1') {
						$('#importNextSequenceName').text(data.sequenceValue);
					} else {
						showErrorMessage('', ajaxGenericErrorMsg);
					}
				}).fail(function() {
					showErrorMessage('', ajaxGenericErrorMsg);
				});
			}
		},

		retrieveNextNameInSequence : function() {
			var settingData = ImportCrosses.constructSettingsObjectFromForm();

			return $.ajax({
				headers: {
										'Accept': 'application/json',
										'Content-Type': 'application/json'
									},
				'url' : ImportCrosses.CROSSES_URL + '/generateSequenceValue',
				'type' : 'POST',
				'data' : JSON.stringify(settingData),
				'cache' : false
			});
		},

		constructSettingsObjectFromForm : function() {
			var settingObject = {};
			settingObject.name = $('#presetName').val();

			settingObject.breedingMethodSetting = {};
			settingObject.breedingMethodSetting.methodId = $('#breedingMethodDropdown').select2('val');

			if ( !settingObject.breedingMethodSetting.methodId || settingObject.breedingMethodSetting.methodId == '') {
				settingObject.breedingMethodSetting.methodId = null;
			}

			settingObject.breedingMethodSetting.basedOnStatusOfParentalLines = ! $('#useSelectedMethodCheckbox').is(':checked');

			settingObject.crossNameSetting = {};
			settingObject.crossNameSetting.prefix = $('#crossPrefix').val();
			settingObject.crossNameSetting.suffix = $('#crossSuffix').val();
			settingObject.crossNameSetting.addSpaceBetweenPrefixAndCode = $('input:radio[name=hasPrefixSpace]:checked').val() == 'true';
			settingObject.crossNameSetting.addSpaceBetweenSuffixAndCode= $('input:radio[name=hasSuffixSpace]:checked').val() == 'true';
			settingObject.crossNameSetting.numOfDigits = $('#sequenceNumberDigits').val();
			settingObject.crossNameSetting.separator = $('#parentageDesignationSeparator').val();
			settingObject.crossNameSetting.startNumber = $('#startingSequenceNumber').val();
			settingObject.crossNameSetting.saveParentageDesignationAsAString= $('input:radio[name=hasParentageDesignationName]:checked').val() == 'true';
			settingObject.preservePlotDuplicates =  ImportCrosses.preservePlotDuplicates;
			settingObject.additionalDetailsSetting = {};
			settingObject.additionalDetailsSetting.harvestLocationId = $('#locationDropdown').select2('val');
			if ($('#harvestYearDropdown').val() !== '' && $('#harvestMonthDropdown').val() !== '') {
				settingObject.additionalDetailsSetting.harvestDate = $('#harvestYearDropdown').val() + '-' + $('#harvestMonthDropdown').val();
			}

			return settingObject;

		},

		populateHarvestMonthDropdown : function(dropdownID) {
			ImportCrosses.retrieveHarvestMonths().done(function(monthData) {
				var dropdownSelect = $('#' + dropdownID);
				dropdownSelect.empty();
				dropdownSelect.select2({
					placeholder : 'Month',
					allowClear : true,
					data : monthData,
					minimumResultsForSearch : -1
				});
			});
		},

		populateHarvestYearDropdown : function(dropdownID) {
			ImportCrosses.retrieveHarvestYears().done(function (yearData) {
				var dropdownData = [];
				var dropdownSelect = $('#' + dropdownID);
				dropdownSelect.empty();
				$.each(yearData, function (index, value) {
					dropdownData.push({
						id : value,
						text : value
					});
				});

				dropdownSelect.select2({
					minimumResultsForSearch : -1,
					data : dropdownData
				});

				dropdownSelect.select2('val', yearData[0]);
			});
		},

		retrieveHarvestMonths : function() {
			return $.ajax({
				url: ImportCrosses.CROSSES_URL + '/getHarvestMonths',
				type: 'GET',
				cache: false
			});
		},

		retrieveHarvestYears: function () {
			return $.ajax({
				url: ImportCrosses.CROSSES_URL + '/getHarvestYears',
				type: 'GET',
				cache: false
			});
		},


        exportCrosses : function() {
            return $.ajax({
                url: ImportCrosses.CROSSES_URL + '/export',
                type: 'GET',
                cache: false
            });
        },

        downloadCrosses : function() {
            ImportCrosses.exportCrosses().done(function (result) {
                if (result.isSuccess) {
                    var downloadUrl = ImportCrosses.CROSSES_URL + '/download/file';

                    $.fileDownload(downloadUrl,{
                    	httpMethod: 'POST',
                        data: result
                    });

                } else {
                    createErrorNotification(crossingExportErrorHeader,result.errorMessage);
                }
            });
        },

		displayCrossesList: function (uniqueId, germplasmListId, listName, isDefault, crossesListId) {
			'use strict';
			var url = '/Fieldbook/germplasm/list/crosses/' + germplasmListId;
			if(!isDefault){
				$('#advanceHref' + uniqueId + ' .fbk-close-tab').before(': [' + listName + ']');
				url += '?isSnapshot=0';
			}else{
				url += '?isSnapshot=1';
			}
			$.ajax({
				url: url,
				type: 'GET',
				cache: false,
				success: function(html) {
					$('.crosses-list' + uniqueId).html(html);
					$('.crosses-list' + uniqueId+'-li').addClass('crosses-germplasm-items');
					$('.crosses-list' + uniqueId+'-li').data('crosses-germplasm-list-id', crossesListId);
				}
			});
		},
		displayTabCrossesList: function (germplasmListId, crossesListId, listName) {
			'use strict';
			var url = '/Fieldbook/germplasm/list/crosses/' + crossesListId;
			url += '?isSnapshot=0';
			$.ajax({
				url: url,
				type: 'GET',
				cache: false,
				success: function(html) {
					$('#saveListTreeModal').modal('hide');
					$('#saveListTreeModal').data('is-save-crosses', '0');
					$('#create-nursery-tabs .tab-pane.info').removeClass('active');
					var uniqueId,
					close,
					aHtml;
					uniqueId = crossesListId;
					close = '<i class="glyphicon glyphicon-remove fbk-close-tab" id="'+uniqueId+'" onclick="javascript: closeAdvanceListTab(' + uniqueId +')"></i>';
					aHtml = '<a id="advance-list'+uniqueId+'" role="tab" class="advanceList crossesList crossesList'+uniqueId+'" data-toggle="tab" href="#advance-list' + uniqueId + '" data-list-id="' + uniqueId + '">Crosses: [' + listName + ']' + close + '</a>';
					var stockHtml = '<div id="stock-content-pane' + uniqueId + '" class="stock-list' + uniqueId + '"></div>';
					$('#create-nursery-tab-headers').append('<li id="advance-list' + uniqueId + '-li" class="advance-germplasm-items crosses-list">' + aHtml + '</li>');
					$('#create-nursery-tabs').append('<div class="tab-pane info crosses-list'+uniqueId+'" id="advance-list' + uniqueId + '">' + html + '</div>');
					$('#create-nursery-tabs').append('<div class="tab-pane info crosses-list'+uniqueId+'" id="stock-tab-pane' + uniqueId + '">' + stockHtml + '</div>');
					$('a#advance-list'+uniqueId).tab('show');
					$('#advance-list'+uniqueId+'.tab-pane.info').addClass('active');
					$('.nav-tabs').tabdrop('layout');
					$('a#advance-list'+uniqueId).on('click', function(){
						$('#create-nursery-tabs .tab-pane.info').removeClass('active');
						$('#advance-list'+uniqueId+'.tab-pane.info').addClass('active');
					});
				}
			});
		},
		openSaveListModal: function(){
			'use strict';
			var  germplasmTreeNode = $('#germplasmFolderTree').dynatree('getTree');
			$.ajax({
					url: '/Fieldbook/ListTreeManager/saveCrossesList/',
					type: 'GET',
					cache: false,
					success: function(html) {
						$('#saveGermplasmRightSection').html(html);
						$('#saveListTreeModal').one('shown.bs.modal', function() {
							$('body').addClass('modal-open');
						}).modal({
							show: true,
							keyboard: true,
							backdrop: 'static'
						});
						$('#saveListTreeModal').data('is-save-crosses', '1');

						TreePersist.preLoadGermplasmTreeState(false, '#germplasmFolderTree', true);
						
						//we preselect the program lists
						if(germplasmTreeNode !== null && germplasmTreeNode.getNodeByKey('LOCAL') !== null) {
							germplasmTreeNode.getNodeByKey('LOCAL').activate();							
						}
					}
				}
			);
		}
};

$(document).ready(function() {
	$('.import-crosses').off('click');
    $('.btn-import-crosses').off('click');    
	$('.import-crosses').on('click', ImportCrosses.showPopup);
    $('.btn-import-crosses').on('click', ImportCrosses.doSubmitImport);
	$('.import-crosses-section .modal').on('hide.bs.modal', function() {
		$('div.import-crosses-file-upload').parent().parent().removeClass('has-error');
	});
});
