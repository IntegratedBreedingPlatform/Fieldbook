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

package com.efficio.fieldbook.web.ontology.form;

import java.util.List;

import org.generationcp.middleware.domain.oms.TraitClassReference;

/**
 * The Class OntologyBrowserForm.
 *
 * @author Efficio.Daniel
 */
public class OntologyBrowserForm {

	/** The has error. */
	private String hasError;

	/** The add successful. */
	private String addSuccessful;

	/** The error message. */
	private String errorMessage;

	/** The trait reference list. */
	private List<TraitClassReference> traitClassReferenceList;

	/** The tree data. */
	private String treeData;
	// convert to json 1 level for the property and standard variable
	/** The search tree data. */
	private String searchTreeData;

	/** The standard variable id. */
	private Integer variableId;

	/** The variable name. */
	private String variableName;

	/** The new variable name. */
	private String newVariableName;

	/** The is delete. */
	private Integer isDelete;

	/** The variable description. */
	private String variableDescription;

	/** The data types. */
	private String dataType;

	/** The data type id. */
	private String dataTypeId;

	/** The roles. */
	private String role;

	/** The crop ontology id. */
	private String cropOntologyDisplay;

	/** The trait class. */
	private String traitClass;

	/** The property. */
	private String property;

	/** The method. */
	private String method;

	/** The scale. */
	private String scale;

	/** The trait class description. */
	private String traitClassDescription;

	/** The property description. */
	private String propertyDescription;

	/** The method description. */
	private String methodDescription;

	/** The scale description. */
	private String scaleDescription;

	/** The min value. */
	private Double minValue;

	/** The max value. */
	private Double maxValue;

	/** The enumerations. */
	private String enumerations;

	private String fromPopup;

	private int preselectVariableId;

	public String getFromPopup() {
		return this.fromPopup;
	}

	public void setFromPopup(String fromPopup) {
		this.fromPopup = fromPopup;
	}

	/**
	 * Gets the checks for error.
	 *
	 * @return the checks for error
	 */
	public String getHasError() {
		return this.hasError;
	}

	/**
	 * Sets the checks for error.
	 *
	 * @param hasError the new checks for error
	 */
	public void setHasError(String hasError) {
		this.hasError = hasError;
	}

	/**
	 * Gets the adds the successful.
	 *
	 * @return the adds the successful
	 */
	public String getAddSuccessful() {
		return this.addSuccessful;
	}

	/**
	 * Sets the adds the successful.
	 *
	 * @param addSuccessful the new adds the successful
	 */
	public void setAddSuccessful(String addSuccessful) {
		this.addSuccessful = addSuccessful;
	}

	/**
	 * Gets the error message.
	 *
	 * @return the error message
	 */
	public String getErrorMessage() {
		return this.errorMessage;
	}

	/**
	 * Sets the error message.
	 *
	 * @param errorMessage the new error message
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * Gets the tree data.
	 *
	 * @return the tree data
	 */
	public String getTreeData() {
		return this.treeData;
	}

	/**
	 * Sets the tree data.
	 *
	 * @param treeData the new tree data
	 */
	public void setTreeData(String treeData) {
		this.treeData = treeData;
	}

	/**
	 * Gets the trait reference list.
	 *
	 * @return the trait reference list
	 */
	public List<TraitClassReference> getTraitClassReferenceList() {
		return this.traitClassReferenceList;
	}

	/**
	 * Sets the trait reference list.
	 *
	 * @param traitClassReferenceList the new trait reference list
	 */
	public void setTraitClassReferenceList(List<TraitClassReference> traitClassReferenceList) {
		this.traitClassReferenceList = traitClassReferenceList;
	}

	/**
	 * Gets the search tree data.
	 *
	 * @return the search tree data
	 */
	public String getSearchTreeData() {
		return this.searchTreeData;
	}

	/**
	 * Sets the search tree data.
	 *
	 * @param searchTreeData the new search tree data
	 */
	public void setSearchTreeData(String searchTreeData) {
		this.searchTreeData = searchTreeData;
	}

	/**
	 * Gets the variable name.
	 *
	 * @return the variable name
	 */
	public String getVariableName() {
		return this.variableName;
	}

	/**
	 * Sets the variable name.
	 *
	 * @param variableName the new variable name
	 */
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	/*
	 * Gets new the variable name.
	 * 
	 * @return the new variable name
	 */
	/**
	 * Gets the new variable name.
	 *
	 * @return the new variable name
	 */
	public String getNewVariableName() {
		return this.newVariableName;
	}

	/**
	 * Sets the new variable name.
	 *
	 * @param newVariableName the new new variable name
	 */
	public void setNewVariableName(String newVariableName) {
		this.newVariableName = newVariableName;
	}

	/**
	 * Gets the checks if is delete.
	 *
	 * @return the checks if is delete
	 */
	public Integer getIsDelete() {
		return this.isDelete;
	}

	/**
	 * Sets the checks if is delete.
	 *
	 * @param isDelete the new checks if is delete
	 */
	public void setIsDelete(Integer isDelete) {
		this.isDelete = isDelete;
	}

	/**
	 * Gets the variable description.
	 *
	 * @return the variable description
	 */
	public String getVariableDescription() {
		return this.variableDescription;
	}

	/**
	 * Sets the variable description.
	 *
	 * @param variableDescription the new variable description
	 */
	public void setVariableDescription(String variableDescription) {
		this.variableDescription = variableDescription;
	}

	/**
	 * Gets the data types.
	 *
	 * @return the data types
	 */
	public String getDataType() {
		return this.dataType;
	}

	/**
	 * Sets the data types.
	 *
	 * @param dataType the new data type
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	/**
	 * Gets the data type id.
	 *
	 * @return the data type id
	 */
	public String getDataTypeId() {
		return this.dataTypeId;
	}

	/**
	 * Sets the data type id.
	 *
	 * @param dataTypeId the new data type id
	 */
	public void setDataTypeId(String dataTypeId) {
		this.dataTypeId = dataTypeId;
	}

	/**
	 * Gets the roles.
	 *
	 * @return the roles
	 */
	public String getRole() {
		return this.role;
	}

	/**
	 * Sets the roles.
	 *
	 * @param role the new role
	 */
	public void setRole(String role) {
		this.role = role;
	}

	/**
	 * Gets the crop ontology id.
	 *
	 * @return the crop ontology id
	 */
	public String getCropOntologyDisplay() {
		return this.cropOntologyDisplay;
	}

	/**
	 * Sets the crop ontology id.
	 *
	 * @param cropOntologyId the new crop ontology id
	 */
	public void setCropOntologyDisplay(String cropOntologyId) {
		this.cropOntologyDisplay = cropOntologyId;
	}

	/**
	 * Gets the trait class.
	 *
	 * @return the trait class
	 */
	public String getTraitClass() {
		return this.traitClass;
	}

	/**
	 * Sets the trait class.
	 *
	 * @param traitClass the new trait class
	 */
	public void setTraitClass(String traitClass) {
		this.traitClass = traitClass;
	}

	/**
	 * Gets the property.
	 *
	 * @return the property
	 */
	public String getProperty() {
		return this.property;
	}

	/**
	 * Sets the property.
	 *
	 * @param property the new property
	 */
	public void setProperty(String property) {
		this.property = property;
	}

	/**
	 * Gets the method.
	 *
	 * @return the method
	 */
	public String getMethod() {
		return this.method;
	}

	/**
	 * Sets the method.
	 *
	 * @param method the new method
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * Gets the scale.
	 *
	 * @return the scale
	 */
	public String getScale() {
		return this.scale;
	}

	/**
	 * Sets the scale.
	 *
	 * @param scale the new scale
	 */
	public void setScale(String scale) {
		this.scale = scale;
	}

	/**
	 * Gets the trait class description.
	 *
	 * @return the trait class description
	 */
	public String getTraitClassDescription() {
		return this.traitClassDescription;
	}

	/**
	 * Sets the trait class description.
	 *
	 * @param traitClassDescription the new trait class description
	 */
	public void setTraitClassDescription(String traitClassDescription) {
		this.traitClassDescription = traitClassDescription;
	}

	/**
	 * Gets the property description.
	 *
	 * @return the property description
	 */
	public String getPropertyDescription() {
		return this.propertyDescription;
	}

	/**
	 * Sets the property description.
	 *
	 * @param propertyDescription the new property description
	 */
	public void setPropertyDescription(String propertyDescription) {
		this.propertyDescription = propertyDescription;
	}

	/**
	 * Gets the method description.
	 *
	 * @return the method description
	 */
	public String getMethodDescription() {
		return this.methodDescription;
	}

	/**
	 * Sets the method description.
	 *
	 * @param methodDescription the new method description
	 */
	public void setMethodDescription(String methodDescription) {
		this.methodDescription = methodDescription;
	}

	/**
	 * Gets the scale description.
	 *
	 * @return the scale description
	 */
	public String getScaleDescription() {
		return this.scaleDescription;
	}

	/**
	 * Sets the scale description.
	 *
	 * @param scaleDescription the new scale description
	 */
	public void setScaleDescription(String scaleDescription) {
		this.scaleDescription = scaleDescription;
	}

	/**
	 * Gets the variable id.
	 *
	 * @return the variableId
	 */
	public Integer getVariableId() {
		return this.variableId;
	}

	/**
	 * Sets the variable id.
	 *
	 * @param variableId the variableId to set
	 */
	public void setVariableId(Integer variableId) {
		this.variableId = variableId;
	}

	/**
	 * Gets the min value.
	 *
	 * @return the min value
	 */
	public Double getMinValue() {
		return this.minValue;
	}

	/**
	 * Sets the min value.
	 *
	 * @param minValue the new min value
	 */
	public void setMinValue(Double minValue) {
		this.minValue = minValue;
	}

	/**
	 * Gets the max value.
	 *
	 * @return the max value
	 */
	public Double getMaxValue() {
		return this.maxValue;
	}

	/**
	 * Sets the max value.
	 *
	 * @param maxValue the new max value
	 */
	public void setMaxValue(Double maxValue) {
		this.maxValue = maxValue;
	}

	/**
	 * Gets the enumerations.
	 *
	 * @return the enumerations
	 */
	public String getEnumerations() {
		return this.enumerations;
	}

	/**
	 * Sets the enumerations.
	 *
	 * @param enumerations the new enumerations
	 */
	public void setEnumerations(String enumerations) {
		this.enumerations = enumerations;
	}

	public int getPreselectVariableId() {
		return this.preselectVariableId;
	}

	public void setPreselectVariableId(int preselectVariableId) {
		this.preselectVariableId = preselectVariableId;
	}

}
