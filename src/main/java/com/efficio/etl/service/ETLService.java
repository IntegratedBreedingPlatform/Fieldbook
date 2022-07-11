package com.efficio.etl.service;

import com.efficio.etl.web.bean.IndexValueDTO;
import com.efficio.etl.web.bean.RowDTO;
import com.efficio.etl.web.bean.SheetDTO;
import com.efficio.etl.web.bean.UserSelection;
import com.efficio.etl.web.bean.VariableDTO;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.util.Message;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public interface ETLService {

  /**
   * Takes in an input stream representing the Excel file to be read, and returns the temporary file name used to store it in the system
   *
   * @param in
   * @return
   */
  String storeUserWorkbook(InputStream in) throws IOException;

  /**
   * Retrieves
   *
   * @param userSelection
   * @return
   * @throws WorkbookParserException
   */
  Workbook retrieveCurrentWorkbook(UserSelection userSelection) throws IOException;

  File retrieveCurrentWorkbookAsFile(UserSelection userSelection) throws IOException;

  List<SheetDTO> retrieveSheetInformation(Workbook workbook);

  List<RowDTO> retrieveRowInformation(Workbook workbook, int sheetIndex, int startRow, int endRow, int maxRowContentLength);

  List<IndexValueDTO> retrieveColumnInformation(Workbook workbook, int sheetIndex, int rowIndex);

  int calculateObservationRows(Workbook workbook, int sheetIndex, int contentRowIndex, int indexColumnIndex);

  List<String> retrieveColumnHeaders(Workbook workbook, UserSelection userSelection, Boolean addObsUnitId);

  int getAvailableRowsForDisplay(Workbook workbook, int selectedSheetIndex);

  int getAvailableRowsForDisplay(Workbook workbook, UserSelection userSelection);

  org.generationcp.middleware.domain.etl.Workbook convertToWorkbook(UserSelection userSelection);

  Map<PhenotypicType, List<VariableDTO>> prepareInitialCategorization(List<String> headers, UserSelection selection);

  VariableDTO retrieveStandardVariableByID(int id);

  void mergeVariableData(VariableDTO[] variables, UserSelection userSelection, boolean maintainHeaderMapping);

  List<MeasurementRow> extractExcelFileData(Workbook workbook, UserSelection userSelection,
		  org.generationcp.middleware.domain.etl.Workbook importData, boolean discardInvalidValues);

  List<String> convertMessageList(List<Message> messages);

  String convertMessage(Message message);

  List<StudyDetails> retrieveExistingStudyDetails(String programUUID);

  Map<String, List<Message>> validateProjectOntology(org.generationcp.middleware.domain.etl.Workbook importData);

  int saveProjectOntology(org.generationcp.middleware.domain.etl.Workbook importData, String programUUID);

  Map<String, List<Message>> validateProjectData(org.generationcp.middleware.domain.etl.Workbook importData, String programUUID);

  int saveProjectData(org.generationcp.middleware.domain.etl.Workbook importData, String programUUID);

  org.generationcp.middleware.domain.etl.Workbook retrieveAndSetProjectOntology(UserSelection userSelection,
		  boolean isMeansDataImport);

  Map<String, List<Message>> checkForMismatchedHeaders(List<String> fileHeaders, List<MeasurementVariable> studyHeaders,
		  boolean isMeansDataImport);

  Tool getFieldbookWebTool();

  Workbook retrieveCurrentWorkbookWithValidation(UserSelection userSelection) throws IOException, WorkbookParserException;

  int getIndexColumnIndex(List<String> fileHeaders, List<MeasurementVariable> studyHeaders);

  StudyDetails readStudyDetails(Sheet sheet);

  boolean hasMeansDataset(int studyId);

  boolean hasMeasurementEffectDataset(int studyId);

  /**
   * Checks the uploaded workbook for out of bounds data. Returns true if there are out of bounds data.
   *
   * @param userSelection
   * @return
   * @throws IOException
   */
  boolean checkOutOfBoundsData(UserSelection userSelection) throws IOException;

  /**
   * Checks whether the workbook has observation records or not
   *
   * @param userSelection
   * @param errors
   * @param workbook
   * @return
   */
  boolean isWorkbookHasObservationRecords(UserSelection userSelection, List<String> errors, Workbook workbook);

  /**
   * Checks if the number of observations in the uploaded file is over the maximum limit
   *
   * @param userSelection
   * @param errors
   * @param workbook
   * @return
   */
  boolean isObservationOverMaximumLimit(UserSelection userSelection, List<String> errors, Workbook workbook);

  /**
   * Creates a Workbook from UserSelection
   *
   * @param userSelection
   * @param isMeansDataImport
   * @return
   */
  org.generationcp.middleware.domain.etl.Workbook createWorkbookFromUserSelection(UserSelection userSelection, boolean isMeansDataImport);

  /**
   * Verify if the file header contains OBS_UNIT_ID
   * @param importData
   * @return
   */
  boolean headersContainsObsUnitId(final org.generationcp.middleware.domain.etl.Workbook importData);

  /**
   * Returns all available entry types at the moment in the form of a map <Name, CVTermId> i.e <C,10170>
   * @param programUUID
   *
   * @return map <Name, CVTermId>
   **/
  Map<String, Integer> retrieveAvailableEntryTypes(String programUUID);

  /**
   * Returns the experimental design value in the observation sheet
   * @param workbook
   * @param userSelection
   * @param columnIndex
   * @return
   */
  String getExperimentalDesignValueFromObservationSheet(Workbook workbook, UserSelection userSelection, final int columnIndex);

  Map<String, Integer> retrieveAvailableCategoricalScales(Integer termId, String programUUID);
}
