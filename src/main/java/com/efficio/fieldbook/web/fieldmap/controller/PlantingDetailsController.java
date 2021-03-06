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

package com.efficio.fieldbook.web.fieldmap.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.service.api.FieldMapService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.fieldmap.bean.Plot;
import com.efficio.fieldbook.web.fieldmap.bean.SelectedFieldmapList;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.fieldmap.form.FieldmapForm;
import com.efficio.fieldbook.web.label.printing.service.FieldPlotLayoutIterator;

/**
 * The Class PlantingDetailsController.
 */
@Controller
@RequestMapping({PlantingDetailsController.URL})
public class PlantingDetailsController extends AbstractBaseFieldbookController {

	private static final Logger LOG = LoggerFactory.getLogger(PlantingDetailsController.class);

	/** The Constant URL. */
	public static final String URL = "/Fieldmap/plantingDetails";

	/** The user selection. */
	@Resource
	private UserFieldmap userFieldmap;

	@Resource
	private FieldMapService fieldmapService;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private FieldPlotLayoutIterator horizontalFieldMapLayoutIterator;

	/**
	 * Show.
	 *
	 * @param form the form
	 * @param model the model
	 * @param session the session
	 * @return the string
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String show(@ModelAttribute("fieldmapForm") FieldmapForm form, Model model, HttpSession session) {
		try {
			this.setPrevValues(form);

			List<FieldMapInfo> infos = this.fieldbookMiddlewareService.getAllFieldMapsInBlockByBlockId(this.userFieldmap.getBlockId());
			if (this.userFieldmap.getSelectedFieldMapsToBeAdded() == null) {
				this.userFieldmap.setSelectedFieldMapsToBeAdded(new ArrayList<FieldMapInfo>(this.userFieldmap.getSelectedFieldMaps()));
			}

			// this is to add the new nusery
			List<FieldMapInfo> fieldmapInfoList = new ArrayList<FieldMapInfo>();
			List<FieldMapInfo> toBeAdded = this.userFieldmap.getSelectedFieldMapsToBeAdded();
			fieldmapInfoList.addAll(toBeAdded);

			if (infos != null && !infos.isEmpty()) {
				this.setOrder(infos);
				fieldmapInfoList.addAll(infos);
			}
			this.setOrder(toBeAdded, infos.size());

			this.userFieldmap.setSelectedFieldMaps(fieldmapInfoList);
			this.userFieldmap.setSelectedFieldmapList(new SelectedFieldmapList(this.userFieldmap.getSelectedFieldMaps()));
			this.userFieldmap.setSelectedFieldmapListToBeAdded(new SelectedFieldmapList(this.userFieldmap.getSelectedFieldMapsToBeAdded()));
			this.userFieldmap.setFieldMapLabels(this.userFieldmap.getAllSelectedFieldMapLabels(false));
			FieldPlotLayoutIterator plotIterator = this.horizontalFieldMapLayoutIterator;

			FieldMapTrialInstanceInfo trialInfo = this.userFieldmap.getAnySelectedTrialInstance();

			if (this.userFieldmap.getFieldmap() != null) {
				this.plotCleanup();
			}

			if (infos != null && !infos.isEmpty()) {
				if (trialInfo != null) {
					if (this.userFieldmap.getFieldmap() == null) {
						this.userFieldmap.setFieldmap(this.fieldmapService.generateFieldmap(this.userFieldmap, plotIterator, true,
								trialInfo.getDeletedPlots()));
					} else {
						// data clean up
						this.plotCleanup();
					}

					this.userFieldmap.setNumberOfRangesInBlock(trialInfo.getRangesInBlock());
					this.userFieldmap.setNumberOfRowsInBlock(trialInfo.getRowsInBlock());
					this.userFieldmap.setNumberOfEntries((long) this.userFieldmap.getAllSelectedFieldMapLabels(false).size());
					this.userFieldmap.setNumberOfRowsPerPlot(trialInfo.getRowsPerPlot());
					this.userFieldmap.setPlantingOrder(trialInfo.getPlantingOrder());
					this.userFieldmap.setFieldMapLabels(this.userFieldmap.getAllSelectedFieldMapLabels(false));
					this.userFieldmap.setMachineRowCapacity(trialInfo.getMachineRowCapacity());

				}
			}

			this.userFieldmap.setStartingColumn(1);
			this.userFieldmap.setStartingRange(1);
			if (infos == null || infos.isEmpty()) {
				// meaning no plants pa
				this.userFieldmap.setPlantingOrder(0);
				this.userFieldmap.setMachineRowCapacity(1);
			}

			form.setUserFieldmap(this.userFieldmap);

		} catch (Exception e) {
			PlantingDetailsController.LOG.error("Accessing the user field map was not successful", e);
		}
		return super.show(model);
	}

	private void plotCleanup() {
		Plot[][] currentPlot = this.userFieldmap.getFieldmap();
		for (int i = 0; i < currentPlot.length; i++) {
			for (int j = 0; j < currentPlot[i].length; j++) {
				Plot plot = currentPlot[i][j];
				if (!plot.isSavedAlready()) {
					// we reset the the plot
					if (plot.isPlotDeleted()) {
						plot.setPlotDeleted(false);
					} else {
						plot.setDisplayString("");
					}
				}
			}
		}
	}

	private void setPrevValues(FieldmapForm form) {
		UserFieldmap info = new UserFieldmap();
		info.setNumberOfRangesInBlock(this.userFieldmap.getNumberOfRangesInBlock());
		info.setNumberOfRowsInBlock(this.userFieldmap.getNumberOfRowsInBlock());
		info.setNumberOfRowsPerPlot(this.userFieldmap.getNumberOfRowsPerPlot());
		info.setLocationName(this.userFieldmap.getLocationName());
		info.setFieldName(this.userFieldmap.getFieldName());
		info.setBlockName(this.userFieldmap.getBlockName());
		this.userFieldmap.setStartingColumn(1);
		this.userFieldmap.setStartingRange(1);
		this.userFieldmap.setMachineRowCapacity(1);
		form.setUserFieldmap(info);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
	 */
	@Override
	public String getContentName() {
		return "Fieldmap/enterPlantingDetails";
	}

	/**
	 * Gets the user fieldmap.
	 *
	 * @return the user fieldmap
	 */
	public UserFieldmap getUserFieldmap() {
		return this.userFieldmap;
	}

	private void setOrder(List<FieldMapInfo> info) {
		this.setOrder(info, -1);
	}

	private void setOrder(List<FieldMapInfo> info, int offset) {
		int order = 1;
		if (info != null && !info.isEmpty()) {
			for (FieldMapInfo rec : info) {
				for (FieldMapDatasetInfo dataset : rec.getDatasets()) {
					for (FieldMapTrialInstanceInfo trial : dataset.getTrialInstances()) {
						if (offset >= 0 && trial.getOrder() != null) {
							trial.setOrder(trial.getOrder() + offset);
						} else {
							trial.setOrder(order++);
						}
					}
				}
			}
		}
	}

}
