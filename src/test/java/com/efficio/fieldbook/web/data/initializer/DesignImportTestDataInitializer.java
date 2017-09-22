
package com.efficio.fieldbook.web.data.initializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;

import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.trial.bean.Environment;
import com.efficio.fieldbook.web.trial.bean.EnvironmentData;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import com.google.common.collect.Lists;

public class DesignImportTestDataInitializer {

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
		designImportData.setRowDataMap(createTestCsvDataForDesignImportData());

		return designImportData;

	}

	public static Map<PhenotypicType, List<DesignHeaderItem>> createTestMappedHeadersForDesignImportData() {

		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = new HashMap<>();

		final List<DesignHeaderItem> trialEvironmentItems = new ArrayList<>();
		trialEvironmentItems.add(createDesignHeaderItem(PhenotypicType.TRIAL_ENVIRONMENT, TermId.TRIAL_INSTANCE_FACTOR.getId(),
				"TRIAL_INSTANCE", 0, NUMERIC_VARIABLE));

		DesignHeaderItem siteNameDesignHeaderItem = createDesignHeaderItem(PhenotypicType.TRIAL_ENVIRONMENT, TermId.SITE_NAME.getId(), "SITE_NAME", 1,
				CHARACTER_VARIABLE);
		siteNameDesignHeaderItem.setName("SITE_NAME_LOCAL_NAME");
		trialEvironmentItems.add(siteNameDesignHeaderItem);

		final List<DesignHeaderItem> germplasmItems = new ArrayList<>();
		germplasmItems.add(createDesignHeaderItem(PhenotypicType.GERMPLASM, TermId.ENTRY_NO.getId(), "ENTRY_NO", 2, NUMERIC_VARIABLE));

		final List<DesignHeaderItem> trialDesignItems = new ArrayList<>();
		trialDesignItems.add(createDesignHeaderItem(PhenotypicType.TRIAL_DESIGN, TermId.PLOT_NO.getId(), "PLOT_NO", 3, NUMERIC_VARIABLE));
		trialDesignItems.add(createDesignHeaderItem(PhenotypicType.TRIAL_DESIGN, TermId.REP_NO.getId(), "REP_NO", 4, NUMERIC_VARIABLE));
		trialDesignItems.add(createDesignHeaderItem(PhenotypicType.TRIAL_DESIGN, TermId.BLOCK_NO.getId(), "BLOCK_NO", 5, NUMERIC_VARIABLE));

		final List<DesignHeaderItem> variateItems = new ArrayList<>();
		variateItems.add(createDesignHeaderItem(PhenotypicType.VARIATE, AFLAVER_5_ID, "AflavER_1_5", 6, CATEGORICAL_VARIABLE));

		mappedHeaders.put(PhenotypicType.TRIAL_ENVIRONMENT, trialEvironmentItems);
		mappedHeaders.put(PhenotypicType.GERMPLASM, germplasmItems);
		mappedHeaders.put(PhenotypicType.TRIAL_DESIGN, trialDesignItems);
		mappedHeaders.put(PhenotypicType.VARIATE, variateItems);

		return mappedHeaders;

	}

	public static Map<Integer, List<String>> createTestCsvDataForDesignImportData() {

		final Map<Integer, List<String>> csvData = new HashMap<>();

		// The first row is the header
		csvData.put(0, Lists.newArrayList("TRIAL_INSTANCE", "SITE_NAME", "ENTRY_NO", "PLOT_NO", "REP_NO", "BLOCK_NO", "AflavER_1_5"));

		// csv data
		csvData.put(1, Lists.newArrayList("1", "Laguna", "1", "1", "1", "1", ""));
		csvData.put(2, Lists.newArrayList("1", "Laguna", "2", "2", "1", "1", ""));
		csvData.put(3, Lists.newArrayList("2", "Bicol", "1", "6", "1", "", ""));
		csvData.put(4, Lists.newArrayList("2", "Bicol", "2", "7", "1", "1", ""));
		csvData.put(5, Lists.newArrayList("3", "Bulacan", "1", "11", "1", "2", ""));
		csvData.put(6, Lists.newArrayList("3", "Bulacan", "2", "12", "1", "2", ""));

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

	public static Map<Integer, StandardVariable> getStandardVariables(final PhenotypicType phenotypicType,
			final List<MeasurementVariable> germplasmFactors) {
		final Map<Integer, StandardVariable> standardVariables = new HashMap<>();

		for (final MeasurementVariable measurementVar : germplasmFactors) {

			if (phenotypicType.getLabelList().contains(measurementVar.getLabel())) {
				final StandardVariable stdVar = convertToStandardVariable(measurementVar);
				standardVariables.put(stdVar.getId(), stdVar);
			}

		}

		return standardVariables;
	}

	public static StandardVariable convertToStandardVariable(final MeasurementVariable measurementVar) {
		final StandardVariable stdVar = new StandardVariable();
		stdVar.setId(measurementVar.getTermId());
		stdVar.setProperty(new Term(0, measurementVar.getProperty(), ""));
		stdVar.setScale(new Term(0, measurementVar.getScale(), ""));
		stdVar.setMethod(new Term(0, measurementVar.getMethod(), ""));
		stdVar.setDataType(new Term(measurementVar.getDataTypeId(), measurementVar.getDataType(), ""));
		stdVar.setPhenotypicType(PhenotypicType.getPhenotypicTypeForLabel(measurementVar.getLabel()));
		return stdVar;
	}

	public static EnvironmentData createEnvironmentData(final int numberOfIntances) {
		final EnvironmentData environmentData = new EnvironmentData();
		final List<Environment> environments = new ArrayList<>();

		for (int x = 0; x < numberOfIntances; x++) {
			final Environment env = new Environment();
			env.setLocationId(x);
			environments.add(env);
		}

		environmentData.setEnvironments(environments);
		environmentData.setNoOfEnvironments(numberOfIntances);
		return environmentData;
	}

	public static void processEnvironmentData(final EnvironmentData data) {
		for (int i = 0; i < data.getEnvironments().size(); i++) {
			final Map<String, String> values = data.getEnvironments().get(i).getManagementDetailValues();
			if (!values.containsKey(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()))) {
				values.put(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()), Integer.toString(i + 1));
			} else if (values.get(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId())) == null
					|| values.get(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId())).isEmpty()) {
				values.put(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()), Integer.toString(i + 1));
			}
		}
	}

	public static void updatePlotNoValue(final List<MeasurementRow> observations) {
		// alter the data first to make sure the PLOT_NO and ENTRY_NO value is not the same
		int plotNoId = observations.size();
		int entryNoId = 1;
		for (final MeasurementRow row : observations) {
			final List<MeasurementData> dataList = row.getDataList();
			final MeasurementData entryNoData = WorkbookUtil.retrieveMeasurementDataFromMeasurementRow(TermId.ENTRY_NO.getId(), dataList);
			final MeasurementData plotNoData = WorkbookUtil.retrieveMeasurementDataFromMeasurementRow(TermId.PLOT_NO.getId(), dataList);
			entryNoData.setValue(String.valueOf(entryNoId));
			plotNoData.setValue(String.valueOf(plotNoId));
			plotNoId--;
			entryNoId++;
		}
	}
}
