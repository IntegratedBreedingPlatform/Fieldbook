package com.efficio.fieldbook.web.importdesign.controller;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.SettingsService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.GenerateDesignInput;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.common.form.ImportDesignForm;
import com.efficio.fieldbook.web.data.initializer.DesignImportTestDataInitializer;
import com.efficio.fieldbook.web.data.initializer.SettingDetailTestDataInitializer;
import com.efficio.fieldbook.web.importdesign.service.impl.DesignImportServiceImpl;
import com.efficio.fieldbook.web.importdesign.validator.DesignImportValidator;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.Instance;
import com.efficio.fieldbook.web.trial.bean.InstanceInfo;
import com.efficio.fieldbook.web.trial.bean.TrialSettingsBean;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import com.efficio.fieldbook.web.util.parsing.DesignImportParser;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.workbench.Project;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by cyrus on 5/28/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class DesignImportControllerTest {

	private static final int GW_100G_TERMID = 9999;

	public static final String TEST_IMPORT_FILE_NAME_CSV = "Test_import_file_name.csv";
	public static final String LOCATION_NAME = "LOCATION_NAME";
	public static final String LOCATION_ID = "LOCATION_ID";
	public static final String SITE_NAME = "SITE_NAME";
	public static final String COOPERATOR_NAME = "COOPERATOR";
	public static final String COOPERATOR_ID = "COOPERATOR_ID";

	@Mock
	private HttpServletRequest httpRequest;

	@Mock
	private HttpSession httpSession;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@Mock
	protected FieldbookService fieldbookService;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private MockMultipartFile multiPartFile;

	@Mock
	private UserSelection userSelection;

	@Mock
	private MessageSource messageSource;

	@Mock
	private SettingsService settingsService;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	private DesignImportServiceImpl designImportService;

	@Mock
	private DesignImportParser designImportParser;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private DesignImportValidator designImportValidator;

	@Mock
	protected StudyDataManager studyDataManager;

	@InjectMocks
	private DesignImportController designImportController;

	private final Project project = this.createProject();

	@Before
	public void init() throws Exception {

		final DesignImportData data = this.createDesignImportDataWithCooperatorVariable();

		Mockito.when(this.multiPartFile.getOriginalFilename()).thenReturn(DesignImportControllerTest.TEST_IMPORT_FILE_NAME_CSV);
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(this.project);
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(this.project.getUniqueID());
		Mockito.when(this.contextUtil.getCurrentWorkbenchUserId()).thenReturn(1);
		Mockito.when(this.designImportParser.parseFile(DesignImportParser.FILE_TYPE_CSV, this.multiPartFile)).thenReturn(data);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.COOPERATOOR_ID.getId()))
				.thenReturn(new Term(TermId.COOPERATOOR_ID.getId(), DesignImportControllerTest.COOPERATOR_ID, ""));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.COOPERATOR.getId()))
				.thenReturn(new Term(TermId.COOPERATOR.getId(), DesignImportControllerTest.COOPERATOR_NAME, ""));

		Mockito.doReturn(data).when(this.userSelection).getDesignImportData();

		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(this.project);
		Mockito.when(this.userSelection.getExpDesignParams()).thenReturn(new ExpDesignParameterUi());
		this.initializeOntologyData();
		this.initializeDesignImportService();
		this.initializeSettingServiceForChecks();

	}

	@Test
	public void testValidateAndSaveNewMapping() throws Exception {
		final Map<String, Object> results = this.verifyMapDesignImportData();

		Assert.assertTrue((Boolean) results.get("success"));
		Assert.assertFalse((Boolean) results.get("hasConflict"));
	}

	@Test
	public void testValidateAndSaveNewMappingWithExistingDesign() throws Exception {
		final Map<String, Object> results = this.verifyMapDesignImportData();

		// lets set a design here
		final Workbook workbook = Mockito.mock(Workbook.class);

		Assert.assertTrue((Boolean) results.get("success"));
		Assert.assertFalse((Boolean) results.get("hasExistingDesign"));
	}

	@Test
	public void testValidateAndSaveNewMappingWithExistingWorkbook() throws Exception {

		final List<MeasurementVariable> workbookMeasurementVariables = new ArrayList<>();
		final Set<MeasurementVariable> designFileMeasurementVariables = new HashSet<>();

		workbookMeasurementVariables.add(this
				.createMeasurementVariable(TermId.SITE_NAME.getId(), DesignImportControllerTest.SITE_NAME, "Location", "Text", "Assigned",
						"TRIAL"));
		workbookMeasurementVariables.add(this
				.createMeasurementVariable(TermId.TRIAL_LOCATION.getId(), DesignImportControllerTest.LOCATION_NAME, "Location", "DBCV",
						"Assigned", "TRIAL"));
		designFileMeasurementVariables.add(this
				.createMeasurementVariable(TermId.SITE_NAME.getId(), DesignImportControllerTest.SITE_NAME, "Location", "Text", "Assigned",
						"TRIAL"));
		designFileMeasurementVariables
				.add(this.createMeasurementVariable(TermId.PI_NAME.getId(), "PI_NAME", "Person", "DBCV", "Assigned", "TRIAL"));

		final Workbook workbook = Mockito.mock(Workbook.class);
		Mockito.when(workbook.getMeasurementDatasetVariables()).thenReturn(workbookMeasurementVariables);
		Mockito.doReturn(designFileMeasurementVariables).when(this.designImportService)
				.getMeasurementVariablesFromDataFile(ArgumentMatchers.<Workbook>isNull(), Matchers.any(DesignImportData.class));
		Mockito.when(this.designImportService.areTrialInstancesMatchTheSelectedEnvironments(3, this.userSelection.getDesignImportData()))
				.thenReturn(true);
		Mockito.when(this.userSelection.getWorkbook()).thenReturn(workbook);

		final Map<String, Object> results = this.verifyMapDesignImportData();

		Assert.assertTrue((Boolean) results.get("success"));
		Assert.assertTrue((Boolean) results.get("hasConflict"));
	}

	@Test
	public void testValidateAndSaveNewMappingWithWarning() throws Exception {

		Mockito.doReturn(false).when(this.designImportService)
				.areTrialInstancesMatchTheSelectedEnvironments(Matchers.anyInt(), Matchers.any(DesignImportData.class));

		Mockito.when(this.messageSource.getMessage("design.import.warning.trial.instances.donotmatch", null, Locale.ENGLISH))
				.thenReturn("WARNING_MSG");

		final Map<String, Object> results = this.designImportController.validateAndSaveNewMapping(this.createTestMappedHeaders(), 3);

		Mockito.verify(this.designImportValidator).validateDesignData(this.userSelection.getDesignImportData());

		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = this.userSelection.getDesignImportData().getMappedHeaders();

		Assert.assertEquals(1, mappedHeaders.get(PhenotypicType.TRIAL_ENVIRONMENT).size());
		Assert.assertEquals(0, mappedHeaders.get(PhenotypicType.GERMPLASM).size());
		Assert.assertEquals(0, mappedHeaders.get(PhenotypicType.TRIAL_DESIGN).size());
		Assert.assertEquals(0, mappedHeaders.get(PhenotypicType.VARIATE).size());

		final DesignHeaderItem designHeaderItem = mappedHeaders.get(PhenotypicType.TRIAL_ENVIRONMENT).get(0);
		Assert.assertEquals("The DesignHeaderItem SITE_NAME should be mapped to the SITE_NAME Standard Variable", TermId.SITE_NAME.getId(),
				designHeaderItem.getVariable().getId());

		assert (Boolean) results.get("success");
		Assert.assertEquals("returns a warning message", "WARNING_MSG", results.get("warning"));

	}

	@Test
	public void testValidateAndSaveNewMappingWithException() throws Exception {

		Mockito.doThrow(new DesignValidationException("DesignValidationException thrown")).when(this.designImportValidator)
				.validateDesignData(Matchers.any(DesignImportData.class));

		final Map<String, Object> results = this.designImportController.validateAndSaveNewMapping(this.createTestMappedHeaders(), 3);

		this.userSelection.getDesignImportData().getMappedHeaders();

		assert !(Boolean) results.get("success");
		Assert.assertEquals("returns a error message", "DesignValidationException thrown", results.get("error"));
		Assert.assertEquals("error = message", results.get("error"), results.get("message"));
	}

	@Test
	public void testPerformAutomap() throws Exception {

		final Map<PhenotypicType, List<DesignHeaderItem>> result = new HashMap<>();
		Mockito.doReturn(result).when(this.designImportService).categorizeHeadersByPhenotype(ArgumentMatchers.<List<DesignHeaderItem>>any());

		this.designImportController.performAutomap(this.userSelection.getDesignImportData());

		// Verify that the categorizeHeadersByPhenotype is called, which is
		// actually the method that automatically maps headers to standard
		// variables
		Mockito.verify(this.designImportService).categorizeHeadersByPhenotype(ArgumentMatchers.<List<DesignHeaderItem>>any());
	}

	@Test
	public void testImportFile() throws Exception {

		final ImportDesignForm form = new ImportDesignForm();
		form.setFile(this.multiPartFile);
		form.setFileType(DesignImportParser.FILE_TYPE_CSV);
		final Workbook workbook = Mockito.mock(Workbook.class);
		final StudyDetails studyDetails = Mockito.mock(StudyDetails.class);
		Mockito.when(this.userSelection.getWorkbook()).thenReturn(workbook);
		Mockito.when(workbook.getStudyDetails()).thenReturn(studyDetails);
		final Workbook workbookReloaded = WorkbookTestDataInitializer.getTestWorkbook(1, StudyTypeDto.getTrialDto());
		Mockito.when(fieldbookMiddlewareService.getStudyDataSet(this.userSelection.getWorkbook().getStudyDetails().getId()))
			.thenReturn(workbookReloaded);
		final String resultsMap = this.designImportController.importFile(form);

		Mockito.verify(this.userSelection).setDesignImportData(Matchers.any(DesignImportData.class));

		// verify we store the filename to design import data
		Assert.assertEquals("", DesignImportControllerTest.TEST_IMPORT_FILE_NAME_CSV,
				this.userSelection.getDesignImportData().getImportFileName());

		Assert.assertTrue(resultsMap.contains("{\"isSuccess\":1}"));
	}

	@Test
	public void testImportFileFail() throws Exception {

		final ImportDesignForm form = new ImportDesignForm();
		form.setFile(this.multiPartFile);
		form.setFileType(DesignImportParser.FILE_TYPE_CSV);
		Mockito.when(this.designImportParser.parseFile(DesignImportParser.FILE_TYPE_CSV, this.multiPartFile))
				.thenThrow(new FileParsingException("force file parse exception"));
		final Workbook workbook = Mockito.mock(Workbook.class);
		final StudyDetails studyDetails = Mockito.mock(StudyDetails.class);
		Mockito.when(this.userSelection.getWorkbook()).thenReturn(workbook);
		Mockito.when(workbook.getStudyDetails()).thenReturn(studyDetails);
		final Workbook workbookReloaded = WorkbookTestDataInitializer.getTestWorkbook(1, StudyTypeDto.getTrialDto());
		Mockito.when(fieldbookMiddlewareService.getStudyDataSet(this.userSelection.getWorkbook().getStudyDetails().getId()))
			.thenReturn(workbookReloaded);

		final String resultsMap = this.designImportController.importFile(form);

		Assert.assertTrue(resultsMap.contains("{\"error\":[\"force file parse exception\"],\"isSuccess\":0}"));
	}

	@Test
	public void testGetMappingData() throws Exception {
		final Set<String> mappingTypes = this.designImportController.getMappingData().keySet();

		assert mappingTypes.contains("unmappedHeaders");
		assert mappingTypes.contains("mappedEnvironmentalFactors");
		assert mappingTypes.contains("mappedDesignFactors");
		assert mappingTypes.contains("mappedGermplasmFactors");
		assert mappingTypes.contains("mappedTraits");
	}

	@Test
	public void testShowDetails() throws Exception {
		final Workbook workbook = Mockito.mock(Workbook.class);

		Mockito.when(this.userSelection.getTemporaryWorkbook()).thenReturn(workbook);

		Mockito.doReturn(Mockito.mock(Set.class)).when(this.designImportService)
				.getDesignMeasurementVariables(Matchers.any(Workbook.class), Matchers.any(DesignImportData.class), Matchers.anyBoolean());

		final Model model = Mockito.mock(Model.class);

		final String html = this.designImportController.showDetails(model);

		Mockito.verify(model).addAttribute(Matchers.eq("measurementVariables"), Matchers.any(Set.class));

		assert DesignImportController.REVIEW_DETAILS_PAGINATION_TEMPLATE.equals(html);
	}

	@Test
	public void testShowDetailsData() throws DesignValidationException {

		final Model model = Mockito.mock(Model.class);
		final ImportDesignForm form = Mockito.mock(ImportDesignForm.class);
		final InstanceInfo instanceInfo = Mockito.mock(InstanceInfo.class);
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(5, 1);

		Mockito.when(this.userSelection.getTemporaryWorkbook()).thenReturn(workbook);
		Mockito.doReturn(workbook.getObservations()).when(this.designImportService)
				.generateDesign(Matchers.any(Workbook.class), Matchers.any(DesignImportData.class), Matchers.any(InstanceInfo.class),
						Matchers.anyBoolean(), Matchers.anyMapOf(String.class, Integer.class));
		;

		final List<Map<String, Object>> result = this.designImportController.showDetailsData(instanceInfo, model, form);

		Assert.assertEquals(workbook.getObservations().size(), result.size());
	}

	@Test
	public void testShowDetailsDataGenerateDesignFailed() throws DesignValidationException {

		final Model model = Mockito.mock(Model.class);
		final ImportDesignForm form = Mockito.mock(ImportDesignForm.class);
		final InstanceInfo instanceInfo = Mockito.mock(InstanceInfo.class);
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(5, 1);

		Mockito.when(this.userSelection.getTemporaryWorkbook()).thenReturn(workbook);
		Mockito.doThrow(new DesignValidationException("")).when(this.designImportService)
				.generateDesign(Matchers.any(Workbook.class), Matchers.any(DesignImportData.class), Matchers.any(InstanceInfo.class),
						Matchers.anyBoolean(), Matchers.anyMapOf(String.class, Integer.class));
		;

		final List<Map<String, Object>> result = this.designImportController.showDetailsData(instanceInfo, model, form);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testUpdateDesignMapping() {

		this.designImportController.updateDesignMapping(this.createMappedHeaders());

		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = this.userSelection.getDesignImportData().getMappedHeaders();

		Assert.assertEquals(1, mappedHeaders.get(PhenotypicType.TRIAL_ENVIRONMENT).size());
		Assert.assertEquals(1, mappedHeaders.get(PhenotypicType.GERMPLASM).size());
		Assert.assertEquals(1, mappedHeaders.get(PhenotypicType.TRIAL_DESIGN).size());
		Assert.assertEquals(1, mappedHeaders.get(PhenotypicType.VARIATE).size());

	}

	@Test
	public void testResolveIDNamePairingAndValuesForStudy() {

		final Set<MeasurementVariable> measurementVariables = new HashSet<>();

		final DesignImportData designImportData = this.createDesignImportDataWithCooperatorVariable();
		final InstanceInfo instanceInfo = this.createEnvironmentData(1);

		Mockito.doReturn(this.createSettingDetails()).when(this.userSelection).getTrialLevelVariableList();
		this.designImportController.resolveIDNamePairingAndValuesForTrial(instanceInfo, designImportData, measurementVariables);

		Assert.assertEquals(6, measurementVariables.size());
		Assert.assertEquals("LOCATION_ID should be added to the Study Variables", "LOCATION_ID",
				Objects.requireNonNull(this.getMeasurementVariable(TermId.LOCATION_ID.getId(), measurementVariables)).getName());
		Assert.assertEquals("COOPERATOR_ID should be added to the Study Variables", "COOPERATOR_ID",
				Objects.requireNonNull(this.getMeasurementVariable(TermId.COOPERATOOR_ID.getId(), measurementVariables)).getName());
		Assert.assertEquals("PI_NAME should be added to the Study Variables", "PI_NAME",
				Objects.requireNonNull(this.getMeasurementVariable(TermId.PI_NAME.getId(), measurementVariables)).getName());
		Assert.assertEquals("SITE_NAME should be added to the Study Variables", DesignImportControllerTest.SITE_NAME,
				Objects.requireNonNull(this.getMeasurementVariable(TermId.SITE_NAME.getId(), measurementVariables)).getName());
		Assert.assertEquals("TRIAL_INSTANCE should be added to the Study Variables", "TRIAL_INSTANCE",
				Objects.requireNonNull(this.getMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), measurementVariables)).getName());

		Assert.assertEquals(1, instanceInfo.getNumberOfInstances());
		final Map<String, String> managementDetailValuesMap = instanceInfo.getInstances().get(0).getManagementDetailValues();

		Assert.assertTrue("LOCATION_ID should be in Management Details",
				managementDetailValuesMap.containsKey(String.valueOf(TermId.LOCATION_ID.getId())));
		Assert.assertTrue("COOPERATOOR_ID should be in Management Details",
				managementDetailValuesMap.containsKey(String.valueOf(TermId.COOPERATOOR_ID.getId())));
		Assert.assertTrue("PI_NAME should be in Management Details",
				managementDetailValuesMap.containsKey(String.valueOf(TermId.PI_NAME.getId())));
		Assert.assertTrue("SITE_NAME should be in Management Details",
				managementDetailValuesMap.containsKey(String.valueOf(TermId.SITE_NAME.getId())));
		Assert.assertTrue("TRIAL_INSTANCE_FACTOR should be in Management Details",
				managementDetailValuesMap.containsKey(String.valueOf(TermId.TRIAL_INSTANCE_FACTOR.getId())));

	}

	@Test
	public void testResolveIDNamePairingAndValuesForStudyLocationVariableDoesNotExistInStudyLevelVariables() {

		final Set<MeasurementVariable> measurementVariables = new HashSet<>();

		final DesignImportData designImportData = this.createDesignImportDataWithCooperatorVariable();
		final InstanceInfo instanceInfo = this.createEnvironmentData(1);

		final List<SettingDetail> settingDetails = new ArrayList<SettingDetail>();

		settingDetails.add(SettingDetailTestDataInitializer
				.createSettingDetail(TermId.LOCATION_ID.getId(), DesignImportControllerTest.LOCATION_ID, "", "TRIAL"));

		Mockito.doReturn(settingDetails).when(this.userSelection).getTrialLevelVariableList();
		Mockito.when(this.ontologyDataManager.getTermById(TermId.TRIAL_LOCATION.getId()))
				.thenReturn(new Term(TermId.TRIAL_LOCATION.getId(), DesignImportControllerTest.LOCATION_NAME, ""));

		this.designImportController.resolveIDNamePairingAndValuesForTrial(instanceInfo, designImportData, measurementVariables);

		Assert.assertEquals(6, measurementVariables.size());

		Assert.assertEquals("LOCATION_NAME should be added to the Study Variables", "LOCATION_NAME",
				Objects.requireNonNull(this.getMeasurementVariable(TermId.TRIAL_LOCATION.getId(), measurementVariables)).getName());
		Assert.assertEquals("LOCATION_ID should be added to the Study Variables", "LOCATION_ID",
				Objects.requireNonNull(this.getMeasurementVariable(TermId.LOCATION_ID.getId(), measurementVariables)).getName());

		Assert.assertEquals(1, instanceInfo.getNumberOfInstances());
		final Map<String, String> managementDetailValuesMap = instanceInfo.getInstances().get(0).getManagementDetailValues();

		Assert.assertTrue("LOCATION_NAME should be in Management Details",
				managementDetailValuesMap.containsKey(String.valueOf(TermId.TRIAL_LOCATION.getId())));
		Assert.assertTrue("LOCATION_NAME_ID should be in Management Details",
				managementDetailValuesMap.containsKey(String.valueOf(TermId.LOCATION_ID.getId())));

	}

	@Test
	public void testResolveIDNamePairingAndValuesForNursery() {

		Mockito.mock(Workbook.class);
		final List<SettingDetail> newDetails = new ArrayList<>();

		final InstanceInfo instanceInfo = this.createEnvironmentData(1);
		final DesignImportData designImportData = DesignImportTestDataInitializer.createDesignImportData(1, 1);

		this.designImportController.resolveIDNamePairingAndValuesForNursery(instanceInfo, designImportData, newDetails);

		Assert.assertEquals(2, newDetails.size());
		Assert.assertEquals("COOPERATOR_ID should be added to the Trial Variables", "COOPERATOR_ID",
				this.getSettingDetail(TermId.COOPERATOOR_ID.getId(), newDetails).getVariable().getName());
		Assert.assertEquals("PI_NAME_ID should be added to the Trial Variables", "PI_NAME_ID",
				this.getSettingDetail(TermId.PI_ID.getId(), newDetails).getVariable().getName());
		Assert.assertEquals(1, instanceInfo.getNumberOfInstances());
		final Map<String, String> managementDetailValuesMap = instanceInfo.getInstances().get(0).getManagementDetailValues();

		Assert.assertTrue("COOPERATOOR_ID should be in Management Details",
				managementDetailValuesMap.containsKey(String.valueOf(TermId.COOPERATOOR_ID.getId())));
		Assert.assertTrue("PI_ID should be in Management Details",
				managementDetailValuesMap.containsKey(String.valueOf(TermId.PI_ID.getId())));
		Assert.assertTrue("SITE_NAME should be in Management Details",
				managementDetailValuesMap.containsKey(String.valueOf(TermId.SITE_NAME.getId())));
		Assert.assertTrue("TRIAL_INSTANCE_FACTOR should be in Management Details",
				managementDetailValuesMap.containsKey(String.valueOf(TermId.TRIAL_INSTANCE_FACTOR.getId())));

	}

	@Test
	public void testAddFactorsIfNecessary() {

		final Set<MeasurementVariable> measurementVariables = new HashSet<>();
		measurementVariables.add(this.createMeasurementVariable(TermId.ROW.getId(), "ROW", "ENTRY"));
		measurementVariables.add(this.createMeasurementVariable(TermId.COL.getId(), "COL", "ENTRY"));

		Mockito.doReturn(measurementVariables).when(this.designImportService)
				.extractMeasurementVariable(Matchers.any(PhenotypicType.class), ArgumentMatchers.<Map<PhenotypicType, List<DesignHeaderItem>>>any());

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(5, 1);

		final DesignImportData data = DesignImportTestDataInitializer.createDesignImportData(1, 1);

		this.designImportController.addFactorsIfNecessary(workbook, data);

		Assert.assertEquals(10, workbook.getFactors().size());
		Assert.assertEquals("ROW should be added to the Factors since it isn't in the list", "ROW", Objects.requireNonNull(
				this.getMeasurementVariable(TermId.ROW.getId(), new HashSet<MeasurementVariable>(workbook.getFactors()))).getName());
		Assert.assertEquals("COL should be added to the Factors since it isn't in the list", "COL", Objects.requireNonNull(
				this.getMeasurementVariable(TermId.COL.getId(), new HashSet<MeasurementVariable>(workbook.getFactors()))).getName());

	}

	@Test
	public void testAddFactorsIfNecessaryVariablesToAddAlreadyExist() throws URISyntaxException, FileParsingException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(5, 1);

		final Set<MeasurementVariable> measurementVariables = new HashSet<>();
		measurementVariables.add(this.getMeasurementVariable(TermId.ENTRY_NO.getId(), new HashSet<>(workbook.getFactors())));
		measurementVariables.add(this.getMeasurementVariable(TermId.GID.getId(), new HashSet<>(workbook.getFactors())));

		Mockito.doReturn(measurementVariables).when(this.designImportService)
				.extractMeasurementVariable(Matchers.any(PhenotypicType.class), ArgumentMatchers.<Map<PhenotypicType, List<DesignHeaderItem>>>any());

		final DesignImportData data = DesignImportTestDataInitializer.createDesignImportData(1, 1);

		this.designImportController.addFactorsIfNecessary(workbook, data);

		Assert.assertEquals("ENTRY_NO and GID should not added to the Factors, so the size of Factor must remain 8", 8,
				workbook.getFactors().size());

	}

	@Test
	public void testAddConditionsIfNecessaryVariablesToAddAlreadyExist() throws URISyntaxException, FileParsingException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(5, StudyTypeDto.getNurseryDto());
		final int originalConditionsSize = workbook.getConditions().size();

		final Set<MeasurementVariable> measurementVariables = new HashSet<>();
		measurementVariables.add(this.getMeasurementVariable(TermId.TRIAL_LOCATION.getId(), new HashSet<>(workbook.getConditions())));

		Mockito.doReturn(measurementVariables).when(this.designImportService)
				.extractMeasurementVariable(Matchers.any(PhenotypicType.class), ArgumentMatchers.<Map<PhenotypicType, List<DesignHeaderItem>>>any());

		final DesignImportData data = DesignImportTestDataInitializer.createDesignImportData(1, 1);
		this.designImportController.addConditionsIfNecessary(workbook, data);

		Assert.assertEquals(
				"LOCATION_NAME should not added to the Conditions, so the size of Conditions must remain " + originalConditionsSize,
				originalConditionsSize, workbook.getConditions().size());

	}

	@Test
	public void testAddConditionsIfNecessary() throws URISyntaxException, FileParsingException {

		final Set<MeasurementVariable> measurementVariables = new HashSet<>();
		measurementVariables.add(this.createMeasurementVariable(TermId.SITE_NAME.getId(), "SITENAME", "TRIAL"));

		Mockito.doReturn(measurementVariables).when(this.designImportService)
				.extractMeasurementVariable(Matchers.any(PhenotypicType.class), ArgumentMatchers.<Map<PhenotypicType, List<DesignHeaderItem>>>any());

		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(5, StudyTypeDto.getNurseryDto());
		final int originalConditionsSize = workbook.getConditions().size();

		final DesignImportData data = DesignImportTestDataInitializer.createDesignImportData(1, 1);
		this.designImportController.addConditionsIfNecessary(workbook, data);

		// Add 1 to the originalConditionsSize to include the SITE_NAME in the
		// count.
		Assert.assertEquals(originalConditionsSize + 1, workbook.getConditions().size());
		Assert.assertEquals("SITENAME should be added to the Conditions since it isn't in the list", "SITENAME", Objects.requireNonNull(
				this.getMeasurementVariable(TermId.SITE_NAME.getId(), new HashSet<MeasurementVariable>(workbook.getConditions())))
				.getName());

	}

	@Test
	public void testPopulateStudyLevelVariableListIfNecessary() {

		final Project project = this.createProject();
		final MeasurementVariable siteName =
				this.createMeasurementVariable(TermId.SITE_NAME.getId(), DesignImportControllerTest.SITE_NAME, "TRIAL");
		final SettingDetail siteNameSettingDetail = SettingDetailTestDataInitializer
				.createSettingDetail(TermId.SITE_NAME.getId(), DesignImportControllerTest.SITE_NAME, "", "TRIAL");

		final List<SettingDetail> settingDetails = new ArrayList<>();
		Mockito.doReturn(settingDetails).when(this.userSelection).getTrialLevelVariableList();

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(5, 3);

		// Add study environment to Factors for testing
		workbook.getFactors().add(siteName);

		this.designImportController.populateTrialLevelVariableListIfNecessary(workbook);

		final SettingDetail settingDetail = settingDetails.get(0);

		// SITE_NAME should be added to setting details passed to
		// addNewSettingDetailsIfNecessary()
		Assert.assertEquals(1, settingDetails.size());
		Assert.assertEquals(TermId.SITE_NAME.getId(), settingDetail.getVariable().getCvTermId().intValue());
		Assert.assertEquals(DesignImportControllerTest.SITE_NAME, settingDetail.getVariable().getName());
		Assert.assertTrue("Newly added setting detail must always be deletable", settingDetail.isDeletable());

	}

	@Test
	public void testPopulateLevelVariableListIfNecessary() throws URISyntaxException, FileParsingException {

		final Project project = this.createProject();
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(5, StudyTypeDto.getNurseryDto());
		final InstanceInfo instanceInfo = this.createEnvironmentData(1);
		final DesignImportData designImportData = DesignImportTestDataInitializer.createDesignImportData(1, 1);

		final MeasurementVariable siteName =
				this.createMeasurementVariable(TermId.SITE_NAME.getId(), DesignImportControllerTest.SITE_NAME, "TRIAL");
		final SettingDetail siteNameSettingDetail = SettingDetailTestDataInitializer
				.createSettingDetail(TermId.SITE_NAME.getId(), DesignImportControllerTest.SITE_NAME, "", "TRIAL");
		final MeasurementVariable piName = this.createMeasurementVariable(TermId.PI_NAME.getId(), "PI_NAME", "TRIAL");
		final SettingDetail piNameSettingDetail =
				SettingDetailTestDataInitializer.createSettingDetail(TermId.PI_NAME.getId(), "PI_NAME", "", "TRIAL");

		workbook.getConditions().clear();
		workbook.getConditions().add(siteName);
		workbook.getConditions().add(piName);

		final List<SettingDetail> newSettingDetails = new ArrayList<>();

		Mockito.doReturn(newSettingDetails).when(this.userSelection).getStudyLevelConditions();

		this.designImportController.populateStudyLevelVariableListIfNecessary(workbook, instanceInfo, designImportData);

		Assert.assertEquals(4, newSettingDetails.size());

		final SettingDetail settingDetail = this.getSettingDetail(TermId.SITE_NAME.getId(), newSettingDetails);

		Assert.assertEquals(siteName.getTermId(), Objects.requireNonNull(settingDetail).getVariable().getCvTermId().intValue());
		Assert.assertEquals(siteName.getName(), settingDetail.getVariable().getName());
		Assert.assertEquals(siteName.getOperation(), settingDetail.getVariable().getOperation());
		Assert.assertEquals("Test Site", settingDetail.getValue());

		final SettingDetail settingDetail2 = this.getSettingDetail(TermId.PI_NAME.getId(), newSettingDetails);

		Assert.assertEquals(piName.getTermId(), Objects.requireNonNull(settingDetail2).getVariable().getCvTermId().intValue());
		Assert.assertEquals(piName.getName(), settingDetail2.getVariable().getName());
		Assert.assertEquals(piName.getOperation(), settingDetail2.getVariable().getOperation());
		Assert.assertEquals("", settingDetail2.getValue());

	}

	@Test
	public void testCheckTheDeletedSettingDetails() {

		final DesignImportData designImportData = DesignImportTestDataInitializer.createDesignImportData(1, 1);
		final Set<MeasurementVariable> measurementVariables = this.createMeasurementVariables();
		final List<SettingDetail> deletedStudyLevelVariables = this.createDeletedStudyLevelVariables();
		final UserSelection selection = new UserSelection();

		selection.setTrialLevelVariableList(new ArrayList<SettingDetail>());
		selection.setDeletedTrialLevelVariables(deletedStudyLevelVariables);

		Mockito.doReturn(measurementVariables).when(this.designImportService)
				.getMeasurementVariablesFromDataFile(ArgumentMatchers.<Workbook>isNull(), Matchers.any(DesignImportData.class));

		this.designImportController.checkTheDeletedSettingDetails(selection, designImportData);

		// The deletedStudyLevelVariables tracks the list of variables that are
		// deleted by the user in the UI. So if the user re-imports a
		// design file which has variables that are already in the deleted list,
		// they should be removed in the deleted list.

		Assert.assertEquals(0, deletedStudyLevelVariables.size());
		Assert.assertEquals(3, selection.getTrialLevelVariableList().size());

		// Make sure variables that were in the deletedStudyLevelVariables list
		// are added in the StudyLevelVariableList
		final SettingDetail siteName = this.getSettingDetail(TermId.SITE_NAME.getId(), selection.getTrialLevelVariableList());
		Assert.assertNotNull(siteName);
		Assert.assertEquals(siteName.getVariable().getOperation(), Operation.UPDATE);

		final SettingDetail locatioName = this.getSettingDetail(TermId.TRIAL_LOCATION.getId(), selection.getTrialLevelVariableList());
		Assert.assertNotNull(locatioName);
		Assert.assertEquals(locatioName.getVariable().getOperation(), Operation.UPDATE);

		final SettingDetail locatioNameId = this.getSettingDetail(TermId.LOCATION_ID.getId(), selection.getTrialLevelVariableList());
		Assert.assertNotNull(locatioNameId);
		Assert.assertEquals(locatioNameId.getVariable().getOperation(), Operation.UPDATE);

	}

	@Test
	public void testGenerateMeasurements() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(5, 1);

		Mockito.doReturn(workbook).when(this.userSelection).getTemporaryWorkbook();
		Mockito.doReturn(workbook).when(this.userSelection).getWorkbook();
		final InstanceInfo instanceInfo = this.createEnvironmentData(1);
		final GenerateDesignInput input = new GenerateDesignInput(instanceInfo, ExperimentDesignType.CUSTOM_IMPORT, null, null, false);
		input.setTrialSettings(new TrialSettingsBean());
		final Map<String, Object> resultsMap = designImportController.generateMeasurements(input);

		Assert.assertEquals(1, resultsMap.get(DesignImportController.IS_SUCCESS));

	}

	@Test
	public void testGenerateMeasurementsFail() throws DesignValidationException {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(5, 1);

		Mockito.doReturn(workbook).when(this.userSelection).getTemporaryWorkbook();

		Mockito.doThrow(new DesignValidationException("")).when(this.designImportService)
				.generateDesign(Matchers.any(Workbook.class), Matchers.any(DesignImportData.class), Matchers.any(InstanceInfo.class),
						Matchers.anyBoolean(), Matchers.anyMapOf(String.class, Integer.class));

		final InstanceInfo instanceInfo = this.createEnvironmentData(1);
		final GenerateDesignInput input = new GenerateDesignInput(instanceInfo, ExperimentDesignType.CUSTOM_IMPORT, null, null, false);
		input.setTrialSettings(new TrialSettingsBean());
		final Map<String, Object> resultsMap = this.designImportController.generateMeasurements(input);

		Assert.assertEquals(0, resultsMap.get(DesignImportController.IS_SUCCESS));
		Assert.assertTrue(resultsMap.containsKey(DesignImportController.ERROR));

	}

	@Test
	public void testUpdateOperationWithTermIdHasMatchInList() {

		final List<SettingDetail> settingDetails = new ArrayList<>();
		final SettingDetail settingDetail = SettingDetailTestDataInitializer
				.createSettingDetail(TermId.SITE_NAME.getId(), DesignImportControllerTest.SITE_NAME, "", "TRIAL");
		settingDetails.add(settingDetail);

		this.designImportController.updateOperation(TermId.SITE_NAME.getId(), settingDetails, Operation.ADD);

		Assert.assertEquals(Operation.ADD, settingDetail.getVariable().getOperation());

	}

	@Test
	public void testUpdateOperationSuppliedTermIdNotInList() {

		final List<SettingDetail> settingDetails = new ArrayList<>();
		final SettingDetail settingDetail = SettingDetailTestDataInitializer
				.createSettingDetail(TermId.SITE_NAME.getId(), DesignImportControllerTest.SITE_NAME, "", "TRIAL");
		settingDetails.add(settingDetail);

		this.designImportController.updateOperation(TermId.BLOCK_ID.getId(), settingDetails, Operation.ADD);

		Assert.assertNull(settingDetail.getVariable().getOperation());

	}

	@Test
	public void testHasConflictTrue() {

		final Set<MeasurementVariable> setA = new HashSet<>();
		final Set<MeasurementVariable> setB = new HashSet<>();

		setA.add(this.createMeasurementVariable(TermId.SITE_NAME.getId(), DesignImportControllerTest.SITE_NAME, "Location", "Text",
				"Assigned", "TRIAL"));
		setA.add(this.createMeasurementVariable(TermId.TRIAL_LOCATION.getId(), DesignImportControllerTest.LOCATION_NAME, "Location", "DBCV",
				"Assigned", "TRIAL"));

		setB.add(this.createMeasurementVariable(TermId.SITE_NAME.getId(), DesignImportControllerTest.SITE_NAME, "Location", "Text",
				"Assigned", "TRIAL"));
		setB.add(this.createMeasurementVariable(TermId.PI_NAME.getId(), "PI_NAME", "Person", "DBCV", "Assigned", "TRIAL"));

		Assert.assertTrue(this.designImportController.hasConflict(setA, setB));

	}

	@Test
	public void testHasConflictFalse() {

		final Set<MeasurementVariable> setA = new HashSet<>();
		final Set<MeasurementVariable> setB = new HashSet<>();

		setA.add(this.createMeasurementVariable(TermId.SITE_NAME.getId(), DesignImportControllerTest.SITE_NAME, "Location", "Text",
				"Assigned", "TRIAL"));
		setA.add(this.createMeasurementVariable(TermId.TRIAL_LOCATION.getId(), DesignImportControllerTest.LOCATION_NAME, "Location", "DBCV",
				"Assigned", "TRIAL"));

		setB.add(this.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL_INSTANCE", "Trial instance", "Number",
				"Enumerated", "TRIAL"));
		setB.add(this.createMeasurementVariable(TermId.PI_NAME.getId(), "PI_NAME", "Person", "DBCV", "Assigned", "TRIAL"));

		Assert.assertFalse(this.designImportController.hasConflict(setA, setB));

	}

	@Test
	public void testGetLocalNameFromSettingDetailsWithMatch() {

		final List<SettingDetail> settingDetails = new ArrayList<>();
		settingDetails.add(SettingDetailTestDataInitializer.createSettingDetail(123, "FACTOR 1", "", "TRIAL"));
		settingDetails.add(SettingDetailTestDataInitializer.createSettingDetail(321, "FACTOR 2", "", "TRIAL"));

		final String result = this.designImportController.getVariableNameFromSettingDetails(123, settingDetails);

		Assert.assertEquals("FACTOR 1", result);
	}

	@Test
	public void testGetLocalNameFromSettingDetailsWithNoMatch() {

		final List<SettingDetail> settingDetails = new ArrayList<>();
		settingDetails.add(SettingDetailTestDataInitializer.createSettingDetail(123, "FACTOR 1", "", "TRIAL"));
		settingDetails.add(SettingDetailTestDataInitializer.createSettingDetail(321, "FACTOR 2", "", "TRIAL"));

		final String result = this.designImportController.getVariableNameFromSettingDetails(567, settingDetails);

		Assert.assertEquals("", result);
	}

	@Test
	public void testCreateStudyObservationsForStudy() {

		final DesignImportData designImportData = DesignImportTestDataInitializer.createDesignImportData(1, 1);
		final InstanceInfo instanceInfo = this.createEnvironmentData(1);
		final Workbook workbook = Mockito.spy(WorkbookDataUtil.getTestWorkbook(5, StudyTypeDto.getTrialDto()));

		this.designImportController.createTrialObservations(instanceInfo, workbook, designImportData);

		Mockito.verify(workbook).setTrialObservations(ArgumentMatchers.<List<MeasurementRow>>any());
		Mockito.verify(this.fieldbookService).addConditionsToTrialObservationsIfNecessary(Matchers.any(Workbook.class));

	}

	@Test
	public void testCancelImportDesign() {

		this.designImportController.cancelImportDesign();

		Mockito.verify(this.userSelection).setTemporaryWorkbook(null);
		Mockito.verify(this.userSelection).setDesignImportData(null);

	}

	@Test
	public void testAddCheckVariablesToDeleted() {

		final List<SettingDetail> studyLevelConditions = new ArrayList<>();

		this.designImportController.addCheckVariablesToDeleted(studyLevelConditions);

		Assert.assertEquals(3, studyLevelConditions.size());

		for (final SettingDetail checkSettingDetail : studyLevelConditions) {
			Assert.assertEquals(Operation.DELETE, checkSettingDetail.getVariable().getOperation());
			Assert.assertEquals(PhenotypicType.TRIAL_ENVIRONMENT, checkSettingDetail.getRole());
		}

	}

	protected void initializeSettingServiceForChecks() {

		final SettingDetail checkStart =
				SettingDetailTestDataInitializer.createSettingDetail(TermId.CHECK_START.getId(), "CHECK_START", "", "TRIAL");
		final SettingDetail checkInterval =
				SettingDetailTestDataInitializer.createSettingDetail(TermId.CHECK_INTERVAL.getId(), "CHECK_INTERVAL", "", "TRIAL");
		final SettingDetail checkPlan =
				SettingDetailTestDataInitializer.createSettingDetail(TermId.CHECK_PLAN.getId(), "CHECK_PLAN", "", "TRIAL");

		Mockito.when(this.settingsService
				.createSettingDetail(TermId.CHECK_START.getId(), "CHECK_START", this.userSelection, 1, this.project.getUniqueID()))
				.thenReturn(checkStart);
		Mockito.when(this.settingsService
				.createSettingDetail(TermId.CHECK_INTERVAL.getId(), "CHECK_INTERVAL", this.userSelection, 1, this.project.getUniqueID()))
				.thenReturn(checkInterval);
		Mockito.when(this.settingsService
				.createSettingDetail(TermId.CHECK_PLAN.getId(), "CHECK_PLAN", this.userSelection, 1, this.project.getUniqueID()))
				.thenReturn(checkPlan);
	}

	@Test
	public void testChangeDesignForNewStudyWithImportedDesign() {
		final Workbook study = WorkbookDataUtil.getTestWorkbook(10, StudyTypeDto.getTrialDto());
		study.getStudyDetails().setId(1);
		final List<MeasurementRow> observations = study.getObservations();

		Mockito.doReturn(study).when(this.userSelection).getWorkbook();

		DesignImportTestDataInitializer.updatePlotNoValue(observations);

		this.designImportController.changeDesign(study.getStudyDetails().getId());
		Mockito.verify(this.userSelection).getExpDesignParams();
		Mockito.verify(this.userSelection).setDesignImportData(null);
		this.assertIfDesignIsResetToDefault(observations);
	}

	@Test
	public void testChangeDesignForExistingStudyWithImportedDesign() {
		final Workbook study = WorkbookDataUtil.getTestWorkbook(10, StudyTypeDto.getTrialDto());
		study.getStudyDetails().setId(1);
		final List<MeasurementRow> observations = study.getObservations();

		Mockito.doReturn(study).when(this.userSelection).getTemporaryWorkbook();
		Mockito.doReturn(study).when(this.userSelection).getWorkbook();

		DesignImportTestDataInitializer.updatePlotNoValue(observations);

		this.designImportController.changeDesign(study.getStudyDetails().getId());
		Mockito.verify(this.userSelection).getExpDesignParams();
		Mockito.verify(this.userSelection).setDesignImportData(null);
		this.assertIfDesignIsResetToDefault(observations);
	}

	private void assertIfDesignIsResetToDefault(final List<MeasurementRow> observations) {
		for (final MeasurementRow row : observations) {
			final List<MeasurementData> dataList = row.getDataList();
			final MeasurementData entryNoData = WorkbookUtil.retrieveMeasurementDataFromMeasurementRow(TermId.ENTRY_NO.getId(), dataList);
			final MeasurementData plotNoData = WorkbookUtil.retrieveMeasurementDataFromMeasurementRow(TermId.PLOT_NO.getId(), dataList);
			Assert.assertEquals("Expecting that the PLOT_NO value is equal to ENTRY_NO.", entryNoData.getValue(), plotNoData.getValue());
		}
	}

	@Test
	public void testHasCheckVariablesTrue() {

		final List<MeasurementVariable> conditions = new ArrayList<>();
		conditions.add(this
				.createMeasurementVariable(TermId.CHECK_START.getId(), "CHECK_START", "ED - check start", "Number", "Field trial",
						"TRIAL"));
		conditions.add(this
				.createMeasurementVariable(TermId.CHECK_INTERVAL.getId(), "CHECK_INTERVAL", "ED - check interval", "Number", "Field trial",
						"TRIAL"));
		conditions.add(this
				.createMeasurementVariable(TermId.CHECK_PLAN.getId(), "CHECK_PLAN", "ED - check plan", "Code of CHECK_PLAN", "Assigned",
						"TRIAL"));

		Assert.assertTrue(this.designImportController.hasCheckVariables(conditions));

	}

	@Test
	public void testHasCheckVariablesFalse() {

		final List<MeasurementVariable> conditions = new ArrayList<>();

		conditions.add(this.createMeasurementVariable(TermId.TRIAL_LOCATION.getId(), DesignImportControllerTest.LOCATION_NAME, "Location",
				"Location name", "Assigned", "TRIAL"));

		Assert.assertFalse(this.designImportController.hasCheckVariables(conditions));

	}

	@Test
	public void testCreateMeasurementVariableFromStandardVariable() {

		final MeasurementVariable measurementVariable = this.designImportController
				.createMeasurementVariableFromStandardVariable("LOCATION_NAME_ID", TermId.LOCATION_ID.getId(),
						PhenotypicType.TRIAL_ENVIRONMENT);

		Assert.assertEquals("LOCATION_NAME_ID", measurementVariable.getName());
		Assert.assertEquals(PhenotypicType.TRIAL_ENVIRONMENT, measurementVariable.getRole());

	}

	@Test
	public void testPopulateTheValueOfLocationIDBasedOnLocationName() {

		final Map<String, String> managementDetailValues = new HashMap<>();

		final Location location = new Location(98987);
		final String locationName = "Agua Fria";

		location.setLname(locationName);
		Mockito.when(this.fieldbookMiddlewareService.getLocationByName(locationName, Operation.EQUAL)).thenReturn(location);

		this.designImportController
				.populateTheValueOfLocationIDBasedOnLocationName(managementDetailValues, TermId.LOCATION_ID.getId(), locationName);

		Assert.assertEquals("If the location is found in the database, the value should be the id of the location ",
				location.getLocid().toString(), managementDetailValues.get(String.valueOf(TermId.LOCATION_ID.getId())));

	}

	@Test
	public void testPopulateTheValueOfLocationIDBasedOnLocationNameLocationDoesNotExist() {

		final Map<String, String> managementDetailValues = new HashMap<>();

		Mockito.when(this.fieldbookMiddlewareService.getLocationByName(Matchers.anyString(), Matchers.any(Operation.class)))
				.thenReturn(null);

		this.designImportController
				.populateTheValueOfLocationIDBasedOnLocationName(managementDetailValues, TermId.LOCATION_ID.getId(), "Agua Fria");

		Assert.assertEquals("If the location does not exist, the value should be empty", "",
				managementDetailValues.get(String.valueOf(TermId.LOCATION_ID.getId())));

	}

	@Test
	public void testPopulateTheValueOfCategoricalVariable() {

		Mockito.when(this.fieldbookService.getAllPossibleValues(TermId.ENTRY_TYPE.getId()))
				.thenReturn(this.createEntryTypePossibleValues());

		final Map<String, String> managementDetailValues = new HashMap<>();
		managementDetailValues.put(String.valueOf(TermId.ENTRY_TYPE.getId()), null);

		this.designImportController.populateTheValueOfCategoricalVariable(TermId.ENTRY_TYPE.getId(), "T", managementDetailValues);

		Assert.assertEquals("The key value of 'T' entry type should be 10170", "10170",
				managementDetailValues.get(String.valueOf(TermId.ENTRY_TYPE.getId())));

	}

	@Test
	public void testPopulateTheValueOfCategoricalVariableValueDoesNotExist() {

		Mockito.when(this.fieldbookService.getAllPossibleValues(TermId.ENTRY_TYPE.getId()))
				.thenReturn(this.createEntryTypePossibleValues());

		final Map<String, String> managementDetailValues = new HashMap<>();
		managementDetailValues.put(String.valueOf(TermId.ENTRY_TYPE.getId()), null);

		this.designImportController.populateTheValueOfCategoricalVariable(TermId.ENTRY_TYPE.getId(), "A", managementDetailValues);

		Assert.assertNull("It will return null for an input not part of possible values",
				managementDetailValues.get(String.valueOf(TermId.ENTRY_TYPE.getId())));

	}

	@Test
	public void testCustomImportDesignTypeDetails() throws Exception {
		// case 1: new study, has no imported design yet
		Mockito.when(this.userSelection.getDesignImportData()).thenReturn(null);
		Mockito.when(this.userSelection.getWorkbook()).thenReturn(null);

		Assert.assertEquals("Show default custom import template name", DesignImportController.DEFAULT_DESIGN,
				this.designImportController.getCustomImportDesignTypeDetails().get("templateName"));
	}

	@Test
	public void testCustomImportDesignTypeDetailsWithImportedFile() throws Exception {
		final DesignImportData designImportData = new DesignImportData();
		designImportData.setImportFileName(DesignImportControllerTest.TEST_IMPORT_FILE_NAME_CSV);

		// case 2: new study with user has already imported design to
		// be saved yet
		Mockito.when(this.userSelection.getDesignImportData()).thenReturn(designImportData);

		Assert.assertEquals("show imported template file name", DesignImportControllerTest.TEST_IMPORT_FILE_NAME_CSV,
				this.designImportController.getCustomImportDesignTypeDetails().get("templateName"));
	}

	@Test
	public void testCustomImportDesignTypeDetailsWithExistingStudy() throws Exception {
		final Workbook workbook = new Workbook();
		final MeasurementVariable expDesignSource = new MeasurementVariable();
		expDesignSource.setTermId(TermId.EXPT_DESIGN_SOURCE.getId());
		expDesignSource.setValue(DesignImportControllerTest.TEST_IMPORT_FILE_NAME_CSV);
		final List<MeasurementVariable> expDesignVariableList = new ArrayList<>();
		expDesignVariableList.add(expDesignSource);

		workbook.setExperimentalDesignVariables(expDesignVariableList);

		// case 3: show filename retrieved from EXP_DESIGN_SOURCE
		Mockito.when(this.userSelection.getWorkbook()).thenReturn(workbook);

		Assert.assertEquals("Show saved custom import file name", DesignImportControllerTest.TEST_IMPORT_FILE_NAME_CSV,
				this.designImportController.getCustomImportDesignTypeDetails().get("templateName"));
	}

	/**
	 * Reusable test assertions for
	 * DesignImportController.validateAndSaveNewMapping
	 *
	 * @return results
	 * @throws DesignValidationException
	 */
	private Map<String, Object> verifyMapDesignImportData() throws DesignValidationException {
		Mockito.when(this.designImportService
				.areTrialInstancesMatchTheSelectedEnvironments(3, this.userSelection.getDesignImportData()))
				.thenReturn(true);

		final Map<String, Object> results = this.designImportController
				.validateAndSaveNewMapping(this.createTestMappedHeaders(), 3);

		Mockito.verify(this.designImportValidator)
				.validateDesignData(this.userSelection.getDesignImportData());

		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders =
			this.userSelection.getDesignImportData().getMappedHeaders();

		Assert.assertEquals(1, mappedHeaders.get(PhenotypicType.TRIAL_ENVIRONMENT).size());
		Assert.assertEquals(0, mappedHeaders.get(PhenotypicType.GERMPLASM).size());
		Assert.assertEquals(0, mappedHeaders.get(PhenotypicType.TRIAL_DESIGN).size());
		Assert.assertEquals(0, mappedHeaders.get(PhenotypicType.VARIATE).size());

		final DesignHeaderItem designHeaderItem = mappedHeaders.get(PhenotypicType.TRIAL_ENVIRONMENT).get(0);
		Assert.assertEquals("The DesignHeaderItem SITE_NAME should be mapped to the SITE_NAME Standard Variable", TermId.SITE_NAME.getId(),
				designHeaderItem.getVariable().getId());

		return results;
	}

	@Test
	public void testResolveLocalNameOfTheStudyEnvironmentVariable() {

		final List<SettingDetail> settingDetails = new ArrayList<SettingDetail>();

		settingDetails.add(SettingDetailTestDataInitializer
				.createSettingDetail(TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL_INSTANCE", "", "TRIAL"));
		settingDetails.add(SettingDetailTestDataInitializer
				.createSettingDetail(TermId.TRIAL_LOCATION.getId(), DesignImportControllerTest.LOCATION_NAME, "", "TRIAL"));

		final DesignImportData designImportData = this.userSelection.getDesignImportData();

		Assert.assertEquals("Expecting a local name value 'LOCATION_NAME' because the variable exists in study variable list",
				DesignImportControllerTest.LOCATION_NAME, this.designImportController
						.resolveLocalNameOfTheTrialEnvironmentVariable(TermId.TRIAL_LOCATION.getId(), settingDetails, designImportData));
		Assert.assertEquals("Expecting a local name value 'SITE_NAME' because the variable exists in the headers of design import data",
				"SITE_NAME_LOCAL_NAME", this.designImportController
						.resolveLocalNameOfTheTrialEnvironmentVariable(TermId.SITE_NAME.getId(), settingDetails, designImportData));
		Assert.assertEquals(
				"Expecting an empty value because COOPERATOR_ID does not exist in both trial variables and headers of design import data",
				"", this.designImportController
						.resolveLocalNameOfTheTrialEnvironmentVariable(TermId.COOPERATOOR_ID.getId(), settingDetails, designImportData));

	}

	@Test
	public void testResolveStandardVariableNameOfTheStudyEnvironmentVariable() {

		final List<SettingDetail> settingDetails = new ArrayList<SettingDetail>();

		settingDetails.add(SettingDetailTestDataInitializer
				.createSettingDetail(TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL_INSTANCE", "", "TRIAL"));
		settingDetails.add(SettingDetailTestDataInitializer
				.createSettingDetail(TermId.TRIAL_LOCATION.getId(), DesignImportControllerTest.LOCATION_NAME, "", "TRIAL"));

		final DesignImportData designImportData = this.userSelection.getDesignImportData();

		Assert.assertEquals("Expecting standard variable name 'LOCATION_NAME' because the variable exists in trial variable list",
				DesignImportControllerTest.LOCATION_NAME, this.designImportController
						.resolveStandardVariableNameOfTheTrialEnvironmentVariable(TermId.TRIAL_LOCATION.getId(), settingDetails,
								designImportData));
		Assert.assertEquals("Expecting standard variable name 'SITE_NAME' because the variable exists in the headers of design import data",
				DesignImportControllerTest.SITE_NAME, this.designImportController
						.resolveStandardVariableNameOfTheTrialEnvironmentVariable(TermId.SITE_NAME.getId(), settingDetails,
								designImportData));

		Assert.assertEquals("Expecting variable name COOPERATOR_ID because the variable exists in Ontology",
				DesignImportControllerTest.COOPERATOR_ID, this.designImportController
						.resolveStandardVariableNameOfTheTrialEnvironmentVariable(TermId.COOPERATOOR_ID.getId(), settingDetails,
								designImportData));

		Assert.assertEquals("Expecting an empty variable name because the term does not exist in trial variable list, headers of design "
				+ "import data and ontology", "", this.designImportController
				.resolveStandardVariableNameOfTheTrialEnvironmentVariable(TermId.OBS_UNIT_ID.getId(), settingDetails, designImportData));

	}

	private MeasurementVariable getMeasurementVariable(final int termId, final Set<MeasurementVariable> measurementVariables) {
		for (final MeasurementVariable mvar : measurementVariables) {
			if (termId == mvar.getTermId()) {
				return mvar;
			}
		}
		return null;

	}

	private SettingDetail getSettingDetail(final int termId, final List<SettingDetail> settingDetails) {
		for (final SettingDetail settingDetail : settingDetails) {
			if (termId == settingDetail.getVariable().getCvTermId().intValue()) {
				return settingDetail;
			}
		}
		return null;

	}

	private InstanceInfo createEnvironmentData(final int numberOfIntances) {
		final InstanceInfo instanceInfo = new InstanceInfo();
		final List<Instance> instances = new ArrayList<>();

		for (int x = 1; x <= numberOfIntances; x++) {
			final Instance env = new Instance();
			env.setManagementDetailValues(this.createManagementDetailValues(x));
			env.setInstanceId(x);
			instances.add(env);
		}

		instanceInfo.setInstances(instances);
		instanceInfo.setNumberOfInstances(numberOfIntances);
		return instanceInfo;
	}

	private Map<String, String> createManagementDetailValues(final int instanceNo) {
		final Map<String, String> map = new HashMap<>();
		map.put(String.valueOf(TermId.TRIAL_INSTANCE_FACTOR.getId()), String.valueOf(instanceNo));
		map.put(String.valueOf(TermId.LOCATION_ID.getId()), "1234");
		map.put(String.valueOf(TermId.TRIAL_LOCATION.getId()), "Test Location");
		map.put(String.valueOf(TermId.SITE_NAME.getId()), "Test Site");
		map.put(String.valueOf(TermId.PI_NAME.getId()), null);
		map.put(String.valueOf(TermId.COOPERATOR.getId()), "4321");
		return map;
	}

	private List<SettingDetail> createSettingDetails() {
		final List<SettingDetail> settingDetails = new ArrayList<SettingDetail>();

		settingDetails.add(SettingDetailTestDataInitializer
				.createSettingDetail(TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL_INSTANCE", "", "TRIAL"));
		settingDetails.add(SettingDetailTestDataInitializer
				.createSettingDetail(TermId.TRIAL_LOCATION.getId(), DesignImportControllerTest.LOCATION_NAME, "", "TRIAL"));
		settingDetails.add(SettingDetailTestDataInitializer.createSettingDetail(TermId.LOCATION_ID.getId(), "LOCATION_ID", "", "TRIAL"));
		settingDetails.add(SettingDetailTestDataInitializer
				.createSettingDetail(TermId.SITE_NAME.getId(), DesignImportControllerTest.SITE_NAME, "", "TRIAL"));
		settingDetails.add(SettingDetailTestDataInitializer.createSettingDetail(TermId.PI_NAME.getId(), "PI_NAME", "", "TRIAL"));
		settingDetails.add(SettingDetailTestDataInitializer.createSettingDetail(TermId.COOPERATOR.getId(), "COOPERATOR", "", "TRIAL"));

		return settingDetails;
	}

	private Map<String, List<DesignHeaderItem>> createMappedHeaders() {

		final Map<String, List<DesignHeaderItem>> map = new HashMap<>();

		map.put("mappedEnvironmentalFactors", this.createDesignHeaderItems(TermId.SITE_NAME.getId()));
		map.put("mappedDesignFactors", this.createDesignHeaderItems(TermId.PLOT_NO.getId()));
		map.put("mappedGermplasmFactors", this.createDesignHeaderItems(TermId.ENTRY_NO.getId()));
		map.put("mappedTraits", this.createDesignHeaderItems(DesignImportControllerTest.GW_100G_TERMID));

		return map;
	}

	private List<DesignHeaderItem> createDesignHeaderItems(final int... termIds) {

		final List<DesignHeaderItem> items = new ArrayList<>();
		for (final int termid : termIds) {
			final DesignHeaderItem item = new DesignHeaderItem();
			item.setId(termid);
			items.add(item);
		}

		return items;
	}

	private void initializeOntologyData() throws MiddlewareQueryException {

		final Map<String, List<StandardVariable>> map = new HashMap<>();

		final StandardVariable trialInstance =
				this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL_INSTANCE", "",
						"", "", "", "", "");
		final StandardVariable siteName = this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, TermId.SITE_NAME.getId(),
				DesignImportControllerTest.SITE_NAME, "", "", "", "", "", "");
		final StandardVariable locationName = this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, TermId.TRIAL_LOCATION.getId(),
				DesignImportControllerTest.LOCATION_NAME, "", "", "", "", "", "");
		final StandardVariable locationID =
				this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, TermId.LOCATION_ID.getId(), "LOCATION_NAME_ID", "", "", "",
						"", "", "");
		final StandardVariable cooperator =
				this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, TermId.COOPERATOR.getId(), "COOPERATOR", "", "", "", "", "",
						"");

		final StandardVariable cooperatorId =
				this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, TermId.COOPERATOOR_ID.getId(), "COOPERATOR_ID", "", "", "",
						"", "", "");
		final StandardVariable principalInvestigator =
				this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, TermId.PI_NAME.getId(), "PI_NAME", "", "", "", "", "", "");

		final StandardVariable principalInvestigatorId =
				this.createStandardVariable(PhenotypicType.TRIAL_ENVIRONMENT, TermId.PI_ID.getId(), "PI_NAME_ID", "", "", "", "", "", "");
		final StandardVariable entryNo =
				this.createStandardVariable(PhenotypicType.GERMPLASM, TermId.ENTRY_NO.getId(), "ENTRY_NO", "", "", "", "", "", "");
		final StandardVariable plotNo =
				this.createStandardVariable(PhenotypicType.TRIAL_DESIGN, TermId.PLOT_NO.getId(), "PLOT_NO", "", "", "", "", "", "");
		final StandardVariable repNo =
				this.createStandardVariable(PhenotypicType.TRIAL_DESIGN, TermId.REP_NO.getId(), "REP_NO", "", "", "", "", "", "");
		final StandardVariable blockNo =
				this.createStandardVariable(PhenotypicType.TRIAL_DESIGN, TermId.BLOCK_NO.getId(), "BLOCK_NO", "", "", "", "", "", "");
		final StandardVariable gw100g =
				this.createStandardVariable(PhenotypicType.VARIATE, DesignImportControllerTest.GW_100G_TERMID, "GW_100G", "", "", "", "",
						"", "");

		map.put("TRIAL_INSTANCE", this.createList(trialInstance));
		map.put(DesignImportControllerTest.SITE_NAME, this.createList(siteName));
		map.put(DesignImportControllerTest.LOCATION_NAME, this.createList(locationName));
		map.put("LOCATION_NAME_ID", this.createList(locationID));
		map.put("ENTRY_NO", this.createList(entryNo));
		map.put("PLOT_NO", this.createList(plotNo));
		map.put("REP_NO", this.createList(repNo));
		map.put("BLOCK_NO", this.createList(blockNo));
		map.put("COOPERATOR", this.createList(cooperator));
		map.put("COOPERATOR_ID", this.createList(cooperatorId));
		map.put("PI_NAME", this.createList(principalInvestigator));
		map.put("PI_NAME_ID", this.createList(principalInvestigatorId));

		Mockito.doReturn(trialInstance).when(this.ontologyDataManager)
				.getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), this.project.getUniqueID());
		Mockito.doReturn(siteName).when(this.ontologyDataManager).getStandardVariable(TermId.SITE_NAME.getId(), this.project.getUniqueID());
		Mockito.doReturn(locationName).when(this.ontologyDataManager)
				.getStandardVariable(TermId.TRIAL_LOCATION.getId(), this.project.getUniqueID());
		Mockito.doReturn(locationID).when(this.ontologyDataManager)
				.getStandardVariable(TermId.LOCATION_ID.getId(), this.project.getUniqueID());
		Mockito.doReturn(entryNo).when(this.ontologyDataManager).getStandardVariable(TermId.ENTRY_NO.getId(), this.project.getUniqueID());
		Mockito.doReturn(plotNo).when(this.ontologyDataManager).getStandardVariable(TermId.PLOT_NO.getId(), this.project.getUniqueID());
		Mockito.doReturn(cooperator).when(this.ontologyDataManager)
				.getStandardVariable(TermId.COOPERATOR.getId(), this.project.getUniqueID());

		Mockito.doReturn(cooperatorId).when(this.ontologyDataManager)
				.getStandardVariable(TermId.COOPERATOOR_ID.getId(), this.project.getUniqueID());
		Mockito.doReturn(principalInvestigator).when(this.ontologyDataManager)
				.getStandardVariable(TermId.PI_NAME.getId(), this.project.getUniqueID());
		Mockito.doReturn(gw100g).when(this.ontologyDataManager)
				.getStandardVariable(DesignImportControllerTest.GW_100G_TERMID, this.project.getUniqueID());

		Mockito.doReturn(siteName).when(this.fieldbookMiddlewareService)
				.getStandardVariable(TermId.SITE_NAME.getId(), this.project.getUniqueID());

		Mockito.doReturn(cooperatorId).when(this.fieldbookMiddlewareService)
				.getStandardVariable(TermId.COOPERATOOR_ID.getId(), this.project.getUniqueID());
		Mockito.doReturn(principalInvestigator).when(this.fieldbookMiddlewareService)
				.getStandardVariable(TermId.PI_NAME.getId(), this.project.getUniqueID());
		Mockito.doReturn(principalInvestigatorId).when(this.fieldbookMiddlewareService)
				.getStandardVariable(TermId.PI_ID.getId(), this.project.getUniqueID());

	}

	private void initializeDesignImportService() throws DesignValidationException {
		Mockito.doReturn(new ArrayList<MeasurementRow>()).when(this.designImportService)
				.generateDesign(Matchers.any(Workbook.class), Matchers.any(DesignImportData.class), Matchers.any(InstanceInfo.class),
						Matchers.anyBoolean(), Matchers.anyMapOf(String.class, Integer.class));
		Mockito.doReturn(new HashSet<MeasurementVariable>()).when(this.designImportService)
				.getDesignMeasurementVariables(Matchers.any(Workbook.class), Matchers.any(DesignImportData.class), Matchers.anyBoolean());
		Mockito.doReturn(new HashSet<MeasurementVariable>()).when(this.designImportService)
				.getDesignRequiredStandardVariables(Matchers.any(Workbook.class), Matchers.any(DesignImportData.class));
		Mockito.doReturn(new HashSet<MeasurementVariable>()).when(this.designImportService)
				.getDesignRequiredMeasurementVariable(Matchers.any(Workbook.class), Matchers.any(DesignImportData.class));
	}

	private StandardVariable createStandardVariable(final PhenotypicType phenotypicType, final int id, final String name,
			final String property, final String scale, final String method, final String dataType, final String storedIn,
			final String isA) {

		final StandardVariable stdVar =
				new StandardVariable(new Term(0, property, ""), new Term(0, scale, ""), new Term(0, method, ""), new Term(0, dataType, ""),
						new Term(0, isA, ""), phenotypicType);

		stdVar.setId(id);
		stdVar.setName(name);
		stdVar.setDescription("");

		return stdVar;
	}

	private List<StandardVariable> createList(final StandardVariable... stdVar) {
		final List<StandardVariable> stdVarList = new ArrayList<>();
		for (final StandardVariable var : stdVar) {
			stdVarList.add(var);
		}
		return stdVarList;

	}

	private Project createProject() {
		final Project project = new Project();
		project.setUniqueID("");
		project.setProjectId(1L);
		return project;
	}

	private MeasurementVariable createMeasurementVariable(final int termId, final String name, final String label) {
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(termId);
		measurementVariable.setName(name);
		measurementVariable.setLabel(label);
		return measurementVariable;
	}

	private MeasurementVariable createMeasurementVariable(final int termId, final String name, final String property, final String scale,
			final String method, final String label) {
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(termId);
		measurementVariable.setName(name);
		measurementVariable.setLabel(label);
		measurementVariable.setProperty(property);
		measurementVariable.setScale(scale);
		measurementVariable.setMethod(method);
		return measurementVariable;
	}

	private Set<MeasurementVariable> createMeasurementVariables() {

		final Set<MeasurementVariable> measurementVariables = new HashSet<>();
		measurementVariables.add(this.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL_INSTANCE", "TRIAL"));
		measurementVariables
				.add(this.createMeasurementVariable(TermId.TRIAL_LOCATION.getId(), DesignImportControllerTest.LOCATION_NAME, "TRIAL"));
		measurementVariables.add(this.createMeasurementVariable(TermId.LOCATION_ID.getId(), "LOCATION_NAME_ID", "TRIAL"));
		measurementVariables.add(this.createMeasurementVariable(TermId.SITE_NAME.getId(), DesignImportControllerTest.SITE_NAME, "TRIAL"));
		measurementVariables.add(this.createMeasurementVariable(TermId.COOPERATOR.getId(), "COOPERATOR", "TRIAL"));
		measurementVariables.add(this.createMeasurementVariable(TermId.ENTRY_NO.getId(), "ENTRY_NO", "GERMPLASM ENTRY"));
		measurementVariables.add(this.createMeasurementVariable(TermId.PLOT_NO.getId(), "PLOT_NO", "PLOT"));
		measurementVariables.add(this.createMeasurementVariable(TermId.REP_NO.getId(), "REP_NO", "PLOT"));
		measurementVariables.add(this.createMeasurementVariable(TermId.BLOCK_NO.getId(), "BLOCK_NO", "PLOT"));
		return measurementVariables;
	}

	private List<SettingDetail> createDeletedStudyLevelVariables() {

		final List<SettingDetail> deletedStudyLevelVariables = new ArrayList<>();

		deletedStudyLevelVariables.add(SettingDetailTestDataInitializer
				.createSettingDetail(TermId.SITE_NAME.getId(), DesignImportControllerTest.SITE_NAME, "", "TRIAL"));
		deletedStudyLevelVariables.add(SettingDetailTestDataInitializer
				.createSettingDetail(TermId.TRIAL_LOCATION.getId(), DesignImportControllerTest.LOCATION_NAME, "", "TRIAL"));
		deletedStudyLevelVariables
				.add(SettingDetailTestDataInitializer.createSettingDetail(TermId.LOCATION_ID.getId(), "LOCATION_NAME_ID", "", "TRIAL"));

		return deletedStudyLevelVariables;
	}

	private Map<String, List<DesignHeaderItem>> createTestMappedHeaders() {
		final Map<String, List<DesignHeaderItem>> testNewMap = new HashMap<>();
		final DesignHeaderItem designHeaderItem = new DesignHeaderItem();
		designHeaderItem.setId(TermId.SITE_NAME.getId());
		designHeaderItem.setName(DesignImportControllerTest.SITE_NAME);
		final List<DesignHeaderItem> designHeaderItems = new ArrayList<>();
		designHeaderItems.add(designHeaderItem);
		testNewMap.put("mappedEnvironmentalFactors", designHeaderItems);
		testNewMap.put("mappedDesignFactors", new ArrayList<DesignHeaderItem>());
		testNewMap.put("mappedGermplasmFactors", new ArrayList<DesignHeaderItem>());
		testNewMap.put("mappedTraits", new ArrayList<DesignHeaderItem>());
		return testNewMap;
	}

	private List<ValueReference> createEntryTypePossibleValues() {

		final List<ValueReference> list = new ArrayList<>();

		list.add(new ValueReference(10170, "T", "Test entry"));
		list.add(new ValueReference(10180, "C", "Check entry"));

		return list;
	}

	private DesignImportData createDesignImportDataWithCooperatorVariable() {

		final DesignImportData data = DesignImportTestDataInitializer.createDesignImportData(1, 1);

		final DesignHeaderItem cooperatorNameDesignHeaderItem = DesignImportTestDataInitializer
				.createDesignHeaderItem(PhenotypicType.TRIAL_ENVIRONMENT, TermId.COOPERATOR.getId(),
						DesignImportControllerTest.COOPERATOR_NAME, 1, DesignImportTestDataInitializer.NUMERIC_VARIABLE);
		cooperatorNameDesignHeaderItem.setName(DesignImportControllerTest.COOPERATOR_NAME);

		data.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT).add(cooperatorNameDesignHeaderItem);

		return data;

	}

}
