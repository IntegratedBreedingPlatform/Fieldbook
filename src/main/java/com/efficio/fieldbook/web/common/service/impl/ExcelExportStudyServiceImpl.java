/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import com.efficio.fieldbook.web.common.service.ExcelExportStudyService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.ZipUtil;

@Service
@Transactional
public class ExcelExportStudyServiceImpl implements ExcelExportStudyService {

	private static final Logger LOG = LoggerFactory.getLogger(ExcelExportStudyServiceImpl.class);

	private static final int PIXEL_SIZE = 250;

	private static final String OCC_8170_LABEL = "8170_LABEL";
	private static final String PLOT = "PLOT";

	@Resource
	private MessageSource messageSource;

	@Resource
	private FieldbookProperties fieldbookProperties;

	@Resource
	private OntologyService ontologyService;

	@Resource
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	protected static final List<Integer> STUDY_DETAILS_IDS = Arrays.asList(TermId.STUDY_NAME.getId(), TermId.STUDY_TITLE.getId(),
			TermId.PM_KEY.getId(), TermId.STUDY_OBJECTIVE.getId(), TermId.START_DATE.getId(), TermId.END_DATE.getId(),
			TermId.STUDY_TYPE.getId(), TermId.STUDY_UID.getId(), TermId.STUDY_STATUS.getId());
	private String breedingMethodPropertyName = "";

	@Override
	public String export(Workbook workbook, String filename, List<Integer> instances) {
		return this.export(workbook, filename, instances, null);
	}

	@Override
	public String export(Workbook workbook, String filename, List<Integer> instances, List<Integer> visibleColumns) {
		FileOutputStream fos = null;
		List<String> filenameList = new ArrayList<String>();
		String outputFilename = null;

		try {
			this.breedingMethodPropertyName = this.ontologyService.getProperty(TermId.BREEDING_METHOD_PROP.getId()).getTerm().getName();
		} catch (MiddlewareQueryException e) {
			ExcelExportStudyServiceImpl.LOG.error(e.getMessage(), e);
		}

		for (Integer index : instances) {
			List<Integer> indexes = new ArrayList<Integer>();
			indexes.add(index);

			List<MeasurementRow> observations =
					ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getExportArrangedObservations(), indexes);
			List<MeasurementRow> trialObservations =
					ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getTrialObservations(), indexes);
			try {
				MeasurementRow trialObservation = trialObservations.get(0);

				HSSFWorkbook xlsBook = new HSSFWorkbook();

				this.writeDescriptionSheet(xlsBook, workbook, trialObservation, visibleColumns);
				this.writeObservationSheet(xlsBook, workbook, observations, visibleColumns);

				String filenamePath =
						this.getFileNamePath(index, workbook.getTrialObservations().get(index - 1), instances, filename,
								workbook.isNursery());
				fos = new FileOutputStream(new File(filenamePath));
				xlsBook.write(fos);
				outputFilename = filenamePath;
				filenameList.add(filenamePath);

			} catch (Exception e) {
				ExcelExportStudyServiceImpl.LOG.error(e.getMessage(), e);
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (Exception e) {
						ExcelExportStudyServiceImpl.LOG.error(e.getMessage(), e);
					}
				}
			}
		}

		if (instances != null && instances.size() > 1) {
			outputFilename =
					this.fieldbookProperties.getUploadDirectory() + File.separator
							+ filename.replaceAll(AppConstants.EXPORT_XLS_SUFFIX.getString(), "")
							+ AppConstants.ZIP_FILE_SUFFIX.getString();
			ZipUtil.zipIt(outputFilename, filenameList);
		}

		return outputFilename;
	}

	protected String getFileNamePath(int index, MeasurementRow trialObservation, List<Integer> instances, String filename, boolean isNursery) {
		String filenamePath = this.fieldbookProperties.getUploadDirectory() + File.separator + SettingsUtil.cleanSheetAndFileName(filename);
		if (instances != null && (instances.size() > 1 || !isNursery)) {
			int fileExtensionIndex = filenamePath.lastIndexOf(".");
			String siteName = ExportImportStudyUtil.getSiteNameOfTrialInstance(trialObservation, this.fieldbookMiddlewareService);
			if (instances.size() > 1) {
				return filenamePath.substring(0, fileExtensionIndex) + "-" + index + SettingsUtil.cleanSheetAndFileName(siteName)
						+ filenamePath.substring(fileExtensionIndex);
			} else {
				return filename.substring(0, filename.lastIndexOf(".")) + "-" + index + SettingsUtil.cleanSheetAndFileName(siteName)
						+ filenamePath.substring(fileExtensionIndex);
			}
		}
		return filenamePath;
	}

	protected void writeDescriptionSheet(HSSFWorkbook xlsBook, Workbook workbook, MeasurementRow trialObservation,
			List<Integer> visibleColumns) {
		Locale locale = LocaleContextHolder.getLocale();
		HSSFSheet xlsSheet = xlsBook.createSheet(this.messageSource.getMessage("export.study.sheet.description", null, locale));
		int currentRowNum = 0;

		currentRowNum = this.writeStudyDetails(currentRowNum, xlsBook, xlsSheet, workbook.getStudyDetails());
		xlsSheet.createRow(currentRowNum++);
		currentRowNum = this.writeConditions(currentRowNum, xlsBook, xlsSheet, workbook.getConditions(), trialObservation, workbook);
		xlsSheet.createRow(currentRowNum++);
		currentRowNum = this.writeFactors(currentRowNum, xlsBook, xlsSheet, workbook.getNonTrialFactors(), visibleColumns, workbook);
		xlsSheet.createRow(currentRowNum++);
		currentRowNum = this.writeConstants(currentRowNum, xlsBook, xlsSheet, workbook.getConstants(), trialObservation, workbook);
		xlsSheet.createRow(currentRowNum++);
		currentRowNum = this.writeVariates(currentRowNum, xlsBook, xlsSheet, workbook.getVariates(), visibleColumns, workbook);

		xlsSheet.setColumnWidth(0, 20 * ExcelExportStudyServiceImpl.PIXEL_SIZE);
		xlsSheet.setColumnWidth(1, 24 * ExcelExportStudyServiceImpl.PIXEL_SIZE);
		xlsSheet.setColumnWidth(2, 30 * ExcelExportStudyServiceImpl.PIXEL_SIZE);
		xlsSheet.setColumnWidth(3, 18 * ExcelExportStudyServiceImpl.PIXEL_SIZE);
		xlsSheet.setColumnWidth(4, 18 * ExcelExportStudyServiceImpl.PIXEL_SIZE);
		xlsSheet.setColumnWidth(5, 15 * ExcelExportStudyServiceImpl.PIXEL_SIZE);
		xlsSheet.setColumnWidth(6, 20 * ExcelExportStudyServiceImpl.PIXEL_SIZE);
		xlsSheet.setColumnWidth(7, 20 * ExcelExportStudyServiceImpl.PIXEL_SIZE);
	}

	protected void writeObservationSheet(HSSFWorkbook xlsBook, Workbook workbook, List<MeasurementRow> observations,
			List<Integer> visibleColumns) {
		Locale locale = LocaleContextHolder.getLocale();
		HSSFSheet xlsSheet = xlsBook.createSheet(this.messageSource.getMessage("export.study.sheet.observation", null, locale));
		int currentRowNum = 0;

		this.writeObservationHeader(currentRowNum++, xlsBook, xlsSheet, workbook.getMeasurementDatasetVariables(), visibleColumns);

		CellStyle style = this.createCellStyle(xlsBook);

		for (MeasurementRow dataRow : observations) {
			this.writeObservationRow(currentRowNum++, xlsSheet, dataRow, workbook.getMeasurementDatasetVariables(), xlsBook, style,
					visibleColumns);
		}
	}

	private CellStyle createCellStyle(HSSFWorkbook xlsBook) {
		CellStyle style = xlsBook.createCellStyle();
		DataFormat format = xlsBook.createDataFormat();
		style.setDataFormat(format.getFormat("0.#"));

		return style;
	}

	private int writeStudyDetails(int currentRowNum, HSSFWorkbook xlsBook, HSSFSheet xlsSheet, StudyDetails studyDetails) {
		int rowNumIndex = currentRowNum;
		this.writeStudyDetailRow(xlsBook, xlsSheet, rowNumIndex++, "export.study.description.details.study",
				studyDetails.getStudyName() != null ? HtmlUtils.htmlUnescape(studyDetails.getStudyName()) : "");
		this.writeStudyDetailRow(xlsBook, xlsSheet, rowNumIndex++, "export.study.description.details.title",
				studyDetails.getTitle() != null ? HtmlUtils.htmlUnescape(studyDetails.getTitle()) : "");
		this.writeStudyDetailRow(xlsBook, xlsSheet, rowNumIndex++, "export.study.description.details.objective",
				studyDetails.getObjective() != null ? HtmlUtils.htmlUnescape(studyDetails.getObjective()) : "");

		String startDate = studyDetails.getStartDate();
		String endDate = studyDetails.getEndDate();

		if (startDate != null) {
			startDate = startDate.replace("-", "");
		}

		if (endDate != null) {
			endDate = endDate.replace("-", "");
		}

		this.writeStudyDetailRow(xlsBook, xlsSheet, rowNumIndex++, "export.study.description.details.startdate", startDate);
		this.writeStudyDetailRow(xlsBook, xlsSheet, rowNumIndex++, "export.study.description.details.enddate", endDate);
		this.writeStudyDetailRow(xlsBook, xlsSheet, rowNumIndex++, "export.study.description.details.studytype", studyDetails
				.getStudyType().name());

		return rowNumIndex;
	}

	private int writeConditions(int currentRowNum, HSSFWorkbook xlsBook, HSSFSheet xlsSheet, List<MeasurementVariable> conditions,
			MeasurementRow trialObservation, Workbook workbook) {
		List<MeasurementVariable> arrangedConditions = new ArrayList<MeasurementVariable>();
		List<MeasurementVariable> filteredConditions = new ArrayList<MeasurementVariable>();
		if (conditions != null) {
			arrangedConditions.addAll(conditions);
			Collections.sort(arrangedConditions, new Comparator<MeasurementVariable>() {

				@Override
				public int compare(MeasurementVariable var1, MeasurementVariable var2) {
					return var1.getName().compareToIgnoreCase(var2.getName());
				}
			});

			for (MeasurementVariable variable : arrangedConditions) {
				if (!ExcelExportStudyServiceImpl.STUDY_DETAILS_IDS.contains(variable.getTermId())) {
					filteredConditions.add(variable);
					if (PhenotypicType.TRIAL_ENVIRONMENT.getLabelList().contains(variable.getLabel())) {
						variable.setValue(trialObservation.getMeasurementDataValue(variable.getTermId()));
						if (variable.getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()) {
							try {
								variable.setPossibleValues(this.fieldbookService.getAllPossibleValues(variable.getTermId()));
							} catch (MiddlewareQueryException e) {
								ExcelExportStudyServiceImpl.LOG.error(e.getMessage(), e);
							}
						}
					}
				}
			}
		}
		filteredConditions = workbook.arrangeMeasurementVariables(filteredConditions);
		return this.writeSection(currentRowNum, xlsBook, xlsSheet, filteredConditions, "export.study.description.column.condition", 51,
				153, 102);
	}

	private int writeFactors(int currentRowNum, HSSFWorkbook xlsBook, HSSFSheet xlsSheet, List<MeasurementVariable> factors,
			List<Integer> visibleColumns, Workbook workbook) {
		List<MeasurementVariable> filteredFactors = new ArrayList<MeasurementVariable>();
		for (MeasurementVariable factor : factors) {
			if (factor.getTermId() != TermId.TRIAL_INSTANCE_FACTOR.getId()
					&& ExportImportStudyUtil.isColumnVisible(factor.getTermId(), visibleColumns)) {
				filteredFactors.add(factor);
			}
		}
		filteredFactors = workbook.arrangeMeasurementVariables(filteredFactors);
		return this.writeSection(currentRowNum, xlsBook, xlsSheet, filteredFactors, "export.study.description.column.factor", 51, 153, 102);
	}

	private int writeConstants(int currentRowNum, HSSFWorkbook xlsBook, HSSFSheet xlsSheet, List<MeasurementVariable> constants,
			MeasurementRow trialObservation, Workbook workbook) {

		List<MeasurementVariable> filteredConstants = new ArrayList<MeasurementVariable>();
		for (MeasurementVariable variable : constants) {
			filteredConstants.add(variable);
			if (PhenotypicType.TRIAL_ENVIRONMENT.getLabelList().contains(variable.getLabel())) {
				variable.setValue(trialObservation.getMeasurementDataValue(variable.getName()));
			}
		}
		filteredConstants = workbook.arrangeMeasurementVariables(filteredConstants);
		return this.writeSection(currentRowNum, xlsBook, xlsSheet, filteredConstants, "export.study.description.column.constant", 51, 51,
				153);
	}

	private int writeVariates(int currentRowNum, HSSFWorkbook xlsBook, HSSFSheet xlsSheet, List<MeasurementVariable> variates,
			List<Integer> visibleColumns, Workbook workbook) {

		List<MeasurementVariable> filteredVariates = new ArrayList<MeasurementVariable>();
		for (MeasurementVariable variate : variates) {
			if (ExportImportStudyUtil.isColumnVisible(variate.getTermId(), visibleColumns)) {
				filteredVariates.add(variate);
			}
		}
		filteredVariates = workbook.arrangeMeasurementVariables(filteredVariates);
		return this.writeSection(currentRowNum, xlsBook, xlsSheet, filteredVariates, "export.study.description.column.variate", 51, 51,
				153, true);
	}

	private CellStyle getHeaderStyle(HSSFWorkbook xlsBook, int c1, int c2, int c3) {
		HSSFPalette palette = xlsBook.getCustomPalette();
		HSSFColor color = palette.findSimilarColor(c1, c2, c3);
		short colorIndex = color.getIndex();

		HSSFFont whiteFont = xlsBook.createFont();
		whiteFont.setColor(new HSSFColor.WHITE().getIndex());

		CellStyle cellStyle = xlsBook.createCellStyle();
		cellStyle.setFillForegroundColor(colorIndex);
		cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		cellStyle.setFont(whiteFont);

		return cellStyle;
	}

	private void writeStudyDetailRow(HSSFWorkbook xlsBook, HSSFSheet xlsSheet, int currentRowNum, String label, String value) {
		Locale locale = LocaleContextHolder.getLocale();
		HSSFRow row = xlsSheet.createRow(currentRowNum);
		HSSFCell cell = row.createCell(0, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, 153, 51, 0));
		cell.setCellValue(this.messageSource.getMessage(label, null, locale));
		cell = row.createCell(1, Cell.CELL_TYPE_STRING);
		cell.setCellValue(value);
	}

	private int writeSection(int currentRowNum, HSSFWorkbook xlsBook, HSSFSheet xlsSheet, List<MeasurementVariable> variables,
			String sectionLabel, int c1, int c2, int c3) {

		return this.writeSection(currentRowNum, xlsBook, xlsSheet, variables, sectionLabel, c1, c2, c3, false);
	}

	private int writeSection(int currentRowNum, HSSFWorkbook xlsBook, HSSFSheet xlsSheet, List<MeasurementVariable> variables,
			String sectionLabel, int c1, int c2, int c3, boolean isVariate) {
		int rowNumIndex = currentRowNum;
		this.writeSectionHeader(xlsBook, xlsSheet, rowNumIndex++, sectionLabel, c1, c2, c3);
		if (variables != null && !variables.isEmpty()) {
			for (MeasurementVariable variable : variables) {

				if (isVariate) {
					variable.setLabel(ExcelExportStudyServiceImpl.PLOT);
				}
				this.writeSectionRow(rowNumIndex++, xlsSheet, variable);
			}
		}
		return rowNumIndex;

	}

	private void writeSectionHeader(HSSFWorkbook xlsBook, HSSFSheet xlsSheet, int currentRowNum, String typeLabel, int c1, int c2, int c3) {
		Locale locale = LocaleContextHolder.getLocale();
		HSSFRow row = xlsSheet.createRow(currentRowNum);

		HSSFCell cell = row.createCell(0, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(this.messageSource.getMessage(typeLabel, null, locale));

		cell = row.createCell(1, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.description", null, locale));

		cell = row.createCell(2, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.property", null, locale));

		cell = row.createCell(3, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.scale", null, locale));

		cell = row.createCell(4, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.method", null, locale));

		cell = row.createCell(5, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.datatype", null, locale));

		cell = row.createCell(6, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.value", null, locale));

		// If typeLabel is constant or variate, the label column should be 'SAMPLE LEVEL'
		cell = row.createCell(7, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, c1, c2, c3));

		if ("export.study.description.column.constant".equals(typeLabel) || "export.study.description.column.variate".equals(typeLabel)) {
			cell.setCellValue(this.messageSource.getMessage("export.study.description.column.samplelevel", null, locale));

		} else {
			cell.setCellValue(this.messageSource.getMessage("export.study.description.column.label", null, locale));

		}

	}

	private void writeSectionRow(int currentRowNum, HSSFSheet xlsSheet, MeasurementVariable variable) {
		HSSFRow row = xlsSheet.createRow(currentRowNum);

		HSSFCell cell = row.createCell(0, Cell.CELL_TYPE_STRING);
		String occName = variable.getName();
		String appConstant8170 = AppConstants.getString(ExcelExportStudyServiceImpl.OCC_8170_LABEL);
		if (appConstant8170 != null && appConstant8170.equalsIgnoreCase(occName)) {
			occName = AppConstants.OCC.getString();
		}
		cell.setCellValue(occName);

		cell = row.createCell(1, Cell.CELL_TYPE_STRING);
		cell.setCellValue(variable.getDescription());

		cell = row.createCell(2, Cell.CELL_TYPE_STRING);
		cell.setCellValue(variable.getProperty());

		cell = row.createCell(3, Cell.CELL_TYPE_STRING);
		cell.setCellValue(variable.getScale());

		cell = row.createCell(4, Cell.CELL_TYPE_STRING);
		cell.setCellValue(variable.getMethod());

		cell = row.createCell(5, Cell.CELL_TYPE_STRING);
		cell.setCellValue(variable.getDataTypeDisplay());

		cell = row.createCell(6, Cell.CELL_TYPE_STRING);
		this.cleanupValue(variable);

		try {
			variable.setPossibleValues(this.fieldbookService.getAllPossibleValues(variable.getTermId()));
		} catch (MiddlewareQueryException e) {
			ExcelExportStudyServiceImpl.LOG.error(e.getMessage(), e);
		}

		if (variable != null && variable.getPossibleValues() != null && !variable.getPossibleValues().isEmpty()
				&& variable.getTermId() != TermId.BREEDING_METHOD_VARIATE.getId()
				&& variable.getTermId() != TermId.BREEDING_METHOD_VARIATE_CODE.getId()
				&& !variable.getProperty().equals(this.breedingMethodPropertyName) && variable.getTermId() != TermId.PI_ID.getId()
				&& variable.getTermId() != Integer.parseInt(AppConstants.COOPERATOR_ID.getString())
				&& variable.getTermId() != TermId.LOCATION_ID.getId()) {
			cell.setCellValue(ExportImportStudyUtil.getCategoricalCellValue(variable.getValue(), variable.getPossibleValues()));
		} else if (variable.getDataTypeId() != null && variable.getDataTypeId().equals(TermId.NUMERIC_VARIABLE.getId())) {
			if (variable.getValue() != null && !"".equalsIgnoreCase(variable.getValue())) {
				cell.setCellType(Cell.CELL_TYPE_BLANK);
				cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				cell.setCellValue(Double.valueOf(variable.getValue()));
			} else {
				cell.setCellValue(variable.getValue());
			}
		} else {
			cell.setCellValue(variable.getValue());
		}

		cell = row.createCell(7, Cell.CELL_TYPE_STRING);
		if (variable.getTreatmentLabel() != null && !"".equals(variable.getTreatmentLabel())) {
			cell.setCellValue(variable.getTreatmentLabel());
		} else {
			cell.setCellValue(variable.getLabel());
		}
	}

	private void writeObservationHeader(int currentRowNum, HSSFWorkbook xlsBook, HSSFSheet xlsSheet, List<MeasurementVariable> variables,
			List<Integer> visibleColumns) {
		if (variables != null && !variables.isEmpty()) {
			int currentColNum = 0;
			int rowNumIndex = currentColNum;
			HSSFRow row = xlsSheet.createRow(rowNumIndex++);
			for (MeasurementVariable variable : variables) {
				if (ExportImportStudyUtil.isColumnVisible(variable.getTermId(), visibleColumns)) {
					HSSFCell cell = row.createCell(currentColNum++);
					cell.setCellStyle(this.getObservationHeaderStyle(variable.isFactor(), xlsBook));
					cell.setCellValue(variable.getName());
				}
			}
		}
	}

	protected CellStyle getObservationHeaderStyle(boolean isFactor, HSSFWorkbook xlsBook) {
		CellStyle style;
		if (isFactor) {
			style = this.getHeaderStyle(xlsBook, 51, 153, 102);
		} else {
			style = this.getHeaderStyle(xlsBook, 51, 51, 153);
		}
		return style;
	}

	private void writeObservationRow(int currentRowNum, HSSFSheet xlsSheet, MeasurementRow dataRow, List<MeasurementVariable> variables,
			HSSFWorkbook xlsBook, CellStyle style, List<Integer> visibleColumns) {

		HSSFRow row = xlsSheet.createRow(currentRowNum);
		int currentColNum = 0;

		for (MeasurementVariable variable : variables) {

			MeasurementData dataCell = dataRow.getMeasurementData(variable.getTermId());
			if (dataCell != null) {
				if (dataCell.getMeasurementVariable() != null
						&& dataCell.getMeasurementVariable().getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()
						|| !ExportImportStudyUtil.isColumnVisible(dataCell.getMeasurementVariable().getTermId(), visibleColumns)) {
					continue;
				}
				HSSFCell cell = row.createCell(currentColNum++);

				if (ExportImportStudyUtil.measurementVariableHasValue(dataCell)
						&& !dataCell.getMeasurementVariable().getPossibleValues().isEmpty()
						&& dataCell.getMeasurementVariable().getTermId() != TermId.BREEDING_METHOD_VARIATE.getId()
						&& dataCell.getMeasurementVariable().getTermId() != TermId.BREEDING_METHOD_VARIATE_CODE.getId()
						&& !dataCell.getMeasurementVariable().getProperty()
								.equals(ExportImportStudyUtil.getPropertyName(this.ontologyService))) {

					cell.setCellValue(ExportImportStudyUtil.getCategoricalCellValue(dataCell.getValue(), dataCell.getMeasurementVariable()
							.getPossibleValues()));

				} else {

					if (AppConstants.NUMERIC_DATA_TYPE.getString().equalsIgnoreCase(dataCell.getDataType())) {
						if (dataCell.getValue() != null && !"".equalsIgnoreCase(dataCell.getValue())
								&& NumberUtils.isNumber(dataCell.getValue())) {
							cell.setCellType(Cell.CELL_TYPE_BLANK);
							cell.setCellType(Cell.CELL_TYPE_NUMERIC);
							cell.setCellValue(Double.valueOf(dataCell.getValue()));
						} else {
							cell.setCellType(Cell.CELL_TYPE_STRING);
							cell.setCellValue(dataCell.getValue());
						}
					} else {
						cell.setCellValue(dataCell.getValue());
					}
				}
			}
		}
	}

	private void cleanupValue(MeasurementVariable variable) {
		if (variable.getValue() != null) {
			variable.setValue(variable.getValue().trim());
			List<Integer> specialDropdowns = this.getSpecialDropdownIds();
			if (specialDropdowns.contains(variable.getTermId()) && "0".equals(variable.getValue())) {
				variable.setValue("");
			} else if (variable.getDataTypeId().equals(TermId.DATE_VARIABLE.getId()) && "0".equals(variable.getValue())) {
				variable.setValue("");
			}
		}
	}

	private List<Integer> getSpecialDropdownIds() {
		List<Integer> ids = new ArrayList<Integer>();

		String idNameCombo = AppConstants.ID_NAME_COMBINATION.getString();
		String[] idNames = idNameCombo.split(",");
		for (String idName : idNames) {
			ids.add(Integer.valueOf(idName.substring(0, idName.indexOf("|"))));
		}

		return ids;
	}

	protected void setFieldbookService(com.efficio.fieldbook.service.api.FieldbookService fieldbookService) {
		this.fieldbookService = fieldbookService;
	}

	protected void setFieldbookMiddlewareService(org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}
}
