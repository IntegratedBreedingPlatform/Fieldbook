
package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.ruleengine.ProcessCodeOrderedRule;
import org.generationcp.commons.ruleengine.ProcessCodeRuleFactory;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.ruleengine.RuleExecutionContext;
import org.generationcp.commons.service.impl.SeedSourceGenerator;
import org.generationcp.commons.settings.AdditionalDetailsSetting;
import org.generationcp.commons.settings.BreedingMethodSetting;
import org.generationcp.commons.settings.CrossNameSetting;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CrossingServiceImplTest {

	private static final int BREEDING_METHOD_ID = 1;
	private static final String SAVED_CROSSES_GID1 = "-9999";
	private static final String SAVED_CROSSES_GID2 = "-8888";
	private static final Integer USER_ID = 123;
	public static final String TEST_BREEDING_METHOD_CODE = "GEN";
	public static final Integer TEST_BREEDING_METHOD_ID = 5;
	public static final String TEST_FEMALE_GID_1 = "12345";
	public static final String TEST_MALE_GID_1 = "54321";
	public static final String TEST_FEMALE_GID_2 = "9999";
	public static final String TEST_MALE_GID_2 = "8888";
	public static final String TEST_PROCESS_CODE = "[BC]";
	public static final String TEST_PROCESS_CODE_WITH_PREFIX = "B[RCRPRNT]";

	private ImportedCrossesList importedCrossesList;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private CrossExpansionProperties crossExpansionProperties;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private SeedSourceGenerator seedSourceGenertor;

	@Mock
	private ProcessCodeRuleFactory processCodeRuleFactory;

	@Mock
	private ProcessCodeOrderedRule processCodeOrderedRule;

	@InjectMocks
	private CrossingServiceImpl crossingService;

	private CrossSetting crossSetting;

	@Before
	public void setUp() throws MiddlewareQueryException {

		this.importedCrossesList = this.createImportedCrossesList();
		this.importedCrossesList.setImportedGermplasms(this.createImportedCrosses());

		Mockito.when(this.processCodeRuleFactory.getRuleByProcessCode(Matchers.anyString()))
				.thenReturn(this.processCodeOrderedRule);
		Mockito.doReturn(this.createNameTypes()).when(this.germplasmListManager).getGermplasmNameTypes();
		Mockito.doReturn(this.createGermplasmIds()).when(this.germplasmDataManager).addGermplasm(Matchers.anyList());
		Mockito.doReturn(new Method()).when(this.germplasmDataManager).getMethodByName(Matchers.anyString());
		Mockito.doReturn(new Method()).when(this.germplasmDataManager)
				.getMethodByID(CrossingServiceImplTest.BREEDING_METHOD_ID);
		Mockito.doReturn(this.createProject()).when(this.contextUtil).getProjectInContext();
		Mockito.doReturn("generatedSourceString").when(this.seedSourceGenertor).generateSeedSourceForCross(
				Matchers.any(Workbook.class), Matchers.anyString(), Matchers.anyString(), Matchers.anyString(),
				Matchers.anyString());
		Mockito.doReturn(new UserDefinedField(1552)).when(this.germplasmDataManager).getPlotCodeField();

		this.crossSetting = new CrossSetting();
		this.crossSetting.setCrossNameSetting(this.createCrossNameSetting());
		this.crossSetting.setBreedingMethodSetting(this.createBreedingMethodSetting());
		this.crossSetting.setAdditionalDetailsSetting(this.getAdditionalDetailsSetting());
	}

	private Project createProject() {
		final Project project = new Project();
		project.setCropType(new CropType("maize"));
		return project;
	}

	@Test
	public void testProcessCrossBreedingMethodCodeAlreadyAvailable() {
		final List<ImportedCrosses> crosses = this.importedCrossesList.getImportedCrosses();

		this.crossSetting.getBreedingMethodSetting().setBasedOnImportFile(true);

		// we modify the data such that one of the entries already have a raw
		// breeding method code (i.e., from import file)
		crosses.get(0).setRawBreedingMethod(CrossingServiceImplTest.TEST_BREEDING_METHOD_CODE);
		final Method method = new Method(CrossingServiceImplTest.TEST_BREEDING_METHOD_ID);
		Mockito.doReturn(method).when(this.germplasmDataManager)
				.getMethodByCode(CrossingServiceImplTest.TEST_BREEDING_METHOD_CODE);

		this.crossingService.processCrossBreedingMethod(this.crossSetting, this.importedCrossesList);

		Assert.assertEquals(
				"Raw breeding method codes after processing should resolve to breeding method IDs in the imported cross",
				CrossingServiceImplTest.TEST_BREEDING_METHOD_ID, crosses.get(0).getBreedingMethodId());
	}

	@Test
	public void testProcessCrossBreedingMethodIDAlreadyAvailable() {

		final List<ImportedCrosses> crosses = this.importedCrossesList.getImportedCrosses();

		this.crossSetting.getBreedingMethodSetting().setBasedOnImportFile(true);
		final Method breedingMethod = new Method();
		breedingMethod.setMid(CrossingServiceImplTest.TEST_BREEDING_METHOD_ID);
		Mockito.when(this.germplasmDataManager.getMethodByCode(Matchers.anyString())).thenReturn(breedingMethod);

		for (final ImportedCrosses cross : crosses) {
			cross.setRawBreedingMethod(String.valueOf(CrossingServiceImplTest.TEST_BREEDING_METHOD_CODE));
		}

		this.crossingService.processCrossBreedingMethod(this.crossSetting, this.importedCrossesList);

		for (final ImportedCrosses cross : crosses) {
			Assert.assertEquals(
					"Breeding method ID should not be overridden if it is already present in the imported cross info",
					CrossingServiceImplTest.TEST_BREEDING_METHOD_ID.intValue(), cross.getBreedingMethodId().intValue());
		}
	}

	@Test
	public void testProcessCrossBreedingMethodUseSetting() {
		this.crossSetting.getBreedingMethodSetting().setMethodId(CrossingServiceImplTest.TEST_BREEDING_METHOD_ID);

		this.crossingService.processCrossBreedingMethod(this.crossSetting, this.importedCrossesList);

		for (final ImportedCrosses importedCrosses : this.importedCrossesList.getImportedCrosses()) {
			Assert.assertEquals("User provided breeding method must be applied to all objects",
					CrossingServiceImplTest.TEST_BREEDING_METHOD_ID, importedCrosses.getBreedingMethodId());
		}
	}

	@Test
	public void testProcessCrossBreedingMethodNoSetting() {
		this.crossSetting.getBreedingMethodSetting().setMethodId(null);
		this.crossingService.processCrossBreedingMethod(this.crossSetting, this.importedCrossesList);

		this.setupMockCallsForGermplasm(Integer.parseInt(CrossingServiceImplTest.TEST_FEMALE_GID_1));
		this.setupMockCallsForGermplasm(Integer.parseInt(CrossingServiceImplTest.TEST_MALE_GID_1));
		this.setupMockCallsForGermplasm(Integer.parseInt(CrossingServiceImplTest.TEST_FEMALE_GID_2));
		this.setupMockCallsForGermplasm(Integer.parseInt(CrossingServiceImplTest.TEST_MALE_GID_2));

		for (final ImportedCrosses importedCrosses : this.importedCrossesList.getImportedCrosses()) {
			Assert.assertNotNull(
					"A method based on parental lines must be assigned to germplasms if user does not select a breeding method",
					importedCrosses.getBreedingMethodId());
			Assert.assertNotSame(
					"A method based on parental lines must be assigned to germplasms if user does not select a breeding method",
					0, importedCrosses.getBreedingMethodId());
		}
	}

	void setupMockCallsForGermplasm(final Integer gid) {
		final Germplasm germplasm = new Germplasm(gid);
		germplasm.setGnpgs(-1);
		Mockito.doReturn(germplasm).when(this.germplasmDataManager).getGermplasmByGID(gid);

	}

	@Test
	public void testApplyCrossSetting() throws MiddlewareQueryException {

		final CrossNameSetting crossNameSetting = this.crossSetting.getCrossNameSetting();
		this.crossingService.processCrossBreedingMethod(this.crossSetting, this.importedCrossesList);
		this.crossingService.applyCrossSetting(this.crossSetting, this.importedCrossesList,
				CrossingServiceImplTest.USER_ID, null);

		final ImportedCrosses cross1 = this.importedCrossesList.getImportedCrosses().get(0);

		Assert.assertEquals(CrossingServiceImplTest.SAVED_CROSSES_GID1, cross1.getGid());
		Assert.assertEquals(crossNameSetting.getPrefix() + " 0000100 " + crossNameSetting.getSuffix(),
				cross1.getDesig());
		Assert.assertEquals((Integer) 1, cross1.getEntryId());
		Assert.assertEquals("1", cross1.getEntryCode());
		Assert.assertEquals(null, cross1.getNames().get(0).getGermplasmId());
		Assert.assertEquals((Integer) 0, cross1.getNames().get(0).getLocationId());
		Assert.assertEquals(CrossingServiceImplTest.USER_ID, cross1.getNames().get(0).getUserId());

		final ImportedCrosses cross2 = this.importedCrossesList.getImportedCrosses().get(1);

		Assert.assertEquals(CrossingServiceImplTest.SAVED_CROSSES_GID2, cross2.getGid());
		Assert.assertEquals(crossNameSetting.getPrefix() + " 0000101 " + crossNameSetting.getSuffix(),
				cross2.getDesig());
		Assert.assertEquals((Integer) 2, cross2.getEntryId());
		Assert.assertEquals("2", cross2.getEntryCode());
		Assert.assertEquals(null, cross2.getNames().get(0).getGermplasmId());
		Assert.assertEquals((Integer) 0, cross2.getNames().get(0).getLocationId());
		Assert.assertEquals(CrossingServiceImplTest.USER_ID, cross2.getNames().get(0).getUserId());

	}

	@Test
	public void testApplyCrossNameSettingToImportedCrosses() throws MiddlewareQueryException {

		final CrossNameSetting setting = this.crossSetting.getCrossNameSetting();

		this.crossingService.applyCrossNameSettingToImportedCrosses(this.crossSetting,
				this.importedCrossesList.getImportedCrosses());

		final ImportedCrosses cross1 = this.importedCrossesList.getImportedCrosses().get(0);

		Assert.assertEquals(null, cross1.getGid());
		Assert.assertEquals(setting.getPrefix() + " 0000100 " + setting.getSuffix(), cross1.getDesig());
		Assert.assertEquals(cross1.getFemaleDesig() + setting.getSeparator() + cross1.getMaleDesig(),
				cross1.getCross());
		Assert.assertEquals((Integer) 1, cross1.getEntryId());
		Assert.assertEquals("1", cross1.getEntryCode());

		final ImportedCrosses cross2 = this.importedCrossesList.getImportedCrosses().get(1);

		Assert.assertEquals(null, cross2.getGid());
		Assert.assertEquals(setting.getPrefix() + " 0000101 " + setting.getSuffix(), cross2.getDesig());
		Assert.assertEquals(cross2.getFemaleDesig() + setting.getSeparator() + cross2.getMaleDesig(),
				cross2.getCross());
		Assert.assertEquals((Integer) 2, cross2.getEntryId());
		Assert.assertEquals("2", cross2.getEntryCode());
	}

	@Test
	public void testApplyCrossSetting_WhenSavingOfParentageDesignationNameIsSetToTrue() {
		final List<Pair<Germplasm, Name>> germplasmPairs = new ArrayList<>();

		final List<Integer> savedGermplasmIds = new ArrayList<Integer>();
		savedGermplasmIds.add(1);
		savedGermplasmIds.add(2);
		Mockito.doReturn(savedGermplasmIds).when(this.germplasmDataManager).addGermplasm(germplasmPairs);

		final CrossNameSetting crossNameSetting = this.createCrossNameSetting();
		crossNameSetting.setSaveParentageDesignationAsAString(true);

		this.crossSetting.setCrossNameSetting(crossNameSetting);
		this.crossingService.processCrossBreedingMethod(this.crossSetting, this.importedCrossesList);
		this.crossingService.applyCrossSetting(this.crossSetting, this.importedCrossesList,
				CrossingServiceImplTest.USER_ID, new Workbook());

		// TODO prepare descriptive messages for verification failure once
		// Mockito has stable 2.0 version
		Mockito.verify(this.germplasmDataManager, Mockito.atLeastOnce()).addGermplasmName(Matchers.any(List.class));

	}

	@Test
	public void testApplyCrossSetting_WhenSavingOfParentageDesignationNameIsSetToFalse() {
		final List<Pair<Germplasm, Name>> germplasmPairs = new ArrayList<>();

		final List<Integer> savedGermplasmIds = new ArrayList<Integer>();
		savedGermplasmIds.add(1);
		savedGermplasmIds.add(2);
		Mockito.doReturn(savedGermplasmIds).when(this.germplasmDataManager).addGermplasm(germplasmPairs);

		final CrossNameSetting crossNameSetting = this.createCrossNameSetting();
		crossNameSetting.setSaveParentageDesignationAsAString(false);

		this.crossSetting.setCrossNameSetting(crossNameSetting);
		this.crossingService.processCrossBreedingMethod(this.crossSetting, this.importedCrossesList);
		this.crossingService.applyCrossSetting(this.crossSetting, this.importedCrossesList,
				CrossingServiceImplTest.USER_ID, new Workbook());

		Mockito.verify(this.germplasmDataManager, Mockito.never()).addGermplasmName(Matchers.anyList());

	}

	@Test
	public void testBuildCrossName() {

		final CrossNameSetting setting = this.createCrossNameSetting();
		final ImportedCrosses cross = this.createCross();
		final String crossName = this.crossingService.buildCrossName(cross, setting.getSeparator());

		Assert.assertEquals(cross.getFemaleDesig() + setting.getSeparator() + cross.getMaleDesig(), crossName);

	}

	@Test
	public void testFormatHarvestDate() {
		Assert.assertTrue(new Integer(20150500).equals(this.crossingService.getFormattedHarvestDate("2015-05")));
	}

	@Test
	public void testPopulateGermplasmdateWithHarvestDate() {
		final Germplasm germplasm = new Germplasm();
		String harvestedDate = "2015-06";
		this.crossingService.populateGermplasmDate(germplasm, harvestedDate);
		harvestedDate = harvestedDate.replace("-", "");
		harvestedDate += "00";
		Assert.assertEquals(germplasm.getGdate(), new Integer(harvestedDate));
	}

	@Test
	public void testPopulateGermplasmdateWithCurrentDate() {
		final Germplasm germplasm = new Germplasm();
		this.crossingService.populateGermplasmDate(germplasm, "");
		Assert.assertEquals(germplasm.getGdate(), DateUtil.getCurrentDateAsIntegerValue());
	}

	@Test
	public void testBuildDesignationNameInSequenceDefaultSetting() {

		final CrossSetting crossSetting = this.createCrossSetting();
		final CrossNameSetting setting = new CrossNameSetting();
		setting.setPrefix("A");
		setting.setSuffix("B");

		crossSetting.setCrossNameSetting(setting);
		final String designationName = this.crossingService.buildDesignationNameInSequence(null, 1, crossSetting);
		Assert.assertEquals("A1B", designationName);
	}

	@Test
	public void testBuildDesignationNameInSequenceWithSpacesInPrefixSuffix() {

		final CrossSetting crossSetting = this.createCrossSetting();
		final CrossNameSetting setting = new CrossNameSetting();
		setting.setPrefix("A");
		setting.setSuffix("B");
		setting.setAddSpaceBetweenPrefixAndCode(true);
		setting.setAddSpaceBetweenSuffixAndCode(true);

		crossSetting.setCrossNameSetting(setting);
		final String designationName = this.crossingService.buildDesignationNameInSequence(null, 1, crossSetting);
		Assert.assertEquals("A 1 B", designationName);
	}

	@Test
	public void testBuildDesignationNameInSequenceWithNumOfDigits() {

		final CrossSetting crossSetting = this.createCrossSetting();
		final CrossNameSetting setting = crossSetting.getCrossNameSetting();
		setting.setAddSpaceBetweenPrefixAndCode(true);
		setting.setAddSpaceBetweenSuffixAndCode(true);
		setting.setNumOfDigits(3);
		setting.setPrefix("A");
		setting.setSuffix("B");

		final String designationName = this.crossingService.buildDesignationNameInSequence(null, 1, crossSetting);
		Assert.assertEquals("A 001 B", designationName);
	}

	@Test
	public void testBuildDesignationNameInSequenceMethodSuffixProcessCodeIsAvailable() throws RuleException {
		final String resolvedSuffixString = "AAA";
		Mockito.when(this.processCodeOrderedRule.runRule(Matchers.any(RuleExecutionContext.class)))
				.thenReturn(resolvedSuffixString);

		final CrossSetting crossSetting = new CrossSetting();
		final CrossNameSetting crossNameSetting = new CrossNameSetting();
		crossNameSetting.setSuffix(resolvedSuffixString);
		crossSetting.setCrossNameSetting(crossNameSetting);

		final ImportedCrosses importedCrosses = new ImportedCrosses();
		final int sequenceNumber = 1;
		final String designationName = this.crossingService.buildDesignationNameInSequence(importedCrosses,
				sequenceNumber, crossSetting);

		final String expectedResult = sequenceNumber + resolvedSuffixString;
		Assert.assertEquals("The designation name should be " + expectedResult, expectedResult, designationName);
	}

	@Test
	public void testBuildDesignationNameInSequenceMethodSuffixProcessCodeWithPrefix() throws RuleException {
		final String resolvedSuffixString = "AAA";
		Mockito.when(this.processCodeOrderedRule.runRule(Matchers.any(RuleExecutionContext.class)))
				.thenReturn(resolvedSuffixString);

		final CrossSetting crossSetting = new CrossSetting();
		final CrossNameSetting crossNameSetting = new CrossNameSetting();
		crossNameSetting.setSuffix(resolvedSuffixString);
		final String prefix = "B";
		crossNameSetting.setPrefix(prefix);
		crossSetting.setCrossNameSetting(crossNameSetting);

		final ImportedCrosses importedCrosses = new ImportedCrosses();
		final int sequenceNumber = 1;
		final String designationName = this.crossingService.buildDesignationNameInSequence(importedCrosses,
				sequenceNumber, crossSetting);

		final String expectedResult = prefix + sequenceNumber + resolvedSuffixString;
		Assert.assertEquals("The designation name should be " + expectedResult, expectedResult, designationName);
	}

	@Test
	public void testBuildPrefixStringDefault() {
		final CrossNameSetting setting = new CrossNameSetting();
		setting.setPrefix(" A  ");
		final String prefix = this.crossingService.buildPrefixString(setting);

		Assert.assertEquals("A", prefix);
	}

	@Test
	public void testBuildPrefixStringWithSpace() {
		final CrossNameSetting setting = new CrossNameSetting();
		setting.setPrefix("   A");
		setting.setAddSpaceBetweenPrefixAndCode(true);
		final String prefix = this.crossingService.buildPrefixString(setting);

		Assert.assertEquals("A ", prefix);
	}

	@Test
	public void testBuildSuffixStringDefault() {

		final CrossNameSetting setting = new CrossNameSetting();
		setting.setSuffix("  B   ");
		final String suffix = this.crossingService.buildSuffixString(setting, setting.getSuffix());

		Assert.assertEquals("B", suffix);
	}

	@Test
	public void testBuildSuffixStringWithSpace() {
		final CrossNameSetting setting = new CrossNameSetting();
		setting.setSuffix("   B   ");
		setting.setAddSpaceBetweenSuffixAndCode(true);
		final String suffix = this.crossingService.buildSuffixString(setting, setting.getSuffix());

		Assert.assertEquals(" B", suffix);
	}

	@Test
	public void testGenerateGermplasmNamePairs() throws MiddlewareQueryException {

		final CrossSetting crossSetting = new CrossSetting();
		final CrossNameSetting crossNameSetting = this.createCrossNameSetting();
		final BreedingMethodSetting breedingMethodSetting = new BreedingMethodSetting();
		final AdditionalDetailsSetting additionalDetailsSetting = this.createAdditionalDetailsSetting();
		crossSetting.setCrossNameSetting(crossNameSetting);
		crossSetting.setBreedingMethodSetting(breedingMethodSetting);
		crossSetting.setAdditionalDetailsSetting(additionalDetailsSetting);

		final CrossingServiceImpl.GermplasmListResult result = this.crossingService.generateGermplasmNamePairs(
				crossSetting, this.importedCrossesList.getImportedCrosses(), CrossingServiceImplTest.USER_ID, false);

		Pair<Germplasm, Name> germplasmNamePair = result.getGermplasmPairs().get(0);
		final Germplasm germplasm1 = germplasmNamePair.getLeft();
		final Name name1 = germplasmNamePair.getRight();
		final ImportedCrosses cross1 = this.importedCrossesList.getImportedCrosses().get(0);

		Assert.assertTrue(result.getIsTrimed());
		Assert.assertNull(germplasm1.getGid());
		Assert.assertEquals(20150101, germplasm1.getGdate().intValue());
		Assert.assertEquals(2, germplasm1.getGnpgs().intValue());
		Assert.assertEquals(cross1.getFemaleGid(), germplasm1.getGpid1().toString());
		Assert.assertEquals(cross1.getMaleGid(), germplasm1.getGpid2().toString());
		Assert.assertEquals(0, germplasm1.getGrplce().intValue());
		Assert.assertEquals(0, germplasm1.getLgid().intValue());
		Assert.assertEquals(0, germplasm1.getGrplce().intValue());
		Assert.assertEquals(0, germplasm1.getLgid().intValue());
		Assert.assertEquals(99, germplasm1.getLocationId().intValue());
		Assert.assertEquals(0, germplasm1.getMgid().intValue());
		Assert.assertNull(germplasm1.getPreferredAbbreviation());
		Assert.assertNull(germplasm1.getPreferredName());
		Assert.assertEquals(0, germplasm1.getReferenceId().intValue());
		Assert.assertEquals(CrossingServiceImplTest.USER_ID, germplasm1.getUserId());

		Assert.assertEquals(null, name1.getGermplasmId());
		Assert.assertEquals(99, name1.getLocationId().intValue());
		Assert.assertEquals(20150101, name1.getNdate().intValue());
		Assert.assertEquals(null, name1.getNid());
		Assert.assertEquals(null, name1.getNstat());
		Assert.assertFalse(name1.getNval().contains("(truncated)"));
		Assert.assertEquals(0, name1.getReferenceId().intValue());
		Assert.assertEquals(null, name1.getTypeId());
		Assert.assertEquals(CrossingServiceImplTest.USER_ID, name1.getUserId());

		germplasmNamePair = result.getGermplasmPairs().get(1);
		final Germplasm germplasm2 = germplasmNamePair.getLeft();
		final Name name2 = germplasmNamePair.getRight();
		final ImportedCrosses cross2 = this.importedCrossesList.getImportedCrosses().get(1);

		Assert.assertNull(null, germplasm2.getGid());
		Assert.assertEquals(20150101, germplasm2.getGdate().intValue());
		Assert.assertEquals(2, germplasm2.getGnpgs().intValue());
		Assert.assertEquals(cross2.getFemaleGid(), germplasm2.getGpid1().toString());
		Assert.assertEquals(cross2.getMaleGid(), germplasm2.getGpid2().toString());
		Assert.assertEquals(0, germplasm2.getGrplce().intValue());
		Assert.assertEquals(0, germplasm2.getLgid().intValue());
		Assert.assertEquals(0, germplasm2.getGrplce().intValue());
		Assert.assertEquals(0, germplasm2.getLgid().intValue());
		Assert.assertEquals(99, germplasm2.getLocationId().intValue());
		Assert.assertEquals(0, germplasm2.getMgid().intValue());
		Assert.assertNull(null, germplasm2.getPreferredAbbreviation());
		Assert.assertNull(null, germplasm2.getPreferredName());
		Assert.assertEquals(0, germplasm2.getReferenceId().intValue());
		Assert.assertEquals(CrossingServiceImplTest.USER_ID, germplasm2.getUserId());

		Assert.assertEquals(null, name2.getGermplasmId());
		Assert.assertEquals(99, name2.getLocationId().intValue());
		Assert.assertEquals(20150101, name2.getNdate().intValue());
		Assert.assertEquals(null, name2.getNid());
		Assert.assertEquals(null, name2.getNstat());
		Assert.assertTrue(name2.getNval().contains("(truncated)"));
		Assert.assertEquals(0, name2.getReferenceId().intValue());
		Assert.assertEquals(null, name2.getTypeId());
		Assert.assertEquals(CrossingServiceImplTest.USER_ID, name2.getUserId());
	}

	@Test
	public void testGetNextNumberInSequenceDefault() throws MiddlewareQueryException {

		final CrossNameSetting setting = new CrossNameSetting();
		setting.setStartNumber(1);
		setting.setPrefix("A");
		Mockito.doReturn("1").when(this.germplasmDataManager).getNextSequenceNumberForCrossName(Matchers.anyString());

		final int nextNumber = this.crossingService.getNextNumberInSequence(setting);

		Assert.assertEquals(1, nextNumber);
	}

	@Test
	public void testGetNextNumberInSequenceStartNumberIsSpecified() throws MiddlewareQueryException {

		final CrossNameSetting setting = new CrossNameSetting();
		setting.setStartNumber(1);
		setting.setPrefix("A");
		Mockito.doReturn("100").when(this.germplasmDataManager).getNextSequenceNumberForCrossName(Matchers.anyString());

		final int nextNumber = this.crossingService.getNextNumberInSequence(setting);

		Assert.assertEquals(1, nextNumber);
	}

	@Test
	public void testGetNextNumberInSequenceStartNumberIsNotSpecified() throws MiddlewareQueryException {

		final CrossNameSetting setting = new CrossNameSetting();
		setting.setStartNumber(0);
		setting.setPrefix("A");
		Mockito.doReturn("100").when(this.germplasmDataManager).getNextSequenceNumberForCrossName(Matchers.anyString());

		final int nextNumber = this.crossingService.getNextNumberInSequence(setting);

		Assert.assertEquals(100, nextNumber);
	}

	@Test
	public void testGetNumberWithLeadingZeroesAsStringDefault() {
		final CrossNameSetting setting = new CrossNameSetting();
		setting.setNumOfDigits(0);
		final String formattedString = this.crossingService.getNumberWithLeadingZeroesAsString(1, setting);

		Assert.assertEquals("1", formattedString);
	}

	@Test
	public void testGetNumberWithLeadingZeroesAsStringWithNumOfDigitsSpecified() {
		final CrossNameSetting setting = new CrossNameSetting();
		setting.setNumOfDigits(8);
		final String formattedString = this.crossingService.getNumberWithLeadingZeroesAsString(1, setting);

		Assert.assertEquals("00000001", formattedString);
	}

	@Test
	public void testGenerateSeedSource() {
		final String newSeedSource = "newSeedSource";
		Mockito.doReturn(newSeedSource).when(this.seedSourceGenertor).generateSeedSourceForCross(
				Matchers.any(Workbook.class), Matchers.anyString(), Matchers.anyString(), Matchers.anyString(),
				Matchers.anyString());

		// Case 1 - No seed source present. Generate new.
		final ImportedCrosses importedCross1 = new ImportedCrosses();
		importedCross1.setSource(null);
		this.crossingService.populateSeedSource(importedCross1, Mockito.mock(Workbook.class));
		Assert.assertEquals(newSeedSource, importedCross1.getSource());

		// Case 2 - Seed source is present. Keep.
		final ImportedCrosses importedCross2 = new ImportedCrosses();
		final String existingSeedSource = "existingSeedSource";
		importedCross2.setSource(existingSeedSource);
		this.crossingService.populateSeedSource(importedCross2, Mockito.mock(Workbook.class));
		Assert.assertEquals(existingSeedSource, importedCross2.getSource());

		// Case 3 - Seed source is presend but is PENDING indicator. Generate
		// new.
		final ImportedCrosses importedCross3 = new ImportedCrosses();
		importedCross3.setSource(ImportedCrosses.SEED_SOURCE_PENDING);
		this.crossingService.populateSeedSource(importedCross3, Mockito.mock(Workbook.class));
		Assert.assertEquals(newSeedSource, importedCross3.getSource());

		// Case 4 - Seed source is present but empty string. Generate new.
		final ImportedCrosses importedCross4 = new ImportedCrosses();
		importedCross4.setSource("");
		this.crossingService.populateSeedSource(importedCross4, Mockito.mock(Workbook.class));
		Assert.assertEquals(newSeedSource, importedCross4.getSource());

	}

	@Test
	public void testApplyCrossSettingWithNamingRules() {

		final CrossSetting crossSetting = this.createCrossSetting();
		final ImportedCrossesList importedCrossesList = this.createImportedCrossesList();
		final Integer userId = 123456;
		final Workbook workbook = new Workbook();

		this.importedCrossesList.addImportedCrosses(this.createCross());
		this.importedCrossesList.addImportedCrosses(this.createSecondCross());

		this.crossingService.applyCrossSettingWithNamingRules(crossSetting, importedCrossesList, userId, workbook);

		int counter = 1;
		for (final ImportedCrosses importedCross : importedCrossesList.getImportedCrosses()) {
			Assert.assertEquals(importedCross.getEntryCode(), importedCross.getEntryId());
			Assert.assertTrue(importedCross.getEntryCode().equals(counter));
			Assert.assertTrue(importedCross.getEntryId().equals(counter));
			counter++;
		}
	}

	private ImportedCrossesList createImportedCrossesList() {

		final ImportedCrossesList importedCrossesList = new ImportedCrossesList();
		final List<ImportedCrosses> importedCrosses = new ArrayList<>();
		importedCrossesList.setImportedGermplasms(importedCrosses);
		return importedCrossesList;

	}

	private List<ImportedCrosses> createImportedCrosses() {

		final List<ImportedCrosses> importedCrosses = new ArrayList<>();
		final ImportedCrosses cross = new ImportedCrosses();
		cross.setFemaleDesig("FEMALE-12345");
		cross.setFemaleGid(CrossingServiceImplTest.TEST_FEMALE_GID_1);
		cross.setMaleDesig("MALE-54321");
		cross.setMaleGid(CrossingServiceImplTest.TEST_MALE_GID_1);
		cross.setCross("CROSS");
		cross.setSource("MALE:1:FEMALE:1");
		cross.setDesig(
				"G9BC0RL34-1P-5P-2-1P-3P-B/G9BC1TSR8P-1P-1P-5P-3P-1P-1P)-3-1-1-1-B*8/((CML150xCLG2501)-B-31-1-B-1-BBB/CML193-BB)-B-1-BB(NonQ)-B*8)-B/((G9BC0RL34-1P-5P-2-1P-3P-B/G9BC1TSR8P-1P-1P-5P-3P-1P-1P)-3-1-1-1-B*8/((CML161xCML451)-B-18-1-BBB/CML1612345");
		importedCrosses.add(cross);
		final ImportedCrosses cross2 = this.createSecondCross();
		importedCrosses.add(cross2);

		return importedCrosses;

	}

	private ImportedCrosses createSecondCross() {
		final ImportedCrosses cross2 = new ImportedCrosses();
		cross2.setFemaleDesig("FEMALE-9999");
		cross2.setFemaleGid(CrossingServiceImplTest.TEST_FEMALE_GID_2);
		cross2.setMaleDesig("MALE-8888");
		cross2.setMaleGid(CrossingServiceImplTest.TEST_MALE_GID_2);
		cross2.setCross("CROSS");
		cross2.setSource("MALE:2:FEMALE:2");
		cross2.setDesig(
				"((G9BC0RL34-1P-5P-2-1P-3P-B/G9BC1TSR8P-1P-1P-5P-3P-1P-1P)-3-1-1-1-B*8/((CML150xCLG2501)-B-31-1-B-1-BBB/CML193-BB)-B-1-BB(NonQ)-B*8)-B((G9BC0RL34-1P-5P-2-1P-3P-B/G9BC1TSR8P-1P-1P-5P-3P-1P-1P)-3-1-1-1-B*8/((CML150xCLG2501)-B-31-1-B-1-BBB/CML193-BB)-B-1-BB(NonQ)-B*8)-B/((G9BC0RL34-1P-5P-2-1P-3P-B/G9BC1TSR8P-1P-1P-5P-3P-1P-1P)-3-1-1-1-B*8/((CML161xCML451)-B-18-1-BBB/CML161");
		return cross2;
	}

	private ImportedCrosses createCross() {
		final ImportedCrosses cross = new ImportedCrosses();
		cross.setFemaleDesig("FEMALE-12345");
		cross.setFemaleGid("12345");
		cross.setMaleDesig("MALE-54321");
		cross.setMaleGid("54321");
		cross.setDesig("Cros12345");
		return cross;
	}

	private CrossSetting createCrossSetting() {
		return new CrossSetting(null, null, this.createCrossNameSetting(), this.createAdditionalDetailsSetting());
	}

	private CrossNameSetting createCrossNameSetting() {
		final CrossNameSetting setting = new CrossNameSetting();

		setting.setPrefix("PREFIX");
		setting.setSuffix("SUFFIX");
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
		setting.setHarvestLocationId(99);

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

}
