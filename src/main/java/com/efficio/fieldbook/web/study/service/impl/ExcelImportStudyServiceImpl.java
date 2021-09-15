/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * <p/>
 * Generation Challenge Programme (GCP)
 * <p/>
 * <p/>
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *******************************************************************************/

package com.efficio.fieldbook.web.study.service.impl;

import com.efficio.fieldbook.web.common.bean.ChangeType;
import com.efficio.fieldbook.web.study.service.ImportStudyService;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.operation.parser.WorkbookParser;
import org.generationcp.middleware.pojos.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Transactional
public class ExcelImportStudyServiceImpl extends AbstractExcelImportStudyService implements ImportStudyService {

	private static final Logger LOG = LoggerFactory.getLogger(ExcelImportStudyServiceImpl.class);

	private static final String TEMPLATE_DESCRIPTION_SHEET_FIRST_VALUE = "STUDY";
	private static final String TEMPLATE_SECTION_CONDITION = "CONDITION";
	private static final String TEMPLATE_SECTION_FACTOR = "FACTOR";
	private static final String TEMPLATE_SECTION_CONSTANT = "CONSTANT";
	private static final String TEMPLATE_SECTION_VARIATE = "VARIATE";
	private static final int COLUMN_NAME = 0;
	private static final int COLUMN_DESCRIPTION = 1;
	private static final int COLUMN_PROPERTY = 2;
	private static final int COLUMN_SCALE = 3;
	private static final int COLUMN_METHOD = 4;
	public static final int EXCEL_OBSERVATION_SHEET_NUMBER = 1;

	@Resource
	private ResourceBundleMessageSource messageSource;

	private static final String STUDY = "STUDY";
	private static final String TRIAL = "TRIAL";

	@Resource
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Resource
	private ContextUtil contextUtil;

	public ExcelImportStudyServiceImpl(final Workbook workbook, final String currentFile, final String originalFileName) {
		super(workbook, currentFile, originalFileName);
	}

	@Override
	protected void detectAddedTraitsAndPerformRename(final Set<ChangeType> modes) {
		final List<String> xlsVariates = new ArrayList<>();
		final Sheet descriptionSheet = this.parsedData.getSheetAt(0);
		final int variateRow = this.findRow(descriptionSheet, ExcelImportStudyServiceImpl.TEMPLATE_SECTION_VARIATE);
		for (int i = variateRow + 1; i <= descriptionSheet.getLastRowNum(); i++) {
			if (descriptionSheet.getRow(i) != null && descriptionSheet.getRow(i).getCell(0) != null) {
				final Cell cell = descriptionSheet.getRow(i).getCell(0);
				if (cell.getStringCellValue() != null && !"".equalsIgnoreCase(cell.getStringCellValue())) {
					xlsVariates.add(cell.getStringCellValue());
				}
			}
		}
		final List<String> wbVariates = new ArrayList<>();
		for (final MeasurementVariable variate : this.workbook.getVariates()) {
			wbVariates.add(variate.getName());
		}
		for (int i = 0; i < xlsVariates.size(); i++) {
			final String xlsVariate = xlsVariates.get(i);
			for (final String wbVariate : wbVariates) {
				if (xlsVariate.equalsIgnoreCase(wbVariate)) {
					xlsVariates.remove(xlsVariate);
					wbVariates.remove(wbVariate);
					i--;
					break;
				}
			}
		}
		if (!xlsVariates.isEmpty()) {
			modes.add(ChangeType.ADDED_TRAITS);
		}
		if (!wbVariates.isEmpty()) {
			modes.add(ChangeType.DELETED_TRAITS);
		}
	}

	@Override
	protected void detectAddedTraitsAndPerformRename(final Set<ChangeType> modes, final List<String> addedVariates,
			final List<String> removedVariates) {
		final Sheet descriptionSheet = this.parsedData.getSheetAt(0);
		final int variateRow = this.findRow(descriptionSheet, ExcelImportStudyServiceImpl.TEMPLATE_SECTION_VARIATE);
		for (int i = variateRow + 1; i <= descriptionSheet.getLastRowNum(); i++) {
			if (descriptionSheet.getRow(i) != null && descriptionSheet.getRow(i).getCell(0) != null) {
				final Cell cell = descriptionSheet.getRow(i).getCell(0);
				if (cell.getStringCellValue() != null && !"".equalsIgnoreCase(cell.getStringCellValue())) {
					addedVariates.add(cell.getStringCellValue());
				}
			}
		}

		for (final MeasurementVariable variate : this.workbook.getVariates()) {
			removedVariates.add(variate.getName());
		}
		for (int i = 0; i < addedVariates.size(); i++) {
			final String xlsVariate = addedVariates.get(i);
			for (final String wbVariate : removedVariates) {
				if (xlsVariate.equalsIgnoreCase(wbVariate)) {
					addedVariates.remove(xlsVariate);
					removedVariates.remove(wbVariate);
					i--;
					break;
				}
			}
		}
		if (!addedVariates.isEmpty()) {
			modes.add(ChangeType.ADDED_TRAITS);
		}
		if (!removedVariates.isEmpty()) {
			modes.add(ChangeType.DELETED_TRAITS);
		}
	}

	@Override
	public int getObservationSheetNumber() {
		return EXCEL_OBSERVATION_SHEET_NUMBER;
	}

	@Override
	void validateObservationColumns() throws WorkbookParserException {
		final Sheet obsSheet = this.parsedData.getSheetAt(1);
		final int entryCol = this.findColumn(obsSheet, this.getColumnLabel(this.workbook, TermId.ENTRY_NO.getId()));
		int plotCol = this.findColumn(obsSheet, this.getColumnLabel(this.workbook, TermId.PLOT_NO.getId()));
		if (plotCol == -1) {
			plotCol = this.findColumn(obsSheet, this.getColumnLabel(this.workbook, TermId.PLOT_NNO.getId()));
		}
		final int gidCol = this.findColumn(obsSheet, this.getColumnLabel(this.workbook, TermId.GID.getId()));
		final int desigCol = this.findColumn(obsSheet, this.getColumnLabel(this.workbook, TermId.DESIG.getId()));
		final int obsUnitId = this.findColumn(obsSheet, this.getColumnLabel(this.workbook, TermId.OBS_UNIT_ID.getId()));

		if (entryCol <= -1 || plotCol <= -1 || gidCol <= -1 || desigCol <= -1 || obsUnitId <= -1) {
			throw new WorkbookParserException("error.workbook.import.requiredColumnsMissing");
		}
	}

	@Override
	void validateImportMetadata() throws WorkbookParserException {
		this.validateNumberOfSheets(this.parsedData);

		final Sheet descriptionSheet = this.parsedData.getSheetAt(0);
		this.validateDescriptionSheetFirstCell(descriptionSheet);
		this.validateSections(descriptionSheet);

		this.validateVariates(this.parsedData, this.workbook);
	}

	@Override
	protected void performWorkbookMetadataUpdate() throws WorkbookParserException {
		final Map<String, Object> variableMap = new HashMap<>();
		final WorkbookParser parser = new WorkbookParser();
		final org.apache.poi.ss.usermodel.Workbook excelWorkbook = parser.loadFileToExcelWorkbook(new File(this.currentFile));

		final Workbook descriptionWorkbook =
			parser.parseFile(excelWorkbook, false, false, String.valueOf(this.contextUtil.getCurrentWorkbenchUserId()));
		final Workbook originalWorkbook = this.workbook;

		final List<MeasurementRow> trialObservations =
				this.filterObservationsByTrialInstance(this.workbook.getTrialObservations(), this.getTrialInstanceNumber(this.parsedData));
		final Map<Object, String> originalValueMap = new HashMap<>();

		if (this.workbook != null && descriptionWorkbook != null) {
			for (final MeasurementVariable var : descriptionWorkbook.getConditions()) {

				if (var.getLabel() != null && var.getLabel().equalsIgnoreCase(ExcelImportStudyServiceImpl.STUDY)) {
					// study conditions
					// we get from the conditions
					this.setDataToMatchingMeasurementVariable(originalWorkbook.getConditions(), var, originalValueMap, variableMap);

				} else if (var.getLabel() != null && var.getLabel().equalsIgnoreCase(ExcelImportStudyServiceImpl.TRIAL)) {
					// trial level conditions
					this.setDataToMatchingMeasurementData(trialObservations, var,originalValueMap,
							variableMap);
				}

			}

			for (final MeasurementVariable var : descriptionWorkbook.getConstants()) {

				if (var.getLabel() != null && var.getLabel().equalsIgnoreCase(ExcelImportStudyServiceImpl.STUDY)) {
					// study conditions
					// we get from the conditions
					this.setDataToMatchingMeasurementVariable(originalWorkbook.getConstants(), var, originalValueMap, variableMap);

				} else if (var.getLabel() != null && var.getLabel().equalsIgnoreCase(ExcelImportStudyServiceImpl.TRIAL)) {
					// trial level conditions
					// we check if its in constants but not in trial observations
					for (final MeasurementVariable constantsVar : originalWorkbook.getConstants()) {
						boolean isFound = false;
						if (!trialObservations.isEmpty()) {
							for (final MeasurementRow temp : trialObservations) {
								for (final MeasurementData data : temp.getDataList()) {
									if (data.getMeasurementVariable().getTermId() == constantsVar.getTermId()) {
										isFound = true;
										break;
									}
								}
								if (isFound) {
									break;
								} else {
									// we need to add it
									final MeasurementData newData =
											new MeasurementData(constantsVar.getName(), "", false, constantsVar.getDataType(),
													constantsVar);
									temp.getDataList().add(newData);
								}
							}
						}
					}
					this.setDataToMatchingMeasurementData(trialObservations, var, originalValueMap, variableMap);
				}
			}
			this.setCorrectBreedingMethodInfo(variableMap);

		}
	}

	private void setCorrectBreedingMethodInfo(final Map<String, Object> variableMap) {
		// we check for special pair variables here and ensure the name is correct
		try {
			// BM_ID, BM_METHOD_CODE
			if (variableMap.containsKey(Integer.toString(TermId.BREEDING_METHOD_ID.getId()))) {
				// we set the code and name accordingly
				final Object tempObj = variableMap.get(Integer.toString(TermId.BREEDING_METHOD_ID.getId()));
				final Object tempObjCode = variableMap.get(Integer.toString(TermId.BREEDING_METHOD_CODE.getId()));
				final Object tempObjName = variableMap.get(Integer.toString(TermId.BREEDING_METHOD.getId()));
				if (tempObj instanceof MeasurementVariable) {
					final MeasurementVariable tempVar = (MeasurementVariable) tempObj;
					final MeasurementVariable tempVarCode = tempObjCode != null ? (MeasurementVariable) tempObjCode : null;
					final MeasurementVariable tempVarName = tempObjName != null ? (MeasurementVariable) tempObjName : null;
					if (tempVar.getValue() != null && !"".equalsIgnoreCase(tempVar.getValue()) && NumberUtils
							.isNumber(tempVar.getValue())) {
						final Method method = this.fieldbookMiddlewareService.getMethodById(Integer.parseInt(tempVar.getValue()));
						if (tempVarCode != null) {
							// we set the proper code
							tempVarCode.setValue(method != null ? method.getMcode() : "");
						}
						if (tempVarName != null) {
							tempVarName.setValue(method != null ? method.getMname() : "");
						}
					} else {
						// we set the bm code and bm to empty string
						if (tempVarCode != null) {
							// we set the proper code
							tempVarCode.setValue("");
						}
						if (tempVarName != null) {
							tempVarName.setValue("");
						}
					}
				}
			} else if (variableMap.containsKey(Integer.toString(TermId.BREEDING_METHOD_CODE.getId()))) {
				// we just set the name
				final Object tempObjCode = variableMap.get(Integer.toString(TermId.BREEDING_METHOD_CODE.getId()));
				final Object tempObjName = variableMap.get(Integer.toString(TermId.BREEDING_METHOD.getId()));
				final MeasurementVariable tempVarCode = tempObjCode != null ? (MeasurementVariable) tempObjCode : null;
				final MeasurementVariable tempVarName = tempObjName != null ? (MeasurementVariable) tempObjName : null;
				if (tempVarCode != null && !"".equalsIgnoreCase(tempVarCode.getValue())) {
					final Method method = this.fieldbookMiddlewareService.getMethodByCode(tempVarCode.getValue());
					if (tempVarName != null) {
						tempVarName.setValue(method != null ? method.getMname() : "");
					}
				}
			}
		} catch (final Exception e) {
			ExcelImportStudyServiceImpl.LOG.error(e.getMessage(), e);
		}
	}

	private void setDataToMatchingMeasurementVariable(final List<MeasurementVariable> measurementVarList, final MeasurementVariable var,
			final Map<Object, String> originalValueMap, final Map<String, Object> variableMap) {

		for (final MeasurementVariable temp : measurementVarList) {

			if (this.isMatchingPropertyScaleMethodLabel(var, temp) && WorkbookUtil.isConditionValidate(temp.getTermId())
					&& temp.getTermId() != TermId.TRIAL_INSTANCE_FACTOR.getId()) {

				variableMap.put(Integer.toString(temp.getTermId()), temp);
				originalValueMap.put(temp, temp.getValue());

				try {
					temp.setPossibleValues(this.fieldbookService.getAllPossibleValues(temp.getTermId(), true));
				} catch (final MiddlewareException e) {
					ExcelImportStudyServiceImpl.LOG.error(e.getMessage(), e);
				}

				final String xlsValue;
				if (temp.getPossibleValues() != null && !temp.getPossibleValues().isEmpty()) {
					xlsValue = this.getCategoricalIdCellValue(var, temp);
				} else {
					xlsValue = var.getValue();
				}
				temp.setValue(xlsValue);

				try {
					if (this.validationService.isValidValue(temp, xlsValue, true)) {
						temp.setOperation(Operation.UPDATE);
					}
				} catch (final Exception e) {
					ExcelImportStudyServiceImpl.LOG.error(e.getMessage(), e);
				}
			}
		}
	}

	protected boolean isMatchingPropertyScaleMethodLabel(final MeasurementVariable var, final MeasurementVariable temp) {
		return temp.getProperty().equalsIgnoreCase(var.getProperty()) && temp.getScale().equalsIgnoreCase(var.getScale()) && temp
				.getMethod().equalsIgnoreCase(var.getMethod()) && temp.getLabel().equalsIgnoreCase(var.getLabel());
	}

	private void setDataToMatchingMeasurementData(final List<MeasurementRow> trialObservations, final MeasurementVariable var,
			final Map<Object, String> originalValueMap, final Map<String, Object> variableMap) {

		for (final MeasurementRow temp : trialObservations) {

			for (final MeasurementData data : temp.getDataList()) {

				final MeasurementVariable origVar = data.getMeasurementVariable();

				if (origVar != null && this.isMatchingPropertyScaleMethodLabel(var, origVar)
						&& origVar.getTermId() != TermId.TRIAL_INSTANCE_FACTOR.getId()) {

					variableMap.put(Integer.toString(origVar.getTermId()), data);
					originalValueMap.put(data, data.getValue());

					try {
						origVar.setPossibleValues(this.fieldbookService.getAllPossibleValues(origVar.getTermId(), true));
					} catch (final MiddlewareException e) {
						ExcelImportStudyServiceImpl.LOG.error(e.getMessage(), e);
					}

					final String xlsValue = this.getXlsValue(var, temp, data, origVar);
					data.setValue(xlsValue);

					try {
						if (this.validationService.isValidValue(origVar, xlsValue, true)) {
							origVar.setOperation(Operation.UPDATE);
						}
					} catch (final Exception e) {
						ExcelImportStudyServiceImpl.LOG.error(e.getMessage(), e);
					}
				}
			}
		}
	}

	protected String getXlsValue(final MeasurementVariable var, final MeasurementRow temp, final MeasurementData data,
			final MeasurementVariable origVar) {
		final String xlsValue;
		if (temp != null && origVar.getPossibleValues() != null && !origVar.getPossibleValues().isEmpty()) {
			xlsValue = this.getCategoricalIdCellValue(var, origVar);
			if (origVar.getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()) {
				data.setcValueId(xlsValue);
			}
		} else {
			xlsValue = var.getValue();
		}
		return xlsValue;
	}

	protected String getCategoricalIdCellValue(final MeasurementVariable var, final MeasurementVariable origVar) {
		return ExportImportStudyUtil.getCategoricalIdCellValue(var.getValue(), origVar.getPossibleValues(), true);
	}

	protected void validateNumberOfSheets(final org.apache.poi.ss.usermodel.Workbook xlsBook) throws WorkbookParserException {
		if (xlsBook.getNumberOfSheets() != 2) {
			throw new WorkbookParserException("error.workbook.import.invalidNumberOfSheets");
		}
	}

	private void validateDescriptionSheetFirstCell(final Sheet descriptionSheet) throws WorkbookParserException {
		if (!ExcelImportStudyServiceImpl.TEMPLATE_DESCRIPTION_SHEET_FIRST_VALUE
				.equalsIgnoreCase(descriptionSheet.getRow(0).getCell(0).getStringCellValue())) {
			throw new WorkbookParserException("error.workbook.import.invalidFormatDescriptionSheet");
		}
	}

	private void validateSections(final Sheet descriptionSheet) throws WorkbookParserException {
		final int conditionRow = this.findRow(descriptionSheet, ExcelImportStudyServiceImpl.TEMPLATE_SECTION_CONDITION);
		final int factorRow = this.findRow(descriptionSheet, ExcelImportStudyServiceImpl.TEMPLATE_SECTION_FACTOR);
		final int constantRow = this.findRow(descriptionSheet, ExcelImportStudyServiceImpl.TEMPLATE_SECTION_CONSTANT);
		final int variateRow = this.findRow(descriptionSheet, ExcelImportStudyServiceImpl.TEMPLATE_SECTION_VARIATE);
		if (conditionRow <= 0 || factorRow <= conditionRow || constantRow <= conditionRow || variateRow <= conditionRow) {
			throw new WorkbookParserException("error.workbook.import.invalidSections");
		}
	}

	private int findRow(final Sheet sheet, final String cellValue) {
		final int result = 0;
		for (int i = 0; i <= sheet.getLastRowNum(); i++) {
			final Row row = sheet.getRow(i);
			if (row != null) {
				final Cell cell = row.getCell(0);
				if (cell != null && cell.getStringCellValue() != null && cell.getStringCellValue().equalsIgnoreCase(cellValue)) {
					return i;
				}
			}
		}

		return result;
	}

	protected String getTrialInstanceNumber(final org.apache.poi.ss.usermodel.Workbook xlsBook) throws WorkbookParserException {
		final Sheet descriptionSheet = xlsBook.getSheetAt(0);
		final int conditionRow = this.findRow(descriptionSheet, ExcelImportStudyServiceImpl.TEMPLATE_SECTION_CONDITION);
		final int factorRow = this.findRow(descriptionSheet, ExcelImportStudyServiceImpl.TEMPLATE_SECTION_FACTOR);

		String trialInstance = null;

		for (int indexRow = conditionRow + 1; indexRow < factorRow; indexRow++) {
			Integer stdVarId;
			try {
				final Row row = descriptionSheet.getRow(indexRow);
				// we need to check the PSM-R
				final Cell propertyCell = row.getCell(2);
				final Cell scaleCell = row.getCell(3);
				final Cell methodCell = row.getCell(4);
				final Cell labelCell = row.getCell(7);

				stdVarId = null;

				if (this.isPropertyScaleMethodLabelCellNotNull(propertyCell, scaleCell, methodCell, labelCell) && this
						.isPropertyScaleMethodLabelCellHasStringValue(propertyCell, scaleCell, methodCell, labelCell)) {
					// we get the corresponding standard variable id
					stdVarId = this.fieldbookMiddlewareService
							.getStandardVariableIdByPropertyScaleMethodRole(propertyCell.getStringCellValue(),
									scaleCell.getStringCellValue(), methodCell.getStringCellValue(),
									PhenotypicType.getPhenotypicTypeForLabel(labelCell.getStringCellValue()));
				}

				if (stdVarId != null && stdVarId == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
					final Cell cell = row.getCell(6);
					if (cell == null) {
						trialInstance = "1";
					} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
						final Double temp = cell.getNumericCellValue();

						trialInstance = Integer.toString(temp.intValue());
					} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
						trialInstance = cell.getStringCellValue();
					}
					break;
				}
			} catch (final Exception e) {
				// cell might be null
				// we won't throw error, since we need to check other variable
				ExcelImportStudyServiceImpl.LOG.error(e.getMessage(), e);
			}
		}

		if (trialInstance == null || "".equalsIgnoreCase(trialInstance)) {
			throw new WorkbookParserException("error.workbook.import.missing.trial.instance");
		}

		return trialInstance;
	}

	protected boolean isPropertyScaleMethodLabelCellHasStringValue(final Cell propertyCell, final Cell scaleCell, final Cell methodCell,
			final Cell labelCell) {
		return propertyCell.getStringCellValue() != null && scaleCell.getStringCellValue() != null
				&& methodCell.getStringCellValue() != null && labelCell.getStringCellValue() != null;
	}

	protected boolean isPropertyScaleMethodLabelCellNotNull(final Cell propertyCell, final Cell scaleCell, final Cell methodCell,
			final Cell labelCell) {
		return propertyCell != null && scaleCell != null && methodCell != null && labelCell != null;
	}

	protected List<MeasurementRow> filterObservationsByTrialInstance(final List<MeasurementRow> observations,
			final String trialInstanceNumber) {
		if (trialInstanceNumber != null) {
			return WorkbookUtil.filterObservationsByTrialInstance(observations, trialInstanceNumber);
		}
		return new ArrayList<>();
	}

	private void validateVariates(final org.apache.poi.ss.usermodel.Workbook xlsBook, final Workbook workbook)
			throws WorkbookParserException {
		final Sheet descriptionSheet = xlsBook.getSheetAt(0);
		final int variateRow = this.findRow(descriptionSheet, ExcelImportStudyServiceImpl.TEMPLATE_SECTION_VARIATE);
		final List<MeasurementVariable> workbookVariates = workbook.getVariates();
		for (int i = variateRow + 1; i <= descriptionSheet.getLastRowNum(); i++) {
			final Row row = descriptionSheet.getRow(i);
			if (row != null) {
				final Cell cell = row.getCell(0);
				if (cell != null && cell.getStringCellValue() != null) {
					final String traitLabel = cell.getStringCellValue();
					final Integer mvarId = WorkbookUtil.getMeasurementVariableId(workbookVariates, traitLabel);
					// new variates
					if (mvarId == null) {
						final MeasurementVariable mvar;
						try {
							mvar = this.getMeasurementVariable(row);
						} catch (final MiddlewareException e) {
							ExcelImportStudyServiceImpl.LOG.error(e.getMessage(), e);
							throw new WorkbookParserException(this.messageSource
									.getMessage("error.import.variate.duplicate.psmr", new String[] {traitLabel},
											LocaleContextHolder.getLocale()));
						}
						if (mvar == null) {
							throw new WorkbookParserException(this.messageSource
									.getMessage("error.import.variate.does.not.exist", new String[] {traitLabel},
											LocaleContextHolder.getLocale()));
						} else if (WorkbookUtil.getMeasurementVariable(workbookVariates, mvar.getTermId()) != null) {
							throw new WorkbookParserException(this.messageSource
									.getMessage("error.import.variate.exists.in.study", new String[] {traitLabel},
											LocaleContextHolder.getLocale()));
						}
					}

				}
			}
		}
	}

	private MeasurementVariable getMeasurementVariable(final Row row) throws MiddlewareException {
		final String property = row.getCell(ExcelImportStudyServiceImpl.COLUMN_PROPERTY).getStringCellValue();
		final String scale = row.getCell(ExcelImportStudyServiceImpl.COLUMN_SCALE).getStringCellValue();
		final String method = row.getCell(ExcelImportStudyServiceImpl.COLUMN_METHOD).getStringCellValue();
		final MeasurementVariable mvar = this.fieldbookMiddlewareService
				.getMeasurementVariableByPropertyScaleMethodAndRole(property, scale, method, PhenotypicType.VARIATE,
						this.contextUtil.getCurrentProgramUUID());
		if (mvar != null) {
			mvar.setName(row.getCell(ExcelImportStudyServiceImpl.COLUMN_NAME).getStringCellValue());
			mvar.setDescription(row.getCell(ExcelImportStudyServiceImpl.COLUMN_DESCRIPTION).getStringCellValue());
		}
		return mvar;
	}
}
