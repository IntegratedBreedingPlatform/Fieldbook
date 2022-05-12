
package com.efficio.fieldbook.web.util;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.data.initializer.DesignImportTestDataInitializer;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.Instance;
import com.google.common.base.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.data.initializer.ImportedGermplasmTestDataInitializer;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.data.initializer.MeasurementRowTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.StandardVariableTestDataInitializer;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WorkbookUtilTest {

	private static final String PROGRAM_UUID = "abcd-efg-10101";

	@Mock
	private OntologyService ontologyService;

	@Mock
	private FieldbookService fieldbookService;

	private List<ValueReference> breedingMethods;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		Mockito.when(this.ontologyService.getProperty(Matchers.anyString()))
			.thenReturn(new Property(new Term(TermId.BREEDING_METHOD_PROP.getId(), TermId.BREEDING_METHOD_PROP.name(), "definition")));
		this.breedingMethods = Arrays.asList(
			new ValueReference(123, RandomStringUtils.randomAlphabetic(20)),
			new ValueReference(125, RandomStringUtils.randomAlphabetic(20)));
		Mockito.when(this.fieldbookService.getAllBreedingMethods(true)).thenReturn(this.breedingMethods);
	}

	@Test
	public void testUpdateTrialObservations() {
		final Workbook currentWorkbook = WorkbookDataUtil.getTestWorkbookForStudy(10, 3);

		final Workbook temporaryWorkbook = WorkbookDataUtil.getTestWorkbookForStudy(10, 2);

		WorkbookUtil.updateTrialObservations(currentWorkbook, temporaryWorkbook);

		Assert.assertEquals("Expecting that the study observations of temporary workbook is copied to current workbook. ",
			currentWorkbook.getTrialObservations(), temporaryWorkbook.getTrialObservations());
	}

	@Test
	public void testResetObservationToDefaultDesign() {
		final Workbook nursery = WorkbookDataUtil.getTestWorkbook(10, new StudyTypeDto("N"));
		final List<MeasurementRow> observations = nursery.getObservations();

		DesignImportTestDataInitializer.updatePlotNoValue(observations);

		WorkbookUtil.resetObservationToDefaultDesign(observations);

		for (final MeasurementRow row : observations) {
			final List<MeasurementData> dataList = row.getDataList();
			final MeasurementData entryNoData = WorkbookUtil.retrieveMeasurementDataFromMeasurementRow(TermId.ENTRY_NO.getId(), dataList);
			final MeasurementData plotNoData = WorkbookUtil.retrieveMeasurementDataFromMeasurementRow(TermId.PLOT_NO.getId(), dataList);
			Assert.assertEquals("Expecting that the PLOT_NO value is equal to ENTRY_NO.", entryNoData.getValue(), plotNoData.getValue());
		}

	}

	@Test
	public void testFindMeasurementVariableByName() {

		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final MeasurementVariable measurementVariable1 = new MeasurementVariable();
		final String variable1 = "VARIABLE1";
		measurementVariable1.setName(variable1);
		measurementVariables.add(measurementVariable1);

		final MeasurementVariable measurementVariable2 = new MeasurementVariable();
		final String variable2 = "VARIABLE2";
		measurementVariable2.setName(variable2);
		measurementVariables.add(measurementVariable2);

		final MeasurementVariable measurementVariable3 = new MeasurementVariable();
		final String variable3 = "VARIABLE_3";
		measurementVariable3.setName(variable3);
		measurementVariables.add(measurementVariable3);

		final Optional<MeasurementVariable> result = WorkbookUtil.findMeasurementVariableByName(measurementVariables, variable1);

		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(measurementVariable1, result.get());
	}

	// TODO: Commented broken tests.
	/*@Test
	public void testAddMeasurementDataToRowsExpForVariableAddOperation() {
		final MeasurementVariable variable = MeasurementVariableTestDataInitializer.createMeasurementVariableWithOperation(
			TermId.SEED_SOURCE.getId(), TermId.SEED_SOURCE.name(), TermId.SEED_SOURCE.name(), Operation.ADD);
		final List<MeasurementVariable> variableList = Arrays.asList(variable);
		final List<MeasurementRow> observations = Arrays.asList(MeasurementRowTestDataInitializer.createMeasurementRow());
		final int previousMeasurementDataSize = observations.get(0).getDataList().size();

		final boolean isVariate = true;
		WorkbookUtil.addMeasurementDataToRowsExp(variableList, observations, isVariate, this.ontologyService, this.fieldbookService,
			WorkbookUtilTest.PROGRAM_UUID);

		final int measurementDataSize = observations.get(0).getDataList().size();
		// The SEED_SOURCE Variable should be added
		Assert.assertEquals(previousMeasurementDataSize + 1, measurementDataSize);
		final MeasurementData newMeasurementData = observations.get(0).getDataList().get(previousMeasurementDataSize);
		Assert.assertNotNull(newMeasurementData);
		Assert.assertEquals(variable.getName(), newMeasurementData.getLabel());
		Assert.assertTrue(newMeasurementData.isEditable());
		Assert.assertTrue(newMeasurementData.getValue().isEmpty());
		Assert.assertEquals(variable, newMeasurementData.getMeasurementVariable());
		Assert.assertNull(newMeasurementData.getMeasurementDataId());
		Assert.assertEquals(this.breedingMethods, variable.getPossibleValues());

		WorkbookUtil.addMeasurementDataToRowsExp(variableList, observations, isVariate, this.ontologyService, this.fieldbookService,
			WorkbookUtilTest.PROGRAM_UUID);
		// The SEED_SOURCE Variable should not be added since it's already in the data list
		Assert.assertEquals(measurementDataSize, observations.get(0).getDataList().size());
	}*/

	/*@Test
	public void testAddMeasurementDataToRowsExpForVariableUpdateOperation() {
		final MeasurementVariable variable = MeasurementVariableTestDataInitializer.createMeasurementVariableWithOperation(
			TermId.SEED_SOURCE.getId(), TermId.SEED_SOURCE.name(), TermId.SEED_SOURCE.name(), Operation.UPDATE);
		final List<MeasurementVariable> variableList = Arrays.asList(variable);
		final List<MeasurementRow> observations = Arrays.asList(MeasurementRowTestDataInitializer.createMeasurementRow());
		final int previousMeasurementDataSize = observations.get(0).getDataList().size();

		final boolean isVariate = true;
		WorkbookUtil.addMeasurementDataToRowsExp(variableList, observations, isVariate, this.ontologyService, this.fieldbookService,
			WorkbookUtilTest.PROGRAM_UUID);

		final int measurementDataSize = observations.get(0).getDataList().size();
		// The SEED_SOURCE Variable should be added
		Assert.assertEquals(previousMeasurementDataSize + 1, measurementDataSize);
		final MeasurementData newMeasurementData = observations.get(0).getDataList().get(previousMeasurementDataSize);
		Assert.assertNotNull(newMeasurementData);
		Assert.assertEquals(variable.getName(), newMeasurementData.getLabel());
		Assert.assertTrue(newMeasurementData.isEditable());
		Assert.assertTrue(newMeasurementData.getValue().isEmpty());
		Assert.assertEquals(variable, newMeasurementData.getMeasurementVariable());
		Assert.assertNull(newMeasurementData.getMeasurementDataId());
		Assert.assertEquals(this.breedingMethods, variable.getPossibleValues());
	}*/

	/*@Test
	public void testAddMeasurementDataToRowsIfNecessary() {
		final MeasurementVariable variable = MeasurementVariableTestDataInitializer.createMeasurementVariableWithOperation(
			TermId.SEED_SOURCE.getId(), TermId.SEED_SOURCE.name(), TermId.SEED_SOURCE.name(), Operation.ADD);
		final List<MeasurementVariable> variableList = Arrays.asList(variable);
		final List<MeasurementRow> observations = Arrays.asList(MeasurementRowTestDataInitializer.createMeasurementRow());
		final int previousMeasurementDataSize = observations.get(0).getDataList().size();

		final boolean isVariate = true;
		WorkbookUtil.addMeasurementDataToRowsIfNecessary(variableList, observations, isVariate, this.ontologyService, this.fieldbookService,
			WorkbookUtilTest.PROGRAM_UUID);

		final int measurementDataSize = observations.get(0).getDataList().size();
		// The SEED_SOURCE Variable should be added
		Assert.assertEquals(previousMeasurementDataSize + 1, measurementDataSize);
		final MeasurementData newMeasurementData = observations.get(0).getDataList().get(previousMeasurementDataSize);
		Assert.assertNotNull(newMeasurementData);
		Assert.assertEquals(variable.getName(), newMeasurementData.getLabel());
		Assert.assertTrue(newMeasurementData.isEditable());
		Assert.assertTrue(newMeasurementData.getValue().isEmpty());
		Assert.assertEquals(variable, newMeasurementData.getMeasurementVariable());
		Assert.assertNull(newMeasurementData.getMeasurementDataId());
		Assert.assertEquals(this.breedingMethods, variable.getPossibleValues());

		WorkbookUtil.addMeasurementDataToRowsExp(variableList, observations, isVariate, this.ontologyService, this.fieldbookService,
			WorkbookUtilTest.PROGRAM_UUID);
		// The SEED_SOURCE Variable should not be added since it's already in the data list
		Assert.assertEquals(measurementDataSize, observations.get(0).getDataList().size());
	}*/

	/*@Test
	public void testAddMeasurementDataToRowsForVariate() {
		final MeasurementVariable variable = MeasurementVariableTestDataInitializer.createMeasurementVariableWithOperation(
			TermId.SEED_SOURCE.getId(), TermId.SEED_SOURCE.name(), TermId.SEED_SOURCE.name(), Operation.ADD);
		final List<MeasurementVariable> variableList = Arrays.asList(variable);
		final List<MeasurementRow> observations = Arrays.asList(MeasurementRowTestDataInitializer.createMeasurementRow());
		final int previousMeasurementDataSize = observations.get(0).getDataList().size();
		final UserSelection userSelection = Mockito.mock(UserSelection.class);
		ImportedGermplasm importedGermplasm = ImportedGermplasmTestDataInitializer.createImportedGermplasm();
		Mockito.doReturn(observations).when(userSelection).getMeasurementRowList();

		final boolean isVariate = true;
		WorkbookUtil.addMeasurementDataToRows(variableList, isVariate, userSelection, this.ontologyService, this.fieldbookService,
			WorkbookUtilTest.PROGRAM_UUID);

		final int measurementDataSize = observations.get(0).getDataList().size();
		// The SEED_SOURCE Variable should be added
		Assert.assertEquals(previousMeasurementDataSize + 1, measurementDataSize);
		final MeasurementData newMeasurementData = observations.get(0).getDataList().get(previousMeasurementDataSize);
		Assert.assertNotNull(newMeasurementData);
		Assert.assertEquals(variable.getName(), newMeasurementData.getLabel());
		Assert.assertTrue(newMeasurementData.isEditable());
		Assert.assertTrue(newMeasurementData.getValue().isEmpty());
		Assert.assertEquals(variable, newMeasurementData.getMeasurementVariable());
		Assert.assertNull(newMeasurementData.getMeasurementDataId());
		Assert.assertEquals(this.breedingMethods, variable.getPossibleValues());
		Mockito.verify(userSelection).getMeasurementRowList();
		Mockito.verifyNoMoreInteractions(userSelection);
	}*/

	/*@Test
	public void testAddMeasurementDataToRowsForVariableUpdateOperation() {
		final MeasurementVariable variable = MeasurementVariableTestDataInitializer.createMeasurementVariableWithOperation(
			TermId.SEED_SOURCE.getId(), TermId.SEED_SOURCE.name(), TermId.SEED_SOURCE.name(), Operation.UPDATE);
		final List<MeasurementVariable> variableList = Arrays.asList(variable);
		final List<MeasurementRow> observations = Arrays.asList(MeasurementRowTestDataInitializer.createMeasurementRow());
		final int previousMeasurementDataSize = observations.get(0).getDataList().size();
		final UserSelection userSelection = Mockito.mock(UserSelection.class);
		Mockito.doReturn(observations).when(userSelection).getMeasurementRowList();

		final boolean isVariate = true;
		WorkbookUtil.addMeasurementDataToRows(variableList, isVariate, userSelection, this.ontologyService, this.fieldbookService,
			WorkbookUtilTest.PROGRAM_UUID);

		final int measurementDataSize = observations.get(0).getDataList().size();
		// The SEED_SOURCE Variable should be not added for UPDATE operation
		Assert.assertEquals(previousMeasurementDataSize, measurementDataSize);
		Mockito.verifyZeroInteractions(this.ontologyService);
		Mockito.verifyZeroInteractions(this.fieldbookService);
	}*/

	/*@Test
	public void testSetVariablePossibleValuesForVariateWithBreedingMethodProperty() {
		final MeasurementVariable variable = MeasurementVariableTestDataInitializer.createMeasurementVariableWithOperation(
			TermId.SEED_SOURCE.getId(), TermId.SEED_SOURCE.name(), TermId.SEED_SOURCE.name(), Operation.UPDATE);
		final String originalProperty = "Pórtúgêsê Própêrty";
		// Perform HTML escaping on actual property value
		variable.setProperty(HtmlUtils.htmlEscape(originalProperty));
		final StandardVariable stdVariable =
			StandardVariableTestDataInitializer.createStandardVariable(TermId.SEED_SOURCE.getId(), TermId.SEED_SOURCE.name());

		WorkbookUtil.setVariablePossibleValues(true, this.ontologyService, this.fieldbookService, variable,	stdVariable);
		Assert.assertEquals(this.breedingMethods, variable.getPossibleValues());
		// Verify that HTML unescape was performed on property before querying ontology service
		Mockito.verify(this.ontologyService).getProperty(originalProperty);
	}*/

	/*@Test
	public void testSetVariablePossibleValuesForVariateNonBreedingMethodProperty() {
		final MeasurementVariable variable = MeasurementVariableTestDataInitializer.createMeasurementVariableWithOperation(
			TermId.SEED_SOURCE.getId(), TermId.SEED_SOURCE.name(), TermId.SEED_SOURCE.name(), Operation.UPDATE);
		final StandardVariable stdVariable =
			StandardVariableTestDataInitializer.createStandardVariable(TermId.SEED_SOURCE.getId(), TermId.SEED_SOURCE.name());
		Mockito.when(this.ontologyService.getProperty(Matchers.anyString()))
			.thenReturn(new Property(new Term(TermId.SEED_SOURCE.getId(), TermId.SEED_SOURCE.name(), "definition")));

		WorkbookUtil.setVariablePossibleValues(true, this.ontologyService, this.fieldbookService,variable, stdVariable);
		Assert.assertEquals(WorkbookUtil.transformPossibleValues(stdVariable.getEnumerations()), variable.getPossibleValues());
	}*/

	/*@Test
	public void testSetVariablePossibleValuesForFactor() {
		final MeasurementVariable variable = MeasurementVariableTestDataInitializer.createMeasurementVariableWithOperation(
			TermId.SEED_SOURCE.getId(), TermId.SEED_SOURCE.name(), TermId.SEED_SOURCE.name(), Operation.UPDATE);
		final StandardVariable stdVariable =
			StandardVariableTestDataInitializer.createStandardVariable(TermId.SEED_SOURCE.getId(), TermId.SEED_SOURCE.name());

		WorkbookUtil.setVariablePossibleValues(false, this.ontologyService, this.fieldbookService, variable, stdVariable);
		Assert.assertEquals(WorkbookUtil.transformPossibleValues(stdVariable.getEnumerations()), variable.getPossibleValues());
	}*/

	@Test
	public void testGetVariatesMapUsedInFormulas() {
		final FormulaDto formula = new FormulaDto();
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final List<FormulaVariable> inputs = new ArrayList<>();

		final MeasurementVariable measurementVariable1 = new MeasurementVariable();
		final String variable1 = "VARIABLE1";
		measurementVariable1.setName(variable1);
		measurementVariable1.setTermId(1);
		measurementVariables.add(measurementVariable1);

		final MeasurementVariable measurementVariable2 = new MeasurementVariable();
		final String variable2 = "VARIABLE2";
		measurementVariable2.setName(variable2);
		measurementVariable2.setTermId(2);
		measurementVariables.add(measurementVariable2);

		final FormulaVariable formulaVariable = new FormulaVariable(1, measurementVariable1.getName(), measurementVariable1.getTermId());
		inputs.add(formulaVariable);
		formula.setInputs(inputs);
		measurementVariable2.setFormula(formula);

		final Map<MeasurementVariable, List<MeasurementVariable>> variatesMapUsedInFormulas =
			WorkbookUtil.getVariatesMapUsedInFormulas(measurementVariables);
		Assert.assertEquals(1, variatesMapUsedInFormulas.size());
		Assert.assertTrue(variatesMapUsedInFormulas.containsKey(measurementVariable1));
		final List<MeasurementVariable> measurementVariableList = variatesMapUsedInFormulas.get(measurementVariable1);
		Assert.assertEquals(measurementVariable2, measurementVariableList.get(0));
	}

	@Test
	public void testGetVariatesUsedInFormulas() {
		final FormulaDto formula = new FormulaDto();
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final List<FormulaVariable> inputs = new ArrayList<>();

		final MeasurementVariable measurementVariable1 = new MeasurementVariable();
		final String variable1 = "VARIABLE1";
		measurementVariable1.setName(variable1);
		measurementVariable1.setTermId(1);
		measurementVariables.add(measurementVariable1);

		final MeasurementVariable measurementVariable2 = new MeasurementVariable();
		final String variable2 = "VARIABLE2";
		measurementVariable2.setName(variable2);
		measurementVariable2.setTermId(2);
		measurementVariables.add(measurementVariable2);

		final FormulaVariable formulaVariable = new FormulaVariable(1, measurementVariable1.getName(), measurementVariable1.getTermId());
		inputs.add(formulaVariable);
		formula.setInputs(inputs);
		measurementVariable2.setFormula(formula);

		final Map<Integer, List<Integer>> variatesMapUsedInFormulas = WorkbookUtil.getVariatesUsedInFormulas(measurementVariables);
		Assert.assertEquals(1, variatesMapUsedInFormulas.size() );
		Assert.assertTrue(variatesMapUsedInFormulas.containsKey(measurementVariable1.getTermId()));
		final List<Integer> measurementVariableList = variatesMapUsedInFormulas.get(measurementVariable1.getTermId());
		Assert.assertEquals(measurementVariable2.getTermId(), measurementVariableList.get(0).intValue());

	}

	@Test
	public void testCreateMeasurementRowsFromEnvironments() {
		final MeasurementVariable variable =
			MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.PLOT_CODE.getId(), TermId.PLOT_CODE.name(), null);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		final Instance instance = new Instance();
		instance.setStockId(Long.parseLong(RandomStringUtils.randomNumeric(5)));
		instance.setExperimentId(Integer.parseInt(RandomStringUtils.randomNumeric(5)));
		instance.setInstanceId(Long.parseLong(RandomStringUtils.randomNumeric(5)));
		final List<MeasurementRow> row = WorkbookUtil
			.createMeasurementRowsFromEnvironments(Arrays.asList(instance), Arrays.asList(variable), new ExpDesignParameterUi());
		Assert.assertNotNull(row);
		Assert.assertEquals(instance.getExperimentId(), row.get(0).getExperimentId());
		Assert.assertEquals(instance.getStockId(), row.get(0).getStockId());
	}
}
