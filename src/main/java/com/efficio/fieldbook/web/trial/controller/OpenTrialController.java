package com.efficio.fieldbook.web.trial.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.util.FieldbookListUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.trial.bean.TrialData;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ListDataProjectUtil;
import com.efficio.fieldbook.web.util.SessionUtility;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

@Controller
@RequestMapping(OpenTrialController.URL)
@SessionAttributes("isCategoricalDescriptionView")
public class OpenTrialController extends BaseTrialController {

	private static final String TRIAL_INSTANCE = "TRIAL_INSTANCE";
	private static final String TRIAL = "TRIAL";
	public static final String URL = "/TrialManager/openTrial";
	@Deprecated
	public static final String IS_EXP_DESIGN_PREVIEW = "isExpDesignPreview";
	public static final String MEASUREMENT_ROW_COUNT = "measurementRowCount";
	public static final String ENVIRONMENT_DATA_TAB = "environmentData";
	public static final String MEASUREMENT_DATA_EXISTING = "measurementDataExisting";
	private static final Logger LOG = LoggerFactory.getLogger(OpenTrialController.class);

	@Resource
	private StudyDataManager studyDataManagerImpl;

	@Resource
	private StudyDataManager studyDataManager;

	@Resource
	private OntologyService ontologyService;

	@Resource
	private ErrorHandlerService errorHandlerService;

	/** The Inventory list manager. */
	@Resource
	private InventoryDataManager inventoryDataManager;

	@Override
	public String getContentName() {
		return "TrialManager/createTrial";
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

	@ModelAttribute("programMethodURL")
	public String getProgramMethod() {
		return this.fieldbookProperties.getProgramBreedingMethodsUrl();
	}

	@ModelAttribute("trialEnvironmentHiddenFields")
	public List<Integer> getTrialEnvironmentHiddenFields() {
		return this.buildVariableIDList(AppConstants.HIDE_TRIAL_ENVIRONMENT_FIELDS.getString());
	}

	@ModelAttribute("isCategoricalDescriptionView")
	public Boolean initIsCategoricalDescriptionView() {
		return Boolean.FALSE;
	}

	@ModelAttribute("operationMode")
	public String getOperationMode() {
		return "OPEN";
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

	@RequestMapping(value = "/measurements", method = RequestMethod.GET)
	public String showMeasurements(@ModelAttribute("createTrialForm") final CreateTrialForm form, final Model model) {

		Workbook workbook = this.userSelection.getWorkbook();
		Integer measurementDatasetId = null;
		if (workbook != null) {

			if (workbook.getMeasurementDatesetId() != null) {
				measurementDatasetId = workbook.getMeasurementDatesetId();
			}

			// this is so we can preview the exp design
			if (this.userSelection.getTemporaryWorkbook() != null) {
				workbook = this.userSelection.getTemporaryWorkbook();
				//TODO Remove this flag it is no longer used on the front-end
				model.addAttribute(OpenTrialController.IS_EXP_DESIGN_PREVIEW, "0");
			}

			this.userSelection.setMeasurementRowList(workbook.getObservations());
			if (measurementDatasetId != null) {
				form.setMeasurementDataExisting(this.fieldbookMiddlewareService.checkIfStudyHasMeasurementData(measurementDatasetId,
						SettingsUtil.buildVariates(workbook.getVariates())));
			} else {
				form.setMeasurementDataExisting(false);
			}

			form.setMeasurementVariables(workbook.getMeasurementDatasetVariablesView());
			model.addAttribute(OpenTrialController.MEASUREMENT_ROW_COUNT, this.studyDataManager.countExperiments(measurementDatasetId));
		}

		return this.showAjaxPage(model, BaseTrialController.URL_MEASUREMENT);
	}

	@ResponseBody
	@RequestMapping(value = "/columns", method = RequestMethod.POST)
	public List<MeasurementVariable> getColumns (@ModelAttribute("createNurseryForm") final CreateNurseryForm form, final Model model,
			final HttpServletRequest request) {
		return this.getLatestMeasurements(form, request);
	}

	@RequestMapping(value = "/{trialId}", method = RequestMethod.GET)
	public String openTrial(@ModelAttribute("createTrialForm") final CreateTrialForm form, @PathVariable final Integer trialId,
			final Model model, final HttpSession session, final RedirectAttributes redirectAttributes) {
		this.clearSessionData(session);
		try {
			if (trialId != null && trialId != 0) {
				final DmsProject dmsProject = this.studyDataManager.getProject(trialId);
				if (dmsProject.getProgramUUID() == null) {
					return "redirect:" + ManageTrialController.URL + "?summaryId=" + trialId + "&summaryName=" + dmsProject.getName();
				}
				final Workbook trialWorkbook = this.fieldbookMiddlewareService.getTrialDataSet(trialId);
				this.filterAnalysisVariable(trialWorkbook);

				this.userSelection.setConstantsWithLabels(trialWorkbook.getConstants());
				this.userSelection.setWorkbook(trialWorkbook);
				this.userSelection
						.setExperimentalDesignVariables(WorkbookUtil.getExperimentalDesignVariables(trialWorkbook.getConditions()));
				this.userSelection
						.setExpDesignParams(SettingsUtil.convertToExpDesignParamsUi(this.userSelection.getExperimentalDesignVariables()));
				this.userSelection.setTemporaryWorkbook(null);
				this.userSelection.setMeasurementRowList(trialWorkbook.getObservations());

				this.fieldbookMiddlewareService
						.setTreatmentFactorValues(trialWorkbook.getTreatmentFactors(), trialWorkbook.getMeasurementDatesetId());

				form.setMeasurementDataExisting(this.fieldbookMiddlewareService
						.checkIfStudyHasMeasurementData(trialWorkbook.getMeasurementDatesetId(),
								SettingsUtil.buildVariates(trialWorkbook.getVariates())));
				form.setStudyId(trialId);

				this.setModelAttributes(form, trialId, model, trialWorkbook);
				this.setUserSelectionImportedGermplasmMainInfo(this.userSelection, trialId, model);
			}
			return this.showAngularPage(model);

		} catch (final MiddlewareQueryException e) {
			OpenTrialController.LOG.debug(e.getMessage(), e);

			redirectAttributes.addFlashAttribute("redirectErrorMessage", this.errorHandlerService.getErrorMessagesAsString(e.getCode(),
					new String[] {AppConstants.TRIAL.getString(), StringUtils.capitalize(AppConstants.TRIAL.getString()),
							AppConstants.TRIAL.getString()}, "\n"));
			return "redirect:" + ManageTrialController.URL;
		}
	}

	protected void setUserSelectionImportedGermplasmMainInfo(final UserSelection userSelection, final Integer trialId, final Model model) {
		final List<GermplasmList> germplasmLists =
				this.fieldbookMiddlewareService.getGermplasmListsByProjectId(Integer.valueOf(trialId), GermplasmListType.TRIAL);
		if (germplasmLists != null && !germplasmLists.isEmpty()) {
			final GermplasmList germplasmList = germplasmLists.get(0);

			final List<ListDataProject> listDataProjects = this.fieldbookMiddlewareService.getListDataProject(germplasmList.getId());
			final long germplasmListChecksSize = this.fieldbookMiddlewareService
					.countListDataProjectByListIdAndEntryType(germplasmList.getId(), SystemDefinedEntryType.CHECK_ENTRY);

			if (listDataProjects != null && !listDataProjects.isEmpty()) {

				model.addAttribute("germplasmListSize", listDataProjects.size());
				model.addAttribute("germplasmChecksSize", germplasmListChecksSize);
				FieldbookListUtil.populateStockIdInListDataProject(listDataProjects, inventoryDataManager);
				final List<ImportedGermplasm> list = ListDataProjectUtil.transformListDataProjectToImportedGermplasm(listDataProjects);
				final ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
				importedGermplasmList.setImportedGermplasms(list);
				final ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
				// BMS-1419, set the id to the original list's id
				mainInfo.setListId(germplasmList.getListRef());
				mainInfo.setAdvanceImportType(true);
				mainInfo.setImportedGermplasmList(importedGermplasmList);

				userSelection.setImportedGermplasmMainInfo(mainInfo);
				userSelection.setImportValid(true);
			}
		}
	}

	protected void setModelAttributes(final CreateTrialForm form, final Integer trialId, final Model model, final Workbook trialWorkbook) {
		model.addAttribute("basicDetailsData", this.prepareBasicDetailsTabInfo(trialWorkbook.getStudyDetails(),
				trialWorkbook.getStudyConditions(), false, trialId));
		model.addAttribute("germplasmData", this.prepareGermplasmTabInfo(trialWorkbook.getFactors(), false));
		model.addAttribute(OpenTrialController.ENVIRONMENT_DATA_TAB, this.prepareEnvironmentsTabInfo(trialWorkbook, false));
		model.addAttribute("trialSettingsData", this.prepareTrialSettingsTabInfo(trialWorkbook.getStudyConditions(), false));
		model.addAttribute("measurementsData",
				this.prepareMeasurementVariableTabInfo(trialWorkbook.getVariates(), VariableType.TRAIT, false));
		model.addAttribute("selectionVariableData",
				this.prepareMeasurementVariableTabInfo(trialWorkbook.getVariates(), VariableType.SELECTION_METHOD, false));
		model.addAttribute("experimentalDesignData", this.prepareExperimentalDesignTabInfo(trialWorkbook, false));

		model.addAttribute(OpenTrialController.MEASUREMENT_DATA_EXISTING, this.fieldbookMiddlewareService
				.checkIfStudyHasMeasurementData(trialWorkbook.getMeasurementDatesetId(),
						SettingsUtil.buildVariates(trialWorkbook.getVariates())));

		model.addAttribute(OpenTrialController.MEASUREMENT_ROW_COUNT,
				this.studyDataManager.countExperiments(trialWorkbook.getMeasurementDatesetId()));
		model.addAttribute("treatmentFactorsData", this.prepareTreatmentFactorsInfo(trialWorkbook.getTreatmentFactors(), false));

		// so that we can reuse the same page being use for nursery
		model.addAttribute("createNurseryForm", form);
		model.addAttribute("experimentalDesignSpecialData", this.prepareExperimentalDesignSpecialData());
		model.addAttribute("studyName", trialWorkbook.getStudyDetails().getLabel());
		model.addAttribute("advancedList", this.getAdvancedList(trialId));

		model.addAttribute("germplasmListSize", 0);
	}

	protected void clearSessionData(final HttpSession session) {
		SessionUtility.clearSessionData(session,
				new String[] {SessionUtility.USER_SELECTION_SESSION_NAME, SessionUtility.POSSIBLE_VALUES_SESSION_NAME,
						SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});
	}

	/**
	 * @param data
	 * @return
	 * @throws MiddlewareQueryException
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@Transactional
	public Map<String, Object> submit(@RequestParam("replace") final int replace, @RequestBody final TrialData data) {

		this.processEnvironmentData(data.getEnvironments());

		final List<SettingDetail> studyLevelConditions = this.userSelection.getStudyLevelConditions();
		final List<SettingDetail> basicDetails = this.userSelection.getBasicDetails();

		final List<SettingDetail> combinedList = new ArrayList<>();
		combinedList.addAll(basicDetails);
		combinedList.addAll(studyLevelConditions);

		// transfer over data from user input into the list of setting details stored in the session
		this.populateSettingData(this.userSelection.getBasicDetails(), data.getBasicDetails().getBasicDetails());
		this.populateSettingData(this.userSelection.getStudyLevelConditions(), data.getTrialSettings().getUserInput());

		if (this.userSelection.getPlotsLevelList() == null) {
			this.userSelection.setPlotsLevelList(new ArrayList<SettingDetail>());
		}
		if (this.userSelection.getBaselineTraitsList() == null) {
			this.userSelection.setBaselineTraitsList(new ArrayList<SettingDetail>());
		}
		if (this.userSelection.getNurseryConditions() == null) {
			this.userSelection.setNurseryConditions(new ArrayList<SettingDetail>());
		}
		if (this.userSelection.getTrialLevelVariableList() == null) {
			this.userSelection.setTrialLevelVariableList(new ArrayList<SettingDetail>());
		}
		if (this.userSelection.getTreatmentFactors() == null) {
			this.userSelection.setTreatmentFactors(new ArrayList<SettingDetail>());
		}
		if (this.userSelection.getSelectionVariates() == null) {
			this.userSelection.setSelectionVariates(new ArrayList<SettingDetail>());
		}

		// TODO: add deleted selection variates
		// include deleted list if measurements are available
		SettingsUtil.addDeletedSettingsList(combinedList, this.userSelection.getDeletedStudyLevelConditions(),
				this.userSelection.getStudyLevelConditions());
		SettingsUtil.addDeletedSettingsList(null, this.userSelection.getDeletedPlotLevelList(), this.userSelection.getPlotsLevelList());
		SettingsUtil.addDeletedSettingsList(null, this.userSelection.getDeletedBaselineTraitsList(),
				this.userSelection.getBaselineTraitsList());
		SettingsUtil
				.addDeletedSettingsList(null, this.userSelection.getDeletedNurseryConditions(), this.userSelection.getNurseryConditions());
		SettingsUtil.addDeletedSettingsList(null, this.userSelection.getDeletedTrialLevelVariables(),
				this.userSelection.getTrialLevelVariableList());
		SettingsUtil
				.addDeletedSettingsList(null, this.userSelection.getDeletedTreatmentFactors(), this.userSelection.getTreatmentFactors());

		final String name = data.getBasicDetails().getBasicDetails().get(TermId.STUDY_NAME.getId());

		// retain measurement dataset id and trial dataset id
		final int trialDatasetId = this.userSelection.getWorkbook().getTrialDatasetId();
		final int measurementDatasetId = this.userSelection.getWorkbook().getMeasurementDatesetId();

		// Combining variates to baseline traits.
		this.userSelection.getBaselineTraitsList().addAll(this.userSelection.getSelectionVariates());

		final Dataset dataset = (Dataset) SettingsUtil.convertPojoToXmlDataSet(this.fieldbookMiddlewareService, name, this.userSelection,
				data.getTreatmentFactors().getCurrentData(), this.contextUtil.getCurrentProgramUUID());

		SettingsUtil.setConstantLabels(dataset, this.userSelection.getConstantsWithLabels());

		final Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, false, this.userSelection.getExpDesignParams(),
				this.userSelection.getExpDesignVariables(), this.fieldbookMiddlewareService,
				this.userSelection.getExperimentalDesignVariables(), this.contextUtil.getCurrentProgramUUID());

		if (this.userSelection.isDesignGenerated()) {

			this.userSelection.setMeasurementRowList(null);
			this.userSelection.getWorkbook().setOriginalObservations(null);
			this.userSelection.getWorkbook().setObservations(null);

			this.addMeasurementVariablesToTrialObservationIfNecessary(data.getEnvironments(), workbook,
					this.userSelection.getTemporaryWorkbook().getTrialObservations());
		}

		this.assignOperationOnExpDesignVariables(workbook.getConditions(), workbook.getExpDesignVariables());

		workbook.setOriginalObservations(this.userSelection.getWorkbook().getOriginalObservations());
		workbook.setTrialObservations(this.userSelection.getWorkbook().getTrialObservations());
		workbook.setTrialDatasetId(trialDatasetId);
		workbook.setMeasurementDatesetId(measurementDatasetId);

		final List<MeasurementVariable> variablesForEnvironment = new ArrayList<MeasurementVariable>();
		variablesForEnvironment.addAll(workbook.getTrialVariables());

		final List<MeasurementRow> trialEnvironmentValues = WorkbookUtil
				.createMeasurementRowsFromEnvironments(data.getEnvironments().getEnvironments(), variablesForEnvironment,
						this.userSelection.getExpDesignParams());
		workbook.setTrialObservations(trialEnvironmentValues);

		this.createStudyDetails(workbook, data.getBasicDetails());

		this.userSelection.setWorkbook(workbook);

		this.userSelection.setTrialEnvironmentValues(this.convertToValueReference(data.getEnvironments().getEnvironments()));

		final Map<String, Object> returnVal = new HashMap<>();
		returnVal.put(OpenTrialController.ENVIRONMENT_DATA_TAB, this.prepareEnvironmentsTabInfo(workbook, false));
		returnVal.put(OpenTrialController.MEASUREMENT_DATA_EXISTING, false);
		returnVal.put(OpenTrialController.MEASUREMENT_ROW_COUNT, 0);

		// saving of measurement rows
		if (replace == 0) {
			try {
				WorkbookUtil.addMeasurementDataToRows(workbook.getFactors(), false, this.userSelection, this.ontologyService,
						this.fieldbookService, this.contextUtil.getCurrentProgramUUID());
				WorkbookUtil.addMeasurementDataToRows(workbook.getVariates(), true, this.userSelection, this.ontologyService,
						this.fieldbookService, this.contextUtil.getCurrentProgramUUID());

				workbook.setMeasurementDatasetVariables(null);
				workbook.setObservations(this.userSelection.getMeasurementRowList());

				this.userSelection.setWorkbook(workbook);

				this.fieldbookService.createIdNameVariablePairs(this.userSelection.getWorkbook(), new ArrayList<SettingDetail>(),
						AppConstants.ID_NAME_COMBINATION.getString(), true);

				this.fieldbookMiddlewareService.saveMeasurementRows(workbook, this.contextUtil.getCurrentProgramUUID());

				returnVal.put(OpenTrialController.MEASUREMENT_DATA_EXISTING, this.fieldbookMiddlewareService
						.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(),
								SettingsUtil.buildVariates(workbook.getVariates())));
				returnVal.put(OpenTrialController.MEASUREMENT_ROW_COUNT, this.studyDataManager.countExperiments(measurementDatasetId));

				this.fieldbookService
						.saveStudyColumnOrdering(workbook.getStudyDetails().getId(), workbook.getStudyName(), data.getColumnOrders(),
								workbook);

				return returnVal;
			} catch (final MiddlewareQueryException e) {
				OpenTrialController.LOG.error(e.getMessage(), e);
				return new HashMap<String, Object>();
			}
		} else {
			return returnVal;
		}
	}

	/**
	 * assign UPDATE operation for existing experimental design variables
	 *
	 * @param conditions
	 * @param existingExpDesignVariables
	 */
	void assignOperationOnExpDesignVariables(final List<MeasurementVariable> conditions,
			final List<StandardVariable> existingExpDesignVariables) {

		// skip update if the trial has no existing experimental design
		if (existingExpDesignVariables == null || existingExpDesignVariables.isEmpty()) {
			return;
		}

		final List<Integer> existingExpDesignVariableIds = new ArrayList<Integer>();
		for (final StandardVariable expVar : existingExpDesignVariables) {
			existingExpDesignVariableIds.add(expVar.getId());
		}

		for (final MeasurementVariable mvar : conditions) {
			// update the operation for experiment design variables : EXP_DESIGN, EXP_DESIGN_SOURCE, NREP
			// only if these variables already exists in the existing trial
			if ((mvar.getTermId() == TermId.EXPERIMENT_DESIGN_FACTOR.getId() || mvar.getTermId() == TermId.NUMBER_OF_REPLICATES.getId()
					|| mvar.getTermId() == TermId.EXPT_DESIGN_SOURCE.getId()) && existingExpDesignVariableIds.contains(mvar.getTermId())) {
				mvar.setOperation(Operation.UPDATE);
			}
		}
	}

	@ResponseBody
	@RequestMapping(value = "/updateSavedTrial", method = RequestMethod.GET)
	public Map<String, Object> updateSavedTrial(@RequestParam(value = "trialID") final int id) {
		final Map<String, Object> returnVal = new HashMap<String, Object>();
		final Workbook trialWorkbook = this.fieldbookMiddlewareService.getTrialDataSet(id);
		this.userSelection.setWorkbook(trialWorkbook);
		this.userSelection.setExperimentalDesignVariables(WorkbookUtil.getExperimentalDesignVariables(trialWorkbook.getConditions()));
		this.userSelection.setExpDesignParams(SettingsUtil.convertToExpDesignParamsUi(this.userSelection.getExperimentalDesignVariables()));
		returnVal.put(OpenTrialController.ENVIRONMENT_DATA_TAB, this.prepareEnvironmentsTabInfo(trialWorkbook, false));
		returnVal.put(OpenTrialController.MEASUREMENT_DATA_EXISTING, this.fieldbookMiddlewareService
				.checkIfStudyHasMeasurementData(trialWorkbook.getMeasurementDatesetId(),
						SettingsUtil.buildVariates(trialWorkbook.getVariates())));
		returnVal.put(OpenTrialController.MEASUREMENT_ROW_COUNT,
				this.studyDataManager.countExperiments(trialWorkbook.getMeasurementDatesetId()));
		returnVal.put("measurementsData", this.prepareMeasurementVariableTabInfo(trialWorkbook.getVariates(), VariableType.TRAIT, false));
		returnVal.put("selectionVariableData",
				this.prepareMeasurementVariableTabInfo(trialWorkbook.getVariates(), VariableType.SELECTION_METHOD, false));

		this.prepareBasicDetailsTabInfo(trialWorkbook.getStudyDetails(), trialWorkbook.getStudyConditions(), false, id);
		this.prepareGermplasmTabInfo(trialWorkbook.getFactors(), false);
		this.prepareTrialSettingsTabInfo(trialWorkbook.getStudyConditions(), false);

		return returnVal;
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/retrieveVariablePairs/{id}", method = RequestMethod.GET)
	public List<SettingDetail> retrieveVariablePairs(@PathVariable final int id) {
		return super.retrieveVariablePairs(id);
	}

	@ModelAttribute("nameTypes")
	public List<UserDefinedField> getNameTypes() {
		try {
			return this.fieldbookMiddlewareService.getGermplasmNameTypes();
		} catch (final MiddlewareQueryException e) {
			OpenTrialController.LOG.error(e.getMessage(), e);
		}

		return new ArrayList<UserDefinedField>();
	}

	/**
	 * Reset session variables after save.
	 *
	 *
	 * */
	@ResponseBody
	@RequestMapping(value = "/recreate/session/variables", method = RequestMethod.GET)
	public  Map<String, Object> resetSessionVariablesAfterSave(@ModelAttribute("createNurseryForm") final CreateNurseryForm form, final Model model) {
		final Workbook workbook = this.userSelection.getWorkbook();
		form.setMeasurementDataExisting(this.fieldbookMiddlewareService
				.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(), SettingsUtil.buildVariates(workbook.getVariates())));

		this.resetSessionVariablesAfterSave(workbook, false);
		final Map<String, Object> result = new HashMap<>();
		result.put("success", "1");
		return result;
	}

	@RequestMapping(value = "/load/preview/measurement", method = RequestMethod.GET)
	public String loadPreviewMeasurement(@ModelAttribute("createNurseryForm") final CreateNurseryForm form, final Model model) {
		final Workbook workbook = this.userSelection.getTemporaryWorkbook();
		final Workbook originalWorkbook = this.userSelection.getWorkbook();
		this.userSelection.setMeasurementRowList(workbook.getObservations());
		model.addAttribute(OpenTrialController.IS_EXP_DESIGN_PREVIEW, this.isPreviewEditable(originalWorkbook));
		return super.showAjaxPage(model, BaseTrialController.URL_DATATABLE);
	}

	protected String isPreviewEditable(final Workbook originalWorkbook) {
		String isPreviewEditable = "0";
		if (originalWorkbook == null || originalWorkbook.getStudyDetails() == null || originalWorkbook.getStudyDetails().getId() == null) {
			isPreviewEditable = "1";
		}
		return isPreviewEditable;
	}

	@ResponseBody
	@RequestMapping(value = "/load/dynamic/change/measurement", method = RequestMethod.POST)
	public Map<String, Object> loadDynamicChangeMeasurement(@ModelAttribute("createNurseryForm") final CreateNurseryForm form, final Model model,
			final HttpServletRequest request) {
		Workbook workbook = this.userSelection.getWorkbook();
		if (this.userSelection.getTemporaryWorkbook() != null) {
			workbook = this.userSelection.getTemporaryWorkbook();
		}

		List<MeasurementVariable> measurementDatasetVariables = new ArrayList<MeasurementVariable>();
		measurementDatasetVariables.addAll(workbook.getMeasurementDatasetVariablesView());

		final String listCsv = request.getParameter("variableList");

		if (!measurementDatasetVariables.isEmpty()) {
			final List<MeasurementVariable> newMeasurementDatasetVariables = this.getMeasurementVariableFactor(measurementDatasetVariables);
			this.getTraitsAndSelectionVariates(measurementDatasetVariables, newMeasurementDatasetVariables, listCsv);
			measurementDatasetVariables = newMeasurementDatasetVariables;
		}

		FieldbookUtil.setColumnOrderingOnWorkbook(workbook, form.getColumnOrders());
		measurementDatasetVariables = workbook.arrangeMeasurementVariables(measurementDatasetVariables);
		this.processPreLoadingMeasurementDataPage(true, form, workbook, measurementDatasetVariables, model, request.getParameter("deletedEnvironment"));
		final Map<String, Object> result = new HashMap<>();
		result.put("success", "1");
		return result;
	}

	private void processPreLoadingMeasurementDataPage(final boolean isTemporary, final CreateNurseryForm form, final Workbook workbook,
			final List<MeasurementVariable> measurementDatasetVariables, final Model model, final String deletedEnvironments) {

		final Integer measurementDatasetId = workbook.getMeasurementDatesetId();
		final List<MeasurementVariable> variates = workbook.getVariates();

		if (!isTemporary) {
			this.userSelection.setWorkbook(workbook);
		}
		if (measurementDatasetId != null) {
			form.setMeasurementDataExisting(this.fieldbookMiddlewareService
					.checkIfStudyHasMeasurementData(measurementDatasetId, SettingsUtil.buildVariates(variates)));
		} else {
			form.setMeasurementDataExisting(false);
		}

		// remove deleted environment from existing observation
		if (deletedEnvironments.length() > 0 && !"0".equals(deletedEnvironments)) {
			final Workbook tempWorkbook = this.processDeletedEnvironments(deletedEnvironments, measurementDatasetVariables, workbook);
			form.setMeasurementRowList(tempWorkbook.getObservations());
			model.addAttribute(OpenTrialController.MEASUREMENT_ROW_COUNT, this.studyDataManager.countExperiments(measurementDatasetId));
		}

		form.setMeasurementVariables(measurementDatasetVariables);
		this.userSelection.setMeasurementDatasetVariable(measurementDatasetVariables);
		model.addAttribute("createNurseryForm", form);
		model.addAttribute(OpenTrialController.IS_EXP_DESIGN_PREVIEW, this.isPreviewEditable(workbook));
	}

	private Workbook processDeletedEnvironments(final String deletedEnvironment,
			final List<MeasurementVariable> measurementDatasetVariables, final Workbook workbook) {

		Workbook tempWorkbook = this.userSelection.getTemporaryWorkbook();
		if (tempWorkbook == null) {
			tempWorkbook = this.generateTemporaryWorkbook();
		}

		// workbook.observations() collection is no longer pre-loaded into user session when trial is opened. Load now as we need it to
		// keep environment deletion functionality working as before (all plots assumed loaded).
		this.fieldbookMiddlewareService.loadAllObservations(workbook);

		final List<MeasurementRow> filteredObservations =
				this.getFilteredObservations(workbook.getObservations(), deletedEnvironment);
		final List<MeasurementRow> filteredTrialObservations =
				this.getFilteredTrialObservations(workbook.getTrialObservations(), deletedEnvironment);

		tempWorkbook.setTrialObservations(filteredTrialObservations);
		tempWorkbook.setObservations(filteredObservations);
		tempWorkbook.setMeasurementDatasetVariables(measurementDatasetVariables);

		this.userSelection.setTemporaryWorkbook(tempWorkbook);
		this.userSelection.setMeasurementRowList(filteredObservations);
		this.userSelection.getWorkbook().setTrialObservations(filteredTrialObservations);
		this.userSelection.getWorkbook().setObservations(filteredObservations);
		this.userSelection.getWorkbook().setMeasurementDatasetVariables(measurementDatasetVariables);

		return tempWorkbook;
	}

	private Workbook generateTemporaryWorkbook() {
		final List<SettingDetail> studyLevelConditions = this.userSelection.getStudyLevelConditions();
		final List<SettingDetail> basicDetails = this.userSelection.getBasicDetails();
		// transfer over data from user input into the list of setting details stored in the session
		final List<SettingDetail> combinedList = new ArrayList<SettingDetail>();
		combinedList.addAll(basicDetails);

		if (studyLevelConditions != null) {
			combinedList.addAll(studyLevelConditions);
		}

		final String name = "";

		final Dataset dataset = (Dataset) SettingsUtil
				.convertPojoToXmlDataset(this.fieldbookMiddlewareService, name, combinedList, this.userSelection.getPlotsLevelList(),
						this.userSelection.getBaselineTraitsList(), this.userSelection, this.userSelection.getTrialLevelVariableList(),
						this.userSelection.getTreatmentFactors(), null, null, this.userSelection.getNurseryConditions(), false,
						this.contextUtil.getCurrentProgramUUID());

		final Workbook tempWorkbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, false, this.contextUtil.getCurrentProgramUUID());
		final StudyDetails details = new StudyDetails();
		details.setStudyType(StudyType.T);
		tempWorkbook.setStudyDetails(details);

		return tempWorkbook;
	}

	protected List<MeasurementRow> getFilteredTrialObservations(final List<MeasurementRow> trialObservations,
			final String deletedEnvironment) {

		if ("0".equalsIgnoreCase(deletedEnvironment) || "".equalsIgnoreCase(deletedEnvironment) || trialObservations == null) {
			return trialObservations;
		}

		List<MeasurementRow> filteredTrialObservations = new ArrayList<MeasurementRow>();
		filteredTrialObservations.addAll(trialObservations);

		// remove the deleted trial instance
		for (final MeasurementRow row : trialObservations) {
			final List<MeasurementData> dataList = row.getDataList();
			for (final MeasurementData data : dataList) {
				if (this.isATrialInstanceMeasurementVariable(data) && deletedEnvironment.equalsIgnoreCase(data.getValue())) {
					filteredTrialObservations.remove(row);
					break;
				}
			}
		}

		filteredTrialObservations = this.updateTrialInstanceNoAfterDelete(deletedEnvironment, filteredTrialObservations);

		return filteredTrialObservations;
	}

	private boolean isATrialInstanceMeasurementVariable(final MeasurementData data) {
		if (data.getMeasurementVariable() != null) {
			final MeasurementVariable var = data.getMeasurementVariable();
			if (var != null && data.getMeasurementVariable().getName() != null && (
					OpenTrialController.TRIAL_INSTANCE.equalsIgnoreCase(var.getName()) || OpenTrialController.TRIAL
							.equalsIgnoreCase(var.getName()))) {
				return true;
			}
		}
		return false;
	}

	protected List<MeasurementRow> updateTrialInstanceNoAfterDelete(final String deletedEnvironment,
			final List<MeasurementRow> filteredMeasurementRowList) {

		final List<MeasurementRow> measurementRowList = new ArrayList<MeasurementRow>();
		measurementRowList.addAll(filteredMeasurementRowList);

		for (final MeasurementRow row : measurementRowList) {
			final List<MeasurementData> dataList = row.getDataList();
			for (final MeasurementData data : dataList) {
				if (this.isATrialInstanceMeasurementVariable(data)) {
					this.updateEnvironmentThatIsGreaterThanDeletedEnvironment(deletedEnvironment, data);
					break;
				}
			}
		}

		return measurementRowList;
	}

	private void updateEnvironmentThatIsGreaterThanDeletedEnvironment(final String deletedEnvironment, final MeasurementData data) {
		final Integer deletedInstanceNo = Integer.valueOf(deletedEnvironment);
		Integer currentInstanceNo = Integer.valueOf(data.getValue());

		if (deletedInstanceNo < currentInstanceNo) {
			data.setValue(String.valueOf(--currentInstanceNo));
		}
	}

	protected List<MeasurementRow> getFilteredObservations(final List<MeasurementRow> observations, final String deletedEnvironment) {

		if ("0".equalsIgnoreCase(deletedEnvironment) || "".equalsIgnoreCase(deletedEnvironment)) {
			return observations;
		}

		List<MeasurementRow> filteredObservations = new ArrayList<MeasurementRow>();
		for (final MeasurementRow row : observations) {
			final List<MeasurementData> dataList = row.getDataList();
			for (final MeasurementData data : dataList) {
				if (this.isATrialInstanceMeasurementVariable(data) && !deletedEnvironment.equalsIgnoreCase(data.getValue()) && !"0"
						.equalsIgnoreCase(data.getValue())) {
					filteredObservations.add(row);
					break;
				}
			}
		}

		filteredObservations = this.updateTrialInstanceNoAfterDelete(deletedEnvironment, filteredObservations);

		return filteredObservations;
	}

}
