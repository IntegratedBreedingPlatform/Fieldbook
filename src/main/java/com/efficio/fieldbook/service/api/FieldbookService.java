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

package com.efficio.fieldbook.service.api;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.form.ImportGermplasmListForm;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.Operation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This is used by the trial manager and nursery manager in communicating to the data access layer, manipulating workbook files and
 * generating design from the design runner
 */
public interface FieldbookService {

	/**
	 * Get all possible values.
	 *
	 * @param id the id
	 * @return the all possible values
	 */
	List<ValueReference> getAllPossibleValues(int id);

	List<ValueReference> getAllPossibleValues(Variable variable);

	/**
	 * Gets the all possible values favorite.
	 *
	 * @param id the id
	 * @param programUUID the project id
	 * @return the all possible values favorite
	 */
	public List<ValueReference> getAllPossibleValuesFavorite(final int id, final String programUUID, Boolean filtered);

	/**
	 * Gets the value.
	 *
	 * @param id the id
	 * @param valueOrId the value or id
	 * @param isCategorical the is categorical
	 * @return the value
	 */
	String getValue(int id, String valueOrId, boolean isCategorical);

	/**
	 * Gets the all breeding methods.
	 *
	 * @param isFilterOutGenerative the is filter out generative
	 * @return the all breeding methods
	 */
	List<ValueReference> getAllBreedingMethods(boolean isFilterOutGenerative);

	/**
	 * Sets the all possible values in workbook.
	 *
	 * @param workbook the new all possible values in workbook
	 */
	void setAllPossibleValuesInWorkbook(Workbook workbook);

	/**
	 * Gets the check list.
	 *
	 * @return the check list
	 */
	List<Enumeration> getCheckTypeList();

	/**
	 * Creates the id name variable pairs.
	 *
	 * @param workbook the workbook
	 * @param settingDetails the setting details
	 * @param idNamePairs the id name pairs
	 * @param deleteIdWhenNameExists the delete id when name exists
	 */
	void createIdNameVariablePairs(Workbook workbook, List<SettingDetail> settingDetails, String idNamePairs, boolean deleteIdWhenNameExists);

	/**
	 * Creates a MeasurementVariable from StandardVariable
	 * @param idToCreate
	 * @param value
	 * @param operation
	 * @param role
	 * @return
	 */
	MeasurementVariable createMeasurementVariable(String idToCreate, String value, Operation operation, PhenotypicType role);

	/**
	 * Creates the id code name variable pairs.
	 *
	 * @param workbook the workbook
	 * @param idCodeNamePairs the id code name pairs
	 */
	void createIdCodeNameVariablePairs(Workbook workbook, String idCodeNamePairs);

	/**
	 * Gets the id name pair for retrieve and save.
	 *
	 * @return the id name pair for retrieve and save
	 */
	Map<String, String> getIdNamePairForRetrieveAndSave();

	/**
	 * Gets the id code name pair for retrieve and save.
	 *
	 * @return the id code name pair for retrieve and save
	 */
	Map<String, List<String>> getIdCodeNamePairForRetrieveAndSave();

	/**
	 * Gets the variable possible values.
	 *
	 * @param var the var
	 * @return the variable possible values
	 */
	List<ValueReference> getVariablePossibleValues(MeasurementVariable var);

	/**
	 * Get all possible values.
	 *
	 * @param id the id
	 * @param isGetAllRecords the is get all records
	 * @return the all possible values
	 */
	List<ValueReference> getAllPossibleValues(int id, boolean isGetAllRecords);

	void addConditionsToTrialObservationsIfNecessary(Workbook workbook);

	void saveStudyColumnOrdering(Integer studyId, String columnOrderDelimited, Workbook workbook);

	public List<ValueReference> getAllPossibleValuesWithFilter(final int id, boolean filtered);

	/**
	 * Adds the specified variable to the measurementVariable list if it does not yet exist in the list.
	 *
	 * @param variableIdToAdd - The variable id
	 * @param phenotypicType
	 * @param measurementVariables
	 */
	void addMeasurementVariableToList(MeasurementVariable measurementVariable, List<MeasurementVariable> measurementVariables);

	/**
	 * Add the STUDY_UID condition and Observation Unit ID factor to workbook
	 * @param workbook
	 */
	void addStudyUUIDConditionAndObsUnitIDFactorToWorkbook(Workbook workbook, boolean addObsUnitIdToMeasurementRows);

	/**
	 * Adds the specified variable to the measurementRows. This will add a blank measurementData on each measurement row for the specified variable.
	 *
	 * @param variableIdToAdd
	 * @param phenotypicType
	 * @param observations
	 */
	void addMeasurementVariableToMeasurementRows(MeasurementVariable measurementVariable, List<MeasurementRow> observations);

}
