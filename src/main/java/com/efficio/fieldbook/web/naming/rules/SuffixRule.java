package com.efficio.fieldbook.web.naming.rules;

import java.util.List;

import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;


public class SuffixRule implements Rule {
	
	private ProcessCodeService processCodeService;
	private AdvancingSource advancingSource;
	
	@Override
	public void init(ProcessCodeService processCodeService, Object source) {
		this.processCodeService = processCodeService;
		advancingSource = (AdvancingSource) source;		
	}

	@Override
	public List<String> runRule(List<String> input) throws RuleException {
		// append a suffix string onto each element of the list - in place
		for (int i = 0; i < input.size(); i++) {
			input.set(i, input.get(i) + processCodeService.applyToName(advancingSource.getBreedingMethod().getSuffix(), advancingSource).get(0));
		}
		return input;
	}

}
