
package com.efficio.etl.service.impl;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.service.FileService;
import com.efficio.etl.web.bean.IndexValueDTO;
import com.efficio.etl.web.bean.RowDTO;
import com.efficio.etl.web.bean.SheetDTO;
import com.efficio.etl.web.bean.UserSelection;
import com.efficio.etl.web.bean.VariableDTO;
import com.efficio.etl.web.util.AppConstants;
import com.efficio.fieldbook.web.exception.FieldbookRequestValidationException;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.dms.DMSVariableType;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.Constants;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.operation.parser.WorkbookParser;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.util.Message;
import org.generationcp.middleware.util.PoiUtil;
import org.generationcp.middleware.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ResourceBundleMessageSource;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public class ETLServiceImpl implements ETLService {

	private static final Logger LOG = LoggerFactory.getLogger(ETLServiceImpl.class);

	public static final int DESCRIPTION_SHEET = 0;
	public static final int OBSERVATION_SHEET = 1;

	public static final int STUDY_NAME_ROW_INDEX = 0;
	public static final int STUDY_TITLE_ROW_INDEX = 1;
	public static final int PMKEY_ROW_INDEX = 2;
	public static final int OBJECTIVE_ROW_INDEX = 3;
	public static final int START_DATE_ROW_INDEX = 4;
	public static final int END_DATE_ROW_INDEX = 5;
	public static final int STUDY_TYPE_ROW_INDEX = 6;
	public static final int STUDY_DETAILS_LABEL_COLUMN_INDEX = 0;
	public static final int STUDY_DETAILS_VALUE_COLUMN_INDEX = 1;
	public static final String PMKEY_LABEL = "PMKEY";

	private int maxRowLimit = WorkbookParser.DEFAULT_MAX_ROW_LIMIT;

	@Resource(name = "etlFileService")
	private FileService fileService;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private DataImportService dataImportService;

	@Resource
	private StudyDataManager studyDataManager;

	@Resource
	private WorkbenchDataManager workbenchDataManager;

	@Resource
	private ResourceBundleMessageSource messageSource;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	private OntologyService ontologyService;

	@Override
	public String storeUserWorkbook(final InputStream in) throws IOException {
		return this.getFileService().saveTemporaryFile(in);
	}

	@Override
	public org.generationcp.middleware.domain.etl.Workbook convertToWorkbook(final UserSelection userSelection) {
		final org.generationcp.middleware.domain.etl.Workbook wb = new org.generationcp.middleware.domain.etl.Workbook();

		wb.setStudyDetails(this.convertToStudyDetails(userSelection));
		wb.setImportType(userSelection.getDatasetType());

		final Set<PhenotypicType> phenoTypicKey = new LinkedHashSet<>();
		// always follow the order: TE, G, TD, V
		phenoTypicKey.add(PhenotypicType.TRIAL_ENVIRONMENT);
		phenoTypicKey.add(PhenotypicType.GERMPLASM);
		phenoTypicKey.add(PhenotypicType.TRIAL_DESIGN);
		phenoTypicKey.add(PhenotypicType.VARIATE);
		phenoTypicKey.add(PhenotypicType.ENTRY_DETAIL);
		final Iterator<PhenotypicType> iter = phenoTypicKey.iterator();

		final List<MeasurementVariable> conditions = new ArrayList<>();
		final List<MeasurementVariable> factors = new ArrayList<>();
		final List<MeasurementVariable> variates = new ArrayList<>();
		final List<MeasurementVariable> entryDetails = new ArrayList<>();

		// loop to all phenotypickeys in the giant Hashmap
		while (iter.hasNext()) {
			final PhenotypicType pheno = iter.next();

			final Map<String, MeasurementVariable> currentItem = userSelection
				.getMeasurementVariablesByPhenotypic(pheno);
			if (currentItem == null || currentItem.isEmpty()) {
				continue;
			}

			final Set<String> itemKeys = currentItem.keySet();
			final Iterator<String> itemIterator = itemKeys.iterator();

			// loop to all strings under the hashmap of phenotypic maps
			while (itemIterator.hasNext()) {
				final String nextKey = itemIterator.next();
				final MeasurementVariable mv = currentItem.get(nextKey);

				if (PhenotypicType.TRIAL_ENVIRONMENT.compareTo(pheno) == 0
					|| PhenotypicType.DATASET.compareTo(pheno) == 0 || PhenotypicType.STUDY.compareTo(pheno) == 0
					|| PhenotypicType.GERMPLASM.compareTo(pheno) == 0
					|| pheno.compareTo(PhenotypicType.TRIAL_DESIGN) == 0) {
					factors.add(mv);
				} else if (PhenotypicType.VARIATE.compareTo(pheno) == 0) {
					variates.add(mv);
				} else if (PhenotypicType.ENTRY_DETAIL.compareTo(pheno) == 0) {
					entryDetails.add(mv);
				}
			}
		}

		wb.setConditions(conditions);
		wb.setFactors(factors);
		wb.setVariates(variates);
		wb.setEntryDetails(entryDetails);

		wb.setConstants(null);

		// set ids
		wb.getStudyDetails().setId(userSelection.getStudyId());
		wb.setTrialDatasetId(userSelection.getTrialDatasetId());
		wb.setMeasurementDatesetId(userSelection.getMeasurementDatasetId());
		wb.setMeansDatasetId(userSelection.getMeansDatasetId());
		return wb;

	}

	private StudyDetails convertToStudyDetails(final UserSelection userSelection) {
		final StudyDetails studyDetails = new StudyDetails();

		studyDetails.setStudyName(userSelection.getStudyName());
		if (userSelection.getStudyEndDate() != null && !userSelection.getStudyEndDate().isEmpty()) {
			studyDetails.setEndDate(ETLServiceImpl.formatDate(userSelection.getStudyEndDate()));
		}

		studyDetails.setObjective(userSelection.getStudyObjective());
		if (userSelection.getStudyType() != null && !StringUtils.isEmpty(userSelection.getStudyType())) {
			studyDetails.setStudyType(this.studyDataManager.getStudyTypeByName(userSelection.getStudyType()));
		} else {
			studyDetails.setStudyType(StudyTypeDto.getNurseryDto());
		}
		studyDetails.setDescription(userSelection.getStudyDescription());
		studyDetails.setStartDate(ETLServiceImpl.formatDate(userSelection.getStudyStartDate()));

		if (userSelection.getStudyUpdate() != null && !userSelection.getStudyUpdate().isEmpty()) {
			studyDetails.setStudyUpdate(ETLServiceImpl.formatDate(userSelection.getStudyUpdate()));
		}

		studyDetails.setObjective(userSelection.getStudyObjective());

		if (userSelection.getCreatedBy() != null) {
			studyDetails.setCreatedBy(userSelection.getCreatedBy());
		} else {
			studyDetails.setCreatedBy(String.valueOf(this.contextUtil.getCurrentWorkbenchUserId()));
		}

		if (userSelection.getStudyId() != null) {
			studyDetails.setId(userSelection.getStudyId());
		}

		// Save the imported Study at system root folder by default.
		studyDetails.setParentFolderId(DmsProject.SYSTEM_FOLDER_ID);

		return studyDetails;
	}

	@Override
	public Workbook retrieveCurrentWorkbook(final UserSelection userSelection) throws IOException {
		return this.getFileService().retrieveWorkbook(userSelection.getServerFileName());
	}

	@Override
	public Workbook retrieveCurrentWorkbookWithValidation(final UserSelection userSelection)
		throws IOException, WorkbookParserException {
		return this.getFileService().retrieveWorkbookWithValidation(userSelection.getServerFileName());
	}

	@Override
	public File retrieveCurrentWorkbookAsFile(final UserSelection userSelection) throws IOException {
		return this.getFileService().retrieveWorkbookFile(userSelection.getServerFileName());
	}

	@Override
	public List<SheetDTO> retrieveSheetInformation(final Workbook workbook) {
		final int sheetCount = workbook.getNumberOfSheets();

		final List<SheetDTO> returnVal = new ArrayList<>();

		for (int i = 0; i < sheetCount; i++) {
			final Sheet sheet = workbook.getSheetAt(i);
			final SheetDTO dto = new SheetDTO(i, sheet.getSheetName());

			returnVal.add(dto);
		}

		return returnVal;
	}

	@Override
	public List<RowDTO> retrieveRowInformation(
		final Workbook workbook, final int sheetIndex, final int startRow,
		final int endRow, final int maxRowContentLength) {
		final Sheet sheet = workbook.getSheetAt(sheetIndex);
		final List<RowDTO> displayRows = new ArrayList<>(endRow);
		for (int rowIndex = startRow; rowIndex <= endRow; rowIndex++) {
			final String row = PoiUtil.rowAsString(sheet, rowIndex, ",", maxRowContentLength);
			final RowDTO dto = new RowDTO(rowIndex, row);

			displayRows.add(dto);
		}

		return displayRows;
	}

	@Override
	public List<IndexValueDTO> retrieveColumnInformation(
		final Workbook workbook, final int sheetIndex,
		final int rowIndex) {
		final Sheet sheet = workbook.getSheetAt(sheetIndex);

		final String[] columnValues = PoiUtil.rowAsStringArray(sheet, rowIndex);

		final List<IndexValueDTO> dtoList = new ArrayList<>(columnValues.length);
		for (int i = 0; i < columnValues.length; i++) {
			final IndexValueDTO dto = new IndexValueDTO(i, columnValues[i].trim());
			dtoList.add(dto);
		}

		return dtoList;
	}

	@Override
	public int calculateObservationRows(
		final Workbook workbook, final int sheetIndex, final int contentRowIndex,
		final int indexColumnIndex) {
		final Sheet sheet = workbook.getSheetAt(sheetIndex);

		final int lastRow = sheet.getLastRowNum();
		int count = 0;

		for (int index = contentRowIndex; index <= lastRow; index++) {

			if (PoiUtil.isEmpty(sheet, index, indexColumnIndex)) {
				break;
			}

			count++;
		}

		return count;
	}

	@Override
	public List<String> retrieveColumnHeaders(
		final Workbook workbook, final UserSelection userSelection,
		final Boolean addObsUnitId) {
		final Sheet sheet = workbook.getSheetAt(userSelection.getSelectedSheet());
		final String[] headerArray = PoiUtil.rowAsStringArray(sheet, userSelection.getHeaderRowIndex());

		final List<String> headers = new ArrayList<>();
		headers.addAll(Arrays.asList(headerArray));
		if (addObsUnitId && !headers.contains(TermId.OBS_UNIT_ID.name())) {
			headers.add(TermId.OBS_UNIT_ID.name());
		}
		// Trim all header names before returning
		return Lists.transform(headers, new Function<String, String>() {

			public String apply(final String s) {
				return s.trim();
			}
		});
	}

	// overloaded the method to have a version that accepts parameterized
	// selected sheet index, for cases where user has not yet
	// 'permanently' selected a sheet
	@Override
	public int getAvailableRowsForDisplay(final Workbook workbook, final int selectedSheetIndex) {

		final Sheet sheet = workbook.getSheetAt(selectedSheetIndex);
		return PoiUtil.getLastRowNum(sheet);
	}

	@Override
	public int getAvailableRowsForDisplay(final Workbook workbook, final UserSelection userSelection) {

		if (userSelection.getLastSheetRowNum() == null) {
			final Sheet sheet = workbook.getSheetAt(userSelection.getSelectedSheet());
			userSelection.setLastSheetRowNum(PoiUtil.getLastRowNum(sheet));
		}

		return userSelection.getLastSheetRowNum();
	}

	protected String getPhenotypicTypeString(final PhenotypicType type) {
		if (type == PhenotypicType.TRIAL_ENVIRONMENT) {
			return AppConstants.TYPE_TRIAL_ENVIRONMENT;
		} else if (type == PhenotypicType.GERMPLASM) {
			return AppConstants.TYPE_GERMPLASM_ENTRY;
		} else if (type == PhenotypicType.TRIAL_DESIGN) {
			return AppConstants.TYPE_TRIAL_DESIGN;
		} else if (type == PhenotypicType.VARIATE) {
			return AppConstants.TYPE_VARIATE;
		} else {
			return null;
		}
	}

	// optimize prep step for new implem
	@Override
	public Map<PhenotypicType, List<VariableDTO>> prepareInitialCategorization(
		final List<String> headers,
		final UserSelection selection) {
		final List<String> headerList = new ArrayList<>(headers);

		final Map<PhenotypicType, List<VariableDTO>> returnVal = new HashMap<>();
		// initialize return variable to ensure non null lists
		returnVal.put(null, new ArrayList<VariableDTO>());
		returnVal.put(PhenotypicType.TRIAL_ENVIRONMENT, new ArrayList<>());
		returnVal.put(PhenotypicType.TRIAL_DESIGN, new ArrayList<>());
		returnVal.put(PhenotypicType.GERMPLASM, new ArrayList<>());
		returnVal.put(PhenotypicType.VARIATE, new ArrayList<>());
		returnVal.put(PhenotypicType.ENTRY_DETAIL, new ArrayList<>());

		try {
			final Map<String, List<StandardVariable>> variables = this.ontologyDataManager
				.getStandardVariablesInProjects(headerList, this.contextUtil.getCurrentProgramUUID());

			if (variables != null) {

				// GCP-6310
				for (final String header : headers) {
					final List<StandardVariable> variableList = variables.get(header.toUpperCase());
					if (variableList != null && !variableList.isEmpty()) {
						final StandardVariable var = variableList.get(0);
						final PhenotypicType type = var.getPhenotypicType();
						List<String> categoryHeaders = selection.getHeadersForCategory(type);
						final List<VariableDTO> variableDTOList = returnVal.get(type);

						if (categoryHeaders == null) {
							categoryHeaders = new LinkedList<>();
							selection.setHeadersForCategory(categoryHeaders, type);
						}

						categoryHeaders.add(header);

						this.populateVariableDtoList(returnVal, header, var, variableDTOList);

					} else {
						List<VariableDTO> unmatched = returnVal.get(null);
						if (unmatched == null) {
							unmatched = new ArrayList<>();
							returnVal.put(null, unmatched);
						}

						final VariableDTO dto = new VariableDTO();
						dto.setHeaderName(header);

						unmatched.add(dto);
					}
				}
			}

			return returnVal;
		} catch (final MiddlewareException e) {
			ETLServiceImpl.LOG.error(e.getMessage(), e);
			return null;
		}
	}

	protected void populateVariableDtoList(
		final Map<PhenotypicType, List<VariableDTO>> returnVal, final String header,
		final StandardVariable var, final List<VariableDTO> variableDTOList) {
		try {
			final VariableDTO dto = new VariableDTO(var);
			dto.setHeaderName(header);

			// it's possible that the Standard Variable is
			// mapped to STUDY or DATASET
			if (variableDTOList == null) {
				// add to unmatched
				returnVal.get(null).add(dto);
			} else {
				variableDTOList.add(dto);
			}
		} catch (final Exception e) {
			ETLServiceImpl.LOG.error(e.getMessage(), e);
			List<VariableDTO> unmatched = returnVal.get(null);
			if (unmatched == null) {
				unmatched = new ArrayList<>();
				returnVal.put(null, unmatched);
			}

			final VariableDTO dto = new VariableDTO();
			dto.setHeaderName(header);

			unmatched.add(dto);
		}
	}

	@Override
	public VariableDTO retrieveStandardVariableByID(final int id) {
		try {
			final StandardVariable standardVariable = this.ontologyDataManager.getStandardVariable(
				id,
				this.contextUtil.getCurrentProgramUUID());
			if (standardVariable != null) {
				return new VariableDTO(standardVariable);
			}
		} catch (final Exception e) {
			ETLServiceImpl.LOG.error(e.getMessage(), e);
		}
		return new VariableDTO();
	}

	@Override
	public void mergeVariableData(
		final VariableDTO[] variables, final UserSelection userSelection, final boolean maintainHeaderMapping) {

		for (final VariableDTO dto : variables) {

			if (dto.getId() == null) {
				continue;
			}

			final MeasurementVariable variable = new MeasurementVariable();
			dto.populateMeasurementVariable(variable);
			if(!maintainHeaderMapping) {
				variable.setAlias(dto.getHeaderName());
				final String name = !StringUtils.isEmpty(dto.getAlias()) ? dto.getAlias() : dto.getVariable();
				variable.setName(name);
			}

			final PhenotypicType type = variable.getRole();
			try {
				final Term dataTypeTerm = this.ontologyDataManager.getTermById(dto.getDataType());
				if (dataTypeTerm != null) {
					variable.setDataType(dataTypeTerm.getName());
					variable.setDataTypeId(dto.getDataType());
				}
			} catch (final MiddlewareQueryException e) {
				ETLServiceImpl.LOG.error(e.getMessage(), e);
			}

			Map<String, MeasurementVariable> measurementVariableMap = userSelection
				.getMeasurementVariablesByPhenotypic(type);

			if (measurementVariableMap == null) {
				// order should be preserved
				measurementVariableMap = new LinkedHashMap<>();
				userSelection.setMeasurementVariablesByPhenotypic(
					type,
					(LinkedHashMap<String, MeasurementVariable>) measurementVariableMap);
			}

			measurementVariableMap.put(dto.getHeaderName(), variable);
		}

	}

	@Override
	public String getExperimentalDesignValueFromObservationSheet(final Workbook workbook, final UserSelection userSelection,
		final int columnIndex) {
		if(columnIndex != -1) {
			final Sheet sheet = workbook.getSheetAt(userSelection.getSelectedSheet());
			final String data = PoiUtil.getCellStringValue(PoiUtil.getCell(sheet, columnIndex, 1));
			return data;
		}
		return null;
	}


	/*
	 * Method for extracting observation data from the provided Excel file in
	 * the case where existing study data is used
	 */
	@Override
	public List<MeasurementRow> extractExcelFileData(
		final Workbook workbook, final UserSelection userSelection,
		final org.generationcp.middleware.domain.etl.Workbook importData, final boolean discardInvalidValues) {
		final List<MeasurementVariable> variableList = importData.getAllVariables();

		final List<String> columnHeaders = this.retrieveColumnHeaders(workbook, userSelection,
			this.headersContainsObsUnitId(importData));
		// DMV : a linkedhashmap is used to preserve insert order
		final Map<Integer, MeasurementVariable> variableIndexMap = new LinkedHashMap<>();

		for (final MeasurementVariable measurementVariable : variableList) {
			final int columnIndex = columnHeaders.indexOf(measurementVariable.getName()) == -1 ?
				columnHeaders.indexOf(measurementVariable.getAlias()) : columnHeaders.indexOf(measurementVariable.getName());
			if (columnIndex != -1) {
				variableIndexMap.put(columnIndex, measurementVariable);
			}
		}

		return this.extractExcelFileData(workbook, userSelection, variableIndexMap, discardInvalidValues);
	}

	protected List<MeasurementRow> extractExcelFileData(
		final Workbook workbook, final UserSelection userSelection,
		final Map<Integer, MeasurementVariable> variableIndexMap, final boolean discardInvalidValues) {
		final Sheet sheet = workbook.getSheetAt(userSelection.getSelectedSheet());

		final List<MeasurementRow> rows = new ArrayList<>(userSelection.getObservationRows());
		for (int i = userSelection.getContentRowIndex(); i <= userSelection.getContentRowIndex()
			+ userSelection.getObservationRows() - 1; i++) {
			final MeasurementRow row = new MeasurementRow();
			row.setDataList(this.convertRow(sheet, i, variableIndexMap, discardInvalidValues));
			rows.add(row);
		}

		return rows;
	}

	protected List<MeasurementData> convertRow(
		final Sheet sheet, final int dataRowIndex,
		final Map<Integer, MeasurementVariable> variableIndexMap, final boolean discardInvalidValues) {
		final List<MeasurementData> dataList = new ArrayList<>(variableIndexMap.size());
		for (final Map.Entry<Integer, MeasurementVariable> entry : variableIndexMap.entrySet()) {
			final Integer columnIndex = entry.getKey();
			final MeasurementVariable variable = entry.getValue();
			final String data = PoiUtil.getCellStringValue(PoiUtil.getCell(sheet, columnIndex, dataRowIndex));
			final MeasurementData measurementData = new MeasurementData(variable.getName(), data);
			measurementData.setMeasurementVariable(variable);

			if (discardInvalidValues && !measurementData.isCategoricalValueValid()
				&& variable.getRole() == PhenotypicType.VARIATE) {
				measurementData.setValue("");
			}

			if (variable.getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId() && variable.getRole() == PhenotypicType.ENTRY_DETAIL && measurementData.getValue() != null) {
				final Map<String, Integer> availableScales = this.retrieveAvailableCategoricalScales(variable.getTermId(),
					this.contextUtil.getCurrentProgramUUID());
				this.convertCategoricalNameToCategoricalID(measurementData, availableScales);
			}

			dataList.add(measurementData);
		}

		return dataList;
	}

	public void convertCategoricalNameToCategoricalID(final MeasurementData measurementData, final Map<String, Integer> availableScales) {
		final String value = measurementData.getValue();
		final Integer scaleId = availableScales.get(value);
		if (scaleId == null) {
			throw new FieldbookRequestValidationException("error.invalid.categorical.value",
				new String[] {String.join(", ", availableScales.keySet())});
		}
		measurementData.setValue(scaleId.toString());
	}

	public FileService getFileService() {
		return this.fileService;
	}

	public void setFileService(final FileService fileService) {
		this.fileService = fileService;
	}

	public static String formatDate(final String date) {
		return ETLServiceImpl.formatDate(date, "MM/dd/yyyy", "yyyyMMdd");
	}

	public static String formatDate(final String date, final String oldFormat, final String newFormat) {
		if (date == null || oldFormat == null || newFormat == null) {
			return "";
		}
		if ("".equals(date) || "".equals(oldFormat) || "".equals(newFormat)) {
			return "";
		}
		try {
			if (date.contains("/") || !date.contains("-")) {
				return DateUtil.convertDate(date, oldFormat, newFormat);
			} else if (date.contains("-")) {
				return DateUtil.convertDate(date, DateUtil.FRONTEND_DATE_FORMAT, newFormat);
			} else {
				return date;
			}
		} catch (final Exception e) {
			ETLServiceImpl.LOG.error(e.getMessage(), e);
		}
		return "";
	}

	@Override
	public List<String> convertMessageList(final List<Message> messages) {
		final Set<String> stringMessages = new LinkedHashSet<>(messages.size());
		for (final Message message : messages) {
			try {
				stringMessages.add(this.convertMessage(message));
			} catch (final NoSuchMessageException e) {
				ETLServiceImpl.LOG.error(e.getMessage(), e);
				stringMessages.add(message.getMessageKey());
			}
		}
		return new ArrayList<>(stringMessages);
	}

	@Override
	public String convertMessage(final Message message) {
		try {
			return this.messageSource.getMessage(message.getMessageKey(), message.getMessageParams(), null);
		} catch (final NoSuchMessageException e) {
			ETLServiceImpl.LOG.error(e.getMessage(), e);
			return message.getMessageKey();
		}
	}

	@Override
	public List<StudyDetails> retrieveExistingStudyDetails(final String programUUID) {
		final List<StudyDetails> returnVal = new LinkedList<>();

		for (final StudyTypeDto studyType : this.studyDataManager.getAllStudyTypes()) {
			try {
				returnVal.addAll(this.studyDataManager.getAllStudyDetails(studyType, programUUID));
			} catch (final MiddlewareQueryException e) {
				ETLServiceImpl.LOG.error(e.getMessage(), e);
			}
		}

		return returnVal;
	}


	@Override
	public Map<String, List<Message>> validateProjectOntology(
		final org.generationcp.middleware.domain.etl.Workbook importData) {
		try {
			return this.dataImportService.validateProjectOntology(importData, this.contextUtil.getCurrentProgramUUID());
		} catch (final MiddlewareException e) {
			ETLServiceImpl.LOG.error(e.getMessage(), e);
			final Map<String, List<Message>> genericError = new HashMap<>();
			final List<Message> error = new ArrayList<>();
			error.add(new Message(Constants.MESSAGE_KEY_GENERIC_ERROR));
			genericError.put(Constants.GLOBAL, error);
			return genericError;
		}
	}

	@Override
	public int saveProjectOntology(
		final org.generationcp.middleware.domain.etl.Workbook importData,
		final String programUUID) {
		return this.dataImportService.saveProjectOntology(importData, programUUID,
			this.contextUtil.getProjectInContext().getCropType());
	}

	@Override
	public Map<String, List<Message>> validateProjectData(
		final org.generationcp.middleware.domain.etl.Workbook importData, final String programUUID) {
		try {
			return this.dataImportService.validateProjectData(importData, programUUID);
		} catch (final MiddlewareException e) {
			ETLServiceImpl.LOG.error(e.getMessage(), e);
			final Map<String, List<Message>> genericError = new HashMap<>();
			final List<Message> error = new ArrayList<>();
			error.add(new Message(Constants.MESSAGE_KEY_GENERIC_ERROR));
			genericError.put(Constants.GLOBAL, error);
			return genericError;
		}

	}

	@Override
	public int saveProjectData(
		final org.generationcp.middleware.domain.etl.Workbook importData,
		final String programUUID) {
		return this.dataImportService.saveProjectData(importData, programUUID,
			this.contextUtil.getProjectInContext().getCropType());
	}

	@Override
	public org.generationcp.middleware.domain.etl.Workbook retrieveAndSetProjectOntology(
		final UserSelection userSelection, final boolean isMeansDataImport) {

		final org.generationcp.middleware.domain.etl.Workbook wb = new org.generationcp.middleware.domain.etl.Workbook();

		wb.setStudyDetails(this.convertToStudyDetails(userSelection));
		wb.setImportType(userSelection.getDatasetType());

		this.fillDetailsOfDatasetsInWorkbook(wb, userSelection.getStudyId(), isMeansDataImport);

		return wb;
	}

	/***
	 * This sets the study id, and id of datasets of the workbook. This also set
	 * the conditions, constants, factors and variates of the workbook based on
	 * what is saved in the trial dataset and the dataset for import (plot data
	 * or means data)
	 *
	 * @param wb
	 *            as the Workbook
	 * @param studyId
	 *            as the id of the study
	 */
	public void fillDetailsOfDatasetsInWorkbook(
		final org.generationcp.middleware.domain.etl.Workbook wb,
		final Integer studyId, final boolean isMeansDataImport) {

		wb.getStudyDetails().setId(studyId);

		final DataSet trialDataset = this.getTrialDataset(studyId);
		final int trialDatasetId = trialDataset.getId();
		wb.setTrialDatasetId(trialDatasetId);

		final DataSet datasetForImport;

		if (isMeansDataImport) {
			datasetForImport = this.getMeansDataset(studyId);
			wb.setMeansDatasetId(datasetForImport.getId());
		} else {
			datasetForImport = this.getPlotDataset(studyId);
			wb.setMeasurementDatesetId(datasetForImport.getId());
		}

		// set variables
		wb.setFactors(this.getFactorsFromDatasets(trialDataset, datasetForImport));
		wb.setVariates(this.getVariatesFromDatasets(trialDataset, datasetForImport, isMeansDataImport));
		wb.setConditions(new ArrayList<MeasurementVariable>());
		wb.setConstants(new ArrayList<MeasurementVariable>());
	}

	private List<MeasurementVariable> getFactorsFromDatasets(final DataSet trialDataset, final DataSet nonTrialDataset) {
		final List<MeasurementVariable> factors = new ArrayList<>();

		for (final DMSVariableType variableType : trialDataset.getVariableTypes().getVariableTypes()) {
			final PhenotypicType pheno = variableType.getStandardVariable().getPhenotypicType();
			if (PhenotypicType.TRIAL_ENVIRONMENT.compareTo(pheno) == 0) {
				factors.add(this.convertToMeasurementVariable(variableType));
			}
		}

		for (final DMSVariableType variableType : nonTrialDataset.getVariableTypes().getVariableTypes()) {
			final PhenotypicType pheno = variableType.getStandardVariable().getPhenotypicType();
			if (PhenotypicType.GERMPLASM.compareTo(pheno) == 0 || pheno.compareTo(PhenotypicType.TRIAL_DESIGN) == 0) {
				factors.add(this.convertToMeasurementVariable(variableType));
			}
		}
		return factors;
	}

	private List<MeasurementVariable> getVariatesFromDatasets(
		final DataSet trialDataset,
		final DataSet nonTrialDataset,
		final boolean isImportDataSet) {
		final List<MeasurementVariable> variates = new ArrayList<>();
		final List<DMSVariableType> variables = new ArrayList<>();
		if (isImportDataSet) {
			// Include means variable
			variables.addAll(trialDataset.getVariableTypes().getVariableTypes());
		}
		variables.addAll(nonTrialDataset.getVariableTypes().getVariableTypes());
		for (final DMSVariableType variableType : variables) {
			final PhenotypicType pheno = variableType.getStandardVariable().getPhenotypicType();
			if (PhenotypicType.VARIATE.compareTo(pheno) == 0) {
				variates.add(this.convertToMeasurementVariable(variableType));
			}
		}
		return variates;
	}

	private DataSet getPlotDataset(final Integer studyId) {
		final DataSet plotDataSet = this.studyDataManager.findOneDataSetByType(studyId, DatasetTypeEnum.PLOT_DATA.getId());
		if (plotDataSet == null) {
			throw new MiddlewareQueryException("Missing plot dataset");
		}
		return plotDataSet;
	}

	private DataSet getTrialDataset(final Integer studyId) {
		final DataSet trialDataSet = this.studyDataManager.findOneDataSetByType(studyId, DatasetTypeEnum.SUMMARY_DATA.getId());
		if (trialDataSet == null) {
			throw new MiddlewareQueryException("Missing trial dataset");
		}
		return trialDataSet;
	}

	private DataSet getMeansDataset(final Integer studyId) {
		final DataSet meansDataSet = this.studyDataManager.findOneDataSetByType(studyId, DatasetTypeEnum.MEANS_DATA.getId());
		if (meansDataSet == null) {
			throw new MiddlewareQueryException("Missing means dataset");
		}
		return meansDataSet;
	}

	private MeasurementVariable convertToMeasurementVariable(final DMSVariableType variableType) {
		final MeasurementVariable mv = new MeasurementVariable();
		mv.setTermId(variableType.getId());
		mv.setName(variableType.getLocalName());
		mv.setDescription(variableType.getLocalDescription());
		mv.setLabel(variableType.getStandardVariable().getPhenotypicType().getLabelList().get(0));
		mv.setRole(variableType.getRole());
		mv.setProperty(variableType.getStandardVariable().getProperty().getName());
		mv.setScale(variableType.getStandardVariable().getScale().getName());
		mv.setMethod(variableType.getStandardVariable().getMethod().getName());
		mv.setDataType(variableType.getStandardVariable().getDataType().getName());
		mv.setDataTypeId(variableType.getStandardVariable().getDataType().getId());
		return mv;
	}

	@Override
	public Map<String, List<Message>> checkForMismatchedHeaders(
		final List<String> fileHeaders,
		final List<MeasurementVariable> studyHeaders, final boolean isMeansDataImport) {
		final Map<String, List<Message>> errors = new LinkedHashMap<>();

		final Map<String, String> fileHeaderMap = new HashMap<>();
		final String delimeter = ", ";

		// construct map of file headers to simplify retrieval / checking of
		// header names later
		for (final String fileHeader : fileHeaders) {
			fileHeaderMap.put(fileHeader.toUpperCase(), fileHeader);
		}

		final StringBuilder missingHeaders = new StringBuilder();
		for (final MeasurementVariable studyHeader : studyHeaders) {

			if (isMeansDataImport && studyHeader.getRole() == PhenotypicType.TRIAL_DESIGN) {
				continue;
			}

			/* Check if the observation headers from imported file match the existing variables in the study.
			Trial Environment variables will be ignored as they are not required in the observation
			sheet of the workbook. */
			// TODO: Verify if OCC variable is obsolete.
			if (!"OCC".equalsIgnoreCase(studyHeader.getName())
				&& studyHeader.getRole() != PhenotypicType.TRIAL_ENVIRONMENT
				&& fileHeaderMap.get(studyHeader.getName().toUpperCase()) == null) {
				if((studyHeader.getAlias() != null && fileHeaderMap.get(studyHeader.getAlias().toUpperCase()) != null)) {
					continue;
				}
				// IBP-4255: Allow user to upload file w/o ENTRY_TYPE
				else if (studyHeader.getTermId() == TermId.ENTRY_TYPE.getId()) {
					continue;
				}
				missingHeaders.append(studyHeader.getName());
				missingHeaders.append(delimeter);
			}
		}

		String missingHeaderString = missingHeaders.toString();
		if (missingHeaderString.length() > 0) {
			missingHeaderString = missingHeaderString.substring(0, missingHeaderString.lastIndexOf(delimeter));
			final List<Message> errorMessages = new ArrayList<>();

			errorMessages.add(new Message("error.missing.headers", missingHeaderString));
			errors.put(Constants.GLOBAL, errorMessages);
		}

		return errors;
	}

	@Override
	public Tool getFieldbookWebTool() {
		try {
			return this.workbenchDataManager.getToolWithName(ToolName.FIELDBOOK_WEB.toString());
		} catch (final MiddlewareQueryException e) {
			ETLServiceImpl.LOG.error(e.getMessage(), e);
		}

		return null;
	}

	@Override
	public int getIndexColumnIndex(final List<String> fileHeaders, final List<MeasurementVariable> studyHeaders) {
		final Map<String, Integer> fileHeaderMap = new HashMap<>();
		for (int i = 0; i < fileHeaders.size(); i++) {
			fileHeaderMap.put(fileHeaders.get(i).toUpperCase(), i);
		}
		for (final MeasurementVariable studyHeader : studyHeaders) {
			final int id = studyHeader.getTermId();
			if (id == TermId.TRIAL_INSTANCE_FACTOR.getId() || id == TermId.ENTRY_NO.getId()
				|| id == TermId.PLOT_NO.getId() || id == TermId.PLOT_NNO.getId()) {
				final Integer index = fileHeaderMap.get(studyHeader.getName().toUpperCase());
				if (index != null) {
					return index;
				}
			}
		}
		return 0;
	}

	@Override
	public StudyDetails readStudyDetails(final Sheet sheet) {
		// get study details
		final String study = this.getCellStringValue(sheet, ETLServiceImpl.STUDY_NAME_ROW_INDEX,
			ETLServiceImpl.STUDY_DETAILS_VALUE_COLUMN_INDEX);
		final String title = this.getCellStringValue(sheet, ETLServiceImpl.STUDY_TITLE_ROW_INDEX,
			ETLServiceImpl.STUDY_DETAILS_VALUE_COLUMN_INDEX);
		final String pmKeyLabel = this.getCellStringValue(sheet, ETLServiceImpl.PMKEY_ROW_INDEX,
			ETLServiceImpl.STUDY_DETAILS_LABEL_COLUMN_INDEX);
		int rowAdjustMent = 0;
		if (pmKeyLabel != null && !pmKeyLabel.trim().equals(ETLServiceImpl.PMKEY_LABEL)) {
			rowAdjustMent++;
		}
		final String objective = this.getCellStringValue(sheet, ETLServiceImpl.OBJECTIVE_ROW_INDEX - rowAdjustMent,
			ETLServiceImpl.STUDY_DETAILS_VALUE_COLUMN_INDEX);
		String startDateStr = this.getCellStringValue(sheet, ETLServiceImpl.START_DATE_ROW_INDEX - rowAdjustMent,
			ETLServiceImpl.STUDY_DETAILS_VALUE_COLUMN_INDEX);
		final String endDateStr = this.getCellStringValue(sheet, ETLServiceImpl.END_DATE_ROW_INDEX - rowAdjustMent,
			ETLServiceImpl.STUDY_DETAILS_VALUE_COLUMN_INDEX);
		final String studyType = this.getCellStringValue(sheet, ETLServiceImpl.STUDY_TYPE_ROW_INDEX - rowAdjustMent,
			ETLServiceImpl.STUDY_DETAILS_VALUE_COLUMN_INDEX);
		final StudyTypeDto studyTypeValue = this.studyDataManager.getStudyTypeByName((StudyTypeDto.TRIAL_LABEL.equals(studyType) ? StudyTypeDto.TRIAL_NAME : StudyTypeDto.NURSERY_NAME));
		if (startDateStr == null || startDateStr.isEmpty()) {
			final SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
			startDateStr = df.format(new Date());
		}
		if (startDateStr == null || startDateStr.isEmpty()) {
			final SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
			startDateStr = df.format(new Date());
		}

		// Always assumes that study is not locked
		return new StudyDetails(study, title, objective, startDateStr, endDateStr, studyTypeValue, 0, null,
			null, Util.getCurrentDateAsStringValue(), null, false);
	}

	private String getCellStringValue(final Sheet sheet, final Integer rowNumber, final Integer columnNumber) {
		try {
			final Row row = sheet.getRow(rowNumber);
			final Cell cell = row.getCell(columnNumber);
			return PoiUtil.getCellStringValue(cell);
		} catch (final IllegalStateException | NullPointerException e) {
			ETLServiceImpl.LOG.error(e.getMessage(), e);
			return "";
		}
	}

	@Override
	public boolean hasMeansDataset(final int studyId) {
		final DatasetReference meansDatasetReference = this.studyDataManager.findOneDataSetReferenceByType(studyId, DatasetTypeEnum.MEANS_DATA.getId());
		return meansDatasetReference != null;
	}

	@Override
	public boolean hasMeasurementEffectDataset(final int studyId) {
		final DatasetReference plotDatasetReference = this.studyDataManager.findOneDataSetReferenceByType(studyId, DatasetTypeEnum.PLOT_DATA.getId());
		return plotDatasetReference != null;
	}

	@Override
	public boolean checkOutOfBoundsData(final UserSelection userSelection) throws IOException {

		final org.generationcp.middleware.domain.etl.Workbook importData;

		final String programUUID = this.contextUtil.getCurrentProgramUUID();

		boolean hasOutOfBoundsData = false;

		final Workbook workbook = this.retrieveCurrentWorkbook(userSelection);

		final boolean isMeansDataImport = userSelection.getDatasetType() != null
			&& userSelection.getDatasetType().intValue() == DatasetTypeEnum.MEANS_DATA.getId();

		importData = this.createWorkbookFromUserSelection(userSelection, isMeansDataImport);

		final List<String> errors = new ArrayList<>();
		final boolean isWorkbookHasObservationRecords = this.isWorkbookHasObservationRecords(userSelection, errors,
			workbook);
		final boolean isObservationOverMaxLimit = this.isObservationOverMaximumLimit(userSelection, errors, workbook);

		if (isWorkbookHasObservationRecords && !isObservationOverMaxLimit) {

			importData.setObservations(this.extractExcelFileData(workbook, userSelection, importData, false));
			hasOutOfBoundsData = this.dataImportService.checkForOutOfBoundsData(importData, programUUID);
		}

		return hasOutOfBoundsData;

	}

	@Override
	public boolean isObservationOverMaximumLimit(
		final UserSelection userSelection, final List<String> errors,
		final Workbook workbook) {
		final Sheet sheet = workbook.getSheetAt(userSelection.getSelectedSheet());
		final Integer lastRowNum = PoiUtil.getLastRowNum(sheet);

		if (lastRowNum > this.maxRowLimit) {
			final List<Message> messages = new ArrayList<>();
			final Message message = new Message(
				"error.observation.over.maximum.limit",
				new DecimalFormat("###,###,###").format(this.maxRowLimit));
			messages.add(message);
			errors.addAll(this.convertMessageList(messages));

			return true;
		}
		return false;
	}

	@Override
	public boolean isWorkbookHasObservationRecords(
		final UserSelection userSelection, final List<String> errors,
		final Workbook workbook) {
		final Sheet sheet = workbook.getSheetAt(userSelection.getSelectedSheet());
		final Integer lastRowNum = PoiUtil.getLastRowNum(sheet);

		if (lastRowNum == 0) {
			final List<Message> messages = new ArrayList<>();
			final Message message = new Message("error.observation.no.records");
			messages.add(message);
			errors.addAll(this.convertMessageList(messages));

			return false;
		}

		return true;
	}

	@Override
	public org.generationcp.middleware.domain.etl.Workbook createWorkbookFromUserSelection(
		final UserSelection userSelection, final boolean isMeansDataImport) {

		final org.generationcp.middleware.domain.etl.Workbook importData;

		ETLServiceImpl.LOG.debug("userSelection.getPhenotypicMap() = " + userSelection.getPhenotypicMap());
		// check if headers are not set (it means the user skipped the import
		// project ontology)
		if (userSelection.getPhenotypicMap() == null || userSelection.getPhenotypicMap().isEmpty()) {
			// set variables and ids in workbook
			importData = this.retrieveAndSetProjectOntology(userSelection, isMeansDataImport);
		} else {
			// get workbook from user selection
			importData = this.convertToWorkbook(userSelection);
		}

		return importData;
	}

	public int getMaxRowLimit() {
		return this.maxRowLimit;
	}

	public void setMaxRowLimit(final int value) {
		if (value > 0) {
			this.maxRowLimit = value;
		}
	}

	@Override
	public boolean headersContainsObsUnitId(final org.generationcp.middleware.domain.etl.Workbook importData) {
		boolean hasObsUnitId = false;
		for (final MeasurementVariable mv : importData.getAllVariables()) {
			if (mv.getTermId() == TermId.OBS_UNIT_ID.getId()) {
				hasObsUnitId = true;
				break;
			}
		}
		return hasObsUnitId;
	}

	/**
	 * Returns all available entry types at the moment in the form of a map
	 * <Name, CVTermId> i.e <C,10170>
	 *
	 * @param programUUID
	 * @return map <Name, CVTermId>
	 */
	@Override
	public Map<String, Integer> retrieveAvailableEntryTypes(final String programUUID) {
		final Map<String, Integer> entryTypeMap = new HashMap<>();
		final List<Enumeration> entryTypes = this.ontologyService
			.getStandardVariable(TermId.ENTRY_TYPE.getId(), programUUID).getEnumerations();

		for (final Enumeration entryType : entryTypes) {
			entryTypeMap.put(entryType.getName(), entryType.getId());
		}

		return entryTypeMap;
	}
	@Override
	public Map<String, Integer> retrieveAvailableCategoricalScales(final Integer termId, final String programUUID) {
		final Map<String, Integer> scaleMap = new HashMap<>();
		final List<Enumeration> categoricalScales = this.ontologyService
			.getStandardVariable(termId, programUUID).getEnumerations();

		for (final Enumeration scale : categoricalScales) {
			scaleMap.put(scale.getName(), scale.getId());
		}

		return scaleMap;
	}
}
