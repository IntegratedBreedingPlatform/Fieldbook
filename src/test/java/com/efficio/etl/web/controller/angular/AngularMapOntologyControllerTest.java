package com.efficio.etl.web.controller.angular;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.web.bean.UserSelection;
import com.efficio.etl.web.bean.VariableDTO;
import com.efficio.fieldbook.service.api.FieldbookService;
import com.google.common.base.Optional;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.etl.Constants;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.exceptions.WorkbookParserException;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class AngularMapOntologyControllerTest {

	private static final String PROGRAM_UUID = "55bd5dde-3a68-4dcd-bdda-d2301eff9e16";
	private static final String CONTEXT_PATH = "contextPath";
	private static final int CURRENT_IBDB_USER_ID = 1;
	private static final int TRIAL_TYPE_ID = 6;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpSession session;

	@Mock
	private UserSelection userSelection;

	@Mock
	private FieldbookService fieldbookService;

	@Mock
	private ETLService etlService;

	@Mock
	private DataImportService dataImportService;

	@Mock
	private ContextUtil contextUtil;

	@InjectMocks
	private AngularMapOntologyController controller;

	@Before
	public void setup() {

		Mockito.when(this.contextUtil.getCurrentProgramUUID())
				.thenReturn(AngularMapOntologyControllerTest.PROGRAM_UUID);
		Mockito.when(this.contextUtil.getCurrentWorkbenchUserId()).thenReturn(CURRENT_IBDB_USER_ID);
		Mockito.when(this.request.getContextPath()).thenReturn(AngularMapOntologyControllerTest.CONTEXT_PATH);
	}

	@Test
	public void testProcessImporHeaderWithErrors() throws IOException, WorkbookParserException {
		final org.apache.poi.ss.usermodel.Workbook apacheWorkbook = Mockito
				.mock(org.apache.poi.ss.usermodel.Workbook.class);
		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(1, new StudyTypeDto(6,"","T"), "Sample Study", 1,
				false);

		Mockito.when(this.etlService.retrieveCurrentWorkbook(this.userSelection)).thenReturn(apacheWorkbook);
		Mockito.when(this.etlService.convertToWorkbook(this.userSelection)).thenReturn(workbook);
		Mockito.when(this.dataImportService.parseWorkbookDescriptionSheet(apacheWorkbook, CURRENT_IBDB_USER_ID)).thenReturn(workbook);
		Mockito.when(this.userSelection.getDatasetType()).thenReturn(DatasetTypeEnum.MEANS_DATA.getId());

		// Add Variable with no header mapping
		final VariableDTO variableWithNoHeaderMapping = new VariableDTO();
		variableWithNoHeaderMapping.setId(null);

		// Add duplicate variables
		final VariableDTO variableDuplicate1 = new VariableDTO();
		variableDuplicate1.setHeaderName("SOME_VARIABLE");
		final VariableDTO variableDuplicate2 = new VariableDTO();
		variableDuplicate2.setHeaderName("SOME_VARIABLE");

		// Add Entry_Type Variable
		final VariableDTO entryTypeVariable = new VariableDTO();
		entryTypeVariable.setHeaderName(TermId.ENTRY_TYPE.name());
		entryTypeVariable.setId(TermId.ENTRY_TYPE.getId());

		//
		Mockito.when(this.dataImportService.findMeasurementVariableByTermId(ArgumentMatchers.eq(TermId.LOCATION_ID.getId()), ArgumentMatchers.anyListOf(
				MeasurementVariable.class))).thenReturn(Optional.absent());
		Mockito.when(this.dataImportService.findMeasurementVariableByTermId(ArgumentMatchers.eq(TermId.TRIAL_LOCATION.getId()), ArgumentMatchers.anyListOf(
				MeasurementVariable.class))).thenReturn(Optional.of(new MeasurementVariable()));

		final VariableDTO[] variables = { variableWithNoHeaderMapping, variableDuplicate1, variableDuplicate2, entryTypeVariable } ;

		this.controller.processImport(variables);

		final ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);


		Mockito.verify(this.etlService).mergeVariableData(variables, this.userSelection, true);
		Mockito.verify(this.etlService).validateProjectOntology(workbook);

		Mockito.verify(this.etlService, Mockito.times(6)).convertMessageList(captor.capture());

		final List<List> messages = captor.getAllValues();
		final List<String> messageKeys = new ArrayList<>();
		for (final List<Message> message : messages) {
			messageKeys.add(message.get(0).getMessageKey());
		}

		Assert.assertTrue(messageKeys.contains(AngularMapOntologyController.ERROR_HEADER_NO_MAPPING));
		Assert.assertTrue(messageKeys.contains(AngularMapOntologyController.ERROR_DUPLICATE_LOCAL_VARIABLE));
		Assert.assertTrue(messageKeys.contains(AngularMapOntologyController.ERROR_LOCATION_ID_DOESNT_EXISTS));
		Assert.assertTrue(messageKeys.contains(AngularMapOntologyController.INVALID_MEANS_IMPORT_VARIABLE));

	}

	@Test
	public void testProcessImporHeaderSuccess() throws IOException, WorkbookParserException {
		final org.apache.poi.ss.usermodel.Workbook apacheWorkbook = Mockito
				.mock(org.apache.poi.ss.usermodel.Workbook.class);
		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(1, new StudyTypeDto(TRIAL_TYPE_ID,"","T"), "Sample Study", 1,
				false);

		Mockito.when(this.etlService.retrieveCurrentWorkbook(this.userSelection)).thenReturn(apacheWorkbook);
		Mockito.when(this.etlService.convertToWorkbook(this.userSelection)).thenReturn(workbook);
		Mockito.when(this.dataImportService.parseWorkbookDescriptionSheet(apacheWorkbook, CURRENT_IBDB_USER_ID)).thenReturn(workbook);

		final VariableDTO variable1 = new VariableDTO();
		variable1.setId(101);
		variable1.setHeaderName("VARIABLE1");
		final VariableDTO variable2 = new VariableDTO();
		variable2.setId(102);
		variable2.setHeaderName("VARIABLE2");

		Mockito.when(this.dataImportService.findMeasurementVariableByTermId(ArgumentMatchers.eq(TermId.LOCATION_ID.getId()), ArgumentMatchers.anyListOf(
				MeasurementVariable.class))).thenReturn(Optional.of(new MeasurementVariable()));
		Mockito.when(this.dataImportService.findMeasurementVariableByTermId(ArgumentMatchers.eq(TermId.TRIAL_LOCATION.getId()), ArgumentMatchers.anyListOf(
				MeasurementVariable.class))).thenReturn(Optional.of(new MeasurementVariable()));

		final VariableDTO[] variables = { variable1, variable2 } ;

		this.controller.processImport(variables);

		Mockito.verify(this.etlService).mergeVariableData(variables, this.userSelection, true);
		Mockito.verify(this.etlService).validateProjectOntology(workbook);

		Mockito.verify(this.etlService, Mockito.times(0)).convertMessageList(Mockito.anyListOf(Message.class));

	}

	@Test
	public void testProcessInvalidTrialInstanceValue() throws IOException, WorkbookParserException {
		final org.apache.poi.ss.usermodel.Workbook apacheWorkbook = Mockito
			.mock(org.apache.poi.ss.usermodel.Workbook.class);
		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(1, new StudyTypeDto(TRIAL_TYPE_ID,"","T"), "Sample Study", 1,
			false);

		// Replace Value of Trial Instance
		for (final MeasurementVariable var : workbook.getConditions()) {
			if (var.getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
				var.setValue("a");
			}
		}

		Mockito.when(this.etlService.retrieveCurrentWorkbook(this.userSelection)).thenReturn(apacheWorkbook);
		Mockito.when(this.etlService.convertToWorkbook(this.userSelection)).thenReturn(workbook);
		Mockito.when(this.dataImportService.parseWorkbookDescriptionSheet(apacheWorkbook, CURRENT_IBDB_USER_ID)).thenReturn(workbook);

		final VariableDTO variable1 = new VariableDTO();
		variable1.setId(101);
		variable1.setHeaderName("VARIABLE1");
		final VariableDTO variable2 = new VariableDTO();
		variable2.setId(102);
		variable2.setHeaderName("VARIABLE2");

		Mockito.when(this.dataImportService.findMeasurementVariableByTermId(ArgumentMatchers.eq(TermId.LOCATION_ID.getId()), ArgumentMatchers.anyListOf(
			MeasurementVariable.class))).thenReturn(Optional.of(new MeasurementVariable()));
		Mockito.when(this.dataImportService.findMeasurementVariableByTermId(ArgumentMatchers.eq(TermId.TRIAL_LOCATION.getId()), ArgumentMatchers.anyListOf(
			MeasurementVariable.class))).thenReturn(Optional.of(new MeasurementVariable()));

		final VariableDTO[] variables = { variable1, variable2 } ;

		this.controller.processImport(variables);

		Mockito.verify(this.etlService).mergeVariableData(variables, this.userSelection, true);
		Mockito.verify(this.etlService).validateProjectOntology(workbook);
		// With Error
		Mockito.verify(this.etlService, Mockito.times(1)).convertMessageList(Mockito.anyListOf(Message.class));

		final Map<String, List<Message>> errorMessages = new HashMap<>();
		this.controller.validateTrialInstanceValue(workbook.getConditions(), errorMessages);
		Assert.assertTrue("Invalid Trial Error",errorMessages.containsKey(Constants.INVALID_TRIAL));
		Assert.assertEquals("Single Error",1, errorMessages.get(Constants.INVALID_TRIAL).size());
	}

	@Test
	public void testConfirmImport() throws IOException, WorkbookParserException {
		final org.apache.poi.ss.usermodel.Workbook apacheWorkbook = Mockito
				.mock(org.apache.poi.ss.usermodel.Workbook.class);
		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(1, new StudyTypeDto(TRIAL_TYPE_ID,"","T"), "Sample Study", 1,
				false);
		workbook.getStudyDetails().getStudyType().setId(1);
		Mockito.when(this.etlService.retrieveCurrentWorkbook(this.userSelection)).thenReturn(apacheWorkbook);
		Mockito.when(this.etlService.convertToWorkbook(this.userSelection)).thenReturn(workbook);
		Mockito.when(this.dataImportService.parseWorkbookDescriptionSheet(apacheWorkbook, CURRENT_IBDB_USER_ID)).thenReturn(workbook);

		final VariableDTO[] variables = new VariableDTO[] {};

		final Map<String, Object> result = this.controller.confirmImport(variables, true, this.session, this.request);

		Mockito.verify(this.userSelection).clearMeasurementVariables();
		Mockito.verify(this.etlService).mergeVariableData(variables, this.userSelection, true);
		Mockito.verify(this.dataImportService).addLocationIDVariableIfNotExists(workbook, workbook.getFactors(), PROGRAM_UUID);
		Mockito.verify(this.dataImportService).addEntryTypeVariableIfNotExists(workbook, PROGRAM_UUID);
		Mockito.verify(this.dataImportService).assignLocationIdVariableToEnvironmentDetailSection(workbook);
		Mockito.verify(this.dataImportService).removeLocationNameVariableIfExists(workbook);
		Mockito.verify(this.fieldbookService).addStudyUUIDConditionAndObsUnitIDFactorToWorkbook(workbook, false);
		Mockito.verify(this.etlService).saveProjectOntology(workbook, AngularMapOntologyControllerTest.PROGRAM_UUID);

		Mockito.verify(this.userSelection).setStudyId(workbook.getStudyDetails().getId());
		Mockito.verify(this.userSelection).setTrialDatasetId(workbook.getTrialDatasetId());
		Mockito.verify(this.userSelection).setMeasurementDatasetId(workbook.getMeasurementDatesetId());
		Mockito.verify(this.userSelection).setMeansDatasetId(workbook.getMeansDatasetId());

		Assert.assertEquals(true, result.get("success"));
		Assert.assertEquals(AngularMapOntologyControllerTest.CONTEXT_PATH + AngularOpenSheetController.URL,
				result.get("redirectUrl"));

	}

	@Test
	public void testProcessExperimentalDesign() throws WorkbookParserException {
		final Workbook importData = new Workbook();
		final org.apache.poi.ss.usermodel.Workbook workbook = Mockito.mock(org.apache.poi.ss.usermodel.Workbook.class);
		final List<String> headers = Arrays.asList("EXPT_DESIGN", "DESIG");
		Mockito.when(this.etlService.retrieveColumnHeaders(workbook, this.userSelection, false)).thenReturn(headers);

		final List<MeasurementVariable> factors = new ArrayList<>();
		factors.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), "EXPT_DESIGN","RCBD"));
		importData.setFactors(factors);

		final String valueFromObsevations = "RCBD";
		Mockito.when(this.etlService.getExperimentalDesignValueFromObservationSheet(workbook, this.userSelection, 0)).thenReturn(valueFromObsevations);

		this.controller.processExperimentalDesign(importData, workbook);

		Mockito.verify(this.etlService).retrieveColumnHeaders(workbook, this.userSelection, false);
		Mockito.verify(this.etlService).getExperimentalDesignValueFromObservationSheet(workbook, this.userSelection, 0);
		Mockito.verify(this.dataImportService).processExperimentalDesign(importData, this.contextUtil.getCurrentProgramUUID(), valueFromObsevations);
	}

	@Test
	public void testCheckIfLocationIdVariableExists() throws IOException, WorkbookParserException {

		final org.apache.poi.ss.usermodel.Workbook apacheWorkbook = Mockito
				.mock(org.apache.poi.ss.usermodel.Workbook.class);
		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(6, new StudyTypeDto(TRIAL_TYPE_ID,"","T"), "Sample Study", 1,
				false);

		Mockito.when(this.etlService.retrieveCurrentWorkbook(this.userSelection)).thenReturn(apacheWorkbook);
		Mockito.when(this.dataImportService.parseWorkbookDescriptionSheet(apacheWorkbook, CURRENT_IBDB_USER_ID)).thenReturn(workbook);
		Mockito.when(this.dataImportService.findMeasurementVariableByTermId(ArgumentMatchers.eq(TermId.LOCATION_ID.getId()), ArgumentMatchers.anyListOf(
				MeasurementVariable.class))).thenReturn(Optional.absent());
		Mockito.when(this.dataImportService.findMeasurementVariableByTermId(ArgumentMatchers.eq(TermId.TRIAL_LOCATION.getId()), ArgumentMatchers.anyListOf(
				MeasurementVariable.class))).thenReturn(Optional.of(new MeasurementVariable()));

		Assert.assertTrue(this.controller.checkIfLocationIdVariableExists(workbook));

		final ArgumentCaptor<List> captor1 = ArgumentCaptor.forClass(List.class);
		final ArgumentCaptor<List> captor2 = ArgumentCaptor.forClass(List.class);
		Mockito.verify(this.dataImportService).findMeasurementVariableByTermId(ArgumentMatchers.eq(TermId.LOCATION_ID.getId()), captor1.capture());
		Mockito.verify(this.dataImportService).findMeasurementVariableByTermId(ArgumentMatchers.eq(TermId.LOCATION_ID.getId()), captor2.capture());

		final List<MeasurementVariable> measurementVariables = new ArrayList<>();

		measurementVariables.addAll(workbook.getConditions());
		measurementVariables.addAll(workbook.getFactors());

		Assert.assertEquals(measurementVariables, captor1.getValue());
		Assert.assertEquals(measurementVariables, captor2.getValue());

	}


}
