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

package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.StudyDetails;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.AddOrRemoveTraitsForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;

@Controller
@RequestMapping(ReviewStudyDetailsController.URL)
public class ReviewStudyDetailsController extends AbstractBaseFieldbookController {

	public static final String URL = "/StudyManager/reviewStudyDetails";

	private static final Logger LOG = LoggerFactory.getLogger(ReviewStudyDetailsController.class);

	private static final int COLS = 3;

	@Resource
	private UserSelection userSelection;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Resource
	private ErrorHandlerService errorHandlerService;

	@Resource
	private Properties appConstantsProperties;

	@Override
	public String getContentName() {
		return this.getContentName(this.userSelection.isTrial());
	}

	private String getContentName(final boolean isTrial) {
		if (isTrial) {
			return "TrialManager/reviewTrialDetails";
		} else {
			return "NurseryManager/reviewNurseryDetails";
		}
	}

	@RequestMapping(value = "/show/{studyType}/{id}", method = RequestMethod.GET)
	public String show(@PathVariable final String studyType, @PathVariable final int id,
			@ModelAttribute("addOrRemoveTraitsForm") final AddOrRemoveTraitsForm form, final Model model) {

		final boolean isNursery = studyType != null && StudyType.N.getName().equalsIgnoreCase(studyType);
		final Workbook workbook;
		StudyDetails details;
		try {
			workbook = this.fieldbookMiddlewareService.getStudyVariableSettings(id, isNursery);
			workbook.getStudyDetails().setId(id);
			this.filterAnalysisVariable(workbook);
			details =
					SettingsUtil.convertWorkbookToStudyDetails(workbook, this.fieldbookMiddlewareService, this.fieldbookService,
							this.userSelection, this.contextUtil.getCurrentProgramUUID(), this.appConstantsProperties);
			this.rearrangeDetails(details);
			this.getPaginationListSelection().addReviewWorkbook(Integer.toString(id), workbook);
			if (workbook.getMeasurementDatesetId() != null) {
				details.setHasMeasurements(this.fieldbookMiddlewareService.countObservations(workbook.getMeasurementDatesetId()) > 0);
			} else {
				details.setHasMeasurements(false);
			}

		} catch (final MiddlewareException e) {
			ReviewStudyDetailsController.LOG.error(e.getMessage(), e);
			details = new StudyDetails();
			this.addErrorMessageToResult(details, e, isNursery, id);
		}

		if (isNursery) {
			model.addAttribute("nurseryDetails", details);
		} else {
			model.addAttribute("trialDetails", details);
		}

		return this.showAjaxPage(model, this.getContentName(!isNursery));
	}

	protected void addErrorMessageToResult(final StudyDetails details, final MiddlewareException e, final boolean isNursery, final int id) {
		final String param;
		if (isNursery) {
			param = AppConstants.NURSERY.getString();
		} else {
			param = AppConstants.TRIAL.getString();
		}
		details.setId(id);
		String errorMessage = e.getMessage();
		if (e instanceof MiddlewareQueryException) {
			errorMessage =
					this.errorHandlerService.getErrorMessagesAsString(((MiddlewareQueryException) e).getCode(), new Object[] {param,
							param.substring(0, 1).toUpperCase().concat(param.substring(1, param.length())), param}, "\n");
		}
		details.setErrorMessage(errorMessage);
	}

	@ResponseBody
	@RequestMapping(value = "/datasets/{nurseryId}")
	public List<DatasetReference> loadDatasets(@PathVariable final int nurseryId) {
		return this.fieldbookMiddlewareService.getDatasetReferences(nurseryId);
	}

	private void rearrangeDetails(final StudyDetails details) {
		details.setBasicStudyDetails(this.rearrangeSettingDetails(details.getBasicStudyDetails()));
		details.setManagementDetails(this.rearrangeSettingDetails(details.getManagementDetails()));
	}

	private List<SettingDetail> rearrangeSettingDetails(final List<SettingDetail> list) {
		final List<SettingDetail> newList = new ArrayList<SettingDetail>();

		if (list != null && !list.isEmpty()) {
			final int rows = Double.valueOf(Math.ceil(list.size() / (double) ReviewStudyDetailsController.COLS)).intValue();
			final int extra = list.size() % ReviewStudyDetailsController.COLS;
			for (int i = 0; i < list.size(); i++) {
				int delta = 0;
				final int currentColumn = i % ReviewStudyDetailsController.COLS;
				if (currentColumn > extra && extra > 0) {
					delta = currentColumn - extra;
				}
				final int computedIndex = currentColumn * rows + i / ReviewStudyDetailsController.COLS - delta;
				if (computedIndex < list.size()) {
					newList.add(list.get(computedIndex));
				} else {
					newList.add(list.get(computedIndex - 1));
				}
			}
		}
		return newList;
	}

	protected void setFieldbookMiddlewareService(final FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

}
