
package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.web.common.exception.CrossingTemplateExportException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.commons.parsing.ExcelCellStyleBuilder;
import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.service.FileService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.middleware.data.initializer.ProjectTestDataInitializer;
import org.generationcp.middleware.data.initializer.VariableTypeListTestDataInitializer;
import org.generationcp.middleware.domain.dms.DMSVariableType;
import org.generationcp.middleware.domain.dms.Experiment;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.generationcp.middleware.service.api.user.UserService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

@RunWith(MockitoJUnitRunner.class)
public class CrossingTemplateExcelExporterTest {

	private static final String XLS_EXT = ".xls";
	private static final String STUDY_NAME = "studyname";
	private static final int STUDY_ID = 1;
	private static final String TEST_FILENAME = "testFilename.xls";
	private static final int CURRENT_USER_ID = 1;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private FileService fileService;

	@Mock
	private File templateFile;

	@Mock
	protected ContextUtil contextUtil;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private UserService userService;

	@Mock
	private StudyEntryService studyEntryService;

	@InjectMocks
	private CrossingTemplateExcelExporter exporter;

	private org.apache.poi.ss.usermodel.Workbook workbook;

	private final InstallationDirectoryUtil installationDirectoryUtil = new InstallationDirectoryUtil();

	@Before
	public void setup() throws IOException, InvalidFormatException {
		MockitoAnnotations.initMocks(this);

		this.exporter.setTemplateFile(TEST_FILENAME);
		this.workbook = WorkbookFactory.create(this.getClass().getClassLoader().getResourceAsStream(TEST_FILENAME));
	}

	@After
	public void tearDown() {
		final File file = new File("CrossingTemplate-" + CrossingTemplateExcelExporterTest.STUDY_NAME + XLS_EXT);
		file.deleteOnExit();
	}

	@Test
	public void testExport() throws Exception {
		Mockito.doReturn(1).when(this.fieldbookMiddlewareService).getMeasurementDatasetId(Matchers.anyInt());
		Mockito.doReturn(this.workbook).when(this.fileService).retrieveWorkbookTemplate(TEST_FILENAME);
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(ProjectTestDataInitializer.createProject());
		Mockito.when(this.userService.getUsersByProjectId(ArgumentMatchers.anyLong()))
			.thenReturn(new ArrayList<WorkbenchUser>());
		Mockito.when(this.studyEntryService.countStudyEntries(CrossingTemplateExcelExporterTest.STUDY_ID)).thenReturn(1l);

		final FileExportInfo exportInfo = this.exporter.export(CrossingTemplateExcelExporterTest.STUDY_ID,
				CrossingTemplateExcelExporterTest.STUDY_NAME, CrossingTemplateExcelExporterTest.CURRENT_USER_ID);

		// Check file is written in proper directory and with correct filename
		final String outputDirectoryPath = this.installationDirectoryUtil
				.getOutputDirectoryForProjectAndTool(this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
		final File outputDirectoryFile = new File(outputDirectoryPath);
		Assert.assertTrue(outputDirectoryFile.exists());
		final File outputFile = new File(exportInfo.getFilePath());
		Assert.assertEquals(outputDirectoryFile, outputFile.getParentFile());
		final String expectedBaseFilename = "CrossingTemplate-" + CrossingTemplateExcelExporterTest.STUDY_NAME;
		Assert.assertTrue(outputFile.getName().startsWith(expectedBaseFilename));
		Assert.assertTrue(outputFile.getName().endsWith(XLS_EXT));
		Assert.assertEquals("Uses same study name", expectedBaseFilename + XLS_EXT,
				exportInfo.getDownloadFileName());
	}

	@Test(expected = CrossingTemplateExportException.class)
	public void testExportException() throws Exception {
		Mockito.doThrow(new InvalidFormatException("forced exception")).when(this.fileService).retrieveWorkbookTemplate(TEST_FILENAME);
		this.exporter.export(CrossingTemplateExcelExporterTest.STUDY_ID, CrossingTemplateExcelExporterTest.STUDY_NAME,
				CrossingTemplateExcelExporterTest.CURRENT_USER_ID);
	}

	@Test
	public void testWriteListDetailsSection() throws IOException {
		final Sheet sheet = this.workbook.getSheetAt(0);
		final GermplasmList list = new GermplasmList();
		list.setDate(20150506L);
		list.setType("LST");
		this.exporter.writeListDetailsSection(sheet, 1, new ExcelCellStyleBuilder((HSSFWorkbook) this.workbook),
				CrossingTemplateExcelExporterTest.STUDY_ID, CrossingTemplateExcelExporterTest.STUDY_NAME);

		Assert.assertEquals(sheet.getRow(0).getCell(0).getStringCellValue(), "LIST NAME");
		Assert.assertEquals(sheet.getRow(0).getCell(1).getStringCellValue(), "");
		Assert.assertEquals(sheet.getRow(0).getCell(3).getStringCellValue(), "Enter a list name here, or add it when saving in the BMS");

		Assert.assertEquals(sheet.getRow(1).getCell(0).getStringCellValue(), "LIST DESCRIPTION");
		Assert.assertEquals(sheet.getRow(1).getCell(1).getStringCellValue(), "");
		Assert.assertEquals(sheet.getRow(1).getCell(3).getStringCellValue(),
				"Enter a list description here, or add it when saving in the BMS");

		Assert.assertEquals(sheet.getRow(2).getCell(0).getStringCellValue(), "LIST DATE");
		final Date todaysDate = new Date();
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		final String todaysDateText = dateFormat.format(todaysDate);
		Assert.assertEquals(sheet.getRow(2).getCell(1).getNumericCellValue(), Long.parseLong(todaysDateText), 0.0);
		Assert.assertEquals(sheet.getRow(2).getCell(3).getStringCellValue(), "Accepted formats: YYYYMMDD or YYYYMM or YYYY or blank");
	}

	@Test
	public void testUpdateCodesSection() throws IOException {

		final Project projectMock = Mockito.mock(Project.class);
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(projectMock);


		// User
		final int userId = 8;
		final Person person = new Person();

		person.setFirstName("John");
		person.setLastName("Doe");
		final ArrayList<WorkbenchUser> users = new ArrayList<WorkbenchUser>();
		final WorkbenchUser workbenchUser = new WorkbenchUser();
		workbenchUser.setUserid(userId);
		workbenchUser.setPerson(person);
		users.add(workbenchUser);
		Mockito.when(this.userService.getUsersByProjectId(ArgumentMatchers.anyLong())).thenReturn(users);

		// Methods
		final String mCode = "6";

		final List<Method> methods = new ArrayList<>();
		final Method mockMethod = Mockito.mock(Method.class);
		methods.add(mockMethod);
		Mockito.when(mockMethod.getMcode()).thenReturn(mCode);
		Mockito.when(this.germplasmDataManager.getMethodsByType(Matchers.anyString())).thenReturn(methods);

		final Sheet sheet = this.workbook.getSheetAt(2);
		this.exporter.updateCodesSection(sheet);

		Assert.assertEquals(String.valueOf(userId), sheet.getRow(1).getCell(2).getStringCellValue());
		Assert.assertEquals(mCode, sheet.getRow(2).getCell(2).getStringCellValue());
	}

	@Test
	public void testwriteNurseryListSection() throws IOException {

		final int measurementDataSetId = 10101;
		Mockito.when(this.fieldbookMiddlewareService.getMeasurementDatasetId(Matchers.anyInt()))
				.thenReturn(measurementDataSetId);

		final List<Experiment> experiments = this.intializeExperiments();
		Mockito.when(this.studyDataManager.getExperiments(measurementDataSetId, Collections.singletonList(1), null)).thenReturn
			(experiments);
		Mockito.when(this.studyDataManager.getTreatmentFactorVariableTypes(measurementDataSetId)).thenReturn(
			VariableTypeListTestDataInitializer.createTreatmentFactorsVariableTypeList());

		final Sheet sheet = this.workbook.getSheetAt(3);
		this.exporter.writeStudyListSheet(sheet,
			CrossingTemplateExcelExporterTest.STUDY_ID, CrossingTemplateExcelExporterTest.STUDY_NAME);

		assertThat("studyname", equalTo(sheet.getRow(1).getCell(0).getStringCellValue()));
		assertThat(1, equalTo((int) sheet.getRow(1).getCell(1).getNumericCellValue()));
		assertThat("1", equalTo(sheet.getRow(1).getCell(3).getStringCellValue()));
		assertThat("1", equalTo(sheet.getRow(1).getCell(4).getStringCellValue()));
		assertThat("ABC", equalTo(sheet.getRow(1).getCell(5).getStringCellValue()));
		assertThat("abc/def", equalTo(sheet.getRow(1).getCell(6).getStringCellValue()));
		assertThat(sheet.getRow(0).getCell(7), nullValue());
		assertThat(sheet.getRow(0).getCell(8), nullValue());


	}

	@Test
	public void testwriteNurseryListSectionWithAddUserDescriptors() throws IOException {

		final int measurementDataSetId = 10101;
		Mockito.when(this.fieldbookMiddlewareService.getMeasurementDatasetId(Matchers.anyInt()))
				.thenReturn(measurementDataSetId);
		Mockito.when(this.studyDataManager.getTreatmentFactorVariableTypes(measurementDataSetId)).thenReturn(
			VariableTypeListTestDataInitializer.createTreatmentFactorsVariableTypeList());

		final List<Experiment> experiments = this.intializeExperimentsWithAddUserDescriptors();

		Mockito.when(this.studyDataManager.getExperiments(measurementDataSetId, Collections.singletonList(1), null)).thenReturn
			(experiments);
		final Sheet sheet = this.workbook.getSheetAt(3);
		this.exporter.writeStudyListSheet(sheet,
			CrossingTemplateExcelExporterTest.STUDY_ID, CrossingTemplateExcelExporterTest.STUDY_NAME);

		// Header added//
		assertThat("FIELDMAP COLUMN", equalTo(sheet.getRow(0).getCell(7).getStringCellValue()));
		assertThat("FIELDMAP RANGE", equalTo(sheet.getRow(0).getCell(8).getStringCellValue()));
		assertThat("studyname", equalTo(sheet.getRow(1).getCell(0).getStringCellValue()));

		// Row 1//
		assertThat(1, equalTo((int) sheet.getRow(1).getCell(1).getNumericCellValue()));
		assertThat(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeName(), equalTo(sheet.getRow(1).getCell(2).getStringCellValue()));
		assertThat("801", equalTo(sheet.getRow(1).getCell(3).getStringCellValue()));
		assertThat("801", equalTo(sheet.getRow(1).getCell(4).getStringCellValue()));
		assertThat("CML502A", equalTo(sheet.getRow(1).getCell(5).getStringCellValue()));
		assertThat("-", equalTo(sheet.getRow(1).getCell(6).getStringCellValue()));
		assertThat("1", equalTo(sheet.getRow(1).getCell(7).getStringCellValue()));
		assertThat("100", equalTo(sheet.getRow(1).getCell(8).getStringCellValue()));

		// Row 2//
		assertThat(2, equalTo((int) sheet.getRow(2).getCell(1).getNumericCellValue()));
		assertThat(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeName(), equalTo(sheet.getRow(2).getCell(2).getStringCellValue()));
		assertThat("802", equalTo(sheet.getRow(2).getCell(3).getStringCellValue()));
		assertThat("802", equalTo(sheet.getRow(2).getCell(4).getStringCellValue()));
		assertThat("CLQRCWQ109", equalTo(sheet.getRow(2).getCell(5).getStringCellValue()));
		assertThat("-", equalTo(sheet.getRow(2).getCell(6).getStringCellValue()));
		assertThat("2", equalTo(sheet.getRow(2).getCell(7).getStringCellValue()));
		assertThat("100", equalTo(sheet.getRow(2).getCell(8).getStringCellValue()));

	}

	private List<Experiment> intializeExperimentsWithAddUserDescriptors() {
		final List<Experiment> experiments = new ArrayList<Experiment>();
		VariableList factors = new VariableList();
		factors.add(this.createTestVariable(TermId.PLOT_NO.getId(), "1"));
		factors.add(this.createTestVariable(TermId.GID.getId(), "801"));
		factors.add(this.createTestVariable(TermId.DESIG.getId(), "CML502A"));
		factors.add(this.createTestVariable(TermId.CROSS.getId(), "-"));
		factors.add(this.createTestVariable(TermId.ENTRY_TYPE.getId(), "10170"));
		factors.add(this.createTestVariable(TermId.FIELDMAP_COLUMN.getId(), "FIELDMAP COLUMN", "1"));
		factors.add(this.createTestVariable(TermId.FIELDMAP_RANGE.getId(), "FIELDMAP RANGE", "100"));
		factors.add(this.createTestVariable(1001, "NFert_NO", "VALUE"));
		experiments.add(this.intializeExperiments(factors, 0));

		factors = new VariableList();
		factors.add(this.createTestVariable(TermId.PLOT_NO.getId(), "2"));
		factors.add(this.createTestVariable(TermId.GID.getId(), "802"));
		factors.add(this.createTestVariable(TermId.DESIG.getId(), "CLQRCWQ109"));
		factors.add(this.createTestVariable(TermId.CROSS.getId(), "-"));
		factors.add(this.createTestVariable(TermId.ENTRY_TYPE.getId(), "10170"));
		factors.add(this.createTestVariable(TermId.FIELDMAP_COLUMN.getId(), "FIELDMAP COLUMN", "2"));
		factors.add(this.createTestVariable(TermId.FIELDMAP_RANGE.getId(), "FIELDMAP RANGE", "100"));

		experiments.add(this.intializeExperiments(factors, 1));

		return experiments;
	}

	@Test
	public void testChangeInvalidaCharacterExportFilename() throws Exception {
		final String studyName = "Nueva Nursery \\ / : * ? \" \\&quot; &lt; &gt; | ,";
		final String expectedBaseFilename = "CrossingTemplate-Nueva Nursery _ _ _ _ _ _ __ _ _ _ _";

		Mockito.doReturn(1).when(this.fieldbookMiddlewareService).getMeasurementDatasetId(Matchers.anyInt());
		Mockito.doReturn(this.workbook).when(this.fileService).retrieveWorkbookTemplate(TEST_FILENAME);
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(ProjectTestDataInitializer.createProject());
		Mockito.when(this.userService.getUsersByProjectId(ArgumentMatchers.anyLong()))
			.thenReturn(new ArrayList<WorkbenchUser>());
		Mockito.when(this.studyEntryService.countStudyEntries(CrossingTemplateExcelExporterTest.STUDY_ID)).thenReturn(1l);

		// to test
		final FileExportInfo exportInfo = this.exporter.export(CrossingTemplateExcelExporterTest.STUDY_ID, studyName,
				CrossingTemplateExcelExporterTest.CURRENT_USER_ID);

		// Check file is written in proper directory and with correct filename
		final String outputDirectoryPath = this.installationDirectoryUtil
				.getOutputDirectoryForProjectAndTool(this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
		final File outputDirectoryFile = new File(outputDirectoryPath);
		Assert.assertTrue(outputDirectoryFile.exists());
		final File outputFile = new File(exportInfo.getFilePath());
		Assert.assertEquals(outputDirectoryFile, outputFile.getParentFile());
		Assert.assertTrue(outputFile.getName().startsWith(expectedBaseFilename));
		Assert.assertTrue(outputFile.getName().endsWith(XLS_EXT));
		Assert.assertEquals("Cleaned up study name", expectedBaseFilename + XLS_EXT,
				exportInfo.getDownloadFileName());
	}

	@Test(expected = CrossingTemplateExportException.class)
	@SuppressWarnings("unchecked")
	public void retrieveAndValidateIfHasGermplasmListExceptionHandling() throws Exception {
		Mockito.when(this.studyEntryService.countStudyEntries(CrossingTemplateExcelExporterTest.STUDY_ID)).thenReturn(0l);
		this.exporter.validateIfStudyHasEntries(CrossingTemplateExcelExporterTest.STUDY_ID);
	}

	private List<GermplasmList> initializeCrossesList() {
		final List<GermplasmList> list = new ArrayList<>();

		for (int i = 0; i < 5; i++) {
			final GermplasmList gplist = new GermplasmList();
			gplist.setId(i);
			gplist.setDate(20150506L);
			list.add(gplist);
		}

		return list;
	}

	private List<Experiment> intializeExperiments() {
		final List<Experiment> list = new ArrayList<>();
		final Experiment experiment = new Experiment();

		final VariableList factors = new VariableList();
		factors.add(this.createTestVariable(TermId.PLOT_NO.getId(), "1"));
		factors.add(this.createTestVariable(TermId.GID.getId(), "1"));
		factors.add(this.createTestVariable(TermId.DESIG.getId(), "ABC"));
		factors.add(this.createTestVariable(TermId.CROSS.getId(), "abc/def"));

		experiment.setFactors(factors);
		list.add(experiment);

		return list;
	}

	private Experiment intializeExperiments(final VariableList factors, final int id) {
		final Experiment experiment = new Experiment();

		experiment.setFactors(factors);
		experiment.setId(id);

		return experiment;
	}

	private Variable createTestVariable(final Integer termId, final String value) {
		final Variable testVariable = new Variable();
		testVariable.setValue(value);
		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(termId);
		final DMSVariableType variableType = new DMSVariableType("test", "test", standardVariable, 0);
		testVariable.setVariableType(variableType);

		return testVariable;
	}

	private Variable createTestVariable(final Integer termId, final String localname, final String value) {
		final Variable testVariable = new Variable();
		testVariable.setValue(value);
		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(termId);
		final DMSVariableType variableType = new DMSVariableType(localname, localname, standardVariable, 0);
		testVariable.setVariableType(variableType);

		return testVariable;
	}

	@After
	public void cleanup() {
		this.deleteTestInstallationDirectory();
	}

	private void deleteTestInstallationDirectory() {
		// Delete test installation directory and its contents as part of cleanup
		final File testInstallationDirectory = new File(InstallationDirectoryUtil.WORKSPACE_DIR);
		this.installationDirectoryUtil.recursiveFileDelete(testInstallationDirectory);
	}
}
