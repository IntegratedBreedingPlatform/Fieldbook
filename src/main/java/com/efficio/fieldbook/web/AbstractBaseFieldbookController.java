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
package com.efficio.fieldbook.web;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.ui.Model;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */
public abstract class AbstractBaseFieldbookController implements ApplicationContextAware {

    public static final String BASE_TEMPLATE_NAME = "base-template";
    public static final String TEMPLATE_NAME_ATTRIBUTE = "templateName";
    public static final String USER_SELECTION_ID_SUFFIX = "TestUserSelection";


    private ApplicationContext applicationContext;

    public abstract String getContentName();

    /**
     * Base functionality for displaying the page.
     *
     * @param model
     * @return
     */
    public String show(Model model) {
        model.addAttribute(TEMPLATE_NAME_ATTRIBUTE, getContentName());

        return BASE_TEMPLATE_NAME;
    }
    

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
