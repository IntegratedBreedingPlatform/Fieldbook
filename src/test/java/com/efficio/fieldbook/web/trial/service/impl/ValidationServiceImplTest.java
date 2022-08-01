
package com.efficio.fieldbook.web.trial.service.impl;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.MethodTestDataInitializer;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.api.role.RoleService;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.user.UserDto;
import org.generationcp.middleware.service.api.user.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ValidationServiceImplTest {

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private UserService userService;
	
	@Mock
	private RoleService roleService;
	
	@InjectMocks
	private ValidationServiceImpl validationService;

	private static final String DATA_TYPE_NUMERIC = "Numeric";

	private Workbook workbook;

	private static final String WARNING_MESSAGE =
			"The value for null in the import file is invalid and will not be imported. You can change this value by editing it manually, or by uploading a corrected import file.";

	private static final String EMPTY_STRING = "";

	@Before
	public void setUp() {
		final Project project = Mockito.mock(Project.class);
		this.workbook = WorkbookTestDataInitializer.getTestWorkbook();
		this.workbook.setConditions(MeasurementVariableTestDataInitializer.createMeasurementVariableList());
		Mockito.when(this.fieldbookMiddlewareService.getAllBreedingMethods(Matchers.eq(false)))
				.thenReturn(MethodTestDataInitializer.createMethodList());
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

	@Test
	public void testValidatePersonIdIfPIIdHasInvalidValue() {
		Mockito.doReturn(new ArrayList<>()).when(this.userService).getUsersByPersonIds(Mockito.<Integer>anyList());
		final String warningMessage =
				this.validationService.validatePersonId(MeasurementVariableTestDataInitializer.createMeasurementVariable());
		Assert.assertTrue("There should be a warning message", ValidationServiceImplTest.WARNING_MESSAGE.equals(warningMessage));
	}

	@Test
	public void testValidatePersonIdIfPIIdHasValidValue() {
		final List<UserDto> users = new ArrayList<>();
		users.add(new UserDto());
		Mockito.doReturn(users).when(this.userService).getUsersByPersonIds(Mockito.<Integer>anyList());
		final String warningMessage =
				this.validationService.validatePersonId(MeasurementVariableTestDataInitializer.createMeasurementVariable());
		Assert.assertTrue("There should be no warning message", ValidationServiceImplTest.EMPTY_STRING.equals(warningMessage));
	}

	@Test
	public void testValidateBreedingMethodCodeIfBMCodeHasValue() {
		final String warningMessage = this.validationService.validateBreedingMethodCode(
				MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.BREEDING_METHOD_CODE.getId(), "PSP"));
		Assert.assertTrue("There should be no warning message", ValidationServiceImplTest.EMPTY_STRING.equals(warningMessage));
	}

	@Test
	public void testValidateBreedingMethodCodeIfBMCodeHasInvalue() {
		final String warningMessage = this.validationService.validateBreedingMethodCode(
				MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.BREEDING_METHOD_CODE.getId(), "PXP"));
		Assert.assertTrue("There should be a warning message", ValidationServiceImplTest.WARNING_MESSAGE.equals(warningMessage));
	}

	@Test
	public void testValidateObservationValueNumeric() {
		Variable variable = new Variable();
		Scale scale = new Scale();
		scale.setDataType(DataType.NUMERIC_VARIABLE);
		variable.setScale(scale);

		// Edge cases, independent of variable.
		Assert.assertFalse(this.validationService.validateObservationValue(variable, "abc"));
		Assert.assertTrue(this.validationService.validateObservationValue(variable, ""));
		Assert.assertTrue(this.validationService.validateObservationValue(variable, null));

		// When both min and max are set.
		scale.setMinValue("10");
		scale.setMaxValue("20");
		Assert.assertFalse(this.validationService.validateObservationValue(variable, "9"));
		Assert.assertFalse(this.validationService.validateObservationValue(variable, "21"));
		Assert.assertTrue(this.validationService.validateObservationValue(variable, "10"));
		Assert.assertTrue(this.validationService.validateObservationValue(variable, "11"));
		Assert.assertTrue(this.validationService.validateObservationValue(variable, "20"));

		// Only min is set. No max.
		scale.setMinValue("10");
		scale.setMaxValue(null);
		Assert.assertFalse(this.validationService.validateObservationValue(variable, "9"));
		Assert.assertTrue(this.validationService.validateObservationValue(variable, "100"));

		// Only max is set. No min.
		scale.setMinValue(null);
		scale.setMaxValue("20");
		Assert.assertTrue(this.validationService.validateObservationValue(variable, "5"));
		Assert.assertFalse(this.validationService.validateObservationValue(variable, "21"));

		// Any number is valid when there is no min/max set on scale.
		scale.setMinValue(null);
		scale.setMaxValue(null);
		Assert.assertTrue(this.validationService.validateObservationValue(variable, "999"));
	}

	@Test
	public void testValidateObservationValueDate() {
		Variable variable = new Variable();
		Scale scale = new Scale();
		scale.setDataType(DataType.DATE_TIME_VARIABLE);
		variable.setScale(scale);

		Assert.assertFalse(this.validationService.validateObservationValue(variable, "abc"));
		Assert.assertTrue(this.validationService.validateObservationValue(variable, ""));
		Assert.assertTrue(this.validationService.validateObservationValue(variable, null));

		Assert.assertTrue(this.validationService.validateObservationValue(variable, "20161225"));
		Assert.assertFalse(this.validationService.validateObservationValue(variable, "20161325"));
		Assert.assertFalse(this.validationService.validateObservationValue(variable, "20161232"));
		Assert.assertFalse(this.validationService.validateObservationValue(variable, "20150229"));
	}
}
