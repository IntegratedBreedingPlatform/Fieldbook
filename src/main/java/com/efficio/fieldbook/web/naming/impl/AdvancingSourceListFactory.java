package com.efficio.fieldbook.web.naming.impl;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.naming.expression.dataprocessor.ExpressionDataProcessor;
import com.efficio.fieldbook.web.naming.expression.dataprocessor.ExpressionDataProcessorFactory;
import com.efficio.fieldbook.web.trial.bean.AdvanceType;
import com.efficio.fieldbook.web.trial.bean.AdvancingStudy;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.ruleengine.pojo.DeprecatedAdvancingSource;
import org.generationcp.middleware.ruleengine.pojo.AdvancingSourceList;
import org.generationcp.middleware.ruleengine.pojo.ImportedGermplasm;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.study.StudyInstanceService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdvancingSourceListFactory {

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private ResourceBundleMessageSource messageSource;

	@Resource
	private ExpressionDataProcessorFactory dataProcessorFactory;

	@Resource
	private StudyDataManager studyDataManager;

	@Resource
	private StudyInstanceService studyInstanceService;

	public AdvancingSourceList createAdvancingSourceList(final Workbook workbook, final AdvancingStudy advanceInfo, final Study study,
			final Map<Integer, Method> breedingMethodMap, final Map<String, Method> breedingMethodCodeMap) throws FieldbookException {

		Map<Integer, List<SampleDTO>> samplesMap = new HashMap<>();
		if (advanceInfo.getAdvanceType().equals(AdvanceType.SAMPLE)) {
			final Integer studyId = advanceInfo.getStudy().getId();
			samplesMap = this.studyDataManager.getExperimentSamplesDTOMap(studyId);
		}

		final DeprecatedAdvancingSource environmentLevel = new DeprecatedAdvancingSource();
		final ExpressionDataProcessor dataProcessor = this.dataProcessorFactory.retrieveExecutorProcessor();

		final AdvancingSourceList advancingSourceList = new AdvancingSourceList();

		final List<DeprecatedAdvancingSource> advancingPlotRows = new ArrayList<>();

		final Integer methodVariateId = advanceInfo.getMethodVariateId();
		final Integer lineVariateId = advanceInfo.getLineVariateId();
		final Integer plotVariateId = advanceInfo.getPlotVariateId();
		final List<Name> names = null;

		String studyName = null;
		if (study != null) {
			studyName = study.getName();
		}

		dataProcessor.processEnvironmentLevelData(environmentLevel, workbook, advanceInfo, study);

		final List<Integer> gids = new ArrayList<>();

		if (workbook != null && workbook.getObservations() != null && !workbook.getObservations().isEmpty()) {
			final Integer studyId = workbook.getStudyDetails().getId();
			final Map<Integer, StudyInstance> studyInstanceMap =
				this.studyInstanceService.getStudyInstances(studyId).stream()
					.collect(Collectors.toMap(StudyInstance::getInstanceNumber, i -> i));

			for (final MeasurementRow row : workbook.getObservations()) {
				final DeprecatedAdvancingSource deprecatedAdvancingSourceCandidate = environmentLevel.copy();

				deprecatedAdvancingSourceCandidate.setTrialInstanceNumber(row.getMeasurementDataValue(TermId.TRIAL_INSTANCE_FACTOR.getId()));

				// If study is Trial, then setting data if trial instance is not null
				if (deprecatedAdvancingSourceCandidate.getTrialInstanceNumber() != null) {
					final Integer trialInstanceNumber = Integer.valueOf(deprecatedAdvancingSourceCandidate.getTrialInstanceNumber());
					final MeasurementRow trialInstanceObservations = workbook.getTrialObservationByTrialInstanceNo(
							Integer.valueOf(deprecatedAdvancingSourceCandidate.getTrialInstanceNumber()));

					// Workaround to correct outdated location ID in trial instance
					if (studyInstanceMap.containsKey(trialInstanceNumber) &&
						trialInstanceObservations.getMeasurementData(TermId.LOCATION_ID.getId()) != null) {
						trialInstanceObservations.getMeasurementData(TermId.LOCATION_ID.getId())
							.setValue(String.valueOf(studyInstanceMap.get(trialInstanceNumber).getLocationId()));
					}

					deprecatedAdvancingSourceCandidate.setTrailInstanceObservation(trialInstanceObservations);
				}

				deprecatedAdvancingSourceCandidate.setStudyType(workbook.getStudyDetails().getStudyType());

				// Setting conditions for Breeders Cross ID
				deprecatedAdvancingSourceCandidate.setConditions(workbook.getConditions());
				deprecatedAdvancingSourceCandidate.setReplicationNumber(row.getMeasurementDataValue(TermId.REP_NO.getId()));


				Integer methodId = null;
				if ((advanceInfo.getMethodChoice() == null || "0".equals(advanceInfo.getMethodChoice())) && !advanceInfo.getAdvanceType()
						.equals(AdvanceType.SAMPLE)) {
					if (methodVariateId != null) {
						methodId = this.getBreedingMethodId(methodVariateId, row, breedingMethodCodeMap);
					}
				} else {
					methodId = this.getIntegerValue(advanceInfo.getBreedingMethodId());
				}

				if (methodId == null) {
					continue;
				}

				final ImportedGermplasm germplasm = this.createGermplasm(row);
				if (germplasm.getGid() != null && NumberUtils.isNumber(germplasm.getGid())) {
					gids.add(Integer.valueOf(germplasm.getGid()));
				}

				final MeasurementData plotNumberData = row.getMeasurementData(TermId.PLOT_NO.getId());
				if (plotNumberData != null) {
					deprecatedAdvancingSourceCandidate.setPlotNumber(plotNumberData.getValue());
				}

				Integer plantsSelected = null;
				final Method breedingMethod = breedingMethodMap.get(methodId);
				if (advanceInfo.getAdvanceType().equals(AdvanceType.SAMPLE)) {
					if (samplesMap.containsKey(row.getExperimentId())) {
						plantsSelected = samplesMap.get(row.getExperimentId()).size();
						deprecatedAdvancingSourceCandidate.setSamples(samplesMap.get(row.getExperimentId()));
					} else {
						continue;
					}
				}
				final Boolean isBulk = breedingMethod.isBulkingMethod();
				if (isBulk != null) {
					if (plantsSelected == null) {
						if (isBulk && (advanceInfo.getAllPlotsChoice() == null || "0".equals(advanceInfo.getAllPlotsChoice()))) {
							if (plotVariateId != null) {
								plantsSelected = this.getIntegerValue(row.getMeasurementDataValue(plotVariateId));
							}
						} else {
							if (lineVariateId != null && (advanceInfo.getLineChoice() == null || "0".equals(advanceInfo.getLineChoice()))) {
								plantsSelected = this.getIntegerValue(row.getMeasurementDataValue(lineVariateId));
							}
						}
					}
					deprecatedAdvancingSourceCandidate.setGermplasm(germplasm);
					deprecatedAdvancingSourceCandidate.setNames(names);
					deprecatedAdvancingSourceCandidate.setPlantsSelected(plantsSelected);
					deprecatedAdvancingSourceCandidate.setBreedingMethod(breedingMethod);
					deprecatedAdvancingSourceCandidate.setStudyName(studyName);
					deprecatedAdvancingSourceCandidate.setStudyId(studyId);
					deprecatedAdvancingSourceCandidate.setEnvironmentDatasetId(workbook.getTrialDatasetId());
					deprecatedAdvancingSourceCandidate.setDesignationIsPreviewOnly(true);

					dataProcessor.processPlotLevelData(deprecatedAdvancingSourceCandidate, row);

					advancingPlotRows.add(deprecatedAdvancingSourceCandidate);
				}

			}
		}

		this.setNamesToGermplasm(advancingPlotRows, gids);
		advancingSourceList.setRows(advancingPlotRows);
		this.assignSourceGermplasms(advancingSourceList, breedingMethodMap, gids);
		return advancingSourceList;
	}

	private void setNamesToGermplasm(final List<DeprecatedAdvancingSource> rows, final List<Integer> gids) throws MiddlewareQueryException {
		if (rows != null && !rows.isEmpty()) {
			final Map<Integer, List<Name>> map = this.fieldbookMiddlewareService.getNamesByGids(gids);
			for (final DeprecatedAdvancingSource row : rows) {
				final String gid = row.getGermplasm().getGid();
				if (gid != null && NumberUtils.isNumber(gid)) {
					final List<Name> names = map.get(Integer.valueOf(gid));
					if (names != null && !names.isEmpty()) {
						row.setNames(names);
					}
				}
			}
		}
	}

	private Integer getIntegerValue(final String value) {
		Integer integerValue = null;

		if (NumberUtils.isNumber(value)) {
			integerValue = Double.valueOf(value).intValue();
		}

		return integerValue;
	}

	private ImportedGermplasm createGermplasm(final MeasurementRow row) {
		final ImportedGermplasm germplasm = new ImportedGermplasm();
		germplasm.setCross(row.getMeasurementDataValue(TermId.CROSS.getId()));
		germplasm.setDesig(row.getMeasurementDataValue(TermId.DESIG.getId()));
		germplasm.setEntryCode(row.getMeasurementDataValue(TermId.ENTRY_CODE.getId()));
		germplasm.setEntryNumber(this.getIntegerValue(row.getMeasurementDataValue(TermId.ENTRY_NO.getId())));
		germplasm.setGid(row.getMeasurementDataValue(TermId.GID.getId()));
		germplasm.setSource(row.getMeasurementDataValue(TermId.IMMEDIATE_SOURCE_NAME.getId()));
		return germplasm;
	}

	private void assignSourceGermplasms(final AdvancingSourceList list, final Map<Integer, Method> breedingMethodMap, final List<Integer> gids) throws FieldbookException {

		if (list != null && list.getRows() != null && !list.getRows().isEmpty()) {
			final List<Germplasm> germplasmList = this.fieldbookMiddlewareService.getGermplasms(gids);
			final Map<String, Germplasm> germplasmMap = new HashMap<>();
			for (final Germplasm germplasm : germplasmList) {
				germplasmMap.put(germplasm.getGid().toString(), germplasm);
			}
			for (final DeprecatedAdvancingSource source : list.getRows()) {
				if (source.getGermplasm() != null && source.getGermplasm().getGid() != null && NumberUtils
						.isNumber(source.getGermplasm().getGid())) {
					final Germplasm germplasm = germplasmMap.get(source.getGermplasm().getGid().toString());

					if (germplasm == null) {
						// we throw exception because germplasm is not existing
						final Locale locale = LocaleContextHolder.getLocale();
						throw new FieldbookException(
								this.messageSource.getMessage("error.advancing.germplasm.not.existing", new String[] {}, locale));
					}

					source.getGermplasm().setGpid1(germplasm.getGpid1());
					source.getGermplasm().setGpid2(germplasm.getGpid2());
					source.getGermplasm().setGnpgs(germplasm.getGnpgs());
					source.getGermplasm().setMgid(germplasm.getMgid());
					final Method sourceMethod = breedingMethodMap.get(germplasm.getMethod().getMid());
					if (sourceMethod != null) {
						source.setSourceMethod(sourceMethod);
					}
					source.getGermplasm().setBreedingMethodId(germplasm.getMethod().getMid());
				}
			}

		}
	}

	private Integer getBreedingMethodId(final Integer methodVariateId, final MeasurementRow row, final Map<String, Method> breedingMethodCodeMap) {
		Integer methodId = null;
		if (methodVariateId.equals(TermId.BREEDING_METHOD_VARIATE.getId())) {
			methodId = this.getIntegerValue(row.getMeasurementDataValue(methodVariateId));
		} else if (methodVariateId.equals(TermId.BREEDING_METHOD_VARIATE_TEXT.getId()) || methodVariateId
				.equals(TermId.BREEDING_METHOD_VARIATE_CODE.getId())) {
			final String methodName = row.getMeasurementDataValue(methodVariateId);
			if (NumberUtils.isNumber(methodName)) {
				methodId = Double.valueOf(methodName).intValue();
			} else {
				// coming from old fb or other sources
				final Set<String> keys = breedingMethodCodeMap.keySet();
				final Iterator<String> iterator = keys.iterator();
				while (iterator.hasNext()) {
					final String code = iterator.next();
					final Method method = breedingMethodCodeMap.get(code);
					if (methodVariateId.equals(TermId.BREEDING_METHOD_VARIATE_TEXT.getId()) && methodName != null && methodName
							.equalsIgnoreCase(method.getMname())) {
						methodId = method.getMid();
						break;
					}
					if (methodVariateId.equals(TermId.BREEDING_METHOD_VARIATE_CODE.getId()) && methodName != null && methodName
							.equalsIgnoreCase(method.getMcode())) {
						methodId = method.getMid();
						break;
					}
				}
			}
		} else {
			// on load of study, this has been converted to id and not the code.
			methodId = this.getIntegerValue(row.getMeasurementDataValue(methodVariateId));
		}
		return methodId;
	}

}
