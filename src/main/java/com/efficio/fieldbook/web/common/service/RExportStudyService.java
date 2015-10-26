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

package com.efficio.fieldbook.web.common.service;

import java.io.IOException;
import java.util.List;

import org.generationcp.middleware.domain.etl.Workbook;

public interface RExportStudyService extends ExportStudyService {

	String exportToR(Workbook workbook, String filename, Integer selectedTrait, List<Integer> instances) throws IOException;
}
