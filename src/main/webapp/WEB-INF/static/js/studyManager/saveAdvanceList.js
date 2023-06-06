var SaveAdvanceList = {};

(function () {
	'use strict';
	SaveAdvanceList.initializeGermplasmListTree = function () {
		displayGermplasmListTree('germplasmFolderTree', true, 1);
		$('#germplasmFolderTree').off('bms.tree.node.activate').on('bms.tree.node.activate', function () {
			var id = $('#germplasmFolderTree').dynatree('getTree').getActiveNode().data.key;
			if (id == 'CROPLISTS') {
				ListTreeOperation.hideFolderDiv('#addGermplasmFolderDiv');
				ListTreeOperation.hideFolderDiv('#renameGermplasmFolderDiv');
			}
		});
		changeBrowseGermplasmButtonBehavior(false);
		$('#saveListTreeModal').off('hide.bs.modal');
		$('#saveListTreeModal').on('hide.bs.modal', function () {
			TreePersist.saveGermplasmTreeState(false, '#germplasmFolderTree');
		});
		$('#saveListTreeModal').on('hidden.bs.modal', function () {
			$('#germplasmFolderTree').dynatree('getTree').reload();
			changeBrowseGermplasmButtonBehavior(false);
		});
	};

})();

