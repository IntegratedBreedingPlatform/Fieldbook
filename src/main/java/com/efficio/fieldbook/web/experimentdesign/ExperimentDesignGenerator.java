package com.efficio.fieldbook.web.experimentdesign;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.trial.bean.BVDesignOutput;
import com.efficio.fieldbook.web.trial.bean.xml.ExpDesign;
import com.efficio.fieldbook.web.trial.bean.xml.ExpDesignParameter;
import com.efficio.fieldbook.web.trial.bean.xml.ListItem;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import com.google.common.base.Optional;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

@Component
public class ExperimentDesignGenerator {

	public static final String NCLATIN_PARAM = "nclatin";
	public static final String NRLATIN_PARAM = "nrlatin";
	public static final String REPLATINGROUPS_PARAM = "replatingroups";
	public static final String COLUMNFACTOR_PARAM = "columnfactor";
	public static final String ROWFACTOR_PARAM = "rowfactor";
	public static final String NCOLUMNS_PARAM = "ncolumns";
	public static final String NROWS_PARAM = "nrows";
	public static final String NBLATIN_PARAM = "nblatin";
	public static final String REPLICATEFACTOR_PARAM = "replicatefactor";
	public static final String TREATMENTFACTOR_PARAM = "treatmentfactor";
	public static final String INITIAL_TREATMENT_NUMBER_PARAM = "initialtreatnum";
	public static final String NREPLICATES_PARAM = "nreplicates";
	public static final String NTREATMENTS_PARAM = "ntreatments";
	public static final String BLOCKSIZE_PARAM = "blocksize";
	public static final String TIMELIMIT_PARAM = "timelimit";
	public static final String LEVELS_PARAM = "levels";
	public static final String TREATMENTFACTORS_PARAM = "treatmentfactors";
	public static final String PLOTFACTOR_PARAM = "plotfactor";
	public static final String INITIAL_PLOT_NUMBER_PARAM = "initialplotnum";
	public static final String BLOCKFACTOR_PARAM = "blockfactor";
	public static final String NBLOCKS_PARAM = "nblocks";
	public static final String OUTPUTFILE_PARAM = "outputfile";
	public static final String SEED_PARAM = "seed";
	public static final String NCONTROLS_PARAM = "ncontrols";

	public static String RANDOMIZED_COMPLETE_BLOCK_DESIGN = "RandomizedBlock";
	public static String RESOLVABLE_INCOMPLETE_BLOCK_DESIGN = "ResolvableIncompleteBlock";
	public static String RESOLVABLE_ROW_COL_DESIGN = "ResolvableRowColumn";
	public static String AUGMENTED_RANDOMIZED_BLOCK_DESIGN = "Augmented";

	private static final Logger LOG = LoggerFactory.getLogger(ExperimentDesignGenerator.class);

	@Resource
	private WorkbenchService workbenchService;

	@Resource
	private FieldbookProperties fieldbookProperties;

	@Resource
	private FieldbookService fieldbookService;

	public MainDesign createRandomizedCompleteBlockDesign(String nBlock, String blockFactor, String plotFactor, Integer initialPlotNumber,
			Integer initialEntryNumber, List<String> treatmentFactor, List<String> levels, String outputfile) {

		String timeLimit = AppConstants.EXP_DESIGN_TIME_LIMIT.getString();

		List<ExpDesignParameter> paramList = new ArrayList<>();
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.SEED_PARAM, "", null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.NBLOCKS_PARAM, nBlock, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.BLOCKFACTOR_PARAM, blockFactor, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.PLOTFACTOR_PARAM, plotFactor, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM, getPlotNumberStringValueOrDefault(initialPlotNumber), null));

		addInitialTreatmenNumberIfAvailable(initialEntryNumber, paramList);

		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.TREATMENTFACTORS_PARAM, null, convertToListItemList(treatmentFactor)));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.LEVELS_PARAM, null, convertToListItemList(levels)));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.TIMELIMIT_PARAM, timeLimit, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.OUTPUTFILE_PARAM, outputfile, null));

		ExpDesign design = new ExpDesign(ExperimentDesignGenerator.RANDOMIZED_COMPLETE_BLOCK_DESIGN, paramList);

		return new MainDesign(design);
	}

	public MainDesign createResolvableIncompleteBlockDesign(String blockSize, String nTreatments, String nReplicates,
			String treatmentFactor, String replicateFactor, String blockFactor, String plotFactor, Integer initialPlotNumber,
			Integer initialEntryNumber, String nBlatin, String replatingGroups, String outputfile, boolean useLatinize) {

		String timeLimit = AppConstants.EXP_DESIGN_TIME_LIMIT.getString();

		List<ExpDesignParameter> paramList = new ArrayList<ExpDesignParameter>();
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.SEED_PARAM, "", null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.BLOCKSIZE_PARAM, blockSize, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.NTREATMENTS_PARAM, nTreatments, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.NREPLICATES_PARAM, nReplicates, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.TREATMENTFACTOR_PARAM, treatmentFactor, null));

		addInitialTreatmenNumberIfAvailable(initialEntryNumber, paramList);

		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.REPLICATEFACTOR_PARAM, replicateFactor, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.BLOCKFACTOR_PARAM, blockFactor, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.PLOTFACTOR_PARAM, plotFactor, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM, getPlotNumberStringValueOrDefault(initialPlotNumber), null));

		addLatinizeParametersForResolvableIncompleteBlockDesign(useLatinize, paramList, nBlatin ,replatingGroups);

		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.TIMELIMIT_PARAM, timeLimit, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.OUTPUTFILE_PARAM, outputfile, null));

		ExpDesign design = new ExpDesign(ExperimentDesignGenerator.RESOLVABLE_INCOMPLETE_BLOCK_DESIGN, paramList);

		return new MainDesign(design);
	}

	public MainDesign createResolvableRowColDesign(String nTreatments, String nReplicates, String nRows, String nColumns,
			String treatmentFactor, String replicateFactor, String rowFactor, String columnFactor, String plotFactor, Integer initialPlotNumber,
			Integer initialEntryNumber, String nrLatin, String ncLatin, String replatingGroups, String outputfile, Boolean useLatinize) {

		String timeLimit = AppConstants.EXP_DESIGN_TIME_LIMIT.getString();

		String plotNumberStrValue = (initialPlotNumber == null) ? "1" : String.valueOf(initialPlotNumber);

		List<ExpDesignParameter> paramList = new ArrayList<ExpDesignParameter>();
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.SEED_PARAM, "", null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.NTREATMENTS_PARAM, nTreatments, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.NREPLICATES_PARAM, nReplicates, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.NROWS_PARAM, nRows, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.NCOLUMNS_PARAM, nColumns, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.TREATMENTFACTOR_PARAM, treatmentFactor, null));

		addInitialTreatmenNumberIfAvailable(initialEntryNumber, paramList);

		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.REPLICATEFACTOR_PARAM, replicateFactor, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.ROWFACTOR_PARAM, rowFactor, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.COLUMNFACTOR_PARAM, columnFactor, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.PLOTFACTOR_PARAM, plotFactor, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM, plotNumberStrValue, null));

		addLatinizeParametersForResolvableRowAndColumnDesign(useLatinize, paramList ,replatingGroups, nrLatin, ncLatin);

		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.TIMELIMIT_PARAM, timeLimit, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.OUTPUTFILE_PARAM, outputfile, null));

		ExpDesign design = new ExpDesign(ExperimentDesignGenerator.RESOLVABLE_ROW_COL_DESIGN, paramList);

		return new MainDesign(design);
	}

	public MainDesign createAugmentedRandomizedBlockDesign(final String nblks, final String ntreatments, final String ncontrols,
			final String treatmentFactor, final String blockFactor, final String plotFactor, final String plotNo) {

		List<ExpDesignParameter> paramList = new ArrayList<>();

		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.NTREATMENTS_PARAM, ntreatments, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.NCONTROLS_PARAM, ncontrols, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.NBLOCKS_PARAM, nblks, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.TREATMENTFACTOR_PARAM, treatmentFactor, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.BLOCKFACTOR_PARAM, blockFactor, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.PLOTFACTOR_PARAM, plotFactor, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM, plotNo, null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.SEED_PARAM, "", null));
		paramList.add(createExpDesignParameter(ExperimentDesignGenerator.OUTPUTFILE_PARAM, "", null));

		ExpDesign design = new ExpDesign(ExperimentDesignGenerator.AUGMENTED_RANDOMIZED_BLOCK_DESIGN, paramList);

		return new MainDesign(design);
	}

	public List<MeasurementRow> generateExperimentDesignMeasurements(int environments, int environmentsToAdd,
			List<MeasurementVariable> trialVariables, List<MeasurementVariable> factors, List<MeasurementVariable> nonTrialFactors,
			List<MeasurementVariable> variates, List<TreatmentVariable> treatmentVariables,
			List<StandardVariable> requiredExpDesignVariable, List<ImportedGermplasm> germplasmList, MainDesign mainDesign,
			String entryNumberIdentifier,
			Map<String, List<String>> treatmentFactorValues, Map<Integer, Integer> mapOfChecks) throws
			BVDesignException {

		//Converting germplasm List to map
		Map<Integer, ImportedGermplasm> importedGermplasmMap = new HashMap<>();
		for(ImportedGermplasm ig : germplasmList){
			importedGermplasmMap.put(ig.getEntryId(), ig);
		}

		List<MeasurementRow> measurementRowList = new ArrayList<MeasurementRow>();
		List<MeasurementVariable> varList = new ArrayList<MeasurementVariable>();
		varList.addAll(nonTrialFactors);
		for (StandardVariable var : requiredExpDesignVariable) {
			if (WorkbookUtil.getMeasurementVariable(nonTrialFactors, var.getId()) == null) {
				MeasurementVariable measureVar =
						ExpDesignUtil.convertStandardVariableToMeasurementVariable(var, Operation.ADD, fieldbookService);
				measureVar.setRole(PhenotypicType.TRIAL_DESIGN);
				varList.add(measureVar);
				if (WorkbookUtil.getMeasurementVariable(factors, var.getId()) == null) {
					factors.add(measureVar);
				}
			}
		}

		if (treatmentVariables != null) {
			for (int i = 0; i < treatmentVariables.size(); i++) {
				varList.add(treatmentVariables.get(i).getLevelVariable());
				varList.add(treatmentVariables.get(i).getValueVariable());
				if (WorkbookUtil.getMeasurementVariable(factors, treatmentVariables.get(i).getLevelVariable().getTermId()) == null) {
					factors.add(treatmentVariables.get(i).getLevelVariable());
				}
				if (WorkbookUtil.getMeasurementVariable(factors, treatmentVariables.get(i).getValueVariable().getTermId()) == null) {
					factors.add(treatmentVariables.get(i).getValueVariable());
				}
			}
		}
		for (MeasurementVariable var : varList) {
			var.setFactor(true);
		}

		varList.addAll(variates);

		int trialInstanceStart = environments - environmentsToAdd + 1;
		for (int trialNo = trialInstanceStart; trialNo <= environments; trialNo++) {

			BVDesignOutput bvOutput = null;
			try {
				bvOutput = fieldbookService.runBVDesign(workbenchService, fieldbookProperties, mainDesign);
			} catch (Exception e) {
				ExperimentDesignGenerator.LOG.error(e.getMessage(), e);
				throw new BVDesignException("experiment.design.bv.exe.error.generate.generic.error");
			}

			if (bvOutput == null || !bvOutput.isSuccess()) {
				throw new BVDesignException("experiment.design.generate.generic.error");
			}

			for (int counter = 0; counter < bvOutput.getBvResultList().size(); counter++) {
				String entryNoValue = bvOutput.getEntryValue(entryNumberIdentifier, counter);
				final Integer entryNumber = StringUtil.parseInt(entryNoValue, null);
				if(entryNumber == null){
					throw new BVDesignException("experiment.design.bv.exe.error.output.invalid.error");
				}
				Optional<ImportedGermplasm> importedGermplasm =
						findImportedGermplasmByEntryNumberAndChecks(importedGermplasmMap, entryNumber, mapOfChecks);

				if (!importedGermplasm.isPresent()) {
					throw new BVDesignException("experiment.design.bv.exe.error.output.invalid.error");
				}
				MeasurementRow row =
						this.createMeasurementRow(varList, importedGermplasm.get(), bvOutput.getEntryMap(counter),
								treatmentFactorValues, trialVariables, trialNo);
				measurementRowList.add(row);
			}
		}
		return measurementRowList;
	}

	ExpDesignParameter createExpDesignParameter(String name, String value, List<ListItem> items) {

		ExpDesignParameter designParam = new ExpDesignParameter(name, value);
		if (items != null && !items.isEmpty()) {
			designParam.setListItem(items);
		}
		return designParam;
	}

	Optional<ImportedGermplasm> findImportedGermplasmByEntryNumberAndChecks(
			Map<Integer, ImportedGermplasm> importedGermplasmMap, Integer entryNumber, Map<Integer, Integer> mapOfChecks) {

		if (importedGermplasmMap.containsKey(entryNumber)) {
			return Optional.of(importedGermplasmMap.get(entryNumber));
		} else {
			// If the entryNumber does not exist in importedGermplasmMap, then it is an entryNumber generated by the design engine.
			return findImportedGermplasmByEntryNumberGeneratedByDesignEngine(importedGermplasmMap, entryNumber, mapOfChecks);
		}

	}

	Optional<ImportedGermplasm> findImportedGermplasmByEntryNumberGeneratedByDesignEngine(
			Map<Integer, ImportedGermplasm> importedGermplasmMap, Integer entryNumber, Map<Integer, Integer> mapOfChecks) {

		Optional<Integer> resolvedEntryNumber = resolveMappedEntryNumber(importedGermplasmMap, entryNumber, mapOfChecks);
		if (resolvedEntryNumber.isPresent()) {
			return Optional.of(importedGermplasmMap.get(resolvedEntryNumber.get()));
		}
		return Optional.absent();

	}

	/**
	 * Returns the original check entry no mapped to the last entries of a germplasm list.
	 *
	 * <pre>
	 * Given a trial has a total of 6 entries and 2 of them are check entries (Entry no. 1 and 2).
	 * But the design engine assumes that the two check entries are at the end of the germplasm list. As
	 * a workaround, we will sequentially map the last two entries to the check entries in the list. The
	 * map will be as follows:
	 *
	 * (Last Entry No) : (Original Check Entry No)
	 * 4 : 1
	 * 5 : 2
	 *
	 * After the design is generated, the output file will have the following design:
	 *
	 * Plot_No	Block_No	Entry_No
	 * 1		1			7
	 * 2		1			6
	 * 3		1			4
	 * 4		1			6
	 * 5		1			7
	 * 6		1			3
	 * 7		1			5
	 *
	 * Notice that there are extra 6 and 7 entry numbers generated by design engine to indicate the inserted
	 * checks. We will sequentially map the extra entry numbers to the last two entries:
	 *
	 * 6 : 4
	 * 7 : 5
	 *
	 * In order to get the original check entry numbers we will use the following logic:
	 *
	 * (Generated Entry no) - (No of checks) = (Entry no from last two entries)
	 *
	 * 6 - 2 = 4
	 * If 4 : 1, then 1 is the original check entry no of 6.
	 *
	 * </pre>
	 *
	 * @param importedGermplasmMap
	 * @param entryNumberGeneratedFromDesignEngine
	 * @param mapOfChecks
	 * @return
	 *
	 */
	 Optional<Integer> resolveMappedEntryNumber(Map<Integer, ImportedGermplasm> importedGermplasmMap,
			Integer entryNumberGeneratedFromDesignEngine, Map<Integer, Integer> mapOfChecks) {

		Integer lastEntryNo = entryNumberGeneratedFromDesignEngine - mapOfChecks.size();

		if (mapOfChecks.containsKey(lastEntryNo)) {
			return Optional.of(mapOfChecks.get(lastEntryNo));
		} else {
			return Optional.absent();
		}

	}

	MeasurementRow createMeasurementRow(List<MeasurementVariable> headerVariable, ImportedGermplasm germplasm,
			Map<String, String> bvEntryMap, Map<String, List<String>> treatmentFactorValues, List<MeasurementVariable> trialVariables,
			int trialNo) {
		MeasurementRow measurementRow = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<MeasurementData>();
		MeasurementData treatmentLevelData = null;
		MeasurementData measurementData = null;

		MeasurementVariable trialInstanceVar = WorkbookUtil.getMeasurementVariable(trialVariables, TermId.TRIAL_INSTANCE_FACTOR.getId());
		measurementData =
				new MeasurementData(trialInstanceVar.getName(), Integer.toString(trialNo), false, trialInstanceVar.getDataType(),
						trialInstanceVar);
		dataList.add(measurementData);

		for (MeasurementVariable var : headerVariable) {

			measurementData = null;

			Integer termId = var.getTermId();

			if (termId.intValue() == TermId.ENTRY_NO.getId()) {
				measurementData = new MeasurementData(var.getName(), String.valueOf(germplasm.getEntryId()), false, var.getDataType(), var);
			} else if (termId.intValue() == TermId.SOURCE.getId() || termId.intValue() == TermId.GERMPLASM_SOURCE.getId()) {
				measurementData =
						new MeasurementData(var.getName(), germplasm.getSource() != null ? germplasm.getSource() : "", false,
								var.getDataType(), var);
			} else if (termId.intValue() == TermId.CROSS.getId()) {
				measurementData = new MeasurementData(var.getName(), germplasm.getCross(), false, var.getDataType(), var);
			} else if (termId.intValue() == TermId.DESIG.getId()) {
				measurementData = new MeasurementData(var.getName(), germplasm.getDesig(), false, var.getDataType(), var);
			} else if (termId.intValue() == TermId.GID.getId()) {
				measurementData = new MeasurementData(var.getName(), germplasm.getGid(), false, var.getDataType(), var);
			} else if (termId.intValue() == TermId.ENTRY_CODE.getId()) {
				measurementData = new MeasurementData(var.getName(), germplasm.getEntryCode(), false, var.getDataType(), var);
			} else if (termId.intValue() == TermId.PLOT_NO.getId()) {
				measurementData = new MeasurementData(var.getName(), bvEntryMap.get(var.getName()), false, var.getDataType(), var);
			} else if (termId.intValue() == TermId.CHECK.getId()) {
				measurementData =
						new MeasurementData(var.getName(), Integer.toString(germplasm.getEntryTypeCategoricalID()), false, var.getDataType(),
								germplasm.getEntryTypeCategoricalID(), var);

			} else if (termId.intValue() == TermId.REP_NO.getId()) {
				measurementData = new MeasurementData(var.getName(), bvEntryMap.get(var.getName()), false, var.getDataType(), var);

			} else if (termId.intValue() == TermId.BLOCK_NO.getId()) {
				measurementData = new MeasurementData(var.getName(), bvEntryMap.get(var.getName()), false, var.getDataType(), var);

			} else if (termId.intValue() == TermId.ROW.getId()) {
				measurementData = new MeasurementData(var.getName(), bvEntryMap.get(var.getName()), false, var.getDataType(), var);

			} else if (termId.intValue() == TermId.COL.getId()) {
				measurementData = new MeasurementData(var.getName(), bvEntryMap.get(var.getName()), false, var.getDataType(), var);

			} else if (termId.intValue() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
				measurementData = new MeasurementData(var.getName(), Integer.toString(trialNo), false, var.getDataType(), var);

			} else if (var.getTreatmentLabel() != null && !"".equals(var.getTreatmentLabel())) {
				if (treatmentLevelData == null) {
					measurementData =
							new MeasurementData(var.getName(), bvEntryMap.get(ExpDesignUtil.cleanBVDesingKey(Integer.toString(var
									.getTermId()))), false, var.getDataType(), var);
					treatmentLevelData = measurementData;
				} else {
					String level = treatmentLevelData.getValue();
					if (NumberUtils.isNumber(level)) {
						int index = Integer.valueOf(level) - 1;
						if (treatmentFactorValues != null
								&& treatmentFactorValues.containsKey(String
								.valueOf(treatmentLevelData.getMeasurementVariable().getTermId()))) {
							Object tempObj =
									treatmentFactorValues.get(String.valueOf(treatmentLevelData.getMeasurementVariable().getTermId())).get(
											index);
							String value = "";
							if (tempObj != null) {
								if (tempObj instanceof String) {
									value = (String) tempObj;
								} else {
									value = Integer.toString((Integer) tempObj);
								}
							}
							if (var.getDataTypeId() != null && var.getDataTypeId().intValue() == TermId.DATE_VARIABLE.getId()) {
								value = DateUtil.convertToDBDateFormat(var.getDataTypeId(), value);
								measurementData = new MeasurementData(var.getName(), value, false, var.getDataType(), var);
							} else if (var.getPossibleValues() != null && !var.getPossibleValues().isEmpty() && NumberUtils.isNumber(value)) {
								measurementData =
										new MeasurementData(var.getName(), value, false, var.getDataType(), Integer.parseInt(value), var);
							} else {
								measurementData = new MeasurementData(var.getName(), value, false, var.getDataType(), var);
							}
						}
					}
					treatmentLevelData = null;
				}

			} else {
				// meaning non factor
				measurementData = new MeasurementData(var.getName(), "", true, var.getDataType(), var);
			}

			dataList.add(measurementData);
		}
		measurementRow.setDataList(dataList);
		return measurementRow;
	}

	String getPlotNumberStringValueOrDefault(Integer initialPlotNumber) {
		return (initialPlotNumber == null) ? "1" : String.valueOf(initialPlotNumber);
	}


	void addInitialTreatmenNumberIfAvailable(final Integer initialEntryNumber, final List<ExpDesignParameter> paramList) {

		if(initialEntryNumber != null){
			paramList.add(createExpDesignParameter(ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM, String.valueOf(initialEntryNumber), null));
		}

	}

	void addLatinizeParametersForResolvableIncompleteBlockDesign(final boolean useLatinize, final List<ExpDesignParameter> paramList, final String nBlatin,
			final String replatingGroups) {

		if (useLatinize) {
			paramList.add(createExpDesignParameter(ExperimentDesignGenerator.NBLATIN_PARAM, nBlatin, null));
			// we add the string tokenize replating groups
			// we tokenize the replating groups
			StringTokenizer tokenizer = new StringTokenizer(replatingGroups, ",");
			List<ListItem> replatingList = new ArrayList<ListItem>();
			while (tokenizer.hasMoreTokens()) {
				replatingList.add(new ListItem(tokenizer.nextToken()));
			}
			paramList.add(createExpDesignParameter(ExperimentDesignGenerator.REPLATINGROUPS_PARAM, null, replatingList));
		} else {
			paramList.add(createExpDesignParameter(ExperimentDesignGenerator.NBLATIN_PARAM, "0", null));
		}

	}

	void addLatinizeParametersForResolvableRowAndColumnDesign(final Boolean useLatinize, final List<ExpDesignParameter> paramList,
			final String replatingGroups, final String nrLatin, final String ncLatin) {

		if (useLatinize != null && useLatinize.booleanValue()) {
			paramList.add(createExpDesignParameter(ExperimentDesignGenerator.NRLATIN_PARAM, nrLatin, null));
			paramList.add(createExpDesignParameter(ExperimentDesignGenerator.NCLATIN_PARAM, ncLatin, null));
			// we tokenize the replating groups
			StringTokenizer tokenizer = new StringTokenizer(replatingGroups, ",");
			List<ListItem> replatingList = new ArrayList<ListItem>();
			while (tokenizer.hasMoreTokens()) {
				replatingList.add(new ListItem(tokenizer.nextToken()));
			}
			paramList.add(createExpDesignParameter(ExperimentDesignGenerator.REPLATINGROUPS_PARAM, null, replatingList));
		} else {
			paramList.add(createExpDesignParameter(ExperimentDesignGenerator.NRLATIN_PARAM, "0", null));
			paramList.add(createExpDesignParameter(ExperimentDesignGenerator.NCLATIN_PARAM, "0", null));
		}

	}

	List<ListItem> convertToListItemList(List<String> listString) {

		List<ListItem> listItemList = new ArrayList<ListItem>();
		for (String value : listString) {
			ListItem listItem = new ListItem(value);
			listItemList.add(listItem);
		}
		return listItemList;

	}

}
