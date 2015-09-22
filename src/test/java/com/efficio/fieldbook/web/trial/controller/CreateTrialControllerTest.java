
package com.efficio.fieldbook.web.trial.controller;

import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.ErrorCode;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;

public class CreateTrialControllerTest extends AbstractBaseIntegrationTest {

	@Autowired
	private CreateTrialController controller;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Test
	public void testUseExistingTrial() throws Exception {
		this.fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		this.controller.setFieldbookMiddlewareService(this.fieldbookMiddlewareService);
		Mockito.when(this.fieldbookMiddlewareService.getTrialDataSet(1)).thenThrow(
				new MiddlewareQueryException(ErrorCode.STUDY_FORMAT_INVALID.getCode(), "The term you entered is invalid"));

		Map<String, Object> tabDetails = this.controller.getExistingTrialDetails(1);

		Assert.assertNotNull("Expecting error but did not get one", tabDetails.get("createTrialForm"));

		CreateTrialForm form = (CreateTrialForm) tabDetails.get("createTrialForm");
		Assert.assertTrue("Expecting error but did not get one", form.isHasError());
	}

	@Test
	public void testRequiredExpDesignVar() {

		Assert.assertTrue("Expected term to be in the required var list but did not found it.",
				this.controller.inRequiredExpDesignVar(TermId.PLOT_NO.getId()));
		Assert.assertTrue("Expected term to be in the required var list but did not found it.",
				this.controller.inRequiredExpDesignVar(TermId.REP_NO.getId()));
		Assert.assertTrue("Expected term to be in the required var list but did not found it.",
				this.controller.inRequiredExpDesignVar(TermId.BLOCK_NO.getId()));
		Assert.assertTrue("Expected term to be in the required var list but did not found it.",
				this.controller.inRequiredExpDesignVar(TermId.ROW.getId()));
		Assert.assertTrue("Expected term to be in the required var list but did not found it.",
				this.controller.inRequiredExpDesignVar(TermId.COL.getId()));
		Assert.assertFalse("Expected term to NOT be in the required var list but did not found it.",
				this.controller.inRequiredExpDesignVar(TermId.LOCATION_ID.getId()));
	}
}
