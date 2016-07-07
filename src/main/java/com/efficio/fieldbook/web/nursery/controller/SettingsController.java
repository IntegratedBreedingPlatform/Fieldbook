/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * <p/>
 * Generation Challenge Programme (GCP)
 * <p/>
 * <p/>
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *******************************************************************************/

package com.efficio.fieldbook.web.nursery.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.workbench.TemplateSetting;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;
import com.efficio.fieldbook.web.nursery.service.ValidationService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;

/**
 * The Class SettingsController.
 */
public abstract class SettingsController extends AbstractBaseFieldbookController {

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(SettingsController.class);

	/** The workbench service. */
	@Resource
	protected WorkbenchService workbenchService;

	/** The fieldbook service. */
	@Resource
	protected FieldbookService fieldbookService;

	/** The fieldbook middleware service. */
	@Resource
	protected org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	/** The user selection. */
	@Resource
	protected UserSelection userSelection;

	/** The measurements generator service. */
	@Resource
	protected MeasurementsGeneratorService measurementsGeneratorService;

	/** The validation service. */
	@Resource
	protected ValidationService validationService;

	/** The data import service. */
	@Resource
	protected DataImportService dataImportService;

	@Resource
	protected OntologyService ontologyService;

	/**
	 * Checks if the measurement table has user input data for a particular variable id
	 *
	 * @param variableId, List<MeasurementRow>
	 * @return
	 */
	public static boolean hasMeasurementDataEntered(final int variableId, final List<MeasurementRow> measurementRow) {
		for (final MeasurementRow row : measurementRow) {
			for (final MeasurementData data : row.getDataList()) {
				if (data.getMeasurementVariable() != null && data.getMeasurementVariable().getTermId() == variableId
						&& data.getValue() != null && !data.getValue().isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Gets the settings list.
	 *
	 * @return the settings list
	 */
	@ModelAttribute("settingsList")
	public List<TemplateSetting> getNurserySettingsList() {
		try {
			final TemplateSetting templateSettingFilter =
					new TemplateSetting(null, Integer.valueOf(this.getCurrentProjectId()), null, this.getNurseryTool(), null, null);
			templateSettingFilter.setIsDefaultToNull();
			final List<TemplateSetting> templateSettingsList = this.workbenchService.getTemplateSettings(templateSettingFilter);
			templateSettingsList.add(0, new TemplateSetting(0, Integer.valueOf(this.getCurrentProjectId()), "", null, "", false));
			return templateSettingsList;

		} catch (final MiddlewareQueryException e) {
			SettingsController.LOG.error(e.getMessage(), e);
		}

		return new ArrayList<>();
	}

	/**
	 * Gets the trial settings list.
	 *
	 * @return the trial settings list
	 */
	@ModelAttribute("settingsTrialList")
	public List<TemplateSetting> getTrialSettingsList() {
		try {
			final TemplateSetting templateSettingFilter =
					new TemplateSetting(null, Integer.valueOf(this.getCurrentProjectId()), null, this.getTrialTool(), null, null);
			templateSettingFilter.setIsDefaultToNull();
			final List<TemplateSetting> templateSettingsList = this.workbenchService.getTemplateSettings(templateSettingFilter);
			templateSettingsList.add(0, new TemplateSetting(0, Integer.valueOf(this.getCurrentProjectId()), "", null, "", false));
			return templateSettingsList;

		} catch (final MiddlewareQueryException e) {
			SettingsController.LOG.error(e.getMessage(), e);
		}

		return new ArrayList<>();
	}

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
	 * Builds the required factors label.
	 *
	 * @param requiredFields the required fields
	 * @param hasLabels the has labels
	 * @return the list
	 */
	protected List<String> buildRequiredVariablesLabel(final String requiredFields, final boolean hasLabels) {

		final List<String> requiredVariables = new ArrayList<>();
		final StringTokenizer token = new StringTokenizer(requiredFields, ",");
		while (token.hasMoreTokens()) {
			if (hasLabels) {
				requiredVariables.add(AppConstants.getString(token.nextToken() + AppConstants.LABEL.getString()));
			} else {
				requiredVariables.add(null);
				token.nextToken();
			}
		}

		return requiredVariables;
	}

	/**
	 * Builds the required factors flag.
	 *
	 * @param requiredFields the required fields
	 * @return the boolean[]
	 */
	protected boolean[] buildRequiredVariablesFlag(final String requiredFields) {
		final StringTokenizer token = new StringTokenizer(requiredFields, ",");
		final boolean[] requiredVariablesFlag = new boolean[token.countTokens()];
		for (int i = 0; i < requiredVariablesFlag.length; i++) {
			requiredVariablesFlag[i] = false;
		}
		return requiredVariablesFlag;
	}

	private String getCodeCounterpart(final String idCodeNameCombination) {
		final StringTokenizer tokenizer = new StringTokenizer(idCodeNameCombination, "|");
		if (tokenizer.hasMoreTokens()) {
			tokenizer.nextToken();
			return tokenizer.nextToken();
		} else {
			return "0";
		}
	}

	/**
	 * Update required fields.
	 *
	 * @param requiredVariables the required variables
	 * @param requiredVariablesLabel the required variables label
	 * @param requiredVariablesFlag the required variables flag
	 * @param variables the variables
	 * @param hasLabels the has labels
	 * @return the list
	 */
	protected List<SettingDetail> updateRequiredFields(final List<Integer> requiredVariables, final List<String> requiredVariablesLabel,
			final boolean[] requiredVariablesFlag, final List<SettingDetail> variables, final boolean hasLabels,
			final String idCodeNameCombination, final String role) {

		// create a map of id and its id-code-name combination
		final Map<String, String> idCodeNameMap = new HashMap<>();
		if (idCodeNameCombination != null & !idCodeNameCombination.isEmpty()) {
			final StringTokenizer tokenizer = new StringTokenizer(idCodeNameCombination, ",");
			if (tokenizer.hasMoreTokens()) {
				while (tokenizer.hasMoreTokens()) {
					final String pair = tokenizer.nextToken();
					final StringTokenizer tokenizerPair = new StringTokenizer(pair, "|");
					idCodeNameMap.put(tokenizerPair.nextToken(), pair);
				}
			}
		}

		// save hidden conditions in a map
		final Map<String, SettingDetail> variablesMap = new HashMap<>();
		if (variables != null) {
			for (final SettingDetail variable : this.userSelection.getRemovedConditions()) {
				variablesMap.put(variable.getVariable().getCvTermId().toString(), variable);
			}
		}

		for (final SettingDetail variable : variables) {
			Integer stdVar;
			if (variable.getVariable().getCvTermId() != null) {
				stdVar = variable.getVariable().getCvTermId();
			} else {
				stdVar =
						this.fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(
								variable.getVariable().getProperty(), variable.getVariable().getScale(),
								variable.getVariable().getMethod(), PhenotypicType.valueOf(variable.getVariable().getRole()));
			}

			// mark required variables that are already in the list
			int ctr = 0;
			for (final Integer requiredFactor : requiredVariables) {
				String code = "0";
				// if the variable is in the id-code-name combination list, get code counterpart of id
				if (idCodeNameMap.get(String.valueOf(stdVar)) != null) {
					code = this.getCodeCounterpart(idCodeNameMap.get(String.valueOf(stdVar)));
				}
				// if the id already exists do not add the code counterpart as a required field
				if (requiredFactor.equals(stdVar) || requiredFactor.equals(Integer.parseInt(code))) {
					requiredVariablesFlag[ctr] = true;
					variable.setOrder((requiredVariables.size() - ctr) * -1);
					if (hasLabels) {
						variable.getVariable().setName(requiredVariablesLabel.get(ctr));
					}
				}
				ctr++;
			}
		}

		// add required variables that are not in existing nursery
		for (int i = 0; i < requiredVariablesFlag.length; i++) {
			if (!requiredVariablesFlag[i]) {
				final SettingDetail newSettingDetail =
						this.createSettingDetail(requiredVariables.get(i), requiredVariablesLabel.get(i), role);
				newSettingDetail.setOrder((requiredVariables.size() - i) * -1);
				// set value of breeding method code if name is provided but id is not
				if (TermId.BREEDING_METHOD_CODE.getId() == requiredVariables.get(i)
						&& variablesMap.get(String.valueOf(TermId.BREEDING_METHOD.getId())) != null
						&& variablesMap.get(String.valueOf(TermId.BREEDING_METHOD_ID.getId())) == null) {
					final Method method =
							this.fieldbookMiddlewareService.getMethodByName(variablesMap
									.get(String.valueOf(TermId.BREEDING_METHOD.getId())).getValue());
					newSettingDetail.setValue(method.getMid() == null ? "" : method.getMid().toString());
				}

				variables.add(newSettingDetail);
			}
		}

		// sort by required fields
		Collections.sort(variables, new Comparator<SettingDetail>() {

			@Override
			public int compare(final SettingDetail o1, final SettingDetail o2) {
				return o1.getOrder() - o2.getOrder();
			}
		});

		return variables;
	}

	/**
	 * Builds the default variables.
	 *
	 * @param defaults the defaults
	 * @param requiredFields the required fields
	 * @param requiredVariablesLabel the required variables label
	 * @return the list
	 */
	protected List<SettingDetail> buildDefaultVariables(final List<SettingDetail> defaults, final String requiredFields,
			final List<String> requiredVariablesLabel, final String role) {
		final StringTokenizer token = new StringTokenizer(requiredFields, ",");
		int ctr = 0;
		while (token.hasMoreTokens()) {
			defaults.add(this.createSettingDetail(Integer.valueOf(token.nextToken()), requiredVariablesLabel.get(ctr), role));
			ctr++;
		}
		return defaults;
	}

	/**
	 * Creates the setting detail.
	 *
	 * @param id the id
	 * @param name the name
	 * @return the setting detail
	 */
	protected SettingDetail createSettingDetail(final int id, final String name, final String role) {
		String variableName;
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
			final List<ValueReference> possibleValues = this.fieldbookService.getAllPossibleValues(id);
			final SettingDetail settingDetail = new SettingDetail(svar, possibleValues, null, false);
			final PhenotypicType type = StringUtils.isEmpty(role) ? null : PhenotypicType.getPhenotypicTypeByName(role);
			settingDetail.setRole(type);
			stdVar.setPhenotypicType(type);
			if (id == TermId.BREEDING_METHOD_ID.getId() || id == TermId.BREEDING_METHOD_CODE.getId()) {
				settingDetail.setValue(AppConstants.PLEASE_CHOOSE.getString());
			} else if (id == TermId.STUDY_UID.getId()) {
				settingDetail.setValue(this.getCurrentIbdbUserId().toString());
			} else if (id == TermId.STUDY_UPDATE.getId()) {
				settingDetail.setValue(DateUtil.getCurrentDateAsStringValue());
			}
			settingDetail.setPossibleValuesToJson(possibleValues);
			final List<ValueReference> possibleValuesFavorite =
					this.fieldbookService.getAllPossibleValuesFavorite(id, this.getCurrentProject().getUniqueID(), false);
			settingDetail.setPossibleValuesFavorite(possibleValuesFavorite);
			settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);

			final List<ValueReference> allValues = this.fieldbookService.getAllPossibleValuesWithFilter(svar.getCvTermId(), false);
			settingDetail.setAllValues(allValues);
			settingDetail.setAllValuesToJson(allValues);

			final List<ValueReference> allFavoriteValues =
					this.fieldbookService.getAllPossibleValuesFavorite(svar.getCvTermId(), this.getCurrentProject().getUniqueID(), null);
			
			final List<ValueReference>  intersection = SettingsUtil.intersection(allValues, allFavoriteValues);
			
			settingDetail.setAllFavoriteValues(intersection);
			settingDetail.setAllFavoriteValuesToJson(intersection);

			return settingDetail;
		} else {
			final SettingVariable svar = new SettingVariable();
			svar.setCvTermId(stdVar.getId());
			return new SettingDetail(svar, null, null, false);
		}
	}
	
	/**
	 * Creates the setting detail.
	 *
	 * @param id the id
	 * @return the setting detail
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	protected SettingDetail createSettingDetail(final int id, final VariableType variableType) throws MiddlewareException {

		Variable variable = this.variableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(), id, false, false);

		Property property = variable.getProperty();
		Scale scale = variable.getScale();
		org.generationcp.middleware.domain.ontology.Method method = variable.getMethod();

		Double minValue = variable.getMinValue() == null ? null : Double.parseDouble(variable.getMinValue());
		Double maxValue = variable.getMaxValue() == null ? null : Double.parseDouble(variable.getMaxValue());

		final SettingVariable settingVariable = new SettingVariable(variable.getName(), variable.getDefinition(),
				variable.getProperty().getName(), scale.getName(), method.getName(),
				variableType.getRole().name(),
				scale.getDataType().getName(), scale.getDataType().getId(),
				minValue, maxValue);

		//NOTE: Using variable type which is used in project properties
		settingVariable.setVariableTypes(Collections.singleton(variableType));

		settingVariable.setCvTermId(variable.getId());
		settingVariable.setCropOntologyId(property.getCropOntologyId());

		if(property.getClasses().size() > 0){
			settingVariable.setTraitClass(property.getClasses().iterator().next());
		}

		settingVariable.setOperation(Operation.ADD);
		final List<ValueReference> possibleValues = this.fieldbookService.getAllPossibleValues(id);

		final SettingDetail settingDetail = new SettingDetail(settingVariable, possibleValues, null, false);
		settingDetail.setRole(variableType.getRole());
		settingDetail.setVariableType(variableType);

		if (id == TermId.BREEDING_METHOD_ID.getId() || id == TermId.BREEDING_METHOD_CODE.getId()) {
			settingDetail.setValue(AppConstants.PLEASE_CHOOSE.getString());
		} else if (id == TermId.STUDY_UID.getId()) {
			settingDetail.setValue(this.getCurrentIbdbUserId().toString());
		} else if (id == TermId.STUDY_UPDATE.getId()) {
			settingDetail.setValue(DateUtil.getCurrentDateAsStringValue());
		}

		settingDetail.setPossibleValuesToJson(possibleValues);
		final List<ValueReference> possibleValuesFavorite =
				this.fieldbookService.getAllPossibleValuesFavorite(id, this.getCurrentProject().getUniqueID(), false);
		settingDetail.setPossibleValuesFavorite(possibleValuesFavorite);
		settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
		return settingDetail;
	}

	/**
	 * Populates Setting Variable.
	 *
	 * @param var the var
	 */
	protected void populateSettingVariable(final SettingVariable var) {
		final StandardVariable stdvar = this.getStandardVariable(var.getCvTermId());
		if (stdvar != null) {
			var.setDescription(stdvar.getDescription());
			var.setProperty(stdvar.getProperty().getName());
			var.setScale(stdvar.getScale().getName());
			var.setMethod(stdvar.getMethod().getName());
			var.setDataType(stdvar.getDataType().getName());
			var.setVariableTypes(stdvar.getVariableTypes());
			var.setCropOntologyId(stdvar.getCropOntologyId() != null ? stdvar.getCropOntologyId() : "");
			var.setTraitClass(stdvar.getIsA() != null ? stdvar.getIsA().getName() : "");
			var.setDataTypeId(stdvar.getDataType().getId());
			var.setMinRange(stdvar.getConstraints() != null && stdvar.getConstraints().getMinValue() != null ? stdvar.getConstraints()
					.getMinValue() : null);
			var.setMaxRange(stdvar.getConstraints() != null && stdvar.getConstraints().getMaxValue() != null ? stdvar.getConstraints()
					.getMaxValue() : null);
			var.setWidgetType();
		}
	}

	/**
	 * Get setting variable.
	 *
	 * @param id the id
	 * @return the setting variable
	 */
	protected SettingVariable getSettingVariable(final int id) {
		final StandardVariable stdVar = this.getStandardVariable(id);
		if (stdVar != null) {
			final SettingVariable svar =
					new SettingVariable(stdVar.getName(), stdVar.getDescription(), stdVar.getProperty().getName(), stdVar.getScale()
							.getName(), stdVar.getMethod().getName(), null, stdVar.getDataType().getName(), stdVar.getDataType().getId(),
							stdVar.getConstraints() != null && stdVar.getConstraints().getMinValue() != null ? stdVar.getConstraints()
									.getMinValue() : null,
							stdVar.getConstraints() != null && stdVar.getConstraints().getMaxValue() != null ? stdVar.getConstraints()
									.getMaxValue() : null);
			svar.setCvTermId(stdVar.getId());
			svar.setCropOntologyId(stdVar.getCropOntologyId() != null ? stdVar.getCropOntologyId() : "");
			svar.setTraitClass(stdVar.getIsA() != null ? stdVar.getIsA().getName() : "");
			return svar;
		}
		return null;
	}

	/**
	 * Get standard variable.
	 *
	 * @param id the id
	 * @return the standard variable
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	protected StandardVariable getStandardVariable(final int id) {
		return this.fieldbookMiddlewareService.getStandardVariable(id, this.contextUtil.getCurrentProgramUUID());
	}

	/**
	 * Creates the study details.
	 *
	 * @param workbook the workbook
	 * @param conditions the conditions
	 * @param folderId the folder id
	 */
	public void createStudyDetails(final Workbook workbook, final List<SettingDetail> conditions, final Integer folderId,
			final Integer studyId) {
		if (workbook.getStudyDetails() == null) {
			workbook.setStudyDetails(new StudyDetails());
		}
		final StudyDetails studyDetails = workbook.getStudyDetails();

		if (conditions != null && !conditions.isEmpty()) {
			if (studyId != null) {
				studyDetails.setId(studyId);
			}
			studyDetails.setTitle(SettingsUtil.getSettingDetailValue(conditions, TermId.STUDY_TITLE.getId()));
			studyDetails.setObjective(SettingsUtil.getSettingDetailValue(conditions, TermId.STUDY_OBJECTIVE.getId()));
			studyDetails.setStudyName(SettingsUtil.getSettingDetailValue(conditions, TermId.STUDY_NAME.getId()));
			studyDetails.setStartDate(SettingsUtil.getSettingDetailValue(conditions, TermId.START_DATE.getId()));
			studyDetails.setEndDate(SettingsUtil.getSettingDetailValue(conditions, TermId.END_DATE.getId()));
			studyDetails.setStudyType(StudyType.N);

			if (folderId != null) {
				studyDetails.setParentFolderId(folderId);
			}
		}
		studyDetails.print(1);
	}

	/**
	 * Checks if the measurement table has user input data for a particular variable id
	 *
	 * @param variableId
	 * @return
	 */
	public boolean hasMeasurementDataEntered(final int variableId) {
		for (final MeasurementRow row : this.userSelection.getMeasurementRowList()) {
			for (final MeasurementData data : row.getDataList()) {
				if (data.getMeasurementVariable().getTermId() == variableId && data.getValue() != null && !data.getValue().isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean hasMeasurementDataEnteredForVariables(final List<Integer> variableIds, final UserSelection userSelectionTemp) {
		for (final Integer variableId : variableIds) {
			for (final MeasurementRow row : userSelectionTemp.getMeasurementRowList()) {
				for (final MeasurementData data : row.getDataList()) {
					if (data.getMeasurementVariable().getTermId() == variableId && data.getValue() != null && !data.getValue().isEmpty()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	protected void removeVariablesFromExistingNursery(final List<SettingDetail> settingList, final String variables) {
		final Iterator<SettingDetail> variableList = settingList.iterator();
		while (variableList.hasNext()) {
			if (SettingsUtil.inHideVariableFields(variableList.next().getVariable().getCvTermId(), variables)) {
				variableList.remove();
			}
		}
	}

	protected void resetSessionVariablesAfterSave(final Workbook workbook, final boolean isNursery) {

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
		this.removeDeletedVariablesInMeasurements(this.userSelection.getDeletedPlotLevelList(), workbook, this.userSelection);
		this.removeDeletedVariablesInMeasurements(this.userSelection.getDeletedBaselineTraitsList(), workbook, this.userSelection);

		// remove deleted variables in the original lists
		// and change add operation to update
		this.removeDeletedSetUpdate(this.userSelection.getStudyLevelConditions(), workbook.getConditions());
		this.removeDeletedSetUpdate(this.userSelection.getPlotsLevelList(), workbook.getFactors());
		this.removeDeletedSetUpdate(this.userSelection.getBaselineTraitsList(), workbook.getVariates());
		this.removeDeletedSetUpdate(this.userSelection.getNurseryConditions(), workbook.getConstants());
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
		SettingsUtil.removeBasicDetailsVariables(this.userSelection.getStudyLevelConditions());

		if (isNursery) {
			this.removeHiddenVariables(this.userSelection.getStudyLevelConditions(), AppConstants.HIDE_NURSERY_FIELDS.getString());
			this.removeRemovedVariablesFromSession(this.userSelection.getStudyLevelConditions(), this.userSelection.getRemovedConditions());
			this.removeHiddenVariables(this.userSelection.getPlotsLevelList(), AppConstants.HIDE_PLOT_FIELDS.getString());
			this.removeRemovedVariablesFromSession(this.userSelection.getPlotsLevelList(), this.userSelection.getRemovedFactors());
			this.addNameVariables(this.userSelection.getRemovedConditions(), workbook,
					AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());
			this.removeCodeVariablesIfNeeded(this.userSelection.getStudyLevelConditions(),
					AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());
			// set value of breeding method code back to code after saving
			SettingsUtil.resetBreedingMethodValueToId(this.fieldbookMiddlewareService, workbook.getObservations(), false,
					this.ontologyService);
			// remove selection variates from traits list
			this.removeSelectionVariatesFromTraits(this.userSelection.getBaselineTraitsList());
		}
	}

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

	private void removeDeletedVariablesInMeasurements(final List<SettingDetail> deletedList, final Workbook workbook,
			final UserSelection userSelection) {
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
	 * @param settingList the setting list
	 * @param variableList the variable list
	 */
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
	private void resetDeletedLists(final UserSelection userSelection) {
		userSelection.setDeletedStudyLevelConditions(new ArrayList<SettingDetail>());
		userSelection.setDeletedPlotLevelList(new ArrayList<SettingDetail>());
		userSelection.setDeletedBaselineTraitsList(new ArrayList<SettingDetail>());
		userSelection.setDeletedNurseryConditions(new ArrayList<SettingDetail>());
		userSelection.setDeletedTrialLevelVariables(new ArrayList<SettingDetail>());
	}

	/**
	 * Removes the selection variates from traits.
	 *
	 * @param traits the traits
	 */
	private void removeSelectionVariatesFromTraits(final List<SettingDetail> traits) {
		if (traits != null) {
			final Iterator<SettingDetail> iter = traits.iterator();
			while (iter.hasNext()) {
				final SettingDetail var = iter.next();
				if (SettingsUtil.inPropertyList(this.ontologyService.getProperty(var.getVariable().getProperty()).getId())) {
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
			final String programUUID = this.contextUtil.getCurrentProgramUUID();
			final StringTokenizer tokenizer = new StringTokenizer(idCodeNamePairs, ",");
			if (tokenizer.hasMoreTokens()) {
				// we iterate it
				while (tokenizer.hasMoreTokens()) {
					final String pair = tokenizer.nextToken();
					final StringTokenizer tokenizerPair = new StringTokenizer(pair, "|");
					final String idTermId = tokenizerPair.nextToken();
					final String codeTermId = tokenizerPair.nextToken();
					final String nameTermId = tokenizerPair.nextToken();

					final Method method = this.getMethod(studyConditionMap, idTermId, codeTermId, programUUID);

					// add code to the removed conditions if code is not yet in the list
					if (studyConditionMap.get(idTermId) != null && studyConditionMap.get(codeTermId) != null
							&& removedConditionsMap.get(codeTermId) == null) {
						this.addSettingDetail(removedConditions, removedConditionsMap, studyConditionMap, codeTermId, method == null ? ""
								: method.getMcode(), this.getCurrentIbdbUserId().toString());
					}

					// add name to the removed conditions if name is not yet in the list
					if (studyConditionMap.get(nameTermId) != null && removedConditionsMap.get(nameTermId) == null) {
						this.addSettingDetail(removedConditions, removedConditionsMap, studyConditionMap, nameTermId, method == null ? ""
								: method.getMname(), this.getCurrentIbdbUserId().toString());

					}
				}
			}
		}
	}

	protected Method getMethod(final Map<String, MeasurementVariable> studyConditionMap, final String idTermId, final String codeTermId,
			final String programUUID) {
		Method method = null;
		if (studyConditionMap.get(idTermId) != null) {
			method =
					studyConditionMap.get(idTermId).getValue().isEmpty() ? null : this.fieldbookMiddlewareService.getMethodById(Double
							.valueOf(studyConditionMap.get(idTermId).getValue()).intValue());
		} else if (studyConditionMap.get(codeTermId) != null) {
			method =
					studyConditionMap.get(codeTermId).getValue().isEmpty() ? null : this.fieldbookMiddlewareService.getMethodByCode(
							studyConditionMap.get(codeTermId).getValue(), programUUID);
		}
		return method;
	}

	private void addSettingDetail(final List<SettingDetail> removedConditions, final Map<String, SettingDetail> removedConditionsMap,
			final Map<String, MeasurementVariable> studyConditionMap, final String id, final String value, final String userId) {
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

				final Iterator<SettingDetail> iter = variableList.iterator();
				while (iter.hasNext()) {
					final Integer cvTermId = iter.next().getVariable().getCvTermId();
					if (cvTermId.equals(Integer.parseInt(codeTermId)) && variableListMap.get(idTermId) != null) {
						iter.remove();
					}
				}
			}
		}
	}

	protected List<SettingDetail> getCheckVariables(final List<SettingDetail> nurseryLevelConditions, final CreateNurseryForm form) {
		final List<SettingDetail> checkVariables =
				this.getSettingDetailsOfSection(nurseryLevelConditions, form, AppConstants.CHECK_VARIABLES.getString());
		// set order by id
		Collections.sort(checkVariables, new Comparator<SettingDetail>() {

			@Override
			public int compare(final SettingDetail o1, final SettingDetail o2) {
				return o1.getVariable().getCvTermId() - o2.getVariable().getCvTermId();
			}
		});
		return checkVariables;
	}

	/**
	 * Gets the basic details.
	 *
	 * @param nurseryLevelConditions the nursery level conditions
	 * @return the basic details
	 */
	protected List<SettingDetail> getSettingDetailsOfSection(final List<SettingDetail> nurseryLevelConditions,
			final CreateNurseryForm form, final String variableList) {
		final List<SettingDetail> settingDetails = new ArrayList<>();

		final StringTokenizer token = new StringTokenizer(variableList, ",");
		while (token.hasMoreTokens()) {
			final Integer termId = Integer.valueOf(token.nextToken());
			final boolean isFound = this.searchAndSetValuesOfSpecialVariables(nurseryLevelConditions, termId, settingDetails, form);
			if (!isFound) {
				this.addSettingDetails(settingDetails, termId, form);
			}
		}

		return settingDetails;
	}

	private boolean searchAndSetValuesOfSpecialVariables(final List<SettingDetail> nurseryLevelConditions, final Integer termId,
			final List<SettingDetail> settingDetails, final CreateNurseryForm form) {
		boolean isFound = false;
		for (final SettingDetail setting : nurseryLevelConditions) {
			if (termId.equals(setting.getVariable().getCvTermId())) {
				isFound = true;
				this.setCreatedByAndStudyUpdate(termId, setting, form);
				settingDetails.add(setting);
			}
		}
		return isFound;
	}

	private void setCreatedByAndStudyUpdate(final Integer termId, final SettingDetail setting, final CreateNurseryForm form) {
		if (termId.equals(Integer.valueOf(TermId.STUDY_UID.getId()))) {
			try {
				if (setting.getValue() != null && !setting.getValue().isEmpty() && NumberUtils.isNumber(setting.getValue())) {
					form.setCreatedBy(this.fieldbookService.getPersonByUserId(Integer.parseInt(setting.getValue())));
				}
			} catch (final MiddlewareQueryException e) {
				SettingsController.LOG.error(e.getMessage(), e);
			}
		} else if (termId.equals(Integer.valueOf(TermId.STUDY_UPDATE.getId()))) {
			setting.setValue(DateUtil.getCurrentDateAsStringValue());
		}
	}

	private void addSettingDetails(final List<SettingDetail> settingDetails, final Integer termId, final CreateNurseryForm form) {
		try {
			settingDetails.add(this.createSettingDetail(termId, null, null));
			if (termId.equals(Integer.valueOf(TermId.STUDY_UID.getId()))) {
				form.setCreatedBy(this.fieldbookService.getPersonByUserId(this.getCurrentIbdbUserId()));
			}
		} catch (final MiddlewareException e) {
			SettingsController.LOG.error(e.getMessage(), e);
		}
	}

	protected void setUserSelection(final UserSelection userSelection) {
		this.userSelection = userSelection;
	}

	protected void addVariableInDeletedList(final List<SettingDetail> currentList, final int mode, final int variableId,
			final boolean createNewSettingIfNull) {
		SettingDetail newSetting = null;
		for (final SettingDetail setting : currentList) {
			if (setting.getVariable().getCvTermId().equals(Integer.valueOf(variableId))) {
				newSetting = setting;
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
		} else if (mode == VariableType.TRAIT.getId()) {
			if (this.userSelection.getDeletedBaselineTraitsList() == null) {
				this.userSelection.setDeletedBaselineTraitsList(new ArrayList<SettingDetail>());
			}
			this.userSelection.getDeletedBaselineTraitsList().add(newSetting);
		} else if (mode == VariableType.SELECTION_METHOD.getId()) {
			if (this.userSelection.getDeletedBaselineTraitsList() == null) {
				this.userSelection.setDeletedBaselineTraitsList(new ArrayList<SettingDetail>());
			}
			this.userSelection.getDeletedBaselineTraitsList().add(newSetting);
		} else if (mode == VariableType.NURSERY_CONDITION.getId() || mode == VariableType.TRIAL_CONDITION.getId()) {
			if (this.userSelection.getDeletedNurseryConditions() == null) {
				this.userSelection.setDeletedNurseryConditions(new ArrayList<SettingDetail>());
			}
			this.userSelection.getDeletedNurseryConditions().add(newSetting);
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
		}
	}

	public void setFieldbookService(final FieldbookService fieldbookService) {
		this.fieldbookService = fieldbookService;
	}

	/**
	 * These model attributes are used in UI JS code e.g. in createNursery.html and editNursery.html to identify various sections on screen
	 * where variables appear.
	 */
	protected void addVariableSectionIdentifiers(final Model model) {
		model.addAttribute("baselineTraitsSegment", VariableType.TRAIT.getId().intValue());
		model.addAttribute("selectionVariatesSegment", VariableType.SELECTION_METHOD.getId().intValue());
		model.addAttribute("studyLevelDetailType", VariableType.STUDY_DETAIL.getId().intValue());
		model.addAttribute("plotLevelDetailType", VariableType.GERMPLASM_DESCRIPTOR.getId().intValue());
		model.addAttribute("nurseryConditionsType", VariableType.NURSERY_CONDITION.getId().intValue());
	}
}
