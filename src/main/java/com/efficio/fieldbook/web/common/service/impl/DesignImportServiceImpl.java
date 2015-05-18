package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.springframework.context.MessageSource;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.common.service.DesignImportService;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.mysql.jdbc.StringUtils;


public class DesignImportServiceImpl implements DesignImportService {

	@Resource
    private UserSelection userSelection;
	
	@Resource
    private FieldbookService fieldbookService;
	
	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;
	
	@Resource
    private MessageSource messageSource;
	
	@Override
	public List<MeasurementRow> generateDesign(Workbook workbook, DesignImportData designImportData) throws DesignValidationException {
		
		List<ImportedGermplasm> importedGermplasm = userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
		
		Map<Integer, List<String>> csvData = designImportData.getCsvData();
		Map<Integer, StandardVariable> germplasmStandardVariables = convertToStandardVariables(workbook.getGermplasmFactors());
		
		List<MeasurementRow> measurements = new ArrayList<>();
		
		Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = designImportData.getMappedHeaders();
		
		//row counter starts at index = 1 because zero index is the header
		int rowCounter = 1;

		while(rowCounter < csvData.size() - 1){
			MeasurementRow measurementRow = createMeasurementRow(mappedHeaders, csvData.get(rowCounter), importedGermplasm, germplasmStandardVariables);
			measurements.add(measurementRow);
			rowCounter++;
			
		}
		return measurements;
	}
	
	@Override
	public void validateDesignData(DesignImportData designImportData) throws DesignValidationException {
		
		Map<Integer, List<String>> csvData = designImportData.getCsvData();
		
		DesignHeaderItem trialInstanceDesignHeaderItem = validateIfTrialFactorExists(designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));
		DesignHeaderItem entryNoDesignHeaderItem = validateIfEntryNumberExists(designImportData.getMappedHeaders().get(PhenotypicType.GERMPLASM));
		validateIfPlotNumberExists(designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN));
		validateIfPlotNumberIsUnique(designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN), designImportData.getCsvData());
		
		Map<String, Map<Integer, List<String>>> csvMap = groupCsvRowsIntoTrialInstance(trialInstanceDesignHeaderItem, csvData);
		
		validateEntryNoMustBeUniquePerInstance(entryNoDesignHeaderItem, csvMap);
		
	}

	@Override
	public Set<MeasurementVariable> getDesignMeasurementVariables(Workbook workbook, DesignImportData designImportData) {
		
		Set<MeasurementVariable> measurementVariables = new LinkedHashSet<>();
		Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = designImportData.getMappedHeaders();
		
		//Add the trial environments first
		measurementVariables.addAll(this.extractMeasurementVariable(PhenotypicType.TRIAL_ENVIRONMENT, mappedHeaders));
		
		//Add the germplasm factors that exist from csv file header
		measurementVariables.addAll(this.extractMeasurementVariable(PhenotypicType.GERMPLASM, mappedHeaders));
		
		//Add the germplasm factors from the selected germplasm in workbook
		measurementVariables.addAll(workbook.getGermplasmFactors());
		
		//Add the design factors that exists from csv file header
		measurementVariables.addAll(this.extractMeasurementVariable(PhenotypicType.TRIAL_DESIGN, mappedHeaders));
		
		//Add the variates that exist from csv file header
		measurementVariables.addAll(this.extractMeasurementVariable(PhenotypicType.VARIATE, mappedHeaders));
		
		//Add the variates from the added traits in workbook
		measurementVariables.addAll(workbook.getVariates());
		
		
		return measurementVariables;
	}
	
	@Override
	public boolean areTrialInstancesMatchTheSelectedEnvironments(Workbook workbook, DesignImportData designImportData){
		
		DesignHeaderItem trialInstanceDesignHeaderItem = filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR, designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));
		
		if (trialInstanceDesignHeaderItem != null){
			Map<String, Map<Integer, List<String>>> csvMap = groupCsvRowsIntoTrialInstance(trialInstanceDesignHeaderItem, designImportData.getCsvData());
			if (workbook.getTotalNumberOfInstances() == csvMap.size()){
				return true;
			}
		}
	
		return false;
	}
	
	protected DesignHeaderItem validateIfTrialFactorExists(List<DesignHeaderItem> headerDesignItems) throws DesignValidationException {
		DesignHeaderItem headerItem = filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR,headerDesignItems);
		if (headerItem == null){
			throw new DesignValidationException(messageSource.getMessage("design.import.error.trial.is.required", null, Locale.ENGLISH));
		}else{
			return headerItem;
		}
	}
	
	protected DesignHeaderItem validateIfEntryNumberExists(List<DesignHeaderItem> headerDesignItems) throws DesignValidationException {
		DesignHeaderItem headerItem = filterDesignHeaderItemsByTermId(TermId.ENTRY_NO, headerDesignItems);
		if (headerItem == null){
			throw new DesignValidationException(messageSource.getMessage("design.import.error.entry.no.is.required", null, Locale.ENGLISH));
		}else{
			return headerItem;
		}
	}
	
	protected void validateIfPlotNumberExists(List<DesignHeaderItem> headerDesignItems) throws DesignValidationException {
		for (DesignHeaderItem headerDesignItem : headerDesignItems){
			if (headerDesignItem.getVariable().getId() == TermId.PLOT_NO.getId()){
				return;
			}
		}
		throw new DesignValidationException(messageSource.getMessage("design.import.error.plot.no.is.required", null, Locale.ENGLISH));
	}
	
	protected void validateIfPlotNumberIsUnique(List<DesignHeaderItem> headerDesignItems, Map<Integer, List<String>> csvMap) throws DesignValidationException {
		Set<String> set = new HashSet<String>();
		for (DesignHeaderItem headerDesignItem : headerDesignItems){
			if (headerDesignItem.getVariable().getId() == TermId.PLOT_NO.getId()){
				for (Entry<Integer, List<String>> entry : csvMap.entrySet()){
					String value = entry.getValue().get(headerDesignItem.getColumnIndex());
					if (StringUtils.isNullOrEmpty(value) && set.contains(value)){
						throw new DesignValidationException(messageSource.getMessage("design.import.error.plot.number.must.be.unique", null, Locale.ENGLISH));
					}else {
						set.add(value);
					}
				}
			}
		}
	}
	
	protected void validateEntryNoMustBeUniquePerInstance(DesignHeaderItem entryNoHeaderItem ,Map<String, Map<Integer, List<String>>> csvMapGrouped) throws DesignValidationException {
		
		for (Entry<String,Map<Integer, List<String>>> entry : csvMapGrouped.entrySet()){
			validateEntryNumberMustBeUnique(entryNoHeaderItem, entry.getValue());
		}
		
	}
	
	protected void validateEntryNumberMustBeUnique(DesignHeaderItem entryNoHeaderItem, Map<Integer, List<String>> csvMap) throws DesignValidationException {
		Set<String> set = new HashSet<String>();
		
		Iterator<Entry<Integer, List<String>>> iterator = csvMap.entrySet().iterator();
		while(iterator.hasNext()){
			String value = iterator.next().getValue().get(entryNoHeaderItem.getColumnIndex());
			if (StringUtils.isNullOrEmpty(value) && set.contains(value)){
				throw new DesignValidationException(messageSource.getMessage("design.import.error.entry.number.unique.per.instance", null, Locale.ENGLISH));
			}else {
				set.add(value);
			}
		}
		validateGermplasmEntriesFromShouldMatchTheGermplasmList(set);
	}
	
	protected void validateGermplasmEntriesFromShouldMatchTheGermplasmList(Set<String> entryNumbers) throws DesignValidationException {
		
		List<ImportedGermplasm> importedGermplasmList = userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
		for (ImportedGermplasm importedGermplasm : importedGermplasmList){
			if (!entryNumbers.contains(importedGermplasm.getEntryId().toString())){
				throw new DesignValidationException(messageSource.getMessage("design.import.error.mismatch.count.of.germplasm.entries", null, Locale.ENGLISH));
			}
		}
		if (importedGermplasmList.size() != entryNumbers.size()){
			throw new DesignValidationException(messageSource.getMessage("design.import.error.mismatch.count.of.germplasm.entries", null, Locale.ENGLISH));
		}
	}

	
	protected Map<String, Map<Integer, List<String>>> groupCsvRowsIntoTrialInstance(DesignHeaderItem trialInstanceHeaderItem, Map<Integer, List<String>> csvMap){
		
		Map<String, Map<Integer, List<String>>> csvMapGrouped = new HashMap<>();
		
		Iterator<Entry<Integer, List<String>>> iterator = csvMap.entrySet().iterator();
		//skip the header row
		iterator.next();
		while(iterator.hasNext()){
			Entry<Integer, List<String>> entry = iterator.next();
			String trialInstance = entry.getValue().get(trialInstanceHeaderItem.getColumnIndex());
			if (!csvMapGrouped.containsKey(trialInstance)){
				csvMapGrouped.put(trialInstance, new HashMap<Integer, List<String>>());
			}
			csvMapGrouped.get(trialInstance).put(entry.getKey(), entry.getValue());
		}
		return csvMapGrouped;
		
	}
	
	protected DesignHeaderItem filterDesignHeaderItemsByTermId(TermId termId, List<DesignHeaderItem> headerDesignItems){
		for (DesignHeaderItem headerDesignItem : headerDesignItems){
			if (headerDesignItem.getVariable().getId() == termId.getId()){
				return headerDesignItem;
			}
		}
		return null;
	}
	
	protected Set<MeasurementVariable> extractMeasurementVariable(PhenotypicType phenotypicType, Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders){
		
		Set<MeasurementVariable> measurementVariables = new HashSet<>();
		
		for (DesignHeaderItem designHeaderItem : mappedHeaders.get(phenotypicType)){
			MeasurementVariable measurementVariable = createMeasurementVariable(designHeaderItem.getVariable());
			measurementVariables.add(measurementVariable);
		}
		
		return measurementVariables;
	}
	
	protected MeasurementRow createMeasurementRow(Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders, List<String> rowValues, List<ImportedGermplasm> importedGermplasm, Map<Integer, StandardVariable> germplasmStandardVariables){
		MeasurementRow measurement = new MeasurementRow();

		List<MeasurementData> dataList = new ArrayList<>();
		
		for (Entry<PhenotypicType, List<DesignHeaderItem>> entry : mappedHeaders.entrySet()){
			for (DesignHeaderItem headerItem : entry.getValue()){
				
				if (headerItem.getVariable().getId() == TermId.ENTRY_NO.getId()){
					
					Integer entryNo = Integer.parseInt(rowValues.get(headerItem.getColumnIndex()));
					
					addGermplasmDetailsToDataList(importedGermplasm, germplasmStandardVariables,
							dataList, entryNo);
					
				}else{
					String value = rowValues.get(headerItem.getColumnIndex());
					dataList.add(createMeasurementData(headerItem.getVariable(), value));
				}
				
			}
		}
		
		measurement.setDataList(dataList);
		return measurement;
	}
	
	protected MeasurementData createMeasurementData(StandardVariable standardVariable, String value){ 
		MeasurementData data = new MeasurementData();
		data.setMeasurementVariable(createMeasurementVariable(standardVariable));
		data.setValue(value);
		return data;
	}
	
	protected MeasurementVariable createMeasurementVariable(StandardVariable standardVariable){
		MeasurementVariable variable = ExpDesignUtil.convertStandardVariableToMeasurementVariable(standardVariable, Operation.ADD, fieldbookService);
		return variable;
	}
	
	protected Map<Integer, StandardVariable> convertToStandardVariables(List<MeasurementVariable> list) {
			
		Map<Integer, StandardVariable> map = new HashMap<>();

		for (MeasurementVariable measurementVariable : list){
			try {
				map.put(measurementVariable.getTermId(), fieldbookMiddlewareService.getStandardVariable(measurementVariable.getTermId()));
			} catch (MiddlewareQueryException e) {
				//do nothing
			}
		}

		return map;
	}
	
	protected void addGermplasmDetailsToDataList(List<ImportedGermplasm> importedGermplasm,
			Map<Integer, StandardVariable> germplasmStandardVariables,
			List<MeasurementData> dataList, Integer entryNo) {
		
		ImportedGermplasm germplasmEntry = importedGermplasm.get(entryNo-1);

		if (germplasmStandardVariables.get(TermId.ENTRY_NO.getId()) != null){
			dataList.add(createMeasurementData(germplasmStandardVariables.get(TermId.ENTRY_NO.getId()), germplasmEntry.getEntryId().toString()));
		}
		if (germplasmStandardVariables.get(TermId.GID.getId()) != null){
			dataList.add(createMeasurementData(germplasmStandardVariables.get(TermId.GID.getId()), germplasmEntry.getGid()));
		}
		if (germplasmStandardVariables.get(TermId.DESIG.getId()) != null){
			dataList.add(createMeasurementData(germplasmStandardVariables.get(TermId.DESIG.getId()), germplasmEntry.getDesig()));
		}
		if (germplasmStandardVariables.get(TermId.ENTRY_TYPE.getId()) != null){
			dataList.add(createMeasurementData(germplasmStandardVariables.get(TermId.ENTRY_TYPE.getId()), germplasmEntry.getCheck()));
		}
		if (germplasmStandardVariables.get(TermId.CROSS.getId()) != null){
			dataList.add(createMeasurementData(germplasmStandardVariables.get(TermId.CROSS.getId()), germplasmEntry.getCross()));
		}
		if (germplasmStandardVariables.get(TermId.ENTRY_CODE.getId()) != null){
			dataList.add(createMeasurementData(germplasmStandardVariables.get(TermId.ENTRY_CODE.getId()), germplasmEntry.getEntryCode()));
		}
		if (germplasmStandardVariables.get(TermId.SOURCE.getId()) != null){
			dataList.add(createMeasurementData(germplasmStandardVariables.get(TermId.SOURCE.getId()), germplasmEntry.getSource()));
		}
		if (germplasmStandardVariables.get(TermId.SEED_SOURCE.getId()) != null){
			dataList.add(createMeasurementData(germplasmStandardVariables.get(TermId.SEED_SOURCE.getId()), germplasmEntry.getSource()));
		}
	}
}
