
package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.dms.Phenotype;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.StudyService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.util.DataMapUtil;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.service.ValidationService;
import com.efficio.fieldbook.web.util.WorkbookUtil;

@Controller
@RequestMapping("/trial/measurements")
public class TrialMeasurementsController extends AbstractBaseFieldbookController {

	private static final String EDIT_EXPERIMENT_CELL_TEMPLATE = "/Common/updateExperimentCell";

	private static final Logger LOG = LoggerFactory.getLogger(TrialMeasurementsController.class);
	private static final String STATUS = "status";
	private static final String ERROR_MESSAGE = "errorMessage";
	private static final String INDEX = "index";
	static final String SUCCESS = "success";
	private static final String TERM_ID = "termId";
	static final String DATA = "data";

	@Resource
	private UserSelection userSelection;

	@Resource
	private ValidationService validationService;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Resource
	private PaginationListSelection paginationListSelection;

	@Resource
	private StudyService studyService;

	@Resource
	private StudyDataManager studyDataManager;

	@Resource
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Override
	public String getContentName() {
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/{studyType}/updateTraits", method = RequestMethod.POST)
	public Map<String, String> updateTraits(@ModelAttribute("createNurseryForm") CreateNurseryForm form, @PathVariable String studyType,
			BindingResult result, Model model) {

		UserSelection userSelection = this.getUserSelection();

		Map<String, String> resultMap = new HashMap<String, String>();

		Workbook workbook = userSelection.getWorkbook();

		form.setMeasurementRowList(userSelection.getMeasurementRowList());
		form.setMeasurementVariables(userSelection.getWorkbook().getMeasurementDatasetVariables());
		form.setStudyName(workbook.getStudyDetails().getStudyName());

		workbook.setObservations(form.getMeasurementRowList());
		workbook.updateTrialObservationsWithReferenceList(form.getTrialEnvironmentValues());

		try {
			this.validationService.validateObservationValues(workbook, "");
			this.fieldbookMiddlewareService.saveMeasurementRows(workbook, this.contextUtil.getCurrentProgramUUID());
			resultMap.put(TrialMeasurementsController.STATUS, "1");
		} catch (WorkbookParserException e) {
			TrialMeasurementsController.LOG.error(e.getMessage(), e);
			resultMap.put(TrialMeasurementsController.STATUS, "-1");
			resultMap.put(TrialMeasurementsController.ERROR_MESSAGE, e.getMessage());
		}

		return resultMap;
	}

	private UserSelection getUserSelection() {
		return this.userSelection;
	}

	protected void setUserSelection(UserSelection userSelection) {
		this.userSelection = userSelection;
	}

	/**
	 * POST call once value has been entered in the table cell and user has blurred out or hit enter.
	 */
	@ResponseBody
	@RequestMapping(value = "/update/experiment/cell/data", method = RequestMethod.POST)
	@Transactional
	public Map<String, Object> updateExperimentCellData(@RequestBody final Map<String, String> data, final HttpServletRequest req) {

		final Map<String, Object> map = new HashMap<String, Object>();
		Integer phenotypeId = null;
		if (StringUtils.isNotBlank(data.get("phenotypeId"))) {
			phenotypeId = Integer.valueOf(data.get("phenotypeId"));
		}
		final int termId = Integer.valueOf(data.get(TrialMeasurementsController.TERM_ID));

		final String value = data.get("value");
		final boolean isDiscard = "1".equalsIgnoreCase(req.getParameter("isDiscard"));
		final boolean invalidButKeep = "1".equalsIgnoreCase(req.getParameter("invalidButKeep"));

		final int experimentId = Integer.valueOf(data.get("experimentId"));
		map.put("experimentId", experimentId);
		map.put("phenotypeId", phenotypeId != null ? phenotypeId : "");

		if (!isDiscard) {
			Phenotype existingPhenotype = null;
			if (phenotypeId != null) {
				existingPhenotype = this.studyDataManager.getPhenotypeById(phenotypeId);
			}

			final Variable trait = this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(), termId, true, false);

			if (!invalidButKeep) {
				if (!this.validationService.validateObservationValue(trait, value)) {
					map.put(TrialMeasurementsController.SUCCESS, "0");
					map.put(TrialMeasurementsController.ERROR_MESSAGE, "Invalid value.");
					return map;
				}
			}
			this.studyDataManager.saveOrUpdatePhenotypeValue(experimentId, trait.getId(), value, existingPhenotype,
					trait.getScale().getDataType().getId());
		}
		map.put(TrialMeasurementsController.SUCCESS, "1");

		Map<String, Object> dataMap = new HashMap<String, Object>();
		final List<ObservationDto> singleObservation =
				this.studyService.getSingleObservation(this.userSelection.getWorkbook().getStudyDetails().getId(), experimentId);
		if (!singleObservation.isEmpty()) {
			dataMap = generateDatatableDataMap(singleObservation.get(0), "");
		}
		map.put(TrialMeasurementsController.DATA, dataMap);
		return map;
	}

	/**
	 * POST call once value has been entered in the table cell and user has blurred out or hit enter.
	 * The Update is made by Index of the record as stored temprorary in memory without persisting to the DB until User hits Save.
	 */
	@ResponseBody
	@RequestMapping(value = "/updateByIndex/experiment/cell/data", method = RequestMethod.POST)
	@Transactional
	public Map<String, Object> updateExperimentCellDataByIndex(@RequestBody final Map<String, String> data, final HttpServletRequest req) {

		final Map<String, Object> map = new HashMap<String, Object>();

		Integer phenotypeId = null;
		if (StringUtils.isNotBlank(data.get("phenotypeId"))) {
			phenotypeId = Integer.valueOf(data.get("phenotypeId"));
		}
		final int termId = Integer.valueOf(data.get(TrialMeasurementsController.TERM_ID));

		if (StringUtils.isNotBlank(data.get(TrialMeasurementsController.INDEX))) {
			final int index = Integer.valueOf(data.get(TrialMeasurementsController.INDEX));
			final String value = data.get("value");
			// for categorical
			int isNew;
			if (data.get("isNew") != null) {
				isNew = Integer.valueOf(data.get("isNew"));
			} else {
				isNew = 1;
			}
			final boolean isDiscard = "1".equalsIgnoreCase(req.getParameter("isDiscard"));

			map.put(TrialMeasurementsController.INDEX, index);

			final UserSelection userSelection = this.getUserSelection();
			final MeasurementRow originalRow = userSelection.getMeasurementRowList().get(index);

			try {
				if (!isDiscard) {
					final MeasurementRow copyRow = originalRow.copy();
					this.copyMeasurementValue(copyRow, originalRow, isNew == 1);
					// we set the data to the copy row
					if (copyRow != null && copyRow.getMeasurementVariables() != null) {
						this.updatePhenotypeValues(copyRow.getDataList(), value, termId, isNew);
					}
					this.validationService.validateObservationValues(userSelection.getWorkbook(), copyRow);
					// if there are no error, meaning everything is good, thats the time we copy it to the original
					this.copyMeasurementValue(originalRow, copyRow, isNew == 1);
					this.updateDates(originalRow);
				}
				map.put(TrialMeasurementsController.SUCCESS, "1");
				final DataMapUtil dataMapUtil = new DataMapUtil();
				final Map<String, Object> dataMap = dataMapUtil.generateDatatableDataMap(originalRow, "", this.userSelection);
				map.put(TrialMeasurementsController.DATA, dataMap);
			} catch (final MiddlewareQueryException e) {
				TrialMeasurementsController.LOG.error(e.getMessage(), e);
				map.put(TrialMeasurementsController.SUCCESS, "0");
				map.put(TrialMeasurementsController.ERROR_MESSAGE, e.getMessage());
			}
		}
		return map;
	}

	private void updateDates(final MeasurementRow originalRow) {
		if (originalRow != null && originalRow.getMeasurementVariables() != null) {
			for (final MeasurementData var : originalRow.getDataList()) {
				this.convertToDBDateIfDate(var);
			}
		}
	}

	private void convertToDBDateIfDate(final MeasurementData var) {
		if (var != null && var.getMeasurementVariable() != null && var.getMeasurementVariable().getDataTypeId() != null
				&& var.getMeasurementVariable().getDataTypeId() == TermId.DATE_VARIABLE.getId()) {
			var.setValue(DateUtil.convertToDBDateFormat(var.getMeasurementVariable().getDataTypeId(), var.getValue()));
		}
	}

	private void updatePhenotypeValues(final List<MeasurementData> measurementDataList, final String value, final int termId, final int isNew) {
		for (final MeasurementData var : measurementDataList) {
			if (var != null && var.getMeasurementVariable().getTermId() == termId) {
				if (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()
						|| !var.getMeasurementVariable().getPossibleValues().isEmpty()) {
					if (isNew == 1) {
						var.setcValueId(null);
						var.setCustomCategoricalValue(true);
					} else {
						var.setcValueId(value);
						var.setCustomCategoricalValue(false);
					}
					var.setValue(value);
					var.setAccepted(true);
				} else {
					var.setAccepted(true);
					var.setValue(value);
				}
				break;
			}
		}
	}

	@ResponseBody
	@RequestMapping(value = "/update/experiment/cell/accepted", method = RequestMethod.POST)
	public Map<String, Object> markExperimentCellDataAsAccepted(@RequestBody Map<String, String> data, HttpServletRequest req) {

		Map<String, Object> map = new HashMap<String, Object>();

		int index = Integer.valueOf(data.get(TrialMeasurementsController.INDEX));
		int termId = Integer.valueOf(data.get(TrialMeasurementsController.TERM_ID));

		map.put(TrialMeasurementsController.INDEX, index);

		UserSelection userSelection = this.getUserSelection();
		MeasurementRow originalRow = userSelection.getMeasurementRowList().get(index);

		if (originalRow != null && originalRow.getMeasurementVariables() != null) {
			for (MeasurementData var : originalRow.getDataList()) {
				if (var != null
						&& var.getMeasurementVariable().getTermId() == termId
						&& (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId() || !var
								.getMeasurementVariable().getPossibleValues().isEmpty())) {
					var.setAccepted(true);
					if (this.isCategoricalValueOutOfBounds(var.getcValueId(), var.getValue(), var.getMeasurementVariable()
							.getPossibleValues())) {
						var.setCustomCategoricalValue(true);
					} else {
						var.setCustomCategoricalValue(false);
					}
					break;
				} else if (var != null && var.getMeasurementVariable().getTermId() == termId
						&& var.getMeasurementVariable().getDataTypeId() == TermId.NUMERIC_VARIABLE.getId()) {
					var.setAccepted(true);
					break;
				}
			}
		}

		map.put(TrialMeasurementsController.SUCCESS, "1");
		final DataMapUtil dataMapUtil = new DataMapUtil();
		final Map<String, Object> dataMap =   dataMapUtil.generateDatatableDataMap(originalRow, "", this.userSelection);
		map.put(TrialMeasurementsController.DATA, dataMap);

		return map;
	}

	private void markNonEmptyVariateValuesAsMissing(List<MeasurementData> measurementDataList) {
		for (MeasurementData var : measurementDataList) {
			if (var != null && !StringUtils.isEmpty(var.getValue())
					&& var.getMeasurementVariable().getDataTypeId() == TermId.NUMERIC_VARIABLE.getId()) {
				if (this.isNumericalValueOutOfBounds(var.getValue(), var.getMeasurementVariable())) {
					var.setAccepted(true);
					var.setValue(MeasurementData.MISSING_VALUE);
				}
			} else if (var != null
					&& !StringUtils.isEmpty(var.getValue())
					&& (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId() || !var
							.getMeasurementVariable().getPossibleValues().isEmpty())) {
				var.setAccepted(true);
				if (this.isCategoricalValueOutOfBounds(var.getcValueId(), var.getValue(), var.getMeasurementVariable().getPossibleValues())) {
					var.setValue(MeasurementData.MISSING_VALUE);
					var.setCustomCategoricalValue(true);
				} else {
					var.setCustomCategoricalValue(false);
				}
			}
		}
	}

	@ResponseBody
	@RequestMapping(value = "/update/experiment/cell/missing/all", method = RequestMethod.GET)
	public Map<String, Object> markAllExperimentDataAsMissing() {
		Map<String, Object> map = new HashMap<String, Object>();
		UserSelection userSelection = this.getUserSelection();
		for (MeasurementRow row : userSelection.getMeasurementRowList()) {
			if (row != null && row.getMeasurementVariables() != null) {
				this.markNonEmptyVariateValuesAsMissing(row.getDataList());
			}
		}
		map.put(TrialMeasurementsController.SUCCESS, "1");
		return map;
	}

	/**
	 * GET call on clicking the cell in table for entering measurement value inline.
	 */
	@RequestMapping(value = "/edit/experiment/cell/{experimentId}/{termId}", method = RequestMethod.GET)
	public String editExperimentCells(@PathVariable int experimentId, @PathVariable int termId,
			@RequestParam(required = false) Integer phenotypeId, Model model) throws MiddlewareQueryException {

		if (phenotypeId != null) {
			Phenotype phenotype = studyDataManager.getPhenotypeById(phenotypeId);
			model.addAttribute("phenotypeId", phenotype.getPhenotypeId());
			model.addAttribute("phenotypeValue", phenotype.getValue());
		} else {
			model.addAttribute("phenotypeId", "");
			model.addAttribute("phenotypeValue", "");
		}

		Variable variable = this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(), termId, true, false);

		model.addAttribute("categoricalVarId", TermId.CATEGORICAL_VARIABLE.getId());
		model.addAttribute("dateVarId", TermId.DATE_VARIABLE.getId());
		model.addAttribute("numericVarId", TermId.NUMERIC_VARIABLE.getId());
		model.addAttribute("isNursery", !this.getUserSelection().isTrial());
		model.addAttribute("variable", variable);
		model.addAttribute("experimentId", experimentId);

		model.addAttribute(TrialMeasurementsController.TERM_ID, termId);
		model.addAttribute("possibleValues", this.fieldbookService.getAllPossibleValues(variable));

		return super.showAjaxPage(model, TrialMeasurementsController.EDIT_EXPERIMENT_CELL_TEMPLATE);
	}

	/**
	 * GET call on clicking the cell in table for bulk editing measurement value inline for import preview measurements table.
	 */
	@RequestMapping(value = "/update/experiment/cell/{index}/{termId}", method = RequestMethod.GET)
	public String editExperimentCells(@PathVariable int index, @PathVariable int termId, Model model) throws MiddlewareQueryException {

		final UserSelection userSelection = this.getUserSelection();
		final List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();
		tempList.addAll(userSelection.getMeasurementRowList());

		final MeasurementRow row = tempList.get(index);
		final MeasurementRow copyRow = row.copy();
		this.copyMeasurementValue(copyRow, row);
		MeasurementData editData = null;
		List<ValueReference> possibleValues = new ArrayList<ValueReference>();
		if (copyRow != null && copyRow.getMeasurementVariables() != null) {
			for (final MeasurementData var : copyRow.getDataList()) {
				this.convertToUIDateIfDate(var);
				if (var != null
						&& (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId() || !var
						.getMeasurementVariable().getPossibleValues().isEmpty())) {
					possibleValues = var.getMeasurementVariable().getPossibleValues();
				}
				if (var != null && var.getMeasurementVariable().getTermId() == termId) {
					editData = var;
					break;
				}
			}
		}

		final Variable variable = this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(), termId, true,
				false);
		model.addAttribute("variable", variable);
		model.addAttribute("phenotypeId", "");
		model.addAttribute("phenotypeValue", "");
		model.addAttribute("possibleValues", this.fieldbookService.getAllPossibleValues(variable));
		model.addAttribute("categoricalVarId", TermId.CATEGORICAL_VARIABLE.getId());
		model.addAttribute("dateVarId", TermId.DATE_VARIABLE.getId());
		model.addAttribute("numericVarId", TermId.NUMERIC_VARIABLE.getId());

		this.updateModel(model, userSelection.getWorkbook().isNursery(), editData, index, termId);
		return super.showAjaxPage(model, TrialMeasurementsController.EDIT_EXPERIMENT_CELL_TEMPLATE);
	}

	private void convertToUIDateIfDate(MeasurementData var) {
		if (var != null && var.getMeasurementVariable() != null && var.getMeasurementVariable().getDataTypeId() != null
				&& var.getMeasurementVariable().getDataTypeId() == TermId.DATE_VARIABLE.getId()) {
			var.setValue(DateUtil.convertToUIDateFormat(var.getMeasurementVariable().getDataTypeId(), var.getValue()));
		}
	}

	private void updateModel(Model model, boolean isNursery, MeasurementData measurementData, int index, int termId) {
		model.addAttribute("isNursery", isNursery);
		model.addAttribute("measurementData", measurementData);
		model.addAttribute(TrialMeasurementsController.INDEX, index);
		model.addAttribute(TrialMeasurementsController.TERM_ID, termId);
	}

	/**
	 * This the call to get data required for measurement table in JSON format.
	 * The url is /plotMeasurements/{studyid}/{instanceid}?pagenumber=1&pagesize=100
	 */
	@ResponseBody
	@RequestMapping(value = "/plotMeasurements/{studyId}/{instanceId}", method = RequestMethod.GET, produces = "application/json")
	@Transactional
	public Map<String, Object> getPlotMeasurementsPaginated(@PathVariable final int studyId, @PathVariable final int instanceId,
			@ModelAttribute("createNurseryForm") final CreateNurseryForm form,
			final Model model, final HttpServletRequest req) {

		final List<Map<String, Object>> masterDataList = new ArrayList<Map<String, Object>>();
		final Map <String, Object> masterMap = new HashMap<>();

		// number of records per page
		final Integer pageSize = Integer.parseInt(req.getParameter("pageSize"));
		final Integer pageNumber = Integer.parseInt(req.getParameter("pageNumber"));
		final String sortBy = req.getParameter("sortBy");
		final String sortOrder = req.getParameter("sortOrder");

		final List<ObservationDto> pageResults =
				this.studyService.getObservations(studyId, instanceId, pageNumber, pageSize, sortBy, sortOrder);

		for (final ObservationDto row : pageResults) {
			final Map<String, Object> dataMap = this.generateDatatableDataMap(row, "");
			masterDataList.add(dataMap);
		}

		final int totalObservationUnits = this.studyService.countTotalObservationUnits(studyId, instanceId);

		//We need to pass back the draw number as an integer value to prevent Cross Site Scripting attacks
		//The draw counter that this object is a response to, we echoing it back for the frontend
		masterMap.put("draw", req.getParameter("draw"));
		masterMap.put("recordsTotal", totalObservationUnits);
		masterMap.put("recordsFiltered", totalObservationUnits);
		masterMap.put("data", masterDataList);

		return masterMap;
	}

	@ResponseBody
	@RequestMapping(value = "/plotMeasurements/preview", method = RequestMethod.GET, produces = "application/json")
	public List<Map<String, Object>> getPreviewPlotMeasurements() {
		final UserSelection userSelection = this.getUserSelection();
		final List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();

		if (userSelection.getTemporaryWorkbook() != null) {
			tempList.addAll(userSelection.getTemporaryWorkbook().getObservations());
		} else {
			tempList.addAll(userSelection.getWorkbook().getObservations());
		}

		final List<Map<String, Object>> masterList = new ArrayList<Map<String, Object>>();

		final DataMapUtil dataMapUtil = new DataMapUtil();
		for (final MeasurementRow row : tempList) {
			final Map<String, Object> dataMap = dataMapUtil.generateDatatableDataMap(row, "", this.userSelection);
			masterList.add(dataMap);
		}

		return masterList;
	}

	@ResponseBody
	@RequestMapping(value = "/instanceMetadata/{studyId}", method = RequestMethod.GET)
	@Transactional
	public List<StudyInstance> getStudyInstanceMetaData(@PathVariable final int studyId) {
		return this.studyService.getStudyInstances(studyId);
	}

	/**
	 * We maintain the state of categorical description view in session to support the ff scenario: 1. When user does a browser refresh, the
	 * state of measurements view is maintained 2. When user switches between studies (either nursery or trial) state is also maintained 3.
	 * Generating the modal for editing whole measurement row/entry is done in the backend (see updateExperimentModal.html) , this also
	 * helps us track which display values in the cateogrical dropdown is used
	 * 
	 * @param showCategoricalDescriptionView
	 * @param session
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/setCategoricalDisplayType", method = RequestMethod.GET)
	public Boolean setCategoricalDisplayType(@RequestParam Boolean showCategoricalDescriptionView, HttpSession session) {
		Boolean isCategoricalDescriptionView = (Boolean) session.getAttribute("isCategoricalDescriptionView");

		if (null != showCategoricalDescriptionView) {
			isCategoricalDescriptionView = showCategoricalDescriptionView;
		} else {
			isCategoricalDescriptionView ^= Boolean.TRUE;
		}

		session.setAttribute("isCategoricalDescriptionView", isCategoricalDescriptionView);

		return isCategoricalDescriptionView;
	}

	protected boolean isNumericalValueOutOfBounds(String value, MeasurementVariable var) {
		return var.getMinRange() != null && var.getMaxRange() != null && NumberUtils.isNumber(value)
				&& (Double.valueOf(value) < var.getMinRange() || Double.valueOf(value) > var.getMaxRange());
	}

	protected boolean isCategoricalValueOutOfBounds(String cValueId, String value, List<ValueReference> possibleValues) {
		String val = cValueId;
		if (val == null) {
			val = value;
		}
		for (ValueReference ref : possibleValues) {
			if (ref.getKey().equals(val)) {
				return false;
			}
		}
		return true;
	}

	protected void copyMeasurementValue(MeasurementRow origRow, MeasurementRow valueRow) {
		this.copyMeasurementValue(origRow, valueRow, false);
	}

	protected void copyMeasurementValue(MeasurementRow origRow, MeasurementRow valueRow, boolean isNew) {

		for (int index = 0; index < origRow.getDataList().size(); index++) {
			MeasurementData data = origRow.getDataList().get(index);
			MeasurementData valueRowData = valueRow.getDataList().get(index);
			this.copyMeasurementDataValue(data, valueRowData, isNew);
		}
	}

	private void copyMeasurementDataValue(MeasurementData oldData, MeasurementData newData, boolean isNew) {
		if (oldData.getMeasurementVariable().getPossibleValues() != null && !oldData.getMeasurementVariable().getPossibleValues().isEmpty()) {
			oldData.setAccepted(newData.isAccepted());
			if (!StringUtils.isEmpty(oldData.getValue())
					&& oldData.isAccepted()
					&& this.isCategoricalValueOutOfBounds(oldData.getcValueId(), oldData.getValue(), oldData.getMeasurementVariable()
							.getPossibleValues())) {
				oldData.setCustomCategoricalValue(true);
			} else {
				oldData.setCustomCategoricalValue(false);
			}
			if (newData.getcValueId() != null) {
				if (isNew) {
					oldData.setCustomCategoricalValue(true);
					oldData.setcValueId(null);
				} else {
					oldData.setcValueId(newData.getcValueId());
					oldData.setCustomCategoricalValue(false);
				}
				oldData.setValue(newData.getcValueId());
			} else if (newData.getValue() != null) {
				if (isNew) {
					oldData.setCustomCategoricalValue(true);
					oldData.setcValueId(null);
				} else {
					oldData.setcValueId(newData.getValue());
					oldData.setCustomCategoricalValue(false);
				}
				oldData.setValue(newData.getValue());
			}
		} else {
			oldData.setValue(newData.getValue());
			oldData.setAccepted(newData.isAccepted());
		}
	}



	private Map<String, Object> generateDatatableDataMap(final ObservationDto row, String suffix) {
		Map<String, Object> dataMap = new HashMap<String, Object>();
		// the 4 attributes are needed always
		dataMap.put("Action", Integer.toString(row.getMeasurementId()));
		dataMap.put("experimentId", Integer.toString(row.getMeasurementId()));
		dataMap.put("GID", row.getGid());
		dataMap.put("DESIGNATION", row.getDesignation());

		// initialize suffix as empty string if its null
		suffix = null == suffix ? "" : suffix;

		List<MeasurementVariable> measurementDatasetVariables = new ArrayList<MeasurementVariable>();
		measurementDatasetVariables.addAll(this.userSelection.getWorkbook().getMeasurementDatasetVariablesView());

		// generate measurement row data from dataList (existing / generated data)
		for (MeasurementDto data : row.getTraitMeasurements()) {

			final Integer traitId = data.getTrait().getTraitId();
			Variable variable = this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(),
					traitId, true, false);
			final MeasurementVariable measurementVariable = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, traitId);
			
			if (variable.getScale().getDataType().equals(DataType.CATEGORICAL_VARIABLE)) {

				this.addDataTableDataMapForCategoricalVariable(variable, data, dataMap, measurementVariable.getName(), suffix);

			} else if (variable.getScale().getDataType().equals(DataType.NUMERIC_VARIABLE)) {
				dataMap.put(measurementVariable.getName(), new Object[] {data.getTriatValue() != null ? data.getTriatValue() : "", true,
						data.getPhenotypeId() != null ? data.getPhenotypeId() : ""});
			} else {
				dataMap.put(measurementVariable.getName(), new Object[] {data.getTriatValue() != null ? data.getTriatValue() : "",
						data.getPhenotypeId() != null ? data.getPhenotypeId() : ""});
			}
		}

		// generate measurement row data for standard factors like TRIAL_INSTANCE, ENTRY_NO, ENTRY_TYPE, PLOT_NO, PLOT_ID, etc
		this.addGermplasmAndPlotFactorsDataToDataMap(row, dataMap, measurementDatasetVariables);

		// generate measurement row data from newly added traits (no data yet)
		final UserSelection userSelection = this.getUserSelection();
		if (userSelection != null && userSelection.getMeasurementDatasetVariable() != null
				&& !userSelection.getMeasurementDatasetVariable().isEmpty()) {
			for (MeasurementVariable var : userSelection.getMeasurementDatasetVariable()) {
				if (!dataMap.containsKey(var.getName())) {
					if (var.getDataTypeId().equals(TermId.CATEGORICAL_VARIABLE.getId())) {
						dataMap.put(var.getName(), new Object[] {"", "", true});
					} else {
						dataMap.put(var.getName(), "");
					}
				}
			}
		}
		return dataMap;
	}

	/*
	 * Generate measurement row data for standard factors like TRIAL_INSTANCE, ENTRY_NO, ENTRY_TYPE, PLOT_NO, REP_NO,
	 * BLOCK_NO, ROW, COL, PLOT_ID and add to dataMap. Use the local name of the variable as key and the 
	 * value of the variable as value in dataMap.
	 */
	private void addGermplasmAndPlotFactorsDataToDataMap(final ObservationDto row, Map<String, Object> dataMap,
			List<MeasurementVariable> measurementDatasetVariables) {
		final MeasurementVariable entryNoVar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, TermId.ENTRY_NO.getId());
		if (entryNoVar != null){
			dataMap.put(entryNoVar.getName(), new Object[] {row.getEntryNo(), false});
		}
		
		final MeasurementVariable entryCodeVar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, TermId.ENTRY_CODE.getId());
		if (entryCodeVar != null) {
			dataMap.put(entryCodeVar.getName(), new Object[] {row.getEntryCode(), false});
		}
		
		final MeasurementVariable entryTypeVar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, TermId.ENTRY_TYPE.getId());
		if (entryTypeVar != null) { 
			dataMap.put(entryTypeVar.getName(), new Object[] {row.getEntryType(), row.getEntryType(), false});
		}
		
		final MeasurementVariable plotNoVar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, TermId.PLOT_NO.getId());
		if (plotNoVar != null) { 
			dataMap.put(plotNoVar.getName(), new Object[] {row.getPlotNumber(), false});
		}

		final MeasurementVariable repNoVar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, TermId.REP_NO.getId());
		if (repNoVar != null) { 
			dataMap.put(repNoVar.getName(), new Object[] {row.getRepitionNumber(), false});
		}
		
		final MeasurementVariable blockNoVar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, TermId.BLOCK_NO.getId());
		if (blockNoVar != null) { 
			dataMap.put(blockNoVar.getName(), new Object[] {row.getBlockNumber(), false});
		}

		final MeasurementVariable rowVar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, TermId.ROW.getId());
		if (rowVar != null) { 
			dataMap.put(rowVar.getName(), new Object[] {row.getRowNumber(), false});
		}
		
		final MeasurementVariable colVar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, TermId.COL.getId());
		if (colVar != null) { 
			dataMap.put(colVar.getName(), new Object[] {row.getColumnNumber(), false});
		}
		
		final MeasurementVariable trialInstanceVar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, TermId.TRIAL_INSTANCE_FACTOR.getId());
		if (trialInstanceVar != null) { 
			dataMap.put(trialInstanceVar.getName(), new Object[] {row.getTrialInstance(), false});
		}
		
		final MeasurementVariable plotIdVar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, TermId.PLOT_ID.getId());
		if (plotIdVar != null) { 
			dataMap.put(plotIdVar.getName(), new Object[] {row.getPlotId(), false});
		}

		for (Pair<String, String> additionalGermplasmAttrCols : row.getAdditionalGermplasmDescriptors()) {
			dataMap.put(additionalGermplasmAttrCols.getLeft(), new Object[] {additionalGermplasmAttrCols.getRight()});
		}
	}

	void addDataTableDataMapForCategoricalVariable(final Variable measurementVariable, final MeasurementDto data, final Map<String, Object> dataMap, final String localVariableName, final String suffix) {

		if (StringUtils.isBlank(data.getTriatValue())) {
			dataMap.put(localVariableName,
					new Object[] {"", "", false, data.getPhenotypeId() != null ? data.getPhenotypeId() : ""});
		} else {
			boolean isCategoricalValueFound = false;
			String catName = "";
			String catDisplayValue = "";

			// Find the categorical value (possible value) of the measurement data, so we can get its name and definition.
			for (TermSummary category : measurementVariable.getScale().getCategories()) {
				if (category.getName().equals(data.getTriatValue())) {
					catName = category.getName();
					catDisplayValue = category.getDefinition();
					isCategoricalValueFound = true;
					break;
				}
			}

			// If the measurement value is out of range from categorical values, then the assumption is, it is custom value.
			// For this case, just display the measurement data as is.
			if (!isCategoricalValueFound) {
				catName = data.getTriatValue();
				catDisplayValue = data.getTriatValue();
			}
			dataMap.put(localVariableName, new Object[] {catName + suffix, catDisplayValue + suffix, true,
					data.getPhenotypeId() != null ? data.getPhenotypeId() : ""});
		}


	}


	void setValidationService(ValidationService validationService) {
		this.validationService = validationService;
	}

	void setStudyService(StudyService studyService) {
		this.studyService = studyService;
	}

	void setOntologyVariableDataManager(OntologyVariableDataManager ontologyVariableDataManager) {
		this.ontologyVariableDataManager = ontologyVariableDataManager;
	}

	void setStudyDataManager(StudyDataManager studyDataManager) {
		this.studyDataManager = studyDataManager;
	}

	void setFieldbookService(com.efficio.fieldbook.service.api.FieldbookService fieldbookService) {
		this.fieldbookService = fieldbookService;
	}
}
