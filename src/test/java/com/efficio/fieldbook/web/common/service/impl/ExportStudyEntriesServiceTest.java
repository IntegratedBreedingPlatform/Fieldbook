
package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportRow;
import org.generationcp.commons.service.GermplasmExportService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryPropertyData;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ExportStudyEntriesServiceTest {

	private static final long LIST_DATE = 20141112L;
	private static final String SAMPLE_NOTES = "Sample Notes";
	private static final String LST = "LST";
	private static final String SAMPLE_DESCRIPTION = "Sample description";
	private static final String SAMPLE_LIST = "Sample List";
	private static final String DESIG_VALUE = "DESIG VALUE";
	private static final String CROSS_VALUE = "Cross value";
	private static final String SOURCE_VALUE = "Source value";
	private static final String ENTRY_CODE_VALUE = "9742";
	private static final String ENTRY_NO_VALUE = "1";
	private static final String GID_VALUE = "12345";
	private static final String NUMERIC_VARIABLE = "NUMERIC VARIABLE";
	private static final String TEST_METHOD = "TEST METHOD";
	private static final String TEST_SCALE = "TEST SCALE";
	private static final String TEST_PROPERTY = "TEST PROPERTY";
	private static final String TEST_DESCRIPTION = "TEST DESCRIPTION";

	private static final String CURRENT_USER_NAME = "User User";
	private static final int CURRENT_USER_ID = 1;
	private static final int LIST_ID = 1;
	// Columns
	private static final String ENTRY_NO = "entryNo";
	private static final String GID = "gid";
	private static final String ENTRY_CODE = "entryCode";
	private static final String DESIGNATION = "desig";
	private static final String PARENTAGE = "parentage";
	private static final String SEED_SOURCE = "seedSource";
	private static final String CHECK = "check";
	private static final String ENTRY_NUMBER_STORAGE = "entryNoStorage";

	private String testFileName;

	@Mock
	private OntologyService ontologyService;

	@Mock
	private UserSelection userSelection;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private GermplasmExportService germplasmExportService;

	@Mock
	private Workbook workbook;

	@Mock
	private StudyDetails studyDetails;

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Mock
	private StudyEntryService studyEntryService;

	@Mock
	GermplasmList germplasmList;

	@Mock
	InventoryDataManager inventoryDataManager;

	@InjectMocks
	private ExportStudyEntriesServiceImpl exportStudyEntryServiceImpl;

	@Before
	public void setUp() throws MiddlewareException {
		final List<ImportedGermplasm> importedGermplasms = this.setUpImportedGermplasm();

		MockitoAnnotations.initMocks(this);

		this.testFileName = "test.csv";

		Mockito.doReturn(ExportStudyEntriesServiceTest.CURRENT_USER_ID).when(this.contextUtil).getCurrentWorkbenchUserId();

		Mockito.when(this.ontologyService.getStandardVariable(TermId.ENTRY_NO.getId(), this.contextUtil.getCurrentProgramUUID()))
			.thenReturn(this.createStandardVariable(TermId.ENTRY_NO.getId(), ExportStudyEntriesServiceTest.ENTRY_NO));
		Mockito.when(this.ontologyService.getStandardVariable(TermId.DESIG.getId(), this.contextUtil.getCurrentProgramUUID()))
			.thenReturn(this.createStandardVariable(TermId.DESIG.getId(), ExportStudyEntriesServiceTest.DESIGNATION));
		Mockito.when(this.ontologyService.getStandardVariable(TermId.GID.getId(), this.contextUtil.getCurrentProgramUUID()))
			.thenReturn(this.createStandardVariable(TermId.GID.getId(), ExportStudyEntriesServiceTest.GID));
		Mockito.when(this.ontologyService.getStandardVariable(TermId.CROSS.getId(), this.contextUtil.getCurrentProgramUUID()))
			.thenReturn(this.createStandardVariable(TermId.CROSS.getId(), ExportStudyEntriesServiceTest.PARENTAGE));
		Mockito.when(this.ontologyService.getStandardVariable(TermId.ENTRY_CODE.getId(), this.contextUtil.getCurrentProgramUUID()))
			.thenReturn(this.createStandardVariable(TermId.ENTRY_CODE.getId(), ExportStudyEntriesServiceTest.ENTRY_CODE));
		Mockito.when(
			this.ontologyService.getStandardVariable(TermId.ENTRY_NUMBER_STORAGE.getId(), this.contextUtil.getCurrentProgramUUID()))
			.thenReturn(this.createStandardVariable(TermId.ENTRY_NUMBER_STORAGE.getId(),
				ExportStudyEntriesServiceTest.ENTRY_NUMBER_STORAGE));
		Mockito.when(this.ontologyService.getStandardVariable(TermId.ENTRY_TYPE.getId(), this.contextUtil.getCurrentProgramUUID()))
			.thenReturn(this.createStandardVariable(TermId.ENTRY_TYPE.getId(), ExportStudyEntriesServiceTest.CHECK));
		Mockito.when(this.userSelection.getWorkbook()).thenReturn(this.workbook);
		Mockito.when(this.workbook.getStudyDetails()).thenReturn(this.studyDetails);
		Mockito.doReturn(this.getPlotLevelList()).when(this.userSelection).getPlotsLevelList();
		Mockito.doReturn(this.getGermplasmList()).when(this.fieldbookMiddlewareService)
			.getGermplasmListById(ExportStudyEntriesServiceTest.LIST_ID);
		Mockito.doReturn(this.createStudyEntry()).when(this.studyEntryService)
			.getStudyEntries(1);
		Mockito.doReturn(ExportStudyEntriesServiceTest.CURRENT_USER_NAME).when(this.fieldbookMiddlewareService)
			.getOwnerListName(ExportStudyEntriesServiceTest.CURRENT_USER_ID);
		Mockito.doReturn("1010").when(this.fieldbookMiddlewareService).getOwnerListName(ArgumentMatchers.anyInt());

		final StandardVariable checkStandardVariable = new StandardVariable();
		checkStandardVariable.setEnumerations(Arrays.asList(new Enumeration(1, "T", "TEST ENTRY", 1)));
		Mockito.when(this.ontologyService.getStandardVariable(TermId.ENTRY_TYPE.getId(), this.contextUtil.getCurrentProgramUUID()))
			.thenReturn(checkStandardVariable);

		Mockito.when(ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(), TermId.SEED_AMOUNT_G.getId(), false))
			.thenReturn(this.createVariable(TermId.SEED_AMOUNT_G.getId()));
	}

	@Test
	public void testExportGermplasmListCSVForStudyManager() {

		try {
			this.exportStudyEntryServiceImpl.exportAsCSVFile(1, this.testFileName, this.getVisibleColumnMap());
			Mockito.verify(this.germplasmExportService, Mockito.times(1))
				.generateCSVFile(ArgumentMatchers.any(List.class), ArgumentMatchers.any(List.class),
					ArgumentMatchers.anyString());

		} catch (final GermplasmListExporterException e) {
			Assert.fail();
		} catch (final IOException e) {
			Assert.fail();
		}

	}

	@Test
	public void testExportGermplasmListCSVForNurseryManager() {

		try {
			this.exportStudyEntryServiceImpl.exportAsCSVFile(1, this.testFileName, this.getVisibleColumnMap());
			Mockito.verify(this.germplasmExportService, Mockito.times(1))
				.generateCSVFile(ArgumentMatchers.any(List.class), ArgumentMatchers.any(List.class),
					ArgumentMatchers.anyString());
		} catch (final GermplasmListExporterException e) {
			Assert.fail();
		} catch (final IOException e) {
			Assert.fail();
		}

	}

	@After
	public void tearDown() {
		final File file = new File(this.testFileName);
		file.deleteOnExit();
	}

	@Test
	public void testGetExportColumnHeadersFromTableStudy() {

		final List<ExportColumnHeader> exportColumnHeaders =
			this.exportStudyEntryServiceImpl.getExportColumnHeadersFromTable(this.getVisibleColumnMap());

		Assert.assertEquals(6, exportColumnHeaders.size());
		Assert.assertTrue(exportColumnHeaders.get(0).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(1).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(2).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(3).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(4).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(5).isDisplay());

		Assert.assertEquals(ExportStudyEntriesServiceTest.GID, exportColumnHeaders.get(0).getName());
		Assert.assertEquals(ExportStudyEntriesServiceTest.PARENTAGE, exportColumnHeaders.get(1).getName());
		Assert.assertEquals(ExportStudyEntriesServiceTest.ENTRY_NO, exportColumnHeaders.get(2).getName());
		Assert.assertEquals(ExportStudyEntriesServiceTest.DESIGNATION, exportColumnHeaders.get(3).getName());
		Assert.assertEquals(ExportStudyEntriesServiceTest.SEED_SOURCE, exportColumnHeaders.get(4).getName());
		Assert.assertEquals(ExportStudyEntriesServiceTest.ENTRY_CODE, exportColumnHeaders.get(5).getName());

	}

	@Test
	public void testGetExportColumnHeadersFromTableNursery() {

		final List<ExportColumnHeader> exportColumnHeaders =
			this.exportStudyEntryServiceImpl.getExportColumnHeadersFromTable(this.getVisibleColumnMap());

		Assert.assertEquals(6, exportColumnHeaders.size());
		Assert.assertTrue(exportColumnHeaders.get(0).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(1).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(2).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(3).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(4).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(5).isDisplay());

		Assert.assertEquals(ExportStudyEntriesServiceTest.GID, exportColumnHeaders.get(0).getName());
		Assert.assertEquals(ExportStudyEntriesServiceTest.PARENTAGE, exportColumnHeaders.get(1).getName());
		Assert.assertEquals(ExportStudyEntriesServiceTest.ENTRY_NO, exportColumnHeaders.get(2).getName());
		Assert.assertEquals(ExportStudyEntriesServiceTest.DESIGNATION, exportColumnHeaders.get(3).getName());
		Assert.assertEquals(ExportStudyEntriesServiceTest.SEED_SOURCE, exportColumnHeaders.get(4).getName());
		Assert.assertEquals(ExportStudyEntriesServiceTest.ENTRY_CODE, exportColumnHeaders.get(5).getName());

	}

	@Test
	public void testGetExportColumnValuesFromTableStudy() {

		final List<ExportRow> exportColumnValues =
			this.exportStudyEntryServiceImpl.getExportColumnValuesFromTable(1);

		Assert.assertEquals(1, exportColumnValues.size());

		final ExportRow row = exportColumnValues.get(0);

		Assert.assertEquals(ExportStudyEntriesServiceTest.DESIG_VALUE, row.getValueForColumn(TermId.DESIG.getId()));
		Assert.assertEquals(ExportStudyEntriesServiceTest.ENTRY_CODE_VALUE, row.getValueForColumn(TermId.ENTRY_CODE.getId()));
		Assert.assertEquals(ExportStudyEntriesServiceTest.CROSS_VALUE, row.getValueForColumn(TermId.CROSS.getId()));
		Assert.assertEquals(ExportStudyEntriesServiceTest.ENTRY_NO_VALUE, row.getValueForColumn(TermId.ENTRY_NO.getId()));
		Assert.assertEquals(ExportStudyEntriesServiceTest.GID_VALUE, row.getValueForColumn(TermId.GID.getId()));

	}

	@Test
	public void testGetExportColumnValuesFromTableNursery() {

		final List<ExportRow> exportRows =
			this.exportStudyEntryServiceImpl.getExportColumnValuesFromTable(1);

		Assert.assertEquals(1, exportRows.size());

		final ExportRow row = exportRows.get(0);

		Assert.assertEquals(ExportStudyEntriesServiceTest.DESIG_VALUE, row.getValueForColumn(TermId.DESIG.getId()));
		Assert.assertEquals(ExportStudyEntriesServiceTest.ENTRY_CODE_VALUE, row.getValueForColumn(TermId.ENTRY_CODE.getId()));
		Assert.assertEquals(ExportStudyEntriesServiceTest.CROSS_VALUE, row.getValueForColumn(TermId.CROSS.getId()));
		Assert.assertEquals(ExportStudyEntriesServiceTest.ENTRY_NO_VALUE, row.getValueForColumn(TermId.ENTRY_NO.getId()));
		Assert.assertEquals(ExportStudyEntriesServiceTest.GID_VALUE, row.getValueForColumn(TermId.GID.getId()));

	}

	@Test
	public void testExtractInventoryVariableMapFromVisibleColumnsInventoryVariablesAreVisible() {

		final Map<String, Boolean> visibleColumnsMap = this.getVisibleColumnMap();
		visibleColumnsMap.put(String.valueOf(TermId.SEED_AMOUNT_G.getId()), true);

		final Map<Integer, Variable> result =
			exportStudyEntryServiceImpl.extractInventoryVariableMapFromVisibleColumns(visibleColumnsMap);

		Assert.assertEquals("There are 1 inventory variables in visibleColumnsMap so the size of InventoryVariableMap should be 1.", 1,
			result.size());
		Assert.assertTrue(result.containsKey(TermId.SEED_AMOUNT_G.getId()));

	}

	@Test
	public void testExtractInventoryVariableMapFromVisibleColumnsInventoryVariablesAreNotVisible() {

		final Map<String, Boolean> visibleColumnsMap = this.getVisibleColumnMap();
		visibleColumnsMap.put(String.valueOf(TermId.SEED_AMOUNT_G.getId()), false);

		final Map<Integer, Variable> result =
			exportStudyEntryServiceImpl.extractInventoryVariableMapFromVisibleColumns(visibleColumnsMap);

		Assert.assertTrue(
			"There are 1 inventory variables in visibleColumnsMap but they are not visible so the size of InventoryVariableMap should be empty",
			result.isEmpty());
		Assert.assertFalse(result.containsKey(TermId.SEED_AMOUNT_G.getId()));

	}

	@Test
	public void testRemoveInventoryVariableMapFromVisibleColumns() {

		final Map<String, Boolean> visibleColumnsMap = this.getVisibleColumnMap();

		// Get the size of visibleColumnsMap before adding the inventory variables so that
		// we can compare the size of visibleColumnsMap after removing the inventory variables.
		final int visibleColumnsMapVariableCount = visibleColumnsMap.size();

		// Add inventory variables in visibleColumnsmap
		visibleColumnsMap.put(String.valueOf(TermId.SEED_AMOUNT_G.getId()), true);

		exportStudyEntryServiceImpl.removeInventoryVariableMapFromVisibleColumns(visibleColumnsMap);

		Assert
			.assertEquals("Expecting " + visibleColumnsMapVariableCount + " variables in visibleColumnsMap", visibleColumnsMapVariableCount,
				visibleColumnsMap.size());
		Assert.assertFalse(visibleColumnsMap.containsKey(TermId.SEED_AMOUNT_G.getId()));

	}

	private Variable createVariable(final int termid) {

		final Variable variable = new Variable();
		variable.setId(termid);
		return variable;

	}


	private Map<String, Boolean> getVisibleColumnMap() {
		final Map<String, Boolean> visibleColumnMap = new LinkedHashMap<String, Boolean>();

		visibleColumnMap.put(String.valueOf(TermId.GID.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.CROSS.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.ENTRY_NO.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.DESIG.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.ENTRY_CODE.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.ENTRY_NUMBER_STORAGE.getId()), false);
		visibleColumnMap.put(String.valueOf(TermId.ENTRY_TYPE.getId()), false);

		return visibleColumnMap;

	}

	private List<SettingDetail> getPlotLevelList() {
		final List<SettingDetail> plotLevelList = new ArrayList<>();

		for (final Entry<String, Boolean> entry : this.getVisibleColumnMap().entrySet()) {
			plotLevelList.add(this.generateSettingDetail(Integer.valueOf(entry.getKey())));
		}

		return plotLevelList;

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
			settingDetail.setPossibleValues(new ArrayList<ValueReference>());

		} catch (final MiddlewareException e) {
			// do nothing
		}

		return settingDetail;
	}

	private StandardVariable createStandardVariable(final int id, final String name) {
		final StandardVariable stdVar = new StandardVariable();
		stdVar.setId(id);
		stdVar.setName(name);
		stdVar.setDescription(ExportStudyEntriesServiceTest.TEST_DESCRIPTION);

		final Term prop = new Term();
		prop.setName(ExportStudyEntriesServiceTest.TEST_PROPERTY);
		stdVar.setProperty(prop);

		final Term scale = new Term();
		scale.setName(ExportStudyEntriesServiceTest.TEST_SCALE);
		stdVar.setScale(scale);

		final Term method = new Term();
		method.setName(ExportStudyEntriesServiceTest.TEST_METHOD);
		stdVar.setMethod(method);

		final Term dataType = new Term();
		dataType.setName(ExportStudyEntriesServiceTest.NUMERIC_VARIABLE);
		stdVar.setDataType(dataType);

		return stdVar;
	}

	private GermplasmList getGermplasmList() {
		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setName(ExportStudyEntriesServiceTest.SAMPLE_LIST);
		germplasmList.setUserId(ExportStudyEntriesServiceTest.CURRENT_USER_ID);
		germplasmList.setDescription(ExportStudyEntriesServiceTest.SAMPLE_DESCRIPTION);
		germplasmList.setType(ExportStudyEntriesServiceTest.LST);
		germplasmList.setDate(ExportStudyEntriesServiceTest.LIST_DATE);
		germplasmList.setNotes(ExportStudyEntriesServiceTest.SAMPLE_NOTES);

		return germplasmList;
	}

	private List<StudyEntryDto> createStudyEntry() {
		final List<StudyEntryDto> studyEntries = new ArrayList<>();
		final StudyEntryDto studyEntry = new StudyEntryDto(Integer.valueOf(ExportStudyEntriesServiceTest.ENTRY_NO_VALUE),
				Integer.valueOf(ExportStudyEntriesServiceTest.ENTRY_NO_VALUE),
			Integer.valueOf(ExportStudyEntriesServiceTest.GID_VALUE), ExportStudyEntriesServiceTest.DESIG_VALUE);
		studyEntry.getProperties().put(TermId.CROSS.getId(), new StudyEntryPropertyData(ExportStudyEntriesServiceTest.CROSS_VALUE));
		studyEntries.add(studyEntry);
		return studyEntries;
	}

	private List<ImportedGermplasm> setUpImportedGermplasm() {
		final List<ImportedGermplasm> importedGermplasms = new ArrayList<>();
		final ImportedGermplasm importedGermplasm = new ImportedGermplasm();

		importedGermplasm.setGid(ExportStudyEntriesServiceTest.GID_VALUE);
		importedGermplasm.setEntryCode(ExportStudyEntriesServiceTest.ENTRY_CODE_VALUE);
		importedGermplasm.setEntryNumber(1);
		importedGermplasm.setSource(ExportStudyEntriesServiceTest.SOURCE_VALUE);
		importedGermplasm.setCross(ExportStudyEntriesServiceTest.CROSS_VALUE);
		importedGermplasm.setDesig(ExportStudyEntriesServiceTest.DESIG_VALUE);
		importedGermplasms.add(importedGermplasm);

		return importedGermplasms;
	}
}
