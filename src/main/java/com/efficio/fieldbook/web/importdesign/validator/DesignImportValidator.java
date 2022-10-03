package com.efficio.fieldbook.web.importdesign.validator;

import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.importdesign.service.DesignImportService;
import com.efficio.fieldbook.web.study.germplasm.StudyEntryTransformer;
import com.mysql.jdbc.StringUtils;
import org.generationcp.middleware.ruleengine.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.springframework.context.MessageSource;

import javax.annotation.Resource;
import java.util.*;
import java.util.Map.Entry;

public class DesignImportValidator {

	@Resource
	private UserSelection userSelection;

	@Resource
	private StudyEntryService studyEntryService;

	@Resource
	private StudyEntryTransformer studyEntryTransformer;

	@Resource
	private MessageSource messageSource;

	@Resource
	private OntologyScaleDataManager ontologyScaleDataManager;

	@Resource
	private DesignImportService designImportService;

	public void validateDesignData(final DesignImportData designImportData) throws DesignValidationException {

		final Map<Integer, List<String>> csvData = designImportData.getRowDataMap();

		final Map<PhenotypicType, Map<Integer, DesignHeaderItem>> mappedHeadersWithDesignHeaderItemsMappedToStdVarId =
			designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId();

		final DesignHeaderItem trialInstanceDesignHeaderItem = this.designImportService
			.validateIfStandardVariableExists(mappedHeadersWithDesignHeaderItemsMappedToStdVarId.get(PhenotypicType.TRIAL_ENVIRONMENT),
				"design.import.error.trial.is.required", TermId.TRIAL_INSTANCE_FACTOR);
		final DesignHeaderItem entryNoDesignHeaderItem = this.designImportService
			.validateIfStandardVariableExists(mappedHeadersWithDesignHeaderItemsMappedToStdVarId.get(PhenotypicType.ENTRY_DETAIL),
				"design.import.error.entry.no.is.required", TermId.ENTRY_NO);
		this.designImportService
			.validateIfStandardVariableExists(mappedHeadersWithDesignHeaderItemsMappedToStdVarId.get(PhenotypicType.TRIAL_DESIGN),
				"design.import.error.plot.no.is.required", TermId.PLOT_NO);

		this.validateEntryNumbers(entryNoDesignHeaderItem, csvData);

		final Map<String, Map<Integer, List<String>>> csvMap =
			this.designImportService.groupCsvRowsIntoTrialInstance(trialInstanceDesignHeaderItem, csvData);
		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = designImportData.getMappedHeaders();
		this.validateIfPlotNumberIsUniquePerInstance(mappedHeaders.get(PhenotypicType.TRIAL_DESIGN), csvMap);
		this.validateColumnValues(designImportData.getRowDataMap(), mappedHeaders);
	}

	/**
	 * Validation to check that entry numbers from design file must match the entries in germplasm list in study.
	 *
	 * @param entryNoHeaderItem
	 * @param csvData
	 * @throws DesignValidationException
	 */
	protected void validateEntryNumbers(
		final DesignHeaderItem entryNoHeaderItem,
		final Map<Integer, List<String>> csvData) throws DesignValidationException {

		// Extract entry numbers from design file.
		final Set<String> entryNumberFromCsvData = new HashSet<>();
		final Iterator<Entry<Integer, List<String>>> iterator = csvData.entrySet().iterator();
		// Skip the first row which is the header.
		iterator.next();
		while (iterator.hasNext()) {
			final String value = iterator.next().getValue().get(entryNoHeaderItem.getColumnIndex());
			entryNumberFromCsvData.add(value);
		}

		final List<StudyEntryDto> studyEntries = this.studyEntryService.getStudyEntries(this.userSelection.getWorkbook().getStudyDetails().getId());
		final List<ImportedGermplasm> germplasmList = this.studyEntryTransformer.tranformToImportedGermplasm(studyEntries);
		// Extract the entry numbers from germplasmList.
		final Set<String> entryNumbersFromGermplasmList = new HashSet<>();
		for (final ImportedGermplasm importedGermplasm : germplasmList) {
			entryNumbersFromGermplasmList.add(String.valueOf(importedGermplasm.getEntryNumber()));
		}

		// If not all entry numbers from design file match the entries in germplasm list in study, throw an error.
		if (!entryNumbersFromGermplasmList.containsAll(entryNumberFromCsvData)) {
			throw new DesignValidationException(
				this.messageSource.getMessage("design.import.error.mismatch.germplasm.entries", null, Locale.ENGLISH));
		}

	}

	/**
	 * In Trial Manager, a study could have at least 1 trial instance. And each trial instance has specific defined set of observation rows.
	 * This method make sure for each set of observations rows per trial instance, there is a unique set of PLOT_NO.
	 *
	 * @param headerDesignItems
	 * @param csvMap
	 * @throws DesignValidationException
	 */
	protected void validateIfPlotNumberIsUniquePerInstance(
		final List<DesignHeaderItem> headerDesignItems,
		final Map<String, Map<Integer, List<String>>> csvMap) throws DesignValidationException {

		for (final DesignHeaderItem headerDesignItem : headerDesignItems) {
			if (headerDesignItem.getVariable().getId() == TermId.PLOT_NO.getId()) {
				for (final Entry<String, Map<Integer, List<String>>> entry : csvMap.entrySet()) {
					this.validatePlotNumberMustBeUnique(headerDesignItem, entry.getValue());
				}
			}
		}
	}

	/**
	 * Returns an exception when there is a non-unique PLOT_NO value from the given group of imported design rows
	 *
	 * @param plotNoHeaderItem
	 * @param csvMap
	 * @throws DesignValidationException
	 */
	protected void validatePlotNumberMustBeUnique(final DesignHeaderItem plotNoHeaderItem, final Map<Integer, List<String>> csvMap)
		throws DesignValidationException {

		final Set<String> set = new HashSet<String>();

		final Iterator<Entry<Integer, List<String>>> iterator = csvMap.entrySet().iterator();
		while (iterator.hasNext()) {
			final String value = iterator.next().getValue().get(plotNoHeaderItem.getColumnIndex());

			if (!StringUtils.isNullOrEmpty(value)) {
				if (set.contains(value)) {
					throw new DesignValidationException(
						this.messageSource.getMessage("design.import.error.plot.number.must.be.unique", null, Locale.ENGLISH));
				} else {
					set.add(value);
				}
			}
		}

	}

	void validateColumnValues(final Map<Integer, List<String>> csvData, final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders)
		throws DesignValidationException {
		// validate values on columns with categorical variables
		final List<DesignHeaderItem> categoricalDesignHeaderItems =
			this.retrieveDesignHeaderItemsBasedOnDataType(mappedHeaders, TermId.CATEGORICAL_VARIABLE.getId());
		this.validateValuesPerColumn(categoricalDesignHeaderItems, csvData, TermId.CATEGORICAL_VARIABLE.getId());

		// validate values on columns with numeric variables
		final List<DesignHeaderItem> numericDesignHeaderItems =
			this.retrieveDesignHeaderItemsBasedOnDataType(mappedHeaders, TermId.NUMERIC_VARIABLE.getId());
		this.validateValuesPerColumn(numericDesignHeaderItems, csvData, TermId.NUMERIC_VARIABLE.getId());
	}

	/****
	 * Retrieve the list of mappedHeaders according to their data type
	 *
	 * @param mappedHeaders
	 * @param variableDataType (Numeric, Categorical, Character and Date)
	 * @return
	 */
	List<DesignHeaderItem> retrieveDesignHeaderItemsBasedOnDataType(
		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders,
		final int variableDataType) {

		final List<DesignHeaderItem> designHeaderItems = new ArrayList<DesignHeaderItem>();

		for (final List<DesignHeaderItem> mappedHeader : mappedHeaders.values()) {
			for (final DesignHeaderItem header : mappedHeader) {
				final StandardVariable standardVariable = header.getVariable();
				final Term dataType = standardVariable.getDataType();
				if (variableDataType == dataType.getId()) {
					designHeaderItems.add(header);
				}
			}
		}

		return designHeaderItems;
	}

	private void validateValuesPerColumn(
		final List<DesignHeaderItem> designHeaderItems, final Map<Integer, List<String>> csvData,
		final int variableDataType) throws DesignValidationException {

		// remove the header rows
		final Map<Integer, List<String>> csvRowData = new LinkedHashMap<Integer, List<String>>(csvData);
		csvRowData.remove(0);

		for (final DesignHeaderItem headerItem : designHeaderItems) {
			final StandardVariable standardVariable = headerItem.getVariable();
			final Integer columnIndex = headerItem.getColumnIndex();
			if (variableDataType == TermId.CATEGORICAL_VARIABLE.getId()) {
				this.validateValuesForCategoricalVariables(csvRowData, columnIndex, standardVariable);
			} else if (variableDataType == TermId.NUMERIC_VARIABLE.getId()) {
				this.validateValuesForNumericalVariables(csvRowData, columnIndex, standardVariable);
			}
		}
	}

	private void validateValuesForNumericalVariables(
		final Map<Integer, List<String>> csvRowData, final Integer columnIndex,
		final StandardVariable standardVariable) throws DesignValidationException {

		final Scale numericScale = this.ontologyScaleDataManager.getScaleById(standardVariable.getScale().getId(), false);

		for (final Map.Entry<Integer, List<String>> row : csvRowData.entrySet()) {
			final List<String> columnValues = row.getValue();
			final String valueToValidate = columnValues.get(columnIndex);

			// skip validation if the value is null or empty
			if (valueToValidate == null || valueToValidate.length() == 0) {
				return;
			}

			if (!NumericVariableValidator.isValidNumericValueForNumericVariable(valueToValidate, standardVariable, numericScale)) {
				throw new DesignValidationException(
					(this.messageSource.getMessage("design.import.error.invalid.value", null, Locale.ENGLISH))
						.replace("{0}", standardVariable.getName()));
			}
		}
	}

	/**
	 * This method will throw an error when the user tries to use a categorical variable with no possible values, or if at least one column
	 * value is not part of the possible values of the given categorical variable
	 *
	 * @param csvRowData
	 * @param columnIndex
	 * @param standardVariable
	 * @throws DesignValidationException
	 */
	void validateValuesForCategoricalVariables(
		final Map<Integer, List<String>> csvRowData, final Integer columnIndex,
		final StandardVariable standardVariable) throws DesignValidationException {
		for (final Map.Entry<Integer, List<String>> row : csvRowData.entrySet()) {
			final List<String> columnValues = row.getValue();
			final String valueToValidate = columnValues.get(columnIndex);

			// skip validation if the value is null or empty
			if (valueToValidate == null || valueToValidate.length() == 0) {
				return;
			}

			// categorical variables are expected to have possible values, otherwise this will cause data error
			if (!standardVariable.hasEnumerations()) {
				throw new DesignValidationException(
					(this.messageSource.getMessage("design.import.error.no.valid.values", null, Locale.ENGLISH))
						.replace("{0}", standardVariable.getName()));
			}

			// make sure that the column value is part of the possible values of the given categorical variable
			if (!CategoricalVariableValidator.isPartOfValidValuesForCategoricalVariable(valueToValidate, standardVariable)) {
				throw new DesignValidationException(
					(this.messageSource.getMessage("design.import.error.invalid.value", null, Locale.ENGLISH))
						.replace("{0}", standardVariable.getName()));
			}
		}
	}

}
