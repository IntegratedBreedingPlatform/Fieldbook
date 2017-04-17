
/*global angular, displayStudyGermplasmSection, openListTree, displaySelectedGermplasmDetails*/

(function() {
    'use strict';

    angular.module('manageTrialApp').controller('GermplasmCtrl',
        ['$scope', 'TrialManagerDataService', function($scope, TrialManagerDataService) {

    $scope.settings = TrialManagerDataService.settings.germplasm;

    if (TrialManagerDataService.isOpenTrial()) {
        TrialManagerDataService.updateTrialMeasurementRowCount(TrialManagerDataService.trialMeasurement.count);
        displaySelectedGermplasmDetails();
        
        var startingEntryNo = $('#txtStartingEntryNo').val();
        TrialManagerDataService.updateStartingEntryNoCount((parseInt(startingEntryNo)) ? parseInt(startingEntryNo) : 1);
    }

    $scope.labels = {};
    $scope.labels.germplasmFactors = {
        label: 'Temp label here',
        placeholderLabel: 'Temp placeholder here'
    };

    $scope.trialMeasurement = TrialManagerDataService.trialMeasurement;

    displayStudyGermplasmSection(TrialManagerDataService.trialMeasurement.hasMeasurement,
    TrialManagerDataService.trialMeasurement.count);

    $('#imported-germplasm-list').bind("germplasmListIsUpdated", function() {
        TrialManagerDataService.indicateUnappliedChangesAvailable(true);  
    });

    $scope.updateOccurred = false;

    $scope.$on('deleteOccurred', function() {
        $scope.updateOccurred = true;
    });

    $scope.$on('variableAdded', function() {
        $scope.updateOccurred = true;
    });

    $scope.handleSaveEvent = function() {
        $scope.updateOccurred = false;

        TrialManagerDataService.specialSettings.experimentalDesign.germplasmTotalListCount = $scope.getTotalListNo();
    };

    // function called whenever the user has successfully selected a germplasm list
    $scope.germplasmListSelected = function() {
        // validation requiring user to re-generate experimental design after selecting new germplasm list is removed as per new maintain germplasm list functionality
        $scope.updateOccurred = false;
    };

    $scope.germplasmListCleared = function() {
        TrialManagerDataService.updateTrialMeasurementRowCount(0);
        TrialManagerDataService.applicationData.germplasmListCleared = true;
        TrialManagerDataService.applicationData.germplasmListSelected = false;
    };
            
    $(document).on('germplasmListUpdated', function() {
        TrialManagerDataService.applicationData.germplasmListSelected = true;
    });

    $scope.openGermplasmTree = function() {
        openListTree(1, $scope.germplasmListSelected);
    };

    TrialManagerDataService.registerSaveListener('germplasmUpdate', $scope.handleSaveEvent);

    $scope.displayUpdateButton = function() {
        return $scope.updateOccurred && $scope.listAvailable();
    };

    $scope.listAvailable = function() {
        var entryHtml = $('#numberOfEntries').html();
        return (entryHtml !== '' && parseInt(entryHtml, 10) > 0);
    };

    $scope.getTotalListNo = function() {
        return (parseInt($('#totalGermplasms').val())) ? parseInt($('#totalGermplasms').val()) : 0;
    };

    $scope.updateDataTable = function() {
        $.ajax({
            url: '/Fieldbook/ListManager/GermplasmList/refreshListDetails',
            type: 'GET',
            cache: false,
            data: ''
        }).success(function(html) {
            $('#liExportList').removeClass('fbk-dropdown-select-fade');
            $('#imported-germplasm-list').html(html);
			window.ImportGermplasm.initialize(dataGermplasmList);
            $('#entries-details').css('display', 'block');
            $('#numberOfEntries').html($('#totalGermplasms').val());
            $('#imported-germplasm-list-reset-button').css('opacity', '1');
            $scope.updateOccurred = false;

            TrialManagerDataService.specialSettings.experimentalDesign.germplasmTotalListCount = $scope.getTotalListNo();

            if (!$scope.$$phase) {
                $scope.$apply();
            }

        });

    };
        }]);
})();

// README IMPORTANT: Code unmanaged by angular should go here

/* This will be called when germplasm details page is loaded */
(function() {
    'use strict';

    document.onLoadGermplasmDetails = function() {

        displayGermplasmListTreeTable('germplasmTree');

        changeBrowseGermplasmButtonBehavior(false);

        $('#listTreeModal').off('hide.bs.modal');
        $('#listTreeModal').on('hide.bs.modal', function() {
            TreePersist.saveGermplasmTreeState(true, '#germplasmTree');
            displayGermplasmListTreeTable('germplasmTree');
            changeBrowseGermplasmButtonBehavior(false);
            $(getDisplayedModalSelector() + ' #addGermplasmFolderDiv').hide();
            $(getDisplayedModalSelector() + ' #renameGermplasmFolderDiv').hide();
        });

        $('#manageCheckTypesModal').on('hidden.bs.modal', function() {
            reloadCheckTypeDropDown(false, 'checklist-select');
        });

        initializeCheckTypeSelect2(document.checkTypes, [], false, 0, 'comboCheckCode');
        $('#updateCheckTypes').hide();
        $('#deleteCheckTypes').hide();

        // this is the handler for when user clicks on the Replace button
        $('.show-germplasm-details').on('click', function() {
            showGermplasmDetailsSection();
        });
    };

})();
