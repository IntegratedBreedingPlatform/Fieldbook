(function () {
    'use strict';

    const module = angular.module('manageTrialApp');

    module.controller('SampleGenotypeImportController', ['$scope', '$rootScope', 'studyContext', '$uibModalInstance', 'studyStateService', 'listId',
        function ($scope, $rootScope, studyContext, $uibModalInstance, studyStateService, listId) {

            $scope.url = '/ibpworkbench/controller/jhipster#/genotype-import/' + listId + '?restartApplication' +
                '&cropName=' + studyContext.cropName +
                '&programUUID=' + studyContext.programId +
                '&studyId=' + studyContext.studyId +
                '&selectedProjectId=' + studyContext.selectedProjectId +
                '&loggedInUserId=' + studyContext.loggedInUserId +
                '&hasGeneratedDesign=' + studyStateService.hasGeneratedDesign();

            window.closeModal = function() {
                $uibModalInstance.close(null);
            };

            window.handleImportGenotypesSuccess = function() {
                $uibModalInstance.close();
            };

            $scope.cancel = function() {
                $uibModalInstance.close(null);
            };

        }]);

    module.factory('sampleGenotypeModalService', ['$uibModal',
        function ($uibModal) {

            var genotypeModalService = {};

            genotypeModalService.openImportGenotypesModal = function (listId) {
                $uibModal.open({
                    templateUrl: '/Fieldbook/static/angular-templates/sample-genotype/sample-genotype-modal.html',
                    controller: "SampleGenotypeImportController",
                    size: 'lg',
                    resolve: {
                        listId: function () {
                            return listId;
                        }
                    },
                });
            };
            return genotypeModalService;

        }]);

})();
