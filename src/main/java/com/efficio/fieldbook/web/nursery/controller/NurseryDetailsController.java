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

package com.efficio.fieldbook.web.nursery.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.NurseryDetailsForm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * The Class NurseryDetailsController.
 */
@Controller
@RequestMapping(NurseryDetailsController.URL)
public class NurseryDetailsController extends AbstractBaseFieldbookController {

	/** The Constant URL. */
	public static final String URL = "/NurseryManager/nurseryDetails";

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(NurseryDetailsController.class);

	/** The ontology service. */
	@Resource
	private OntologyService ontologyService;

	/** The user selection. */
	@Resource
	private UserSelection userSelection;
	
	@Resource
	private ContextUtil contextUtil;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
	 */
	@Override
	public String getContentName() {
		return "NurseryManager/nurseryDetails";
	}

	/**
	 * Sets the user selection.
	 *
	 * @param userSelection the new user selection
	 */
	public void setUserSelection(UserSelection userSelection) {
		this.userSelection = userSelection;
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
	 * Shows the screen.
	 *
	 * @param form the form
	 * @param result the result
	 * @param model the model
	 * @param session the session
	 * @return the string
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String show(@ModelAttribute("nurseryDetailsForm") NurseryDetailsForm form, BindingResult result, Model model, HttpSession session) {
		if (this.userSelection.getWorkbook() == null) {
			result.reject("form.workbook", "Error occurred while parsing file.");
			this.userSelection.setWorkbook(new Workbook());
		}

		// Get the values of conditions from Workbook
		List<MeasurementVariable> conditions = this.userSelection.getWorkbook().getConditions();
		List<String> values = new ArrayList<String>();
		for (MeasurementVariable condition : conditions) {
			values.add(condition.getValue());
		}
		form.setValues(values);

		form.setWorkbook(this.userSelection.getWorkbook());
		return super.show(model);
	}

	@ResponseBody
	@RequestMapping(value = "/showPlantHeightInfo/{id}", method = RequestMethod.GET)
	public Map<String, String> getVariableDetails(@PathVariable String id) {
		Map<String, String> result = new HashMap<String, String>();
		try {
			if (NurseryDetailsController.isNumeric(id)) {
				result.put("stdVar", FieldbookUtil.convertEnumerationsAndStandardVariableToJSON
						(null, this.ontologyService.getStandardVariable(Integer.parseInt(id), contextUtil.getCurrentProgramUUID())));
			} else {
				// this part should be commented out when id is already used
				List<StandardVariable> stdVariables = this.ontologyService.getStandardVariables(id,
						contextUtil.getCurrentProgramUUID());

				if (stdVariables != null && !stdVariables.isEmpty()) {
					result.put("stdVar", FieldbookUtil.convertEnumerationsAndStandardVariableToJSON(null, stdVariables.get(0)));
				} else {
					result.put("stdVar", null);
				}
			}
			result.put("error", "0");
		} catch (MiddlewareException e) {
			result.put("error", "1");
			NurseryDetailsController.LOG.error(e.getMessage(), e);
		}
		return result;
	}

	private static boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	/**
	 * Submits the details.
	 *
	 * @param form the form
	 * @param result the result
	 * @param model the model
	 * @return the string
	 */
	@RequestMapping(method = RequestMethod.POST)
	public String submitDetails(@ModelAttribute("nurseryDetailsForm") NurseryDetailsForm form, BindingResult result, Model model) {
		this.userSelection.setFieldLayoutRandom(form.getFieldLayoutRandom());

		// Set the values of conditions
		List<String> values = form.getValues();
		Workbook workbook = this.userSelection.getWorkbook();
		List<MeasurementVariable> conditions = workbook.getConditions();
		for (MeasurementVariable condition : conditions) {
			condition.setValue(values.get(conditions.indexOf(condition)));
		}
		workbook.setConditions(conditions);
		this.userSelection.setWorkbook(workbook);

		return "redirect:" + ImportGermplasmListController.URL;
	}

}
