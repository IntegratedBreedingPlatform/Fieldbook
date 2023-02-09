(function () {
    'use strict';

    const observationDetailsModule = angular.module('manageTrialApp');
    observationDetailsModule.factory('observationDetailsModalService', ['$uibModal', '$rootScope', function ($uibModal, $rootScope) {
        const observationDetailsModalService = {};
        observationDetailsModalService.openObservationDetailsModal = function (observationUnitId, callBackFunction) {
            observationDetailsModalService.modal = $uibModal.open({
                windowClass: 'modal-extra-large',
                templateUrl: '/Fieldbook/static/js/trialmanager/observations/observation-details-modal/observation-details-modal.html',
                controller: function ($scope, $uibModalInstance, studyContext) {

                    const observationDetailsURL = '/ibpworkbench/main/app/#/observation-details/' + observationUnitId + '?cropName=' + studyContext.cropName + '&programUUID=' + studyContext.programId
                        + '&modal=true';
                    $scope.url = observationDetailsURL;
                    $scope.observationUnitId = observationUnitId;

                    $scope.close = function () {
                        $uibModalInstance.close(null);
                    };
                },
            }).result.finally(function() {
                setTimeout(function() {
                    if (callBackFunction) {
                        callBackFunction();
                    }
                }, 200);

            });
        };

        return observationDetailsModalService;
    }]);
})();
