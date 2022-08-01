
package com.efficio.fieldbook.web.trial.controller;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.data.initializer.DesignImportTestDataInitializer;
import com.efficio.fieldbook.web.study.germplasm.StudyEntryTransformer;
import com.efficio.fieldbook.web.trial.TestDataHelper;
import com.efficio.fieldbook.web.trial.bean.*;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.util.SessionUtility;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import com.google.common.collect.Lists;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.security.AuthorizationService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.api.file.FileMetadataService;
import org.generationcp.middleware.data.initializer.MeasurementDataTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.StandardVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.*;
import org.generationcp.middleware.domain.etl.*;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.samplelist.SampleListDTO;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.api.role.RoleService;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.dms.StudyType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.SampleListService;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.generationcp.middleware.service.api.study.StudyService;
import org.generationcp.middleware.service.api.user.UserService;
import org.generationcp.middleware.util.Util;
import org.generationcp.middleware.utils.test.UnitTestDaoIDGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class OpenTrialControllerTest {

	private static final String VARIABLE_NAME_PREFIX = "Variable ";
	private static final String VARIABLE_NAME = "Variable Name";
	private static final String SELECTION_TRAIT = "BM_CODE_VTE";
	private static final String TRAIT = "nEarsSel_Local";
	private static final int NO_OF_TRIAL_INSTANCES = 3;
	private static final int NO_OF_OBSERVATIONS = 5;
	private static final int STUDY_ID = 1;
	private static final String PROGRAM_UUID = "68f0d114-5b5b-11e5-885d-feff819cdc9f";
	private static final String TEST_STUDY_NAME = "dummyStudy";
	private static final int BM_CODE_VTE_ID = 8252;
	private static final int N_EARS_SEL = 8253;
	private static final String GERMPLASM_LIST_SIZE = "germplasmListSize";
	private static final String GERMPLASM_CHECKS_SIZE = "germplasmChecksSize";

	@Mock
	private HttpServletRequest httpRequest;

	@Mock
	private HttpSession httpSession;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private RoleService roleService;

	@Mock
	private UserSelection userSelection;

	@Mock
	private CreateTrialForm createTrialForm;

	@Mock
	private Model model;

	@Mock
	private RedirectAttributes redirectAttributes;

	@Mock
	protected FieldbookService fieldbookMiddlewareService;

	@Mock
	protected com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Mock
	private ErrorHandlerService errorHandlerService;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private UserService userService;

	@Mock
	private OntologyVariableDataManager variableDataManager;

	@Mock
	private InventoryDataManager inventoryDataManager;

	@Mock
	private AuthorizationService authorizationService;

	@InjectMocks
	private OpenTrialController openTrialController;

	@Mock
	private SampleListService sampleListService;

	private Variable testVariable;

	@Mock
	private DatasetTypeService datasetTypeService;

	@Mock
	private DatasetService datasetService;

	@Mock
	private TermDataManager termDataManager;

	@Mock
	private StudyEntryService studyEntryService;

	@Mock
	private StudyEntryTransformer studyEntryTransformer;

	@Mock
	private StudyService studyService;

	@Mock
	private FileMetadataService fileMetadataService;

	@Before
	public void setUp() {
		final DmsProject dmsProject = this.createDmsProject();
		final StudyType studyType = new StudyType();
		studyType.setName(StudyTypeDto.TRIAL_NAME);
		studyType.setLabel(StudyTypeDto.TRIAL_LABEL);
		dmsProject.setStudyType(studyType);

		Mockito.when(this.studyDataManager.getProject(1)).thenReturn(dmsProject);
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(OpenTrialControllerTest.PROGRAM_UUID);

		final Project testProject = new Project();
		testProject.setProjectId(1L);
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(testProject);
		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook();
		workbook.setTrialDatasetId(1);
		workbook.setMeasurementDatesetId(1);
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(1);
		workbook.setStudyDetails(studyDetails);
		Mockito.when(this.userSelection.getWorkbook()).thenReturn(workbook);
		this.initializeOntology();
		final VariableTypeList factors = Mockito.mock(VariableTypeList.class);
		Mockito.when(factors.findById(ArgumentMatchers.anyInt())).thenReturn(null);
		Mockito.when(this.studyDataManager.getAllStudyFactors(ArgumentMatchers.anyInt())).thenReturn(factors);
		this.createTestVariable();
		Mockito.when(this.variableDataManager.getVariable(ArgumentMatchers.any(String.class), ArgumentMatchers.any(Integer.class), ArgumentMatchers.anyBoolean()))
			.thenReturn(this.testVariable);
		Mockito.when(this.studyDataManager.getStudyTypeByName(Mockito.anyString())).thenReturn(StudyTypeDto.getTrialDto());

		Mockito.when(this.authorizationService.isSuperAdminUser()).thenReturn(Boolean.TRUE);
	}

	@Test
	public void testOpenStudyNoRedirect() {
		final List<Integer> datasetTypes = new ArrayList<>();
		datasetTypes.add(4);
		Mockito.doReturn(datasetTypes).when(this.datasetTypeService).getObservationDatasetTypeIds();
		final Workbook workbook =
			WorkbookTestDataInitializer.getTestWorkbook(OpenTrialControllerTest.NO_OF_OBSERVATIONS, StudyTypeDto.getTrialDto());
		WorkbookTestDataInitializer.setTrialObservations(workbook);
		Mockito.when(this.fieldbookMiddlewareService.getStudyDataSet(OpenTrialControllerTest.STUDY_ID)).thenReturn(workbook);
		Mockito.when(this.studyService.studyHasGivenDatasetType(Mockito.anyInt(), Mockito.anyInt())).thenReturn(false);

		final Study study = new Study();
		study.setStudyType(StudyTypeDto.getTrialDto());

		this.mockStandardVariables(workbook.getAllVariables());

		final String out = this.openTrialController.openTrial(this.createTrialForm, OpenTrialControllerTest.STUDY_ID, this.model,
			this.httpSession, this.redirectAttributes, null);

		Mockito.verify(this.fieldbookMiddlewareService).getStudyDataSet(OpenTrialControllerTest.STUDY_ID);

		Assert.assertEquals("should return the base angular template", AbstractBaseFieldbookController.ANGULAR_BASE_TEMPLATE_NAME, out);
	}

	@Test
	public void testOpenStudyRedirectForIncompatibleStudy() {

		Mockito.when(this.fieldbookMiddlewareService.getStudyDataSet(OpenTrialControllerTest.STUDY_ID))
			.thenThrow(MiddlewareQueryException.class);

		final String out = this.openTrialController.openTrial(this.createTrialForm, OpenTrialControllerTest.STUDY_ID, this.model,
			this.httpSession, this.redirectAttributes, null);

		Assert.assertEquals("should redirect to manage Study page", "redirect:" + ManageTrialController.URL, out);

		final ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<String> arg2 = ArgumentCaptor.forClass(String.class);

		Mockito.verify(this.redirectAttributes).addFlashAttribute(arg1.capture(), arg2.capture());
		Assert.assertEquals("value should be redirectErrorMessage", "redirectErrorMessage", arg1.getValue());

	}

	@Test
	public void testSessionClearOnOpenStudy() {

		final MockHttpSession mockSession = new MockHttpSession();

		final Workbook workbook =
			WorkbookTestDataInitializer.getTestWorkbook(OpenTrialControllerTest.NO_OF_OBSERVATIONS, StudyTypeDto.getTrialDto());
		WorkbookTestDataInitializer.setTrialObservations(workbook);

		mockSession.setAttribute(SessionUtility.USER_SELECTION_SESSION_NAME, new UserSelection());
		mockSession.setAttribute(SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME, new ArrayList<Integer>());

		try {
			Mockito.when(this.fieldbookMiddlewareService.getStudyDataSet(ArgumentMatchers.anyInt())).thenReturn(workbook);
			this.mockStandardVariables(workbook.getAllVariables());
			Mockito.when(this.studyService.studyHasGivenDatasetType(Mockito.anyInt(), Mockito.anyInt())).thenReturn(false);
			this.openTrialController.openTrial(new CreateTrialForm(), OpenTrialControllerTest.STUDY_ID, new ExtendedModelMap(), mockSession,
				Mockito.mock(RedirectAttributes.class), null);
		} catch (final MiddlewareException e) {
			this.handleUnexpectedException(e);
		}

		Assert.assertNull("Controller does not properly reset user selection object on open of Study",
			mockSession.getAttribute(SessionUtility.USER_SELECTION_SESSION_NAME));
		Assert.assertNull("Controller does not properly reset the pagination list selection",
			mockSession.getAttribute(SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME));
	}

	@Test
	public void testHappyPathOpenStudyCheckModelAttributes() {

		final Model model = new ExtendedModelMap();

		final Workbook workbook =
			WorkbookTestDataInitializer.getTestWorkbook(OpenTrialControllerTest.NO_OF_OBSERVATIONS, StudyTypeDto.getTrialDto());
		WorkbookTestDataInitializer.setTrialObservations(workbook);

		// Verify that workbook has Analysis and/or Analysis Summary variables
		// beforehand to check that they were later removed
		Assert.assertTrue(this.hasAnalysisVariables(workbook.getConditions()));
		Assert.assertTrue(this.hasAnalysisVariables(workbook.getConstants()));

		try {

			Mockito.when(this.fieldbookMiddlewareService.getStudyDataSet(ArgumentMatchers.anyInt())).thenReturn(workbook);
			this.mockStandardVariables(workbook.getAllVariables());
			Mockito.when(this.studyService.studyHasGivenDatasetType(Mockito.anyInt(), Mockito.anyInt())).thenReturn(false);

			this.openTrialController.openTrial(new CreateTrialForm(), OpenTrialControllerTest.STUDY_ID, model, new MockHttpSession(),
				Mockito.mock(RedirectAttributes.class), null);

			Assert.assertTrue("Controller does not properly set into the model the data for the basic details",
				model.containsAttribute("basicDetailsData"));

			Assert.assertTrue("Controller does not properly set into the model the data for the germplasm tab",
				model.containsAttribute("germplasmData"));
			Assert.assertTrue("Controller does not properly set into the model the data for the environments tab",
				model.containsAttribute(OpenTrialController.ENVIRONMENT_DATA_TAB));
			Assert.assertTrue("Controller does not properly set into the model the data for the Study settings tab",
				model.containsAttribute("trialSettingsData"));
			Assert.assertTrue("Controller does not properly set into the model the data for the experimental design tab",
				model.containsAttribute("experimentalDesignData"));
			Assert.assertTrue("Controller does not properly set into the model the data for the treatment factors tab",
				model.containsAttribute("treatmentFactorsData"));
			Assert.assertTrue("Controller does not properly set into the model the data for the germplasm list size",
				model.containsAttribute(OpenTrialControllerTest.GERMPLASM_LIST_SIZE));
			Assert.assertTrue("Controller does not properly set into the model copy of the study form",
				model.containsAttribute("createTrialForm"));
			Assert.assertTrue("Controller does not properly set into the model special data required for experimental design tab",
				model.containsAttribute("experimentalDesignSpecialData"));
			Assert.assertTrue("Controller does not properly set into the model the study name", model.containsAttribute("studyName"));
			Assert.assertFalse("'Analysis' and 'Analysis Summary' variables should not be displayed.", this.hasAnalysisVariables(model));

		} catch (final MiddlewareException e) {
			this.handleUnexpectedException(e);
		}
	}

	private boolean hasAnalysisVariables(final List<MeasurementVariable> variables) {
		boolean analysisVariableFound = false;
		for (final MeasurementVariable variable : variables) {
			if (VariableType.getReservedVariableTypes().contains(variable.getVariableType())) {
				analysisVariableFound = true;
				break;
			}
		}
		return analysisVariableFound;
	}

	private boolean hasAnalysisVariables(final Model model) {
		final List<SettingDetail> settingDetails = this.getSettingDetailsPossiblyWithAnalysisVariables(model);
		boolean analysisVariableFound = false;
		for (final SettingDetail settingDetail : settingDetails) {
			if (VariableType.getReservedVariableTypes().contains(settingDetail.getVariableType())) {
				analysisVariableFound = true;
				break;
			}
		}
		return analysisVariableFound;
	}

	@SuppressWarnings("unchecked")
	private List<SettingDetail> getSettingDetailsPossiblyWithAnalysisVariables(final Model model) {
		final List<SettingDetail> settingDetails = new ArrayList<>();

		final Map<String, Object> modelMap = model.asMap();

		final TabInfo experimentsDataTabInfo = (TabInfo) modelMap.get(OpenTrialController.ENVIRONMENT_DATA_TAB);
		final List<SettingDetail> managementDetailList =
			(List<SettingDetail>) experimentsDataTabInfo.getSettingMap().get("managementDetails");
		final List<SettingDetail> conditionDetails =
			(List<SettingDetail>) experimentsDataTabInfo.getSettingMap().get("trialConditionDetails");
		settingDetails.addAll(managementDetailList);
		settingDetails.addAll(conditionDetails);
		return settingDetails;
	}

	private void mockStandardVariables(final List<MeasurementVariable> allVariables) {
		for (final MeasurementVariable measurementVariable : allVariables) {
			Mockito.doReturn(this.createStandardVariable(measurementVariable.getTermId())).when(this.fieldbookMiddlewareService)
				.getStandardVariable(measurementVariable.getTermId(), OpenTrialControllerTest.PROGRAM_UUID);
			final Variable variable = new Variable();
			variable.setId(measurementVariable.getTermId());
			variable.setName(measurementVariable.getName());
			variable.setMethod(TestDataHelper.createMethod());
			variable.setProperty(TestDataHelper.createProperty());
			variable.setScale(TestDataHelper.createScale());
			Mockito.when(this.variableDataManager.getVariable(ArgumentMatchers.eq(OpenTrialControllerTest.PROGRAM_UUID),
				ArgumentMatchers.eq(measurementVariable.getTermId()), ArgumentMatchers.anyBoolean())).thenReturn(variable);
		}
	}

	private StandardVariable createStandardVariable(final Integer id) {
		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(id);
		standardVariable.setProperty(new Term(1, "1", "1"));
		standardVariable.setMethod(new Term(2, "2", "2"));
		standardVariable.setScale(new Term(3, "3", "3"));
		standardVariable.setDataType(new Term(4, "4", "4"));
		return standardVariable;
	}

	@Test
	public void testIsPreviewEditableIfStudyDetailsIsExisting() {

		final Workbook originalWorkbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(1);
		originalWorkbook.setStudyDetails(studyDetails);
		final String isPreviewEditable = this.openTrialController.isPreviewEditable(originalWorkbook);
		Assert.assertEquals("Should return 0 since there is already existing study", "0", isPreviewEditable);

	}

	@Test
	public void testIsPreviewEditableIfStudyDetailsIsNull() {

		final Workbook originalWorkbook = new Workbook();
		final String isPreviewEditable = this.openTrialController.isPreviewEditable(originalWorkbook);
		Assert.assertEquals("Should return 1 since there is no existing study", "1", isPreviewEditable);

	}

	@Test
	public void testIsPreviewEditableIfStudyDetailsIsNotNullAndIdIsNull() {

		final Workbook originalWorkbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		originalWorkbook.setStudyDetails(studyDetails);
		final String isPreviewEditable = this.openTrialController.isPreviewEditable(originalWorkbook);
		Assert.assertEquals("Should return 1 since there is no existing study", "1", isPreviewEditable);
	}

	@Test
	public void testIsPreviewEditableIfOriginalWorkbookIsNull() {

		final Workbook originalWorkbook = null;
		final String isPreviewEditable = this.openTrialController.isPreviewEditable(originalWorkbook);
		Assert.assertEquals("Should return 1 since there is no existing study", "1", isPreviewEditable);

	}

	@Test
	public void testGetFilteredStudyObservations() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
			OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);

		final List<MeasurementRow> filteredStudyObservations =
			this.openTrialController.getFilteredTrialObservations(workbook.getTrialObservations(), "2");

		Assert.assertEquals("Expecting the number of Study observations is decreased by one.", workbook.getTotalNumberOfInstances() - 1,
			filteredStudyObservations.size());

		// expecting the trial instance no are in incremental order
		Integer trialInstanceNo = 1;
		for (final MeasurementRow row : filteredStudyObservations) {
			final List<MeasurementData> dataList = row.getDataList();
			for (final MeasurementData data : dataList) {
				if (data.getMeasurementVariable() != null) {
					final MeasurementVariable var = data.getMeasurementVariable();

					if (var != null && data.getMeasurementVariable().getName() != null
						&& "TRIAL_INSTANCE".equalsIgnoreCase(var.getName())) {
						final Integer currentTrialInstanceNo = Integer.valueOf(data.getValue());
						Assert.assertEquals("Expecting trial instance the next trial instance no is " + trialInstanceNo + " but returned "
							+ currentTrialInstanceNo, trialInstanceNo, currentTrialInstanceNo);
						trialInstanceNo++;
						break;
					}
				}
			}
		}
	}

	@Test
	public void testGetFilteredStudyObservationsWithNoDeletedEnvironmentId() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
			OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);

		final List<MeasurementRow> filteredStudyObservations =
			this.openTrialController.getFilteredTrialObservations(workbook.getTrialObservations(), "");

		Assert.assertEquals("Expecting the number of Study observations is the same after the method call.",
			workbook.getTotalNumberOfInstances(), filteredStudyObservations.size());
	}

	@Test
	public void testGetFilteredObservations() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
			OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);

		final List<MeasurementRow> filteredObservations = this.openTrialController.getFilteredObservations(workbook.getObservations(), "2");

		Assert.assertEquals("Expecting the number of observations is decreased by " + OpenTrialControllerTest.NO_OF_OBSERVATIONS,
			workbook.getObservations().size() - OpenTrialControllerTest.NO_OF_OBSERVATIONS, filteredObservations.size());

		// expecting the trial instance no are in incremental order
		final int noOfTrialInstances = OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES - 1;
		for (final MeasurementRow row : filteredObservations) {
			final List<MeasurementData> dataList = row.getDataList();
			for (final MeasurementData data : dataList) {
				if (data.getMeasurementVariable() != null) {
					final MeasurementVariable var = data.getMeasurementVariable();

					if (var != null && data.getMeasurementVariable().getName() != null
						&& "TRIAL_INSTANCE".equalsIgnoreCase(var.getName())) {
						final int currentTrialInstanceNo = Integer.parseInt(data.getValue());
						Assert.assertTrue("Expecting trial instance the next trial instance no is within the "
							+ "possible range of trial instance no but didn't.", currentTrialInstanceNo <= noOfTrialInstances);
					}
				}
			}
		}
	}

	@Test
	public void testGetFilteredObservationsWithNoDeletedEnvironmentId() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
			OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);

		final List<MeasurementRow> filteredObservations = this.openTrialController.getFilteredObservations(workbook.getObservations(), "");

		Assert.assertEquals("Expecting the number of observations is the same after the method call.", workbook.getObservations().size(),
			filteredObservations.size());
	}

	private void handleUnexpectedException(final Exception e) {
		Assert.fail("Unexpected error during unit test : " + e.getMessage());
	}

	private DmsProject createDmsProject() {
		final DmsProject dmsProject = new DmsProject();
		dmsProject.setProjectId(OpenTrialControllerTest.STUDY_ID);
		dmsProject.setName(OpenTrialControllerTest.TEST_STUDY_NAME);
		dmsProject.setProgramUUID(OpenTrialControllerTest.PROGRAM_UUID);
		return dmsProject;
	}

	private void initializeOntology() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
			OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);

		for (final MeasurementVariable mvar : workbook.getAllVariables()) {
			final StandardVariable stdVar = this.convertToStandardVariable(mvar);
			Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(stdVar.getId(), OpenTrialControllerTest.PROGRAM_UUID))
				.thenReturn(stdVar);
		}

		final StandardVariable plotNo =
			this.createStandardVariable(TermId.PLOT_NO.getId(), "PLOT_NO", "Field plot", "Number", "Enumerated", 1110,
				"Numeric variable", "TRIAL_DESIGN");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.PLOT_NO.getId(), OpenTrialControllerTest.PROGRAM_UUID))
			.thenReturn(plotNo);

		final StandardVariable repNo =
			this.createStandardVariable(TermId.REP_NO.getId(), "REP_NO", "Replication factor", "Number", "Enumerated", 1110,
				"Numeric variable", "TRIAL_DESIGN");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.REP_NO.getId(), OpenTrialControllerTest.PROGRAM_UUID))
			.thenReturn(repNo);

		final StandardVariable blockNo =
			this.createStandardVariable(TermId.BLOCK_NO.getId(), "BLOCK_NO", "Blocking factor", "Number", "Enumerated", 1110,
				"Numeric variable", "TRIAL_DESIGN");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.BLOCK_NO.getId(), OpenTrialControllerTest.PROGRAM_UUID))
			.thenReturn(blockNo);

		final StandardVariable row =
			this.createStandardVariable(TermId.ROW.getId(), "ROW", "Row in layout", "Number", "Enumerated", 1110, "Numeric variable",
				"TRIAL_DESIGN");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.ROW.getId(), OpenTrialControllerTest.PROGRAM_UUID))
			.thenReturn(row);

		final StandardVariable col =
			this.createStandardVariable(TermId.COL.getId(), "COL", "Column in layout", "Number", "Enumerated", 1110,
				"Numeric variable", "TRIAL_DESIGN");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.COL.getId(), OpenTrialControllerTest.PROGRAM_UUID))
			.thenReturn(col);

		final StandardVariable prep =
			this.createStandardVariable(TermId.PERCENTAGE_OF_REPLICATION.getId(), "PREP", "ED - % of test entries to replicate", "Number",
				"Assigned", 1110,
				"Numeric variable", "TRIAL_DESIGN");
		Mockito.when(this.fieldbookMiddlewareService
			.getStandardVariable(TermId.PERCENTAGE_OF_REPLICATION.getId(), OpenTrialControllerTest.PROGRAM_UUID)).thenReturn(prep);

	}

	private StandardVariable convertToStandardVariable(final MeasurementVariable measurementVar) {
		return this.createStandardVariable(measurementVar.getTermId(), measurementVar.getName(),
			measurementVar.getProperty(), measurementVar.getScale(), measurementVar.getMethod(), measurementVar.getDataTypeId(),
			measurementVar.getDataType(), measurementVar.getLabel());
	}

	protected StandardVariable createStandardVariable(final int termId, final String name, final String property, final String scale,
		final String method, final int dataTypeId, final String dataType, final String label) {
		final StandardVariable stdVar = new StandardVariable();
		stdVar.setId(termId);
		stdVar.setName(name);
		stdVar.setProperty(new Term(0, property, ""));
		stdVar.setScale(new Term(0, scale, ""));
		stdVar.setMethod(new Term(0, method, ""));
		stdVar.setDataType(new Term(dataTypeId, dataType, ""));
		stdVar.setPhenotypicType(PhenotypicType.getPhenotypicTypeForLabel(label));
		return stdVar;
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_RCBD() {
		final String exptDesignSourceValue = null;
		final Integer nRepValue = 3;
		final String rMapValue = null;
		final Integer replicationsArrangement = null;
		final String percentageReplication = null;
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
			OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook, String.valueOf(TermId.RANDOMIZED_COMPLETE_BLOCK.getId()),
			exptDesignSourceValue, nRepValue.toString(), rMapValue, percentageReplication);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be RCBD", ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getId().intValue(),
			data.getDesignType().intValue());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications arrangement should be " + replicationsArrangement, replicationsArrangement,
			data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", 3, data.getBlockSize().intValue());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_RCBDWithRMap() {
		final String exptDesignSourceValue = null;
		final Integer nRepValue = 3;
		final String rMapValue = String.valueOf(TermId.REPS_IN_SINGLE_COL.getId());
		final Integer replicationsArrangement = 1;
		final String percentageReplication = null;
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
			OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook, String.valueOf(TermId.RANDOMIZED_COMPLETE_BLOCK.getId()),
			exptDesignSourceValue, nRepValue.toString(), rMapValue, percentageReplication);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be RCBD", ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getId().intValue(),
			data.getDesignType().intValue());
		Assert.assertFalse("Design type should not be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications map should be " + replicationsArrangement, replicationsArrangement,
			data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", 3, data.getBlockSize().intValue());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_RIBD() {
		final String exptDesignSourceValue = null;
		final Integer nRepValue = 5;
		final String rMapValue = null;
		final Integer replicationsArrangement = null;
		final String percentageReplication = null;
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
			OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook,
			String.valueOf(TermId.RESOLVABLE_INCOMPLETE_BLOCK.getId()), exptDesignSourceValue, nRepValue.toString(), rMapValue,
			percentageReplication);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be RIBD", ExperimentDesignType.RESOLVABLE_INCOMPLETE_BLOCK.getId().intValue(),
			data.getDesignType().intValue());
		Assert.assertFalse("Design type should not be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications arrangement should be " + replicationsArrangement, replicationsArrangement,
			data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", 3, data.getBlockSize().intValue());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_RIBDL() {
		final String exptDesignSourceValue = null;
		final Integer nRepValue = 3;
		final String rMapValue = null;
		final Integer replicationsArrangement = null;
		final String percentageReplication = null;
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
			OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook,
			String.valueOf(TermId.RESOLVABLE_INCOMPLETE_BLOCK_LATIN.getId()), exptDesignSourceValue, nRepValue.toString(), rMapValue,
			percentageReplication);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be RIBDL", ExperimentDesignType.RESOLVABLE_INCOMPLETE_BLOCK.getId().intValue(),
			data.getDesignType().intValue());
		Assert.assertTrue("Design type should be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications arrangement should be " + replicationsArrangement, replicationsArrangement,
			data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", 3, data.getBlockSize().intValue());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_RRCD() {
		final String exptDesignSourceValue = null;
		final Integer nRepValue = 5;
		final String rMapValue = null;
		final Integer replicationsArrangement = null;
		final String percentageReplication = null;
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
			OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook,
			String.valueOf(TermId.RESOLVABLE_INCOMPLETE_ROW_COL.getId()), exptDesignSourceValue, nRepValue.toString(), rMapValue,
			percentageReplication);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be RRCD", ExperimentDesignType.ROW_COL.getId().intValue(), data.getDesignType().intValue());
		Assert.assertFalse("Design type should not be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications arrangement should be " + replicationsArrangement, replicationsArrangement,
			data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", 3, data.getBlockSize().intValue());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_RRCDL() {
		final String exptDesignSourceValue = null;
		final Integer nRepValue = 3;
		final String rMapValue = null;
		final Integer replicationsArrangement = null;
		final String percentageReplication = null;
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
			OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook,
			String.valueOf(TermId.RESOLVABLE_INCOMPLETE_ROW_COL_LATIN.getId()), exptDesignSourceValue, nRepValue.toString(), rMapValue,
			percentageReplication);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert
			.assertEquals("Design type should be RRCDL", ExperimentDesignType.ROW_COL.getId().intValue(), data.getDesignType().intValue());
		Assert.assertTrue("Design type should be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications arrangement should be " + replicationsArrangement, replicationsArrangement,
			data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", 3, data.getBlockSize().intValue());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_OtherDesign() {
		final String exptDesignSourceValue = "Other design.csv";
		final Integer nRepValue = 2;
		final String rMapValue = null;
		final Integer replicationsArrangement = null;
		final String percentageReplication = null;
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
			OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook, String.valueOf(TermId.OTHER_DESIGN.getId()),
			exptDesignSourceValue, nRepValue.toString(), rMapValue, percentageReplication);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be Other Design", ExperimentDesignType.CUSTOM_IMPORT.getId().intValue(),
			data.getDesignType().intValue());
		Assert.assertFalse("Design type should not be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications arrangement should be " + replicationsArrangement, replicationsArrangement,
			data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", 3, data.getBlockSize().intValue());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_PREP() {
		final String exptDesignSourceValue = null;
		final Integer nRepValue = 5;
		final String rMapValue = null;
		final String percentageReplication = null;
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
			OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook,
			String.valueOf(TermId.P_REP.getId()), exptDesignSourceValue, nRepValue.toString(), rMapValue, percentageReplication);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be P_REP", ExperimentDesignType.P_REP.getId().intValue(),
			data.getDesignType().intValue());
		Assert.assertFalse("Design type should not be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("% of replication should be " + percentageReplication, percentageReplication, data.getReplicationPercentage());
		Assert.assertEquals("Block size should be 3", 3, data.getBlockSize().intValue());
	}

	@Test
	public void testPrepareMeasurementVariableTabInfoForSelectionMethod() {
		final List<MeasurementVariable> variatesList = this.createVariates();

		final boolean isUsePrevious = false;
		final TabInfo tabInfo =
			this.openTrialController.prepareMeasurementVariableTabInfo(variatesList, VariableType.SELECTION_METHOD, isUsePrevious);

		final List<SettingDetail> settings = tabInfo.getSettings();
		Assert.assertEquals("Expecting only selection methods to be included.", 1, settings.size());
		Assert.assertEquals("Expecting trait alias to be used in Setting Detail.", OpenTrialControllerTest.SELECTION_TRAIT,
			settings.get(0).getVariable().getName());
		Assert.assertEquals("Operation should be UPDATE", Operation.UPDATE, settings.get(0).getVariable().getOperation());
		Assert.assertTrue("Setting Detail should be deleteable", settings.get(0).isDeletable());
		Mockito.verify(this.userSelection).setSelectionVariates(settings);
		Mockito.verify(this.userSelection, Mockito.never()).setBaselineTraitsList(ArgumentMatchers.anyListOf(SettingDetail.class));
	}

	@Test
	public void testPrepareMeasurementVariableTabInfoForTrait() {
		final List<MeasurementVariable> variatesList = this.createVariates();

		final boolean isUsePrevious = false;
		final TabInfo tabInfo = this.openTrialController.prepareMeasurementVariableTabInfo(variatesList, VariableType.TRAIT, isUsePrevious);

		final List<SettingDetail> settings = tabInfo.getSettings();
		Assert.assertEquals("Expecting only traits to be included.", 1, settings.size());
		Assert.assertEquals("Expecting trait alias to be used in Setting Detail.", OpenTrialControllerTest.TRAIT,
			settings.get(0).getVariable().getName());
		Assert.assertEquals("Operation should be UPDATE", Operation.UPDATE, settings.get(0).getVariable().getOperation());
		Assert.assertTrue("Setting Detail should be deleteable", settings.get(0).isDeletable());
		Mockito.verify(this.userSelection).setBaselineTraitsList(settings);
		Mockito.verify(this.userSelection, Mockito.never()).setSelectionVariates(ArgumentMatchers.anyListOf(SettingDetail.class));
	}

	@Test
	public void testPrepareMeasurementVariableTabInfoDoUsePrevious() {
		final List<MeasurementVariable> variatesList = this.createVariates();

		final boolean isUsePrevious = true;
		final TabInfo tabInfo = this.openTrialController.prepareMeasurementVariableTabInfo(variatesList, VariableType.TRAIT, isUsePrevious);

		final List<SettingDetail> settings = tabInfo.getSettings();
		Assert.assertEquals("Expecting only traits to be included.", 1, settings.size());
		Assert.assertEquals("Expecting trait alias to be used in Setting Detail.", OpenTrialControllerTest.TRAIT,
			settings.get(0).getVariable().getName());
		Assert.assertEquals("Operation should be ADD", Operation.ADD, settings.get(0).getVariable().getOperation());
		Assert.assertTrue("Setting Detail should be deleteable", settings.get(0).isDeletable());
	}

	@Test
	public void testGetTraitsAndSelectionVariatesWhenTraitIsAdded() {
		final List<SettingDetail> settings =
			Lists.newArrayList(this.createSettingDetail(OpenTrialControllerTest.N_EARS_SEL, OpenTrialControllerTest.TRAIT));
		final List<MeasurementVariable> newVariables = new ArrayList<>();
		final String idList = String.valueOf(OpenTrialControllerTest.N_EARS_SEL);
		Mockito.when(this.userSelection.getBaselineTraitsList()).thenReturn(settings);

		final StandardVariable standardVariable = this.createStandardVariable(OpenTrialControllerTest.N_EARS_SEL);
		standardVariable.setName(OpenTrialControllerTest.VARIABLE_NAME_PREFIX + OpenTrialControllerTest.N_EARS_SEL);
		Mockito.doReturn(standardVariable).when(this.fieldbookMiddlewareService).getStandardVariable(OpenTrialControllerTest.N_EARS_SEL,
			OpenTrialControllerTest.PROGRAM_UUID);

		this.openTrialController.getTraitsAndSelectionVariates(new ArrayList<MeasurementVariable>(), newVariables, idList);
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1)).getStandardVariable(ArgumentMatchers.anyInt(), ArgumentMatchers.anyString());
		// Getting of traits called twice because of null checking
		Mockito.verify(this.userSelection, Mockito.times(2)).getBaselineTraitsList();
		Mockito.verify(this.userSelection, Mockito.times(1)).getSelectionVariates();
		// Verify that trait was added to list of new variables and the alias from Setting Detail was used not the standard name
		Assert.assertEquals(1, newVariables.size());
		Assert.assertEquals(OpenTrialControllerTest.TRAIT, newVariables.get(0).getName());
		Assert.assertEquals(PhenotypicType.VARIATE, newVariables.get(0).getRole());
	}

	@Test
	public void testGetTraitsAndSelectionVariatesWhenTraitIsAddedButNotInUserSelection() {
		final List<MeasurementVariable> newVariables = new ArrayList<>();
		final String idList = String.valueOf(OpenTrialControllerTest.N_EARS_SEL);

		final StandardVariable standardVariable = this.createStandardVariable(OpenTrialControllerTest.N_EARS_SEL);
		standardVariable.setName(OpenTrialControllerTest.VARIABLE_NAME_PREFIX + OpenTrialControllerTest.N_EARS_SEL);
		Mockito.doReturn(standardVariable).when(this.fieldbookMiddlewareService).getStandardVariable(OpenTrialControllerTest.N_EARS_SEL,
			OpenTrialControllerTest.PROGRAM_UUID);

		this.openTrialController.getTraitsAndSelectionVariates(new ArrayList<MeasurementVariable>(), newVariables, idList);
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1)).getStandardVariable(ArgumentMatchers.anyInt(), ArgumentMatchers.anyString());
		Mockito.verify(this.userSelection, Mockito.times(1)).getBaselineTraitsList();
		Mockito.verify(this.userSelection, Mockito.times(1)).getSelectionVariates();
		// Verify that trait was added to list of new variables and the standard name was used
		Assert.assertEquals(1, newVariables.size());
		Assert.assertEquals(OpenTrialControllerTest.VARIABLE_NAME_PREFIX + OpenTrialControllerTest.N_EARS_SEL,
			newVariables.get(0).getName());
		Assert.assertEquals(PhenotypicType.VARIATE, newVariables.get(0).getRole());
	}

	@Test
	public void testGetTraitsAndSelectionVariatesWhenSelectionMethodIsAdded() {
		final List<SettingDetail> settings = Lists
			.newArrayList(this.createSettingDetail(OpenTrialControllerTest.BM_CODE_VTE_ID, OpenTrialControllerTest.SELECTION_TRAIT));
		final List<MeasurementVariable> newVariables = new ArrayList<>();
		final String idList = String.valueOf(OpenTrialControllerTest.BM_CODE_VTE_ID);
		Mockito.when(this.userSelection.getSelectionVariates()).thenReturn(settings);

		final StandardVariable standardVariable = this.createStandardVariable(OpenTrialControllerTest.BM_CODE_VTE_ID);
		standardVariable.setName(OpenTrialControllerTest.VARIABLE_NAME_PREFIX + OpenTrialControllerTest.BM_CODE_VTE_ID);
		Mockito.doReturn(standardVariable).when(this.fieldbookMiddlewareService).getStandardVariable(OpenTrialControllerTest.BM_CODE_VTE_ID,
			OpenTrialControllerTest.PROGRAM_UUID);

		this.openTrialController.getTraitsAndSelectionVariates(new ArrayList<MeasurementVariable>(), newVariables, idList);
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1)).getStandardVariable(ArgumentMatchers.anyInt(), ArgumentMatchers.anyString());
		// Getting of selection methods called twice because of null checking
		Mockito.verify(this.userSelection, Mockito.times(1)).getBaselineTraitsList();
		Mockito.verify(this.userSelection, Mockito.times(2)).getSelectionVariates();
		// Verify that selection method was added to list of new variables and the alias from Setting Detail was used not the standard name
		Assert.assertEquals(1, newVariables.size());
		Assert.assertEquals(OpenTrialControllerTest.SELECTION_TRAIT, newVariables.get(0).getName());
		Assert.assertEquals(PhenotypicType.VARIATE, newVariables.get(0).getRole());
	}

	@Test
	public void testGetTraitsAndSelectionVariatesWhenSelectionMethodIsAddedButNotInUserSelection() {
		final List<MeasurementVariable> newVariables = new ArrayList<>();
		final String idList = String.valueOf(OpenTrialControllerTest.BM_CODE_VTE_ID);

		final StandardVariable standardVariable = this.createStandardVariable(OpenTrialControllerTest.BM_CODE_VTE_ID);
		standardVariable.setName(OpenTrialControllerTest.VARIABLE_NAME_PREFIX + OpenTrialControllerTest.BM_CODE_VTE_ID);
		Mockito.doReturn(standardVariable).when(this.fieldbookMiddlewareService).getStandardVariable(OpenTrialControllerTest.BM_CODE_VTE_ID,
			OpenTrialControllerTest.PROGRAM_UUID);

		this.openTrialController.getTraitsAndSelectionVariates(new ArrayList<MeasurementVariable>(), newVariables, idList);
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1)).getStandardVariable(ArgumentMatchers.anyInt(), ArgumentMatchers.anyString());
		Mockito.verify(this.userSelection, Mockito.times(1)).getBaselineTraitsList();
		Mockito.verify(this.userSelection, Mockito.times(1)).getSelectionVariates();
		// Verify that selection method was added to list of new variables and the standard name was used
		Assert.assertEquals(1, newVariables.size());
		Assert.assertEquals(OpenTrialControllerTest.VARIABLE_NAME_PREFIX + OpenTrialControllerTest.BM_CODE_VTE_ID,
			newVariables.get(0).getName());
		Assert.assertEquals(PhenotypicType.VARIATE, newVariables.get(0).getRole());
	}

	@Test
	public void testGetTraitsAndSelectionVariatesWhenTraitAndSelectionMethodAdded() {
		final List<SettingDetail> traitSettings = Lists
			.newArrayList(this.createSettingDetail(OpenTrialControllerTest.BM_CODE_VTE_ID, OpenTrialControllerTest.SELECTION_TRAIT));
		Mockito.when(this.userSelection.getSelectionVariates()).thenReturn(traitSettings);
		final List<SettingDetail> selectionMethodSettings =
			Lists.newArrayList(this.createSettingDetail(OpenTrialControllerTest.N_EARS_SEL, OpenTrialControllerTest.TRAIT));
		Mockito.when(this.userSelection.getBaselineTraitsList()).thenReturn(selectionMethodSettings);
		final List<MeasurementVariable> newVariables = new ArrayList<>();
		final String idList = OpenTrialControllerTest.BM_CODE_VTE_ID + "," + OpenTrialControllerTest.N_EARS_SEL;

		final StandardVariable standardVariable1 = this.createStandardVariable(OpenTrialControllerTest.BM_CODE_VTE_ID);
		standardVariable1.setName(OpenTrialControllerTest.VARIABLE_NAME_PREFIX + OpenTrialControllerTest.BM_CODE_VTE_ID);
		Mockito.doReturn(standardVariable1).when(this.fieldbookMiddlewareService)
			.getStandardVariable(OpenTrialControllerTest.BM_CODE_VTE_ID, OpenTrialControllerTest.PROGRAM_UUID);
		final StandardVariable standardVariable2 = this.createStandardVariable(OpenTrialControllerTest.N_EARS_SEL);
		standardVariable2.setName(OpenTrialControllerTest.VARIABLE_NAME_PREFIX + OpenTrialControllerTest.N_EARS_SEL);
		Mockito.doReturn(standardVariable2).when(this.fieldbookMiddlewareService).getStandardVariable(OpenTrialControllerTest.N_EARS_SEL,
			OpenTrialControllerTest.PROGRAM_UUID);

		this.openTrialController.getTraitsAndSelectionVariates(new ArrayList<>(), newVariables, idList);
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(2)).getStandardVariable(ArgumentMatchers.anyInt(), ArgumentMatchers.anyString());
		// Getting of trait and selection methods called twice because of null checking
		Mockito.verify(this.userSelection, Mockito.times(2)).getBaselineTraitsList();
		Mockito.verify(this.userSelection, Mockito.times(2)).getSelectionVariates();
		// Verify that trait and selection method was added to list of new variables and the alias from Setting Detail was used not the
		// standard name
		Assert.assertEquals(2, newVariables.size());
		final MeasurementVariable measurementVariable1 = newVariables.get(0);
		Assert.assertEquals(OpenTrialControllerTest.BM_CODE_VTE_ID, measurementVariable1.getTermId());
		Assert.assertEquals(OpenTrialControllerTest.SELECTION_TRAIT, measurementVariable1.getName());
		Assert.assertEquals(PhenotypicType.VARIATE, measurementVariable1.getRole());
		final MeasurementVariable measurementVariable2 = newVariables.get(1);
		Assert.assertEquals(OpenTrialControllerTest.N_EARS_SEL, measurementVariable2.getTermId());
		Assert.assertEquals(OpenTrialControllerTest.TRAIT, measurementVariable2.getName());
		Assert.assertEquals(PhenotypicType.VARIATE, measurementVariable2.getRole());
	}

	@Test
	public void testGetTraitsAndSelectionVariatesWhenIdListCsvIsNullOrEmpty() {
		final List<MeasurementVariable> newVariables = new ArrayList<>();

		this.openTrialController.getTraitsAndSelectionVariates(new ArrayList<MeasurementVariable>(), newVariables, "");
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.never()).getStandardVariable(ArgumentMatchers.anyInt(), ArgumentMatchers.anyString());
		Mockito.verify(this.userSelection, Mockito.never()).getBaselineTraitsList();
		Mockito.verify(this.userSelection, Mockito.never()).getSelectionVariates();
		Assert.assertTrue(newVariables.isEmpty());

		this.openTrialController.getTraitsAndSelectionVariates(new ArrayList<MeasurementVariable>(), newVariables, null);
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.never()).getStandardVariable(ArgumentMatchers.anyInt(), ArgumentMatchers.anyString());
		Mockito.verify(this.userSelection, Mockito.never()).getBaselineTraitsList();
		Mockito.verify(this.userSelection, Mockito.never()).getSelectionVariates();
		Assert.assertTrue(newVariables.isEmpty());
	}

	@Test
	public void testGetTraitsAndSelectionVariatesWhenAllTraitsAlreadyInWorkbook() {
		final List<MeasurementVariable> variablesList = this.createVariates();
		final List<MeasurementVariable> newVariables = new ArrayList<>();
		final String idList = OpenTrialControllerTest.BM_CODE_VTE_ID + "," + OpenTrialControllerTest.N_EARS_SEL;

		this.openTrialController.getTraitsAndSelectionVariates(variablesList, newVariables, idList);
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.never()).getStandardVariable(ArgumentMatchers.anyInt(), ArgumentMatchers.anyString());
		Mockito.verify(this.userSelection, Mockito.times(1)).getBaselineTraitsList();
		Mockito.verify(this.userSelection, Mockito.times(1)).getSelectionVariates();
		// Verify that existing variables are reused
		Assert.assertEquals(variablesList, newVariables);
	}

	private void createTestVariable() {
		this.testVariable = new Variable();
		this.testVariable.setId(UnitTestDaoIDGenerator.generateId(Variable.class));
		this.testVariable.setName(OpenTrialControllerTest.VARIABLE_NAME);
		this.testVariable.setMethod(TestDataHelper.createMethod());
		this.testVariable.setProperty(TestDataHelper.createProperty());
		this.testVariable.setScale(TestDataHelper.createScale());
	}

	private List<MeasurementVariable> createVariates() {
		final List<MeasurementVariable> variables = new ArrayList<>();
		variables.add(this.createMeasurementVariable(OpenTrialControllerTest.BM_CODE_VTE_ID, OpenTrialControllerTest.SELECTION_TRAIT,
			"Breeding Method", "BMETH_CODE", "Observed", "VARIATE", VariableType.SELECTION_METHOD));
		variables.add(this.createMeasurementVariable(OpenTrialControllerTest.N_EARS_SEL, OpenTrialControllerTest.TRAIT, "Number of Ears",
			"Number", "Selected", "VARIATE", VariableType.TRAIT));
		return variables;
	}

	private MeasurementVariable createMeasurementVariable(final int termId, final String name, final String property, final String scale,
		final String method, final String label, final VariableType variableType) {
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(termId);
		measurementVariable.setName(name);
		measurementVariable.setLabel(label);
		measurementVariable.setProperty(property);
		measurementVariable.setScale(scale);
		measurementVariable.setMethod(method);
		measurementVariable.setVariableType(variableType);
		measurementVariable.setRole(variableType.getRole());
		return measurementVariable;
	}

	private SettingDetail createSettingDetail(final Integer id, final String name) {
		final SettingDetail settingDetail = new SettingDetail();
		final SettingVariable settingVariable = new SettingVariable();
		settingVariable.setCvTermId(id);
		settingVariable.setName(name);
		settingDetail.setVariable(settingVariable);

		return settingDetail;
	}


	@Test
	public void testUpdateSavedTrial() throws ParseException {
		final Workbook workbook =
			WorkbookTestDataInitializer.getTestWorkbook(OpenTrialControllerTest.NO_OF_OBSERVATIONS, StudyTypeDto.getTrialDto());
		Mockito.when(this.fieldbookMiddlewareService.getStudyDataSet(OpenTrialControllerTest.STUDY_ID)).thenReturn(workbook);
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(ArgumentMatchers.anyInt(), ArgumentMatchers.anyString()))
			.thenReturn(StandardVariableTestDataInitializer.createStandardVariable(1, "STD"));
		final Study study = new Study();
		study.setStudyType(StudyTypeDto.getTrialDto());

		// Verify that workbook has Analysis and/or Analysis Summary variables
		// beforehand to check that they were later removed
		Assert.assertTrue(this.hasAnalysisVariables(workbook.getConditions()));
		Assert.assertTrue(this.hasAnalysisVariables(workbook.getConstants()));

		final Map<String, Object> resultMap = this.openTrialController.updateSavedTrial(OpenTrialControllerTest.STUDY_ID);
		Assert.assertNotNull(resultMap.get(OpenTrialController.ENVIRONMENT_DATA_TAB));
		Assert.assertNotNull(resultMap.get(OpenTrialController.TRIAL_SETTINGS_DATA));

		Mockito.verify(this.userSelection, Mockito.times(1)).setWorkbook(workbook);
		Mockito.verify(this.userSelection, Mockito.times(1))
			.setExperimentalDesignVariables(WorkbookUtil.getExperimentalDesignVariables(workbook.getConditions()));
		Mockito.verify(this.userSelection, Mockito.times(1))
			.setExpDesignParams(SettingsUtil.convertToExpDesignParamsUi(this.userSelection.getExperimentalDesignVariables()));
	}

	@Test
	public void testAssignOperationOnExpDesignVariablesForExistingTrialWithoutExperimentalDesign() {
		final List<MeasurementVariable> conditions = this.initMeasurementVariableList();

		this.openTrialController.assignOperationOnExpDesignVariables(conditions);

		for (final MeasurementVariable var : conditions) {
			Assert.assertEquals("Expecting that the experimental variable's operation still set to ADD", var.getOperation(), Operation.ADD);
		}
	}

	@Test
	public void testAssignOperationOnExpDesignVariablesForExistingTrialWithExperimentalDesign() {
		final VariableTypeList factors = Mockito.mock(VariableTypeList.class);
		Mockito.when(factors.findById(ArgumentMatchers.anyInt())).thenReturn(new DMSVariableType());
		Mockito.when(this.studyDataManager.getAllStudyFactors(ArgumentMatchers.anyInt())).thenReturn(factors);

		final List<MeasurementVariable> conditions = this.initMeasurementVariableList();
		this.openTrialController.assignOperationOnExpDesignVariables(conditions);

		for (final MeasurementVariable var : conditions) {
			Assert.assertEquals("Expecting that the experimental variable's operation is now set to UPDATE", var.getOperation(),
				Operation.UPDATE);
		}
	}

	@Test
	public void testSubmitWhereReplaceIsZero() {
		final TrialData data = this.setUpTrialData();
		Mockito.when(this.studyDataManager.getStudyTypeByName(Mockito.anyString())).thenReturn(StudyTypeDto.getTrialDto());
		data.setBasicDetails(new BasicDetails());
		data.getBasicDetails().setStudyType(StudyTypeDto.getTrialDto());
		final Map<String, Object> returnVal = this.openTrialController.submit(0, data);

		Assert.assertNotNull("The environment data tab should not be null", returnVal.get(OpenTrialController.ENVIRONMENT_DATA_TAB));
		Mockito.verify(this.fieldbookMiddlewareService)
			.saveWorkbookVariablesAndObservations(ArgumentMatchers.any(Workbook.class));
		Mockito.verify(this.fieldbookService).createIdNameVariablePairs(ArgumentMatchers.any(Workbook.class), ArgumentMatchers.anyListOf(
			SettingDetail.class),
			ArgumentMatchers.anyString(), ArgumentMatchers.anyBoolean());
		Mockito.verify(this.fieldbookService).saveStudyColumnOrdering(ArgumentMatchers.anyInt(), ArgumentMatchers.<String>isNull(),
			ArgumentMatchers.any(Workbook.class));
	}

	private TrialData setUpTrialData() {
		final TrialData data = Mockito.mock(TrialData.class);
		Mockito.when(data.getInstanceInfo()).thenReturn(DesignImportTestDataInitializer.createEnvironmentData(1));
		final BasicDetails basicDetails = Mockito.mock(BasicDetails.class);
		Mockito.when(basicDetails.getBasicDetails()).thenReturn(new HashMap<>());
		Mockito.when(data.getBasicDetails()).thenReturn(basicDetails);
		Mockito.when(data.getBasicDetails().getStudyType()).thenReturn(StudyTypeDto.getTrialDto());
		final TrialSettingsBean trialSettings = Mockito.mock(TrialSettingsBean.class);
		Mockito.when(trialSettings.getUserInput()).thenReturn(new HashMap<>());
		Mockito.when(data.getTrialSettings()).thenReturn(trialSettings);
		final TreatmentFactorTabBean treatmentFactor = Mockito.mock(TreatmentFactorTabBean.class);
		Mockito.when(treatmentFactor.getCurrentData()).thenReturn(new HashMap<>());
		Mockito.when(data.getTreatmentFactors()).thenReturn(treatmentFactor);
		return data;
	}

	@Test
	public void testSetModelAttributes() throws ParseException {
		final Integer trialId = 1010;
		final Workbook testWorkbook = WorkbookTestDataInitializer.getTestWorkbook();
		Mockito.when(this.studyService.studyHasGivenDatasetType(trialId, DatasetTypeEnum.MEANS_DATA.getId())).thenReturn(true);
		final DatasetDTO meansDataset = new DatasetDTO();
		meansDataset.setDatasetId(1);
		meansDataset.setDatasetTypeId(DatasetTypeEnum.MEANS_DATA.getId());
		final DatasetDTO summaryStatisticsDataset = new DatasetDTO();
		summaryStatisticsDataset.setDatasetId(1);
		summaryStatisticsDataset.setDatasetTypeId(DatasetTypeEnum.SUMMARY_STATISTICS_DATA.getId());
		Mockito.when(this.datasetService.getDatasets(trialId, new HashSet<>(DatasetTypeEnum.ANALYSIS_RESULTS_DATASET_IDS)))
			.thenReturn(Arrays.asList(meansDataset, summaryStatisticsDataset));
		this.openTrialController.setModelAttributes(this.createTrialForm, trialId, this.model, testWorkbook);
		Mockito.verify(this.model).addAttribute(ArgumentMatchers.eq("basicDetailsData"), ArgumentMatchers.any(TabInfo.class));
		Mockito.verify(this.model).addAttribute(ArgumentMatchers.eq("germplasmData"), ArgumentMatchers.any(TabInfo.class));
		Mockito.verify(this.model).addAttribute(ArgumentMatchers.eq(OpenTrialController.ENVIRONMENT_DATA_TAB), ArgumentMatchers.any(TabInfo.class));
		Mockito.verify(this.model).addAttribute(ArgumentMatchers.eq(OpenTrialController.TRIAL_SETTINGS_DATA),
			ArgumentMatchers.any(TabInfo.class));
		Mockito.verify(this.model).addAttribute(ArgumentMatchers.eq("experimentalDesignData"), ArgumentMatchers.any(TabInfo.class));
		Mockito.verify(this.model).addAttribute(ArgumentMatchers.eq("treatmentFactorsData"), ArgumentMatchers.any(TabInfo.class));
		Mockito.verify(this.model).addAttribute(ArgumentMatchers.eq("studyTypes"), ArgumentMatchers.anyListOf(StudyType.class));
		Mockito.verify(this.model).addAttribute("createTrialForm", this.createTrialForm);
		Mockito.verify(this.model).addAttribute(ArgumentMatchers.eq("experimentalDesignSpecialData"), ArgumentMatchers.any(TabInfo.class));
		Mockito.verify(this.model).addAttribute("studyName", testWorkbook.getStudyDetails().getLabel());
		Mockito.verify(this.model).addAttribute("description", testWorkbook.getStudyDetails().getDescription());
		Mockito.verify(this.model).addAttribute(ArgumentMatchers.eq("sampleList"), ArgumentMatchers.anyListOf(SampleListDTO.class));
		Mockito.verify(this.model).addAttribute("germplasmListSize", 0);
		Mockito.verify(this.model).addAttribute("meansDatasetId", meansDataset.getDatasetId());
		Mockito.verify(this.model).addAttribute("summaryStatisticsDatasetId", summaryStatisticsDataset.getDatasetId());
		Mockito.verify(this.model).addAttribute(ArgumentMatchers.eq("isSuperAdmin"), ArgumentMatchers.anyBoolean());
	}

	@Test
	public void testPrepareBasicDetailsTabInfo() throws ParseException {
		final int trialID = 1011;
		final StudyDetails studyDetails = this.createTestStudyDetails(trialID);
		final String startDate = Util.convertDate(studyDetails.getStartDate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT);
		final String endDate = Util.convertDate(studyDetails.getEndDate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT);
		final String updateDate = Util.convertDate(studyDetails.getStudyUpdate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT);
		final String ownerName = RandomStringUtils.randomAlphanumeric(20);
		final String folderName = RandomStringUtils.randomAlphanumeric(20);
		Mockito.doReturn(folderName).when(this.fieldbookMiddlewareService).getFolderNameById(ArgumentMatchers.anyInt());
		Mockito.doReturn(ownerName).when(this.userService).getPersonNameForUserId(ArgumentMatchers.anyInt());

		final TabInfo tabInfo = this.openTrialController.prepareBasicDetailsTabInfo(studyDetails, false, trialID);
		final BasicDetails basicData = (BasicDetails) tabInfo.getData();
		Assert.assertNotNull(basicData);
		this.verifyBasicDetailsInfo(studyDetails, startDate, endDate, updateDate, Integer.valueOf(studyDetails.getCreatedBy()), ownerName,
			folderName, basicData);

		this.verifyUserSelectionUponBasicDetailsPreparation(studyDetails);
	}

	@Test
	public void testPrepareBasicDetailsTabInfoWithNullDates() throws ParseException {
		final int trialID = 1011;
		final StudyDetails studyDetails = this.createTestStudyDetails(trialID);
		studyDetails.setEndDate(null);
		studyDetails.setStudyUpdate(null);
		final String startDate = Util.convertDate(studyDetails.getStartDate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT);
		final String ownerName = RandomStringUtils.randomAlphanumeric(20);
		final String folderName = RandomStringUtils.randomAlphanumeric(20);
		Mockito.doReturn(folderName).when(this.fieldbookMiddlewareService).getFolderNameById(ArgumentMatchers.anyInt());
		Mockito.doReturn(ownerName).when(this.userService).getPersonNameForUserId(ArgumentMatchers.anyInt());

		final TabInfo tabInfo = this.openTrialController.prepareBasicDetailsTabInfo(studyDetails, false, trialID);
		final BasicDetails basicData = (BasicDetails) tabInfo.getData();
		Assert.assertNotNull(basicData);
		this.verifyBasicDetailsInfo(studyDetails, startDate, StringUtils.EMPTY, StringUtils.EMPTY,
			Integer.valueOf(studyDetails.getCreatedBy()), ownerName, folderName, basicData);

		this.verifyUserSelectionUponBasicDetailsPreparation(studyDetails);
	}

	@Test
	public void testPrepareBasicDetailsTabInfoWithNoCreatorInfo() throws ParseException {
		final int trialID = 1011;
		final StudyDetails studyDetails = this.createTestStudyDetails(trialID);
		studyDetails.setCreatedBy("");
		final String startDate = Util.convertDate(studyDetails.getStartDate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT);
		final String endDate = Util.convertDate(studyDetails.getEndDate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT);
		final String updateDate = Util.convertDate(studyDetails.getStudyUpdate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT);
		final String folderName = RandomStringUtils.randomAlphanumeric(20);
		Mockito.doReturn(folderName).when(this.fieldbookMiddlewareService).getFolderNameById(ArgumentMatchers.anyInt());

		final TabInfo tabInfo = this.openTrialController.prepareBasicDetailsTabInfo(studyDetails, false, trialID);
		final BasicDetails basicData = (BasicDetails) tabInfo.getData();
		Assert.assertNotNull(basicData);
		this.verifyBasicDetailsInfo(studyDetails, startDate, endDate, updateDate, null, StringUtils.EMPTY, folderName, basicData);

		this.verifyUserSelectionUponBasicDetailsPreparation(studyDetails);
	}

	@Test
	public void testPrepareBasicDetailsTabInfoWhenParentFolderIsRootFolder() throws ParseException {
		final int trialID = 1011;
		final StudyDetails studyDetails = this.createTestStudyDetails(trialID);
		studyDetails.setParentFolderId(DmsProject.SYSTEM_FOLDER_ID);
		final String startDate = Util.convertDate(studyDetails.getStartDate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT);
		final String endDate = Util.convertDate(studyDetails.getEndDate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT);
		final String updateDate = Util.convertDate(studyDetails.getStudyUpdate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT);
		final String ownerName = RandomStringUtils.randomAlphanumeric(20);
		Mockito.doReturn(ownerName).when(this.userService).getPersonNameForUserId(ArgumentMatchers.anyInt());

		final TabInfo tabInfo = this.openTrialController.prepareBasicDetailsTabInfo(studyDetails, false, trialID);
		final BasicDetails basicData = (BasicDetails) tabInfo.getData();
		Assert.assertNotNull(basicData);
		this.verifyBasicDetailsInfo(studyDetails, startDate, endDate, updateDate, Integer.valueOf(studyDetails.getCreatedBy()), ownerName,
			AppConstants.STUDIES.getString(), basicData);

		this.verifyUserSelectionUponBasicDetailsPreparation(studyDetails);
	}

	@Test
	public void testDetectValueChangesInVariables_ValueChangedOnMultipleVariables() {

		final Random random = new Random();
		final int stockId = random.nextInt();
		final int geoLocationId = random.nextInt();
		final int variableId1 = random.nextInt();
		final int variableId2 = random.nextInt();
		final String variableName1 = "SomeVariable1";
		final String variableName2 = "SomeVariable2";
		final MeasurementRow measurementRowWithOldData = new MeasurementRow(stockId, geoLocationId,
			Arrays.asList(MeasurementDataTestDataInitializer.createMeasurementData(variableId1, variableName1, "123"),
				MeasurementDataTestDataInitializer.createMeasurementData(variableId2, variableName2, "234")));
		final MeasurementRow measurementRowWithNewData = new MeasurementRow(stockId, geoLocationId,
			Arrays.asList(MeasurementDataTestDataInitializer.createMeasurementData(variableId1, variableName1, "1"),
				MeasurementDataTestDataInitializer.createMeasurementData(variableId2, variableName2, "2")));

		final Map<Integer, List<Integer>> result =
			this.openTrialController
				.detectValueChangesInVariables(Arrays.asList(measurementRowWithOldData), Arrays.asList(measurementRowWithNewData));

		Assert.assertTrue(result.containsKey(geoLocationId));
		Assert.assertTrue(result.get(geoLocationId).contains(variableId1));
		Assert.assertTrue(result.get(geoLocationId).contains(variableId2));
	}

	@Test
	public void testGetExperimentalDesignName() {
		final Term term = new Term(TermId.RANDOMIZED_COMPLETE_BLOCK.getId(), "RCBD", "RCBD");
		Mockito.when(this.termDataManager.getTermById((term.getId()))).thenReturn(term);

		final Map<String, Object> map = this.openTrialController.getExperimentalDesignName(TermId.RANDOMIZED_COMPLETE_BLOCK.getId());
		Assert.assertEquals(term.getName(), map.get("name").toString());
	}

	@Test
	public void testDetectValueChangesInVariables_NoValueChanged() {

		final Random random = new Random();
		final int stockId = random.nextInt();
		final int geoLocationId = random.nextInt();
		final int variableId1 = random.nextInt();
		final int variableId2 = random.nextInt();
		final String variableName1 = "SomeVariable1";
		final String variableName2 = "SomeVariable2";
		final MeasurementRow measurementRowWithOldData = new MeasurementRow(stockId, geoLocationId,
			Arrays.asList(MeasurementDataTestDataInitializer.createMeasurementData(variableId1, variableName1, "123"),
				MeasurementDataTestDataInitializer.createMeasurementData(variableId2, variableName2, "234")));
		final MeasurementRow measurementRowWithNewData = new MeasurementRow(stockId, geoLocationId,
			Arrays.asList(MeasurementDataTestDataInitializer.createMeasurementData(variableId1, variableName1, "123"),
				MeasurementDataTestDataInitializer.createMeasurementData(variableId2, variableName2, "234")));

		final Map<Integer, List<Integer>> result =
			this.openTrialController
				.detectValueChangesInVariables(Arrays.asList(measurementRowWithOldData), Arrays.asList(measurementRowWithNewData));

		Assert.assertFalse(result.containsKey(geoLocationId));
	}

	@Test
	public void testSetNonStudyVariablesOperationToNull() {
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final MeasurementVariable studyVariable = MeasurementVariableTestDataInitializer.createMeasurementVariableWithOperation(TermId.SITE_NAME.getId(), TermId.SITE_NAME.name(), "SITE NAME", Operation.ADD);
		studyVariable.setRole(PhenotypicType.STUDY);
		measurementVariables.add(studyVariable);
		final MeasurementVariable locationVariable = MeasurementVariableTestDataInitializer.createMeasurementVariableWithOperation(TermId.LOCATION_ID.getId(), TermId.LOCATION_ID.name(), "SITE NAME", Operation.ADD);
		locationVariable.setRole(PhenotypicType.TRIAL_ENVIRONMENT);
		measurementVariables.add(locationVariable);
		this.openTrialController.setNonStudyVariablesOperationToNull(measurementVariables);
		Assert.assertEquals(Operation.ADD, measurementVariables.get(0).getOperation());
		Assert.assertNull(measurementVariables.get(1).getOperation());
	}

	private void verifyUserSelectionUponBasicDetailsPreparation(final StudyDetails studyDetails) {
		Mockito.verify(this.userSelection).setBasicDetails(ArgumentMatchers.anyListOf(SettingDetail.class));
		Mockito.verify(this.userSelection).setStudyName(studyDetails.getStudyName());
		Mockito.verify(this.userSelection).setStudyDescription(studyDetails.getDescription());
		Mockito.verify(this.userSelection).setStudyStartDate(studyDetails.getStartDate());
		Mockito.verify(this.userSelection).setStudyEndDate(studyDetails.getEndDate());
		Mockito.verify(this.userSelection).setStudyUpdate(studyDetails.getStudyUpdate());
		Mockito.verify(this.userSelection).setStudyObjective(studyDetails.getObjective());
		Mockito.verify(this.userSelection).setStudyType(studyDetails.getStudyType().getName());
	}

	private void verifyBasicDetailsInfo(final StudyDetails studyDetails, final String startDate, final String endDate,
		final String updateDate, final Integer userId, final String ownerName, final String folderName, final BasicDetails basicData) {
		// for this test asserting empty as all required fields are present
		Assert.assertTrue(basicData.getBasicDetails().isEmpty());
		Assert.assertEquals(studyDetails.getId(), basicData.getStudyID());
		Assert.assertEquals(studyDetails.getStudyName(), basicData.getStudyName());
		Assert.assertEquals(studyDetails.getDescription(), basicData.getDescription());
		Assert.assertEquals(studyDetails.getObjective(), basicData.getObjective());
		Assert.assertEquals(startDate, basicData.getStartDate());
		Assert.assertEquals(endDate, basicData.getEndDate());
		Assert.assertEquals(updateDate, basicData.getStudyUpdate());
		Assert.assertEquals(studyDetails.getStudyType(), basicData.getStudyType());
		Assert.assertEquals(studyDetails.getParentFolderId(), basicData.getFolderId().longValue());
		Assert.assertEquals(folderName, basicData.getFolderName());
		Assert.assertEquals(folderName, basicData.getFolderNameLabel());
		Assert.assertEquals(userId, basicData.getUserID());
		Assert.assertEquals(ownerName, basicData.getUserName());
		Assert.assertTrue(basicData.getIsLocked());
	}

	private StudyDetails createTestStudyDetails(final int trialID) {
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(trialID);
		studyDetails.setStudyName(RandomStringUtils.randomAlphanumeric(20));
		studyDetails.setDescription(RandomStringUtils.randomAlphanumeric(20));
		studyDetails.setObjective(RandomStringUtils.randomAlphanumeric(20));
		studyDetails.setStartDate("20110915");
		studyDetails.setEndDate("20140421");
		studyDetails.setStudyUpdate("20160601");
		studyDetails.setStudyType(StudyTypeDto.getNurseryDto());
		studyDetails.setIsLocked(true);
		final int parentFolderId = 125;
		studyDetails.setParentFolderId(parentFolderId);
		final String createdBy = "210";
		studyDetails.setCreatedBy(createdBy);
		return studyDetails;
	}

	private List<MeasurementVariable> initMeasurementVariableList() {
		final List<MeasurementVariable> conditions = new ArrayList<MeasurementVariable>();
		conditions.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), "10110"));
		conditions
			.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.EXPT_DESIGN_SOURCE.getId(), "SampleFile.csv"));
		conditions.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.NUMBER_OF_REPLICATES.getId(), "2"));
		conditions.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.PERCENTAGE_OF_REPLICATION.getId(), "50"));

		for (final MeasurementVariable var : conditions) {
			var.setOperation(Operation.ADD);
		}
		return conditions;
	}
}
