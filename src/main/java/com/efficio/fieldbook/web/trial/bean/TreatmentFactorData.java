
package com.efficio.fieldbook.web.trial.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 *
 * This is a class used to represent treatment factor data being submitted from the front end
 */
public class TreatmentFactorData {

	private int levels;
	private List<String> labels;
	private Integer variableId;

	public TreatmentFactorData() {
		this.labels = new ArrayList<String>();
	}

	public int getLevels() {
		return this.levels;
	}

	public void setLevels(int levels) {
		this.levels = levels;
	}

	public List<String> getLabels() {
		return this.labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

	public Integer getVariableId() {
		return this.variableId;
	}

	public void setVariableId(Integer variableId) {
		this.variableId = variableId;
	}

}
