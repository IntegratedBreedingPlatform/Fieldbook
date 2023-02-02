
package com.efficio.fieldbook.web.study.service.impl;

import com.efficio.fieldbook.web.common.bean.ChangeType;
import com.efficio.fieldbook.web.study.service.ImportStudyService;
import com.efficio.fieldbook.web.util.KsuFieldbookUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Transactional
public class KsuExcelImportStudyServiceImpl extends AbstractExcelImportStudyService implements ImportStudyService {
    @Resource
    private OntologyDataManager ontologyDataManager;

    @Resource
    private ContextUtil contextUtil;

    public static final int KSU_OBSERVATION_SHEET_NUMBER = 0;

    public KsuExcelImportStudyServiceImpl(final Workbook workbook, final String currentFile, final String originalFileName){
        super(workbook, currentFile, originalFileName);
    }

    @Override
    protected void detectAddedTraitsAndPerformRename(final Set<ChangeType> modes) throws WorkbookParserException{
        final List<String> measurementHeaders =  this.getMeasurementHeaders(workbook);
        final List<String> headers = Arrays.asList(this.getColumnHeaders(this.parsedData.getSheetAt(0)));
        for (int i = 0; i < headers.size(); i++) {
            final String header = headers.get(i);
            if (measurementHeaders.contains(header) || header.equals(KsuFieldbookUtil.PLOT)) {
                continue;
            }

            final Set<StandardVariable> standardVariables =
                    ontologyDataManager.findStandardVariablesByNameOrSynonym(header, contextUtil.getCurrentProgramUUID());
            Boolean found = false;
            for (final StandardVariable standardVariable : standardVariables) {
                if (measurementHeaders.contains(standardVariable.getName())) {
                    headers.set(i, standardVariable.getName());
                    found = true;
                    break;
                }
            }

            if (!found) {
                modes.add(ChangeType.ADDED_TRAITS);
            }

        }

    }

    @Override
    public int getObservationSheetNumber() {
        // KSU format has their observation data on the first sheet
        return KSU_OBSERVATION_SHEET_NUMBER;
    }

    @Override
    void validateObservationColumns() throws WorkbookParserException {
        final Sheet observationSheet = parsedData.getSheetAt(0);
        final String[] headerNames = this.getColumnHeaders(observationSheet);
        if (!this.isValidHeaderNames(headerNames)) {
            throw new WorkbookParserException("error.workbook.import.requiredColumnsMissing");
        }
    }

    @Override
    void validateImportMetadata() throws WorkbookParserException {
        this.validateNumberOfSheets(parsedData);
    }

    protected boolean isValidHeaderNames(final String[] headerNames) {
		return KsuFieldbookUtil.isValidHeaderNames(headerNames);
	}

	protected void validateNumberOfSheets(final org.apache.poi.ss.usermodel.Workbook xlsBook) throws WorkbookParserException {
		if (xlsBook.getNumberOfSheets() != 1) {
			throw new WorkbookParserException("error.workbook.import.invalidNumberOfSheets");
		}
	}

	protected String[] getColumnHeaders(final Sheet sheet) throws WorkbookParserException {
		final Row row = sheet.getRow(0);
		final int noOfColumnHeaders = row.getLastCellNum();

		final String[] headerNames = new String[noOfColumnHeaders];
		for (int i = 0; i < noOfColumnHeaders; i++) {
			final Cell cell = row.getCell(i);
			if (cell == null) {
				throw new WorkbookParserException("error.workbook.import.missing.columns.import.file");
			}

			headerNames[i] = cell.getStringCellValue();
		}

		return headerNames;
	}

    void setOntologyDataManager(OntologyDataManager ontologyDataManager) {
        this.ontologyDataManager = ontologyDataManager;
    }

    void setContextUtil(ContextUtil contextUtil) {
        this.contextUtil = contextUtil;
    }
    
	@Override
	protected void detectAddedTraitsAndPerformRename(Set<ChangeType> modes, List<String> addedVariates,
			List<String> removedVariates) throws WorkbookParserException {
		this.detectAddedTraitsAndPerformRename(modes);
		
	}
	
}
