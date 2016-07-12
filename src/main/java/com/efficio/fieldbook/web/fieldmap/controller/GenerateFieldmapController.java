/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *******************************************************************************/

package com.efficio.fieldbook.web.fieldmap.controller;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.service.api.ExportExcelService;
import com.efficio.fieldbook.service.api.FieldMapService;
import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.fieldmap.bean.Plot;
import com.efficio.fieldbook.web.fieldmap.bean.SelectedFieldmapList;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.fieldmap.form.FieldmapForm;
import com.efficio.fieldbook.web.label.printing.service.FieldPlotLayoutIterator;
import com.efficio.fieldbook.web.nursery.controller.ManageNurseriesController;
import com.efficio.fieldbook.web.trial.controller.ManageTrialController;

/**
 * The Class GenerateFieldmapController.
 * <p/>
 * Generates the final fieldmap for the step 3.
 */
@Controller @RequestMapping({GenerateFieldmapController.URL}) public class GenerateFieldmapController
		extends AbstractBaseFieldbookController {

	/**
	 * The Constant URL.
	 */
	public static final String URL = "/Fieldmap/generateFieldmapView";
	public static final String REDIRECT = "redirect:";
	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(GenerateFieldmapController.class);
	/**
	 * The Constant BUFFER_SIZE.
	 */
	private static final int BUFFER_SIZE = 4096 * 4;
	/**
	 * The user fieldmap.
	 */
	@Resource
	private UserFieldmap userFieldmap;
	/**
	 * The fieldmap service.
	 */
	@Resource
	private FieldMapService fieldmapService;

	@Resource
	private FieldPlotLayoutIterator horizontalFieldMapLayoutIterator;
	/**
	 * The fieldbook middleware service.
	 */
	@Resource
	private FieldbookService fieldbookMiddlewareService;
	/**
	 * The export excel service.
	 */
	@Resource
	private ExportExcelService exportExcelService;

	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	/**
	 * Show generated fieldmap.
	 *
	 * @param form the form
	 * @param model the model
	 * @return the string
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String showGeneratedFieldmap(@ModelAttribute("fieldmapForm") FieldmapForm form, Model model) {

		form.setUserFieldmap(this.userFieldmap);

		return super.show(model);
	}

	/**
	 * View fieldmap.
	 *
	 * @param form the form
	 * @param model the model
	 * @param datasetId the dataset id
	 * @param geolocationId the geolocation id
	 * @param studyType the study type
	 * @return the string
	 */
	@RequestMapping(value = "/viewFieldmap/{studyType}/{datasetId}/{geolocationId}", method = RequestMethod.GET)
	public String viewFieldmap(@ModelAttribute("fieldmapForm") FieldmapForm form, Model model, @PathVariable Integer datasetId,
			@PathVariable Integer geolocationId, @PathVariable String studyType) {
		try {

			this.userFieldmap.setSelectedDatasetId(datasetId);
			this.userFieldmap.setSelectedGeolocationId(geolocationId);

			this.userFieldmap.setSelectedFieldMaps(this.fieldbookMiddlewareService
					.getAllFieldMapsInBlockByTrialInstanceId(datasetId, geolocationId, this.crossExpansionProperties));

			FieldMapTrialInstanceInfo trialInfo =
					this.userFieldmap.getSelectedTrialInstanceByDatasetIdAndGeolocationId(datasetId, geolocationId);
			if (trialInfo != null) {
				this.userFieldmap.setNumberOfRangesInBlock(trialInfo.getRangesInBlock());
				this.userFieldmap.setNumberOfRowsInBlock(trialInfo.getRowsInBlock());
				this.userFieldmap.setNumberOfEntries((long) this.userFieldmap.getAllSelectedFieldMapLabels(false).size());
				this.userFieldmap.setNumberOfRowsPerPlot(trialInfo.getRowsPerPlot());
				this.userFieldmap.setPlantingOrder(trialInfo.getPlantingOrder());
				this.userFieldmap.setBlockName(trialInfo.getBlockName());
				this.userFieldmap.setFieldName(trialInfo.getFieldName());
				this.userFieldmap.setLocationName(trialInfo.getLocationName());
				this.userFieldmap.setFieldMapLabels(this.userFieldmap.getAllSelectedFieldMapLabels(false));
				this.userFieldmap.setTrial("trial".equals(studyType));
				this.userFieldmap.setMachineRowCapacity(trialInfo.getMachineRowCapacity());

				FieldPlotLayoutIterator plotIterator = this.horizontalFieldMapLayoutIterator;
				this.userFieldmap.setFieldmap(
						this.fieldmapService.generateFieldmap(this.userFieldmap, plotIterator, false, trialInfo.getDeletedPlots()));
			}
			this.userFieldmap.setSelectedFieldmapList(
					new SelectedFieldmapList(this.userFieldmap.getSelectedFieldMaps(), this.userFieldmap.isTrial()));
			this.userFieldmap.setGenerated(false);
			form.setUserFieldmap(this.userFieldmap);

		} catch (MiddlewareQueryException e) {
			GenerateFieldmapController.LOG.error(e.getMessage(), e);
		}
		return super.show(model);
	}

	@RequestMapping(value = "/exportExcel", method = RequestMethod.GET)
	public ResponseEntity<FileSystemResource> exportExcel(HttpServletRequest request)
			throws UnsupportedEncodingException, FieldbookException {

		// changed selected name to block name for now
		String fileName = this.makeSafeFileName(this.userFieldmap.getBlockName());

		this.exportExcelService.exportFieldMapToExcel(fileName, this.userFieldmap);

		return FieldbookUtil.createResponseEntityForFileDownload(new File(fileName));
	}

	protected String makeSafeFileName(String filename) {
		return filename.replace(" ", "") + "-" + DateUtil.getCurrentDateAsStringValue() + ".xls";
	}

	/**
	 * Submits the details.
	 *
	 * @param form the form
	 * @param model the model
	 * @return the string
	 */
	@RequestMapping(method = RequestMethod.POST)
	public String submitDetails(@ModelAttribute("FieldmapForm") FieldmapForm form, Model model) {

		this.userFieldmap.setStartingColumn(form.getUserFieldmap().getStartingColumn());
		this.userFieldmap.setStartingRange(form.getUserFieldmap().getStartingRange());
		this.userFieldmap.setPlantingOrder(form.getUserFieldmap().getPlantingOrder());
		this.userFieldmap.setMachineRowCapacity(form.getUserFieldmap().getMachineRowCapacity());

		int startRange = this.userFieldmap.getStartingRange() - 1;
		int startCol = this.userFieldmap.getStartingColumn() - 1;
		int rows = this.userFieldmap.getNumberOfRowsInBlock();
		int ranges = this.userFieldmap.getNumberOfRangesInBlock();
		int rowsPerPlot = this.userFieldmap.getNumberOfRowsPerPlot();
		boolean isSerpentine = this.userFieldmap.getPlantingOrder() == 2;

		int col = rows / rowsPerPlot;
		// should list here the deleted plot in col-range format
		Map<String, String> deletedPlot = new HashMap<String, String>();
		if (form.getMarkedCells() != null && !form.getMarkedCells().isEmpty()) {
			List<String> markedCells = Arrays.asList(form.getMarkedCells().split(","));

			for (String markedCell : markedCells) {
				deletedPlot.put(markedCell, markedCell);
			}
		}

		this.markDeletedPlots(form, form.getMarkedCells());

		List<FieldMapLabel> labels = this.userFieldmap.getAllSelectedFieldMapLabelsToBeAdded(true);

		// we can add logic here to decide if its vertical or horizontal
		FieldPlotLayoutIterator plotIterator = this.horizontalFieldMapLayoutIterator;
		Plot[][] plots = plotIterator
				.createFieldMap(col, ranges, startRange, startCol, isSerpentine, deletedPlot, labels, this.userFieldmap.isTrial(),
						this.userFieldmap.getFieldmap());
		this.userFieldmap.setFieldmap(plots);
		form.setUserFieldmap(this.userFieldmap);

		this.userFieldmap.setGenerated(true);

		return GenerateFieldmapController.REDIRECT + GenerateFieldmapController.URL;
	}

	/**
	 * Redirect to main screen.
	 *
	 * @param form the form
	 * @param model the model
	 * @return the string
	 */
	@RequestMapping(value = "/showMainPage", method = RequestMethod.GET)
	public String redirectToMainScreen(@ModelAttribute("fieldmapForm") FieldmapForm form, Model model) {

		if (this.userFieldmap.isTrial()) {
			return GenerateFieldmapController.REDIRECT + ManageTrialController.URL;
		} else {
			return GenerateFieldmapController.REDIRECT + ManageNurseriesController.URL;
		}
	}

	/**
	 * Gets the user fieldmap.
	 *
	 * @return the user fieldmap
	 */
	public UserFieldmap getUserFieldmap() {
		return this.userFieldmap;
	}

	/**
	 * Sets the user fieldmap.
	 *
	 * @param userFieldmap the new user fieldmap
	 */
	public void setUserFieldmap(UserFieldmap userFieldmap) {
		this.userFieldmap = userFieldmap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
	 */
	@Override
	public String getContentName() {
		return "Fieldmap/generateFieldmapView";
	}

	private void markDeletedPlots(FieldmapForm form, String deletedPlots) {
		List<String> dpform = new ArrayList<String>();
		if (deletedPlots != null) {
			String[] dps = deletedPlots.split(",");
			for (String deletedPlot : dps) {
				String[] coordinates = deletedPlot.split("_");
				if (coordinates.length == 2 && NumberUtils.isNumber(coordinates[0]) && NumberUtils.isNumber(coordinates[1])) {
					dpform.add(coordinates[0] + "," + coordinates[1]);
				}
			}
		}
		this.userFieldmap.setDeletedPlots(dpform);
	}
}
