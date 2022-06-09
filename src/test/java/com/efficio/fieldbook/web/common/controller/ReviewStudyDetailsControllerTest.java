/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.StudyDetails;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.security.AuthorizationService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.ErrorCode;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.generationcp.middleware.service.api.user.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ReviewStudyDetailsControllerTest extends AbstractBaseIntegrationTest {

	@Resource
	private ReviewStudyDetailsController reviewStudyDetailsController;

	@Mock
	private FieldbookService fieldbookMWService;

	@Mock
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Mock
	private UserService userService;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private AuthorizationService authorizationService;

	@Mock
	private StudyEntryService studyEntryService;

	private Workbook workbook;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		final Project project = new Project();
		project.setCropType(new CropType("Maize"));
		this.workbook = WorkbookTestDataInitializer.getTestWorkbook(true);
		this.reviewStudyDetailsController.setAuthorizationService(authorizationService);
		this.reviewStudyDetailsController.setFieldbookMiddlewareService(this.fieldbookMWService);
		this.reviewStudyDetailsController.setFieldbookService(this.fieldbookService);
		this.reviewStudyDetailsController.setUserService(this.userService);
		this.reviewStudyDetailsController.setStudyEntryService(this.studyEntryService);
		Mockito.doReturn(this.workbook).when(this.fieldbookMWService).getStudyVariableSettings(ArgumentMatchers.anyInt());
		this.mockStandardVariables(this.workbook.getAllVariables(), this.fieldbookMWService, this.fieldbookService);

		this.reviewStudyDetailsController.setContextUtil(this.contextUtil);
		Mockito.doReturn(this.PROGRAM_UUID).when(this.contextUtil).getCurrentProgramUUID();
		Mockito.doReturn(project).when(this.contextUtil).getProjectInContext();

	}

	@Test
	public void testAddErrorMessageToResultForStudy() {
		final StudyDetails details = new StudyDetails();

		this.reviewStudyDetailsController.addErrorMessageToResult(details,
			new MiddlewareQueryException(ErrorCode.STUDY_FORMAT_INVALID.getCode(), "The term you entered is invalid"), 1);

		Assert.assertEquals("Expecting error message for nursery but got " + details.getErrorMessage() + " instead.",
				"This study is in a format that cannot be opened in the Study Manager. Please use the Study Browser if you"
						+ " wish to see the details of this study.",
				details.getErrorMessage());
	}

	@Test
	public void testShowStudySummaryEnvironmentsWithoutAnalysisVariables() {
		final int id = 1;
		final CreateTrialForm form = new CreateTrialForm();
		final Model model = new ExtendedModelMap();

		// Verify that this.workbook has Analysis and/or Analysis Summary variables beforehand to check that they were later removed
		Assert.assertTrue(this.hasAnalysisVariables(this.workbook.getConditions()));
		Assert.assertTrue(this.hasAnalysisVariables(this.workbook.getConstants()));

		this.reviewStudyDetailsController.show(id, form, model);

		final StudyDetails details = (StudyDetails) model.asMap().get("trialDetails");
		Assert.assertNotNull(details);
		final List<SettingDetail> conditionSettingDetails = details.getStudyConditionDetails();
		boolean hasAnalysisVariable = false;
		for (final SettingDetail settingDetail : conditionSettingDetails) {
			if (VariableType.getReservedVariableTypes().contains(settingDetail.getVariableType())) {
				hasAnalysisVariable = true;
				break;
			}
		}
		Assert.assertFalse("'Analysis' and 'Analysis Summary' variables should not be found under Study Conditions of the Summary page.",
				hasAnalysisVariable);
		Mockito.verify(this.userService).getPersonNameForUserId(NumberUtils.toInt(this.workbook.getStudyDetails().getCreatedBy()));
	}

	@Test
	public void testShowStudySummary() {
		final int id = new Random().nextInt(100);
		final CreateTrialForm form = new CreateTrialForm();
		final Model model = new ExtendedModelMap();
		final List<ValueReference> allEntries = this.getAllEntries(5, 3, 5);
		final List<Integer> checksEntries = this.getAllCheckEntryTypeIds(allEntries);

		Mockito.when(this.fieldbookService.getAllPossibleValues(TermId.ENTRY_TYPE.getId(), true)).thenReturn(allEntries);
		Mockito.doReturn(5L).when(this.studyEntryService).countStudyGermplasmByEntryTypeIds(id, checksEntries);
		Mockito.doReturn(3L).when(this.studyEntryService).countStudyGermplasmByEntryTypeIds(id,
				Collections.singletonList(Integer.valueOf(SystemDefinedEntryType.NON_REPLICATED_ENTRY.getEntryTypeCategoricalId())));
		this.reviewStudyDetailsController.show(id, form, model);

		final StudyDetails details = (StudyDetails) model.asMap().get("trialDetails");
		Assert.assertNotNull(details);
		final Boolean isSuperAdmin =  (Boolean) model.asMap().get("isSuperAdmin");
		Assert.assertNotNull(isSuperAdmin);
		Assert.assertEquals(5L, model.asMap().get("numberOfChecks"));
	}

	@Test
	public void testShowStudySummaryViewTemplateWithNullCreatedBy() {
		final int id = 1;
		final CreateTrialForm form = new CreateTrialForm();
		final Model model = new ExtendedModelMap();

		this.workbook.getStudyDetails().setCreatedBy(null);
		this.reviewStudyDetailsController.show(id, form, model);

		Mockito.verify(this.userService).getPersonNameForUserId(0);

	}

	@Test
	public void testShowStudySummaryWoNonReplicatedCount() {
		final int id = new Random().nextInt(100);
		final CreateTrialForm form = new CreateTrialForm();
		final Model model = new ExtendedModelMap();
		final List<ValueReference> allEntries = this.getAllEntries(5, 0, 5);
		final List<Integer> checksEntries = this.getAllCheckEntryTypeIds(allEntries);

		Mockito.when(this.fieldbookService.getAllPossibleValues(TermId.ENTRY_TYPE.getId(), true)).thenReturn(allEntries);
		Mockito.doReturn(5L).when(this.studyEntryService).countStudyGermplasmByEntryTypeIds(id, checksEntries);

		this.reviewStudyDetailsController.show(id, form, model);

		final StudyDetails details = (StudyDetails) model.asMap().get("trialDetails");
		Assert.assertNotNull(details);
		final Boolean isSuperAdmin =  (Boolean) model.asMap().get("isSuperAdmin");
		Assert.assertNotNull(isSuperAdmin);
		Assert.assertEquals(5L, model.asMap().get("numberOfChecks"));
		Assert.assertNull(model.asMap().get("nonReplicatedEntriesCount"));
	}

	@Test
	public void testSetIsSuperAdminAttributeForNonSuperAdminUser() {
		final Model model = new ExtendedModelMap();

		Mockito.when(authorizationService.isSuperAdminUser()).thenReturn(Boolean.FALSE);
		this.reviewStudyDetailsController.setIsSuperAdminAttribute(model);
		Assert.assertFalse((Boolean)model.asMap().get("isSuperAdmin"));
	}

	@Test
	public void testSetIsSuperAdminAttributeForSuperAdminUser() {
		final Model model = new ExtendedModelMap();

		Mockito.when(authorizationService.isSuperAdminUser()).thenReturn(Boolean.TRUE);

		this.reviewStudyDetailsController.setIsSuperAdminAttribute(model);
		Assert.assertTrue((Boolean)model.asMap().get("isSuperAdmin"));
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

	private void mockStandardVariables(final List<MeasurementVariable> allVariables, final FieldbookService fieldbookMiddlewareService,
			final com.efficio.fieldbook.service.api.FieldbookService fieldbookService) {
		for (final MeasurementVariable measurementVariable : allVariables) {
			final StandardVariable stdVar = this.createStandardVariable(measurementVariable.getTermId(), measurementVariable.getProperty(),
					measurementVariable.getScale(), measurementVariable.getMethod(), measurementVariable.getRole());
			Mockito.doReturn(stdVar).when(fieldbookMiddlewareService).getStandardVariable(measurementVariable.getTermId(),
					this.PROGRAM_UUID);
			Mockito.doReturn(stdVar.getId()).when(fieldbookMiddlewareService).getStandardVariableIdByPropertyScaleMethodRole(
					measurementVariable.getProperty(), measurementVariable.getScale(), measurementVariable.getMethod(),
					measurementVariable.getRole());
			Mockito.when(fieldbookService.getValue(ArgumentMatchers.anyInt(), ArgumentMatchers.anyString(), ArgumentMatchers.anyBoolean())).thenReturn("");
		}
	}

	@Test
	public void testShowStudyPrepDesignSummary() {
		final Workbook wb = this.workbook;
		final MeasurementVariable prepDesign = WorkbookTestDataInitializer.createMeasurementVariable(WorkbookTestDataInitializer.EXPT_DESIGN_ID, "DESIGN",
				"EXPERIMENTAL DESIGN", WorkbookTestDataInitializer.TYPE, WorkbookTestDataInitializer.ASSIGNED,
				WorkbookTestDataInitializer.EXPERIMENT_DESIGN, WorkbookTestDataInitializer.CHAR,
				String.valueOf(TermId.P_REP.getId()), WorkbookTestDataInitializer.TRIAL,
				TermId.CHARACTER_VARIABLE.getId(), PhenotypicType.TRIAL_ENVIRONMENT, false);
		wb.setExperimentalDesignVariables(Collections.singletonList(prepDesign));
		Mockito.doReturn(wb).when(this.fieldbookMWService).getStudyVariableSettings(ArgumentMatchers.anyInt());
		this.mockStandardVariables(this.workbook.getAllVariables(), this.fieldbookMWService, this.fieldbookService);

		final int id = new Random().nextInt(100);
		final CreateTrialForm form = new CreateTrialForm();
		final Model model = new ExtendedModelMap();
		final List<ValueReference> allEntries = this.getAllEntries(5, 3, 5);
		final List<Integer> checksEntries = this.getAllCheckEntryTypeIds(allEntries);

		Mockito.when(this.fieldbookService.getAllPossibleValues(TermId.ENTRY_TYPE.getId(), true)).thenReturn(allEntries);
		Mockito.doReturn(5L).when(this.studyEntryService).countStudyGermplasmByEntryTypeIds(id, checksEntries);
		Mockito.doReturn(3L).when(this.studyEntryService).countStudyGermplasmByEntryTypeIds(id,
				Collections.singletonList(Integer.valueOf(SystemDefinedEntryType.NON_REPLICATED_ENTRY.getEntryTypeCategoricalId())));
		this.reviewStudyDetailsController.show(id, form, model);

		final StudyDetails details = (StudyDetails) model.asMap().get("trialDetails");
		Assert.assertNotNull(details);
		final Boolean isSuperAdmin =  (Boolean) model.asMap().get("isSuperAdmin");
		Assert.assertNotNull(isSuperAdmin);
		Assert.assertEquals("Checks Count - Non Replicated Count", 2L, model.asMap().get("numberOfChecks"));
		Assert.assertNotNull(model.asMap().get("nonReplicatedEntriesCount"));
		Assert.assertEquals(3L,model.asMap().get("nonReplicatedEntriesCount"));
	}

	private StandardVariable createStandardVariable(final Integer id, final String property, final String scale, final String method,
			final PhenotypicType role) {
		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(id);
		standardVariable.setProperty(this.createTerm(property));
		standardVariable.setScale(this.createTerm(scale));
		standardVariable.setMethod(this.createTerm(method));
		standardVariable.setPhenotypicType(role);
		standardVariable.setDataType(this.createTerm("N"));
		return standardVariable;
	}

	private Term createTerm(final String name) {
		final Term term = new Term();
		term.setId(1);
		term.setName(name);
		return term;
	}

	private List<ValueReference> getAllEntries(final int checksSize, final int nonReplicatedEntriesCount, final int testSize) {
		final ArrayList<ValueReference> references = new ArrayList<>();

		for (int i=0; i<checksSize; i++) {
			ValueReference reference = new ValueReference();
			reference.setId(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());
			reference.setName(RandomStringUtils.randomAlphanumeric(10));
			reference.setDescription(RandomStringUtils.randomAlphanumeric(20));
			reference.setKey(String.valueOf(i));
			reference.setProgramUUID(contextUtil.getCurrentProgramUUID());
			references.add(reference);
		}

		for (int i=0; i<nonReplicatedEntriesCount; i++) {
			ValueReference reference = new ValueReference();
			reference.setId(SystemDefinedEntryType.NON_REPLICATED_ENTRY.getEntryTypeCategoricalId());
			reference.setName(RandomStringUtils.randomAlphanumeric(10));
			reference.setDescription(RandomStringUtils.randomAlphanumeric(20));
			reference.setKey(String.valueOf(i));
			reference.setProgramUUID(contextUtil.getCurrentProgramUUID());
			references.add(reference);
		}

		for (int i=0; i<testSize; i++) {
			ValueReference reference = new ValueReference();
			reference.setId(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId());
			reference.setName(RandomStringUtils.randomAlphanumeric(10));
			reference.setDescription(RandomStringUtils.randomAlphanumeric(20));
			reference.setKey(String.valueOf(i));
			reference.setProgramUUID(contextUtil.getCurrentProgramUUID());
			references.add(reference);
		}

		return references;
	}

	private List<Integer> getAllCheckEntryTypeIds(final List<ValueReference> valueReferences) {
		final ArrayList<Integer> ids = new ArrayList<>();
		for (final ValueReference valueReference : valueReferences) {
			if (SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId() != valueReference.getId()) {
				ids.add(valueReference.getId());
			}
		}
		return ids;
	}
}
