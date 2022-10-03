
package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import org.generationcp.middleware.ruleengine.pojo.AdvancingSource;
import com.google.common.collect.Lists;
import org.generationcp.middleware.data.initializer.MeasurementDataTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.ValueReferenceTestDataInitializer;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SeasonExpressionDataProcessorTest {

	private static final String EMPTY_STRING = "";
	public static final String SEASON_NAME_WET = "1";
	public static final String SEASON_NAME_DRY = "2";
	public static final String SEASON_DESCRIPTION_DRY = "Dry Season";
	public static final String SEASON_DESCRIPTION_WET = "Wet Season";
	public static final int SEASON_ID_WET = 10101;
	public static final int SEASON_ID_DRY = 20202;

	private static final String SEASON_CATEGORY_ID = "10290";
	private static final String SEASON_CATEGORY_VALUE = "Dry Season";
	private static final String SEASON_MONTH_VALUE = "201608";

	@InjectMocks
	private SeasonExpressionDataProcessor seasonExpressionDataProcessor;

	private WorkbookTestDataInitializer workbookTestDataInitializer;
	private MeasurementDataTestDataInitializer measurementDataTestDataInitializer;
	private ValueReferenceTestDataInitializer valueReferenceTestDataInitializer;
	private AdvancingSource advancingSource;

	@Before
	public void setUp() {
		this.workbookTestDataInitializer = new WorkbookTestDataInitializer();
		this.measurementDataTestDataInitializer = new MeasurementDataTestDataInitializer();
		this.valueReferenceTestDataInitializer = new ValueReferenceTestDataInitializer();
		this.advancingSource = new AdvancingSource();
	}

	@Test
	public void testProcessEnvironmentLevelDataWithSeasonMonthVariable() {
		final Workbook workbook = this.workbookTestDataInitializer.createWorkbook(StudyTypeDto.getNurseryDto());

		final MeasurementVariable seasonMeasurementVariable = MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.SEASON_MONTH.getId(),
				SeasonExpressionDataProcessorTest.SEASON_MONTH_VALUE);

		workbook.setConditions(Lists.newArrayList(seasonMeasurementVariable));

		this.seasonExpressionDataProcessor.processEnvironmentLevelData(this.advancingSource, workbook, null, null);
		Assert.assertEquals("The season should be " + SeasonExpressionDataProcessorTest.SEASON_MONTH_VALUE,
				SeasonExpressionDataProcessorTest.SEASON_MONTH_VALUE, this.advancingSource.getSeason());
	}

	@Test
	public void testProcessEnvironmentLevelDataWithSeasonVarTextVariable() {
		final Workbook workbook = this.workbookTestDataInitializer.createWorkbook(StudyTypeDto.getNurseryDto());

		final MeasurementVariable seasonMeasurementVariable = MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.SEASON_VAR_TEXT.getId(),
				SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE);

		workbook.setConditions(Lists.newArrayList(seasonMeasurementVariable));

		this.seasonExpressionDataProcessor.processEnvironmentLevelData(this.advancingSource, workbook, null, null);
		Assert.assertEquals("The season should be " + SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE,
				SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE, this.advancingSource.getSeason());
	}

	@Test
	public void testProcessEnvironmentLevelDataWithSeasonVarVariable() {
		final Workbook workbook = this.workbookTestDataInitializer.createWorkbook(StudyTypeDto.getNurseryDto());

		final MeasurementVariable seasonMeasurementVariable = MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.SEASON_VAR.getId(),
				SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE);

		workbook.setConditions(Lists.newArrayList(seasonMeasurementVariable));

		this.seasonExpressionDataProcessor.processEnvironmentLevelData(this.advancingSource, workbook, null, null);
		Assert.assertEquals("The season should be " + SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE,
				SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE, this.advancingSource.getSeason());
	}

	@Test
	public void testProcessEnvironmentLevelDataWithNumericSeasonVarVariable() {
		final Workbook workbook = this.workbookTestDataInitializer.createWorkbook(StudyTypeDto.getNurseryDto());

		final MeasurementVariable seasonMeasurementVariable = MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.SEASON_VAR.getId(),
				SeasonExpressionDataProcessorTest.SEASON_CATEGORY_ID);
		seasonMeasurementVariable.setPossibleValues(Arrays.asList(this.valueReferenceTestDataInitializer
				.createValueReference(Integer.parseInt(SEASON_CATEGORY_ID), SEASON_CATEGORY_VALUE)));

		workbook.setConditions(Lists.newArrayList(seasonMeasurementVariable));

		this.seasonExpressionDataProcessor.processEnvironmentLevelData(this.advancingSource, workbook, null, null);
		Assert.assertEquals("The season should be " + SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE,
				SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE, this.advancingSource.getSeason());
	}

	@Test
	public void testProcessEnvironmentLevelDataWithNoSeasonVariable() {
		final Workbook workbook = this.workbookTestDataInitializer.createWorkbook(StudyTypeDto.getNurseryDto());
		workbook.setConditions(new ArrayList<MeasurementVariable>());

		this.seasonExpressionDataProcessor.processEnvironmentLevelData(this.advancingSource, workbook, null, null);
		Assert.assertEquals("The season should be an empty String", SeasonExpressionDataProcessorTest.EMPTY_STRING,
				this.advancingSource.getSeason());
	}

	@Test
	public void testProcessPlotLevelDataWithSeasonMonthVariable() {
		final MeasurementVariable firstInstanceSeasonMeasurementVariable = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.SEASON_MONTH.getId(), "");
		final MeasurementData firstInstanceSeasonMeasurementData =
				this.measurementDataTestDataInitializer
						.createMeasurementData(SeasonExpressionDataProcessorTest.SEASON_MONTH_VALUE, firstInstanceSeasonMeasurementVariable);

		final MeasurementVariable firstInstanceMeasurementVariable = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "");
		final MeasurementData firstInstanceMeasurementData = this.measurementDataTestDataInitializer.createMeasurementData("1", firstInstanceMeasurementVariable);

		final MeasurementRow trialInstanceObservation = new MeasurementRow();
		trialInstanceObservation.setDataList(Lists.newArrayList(firstInstanceMeasurementData, firstInstanceSeasonMeasurementData));

		this.advancingSource.setTrailInstanceObservation(trialInstanceObservation);

		this.seasonExpressionDataProcessor.processPlotLevelData(this.advancingSource, trialInstanceObservation);
		Assert.assertEquals("The season should be " + SeasonExpressionDataProcessorTest.SEASON_MONTH_VALUE,
				SeasonExpressionDataProcessorTest.SEASON_MONTH_VALUE, this.advancingSource.getSeason());
	}

	@Test
	public void testProcessPlotLevelDataWithSeasonVarTextVariable() {
		final MeasurementVariable firstInstanceSeasonMeasurementVariable = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.SEASON_VAR_TEXT.getId(), "");
		final MeasurementData firstInstanceSeasonMeasurementData =
				this.measurementDataTestDataInitializer
						.createMeasurementData(SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE, firstInstanceSeasonMeasurementVariable);

		final MeasurementVariable firstTrialInstanceMeasurementVariable = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "");
		final MeasurementData firstTrialInstanceMeasurementData = this.measurementDataTestDataInitializer.createMeasurementData("1", firstTrialInstanceMeasurementVariable);

		final MeasurementRow trialInstanceObservation = new MeasurementRow();
		trialInstanceObservation.setDataList(Lists.newArrayList(firstTrialInstanceMeasurementData, firstInstanceSeasonMeasurementData));

		this.advancingSource.setTrailInstanceObservation(trialInstanceObservation);

		this.seasonExpressionDataProcessor.processPlotLevelData(this.advancingSource, trialInstanceObservation);
		Assert.assertEquals("The season should be " + SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE,
				SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE, this.advancingSource.getSeason());
	}

	@Test
	public void testProcessPlotLevelDataWithSeasonVarVariable() {
		final MeasurementVariable firstInstanceSeasonMeasurementVariable = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.SEASON_VAR.getId(), "");
		final MeasurementData firstInstanceSeasonMeasurementData =
				this.measurementDataTestDataInitializer
						.createMeasurementData(SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE, firstInstanceSeasonMeasurementVariable);

		final MeasurementVariable firstTrialInstanceMeasurementVariable = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "");
		final MeasurementData firstTrialInstanceMeasurementData = this.measurementDataTestDataInitializer.createMeasurementData("1", firstTrialInstanceMeasurementVariable);

		final MeasurementRow trialInstanceObservation = new MeasurementRow();
		trialInstanceObservation.setDataList(Lists.newArrayList(firstTrialInstanceMeasurementData, firstInstanceSeasonMeasurementData));

		this.advancingSource.setTrailInstanceObservation(trialInstanceObservation);

		this.seasonExpressionDataProcessor.processPlotLevelData(this.advancingSource, trialInstanceObservation);
		Assert.assertEquals("The season should be " + SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE,
				SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE, this.advancingSource.getSeason());
	}

	@Test
	public void testProcessPlotLevelDataWithNumericSeasonVarVariable() {
		final MeasurementVariable firstInstanceSeasonMeasurementVariable = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.SEASON_VAR.getId(), "");
		firstInstanceSeasonMeasurementVariable.setPossibleValues(Arrays.asList(this.valueReferenceTestDataInitializer
				.createValueReference(Integer.parseInt(SEASON_CATEGORY_ID), SEASON_CATEGORY_VALUE)));
		final MeasurementData firstInstanceSeasonMeasurementData =
				this.measurementDataTestDataInitializer
						.createMeasurementData(SeasonExpressionDataProcessorTest.SEASON_CATEGORY_ID, firstInstanceSeasonMeasurementVariable);

		final MeasurementVariable firstTrialInstanceMeasurementVariable = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "");
		final MeasurementData firstTrialInstanceMeasurementData = this.measurementDataTestDataInitializer.createMeasurementData("1", firstTrialInstanceMeasurementVariable);

		final MeasurementRow trialInstanceObservation = new MeasurementRow();
		trialInstanceObservation.setDataList(Lists.newArrayList(firstTrialInstanceMeasurementData, firstInstanceSeasonMeasurementData));

		this.advancingSource.setTrailInstanceObservation(trialInstanceObservation);

		this.seasonExpressionDataProcessor.processPlotLevelData(this.advancingSource, trialInstanceObservation);
		Assert.assertEquals("The season should be " + SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE,
				SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE, this.advancingSource.getSeason());
	}

	@Test
	public void testProcessPlotLevelDataWithNoSeasonVariable() {
		final MeasurementVariable firstTrialInstanceMeasurementVariable = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "");
		final MeasurementData firstTrialInstanceMeasurementData = this.measurementDataTestDataInitializer.createMeasurementData("1", firstTrialInstanceMeasurementVariable);

		final MeasurementRow trialInstanceObservation = new MeasurementRow();
		trialInstanceObservation.setDataList(Lists.newArrayList(firstTrialInstanceMeasurementData));

		this.advancingSource.setTrailInstanceObservation(trialInstanceObservation);

		this.seasonExpressionDataProcessor.processPlotLevelData(this.advancingSource, trialInstanceObservation);
		Assert.assertEquals("The season should be an empty String", SeasonExpressionDataProcessorTest.EMPTY_STRING,
				this.advancingSource.getSeason());
	}


	@Test
	public void testGetSeasonNameValueIsCategoricalId() {

		final List<ValueReference> possibleValues = this.createPossibleValues();

		Assert.assertEquals(SEASON_NAME_WET, this.seasonExpressionDataProcessor.getSeasonName(String.valueOf(SEASON_ID_WET), possibleValues));
		Assert.assertEquals(SEASON_NAME_DRY, this.seasonExpressionDataProcessor.getSeasonName(String.valueOf(SEASON_ID_DRY), possibleValues));

	}

	@Test
	public void testGetSeasonNameValueIsCategoricalDescription() {

		final List<ValueReference> possibleValues = this.createPossibleValues();

		Assert.assertEquals(SEASON_NAME_WET, this.seasonExpressionDataProcessor.getSeasonName(SEASON_DESCRIPTION_DRY, possibleValues));
		Assert.assertEquals(SEASON_NAME_DRY, this.seasonExpressionDataProcessor.getSeasonName(SEASON_DESCRIPTION_WET, possibleValues));

	}

	@Test
	public void testGetSeasonNamePossibleValuesIsNullOrEmpty() {

		Assert.assertEquals(SEASON_NAME_WET, this.seasonExpressionDataProcessor.getSeasonName(SEASON_NAME_WET, null));
		Assert.assertEquals(SEASON_NAME_WET, this.seasonExpressionDataProcessor.getSeasonName(SEASON_NAME_WET, new ArrayList<ValueReference>()));


	}

	private List<ValueReference> createPossibleValues() {

		final List<ValueReference> possibleValues = new ArrayList<>();
		possibleValues.add(valueReferenceTestDataInitializer.createValueReference(SEASON_ID_WET, SEASON_NAME_WET, SEASON_DESCRIPTION_DRY));
		possibleValues.add(valueReferenceTestDataInitializer.createValueReference(SEASON_ID_DRY, SEASON_NAME_DRY, SEASON_DESCRIPTION_WET));
		return possibleValues;

	}

}
