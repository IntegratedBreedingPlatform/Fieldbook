/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.common.bean;

import com.efficio.fieldbook.web.trial.form.AdvancingStudyForm;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class PaginationListSelection.
 *
 * This is the session object that keeps track of list that needs to be paginated over multiple tabs
 */
public class PaginationListSelection implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2448596622077650635L;

	/** The review details list. */
	private final Map<String, List<MeasurementRow>> reviewDetailsList = new HashMap<String, List<MeasurementRow>>();

	/** The review variable list. */
	private final Map<String, List<MeasurementVariable>> reviewVariableList = new HashMap<String, List<MeasurementVariable>>();

	/** The advance map. */
	private final Map<String, AdvancingStudyForm> advanceMap = new HashMap<String, AdvancingStudyForm>();

	private final Map<String, Workbook> reviewWorkbookList = new HashMap<String, Workbook>();


	/**
	 * Adds the review details list.
	 *
	 * @param datasetId the dataset id
	 * @param rows the rows
	 */
	public void addReviewDetailsList(String datasetId, List<MeasurementRow> rows) {
		this.reviewDetailsList.put(datasetId, rows);
	}

	/**
	 * Gets the review details list.
	 *
	 * @param datasetId the dataset id
	 * @return the review details list
	 */
	public List<MeasurementRow> getReviewDetailsList(String datasetId) {
		return this.reviewDetailsList.get(datasetId);
	}

	/**
	 * Adds the review variable list.
	 *
	 * @param datasetId the dataset id
	 * @param variables the variables
	 */
	public void addReviewVariableList(String datasetId, List<MeasurementVariable> variables) {
		this.reviewVariableList.put(datasetId, variables);
	}

	/**
	 * Gets the review variable list.
	 *
	 * @param datasetId the dataset id
	 * @return the review variable list
	 */
	public List<MeasurementVariable> getReviewVariableList(String datasetId) {
		return this.reviewVariableList.get(datasetId);
	}

	/**
	 * Adds the advance details.
	 *
	 * @param id the id
	 * @param form the form
	 */
	public void addAdvanceDetails(String id, AdvancingStudyForm form) {
		this.advanceMap.put(id, form);
	}

	/**
	 * Gets the advance details.
	 *
	 * @param id the id
	 * @return the advance details
	 */
	public AdvancingStudyForm getAdvanceDetails(String id) {
		return this.advanceMap.get(id);
	}

	/**
	 * Adds the advance details.
	 *
	 * @param id the id
	 * @param form the form
	 */
	@Deprecated
	public void addReviewWorkbook(String id, Workbook workbook) {
		this.reviewWorkbookList.put(id, workbook);
	}

	/**
	 * Gets the advance details.
	 *
	 * @param id the id
	 * @return the advance details
	 */
	public Workbook getReviewWorkbook(String id) {
		return this.reviewWorkbookList.get(id);
	}
}
