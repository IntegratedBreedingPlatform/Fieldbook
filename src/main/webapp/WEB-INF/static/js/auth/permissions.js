(function () {
	'use strict';
	const module = angular.module('auth');


	const MANAGE_STUDIES_PERMISSIONS = [
		'ADMIN',
		'STUDIES',
		'MANAGE_STUDIES'
	];

	const MS_WITHDRAW_INVENTORY_PERMISSIONS = [
		...MANAGE_STUDIES_PERMISSIONS,
		'MS_MANAGE_OBSERVATION_UNITS',
		'MS_WITHDRAW_INVENTORY'
	];
	const MS_CREATE_PENDING_WITHDRAWALS = [
		...MS_WITHDRAW_INVENTORY_PERMISSIONS,
		'MS_CREATE_PENDING_WITHDRAWALS'
	];
	const MS_CREATE_CONFIRMED_WITHDRAWALS = [
		...MS_WITHDRAW_INVENTORY_PERMISSIONS,
		'MS_CREATE_CONFIRMED_WITHDRAWALS'
	];

	const MS_CANCEL_PENDING_TRANSACTIONS = [
		...MS_WITHDRAW_INVENTORY_PERMISSIONS,
		'MS_CANCEL_PENDING_TRANSACTIONS'
	];
	const PREPARE_PLANTING_PERMISSIONS = [
		...MS_CREATE_PENDING_WITHDRAWALS,
		...MS_CREATE_CONFIRMED_WITHDRAWALS
	];

	const MS_MANAGE_FILES_OBSERVATIONS_PERMISSION = [
		...MANAGE_STUDIES_PERMISSIONS,
		'MS_OBSERVATIONS',
		'MS_MANAGE_FILES_OBSERVATIONS'
	];

	const GERMPLASM_AND_CHECKS_PERMISSIONS = [
		'MS_GERMPLASM_AND_CHECKS',
		'MS_VIEW_GERMPLASM_AND_CHECKS',
		'MS_ADD_ENTRY_DETAILS_VARIABLES',
		'MS_MODIFY_ENTRY_DETAILS_VALUES',
		'MS_MODIFY_COLUMNS',
		'MS_REPLACE_GERMPLASM',
		'MS_ADD_NEW_ENTRIES',
		'MS_IMPORT_ENTRY_DETAILS'
	];

	const TREATMENT_FACTORS_PERMISSIONS = [
		'MS_TREATMENT_FACTORS',
		'MS_VIEW_TREATMENT_FACTORS',
		'MS_ADD_TREATMENT_FACTORS_VARIABLES'
	];

	const EXPERIMENTAL_DESIGN_PERMISSIONS = [
		'MS_EXPERIMENTAL_DESIGN',
		'MS_VIEW_EXPERIMENTAL_DESIGN',
		'MS_GENERATE_EXPERIMENTAL_DESIGN',
		'MS_DELETE_EXPERIMENTAL_DESIGN'
	];

	const ENVIRONMENT_PERMISSIONS = [
		'MS_ENVIRONMENT',
		'MS_VIEW_ENVIRONMENT',
		'MS_ADD_ENVIRONMENT_DETAILS_VARIABLES',
		'MS_ADD_ENVIRONMENTAL_CONDITIONS_VARIABLES',
		'MS_MODIFY_NUMBER_OF_ENVIRONMENTS',
		'MS_MODIFY_ENVIRONMENT_VALUES',
		'MS_MANAGE_FILES_ENVIRONMENT'
	];

	module.constant('PERMISSIONS', {
		PREPARE_PLANTING_PERMISSIONS: PREPARE_PLANTING_PERMISSIONS,
		MS_CREATE_PENDING_WITHDRAWALS: MS_CREATE_PENDING_WITHDRAWALS,
		MS_CREATE_CONFIRMED_WITHDRAWALS: MS_CREATE_CONFIRMED_WITHDRAWALS,
		MS_CANCEL_PENDING_TRANSACTIONS: MS_CANCEL_PENDING_TRANSACTIONS,
		MANAGE_FILES_OBSERVATIONS_PERMISSION: MS_MANAGE_FILES_OBSERVATIONS_PERMISSION,
		MANAGE_STUDIES_PERMISSIONS: MANAGE_STUDIES_PERMISSIONS,
		FULL_MANAGE_STUDIES_PERMISSIONS: [
			...MANAGE_STUDIES_PERMISSIONS,
			...GERMPLASM_AND_CHECKS_PERMISSIONS,
			...TREATMENT_FACTORS_PERMISSIONS,
			...EXPERIMENTAL_DESIGN_PERMISSIONS,
			...ENVIRONMENT_PERMISSIONS,
			'CREATE_STUDIES', 'DELETE_STUDY', 'CLOSE_STUDY', 'LOCK_STUDY', 'MS_MANAGE_OBSERVATION_UNITS', 'MS_WITHDRAW_INVENTORY', 'MS_CREATE_PENDING_WITHDRAWALS',
			'MS_CREATE_CONFIRMED_WITHDRAWALS', 'MS_CANCEL_PENDING_TRANSACTIONS', 'MS_MANAGE_FILES_OBSERVATIONS', 'MS_CREATE_LOTS'],

		// Action in The Study permissions
		DELETE_STUDY_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'DELETE_STUDY'],
		CLOSE_STUDY_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'CLOSE_STUDY'],
		LOCK_STUDY_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'LOCK_STUDY'],

		// Required Permission to see the tabs available in the study.
		VIEW_STUDY_SETTINGS_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_STUDY_SETTINGS', 'MS_VIEW_STUDY_SETTINGS'],
		VIEW_GERMPLASM_AND_CHECKS_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_GERMPLASM_AND_CHECKS', 'MS_VIEW_GERMPLASM_AND_CHECKS'],
		VIEW_TREATMENT_FACTORS_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_TREATMENT_FACTORS', 'MS_VIEW_TREATMENT_FACTORS'],
		VIEW_EXPERIMENTAL_DESIGN_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_EXPERIMENTAL_DESIGN', 'MS_VIEW_EXPERIMENTAL_DESIGN'],
		VIEW_ENVIRONMENT_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_ENVIRONMENT', 'MS_VIEW_ENVIRONMENT'],
		VIEW_CROSSES_AND_SELECTIONS_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_CROSSES_AND_SELECTIONS', 'MS_VIEW_CROSSES_AND_SELECTIONS'],
		VIEW_INVENTORY_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_INVENTORY', 'MS_VIEW_INVENTORY'],
		VIEW_SAMPLE_LIST_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_SAMPLE_LISTS', 'MS_VIEW_SAMPLE_LISTS'],
		VIEW_SINGLE_SITE_ANALYSIS_RESULTS_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_SSA_RESULTS', 'MS_VIEW_SSA_RESULTS'],
		VIEW_OBSERVATIONS_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_OBSERVATIONS', 'MS_VIEW_OBSERVATIONS'],

		// Observation and SubObservation
		ADD_OBSERVATION_TRAIT_VARIABLES_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_OBSERVATIONS', 'MS_ADD_OBSERVATION_TRAIT_VARIABLES'],
		ADD_OBSERVATION_SELECTION_VARIABLES_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_OBSERVATIONS', 'MS_ADD_OBSERVATION_SELECTION_VARIABLES'],
		MANAGE_PENDING_OBSERVATION_VALUES_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_OBSERVATIONS', 'MS_MANAGE_PENDING_OBSERVATIONS'],
		MANAGE_CONFIRMED_OBSERVATION_VALUES_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_OBSERVATIONS', 'MS_MANAGE_CONFIRMED_OBSERVATIONS'],
		MANAGE_ACCEPT_PENDING_OBSERVATION_VALUES_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_OBSERVATIONS', 'MS_ACCEPT_PENDING_OBSERVATION'],

		// Single-site Analisys.
		SSA_SUMMARY_STATISTICS_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_SSA_RESULTS', 'MS_SSA_SUMMARY_STATISTICS'],
		SSA_MEANS_BLUE_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_SSA_RESULTS', 'MS_SSA_MEANS_BLUE'],

		// Sample List Export List
		EXPORT_FILE_SAMPLE_LIST_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_SAMPLE_LISTS', 'MS_EXPORT_SAMPLE_LIST'],
		DELETE_SAMPLES_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_SAMPLE_LISTS', 'MS_DELETE_SAMPLES'],

		// Settings Permissions
		ADD_STUDY_SETTINGS_VARIABLES_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_STUDY_SETTINGS', 'MS_ADD_STUDY_SETTINGS_VARIABLES'],

		// Germplasm & Checks Permissions
		MODIFY_COLUMNS_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_GERMPLASM_AND_CHECKS', 'MS_MODIFY_COLUMNS'],
		REPLACE_GERMPLASM_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_GERMPLASM_AND_CHECKS', 'MS_REPLACE_GERMPLASM'],
		ADD_NEW_ENTRIES_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_GERMPLASM_AND_CHECKS', 'MS_ADD_NEW_ENTRIES'],
		ADD_ENTRY_DETAILS_VARIABLES_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_GERMPLASM_AND_CHECKS', 'MS_ADD_ENTRY_DETAILS_VARIABLES'],
		MODIFY_ENTRY_DETAILS_VALUES_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_GERMPLASM_AND_CHECKS', 'MS_MODIFY_ENTRY_DETAILS_VALUES'],
		IMPORT_ENTRY_DETAILS_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_GERMPLASM_AND_CHECKS', 'MS_IMPORT_ENTRY_DETAILS'],

		// Treatment Permissions
		ADD_TREATMENT_FACTORS_VARIABLES_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_TREATMENT_FACTORS', 'MS_ADD_TREATMENT_FACTORS_VARIABLES'],

		// Experimental Design
		DELETE_EXPERIMENTAL_DESIGN_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_EXPERIMENTAL_DESIGN', 'MS_DELETE_EXPERIMENTAL_DESIGN'],
		GENERATE_EXPERIMENTAL_DESIGN_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_EXPERIMENTAL_DESIGN', 'MS_GENERATE_EXPERIMENTAL_DESIGN'],

		// Environment
		ADD_ENVIRONMENT_DETAILS_VARIABLES_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_ENVIRONMENT', 'MS_ADD_ENVIRONMENT_DETAILS_VARIABLES'],
		ADD_ENVIRONMENTAL_CONDITIONS_VARIABLES_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_ENVIRONMENT', 'MS_ADD_ENVIRONMENTAL_CONDITIONS_VARIABLES'],
		MODIFY_NUMBER_OF_ENVIRONMENT_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_ENVIRONMENT', 'MS_MODIFY_NUMBER_OF_ENVIRONMENTS'],
		MODIFY_ENVIRONMENT_VALUES_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_ENVIRONMENT', 'MS_MODIFY_ENVIRONMENT_VALUES'],

		// Study Action
		CREATE_PLANTING_LABELS_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_STUDY_ACTIONS', 'MS_DESIGN_AND_PLANNING_OPTIONS', 'MS_CREATE_PLANTING_LABELS'],
		EXPORT_DESIGN_TEMPLATE_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_STUDY_ACTIONS', 'MS_DESIGN_AND_PLANNING_OPTIONS', 'MS_EXPORT_DESIGN_TEMPLATE'],
		CREATE_LOTS_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_STUDY_ACTIONS', 'MS_CREATE_LOTS'],
		EXECUTE_CALCULATED_VARIABLES_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_STUDY_ACTIONS', 'MS_EXECUTE_CALCULATED_VARIABLES'],
		CREATE_GENOTYPING_SAMPLES_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_STUDY_ACTIONS', 'MS_CREATE_GENOTYPING_SAMPLES'],

		ANALYZE_WITH_STA_BRAPP_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_STUDY_ACTIONS', 'MS_ANALYZE_WITH_STA_BRAPP'],
		ANALYZE_WITH_DECISION_SUPPORT_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_STUDY_ACTIONS', 'MS_ANALYZE_WITH_DECISION_SUPPORT'],

		ADVANCES_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_STUDY_ACTIONS', 'MS_ADVANCES', 'MS_ADVANCE_STUDY', 'MS_ADVANCE_STUDY_FOR_PLANTS'],
		ADVANCE_STUDY_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_STUDY_ACTIONS', 'MS_ADVANCES', 'MS_ADVANCE_STUDY'],
		ADVANCE_STUDY_FOR_PLANTS_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_STUDY_ACTIONS', 'MS_ADVANCES', 'MS_ADVANCE_STUDY_FOR_PLANTS'],

		CREATE_SUB_OBSERVATION_UNITS_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_STUDY_ACTIONS', 'MS_MANAGE_OBSERVATION_UNITS', 'MS_CREATE_SUB_OBSERVATION_UNITS'],
		CHANGE_PLOT_ENTRY_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_STUDY_ACTIONS', 'MS_MANAGE_OBSERVATION_UNITS', 'MS_CHANGE_PLOT_ENTRY'],
		EXPORT_STUDY_BOOK_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_STUDY_ACTIONS', 'MS_MANAGE_OBSERVATION_UNITS', 'MS_EXPORT_STUDY_BOOK'],
		EXPORT_STUDY_ENTRIES_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'MS_STUDY_ACTIONS', 'MS_MANAGE_OBSERVATION_UNITS', 'EXPORT_STUDY_ENTRIES'],
	});
})();
