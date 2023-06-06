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

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(AdvancingController.URL)
public class AdvancingController extends AbstractBaseFieldbookController {

	/**
	 * The Constant URL.
	 */
	protected static final String URL = "/StudyManager/advance/study";

	@Override
	public String getContentName() {
		return null;
	}

	// FIXME: De-thymeleaf this page
	@RequestMapping(value = "/selectEnvironmentModal", method = RequestMethod.GET)
	public String selectEnvironmentModal(final Model model) {
		return super.showAjaxPage(model, "StudyManager/selectEnvironmentModal");
	}

}
