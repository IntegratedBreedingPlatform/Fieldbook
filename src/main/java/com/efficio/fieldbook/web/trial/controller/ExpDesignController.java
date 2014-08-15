package com.efficio.fieldbook.web.trial.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.common.service.ExperimentDesignService;
import com.efficio.fieldbook.web.common.service.RandomizeCompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableIncompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableRowColumnDesignService;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

@Controller
@RequestMapping(ExpDesignController.URL)
public class ExpDesignController extends
        BaseTrialController {

    private static final Logger LOG = LoggerFactory.getLogger(ExpDesignController.class);
    public static final String URL = "/TrialManager/experimental/design";
    
    @Resource
    private OntologyService ontologyService;
    
    @Resource
    private RandomizeCompleteBlockDesignService randomizeCompleteBlockDesign;
    @Resource
    private ResolvableIncompleteBlockDesignService resolveIncompleteBlockDesign;
    @Resource
    private ResolvableRowColumnDesignService resolvableRowColumnDesign;
    @Resource
    private ResourceBundleMessageSource messageSource;
    
    @Override
    public String getContentName() {
        return "TrialManager/openTrial";
    }
   
    @ResponseBody
    @RequestMapping(value = "/generate", method = RequestMethod.POST)
    public ExpDesignValidationOutput showMeasurements(Model model, @RequestBody ExpDesignParameterUi expDesign) {
    	/*
			0 - Resolvable Complete Block Design
			1 - Resolvable Incomplete Block Design
			2 - Resolvable Row Col
    	 */
    	//we do the conversion
         List<SettingDetail> studyLevelConditions = userSelection.getStudyLevelConditions();
         List<SettingDetail> basicDetails = userSelection.getBasicDetails();
         // transfer over data from user input into the list of setting details stored in the session
    	 List<SettingDetail> combinedList = new ArrayList<SettingDetail>();
         combinedList.addAll(basicDetails);

         if (studyLevelConditions != null) {             
             combinedList.addAll(studyLevelConditions);
         }

         String name = "";

         
    	Dataset dataset = (Dataset) SettingsUtil.convertPojoToXmlDataset(fieldbookMiddlewareService, name, combinedList,
                userSelection.getPlotsLevelList(), userSelection.getBaselineTraitsList(), userSelection, userSelection.getTrialLevelVariableList(),
                userSelection.getTreatmentFactors(), null, null, userSelection.getNurseryConditions(), false);

        Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, false);
        StudyDetails details = new StudyDetails();
        details.setStudyType(StudyType.T);
        workbook.setStudyDetails(details);
        userSelection.setTemporaryWorkbook(workbook);
        
    	int designType = expDesign.getDesignType();
    	List<ImportedGermplasm> germplasmList = userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
		
		//{designType=0, replicationsCount=4, treatmentFactors={8284={variable={cvTermId=8284, name=DAY_OBS, description=Day of observation - Not specified (Day), property=Observation time, scale=Day (dd), method=Not specified, role=Trial design information, dataType=Numeric variable, traitClass=Trial environment, cropOntologyId=, dataTypeId=1110, minRange=null, maxRange=null, widgetType=NTEXT, operation=ADD, storedInId=null}, possibleValues=[], possibleValuesFavorite=null, possibleValuesJson=null, possibleValuesFavoriteJson=null, value=null, order=0, group=null, pairedVariable=null, hidden=false, deletable=true, favorite=false}}, treatmentFactorsData={8284={levels=4, labels=[100, 200, 300, 400], pairCvTermId=8282}}}
    	ExpDesignValidationOutput expParameterOutput = new ExpDesignValidationOutput(true, "");
    	Locale locale = LocaleContextHolder.getLocale();
    	try{
    			    	
	    	//we validate here if there is gerplasm
	    	if(germplasmList == null){
	    		expParameterOutput = new ExpDesignValidationOutput(false,  messageSource.getMessage(
	                    "experiment.design.generate.no.germplasm", null, locale));
	    	}else{	    			    
		    	ExperimentDesignService designService = getExpDesignService(designType);
		    	if(designService != null){
		    		//we call the validation
		    		expParameterOutput = designService.validate(expDesign, germplasmList);
		    		//we call the actual process
		    		if(expParameterOutput.isValid()){
		    			List<MeasurementRow> measurementRows = designService.generateDesign(germplasmList, expDesign,workbook.getConditions(), workbook.getFactors(), workbook.getGermplasmFactors(), workbook.getVariates(), workbook.getTreatmentFactors());

		    			userSelection.setExpDesignParams(expDesign);
		    			userSelection.setExpDesignVariables(designService.getExperimentalDesignVariables(expDesign));
		    			
		    			workbook.setObservations(measurementRows);
		    			//should have at least 1 record
		    			List<MeasurementVariable> currentNewFactors = new ArrayList();
		    			List<MeasurementVariable> oldFactors = workbook.getFactors();
		    			List<MeasurementVariable> deletedFactors = new ArrayList();
		    			if(measurementRows != null && !measurementRows.isEmpty()){
		    				List<MeasurementVariable> measurementDatasetVariables = new ArrayList();
			    	        MeasurementRow dataRow =  measurementRows.get(0);
			    	        for(MeasurementData measurementData : dataRow.getDataList()){
			    	        	measurementDatasetVariables.add(measurementData.getMeasurementVariable());
			    	        	if(measurementData.getMeasurementVariable() != null && measurementData.getMeasurementVariable().isFactor()){
			    	        		currentNewFactors.add(measurementData.getMeasurementVariable());
			    	        	}
			    	        }
			    	        workbook.setMeasurementDatasetVariables(measurementDatasetVariables);
		    			}
		    			for(MeasurementVariable var : oldFactors){
		    				//we do the cleanup of old variables
		    				if(WorkbookUtil.getMeasurementVariable(currentNewFactors, var.getTermId()) == null){
		    					//we remove it
		    					deletedFactors.add(var);
		    				}
		    			}
		    			if(oldFactors != null){
			    			for(MeasurementVariable var : deletedFactors){
			    				oldFactors.remove(var);
			    			}
		    			}
		    			workbook.setExpDesignVariables(designService.getRequiredVariable());
		    		}
		    	}
	    	}
    	}catch(BVDesignException e){
    		//this should catch when the BV design is not successful
    		expParameterOutput = new ExpDesignValidationOutput(false,  messageSource.getMessage(
                    e.getBvErrorCode(), null, locale));
		}catch(Exception e){
			e.printStackTrace();
			expParameterOutput = new ExpDesignValidationOutput(false, messageSource.getMessage(
                    "experiment.design.invalid.generic.error", null, locale));
    	}
        
        return expParameterOutput;
    }
    
    private ExperimentDesignService getExpDesignService(int designType){
    	if(designType == 0){
    		return randomizeCompleteBlockDesign;
    	}else if(designType == 1){
    		return resolveIncompleteBlockDesign;
    	}else if(designType == 2){    		
    		return resolvableRowColumnDesign;
    	}
    	return null;
    }
}
