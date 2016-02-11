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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.presets.ProgramPreset;

import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;
import com.efficio.fieldbook.web.label.printing.bean.LabelPrintingPresets;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;

/**
 * The Interface LabelPrintingService.
 */
public interface LabelPrintingService {

	/**
	 * Generate pdf labels.
	 *
	 * @param trialInstances the trial instances
	 * @param userLabelPrinting the user label printing
	 * @param baos the baos
	 * @return the string
	 * @throws LabelPrintingException the label printing exception
	 */
	String generateLabels(String labelType, List<StudyTrialInstanceInfo> trialInstances, UserLabelPrinting userLabelPrinting, ByteArrayOutputStream baos)
					throws LabelPrintingException;
	/**
	 * Gets the available label fields for FieldMap.
	 *
	 * @param isTrial the is trial
	 * @param hasFieldMap the has field map
	 * @param locale the locale
	 * @return the available label fields
	 */
	List<LabelFields> getAvailableLabelFieldsForFieldMap(boolean isTrial, boolean hasFieldMap, Locale locale);

	/**
	 * Gets the available label fields for Nursery, Trial.
	 *
	 * @param isTrial the is trial
	 * @param hasFieldMap the has field map
	 * @param locale the locale
	 * @return the available label fields
	 */
	List<LabelFields> getAvailableLabelFieldsForStudy(boolean isTrial, boolean hasFieldMap, Locale locale, int studyID);

	/***
	 * Get available label fields for Stock List
	 *
	 * @param listType
	 * @param locale
	 * @param studyID
	 * @return
	 */
	List<LabelFields> getAvailableLabelFieldsForStockList(GermplasmListType listType, Locale locale, int studyID);

	/**
	 * Check and set fieldmap properties.
	 *
	 * @param userLabelPrinting the user label printing
	 * @param fieldMapInfoDetail the field map info detail
	 * @return true, if successful
	 */
	boolean checkAndSetFieldmapProperties(UserLabelPrinting userLabelPrinting, FieldMapInfo fieldMapInfoDetail);

	/**
	 *
	 * @param presetId
	 * @param presetType
	 * @return
	 * @throws MiddlewareQueryException
	 */
	LabelPrintingPresets getLabelPrintingPreset(Integer presetId, Integer presetType) throws MiddlewareQueryException;

	/**
	 *
	 * @param programPresetId
	 * @return
	 * @throws MiddlewareQueryException
	 */
	ProgramPreset getLabelPrintingProgramPreset(Integer programPresetId) throws MiddlewareQueryException;

	/**
	 *
	 * @param presetName
	 * @param programId
	 * @param presetType
	 * @return
	 * @throws MiddlewareQueryException
	 */
	List<LabelPrintingPresets> getAllLabelPrintingPresetsByName(String presetName, Integer programId, Integer presetType)
					throws MiddlewareQueryException;

	/**
	 *
	 * @param programId
	 * @return
	 * @throws LabelPrintingException
	 */
	List<LabelPrintingPresets> getAllLabelPrintingPresets(Integer programId) throws LabelPrintingException;

	/**
	 *
	 * @param presetType
	 * @param presetId
	 * @return
	 * @throws LabelPrintingException
	 */
	String getLabelPrintingPresetConfig(int presetType, int presetId) throws LabelPrintingException;

	/**
	 *
	 * @param settingsName
	 * @param xmlConfig
	 * @param programId
	 * @throws MiddlewareQueryException
	 */
	void saveOrUpdateLabelPrintingPresetConfig(String settingsName, String xmlConfig, Integer programId) throws MiddlewareQueryException;

	void populateUserSpecifiedLabelFields(List<FieldMapTrialInstanceInfo> trialFieldMap, Workbook workbook, String selectedFields,
			boolean isTrial, boolean isStockList);

	void deleteProgramPreset(Integer programPresetId) throws MiddlewareQueryException;

	/**
	 * Returns if the list is either ADVANCED list of CROSSES list
	 * 
	 * @param type
	 * @return
	 */
	GermplasmListType getStockListType(String type);

	/**
	 * 
	 * @param stockList
	 * @return
	 * @throws MiddlewareQueryException
	 */
    Map<String, InventoryDetails> getInventoryDetailsMap(GermplasmList stockList);
}
