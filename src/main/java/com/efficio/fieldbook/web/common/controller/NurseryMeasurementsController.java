
package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.CategoricalDisplayValue;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.study.StudyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.AddOrRemoveTraitsForm;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.service.ValidationService;

@Controller
@RequestMapping("/nursery/measurements")
public class NurseryMeasurementsController extends AbstractBaseFieldbookController {

	private static final Logger LOG = LoggerFactory.getLogger(NurseryMeasurementsController.class);
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

	@RequestMapping(value = "/inlineinput/single/{index}/{termId}", method = RequestMethod.GET)
	public String inlineInputNurseryGet(@PathVariable int index, @PathVariable int termId, Model model) throws MiddlewareQueryException {

		List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();
		tempList.addAll(this.userSelection.getMeasurementRowList());

		MeasurementRow row = tempList.get(index);
		MeasurementRow copyRow = row.copy();
		this.copyMeasurementValue(copyRow, row);
		MeasurementData editData = null;
		List<ValueReference> possibleValues = new ArrayList<ValueReference>();
		if (copyRow != null && copyRow.getMeasurementVariables() != null) {
			for (MeasurementData var : copyRow.getDataList()) {
				this.convertToUIDateIfDate(var);
				if (var != null && (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()
						|| !var.getMeasurementVariable().getPossibleValues().isEmpty())) {
					possibleValues = var.getMeasurementVariable().getPossibleValues();
				}
				if (var != null && var.getMeasurementVariable().getTermId() == termId) {
					editData = var;
					break;
				}
			}
		}
		this.updateModel(model, userSelection.getWorkbook().isNursery(), editData, index, termId, possibleValues);
		return super.showAjaxPage(model, "/NurseryManager/inlineInputMeasurement");
	}

	@ResponseBody
	@RequestMapping(value = "/inlineinput/single", method = RequestMethod.POST)
	public Map<String, Object> inlineInputNurseryPost(@RequestBody Map<String, String> data, HttpServletRequest req) {

		Map<String, Object> map = new HashMap<String, Object>();

		int index = Integer.valueOf(data.get(NurseryMeasurementsController.INDEX));
		int termId = Integer.valueOf(data.get(NurseryMeasurementsController.TERM_ID));
		String value = data.get("value");
		// for categorical
		int isNew = Integer.valueOf(data.get("isNew"));
		boolean isDiscard = "1".equalsIgnoreCase(req.getParameter("isDiscard")) ? true : false;

		map.put(NurseryMeasurementsController.INDEX, index);

		List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();
		tempList.addAll(userSelection.getMeasurementRowList());

		MeasurementRow originalRow = userSelection.getMeasurementRowList().get(index);

		try {
			if (!isDiscard) {
				MeasurementRow copyRow = originalRow.copy();
				this.copyMeasurementValue(copyRow, originalRow, isNew == 1 ? true : false);
				// we set the data to the copy row
				if (copyRow != null && copyRow.getMeasurementVariables() != null) {
					this.updatePhenotypeValues(copyRow.getDataList(), value, termId, isNew);
				}
				this.validationService.validateObservationValues(userSelection.getWorkbook(), copyRow);
				// if there are no error, meaning everything is good, thats the time we copy it to the original
				this.copyMeasurementValue(originalRow, copyRow, isNew == 1 ? true : false);
				this.updateDates(originalRow);
			}
			map.put(NurseryMeasurementsController.SUCCESS, "1");
			Map<String, Object> dataMap = this.generateDatatableDataMap(originalRow, "");
			map.put(NurseryMeasurementsController.DATA, dataMap);
		} catch (MiddlewareQueryException e) {
			NurseryMeasurementsController.LOG.error(e.getMessage(), e);
			map.put(NurseryMeasurementsController.SUCCESS, "0");
			map.put(NurseryMeasurementsController.ERROR_MESSAGE, e.getMessage());
		}

		return map;
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	public List<Map<String, Object>> nurseryMeasurementsGet(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model) {
		List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();

		if (this.userSelection.getTemporaryWorkbook() != null && this.userSelection.getMeasurementRowList() == null) {
			tempList.addAll(this.userSelection.getTemporaryWorkbook().getObservations());
		} else {
			tempList.addAll(this.userSelection.getMeasurementRowList());
		}

		form.setMeasurementRowList(tempList);

		List<Map<String, Object>> masterList = new ArrayList<Map<String, Object>>();

		for (MeasurementRow row : tempList) {

			Map<String, Object> dataMap = this.generateDatatableDataMap(row, "");

			masterList.add(dataMap);
		}

		return masterList;
	}

	/**
	 * This is the GET call to open the action dialog to edit one row.
	 */
	@RequestMapping(value = "/inlineinput/multiple/{index}", method = RequestMethod.GET)
	public String inlineInputNurseryMultipleGet(@PathVariable int index,
			@ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form, Model model) throws MiddlewareQueryException {

		List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();
		tempList.addAll(userSelection.getMeasurementRowList());

		MeasurementRow row = tempList.get(index);
		MeasurementRow copyRow = row.copy();
		this.copyMeasurementValue(copyRow, row);
		if (copyRow != null && copyRow.getMeasurementVariables() != null) {
			for (MeasurementData var : copyRow.getDataList()) {
				if (var != null && var.getMeasurementVariable() != null && var.getMeasurementVariable().getDataTypeId() != null
						&& var.getMeasurementVariable().getDataTypeId() == TermId.DATE_VARIABLE.getId()) {
					// we change the date to the UI format
					var.setValue(DateUtil.convertToUIDateFormat(var.getMeasurementVariable().getDataTypeId(), var.getValue()));
				}
			}
		}
		form.setUpdateObservation(copyRow);
		form.setExperimentIndex(index);
		model.addAttribute("categoricalVarId", TermId.CATEGORICAL_VARIABLE.getId());
		model.addAttribute("dateVarId", TermId.DATE_VARIABLE.getId());
		model.addAttribute("numericVarId", TermId.NUMERIC_VARIABLE.getId());
		model.addAttribute("isNursery", userSelection.getWorkbook().isNursery());
		return super.showAjaxPage(model, "/Common/updateExperimentModal");
	}

	@ResponseBody
	@RequestMapping(value = "/inlineinput/multiple/{index}", method = RequestMethod.POST)
	public Map<String, Object> inlineInputNurseryMultiplePost(@PathVariable int index,
			@ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form) {
		Map<String, Object> map = new HashMap<String, Object>();
		List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();
		tempList.addAll(userSelection.getMeasurementRowList());

		MeasurementRow row = form.getUpdateObservation();
		MeasurementRow originalRow = userSelection.getMeasurementRowList().get(index);
		MeasurementRow copyRow = originalRow.copy();
		this.copyMeasurementValue(copyRow, row);

		try {
			this.validationService.validateObservationValues(userSelection.getWorkbook(), copyRow);
			// if there are no error, meaning everything is good, thats the time we copy it to the original
			this.copyMeasurementValue(originalRow, row);
			this.updateDates(originalRow);
			map.put(NurseryMeasurementsController.SUCCESS, "1");
			for (MeasurementData data : originalRow.getDataList()) {
				// we set the data accepted automatically to true, if value is out out limit
				if (data.getMeasurementVariable().getDataTypeId().equals(TermId.NUMERIC_VARIABLE.getId())) {
					Double minRange = data.getMeasurementVariable().getMinRange();
					Double maxRange = data.getMeasurementVariable().getMaxRange();
					if (minRange != null && maxRange != null && NumberUtils.isNumber(data.getValue())
							&& (Double.parseDouble(data.getValue()) < minRange || Double.parseDouble(data.getValue()) > maxRange)) {
						data.setAccepted(true);
					}
				}
			}

			Map<String, Object> dataMap = this.generateDatatableDataMap(originalRow, "");
			map.put(NurseryMeasurementsController.DATA, dataMap);
		} catch (MiddlewareQueryException e) {
			NurseryMeasurementsController.LOG.error(e.getMessage(), e);
			map.put(NurseryMeasurementsController.SUCCESS, "0");
			map.put(NurseryMeasurementsController.ERROR_MESSAGE, e.getMessage());
		}

		return map;
	}

	@ResponseBody
	@RequestMapping(value = "/inlineinput/accepted", method = RequestMethod.POST)
	public Map<String, Object> markExperimentCellDataAsAccepted(@RequestBody Map<String, String> data, HttpServletRequest req) {

		Map<String, Object> map = new HashMap<String, Object>();

		int index = Integer.valueOf(data.get(NurseryMeasurementsController.INDEX));
		int termId = Integer.valueOf(data.get(NurseryMeasurementsController.TERM_ID));

		map.put(NurseryMeasurementsController.INDEX, index);

		MeasurementRow originalRow = userSelection.getMeasurementRowList().get(index);

		if (originalRow != null && originalRow.getMeasurementVariables() != null) {
			for (MeasurementData var : originalRow.getDataList()) {
				if (var != null && var.getMeasurementVariable().getTermId() == termId
						&& (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()
								|| !var.getMeasurementVariable().getPossibleValues().isEmpty())) {
					var.setAccepted(true);
					if (this.isCategoricalValueOutOfBounds(var.getcValueId(), var.getValue(),
							var.getMeasurementVariable().getPossibleValues())) {
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

		map.put(NurseryMeasurementsController.SUCCESS, "1");
		Map<String, Object> dataMap = this.generateDatatableDataMap(originalRow, "");
		map.put(NurseryMeasurementsController.DATA, dataMap);

		return map;
	}

	@ResponseBody
	@RequestMapping(value = "/inlineinput/accepted/all", method = RequestMethod.GET)
	public Map<String, Object> markAllExperimentDataAsAccepted() {

		Map<String, Object> map = new HashMap<String, Object>();

		for (MeasurementRow row : userSelection.getMeasurementRowList()) {
			if (row != null && row.getMeasurementVariables() != null) {
				this.markNonEmptyVariateValuesAsAccepted(row.getDataList());
			}
		}

		map.put(NurseryMeasurementsController.SUCCESS, "1");

		return map;
	}

	private void markNonEmptyVariateValuesAsAccepted(List<MeasurementData> measurementDataList) {
		for (MeasurementData var : measurementDataList) {
			if (var != null && !StringUtils.isEmpty(var.getValue())
					&& var.getMeasurementVariable().getDataTypeId() == TermId.NUMERIC_VARIABLE.getId()) {
				if (this.isNumericalValueOutOfBounds(var.getValue(), var.getMeasurementVariable())) {
					var.setAccepted(true);
				}
			} else if (var != null && !StringUtils.isEmpty(var.getValue())
					&& (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()
							|| !var.getMeasurementVariable().getPossibleValues().isEmpty())) {
				var.setAccepted(true);
				if (this.isCategoricalValueOutOfBounds(var.getcValueId(), var.getValue(),
						var.getMeasurementVariable().getPossibleValues())) {
					var.setCustomCategoricalValue(true);
				} else {
					var.setCustomCategoricalValue(false);
				}

			}
		}
	}

	private void markNonEmptyVariateValuesAsMissing(List<MeasurementData> measurementDataList) {
		for (MeasurementData var : measurementDataList) {
			if (var != null && !StringUtils.isEmpty(var.getValue())
					&& var.getMeasurementVariable().getDataTypeId() == TermId.NUMERIC_VARIABLE.getId()) {
				if (this.isNumericalValueOutOfBounds(var.getValue(), var.getMeasurementVariable())) {
					var.setAccepted(true);
					var.setValue(MeasurementData.MISSING_VALUE);
				}
			} else if (var != null && !StringUtils.isEmpty(var.getValue())
					&& (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()
							|| !var.getMeasurementVariable().getPossibleValues().isEmpty())) {
				var.setAccepted(true);
				if (this.isCategoricalValueOutOfBounds(var.getcValueId(), var.getValue(),
						var.getMeasurementVariable().getPossibleValues())) {
					var.setValue(MeasurementData.MISSING_VALUE);
					var.setCustomCategoricalValue(true);
				} else {
					var.setCustomCategoricalValue(false);
				}
			}
		}
	}

	protected boolean isNumericalValueOutOfBounds(String value, MeasurementVariable var) {
		return var.getMinRange() != null && var.getMaxRange() != null && NumberUtils.isNumber(value)
				&& (Double.valueOf(value) < var.getMinRange() || Double.valueOf(value) > var.getMaxRange());
	}

	@ResponseBody
	@RequestMapping(value = "/inlineinput/missing/all", method = RequestMethod.GET)
	public Map<String, Object> markAllExperimentDataAsMissing() {
		Map<String, Object> map = new HashMap<String, Object>();
		for (MeasurementRow row : userSelection.getMeasurementRowList()) {
			if (row != null && row.getMeasurementVariables() != null) {
				this.markNonEmptyVariateValuesAsMissing(row.getDataList());
			}
		}
		map.put(TrialMeasurementsController.SUCCESS, "1");
		return map;
	}

	private Map<String, Object> generateDatatableDataMap(MeasurementRow row, String suffix) {
		Map<String, Object> dataMap = new HashMap<String, Object>();
		// the 4 attributes are needed always
		dataMap.put("Action", Integer.toString(row.getExperimentId()));
		dataMap.put("experimentId", Integer.toString(row.getExperimentId()));
		dataMap.put("GID", row.getMeasurementDataValue(TermId.GID.getId()));
		dataMap.put("DESIGNATION", row.getMeasurementDataValue(TermId.DESIG.getId()));

		// initialize suffix as empty string if its null
		suffix = null == suffix ? "" : suffix;

		// generate measurement row data from dataList (existing / generated data)
		for (MeasurementData data : row.getDataList()) {
			if (data.isCategorical()) {
				CategoricalDisplayValue categoricalDisplayValue = data.getDisplayValueForCategoricalData();

				dataMap.put(data.getMeasurementVariable().getName(), new Object[] {categoricalDisplayValue.getName() + suffix,
						categoricalDisplayValue.getDescription() + suffix, data.isAccepted()});

			} else if (data.isNumeric()) {
				dataMap.put(data.getMeasurementVariable().getName(), new Object[] {data.getDisplayValue() + suffix, data.isAccepted()});
			} else {
				dataMap.put(data.getMeasurementVariable().getName(), data.getDisplayValue());
			}
		}

		// generate measurement row data from newly added traits (no data yet)
		if (this.userSelection != null && this.userSelection.getMeasurementDatasetVariable() != null
				&& !this.userSelection.getMeasurementDatasetVariable().isEmpty()) {
			for (MeasurementVariable var : this.userSelection.getMeasurementDatasetVariable()) {
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

	private void updateModel(Model model, boolean isNursery, MeasurementData measurementData, int index, int termId,
			List<ValueReference> possibleValues) {
		model.addAttribute("categoricalVarId", TermId.CATEGORICAL_VARIABLE.getId());
		model.addAttribute("dateVarId", TermId.DATE_VARIABLE.getId());
		model.addAttribute("numericVarId", TermId.NUMERIC_VARIABLE.getId());
		model.addAttribute("isNursery", isNursery);
		model.addAttribute("measurementData", measurementData);
		model.addAttribute(NurseryMeasurementsController.INDEX, index);
		model.addAttribute(NurseryMeasurementsController.TERM_ID, termId);
		model.addAttribute("possibleValues", possibleValues);
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
		if (oldData.getMeasurementVariable().getPossibleValues() != null
				&& !oldData.getMeasurementVariable().getPossibleValues().isEmpty()) {
			oldData.setAccepted(newData.isAccepted());
			if (!StringUtils.isEmpty(oldData.getValue()) && oldData.isAccepted() && this.isCategoricalValueOutOfBounds(
					oldData.getcValueId(), oldData.getValue(), oldData.getMeasurementVariable().getPossibleValues())) {
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

	private boolean isCategoricalValueOutOfBounds(String cValueId, String value, List<ValueReference> possibleValues) {
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

	private void updatePhenotypeValues(final List<MeasurementData> measurementDataList, final String value, final int termId,
			final int isNew) {
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

	private void convertToUIDateIfDate(MeasurementData var) {
		if (var != null && var.getMeasurementVariable() != null && var.getMeasurementVariable().getDataTypeId() != null
				&& var.getMeasurementVariable().getDataTypeId() == TermId.DATE_VARIABLE.getId()) {
			var.setValue(DateUtil.convertToUIDateFormat(var.getMeasurementVariable().getDataTypeId(), var.getValue()));
		}
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

}
