
package com.efficio.fieldbook.web.naming.impl;

import com.google.common.collect.Lists;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.ruleengine.RuleException;
import org.generationcp.middleware.ruleengine.RuleExecutionContext;
import org.generationcp.middleware.ruleengine.RuleExecutionNamespace;
import org.generationcp.middleware.ruleengine.RuleFactory;
import org.generationcp.middleware.ruleengine.namingdeprecated.impl.DeprecatedNamingConventionServiceImpl;
import org.generationcp.middleware.ruleengine.pojo.DeprecatedAdvancingSource;
import org.generationcp.middleware.ruleengine.pojo.DeprecatedAdvancingSourceList;
import org.generationcp.middleware.ruleengine.pojo.ImportedCross;
import org.generationcp.middleware.ruleengine.pojo.ImportedGermplasm;
import org.generationcp.middleware.ruleengine.pojo.ImportedGermplasmParent;
import org.generationcp.middleware.ruleengine.service.RulesService;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.hamcrest.CoreMatchers.is;

public class DeprecatedNamingConventionServiceImplTest {

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private RulesService rulesService;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private RuleFactory ruleFactory;

	@InjectMocks
	private final DeprecatedNamingConventionServiceImpl namingConventionService = new DeprecatedNamingConventionServiceImpl();

	private Method breedingMethod;
	private DeprecatedAdvancingSource row;
	private Integer breedingMethodSnameType;
	private Workbook workbook;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.breedingMethodSnameType = 5;
		this.breedingMethod = new Method();
		this.breedingMethod.setSnametype(this.breedingMethodSnameType);
		this.breedingMethod.setGeneq(TermId.NON_BULKING_BREEDING_METHOD_CLASS.getId());
		this.row = new DeprecatedAdvancingSource();
		this.row.setBreedingMethod(this.breedingMethod);
		this.workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyType(StudyTypeDto.getNurseryDto());
		studyDetails.setStudyName("STUDY:ABC");
		studyDetails.setId(new Random().nextInt());
		this.workbook.setStudyDetails(studyDetails);

	}

	@Test
	public void testGenerateCrossesList() throws RuleException {
		final List<ImportedCross> importedCrosses = new ArrayList<>();
		final ImportedCross importedCross = new ImportedCross();
		final ImportedGermplasmParent femaleParent = new ImportedGermplasmParent(1, "femaleDesig", "femalePedig");
		importedCross.setFemaleParent(femaleParent);
		final ImportedGermplasmParent maleParent = new ImportedGermplasmParent(2, "maleDesig", "malePedig");
		importedCross.setMaleParents(Collections.singletonList(maleParent));
		importedCrosses.add(importedCross);

		final DeprecatedAdvancingSourceList rows = new DeprecatedAdvancingSourceList();
		final DeprecatedAdvancingSource advancingSource = new DeprecatedAdvancingSource();
		advancingSource.setBreedingMethodId(101);
		rows.setRows(Collections.singletonList(advancingSource));

		final Workbook workbook = Mockito.mock(Workbook.class);
		final List<Integer> gids = Collections.singletonList(1);
		final Method method = new Method();
		method.setMid(101);
		method.setPrefix("IB");
		method.setCount("[COUNT]");

		Mockito.when(this.fieldbookMiddlewareService.getAllBreedingMethods(false)).thenReturn(Collections.singletonList(method));
		Mockito.when(this.germplasmDataManager.isMethodNamingConfigurationValid(method)).thenReturn(true);
		Mockito.when(this.rulesService.runRules(ArgumentMatchers.any(RuleExecutionContext.class)))
			.thenReturn(Collections.singletonList("name"));
		Mockito.when(this.ruleFactory.getRuleSequenceForNamespace(RuleExecutionNamespace.NAMING)).thenReturn(new String[] {"[COUNT]"});

		this.namingConventionService.generateCrossesList(importedCrosses, rows, true, workbook, gids);
		Assert.assertEquals("name", importedCross.getDesig());
		Mockito.verify(this.fieldbookMiddlewareService).getAllBreedingMethods(false);
		Mockito.verify(this.germplasmDataManager).isMethodNamingConfigurationValid(method);
		Mockito.verify(this.rulesService).runRules(ArgumentMatchers.any(RuleExecutionContext.class));
		Mockito.verify(this.ruleFactory).getRuleSequenceForNamespace(RuleExecutionNamespace.NAMING);
		Assert.assertEquals(0, advancingSource.getCurrentMaxSequence());
	}

	private ImportedGermplasm createImportedGermplasm(final int gid) {
		final String gidString = String.valueOf(gid);
		final String desig = "ABC" + gid;

		final ImportedGermplasm germplasm = new ImportedGermplasm();
		germplasm.setGid(gidString);
		germplasm.setEntryNumber(gid);
		germplasm.setEntryCode(gidString);
		germplasm.setDesig(desig);
		germplasm.setSource("XYZ:" + gid);
		germplasm.setCross(gid + "/" + (gid + 1));
		germplasm.setSource("Import file");
		germplasm.setLocationId(RandomUtils.nextInt());
		germplasm.setTrialInstanceNumber("1");
		germplasm.setPlotNumber(gidString);

		return germplasm;
	}

	private DeprecatedAdvancingSource createAdvancingSource() {
		final DeprecatedAdvancingSource as1 = new DeprecatedAdvancingSource();
		as1.setNames(new ArrayList<>());
		as1.setBreedingMethod(this.breedingMethod);
		as1.setPlantsSelected(1);
		as1.setBulk(false);
		as1.setStudyName("Test One");
		as1.setSeason("201412");
		as1.setCurrentMaxSequence(0);
		as1.setTrialInstanceNumber("1");

		return as1;
	}
}
