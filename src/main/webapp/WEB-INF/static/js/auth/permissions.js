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

	const CREATE_LOTS_PERMISSIONS = [
		...MANAGE_STUDIES_PERMISSIONS,
		'MS_CREATE_LOTS'
	];

	const MS_MANAGE_FILES_PERMISSION = [
		...MANAGE_STUDIES_PERMISSIONS,
		'MS_MANAGE_OBSERVATION_UNITS',
		'MS_MANAGE_FILES'
	];

	module.constant('PERMISSIONS', {
		PREPARE_PLANTING_PERMISSIONS: PREPARE_PLANTING_PERMISSIONS,
		MS_CREATE_PENDING_WITHDRAWALS: MS_CREATE_PENDING_WITHDRAWALS,
		MS_CREATE_CONFIRMED_WITHDRAWALS: MS_CREATE_CONFIRMED_WITHDRAWALS,
		MS_CANCEL_PENDING_TRANSACTIONS : MS_CANCEL_PENDING_TRANSACTIONS,
		MS_CREATE_LOTS_PERMISSIONS : CREATE_LOTS_PERMISSIONS,
		MS_MANAGE_FILES_PERMISSION : MS_MANAGE_FILES_PERMISSION,
		MANAGE_STUDIES_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'CREATE_STUDIES', 'DELETE_STUDY', 'CLOSE_STUDY', 'LOCK_STUDY','MS_MANAGE_OBSERVATION_UNITS','MS_WITHDRAW_INVENTORY','MS_CREATE_PENDING_WITHDRAWALS',
		'MS_CREATE_CONFIRMED_WITHDRAWALS','MS_CANCEL_PENDING_TRANSACTIONS','MS_MANAGE_FILES','MS_CREATE_LOTS', 'GERMPLASM_AND_CHECKS','VIEW_GERMPLASM_AND_CHECKS','ADD_ENTRY_DETAILS_VARIABLES','ADD_ENTRY_DETAILS_VALUES',
		'MODIFY_COLUMNS','REPLACE_GERMPLASM','ADD_NEW_ENTRIES','IMPORT_ENTRY_DETAILS'],
		DELETE_STUDY_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'DELETE_STUDY'],
		CLOSE_STUDY_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'CLOSE_STUDY'],
		LOCK_STUDY_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'LOCK_STUDY'],
		MODIFY_COLUMNS_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'GERMPLASM_AND_CHECKS', 'MODIFY_COLUMNS'],
		REPLACE_GERMPLASM_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'GERMPLASM_AND_CHECKS', 'REPLACE_GERMPLASM'],
		ADD_NEW_ENTRIES_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'GERMPLASM_AND_CHECKS', 'ADD_NEW_ENTRIES'],
		ADD_ENTRY_DETAILS_VARIABLES_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'GERMPLASM_AND_CHECKS', 'ADD_ENTRY_DETAILS_VARIABLES'],
		ADD_ENTRY_DETAILS_VALUES_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'GERMPLASM_AND_CHECKS', 'ADD_ENTRY_DETAILS_VALUES'],
		IMPORT_ENTRY_DETAILS_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'GERMPLASM_AND_CHECKS', 'IMPORT_ENTRY_DETAILS'],
		GERMPLASM_AND_CHECKS_ACTION_PERMISSIONS: [...MANAGE_STUDIES_PERMISSIONS, 'GERMPLASM_AND_CHECKS', 'REPLACE_GERMPLASM', 'ADD_NEW_ENTRIES', 'IMPORT_ENTRY_DETAILS', 'MODIFY_COLUMNS']
	});
})();
