
package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.springframework.stereotype.Component;

import com.efficio.fieldbook.web.trial.bean.AdvancingStudy;
import org.generationcp.middleware.ruleengine.pojo.DeprecatedAdvancingSource;

@Deprecated
@Component
public class SelectionTraitExpressionDataProcessor implements ExpressionDataProcessor {

	public static final String SELECTION_TRAIT_PROPERTY = "Selection Criteria";

	@Resource
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Resource
	private ContextUtil contextUtil;

	@Override
	public void processEnvironmentLevelData(final DeprecatedAdvancingSource source, final Workbook workbook, final AdvancingStudy nurseryInfo,
			final Study study) {
        // management details / study details are stored within the workbook conditions. study conditions are stored in the workbook
		// constants
        final List<MeasurementVariable> possibleEnvironmentSources = new ArrayList<>(workbook.getConditions());
		if (workbook.getConstants() != null) {
			possibleEnvironmentSources.addAll(workbook.getConstants());
		}
		for (final MeasurementVariable condition : possibleEnvironmentSources) {
			if (SELECTION_TRAIT_PROPERTY.equalsIgnoreCase(condition.getProperty())) {
				setSelectionTraitValue(condition.getValue(), source, condition.getTermId(), condition.getPossibleValues());
			}
		}
	}

	@Override
	public void processPlotLevelData(final DeprecatedAdvancingSource source, final MeasurementRow row) {
		final List<MeasurementData> rowData = row.getDataList();

		if(source.getTrailInstanceObservationMeasurementRow() != null){
			rowData.addAll(source.getTrailInstanceObservationMeasurementRow().getDataList());
		}

		for (final MeasurementData measurementData : rowData) {
			if (SELECTION_TRAIT_PROPERTY.equalsIgnoreCase(measurementData.getMeasurementVariable().getProperty())) {
				setSelectionTraitValue(measurementData.getValue(), source, measurementData.getMeasurementVariable().getTermId(),
					measurementData.getMeasurementVariable().getPossibleValues());
			}
		}
	}

	protected void setSelectionTraitValue(final String categoricalValue, final DeprecatedAdvancingSource source, final int termID, final List<ValueReference> possibleValuesForSelectionTraitProperty){
		if(StringUtils.isNumeric(categoricalValue)){
			source.setSelectionTraitValue(extractValue(categoricalValue, termID));
		}
		else{
			if(possibleValuesForSelectionTraitProperty != null && !possibleValuesForSelectionTraitProperty.isEmpty()){
				for(final ValueReference valueReference : possibleValuesForSelectionTraitProperty){
					if(Objects.equals(valueReference.getDescription(), categoricalValue)){
						source.setSelectionTraitValue(extractValue(String.valueOf(valueReference.getId()), termID));
					}
				}
			}
		}
	}

	protected String extractValue(final String value, final Integer variableTermID) {
		if (!StringUtils.isNumeric(value)) {
			// this case happens when a character value is provided as an out of bounds value
			return value;
		}

		final String categoricalValue =
				this.ontologyVariableDataManager.retrieveVariableCategoricalNameValue(contextUtil.getCurrentProgramUUID(), variableTermID,
						Integer.parseInt(value), true);

		if (categoricalValue == null) {
			// this case happens when a numeric value is provided as an out of bounds value
			return value;
		} else {
			return categoricalValue;
		}
	}
}
