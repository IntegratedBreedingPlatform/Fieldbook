package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.SaveListForm;
import com.efficio.fieldbook.web.common.service.impl.CrossingServiceImpl;
import com.efficio.fieldbook.web.naming.service.NamingConventionService;
import com.efficio.fieldbook.web.trial.bean.AdvancingStudy;
import com.efficio.fieldbook.web.trial.form.AdvancingStudyForm;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.middleware.ruleengine.pojo.ImportedCross;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.middleware.ruleengine.pojo.ImportedGermplasm;
import org.generationcp.middleware.ruleengine.pojo.ImportedGermplasmParent;
import org.generationcp.middleware.ruleengine.pojo.AdvancingSource;
import org.generationcp.commons.pojo.AdvancingSourceList;
import org.generationcp.middleware.ruleengine.RuleException;
import org.generationcp.middleware.ruleengine.settings.AdditionalDetailsSetting;
import org.generationcp.middleware.ruleengine.settings.BreedingMethodSetting;
import org.generationcp.middleware.ruleengine.settings.CrossNameSetting;
import org.generationcp.middleware.ruleengine.settings.CrossSetting;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Attribute;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceService;
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
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmTreeControllerTest {

	private static final String GERMPLASM_NAME_PREFIX = "TEST VARIETY-";
	private static final String LIST_NAME_SHOULD_BE_UNIQUE = "List Name should be unique";
	private static final String PROJECT_ID = "1";
	private static final String LIST_PARENT_ID = "999";
	private static final String LIST_TYPE = "GERMPLASM LITS";
	private static final String LIST_NOTES = "LIST NOTES";
	private static final String LIST_IDENTIFIER = "LIST IDENTIFIER";
	private static final String LIST_DESCRIPTION = "LIST DESCRIPTION";
	private static final String LIST_DATE = "2015-01-30";
	private static final String LIST_NAME = "LIST 1";
	private static final Integer SAVED_GERMPLASM_ID = 12345;
	private static final String ERROR_MESSAGE = "middeware exception message";
	private static final Integer TEST_USER_ID = 101;
	private static final String TEST_USER_NAME = "Test User";
	private static final String TEST_PROGRAM_UUID = "1234567890";
	private static final int PLOT_CODE_FIELD_NO = 1152;
	private static final int REP_FIELD_NO = 1153;
	private static final int PLOT_FIELD_NO = 1154;
	private static final int TRIAL_INSTANCE_FIELD_NO = 1155;
	private static final int PLANT_NUMBER = 1156;

	@Mock
	private ResourceBundleMessageSource messageSource;

	@Mock
	private HttpServletRequest request;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private final UserSelection userSelection = new UserSelection();

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private CrossingServiceImpl crossingService;

	@Mock
	private NamingConventionService namingConventionService;

	@Mock
	private DatasetService datasetService;

	@Mock
	private GermplasmService germplasmService;

	@Mock
	private GermplasmStudySourceService germplasmStudySourceService;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@InjectMocks
	private GermplasmTreeController controller;

	@Before
	public void setUp() throws MiddlewareQueryException {
		final Workbook workbook = this.createWorkBook();

		Mockito.doReturn(this.createCrossSetting()).when(this.userSelection).getCrossSettings();
		Mockito.doReturn(this.createImportedCrossesList()).when(this.userSelection).getImportedCrossesList();
		Mockito.doReturn(workbook).when(this.userSelection).getWorkbook();
		Mockito.doReturn(GermplasmTreeControllerTest.SAVED_GERMPLASM_ID).when(this.fieldbookMiddlewareService)
			.saveGermplasmList(ArgumentMatchers.any(), ArgumentMatchers.<List<Pair<Germplasm, GermplasmListData>>>any(),
				ArgumentMatchers.any(GermplasmList.class), ArgumentMatchers.eq(false));

		final Table<Integer, Integer, Integer> observationUnitIdsTable = HashBasedTable.create();
		observationUnitIdsTable.put(1, 1, 100);
		observationUnitIdsTable.put(1, 3, 101);
		Mockito.doReturn(observationUnitIdsTable).when(this.datasetService)
			.getTrialNumberPlotNumberObservationUnitIdTable(ArgumentMatchers.eq(workbook.getMeasurementDatesetId()),
				ArgumentMatchers.anySet(), ArgumentMatchers.anySet());

		try {
			Mockito.doReturn(GermplasmTreeControllerTest.LIST_NAME_SHOULD_BE_UNIQUE).when(this.messageSource)
				.getMessage("germplasm.save.list.name.unique.error", null, LocaleContextHolder.getLocale());
		} catch (final Exception e) {

		}

		final Term plotCodeVariable = new Term();
		plotCodeVariable.setId(PLOT_CODE_FIELD_NO);
		final Term repFieldNoVariable = new Term();
		repFieldNoVariable.setId(REP_FIELD_NO);
		final Term trialInstanceVariable = new Term();
		trialInstanceVariable.setId(TRIAL_INSTANCE_FIELD_NO);
		final Term plotFieldNoVariableId = new Term();
		plotFieldNoVariableId.setId(PLOT_FIELD_NO);
		final Term plantNumberVariableId = new Term();
		plantNumberVariableId.setId(PLANT_NUMBER);

		Mockito.when(this.germplasmService.getPlotCodeField())
			.thenReturn(plotCodeVariable);

		Mockito.when(this.ontologyDataManager.findTermByName(Mockito.eq("REP_NUMBER_AP_text"), Mockito.eq(CvId.VARIABLES.getId())))
			.thenReturn(repFieldNoVariable);
		Mockito.when(this.ontologyDataManager.findTermByName(Mockito.eq("INSTANCE_NUMBER_AP_text"), Mockito.eq(CvId.VARIABLES.getId())))
			.thenReturn(trialInstanceVariable);
		Mockito.when(this.ontologyDataManager.findTermByName(Mockito.eq("PLANT_NUMBER_AP_text"), Mockito.eq(CvId.VARIABLES.getId())))
			.thenReturn(plantNumberVariableId);
		Mockito.when(this.ontologyDataManager.findTermByName(Mockito.eq("PLOT_NUMBER_AP_text"), Mockito.eq(CvId.VARIABLES.getId())))
			.thenReturn(plotFieldNoVariableId);

		Mockito.when(this.contextUtil.getCurrentWorkbenchUserId()).thenReturn(GermplasmTreeControllerTest.TEST_USER_ID);
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(GermplasmTreeControllerTest.TEST_PROGRAM_UUID);
		final Project project = new Project();
		project.setCropType(new CropType());
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(project);
		Mockito.when(this.fieldbookMiddlewareService.getOwnerListName(GermplasmTreeControllerTest.TEST_USER_ID))
			.thenReturn(GermplasmTreeControllerTest.TEST_USER_NAME);

		ContextHolder.setLoggedInUserId(TEST_USER_ID);
	}

	@Test
	public void testSaveAdvanceListPostSuccessful() {
		final PaginationListSelection paginationListSelection = new PaginationListSelection();
		paginationListSelection.addAdvanceDetails(GermplasmTreeControllerTest.LIST_IDENTIFIER, this.createAdvancingStudyForm(true));

		final SaveListForm form = this.createSaveListForm();
		form.setGermplasmListType(GermplasmTreeController.GERMPLASM_LIST_TYPE_ADVANCE);

		this.controller.setPaginationListSelection(paginationListSelection);

		final Map<String, Object> result = this.controller.savePost(form, Mockito.mock(Model.class));

		Assert.assertEquals("isSuccess Value should be 1", 1, result.get("isSuccess"));
		Assert.assertEquals("Unique ID should be LIST IDENTIFIER", form.getListIdentifier(), result.get("uniqueId"));
		Assert.assertEquals("List Name should be LIST 1", form.getListName(), result.get("listName"));
		Assert.assertNotNull(result.get("isNamesChanged"));
		Mockito.verify(this.germplasmStudySourceService).saveGermplasmStudySources(ArgumentMatchers.anyList());
	}

	@Test
	public void testSaveList() {
		final SaveListForm form = new SaveListForm();
		this.controller.saveList(form, GermplasmTreeControllerTest.LIST_IDENTIFIER, Mockito.mock(Model.class));
		Assert.assertEquals(DateUtil.getCurrentDateInUIFormat(), form.getListDate());
		Assert.assertEquals(GermplasmTreeControllerTest.LIST_IDENTIFIER, form.getListIdentifier());
		Assert.assertEquals(GermplasmTreeControllerTest.TEST_USER_NAME, form.getListOwner());
	}

	@Test
	public void testSaveCrossesList() {
		this.userSelection.getImportedCrossesList().setDate(DateUtil.getCurrentDate());
		this.userSelection.getImportedCrossesList().setTitle("");
		this.userSelection.getImportedCrossesList().setType(AppConstants.GERMPLASM_LIST_TYPE_GENERIC_LIST.getString());
		this.userSelection.getImportedCrossesList().setUserId(GermplasmTreeControllerTest.TEST_USER_ID);

		final SaveListForm form = new SaveListForm();
		this.controller.saveList(form, Mockito.mock(Model.class));

		Assert.assertNull(form.getListName());
		Assert.assertEquals(DateUtil.getCurrentDateInUIFormat(), form.getListDate());
		Assert.assertEquals("", form.getListDescription());
		Assert.assertEquals(AppConstants.GERMPLASM_LIST_TYPE_GENERIC_LIST.getString(), form.getListType());
		Assert.assertEquals(GermplasmTreeControllerTest.TEST_USER_NAME, form.getListOwner());
	}

	@Test
	public void testGetPreferredName() {
		final String name = "name";
		Mockito.when(this.germplasmDataManager.getPreferredNameValueByGID(1)).thenReturn(name);
		final String preferredName = this.controller.getPreferredName("1");
		Assert.assertEquals(name, preferredName);
	}

	@Test
	public void testSaveCrossesListPostSuccessful() {
		final SaveListForm form = this.createSaveListForm();
		form.setGermplasmListType(GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS);

		final Map<String, Object> result = this.controller.savePost(form, Mockito.mock(Model.class));

		Assert.assertEquals("isSuccess Value should be 1", 1, result.get("isSuccess"));
		Assert.assertEquals("germplasmListId should be " + SAVED_GERMPLASM_ID, SAVED_GERMPLASM_ID, result.get("germplasmListId"));
		Assert.assertEquals("Unique ID should be LIST IDENTIFIER", form.getListIdentifier(), result.get("uniqueId"));
		Assert.assertEquals("List Name should be LIST 1", form.getListName(), result.get("listName"));
		Assert.assertEquals("isNamesChanged should be 0", 0, result.get("isNamesChanged"));
		Mockito.verify(this.crossingService).applyCrossSetting(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(this.germplasmStudySourceService).saveGermplasmStudySources(ArgumentMatchers.anyList());
	}

	@Test
	public void testSaveCrossesList_OmitAlertedCrosses_OneCrossIsOmitted() {
		final SaveListForm form = this.createSaveListForm();
		form.setOmitAlertedCrosses(true);
		form.setGermplasmListType(GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS);

		final ImportedCross cross1 = this.userSelection.getImportedCrossesList().getImportedCrosses().get(0);

		// Set the first cross to have existing crosses
		Mockito.when(this.germplasmDataManager.hasExistingCrosses(Integer.valueOf(cross1.getFemaleGid()),
			cross1.getMaleGids(), Optional.of(Integer.valueOf(cross1.getGid())))).thenReturn(true);

		final Map<String, Object> result = this.controller.savePost(form, Mockito.mock(Model.class));

		Assert.assertEquals("isSuccess Value should be 1", 1, result.get("isSuccess"));
		Assert.assertEquals("germplasmListId should be " + SAVED_GERMPLASM_ID, SAVED_GERMPLASM_ID, result.get("germplasmListId"));
		Assert.assertEquals("Unique ID should be LIST IDENTIFIER", form.getListIdentifier(), result.get("uniqueId"));
		Assert.assertEquals("List Name should be LIST 1", form.getListName(), result.get("listName"));
		Assert.assertEquals("isNamesChanged should be 0", 0, result.get("isNamesChanged"));
		Mockito.verify(this.crossingService).applyCrossSetting(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(this.germplasmStudySourceService).saveGermplasmStudySources(ArgumentMatchers.anyList());

		final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<List<Pair<Germplasm, GermplasmListData>>> listCaptor = ArgumentCaptor.forClass(List.class);
		final ArgumentCaptor<GermplasmList> germplasmListCaptor = ArgumentCaptor.forClass(GermplasmList.class);
		final ArgumentCaptor<Boolean> booleanCaptor = ArgumentCaptor.forClass(Boolean.class);

		Mockito.verify(this.fieldbookMiddlewareService).saveGermplasmList(stringCaptor.capture(), listCaptor.capture(),
			germplasmListCaptor.capture(), booleanCaptor.capture());

		Assert.assertEquals("Only one cross should be created", 1, listCaptor.getValue().size());
		Assert.assertEquals(1, listCaptor.getValue().get(0).getRight().getEntryId().intValue());
	}

	@Test
	public void testSaveCrossesList_OmitAlertedCrosses_AllCrossesAreOmitted() {
		final SaveListForm form = this.createSaveListForm();
		form.setOmitAlertedCrosses(true);
		form.setGermplasmListType(GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS);

		final ImportedCross cross1 = this.userSelection.getImportedCrossesList().getImportedCrosses().get(0);

		// Set all crosses to have existing crosses
		Mockito.when(this.germplasmDataManager.hasExistingCrosses(Mockito.any(),
			Mockito.anyList(), Mockito.any())).thenReturn(true);

		final Map<String, Object> result = this.controller.savePost(form, Mockito.mock(Model.class));
		Assert.assertEquals("isSuccess Value should be 0", 0, result.get("isSuccess"));
		Assert.assertEquals("No crosses to save.", result.get("message"));

		Mockito.verify(this.crossingService).applyCrossSetting(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(this.germplasmStudySourceService, Mockito.times(0)).saveGermplasmStudySources(ArgumentMatchers.anyList());

	}

	@Test
	public void testSaveListPostWithExistingGermplasmList() throws MiddlewareQueryException {
		final SaveListForm form = this.createSaveListForm();
		form.setGermplasmListType(GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS);

		Mockito.doReturn(this.createGermplasmList()).when(this.fieldbookMiddlewareService)
			.getGermplasmListByName(ArgumentMatchers.anyString(), ArgumentMatchers.anyString());

		final Map<String, Object> result = this.controller.savePost(form, Mockito.mock(Model.class));

		Assert.assertEquals(0, result.get("isSuccess"));
		Assert.assertEquals(GermplasmTreeControllerTest.LIST_NAME_SHOULD_BE_UNIQUE, result.get("message"));
	}

	@Test
	public void testSaveListPostExistingListNameWithTrailingSpaces() throws MiddlewareQueryException {
		final SaveListForm form = this.createSaveListForm();
		form.setListName(GermplasmTreeControllerTest.LIST_NAME + "   ");
		form.setGermplasmListType(GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS);

		// Setup mocks
		Mockito.doReturn(this.createGermplasmList()).when(this.fieldbookMiddlewareService)
			.getGermplasmListByName(GermplasmTreeControllerTest.LIST_NAME, GermplasmTreeControllerTest.TEST_PROGRAM_UUID);

		final Map<String, Object> result = this.controller.savePost(form, Mockito.mock(Model.class));

		// Verify that list name was trimmed before being as Middleware
		// parameter
		Mockito.verify(this.fieldbookMiddlewareService)
			.getGermplasmListByName(GermplasmTreeControllerTest.LIST_NAME, GermplasmTreeControllerTest.TEST_PROGRAM_UUID);
		Assert.assertEquals(0, result.get("isSuccess"));
		Assert.assertEquals(GermplasmTreeControllerTest.LIST_NAME_SHOULD_BE_UNIQUE, result.get("message"));
	}

	@Test
	public void testCreateGermplasmListUsingNameWithTrailingSpaces() throws MiddlewareQueryException {
		final SaveListForm form = this.createSaveListForm();
		form.setListName(GermplasmTreeControllerTest.LIST_NAME + "   ");
		form.setGermplasmListType(GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS);

		final GermplasmList germplasmList = this.controller.createGermplasmList(form, GermplasmTreeControllerTest.TEST_USER_ID);

		// Verify that list name was trimmed plus that other list fields were
		// populated properly
		Assert.assertEquals(GermplasmTreeControllerTest.LIST_NAME, germplasmList.getName());
		Assert.assertEquals(GermplasmTreeControllerTest.LIST_DATE.toString().replace("-", ""), germplasmList.getDate().toString());
		Assert.assertEquals(GermplasmTreeControllerTest.LIST_DESCRIPTION, germplasmList.getDescription());
		Assert.assertEquals(GermplasmTreeControllerTest.LIST_TYPE, germplasmList.getType());
		Assert.assertEquals(GermplasmTreeControllerTest.LIST_NOTES, germplasmList.getNotes());
		Assert.assertEquals(new Integer(1), germplasmList.getStatus());
		Assert.assertEquals(GermplasmTreeControllerTest.TEST_PROGRAM_UUID, germplasmList.getProgramUUID());
	}

	@Test
	public void testCheckIfUniqueUsingExistingListNameWithTrailingSpaces() {
		Mockito.doReturn(Collections.singletonList(this.createGermplasmList())).when(this.germplasmListManager)
			.getGermplasmListByName(GermplasmTreeControllerTest.LIST_NAME, GermplasmTreeControllerTest.TEST_PROGRAM_UUID, 0, 1, null);
		try {
			this.controller.checkIfUnique(GermplasmTreeControllerTest.LIST_NAME + "  ", GermplasmTreeControllerTest.TEST_PROGRAM_UUID);
			Assert.fail("Should have thrown Middleware Exception but didn't.");
		} catch (final MiddlewareException e) {
			Assert.assertEquals(GermplasmTreeController.NAME_NOT_UNIQUE, e.getMessage());
		}
	}

	@Test
	public void testSaveListPostWithError() throws MiddlewareQueryException {
		final SaveListForm form = this.createSaveListForm();
		form.setGermplasmListType(GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS);

		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListByName(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
			.thenThrow(new MiddlewareQueryException(GermplasmTreeControllerTest.ERROR_MESSAGE));

		final Map<String, Object> result = this.controller.savePost(form, Mockito.mock(Model.class));

		Assert.assertEquals(0, result.get("isSuccess"));
		Assert.assertEquals(GermplasmTreeControllerTest.ERROR_MESSAGE, result.get("message"));
	}

	@Test
	public void testPopulateGermplasmListDataFromAdvancedForTrialWithGeneratedDesign() throws RuleException {
		final List<Pair<Germplasm, GermplasmListData>> listDataItems = new ArrayList<>();
		final List<Pair<Germplasm, List<Name>>> germplasmNames = new ArrayList<>();
		final List<Pair<Germplasm, List<Attribute>>> germplasmAttributes = new ArrayList<>();
		final Integer currentDate = DateUtil.getCurrentDateAsIntegerValue();

		final AdvancingStudyForm advancingForm = this.createAdvancingStudyForm(true);

		this.controller.populateGermplasmListDataFromAdvanced(new GermplasmList(), advancingForm, germplasmNames, listDataItems,
			germplasmAttributes);

		// Check Attribute Objects created. Additional attributes are created
		// for studies only
		final List<ImportedGermplasm> inputGermplasmList = advancingForm.getGermplasmList();
		for (int i = 0; i < inputGermplasmList.size(); i++) {
			final List<Attribute> attributes = germplasmAttributes.get(i).getRight();
			final Iterator<Attribute> attributeIterator = attributes.iterator();
			final ImportedGermplasm importedGermplasm = inputGermplasmList.get(i);

			// Expecting REP_NUMBER, PLOTCODE, PLOT_NUMBER and INSTANCE_NUMBER
			// attributes to be created per germplasm
			// GIDs in Attributes are null at this point. It will be set after
			// saving of germplasm
			Assert.assertEquals("Expecting # of Attribute objects per germplasm is 4", 4, attributes.size());
			final Attribute originAttribute = attributeIterator.next();
			Assert.assertNull("Expecting Attribute GID to be null", originAttribute.getGermplasmId());
			Assert.assertEquals("Expecting Attribute Location ID is same as germplasm's Location ID", advancingForm.getHarvestLocationId(),
				originAttribute.getLocationId().toString());
			Assert.assertEquals("Expecting Attribute User ID is same as germplasm's User ID", GermplasmTreeControllerTest.TEST_USER_ID,
				originAttribute.getCreatedBy());
			Assert.assertEquals("Expecting Attribute Date is current date", currentDate, originAttribute.getAdate());
			Assert.assertEquals("Expecting Attribute Type ID is PLOT_CODE id",
				Integer.valueOf(GermplasmTreeControllerTest.PLOT_CODE_FIELD_NO), originAttribute.getTypeId());
			Assert.assertEquals("Expecting Attribute Value is germplasm's source", importedGermplasm.getSource(),
				originAttribute.getAval());

			final Attribute plotAttribute = attributeIterator.next();
			Assert.assertNull("Expecting Attribute GID to be null", plotAttribute.getGermplasmId());
			Assert.assertEquals("Expecting Attribute Location ID is same as germplasm's Location ID", advancingForm.getHarvestLocationId(),
				plotAttribute.getLocationId().toString());
			Assert.assertEquals("Expecting Attribute User ID is same as germplasm's User ID", GermplasmTreeControllerTest.TEST_USER_ID,
				plotAttribute.getCreatedBy());
			Assert.assertEquals("Expecting Attribute Date is current date", currentDate, plotAttribute.getAdate());
			Assert.assertEquals("Expecting Attribute Type ID is PLOT_CODE id", Integer.valueOf(GermplasmTreeControllerTest.PLOT_FIELD_NO),
				plotAttribute.getTypeId());
			Assert.assertEquals("Expecting Attribute Value is germplasm's plot number", importedGermplasm.getPlotNumber(),
				plotAttribute.getAval());

			final Attribute repAttribute = attributeIterator.next();
			Assert.assertNull("Expecting Attribute GID to be null", repAttribute.getGermplasmId());
			Assert.assertEquals("Expecting Attribute Location ID is same as germplasm's Location ID", advancingForm.getHarvestLocationId(),
				repAttribute.getLocationId().toString());
			Assert.assertEquals("Expecting Attribute User ID is same as germplasm's User ID", GermplasmTreeControllerTest.TEST_USER_ID,
				repAttribute.getCreatedBy());
			Assert.assertEquals("Expecting Attribute Date is current date", currentDate, repAttribute.getAdate());
			Assert.assertEquals("Expecting Attribute Type ID is PLOT_CODE id", Integer.valueOf(GermplasmTreeControllerTest.REP_FIELD_NO),
				repAttribute.getTypeId());
			Assert.assertEquals("Expecting Attribute Value is germplasm's plot number", importedGermplasm.getReplicationNumber(),
				repAttribute.getAval());

			final Attribute instanceAttribute = attributeIterator.next();
			Assert.assertNull("Expecting Attribute GID to be null", instanceAttribute.getGermplasmId());
			Assert.assertEquals("Expecting Attribute Location ID is same as germplasm's Location ID", advancingForm.getHarvestLocationId(),
				instanceAttribute.getLocationId().toString());
			Assert.assertEquals("Expecting Attribute User ID is same as germplasm's User ID", GermplasmTreeControllerTest.TEST_USER_ID,
				instanceAttribute.getCreatedBy());
			Assert.assertEquals("Expecting Attribute Date is current date", currentDate, instanceAttribute.getAdate());
			Assert.assertEquals("Expecting Attribute Type ID is PLOT_CODE id",
				Integer.valueOf(GermplasmTreeControllerTest.TRIAL_INSTANCE_FIELD_NO), instanceAttribute.getTypeId());
			Assert.assertEquals("Expecting Attribute Value is germplasm's plot number", importedGermplasm.getTrialInstanceNumber(),
				instanceAttribute.getAval());
		}

		Mockito.verify(this.namingConventionService).generateAdvanceListNames(Mockito.any(), Mockito.eq(false), Mockito.anyList());

	}

	@Test
	public void testPopulateGermplasmListDataFromAdvancedForTrialWithImportedBasicDesign() throws RuleException {
		final List<Pair<Germplasm, GermplasmListData>> listDataItems = new ArrayList<>();
		final List<Pair<Germplasm, List<Name>>> germplasmNames = new ArrayList<>();
		final List<Pair<Germplasm, List<Attribute>>> germplasmAttributes = new ArrayList<>();
		final Integer currentDate = DateUtil.getCurrentDateAsIntegerValue();

		final AdvancingStudyForm advancingForm = this.createAdvancingStudyForm(false);

		this.controller.populateGermplasmListDataFromAdvanced(new GermplasmList(), advancingForm, germplasmNames, listDataItems,
			germplasmAttributes);

		// Check Attribute Objects created. Additional attributes are created
		// for studies only
		final List<ImportedGermplasm> inputGermplasmList = advancingForm.getGermplasmList();
		for (int i = 0; i < inputGermplasmList.size(); i++) {
			final List<Attribute> attributes = germplasmAttributes.get(i).getRight();
			final Iterator<Attribute> attributeIterator = attributes.iterator();
			final ImportedGermplasm importedGermplasm = inputGermplasmList.get(i);

			// Expecting PLOTCODE, PLOT_NUMBER and INSTANCE_NUMBER attributes to
			// be created per germplasm.
			// REP_NUMBER should not be created since basic import design only
			// contains TRIAL_INSTANCE, ENTRY_NO and PLOT_NO variables
			Assert.assertEquals("Expecting # of Attribute objects per germplasm is 3", 3, attributes.size());
			final Attribute originAttribute = attributeIterator.next();
			Assert.assertNull("Expecting Attribute GID to be null", originAttribute.getGermplasmId());
			Assert.assertEquals("Expecting Attribute Location ID is same as germplasm's Location ID", advancingForm.getHarvestLocationId(),
				originAttribute.getLocationId().toString());
			Assert.assertEquals("Expecting Attribute User ID is same as germplasm's User ID", GermplasmTreeControllerTest.TEST_USER_ID,
				originAttribute.getCreatedBy());
			Assert.assertEquals("Expecting Attribute Date is current date", currentDate, originAttribute.getAdate());
			Assert.assertEquals("Expecting Attribute Type ID is PLOT_CODE id",
				Integer.valueOf(GermplasmTreeControllerTest.PLOT_CODE_FIELD_NO), originAttribute.getTypeId());
			Assert.assertEquals("Expecting Attribute Value is germplasm's source", importedGermplasm.getSource(),
				originAttribute.getAval());

			final Attribute plotAttribute = attributeIterator.next();
			Assert.assertNull("Expecting Attribute GID to be null", plotAttribute.getGermplasmId());
			Assert.assertEquals("Expecting Attribute Location ID is same as germplasm's Location ID", advancingForm.getHarvestLocationId(),
				plotAttribute.getLocationId().toString());
			Assert.assertEquals("Expecting Attribute User ID is same as germplasm's User ID", GermplasmTreeControllerTest.TEST_USER_ID,
				plotAttribute.getCreatedBy());
			Assert.assertEquals("Expecting Attribute Date is current date", currentDate, plotAttribute.getAdate());
			Assert.assertEquals("Expecting Attribute Type ID is PLOT_CODE id", Integer.valueOf(GermplasmTreeControllerTest.PLOT_FIELD_NO),
				plotAttribute.getTypeId());
			Assert.assertEquals("Expecting Attribute Value is germplasm's plot number", importedGermplasm.getPlotNumber(),
				plotAttribute.getAval());

			final Attribute instanceAttribute = attributeIterator.next();
			Assert.assertNull("Expecting Attribute GID to be null", instanceAttribute.getGermplasmId());
			Assert.assertEquals("Expecting Attribute Location ID is same as germplasm's Location ID", advancingForm.getHarvestLocationId(),
				instanceAttribute.getLocationId().toString());
			Assert.assertEquals("Expecting Attribute User ID is same as germplasm's User ID", GermplasmTreeControllerTest.TEST_USER_ID,
				instanceAttribute.getCreatedBy());
			Assert.assertEquals("Expecting Attribute Date is current date", currentDate, instanceAttribute.getAdate());
			Assert.assertEquals("Expecting Attribute Type ID is PLOT_CODE id",
				Integer.valueOf(GermplasmTreeControllerTest.TRIAL_INSTANCE_FIELD_NO), instanceAttribute.getTypeId());
			Assert.assertEquals("Expecting Attribute Value is germplasm's plot number", importedGermplasm.getTrialInstanceNumber(),
				instanceAttribute.getAval());
		}

	}

	@Test
	public void testPopulateGermplasmListDataFromAdvancedNoHarvestLocationId() throws RuleException {
		final List<Pair<Germplasm, GermplasmListData>> listDataItems = new ArrayList<>();
		final List<Pair<Germplasm, List<Name>>> germplasmNames = new ArrayList<>();
		final List<Pair<Germplasm, List<Attribute>>> germplasmAttributes = new ArrayList<>();

		final AdvancingStudyForm advancingForm = this.createAdvancingStudyForm(false);

		advancingForm.setHarvestLocationId("0");

		this.controller.populateGermplasmListDataFromAdvanced(new GermplasmList(), advancingForm, germplasmNames, listDataItems,
			germplasmAttributes);

		final List<ImportedGermplasm> inputGermplasmList = advancingForm.getGermplasmList();
		for (int i = 0; i < inputGermplasmList.size(); i++) {
			final List<Attribute> attributes = germplasmAttributes.get(i).getRight();
			final Iterator<Attribute> attributeIterator = attributes.iterator();
			final ImportedGermplasm importedGermplasm = inputGermplasmList.get(i);

			final Attribute originAttribute = attributeIterator.next();

			Assert.assertEquals("Expecting Attribute Location ID is same as germplasm's Location ID", importedGermplasm.getLocationId(),
				originAttribute.getLocationId());
		}

	}

	@Test
	public void testCreateGermplasmListParentDoesntExist() {

		final SaveListForm saveListForm = this.createSaveListForm();
		final GermplasmList parentListFolder = new GermplasmList();
		parentListFolder.setType(GermplasmList.FOLDER_TYPE);

		final GermplasmList germplasmList = this.controller.createGermplasmList(saveListForm, TEST_USER_ID);

		Assert.assertNull(germplasmList.getId());
		Assert.assertEquals(LIST_NAME, germplasmList.getName());
		Assert.assertEquals("20150130", germplasmList.getDate().toString());
		Assert.assertEquals(LIST_TYPE, germplasmList.getType());
		Assert.assertEquals(TEST_USER_ID, germplasmList.getUserId());
		Assert.assertEquals(LIST_DESCRIPTION, germplasmList.getDescription());
		Assert.assertNull(germplasmList.getParent());
		Assert.assertEquals(1, germplasmList.getStatus().intValue());
		Assert.assertEquals(LIST_NOTES, germplasmList.getNotes());
		Assert.assertEquals(TEST_PROGRAM_UUID, germplasmList.getProgramUUID());

	}

	@Test
	public void testCreateGermplasmListParentIsFolder() {

		final SaveListForm saveListForm = this.createSaveListForm();
		final GermplasmList parentListFolder = new GermplasmList();
		parentListFolder.setProgramUUID(TEST_PROGRAM_UUID);
		parentListFolder.setType(GermplasmList.FOLDER_TYPE);
		Mockito.when(this.germplasmListManager.getGermplasmListById(Integer.valueOf(LIST_PARENT_ID))).thenReturn(parentListFolder);

		final GermplasmList germplasmList = this.controller.createGermplasmList(saveListForm, TEST_USER_ID);

		Assert.assertNull(germplasmList.getId());
		Assert.assertEquals(LIST_NAME, germplasmList.getName());
		Assert.assertEquals("20150130", germplasmList.getDate().toString());
		Assert.assertEquals(LIST_TYPE, germplasmList.getType());
		Assert.assertEquals(TEST_USER_ID, germplasmList.getUserId());
		Assert.assertEquals(LIST_DESCRIPTION, germplasmList.getDescription());
		Assert.assertEquals(parentListFolder, germplasmList.getParent());
		Assert.assertEquals(1, germplasmList.getStatus().intValue());
		Assert.assertEquals(LIST_NOTES, germplasmList.getNotes());
		Assert.assertEquals(TEST_PROGRAM_UUID, germplasmList.getProgramUUID());

	}

	@Test
	public void testCreateGermplasmListListParentIsCropList() {

		final SaveListForm saveListForm = this.createSaveListForm();
		saveListForm.setParentId(GermplasmTreeController.CROP_LISTS);
		final GermplasmList germplasmList = this.controller.createGermplasmList(saveListForm, TEST_USER_ID);

		Assert.assertNull(germplasmList.getId());
		Assert.assertEquals(LIST_NAME, germplasmList.getName());
		Assert.assertEquals("20150130", germplasmList.getDate().toString());
		Assert.assertEquals(LIST_TYPE, germplasmList.getType());
		Assert.assertEquals(TEST_USER_ID, germplasmList.getUserId());
		Assert.assertEquals(LIST_DESCRIPTION, germplasmList.getDescription());
		Assert.assertNull(GermplasmTreeController.CROP_LISTS, germplasmList.getParent());
		Assert.assertEquals(GermplasmTreeController.LOCKED_LIST_STATUS, germplasmList.getStatus());
		Assert.assertEquals(LIST_NOTES, germplasmList.getNotes());
		Assert.assertNull(germplasmList.getProgramUUID());

	}

	@Test
	public void testCreateGermplasmListListParentIsProgramList() {

		final SaveListForm saveListForm = this.createSaveListForm();
		saveListForm.setParentId(GermplasmTreeController.PROGRAM_LISTS);
		final GermplasmList germplasmList = this.controller.createGermplasmList(saveListForm, TEST_USER_ID);

		Assert.assertNull(germplasmList.getId());
		Assert.assertEquals(LIST_NAME, germplasmList.getName());
		Assert.assertEquals("20150130", germplasmList.getDate().toString());
		Assert.assertEquals(LIST_TYPE, germplasmList.getType());
		Assert.assertEquals(TEST_USER_ID, germplasmList.getUserId());
		Assert.assertEquals(LIST_DESCRIPTION, germplasmList.getDescription());
		Assert.assertNull(GermplasmTreeController.PROGRAM_LISTS, germplasmList.getParent());
		Assert.assertEquals(1, germplasmList.getStatus().intValue());
		Assert.assertEquals(LIST_NOTES, germplasmList.getNotes());
		Assert.assertEquals(TEST_PROGRAM_UUID, germplasmList.getProgramUUID());

	}

	@Test
	public void testApplyNamingRules() throws RuleException {

		final ImportedCrossesList importedCrossesList = this.createImportedCrossesList();
		final Workbook workbook = this.userSelection.getWorkbook();

		this.controller.applyNamingRules(importedCrossesList);

		final ArgumentCaptor<List> argumentCaptor1 = ArgumentCaptor.forClass(List.class);
		final ArgumentCaptor<AdvancingSourceList> argumentCaptor2 = ArgumentCaptor.forClass(AdvancingSourceList.class);
		final ArgumentCaptor<AdvancingStudy> argumentCaptor3 = ArgumentCaptor.forClass(AdvancingStudy.class);
		final ArgumentCaptor<List> argumentCaptor4 = ArgumentCaptor.forClass(List.class);

		Mockito.verify(this.namingConventionService)
			.generateCrossesList(argumentCaptor1.capture(), argumentCaptor2.capture(), argumentCaptor3.capture(), Mockito.eq(workbook),
				argumentCaptor4.capture());
		Mockito.verify(this.userSelection).setImportedCrossesList(importedCrossesList);

		final List<ImportedCross> importedCrossArgument1 = argumentCaptor1.getValue();
		final AdvancingSourceList importedCrossesArgument2 = argumentCaptor2.getValue();
		final AdvancingStudy importedCrossesArgument3 = argumentCaptor3.getValue();
		final List<Integer> importedCrossesArgument4 = argumentCaptor4.getValue();

		Assert.assertEquals(2, importedCrossArgument1.size());
		Assert.assertEquals(2, importedCrossesArgument2.getRows().size());
		Assert.assertEquals(2, importedCrossesArgument4.size());
		Assert.assertTrue(importedCrossesArgument3.isCheckAdvanceLinesUnique());

	}

	@Test
	public void testAssignCrossNames() {

		final List<ImportedCross> importedCrossList = this.createImportedCrossesList().getImportedCrosses();
		final ImportedCross importedCross = importedCrossList.get(0);

		this.controller.assignCrossNames(importedCross);

		Assert.assertFalse(importedCross.getNames().isEmpty());
		Assert.assertEquals(1, importedCross.getNames().get(0).getNstat().intValue());
		Assert.assertEquals(importedCross.getCross(), importedCross.getNames().get(0).getNval());

	}

	@Test
	public void testCreateAdvancingSource() {

		final List<ImportedCross> importedCrossList = this.createImportedCrossesList().getImportedCrosses();
		final ImportedCross importedCross = importedCrossList.get(0);
		final Workbook workbook = this.userSelection.getWorkbook();

		final AdvancingSource advancingSource = this.controller.createAdvancingSource(importedCross);

		Assert.assertEquals(1, advancingSource.getStudyId().intValue());
		Assert.assertEquals(workbook.getConditions(), advancingSource.getConditions());
		Assert.assertEquals(workbook.getStudyDetails().getStudyType(), advancingSource.getStudyType());
		Assert.assertEquals(importedCross.getBreedingMethodId(), advancingSource.getBreedingMethodId());

	}

	public SaveListForm createSaveListForm() {
		final SaveListForm form = new SaveListForm();
		form.setListName(GermplasmTreeControllerTest.LIST_NAME);
		form.setListDate(GermplasmTreeControllerTest.LIST_DATE);
		form.setListDescription(GermplasmTreeControllerTest.LIST_DESCRIPTION);
		form.setListIdentifier(GermplasmTreeControllerTest.LIST_IDENTIFIER);
		form.setListNotes(GermplasmTreeControllerTest.LIST_NOTES);
		form.setListType(GermplasmTreeControllerTest.LIST_TYPE);
		form.setParentId(GermplasmTreeControllerTest.LIST_PARENT_ID);
		form.setOmitAlertedCrosses(false);
		return form;
	}

	private CrossSetting createCrossSetting() {
		final CrossSetting crossSetting = new CrossSetting();

		final CrossNameSetting crossNameSetting = new CrossNameSetting();
		crossNameSetting.setPrefix("PREFIX");
		crossNameSetting.setSuffix("SUFFIX");
		crossNameSetting.setAddSpaceBetweenPrefixAndCode(true);
		crossNameSetting.setAddSpaceBetweenSuffixAndCode(true);
		crossNameSetting.setSeparator("|");
		crossNameSetting.setStartNumber(100);
		crossNameSetting.setNumOfDigits(7);

		crossSetting.setCrossNameSetting(crossNameSetting);
		crossSetting.setBreedingMethodSetting(new BreedingMethodSetting());
		crossSetting.setAdditionalDetailsSetting(new AdditionalDetailsSetting());
		crossSetting.setIsUseManualSettingsForNaming(true);

		return crossSetting;
	}

	private ImportedCrossesList createImportedCrossesList() {
		final ImportedCrossesList importedCrossesList = new ImportedCrossesList();
		final List<ImportedCross> importedCrosses = new ArrayList<>();
		final ImportedCross cross = new ImportedCross();
		final ImportedGermplasmParent femaleParent = new ImportedGermplasmParent(12345, "FEMALE-12345", 1, "");
		cross.setFemaleParent(femaleParent);
		final ImportedGermplasmParent maleParent = new ImportedGermplasmParent(54321, "MALE-54321", 2, "");
		cross.setMaleParents(Lists.newArrayList(maleParent));
		cross.setGid("10021");
		cross.setDesig("Default name1");
		importedCrosses.add(cross);
		final ImportedCross cross2 = new ImportedCross();
		final ImportedGermplasmParent femaleParent2 = new ImportedGermplasmParent(9999, "FEMALE-9999", 3, "");
		cross2.setFemaleParent(femaleParent2);
		final ImportedGermplasmParent maleParent2 = new ImportedGermplasmParent(8888, "MALE-8888", 4, "");
		cross2.setMaleParents(Lists.newArrayList(maleParent2));
		cross2.setGid("10022");
		cross2.setDesig("Default name2");
		importedCrosses.add(cross2);
		importedCrossesList.setImportedGermplasms(importedCrosses);

		return importedCrossesList;
	}

	private Workbook createWorkBook() {
		final Workbook wb = new Workbook();

		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(Integer.valueOf(GermplasmTreeControllerTest.PROJECT_ID));
		wb.setStudyDetails(studyDetails);
		return wb;
	}

	private GermplasmList createGermplasmList() {
		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(1);
		return germplasmList;
	}

	private AdvancingStudyForm createAdvancingStudyForm(final boolean withReplicationNumber) {
		final AdvancingStudyForm advancingStudyForm = new AdvancingStudyForm();
		final List<ImportedGermplasm> importedGermplasmList = new ArrayList<>();
		final List<AdvancingSource> advancingSourceList = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			final ImportedGermplasm importedGermplasm = this.createImportedGermplasm(i, withReplicationNumber);
			importedGermplasmList.add(importedGermplasm);
			advancingSourceList.add(new AdvancingSource(importedGermplasm));
		}
		advancingStudyForm.setHarvestYear("2015");
		advancingStudyForm.setHarvestMonth("08");
		advancingStudyForm.setHarvestLocationId("252");
		advancingStudyForm.setGermplasmList(importedGermplasmList);
		advancingStudyForm.setAdvancingSourceItems(advancingSourceList);
		return advancingStudyForm;
	}

	private ImportedGermplasm createImportedGermplasm(final int gid, final boolean withReplicationNumber) {
		final String gidString = String.valueOf(gid);
		final String desig = GermplasmTreeControllerTest.GERMPLASM_NAME_PREFIX + gid;

		final ImportedGermplasm germplasm = new ImportedGermplasm();
		germplasm.setGid(gidString);
		germplasm.setEntryNumber(gid);
		germplasm.setEntryCode(gidString);
		germplasm.setDesig(desig);
		germplasm.setSource(GermplasmTreeControllerTest.LIST_NAME + ":" + gid);
		germplasm.setCross(gid + "/" + (gid + 1));
		germplasm.setSource("Import file");
		germplasm.setLocationId(RandomUtils.nextInt());
		germplasm.setTrialInstanceNumber("1");
		germplasm.setPlotNumber(gidString);
		if (withReplicationNumber) {
			germplasm.setReplicationNumber("2");
		}

		final Name name = new Name();
		name.setGermplasm(new Germplasm(gid));
		name.setNval(desig);
		name.setNstat(1);
		germplasm.setNames(Collections.singletonList(name));
		return germplasm;
	}
}
