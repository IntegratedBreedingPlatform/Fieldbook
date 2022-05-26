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

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.fieldmap.bean.FieldmapRequestDto;
import com.efficio.fieldbook.web.fieldmap.bean.SelectedFieldmapList;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.fieldmap.form.FieldmapForm;
import com.efficio.fieldbook.web.util.SessionUtility;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.fieldbook.FieldmapBlockInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The Class FieldmapController.
 * <p>
 * This is the initial controller for the fieldmap generation. It handles the step 1 of 3 of the fieldmap process.
 */
@Controller
@RequestMapping({FieldmapController.URL})
public class FieldmapController extends AbstractBaseFieldbookController {

	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(FieldmapController.class);

	/**
	 * The Constant URL.
	 */
	public static final String URL = "/Fieldmap/enterFieldDetails";

	/**
	 * The user field map.
	 */
	@Resource
	private UserFieldmap userFieldmap;

	/**
	 * The fieldbook middleware service.
	 */
	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	@ModelAttribute("cropName")
	public String getCropName() {
		return this.contextUtil.getProjectInContext().getCropType().getCropName();
	}

	@ModelAttribute("currentProgramId")
	public String getCurrentProgramId() {
		return this.contextUtil.getProjectInContext().getUniqueID();
	}

	/**
	 * Determine field map navigation.
	 *
	 * @param ids     the ids
	 * @param model   the model
	 * @param session the session
	 * @return the map
	 */
	@ResponseBody
	@RequestMapping(value = "/createFieldmap/{ids}", method = RequestMethod.GET)
	public Map<String, String> determineFieldMapNavigation(@PathVariable final String ids, final Model model, final HttpServletRequest req,
		final HttpSession session) {

		SessionUtility.clearSessionData(session, new String[] {
			SessionUtility.FIELDMAP_SESSION_NAME,
			SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});
		final Map<String, String> result = new HashMap<>();
		String nav = "1";
		try {
			final List<Integer> trialIds = Arrays.stream(ids.split(",")).map(Integer::valueOf).collect(Collectors.toList());
			final List<FieldMapInfo> fieldMapInfoList =
				this.fieldbookMiddlewareService.getFieldMapInfoOfTrial(trialIds, this.crossExpansionProperties);

			this.clearFields();
			this.userFieldmap.setUserFieldmapInfo(fieldMapInfoList);
			// get trial instances with field map
			for (final FieldMapInfo fieldMapInfo : fieldMapInfoList) {
				final List<FieldMapDatasetInfo> datasetList = fieldMapInfo.getDatasetsWithFieldMap();
				if (datasetList != null && !datasetList.isEmpty()) {
					final List<FieldMapTrialInstanceInfo> trials = datasetList.get(0).getTrialInstancesWithFieldMap();
					if (trials != null && !trials.isEmpty()) {
						nav = "0";
					}
				}
			}
		} catch (final MiddlewareQueryException e) {
			FieldmapController.LOG.error(e.getMessage(), e);
		}
		result.put("nav", nav);

		return result;
	}

	/**
	 * Gets the field map info data.
	 *
	 * @return the field map info data
	 */
	@ResponseBody
	@RequestMapping(value = "/selectTrialInstance", method = RequestMethod.GET)
	public Map<String, String> getFieldMapInfoData() {
		final Map<String, String> result = new HashMap<>();
		final List<FieldMapInfo> fieldMapInfoList = this.userFieldmap.getFieldMapInfo();
		String size = "0";
		String datasetId = null;
		String environmentId = null;
		final String fieldMapInfoJson;
		boolean hasInvalidValues = false;
		for (final FieldMapInfo fieldMapInfo : fieldMapInfoList) {
			// for viewing of fieldmaps
			final List<FieldMapDatasetInfo> datasetList = fieldMapInfo.getDatasetsWithFieldMap();
			if (!hasInvalidValues) {
				hasInvalidValues = this.validateFieldMapDataSetInfo(datasetList);
			}
			if (datasetList != null && !datasetList.isEmpty()) {
				final List<FieldMapTrialInstanceInfo> trials = datasetList.get(0).getTrialInstancesWithFieldMap();
				if (trials != null && !trials.isEmpty()) {
					size = String.valueOf(trials.size());
					if (trials.size() == 1) {
						datasetId = datasetList.get(0).getDatasetId().toString();
						environmentId = trials.get(0).getInstanceId().toString();
					}
				}
			}
		}

		fieldMapInfoJson = this.convertFieldMapInfoToJson(fieldMapInfoList);

		result.put("fieldMapInfo", fieldMapInfoJson);
		result.put("size", size);
		result.put("hasInvalidValues", String.valueOf(hasInvalidValues));
		if (datasetId != null) {
			result.put("datasetId", datasetId);
		}
		if (environmentId != null) {
			result.put("environmentId", environmentId);
		}
		return result;
	}

	/**
	 * Convert field map info to json.
	 *
	 * @param fieldMapInfo the field map info
	 * @return the string
	 */
	private String convertFieldMapInfoToJson(final List<FieldMapInfo> fieldMapInfo) {
		if (fieldMapInfo != null) {
			try {
				final ObjectMapper mapper = new ObjectMapper();
				return mapper.writeValueAsString(fieldMapInfo);
			} catch (final Exception e) {
				FieldmapController.LOG.error(e.getMessage(), e);
			}
		}
		return "";
	}

	/**
	 * Show trial.
	 *
	 * @param form the form
	 * @param id the id
	 * @param model the model
	 * @param session the session
	 * @return the string
	 */
	@Deprecated
	@RequestMapping(value = "/trial/{id}", method = RequestMethod.GET)
	public String showTrial(@ModelAttribute("fieldmapForm") final FieldmapForm form, @PathVariable
	final String id, final Model model, final HttpSession session) {
		try {
			this.setSelectedFieldMapInfo(id);
			form.setUserFieldmap(this.userFieldmap);
			form.setProjectId(this.getCurrentProjectId());
		} catch (final NumberFormatException e) {
			FieldmapController.LOG.error(e.toString());
		}
		form.setProgramLocationUrl(this.fieldbookProperties.getProgramLocationsUrl());
		return super.show(model);
	}

	/**
	 * Sets the selected field map info.
	 *
	 * @param id the id
	 */
	private void setSelectedFieldMapInfo(final String id) {
		final String[] groupId = id.split(",");
		final List<FieldMapInfo> fieldMapInfoList = this.userFieldmap.getFieldMapInfo();

		final List<FieldMapInfo> selectedFieldMapInfoList = new ArrayList<>();
		FieldMapInfo newFieldMapInfo = null;
		List<FieldMapDatasetInfo> datasets = null;
		FieldMapDatasetInfo dataset = null;
		List<FieldMapTrialInstanceInfo> trialInstances = null;
		FieldMapTrialInstanceInfo trialInstance;

		Integer studyId = null;
		Integer datasetId = null;
		String fieldbookName = null;
		String datasetName = null;
		// build the selectedFieldMaps
		for (final String group : groupId) {
			final String[] ids = group.split("\\|");
			final int selectedStudyId = Integer.parseInt(ids[0]);
			final int selectedDatasetId = Integer.parseInt(ids[1]);
			final int selectedInstanceId = Integer.parseInt(ids[2]);

			for (final FieldMapInfo fieldMapInfo : fieldMapInfoList) {
				// if current study id is equal to the selected study id
				if (fieldMapInfo.getFieldbookId().equals(selectedStudyId)) {
					if (datasetId == null) {
						dataset = new FieldMapDatasetInfo();
						trialInstances = new ArrayList<>();
					} else {
						// if dataset has changed, add previously saved dataset to the list
						if (!datasetId.equals(selectedDatasetId)) {
							dataset.setDatasetId(datasetId);
							dataset.setDatasetName(datasetName);
							dataset.setTrialInstances(trialInstances);
							datasets.add(dataset);
							dataset = new FieldMapDatasetInfo();
							trialInstances = new ArrayList<>();
						}
					}

					if (studyId == null) {
						newFieldMapInfo = new FieldMapInfo();
						datasets = new ArrayList<>();
					} else {
						// if study id has changed, add previously saved study to the list
						if (!studyId.equals(selectedStudyId)) {
							newFieldMapInfo.setFieldbookId(studyId);
							newFieldMapInfo.setFieldbookName(fieldbookName);
							newFieldMapInfo.setDatasets(datasets);
							selectedFieldMapInfoList.add(newFieldMapInfo);
							newFieldMapInfo = new FieldMapInfo();
							datasets = new ArrayList<>();
						}
					}

					trialInstance = fieldMapInfo.getDataSet(selectedDatasetId).getTrialInstance(selectedInstanceId);
					trialInstances.add(trialInstance);

					datasetId = selectedDatasetId;
					studyId = selectedStudyId;
					datasetName = fieldMapInfo.getDataSet(datasetId).getDatasetName();
					fieldbookName = fieldMapInfo.getFieldbookName();
				}
			}
		}
		// add last dataset and study to the list
		dataset.setDatasetId(datasetId);
		dataset.setDatasetName(datasetName);
		dataset.setTrialInstances(trialInstances);
		assert datasets != null;
		datasets.add(dataset);

		newFieldMapInfo.setFieldbookId(studyId);
		newFieldMapInfo.setFieldbookName(fieldbookName);
		newFieldMapInfo.setDatasets(datasets);
		selectedFieldMapInfoList.add(newFieldMapInfo);

		this.userFieldmap.setSelectedFieldMaps(selectedFieldMapInfoList);

	}

	/**
	 * Determine nursery field map navigation.
	 *
	 * @param ids the ids
	 * @param session the session
	 * @return the map
	 */
	@Deprecated
	@ResponseBody
	@RequestMapping(value = "/createNurseryFieldmap/{ids}", method = RequestMethod.GET)
	public Map<String, String> determineNurseryFieldMapNavigation(@PathVariable
	final String ids, final HttpServletRequest req, final HttpSession session) {
		SessionUtility.clearSessionData(session, new String[] {SessionUtility.FIELDMAP_SESSION_NAME,
				SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});
		final Map<String, String> result = new HashMap<>();

		String nav = "1";
		try {
			final List<Integer> nurseryIds = new ArrayList<>();
			final String[] idList = ids.split(",");
			for (final String id : idList) {
				nurseryIds.add(Integer.parseInt(id));
			}

			this.clearFields();
			final List<FieldMapInfo> fieldMapInfoList =
					this.fieldbookMiddlewareService.getFieldMapInfoOfNursery(nurseryIds, this.crossExpansionProperties);

			this.userFieldmap.setUserFieldmapInfo(fieldMapInfoList);

			for (final FieldMapInfo fieldMapInfo : fieldMapInfoList) {
				final List<FieldMapDatasetInfo> datasetList = fieldMapInfo.getDatasetsWithFieldMap();
				if (datasetList != null && !datasetList.isEmpty()) {
					final List<FieldMapTrialInstanceInfo> trials = datasetList.get(0).getTrialInstancesWithFieldMap();
					if (trials != null && !trials.isEmpty()) {
						final FieldMapDatasetInfo dataset = datasetList.get(0);
						nav = "0";
						this.userFieldmap.setSelectedDatasetId(dataset.getDatasetId());
						this.userFieldmap.setSelectedInstanceId(dataset.getTrialInstancesWithFieldMap().get(0).getInstanceId());
						result.put("datasetId", this.userFieldmap.getSelectedDatasetId().toString());
						result.put("instanceId", this.userFieldmap.getSelectedInstanceId().toString());
					}
				}
			}
		} catch (final MiddlewareQueryException e) {
			FieldmapController.LOG.error(e.getMessage(), e);
		}
		result.put("nav", nav);
		return result;
	}

	/**
	 * Show nursery.
	 *
	 * @param form the form
	 * @param id the id
	 * @param model the model
	 * @param session the session
	 * @return the string
	 */
	@RequestMapping(value = "/nursery/{studyId}/{id}", method = RequestMethod.GET) //TODO CAMBIAR URL AND METHOD NAME
	public String showNursery(@ModelAttribute("fieldmapForm") final FieldmapForm form, @PathVariable final Integer studyId, @PathVariable final String id, final Model model, final HttpSession session) {
		try {
			this.setSelectedFieldMapInfo(id);
			form.setUserFieldmap(this.userFieldmap);
			form.setProjectId(this.getCurrentProjectId());
		} catch (final NumberFormatException e) {
			FieldmapController.LOG.error(e.toString());
		}
		form.setProgramLocationUrl(this.fieldbookProperties.getProgramLocationsUrl());
		form.getUserFieldmap().setStudyId(studyId);
		return super.show(model);
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
	public String submitDetails(@ModelAttribute("fieldmapForm") final FieldmapForm form, final BindingResult result, final Model model) {
		this.setTrialInstanceOrder(form);
		if (form.getUserFieldmap().getFieldmap() != null) {
			form.getUserFieldmap().setFieldmap(null);
		}
		this.userFieldmap.setFieldmap(null);
		this.userFieldmap.setFieldMapLabels(null);

		if (this.userFieldmap.getSelectedFieldMapsToBeAdded() != null) {
			for (final FieldMapInfo info : this.userFieldmap.getSelectedFieldMapsToBeAdded()) {
				for (final FieldMapDatasetInfo dataset : info.getDatasets()) {
					for (final FieldMapTrialInstanceInfo trial : dataset.getTrialInstances()) {
						trial.clearColumnRangeIfExists();
					}
				}
			}
		}

		this.setUserFieldMapDetails(form);
		return "redirect:" + PlantingDetailsController.URL;
	}

	/**
	 * Sets the trial instance order.
	 *
	 * @param form the new trial instance order
	 */
	private void setTrialInstanceOrder(final FieldmapForm form) {
		final String[] orderList = form.getUserFieldmap().getOrder().split(",");
		final List<FieldMapInfo> fieldMapInfoList = this.userFieldmap.getSelectedFieldMaps();
		for (final String order : orderList) {
			final String[] ids = order.split("\\|");
			final int orderId;
			final int fieldbookId;
			final int datasetId;
			final int instanceId;
			int ctr = 0;
			orderId = Integer.parseInt(ids[0]);
			fieldbookId = Integer.parseInt(ids[1]);
			datasetId = Integer.parseInt(ids[2]);
			instanceId = Integer.parseInt(ids[3]);
			for (final FieldMapInfo fieldMapInfo : fieldMapInfoList) {
				if (fieldMapInfo.getFieldbookId().equals(fieldbookId)) {
					this.userFieldmap.getSelectedFieldMaps().get(ctr).getDataSet(datasetId).getTrialInstance(instanceId)
						.setOrder(orderId);
					break;
				}
				ctr++;
			}
		}
		this.userFieldmap.setSelectedFieldmapList(new SelectedFieldmapList(this.userFieldmap.getSelectedFieldMaps()));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
	 */
	@Override
	public String getContentName() {
		return "Fieldmap/enterFieldDetails";
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
	 * Gets the locations.
	 *
	 * @param locationId the location id
	 * @return the locations
	 */
	@ResponseBody
	@RequestMapping(value = "/getFields/{locationId}", method = RequestMethod.GET)
	public Map<String, String> getFieldLocations(@PathVariable final int locationId) {
		final Map<String, String> result = new HashMap<>();

		try {
			final List<Location> allFields = this.fieldbookMiddlewareService.getAllFieldLocations(locationId);
			result.put("success", "1");

			result.put("allFields", this.convertObjectToJson(allFields));
		} catch (final MiddlewareQueryException e) {
			FieldmapController.LOG.error(e.getMessage(), e);
			result.put("success", "-1");
		}

		return result;
	}

	/**
	 * Gets the locations.
	 *
	 * @param fieldId the field id
	 * @return the locations
	 */
	@ResponseBody
	@RequestMapping(value = "/getBlocks/{fieldId}", method = RequestMethod.GET)
	public Map<String, String> getBlockFields(@PathVariable final int fieldId) {
		final Map<String, String> result = new HashMap<>();

		try {

			final List<Location> allBlocks = this.fieldbookMiddlewareService.getAllBlockLocations(fieldId);
			result.put("success", "1");
			result.put("allBlocks", this.convertObjectToJson(allBlocks));

		} catch (final MiddlewareQueryException e) {
			FieldmapController.LOG.error(e.getMessage(), e);
			result.put("success", "-1");
		}

		return result;
	}

	/**
	 * Gets the block info.
	 *
	 * @param blockId the block id
	 * @return the block info
	 */
	@ResponseBody
	@RequestMapping(value = "/getBlockInformation/{blockId}", method = RequestMethod.GET)
	public Map<String, String> getBlockInfo(@PathVariable final int blockId) {
		final Map<String, String> result = new HashMap<>();

		try {

			final FieldmapBlockInfo blockInfo = this.fieldbookMiddlewareService.getBlockInformation(blockId);
			result.put("success", "1");
			result.put("blockInfo", this.convertObjectToJson(blockInfo));

		} catch (final MiddlewareQueryException e) {
			FieldmapController.LOG.error(e.getMessage(), e);
			result.put("success", "-1");
		}

		return result;
	}

	/**
	 * Submits the details.
	 *
	 * @param form the form
	 * @param result the result
	 * @param model the model
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/addNewField", method = RequestMethod.POST)
	public String addNewField(@ModelAttribute("fieldmapForm") final FieldmapForm form, final BindingResult result, final Model model) {
		final String fieldName = form.getNewFieldName();
		final Integer locationId = form.getParentLocationId();
		String msg = "success";
		try {
			final Integer currentUserId = this.getCurrentIbdbUserId();
			if (this.isFieldNameUnique(fieldName, locationId)) {
				this.fieldbookMiddlewareService.addFieldLocation(fieldName, locationId, currentUserId);
			} else {
				msg = "error";
			}
		} catch (final MiddlewareQueryException e) {
			FieldmapController.LOG.error(e.getMessage(), e);
		}
		return msg;
	}

	private boolean isFieldNameUnique(final String fieldName, final Integer locationId) {
		boolean isUnique = true;
		try {
			final List<Location> allFields = this.fieldbookMiddlewareService.getAllFieldLocations(locationId);
			if (allFields != null && !allFields.isEmpty()) {
				for (final Location loc : allFields) {
					if (fieldName.equalsIgnoreCase(loc.getLname())) {
						isUnique = false;
						break;
					}
				}
			}
		} catch (final MiddlewareQueryException e) {
			FieldmapController.LOG.error(e.getMessage(), e);
		}
		return isUnique;
	}

	private boolean isBlockNameUnique(final String blockName, final Integer fieldId) {
		boolean isUnique = true;
		try {
			final List<Location> allBlocks = this.fieldbookMiddlewareService.getAllBlockLocations(fieldId);
			if (allBlocks != null && !allBlocks.isEmpty()) {
				for (final Location loc : allBlocks) {
					if (blockName.equalsIgnoreCase(loc.getLname())) {
						isUnique = false;
						break;
					}
				}
			}
		} catch (final MiddlewareQueryException e) {
			FieldmapController.LOG.error(e.getMessage(), e);
		}
		return isUnique;
	}

	/**
	 * Adds the new block.
	 *
	 * @param form the form
	 * @param result the result
	 * @param model the model
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/addNewBlock", method = RequestMethod.POST)
	public String addNewBlock(@ModelAttribute("fieldmapForm") final FieldmapForm form, final BindingResult result, final Model model) {
		final String blockName = form.getNewBlockName();
		final Integer parentFieldId = form.getParentFieldId();
		String msg = "success";
		try {
			final Integer currentUserId = this.getCurrentIbdbUserId();
			if (this.isBlockNameUnique(blockName, parentFieldId)) {
				this.fieldbookMiddlewareService.addBlockLocation(blockName, parentFieldId, currentUserId);
			} else {
				msg = "error";
			}
		} catch (final MiddlewareQueryException e) {
			FieldmapController.LOG.error(e.getMessage(), e);
		}
		return msg;
	}

	/**
	 * Gets the field locations.
	 *
	 * @return the field locations
	 */
	@ResponseBody
	@RequestMapping(value = "/getFields", method = RequestMethod.GET)
	public Map<String, String> getFieldLocations() {
		final Map<String, String> result = new HashMap<>();

		try {
			final List<Location> allLocations = this.fieldbookMiddlewareService.getAllFields();
			result.put("success", "1");
			result.put("allFields", this.convertObjectToJson(allLocations));

		} catch (final MiddlewareQueryException e) {
			FieldmapController.LOG.error(e.getMessage(), e);
			result.put("success", "-1");
		}

		return result;
	}

	/**
	 * Delete field map.
	 */
	@ResponseBody
	@RequestMapping(value = "/deletion", method = RequestMethod.POST,
		produces = "application/json; charset=utf-8")
	public ResponseEntity<Void> deleteFieldMap(
		@RequestBody final FieldmapRequestDto requestDto) {
		this.fieldbookMiddlewareService.deleteAllFieldMapsByTrialInstanceIds(
			requestDto.getInstanceIds(), requestDto.getDatasetId(), requestDto.isAllExistingFieldmapSelected());

		final List<FieldMapDatasetInfo> datasets = this.userFieldmap.getFieldMapInfo().stream().flatMap(x -> x.getDatasets().stream())
			.collect(Collectors.toList());
		final List<FieldMapTrialInstanceInfo> instances = datasets.stream().flatMap(x -> x.getTrialInstances().stream())
			.collect(Collectors.toList());
		final List<FieldMapTrialInstanceInfo> filteredInstances = instances.stream().filter
			(trial -> requestDto.getInstanceIds().contains(trial.getInstanceId())).collect(Collectors.toList());

		filteredInstances.forEach(trial -> {
			trial.setHasFieldMap(false);
			trial.clearColumnRangeIfExists();
		});

		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * Sets the user field map details.
	 *
	 * @param form the new user field map details
	 */
	private void setUserFieldMapDetails(final FieldmapForm form) {
		this.userFieldmap.setSelectedDatasetId(form.getUserFieldmap().getSelectedDatasetId());
		this.userFieldmap.setSelectedInstanceId(form.getUserFieldmap().getSelectedInstanceId());
		this.userFieldmap.setUserFieldmapInfo(this.userFieldmap.getFieldMapInfo());
		this.userFieldmap.setNumberOfEntries(form.getUserFieldmap().getNumberOfEntries());
		this.userFieldmap.setNumberOfReps(form.getUserFieldmap().getNumberOfReps());
		this.userFieldmap.setTotalNumberOfPlots(form.getUserFieldmap().getTotalNumberOfPlots());
		this.userFieldmap.setBlockName(form.getUserFieldmap().getBlockName());
		this.userFieldmap.setFieldLocationId(form.getUserFieldmap().getFieldLocationId());
		this.userFieldmap.setFieldName(form.getUserFieldmap().getFieldName());
		this.userFieldmap.setNumberOfRangesInBlock(form.getUserFieldmap().getNumberOfRangesInBlock());
		this.userFieldmap.setNumberOfRowsInBlock(form.getUserFieldmap().getNumberOfRowsInBlock());
		this.userFieldmap.setLocationName(form.getUserFieldmap().getLocationName());
		this.userFieldmap.setNumberOfRowsPerPlot(form.getNumberOfRowsPerPlot());
		this.userFieldmap.setNew(form.getUserFieldmap().isNew());
		this.userFieldmap.setFieldId(form.getUserFieldmap().getFieldId());
		this.userFieldmap.setBlockId(form.getUserFieldmap().getBlockId());
		this.userFieldmap.setLocationName(form.getUserFieldmap().getLocationName());
		this.userFieldmap.setFieldName(form.getUserFieldmap().getFieldName());
		this.userFieldmap.setBlockName(form.getUserFieldmap().getBlockName());
		this.userFieldmap.setStudyId(form.getUserFieldmap().getStudyId());
	}

	/**
	 * Clear fields.
	 */
	private void clearFields() {
		if (this.userFieldmap != null) {
			this.userFieldmap.setBlockName("");
			this.userFieldmap.setFieldName("");
			this.userFieldmap.setNumberOfRowsInBlock(0);
			this.userFieldmap.setNumberOfRangesInBlock(0);
			this.userFieldmap.setStartingColumn(1);
			this.userFieldmap.setStartingRange(1);
			this.userFieldmap.setPlantingOrder(0);
			this.userFieldmap.setMachineRowCapacity(1);
			this.userFieldmap.setFieldMapLabels(null);
			this.userFieldmap.setFieldmap(null);
		}
	}

	@ModelAttribute("programLocationURL")
	public String getProgramLocation() {
		return this.fieldbookProperties.getProgramLocationsUrl();
	}

	@ModelAttribute("projectID")
	public String getProgramID() {
		return this.getCurrentProjectId();
	}

	public boolean validateFieldMapDataSetInfo(final List<FieldMapDatasetInfo> datasetList) {
		if (!CollectionUtils.isEmpty(datasetList)) {
			final List<FieldMapDatasetInfo> withFieldMap = datasetList.stream()
				.filter(fieldMapDatasetInfo -> !CollectionUtils.isEmpty(fieldMapDatasetInfo.getTrialInstancesWithFieldMap())).collect(
					Collectors.toList());
			for (final FieldMapDatasetInfo fieldMapDatasetInfo : withFieldMap) {
				final boolean hasInvalidValues = fieldMapDatasetInfo.getTrialInstancesWithFieldMap().stream()
					.anyMatch(fieldMapTrialInstanceInfo -> fieldMapTrialInstanceInfo.isHasInValidValue());
				if (hasInvalidValues) {
					return hasInvalidValues;
				}
			}
		}
		return false;
	}
}
