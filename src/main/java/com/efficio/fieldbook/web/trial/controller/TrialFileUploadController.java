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


import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.form.FileUploadForm;
import com.efficio.fieldbook.web.util.SessionUtility;
import com.efficio.fieldbook.web.util.ToolUtil;

/**
 * The Class FileUploadController.
 */
@Controller
@RequestMapping(TrialFileUploadController.URL)
public class TrialFileUploadController extends AbstractBaseFieldbookController{
    
    private static final Logger LOG = LoggerFactory.getLogger(TrialFileUploadController.class);
    
    /** The Constant URL. */
    public static final String URL = "/TrialManager/fileUpload";
    
    /** The fieldbook service. */
    @Resource
    private FieldbookService fieldbookService;
    
    @Resource
    private UserSelection userSelection;
		
    /**
     * Shows the file upload screen
     *
     * @param uploadForm the upload form
     * @param model the model
     * @param session the session
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("fileUploadForm") FileUploadForm uploadForm
            , Model model, HttpServletRequest req, HttpSession session) {
    	SessionUtility.clearSessionData(session, new String[]{SessionUtility.USER_SELECTION_SESSION_NAME,SessionUtility.POSSIBLE_VALUES_SESSION_NAME});
    	
    	try {
    	    ToolUtil toolUtil = new ToolUtil();
    	    toolUtil.launchNativeTool(this.getOldFieldbookPath(), "--ibpApplication=IBFieldbookTools");
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    	
    	return super.show(model);
    }
    
    @RequestMapping(value="/newTrial", method = RequestMethod.GET)
    public String openTrial(@ModelAttribute("fileUploadForm") FileUploadForm uploadForm
            , Model model, HttpSession session) {
       
        try {
            ToolUtil toolUtil = new ToolUtil();
            toolUtil.launchNativeTool(this.getOldFieldbookPath(), "--ibpApplication=IBFieldbookTools");
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        
        return super.show(model);
    }

    
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "TrialManager/fileUpload";
    }
    
    /**
     * Gets the form.
     *
     * @return the form
     */
    @ModelAttribute("form")
    public FileUploadForm getForm() {
        return new FileUploadForm();
    }
    
    /**
     * Sets the fieldbook service.
     *
     * @param fieldbookService the new fieldbook service
     */
    public void setFieldbookService(FieldbookService fieldbookService) {
        this.fieldbookService = fieldbookService;
    }
    
    public UserSelection getUserSelection() {
        return userSelection;
    }
    
    public void setTrialSelection(UserSelection userSelection) {
        this.userSelection = userSelection;
    }

}