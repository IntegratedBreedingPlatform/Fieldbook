
package com.efficio.fieldbook.web.importdesign.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;

import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.trial.bean.InstanceInfo;

public interface DesignImportService {

	List<MeasurementRow> generateDesign(Workbook workbook, DesignImportData designImportData, InstanceInfo instanceInfo,
			boolean isPreview, Map<String, Integer> additionalParams) throws DesignValidationException;

	Set<MeasurementVariable> getDesignMeasurementVariables(DesignImportData designImportData, boolean isPreview);

	Set<StandardVariable> getDesignRequiredStandardVariables(Workbook workbook, DesignImportData designImportData);

	Set<MeasurementVariable> getMeasurementVariablesFromDataFile(Workbook workbook, DesignImportData designImportData);

	boolean areTrialInstancesMatchTheSelectedEnvironments(Integer noOfEnvironments, DesignImportData designImportData)
			throws DesignValidationException;

	Map<PhenotypicType, List<DesignHeaderItem>> categorizeHeadersByPhenotype(List<DesignHeaderItem> designHeaders);

	Set<MeasurementVariable> getDesignRequiredMeasurementVariable(Workbook workbook, DesignImportData designImportData);

	Set<MeasurementVariable> extractMeasurementVariable(PhenotypicType phenotypicType,
			Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders);

	Map<String, Map<Integer, List<String>>> groupCsvRowsIntoTrialInstance(DesignHeaderItem trialInstanceHeaderItem,
			Map<Integer, List<String>> csvMap);

	DesignHeaderItem validateIfStandardVariableExists(Map<Integer, DesignHeaderItem> map, String messageCodeId, TermId termId)
			throws DesignValidationException;

}
