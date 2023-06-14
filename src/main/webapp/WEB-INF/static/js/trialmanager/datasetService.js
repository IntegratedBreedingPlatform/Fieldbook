(function () {
	'use strict';

	var datasetsApiModule = angular.module('datasets-api', ['ui.bootstrap', 'datasets-api', 'datasetOptionModal', 'fieldbook-utils']);

	datasetsApiModule.factory('datasetService', ['$http', '$q', 'studyContext', 'serviceUtilities', 'DATASET_TYPES', 'DATASET_TYPES_OBSERVATION_IDS', 'VARIABLE_TYPES',
		function ($http, $q, studyContext, serviceUtilities, DATASET_TYPES, DATASET_TYPES_OBSERVATION_IDS, VARIABLE_TYPES) {

			var BASE_URL = '/bmsapi/crops/' + studyContext.cropName + '/programs/' + studyContext.programId + '/studies/';

			var datasetTypeMap = {};
			var datasetTypes = [{
				id: DATASET_TYPES.PLANT_SUBOBSERVATIONS,
				tabTitlePrefix: 'Plants: ',
				tabNamePrefix: 'Plants: '
			}, {
				id: DATASET_TYPES.QUADRAT_SUBOBSERVATIONS,
				tabTitlePrefix: 'Quadrats: ',
				tabNamePrefix: 'Quadrats: '
			}, {
				id: DATASET_TYPES.TIME_SERIES_SUBOBSERVATIONS,
				tabTitlePrefix: 'Time Series: ',
				tabNamePrefix: 'Time Series: '
			}, {
				id: DATASET_TYPES.CUSTOM_SUBOBSERVATIONS,
				tabTitlePrefix: 'Sub-Observation Units: ',
				tabNamePrefix: 'SOUs: '
			}, {
				id: DATASET_TYPES.PLOT_OBSERVATIONS,
				tabTitlePrefix: '',
				tabNamePrefix: ''
			}];

			angular.forEach(datasetTypes, function (datasetType) {
				datasetTypeMap[datasetType.id] = datasetType;
			});


			var datasetService = {};
			var successHandler = serviceUtilities.restSuccessHandler,
				failureHandler = serviceUtilities.restFailureHandler;

			datasetService.observationCount = function (datasetId, variableIds) {

				if (datasetId && variableIds) {
					return $http.head(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/variables/observations?variableIds=' + variableIds.join(','));
				}

				return $q.reject('studyId, datasetId and variableIds are not defined.');
			};

			datasetService.observationCountByInstance = function (datasetId, instanceId) {

				if (datasetId && instanceId) {
					return $http.head(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observationUnits/' + instanceId);
				}

				return $q.reject('instanceId and datasetId are not defined.');
			};

			datasetService.addObservation = function (datasetId, observationUnitId, observation) {
				var request = $http.post(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observationUnits/' + observationUnitId + '/observations/'
					, observation);
				return request.then(successHandler, failureHandler);
			};

			datasetService.updateObservation = function (datasetId, observationUnitId, observationId, observationValue) {
				var request = $http.patch(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observationUnits/' +
					observationUnitId + '/observations/' + observationId, observationValue);
				return request.then(successHandler, failureHandler);
			};

			datasetService.deleteObservation = function (datasetId, observationUnitId, observationId) {
				var request = $http.delete(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observationUnits/' +
					observationUnitId + '/observations/' + observationId);
				return request.then(successHandler, failureHandler);
			};

			datasetService.getDatasets = function (datasetTypeIds) {
				if (!studyContext.studyId) {
					return $q.resolve([]);
				}
				var request = $http.get(BASE_URL + studyContext.studyId + '/datasets', angular.merge({
					params: {
						datasetTypeIds: datasetTypeIds || DATASET_TYPES_OBSERVATION_IDS.join(",")
					}
				}));
				return request.then(successHandler, failureHandler);
			};

			datasetService.getColumns = function (datasetId, draftMode) {
				var request = $http.get(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observationUnits/table/columns', {
					params: {
						draftMode: Boolean(draftMode)
					}
				});
				return request.then(successHandler, failureHandler);
			};

			datasetService.getObservationTableUrl = function (datasetId) {
				return BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observationUnits/table';
			};

			datasetService.getObservationForVisualization = function (datasetId, observationUnitsSearch) {
				var request = $http.post(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observationUnits/mapList', observationUnitsSearch);
				return request.then(successHandler, failureHandler);
			};

			datasetService.generation = function (newDataset) {
				var request = $http.post(BASE_URL + studyContext.studyId + '/datasets/' + studyContext.measurementDatasetId + '/generation', newDataset);
				return request.then(successHandler, failureHandler);
			};

			datasetService.getDataset = function (datasetId) {
				var request = $http.get(BASE_URL + studyContext.studyId + '/datasets/' + datasetId);
				return request.then(successHandler, failureHandler);
			};

			datasetService.addVariables = function (datasetId, newVariable) {
				let url = '';
				if (newVariable.variableTypeId === VARIABLE_TYPES.TRAIT) {
					url = BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/traits';
				} else if (newVariable.variableTypeId === VARIABLE_TYPES.SELECTION_METHOD) {
					url = BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/selections';
				} else if (newVariable.variableTypeId === VARIABLE_TYPES.ENVIRONMENT_DETAIL) {
					url = BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/environment-details';
				} else if (newVariable.variableTypeId === VARIABLE_TYPES.STUDY_CONDITION) {
					url = BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/enviromental-conditions';
				}
				var request = $http.put(url, newVariable);
				return request.then(successHandler, failureHandler);
			};

			datasetService.addEntryDetails = function (datasetId, newVariable) {
				var request = $http.put(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/entry-details', newVariable);
				return request.then(successHandler, failureHandler);
			};

			datasetService.getVariables = function (datasetId, variableTypeId) {
				var request = $http.get(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/variables/' + variableTypeId);
				return request.then(successHandler, failureHandler);
			};

			datasetService.getAllProperties = function (datasetId, variableTypeId) {
				var request = $http.get(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/properties/');
				return request.then(successHandler, failureHandler);
			};

			datasetService.removeVariables = function (datasetId, variableType, variableIds) {
				let url = '';
				if (variableType === VARIABLE_TYPES.TRAIT.toString()) {
					url = BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/traits?';
				} else if (variableType === VARIABLE_TYPES.SELECTION_METHOD.toString()) {
					url = BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/selections?';
				} else if (variableType === VARIABLE_TYPES.ENVIRONMENT_DETAIL.toString()) {
					url = BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '//environment-details?';
				} else if (variableType === VARIABLE_TYPES.STUDY_CONDITION.toString()) {
					url = BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/enviromental-conditions?';
				} else if (variableType === VARIABLE_TYPES.ENTRY_DETAIL.toString()) {
					url = BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/entry-details?';
				}
				var request = $http.delete(url, {
					params: {
						variableIds: variableIds.join(",")
					}
				});
				return request.then(successHandler, failureHandler);
			};

			datasetService.getDatasetInstances = function (datasetId) {
				var request = $http.get(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/instances');
				return request.then(successHandler, failureHandler);
			};

			datasetService.getDatasetType = function (datasetTypeId) {
				return datasetTypeMap[datasetTypeId];

			};

			datasetService.exportDataset = function (datasetId, instanceIds, collectionOrderId, singleFile, fileFormat, includeSampleGenotypeValues) {
				var request = $http.get(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/' + fileFormat, {
					params: {
						instanceIds: instanceIds.join(","),
						collectionOrderId: collectionOrderId,
						singleFile: singleFile,
						includeSampleGenotypeValues: includeSampleGenotypeValues
					},
					responseType: 'blob'
				});

				return request.then(function (response) {
					return response;
				}, failureHandler);
			};

			datasetService.importObservations = function (datasetId, observationList, processWarnings) {
				if (!studyContext.studyId) {
					return $q.resolve([]);
				}
				var request = $http.put(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observationUnits/observations',
					{
						processWarnings: processWarnings,
						data: observationList,
						draftMode: true
					});
				return request.then(successHandler, failureHandler);
			};
			
			datasetService.importEnvironmentVariableValues = function (datasetId, observationList) {
				if (!studyContext.studyId) {
					return $q.resolve([]);
				}
				var request = $http.put(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/environment-variables/values',
					{
						data: observationList
					});
				return request.then(successHandler, failureHandler);
			};

			datasetService.acceptDraftData = function (datasetId, instanceIds) {
				var request = $http.post(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observation-units/drafts/acceptance' + (instanceIds ? '?instanceIds=' + instanceIds.join(",") : ''));
				return request.then(successHandler, failureHandler);
			};

			datasetService.checkOutOfBoundDraftData = function (datasetId) {
				var request = $http.get(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observation-units/drafts/out-of-bounds');
				return request.then(successHandler, failureHandler);

			};

			datasetService.rejectDraftData = function (datasetId, instanceIds) {
				var request = $http.post(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observation-units/drafts/rejection' + (instanceIds ? '?instanceIds=' + instanceIds.join(",") : ''));
				return request.then(successHandler, failureHandler);

			};

			datasetService.setAsMissingDraftData = function (datasetId, instanceIds) {
				var request = $http.post(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observation-units/drafts/set-as-missing' + (instanceIds ? '?instanceIds=' + instanceIds.join(",") : ''));
				return request.then(successHandler, failureHandler);

			};

			datasetService.countFilteredPhenotypesAndInstances = function (datasetId, observationUnitsSearch) {
				var request = $http.post(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observation-units/observations/filter/count', observationUnitsSearch);
				return request.then(successHandler, failureHandler);

			};

			datasetService.acceptDraftDataByVariable = function (datasetId, observationUnitsSearch) {
				var request = $http.post(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observation-units/drafts/filter/acceptance', observationUnitsSearch);
				return request.then(successHandler, failureHandler);

			};

			datasetService.setValueToVariable = function (datasetId, observationUnitsSearch) {
				var request = $http.post(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observation-units/filter/set-value', observationUnitsSearch);
				return request.then(successHandler, failureHandler);
			};

			datasetService.deleteVariableValues = function (datasetId, observationUnitsSearch) {
				var request = $http.post(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observation-units/filter/delete-value', observationUnitsSearch);
				return request.then(successHandler, failureHandler);

			};

			datasetService.getAllDatasetProperties = function (datasetId) {
				var request = $http.get(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/properties');
				return request.then(successHandler, failureHandler);
			};

			datasetService.getObservationUnitsMetadata = function (searchCompositeRequest, datasetId) {
				const url = BASE_URL + studyContext.studyId + `/datasets/${datasetId}/observation-units/metadata`;
				return $http.post(url, searchCompositeRequest)
					.then(successHandler, failureHandler);
			};

			datasetService.countObservationUnits = function (datasetId) {

				if (datasetId) {
					return $http.head(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observation-units/');
				}

				return $q.reject('datasetId not defined.');
			};

			datasetService.saveSearchRequest = function (germplasmStudySourceRequest, datasetId) {
				germplasmStudySourceRequest.studyId = studyContext.studyId;
				return $http.post(BASE_URL + studyContext.studyId + '/datasets/' + datasetId + '/observationUnits/search', germplasmStudySourceRequest)
					.then(successHandler, failureHandler);
			};

			datasetService.updateDatasetProperties = function (plotDatasetProperties) {
				var request = $http.put(BASE_URL + studyContext.studyId + '/plot-datasets/properties', plotDatasetProperties);
				return request.then(successHandler, failureHandler);
			};

			return datasetService;

		}]);


	datasetsApiModule.factory('experimentDesignService', ['$http', '$q', 'studyContext', 'serviceUtilities',
		function ($http, $q, studyContext, serviceUtilities) {

			var BASE_CROP_URL = '/bmsapi/crops/' + studyContext.cropName;
			var BASE_STUDY_URL = BASE_CROP_URL + '/programs/' + studyContext.programId + '/studies/';

			var experimentDesignService = {};
			var successHandler = serviceUtilities.restSuccessHandler,
				failureHandler = serviceUtilities.restFailureHandler;

			experimentDesignService.generateDesign = function (experimentDesignInput) {
				var request = $http.post(BASE_STUDY_URL + studyContext.studyId + '/experimental-designs/generation', experimentDesignInput);
				return request.then(successHandler, failureHandler);
			}

			experimentDesignService.deleteDesign = function () {
				var request = $http.delete(BASE_STUDY_URL + studyContext.studyId + '/experimental-designs');
				return request.then(successHandler, failureHandler);
			}

			experimentDesignService.getBVDesignLicense = function () {
				return $http.get('/bmsapi/breeding-view-licenses');
			}

			experimentDesignService.getDesignTypes = function () {
				var request = $http.get(BASE_CROP_URL + '/experimental-design-types?programUUID=' + studyContext.programId);
				return request.then(successHandler, failureHandler);
			}

			experimentDesignService.getInsertionManners = function () {
				var request = $http.get(BASE_CROP_URL + '/check-insertion-manners?programUUID=' + studyContext.programId);
				return request.then(successHandler, failureHandler);
			}

			return experimentDesignService;

		}]);

})();
