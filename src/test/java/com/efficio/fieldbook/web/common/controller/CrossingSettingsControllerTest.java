package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.common.bean.CrossImportSettings;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.CrossingTemplateExportException;
import com.efficio.fieldbook.web.common.exception.InvalidInputException;
import com.efficio.fieldbook.web.common.service.CrossingService;
import com.efficio.fieldbook.web.common.service.impl.CrossingTemplateExcelExporter;
import com.efficio.fieldbook.web.util.CrossesListUtil;
import com.google.common.collect.Lists;
import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.commons.data.initializer.ImportedCrossesTestDataInitializer;
import org.generationcp.commons.parsing.pojo.ImportedCross;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmParent;
import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.service.SettingsPresetService;
import org.generationcp.commons.service.impl.SettingsPresetServiceImpl;
import org.generationcp.commons.settings.AdditionalDetailsSetting;
import org.generationcp.commons.settings.BreedingMethodSetting;
import org.generationcp.commons.settings.CrossNameSetting;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.tool.ToolService;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.PresetService;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.MethodType;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.germplasm.GermplasmParent;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class CrossingSettingsControllerTest {

	private static final String TEST_SEQUENCE_NAME_VALUE = "PRE1";
	private static final String SUCCESS_VALUE = "1";
	private static final String TEST_SETTING_NAME = "mySettingName";
	private static final Integer TEST_BREEDING_METHOD_ID = 1;
	private static final String SETTING_PREFIX = "PRE";
	private static final String SETTING_SEPARATOR = "-";
	private static final Integer TEST_PROGRAM_PRESET_ID = 1;
	private static final String TEST_PROGRAM_ID = "2";
	private static final int DUMMY_STUDY_ID = 2;
	private static final long DUMMY_TOOL_ID = 2;
	private static final int NUMBER_OF_MONTHS = 12;
	private static final String DUMMY_ABS_PATH = "dummy/abs/path";
	private static final String TEST_ENTRY_CODE = "testEntryCode";
	private static final String TEST_SEED_SOURCE = "testSeedSource";
	private static final String TEST_MALE_PARENT1 = "testMaleParent1";
	private static final String TEST_MALE_PARENT2 = "testMaleParent2";
	private static final int ENTRY_ID = 56;
	private static final String TEST_FEMALE_PARENT = "testFemaleParent";
	private static final Integer MGID1 = 836;
	private static final Integer MGID2 = 987;
	private static final Integer FGID = 535;
	private static final String TEST_DUPLICATE = "SID-1";
	private static final Integer FEMALE_PLOT = 11;
	private static final String BREEDING_METHOD = "Test Method";
	private static final Integer MALE_PLOT1 = 21;
	private static final Integer MALE_PLOT2 = 22;
	private static final String MALE_STUDY_NAME = "maleStudy";
	private static final Integer CROSSING_DATE = 20161212;
	private static final String NOTES = "Test notes";
	private static final String FEMALE_PEDIGREE = RandomStringUtils.random(20);
	private static final String MALE_PEDIGREE1 = RandomStringUtils.random(20);
	private static final String MALE_PEDIGREE2 = RandomStringUtils.random(20);
	private static final String FEMALE_CROSS = RandomStringUtils.random(20);
	private static final String MALE_CROSS1 = RandomStringUtils.random(20);
	private static final String MALE_CROSS2 = RandomStringUtils.random(20);

	private ImportedCrossesTestDataInitializer importedCrossesTestDataInitializer;
	@Mock
	private CrossExpansionProperties crossExpansionProperties;

	@Mock
	private PresetService presetService;

	@Mock
	private UserSelection studySelection;

	@Mock
	private HttpServletRequest request;

	@Mock
	private CrossingTemplateExcelExporter crossingTemplateExcelExporter;

	@Mock
	private MessageSource messageSource;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private CrossingService crossingService;

	@Mock
	private ToolService toolService;

	@Mock
	private StudyEntryService studyEntryService;

	@Mock
	private BreedingMethodService breedingMethodService;

	private CrossesListUtil crossesListUtil;

	@Spy
	private final SettingsPresetService settingsPresetService = new SettingsPresetServiceImpl();

	@InjectMocks
	private CrossingSettingsController crossingSettingsController;

	@Before
	public void setup() {
		this.crossesListUtil = new CrossesListUtil();
		this.crossesListUtil.setOntologyDataManager(this.ontologyDataManager);
		this.crossingSettingsController.setCrossesListUtil(this.crossesListUtil);
		this.mockMappingOfHeadersToOntology();

		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(CrossingSettingsControllerTest.DUMMY_STUDY_ID);
		workbook.setStudyDetails(studyDetails);
		Mockito.when(this.studySelection.getWorkbook()).thenReturn(workbook);
		Mockito.when(this.crossExpansionProperties.getHybridBreedingMethods()).thenReturn(new HashSet<Integer>(Arrays.asList(1)));
		Mockito.when(this.germplasmDataManager.getMethodCodeByMethodIds(this.crossExpansionProperties.getHybridBreedingMethods()))
				.thenReturn(new ArrayList<String>(Arrays.asList("TCR")));
		this.importedCrossesTestDataInitializer = new ImportedCrossesTestDataInitializer();
	}

	private void mockMappingOfHeadersToOntology() {
		Mockito.when(this.ontologyDataManager.getTermById(TermId.FEMALE_PARENT.getId()))
				.thenReturn(this.getTerm(ColumnLabels.FEMALE_PARENT));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.FGID.getId())).thenReturn(this.getTerm(ColumnLabels.FGID));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.MALE_PARENT.getId())).thenReturn(this.getTerm(ColumnLabels.MALE_PARENT));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.MGID.getId())).thenReturn(this.getTerm(ColumnLabels.MGID));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS_FEMALE_GID.getId()))
				.thenReturn(this.getTerm(ColumnLabels.CROSS_FEMALE_GID));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS_MALE_GID.getId()))
				.thenReturn(this.getTerm(ColumnLabels.CROSS_MALE_GID));
	}

	private Term getTerm(final ColumnLabels columnLabel) {
		final int id = columnLabel.getTermId().getId();
		final String name = columnLabel.getName();
		return new Term(id, name, name);
	}

	@Test
	public void testGenerateNextNameInSequenceSuccess() throws InvalidInputException {

		try {
			Mockito.doReturn(CrossingSettingsControllerTest.TEST_SEQUENCE_NAME_VALUE).when(this.crossingService)
					.getNextNameInSequence(ArgumentMatchers.isNull());
			final Map<String, String> output =
					this.crossingSettingsController.generateSequenceValue(Mockito.mock(CrossSetting.class), this.request);

			Assert.assertNotNull(output);
			Assert.assertEquals(CrossingSettingsControllerTest.SUCCESS_VALUE, output.get("success"));
			Assert.assertEquals(CrossingSettingsControllerTest.TEST_SEQUENCE_NAME_VALUE, output.get("sequenceValue"));
		} catch (final RuntimeException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGenerateNextNameInSequenceInvalidInputError() throws InvalidInputException {

		final String errorMessage = "Please select a starting sequence number larger than 10";

		Mockito.doThrow(new InvalidInputException(errorMessage)).when(this.crossingService)
				.getNextNameInSequence(ArgumentMatchers.isNull());

		final Map<String, String> result = this.crossingSettingsController.generateSequenceValue(Mockito.mock(CrossSetting.class), this.request);
		Assert.assertEquals(errorMessage, result.get(CrossingSettingsController.ERROR));
		Assert.assertEquals("0", result.get(CrossingSettingsController.SUCCESS_KEY));

	}

	@Test
	public void testGenerateNextNameInSequenceRuntimeError() throws InvalidInputException {

		final String errorMessage = "runtime error";

		Mockito.doThrow(new InvalidInputException(errorMessage)).when(this.crossingService)
				.getNextNameInSequence(ArgumentMatchers.isNull());

		final Map<String, String> result = this.crossingSettingsController.generateSequenceValue(Mockito.mock(CrossSetting.class), this.request);
		Assert.assertEquals(errorMessage, result.get(CrossingSettingsController.ERROR));
		Assert.assertEquals("0", result.get(CrossingSettingsController.SUCCESS_KEY));

	}

	@Test
	public void testSubmitCrossingSetting() {
		final CrossSetting setting = Mockito.mock(CrossSetting.class);
		final Map<String, Object> output = this.crossingSettingsController.submitCrossSettings(setting);

		Mockito.verify(this.studySelection).setCrossSettings(setting);

		Assert.assertNotNull(output);
		Assert.assertEquals(1, output.get("success"));
	}

	@Test
	public void testSaveAndSubmitCrossSettingNewSetting() {
		try {
			final Tool fieldbookTool = new Tool();
			fieldbookTool.setToolId(CrossingSettingsControllerTest.DUMMY_TOOL_ID);
			final CrossingSettingsController mole = Mockito.spy(this.crossingSettingsController);
			final CrossSetting sampleSetting = this.constructCrossSetting();
			Mockito.doReturn(CrossingSettingsControllerTest.TEST_PROGRAM_ID).when(mole).getCurrentProgramID();
			Mockito.doReturn(fieldbookTool).when(this.toolService).getToolWithName(ToolName.FIELDBOOK_WEB.getName());

			Mockito.doReturn(new ArrayList<ProgramPreset>()).when(this.presetService)
					.getProgramPresetFromProgramAndTool(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyString());

			final ArgumentCaptor<ProgramPreset> param = ArgumentCaptor.forClass(ProgramPreset.class);
			mole.submitAndSaveCrossSettings(this.constructCrossSetting());

			Mockito.verify(this.presetService).saveOrUpdateProgramPreset(param.capture());

			final ProgramPreset captured = param.getValue();
			Assert.assertEquals(CrossingSettingsControllerTest.TEST_SETTING_NAME, captured.getName());
			Assert.assertEquals(this.settingsPresetService.convertPresetSettingToXml(sampleSetting, CrossSetting.class),
					captured.getConfiguration());

			// we verify that the program preset that we have is blank
			Assert.assertEquals(0, captured.getProgramPresetId());

		} catch (final MiddlewareQueryException | JAXBException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testSaveAndSubmitCrossSettingPreviousSetting() {
		try {
			final Tool fieldbookTool = new Tool();
			fieldbookTool.setToolId(CrossingSettingsControllerTest.DUMMY_TOOL_ID);
			final CrossingSettingsController mole = Mockito.spy(this.crossingSettingsController);
			final CrossSetting sampleSetting = this.constructCrossSetting();
			Mockito.doReturn(CrossingSettingsControllerTest.TEST_PROGRAM_ID).when(mole).getCurrentProgramID();
			Mockito.doReturn(fieldbookTool).when(this.toolService).getToolWithName(ToolName.FIELDBOOK_WEB.getName());

			Mockito.doReturn(this.constructDummyPresetList()).when(this.presetService)
					.getProgramPresetFromProgramAndTool(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyString());

			final ArgumentCaptor<ProgramPreset> param = ArgumentCaptor.forClass(ProgramPreset.class);
			mole.submitAndSaveCrossSettings(this.constructCrossSetting());

			Mockito.verify(this.presetService).saveOrUpdateProgramPreset(param.capture());

			final ProgramPreset captured = param.getValue();
			Assert.assertEquals(CrossingSettingsControllerTest.TEST_SETTING_NAME, captured.getName());
			Assert.assertEquals(this.settingsPresetService.convertPresetSettingToXml(sampleSetting, CrossSetting.class),
					captured.getConfiguration());

			// we verify that the program preset that we have is blank
			Assert.assertEquals(CrossingSettingsControllerTest.TEST_PROGRAM_PRESET_ID.longValue(), captured.getProgramPresetId());

		} catch (final MiddlewareQueryException | JAXBException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetHarvestMonth() {
		final List<Map<String, String>> harvestMonths = this.crossingSettingsController.getHarvestMonths();

		Assert.assertNotNull(harvestMonths);
		Assert.assertEquals(CrossingSettingsControllerTest.NUMBER_OF_MONTHS, harvestMonths.size());

		for (final Map<String, String> harvestMonth : harvestMonths) {
			Assert.assertTrue(harvestMonth.containsKey(CrossingSettingsController.ID));
			Assert.assertTrue(harvestMonth.containsKey(CrossingSettingsController.TEXT));

			Assert.assertEquals(2, harvestMonth.get(CrossingSettingsController.ID).length());
		}
	}

	@Test
	public void testGetHarvestYears() {
		final List<String> harvestYears = this.crossingSettingsController.getHarvestYears();

		Assert.assertNotNull(harvestYears);
		Assert.assertEquals(CrossingSettingsController.YEAR_INTERVAL * 2 + 1, harvestYears.size());

		final String firstDisplayed = harvestYears.get(0);

		final int currentYearIndex = harvestYears.size() / 2;
		final String currentYearDisplayed = harvestYears.get(currentYearIndex);

		final Calendar cal = DateUtil.getCalendarInstance();
		Assert.assertEquals(Integer.toString(cal.get(Calendar.YEAR) + 10), firstDisplayed);
		Assert.assertEquals(Integer.toString(cal.get(Calendar.YEAR)), currentYearDisplayed);
	}

	@Test
	public void testRetrieveImportSettings() {
		try {
			final Tool fieldbookTool = new Tool();
			fieldbookTool.setToolId(CrossingSettingsControllerTest.DUMMY_TOOL_ID);
			final CrossingSettingsController mole = Mockito.spy(this.crossingSettingsController);
			Mockito.doReturn(CrossingSettingsControllerTest.TEST_PROGRAM_ID).when(mole).getCurrentProgramID();
			Mockito.doReturn(fieldbookTool).when(this.toolService).getToolWithName(ToolName.FIELDBOOK_WEB.getName());

			Mockito.doReturn(this.constructDummyPresetList()).when(this.presetService)
					.getProgramPresetFromProgramAndTool(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyString());

			final List<CrossImportSettings> output = mole.getAvailableCrossImportSettings();
			Assert.assertTrue(output.size() > 0);
			final CrossImportSettings setting = output.get(0);
			Assert.assertEquals(CrossingSettingsControllerTest.TEST_SETTING_NAME, setting.getName());
			Assert.assertEquals(CrossingSettingsControllerTest.TEST_BREEDING_METHOD_ID, setting.getBreedingMethodID());
			Assert.assertEquals(CrossingSettingsControllerTest.SETTING_PREFIX, setting.getCrossPrefix());
			Assert.assertEquals(CrossingSettingsControllerTest.SETTING_SEPARATOR, setting.getParentageDesignationSeparator());
		} catch (final MiddlewareQueryException | JAXBException e) {
			Assert.fail(e.getMessage());
		}

	}

	@Test
	public void testDoCrossingExportSuccess() throws Exception {

		Mockito.when(this.crossingTemplateExcelExporter.export(ArgumentMatchers.anyInt(), ArgumentMatchers.isNull(), ArgumentMatchers.anyInt()))
				.thenReturn(new FileExportInfo(CrossingSettingsControllerTest.DUMMY_ABS_PATH, CrossingSettingsControllerTest.DUMMY_ABS_PATH));

		final Map<String, Object> jsonResult = this.crossingSettingsController.doCrossingExport();

		Assert.assertEquals("should return success", Boolean.TRUE, jsonResult.get("isSuccess"));
		Assert.assertEquals("should return the correct output path", CrossingSettingsControllerTest.DUMMY_ABS_PATH,
				jsonResult.get("outputFilename"));
	}

	@Test
	public void testDoCrossingExportFail() throws Exception {
		final Workbook wb = Mockito.mock(Workbook.class);
		final StudyDetails studyDetails = Mockito.mock(StudyDetails.class);

		Mockito.when(wb.getStudyDetails()).thenReturn(studyDetails);
		Mockito.when(studyDetails.getId()).thenReturn(CrossingSettingsControllerTest.DUMMY_STUDY_ID);
		Mockito.when(wb.getStudyName()).thenReturn("dummy study name");
		Mockito.when(this.studySelection.getWorkbook()).thenReturn(wb);

		Mockito.when(this.crossingTemplateExcelExporter.export(ArgumentMatchers.anyInt(), ArgumentMatchers.anyString(), ArgumentMatchers.anyInt()))
				.thenThrow(new CrossingTemplateExportException("export.error"));

		Mockito.when(this.messageSource.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.any(String[].class), ArgumentMatchers.anyString(),
				ArgumentMatchers.eq(LocaleContextHolder.getLocale()))).thenReturn("export.error");

		final Map<String, Object> jsonResult = this.crossingSettingsController.doCrossingExport();

		Assert.assertEquals("should return success", Boolean.FALSE, jsonResult.get("isSuccess"));
		Assert.assertEquals("should return the correct error message", "export.error", jsonResult.get("errorMessage"));
	}

	@Test
	public void testDeleteCrossList() {
		final Integer crossListId = 1;
		this.crossingSettingsController.deleteCrossList(crossListId);

		Mockito.verify(this.germplasmListManager, times(1)).deleteGermplasmListByListIdPhysically(crossListId);
	}

	@Test
	public void testDeleteSetting() {
		final int programPresetId = 1;
		this.crossingSettingsController.deleteCrossSetting(programPresetId);

		Mockito.verify(this.presetService, times(1)).deleteProgramPreset(programPresetId);
	}

	@Test
	public void testGetHybridMethods() {
		final Set<Integer> hybridMethods = this.crossingSettingsController.getHybridMethods();
		Assert.assertNotNull("The hybrid methods should not be null", hybridMethods);
		Assert.assertFalse("The Hybrid methods should not be empty", hybridMethods.isEmpty());

	}

	@Test
	public void testCheckForHybridMethodsTrue() {
		final List<ImportedCross> importedCrosses = this.importedCrossesTestDataInitializer.createImportedCrossesList(1, true);
		Assert.assertTrue("The imported crosses should have hybrid methods",
				this.crossingSettingsController.checkForHybridMethods(importedCrosses));
	}

	@Test
	public void testCheckForHybridMethodsFalse() {
		final List<ImportedCross> importedCrosses = this.importedCrossesTestDataInitializer.createImportedCrossesList(1, false);
		Assert.assertFalse("The imported crosses should not have hybrid methods",
				this.crossingSettingsController.checkForHybridMethods(importedCrosses));
	}

	public List<ProgramPreset> constructDummyPresetList() throws JAXBException {
		final ProgramPreset existing = new ProgramPreset();
		existing.setName(CrossingSettingsControllerTest.TEST_SETTING_NAME);
		existing.setProgramPresetId(CrossingSettingsControllerTest.TEST_PROGRAM_PRESET_ID);
		existing.setConfiguration(this.settingsPresetService.convertPresetSettingToXml(this.constructCrossSetting(), CrossSetting.class));

		final List<ProgramPreset> presetList = new ArrayList<>();
		presetList.add(existing);

		return presetList;
	}

	private CrossSetting constructCrossSetting() {
		final CrossSetting setting = new CrossSetting();
		setting.setName(CrossingSettingsControllerTest.TEST_SETTING_NAME);

		final BreedingMethodSetting methodSetting =
				new BreedingMethodSetting(CrossingSettingsControllerTest.TEST_BREEDING_METHOD_ID, false, false);
		setting.setBreedingMethodSetting(methodSetting);

		final CrossNameSetting nameSetting = new CrossNameSetting();
		nameSetting.setPrefix(CrossingSettingsControllerTest.SETTING_PREFIX);
		nameSetting.setSeparator(CrossingSettingsControllerTest.SETTING_SEPARATOR);
		setting.setCrossNameSetting(nameSetting);
		final AdditionalDetailsSetting additionalDetailsSetting = new AdditionalDetailsSetting(0, "");
		setting.setAdditionalDetailsSetting(additionalDetailsSetting);

		return setting;
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetImportedCrossesListSuccess() {

		final List<GermplasmListData> germplasmListDatas = new ArrayList<>();
		final GermplasmList germplasmList = new GermplasmList();
		final GermplasmListData germplasmListData = new GermplasmListData(771, germplasmList, 45, CrossingSettingsControllerTest.ENTRY_ID,
			CrossingSettingsControllerTest.TEST_SEED_SOURCE, "testGroupName", 0, 5);
		germplasmListData.addMaleParent(new GermplasmParent(CrossingSettingsControllerTest.MGID1, CrossingSettingsControllerTest.TEST_MALE_PARENT1, CrossingSettingsControllerTest.MALE_PEDIGREE1));
		germplasmListData.setFemaleParent(new GermplasmParent(CrossingSettingsControllerTest.FGID, CrossingSettingsControllerTest.TEST_FEMALE_PARENT, CrossingSettingsControllerTest.FEMALE_PEDIGREE));

		germplasmListDatas.add(germplasmListData);
		Mockito.when(this.germplasmListManager.retrieveGermplasmListDataWithParents(80)).thenReturn(germplasmListDatas);
		Mockito.when(this.germplasmListManager.getGermplasmListById(80)).thenReturn(germplasmList);
		final UserDefinedField userDefinedField = new UserDefinedField();
		Mockito.when(this.germplasmDataManager
				.getUserDefinedFieldByTableTypeAndCode(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
				.thenReturn(userDefinedField);

		final Map<String, Object> testResponseMap = this.crossingSettingsController.getImportedCrossesList(false, "80");
		final List<String> tableHeaderList = (List<String>) testResponseMap.get(CrossesListUtil.TABLE_HEADER_LIST);
		final List<Map<String, Object>> testMasterList = (List<Map<String, Object>>) testResponseMap.get(CrossesListUtil.LIST_DATA_TABLE);

		Assert.assertEquals("The master list should contain 1 record: ", 1, testMasterList.size());
		final Map<String, Object> data = testMasterList.get(0);
		Assert.assertEquals("The master list should contain 1 record: ", 1, testMasterList.size());
		Assert.assertEquals(CrossingSettingsControllerTest.ENTRY_ID, data.get(tableHeaderList.get(CrossesListUtil.ENTRY_INDEX)));
		Assert.assertEquals(CrossingSettingsControllerTest.FGID.toString(), data.get(tableHeaderList.get(CrossesListUtil.FGID_INDEX)));
		Assert.assertEquals(CrossingSettingsControllerTest.TEST_FEMALE_PARENT + "/" + CrossingSettingsControllerTest.TEST_MALE_PARENT1, data.get(tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX)));
		Assert.assertEquals(CrossingSettingsControllerTest.TEST_SEED_SOURCE, data.get(tableHeaderList.get(CrossesListUtil.SOURCE_INDEX)));
		Assert.assertEquals(Arrays.asList(CrossingSettingsControllerTest.MGID1), data.get(tableHeaderList.get(CrossesListUtil.MGID_INDEX)));
		Assert.assertEquals(CrossingSettingsControllerTest.TEST_FEMALE_PARENT, data.get(ColumnLabels.FEMALE_PARENT.name()));
		Assert.assertEquals(Arrays.asList(CrossingSettingsControllerTest.TEST_MALE_PARENT1), data.get(ColumnLabels.MALE_PARENT.name()));
		Assert.assertNull(data.get(tableHeaderList.get(CrossesListUtil.DUPLICATE_INDEX)));
		Assert.assertEquals(CrossingSettingsControllerTest.FEMALE_PEDIGREE, data.get(tableHeaderList.get(CrossesListUtil.FEMALE_PEDIGREE)));
		Assert.assertEquals(CrossingSettingsControllerTest.MALE_PEDIGREE1, data.get(tableHeaderList.get(CrossesListUtil.MALE_PEDIGREE)));
	}

	@Test
	public void testGetImportedCrossesListEmpty() {
		final Map<String, Object> testResponseMap = this.crossingSettingsController.getImportedCrossesList(false);
		Assert.assertTrue("The response map should be empty", testResponseMap.isEmpty());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetImportedCrossesListWithSessionData() {
		this.fillUpUserSelectionWithImportedCrossTestData();

		final UserDefinedField userDefinedField = new UserDefinedField();
		Mockito.when(this.germplasmDataManager
				.getUserDefinedFieldByTableTypeAndCode(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
				.thenReturn(userDefinedField);

		final Map<String, Object> testResponseMap = this.crossingSettingsController.getImportedCrossesList(false);
		Assert.assertFalse("The response map should not be empty", testResponseMap.isEmpty());

		final List<String> tableHeaderList = (List<String>) testResponseMap.get(CrossesListUtil.TABLE_HEADER_LIST);
		final List<Map<String, Object>> testMasterList = (List<Map<String, Object>>) testResponseMap.get(CrossesListUtil.LIST_DATA_TABLE);
		final Map<String, Object> data = testMasterList.get(0);
		Assert.assertEquals("The master list should contain 1 record: ", 1, testMasterList.size());
		Assert.assertEquals(CrossingSettingsControllerTest.ENTRY_ID, data.get(tableHeaderList.get(CrossesListUtil.ENTRY_INDEX)));
		Assert.assertEquals(CrossingSettingsControllerTest.FGID.toString(), data.get(tableHeaderList.get(CrossesListUtil.FGID_INDEX)));
		Assert.assertEquals(CrossingSettingsControllerTest.TEST_FEMALE_PARENT + "/" + CrossingSettingsControllerTest.TEST_MALE_PARENT1, data.get(tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX)));
		Assert.assertEquals(CrossingSettingsControllerTest.TEST_SEED_SOURCE, data.get(tableHeaderList.get(CrossesListUtil.SOURCE_INDEX)));
		Assert.assertEquals(Arrays.asList(CrossingSettingsControllerTest.MGID1), data.get(tableHeaderList.get(CrossesListUtil.MGID_INDEX)));
		Assert.assertEquals(CrossingSettingsControllerTest.FEMALE_CROSS, data.get(ColumnLabels.FEMALE_PARENT.name()));
		Assert.assertEquals(Arrays.asList(CrossingSettingsControllerTest.MALE_CROSS1), data.get(ColumnLabels.MALE_PARENT.name()));
		Assert.assertEquals(CrossingSettingsControllerTest.BREEDING_METHOD, data.get(tableHeaderList.get(CrossesListUtil.BREEDING_METHOD_INDEX)));
		Assert.assertEquals(CrossingSettingsControllerTest.TEST_DUPLICATE, data.get(tableHeaderList.get(CrossesListUtil.DUPLICATE_INDEX)));
		Assert.assertEquals(CrossingSettingsControllerTest.FEMALE_PEDIGREE, data.get(tableHeaderList.get(CrossesListUtil.FEMALE_PEDIGREE)));
		Assert.assertEquals(CrossingSettingsControllerTest.MALE_PEDIGREE1, data.get(tableHeaderList.get(CrossesListUtil.MALE_PEDIGREE)));
	}

	@Test
	public void testValidateBreedingMethodsBasedOnImportFileWithNonGenerativeMethod() {
		final ImportedCross cross = new ImportedCross();
		cross.setRawBreedingMethod("UBM");
		final ImportedCrossesList importedCrossesList = new ImportedCrossesList();
		importedCrossesList.setImportedGermplasms(Collections.singletonList(cross));
		Mockito.when(this.studySelection.getImportedCrossesList()).thenReturn(importedCrossesList);
		Mockito.when(this.breedingMethodService.searchBreedingMethods(ArgumentMatchers.any(BreedingMethodSearchRequest.class), ArgumentMatchers.any(), ArgumentMatchers.isNull()))
			.thenReturn(new ArrayList<>());
		final String errorMessage = "error.crossing.non.generative.method";
		Mockito.when(this.messageSource.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.any(String[].class),
			ArgumentMatchers.eq(LocaleContextHolder.getLocale()))).thenReturn(errorMessage);
		final Map<String, Object> result = this.crossingSettingsController.validateBreedingMethods(null);
		Assert.assertEquals(errorMessage, result.get(CrossingSettingsController.ERROR));
	}

	@Test
	public void testValidateBreedingMethodsBasedOnImportFileWithMprgnEqualsOne() {
		final ImportedCross cross = new ImportedCross();
		cross.setRawBreedingMethod("UBM");
		final ImportedCrossesList importedCrossesList = new ImportedCrossesList();
		importedCrossesList.setImportedGermplasms(Collections.singletonList(cross));
		Mockito.when(this.studySelection.getImportedCrossesList()).thenReturn(importedCrossesList);
		final BreedingMethodDTO method = new BreedingMethodDTO();
		method.setNumberOfProgenitors(1);
		Mockito.when(this.breedingMethodService.searchBreedingMethods(ArgumentMatchers.any(BreedingMethodSearchRequest.class), ArgumentMatchers.any(), ArgumentMatchers.isNull()))
			.thenReturn(Collections.singletonList(method));
		final String errorMessage = "error.crossing.method.mprgn.equals.one";
		Mockito.when(this.messageSource.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.any(String[].class),
			ArgumentMatchers.eq(LocaleContextHolder.getLocale()))).thenReturn(errorMessage);
		final Map<String, Object> result = this.crossingSettingsController.validateBreedingMethods(null);
		Assert.assertEquals(errorMessage, result.get(CrossingSettingsController.ERROR));
	}

	@Test
	public void testValidateBreedingMethodNonGenerative() {
		final Method method = new Method();
		method.setMid(1);
		method.setMtype(MethodType.DERIVATIVE.getCode());
		Mockito.when(this.germplasmDataManager.getMethodByID(method.getMid())).thenReturn(method);
		final String errorMessage = "error.crossing.selected.non.generative.method";
		Mockito.when(this.messageSource.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.any(String[].class),
			ArgumentMatchers.eq(LocaleContextHolder.getLocale()))).thenReturn(errorMessage);
		final Map<String, Object> result = this.crossingSettingsController.validateBreedingMethods(method.getMid());
		Assert.assertEquals(errorMessage, result.get(CrossingSettingsController.ERROR));
	}

	@Test
	public void testValidateBreedingMethodMprgnEqualsOne() {
		final Method method = new Method();
		method.setMid(1);
		method.setMtype(MethodType.GENERATIVE.getCode());
		method.setMprgn(1);
		Mockito.when(this.germplasmDataManager.getMethodByID(method.getMid())).thenReturn(method);
		final String errorMessage = "error.crossing.selected.method.mprgn.equals.one";
		Mockito.when(this.messageSource.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.any(String[].class),
			ArgumentMatchers.eq(LocaleContextHolder.getLocale()))).thenReturn(errorMessage);
		final Map<String, Object> result = this.crossingSettingsController.validateBreedingMethods(method.getMid());
		Assert.assertEquals(errorMessage, result.get(CrossingSettingsController.ERROR));
	}

	@Test
	public void testSetParentsInformation() {
		final List<ImportedCross> crosses = this.createImportedCrossesList(true).getImportedCrosses();
		final Map<Integer, String[]> parentsMap = new HashMap<>();
		final String newFemalePedigree = RandomStringUtils.randomAlphabetic(20);
		final String newFemaleCross = RandomStringUtils.randomAlphabetic(20);
		final String newMalePedigree1 = RandomStringUtils.randomAlphabetic(20);
		final String newMaleCross1 = RandomStringUtils.randomAlphabetic(20);
		final String newMalePedigree2 = RandomStringUtils.randomAlphabetic(20);
		final String newMaleCross2 = RandomStringUtils.randomAlphabetic(20);
		parentsMap.put(CrossingSettingsControllerTest.FGID, new String[] {newFemalePedigree, newFemaleCross});
		parentsMap.put(CrossingSettingsControllerTest.MGID1, new String[] {newMalePedigree1, newMaleCross1});
		parentsMap.put(CrossingSettingsControllerTest.MGID2, new String[] {newMalePedigree2, newMaleCross2});
		Mockito.when(this.germplasmDataManager.getParentsInfoByGIDList(Arrays.asList(CrossingSettingsControllerTest.MGID1,
				CrossingSettingsControllerTest.MGID2, CrossingSettingsControllerTest.FGID))).thenReturn(parentsMap);

		this.crossingSettingsController.setParentsInformation(crosses);
		final ImportedCross cross = crosses.get(0);
		Assert.assertEquals(newFemalePedigree, cross.getFemalePedigree());
		Assert.assertEquals(newFemaleCross, cross.getFemaleCross());
		Assert.assertEquals(newMalePedigree1, cross.getMaleParents().get(0).getPedigree());
		Assert.assertEquals(newMaleCross1,cross.getMaleParents().get(0).getCross());
		Assert.assertEquals(newMalePedigree2, cross.getMaleParents().get(1).getPedigree());
		Assert.assertEquals(newMaleCross2,cross.getMaleParents().get(1).getCross());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void getExistingCrossesList() {
		final Germplasm germplasm = GermplasmTestDataInitializer.createGermplasm(1);
		Mockito.when(this.germplasmDataManager.getExistingCrosses(1, Collections.singletonList(1), Optional.of(1))).thenReturn(
			Collections.singletonList(germplasm));
		final Map<String, Object> responseMap = this.crossingSettingsController.getExistingCrossesList(1, Collections.singletonList(1), "1");
		Assert.assertEquals(1, responseMap.get(CrossingSettingsController.IS_SUCCESS));

		final List<String> tableHeaderList = (List<String>) responseMap.get(CrossesListUtil.TABLE_HEADER_LIST);
		Assert.assertEquals(ColumnLabels.GID.getName(), tableHeaderList.get(0));
		Assert.assertEquals(ColumnLabels.DESIGNATION.getName(), tableHeaderList.get(1));

		final List<Map<String, Object>> masterList = (List<Map<String, Object>>) responseMap.get(CrossesListUtil.LIST_DATA_TABLE);
		Assert.assertEquals(germplasm.getGid(), masterList.get(0).get(ColumnLabels.GID.getName()));
		Assert.assertEquals(germplasm.getGermplasmPreferredName(), masterList.get(0).get(ColumnLabels.DESIGNATION.getName()));
	}

	private void fillUpUserSelectionWithImportedCrossTestData() {
		Mockito.when(this.studySelection.getImportedCrossesList()).thenReturn(this.createImportedCrossesList(false));
	}

	private ImportedCrossesList createImportedCrossesList(final boolean hasMultipleParents) {
		final ImportedCrossesList list = new ImportedCrossesList();
		final List<ImportedCross> importedCrossList = new ArrayList<>();
		final ImportedCross importedCross = new ImportedCross();
		importedCross.setEntryNumber(CrossingSettingsControllerTest.ENTRY_ID);
		importedCross.setCross(CrossingSettingsControllerTest.TEST_FEMALE_PARENT + "/" + CrossingSettingsControllerTest.TEST_MALE_PARENT1);
		importedCross.setEntryCode(CrossingSettingsControllerTest.TEST_ENTRY_CODE);

		final ImportedGermplasmParent femaleParent = new ImportedGermplasmParent(CrossingSettingsControllerTest.FGID, CrossingSettingsControllerTest.TEST_FEMALE_PARENT, CrossingSettingsControllerTest.FEMALE_PLOT, "");
		femaleParent.setPedigree(CrossingSettingsControllerTest.FEMALE_PEDIGREE);
		importedCross.setFemaleParent(femaleParent);
		femaleParent.setCross(CrossingSettingsControllerTest.FEMALE_CROSS);

		final ImportedGermplasmParent maleParent1 = new ImportedGermplasmParent(CrossingSettingsControllerTest.MGID1, CrossingSettingsControllerTest.TEST_MALE_PARENT1, CrossingSettingsControllerTest.MALE_PLOT1, CrossingSettingsControllerTest.MALE_STUDY_NAME);
		maleParent1.setPedigree(CrossingSettingsControllerTest.MALE_PEDIGREE1);
		maleParent1.setCross(CrossingSettingsControllerTest.MALE_CROSS1);
		final ImportedGermplasmParent maleParent2 = new ImportedGermplasmParent(CrossingSettingsControllerTest.MGID2, CrossingSettingsControllerTest.TEST_MALE_PARENT2, CrossingSettingsControllerTest.MALE_PLOT2, CrossingSettingsControllerTest.MALE_STUDY_NAME);
		maleParent2.setPedigree(CrossingSettingsControllerTest.MALE_PEDIGREE2);
		maleParent2.setCross(CrossingSettingsControllerTest.MALE_CROSS2);
		if (!hasMultipleParents) {
			importedCross.setMaleParents(Lists.newArrayList(maleParent1));
		} else {
			importedCross.setMaleParents(Lists.newArrayList(maleParent1, maleParent2));
		}

		importedCross.setSource(CrossingSettingsControllerTest.TEST_SEED_SOURCE);
		importedCross.setDuplicate(CrossingSettingsControllerTest.TEST_DUPLICATE);
		importedCross.setRawBreedingMethod(CrossingSettingsControllerTest.BREEDING_METHOD);
		importedCross.setCrossingDate(CrossingSettingsControllerTest.CROSSING_DATE);
		importedCross.setNotes(CrossingSettingsControllerTest.NOTES);
		importedCrossList.add(importedCross);
		list.setImportedGermplasms(importedCrossList);
		return list;
	}

}
