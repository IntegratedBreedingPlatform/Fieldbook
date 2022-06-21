/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * <p/>
 * Generation Challenge Programme (GCP)
 * <p/>
 * <p/>
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *******************************************************************************/

package com.efficio.fieldbook.web.trial.controller;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.bean.Instance;
import com.efficio.fieldbook.web.trial.service.ValidationService;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

/**
 * The Class SettingsController.
 */
public abstract class SettingsController extends AbstractBaseFieldbookController {

	protected static final List<Integer> EXPERIMENT_DESIGN_FACTOR_IDS = Arrays
		.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId(), TermId.PERCENTAGE_OF_REPLICATION.getId(),
			TermId.EXPT_DESIGN_SOURCE.getId());

	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(SettingsController.class);

	/**
	 * The fieldbook middleware service.
	 */
	@Resource
	protected org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	/**
	 * The user selection.
	 */
	@Resource
	protected UserSelection userSelection;

	/**
	 * The validation service.
	 */
	@Resource
	protected ValidationService validationService;

	/**
	 * The data import service.
	 */
	@Resource
	protected DataImportService dataImportService;

	@Resource
	protected OntologyService ontologyService;

	@Resource
	protected StudyDataManager studyDataManager;

	/**
	 * Builds the required factors.
	 *
	 * @param requiredFields the required fields
	 * @return the list
	 */
	protected List<Integer> buildVariableIDList(final String requiredFields) {
		return FieldbookUtil.getInstance().buildVariableIDList(requiredFields);
	}

	/**
	 * Creates the setting detail.
	 *
	 * @param id   the id
	 * @param name the name
	 * @return the setting detail
	 */
	protected SettingDetail createSettingDetail(final int id, final String name, final String role) {
		final String variableName;
		final StandardVariable stdVar = this.getStandardVariable(id);
		if (name != null && !name.isEmpty()) {
			variableName = name;
		} else {
			variableName = stdVar.getName();
		}

		if (stdVar != null && stdVar.getName() != null) {
			final SettingVariable svar =
				new SettingVariable(variableName, stdVar.getDescription(), stdVar.getProperty().getName(), stdVar.getScale().getName(),
					stdVar.getMethod().getName(), role, stdVar.getDataType().getName(), stdVar.getDataType().getId(),
					stdVar.getConstraints() != null && stdVar.getConstraints().getMinValue() != null
						? stdVar.getConstraints().getMinValue() : null,
					stdVar.getConstraints() != null && stdVar.getConstraints().getMaxValue() != null
						? stdVar.getConstraints().getMaxValue() : null);
			svar.setCvTermId(stdVar.getId());
			svar.setCropOntologyId(stdVar.getCropOntologyId() != null ? stdVar.getCropOntologyId() : "");
			svar.setTraitClass(stdVar.getIsA() != null ? stdVar.getIsA().getName() : "");
			svar.setOperation(Operation.ADD);
			final List<ValueReference> possibleValues = this.fieldbookService.getAllPossibleValues(id, true);
			final SettingDetail settingDetail = new SettingDetail(svar, possibleValues, null, false);
			final PhenotypicType type = StringUtils.isEmpty(role) ? null : PhenotypicType.getPhenotypicTypeByName(role);
			settingDetail.setRole(type);
			stdVar.setPhenotypicType(type);
			if (id == TermId.BREEDING_METHOD_ID.getId() || id == TermId.BREEDING_METHOD_CODE.getId()) {
				settingDetail.setValue(AppConstants.PLEASE_CHOOSE.getString());
			}
			settingDetail.setPossibleValuesToJson(possibleValues);
			final List<ValueReference> possibleValuesFavorite =
				this.fieldbookService.getAllPossibleValuesFavorite(id, this.getCurrentProject().getUniqueID(), true);
			settingDetail.setPossibleValuesFavorite(possibleValuesFavorite);
			settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);

			final List<ValueReference> allValues = this.fieldbookService.getAllPossibleValuesWithFilter(svar.getCvTermId(), false);
			settingDetail.setAllValues(allValues);
			settingDetail.setAllValuesToJson(allValues);

			final List<ValueReference> allFavoriteValues =
				this.fieldbookService.getAllPossibleValuesFavorite(svar.getCvTermId(), this.getCurrentProject().getUniqueID(), null);

			final List<ValueReference> intersection = SettingsUtil.intersection(allValues, allFavoriteValues);

			settingDetail.setAllFavoriteValues(intersection);
			settingDetail.setAllFavoriteValuesToJson(intersection);

			return settingDetail;
		} else {
			final SettingVariable svar = new SettingVariable();
			svar.setCvTermId(stdVar != null ? stdVar.getId() : 0);
			return new SettingDetail(svar, null, null, false);
		}
	}

	/**
	 * Creates the setting detail of given variable type
	 *
	 * @param id           the variable id
	 * @param alias        the variable alias
	 * @param variableType the variable type
	 * @return the setting detail
	 */// TODO TRIAL
	protected SettingDetail createSettingDetailWithVariableType(final int id, final String alias, final VariableType variableType) {
		final Variable variable = this.variableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(), id, false);

		String variableName = variable.getName();
		if (alias != null && !alias.isEmpty()) {
			variableName = alias;
		}

		final Property property = variable.getProperty();
		final Scale scale = variable.getScale();
		final org.generationcp.middleware.domain.ontology.Method method = variable.getMethod();

		final Double minValue = variable.getMinValue() == null ? null : Double.parseDouble(variable.getMinValue());
		final Double maxValue = variable.getMaxValue() == null ? null : Double.parseDouble(variable.getMaxValue());

		final SettingVariable settingVariable = new SettingVariable(variableName, variable.getDefinition(),
			variable.getProperty().getName(), scale.getName(), method.getName(), variableType.getRole().name(),
			scale.getDataType().getName(), scale.getDataType().getId(), minValue, maxValue);

		// NOTE: Using variable type which is used in project properties
		settingVariable.setVariableTypes(Collections.singleton(variableType));

		settingVariable.setCvTermId(variable.getId());
		settingVariable.setCropOntologyId(property.getCropOntologyId());

		if (variable.getFormula() != null) {
			settingVariable.setFormula(variable.getFormula());
		}

		if (!property.getClasses().isEmpty()) {
			settingVariable.setTraitClass(property.getClasses().iterator().next());
		}

		settingVariable.setOperation(Operation.ADD);
		final List<ValueReference> possibleValues = this.fieldbookService.getAllPossibleValues(id);

		final SettingDetail settingDetail = new SettingDetail(settingVariable, possibleValues, null, false);
		settingDetail.setRole(variableType.getRole());
		settingDetail.setVariableType(variableType);

		if (id == TermId.BREEDING_METHOD_ID.getId() || id == TermId.BREEDING_METHOD_CODE.getId()) {
			settingDetail.setValue(AppConstants.PLEASE_CHOOSE.getString());
		}
		settingDetail.setPossibleValuesToJson(possibleValues);
		final List<ValueReference> possibleValuesFavorite =
			this.fieldbookService.getAllPossibleValuesFavorite(id, this.getCurrentProject().getUniqueID(), false);
		settingDetail.setPossibleValuesFavorite(possibleValuesFavorite);
		settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
		return settingDetail;
	}

	/**
	 * Get standard variable.
	 *
	 * @param id the id
	 * @return the standard variable
	 */
	protected StandardVariable getStandardVariable(final int id) {
		return this.fieldbookMiddlewareService.getStandardVariable(id, this.contextUtil.getCurrentProgramUUID());
	}

	protected void resetSessionVariablesAfterSave(final Workbook workbook) {

		// update variables in measurement rows
		if (this.userSelection.getMeasurementRowList() != null && !this.userSelection.getMeasurementRowList().isEmpty()) {
			final MeasurementRow row = this.userSelection.getMeasurementRowList().get(0);
			for (final MeasurementVariable mvar : workbook.getMeasurementDatasetVariables()) {
				if (mvar.getOperation() == Operation.UPDATE) {
					for (final MeasurementVariable rvar : row.getMeasurementVariables()) {
						if (mvar.getTermId() == rvar.getTermId()) {
							if (mvar.getName() != null && !"".equals(mvar.getName())) {
								rvar.setName(mvar.getName());
							}
							break;
						}
					}
				}
			}
		}

		// remove deleted variables in measurement rows & header for variates
		this.removeDeletedVariablesInMeasurements(this.userSelection.getDeletedPlotLevelList(), workbook);
		this.removeDeletedVariablesInMeasurements(this.userSelection.getDeletedBaselineTraitsList(), workbook);

		// remove deleted variables in the original lists
		// and change add operation to update
		this.removeDeletedSetUpdate(this.userSelection.getStudyLevelConditions(), workbook.getConditions());
		this.removeDeletedSetUpdate(this.userSelection.getPlotsLevelList(), workbook.getFactors());

		this.removeDeletedSetUpdate(this.userSelection.getBaselineTraitsList(), workbook.getVariates());
		this.removeDeletedSetUpdate(this.userSelection.getStudyConditions(), workbook.getConstants());
		this.removeDeletedSetUpdate(this.userSelection.getTrialLevelVariableList(), null);
		this.removeDeletedSetUpdate(this.userSelection.getSelectionVariates(), null);
		workbook.reset();

		// reorder variates based on measurementrow order
		int index = 0;
		final List<MeasurementVariable> newVariatesList = new ArrayList<>();
		if (this.userSelection.getMeasurementRowList() != null) {
			for (final MeasurementRow row : this.userSelection.getMeasurementRowList()) {
				if (index == 0) {
					for (final MeasurementData var : row.getDataList()) {
						for (final MeasurementVariable varToArrange : workbook.getVariates()) {
							if (var.getMeasurementVariable().getTermId() == varToArrange.getTermId()) {
								newVariatesList.add(varToArrange);
							}
						}
					}
				}
				index++;
				break;
			}
		}
		workbook.setVariates(newVariatesList);

		// remove deleted variables in the deleted lists
		this.resetDeletedLists(this.userSelection);

		// add name variables
		if (this.userSelection.getRemovedConditions() == null) {
			this.userSelection.setRemovedConditions(new ArrayList<SettingDetail>());
		}
		// remove basic details & hidden variables from study level variables
		final String variableIds = AppConstants.FIXED_STUDY_VARIABLES.getString() + AppConstants.CHECK_VARIABLES.getString();
		SettingsUtil.removeBasicDetailsVariables(this.userSelection.getStudyLevelConditions(), variableIds);

		this.removeHiddenVariables(this.userSelection.getStudyLevelConditions(), AppConstants.HIDE_STUDY_FIELDS.getString());
		this.removeRemovedVariablesFromSession(this.userSelection.getStudyLevelConditions(), this.userSelection.getRemovedConditions());
		this.removeHiddenVariables(this.userSelection.getPlotsLevelList(), AppConstants.HIDE_PLOT_FIELDS.getString());
		this.removeRemovedVariablesFromSession(this.userSelection.getPlotsLevelList(), this.userSelection.getRemovedFactors());
		this.addNameVariables(this.userSelection.getRemovedConditions(), workbook, AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());
		this.removeCodeVariablesIfNeeded(this.userSelection.getStudyLevelConditions(),
			AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());
		// set value of breeding method code back to code after saving
		SettingsUtil.resetBreedingMethodValueToId(this.fieldbookMiddlewareService, workbook.getObservations(), false, this.ontologyService,
			this.contextUtil.getCurrentProgramUUID());
		// remove selection variates from traits list
		this.removeSelectionVariatesFromTraits(this.userSelection.getBaselineTraitsList());

	}

	//TODO TRIAL
	private void removeRemovedVariablesFromSession(final List<SettingDetail> variableList, final List<SettingDetail> removedVariableList) {
		if (removedVariableList == null || variableList == null) {
			return;
		}
		for (final SettingDetail setting : removedVariableList) {
			final Iterator<SettingDetail> iter = variableList.iterator();
			while (iter.hasNext()) {
				if (iter.next().getVariable().getCvTermId().equals(setting.getVariable().getCvTermId())) {
					iter.remove();
				}
			}
		}
	}

	//TODO TRIAL
	private void removeDeletedVariablesInMeasurements(final List<SettingDetail> deletedList, final Workbook workbook) {
		if (deletedList != null) {
			for (final SettingDetail setting : deletedList) {
				// remove from header
				if (workbook.getMeasurementDatasetVariables() != null) {
					final Iterator<MeasurementVariable> iter = workbook.getMeasurementDatasetVariables().iterator();
					while (iter.hasNext()) {
						if (iter.next().getTermId() == setting.getVariable().getCvTermId()) {
							iter.remove();
						}
					}
				}
			}
		}
	}

	/**
	 * Removes the deleted set update.
	 *
	 * @param settingList  the setting list
	 * @param variableList the variable list
	 */
	//TODO TRIAL
	private void removeDeletedSetUpdate(final List<SettingDetail> settingList, final List<MeasurementVariable> variableList) {
		if (settingList != null) {
			// remove all variables having delete and add operation
			final Iterator<SettingDetail> iter = settingList.iterator();
			while (iter.hasNext()) {
				final SettingDetail setting = iter.next();
				if (setting.getVariable().getOperation() != null && setting.getVariable().getOperation().equals(Operation.DELETE)) {
					iter.remove();
				} else if (setting.getVariable().getOperation() != null && setting.getVariable().getOperation().equals(Operation.ADD)) {
					setting.getVariable().setOperation(Operation.UPDATE);
				}
			}
		}

		if (variableList != null) {
			// remove all variables having delete and add operation
			final Iterator<MeasurementVariable> iter2 = variableList.iterator();
			while (iter2.hasNext()) {
				final MeasurementVariable var = iter2.next();
				if (var.getOperation() != null && var.getOperation().equals(Operation.DELETE)) {
					iter2.remove();
				} else if (var.getOperation() != null && var.getOperation().equals(Operation.ADD)) {
					var.setOperation(Operation.UPDATE);
				}
			}
		}
	}

	/**
	 * Reset deleted lists.
	 */
	//TODO TRIAL
	private void resetDeletedLists(final UserSelection userSelection) {
		userSelection.setDeletedStudyLevelConditions(new ArrayList<SettingDetail>());
		userSelection.setDeletedPlotLevelList(new ArrayList<SettingDetail>());
		userSelection.setDeletedBaselineTraitsList(new ArrayList<SettingDetail>());
		userSelection.setDeletedStudyConditions(new ArrayList<SettingDetail>());
		userSelection.setDeletedTrialLevelVariables(new ArrayList<SettingDetail>());
	}

	/**
	 * Removes the selection variates from traits.
	 *
	 * @param traits the traits
	 */
	//TODO TRIAL
	void removeSelectionVariatesFromTraits(final List<SettingDetail> traits) {
		if (traits != null) {
			final Iterator<SettingDetail> iter = traits.iterator();
			while (iter.hasNext()) {
				final SettingDetail var = iter.next();
				final String property = HtmlUtils.htmlUnescape(var.getVariable().getProperty());
				if (SettingsUtil.inPropertyList(this.ontologyService.getProperty(property).getId())) {
					iter.remove();
				}
			}
		}
	}

	/**
	 * Removes the hidden variables.
	 *
	 * @param settingList
	 * @param hiddenVarList
	 */
	//TODO TRIAL
	private void removeHiddenVariables(final List<SettingDetail> settingList, final String hiddenVarList) {
		if (settingList != null) {

			final Iterator<SettingDetail> iter = settingList.iterator();
			while (iter.hasNext()) {
				if (SettingsUtil.inHideVariableFields(iter.next().getVariable().getCvTermId(), hiddenVarList)) {
					iter.remove();
				}
			}
		}
	}

	//TODO TRIAL
	private void addNameVariables(final List<SettingDetail> removedConditions, final Workbook workbook, final String idCodeNamePairs) {
		final Map<String, MeasurementVariable> studyConditionMap = new HashMap<>();
		final Map<String, SettingDetail> removedConditionsMap = new HashMap<>();
		if (workbook != null && idCodeNamePairs != null && !"".equalsIgnoreCase(idCodeNamePairs)) {
			// we get a map so we can check easily instead of traversing it again
			for (final MeasurementVariable var : workbook.getConditions()) {
				if (var != null) {
					studyConditionMap.put(Integer.toString(var.getTermId()), var);
				}
			}

			if (removedConditions != null) {
				for (final SettingDetail setting : removedConditions) {
					if (setting != null) {
						removedConditionsMap.put(Integer.toString(setting.getVariable().getCvTermId()), setting);
					}
				}
			}
			final StringTokenizer tokenizer = new StringTokenizer(idCodeNamePairs, ",");
			if (tokenizer.hasMoreTokens()) {
				// we iterate it
				while (tokenizer.hasMoreTokens()) {
					final String pair = tokenizer.nextToken();
					final StringTokenizer tokenizerPair = new StringTokenizer(pair, "|");
					final String idTermId = tokenizerPair.nextToken();
					final String codeTermId = tokenizerPair.nextToken();
					final String nameTermId = tokenizerPair.nextToken();

					final Method method = this.getMethod(studyConditionMap, idTermId, codeTermId);

					// add code to the removed conditions if code is not yet in the list
					if (studyConditionMap.get(idTermId) != null && studyConditionMap.get(codeTermId) != null
						&& removedConditionsMap.get(codeTermId) == null) {
						this.addSettingDetail(removedConditions, removedConditionsMap, studyConditionMap, codeTermId,
							method == null ? "" : method.getMcode());
					}

					// add name to the removed conditions if name is not yet in the list
					if (studyConditionMap.get(nameTermId) != null && removedConditionsMap.get(nameTermId) == null) {
						this.addSettingDetail(removedConditions, removedConditionsMap, studyConditionMap, nameTermId,
							method == null ? "" : method.getMname());

					}
				}
			}
		}
	}

	//TODO TRIAL
	protected Method getMethod(final Map<String, MeasurementVariable> studyConditionMap, final String idTermId, final String codeTermId) {
		Method method = null;
		if (studyConditionMap.get(idTermId) != null) {
			method = StringUtils.isEmpty(studyConditionMap.get(idTermId).getValue()) ? null
				: this.fieldbookMiddlewareService.getMethodById(Double.valueOf(studyConditionMap.get(idTermId).getValue()).intValue());
		} else if (studyConditionMap.get(codeTermId) != null) {
			method = StringUtils.isEmpty(studyConditionMap.get(codeTermId).getValue()) ? null
				: this.fieldbookMiddlewareService.getMethodByCode(studyConditionMap.get(codeTermId).getValue());
		}
		return method;
	}

	//TODO TRIAL
	private void addSettingDetail(final List<SettingDetail> removedConditions, final Map<String, SettingDetail> removedConditionsMap,
		final Map<String, MeasurementVariable> studyConditionMap, final String id, final String value) {
		if (removedConditionsMap.get(id) == null) {
			removedConditions.add(this.createSettingDetail(Integer.parseInt(id), studyConditionMap.get(id).getName(), null));
		}
		if (removedConditions != null) {
			for (final SettingDetail setting : removedConditions) {
				if (setting.getVariable().getCvTermId() == Integer.parseInt(id)) {
					setting.setValue(value);
					setting.getVariable().setOperation(Operation.UPDATE);
				}
			}
		}
	}

	//TODO TRIAL
	private void removeCodeVariablesIfNeeded(final List<SettingDetail> variableList, final String idCodeNamePairs) {
		final Map<String, SettingDetail> variableListMap = new HashMap<>();
		if (variableList != null) {
			for (final SettingDetail setting : variableList) {
				if (setting != null) {
					variableListMap.put(Integer.toString(setting.getVariable().getCvTermId()), setting);
				}
			}
		}

		final StringTokenizer tokenizer = new StringTokenizer(idCodeNamePairs, ",");
		if (tokenizer.hasMoreTokens()) {
			// we iterate it
			while (tokenizer.hasMoreTokens()) {
				final String pair = tokenizer.nextToken();
				final StringTokenizer tokenizerPair = new StringTokenizer(pair, "|");
				final String idTermId = tokenizerPair.nextToken();
				final String codeTermId = tokenizerPair.nextToken();

				final Iterator<SettingDetail> iter = variableList != null ? variableList.iterator() : null;
				while (iter.hasNext()) {
					final Integer cvTermId = iter.next().getVariable().getCvTermId();
					if (cvTermId.equals(Integer.parseInt(codeTermId)) && variableListMap.get(idTermId) != null) {
						iter.remove();
					}
				}
			}
		}
	}

	protected void setUserSelection(final UserSelection userSelection) {
		this.userSelection = userSelection;
	}

	//TODO TRIAL
	protected void addVariableInDeletedList(final List<SettingDetail> currentList, final int mode, final int variableId,
		final boolean createNewSettingIfNull) {
		SettingDetail newSetting = null;
		for (final SettingDetail setting : currentList) {
			if (setting.getVariable().getCvTermId().equals(Integer.valueOf(variableId))) {
				newSetting = setting;
				break;
			}
		}

		if (newSetting == null && createNewSettingIfNull) {
			try {
				newSetting = this.createSettingDetail(variableId, "", "");
				newSetting.getVariable().setOperation(Operation.UPDATE);
			} catch (final MiddlewareQueryException e) {
				SettingsController.LOG.error(e.getMessage(), e);
			}
		} else if (newSetting == null) {
			return;
		}

		if (mode == VariableType.STUDY_DETAIL.getId()) {
			if (this.userSelection.getDeletedStudyLevelConditions() == null) {
				this.userSelection.setDeletedStudyLevelConditions(new ArrayList<SettingDetail>());
			}
			this.userSelection.getDeletedStudyLevelConditions().add(newSetting);
		} else if (mode == VariableType.EXPERIMENTAL_DESIGN.getId() || mode == VariableType.GERMPLASM_DESCRIPTOR.getId()) {
			if (this.userSelection.getDeletedPlotLevelList() == null) {
				this.userSelection.setDeletedPlotLevelList(new ArrayList<SettingDetail>());
			}
			this.userSelection.getDeletedPlotLevelList().add(newSetting);
		} else if (mode == VariableType.ENVIRONMENT_CONDITION.getId()) {
			if (this.userSelection.getDeletedStudyConditions() == null) {
				this.userSelection.setDeletedStudyConditions(new ArrayList<SettingDetail>());
			}
			this.userSelection.getDeletedStudyConditions().add(newSetting);
		} else if (mode == VariableType.ENVIRONMENT_DETAIL.getId()) {
			if (this.userSelection.getDeletedTrialLevelVariables() == null) {
				this.userSelection.setDeletedTrialLevelVariables(new ArrayList<SettingDetail>());
			}
			this.userSelection.getDeletedTrialLevelVariables().add(newSetting);
		} else if (mode == VariableType.TREATMENT_FACTOR.getId()) {
			if (this.userSelection.getDeletedTreatmentFactors() == null) {
				this.userSelection.setDeletedTreatmentFactors(new ArrayList<SettingDetail>());
			}
			this.userSelection.getDeletedTreatmentFactors().add(newSetting);
			this.addDeletedTreatmentFactorInDeletedPlotLevelList(newSetting);

		}
	}

	protected void addDeletedTreatmentFactorInDeletedPlotLevelList(final SettingDetail newSetting) {
		if (!CollectionUtils.isEmpty(this.userSelection.getPlotsLevelList())) {
			//Also add the deleted setting detail to the plotLevelList to delete the corresponding TF variable in the measurements table
			if (this.userSelection.getDeletedPlotLevelList() == null) {
				this.userSelection.setDeletedPlotLevelList(new ArrayList<SettingDetail>());
			}

			final int plotSettingDetailIndex = this.userSelection.getPlotsLevelList().indexOf(newSetting);
			if (plotSettingDetailIndex >= 0) {
				//We need to retrieve the Setting Detail object from the plotLevelList
				final SettingDetail plotSettingDetail = this.userSelection.getPlotsLevelList().get(plotSettingDetailIndex);
				this.userSelection.getDeletedPlotLevelList().add(plotSettingDetail);
			}
		}
	}

	public void setFieldbookService(final FieldbookService fieldbookService) {
		this.fieldbookService = fieldbookService;
	}

	public void setOntologyService(OntologyService ontologyService) {
		this.ontologyService = ontologyService;
	}

	/**
	 * @param userSelection
	 */
	protected void updateObservationsFromTemporaryWorkbookToWorkbook(final UserSelection userSelection) {

		final Map<Integer, MeasurementVariable> observationVariables = WorkbookUtil.createVariableList(
			userSelection.getWorkbook().getFactors(), userSelection.getWorkbook().getVariates());

		WorkbookUtil.deleteDeletedVariablesInObservations(observationVariables,
			userSelection.getWorkbook().getObservations());

		userSelection.setMeasurementRowList(userSelection.getWorkbook().getObservations());

		WorkbookUtil.updateTrialObservations(userSelection.getWorkbook(), userSelection.getTemporaryWorkbook());

	}

	/**
	 * This will copy the factors, variates and experimental design variable
	 * generated from importing a Custom Design to the Workbook that will be
	 * saved.
	 *
	 * @param userSelection
	 */
	protected void addVariablesFromTemporaryWorkbookToWorkbook(final UserSelection userSelection) {

		if (userSelection.getExperimentalDesignVariables() != null) {

			// Make sure that measurement variables are unique.
			final Set<MeasurementVariable> unique = new HashSet<>(userSelection.getWorkbook().getFactors());
			unique.addAll(userSelection.getTemporaryWorkbook().getFactors());
			unique.addAll(userSelection.getExperimentalDesignVariables());
			userSelection.getWorkbook().getFactors().clear();
			userSelection.getWorkbook().getFactors().addAll(unique);

			final Set<MeasurementVariable> makeUniqueVariates = new HashSet<>(
				userSelection.getTemporaryWorkbook().getVariates());
			makeUniqueVariates.addAll(userSelection.getWorkbook().getVariates());
			userSelection.getWorkbook().getVariates().clear();
			userSelection.getWorkbook().getVariates().addAll(makeUniqueVariates);

		}
	}

	/**
	 * assign UPDATE operation for existing experimental design variables
	 *
	 * @param conditions
	 */
	public void assignOperationOnExpDesignVariables(final List<MeasurementVariable> conditions) {
		final VariableTypeList factors =
			this.studyDataManager.getAllStudyFactors(this.userSelection.getWorkbook().getStudyDetails().getId());

		for (final MeasurementVariable mvar : conditions) {
			// update the operation for experiment design variables
			// EXP_DESIGN, EXP_DESIGN_SOURCE, NREP, PERCENTAGE_OF_REPLICATION
			// only if these variables already exists in the existing trial
			if (EXPERIMENT_DESIGN_FACTOR_IDS.contains(mvar.getTermId()) && factors.findById(mvar.getTermId()) != null) {
				mvar.setOperation(Operation.UPDATE);
			}
		}
	}

	protected List<List<ValueReference>> convertToValueReference(final List<Instance> instances) {
		final List<List<ValueReference>> returnVal = new ArrayList<>(instances.size());

		for (final Instance instance : instances) {
			final List<ValueReference> valueRefList = new ArrayList<>();

			for (final Map.Entry<String, String> entry : instance.getManagementDetailValues().entrySet()) {
				final ValueReference valueRef = new ValueReference(entry.getKey(), entry.getValue());
				valueRefList.add(valueRef);
			}

			returnVal.add(valueRefList);
		}

		return returnVal;
	}

	public void addDeletedSettingsList() {
		final List<SettingDetail> studyLevelConditions = this.userSelection.getStudyLevelConditions();
		final List<SettingDetail> basicDetails = this.userSelection.getBasicDetails();

		final List<SettingDetail> combinedList = new ArrayList<>();
		combinedList.addAll(basicDetails);
		combinedList.addAll(studyLevelConditions);
		// TODO: add deleted selection variates
		// include deleted list if measurements are available
		SettingsUtil.addDeletedSettingsList(combinedList, this.userSelection.getDeletedStudyLevelConditions(),
			this.userSelection.getStudyLevelConditions());
		SettingsUtil.addDeletedSettingsList(null, this.userSelection.getDeletedPlotLevelList(), this.userSelection.getPlotsLevelList());

		SettingsUtil.addDeletedSettingsList(null, this.userSelection.getDeletedBaselineTraitsList(),
			this.userSelection.getBaselineTraitsList());
		SettingsUtil.addDeletedSettingsList(null, this.userSelection.getDeletedStudyConditions(), this.userSelection.getStudyConditions());
		SettingsUtil.addDeletedSettingsList(null, this.userSelection.getDeletedTrialLevelVariables(),
			this.userSelection.getTrialLevelVariableList());
		SettingsUtil
			.addDeletedSettingsList(null, this.userSelection.getDeletedTreatmentFactors(), this.userSelection.getTreatmentFactors());
	}

	public void initializeBasicUserSelectionLists() {
		if (this.userSelection.getPlotsLevelList() == null) {
			this.userSelection.setPlotsLevelList(new ArrayList<SettingDetail>());
		}
		if (this.userSelection.getBaselineTraitsList() == null) {
			this.userSelection.setBaselineTraitsList(new ArrayList<SettingDetail>());
		}
		if (this.userSelection.getStudyConditions() == null) {
			this.userSelection.setStudyConditions(new ArrayList<SettingDetail>());
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
	}

	protected void populateSettingData(final List<SettingDetail> details, final Map<String, String> values) {
		if (details == null || details.isEmpty()) {
			return;
		}

		for (final SettingDetail detail : details) {
			if (values.containsKey(detail.getVariable().getCvTermId().toString())) {
				detail.setValue(values.get(detail.getVariable().getCvTermId().toString()));
			}
		}
	}

	protected List<SettingDetail> getEntryDetails(final List<MeasurementVariable> entryDetails) {
		final List<Integer> requiredIDList = this.buildVariableIDList(AppConstants.CREATE_STUDY_ENTRY_DETAILS_REQUIRED_FIELDS.getString());

		return entryDetails.stream()
			.map(variable -> {
				final SettingDetail detail = this.createSettingDetail(
					variable.getTermId(),
					variable.getName(),
					variable.getVariableType().getRole().name()
				);
				if (requiredIDList.contains(variable.getTermId())) {
					detail.setDeletable(false);
				} else {
					detail.setDeletable(true);
				}
				detail.getVariable().setOperation(Operation.UPDATE);
				return detail;
			})
			.collect(Collectors.toList());

	}
	protected List<SettingDetail> getPlotsLevelsDetails(final List<MeasurementVariable> measurementVariables, final boolean isUsePrevious){
		final List<SettingDetail> detailList = new ArrayList<>();
		final List<Integer> requiredIDList = this.buildVariableIDList(AppConstants.CREATE_STUDY_PLOT_REQUIRED_FIELDS.getString());

		for (final MeasurementVariable measurementVariable : measurementVariables) {
			// this condition is required so that treatment factors are not
			// included in the list of factors for the germplasm tab
			if (measurementVariable.getTreatmentLabel() != null && !measurementVariable.getTreatmentLabel().isEmpty()
				|| this.inRequiredExpDesignVar(measurementVariable.getTermId()) && isUsePrevious) {
				continue;
			}

			final SettingDetail detail =
				this.createSettingDetail(measurementVariable.getTermId(), measurementVariable.getName(), VariableType.GERMPLASM_DESCRIPTOR.getRole().name());

			if (measurementVariable.getRole() != null) {
				detail.setRole(measurementVariable.getRole());
				detail.getVariable().setRole(measurementVariable.getRole().name());
			}

			if (requiredIDList.contains(measurementVariable.getTermId())) {
				detail.setDeletable(false);
			} else {
				detail.setDeletable(true);
			}

			// set all variables with trial design role to hidden
			if (measurementVariable.getRole() == PhenotypicType.TRIAL_DESIGN) {
				detail.setHidden(true);
				// BMS-1048
				if (measurementVariable.getTermId() == TermId.COLUMN_NO.getId() || measurementVariable.getTermId() == TermId.RANGE_NO.getId()) {
					detail.setDeletable(false);
					detail.setHidden(false);
				}
			} else {
				detail.setHidden(false);
			}

			detail.getVariable().setOperation(Operation.UPDATE);
			detailList.add(detail);
		}
		return detailList;
	}

	boolean inRequiredExpDesignVar(final int termId) {
		final StringTokenizer token = new StringTokenizer(AppConstants.EXP_DESIGN_REQUIRED_VARIABLES.getString(), ",");

		while (token.hasMoreTokens()) {
			if (Integer.parseInt(token.nextToken()) == termId) {
				return true;
			}
		}
		return false;
	}
}
