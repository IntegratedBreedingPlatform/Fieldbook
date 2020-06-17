
package com.efficio.fieldbook.web.trial.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ExpDesignParameterUi implements Serializable, TabInfoBean {

	private static final long serialVersionUID = -3081300491744812262L;
	private List<Integer> trialInstancesForDesignGeneration;
	private Integer designType;
	private Integer replicationsCount;
	private Boolean useLatenized;
	private Integer blockSize;
	private Integer rowsPerReplications;
	private Integer colsPerReplications;
	private Map treatmentFactors;
	private Map treatmentFactorsData;
	private Integer totalGermplasmListCount;
	private Integer nclatin;
	private Integer nrlatin;
	private Integer nblatin;
	private String replatinGroups;
	private boolean hasMeasurementData;
	private Integer startingPlotNo;
	private String fileName;
	private Integer numberOfBlocks;
	private Integer checkStartingPosition;
	private Integer checkSpacing;
	private Integer checkInsertionManner;
	private Integer replicationPercentage;
	private List<Instance> instances;
	private TrialSettingsBean trialSettings;

	/*
	 * 1 - single col 2 - single row 3 - adjacent
	 */
	private Integer replicationsArrangement;

	public List<Integer> getTrialInstancesForDesignGeneration() {
		return this.trialInstancesForDesignGeneration;
	}

	public Integer getDesignType() {
		return this.designType;
	}

	public void setDesignType(final Integer designType) {
		this.designType = designType;
	}

	public Integer getReplicationsCount() {
		return this.replicationsCount;
	}

	public void setReplicationsCount(final Integer replicationsCount) {
		this.replicationsCount = replicationsCount;
	}

	public Boolean getUseLatenized() {
		return this.useLatenized;
	}

	public void setUseLatenized(final Boolean useLatenized) {
		this.useLatenized = useLatenized;
	}

	public Integer getBlockSize() {
		return this.blockSize;
	}

	public void setBlockSize(final Integer blockSize) {
		this.blockSize = blockSize;
	}

	public Integer getRowsPerReplications() {
		return this.rowsPerReplications;
	}

	public void setRowsPerReplications(final Integer rowsPerReplications) {
		this.rowsPerReplications = rowsPerReplications;
	}

	public Integer getColsPerReplications() {
		return this.colsPerReplications;
	}

	public void setColsPerReplications(final Integer colsPerReplications) {
		this.colsPerReplications = colsPerReplications;
	}

	public Map getTreatmentFactors() {
		return this.treatmentFactors;
	}

	public void setTreatmentFactors(final Map treatmentFactors) {
		this.treatmentFactors = treatmentFactors;
	}

	public Map getTreatmentFactorsData() {
		return this.treatmentFactorsData;
	}

	public void setTreatmentFactorsData(final Map treatmentFactorsData) {
		this.treatmentFactorsData = treatmentFactorsData;
	}

	public Integer getTotalGermplasmListCount() {
		return this.totalGermplasmListCount;
	}

	public void setTotalGermplasmListCount(final Integer totalGermplasmListCount) {
		this.totalGermplasmListCount = totalGermplasmListCount;
	}

	public Integer getNclatin() {
		return this.nclatin;
	}

	public void setNclatin(final Integer nclatin) {
		this.nclatin = nclatin;
	}

	public Integer getNrlatin() {
		return this.nrlatin;
	}

	public void setNrlatin(final Integer nrlatin) {
		this.nrlatin = nrlatin;
	}

	public Integer getNblatin() {
		return this.nblatin;
	}

	public void setNblatin(final Integer nblatin) {
		this.nblatin = nblatin;
	}

	public String getReplatinGroups() {
		return this.replatinGroups;
	}

	public void setReplatinGroups(final String replatinGroups) {
		this.replatinGroups = replatinGroups;
	}

	public Integer getReplicationsArrangement() {
		return this.replicationsArrangement;
	}

	public void setReplicationsArrangement(final Integer replicationsArrangement) {
		this.replicationsArrangement = replicationsArrangement;
	}

	public boolean isHasMeasurementData() {
		return this.hasMeasurementData;
	}

	public void setHasMeasurementData(final boolean hasMeasurementData) {
		this.hasMeasurementData = hasMeasurementData;
	}

	public Integer getStartingPlotNo() {
		return this.startingPlotNo;
	}

	public void setStartingPlotNo(final Integer startingPlotNo) {
		this.startingPlotNo = startingPlotNo;
	}

	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	public Integer getNumberOfBlocks() {
		return this.numberOfBlocks;
	}

	public void setNumberOfBlocks(final Integer numberOfBlocks) {
		this.numberOfBlocks = numberOfBlocks;
	}

	public Integer getCheckStartingPosition() {
		return this.checkStartingPosition;
	}

	public void setCheckStartingPosition(final Integer checkStartingPosition) {
		this.checkStartingPosition = checkStartingPosition;
	}

	public Integer getCheckSpacing() {
		return this.checkSpacing;
	}

	public void setCheckSpacing(final Integer checkSpacing) {
		this.checkSpacing = checkSpacing;
	}

	public Integer getCheckInsertionManner() {
		return this.checkInsertionManner;
	}

	public void setCheckInsertionManner(final Integer checkInsertionManner) {
		this.checkInsertionManner = checkInsertionManner;
	}

	public Integer getReplicationPercentage() {
		return this.replicationPercentage;
	}

	public void setReplicationPercentage(final Integer replicationPercentage) {
		this.replicationPercentage = replicationPercentage;
	}

	public List<Instance> getInstances() {
		return this.instances;
	}

	public void setInstances(final List<Instance> instances) {
		this.instances = instances;
	}

	public TrialSettingsBean getTrialSettings() {
		return this.trialSettings;
	}

	public void setTrialSettings(final TrialSettingsBean trialSettings) {
		this.trialSettings = trialSettings;
	}
}
