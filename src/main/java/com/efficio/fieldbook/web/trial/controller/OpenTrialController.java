package com.efficio.fieldbook.web.trial.controller;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.trial.bean.TabInfo;
import com.efficio.fieldbook.web.trial.bean.TrialData;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.trial.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.SessionUtility;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.samplelist.SampleListDTO;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.service.api.SampleListService;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.generationcp.middleware.service.api.study.StudyService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping(OpenTrialController.URL)
@Transactional
public class OpenTrialController extends BaseTrialController {

	static final String TRIAL_SETTINGS_DATA = "trialSettingsData";
	private static final String TRIAL_INSTANCE = "TRIAL_INSTANCE";
	private static final String TRIAL = "TRIAL";
	public static final String URL = "/TrialManager/openTrial";
	private static final String HAS_GENERATED_DESIGN = "hasGeneratedDesign";
	static final String ENVIRONMENT_DATA_TAB = "environmentData";
	private static final String HAS_LISTS_OR_SUB_OBS = "hasListsOrSubObs";
	private static final String HAS_MEANS_DATASET = "hasMeansDataset";
	private static final Logger LOG = LoggerFactory.getLogger(OpenTrialController.class);
	private static final String IS_DELETED_ENVIRONMENT = "0";
	private static final String IS_PREVIEW_EDITABLE = "0";
	private static final String REDIRECT = "redirect:";

	@Resource
	private ErrorHandlerService errorHandlerService;

	@Resource
	private DatasetService datasetService;

	@Resource
	private DatasetTypeService datasetTypeService;

	@Resource
	private TermDataManager termDataManager;

	@Resource
	private StudyService studyService;

	@Resource
	private StudyEntryService studyEntryService;

	@Resource
	private SampleListService sampleListService;

	@Override
	public String getContentName() {
		return "TrialManager/createTrial";
	}

	@ModelAttribute("staBrappUrl")
	public String getStaBrappUrl() {
		return this.fieldbookProperties.getStaBrappUrl();
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
		return this.buildVariableIDList(AppConstants.HIDE_STUDY_ENVIRONMENT_FIELDS.getString());
	}

	@ModelAttribute("unspecifiedLocationId")
	public Integer unspecifiedLocationId() {
		return this.getUnspecifiedLocationId();
	}

	@ModelAttribute("breedingMethodCodeScale")
	public Integer breedingMethodCodeScale() {
		return TermId.BREEDING_METHOD_SCALE.getId();
	}

	@ModelAttribute("operationMode")
	public String getOperationMode() {
		return "OPEN";
	}

	@ModelAttribute("currentCropUserId")
	public Integer getCurrentCropUserId() {
		return this.contextUtil.getCurrentWorkbenchUserId();
	}

	@ModelAttribute("maxNumOfSubObsSets")
	public Integer getMaxNumOfSubObsSets() {
		return this.fieldbookProperties.getMaxNumOfSubObsSetsPerStudy();
	}

	@ModelAttribute("maxNumOfSubObsSetsPerParentUnit")
	public Integer getMaxNumOfSubObsSetsPerParentunit() {
		return this.fieldbookProperties.getMaxNumOfSubObsPerParentUnit();
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

	@RequestMapping(value = "/subObservationTab", method = RequestMethod.GET)
	public String showSubObservationTab(final Model model) {
		return this.showAjaxPage(model, BaseTrialController.URL_SUB_OBSERVATION_TAB);
	}

	@RequestMapping(value = "/subObservationSet", method = RequestMethod.GET)
	public String showSubObservationSet(final Model model) {
		return this.showAjaxPage(model, BaseTrialController.URL_SUB_OBSERVATION_SET);
	}

	@RequestMapping(value = "/{studyId}", method = RequestMethod.GET)
	public String openTrial(
		@ModelAttribute("createTrialForm") final CreateTrialForm form, @PathVariable final Integer studyId,
		final Model model, final HttpSession session, final RedirectAttributes redirectAttributes,
		@RequestParam(value = "crosseslistid", required = false) final String crossesListId) {

		model.addAttribute("createdCrossesListId", crossesListId);

		this.clearSessionData(session);
		session.setAttribute("createdCrossesListId", crossesListId);
		try {
			if (studyId != null && studyId != 0) {
				final DmsProject dmsProject = this.studyDataManager.getProject(studyId);
				if (dmsProject.getProgramUUID() == null) {
					return REDIRECT + ManageTrialController.URL + "?summaryId=" + studyId + "&summaryName=" + dmsProject.getName();
				}

				final Workbook workbook = this.fieldbookMiddlewareService.getStudyDataSet(studyId);

				// FIXME
				// See setStartingEntryNoAndPlotNoFromObservations() in
				// prepareExperimentalDesignTabInfo

				this.removeAnalysisAndAnalysisSummaryVariables(workbook);

				this.userSelection.setConstantsWithLabels(workbook.getConstants());
				this.userSelection.setWorkbook(workbook);
				this.userSelection.setExperimentalDesignVariables(WorkbookUtil.getExperimentalDesignVariables(workbook.getConditions()));
				this.userSelection
					.setExpDesignParams(SettingsUtil.convertToExpDesignParamsUi(this.userSelection.getExperimentalDesignVariables()));
				this.userSelection.setTemporaryWorkbook(null);
				this.userSelection.setMeasurementRowList(workbook.getObservations());

				this.fieldbookMiddlewareService
					.setTreatmentFactorValues(workbook.getTreatmentFactors(), workbook.getMeasurementDatesetId());

				form.setMeasurementDataExisting(this.fieldbookMiddlewareService
					.checkIfStudyHasMeasurementData(
						workbook.getMeasurementDatesetId(),
						SettingsUtil.buildVariates(workbook.getVariates())));
				form.setStudyId(studyId);
				form.setStudyTypeName(dmsProject.getStudyType().getName());
				this.setModelAttributes(form, studyId, model, workbook);
				this.setGermplasmListSize(studyId, model);
			}
			return this.showAngularPage(model);

		} catch (final MiddlewareQueryException e) {
			OpenTrialController.LOG.debug(e.getMessage(), e);

			redirectAttributes.addFlashAttribute("redirectErrorMessage", this.errorHandlerService.getErrorMessagesAsString(e.getCode(),
				new String[] {
					AppConstants.STUDY.getString(), StringUtils.capitalize(AppConstants.STUDY.getString()),
					AppConstants.STUDY.getString()}, "\n"));
			return REDIRECT + ManageTrialController.URL;
		} catch (final ParseException e) {
			redirectAttributes.addFlashAttribute("redirectErrorMessage", this.errorHandlerService
				.getErrorMessagesAsString("study.error.parser.format.date.basic.details",
					new String[] {
						AppConstants.STUDY.getString(), StringUtils.capitalize(AppConstants.STUDY.getString()),
						AppConstants.STUDY.getString()}, "\n"));
			return REDIRECT + ManageTrialController.URL;
		}
	}

	void setGermplasmListSize(final Integer studyId, final Model model) {

		final List<StudyEntryDto> studyEntries = this.studyEntryService.getStudyEntries(studyId);
		if (!studyEntries.isEmpty()) {

			final long germplasmListChecksSize =
				this.studyEntryService.countStudyGermplasmByEntryTypeIds(studyId, this.getAllCheckEntryTypeIds());
			model.addAttribute("germplasmListSize", studyEntries.size());
			model.addAttribute("germplasmChecksSize", germplasmListChecksSize);
		}
	}

	protected void setModelAttributes(final CreateTrialForm form, final Integer trialId, final Model model, final Workbook trialWorkbook)
		throws ParseException {
		final List<SampleListDTO> sampleListDTOS = this.getSampleList(trialWorkbook.getStudyDetails().getId());

		final List<Integer> datasetTypeIds = this.datasetTypeService.getSubObservationDatasetTypeIds();
		final List<DatasetDTO> datasetDTOS = this.datasetService.getDatasets(trialId, new HashSet<>(datasetTypeIds));

		final boolean hasListOrSubObs = !sampleListDTOS.isEmpty() || !datasetDTOS.isEmpty();
		final boolean hasMeansDataset = this.studyService.studyHasGivenDatasetType(trialId, DatasetTypeEnum.MEANS_DATA.getId());
		model.addAttribute("basicDetailsData", this.prepareBasicDetailsTabInfo(trialWorkbook.getStudyDetails(), false, trialId));
		model.addAttribute("germplasmData", this.prepareGermplasmTabInfo(trialWorkbook.getFactors(), false));
		model.addAttribute("entryDetailsData", this.prepareEntryDetailsData(trialWorkbook.getEntryDetails()));
		model.addAttribute(OpenTrialController.ENVIRONMENT_DATA_TAB, this.prepareEnvironmentsTabInfo(trialWorkbook, false));
		model.addAttribute(
			OpenTrialController.TRIAL_SETTINGS_DATA,
			this.prepareTrialSettingsTabInfo(trialWorkbook.getStudyConditions(), false));
		this.prepareMeasurementVariableTabInfo(trialWorkbook.getVariates(), VariableType.TRAIT, false);
		this.prepareMeasurementVariableTabInfo(trialWorkbook.getVariates(), VariableType.SELECTION_METHOD, false);
		model.addAttribute("experimentalDesignData", this.prepareExperimentalDesignTabInfo(trialWorkbook, false));
		model.addAttribute(
			OpenTrialController.HAS_LISTS_OR_SUB_OBS, hasListOrSubObs);
		model.addAttribute(OpenTrialController.HAS_GENERATED_DESIGN,
			this.studyDataManager.countExperiments(trialWorkbook.getMeasurementDatesetId()) > 0);
		model.addAttribute("treatmentFactorsData", this.prepareTreatmentFactorsInfo(trialWorkbook.getTreatmentFactors(), false));
		model.addAttribute("studyTypes", this.studyDataManager.getAllVisibleStudyTypes());

		// so that we can reuse the same page being use for nursery
		model.addAttribute("createTrialForm", form);
		model.addAttribute("experimentalDesignSpecialData", this.prepareExperimentalDesignSpecialData());
		model.addAttribute("studyName", trialWorkbook.getStudyDetails().getLabel());
		model.addAttribute("description", trialWorkbook.getStudyDetails().getDescription());
		model.addAttribute("sampleList", sampleListDTOS);

		model.addAttribute("germplasmListSize", 0);
		model.addAttribute("studyId", trialWorkbook.getStudyDetails().getId());
		model.addAttribute("measurementDatasetId", trialWorkbook.getMeasurementDatesetId());
		model.addAttribute("trialDatasetId", trialWorkbook.getTrialDatasetId());
		model.addAttribute(OpenTrialController.HAS_MEANS_DATASET, hasMeansDataset);

		this.setIsSuperAdminAttribute(model);
	}

	private void clearSessionData(final HttpSession session) {
		SessionUtility.clearSessionData(
			session,
			new String[] {
				SessionUtility.USER_SELECTION_SESSION_NAME, SessionUtility.POSSIBLE_VALUES_SESSION_NAME,
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
		this.processEnvironmentData(data.getInstanceInfo());
		// transfer over data from user input into the list of setting details
		// stored in the session
		this.populateSettingData(this.userSelection.getBasicDetails(), data.getBasicDetails().getBasicDetails());
		this.populateSettingData(this.userSelection.getStudyLevelConditions(), data.getTrialSettings().getUserInput());

		this.initializeBasicUserSelectionLists();
		this.addDeletedSettingsList();
		final String name = data.getBasicDetails().getStudyName();
		// retain measurement dataset id and trial dataset id
		final int trialDatasetId = this.userSelection.getWorkbook().getTrialDatasetId();
		final int measurementDatasetId = this.userSelection.getWorkbook().getMeasurementDatesetId();

		// Combining variates to baseline traits.
		this.userSelection.getBaselineTraitsList().addAll(this.userSelection.getSelectionVariates());

		final Dataset dataset = (Dataset) SettingsUtil.convertPojoToXmlDataSet(this.fieldbookMiddlewareService, name, this.userSelection,
			data.getTreatmentFactors().getCurrentData(), this.contextUtil.getCurrentProgramUUID());

		SettingsUtil.setConstantLabels(dataset, this.userSelection.getConstantsWithLabels());

		final Workbook workbook = SettingsUtil
			.convertXmlDatasetToWorkbook(dataset, this.userSelection.getExpDesignParams(), this.userSelection.getExpDesignVariables(),
				this.fieldbookMiddlewareService, this.userSelection.getExperimentalDesignVariables(),
				this.contextUtil.getCurrentProgramUUID());

		workbook.setOriginalObservations(this.userSelection.getWorkbook().getOriginalObservations());
		workbook.setTrialObservations(this.userSelection.getWorkbook().getTrialObservations());
		workbook.setTrialDatasetId(trialDatasetId);
		workbook.setMeasurementDatesetId(measurementDatasetId);

		final List<MeasurementVariable> variablesForEnvironment = new ArrayList<>(workbook.getTrialVariables());

		final List<MeasurementRow> trialEnvironmentValues = WorkbookUtil
			.createMeasurementRowsFromEnvironments(data.getInstanceInfo().getInstances(), variablesForEnvironment,
				this.userSelection.getExpDesignParams());

		workbook.setTrialObservations(trialEnvironmentValues);

		this.createStudyDetails(workbook, data.getBasicDetails());

		this.userSelection.setWorkbook(workbook);

		this.userSelection.setTrialEnvironmentValues(this.convertToValueReference(data.getInstanceInfo().getInstances()));

		final Map<String, Object> returnVal = new HashMap<>();
		returnVal.put(OpenTrialController.ENVIRONMENT_DATA_TAB, this.prepareEnvironmentsTabInfo(workbook, false));

		// saving variables with generated design
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

				// Set the non-study variables' operation to null since they're already saved
				this.setNonStudyVariablesOperationToNull(workbook.getAllVariables());

				this.fieldbookMiddlewareService.saveWorkbookVariablesAndObservations(workbook);
				this.fieldbookService
					.saveStudyColumnOrdering(workbook.getStudyDetails().getId(), data.getColumnOrders(),
						workbook);

				return returnVal;
			} catch (final MiddlewareQueryException e) {
				OpenTrialController.LOG.error(e.getMessage(), e);
				return new HashMap<>();
			}
		} else {
			return returnVal;
		}
	}

	void setNonStudyVariablesOperationToNull(final List<MeasurementVariable> variables) {
		for(final MeasurementVariable measurementVariable: variables) {
			if(!PhenotypicType.STUDY.equals(measurementVariable.getRole())) {
				measurementVariable.setOperation(null);
			}
		}
	}

	Map<Integer, List<Integer>> detectValueChangesInVariables(final List<MeasurementRow> oldMeasurementRows,
		final List<MeasurementRow> newMeasurementRows) {

		final Map<Integer, List<Integer>> changedVariablesPerInstance = new HashMap<>();

		if (oldMeasurementRows == null || newMeasurementRows == null) {
			return changedVariablesPerInstance;
		}

		final Map<Integer, Map<Integer, MeasurementData>> oldMeasurementDataMap = new HashMap<>();
		for (final MeasurementRow measurementRow : oldMeasurementRows) {
			final Map<Integer, MeasurementData> measurementDataMap = new HashMap<>();
			for (final MeasurementData measurementData : measurementRow.getDataList()) {
				measurementDataMap.put(measurementData.getMeasurementVariable().getTermId(), measurementData);
			}
			oldMeasurementDataMap.put((int) measurementRow.getLocationId(), measurementDataMap);
		}

		for (final MeasurementRow measurementRow : newMeasurementRows) {
			final int locationId = (int) measurementRow.getLocationId();
			for (final MeasurementData newMeasurementData : measurementRow.getDataList()) {
				final int termId = newMeasurementData.getMeasurementVariable().getTermId();
				if (oldMeasurementDataMap.containsKey(locationId) && oldMeasurementDataMap.get(locationId).containsKey(termId)) {
					final String newValue = newMeasurementData.getValue();
					final String oldValue = oldMeasurementDataMap.get(locationId).get(termId).getValue();
					if (!newValue.equals(oldValue)) {
						if (!changedVariablesPerInstance.containsKey(locationId)) {
							changedVariablesPerInstance.put(locationId, new ArrayList<Integer>());
						}
						changedVariablesPerInstance.get(locationId).add(termId);
					}
				}
			}
		}

		return changedVariablesPerInstance;
	}

	@ResponseBody
	@RequestMapping(value = "/updateSavedTrial", method = RequestMethod.GET)
	public Map<String, Object> updateSavedTrial(@RequestParam(value = "trialID") final int id) throws ParseException {
		final Map<String, Object> returnVal = new HashMap<>();
		final Workbook trialWorkbook = this.fieldbookMiddlewareService.getStudyDataSet(id);

		this.removeAnalysisAndAnalysisSummaryVariables(trialWorkbook);

		this.userSelection.setWorkbook(trialWorkbook);
		this.userSelection.setExperimentalDesignVariables(WorkbookUtil.getExperimentalDesignVariables(trialWorkbook.getConditions()));
		this.userSelection.setExpDesignParams(SettingsUtil.convertToExpDesignParamsUi(this.userSelection.getExperimentalDesignVariables()));
		returnVal.put(OpenTrialController.ENVIRONMENT_DATA_TAB, this.prepareEnvironmentsTabInfo(trialWorkbook, false));
		returnVal.put(OpenTrialController.TRIAL_SETTINGS_DATA, this.prepareTrialSettingsTabInfo(trialWorkbook.getStudyConditions(), false));
		this.prepareBasicDetailsTabInfo(trialWorkbook.getStudyDetails(), false, id);
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

		return new ArrayList<>();
	}

	/**
	 * Reset session variables after save.
	 */
	@ResponseBody
	@RequestMapping(value = "/recreate/session/variables", method = RequestMethod.GET)
	public Map<String, Object> resetSessionVariablesAfterSave(
		@ModelAttribute("createTrialForm") final CreateTrialForm form,
		final Model model) {
		final Workbook workbook = this.userSelection.getWorkbook();
		form.setMeasurementDataExisting(this.fieldbookMiddlewareService
			.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(), SettingsUtil.buildVariates(workbook.getVariates())));

		this.resetSessionVariablesAfterSave(workbook);
		final Map<String, Object> result = new HashMap<>();
		result.put("success", "1");
		return result;
	}

	String isPreviewEditable(final Workbook originalWorkbook) {
		String isPreviewEditable = IS_PREVIEW_EDITABLE;
		if (originalWorkbook == null || originalWorkbook.getStudyDetails() == null || originalWorkbook.getStudyDetails().getId() == null) {
			isPreviewEditable = "1";
		}
		return isPreviewEditable;
	}

	List<MeasurementRow> getFilteredTrialObservations(
		final List<MeasurementRow> trialObservations,
		final String deletedEnvironment) {

		if (IS_DELETED_ENVIRONMENT.equalsIgnoreCase(deletedEnvironment) || StringUtils.EMPTY.equalsIgnoreCase(deletedEnvironment)
			|| trialObservations == null) {
			return trialObservations;
		}

		List<MeasurementRow> filteredTrialObservations = new ArrayList<>(trialObservations);

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

	private List<MeasurementRow> updateTrialInstanceNoAfterDelete(
		final String deletedEnvironment,
		final List<MeasurementRow> filteredMeasurementRowList) {

		final List<MeasurementRow> measurementRowList = new ArrayList<>(filteredMeasurementRowList);

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
		final int deletedInstanceNo = Integer.parseInt(deletedEnvironment);
		int currentInstanceNo = Integer.parseInt(data.getValue());

		if (deletedInstanceNo < currentInstanceNo) {
			data.setValue(String.valueOf(--currentInstanceNo));
		}
	}

	List<MeasurementRow> getFilteredObservations(final List<MeasurementRow> observations, final String deletedEnvironment) {

		if (IS_DELETED_ENVIRONMENT.equalsIgnoreCase(deletedEnvironment) || StringUtils.EMPTY.equalsIgnoreCase(deletedEnvironment)) {
			return observations;
		}

		List<MeasurementRow> filteredObservations = new ArrayList<>();
		for (final MeasurementRow row : observations) {
			final List<MeasurementData> dataList = row.getDataList();
			for (final MeasurementData data : dataList) {
				if (this.isATrialInstanceMeasurementVariable(data) && !deletedEnvironment.equalsIgnoreCase(data.getValue())
					&& !IS_DELETED_ENVIRONMENT.equalsIgnoreCase(data.getValue())) {
					filteredObservations.add(row);
					break;
				}
			}
		}

		filteredObservations = this.updateTrialInstanceNoAfterDelete(deletedEnvironment, filteredObservations);

		return filteredObservations;
	}

	private List<SampleListDTO> getSampleList(final Integer studyId) {
		final List<Integer> datasetTypeIds = this.datasetTypeService.getObservationDatasetTypeIds();
		final List<Integer> datasetIds = new ArrayList<>();

		final List<DatasetDTO> datasetDTOs = this.datasetService.getDatasets(studyId, new HashSet<>(datasetTypeIds));
		for (final DatasetDTO dataset : datasetDTOs) {
			datasetIds.add(dataset.getDatasetId());
		}
		if (!datasetIds.isEmpty()) {
			return this.sampleListService.getSampleLists(datasetIds);
		}
		return new ArrayList<>();
	}

	/**
	 * We maintain the state of categorical description view in session to support the ff scenario:
	 * 1. When user does a browser refresh, the state of measurements view is maintained
	 * 2. When user switches between studies, state is also maintained
	 *
	 * @param showCategoricalDescriptionView
	 * @param session
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/setCategoricalDisplayType", method = RequestMethod.GET)
	public Boolean setCategoricalDisplayType(@RequestParam final Boolean showCategoricalDescriptionView,
		final HttpSession session) {
		Boolean isCategoricalDescriptionView = (Boolean) session.getAttribute("isCategoricalDescriptionView");

		if (isCategoricalDescriptionView == null) {
			isCategoricalDescriptionView = Boolean.FALSE;
		}

		if (null != showCategoricalDescriptionView) {
			isCategoricalDescriptionView = showCategoricalDescriptionView;
		} else {
			isCategoricalDescriptionView ^= Boolean.TRUE;
		}

		session.setAttribute("isCategoricalDescriptionView", isCategoricalDescriptionView);

		return isCategoricalDescriptionView;
	}

	@ResponseBody
	@RequestMapping(value = "/getExperimentalDesignName/{experimentalDesignId}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public Map<String, Object> getExperimentalDesignName(@PathVariable final Integer experimentalDesignId) {
		final Map<String, Object> output = new HashMap<>();
		final Term exptDesignValue = this.termDataManager.getTermById(experimentalDesignId);
		output.put("name", exptDesignValue.getDefinition());
		return output;
	}
}
