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

package com.efficio.fieldbook.web.util;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.bean.PairedVariable;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.StudyDetails;
import com.efficio.fieldbook.web.common.bean.TreatmentFactorDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.TreatmentFactorData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.workbench.settings.Condition;
import org.generationcp.middleware.pojos.workbench.settings.Constant;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.pojos.workbench.settings.Factor;
import org.generationcp.middleware.pojos.workbench.settings.ParentDataset;
import org.generationcp.middleware.pojos.workbench.settings.TreatmentFactor;
import org.generationcp.middleware.pojos.workbench.settings.Variate;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

/**
 * The Class SettingsUtil.
 */
public class SettingsUtil {

	static final List<String> HIDDEN_FIELDS = Arrays.asList(AppConstants.HIDDEN_FIELDS.getString().split(","));
	static final List<String> STUDY_BASIC_REQUIRED_FIELDS =
		Arrays.asList(AppConstants.STUDY_BASIC_REQUIRED_FIELDS.getString().split(","));

	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(SettingsUtil.class);
	public static final String DESCRIPTION = "Description";
	private static final String START_DATE = "Creation date";
	private static final String END_DATE = "Completion date";
	private static final String STUDY_UPDATE = "Last updated";
	private static final String OBJECTIVE = "Objective";
	private static final String STUDY_NAME = "Study name";
	private static final String CREATED_BY = "Created by";
	private static final String STUDY_TYPE = "Study Type";

	private SettingsUtil() {
		// do nothing
	}

	public static String cleanSheetAndFileName(final String name) {
		String finalName = name;
		if (finalName == null) {
			return null;
		}
		// http://www.rgagnon.com/javadetails/java-0662.html
		finalName = finalName.replaceAll("[:\\\\/*?|<>]", "_");
		return finalName;
	}

	/**
	 * Get standard variable.
	 *
	 * @param id                         the id
	 * @param fieldbookMiddlewareService the fieldbook middleware service
	 * @param programUUID
	 * @return the standard variable
	 */
	private static StandardVariable getStandardVariable(
		final int id,
		final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, final String programUUID) {

		return fieldbookMiddlewareService.getStandardVariable(id, programUUID);
	}

	static List<Condition> convertDetailsToConditions(
		final List<SettingDetail> details,
		final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, final String programUUID) {
		final List<Condition> conditions = new ArrayList<>();

		if (details == null) {
			return conditions;
		}

		for (final SettingDetail settingDetail : details) {
			final SettingVariable variable = settingDetail.getVariable();

			final StandardVariable standardVariable =
				SettingsUtil.getStandardVariable(variable.getCvTermId(), fieldbookMiddlewareService, programUUID);

			if (standardVariable.getName() == null) {
				continue;
			}

			variable.setPSMRFromStandardVariable(standardVariable, settingDetail.getRole().name());

			if ((variable.getCvTermId().equals(TermId.BREEDING_METHOD_ID.getId()) || variable.getCvTermId().equals(
				TermId.BREEDING_METHOD_CODE.getId()))
				&& "0".equals(settingDetail.getValue())) {
				settingDetail.setValue("");
			}

			final Condition condition = new Condition(variable.getName(), variable.getDescription(), variable.getProperty(),
				variable.getScale(), variable.getMethod(), variable.getRole(), variable.getDataType(),
				DateUtil.convertToDBDateFormat(variable.getDataTypeId(), HtmlUtils.htmlEscape(settingDetail.getValue())),
				variable.getDataTypeId(), variable.getMinRange(), variable.getMaxRange());
			condition.setOperation(variable.getOperation());
			condition.setId(variable.getCvTermId());
			condition.setPossibleValues(settingDetail.getPossibleValues());
			conditions.add(condition);
		}

		return conditions;
	}

	static List<Variate> convertBaselineTraitsToVariates(
		final List<SettingDetail> baselineTraits,
		final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, final String programUUID) {
		final List<Variate> variateList = new ArrayList<>();
		if (baselineTraits == null || baselineTraits.isEmpty()) {
			return variateList;
		}

		for (final SettingDetail settingDetail : baselineTraits) {
			//Setting Detail's variable becomes null when the trait it represents is deleted.
			if (settingDetail.getVariable() != null) {
				final SettingVariable variable = settingDetail.getVariable();

				final StandardVariable standardVariable =
					SettingsUtil.getStandardVariable(variable.getCvTermId(), fieldbookMiddlewareService, programUUID);
				variable.setPSMRFromStandardVariable(standardVariable, settingDetail.getRole().name());

				final Variate variate = new Variate(variable.getName(), variable.getDescription(), variable.getProperty(),
					variable.getScale(), variable.getMethod(), variable.getRole(), variable.getDataType(), variable.getDataTypeId(),
					settingDetail.getPossibleValues(), variable.getMinRange(), variable.getMaxRange());
				if (settingDetail.getVariableType() != null) {
					variate.setVariableType(settingDetail.getVariableType().getName());
				}
				variate.setOperation(variable.getOperation());
				variate.setId(variable.getCvTermId());
				variateList.add(variate);
			}
		}

		return variateList;
	}

	private static List<Factor> convertDetailsToFactors(
		final List<SettingDetail> plotLevelDetails,
		final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, final String programUUID) {
		final List<Factor> factors = new ArrayList<>();

		if (plotLevelDetails == null || plotLevelDetails.isEmpty()) {
			return factors;
		}

		for (final SettingDetail settingDetail : plotLevelDetails) {
			final SettingVariable variable = settingDetail.getVariable();

			final StandardVariable standardVariable =
				SettingsUtil.getStandardVariable(variable.getCvTermId(), fieldbookMiddlewareService, programUUID);
			variable.setPSMRFromStandardVariable(standardVariable, settingDetail.getRole().name());

			final Factor factor = SettingsUtil.convertStandardVariableToFactor(standardVariable);
			factor.setOperation(variable.getOperation());
			factor.setPossibleValues(settingDetail.getPossibleValues());
			factor.setMinRange(variable.getMinRange());
			factor.setMaxRange(variable.getMaxRange());
			factor.setName(variable.getName());
			factors.add(factor);

		}

		return factors;
	}

	private static Factor convertStandardVariableToFactor(final StandardVariable variable) {
		final Factor factor = new Factor(variable.getName(), variable.getDescription(), variable.getProperty().getName(),
			variable.getScale().getName(), variable.getMethod().getName(), variable.getPhenotypicType().name(),
			variable.getDataType().getName(), variable.getId());

		factor.setId(variable.getId());
		factor.setDataTypeId(variable.getDataType().getId());

		return factor;
	}

	private static List<Constant> convertConditionsToConstants(
		final List<SettingDetail> studyConditions,
		final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, final String programUUID) {
		final List<Constant> constants = new ArrayList<>();

		if (studyConditions == null || studyConditions.isEmpty()) {
			return constants;
		}

		for (final SettingDetail settingDetail : studyConditions) {
			final SettingVariable variable = settingDetail.getVariable();

			final StandardVariable standardVariable =
				SettingsUtil.getStandardVariable(variable.getCvTermId(), fieldbookMiddlewareService, programUUID);

			variable.setPSMRFromStandardVariable(standardVariable, settingDetail.getRole().name());
			// need to get the name from the session

			final Constant constant = new Constant(variable.getName(), variable.getDescription(), variable.getProperty(),
				variable.getScale(), variable.getMethod(), variable.getRole(), variable.getDataType(),
				DateUtil.convertToDBDateFormat(variable.getDataTypeId(), HtmlUtils.htmlEscape(settingDetail.getValue())),
				variable.getDataTypeId(), variable.getMinRange(), variable.getMaxRange());
			constant.setOperation(variable.getOperation());
			constant.setId(variable.getCvTermId());
			constants.add(constant);

		}

		return constants;
	}

	static List<TreatmentFactor> processTreatmentFactorItems(
		final List<SettingDetail> treatmentFactorDetails,
		final Map<String, TreatmentFactorData> treatmentFactorItems, final List<Factor> factorList,
		final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, final String programUUID) {
		final List<TreatmentFactor> treatmentFactors = new ArrayList<>();
		if (MapUtils.isEmpty(treatmentFactorItems) || CollectionUtils.isEmpty(treatmentFactorDetails)) {
			return treatmentFactors;
		}
		final Map<Integer, Factor> factorsMap = new HashMap<>();
		for (final Factor factor : factorList) {
			factorsMap.put(factor.getId(), factor);
		}

		for (final SettingDetail detail : treatmentFactorDetails) {
			if (detail.getVariable().getOperation() != Operation.DELETE) {
				final Integer termId = detail.getVariable().getCvTermId();
				final StandardVariable levelVariable = SettingsUtil.getStandardVariable(termId, fieldbookMiddlewareService, programUUID);
				levelVariable.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
				final Factor levelFactor = SettingsUtil.convertStandardVariableToFactor(levelVariable);
				levelFactor.setName(detail.getVariable().getName());
				final Factor valueFactor;
				levelFactor.setOperation(detail.getVariable().getOperation());
				levelFactor.setTreatmentLabel(detail.getVariable().getName());

				final TreatmentFactorData data = treatmentFactorItems.get(termId.toString());

				if (data != null) {
					final StandardVariable valueVariable =
						SettingsUtil.getStandardVariable(data.getVariableId(), fieldbookMiddlewareService, programUUID);

					valueVariable.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
					valueFactor = SettingsUtil.convertStandardVariableToFactor(valueVariable);
					valueFactor.setOperation(detail.getVariable().getOperation());
					valueFactor.setTreatmentLabel(detail.getVariable().getName());

					int index = 1;
					for (final String labelValue : data.getLabels()) {
						final TreatmentFactor treatmentFactor = new TreatmentFactor(levelFactor, valueFactor, index, labelValue);
						treatmentFactors.add(treatmentFactor);
						index++;
					}
					valueFactor.setRole(detail.getRole().name());
					SettingsUtil.addFactor(factorsMap, valueFactor, factorList);
				}
				levelFactor.setRole(detail.getRole().name());
				SettingsUtil.addFactor(factorsMap, levelFactor, factorList);
			}
		}

		return treatmentFactors;
	}

	static void addFactor(final Map<Integer, Factor> factorsMap, final Factor factor, final List<Factor> factorList) {
		if (factorsMap.get(factor.getId()) == null) {
			factorList.add(factor);
			factorsMap.put(factor.getId(), factor);
		}
	}

	/**
	 * Convert pojo to xml dataset.
	 *
	 * @param fieldbookMiddlewareService the fieldbook middleware service
	 * @param name                       the name
	 * @param userSelection              the user selection
	 * @return the parent dataSet
	 */
	public static ParentDataset convertPojoToXmlDataSet(
		final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, final String name,
		final UserSelection userSelection, final Map<String, TreatmentFactorData> treatmentFactorItems, final String programUUID) {

		final List<SettingDetail> studyLevelConditions = userSelection.getStudyLevelConditions();
		final List<SettingDetail> basicDetails = userSelection.getBasicDetails();

		final List<SettingDetail> combinedList = new ArrayList<>();
		combinedList.addAll(basicDetails);
		combinedList.addAll(studyLevelConditions);

		final Dataset dataset = new Dataset();
		dataset.setName(name);
		dataset.setConditions(SettingsUtil.convertDetailsToConditions(combinedList, fieldbookMiddlewareService, programUUID));

		final List<Factor> factors =
			SettingsUtil.convertDetailsToFactors(userSelection.getPlotsLevelList(), fieldbookMiddlewareService, programUUID);

		dataset.setFactors(factors);

		final List<SettingDetail> variates = new ArrayList<>(userSelection.getBaselineTraitsList());

		dataset.setVariates(SettingsUtil.convertBaselineTraitsToVariates(variates, fieldbookMiddlewareService, programUUID));

		dataset.setConstants(
			SettingsUtil.convertConditionsToConstants(userSelection.getStudyConditions(), fieldbookMiddlewareService, programUUID));

		dataset.setTrialLevelFactor(
			SettingsUtil.convertDetailsToFactors(userSelection.getTrialLevelVariableList(), fieldbookMiddlewareService, programUUID));

		final List<TreatmentFactor> treatmentFactors = SettingsUtil.processTreatmentFactorItems(userSelection.getTreatmentFactors(),
			treatmentFactorItems, factors, fieldbookMiddlewareService, programUUID);

		dataset.setTreatmentFactors(treatmentFactors);

		return dataset;
	}

	/**
	 * Convert pojo to xml dataset.
	 *
	 * @param fieldbookMiddlewareService the fieldbook middleware service
	 * @param name                       the name
	 * @param studyLevelConditions       the study level conditions
	 * @param plotsLevelList             the plots level list
	 * @param baselineTraitsList         the baseline traits list
	 * @param trialLevelVariablesList    the trial level variables list
	 * @param description
	 * @return the parent dataset
	 */
	public static ParentDataset convertPojoToXmlDataset(
		final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, final String name,
		final List<SettingDetail> studyLevelConditions, final List<SettingDetail> plotsLevelList,
		final List<SettingDetail> baselineTraitsList, final List<SettingDetail> trialLevelVariablesList,
		final List<SettingDetail> treatmentFactorDetails, final Map<String, TreatmentFactorData> treatmentFactorItems,
		final List<SettingDetail> studyConditions, final List<SettingDetail> trialLevelConditions, final List<SettingDetail> entryDetails,
		final String programUUID, final String description, final String startDate, final String endDate, final String studyUpdate) {

		// name and operation setting are no longer performed on the other
		// setting lists provided as params in this method
		// because those are only defined for trials
		// assumption is that params provided from trial management do not
		// need this operation

		final List<Condition> conditions =
			SettingsUtil.convertDetailsToConditions(studyLevelConditions, fieldbookMiddlewareService, programUUID);
		final List<Factor> factors = SettingsUtil.convertDetailsToFactors(plotsLevelList, fieldbookMiddlewareService, programUUID);

		factors.addAll(SettingsUtil.convertDetailsToFactors(entryDetails, fieldbookMiddlewareService, programUUID));

		final List<Variate> variates =
			SettingsUtil.convertBaselineTraitsToVariates(baselineTraitsList, fieldbookMiddlewareService, programUUID);
		final List<Constant> constants =
			SettingsUtil.convertConditionsToConstants(studyConditions, fieldbookMiddlewareService, programUUID);
		final List<Factor> trialLevelVariables =
			SettingsUtil.convertDetailsToFactors(trialLevelVariablesList, fieldbookMiddlewareService, programUUID);

		final List<TreatmentFactor> treatmentFactors = SettingsUtil.processTreatmentFactorItems(treatmentFactorDetails,
			treatmentFactorItems, factors, fieldbookMiddlewareService, programUUID);

		constants.addAll(SettingsUtil.convertConditionsToConstants(trialLevelConditions, fieldbookMiddlewareService, programUUID));

		final Dataset dataset = new Dataset();
		dataset.setConditions(conditions);
		dataset.setFactors(factors);
		dataset.setVariates(variates);
		dataset.setConstants(constants);
		dataset.setName(name);
		dataset.setDescription(description);
		dataset.setStartDate(startDate);
		dataset.setEndDate(endDate);
		dataset.setStudyUpdate(studyUpdate);
		if (trialLevelVariablesList != null) {
			dataset.setTrialLevelFactor(trialLevelVariables);
			dataset.setTreatmentFactors(treatmentFactors);
		}

		return dataset;
	}

	/**
	 * Checks if is setting variable deletable.
	 *
	 * @param standardVariableId the standard variable id
	 * @param requiredFields     the required fields
	 * @return true, if is setting variable deletable
	 */
	private static boolean isSettingVariableDeletable(final Integer standardVariableId, final String requiredFields) {
		// need to add the checking here if the specific PSM-R is deletable, for
		// the study level details
		final StringTokenizer token = new StringTokenizer(requiredFields, ",");
		while (token.hasMoreTokens()) {
			if (standardVariableId.equals(Integer.parseInt(token.nextToken()))) {
				return false;
			}
		}
		return true;
	}

	public static Map<String, MeasurementVariable> buildMeasurementVariableMap(final List<MeasurementVariable> factors) {
		final Map<String, MeasurementVariable> factorsMap = new HashMap<>();
		for (final MeasurementVariable factor : factors) {
			factorsMap.put(String.valueOf(factor.getTermId()), factor);
		}
		return factorsMap;
	}

	public static String getNameCounterpart(final Integer idTermId, final String idNameCombination) {
		final StringTokenizer tokenizer = new StringTokenizer(idNameCombination, ",");
		while (tokenizer.hasMoreTokens()) {
			final String pair = tokenizer.nextToken();
			final StringTokenizer tokenizerPair = new StringTokenizer(pair, "|");
			if (tokenizerPair.nextToken().equals(String.valueOf(idTermId))) {
				return tokenizerPair.nextToken();
			}
		}
		return "";
	}

	public static List<Integer> parseFieldListAndConvertToListOfIDs(final String selectedFieldList) {
		final List<Integer> selectedFieldIDs = new ArrayList<>();
		final String[] split = selectedFieldList.split(",");
		for (final String s : split) {
			try {
				if ("".equals(s)) {
					continue;
				}

				final Integer termID = new Integer(s);
				final String nameTermID = SettingsUtil.getNameCounterpart(termID, AppConstants.ID_NAME_COMBINATION.getString());

				if ("".equals(nameTermID)) {
					selectedFieldIDs.add(termID);
				} else {
					selectedFieldIDs.add(new Integer(nameTermID));
				}
			} catch (final NumberFormatException e) {
				SettingsUtil.LOG.error(e.getMessage());
			}
		}

		return selectedFieldIDs;
	}

	public static boolean inPropertyList(final int propertyId) {
		final StringTokenizer token = new StringTokenizer(AppConstants.SELECTION_VARIATES_PROPERTIES.getString(), ",");
		while (token.hasMoreTokens()) {
			final int propId = Integer.parseInt(token.nextToken());

			if (propId == propertyId) {
				return true;
			}
		}
		return false;
	}

	/**
	 * In hide variable fields.
	 *
	 * @param stdVarId     the std var id
	 * @param variableList the variable list
	 * @return true, if successful
	 */
	public static boolean inHideVariableFields(final Integer stdVarId, final String variableList) {
		final StringTokenizer token = new StringTokenizer(variableList, ",");
		boolean inList = false;
		while (token.hasMoreTokens()) {
			if (stdVarId.equals(Integer.parseInt(token.nextToken()))) {
				inList = true;
				break;
			}
		}
		return inList;
	}

	public static Workbook convertXmlDatasetToWorkbook(final ParentDataset dataset, final String programUUID) {
		return SettingsUtil.convertXmlDatasetToWorkbook(dataset, null, null, null, null, programUUID);
	}

	/**
	 * Convert xml dataset to workbook.
	 *
	 * @param dataset the dataset
	 * @return the workbook
	 */
	public static Workbook convertXmlDatasetToWorkbook(
		final ParentDataset dataset, final ExpDesignParameterUi param,
		final List<Integer> variables, final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService,
		final List<MeasurementVariable> allExpDesignVariables, final String programUUID) {

		final Workbook workbook = new Workbook();
		final Dataset studyDataSet = (Dataset) dataset;

		workbook.setConditions(SettingsUtil.convertConditionsToMeasurementVariables(studyDataSet.getConditions()));
		workbook.setFactors(SettingsUtil.convertFactorsToMeasurementVariables(studyDataSet.getFactors()));
		workbook.setVariates(SettingsUtil.convertVariatesToMeasurementVariables(studyDataSet.getVariates()));
		workbook.getConditions().addAll(SettingsUtil.convertFactorsToMeasurementVariables(studyDataSet.getTrialLevelFactor()));
		workbook.setConstants(SettingsUtil.convertConstantsToMeasurementVariables(studyDataSet.getConstants()));

		if (workbook.getTreatmentFactors() == null) {
			workbook.setTreatmentFactors(new ArrayList<TreatmentVariable>());
		}
		workbook.getTreatmentFactors().addAll(SettingsUtil.convertTreatmentFactorsToTreatmentVariables(studyDataSet.getTreatmentFactors()));
		SettingsUtil.setExperimentalDesignToWorkbook(param, variables, workbook, allExpDesignVariables, fieldbookMiddlewareService,
			programUUID);

		workbook.setEntryDetails(SettingsUtil.convertFactorsToMeasurementVariables(studyDataSet.getFactors().stream().filter(factor -> factor.getRole().equals(PhenotypicType.ENTRY_DETAIL.name())).collect(
			Collectors.toList())));

		return workbook;
	}

	/**
	 * Convert workbook to xml dataset.
	 *
	 * @param workbook the workbook
	 * @return the dataset
	 */
	static ParentDataset convertWorkbookToXmlDataset(final Workbook workbook) {
		final Dataset dataset = new Dataset();

		final List<Condition> conditions = SettingsUtil.convertMeasurementVariablesToConditions(workbook.getStudyConditions());
		final List<Factor> factors = SettingsUtil.convertMeasurementVariablesToFactors(workbook.getFactors());
		final List<Variate> variates = SettingsUtil.convertMeasurementVariablesToVariates(workbook.getVariates());
		final List<Constant> constants = SettingsUtil.convertMeasurementVariablesToConstants(workbook.getConstants());
		final List<TreatmentFactor> treatmentFactors =
			SettingsUtil.convertTreatmentVariablesToTreatmentFactors(workbook.getTreatmentFactors());

		dataset.setConditions(conditions);
		dataset.setFactors(factors);
		dataset.setVariates(variates);
		dataset.setConstants(constants);
		dataset.setTrialLevelFactor(SettingsUtil.convertMeasurementVariablesToFactors(workbook.getTrialConditions()));
		dataset.setTreatmentFactors(treatmentFactors);

		return dataset;
	}

	/**
	 * Convert measurement variables to conditions.
	 *
	 * @param mlist the mlist
	 * @return the list
	 */
	private static List<Condition> convertMeasurementVariablesToConditions(final List<MeasurementVariable> mlist) {
		final List<Condition> conditions = new ArrayList<>();

		if (mlist != null && !mlist.isEmpty()) {
			for (final MeasurementVariable mvar : mlist) {
				final Condition condition = new Condition(mvar.getName(), mvar.getDescription(), mvar.getProperty(), mvar.getScale(),
					mvar.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(mvar.getLabel()).toString(), mvar.getDataType(),
					mvar.getValue(), null, null, null);
				condition.setId(mvar.getTermId());
				conditions.add(condition);
			}
		}

		return conditions;
	}

	private static List<Constant> convertMeasurementVariablesToConstants(final List<MeasurementVariable> mlist) {
		final List<Constant> constants = new ArrayList<>();

		if (mlist != null && !mlist.isEmpty()) {

			for (final MeasurementVariable mvar : mlist) {
				final Constant constant = new Constant(mvar.getName(), mvar.getDescription(), mvar.getProperty(), mvar.getScale(),
					mvar.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(mvar.getLabel()).toString(), mvar.getDataType(),
					mvar.getValue(), null, null, null);
				constant.setId(mvar.getTermId());
				constants.add(constant);
			}
		}

		return constants;
	}

	/**
	 * Convert measurement variables to factors.
	 *
	 * @param mlist the mlist
	 * @return the list
	 */
	private static List<Factor> convertMeasurementVariablesToFactors(final List<MeasurementVariable> mlist) {
		final List<Factor> factors = new ArrayList<>();

		if (mlist != null && !mlist.isEmpty()) {
			for (final MeasurementVariable mvar : mlist) {
				final Factor factor =
					new Factor(mvar.getName(), mvar.getDescription(), mvar.getProperty(), mvar.getScale(), mvar.getMethod(),
						PhenotypicType.getPhenotypicTypeForLabel(mvar.getLabel()).toString(), mvar.getDataType(), mvar.getTermId());
				factor.setTreatmentLabel(mvar.getTreatmentLabel());
				factor.setId(mvar.getTermId());
				factors.add(factor);
			}
		}

		return factors;
	}

	private static List<TreatmentFactor> convertTreatmentVariablesToTreatmentFactors(final List<TreatmentVariable> mlist) {
		final List<TreatmentFactor> factors = new ArrayList<>();

		if (mlist != null && !mlist.isEmpty()) {
			Factor levelFactor;
			Factor valueFactor;
			for (final TreatmentVariable var : mlist) {
				final MeasurementVariable mvar = var.getLevelVariable();
				final MeasurementVariable vvar = var.getValueVariable();
				levelFactor = new Factor(mvar.getName(), mvar.getDescription(), mvar.getProperty(), mvar.getScale(), mvar.getMethod(),
					PhenotypicType.getPhenotypicTypeForLabel(mvar.getLabel()).toString(), mvar.getDataType(), mvar.getTermId());
				valueFactor = new Factor(vvar.getName(), vvar.getDescription(), vvar.getProperty(), vvar.getScale(), vvar.getMethod(),
					PhenotypicType.getPhenotypicTypeForLabel(vvar.getLabel()).toString(), vvar.getDataType(), vvar.getTermId());
				factors.add(new TreatmentFactor(levelFactor, valueFactor));
			}
		}

		return factors;
	}

	/**
	 * Convert measurement variables to variates.
	 *
	 * @param mlist the mlist
	 * @return the list
	 */
	private static List<Variate> convertMeasurementVariablesToVariates(final List<MeasurementVariable> mlist) {
		final List<Variate> variates = new ArrayList<>();

		if (mlist != null && !mlist.isEmpty()) {
			for (final MeasurementVariable mvar : mlist) {
				final Variate variate = new Variate(mvar.getName(), mvar.getDescription(), mvar.getProperty(), mvar.getScale(),
					mvar.getMethod(), PhenotypicType.VARIATE.toString(), mvar.getDataType(), mvar.getDataTypeId(),
					mvar.getPossibleValues(), mvar.getMinRange(), mvar.getMaxRange());
				variate.setVariableType(mvar.getVariableType().getName());
				variate.setId(mvar.getTermId());
				variates.add(variate);
			}
		}

		return variates;
	}

	/**
	 * Convert conditions to measurement variables.
	 *
	 * @param conditions the conditions
	 * @return the list
	 */
	private static List<MeasurementVariable> convertConditionsToMeasurementVariables(final List<Condition> conditions) {
		final List<MeasurementVariable> list = new ArrayList<>();
		if (conditions != null && !conditions.isEmpty()) {
			for (final Condition condition : conditions) {
				list.add(SettingsUtil.convertConditionToMeasurementVariable(condition));
			}
		}
		return list;
	}

	private static List<MeasurementVariable> convertConstantsToMeasurementVariables(final List<Constant> constants) {
		final List<MeasurementVariable> list = new ArrayList<>();
		if (constants != null && !constants.isEmpty()) {
			for (final Constant constant : constants) {
				list.add(SettingsUtil.convertConstantToMeasurementVariable(constant));
			}
		}
		return list;
	}

	/**
	 * Convert condition to measurement variable.
	 *
	 * @param condition the condition
	 * @return the measurement variable
	 */
	static MeasurementVariable convertConditionToMeasurementVariable(final Condition condition) {
		final String label;
		label = PhenotypicType.valueOf(condition.getRole()).getLabelList().get(0);
		final MeasurementVariable measurementVariable =
			new MeasurementVariable(condition.getName(), condition.getDescription(), condition.getScale(), condition.getMethod(),
				condition.getProperty(), condition.getDatatype(), condition.getValue(), label, condition.getMinRange(),
				condition.getMaxRange(), PhenotypicType.getPhenotypicTypeByName(condition.getRole()));
		measurementVariable.setOperation(condition.getOperation());
		measurementVariable.setTermId(condition.getId());
		measurementVariable.setFactor(true);
		measurementVariable.setDataTypeId(condition.getDataTypeId());
		measurementVariable.setPossibleValues(condition.getPossibleValues());
		return measurementVariable;
	}

	static MeasurementVariable convertConstantToMeasurementVariable(final Constant constant) {
		String label = constant.getLabel();

		// currently if operation is add, then it's always a trial constant
		if (constant.getOperation() == Operation.ADD || (label == null && constant.getOperation() == Operation.UPDATE)) {
			label = PhenotypicType.TRIAL_ENVIRONMENT.getLabelList().get(0);
		}

		final MeasurementVariable mvar = new MeasurementVariable(constant.getName(), constant.getDescription(), constant.getScale(),
			constant.getMethod(), constant.getProperty(), constant.getDatatype(), constant.getValue(), label, constant.getMinRange(),
			constant.getMaxRange(), PhenotypicType.getPhenotypicTypeByName(constant.getRole()));

		mvar.setOperation(constant.getOperation());
		mvar.setTermId(constant.getId());
		mvar.setFactor(false);
		mvar.setDataTypeId(constant.getDataTypeId());
		mvar.setPossibleValues(constant.getPossibleValues());
		mvar.setVariableType(VariableType.ENVIRONMENT_CONDITION);
		return mvar;
	}

	/**
	 * Convert factors to measurement variables.
	 *
	 * @param factors the factors
	 * @return the list
	 */
	private static List<MeasurementVariable> convertFactorsToMeasurementVariables(final List<Factor> factors) {
		final List<MeasurementVariable> list = new ArrayList<>();
		if (factors != null && !factors.isEmpty()) {
			for (final Factor factor : factors) {
				list.add(SettingsUtil.convertFactorToMeasurementVariable(factor));
			}
		}
		return list;
	}

	/**
	 * Convert factor to measurement variable.
	 *
	 * @param factor the factor
	 * @return the measurement variable
	 */
	private static MeasurementVariable convertFactorToMeasurementVariable(final Factor factor) {
		final MeasurementVariable mvar = new MeasurementVariable(factor.getName(), factor.getDescription(), factor.getScale(),
			factor.getMethod(), factor.getProperty(), factor.getDatatype(), null,
			PhenotypicType.valueOf(factor.getRole()).getLabelList().get(0), PhenotypicType.getPhenotypicTypeByName(factor.getRole()));
		mvar.setFactor(true);
		mvar.setOperation(factor.getOperation());
		mvar.setTermId(factor.getId());
		mvar.setTreatmentLabel(factor.getTreatmentLabel());
		mvar.setDataTypeId(factor.getDataTypeId());
		mvar.setPossibleValues(factor.getPossibleValues());
		mvar.setMinRange(factor.getMinRange());
		mvar.setMaxRange(factor.getMaxRange());
		return mvar;
	}

	private static List<TreatmentVariable> convertTreatmentFactorsToTreatmentVariables(final List<TreatmentFactor> factors) {
		final List<TreatmentVariable> list = new ArrayList<>();
		if (factors != null && !factors.isEmpty()) {
			for (final TreatmentFactor factor : factors) {
				list.add(SettingsUtil.convertTreatmentFactorToTreatmentVariable(factor));
			}
		}
		return list;
	}

	private static TreatmentVariable convertTreatmentFactorToTreatmentVariable(final TreatmentFactor factor) {
		final TreatmentVariable mvar = new TreatmentVariable();
		final MeasurementVariable levelVariable = SettingsUtil.convertFactorToMeasurementVariable(factor.getLevelFactor());
		final MeasurementVariable valueVariable = SettingsUtil.convertFactorToMeasurementVariable(factor.getValueFactor());
		levelVariable.setValue(factor.getLevelNumber() != null ? factor.getLevelNumber().toString() : null);
		levelVariable.setTreatmentLabel(factor.getLevelFactor().getName());
		valueVariable.setValue(factor.getValue());
		valueVariable.setTreatmentLabel(factor.getLevelFactor().getName());
		mvar.setLevelVariable(levelVariable);
		mvar.setValueVariable(valueVariable);
		return mvar;
	}

	/**
	 * Convert variates to measurement variables.
	 *
	 * @param variates the variates
	 * @return the list
	 */
	private static List<MeasurementVariable> convertVariatesToMeasurementVariables(final List<Variate> variates) {
		final List<MeasurementVariable> list = new ArrayList<>();
		if (variates != null && !variates.isEmpty()) {
			for (final Variate variate : variates) {
				list.add(SettingsUtil.convertVariateToMeasurementVariable(variate));
			}
		}
		return list;
	}

	/**
	 * Convert variate to measurement variable.
	 *
	 * @param variate the variate
	 * @return the measurement variable
	 */
	private static MeasurementVariable convertVariateToMeasurementVariable(final Variate variate) {
		// because variates are mostly PLOT variables
		final MeasurementVariable mvar = new MeasurementVariable(variate.getName(), variate.getDescription(), variate.getScale(),
			variate.getMethod(), variate.getProperty(), variate.getDatatype(), null, PhenotypicType.TRIAL_DESIGN.getLabelList().get(0),
			variate.getMinRange(), variate.getMaxRange(), PhenotypicType.getPhenotypicTypeByName(variate.getRole()));
		if (variate.getVariableType() != null) {
			mvar.setVariableType(VariableType.getByName(variate.getVariableType()));
		}
		mvar.setOperation(variate.getOperation());
		mvar.setTermId(variate.getId());
		mvar.setFactor(false);
		mvar.setDataTypeId(variate.getDataTypeId());
		mvar.setPossibleValues(variate.getPossibleValues());
		return mvar;
	}

	public static StudyDetails convertWorkbookToStudyDetails(
		final Workbook workbook, final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService,
		final FieldbookService fieldbookService, final UserSelection userSelection, final String programUUID,
		final Properties appConstantsProperties, final String createdBy) {

		final StudyDetails studyDetails =
			SettingsUtil.convertWorkbookStudyLevelVariablesToStudyDetails(workbook, fieldbookMiddlewareService, fieldbookService,
				userSelection, programUUID, appConstantsProperties, createdBy);

		if (workbook.getTrialDatasetId() != null) {
			studyDetails.setNumberOfEnvironments(
				Long.valueOf(fieldbookMiddlewareService.countObservations(workbook.getTrialDatasetId())).intValue());
		} else {
			studyDetails.setNumberOfEnvironments(0);
		}

		final List<SettingDetail> factors =
			SettingsUtil.convertWorkbookFactorsToSettingDetails(workbook.getNonTrialFactors(), fieldbookMiddlewareService);
		final List<SettingDetail> germplasmDescriptors = new ArrayList<>();
		SettingsUtil.rearrangeSettings(factors, germplasmDescriptors, PhenotypicType.GERMPLASM);
		studyDetails.setGermplasmDescriptors(germplasmDescriptors);
		final List<SettingDetail> entryDetails =
			SettingsUtil.convertWorkbookFactorsToSettingDetails(workbook.getEntryDetails(), fieldbookMiddlewareService);
		studyDetails.setEntryDetails(entryDetails);
		final List<TreatmentFactorDetail> treatmentFactorDetails =
			SettingsUtil.convertWorkbookFactorsToTreatmentDetailFactors(workbook.getTreatmentFactors());
		studyDetails.setTreatmentFactorDetails(treatmentFactorDetails);
		studyDetails.setFactorDetails(factors);
		final List<SettingDetail> traits = new ArrayList<>();
		final List<SettingDetail> selectionVariateDetails = new ArrayList<>();
		SettingsUtil.convertWorkbookVariatesToSettingDetails(workbook.getVariates(), fieldbookMiddlewareService, traits,
			selectionVariateDetails);
		studyDetails.setVariateDetails(traits);
		studyDetails.setSelectionVariateDetails(selectionVariateDetails);
		studyDetails.setExperimentalDesignDetails(workbook.getExperimentalDesignVariables());

		return studyDetails;
	}

	static StudyDetails convertWorkbookStudyLevelVariablesToStudyDetails(
		final Workbook workbook,
		final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, final FieldbookService fieldbookService,
		final UserSelection userSelection, final String programUUID, final Properties appConstantsProperties, final String createdBy) {

		final StudyDetails details = new StudyDetails();
		final org.generationcp.middleware.domain.etl.StudyDetails sourceStudyDetails = workbook.getStudyDetails();
		details.setId(sourceStudyDetails.getId());
		details.setProgramUUID(sourceStudyDetails.getProgramUUID());
		details.setIsLocked(sourceStudyDetails.getIsLocked());
		if (sourceStudyDetails.getCreatedBy() != null) {
			details.setOwnerId(Integer.valueOf(sourceStudyDetails.getCreatedBy()));
		}

		final List<MeasurementVariable> conditions = workbook.getConditions();
		final List<MeasurementVariable> constants = workbook.getConstants();

		List<SettingDetail> basicDetails = new ArrayList<>();
		List<SettingDetail> managementDetails = new ArrayList<>();
		List<SettingDetail> studyConditionDetails = new ArrayList<>();

		final List<String> basicFields;
		basicFields = Arrays.asList(AppConstants.STUDY_BASIC_REQUIRED_FIELDS.getString().split(","));

		if (conditions != null) {
			final String studyName = sourceStudyDetails.getStudyName();
			if (studyName != null) {
				details.setName(studyName);
			}
			basicDetails = SettingsUtil
				.convertWorkbookToSettingDetails(basicFields, conditions, fieldbookMiddlewareService, fieldbookService, userSelection,
					workbook, programUUID, appConstantsProperties, createdBy);
			managementDetails = SettingsUtil
				.convertWorkbookOtherStudyVariablesToSettingDetails(conditions, managementDetails.size(), userSelection,
					fieldbookMiddlewareService, fieldbookService, programUUID);
			studyConditionDetails = SettingsUtil
				.convertWorkbookOtherStudyVariablesToSettingDetails(constants, 1, userSelection, fieldbookMiddlewareService,
					fieldbookService, programUUID);
		}

		final List<SettingDetail> environmentManagementDetails = new ArrayList<>();
		SettingsUtil.rearrangeSettings(managementDetails, environmentManagementDetails, PhenotypicType.TRIAL_ENVIRONMENT);
		details.setEnvironmentManagementDetails(environmentManagementDetails);

		details.setBasicStudyDetails(basicDetails);
		details.setManagementDetails(managementDetails);
		details.setStudyConditionDetails(studyConditionDetails);
		return details;
	}

	private static List<SettingDetail> convertWorkbookOtherStudyVariablesToSettingDetails(
		final List<MeasurementVariable> conditions,
		final int orderIndex, final UserSelection userSelection,
		final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService,
		final FieldbookService fieldbookService, final String programUUID) {
		int index = orderIndex;

		final List<SettingDetail> details = new ArrayList<>();

		if (conditions == null) {
			return details;
		}

		final Map<String, MeasurementVariable> variableMap = new HashMap<>();

		for (final MeasurementVariable condition : conditions) {
			variableMap.put(String.valueOf(condition.getTermId()), condition);
		}

		for (final MeasurementVariable condition : conditions) {
			final String id = String.valueOf(condition.getTermId());
			final String role = condition.getRole().name();
			if (!SettingsUtil.isIdInFieldListForHiding(id)
				// do not show breeding method id if code exists
				&& !SettingsUtil.breedingCodeExists(condition.getTermId(), variableMap)) {
				// do not name if code or id exists

				final SettingVariable variable =
					SettingsUtil.getSettingVariable(SettingsUtil.getDisplayName(conditions, condition.getTermId(), condition.getName()),
						condition.getDescription(), condition.getProperty(), condition.getScale(), condition.getMethod(), role,
						condition.getDataType(), condition.getDataTypeId(), condition.getMinRange(), condition.getMaxRange(),
						userSelection, fieldbookMiddlewareService, programUUID);
				variable.setCvTermId(condition.getTermId());
				final String value = fieldbookService.getValue(variable.getCvTermId(), HtmlUtils.htmlUnescape(condition.getValue()),
					condition.getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId());
				final SettingDetail settingDetail = new SettingDetail(variable, null, HtmlUtils.htmlUnescape(value), false);
				settingDetail.setPossibleValues(fieldbookService.getAllPossibleValues(condition.getTermId()));
				index = SettingsUtil.addToList(details, settingDetail, index, null, null);
			}
		}

		return details;
	}

	private static boolean isIdInFieldListForHiding(final String termId) {
		final List<String> basicFields;
		basicFields = SettingsUtil.STUDY_BASIC_REQUIRED_FIELDS;
		return basicFields.contains(termId) || SettingsUtil.HIDDEN_FIELDS.contains(termId);
	}

	private static boolean breedingCodeExists(final Integer termId, final Map<String, MeasurementVariable> variableMap) {
		return termId == TermId.BREEDING_METHOD_ID.getId() && variableMap.get(String.valueOf(TermId.BREEDING_METHOD_CODE.getId())) != null
			|| termId == TermId.BREEDING_METHOD.getId() && (variableMap.get(String.valueOf(TermId.BREEDING_METHOD_CODE.getId())) != null
			|| variableMap.get(String.valueOf(TermId.BREEDING_METHOD_ID.getId())) != null);
	}

	private static String getDisplayName(final List<MeasurementVariable> variables, final int termId, final String name) {
		if (AppConstants.getString(termId + AppConstants.LABEL.getString()) != null) {
			return AppConstants.getString(termId + AppConstants.LABEL.getString());
		} else {
			final Map<String, String> map = AppConstants.ID_NAME_COMBINATION.getMapOfValues();
			final String pair = map.get(String.valueOf(termId));
			if (pair != null) {
				for (final MeasurementVariable variable : variables) {
					if (pair.equals(String.valueOf(variable.getTermId()))) {
						return variable.getName();
					}
				}
			}
		}
		return name;
	}

	private static List<SettingDetail> convertWorkbookToSettingDetails(
		final List<String> fields,
		final List<MeasurementVariable> conditions,
		final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService,
		final FieldbookService fieldbookService, final UserSelection userSelection, final Workbook workbook, final String programUUID,
		final Properties appConstantsProperties, final String createdBy) {

		int index = fields != null ? fields.size() : 0;
		final List<SettingDetail> details = new ArrayList<>();

		final String studyName = workbook.getStudyDetails().getStudyName() != null ? workbook.getStudyDetails().getStudyName() : "";
		final String description = workbook.getStudyDetails().getDescription() != null ? workbook.getStudyDetails().getDescription() : "";
		final String startDate = workbook.getStudyDetails().getStartDate() != null ? workbook.getStudyDetails().getStartDate() : "";
		final String endDate = workbook.getStudyDetails().getEndDate() != null ? workbook.getStudyDetails().getEndDate() : "";
		final String studyUpdate = workbook.getStudyDetails().getStudyUpdate() != null ? workbook.getStudyDetails().getStudyUpdate() : "";
		final String objective = workbook.getStudyDetails().getObjective() != null ? workbook.getStudyDetails().getObjective() : "";

		final String studyType = workbook.getStudyDetails().getStudyType().getLabel();
		Integer datasetId = workbook.getMeasurementDatesetId();
		if (datasetId == null) {
			datasetId = fieldbookMiddlewareService.getMeasurementDatasetId(workbook.getStudyDetails().getId());
		}

		final List<String> labelFieldsWithPairedVariable = new ArrayList<>(fields);
		labelFieldsWithPairedVariable.add(AppConstants.SPFLD_PLOT_COUNT.getString());
		final Map<String, String> variableAppConstantLabels =
			SettingsUtil.getVariableAppConstantLabels(labelFieldsWithPairedVariable, appConstantsProperties);

		for (final String strFieldId : fields) {
			if (StringUtils.isEmpty(strFieldId) || conditions == null) {
				continue;
			}

			boolean found = false;
			String label = variableAppConstantLabels.get(strFieldId);

			// label field is a UI construct for the Settings sections of the fieldbook UI (a label field and its value which can be a
			// textfield, number or dropdown)
			// usually a label field contains the ontology measurement variable name and its value of a study.
			// special field is a label field that contains additional logic for determining its proper label and value
			// see SettingsUtil.getSpecialFieldValue()

			for (final MeasurementVariable condition : conditions) {
				if (NumberUtils.isNumber(strFieldId)) {
					if (condition.getTermId() == Integer.valueOf(strFieldId)) {
						if (label == null || "".equals(label.trim())) {
							label = condition.getName();
						}
						final SettingVariable variable = SettingsUtil.getSettingVariable(label, condition.getDescription(),
							condition.getProperty(), condition.getScale(), condition.getMethod(), condition.getRole().name(),
							condition.getDataType(), condition.getDataTypeId(), condition.getMinRange(), condition.getMaxRange(),
							userSelection, fieldbookMiddlewareService, programUUID);
						variable.setCvTermId(Integer.valueOf(strFieldId));
						final String value = fieldbookService.getValue(variable.getCvTermId(), HtmlUtils.htmlUnescape(condition.getValue()),
							condition.getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId());
						final SettingDetail settingDetail = new SettingDetail(variable, null, HtmlUtils.htmlUnescape(value), false);
						index = SettingsUtil.addToList(details, settingDetail, index, fields, strFieldId);
						found = true;
						break;
					}
				} else {
					// special field logic
					// FIXME BMS-4397
					if (DESCRIPTION.equals(label)) {
						final SettingVariable variableDescription =
							new SettingVariable(DESCRIPTION, null, null, null, null, null, null, null, null, null);
						final SettingDetail settingDetailDescription = new SettingDetail(variableDescription, null, description, false);
						index = SettingsUtil.addToList(details, settingDetailDescription, index, fields, strFieldId);
						found = true;
						break;
					} else if (STUDY_TYPE.equals(label)) {
						final SettingVariable variableStudyType =
							new SettingVariable(STUDY_TYPE, null, null, null, null, null, null, null, null, null);
						final SettingDetail settingDetailDescription = new SettingDetail(variableStudyType, null, studyType, false);
						index = SettingsUtil.addToList(details, settingDetailDescription, index, fields, strFieldId);
						found = true;
						break;
					} else if (START_DATE.equals(label)) {
						final SettingVariable variableDescription =
							new SettingVariable(START_DATE, null, null, null, null, null, null, null, null, null);
						final SettingDetail settingDetailDescription = new SettingDetail(variableDescription, null, startDate, false);
						index = SettingsUtil.addToList(details, settingDetailDescription, index, fields, strFieldId);
						found = true;
						break;
					} else if (END_DATE.equals(label)) {
						final SettingVariable variableDescription =
							new SettingVariable(END_DATE, null, null, null, null, null, null, null, null, null);
						final SettingDetail settingDetailDescription = new SettingDetail(variableDescription, null, endDate, false);
						index = SettingsUtil.addToList(details, settingDetailDescription, index, fields, strFieldId);
						found = true;
						break;
					} else if (STUDY_UPDATE.equals(label)) {
						final SettingVariable variableDescription =
							new SettingVariable(STUDY_UPDATE, null, null, null, null, null, null, null, null, null);
						final SettingDetail settingDetailDescription = new SettingDetail(variableDescription, null, studyUpdate, false);
						index = SettingsUtil.addToList(details, settingDetailDescription, index, fields, strFieldId);
						found = true;
						break;
					} else if (OBJECTIVE.equals(label)) {
						final SettingVariable variableDescription =
							new SettingVariable(OBJECTIVE, null, null, null, null, null, null, null, null, null);
						final SettingDetail settingDetailDescription = new SettingDetail(variableDescription, null, objective, false);
						index = SettingsUtil.addToList(details, settingDetailDescription, index, fields, strFieldId);
						found = true;
						break;
					} else if (STUDY_NAME.equals(label)) {
						final SettingVariable variableDescription =
							new SettingVariable(STUDY_NAME, null, null, null, null, null, null, null, null, null);
						final SettingDetail settingDetailDescription = new SettingDetail(variableDescription, null, studyName, false);
						index = SettingsUtil.addToList(details, settingDetailDescription, index, fields, strFieldId);
						found = true;
						break;
					} else if (CREATED_BY.equals(label)) {
						final SettingVariable variableDescription =
							new SettingVariable(CREATED_BY, null, null, null, null, null, null, null, null, null);
						final SettingDetail settingDetailDescription = new SettingDetail(variableDescription, null, createdBy, false);
						index = SettingsUtil.addToList(details, settingDetailDescription, index, fields, strFieldId);
						found = true;
						break;
					} else {
						final SettingVariable variable = new SettingVariable(label, null, null, null, null, null, null, null, null, null);
						final String value = SettingsUtil.getSpecialFieldValue(strFieldId, datasetId, fieldbookMiddlewareService, workbook);
						final SettingDetail settingDetail = new SettingDetail(variable, null, value, false);
						if (strFieldId.equals(AppConstants.SPFLD_ENTRIES.getString())) {
							final String plotValue = SettingsUtil
								.getSpecialFieldValue(AppConstants.SPFLD_PLOT_COUNT.getString(), datasetId, fieldbookMiddlewareService,
									workbook);
							final PairedVariable pair =
								new PairedVariable(variableAppConstantLabels.get(AppConstants.SPFLD_PLOT_COUNT.getString()), plotValue);
							settingDetail.setPairedVariable(pair);
						}
						index = SettingsUtil.addToList(details, settingDetail, index, fields, strFieldId);
						found = true;
						break;
					}
				}
			}

			if (!found) {
				// required field but has no value
				final SettingVariable variable = new SettingVariable(label, null, null, null, null, null, null, null, null, null);
				final SettingDetail settingDetail = new SettingDetail(variable, null, "", false);
				index = SettingsUtil.addToList(details, settingDetail, index, fields, strFieldId);
			}
		}

		return details;
	}

	static Map<String, String> getVariableAppConstantLabels(final List<String> labels, final Properties appConstantsProperties) {
		final Map<String, String> variableLabels = new HashMap<>();

		for (final String label : labels) {
			final String value = appConstantsProperties.getProperty(label.toUpperCase() + "_LABEL");
			variableLabels.put(label, value != null ? value : "");
		}

		return variableLabels;
	}

	private static String getSpecialFieldValue(
		final String specialFieldLabel, final Integer datasetId,
		final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, final Workbook workbook) {

		if (AppConstants.SPFLD_ENTRIES.getString().equals(specialFieldLabel)) {
			final long count = fieldbookMiddlewareService.countStocks(datasetId);
			return String.valueOf(count);
		} else if (AppConstants.SPFLD_HAS_FIELDMAP.getString().equals(specialFieldLabel)) {
			return fieldbookMiddlewareService.hasFieldMap(datasetId) ? "Yes" : "No";
		} else if (AppConstants.SPFLD_COUNT_VARIATES.getString().equals(specialFieldLabel)) {
			final List<Integer> variateIds = new ArrayList<>();
			if (workbook.getVariates() != null) {
				for (final MeasurementVariable variate : workbook.getVariates()) {
					variateIds.add(variate.getTermId());
				}
			}
			long count = 0;
			if (datasetId != null) {
				count = fieldbookMiddlewareService.countVariatesWithData(datasetId, variateIds);
			}
			return count + " of " + variateIds.size();
		} else if (AppConstants.SPFLD_PLOT_COUNT.getString().equals(specialFieldLabel)) {
			return String.valueOf(fieldbookMiddlewareService.countObservations(datasetId));
		}
		return "";
	}

	private static List<SettingDetail> convertWorkbookFactorsToSettingDetails(
		final List<MeasurementVariable> factors,
		final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {

		final List<SettingDetail> plotsLevelList = new ArrayList<>();
		if (factors == null) {
			return plotsLevelList;
		}

		for (final MeasurementVariable factor : factors) {
			final SettingVariable variable = new SettingVariable(factor.getName(), factor.getDescription(), factor.getProperty(),
				factor.getScale(), factor.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(factor.getLabel()).toString(),
				factor.getDataType());
			final Integer stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(

				HtmlUtils.htmlUnescape(variable.getProperty()), HtmlUtils.htmlUnescape(variable.getScale()),
				HtmlUtils.htmlUnescape(variable.getMethod()), PhenotypicType.valueOf(HtmlUtils.htmlUnescape(variable.getRole())));

			if (!SettingsUtil.inHideVariableFields(stdVar, AppConstants.HIDE_PLOT_FIELDS.getString())
				&& (factor.getTreatmentLabel() == null || "".equals(factor.getTreatmentLabel()))) {

				variable.setCvTermId(stdVar);
				variable.setRole(factor.getRole().name());
				final SettingDetail settingDetail = new SettingDetail(variable, null, null,
					SettingsUtil.isSettingVariableDeletable(stdVar, AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()));
				settingDetail.setRole(factor.getRole());
				plotsLevelList.add(settingDetail);
			}
		}

		return plotsLevelList;
	}

	public static void convertWorkbookVariatesToSettingDetails(
		final List<MeasurementVariable> variates,
		final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService,
		final List<SettingDetail> traits, final List<SettingDetail> selectedVariates) {

		if (variates == null) {
			return;
		}

		for (final MeasurementVariable variate : variates) {
			final SettingVariable variable = new SettingVariable(variate.getName(), variate.getDescription(), variate.getProperty(),
				variate.getScale(), variate.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(variate.getLabel()).toString(),
				variate.getDataType());
			final Integer stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(
				HtmlUtils.htmlUnescape(variable.getProperty()), HtmlUtils.htmlUnescape(variable.getScale()),
				HtmlUtils.htmlUnescape(variable.getMethod()), PhenotypicType.VARIATE);
			variable.setCvTermId(stdVar);
			final SettingDetail settingDetail = new SettingDetail(variable, null, null, true);
			if (variate.getVariableType() != null && VariableType.SELECTION_METHOD.getId().equals(variate.getVariableType().getId())) {
				selectedVariates.add(settingDetail);
			} else {
				traits.add(settingDetail);
			}
		}

	}

	private static SettingVariable getSettingVariable(
		final String name, final String description, final String property,
		final String scale, final String method, final String role, final String dataType, final Integer dataTypeId,
		final Double minRange, final Double maxRange, final UserSelection userSelection,
		final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, final String programUUID) {

		final SettingVariable variable =
			new SettingVariable(name, description, property, scale, method, role, dataType, dataTypeId, minRange, maxRange);

		final Integer stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(
			HtmlUtils.htmlUnescape(variable.getProperty()), HtmlUtils.htmlUnescape(variable.getScale()),
			HtmlUtils.htmlUnescape(variable.getMethod()), PhenotypicType.valueOf(HtmlUtils.htmlUnescape(variable.getRole())));
		variable.setCvTermId(stdVar);
		if (userSelection != null) {
			final StandardVariable standardVariable =
				SettingsUtil.getStandardVariable(variable.getCvTermId(), fieldbookMiddlewareService, programUUID);
			variable.setPSMRFromStandardVariable(standardVariable, role);
		}

		return variable;
	}

	private static int addToList(
		final List<SettingDetail> list, final SettingDetail settingDetail, final int orderIndex,
		final List<String> fields, final String idString) {
		int order = -1;
		if (fields != null) {
			order = fields.indexOf(idString);
		}
		int index = orderIndex;
		settingDetail.setOrder(order > -1 ? order : index++);
		list.add(settingDetail);

		return index;
	}

	private static List<Integer> getBreedingMethodIndices(
		final List<MeasurementRow> observations, final OntologyService ontologyService,
		final boolean isResetAll, final String programUUID) {
		SettingsUtil.LOG.info("Start BreedingMethodIndices");
		final List<Integer> indices = new ArrayList<>();
		final MeasurementRow mrow = observations.get(0);
		final Map<Integer, StandardVariable> varCache = new HashMap<>();
		StandardVariable stdVar;
		int index = 0;
		// FIXME this is OTT logic
		for (final MeasurementData data : mrow.getDataList()) {
			final boolean isVariableSample = data.getMeasurementVariable().getTermId() == TermId.SAMPLES.getId();
			if (!varCache.keySet().contains(data.getMeasurementVariable().getTermId()) && !isVariableSample) {

				// EHCached we hope
				stdVar = ontologyService.getStandardVariable(data.getMeasurementVariable().getTermId(), programUUID);
				varCache.put(data.getMeasurementVariable().getTermId(), stdVar);
			}
			stdVar = varCache.get(data.getMeasurementVariable().getTermId());
			if (stdVar != null) {
				if (stdVar.getId() != TermId.BREEDING_METHOD_VARIATE.getId()
					&& stdVar.getProperty().getId() == TermId.BREEDING_METHOD_PROP.getId() && isResetAll
					|| !isResetAll && stdVar.getId() == TermId.BREEDING_METHOD_VARIATE_CODE.getId()) {
					indices.add(index);
				}
			}
			index++;
		}
		SettingsUtil.LOG.info("End BreedingMethodIndices");
		return indices;
	}

	public static void resetBreedingMethodValueToId(
		final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService,
		final List<MeasurementRow> observations, final boolean isResetAll, final OntologyService ontologyService,
		final String programUUID) {
		if (observations == null || observations.isEmpty()) {
			return;
		}

		final List<Integer> indeces = SettingsUtil.getBreedingMethodIndices(observations, ontologyService, isResetAll, programUUID);

		if (indeces.isEmpty()) {
			return;
		}

		final List<Method> methods = fieldbookMiddlewareService.getAllBreedingMethods(false);
		final Map<String, Method> methodMap = new HashMap<>();
		// create a map to get method id based on given code
		if (methods != null) {
			for (final Method method : methods) {
				methodMap.put(method.getMcode(), method);
			}
		}

		// set value back to id
		for (final MeasurementRow row : observations) {
			for (final Integer i : indeces) {
				final Method method = methodMap.get(row.getDataList().get(i).getValue());
				row.getDataList().get(i).setValue(method == null ? row.getDataList().get(i).getValue() : String.valueOf(method.getMid()));
			}
		}

	}

	public static void resetBreedingMethodValueToCode(
		final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService,
		final List<MeasurementRow> observations, final boolean isResetAll, final OntologyService ontologyService,
		final String programUUID) {
		// set value of breeding method code in selection variates to code
		// instead of id

		SettingsUtil.LOG.info("Start ResetBreedingMethodValueToCode");

		if (observations == null || observations.isEmpty()) {
			return;
		}

		final List<Integer> indeces = SettingsUtil.getBreedingMethodIndices(observations, ontologyService, isResetAll, programUUID);

		if (indeces.isEmpty()) {
			return;
		}

		final List<Method> methods = fieldbookMiddlewareService.getAllBreedingMethods(false);
		final Map<Integer, Method> methodMap = new HashMap<>();

		if (methods != null) {
			for (final Method method : methods) {
				methodMap.put(method.getMid(), method);
			}
		}
		for (final MeasurementRow row : observations) {
			for (final Integer i : indeces) {
				Integer value = null;

				if (row.getDataList().get(i).getValue() == null || row.getDataList().get(i).getValue().isEmpty()) {
					value = null;
				} else if (NumberUtils.isNumber(row.getDataList().get(i).getValue())) {
					value = Integer.parseInt(row.getDataList().get(i).getValue());
				}

				final Method method = methodMap.get(value);
				row.getDataList().get(i).setValue(method == null ? row.getDataList().get(i).getValue() : method.getMcode());
			}
		}

		SettingsUtil.LOG.info("End ResetBreedingMethodValueToCode");

	}

	public static List<Integer> buildVariates(final List<MeasurementVariable> variates) {
		final List<Integer> variateList = new ArrayList<>();
		if (variates != null) {
			for (final MeasurementVariable var : variates) {
				variateList.add(var.getTermId());
			}
		}
		return variateList;
	}

	private static void rearrangeSettings(
		final List<SettingDetail> sourceList, final List<SettingDetail> trialList,
		final PhenotypicType type) {
		if (sourceList != null && !sourceList.isEmpty()) {
			for (final SettingDetail source : sourceList) {
				if (source.getVariable().getRole() != null
					&& type == PhenotypicType.getPhenotypicTypeByName(source.getVariable().getRole())) {
					trialList.add(source);
				}
			}
			sourceList.removeAll(trialList);
		}
	}

	private static List<TreatmentFactorDetail> convertWorkbookFactorsToTreatmentDetailFactors(final List<TreatmentVariable> factors) {
		final List<TreatmentFactorDetail> details = new ArrayList<>();
		if (factors != null && !factors.isEmpty()) {
			MeasurementVariable levelFactor;
			MeasurementVariable amountFactor;
			final ObjectMapper objectMapper = new ObjectMapper();
			for (final TreatmentVariable factor : factors) {
				try {
					levelFactor = factor.getLevelVariable();
					amountFactor = factor.getValueVariable();
					final int levels = factor.getValues() != null ? factor.getValues().size() : 0;

					final TreatmentFactorDetail detail = new TreatmentFactorDetail(levelFactor.getTermId(), amountFactor.getTermId(),
						String.valueOf(levels), amountFactor.getValue(), levelFactor.getName(), amountFactor.getName(),
						amountFactor.getDataTypeId(), objectMapper.writeValueAsString(amountFactor.getPossibleValues()),
						amountFactor.getMinRange(), amountFactor.getMaxRange());
					detail.setLevelDescription(levelFactor.getDescription());
					details.add(detail);

				} catch (final Exception e) {
					throw new MiddlewareQueryException(e.getMessage(), e);
				}
			}
		}
		return details;
	}

	/**
	 * Adds the deleted settings list.
	 *
	 * @param previousFormList    the form list
	 * @param deletedList         the deleted list
	 * @param previousSessionList the session list
	 */
	public static void addDeletedSettingsList(
		final List<SettingDetail> previousFormList, final List<SettingDetail> deletedList,
		final List<SettingDetail> previousSessionList) {
		List<SettingDetail> formList = previousFormList;
		List<SettingDetail> sessionList = previousSessionList;
		if (deletedList != null) {
			final List<SettingDetail> newDeletedList = new ArrayList<>();
			for (final SettingDetail setting : deletedList) {
				if (setting.getVariable().getOperation().equals(Operation.UPDATE)) {
					setting.getVariable().setOperation(Operation.DELETE);
					newDeletedList.add(setting);
				}
			}
			if (!newDeletedList.isEmpty()) {
				if (formList == null) {
					formList = new ArrayList<>();
				}
				formList.addAll(newDeletedList);
				if (sessionList == null) {
					sessionList = new ArrayList<>();
				}
				sessionList.addAll(newDeletedList);
			}
		}
	}

	/**
	 * Removes the basic details variables.
	 *
	 * @param studyLevelConditions the study level conditions
	 * @param variableIds          the ids of the variables to be removed from the list
	 */
	public static void removeBasicDetailsVariables(final List<SettingDetail> studyLevelConditions, final String variableIds) {
		final Iterator<SettingDetail> iter = studyLevelConditions.iterator();
		while (iter.hasNext()) {
			if (SettingsUtil.inVariableIds(iter.next().getVariable().getCvTermId(), variableIds)) {
				iter.remove();
			}
		}
	}

	/**
	 * Check if the property id is in the given variable ids
	 * In fixed study list.
	 *
	 * @param propertyId  the property id
	 * @param variableIds the ids of the variables to be removed from the list
	 * @return true if the property id is included in the variable ids
	 */
	static boolean inVariableIds(final int propertyId, final String variableIds) {
		final StringTokenizer token = new StringTokenizer(variableIds, ",");

		while (token.hasMoreTokens()) {
			if (token.nextToken().equals(String.valueOf(propertyId))) {
				return true;
			}
		}
		return false;
	}

	public static void setConstantLabels(final Dataset dataset, final List<MeasurementVariable> constants) {
		if (constants != null && !constants.isEmpty() && dataset != null && dataset.getConstants() != null
			&& !dataset.getConstants().isEmpty()) {
			for (final Constant constant : dataset.getConstants()) {
				for (final MeasurementVariable mvar : constants) {
					if (constant.getId() == mvar.getTermId()) {
						if (constant.getOperation() != Operation.ADD) {
							constant.setLabel(mvar.getLabel());
						}
						if (mvar.getPossibleValues() != null) {
							constant.setPossibleValues(mvar.getPossibleValues());
						}
						break;
					}
				}
			}
		}
	}

	public static void addTrialCondition(
		final TermId termId, final ExpDesignParameterUi param, final Workbook workbook,
		final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, final String programUUID) {

		final String value = SettingsUtil.getExperimentalDesignValue(param, termId);
		MeasurementVariable mvar = null;
		if (workbook.getTrialConditions() != null && !workbook.getTrialConditions().isEmpty()) {
			for (final MeasurementVariable var : workbook.getTrialConditions()) {
				if (var.getTermId() == termId.getId()) {
					mvar = var;
					mvar.setValue(value);
					mvar.setOperation(Operation.UPDATE);
					mvar.setLabel(PhenotypicType.TRIAL_ENVIRONMENT.getLabelList().get(0));
					break;
				}
			}
		}
		if (mvar == null) {
			final StandardVariable stdvar = fieldbookMiddlewareService.getStandardVariable(termId.getId(), programUUID);
			if (stdvar != null) {
				mvar = new MeasurementVariable(stdvar.getId(), stdvar.getName(), stdvar.getDescription(), stdvar.getScale().getName(),
					stdvar.getMethod().getName(), stdvar.getProperty().getName(), stdvar.getDataType().getName(), value,
					PhenotypicType.TRIAL_ENVIRONMENT.getLabelList().get(0),
					stdvar.getConstraints() != null ? stdvar.getConstraints().getMinValue() : null,
					stdvar.getConstraints() != null ? stdvar.getConstraints().getMaxValue() : null, PhenotypicType.TRIAL_ENVIRONMENT);
				mvar.setOperation(Operation.ADD);
				mvar.setDataTypeId(stdvar.getDataType().getId());
				workbook.getConditions().add(mvar);
				workbook.resetTrialConditions();
			}
		}
	}

	private static void removeTrialConditions(final List<Integer> ids, final Workbook workbook) {

		if (workbook.getTrialConditions() != null && !workbook.getTrialConditions().isEmpty()) {
			Iterator<MeasurementVariable> iter = workbook.getConditions().iterator();
			while (iter.hasNext()) {
				final MeasurementVariable measurementVariable = iter.next();
				if (ids.contains(measurementVariable.getTermId())) {
					iter.remove();
				}
			}
		}
	}

	private static void addOldExperimentalDesignToCurrentWorkbook(
		final Workbook workbook,
		final List<MeasurementVariable> allExpDesignVariables) {
		final List<Integer> expDesignConstants = AppConstants.EXP_DESIGN_VARIABLES.getIntegerList();
		if (allExpDesignVariables != null && !allExpDesignVariables.isEmpty()) {
			for (final MeasurementVariable condition : allExpDesignVariables) {
				if (expDesignConstants.contains(condition.getTermId())) {
					boolean found = false;
					if (workbook.getConditions() != null && !workbook.getConditions().isEmpty()) {
						for (final MeasurementVariable currentCondition : workbook.getConditions()) {
							if (currentCondition.getTermId() == condition.getTermId()) {
								found = true;
								break;
							}
						}
					}
					if (!found) {
						// for deletion
						workbook.getConditions().add(condition);
						workbook.resetTrialConditions();
					}
				}
			}
		}
	}

	private static void setExperimentalDesignToWorkbook(
		final ExpDesignParameterUi param, final List<Integer> included,
		final Workbook workbook, final List<MeasurementVariable> allExpDesignVariables,
		final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, final String programUUID) {

		SettingsUtil.addOldExperimentalDesignToCurrentWorkbook(workbook, allExpDesignVariables);
		if (param != null && included != null) {
			for (final Integer id : included) {
				final TermId termId = TermId.getById(id);
				SettingsUtil.addTrialCondition(termId, param, workbook, fieldbookMiddlewareService, programUUID);
			}

			final List<Integer> excluded = new ArrayList<>();
			if (workbook.getTrialConditions() != null && !workbook.getTrialConditions().isEmpty()) {
				for (final MeasurementVariable var : workbook.getTrialConditions()) {
					if (!included.contains(var.getTermId())
						&& AppConstants.EXP_DESIGN_VARIABLES.getIntegerList().contains(var.getTermId())) {
						excluded.add(var.getTermId());
					}
				}
			}
			SettingsUtil.removeTrialConditions(excluded, workbook);
		}
	}

	static String getExperimentalDesignValue(final ExpDesignParameterUi param, final TermId termId) {
		switch (termId) {
			case EXPERIMENT_DESIGN_FACTOR:
				if (param.getDesignType() != null) {
					return String.valueOf(ExperimentDesignType.getTermIdByDesignTypeId(param.getDesignType(), param.getUseLatenized()));
				}
				break;
			case NUMBER_OF_REPLICATES:
				return String.valueOf(param.getReplicationsCount());
			case PERCENTAGE_OF_REPLICATION:
				return String.valueOf(param.getReplicationPercentage());
			case BLOCK_SIZE:
				return String.valueOf(param.getBlockSize());
			case REPLICATIONS_MAP:
				if (param.getReplicationsArrangement() != null) {
					switch (param.getReplicationsArrangement()) {
						case 1:
							return String.valueOf(TermId.REPS_IN_SINGLE_COL.getId());
						case 2:
							return String.valueOf(TermId.REPS_IN_SINGLE_ROW.getId());
						case 3:
							return String.valueOf(TermId.REPS_IN_ADJACENT_COLS.getId());
						default:
					}
				}
				break;
			case NO_OF_REPS_IN_COLS:
				return param.getReplatinGroups();
			case NO_OF_CBLKS_LATINIZE:
				return String.valueOf(param.getNblatin());
			case NO_OF_ROWS_IN_REPS:
				return String.valueOf(param.getRowsPerReplications());
			case NO_OF_COLS_IN_REPS:
				return String.valueOf(param.getColsPerReplications());
			case NO_OF_CCOLS_LATINIZE:
				return String.valueOf(param.getNclatin());
			case NO_OF_CROWS_LATINIZE:
				return String.valueOf(param.getNrlatin());
			case EXPT_DESIGN_SOURCE:
				return param.getFileName();
			case NBLKS:
				return String.valueOf(param.getNumberOfBlocks());
			case CHECK_START:
				return String.valueOf(param.getCheckStartingPosition());
			case CHECK_INTERVAL:
				return String.valueOf(param.getCheckSpacing());
			case CHECK_PLAN:
				return String.valueOf(param.getCheckInsertionManner());
			default:
		}
		return "";
	}

	public static ExpDesignParameterUi convertToExpDesignParamsUi(final List<MeasurementVariable> expDesigns) {
		final ExpDesignParameterUi param = new ExpDesignParameterUi();
		if (expDesigns == null || expDesigns.isEmpty()) {
			return param;
		}
		for (final MeasurementVariable var : expDesigns) {
			final String value = var.getValue();
			if(!StringUtils.isEmpty(value)) {
				switch (TermId.getById(var.getTermId())) {
					case BLOCK_SIZE:
						param.setBlockSize(Integer.valueOf(value));
						break;
					case NO_OF_COLS_IN_REPS:
						param.setColsPerReplications(Integer.valueOf(value));
						break;
					case NO_OF_ROWS_IN_REPS:
						param.setRowsPerReplications(Integer.valueOf(value));
						break;
					case EXPERIMENT_DESIGN_FACTOR:
						final int designTypeTermId = Integer.parseInt(value);
						final ExperimentDesignType experimentDesignType = ExperimentDesignType.getDesignTypeItemByTermId(designTypeTermId);
						param.setDesignType(experimentDesignType != null ? experimentDesignType.getId() : null);
						param.setUseLatenized(ExperimentDesignType.isLatinized(designTypeTermId));
						break;
					case NO_OF_CBLKS_LATINIZE:
						param.setNblatin(Integer.valueOf(value));
						break;
					case NO_OF_CCOLS_LATINIZE:
						param.setNclatin(Integer.valueOf(value));
						break;
					case NO_OF_CROWS_LATINIZE:
						param.setNrlatin(Integer.valueOf(value));
						break;
					case NO_OF_REPS_IN_COLS:
						param.setReplatinGroups(value);
						break;
					case REPLICATIONS_MAP:
						if (String.valueOf(TermId.REPS_IN_SINGLE_COL.getId()).equals(value)) {
							param.setReplicationsArrangement(1);
						} else if (String.valueOf(TermId.REPS_IN_SINGLE_ROW.getId()).equals(value)) {
							param.setReplicationsArrangement(2);
						} else if (String.valueOf(TermId.REPS_IN_ADJACENT_COLS.getId()).equals(value)) {
							param.setReplicationsArrangement(3);
						}
						break;
					case NUMBER_OF_REPLICATES:
						param.setReplicationsCount(Integer.valueOf(value));
						break;
					case PERCENTAGE_OF_REPLICATION:
						param.setReplicationPercentage(Integer.valueOf(value));
						break;
					case EXPT_DESIGN_SOURCE:
						param.setFileName(value);
						break;
					case NBLKS:
						param.setNumberOfBlocks(Integer.valueOf(value));
						break;
					case CHECK_PLAN:
						param.setCheckInsertionManner(Integer.valueOf(value));
						break;
					case CHECK_INTERVAL:
						param.setCheckSpacing(Integer.valueOf(value));
						break;
					case CHECK_START:
						param.setCheckStartingPosition(Integer.valueOf(value));
						break;
				}
			}

		}

		return param;
	}

	/**
	 * Gets the setting detail value.
	 *
	 * @param details the details
	 * @param termId  the term id
	 * @return the setting detail value
	 */
	public static String getSettingDetailValue(final List<SettingDetail> details, final int termId) {
		String value = null;

		for (final SettingDetail detail : details) {
			if (detail.getVariable().getCvTermId().equals(termId)) {
				value = detail.getValue();
				break;
			}
		}

		return value;
	}

	static int getCodeInPossibleValues(final List<ValueReference> valueRefs, final String settingDetailValue) {
		for (final ValueReference valueRef : valueRefs) {
			if (valueRef.getId().equals(Integer.parseInt(settingDetailValue))) {
				return Integer.parseInt(valueRef.getName());
			}
		}
		return 0;
	}

	static int getCodeValue(final String settingDetailValue, final List<SettingDetail> removedConditions, final int termId) {
		if (removedConditions != null) {
			for (final SettingDetail detail : removedConditions) {
				if (detail.getVariable().getCvTermId().equals(termId)) {
					return SettingsUtil.getCodeInPossibleValues(detail, settingDetailValue);
				}
			}
		}
		return 0;
	}

	private static int getCodeInPossibleValues(final SettingDetail detail, final String settingDetailValue) {
		if (detail.getPossibleValues() != null && !detail.getPossibleValues().isEmpty()) {
			for (final ValueReference valueRef : detail.getPossibleValues()) {
				if (valueRef.getId().equals(Integer.parseInt(settingDetailValue))) {
					return Integer.parseInt(valueRef.getName());
				}
			}
		}
		return 0;
	}

	static boolean checkVariablesHaveValues(final List<SettingDetail> checkVariables) {
		if (checkVariables != null && !checkVariables.isEmpty()) {
			for (final SettingDetail setting : checkVariables) {
				if (setting.getValue() == null) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	static List<Integer> parseVariableIds(final String variableIds) {
		final List<Integer> variableIdList = new ArrayList<>();
		final StringTokenizer tokenizer = new StringTokenizer(variableIds, "|");
		while (tokenizer.hasMoreTokens()) {
			variableIdList.add(Integer.valueOf(tokenizer.nextToken()));
		}
		return variableIdList;
	}

	public static void addNewSettingDetails(final int mode, final List<SettingDetail> newDetails, final UserSelection userSelection) {
		SettingsUtil.setSettingDetailRoleAndVariableType(mode, newDetails, null, null);

		if (mode == VariableType.STUDY_DETAIL.getId()) {
			if (userSelection.getStudyLevelConditions() == null) {
				userSelection.setStudyLevelConditions(newDetails);
			} else {
				userSelection.getStudyLevelConditions().addAll(newDetails);
			}

		} else if (mode == VariableType.EXPERIMENTAL_DESIGN.getId()
			|| mode == VariableType.GERMPLASM_DESCRIPTOR.getId()
			|| mode == VariableType.ENTRY_DETAIL.getId()) {
			if (userSelection.getPlotsLevelList() == null) {
				userSelection.setPlotsLevelList(newDetails);
			} else {
				userSelection.getPlotsLevelList().addAll(newDetails);
			}
		} else if (mode == VariableType.TRAIT.getId()) {
			if (userSelection.getBaselineTraitsList() == null) {
				userSelection.setBaselineTraitsList(newDetails);
			} else {
				userSelection.getBaselineTraitsList().addAll(newDetails);
			}
		} else if (mode == VariableType.SELECTION_METHOD.getId()) {
			if (userSelection.getSelectionVariates() == null) {
				userSelection.setSelectionVariates(newDetails);
			} else {
				userSelection.getSelectionVariates().addAll(newDetails);
			}
		} else if (mode == VariableType.TREATMENT_FACTOR.getId()) {
			if (userSelection.getTreatmentFactors() == null) {
				userSelection.setTreatmentFactors(newDetails);
			} else {
				userSelection.getTreatmentFactors().addAll(newDetails);
			}
		} else if (mode == VariableType.ENVIRONMENT_DETAIL.getId()) {
			if (userSelection.getTrialLevelVariableList() == null) {
				userSelection.setTrialLevelVariableList(newDetails);
			} else {
				userSelection.getTrialLevelVariableList().addAll(newDetails);
			}
		} else {
			if (userSelection.getStudyConditions() == null) {
				userSelection.setStudyConditions(newDetails);
			} else {
				userSelection.getStudyConditions().addAll(newDetails);
			}
		}
	}

	public static void deleteVariableInSession(final List<SettingDetail> variableList, final int variableId) {
		final Iterator<SettingDetail> iter = variableList.iterator();
		while (iter.hasNext()) {
			if (iter.next().getVariable().getCvTermId().equals(variableId)) {
				iter.remove();
			}
		}
	}

	public static void hideVariableInSession(final List<SettingDetail> variableList, final int variableId) {
		final Iterator<SettingDetail> iter = variableList.iterator();
		while (iter.hasNext()) {
			final SettingDetail next = iter.next();
			if (next.getVariable().getCvTermId().equals(variableId)) {
				next.setHidden(true);
			}
		}

	}

	static void setSettingDetailRoleAndVariableType(
		final int mode, final List<SettingDetail> newDetails,
		final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, final String programUUID) {

		if (newDetails != null) {
			for (final SettingDetail settingDetail : newDetails) {

				if (settingDetail.getRole() != null) {
					continue;
				}

				if (settingDetail.getVariable().getCvTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
					settingDetail.setRole(PhenotypicType.TRIAL_ENVIRONMENT);
				} else if (mode == VariableType.GERMPLASM_DESCRIPTOR.getId()) {

					if (settingDetail.getVariable().getVariableTypes() == null && fieldbookMiddlewareService != null) {
						final StandardVariable standardVariable = SettingsUtil
							.getStandardVariable(settingDetail.getVariable().getCvTermId(), fieldbookMiddlewareService, programUUID);
						settingDetail.getVariable().setVariableTypes(standardVariable.getVariableTypes());
					}

					// The default Role for Germplasm Descriptor is
					// PhenotypicType.GERMPLASM
					// but if the VariableType(s) assigned to the variable is
					// only EXPERIMENTAL DESIGN then
					// set the role as PhenotypicType.TRIAL_DESIGN
					if (settingDetail.getVariable().getVariableTypes() != null
						&& !SettingsUtil.hasVariableType(
						VariableType.GERMPLASM_DESCRIPTOR,
						settingDetail.getVariable().getVariableTypes())
						&& SettingsUtil.hasVariableType(
						VariableType.EXPERIMENTAL_DESIGN,
						settingDetail.getVariable().getVariableTypes())) {
						settingDetail.setRole(VariableType.EXPERIMENTAL_DESIGN.getRole());
						settingDetail.setVariableType(VariableType.EXPERIMENTAL_DESIGN);
					} else {
						settingDetail.setRole(VariableType.GERMPLASM_DESCRIPTOR.getRole());
						settingDetail.setVariableType(VariableType.GERMPLASM_DESCRIPTOR);
					}
				} else {
					settingDetail.setRole(VariableType.getById(mode).getRole());
					settingDetail.setVariableType(VariableType.getById(mode));
				}
			}
		}
	}

	public static boolean hasVariableType(final VariableType variableType, final Set<VariableType> variableTypes) {
		for (final VariableType varType : variableTypes) {
			if (varType.equals(variableType)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static List<ValueReference> intersection(final List<ValueReference> firstList, final List<ValueReference> secondList) {
		if (firstList != null && secondList != null) {
			return ListUtils.intersection(firstList, secondList);
		}
		return ListUtils.EMPTY_LIST;
	}

}
