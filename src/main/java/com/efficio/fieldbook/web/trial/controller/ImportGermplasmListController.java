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

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.form.ImportGermplasmListForm;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.DataImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

/**
 * This controller handles the 2nd step in the study manager process.
 *
 * @author Daniel Jao
 */
@Controller
@RequestMapping({ ImportGermplasmListController.URL,
		ImportGermplasmListController.URL_1, ImportGermplasmListController.URL_2 })
@Transactional
public class ImportGermplasmListController extends SettingsController {

	/** The Constant URL. */
	public static final String URL = "/StudyManager/importGermplasmList";
	static final String URL_1 = "/TrialManager/GermplasmList";
	static final String URL_2 = "/ListManager/GermplasmList";

	/** The data import service. */
	@Resource
	private DataImportService dataImportService;

	/** The message source. */
	@Autowired
	public MessageSource messageSource;

	@Override
	public String getContentName() {
		return "StudyManager/importGermplasmList";
	}

	/**
	 * Gets the user selection.
	 *
	 * @return the user selection
	 */
	public UserSelection getUserSelection() {
		return this.userSelection;
	}

	/**
	 * Goes to the Next screen. Added validation if a germplasm list was
	 * properly uploaded
	 *
	 * @param form
	 *            the form
	 * @param result
	 *            the result
	 * @param model
	 *            the model
	 * @return the string
	 * @throws MiddlewareQueryException
	 *             the middleware query exception
	 */
	@ResponseBody
	@RequestMapping(value = { "/next", "/submitAll" }, method = RequestMethod.POST)
	@Transactional
	public String nextScreen(@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form,
			final BindingResult result, final Model model, final HttpServletRequest req) {
		this.userSelection.setMeasurementRowList(null);
		this.userSelection.getWorkbook().setOriginalObservations(null);
		this.userSelection.getWorkbook().setObservations(new ArrayList<>());

		for (final MeasurementVariable mv : userSelection.getWorkbook().getFactors()) {
			//Set Factor's Operation to null since it's autosaved
			mv.setOperation(null);
		}
		this.fieldbookService.createIdCodeNameVariablePairs(this.userSelection.getWorkbook(),
				AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());
		this.fieldbookService.createIdNameVariablePairs(this.userSelection.getWorkbook(),
				new ArrayList<SettingDetail>(), AppConstants.ID_NAME_COMBINATION.getString(), true);
		final int studyId = this.dataImportService.saveDataset(this.userSelection.getWorkbook(), true,
				false, this.getCurrentProject().getUniqueID(),
				this.getCurrentProject().getCropType());
		this.fieldbookService.saveStudyColumnOrdering(studyId,
			form.getColumnOrders(), this.userSelection.getWorkbook());

		return Integer.toString(studyId);
	}

	@ModelAttribute("contextInfo")
	public ContextInfo getContextInfo() {
		return this.contextUtil.getContextInfoFromSession();
	}

}
