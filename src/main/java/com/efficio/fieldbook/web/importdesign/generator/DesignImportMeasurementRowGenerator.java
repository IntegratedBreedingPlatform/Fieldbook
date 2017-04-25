
package com.efficio.fieldbook.web.importdesign.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

public class DesignImportMeasurementRowGenerator {

	private static final Logger LOG = LoggerFactory.getLogger(DesignImportMeasurementRowGenerator.class);

	private Workbook workbook;
	private Map<PhenotypicType, Map<Integer, DesignHeaderItem>> mappedHeaders;
	private List<String> rowValues;
	private List<ImportedGermplasm> importedGermplasm;
	private Map<Integer, StandardVariable> germplasmStandardVariables;
	private Set<String> trialInstancesFromUI;
	private boolean isPreview;
	private Map<String, Integer> availableCheckTypes;

	private FieldbookService fieldbookService;

	public DesignImportMeasurementRowGenerator() {
		super();
	}

	public DesignImportMeasurementRowGenerator(final FieldbookService fieldbookService, final Workbook workbook,
			final Map<PhenotypicType, Map<Integer, DesignHeaderItem>> mappedHeadersWithStdVarId,
			final List<ImportedGermplasm> importedGermplasm, final Map<Integer, StandardVariable> germplasmStandardVariables,
			final Set<String> trialInstancesFromUI, final boolean isPreview, final Map<String, Integer> availableCheckTypes) {
		super();
		this.fieldbookService = fieldbookService;
		this.workbook = workbook;
		this.mappedHeaders = mappedHeadersWithStdVarId;
		this.importedGermplasm = importedGermplasm;
		this.germplasmStandardVariables = germplasmStandardVariables;
		this.trialInstancesFromUI = trialInstancesFromUI;
		this.isPreview = isPreview;
		this.availableCheckTypes = availableCheckTypes;
	}

	/**
	 * Create measurement row based on the values per row from the csv file.
	 * 
	 * @param rowValues
	 * @return returns measurement row based on the values per row from the csv file filtered from the number of trial instances in UI. If
	 *         the trial instance value from the row is not included from the trial instances from the UI, this method will only returns
	 *         empty class with empty data list.
	 */
	public MeasurementRow createMeasurementRow(final List<String> rowValues) {
		LOG.debug("Design Import - Creating Measurement Row");

		final MeasurementRow measurement = new MeasurementRow();

		final Map<Integer, DesignHeaderItem> trialEnvironmentHeaders = this.mappedHeaders.get(PhenotypicType.TRIAL_ENVIRONMENT);

		final List<MeasurementData> dataList = new ArrayList<MeasurementData>();
		measurement.setDataList(dataList);

		// only add record from file if the trial instance value within the row is selected/included in environment tab
		if (this.trialInstancesFromUI.contains(rowValues.get(trialEnvironmentHeaders.get(TermId.TRIAL_INSTANCE_FACTOR.getId())
				.getColumnIndex()))) {
			dataList.addAll(this.createMeasurementRowDataList(rowValues));
		}

		LOG.debug("Design Import - Creating Data List");

		return measurement;
	}

	private List<MeasurementData> createMeasurementRowDataList(final List<String> rowValues) {

		final List<MeasurementData> dataList = new ArrayList<>();

		this.addTrialEnvironmentVariablesToDataList(rowValues, dataList);
		LOG.debug("Added Environment Variables to MeasurementDataList : size=" + dataList.size());

		this.addGermplasmVariablesToDataList(rowValues, dataList);
		LOG.debug("Added Germplasm Variables to MeasurementDataList : size=" + dataList.size());

		this.addTrialDesignAndVariatesToDataList(rowValues, dataList);
		LOG.debug("Added TrialDesign and Variates to MeasurementDataList : size=" + dataList.size());

		return dataList;
	}

	private void addTrialDesignAndVariatesToDataList(final List<String> rowValues, final List<MeasurementData> dataList) {

		final Map<Integer, DesignHeaderItem> trialDesignHeaders = this.mappedHeaders.get(PhenotypicType.TRIAL_DESIGN);
		final Map<Integer, DesignHeaderItem> variateHeaders = this.mappedHeaders.get(PhenotypicType.VARIATE);
		final List<DesignHeaderItem> remainingColumnHeaders = new ArrayList<DesignHeaderItem>();
		remainingColumnHeaders.addAll(trialDesignHeaders.values());
		remainingColumnHeaders.addAll(variateHeaders.values());

		for (final DesignHeaderItem headerItem : remainingColumnHeaders) {
			final String value = rowValues.get(headerItem.getColumnIndex());
			dataList.add(this.createMeasurementData(headerItem.getVariable(), value));
		}

	}

	private void addTrialEnvironmentVariablesToDataList(final List<String> rowValues, final List<MeasurementData> dataList) {
		final Map<Integer, DesignHeaderItem> trialEnvironmentHeaders = this.mappedHeaders.get(PhenotypicType.TRIAL_ENVIRONMENT);
		for (final Map.Entry<Integer, DesignHeaderItem> trialEnvironmentHeader : trialEnvironmentHeaders.entrySet()) {
			final DesignHeaderItem headerItem = trialEnvironmentHeader.getValue();

			// add trial instance factor
			if (headerItem.getVariable().getId() == TermId.TRIAL_INSTANCE_FACTOR.getId()
					&& this.workbook.getStudyDetails().getStudyType() == StudyType.N) {
				// do not add the trial instance to measurement data list if the workbook is Nursery
				LOG.debug("Study Type is Nursery - did not add Trial Instance Factor");
				continue;
			} else if (headerItem.getVariable().getId() == TermId.TRIAL_INSTANCE_FACTOR.getId()
					&& this.workbook.getStudyDetails().getStudyType() == StudyType.T) {
				LOG.debug("Study Type is Trial - adding Trial Instance Factor");
				final String value = rowValues.get(headerItem.getColumnIndex());
				dataList.add(this.createMeasurementData(headerItem.getVariable(), value));
			}

			// add the remaining trial environment factors here. i.e SITE NAME
			if (this.isPreview && headerItem.getVariable().getId() != TermId.TRIAL_INSTANCE_FACTOR.getId()) {
				// only add the trial environment factors in measurement row ONLY in PREVIEW mode
				final String value = rowValues.get(headerItem.getColumnIndex());
				dataList.add(this.createMeasurementData(headerItem.getVariable(), value));
			}
		}
	}

	private void addGermplasmVariablesToDataList(final List<String> rowValues, final List<MeasurementData> dataList) {

		final Map<Integer, DesignHeaderItem> germplasmHeaders = this.mappedHeaders.get(PhenotypicType.GERMPLASM);

		// ENTRY_TYPE or CHECK
		boolean hasEntryTypeColumnFromTheImport = false;
		final DesignHeaderItem entryTypeHeaderItem = germplasmHeaders.get(TermId.ENTRY_TYPE.getId());
		if (entryTypeHeaderItem != null && this.germplasmStandardVariables.get(TermId.ENTRY_TYPE.getId()) != null) {
			final String checkType = String.valueOf(rowValues.get(entryTypeHeaderItem.getColumnIndex()));
			final String checkTypeId = String.valueOf(this.availableCheckTypes.get(checkType));
			dataList.add(this.createMeasurementData(this.germplasmStandardVariables.get(TermId.ENTRY_TYPE.getId()), checkTypeId));
			hasEntryTypeColumnFromTheImport = true;
		}

		// ENTRY_NO
		final DesignHeaderItem entryNoHeaderItem = germplasmHeaders.get(TermId.ENTRY_NO.getId());
		if (entryNoHeaderItem != null) {
			final Integer entryNo = Integer.parseInt(rowValues.get(entryNoHeaderItem.getColumnIndex()));
			this.addGermplasmDetailsToDataList(this.importedGermplasm, this.germplasmStandardVariables, dataList, entryNo,
					hasEntryTypeColumnFromTheImport);
		}

	}

	protected void addGermplasmDetailsToDataList(final List<ImportedGermplasm> importedGermplasm,
			final Map<Integer, StandardVariable> germplasmStandardVariables, final List<MeasurementData> dataList, final Integer entryNo,
			final boolean hasEntryTypeColumnFromTheImport) {

		final ImportedGermplasm germplasmEntry = importedGermplasm.get(entryNo - 1);

		if (germplasmStandardVariables.get(TermId.ENTRY_NO.getId()) != null) {
			dataList.add(this.createMeasurementData(germplasmStandardVariables.get(TermId.ENTRY_NO.getId()), germplasmEntry.getEntryId()
					.toString()));
		}
		if (germplasmStandardVariables.get(TermId.GID.getId()) != null) {
			dataList.add(this.createMeasurementData(germplasmStandardVariables.get(TermId.GID.getId()), germplasmEntry.getGid()));
		}
		if (germplasmStandardVariables.get(TermId.DESIG.getId()) != null) {
			dataList.add(this.createMeasurementData(germplasmStandardVariables.get(TermId.DESIG.getId()), germplasmEntry.getDesig()));
		}
		if (germplasmStandardVariables.get(TermId.CROSS.getId()) != null) {
			dataList.add(this.createMeasurementData(germplasmStandardVariables.get(TermId.CROSS.getId()), germplasmEntry.getCross()));
		}
		if (germplasmStandardVariables.get(TermId.ENTRY_CODE.getId()) != null) {
			dataList.add(this.createMeasurementData(germplasmStandardVariables.get(TermId.ENTRY_CODE.getId()),
					germplasmEntry.getEntryCode()));
		}
		if (germplasmStandardVariables.get(TermId.ENTRY_TYPE.getId()) != null && !hasEntryTypeColumnFromTheImport) {
			dataList.add(this.createMeasurementData(germplasmStandardVariables.get(TermId.ENTRY_TYPE.getId()), germplasmEntry.getEntryTypeValue()));
		}
		if (germplasmStandardVariables.get(TermId.GERMPLASM_SOURCE.getId()) != null) {
			dataList.add(this.createMeasurementData(germplasmStandardVariables.get(TermId.GERMPLASM_SOURCE.getId()),
					germplasmEntry.getSource()));
		}
		if (germplasmStandardVariables.get(TermId.SEED_SOURCE.getId()) != null) {
			dataList.add(this.createMeasurementData(germplasmStandardVariables.get(TermId.SEED_SOURCE.getId()), germplasmEntry.getSource()));
		}
		if (germplasmStandardVariables.get(TermId.PLOT_ID.getId()) != null) {
			// This will initially create blank values for PLOT_ID but the generation of plot IDs will be handled during the saving of Workbook.
			dataList.add(this.createMeasurementData(germplasmStandardVariables.get(TermId.PLOT_ID.getId()), ""));
		}
	}

	protected MeasurementData createMeasurementData(final StandardVariable standardVariable, final String value) {
		final MeasurementData data = new MeasurementData();
		data.setMeasurementVariable(this.createMeasurementVariable(standardVariable));
		data.setValue(value);
		data.setLabel(data.getMeasurementVariable().getName());
		data.setDataType(data.getMeasurementVariable().getDataType());
		return data;
	}

	protected MeasurementData createMeasurementData(final MeasurementVariable measurementVariable, final String value) {
		final MeasurementData data = new MeasurementData();
		data.setMeasurementVariable(measurementVariable);
		data.setValue(value);
		data.setLabel(data.getMeasurementVariable().getName());
		data.setDataType(data.getMeasurementVariable().getDataType());
		return data;
	}

	protected MeasurementVariable createMeasurementVariable(final StandardVariable standardVariable) {
		return ExpDesignUtil.convertStandardVariableToMeasurementVariable(standardVariable, Operation.ADD, this.fieldbookService);
	}

	public void addFactorsToMeasurementRows(final List<MeasurementRow> measurements) {

		if (this.workbook.getStudyDetails().getStudyType() == StudyType.N) {
			for (final MeasurementVariable factor : this.workbook.getFactors()) {
				for (final MeasurementRow row : measurements) {
					this.addFactorToDataListIfNecessary(factor, row.getDataList());
				}

			}
		}
	}

	protected void addFactorToDataListIfNecessary(final MeasurementVariable factor, final List<MeasurementData> dataList) {
		for (final MeasurementData data : dataList) {
			if (data.getMeasurementVariable().equals(factor)) {
				return;
			}
		}
		dataList.add(this.createMeasurementData(factor, ""));
	}

	public void addVariatesToMeasurementRows(final List<MeasurementRow> measurements, final UserSelection userSelection,
			final OntologyService ontologyService, final ContextUtil contextUtil) {

		final Set<MeasurementVariable> temporaryList = new HashSet<>();
		for (final MeasurementVariable mvar : this.workbook.getVariates()) {
			if (mvar.getOperation() == Operation.ADD || mvar.getOperation() == Operation.UPDATE) {
				final MeasurementVariable copy = mvar.copy();
				temporaryList.add(copy);
			}
		}

		WorkbookUtil.addMeasurementDataToRowsIfNecessary(new ArrayList<MeasurementVariable>(temporaryList), measurements, true,
				userSelection, ontologyService, this.fieldbookService, contextUtil.getCurrentProgramUUID());

	}

	public Workbook getWorkbook() {
		return this.workbook;
	}

	public void setWorkbook(final Workbook workbook) {
		this.workbook = workbook;
	}

	public Map<PhenotypicType, Map<Integer, DesignHeaderItem>> getMappedHeaders() {
		return this.mappedHeaders;
	}

	public void setMappedHeaders(final Map<PhenotypicType, Map<Integer, DesignHeaderItem>> mappedHeaders) {
		this.mappedHeaders = mappedHeaders;
	}

	public List<String> getRowValues() {
		return this.rowValues;
	}

	public void setRowValues(final List<String> rowValues) {
		this.rowValues = rowValues;
	}

	public List<ImportedGermplasm> getImportedGermplasm() {
		return this.importedGermplasm;
	}

	public void setImportedGermplasm(final List<ImportedGermplasm> importedGermplasm) {
		this.importedGermplasm = importedGermplasm;
	}

	public Map<Integer, StandardVariable> getGermplasmStandardVariables() {
		return this.germplasmStandardVariables;
	}

	public void setGermplasmStandardVariables(final Map<Integer, StandardVariable> germplasmStandardVariables) {
		this.germplasmStandardVariables = germplasmStandardVariables;
	}

	public Set<String> getTrialInstancesFromUI() {
		return this.trialInstancesFromUI;
	}

	public void setTrialInstancesFromUI(final Set<String> trialInstancesFromUI) {
		this.trialInstancesFromUI = trialInstancesFromUI;
	}

	public boolean isPreview() {
		return this.isPreview;
	}

	public void setPreview(final boolean isPreview) {
		this.isPreview = isPreview;
	}

	public Map<String, Integer> getAvailableCheckTypes() {
		return this.availableCheckTypes;
	}

	public void setAvailableCheckTypes(final Map<String, Integer> availableCheckTypes) {
		this.availableCheckTypes = availableCheckTypes;
	}

}
