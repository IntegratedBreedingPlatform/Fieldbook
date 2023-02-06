package com.efficio.fieldbook.web.study.service.impl;

import org.generationcp.middleware.data.initializer.MeasurementTestDataInitializer;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvImportStudyServiceImplTest {

    private static final String GW100_G_VALUE = "100";

    private static final String GW100_G = "GW100_g";

    private static final String PLOT_NO = "PLOT_NO";

    private static final String ENTRY_NO = "ENTRY_NO";

    private static final String DESIG = "DESIG";

    private static final String GID = "GID";

	private static final String OBS_UNIT_ID = "OBS_UNIT_ID";

    private final String[] rowHeaders = {"ENTRY_TYPE", this.GID, "DESIGNATION", this.ENTRY_NO, "REP_NO",
		this.PLOT_NO, this.OBS_UNIT_ID};

    private Workbook workbook;

    private CsvImportStudyServiceImpl csvImport;

    @Before
	public void setUp() {
		this.workbook = WorkbookTestDataInitializer.getTestWorkbook(1, new StudyTypeDto("N"));
        this.csvImport = new CsvImportStudyServiceImpl(this.workbook, "", "");
	}

	@Test
	public void testIsValidHeaderNamesTrue() {
		Assert.assertTrue("The headers should be valid.", this.csvImport.isValidHeaderNames(this.rowHeaders));
	}

	@Test
	public void testIsValidHeaderNamesFalse() {
		final String[] headers = {"ENTRY_TYPE", this.GID, "DESIGNATION", this.ENTRY_NO, "REP_NO", "plot"};
		Assert.assertFalse("The headers should be valid.", this.csvImport.isValidHeaderNames(headers));
	}

	@Test
	public void testGetLabelFromKsuRequiredColumnTermIdPresentInEnum() {
		final MeasurementTestDataInitializer measurementTestDataInitializer = new MeasurementTestDataInitializer();
		final MeasurementVariable mVar = measurementTestDataInitializer.createMeasurementVariable(TermId.ENTRY_NO.getId(), 1);
		final String label = this.csvImport.getLabelFromRequiredColumn(mVar);
		Assert.assertEquals("The label should be ENTRY_NO", CsvImportStudyServiceImplTest.ENTRY_NO, label);

	}

	@Test
	public void testGetLabelFromKsuRequiredColumnObsUnitIdPresentInEnum() {
		final MeasurementTestDataInitializer measurementTestDataInitializer = new MeasurementTestDataInitializer();
		final MeasurementVariable mVar = measurementTestDataInitializer.createMeasurementVariable(TermId.OBS_UNIT_ID.getId(), 1);
		final String label = this.csvImport.getLabelFromRequiredColumn(mVar);
		Assert.assertEquals("The label should be OBS_UNIT_ID", CsvImportStudyServiceImplTest.OBS_UNIT_ID, label);

	}

	@Test
	public void testGetLabelFromKsuRequiredColumnTermIdNotPresentInEnum() {
		final MeasurementTestDataInitializer measurementTestDataInitializer = new MeasurementTestDataInitializer();
		final MeasurementVariable mVar = measurementTestDataInitializer.createMeasurementVariable(1, 1);
		final String label = this.csvImport.getLabelFromRequiredColumn(mVar);
		Assert.assertEquals("The label should be " + mVar.getName(), mVar.getName(), label);
	}

	@Test
	public void testGetObsUnitIdFromRow() throws WorkbookParserException {
		final List<String> row = Arrays.asList("1", "1", "1","PLOT123P123456");
		final String keyIdentifier = this.csvImport.getObsUnitIdFromRow(row, 3);
		Assert.assertEquals("KeyIdendtifier should be PLOT123P123456", "PLOT123P123456", keyIdentifier);
	}

	@Test
	public void testGetRealNumericValueOfIntWithTrailingZeroes() {
		final String realNumericValue = this.csvImport.getRealNumericValue("1.00");
		Assert.assertEquals("The value should be 1", "1", realNumericValue);
	}

	@Test
	public void testGetRealNumericValueOfIntOfDoubleValue() {
		final String realNumericValue = this.csvImport.getRealNumericValue("1.25");
		Assert.assertEquals("The value should be 1.25", "1.25", realNumericValue);
	}

	private Map<Integer, List<String>> createCsvMap() {
		final Map<Integer, List<String>> csvMap = new HashMap<>();
		final List<String> headers =
			Arrays.asList(this.GID, CsvImportStudyServiceImplTest.DESIG, this.ENTRY_NO, this.PLOT_NO, this.GW100_G);
		csvMap.put(0, headers);
		final List<String> row = Arrays.asList("999999", "TIANDOUGOU-9", "0", "0", CsvImportStudyServiceImplTest.GW100_G_VALUE);
		csvMap.put(1, row);
		return csvMap;
	}
}
