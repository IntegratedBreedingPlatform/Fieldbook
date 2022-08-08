'use strict';

describe('PlantingPreparationModalCtrl:', function () {
	var $controller,
		$rootScope,
		$q,
		$timeout,
		controller,
		scope;

	// Mocks
	var studyContextMock = {
			studyId: 1,
			cropName: 'maize',
			measurementDatasetId: 2009
		},
		uibModalInstanceMock = {
			close: jasmine.createSpy('close'),
			dismiss: jasmine.createSpy('dismiss'),
			result: {
				then: jasmine.createSpy('then')
			}
		},
		InventoryServiceMock = jasmine.createSpyObj('InventoryService', [
			'queryUnits'
		]),
		PlantingPreparationServiceMock = jasmine.createSpyObj('datasetService', [
			'getPlantingPreparationData',
			'getMetadata',
			'confirmPlanting'
		]),
		datasetServiceMock = jasmine.createSpyObj('datasetService', [
			'getDataset'
		]),
		HasAnyAuthorityServiceMock = {},
		PERMISSIONSMock	= [],
		VARIABLE_TYPESMock = [];

	beforeEach(function (done) {
		module('manageTrialApp');

		module(function ($provide) {
			$provide.value("datasetService", datasetServiceMock);
			$provide.value("PlantingPreparationService", PlantingPreparationServiceMock);
			$provide.value("InventoryService", InventoryServiceMock);
		});

		inject(function(_$controller_, _$rootScope_, _$q_, $injector, _$timeout_, $httpBackend) {
			$controller = _$controller_;
			$rootScope = _$rootScope_;
			$timeout = _$timeout_;
			$q = _$q_;

			scope = $rootScope.$new();
			scope.$resolve = {
				searchComposite: {
					itemIds: [123501, 123505]
				},
				datasetId: 1
			};

			$httpBackend.whenGET('/Fieldbook/TrialManager/createTrial/trialSettings').respond(200, {data: "ok"});

			datasetServiceMock.getDataset.and.returnValue($q.resolve(getMockVariablesByFilter()))
			PlantingPreparationServiceMock.getPlantingPreparationData.and.returnValue($q.resolve(getMockPlantingPreparationData()))
			InventoryServiceMock.queryUnits.and.returnValue($q.resolve(getMockUnits()))

			controller = $controller('PlantingPreparationModalCtrl', {
				$scope: scope,
				$uibModalInstance: uibModalInstanceMock,
				service: PlantingPreparationServiceMock,
				InventoryService: InventoryServiceMock,
				studyContext: studyContextMock,
				HasAnyAuthorityService: HasAnyAuthorityServiceMock,
				PERMISSIONS: PERMISSIONSMock,
				datasetService: datasetServiceMock,
				VARIABLE_TYPES: VARIABLE_TYPESMock
			});

			scope.initPromise.then(function () {
				done();
			});

			$rootScope.$apply();
		});
	});

	describe('an entry', function () {
		it('should pass validation when availableBalance > withdrawal', function () {
			const entry = scope.entryMap[8264][9];
			entry.stockSelected = entry.stockByStockId['SID1-8']
			scope.units[entry.stockSelected.unitId].amountPerPacket = 50;
			expect(scope.isValid(entry)).toBe(false);

			scope.units[entry.stockSelected.unitId].amountPerPacket = 44;
			expect(scope.isValid(entry)).toBe(true);
		});
	})

	function getMockVariablesByFilter() {
		return {
			"datasetId": 25039,
			"name": "Observations",
			"datasetTypeId": 4,
			"studyId": 25037,
			"cropName": "maize",
			"variables": [
				{
					"termId": 8240,
					"name": "GID",
					"alias": "GID",
					"description": "Germplasm identifier - assigned (DBID)",
					"scale": "Germplasm id",
					"scaleId": 1907,
					"method": "Assigned",
					"property": "Germplasm id",
					"dataType": "Germplasm List",
					"value": null,
					"label": "",
					"dataTypeId": 1135,
					"possibleValues": null,
					"possibleValuesString": null,
					"minRange": null,
					"maxRange": null,
					"scaleMinRange": null,
					"scaleMaxRange": null,
					"variableMinRange": null,
					"variableMaxRange": null,
					"required": false,
					"treatmentLabel": null,
					"operation": null,
					"role": null,
					"variableType": "GERMPLASM_DESCRIPTOR",
					"formula": null,
					"cropOntology": null,
					"factor": true,
					"dataTypeCode": "C",
					"systemVariable": true
				},
				{
					"termId": 8250,
					"name": "DESIGNATION",
					"alias": "DESIGNATION",
					"description": "Germplasm identifier - assigned (DBCV)",
					"scale": "Germplasm name",
					"scaleId": 1908,
					"method": "Assigned",
					"property": "Germplasm id",
					"dataType": "Germplasm List",
					"value": null,
					"label": "",
					"dataTypeId": 1135,
					"possibleValues": null,
					"possibleValuesString": null,
					"minRange": null,
					"maxRange": null,
					"scaleMinRange": null,
					"scaleMaxRange": null,
					"variableMinRange": null,
					"variableMaxRange": null,
					"required": false,
					"treatmentLabel": null,
					"operation": null,
					"role": null,
					"variableType": "GERMPLASM_DESCRIPTOR",
					"formula": null,
					"cropOntology": null,
					"factor": true,
					"dataTypeCode": "C",
					"systemVariable": true
				},
				{
					"termId": 8201,
					"name": "OBS_UNIT_ID",
					"alias": "OBS_UNIT_ID",
					"description": "Field observation unit id - assigned (text)",
					"scale": "Text",
					"scaleId": 6020,
					"method": "Assigned",
					"property": "Field plot",
					"dataType": "Character",
					"value": null,
					"label": "",
					"dataTypeId": 1120,
					"possibleValues": null,
					"possibleValuesString": null,
					"minRange": null,
					"maxRange": null,
					"scaleMinRange": null,
					"scaleMaxRange": null,
					"variableMinRange": null,
					"variableMaxRange": null,
					"required": false,
					"treatmentLabel": null,
					"operation": null,
					"role": null,
					"variableType": "GERMPLASM_DESCRIPTOR",
					"formula": null,
					"cropOntology": null,
					"factor": true,
					"dataTypeCode": "T",
					"systemVariable": true
				},
				{
					"termId": 8230,
					"name": "ENTRY_NO",
					"alias": "ENTRY_NO",
					"description": "Germplasm entry - enumerated (number)",
					"scale": "Number",
					"scaleId": 6040,
					"method": "Enumerated",
					"property": "Germplasm entry",
					"dataType": "Numeric",
					"value": null,
					"label": "",
					"dataTypeId": 1110,
					"possibleValues": null,
					"possibleValuesString": null,
					"minRange": null,
					"maxRange": null,
					"scaleMinRange": null,
					"scaleMaxRange": null,
					"variableMinRange": null,
					"variableMaxRange": null,
					"required": false,
					"treatmentLabel": null,
					"operation": null,
					"role": null,
					"variableType": "ENTRY_DETAIL",
					"formula": null,
					"cropOntology": null,
					"factor": true,
					"dataTypeCode": "N",
					"systemVariable": true
				},
				{
					"termId": 8255,
					"name": "ENTRY_TYPE",
					"alias": "ENTRY_TYPE",
					"description": "Entry type (test/check)- assigned (type)",
					"scale": "Type of ENTRY_TYPE",
					"scaleId": 17269,
					"method": "Assigned",
					"property": "Entry type",
					"dataType": "Categorical",
					"value": null,
					"label": "",
					"dataTypeId": 1130,
					"possibleValues": [
						{
							"id": 10170,
							"name": "T",
							"description": "Test entry",
							"programUUID": null,
							"key": "10170",
							"displayDescription": "T= Test entry",
							"folder": false,
							"study": false
						},
						{
							"id": 10190,
							"name": "D",
							"description": "Disease check",
							"programUUID": null,
							"key": "10190",
							"displayDescription": "D= Disease check",
							"folder": false,
							"study": false
						},
						{
							"id": 10185,
							"name": "X",
							"description": "Non Replicated",
							"programUUID": null,
							"key": "10185",
							"displayDescription": "X= Non Replicated",
							"folder": false,
							"study": false
						},
						{
							"id": 10180,
							"name": "C",
							"description": "Check entry",
							"programUUID": null,
							"key": "10180",
							"displayDescription": "C= Check entry",
							"folder": false,
							"study": false
						},
						{
							"id": 10200,
							"name": "S",
							"description": "Stress check",
							"programUUID": null,
							"key": "10200",
							"displayDescription": "S= Stress check",
							"folder": false,
							"study": false
						}
					],
					"possibleValuesString": "",
					"minRange": null,
					"maxRange": null,
					"scaleMinRange": null,
					"scaleMaxRange": null,
					"variableMinRange": null,
					"variableMaxRange": null,
					"required": false,
					"treatmentLabel": null,
					"operation": null,
					"role": null,
					"variableType": "ENTRY_DETAIL",
					"formula": null,
					"cropOntology": null,
					"factor": true,
					"dataTypeCode": "C",
					"systemVariable": true
				},
				{
					"termId": 100257,
					"name": "PACKET_WEIGHT",
					"alias": "DIego",
					"description": "PACKET_WEIGHT",
					"scale": "Number",
					"scaleId": 6040,
					"method": "Enumerated",
					"property": "Plant Number",
					"dataType": "Numeric",
					"value": null,
					"label": "",
					"dataTypeId": 1110,
					"possibleValues": null,
					"possibleValuesString": null,
					"minRange": null,
					"maxRange": null,
					"scaleMinRange": null,
					"scaleMaxRange": null,
					"variableMinRange": null,
					"variableMaxRange": null,
					"required": false,
					"treatmentLabel": null,
					"operation": null,
					"role": null,
					"variableType": "ENTRY_DETAIL",
					"formula": null,
					"cropOntology": null,
					"factor": true,
					"dataTypeCode": "N",
					"systemVariable": false
				},
			],
			"instances": [
				{
					"instanceId": 11,
					"locationName": "Argentina",
					"locationAbbreviation": "ARG",
					"customLocationAbbreviation": null,
					"locationDescriptorDataId": null,
					"instanceNumber": 1,
					"hasFieldmap": false,
					"hasGeoJSON": false,
					"hasFieldLayout": false,
					"hasInventory": true,
					"hasExperimentalDesign": true,
					"hasMeasurements": false,
					"canBeDeleted": false,
					"experimentId": 0
				}
			],
			"hasPendingData": false,
			"hasOutOfSyncData": false
		}
	}

	function getMockPlantingPreparationData() {
		return {
			"entries": [
				{
					"entryNo": 9,
					"entryType": "Test entry",
					"gid": 8,
					"designation": "CML8",
					"stockByStockId": {
						"SID1-8": {
							"stockId": "SID1-8",
							"lotId": 8,
							"storageLocation": "Default Seed Store",
							"availableBalance": 45,
							"unitId": 8264
						},
						"ICRAF0": {
							"stockId": "ICRAF0",
							"lotId": 407142,
							"storageLocation": "INT CENTER FOR RESEARCH IN AGROFORSTRY",
							"availableBalance": 1,
							"unitId": 8267
						},
						"ICRAF2": {
							"stockId": "ICRAF2",
							"lotId": 407144,
							"storageLocation": "INT CENTER FOR RESEARCH IN AGROFORSTRY",
							"availableBalance": 1,
							"unitId": 8268
						}
					},
					"observationUnits": [
						{
							"ndExperimentId": 123501,
							"observationUnitId": "b3afda17-0106-4a5f-8af7-f3eeb9e4ecea",
							"instanceId": 1
						}
					],
					"entryDetailVariableId": {"100257": 8}
				},
				{
					"entryNo": 1,
					"entryType": "Test entry",
					"gid": 1,
					"designation": "CML1",
					"stockByStockId": {
						"SID1-1": {
							"stockId": "SID1-1",
							"lotId": 1,
							"storageLocation": "INT CENTER FOR RESEARCH IN AGROFORSTRY",
							"availableBalance": 20,
							"unitId": 8264
						}
					},
					"observationUnits": [
						{
							"ndExperimentId": 123505,
							"observationUnitId": "e46a9d13-f380-4035-98d8-1a633dcb92df",
							"instanceId": 1
						}
					],
					"entryDetailVariableId": {"100257": 8}
				}
			]
		}
	}

	function getMockUnits() {
		return [
			{
				"id": "8264",
				"name": "SEED_AMOUNT_g",
			},
			{
				"id": "8267",
				"name": "SEED_AMOUNT_kg",
			},
			{
				"id": "8266",
				"name": "SEED_AMOUNT_No",
			},
			{
				"id": "8268",
				"name": "SEED_AMOUNT_Packets",
			},
			{
				"id": "8710",
				"name": "SEED_AMOUNT_t",
			}
		];
	}
});
