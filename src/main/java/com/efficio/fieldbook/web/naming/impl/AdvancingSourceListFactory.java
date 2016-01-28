
package com.efficio.fieldbook.web.naming.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import com.efficio.fieldbook.web.naming.expression.dataprocessor.ExpressionDataProcessor;
import com.efficio.fieldbook.web.naming.expression.dataprocessor.ExpressionDataProcessorFactory;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.FieldbookService;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSourceList;

@Service
@Transactional
public class AdvancingSourceListFactory {

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private ResourceBundleMessageSource messageSource;

	@Resource
	private ExpressionDataProcessorFactory dataProcessorFactory;

	private static final String DEFAULT_TEST_VALUE = "T";

	public AdvancingSourceList createAdvancingSourceList(Workbook workbook, AdvancingNursery advanceInfo, Study nursery,
			Map<Integer, Method> breedingMethodMap, Map<String, Method> breedingMethodCodeMap) throws FieldbookException {

		AdvancingSource environmentLevel = new AdvancingSource();
		ExpressionDataProcessor dataProcessor = dataProcessorFactory.retrieveExecutorProcessor();

		AdvancingSourceList list = new AdvancingSourceList();

		List<AdvancingSource> rows = new ArrayList<>();

		Integer methodVariateId = advanceInfo.getMethodVariateId();
		Integer lineVariateId = advanceInfo.getLineVariateId();
		Integer plotVariateId = advanceInfo.getPlotVariateId();
		List<Name> names = null;

		String nurseryName = null;
		if (nursery != null) {
			nurseryName = nursery.getName();
		}

		dataProcessor.processEnvironmentLevelData(environmentLevel, workbook, advanceInfo, nursery);

		List<Integer> gids = new ArrayList<>();

		if (workbook != null && workbook.getObservations() != null && !workbook.getObservations().isEmpty()) {
			for (MeasurementRow row : workbook.getObservations()) {
                AdvancingSource source = environmentLevel.copy();

				Integer methodId = null;
				if (advanceInfo.getMethodChoice() == null || "0".equals(advanceInfo.getMethodChoice())) {
					if (methodVariateId != null) {
						methodId = this.getBreedingMethodId(methodVariateId, row, breedingMethodCodeMap);
					}
				} else {
					methodId = this.getIntegerValue(advanceInfo.getBreedingMethodId());
				}

				if (methodId == null) {
					continue;
				}

				ImportedGermplasm germplasm = this.createGermplasm(row);
				if (germplasm.getGid() != null && NumberUtils.isNumber(germplasm.getGid())) {
					gids.add(Integer.valueOf(germplasm.getGid()));
				}

				MeasurementData checkData = row.getMeasurementData(TermId.CHECK.getId());
				String check = null;
				if (checkData != null) {
					check = checkData.getcValueId();
					if (checkData != null && checkData.getMeasurementVariable() != null
							&& checkData.getMeasurementVariable().getPossibleValues() != null
							&& !checkData.getMeasurementVariable().getPossibleValues().isEmpty() && check != null
							&& NumberUtils.isNumber(check)) {

						for (ValueReference valref : checkData.getMeasurementVariable().getPossibleValues()) {
							if (valref.getId().equals(Double.valueOf(check).intValue())) {
								check = valref.getName();
								break;
							}
						}
					}
				}

				boolean isCheck =
						check != null && !"".equals(check) && !AdvancingSourceListFactory.DEFAULT_TEST_VALUE.equalsIgnoreCase(check);

				MeasurementData plotNumberData = row.getMeasurementData(TermId.PLOT_NO.getId());
				if (plotNumberData != null) {
					source.setPlotNumber(plotNumberData.getValue());
				}
				
				Method breedingMethod = breedingMethodMap.get(methodId);
				Integer plantsSelected = null;
				Boolean isBulk = breedingMethod.isBulkingMethod();
				if (isBulk != null) {
					if (isBulk && (advanceInfo.getAllPlotsChoice() == null || "0".equals(advanceInfo.getAllPlotsChoice()))) {
						if (plotVariateId != null) {
							plantsSelected = this.getIntegerValue(row.getMeasurementDataValue(plotVariateId));
						}
					} else {
						if (lineVariateId != null && (advanceInfo.getLineChoice() == null || "0".equals(advanceInfo.getLineChoice()))) {
							plantsSelected = this.getIntegerValue(row.getMeasurementDataValue(lineVariateId));
						}
					}
					source.setGermplasm(germplasm);
					source.setNames(names);
					source.setPlantsSelected(plantsSelected);
					source.setBreedingMethod(breedingMethod);
					source.setCheck(isCheck);
					source.setNurseryName(nurseryName);

                    dataProcessor.processPlotLevelData(source, row);

					rows.add(source);
				}

			}
		}

		this.setNamesToGermplasm(rows, gids);
		list.setRows(rows);
		this.assignSourceGermplasms(list, breedingMethodMap);
		return list;
	}

	private void setNamesToGermplasm(List<AdvancingSource> rows, List<Integer> gids) throws MiddlewareQueryException {
		if (rows != null && !rows.isEmpty()) {
			Map<Integer, List<Name>> map = this.fieldbookMiddlewareService.getNamesByGids(gids);
			for (AdvancingSource row : rows) {
				String gid = row.getGermplasm().getGid();
				if (gid != null && NumberUtils.isNumber(gid)) {
					List<Name> names = map.get(Integer.valueOf(gid));
					if (names != null && !names.isEmpty()) {
						row.setNames(names);
					}
				}
			}
		}
	}

	private Integer getIntegerValue(String value) {
		Integer integerValue = null;

		if (NumberUtils.isNumber(value)) {
			integerValue = Double.valueOf(value).intValue();
		}

		return integerValue;
	}

	private ImportedGermplasm createGermplasm(MeasurementRow row) {
		ImportedGermplasm germplasm = new ImportedGermplasm();
		germplasm.setCross(row.getMeasurementDataValue(TermId.CROSS.getId()));
		germplasm.setDesig(row.getMeasurementDataValue(TermId.DESIG.getId()));
		germplasm.setEntryCode(row.getMeasurementDataValue(TermId.ENTRY_CODE.getId()));
		germplasm.setEntryId(this.getIntegerValue(row.getMeasurementDataValue(TermId.ENTRY_NO.getId())));
		germplasm.setGid(row.getMeasurementDataValue(TermId.GID.getId()));
		germplasm.setSource(row.getMeasurementDataValue(TermId.SOURCE.getId()));
		return germplasm;
	}

	private void assignSourceGermplasms(AdvancingSourceList list, Map<Integer, Method> breedingMethodMap) throws FieldbookException {
		List<Integer> gidList = new ArrayList<>();

		if (list != null && list.getRows() != null && !list.getRows().isEmpty()) {
			for (AdvancingSource source : list.getRows()) {
				if (source.getGermplasm() != null && source.getGermplasm().getGid() != null
						&& NumberUtils.isNumber(source.getGermplasm().getGid())) {

					gidList.add(Integer.valueOf(source.getGermplasm().getGid()));
				}
			}
			List<Germplasm> germplasmList = this.fieldbookMiddlewareService.getGermplasms(gidList);
			Map<String, Germplasm> germplasmMap = new HashMap<>();
			for (Germplasm germplasm : germplasmList) {
				germplasmMap.put(germplasm.getGid().toString(), germplasm);
			}
			for (AdvancingSource source : list.getRows()) {
				if (source.getGermplasm() != null && source.getGermplasm().getGid() != null
						&& NumberUtils.isNumber(source.getGermplasm().getGid())) {
					Germplasm germplasm = germplasmMap.get(source.getGermplasm().getGid().toString());

					if (germplasm == null) {
						// we throw exception because germplasm is not existing
						Locale locale = LocaleContextHolder.getLocale();
						throw new FieldbookException(this.messageSource.getMessage("error.advancing.germplasm.not.existing",
								new String[] {}, locale));
					}

					source.getGermplasm().setGpid1(germplasm.getGpid1());
					source.getGermplasm().setGpid2(germplasm.getGpid2());
					source.getGermplasm().setGnpgs(germplasm.getGnpgs());
					Method sourceMethod = breedingMethodMap.get(germplasm.getMethodId());
					if (sourceMethod != null) {
						source.setSourceMethod(sourceMethod);
					}
					source.getGermplasm().setBreedingMethodId(germplasm.getMethodId());
				}
			}

		}
	}

	private Integer getBreedingMethodId(Integer methodVariateId, MeasurementRow row, Map<String, Method> breedingMethodCodeMap) {
		Integer methodId = null;
		if (methodVariateId.equals(TermId.BREEDING_METHOD_VARIATE.getId())) {
			methodId = this.getIntegerValue(row.getMeasurementDataValue(methodVariateId));
		} else if (methodVariateId.equals(TermId.BREEDING_METHOD_VARIATE_TEXT.getId())) {
			String methodName = row.getMeasurementDataValue(methodVariateId);
			if (NumberUtils.isNumber(methodName)) {
				methodId = Double.valueOf(methodName).intValue();
			} else {
				// coming from old fb or other sources
				Set<String> keys = breedingMethodCodeMap.keySet();
				Iterator<String> iterator = keys.iterator();
				while (iterator.hasNext()) {
					String code = iterator.next();
					Method method = breedingMethodCodeMap.get(code);
					if (methodName != null && methodName.equalsIgnoreCase(method.getMname())) {
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
