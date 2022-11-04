
package com.efficio.etl.web;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.web.bean.FileUploadForm;
import com.efficio.etl.web.bean.UserSelection;
import com.efficio.etl.web.validators.FileUploadFormValidator;
import com.efficio.fieldbook.service.api.FieldbookService;
import com.google.common.base.Optional;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.HTTPSessionUtil;
import org.generationcp.commons.util.StudyPermissionValidator;
import org.generationcp.middleware.data.initializer.MeasurementRowTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.operation.parser.WorkbookParser;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.util.Message;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */

public class FileUploadControllerTest {

	private static final int USER_ID = 1;
	private static final String PROGRAM_UUID = "55bd5dde-3a68-4dcd-bdda-d2301eff9e16";
	@Mock
	private MultipartFile file;

	@Mock
	private ETLService etlService;

	@Mock
	private DataImportService dataImportService;

	@Mock
	private FieldbookService fieldbookService;

	@Mock
	private UserSelection userSelection;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private HTTPSessionUtil httpSessionUtil;

	@Mock
	private BindingResult result;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	private Model model;

	@Mock
	private Workbook workbook;

	@Mock
	private File mockFile;

	@Mock
	private StudyPermissionValidator studyPermissionValidator;

	@Mock
	private Optional<StudyReference> studyOptional;

	private FileUploadForm form;
	private HttpSession session;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private MeasurementVariable obsUnitIdMeasurementVariable;

	private Locale locale = LocaleContextHolder.getLocale();
	private CropType crop;

	@InjectMocks
	private FileUploadController fileUploadController;

	@Before
	public void setup() throws WorkbookParserException, IOException {
		MockitoAnnotations.initMocks(this);

		this.session = new MockHttpSession();
		this.request = new MockHttpServletRequest();
		this.response = new MockHttpServletResponse();

		this.form = new FileUploadForm();
		this.form.setFile(this.file);

		this.fileUploadController.setEtlService(this.etlService);
		this.fileUploadController.setUserSelection(this.userSelection);
		this.fileUploadController.setStudyPermissionValidator(this.studyPermissionValidator);

		final Project project = new Project();
		this.crop = new CropType("Maize");
		project.setCropType(this.crop);
		project.setProjectId(Long.valueOf(1));
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(project);
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(FileUploadControllerTest.PROGRAM_UUID);
		Mockito.when(this.contextUtil.getCurrentWorkbenchUserId()).thenReturn(USER_ID);

		this.obsUnitIdMeasurementVariable = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.OBS_UNIT_ID.getId(), TermId.OBS_UNIT_ID.name(), null);
		Mockito.when(this.fieldbookService.createMeasurementVariable(String.valueOf(TermId.OBS_UNIT_ID.getId()), "",
				Operation.ADD, PhenotypicType.TRIAL_DESIGN)).thenReturn(this.obsUnitIdMeasurementVariable);

		Mockito.when(this.fieldbookMiddlewareService.getMeasurementVariableByPropertyScaleMethodAndRole(
				Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.any(PhenotypicType.class),
				Matchers.anyString())).thenReturn(null);

		Mockito.when(this.etlService.retrieveCurrentWorkbookAsFile(this.userSelection)).thenReturn(this.mockFile);
		Mockito.when(this.dataImportService.strictParseWorkbook(this.mockFile, FileUploadControllerTest.PROGRAM_UUID, USER_ID)).thenReturn(this.workbook);
		Mockito.when(this.studyPermissionValidator.userLacksPermissionForStudy(Matchers.any(StudyReference.class))).thenReturn(false);
		Mockito.when(this.fieldbookMiddlewareService.getStudyReferenceByNameAndProgramUUID(ArgumentMatchers.<String>isNull(), Matchers.anyString())).thenReturn(this.studyOptional);
	}

	@Test
	public void testEmptyFileHandling() {

		this.form.setFile(null);

		// stub the hasErrors call to provide expected fileUploadController flow
		Mockito.when(this.result.hasErrors()).thenReturn(true);

		final String navigationResult = this.fileUploadController.uploadFile(this.form, this.result, this.model);

		// verify if the expected methods in the mock object were called
		Mockito.verify(this.result).rejectValue("file", FileUploadFormValidator.FILE_NOT_FOUND_ERROR);

		System.out.println(navigationResult);

		Assert.assertEquals(this.fileUploadController.getContentName(), navigationResult);
	}

	@Test
	public void testNonExcelFileUpload() {
		this.form.setFile(this.file);

		Mockito.when(this.file.getOriginalFilename()).thenReturn("something.txt");
		Mockito.when(this.result.hasErrors()).thenReturn(true);

		final String navigationResult = this.fileUploadController.uploadFile(this.form, this.result, this.model);

		// verify if the expected methods in the mock object were called
		Mockito.verify(this.result).rejectValue("file", FileUploadFormValidator.FILE_NOT_EXCEL_ERROR);

		Assert.assertEquals(this.fileUploadController.getContentName(), navigationResult);
	}

	@Test
	public void testStartProcessSuccessful() throws WorkbookParserException {

		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(1, StudyTypeDto.getTrialDto(), "Sample Study", 1,
				false);

		Mockito.when(this.dataImportService.parseWorkbook(Matchers.any(File.class), Matchers.anyString(),
				Matchers.anyBoolean(), Matchers.any(WorkbookParser.class), Matchers.anyInt())).thenReturn(workbook);

		final Map<String, String> returnMessage = this.fileUploadController.startProcess(0, this.session, this.request,
				this.response, this.model);

		Mockito.verify(this.fieldbookService).addStudyUUIDConditionAndObsUnitIDFactorToWorkbook(workbook, true);
		Mockito.verify(this.dataImportService).saveDataset(workbook, FileUploadControllerTest.PROGRAM_UUID,
				this.crop);
		Mockito.verify(this.httpSessionUtil).clearSessionData(this.session,
				new String[] { HTTPSessionUtil.USER_SELECTION_SESSION_NAME });

		Assert.assertEquals(FileUploadController.STATUS_CODE_SUCCESSFUL,
				returnMessage.get(FileUploadController.STATUS_CODE));
		Assert.assertEquals("Import is done.", returnMessage.get(FileUploadController.STATUS_MESSAGE));

	}

	@Test
	public void testStartProcessParserException() throws WorkbookParserException {

		final String errorMessage = "sample message";
		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(1, StudyTypeDto.getTrialDto(), "Sample Study", 1,
				false);

		Mockito.when(this.dataImportService.parseWorkbook(Matchers.any(File.class), Matchers.anyString(),
				Matchers.anyBoolean(), Matchers.any(WorkbookParser.class), Matchers.anyInt()))
				.thenThrow(new WorkbookParserException(errorMessage));

		final Map<String, String> returnMessage = this.fileUploadController.startProcess(0, this.session, this.request,
				this.response, this.model);

		Mockito.verify(this.fieldbookService, Mockito.times(0))
				.addMeasurementVariableToList(this.obsUnitIdMeasurementVariable, workbook.getFactors());
		Mockito.verify(this.fieldbookService, Mockito.times(0)).addStudyUUIDConditionAndObsUnitIDFactorToWorkbook(workbook,
				false);
		Mockito.verify(this.fieldbookService, Mockito.times(0))
				.addMeasurementVariableToMeasurementRows(this.obsUnitIdMeasurementVariable, workbook.getObservations());
		Mockito.verify(this.dataImportService, Mockito.times(0)).saveDataset(workbook,
				FileUploadControllerTest.PROGRAM_UUID, this.crop);
		Mockito.verify(this.httpSessionUtil, Mockito.times(0)).clearSessionData(this.session,
				new String[] { HTTPSessionUtil.USER_SELECTION_SESSION_NAME });

		Assert.assertEquals(FileUploadController.STATUS_CODE_HAS_ERROR,
				returnMessage.get(FileUploadController.STATUS_CODE));
		Assert.assertEquals(errorMessage, returnMessage.get(FileUploadController.STATUS_MESSAGE));
		Assert.assertEquals(WorkbookParserException.class.getSimpleName(),
				returnMessage.get(FileUploadController.ERROR_TYPE));

	}

	@Test
	public void testStartProcessParserIOException() throws WorkbookParserException, IOException {

		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(1, StudyTypeDto.getTrialDto(), "Sample Study", 1,
				false);

		Mockito.when(this.etlService.retrieveCurrentWorkbookAsFile(this.userSelection)).thenThrow(new IOException());

		final Map<String, String> returnMessage = this.fileUploadController.startProcess(0, this.session, this.request,
				this.response, this.model);

		Mockito.verify(this.fieldbookService, Mockito.times(0))
				.addMeasurementVariableToList(this.obsUnitIdMeasurementVariable, workbook.getFactors());
		Mockito.verify(this.fieldbookService, Mockito.times(0)).addStudyUUIDConditionAndObsUnitIDFactorToWorkbook(workbook,
				false);
		Mockito.verify(this.fieldbookService, Mockito.times(0))
				.addMeasurementVariableToMeasurementRows(this.obsUnitIdMeasurementVariable, workbook.getObservations());
		Mockito.verify(this.dataImportService, Mockito.times(0)).saveDataset(workbook,
				FileUploadControllerTest.PROGRAM_UUID, this.crop);
		Mockito.verify(this.httpSessionUtil, Mockito.times(0)).clearSessionData(this.session,
				new String[] { HTTPSessionUtil.USER_SELECTION_SESSION_NAME });

		Assert.assertEquals(FileUploadController.STATUS_CODE_HAS_ERROR,
				returnMessage.get(FileUploadController.STATUS_CODE));
		Assert.assertEquals("An error occurred while reading the file.",
				returnMessage.get(FileUploadController.STATUS_MESSAGE));
		Assert.assertEquals(IOException.class.getSimpleName(), returnMessage.get(FileUploadController.ERROR_TYPE));

	}

	@Test
	public void testConvertEntryTypeNameToID() {
		final String value = "T";
		final MeasurementVariable measurementVariable = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.ENTRY_TYPE.getId(), TermId.ENTRY_TYPE.name(), value);
		Mockito.when(this.fieldbookMiddlewareService.getMeasurementVariableByPropertyScaleMethodAndRole(
				Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.any(PhenotypicType.class),
				Matchers.anyString())).thenReturn(measurementVariable);
		final List<MeasurementRow> observations = MeasurementRowTestDataInitializer.createMeasurementRowList(
				TermId.ENTRY_TYPE.getId(), TermId.ENTRY_TYPE.name(), value, measurementVariable);
		final Map<String, Integer> availableEntryTypes = new HashMap<>();
		availableEntryTypes.put(value, TermId.ENTRY_TYPE.getId());
		final MeasurementData mdata = observations.get(0).getMeasurementData(TermId.ENTRY_TYPE.getId());
		this.fileUploadController.convertEntryTypeNameToID(FileUploadControllerTest.PROGRAM_UUID, observations,
				availableEntryTypes);

		Assert.assertEquals(String.valueOf(TermId.ENTRY_TYPE.getId()), mdata.getValue());
	}

	@Test
	public void testValidateAndParseWorkbookForNewStudy() {
		final HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);
		Mockito.when(this.studyOptional.isPresent()).thenReturn(false);

		final Map<String, String> returnMessage = this.fileUploadController.validateAndParseWorkbook(this.session, this.request, mockResponse, this.model, this.locale);
		Mockito.verify(mockResponse).setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		Mockito.verify(mockResponse).setHeader("Pragma", "no-cache");
		Mockito.verify(mockResponse).setDateHeader("Expires", 0);
		Assert.assertEquals(FileUploadController.STATUS_CODE_SUCCESSFUL, returnMessage.get(FileUploadController.STATUS_CODE));
		Assert.assertEquals("", returnMessage.get(FileUploadController.STATUS_MESSAGE));
	}

	@Test
	public void testValidateAndParseWorkbookForExistingStudy() {
		final HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);
		Mockito.when(this.studyOptional.isPresent()).thenReturn(true);

		final Map<String, String> returnMessage = this.fileUploadController.validateAndParseWorkbook(this.session, this.request, mockResponse, this.model, this.locale);
		Mockito.verify(mockResponse).setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		Mockito.verify(mockResponse).setHeader("Pragma", "no-cache");
		Mockito.verify(mockResponse).setDateHeader("Expires", 0);
		Assert.assertEquals(FileUploadController.STATUS_CODE_SUCCESSFUL, returnMessage.get(FileUploadController.STATUS_CODE));
		Assert.assertEquals("", returnMessage.get(FileUploadController.STATUS_MESSAGE));
	}

	@Test
	public void testValidateAndParseWorkbookForExistingButRestrictedStudy() {
		final HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);
		injectMessageSource();

		Mockito.when(this.studyOptional.isPresent()).thenReturn(true);
		Mockito.when(this.studyPermissionValidator.userLacksPermissionForStudy(Matchers.any(StudyReference.class))).thenReturn(true);
		final StudyReference study = new StudyReference(1, "Study1");
		Mockito.when(this.studyOptional.get()).thenReturn(study);


		final Map<String, String> returnMessage = this.fileUploadController.validateAndParseWorkbook(this.session, this.request, mockResponse, this.model, locale);
		Mockito.verify(mockResponse).setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		Mockito.verify(mockResponse).setHeader("Pragma", "no-cache");
		Mockito.verify(mockResponse).setDateHeader("Expires", 0);
		Assert.assertEquals(FileUploadController.STATUS_CODE_LACKS_PERMISSION, returnMessage.get(FileUploadController.STATUS_CODE));
		Assert.assertEquals(FileUploadController.USER_LACKS_PERMISSION_MESSAGE, returnMessage.get(FileUploadController.STATUS_MESSAGE));
	}

	private void injectMessageSource() {
		ResourceBundleMessageSource bundleMessageSource = new ResourceBundleMessageSource();
		bundleMessageSource.setUseCodeAsDefaultMessage(true);
		ReflectionTestUtils.setField(this.fileUploadController, "messageSource", bundleMessageSource);
	}

	@Test
	public void testValidateAndParseWorkbookWithIOException() throws IOException {
		final String message = "Some IOException message";
		Mockito.doThrow(new IOException(message)).when(this.etlService).retrieveCurrentWorkbookAsFile(this.userSelection);

		final Map<String, String> returnMessage = this.fileUploadController.validateAndParseWorkbook(this.session, this.request, this.response, this.model, this.locale);
		Assert.assertEquals(FileUploadController.STATUS_CODE_HAS_ERROR, returnMessage.get(FileUploadController.STATUS_CODE));
		Assert.assertEquals(message, returnMessage.get(FileUploadController.STATUS_MESSAGE));
		Assert.assertEquals("IOException", returnMessage.get(FileUploadController.ERROR_TYPE));
	}

	@Test
	public void testValidateAndParseWorkbookWithOutOfBoundsData() {
		Mockito.when(this.workbook.hasOutOfBoundsData()).thenReturn(true);
		final Map<String, String> returnMessage = this.fileUploadController.validateAndParseWorkbook(this.session, this.request, this.response, this.model, this.locale);
		Assert.assertEquals(FileUploadController.STATUS_CODE_HAS_OUT_OF_BOUNDS, returnMessage.get(FileUploadController.STATUS_CODE));
		Assert.assertEquals("", returnMessage.get(FileUploadController.STATUS_MESSAGE));
	}

	@Test
	public void testValidateAndParseWorkbookOverMaxLimit() throws IOException {
		ResourceBundleMessageSource bundleMessageSource = new ResourceBundleMessageSource();
		bundleMessageSource.setUseCodeAsDefaultMessage(true);
		ReflectionTestUtils.setField(this.fileUploadController, "messageSource", bundleMessageSource);
		final String messageKey = "error.observation.over.maximum.limit";
		Mockito.doAnswer(new Answer<File>() {

			@Override
			public File answer(final InvocationOnMock invocationOnMock) throws Throwable {
				throw new WorkbookParserException(Arrays.asList(new Message(messageKey)));
			}
		}).when(this.etlService).retrieveCurrentWorkbookAsFile(this.userSelection);

		final Map<String, String> returnMessage = this.fileUploadController.validateAndParseWorkbook(this.session, this.request, this.response, this.model, this.locale);
		Assert.assertEquals(FileUploadController.STATUS_CODE_HAS_ERROR, returnMessage.get(FileUploadController.STATUS_CODE));
		Assert.assertEquals("The system detected format errors in the file:<br/><br/>" + messageKey  + "<br />", returnMessage.get(FileUploadController.STATUS_MESSAGE));
		Assert.assertEquals("WorkbookParserException-OverMaxLimit", returnMessage.get(FileUploadController.ERROR_TYPE));
	}
}
