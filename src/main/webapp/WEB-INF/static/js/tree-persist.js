/*global germplasmTreetable*/
var GERMPLASM_LIST_TYPE = 'GERMPLASM_LIST';
var STUDY_LIST_TYPE = 'STUDY_LIST';
var SAMPLE_LIST_TYPE = 'SAMPLE_LIST';

var TreePersist = {
	retrieveExpandedNodes : function(isTreeTable, containerSection) {
		'use strict';
		var expandedNodes = [];
		if (isTreeTable) {
			$(containerSection).find('.expanded').each(function() {
				expandedNodes.push($(this).data('tt-id'));
			});
		} else {
			var dynatree = $(containerSection).dynatree('getTree').toDict(true);
			expandedNodes = TreePersist.getAllTreeExpandedNode(
					dynatree.children, expandedNodes);
		}

		return expandedNodes;
	},
	getAllTreeExpandedNode : function(children, expandedNodes) {
		'use strict';
		if (typeof children !== 'undefined') {
			for (var index = 0; index < children.length; index++) {
				if (children[index].expand === true) {
					expandedNodes.push(children[index].key);
					TreePersist.getAllTreeExpandedNode(
							children[index].children, expandedNodes);
				}
			}
		}
		return expandedNodes;
	},
	saveGermplasmTreeState : function(isTreeTable, containerSection) {
		'use strict';
		var programFolders = TreePersist.retrieveExpandedNodes(isTreeTable,
			containerSection);
		var data = {
			programFolders: programFolders,
			cropFolders: null
		}
		var xAuthToken = JSON.parse(localStorage["bms.xAuthToken"]).token;
		$.ajax({
			url : '/bmsapi/crops/' + cropName + '/germplasm-lists/tree-state?programUUID=' + currentProgramId,
			type : 'POST',
			beforeSend: function (xhr) {
				xhr.setRequestHeader('X-Auth-Token', xAuthToken);
			},
			data : JSON.stringify(data),
			cache : false,
			async : false,
			contentType: 'application/json',
			success : function(data) {
			}
		});
	},
	saveSampleTreeState : function(isTreeTable, containerSection) {
		'use strict';
		TreePersist.saveTreeState(isTreeTable, containerSection, SAMPLE_LIST_TYPE);
	},
	saveStudyTreeState : function(isTreeTable, containerSection) {
		'use strict';
		TreePersist.saveTreeState(isTreeTable, containerSection, STUDY_LIST_TYPE);
	},
	saveTreeState : function(isTreeTable, containerSection, listType) {
		'use strict';
		var expandedNodesState = TreePersist.retrieveExpandedNodes(isTreeTable,
				containerSection);
		if (expandedNodesState.length === 0) {
			expandedNodesState = ['None'];
		}
		$.ajax({
			url : '/Fieldbook/StudyTreeManager/save/state/' + listType,
			type : 'POST',
			data : {
				expandedNodes : expandedNodesState
			},
			cache : false,
			async : false,
			success : function(data) {
			}
		});
	},
	preLoadStudyTreeState : function(containerSection) {
		'use strict';
		TreePersist.retrievePreviousStudyTreeState().done(function(expandedNodes) {
			var dynatree = $(containerSection).dynatree('getTree');
			TreePersist.traverseNodes(expandedNodes, STUDY_LIST_TYPE, function(key) {
				var germplasmFocusNode = dynatree.getNodeByKey(key);
				if (germplasmFocusNode !== null) {
					germplasmFocusNode.expand();
				}
			});
			setTimeout(function() {
				$(containerSection).dynatree('getRoot').visit(function(node) {
					node.select(false);
					node.deactivate();
				});
			}, 50);

		});
	},
	preLoadGermplasmTreeState: function(isTreeTable, containerSection, isSaveList) {
		'use strict';
		if (isTreeTable){
			TreePersist.preLoadGermplasmTreeTableState();
		} else {
			TreePersist.preLoadTreeState(containerSection, isSaveList);
		}

	},

	preLoadSampleTreeState: function(isTreeTable, containerSection, isSaveList) {
		'use strict';
		if (isTreeTable){
			TreePersist.preLoadTreeTableSampleListState(SAMPLE_LIST_TYPE, isSaveList);
		} else {
			TreePersist.preLoadTreeSampleListState(containerSection, SAMPLE_LIST_TYPE, isSaveList);
		}

	},

	retrievePreviousStudyTreeState: function() {
		'use strict';
		var deferred = $.Deferred();
		$.ajax({
			url: '/Fieldbook/StudyTreeManager/retrieve/state/' + STUDY_LIST_TYPE,
			type : 'GET',
			data : '',
			cache : false,
			async : false,
			success : function(data) {
				var expandedNodes = $.parseJSON(data);
				if((expandedNodes.length === 1 && expandedNodes[0] === '') || expandedNodes.length === 0){
					deferred.reject(expandedNodes);
				} else {
					deferred.resolve(expandedNodes);
				}
			}
		});

		return deferred.promise();
	},
	retrievePreviousGermplasmTreeState: function() {
		'use strict';
		var deferred = $.Deferred();
		var xAuthToken = JSON.parse(localStorage["bms.xAuthToken"]).token;

		$.ajax({
			url: '/bmsapi/crops/' + cropName + '/germplasm-lists/tree-state?programUUID=' + currentProgramId,
			type : 'GET',
			data : '',
			cache : false,
			beforeSend: function (xhr) {
				xhr.setRequestHeader('X-Auth-Token', xAuthToken);
			},
			async : false,
			success : function(data) {
				//Remove the crop lists nodes
				data.shift();
				var expandedNodes = [];
				TreePersist.getAllTreeExpandedNode(data, expandedNodes);
				if((expandedNodes.length === 1 && expandedNodes[0] === '') || expandedNodes.length === 0){
					deferred.reject(expandedNodes);
				} else {
					deferred.resolve(expandedNodes);
				}
			}
		});

		return deferred.promise();
	},

	retrievePreviousTreeSampleListState: function(listType, isSaveList) {
		'use strict';
		var deferred = $.Deferred();

		if (isSaveList === undefined) {
			isSaveList = false;
		}

		$.ajax({
			url: '/Fieldbook/SampleListTreeManager/retrieve/state/' + listType + '/' + isSaveList,
			type : 'GET',
			data : '',
			cache : false,
			async : false,
			success : function(data) {
				var expandedNodes = $.parseJSON(data);
				if((expandedNodes.length === 1 && expandedNodes[0] === '') || expandedNodes.length === 0){
					deferred.reject(expandedNodes);
				} else {
					deferred.resolve(expandedNodes);
				}
			}
		});

		return deferred.promise();
	},

	expandNode: function (id) {
		$('#treeTable').treetable('expandNode', id);
	},

	preLoadGermplasmTreeTableState: function() {
		'use strict';
		TreePersist.retrievePreviousGermplasmTreeState().done(function(expandedNodes) {
			TreePersist.traverseNodes(expandedNodes, GERMPLASM_LIST_TYPE, TreePersist.expandNode);
		}).fail(function () {
			// If there's no previous tree state, the top level 'Lists' node should be expanded by default.
			TreePersist.expandNode('LISTS');
		});

		// Removed in BMS-4659
		// TreePersist.expandNode('CROPLISTS');
	},

	// TODO call in Sample Manager (Workbench)
	preLoadTreeTableSampleListState: function(listType, isSaveList) {
		'use strict';
		TreePersist.retrievePreviousTreeSampleListState(listType, isSaveList).done(function(expandedNodes) {
			TreePersist.traverseNodes(expandedNodes, listType, TreePersist.expandNode);
		}).fail(function () {
			// If there's no previous tree state, the top level 'Lists' node should be expanded by default.
			TreePersist.expandNode('LISTS');
		});

		TreePersist.expandNode('CROPLISTS');
	},

	preLoadTreeState: function(containerSection, isSaveList) {
		'use strict';

		TreePersist.retrievePreviousGermplasmTreeState().done(function(expandedNodes) {
			var dynatree = $(containerSection).dynatree('getTree');
			var shouldActivateNode = isSaveList;
			TreePersist.traverseNodes(expandedNodes, GERMPLASM_LIST_TYPE, function(key) {
				var germplasmFocusNode = dynatree.getNodeByKey(key);
				if (germplasmFocusNode !== null) {
					germplasmFocusNode.expand();
				}
			});

			setTimeout(function() {
				$(containerSection).dynatree('getRoot').visit(function(node) {
					node.select(false);
					node.deactivate();
				});

				if (shouldActivateNode) {
					dynatree.getNodeByKey(expandedNodes[expandedNodes.length - 1]).activate();
				}
			}, 50);

		});
	},

	preLoadTreeSampleListState: function(containerSection, listType, isSaveList) {
		'use strict';

		TreePersist.retrievePreviousTreeSampleListState(listType, isSaveList).done(function(expandedNodes) {
			var dynatree = $(containerSection).dynatree('getTree');
			var shouldActivateNode = false;

			if (isSaveList) {
				// tree state retrieval used when saving lists provides an additional marker key at the front to indicate status
				shouldActivateNode = expandedNodes[0] === 'SAVED';

				// remove the marker key to continue normal tree state processing
				expandedNodes = expandedNodes.slice(1, expandedNodes.length);
			}

			TreePersist.traverseNodes(expandedNodes, listType, function(key) {
				var germplasmFocusNode = dynatree.getNodeByKey(key);
				if (germplasmFocusNode !== null) {
					germplasmFocusNode.expand();
				}
			});

			setTimeout(function() {
				$(containerSection).dynatree('getRoot').visit(function(node) {
					node.select(false);
					node.deactivate();
				});

				if (shouldActivateNode) {
					dynatree.getNodeByKey(expandedNodes[expandedNodes.length - 1]).activate();
				}
			}, 50);

		});
	},

	traverseNodes : function(expandedNodes, listType, keyProcessor){
		var key, index;
		for (index = 0; index < expandedNodes.length; index++) {
			key = expandedNodes[index];
			if (index === 0) {
				if (listType === GERMPLASM_LIST_TYPE || listType === SAMPLE_LIST_TYPE) {
					key = 'LISTS';
				} else if (listType === STUDY_LIST_TYPE) {
					key = 'LOCAL';
				}
			}
			key = $.trim(key);
			keyProcessor(key);
		}
	}
};
