
package com.efficio.fieldbook.web.data.initializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;

import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;

public class DesignImportDataInitializer {

	public static final int NO_OF_CHARACTER_VARIABLES = 1;
	public static final int NO_OF_CATEGORICAL_VARIABLES = 1;
	public static final int NO_OF_NUMERIC_VARIABLES = 5;

	public static final int CATEGORICAL_VARIABLE = 1130;
	public static final int CHARACTER_VARIABLE = 1120;
	public static final int NUMERIC_VARIABLE = 1110;
	public static final int NO_OF_TEST_ENTRIES = 2;

	public static final int AFLAVER_5_ID = 51510;

	public static DesignImportData createDesignImportData() {

		final DesignImportData designImportData = new DesignImportData();

		designImportData.setMappedHeaders(createTestMappedHeadersForDesignImportData());
		designImportData.setCsvData(createTestCsvDataForDesignImportData());

		return designImportData;

	}

	public static Map<PhenotypicType, List<DesignHeaderItem>> createTestMappedHeadersForDesignImportData() {

		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = new HashMap<>();

		final List<DesignHeaderItem> trialEvironmentItems = new ArrayList<>();
		trialEvironmentItems.add(createDesignHeaderItem(PhenotypicType.TRIAL_ENVIRONMENT, TermId.TRIAL_INSTANCE_FACTOR.getId(),
				"TRIAL_INSTANCE", 0, NUMERIC_VARIABLE));
		trialEvironmentItems.add(createDesignHeaderItem(PhenotypicType.TRIAL_ENVIRONMENT, TermId.SITE_NAME.getId(), "SITE_NAME", 1,
				CHARACTER_VARIABLE));

		final List<DesignHeaderItem> germplasmItems = new ArrayList<>();
		germplasmItems.add(createDesignHeaderItem(PhenotypicType.GERMPLASM, TermId.ENTRY_NO.getId(), "ENTRY_NO", 2, NUMERIC_VARIABLE));

		final List<DesignHeaderItem> trialDesignItems = new ArrayList<>();
		trialDesignItems.add(createDesignHeaderItem(PhenotypicType.TRIAL_DESIGN, TermId.PLOT_NO.getId(), "PLOT_NO", 3, NUMERIC_VARIABLE));
		trialDesignItems.add(createDesignHeaderItem(PhenotypicType.TRIAL_DESIGN, TermId.REP_NO.getId(), "REP_NO", 4, NUMERIC_VARIABLE));
		trialDesignItems.add(createDesignHeaderItem(PhenotypicType.TRIAL_DESIGN, TermId.BLOCK_NO.getId(), "BLOCK_NO", 5, NUMERIC_VARIABLE));

		final List<DesignHeaderItem> variateItems = new ArrayList<>();
		trialDesignItems.add(createDesignHeaderItem(PhenotypicType.VARIATE, AFLAVER_5_ID, "AflavER_1_5", 6, CATEGORICAL_VARIABLE));

		mappedHeaders.put(PhenotypicType.TRIAL_ENVIRONMENT, trialEvironmentItems);
		mappedHeaders.put(PhenotypicType.GERMPLASM, germplasmItems);
		mappedHeaders.put(PhenotypicType.TRIAL_DESIGN, trialDesignItems);
		mappedHeaders.put(PhenotypicType.VARIATE, variateItems);

		return mappedHeaders;

	}

	public static Map<Integer, List<String>> createTestCsvDataForDesignImportData() {

		final Map<Integer, List<String>> csvData = new HashMap<>();

		// The first row is the header
		csvData.put(0, createListOfString("TRIAL_INSTANCE", "SITE_NAME", "ENTRY_NO", "PLOT_NO", "REP_NO", "BLOCK_NO", "AflavER_1_5"));

		// csv data
		csvData.put(1, createListOfString("1", "Laguna", "1", "1", "1", "1", "1"));
		csvData.put(2, createListOfString("1", "Laguna", "2", "2", "1", "1", "2"));
		csvData.put(3, createListOfString("2", "Bicol", "1", "6", "1", "1", "3"));
		csvData.put(4, createListOfString("2", "Bicol", "2", "7", "1", "1", "2"));
		csvData.put(5, createListOfString("3", "Bulacan", "1", "11", "1", "2", "3"));
		csvData.put(6, createListOfString("3", "Bulacan", "2", "12", "1", "2", "1"));

		return csvData;

	}

	public static DesignHeaderItem createDesignHeaderItem(final PhenotypicType phenotypicType, final int termId, final String headerName,
			final int columnIndex, final int dataTypeId) {
		final DesignHeaderItem designHeaderItem = createDesignHeaderItem(termId, headerName, columnIndex);
		designHeaderItem.setVariable(createStandardVariable(phenotypicType, termId, headerName, "", "", "", dataTypeId, "", "", ""));
		return designHeaderItem;
	}

	public static DesignHeaderItem createDesignHeaderItem(final int termId, final String headerName, final int columnIndex) {
		final DesignHeaderItem designHeaderItem = new DesignHeaderItem();
		designHeaderItem.setId(termId);
		designHeaderItem.setName(headerName);
		designHeaderItem.setColumnIndex(columnIndex);
		return designHeaderItem;
	}

	public static StandardVariable createStandardVariable(final PhenotypicType phenotypicType, final int id, final String name,
			final String property, final String scale, final String method, final int dataTypeId, final String dataType,
			final String storedIn, final String isA) {

		final StandardVariable stdVar =
				new StandardVariable(new Term(0, property, ""), new Term(0, scale, ""), new Term(0, method, ""), new Term(dataTypeId,
						dataType, ""), new Term(0, isA, ""), phenotypicType);

		stdVar.setId(id);
		stdVar.setName(name);
		stdVar.setDescription("");

		if (dataTypeId == CATEGORICAL_VARIABLE) {
			stdVar.setEnumerations(createPossibleValues(5));
		}

		return stdVar;
	}

	public static StandardVariable createStandardVariable(final VariableType variableType, final int id, final String name,
			final String property, final String scale, final String method, final String dataType, final String storedIn, final String isA) {

		final StandardVariable stdVar =
				new StandardVariable(new Term(0, property, ""), new Term(0, scale, ""), new Term(0, method, ""), new Term(0, dataType, ""),
						new Term(0, isA, ""), null);

		stdVar.setId(id);
		stdVar.setName(name);
		stdVar.setDescription("");

		final Set<VariableType> variableTypes = new HashSet<>();
		variableTypes.add(variableType);

		stdVar.setVariableTypes(variableTypes);

		return stdVar;
	}

	public static List<Enumeration> createPossibleValues(final int noOfPossibleValues) {
		final List<Enumeration> possibleValues = new ArrayList<Enumeration>();
		for (int i = 0; i < noOfPossibleValues; i++) {
			final Enumeration possibleValue = new Enumeration();
			final int id = i + 1;
			possibleValue.setId(id);
			possibleValue.setName(String.valueOf(id));
			possibleValue.setDescription("Possible Value: " + id);

			possibleValues.add(possibleValue);

		}
		return possibleValues;
	}

	public static List<String> createListOfString(final String... listData) {
		final List<String> list = new ArrayList<>();
		for (final String data : listData) {
			list.add(data);
		}
		return list;
	}

	public static DesignHeaderItem filterDesignHeaderItemsByTermId(final TermId termId, final List<DesignHeaderItem> headerDesignItems) {
		for (final DesignHeaderItem headerDesignItem : headerDesignItems) {
			if (headerDesignItem.getVariable().getId() == termId.getId()) {
				return headerDesignItem;
			}
		}
		return null;
	}

	public static Map<String, Map<Integer, List<String>>> groupCsvRowsIntoTrialInstance(final DesignHeaderItem trialInstanceHeaderItem,
			final Map<Integer, List<String>> csvMap) {

		final Map<String, Map<Integer, List<String>>> csvMapGrouped = new HashMap<>();

		final Iterator<Entry<Integer, List<String>>> iterator = csvMap.entrySet().iterator();
		// skip the header row
		iterator.next();
		while (iterator.hasNext()) {
			final Entry<Integer, List<String>> entry = iterator.next();
			final String trialInstance = entry.getValue().get(trialInstanceHeaderItem.getColumnIndex());
			if (!csvMapGrouped.containsKey(trialInstance)) {
				csvMapGrouped.put(trialInstance, new HashMap<Integer, List<String>>());
			}
			csvMapGrouped.get(trialInstance).put(entry.getKey(), entry.getValue());
		}
		return csvMapGrouped;

	}

}
