
package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.common.service.RandomizeCompleteBlockDesignService;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.efficio.fieldbook.web.util.SettingsUtil;

@Service
@Transactional
public class RandomizeCompleteBlockDesignServiceImpl implements RandomizeCompleteBlockDesignService {

	private static final Logger LOG = LoggerFactory.getLogger(RandomizeCompleteBlockDesignServiceImpl.class);

	@Resource
	public org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;
	@Resource
	protected WorkbenchService workbenchService;
	@Resource
	protected FieldbookProperties fieldbookProperties;
	@Resource
	private ResourceBundleMessageSource messageSource;
	@Resource
	public FieldbookService fieldbookService;
	@Resource
	private UserSelection userSelection;
	@Resource
	private ContextUtil contextUtil;

	@Override
	public List<MeasurementRow> generateDesign(List<ImportedGermplasm> germplasmList, ExpDesignParameterUi parameter,
			List<MeasurementVariable> trialVariables, List<MeasurementVariable> factors, List<MeasurementVariable> nonTrialFactors,
			List<MeasurementVariable> variates, List<TreatmentVariable> treatmentVariables) throws BVDesignException {

		List<MeasurementRow> measurementRowList = new ArrayList<>();
		String block = parameter.getReplicationsCount();
		int environments = Integer.valueOf(parameter.getNoOfEnvironments());
		int environmentsToAdd = Integer.valueOf(parameter.getNoOfEnvironmentsToAdd());

		try {

			List<String> treatmentFactor = new ArrayList<>();
			List<String> levels = new ArrayList<>();

			// Key - CVTerm ID , List of values
			Map<String, List<String>> treatmentFactorValues = new HashMap<String, List<String>>();
			Map treatmentFactorsData = parameter.getTreatmentFactorsData();

			List<SettingDetail> treatmentFactorList = this.userSelection.getTreatmentFactors();

			if (treatmentFactorsData != null) {
				Iterator keySetIter = treatmentFactorsData.keySet().iterator();
				while (keySetIter.hasNext()) {
					String key = (String) keySetIter.next();
					Map treatmentData = (Map) treatmentFactorsData.get(key);
					treatmentFactorValues.put(key, (List) treatmentData.get("labels"));
					// add the treatment variables
					Object pairVarObj = treatmentData.get("variableId");

					String pairVar = "";
					if (pairVarObj instanceof String) {
						pairVar = (String) pairVarObj;
					} else {
						pairVar = pairVarObj.toString();
					}
					if (key != null && NumberUtils.isNumber(key) && pairVar != null && NumberUtils.isNumber(pairVar)) {
						int treatmentPair1 = Integer.parseInt(key);
						int treatmentPair2 = Integer.parseInt(pairVar);
						StandardVariable stdVar1 = this.fieldbookMiddlewareService.getStandardVariable(treatmentPair1,
								contextUtil.getCurrentProgramUUID());
						stdVar1.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
						StandardVariable stdVar2 = this.fieldbookMiddlewareService.getStandardVariable(treatmentPair2,
								contextUtil.getCurrentProgramUUID());
						stdVar2.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
						TreatmentVariable treatmentVar = new TreatmentVariable();
						MeasurementVariable measureVar1 =
								ExpDesignUtil.convertStandardVariableToMeasurementVariable(stdVar1, Operation.ADD, this.fieldbookService);
						MeasurementVariable measureVar2 =
								ExpDesignUtil.convertStandardVariableToMeasurementVariable(stdVar2, Operation.ADD, this.fieldbookService);
						measureVar1.setFactor(true);
						measureVar2.setFactor(true);

						SettingsUtil.findAndUpdateVariableName(treatmentFactorList, measureVar1);

						measureVar1.setTreatmentLabel(measureVar1.getName());
						measureVar2.setTreatmentLabel(measureVar1.getName());

						treatmentVar.setLevelVariable(measureVar1);
						treatmentVar.setValueVariable(measureVar2);
						treatmentVariables.add(treatmentVar);
					}

				}
			}

			if (treatmentFactorValues != null) {
				Set<String> keySet = treatmentFactorValues.keySet();
				for (String key : keySet) {
					int level = treatmentFactorValues.get(key).size();
					treatmentFactor.add(ExpDesignUtil.cleanBVDesingKey(key));
					levels.add(Integer.toString(level));
				}
			}

			StandardVariable stdvarTreatment = this.fieldbookMiddlewareService.getStandardVariable(
					TermId.ENTRY_NO.getId(),contextUtil.getCurrentProgramUUID());

			treatmentFactorValues.put(stdvarTreatment.getName(), Arrays.asList(Integer.toString(germplasmList.size())));
			treatmentFactor.add(stdvarTreatment.getName());
			levels.add(Integer.toString(germplasmList.size()));

			StandardVariable stdvarRep = null;
			StandardVariable stdvarPlot = null;

			List<StandardVariable> reqVarList = this.getRequiredVariable();

			for (StandardVariable var : reqVarList) {
				if (var.getId() == TermId.REP_NO.getId()) {
					stdvarRep = var;
				} else if (var.getId() == TermId.PLOT_NO.getId()) {
					stdvarPlot = var;
				}
			}

			Integer plotNo = this.userSelection.getStartingPlotNo();

			MainDesign mainDesign =
					ExpDesignUtil.createRandomizedCompleteBlockDesign(block, stdvarRep.getName(), stdvarPlot.getName(), plotNo, treatmentFactor, levels, "");

			measurementRowList =
					ExpDesignUtil.generateExpDesignMeasurements(environments, environmentsToAdd, trialVariables, factors, nonTrialFactors,
							variates, treatmentVariables, reqVarList, germplasmList, mainDesign, this.workbenchService,
							this.fieldbookProperties, stdvarTreatment.getName(), treatmentFactorValues, this.fieldbookService);


		} catch (BVDesignException e) {
			throw e;
		} catch (Exception e) {
			RandomizeCompleteBlockDesignServiceImpl.LOG.error(e.getMessage(), e);
		}

		return measurementRowList;
	}

	@Override
	public List<StandardVariable> getRequiredVariable() {
		List<StandardVariable> varList = new ArrayList<>();
		try {
			StandardVariable stdvarRep = this.fieldbookMiddlewareService.getStandardVariable(TermId.REP_NO.getId(),
					contextUtil.getCurrentProgramUUID());
			stdvarRep.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
			StandardVariable stdvarPlot = this.fieldbookMiddlewareService.getStandardVariable(TermId.PLOT_NO.getId(),
					contextUtil.getCurrentProgramUUID());
			stdvarPlot.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);

			varList.add(stdvarRep);
			varList.add(stdvarPlot);
		} catch (MiddlewareException e) {
			RandomizeCompleteBlockDesignServiceImpl.LOG.error(e.getMessage(), e);
		}
		return varList;
	}

	@Override
	public ExpDesignValidationOutput validate(ExpDesignParameterUi expDesignParameter, List<ImportedGermplasm> germplasmList) {
		Locale locale = LocaleContextHolder.getLocale();
		ExpDesignValidationOutput output = new ExpDesignValidationOutput(true, "");
		try {
			if (expDesignParameter != null && germplasmList != null) {
				if (!NumberUtils.isNumber(expDesignParameter.getReplicationsCount())) {
					output =
							new ExpDesignValidationOutput(false, this.messageSource.getMessage(
									"experiment.design.replication.count.should.be.a.number", null, locale));
				} else {
					int replicationCount = Integer.valueOf(expDesignParameter.getReplicationsCount());

					if (replicationCount <= 0 || replicationCount >= 13) {
						output =
								new ExpDesignValidationOutput(false, this.messageSource.getMessage(
										"experiment.design.replication.count.rcbd.error", null, locale));
					}
				}
			}
		} catch (Exception e) {
			output =
					new ExpDesignValidationOutput(false, this.messageSource.getMessage("experiment.design.invalid.generic.error", null,
							locale));
		}

		return output;
	}

	@Override
	public List<Integer> getExperimentalDesignVariables(ExpDesignParameterUi params) {
		return Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId());
	}

}
