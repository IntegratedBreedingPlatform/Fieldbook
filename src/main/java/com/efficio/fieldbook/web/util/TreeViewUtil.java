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

package com.efficio.fieldbook.web.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.domain.oms.PropertyReference;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.TraitClassReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListMetadata;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.pojos.treeview.TreeNode;
import com.efficio.pojos.treeview.TreeTableNode;
import com.efficio.pojos.treeview.TypeAheadSearchTreeNode;

/**
 * The Class TreeViewUtil.
 */
public class TreeViewUtil {

	private static final Logger LOG = LoggerFactory.getLogger(TreeViewUtil.class);

	private TreeViewUtil() {

	}

	/**
	 * Convert references to json.
	 *
	 * @param references the references
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertReferencesToJson(List<Reference> references) throws IOException {
		List<TreeNode> treeNodes = TreeViewUtil.convertReferencesToTreeView(references);
		return TreeViewUtil.convertTreeViewToJson(treeNodes);
	}

	/**
	 * Convert folder references to json.
	 *
	 * @param references the references
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertStudyFolderReferencesToJson(List<Reference> references, boolean isAll, boolean isLazy, boolean isFolderOnly)
			throws IOException {
		List<TreeNode> treeNodes = TreeViewUtil.convertStudyFolderReferencesToTreeView(references, isAll, isLazy, isFolderOnly);
		return TreeViewUtil.convertTreeViewToJson(treeNodes);
	}

	/**
	 * Convert folder references to json.
	 *
	 * @param references the references
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertFolderReferencesToJson(List<FolderReference> references, boolean isLazy) throws IOException {
		List<TreeNode> treeNodes = TreeViewUtil.convertFolderReferencesToTreeView(references, isLazy);
		return TreeViewUtil.convertTreeViewToJson(treeNodes);
	}

	/**
	 * Convert dataset references to json.
	 *
	 * @param references the references
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertDatasetReferencesToJson(List<DatasetReference> references) throws IOException {
		List<TreeNode> treeNodes = TreeViewUtil.convertDatasetReferencesToTreeView(references);
		return TreeViewUtil.convertTreeViewToJson(treeNodes);
	}

	/**
	 * Convert germplasm list to json.
	 *
	 * @param germplasmLists the germplasm lists
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertGermplasmListToJson(List<GermplasmList> germplasmLists, boolean isFolderOnly) throws IOException {
		List<TreeNode> treeNodes = TreeViewUtil.convertGermplasmListToTreeView(germplasmLists, isFolderOnly);
		return TreeViewUtil.convertTreeViewToJson(treeNodes);
	}

	/**
	 * Convert references to tree view.
	 *
	 * @param references the references
	 * @return the list
	 */
	private static List<TreeNode> convertReferencesToTreeView(List<Reference> references) {
		List<TreeNode> treeNodes = new ArrayList<TreeNode>();
		if (references != null && !references.isEmpty()) {
			for (Reference reference : references) {
				treeNodes.add(TreeViewUtil.convertReferenceToTreeNode(reference));
			}
		}
		return treeNodes;
	}

	/**
	 * Convert folder references to tree view.
	 *
	 * @param references the references
	 * @return the list
	 */
	private static List<TreeNode> convertFolderReferencesToTreeView(List<FolderReference> references, boolean isLazy) {
		List<TreeNode> treeNodes = new ArrayList<TreeNode>();
		TreeNode treeNode;
		if (references != null && !references.isEmpty()) {
			for (FolderReference reference : references) {
				treeNode = TreeViewUtil.convertReferenceToTreeNode(reference);
				treeNode.setIsLazy(isLazy);
				treeNodes.add(treeNode);
				if (reference.getSubFolders() != null && !reference.getSubFolders().isEmpty()) {
					treeNode.setChildren(TreeViewUtil.convertFolderReferencesToTreeView(reference.getSubFolders(), isLazy));
				} else {
					treeNode.setIsFolder(false);
				}
			}
		}
		return treeNodes;
	}

	public static List<TreeNode> convertStudyFolderReferencesToTreeView(List<Reference> references, boolean isAll, boolean isLazy,
			boolean isFolderOnly) {
		List<TreeNode> treeNodes = new ArrayList<TreeNode>();
		if (references != null && !references.isEmpty()) {
			for (Reference reference : references) {
				// isFolderOnly also comes all the way from UI. Keeping the existing logic. Not entirely sure what it is for.
				if (reference.isStudy() && isFolderOnly) {
					continue;
				}

				TreeNode treeNode = TreeViewUtil.convertStudyFolderReferenceToTreeNode(reference);
				treeNode.setIsLazy(isLazy);
				treeNodes.add(treeNode);
			}
		}
		return treeNodes;
	}

	/**
	 * Convert dataset references to tree view.
	 *
	 * @param references the references
	 * @return the list
	 */
	private static List<TreeNode> convertDatasetReferencesToTreeView(List<DatasetReference> references) {
		List<TreeNode> treeNodes = new ArrayList<TreeNode>();
		if (references != null && !references.isEmpty()) {
			for (DatasetReference reference : references) {
				treeNodes.add(TreeViewUtil.convertReferenceToTreeNode(reference));
			}
		}
		return treeNodes;
	}

	/**
	 * Convert germplasm list to tree view.
	 *
	 * @param germplasmLists the germplasm lists
	 * @return the list
	 */
	public static List<TreeNode> convertGermplasmListToTreeView(List<GermplasmList> germplasmLists, boolean isFolderOnly) {
		List<TreeNode> treeNodes = new ArrayList<TreeNode>();
		if (germplasmLists != null && !germplasmLists.isEmpty()) {
			for (GermplasmList germplasmList : germplasmLists) {
				TreeNode node = TreeViewUtil.convertGermplasmListToTreeNode(germplasmList, isFolderOnly);
				if (node != null) {
					treeNodes.add(node);
				}
			}
		}
		return treeNodes;
	}

	/**
	 * Convert list of germplasmList to tree table nodes.
	 *
	 * @param germplasmLists the germplasm lists
	 * @return the list
	 */
	public static List<TreeTableNode> convertGermplasmListToTreeTableNodes(List<GermplasmList> germplasmLists,
			GermplasmListManager germplasmListManager) {
		List<TreeTableNode> treeTableNodes = new ArrayList<TreeTableNode>();
		if (germplasmLists != null && !germplasmLists.isEmpty()) {
			
			List<UserDefinedField> listTypes = germplasmListManager.getGermplasmListTypes();
			Map<Long, GermplasmListMetadata> allListMetaData = germplasmListManager.getAllGermplasmListMetadata();
			
			for (GermplasmList germplasmList : germplasmLists) {
				TreeTableNode node =
						TreeViewUtil.convertGermplasmListToTreeTableNode(germplasmList, listTypes,
								allListMetaData.get(Long.valueOf(germplasmList.getId())));
				if (node != null) {
					treeTableNodes.add(node);
				}
			}
		}
		return treeTableNodes;
	}

	private static String getDescriptionForDisplay(GermplasmList germplasmList) {
		String description = "-";
		if (germplasmList != null && germplasmList.getDescription() != null && germplasmList.getDescription().length() != 0) {
			description = germplasmList.getDescription().replaceAll("<", "&lt;");
			description = description.replaceAll(">", "&gt;");
			if (description.length() > 27) {
				description = description.substring(0, 27) + "...";
			}
		}
		return description;
	}

	/**
	 * Convert reference to tree node.
	 *
	 * @param reference the reference
	 * @return the tree node
	 */
	private static TreeNode convertReferenceToTreeNode(Reference reference) {
		TreeNode treeNode = new TreeNode();

		treeNode.setKey(reference.getId().toString());
		treeNode.setTitle(reference.getName());
		treeNode.setIsFolder(reference instanceof DatasetReference ? false : true);
		treeNode.setIsLazy(true);
		treeNode.setProgramUUID(reference.getProgramUUID());

		return treeNode;
	}

	/**
	 * Convert reference to tree node.
	 *
	 * @param reference the reference
	 * @return the tree node
	 */
	private static TreeNode convertStudyFolderReferenceToTreeNode(Reference reference) {
		TreeNode treeNode = new TreeNode();
		treeNode.setKey(reference.getId().toString());
		treeNode.setTitle(reference.getName());
		boolean isFolder = reference.isFolder();
		treeNode.setIsFolder(isFolder);
		treeNode.setIsLazy(true);
		treeNode.setProgramUUID(reference.getProgramUUID());
		if (isFolder) {
			treeNode.setIcon(AppConstants.FOLDER_ICON_PNG.getString());
		} else {
			treeNode.setIcon(AppConstants.STUDY_ICON_PNG.getString());
		}
		return treeNode;
	}

	/**
	 * Convert germplasm list to tree node.
	 *
	 * @param germplasmList the germplasm list
	 * @return the tree node
	 */
	private static TreeNode convertGermplasmListToTreeNode(GermplasmList germplasmList, boolean isFolderOnly) {
		TreeNode treeNode = new TreeNode();

		treeNode.setKey(germplasmList.getId().toString());
		treeNode.setTitle(germplasmList.getName());
		treeNode.setIsFolder(germplasmList.getType() != null && "FOLDER".equals(germplasmList.getType()) ? true : false);
		treeNode.setIsLazy(false);
		if (treeNode.getIsFolder()) {
			treeNode.setIcon(AppConstants.FOLDER_ICON_PNG.getString());
		} else {
			treeNode.setIcon(AppConstants.BASIC_DETAILS_PNG.getString());
		}
		if (isFolderOnly && !treeNode.getIsFolder()) {
			return null;
		}

		return treeNode;
	}

	/**
	 * Convert germplasm list to tree node.
	 *
	 * @param germplasmList the germplasm list
	 * @return the tree node
	 */
	private static TreeTableNode convertGermplasmListToTreeTableNode(GermplasmList germplasmList, List<UserDefinedField> listTypes, GermplasmListMetadata listMetaData) {
		TreeTableNode treeTableNode = new TreeTableNode();

		treeTableNode.setId(germplasmList.getId().toString());
		treeTableNode.setName(germplasmList.getName());
		treeTableNode.setDescription(TreeViewUtil.getDescriptionForDisplay(germplasmList));
		treeTableNode.setType(TreeViewUtil.getTypeString(germplasmList.getType(), listTypes));
		treeTableNode.setOwner(listMetaData != null ? listMetaData.getOwnerName() : "");

		treeTableNode.setIsFolder(germplasmList.isFolder() ? "1" : "0");
		long noOfEntries = listMetaData != null ? listMetaData.getNumberOfEntries() : 0;
		treeTableNode.setNoOfEntries(noOfEntries == 0 ? "" : String.valueOf(noOfEntries));
		treeTableNode.setParentId(TreeViewUtil.getParentId(germplasmList));
		return treeTableNode;
	}

	private static String getParentId(GermplasmList germplasmList) {
		Integer parentId = germplasmList.getParentId();
		if (parentId == null) {
			return "LISTS";
		}
		return String.valueOf(parentId);
	}

	private static String getTypeString(String typeCode, List<UserDefinedField> listTypes) {
		String type = "Germplasm List";
		if (typeCode == null) {
			return type;
		}
		try {
			for (UserDefinedField listType : listTypes) {
				if (typeCode.equals(listType.getFcode())) {
					return listType.getFname();
				}
			}
		} catch (MiddlewareQueryException ex) {
			TreeViewUtil.LOG.error("Error in getting list types.", ex);
			return "";
		}
		return type;
	}

	/**
	 * Convert tree view to json.
	 *
	 * @param treeNodes the tree nodes
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertTreeViewToJson(List<TreeNode> treeNodes) throws IOException {
		if (treeNodes != null && !treeNodes.isEmpty()) {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(treeNodes);
		}
		return "[]";
	}

	/**
	 * Convert search tree view to json.
	 *
	 * @param treeNodes the tree nodes
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertSearchTreeViewToJson(List<TypeAheadSearchTreeNode> treeNodes) throws IOException {
		if (treeNodes != null && !treeNodes.isEmpty()) {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(treeNodes);
		}
		return "[]";
	}

	// for the ontology Browser
	/**
	 * Convert ontology traits to search single level json.
	 *
	 * @param traitClassReferences the trait references
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertOntologyTraitsToSearchSingleLevelJson(List<TraitClassReference> traitClassReferences,
			Map<String, StandardVariableReference> mapVariableRef) throws IOException {
		return TreeViewUtil.convertSearchTreeViewToJson(TreeViewUtil.getTypeAheadTreeNodes("", traitClassReferences, mapVariableRef));
	}

	private static List<TypeAheadSearchTreeNode> getTypeAheadTreeNodes(String parentId, List<TraitClassReference> traitClassReferences,
			Map<String, StandardVariableReference> mapVariableRef) {
		List<TypeAheadSearchTreeNode> treeNodes = new ArrayList<TypeAheadSearchTreeNode>();

		if (traitClassReferences != null && !traitClassReferences.isEmpty()) {
			for (TraitClassReference reference : traitClassReferences) {
				// this is for the inner trait classes
				if (reference.getTraitClassChildren() != null && !reference.getTraitClassChildren().isEmpty()) {
					String newParentId = "";
					if (parentId != null && !"".equals(parentId)) {
						newParentId = parentId + "_";
					}
					newParentId = newParentId + reference.getId().toString();
					treeNodes.addAll(TreeViewUtil.getTypeAheadTreeNodes(newParentId, reference.getTraitClassChildren(), mapVariableRef));
				}

				List<PropertyReference> propRefList = reference.getProperties();
				for (PropertyReference propRef : propRefList) {
					List<StandardVariableReference> variableRefList = propRef.getStandardVariables();
					String parentTitle = reference.getName();
					String key = reference.getId().toString() + "_" + propRef.getId().toString();

					if (parentId != null && !"".equals(parentId)) {
						key = parentId + "_" + key;
					}

					List<String> token = new ArrayList<String>();
					token.add(propRef.getName());
					TypeAheadSearchTreeNode searchTreeNode =
							new TypeAheadSearchTreeNode(key, token, propRef.getName(), parentTitle, "Property");
					treeNodes.add(searchTreeNode);

					for (StandardVariableReference variableRef : variableRefList) {
						boolean addVariableToSearch = true;
						if (mapVariableRef != null && !mapVariableRef.isEmpty()) {
							// we only show variables that are in the map
							if (mapVariableRef.containsKey(variableRef.getId().toString())) {
								addVariableToSearch = true;
							} else {
								addVariableToSearch = false;
							}
						}

						if (addVariableToSearch) {
							String varParentTitle = reference.getName() + " > " + propRef.getName();
							String varKey = key + "_" + variableRef.getId().toString();
							List<String> varToken = new ArrayList<String>();
							varToken.add(variableRef.getName());
							TypeAheadSearchTreeNode varSearchTreeNode =
									new TypeAheadSearchTreeNode(varKey, varToken, variableRef.getName(), varParentTitle,
											"Standard Variable");
							treeNodes.add(varSearchTreeNode);
						}
					}
				}

			}
		}

		return treeNodes;
	}

	/**
	 * Convert ontology traits to json.
	 *
	 * @param traitClassReferences the trait references
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertOntologyTraitsToJson(List<TraitClassReference> traitClassReferences,
			Map<String, StandardVariableReference> mapVariableRef) throws IOException {

		List<TreeNode> treeNodes = TreeViewUtil.convertTraitClassReferencesToTreeView(traitClassReferences, mapVariableRef);

		return TreeViewUtil.convertTreeViewToJson(treeNodes);
	}

	/**
	 * Convert trait references to tree view.
	 *
	 * @param traitClassReferences the trait references
	 * @return the list
	 */
	private static List<TreeNode> convertTraitClassReferencesToTreeView(List<TraitClassReference> traitClassReferences,
			Map<String, StandardVariableReference> mapVariableRef) {
		List<TreeNode> treeNodes = new ArrayList<TreeNode>();
		if (traitClassReferences != null && !traitClassReferences.isEmpty()) {
			for (TraitClassReference reference : traitClassReferences) {
				treeNodes.add(TreeViewUtil.convertTraitClassReferenceToTreeNode("", reference, mapVariableRef));
			}
		}
		return treeNodes;
	}

	/**
	 * Convert trait reference to tree node.
	 *
	 * @param reference the reference
	 * @return the tree node
	 */
	private static TreeNode convertTraitClassReferenceToTreeNode(String parentParentId, TraitClassReference reference,
			Map<String, StandardVariableReference> mapVariableRef) {
		TreeNode treeNode = new TreeNode();
		String parentId = reference.getId().toString();
		if (parentParentId != null && !"".equals(parentParentId)) {
			parentId = parentParentId + "_" + parentId;
		}
		treeNode.setKey(parentId);
		treeNode.setAddClass(parentId);
		treeNode.setTitle(reference.getName());
		treeNode.setIsFolder(true);
		treeNode.setIsLazy(false);
		treeNode.setIcon(false);
		treeNode.setIncludeInSearch(false);

		List<TreeNode> treeNodes = new ArrayList<TreeNode>();

		// this is for the inner trait classes
		if (reference.getTraitClassChildren() != null && !reference.getTraitClassChildren().isEmpty()) {
			for (TraitClassReference childTrait : reference.getTraitClassChildren()) {
				treeNodes.add(TreeViewUtil.convertTraitClassReferenceToTreeNode(parentId, childTrait, mapVariableRef));
			}
		}
		// we need to set the children for the property

		if (reference.getProperties() != null && !reference.getProperties().isEmpty()) {
			for (PropertyReference propRef : reference.getProperties()) {
				treeNodes.add(TreeViewUtil.convertPropertyReferenceToTreeNode(parentId, propRef, reference.getName(), mapVariableRef));
			}

		}
		treeNode.setChildren(treeNodes);

		return treeNode;
	}

	/**
	 * Convert property reference to tree node.
	 *
	 * @param parentId the parent id
	 * @param reference the reference
	 * @param parentTitle the parent title
	 * @return the tree node
	 */
	private static TreeNode convertPropertyReferenceToTreeNode(String parentId, PropertyReference reference, String parentTitle,
			Map<String, StandardVariableReference> mapVariableRef) {
		TreeNode treeNode = new TreeNode();
		String id = parentId + "_" + reference.getId().toString();
		treeNode.setKey(id);
		treeNode.setAddClass(id);
		treeNode.setTitle(reference.getName());
		treeNode.setIsFolder(true);
		treeNode.setIsLazy(false);
		treeNode.setIcon(false);
		treeNode.setIncludeInSearch(true);
		String newParentTitle = parentTitle + " > " + reference.getName();
		treeNode.setParentTitle(newParentTitle);
		// we need to set the children for the property
		List<TreeNode> treeNodes = new ArrayList<TreeNode>();
		if (reference.getStandardVariables() != null && !reference.getStandardVariables().isEmpty()) {
			for (StandardVariableReference variableRef : reference.getStandardVariables()) {
				TreeNode variableTreeNode =
						TreeViewUtil.convertStandardVariableReferenceToTreeNode(id, variableRef, newParentTitle, mapVariableRef);
				if (variableTreeNode != null) {
					treeNodes.add(variableTreeNode);
				}
			}

		}
		treeNode.setChildren(treeNodes);

		return treeNode;
	}

	/**
	 * Convert standard variable reference to tree node.
	 *
	 * @param parentId the parent id
	 * @param reference the reference
	 * @param parentTitle the parent title
	 * @return the tree node
	 */
	private static TreeNode convertStandardVariableReferenceToTreeNode(String parentId, StandardVariableReference reference,
			String parentTitle, Map<String, StandardVariableReference> mapVariableRef) {

		if (mapVariableRef != null && !mapVariableRef.isEmpty() && !mapVariableRef.containsKey(reference.getId().toString())) {
			return null;
		}

		TreeNode treeNode = new TreeNode();
		String id = parentId + "_" + reference.getId().toString();
		treeNode.setKey(id);
		treeNode.setAddClass(id);
		treeNode.setTitle(reference.getName());
		treeNode.setIsFolder(false);
		treeNode.setIsLazy(false);
		treeNode.setLastChildren(true);
		treeNode.setIcon(false);
		treeNode.setIncludeInSearch(true);
		String newParentTitle = parentTitle + " > " + reference.getName();
		treeNode.setParentTitle(newParentTitle);
		// we need to set the children for the property
		List<TreeNode> treeNodes = new ArrayList<TreeNode>();
		treeNode.setChildren(treeNodes);

		return treeNode;
	}
}
