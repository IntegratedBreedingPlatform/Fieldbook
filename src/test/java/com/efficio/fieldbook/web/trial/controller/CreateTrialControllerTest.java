
package com.efficio.fieldbook.web.trial.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpSession;

import com.efficio.fieldbook.service.FieldbookServiceImpl;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.api.file.FileMetadataService;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.ErrorCode;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.dms.StudyType;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.bean.Instance;
import com.efficio.fieldbook.web.trial.bean.InstanceInfo;
import com.efficio.fieldbook.web.trial.bean.TabInfo;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import org.generationcp.commons.constant.AppConstants;

public class CreateTrialControllerTest {

	private static final String PROGRAM_UUID = RandomStringUtils.randomAlphabetic(20);

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private UserSelection userSelection;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private LocationDataManager locationDataManager;

	@Mock
	private ErrorHandlerService errorHandlerService;

	@Mock
	private FileMetadataService fileMetadataService;

	@InjectMocks
	private CreateTrialController controller;

	private Integer defaultLocationId;

	@Mock
	private FieldbookServiceImpl fieldbookServiceImpl;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		Mockito.doReturn(CreateTrialControllerTest.PROGRAM_UUID).when(this.contextUtil).getCurrentProgramUUID();
		this.defaultLocationId = Math.abs(new Random().nextInt());
		Mockito.doReturn(Arrays.asList(new Location(this.defaultLocationId))).when(this.locationDataManager)
				.getLocationsByName(Location.UNSPECIFIED_LOCATION, Operation.EQUAL);

	}

	@Test
	public void testUseExistingStudyWithError() {
		Mockito.when(this.fieldbookMiddlewareService.getStudyDataSet(1))
				.thenThrow(new MiddlewareQueryException(ErrorCode.STUDY_FORMAT_INVALID.getCode(), "The term you entered is invalid"));

		final Map<String, Object> tabDetails = this.controller.getExistingTrialDetails(1);

		Assert.assertNotNull("Expecting error but did not get one", tabDetails.get("createTrialForm"));

		final CreateTrialForm form = (CreateTrialForm) tabDetails.get("createTrialForm");
		Assert.assertTrue("Expecting error but did not get one", form.isHasError());
	}

	@Test
	public void testUseExistingStudyWithAnalysisAndAnalysisSummaryVariables() {
		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook(true);
		WorkbookTestDataInitializer.setTrialObservations(workbook);
		Mockito.doReturn(workbook).when(this.fieldbookMiddlewareService).getStudyDataSet(1);
		this.mockStandardVariables(workbook.getAllVariables());

		// Verify that workbook has Analysis and/or Analysis Summary variables beforehand to check that they were later removed
		Assert.assertTrue(this.hasAnalysisVariables(workbook.getConditions()));
		Assert.assertTrue(this.hasAnalysisVariables(workbook.getConstants()));

		final Map<String, Object> tabDetails = this.controller.getExistingTrialDetails(1);
		boolean analysisVariableFound = false;
		for (final String tab : tabDetails.keySet()) {
			final Object tabDetail = tabDetails.get(tab);
			if (tabDetail instanceof TabInfo) {
				final TabInfo tabInfo = (TabInfo) tabDetail;
				final List<SettingDetail> detailList = this.getSettingDetails(tabInfo);
				if (detailList == null) {
					continue;
				}
				for (final SettingDetail settingDetail : detailList) {
					if (VariableType.getReservedVariableTypes().contains(settingDetail.getVariableType())) {
						analysisVariableFound = true;
						break;
					}
				}
			}
		}
		Assert.assertFalse("'Analysis' and 'Analysis Summary' variables should not be included.", analysisVariableFound);
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

	private void mockStandardVariables(final List<MeasurementVariable> allVariables) {
		for (final MeasurementVariable measurementVariable : allVariables) {
			Mockito.doReturn(this.createStandardVariable(measurementVariable.getTermId())).when(this.fieldbookMiddlewareService)
					.getStandardVariable(measurementVariable.getTermId(), CreateTrialControllerTest.PROGRAM_UUID);
		}
	}

	private StandardVariable createStandardVariable(final Integer id) {
		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(id);
		return standardVariable;
	}

	@SuppressWarnings("unchecked")
	private List<SettingDetail> getSettingDetails(final TabInfo tabInfo) {
		final List<SettingDetail> detailList = new ArrayList<>();
		if (tabInfo.getSettings() != null && !tabInfo.getSettings().isEmpty()) {
			return tabInfo.getSettings();
		}
		if (tabInfo.getSettingMap() != null && !tabInfo.getSettingMap().isEmpty() && tabInfo.getSettingMap().values() != null
				&& !tabInfo.getSettingMap().values().isEmpty()) {
			if (tabInfo.getSettingMap().containsKey("managementDetails")) {
				detailList.addAll((List<SettingDetail>) tabInfo.getSettingMap().get("managementDetails"));
			} else if (tabInfo.getSettingMap().containsKey("trialConditionDetails")) {
				detailList.addAll((List<SettingDetail>) tabInfo.getSettingMap().get("trialConditionDetails"));
			} else if (tabInfo.getSettingMap().containsKey("details")) {
				detailList.addAll((List<SettingDetail>) tabInfo.getSettingMap().get("details"));
			} else if (tabInfo.getSettingMap().containsKey("treatmentLevelPairs")) {
				final Map<String, List<SettingDetail>> treatmentFactorPairs =
						(Map<String, List<SettingDetail>>) tabInfo.getSettingMap().get("details");
				for (final List<SettingDetail> settingDetails : treatmentFactorPairs.values()) {
					detailList.addAll(settingDetails);
				}
			}
		}

		return detailList;
	}

	@Test
	public void testRequiredExpDesignVar() {

		Assert.assertTrue("Expected term to be in the required var list but did not found it.",
				this.controller.inRequiredExpDesignVar(TermId.PLOT_NO.getId()));
		Assert.assertTrue("Expected term to be in the required var list but did not found it.",
				this.controller.inRequiredExpDesignVar(TermId.REP_NO.getId()));
		Assert.assertTrue("Expected term to be in the required var list but did not found it.",
				this.controller.inRequiredExpDesignVar(TermId.BLOCK_NO.getId()));
		Assert.assertTrue("Expected term to be in the required var list but did not found it.",
				this.controller.inRequiredExpDesignVar(TermId.ROW.getId()));
		Assert.assertTrue("Expected term to be in the required var list but did not found it.",
				this.controller.inRequiredExpDesignVar(TermId.COL.getId()));
		Assert.assertFalse("Expected term to NOT be in the required var list but did not found it.",
				this.controller.inRequiredExpDesignVar(TermId.LOCATION_ID.getId()));
	}

	@Test
	public void testShow() {
		final CreateTrialController spy = Mockito.spy(new CreateTrialController());
		final StudyDataManager studyDataManager = Mockito.mock(StudyDataManager.class);
		spy.setStudyDataManager(studyDataManager);
		Mockito.doReturn(new TabInfo()).when(spy).prepareBasicDetailsTabInfo();
		Mockito.doReturn(new TabInfo()).when(spy).prepareGermplasmTabInfo(false);
		Mockito.doReturn(new TabInfo()).when(spy).prepareEnvironmentsTabInfo(false);
		Mockito.doReturn(new TabInfo()).when(spy).prepareTrialSettingsTabInfo();
		Mockito.doReturn(new TabInfo()).when(spy).prepareExperimentalDesignSpecialData();
		final Model model = Mockito.mock(Model.class);
		final CreateTrialForm form = Mockito.mock(CreateTrialForm.class);
		final HttpSession session = Mockito.mock(HttpSession.class);
		spy.show(form, model, session);

		// Verify model attributes
		Mockito.verify(model).addAttribute(ArgumentMatchers.eq("basicDetailsData"), ArgumentMatchers.any(TabInfo.class));
		Mockito.verify(model).addAttribute(ArgumentMatchers.eq("germplasmData"), ArgumentMatchers.any(TabInfo.class));
		Mockito.verify(model).addAttribute(ArgumentMatchers.eq(CreateTrialController.ENVIRONMENT_DATA_TAB), ArgumentMatchers.any(TabInfo.class));
		Mockito.verify(model).addAttribute(ArgumentMatchers.eq(CreateTrialController.TRIAL_SETTINGS_DATA_TAB), ArgumentMatchers.any(TabInfo.class));
		Mockito.verify(model).addAttribute(ArgumentMatchers.eq("experimentalDesignSpecialData"), ArgumentMatchers.any(TabInfo.class));
		Mockito.verify(model).addAttribute(ArgumentMatchers.eq("studyTypes"), ArgumentMatchers.anyListOf(StudyType.class));
		Mockito.verify(model).addAttribute("createTrialForm", form);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPrepareEnvironmentsTabInfo() {
		Mockito.doReturn(this.createStandardVariable(new Random().nextInt())).when(this.fieldbookMiddlewareService)
				.getStandardVariable(ArgumentMatchers.anyInt(), ArgumentMatchers.eq(CreateTrialControllerTest.PROGRAM_UUID));

		final TabInfo tabInfo = this.controller.prepareEnvironmentsTabInfo(true);
		final InstanceInfo instanceInfo = (InstanceInfo) tabInfo.getData();
		final int environmentCount = Integer.parseInt(AppConstants.DEFAULT_NO_OF_ENVIRONMENT_COUNT.getString());
		Assert.assertEquals(environmentCount, instanceInfo.getNumberOfInstances());
		Assert.assertEquals(environmentCount, instanceInfo.getInstances().size());
		for (final Instance instance : instanceInfo.getInstances()) {
			Assert.assertNotNull(instance.getManagementDetailValues());
			Assert.assertEquals(String.valueOf(this.defaultLocationId),
					instance.getManagementDetailValues().get(String.valueOf(TermId.LOCATION_ID.getId())));
		}
		Assert.assertNotNull(tabInfo.getSettingMap());
		Assert.assertNotNull(tabInfo.getSettingMap().get("trialConditionDetails"));
		final List<SettingDetail> mgtDetailsList = (List<SettingDetail>) tabInfo.getSettingMap().get("managementDetails");
		Assert.assertNotNull(mgtDetailsList);
		Assert.assertEquals(AppConstants.CREATE_STUDY_ENVIRONMENT_REQUIRED_FIELDS.getString().split(",").length, mgtDetailsList.size());
		Mockito.verify(this.userSelection).setTrialLevelVariableList(mgtDetailsList);
	}

	@Test
	public void testPrepareEnvironmentsTabInfoNoDefaultLocation() {
		Mockito.doReturn(this.createStandardVariable(new Random().nextInt())).when(this.fieldbookMiddlewareService)
			.getStandardVariable(ArgumentMatchers.anyInt(), ArgumentMatchers.eq(CreateTrialControllerTest.PROGRAM_UUID));
		Mockito.doReturn(new ArrayList<>()).when(this.locationDataManager)
			.getLocationsByName(Location.UNSPECIFIED_LOCATION, Operation.EQUAL);

		final TabInfo tabInfo = this.controller.prepareEnvironmentsTabInfo(true);
		final InstanceInfo instanceInfo = (InstanceInfo) tabInfo.getData();
		final int environmentCount = Integer.parseInt(AppConstants.DEFAULT_NO_OF_ENVIRONMENT_COUNT.getString());
		Assert.assertEquals(environmentCount, instanceInfo.getNumberOfInstances());
		Assert.assertEquals(environmentCount, instanceInfo.getInstances().size());
		for (final Instance instance : instanceInfo.getInstances()) {
			Assert.assertTrue(instance.getManagementDetailValues().isEmpty());
		}
		Assert.assertNotNull(tabInfo.getSettingMap());
		Assert.assertNotNull(tabInfo.getSettingMap().get("trialConditionDetails"));
		final List<SettingDetail> mgtDetailsList = (List<SettingDetail>) tabInfo.getSettingMap().get("managementDetails");
		Assert.assertNotNull(mgtDetailsList);
		Assert.assertEquals(AppConstants.CREATE_STUDY_ENVIRONMENT_REQUIRED_FIELDS.getString().split(",").length, mgtDetailsList.size());
		Mockito.verify(this.userSelection).setTrialLevelVariableList(mgtDetailsList);
	}

	@Test
	public void testPrepareEnvironmentsTabInfoFromExistingStudyWithNoEnvironment() {
		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook(false);
		// Assert that there are no trial environments specified in test workbook. But still expect a default one to be created
		Assert.assertEquals(0, workbook.getTrialObservations().size());

		final TabInfo tabInfo = this.controller.prepareEnvironmentsTabInfo(workbook, true);
		final InstanceInfo instanceInfo = (InstanceInfo) tabInfo.getData();
		Assert.assertEquals(1, instanceInfo.getNumberOfInstances());
		Assert.assertEquals(1, instanceInfo.getInstances().size());
		final Instance instance = instanceInfo.getInstances().get(0);
		Assert.assertNotNull(instance.getManagementDetailValues());
		Assert.assertEquals(String.valueOf(this.defaultLocationId),
				instance.getManagementDetailValues().get(String.valueOf(TermId.LOCATION_ID.getId())));
	}

	@Test
	public void testPairVariableEnvironmentTab(){

		final UserSelection userSelection = new UserSelection();
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, new StudyTypeDto("N"));
		final List<MeasurementVariable> conditions = this.getConditions(TermId.PI_ID.getId(), Operation.ADD, PhenotypicType.STUDY);
		conditions.addAll(this.getConditions(TermId.PI_NAME.getId(), Operation.ADD, PhenotypicType.STUDY));
		workbook.setConditions(conditions);
		userSelection.setWorkbook(workbook);
		this.fieldbookServiceImpl.createIdNameVariablePairs(userSelection.getWorkbook(), new ArrayList<>(),
				AppConstants.ID_NAME_COMBINATION.getString(), true);
		final TabInfo tabInfo = this.controller.prepareEnvironmentsTabInfo(workbook, true);
		final List<SettingDetail> managementDetails = (List<SettingDetail>) tabInfo.getSettingMap().get("managementDetails");
		Assert.assertEquals(1, managementDetails.size());
	}

	private MeasurementVariable getMeasurementVariableForCategoricalVariable(
		final int termId, final Operation operation, final PhenotypicType role) {
		final MeasurementVariable variable =
				new MeasurementVariable(termId, "PI_ID", "TRIAL NUMBER", WorkbookDataUtil.NUMBER,
						WorkbookDataUtil.ENUMERATED, WorkbookDataUtil.TRIAL_INSTANCE, WorkbookDataUtil.NUMERIC, "", WorkbookDataUtil.TRIAL);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		variable.setPossibleValues(this.getValueReferenceList());
		variable.setRole(role);
		variable.setOperation(operation);
		return variable;
	}


	private List<ValueReference> getValueReferenceList() {
		final List<ValueReference> possibleValues = new ArrayList<>();

		for (int i = 0; i < 5; i++) {
			final ValueReference possibleValue = new ValueReference(i, String.valueOf(i));
			possibleValues.add(possibleValue);
		}
		return possibleValues;
	}

	private List<MeasurementVariable> getConditions(final int cvtermId, final Operation operation, final PhenotypicType roleId){
		final List<MeasurementVariable> conditionsList = new ArrayList<>();
		conditionsList.add(this.getMeasurementVariableForCategoricalVariable(cvtermId, operation, roleId));
		return  conditionsList;

	}
}
