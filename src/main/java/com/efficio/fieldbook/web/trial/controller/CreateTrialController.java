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
package com.efficio.fieldbook.web.trial.controller;

import java.util.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.TemplateSetting;
import org.generationcp.middleware.pojos.workbench.settings.TrialDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.TreatmentFactorDetail;
import com.efficio.fieldbook.web.nursery.controller.SettingsController;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SessionUtility;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

/**
 * The Class CreateTrialController.
 */
@Controller
@RequestMapping(CreateTrialController.URL)
public class CreateTrialController extends SettingsController {
    // TODO : rename and repurpose class to handle not just initial creation, but also editing

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CreateTrialController.class);

    /**
     * The Constant URL.
     */
    public static final String URL = "/TrialManager/createTrial";

    /**
     * The Constant URL_SETTINGS.
     */
    public static final String URL_SETTINGS = "TrialManager/templates/trialSettings";
    public static final String URL_GERMPLASM = "TrialManager/templates/germplasmDetails";
    public static final String URL_ENVIRONMENTS = "TrialManager/templates/environments";
    public static final String URL_TREATMENT = "TrialManager/templates/treatment";
    public static final String URL_EXPERIMENTAL_DESIGN = "TrialManager/templates/experimentalDesign";
    public static final String URL_MEASUREMENT = "TrialManager/templates/measurements";

    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "TrialManager/createTrial";
    }

    @RequestMapping(value = "/trialSettings", method = RequestMethod.GET)
    public String showCreateTrial(Model model, HttpSession session, HttpServletRequest req) {
        return showAjaxPage(model, URL_SETTINGS);
    }

    @ModelAttribute("programLocationURL")
    public String getProgramLocation() {
        return fieldbookProperties.getProgramLocationsUrl();
    }

    @ModelAttribute("projectID")
    public String getProgramID() {
        return getCurrentProjectId();
    }


    @RequestMapping(value = "/environment", method = RequestMethod.GET)
    public String showEnvironments(Model model, HttpSession session, HttpServletRequest req) {
        return showAjaxPage(model, URL_ENVIRONMENTS);
    }


    @RequestMapping(value = "/germplasm", method = RequestMethod.GET)
    public String showGermplasm(Model model, HttpSession session, HttpServletRequest req, @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form) {
        return showAjaxPage(model, URL_GERMPLASM);
    }


    @RequestMapping(value = "/treatment", method = RequestMethod.GET)
    public String showTreatmentFactors(Model model, HttpSession session, HttpServletRequest req) {
        return showAjaxPage(model, URL_TREATMENT);
    }


    @RequestMapping(value = "/experimentalDesign", method = RequestMethod.GET)
    public String showExperimentalDesign(Model model, HttpSession session, HttpServletRequest req) {
        return showAjaxPage(model, URL_EXPERIMENTAL_DESIGN);
    }

    @RequestMapping(value = "/measurements", method = RequestMethod.GET)
    public String showMeasurements(Model model, HttpSession session, HttpServletRequest req) {
        return showAjaxPage(model, URL_MEASUREMENT);
    }


    /**
     * Use existing Trial.
     *
     * @param form the form
     * @param trialId the Trial id
     * @param model the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(value="/trial/{trialId}", method = RequestMethod.GET)
    public String useExistingTrial(@ModelAttribute("manageSettingsForm") CreateTrialForm form, @PathVariable int trialId
            , Model model, HttpSession session) throws MiddlewareQueryException{
        if(trialId != 0){
            Workbook workbook = null;
            
            try { 
                workbook = fieldbookMiddlewareService.getTrialDataSet(trialId);
            } catch (MiddlewareQueryException e) {
                LOG.error(e.getMessage(), e);
            }
            
            userSelection.setWorkbook(workbook);
            TrialDataset dataset = (TrialDataset)SettingsUtil.convertWorkbookToXmlDataset(workbook, false);
            SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, fieldbookService, dataset, userSelection, this.getCurrentProjectId(), true);
            
            //study-level
            List<SettingDetail> trialLevelConditions = updateRequiredFields(buildRequiredVariables(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString()), 
                    buildRequiredVariablesLabel(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString(), true), 
                    buildRequiredVariablesFlag(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString()), 
                    userSelection.getStudyLevelConditions(), true, "");
            
            //plot-level
            List<SettingDetail> plotLevelConditions = updateRequiredFields(buildRequiredVariables(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()), 
                    buildRequiredVariablesLabel(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString(), false), 
                    buildRequiredVariablesFlag(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()), 
                    userSelection.getPlotsLevelList(), false, "");
            
            //trial or study level variables 
            List<SettingDetail> trialLevelVariableList = sortDefaultTrialVariables(updateRequiredFields(buildRequiredVariables(AppConstants.CREATE_TRIAL_ENVIRONMENT_REQUIRED_FIELDS.getString()), 
                    buildRequiredVariablesLabel(AppConstants.CREATE_TRIAL_ENVIRONMENT_REQUIRED_FIELDS.getString(), true), 
                    buildRequiredVariablesFlag(AppConstants.CREATE_TRIAL_ENVIRONMENT_REQUIRED_FIELDS.getString()), 
                    userSelection.getTrialLevelVariableList(), true, ""));
            
            userSelection.setStudyLevelConditions(trialLevelConditions);
            userSelection.setPlotsLevelList(plotLevelConditions);
            userSelection.setTrialLevelVariableList(trialLevelVariableList);
            form.setStudyLevelVariables(userSelection.getStudyLevelConditions());
            form.setBaselineTraitVariables(userSelection.getBaselineTraitsList());
            form.setPlotLevelVariables(userSelection.getPlotsLevelList());
            form.setTrialLevelVariables(userSelection.getTrialLevelVariableList());
            form.setTreatmentFactors(convertSettingDetailToTreatment(userSelection.getTreatmentFactors()));
            
            //build trial environment details
            List<List<ValueReference>> trialEnvList = createTrialEnvValueList(userSelection.getTrialLevelVariableList(), 1, true);
            form.setTrialEnvironmentValues(trialEnvList);
            form.setTrialInstances(1);
            form.setLoadSettings("1");
            form.setRequiredFields(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString());
        }
        setFormStaticData(form);
        model.addAttribute("createTrialForm", form);
        model.addAttribute("settingsTrialList", getTrialSettingsList());
        model.addAttribute("trialList", getTrialList());
        model.addAttribute("experimentalDesignValues", getExperimentalDesignValues());
        form.setDesignLayout(AppConstants.DESIGN_LAYOUT_INDIVIDUAL.getString());
        return super.showAjaxPage(model, URL_SETTINGS);
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
    public String show(@ModelAttribute("createTrialForm") CreateTrialForm form, @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form2, Model model, HttpServletRequest req, HttpSession session) throws MiddlewareQueryException{
    	
    	SessionUtility.clearSessionData(session, new String[]{SessionUtility.USER_SELECTION_SESSION_NAME,SessionUtility.POSSIBLE_VALUES_SESSION_NAME, SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});
    	form.setProjectId(this.getCurrentProjectId());
    	form.setRequiredFields(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString());
    	form.setFolderId(1);
    	form.setFolderName(AppConstants.PROGRAM_TRIALS.getString());
    	form.setFolderNameLabel(AppConstants.PROGRAM_TRIALS.getString());

    	setFormStaticData(form);
    	return showAngularPage(model);
    }    
    
    private List<TreatmentFactorDetail> convertSettingDetailToTreatment(List<SettingDetail> treatmentFactors) {
        List<TreatmentFactorDetail> newTreatmentFactors = new ArrayList<TreatmentFactorDetail>();
        int index = 0;
        
        for (SettingDetail settingDetail : treatmentFactors) {
            if (index%2 == 0) {
                newTreatmentFactors.add(new TreatmentFactorDetail(settingDetail.getVariable().getCvTermId(), 
                        treatmentFactors.get(index+1).getVariable().getCvTermId(), "1", 
                        treatmentFactors.get(index+1).getValue(), settingDetail.getVariable().getName(), 
                        treatmentFactors.get(index+1).getVariable().getName(), 
                        treatmentFactors.get(index+1).getVariable().getDataTypeId(),
                        treatmentFactors.get(index+1).getPossibleValuesJson(), 
                        treatmentFactors.get(index+1).getVariable().getMinRange(), 
                        treatmentFactors.get(index+1).getVariable().getMaxRange()));
                index++;
            } else {
                index++;
                continue;
            }
        }
        return newTreatmentFactors;
    }
    
    private List<List<ValueReference>> createTrialEnvValueList(List<SettingDetail> trialLevelVariableList, int trialInstances, boolean addDefault) {
        List<List<ValueReference>> trialEnvValueList = new ArrayList<List<ValueReference>>();
        for (int i=0; i<trialInstances; i++) {
            List<ValueReference> trialInstanceVariables = new ArrayList<ValueReference>();
            for (SettingDetail detail : trialLevelVariableList) {
                if (detail.getVariable().getCvTermId() != null) {
                    //set value to empty except for trial instance no.
                    if (detail.getVariable().getCvTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
                        trialInstanceVariables.add(new ValueReference(detail.getVariable().getCvTermId(), String.valueOf(i+1)));
                    } else {
                        trialInstanceVariables.add(new ValueReference(detail.getVariable().getCvTermId(), ""));
                    }
                } else {
                    trialInstanceVariables.add(new ValueReference(0, ""));
                }
            }
            trialEnvValueList.add(trialInstanceVariables);
        }
        userSelection.setTrialEnvironmentValues(trialEnvValueList);
        return trialEnvValueList;
    }
    
    private List<List<ValueReference>> createTrialEnvValueList(List<SettingDetail> trialLevelVariableList) {
        List<List<ValueReference>> trialEnvValueList = new ArrayList<List<ValueReference>>();
        List<MeasurementRow> trialObservations = userSelection.getWorkbook().getTrialObservations();        
        
        for (MeasurementRow trialObservation : trialObservations) {
            List<ValueReference> trialInstanceVariables = new ArrayList<ValueReference>();
            for (SettingDetail detail : trialLevelVariableList) {
                String headerName = WorkbookUtil.getMeasurementVariableName(userSelection.getWorkbook().getTrialVariables(), detail.getVariable().getCvTermId());
                String value = trialObservation.getMeasurementDataValue(headerName);
                trialInstanceVariables.add(new ValueReference(detail.getVariable().getCvTermId(), value));
            }
            trialEnvValueList.add(trialInstanceVariables);
        }
        userSelection.setTrialEnvironmentValues(trialEnvValueList);
        return trialEnvValueList;
    }
    
    private List<SettingDetail> sortDefaultTrialVariables(List<SettingDetail> trialLevelVariableList) {
        //set orderBy
        StringTokenizer tokenOrder = new StringTokenizer(AppConstants.TRIAL_ENVIRONMENT_ORDER.getString(), ",");
        int i=0;
        int tokenSize = tokenOrder.countTokens();
        while (tokenOrder.hasMoreTokens()) {
            String variableId = tokenOrder.nextToken();
            for (SettingDetail settingDetail : trialLevelVariableList) {
                if (settingDetail.getVariable().getCvTermId().equals(Integer.parseInt(variableId))) {
                    settingDetail.setOrder((tokenSize-i)*-1);
                }
            }
            i++;
        }

        Collections.sort(trialLevelVariableList, new  Comparator<SettingDetail>() {
            @Override
            public int compare(SettingDetail o1, SettingDetail o2) {
                    return o1.getOrder() - o2.getOrder();
            }
        });
                
        return trialLevelVariableList;
    }
    
    private List<ValueReference> getPossibleValuesOfDefaultVariable(String variableName) {
        List<ValueReference> values = new ArrayList<ValueReference>();
        variableName = variableName.toUpperCase().replace(" ", "_");
        
        StringTokenizer token = new StringTokenizer(AppConstants.getString(variableName + AppConstants.VALUES.getString()), ",");
        
        int i = 0;
        while (token.hasMoreTokens()) {
            values.add(new ValueReference(i, token.nextToken()));
            i++;
        }
        
        return values;
    }

    /**
     * Submit.
     *
     * @param form the form
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    /*
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public String submit(@ModelAttribute("createTrialForm") CreateTrialForm form, Model model) throws MiddlewareQueryException {
    	
    	String name = null;
    	for (SettingDetail nvar : form.getStudyLevelVariables()) {
    		if (nvar.getVariable() != null && nvar.getVariable().getCvTermId() != null && nvar.getVariable().getCvTermId().equals(TermId.STUDY_NAME.getId())) {
    			name = nvar.getValue();
    			break;
    		}
    	}
    	
    	form.setTrialLevelVariables(userSelection.getTrialLevelVariableList());
    	TrialDataset dataset = (TrialDataset) SettingsUtil.convertPojoToXmlDataset(fieldbookMiddlewareService, name, form.getStudyLevelVariables(), 
    			form.getPlotLevelVariables(), form.getBaselineTraitVariables(), userSelection, form.getTrialLevelVariables(), null, form.getTreatmentFactors());
    	Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset);
    	userSelection.setWorkbook(workbook);

    	if (form.getDesignLayout() != null && form.getDesignLayout().equals(AppConstants.DESIGN_LAYOUT_SAME_FOR_ALL.getString())
    			&& form.getExperimentalDesignForAll() != null) {
    		
    		for (List<ValueReference> rowValues : form.getTrialEnvironmentValues()) {
    			for (ValueReference cellValue : rowValues) {
    				if (cellValue.getId().equals(TermId.EXPERIMENT_DESIGN_FACTOR.getId())) {
    					cellValue.setName(form.getExperimentalDesignForAll());
    				}
    			}
    		}
    	}

    	if (form.getTrialEnvironmentValues() != null && !form.getTrialEnvironmentValues().isEmpty()) {
    		userSelection.getWorkbook().setTrialObservations(WorkbookUtil.createMeasurementRows(form.getTrialEnvironmentValues(), workbook.getTrialVariables()));
    	}
		
     	userSelection.setTrialEnvironmentValues(form.getTrialEnvironmentValues());
    	
    	createStudyDetails(workbook, form.getStudyLevelVariables(), form.getFolderId());
 
    	return "success";
    }
    */

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
	        studyDetails.setStudyType(StudyType.T);
	        
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
    private void setFormStaticData(CreateTrialForm form){
        form.setBreedingMethodId(AppConstants.BREEDING_METHOD_ID.getString());
        form.setLocationId(AppConstants.LOCATION_ID.getString());
        form.setBreedingMethodUrl(fieldbookProperties.getProgramBreedintMethodsUrl());
        form.setImportLocationUrl(fieldbookProperties.getGermplasmImportUrl());
        form.setStudyNameTermId(AppConstants.STUDY_NAME_ID.getString());
        form.setStartDateId(AppConstants.START_DATE_ID.getString());
    	form.setEndDateId(AppConstants.END_DATE_ID.getString());
    	form.setTrialInstanceFactor(AppConstants.TRIAL_INSTANCE_FACTOR.getString());
    	form.setReplicates(AppConstants.REPLICATES.getString());
    	form.setBlockSize(AppConstants.BLOCK_SIZE.getString());
    	form.setExperimentalDesign(AppConstants.EXPERIMENTAL_DESIGN.getString());
    	form.setOpenGermplasmUrl(fieldbookProperties.getGermplasmDetailsUrl());
    }
    
    @ModelAttribute("experimentalDesignValues")
    public List<ValueReference> getExperimentalDesignValues() throws MiddlewareQueryException {
        return fieldbookService.getAllPossibleValues(TermId.EXPERIMENT_DESIGN_FACTOR.getId());
    }

    @ModelAttribute("trialSettingsData")
    public Map<String, Object> getTrialSettingsInitialData() {
        return new HashMap<String, Object>();
    }

    @ModelAttribute("environmentData")
    public Map<String, Object> getEnvironmentInitialData() {
        return new HashMap<String, Object>();
    }

    @ModelAttribute("germplasmData")
    public Map<String, Object> getGermplasmInitialData() {
        Map<String, Object> initialData = new HashMap<String, Object>();
        Map<Integer, SettingDetail> initialDetails = new HashMap<Integer, SettingDetail>();
        List<SettingDetail> initialDetailList = new ArrayList<SettingDetail>();
        String[] initialSettingIDs = AppConstants.CREATE_TRIAL_PLOT_REQUIRED_FIELDS.getString().split(",");

        for (String initialSettingID : initialSettingIDs) {
            try {
                SettingDetail detail = createSettingDetail(Integer.valueOf(initialSettingID), null);
                initialDetails.put(detail.getVariable().getCvTermId(), detail);
                initialDetailList.add(detail);
            } catch (MiddlewareQueryException e) {
                e.printStackTrace();
            }

        }

        initialData.put("settings", initialDetails);

        if (userSelection.getPlotsLevelList() == null) {
            userSelection.setPlotsLevelList(initialDetailList);
        }

        return initialData;
    }

    @ModelAttribute("treatmentFactorsData")
    public Map<String, Object> getTreatmentFactorsInitialData() {
        return new HashMap<String, Object>();
    }

    @ModelAttribute("experimentalDesignData")
    public Map<String, Object> getExperimentalDesignInitialData() {
        return new HashMap<String, Object>();
    }

    @ModelAttribute("measurementData")
    public Map<String, Object> getMeasurementInitialData() {
        return new HashMap<String, Object>();
    }
}
