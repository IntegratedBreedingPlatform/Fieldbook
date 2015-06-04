/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.trial.controller;

import org.generationcp.middleware.domain.oms.StudyType;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;

import com.efficio.fieldbook.web.AbstractBaseControllerIntegrationTest;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

public class ManageTrialControllerTest extends AbstractBaseControllerIntegrationTest {

	@Test
	public void testGetReturnsCorrectModelAndView() throws Exception {

		ModelAndView mav = this.request(ManageTrialController.URL, HttpMethod.GET.name());

		ModelAndViewAssert.assertViewName(mav, AbstractBaseFieldbookController.BASE_TEMPLATE_NAME);
		ModelAndViewAssert.assertModelAttributeValue(mav, AbstractBaseFieldbookController.TEMPLATE_NAME_ATTRIBUTE, "Common/manageStudy");
		ModelAndViewAssert.assertModelAttributeValue(mav, "type", StudyType.T.getName());

	}
}
