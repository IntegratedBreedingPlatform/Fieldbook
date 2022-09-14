(function () {
	'use strict';

	const breedingMethodModule = angular.module('manageTrialApp');
	breedingMethodModule.factory('breedingMethodModalService', ['$uibModal', '$rootScope', function ($uibModal, $rootScope) {
		const breedingMethodModalService = {};
		breedingMethodModalService.openBreedingMethodModal = function (breedingMethodId) {
			breedingMethodModalService.modal = $uibModal.open({
				windowClass: 'modal-large',
				templateUrl: '/Fieldbook/static/js/trialmanager/germplasm/breeding-method-modal/breeding-method-modal.html',
				controller: function ($scope, $uibModalInstance, breedingMethodModalService, studyContext) {

					const url = '/ibpworkbench/main/app/#/breeding-method-page/' + breedingMethodId + '?cropName=' + studyContext.cropName
						+ '&programUUID=' + studyContext.programId
						+ '&modal=true';
					$scope.url = url;
					$scope.breedingMethodId = breedingMethodId;

					window.closeModal = function() {
						$uibModalInstance.close(null);
					};

					$scope.clear = function() {
						$uibModalInstance.close(null);
					};
				},
			}).result;
		};

		return breedingMethodModalService;
	}]);
})();
