(function () {
    'use strict';
    const germplasmDetailsModule = angular.module('germplasmDetailsModule', ['ui.bootstrap']);
    germplasmDetailsModule.factory('germplasmDetailsModalService', ['$uibModal', '$rootScope', function ($uibModal, $rootScope) {
        const germplasmDetailsModalService = {};
        germplasmDetailsModalService.openGermplasmDetailsModal = function (gid, callBackFunction) {
            germplasmDetailsModalService.modal = $uibModal.open({
                windowClass: 'modal-extra-large',
                templateUrl: '/Fieldbook/static/js/trialmanager/germplasm/germplasm-details-modal/germplasm-details-modal.html',
                controller: function ($scope, $uibModalInstance, germplasmDetailsService, studyContext) {

                    const germplasmDetailsURL = '/ibpworkbench/main/app/#/germplasm-details/' + gid + '?cropName=' + studyContext.cropName + '&programUUID=' + studyContext.programId
                        + '&modal=true';
                    $scope.url = germplasmDetailsURL;
                    $scope.gid = gid;
                    const MAX_NAME_DISPLAY_SIZE = 30;

                    setPreferredName(gid);

                    $scope.close = function () {
                        $uibModalInstance.close(null);
                    };

                    $rootScope.$on("reloadGermplasmDetailsModal", function (event) {
                        $scope.updateModal();
                    });

                    $scope.updateModal = function () {
                       setPreferredName();
                    }

                    function setPreferredName () {
                        germplasmDetailsService.getGermplasmByGid(gid).then(function (response) {
                            const name = response.data.preferredName;
                            $scope.preferredName = (!!name && name.length > MAX_NAME_DISPLAY_SIZE) ? name.substring(0, MAX_NAME_DISPLAY_SIZE) + "..." : name;
                        });
                    }
                },
            }).result.finally(function() {
                setTimeout(function() {
                    if (callBackFunction) {
                        callBackFunction();
                    }
                }, 200);

            });
        };

        germplasmDetailsModalService.updateGermplasmDetailsModal = function () {
            $rootScope.$broadcast('reloadGermplasmDetailsModal');
        }
        return germplasmDetailsModalService;
    }]);

    germplasmDetailsModule.factory('germplasmDetailsService', ['studyContext', '$http',function (studyContext, $http) {
        const BASE_CROP_URL = '/bmsapi/crops/' + studyContext.cropName + '/germplasm/';
        const germplasmService = {};
        germplasmService.getGermplasmByGid = function (gid) {
            return $http.get(BASE_CROP_URL + gid);
        };
        return germplasmService;
    }]);
})();
