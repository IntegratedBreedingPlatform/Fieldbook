package com.efficio.etl.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.util.Message;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.Model;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.web.bean.FileUploadForm;
import com.efficio.etl.web.bean.UserSelection;

@RunWith(MockitoJUnitRunner.class)
public class ImportObservationsControllerTest {

	private static final String PROGRAM_UUID = "55bd5dde-3a68-4dcd-bdda-d2301eff9e16";
	private static final String PROJECT_CODE_PREFIX = "AAGhs";
	private static final int CURRENT_IBDB_USER_ID = 1;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private ETLService etlService;

	@Mock
	private UserSelection userSelection;

	@Mock
	private DataImportService dataImportService;

	@InjectMocks
	ImportObservationsController importObservationsController;
	private HttpSession session;
	private HttpServletRequest request;
	private FileUploadForm uploadForm;
	private Model model;

	@Before
	public void setUp() {
		this.session = new MockHttpSession();
		this.request = new MockHttpServletRequest();
		this.uploadForm = Mockito.mock(FileUploadForm.class);
		this.model = Mockito.mock(Model.class);

		final Project project = new Project();
		project.setCropType(new CropType("Maize"));
		project.getCropType().setPlotCodePrefix(ImportObservationsControllerTest.PROJECT_CODE_PREFIX);
		project.setProjectId(Long.valueOf(123));
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(ImportObservationsControllerTest.PROGRAM_UUID);
		Mockito.when(this.contextUtil.getCurrentWorkbenchUserId()).thenReturn(CURRENT_IBDB_USER_ID);
	}

	@Test
	public void testProcessImportWithNoErrors() throws IOException, WorkbookParserException {
		final Workbook workbook = Mockito.mock(Workbook.class);
		final org.generationcp.middleware.domain.etl.Workbook importData =
			Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class);
		Mockito.when(
				this.etlService.createWorkbookFromUserSelection(ArgumentMatchers.eq(this.userSelection), ArgumentMatchers.anyBoolean()))
			.thenReturn(importData);
		Mockito.when(this.etlService.retrieveCurrentWorkbook(this.userSelection)).thenReturn(workbook);
		Mockito.when(this.dataImportService.parseWorkbookDescriptionSheet(workbook, CURRENT_IBDB_USER_ID)).thenReturn(importData);

		final String returnValue =
			this.importObservationsController.processImport(this.uploadForm, 1, this.model, this.session, this.request);
		Assert.assertEquals("redirect:/etl/fileUpload", returnValue);

		Mockito.verify(this.contextUtil).getCurrentProgramUUID();
		Mockito.verify(this.etlService)
			.createWorkbookFromUserSelection(ArgumentMatchers.eq(this.userSelection), ArgumentMatchers.anyBoolean());
		Mockito.verify(this.dataImportService).parseWorkbookDescriptionSheet(workbook, CURRENT_IBDB_USER_ID);
		Mockito.verify(this.dataImportService).assignLocationIdVariableToEnvironmentDetailSection(importData);
		Mockito.verify(this.etlService).saveProjectData(importData, ImportObservationsControllerTest.PROGRAM_UUID);
		Mockito.verify(this.dataImportService).removeLocationNameVariableIfExists(importData);

		final ArgumentCaptor<List<String>> errorsCaptor = ArgumentCaptor.forClass(ArrayList.class);
		Mockito.verify(this.model, Mockito.times(2)).addAttribute(ArgumentMatchers.eq("errors"), errorsCaptor.capture());
		final List<String> projectDataErrorsList = errorsCaptor.getValue();
		Assert.assertTrue(projectDataErrorsList.isEmpty());

		final ArgumentCaptor<Boolean> hasErrorsCaptor = ArgumentCaptor.forClass(Boolean.class);
		Mockito.verify(this.model, Mockito.times(2)).addAttribute(ArgumentMatchers.eq("hasErrors"), hasErrorsCaptor.capture());
		Assert.assertFalse(hasErrorsCaptor.getValue());
	}

	@Test
	public void testProcessImportWithInvalidGIDsError() throws IOException {
		final Workbook workbook = Mockito.mock(Workbook.class);
		final org.generationcp.middleware.domain.etl.Workbook importData =
			Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class);
		Mockito.when(
				this.etlService.createWorkbookFromUserSelection(ArgumentMatchers.eq(this.userSelection), ArgumentMatchers.anyBoolean()))
			.thenReturn(importData);
		Mockito.when(this.etlService.retrieveCurrentWorkbook(this.userSelection)).thenReturn(workbook);
		Mockito.when(this.etlService.convertMessageList(ArgumentMatchers.<List<Message>>any())).thenReturn(Arrays.asList("error"));

		final String returnValue =
			this.importObservationsController.processImport(this.uploadForm, 1, this.model, this.session, this.request);
		Assert.assertEquals("etl/validateProjectData", returnValue);
		Mockito.verify(this.contextUtil).getCurrentProgramUUID();
		Mockito.verify(this.etlService)
			.createWorkbookFromUserSelection(ArgumentMatchers.eq(this.userSelection), ArgumentMatchers.anyBoolean());
		Mockito.verify(this.dataImportService, Mockito.never()).removeLocationNameVariableIfExists(importData);
		Mockito.verify(this.dataImportService, Mockito.never()).assignLocationIdVariableToEnvironmentDetailSection(importData);
		Mockito.verify(this.dataImportService).checkForInvalidGids(ArgumentMatchers.eq(importData), ArgumentMatchers.<Message>anyList(),
			ArgumentMatchers.eq(ImportObservationsControllerTest.PROGRAM_UUID));
		Mockito.verify(this.etlService).convertMessageList(ArgumentMatchers.<List<Message>>any());
	}

	@Test
	public void testProcessImportWithProjectDataErrors() throws IOException {
		final Workbook workbook = Mockito.mock(Workbook.class);
		final org.generationcp.middleware.domain.etl.Workbook importData =
			Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class);
		Mockito.when(
				this.etlService.createWorkbookFromUserSelection(ArgumentMatchers.eq(this.userSelection), ArgumentMatchers.anyBoolean()))
			.thenReturn(importData);
		Mockito.when(this.etlService.retrieveCurrentWorkbook(this.userSelection)).thenReturn(workbook);

		final Map<String, List<Message>> projectDataErrors = new HashMap<>();
		final List<Message> projectDataError = Arrays.asList(new Message("Project Data Error"));
		projectDataErrors.put("ERRORS", projectDataError);
		final String errorMessage = "Project Data Error";
		Mockito.when(this.etlService.convertMessageList(projectDataError)).thenReturn(Arrays.asList(errorMessage));
		Mockito.when(this.etlService.validateProjectData(importData, PROGRAM_UUID)).thenReturn(projectDataErrors);

		Mockito.when(
			this.etlService.isWorkbookHasObservationRecords(ArgumentMatchers.eq(this.userSelection), ArgumentMatchers.<String>anyList(),
				ArgumentMatchers.eq(workbook))).thenReturn(true);
		Mockito.when(
			this.etlService.isObservationOverMaximumLimit(ArgumentMatchers.eq(this.userSelection), ArgumentMatchers.<String>anyList(),
				ArgumentMatchers.eq(workbook))).thenReturn(false);

		final String returnValue =
			this.importObservationsController.processImport(this.uploadForm, 1, this.model, this.session, this.request);

		final ArgumentCaptor<List<String>> errorsCaptor = ArgumentCaptor.forClass(ArrayList.class);
		Mockito.verify(this.model).addAttribute(ArgumentMatchers.eq("errors"), errorsCaptor.capture());
		final List<String> projectDataErrorsList = errorsCaptor.getValue();
		Assert.assertTrue(projectDataErrorsList.contains(errorMessage));

		final ArgumentCaptor<Boolean> hasErrorsCaptor = ArgumentCaptor.forClass(Boolean.class);
		Mockito.verify(this.model).addAttribute(ArgumentMatchers.eq("hasErrors"), hasErrorsCaptor.capture());
		Assert.assertTrue(hasErrorsCaptor.getValue());

		Assert.assertEquals("etl/validateProjectData", returnValue);
		Mockito.verify(this.contextUtil).getCurrentProgramUUID();
		Mockito.verify(this.etlService)
			.createWorkbookFromUserSelection(ArgumentMatchers.eq(this.userSelection), ArgumentMatchers.anyBoolean());
		Mockito.verify(this.dataImportService).removeLocationNameVariableIfExists(importData);
		Mockito.verify(this.dataImportService).assignLocationIdVariableToEnvironmentDetailSection(importData);
		Mockito.verify(this.dataImportService).checkForInvalidGids(ArgumentMatchers.eq(importData), ArgumentMatchers.<Message>anyList(),
			ArgumentMatchers.eq(ImportObservationsControllerTest.PROGRAM_UUID));
		Mockito.verify(this.etlService).extractExcelFileData(workbook, this.userSelection, importData, true);
		Mockito.verify(this.etlService, Mockito.times(2)).convertMessageList(ArgumentMatchers.<List<Message>>any());
	}

	@Test
	public void testProcessImportWithMisMatchErrors() throws IOException {
		final Workbook workbook = Mockito.mock(Workbook.class);
		final org.generationcp.middleware.domain.etl.Workbook importData =
			Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class);
		Mockito.when(
				this.etlService.createWorkbookFromUserSelection(ArgumentMatchers.eq(this.userSelection), ArgumentMatchers.anyBoolean()))
			.thenReturn(importData);
		Mockito.when(this.etlService.retrieveCurrentWorkbook(this.userSelection)).thenReturn(workbook);
		Mockito.when(this.etlService.convertMessageList(ArgumentMatchers.<List<Message>>any())).thenReturn(Arrays.asList("error"));
		Mockito.when(this.etlService.headersContainsObsUnitId(importData)).thenReturn(false);
		final List<String> headers = new ArrayList<>();
		Mockito.when(this.etlService.retrieveColumnHeaders(workbook, this.userSelection, false)).thenReturn(headers);

		final Map<String, List<Message>> mismatchedErrors = new HashMap<>();
		final String errorMessage = "Mismatched headers Error";
		final List<Message> mismatchedError = Arrays.asList(new Message(errorMessage));
		mismatchedErrors.put("ERRORS", mismatchedError);
		Mockito.when(this.etlService.convertMessageList(mismatchedError)).thenReturn(Arrays.asList(errorMessage));
		Mockito.when(
			this.etlService.checkForMismatchedHeaders(ArgumentMatchers.eq(headers), ArgumentMatchers.<MeasurementVariable>anyList(),
				ArgumentMatchers.eq(false))).thenReturn(mismatchedErrors);

		final String returnValue =
			this.importObservationsController.processImport(this.uploadForm, 1, this.model, this.session, this.request);

		final ArgumentCaptor<List<String>> errorsCaptor = ArgumentCaptor.forClass(ArrayList.class);
		Mockito.verify(this.model).addAttribute(ArgumentMatchers.eq("errors"), errorsCaptor.capture());
		final List<String> projectDataErrorsList = errorsCaptor.getValue();
		Assert.assertTrue(projectDataErrorsList.contains(errorMessage));

		final ArgumentCaptor<Boolean> hasErrorsCaptor = ArgumentCaptor.forClass(Boolean.class);
		Mockito.verify(this.model).addAttribute(ArgumentMatchers.eq("hasErrors"), hasErrorsCaptor.capture());
		Assert.assertTrue(hasErrorsCaptor.getValue());

		Assert.assertEquals("etl/validateProjectData", returnValue);
		Mockito.verify(this.contextUtil).getCurrentProgramUUID();
		Mockito.verify(this.etlService)
			.createWorkbookFromUserSelection(ArgumentMatchers.eq(this.userSelection), ArgumentMatchers.anyBoolean());
		Mockito.verify(this.dataImportService, Mockito.never()).removeLocationNameVariableIfExists(importData);
		Mockito.verify(this.dataImportService, Mockito.never()).assignLocationIdVariableToEnvironmentDetailSection(importData);
		Mockito.verify(this.dataImportService).checkForInvalidGids(ArgumentMatchers.eq(importData), ArgumentMatchers.<Message>anyList(),
			ArgumentMatchers.eq(ImportObservationsControllerTest.PROGRAM_UUID));
		Mockito.verify(this.etlService, Mockito.times(2)).convertMessageList(ArgumentMatchers.<List<Message>>any());
	}

	@Test
	public void testConfirmImport() throws WorkbookParserException, IOException {
		final org.generationcp.middleware.domain.etl.Workbook importData =
			Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class);
		final org.generationcp.middleware.domain.etl.Workbook referenceWorkbook =
			Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class);
		final Workbook workbook = Mockito.mock(Workbook.class);
		Mockito.when(this.dataImportService.parseWorkbookDescriptionSheet(workbook, CURRENT_IBDB_USER_ID)).thenReturn(referenceWorkbook);
		Mockito.when(this.etlService.retrieveCurrentWorkbook(this.userSelection)).thenReturn(workbook);

		final String returnValue =
			this.importObservationsController.confirmImport(this.model, importData, ImportObservationsControllerTest.PROGRAM_UUID);

		Assert.assertEquals("redirect:/etl/fileUpload", returnValue);
		Mockito.verify(this.dataImportService).addLocationIDVariableIfNotExists(importData, importData.getFactors(), PROGRAM_UUID);
		Mockito.verify(this.dataImportService).assignLocationIdVariableToEnvironmentDetailSection(importData);
		Mockito.verify(this.dataImportService).addEntryTypeVariableIfNotExists(importData, PROGRAM_UUID);
		Mockito.verify(this.dataImportService).removeLocationNameVariableIfExists(importData);
		Mockito.verify(this.dataImportService).parseWorkbookDescriptionSheet(workbook, CURRENT_IBDB_USER_ID);
		Mockito.verify(this.etlService).saveProjectData(importData, ImportObservationsControllerTest.PROGRAM_UUID);
	}
}
