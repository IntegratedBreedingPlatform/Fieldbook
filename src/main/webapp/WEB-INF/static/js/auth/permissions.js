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
		MANAGE_STUDIES_PERMISSIONS: MANAGE_STUDIES_PERMISSIONS
	});
})();
