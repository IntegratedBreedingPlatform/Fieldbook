
package com.efficio.fieldbook.web.naming.expression;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

import junit.framework.Assert;

public class SeasonExpressionTest extends TestExpression {
	
	@Test
	public void testSeasonAsPrefix() throws Exception {
		SeasonExpression expression = new SeasonExpression();
		AdvancingSource source = this.createAdvancingSourceTestData("GERMPLASM_TEST", null, "[SEASON]", null, null, true);
		List<StringBuilder> values = this.createInitialValues(source);
		expression.apply(values, source);
		this.printResult(values, source);
		Assert.assertEquals("GERMPLASM_TESTDry", this.buildResult(values));
	}

	@Test
	public void testSeasonAsSuffix() throws Exception {
		SeasonExpression expression = new SeasonExpression();
		AdvancingSource source = this.createAdvancingSourceTestData("GERMPLASM_TEST", ":", null, null, "[SEASON]", true);
		List<StringBuilder> values = this.createInitialValues(source);
		expression.apply(values, source);
		this.printResult(values, source);
		Assert.assertEquals("GERMPLASM_TEST:Dry", this.buildResult(values));
	}

	@Test
	public void testNoSeason() throws Exception {
		SimpleDateFormat f = new SimpleDateFormat("YYYYMM");
		String defSeason = f.format(new Date());
		System.out.println("Testing No Season");
		SeasonExpression expression = new SeasonExpression();
		AdvancingSource source = this.createAdvancingSourceTestData("GERMPLASM_TEST", "-", null, null, "[SEASON]", true);
		source.setSeason(null);
		List<StringBuilder> values = this.createInitialValues(source);
		expression.apply(values, source);
		this.printResult(values, source);
		Assert.assertEquals("GERMPLASM_TEST-" + defSeason, this.buildResult(values));
	}

	@Test
	public void testCaseSensitive() throws Exception {
		SeasonExpression expression = new SeasonExpression();
		AdvancingSource source = this.createAdvancingSourceTestData("GERMPLASM_TEST", null, "[seasOn]", null, null, true);
		List<StringBuilder> values = this.createInitialValues(source);
		expression.apply(values, source);
		System.out.println("process code is in lower case");
		this.printResult(values, source);
		Assert.assertEquals("GERMPLASM_TESTDry", this.buildResult(values));
	}

}
