
package com.efficio.fieldbook.web.trial.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.StandardVariableInitializer;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.DesignTypeItem;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchRuntimeData;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.utils.test.UnitTestDaoIDGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.TestDataHelper;
import com.efficio.fieldbook.web.trial.bean.AdvanceList;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.TabInfo;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.util.SessionUtility;

@RunWith(MockitoJUnitRunner.class)
public class OpenTrialControllerTest {

	private static final int NO_OF_TRIAL_INSTANCES = 3;
	private static final int NO_OF_OBSERVATIONS = 5;
	private static final int TRIAL_ID = 1;
	private static final int WORKBENCH_USER_ID = 1;
	private static final long WORKBENCH_PROJECT_ID = 1L;
	private static final String WORKBENCH_PROJECT_NAME = "Project 1";
	private static final int IBDB_USER_ID = 1;
	private static final String PROGRAM_UUID = "68f0d114-5b5b-11e5-885d-feff819cdc9f";
	public static final String TEST_TRIAL_NAME = "dummyTrial";
	private static final int BM_CODE_VTE = 8252;

	@Mock
	private HttpServletRequest httpRequest;

	@Mock
	private HttpSession httpSession;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@Mock
	private WorkbenchService workbenchService;

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
	private OntologyVariableDataManager variableDataManager;

	@InjectMocks
	private OpenTrialController openTrialController;

	@Before
	public void setUp() {

		final Project project = this.createProject();
		final DmsProject dmsProject = this.createDmsProject();
		final WorkbenchRuntimeData workbenchRuntimeData = new WorkbenchRuntimeData();
		workbenchRuntimeData.setUserId(OpenTrialControllerTest.WORKBENCH_USER_ID);

		Mockito.when(this.workbenchService.getCurrentIbdbUserId(1L, OpenTrialControllerTest.WORKBENCH_USER_ID)).thenReturn(
				OpenTrialControllerTest.IBDB_USER_ID);
		Mockito.when(this.workbenchDataManager.getWorkbenchRuntimeData()).thenReturn(workbenchRuntimeData);
		Mockito.when(this.workbenchDataManager.getLastOpenedProjectAnyUser()).thenReturn(project);
		Mockito.when(this.studyDataManager.getProject(1)).thenReturn(dmsProject);
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(OpenTrialControllerTest.PROGRAM_UUID);
		final Project testProject = new Project();
		testProject.setProjectId(1L);
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(testProject);

		this.initializeOntology();

	}

	@Test
	public void testOpenTrialNoRedirect() throws Exception {

		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook(OpenTrialControllerTest.NO_OF_OBSERVATIONS, StudyType.T);
		WorkbookTestDataInitializer.setTrialObservations(workbook);

		Mockito.when(this.fieldbookMiddlewareService.getTrialDataSet(OpenTrialControllerTest.TRIAL_ID)).thenReturn(workbook);
		this.mockStandardVariables(workbook.getAllVariables());

		final String out =
				this.openTrialController.openTrial(this.createTrialForm, OpenTrialControllerTest.TRIAL_ID, this.model, this.httpSession,
						this.redirectAttributes);

		Mockito.verify(this.fieldbookMiddlewareService).getTrialDataSet(OpenTrialControllerTest.TRIAL_ID);

		Assert.assertEquals("should return the base angular template", AbstractBaseFieldbookController.ANGULAR_BASE_TEMPLATE_NAME, out);
	}

	@Test
	public void testOpenTrialRedirectForIncompatibleStudy() throws Exception {

		Mockito.when(this.fieldbookMiddlewareService.getTrialDataSet(OpenTrialControllerTest.TRIAL_ID)).thenThrow(
				MiddlewareQueryException.class);

		final String out =
				this.openTrialController.openTrial(this.createTrialForm, OpenTrialControllerTest.TRIAL_ID, this.model, this.httpSession,
						this.redirectAttributes);

		Assert.assertEquals("should redirect to manage trial page", "redirect:" + ManageTrialController.URL, out);

		final ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<String> arg2 = ArgumentCaptor.forClass(String.class);

		Mockito.verify(this.redirectAttributes).addFlashAttribute(arg1.capture(), arg2.capture());
		Assert.assertEquals("value should be redirectErrorMessage", "redirectErrorMessage", arg1.getValue());

	}

	@Test
	public void testSessionClearOnOpenTrial() {

		final MockHttpSession mockSession = new MockHttpSession();

		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook(OpenTrialControllerTest.NO_OF_OBSERVATIONS, StudyType.T);
		WorkbookTestDataInitializer.setTrialObservations(workbook);

		mockSession.setAttribute(SessionUtility.USER_SELECTION_SESSION_NAME, new UserSelection());
		mockSession.setAttribute(SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME, new ArrayList<Integer>());

		try {
			Mockito.when(this.fieldbookMiddlewareService.getTrialDataSet(Matchers.anyInt())).thenReturn(workbook);
			this.mockStandardVariables(workbook.getAllVariables());
			this.openTrialController.openTrial(new CreateTrialForm(), OpenTrialControllerTest.TRIAL_ID, new ExtendedModelMap(),
					mockSession, Mockito.mock(RedirectAttributes.class));
		} catch (final MiddlewareException e) {
			this.handleUnexpectedException(e);
		}

		Assert.assertNull("Controller does not properly reset user selection object on open of trial",
				mockSession.getAttribute(SessionUtility.USER_SELECTION_SESSION_NAME));
		Assert.assertNull("Controller does not properly reset the pagination list selection",
				mockSession.getAttribute(SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME));
	}

	@Test
	public void testHappyPathOpenTrialCheckModelAttributes() {

		final Model model = new ExtendedModelMap();

		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook(OpenTrialControllerTest.NO_OF_OBSERVATIONS, StudyType.T);
		WorkbookTestDataInitializer.setTrialObservations(workbook);

		try {

			Mockito.when(this.fieldbookMiddlewareService.getTrialDataSet(Matchers.anyInt())).thenReturn(workbook);
			this.mockStandardVariables(workbook.getAllVariables());

			this.openTrialController.openTrial(new CreateTrialForm(), OpenTrialControllerTest.TRIAL_ID, model, new MockHttpSession(),
					Mockito.mock(RedirectAttributes.class));

			Assert.assertTrue("Controller does not properly set into the model the data for the basic details",
					model.containsAttribute("basicDetailsData"));

			Assert.assertTrue("Controller does not properly set into the model the data for the germplasm tab",
					model.containsAttribute("germplasmData"));
			Assert.assertTrue("Controller does not properly set into the model the data for the environments tab",
					model.containsAttribute(OpenTrialController.ENVIRONMENT_DATA_TAB));
			Assert.assertTrue("Controller does not properly set into the model the data for the trial settings tab",
					model.containsAttribute("trialSettingsData"));
			Assert.assertTrue("Controller does not properly set into the model the data for the measurements tab",
					model.containsAttribute("measurementsData"));
			Assert.assertTrue("Controller does not properly set into the model the data for the experimental design tab",
					model.containsAttribute("experimentalDesignData"));
			Assert.assertTrue("Controller does not properly set into the model the data for the treatment factors tab",
					model.containsAttribute("treatmentFactorsData"));
			Assert.assertTrue("Controller does not properly set into the model the data for the germplasm list size",
					model.containsAttribute("germplasmListSize"));
			Assert.assertTrue("Controller does not properly set into the model copy of the trial form",
					model.containsAttribute("createNurseryForm"));
			Assert.assertTrue("Controller does not properly set into the model special data required for experimental design tab",
					model.containsAttribute("experimentalDesignSpecialData"));
			Assert.assertTrue("Controller does not properly set into the model the study name", model.containsAttribute("studyName"));
			Assert.assertTrue("Controller does not properly set into the model information on whether trial has measurements or not",
					model.containsAttribute(OpenTrialController.MEASUREMENT_DATA_EXISTING));
			Assert.assertTrue("Controller does not properly set into the model the data for measurement row count",
					model.containsAttribute(OpenTrialController.MEASUREMENT_ROW_COUNT));

			Assert.assertFalse("Analysis variables should not be displayed.", this.hasAnalysisVariables(model));

		} catch (final MiddlewareException e) {
			this.handleUnexpectedException(e);
		}
	}

	private boolean hasAnalysisVariables(final Model model) {
		final List<SettingDetail> settingDetails = this.getSettingDetailsPossiblyWithAnalysisVariables(model);
		boolean analysisVariableFound = false;
		for (final SettingDetail settingDetail : settingDetails) {
			final Integer termId = settingDetail.getVariable().getCvTermId();
			if (WorkbookTestDataInitializer.PLANT_HEIGHT_MEAN_ID == termId
					|| WorkbookTestDataInitializer.PLANT_HEIGHT_UNIT_ERRORS_ID == termId) {
				analysisVariableFound = true;
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
		final List<SettingDetail> trialConditionsList =
				(List<SettingDetail>) experimentsDataTabInfo.getSettingMap().get("trialConditionDetails");
		settingDetails.addAll(managementDetailList);
		settingDetails.addAll(trialConditionsList);

		final TabInfo measurementsDataTabInfo = (TabInfo) modelMap.get("measurementsData");
		settingDetails.addAll(measurementsDataTabInfo.getSettings());

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

			Mockito.when(
					this.variableDataManager.getVariable(Mockito.eq(PROGRAM_UUID), Mockito.eq(measurementVariable.getTermId()),
							Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(variable);
			;
		}
	}

	private StandardVariable createStandardVariable(final Integer id) {
		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(id);
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
	public void testGetFilteredTrialObservations() {

		final Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
						OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);

		final List<MeasurementRow> filteredTrialObservations =
				this.openTrialController.getFilteredTrialObservations(workbook.getTrialObservations(), "2");

		Assert.assertEquals("Expecting the number of trial observations is decreased by one.", workbook.getTotalNumberOfInstances() - 1,
				filteredTrialObservations.size());

		// expecting the trial instance no are in incremental order
		Integer trialInstanceNo = 1;
		for (final MeasurementRow row : filteredTrialObservations) {
			final List<MeasurementData> dataList = row.getDataList();
			for (final MeasurementData data : dataList) {
				if (data.getMeasurementVariable() != null) {
					final MeasurementVariable var = data.getMeasurementVariable();

					if (var != null && data.getMeasurementVariable().getName() != null && "TRIAL_INSTANCE".equalsIgnoreCase(var.getName())) {
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
	public void testGetFilteredTrialObservationsWithNoDeletedEnvironmentId() {
		final Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
						OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);

		final List<MeasurementRow> filteredTrialObservations =
				this.openTrialController.getFilteredTrialObservations(workbook.getTrialObservations(), "");

		Assert.assertEquals("Expecting the number of trial observations is the same after the method call.",
				workbook.getTotalNumberOfInstances(), filteredTrialObservations.size());
	}

	@Test
	public void testGetFilteredObservations() {

		final Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
						OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);

		final List<MeasurementRow> filteredObservations = this.openTrialController.getFilteredObservations(workbook.getObservations(), "2");

		Assert.assertEquals("Expecting the number of observations is decreased by " + OpenTrialControllerTest.NO_OF_OBSERVATIONS, workbook
				.getObservations().size() - OpenTrialControllerTest.NO_OF_OBSERVATIONS, filteredObservations.size());

		// expecting the trial instance no are in incremental order
		final Integer noOfTrialInstances = OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES - 1;
		for (final MeasurementRow row : filteredObservations) {
			final List<MeasurementData> dataList = row.getDataList();
			for (final MeasurementData data : dataList) {
				if (data.getMeasurementVariable() != null) {
					final MeasurementVariable var = data.getMeasurementVariable();

					if (var != null && data.getMeasurementVariable().getName() != null && "TRIAL_INSTANCE".equalsIgnoreCase(var.getName())) {
						final Integer currentTrialInstanceNo = Integer.valueOf(data.getValue());
						Assert.assertTrue("Expecting trial instance the next trial instance no is within the "
								+ "possible range of trial instance no but didn't.", currentTrialInstanceNo <= noOfTrialInstances);
					}
				}
			}
		}
	}

	@Test
	public void testGetFilteredObservationsWithNoDeletedEnvironmentId() {
		final Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
						OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);

		final List<MeasurementRow> filteredObservations = this.openTrialController.getFilteredObservations(workbook.getObservations(), "");

		Assert.assertEquals("Expecting the number of observations is the same after the method call.", workbook.getObservations().size(),
				filteredObservations.size());
	}

	protected void handleUnexpectedException(final Exception e) {
		Assert.fail("Unexpected error during unit test : " + e.getMessage());
	}

	protected DmsProject createDmsProject() {
		final DmsProject dmsProject = new DmsProject();
		dmsProject.setProjectId(OpenTrialControllerTest.TRIAL_ID);
		dmsProject.setName(OpenTrialControllerTest.TEST_TRIAL_NAME);
		dmsProject.setProgramUUID(OpenTrialControllerTest.PROGRAM_UUID);
		return dmsProject;
	}

	private Project createProject() {
		final Project project = new Project();
		project.setProjectId(OpenTrialControllerTest.WORKBENCH_PROJECT_ID);
		project.setProjectName(OpenTrialControllerTest.WORKBENCH_PROJECT_NAME);
		return project;
	}

	protected void initializeOntology() {

		final Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
						OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);

		for (final MeasurementVariable mvar : workbook.getAllVariables()) {

			final StandardVariable stdVar = this.convertToStandardVariable(mvar);
			Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(stdVar.getId(), OpenTrialControllerTest.PROGRAM_UUID))
					.thenReturn(stdVar);
		}

		// StudyName
		final StandardVariable studyName =
				this.createStandardVariable(8005, "STUDY_NAME", "Study", "DBCV", "Assigned", 1120, "Character variable", "STUDY");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(8005, OpenTrialControllerTest.PROGRAM_UUID)).thenReturn(studyName);

		// StudyTitle
		final StandardVariable studyTitle =
				this.createStandardVariable(8007, "STUDY_TITLE", "Study title", "Text", "Assigned", 1120, "Character variable", "STUDY");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(8007, OpenTrialControllerTest.PROGRAM_UUID))
				.thenReturn(studyTitle);

		// StudyObjective
		final StandardVariable studyObjective =
				this.createStandardVariable(8030, "STUDY_OBJECTIVE", "Study objective", "Text", "Described", 1120, "Character variable",
						"STUDY");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(8030, OpenTrialControllerTest.PROGRAM_UUID)).thenReturn(
				studyObjective);

		// StartDate
		final StandardVariable startDate =
				this.createStandardVariable(8050, "START_DATE", "Start date", "Date (yyyymmdd)", "Assigned", 1117, "Date variable", "STUDY");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(8050, OpenTrialControllerTest.PROGRAM_UUID)).thenReturn(startDate);

		// EndDate
		final StandardVariable endDate =
				this.createStandardVariable(8060, "END_DATE", "End date", "Date (yyyymmdd)", "Assigned", 1117, "Date variable", "STUDY");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(8060, OpenTrialControllerTest.PROGRAM_UUID)).thenReturn(endDate);

		final StandardVariable plotNo =
				this.createStandardVariable(8200, "PLOT_NO", "Field plot", "Number", "Enumerated", 1110, "Numeric variable", "TRIAL_DESIGN");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(8200, OpenTrialControllerTest.PROGRAM_UUID)).thenReturn(plotNo);

		final StandardVariable repNo =
				this.createStandardVariable(8210, "REP_NO", "Replication factor", "Number", "Enumerated", 1110, "Numeric variable",
						"TRIAL_DESIGN");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(8210, OpenTrialControllerTest.PROGRAM_UUID)).thenReturn(repNo);

		final StandardVariable blockNo =
				this.createStandardVariable(8220, "BLOCK_NO", "Blocking factor", "Number", "Enumerated", 1110, "Numeric variable",
						"TRIAL_DESIGN");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(8220, OpenTrialControllerTest.PROGRAM_UUID)).thenReturn(blockNo);

		final StandardVariable row =
				this.createStandardVariable(8581, "ROW", "Row in layout", "Number", "Enumerated", 1110, "Numeric variable", "TRIAL_DESIGN");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(8581, OpenTrialControllerTest.PROGRAM_UUID)).thenReturn(row);

		final StandardVariable col =
				this.createStandardVariable(8582, "COL", "Column in layout", "Number", "Enumerated", 1110, "Numeric variable",
						"TRIAL_DESIGN");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(8582, OpenTrialControllerTest.PROGRAM_UUID)).thenReturn(col);

	}

	protected StandardVariable convertToStandardVariable(final MeasurementVariable measurementVar) {
		final StandardVariable stdVar =
				this.createStandardVariable(measurementVar.getTermId(), measurementVar.getName(), measurementVar.getProperty(),
						measurementVar.getScale(), measurementVar.getMethod(), measurementVar.getDataTypeId(),
						measurementVar.getDataType(), measurementVar.getLabel());
		return stdVar;
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
		final String nRepValue = "3";
		final String rMapValue = null;
		final Integer replicationsArrangement = null;
		final Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
						OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook, new Integer(TermId.RANDOMIZED_COMPLETE_BLOCK.getId()).toString(),
				exptDesignSourceValue, nRepValue, rMapValue);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be RCBD", DesignTypeItem.RANDOMIZED_COMPLETE_BLOCK.getId().intValue(), data.getDesignType()
				.intValue());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications arrangement should be " + replicationsArrangement, replicationsArrangement,
				data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", "3", data.getBlockSize());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_RCBDWithRMap() {
		final String exptDesignSourceValue = null;
		final String nRepValue = "3";
		final String rMapValue = new Integer(TermId.REPS_IN_SINGLE_COL.getId()).toString();
		final Integer replicationsArrangement = 1;
		final Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
						OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook, new Integer(TermId.RANDOMIZED_COMPLETE_BLOCK.getId()).toString(),
				exptDesignSourceValue, nRepValue, rMapValue);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be RCBD", DesignTypeItem.RANDOMIZED_COMPLETE_BLOCK.getId().intValue(), data.getDesignType()
				.intValue());
		Assert.assertFalse("Design type should not be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications map should be " + replicationsArrangement, replicationsArrangement,
				data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", "3", data.getBlockSize());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_RIBD() {
		final String exptDesignSourceValue = null;
		final String nRepValue = "5";
		final String rMapValue = null;
		final Integer replicationsArrangement = null;
		final Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
						OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook,
				new Integer(TermId.RESOLVABLE_INCOMPLETE_BLOCK.getId()).toString(), exptDesignSourceValue, nRepValue, rMapValue);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be RIBD", DesignTypeItem.RESOLVABLE_INCOMPLETE_BLOCK.getId().intValue(), data
				.getDesignType().intValue());
		Assert.assertFalse("Design type should not be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications arrangement should be " + replicationsArrangement, replicationsArrangement,
				data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", "3", data.getBlockSize());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_RIBDL() {
		final String exptDesignSourceValue = null;
		final String nRepValue = "3";
		final String rMapValue = null;
		final Integer replicationsArrangement = null;
		final Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
						OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook,
				new Integer(TermId.RESOLVABLE_INCOMPLETE_BLOCK_LATIN.getId()).toString(), exptDesignSourceValue, nRepValue, rMapValue);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be RIBDL", DesignTypeItem.RESOLVABLE_INCOMPLETE_BLOCK.getId().intValue(), data
				.getDesignType().intValue());
		Assert.assertTrue("Design type should be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications arrangement should be " + replicationsArrangement, replicationsArrangement,
				data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", "3", data.getBlockSize());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_E30Rep2Block65Ind() {
		final String exptDesignSourceValue = "E30-Rep2-Block6-5Ind.csv";
		final String nRepValue = "2";
		final String rMapValue = null;
		final Integer replicationsArrangement = null;
		final Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
						OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook,
				new Integer(TermId.RESOLVABLE_INCOMPLETE_BLOCK.getId()).toString(), exptDesignSourceValue, nRepValue, rMapValue);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be Alpha Lattice using preset E30-Rep2-Block6-5Ind", 4, data.getDesignType().intValue());
		Assert.assertFalse("Design type should not be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications arrangement should be " + replicationsArrangement, replicationsArrangement,
				data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", "3", data.getBlockSize());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_E30Rep3Block65Ind() {
		final String exptDesignSourceValue = "E30-Rep3-Block6-5Ind.csv";
		final String nRepValue = "3";
		final String rMapValue = null;
		final Integer replicationsArrangement = null;
		final Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
						OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook,
				new Integer(TermId.RESOLVABLE_INCOMPLETE_BLOCK.getId()).toString(), exptDesignSourceValue, nRepValue, rMapValue);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be Alpha Lattice using preset E30-Rep3-Block6-5Ind", 5, data.getDesignType().intValue());
		Assert.assertFalse("Design type should not be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications arrangement should be " + replicationsArrangement, replicationsArrangement,
				data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", "3", data.getBlockSize());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_E50Rep2Block510Ind() {
		final String exptDesignSourceValue = "E50-Rep2-Block5-10Ind.csv";
		final String nRepValue = "2";
		final String rMapValue = null;
		final Integer replicationsArrangement = null;
		final Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
						OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook,
				new Integer(TermId.RESOLVABLE_INCOMPLETE_BLOCK.getId()).toString(), exptDesignSourceValue, nRepValue, rMapValue);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be Alpha Lattice using preset E50-Rep2-Block5-10Ind", 6, data.getDesignType().intValue());
		Assert.assertFalse("Design type should not be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications arrangement should be " + replicationsArrangement, replicationsArrangement,
				data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", "3", data.getBlockSize());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_RRCD() {
		final String exptDesignSourceValue = null;
		final String nRepValue = "5";
		final String rMapValue = null;
		final Integer replicationsArrangement = null;
		final Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
						OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook,
				new Integer(TermId.RESOLVABLE_INCOMPLETE_ROW_COL.getId()).toString(), exptDesignSourceValue, nRepValue, rMapValue);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be RRCD", DesignTypeItem.ROW_COL.getId().intValue(), data.getDesignType().intValue());
		Assert.assertFalse("Design type should not be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications arrangement should be " + replicationsArrangement, replicationsArrangement,
				data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", "3", data.getBlockSize());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_RRCDL() {
		final String exptDesignSourceValue = null;
		final String nRepValue = "3";
		final String rMapValue = null;
		final Integer replicationsArrangement = null;
		final Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
						OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook,
				new Integer(TermId.RESOLVABLE_INCOMPLETE_ROW_COL_LATIN.getId()).toString(), exptDesignSourceValue, nRepValue, rMapValue);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be RRCDL", DesignTypeItem.ROW_COL.getId().intValue(), data.getDesignType().intValue());
		Assert.assertTrue("Design type should be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications arrangement should be " + replicationsArrangement, replicationsArrangement,
				data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", "3", data.getBlockSize());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_OtherDesign() {
		final String exptDesignSourceValue = "Other design.csv";
		final String nRepValue = "2";
		final String rMapValue = null;
		final Integer replicationsArrangement = null;
		final Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
						OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook, new Integer(TermId.OTHER_DESIGN.getId()).toString(),
				exptDesignSourceValue, nRepValue, rMapValue);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertEquals("Design type should be Other Design", DesignTypeItem.CUSTOM_IMPORT.getId().intValue(), data.getDesignType()
				.intValue());
		Assert.assertFalse("Design type should not be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications arrangement should be " + replicationsArrangement, replicationsArrangement,
				data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", "3", data.getBlockSize());
	}

	@Test
	public void testPrepareExperimentalDesignTabInfo_UnknownDesign() {
		final String exptDesignSourceValue = null;
		final String nRepValue = null;
		final String rMapValue = null;
		final Integer replicationsArrangement = null;
		final Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(OpenTrialControllerTest.NO_OF_OBSERVATIONS,
						OpenTrialControllerTest.NO_OF_TRIAL_INSTANCES);
		WorkbookDataUtil.addOrUpdateExperimentalDesignVariables(workbook, "12345", exptDesignSourceValue, nRepValue, rMapValue);
		final TabInfo tabInfo = this.openTrialController.prepareExperimentalDesignTabInfo(workbook, false);
		final ExpDesignParameterUi data = (ExpDesignParameterUi) tabInfo.getData();
		Assert.assertNull("Design type should be unknown", data.getDesignType());
		Assert.assertFalse("Design type should not be latinized", data.getUseLatenized());
		Assert.assertEquals("Source should be " + exptDesignSourceValue, exptDesignSourceValue, data.getFileName());
		Assert.assertEquals("Number of replicates should be " + nRepValue, nRepValue, data.getReplicationsCount());
		Assert.assertEquals("Replications arrangement should be " + replicationsArrangement, replicationsArrangement,
				data.getReplicationsArrangement());
		Assert.assertEquals("Block size should be 3", "3", data.getBlockSize());
	}

	@Test
	public void testPrepareMeasurementVariableTabInfo() {
		final List<MeasurementVariable> variatesList = this.createVariates();

		final Variable variable = new Variable();
		variable.setId(UnitTestDaoIDGenerator.generateId(Variable.class));
		variable.setName("Variable Name");
		variable.setMethod(TestDataHelper.createMethod());
		variable.setProperty(TestDataHelper.createProperty());
		variable.setScale(TestDataHelper.createScale());

		Mockito.when(
				this.variableDataManager.getVariable(Mockito.any(String.class), Mockito.any(Integer.class), Mockito.anyBoolean(),
						Mockito.anyBoolean())).thenReturn(variable);

		final TabInfo tabInfo =
				this.openTrialController.prepareMeasurementVariableTabInfo(variatesList, VariableType.SELECTION_METHOD, false);

		Assert.assertEquals("Operation", Operation.UPDATE, tabInfo.getSettings().get(0).getVariable().getOperation());
		Assert.assertEquals("Deletable", true, tabInfo.getSettings().get(0).isDeletable());
	}

	private List<MeasurementVariable> createVariates() {
		final List<MeasurementVariable> variables = new ArrayList<>();
		variables.add(this.createMeasurementVariable(BM_CODE_VTE, "BM_CODE_VTE", "Breeding Method", "BMETH_CODE", "Observed", "VARIATE"));
		return variables;
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
		measurementVariable.setVariableType(VariableType.SELECTION_METHOD);
		measurementVariable.setRole(VariableType.SELECTION_METHOD.getRole());
		return measurementVariable;
	}

	@Test
	public void testGetAdvancedList() {
		final GermplasmList germplasm = new GermplasmList();
		germplasm.setId(501);
		germplasm.setName("Advance Trial List");

		final List<GermplasmList> germplasmList = new ArrayList<>();
		germplasmList.add(germplasm);

		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListsByProjectId(Mockito.anyInt(), Mockito.any(GermplasmListType.class)))
				.thenReturn(germplasmList);

		final List<AdvanceList> advancedList = this.openTrialController.getAdvancedList(germplasm.getId());

		Assert.assertEquals("Advance List size", 1, advancedList.size());
		Assert.assertEquals("Advance List Id: ", germplasm.getId(), advancedList.get(0).getId());
		Assert.assertEquals("Advance List Name: ", germplasm.getName(), advancedList.get(0).getName());
	}

	@Test
	public void testAssignOperationOnExpDesignVariablesForExistingTrialWithoutExperimentalDesign() {
		final List<MeasurementVariable> conditions = this.initMeasurementVariableList();

		this.openTrialController.assignOperationOnExpDesignVariables(conditions, null);

		for (final MeasurementVariable var : conditions) {
			Assert.assertTrue("Expecting that the experimental variable's operation still set to ADD",
					var.getOperation().equals(Operation.ADD));
		}
	}

	@Test
	public void testAssignOperationOnExpDesignVariablesForExistingTrialWithExperimentalDesign() {
		final List<MeasurementVariable> conditions = this.initMeasurementVariableList();

		final List<StandardVariable> existingExpDesignVariables = this.initExpDesignVariables();

		this.openTrialController.assignOperationOnExpDesignVariables(conditions, existingExpDesignVariables);

		for (final MeasurementVariable var : conditions) {
			Assert.assertTrue("Expecting that the experimental variable's operation is now set to UPDATE",
					var.getOperation().equals(Operation.UPDATE));
		}
	}

	private List<StandardVariable> initExpDesignVariables() {
		final List<StandardVariable> existingExpDesignVariables = new ArrayList<StandardVariable>();
		existingExpDesignVariables
				.add(StandardVariableInitializer.createStdVariable(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), "EXP_DESIGN"));
		existingExpDesignVariables.add(StandardVariableInitializer.createStdVariable(TermId.NUMBER_OF_REPLICATES.getId(), "NREP"));
		existingExpDesignVariables
				.add(StandardVariableInitializer.createStdVariable(TermId.EXPT_DESIGN_SOURCE.getId(), "EXP_DESIGN_SOURCE"));
		return existingExpDesignVariables;
	}

	private List<MeasurementVariable> initMeasurementVariableList() {
		final MeasurementVariableTestDataInitializer measurementVariableInit = new MeasurementVariableTestDataInitializer();
		final List<MeasurementVariable> conditions = new ArrayList<MeasurementVariable>();
		conditions.add(measurementVariableInit.createMeasurementVariable(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), "10110"));
		conditions.add(measurementVariableInit.createMeasurementVariable(TermId.EXPT_DESIGN_SOURCE.getId(), "SampleFile.csv"));
		conditions.add(measurementVariableInit.createMeasurementVariable(TermId.NUMBER_OF_REPLICATES.getId(), "2"));

		for (final MeasurementVariable var : conditions) {
			var.setOperation(Operation.ADD);
		}
		return conditions;
	}
}
