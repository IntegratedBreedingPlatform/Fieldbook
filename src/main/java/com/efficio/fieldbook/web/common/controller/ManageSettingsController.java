package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Resource;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.ontology.api.OntologyPropertyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.generationcp.middleware.service.api.study.StudyService;
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

import com.efficio.fieldbook.web.common.bean.PropertyTreeSummary;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.nursery.controller.SettingsController;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.ontology.form.OntologyDetailsForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 * <p/>
 * This controller class handles back end functionality that are common to the operations that require management of settings (Create/Edit
 * Nursery/Trial)
 */

@Controller
@RequestMapping(value = ManageSettingsController.URL)
public class ManageSettingsController extends SettingsController {

	public static final String URL = "/manageSettings";

	public static final String DETAILS_TEMPLATE = "/OntologyBrowser/detailTab";

	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ManageSettingsController.class);

	@Resource
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Resource
	private OntologyPropertyDataManager ontologyPropertyDataManager;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	protected StudyService studyService;

	@ResponseBody
	@RequestMapping(value = "/settings/role/{roleId}", method = RequestMethod.GET,
					produces = "application/json; charset=utf-8")
	public List<PropertyTreeSummary> getOntologyPropertiesByRole(@PathVariable Integer roleId) {
		assert !Objects.equals(roleId, null);

		PhenotypicType phenotypicTypeById = PhenotypicType.getPhenotypicTypeById(roleId);

		assert !Objects.equals(phenotypicTypeById, null);

		Set<Integer> variableTypes = VariableType.getVariableTypesIdsByPhenotype(phenotypicTypeById);

		return getOntologyPropertiesByVariableType(variableTypes.toArray(new Integer[variableTypes.size()]), null, false, true);
	}

	@ResponseBody
	@RequestMapping(value = "/settings/properties", method = RequestMethod.GET,
					produces = "application/json; charset=utf-8")
	public List<PropertyTreeSummary> getOntologyPropertiesByVariableType(
			@RequestParam(value = "type", required = true) Integer[] variableTypes,
			@RequestParam(value = "classes", required = false) String[] classes, @RequestParam(required = false) boolean isTrial,
			@RequestParam(required = false) boolean showHiddenVariables) {
		
		
		// HACK! Workaround if callie is from design import
		List<Integer> correctedVarTypes = new ArrayList<>();
		for (Integer varType : variableTypes) {
			// this is not a varType but a phenotype
			if (!varType.toString().startsWith("18")) {
				PhenotypicType phenotypicTypeById = PhenotypicType.getPhenotypicTypeById(varType);
				correctedVarTypes.addAll(VariableType
						.getVariableTypesIdsByPhenotype(phenotypicTypeById));
			} else {
				correctedVarTypes.add(varType);
			}
		}
		
		List<PropertyTreeSummary> propertyTreeList = new ArrayList<>();

		try {
			Set<VariableType> selectedVariableTypes = new HashSet<>();
			List<String> varTypeValues = new ArrayList<>();
			for (Integer varType : correctedVarTypes) {
				selectedVariableTypes.add(VariableType.getById(varType));
				varTypeValues.add(VariableType.getById(varType).getName());
			}

			List<Property> properties;

			properties = ontologyPropertyDataManager
					.getAllPropertiesWithClassAndVariableType(classes, varTypeValues.toArray(new String[varTypeValues.size()]));

			// fetch all standard variables given property
			for (Property property : properties) {
				VariableFilter variableFilterOptions = new VariableFilter();
				variableFilterOptions.setProgramUuid(contextUtil.getCurrentProgramUUID());
				variableFilterOptions.addPropertyId(property.getId());

				variableFilterOptions.getVariableTypes().addAll(selectedVariableTypes);

				if (!showHiddenVariables) {
					variableFilterOptions.getExcludedVariableIds().addAll(filterOutVariablesByVariableType(selectedVariableTypes, isTrial));
				}

				List<Variable> ontologyList = ontologyVariableDataManager.getWithFilter(variableFilterOptions);

				if (ontologyList.isEmpty()) {
					continue;
				}

				if (selectedVariableTypes.contains(VariableType.TREATMENT_FACTOR)) {
					ontologyVariableDataManager.processTreatmentFactorHasPairValue(ontologyList,
							AppConstants.CREATE_TRIAL_REMOVE_TREATMENT_FACTOR_IDS.getIntegerList());
				}


				PropertyTreeSummary propertyTree = new PropertyTreeSummary(property, ontologyList);
				propertyTreeList.add(propertyTree);

			}

			// Todo: what to make of this.fieldbookMiddlewareService.filterStandardVariablesByIsAIds(...)

		} catch (MiddlewareException e) {
			LOG.error(e.getMessage(), e);
		}

		return propertyTreeList;
	}

	/**
	 * Gets the ontology details.
	 *
	 * @param variableTypeId
	 * @param variableId
	 * @param model
	 * @param variableDetails
	 * @return detailTab.html
	 */
	@RequestMapping(value = "/settings/details/{variableTypeId}/{variableId}", method = RequestMethod.GET)
	public String getOntologyDetails(@PathVariable int variableTypeId, @PathVariable int variableId, Model model,
			@ModelAttribute("variableDetails") OntologyDetailsForm variableDetails) {
		try {
			Variable ontologyVariable =
					this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(), variableId, true, false);

			if (!Objects.equals(ontologyVariable, null)) {
				variableDetails.setVariable(ontologyVariable);
				variableDetails.setCurrentVariableType(VariableType.getById(variableTypeId));

			}

		} catch (MiddlewareException e) {
			ManageSettingsController.LOG.error(e.getMessage(), e);
		}
		return super.showAjaxPage(model, ManageSettingsController.DETAILS_TEMPLATE);
	}

	private List<Integer> filterOutVariablesByVariableType(Set<VariableType> selectedVariableTypes, boolean isTrial) {
		List<Integer> cvTermIDs = new ArrayList<>();

		for (VariableType varType : selectedVariableTypes) {
			switch (varType) {
				case STUDY_DETAIL:
					cvTermIDs.addAll(AppConstants.HIDE_STUDY_DETAIL_VARIABLES.getIntegerList());
					break;
				case SELECTION_METHOD:
					cvTermIDs.addAll(AppConstants.HIDE_ID_VARIABLES.getIntegerList());
					break;
				case ENVIRONMENT_DETAIL:
					cvTermIDs.addAll(AppConstants.HIDE_TRIAL_VARIABLES.getIntegerList());

					if (isTrial) {
						cvTermIDs.addAll(AppConstants.HIDE_TRIAL_ENVIRONMENT_FIELDS.getIntegerList());
						cvTermIDs.addAll(AppConstants.HIDE_TRIAL_ENVIRONMENT_FIELDS_FROM_POPUP.getIntegerList());
					}
					break;
				case TREATMENT_FACTOR:
					cvTermIDs.addAll(AppConstants.CREATE_TRIAL_REMOVE_TREATMENT_FACTOR_IDS.getIntegerList());
					break;
				default:
					cvTermIDs.addAll(AppConstants.HIDE_PLOT_FIELDS.getIntegerList());
					break;
			}
		}

		return cvTermIDs;
	}

	/**
	 * Adds the settings.
	 *
	 * @param form the form
	 * @param mode the mode
	 * @return the string
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value = "/addSettings/{mode}", method = RequestMethod.POST)
	public List<SettingDetail> addSettings(@RequestBody final CreateNurseryForm form, @PathVariable final int mode) {
		final List<SettingDetail> newSettings = new ArrayList<SettingDetail>();
		try {
			final List<SettingVariable> selectedVariables = form.getSelectedVariables();
			if (selectedVariables != null && !selectedVariables.isEmpty()) {
				for (final SettingVariable var : selectedVariables) {
					final Operation operation = this.removeVarFromDeletedList(var, mode);

					var.setOperation(operation);
					this.populateSettingVariable(var);
					final List<ValueReference> possibleValues = this.fieldbookService.getAllPossibleValues(var.getCvTermId());
					final SettingDetail newSetting = new SettingDetail(var, possibleValues, null, true);
					final List<ValueReference> possibleValuesFavoriteFiltered = this.fieldbookService
							.getAllPossibleValuesFavorite(var.getCvTermId(), this.getCurrentProject().getUniqueID(), true);

					final List<ValueReference> allValues = this.fieldbookService.getAllPossibleValuesWithFilter(var.getCvTermId(), false);

					final List<ValueReference> allFavoriteValues = this.fieldbookService.getAllPossibleValuesFavorite(var.getCvTermId(),
							this.getCurrentProject().getUniqueID(), null);
					
					
					final List<ValueReference>  intersection = SettingsUtil.intersection(allValues, allFavoriteValues);
					
					newSetting.setAllFavoriteValues(intersection);
					newSetting.setAllFavoriteValuesToJson(intersection);

					newSetting.setPossibleValuesFavorite(possibleValuesFavoriteFiltered);
					newSetting.setAllValues(allValues);

					newSetting.setPossibleValuesToJson(possibleValues);
					newSetting.setPossibleValuesFavoriteToJson(possibleValuesFavoriteFiltered);
					newSetting.setAllValuesToJson(allValues);
					newSettings.add(newSetting);
				}
			}

			if (newSettings != null && !newSettings.isEmpty()) {
				this.addNewSettingDetails(mode, newSettings);
				return newSettings;
			}

		} catch (final Exception e) {
			ManageSettingsController.LOG.error(e.getMessage(), e);
		}

		return new ArrayList<SettingDetail>();
	}

	/**
	 * Adds the new setting details.
	 *
	 * @param mode       the mode
	 * @param newDetails the new details
	 * @throws Exception the exception
	 */
	private void addNewSettingDetails(int mode, List<SettingDetail> newDetails) throws Exception {
		SettingsUtil.addNewSettingDetails(mode, newDetails, userSelection);
	}

	private Operation removeVarFromDeletedList(SettingVariable var, int mode) {
		List<SettingDetail> settingsList = new ArrayList<SettingDetail>();
		if (mode == VariableType.STUDY_DETAIL.getId()) {
			settingsList = this.userSelection.getDeletedStudyLevelConditions();
		} else if (mode == VariableType.EXPERIMENTAL_DESIGN.getId() || mode == VariableType.GERMPLASM_DESCRIPTOR.getId()) {
			settingsList = this.userSelection.getDeletedPlotLevelList();
		} else if (mode == VariableType.TRAIT.getId() || mode == VariableType.SELECTION_METHOD.getId()) {
			settingsList = this.userSelection.getDeletedBaselineTraitsList();
		} else if (mode == VariableType.NURSERY_CONDITION.getId()) {
			settingsList = this.userSelection.getDeletedNurseryConditions();
		} else if (mode == VariableType.TREATMENT_FACTOR.getId()) {
			settingsList = this.userSelection.getDeletedTreatmentFactors();
		} else if (mode == VariableType.ENVIRONMENT_DETAIL.getId()) {
			settingsList = this.userSelection.getDeletedTrialLevelVariables();
		}

		Operation operation = Operation.ADD;
		if (settingsList != null) {
			Iterator<SettingDetail> iter = settingsList.iterator();
			while (iter.hasNext()) {
				SettingVariable deletedVariable = iter.next().getVariable();
				if (deletedVariable.getCvTermId().equals(Integer.valueOf(var.getCvTermId()))) {
					operation = deletedVariable.getOperation();
					iter.remove();
				}
			}
		}
		return operation;
	}

	@ResponseBody
	@RequestMapping(value = "/deleteVariable/{mode}", method = RequestMethod.POST)
	public boolean deleteVariable(@PathVariable int mode, @RequestBody List<Integer> ids) {

		for (Integer id : ids) {
			this.deleteVariable(mode, id);
		}

		return true;
	}

	@ResponseBody
	@RequestMapping(value = "/deleteVariable/{mode}/{variableId}", method = RequestMethod.POST)
	public ResponseEntity<String> deleteVariable(@PathVariable int mode, @PathVariable int variableId) {
		try {
			Map<String, String> idNameRetrieveSaveMap = this.fieldbookService.getIdNamePairForRetrieveAndSave();
			if (mode == VariableType.STUDY_DETAIL.getId()) {

				this.addVariableInDeletedList(userSelection.getStudyLevelConditions(), mode, variableId, true);
				SettingsUtil.deleteVariableInSession(userSelection.getStudyLevelConditions(), variableId);
				if (idNameRetrieveSaveMap.get(variableId) != null) {
					//special case so we must delete it as well
					this.addVariableInDeletedList(userSelection.getStudyLevelConditions(), mode,
							Integer.parseInt(idNameRetrieveSaveMap.get(variableId)), true);
					SettingsUtil.deleteVariableInSession(this.userSelection.getStudyLevelConditions(),
							Integer.parseInt(idNameRetrieveSaveMap.get(variableId)));
				}
			} else if (mode == VariableType.EXPERIMENTAL_DESIGN.getId() || mode == VariableType.GERMPLASM_DESCRIPTOR.getId()) {
				this.addVariableInDeletedList(this.userSelection.getPlotsLevelList(), mode, variableId, true);
				SettingsUtil.deleteVariableInSession(this.userSelection.getPlotsLevelList(), variableId);
			} else if (mode == VariableType.TRAIT.getId()) {
				this.addVariableInDeletedList(this.userSelection.getBaselineTraitsList(), mode, variableId, true);
				SettingsUtil.deleteVariableInSession(this.userSelection.getBaselineTraitsList(), variableId);
			} else if (mode == VariableType.SELECTION_METHOD.getId()) {
				this.addVariableInDeletedList(this.userSelection.getSelectionVariates(), mode, variableId, true);
				SettingsUtil.deleteVariableInSession(this.userSelection.getSelectionVariates(), variableId);
			} else if (mode == VariableType.NURSERY_CONDITION.getId() || mode == VariableType.TRIAL_CONDITION.getId()) {
				this.addVariableInDeletedList(this.userSelection.getNurseryConditions(), mode, variableId, true);
				SettingsUtil.deleteVariableInSession(this.userSelection.getNurseryConditions(), variableId);
			} else if (mode == VariableType.TREATMENT_FACTOR.getId()) {
				this.addVariableInDeletedList(this.userSelection.getTreatmentFactors(), mode, variableId, true);
				SettingsUtil.deleteVariableInSession(this.userSelection.getTreatmentFactors(), variableId);
			} else {
				this.addVariableInDeletedList(this.userSelection.getTrialLevelVariableList(), mode, variableId, true);
				SettingsUtil.deleteVariableInSession(this.userSelection.getTrialLevelVariableList(), variableId);
			}
		} catch (MiddlewareException e) {
			LOG.error(e.getMessage(), e);
			return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>("", HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/deleteTreatmentFactorVariable", method = RequestMethod.POST)
	public String deleteTreatmentFactorVariable(@RequestBody Map<String, Integer> ids) {
		Integer levelID = ids.get("levelID");
		Integer valueID = ids.get("valueID");
		if (levelID != null && levelID != 0) {
			this.deleteVariable(VariableType.TREATMENT_FACTOR.getId(), levelID);
		}

		if (valueID != null && valueID != 0) {
			this.deleteVariable(VariableType.TREATMENT_FACTOR.getId(), valueID);
		}

		return "";
	}

	@ResponseBody
	@RequestMapping(value = "/hasMeasurementData/{mode}", method = RequestMethod.POST)
	@Transactional
	public boolean hasMeasurementData(@RequestBody List<Integer> ids, @PathVariable int mode) {
		return this.checkModeAndHasMeasurementDataEntered(mode, ids, this.userSelection.getWorkbook().getStudyDetails().getId());
	}

	@ResponseBody
	@RequestMapping(value = "/hasMeasurementData/environmentNo/{environmentNo}", method = RequestMethod.POST)
	@Transactional
	public boolean hasMeasurementDataOnEnvironment(@RequestBody List<Integer> ids, @PathVariable int environmentNo) {

		return this.studyService
			.hasMeasurementDataOnEnvironment(this.userSelection.getWorkbook().getStudyDetails().getId(), environmentNo);
	}

	protected boolean checkModeAndHasMeasurementData(int mode, int variableId) {
		return mode == VariableType.TRAIT.getId() && this.userSelection.getMeasurementRowList() != null && !this.userSelection
				.getMeasurementRowList().isEmpty() && this.hasMeasurementDataEntered(variableId);
	}

	protected boolean checkModeAndHasMeasurementDataEntered(final int mode, final List<Integer> ids, final Integer studyId) {
		return mode == VariableType.TRAIT.getId() && this.studyService.hasMeasurementDataEntered(ids, studyId);
	}

	@Override
	public String getContentName() {
		return null;
	}
}
