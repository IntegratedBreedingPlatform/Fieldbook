package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.common.service.KsuExceIExportStudyService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.FieldbookProperty;
import com.efficio.fieldbook.web.util.KsuFieldbookUtil;
import com.efficio.fieldbook.web.util.ZipUtil;

@Service
public class KsuExcelExportStudyServiceImpl implements
		KsuExceIExportStudyService {

	@Override
	public String export(Workbook workbook, String filename, int start, int end) {
		
		String outputFilename = null;
		FileOutputStream fos = null;

        try {
        	List<String> filenameList = new ArrayList<String>();
        	for (int i = start; i <= end; i++) {
        		
	            List<MeasurementRow> observations = ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getExportArrangedObservations(), i, i);
	            List<List<String>> dataTable = KsuFieldbookUtil.convertWorkbookData(observations, workbook.getMeasurementDatasetVariables());
	
				HSSFWorkbook xlsBook = new HSSFWorkbook();
	
				if (dataTable != null && !dataTable.isEmpty()) {
					HSSFSheet xlsSheet = xlsBook.createSheet(filename.substring(0, filename.lastIndexOf(".")));
					for (int rowIndex = 0; rowIndex < dataTable.size(); rowIndex++) {
						HSSFRow xlsRow = xlsSheet.createRow(rowIndex); 
						
						for (int colIndex = 0; colIndex < dataTable.get(rowIndex).size(); colIndex++) {
							HSSFCell cell = xlsRow.createCell(colIndex);
							cell.setCellValue(dataTable.get(rowIndex).get(colIndex));
						}
					}
				}
				
				int fileExtensionIndex = filename.lastIndexOf(".");
				String filenamePath = FieldbookProperty.getPathProperty() + File.separator 
						+ filename.substring(0, fileExtensionIndex)
						+ "-" + String.valueOf(i) + filename.substring(fileExtensionIndex);
				fos = new FileOutputStream(new File(filenamePath));
				xlsBook.write(fos);
				filenameList.add(filenamePath);
        	}
        	
        	if (filenameList.size() == 1) {
        		outputFilename = filenameList.get(0);
        	}
        	else { //multi-trial instances
				outputFilename = FieldbookProperty.getPathProperty() 
						+ File.separator 
						+ filename.replaceAll(AppConstants.EXPORT_XLS_SUFFIX.getString(), "") 
						+ AppConstants.ZIP_FILE_SUFFIX.getString();
				ZipUtil.zipIt(outputFilename, filenameList);
        	}
        	
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return outputFilename;
	}

}
