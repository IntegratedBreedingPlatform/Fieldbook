
package com.efficio.fieldbook.util.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.WorkbookRowConverter;
import org.generationcp.commons.parsing.validation.ParsingValidator;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.pojos.Location;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.web.util.parsing.InventoryHeaderLabels;
import com.efficio.fieldbook.web.util.parsing.InventoryImportParser;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 3/12/2015 Time: 5:13 PM
 */

@RunWith(MockitoJUnitRunner.class)
public class WorkbookRowConverterTest {

	private WorkbookRowConverter<InventoryDetails> dut;

	public static final String TEST_VALUE = "TEST VALUE";
	public static final String TEST_COLUMN = "TEST_COLUMN";

	@Before
	public void setUp() throws Exception {
		Map<InventoryHeaderLabels, Integer> inventoryHeaderLabelsMap = InventoryHeaderLabels.headers(GermplasmListType.CROSSES);
		this.dut =
				new InventoryImportParser.InventoryRowConverter(Mockito.mock(Workbook.class), 0, 0, inventoryHeaderLabelsMap.size(),
						inventoryHeaderLabelsMap, new HashMap<String, Location>(), new Scale());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testApplyValidations() throws FileParsingException {
		List<ParsingValidator> validatorList = new ArrayList<>();
		ParsingValidator validator = Mockito.mock(ParsingValidator.class);
		Mockito.when(validator.isParsedValueValid(Matchers.anyString(), Matchers.anyMap())).thenReturn(true);

		validatorList.add(validator);

		this.dut.applyValidation(WorkbookRowConverterTest.TEST_VALUE, null, WorkbookRowConverterTest.TEST_COLUMN, validatorList);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = FileParsingException.class)
	public void testApplyValidationsParseErrorOccur() throws FileParsingException {
		List<ParsingValidator> validatorList = new ArrayList<>();
		ParsingValidator validator = Mockito.mock(ParsingValidator.class);
		Mockito.when(validator.isParsedValueValid(Matchers.anyString(), Matchers.anyMap())).thenReturn(false);

		validatorList.add(validator);

		this.dut.applyValidation(WorkbookRowConverterTest.TEST_VALUE, null, WorkbookRowConverterTest.TEST_COLUMN, validatorList);
	}

	@Test
	public void testContinueTillBlankWithNonBlankValue() {
		WorkbookRowConverter.ContinueExpression continueExpression = new WorkbookRowConverter.ContinueTillBlank();
		Map<Integer, String> currentValue = new HashMap<>();
		currentValue.put(1, "NON_EMPTY_VALUE");
		Assert.assertTrue(continueExpression.shouldContinue(currentValue));
	}

	@Test
	public void testContinueTillBlankWithBlankValue() {
		WorkbookRowConverter.ContinueExpression continueExpression = new WorkbookRowConverter.ContinueTillBlank();

		Assert.assertFalse(continueExpression.shouldContinue(null));
	}

	@Test
	public void testConvertWorkbookRowsToObjectBlank() throws FileParsingException {
		WorkbookRowConverter<InventoryDetails> mole = Mockito.spy(this.dut);

		Mockito.doReturn(true).when(mole).isRowEmpty(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt());
		List<InventoryDetails> inventoryDetailList = mole.convertWorkbookRowsToObject(new WorkbookRowConverter.ContinueTillBlank());

		Assert.assertNotNull(inventoryDetailList);
		Assert.assertTrue(inventoryDetailList.isEmpty());
	}

	@Test
	public void testConvertWorkbookRowsToObject() throws FileParsingException {
		WorkbookRowConverter<InventoryDetails> mole = Mockito.spy(this.dut);

		Mockito.doReturn(false).doReturn(true).when(mole).isRowEmpty(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt());
		Mockito.doReturn("").when(mole).getCellStringValue(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt());
		InventoryDetails details = Mockito.mock(InventoryDetails.class);
		Mockito.doReturn(details).when(mole).convertToObject(Matchers.anyMap());

		List<InventoryDetails> inventoryDetailList = mole.convertWorkbookRowsToObject(new WorkbookRowConverter.ContinueTillBlank());

		Assert.assertNotNull(inventoryDetailList);
		Assert.assertFalse(inventoryDetailList.isEmpty());
		Assert.assertEquals(details, inventoryDetailList.get(0));
	}
}
