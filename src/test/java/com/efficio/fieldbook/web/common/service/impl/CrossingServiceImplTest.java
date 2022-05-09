package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.web.common.exception.InvalidInputException;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.generationcp.commons.parsing.pojo.ImportedCross;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmParent;
import org.generationcp.commons.ruleengine.generator.SeedSourceGenerator;
import org.generationcp.commons.service.GermplasmNamingService;
import org.generationcp.commons.settings.AdditionalDetailsSetting;
import org.generationcp.commons.settings.BreedingMethodSetting;
import org.generationcp.commons.settings.CrossNameSetting;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.InvalidGermplasmNameSettingException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.PedigreeDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.Attribute;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Methods;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.Progenitor;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.germplasm.GermplasmNameSetting;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.study.StudyInstanceService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.generationcp.middleware.util.Util;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CrossingServiceImplTest {

	private static final int PLOT_CODE_FLD_NO = 1552;
	private static final String SUFFIX = "SUFFIX";
	private static final String PREFIX = "PREFIX";
	private static final int BREEDING_METHOD_ID = 1;
	private static final String SAVED_CROSSES_GID1 = "-9999";
	private static final String SAVED_CROSSES_GID2 = "-8888";
	private static final Integer USER_ID = new Random().nextInt(Integer.MAX_VALUE);
	private static final String TEST_BREEDING_METHOD_CODE = "GEN";
	private static final Integer TEST_BREEDING_METHOD_ID = 5;
	private static final Integer TEST_FEMALE_GID_1 = 12345;
	private static final Integer TEST_MALE_GID_1 = 54321;

	private static final Integer TEST_FEMALE_GID_2 = 9999;
	private static final Integer TEST_MALE_GID_2 = 8888;
	private static final Integer TEST_MALE_GID_3 = 93939;

	private static final int HARVEST_LOCATION_ID = 99;
	private static final Integer NEXT_NUMBER = 100;

	private ImportedCrossesList importedCrossesList;

	@Captor
	private ArgumentCaptor<List<Attribute>> attributesListCaptor;

	@Captor
	private ArgumentCaptor<List<Name>> namesCaptor;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private CrossExpansionProperties crossExpansionProperties;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private SeedSourceGenerator seedSourceGenerator;

	@Mock
	private MessageSource messageSource;

	@Mock
	private PedigreeDataManager pedigreeDataManager;

	@Mock
	private GermplasmNamingService germplasmNamingService;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private DatasetService datasetService;

	@Mock
	private GermplasmService germplasmService;

	@Mock
	private StudyInstanceService studyInstanceService;

	@InjectMocks
	private CrossingServiceImpl crossingService;

	private CrossSetting crossSetting;

	private CropType cropType;

	@Before
	public void setUp() throws InvalidGermplasmNameSettingException {
		this.importedCrossesList = this.createImportedCrossesList();
		this.importedCrossesList.setImportedGermplasms(this.createImportedCrosses());

		final Project project = new Project();
		this.cropType = new CropType("maize");
		this.cropType.setUseUUID(false);
		project.setCropType(this.cropType);
		Mockito.doReturn(project).when(this.contextUtil).getProjectInContext();

		Mockito.doReturn(this.createNameTypes()).when(this.germplasmListManager).getGermplasmNameTypes();
		Mockito.doReturn(this.createGermplasmIds()).when(this.germplasmDataManager).addGermplasm(
			ArgumentMatchers.<List<Triple<Germplasm, Name, List<Progenitor>>>>any(), ArgumentMatchers.eq(this.cropType));
		Mockito.doReturn(new Method()).when(this.germplasmDataManager).getMethodByID(CrossingServiceImplTest.BREEDING_METHOD_ID);

		final Term plotCodeTerm = new Term();
		plotCodeTerm.setId(PLOT_CODE_FLD_NO);

		Mockito.doReturn(plotCodeTerm).when(this.germplasmService).getPlotCodeField();

		this.crossSetting = new CrossSetting();
		this.crossSetting.setCrossNameSetting(this.createCrossNameSetting());
		this.crossSetting.setBreedingMethodSetting(this.createBreedingMethodSetting());
		this.crossSetting.setAdditionalDetailsSetting(this.getAdditionalDetailsSetting());

		Mockito.doReturn(CrossingServiceImplTest.NEXT_NUMBER).when(this.germplasmNamingService)
			.getNextSequence(ArgumentMatchers.anyString());
		Mockito.doReturn(this.getExpectedName(CrossingServiceImplTest.NEXT_NUMBER),
			this.getExpectedName(CrossingServiceImplTest.NEXT_NUMBER + 1)).when(this.germplasmNamingService)
			.generateNextNameAndIncrementSequence(ArgumentMatchers.<GermplasmNameSetting>any());
		Mockito.doReturn(this.getExpectedName(CrossingServiceImplTest.NEXT_NUMBER)).when(this.germplasmNamingService)
			.getNextNameInSequence(ArgumentMatchers.<GermplasmNameSetting>any());

		ContextHolder.setLoggedInUserId(USER_ID);
	}

	@Test
	public void testProcessCrossBreedingMethodCodeAlreadyAvailable() {
		final List<ImportedCross> crosses = this.importedCrossesList.getImportedCrosses();

		this.crossSetting.getBreedingMethodSetting().setBasedOnImportFile(true);

		// we modify the data such that one of the entries already have a raw
		// breeding method code (i.e., from import file)
		crosses.get(0).setRawBreedingMethod(CrossingServiceImplTest.TEST_BREEDING_METHOD_CODE);
		final Method method = new Method(CrossingServiceImplTest.TEST_BREEDING_METHOD_ID);
		Mockito.doReturn(method).when(this.germplasmDataManager).getMethodByCode(CrossingServiceImplTest.TEST_BREEDING_METHOD_CODE);

		this.crossingService.processCrossBreedingMethod(this.crossSetting, this.importedCrossesList);

		assertEquals("Raw breeding method codes after processing should resolve to breeding method IDs in the imported cross",
			CrossingServiceImplTest.TEST_BREEDING_METHOD_ID, crosses.get(0).getBreedingMethodId());
	}

	@Test
	public void testProcessCrossBreedingMethodIDAlreadyAvailable() {

		final List<ImportedCross> crosses = this.importedCrossesList.getImportedCrosses();

		this.crossSetting.getBreedingMethodSetting().setBasedOnImportFile(true);
		final Method breedingMethod = new Method();
		breedingMethod.setMid(CrossingServiceImplTest.TEST_BREEDING_METHOD_ID);
		when(this.germplasmDataManager.getMethodByCode(ArgumentMatchers.anyString())).thenReturn(breedingMethod);

		for (final ImportedCross cross : crosses) {
			cross.setRawBreedingMethod(CrossingServiceImplTest.TEST_BREEDING_METHOD_CODE);
		}

		this.crossingService.processCrossBreedingMethod(this.crossSetting, this.importedCrossesList);

		for (final ImportedCross cross : crosses) {
			assertEquals("Breeding method ID should not be overridden if it is already present in the imported cross info",
				CrossingServiceImplTest.TEST_BREEDING_METHOD_ID.intValue(), cross.getBreedingMethodId().intValue());
		}
	}

	@Test
	public void testProcessCrossBreedingMethodUseSetting() {
		this.crossSetting.getBreedingMethodSetting().setMethodId(CrossingServiceImplTest.TEST_BREEDING_METHOD_ID);

		this.crossingService.processCrossBreedingMethod(this.crossSetting, this.importedCrossesList);

		for (final ImportedCross importedCross : this.importedCrossesList.getImportedCrosses()) {
			assertEquals("User provided breeding method must be applied to all objects",
				CrossingServiceImplTest.TEST_BREEDING_METHOD_ID, importedCross.getBreedingMethodId());
		}
	}

	@Test
	public void testProcessCrossBreedingMethodBasedOnParental() {
		this.crossSetting.getBreedingMethodSetting().setMethodId(null);
		final ImportedCross cross = new ImportedCross();
		final ImportedGermplasmParent femaleParent =
			new ImportedGermplasmParent(CrossingServiceImplTest.TEST_FEMALE_GID_1, "FEMALE-12345", "");
		femaleParent.setPlotNo(1);
		cross.setFemaleParent(femaleParent);
		final ImportedGermplasmParent maleParent = new ImportedGermplasmParent(0, "UNKNOWN", "UNKNOWN");
		maleParent.setPlotNo(2);
		cross.setMaleParents(Lists.newArrayList(maleParent));
		cross.setDesig("Cros12345");
		cross.setBreedingMethodId(123);
		this.importedCrossesList.addImportedCrosses(cross);

		this.crossingService.processCrossBreedingMethod(this.crossSetting, this.importedCrossesList);

		for (final ImportedCross importedCross : this.importedCrossesList.getImportedCrosses()) {
			Assert.assertNotNull(
				"A method based on parental lines must be assigned to germplasms if user does not select a breeding method",
				importedCross.getBreedingMethodId());
			assertNotSame(
				"A method based on parental lines must be assigned to germplasms if user does not select a breeding method", 0,
				importedCross.getBreedingMethodId());
			if (importedCross.isPolyCross()) {
				assertEquals(Methods.SELECTED_POLLEN_CROSS.getMethodID(), importedCross.getBreedingMethodId());
			} else if (importedCross.getMaleParents().get(0).getGid().equals(0)) {
				assertEquals(Methods.OPEN_POLLINATION_HALF_SIB.getMethodID(), importedCross.getBreedingMethodId());
			}
		}
	}

	private void setupMockCallsForGermplasm(final Integer gid) {
		final Germplasm germplasm = new Germplasm(gid);
		germplasm.setGnpgs(-1);
	}

	@Test
	public void testApplyCrossSetting() {
		final List<StudyInstance> studyInstances = new ArrayList<>();
		final StudyInstance studyInstance = new StudyInstance();
		studyInstance.setLocationAbbreviation("LABBR");
		studyInstance.setInstanceNumber(1);
		studyInstances.add(studyInstance);

		Mockito.doReturn(studyInstances).when(this.studyInstanceService).getStudyInstances(Mockito.anyInt());

		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook();
		workbook.getStudyDetails().setId(new Random().nextInt());
		this.crossingService.processCrossBreedingMethod(this.crossSetting, this.importedCrossesList);
		this.crossingService.applyCrossSetting(this.crossSetting, this.importedCrossesList, workbook);

		final ImportedCross cross1 = this.importedCrossesList.getImportedCrosses().get(0);

		assertEquals(CrossingServiceImplTest.SAVED_CROSSES_GID1, cross1.getGid());
		assertEquals(this.getExpectedName(CrossingServiceImplTest.NEXT_NUMBER), cross1.getDesig());
		assertEquals((Integer) 1, cross1.getEntryNumber());
		assertEquals("1", cross1.getEntryCode());
		assertNull(cross1.getNames().get(0).getGermplasm().getGid());
		assertEquals((Integer) 0, cross1.getNames().get(0).getLocationId());
		assertEquals(CrossingServiceImplTest.USER_ID, cross1.getNames().get(0).getCreatedBy());

		final ImportedCross cross2 = this.importedCrossesList.getImportedCrosses().get(1);

		assertEquals(CrossingServiceImplTest.SAVED_CROSSES_GID2, cross2.getGid());
		assertEquals(this.getExpectedName(NEXT_NUMBER + 1), cross2.getDesig());
		assertEquals((Integer) 2, cross2.getEntryNumber());
		assertEquals("2", cross2.getEntryCode());
		assertNull(cross2.getNames().get(0).getGermplasm().getGid());
		assertEquals((Integer) 0, cross2.getNames().get(0).getLocationId());
		assertEquals(CrossingServiceImplTest.USER_ID, cross2.getNames().get(0).getCreatedBy());

	}

	@Test
	public void testApplyCrossNameSettingToImportedCrosses() {

		final CrossNameSetting setting = this.crossSetting.getCrossNameSetting();

		this.crossingService.applyCrossNameSettingToImportedCrosses(setting, this.importedCrossesList.getImportedCrosses());

		final ImportedCross cross1 = this.importedCrossesList.getImportedCrosses().get(0);

		assertNull(cross1.getGid());
		assertEquals(this.getExpectedName(NEXT_NUMBER), cross1.getDesig());
		assertEquals(cross1.getFemaleDesignation() + setting.getSeparator() + cross1.getMaleDesignationsAsString(), cross1.getCross());
		assertEquals((Integer) 1, cross1.getEntryNumber());
		assertEquals("1", cross1.getEntryCode());

		final ImportedCross cross2 = this.importedCrossesList.getImportedCrosses().get(1);

		assertNull(cross2.getGid());
		assertEquals(this.getExpectedName(NEXT_NUMBER + 1), cross2.getDesig());
		assertEquals(cross2.getFemaleDesignation() + setting.getSeparator() + cross2.getMaleDesignationsAsString(), cross2.getCross());
		assertEquals((Integer) 2, cross2.getEntryNumber());
		assertEquals("2", cross2.getEntryCode());
	}

	@Test
	public void testApplyCrossSetting_WhenSavingOfParentageDesignationNameIsSetToTrue() {
		final List<Integer> savedGermplasmIds = new ArrayList<Integer>();
		savedGermplasmIds.add(1);
		savedGermplasmIds.add(2);

		final CrossNameSetting crossNameSetting = this.createCrossNameSetting();
		crossNameSetting.setSaveParentageDesignationAsAString(true);

		this.crossSetting.setCrossNameSetting(crossNameSetting);
		this.crossingService.processCrossBreedingMethod(this.crossSetting, this.importedCrossesList);
		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook();
		workbook.getStudyDetails().setId(new Random().nextInt());
		this.crossingService
			.applyCrossSetting(this.crossSetting, this.importedCrossesList, workbook);

		// TODO prepare descriptive messages for verification failure once
		// Mockito has stable 2.0 version
		Mockito.verify(this.germplasmDataManager, Mockito.atLeastOnce()).addGermplasmName(ArgumentMatchers.<Name>anyList());

	}

	@Test
	public void testApplyCrossSetting_WhenSavingOfParentageDesignationNameIsSetToFalse() {
		final List<Integer> savedGermplasmIds = new ArrayList<Integer>();
		savedGermplasmIds.add(1);
		savedGermplasmIds.add(2);

		final CrossNameSetting crossNameSetting = this.createCrossNameSetting();
		crossNameSetting.setSaveParentageDesignationAsAString(false);

		this.crossSetting.setCrossNameSetting(crossNameSetting);
		this.crossingService.processCrossBreedingMethod(this.crossSetting, this.importedCrossesList);
		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook();
		workbook.getStudyDetails().setId(new Random().nextInt());
		this.crossingService
			.applyCrossSetting(this.crossSetting, this.importedCrossesList, workbook);

		Mockito.verify(this.germplasmDataManager, Mockito.never()).addGermplasmName(ArgumentMatchers.<List<Name>>any());

	}

	@Test
	public void testBuildCrossName() {

		final CrossNameSetting setting = this.createCrossNameSetting();
		final ImportedCross cross = this.createCross();
		final String crossName = this.crossingService.buildCrossName(cross, setting.getSeparator());

		assertEquals(cross.getFemaleDesignation() + setting.getSeparator() + cross.getMaleDesignationsAsString(), crossName);

	}

	@Test
	public void testFormatHarvestDate() {
		assertTrue(new Integer(20150500).equals(this.crossingService.getFormattedHarvestDate("2015-05")));
	}

	@Test
	public void testPopulateGermplasmdateWithHarvestDate() {
		final Germplasm germplasm = new Germplasm();
		String harvestedDate = "2015-06";
		this.crossingService.populateGermplasmDate(germplasm, harvestedDate);
		harvestedDate = harvestedDate.replace("-", "");
		harvestedDate += "00";
		assertEquals(germplasm.getGdate(), new Integer(harvestedDate));
	}

	@Test
	public void testPopulateGermplasmdateWithCurrentDate() {
		final Germplasm germplasm = new Germplasm();
		this.crossingService.populateGermplasmDate(germplasm, "");
		assertEquals(germplasm.getGdate(), DateUtil.getCurrentDateAsIntegerValue());
	}

	@Test
	public void testGenerateGermplasmNameTriples() {

		final CrossSetting crossSetting = new CrossSetting();
		final CrossNameSetting crossNameSetting = this.createCrossNameSetting();
		final BreedingMethodSetting breedingMethodSetting = new BreedingMethodSetting();
		final AdditionalDetailsSetting additionalDetailsSetting = this.createAdditionalDetailsSetting();
		crossSetting.setCrossNameSetting(crossNameSetting);
		crossSetting.setBreedingMethodSetting(breedingMethodSetting);
		crossSetting.setAdditionalDetailsSetting(additionalDetailsSetting);

		final CrossingServiceImpl.GermplasmListResult result = this.crossingService
			.generateGermplasmNameTriples(crossSetting, this.importedCrossesList.getImportedCrosses(),
				false);

		Triple<Germplasm, Name, List<Progenitor>> germplasmTriple = result.getGermplasmTriples().get(0);
		final Germplasm germplasm1 = germplasmTriple.getLeft();
		final Name name1 = germplasmTriple.getMiddle();
		final List<Progenitor> progenitorList1 = germplasmTriple.getRight();
		final ImportedCross cross1 = this.importedCrossesList.getImportedCrosses().get(0);


		assertTrue(result.getIsTrimed());
		Assert.assertNull(germplasm1.getGid());
		assertEquals(20150101, germplasm1.getGdate().intValue());
		assertEquals(2, germplasm1.getGnpgs().intValue());
		assertEquals(cross1.getFemaleGid(), germplasm1.getGpid1().toString());
		assertEquals(cross1.getMaleGids().get(0), germplasm1.getGpid2());
		assertEquals(0, germplasm1.getGrplce().intValue());
		assertEquals(0, germplasm1.getLgid().intValue());
		assertEquals(0, germplasm1.getGrplce().intValue());
		assertEquals(0, germplasm1.getLgid().intValue());
		assertEquals(99, germplasm1.getLocationId().intValue());
		assertEquals(0, germplasm1.getMgid().intValue());
		Assert.assertNull(germplasm1.getPreferredAbbreviation());
		Assert.assertNull(germplasm1.getPreferredName());
		assertEquals(0, germplasm1.getReferenceId().intValue());
		assertEquals(CrossingServiceImplTest.USER_ID, germplasm1.getCreatedBy());
		assertSame(germplasm1, progenitorList1.get(0).getGermplasm());
		assertEquals(TEST_MALE_GID_3, progenitorList1.get(0).getProgenitorGid());

		Assert.assertNull(name1.getGermplasm().getGid());
		assertEquals(99, name1.getLocationId().intValue());
		assertEquals(20150101, name1.getNdate().intValue());
		Assert.assertNull(name1.getNid());
		Assert.assertNull(name1.getNstat());
		assertFalse(name1.getNval().contains("(truncated)"));
		assertEquals(0, name1.getReferenceId().intValue());
		Assert.assertNull(name1.getTypeId());
		assertEquals(CrossingServiceImplTest.USER_ID, name1.getCreatedBy());

		germplasmTriple = result.getGermplasmTriples().get(1);
		final Germplasm germplasm2 = germplasmTriple.getLeft();
		final Name name2 = germplasmTriple.getMiddle();
		final List<Progenitor> progenitorList2 = germplasmTriple.getRight();
		final ImportedCross cross2 = this.importedCrossesList.getImportedCrosses().get(1);

		Assert.assertNull(null, germplasm2.getGid());
		assertEquals(20150101, germplasm2.getGdate().intValue());
		assertEquals(2, germplasm2.getGnpgs().intValue());
		assertEquals(cross2.getFemaleGid(), germplasm2.getGpid1().toString());
		assertEquals(cross2.getMaleGids().get(0), germplasm2.getGpid2());
		assertEquals(0, germplasm2.getGrplce().intValue());
		assertEquals(0, germplasm2.getLgid().intValue());
		assertEquals(0, germplasm2.getGrplce().intValue());
		assertEquals(0, germplasm2.getLgid().intValue());
		assertEquals(99, germplasm2.getLocationId().intValue());
		assertEquals(0, germplasm2.getMgid().intValue());
		Assert.assertNull(null, germplasm2.getPreferredAbbreviation());
		Assert.assertNull(null, germplasm2.getPreferredName());
		assertEquals(0, germplasm2.getReferenceId().intValue());
		assertEquals(CrossingServiceImplTest.USER_ID, germplasm2.getCreatedBy());
		assertTrue(progenitorList2.isEmpty());

		Assert.assertNull(name2.getGermplasm().getGid());
		assertEquals(99, name2.getLocationId().intValue());
		assertEquals(20150101, name2.getNdate().intValue());
		Assert.assertNull(name2.getNid());
		Assert.assertNull(name2.getNstat());
		assertTrue(name2.getNval().contains("(truncated)"));
		assertEquals(0, name2.getReferenceId().intValue());
		Assert.assertNull(name2.getTypeId());
		assertEquals(CrossingServiceImplTest.USER_ID, name2.getCreatedBy());
	}

	@Test
	public void testCreateGermplasm_CrossAlreadyExistsInDatabase() {

		final int crossGid = 1;
		final int userId = 1;
		final int harvestLocationId = 2;
		final int breedingMethodId = 3;
		final String harvestDate = "2019-01-01";

		// If cross gid is not null, it means the cross is created from Design Crosses functionality
		final ImportedCross cross = new ImportedCross();
		cross.setGid(String.valueOf(crossGid));

		cross.setBreedingMethodId(breedingMethodId);
		final Germplasm existingGermplasm = new Germplasm();
		existingGermplasm.setGid(crossGid);

		when(this.germplasmDataManager.getGermplasmByGID(crossGid)).thenReturn(existingGermplasm);

		final Germplasm result = this.crossingService.createGermplasm(cross, harvestLocationId, harvestDate);

		assertSame(existingGermplasm, result);
		assertEquals(breedingMethodId, result.getMethodId().intValue());
		assertEquals(20190101, result.getGdate().intValue());
		assertEquals(harvestLocationId, result.getLocationId().intValue());
		assertNotEquals(TEST_FEMALE_GID_1, result.getGpid1());
		assertNotEquals(TEST_MALE_GID_1, result.getGpid2());

	}

	@Test
	public void testCreateGermplasm_ImportedCrossNotYetSavedInDatabase() {

		final int harvestLocationId = 2;
		final String harvestDate = "2019-01-01";

		final ImportedCross cross = this.createCross();
		// If cross gid is null, it means the cross is created from Imported Crosses functionality
		cross.setGid(null);

		final Germplasm result = this.crossingService.createGermplasm(cross, harvestLocationId, harvestDate);

		verify(this.germplasmDataManager, times(0)).getGermplasmByGID(anyInt());

		assertEquals(cross.getBreedingMethodId(), result.getMethodId());
		assertEquals(20190101, result.getGdate().intValue());
		assertEquals(harvestLocationId, result.getLocationId().intValue());
		assertEquals(USER_ID, result.getCreatedBy());
		assertEquals(TEST_FEMALE_GID_1, result.getGpid1());
		assertEquals(TEST_MALE_GID_1, result.getGpid2());

		assertEquals(CrossingServiceImpl.GERMPLASM_GNPGS, result.getGnpgs());
		assertEquals(CrossingServiceImpl.GERMPLASM_GRPLCE, result.getGrplce());
		assertEquals(CrossingServiceImpl.GERMPLASM_LGID, result.getLgid());
		assertEquals(CrossingServiceImpl.GERMPLASM_MGID, result.getMgid());
		assertEquals(CrossingServiceImpl.GERMPLASM_REFID, result.getReferenceId());

	}

	@Test
	public void testCreateName_CrossAlreadyExistsInDatabase() {

		final int crossGid = 1;
		final int harvestLocationId = 2;
		final Germplasm germplasm = new Germplasm();
		final Name existingPreferredName = new Name();
		existingPreferredName.setNstat(1);
		germplasm.setNames(Arrays.asList(existingPreferredName));
		germplasm.setGid(crossGid);
		germplasm.setGdate(20190101);

		final ImportedCross cross = this.createCross();
		cross.setGid(String.valueOf(crossGid));

		final Name result = this.crossingService.createName(germplasm, cross, harvestLocationId);

		assertSame(existingPreferredName, result);
		assertEquals("Cros12345", result.getNval());
		assertEquals(USER_ID, result.getCreatedBy());
		assertEquals(20190101, result.getNdate().intValue());
		assertEquals(harvestLocationId, result.getLocationId().intValue());

	}

	@Test
	public void testCreateName_ImportedCrossNotYetSavedInDatabase() {

		final int harvestLocationId = 2;
		final Germplasm germplasm = new Germplasm();
		final Name existingPreferredName = new Name();
		existingPreferredName.setNstat(1);
		germplasm.setNames(Collections.singletonList(existingPreferredName));
		germplasm.setGid(null);
		germplasm.setGdate(20190101);

		final ImportedCross cross = this.createCross();
		cross.setGid(null);

		final Name result = this.crossingService.createName(germplasm, cross, harvestLocationId);

		assertNotSame(existingPreferredName, result);
		assertEquals("Cros12345", result.getNval());
		assertEquals(USER_ID, result.getCreatedBy());
		assertEquals(20190101, result.getNdate().intValue());
		assertEquals(harvestLocationId, result.getLocationId().intValue());
		assertEquals(CrossingServiceImpl.NAME_REFID, result.getReferenceId());

	}

	@Test
	public void testCreateProgenitors_SingleCross() {

		final Germplasm germplasm = new Germplasm();
		final ImportedCross cross = new ImportedCross();
		final ImportedGermplasmParent maleParent1 = new ImportedGermplasmParent(111, "", "");
		cross.setMaleParents(Lists.newArrayList(maleParent1));
		final List<Progenitor> result = this.crossingService.createProgenitors(cross, germplasm);
		assertTrue(result.isEmpty());

	}

	@Test
	public void testCreateProgenitors_CrossAlreadyExistsInDatabase() {

		final int crossGid = 1;
		final Germplasm germplasm = new Germplasm();
		final ImportedCross cross = new ImportedCross();
		cross.setGid(String.valueOf(crossGid));
		final Progenitor maleParent1 = new Progenitor(germplasm, 3, 1);
		final Progenitor maleParent2 = new Progenitor(germplasm, 4, 2);
		final Progenitor maleParent3 = new Progenitor(germplasm, 5, 3);

		final List<Progenitor> existingProgenitors = Arrays.asList(maleParent1, maleParent2, maleParent3);
		when(this.pedigreeDataManager.getProgenitorsByGID(crossGid)).thenReturn(existingProgenitors);

		final List<Progenitor> result = this.crossingService.createProgenitors(cross, germplasm);

		assertSame(existingProgenitors, result);

	}

	@Test
	public void testCreateProgenitors_PolyCross() {

		final Germplasm germplasm = new Germplasm();
		final ImportedCross cross = new ImportedCross();
		final ImportedGermplasmParent maleParent1 = new ImportedGermplasmParent(111, "", "");
		final ImportedGermplasmParent maleParent2 = new ImportedGermplasmParent(222, "", "");
		final ImportedGermplasmParent maleParent3 = new ImportedGermplasmParent(333, "", "");
		cross.setMaleParents(Lists.newArrayList(maleParent1, maleParent2, maleParent3));
		final List<Progenitor> result = this.crossingService.createProgenitors(cross, germplasm);

		assertFalse(result.isEmpty());
		//Size should be 2, only second and third male parents should have progenitor records"
		assertEquals(2, result.size());
		assertSame(germplasm, result.get(0).getGermplasm());
		assertEquals(3, result.get(0).getProgenitorNumber().intValue());
		assertEquals(222, result.get(0).getProgenitorGid().intValue());
		assertSame(germplasm, result.get(1).getGermplasm());
		assertEquals(4, result.get(1).getProgenitorNumber().intValue());
		assertEquals(333, result.get(1).getProgenitorGid().intValue());

	}

	@Test
	public void testGetNextNumberInSequenceDefault() {
		final String prefix = "A";

		final int nextNumber = this.crossingService.getNextNumberInSequence(prefix);
		assertEquals(CrossingServiceImplTest.NEXT_NUMBER.intValue(), nextNumber);
		final ArgumentCaptor<String> prefixCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(this.germplasmNamingService).getNextSequence(prefixCaptor.capture());
		assertEquals(prefix, prefixCaptor.getValue());
	}

	@Test
	public void testGenerateSeedSource() {
		final String newSeedSource = "newSeedSource";
		Mockito.doReturn(newSeedSource).when(this.seedSourceGenerator)
			.generateSeedSourceForCross(ArgumentMatchers.any(Pair.class), ArgumentMatchers.any(Pair.class),
				ArgumentMatchers.any(Pair.class), ArgumentMatchers.any(Pair.class), ArgumentMatchers.any(Pair.class),
				ArgumentMatchers.any(ImportedCross.class));

		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook();
		workbook.getStudyDetails().setId(new Random().nextInt());
		// Case 1 - No seed source present. Generate new.
		final ImportedCross importedCross1 = this.createCross();
		final String studyName = workbook.getStudyName();
		importedCross1.setMaleStudyname(studyName);
		importedCross1.setSource(null);
		final CrossingServiceImpl.CrossSourceStudy studydata = this.crossingService.new CrossSourceStudy(workbook, null, null, null);
		this.crossingService.populateSeedSource(importedCross1, studydata, Collections.emptyMap());

		assertEquals(newSeedSource, importedCross1.getSource());

		// Case 2 - Seed source is present. Keep.
		final ImportedCross importedCross2 = this.createCross();
		final String existingSeedSource = "existingSeedSource";
		importedCross2.setSource(existingSeedSource);
		importedCross2.setMaleStudyname(studyName);
		this.crossingService
			.populateSeedSource(importedCross2, this.crossingService.new CrossSourceStudy(workbook, null, null, null), new HashMap<>());
		assertEquals(existingSeedSource, importedCross2.getSource());

		// Case 3 - Seed source is presend but is PENDING indicator. Generate
		// new.
		final ImportedCross importedCross3 = this.createCross();
		importedCross3.setSource(ImportedCross.SEED_SOURCE_PENDING);
		importedCross3.setMaleStudyname(studyName);
		this.crossingService
			.populateSeedSource(importedCross3, this.crossingService.new CrossSourceStudy(workbook, null, null, null), new HashMap<>());
		assertEquals(newSeedSource, importedCross3.getSource());

		// Case 4 - Seed source is present but empty string. Generate new.
		final ImportedCross importedCross4 = this.createCross();
		importedCross4.setSource("");
		importedCross4.setMaleStudyname(studyName);
		this.crossingService
			.populateSeedSource(importedCross4, this.crossingService.new CrossSourceStudy(workbook, null, null, null), new HashMap<>());
		assertEquals(newSeedSource, importedCross4.getSource());

	}

	@Test
	public void testGenerateSeedSource_GeneratedSourceIsOverMaximumLength() {

		final String newSeedSource = RandomStringUtils.randomAlphabetic(300);
		Mockito.doReturn(newSeedSource).when(this.seedSourceGenerator)
			.generateSeedSourceForCross(ArgumentMatchers.any(Pair.class), ArgumentMatchers.any(Pair.class),
				ArgumentMatchers.any(Pair.class), ArgumentMatchers.any(Pair.class), ArgumentMatchers.any(Pair.class),
				ArgumentMatchers.any(ImportedCross.class));


		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook();
		workbook.getStudyDetails().setId(new Random().nextInt());
		// Case 1 - No seed source present. Generate new.
		final ImportedCross importedCross1 = this.createCross();
		importedCross1.setSource(null);
		final String studyName = workbook.getStudyName();
		importedCross1.setMaleStudyname(studyName);
		final CrossingServiceImpl.CrossSourceStudy studydata = this.crossingService.new CrossSourceStudy(workbook, null, null, null);
		this.crossingService.populateSeedSource(importedCross1, studydata, Collections.singletonMap(studyName, studydata));
		assertEquals(CrossingServiceImpl.MAX_SEED_SOURCE_SIZE, importedCross1.getSource().length());
	}

	@Test
	public void testApplyCrossSettingWithNamingRules() {

		final CrossSetting crossSetting = this.createCrossSetting();
		final ImportedCrossesList importedCrossesList = this.createImportedCrossesList();
		final Integer userId = 123456;
		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook();
		workbook.getStudyDetails().setId(new Random().nextInt());

		this.importedCrossesList.addImportedCrosses(this.createCross());
		this.importedCrossesList.addImportedCrosses(this.createSecondCross());

		this.crossingService.applyCrossSettingWithNamingRules(crossSetting, importedCrossesList, userId, workbook);

		int counter = 1;
		for (final ImportedCross importedCross : importedCrossesList.getImportedCrosses()) {
			assertEquals(importedCross.getEntryCode(), importedCross.getEntryNumber());
			assertTrue(importedCross.getEntryCode().equals(counter));
			assertTrue(importedCross.getEntryNumber().equals(counter));
			counter++;
		}
	}

	@Test
	public void testGetNextNameInSequence() throws InvalidInputException {
		final String nextNameInSequence = this.crossingService.getNextNameInSequence(this.crossSetting.getCrossNameSetting());
		assertEquals(this.getExpectedName(NEXT_NUMBER), nextNameInSequence);
	}

	@Test
	public void testGetNextNameInSequenceWhenSpecifiedSequenceStartingNumberIsLower() throws InvalidGermplasmNameSettingException {
		this.crossSetting.getCrossNameSetting().setPrefix("ABC");
		this.crossSetting.getCrossNameSetting().setStartNumber(1);

		final String errorMessage = "The starting sequence number specified will generate conflict with already existing cross codes.";
		when(this.messageSource.getMessage(ArgumentMatchers.isA(String.class), ArgumentMatchers.any(Object[].class), ArgumentMatchers.isA(Locale.class)))
			.thenReturn(errorMessage);

		Mockito.doThrow(new InvalidGermplasmNameSettingException("")).when(this.germplasmNamingService).getNextNameInSequence(ArgumentMatchers.<GermplasmNameSetting>any());
		try {
			this.crossingService.getNextNameInSequence(this.crossSetting.getCrossNameSetting());
			Assert.fail("Should have thrown InvalidInputException but did not.");
		} catch (final InvalidInputException e) {
			assertEquals(errorMessage, e.getMessage());
		}
	}

	@Test
	public void testSaveAttributes() {
		final List<Integer> germplasmIds = Arrays.asList(101, 102);
		this.crossingService.saveAttributes(this.crossSetting, this.importedCrossesList, germplasmIds);
		Mockito.verify(this.germplasmDataManager).addAttributes(this.attributesListCaptor.capture());

		final List<Attribute> attributesList = this.attributesListCaptor.getValue();
		assertEquals(germplasmIds.size(), attributesList.size());
		final Iterator<Integer> idsIterator = germplasmIds.iterator();
		final Iterator<ImportedCross> crossesIterator = this.importedCrossesList.getImportedCrosses().iterator();
		for (final Attribute attribute : attributesList) {
			final Integer gid = idsIterator.next();
			final ImportedCross cross = crossesIterator.next();
			this.verifyPlotCodeAttributeValues(attribute, gid, cross);
		}
	}

	@Test
	public void testSaveAttributesWhenMergingPlotDuplicates() {
		final ImportedCross secondCross = this.importedCrossesList.getImportedCrosses().get(1);
		// Set 2nd cross as plot duplicate of first cross
		secondCross.setDuplicateEntries(new HashSet<>(Collections.singletonList(1)));
		secondCross.setDuplicatePrefix(ImportedCross.PLOT_DUPE_PREFIX);
		this.crossSetting.setPreservePlotDuplicates(false);

		final List<Integer> germplasmIds = Arrays.asList(101, 102);
		this.crossingService.saveAttributes(this.crossSetting, this.importedCrossesList, germplasmIds);
		Mockito.verify(this.germplasmDataManager).addAttributes(this.attributesListCaptor.capture());
		final List<Attribute> attributesList = this.attributesListCaptor.getValue();
		assertEquals("Attribute will be saved only for first entry", 1, attributesList.size());
		final ImportedCross firstCross = this.importedCrossesList.getImportedCrosses().get(0);
		this.verifyPlotCodeAttributeValues(attributesList.get(0), germplasmIds.get(0),
			firstCross);
		// Verify that gid, cross and designation from 1st cross was copied to 2nd cross
		assertEquals(firstCross.getGid(), secondCross.getGid());
		assertEquals(firstCross.getCross(), secondCross.getCross());
		assertEquals(firstCross.getDesig(), secondCross.getDesig());
	}

	@Test
	public void testSaveAttributesWhenPreservingPlotDuplicates() {
		final ImportedCross secondCross = this.importedCrossesList.getImportedCrosses().get(1);
		// Set 2nd cross as plot duplicate of first cross
		secondCross.setDuplicateEntries(new HashSet<>(Collections.singletonList(1)));
		secondCross.setDuplicatePrefix(ImportedCross.PLOT_DUPE_PREFIX);
		this.crossSetting.setPreservePlotDuplicates(true);

		final List<Integer> germplasmIds = Arrays.asList(101, 102);
		this.crossingService.saveAttributes(this.crossSetting, this.importedCrossesList, germplasmIds);
		Mockito.verify(this.germplasmDataManager).addAttributes(this.attributesListCaptor.capture());
		final List<Attribute> attributesList = this.attributesListCaptor.getValue();
		assertEquals("Expecting plot duplicate crosses to be preserved", germplasmIds.size(), attributesList.size());
		final Iterator<Integer> idsIterator = germplasmIds.iterator();
		final Iterator<ImportedCross> crossesIterator = this.importedCrossesList.getImportedCrosses().iterator();
		for (final Attribute attribute : attributesList) {
			final Integer gid = idsIterator.next();
			final ImportedCross cross = crossesIterator.next();
			this.verifyPlotCodeAttributeValues(attribute, gid, cross);
		}
	}

	@Test
	public void testSavePedigreeDesignationName() {
		final List<Integer> gids = new ArrayList<>();
		for (int i=0; i<this.importedCrossesList.getImportedCrosses().size(); i++) {
			gids.add(new Random().nextInt());
		}
		this.crossSetting.setAdditionalDetailsSetting(this.createAdditionalDetailsSetting());

		this.crossingService.savePedigreeDesignationName(this.importedCrossesList, gids, this.crossSetting);
		final Iterator<Integer> gidsIterator = gids.iterator();
		Mockito.verify(this.germplasmDataManager).addGermplasmName(this.namesCaptor.capture());
		final Iterator<Name> namesIterator = this.namesCaptor.getValue().iterator();
		for (final ImportedCross cross : this.importedCrossesList.getImportedCrosses()) {
			final Integer gid = gidsIterator.next();
			final Name name = namesIterator.next();
			assertNotNull(name.getGermplasm());
			assertEquals(gid, name.getGermplasm().getGid());
			assertEquals(USER_ID, name.getCreatedBy());
			assertEquals(CrossingServiceImpl.PEDIGREE_NAME_TYPE, name.getTypeId());
			assertEquals(cross.getFemaleDesignation() + CrossingServiceImpl.DEFAULT_SEPARATOR + cross.getMaleDesignationsAsString(), name.getNval());
			assertEquals(CrossingServiceImpl.PREFERRED_NAME, name.getNstat());
			assertEquals(CrossingServiceImplTest.HARVEST_LOCATION_ID, name.getLocationId().intValue());
			assertEquals(Util.getCurrentDateAsIntegerValue(), name.getNdate());
			assertEquals(0, name.getReferenceId().intValue());
		}
	}

	private void verifyPlotCodeAttributeValues(final Attribute attribute, final Integer gid, final ImportedCross cross) {
		assertEquals(Integer.valueOf(DateUtil.getCurrentDateAsStringValue()), attribute.getAdate());
		assertEquals(gid, attribute.getGermplasmId());
		assertEquals(gid.toString(), cross.getGid());
		assertEquals(cross.getSource(), attribute.getAval());
		assertEquals(PLOT_CODE_FLD_NO, attribute.getTypeId().intValue());
		assertEquals(USER_ID, attribute.getCreatedBy());
	}

	private ImportedCrossesList createImportedCrossesList() {

		final ImportedCrossesList importedCrossesList = new ImportedCrossesList();
		final List<ImportedCross> importedCrosses = new ArrayList<>();
		importedCrossesList.setImportedGermplasms(importedCrosses);
		return importedCrossesList;

	}

	private List<ImportedCross> createImportedCrosses() {

		final List<ImportedCross> importedCrosses = new ArrayList<>();
		final ImportedCross cross = new ImportedCross();
		final ImportedGermplasmParent femaleParent =
			new ImportedGermplasmParent(CrossingServiceImplTest.TEST_FEMALE_GID_1, "FEMALE-12345", "");
		cross.setFemaleParent(femaleParent);
		final ImportedGermplasmParent maleParent1 = new ImportedGermplasmParent(CrossingServiceImplTest.TEST_MALE_GID_1, "MALE-54321", "");
		final ImportedGermplasmParent maleParent2 = new ImportedGermplasmParent(CrossingServiceImplTest.TEST_MALE_GID_3, "", "");
		cross.setMaleParents(Lists.newArrayList(maleParent1, maleParent2));
		cross.setCross("CROSS 1");
		cross.setSource("MALE:1:FEMALE:1");
		cross.setDesig(
			"G9BC0RL34-1P-5P-2-1P-3P-B/G9BC1TSR8P-1P-1P-5P-3P-1P-1P)-3-1-1-1-B*8/((CML150xCLG2501)-B-31-1-B-1-BBB/CML193-BB)-B-1-BB(NonQ)-B*8)-B/((G9BC0RL34-1P-5P-2-1P-3P-B/G9BC1TSR8P-1P-1P-5P-3P-1P-1P)-3-1-1-1-B*8/((CML161xCML451)-B-18-1-BBB/CML1612345");
		cross.setEntryNumber(1);
		importedCrosses.add(cross);
		final ImportedCross cross2 = this.createSecondCross();
		importedCrosses.add(cross2);

		return importedCrosses;

	}

	private ImportedCross createSecondCross() {
		final ImportedCross cross2 = new ImportedCross();
		final ImportedGermplasmParent femaleParent =
			new ImportedGermplasmParent(CrossingServiceImplTest.TEST_FEMALE_GID_2, "FEMALE-9999", "");
		cross2.setFemaleParent(femaleParent);
		final ImportedGermplasmParent maleParent = new ImportedGermplasmParent(CrossingServiceImplTest.TEST_MALE_GID_2, "MALE-8888", "");
		cross2.setMaleParents(Lists.newArrayList(maleParent));
		cross2.setCross("CROSS 2");
		cross2.setSource("MALE:2:FEMALE:2");
		cross2.setDesig(RandomStringUtils.randomAlphabetic(5500));
		cross2.setEntryNumber(2);
		return cross2;
	}

	private ImportedCross createCross() {
		final ImportedCross cross = new ImportedCross();
		final ImportedGermplasmParent femaleParent =
			new ImportedGermplasmParent(CrossingServiceImplTest.TEST_FEMALE_GID_1, "FEMALE-12345", "");
		femaleParent.setPlotNo(1);
		cross.setFemaleParent(femaleParent);
		final ImportedGermplasmParent maleParent = new ImportedGermplasmParent(CrossingServiceImplTest.TEST_MALE_GID_1, "MALE-54321", "");
		maleParent.setPlotNo(2);
		cross.setMaleParents(Lists.newArrayList(maleParent));
		cross.setDesig("Cros12345");
		cross.setBreedingMethodId(123);
		return cross;
	}

	private CrossSetting createCrossSetting() {
		return new CrossSetting(null, null, this.createCrossNameSetting(), this.createAdditionalDetailsSetting());
	}

	private CrossNameSetting createCrossNameSetting() {
		final CrossNameSetting setting = new CrossNameSetting();

		setting.setPrefix(CrossingServiceImplTest.PREFIX);
		setting.setSuffix(CrossingServiceImplTest.SUFFIX);
		setting.setAddSpaceBetweenPrefixAndCode(true);
		setting.setAddSpaceBetweenSuffixAndCode(true);
		setting.setSeparator("|");
		setting.setStartNumber(100);
		setting.setNumOfDigits(7);

		return setting;
	}

	private AdditionalDetailsSetting getAdditionalDetailsSetting() {
		return new AdditionalDetailsSetting();
	}

	private BreedingMethodSetting createBreedingMethodSetting() {
		final BreedingMethodSetting breedingMethodSetting = new BreedingMethodSetting();
		breedingMethodSetting.setMethodId(CrossingServiceImplTest.BREEDING_METHOD_ID);
		breedingMethodSetting.setBasedOnImportFile(false);
		breedingMethodSetting.setBasedOnStatusOfParentalLines(false);
		return breedingMethodSetting;
	}

	private AdditionalDetailsSetting createAdditionalDetailsSetting() {
		final AdditionalDetailsSetting setting = new AdditionalDetailsSetting();

		setting.setHarvestDate("20150101");
		setting.setHarvestLocationId(HARVEST_LOCATION_ID);

		return setting;
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
		ids.add(Integer.valueOf(CrossingServiceImplTest.SAVED_CROSSES_GID1));
		ids.add(Integer.valueOf(CrossingServiceImplTest.SAVED_CROSSES_GID2));
		return ids;
	}

	private String getExpectedName(final Integer number) {
		return PREFIX + " 0000" + number + " " + SUFFIX;
	}


}
