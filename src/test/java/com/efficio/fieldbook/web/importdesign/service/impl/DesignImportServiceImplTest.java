package com.efficio.fieldbook.web.importdesign.service.impl;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.data.initializer.DesignImportTestDataInitializer;
import com.efficio.fieldbook.web.data.initializer.ImportedGermplasmMainInfoInitializer;
import com.efficio.fieldbook.web.importdesign.generator.DesignImportMeasurementRowGenerator;
import com.efficio.fieldbook.web.trial.bean.EnvironmentData;
import com.efficio.fieldbook.web.util.parsing.DesignImportCsvParser;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import junit.framework.Assert;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.StandardVariableTestDataInitializer;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("deprecation")
@RunWith(MockitoJUnitRunner.class)
public class DesignImportServiceImplTest {

	private static final int CHALK_PCT_TERMID = 22768;

	private static final int GYLD_TERMID = 18000;

	private static final String PROGRAM_UUID = "789c6438-5a94-11e5-885d-feff819cdc9f";

	@Mock
	private DesignImportCsvParser designImportParser;

	@Mock
	private FieldbookService fieldbookService;

	@Mock
	private OntologyService ontologyService;

	@Mock
	private OntologyScaleDataManager ontologyScaleDataManager;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private MessageSource messageSource;

	@Mock
	private MockMultipartFile multiPartFile;

	@Mock
	private UserSelection userSelection;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private DesignImportMeasurementRowGenerator measurementRowGenerator;

	private DesignImportData designImportData;

	@InjectMocks
	private DesignImportServiceImpl service;

	@Before
	public void setUp() {
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(DesignImportServiceImplTest.PROGRAM_UUID);

		this.initializeOntologyScaleDataManager();
		this.initializeOntologyService();
		this.initializeDesignImportData();
		this.initializeGermplasmList();

	}

	@Test
	public void testAreTrialInstancesMatchTheSelectedEnvironments() throws DesignValidationException {

		Assert.assertFalse("Should be false because the no of environments in studies don't match the no of studies in the design file",
				this.service.areTrialInstancesMatchTheSelectedEnvironments(1, this.designImportData));
		Assert.assertTrue("Should be true because the no of environments in studies match the no of studies in the design file",
				this.service.areTrialInstancesMatchTheSelectedEnvironments(3, this.designImportData));
	}

	@Test
	public void testCategorizeHeadersByPhenotype() {

		final Map<PhenotypicType, List<DesignHeaderItem>> result = this.service.categorizeHeadersByPhenotype(this.createUnmappedHeaders());

		Assert.assertEquals("Total No of TRIAL in file is 2", 2, result.get(PhenotypicType.TRIAL_ENVIRONMENT).size());
		Assert.assertEquals("Total No of GERMPLASM FACTOR in file is 1", 1, result.get(PhenotypicType.GERMPLASM).size());
		Assert.assertEquals("Total No of DESIGN FACTOR in file is 3", 3, result.get(PhenotypicType.TRIAL_DESIGN).size());
		Assert.assertEquals("Total No of VARIATE in file is 0", 0, result.get(PhenotypicType.VARIATE).size());

	}

	@Test
	public void testCategorizeHeadersByPhenotypeIfCaseInsensitive() {

		final Map<PhenotypicType, List<DesignHeaderItem>> result =
				this.service.categorizeHeadersByPhenotype(this.createUnmappedHeadersWithWrongCase());

		Assert.assertEquals("Total No of TRIAL in file is 2", 2, result.get(PhenotypicType.TRIAL_ENVIRONMENT).size());
		Assert.assertEquals("Total No of GERMPLASM FACTOR in file is 1", 1, result.get(PhenotypicType.GERMPLASM).size());
		Assert.assertEquals("Total No of DESIGN FACTOR in file is 3", 3, result.get(PhenotypicType.TRIAL_DESIGN).size());
		Assert.assertEquals("Total No of VARIATE in file is 0", 0, result.get(PhenotypicType.VARIATE).size());

	}

	@Test
	public void testConvertToStandardVariables() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(10, 3);

		final Map<Integer, StandardVariable> result =
				this.service.convertToStandardVariables(workbook.getGermplasmFactors(), PhenotypicType.GERMPLASM);

		Assert.assertEquals(
				"The number of converted standard variables must be equal to the number of germplasm factors from the workbook.",
				result.size(), workbook.getGermplasmFactors().size());

		for (final MeasurementVariable measurementVar : workbook.getFactors()) {
			final StandardVariable stdVar = result.get(measurementVar.getTermId());
			if (stdVar != null) {
				Assert.assertEquals("The standard variable id must be equal to the measurement variable id.", stdVar.getId(),
					measurementVar.getTermId());
				Assert.assertTrue("The standard variable phenotypic type must be equal to the measurement variable label.",
						stdVar.getPhenotypicType().getLabelList().contains(measurementVar.getLabel()));
			}
		}
	}

	@Test
	public void testExtractTrialInstancesFromEnvironmentData() {
		final int expectedNumberOfTrialInstances = 5;
		final EnvironmentData environmentData = DesignImportTestDataInitializer.createEnvironmentData(expectedNumberOfTrialInstances);
		DesignImportTestDataInitializer.processEnvironmentData(environmentData);

		final Set<String> result = this.service.extractTrialInstancesFromEnvironmentData(environmentData);

		final int actualNoOfExtractedTrialInstances = result.size();
		Assert.assertEquals("The number of extracted trial instances must be equal to " + expectedNumberOfTrialInstances + " but returned "
				+ actualNoOfExtractedTrialInstances, expectedNumberOfTrialInstances, actualNoOfExtractedTrialInstances);
	}

	@Test
	public void testGenerateDesignForOneInstanceOnly() throws DesignValidationException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(10, 3);

		Mockito.doReturn(ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo()).when(this.userSelection)
				.getImportedGermplasmMainInfo();

		// Make sure that there is only 1 trial instance included in the selected environments from the UI
		final EnvironmentData environmentData = DesignImportTestDataInitializer.createEnvironmentData(1);

		DesignImportTestDataInitializer.processEnvironmentData(environmentData);

		final List<MeasurementRow> measurements =
				this.service.generateDesign(workbook, this.designImportData, environmentData, true, this.createAdditionalParamsMap(1, 1));

		Assert.assertEquals(
				"The first trial instance must only have " + DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES + " observations.",
				DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES, measurements.size());

	}

	@Test
	public void testGenerateDesignWithCustomPlotNo() throws DesignValidationException {

		int startingEntryNo = 1;
		int startingPlotNo = 12;

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(10, 3);

		Mockito.doReturn(ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo()).when(this.userSelection)
				.getImportedGermplasmMainInfo();

		final EnvironmentData environmentData = DesignImportTestDataInitializer.createEnvironmentData(1);

		DesignImportTestDataInitializer.processEnvironmentData(environmentData);

		final DesignImportData designImportData = DesignImportTestDataInitializer.createDesignImportData(startingEntryNo, startingPlotNo);

		final List<MeasurementRow> measurements = this.service.generateDesign(workbook, designImportData, environmentData, true,
				this.createAdditionalParamsMap(startingEntryNo, startingPlotNo));

		Assert.assertEquals(
				"The first trial instance must only have " + DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES + " observations.",
				DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES, measurements.size());

		// Verify that PLOT_NO column in measurement rows starts with the specified starting plotNo
		for (final MeasurementRow measurementRow : measurements) {
			Assert.assertEquals(String.valueOf(startingPlotNo++), measurementRow.getMeasurementDataValue(TermId.PLOT_NO.getId()));
		}

		// Verify that the ENTRY_NO column in measurement rows starts with the specified starting entry no
		for (final MeasurementRow measurementRow : measurements) {
			Assert.assertEquals(String.valueOf(startingEntryNo++), measurementRow.getMeasurementDataValue(TermId.ENTRY_NO.getId()));
		}

	}

	@Test
	public void testGenerateDesignWithCustomEntryNo() throws DesignValidationException {

		int startingEntryNo = 10;
		int startingPlotNo = 1;

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(10, 3);

		final ImportedGermplasmMainInfo importedGermplasmMainInfo =
				ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo(startingEntryNo);

		Mockito.doReturn(importedGermplasmMainInfo).when(this.userSelection).getImportedGermplasmMainInfo();

		final EnvironmentData environmentData = DesignImportTestDataInitializer.createEnvironmentData(1);

		DesignImportTestDataInitializer.processEnvironmentData(environmentData);

		final DesignImportData designImportData = DesignImportTestDataInitializer.createDesignImportData(startingEntryNo, startingPlotNo);

		final List<MeasurementRow> measurements = this.service.generateDesign(workbook, designImportData, environmentData, true,
				this.createAdditionalParamsMap(startingEntryNo, startingPlotNo));

		Assert.assertEquals(
				"The first trial instance must only have " + DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES + " observations.",
				DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES, measurements.size());

		// Verify that the PLOT_NO column in measurement rows starts with the specified starting plotNo
		for (final MeasurementRow measurementRow : measurements) {
			Assert.assertEquals(String.valueOf(startingPlotNo++), measurementRow.getMeasurementDataValue(TermId.PLOT_NO.getId()));
		}

		// Verify that the ENTRY_NO column in measurement rows starts with the specified starting entry no
		for (final MeasurementRow measurementRow : measurements) {
			Assert.assertEquals(String.valueOf(startingEntryNo++), measurementRow.getMeasurementDataValue(TermId.ENTRY_NO.getId()));
		}

	}

	@Test
	public void testGenerateDesignForThreeInstances() throws DesignValidationException {

		final int noOfTrialInstances = 3;
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(10, noOfTrialInstances);

		Mockito.doReturn(ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo()).when(this.userSelection)
				.getImportedGermplasmMainInfo();

		// Make sure that there is only 3 trial instances included in the selected environments from the UI
		final EnvironmentData environmentData = DesignImportTestDataInitializer.createEnvironmentData(noOfTrialInstances);

		DesignImportTestDataInitializer.processEnvironmentData(environmentData);

		final List<MeasurementRow> measurements =
				this.service.generateDesign(workbook, this.designImportData, environmentData, true, this.createAdditionalParamsMap(1, 1));

		// Not including the header row from the count of number of rows from the csv file
		final int expectedNumberOfMeasurements = this.designImportData.getRowDataMap().size() - 1;
		Assert.assertEquals("Expecting to return " + expectedNumberOfMeasurements + " observations for " + noOfTrialInstances
				+ " trial instances but didn't. ", expectedNumberOfMeasurements, measurements.size());

	}

	private Map<String, Integer> createAdditionalParamsMap(final Integer startingEntryNo, final Integer startingPlotNo) {
		final Map<String, Integer> additionalParams = new HashMap<String, Integer>();
		additionalParams.put("startingEntryNo", startingEntryNo);
		additionalParams.put("startingPlotNo", startingPlotNo);
		return additionalParams;
	}

	@Test
	public void testGenerateDesignForNursery() throws DesignValidationException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(5, StudyTypeDto.getNurseryDto());

		// Setting to 1 since there is only 1 environment for nursery
		final EnvironmentData environmentData = DesignImportTestDataInitializer.createEnvironmentData(1);
		DesignImportTestDataInitializer.processEnvironmentData(environmentData);

		final List<MeasurementRow> measurements =
				this.service.generateDesign(workbook, this.designImportData, environmentData, true, this.createAdditionalParamsMap(1, 1));

		Assert.assertEquals("Expecting that the number of measurement rows generated for nursery is "
						+ DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES + " observations but returned " + measurements.size() + " instead.",
				DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES, measurements.size());

	}

	@Test
	public void testGetDesignMeasurementVariables() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(10, 3);
		final Set<MeasurementVariable> result = this.service.getDesignMeasurementVariables(workbook, this.designImportData, true);

		// retrieve no of variables imported from the csv file without TermId.TRIAL_INSTANCE_FACTOR.getId()
		final int noOfVariablesFromCSVFile = this.designImportData.getRowDataMap().get(0).size();
		final int expectedNoOfVariablesUsed =
				noOfVariablesFromCSVFile + workbook.getFactors().size() + workbook.getVariates().size();
		Assert.assertEquals("The total number of variables to use for generating design from workbook and csv file must be equal to "
				+ expectedNoOfVariablesUsed + " but returned " + result.size() + " instead.", expectedNoOfVariablesUsed, result.size());
	}

	/**
	 * NOTE: PREVIEW mode is the table shown in the REVIEW DESIGN DETAILS modal before clicking the finish button in Import Design feature
	 */
	@Test
	public void testGetDesignMeasurementVariablesNotPreview() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(10, 3);

		final Set<MeasurementVariable> result = this.service.getDesignMeasurementVariables(workbook, this.designImportData, false);

		// If NOT in PREVIEW mode, the method will remove the trial environment factors in the list except for trial instance. This is
		// because the actual measurements/observations that will be generated from import should not contain study environment factors.
		Assert.assertEquals("The total number of Factors and Variates (less the trial environments) in workbook is 17", 17, result.size());
	}

	/**
	 * Extracts Study design variables as list of Measurement Variable. The list of trial design variables from the csv file are the
	 * following: PLOT_NO, ENTRY_NO, BLOCK_NO
	 */
	@Test
	public void testGetDesignRequiredMeasurementVariable() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(10, 3);

		final int expectedNoOfStudyDesignVariablesFromCSV =
				this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN).size();
		final Set<MeasurementVariable> result = this.service.getDesignRequiredMeasurementVariable(workbook, this.designImportData);

		Assert.assertEquals(
				"The total number of Study Design Factors is " + expectedNoOfStudyDesignVariablesFromCSV + " but returned " + result.size()
						+ " instead.", expectedNoOfStudyDesignVariablesFromCSV, result.size());
	}

	/**
	 * Extracts Study design variables as list of Standard Variable. The list of trial design variables from the csv file are the following:
	 * PLOT_NO, ENTRY_NO, BLOCK_NO
	 */
	@Test
	public void testGetDesignRequiredStandardVariables() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(10, 3);

		final int expectedNoOfStudyDesignVariablesFromCSV =
				this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN).size();
		final Set<StandardVariable> result = this.service.getDesignRequiredStandardVariables(workbook, this.designImportData);

		Assert.assertEquals(
				"The total number of Study Design Factors is " + expectedNoOfStudyDesignVariablesFromCSV + " but returned " + result.size()
						+ " instead.", expectedNoOfStudyDesignVariablesFromCSV, result.size());

	}

	@Test
	public void testGroupCsvRowsIntoTrialInstance() throws DesignValidationException {

		final DesignHeaderItem trialInstanceHeaderItem = this.service.validateIfStandardVariableExists(
				this.designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId().get(PhenotypicType.TRIAL_ENVIRONMENT),
				"design.import.error.study.is.required", TermId.TRIAL_INSTANCE_FACTOR);

		final Map<String, Map<Integer, List<String>>> result =
				this.service.groupCsvRowsIntoTrialInstance(trialInstanceHeaderItem, this.designImportData.getRowDataMap());

		Assert.assertEquals("The total number of trial instances in file is 3", 3, result.size());
		Assert.assertEquals("1st trial instance in file has " + DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES + " observations",
				DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES, result.get("1").size());
		Assert.assertEquals("2nd trial instance in file has " + DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES + " observations",
				DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES, result.get("2").size());
		Assert.assertEquals("Each trial instance in file has " + DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES + " observations",
				DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES, result.get("3").size());

	}

	@Test
	public void testGetMeasurementVariablesFromDataFile() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(10, 3);

		final int expectedNoOfMeasurementVariablesFromCSV = this.designImportData.getRowDataMap().get(0).size();
		final Set<MeasurementVariable> returnValue = this.service.getMeasurementVariablesFromDataFile(workbook, this.designImportData);

		Assert.assertEquals("The csv file only contains " + expectedNoOfMeasurementVariablesFromCSV + " Measurement Variables",
				expectedNoOfMeasurementVariablesFromCSV, returnValue.size());

	}

	@Test
	public void testValidateIfStandardVariableExists() {
		try {

			this.service.validateIfStandardVariableExists(
					this.designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId().get(PhenotypicType.TRIAL_ENVIRONMENT),
					"design.import.error.study.is.required", TermId.TRIAL_INSTANCE_FACTOR);

		} catch (final DesignValidationException e) {

			Assert.fail("The logic did not detect that the study number exist");

		}

	}

	@Test(expected = DesignValidationException.class)
	public void testValidateIfStandardVariableExistsTrialInstanceDoNotExist() throws DesignValidationException {
		this.service.validateIfStandardVariableExists(
				this.designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId().get(PhenotypicType.GERMPLASM),
				"design.import.error.study.is.required", TermId.TRIAL_INSTANCE_FACTOR);

		Assert.fail("The logic should detect that the study number exist");
	}

	/**
	 * This method test the following scenarios: 1. If the number of measurement rows created are the same with the number of rows imported
	 * from the file 2. If the starting plot no is applied properly for each measurement row created
	 * <p/>
	 * This service method updates the measurement rows.
	 * <p/>
	 * NOTE: Trial Instance = Environment
	 */
	@Test
	public void testCreatePresetMeasurementRowsPerInstance() {
		final Map<Integer, List<String>> csvData = this.designImportData.getRowDataMap();
		final List<MeasurementRow> measurements = new ArrayList<MeasurementRow>();
		final DesignImportMeasurementRowGenerator measurementRowGenerator = this.generateMeasurementRowGenerator();
		final int trialInstanceNo = 1;

		this.service.createMeasurementRowsPerInstance(csvData, measurements, measurementRowGenerator, trialInstanceNo);

		Assert.assertEquals("The number of measurement rows from the csv file must be equal to the number of measurements row generated.",
				csvData.size() - 1, measurements.size());

		// SITE_NAME must not be counted for the expected columns imported from the custom import file
		final Integer expectedColumnNo = csvData.get(0).size() - 1;
		Assert.assertEquals(
				"The number of columns from the csv file must be equal to the number of measurements data per measurement row generated.",
				expectedColumnNo.intValue(), measurements.get(0).getDataList().size());

		// find the index of PLOT_NO column from the import file
		final int plotNoIndxCSV =
				this.designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId().get(PhenotypicType.TRIAL_DESIGN)
						.get(TermId.PLOT_NO.getId()).getColumnIndex();

		// Verify if the plot no is increased per each measurement rows based on the stated Starting Plot No
		for (int i = 0; i < measurements.size(); i++) {
			final List<String> rowCSV = csvData.get(i + 1);
			final int plotNoCsv = Integer.valueOf(rowCSV.get(plotNoIndxCSV));

			final Map<Integer, MeasurementData> dataListMap = this.service.getMeasurementDataMap(measurements.get(i).getDataList());
			final int plotNoActual = Integer.valueOf(dataListMap.get(TermId.PLOT_NO.getId()).getValue());

			Assert.assertEquals("Expecting that the generated value for plot no is increased based on the stated starting plot no.",
					plotNoCsv, plotNoActual);
		}
	}

	private DesignImportMeasurementRowGenerator generateMeasurementRowGenerator() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(6, 3);
		final Map<PhenotypicType, Map<Integer, DesignHeaderItem>> mappedHeadersWithStdVarId =
				this.designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId();

		final Map<Integer, ImportedGermplasm> importedGermplasm =
				Maps.uniqueIndex(ImportedGermplasmMainInfoInitializer.createImportedGermplasmList(),
						new Function<ImportedGermplasm, Integer>() {

							@Override
							public Integer apply(final ImportedGermplasm input) {
								return input.getEntryId();
							}
						});

		final Map<Integer, StandardVariable> germplasmStandardVariables = new HashMap<Integer, StandardVariable>();
		germplasmStandardVariables.put(TermId.ENTRY_NO.getId(),
				StandardVariableTestDataInitializer.createStandardVariable(TermId.ENTRY_NO.getId(), TermId.ENTRY_NO.name()));
		final Set<String> trialInstancesFromUI = new HashSet<String>();
		trialInstancesFromUI.add("1");
		trialInstancesFromUI.add("2");
		trialInstancesFromUI.add("3");
		final boolean isPreview = false;
		final Map<String, Integer> availableCheckTypes = new HashMap<String, Integer>();
		final DesignImportMeasurementRowGenerator measurementRowGenerator =
				new DesignImportMeasurementRowGenerator(this.fieldbookService, workbook, mappedHeadersWithStdVarId, importedGermplasm,
						germplasmStandardVariables, trialInstancesFromUI, isPreview, availableCheckTypes);
		return measurementRowGenerator;
	}

	private void initializeDesignImportData() {

		try {
			Mockito.when(this.designImportParser.parseFile(this.multiPartFile))
					.thenReturn(DesignImportTestDataInitializer.createDesignImportData(1, 1));
			this.designImportData = this.designImportParser.parseFile(this.multiPartFile);
		} catch (final FileParsingException e) {
			Assert.fail("Failed to create DesignImportData");
		}

	}

	private void initializeOntologyScaleDataManager() {
		final Scale scale = new Scale();
		scale.setMaxValue("1");
		scale.setMinValue("100");
	}

	@SuppressWarnings({"unchecked"})
	private void initializeOntologyService() {

		Mockito.doReturn(DesignImportTestDataInitializer
				.createStandardVariable(PhenotypicType.GERMPLASM, TermId.ENTRY_NO.getId(), "ENTRY_NO", "", "", "",
						DesignImportTestDataInitializer.NUMERIC_VARIABLE, "N", "", "")).when(this.ontologyService)
				.getStandardVariable(TermId.ENTRY_NO.getId(), DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(DesignImportTestDataInitializer
				.createStandardVariable(PhenotypicType.GERMPLASM, TermId.GID.getId(), "GID", "", "", "",
						DesignImportTestDataInitializer.NUMERIC_VARIABLE, "N", "", "")).when(this.ontologyService)
				.getStandardVariable(TermId.GID.getId(), DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(DesignImportTestDataInitializer
				.createStandardVariable(PhenotypicType.GERMPLASM, TermId.DESIG.getId(), "DESIG", "", "", "",
						DesignImportTestDataInitializer.CHARACTER_VARIABLE, "C", "", "")).when(this.ontologyService)
				.getStandardVariable(TermId.DESIG.getId(), DesignImportServiceImplTest.PROGRAM_UUID);

		Mockito.doReturn(DesignImportTestDataInitializer
				.createStandardVariable(PhenotypicType.GERMPLASM, TermId.OBS_UNIT_ID.getId(), "OBS_UNIT_ID", "", "", "",
						DesignImportTestDataInitializer.CHARACTER_VARIABLE, "C", "", "")).when(this.ontologyService)
				.getStandardVariable(TermId.OBS_UNIT_ID.getId(), DesignImportServiceImplTest.PROGRAM_UUID);

		Mockito.doReturn(DesignImportTestDataInitializer
				.createStandardVariable(PhenotypicType.GERMPLASM, TermId.CROSS.getId(), "CROSS", "", "", "",
						DesignImportTestDataInitializer.CHARACTER_VARIABLE, "C", "", "")).when(this.ontologyService)
				.getStandardVariable(TermId.CROSS.getId(), DesignImportServiceImplTest.PROGRAM_UUID);
		Mockito.doReturn(DesignImportTestDataInitializer
				.createStandardVariable(PhenotypicType.GERMPLASM, TermId.SEED_SOURCE.getId(), "SEED_SOURCE", "", "", "",
						DesignImportTestDataInitializer.CHARACTER_VARIABLE, "C", "", "")).when(this.ontologyService)
				.getStandardVariable(TermId.SEED_SOURCE.getId(), DesignImportServiceImplTest.PROGRAM_UUID);

		final Property prop = new Property();
		final Term term = new Term();
		term.setId(0);
		prop.setTerm(term);

		final Map<String, List<StandardVariable>> map = new HashMap<>();

		map.put("TRIAL_INSTANCE", this.createList(DesignImportTestDataInitializer
				.createStandardVariable(VariableType.ENVIRONMENT_DETAIL, TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL_INSTANCE", "", "", "",
						"", "", "")));
		map.put("SITE_NAME", this.createList(DesignImportTestDataInitializer
				.createStandardVariable(VariableType.ENVIRONMENT_DETAIL, StudyTypeDto.getTrialDto().getId(), "SITE_NAME", "", "", "", "", "", "")));
		map.put("ENTRY_NO", this.createList(DesignImportTestDataInitializer
				.createStandardVariable(VariableType.GERMPLASM_DESCRIPTOR, TermId.ENTRY_NO.getId(), "ENTRY_NO", "", "", "", "", "", "")));
		map.put("PLOT_NO", this.createList(DesignImportTestDataInitializer
				.createStandardVariable(VariableType.EXPERIMENTAL_DESIGN, TermId.PLOT_NO.getId(), "PLOT_NO", "", "", "", "", "", "")));
		map.put("REP_NO", this.createList(DesignImportTestDataInitializer
				.createStandardVariable(VariableType.EXPERIMENTAL_DESIGN, TermId.REP_NO.getId(), "REP_NO", "", "", "", "", "", "")));
		map.put("BLOCK_NO", this.createList(DesignImportTestDataInitializer
				.createStandardVariable(VariableType.EXPERIMENTAL_DESIGN, TermId.BLOCK_NO.getId(), "BLOCK_NO", "", "", "", "", "", "")));

		Mockito.doReturn(map).when(this.ontologyDataManager).getStandardVariablesInProjects(Matchers.anyListOf(String.class), Matchers.anyString());

	}

	private void initializeGermplasmList() {

		final ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		final ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
		importedGermplasmList.setImportedGermplasms(ImportedGermplasmMainInfoInitializer.createImportedGermplasmList());
		mainInfo.setImportedGermplasmList(importedGermplasmList);

		Mockito.doReturn(mainInfo).when(this.userSelection).getImportedGermplasmMainInfo();

	}

	private List<StandardVariable> createList(final StandardVariable... stdVar) {
		final List<StandardVariable> stdVarList = new ArrayList<>();
		for (final StandardVariable var : stdVar) {
			stdVarList.add(var);
		}
		return stdVarList;

	}

	private List<DesignHeaderItem> createUnmappedHeaders() {
		final List<DesignHeaderItem> items = new ArrayList<>();

		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL_INSTANCE", 0));
		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(TermId.SITE_NAME.getId(), "SITE_NAME", 1));
		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(TermId.ENTRY_NO.getId(), "ENTRY_NO", 2));
		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(TermId.PLOT_NO.getId(), "PLOT_NO", 3));
		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(TermId.REP_NO.getId(), "REP_NO", 4));
		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(TermId.BLOCK_NO.getId(), "BLOCK_NO", 5));
		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(DesignImportTestDataInitializer.AFLAVER_5_ID, "AflavER_1_5", 6));

		return items;
	}

	private List<DesignHeaderItem> createUnmappedHeadersWithWrongCase() {
		final List<DesignHeaderItem> items = new ArrayList<>();

		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(TermId.TRIAL_INSTANCE_FACTOR.getId(), "TriAL_iNSTANCE", 0));
		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(TermId.SITE_NAME.getId(), "SiTe_NaME", 1));
		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(TermId.ENTRY_NO.getId(), "ENtRY_nO", 2));
		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(TermId.PLOT_NO.getId(), "PLoT_NO", 3));
		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(TermId.REP_NO.getId(), "ReP_nO", 4));
		items.add(DesignImportTestDataInitializer.createDesignHeaderItem(TermId.BLOCK_NO.getId(), "BLoCK_nO", 5));

		return items;
	}

}
