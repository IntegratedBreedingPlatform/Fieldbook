/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.web.nursery.controller;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.TemplateSetting;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.nursery.bean.SettingDetail;
import com.efficio.fieldbook.web.nursery.bean.SettingVariable;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class CreateNurseryController.
 */
@Controller
@RequestMapping(CreateNurseryController.URL)
public class CreateNurseryController extends SettingsController {
	
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(CreateNurseryController.class);

    /** The Constant URL. */
    public static final String URL = "/NurseryManager/createNursery";
    
    /** The Constant URL_SETTINGS. */
    public static final String URL_SETTINGS = "/NurseryManager/chooseSettings";
	
   
	/* (non-Javadoc)
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
	 */
	@Override
	public String getContentName() {
		return "NurseryManager/createNursery";
	}

    

    /**
     * Use existing nursery.
     *
     * @param form the form
     * @param nurseryId the nursery id
     * @param model the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(value="/nursery/{nurseryId}", method = RequestMethod.GET)
    public String useExistingNursery(@ModelAttribute("manageSettingsForm") CreateNurseryForm form, @PathVariable int nurseryId
            , Model model, HttpSession session) throws MiddlewareQueryException{
        if(nurseryId != 0){     
            Workbook workbook = fieldbookMiddlewareService.getNurseryVariableSettings(nurseryId);
            Dataset dataset = SettingsUtil.convertWorkbookToXmlDataset(workbook);
            SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, fieldbookService, dataset, userSelection, this.getCurrentProjectId());
            List<Integer> requiredFactors = buildRequiredFactors();
            List<String> requiredFactorsLabel = buildRequiredFactorsLabel();
            boolean[] requiredFactorsFlag = buildRequiredFactorsFlag();
            List<SettingDetail> nurseryLevelConditions = userSelection.getNurseryLevelConditions();
                    
            for(SettingDetail nurseryLevelCondition : nurseryLevelConditions){
                Integer  stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(nurseryLevelCondition.getVariable().getProperty(), 
                        nurseryLevelCondition.getVariable().getScale(), nurseryLevelCondition.getVariable().getMethod(), 
                        PhenotypicType.valueOf(nurseryLevelCondition.getVariable().getRole()));
                
                //mark required factors that are already in the list
                int ctr = 0;
                for (Integer requiredFactor: requiredFactors) {
                    if (requiredFactor.equals(stdVar)) {
                        requiredFactorsFlag[ctr] = true;
                        nurseryLevelCondition.setOrder((requiredFactors.size()-ctr)*-1);
                        nurseryLevelCondition.getVariable().setName(requiredFactorsLabel.get(ctr));
                    }
                    ctr++;
                }
            }
            
            
            //add required factors that are not in existing nursery
            for (int i = 0; i < requiredFactorsFlag.length; i++) {
                if (!requiredFactorsFlag[i]) {
                    SettingDetail newSettingDetail = createSettingDetail(requiredFactors.get(i), requiredFactorsLabel.get(i));
                    newSettingDetail.setOrder((requiredFactors.size()-i)*-1);
                    nurseryLevelConditions.add(newSettingDetail);
                }
            }
            
            //sort by required fields
            Collections.sort(nurseryLevelConditions, new  Comparator<SettingDetail>() {
                @Override
                public int compare(SettingDetail o1, SettingDetail o2) {
                        return o1.getOrder() - o2.getOrder();
                }
            });
            
            userSelection.setNurseryLevelConditions(nurseryLevelConditions);
            form.setNurseryLevelVariables(userSelection.getNurseryLevelConditions());
            form.setBaselineTraitVariables(userSelection.getBaselineTraitsList());
            form.setPlotLevelVariables(userSelection.getPlotsLevelList());
            //form.setSelectedSettingId(1);
            form.setLoadSettings("1");
            form.setRequiredFields(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString());
        }
        setFormStaticData(form);
        model.addAttribute("createNurseryForm", form);
        model.addAttribute("settingsList", getSettingsList());
        model.addAttribute("nurseryList", getNurseryList());
        //setupFormData(form);
        return super.showAjaxPage(model, URL_SETTINGS);
    }
    
    /**
     * Creates the setting detail.
     *
     * @param id the id
     * @param name the name
     * @return the setting detail
     * @throws MiddlewareQueryException the middleware query exception
     */
    private SettingDetail createSettingDetail(int id, String name) throws MiddlewareQueryException {
            String variableName = "";
            StandardVariable stdVar = getStandardVariable(id);
            if (name != null) {
                variableName = name;
            } else {
                variableName = stdVar.getName();
            }
            if (stdVar != null) {
            SettingVariable svar = new SettingVariable(
                    variableName, stdVar.getDescription(), stdVar.getProperty().getName(),
                                        stdVar.getScale().getName(), stdVar.getMethod().getName(), stdVar.getStoredIn().getName(), 
                                        stdVar.getDataType().getName(), stdVar.getDataType().getId(), 
                                        stdVar.getConstraints() != null && stdVar.getConstraints().getMinValue() != null ? stdVar.getConstraints().getMinValue() : null,
                                        stdVar.getConstraints() != null && stdVar.getConstraints().getMaxValue() != null ? stdVar.getConstraints().getMaxValue() : null);
                        svar.setCvTermId(stdVar.getId());
                        svar.setCropOntologyId(stdVar.getCropOntologyId() != null ? stdVar.getCropOntologyId() : "");
                        svar.setTraitClass(stdVar.getIsA() != null ? stdVar.getIsA().getName() : "");

                        List<ValueReference> possibleValues = fieldbookService.getAllPossibleValues(id);
                        SettingDetail settingDetail = new SettingDetail(svar, possibleValues, null, false);
                        settingDetail.setPossibleValuesToJson(possibleValues);
                        List<ValueReference> possibleValuesFavorite = fieldbookService.getAllPossibleValuesFavorite(id, this.getCurrentProjectId());
                        settingDetail.setPossibleValuesFavorite(possibleValuesFavorite);
                        settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
                        return settingDetail;
                }
                return new SettingDetail();
    }
    
    
    /**
     * Show.
     *
     * @param form the form
     * @param form2 the form2
     * @param model the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("createNurseryForm") CreateNurseryForm form, @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form2, Model model, HttpSession session) throws MiddlewareQueryException{
    	session.invalidate();
    	form.setProjectId(this.getCurrentProjectId());
    	form.setRequiredFields(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString());
    	setFormStaticData(form);
    	return super.show(model);
    }

    /**
     * View settings.
     *
     * @param form the form
     * @param templateSettingId the template setting id
     * @param model the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(value="/view/{templateSettingId}", method = RequestMethod.POST)
    public String viewSettings(@ModelAttribute("createNurseryForm") CreateNurseryForm form, @PathVariable int templateSettingId, 
    	Model model, HttpSession session) throws MiddlewareQueryException{
    	
    	if(templateSettingId != 0){    	
	    	TemplateSetting templateSettingFilter = new TemplateSetting(Integer.valueOf(templateSettingId), Integer.valueOf(getCurrentProjectId()), null, getNurseryTool(), null, null);
	    	templateSettingFilter.setIsDefaultToNull();
	    	List<TemplateSetting> templateSettings = workbenchService.getTemplateSettings(templateSettingFilter);
	    	TemplateSetting templateSetting = templateSettings.get(0); //always 1
	    	Dataset dataset = SettingsUtil.parseXmlToDatasetPojo(templateSetting.getConfiguration());
	    	userSelection.setDataset(dataset);
	    	SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, fieldbookService, dataset, userSelection, this.getCurrentProjectId());
	    	form.setNurseryLevelVariables(userSelection.getNurseryLevelConditions());
	    	form.setBaselineTraitVariables(userSelection.getBaselineTraitsList());
	    	form.setPlotLevelVariables(userSelection.getPlotsLevelList());
//	    	form.setIsDefault(templateSetting.getIsDefault().intValue() == 1 ? true : false);
//	    	form.setSettingName(templateSetting.getName());
	    	form.setSelectedSettingId(templateSetting.getTemplateSettingId());
	    	form.setRequiredFields(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString());
//    	}else{
//    		assignDefaultValues(form);
    	}
//    	model.addAttribute("createNurseryForm", form);
//    	model.addAttribute("settingsList", getSettingsList());
    	form.setLoadSettings("1");
    	setFormStaticData(form);
        return super.showAjaxPage(model, URL_SETTINGS );
    }

    /**
     * Submit.
     *
     * @param form the form
     * @param model the model
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public String submit(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model) throws MiddlewareQueryException {
    	
    	String name = null;
    	for (SettingDetail nvar : form.getNurseryLevelVariables()) {
    		if (nvar.getVariable() != null && nvar.getVariable().getCvTermId() != null && nvar.getVariable().getCvTermId().equals(TermId.STUDY_NAME.getId())) {
    			name = nvar.getValue();
    			break;
    		}
    	}
    	System.out.println("NAME IS " + name);
    	
    	Dataset dataset = SettingsUtil.convertPojoToXmlDataset(fieldbookMiddlewareService, name, form.getNurseryLevelVariables(), form.getPlotLevelVariables(), form.getBaselineTraitVariables(), userSelection);
//    	Dataset dataset = userSelection.getDataset();
    	Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset);
    	userSelection.setWorkbook(workbook);

    	
    	createStudyDetails(workbook, form.getNurseryLevelVariables(), form.getFolderId());
 
    	return "success";
    }
    
    /**
     * Creates the study details.
     *
     * @param workbook the workbook
     * @param conditions the conditions
     * @param folderId the folder id
     */
    private void createStudyDetails(Workbook workbook, List<SettingDetail> conditions, Integer folderId) {
        if (workbook.getStudyDetails() == null) {
            workbook.setStudyDetails(new StudyDetails());
        }
        StudyDetails studyDetails = workbook.getStudyDetails();

        if (conditions != null && !conditions.isEmpty()) {
	        studyDetails.setTitle(getSettingDetailValue(conditions, TermId.STUDY_TITLE.getId()));
	        studyDetails.setObjective(getSettingDetailValue(conditions, TermId.STUDY_OBJECTIVE.getId()));
	        studyDetails.setStudyName(getSettingDetailValue(conditions, TermId.STUDY_NAME.getId()));
	        studyDetails.setStudyType(StudyType.N);
	        
	        if (folderId != null) {
	        	studyDetails.setParentFolderId(folderId);
	        }
    	}
        studyDetails.print(1);
    }
    
    /**
     * Gets the setting detail value.
     *
     * @param details the details
     * @param termId the term id
     * @return the setting detail value
     */
    private String getSettingDetailValue(List<SettingDetail> details, int termId) {
    	String value = null;
    	
    	for (SettingDetail detail : details) {
    		if (detail.getVariable().getCvTermId().equals(termId)) {
    			value = detail.getValue();
    			break;
    		}
    	}
    	
    	return value;
    }
    
    /**
     * Sets the form static data.
     *
     * @param form the new form static data
     */
    private void setFormStaticData(CreateNurseryForm form){
        form.setBreedingMethodId(AppConstants.BREEDING_METHOD_ID.getString());
        form.setLocationId(AppConstants.LOCATION_ID.getString());
        form.setBreedingMethodUrl(AppConstants.BREEDING_METHOD_URL.getString());
        form.setLocationUrl(AppConstants.LOCATION_URL.getString());
        form.setProjectId(this.getCurrentProjectId());
        form.setImportLocationUrl(AppConstants.IMPORT_GERMPLASM_URL.getString());
        form.setStudyNameTermId(AppConstants.STUDY_NAME_ID.getString());
    }
}
