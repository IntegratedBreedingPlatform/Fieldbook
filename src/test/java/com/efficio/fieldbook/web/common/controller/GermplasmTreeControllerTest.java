
package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.tuple.Pair;
import org.generationcp.commons.constant.ListTreeState;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.service.UserTreeStateService;
import org.generationcp.commons.settings.AdditionalDetailsSetting;
import org.generationcp.commons.settings.BreedingMethodSetting;
import org.generationcp.commons.settings.CrossNameSetting;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Attribute;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchRuntimeData;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.ui.Model;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.SaveListForm;
import com.efficio.fieldbook.web.common.service.impl.CrossingServiceImpl;
import com.efficio.fieldbook.web.nursery.form.AdvancingNurseryForm;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmTreeControllerTest {

	private static final String GERMPLASM_NAME_PREFIX = "TEST VARIETY-";
	private static final String LIST_NAME_SHOULD_BE_UNIQUE = "List Name should be unique";
	private static final String ERROR_RULES_NOT_CONFIGURED = "The system was not able to generate names for your crosses because automatic "
			+ "naming rules are not configured for the breeding methods used by the crosses. Please contact your system administrator for "
			+ "assistance, or choose the “Specify name format” option to define the cross names you would like to use.";
	private static final String PROJECT_ID = "1";
	private static final String LIST_PARENT_ID = GermplasmTreeControllerTest.PROJECT_ID;
	private static final String LIST_TYPE = "GERMPLASM LITS";
	private static final String LIST_NOTES = "LIST NOTES";
	private static final String LIST_IDENTIFIER = "LIST IDENTIFIER";
	private static final String LIST_DESCRIPTION = "LIST DESCRIPTION";
	private static final String LIST_DATE = "2015-01-30";
	private static final String SAVED_CROSSES_GID1 = "-9999";
	private static final String SAVED_CROSSES_GID2 = "-8888";
	private static final String LIST_NAME = "LIST 1";
	private static final Integer SAVED_GERMPLASM_ID = 1;
	private static final int SAVED_LISTPROJECT_ID = 2;
	private static final String ERROR_MESSAGE = "middeware exception message";
	private static final Integer TEST_USER_ID = 101;
	private static final String TEST_PROGRAM_UUID = "1234567890";
	private static final int PLOT_CODE_FIELD_NO = 1152;
	private static final int REP_FIELD_NO = 1153;
	private static final int PLOT_FIELD_NO = 1154;
	private static final int TRIAL_INSTANCE_FIELD_NO = 1155;

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
	private WorkbenchService workbenchService;

	@Mock
	private ContextUtil contextUtil;

	private SaveListForm form;

	@Mock
	private CrossingServiceImpl crossingService;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@Mock
	private AbstractBaseFieldbookController abstractFieldbookController;

	@Mock
	private WorkbenchRuntimeData workbenchRuntimeData;

	@Mock
	private UserTreeStateService userTreeStateService;

	@InjectMocks
	private GermplasmTreeController controller;

	@Before
	public void setUp() throws MiddlewareQueryException {
		Mockito.doReturn(this.getProject()).when(this.workbenchDataManager).getLastOpenedProjectAnyUser();
		Mockito.doReturn(this.workbenchRuntimeData).when(this.workbenchDataManager).getWorkbenchRuntimeData();
		Mockito.doReturn(this.createCrossSetting()).when(this.userSelection).getCrossSettings();
		Mockito.doReturn(this.createImportedCrossesList()).when(this.userSelection).getImportedCrossesList();
		Mockito.doReturn(this.createWorkBook()).when(this.userSelection).getWorkbook();
		Mockito.doReturn(null).when(this.fieldbookMiddlewareService).getGermplasmIdByName(Matchers.anyString());
		Mockito.doReturn(GermplasmTreeControllerTest.SAVED_GERMPLASM_ID).when(this.fieldbookMiddlewareService)
				.saveGermplasmList(Matchers.anyList(), Matchers.any(GermplasmList.class));
		Mockito.doReturn(GermplasmTreeControllerTest.SAVED_LISTPROJECT_ID).when(this.fieldbookMiddlewareService)
				.saveOrUpdateListDataProject(Matchers.anyInt(), Matchers.any(GermplasmListType.class), Matchers.anyInt(),
						Matchers.anyList(), Matchers.anyInt());

		Mockito.doReturn(1).when(this.crossingService).getIDForUserDefinedFieldCrossingName();

		Mockito.doReturn(new Method()).when(this.germplasmDataManager).getMethodByName(Matchers.anyString());
		Mockito.doReturn(this.createGermplasmIds()).when(this.germplasmDataManager).addGermplasm(Matchers.anyMap());
		Mockito.doReturn(this.createNameTypes()).when(this.germplasmListManager).getGermplasmNameTypes();
		Mockito.doReturn(this.createGermplasmListData()).when(this.germplasmListManager).getGermplasmListDataByListId(Matchers.anyInt());

		try {
			Mockito.doReturn(GermplasmTreeControllerTest.LIST_NAME_SHOULD_BE_UNIQUE).when(this.messageSource)
					.getMessage("germplasm.save.list.name.unique.error", null, LocaleContextHolder.getLocale());
		} catch (final Exception e) {

		}
		Mockito.when(this.messageSource.getMessage("error.save.cross.rules.not.configured", null, "The rules" + " were not configured",
				LocaleContextHolder.getLocale())).thenReturn(GermplasmTreeControllerTest.ERROR_RULES_NOT_CONFIGURED);
		Mockito.when(this.germplasmDataManager.getPlotCodeField())
				.thenReturn(new UserDefinedField(GermplasmTreeControllerTest.PLOT_CODE_FIELD_NO));
		Mockito.when(this.germplasmDataManager.getUserDefinedFieldByTableTypeAndCode("ATRIBUTS", "PASSPORT", "PLOT_NUMBER"))
				.thenReturn(new UserDefinedField(GermplasmTreeControllerTest.PLOT_FIELD_NO));
		Mockito.when(this.germplasmDataManager.getUserDefinedFieldByTableTypeAndCode("ATRIBUTS", "PASSPORT", "REP_NUMBER"))
				.thenReturn(new UserDefinedField(GermplasmTreeControllerTest.REP_FIELD_NO));
		Mockito.when(this.germplasmDataManager.getUserDefinedFieldByTableTypeAndCode("ATRIBUTS", "PASSPORT", "INSTANCE_NUMBER"))
				.thenReturn(new UserDefinedField(GermplasmTreeControllerTest.TRIAL_INSTANCE_FIELD_NO));
	}

	private Project getProject() {
		final Project project = new Project();
		project.setProjectId((long) 1);
		return project;
	}

	@Test
	public void testSaveAdvanceListPostSuccessful() {
		final PaginationListSelection paginationListSelection = new PaginationListSelection();
		paginationListSelection.addAdvanceDetails(GermplasmTreeControllerTest.LIST_IDENTIFIER, this.createAdvancingNurseryForm(true));

		this.form = new SaveListForm();
		this.form.setListName(GermplasmTreeControllerTest.LIST_NAME);
		this.form.setListDate(GermplasmTreeControllerTest.LIST_DATE);
		this.form.setListDescription(GermplasmTreeControllerTest.LIST_DESCRIPTION);
		this.form.setListIdentifier(GermplasmTreeControllerTest.LIST_IDENTIFIER);
		this.form.setListNotes(GermplasmTreeControllerTest.LIST_NOTES);
		this.form.setListType(GermplasmTreeControllerTest.LIST_TYPE);
		this.form.setParentId(GermplasmTreeControllerTest.LIST_PARENT_ID);
		this.form.setGermplasmListType(GermplasmTreeController.GERMPLASM_LIST_TYPE_ADVANCE);

		this.controller.setPaginationListSelection(paginationListSelection);

		final Map<String, Object> result = this.controller.savePost(this.form, Mockito.mock(Model.class));

		Assert.assertEquals("isSuccess Value should be 1", 1, result.get("isSuccess"));
		Assert.assertEquals("advancedGermplasmListId should be 2", 2, result.get("advancedGermplasmListId"));
		Assert.assertEquals("Unique ID should be LIST IDENTIFIER", this.form.getListIdentifier(), result.get("uniqueId"));
		Assert.assertEquals("List Name should be LIST 1", this.form.getListName(), result.get("listName"));
	}

	@Test
	public void testSaveCrossesListPostSuccessful() {
		this.form = new SaveListForm();
		this.form.setListName(GermplasmTreeControllerTest.LIST_NAME);
		this.form.setListDate(GermplasmTreeControllerTest.LIST_DATE);
		this.form.setListDescription(GermplasmTreeControllerTest.LIST_DESCRIPTION);
		this.form.setListIdentifier(GermplasmTreeControllerTest.LIST_IDENTIFIER);
		this.form.setListNotes(GermplasmTreeControllerTest.LIST_NOTES);
		this.form.setListType(GermplasmTreeControllerTest.LIST_TYPE);
		this.form.setParentId(GermplasmTreeControllerTest.LIST_PARENT_ID);
		this.form.setGermplasmListType(GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS);

		final Map<String, Object> result = this.controller.savePost(this.form, Mockito.mock(Model.class));

		Assert.assertEquals("isSuccess Value should be 1", 1, result.get("isSuccess"));
		Assert.assertEquals("germplasmListId should be 1", 1, result.get("germplasmListId"));
		Assert.assertEquals("crossesListId should be 2", 2, result.get("crossesListId"));
		Assert.assertEquals("Unique ID should be LIST IDENTIFIER", this.form.getListIdentifier(), result.get("uniqueId"));
		Assert.assertEquals("List Name should be LIST 1", this.form.getListName(), result.get("listName"));
	}

	@Test
	public void testSaveListPostWithExistingGermplasmList() throws MiddlewareQueryException {
		this.form = new SaveListForm();
		this.form.setListName(GermplasmTreeControllerTest.LIST_NAME);
		this.form.setListDate(GermplasmTreeControllerTest.LIST_DATE);
		this.form.setListDescription(GermplasmTreeControllerTest.LIST_DESCRIPTION);
		this.form.setListIdentifier(GermplasmTreeControllerTest.LIST_IDENTIFIER);
		this.form.setListNotes(GermplasmTreeControllerTest.LIST_NOTES);
		this.form.setListType(GermplasmTreeControllerTest.LIST_TYPE);
		this.form.setParentId(GermplasmTreeControllerTest.LIST_PARENT_ID);
		this.form.setGermplasmListType(GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS);

		Mockito.doReturn(this.createGermplasmList()).when(this.fieldbookMiddlewareService).getGermplasmListByName(Matchers.anyString(),
				Matchers.anyString());

		final Map<String, Object> result = this.controller.savePost(this.form, Mockito.mock(Model.class));

		Assert.assertEquals(0, result.get("isSuccess"));
		Assert.assertEquals(GermplasmTreeControllerTest.LIST_NAME_SHOULD_BE_UNIQUE, result.get("message"));
	}

	@Test
	public void testSaveListPostWithError() throws MiddlewareQueryException {
		this.form = new SaveListForm();
		this.form.setListName(GermplasmTreeControllerTest.LIST_NAME);
		this.form.setListDate(GermplasmTreeControllerTest.LIST_DATE);
		this.form.setListDescription(GermplasmTreeControllerTest.LIST_DESCRIPTION);
		this.form.setListIdentifier(GermplasmTreeControllerTest.LIST_IDENTIFIER);
		this.form.setListNotes(GermplasmTreeControllerTest.LIST_NOTES);
		this.form.setListType(GermplasmTreeControllerTest.LIST_TYPE);
		this.form.setParentId(GermplasmTreeControllerTest.LIST_PARENT_ID);
		this.form.setGermplasmListType(GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS);

		Mockito.when(this.germplasmDataManager.getMethodByName(Matchers.anyString()))
				.thenThrow(new MiddlewareQueryException(GermplasmTreeControllerTest.ERROR_MESSAGE));
		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListByName(Matchers.anyString(), Matchers.anyString()))
				.thenThrow(new MiddlewareQueryException(GermplasmTreeControllerTest.ERROR_MESSAGE));

		final Map<String, Object> result = this.controller.savePost(this.form, Mockito.mock(Model.class));

		Assert.assertEquals(0, result.get("isSuccess"));
		Assert.assertEquals(GermplasmTreeControllerTest.ERROR_MESSAGE, result.get("message"));
	}

	@Test
	public void testSaveTreeState() throws MiddlewareQueryException {
		final String[] expandedNodes = {"2", "5", "6"};

		Mockito.doReturn(GermplasmTreeControllerTest.TEST_USER_ID).when(this.contextUtil).getCurrentUserLocalId();
		Mockito.doReturn(GermplasmTreeControllerTest.TEST_PROGRAM_UUID).when(this.contextUtil).getCurrentProgramUUID();
		final String response = this.controller.saveTreeState(ListTreeState.GERMPLASM_LIST.toString(), expandedNodes);
		Assert.assertEquals("Should return ok", "OK", response);
	}

	@Test
	public void testLoadTreeStateNonSaveDialog() throws MiddlewareQueryException {
		Mockito.doReturn(GermplasmTreeControllerTest.TEST_USER_ID).when(this.contextUtil).getCurrentUserLocalId();
		Mockito.doReturn(GermplasmTreeControllerTest.TEST_PROGRAM_UUID).when(this.contextUtil).getCurrentProgramUUID();
		final List<String> response = new ArrayList<String>();
		response.add("1");
		response.add("2");
		Mockito.doReturn(response).when(this.userTreeStateService).getUserProgramTreeStateByUserIdProgramUuidAndType(
				GermplasmTreeControllerTest.TEST_USER_ID, GermplasmTreeControllerTest.TEST_PROGRAM_UUID,
				ListTreeState.GERMPLASM_LIST.name());

		final String returnData = this.controller.retrieveTreeState(ListTreeState.GERMPLASM_LIST.name(), false);

		Assert.assertEquals("Should return [1, 2]", "[\"1\",\"2\"]", returnData);
	}

	@Test
	public void testLoadTreeStateSaveDialog() throws MiddlewareQueryException {
		Mockito.doReturn(GermplasmTreeControllerTest.TEST_USER_ID).when(this.contextUtil).getCurrentUserLocalId();
		Mockito.doReturn(GermplasmTreeControllerTest.TEST_PROGRAM_UUID).when(this.contextUtil).getCurrentProgramUUID();
		final List<String> response = new ArrayList<String>();
		response.add("1");
		response.add("2");
		Mockito.doReturn(response).when(this.userTreeStateService).getUserProgramTreeStateForSaveList(
				GermplasmTreeControllerTest.TEST_USER_ID, GermplasmTreeControllerTest.TEST_PROGRAM_UUID);

		final String returnData = this.controller.retrieveTreeState(ListTreeState.GERMPLASM_LIST.name(), true);

		Mockito.verify(this.userTreeStateService).getUserProgramTreeStateForSaveList(GermplasmTreeControllerTest.TEST_USER_ID,
				GermplasmTreeControllerTest.TEST_PROGRAM_UUID);

		Assert.assertEquals("Should return [1, 2]", "[\"1\",\"2\"]", returnData);
	}

	@Test
	public void testAddGermplasmFolder() {
		final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		final String parentID = "1";
		final String folderName = "NewFolder";
		final int listId = 10;
		Mockito.doReturn(parentID).when(req).getParameter("parentFolderId");
		Mockito.doReturn(folderName).when(req).getParameter("folderName");
		Mockito.doReturn(listId).when(this.germplasmListManager).addGermplasmList(Matchers.any(GermplasmList.class));

		final Map<String, Object> resultsMap = this.controller.addGermplasmFolder(req);
		Assert.assertTrue("Expecting that Germplasm Folder is added successfully.",
				resultsMap.get(GermplasmTreeController.IS_SUCCESS).equals("1"));
		Assert.assertTrue("Expecting that Germplasm Folder is added has id " + listId, resultsMap.get("id").equals(listId));
	}

	@Test
	public void testPopulateGermplasmListDataFromAdvancedForNursery() {
		final List<Pair<Germplasm, GermplasmListData>> listDataItems = new ArrayList<>();
		final List<Pair<Germplasm, List<Name>>> germplasmNames = new ArrayList<>();
		final List<Pair<Germplasm, List<Attribute>>> germplasmAttributes = new ArrayList<>();
		final Integer currentDate = DateUtil.getCurrentDateAsIntegerValue();
		final AdvancingNurseryForm advancingForm = this.createAdvancingNurseryForm(true);

		this.controller.populateGermplasmListDataFromAdvanced(new GermplasmList(), advancingForm, germplasmNames, listDataItems,
				GermplasmTreeControllerTest.TEST_USER_ID, germplasmAttributes);

		// Check List Data objects created
		final List<ImportedGermplasm> inputGermplasmList = advancingForm.getGermplasmList();
		final Iterator<ImportedGermplasm> germplasmIterator = inputGermplasmList.iterator();
		Assert.assertEquals("Expecting # of list data objects equals input germplasm size", listDataItems.size(),
				inputGermplasmList.size());
		for (final Pair<Germplasm, GermplasmListData> listDataPair : listDataItems) {
			final GermplasmListData listData = listDataPair.getRight();
			final ImportedGermplasm germplasm = germplasmIterator.next();
			Assert.assertEquals("Expecting list data GID is same as germplasm's GID", germplasm.getGid(),
					listData.getGermplasmId().toString());
			Assert.assertEquals("Expecting list data Entry ID is same as germplasm's Entry ID", germplasm.getEntryId(),
					listData.getEntryId());
			Assert.assertEquals("Expecting list data Entry Code is same as germplasm's Entry Code", germplasm.getEntryCode(),
					listData.getEntryCode());
			Assert.assertEquals("Expecting list data Designation is same as germplasm's Designation", germplasm.getDesig(),
					listData.getDesignation());
			Assert.assertEquals("Expecting list data Seed Source is same as germplasm's Seed Source", germplasm.getSource(),
					listData.getSeedSource());
			Assert.assertEquals("Expecting list data Cross is same as germplasm's Cross", germplasm.getCross(), listData.getGroupName());
		}

		// Check Name objects created
		Assert.assertEquals("Expecting # of Name objects equals input germplasm size", germplasmNames.size(), inputGermplasmList.size());
		for (int i = 0; i < inputGermplasmList.size(); i++) {
			final List<Name> names = germplasmNames.get(i).getRight();
			final ImportedGermplasm germplasm = inputGermplasmList.get(i);
			for (final Name name : names) {
				Assert.assertEquals("Expecting Name GID is same as germplasm's GID", germplasm.getGid(), name.getGermplasmId().toString());
				Assert.assertEquals("Expecting Name Designation is same as germplasm's Designation", germplasm.getDesig(), name.getNval());
				Assert.assertEquals("Expecting Name Location ID is same as form's Location ID", advancingForm.getHarvestLocationId(),
						name.getLocationId().toString());
				Assert.assertEquals("Expecting Name User ID is same as germplasm's User ID", GermplasmTreeControllerTest.TEST_USER_ID,
						name.getUserId());
				Assert.assertEquals("Expecting Name Date is current date", currentDate, name.getNdate());
			}
		}

		// Check Attribute objects created
		for (int i = 0; i < inputGermplasmList.size(); i++) {
			final List<Attribute> attributes = germplasmAttributes.get(i).getRight();

			Assert.assertEquals("Expecting 1 Attribute object per germplasm", 1, attributes.size());
			final Attribute attribute = attributes.get(0);
			// GID in Attribute is null at this point. It will be set after saving of germplasm
			Assert.assertNull("Expecting Attribute GID to be null", attribute.getGermplasmId());
			Assert.assertEquals("Expecting Attribute Location ID is same as germplasm's Location ID", advancingForm.getHarvestLocationId(),
					attribute.getLocationId().toString());
			Assert.assertEquals("Expecting Attribute User ID is same as germplasm's User ID", GermplasmTreeControllerTest.TEST_USER_ID,
					attribute.getUserId());
			Assert.assertEquals("Expecting Attribute Date is current date", currentDate, attribute.getAdate());
			Assert.assertEquals("Expecting Attribute Type ID is PLOT_CODE id",
					Integer.valueOf(GermplasmTreeControllerTest.PLOT_CODE_FIELD_NO), attribute.getTypeId());
			Assert.assertEquals("Expecting Attribute Value is germplasm's source", inputGermplasmList.get(i).getSource(),
					attribute.getAval());

		}
	}

	@Test
	public void testPopulateGermplasmListDataFromAdvancedForTrialWithGeneratedDesign() {
		final List<Pair<Germplasm, GermplasmListData>> listDataItems = new ArrayList<>();
		final List<Pair<Germplasm, List<Name>>> germplasmNames = new ArrayList<>();
		final List<Pair<Germplasm, List<Attribute>>> germplasmAttributes = new ArrayList<>();
		final Integer currentDate = DateUtil.getCurrentDateAsIntegerValue();

		final AdvancingNurseryForm advancingForm = this.createAdvancingNurseryForm(true);
		Mockito.doReturn(true).when(this.userSelection).isTrial();

		this.controller.populateGermplasmListDataFromAdvanced(new GermplasmList(), advancingForm, germplasmNames, listDataItems,
				GermplasmTreeControllerTest.TEST_USER_ID, germplasmAttributes);

		// Check Attribute Objects created. Additional attributes are created for trials only
		final List<ImportedGermplasm> inputGermplasmList = advancingForm.getGermplasmList();
		for (int i = 0; i < inputGermplasmList.size(); i++) {
			final List<Attribute> attributes = germplasmAttributes.get(i).getRight();
			final Iterator<Attribute> attributeIterator = attributes.iterator();
			final ImportedGermplasm importedGermplasm = inputGermplasmList.get(i);

			// Expecting REP_NUMBER, PLOTCODE, PLOT_NUMBER and INSTANCE_NUMBER attributes to be created per germplasm
			// GIDs in Attributes are null at this point. It will be set after saving of germplasm
			Assert.assertEquals("Expecting # of Attribute objects per germplasm is 4", 4, attributes.size());
			final Attribute originAttribute = attributeIterator.next();
			Assert.assertNull("Expecting Attribute GID to be null", originAttribute.getGermplasmId());
			Assert.assertEquals("Expecting Attribute Location ID is same as germplasm's Location ID", advancingForm.getHarvestLocationId(),
					originAttribute.getLocationId().toString());
			Assert.assertEquals("Expecting Attribute User ID is same as germplasm's User ID", GermplasmTreeControllerTest.TEST_USER_ID,
					originAttribute.getUserId());
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
					plotAttribute.getUserId());
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
					repAttribute.getUserId());
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
					instanceAttribute.getUserId());
			Assert.assertEquals("Expecting Attribute Date is current date", currentDate, instanceAttribute.getAdate());
			Assert.assertEquals("Expecting Attribute Type ID is PLOT_CODE id",
					Integer.valueOf(GermplasmTreeControllerTest.TRIAL_INSTANCE_FIELD_NO), instanceAttribute.getTypeId());
			Assert.assertEquals("Expecting Attribute Value is germplasm's plot number", importedGermplasm.getTrialInstanceNumber(),
					instanceAttribute.getAval());
		}

	}

	@Test
	public void testPopulateGermplasmListDataFromAdvancedForTrialWithImportedBasicDesign() {
		final List<Pair<Germplasm, GermplasmListData>> listDataItems = new ArrayList<>();
		final List<Pair<Germplasm, List<Name>>> germplasmNames = new ArrayList<>();
		final List<Pair<Germplasm, List<Attribute>>> germplasmAttributes = new ArrayList<>();
		final Integer currentDate = DateUtil.getCurrentDateAsIntegerValue();

		final AdvancingNurseryForm advancingForm = this.createAdvancingNurseryForm(false);
		Mockito.doReturn(true).when(this.userSelection).isTrial();

		this.controller.populateGermplasmListDataFromAdvanced(new GermplasmList(), advancingForm, germplasmNames, listDataItems,
				GermplasmTreeControllerTest.TEST_USER_ID, germplasmAttributes);

		// Check Attribute Objects created. Additional attributes are created for trials only
		final List<ImportedGermplasm> inputGermplasmList = advancingForm.getGermplasmList();
		for (int i = 0; i < inputGermplasmList.size(); i++) {
			final List<Attribute> attributes = germplasmAttributes.get(i).getRight();
			final Iterator<Attribute> attributeIterator = attributes.iterator();
			final ImportedGermplasm importedGermplasm = inputGermplasmList.get(i);

			// Expecting PLOTCODE, PLOT_NUMBER and INSTANCE_NUMBER attributes to be created per germplasm.
			// REP_NUMBER should not be created since basic import design only contains TRIAL_INSTANCE, ENTRY_NO and PLOT_NO variables
			Assert.assertEquals("Expecting # of Attribute objects per germplasm is 3", 3, attributes.size());
			final Attribute originAttribute = attributeIterator.next();
			Assert.assertNull("Expecting Attribute GID to be null", originAttribute.getGermplasmId());
			Assert.assertEquals("Expecting Attribute Location ID is same as germplasm's Location ID", advancingForm.getHarvestLocationId(),
					originAttribute.getLocationId().toString());
			Assert.assertEquals("Expecting Attribute User ID is same as germplasm's User ID", GermplasmTreeControllerTest.TEST_USER_ID,
					originAttribute.getUserId());
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
					plotAttribute.getUserId());
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
					instanceAttribute.getUserId());
			Assert.assertEquals("Expecting Attribute Date is current date", currentDate, instanceAttribute.getAdate());
			Assert.assertEquals("Expecting Attribute Type ID is PLOT_CODE id",
					Integer.valueOf(GermplasmTreeControllerTest.TRIAL_INSTANCE_FIELD_NO), instanceAttribute.getTypeId());
			Assert.assertEquals("Expecting Attribute Value is germplasm's plot number", importedGermplasm.getTrialInstanceNumber(),
					instanceAttribute.getAval());
		}

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
		final List<ImportedCrosses> importedCrosses = new ArrayList<>();
		final ImportedCrosses cross = new ImportedCrosses();
		cross.setFemaleDesig("FEMALE-12345");
		cross.setFemaleGid("12345");
		cross.setMaleDesig("MALE-54321");
		cross.setMaleGid("54321");
		cross.setGid("10021");
		cross.setDesig("Default name1");
		importedCrosses.add(cross);
		final ImportedCrosses cross2 = new ImportedCrosses();
		cross2.setFemaleDesig("FEMALE-9999");
		cross2.setFemaleGid("9999");
		cross2.setMaleDesig("MALE-8888");
		cross2.setMaleGid("8888");
		cross2.setGid("10022");
		cross2.setDesig("Default name2");
		importedCrosses.add(cross2);
		importedCrossesList.setImportedGermplasms(importedCrosses);

		return importedCrossesList;
	}

	private List<UserDefinedField> createNameTypes() {
		final List<UserDefinedField> nameTypes = new ArrayList<>();
		final UserDefinedField udf = new UserDefinedField();
		udf.setFcode(CrossingServiceImpl.USER_DEF_FIELD_CROSS_NAME[0]);
		nameTypes.add(udf);
		return nameTypes;
	}

	private List<Integer> createGermplasmIds() {
		final List<Integer> ids = new ArrayList<>();
		ids.add(Integer.valueOf(GermplasmTreeControllerTest.SAVED_CROSSES_GID1));
		ids.add(Integer.valueOf(GermplasmTreeControllerTest.SAVED_CROSSES_GID2));
		return ids;
	}

	private List<GermplasmListData> createGermplasmListData() {
		final List<GermplasmListData> listData = new ArrayList<>();

		final GermplasmListData data1 = new GermplasmListData();
		data1.setGid(Integer.valueOf(GermplasmTreeControllerTest.SAVED_CROSSES_GID1));
		data1.setDesignation("DESIG 1");
		data1.setEntryId(1);
		data1.setGroupName("GROUP 1");
		data1.setSeedSource("SEED 1");
		listData.add(data1);
		final GermplasmListData data2 = new GermplasmListData();
		data2.setGid(Integer.valueOf(GermplasmTreeControllerTest.SAVED_CROSSES_GID2));
		data2.setDesignation("DESIG 2");
		data2.setEntryId(2);
		data2.setGroupName("GROUP 2");
		data2.setSeedSource("SEED 2");
		listData.add(data2);

		return listData;
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

	private AdvancingNurseryForm createAdvancingNurseryForm(final boolean withReplicationNumber) {
		final AdvancingNurseryForm advancingNurseryForm = new AdvancingNurseryForm();
		final List<ImportedGermplasm> importedGermplasmList = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			importedGermplasmList.add(this.createImportedGermplasm(i, withReplicationNumber));
		}
		advancingNurseryForm.setHarvestYear("2015");
		advancingNurseryForm.setHarvestMonth("08");
		advancingNurseryForm.setHarvestLocationId("252");
		advancingNurseryForm.setGermplasmList(importedGermplasmList);
		return advancingNurseryForm;
	}

	private ImportedGermplasm createImportedGermplasm(final int gid, final boolean withReplicationNumber) {
		final String gidString = String.valueOf(gid);
		final String desig = GermplasmTreeControllerTest.GERMPLASM_NAME_PREFIX + gid;

		final ImportedGermplasm germplasm = new ImportedGermplasm();
		germplasm.setGid(gidString);
		germplasm.setEntryId(gid);
		germplasm.setEntryCode(gidString);
		germplasm.setDesig(desig);
		germplasm.setSource(GermplasmTreeControllerTest.LIST_NAME + ":" + gid);
		germplasm.setCross(gid + "/" + (gid + 1));
		germplasm.setSource("Import file");
		germplasm.setTrialInstanceNumber("1");
		germplasm.setPlotNumber(gidString);
		if (withReplicationNumber) {
			germplasm.setReplicationNumber("2");
		}

		final Name name = new Name();
		name.setGermplasmId(gid);
		name.setNval(desig);
		name.setNstat(1);
		germplasm.setNames(Collections.singletonList(name));
		return germplasm;
	}
}
