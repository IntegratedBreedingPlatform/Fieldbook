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
public class AttributeFemaleParentExpressionTest extends TestExpression {

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@InjectMocks
	AttributeFemaleParentExpression expression = new AttributeFemaleParentExpression();

	private static final String ATTRIBUTE_NAME = "ORI_COUN";

	private static final String PREFIX = "[ATTRFP.ORI_COUN]";

	private static final String COUNT = "[SEQUENCE]";

	@Test
	public void testAttributeAsPrefixDerivativeMethod() throws Exception {

		Mockito.when(germplasmDataManager.getAttributeValue(104, ATTRIBUTE_NAME)).thenReturn("Mexico");
		final Method derivativeMethod = this.createDerivativeMethod(PREFIX, COUNT, null, "-", true);

		final ImportedGermplasm importedGermplasm =
				this.createImportedGermplasm(1, "(AA/ABC)", "1000", 104, 105, -1, derivativeMethod.getMid());
		final AdvancingSource source =
				this.createAdvancingSourceTestData(derivativeMethod, importedGermplasm, "(AA/ABC)", "Dry", "NurseryTest");
		final List<StringBuilder> values = this.createInitialValues(source);

		expression.apply(values, source, PREFIX);

		assertThat(values.get(0).toString(), is(equalTo("(AA/ABC)-Mexico")));
	}

	@Test
	public void testAttributeAsPrefixWithoutAttributeValueDerivativeMethod() throws Exception {
		Mockito.when(germplasmDataManager.getAttributeValue(104, ATTRIBUTE_NAME)).thenReturn("");
		final Method derivativeMethod = this.createDerivativeMethod(PREFIX, COUNT, null, "-", true);
		final ImportedGermplasm importedGermplasm =
				this.createImportedGermplasm(1, "(AA/ABC)", "1000", 104, 105, -1, derivativeMethod.getMid());
		final AdvancingSource source =
				this.createAdvancingSourceTestData(derivativeMethod, importedGermplasm, "(AA/ABC)", "Dry", "NurseryTest");
		final List<StringBuilder> values = this.createInitialValues(source);

		expression.apply(values, source, PREFIX);

		assertThat(values.get(0).toString(), is(equalTo("(AA/ABC)-")));
	}

	@Test
	public void testAttributeAsPrefixGpid1UnknownDerivativeMethod() throws Exception {

		Mockito.when(germplasmDataManager.getAttributeValue(0, ATTRIBUTE_NAME)).thenReturn("");
		final Method derivativeMethod = this.createDerivativeMethod(PREFIX, COUNT, null, "-", true);
		final ImportedGermplasm importedGermplasm =
				this.createImportedGermplasm(1, "(AA/ABC)", "1000", 0, 0, -1, derivativeMethod.getMid());
		AdvancingSource source = this.createAdvancingSourceTestData(derivativeMethod, importedGermplasm, "(AA/ABC)", "Dry", "NurseryTest");
		List<StringBuilder> values = this.createInitialValues(source);
		expression.apply(values, source, PREFIX);

		assertThat(values.get(0).toString(), is(equalTo("(AA/ABC)-")));
	}

	@Test
	public void testAttributeAsPrefixGenerativeMethod() throws Exception {
		Mockito.when(germplasmDataManager.getAttributeValue(104, ATTRIBUTE_NAME)).thenReturn("Mexico");
		final Method generativeMethod = this.createGenerativeMethod(PREFIX, COUNT, null, "-", true);
		final ImportedGermplasm importedGermplasm =
				this.createImportedGermplasm(1, "(AA/ABC)", "1000", 104, 105, -1, generativeMethod.getMid());
		final AdvancingSource source =
				this.createAdvancingSourceTestData(generativeMethod, importedGermplasm, "(AA/ABC)", "Dry", "NurseryTest");

		source.setFemaleGid(104);

		final List<StringBuilder> values = this.createInitialValues(source);

		expression.apply(values, source, PREFIX);

		assertThat(values.get(0).toString(), is(equalTo("(AA/ABC)-Mexico")));
	}

	@Test
	public void testAttributeAsPrefixWithoutAttributeValueGenerativeMethod() throws Exception {
		Mockito.when(germplasmDataManager.getAttributeValue(104, ATTRIBUTE_NAME)).thenReturn("");
		final Method generativeMethod = this.createGenerativeMethod(PREFIX, COUNT, null, "-", true);
		final ImportedGermplasm importedGermplasm =
				this.createImportedGermplasm(1, "(AA/ABC)", "1000", 104, 105, -1, generativeMethod.getMid());
		final AdvancingSource source =
				this.createAdvancingSourceTestData(generativeMethod, importedGermplasm, "(AA/ABC)", "Dry", "NurseryTest");

		source.setFemaleGid(104);

		final List<StringBuilder> values = this.createInitialValues(source);

		expression.apply(values, source, PREFIX);

		assertThat(values.get(0).toString(), is(equalTo("(AA/ABC)-")));
	}

	@Test
	public void testAttributeAsPrefixGpid1UnknownGenerativeMethod() throws Exception {

		Mockito.when(germplasmDataManager.getAttributeValue(0, ATTRIBUTE_NAME)).thenReturn("");
		final Method generativeMethod = this.createGenerativeMethod(PREFIX, COUNT, null, "-", true);
		final ImportedGermplasm importedGermplasm =
				this.createImportedGermplasm(1, "(AA/ABC)", "1000", 0, 0, -1, generativeMethod.getMid());
		AdvancingSource source = this.createAdvancingSourceTestData(generativeMethod, importedGermplasm, "(AA/ABC)", "Dry", "NurseryTest");
		List<StringBuilder> values = this.createInitialValues(source);
		expression.apply(values, source, PREFIX);

		assertThat(values.get(0).toString(), is(equalTo("(AA/ABC)-")));
	}

}
