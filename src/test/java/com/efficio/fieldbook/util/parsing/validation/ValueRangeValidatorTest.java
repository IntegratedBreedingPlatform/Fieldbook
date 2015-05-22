package com.efficio.fieldbook.util.parsing.validation;

import org.generationcp.commons.parsing.validation.ValueRangeValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */

@RunWith(BlockJUnit4ClassRunner.class)
public class ValueRangeValidatorTest {
	private ValueRangeValidator dut;

	@Before
	public void setUp() throws Exception {
		List<String> validValues = new ArrayList<>();
		validValues.add("A");
		validValues.add("B");
		validValues.add("C");

		dut = new ValueRangeValidator(validValues);
	}


	@Test
	public void testValueInRange() {
		assertTrue(dut.isParsedValueValid("B",null));
	}

	@Test
	public void testValueNotInRange() {
		assertFalse(dut.isParsedValueValid("Z",null));
	}

	@Test
	public void testBlankValueAndSkipIfEmptyTrue() {
		assertTrue(dut.isParsedValueValid(null,null));
	}

	@Test
	public void testBlankValueAndSkipIfEmptyFalse() {
		List<String> validValues = new ArrayList<>();
		validValues.add("A");
		validValues.add("B");
		validValues.add("C");
		dut = new ValueRangeValidator(validValues, false);

		assertFalse(dut.isParsedValueValid(null,null));
	}

	@Test
	public void testValueCheckAgainstBlankValid() {
		dut = new ValueRangeValidator(null);

		assertTrue(dut.isParsedValueValid("ZZ",null));
	}
}
