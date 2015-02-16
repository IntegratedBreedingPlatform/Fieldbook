package com.efficio.fieldbook.web.naming.impl;

import com.efficio.fieldbook.web.naming.RuleConfigurationProvider;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/14/2015
 * Time: 6:55 AM
 */
public class PropertyFileRuleConfigurationProvider implements RuleConfigurationProvider {

	private Map<String, List<String>> ruleSequenceConfiguration;

	@Override public Map<String, List<String>> retrieveRuleSequenceConfiguration() {
		return null;
	}

	public void setRuleSequenceConfiguration(Map<String, List<String>> ruleSequenceConfiguration) {
		this.ruleSequenceConfiguration = ruleSequenceConfiguration;
	}
}
