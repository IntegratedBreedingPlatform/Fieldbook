package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.service.api.KeySequenceRegisterService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class DoubleHaploidSourceExpressionTest extends TestExpression {

	@Mock
	private KeySequenceRegisterService keySequenceRegisterService;

	@InjectMocks
	private DoubleHaploidSourceExpression doubleHaploidSourceExpression;

	/**
	 * Test to check whether designation value is suffixed with '@' + [sequence number] for Double Haploid Method
	 * @throws MiddlewareException
	 */
    @Test
    public void testDesignationValueWithLastUsedSequenceNumberForDoubleHaploid() throws MiddlewareException {
		AdvancingSource source = this.createAdvancingSourceTestData("(CML454 X CML451)-B-3-1-1@0", null, null, null, "[DHSOURCE]", false);

		List<StringBuilder> values = this.createInitialValues(source);

		Mockito.when(this.keySequenceRegisterService.incrementAndGetNextSequence("(CML454 X CML451)-B-3-1-1@", null)).thenReturn(25);

		doubleHaploidSourceExpression.apply(values, source);

		Assert.assertEquals("Error in Designation value for Double Haploid Source Method", "(CML454 X CML451)-B-3-1-1@25", values.get(0).toString());
	}

	/**
	 * Test to check whether designation value is not suffixed if value does not contain '@0' for Double Haploid Method
	 * @throws MiddlewareException
	 */
	@Test
	public void testDesignationValueWithoutLastUsedSequenceNumberForDoubleHaploid() throws MiddlewareException {
		AdvancingSource source = this.createAdvancingSourceTestData("(CML454 X CML451)-B-3-1-1", "-", "DH", null, "[DHSOURCE]", false);

		List<StringBuilder> values = this.createInitialValues(source);

		doubleHaploidSourceExpression.apply(values, source);

		Assert.assertEquals("Error in Designation value for Double Haploid Source Method", "(CML454 X CML451)-B-3-1-1-DH", values.get(0).toString());
	}


}
