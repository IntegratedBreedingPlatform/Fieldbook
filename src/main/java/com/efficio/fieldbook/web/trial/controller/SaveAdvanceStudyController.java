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
import com.efficio.fieldbook.web.trial.form.AdvancingStudyForm;
import org.generationcp.middleware.ruleengine.pojo.ImportedGermplasm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping(SaveAdvanceStudyController.URL)
public class SaveAdvanceStudyController extends AbstractBaseFieldbookController {

	public static final String URL = "/StudyManager/saveAdvanceStudy";

	public static final String PAGINATION_TEMPLATE = "/StudyManager/showSaveAdvanceStudyPagination";

	@Override
	public String getContentName() {
		return "StudyManager/saveAdvanceStudy";
	}

	@RequestMapping(value = "/page/{pageNum}", method = RequestMethod.GET)
	public String getPaginatedList(@PathVariable int pageNum, @ModelAttribute("advancingStudyForm") AdvancingStudyForm form,
		Model model, HttpServletRequest req) {

		String listIdentifier = req.getParameter("listIdentifier");
		AdvancingStudyForm formFromSession = this.getPaginationListSelection().getAdvanceDetails(listIdentifier);
		List<ImportedGermplasm> importedAdvanceGermplasmList = formFromSession.getGermplasmList();

		if (importedAdvanceGermplasmList != null) {
			form.setGermplasmList(importedAdvanceGermplasmList);
			form.setEntries(importedAdvanceGermplasmList.size());
			form.changePage(pageNum);
		}

		return super.showAjaxPage(model, SaveAdvanceStudyController.PAGINATION_TEMPLATE);
	}
}
