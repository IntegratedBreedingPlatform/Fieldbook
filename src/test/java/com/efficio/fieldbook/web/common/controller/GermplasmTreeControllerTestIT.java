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

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.easymock.EasyMock;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.commons.pojo.treeview.TreeTableNode;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GermplasmTreeControllerTestIT extends AbstractBaseIntegrationTest {

	/** The controller. */
	@Autowired
	private GermplasmTreeController controller;

	private static final Integer LIST_USER_ID = 1;
	private static final String TEST_GERMPLASM_LIST = "Test Germplasm List";
	private static final String NAME_NOT_UNIQUE = "Name not unique";
	private static final String PROGRAM_UUID = "1234567";

	/** The Constant LIST_1. */
	private static final GermplasmList LIST_1 =
			new GermplasmList(1, "List 1", null, "LST", GermplasmTreeControllerTestIT.LIST_USER_ID, "List 1", null, 1);

	/** The Constant LIST_2. */
	private static final GermplasmList LIST_2 =
			new GermplasmList(2, "List 2", null, "LST", GermplasmTreeControllerTestIT.LIST_USER_ID, "List 2", null, 1);

	/** The Constant LOCAL_LIST_3. */
	private static final GermplasmList LIST_3 =
			new GermplasmList(3, "List 3", null, "LST", GermplasmTreeControllerTestIT.LIST_USER_ID, "List 3", null, 1);

	private static final GermplasmList CROP_LIST =
			new GermplasmList(4, "Shared List", null, "LST", GermplasmTreeControllerTestIT.LIST_USER_ID, "Crop List", null, 1);

	private static final GermplasmList OTHER_LIST =
			new GermplasmList(5, "Other List", null, "LST", GermplasmTreeControllerTestIT.LIST_USER_ID, "Other List", null, 1);

	private static final List<GermplasmList> CROP_LISTS = Arrays.asList(GermplasmTreeControllerTestIT.CROP_LIST);

	private static final List<GermplasmList> PROGRAM_LISTS = Arrays.asList(GermplasmTreeControllerTestIT.LIST_1,
			GermplasmTreeControllerTestIT.LIST_2, GermplasmTreeControllerTestIT.LIST_3);

	private static final List<GermplasmList> EMPTY_GERMPLASM_LIST_TEST_DATA = new ArrayList<GermplasmList>();

	/** The object mapper. */
	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeClass
	public static void setUpClass() {
		GermplasmTreeControllerTestIT.LIST_1.setProgramUUID(GermplasmTreeControllerTestIT.PROGRAM_UUID);
		GermplasmTreeControllerTestIT.LIST_2.setProgramUUID(GermplasmTreeControllerTestIT.PROGRAM_UUID);
		GermplasmTreeControllerTestIT.LIST_3.setProgramUUID(GermplasmTreeControllerTestIT.PROGRAM_UUID);
		GermplasmTreeControllerTestIT.OTHER_LIST.setProgramUUID("other12345");
	}

	/**
	 * Sets the up.
	 *
	 * @throws Exception the exception
	 */
	@Before
	public void setUp() throws Exception {
		this.mockGermplasmListManagerAndItsMethods();
		this.mockCurrentProgramUUID();
	}

	private void mockCurrentProgramUUID() {
		ContextUtil contextUtil = Mockito.mock(ContextUtil.class);
		Mockito.when(contextUtil.getCurrentProgramUUID()).thenReturn(GermplasmTreeControllerTestIT.PROGRAM_UUID);
		controller.setContextUtil(contextUtil);
	}

	/**
	 * Test load initial tree.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testLoadInitialTree() throws Exception {
		String jsonResponse = this.controller.loadInitialGermplasmTree("0");

		List<TreeNode> treeNodes = this.objectMapper.readValue(jsonResponse, new TypeReference<List<TreeNode>>() {
		});

		Assert.assertEquals(2, treeNodes.size());
		Assert.assertEquals(GermplasmTreeController.CROP_LISTS, treeNodes.get(0).getKey());
		Assert.assertEquals(GermplasmTreeController.PROGRAM_LISTS, treeNodes.get(1).getKey());
	}

	/**
	 * Test expand germplasm node.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testExpandGermplasmNode() throws Exception {
		String jsonResponse = this.controller.expandGermplasmTree("List 1", "0");
		Assert.assertEquals("[]", jsonResponse);
	}

	/**
	 * Test load initial tree table.
	 *
	 * @throws Exception the exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testLoadInitialGermplasmTreeTable() throws Exception {
		ExtendedModelMap model = new ExtendedModelMap();
		this.controller.loadInitialGermplasmTreeTable(model);
		List<TreeTableNode> treeNodes = (List<TreeTableNode>) model.get(GermplasmTreeController.GERMPLASM_LIST_ROOT_NODES);
		int numberOfRootNodes = 0;
		List<TreeTableNode> rootNodes = new ArrayList<TreeTableNode>();
		for (TreeTableNode treeTableNode : treeNodes) {
			if (treeTableNode.getParentId() == null) {
				rootNodes.add(treeTableNode);
				numberOfRootNodes++;
			}
		}

		Assert.assertEquals("The number of root nodes should be 1", 1, numberOfRootNodes);
		Assert.assertEquals("The first root node should be have an id of " + GermplasmTreeController.PROGRAM_LISTS,
				GermplasmTreeController.PROGRAM_LISTS, rootNodes.get(0).getId());
	}


	private void mockGermplasmListManagerAndItsMethods() {
		GermplasmListManager germplasmListManager = Mockito.mock(GermplasmListManager.class);
		Mockito.when(germplasmListManager.getAllTopLevelLists(GermplasmTreeControllerTestIT.PROGRAM_UUID)).thenReturn(GermplasmTreeControllerTestIT.PROGRAM_LISTS);
		Mockito.when(germplasmListManager.getAllTopLevelLists(null)).thenReturn(GermplasmTreeControllerTestIT.CROP_LISTS);
		Mockito.when(
				germplasmListManager.getGermplasmListByParentFolderId(ArgumentMatchers.anyInt(), ArgumentMatchers.anyString()))
				.thenReturn(GermplasmTreeControllerTestIT.EMPTY_GERMPLASM_LIST_TEST_DATA);
		List<UserDefinedField> userDefinedFields = this.createGermplasmListUserDefinedFields();
		Mockito.when(germplasmListManager.getGermplasmListTypes()).thenReturn(userDefinedFields);
		controller.setGermplasmListManager(germplasmListManager);
	}

	private List<UserDefinedField> createGermplasmListUserDefinedFields() {
		List<UserDefinedField> userDefinedFields = new ArrayList<UserDefinedField>();
		UserDefinedField listType = new UserDefinedField();
		listType.setFcode("LST");
		listType.setFname("LIST FOLDER");
		userDefinedFields.add(listType);
		UserDefinedField folderType = new UserDefinedField();
		folderType.setFcode("FOLDER");
		folderType.setFname("FOLDER");
		userDefinedFields.add(folderType);
		return userDefinedFields;
	}

	@Test
	public void testGetGermplasmListChildren() {
		TreeTableNode anyChildNode = new TreeTableNode(Integer.toString(EasyMock.anyInt()),
				GermplasmTreeControllerTestIT.TEST_GERMPLASM_LIST, null, null, null, null, "1");
		List<GermplasmList> germplasmListChildren = this.controller.getGermplasmListChildren(anyChildNode.getId(), PROGRAM_UUID);
		Assert.assertTrue(
				GermplasmTreeControllerTestIT.TEST_GERMPLASM_LIST + " should have " + germplasmListChildren.size() + " number of children",
				germplasmListChildren.size() == GermplasmTreeControllerTestIT.EMPTY_GERMPLASM_LIST_TEST_DATA.size());

		TreeTableNode localRootNode =
				new TreeTableNode(GermplasmTreeController.PROGRAM_LISTS, AppConstants.PROGRAM_LISTS.getString(), null, null, null, null, "1");
		germplasmListChildren = this.controller.getGermplasmListChildren(localRootNode.getId(), PROGRAM_UUID);
		Assert.assertTrue(AppConstants.PROGRAM_LISTS.getString() + " should have " + germplasmListChildren.size() + " number of children",
				germplasmListChildren.size() == GermplasmTreeControllerTestIT.PROGRAM_LISTS.size());
	}

	@Test
	public void testCheckIfUniqueUsingTheRootFolderAsAnInput() {
		try {
			this.controller.checkIfUnique(GermplasmTreeController.PROGRAM_LISTS, PROGRAM_UUID);
		} catch (MiddlewareException e) {
			Assert.assertEquals(GermplasmTreeControllerTestIT.NAME_NOT_UNIQUE, e.getMessage());
		}
	}

	@Test
	public void testCheckIfUniqueUsingExistingListAsInput() {
		GermplasmListManager germplasmListManager = Mockito.mock(GermplasmListManager.class);
		String folderName = "Sample Folder Name";

		Mockito.when(germplasmListManager.getGermplasmListByName(folderName, GermplasmTreeControllerTestIT.PROGRAM_UUID, 0, 1, null))
				.thenReturn(GermplasmTreeControllerTestIT.PROGRAM_LISTS);
		ReflectionTestUtils.setField(this.controller, "germplasmListManager", germplasmListManager, GermplasmListManager.class);
		try {
			this.controller.checkIfUnique(folderName, PROGRAM_UUID);
		} catch (MiddlewareException e) {
			Assert.assertEquals(GermplasmTreeControllerTestIT.NAME_NOT_UNIQUE, e.getMessage());
		}
	}

	@Test
	public void testIsSimilarToRootFolderNameReturnsTrueForItemNameSimilarToRootFolder() {
		Assert.assertTrue("Expecting to return true for item name similar to \"Program lists\" ",
				this.controller.isSimilarToRootFolderName("Program lists"));
	}

	@Test
	public void testIsSimilarToRootFolderNameReturnsTrueForItemNameNotSimilarToRootFolder() {
		Assert.assertFalse("Expecting to return true for item name not similar to \"Lists\" ",
				this.controller.isSimilarToRootFolderName("Dummy Folder Name"));
	}

}
