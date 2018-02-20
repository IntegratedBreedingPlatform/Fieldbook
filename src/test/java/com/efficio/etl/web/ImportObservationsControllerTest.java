package com.efficio.etl.web;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.DataImportService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.Model;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.web.bean.FileUploadForm;
import com.efficio.etl.web.bean.UserSelection;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class ImportObservationsControllerTest {
	private static final String PROGRAM_UUID = "55bd5dde-3a68-4dcd-bdda-d2301eff9e16";
	private static final String PROJECT_CODE_PREFIX = "AAGhs";

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
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(project);
		Mockito.when(this.contextUtil.getCurrentProgramUUID())
				.thenReturn(ImportObservationsControllerTest.PROGRAM_UUID);
	}

	@Test
	public void testProcessImportWithNoErrors() throws IOException, WorkbookParserException {
		final Workbook workbook = Mockito.mock(Workbook.class);
		final org.generationcp.middleware.domain.etl.Workbook importData = Mockito
				.mock(org.generationcp.middleware.domain.etl.Workbook.class);
		Mockito.when(
				this.etlService.createWorkbookFromUserSelection(Matchers.eq(this.userSelection), Matchers.anyBoolean()))
				.thenReturn(importData);
		Mockito.when(this.etlService.retrieveCurrentWorkbook(this.userSelection)).thenReturn(workbook);
		Mockito.when(this.dataImportService.parseWorkbookDescriptionSheet(workbook)).thenReturn(importData);

		final String returnValue = this.importObservationsController.processImport(this.uploadForm, 1, this.model,
				this.session, this.request);
		Assert.assertEquals("redirect:/etl/fileUpload", returnValue);
		Mockito.verify(this.contextUtil).getCurrentProgramUUID();
		Mockito.verify(this.etlService).createWorkbookFromUserSelection(Matchers.eq(this.userSelection),
				Matchers.anyBoolean());
		Mockito.verify(this.dataImportService).parseWorkbookDescriptionSheet(workbook);
		Mockito.verify(this.etlService).saveProjectData(importData, ImportObservationsControllerTest.PROGRAM_UUID);
	}

	@Test
	public void testProcessImportWithErrors() throws IOException, WorkbookParserException {
		final Workbook workbook = Mockito.mock(Workbook.class);
		final org.generationcp.middleware.domain.etl.Workbook importData = Mockito
				.mock(org.generationcp.middleware.domain.etl.Workbook.class);
		Mockito.when(
				this.etlService.createWorkbookFromUserSelection(Matchers.eq(this.userSelection), Matchers.anyBoolean()))
				.thenReturn(importData);
		Mockito.when(this.etlService.retrieveCurrentWorkbook(this.userSelection)).thenReturn(workbook);
		Mockito.when(this.etlService.convertMessageList(Matchers.anyList())).thenReturn(Arrays.asList("error"));

		final String returnValue = this.importObservationsController.processImport(this.uploadForm, 1, this.model,
				this.session, this.request);
		Assert.assertEquals("etl/validateProjectData", returnValue);
		Mockito.verify(this.contextUtil).getCurrentProgramUUID();
		Mockito.verify(this.etlService).createWorkbookFromUserSelection(Matchers.eq(this.userSelection),
				Matchers.anyBoolean());
	}

	@Test
	public void testConfirmImport() throws WorkbookParserException, IOException {
		final org.generationcp.middleware.domain.etl.Workbook importData = Mockito
				.mock(org.generationcp.middleware.domain.etl.Workbook.class);
		final org.generationcp.middleware.domain.etl.Workbook referenceWorkbook = Mockito
				.mock(org.generationcp.middleware.domain.etl.Workbook.class);
		final Workbook workbook = Mockito.mock(Workbook.class);
		Mockito.when(this.dataImportService.parseWorkbookDescriptionSheet(workbook)).thenReturn(referenceWorkbook);
		Mockito.when(this.etlService.retrieveCurrentWorkbook(this.userSelection)).thenReturn(workbook);

		final String returnValue = this.importObservationsController.confirmImport(this.model, importData,
				ImportObservationsControllerTest.PROGRAM_UUID);

		Assert.assertEquals("redirect:/etl/fileUpload", returnValue);
		Mockito.verify(this.dataImportService).parseWorkbookDescriptionSheet(workbook);
		Mockito.verify(this.etlService).saveProjectData(importData, ImportObservationsControllerTest.PROGRAM_UUID);
	}
}
