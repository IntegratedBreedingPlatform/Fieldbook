/* global Spinner:false */
/* global showErrorMessage */
/* global deleteConfirmation */
/* global deleteNurseryConfirmation */
/* global deleteFolderTitle */
/* global deleteNurseryTitle */

function addFolder(object) {
    'use strict';
    if (!$(object).hasClass('disable-image')) {
        hideRenameFolderDiv();
        $('#addFolderName').val('');
        $('#addFolderDiv').slideDown('fast');

    }
}

function hideAddFolderDiv() {
    'use strict';
    $('#addFolderDiv').slideUp('fast');
}

function hideRenameFolderDiv() {
    'use strict';
    $('#renameFolderDiv').slideUp('fast');
}

function renameFolder(object) {
    'use strict';

    var currentFolderName;

    if (!$(object).hasClass('disable-image')) {
        hideAddFolderDiv();
        $('#renameFolderDiv').slideDown('fast');
        currentFolderName = $('#studyTree').dynatree('getTree').getActiveNode().data.title;
        $('#newFolderName').val(currentFolderName);
    }
}

function submitRenameFolder() {
    'use strict';

    var folderName = $.trim($('#newFolderName').val()),
            parentFolderId;

    var activeStudyNode = $('#studyTree').dynatree('getTree').getActiveNode();
    
    if(activeStudyNode == null || activeStudyNode.data.isFolder === false || activeStudyNode.data.key === 'LOCAL'){
		showErrorMessage('', studyProgramFolderRequired);
		return false;
	}
    
    if ($.trim(folderName) === activeStudyNode.data.title) {
        $('#renameFolderDiv').slideUp('fast');
        return false;
    }
    if (folderName === '') {
        showErrorMessage('page-rename-study-folder-message-modal', folderNameRequiredMessage);
        return false;
    } else if (!isValidInput(folderName)) {
        showErrorMessage('page-rename-study-folder-message-modal', invalidFolderNameCharacterMessage);
        return false;
    } else {
        parentFolderId = activeStudyNode.data.key;
        
        if (parentFolderId === 'LOCAL') {
            parentFolderId = 1;
        }

        $.ajax({
            url: '/Fieldbook/StudyTreeManager/renameStudyFolder',
            type: 'POST',
            data: 'folderId=' + parentFolderId + '&newFolderName=' + folderName,
            cache: false,
            success: function (data) {
                var node;
                if (data.isSuccess === '1') {
                    hideRenameFolderDiv();
                    node = $('#studyTree').dynatree('getTree').getActiveNode();
                    node.data.title = folderName;
                    $(node.span).find('a').html(folderName);
                    node.focus();
                    showSuccessfulMessage('',renameItemSuccessful);
                } else {
                    showErrorMessage('page-rename-study-folder-message-modal', data.message);
                }
            }
        });
    }
}
