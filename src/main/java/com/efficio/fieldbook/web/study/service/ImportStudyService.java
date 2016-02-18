/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.study.service;

import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.WorkbookParserException;

import com.efficio.fieldbook.web.common.bean.ImportResult;

public interface ImportStudyService {

	ImportResult importWorkbook(Workbook workbook, String currentFile, String originalFileName) throws WorkbookParserException;
}
