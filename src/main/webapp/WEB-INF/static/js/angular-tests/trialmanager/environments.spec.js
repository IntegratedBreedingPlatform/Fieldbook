'use strict';
describe('Measurement Controller', function () {

	var controller, q, httpBackend, $rootScope;
	var scope = jasmine.createSpyObj('scope', ['deleteInstance', 'openConfirmModal', '$watch'])

	var studyContext = {
		studyId: 1,
		measurementDatasetId: 2009,
		cropName: 'maize',
		programId: 1
	};
	var derivedVariableService = {};
	var HasAnyAuthorityServiceMock = {};
	var PERMISSIONSMock = {};
	var rootScopeMock = {
		openConfirmModal: jasmine.createSpyObj('$scope', ['openConfirmModal', 'watch']),
	};
	var datasetServiceMock = {observationCountByInstance: jasmine.createSpy('observationCountByInstance')};
	var responseMock = {headers: jasmine.createSpy('headers')};
	var trialManagerServiceMock = {
		settings: {
			environments: [],
			managementDetails: []
		},
		currentData: {
			environments: []
		},
		trialMeasurement: {},
		onUpdateData: function () {
		},
	};

	var studyInstanceServiceMock = jasmine.createSpyObj('studyInstanceService', ['getStudyInstance', 'instanceInfo']);

	var studyInstanceMockWithMeasurement = {
		instanceId: 1,
		locationName: 'Sample',
		locationAbbreviation: '',
		customLocationAbbreviation: '',
		locationObservationId: '',
		instanceNumber: '1',
		hasFieldmap: 'true',
		hasGeoJSON: 'true',
		hasFieldLayout: 'true',
		hasInventory: 'true',
		hasExperimentalDesign: 'true',
		hasMeasurements: 'true',
		canBeDeleted: 'true'
	}


	var studyInstanceMockWithoutMeasurement = {
		instanceId: 1,
		locationName: 'Sample',
		locationAbbreviation: '',
		customLocationAbbreviation: '',
		locationObservationId: '',
		instanceNumber: '1',
		hasFieldmap: 'false',
		hasGeoJSON: 'true',
		hasFieldLayout: 'true',
		hasInventory: 'true',
		hasExperimentalDesign: 'true',
		hasMeasurements: 'false',
		canBeDeleted: 'true'
	}

	var fileServiceMock = jasmine.createSpyObj('fileService', ['getFileStorageStatus', 'getFileCount']);

	var fileStorageMap = {
		status: false
	}

	var uibModalInstance = {
		close: jasmine.createSpy('close'),
		dismiss: jasmine.createSpy('dismiss'),
		result: {
			then: jasmine.createSpy('then')
		}
	};

	beforeEach(function () {

		module('datatables');
		module('datatables.buttons');
		module('ngResource');
		module('ngStorage');
		module('leafnode-utils');
		module('fieldbook-utils');
		module('ui.bootstrap');
		module('ui.select');
		module('ui.select2');
		module('ngSanitize');
		module('ui.router');
		module('designImportApp');
		module('ngLodash');
		module('manageTrialApp');

		module(function ($provide) {
			$provide.value("TrialManagerDataService", trialManagerServiceMock);
			$provide.value("datasetService", datasetServiceMock);
			$provide.value("studyContext", studyContext);
			$provide.value("studyInstanceService", studyInstanceServiceMock);
			$provide.value("fileService", fileServiceMock);
			$provide.value("HasAnyAuthorityService", HasAnyAuthorityServiceMock);
			$provide.value("PERMISSIONS", PERMISSIONSMock);
		});

	});

	beforeEach(function () {
		inject(function (_$rootScope_, $controller, $q, $httpBackend, $injector) {

			$rootScope = _$rootScope_;
			scope = _$rootScope_.$new();
			q = $q;
			httpBackend = $httpBackend;
			trialManagerServiceMock.trialMeasurement['hasAdvancedOrCrossesList'] = false;
			studyInstanceServiceMock = $injector.get('studyInstanceService');
			fileServiceMock = $injector.get('fileService');
			fileServiceMock.getFileStorageStatus.and.returnValue(q.resolve(fileStorageMap));

			var fileCountResponse = {headers: jasmine.createSpy('headers')};
			fileCountResponse.headers.withArgs('X-Total-Count').and.returnValue(1);
			fileServiceMock.getFileCount.and.returnValue(Promise.resolve(fileCountResponse));

			uibModalInstance.result.then.and.returnValue(q.resolve(false));
			scope.openConfirmModal = jasmine.createSpy('openConfirmModal');
			scope.openConfirmModal.and.returnValue(uibModalInstance)
			scope.$watch = jasmine.createSpy('watch');

			controller = $controller('EnvironmentCtrl', {
				$rootScope: rootScopeMock,
				$scope: scope,
				studyContext: studyContext,
				datasetService: datasetServiceMock,
				TrialManagerDataService: trialManagerServiceMock,
				studyInstanceService: studyInstanceServiceMock,
				LOCATION_ID: 1,
				PROGRAM_DEFAULT_LOCATION_ID: 0,
				derivedVariableService: derivedVariableService,
				fileService: fileServiceMock
			});
		});
	});

	beforeEach(function () {
		httpBackend.whenGET('/Fieldbook/TrialManager/createTrial/trialSettings').respond(200, {data: "ok"});
	})

	describe('deleteInstance', function () {

		it('should show confirmation window for study with measurements', function () {
			studyInstanceServiceMock.getStudyInstance.and.returnValue(q.resolve(studyInstanceMockWithMeasurement));
			scope.deleteInstance(1, 1).then(() => {
				expect(scope.openConfirmModal).toHaveBeenCalled();
			});
			scope.$apply();
		});

		it('should show confirmation window for study without measurements / fieldmap', function () {
			studyInstanceServiceMock.getStudyInstance.and.returnValue(q.resolve(studyInstanceMockWithoutMeasurement));
			scope.deleteInstance(1, 1).then(() => {
				expect(scope.openConfirmModal).toHaveBeenCalled();
			});
			scope.$apply();
		});

	})
});
