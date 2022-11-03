
package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.springframework.stereotype.Component;

import com.efficio.fieldbook.web.trial.bean.AdvancingStudy;
import org.generationcp.middleware.ruleengine.pojo.DeprecatedAdvancingSource;

@Deprecated
@Component
public class SeasonExpressionDataProcessor implements ExpressionDataProcessor {

	@Override
	public void processEnvironmentLevelData(final DeprecatedAdvancingSource source, final Workbook workbook, final AdvancingStudy nurseryInfo,
			final Study study) {
		final Map<Integer, String> measurementVariablesValues = new HashMap<Integer, String>();
		for (final MeasurementVariable mv : workbook.getConditions()) {
			this.addValueToMeasurementVariablesValues(mv.getValue(), mv.getPossibleValues(), mv.getTermId(), measurementVariablesValues);
		}
		source.setSeason(this.getValueOfPrioritySeasonVariable(measurementVariablesValues));
	}

	@Override
	public void processPlotLevelData(final DeprecatedAdvancingSource source, final MeasurementRow row) {
		if (StringUtils.isBlank(source.getSeason())
				&& source.getTrailInstanceObservationMeasurementRow() != null && source.getTrailInstanceObservationMeasurementRow().getDataList() != null) {
			final Map<Integer, String> measurementVariablesValues = new HashMap<>();
			for (final MeasurementData measurementData : source.getTrailInstanceObservationMeasurementRow().getDataList()) {
				final int termId = measurementData.getMeasurementVariable().getTermId();
				final List<ValueReference> possibleValues = measurementData.getMeasurementVariable().getPossibleValues();
				this.addValueToMeasurementVariablesValues(measurementData.getValue(), possibleValues, termId, measurementVariablesValues);
			}
			source.setSeason(this.getValueOfPrioritySeasonVariable(measurementVariablesValues));
		}
	}

	private String getValueOfPrioritySeasonVariable(final Map<Integer, String> measurementVariablesValues) {
		String season = "";
		if (measurementVariablesValues.get(TermId.SEASON_MONTH.getId()) != null) {
			season = measurementVariablesValues.get(TermId.SEASON_MONTH.getId());
		} else if (measurementVariablesValues.get(TermId.SEASON_VAR_TEXT.getId()) != null) {
			season = measurementVariablesValues.get(TermId.SEASON_VAR_TEXT.getId());
		} else if (measurementVariablesValues.get(TermId.SEASON_VAR.getId()) != null) {
			season = measurementVariablesValues.get(TermId.SEASON_VAR.getId());
		}
		return season;
	}

	String getSeasonName(final String value, final List<ValueReference> possibleValues) {

		if (possibleValues != null && !possibleValues.isEmpty()) {
			for (final ValueReference valueReference : possibleValues) {
				// The Season Code variable is categorical type, it's value should be the id of the season (valid value).
				// But Season Code's value can also be the text description of the season, so we also need to find the valid value by description.
				if ((StringUtils.isNumeric(value) && valueReference.getId().intValue() == Integer.parseInt(value)) || valueReference.getDescription().equals(value)) {
					return valueReference.getName();
				}
			}
		}
		// if the value is not in the possible values (valid values), just return it as is.
		return value;
	}

	private void addValueToMeasurementVariablesValues(final String value, final List<ValueReference> possibleValues, final int termId,
			final Map<Integer, String> measurementVariablesValues) {
		if (StringUtils.isNotBlank(value)) {
			if (termId == TermId.SEASON_VAR.getId()) {
				final String seasonVarValue = this.getSeasonName(value, possibleValues);
				measurementVariablesValues.put(termId, seasonVarValue);
			} else {
				measurementVariablesValues.put(termId, value);
			}
		}
	}
}
