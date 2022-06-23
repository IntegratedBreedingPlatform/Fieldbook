package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.service.api.SettingsService;
import com.efficio.fieldbook.web.common.bean.PropertyTreeSummary;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.trial.controller.SettingsController;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.derivedvariable.DerivedVariableUtils;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.ontology.api.OntologyPropertyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.generationcp.middleware.service.api.study.StudyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

	private static final List<Integer> GERMPLASM_DESCRIPTOR_VARIABLE_IDS_ALLOWED = Arrays.asList(8377, 8250, 8240, 8330, 8340, 8235, 8378, 8201);

	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ManageSettingsController.class);

	@Resource
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Resource
	private OntologyPropertyDataManager ontologyPropertyDataManager;

	@Resource
	protected StudyService studyService;

	@Resource
	protected SettingsService settingsService;

	@Resource
	protected FormulaService formulaService;

	@ResponseBody
	@RequestMapping(value = "/settings/role/{roleId}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public List<PropertyTreeSummary> getOntologyPropertiesByRole(@PathVariable final Integer roleId) {
		assert !Objects.equals(roleId, null);

		final PhenotypicType phenotypicTypeById = PhenotypicType.getPhenotypicTypeById(roleId);

		assert !Objects.equals(phenotypicTypeById, null);

		final Set<Integer> variableTypes = VariableType.getVariableTypesIdsByPhenotype(phenotypicTypeById);

		return getOntologyPropertiesByVariableType(variableTypes.toArray(new Integer[0]), null, true);
	}

	@ResponseBody
	@RequestMapping(value = "/settings/properties", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public List<PropertyTreeSummary> getOntologyPropertiesByVariableType(
			@RequestParam(value = "type", required = true) final Integer[] variableTypes,
			@RequestParam(value = "classes", required = false) final String[] classes,
			@RequestParam(required = false) final boolean showHiddenVariables) {

		// HACK! Workaround if callie is from design import
		final List<Integer> correctedVarTypes = new ArrayList<>();
		for (final Integer varType : variableTypes) {
			// this is not a varType but a phenotype
			if (!varType.toString().startsWith("18")) {
				final PhenotypicType phenotypicTypeById = PhenotypicType.getPhenotypicTypeById(varType);
				correctedVarTypes.addAll(VariableType.getVariableTypesIdsByPhenotype(phenotypicTypeById));
			} else {
				correctedVarTypes.add(varType);
			}
		}

		final List<PropertyTreeSummary> propertyTreeList = new ArrayList<>();

		try {
			final Set<VariableType> selectedVariableTypes = new HashSet<>();
			final List<String> varTypeValues = new ArrayList<>();
			for (final Integer varType : correctedVarTypes) {
				selectedVariableTypes.add(VariableType.getById(varType));
				varTypeValues.add(VariableType.getById(varType).getName());
			}

			final List<Property> properties;

			properties =
					ontologyPropertyDataManager.getAllPropertiesWithClassAndVariableType(classes, varTypeValues.toArray(new String[0]));

			// fetch all standard variables given property
			for (final Property property : properties) {
				final VariableFilter variableFilterOptions = new VariableFilter();
				variableFilterOptions.setProgramUuid(contextUtil.getCurrentProgramUUID());
				variableFilterOptions.addPropertyId(property.getId());

				variableFilterOptions.getVariableTypes().addAll(selectedVariableTypes);

				if (!showHiddenVariables) {
					variableFilterOptions.getExcludedVariableIds().addAll(filterOutVariablesByVariableType(selectedVariableTypes));
				}

				final List<Variable> ontologyList = ontologyVariableDataManager.getWithFilter(variableFilterOptions);

				if (ontologyList.isEmpty()) {
					continue;
				}

				List variableToRemove = new ArrayList<>();
				for (final Variable variable : ontologyList) {
					if (selectedVariableTypes.contains(VariableType.GERMPLASM_DESCRIPTOR) && !ManageSettingsController.GERMPLASM_DESCRIPTOR_VARIABLE_IDS_ALLOWED.contains(variable.getId())) {
						variableToRemove.add(variable);
						continue;
					}

					final FormulaDto formula = variable.getFormula();
					if (formula != null) {
						final Map<String, FormulaVariable> formulaVariableMap =
								Maps.uniqueIndex(formula.getInputs(), new Function<FormulaVariable, String>() {

									public String apply(FormulaVariable from) {
										return String.valueOf(from.getId());
									}
								});
						// Convert the termids in formula definition to variable names.
						formula.setDefinition(
								DerivedVariableUtils.getDisplayableFormat(formula.getDefinition(), formulaVariableMap));
					}
				}

				ontologyList.removeAll(variableToRemove);

				if (ontologyList.isEmpty()) {
					continue;
				}
				if (selectedVariableTypes.contains(VariableType.TREATMENT_FACTOR)) {
					ontologyVariableDataManager.processTreatmentFactorHasPairValue(ontologyList,
							AppConstants.CREATE_STUDY_REMOVE_TREATMENT_FACTOR_IDS.getIntegerList());
				}

				final PropertyTreeSummary propertyTree = new PropertyTreeSummary(property, ontologyList);
				propertyTreeList.add(propertyTree);

			}

			// Todo: what to make of this.fieldbookMiddlewareService.filterStandardVariablesByIsAIds(...)

		} catch (final MiddlewareException e) {
			LOG.error(e.getMessage(), e);
		}

		return propertyTreeList;
	}

	private List<Integer> filterOutVariablesByVariableType(final Set<VariableType> selectedVariableTypes) {
		final List<Integer> cvTermIDs = new ArrayList<>();

		for (final VariableType varType : selectedVariableTypes) {
			switch (varType) {
				case STUDY_DETAIL:
					cvTermIDs.addAll(AppConstants.HIDE_STUDY_DETAIL_VARIABLES.getIntegerList());
					break;
				case SELECTION_METHOD:
					cvTermIDs.addAll(AppConstants.HIDE_ID_VARIABLES.getIntegerList());
					break;
				case ENVIRONMENT_DETAIL:
					cvTermIDs.addAll(AppConstants.HIDE_STUDY_VARIABLES.getIntegerList());
					break;
				case TREATMENT_FACTOR:
					cvTermIDs.addAll(AppConstants.CREATE_STUDY_REMOVE_TREATMENT_FACTOR_IDS.getIntegerList());
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
	public List<SettingDetail> addSettings(@RequestBody final CreateTrialForm form, @PathVariable final int mode) {
		final List<SettingDetail> newSettings = new ArrayList<>();
		try {
			final String programUUID = this.contextUtil.getCurrentProgramUUID();
			final List<SettingVariable> selectedVariables = form.getSelectedVariables();

			if (selectedVariables != null && !selectedVariables.isEmpty()) {

				for (final SettingVariable settingVariable : selectedVariables) {
					final Operation operation;

					if (VariableType.GERMPLASM_DESCRIPTOR.getId().equals(mode) || VariableType.ENTRY_DETAIL.getId().equals(mode)) {
						operation = Operation.UPDATE;
					} else {
						operation = this.removeVarFromDeletedList(settingVariable, mode);
					}

					settingVariable.setOperation(operation);

					this.settingsService.populateSettingVariable(settingVariable);

					final List<ValueReference> possibleValues = this.fieldbookService.getAllPossibleValues(settingVariable.getCvTermId());
					final Optional<FormulaDto> formula = this.formulaService.getByTargetId(settingVariable.getCvTermId());

					if (formula.isPresent()) {
						settingVariable.setFormula(formula.get());
					}

					final SettingDetail newSetting = new SettingDetail(settingVariable, possibleValues, null, true);
					final List<ValueReference> possibleValuesFavoriteFiltered =
							this.fieldbookService.getAllPossibleValuesFavorite(settingVariable.getCvTermId(), programUUID, true);

					final List<ValueReference> allValues =
							this.fieldbookService.getAllPossibleValuesWithFilter(settingVariable.getCvTermId(), false);

					final List<ValueReference> allFavoriteValues =
							this.fieldbookService.getAllPossibleValuesFavorite(settingVariable.getCvTermId(), programUUID, null);

					final List<ValueReference> intersection = SettingsUtil.intersection(allValues, allFavoriteValues);

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
				this.settingsService.addNewSettingDetails(mode, newSettings);
				return newSettings;
			}

		} catch (final Exception e) {
			ManageSettingsController.LOG.error(e.getMessage(), e);
			throw new RuntimeException("Something went wrong",e);

		}

		return new ArrayList<>();
	}

	private Operation removeVarFromDeletedList(final SettingVariable var, final int mode) {
		List<SettingDetail> settingsList = new ArrayList<SettingDetail>();
		if (mode == VariableType.STUDY_DETAIL.getId()) {
			settingsList = this.userSelection.getDeletedStudyLevelConditions();
		} else if (mode == VariableType.EXPERIMENTAL_DESIGN.getId() || mode == VariableType.GERMPLASM_DESCRIPTOR.getId()) {
			settingsList = this.userSelection.getDeletedPlotLevelList();
		} else if (mode == VariableType.ENVIRONMENT_CONDITION.getId()) {
			settingsList = this.userSelection.getDeletedStudyConditions();
		} else if (mode == VariableType.TREATMENT_FACTOR.getId()) {
			settingsList = this.userSelection.getDeletedTreatmentFactors();
		} else if (mode == VariableType.ENVIRONMENT_DETAIL.getId()) {
			settingsList = this.userSelection.getDeletedTrialLevelVariables();
		}

		Operation operation = Operation.ADD;
		if (settingsList != null) {
			final Iterator<SettingDetail> iter = settingsList.iterator();
			while (iter.hasNext()) {
				final SettingVariable deletedVariable = iter.next().getVariable();
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
	public boolean deleteVariable(@PathVariable final int mode, @RequestBody final List<Integer> ids) {

		for (final Integer id : ids) {
			this.deleteVariable(mode, id);
		}

		return true;
	}

	@ResponseBody
	@RequestMapping(value = "/deleteVariable/{mode}/{variableId}", method = RequestMethod.POST)
	public ResponseEntity<String> deleteVariable(@PathVariable final int mode, @PathVariable final int variableId) {
		try {
			if (mode == VariableType.STUDY_DETAIL.getId()) {
				final Map<String, String> idNameRetrieveSaveMap = this.fieldbookService.getIdNamePairForRetrieveAndSave();
				this.addVariableInDeletedList(userSelection.getStudyLevelConditions(), mode, variableId, true);
				SettingsUtil.deleteVariableInSession(userSelection.getStudyLevelConditions(), variableId);
				if (idNameRetrieveSaveMap.get(variableId) != null) {
					//special case so we must delete it as well
					this.addVariableInDeletedList(userSelection.getStudyLevelConditions(), mode,
							Integer.parseInt(idNameRetrieveSaveMap.get(variableId)), true);
					SettingsUtil.deleteVariableInSession(this.userSelection.getStudyLevelConditions(),
							Integer.parseInt(idNameRetrieveSaveMap.get(variableId)));
				}
				final Map<String, List<String>> idCodeNameRetrieveSaveMap = this.fieldbookService.getIdCodeNamePairForRetrieveAndSave();
				if (idCodeNameRetrieveSaveMap.get(variableId) != null) {
					final List<String> variables = idCodeNameRetrieveSaveMap.get(variableId);
					if (variables != null || variables.isEmpty()) {
						for (String varId : variables) {
							this.addVariableInDeletedList(userSelection.getStudyLevelConditions(), mode,
								Integer.parseInt(idNameRetrieveSaveMap.get(varId)), true);
							SettingsUtil.deleteVariableInSession(this.userSelection.getStudyLevelConditions(),
								Integer.parseInt(idNameRetrieveSaveMap.get(varId)));
						}
					}
				}

			} else if (mode == VariableType.EXPERIMENTAL_DESIGN.getId()
				|| mode == VariableType.GERMPLASM_DESCRIPTOR.getId()
				|| mode == VariableType.ENTRY_DETAIL.getId()) {
				this.addVariableInDeletedList(this.userSelection.getPlotsLevelList(), mode, variableId, true);
				SettingsUtil.deleteVariableInSession(this.userSelection.getPlotsLevelList(), variableId);
			} else if (mode == VariableType.ENVIRONMENT_CONDITION.getId() || mode == VariableType.ENVIRONMENT_CONDITION.getId()) {
				this.addVariableInDeletedList(this.userSelection.getStudyConditions(), mode, variableId, true);
				SettingsUtil.deleteVariableInSession(this.userSelection.getStudyConditions(), variableId);
			} else if (mode == VariableType.TREATMENT_FACTOR.getId()) {
				this.addVariableInDeletedList(this.userSelection.getTreatmentFactors(), mode, variableId, true);
				SettingsUtil.deleteVariableInSession(this.userSelection.getTreatmentFactors(), variableId);
			} else {
				this.addVariableInDeletedList(this.userSelection.getTrialLevelVariableList(), mode, variableId, true);
				SettingsUtil.deleteVariableInSession(this.userSelection.getTrialLevelVariableList(), variableId);
			}
		} catch (final MiddlewareException e) {
			LOG.error(e.getMessage(), e);
			return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>("", HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/deleteTreatmentFactorVariable", method = RequestMethod.POST)
	public String deleteTreatmentFactorVariable(@RequestBody final Map<String, Integer> ids) {
		final Integer levelID = ids.get("levelID");
		final Integer valueID = ids.get("valueID");
		if (levelID != null && levelID != 0) {
			this.deleteVariable(VariableType.TREATMENT_FACTOR.getId(), levelID);
		}

		if (valueID != null && valueID != 0) {
			this.deleteVariable(VariableType.TREATMENT_FACTOR.getId(), valueID);
		}

		return "";
	}

	@Override
	public String getContentName() {
		return null;
	}
}
