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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum AppConstants {

	// Label Printing
	SIZE_OF_PAPER_A4, SIZE_OF_PAPER_LETTER

	, AVAILABLE_LABEL_FIELDS_ENTRY_NUM, AVAILABLE_LABEL_FIELDS_GID, AVAILABLE_LABEL_FIELDS_GERMPLASM_NAME, AVAILABLE_LABEL_FIELDS_YEAR, AVAILABLE_LABEL_FIELDS_SEASON, AVAILABLE_LABEL_FIELDS_TRIAL_NAME, AVAILABLE_LABEL_FIELDS_TRIAL_INSTANCE_NUM, AVAILABLE_LABEL_FIELDS_REP, AVAILABLE_LABEL_FIELDS_LOCATION, AVAILABLE_LABEL_FIELDS_BLOCK_NAME, AVAILABLE_LABEL_FIELDS_PLOT, AVAILABLE_LABEL_FIELDS_NURSERY_NAME, AVAILABLE_LABEL_FIELDS_PARENTAGE, AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES, AVAILABLE_LABEL_FIELDS_FIELD_NAME, AVAILABLE_LABEL_BARCODE, FIELDMAP_LABEL_FIELDS, AVAILABLE_LABEL_SEED_INVENTORY_AMOUNT, AVAILABLE_LABEL_SEED_INVENTORY_SCALE, AVAILABLE_LABEL_SEED_LOT_ID, AVAILABLE_LABEL_FIELDS_DESIGNATION, AVAILABLE_LABEL_FIELDS_CROSS, AVAILABLE_LABEL_FIELDS_STOCK_ID

	// Field Map
	, ROW_COLUMN, SERPENTINE, PLANTING_ORDER_ROW_COLUMN, PLANTING_ORDER_SERPENTINE

	// Nursery Manager

	, GERMPLASM_LIST_CHOOSE_SPECIFY_CHECK_SELECT_ONE_OR_MORE_IN_THE_LIST

	, NAMING_CONVENTION_CIMMYT_WHEAT, NAMING_CONVENTION_CIMMYT_MAIZE, NAMING_CONVENTION_OTHER_CROPS

	, SINGLE_PLANT_SELECTION_SF

	, METHOD_CHOICE_YES, METHOD_CHOICE_NO, LINE_CHOICE_YES, LINE_CHOICE_NO

	, SELECTED_BULK_SF, RANDOM_BULK_SF, RANDOM_BULK_CF, HALF_MASS_SELECTION, DOUBLE_HAPLOID_LINE

	, METHOD_TYPE_GEN

	, METHOD_UNKNOWN_DERIVATIVE_METHOD_SF, METHOD_UNKNOWN_GENERATIVE_METHOD_SF

	, ENTRY_CODE_PREFIX

	, NAME_SEPARATOR

	, GERMPLASM_LIST_TYPE_HARVEST, GERMPLASM_LIST_TYPE_GENERIC_LIST

	, TOOL_NAME_NURSERY_MANAGER_WEB, TOOL_NAME_TRIAL_MANAGER_WEB, TOOL_NAME_OLD_FIELDBOOK, TOOL_NAME_BREEDING_VIEW, LISTS

	, PRINCIPAL_INVESTIGATOR, LOCATION, STUDY_NAME, STUDY_TITLE, OBJECTIVE

	, FEMALE_NURSERY, FEMALE_ENTRY, FEMALE_PLOT, MALE_NURSERY, MALE_ENTRY, MALE_PLOT, BREEDING_METHOD, CROSSING_DATE, SEEDS_HARVESTED, NOTES

	// Import Germplasm
	, CONDITION, DESCRIPTION, PROPERTY, SCALE, METHOD, DATA_TYPE, VALUE, FACTOR, ENTRY, DESIGNATION, DESIG, GID, CROSS, SOURCE, ENTRY_CODE, PLOT, CHECK, TYPE_OF_ENTRY, CODE, ASSIGNED, C, CONSTANT, VARIATE

	, EXPORT_NURSERY_FIELDLOG_FIELDROID, EXPORT_NURSERY_EXCEL, EXPORT_DATAKAPTURE, EXPORT_KSU_EXCEL, EXPORT_KSU_CSV, EXPORT_CSV

	, LIST_DATE, LIST_TYPE

	, FILE_NOT_EXCEL_ERROR, FILE_NOT_CSV_ERROR, FILE_NOT_FOUND_ERROR

	, EXPORT_FIELDLOG_SUFFIX, EXPORT_XLS_SUFFIX, DATAKAPTURE_TRAITS_SUFFIX, EXPORT_CSV_SUFFIX, EXPORT_KSU_TRAITS_SUFFIX

	, CREATE_NURSERY_REQUIRED_FIELDS, CREATE_PLOT_REQUIRED_FIELDS, HIDE_NURSERY_FIELDS, HIDE_TRIAL_FIELDS, HIDE_TRIAL_VARIABLE_DBCV_FIELDS, HIDE_TRIAL_VARIABLE_SETTINGS_FIELDS, HIDE_PLOT_FIELDS, HIDE_GERMPLASM_DESCRIPTOR_HEADER_TABLE, FILTER_NURSERY_FIELDS, ID_NAME_COMBINATION, ID_CODE_NAME_COMBINATION_STUDY, ID_CODE_NAME_COMBINATION_VARIATE, HIDE_ID_VARIABLES, HIDE_STUDY_DETAIL_VARIABLES, REMOVE_FACTORS_IN_USE_PREVIOUS_STUDY, CREATE_TRIAL_REQUIRED_FIELDS, HIDE_TRIAL_MANAGEMENT_SETTINGS_FIELDS, CREATE_TRIAL_ENVIRONMENT_REQUIRED_FIELDS, CREATE_TRIAL_PLOT_REQUIRED_FIELDS, CREATE_TRIAL_DEFAULT_PLOT_FIELDS, CREATE_TRIAL_EXP_DESIGN_DEFAULT_FIELDS, CREATE_TRIAL_REMOVE_TREATMENT_FACTOR_IDS, HIDE_TRIAL_ENVIRONMENT_FIELDS, HIDE_TRIAL_ENVIRONMENT_FIELDS_FROM_POPUP, DEFAULT_NO_OF_ENVIRONMENT_COUNT, CHECK_VARIABLES, NURSERY_BASIC_DETAIL_FIELDS_HIDDEN_LABELS

	, LOCATION_ID, BREEDING_METHOD_ID, BREEDING_METHOD_CODE, START_DATE_ID, END_DATE_ID, STUDY_NAME_ID, COOPERATOR_ID, COOPERATOR_NAME

	, LABEL

	, NUMERIC_DATA_TYPE

	, CROP_WHEAT, CROP_MAIZE

	, MAIZE_BREEDING_METHOD_SELFED_SHELLED, MAIZE_BREEDING_METHOD_SELFED_BULKED, MAIZE_BREEDING_METHOD_SIB_INCREASED, MAIZE_BREEDING_METHOD_COLCHICINIZE

	, ZIP_FILE_SUFFIX, TRIAL_INSTANCE_FACTOR, EXPERIMENTAL_DESIGN_VALUES, REPLICATES_VALUES, BLOCK_SIZE_VALUES, BLOCK_PER_REPLICATE_VALUES, VALUES, BLOCK_PER_REPLICATE, REPLICATES, BLOCK_SIZE, EXPERIMENTAL_DESIGN, TRIAL_ENVIRONMENT_ORDER

	, MANNER_IN_TURN, MANNER_PER_LOCATION

	, DESIGN_LAYOUT_SAME_FOR_ALL, DESIGN_LAYOUT_INDIVIDUAL

	, OBJECTIVE_ID, OCC, NURSERIES, TRIALS

	, FOLDER_ICON_PNG, STUDY_ICON_PNG, BASIC_DETAILS_PNG

	, NURSERY_BASIC_REQUIRED_FIELDS, ENTRIES_LABEL, HAS_MEASUREMENTS_LABEL, HAS_FIELDMAP_LABEL, HIDDEN_FIELDS, SPFLD_ENTRIES, SPFLD_COUNT_VARIATES, SPFLD_HAS_FIELDMAP, SPFLD_PLOT_COUNT, SELECTION_VARIATES_PROPERTIES, FIXED_NURSERY_VARIABLES

	, PROPERTY_BREEDING_METHOD, PROPERTY_PLANTS_SELECTED

	, ADVANCING_YEAR_RANGE, CHAR_LIMIT, PLEASE_CHOOSE

	, BM_CODE, DBID, DBCV, ID_SUFFIX

	, TABLE_HEADER_KEY_SUFFIX

	, TRIAL_BASIC_REQUIRED_FIELDS, EXP_DESIGN_TIME_LIMIT

	, ENTRY_TYPE_ID, HIDE_TRIAL_VARIABLES, EXP_DESIGN_VARIABLES, EXP_DESIGN_REQUIRED_VARIABLES, EXP_DESIGN_REQUIRED_WITHOUT_PLOT_NO_VARIABLES, STANDARD_VARIABLE_NAME_LIMIT, NURSERY, TRIAL

	, LABEL_PRINTING_PDF, LABEL_PRINTING_EXCEL, LABEL_PRINTING_CSV

	, MESSAGE

	, EXPORT_ADVANCE_NURSERY_EXCEL, EXPORT_ADVANCE_NURSERY_CSV

	, TEMPORARY_INVENTORY_AMOUNT, TEMPORARY_INVENTORY_SCALE, TEMPORARY_INVENTORY_COMMENT

	, ADVANCE_ZIP_DEFAULT_FILENAME

	, FILTER_MEAN_AND_STATISCAL_VARIABLES_IS_A_IDS

	, DESIGN_TEMPLATE_ALPHA_LATTICE_FOLDER;

	private static final Logger LOG = LoggerFactory.getLogger(AppConstants.class);

	private static Properties configFile = new Properties();

	public static final String PROPERTY_FILE = "appconstants.properties";

	static {
		try {
			AppConstants.configFile.load(AppConstants.class.getClassLoader().getResourceAsStream(AppConstants.PROPERTY_FILE));
		} catch (final IOException e) {
			AppConstants.LOG.error("Error accessing property file: " + AppConstants.PROPERTY_FILE, e);
		}

	}

	public int getInt() {
		int appConstant = -1;
		final String value = this.getString().trim();
		if (value != null) {
			appConstant = Integer.valueOf(value);
		}
		return appConstant;
	}

	public boolean isInt() {
		final String value = this.getString().trim();
		if (value != null) {
			try {
				Integer.valueOf(value);
			} catch (final NumberFormatException e) {
				return false;
			}
		}
		return true;
	}

	public String getString() {
		String value = null;
		try {
			value = AppConstants.configFile.getProperty(this.toString());
		} catch (final NumberFormatException e) {
			AppConstants.LOG.error("Value not numeric.", e);
		}
		return value;
	}

	public static String getString(final String labelKey) {
		String value = null;
		try {
			value = AppConstants.configFile.getProperty(labelKey);
		} catch (final NumberFormatException e) {
			AppConstants.LOG.error("Value not numeric.", e);
		}
		return value;
	}

	public Map<String, String> getMapOfValues() {
		final String constantValue = this.getString();
		final Map<String, String> map = new HashMap<String, String>();
		final String[] pairs = constantValue.split(",");
		if (pairs != null) {
			for (final String pair : pairs) {
				final String[] separated = pair.split("\\|");
				if (separated != null && separated.length == 2) {
					map.put(separated[0], separated[1]);
				}
			}
		}
		return map;
	}

	public List<String> getList() {
		final String[] arr = this.getString().split(",");
		if (arr != null) {
			return Arrays.asList(arr);
		}
		return new ArrayList<String>();
	}

	public List<Integer> getIntegerList() {
		final List<Integer> list = new ArrayList<Integer>();
		final String[] arr = this.getString().split(",");
		if (arr != null) {
			for (final String rec : arr) {
				if (NumberUtils.isNumber(rec)) {
					list.add(Integer.valueOf(rec));
				}
			}
		}
		return list;
	}
}
