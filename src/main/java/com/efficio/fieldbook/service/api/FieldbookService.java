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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Variable;

import com.efficio.fieldbook.web.common.bean.AdvanceResult;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.trial.bean.BVDesignOutput;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.FieldbookProperties;

/**
 * This is used by the trial manager and nursery manager in communicating to the data access layer, manipulating workbook files and
 * generating design from the design runner
 */
public interface FieldbookService {

	/**
	 * Takes in an input stream representing the Excel file to be read, and returns the temporary file name used to store it in the system.
	 *
	 * @param in the in
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	String storeUserWorkbook(InputStream in) throws IOException;

	/**
	 * Logic for advancing a nursery following a particular naming convention.
	 *
	 * @param advanceInfo the advance info
	 * @param workbook the workbook
	 * @return the list
	 * @throws RuleException
	 */
	AdvanceResult advanceNursery(AdvancingNursery advanceInfo, Workbook workbook) throws RuleException;

	/**
	 * Filters the variables based on the current setting mode and excludes the selected ones.
	 *
	 * @param mode the mode
	 * @param selectedList the selected list
	 * @return the list
	 */
	List<StandardVariableReference> filterStandardVariablesForSetting(int mode, Collection<SettingDetail> selectedList);

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
	 * @param projectId the project id
	 * @return the all possible values favorite
	 */
	List<ValueReference> getAllPossibleValuesFavorite(int id, String projectId);

	/**
	 * Gets the all possible values by psmr.
	 *
	 * @param property the property
	 * @param scale the scale
	 * @param method the method
	 * @param phenotypeType the phenotype type
	 * @return the all possible values by psmr
	 */
	List<ValueReference> getAllPossibleValuesByPSMR(String property, String scale, String method, PhenotypicType phenotypeType);

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
	 * Retrieves the person by user id
	 *
	 * @param userId
	 * @return
	 */
	String getPersonByUserId(int userId);

	/**
	 * Gets the term by id.
	 *
	 * @param termId the term id
	 * @return the term by id
	 */
	Term getTermById(int termId);

	/**
	 * Gets the all breeding methods.
	 *
	 * @param programUUID - unique id of the current program
	 * @param isFilterOutGenerative the is filter out generative
	 * @return the all breeding methods
	 */
	List<ValueReference> getAllBreedingMethods(boolean isFilterOutGenerative, String programUUID);

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
	void createIdNameVariablePairs(Workbook workbook, List<SettingDetail> settingDetails, String idNamePairs,
			boolean deleteIdWhenNameExists);

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

	/**
	 * Add/Updates/Deletes check variables.
	 *
	 * @param userSelection the userSelection
	 * @param form the form
	 */
	void manageCheckVariables(UserSelection userSelection, ImportGermplasmListForm form);

	BVDesignOutput runBVDesign(WorkbenchService workbenchService, FieldbookProperties fieldbookProperties, MainDesign design)
			throws IOException;

	void saveStudyImportedCrosses(List<Integer> crossesIds, Integer studyId);

	void addConditionsToTrialObservationsIfNecessary(Workbook workbook);

	void saveStudyColumnOrdering(Integer studyId, String studyName, String columnOrderDelimited, Workbook workbook);
}
