
package com.efficio.fieldbook.web.naming.impl;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.common.bean.AdvanceResult;
import com.efficio.fieldbook.web.naming.service.NamingConventionService;
import com.efficio.fieldbook.web.trial.bean.AdvancingStudy;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.parsing.pojo.ImportedCross;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.pojo.AdvanceGermplasmChangeDetail;
import org.generationcp.commons.pojo.AdvancingSource;
import org.generationcp.commons.pojo.AdvancingSourceList;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.ruleengine.RuleExecutionContext;
import org.generationcp.commons.ruleengine.RuleFactory;
import org.generationcp.commons.ruleengine.RulesNotConfiguredException;
import org.generationcp.commons.ruleengine.generator.SeedSourceGenerator;
import org.generationcp.commons.ruleengine.naming.rules.EnforceUniqueNameRule;
import org.generationcp.commons.ruleengine.naming.rules.NamingRuleExecutionContext;
import org.generationcp.commons.ruleengine.naming.service.ProcessCodeService;
import org.generationcp.commons.ruleengine.service.RulesService;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.util.TimerWatch;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.generationcp.middleware.service.api.dataset.ObservationUnitUtils.fromMeasurementRow;

@Service
@Transactional
public class NamingConventionServiceImpl implements NamingConventionService {

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private RulesService rulesService;

	@Resource
	private GermplasmDataManager germplasmDataManager;

	@Resource
	private AdvancingSourceListFactory advancingSourceListFactory;

	@Resource
	private ProcessCodeService processCodeService;

	@Resource
	private RuleFactory ruleFactory;

	@Resource
	private ResourceBundleMessageSource messageSource;

	@Resource
	private SeedSourceGenerator seedSourceGenerator;

	@Resource
	private StudyDataManager studyDataManager;

	@Resource
	private DatasetService datasetService;

	@Override
	public AdvanceResult advanceStudy(final AdvancingStudy info, final Workbook workbook) throws RuleException, FieldbookException {

		final Map<Integer, Method> breedingMethodMap = new HashMap<>();
		final Map<String, Method> breedingMethodCodeMap = new HashMap<>();
		final List<Method> methodList = this.fieldbookMiddlewareService.getAllBreedingMethods(false);

		for (final Method method : methodList) {
			breedingMethodMap.put(method.getMid(), method);
			breedingMethodCodeMap.put(method.getMcode(), method);
		}

		final AdvancingSourceList list = this.createAdvancingSourceList(info, workbook, breedingMethodMap, breedingMethodCodeMap);
		this.updatePlantsSelectedIfNecessary(list, info);
		final List<ImportedGermplasm> importedGermplasmList = this.generateGermplasmList(list, info, workbook);

		final List<AdvanceGermplasmChangeDetail> changeDetails = new ArrayList<>();
		for (final AdvancingSource source : list.getRows()) {
			if (source.getChangeDetail() != null) {
				changeDetails.add(source.getChangeDetail());
			}
		}

		final AdvanceResult result = new AdvanceResult();
		result.setAdvanceList(importedGermplasmList);
		result.setChangeDetails(changeDetails);

		return result;
	}

	private AdvancingSourceList createAdvancingSourceList(final AdvancingStudy advanceInfo, Workbook workbook,
		final Map<Integer, Method> breedingMethodMap, final Map<String, Method> breedingMethodCodeMap) throws FieldbookException {

		final Study study = advanceInfo.getStudy();
		if (workbook == null) {
			workbook = this.fieldbookMiddlewareService.getStudyDataSet(study.getId());
		}
		return this.advancingSourceListFactory
			.createAdvancingSourceList(workbook, advanceInfo, study, breedingMethodMap, breedingMethodCodeMap);
	}

	private void updatePlantsSelectedIfNecessary(final AdvancingSourceList list, final AdvancingStudy info) {
		boolean lineChoiceSame = info.getLineChoice() != null && "1".equals(info.getLineChoice());
		final boolean allPlotsChoice = info.getAllPlotsChoice() != null && "1".equals(info.getAllPlotsChoice());
		int plantsSelected = 0;
		if (info.getLineSelected() != null && NumberUtils.isNumber(info.getLineSelected())) {
			plantsSelected = Integer.valueOf(info.getLineSelected());
		} else {
			lineChoiceSame = false;
		}
		if (list != null && list.getRows() != null && !list.getRows().isEmpty() && (lineChoiceSame && plantsSelected > 0
			|| allPlotsChoice)) {
			for (final AdvancingSource row : list.getRows()) {
				if (!row.isBulk() && lineChoiceSame) {
					row.setPlantsSelected(plantsSelected);
				} else if (row.isBulk() && allPlotsChoice) {
					// set it to 1, it does not matter since it's bulked
					row.setPlantsSelected(1);
				}
			}
		}
	}

	private void assignGermplasmAttributes(final ImportedGermplasm germplasm, final Integer sourceGid, final Integer sourceGnpgs,
		final Integer sourceGpid1, final Integer sourceGpid2, final Method sourceMethod, final Method breedingMethod) {

		if ((sourceMethod != null && sourceMethod.getMtype() != null
			&& AppConstants.METHOD_TYPE_GEN.getString().equals(sourceMethod.getMtype())) || sourceGnpgs < 0 &&
			(sourceGpid1 != null && sourceGpid1.equals(0)) && (sourceGpid2 != null && sourceGpid2.equals(0))) {

			germplasm.setGpid1(sourceGid);
		} else {
			germplasm.setGpid1(sourceGpid1);
		}

		germplasm.setGpid2(sourceGid);

		if (breedingMethod != null) {
			germplasm.setGnpgs(breedingMethod.getMprgn());
		}
	}

	protected void addImportedGermplasmToList(final List<ImportedGermplasm> list, final AdvancingSource source,
		final String newGermplasmName, final Method breedingMethod, final int index, final Workbook workbook, final int selectionNumber,
		final AdvancingStudy advancingParameters, final String plantNo, final Map<String, String> locationIdNameMap,
		final List<MeasurementVariable> environmentVariables) {

		String selectionNumberToApply = null;
		final boolean allPlotsSelected = "1".equals(advancingParameters.getAllPlotsChoice());
		if (source.isBulk()) {
			if (allPlotsSelected) {
				selectionNumberToApply = null;
			} else {
				selectionNumberToApply = String.valueOf(source.getPlantsSelected());
			}
		} else {
			selectionNumberToApply = String.valueOf(selectionNumber);
		}

		// set the seed source string for the new Germplasm
		final String seedSource = this.seedSourceGenerator
			.generateSeedSource(fromMeasurementRow(workbook.getTrialObservationByTrialInstanceNo(Integer.valueOf(source.getTrialInstanceNumber()))),
				workbook.getConditions(), selectionNumberToApply, source.getPlotNumber(), workbook.getStudyName(), plantNo, locationIdNameMap, environmentVariables);

		final ImportedGermplasm germplasm =
			new ImportedGermplasm(index, newGermplasmName, null /* gid */
				, source.getGermplasm().getCross(), seedSource,
				FieldbookUtil.generateEntryCode(index), null /* check */
				, breedingMethod.getMid());

		// assign parentage etc for the new Germplasm
		final Integer sourceGid = source.getGermplasm().getGid() != null ? Integer.valueOf(source.getGermplasm().getGid()) : -1;
		final Integer gnpgs = source.getGermplasm().getGnpgs() != null ? source.getGermplasm().getGnpgs() : -1;
		this.assignGermplasmAttributes(germplasm, sourceGid, gnpgs, source.getGermplasm().getGpid1(), source.getGermplasm().getGpid2(),
			source.getSourceMethod(), breedingMethod);

		// assign grouping based on parentage

		// check to see if a group ID (MGID) exists in the parent for this Germplasm, and set
		// newly created germplasm if part of a group ( > 0 )
		if (source.getGermplasm().getMgid() != null && source.getGermplasm().getMgid() > 0) {
			germplasm.setMgid(source.getGermplasm().getMgid());
		}

		this.assignNames(germplasm);

		germplasm.setTrialInstanceNumber(source.getTrialInstanceNumber());
		germplasm.setReplicationNumber(source.getReplicationNumber());
		germplasm.setPlotNumber(source.getPlotNumber());
		germplasm.setLocationId(source.getHarvestLocationId());
		if (plantNo != null) {
			germplasm.setPlantNumber(plantNo);
		}

		list.add(germplasm);
	}

	protected void assignNames(final ImportedGermplasm germplasm) {
		final List<Name> names = new ArrayList<>();

		final Name name = new Name();
		name.setTypeId(GermplasmNameType.DERIVATIVE_NAME.getUserDefinedFieldID());
		name.setNval(germplasm.getDesig());
		name.setNstat(1);
		names.add(name);

		germplasm.setNames(names);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ImportedGermplasm> generateGermplasmList(final AdvancingSourceList rows, final AdvancingStudy advancingParameters,
		final Workbook workbook) throws RuleException {

		final List<ImportedGermplasm> list = new ArrayList<>();
		int index = 1;
		final TimerWatch timer = new TimerWatch("advance");
		final Map<String, String> locationIdNameMap = this.studyDataManager.createInstanceLocationIdToNameMapFromStudy(workbook.getStudyDetails().getId());
		final List<MeasurementVariable> environmentVariables =
			this.datasetService.getObservationSetVariables(workbook.getTrialDatasetId(), Collections.singletonList(
				VariableType.ENVIRONMENT_DETAIL.getId()));
		for (final AdvancingSource row : rows.getRows()) {
			if (row.getGermplasm() != null && !row.isCheck() && row.getPlantsSelected() != null && row.getBreedingMethod() != null
				&& row.getPlantsSelected() > 0 && row.getBreedingMethod().isBulkingMethod() != null) {

				final List<String> names;
				final RuleExecutionContext namingExecutionContext =
					this.setupNamingRuleExecutionContext(row, advancingParameters.isCheckAdvanceLinesUnique());
				names = (List<String>) this.rulesService.runRules(namingExecutionContext);

				// if change detail object is created due to a duplicate being encountered somewhere during processing, provide a
				// reference index
				if (row.getChangeDetail() != null) {
					// index - 1 is used because Java uses 0-based referencing
					row.getChangeDetail().setIndex(index - 1);
				}

				// One plot may result in multiple plants/ears selected depending on selection method.
				int selectionNumber = row.getCurrentMaxSequence() + 1;
				final Iterator<SampleDTO> sampleIterator = row.getSamples().iterator();
				for (final String name : names) {
					String sampleNo = null;
					if (sampleIterator.hasNext()) {
						sampleNo = String.valueOf(sampleIterator.next().getSampleNumber());
					}
					this.addImportedGermplasmToList(list, row, name, row.getBreedingMethod(), index++, workbook, selectionNumber,
						advancingParameters, sampleNo, locationIdNameMap, environmentVariables);
					selectionNumber++;
				}
			}
		}
		timer.stop();
		return list;
	}

	@Override
	public List<ImportedCross> generateCrossesList(final List<ImportedCross> importedCrosses, final AdvancingSourceList rows,
		final AdvancingStudy advancingParameters, final Workbook workbook, final List<Integer> gids) throws RuleException {

		final List<Method> methodList = this.fieldbookMiddlewareService.getAllBreedingMethods(false);
		final Map<Integer, Method> breedingMethodMap = new HashMap<>();
		for (final Method method : methodList) {
			breedingMethodMap.put(method.getMid(), method);
		}

		int index = 0;
		final TimerWatch timer = new TimerWatch("cross");

		// PreviousMaxSequence is used is the DEFAULT indexed numbering used for entries.
		// The [SEQUENCE] code does not read this number but instead queries from the DB the next available number
		int previousMaxSequence = 0;
		for (final AdvancingSource advancingSource : rows.getRows()) {

			final ImportedCross importedCross = importedCrosses.get(index++);
			final List<String> names;
			advancingSource.setCurrentMaxSequence(previousMaxSequence);

			final Integer breedingMethodId = advancingSource.getBreedingMethodId();
			final Method selectedMethod = breedingMethodMap.get(breedingMethodId);

			if (!this.germplasmDataManager.isMethodNamingConfigurationValid(selectedMethod)) {
				throw new RulesNotConfiguredException(this.messageSource
					.getMessage("error.save.cross.rule.not.configured", new Object[] {selectedMethod.getMname()}, "The rules"
						+ " were not configured", LocaleContextHolder.getLocale()));
			}

			// here, we resolve the breeding method ID stored in the advancing source object into a proper breeding Method object
			advancingSource.setBreedingMethod(selectedMethod);
			//default plants selected value to 1 for list of crosses because sequence is not working if plants selected value is not set
			advancingSource.setPlantsSelected(1);

			// pass the parent gids (female and male) of the imported cross, this is required to properly resolve the Backcross process codes.
			advancingSource
				.setFemaleGid(StringUtils.isNumeric(importedCross.getFemaleGid()) ? Integer.valueOf(importedCross.getFemaleGid()) : 0);
			// Always gets the first male parent, ie. GPID2
			final String firstMaleGid = importedCross.getMaleGids().get(0).toString();
			advancingSource.setMaleGid(StringUtils.isNumeric(firstMaleGid) ? Integer.valueOf(firstMaleGid) : 0);

			final RuleExecutionContext namingExecutionContext =
				this.setupNamingRuleExecutionContext(advancingSource, advancingParameters.isCheckAdvanceLinesUnique());
			names = (List<String>) this.rulesService.runRules(namingExecutionContext);

			// Save away the current max sequence once rules have been run for this entry.
			previousMaxSequence = advancingSource.getCurrentMaxSequence() + 1;
			for (final String name : names) {
				importedCross.setDesig(name);
			}
		}
		timer.stop();
		return importedCrosses;
	}

	protected RuleExecutionContext setupNamingRuleExecutionContext(final AdvancingSource row, final boolean checkForDuplicateName) {
		List<String> sequenceList = Arrays.asList(this.ruleFactory.getRuleSequenceForNamespace("naming"));

		if (checkForDuplicateName) {
			// new array list is required since list generated from asList method does not support adding of more elements
			sequenceList = new ArrayList<>(sequenceList);
			sequenceList.add(EnforceUniqueNameRule.KEY);
		}

		final NamingRuleExecutionContext context =
			new NamingRuleExecutionContext(sequenceList, this.processCodeService, row, this.germplasmDataManager,
				new ArrayList<String>());
		context.setMessageSource(this.messageSource);

		return context;
	}

	void setMessageSource(final ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	void setFieldbookMiddlewareService(final FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}
}
