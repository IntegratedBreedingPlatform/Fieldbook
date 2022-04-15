package com.efficio.fieldbook.web.trial.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 7/8/2014 Time: 5:08 PM
 */
// Added JsonIgnoreProperties to this class to ensure that the server will ignore any unknown and unnecessary properties
// that it receives. It's possible that Instance JSON object from the browser is modified with properties that are not
// essential in the server processing.
@JsonIgnoreProperties(ignoreUnknown = true)
public class Instance {

	private long stockId;
	private long instanceId;
	private int experimentId;
	private Integer fileCount;
	private String[] fileVariableIds;

	private Map<String, String> managementDetailValues;
	private Map<String, String> trialDetailValues;
	private Map<String, Integer> trialConditionDataIdMap;
	private Map<String, Integer> managementDetailDataIdMap;

	public Instance() {
		this.managementDetailValues = new HashMap<>();
		this.trialDetailValues = new HashMap<>();
		this.trialConditionDataIdMap = new HashMap<>();
		this.managementDetailDataIdMap = new HashMap<>();
	}

	public Map<String, String> getManagementDetailValues() {
		return this.managementDetailValues;
	}

	public void setManagementDetailValues(final Map<String, String> managementDetailValues) {
		this.managementDetailValues = managementDetailValues;
	}

	public Map<String, String> getTrialDetailValues() {
		return this.trialDetailValues;
	}

	public void setTrialDetailValues(final Map<String, String> trialDetailValues) {
		this.trialDetailValues = trialDetailValues;
	}

	public long getStockId() {
		return this.stockId;
	}

	public void setStockId(final long stockId) {
		this.stockId = stockId;
	}

	public long getInstanceId() {
		return this.instanceId;
	}

	public void setInstanceId(final long instanceId) {
		this.instanceId = instanceId;
	}

	public Map<String, Integer> getTrialConditionDataIdMap() {
		return this.trialConditionDataIdMap;
	}

	public void setTrialConditionDataIdMap(final Map<String, Integer> trialConditionDataIdMap) {
		this.trialConditionDataIdMap = trialConditionDataIdMap;
	}

	public Map<String, Integer> getManagementDetailDataIdMap() {
		return this.managementDetailDataIdMap;
	}

	public void setManagementDetailDataIdMap(final Map<String, Integer> managementDetailDataIdMap) {
		this.managementDetailDataIdMap = managementDetailDataIdMap;
	}

	public int getExperimentId() {
		return this.experimentId;
	}

	public void setExperimentId(final int experimentId) {
		this.experimentId = experimentId;
	}

	public Integer getFileCount() {
		return fileCount;
	}

	public void setFileCount(final Integer fileCount) {
		this.fileCount = fileCount;
	}

	public String[] getFileVariableIds() {
		return fileVariableIds;
	}

	public void setFileVariableIds(final String[] fileVariableIds) {
		this.fileVariableIds = fileVariableIds;
	}
}
