package com.efficio.fieldbook.web.naming.impl;

import java.text.SimpleDateFormat;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.service.GermplasmOriginGenerationParameters;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.naming.service.GermplasmOriginParameterBuilder;

@Service
public class GermplasmOriginParameterBuilderImpl implements GermplasmOriginParameterBuilder {

	@Resource
	private ContextUtil contextUtil;

	@Resource
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Override
	public GermplasmOriginGenerationParameters build(Workbook workbook, String plotNumber) throws FieldbookException {
		final GermplasmOriginGenerationParameters originGenerationParameters = new GermplasmOriginGenerationParameters();
		originGenerationParameters.setCrop(this.contextUtil.getProjectInContext().getCropType().getCropName());
		originGenerationParameters.setStudyName(workbook.getStudyName());
		originGenerationParameters.setStudyType(workbook.getStudyDetails().getStudyType());

		// To populate LOCATION placeholder we look for LOCATION_ABBR(8189) variable in general settings.
		MeasurementVariable locationAbbrVariable = workbook.findConditionById(TermId.LOCATION_ABBR.getId());
		if (locationAbbrVariable != null) {
			originGenerationParameters.setLocation(locationAbbrVariable.getValue());
		}

		// To populate SEASON placeholder we look for Crop_season_Code(8371) variable in general settings.
		MeasurementVariable seasonVariable = workbook.findConditionById(TermId.SEASON_VAR.getId());
		if (seasonVariable != null && StringUtils.isNotBlank(seasonVariable.getValue())) {
			Variable variable =
					this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(), seasonVariable.getTermId(), true,
							false);
			for (TermSummary seasonOption : variable.getScale().getCategories()) {
				if (seasonVariable.getValue().equals(seasonOption.getId().toString())) {
					originGenerationParameters.setSeason(seasonOption.getDefinition());
					break;
				}
			}
		} else {
			// Default the season to current year and month.
			SimpleDateFormat formatter = new SimpleDateFormat("YYYYMM");
			String currentYearAndMonth = formatter.format(new java.util.Date());
			originGenerationParameters.setSeason(currentYearAndMonth);
		}
		originGenerationParameters.setPlotNumber(plotNumber);
		return originGenerationParameters;
	}

}
