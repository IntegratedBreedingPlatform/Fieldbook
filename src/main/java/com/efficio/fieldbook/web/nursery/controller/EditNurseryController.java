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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.math.NumberUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.context.ContextConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.commons.util.ContextUtil;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.WebUtils;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.DateUtil;
import com.efficio.fieldbook.web.util.SessionUtility;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class CreateNurseryController.
 */
@Controller
@RequestMapping(EditNurseryController.URL)
public class EditNurseryController extends SettingsController {
	
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(EditNurseryController.class);

    /** The Constant URL. */
    public static final String URL = "/NurseryManager/editNursery";
    
    /** The Constant URL_SETTINGS. */
    public static final String URL_SETTINGS = "/NurseryManager/addOrRemoveTraits";
    
    /** The ontology service. */
    @Resource
    private OntologyService ontologyService;
	
    /** The fieldbook service. */
    @Resource
    private FieldbookService fieldbookService;
   
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
	return "NurseryManager/editNursery";
    }

    /**
     * Use existing nursery.
     *
     * @param form the form
     * @param form2 the form2
     * @param nurseryId the nursery id
     * @param model the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(value="/{nurseryId}", method = RequestMethod.GET)
    public String useExistingNursery(@ModelAttribute("createNurseryForm") CreateNurseryForm form, 
    		@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form2, 
            @PathVariable int nurseryId,@RequestParam(required=false) String isAjax, 
            Model model, HttpServletRequest req, HttpSession session, HttpServletRequest request) throws MiddlewareQueryException{
    	
    	ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO); 
    	String contextParams = ContextUtil.getContextParameterString(contextInfo);
    	
    	SessionUtility.clearSessionData(session, new String[]{SessionUtility.USER_SELECTION_SESSION_NAME,SessionUtility.POSSIBLE_VALUES_SESSION_NAME, SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});
    	
    	Workbook workbook = null;
        if(nurseryId != 0){     
            //settings part
            workbook = fieldbookMiddlewareService.getNurseryDataSet(nurseryId);

            form.setMeasurementDataExisting(fieldbookMiddlewareService.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(), SettingsUtil.buildVariates(workbook.getVariates())));
            
            Dataset dataset = (Dataset)SettingsUtil.convertWorkbookToXmlDataset(workbook);
            
            SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, fieldbookService, dataset, userSelection, this.getCurrentProjectId(), false, false);
            
            //nursery-level
            List<SettingDetail> nurseryLevelConditions = updateRequiredFields(buildVariableIDList(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString()),
                    buildRequiredVariablesLabel(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString(), true), 
                    buildRequiredVariablesFlag(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString()), 
                    userSelection.getStudyLevelConditions(), false, AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());
            
            List<SettingDetail> basicDetails = getBasicDetails(nurseryLevelConditions, form);
            
            SettingsUtil.removeBasicDetailsVariables(nurseryLevelConditions);
            
            userSelection.setBasicDetails(basicDetails);
            form.setStudyId(nurseryId);
            form.setBasicDetails(userSelection.getBasicDetails());
            form.setStudyLevelVariables(userSelection.getStudyLevelConditions());
            form.setBaselineTraitVariables(userSelection.getBaselineTraitsList());
            form.setSelectionVariatesVariables(userSelection.getSelectionVariates());
            
            form.setNurseryConditions(userSelection.getNurseryConditions());
            //form.setSelectedSettingId(1);
            form.setLoadSettings("1");
            form.setFolderId(Integer.valueOf((int)workbook.getStudyDetails().getParentFolderId()));
            if (form.getFolderId() == 1) {
            	if (nurseryId > 0) {
            		form.setFolderName(AppConstants.PUBLIC_NURSERIES.getString());
            	}
            	else {
            		form.setFolderName(AppConstants.PROGRAM_NURSERIES.getString());
            	}
            }
            else {
            	form.setFolderName(fieldbookMiddlewareService.getFolderNameById(form.getFolderId()));
            }
            
            
            //measurements part
            if (workbook != null) {
                SettingsUtil.resetBreedingMethodValueToId(fieldbookMiddlewareService, workbook.getObservations(), false, ontologyService);
            	setMeasurementsData(form, workbook);
            }
            
            //make factors uneditable if experiments exist already
            if (form.isMeasurementDataExisting()) {
                for (SettingDetail setting : userSelection.getPlotsLevelList()) {
                    setting.setDeletable(false);
                }
            }
            
            form.setPlotLevelVariables(userSelection.getPlotsLevelList());
        }
        setFormStaticData(form, contextParams, workbook);
        model.addAttribute("createNurseryForm", form);
        
        List<GermplasmList> germplasmList = new ArrayList();
        for(int i = -4 ; i < 0 ; i++){
        	GermplasmList temp = new GermplasmList();
        	temp.setId(i);
        	temp.setName("Temp " + i);
        	germplasmList.add(temp);
        }
        model.addAttribute("advancedList", germplasmList);
        
        if(isAjax != null && isAjax.equalsIgnoreCase("1")) {
        	return super.showAjaxPage(model, getContentName());
        }
        
        return super.show(model);
    }
    
    /**
     * Sets the measurements data.
     *
     * @param form the form
     * @param workbook the workbook
     */
    private void setMeasurementsData(CreateNurseryForm form, Workbook workbook) {
    	userSelection.setMeasurementRowList(workbook.getObservations());
        form.setMeasurementRowList(userSelection.getMeasurementRowList());
        form.setMeasurementVariables(workbook.getMeasurementDatasetVariables());
        form.setStudyName(workbook.getStudyDetails().getStudyName());
        form.changePage(1);
        userSelection.setCurrentPage(form.getCurrentPage());
        userSelection.setWorkbook(workbook);
        userSelection.setTemporaryWorkbook(null);
    }
    
    /**
     * Gets the basic details.
     *
     * @param nurseryLevelConditions the nursery level conditions
     * @return the basic details
     */
    private List<SettingDetail> getBasicDetails(List<SettingDetail> nurseryLevelConditions, CreateNurseryForm form) {
        List<SettingDetail> basicDetails = new ArrayList<SettingDetail>();
        
        StringTokenizer token = new StringTokenizer(AppConstants.FIXED_NURSERY_VARIABLES.getString(), ",");
        while(token.hasMoreTokens()){
            Integer termId = Integer.valueOf(token.nextToken());
            boolean isFound = false;
            for (SettingDetail setting : nurseryLevelConditions) {
                if (termId.equals(setting.getVariable().getCvTermId())) {
                    isFound = true;
                    if (termId.equals(Integer.valueOf(TermId.STUDY_UID.getId()))) {
                        try {
                        	if (setting.getValue() != null && !setting.getValue().isEmpty() && NumberUtils.isNumber(setting.getValue())) {
                        		form.setCreatedBy(fieldbookService.getPersonById(Integer.parseInt(setting.getValue())));
                        	}
                        }
                        catch (MiddlewareQueryException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    } else if (termId.equals(Integer.valueOf(TermId.STUDY_UPDATE.getId()))) {
                        DateFormat dateFormat = new SimpleDateFormat(DateUtil.DB_DATE_FORMAT);
                        Date date = new Date();
                        setting.setValue(dateFormat.format(date));
                    }
                    basicDetails.add(setting);
                }
            }  
            if(!isFound){
                try {
                    basicDetails.add(createSettingDetail(termId, null));
                    if (termId.equals(Integer.valueOf(TermId.STUDY_UID.getId()))) {
                        try {
                            form.setCreatedBy(fieldbookService.getPersonById(this.getCurrentIbdbUserId()));
                        }
                        catch (MiddlewareQueryException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                } catch (MiddlewareQueryException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        
        return basicDetails;
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
    public String show(@ModelAttribute("createNurseryForm") CreateNurseryForm form,
    				@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form2, 
    				Model model, HttpServletRequest req, HttpSession session, HttpServletRequest request) throws MiddlewareQueryException {
    	
    	ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO); 
    	String contextParams = ContextUtil.getContextParameterString(contextInfo);
    	SessionUtility.clearSessionData(session, new String[]{SessionUtility.USER_SELECTION_SESSION_NAME,SessionUtility.POSSIBLE_VALUES_SESSION_NAME, SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});
    	setFormStaticData(form, contextParams, new Workbook());
    	assignDefaultValues(form);
    	return super.show(model);
    }
    
    /**
     * Assign default values.
     *
     * @param form the form
     * @throws MiddlewareQueryException the middleware query exception
     */
    private void assignDefaultValues(CreateNurseryForm form) throws MiddlewareQueryException {
        List<SettingDetail> basicDetails = new ArrayList<SettingDetail>();
        List<SettingDetail> nurseryDefaults = new ArrayList<SettingDetail>();
        List<SettingDetail> plotDefaults = new ArrayList<SettingDetail>();
        List<SettingDetail> baselineTraitsList = new ArrayList<SettingDetail>();
        List<SettingDetail> nurseryConditions = new ArrayList<SettingDetail>();
        
        basicDetails = buildDefaultVariables(basicDetails, AppConstants.FIXED_NURSERY_VARIABLES.getString(), buildRequiredVariablesLabel(AppConstants.FIXED_NURSERY_VARIABLES.getString(), false));
        form.setBasicDetails(basicDetails);
        form.setStudyLevelVariables(nurseryDefaults);
        form.setPlotLevelVariables(plotDefaults);
        nurseryDefaults = buildDefaultVariables(nurseryDefaults, AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString(), buildRequiredVariablesLabel(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString(), true));
        plotDefaults = buildDefaultVariables(plotDefaults, AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString(), buildRequiredVariablesLabel(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString(), false));
        
        this.userSelection.setBasicDetails(basicDetails);
        this.userSelection.setStudyLevelConditions(nurseryDefaults);
        this.userSelection.setPlotsLevelList(plotDefaults);
        this.userSelection.setBaselineTraitsList(baselineTraitsList);
        this.userSelection.setNurseryConditions(nurseryConditions);
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
    public Map<String, String> submit(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model) throws MiddlewareQueryException {
        //get the name of the nursery
    	String name = null;
    	for (SettingDetail nvar : form.getBasicDetails()) {
    		if (nvar.getVariable() != null && nvar.getVariable().getCvTermId() != null && nvar.getVariable().getCvTermId().equals(TermId.STUDY_NAME.getId())) {
    			name = nvar.getValue();
    			break;
    		}
    	}

    	//combine all study conditions (basic details and management details and hidden variables)
    	List<SettingDetail> studyLevelVariables = new ArrayList<SettingDetail>();
    	if (form.getStudyLevelVariables() != null && !form.getStudyLevelVariables().isEmpty()) {
    		studyLevelVariables.addAll(form.getStudyLevelVariables());
    	}
    	studyLevelVariables.addAll(form.getBasicDetails());
    	    	 
    	List<SettingDetail> studyLevelVariablesSession = userSelection.getBasicDetails();
    	userSelection.getStudyLevelConditions().addAll(studyLevelVariablesSession);
    	if (userSelection.getRemovedConditions() != null) {
    	    studyLevelVariables.addAll(userSelection.getRemovedConditions());
    	    userSelection.getStudyLevelConditions().addAll(userSelection.getRemovedConditions());
    	}
    	    	
    	//add hidden variables like OCC in factors list
    	if (userSelection.getRemovedFactors() != null) {
    		form.getPlotLevelVariables().addAll(userSelection.getRemovedFactors());
    		userSelection.getPlotsLevelList().addAll(userSelection.getRemovedFactors());
    	}
    	
    	//combine all variates (traits and selection variates)
    	List<SettingDetail> baselineTraits = form.getBaselineTraitVariables();
    	List<SettingDetail> baselineTraitsSession = userSelection.getSelectionVariates();
    	if (baselineTraits == null) {
    	    baselineTraits = form.getSelectionVariatesVariables();
    	    userSelection.getBaselineTraitsList().addAll(baselineTraitsSession);
    	} else if (form.getSelectionVariatesVariables() != null) {
    	    baselineTraits.addAll(form.getSelectionVariatesVariables());
    	    userSelection.getBaselineTraitsList().addAll(baselineTraitsSession);
    	}

    	if (form.getPlotLevelVariables() == null) {
    		form.setPlotLevelVariables(new ArrayList<SettingDetail>());
    	}
    	if (baselineTraits == null) {
    		baselineTraits = new ArrayList<SettingDetail>();
    	}
    	if (form.getNurseryConditions() == null) {
    		form.setNurseryConditions(new ArrayList<SettingDetail>());
    	}
    	
    	//include deleted list if measurements are available
        SettingsUtil.addDeletedSettingsList(studyLevelVariables, userSelection.getDeletedStudyLevelConditions(),  
    	    userSelection.getStudyLevelConditions());
        SettingsUtil.addDeletedSettingsList(form.getPlotLevelVariables(), userSelection.getDeletedPlotLevelList(), 
    	    userSelection.getPlotsLevelList());
        SettingsUtil.addDeletedSettingsList(baselineTraits, userSelection.getDeletedBaselineTraitsList(), 
    	    userSelection.getBaselineTraitsList());
        SettingsUtil.addDeletedSettingsList(form.getNurseryConditions(), userSelection.getDeletedNurseryConditions(), 
            userSelection.getNurseryConditions());
        
		int trialDatasetId = userSelection.getWorkbook().getTrialDatasetId();
	    //retain measurement dataset id
	    int measurementDatasetId = userSelection.getWorkbook().getMeasurementDatesetId(); 

    	Dataset dataset = (Dataset)SettingsUtil.convertPojoToXmlDataset(fieldbookMiddlewareService, name, studyLevelVariables, 
    	        form.getPlotLevelVariables(), baselineTraits, userSelection, form.getNurseryConditions());
    	Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, true);
    	workbook.setOriginalObservations(userSelection.getWorkbook().getOriginalObservations());
    	workbook.setTrialDatasetId(trialDatasetId);
    	workbook.setMeasurementDatesetId(measurementDatasetId);
    	workbook.setTrialObservations(userSelection.getWorkbook().getTrialObservations());
    	setTrialObservationsFromVariables(workbook);
    	    	
    	this.createStudyDetails(workbook, form.getBasicDetails(), form.getFolderId(), form.getStudyId());
    	userSelection.setWorkbook(workbook);
    	        
    	Map<String, String> resultMap = new HashMap<String, String>();
    	//saving of measurement rows
    	if (userSelection.getMeasurementRowList() != null && userSelection.getMeasurementRowList().size() > 0) {
            try {
                WorkbookUtil.addMeasurementDataToRows(workbook.getFactors(), false, userSelection, ontologyService, fieldbookService);
                WorkbookUtil.addMeasurementDataToRows(workbook.getVariates(), true, userSelection, ontologyService, fieldbookService);
                
                workbook.setMeasurementDatasetVariables(null);
                form.setMeasurementRowList(userSelection.getMeasurementRowList());
                form.setMeasurementVariables(userSelection.getWorkbook().getMeasurementDatasetVariables());
                workbook.setObservations(form.getMeasurementRowList());
                
                userSelection.setWorkbook(workbook);
                //validationService.validateObservationValues(workbook);
                
                fieldbookService.createIdCodeNameVariablePairs(userSelection.getWorkbook(), AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());
                fieldbookService.createIdNameVariablePairs(userSelection.getWorkbook(), userSelection.getRemovedConditions(), AppConstants.ID_NAME_COMBINATION.getString(), true);
                fieldbookMiddlewareService.saveMeasurementRows(workbook);
                workbook.setTrialObservations(
                		fieldbookMiddlewareService.buildTrialObservations(trialDatasetId, workbook.getTrialConditions(), workbook.getTrialConstants()));
                workbook.setOriginalObservations(workbook.getObservations());
                
                resultMap.put("status", "1");
                resultMap.put("hasMeasurementData", String.valueOf(fieldbookMiddlewareService.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(), SettingsUtil.buildVariates(workbook.getVariates()))));
            } catch (MiddlewareQueryException e) {
                LOG.error(e.getMessage());
                resultMap.put("status", "-1");
                resultMap.put("errorMessage", e.getMessage());
            }
            return resultMap;
    	} else {
    	    resultMap.put("status", "1");
    	    return resultMap;
    	}
    	
    }
    
    private void setTrialObservationsFromVariables(Workbook workbook) {
    	if (workbook.getTrialObservations() != null && !workbook.getTrialObservations().isEmpty()) {
    		if (workbook.getTrialConditions() != null && !workbook.getTrialConditions().isEmpty()) {
    			for (MeasurementVariable condition : workbook.getTrialConditions()) {
    				for (MeasurementData data : workbook.getTrialObservations().get(0).getDataList()) {
    					if (data.getMeasurementVariable().getTermId() == condition.getTermId()) {
    						data.setValue(condition.getValue());
    					}
    				}
    			}
    		}
    		if (workbook.getTrialConstants() != null && !workbook.getTrialConditions().isEmpty()) {
    			for (MeasurementVariable constant : workbook.getTrialConstants()) {
    				for (MeasurementData data : workbook.getTrialObservations().get(0).getDataList()) {
    					if (data.getMeasurementVariable().getTermId() == constant.getTermId()) {
    						data.setValue(constant.getValue());
    					}
    				}
    			}
    		}
    	}
    }
          
    /**
     * Sets the form static data.
     *
     * @param form the new form static data
     */
    private void setFormStaticData(CreateNurseryForm form, String contextParams, Workbook workbook){
        form.setBreedingMethodId(AppConstants.BREEDING_METHOD_ID.getString());
        form.setLocationId(AppConstants.LOCATION_ID.getString());
        form.setBreedingMethodUrl(fieldbookProperties.getProgramBreedingMethodsUrl());
        form.setLocationUrl(fieldbookProperties.getProgramLocationsUrl());
        form.setProjectId(this.getCurrentProjectId());
        form.setImportLocationUrl(fieldbookProperties.getGermplasmImportUrl() + "?" + contextParams);
        form.setStudyNameTermId(AppConstants.STUDY_NAME_ID.getString());
        form.setStartDateId(AppConstants.START_DATE_ID.getString());
    	form.setEndDateId(AppConstants.END_DATE_ID.getString());
    	form.setOpenGermplasmUrl(fieldbookProperties.getGermplasmDetailsUrl());
    	form.setBaselineTraitsSegment(AppConstants.SEGMENT_TRAITS.getString());
    	form.setSelectionVariatesSegment(AppConstants.SEGMENT_SELECTION_VARIATES.getString());
    	form.setCharLimit(Integer.parseInt(AppConstants.CHAR_LIMIT.getString()));
    	form.setRequiredFields(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString() + "," + AppConstants.FIXED_NURSERY_VARIABLES.getString());
        form.setProjectId(this.getCurrentProjectId());
        form.setIdNameVariables(AppConstants.ID_NAME_COMBINATION.getString());
        form.setBreedingMethodCode(AppConstants.BREEDING_METHOD_CODE.getString());
        Integer datasetId = workbook.getMeasurementDatesetId();
        try {
            if (datasetId == null) {
                datasetId = fieldbookMiddlewareService.getMeasurementDatasetId(workbook.getStudyId(), workbook.getStudyName());
            }
            form.setHasFieldmap(fieldbookMiddlewareService.hasFieldMap(datasetId));
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
    }
    
    /**
     * Check measurement data.
     *
     * @param form the form
     * @param model the model
     * @param mode the mode
     * @param variableId the variable id
     * @return the map
     */
    @ResponseBody
    @RequestMapping(value = "/checkMeasurementData/{mode}/{variableId}", method = RequestMethod.GET)
    public Map<String, String> checkMeasurementData(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model, 
            @PathVariable int mode, @PathVariable int variableId) {
        Map<String, String> resultMap = new HashMap<String, String>();
        boolean hasData = false;
        
        //if there are measurement rows, check if values are already entered
        if (userSelection.getMeasurementRowList() != null && !userSelection.getMeasurementRowList().isEmpty()) {
            for (MeasurementRow row: userSelection.getMeasurementRowList()) {
                for (MeasurementData data: row.getDataList()) {
                    if (data.getMeasurementVariable().getTermId() == variableId && data.getValue() != null && !data.getValue().isEmpty()) {
                        hasData = true;
                        break;
                    }
                }
                if (hasData) break;
            }
        }

        if (hasData)
            resultMap.put("hasMeasurementData", "1");
        else 
            resultMap.put("hasMeasurementData", "0");
        
        return resultMap;
    }
                        
    /**
     * Reset session variables after save.
     *
     * @param form the form
     * @param model the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(value="/recreate/session/variables", method = RequestMethod.GET)
    public String resetSessionVariablesAfterSave(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model, 
    		HttpSession session, HttpServletRequest request) throws MiddlewareQueryException{
    	
    	ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO); 
    	String contextParams = ContextUtil.getContextParameterString(contextInfo);

    	Workbook workbook = userSelection.getWorkbook();
        form.setMeasurementDataExisting(fieldbookMiddlewareService.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(), SettingsUtil.buildVariates(workbook.getVariates())));
    	
        resetSessionVariablesAfterSave(workbook, true);

    	//set measurement session variables to form
    	setMeasurementsData(form, workbook);        
    	setFormStaticData(form, contextParams, workbook);
        model.addAttribute("createNurseryForm", form);
    	
        return super.showAjaxPage(model, URL_SETTINGS);
    }
    
    /**
     * Show variable details.
     *
     * @param id the id
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value="/showVariableDetails/{id}", method = RequestMethod.GET)
    public String showVariableDetails(@PathVariable int id) {
    	try {

    		SettingVariable svar = getSettingVariable(id);
    		if (svar != null) {
    			ObjectMapper om = new ObjectMapper();
    			return om.writeValueAsString(svar);
    		}
    		
    	} catch(Exception e) {
    		LOG.error(e.getMessage(), e);
    	}
    	return "[]";
    }
        
    @ResponseBody
    @RequestMapping(value="/deleteMeasurementRows", method = RequestMethod.POST)
    public Map<String, String> deleteMeasurementRows() {
        Map<String, String> resultMap = new HashMap<String, String>();
        
        try {
            fieldbookMiddlewareService.deleteObservationsOfStudy(userSelection.getWorkbook().getMeasurementDatesetId());
            resultMap.put("status", "1");
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage());
            resultMap.put("status", "-1");
            resultMap.put("errorMessage", e.getMessage());   
        }
        
        userSelection.setMeasurementRowList(null);
        userSelection.getWorkbook().setOriginalObservations(null);
        userSelection.getWorkbook().setObservations(null);
        return resultMap;
    }
    
    @ModelAttribute("nameTypes")
    public List<UserDefinedField> getNameTypes(){
        try {
            List<UserDefinedField> nameTypes = fieldbookMiddlewareService.getGermplasmNameTypes();
            
            return nameTypes;
        }catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }

        return null;
    }

    @ModelAttribute("programLocationURL")
    public String getProgramLocation() {
        return fieldbookProperties.getProgramLocationsUrl();
    }
    
    @ModelAttribute("programMethodURL")
    public String getProgramMethod() {
        return fieldbookProperties.getProgramBreedingMethodsUrl();
    }

    @ModelAttribute("projectID")
    public String getProgramID() {
        return getCurrentProjectId();
    }
}
