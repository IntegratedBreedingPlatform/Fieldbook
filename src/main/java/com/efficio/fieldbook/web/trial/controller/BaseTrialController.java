
package com.efficio.fieldbook.web.trial.controller;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.exception.FieldbookRequestException;
import com.efficio.fieldbook.web.trial.bean.BasicDetails;
import com.efficio.fieldbook.web.trial.bean.ExpDesignData;
import com.efficio.fieldbook.web.trial.bean.ExpDesignDataDetail;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.Instance;
import com.efficio.fieldbook.web.trial.bean.InstanceInfo;
import com.efficio.fieldbook.web.trial.bean.TabInfo;
import com.efficio.fieldbook.web.trial.bean.TreatmentFactorData;
import com.efficio.fieldbook.web.trial.bean.TreatmentFactorTabBean;
import com.efficio.fieldbook.web.trial.bean.TrialSettingsBean;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.api.file.FileMetadataDTO;
import org.generationcp.middleware.api.file.FileMetadataFilterRequest;
import org.generationcp.middleware.api.file.FileMetadataService;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.ExperimentalDesignVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.service.api.user.UserService;
import org.generationcp.middleware.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public abstract class BaseTrialController extends SettingsController {

	@Resource
	protected UserService userService;

	@Resource
	private FileMetadataService fileMetadataService;

	private static final Logger LOG = LoggerFactory.getLogger(BaseTrialController.class);

	static final String URL_SETTINGS = "TrialManager/templates/trialSettings";
	static final String URL_GERMPLASM = "TrialManager/templates/germplasmDetails";
	static final String URL_ENVIRONMENTS = "TrialManager/templates/environments";
	static final String URL_TREATMENT = "TrialManager/templates/treatment";
	static final String URL_EXPERIMENTAL_DESIGN = "TrialManager/templates/experimentalDesign";
	static final String URL_SUB_OBSERVATION_TAB = "TrialManager/templates/subobservations/subObservationTab";
	static final String URL_SUB_OBSERVATION_SET = "TrialManager/templates/subobservations/subObservationSet";


	void createStudyDetails(final Workbook workbook, final BasicDetails detailBean) {
		if (workbook.getStudyDetails() == null) {
			workbook.setStudyDetails(new StudyDetails());
		}

		final StudyDetails studyDetails = workbook.getStudyDetails();

		studyDetails.setId(detailBean.getStudyID());
		studyDetails.setDescription(detailBean.getDescription());
		studyDetails.setObjective(detailBean.getObjective());
		studyDetails.setStudyName(detailBean.getStudyName());
		studyDetails.setStartDate(detailBean.getStartDate());
		studyDetails.setEndDate(detailBean.getEndDate());
		studyDetails.setStudyUpdate(Util.getCurrentDateAsStringValue(Util.DATE_AS_NUMBER_FORMAT));
		studyDetails.setStudyType(this.studyDataManager.getStudyTypeByName(detailBean.getStudyType().getName()));

		if (detailBean.getCreatedBy() != null) {
			studyDetails.setCreatedBy(detailBean.getCreatedBy());
		}

		if (detailBean.getFolderId() != null) {
			studyDetails.setParentFolderId(detailBean.getFolderId());
		}

		studyDetails.print(1);
	}

	void processEnvironmentData(final InstanceInfo data) {
		for (int i = 0; i < data.getInstances().size(); i++) {
			final Map<String, String> values = data.getInstances().get(i).getManagementDetailValues();

			if (!values.containsKey(Integer.toString(TermId.LOCATION_ID.getId()))
				|| values.get(Integer.toString(TermId.LOCATION_ID.getId())) == null || values
				.get(Integer.toString(TermId.LOCATION_ID.getId())).isEmpty()) {
				throw new FieldbookRequestException("save.study.no.location.selected.on.environment");
			}

			if (!values.containsKey(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()))
				|| values.get(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId())) == null
				|| values.get(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId())).isEmpty()) {
				values.put(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()), Integer.toString(i + 1));
			}
		}
	}

	TabInfo prepareExperimentalDesignTabInfo(final Workbook trialWorkbook, final boolean isUsePrevious) {
		final TabInfo tabInfo = new TabInfo();
		final ExperimentalDesignVariable xpDesignVariable = trialWorkbook.getExperimentalDesignVariables();
		// currently, the saved experimental design information is not loaded up
		// when choosing a previous trial as template
		if (!isUsePrevious && xpDesignVariable != null) {
			final ExpDesignParameterUi data = new ExpDesignParameterUi();

			// default values
			data.setDesignType(null);
			data.setUseLatenized(false);

			data.setBlockSize(this.getExperimentalDesignData(xpDesignVariable.getBlockSize()));

			// set cols per replication
			data.setColsPerReplications(this.getExperimentalDesignData(xpDesignVariable.getNumberOfColsInReps()));
			data.setRowsPerReplications(this.getExperimentalDesignData(xpDesignVariable.getNumberOfRowsInReps()));

			data.setNclatin(this.getExperimentalDesignData(xpDesignVariable.getNumberOfContiguousColsLatinize()));
			data.setNblatin(this.getExperimentalDesignData(xpDesignVariable.getNumberOfContiguousBlocksLatinize()));
			data.setNrlatin(this.getExperimentalDesignData(xpDesignVariable.getNumberOfContiguousRowsLatinize()));

			data.setReplatinGroups(this.getExperimentalDesignDataString(xpDesignVariable.getNumberOfRepsInCols()));
			final String replicationsMap = this.getExperimentalDesignDataString(xpDesignVariable.getReplicationsMap());

			if (NumberUtils.isNumber(replicationsMap)) {
				final int repArrangementID = Integer.parseInt(replicationsMap);
				if (TermId.REPS_IN_SINGLE_COL.getId() == repArrangementID) {
					data.setReplicationsArrangement(1);
				} else if (TermId.REPS_IN_SINGLE_ROW.getId() == repArrangementID) {
					data.setReplicationsArrangement(2);
				} else if (TermId.REPS_IN_ADJACENT_COLS.getId() == repArrangementID) {
					data.setReplicationsArrangement(3);
				}
			}

			data.setReplicationsCount(this.getExperimentalDesignData(xpDesignVariable.getNumberOfReplicates()));

			data.setFileName(this.getExperimentalDesignDataString(xpDesignVariable.getExperimentalDesignSource()));

			// FIXME
			// Get starting entry and plot without loading all observations in OpenTrialController.openTrial()
			this.setStartingPlotNoFromObservations(trialWorkbook, data);

			final String designTypeString =
				xpDesignVariable.getExperimentalDesign() == null ? null : xpDesignVariable.getExperimentalDesign().getValue();
			if (NumberUtils.isNumber(designTypeString)) {
				final int designTypeTermID = Integer.parseInt(designTypeString);
				final ExperimentDesignType experimentDesignType = ExperimentDesignType.getDesignTypeItemByTermId(designTypeTermID);
				data.setDesignType(experimentDesignType != null ? experimentDesignType.getId() : designTypeTermID);
				data.setUseLatenized(ExperimentDesignType.isLatinized(designTypeTermID));
			}

			final Integer replicationPercentage = this.getExperimentalDesignData(xpDesignVariable.getReplicationPercentage());
			data.setReplicationPercentage(replicationPercentage);

			data.setNumberOfBlocks(this.getExperimentalDesignData(xpDesignVariable.getNumberOfBlocks()));
			data.setCheckInsertionManner(this.getExperimentalDesignData(xpDesignVariable.getChecksMannerOfInsertion()));
			data.setCheckSpacing(this.getExperimentalDesignData(xpDesignVariable.getChecksSpacing()));
			data.setCheckStartingPosition(this.getExperimentalDesignData(xpDesignVariable.getChecksStartingPosition()));

			tabInfo.setData(data);
		}

		return tabInfo;
	}

	private void setStartingPlotNoFromObservations(final Workbook trialWorkbook, final ExpDesignParameterUi data) {
		// Set starting entry and plot number from observations
		int startingPlotNo = 0;
		if (trialWorkbook.getObservations() != null && !trialWorkbook.getObservations().isEmpty()) {

			final List<MeasurementRow> measurementRows = trialWorkbook.getObservations();
			for (final MeasurementRow measurementRow : measurementRows) {

				final Map<Integer, MeasurementData> dataMap =
					Maps.uniqueIndex(measurementRow.getDataList(), new Function<MeasurementData, Integer>() {

						@Override
						public Integer apply(final MeasurementData from) {
							return from.getMeasurementVariable().getTermId();
						}
					});

				final int currentPlotNo = Integer.parseInt(dataMap.get(TermId.PLOT_NO.getId()).getValue());
				if (currentPlotNo < startingPlotNo || startingPlotNo == 0) {
					startingPlotNo = currentPlotNo;
				}
			}
		} else {
			// set the default starting plot no
			startingPlotNo = 1;
		}
		data.setStartingPlotNo(startingPlotNo);
	}

	private Integer getExperimentalDesignData(final MeasurementVariable var) {
		if (var != null && NumberUtils.isDigits(var.getValue())) {
			return Integer.valueOf(var.getValue());
		} else {
			return null;
		}
	}

	private String getExperimentalDesignDataString(final MeasurementVariable var) {
		if (var != null) {
			return var.getValue();
		} else {
			return null;
		}
	}

	TabInfo prepareGermplasmTabInfo(final List<MeasurementVariable> measurementVariables, final boolean isUsePrevious) {
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

			if (!isUsePrevious) {
				detail.getVariable().setOperation(Operation.UPDATE);
			} else {
				detail.getVariable().setOperation(Operation.ADD);
			}

			detailList.add(detail);
		}

		final TabInfo info = new TabInfo();
		info.setSettings(detailList);

		this.userSelection.setPlotsLevelList(detailList);

		return info;
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

	TabInfo prepareEntryDetailsData(final List<MeasurementVariable> entryDetails) {
		final List<Integer> requiredIDList = this.buildVariableIDList(AppConstants.CREATE_STUDY_ENTRY_DETAILS_REQUIRED_FIELDS.getString());

		final List<SettingDetail> settings = entryDetails.stream()
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

		final TabInfo tabInfo = new TabInfo();
		tabInfo.setSettings(settings);
		this.userSelection.setEntryDetails(settings);
		return tabInfo;
	}

	protected TabInfo prepareTreatmentFactorsInfo(final List<TreatmentVariable> treatmentVariables, final boolean isUsePrevious) {
		final Map<Integer, SettingDetail> levelDetails = new HashMap<>();
		final Map<String, TreatmentFactorData> currentData = new HashMap<>();
		final Map<String, List<SettingDetail>> treatmentFactorPairs = new HashMap<>();

		for (final TreatmentVariable treatmentVariable : treatmentVariables) {
			final int levelFactorID = treatmentVariable.getLevelVariable().getTermId();
			if (!levelDetails.containsKey(levelFactorID)) {
				final SettingDetail detail = this.createSettingDetail(levelFactorID, treatmentVariable.getLevelVariable().getName(),
					VariableType.TREATMENT_FACTOR.getRole().name());

				if (!isUsePrevious) {
					detail.getVariable().setOperation(Operation.UPDATE);
				} else {
					detail.getVariable().setOperation(Operation.ADD);
				}

				levelDetails.put(levelFactorID, detail);
			}

			final TreatmentFactorData treatmentFactorData;
			if (!currentData.containsKey(Integer.toString(levelFactorID))) {
				treatmentFactorData = new TreatmentFactorData();
				treatmentFactorData.setVariableId(treatmentVariable.getValueVariable().getTermId());
				currentData.put(Integer.toString(levelFactorID), treatmentFactorData);
			} else {
				treatmentFactorData = currentData.get(Integer.toString(levelFactorID));
			}
			treatmentFactorData.setLabels(treatmentVariable.getValues());
			treatmentFactorData.setLevels(treatmentVariable.getValues().size());
			treatmentFactorPairs.put(Integer.toString(levelFactorID), this.retrieveVariablePairs(levelFactorID));

		}

		final TabInfo info = new TabInfo();
		final TreatmentFactorTabBean tabBean = new TreatmentFactorTabBean();
		tabBean.setCurrentData(currentData);
		info.setData(tabBean);

		final List<SettingDetail> detailList = new ArrayList<>(levelDetails.values());
		final Map<String, Object> treatmentFactorSettings = new HashMap<>();
		treatmentFactorSettings.put("details", detailList);
		treatmentFactorSettings.put("treatmentLevelPairs", treatmentFactorPairs);

		this.userSelection.setTreatmentFactors(detailList);
		info.setSettingMap(treatmentFactorSettings);

		return info;
	}

	TabInfo prepareMeasurementVariableTabInfo(
		final List<MeasurementVariable> variatesList, final VariableType variableType,
		final boolean isUsePrevious) {

		final List<SettingDetail> detailList = new ArrayList<>();

		for (final MeasurementVariable var : variatesList) {
			if (var.getVariableType() == variableType) {

				final SettingDetail detail = this.createSettingDetailWithVariableType(var.getTermId(), var.getName(), variableType);

				if (!isUsePrevious) {
					detail.getVariable().setOperation(Operation.UPDATE);
				} else {
					detail.getVariable().setOperation(Operation.ADD);
				}

				detail.setDeletable(true);

				detailList.add(detail);
			}
		}

		if (variableType == VariableType.TRAIT) {
			this.userSelection.setBaselineTraitsList(detailList);
		} else if (variableType == VariableType.SELECTION_METHOD) {
			this.userSelection.setSelectionVariates(detailList);
		}

		final TabInfo info = new TabInfo();
		info.setSettings(detailList);

		return info;
	}

	TabInfo prepareEnvironmentsTabInfo(final Workbook workbook, final boolean isUsePrevious) {
		final TabInfo info = new TabInfo();
		final Map<String, Object> settingMap = new HashMap<>();
		final List<SettingDetail> managementDetailList = new ArrayList<>();
		final List<SettingDetail> trialConditionsList = new ArrayList<>();
		final List<Integer> hiddenFields = this.buildVariableIDList(
			AppConstants.HIDE_STUDY_ENVIRONMENT_FIELDS.getString() + "," + AppConstants.HIDE_STUDY_VARIABLE_DBCV_FIELDS.getString());
		final List<Integer> requiredFields = this.buildVariableIDList(AppConstants.CREATE_STUDY_ENVIRONMENT_REQUIRED_FIELDS.getString());
		final List<Integer> filterFields = this.buildVariableIDList(AppConstants.EXP_DESIGN_VARIABLES.getString());
		final Map<String, MeasurementVariable> factorsMap = SettingsUtil.buildMeasurementVariableMap(workbook.getTrialConditions());
		final Map<String, String> pairedVariable = AppConstants.ID_NAME_COMBINATION.getMapOfValues();
		for (final MeasurementVariable measurementVariable : workbook.getTrialConditions()) {

			// Exclude NameId variable to front end to resolve IBP-3139 for environment tab
			if(!pairedVariable.containsValue(String.valueOf(measurementVariable.getTermId()))){

				final SettingDetail detail =
						this.createSettingDetail(measurementVariable.getTermId(), measurementVariable.getName(), VariableType.ENVIRONMENT_DETAIL.getRole().name());

				if (filterFields.contains(measurementVariable.getTermId())) {
					continue;
				}

				if (hiddenFields.contains(measurementVariable.getTermId())) {
					detail.setHidden(true);
				}

				if (!requiredFields.contains(measurementVariable.getTermId())) {
					detail.setDeletable(true);
				}

				managementDetailList.add(detail);

				if (!isUsePrevious) {
					detail.getVariable().setOperation(Operation.UPDATE);
				} else {
					detail.getVariable().setOperation(Operation.ADD);
				}

				// set local name of id variable to local name of name variable
				final String nameTermId = SettingsUtil.getNameCounterpart(measurementVariable.getTermId(), AppConstants.ID_NAME_COMBINATION.getString());
				if (factorsMap.get(nameTermId) != null) {
					detail.getVariable().setName(factorsMap.get(nameTermId).getName());
				}
			}
		}

		for (final MeasurementVariable var : workbook.getTrialConstants()) {
			final SettingDetail detail =
				this.createSettingDetail(var.getTermId(), var.getName(), VariableType.ENVIRONMENT_CONDITION.getRole().name());

			if (!isUsePrevious) {
				detail.getVariable().setOperation(Operation.UPDATE);
			} else {
				detail.getVariable().setOperation(Operation.ADD);
			}

			detail.setDeletable(true);
			trialConditionsList.add(detail);
		}

		settingMap.put("managementDetails", managementDetailList);
		settingMap.put("trialConditionDetails", trialConditionsList);

		info.setSettingMap(settingMap);

		final InstanceInfo data = new InstanceInfo();
		final List<MeasurementRow> trialObservations = workbook.getTrialObservations();

		data.setNumberOfInstances(trialObservations.size());

		final FileMetadataFilterRequest filterRequest = new FileMetadataFilterRequest();
		filterRequest.setInstanceIds(trialObservations.stream().map(row -> (int)row.getLocationId()).collect(Collectors.toList()));
		final List<FileMetadataDTO> files = this.fileMetadataService
			.search(filterRequest, this.contextUtil.getCurrentProgramUUID(), null);
		final Map<Integer, List<FileMetadataDTO>> filesMap = new HashMap<>();

		files.forEach(file -> filesMap.computeIfAbsent(file.getInstanceId(), k -> new ArrayList<>()).add(file));
		final List<Instance> instances = new ArrayList<>();
		for (final MeasurementRow row : trialObservations) {
			final Instance instance = new Instance();
			if (!isUsePrevious) {
				instance.setInstanceId(row.getLocationId());
				instance.setStockId(row.getStockId());
				instance.setExperimentId(row.getExperimentId());
				final int instanceId = (int)instance.getInstanceId();
				if (filesMap.containsKey(instanceId)) {
					instance.setFileCount(filesMap.get(instanceId).size());
					final Set<String> fileVariableIdsList = filesMap.get(instanceId).stream()
						.filter(f -> CollectionUtils.isNotEmpty(f.getVariables())).flatMap(f -> f.getVariables().stream())
						.map(v -> String.valueOf(v.getId())).collect(Collectors.toSet());
					if(CollectionUtils.isNotEmpty(fileVariableIdsList)) {
						instance.setFileVariableIds(fileVariableIdsList.toArray(new String[fileVariableIdsList.size()]));
					}
				}
			}

			final Map<String, String> managementDetailValues = new HashMap<>();
			final Map<String, String> trialConditionValues = new HashMap<>();
			final Map<String, Integer> trialConditionDataIdMap = new HashMap<>();
			final Map<String, Integer> managementDetailDataIdMap = new HashMap<>();
			for (final SettingDetail detail : managementDetailList) {

				final MeasurementData mData = row.getMeasurementData(detail.getVariable().getCvTermId());
				if (mData != null) {
					final String value;
					if (mData.getcValueId() != null) {
						value = mData.getcValueId();
					} else {
						value = mData.getValue();
					}
					managementDetailDataIdMap.put(Integer.toString(mData.getMeasurementVariable().getTermId()), mData.getMeasurementDataId());
					managementDetailValues.put(Integer.toString(mData.getMeasurementVariable().getTermId()), value);
				}
			}
			for (final SettingDetail detail : trialConditionsList) {

				final MeasurementData mData = row.getMeasurementData(detail.getVariable().getCvTermId());
				if (mData != null) {
					final String value = mData.getValue();
					if (!isUsePrevious) {
						trialConditionDataIdMap.put(Integer.toString(mData.getMeasurementVariable().getTermId()), mData.getMeasurementDataId());
					}
					trialConditionValues.put(Integer.toString(mData.getMeasurementVariable().getTermId()), value);
				}
			}

			instance.setManagementDetailValues(managementDetailValues);
			instance.setTrialDetailValues(trialConditionValues);
			instance.setTrialConditionDataIdMap(trialConditionDataIdMap);
			instance.setManagementDetailDataIdMap(managementDetailDataIdMap);
			instances.add(instance);
		}

		// minimum number of environments is 1
		if (data.getNumberOfInstances() == 0) {
			data.setNumberOfInstances(1);
			if (isUsePrevious) {
				instances.add(this.createEnvironmentWithDefaultLocation(this.getProgramDefaultLocationId()));
			}
		}

		data.setInstances(instances);
		info.setData(data);

		this.userSelection.setTrialLevelVariableList(managementDetailList);

		this.userSelection.setStudyConditions(trialConditionsList);

		return info;
	}

	Instance createEnvironmentWithDefaultLocation(final Integer defaultLocationId) {
		final Instance defaultInstance = new Instance();
		final Map<String, String> managementDetails = new HashMap<>();
		managementDetails.put(String.valueOf(TermId.LOCATION_ID.getId()), String.valueOf(defaultLocationId));
		defaultInstance.setManagementDetailValues(managementDetails);
		return defaultInstance;
	}

	public List<SettingDetail> retrieveVariablePairs(final int cvTermId) {
		final List<SettingDetail> output = new ArrayList<>();

		try {

			final StandardVariable variable = this.ontologyService.getStandardVariable(cvTermId, this.contextUtil.getCurrentProgramUUID());

			final List<StandardVariable> pairs = this.fieldbookMiddlewareService.getPossibleTreatmentPairs(variable.getId(),
				variable.getProperty().getId(), AppConstants.CREATE_STUDY_REMOVE_TREATMENT_FACTOR_IDS.getIntegerList());

			for (final StandardVariable item : pairs) {
				output.add(this.createSettingDetail(item.getId(), null, VariableType.TREATMENT_FACTOR.getRole().name()));
			}

		} catch (final MiddlewareException e) {
			BaseTrialController.LOG.error(e.getMessage(), e);
		}

		return output;
	}

	TabInfo prepareBasicDetailsTabInfo(final StudyDetails studyDetails, final boolean isUsePrevious, final int trialID)
		throws ParseException {
		final Map<String, String> basicDetails = new HashMap<>();
		final List<SettingDetail> initialDetailList = new ArrayList<>();

		final List<Integer> initialSettingIDs = this.buildVariableIDList(AppConstants.CREATE_STUDY_REQUIRED_FIELDS.getString());

		for (final Integer initialSettingID : initialSettingIDs) {
			try {
				basicDetails.put(initialSettingID.toString(), "");
				final SettingDetail detail = this.createSettingDetail(initialSettingID, null, VariableType.STUDY_DETAIL.getRole().name());

				if (!isUsePrevious) {
					detail.getVariable().setOperation(Operation.UPDATE);
				} else {
					detail.getVariable().setOperation(Operation.ADD);
				}

				initialDetailList.add(detail);
			} catch (final MiddlewareQueryException e) {
				BaseTrialController.LOG.error(e.getMessage(), e);
			}
		}

		final BasicDetails basic = new BasicDetails();

		basic.setBasicDetails(basicDetails);
		basic.setStudyName(studyDetails.getStudyName());
		basic.setStudyID(trialID);
		basic.setDescription(studyDetails.getDescription());
		basic.setStartDate(Util.convertDate(studyDetails.getStartDate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT));
		basic.setEndDate(studyDetails.getEndDate() != null && !studyDetails.getEndDate().isEmpty()
			? Util.convertDate(studyDetails.getEndDate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT) : StringUtils.EMPTY);
		basic.setStudyUpdate(studyDetails.getStudyUpdate() != null && !studyDetails.getStudyUpdate().isEmpty()
			? Util.convertDate(studyDetails.getStudyUpdate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT)
			: StringUtils.EMPTY);
		basic.setObjective(studyDetails.getObjective());
		basic.setisLocked(studyDetails.getIsLocked());

		final int folderId = (int) studyDetails.getParentFolderId();
		final String folderName;

		if (DmsProject.SYSTEM_FOLDER_ID.equals(folderId)) {
			folderName = AppConstants.STUDIES.getString();
		} else {
			folderName = this.fieldbookMiddlewareService.getFolderNameById(folderId);
		}

		basic.setFolderId(folderId);
		basic.setFolderName(folderName);
		basic.setFolderNameLabel(folderName);

		String studyOwnerPersonName = StringUtils.EMPTY;
		Integer studyOwnerUserId = null;
		final String createdBy = studyDetails.getCreatedBy();
		if (!StringUtils.isEmpty(createdBy)) {
			studyOwnerUserId = Integer.valueOf(createdBy);
			studyOwnerPersonName = this.userService.getPersonNameForUserId(Integer.valueOf(createdBy));
		}
		basic.setUserID(studyOwnerUserId);
		basic.setUserName(studyOwnerPersonName);
		basic.setStudyType(studyDetails.getStudyType());

		final TabInfo tab = new TabInfo();
		tab.setData(basic);

		this.setUserSelectionBasicInformation(studyDetails, initialDetailList);
		return tab;
	}

	private void setUserSelectionBasicInformation(final StudyDetails studyDetails, final List<SettingDetail> initialDetailList) {
		this.userSelection.setBasicDetails(initialDetailList);
		this.userSelection.setStudyName(studyDetails.getStudyName());
		this.userSelection.setStudyDescription(studyDetails.getDescription());
		this.userSelection.setStudyStartDate(studyDetails.getStartDate());
		this.userSelection.setStudyEndDate(studyDetails.getEndDate());
		this.userSelection.setStudyUpdate(studyDetails.getStudyUpdate());
		this.userSelection.setStudyObjective(studyDetails.getObjective());
		this.userSelection.setStudyType(studyDetails.getStudyType().getName());
	}

	private String convertDateStringForUI(final String value) {
		if (value != null && !value.contains("-")) {
			return DateUtil.convertToUIDateFormat(TermId.DATE_VARIABLE.getId(), value);
		} else {
			return value;
		}

	}

	TabInfo prepareTrialSettingsTabInfo(final List<MeasurementVariable> measurementVariables, final boolean isUsePrevious) {
		final TabInfo info = new TabInfo();
		final Map<String, String> trialValues = new HashMap<>();
		final List<SettingDetail> details = new ArrayList<>();

		final List<Integer> hiddenFields = this.buildVariableIDList(AppConstants.HIDE_STUDY_VARIABLE_DBCV_FIELDS.getString());
		final List<Integer> basicDetailIDList = this.buildVariableIDList(AppConstants.HIDE_STUDY_FIELDS.getString());
		final Map<String, MeasurementVariable> settingsMap = SettingsUtil.buildMeasurementVariableMap(measurementVariables);
		for (final MeasurementVariable var : measurementVariables) {
			if (!basicDetailIDList.contains(var.getTermId())) {
				final SettingDetail detail =
					this.createSettingDetail(var.getTermId(), var.getName(), VariableType.STUDY_DETAIL.getRole().name());
				detail.setDeletable(true);
				details.add(detail);

				if (hiddenFields.contains(var.getTermId())) {
					detail.setHidden(true);
				} else {
					detail.setHidden(false);
				}

				if (!isUsePrevious) {
					detail.getVariable().setOperation(Operation.UPDATE);
				} else {
					detail.getVariable().setOperation(Operation.ADD);
				}

				// set local name of id variable to local name of name variable
				final String nameTermId = SettingsUtil.getNameCounterpart(var.getTermId(), AppConstants.ID_NAME_COMBINATION.getString());
				if (settingsMap.get(nameTermId) != null) {
					detail.getVariable().setName(settingsMap.get(nameTermId).getName());
				}

				final String value;
				if ("DATE".equals(detail.getVariable().getWidgetType().getType())) {
					value = this.convertDateStringForUI(var.getValue());
				} else {
					value = var.getValue();
				}
				trialValues.put(Integer.toString(var.getTermId()), value);
			}
		}

		this.userSelection.setStudyLevelConditions(details);

		info.setSettings(details);
		final TrialSettingsBean trialSettingsBean = new TrialSettingsBean();
		trialSettingsBean.setUserInput(trialValues);
		info.setData(trialSettingsBean);
		return info;
	}

	TabInfo prepareExperimentalDesignSpecialData() {
		final TabInfo info = new TabInfo();
		final ExpDesignData data = new ExpDesignData();
		final List<ExpDesignDataDetail> detailList = new ArrayList<>();

		final List<Integer> ids = this.buildVariableIDList(AppConstants.CREATE_STUDY_EXP_DESIGN_DEFAULT_FIELDS.getString());
		for (final Integer id : ids) {
			// PLOT, REP, BLOCK, ENTRY NO
			final StandardVariable stdvar =
				this.fieldbookMiddlewareService.getStandardVariable(id, this.contextUtil.getCurrentProgramUUID());
			final SettingVariable svar = new SettingVariable();
			svar.setCvTermId(id);
			svar.setName(stdvar.getName());
			final ExpDesignDataDetail dataDetail =
				new ExpDesignDataDetail(AppConstants.getString(id + AppConstants.LABEL.getString()), svar);
			detailList.add(dataDetail);

		}
		data.setExpDesignDetailList(detailList);
		info.setData(data);

		return info;
	}

	void addMeasurementVariablesToTrialObservationIfNecessary(
		final List<Instance> instances, final Workbook workbook,
		final List<MeasurementRow> trialObservations) {

		if (trialObservations == null) {
			return;
		}

		int x = 0;
		for (final MeasurementRow row : trialObservations) {

			final Map<String, String> trialDetailValues = instances.get(x).getTrialDetailValues();
			final Map<String, String> managementDetailValues = instances.get(x).getManagementDetailValues();

			for (final MeasurementVariable measurementVariable : workbook.getTrialVariables()) {
				final MeasurementData data = row.getMeasurementData(measurementVariable.getTermId());
				if (data == null) {

					String val = "";
					final String trialDetailValue = trialDetailValues.get(String.valueOf(measurementVariable.getTermId()));
					final String managementDetailValue = managementDetailValues.get(String.valueOf(measurementVariable.getTermId()));

					if (trialDetailValue != null) {
						val = trialDetailValue;
					} else if (managementDetailValue != null) {
						val = managementDetailValue;
					}

					final MeasurementData newData = new MeasurementData(measurementVariable.getName(), val, false,
						measurementVariable.getDataType(), measurementVariable);
					row.getDataList().add(newData);
				}

			}

			x++;
		}

	}

	void getTraitsAndSelectionVariates(
		final List<MeasurementVariable> measurementDatasetVariables,
		final List<MeasurementVariable> newMeasurementDatasetVariables, final String listCsv) {

		if (listCsv != null && !"".equalsIgnoreCase(listCsv)) {
			// Create a map of traits and selection variates from user selection (UI) so their aliases can be retrieved
			final Map<Integer, SettingDetail> traitsMap = this.createMapOfTraitsAndSelectionVariatesFromUserSelection();
			final StringTokenizer token = new StringTokenizer(listCsv, ",");
			while (token.hasMoreTokens()) {
				final int id = Integer.parseInt(token.nextToken());
				final MeasurementVariable currentVar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, id);
				if (currentVar == null) {
					final StandardVariable var =
						this.fieldbookMiddlewareService.getStandardVariable(id, this.contextUtil.getCurrentProgramUUID());
					var.setPhenotypicType(PhenotypicType.VARIATE);
					final MeasurementVariable newVar =
						ExpDesignUtil.convertStandardVariableToMeasurementVariable(var, Operation.ADD, this.fieldbookService);
					newVar.setFactor(false);
					// Get trait's alias, if any, from user selection (UI) so that it will reflect on Measurements tab
					final SettingDetail traitFromSession = traitsMap.get(id);
					if (traitFromSession != null && traitFromSession.getVariable() != null) {
						newVar.setName(traitFromSession.getVariable().getName());
					}
					newMeasurementDatasetVariables.add(newVar);
				} else {
					newMeasurementDatasetVariables.add(currentVar);
				}
			}
		}
	}

	private ImmutableMap<Integer, SettingDetail> createMapOfTraitsAndSelectionVariatesFromUserSelection() {
		final Set<SettingDetail> variates = new HashSet<>();
		if (CollectionUtils.isNotEmpty(this.userSelection.getBaselineTraitsList())) {
			variates.addAll(this.userSelection.getBaselineTraitsList());
		}
		if (CollectionUtils.isNotEmpty(this.userSelection.getSelectionVariates())) {
			variates.addAll(this.userSelection.getSelectionVariates());
		}
		return Maps.uniqueIndex(variates, new Function<SettingDetail, Integer>() {

			@Override
			public Integer apply(final SettingDetail setting) {
				return setting.getVariable().getCvTermId();
			}
		});
	}

	protected void setStudyDataManager(final StudyDataManager studyDataManager) {
		this.studyDataManager = studyDataManager;
	}
}
