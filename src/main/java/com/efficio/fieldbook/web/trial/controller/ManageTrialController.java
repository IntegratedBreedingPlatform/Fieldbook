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
import com.efficio.fieldbook.web.trial.form.ManageTrialForm;

import org.generationcp.commons.security.AuthorizationService;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * The Class ManageTrialController.
 */
@Controller
@RequestMapping({"/TrialManager", ManageTrialController.URL})
public class ManageTrialController extends AbstractBaseFieldbookController {

	/** The Constant URL. */
	public static final String URL = "/TrialManager/manageTrial";

	@Resource
	protected StudyDataManager studyDataManager;

	@Resource
	private AuthorizationService authorizationService;

	/**
	 *
	 * @param form
	 * @param model
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String show(@ModelAttribute("manageTrialForm") ManageTrialForm form, Model model, HttpServletRequest request) {
		model.addAttribute("preloadSummaryId", request.getParameter("summaryId") != null ? request.getParameter("summaryId") : "");
		model.addAttribute("preloadSummaryName", request.getParameter("summaryName") != null ? request.getParameter("summaryName") : "");
		model.addAttribute("studyTypes", this.studyDataManager.getAllVisibleStudyTypes());
		model.addAttribute("hasManageStudiesPermission", this.authorizationService.hasAnyAuthority(PermissionsEnum.MANAGE_STUDIES_PERMISSIONS));
		setIsSuperAdminAttribute(model);
		return super.show(model);
	}

	@Override
	public String getContentName() {
		return "Common/manageStudy";
	}

	@ModelAttribute("currentCropUserId")
	public Integer getCurrentCropUserId() {
		return this.contextUtil.getCurrentWorkbenchUserId();
	}

	@ModelAttribute("cropName")
	public String getCropName() {
		return this.contextUtil.getProjectInContext().getCropType().getCropName();
	}

	@ModelAttribute("currentProgramId")
	public String getProgramId() {
		return this.contextUtil.getProjectInContext().getUniqueID();
	}


}
