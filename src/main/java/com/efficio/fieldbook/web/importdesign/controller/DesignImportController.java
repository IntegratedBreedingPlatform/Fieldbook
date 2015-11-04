
package com.efficio.fieldbook.web.importdesign.controller;

/**
 * Created by cyrus on 5/8/15.
 */

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import com.efficio.fieldbook.service.api.SettingsService;
import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.common.form.ImportDesignForm;
import com.efficio.fieldbook.web.importdesign.service.DesignImportService;
import com.efficio.fieldbook.web.importdesign.validator.DesignImportValidator;
import com.efficio.fieldbook.web.nursery.controller.SettingsController;
import com.efficio.fieldbook.web.trial.bean.Environment;
import com.efficio.fieldbook.web.trial.bean.EnvironmentData;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import com.efficio.fieldbook.web.util.parsing.DesignImportParser;
import org.apache.commons.lang.StringUtils;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.ExperimentalDesignVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * The Class DesignImportController.
 */
@Controller
@RequestMapping(DesignImportController.URL)
public class DesignImportController extends SettingsController {

	private static final String UNMAPPED_HEADERS = "unmappedHeaders";

	private static final String SUCCESS = "success";

	private static final String MAPPED_ENVIRONMENTAL_FACTORS = "mappedEnvironmentalFactors";

	private static final String MAPPED_DESIGN_FACTORS = "mappedDesignFactors";

	private static final String MAPPED_GERMPLASM_FACTORS = "mappedGermplasmFactors";

	private static final String MAPPED_TRAITS = "mappedTraits";

	public static final String IS_SUCCESS = "isSuccess";

	public static final String ERROR = "error";

	private static final Logger LOG = LoggerFactory.getLogger(DesignImportController.class);

	public static final String URL = "/DesignImport";

	public static final String REVIEW_DETAILS_PAGINATION_TEMPLATE = "/DesignImport/reviewDetailsPagination";

	@Resource
	private DesignImportParser parser;

	@Resource
	private DesignImportService designImportService;

	@Resource
	private MessageSource messageSource;

	@Resource
	private UserSelection userSelection;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private DesignImportValidator designImportValidator;

	@Resource
	private SettingsService settingsService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#show(org. springframework.ui.Model)
	 */
	@Override
	@RequestMapping(method = RequestMethod.GET)
	public String show(final Model model) {
		return super.showAngularPage(model);
	}

	@Override
	public String getContentName() {
		return String.format("%s/designImportMain", DesignImportController.URL);
	}

	@ResponseBody
	@RequestMapping(value = "/import/{studyType}", method = RequestMethod.POST, produces = "text/plain")
	public String importFile(@ModelAttribute("importDesignForm") final ImportDesignForm form, @PathVariable final String studyType) {

		final Map<String, Object> resultsMap = new HashMap<>();

		try {

			this.initializeTemporaryWorkbook(studyType);

			final DesignImportData designImportData = this.parser.parseFile(form.getFile());

			this.performAutomap(designImportData);

			this.userSelection.setDesignImportData(designImportData);

			resultsMap.put(DesignImportController.IS_SUCCESS, 1);

		} catch (final FileParsingException e) {

			DesignImportController.LOG.error(e.getMessage(), e);

			resultsMap.put(DesignImportController.IS_SUCCESS, 0);
			// error messages is still in .prop format,
			resultsMap.put(DesignImportController.ERROR, new String[] {e.getMessage()});
		}

		// we return string instead of json to fix IE issue rel. DataTable
		return this.convertObjectToJson(resultsMap);
	}

	/**
	 * For now this method is only applicable in Nursery. Added the studyType as a parameter for future implementation.
	 * 
	 * @param studyId
	 * @param studyType
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/import/change/{studyId}/{studyType}", method = RequestMethod.POST,
			produces = "application/json; charset=utf-8")
	public String changeDesign(@PathVariable final Integer studyId, @PathVariable final String studyType) {

		final Map<String, Object> resultsMap = new HashMap<>();

		// reset
		this.cancelImportDesign();

		this.userSelection.setExperimentalDesignVariables(null);
		this.userSelection.setExpDesignParams(null);
		this.userSelection.setExpDesignVariables(null);

		// handling for existing study
		if (studyId != null && studyId != 0) {
			WorkbookUtil.resetObservationToDefaultDesign(this.userSelection.getWorkbook().getObservations());
		}

		resultsMap.put(DesignImportController.IS_SUCCESS, 1);
		resultsMap.put(DesignImportController.SUCCESS,
				this.messageSource.getMessage("design.import.change.design.success.message", null, Locale.ENGLISH));

		// we return string instead of json to fix IE issue rel. DataTable
		return this.convertObjectToJson(resultsMap);
	}

	@ResponseBody
	@RequestMapping(value = "/getMappingData", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public Map<String, List<DesignHeaderItem>> getMappingData() {
		final Map<String, List<DesignHeaderItem>> mappingData = new HashMap<>();

		mappingData.put(DesignImportController.UNMAPPED_HEADERS, this.userSelection.getDesignImportData().getUnmappedHeaders());
		mappingData.put(DesignImportController.MAPPED_ENVIRONMENTAL_FACTORS, this.userSelection.getDesignImportData().getMappedHeaders()
				.get(PhenotypicType.TRIAL_ENVIRONMENT));
		mappingData.put(DesignImportController.MAPPED_DESIGN_FACTORS,
				this.userSelection.getDesignImportData().getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN));
		mappingData.put(DesignImportController.MAPPED_GERMPLASM_FACTORS,
				this.userSelection.getDesignImportData().getMappedHeaders().get(PhenotypicType.GERMPLASM));
		mappingData.put(DesignImportController.MAPPED_TRAITS,
				this.userSelection.getDesignImportData().getMappedHeaders().get(PhenotypicType.VARIATE));

		return mappingData;
	}

	@RequestMapping(value = "/showDetails", method = RequestMethod.GET)
	public String showDetails(final Model model) {

		final Workbook workbook = this.userSelection.getTemporaryWorkbook();
		final DesignImportData designImportData = this.userSelection.getDesignImportData();

		final Set<MeasurementVariable> measurementVariables =
				this.designImportService.getDesignMeasurementVariables(workbook, designImportData, false);

		model.addAttribute("measurementVariables", measurementVariables);

		return super.showAjaxPage(model, DesignImportController.REVIEW_DETAILS_PAGINATION_TEMPLATE);

	}

	@ResponseBody
	@RequestMapping(value = "/showDetails/data", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	public List<Map<String, Object>> showDetailsData(@RequestBody final EnvironmentData environmentData, final Model model,
			@ModelAttribute("importDesignForm") final ImportDesignForm form) {

		this.processEnvironmentData(environmentData);

		final Workbook workbook = this.userSelection.getTemporaryWorkbook();
		final DesignImportData designImportData = this.userSelection.getDesignImportData();

		List<MeasurementRow> measurementRows = new ArrayList<>();

		try {
			measurementRows = this.designImportService.generateDesign(workbook, designImportData, environmentData, false);
		} catch (final DesignValidationException e) {
			DesignImportController.LOG.error(e.getMessage(), e);
		}

		final List<Map<String, Object>> masterList = new ArrayList<>();

		for (final MeasurementRow row : measurementRows) {

			final Map<String, Object> dataMap = this.generateDatatableDataMap(row, null);

			masterList.add(dataMap);
		}

		return masterList;

	}

	@ResponseBody
	@RequestMapping(value = "/postSelectedNurseryType")
	public Boolean postSelectedNurseryType(@RequestBody final String nurseryTypeId) {
		if (StringUtils.isNumeric(nurseryTypeId)) {
			final Integer value = Integer.valueOf(nurseryTypeId);
			this.userSelection.setNurseryTypeForDesign(value);
		}

		return true;
	}

	@ResponseBody
	@RequestMapping(value = "/cancelImportDesign")
	public void cancelImportDesign() {

		// If the Import Design is canceled, make sure to revert the changes made to UserSelection.
		this.userSelection.setTemporaryWorkbook(null);
		this.userSelection.setDesignImportData(null);

	}

	@ResponseBody
	@RequestMapping(value = "/validateAndSaveNewMapping/{noOfEnvironments}", method = RequestMethod.POST)
	public Map<String, Object> validateAndSaveNewMapping(@RequestBody final Map<String, List<DesignHeaderItem>> mappedHeaders,
			@PathVariable final Integer noOfEnvironments) {

		final Map<String, Object> resultsMap = new HashMap<>();
		try {
			this.updateDesignMapping(mappedHeaders);

			this.designImportValidator.validateDesignData(this.userSelection.getDesignImportData());

			if (!this.designImportService.areTrialInstancesMatchTheSelectedEnvironments(noOfEnvironments,
					this.userSelection.getDesignImportData())) {
				resultsMap.put("warning",
						this.messageSource.getMessage("design.import.warning.trial.instances.donotmatch", null, Locale.ENGLISH));
			}

			boolean hasConflict = false;
			boolean hasChecksSelected = false;
			boolean hasExistingDesign = false;

			if (this.userSelection.getWorkbook() != null) {
				hasConflict = this.userSelection.getWorkbook().getMeasurementDatasetVariables() != null && this.hasConflict(this.designImportService.getMeasurementVariablesFromDataFile(
						this.userSelection.getTemporaryWorkbook(), this.userSelection.getDesignImportData()), new HashSet<>(
						this.userSelection.getWorkbook().getMeasurementDatasetVariables()));
				hasChecksSelected = this.hasCheckVariables(this.userSelection.getWorkbook().getConditions());
				hasExistingDesign = this.userSelection.getWorkbook().hasExistingExperimentalDesign();
			}


			resultsMap.put(DesignImportController.SUCCESS, Boolean.TRUE);
			resultsMap.put("hasConflict", hasConflict);
			resultsMap.put("hasChecksSelected", hasChecksSelected);
			resultsMap.put("hasExistingDesign",hasExistingDesign);

		} catch (final DesignValidationException e) {

			DesignImportController.LOG.error(e.getMessage(), e);

			resultsMap.put(DesignImportController.SUCCESS, Boolean.FALSE);
			resultsMap.put(DesignImportController.ERROR, e.getMessage());
			resultsMap.put("message", e.getMessage());
		}

		return resultsMap;
	}

	protected boolean hasConflict(final Set<MeasurementVariable> setA, final Set<MeasurementVariable> setB) {
		Set<MeasurementVariable> a;
		Set<MeasurementVariable> b;

		if (setA.size() <= setB.size()) {
			a = setA;
			b = setB;
		} else {
			a = setB;
			b = setA;
		}

		for (final MeasurementVariable e : a) {
			if (b.contains(e)) {
				return true;
			}
		}
		return false;
	}

	protected void updateDesignMapping(final Map<String, List<DesignHeaderItem>> mappedHeaders) {
		final Map<PhenotypicType, List<DesignHeaderItem>> newMappingResults = new HashMap<>();

		for (final Map.Entry<String, List<DesignHeaderItem>> item : mappedHeaders.entrySet()) {
			for (final DesignHeaderItem mappedHeader : item.getValue()) {

				final StandardVariable stdVar =
						this.ontologyDataManager.getStandardVariable(mappedHeader.getId(), this.contextUtil.getCurrentProgramUUID());

				if (DesignImportController.MAPPED_ENVIRONMENTAL_FACTORS.equals(item.getKey())) {
					stdVar.setPhenotypicType(PhenotypicType.TRIAL_ENVIRONMENT);
				} else if (DesignImportController.MAPPED_DESIGN_FACTORS.equals(item.getKey())) {
					stdVar.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
				} else if (DesignImportController.MAPPED_GERMPLASM_FACTORS.equals(item.getKey())) {
					stdVar.setPhenotypicType(PhenotypicType.GERMPLASM);
				} else if (DesignImportController.MAPPED_TRAITS.equals(item.getKey())) {
					stdVar.setPhenotypicType(PhenotypicType.VARIATE);
				}

				mappedHeader.setVariable(stdVar);
			}

			if (DesignImportController.MAPPED_ENVIRONMENTAL_FACTORS.equals(item.getKey())) {
				newMappingResults.put(PhenotypicType.TRIAL_ENVIRONMENT, item.getValue());
			} else if (DesignImportController.MAPPED_DESIGN_FACTORS.equals(item.getKey())) {
				newMappingResults.put(PhenotypicType.TRIAL_DESIGN, item.getValue());
			} else if (DesignImportController.MAPPED_GERMPLASM_FACTORS.equals(item.getKey())) {
				newMappingResults.put(PhenotypicType.GERMPLASM, item.getValue());
			} else if (DesignImportController.MAPPED_TRAITS.equals(item.getKey())) {
				newMappingResults.put(PhenotypicType.VARIATE, item.getValue());
			}
		}

		this.userSelection.getDesignImportData().setMappedHeaders(newMappingResults);
	}

	@ResponseBody
	@RequestMapping(value = "/generate", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	public Map<String, Object> generateMeasurements(@RequestBody final EnvironmentData environmentData) {

		final Map<String, Object> resultsMap = new HashMap<>();

		this.processEnvironmentData(environmentData);

		try {

			this.checkTheDeletedSettingDetails(this.userSelection, this.userSelection.getDesignImportData());

			this.initializeTemporaryWorkbook(this.userSelection.getTemporaryWorkbook().getStudyDetails().getStudyType().name());

			final Workbook workbook = this.userSelection.getTemporaryWorkbook();
			final DesignImportData designImportData = this.userSelection.getDesignImportData();

			this.removeExperimentDesignVariables(workbook.getFactors());

			List<MeasurementRow> measurementRows;
			Set<MeasurementVariable> measurementVariables;
			Set<StandardVariable> expDesignVariables;
			Set<MeasurementVariable> experimentalDesignMeasurementVariables;

			measurementRows = this.designImportService.generateDesign(workbook, designImportData, environmentData, false);

			workbook.setObservations(measurementRows);

			measurementVariables = this.designImportService.getDesignMeasurementVariables(workbook, designImportData, false);

			workbook.setMeasurementDatasetVariables(new ArrayList<>(measurementVariables));

			expDesignVariables = this.designImportService.getDesignRequiredStandardVariables(workbook, designImportData);

			workbook.setExpDesignVariables(new ArrayList<>(expDesignVariables));

			experimentalDesignMeasurementVariables =
					this.designImportService.getDesignRequiredMeasurementVariable(workbook, designImportData);

			this.userSelection.setExperimentalDesignVariables(new ArrayList<>(experimentalDesignMeasurementVariables));

			// Only for Trial
			this.addFactorsIfNecessary(workbook, designImportData);

			// Only for Nursery
			this.addConditionsIfNecessary(workbook, designImportData);

			this.addVariates(workbook, designImportData);

			this.addExperimentDesign(workbook, experimentalDesignMeasurementVariables);

			// Only for Trial
			this.populateTrialLevelVariableListIfNecessary(workbook);

			// Only for Nursery
			this.populateStudyLevelVariableListIfNecessary(workbook, environmentData, designImportData);

			this.createTrialObservations(environmentData, workbook, designImportData);

			// Only for Nursery
			this.resetCheckList(workbook, this.userSelection);

			resultsMap.put(DesignImportController.IS_SUCCESS, 1);
			resultsMap.put("environmentData", environmentData);
			resultsMap.put("environmentSettings", this.userSelection.getTrialLevelVariableList());

		} catch (final Exception e) {

			DesignImportController.LOG.error(e.getMessage(), e);

			resultsMap.put(DesignImportController.IS_SUCCESS, 0);
			// error messages is still in .prop format,
			resultsMap.put(DesignImportController.ERROR, new String[] {e.getMessage()});
		}

		return resultsMap;
	}

	/**
	 * Resets the Check list and deletes all Check Variables previously saved in Nursery. The system will automatically reset and override
	 * the Check List after importing a Custom Design.
	 * 
	 * @param workbook
	 * @param userSelection
	 */
	protected void resetCheckList(final Workbook workbook, final UserSelection userSelection) {

		// This is only applicable in Nursery since there's no Check List in Trial.
		if (workbook.getStudyDetails().getStudyType() == StudyType.N) {

			// Create an ImportedCheckGermplasmMainInfo with an EMPTY data so that it will be deleted on save.
			final ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
			mainInfo.setAdvanceImportType(true);

			final List<ImportedGermplasm> list = new ArrayList<>();

			final ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
			importedGermplasmList.setImportedGermplasms(list);
			mainInfo.setImportedGermplasmList(importedGermplasmList);

			userSelection.setCurrentPageCheckGermplasmList(1);
			userSelection.setImportedCheckGermplasmMainInfo(mainInfo);
			userSelection.setImportValid(true);

			// Also delete the CHECK VARIABLES
			this.addCheckVariablesToDeleted(userSelection.getStudyLevelConditions());

		}

	}

	protected void checkTheDeletedSettingDetails(final UserSelection userSelection, final DesignImportData designImportData) {

		final Map<String, String> idNameMap = AppConstants.ID_NAME_COMBINATION.getMapOfValues();
		final Map<String, String> nameIdMap = this.switchKey(idNameMap);

		for (final MeasurementVariable mvar : this.designImportService.getMeasurementVariablesFromDataFile(null, designImportData)) {

			if (userSelection.getDeletedTrialLevelVariables() != null) {
				final Iterator<SettingDetail> deletedTrialLevelVariables = userSelection.getDeletedTrialLevelVariables().iterator();
				while (deletedTrialLevelVariables.hasNext()) {
					final SettingDetail deletedSettingDetail = deletedTrialLevelVariables.next();

					if (deletedSettingDetail.getVariable().getCvTermId().intValue() == mvar.getTermId()) {

						deletedSettingDetail.getVariable().setOperation(Operation.UPDATE);
						userSelection.getTrialLevelVariableList().add(deletedSettingDetail);

						deletedTrialLevelVariables.remove();

					}

					final String termIdOfName = idNameMap.get(String.valueOf(deletedSettingDetail.getVariable().getCvTermId()));
					if (termIdOfName != null) {

						this.updateOperation(Integer.valueOf(termIdOfName), userSelection.getTrialLevelVariableList(), Operation.UPDATE);

						deletedSettingDetail.getVariable().setOperation(Operation.UPDATE);
						userSelection.getTrialLevelVariableList().add(deletedSettingDetail);

						deletedTrialLevelVariables.remove();
					}

					final String termIdOfId = nameIdMap.get(String.valueOf(deletedSettingDetail.getVariable().getCvTermId()));
					if (termIdOfId != null) {
						this.updateOperation(Integer.valueOf(termIdOfId), userSelection.getTrialLevelVariableList(), Operation.UPDATE);

						deletedSettingDetail.getVariable().setOperation(Operation.UPDATE);
						userSelection.getTrialLevelVariableList().add(deletedSettingDetail);

						deletedTrialLevelVariables.remove();
					}
				}

			}

		}

	}

	protected void updateOperation(final int termId, final List<SettingDetail> settingDetails, final Operation operation) {

		for (final SettingDetail sd : settingDetails) {
			if (sd.getVariable().getCvTermId().intValue() == termId) {
				sd.getVariable().setOperation(operation);
				break;
			}
		}

	}

	public void initializeTemporaryWorkbook(final String studyType) {

		final List<SettingDetail> studyLevelConditions = this.userSelection.getStudyLevelConditions();
		final List<SettingDetail> basicDetails = this.userSelection.getBasicDetails();
		// transfer over data from user input into the list of setting details
		// stored in the session
		final List<SettingDetail> combinedList = new ArrayList<>();

		if (basicDetails != null) {
			combinedList.addAll(basicDetails);
		}

		if (studyLevelConditions != null) {
			combinedList.addAll(studyLevelConditions);
		}

		final String name = "";

		Workbook workbook;
		final StudyDetails details = new StudyDetails();

		if ("T".equalsIgnoreCase(studyType)) {

			final Dataset dataset =
					(Dataset) SettingsUtil.convertPojoToXmlDataset(this.fieldbookMiddlewareService, name, combinedList,
							this.userSelection.getPlotsLevelList(), this.userSelection.getBaselineTraitsList(), this.userSelection,
							this.userSelection.getTrialLevelVariableList(), this.userSelection.getTreatmentFactors(), null, null,
							this.userSelection.getNurseryConditions(), false, this.contextUtil.getCurrentProgramUUID());

			workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, false, this.contextUtil.getCurrentProgramUUID());

			details.setStudyType(StudyType.T);

		} else {

			final List<SettingDetail> variatesList = new ArrayList<>();

			if (this.userSelection.getBaselineTraitsList() != null) {
				variatesList.addAll(this.userSelection.getBaselineTraitsList());
			}

			if (this.userSelection.getSelectionVariates() != null) {
				variatesList.addAll(this.userSelection.getSelectionVariates());
			}

			final Dataset dataset =
					(Dataset) SettingsUtil.convertPojoToXmlDataset(this.fieldbookMiddlewareService, name, combinedList,
							this.userSelection.getPlotsLevelList(), variatesList, this.userSelection,
							this.userSelection.getTrialLevelVariableList(), this.userSelection.getTreatmentFactors(), null, null,
							this.userSelection.getNurseryConditions(), true, this.contextUtil.getCurrentProgramUUID());

			workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, true, this.contextUtil.getCurrentProgramUUID());

			details.setStudyType(StudyType.N);

		}

		workbook.setStudyDetails(details);

		this.userSelection.setTemporaryWorkbook(workbook);

	}

	protected void addExperimentDesign(final Workbook workbook, final Set<MeasurementVariable> experimentalDesignMeasurementVariables) {

		final ExpDesignParameterUi designParam = new ExpDesignParameterUi();
		designParam.setDesignType(3);

		final List<Integer> expDesignTermIds = new ArrayList<>();
		expDesignTermIds.add(TermId.EXPERIMENT_DESIGN_FACTOR.getId());

		this.userSelection.setExpDesignParams(designParam);
		this.userSelection.setExpDesignVariables(expDesignTermIds);

		final TermId termId = TermId.getById(TermId.EXPERIMENT_DESIGN_FACTOR.getId());

		SettingsUtil.addTrialCondition(termId, designParam, workbook, this.fieldbookMiddlewareService, this.getCurrentProject()
				.getUniqueID());

		workbook.getFactors().addAll(experimentalDesignMeasurementVariables);

		final ExperimentalDesignVariable expDesignVar = workbook.getExperimentalDesignVariables();
		if (expDesignVar != null && expDesignVar.getExperimentalDesign() != null) {
			for (final MeasurementVariable mvar : workbook.getConditions()) {
				if (mvar.getTermId() == termId.getId()) {
					mvar.setOperation(Operation.UPDATE);
				}
			}
		}

	}

	protected void addFactorsIfNecessary(final Workbook workbook, final DesignImportData designImportData) {

		if (workbook.getStudyDetails().getStudyType() == StudyType.T) {

			final Set<MeasurementVariable> uniqueFactors = new HashSet<>(workbook.getFactors());
			uniqueFactors.addAll(this.designImportService.extractMeasurementVariable(PhenotypicType.TRIAL_ENVIRONMENT,
					designImportData.getMappedHeaders()));

			for (final MeasurementVariable mvar : uniqueFactors) {
				final MeasurementVariable tempMvar = this.getMeasurementVariableInListByTermId(mvar.getTermId(), workbook.getConditions());
				if (tempMvar != null) {
					mvar.setOperation(tempMvar.getOperation());
					mvar.setName(tempMvar.getName());
				}
			}

			workbook.getFactors().clear();
			workbook.getFactors().addAll(new ArrayList<>(uniqueFactors));

		}

	}

	protected void addConditionsIfNecessary(final Workbook workbook, final DesignImportData designImportData) {

		if (workbook.getStudyDetails().getStudyType() == StudyType.N) {

			final Set<MeasurementVariable> uniqueConditions = new HashSet<>(workbook.getConditions());

			for (final MeasurementVariable mvar : this.designImportService.extractMeasurementVariable(PhenotypicType.TRIAL_ENVIRONMENT,
					designImportData.getMappedHeaders())) {
				if (mvar.getTermId() != TermId.TRIAL_INSTANCE_FACTOR.getId()) {
					uniqueConditions.add(mvar);
				}
			}

			workbook.getConditions().clear();
			workbook.getConditions().addAll(new ArrayList<>(uniqueConditions));

		}

	}

	protected void addVariates(final Workbook workbook, final DesignImportData designImportData) {
		final Set<MeasurementVariable> uniqueVariates = new HashSet<>(workbook.getVariates());
		uniqueVariates.addAll(this.designImportService.extractMeasurementVariable(PhenotypicType.VARIATE,
				designImportData.getMappedHeaders()));

		workbook.getVariates().clear();
		workbook.getVariates().addAll(new ArrayList<>(uniqueVariates));
	}

	protected void populateTrialLevelVariableListIfNecessary(final Workbook workbook) {
		// retrieve all trial level factors and convert them to setting details
		final Set<MeasurementVariable> trialLevelFactors = new HashSet<>();
		for (final MeasurementVariable factor : workbook.getFactors()) {
			if (PhenotypicType.TRIAL_ENVIRONMENT.getLabelList().contains(factor.getLabel())) {
				trialLevelFactors.add(factor);
			}
		}

		final List<SettingDetail> newDetails = new ArrayList<>();

		for (final MeasurementVariable mvar : trialLevelFactors) {
			final SettingDetail newDetail =
					this.createSettingDetail(mvar.getTermId(), mvar.getName(), PhenotypicType.TRIAL_ENVIRONMENT.name());
			newDetail.setRole(mvar.getRole());
			newDetail.setDeletable(true);
			newDetails.add(newDetail);
		}

		this.addNewSettingDetailsIfNecessary(newDetails);

	}

	protected void populateStudyLevelVariableListIfNecessary(final Workbook workbook, final EnvironmentData environmentData,
			final DesignImportData designImportData) {
		if (workbook.getStudyDetails().getStudyType() == StudyType.N) {

			final Map<String, String> managementDetailValues = environmentData.getEnvironments().get(0).getManagementDetailValues();

			final List<SettingDetail> newDetails = new ArrayList<>();

			for (final MeasurementVariable mvar : workbook.getConditions()) {
				final SettingDetail newDetail = this.createSettingDetail(mvar.getTermId(), mvar.getName(), PhenotypicType.STUDY.name());
				newDetail.setRole(mvar.getRole());

				final String value = managementDetailValues.get(String.valueOf(newDetail.getVariable().getCvTermId()));
				if (value != null) {
					newDetail.setValue(value);
				} else {
					newDetail.setValue("");
				}

				newDetail.getVariable().setOperation(mvar.getOperation());
				newDetails.add(newDetail);
			}

			this.resolveTheEnvironmentFactorsWithIDNamePairing(environmentData, designImportData, newDetails);

			this.userSelection.getStudyLevelConditions().clear();
			this.userSelection.getStudyLevelConditions().addAll(newDetails);
		}

	}

	protected void addNewSettingDetailsIfNecessary(final List<SettingDetail> newDetails) {

		if (this.userSelection.getTrialLevelVariableList() == null) {
			this.userSelection.setTrialLevelVariableList(new ArrayList<SettingDetail>());
		}

		for (final SettingDetail settingDetail : newDetails) {

			boolean isExisting = false;

			for (final SettingDetail settingDetailFromUserSelection : this.userSelection.getTrialLevelVariableList()) {
				if (settingDetail.getVariable().getCvTermId().intValue() == settingDetailFromUserSelection.getVariable().getCvTermId()
						.intValue()) {
					isExisting = true;
					break;
				}
			}

			if (!isExisting) {
				this.userSelection.getTrialLevelVariableList().add(settingDetail);
			}

		}

	}

	protected void createTrialObservations(final EnvironmentData environmentData, final Workbook workbook,
			final DesignImportData designImportData) {

		// get the Experiment Design MeasurementVariable
		final Set<MeasurementVariable> trialVariables = new HashSet<>(workbook.getTrialFactors());

		trialVariables.addAll(workbook.getConstants());

		for (final MeasurementVariable trialCondition : workbook.getTrialConditions()) {
			if (trialCondition.getTermId() == TermId.EXPERIMENT_DESIGN_FACTOR.getId()) {
				trialVariables.add(trialCondition);
			}
		}

		this.resolveTheEnvironmentFactorsWithIDNamePairing(environmentData, designImportData, trialVariables);

		final List<MeasurementRow> trialEnvironmentValues =
				WorkbookUtil.createMeasurementRowsFromEnvironments(environmentData.getEnvironments(), new ArrayList<>(trialVariables),
						this.userSelection.getExpDesignParams());

		workbook.setTrialObservations(trialEnvironmentValues);

		if (workbook.getStudyDetails().getStudyType() == StudyType.T) {
			this.fieldbookService.addConditionsToTrialObservationsIfNecessary(workbook);
		}

	}

	protected Map<String, Object> generateDatatableDataMap(final MeasurementRow row, final String suffix) {
		final Map<String, Object> dataMap = new HashMap<>();
		// the 4 attributes are needed always
		for (final MeasurementData data : row.getDataList()) {
			String displayVal = data.getDisplayValue();
			if (suffix != null) {
				displayVal += suffix;
			}

			if (data.getMeasurementVariable().getDataTypeId().equals(TermId.CATEGORICAL_VARIABLE.getId())
					|| data.getMeasurementVariable().getDataTypeId().equals(TermId.NUMERIC_VARIABLE.getId())) {
				final Object[] categArray = new Object[] {displayVal, data.isAccepted()};
				dataMap.put(data.getMeasurementVariable().getName(), categArray);
			} else {
				dataMap.put(data.getMeasurementVariable().getName(), displayVal);
			}
		}
		return dataMap;
	}

	protected void performAutomap(final DesignImportData designImportData) {
		final Map<PhenotypicType, List<DesignHeaderItem>> result =
				this.designImportService.categorizeHeadersByPhenotype(designImportData.getUnmappedHeaders());

		designImportData.setUnmappedHeaders(result.get(PhenotypicType.UNASSIGNED));

		// removed unmapped headers before assigning to the mappedHeaders field of designImportData
		result.remove(PhenotypicType.UNASSIGNED);

		designImportData.setMappedHeaders(result);

	}

	protected void processEnvironmentData(final EnvironmentData data) {
		for (int i = 0; i < data.getEnvironments().size(); i++) {
			final Map<String, String> values = data.getEnvironments().get(i).getManagementDetailValues();
			if (!values.containsKey(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()))) {
				values.put(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()), Integer.toString(i + 1));
			} else if (values.get(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId())) == null
					|| values.get(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId())).isEmpty()) {
				values.put(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()), Integer.toString(i + 1));
			}
		}
	}

	protected void resolveTheEnvironmentFactorsWithIDNamePairing(final EnvironmentData environmentData,
			final DesignImportData designImportData, final Set<MeasurementVariable> trialVariables) {

		final Map<String, String> idNameMap = AppConstants.ID_NAME_COMBINATION.getMapOfValues();
		final Map<String, String> nameIdMap = this.switchKey(idNameMap);

		for (final Environment environment : environmentData.getEnvironments()) {

			final Map<String, String> copyOfManagementDetailValues = new HashMap<>();
			copyOfManagementDetailValues.putAll(environment.getManagementDetailValues());

			for (final Entry<String, String> managementDetail : environment.getManagementDetailValues().entrySet()) {

				String headerName =
						this.getLocalNameFromSettingDetails(Integer.valueOf(managementDetail.getKey()),
								this.userSelection.getTrialLevelVariableList());
				String standardVariableName =
						this.getVariableNameFromSettingDetails(Integer.valueOf(managementDetail.getKey()),
								this.userSelection.getTrialLevelVariableList());

				if ("".equals(headerName)) {

					headerName =
							this.getHeaderName(Integer.valueOf(managementDetail.getKey()),
									designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));
				}

				if ("".equals(standardVariableName)) {
					standardVariableName =
							this.getStandardVariableName(Integer.valueOf(managementDetail.getKey()), designImportData.getMappedHeaders()
									.get(PhenotypicType.TRIAL_ENVIRONMENT));
				}

				// For TRIAL_LOCATION (Location Name)
				if (Integer.valueOf(managementDetail.getKey()) == TermId.TRIAL_LOCATION.getId()) {
					final String termId = nameIdMap.get(managementDetail.getKey().toUpperCase());
					if (termId != null) {

						if (this.isTermIdExisting(Integer.valueOf(managementDetail.getKey()),
								designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT))) {
							final Location location =
									this.fieldbookMiddlewareService.getLocationByName(managementDetail.getValue(), Operation.EQUAL);
							if (location == null) {
								copyOfManagementDetailValues.put(termId, "");
							} else {
								copyOfManagementDetailValues.put(termId, String.valueOf(location.getLocid()));
							}
						}

						final SettingDetail settingDetail =
								this.createSettingDetail(Integer.valueOf(termId), headerName, VariableType.ENVIRONMENT_DETAIL.name());
						settingDetail.setRole(PhenotypicType.TRIAL_ENVIRONMENT);
						this.addSettingDetailToTrialLevelVariableListIfNecessary(settingDetail);

						MeasurementVariable measurementVariable = null;
						final StandardVariable var =
								this.ontologyDataManager.getStandardVariable(Integer.valueOf(termId), this.getCurrentProject()
										.getUniqueID());
						var.setPhenotypicType(PhenotypicType.TRIAL_ENVIRONMENT);
						measurementVariable =
								ExpDesignUtil.convertStandardVariableToMeasurementVariable(var, Operation.ADD, this.fieldbookService);
						measurementVariable.setName(standardVariableName + AppConstants.ID_SUFFIX.getString());
						measurementVariable.setRole(PhenotypicType.TRIAL_ENVIRONMENT);
						trialVariables.add(measurementVariable);

						copyOfManagementDetailValues.remove(managementDetail.getKey());

						SettingsUtil.hideVariableInSession(this.userSelection.getTrialLevelVariableList(),
								Integer.valueOf(managementDetail.getKey()));
					}
				} else if (Integer.valueOf(managementDetail.getKey()) == 8373) {
					final String termId = nameIdMap.get(managementDetail.getKey());
					if (termId != null) {

						copyOfManagementDetailValues.put(termId, String.valueOf(super.getCurrentIbdbUserId()));

						final SettingDetail settingDetail =
								this.createSettingDetail(Integer.valueOf(termId), headerName, VariableType.ENVIRONMENT_DETAIL.name());
						settingDetail.setRole(PhenotypicType.TRIAL_ENVIRONMENT);
						this.addSettingDetailToTrialLevelVariableListIfNecessary(settingDetail);

						MeasurementVariable measurementVariable = null;
						final StandardVariable var =
								this.ontologyDataManager.getStandardVariable(Integer.valueOf(termId), this.getCurrentProject()
										.getUniqueID());
						var.setPhenotypicType(PhenotypicType.TRIAL_ENVIRONMENT);
						measurementVariable =
								ExpDesignUtil.convertStandardVariableToMeasurementVariable(var, Operation.ADD, this.fieldbookService);
						measurementVariable.setName(standardVariableName + AppConstants.ID_SUFFIX.getString());
						measurementVariable.setRole(PhenotypicType.TRIAL_ENVIRONMENT);
						trialVariables.add(measurementVariable);

						copyOfManagementDetailValues.remove(managementDetail.getKey());

						SettingsUtil.hideVariableInSession(this.userSelection.getTrialLevelVariableList(),
								Integer.valueOf(managementDetail.getKey()));
					}

				} else {
					MeasurementVariable measurementVariable = null;
					final StandardVariable var =
							this.ontologyDataManager.getStandardVariable(Integer.valueOf(managementDetail.getKey()), this
									.getCurrentProject().getUniqueID());

					var.setPhenotypicType(PhenotypicType.TRIAL_ENVIRONMENT);
					measurementVariable =
							ExpDesignUtil.convertStandardVariableToMeasurementVariable(var, Operation.ADD, this.fieldbookService);
					measurementVariable.setName(var.getName());
					trialVariables.add(measurementVariable);
				}

				// For Categorical Variables
				final StandardVariable tempStandardVarable =
						this.ontologyDataManager.getStandardVariable(Integer.valueOf(managementDetail.getKey()), this.getCurrentProject()
								.getUniqueID());
				tempStandardVarable.setPhenotypicType(PhenotypicType.TRIAL_ENVIRONMENT);
				if (tempStandardVarable != null && tempStandardVarable.hasEnumerations()) {

					final Enumeration enumeration =
							this.findInEnumeration(managementDetail.getValue(), tempStandardVarable.getEnumerations());
					if (enumeration != null) {
						copyOfManagementDetailValues.put(managementDetail.getKey(), String.valueOf(enumeration.getId()));
					}
				}

			}

			environment.getManagementDetailValues().clear();
			environment.getManagementDetailValues().putAll(copyOfManagementDetailValues);

		}

	}

	protected void resolveTheEnvironmentFactorsWithIDNamePairing(final EnvironmentData environmentData,
			final DesignImportData designImportData, final List<SettingDetail> newDetails) {

		final Map<String, String> idNameMap = AppConstants.ID_NAME_COMBINATION.getMapOfValues();
		final Map<String, String> nameIdMap = this.switchKey(idNameMap);

		final Environment environment = environmentData.getEnvironments().get(0);

		final Map<String, String> copyOfManagementDetailValues = new HashMap<>();
		copyOfManagementDetailValues.putAll(environment.getManagementDetailValues());

		for (final Entry<String, String> managementDetail : environment.getManagementDetailValues().entrySet()) {

			// For TRIAL_LOCATION (Location Name)
			if (Integer.valueOf(managementDetail.getKey()) == TermId.TRIAL_LOCATION.getId()) {
				final String termId = nameIdMap.get(managementDetail.getKey());
				if (termId != null) {

					final Location location =
							this.fieldbookMiddlewareService.getLocationByName(managementDetail.getValue(), Operation.EQUAL);
					if (location != null) {
						copyOfManagementDetailValues.put(termId, String.valueOf(location.getLocid()));
					} else {
						copyOfManagementDetailValues.put(termId, "");
					}

					final String headerName =
							this.getHeaderName(Integer.valueOf(managementDetail.getKey()),
									designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));

					final SettingDetail settingDetail =
							this.createSettingDetail(Integer.valueOf(termId), headerName, VariableType.STUDY_DETAIL.name());
					settingDetail.setRole(PhenotypicType.STUDY);
					settingDetail.getVariable().setOperation(Operation.ADD);
					settingDetail.setValue("");
					if (location != null) {
						settingDetail.setValue(String.valueOf(location.getLocid()));
					}

					this.addSettingOrUpdateDetailToTargetListIfNecessary(settingDetail, newDetails);

					copyOfManagementDetailValues.remove(managementDetail.getKey());

				}
			}

			// For COOPERATOR and PI_NAME
			if (Integer.valueOf(managementDetail.getKey()) == 8373 || Integer.valueOf(managementDetail.getKey()) == 8100) {
				final String termId = nameIdMap.get(managementDetail.getKey());
				if (termId != null) {

					copyOfManagementDetailValues.put(termId, String.valueOf(super.getCurrentIbdbUserId()));

					final String headerName =
							this.getHeaderName(Integer.valueOf(managementDetail.getKey()),
									designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));

					final SettingDetail settingDetail =
							this.createSettingDetail(Integer.valueOf(termId), headerName, VariableType.STUDY_DETAIL.name());
					settingDetail.setRole(PhenotypicType.STUDY);
					settingDetail.getVariable().setOperation(Operation.ADD);
					settingDetail.setValue(String.valueOf(super.getCurrentIbdbUserId()));

					this.addSettingOrUpdateDetailToTargetListIfNecessary(settingDetail, newDetails);

					copyOfManagementDetailValues.remove(managementDetail.getKey());

				}

			}

			// For Categorical Variables
			final StandardVariable tempStandardVarable =
					this.ontologyDataManager.getStandardVariable(Integer.valueOf(managementDetail.getKey()), this.getCurrentProject()
							.getUniqueID());
			if (tempStandardVarable != null && tempStandardVarable.hasEnumerations()) {

				final Enumeration enumeration = this.findInEnumeration(managementDetail.getValue(), tempStandardVarable.getEnumerations());
				if (enumeration != null) {
					copyOfManagementDetailValues.put(managementDetail.getKey(), String.valueOf(enumeration.getId()));
				}
			}

		}

		environment.getManagementDetailValues().clear();
		environment.getManagementDetailValues().putAll(copyOfManagementDetailValues);

	}

	protected Enumeration findInEnumeration(final String value, final List<Enumeration> enumerations) {
		for (final Enumeration enumeration : enumerations) {
			if (enumeration.getName().equalsIgnoreCase(value)) {
				return enumeration;
			} else if (enumeration.getDescription().equalsIgnoreCase(value)) {
				return enumeration;
			}
		}
		return null;
	}

	protected void addSettingDetailToTrialLevelVariableListIfNecessary(final SettingDetail settingDetail) {

		for (final SettingDetail sd : this.userSelection.getTrialLevelVariableList()) {
			if (sd.getVariable().getCvTermId().intValue() == settingDetail.getVariable().getCvTermId().intValue()) {
				return;
			}
		}
		this.userSelection.getTrialLevelVariableList().add(settingDetail);
	}

	protected void addSettingOrUpdateDetailToTargetListIfNecessary(final SettingDetail settingDetail, final List<SettingDetail> targetList) {

		final Iterator<SettingDetail> iterator = targetList.iterator();
		while (iterator.hasNext()) {
			final SettingDetail sd = iterator.next();
			if (sd.getVariable().getCvTermId().intValue() == settingDetail.getVariable().getCvTermId().intValue()) {
				settingDetail.getVariable().setOperation(Operation.UPDATE);
				iterator.remove();
			}
		}

		targetList.add(settingDetail);

	}

	protected boolean isTermIdExisting(final int termId, final List<DesignHeaderItem> items) {
		for (final DesignHeaderItem item : items) {
			if (item.getId() == termId) {
				return true;
			}
		}
		return false;
	}

	protected String getHeaderName(final int termId, final List<DesignHeaderItem> items) {
		for (final DesignHeaderItem item : items) {
			if (item.getId() == termId) {
				return item.getName();
			}
		}
		return "";
	}

	protected String getStandardVariableName(final Integer termId, final List<DesignHeaderItem> items) {
		for (final DesignHeaderItem item : items) {
			if (item.getId() == termId) {
				return item.getVariable().getName();
			}
		}
		return "";
	}

	protected Map<String, String> switchKey(final Map<String, String> map) {
		final Map<String, String> newMap = new HashMap<>();
		for (final Entry<String, String> entry : map.entrySet()) {
			newMap.put(entry.getValue(), entry.getKey());
		}
		return newMap;
	}

	protected MeasurementVariable getMeasurementVariableInListByTermId(final int termid, final List<MeasurementVariable> list) {
		for (final MeasurementVariable mvar : list) {
			if (termid == mvar.getTermId()) {
				return mvar;
			}
		}

		return null;
	}

	protected void removeExperimentDesignVariables(final List<MeasurementVariable> variables) {
		final Iterator<MeasurementVariable> iterator = variables.iterator();
		while (iterator.hasNext()) {
			final MeasurementVariable variable = iterator.next();
			if (variable.getRole() == PhenotypicType.TRIAL_DESIGN) {
				iterator.remove();
			}
		}
	}

	protected String getLocalNameFromSettingDetails(final int termId, final List<SettingDetail> settingDetails) {
		for (final SettingDetail detail : settingDetails) {
			if (detail.getVariable().getCvTermId().intValue() == termId) {
				return detail.getVariable().getName();
			}
		}
		return "";
	}

	protected String getVariableNameFromSettingDetails(final int termId, final List<SettingDetail> settingDetails) {
		for (final SettingDetail detail : settingDetails) {
			if (detail.getVariable().getCvTermId().intValue() == termId) {
				return detail.getVariable().getName();
			}
		}
		return "";
	}

	/**
	 * Create check variables to be deleted.
	 * 
	 * @param studyLevelConditions
	 */
	protected void addCheckVariablesToDeleted(final List<SettingDetail> studyLevelConditions) {

		studyLevelConditions.add(this.createCheckVariableToBeDeleted(TermId.CHECK_START.getId(), "CHECK_START"));
		studyLevelConditions.add(this.createCheckVariableToBeDeleted(TermId.CHECK_PLAN.getId(), "CHECK_PLAN"));
		studyLevelConditions.add(this.createCheckVariableToBeDeleted(TermId.CHECK_INTERVAL.getId(), "CHECK_INTERVAL"));

	}

	protected SettingDetail createCheckVariableToBeDeleted(final int checkTermId, final String name) {

		final String programUUID = this.contextUtil.getCurrentProgramUUID();
		final int currentIbDbUserId = this.contextUtil.getCurrentUserLocalId();

		final SettingDetail checkSettingDetail =
				this.settingsService.createSettingDetail(checkTermId, name, this.userSelection, currentIbDbUserId, programUUID);

		checkSettingDetail.getVariable().setOperation(Operation.DELETE);
		checkSettingDetail.setRole(PhenotypicType.TRIAL_ENVIRONMENT);

		return checkSettingDetail;

	}

	protected boolean hasCheckVariables(final List<MeasurementVariable> conditions) {
		if (conditions != null && !conditions.isEmpty()) {
			final StringTokenizer tokenizer = new StringTokenizer(AppConstants.CHECK_VARIABLES.getString(), ",");
			for (final MeasurementVariable var : conditions) {
				while (tokenizer.hasMoreTokens()) {
					if (Integer.valueOf(tokenizer.nextToken()).intValue() == var.getTermId()) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
