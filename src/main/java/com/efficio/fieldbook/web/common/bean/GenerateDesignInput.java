
package com.efficio.fieldbook.web.common.bean;

import com.efficio.fieldbook.web.trial.bean.TrialSettingsBean;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;

import com.efficio.fieldbook.web.trial.bean.InstanceInfo;

/**
 * This class is use to contain all the variables needed in generating design for the following design types: Preset Design (e.g.
 * E30-Rep2-Block6-5Ind, E30-Rep3-Block6-5Ind, E50-Rep2-Block5-10Ind) and Custom Import Design. Used mainly in DesignImport feature.
 */
public class GenerateDesignInput {

	private InstanceInfo instanceInfo;
	private ExperimentDesignType selectedExperimentDesignType;
	private Integer startingEntryNo;
	private Integer startingPlotNo;
	private Boolean hasNewEnvironmentAdded;
	private TrialSettingsBean trialSettings;

	public GenerateDesignInput() {
		this.instanceInfo = new InstanceInfo();
		this.selectedExperimentDesignType = new ExperimentDesignType();
		this.startingEntryNo = 1;
		this.startingPlotNo = 1;
		this.hasNewEnvironmentAdded = false;
	}

	public GenerateDesignInput(final InstanceInfo instanceInfo, final ExperimentDesignType selectedExperimentDesignType,
			final Integer startingEntryNo, final Integer startingPlotNo, final Boolean hasNewEnvironmentAdded) {
		super();
		this.instanceInfo = instanceInfo;
		this.selectedExperimentDesignType = selectedExperimentDesignType;
		this.startingEntryNo = startingEntryNo;
		this.startingPlotNo = startingPlotNo;
		this.hasNewEnvironmentAdded = hasNewEnvironmentAdded;
	}

	public InstanceInfo getInstanceInfo() {
		return this.instanceInfo;
	}

	public void setInstanceInfo(final InstanceInfo instanceInfo) {
		this.instanceInfo = instanceInfo;
	}

	public ExperimentDesignType getSelectedExperimentDesignType() {
		return this.selectedExperimentDesignType;
	}

	public void setSelectedExperimentDesignType(final ExperimentDesignType selectedExperimentDesignType) {
		this.selectedExperimentDesignType = selectedExperimentDesignType;
	}

	public Integer getStartingEntryNo() {
		return this.startingEntryNo;
	}

	public void setStartingEntryNo(final Integer startingEntryNo) {
		this.startingEntryNo = startingEntryNo;
	}

	public Integer getStartingPlotNo() {
		return this.startingPlotNo;
	}

	public void setStartingPlotNo(final Integer startingPlotNo) {
		this.startingPlotNo = startingPlotNo;
	}

	public Boolean getHasNewEnvironmentAdded() {
		return this.hasNewEnvironmentAdded;
	}

	public void setHasNewEnvironmentAdded(final Boolean hasNewEnvironmentAdded) {
		this.hasNewEnvironmentAdded = hasNewEnvironmentAdded;
	}

	public TrialSettingsBean getTrialSettings() {
		return trialSettings;
	}

	public void setTrialSettings(TrialSettingsBean trialSettings) {
		this.trialSettings = trialSettings;
	}
}
