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

package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.transaction.PlatformTransactionManager;

public class StudyTreeControllerTest {

	private static final String NEW_FOLDER_ID = "2";
	
	private static final String FOLDER_ID = "1";

	private static final String PARENT_FOLDER_ID = "1";

	private static final String FOLDER_NAME = "FOLDER 1";

	private static final String CONTAINS_TRIALS = "The folder FOLDER 1 cannot be deleted because it contains trials";
	
	private static final String CONTAINS_NURSERIES = "The folder FOLDER 1 cannot be deleted because it contains nurseries";
	
	private static final String FOLDER_NOT_EMPTY = "The folder FOLDER 1 cannot be deleted because it is not empty";
	
	private Project selectedProject;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private MessageSource messageSource;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private HttpServletRequest request;

	@Mock
	private PlatformTransactionManager transactionManager;

	@InjectMocks
	private StudyTreeController controller;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.selectedProject = this.createProject();
		Mockito.doReturn(this.selectedProject.getUniqueID()).when(this.contextUtil).getCurrentProgramUUID();
	}

	private Project createProject() {
		Project project = new Project();
		project.setProjectId(1L);
		project.setProjectName("Test Project");
		project.setUniqueID(UUID.randomUUID().toString());
		return project;
	}

	@Test
	public void testLoadInitialTree() {
		String result = this.controller.loadInitialTree(PARENT_FOLDER_ID, "N");
		Assert.assertNotNull(result);
		Assert.assertTrue(!"[]".equals(result));
	}

	@Test
	public void testExpandTreeRoot() {
		List<Reference> testTree = new ArrayList<>();
		testTree.add(new FolderReference(1, 2, "My Folder", "My Folder Description"));
		testTree.add(new FolderReference(2, 3, "My Sub Folder", "My Sub Folder Description"));
		testTree.add(new StudyReference(11, "My Nursery", "My Nursery Description"));

		Mockito.doReturn(testTree).when(this.studyDataManager).getRootFolders(this.selectedProject.getUniqueID(), StudyType.nurseries());

		String result = this.controller.expandTree(StudyTreeController.LOCAL, PARENT_FOLDER_ID, "N");
		Assert.assertNotNull(result);
		Assert.assertTrue(!"[]".equals(result));
	}

	@Test
	public void testExpandTreeUnderRoot() {
		List<Reference> testTree = new ArrayList<>();
		testTree.add(new FolderReference(1, 2, "My Folder", "My Folder Description"));
		testTree.add(new FolderReference(2, 3, "My Sub Folder", "My Sub Folder Description"));
		testTree.add(new StudyReference(11, "My Nursery", "My Nursery Description"));

		Mockito.doReturn(testTree).when(this.studyDataManager)
				.getChildrenOfFolder(1, this.selectedProject.getUniqueID(), StudyType.nurseries());

		String result = this.controller.expandTree(PARENT_FOLDER_ID, PARENT_FOLDER_ID, "N");
		Assert.assertNotNull(result);
		Assert.assertTrue(!"[]".equals(result));
	}

	@Test
	public void testAddStudyFolderSuccessful() {

		Mockito.when(this.request.getParameter("parentFolderId")).thenReturn(PARENT_FOLDER_ID);
		Mockito.when(this.request.getParameter("folderName")).thenReturn(FOLDER_NAME);
		Mockito.when(
				this.studyDataManager.addSubFolder(Integer.valueOf(PARENT_FOLDER_ID), FOLDER_NAME, FOLDER_NAME,
						this.selectedProject.getUniqueID())).thenReturn(Integer.valueOf(NEW_FOLDER_ID));

		Map<String, Object> result = this.controller.addStudyFolder(this.request);

		Assert.assertEquals("1", result.get(StudyTreeController.IS_SUCCESS));
		Assert.assertEquals(NEW_FOLDER_ID, result.get(StudyTreeController.NEW_FOLDER_ID));
	}

	@Test
	public void testAddStudyFolderParentFolderCannotBeNull() {

		Mockito.when(this.request.getParameter("parentFolderId")).thenReturn(PARENT_FOLDER_ID);
		Mockito.when(this.request.getParameter("folderName")).thenReturn(FOLDER_NAME);
		Mockito.when(
				this.studyDataManager.addSubFolder(Integer.valueOf(PARENT_FOLDER_ID), FOLDER_NAME, FOLDER_NAME,
						this.selectedProject.getUniqueID())).thenReturn(Integer.valueOf(NEW_FOLDER_ID));
		Mockito.when(this.studyDataManager.isStudy(Integer.valueOf(PARENT_FOLDER_ID))).thenReturn(true);
		Mockito.when(this.studyDataManager.getParentFolder(Integer.valueOf(PARENT_FOLDER_ID))).thenReturn(null);

		Map<String, Object> result = this.controller.addStudyFolder(this.request);

		Assert.assertEquals("0", result.get(StudyTreeController.IS_SUCCESS));
		Assert.assertEquals("Parent folder cannot be null", result.get(StudyTreeController.MESSAGE));
	}

	@Test
	public void testAddStudyFolderFolderNameIsNotUnique() {

		Mockito.when(this.request.getParameter("parentFolderId")).thenReturn(PARENT_FOLDER_ID);
		Mockito.when(this.request.getParameter("folderName")).thenReturn(FOLDER_NAME);
		Mockito.when(
				this.studyDataManager.addSubFolder(Integer.valueOf(PARENT_FOLDER_ID), FOLDER_NAME, FOLDER_NAME,
						this.selectedProject.getUniqueID())).thenThrow(new MiddlewareQueryException("Folder name is not unique"));

		Map<String, Object> result = this.controller.addStudyFolder(this.request);

		Assert.assertEquals("0", result.get(StudyTreeController.IS_SUCCESS));
		Assert.assertEquals("Folder name is not unique", result.get(StudyTreeController.MESSAGE));
	}
	
	@Test
	public void testIsFolderEmptyTrue() {
		Mockito.when(this.studyDataManager.isFolderEmpty(Matchers.anyInt(), Matchers.anyString(), Matchers.eq(StudyType.nurseriesAndTrials()))).thenReturn(true);
		Map<String, Object> result = this.controller.isFolderEmpty(this.request, FOLDER_ID, StudyType.N.getName(), FOLDER_NAME);
		Assert.assertEquals("The result's isSuccess attribute should be 1", "1", result.get(StudyTreeController.IS_SUCCESS));
	}
	
	@Test
	public void testIsFolderEmptyFalseContainsTrials() {
		Mockito.when(this.studyDataManager.isFolderEmpty(Matchers.anyInt(), Matchers.anyString(), Matchers.eq(StudyType.nurseriesAndTrials()))).thenReturn(false);
		Mockito.when(this.studyDataManager.isFolderEmpty(Matchers.anyInt(), Matchers.anyString(), Matchers.eq(StudyType.nurseries()))).thenReturn(true);
		Mockito.when(this.messageSource.getMessage(Matchers.eq("browse.trial.delete.folder.contains.trials"), Matchers.eq(new Object[] {FOLDER_NAME}), Matchers.eq(LocaleContextHolder.getLocale()))).thenReturn(CONTAINS_TRIALS);
		
		Map<String, Object> result = this.controller.isFolderEmpty(this.request, FOLDER_ID, StudyType.N.getName(), FOLDER_NAME);
		Assert.assertEquals("The result's isSuccess attribute should be 0", "0", result.get(StudyTreeController.IS_SUCCESS));		
		Assert.assertEquals("The message should be '"+CONTAINS_TRIALS+"'", CONTAINS_TRIALS, result.get(StudyTreeController.MESSAGE));
	}
	
	@Test
	public void testIsFolderEmptyFalseContainsNurseries() {
		Mockito.when(this.studyDataManager.isFolderEmpty(Matchers.anyInt(), Matchers.anyString(), Matchers.eq(StudyType.nurseriesAndTrials()))).thenReturn(false);
		Mockito.when(this.studyDataManager.isFolderEmpty(Matchers.anyInt(), Matchers.anyString(), Matchers.eq(StudyType.trials()))).thenReturn(true);
		Mockito.when(this.messageSource.getMessage(Matchers.eq("browse.nursery.delete.folder.contains.nurseries"), Matchers.eq(new Object[] {FOLDER_NAME}), Matchers.eq(LocaleContextHolder.getLocale()))).thenReturn(CONTAINS_NURSERIES);
		
		Map<String, Object> result = this.controller.isFolderEmpty(this.request, FOLDER_ID, StudyType.T.getName(), FOLDER_NAME);
		Assert.assertEquals("The result's isSuccess attribute should be 0", "0", result.get(StudyTreeController.IS_SUCCESS));		
		Assert.assertEquals("The message should be '"+CONTAINS_NURSERIES+"'", CONTAINS_NURSERIES, result.get(StudyTreeController.MESSAGE));
	}
	
	@Test
	public void testIsFolderEmptyFalseFolderEmpty() {
		Mockito.when(this.studyDataManager.isFolderEmpty(Matchers.anyInt(), Matchers.anyString(), Matchers.eq(StudyType.nurseriesAndTrials()))).thenReturn(false);
		Mockito.when(this.studyDataManager.isFolderEmpty(Matchers.anyInt(), Matchers.anyString(), Matchers.eq(StudyType.nurseries()))).thenReturn(false);
		Mockito.when(this.messageSource.getMessage(Matchers.eq("browse.nursery.delete.folder.not.empty"), Matchers.eq(new Object[] {FOLDER_NAME}), Matchers.eq(LocaleContextHolder.getLocale()))).thenReturn(FOLDER_NOT_EMPTY);
		
		Map<String, Object> result = this.controller.isFolderEmpty(this.request, FOLDER_ID, StudyType.N.getName(), FOLDER_NAME);
		Assert.assertEquals("The result's isSuccess attribute should be 0", "0", result.get(StudyTreeController.IS_SUCCESS));		
		Assert.assertEquals("The message should be '"+FOLDER_NOT_EMPTY+"'", FOLDER_NOT_EMPTY, result.get(StudyTreeController.MESSAGE));
	}
}
