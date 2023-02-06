
package com.efficio.fieldbook.web.trial.service.impl;

import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ValidationServiceImplTest {

	@InjectMocks
	private ValidationServiceImpl validationService;

	private static final String DATA_TYPE_NUMERIC = "Numeric";

	private Workbook workbook;

	@Before
	public void setUp() {
		this.workbook = WorkbookTestDataInitializer.getTestWorkbook();
		this.workbook.setConditions(MeasurementVariableTestDataInitializer.createMeasurementVariableList());
	}

	@Test
	public void testisValidValueValidDefault() {

		final MeasurementVariable var = new MeasurementVariable();
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "sadasd", false));

	}

	@Test
	public void testisValidValueValidValueRange() {

		final MeasurementVariable var = new MeasurementVariable();
		var.setMaxRange(100d);
		var.setMinRange(1d);

		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "", false));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, null, false));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "50", false));

	}

	@Test
	public void testisValidValueValidNumericValue() {

		final MeasurementVariable var = new MeasurementVariable();
		var.setDataType(ValidationServiceImplTest.DATA_TYPE_NUMERIC);

		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "", false));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, null, false));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "50", false));

	}

	@Test
	public void testisValidValueValidDateValue() {

		final MeasurementVariable var = new MeasurementVariable();
		var.setDataTypeId(TermId.DATE_VARIABLE.getId());

		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "", true));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, null, true));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "20141010", true));

	}

	@Test
	public void testisValidValueInValidValueRange() {

		final MeasurementVariable var = new MeasurementVariable();
		var.setMaxRange(100d);
		var.setMinRange(1d);

		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "", false));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, null, false));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "101", false));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "0", false));
		Assert.assertFalse("The value is not valid therefore it must be false.", this.validationService.isValidValue(var, "abc", false));

	}

	@Test
	public void testisValidValueInValidNumericValue() {

		final MeasurementVariable var = new MeasurementVariable();
		var.setDataType(ValidationServiceImplTest.DATA_TYPE_NUMERIC);

		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "", false));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, null, false));
		Assert.assertFalse("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "abc", false));

	}

	@Test
	public void testisValidValueInValidDateValue() {

		final MeasurementVariable var = new MeasurementVariable();
		var.setDataTypeId(TermId.DATE_VARIABLE.getId());

		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "", true));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, null, true));
		Assert.assertFalse("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "sss", true));

	}

	@Test
	public void testisValidValueIfCategorical() {

		final MeasurementVariable var = new MeasurementVariable();
		var.setDataTypeId(TermId.CATEGORICAL_VARIABLE.getId());

		Assert.assertTrue("The value should always be valid since we allow out of bounds value already",
				this.validationService.isValidValue(var, "xxx", false));
	}

}
