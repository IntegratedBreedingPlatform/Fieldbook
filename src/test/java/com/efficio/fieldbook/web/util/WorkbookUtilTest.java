
package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.generationcp.commons.data.initializer.ImportedGermplasmTestDataInitializer;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.middleware.data.initializer.MeasurementRowTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.StandardVariableTestDataInitializer;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.junit.Test;
import org.mockito.Mockito;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.data.initializer.DesignImportTestDataInitializer;
import com.google.common.base.Optional;

import junit.framework.Assert;

public class WorkbookUtilTest {

	@Test
	public void testUpdateTrialObservations() {
		final Workbook currentWorkbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final Workbook temporaryWorkbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 2);

		WorkbookUtil.updateTrialObservations(currentWorkbook, temporaryWorkbook);

		Assert.assertEquals("Expecting that the trial observations of temporary workbook is copied to current workbook. ",
				currentWorkbook.getTrialObservations(), temporaryWorkbook.getTrialObservations());
	}

	@Test
	public void testResetObservationToDefaultDesign() {
		final Workbook nursery = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
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

		List<MeasurementVariable> measurementVariables = new ArrayList<>();
		MeasurementVariable measurementVariable1 = new MeasurementVariable();
		final String variable1 = "VARIABLE1";
		measurementVariable1.setName(variable1);
		measurementVariables.add(measurementVariable1);

		MeasurementVariable measurementVariable2 = new MeasurementVariable();
		final String variable2 = "VARIABLE2";
		measurementVariable2.setName(variable2);
		measurementVariables.add(measurementVariable2);

		MeasurementVariable measurementVariable3 = new MeasurementVariable();
		final String variable3 = "VARIABLE_3";
		measurementVariable3.setName(variable3);
		measurementVariables.add(measurementVariable3);

		Optional<MeasurementVariable> result = WorkbookUtil.findMeasurementVariableByName(measurementVariables, variable1);

		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(measurementVariable1, result.get());
	}
	
	@Test
	public void testAddFactorsToMeasurementRowDataList() {
		MeasurementRow row = MeasurementRowTestDataInitializer.createMeasurementRow();
		StandardVariable stdVariable = StandardVariableTestDataInitializer.createStandardVariable(TermId.PLOT_CODE.getId(), TermId.PLOT_CODE.name());
		MeasurementVariable variable = MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.PLOT_CODE.getId(), TermId.PLOT_CODE.name(), null);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		UserSelection userSelection = Mockito.mock(UserSelection.class);
		this.setUpUserSelection(userSelection);
		WorkbookUtil.addFactorsToMeasurementRowDataList(row, stdVariable, true, variable, userSelection);
		Assert.assertEquals("", row.getDataList().get(4).getValue());
	}
	
	@Test
	public void testAddFactorsToMeasurementRowDataListForGroupGID() {
		MeasurementRow row = MeasurementRowTestDataInitializer.createMeasurementRow();
		StandardVariable stdVariable = StandardVariableTestDataInitializer.createStandardVariable(TermId.GROUPGID.getId(), TermId.GROUPGID.name());
		MeasurementVariable variable = MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.GROUPGID.getId(), TermId.GROUPGID.name(), null);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		UserSelection userSelection = Mockito.mock(UserSelection.class);
		ImportedGermplasm importedGermplasm = this.setUpUserSelection(userSelection);
		WorkbookUtil.addFactorsToMeasurementRowDataList(row, stdVariable, true, variable, userSelection);
		Assert.assertEquals(importedGermplasm.getGroupId().toString(), row.getDataList().get(4).getValue());
	}
	
	@Test
	public void testAddFactorsToMeasurementRowDataListForSEED_SOURCE() {
		MeasurementRow row = MeasurementRowTestDataInitializer.createMeasurementRow();
		StandardVariable stdVariable = StandardVariableTestDataInitializer.createStandardVariable(TermId.SEED_SOURCE.getId(), TermId.SEED_SOURCE.name());
		MeasurementVariable variable = MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.SEED_SOURCE.getId(), TermId.SEED_SOURCE.name(), null);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		UserSelection userSelection = Mockito.mock(UserSelection.class);
		ImportedGermplasm importedGermplasm = this.setUpUserSelection(userSelection);
		WorkbookUtil.addFactorsToMeasurementRowDataList(row, stdVariable, true, variable, userSelection);
		Assert.assertEquals(importedGermplasm.getSource(), row.getDataList().get(4).getValue());
	}
	
	@Test
	public void testAddFactorsToMeasurementRowDataListForSOURCE() {
		MeasurementRow row = MeasurementRowTestDataInitializer.createMeasurementRow();
		StandardVariable stdVariable = StandardVariableTestDataInitializer.createStandardVariable(TermId.GERMPLASM_SOURCE.getId(), TermId.GERMPLASM_SOURCE.name());
		MeasurementVariable variable = MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.GERMPLASM_SOURCE.getId(), TermId.GERMPLASM_SOURCE.name(), null);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		UserSelection userSelection = Mockito.mock(UserSelection.class);
		ImportedGermplasm importedGermplasm = this.setUpUserSelection(userSelection);
		WorkbookUtil.addFactorsToMeasurementRowDataList(row, stdVariable, true, variable, userSelection);
		Assert.assertEquals(importedGermplasm.getSource(), row.getDataList().get(4).getValue());
	}
	
	@Test
	public void testAddFactorsToMeasurementRowDataListForSTOCKID() {
		MeasurementRow row = MeasurementRowTestDataInitializer.createMeasurementRow();
		StandardVariable stdVariable = StandardVariableTestDataInitializer.createStandardVariable(TermId.STOCKID.getId(), TermId.STOCKID.name());
		MeasurementVariable variable = MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.STOCKID.getId(), TermId.STOCKID.name(), null);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		UserSelection userSelection = Mockito.mock(UserSelection.class);
		ImportedGermplasm importedGermplasm = this.setUpUserSelection(userSelection);
		WorkbookUtil.addFactorsToMeasurementRowDataList(row, stdVariable, true, variable, userSelection);
		Assert.assertEquals(importedGermplasm.getStockIDs(), row.getDataList().get(4).getValue());
	}
	
	@Test
	public void testAddFactorsToMeasurementRowDataListForENTRY_CODE() {
		MeasurementRow row = MeasurementRowTestDataInitializer.createMeasurementRow();
		StandardVariable stdVariable = StandardVariableTestDataInitializer.createStandardVariable(TermId.ENTRY_CODE.getId(), TermId.ENTRY_CODE.name());
		MeasurementVariable variable = MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.ENTRY_CODE.getId(), TermId.ENTRY_CODE.name(), null);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		UserSelection userSelection = Mockito.mock(UserSelection.class);
		ImportedGermplasm importedGermplasm = this.setUpUserSelection(userSelection);
		WorkbookUtil.addFactorsToMeasurementRowDataList(row, stdVariable, true, variable, userSelection);
		Assert.assertEquals(importedGermplasm.getEntryCode(), row.getDataList().get(4).getValue());
	}
	
	@Test
	public void testAddFactorsToMeasurementRowDataListForCROSS() {
		MeasurementRow row = MeasurementRowTestDataInitializer.createMeasurementRow();
		StandardVariable stdVariable = StandardVariableTestDataInitializer.createStandardVariable(TermId.CROSS.getId(), TermId.CROSS.name());
		MeasurementVariable variable = MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.CROSS.getId(), TermId.CROSS.name(), null);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		UserSelection userSelection = Mockito.mock(UserSelection.class);
		ImportedGermplasm importedGermplasm = this.setUpUserSelection(userSelection);
		WorkbookUtil.addFactorsToMeasurementRowDataList(row, stdVariable, true, variable, userSelection);
		Assert.assertEquals(importedGermplasm.getCross(), row.getDataList().get(4).getValue());
	}
	
	private ImportedGermplasm setUpUserSelection(UserSelection userSelection) {
		ImportedGermplasmMainInfo  importedGermplasmMainInfo = Mockito.mock(ImportedGermplasmMainInfo.class);
		ImportedGermplasmList importedGermplasmList = Mockito.mock(ImportedGermplasmList.class);
		ImportedGermplasm importedGermplasm = ImportedGermplasmTestDataInitializer.createImportedGermplasm();
		
		Mockito.when(userSelection.getImportedGermplasmMainInfo()).thenReturn(importedGermplasmMainInfo);
		Mockito.when(importedGermplasmMainInfo.getImportedGermplasmList()).thenReturn(importedGermplasmList);
		Mockito.when(importedGermplasmList.getImportedGermplasms()).thenReturn(Arrays.asList(importedGermplasm));
		return importedGermplasm;
	}

}
