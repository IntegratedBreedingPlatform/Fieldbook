
package com.efficio.fieldbook.web.naming.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Assert;

import org.generationcp.middleware.ruleengine.RuleException;
import org.generationcp.middleware.ruleengine.RuleFactory;
import org.generationcp.middleware.ruleengine.service.RulesService;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import org.generationcp.middleware.ruleengine.naming.deprecated.rules.DeprecatedEnforceUniqueNameRule;
import org.generationcp.middleware.ruleengine.naming.deprecated.rules.DeprecatedNamingRuleExecutionContext;
import org.generationcp.middleware.ruleengine.naming.service.ProcessCodeService;
import org.generationcp.middleware.ruleengine.pojo.DeprecatedAdvancingSource;

@Ignore(value ="BMS-1571. Ignoring temporarily. Please fix the failures and remove @Ignore.")
public class RuleServiceImplTest extends AbstractBaseIntegrationTest {

	@Resource
	RulesService rulesService;

	@Resource
	private ProcessCodeService processCodeService;

	@Resource
	private RuleFactory ruleFactory;

	@Resource
	private MessageSource messageSource;

	private GermplasmDataManager germplasmDataManager;

	private Method breedingMethod;
	private DeprecatedAdvancingSource row;
	private String testGermplasmName;
	private Integer breedingMethodSnameType;

	@Before
	public void setUp() {
		this.breedingMethodSnameType = 5;
		// namingConventionService.setMessageSource(Mockito.mock(ResourceBundleMessageSource.class));
		this.breedingMethod = new Method();
		this.breedingMethod.setSnametype(this.breedingMethodSnameType);
		this.breedingMethod.setPrefix("pre");
		this.breedingMethod.setSeparator("-");
		this.breedingMethod.setCount("[NUMBER]");
		this.breedingMethod.setSuffix("suff");
		this.row = new DeprecatedAdvancingSource();
		this.row.setBreedingMethod(this.breedingMethod);
		this.row.setPlantsSelected(2);
		this.testGermplasmName = "test-germplasm-name";
	}

	private Name generateNewName(Integer typeId, Integer nStat) {
		Name name = new Name();
		name.setTypeId(typeId);
		name.setNstat(nStat);
		name.setNval(this.testGermplasmName);
		return name;
	}

	@Test
	public void testRulesEngineUniqueCheckPass() {

		List<Name> names = new ArrayList<Name>();
		names.add(this.generateNewName(this.breedingMethodSnameType, 1));
		this.row.setNames(names);

		this.germplasmDataManager = Mockito.mock(GermplasmDataManager.class);

		try {
			Mockito.when(this.germplasmDataManager.checkIfMatches(Matchers.anyString())).thenReturn(false);
			List<String> sequenceList = Arrays.asList(this.ruleFactory.getRuleSequenceForNamespace("naming"));
			sequenceList = new ArrayList<>(sequenceList);
			sequenceList.add(DeprecatedEnforceUniqueNameRule.KEY);
			DeprecatedNamingRuleExecutionContext ruleExecutionContext =
					new DeprecatedNamingRuleExecutionContext(sequenceList, this.processCodeService, this.row, this.germplasmDataManager,
							new ArrayList<String>());
			ruleExecutionContext.setMessageSource(this.messageSource);
			List<String> results = (List<String>) this.rulesService.runRules(ruleExecutionContext);

			Assert.assertFalse(results.isEmpty());
			System.out.println(results);

			Assert.assertEquals("test-germplasm-name-pre1suff", results.get(0));

			Assert.assertNull(this.row.getChangeDetail());
		} catch (RuleException | MiddlewareQueryException e) {

			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testRulesEngineUniqueCheckFail() {

		List<Name> names = new ArrayList<Name>();
		names.add(this.generateNewName(this.breedingMethodSnameType, 1));
		this.row.setNames(names);

		this.germplasmDataManager = Mockito.mock(GermplasmDataManager.class);

		try {
			// set the test up so that the unique check fails twice before passing
			Mockito.when(this.germplasmDataManager.checkIfMatches(Matchers.anyString())).thenReturn(true).thenReturn(true)
					.thenReturn(false);

			List<String> sequenceList = Arrays.asList(this.ruleFactory.getRuleSequenceForNamespace("naming"));
			sequenceList = new ArrayList<>(sequenceList);
			sequenceList.add(DeprecatedEnforceUniqueNameRule.KEY);

			DeprecatedNamingRuleExecutionContext ruleExecutionContext =
					new DeprecatedNamingRuleExecutionContext(sequenceList, this.processCodeService, this.row, this.germplasmDataManager,
							new ArrayList<String>());
			ruleExecutionContext.setMessageSource(this.messageSource);

			List<String> results = (List<String>) this.rulesService.runRules(ruleExecutionContext);
			Assert.assertFalse(results.isEmpty());

			System.out.println(results);

			Assert.assertEquals("test-germplasm-name-pre3suff", results.get(0));
			Assert.assertNotNull(this.row.getChangeDetail());
			Assert.assertNotNull("Sequence text not properly set for change detail object", this.row.getChangeDetail().getAddSequenceText());
			Assert.assertNotNull("Question text not properly set for change detail object", this.row.getChangeDetail().getQuestionText());
		} catch (RuleException | MiddlewareQueryException e) {

			Assert.fail(e.getMessage());
		}
	}

}
