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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class PaginationListSelection.
 *
 * This is the session object that keeps track of list that needs to be paginated over multiple tabs
 */
public class PaginationListSelection implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2448596622077650635L;

	/** The advance map. */
	private final Map<String, AdvancingStudyForm> advanceMap = new HashMap<String, AdvancingStudyForm>();

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

}
