/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.StudyDetails;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.util.SettingsUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.generationcp.middleware.service.api.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@Controller
@RequestMapping(ReviewStudyDetailsController.URL)
public class ReviewStudyDetailsController extends AbstractBaseFieldbookController {

	public static final String URL = "/StudyManager/reviewStudyDetails";

	private static final Logger LOG = LoggerFactory.getLogger(ReviewStudyDetailsController.class);

	private static final int COLS = 3;

	private static final String TRIAL_MANAGER_REVIEW_TRIAL_DETAILS = "TrialManager/reviewTrialDetails";

	private static final String OBSERVATIONS_HTML = "TrialManager/observations";

	static final String MISSING_VALUE = "missing";

	@Resource
	private UserSelection userSelection;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private ErrorHandlerService errorHandlerService;

	@Resource
	private Properties appConstantsProperties;

	@Resource
	private PaginationListSelection paginationListSelection;

	@Resource
	private OntologyService ontologyService;

	@Resource
	private StudyDataManager studyDataManager;

	@Resource
	private UserService userService;

	@Resource
	private StudyEntryService studyEntryService;

	@Override
	public String getContentName() {
		return TRIAL_MANAGER_REVIEW_TRIAL_DETAILS;
	}

	@RequestMapping(value = "/show/{id}", method = RequestMethod.GET)
	public String show(@PathVariable final int id, @ModelAttribute("createTrialForm") final CreateTrialForm form,
		final Model model) {

		final Workbook workbook;
		StudyDetails details;
		try {
			workbook = this.fieldbookMiddlewareService.getStudyVariableSettings(id);
			workbook.getStudyDetails().setId(id);
			final String createdBy = this.userService.getPersonNameForUserId(NumberUtils.toInt(workbook.getStudyDetails().getCreatedBy()));
			details = SettingsUtil.convertWorkbookToStudyDetails(workbook, this.fieldbookMiddlewareService, this.fieldbookService,
					this.userSelection, this.contextUtil.getCurrentProgramUUID(), this.appConstantsProperties, createdBy);
			this.rearrangeDetails(details);
			this.getPaginationListSelection().addReviewWorkbook(Integer.toString(id), workbook);
			if (workbook.getMeasurementDatesetId() != null) {
				details.setHasMeasurements(this.fieldbookMiddlewareService.countObservations(workbook.getMeasurementDatesetId()) > 0);
			} else {
				details.setHasMeasurements(false);
			}
			model.addAttribute("plotDataset", workbook.getMeasurementDatesetId());
			model.addAttribute("cropName", this.contextUtil.getProjectInContext().getCropType().getCropName());


			this.userSelection.setWorkbook(workbook);
		} catch (final MiddlewareException e) {
			ReviewStudyDetailsController.LOG.error(e.getMessage(), e);
			details = new StudyDetails();
			this.addErrorMessageToResult(details, e, id);
		}

		final Optional<Long> nonReplicatedEntriesCount = this.getNonReplicatedEntriesCount(details);
		final long numberOfChecks = this.countNumberOfChecks(details, nonReplicatedEntriesCount);

		model.addAttribute("trialDetails", details);
		model.addAttribute("numberOfChecks", numberOfChecks);
		if (nonReplicatedEntriesCount.isPresent()) {
			model.addAttribute("nonReplicatedEntriesCount", nonReplicatedEntriesCount.get());
		}
		this.setIsSuperAdminAttribute(model);
		return this.showAjaxPage(model, this.getContentName());
	}

	void addErrorMessageToResult(final StudyDetails details, final MiddlewareException e, final int id) {
		final String param = AppConstants.STUDY.getString();
		details.setId(id);
		String errorMessage = e.getMessage();
		if (e instanceof MiddlewareQueryException) {
			errorMessage = this.errorHandlerService.getErrorMessagesAsString(((MiddlewareQueryException) e).getCode(),
				new Object[] {param, param.substring(0, 1).toUpperCase().concat(param.substring(1, param.length())), param}, "\n");
		}
		details.setErrorMessage(errorMessage);
	}

	@ResponseBody
	@RequestMapping(value = "/datasets/{nurseryId}")
	public List<DatasetReference> loadDatasets(@PathVariable final int nurseryId) {
		return this.fieldbookMiddlewareService.getDatasetReferences(nurseryId);
	}

	private void rearrangeDetails(final StudyDetails details) {
		details.setBasicStudyDetails(this.rearrangeSettingDetails(details.getBasicStudyDetails()));
		details.setManagementDetails(this.rearrangeSettingDetails(details.getManagementDetails()));
	}

	private List<SettingDetail> rearrangeSettingDetails(final List<SettingDetail> list) {
		final List<SettingDetail> newList = new ArrayList<>();

		if (list != null && !list.isEmpty()) {
			final int rows = Double.valueOf(Math.ceil(list.size() / (double) ReviewStudyDetailsController.COLS)).intValue();
			final int extra = list.size() % ReviewStudyDetailsController.COLS;
			for (int i = 0; i < list.size(); i++) {
				int delta = 0;
				final int currentColumn = i % ReviewStudyDetailsController.COLS;
				if (currentColumn > extra && extra > 0) {
					delta = currentColumn - extra;
				}
				final int computedIndex = currentColumn * rows + i / ReviewStudyDetailsController.COLS - delta;
				if (computedIndex < list.size()) {
					newList.add(list.get(computedIndex));
				} else {
					newList.add(list.get(computedIndex - 1));
				}
			}
		}
		return newList;
	}

	@ModelAttribute("currentCropUserId")
	public Integer getCurrentCropUserId() {
		return this.contextUtil.getCurrentWorkbenchUserId();
	}

	protected void setFieldbookMiddlewareService(final FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	protected void setFieldbookService(final com.efficio.fieldbook.service.api.FieldbookService fieldbookService) {
		this.fieldbookService = fieldbookService;
	}

	protected void setUserService(final UserService userService) {
		this.userService = userService;
	}


	public UserSelection getUserSelection() {
		return this.userSelection;
	}

	@RequestMapping(value = "/measurements/pageView/{pageNum}", method = RequestMethod.GET)
	public String getPaginatedListViewOnly(@PathVariable final int pageNum,
		@ModelAttribute("createTrialForm") final CreateTrialForm form, final Model model,
		@RequestParam("listIdentifier") final String datasetId) {

		final List<MeasurementRow> rows = this.paginationListSelection.getReviewDetailsList(datasetId);
		if (rows != null) {
			form.setMeasurementRowList(rows);
			form.changePage(pageNum);
		}
		final List<MeasurementVariable> variables = this.paginationListSelection.getReviewVariableList(datasetId);
		if (variables != null) {
			form.setMeasurementVariables(variables);
		}
		form.changePage(pageNum);
		this.getUserSelection().setCurrentPage(form.getCurrentPage());
		return super.showAjaxPage(model, "/TrialManager/datasetSummaryView");
	}

	@RequestMapping(value = "/measurements/viewStudyAjax/{datasetId}/{studyId}", method = RequestMethod.GET)
	public String viewStudyAjax(@ModelAttribute("createTrialForm") final CreateTrialForm form, final Model model,
		@PathVariable final int datasetId, @PathVariable final int studyId) {
		Workbook workbook = null;
		try {
			workbook = this.fieldbookMiddlewareService.getCompleteDataset(datasetId);
			this.fieldbookService.setAllPossibleValuesInWorkbook(workbook);
			SettingsUtil.resetBreedingMethodValueToId(this.fieldbookMiddlewareService, workbook.getObservations(), false,
				this.ontologyService, this.contextUtil.getCurrentProgramUUID());
		} catch (final MiddlewareException e) {
			ReviewStudyDetailsController.LOG.error(e.getMessage(), e);
		}
		this.getUserSelection()
			.setMeasurementRowList(workbook.arrangeMeasurementObservation(workbook.getObservations()));
		form.setMeasurementRowList(this.getUserSelection().getMeasurementRowList());
		form.setMeasurementVariables(workbook.getMeasurementDatasetVariables());
		this.changeLocationIdToName(form.getMeasurementRowList(), workbook.getMeasurementDatasetVariablesMap(),
			studyId);
		this.roundNumericValues(form.getMeasurementRowList());
		this.paginationListSelection.addReviewDetailsList(String.valueOf(datasetId), form.getMeasurementRowList());
		this.paginationListSelection.addReviewVariableList(String.valueOf(datasetId), form.getMeasurementVariables());
		form.changePage(1);
		this.getUserSelection().setCurrentPage(form.getCurrentPage());

		return super.showAjaxPage(model, ReviewStudyDetailsController.OBSERVATIONS_HTML);
	}

	void roundNumericValues(final List<MeasurementRow> measurementRowList) {
		for (final MeasurementRow row : measurementRowList) {
			for (final MeasurementData data : row.getDataList()) {
				if (data.getMeasurementVariable().getVariableType() != null && data.getMeasurementVariable().getVariableType().getId().equals(
					VariableType.TRAIT.getId()) && data.isNumeric() && !StringUtils.isEmpty(data.getValue()) && !ReviewStudyDetailsController.MISSING_VALUE.equals(data.getValue())) {
					final String value = StringUtils.stripEnd(String.format ("%.4f", Double.parseDouble(data.getValue())), "0");
					data.setValue(StringUtils.stripEnd(value, "."));
				}
			}
		}
	}

	void changeLocationIdToName(final List<MeasurementRow> measurementRowList,
		final Map<String, MeasurementVariable> measurementDatasetVariablesMap, final int studyId) {
		if (measurementDatasetVariablesMap.get(String.valueOf(TermId.LOCATION_ID.getId())) != null) {
			final Map<String, String> locationNameMap = this.studyDataManager
				.createInstanceLocationIdToNameMapFromStudy(studyId);
			for (final MeasurementRow row : measurementRowList) {
				for (final MeasurementData data : row.getDataList()) {
					if (TermId.LOCATION_ID.getId() == data.getMeasurementVariable().getTermId()) {
						data.setValue(locationNameMap.get(data.getValue()));
					}
				}
			}
		}

	}

	public void setStudyEntryService(final StudyEntryService studyEntryService) {
		this.studyEntryService = studyEntryService;
	}

	private long countNumberOfChecks(final StudyDetails studyDetails, final Optional<Long> nonReplicatedEntriesCount) {
	  final long checkEntriesCount = this.studyEntryService.countStudyGermplasmByEntryTypeIds(studyDetails.getId(), this.getAllCheckEntryTypeIds());

	  if (TermId.P_REP.getId() == this.getExperimentalDesignValue(studyDetails) && nonReplicatedEntriesCount.isPresent()) {
		return checkEntriesCount - nonReplicatedEntriesCount.get();
	  }

	  return checkEntriesCount;
	}

	private int getExperimentalDesignValue(final StudyDetails studyDetails) {
	  if (studyDetails.getExperimentalDesignDetails() != null && studyDetails.getExperimentalDesignDetails().getExperimentalDesign() != null) {
	    return studyDetails.getExperimentalDesignDetails().getExperimentalDesign().getValue() == null ? 0 :
				Integer.parseInt(studyDetails.getExperimentalDesignDetails().getExperimentalDesign().getValue());
	  }
	  return 0;
	}

	private Optional<Long> getNonReplicatedEntriesCount(final StudyDetails studyDetails) {
		if (TermId.P_REP.getId() == this.getExperimentalDesignValue(studyDetails)) {
			return Optional.of(this.studyEntryService.countStudyGermplasmByEntryTypeIds(studyDetails.getId(),
					Collections.singletonList(String.valueOf(SystemDefinedEntryType.NON_REPLICATED_ENTRY.getEntryTypeCategoricalId()))));
		}
		return Optional.empty();
	}
}
