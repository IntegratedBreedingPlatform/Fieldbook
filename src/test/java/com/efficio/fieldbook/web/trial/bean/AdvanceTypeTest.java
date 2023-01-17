package com.efficio.fieldbook.web.trial.bean;

import org.junit.Assert;
import org.junit.Test;

public class AdvanceTypeTest {

	@Test
	public void testfromLowerCaseName() {
		Assert.assertEquals(AdvanceType.fromLowerCaseName("deprecatedSample"), AdvanceType.SAMPLE);
		Assert.assertEquals(AdvanceType.fromLowerCaseName("study"), AdvanceType.STUDY);
		Assert.assertEquals(AdvanceType.fromLowerCaseName(""), AdvanceType.NONE);
		Assert.assertEquals(AdvanceType.fromLowerCaseName(null), AdvanceType.NONE);
	}
}
