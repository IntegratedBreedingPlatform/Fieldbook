
package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportColumnValue;
import org.generationcp.commons.service.ExportService;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.efficio.fieldbook.web.common.service.CsvExportStudyService;
import com.efficio.fieldbook.web.nursery.service.impl.ValidationServiceImpl;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.ZipUtil;

@Service
@Transactional
public class CsvExportStudyServiceImpl implements CsvExportStudyService {

	private static final Logger LOG = LoggerFactory.getLogger(CsvExportStudyServiceImpl.class);

	@Resource
	private FieldbookProperties fieldbookProperties;

	@Resource
	private OntologyService ontologyService;

	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Resource
	private ExportService exportService;

	@Override
	public String export(Workbook workbook, String filename, List<Integer> instances) {
		return this.export(workbook, filename, instances, null);
	}

	@Override
	public String export(Workbook workbook, String filename, List<Integer> instances, List<Integer> visibleColumns) {

		FileOutputStream fos = null;
		List<String> filenameList = new ArrayList<String>();
		String outputFilename = null;

		for (Integer index : instances) {
			List<Integer> indexes = new ArrayList<Integer>();
			indexes.add(index);

			List<MeasurementRow> observations = this.getApplicableObservations(workbook, indexes);

			try {

				String filenamePath =
						this.getFileNamePath(index, workbook.getTrialObservations().get(index - 1), instances, filename,
								workbook.isNursery());

				List<ExportColumnHeader> exportColumnHeaders =
						this.getExportColumnHeaders(visibleColumns, workbook.getMeasurementDatasetVariables());
				List<Map<Integer, ExportColumnValue>> exportColumnValues =
						this.getExportColumnValues(exportColumnHeaders, workbook.getMeasurementDatasetVariables(), observations);

				this.exportService.generateCSVFile(exportColumnValues, exportColumnHeaders, filenamePath);

				outputFilename = filenamePath;
				filenameList.add(filenamePath);

			} catch (Exception e) {
				CsvExportStudyServiceImpl.LOG.error(e.getMessage(), e);
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (Exception e) {
						CsvExportStudyServiceImpl.LOG.error(e.getMessage(), e);
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

	protected List<MeasurementRow> getApplicableObservations(Workbook workbook, List<Integer> indexes) {
		return ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getExportArrangedObservations(), indexes);
	}

	protected String getFileNamePath(int index, MeasurementRow trialObservation, List<Integer> instances, String filename, boolean isNursery)
			throws MiddlewareQueryException {
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

	protected List<ExportColumnHeader> getExportColumnHeaders(List<Integer> visibleColumns, List<MeasurementVariable> variables) {

		List<ExportColumnHeader> exportColumnHeaders = new ArrayList<>();

		if (variables != null && !variables.isEmpty()) {
			for (MeasurementVariable variable : variables) {
				if (visibleColumns == null) {
					exportColumnHeaders.add(new ExportColumnHeader(variable.getTermId(), variable.getName(), true));
				} else {
					exportColumnHeaders.add(this.getColumnsBasedOnVisibility(visibleColumns, variable));
				}

			}
		}

		return exportColumnHeaders;
	}

	protected ExportColumnHeader getColumnsBasedOnVisibility(List<Integer> visibleColumns, MeasurementVariable variable) {
		if (visibleColumns.contains(variable.getTermId()) || ExportImportStudyUtil.partOfRequiredColumns(variable.getTermId())) {
			return new ExportColumnHeader(variable.getTermId(), variable.getName(), true);
		} else {
			return new ExportColumnHeader(variable.getTermId(), variable.getName(), false);
		}
	}

	protected List<Map<Integer, ExportColumnValue>> getExportColumnValues(List<ExportColumnHeader> columns,
			List<MeasurementVariable> variables, List<MeasurementRow> observations) {

		List<Map<Integer, ExportColumnValue>> exportColumnValues = new ArrayList<>();

		for (MeasurementRow dataRow : observations) {
			exportColumnValues.add(this.getColumnValueMap(columns, dataRow));
		}

		return exportColumnValues;
	}

	protected Map<Integer, ExportColumnValue> getColumnValueMap(List<ExportColumnHeader> columns, MeasurementRow dataRow) {
		Map<Integer, ExportColumnValue> columnValueMap = new HashMap<>();

		for (ExportColumnHeader column : columns) {
			Integer termId = column.getId();
			MeasurementData dataCell = dataRow.getMeasurementData(termId);

			if (column.isDisplay() && dataCell != null) {
				if (dataCell.getMeasurementVariable() != null
						&& dataCell.getMeasurementVariable().getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
					continue;
				}

				columnValueMap.put(termId, this.getColumnValue(dataCell, termId));

			}
		}

		return columnValueMap;
	}

	protected ExportColumnValue getColumnValue(MeasurementData dataCell, Integer termId) {
		ExportColumnValue columnValue = null;

		if (ExportImportStudyUtil.measurementVariableHasValue(dataCell) && !dataCell.getMeasurementVariable().getPossibleValues().isEmpty()
				&& dataCell.getMeasurementVariable().getTermId() != TermId.BREEDING_METHOD_VARIATE.getId()
				&& dataCell.getMeasurementVariable().getTermId() != TermId.BREEDING_METHOD_VARIATE_CODE.getId()
				&& !dataCell.getMeasurementVariable().getProperty().equals(ExportImportStudyUtil.getPropertyName(this.ontologyService))) {

			String value = this.getCategoricalCellValue(dataCell);
			columnValue = new ExportColumnValue(termId, value);

		} else {

			if (AppConstants.NUMERIC_DATA_TYPE.getString().equalsIgnoreCase(dataCell.getDataType())) {

				columnValue = this.getNumericColumnValue(dataCell, termId);

			} else {
				columnValue = new ExportColumnValue(termId, dataCell.getValue());
			}

		}
		return columnValue;
	}

	protected ExportColumnValue getNumericColumnValue(MeasurementData dataCell, Integer termId) {
		ExportColumnValue columnValue = null;
		String cellVal = "";

		if (dataCell.getValue() != null && !"".equalsIgnoreCase(dataCell.getValue())) {
			if (ValidationServiceImpl.MISSING_VAL.equalsIgnoreCase(dataCell.getValue())) {
				cellVal = dataCell.getValue();
			} else {
				cellVal = Double.valueOf(dataCell.getValue()).toString();
			}
			columnValue = new ExportColumnValue(termId, cellVal);
		}
		return columnValue;
	}

	protected String getCategoricalCellValue(MeasurementData dataCell) {
		return ExportImportStudyUtil.getCategoricalCellValue(dataCell.getValue(), dataCell.getMeasurementVariable().getPossibleValues());
	}

	public void setOntologyService(OntologyService ontologyService) {
		this.ontologyService = ontologyService;
	}

	public void setFieldbookProperties(FieldbookProperties fieldbookProperties) {
		this.fieldbookProperties = fieldbookProperties;
	}

	public void setExportService(ExportService exportService) {
		this.exportService = exportService;
	}
}
