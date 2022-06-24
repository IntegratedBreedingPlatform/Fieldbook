
package com.efficio.fieldbook.web.util;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.StudyDetails;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.data.initializer.SettingDetailTestDataInitializer;
import com.efficio.fieldbook.web.trial.TestDataHelper;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.TreatmentFactorData;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.middleware.data.initializer.StandardVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.workbench.settings.Condition;
import org.generationcp.middleware.pojos.workbench.settings.Constant;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.pojos.workbench.settings.Factor;
import org.generationcp.middleware.pojos.workbench.settings.TreatmentFactor;
import org.generationcp.middleware.pojos.workbench.settings.Variate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class SettingsUtilTest {

	public static final String NFERT_NO = "NFERT_NO";
	public static final String NFERT_KG = "NFERT_KG";
	public static final int NFERT_NO_ID = 1001;
	public static final int NFERT_KG_ID = 1002;
	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;
	
	@Mock
	private FieldbookService fieldbookService;

	@Mock
	private Properties properties;
	
	@Mock
	private UserSelection userSelection;

	private static final String PROGRAM_UUID = "123456789";

	private static final int DUMMY_ID = 0;

	private static final Term CATEGORICAL_DATATYPE_TERM = new Term(TermId.CATEGORICAL_VARIABLE.getId(),
			"Categorical Variable", "Categorical Variable");
	private static final Term C_DATATYPE_TERM = new Term(TermId.CHARACTER_VARIABLE.getId(), "C", "C");

	private static final String C_DATATYPE = "C";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testConvertXmlDatasetToWorkbookAndBack() {
		final Dataset dataset = new Dataset();

		dataset.setConditions(new ArrayList<Condition>());
		dataset.setFactors(new ArrayList<Factor>());
		dataset.setVariates(new ArrayList<Variate>());

		dataset.getConditions().add(new Condition("CONDITION1", "CONDITION1", "PERSON", "DBCV", "ASSIGNED",
				PhenotypicType.STUDY.toString(), "C", "Meeh", null, null, null));
		dataset.getFactors().add(new Factor("FACTOR1", "FACTOR1", "GERMPLASM ENTRY", "NUMBER", "ENUMERATED",
				PhenotypicType.ENTRY_DETAIL.toString(), "N", 0));
		final Variate variate = new Variate("VARIATE1", "VARIATE1", "YIELD (GRAIN)", "Kg/ha", "Paddy Rice",
				PhenotypicType.VARIATE.toString(), "N", TermId.NUMERIC_VARIABLE.getId(),
				new ArrayList<ValueReference>(), 0.0, 0.0);
		variate.setVariableType(VariableType.TRAIT.getName());
		dataset.getVariates().add(variate);

		final Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, SettingsUtilTest.PROGRAM_UUID);

		final Dataset newDataset = (Dataset) SettingsUtil.convertWorkbookToXmlDataset(workbook);
		Assert.assertEquals(dataset.getConditions().get(0).getName(), newDataset.getConditions().get(0).getName());
		Assert.assertEquals(dataset.getConditions().get(0).getDescription(),
				newDataset.getConditions().get(0).getDescription());
		Assert.assertEquals(dataset.getConditions().get(0).getProperty(),
				newDataset.getConditions().get(0).getProperty());
		Assert.assertEquals(dataset.getConditions().get(0).getScale(), newDataset.getConditions().get(0).getScale());
		Assert.assertEquals(dataset.getConditions().get(0).getMethod(), newDataset.getConditions().get(0).getMethod());
		Assert.assertEquals(dataset.getConditions().get(0).getRole(), newDataset.getConditions().get(0).getRole());
		Assert.assertEquals(dataset.getConditions().get(0).getDatatype(),
				newDataset.getConditions().get(0).getDatatype());

		Assert.assertEquals(dataset.getFactors().get(0).getName(), newDataset.getFactors().get(0).getName());
		Assert.assertEquals(dataset.getFactors().get(0).getDescription(),
				newDataset.getFactors().get(0).getDescription());
		Assert.assertEquals(dataset.getFactors().get(0).getProperty(), newDataset.getFactors().get(0).getProperty());
		Assert.assertEquals(dataset.getFactors().get(0).getScale(), newDataset.getFactors().get(0).getScale());
		Assert.assertEquals(dataset.getFactors().get(0).getMethod(), newDataset.getFactors().get(0).getMethod());
		Assert.assertEquals(dataset.getFactors().get(0).getRole(), newDataset.getFactors().get(0).getRole());
		Assert.assertEquals(dataset.getFactors().get(0).getDatatype(), newDataset.getFactors().get(0).getDatatype());

		Assert.assertEquals(dataset.getVariates().get(0).getName(), newDataset.getVariates().get(0).getName());
		Assert.assertEquals(dataset.getVariates().get(0).getDescription(),
				newDataset.getVariates().get(0).getDescription());
		Assert.assertEquals(dataset.getVariates().get(0).getProperty(), newDataset.getVariates().get(0).getProperty());
		Assert.assertEquals(dataset.getVariates().get(0).getScale(), newDataset.getVariates().get(0).getScale());
		Assert.assertEquals(dataset.getVariates().get(0).getMethod(), newDataset.getVariates().get(0).getMethod());
		Assert.assertEquals(dataset.getVariates().get(0).getRole(), newDataset.getVariates().get(0).getRole());
		Assert.assertEquals(dataset.getVariates().get(0).getDatatype(), newDataset.getVariates().get(0).getDatatype());

	}

	@Test
	public void testIfCheckVariablesAreInFixedNurseryList() {
		final String variableIds = AppConstants.FIXED_STUDY_VARIABLES.getString()
				+ AppConstants.CHECK_VARIABLES.getString()
				+ AppConstants.BREEDING_METHOD_ID_CODE_NAME_COMBINATION.getString();
		Assert.assertTrue(SettingsUtil.inVariableIds(TermId.CHECK_START.getId(), variableIds));
		Assert.assertTrue(SettingsUtil.inVariableIds(TermId.CHECK_INTERVAL.getId(), variableIds));
		Assert.assertTrue(SettingsUtil.inVariableIds(TermId.CHECK_PLAN.getId(), variableIds));
	}

	@Test
	public void testGetCodeValueValid() {
		final List<SettingDetail> removedConditions = this.createCheckVariables(true);
		final int code = SettingsUtil.getCodeValue("8414", removedConditions, TermId.CHECK_PLAN.getId());
		Assert.assertEquals("Expected 1 but got " + code + " instead.", 1, code);
	}

	@Test
	public void testGetCodeValueWhenConditionsIsNull() {
		final List<SettingDetail> removedConditions = null;
		final int code = SettingsUtil.getCodeValue("8414", removedConditions, TermId.CHECK_PLAN.getId());
		Assert.assertEquals("Expected 0 but got " + code + " instead.", 0, code);
	}

	@Test
	public void testGetCodeValueWhenPossibleValuesIsNull() {
		final List<SettingDetail> removedConditions = this.createCheckVariables(true);
		final int code = SettingsUtil.getCodeValue("8411", removedConditions, TermId.CHECK_START.getId());
		Assert.assertEquals("Expected 0 but got " + code + " instead.", 0, code);
	}

	@Test
	public void testGetCodeValueWhenPossibleValuesIsNotNullButEmpty() {
		final List<SettingDetail> removedConditions = this.createCheckVariables(true);
		final int code = SettingsUtil.getCodeValue("8412", removedConditions, TermId.CHECK_INTERVAL.getId());
		Assert.assertEquals("Expected 0 but got " + code + " instead.", 0, code);
	}

	@Test
	public void testGetVariableAppConstantLabels() throws Exception {
		final List<String> labels = new ArrayList<>(Arrays.asList(new String[] { "abc", "def" }));

		final Properties appConfigProp = Mockito.mock(Properties.class);
		Mockito.when(appConfigProp.getProperty(ArgumentMatchers.any(String.class))).thenReturn("any value");
		final ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

		SettingsUtil.getVariableAppConstantLabels(labels, appConfigProp);

		Mockito.verify(appConfigProp, Mockito.times(labels.size())).getProperty(argument.capture());
		Assert.assertTrue(argument.getValue().contains("LABEL"));
	}

	@Test
	public void testGetCodeValueInvalid() {
		final List<SettingDetail> removedConditions = this.createCheckVariables(true);
		final int code = SettingsUtil.getCodeValue("8413", removedConditions, TermId.CHECK_PLAN.getId());
		Assert.assertNotSame("Expected 1 but got " + code + " instead.", 1, code);
	}

	@Test
	public void testIfCheckVariablesHaveValues() {
		final List<SettingDetail> checkVariables = this.createCheckVariables(true);
		final boolean checksHaveValues = SettingsUtil.checkVariablesHaveValues(checkVariables);
		Assert.assertTrue(checksHaveValues);
	}

	@Test
	public void testIfCheckVariablesHaveNoValues() {
		final List<SettingDetail> checkVariables = this.createCheckVariables(false);
		final boolean checksHaveValues = SettingsUtil.checkVariablesHaveValues(checkVariables);
		Assert.assertFalse(checksHaveValues);
	}

	@Test
	public void testIfCheckVariablesIsNull() {
		final List<SettingDetail> checkVariables = null;
		final boolean checksHaveValues = SettingsUtil.checkVariablesHaveValues(checkVariables);
		Assert.assertFalse(checksHaveValues);
	}

	@Test
	public void testIfCheckVariablesIsEmpty() {
		final List<SettingDetail> checkVariables = new ArrayList<SettingDetail>();
		final boolean checksHaveValues = SettingsUtil.checkVariablesHaveValues(checkVariables);
		Assert.assertFalse(checksHaveValues);
	}

	@Test
	public void testParseVariableIds() {
		final List<Integer> variableIds = this.settingsUtilParseVariableIds("1|2|3");
		Assert.assertEquals("Should have 3 variable ids", 3, variableIds.size());
		Assert.assertEquals("1st Id should be 1", new Integer(1), variableIds.get(0));
		Assert.assertEquals("2nd Id should be 2", new Integer(2), variableIds.get(1));
		Assert.assertEquals("3rd Id should be 3", new Integer(3), variableIds.get(2));
	}

	@Test
	public void testConvertToExpDesignParamsUi_NullValues() {

		final List<MeasurementVariable> expDesigns = new ArrayList<>();

		expDesigns.add(this.createMeasurementVariable(TermId.BLOCK_SIZE.getId(), null));
		expDesigns.add(this.createMeasurementVariable(TermId.NO_OF_COLS_IN_REPS.getId(), null));
		expDesigns.add(this.createMeasurementVariable(TermId.NO_OF_ROWS_IN_REPS.getId(), null));
		expDesigns.add(this.createMeasurementVariable(TermId.NO_OF_CBLKS_LATINIZE.getId(), null));
		expDesigns.add(this.createMeasurementVariable(TermId.NO_OF_CCOLS_LATINIZE.getId(), null));
		expDesigns.add(this.createMeasurementVariable(TermId.NO_OF_CROWS_LATINIZE.getId(), null));
		expDesigns.add(this.createMeasurementVariable(TermId.NO_OF_REPS_IN_COLS.getId(), null));
		expDesigns.add(this.createMeasurementVariable(TermId.NUMBER_OF_REPLICATES.getId(), null));
		expDesigns.add(this.createMeasurementVariable(TermId.EXPT_DESIGN_SOURCE.getId(), null));
		expDesigns.add(this.createMeasurementVariable(TermId.PERCENTAGE_OF_REPLICATION.getId(), null));
		expDesigns.add(this.createMeasurementVariable(TermId.NBLKS.getId(), null));
		expDesigns.add(this.createMeasurementVariable(TermId.CHECK_PLAN.getId(), null));
		expDesigns.add(this.createMeasurementVariable(TermId.CHECK_INTERVAL.getId(), null));
		expDesigns.add(this.createMeasurementVariable(TermId.CHECK_START.getId(), null));

		final ExpDesignParameterUi result = SettingsUtil.convertToExpDesignParamsUi(expDesigns);

		Assert.assertNull(result.getBlockSize());
		Assert.assertNull(result.getColsPerReplications());
		Assert.assertNull(result.getRowsPerReplications());
		Assert.assertNull(result.getNblatin());
		Assert.assertNull(result.getNclatin());
		Assert.assertNull(result.getNrlatin());
		Assert.assertNull(result.getReplatinGroups());
		Assert.assertNull(result.getReplicationsCount());
		Assert.assertNull(result.getFileName());
		Assert.assertNull(result.getReplicationPercentage());
		Assert.assertNull(result.getNumberOfBlocks());
		Assert.assertNull(result.getCheckInsertionManner());
		Assert.assertNull(result.getCheckSpacing());
		Assert.assertNull(result.getCheckStartingPosition());

	}

	@Test
	public void testConvertToExpDesignParamsUi_EmptyValues() {

		final List<MeasurementVariable> expDesigns = new ArrayList<>();

		expDesigns.add(this.createMeasurementVariable(TermId.BLOCK_SIZE.getId(), ""));
		expDesigns.add(this.createMeasurementVariable(TermId.NO_OF_COLS_IN_REPS.getId(), ""));
		expDesigns.add(this.createMeasurementVariable(TermId.NO_OF_ROWS_IN_REPS.getId(), ""));
		expDesigns.add(this.createMeasurementVariable(TermId.NO_OF_CBLKS_LATINIZE.getId(), ""));
		expDesigns.add(this.createMeasurementVariable(TermId.NO_OF_CCOLS_LATINIZE.getId(), ""));
		expDesigns.add(this.createMeasurementVariable(TermId.NO_OF_CROWS_LATINIZE.getId(), ""));
		expDesigns.add(this.createMeasurementVariable(TermId.NO_OF_REPS_IN_COLS.getId(), ""));
		expDesigns.add(this.createMeasurementVariable(TermId.NUMBER_OF_REPLICATES.getId(), ""));
		expDesigns.add(this.createMeasurementVariable(TermId.EXPT_DESIGN_SOURCE.getId(), ""));
		expDesigns.add(this.createMeasurementVariable(TermId.PERCENTAGE_OF_REPLICATION.getId(), ""));
		expDesigns.add(this.createMeasurementVariable(TermId.NBLKS.getId(), ""));
		expDesigns.add(this.createMeasurementVariable(TermId.CHECK_PLAN.getId(), ""));
		expDesigns.add(this.createMeasurementVariable(TermId.CHECK_INTERVAL.getId(), ""));
		expDesigns.add(this.createMeasurementVariable(TermId.CHECK_START.getId(), ""));

		final ExpDesignParameterUi result = SettingsUtil.convertToExpDesignParamsUi(expDesigns);

		Assert.assertNull(result.getBlockSize());
		Assert.assertNull(result.getColsPerReplications());
		Assert.assertNull(result.getRowsPerReplications());
		Assert.assertNull(result.getNblatin());
		Assert.assertNull(result.getNclatin());
		Assert.assertNull(result.getNrlatin());
		Assert.assertNull(result.getReplatinGroups());
		Assert.assertNull(result.getReplicationsCount());
		Assert.assertNull(result.getFileName());
		Assert.assertNull(result.getReplicationPercentage());
		Assert.assertNull(result.getNumberOfBlocks());
		Assert.assertNull(result.getCheckInsertionManner());
		Assert.assertNull(result.getCheckSpacing());
		Assert.assertNull(result.getCheckStartingPosition());

	}

	@Test
	public void testConvertToExpDesignParamsUi() {

		final List<MeasurementVariable> expDesigns = new ArrayList<>();

		expDesigns.add(this.createMeasurementVariable(TermId.BLOCK_SIZE.getId(), "1"));
		expDesigns.add(this.createMeasurementVariable(TermId.NO_OF_COLS_IN_REPS.getId(), "2"));
		expDesigns.add(this.createMeasurementVariable(TermId.NO_OF_ROWS_IN_REPS.getId(), "3"));
		expDesigns.add(this.createMeasurementVariable(TermId.NO_OF_CBLKS_LATINIZE.getId(), "4"));
		expDesigns.add(this.createMeasurementVariable(TermId.NO_OF_CCOLS_LATINIZE.getId(), "5"));
		expDesigns.add(this.createMeasurementVariable(TermId.NO_OF_CROWS_LATINIZE.getId(), "6"));
		expDesigns.add(this.createMeasurementVariable(TermId.NO_OF_REPS_IN_COLS.getId(), "7"));
		expDesigns.add(this.createMeasurementVariable(TermId.NUMBER_OF_REPLICATES.getId(), "8"));
		expDesigns.add(this.createMeasurementVariable(TermId.EXPT_DESIGN_SOURCE.getId(), "9"));
		expDesigns.add(this.createMeasurementVariable(TermId.PERCENTAGE_OF_REPLICATION.getId(), "10"));
		expDesigns.add(this.createMeasurementVariable(TermId.NBLKS.getId(), "11"));
		expDesigns.add(this.createMeasurementVariable(TermId.CHECK_PLAN.getId(), "12"));
		expDesigns.add(this.createMeasurementVariable(TermId.CHECK_INTERVAL.getId(), "13"));
		expDesigns.add(this.createMeasurementVariable(TermId.CHECK_START.getId(), "14"));

		final ExpDesignParameterUi result = SettingsUtil.convertToExpDesignParamsUi(expDesigns);

		Assert.assertEquals(1, result.getBlockSize().intValue());
		Assert.assertEquals(2, result.getColsPerReplications().intValue());
		Assert.assertEquals(3, result.getRowsPerReplications().intValue());
		Assert.assertEquals(4, result.getNblatin().intValue());
		Assert.assertEquals(5, result.getNclatin().intValue());
		Assert.assertEquals(6, result.getNrlatin().intValue());
		Assert.assertEquals("7", result.getReplatinGroups());
		Assert.assertEquals(8, result.getReplicationsCount().intValue());
		Assert.assertEquals("9", result.getFileName());
		Assert.assertEquals(10, result.getReplicationPercentage().intValue());
		Assert.assertEquals(11, result.getNumberOfBlocks().intValue());
		Assert.assertEquals(12, result.getCheckInsertionManner().intValue());
		Assert.assertEquals(13, result.getCheckSpacing().intValue());
		Assert.assertEquals(14, result.getCheckStartingPosition().intValue());

	}

	@Test
	public void testConvertToExpDesignParamsUiRandomizedBlockDesign() {

		final List<MeasurementVariable> expDesigns = new ArrayList<>();
		expDesigns.add(this.createMeasurementVariable(TermId.EXPERIMENT_DESIGN_FACTOR.getId(),
				String.valueOf(TermId.RANDOMIZED_COMPLETE_BLOCK.getId())));

		final ExpDesignParameterUi result;
		result = SettingsUtil.convertToExpDesignParamsUi(expDesigns);
		Assert.assertEquals(0, result.getDesignType().intValue());

	}

	@Test
	public void testConvertToExpDesignParamsUiResolvableIncompleteBlock() {

		final List<MeasurementVariable> expDesigns = new ArrayList<>();
		expDesigns.add(this.createMeasurementVariable(TermId.EXPERIMENT_DESIGN_FACTOR.getId(),
				String.valueOf(TermId.RESOLVABLE_INCOMPLETE_BLOCK.getId())));

		final ExpDesignParameterUi result;
		result = SettingsUtil.convertToExpDesignParamsUi(expDesigns);
		Assert.assertEquals(1, result.getDesignType().intValue());

	}

	@Test
	public void testConvertToExpDesignParamsUiResolvableIncompleteRowColumn() {

		final List<MeasurementVariable> expDesigns = new ArrayList<>();
		expDesigns.add(this.createMeasurementVariable(TermId.EXPERIMENT_DESIGN_FACTOR.getId(),
				String.valueOf(TermId.RESOLVABLE_INCOMPLETE_ROW_COL.getId())));

		final ExpDesignParameterUi result;
		result = SettingsUtil.convertToExpDesignParamsUi(expDesigns);
		Assert.assertEquals(2, result.getDesignType().intValue());

	}

	@Test
	public void testConvertToExpDesignParamsUiOtherDesign() {

		final List<MeasurementVariable> expDesigns = new ArrayList<>();
		expDesigns.add(this.createMeasurementVariable(TermId.EXPERIMENT_DESIGN_FACTOR.getId(),
				String.valueOf(TermId.OTHER_DESIGN.getId())));

		final ExpDesignParameterUi result;
		result = SettingsUtil.convertToExpDesignParamsUi(expDesigns);
		Assert.assertEquals(3, result.getDesignType().intValue());

	}

	@Test
	public void testConvertToExpDesignParamsUiRepsInSingleColumn() {

		final List<MeasurementVariable> expDesigns = new ArrayList<>();
		expDesigns.add(this.createMeasurementVariable(TermId.REPLICATIONS_MAP.getId(),
				String.valueOf(TermId.REPS_IN_SINGLE_COL.getId())));

		final ExpDesignParameterUi result;
		result = SettingsUtil.convertToExpDesignParamsUi(expDesigns);
		Assert.assertEquals(1, result.getReplicationsArrangement().intValue());

	}

	@Test
	public void testProcessTreatmentFactorItems() {
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(SettingsUtilTest.NFERT_NO_ID, SettingsUtilTest.PROGRAM_UUID)).thenReturn(StandardVariableTestDataInitializer.createStandardVariable(
				SettingsUtilTest.NFERT_NO_ID, SettingsUtilTest.NFERT_NO));
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(SettingsUtilTest.NFERT_KG_ID, SettingsUtilTest.PROGRAM_UUID)).thenReturn(StandardVariableTestDataInitializer.createStandardVariable(
				SettingsUtilTest.NFERT_KG_ID, SettingsUtilTest.NFERT_KG));
		final List<SettingDetail> treatmentFactorDetails =  new ArrayList<>();
		treatmentFactorDetails.add(SettingDetailTestDataInitializer.createSettingDetail(SettingsUtilTest.NFERT_NO_ID, SettingsUtilTest.NFERT_NO, SettingsUtilTest.NFERT_KG, PhenotypicType.TRIAL_DESIGN));
		treatmentFactorDetails.add(SettingDetailTestDataInitializer.createSettingDetail(SettingsUtilTest.NFERT_KG_ID, SettingsUtilTest.NFERT_KG, "1", PhenotypicType.TRIAL_DESIGN));
		final SettingDetail deletedTreatmentFactorLabel = SettingDetailTestDataInitializer.createSettingDetail(1, "Deleted", "Deleted TF Value", PhenotypicType.TRIAL_DESIGN);
		deletedTreatmentFactorLabel.getVariable().setOperation(Operation.DELETE);
		final SettingDetail deletedTreatmentFactorValue = SettingDetailTestDataInitializer.createSettingDetail(2, "Deleted TF Value", "1", PhenotypicType.TRIAL_DESIGN);
		deletedTreatmentFactorValue.getVariable().setOperation(Operation.DELETE);
		treatmentFactorDetails.add(deletedTreatmentFactorLabel);
		treatmentFactorDetails.add(deletedTreatmentFactorValue);
		final Map<String, TreatmentFactorData> treatmentFactorItems = new HashMap<>();
		final TreatmentFactorData data = new TreatmentFactorData();
		data.setVariableId(SettingsUtilTest.NFERT_KG_ID);
		data.setLabels(Arrays.asList("1"));
		treatmentFactorItems.put("1001", data);

		final List<Factor> factorList = new ArrayList<>();

		final List<TreatmentFactor> treatmentFactors = SettingsUtil.processTreatmentFactorItems(treatmentFactorDetails, treatmentFactorItems, factorList, this.fieldbookMiddlewareService, SettingsUtilTest.PROGRAM_UUID);
		Assert.assertEquals(1, treatmentFactors.size());
		Assert.assertEquals("1", treatmentFactors.get(0).getValue());
		Assert.assertEquals("1", treatmentFactors.get(0).getLevelNumber().toString());
		Assert.assertEquals(SettingsUtilTest.NFERT_NO, treatmentFactors.get(0).getLevelFactor().getName());
		Assert.assertEquals(SettingsUtilTest.NFERT_KG, treatmentFactors.get(0).getValueFactor().getName());
	}

	@Test
	public void testAddFactor() {
		final Map<Integer, Factor> factorsMap = new HashMap<>();
		final Factor factor = new Factor();
		factor.setId(1);
		final List<Factor> factorList = new ArrayList<>();

		SettingsUtil.addFactor(factorsMap, factor, factorList);
		Assert.assertEquals(1, factorList.size());
		//Try to re-add the factor
		SettingsUtil.addFactor(factorsMap, factor, factorList);
		Assert.assertEquals(1, factorList.size());
	}

	@Test
	public void testConvertToExpDesignParamsUiRepsInSingleRow() {

		final List<MeasurementVariable> expDesigns = new ArrayList<>();
		expDesigns.add(this.createMeasurementVariable(TermId.REPLICATIONS_MAP.getId(),
				String.valueOf(TermId.REPS_IN_SINGLE_ROW.getId())));

		final ExpDesignParameterUi result;
		result = SettingsUtil.convertToExpDesignParamsUi(expDesigns);
		Assert.assertEquals(2, result.getReplicationsArrangement().intValue());

	}

	@Test
	public void testConvertToExpDesignParamsUiRepsInAdjacentCol() {

		final List<MeasurementVariable> expDesigns = new ArrayList<>();
		expDesigns.add(this.createMeasurementVariable(TermId.REPLICATIONS_MAP.getId(),
				String.valueOf(TermId.REPS_IN_ADJACENT_COLS.getId())));

		final ExpDesignParameterUi result;
		result = SettingsUtil.convertToExpDesignParamsUi(expDesigns);
		Assert.assertEquals(3, result.getReplicationsArrangement().intValue());

	}

	@Test
	public void testGetExperimentalDesignValue() {

		final ExpDesignParameterUi expDesignParameterUi = new ExpDesignParameterUi();

		expDesignParameterUi.setBlockSize(1);
		expDesignParameterUi.setColsPerReplications(2);
		expDesignParameterUi.setRowsPerReplications(3);
		expDesignParameterUi.setNblatin(4);
		expDesignParameterUi.setNclatin(5);
		expDesignParameterUi.setNrlatin(6);
		expDesignParameterUi.setReplatinGroups("7");
		expDesignParameterUi.setReplicationsCount(8);
		expDesignParameterUi.setUseLatenized(false);

		Assert.assertEquals("1", SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.BLOCK_SIZE));
		Assert.assertEquals("2",
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.NO_OF_COLS_IN_REPS));
		Assert.assertEquals("3",
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.NO_OF_ROWS_IN_REPS));
		Assert.assertEquals("4",
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.NO_OF_CBLKS_LATINIZE));
		Assert.assertEquals("5",
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.NO_OF_CCOLS_LATINIZE));
		Assert.assertEquals("6",
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.NO_OF_CROWS_LATINIZE));
		Assert.assertEquals("7",
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.NO_OF_REPS_IN_COLS));
		Assert.assertEquals("8",
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.NUMBER_OF_REPLICATES));

		expDesignParameterUi.setDesignType(0);
		Assert.assertEquals(String.valueOf(TermId.RANDOMIZED_COMPLETE_BLOCK.getId()),
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.EXPERIMENT_DESIGN_FACTOR));

		expDesignParameterUi.setDesignType(1);
		expDesignParameterUi.setUseLatenized(false);
		Assert.assertEquals(String.valueOf(TermId.RESOLVABLE_INCOMPLETE_BLOCK.getId()),
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.EXPERIMENT_DESIGN_FACTOR));

		expDesignParameterUi.setDesignType(1);
		expDesignParameterUi.setUseLatenized(true);
		Assert.assertEquals(String.valueOf(TermId.RESOLVABLE_INCOMPLETE_BLOCK_LATIN.getId()),
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.EXPERIMENT_DESIGN_FACTOR));

		expDesignParameterUi.setDesignType(2);
		expDesignParameterUi.setUseLatenized(false);
		Assert.assertEquals(String.valueOf(TermId.RESOLVABLE_INCOMPLETE_ROW_COL.getId()),
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.EXPERIMENT_DESIGN_FACTOR));

		expDesignParameterUi.setDesignType(2);
		expDesignParameterUi.setUseLatenized(true);
		Assert.assertEquals(String.valueOf(TermId.RESOLVABLE_INCOMPLETE_ROW_COL_LATIN.getId()),
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.EXPERIMENT_DESIGN_FACTOR));

		expDesignParameterUi.setDesignType(3);
		expDesignParameterUi.setUseLatenized(false);
		Assert.assertEquals(String.valueOf(TermId.OTHER_DESIGN.getId()),
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.EXPERIMENT_DESIGN_FACTOR));

		expDesignParameterUi.setDesignType(4);
		expDesignParameterUi.setUseLatenized(false);
		Assert.assertEquals(String.valueOf(TermId.AUGMENTED_RANDOMIZED_BLOCK.getId()),
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.EXPERIMENT_DESIGN_FACTOR));

		expDesignParameterUi.setUseLatenized(false);
		expDesignParameterUi.setDesignType(5);
		Assert.assertEquals(String.valueOf(TermId.ENTRY_LIST_ORDER.getId()),
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.EXPERIMENT_DESIGN_FACTOR));

		expDesignParameterUi.setDesignType(6);
		Assert.assertEquals(String.valueOf(TermId.P_REP.getId()),
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.EXPERIMENT_DESIGN_FACTOR));

		expDesignParameterUi.setReplicationsArrangement(1);
		Assert.assertEquals(String.valueOf(TermId.REPS_IN_SINGLE_COL.getId()),
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.REPLICATIONS_MAP));

		expDesignParameterUi.setReplicationsArrangement(2);
		Assert.assertEquals(String.valueOf(TermId.REPS_IN_SINGLE_ROW.getId()),
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.REPLICATIONS_MAP));

		expDesignParameterUi.setReplicationsArrangement(3);
		Assert.assertEquals(String.valueOf(TermId.REPS_IN_ADJACENT_COLS.getId()),
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.REPLICATIONS_MAP));
	}

	private List<Integer> settingsUtilParseVariableIds(final String variableIds) {
		return SettingsUtil.parseVariableIds(variableIds);
	}

	private List<SettingDetail> createCheckVariables(final boolean hasValue) {
		final List<SettingDetail> checkVariables = new ArrayList<SettingDetail>();

		checkVariables.add(this.createSettingDetail(TermId.CHECK_START.getId(), hasValue ? "1" : null));
		checkVariables.add(this.createSettingDetail(TermId.CHECK_INTERVAL.getId(), hasValue ? "4" : null));
		checkVariables.add(this.createSettingDetail(TermId.CHECK_PLAN.getId(), hasValue ? "8414" : null));

		return checkVariables;
	}

	private SettingDetail createSettingDetail(final int cvTermId, final String value) {
		final SettingVariable variable = new SettingVariable();
		variable.setCvTermId(cvTermId);
		final SettingDetail settingDetail = new SettingDetail(variable, null, value, false);

		if (cvTermId == TermId.CHECK_PLAN.getId()) {
			final List<ValueReference> possibleValues = new ArrayList<>();
			possibleValues.add(new ValueReference(8414, "1", "Insert each check in turn"));
			possibleValues.add(new ValueReference(8415, "2", "Insert all checks at each position"));
			settingDetail.setPossibleValues(possibleValues);
		} else if (cvTermId == TermId.CHECK_INTERVAL.getId()) {
			settingDetail.setPossibleValues(new ArrayList<ValueReference>());
		}
		return settingDetail;
	}

	@Test
	public void testcleanSheetAndFileNameWithInvalid() {
		final String cleanedName = SettingsUtil.cleanSheetAndFileName("Test[:\\\\/*?|<>]");
		Assert.assertEquals("String should be cleaned", "Test[_________]", cleanedName);
	}

	@Test
	public void testGetCodeInPossibleValues() {
		final List<ValueReference> valueRefs = new ArrayList<ValueReference>();
		valueRefs.add(new ValueReference(8414, "1"));
		valueRefs.add(new ValueReference(8415, "2"));
		Assert.assertEquals("Should return 1 since the matching name for 8414 is 1", 1,
				SettingsUtil.getCodeInPossibleValues(valueRefs, "8414"));
		Assert.assertEquals("Should return 2 since the matching name for 8415 is 2", 2,
				SettingsUtil.getCodeInPossibleValues(valueRefs, "8415"));
	}

	@Test
	public void testSetSettingDetailRoleForDefaultVartypes() {
		// Create a standardVaraible as pre-req
		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setVariableTypes(null);

		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.CATEGORICAL_VARIATE.getId(),
				SettingsUtilTest.PROGRAM_UUID)).thenReturn(standardVariable);

		for (final VariableType varType : VariableType.values()) {
			final SettingDetail settingDetail = new SettingDetail();
			final SettingVariable settingVariable = new SettingVariable();
			settingVariable.setCvTermId(TermId.CATEGORICAL_VARIATE.getId());
			settingDetail.setVariable(settingVariable);
			settingDetail.setRole(null);

			final List<SettingDetail> detailList = new ArrayList<>();
			// use any setting variable that is not a trial instance factor
			detailList.add(settingDetail);

			SettingsUtil.setSettingDetailRoleAndVariableType(varType.getId(), detailList,
					this.fieldbookMiddlewareService, SettingsUtilTest.PROGRAM_UUID);
			Assert.assertEquals("Should have the correct phenotypic type role as per the variable type",
					varType.getRole(), settingDetail.getRole());

		}
	}

	@Test
	public void testSetSettingDetailRoleWithTrialInstanceFactorAsRole() {
		// SettingDetail contains Trial instance factor
		final List<SettingDetail> newDetails = new ArrayList<SettingDetail>();
		final SettingDetail detail = new SettingDetail();
		final SettingVariable variable = new SettingVariable();
		variable.setCvTermId(TermId.TRIAL_INSTANCE_FACTOR.getId());
		detail.setVariable(variable);
		newDetails.add(detail);

		// for mode, we use any that is not a germplasm descriptor
		final VariableType studyDetailMode = VariableType.STUDY_DETAIL;
		SettingsUtil.setSettingDetailRoleAndVariableType(studyDetailMode.getId().intValue(), newDetails,
				this.fieldbookMiddlewareService, SettingsUtilTest.PROGRAM_UUID);
		Assert.assertEquals(
				"Since we had a settingDetail that is a trial instance factor, the detail's role should be converted to Trial Environment",
				PhenotypicType.TRIAL_ENVIRONMENT, detail.getRole());

	}

	/**
	 * Test for check if given baseline traits empty or null then empty variate
	 * list should be returned.
	 */
	@Test
	public void testConvertBaselineTraitsToVariatesWithEmptyBaselineTraits() {
		final List<SettingDetail> baselineTraits = new ArrayList<>();

		final List<Variate> baselineVariates = SettingsUtil.convertBaselineTraitsToVariates(baselineTraits,
				this.fieldbookMiddlewareService, SettingsUtilTest.PROGRAM_UUID);
		Assert.assertEquals(baselineTraits.size(), baselineVariates.size());
	}

	/**
	 * Test to verify Role and Variable Type as STUDY_DETAIL
	 */
	@Test
	public void testSetSettingDetailRoleAndVariableTypeForStudyDetailVariableType() {
		final List<SettingDetail> newDetails = new ArrayList<>();
		final SettingDetail detail = new SettingDetail();
		final SettingVariable variable = new SettingVariable();
		variable.setCvTermId(TermId.EXPERIMENT_DESIGN_FACTOR.getId());
		detail.setVariable(variable);
		newDetails.add(detail);

		final VariableType studyDetailMode = VariableType.STUDY_DETAIL;
		SettingsUtil.setSettingDetailRoleAndVariableType(studyDetailMode.getId(), newDetails,
				this.fieldbookMiddlewareService, SettingsUtilTest.PROGRAM_UUID);

		Assert.assertEquals("Invalid Role", VariableType.STUDY_DETAIL.getRole(), detail.getRole());
		Assert.assertEquals("Invalid Variable Type", VariableType.STUDY_DETAIL, detail.getVariableType());
	}

	/**
	 * Test to verify Role and Variable Type as GERMPLASM_DESCRIPTOR
	 */
	@Test
	public void testSetSettingDetailRoleAndVariableTypeForGermplasmDescriptorVariableType() {
		final List<SettingDetail> newDetails = new ArrayList<>();
		final SettingDetail detail = new SettingDetail();
		final SettingVariable variable = new SettingVariable();
		variable.setCvTermId(TermId.GERMPLASM_SOURCE.getId());

		// Set Variable types as GERMPLASM_DESCRIPTOR as we are checking for
		// GERMPLASM_DESCRIPTOR
		final Set<VariableType> variableTypeSet = new HashSet<>();
		variableTypeSet.add(VariableType.GERMPLASM_DESCRIPTOR);
		variable.setVariableTypes(variableTypeSet);
		detail.setVariable(variable);
		newDetails.add(detail);

		final VariableType germplasmDescriptorMode = VariableType.GERMPLASM_DESCRIPTOR;
		SettingsUtil.setSettingDetailRoleAndVariableType(germplasmDescriptorMode.getId(), newDetails,
				this.fieldbookMiddlewareService, SettingsUtilTest.PROGRAM_UUID);

		Assert.assertEquals("Invalid Role", VariableType.GERMPLASM_DESCRIPTOR.getRole(), detail.getRole());
		Assert.assertEquals("Invalid Variable Type", VariableType.GERMPLASM_DESCRIPTOR, detail.getVariableType());
	}

	/**
	 * Test to verify Role and Variable Type as EXPERIMENTAL_DESIGN
	 */
	@Test
	public void testSetSettingDetailRoleAndVariableTypeForExperimentalDesignVariableType() {
		final List<SettingDetail> newDetails = new ArrayList<>();
		final SettingDetail detail = new SettingDetail();
		final SettingVariable variable = new SettingVariable();
		variable.setCvTermId(TermId.GERMPLASM_SOURCE.getId());

		// Set Variable type as EXPERIMENTAL_DESIGN as we are checking for
		// EXPERIMENTAL_DESIGN
		final Set<VariableType> variableTypeSet = new HashSet<>();
		variableTypeSet.add(VariableType.EXPERIMENTAL_DESIGN);
		variable.setVariableTypes(variableTypeSet);
		detail.setVariable(variable);
		newDetails.add(detail);

		final VariableType germplasmDescriptorMode = VariableType.GERMPLASM_DESCRIPTOR;
		SettingsUtil.setSettingDetailRoleAndVariableType(germplasmDescriptorMode.getId(), newDetails,
				this.fieldbookMiddlewareService, SettingsUtilTest.PROGRAM_UUID);

		Assert.assertEquals("Invalid Role", VariableType.EXPERIMENTAL_DESIGN.getRole(), detail.getRole());
		Assert.assertEquals("Invalid Variable Type", VariableType.EXPERIMENTAL_DESIGN, detail.getVariableType());
	}

	protected MeasurementVariable createMeasurementVariable(final int termId, final String value) {
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(termId);
		measurementVariable.setValue(value);
		return measurementVariable;
	}

	/**
	 * Test to verify convertPojoToXmlDataSet method works properly or not
	 */
	@Test
	public void testConvertPojoToXmlDataSet() {
		final String dataSetName = "January Study";

		final List<SettingDetail> studyLevelConditions = new ArrayList<>();

		final List<SettingDetail> basicDetails = this.createSettingDetailVariables();
		basicDetails.get(0).setRole(VariableType.SELECTION_METHOD.getRole());

		final Map<String, TreatmentFactorData> treatmentFactorItems = new HashMap<>();

		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setName("Standard Variable");
		standardVariable.setMethod(TestDataHelper.createMethod());
		standardVariable.setProperty(TestDataHelper.createProperty());
		standardVariable.setScale(TestDataHelper.createScale());

		final DataType dataType = DataType.getById(TermId.NUMERIC_VARIABLE.getId());
		standardVariable.setDataType(new Term(dataType.getId(), dataType.getName(), dataType.getName()));

		final SettingDetail settingDetail = new SettingDetail();
		settingDetail.setVariable(basicDetails.get(0).getVariable());
		settingDetail.setRole(VariableType.SELECTION_METHOD.getRole());
		settingDetail.setVariableType(VariableType.SELECTION_METHOD);

		final List<SettingDetail> variatesList = new ArrayList<>();
		variatesList.add(settingDetail);

		final List<ValueReference> valueReferenceList = new ArrayList<>();
		final Variate variate = new Variate("BM_CODE_VTE", "Breeding method observed on each plot (CODE)",
				TestDataHelper.createProperty().getName(), TestDataHelper.createScale().getName(),
				TestDataHelper.createMethod().getName(), VariableType.SELECTION_METHOD.getRole().name(), "N",
				DataType.NUMERIC_VARIABLE.getId(), valueReferenceList, 50.00, 500.00);
		variate.setVariableType("Selection Method");

		Mockito.when(this.userSelection.getStudyLevelConditions()).thenReturn(studyLevelConditions);
		Mockito.when(this.userSelection.getBasicDetails()).thenReturn(basicDetails);
		Mockito.when(this.userSelection.getBaselineTraitsList()).thenReturn(variatesList);
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(ArgumentMatchers.anyInt(), ArgumentMatchers.anyString()))
				.thenReturn(standardVariable);
		Mockito.when(this.userSelection.getPlotsLevelList()).thenReturn(basicDetails);
		Mockito.when(this.userSelection.getStudyConditions()).thenReturn(basicDetails);

		final Dataset dataSet = (Dataset) SettingsUtil.convertPojoToXmlDataSet(this.fieldbookMiddlewareService,
				dataSetName, this.userSelection, treatmentFactorItems, SettingsUtilTest.PROGRAM_UUID);

		Assert.assertEquals("DataSet Name", dataSetName, dataSet.getName());
		Assert.assertEquals("DataSet Study Level Factor", 0, dataSet.getTrialLevelFactor().size());
		Assert.assertEquals("DataSet Treatment Factor", 0, dataSet.getTreatmentFactors().size());

		int i = 0;
		for (final Condition condition : dataSet.getConditions()) {
			Assert.assertEquals("DataSet Condition Name", condition.getName(),
					basicDetails.get(i).getVariable().getName());
			Assert.assertEquals("Property", condition.getProperty(), basicDetails.get(i).getVariable().getProperty());
			Assert.assertEquals("Scale", condition.getScale(), basicDetails.get(i).getVariable().getScale());
			Assert.assertEquals("Method", condition.getMethod(), basicDetails.get(i).getVariable().getMethod());
			Assert.assertEquals("Data Type", condition.getDatatype(), basicDetails.get(i).getVariable().getDataType());
			i++;
		}

		for (final Variate dataSetVariate : dataSet.getVariates()) {
			Assert.assertEquals("Variate Name", variate.getName(), dataSetVariate.getName());
			Assert.assertEquals("Variate Property", variate.getProperty(), dataSetVariate.getProperty());
			Assert.assertEquals("Variate Scale", variate.getScale(), dataSetVariate.getScale());
			Assert.assertEquals("Variate DataType", variate.getDatatype(), dataSetVariate.getDatatype());
			Assert.assertEquals("Variate Role", variate.getRole(), dataSetVariate.getRole());
			Assert.assertEquals("Variate Variable Type", variate.getVariableType(), dataSetVariate.getVariableType());
		}

		i = 0;
		for (final Factor factor : dataSet.getFactors()) {
			Assert.assertEquals("Factor Name", basicDetails.get(i).getVariable().getName(), factor.getName());
			Assert.assertEquals("Factor Property", basicDetails.get(i).getVariable().getProperty(),
					factor.getProperty());
			Assert.assertEquals("Factor Scale", basicDetails.get(i).getVariable().getScale(), factor.getScale());
			Assert.assertEquals("Factor Method", basicDetails.get(i).getVariable().getMethod(), factor.getMethod());
			Assert.assertEquals("Factor Data Type", "Numeric", factor.getDatatype());
			Assert.assertEquals("Factor Role", basicDetails.get(i).getVariable().getRole(), factor.getRole());
			i++;
		}

		i = 0;
		for (final Constant constant : dataSet.getConstants()) {
			Assert.assertEquals("Constant Name", basicDetails.get(i).getVariable().getName(), constant.getName());
			Assert.assertEquals("Constant Property", basicDetails.get(i).getVariable().getProperty(),
					constant.getProperty());
			Assert.assertEquals("Constant Scale", basicDetails.get(i).getVariable().getScale(), constant.getScale());
			Assert.assertEquals("Constant Method", basicDetails.get(i).getVariable().getMethod(), constant.getMethod());
			Assert.assertEquals("Constant Data Type", basicDetails.get(i).getVariable().getDataType(),
					constant.getDatatype());
			Assert.assertEquals("Constant Role", basicDetails.get(i).getVariable().getRole(), constant.getRole());
			i++;
		}
	}

	private List<SettingDetail> createSettingDetailVariables() {
		final List<SettingDetail> variables = new ArrayList<>();
		variables.add(this.createSettingDetail(TermId.BREEDING_METHOD_VARIATE_CODE.getId(), "BM_CODE_VTE",
				"Breeding method observed on each plot(CODE)", SettingsUtilTest.C_DATATYPE));
		return variables;
	}

	private SettingDetail createSettingDetail(final int cvTermId, final String name, final String value,
			final String dataType) {
		final SettingVariable variable = new SettingVariable();
		variable.setCvTermId(cvTermId);
		variable.setName(name);
		variable.setRole(PhenotypicType.STUDY.toString());
		final SettingDetail settingDetail = new SettingDetail(variable, null, value, false);
		settingDetail.setRole(PhenotypicType.STUDY);
		return settingDetail;
	}

	@Test
	public void testConvertDetailsToConditionsWithNullSettingDetailsList() {
		final List<Condition> conditions = SettingsUtil.convertDetailsToConditions(null,
				this.fieldbookMiddlewareService, SettingsUtilTest.PROGRAM_UUID);
		Assert.assertNotNull("Conditions should not be null", conditions);
		Assert.assertEquals("Conditions should be empty", 0, conditions.size());
	}

	@Test
	public void testConvertDetailsToConditions() {
		final List<SettingDetail> conditionSettingDetails = new ArrayList<>();
		final List<SettingDetail> studyLevelConditions = this.createStudyLevelConditions();
		final List<SettingDetail> basicDetails = this.createSettingDetailVariables();
		conditionSettingDetails.addAll(basicDetails);
		conditionSettingDetails.addAll(studyLevelConditions);

		this.mockGetStandardVariable(conditionSettingDetails);

		final List<Condition> conditions = SettingsUtil.convertDetailsToConditions(conditionSettingDetails,
				this.fieldbookMiddlewareService, SettingsUtilTest.PROGRAM_UUID);
		Assert.assertNotNull("Conditions should not be null", conditions);

		final int expectedSize = conditionSettingDetails.size();
		Assert.assertEquals("Conditions should contain " + expectedSize + " items", expectedSize, conditions.size());

		for (final Condition condition : conditions) {
			if (TermId.BREEDING_METHOD_VARIATE_CODE.getId() == condition.getId()) {
				Assert.assertEquals("The name should be 'BM_CODE_VTE'", "BM_CODE_VTE", condition.getName());
				Assert.assertEquals("The value should be 'Breeding method observed on each plot(CODE)'",
						"Breeding method observed on each plot(CODE)", condition.getValue());
			} else if (TermId.SEASON_VAR.getId() == condition.getId()) {
				Assert.assertEquals("The name should be 'Crop_season_Code'", "Crop_season_Code", condition.getName());
				Assert.assertEquals("The description should be 'Season - Assigned (Code)'", "Season - Assigned (Code)",
						condition.getDescription());
				Assert.assertEquals("The property should be 'Season'", "Season", condition.getProperty());
				Assert.assertEquals("The scale should be 'Code of Crop_season_Code'", "Code of Crop_season_Code",
						condition.getScale());
				Assert.assertEquals("The method should be 'Assigned'", "Assigned", condition.getMethod());
				Assert.assertEquals("The role should be '" + PhenotypicType.STUDY.toString() + "'",
						PhenotypicType.STUDY.toString(), condition.getRole());
				Assert.assertEquals("The datatype should be 'C'", "C", condition.getDatatype());
				Assert.assertEquals("The datatypeId should be '" + TermId.CATEGORICAL_VARIABLE.getId() + "'",
						TermId.CATEGORICAL_VARIABLE.getId(), condition.getDataTypeId().intValue());
			}
		}
	}
	
	@Test
	public void testConvertWorkbookStudyLevelVariablesToStudyDetails() {
		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook();
		workbook.getStudyDetails().setIsLocked(true);
		final String createdBy = "123";
		
		final StudyDetails studyDetails = SettingsUtil.convertWorkbookStudyLevelVariablesToStudyDetails(workbook, this.fieldbookMiddlewareService, this.fieldbookService, this.userSelection, PROGRAM_UUID, this.properties, createdBy);
		final org.generationcp.middleware.domain.etl.StudyDetails sourceStudyDetails = workbook.getStudyDetails();
		Assert.assertEquals(sourceStudyDetails.getId(), studyDetails.getId());
		Assert.assertEquals(sourceStudyDetails.getProgramUUID(), studyDetails.getProgramUUID());
		Assert.assertTrue(studyDetails.getIsLocked());
		Assert.assertEquals(sourceStudyDetails.getCreatedBy(), studyDetails.getOwnerId().toString());
		Assert.assertEquals(sourceStudyDetails.getStudyName(), studyDetails.getName());
	}

	private void mockGetStandardVariable(final List<SettingDetail> conditionSettingDetails) {
		for (final SettingDetail settingDetail : conditionSettingDetails) {

			final SettingVariable settingVariable = settingDetail.getVariable();
			final StandardVariable standardVariable = new StandardVariable();
			standardVariable.setId(settingVariable.getCvTermId());
			standardVariable.setName(settingVariable.getName());
			standardVariable.setProperty(
					new Term(SettingsUtilTest.DUMMY_ID, settingVariable.getProperty(), settingVariable.getProperty()));
			standardVariable.setScale(
					new Term(SettingsUtilTest.DUMMY_ID, settingVariable.getScale(), settingVariable.getScale()));
			standardVariable.setMethod(
					new Term(SettingsUtilTest.DUMMY_ID, settingVariable.getMethod(), settingVariable.getMethod()));
			standardVariable.setPhenotypicType(settingDetail.getRole());
			standardVariable.setDescription(settingVariable.getDescription());
			if (settingVariable.getDataTypeId() != null && settingVariable.getDataTypeId()
					.intValue() == SettingsUtilTest.CATEGORICAL_DATATYPE_TERM.getId()) {
				standardVariable.setDataType(SettingsUtilTest.CATEGORICAL_DATATYPE_TERM);
			} else {
				standardVariable.setDataType(SettingsUtilTest.C_DATATYPE_TERM);
			}

			Mockito.doReturn(standardVariable).when(this.fieldbookMiddlewareService)
					.getStandardVariable(settingVariable.getCvTermId().intValue(), SettingsUtilTest.PROGRAM_UUID);
		}

	}

	private List<SettingDetail> createStudyLevelConditions() {
		final List<SettingDetail> studyLevelConditions = new ArrayList<>();

		final SettingDetail settingDetail = new SettingDetail();
		studyLevelConditions.add(settingDetail);

		final SettingVariable cropSeasonCodeVariable = this.createCropSeasonCodeSettingVariable();
		settingDetail.setVariable(cropSeasonCodeVariable);

		final List<ValueReference> cropSeasonCodePossibleValues = this.createCropSeasonPossibleValues();
		settingDetail.setPossibleValues(cropSeasonCodePossibleValues);

		// set value to the numeric value of the first categorical variable
		settingDetail.setValue(cropSeasonCodePossibleValues.get(0).getId().toString());

		settingDetail.setRole(PhenotypicType.STUDY);
		settingDetail.setVariableType(VariableType.STUDY_DETAIL);

		return studyLevelConditions;
	}

	private List<ValueReference> createCropSeasonPossibleValues() {
		final List<ValueReference> cropSeasonCodePossibleValues = new ArrayList<>();
		cropSeasonCodePossibleValues.add(new ValueReference(10290, "Dry season"));
		cropSeasonCodePossibleValues.add(new ValueReference(10300, "Wet season"));
		cropSeasonCodePossibleValues.add(new ValueReference(60084, "Main season"));
		cropSeasonCodePossibleValues.add(new ValueReference(60085, "Off season"));
		return cropSeasonCodePossibleValues;
	}

	private SettingVariable createCropSeasonCodeSettingVariable() {
		final SettingVariable cropSeasonCodeVariable = new SettingVariable();
		cropSeasonCodeVariable.setCvTermId(TermId.SEASON_VAR.getId());
		cropSeasonCodeVariable.setName("Crop_season_Code");
		cropSeasonCodeVariable.setDescription("Season - Assigned (Code)");
		cropSeasonCodeVariable.setProperty("Season");
		cropSeasonCodeVariable.setScale("Code of Crop_season_Code");
		cropSeasonCodeVariable.setMethod("Assigned");
		cropSeasonCodeVariable.setRole(PhenotypicType.STUDY.toString());
		cropSeasonCodeVariable.setDataType("C");
		cropSeasonCodeVariable.setDataTypeId(TermId.CATEGORICAL_VARIABLE.getId());
		return cropSeasonCodeVariable;
	}

	@Test
	public void testConvertConditionToMeasurementVariable() {
		final Condition cropSeasonCodeCondition = new Condition("Crop_season_Code", "Season - Assigned (Code)",
			"Season", "Code of Crop_season_Code", "Assigned", PhenotypicType.STUDY.toString(), "C", "Dry season",
			TermId.CATEGORICAL_VARIABLE.getId(), null, null);
		cropSeasonCodeCondition.setOperation(Operation.ADD);
		cropSeasonCodeCondition.setId(TermId.SEASON_VAR.getId());
		cropSeasonCodeCondition.setPossibleValues(this.createCropSeasonPossibleValues());

		final MeasurementVariable measurementVariable = SettingsUtil
			.convertConditionToMeasurementVariable(cropSeasonCodeCondition);

		Assert.assertEquals("The name should be '" + cropSeasonCodeCondition.getName() + "'",
			cropSeasonCodeCondition.getName(), measurementVariable.getName());
		Assert.assertEquals("The description should be '" + cropSeasonCodeCondition.getDescription() + "'",
			cropSeasonCodeCondition.getDescription(), measurementVariable.getDescription());
		Assert.assertEquals("The property should be '" + cropSeasonCodeCondition.getProperty() + "'",
			cropSeasonCodeCondition.getProperty(), measurementVariable.getProperty());
		Assert.assertEquals("The scale should be '" + cropSeasonCodeCondition.getScale() + "'",
			cropSeasonCodeCondition.getScale(), measurementVariable.getScale());
		Assert.assertEquals("The method should be '" + cropSeasonCodeCondition.getMethod() + "'",
			cropSeasonCodeCondition.getMethod(), measurementVariable.getMethod());
		Assert.assertEquals("The datatype should be '" + cropSeasonCodeCondition.getDatatype() + "'",
			cropSeasonCodeCondition.getDatatype(), measurementVariable.getDataType());
		Assert.assertEquals("The dataTypeId should be '" + cropSeasonCodeCondition.getDataTypeId().intValue() + "'",
			cropSeasonCodeCondition.getDataTypeId().intValue(), measurementVariable.getDataTypeId().intValue());
		Assert.assertEquals("The value should be '" + cropSeasonCodeCondition.getValue() + "'",
			cropSeasonCodeCondition.getValue(), measurementVariable.getValue());
		Assert.assertEquals("The label should be 'STUDY'", "STUDY", measurementVariable.getLabel());
		Assert.assertEquals("The minRange should be '" + cropSeasonCodeCondition.getMinRange() + "'",
			cropSeasonCodeCondition.getMinRange(), measurementVariable.getMinRange());
		Assert.assertEquals("The maxRange should be '" + cropSeasonCodeCondition.getMaxRange() + "'",
			cropSeasonCodeCondition.getMaxRange(), measurementVariable.getMaxRange());
		Assert.assertEquals("The role should be '" + PhenotypicType.STUDY + "'", PhenotypicType.STUDY,
			measurementVariable.getRole());
		Assert.assertEquals("The operation should be '" + cropSeasonCodeCondition.getOperation() + "'",
			cropSeasonCodeCondition.getOperation(), measurementVariable.getOperation());
		Assert.assertEquals("The termId should be '" + cropSeasonCodeCondition.getId() + "'",
			cropSeasonCodeCondition.getId(), measurementVariable.getTermId());
		Assert.assertTrue("It should be a factor", measurementVariable.isFactor());
		Assert.assertEquals("The possibleValues should be '" + cropSeasonCodeCondition.getPossibleValues() + "'",
			cropSeasonCodeCondition.getPossibleValues(), measurementVariable.getPossibleValues());
	}

	@Test
	public void testConvertConstantToMeasurementVariableOperationAddOrUpdate() {

		final Constant constant =
			new Constant("CONSTANT1", "CONSTANT1", "YIELD (GRAIN)", "Kg/ha", "Paddy Rice", PhenotypicType.VARIATE.toString(), "N", "",
				TermId.NUMERIC_VARIABLE.getId(), 0.0, 0.0);
		constant.setOperation(Operation.ADD);
		final MeasurementVariable measurementVariable = SettingsUtil.convertConstantToMeasurementVariable(constant);
		Assert.assertEquals(PhenotypicType.TRIAL_ENVIRONMENT.getLabelList().get(0), measurementVariable.getLabel());

		constant.setOperation(Operation.UPDATE);
		final MeasurementVariable measurementVariable2 = SettingsUtil.convertConstantToMeasurementVariable(constant);
		Assert.assertEquals(PhenotypicType.TRIAL_ENVIRONMENT.getLabelList().get(0), measurementVariable2.getLabel());

	}

	@Test
	public void testConvertConstantToMeasurementVariable() {

		final Constant constant =
			new Constant("CONSTANT1", "CONSTANT1", "YIELD (GRAIN)", "Kg/ha", "Paddy Rice", PhenotypicType.VARIATE.toString(), "N", "",
				TermId.NUMERIC_VARIABLE.getId(), 0.0, 0.0);
		final MeasurementVariable measurementVariable = SettingsUtil.convertConstantToMeasurementVariable(constant);

		Assert.assertEquals(constant.getName(), measurementVariable.getName());
		Assert.assertEquals(
			constant.getDescription(),
			measurementVariable.getDescription());
		Assert.assertEquals(constant.getProperty(), measurementVariable.getProperty());
		Assert.assertEquals(constant.getScale(), measurementVariable.getScale());
		Assert.assertEquals(constant.getMethod(), measurementVariable.getMethod());
		Assert.assertEquals(constant.getRole(), measurementVariable.getRole().name());
		Assert.assertEquals(constant.getDatatype(), measurementVariable.getDataType());
		Assert.assertEquals(PhenotypicType.STUDY.getLabelList().get(0), measurementVariable.getLabel());
		Assert.assertEquals(VariableType.ENVIRONMENT_CONDITION, measurementVariable.getVariableType());
		Assert.assertFalse(measurementVariable.isFactor());

	}

}
