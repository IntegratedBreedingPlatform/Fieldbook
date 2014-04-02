package com.efficio.fieldbook.web.common.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.StudySelection;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.service.ExcelExportStudyService;
import com.efficio.fieldbook.web.nursery.service.FieldroidExportStudyService;
import com.efficio.fieldbook.web.nursery.service.RExportStudyService;
import com.efficio.fieldbook.web.trial.bean.TrialSelection;
import com.efficio.fieldbook.web.util.AppConstants;

@Controller
@RequestMapping(ExportStudyController.URL)
public class ExportStudyController extends AbstractBaseFieldbookController {

    private static final Logger LOG = LoggerFactory.getLogger(ExportStudyController.class);
    public static final String URL = "/ExportManager";
    private static final int BUFFER_SIZE = 4096 * 4;

    @Resource
    private UserSelection nurserySelection;
    
    @Resource
    private TrialSelection trialSelection;
    
    @Resource
    private FieldroidExportStudyService fielddroidExportStudyService;
    
    @Resource
    private RExportStudyService rExportStudyService;
    
    @Resource
    private ExcelExportStudyService excelExportStudyService;
    
    @Override
	public String getContentName() {
		return null;
	}

    @ResponseBody
    @RequestMapping(value="/export/{studyType}/{exportType}/{selectedTraitTermId}", method = RequestMethod.GET)
    public String exportRFileForNursery(@PathVariable String studyType, @PathVariable int exportType, @PathVariable int selectedTraitTermId, HttpServletResponse response) {
    	boolean isTrial = studyType.equalsIgnoreCase("TRIAL");
    	return doExport(exportType, selectedTraitTermId, response, isTrial);
    	
    }
    
    @ResponseBody
    @RequestMapping(value="/export/{studyType}/{exportType}", method = RequestMethod.GET)
    public String exportFile(@PathVariable String studyType, @PathVariable int exportType, HttpServletResponse response) {
    	boolean isTrial = studyType.equalsIgnoreCase("TRIAL");
        return doExport(exportType, 0, response, isTrial);
    	
    }
   
    
    /**
     * Do export.
     *
     * @param exportType the export type
     * @param selectedTraitTermId the selected trait term id
     * @param response the response
     * @return the string
     */
    private String doExport(int exportType, int selectedTraitTermId, HttpServletResponse response, boolean isTrial){
    	StudySelection userSelection = getUserSelection(isTrial);
    	String filename = userSelection.getWorkbook().getStudyDetails().getStudyName();
    	if(AppConstants.EXPORT_NURSERY_FIELDLOG_FIELDROID.getInt() == exportType){
    		filename = filename  + AppConstants.EXPORT_FIELDLOG_SUFFIX.getString();
    		fielddroidExportStudyService.export(userSelection.getWorkbook(), filename);
    		response.setContentType("text/csv");
    	}else if(AppConstants.EXPORT_NURSERY_R.getInt() == exportType){
    		filename = filename  + AppConstants.EXPORT_R_SUFFIX.getString();
    		rExportStudyService.exportToR(userSelection.getWorkbook(), filename, selectedTraitTermId);    		
    		response.setContentType("text/csv");
    	}else if(AppConstants.EXPORT_NURSERY_EXCEL.getInt() == exportType){
    		filename = filename  + AppConstants.EXPORT_XLS_SUFFIX.getString();
    		excelExportStudyService.export(userSelection.getWorkbook(), filename);
    		response.setContentType("application/vnd.ms-excel");
    	}
    	        
        File xls = new File(filename); // the selected name + current date
        FileInputStream in;
        
        response.setHeader("Content-disposition","attachment; filename=" + filename);
        try {
            in = new FileInputStream(xls);
            OutputStream out = response.getOutputStream();

            byte[] buffer= new byte[BUFFER_SIZE]; // use bigger if you want
            int length = 0;

            while ((length = in.read(buffer)) > 0){
                 out.write(buffer, 0, length);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
        	LOG.error(e.getMessage(), e);
        } catch (IOException e) {
        	LOG.error(e.getMessage(), e);
        }
       
        return "";
    }
    
    private StudySelection getUserSelection(boolean isTrial) {
    	return isTrial ? this.trialSelection : this.nurserySelection;
    }
}
