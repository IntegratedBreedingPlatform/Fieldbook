package com.efficio.fieldbook.web.naming.impl;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.naming.expression.dataprocessor.ExpressionDataProcessor;
import com.efficio.fieldbook.web.naming.expression.dataprocessor.ExpressionDataProcessorFactory;
import com.efficio.fieldbook.web.trial.bean.AdvanceType;
import com.efficio.fieldbook.web.trial.bean.AdvancingStudy;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.pojo.AdvancingSource;
import org.generationcp.commons.pojo.AdvancingSourceList;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.ValueReference;
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
import org.generationcp.middleware.service.api.FieldbookService;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

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

	private static final String DEFAULT_TEST_VALUE = "T";

	public AdvancingSourceList createAdvancingSourceList(final Workbook workbook, final AdvancingStudy advanceInfo, final Study study,
			final Map<Integer, Method> breedingMethodMap, final Map<String, Method> breedingMethodCodeMap) throws FieldbookException {

		Map<Integer, List<SampleDTO>> samplesMap = new HashMap<>();
		if (advanceInfo.getAdvanceType().equals(AdvanceType.SAMPLE)) {
			final Integer studyId = advanceInfo.getStudy().getId();
			samplesMap = this.studyDataManager.getExperimentSamplesDTOMap(studyId);
		}

		final AdvancingSource environmentLevel = new AdvancingSource();
		final ExpressionDataProcessor dataProcessor = dataProcessorFactory.retrieveExecutorProcessor();

		final AdvancingSourceList advancingSourceList = new AdvancingSourceList();

		final List<AdvancingSource> advancingPlotRows = new ArrayList<>();

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
			for (final MeasurementRow row : workbook.getObservations()) {
				final AdvancingSource advancingSourceCandidate = environmentLevel.copy();

				advancingSourceCandidate.setTrialInstanceNumber(row.getMeasurementDataValue(TermId.TRIAL_INSTANCE_FACTOR.getId()));

				// If study is Trail then setting data if trail instance is not null
				if (advancingSourceCandidate.getTrialInstanceNumber() != null) {
					final MeasurementRow trialInstanceObservations = workbook.getTrialObservationByTrialInstanceNo(
							Integer.valueOf(advancingSourceCandidate.getTrialInstanceNumber()));

					advancingSourceCandidate.setTrailInstanceObservation(trialInstanceObservations);
				}

				advancingSourceCandidate.setStudyType(workbook.getStudyDetails().getStudyType());

				// Setting conditions for Breeders Cross ID
				advancingSourceCandidate.setConditions(workbook.getConditions());


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

				final MeasurementData checkData = row.getMeasurementData(TermId.CHECK.getId());
				String check = null;
				if (checkData != null) {
					check = checkData.getcValueId();
					if (checkData != null && checkData.getMeasurementVariable() != null
							&& checkData.getMeasurementVariable().getPossibleValues() != null && !checkData.getMeasurementVariable()
							.getPossibleValues().isEmpty() && check != null && NumberUtils.isNumber(check)) {

						for (final ValueReference valref : checkData.getMeasurementVariable().getPossibleValues()) {
							if (valref.getId().equals(Double.valueOf(check).intValue())) {
								check = valref.getName();
								break;
							}
						}
					}
				}

				final boolean isCheck =
						check != null && !"".equals(check) && !AdvancingSourceListFactory.DEFAULT_TEST_VALUE.equalsIgnoreCase(check);

				final MeasurementData plotNumberData = row.getMeasurementData(TermId.PLOT_NO.getId());
				if (plotNumberData != null) {
					advancingSourceCandidate.setPlotNumber(plotNumberData.getValue());
				}

				Integer plantsSelected = null;
				final Method breedingMethod = breedingMethodMap.get(methodId);
				if (advanceInfo.getAdvanceType().equals(AdvanceType.SAMPLE)) {
					if (samplesMap.containsKey(row.getExperimentId())) {
						plantsSelected = samplesMap.get(row.getExperimentId()).size();
						advancingSourceCandidate.setSamples(samplesMap.get(row.getExperimentId()));
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
					advancingSourceCandidate.setGermplasm(germplasm);
					advancingSourceCandidate.setNames(names);
					advancingSourceCandidate.setPlantsSelected(plantsSelected);
					advancingSourceCandidate.setBreedingMethod(breedingMethod);
					advancingSourceCandidate.setCheck(isCheck);
					advancingSourceCandidate.setStudyName(studyName);
					advancingSourceCandidate.setStudyId(workbook.getStudyDetails().getId());
					advancingSourceCandidate.setEnvironmentDatasetId(workbook.getTrialDatasetId());
					advancingSourceCandidate.setDesignationIsPreviewOnly(true);

					dataProcessor.processPlotLevelData(advancingSourceCandidate, row);

					advancingPlotRows.add(advancingSourceCandidate);
				}

			}
		}

		this.setNamesToGermplasm(advancingPlotRows, gids);
		advancingSourceList.setRows(advancingPlotRows);
		this.assignSourceGermplasms(advancingSourceList, breedingMethodMap, gids);
		return advancingSourceList;
	}

	private void setNamesToGermplasm(final List<AdvancingSource> rows, final List<Integer> gids) throws MiddlewareQueryException {
		if (rows != null && !rows.isEmpty()) {
			final Map<Integer, List<Name>> map = this.fieldbookMiddlewareService.getNamesByGids(gids);
			for (final AdvancingSource row : rows) {
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
		germplasm.setSource(row.getMeasurementDataValue(TermId.SOURCE.getId()));
		return germplasm;
	}

	private void assignSourceGermplasms(final AdvancingSourceList list, final Map<Integer, Method> breedingMethodMap, final List<Integer> gids) throws FieldbookException {

		if (list != null && list.getRows() != null && !list.getRows().isEmpty()) {
			final List<Germplasm> germplasmList = this.fieldbookMiddlewareService.getGermplasms(gids);
			final Map<String, Germplasm> germplasmMap = new HashMap<>();
			for (final Germplasm germplasm : germplasmList) {
				germplasmMap.put(germplasm.getGid().toString(), germplasm);
			}
			for (final AdvancingSource source : list.getRows()) {
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
					final Method sourceMethod = breedingMethodMap.get(germplasm.getMethodId());
					if (sourceMethod != null) {
						source.setSourceMethod(sourceMethod);
					}
					source.getGermplasm().setBreedingMethodId(germplasm.getMethodId());
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
