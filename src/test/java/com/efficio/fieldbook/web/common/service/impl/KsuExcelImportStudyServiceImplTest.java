
package com.efficio.fieldbook.web.common.service.impl;

import junit.framework.Assert;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.efficio.fieldbook.web.util.KsuFieldbookUtil.KsuRequiredColumnEnum;

public class KsuExcelImportStudyServiceImplTest {

	private KsuExcelImportStudyServiceImpl ksuExcelImportStudy;

	@Before
	public void setUp() {
		this.ksuExcelImportStudy = Mockito.spy(new KsuExcelImportStudyServiceImpl());
	}

	@Test
	public void testGetLabelFromKsuRequiredColumn_ReturnsLabelFromKsuRequiredColumnEnum() {
		MeasurementVariable variable = new MeasurementVariable();
		variable.setTermId(TermId.ENTRY_NO.getId());

		String returnedLabel = this.ksuExcelImportStudy.getLabelFromKsuRequiredColumn(variable);
		String expectedLabel = KsuRequiredColumnEnum.get(variable.getTermId()).getLabel();

		Assert.assertEquals("Expecting to returned label from the ksuRequiredColumnEnum but didn't.", expectedLabel, returnedLabel);
	}

	@Test
	public void testGetLabelFromKsuRequiredColumn_ReturnsLabelFromVariableName() {
		MeasurementVariable variable = new MeasurementVariable();
		variable.setTermId(TermId.CROSS.getId());
		String expectedLabel = "Sample Label";
		variable.setName(expectedLabel);

		String returnedLabel = this.ksuExcelImportStudy.getLabelFromKsuRequiredColumn(variable);
		Assert.assertEquals("Expecting to returned label from the variable name but didn't.", expectedLabel, returnedLabel);
	}

	@Test
	public void testGetColumnHeaders() throws WorkbookParserException {
		Sheet sheet = Mockito.mock(Sheet.class);
		Row row = Mockito.mock(Row.class);
		Mockito.doReturn(row).when(sheet).getRow(0);

		int noOfColumnHeaders = 5;
		Mockito.doReturn((short) noOfColumnHeaders).when(row).getLastCellNum();

		for (int i = 0; i < noOfColumnHeaders; i++) {
			Cell cell = Mockito.mock(Cell.class);
			Mockito.doReturn(cell).when(row).getCell(i);
			Mockito.doReturn("TempValue" + i).when(cell).getStringCellValue();
		}

		String[] headerNames = this.ksuExcelImportStudy.getColumnHeaders(sheet);
		for (int i = 0; i < headerNames.length; i++) {
			Assert.assertEquals("Expecting to return TempValue" + i + " but returned " + headerNames[i], "TempValue" + i, headerNames[i]);
		}
	}

	@Test
	public void testValidateNumberOfSheets_ReturnsExceptionForInvalidNoOfSheet() {
		org.apache.poi.ss.usermodel.Workbook xlsBook = Mockito.mock(org.apache.poi.ss.usermodel.Workbook.class);
		Mockito.doReturn(2).when(xlsBook).getNumberOfSheets();

		try {
			this.ksuExcelImportStudy.validateNumberOfSheets(xlsBook);
			Assert.fail("Expecting to return an exception for invalid number of sheets but didn't.");
		} catch (WorkbookParserException e) {
			Assert.assertEquals("error.workbook.import.invalidNumberOfSheets", e.getMessage());
		}
	}

	@Test
	public void testValidateNumberOfSheets_ReturnsNoExceptionForValidNoOfSheet() {
		org.apache.poi.ss.usermodel.Workbook xlsBook = Mockito.mock(org.apache.poi.ss.usermodel.Workbook.class);
		Mockito.doReturn(1).when(xlsBook).getNumberOfSheets();

		try {
			this.ksuExcelImportStudy.validateNumberOfSheets(xlsBook);
		} catch (WorkbookParserException e) {
			Assert.fail("Expecting to return no exception for valid number of sheets but didn't.");
		}
	}

	@Test
	public void testValidate_ReturnsAnExceptionForInvalidHeaderNames() {
		try {
			org.apache.poi.ss.usermodel.Workbook xlsBook = Mockito.mock(org.apache.poi.ss.usermodel.Workbook.class);
			Sheet observationSheet = Mockito.mock(Sheet.class);
			Mockito.doNothing().when(this.ksuExcelImportStudy).validateNumberOfSheets(xlsBook);
			Mockito.doReturn(observationSheet).when(xlsBook).getSheetAt(0);

			// invalid header names, since it lacks Designation column
			String[] headerNames = {"Plot", "Entry_no", "GID"};
			Mockito.doReturn(headerNames).when(this.ksuExcelImportStudy).getColumnHeaders(observationSheet);

			this.ksuExcelImportStudy.validate(xlsBook);
			Assert.fail("Expecting to return an exception for invalid headers but didn't.");
		} catch (WorkbookParserException e) {
			Assert.assertEquals("error.workbook.import.requiredColumnsMissing", e.getMessage());
		}
	}

	@Test
	public void testValidate_ReturnsNoExceptionForValidHeaderNames() {
		try {
			org.apache.poi.ss.usermodel.Workbook xlsBook = Mockito.mock(org.apache.poi.ss.usermodel.Workbook.class);
			Sheet observationSheet = Mockito.mock(Sheet.class);
			Mockito.doNothing().when(this.ksuExcelImportStudy).validateNumberOfSheets(xlsBook);
			Mockito.doReturn(observationSheet).when(xlsBook).getSheetAt(0);

			String[] headerNames = {"Plot", "Entry_no", "GID", "Designation"};
			Mockito.doReturn(headerNames).when(this.ksuExcelImportStudy).getColumnHeaders(observationSheet);

			this.ksuExcelImportStudy.validate(xlsBook);
		} catch (WorkbookParserException e) {
			Assert.fail("Expecting to not return an exception for valid headers but didn't.");
		}
	}

	@Test
	public void testGetTrialInstanceNoFromFileName_ReturnsAnExceptionForFileNameWithoutTrialInstanceNo() {
		try {
			this.ksuExcelImportStudy.getTrialInstanceNoFromFileName("SampleFile.xls");
			Assert.fail("Expecting to return an exception for filename without trial instance no but didn't.");
		} catch (WorkbookParserException e) {
			Assert.assertEquals("error.workbook.import.missing.trial.instance", e.getMessage());
		}
	}

	@Test
	public void testGetTrialInstanceNoFromFileName_ReturnsNoExceptionForFileNameWithTrialInstanceNo() {
		try {
			this.ksuExcelImportStudy.getTrialInstanceNoFromFileName("2015.03.17_Trial 001-1.xls");
		} catch (WorkbookParserException e) {
			Assert.fail("Expecting to not return an exception for filename with trial instance no but didn't.");
		}
	}
}
