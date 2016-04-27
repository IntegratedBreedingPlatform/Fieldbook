package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.KeySequenceRegisterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

@Component
public class DoubleHaploidSourceExpression extends BaseExpression {

    private static final Logger LOG = LoggerFactory.getLogger(DoubleHaploidSourceExpression.class);

    public static final String KEY = "[DHSOURCE]";

	@Autowired
	private KeySequenceRegisterService keySequenceRegisterService;

	/**
	 * Method to append '@' + [lastUsedSequence] in designation column ex. @1, @2 etc.
	 * @param values Designation column value
	 * @param source Advancing Source object contains information about source
	 */
    @Override
    public void apply(List<StringBuilder> values, AdvancingSource source) {
        for (StringBuilder value : values) {

            try {
	            String suffixValue = "@";
				String methodCode = source.getBreedingMethod().getMcode();
				if(methodCode != null) {
					int checkIndex = value.lastIndexOf("@0");

					if(checkIndex != -1) {
						// Get the last used sequence number for method and append it using suffix value
						int lastUsedSequence = this.keySequenceRegisterService.incrementAndGetNextSequence(methodCode);
						this.replaceExistingSuffixValue(value, checkIndex);
						this.replaceExpressionWithValue(value, suffixValue + lastUsedSequence);
					} else {
						// If designation does not contains @0 string then keep its value as it is
						this.replaceExpressionWithValue(value, "");
					}
				}
			} catch (MiddlewareQueryException e) {
                DoubleHaploidSourceExpression.LOG.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public String getExpressionKey() {
        return KEY;
    }

	/**
	 * Replace the existing suffix value '@0' from designation so that it will be used later
	 * @param container designation value
	 * @param startIndex starting index of @0
	 */
	private void replaceExistingSuffixValue(StringBuilder container, int startIndex) {
		int endIndex = startIndex + 2;

		container.replace(startIndex, endIndex, "");
	}
}
