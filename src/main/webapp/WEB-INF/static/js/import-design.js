var ImportDesign = (function() {
	'use strict';
	return {

		hasGermplasmListSelected: function() {
			return angular.element('#mainApp').injector().get(
				'TrialManagerDataService').applicationData.germplasmListSelected;
		},

		getDesignImportNgApp: function() {
			return angular.injector(['ng', 'designImportApp']);
		},

		getMessages: function() {
			return ImportDesign.getDesignImportNgApp().get('Messages');
		},

		showDesignWarningMessage: function() {
			showAlertMessage('',
					ImportDesign.getMessages().OWN_DESIGN_SELECT_WARNING, 5000);
		},

		getTrialManagerDataService: function() {
			return angular.element('#mainApp').injector().get(
					'TrialManagerDataService');
		},

		studyManagerCurrentData: function() {
			return ImportDesign.getTrialManagerDataService().currentData;
		},

		reloadMeasurements: function() {
			var angularElem = angular.element('#mainApp');

			angularElem
				.scope()
				.$apply(
				function () {
					ImportDesign.getTrialManagerDataService().applicationData.isGeneratedOwnDesign = true;
					ImportDesign.getTrialManagerDataService().applicationData.unsavedGeneratedDesign = true;
				});
		},

		showPopup: function(hasGermplasmListSelected) {

			if (hasGeneratedDesign()) {
				showErrorMessage(designImportErrorHeader, 'This study has generated a design, the experimental design can no longer be modified.');
			} else if (!hasGermplasmListSelected) {
				showErrorMessage(designImportErrorHeader, 'Please choose a germplasm list before you can import a design.');
			} else {
				$('#importDesignModal').modal({
					backdrop: 'static',
					keyboard: true
				});
			}

		},

		showDesignMapPopup: function() {
			setTimeout(function() {
				$('#designMapModal').one('show.bs.modal', function() {
					ImportDesign.initDesignMapPopup();
					setTimeout(function () {
						ImportDesign.showDesignWarningMessage();
					}, 200);

				}).modal();
			}, 300);

		},

		hideDesignMapPopup: function() {
			var deferred = $.Deferred();

			setTimeout(function() {
				$('#designMapModal').one('hidden.bs.modal', function() {
					deferred.resolve();
				}).modal('hide');

			}, 300);

			return deferred.promise();
		},

		initDesignMapPopup: function() {

			// get your angular element
			var elem = angular
					.element('#designMapModal .modal-content[ng-controller=designImportCtrl]');

			// get the injector.
			var injector = elem.injector();

			// get the service.
			var myService = injector.get('DesignMappingService');

			var scope = elem.scope();
			scope.designType = '';

			// retrieve initial data from the service
			$.getJSON('/Fieldbook/DesignImport/getMappingData').done(
					function(data) {

						myService.data = data;
						scope.data = myService.data;

						// apply the changes to the scope.
						scope.$apply();
					});
		},

		showReviewPopup: function() {
			return ImportDesign.showReviewDesignData().then(function(html) {
				$('#reviewDesignModal').one('shown.bs.modal', function() {
					$('#divDesignMeasurements').html(html);
				}).modal({
					backdrop: 'static',
					keyboard: true
				});
			});
		},

		showReviewDesignData: function() {
			return $.get('/Fieldbook/DesignImport/showDetails');
		},

		showChangeDesignPopup: function(hasGermplasmListSelected) {
			if (hasGermplasmListSelected) {
				$('#changeDesignModal').modal({
					backdrop: 'static',
					keyboard: true
				});
				$('#change-design-description-study').show();
			} else {
				showErrorMessage(designImportErrorHeader,
					'Please choose a germplasm list before you can import a design.');
			}
		},

		generateDesign: function() {
			var $body = $('body');

			//if the design is generated but not saved, the measurements datatable is for preview only (edit is not allowed)
			$body.addClass('preview-measurements-only');

			var environmentData =
				angular.copy(ImportDesign.studyManagerCurrentData().environments);

			$.each(environmentData.environments, function(key, data) {
				$.each(data.managementDetailValues, function(key, value) {
					if (value && value.id) {
						data.managementDetailValues[key] = value.id;
					}
				});
			});

			var service = ImportDesign.getTrialManagerDataService();
			// custom import design type id
			var designTypeId = 3;
			var data = service.retrieveGenerateDesignInput(designTypeId);
			data.environmentData = environmentData;

			$.ajax({
				type: 'POST',
				url: '/Fieldbook/DesignImport/generate',
				data: JSON.stringify(data),
				dataType: 'json',
				contentType: 'application/json; charset=utf-8'
			}).done(function(resp) {
				ImportDesign.updateEnvironmentAndMeasurements(resp);
				//TODO Remove expDesignShowPreview global, broadcast and update
				angular.element('#mainApp').scope().$broadcast('designImportGenerated');
				//TODO if error - remove preview class and show error

			});
		},

		updateEnvironmentAndMeasurements: function(resp) {
			if (!resp.isSuccess) {
				createErrorNotification(designImportErrorHeader, resp.error.join('<br/>'));
				return;
			}

			var $body = $('body');

			$body.removeClass('modal-open');
			$('#chooseGermplasmAndChecks').data('replace', '1');

			ImportDesign.closeReviewModal();
			ImportDesign.reloadMeasurements();

			var environmentData = resp.environmentData, environmentSettings = resp.environmentSettings, trialService = ImportDesign
				.getTrialManagerDataService();

			$.each(environmentSettings, function (key, value) {
				trialService.settings.environments.managementDetails.push(
					value.variable.cvTermId, trialService
						.transformViewSettingsVariable(value));
			});

			// Set the design type to Other Design Type
			trialService.currentData.experimentalDesign.designType = 3;
			trialService.updateCurrentData('environments', environmentData);

			angular.element('#mainApp').scope().$apply();

			ImportDesign.getTrialManagerDataService().clearUnappliedChangesFlag();
			//TODO Localise the message
			showSuccessfulMessage('','The study design was imported successfully and saved. You can now access the observations tab. The observations data were saved automatically.');
		},

		loadReviewDesignData: function() {
			setTimeout(
					function() {

						var environmentData =
							angular
								.copy(ImportDesign
									.studyManagerCurrentData().environments);
						$.each(environmentData.environments,
							function(key, data) {
								$.each(data.managementDetailValues,
									function(key, value) {
										if (value &&
												value.id) {
											data.managementDetailValues[key] = value.id;
										}
									});
							});

						$.ajax({
								url: '/Fieldbook/DesignImport/showDetails/data/',
								type: 'POST',
								data: JSON.stringify(environmentData),
								dataType: 'json',
								contentType: 'application/json; charset=utf-8',
								cache: false,
								success: function(response) {
									new BMS.Fieldbook.PreviewDesignMeasurementsDataTable(
											'#design-measurement-table',
											response);
								}
							});

					}, 200);

		},

		cancelDesignImport: function() {
			$.get('/Fieldbook/DesignImport/cancelImportDesign');
		},

		doSubmitImport: function() {
			if ($('#fileupload-import-design').val() === '') {
				showErrorMessage('', 'Please choose a file to import');
				return false;
			}

			var TrialManagerDataService = angular.element('#mainApp').injector().get('TrialManagerDataService');
			var hasNewEnvironmentAdded = TrialManagerDataService.applicationData.hasNewEnvironmentAdded;
			var noOfEnvironments = parseInt(TrialManagerDataService.currentData.environments.noOfEnvironments);

			var actionURL = '/Fieldbook/DesignImport/import';
			if (hasNewEnvironmentAdded) {
				actionURL += '/' + noOfEnvironments;
			}

			$('#importDesignUploadForm').attr('action', actionURL);

			ImportDesign.submitImport($('#importDesignUploadForm')).done(
					function(resp) {

						var resultJson = JSON.parse(resp);

						if (!resultJson.isSuccess) {
							createErrorNotification(designImportErrorHeader,
									resultJson.error.join('<br/>'));
							return;
						} else if (resultJson.warning) {
							showAlertMessage('', resultJson.warning);
						}

						$('#importDesignModal').one('hidden.bs.modal',
								function() {
									ImportDesign.showDesignMapPopup();
								}).modal('hide');

					});

		},
		closeReviewModal: function() {
			$('#reviewDesignModal').modal('hide');
		},

		submitImport: function($importDesignUploadForm) {
			var deferred = $.Deferred();

			$importDesignUploadForm.ajaxForm(
					{
						dataType: 'text',
						success: function(response) {
							deferred.resolve(response);
						},
						error: function(response) {
							createErrorNotification(designImportErrorHeader,
									invalidImportedFile);
							deferred.reject(response);
						}
					}).submit();

			return deferred.promise();
		},

		hideChangeButton: function() {
			if (hasGeneratedDesign()) {
				$('#change-import-design-url-link').hide();
			} else {
				$('#change-import-design-url-link').show();
			}
		},

		doResetDesign: function() {
			'use strict';
			var studyId = 0;
			if ($('#studyId').val() != undefined && $('#studyId').val() != '') {
				studyId = $('#studyId').val();
			}

			$.ajax({
				url: '/Fieldbook/DesignImport/import/change/' + studyId,
				type: 'POST',
				data: '',
				cache: false,
				success: function(response) {
					showSuccessfulMessage('', response.success);

					angular.element('#mainApp').scope().$broadcast('importedDesignReset');
					angular.element('#mainApp').scope().$apply();

					$('#changeDesignModal').modal('hide');
				}
			});
		}
	};
})();

$(document).ready(
		function() {
			'use strict';
			$('.btn-import-design').on('click', ImportDesign.doSubmitImport);
			$('.btn-import-generate').on('click', ImportDesign.generateDesign);
			$('.btn-import-generate-cancel').on('click',
					ImportDesign.cancelDesignImport);
			$('.import-design-section .modal').on(
					'hide.bs.modal',
					function() {
						$('#fileupload-import-design').parent().parent()
								.removeClass('has-error');
					});
			$('.btn-change-imported-design').on('click',
					ImportDesign.doResetDesign);

			// hide the change button if the study has measurement data,
			// otherwise, leave as is.
			ImportDesign.hideChangeButton();
		});
