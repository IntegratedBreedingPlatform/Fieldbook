/*global angular*/

(function() {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');
	
	manageTrialApp.controller('selectUserCtrl', ['$scope', '$http', '_', function($scope, $http, _) {
        $scope.currentProgramMembers = [];

        $http.get('/Fieldbook/crosses/getCurrentUser')
            .then(function(userId) {
                $scope.selectedUserId = userId;
            }).catch(function(response) {
                showErrorMessage('', $.fieldbookMessages.errorNoCurrentUser);
            });

        $http.get('/Fieldbook/crosses/getCurrentProgramMembers')
            .then(function(programMembers) {
                $scope.currentProgramMembers = programMembers;
                if (!$scope.selectedUserId) {
                    $scope.selectedUserId = _.keys($scope.currentProgramMembers[0]);
                }
            }).catch(function(response) {
                showErrorMessage('', $.fieldbookMessages.errorNoProgramMembers);
            });

         $http.post('/Fieldbook/crosses/submitListOwner', $scope.selectedUserId)
            .then(function(data) {
                if (data.isSuccess === 0) {
                    showErrorMessage('', data.error);
                }
            }).catch(function(response) {
                showErrorMessage('', $.fieldbookMessages.errorSubmittingListOwner);
            });
    }]);

})();
