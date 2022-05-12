
package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.ExportGermplasmListForm;
import com.efficio.fieldbook.web.common.service.ExportStudyEntriesService;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.FileNameGenerator;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.middleware.data.initializer.ProjectTestDataInitializer;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class ExportStudyEntriesControllerTest {

	private static final int LIST_ID = 105;
	private static final long LIST_DATE = 20141112L;
	private static final String SAMPLE_NOTES = "Sample Notes";
	private static final String LST = "LST";
	private static final String SAMPLE_DESCRIPTION = "Sample description";
	private static final String SAMPLE_LIST = "Sample List";
	private static final String LIST_NAME = "LIST NAME";
	private static final int CURRENT_USER_ID = 1;
	private static final int LIST_STATUS = 1;
	// Columns
	private static final String ENTRY_NO = "entryNo";
	private static final String GID = "gid";
	private static final String ENTRY_CODE = "entryCode";
	private static final String DESIGNATION = "desig";
	private static final String PARENTAGE = "parentage";
	private static final String SEED_SOURCE = "seedSource";
	private static final String CHECK = "check";
	private static final String ENTRY_NUMBER_STORAGE = "entryNoStorage";

	private static final String NUMERIC_VARIABLE = "NUMERIC VARIABLE";
	private static final String TEST_METHOD = "TEST METHOD";
	private static final String TEST_SCALE = "TEST SCALE";
	private static final String TEST_PROPERTY = "TEST PROPERTY";
	private static final String TEST_DESCRIPTION = "TEST DESCRIPTION";

	private static final int EXCEL_TYPE = 1;
	private static final int CSV_TYPE = 2;

	@Mock
	private HttpServletResponse response;

	@Mock
	private HttpServletRequest req;

	@Mock
	private UserSelection userSelection;

	@Mock
	private OntologyService ontologyService;

	@Mock
	private FieldbookProperties fieldbookProperties;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private ExportStudyEntriesService exportStudyEntriesService;

	@Mock
	private StudyEntryService studyEntryService;

	@Mock
	private ContextUtil contextUtil;

	@InjectMocks
	private ExportStudyEntriesController exportStudyEntriesController;

	private final InstallationDirectoryUtil installationDirectoryUtil = new InstallationDirectoryUtil();

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		Mockito.when(this.ontologyService.getStandardVariable(TermId.ENTRY_NO.getId(), this.contextUtil.getCurrentProgramUUID()))
				.thenReturn(this.createStandardVariable(TermId.ENTRY_NO.getId(), ExportStudyEntriesControllerTest.ENTRY_NO));
		Mockito.when(this.ontologyService.getStandardVariable(TermId.DESIG.getId(), this.contextUtil.getCurrentProgramUUID()))
				.thenReturn(this.createStandardVariable(TermId.DESIG.getId(), ExportStudyEntriesControllerTest.DESIGNATION));
		Mockito.when(this.ontologyService.getStandardVariable(TermId.GID.getId(), this.contextUtil.getCurrentProgramUUID()))
				.thenReturn(this.createStandardVariable(TermId.GID.getId(), ExportStudyEntriesControllerTest.GID));
		Mockito.when(this.ontologyService.getStandardVariable(TermId.CROSS.getId(), this.contextUtil.getCurrentProgramUUID()))
				.thenReturn(this.createStandardVariable(TermId.CROSS.getId(), ExportStudyEntriesControllerTest.PARENTAGE));
		Mockito.when(this.ontologyService.getStandardVariable(TermId.ENTRY_CODE.getId(), this.contextUtil.getCurrentProgramUUID()))
				.thenReturn(this.createStandardVariable(TermId.ENTRY_CODE.getId(), ExportStudyEntriesControllerTest.ENTRY_CODE));
		Mockito.when(
				this.ontologyService.getStandardVariable(TermId.ENTRY_NUMBER_STORAGE.getId(), this.contextUtil.getCurrentProgramUUID()))
				.thenReturn(this.createStandardVariable(TermId.ENTRY_NUMBER_STORAGE.getId(),
						ExportStudyEntriesControllerTest.ENTRY_NUMBER_STORAGE));
		Mockito.when(this.ontologyService.getStandardVariable(TermId.ENTRY_TYPE.getId(), this.contextUtil.getCurrentProgramUUID()))
				.thenReturn(this.createStandardVariable(TermId.ENTRY_TYPE.getId(), ExportStudyEntriesControllerTest.CHECK));
		Mockito.doReturn(this.getPlotLevelList()).when(this.userSelection).getPlotsLevelList();

		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListById(ArgumentMatchers.anyInt())).thenReturn(this.getGermplasmList());

		Mockito.doReturn(ProjectTestDataInitializer.createProject()).when(this.contextUtil).getProjectInContext();
	}


	private File getOutputFilePath() {
		final String outputDirectoryPath = this.installationDirectoryUtil.getOutputDirectoryForProjectAndTool(this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
		final File outputDirectoryFile = new File(outputDirectoryPath);
		Assert.assertTrue(outputDirectoryFile.exists());
		File outputFile = null;
		for (final File file : outputDirectoryFile.listFiles()) {
			System.out.println(file.getName());
			if (file.getName().startsWith(ExportStudyEntriesController.EXPORTED_GERMPLASM_LIST)) {
				outputFile = file;
			}
		}
		return outputFile;
	}

	@Test
	public void testExportGermplasmListCSVForStudy() throws JsonParseException, JsonMappingException, IOException {

		final ExportGermplasmListForm form = new ExportGermplasmListForm();
		form.setGermplasmListVisibleColumns("0");
		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		final int studyId = new Random().nextInt();
		studyDetails.setId(studyId);
		workbook.setStudyDetails(studyDetails);
		Mockito.doReturn(workbook).when(this.userSelection).getWorkbook();
		Mockito.doReturn(10L).when(this.studyEntryService).countStudyEntries(studyId);

		try {
			final String output =
				this.exportStudyEntriesController.exportStudyEntries(form, ExportStudyEntriesControllerTest.CSV_TYPE, this.response);

			//  Verify temporary file is created in proper directory and response object is properly set
			final ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);
			Mockito.verify(this.exportStudyEntriesService, Mockito.times(1)).exportAsCSVFile(ArgumentMatchers.eq(studyId), filenameCaptor.capture(),
					ArgumentMatchers.any(Map.class));
			final File outputFile = this.getOutputFilePath();
			Assert.assertNotNull(outputFile);
			Assert.assertEquals(outputFile.getAbsolutePath(), filenameCaptor.getValue());
			final Map<String, Object> result = new ObjectMapper().readValue(output, Map.class);
			Assert.assertEquals(outputFile.getAbsolutePath(), result.get(ExportStudyEntriesController.OUTPUT_FILENAME));
			Assert.assertTrue(FileNameGenerator.isValidFileNameFormat(result.get(ExportStudyEntriesController.FILENAME).toString(), FileNameGenerator.CSV_DATE_TIME_PATTERN));
			Mockito.verify(this.response).setContentType(FileUtils.MIME_CSV);
		} catch (final GermplasmListExporterException e) {
			Assert.fail();
		}

	}

	@Test
	public void testExportGermplasmListCSVForStudy_NoSelectedGermplasmList() {

		try {
			Mockito.doReturn(null).when(this.fieldbookMiddlewareService).getGermplasmListById(ArgumentMatchers.anyInt());
		} catch (final MiddlewareQueryException e1) {
			Assert.fail();
		}
		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		final int studyId = new Random().nextInt();
		studyDetails.setId(studyId);
		workbook.setStudyDetails(studyDetails);
		Mockito.doReturn(workbook).when(this.userSelection).getWorkbook();
		final ExportGermplasmListForm form = new ExportGermplasmListForm();
		form.setGermplasmListVisibleColumns("0");

		try {
			this.exportStudyEntriesController.exportStudyEntries(form, ExportStudyEntriesControllerTest.CSV_TYPE, this.response);
			Mockito.verify(this.exportStudyEntriesService, Mockito.times(0)).exportAsCSVFile(ArgumentMatchers.eq(studyId), ArgumentMatchers.anyString(),
					ArgumentMatchers.any(Map.class));
		} catch (final GermplasmListExporterException e) {
			Assert.fail();
		}

	}

	@Test
	public void test_getVisibleColumnsMapStudy() {

		final String[] termIds = new String[] {String.valueOf(TermId.ENTRY_TYPE.getId())};
		final Map<String, Boolean> result = this.exportStudyEntriesController.getVisibleColumnsMap(termIds);

		Assert.assertTrue(result.get(String.valueOf(TermId.GID.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.DESIG.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.ENTRY_NO.getId())));

		Assert.assertTrue(result.get(String.valueOf(TermId.ENTRY_TYPE.getId())));

		Assert.assertFalse(result.get(String.valueOf(TermId.ENTRY_CODE.getId())));
		Assert.assertFalse(result.get(String.valueOf(TermId.CROSS.getId())));
		Assert.assertFalse(result.get(String.valueOf(TermId.ENTRY_NUMBER_STORAGE.getId())));

	}

	@Test
	public void test_getVisibleColumnsMapNurseryStudy() {

		final String[] termIds = new String[] {"0"};
		Mockito.when(this.userSelection.getPlotsLevelList()).thenReturn(WorkbookDataUtil.getPlotLevelList());

		final Map<String, Boolean> result = this.exportStudyEntriesController.getVisibleColumnsMap(termIds);

		Assert.assertTrue(result.get(String.valueOf(TermId.GID.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.DESIG.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.ENTRY_NO.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.CROSS.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.ENTRY_CODE.getId())));
		Assert.assertNull(result.get(String.valueOf(TermId.ENTRY_NUMBER_STORAGE.getId())));
		Assert.assertNull(result.get(String.valueOf(TermId.ENTRY_TYPE.getId())));
	}

	@Test
	public void test_getVisibleColumnsMapWithNoVisibleColumns() {

		final String[] termIds = new String[] {"0"};

		final Map<String, Boolean> result = this.exportStudyEntriesController.getVisibleColumnsMap(termIds);

		Assert.assertTrue(result.get(String.valueOf(TermId.GID.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.DESIG.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.ENTRY_NO.getId())));

		Assert.assertTrue(result.get(String.valueOf(TermId.ENTRY_CODE.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.CROSS.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.ENTRY_NUMBER_STORAGE.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.ENTRY_TYPE.getId())));
	}

	private List<SettingDetail> getPlotLevelList() {
		final List<SettingDetail> plotLevelList = new ArrayList<>();

		for (final Entry<String, Boolean> entry : this.getVisibleColumnMap().entrySet()) {
			plotLevelList.add(this.generateSettingDetail(Integer.valueOf(entry.getKey())));
		}

		return plotLevelList;

	}

	private Map<String, Boolean> getVisibleColumnMap() {
		final Map<String, Boolean> visibleColumnMap = new HashMap<String, Boolean>();

		visibleColumnMap.put(String.valueOf(TermId.GID.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.CROSS.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.ENTRY_NO.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.DESIG.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.ENTRY_CODE.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.ENTRY_NUMBER_STORAGE.getId()), false);
		visibleColumnMap.put(String.valueOf(TermId.ENTRY_TYPE.getId()), false);

		return visibleColumnMap;

	}

	private SettingDetail generateSettingDetail(final Integer termId) {
		final SettingDetail settingDetail = new SettingDetail();
		settingDetail.setHidden(false);
		final SettingVariable var = new SettingVariable();
		var.setCvTermId(termId);
		settingDetail.setVariable(var);

		final StandardVariable stdVar;
		try {
			stdVar = this.ontologyService.getStandardVariable(termId, this.contextUtil.getCurrentProgramUUID());

			settingDetail.getVariable().setName(stdVar.getName());
			settingDetail.getVariable().setDescription(stdVar.getDescription());
			settingDetail.getVariable().setProperty(stdVar.getProperty().getName());
			settingDetail.getVariable().setScale(stdVar.getScale().getName());
			settingDetail.getVariable().setMethod(stdVar.getMethod().getName());
			settingDetail.getVariable().setDataType(stdVar.getDataType().getName());

		} catch (final MiddlewareException e) {
			// do nothing
		}

		return settingDetail;
	}

	private StandardVariable createStandardVariable(final int id, final String name) {
		final StandardVariable stdVar = new StandardVariable();
		stdVar.setId(id);
		stdVar.setName(name);
		stdVar.setDescription(ExportStudyEntriesControllerTest.TEST_DESCRIPTION);

		final Term prop = new Term();
		prop.setName(ExportStudyEntriesControllerTest.TEST_PROPERTY);
		stdVar.setProperty(prop);

		final Term scale = new Term();
		scale.setName(ExportStudyEntriesControllerTest.TEST_SCALE);
		stdVar.setScale(scale);

		final Term method = new Term();
		method.setName(ExportStudyEntriesControllerTest.TEST_METHOD);
		stdVar.setMethod(method);

		final Term dataType = new Term();
		dataType.setName(ExportStudyEntriesControllerTest.NUMERIC_VARIABLE);
		stdVar.setDataType(dataType);

		return stdVar;
	}

	private GermplasmList getGermplasmList() {
		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setName(ExportStudyEntriesControllerTest.SAMPLE_LIST);
		germplasmList.setUserId(ExportStudyEntriesControllerTest.CURRENT_USER_ID);
		germplasmList.setDescription(ExportStudyEntriesControllerTest.SAMPLE_DESCRIPTION);
		germplasmList.setType(ExportStudyEntriesControllerTest.LST);
		germplasmList.setDate(ExportStudyEntriesControllerTest.LIST_DATE);
		germplasmList.setNotes(ExportStudyEntriesControllerTest.SAMPLE_NOTES);
		germplasmList.setStatus(ExportStudyEntriesControllerTest.LIST_STATUS);

		return germplasmList;
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
