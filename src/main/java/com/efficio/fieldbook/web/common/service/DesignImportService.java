
package com.efficio.fieldbook.web.common.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;

import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.trial.bean.EnvironmentData;

public interface DesignImportService {

	List<MeasurementRow> generateDesign(Workbook workbook, DesignImportData designImportData, EnvironmentData environmentData,
			boolean isPreview) throws DesignValidationException;

	Set<MeasurementVariable> getDesignMeasurementVariables(Workbook workbook, DesignImportData designImportData, boolean isPreview);

	Set<StandardVariable> getDesignRequiredStandardVariables(Workbook workbook, DesignImportData designImportData);

	Set<MeasurementVariable> getMeasurementVariablesFromDataFile(Workbook workbook, DesignImportData designImportData);

	boolean areTrialInstancesMatchTheSelectedEnvironments(Integer noOfEnvironments, DesignImportData designImportData);

	Map<PhenotypicType, List<DesignHeaderItem>> categorizeHeadersByPhenotype(List<DesignHeaderItem> designHeaders)
			throws MiddlewareException;

	Set<MeasurementVariable> getDesignRequiredMeasurementVariable(Workbook workbook, DesignImportData designImportData);

	Set<MeasurementVariable> extractMeasurementVariable(PhenotypicType phenotypicType,
			Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders);

	DesignHeaderItem filterDesignHeaderItemsByTermId(TermId termId, List<DesignHeaderItem> headerDesignItems);

	Map<String, Map<Integer, List<String>>> groupCsvRowsIntoTrialInstance(DesignHeaderItem trialInstanceHeaderItem,
			Map<Integer, List<String>> csvMap);
}
