
package com.efficio.fieldbook.web.naming.impl;

import org.generationcp.middleware.ruleengine.Rule;
import org.generationcp.middleware.ruleengine.RuleFactory;
import org.generationcp.middleware.ruleengine.RulesPostProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ContextConfiguration;

import org.generationcp.middleware.ruleengine.newnaming.rules.CountRule;
import com.efficio.fieldbook.web.trial.controller.CreateTrialController;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 2/16/2015 Time: 12:52 PM
 */

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration
public class RulePostProcessorTest {

	@Mock
	private RuleFactory factory;

	@InjectMocks
	private RulesPostProcessor postProcessor;

	@Test
	public void testPostProcessingForRule() {
		// verify that Rule objects are processed correctly by the class
		Rule rule = new CountRule();

		this.postProcessor.postProcessAfterInitialization(rule, rule.getKey());
		Mockito.verify(this.factory).addRule(rule);
	}

	@Test
	public void testPostProcessingOtherObjects() {
		// verify that other objects in component scan do not affect processor
		CreateTrialController controller = new CreateTrialController();

		this.postProcessor.postProcessAfterInitialization(controller, "CreateTrialController");
		Mockito.verify(this.factory, Mockito.never()).addRule(Matchers.any(Rule.class));
	}
}
