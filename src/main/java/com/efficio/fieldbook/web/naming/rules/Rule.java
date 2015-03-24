package com.efficio.fieldbook.web.naming.rules;

public interface Rule<T extends RuleExecutionContext> {


	public Object runRule(T context) throws RuleException;

	public String getNextRuleStepKey(T context);

	public String getKey();
}
