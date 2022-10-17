/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.trial.controller;

import org.generationcp.commons.security.AuthorizationService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

public class ManageTrialControllerTest {

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private AuthorizationService authorizationService;

	@InjectMocks
	private ManageTrialController controller;

	private MockMvc mockMvc;

	private static final String CROP_NAME = "maize";

	private static final String PROGRAM_UUID = "programUUID123";

	@Before
	public void setup() {
        MockitoAnnotations.initMocks(this);
        // Use standalone setup in order to mock StudyDataManager (there is error in
        // in CI server when creating bean StudyDataManager since there's no program in DB)
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		Mockito.when(authorizationService.isSuperAdminUser()).thenReturn(Boolean.TRUE);
		Mockito.when(authorizationService.hasAnyAuthority(ArgumentMatchers.anyList())).thenReturn(Boolean.TRUE);

		final Project project = new Project();
		project.setCropType(new CropType(CROP_NAME));
		project.setUniqueID(PROGRAM_UUID);
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(project);

	}

	@Test
	public void testGet() throws Exception {

		this.mockMvc.perform(MockMvcRequestBuilders.get(ManageTrialController.URL))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.model().attributeExists("preloadSummaryId"))
				.andExpect(MockMvcResultMatchers.model().attributeExists("preloadSummaryName"))
				.andExpect(MockMvcResultMatchers.model().attributeExists("studyTypes"))
				.andExpect(MockMvcResultMatchers.model().attributeExists("isSuperAdmin"))
				.andExpect(MockMvcResultMatchers.model().attributeExists("hasManageStudiesPermission"))
				.andExpect(MockMvcResultMatchers.model().attribute(AbstractBaseFieldbookController.TEMPLATE_NAME_ATTRIBUTE, "Common/manageStudy"));

		// Used ModelAttribute annotation for this so cannot assert above so verify mock interaction instead
		Mockito.verify(this.contextUtil).getCurrentWorkbenchUserId();
	}
}
