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

package com.efficio.fieldbook.utils.test;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.operation.builder.WorkbookBuilder;
import org.generationcp.middleware.pojos.Location;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The Class SettingsUtil.
 */
public class WorkbookDataUtil {

	public static final String FILE_NAME = "sample_file";
	public static final int NUMBER_OF_OBSERVATIONS = 12000;

	private static final String NURSERY_NAME = "Nursery_";
	private static final String TRIAL_NAME = "Trial_";

	// STUDY DETAILS
	private static final String TITLE = "Nursery Workbook";
	private static final String OBJECTIVE = "To evaluate the Population 114";
	private static final String START_DATE = "20130805";
	private static final String END_DATE = "20130805";
	private static final int FOLDER_ID = 1;

	// PROPERTIES
	private static final String PERSON = "PERSON";
	public static final String TRIAL_INSTANCE = "TRIAL_INSTANCE";
	private static final String LOCATION = "LOCATION";
	private static final String GERMPLASM_ENTRY = "GERMPLASM ENTRY";
	private static final String GERMPLASM_ID = "GERMPLASM ID";
	private static final String CROSS_HISTORY = "CROSS HISTORY";
	private static final String SEED_SOURCE = "SEED SOURCE";
	private static final String FIELD_PLOT = "FIELD PLOT";
	private static final String REPLICATION = "REPLICATION";
	private static final String REPLICATION_FACTOR = "REPLICATION FACTOR";
	private static final String BLOCKING_FACTOR = "BLOCKING FACTOR";
	private static final String PLANTING_DATE = "Planting Date";
	private static final String SOIL_ACIDITY = "Soil Acidity";
	private static final String YIELD = "Yield";
	private static final String PLANTS_SELECTED = "Plants Selected";

	// SCALES
	private static final String DBCV = "DBCV";
	private static final String DBID = "DBID";
	public static final String NUMBER = "NUMBER";
	private static final String PEDIGREE_STRING = "PEDIGREE STRING";
	private static final String NAME = "NAME";
	private static final String NESTED_NUMBER = "NESTED NUMBER";
	private static final String DATE = "Date (yyyymmdd)";
	private static final String KG_HA = "kg/ha";
	private static final String PH = "ph";
	private static final String TEXT = "TEXT";

	// METHODS
	private static final String ASSIGNED = "ASSIGNED";
	public static final String ENUMERATED = "ENUMERATED";
	private static final String CONDUCTED = "CONDUCTED";
	private static final String APPLIED = "APPLIED";
	private static final String SELECTED = "SELECTED";
	private static final String PH_METER = "PH Meter";
	private static final String DRY_AND_WEIGH = "Dry and Weigh";
	private static final String COUNTED = "Counted";

	// LABELS
	private static final String STUDY = "STUDY";
	public static final String TRIAL = "TRIAL";
	public static final String ENTRY = "ENTRY";
	public static final String PLOT = "PLOT";

	// DATA TYPES
	private static final String CHAR = "C";
	public static final String NUMERIC = "N";

	// FACTORS
	public static final String GID = "GID";
	public static final String DESIG = "DESIG";
	private static final String CROSS = "CROSS";
	private static final String SOURCE = "SOURCE";
	private static final String BLOCK = "BLOCK";
	private static final String REP = "REP";

	// CONSTANTS
	private static final int DATE_SEEDED_ID = 20810;
	private static final int SOILPH_ID = 22531;

	// VARIATES
	private static final String GYLD = "GYLD";
	private static final String CHALK_PCT = "CHALK_PCT";
	private static final int GYLD_ID = 18000;
	public static final int CHALK_PCT_ID = 22768;

	// CONDITIONS
	private static final String PI_NAME = "PI Name";
	private static final String PI_ID = "PI Id";
	private static final String COOPERATOR = "Cooperator";
	private static final String COOPERATOR_ID = "Cooperator Id";
	private static final String SITE = "Site";
	public static final String SITE_ID = "Site Id";

	private static final String GERMPLASM_NAME = "TIANDOUGOU-9";
	private static final String NUMERIC_VALUE = "1";

	public static final Integer LOCATION_ID_1 = 1;
	public static final Integer LOCATION_ID_2 = 2;
	private static final Integer LTYPE = 1;
	private static final Integer NLLP = 1;
	public static final String LNAME = "Location";
	private static final String LABBR = "LOC2";
	private static final Integer SNL3ID = 1;
	private static final Integer SNL2ID = 1;
	private static final Integer SNL1ID = 1;
	private static final Integer CNTRYID = 1;
	private static final Integer LRPLCE = 1;

	private static final String NUMERIC_VARIABLE = "NUMERIC VARIABLE";
	private static final String TEST_METHOD = "TEST METHOD";
	private static final String TEST_SCALE = "TEST SCALE";
	private static final String TEST_PROPERTY = "TEST PROPERTY";
	private static final String TEST_DESCRIPTION = "TEST DESCRIPTION";
	private static final String CREATED_BY = "1" ;

	public static Workbook getTestWorkbook(final int noOfObservations, final StudyTypeDto studyType) {
		return WorkbookDataUtil.createTestWorkbook(noOfObservations, studyType);
	}

	public static Workbook getTestWorkbookForStudy(final int noOfObservations, final int noOfInstance) {
		return WorkbookDataUtil.createTestWorkbook(noOfObservations, StudyTypeDto.getTrialDto(), noOfInstance);
	}

	private static Workbook createTestWorkbook(final int noOfObservations, final StudyTypeDto studyType) {
		return WorkbookDataUtil.createTestWorkbook(noOfObservations, studyType, 2);
	}

	private static Workbook createTestWorkbook(final int noOfObservations, final StudyTypeDto studyType, final int noOfInstance) {
		final Workbook workbook = new Workbook();

		workbook.setStudyDetails(WorkbookDataUtil.createStudyDetails(studyType));
		workbook.setConditions(WorkbookDataUtil.createConditions());
		workbook.setFactors(WorkbookDataUtil.createFactors());
		workbook.setConstants(WorkbookDataUtil.createConstants());
		workbook.setVariates(WorkbookDataUtil.createVariates());
		workbook.setObservations(WorkbookDataUtil.createObservations(noOfObservations, noOfInstance,
				workbook));
		workbook.setTrialObservations(WorkbookDataUtil.createStudyObservations(noOfInstance, workbook));
		workbook.setMeasurementDatesetId(2);
		workbook.setTrialDatasetId(3);
		return workbook;
	}

	private static StudyDetails createStudyDetails(final StudyTypeDto studyType) {
		final StudyDetails details = new StudyDetails();
		details.setStudyName((studyType.getName().equalsIgnoreCase("N") ? WorkbookDataUtil.NURSERY_NAME : WorkbookDataUtil.TRIAL_NAME)
				+ new Random().nextInt(10000));
		details.setDescription(WorkbookDataUtil.TITLE);
		details.setObjective(WorkbookDataUtil.OBJECTIVE);
		details.setStartDate(WorkbookDataUtil.START_DATE);
		details.setEndDate(WorkbookDataUtil.END_DATE);
		details.setParentFolderId(WorkbookDataUtil.FOLDER_ID);
		details.setStudyType(studyType);
		details.setCreatedBy(WorkbookDataUtil.CREATED_BY);
		details.setId(-1);

		return details;
	}

	private static List<MeasurementVariable> createConditions() {
		// Create measurement variables and set its dataTypeId
		final List<MeasurementVariable> conditions = new ArrayList<>();

		MeasurementVariable variable =
				new MeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL_INSTANCE", "TRIAL NUMBER", WorkbookDataUtil.NUMBER,
						WorkbookDataUtil.ENUMERATED, WorkbookDataUtil.TRIAL_INSTANCE, WorkbookDataUtil.NUMERIC, "", WorkbookDataUtil.TRIAL);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		conditions.add(variable);

		variable =
				new MeasurementVariable(TermId.PI_NAME.getId(), "PI Name", "Name of Principal Investigator", WorkbookDataUtil.DBCV,
						WorkbookDataUtil.ASSIGNED, WorkbookDataUtil.PERSON, WorkbookDataUtil.CHAR, "", WorkbookDataUtil.STUDY);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		conditions.add(variable);

		variable =
				new MeasurementVariable(TermId.PI_ID.getId(), "PI ID", "ID of Principal Investigator", WorkbookDataUtil.DBID,
						WorkbookDataUtil.ASSIGNED, WorkbookDataUtil.PERSON, WorkbookDataUtil.NUMERIC, WorkbookDataUtil.NUMERIC_VALUE,
						WorkbookDataUtil.STUDY);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		conditions.add(variable);

		variable =
				new MeasurementVariable(AppConstants.COOPERATOR_NAME.getInt(), "COOPERATOR", "COOPERATOR NAME", WorkbookDataUtil.DBCV,
						WorkbookDataUtil.CONDUCTED, WorkbookDataUtil.PERSON, WorkbookDataUtil.CHAR, "John Smith", WorkbookDataUtil.TRIAL);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		conditions.add(variable);

		variable =
				new MeasurementVariable(AppConstants.COOPERATOR_ID.getInt(), "COOPERATOR ID", "COOPERATOR ID", WorkbookDataUtil.DBID,
						WorkbookDataUtil.CONDUCTED, WorkbookDataUtil.PERSON, WorkbookDataUtil.NUMERIC, WorkbookDataUtil.NUMERIC_VALUE,
						WorkbookDataUtil.TRIAL);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		conditions.add(variable);

		variable =
				new MeasurementVariable(TermId.TRIAL_LOCATION.getId(), "SITE", "TRIAL SITE NAME", WorkbookDataUtil.DBCV,
						WorkbookDataUtil.ASSIGNED, WorkbookDataUtil.LOCATION, WorkbookDataUtil.CHAR, "", WorkbookDataUtil.TRIAL);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		conditions.add(variable);

		variable =
				new MeasurementVariable(TermId.LOCATION_ID.getId(), "SITE ID", "TRIAL SITE ID", WorkbookDataUtil.DBID,
						WorkbookDataUtil.ASSIGNED, WorkbookDataUtil.LOCATION, WorkbookDataUtil.NUMERIC, WorkbookDataUtil.NUMERIC_VALUE,
						WorkbookDataUtil.TRIAL);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		conditions.add(variable);

		variable =
				new MeasurementVariable(TermId.NUMBER_OF_REPLICATES.getId(), "NREP", "Number of replications in an experiment", "Number",
						"Assigned", "ED - nrep", "Numeric", "2", null);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		conditions.add(variable);

		variable =
				new MeasurementVariable(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), "EXPT_DESIGN", "Experimental design - assigned (type)",
						"Type of EXPT_DESIGN", "Assigned", "Experimental design", "Categorical",
						Integer.toString(TermId.RESOLVABLE_INCOMPLETE_BLOCK.getId()), null);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		conditions.add(variable);

		variable =
				new MeasurementVariable(TermId.EXPT_DESIGN_SOURCE.getId(), "EXPT_DESIGN_SOURCE", "Source of the experimental design.",
						"Text", "Assigned", "Experimental design", WorkbookDataUtil.CHAR, "E30-Rep2-Block6-5Ind.csv", null);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		conditions.add(variable);

		return conditions;
	}

	public static void addCheckConditions(final Workbook workbook) {
		MeasurementVariable variable =
				new MeasurementVariable(TermId.CHECK_START.getId(), "CHECK_START", "CHECK_START", WorkbookDataUtil.DBID,
						WorkbookDataUtil.ASSIGNED, WorkbookDataUtil.LOCATION, WorkbookDataUtil.NUMERIC, WorkbookDataUtil.NUMERIC_VALUE,
						WorkbookDataUtil.TRIAL);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		workbook.getConditions().add(variable);

		variable =
				new MeasurementVariable(TermId.CHECK_INTERVAL.getId(), "CHECK_INTERVAL", "CHECK_INTERVAL", WorkbookDataUtil.DBID,
						WorkbookDataUtil.ASSIGNED, WorkbookDataUtil.LOCATION, WorkbookDataUtil.NUMERIC, WorkbookDataUtil.NUMERIC_VALUE,
						WorkbookDataUtil.TRIAL);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		workbook.getConditions().add(variable);

		variable =
				new MeasurementVariable(TermId.CHECK_PLAN.getId(), "CHECK_PLAN", "CHECK_PLAN", WorkbookDataUtil.DBID,
						WorkbookDataUtil.ASSIGNED, WorkbookDataUtil.LOCATION, WorkbookDataUtil.NUMERIC, WorkbookDataUtil.NUMERIC_VALUE,
						WorkbookDataUtil.TRIAL);
		variable.setDataTypeId(TermId.CATEGORICAL_VARIABLE.getId());
		workbook.getConditions().add(variable);
	}

	public static List<MeasurementVariable> createFactors() {
		// Create measurement variables and set its dataTypeId
		final List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();

		// Entry Factors
		MeasurementVariable variable =
				new MeasurementVariable(TermId.ENTRY_NO.getId(), WorkbookDataUtil.ENTRY, "The germplasm entry number",
						WorkbookDataUtil.NUMBER, WorkbookDataUtil.ENUMERATED, WorkbookDataUtil.GERMPLASM_ENTRY, WorkbookDataUtil.NUMERIC,
						WorkbookDataUtil.STUDY, WorkbookDataUtil.ENTRY);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		variable.setFactor(true);
		factors.add(variable);

		variable =
				new MeasurementVariable(TermId.GID.getId(), WorkbookDataUtil.GID, "The GID of the germplasm", WorkbookDataUtil.DBID,
						WorkbookDataUtil.ASSIGNED, WorkbookDataUtil.GERMPLASM_ID, WorkbookDataUtil.NUMERIC, WorkbookDataUtil.NUMERIC_VALUE,
						WorkbookDataUtil.ENTRY);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		variable.setFactor(true);
		factors.add(variable);

		variable =
				new MeasurementVariable(TermId.DESIG.getId(), WorkbookDataUtil.DESIG, "The name of the germplasm", WorkbookDataUtil.DBCV,
						WorkbookDataUtil.ASSIGNED, WorkbookDataUtil.GERMPLASM_ID, WorkbookDataUtil.CHAR, WorkbookDataUtil.STUDY,
						WorkbookDataUtil.ENTRY);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		variable.setFactor(true);
		factors.add(variable);

		variable =
				new MeasurementVariable(TermId.CROSS.getId(), WorkbookDataUtil.CROSS, "The pedigree string of the germplasm",
						WorkbookDataUtil.PEDIGREE_STRING, WorkbookDataUtil.ASSIGNED, WorkbookDataUtil.CROSS_HISTORY, WorkbookDataUtil.CHAR,
						WorkbookDataUtil.STUDY, WorkbookDataUtil.ENTRY);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		variable.setFactor(true);
		factors.add(variable);

		variable =
				new MeasurementVariable(TermId.SEED_SOURCE.getId(), WorkbookDataUtil.SEED_SOURCE, "The seed source of the germplasm",
						WorkbookDataUtil.NAME, WorkbookDataUtil.SELECTED, WorkbookDataUtil.SEED_SOURCE, WorkbookDataUtil.CHAR,
						WorkbookDataUtil.STUDY, WorkbookDataUtil.ENTRY);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		factors.add(variable);

		variable =
				new MeasurementVariable(TermId.PLOT_NO.getId(), WorkbookDataUtil.PLOT, "Plot number ", WorkbookDataUtil.NESTED_NUMBER,
						WorkbookDataUtil.ENUMERATED, WorkbookDataUtil.FIELD_PLOT, WorkbookDataUtil.NUMERIC, WorkbookDataUtil.NUMERIC_VALUE,
						WorkbookDataUtil.PLOT);
		variable.setFactor(true);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		factors.add(variable);

		variable =
				new MeasurementVariable(TermId.OBS_UNIT_ID.getId(), WorkbookDataUtil.PLOT, "Field Observation Unit id - assigned (text)", WorkbookDataUtil.TEXT,
						WorkbookDataUtil.ASSIGNED, WorkbookDataUtil.FIELD_PLOT, WorkbookDataUtil.CHAR, "",
						WorkbookDataUtil.ENTRY);
		variable.setFactor(true);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		factors.add(variable);

		// Plot Factors
		variable =
				new MeasurementVariable(TermId.BLOCK_NO.getId(), WorkbookDataUtil.BLOCK, "INCOMPLETE BLOCK", WorkbookDataUtil.NUMBER,
						WorkbookDataUtil.ENUMERATED, WorkbookDataUtil.BLOCKING_FACTOR, WorkbookDataUtil.NUMERIC,
						WorkbookDataUtil.NUMERIC_VALUE, WorkbookDataUtil.PLOT);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		factors.add(variable);

		variable =
				new MeasurementVariable(TermId.REP_NO.getId(), WorkbookDataUtil.REP, WorkbookDataUtil.REPLICATION, WorkbookDataUtil.NUMBER,
						WorkbookDataUtil.ENUMERATED, WorkbookDataUtil.REPLICATION_FACTOR, WorkbookDataUtil.NUMERIC,
						WorkbookDataUtil.NUMERIC_VALUE, WorkbookDataUtil.PLOT);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		factors.add(variable);

		return factors;
	}

	private static List<MeasurementVariable> createConstants() {
		// Create measurement variables and set its dataTypeId
		final List<MeasurementVariable> constants = new ArrayList<MeasurementVariable>();

		MeasurementVariable variable =
				new MeasurementVariable(WorkbookDataUtil.DATE_SEEDED_ID, "DATE_SEEDED", "Date Seeded", WorkbookDataUtil.DATE,
						WorkbookDataUtil.APPLIED, WorkbookDataUtil.PLANTING_DATE, WorkbookDataUtil.NUMERIC, WorkbookDataUtil.NUMERIC_VALUE,
						WorkbookDataUtil.STUDY);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		constants.add(variable);

		variable =
				new MeasurementVariable(WorkbookDataUtil.SOILPH_ID, "SOILPH", "Soil pH", WorkbookDataUtil.PH, WorkbookDataUtil.PH_METER,
						WorkbookDataUtil.SOIL_ACIDITY, WorkbookDataUtil.NUMERIC, WorkbookDataUtil.NUMERIC_VALUE, WorkbookDataUtil.STUDY);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		constants.add(variable);

		return constants;
	}

	private static List<MeasurementVariable> createVariates() {
		// Create measurement variables and set its dataTypeId
		final List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();

		MeasurementVariable variable =
				new MeasurementVariable(WorkbookDataUtil.GYLD_ID, WorkbookDataUtil.GYLD, "Grain yield -dry and weigh (kg/ha)",
						WorkbookDataUtil.KG_HA, WorkbookDataUtil.DRY_AND_WEIGH, WorkbookDataUtil.YIELD, WorkbookDataUtil.NUMERIC,
						WorkbookDataUtil.NUMERIC_VALUE, WorkbookDataUtil.STUDY);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		variates.add(variable);

		variable =
				new MeasurementVariable(WorkbookDataUtil.CHALK_PCT_ID, WorkbookDataUtil.CHALK_PCT, "NUMBER OF PLANTS SELECTED",
						WorkbookDataUtil.NUMBER, WorkbookDataUtil.COUNTED, WorkbookDataUtil.PLANTS_SELECTED, WorkbookDataUtil.NUMERIC,
						WorkbookDataUtil.NUMERIC_VALUE, WorkbookDataUtil.STUDY);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		variates.add(variable);
		return variates;
	}

	public static List<MeasurementRow> createObservations(final int noOfObservations, final int trialInstances, final Workbook workbook) {
		final List<MeasurementRow> observations = new ArrayList<MeasurementRow>();

		MeasurementRow row;
		List<MeasurementData> dataList;
		final Random random = new Random();
		final DecimalFormat fmt = new DecimalFormat("#.##");

		// Create n number of observation rows
		for (int j = 0; j < trialInstances; j++) {
			for (int i = 0; i < noOfObservations; i++) {
				row = new MeasurementRow();
				dataList = new ArrayList<MeasurementData>();

				MeasurementData data = new MeasurementData(WorkbookDataUtil.ENTRY, String.valueOf(i));
				data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(TermId.ENTRY_NO.getId(), workbook.getFactors()));
				dataList.add(data);

				data = new MeasurementData(WorkbookDataUtil.GID, WorkbookDataUtil.computeGID(i));
				data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(TermId.GID.getId(), workbook.getFactors()));
				dataList.add(data);

				data = new MeasurementData(WorkbookDataUtil.DESIG, WorkbookDataUtil.GERMPLASM_NAME + new Random().nextInt(10000));
				data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(TermId.DESIG.getId(), workbook.getFactors()));
				dataList.add(data);

				data = new MeasurementData(WorkbookDataUtil.CROSS, "-");
				data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(TermId.CROSS.getId(), workbook.getFactors()));
				dataList.add(data);

				data = new MeasurementData(WorkbookDataUtil.SOURCE, "-");
				data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(TermId.SEED_SOURCE.getId(), workbook.getFactors()));
				dataList.add(data);

				data = new MeasurementData(WorkbookDataUtil.PLOT, String.valueOf(i));
				data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(TermId.PLOT_NO.getId(), workbook.getFactors()));
				dataList.add(data);

				data = new MeasurementData(WorkbookDataUtil.BLOCK, "");
				data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(TermId.BLOCK_NO.getId(), workbook.getFactors()));
				dataList.add(data);

				data = new MeasurementData(WorkbookDataUtil.REP, "");
				data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(TermId.REP_NO.getId(), workbook.getFactors()));
				dataList.add(data);

				data = new MeasurementData(WorkbookDataUtil.GYLD, WorkbookDataUtil.randomizeValue(random, fmt, 5000));
				data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(WorkbookDataUtil.GYLD_ID, workbook.getVariates()));
				dataList.add(data);

				data = new MeasurementData(WorkbookDataUtil.CHALK_PCT, String.valueOf(i));
				data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(WorkbookDataUtil.CHALK_PCT_ID, workbook.getVariates()));
				dataList.add(data);

				data = new MeasurementData(WorkbookDataUtil.TRIAL_INSTANCE, String.valueOf(j + 1));
				data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(),
						workbook.getConditions()));
				dataList.add(data);

				row.setDataList(dataList);
				observations.add(row);
			}
		}

		return observations;
	}

	public static List<MeasurementRow> createNewObservations(final int noOfObservations) {
		final List<MeasurementRow> observations = new ArrayList<MeasurementRow>();

		MeasurementRow row;
		List<MeasurementData> dataList;
		final Random random = new Random();
		final DecimalFormat fmt = new DecimalFormat("#.##");

		// Create n number of observation rows
		for (int i = 0; i < noOfObservations; i++) {
			row = new MeasurementRow();
			dataList = new ArrayList<MeasurementData>();

			MeasurementData data = new MeasurementData(WorkbookDataUtil.ENTRY, String.valueOf(i));
			dataList.add(data);

			data = new MeasurementData(WorkbookDataUtil.GID, WorkbookDataUtil.computeGID(i));
			dataList.add(data);

			data = new MeasurementData(WorkbookDataUtil.DESIG, WorkbookDataUtil.GERMPLASM_NAME + new Random().nextInt(10000));
			dataList.add(data);

			data = new MeasurementData(WorkbookDataUtil.CROSS, "-");
			dataList.add(data);

			data = new MeasurementData(WorkbookDataUtil.SOURCE, "-");
			dataList.add(data);

			data = new MeasurementData(WorkbookDataUtil.PLOT, String.valueOf(i));
			dataList.add(data);

			data = new MeasurementData(WorkbookDataUtil.BLOCK, "");
			dataList.add(data);

			data = new MeasurementData(WorkbookDataUtil.REP, "");
			dataList.add(data);

			data = new MeasurementData(WorkbookDataUtil.GYLD, WorkbookDataUtil.randomizeValue(random, fmt, 5000));
			dataList.add(data);

			data = new MeasurementData(WorkbookDataUtil.CHALK_PCT, String.valueOf(i));
			dataList.add(data);

			row.setDataList(dataList);
			observations.add(row);
		}

		return observations;
	}

	public static List<MeasurementRow> createStudyObservations(final int noOfTrialInstances, final Workbook workbook) {
		final List<MeasurementRow> studyObservations = new ArrayList<MeasurementRow>();

		MeasurementRow row;
		List<MeasurementData> dataList;

		for (int i = 0; i < noOfTrialInstances; i++) {
			row = new MeasurementRow();
			dataList = new ArrayList<>();

			MeasurementData data = new MeasurementData(WorkbookDataUtil.TRIAL_INSTANCE, String.valueOf(i + 1));
			data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(),
					workbook.getConditions()));
			dataList.add(data);
			data = new MeasurementData(WorkbookDataUtil.PI_NAME, "");
			data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(TermId.PI_NAME.getId(), workbook.getConditions()));
			dataList.add(data);
			data = new MeasurementData(WorkbookDataUtil.PI_ID, "");
			data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(TermId.PI_ID.getId(), workbook.getConditions()));
			dataList.add(data);
			data = new MeasurementData(WorkbookDataUtil.COOPERATOR, "");
			data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(AppConstants.COOPERATOR_NAME.getInt(),
					workbook.getConditions()));
			dataList.add(data);
			data = new MeasurementData(WorkbookDataUtil.COOPERATOR_ID, "");
			data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(AppConstants.COOPERATOR_ID.getInt(),
					workbook.getConditions()));
			dataList.add(data);
			data = new MeasurementData(WorkbookDataUtil.SITE, WorkbookDataUtil.LNAME + "_" + (i + 1));
			data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(TermId.TRIAL_LOCATION.getId(), workbook.getConditions()));
			dataList.add(data);
			data = new MeasurementData(WorkbookDataUtil.SITE_ID, String.valueOf(i + 1));
			data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(TermId.LOCATION_ID.getId(), workbook.getConditions()));
			dataList.add(data);

			// Check variables
			data = new MeasurementData("CHECK_START", String.valueOf(i + 1));
			data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(TermId.CHECK_START.getId(), workbook.getConditions()));
			dataList.add(data);
			data = new MeasurementData("CHECK_INTERVAL", String.valueOf(i + 1));
			data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(TermId.CHECK_PLAN.getId(), workbook.getConditions()));
			dataList.add(data);
			data = new MeasurementData("CHECK_PLAN", "1");
			data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(TermId.CHECK_INTERVAL.getId(), workbook.getConditions()));
			dataList.add(data);

			row.setDataList(dataList);
			studyObservations.add(row);
		}

		return studyObservations;
	}

	public static MeasurementVariable getMeasurementVariable(final int termId, final List<MeasurementVariable> variables) {
		if (variables != null) {
			// get matching MeasurementVariable object given the term id
			for (final MeasurementVariable var : variables) {
				if (var.getTermId() == termId) {
					return var;
				}
			}
		}
		return null;
	}

	private static String computeGID(final int i) {
		int gid = 1000000;
		gid += i;
		return String.valueOf(gid);
	}

	private static String randomizeValue(final Random random, final DecimalFormat fmt, final int base) {
		final double value = random.nextDouble() * base;
		return fmt.format(value);
	}

	public static List<Location> createLocationData() {
		final List<Location> locations = new ArrayList<Location>();
		locations.add(new Location(WorkbookDataUtil.LOCATION_ID_1, WorkbookDataUtil.LTYPE, WorkbookDataUtil.NLLP, WorkbookDataUtil.LNAME
				+ " 1", WorkbookDataUtil.LABBR, WorkbookDataUtil.SNL3ID, WorkbookDataUtil.SNL2ID, WorkbookDataUtil.SNL1ID,
				new Location(WorkbookDataUtil.CNTRYID), WorkbookDataUtil.LRPLCE));
		locations.add(new Location(WorkbookDataUtil.LOCATION_ID_2, WorkbookDataUtil.LTYPE, WorkbookDataUtil.NLLP, WorkbookDataUtil.LNAME
				+ " 2", WorkbookDataUtil.LABBR, WorkbookDataUtil.SNL3ID, WorkbookDataUtil.SNL2ID, WorkbookDataUtil.SNL1ID,
				new Location(WorkbookDataUtil.CNTRYID), WorkbookDataUtil.LRPLCE));
		return locations;
	}

	public static MeasurementRow createStudyObservationWithoutSite() {
		final Workbook workbook = WorkbookDataUtil.createTestWorkbook(2, StudyTypeDto.getTrialDto());

		WorkbookDataUtil.createStudyDetails(StudyTypeDto.getTrialDto());
		WorkbookDataUtil.createConditions();

		final MeasurementRow row = new MeasurementRow();
		final List<MeasurementData> dataList = new ArrayList<MeasurementData>();

		MeasurementData data = new MeasurementData(WorkbookDataUtil.TRIAL_INSTANCE, WorkbookDataUtil.NUMERIC_VALUE);
		data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), workbook.getConditions()));
		dataList.add(data);
		data = new MeasurementData(WorkbookDataUtil.PI_NAME, "");
		data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(TermId.PI_NAME.getId(), workbook.getConditions()));
		dataList.add(data);
		data = new MeasurementData(WorkbookDataUtil.PI_ID, "");
		data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(TermId.PI_ID.getId(), workbook.getConditions()));
		dataList.add(data);
		data = new MeasurementData(WorkbookDataUtil.COOPERATOR, "");
		data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(AppConstants.COOPERATOR_NAME.getInt(), workbook.getConditions()));
		dataList.add(data);
		data = new MeasurementData(WorkbookDataUtil.COOPERATOR_ID, "");
		data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(AppConstants.COOPERATOR_ID.getInt(), workbook.getConditions()));
		dataList.add(data);

		row.setDataList(dataList);
		return row;
	}

	public static List<Integer> getTrialInstances(final Workbook workbook) {
		final List<Integer> instances = new ArrayList<Integer>();
		for (final MeasurementRow row : workbook.getTrialObservations()) {
			if (row.getDataList() != null) {
				instances.add(WorkbookDataUtil.getTrialInstanceNo(row.getDataList()));
			}
		}
		return instances;
	}

	private static int getTrialInstanceNo(final List<MeasurementData> dataList) {
		for (final MeasurementData data : dataList) {
			if (data.getMeasurementVariable().getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
				return Integer.valueOf(data.getValue());
			}
		}
		return 0;
	}

	public static void addOrUpdateExperimentalDesignVariables(final Workbook workbook, final String exptDesignFactorValue,
			final String exptDesignSourceValue, final String nRepValue, final String rMapValue, final String pRepValue) {
		if (workbook.getExperimentalDesignVariables() == null) {
			workbook.setExperimentalDesignVariables(new ArrayList<MeasurementVariable>());
		}
		for (final Integer termId : WorkbookBuilder.EXPERIMENTAL_DESIGN_VARIABLES) {
			String termValue = null;
			if (termId == TermId.EXPERIMENT_DESIGN_FACTOR.getId()) {
				termValue = exptDesignFactorValue;
			} else if (termId == TermId.EXPT_DESIGN_SOURCE.getId()) {
				if (exptDesignSourceValue != null) {
					termValue = exptDesignSourceValue;
				}
			} else if (termId == TermId.NUMBER_OF_REPLICATES.getId()) {
				if (nRepValue != null) {
					termValue = nRepValue;
				}
			} else if (termId == TermId.REPLICATIONS_MAP.getId()) {
				if (rMapValue != null) {
					termValue = rMapValue;
				}
			} else if (termId == TermId.PERCENTAGE_OF_REPLICATION.getId()) {
				if (rMapValue != null) {
					termValue = pRepValue;
				}
			} else {
				termValue = "3";
			}
			final MeasurementVariable variable = WorkbookDataUtil.createMeasurementVariableWithIdAndData(termId, termValue);
			WorkbookDataUtil.addOrUpdateVariable(variable, workbook.getConditions());
			WorkbookDataUtil.addOrUpdateVariable(variable, workbook.getExperimentalDesignVariables().getVariables());
		}
	}

	private static void addOrUpdateVariable(final MeasurementVariable variable, final List<MeasurementVariable> list) {
		final int indexOfVariable = list.indexOf(variable);
		if (indexOfVariable >= 0) {
			list.get(indexOfVariable).setValue(variable.getValue());
		} else {
			list.add(variable);
		}
	}

	public static MeasurementVariable createMeasurementVariableWithIdAndData(final int termId, final String value) {
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(termId);
		measurementVariable.setValue(value);
		return measurementVariable;
	}

	public static List<SettingDetail> getPlotLevelList() {
		final List<SettingDetail> plotLevelList = new ArrayList<>();

		for (final Map.Entry<TermId, Boolean> entry : WorkbookDataUtil.getVisibleColumnMap().entrySet()) {
			plotLevelList.add(WorkbookDataUtil.generateSettingDetail(entry.getKey()));
		}

		return plotLevelList;

	}

	public static Map<TermId, Boolean> getVisibleColumnMap() {
		final Map<TermId, Boolean> visibleColumnMap = new LinkedHashMap<>();

		visibleColumnMap.put(TermId.GID, true);
		visibleColumnMap.put(TermId.DESIG, true);
		visibleColumnMap.put(TermId.CROSS, true);
		visibleColumnMap.put(TermId.ENTRY_NO, true);
		visibleColumnMap.put(TermId.DESIG, true);
		visibleColumnMap.put(TermId.ENTRY_CODE, true);
		visibleColumnMap.put(TermId.SEED_SOURCE, true);

		return visibleColumnMap;

	}

	public static SettingDetail generateSettingDetail(final TermId termId) {
		final SettingDetail settingDetail = new SettingDetail();
		settingDetail.setHidden(false);
		final SettingVariable var = new SettingVariable();
		var.setCvTermId(termId.getId());
		settingDetail.setVariable(var);

		final StandardVariable stdVar = WorkbookDataUtil.createStandardVariable(termId.getId(), termId.name());
		settingDetail.getVariable().setName(stdVar.getName());
		settingDetail.getVariable().setDescription(stdVar.getDescription());
		settingDetail.setPossibleValues(new ArrayList<ValueReference>());

		return settingDetail;
	}

	public static StandardVariable createStandardVariable(final int id, final String name) {
		final StandardVariable stdVar = new StandardVariable();
		stdVar.setId(id);
		stdVar.setName(name);
		stdVar.setDescription(WorkbookDataUtil.TEST_DESCRIPTION);

		final Term prop = new Term();
		prop.setName(WorkbookDataUtil.TEST_PROPERTY);
		stdVar.setProperty(prop);

		final Term scale = new Term();
		scale.setName(WorkbookDataUtil.TEST_SCALE);
		stdVar.setScale(scale);

		final Term method = new Term();
		method.setName(WorkbookDataUtil.TEST_METHOD);
		stdVar.setMethod(method);

		final Term dataType = new Term();
		dataType.setName(WorkbookDataUtil.NUMERIC_VARIABLE);
		stdVar.setDataType(dataType);

		return stdVar;
	}


}
