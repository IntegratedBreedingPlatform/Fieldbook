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

package com.efficio.fieldbook.web.trial.controller;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.trial.bean.BasicDetails;
import com.efficio.fieldbook.web.trial.bean.Instance;
import com.efficio.fieldbook.web.trial.bean.InstanceInfo;
import com.efficio.fieldbook.web.trial.bean.TabInfo;
import com.efficio.fieldbook.web.trial.bean.TrialData;
import com.efficio.fieldbook.web.trial.bean.TrialSettingsBean;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.trial.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.SessionUtility;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * The Class CreateTrialController.
 */
@Controller
@RequestMapping(CreateTrialController.URL)
@Transactional
public class CreateTrialController extends BaseTrialController {

	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(CreateTrialController.class);

	/**
	 * The Constant URL.
	 */
	public static final String URL = "/TrialManager/createTrial";
	static final String ENVIRONMENT_DATA_TAB = "environmentData";
	static final String TRIAL_SETTINGS_DATA_TAB = "trialSettingsData";

	/**
	 * The Constant URL_SETTINGS.
	 */

	@Resource
	private ErrorHandlerService errorHandlerService;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
	 */
	@Override
	public String getContentName() {
		return "TrialManager/createTrial";
	}

	@ModelAttribute("operationMode")
	public String getOperationMode() {
		return "CREATE";
	}

	@ModelAttribute("measurementDataExisting")
	public Boolean getMeasurementDataExisting() {
		return false;
	}

	/**
	 * Show.
	 *
	 * @param model   the model
	 * @param session the session
	 * @return the string
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String show(@ModelAttribute("createTrialForm") final CreateTrialForm form, final Model model, final HttpSession session) {

		SessionUtility.clearSessionData(session,
				new String[] {SessionUtility.USER_SELECTION_SESSION_NAME, SessionUtility.POSSIBLE_VALUES_SESSION_NAME,
						SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});

		model.addAttribute("basicDetailsData", this.prepareBasicDetailsTabInfo());
		model.addAttribute("germplasmData", this.prepareGermplasmTabInfo(false));
		model.addAttribute("entryDetailsData", this.prepareEntryDetailsData());
		model.addAttribute(CreateTrialController.ENVIRONMENT_DATA_TAB, this.prepareEnvironmentsTabInfo(false));
		model.addAttribute(CreateTrialController.TRIAL_SETTINGS_DATA_TAB, this.prepareTrialSettingsTabInfo());
		model.addAttribute("experimentalDesignSpecialData", this.prepareExperimentalDesignSpecialData());
		model.addAttribute("studyTypes", this.studyDataManager.getAllVisibleStudyTypes());

		// so that we can reuse the same page being use for nursery
		model.addAttribute("createTrialForm", form);
		return this.showAngularPage(model);
	}

	@ResponseBody
	@RequestMapping(value = "/useExistingStudy", method = RequestMethod.GET)
	public Map<String, Object> getExistingTrialDetails(@RequestParam(value = "studyId") final Integer studyId) {
		final Map<String, Object> tabDetails = new HashMap<>();
		CreateTrialForm form = new CreateTrialForm();
		try {
			if (studyId != null && studyId != 0) {
				final Workbook trialWorkbook = this.fieldbookMiddlewareService.getStudyDataSet(studyId);

				this.excludeTreatmentFactorVariables(trialWorkbook);

				this.userSelection.setConstantsWithLabels(trialWorkbook.getConstants());

				tabDetails.put("germplasmData", this.prepareGermplasmTabInfo(trialWorkbook.getFactors(), true));
				tabDetails.put(CreateTrialController.ENVIRONMENT_DATA_TAB, this.prepareEnvironmentsTabInfo(trialWorkbook, true));
				tabDetails.put(CreateTrialController.TRIAL_SETTINGS_DATA_TAB,
						this.prepareTrialSettingsTabInfo(trialWorkbook.getStudyConditions(), true));
				tabDetails.put("measurementsData",
						this.prepareMeasurementVariableTabInfo(trialWorkbook.getVariates(), VariableType.TRAIT, true));
				tabDetails.put("entryDetailsData", this.prepareEntryDetailsData(trialWorkbook.getEntryDetails(), true));
				// TODO:Uncomment lines below related to treatment factors after resolving IBP-2207
				/*this.fieldbookMiddlewareService
						.setTreatmentFactorValues(trialWorkbook.getTreatmentFactors(), trialWorkbook.getMeasurementDatesetId());
				tabDetails.put("treatmentFactorsData", this.prepareTreatmentFactorsInfo(trialWorkbook.getTreatmentFactors(), true));*/
				form.setStudyTypeName(trialWorkbook.getStudyDetails().getStudyType().getName());
			}
		} catch (final MiddlewareException e) {
			CreateTrialController.LOG.error(e.getMessage(), e);
			form = this.addErrorMessageToResult(e);
		}

		tabDetails.put("createTrialForm", form);
		return tabDetails;
	}

	// TODO: Remove this method after resolving IBP-2207
	void excludeTreatmentFactorVariables(final Workbook trialWorkbook) {
		final List<Integer> treatmentFactorVariableIds = new ArrayList<>();
		for(final TreatmentVariable treatmentVariable: trialWorkbook.getTreatmentFactors()) {
			treatmentFactorVariableIds.add(treatmentVariable.getValueVariable().getTermId());
			treatmentFactorVariableIds.add(treatmentVariable.getLevelVariable().getTermId());
		}

		trialWorkbook.getFactors().removeIf(variable -> treatmentFactorVariableIds.contains(variable.getTermId()));
		trialWorkbook.setTreatmentFactors(new ArrayList<>());
	}

	private CreateTrialForm addErrorMessageToResult(final MiddlewareException e) {
		final String param = AppConstants.STUDY.getString();
		final CreateTrialForm form = new CreateTrialForm();
		form.setHasError(true);
		if (e instanceof MiddlewareQueryException) {
			form.setErrorMessage(this.errorHandlerService.getErrorMessagesAsString(((MiddlewareQueryException) e).getCode(),
					new Object[] {param, param.substring(0, 1).toUpperCase().concat(param.substring(1, param.length())), param}, "\n"));
		} else {
			form.setErrorMessage(e.getMessage());
		}
		return form;
	}

	@ModelAttribute("programLocationURL")
	public String getProgramLocation() {
		return this.fieldbookProperties.getProgramLocationsUrl();
	}

	@ModelAttribute("projectID")
	public String getProgramID() {
		return this.getCurrentProjectId();
	}

	@ModelAttribute("contextInfo")
	public ContextInfo getContextInfo() {
		return this.contextUtil.getContextInfoFromSession();
	}

	@ModelAttribute("cropName")
	public String getCropName() {
		return this.contextUtil.getProjectInContext().getCropType().getCropName();
	}

	@ModelAttribute("currentProgramId")
	public String getCurrentProgramId() {
		return this.contextUtil.getProjectInContext().getUniqueID();
	}

	@ModelAttribute("trialEnvironmentHiddenFields")
	public List<Integer> getTrialEnvironmentHiddenFields() {
		return this.buildVariableIDList(AppConstants.HIDE_STUDY_ENVIRONMENT_FIELDS.getString());
	}

	@ModelAttribute("programDefaultLocationId")
	public Integer programDefaultLocationId() {
		return this.getProgramDefaultLocationId();
	}

	@ModelAttribute("currentCropUserId")
	public Integer getCurrentCropUserId() {
		return this.contextUtil.getCurrentWorkbenchUserId();
	}

	@ModelAttribute("isSuperAdmin")
	public Boolean getIsSuperAdmin() {
		return this.authorizationService.isSuperAdminUser();
	}

	@RequestMapping(value = "/trialSettings", method = RequestMethod.GET)
	public String showCreateTrial(final Model model) {
		return this.showAjaxPage(model, BaseTrialController.URL_SETTINGS);
	}

	@RequestMapping(value = "/environment", method = RequestMethod.GET)
	public String showEnvironments(final Model model) {
		return this.showAjaxPage(model, BaseTrialController.URL_ENVIRONMENTS);
	}

	@RequestMapping(value = "/germplasm", method = RequestMethod.GET)
	public String showGermplasm(final Model model, @ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form) {
		return this.showAjaxPage(model, BaseTrialController.URL_GERMPLASM);
	}

	@RequestMapping(value = "/treatment", method = RequestMethod.GET)
	public String showTreatmentFactors(final Model model) {
		return this.showAjaxPage(model, BaseTrialController.URL_TREATMENT);
	}

	@RequestMapping(value = "/experimentalDesign", method = RequestMethod.GET)
	public String showExperimentalDesign(final Model model) {
		return this.showAjaxPage(model, BaseTrialController.URL_EXPERIMENTAL_DESIGN);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/retrieveVariablePairs/{id}", method = RequestMethod.GET)
	public List<SettingDetail> retrieveVariablePairs(@PathVariable final int id) {
		return super.retrieveVariablePairs(id);
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<String> submit(@RequestBody final TrialData data) {
		this.processEnvironmentData(data.getInstanceInfo());
		final List<SettingDetail> studyLevelConditions = this.userSelection.getStudyLevelConditions();
		final List<SettingDetail> basicDetails = this.userSelection.getBasicDetails();
		// transfer over data from user input into the list of setting details stored in the session
		this.populateSettingData(basicDetails, data.getBasicDetails().getBasicDetails());


		if (studyLevelConditions != null) {
			this.populateSettingData(studyLevelConditions, data.getTrialSettings().getUserInput());
		}

		final String name = data.getBasicDetails().getStudyName();

		if (this.userSelection.getStudyLevelConditions() == null) {
			this.userSelection.setStudyLevelConditions(new ArrayList<>());
		}

		if (this.userSelection.getBaselineTraitsList() == null) {
			this.userSelection.setBaselineTraitsList(new ArrayList<>());
		}

		if (this.userSelection.getSelectionVariates() == null) {
			this.userSelection.setSelectionVariates(new ArrayList<>());
		}

		// Combining variates to baseline traits
		this.userSelection.getBaselineTraitsList().addAll(this.userSelection.getSelectionVariates());

		final Dataset dataset = (Dataset) SettingsUtil.convertPojoToXmlDataSet(this.fieldbookMiddlewareService, name, this.userSelection,
				data.getTreatmentFactors().getCurrentData(), this.contextUtil.getCurrentProgramUUID());

		SettingsUtil.setConstantLabels(dataset, this.userSelection.getConstantsWithLabels());
		final Workbook workbook = SettingsUtil
				.convertXmlDatasetToWorkbook(dataset, this.userSelection.getExpDesignParams(), this.userSelection.getExpDesignVariables(),
						this.fieldbookMiddlewareService, this.userSelection.getExperimentalDesignVariables(),
						this.contextUtil.getCurrentProgramUUID());

		if (this.userSelection.getTemporaryWorkbook() != null) {
			this.addMeasurementVariablesToTrialObservationIfNecessary(data.getInstanceInfo().getInstances(), workbook,
					this.userSelection.getTemporaryWorkbook().getTrialObservations());
		}

		final List<MeasurementVariable> variablesForEnvironment = new ArrayList<>(workbook.getTrialVariables());

		final List<MeasurementRow> trialEnvironmentValues = WorkbookUtil
				.createMeasurementRowsFromEnvironments(data.getInstanceInfo().getInstances(), variablesForEnvironment,
						this.userSelection.getExpDesignParams());
		workbook.setTrialObservations(trialEnvironmentValues);
		data.getBasicDetails().setCreatedBy(String.valueOf(this.contextUtil.getCurrentWorkbenchUserId()));
		this.createStudyDetails(workbook, data.getBasicDetails());

		this.userSelection.setWorkbook(workbook);

		this.userSelection.setTrialEnvironmentValues(this.convertToValueReference(data.getInstanceInfo().getInstances()));

		this.fieldbookService.saveStudyColumnOrdering(workbook.getStudyDetails().getId(), data.getColumnOrders(), workbook);

		return new ResponseEntity<>("", HttpStatus.OK);
	}

	protected TabInfo prepareGermplasmTabInfo(final boolean isClearSettings) {
		final List<SettingDetail> initialDetailList = new ArrayList<>();
		final List<Integer> initialSettingIDs = this.buildVariableIDList(AppConstants.CREATE_STUDY_PLOT_REQUIRED_FIELDS.getString());

		for (final Integer initialSettingID : initialSettingIDs) {
			try {
				final SettingDetail detail =
						this.createSettingDetail(initialSettingID, null, VariableType.GERMPLASM_DESCRIPTOR.getRole().name());
				initialDetailList.add(detail);
			} catch (final MiddlewareException e) {
				CreateTrialController.LOG.error(e.getMessage(), e);
			}

		}

		final TabInfo info = new TabInfo();
		info.setSettings(initialDetailList);

		if (isClearSettings || this.userSelection.getPlotsLevelList() == null) {
			this.userSelection.setPlotsLevelList(initialDetailList);
		}

		return info;
	}

	protected TabInfo prepareEntryDetailsData() {
		final List<SettingDetail> initialDetailList = new ArrayList<>();
		final List<Integer> initialSettingIDs = this.buildVariableIDList(AppConstants.CREATE_STUDY_ENTRY_DETAILS_REQUIRED_FIELDS.getString());

		for (final Integer initialSettingID : initialSettingIDs) {
			try {
				final SettingDetail detail =
					this.createSettingDetail(initialSettingID, null, VariableType.ENTRY_DETAIL.getRole().name());
				initialDetailList.add(detail);
			} catch (final MiddlewareException e) {
				CreateTrialController.LOG.error(e.getMessage(), e);
			}
		}

		final TabInfo info = new TabInfo();
		info.setSettings(initialDetailList);

		if (!isEmpty(this.userSelection.getPlotsLevelList())) {
			this.userSelection.getPlotsLevelList().addAll(initialDetailList);
		}

		return info;
	}

	protected TabInfo prepareEntryDetailsData(final List<MeasurementVariable> measurementVariables, final boolean isUsePrevious) {
		final List<Integer> requiredIDList = this.buildVariableIDList(AppConstants.CREATE_STUDY_ENTRY_DETAILS_REQUIRED_FIELDS.getString());
		final List<SettingDetail> detailList = measurementVariables.stream()
			.map(measurementVariable -> {
				final SettingDetail settingDetail =
					this.createSettingDetail(measurementVariable.getTermId(), measurementVariable.getName(),
						VariableType.ENTRY_DETAIL.getRole().name());

				if (measurementVariable.getRole() != null) {
					settingDetail.setRole(measurementVariable.getRole());
					settingDetail.getVariable().setRole(measurementVariable.getRole().name());
				}

				if (requiredIDList.contains(measurementVariable.getTermId())) {
					settingDetail.setDeletable(false);
				} else {
					settingDetail.setDeletable(true);
				}

				if (!isUsePrevious) {
					settingDetail.getVariable().setOperation(Operation.UPDATE);
				} else {
					settingDetail.getVariable().setOperation(Operation.ADD);
				}
				return settingDetail;
			})
			.collect(Collectors.toList());

		final TabInfo info = new TabInfo();
		info.setSettings(detailList);

		if (!isEmpty(this.userSelection.getPlotsLevelList())) {
			this.userSelection.getPlotsLevelList().addAll(detailList);
		}

		return info;
	}


	protected TabInfo prepareEnvironmentsTabInfo(final boolean isClearSettings) {
		final TabInfo info = new TabInfo();
		final InstanceInfo data = new InstanceInfo();
		final int noOfEnvironments = Integer.parseInt(AppConstants.DEFAULT_NO_OF_ENVIRONMENT_COUNT.getString());
		data.setNumberOfInstances(noOfEnvironments);
		info.setData(data);

		final Integer programDefaultLocationId = this.getProgramDefaultLocationId();
		for (int i = 0; i < noOfEnvironments; i++) {
			if (programDefaultLocationId != null) {
				// Create an environment with default location ONLY IF the system found a default location
				// If programDefaultLocationId is more than 0, it means the default location exists.
				data.getInstances().add(this.createEnvironmentWithDefaultLocation(programDefaultLocationId));
			} else {
				data.getInstances().add(new Instance());
			}
		}

		final Map<String, Object> settingMap = new HashMap<>();
		final List<SettingDetail> managementDetailList = new ArrayList<>();
		final List<Integer> hiddenFields = this.buildVariableIDList(AppConstants.HIDE_STUDY_ENVIRONMENT_FIELDS.getString());

		for (final Integer id : this.buildVariableIDList(AppConstants.CREATE_STUDY_ENVIRONMENT_REQUIRED_FIELDS.getString())) {
			final SettingDetail detail = this.createSettingDetail(id, null, VariableType.ENVIRONMENT_DETAIL.getRole().name());

			if (TermId.LOCATION_ID.getId() == id) {
				detail.getVariable().setName(Workbook.DEFAULT_LOCATION_ID_VARIABLE_ALIAS);
			}

			for (final Integer hiddenField : hiddenFields) {
				if (id.equals(hiddenField)) {
					detail.setHidden(true);
				}
			}

			managementDetailList.add(detail);
		}

		settingMap.put("managementDetails", managementDetailList);
		settingMap.put("trialConditionDetails", new ArrayList<SettingDetail>());

		if (isClearSettings || this.userSelection.getTrialLevelVariableList() == null || this.userSelection.getBasicDetails().isEmpty()) {
			this.userSelection.setTrialLevelVariableList(managementDetailList);
		}

		info.setSettingMap(settingMap);
		return info;
	}

	protected TabInfo prepareBasicDetailsTabInfo() {
		final Map<String, String> basicDetails = new HashMap<>();
		final List<SettingDetail> initialDetailList = new ArrayList<>();
		final List<Integer> initialSettingIDs = this.buildVariableIDList(AppConstants.CREATE_STUDY_REQUIRED_FIELDS.getString());

		for (final Integer initialSettingID : initialSettingIDs) {
			try {
				basicDetails.put(initialSettingID.toString(), "");
				final SettingDetail detail = this.createSettingDetail(initialSettingID, null, VariableType.STUDY_DETAIL.getRole().name());
				initialDetailList.add(detail);
			} catch (final MiddlewareQueryException e) {
				CreateTrialController.LOG.error(e.getMessage(), e);
			}
		}

		final BasicDetails basic = new BasicDetails();
		basic.setBasicDetails(basicDetails);

		basic.setUserID(this.contextUtil.getCurrentWorkbenchUserId());
		basic.setUserName(this.userService.getPersonNameForUserId(basic.getUserID()));

		final TabInfo tab = new TabInfo();
		tab.setData(basic);

		if (this.userSelection.getBasicDetails() == null || this.userSelection.getBasicDetails().isEmpty()) {
			this.userSelection.setBasicDetails(initialDetailList);
		}

		return tab;
	}

	protected TabInfo prepareTrialSettingsTabInfo() {
		final TabInfo info = new TabInfo();
		info.setSettings(new ArrayList<>());
		info.setData(new TrialSettingsBean());
		return info;
	}

	@ResponseBody
	@RequestMapping(value = "/clearSettings", method = RequestMethod.GET)
	public String clearSettings() {
		try {
			this.prepareGermplasmTabInfo(true);
			this.prepareEnvironmentsTabInfo(true);

			this.prepareTrialSettingsTabInfo();
			this.prepareExperimentalDesignSpecialData();
			List<SettingDetail> detailList = new ArrayList<>();
			this.userSelection.setBaselineTraitsList(detailList);
			this.userSelection.setStudyLevelConditions(new ArrayList<>());
			this.userSelection.setStudyConditions(new ArrayList<>());
			detailList = new ArrayList<>();
			this.userSelection.setTreatmentFactors(detailList);
			if (this.userSelection.getTemporaryWorkbook() != null) {
				this.userSelection.setTemporaryWorkbook(null);
			}
		} catch (final MiddlewareException e) {
			CreateTrialController.LOG.error(e.getMessage(), e);
		}
		return "success";
	}

	@ResponseBody
	@RequestMapping(value = "/refresh/settings/tab", method = RequestMethod.GET)
	public Map<String, TabInfo> refreshSettingsTab() {
		final Map<String, TabInfo> tabDetails = new HashMap<>();

		final Workbook trialWorkbook = this.userSelection.getWorkbook();
		this.userSelection.setConstantsWithLabels(trialWorkbook.getConstants());

		tabDetails.put(CreateTrialController.ENVIRONMENT_DATA_TAB, this.prepareEnvironmentsTabInfo(trialWorkbook, false));
		tabDetails.put(CreateTrialController.TRIAL_SETTINGS_DATA_TAB,
				this.prepareTrialSettingsTabInfo(trialWorkbook.getStudyConditions(), false));

		return tabDetails;
	}

	protected void setFieldbookMiddlewareService(
			final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}
}
