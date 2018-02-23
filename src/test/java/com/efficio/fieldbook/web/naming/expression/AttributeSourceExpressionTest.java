package com.efficio.fieldbook.web.naming.expression;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Method;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


@RunWith(MockitoJUnitRunner.class)
public class AttributeSourceExpressionTest extends TestExpression {

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@InjectMocks
	AttributeSourceExpression expression = new AttributeSourceExpression();

	private static final String ATTRIBUTE_NAME = "IBP_BMS";

	private static final String PREFIX = "[ATTRSC.IBP_BMS]";

	private static final String COUNT = "[SEQUENCE]";


	@Test
	public void testAttributeAsPrefix() throws Exception {
		Mockito.when(germplasmDataManager.getAttributeValue(1000, ATTRIBUTE_NAME)).thenReturn("AA");
		final Method derivativeMethod = this.createDerivativeMethod(PREFIX, COUNT, null, "-", true);
		final ImportedGermplasm importedGermplasm =
			this.createImportedGermplasm(1, "(AA/ABC)", "1000", 104, 104, -1, derivativeMethod.getMid());
		final AdvancingSource source =
			this.createAdvancingSourceTestData(derivativeMethod, importedGermplasm, "(AA/ABC)", "Dry", "NurseryTest");
		final List<StringBuilder> values = this.createInitialValues(source);
		expression.apply(values, source, PREFIX);
		this.printResult(values, source);
		assertThat("(AA/ABC)-AA", is(equalTo(values.get(0).toString())));
	}

	@Test
	public void testAttributeAsPrefixWithOutAttributeValue() throws Exception {
		Mockito.when(germplasmDataManager.getAttributeValue(1000, ATTRIBUTE_NAME)).thenReturn("");
		final Method derivativeMethod = this.createDerivativeMethod(PREFIX, COUNT, null, "-", true);
		final ImportedGermplasm importedGermplasm =
			this.createImportedGermplasm(1, "(AA/ABC)", "1000", 104, 104, -1, derivativeMethod.getMid());
		final AdvancingSource source =
			this.createAdvancingSourceTestData(derivativeMethod, importedGermplasm, "(AA/ABC)", "Dry", "NurseryTest");
		final List<StringBuilder> values = this.createInitialValues(source);
		expression.apply(values, source, PREFIX);
		this.printResult(values, source);
		assertThat("(AA/ABC)-", is(equalTo(values.get(0).toString())));
	}

	@Test
	public void testAttributeAsPrefixInvalidGpid2() throws Exception {

		Mockito.when(germplasmDataManager.getAttributeValue(1000, ATTRIBUTE_NAME)).thenReturn("AA");
		final Method derivativeMethod = this.createDerivativeMethod(PREFIX, COUNT, null, "-", true);
		final ImportedGermplasm importedGermplasm =
			this.createImportedGermplasm(1, "(AA/ABC)", "1000", 0, 0, -1, derivativeMethod.getMid());
		AdvancingSource source = this.createAdvancingSourceTestData(derivativeMethod, importedGermplasm, "(AA/ABC)", "Dry", "NurseryTest");
		List<StringBuilder> values = this.createInitialValues(source);
		expression.apply(values, source, PREFIX);
		this.printResult(values, source);
		assertThat("(AA/ABC)-", is(equalTo(values.get(0).toString())));
	}

	@Test
	public void testAttributeAsPrefixInvalidBreedingMethod() throws Exception {

		Mockito.when(germplasmDataManager.getAttributeValue(1000, ATTRIBUTE_NAME)).thenReturn("AB");
		final Method generativeMethod = this.createGenerativeMethod(PREFIX, COUNT, null, "-", true);
		final ImportedGermplasm importedGermplasm =
			this.createImportedGermplasm(1, "(AA/ABC)", "1000", 104, 104, -1, generativeMethod.getMid());
		AdvancingSource source =
			this.createAdvancingSourceTestData(generativeMethod, importedGermplasm, "(AA/ABC)", "Dry", "NurseryTest");
		List<StringBuilder> values = this.createInitialValues(source);
		expression.apply(values, source, PREFIX);
		this.printResult(values, source);
		assertThat("(AA/ABC)-",is(equalTo(values.get(0).toString())));
	}
}
