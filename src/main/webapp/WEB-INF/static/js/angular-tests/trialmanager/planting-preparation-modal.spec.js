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
		variableServiceMock = jasmine.createSpyObj('variableService', [
			'getVariablesByFilter'
		]),
		HasAnyAuthorityServiceMock = {},
		PERMISSIONSMock	= [],
		VARIABLE_TYPESMock = [];

	beforeEach(function (done) {
		module('manageTrialApp');

		module(function ($provide) {
			$provide.value("variableService", variableServiceMock);
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

			variableServiceMock.getVariablesByFilter.and.returnValue($q.resolve(getMockVariablesByFilter()))
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
				variableService: variableServiceMock,
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
		return [{
			"id": "100257", "name": "PACKET_WEIGHT", "alias": "", "description": "PACKET_WEIGHT", "property": {
				"id": "100050", "name": "Plant Number", "description": "Plant Number", "cropOntologyId": null, "classes": ["Passport"],
				"metadata": {
					"dateCreated": null, "lastModified": null, "editableFields": [], "deletable": false,
					"usage": {"observations": 0, "studies": 0}
				}
			}, "method": {
				"id": "4040", "name": "Enumerated", "description": "Levels enumerated - 1,2,3", "metadata": {
					"dateCreated": "2016-01-28T19:36:53.000Z", "lastModified": null, "editableFields": [], "deletable": false,
					"usage": {"observations": 0, "studies": 0}
				}
			}, "scale": {
				"id": "6040", "name": "Number", "description": "Number",
				"dataType": {"id": "1110", "name": "Numeric", "systemDataType": false}, "validValues": {}, "metadata": {
					"dateCreated": "2016-01-28T19:36:53.000Z", "lastModified": null, "editableFields": [], "deletable": false,
					"usage": {"observations": 0, "studies": 0}
				}
			}, "variableTypes": [{"id": "1815", "name": "Entry Detail", "description": "Variables that describes list entries"}],
			"favourite": false, "metadata": {
				"editableFields": [], "deletable": false, "dateCreated": "2022-08-05T14:10:55.853Z", "lastModified": null, "usage": {
					"observations": 0, "studies": 0, "datasets": 0, "germplasm": 0, "breedingMethods": 0, "lists": 0,
					"isSystemVariable": false
				}
			}, "expectedRange": {}, "formula": null, "allowsFormula": false
		}]
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
					"entryDetailByVariableId": {
						"100257": {
							"variableId": 100257,
							"name": "PACKET_WEIGHT",
							"value": 8
						}
					}
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
					"entryDetailByVariableId": {
						"100257": {
							"variableId": 100257,
							"name": "PACKET_WEIGHT",
							"value": 8
						}
					}
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
