function checkMethod(){
	if($('input[type=radio][name=methodChoice]:checked').val() == 1){
			//$('#methodSelected').prop('disabled', false);
			$('#methodIdFavorite').select2('enable', true);
			$('#methodIdAll').select2('enable', true);
			$('#showFavoriteMethod').prop('disabled', false);
			
			//$('#methodSelected').val(oldMethodSelected);
			setCorrectMethodValues(true);
		}else{
			//$('#methodSelected').prop('disabled', 'disabled');
			$('#showFavoriteMethod').prop('disabled', 'disabled');
			$('#methodIdFavorite').select2('enable', false);
			$('#methodIdAll').select2('enable', false);
			
			oldMethodSelected = $('#'+getJquerySafeId("breedingMethodId")).val();
			 $('#methodSelected').val($('#defaultMethodId').val());
			 setCorrectMethodValues(false);
		} 	
}
function setCorrectMethodValues(isCheckMethod){
	 
	 if($('#showFavoriteMethod').is(':checked')){
		 //we check if the default is in the favorite method list or not
		 var isFound = false;
		 var dataVal = null;
		 var findId = $('#defaultMethodId').val();
		 if(isCheckMethod)
		 	findId = oldMethodSelected;
		 for(key in methodSuggestionsFav_obj){
			 if(methodSuggestionsFav_obj[key].id == findId){
				 isFound = true;
				 dataVal = methodSuggestionsFav_obj[key];
				 break;
			 }
		 }
		 if(isFound){
			 $("#methodIdFavorite").select2('data', dataVal).trigger('change');					  	
		 }else if(methodSuggestionsFav_obj.length > 0){
			 //we set the first
			 $("#methodIdFavorite").select2('data', methodSuggestionsFav_obj[0]).trigger('change');
		 }else{
			 $('#'+getJquerySafeId("breedingMethodId")).val(0);
		 }
		 
		
	 }else{
		 var isFound = false;
		 var dataVal = null;
		 var findId = $('#defaultMethodId').val();
		 if(isCheckMethod)
		 	findId = oldMethodSelected;
		 for(key in methodSuggestions_obj){
			 if(methodSuggestions_obj[key].id == findId){
				 isFound = true;
				 dataVal = methodSuggestions_obj[key];
				 break;
			 }
		 }
		 if(isFound){
			 $("#methodIdAll").select2('data', dataVal).trigger('change');					  	
		 }
	 }
}
function lineMethod(){
	if($('input[type=radio][name=lineChoice]:checked').val() == 1){
			$('#lineSelected').prop('disabled', false);
			$('#lineSelected').val(oldLineSelected);
		}else{
			$('#lineSelected').prop('disabled', 'disabled');
			oldLineSelected = $('#lineSelected').val();
			$('#lineSelected').val(1);
		} 
}


function showCorrectLocationCombo() {
	var isChecked = $('#showFavoriteLocation').is(':checked');
	//if show favorite location is checked, hide all field locations, else, show only favorite locations
	if(isChecked){
		$('#s2id_harvestLocationIdFavorite').show();
		$('#s2id_harvestLocationIdAll').hide();
		if($('#'+getJquerySafeId("harvestLocationIdFavorite")).select2("data") != null){
			$('#'+getJquerySafeId("harvestLocationId")).val($('#'+getJquerySafeId("harvestLocationIdFavorite")).select2("data").id);
			$('#'+getJquerySafeId("harvestLocationName")).val($('#'+getJquerySafeId("harvestLocationIdFavorite")).select2("data").text);
			$('#'+getJquerySafeId("harvestLocationAbbreviation")).val($('#'+getJquerySafeId("harvestLocationIdFavorite")).select2("data").abbr);
			
		}else{
			$('#'+getJquerySafeId("harvestLocationId")).val(0);
			$('#'+getJquerySafeId("harvestLocationName")).val("");
			$('#'+getJquerySafeId("harvestLocationAbbreviation")).val("");
		}
	}else{
		$('#s2id_harvestLocationIdFavorite').hide();
		$('#s2id_harvestLocationIdAll').show();
		if($('#'+getJquerySafeId("harvestLocationIdAll")).select2("data") != null){
			$('#'+getJquerySafeId("harvestLocationId")).val($('#'+getJquerySafeId("harvestLocationIdAll")).select2("data").id);
			$('#'+getJquerySafeId("harvestLocationName")).val($('#'+getJquerySafeId("harvestLocationIdAll")).select2("data").text);
			$('#'+getJquerySafeId("harvestLocationAbbreviation")).val($('#'+getJquerySafeId("harvestLocationIdAll")).select2("data").abbr);
		}else{
			$('#'+getJquerySafeId("harvestLocationId")).val(0);
			$('#'+getJquerySafeId("harvestLocationName")).val("");
			$('#'+getJquerySafeId("harvestLocationAbbreviation")).val("");
		}
		
	}
}

function showCorrectMethodCombo() {
	var isChecked = $('#showFavoriteMethod').is(':checked');
	//if show favorite Method is checked, hide all field locations, else, show only favorite methods
	var methodSelect = false;
	if($('input[type=radio][name=methodChoice]:checked').val() == 1)
		methodSelect = true;
	
	if(isChecked){
		$('#s2id_methodIdFavorite').show();
		$('#s2id_methodIdAll').hide();
		setCorrectMethodValues(methodSelect);
		if($('#'+getJquerySafeId("methodIdFavorite")).select2("data") != null){
			$('#'+getJquerySafeId("breedingMethodId")).val($('#'+getJquerySafeId("methodIdFavorite")).select2("data").id); 						 						
		}else{
			$('#'+getJquerySafeId("breedingMethodId")).val(0); 						
		}
	}else{
		$('#s2id_methodIdFavorite').hide();
		$('#s2id_methodIdAll').show();
		setCorrectMethodValues(methodSelect);
		if($('#'+getJquerySafeId("methodIdAll")).select2("data") != null){
			$('#'+getJquerySafeId("breedingMethodId")).val($('#'+getJquerySafeId("methodIdAll")).select2("data").id);
		}else{
			$('#'+getJquerySafeId("breedingMethodId")).val(0);
		} 					
	}
}

function openManageLocations() {
	$('#manageLocationModal').modal({ backdrop: 'static', keyboard: true });
	$("#manageLocationModal").modal("show");
	if(locationIframeOpened == false){
		locationIframeOpened = true;
		$('#locationFrame').attr('src', programLocationUrl + $('#projectId').val());
	}
	
}

function openManageMethods() {
	$('#manageMethodModal').modal({ backdrop: 'static', keyboard: true });
	$("#manageMethodModal").modal("show");
	if(methodIframeOpened == false){
		methodIframeOpened = true;
		$('#methodFrame').attr('src', programMethodUrl + $('#projectId').val());
	}
}

function recreateMethodCombo() {
var selectedMethodAll = $("#methodIdAll").val();
var selectedMethodFavorite = $("#methodIdFavorite").val();

Spinner.toggle();
$.ajax(
     { url: "/Fieldbook/NurseryManager/advance/nursery/getBreedingMethods",
       type: "GET",
       cache: false,
       data: "",
       async: false,
       success: function(data) {
    	   if (data.success == "1") {
    		   if (selectedMethodAll != null) {
	    		   //recreate the select2 combos to get updated list of methods    			   
	    		   recreateMethodComboAfterClose("methodIdAll", $.parseJSON(data.allMethods));
	    		   recreateMethodComboAfterClose("methodIdFavorite", $.parseJSON(data.favoriteMethods));
	    		   showCorrectMethodCombo();
	    		   //set previously selected value of method
	    		   if ($("#showFavoriteMethod").prop("checked")) {
	    			   setComboValues(methodSuggestionsFav_obj, selectedMethodFavorite, "methodIdFavorite");
	    		   } else {
	    			   setComboValues(methodSuggestions_obj, selectedMethodAll, "methodIdAll");
	    		   }
    		   } else {
    			   var selectedVal = null;
    			   //get index of breeding method row
    			   var index = getBreedingMethodRowIndex();
    			   
    			   if ($("#" + getJquerySafeId("studyLevelVariables" + index + ".value")).select2("data")) {
    				   selectedVal = $("#" + getJquerySafeId("studyLevelVariables" + index + ".value")).select2("data").id;
    			   }
    			   //recreate select2 of breeding method
    			   initializePossibleValuesCombo([], 
	 			 			"#" + getJquerySafeId("studyLevelVariables" + index + ".value"), false, selectedVal);
    			   
    			   //update values of combo
    			   if ($("#" + getJquerySafeId("studyLevelVariables" + index + ".favorite1")).is(":checked")) {
					   initializePossibleValuesCombo($.parseJSON(data.favoriteMethods), 
		 			 			"#" + getJquerySafeId("studyLevelVariables" + index + ".value"), false, selectedVal);
    			   } else {
    				   initializePossibleValuesCombo($.parseJSON(data.allMethods), 
		 			 			"#" + getJquerySafeId("studyLevelVariables" + index + ".value"), false, selectedVal);
    			   }
    			   
    			   replacePossibleJsonValues(data.favoriteMethods, data.allMethods, index, "");
    		   }
    	   } else {
    		   showErrorMessage("page-message", data.errorMessage);
    	   }
       },
       error: function(jqXHR, textStatus, errorThrown){
			console.log("The following error occured: " + textStatus, errorThrown); 
	   }, 
	   complete: function(){
		   Spinner.toggle();
	   } 
     }
 );
}

function getBreedingMethodRowIndex() {
	var rowIndex = 0;
	$.each($(".nurseryLevelSettings"), function (index, row) {
		//daniel
		var cvTermId = $($(row).find('.1st')
				.find("#" + getJquerySafeId("studyLevelVariables" + index + ".variable.cvTermId"))).val();
		if (parseInt(cvTermId) == parseInt(breedingMethodId)) {
			rowIndex = index;
		}
	});
	return rowIndex;
}

function getLocationRowIndex() {
	var rowIndex = -1;
	$.each($(".nurseryLevelSettings"), function (index, row) {
		var cvTermId = $($(row).find('.1st')
				.find("#" + getJquerySafeId("studyLevelVariables" + index + ".variable.cvTermId"))).val();
		if (parseInt(cvTermId) == parseInt(locationId)) {
			rowIndex = index;
		}
	});
	return rowIndex;
}

function recreateLocationCombo() {
	var selectedLocationAll = $("#harvestLocationIdAll").val();
	var selectedLocationFavorite = $("#harvestLocationIdFavorite").val();
	var index = getLocationRowIndex();
	var trialInstances = $("#trialInstancesTable tbody tr").length;
	
	if (selectedLocationAll == undefined && (trialInstances == undefined || trialInstances == 0 )) {
		 if(index == -1)
			 return;
	}
	
	Spinner.toggle();
	$.ajax(
	{ url: "/Fieldbook/NurseryManager/advance/nursery/getLocations",
       type: "GET",
       cache: false,
       data: "",
       async: false,
       success: function(data) {
    	   if (data.success == "1") {
    		   if (selectedLocationAll != null) {
	    		   //recreate the select2 combos to get updated list of locations
	    		   recreateLocationComboAfterClose("harvestLocationIdAll", $.parseJSON(data.allLocations));
	    		   recreateLocationComboAfterClose("harvestLocationIdFavorite", $.parseJSON(data.favoriteLocations));
	    		   showCorrectLocationCombo();
	    		   //set previously selected value of location
	    		   if ($("#showFavoriteLocation").prop("checked")) {
	    			   setComboValues(locationSuggestionsFav_obj, selectedLocationFavorite, "harvestLocationIdFavorite");
	    		   } else {
	    			   setComboValues(locationSuggestions_obj, selectedLocationAll, "harvestLocationIdAll");
	    		   }
    		   } else if (showFavoriteLocationForAll) {
    			   recreateLocationsAfterClose(data);
    		   } else {
    			   var selectedVal = null;
    			  
    			   
    			   if ($("#" + getJquerySafeId("studyLevelVariables"+index+".value")).select2("data")) {
    				   selectedVal = $("#" + getJquerySafeId("studyLevelVariables"+index+".value")).select2("data").id;
    			   } 
    			   initializePossibleValuesCombo([], 
	 			 			"#" + getJquerySafeId("studyLevelVariables"+index+".value"), true, selectedVal);
    			   
    			   //update values in combo
    			   if ($("#" + getJquerySafeId("studyLevelVariables"+index+".favorite1")).is(":checked")) {
	    			   initializePossibleValuesCombo($.parseJSON(data.favoriteLocations), 
		 			 			"#" + getJquerySafeId("studyLevelVariables"+index+".value"), false, selectedVal);
    			   } else {
    				   initializePossibleValuesCombo($.parseJSON(data.allLocations), 
		 			 			"#" + getJquerySafeId("studyLevelVariables"+index+".value"), true, selectedVal);
    			   }
    			   
    			   replacePossibleJsonValues(data.favoriteLocations, data.allLocations, index, "");
    		   }
    	   } else {
    		   showErrorMessage("page-message", data.errorMessage);
    	   }
       },
       error: function(jqXHR, textStatus, errorThrown){
			console.log("The following error occured: " + textStatus, errorThrown); 
	   }, 
	   complete: function(){  
		   Spinner.toggle();
	   } 
     }
 );
}

function recreateLocationsAfterClose(data) {
	$.each($("#trialInstancesTable tbody tr"), function (index, row){
		$.each($(row).children("td"), function (cellIndex, cell) {
			if ($($(cell).children(".cvTermIds")).val() == locationId) {
				var selectedVal = null;
  			  
 			   if ($("#" + getJquerySafeId("trialEnvironmentValues"+index+cellIndex+".name")).select2("data")) {
 				   selectedVal = $("#" + getJquerySafeId("trialEnvironmentValues"+index+cellIndex+".name")).select2("data").id;
 			   } 
 			   initializePossibleValuesCombo([], 
	 			 			"#" + getJquerySafeId("trialEnvironmentValues"+index+cellIndex+".name"), true, selectedVal);
 			   
 			   //update values in combo
 			   if ($("#showFavoriteLocationForAll").is(":checked")) {
	    			   initializePossibleValuesCombo($.parseJSON(data.favoriteLocations), 
		 			 			"#" + getJquerySafeId("trialEnvironmentValues"+index+cellIndex+".name"), false, selectedVal);
 			   } else {
 				   initializePossibleValuesCombo($.parseJSON(data.allLocations), 
		 			 			"#" + getJquerySafeId("trialEnvironmentValues"+index+cellIndex+".name"), true, selectedVal);
 			   }
 			   
 			   replacePossibleJsonValues(data.favoriteLocations, data.allLocations, index+"a"+cellIndex, "Trial");
			}
		});
	});
}

function replacePossibleJsonValues(favoriteJson, allJson, index, trialSuffix) {
	$("#possibleValuesJson"+trialSuffix+index).text(allJson);
	$("#possibleValuesFavoriteJson"+trialSuffix+index).text(favoriteJson);
}

function setComboValues(suggestions_obj, id, name) {
	var dataVal = {id:'',text:'',description:''}; //default value
	if(id != ''){
		var count = 0;
		//find the matching value in the array given
    	for(count = 0 ; count < suggestions_obj.length ; count++){
    		if(suggestions_obj[count].id == id){
    			dataVal = suggestions_obj[count];			    			
    			break;
    		}			    			
    	}
	}
	//set the selected value of the combo
	$("#" + name).select2('data', dataVal);
}

function recreateLocationComboAfterClose(comboName, data) {	
	if (comboName == "harvestLocationIdAll") {
		//clear all locations dropdown
		locationSuggestions = [];
		locationSuggestions_obj = [];
		initializeHarvestLocationSelect2(locationSuggestions, locationSuggestions_obj);
		//reload the data retrieved
		locationSuggestions = data;
		initializeHarvestLocationSelect2(locationSuggestions, locationSuggestions_obj);
	} else {
		//clear the favorite locations dropdown
		locationSuggestionsFav = [];
		locationSuggestionsFav_obj = [];
		initializeHarvestLocationFavSelect2(locationSuggestionsFav, locationSuggestionsFav_obj);
		//reload the data
		locationSuggestionsFav = data;
		initializeHarvestLocationFavSelect2(locationSuggestionsFav, locationSuggestionsFav_obj);
	}

}

function recreateMethodComboAfterClose(comboName, data) {
	if (comboName == "methodIdAll") {
		//clear the all methods dropdown
		methodSuggestions = [];
		methodSuggestions_obj = [];
		initializeMethodSelect2(methodSuggestions, methodSuggestions_obj);
		//reload the data
		methodSuggestions = data;
		initializeMethodSelect2(methodSuggestions, methodSuggestions_obj);
	} else {
		//clear the favorite methods dropdown
		methodSuggestionsFav = [];
		methodSuggestionsFav_obj = [];
		initializeMethodFavSelect2(methodSuggestionsFav, methodSuggestionsFav_obj);
		//reload the data
		methodSuggestionsFav = data;
		initializeMethodFavSelect2(methodSuggestionsFav, methodSuggestionsFav_obj);
	}
	//console.log(methodSuggestions);
}

function openAddVariablesSetting(variableType) {
	//change heading of popup based on clicked link
	//$('#var-info').slideUp('fast');
	$('#ontology-detail-tabs').empty().html($('.variable-detail-info').html());		
	//this would reset the tree view	
	
    
	$("#variable-details").html('');
	switch (parseInt(variableType)) {
		case 1:
			$("#heading-modal").text(addNurseryLevelSettings);
			$('#reminder-placeholder').html(reminderNursery);
			
			break;
		case 2:
			$("#heading-modal").text(addPlotLevelSettings);
			$('#reminder-placeholder').html(reminderPlot);
			break;
		case 3:
			$("#heading-modal").text(addBaselineTraits);
			$('#reminder-placeholder').html(reminderTraits);
			break;
		case 4:
			$("#heading-modal").text(addTrialEnvironmentTraits);
			$('#reminder-placeholder').html(reminderTrialEnvironment);
			break;
	default: 
		$("#heading-modal").text(addNurseryLevelSettings);
		$('#reminder-placeholder').html(reminderNursery);
	}
	getStandardVariables(variableType);
	
}

function getStandardVariables(variableType) {
	Spinner.toggle();
	$.ajax({
		url: "/Fieldbook/TrialManager/manageTrialSettings/displayAddSetting/" + variableType,
		type: "GET",
		cache: false,
		success: function (data) {
			//clear and initialize standard variable combo
			//initializeStandardVariableSearch([]);
			//initializeStandardVariableSearch($.parseJSON(data));
			//console.log(data.treeData);
			//console.log(data.searchtreeData);
			
			if(treeData != null){
				$("#"+treeDivId).dynatree("destroy");				
			}
			treeData = data.treeData;
			searchTreeData = data.searchTreeData;
			displayOntologyTree(treeDivId, treeData, searchTreeData, 'srch-term');
			$('#'+'srch-term').val('');
			$(".tt-hint").css('top','3px');
	  	     $(".tt-hint").css('left','0px');
			
			
			//clear selected variables table and attribute fields
			$("#newVariablesList > tbody").empty();
			$("#page-message-modal").html("");
			clearAttributeFields();
			$("#addVariables").attr("onclick", "javascript: submitSelectedVariables(" + variableType + ");");
			$("#addVariablesSettingModal").modal("show");
		},
		error: function(jqXHR, textStatus, errorThrown){
			console.log("The following error occured: " + textStatus, errorThrown); 
		},
		complete: function() {
			Spinner.toggle();
		}
	});
}

function initializeStandardVariableSearch(variables) {
	//set values
	var stdVariableSuggestions_obj = [];
	$.each(variables, function(index, value) {
		stdVariableSuggestions_obj.push({ 'id' : value.id,
			  'text' : value.name
		});  
	});
	
	stdVariableSuggestions_obj = sortByKey(stdVariableSuggestions_obj, "text"); 

	$("#stdVarSearch").select2({
		query: function (query) {	
      var data = {results: stdVariableSuggestions_obj}, i, j, s;
      // return the array that matches
      data.results = $.grep(data.results,function(item,index) {
        return ($.fn.select2.defaults.matcher(query.term,item.text));
      
      });
      if (data.results.length === 0){
    	  data.results.unshift({id:query.term,text:query.term});	        	 
      }
      
        query.callback(data);
    }
    }).unbind("change").on("change", function (){
    	//set attribute values
    	getStandardVariableDetailsModal($("#stdVarSearch").select2("data").id);
    });
	var dataVal = {'id': '', 'text': ''};
	$("#stdVarSearch").select2('data', dataVal).trigger('change');
}

function getStandardVariableDetailsModal(id) {
	if(id != ''){
		Spinner.toggle();
		$.ajax({
			url: "/Fieldbook/TrialManager/manageTrialSettings/showVariableDetails/" + id,
			type: "GET",
			cache: false,
			success: function (data) {
				$('#var-info').slideDown("slow");
				populateAttributeFields($.parseJSON(data));
			},
			error: function(jqXHR, textStatus, errorThrown){
				console.log("The following error occured: " + textStatus, errorThrown); 
			},
			complete: function() {
				Spinner.toggle();
			}
		});
	}
	
}
		
function populateAttributeFields(data) {
	$("#selectedTraitClass").html(checkIfEmpty(data.traitClass));
	$("#selectedProperty").html(checkIfEmpty(data.property));
	$("#selectedMethod").html(checkIfEmpty(data.method));
	$("#selectedScale").html(checkIfEmpty(data.scale));
	$("#selectedDataType").html(checkIfEmpty(data.dataType));
	$("#selectedRole").html(checkIfEmpty(data.role));
	$("#selectedCropOntologyId").html(checkIfEmpty(data.cropOntologyId));
	$("#selectedStdVarId").val(data.cvTermId);
	$("#selectedName").val(data.name);
}

function checkIfEmpty(value) {
	if (value == "") {
		return "&nbsp";
	} else {
		return value;
	}
}

function clearAttributeFields() {
	$("#selectedTraitClass").html("&nbsp;");
	$("#selectedProperty").html("&nbsp;");
	$("#selectedMethod").html("&nbsp;");
	$("#selectedScale").html("&nbsp;");
	$("#selectedDataType").html("&nbsp;");
	$("#selectedRole").html("&nbsp;");
	$("#selectedCropOntologyId").html("&nbsp;");
	$("#selectedStdVarId").val("");
	$("#selectedName").val("");
}

function getIdNameCounterpart(selectedVariable, idNameCombinationVariables) {
	var inList = -1;
	// return the id or name counterpart of the selected variable if it is in the combination list
	$.each(idNameCombinationVariables, function (index, item){
		if (parseInt(item.split("|")[0]) == parseInt(selectedVariable)) {
			inList = parseInt(item.split("|")[1]);
			return false;
		} 
		if (parseInt(item.split("|")[1]) == parseInt(selectedVariable)) {
			inList = parseInt(item.split("|")[0]);
			return false;
		}
	});
	return inList;
}

function getIdCounterpart(selectedVariable, idNameCombinationVariables) {
	var inList = selectedVariable;
	// return the id counterpart of the variable selected if it is in the list
	$.each(idNameCombinationVariables, function (index, item){ 
		if (parseInt(item.split("|")[1]) == parseInt(selectedVariable)) {
			inList = parseInt(item.split("|")[0]);
			return false;
		}
	});
	return inList;
}

function idNameCounterpartSelected(selectedVariable) {
	var itemToCompare = getIdNameCounterpart(selectedVariable, $("#idNameVariables").val().split(","));

	if (itemToCompare != -1) {
		//if it is selected/added already
		if (!notInList(itemToCompare)) {
			return true;
		}
	}
	return false;
}

function addVariableToList() { 
	var newRow;
	var rowCount = $("#newVariablesList tbody tr").length;
	var ctr;
	
	//get the last counter for the selected variables and add 1
	if (rowCount == 0) {
		ctr = 0; 
	} else {
		var lastVarId = $("#newVariablesList tbody tr:last-child td input[type='hidden']").attr("name");
		ctr = parseInt(lastVarId.substring(lastVarId.indexOf("[") + 1, lastVarId.indexOf("]"))) + 1;
	}

	var length = $("#newVariablesList tbody tr").length + 1;
	var className = length % 2 == 1 ? 'even' : 'odd';
	
	if (idNameCounterpartSelected($("#selectedStdVarId").val())) {
		//if selected variable is an id/name counterpart of a variable already selected/added
		$("#page-message-modal").html(
			    "<div class='alert alert-danger'>"+ idNameCounterpartAddedError +"</div>"
		);
	} else if (notInList($("#selectedStdVarId").val()) && $("#selectedStdVarId").val() != "") {
		//if selected variable is not yet in the list and is not blank or new, add it
		newRow = "<tr>";
		newRow = newRow + "<td class='"+className+"'><input type='hidden' class='addVariables cvTermIds' id='selectedVariables"+ ctr + ".cvTermId' " +  
			"name='selectedVariables["+ ctr + "].cvTermId' value='" + $("#selectedStdVarId").val() + "' />";
		newRow = newRow + "<input type='text' class='addVariables' id='selectedVariables"+ ctr + ".name' " +  
			"name='selectedVariables["+ ctr + "].name' maxLength='75' value='" + $("#selectedName").val() + "' /></td>";
		newRow = newRow + "<td class='"+className+"'>" + $("#selectedProperty").text() + "</td>";
		newRow = newRow + "<td class='"+className+"'>" + $("#selectedScale").text() + "</td>";
		newRow = newRow + "<td class='"+className+"'>" + $("#selectedMethod").text() + "</td>";
		newRow = newRow + "<td class='"+className+"'>" + $("#selectedRole").text() + "</td>";
		newRow = newRow + "</tr>";
		
		$("#newVariablesList").append(newRow);
		$("#page-message-modal").html("");
		
	} else {
		
		$("#page-message-modal").html(
			    "<div class='alert alert-danger'>"+ varInListMessage +"</div>"
		);
	}
}
	
function notInList(id) {
	var isNotInList = true;
	/*
	$.each($("#newVariablesList tbody tr"), function() {
		if ($(this).find("input[type='hidden']").val() == id) {
			isNotInList = false;
		}
	});
	*/
	$.each($('.cvTermIds'), function() {
		if ($(this).val() == id) {
			isNotInList = false;
		}
	});
	return isNotInList;
}

function hasNoVariableName() {
	var result = false;
	$.each($("#newVariablesList tbody tr"), function (index, row) {
		if ($($(row).children("td:nth-child(1)").children("#"+getJquerySafeId("selectedVariables"+index+".name"))).val() == "") {
			result = true;
		}
	});
	return result;
}

function submitSelectedVariables(variableType) {
	if($("#newVariablesList tbody tr").length == 0){
		$("#page-message-modal").html(
			    "<div class='alert alert-danger'>"+ noVariableAddedMessage +"</div>"
		);
	}
	else if ($("#newVariablesList tbody tr").length > 0 && hasNoVariableName()) {
		$("#page-message-modal").html(
			    "<div class='alert alert-danger'>"+ noVariableNameError +"</div>"
		);
	}
	else if ($("#newVariablesList tbody tr").length > 0) {
		replaceNameVariables();
		var serializedData = $("input.addVariables").serialize();
		$("#page-message-modal").html("");
		Spinner.toggle();
		
		$.ajax({
			url: "/Fieldbook/TrialManager/manageTrialSettings/addSettings/" + variableType,
			type: "POST",
			data: serializedData,
			success: function (data) {
				switch (variableType) {
					case 1:
						createTrialLevelSettingVariables($.parseJSON(data));
						break;
					case 2:
						createPlotLevelSettingVariables($.parseJSON(data));
						break;
					case 3:
						createBaselineTraitVariables($.parseJSON(data));
						break;
					case 4:
						createTrialEnvironmentVariables($.parseJSON(data));
						break;
					default:
						createTrialLevelSettingVariables($.parseJSON(data));
				}
				
			},
			error: function(jqXHR, textStatus, errorThrown){
				console.log("The following error occured: " + textStatus, errorThrown); 
			},
			complete: function() {
				Spinner.toggle();
				$("#addVariablesSettingModal").modal("hide");
			}
		});
	} else {
		$("#page-message-modal").html(
			    "<div class='alert alert-danger'>"+ varInListMessage +"</div>"
		);
	}
}

function replaceNameVariables() {
	$.each($("#newVariablesList tbody tr"), function (index, row){
		value = $($(row).children("td:nth-child(1)").children("#" + 
				getJquerySafeId("selectedVariables"+ index + ".cvTermId"))).val();
		//use the id counterpart of the name variable
		$($(row).children("td:nth-child(1)").children("#" 
				+ getJquerySafeId("selectedVariables"+ index + ".cvTermId"))).val(getIdCounterpart(value, $("#idNameVariables").val().split(",")));
	});

}

function getLastRowIndex(name, hasTBody) {
	if (hasTBody) {
		return $("#" + name + " tbody tr").length - 1;
	} else {
		return $("#" + name + " tr").length - 1;
	}
}

function createTrialLevelSettingVariables(data) {
	var ctr = $('.trialLevelSettings').length; //getLastRowIndex("nurseryLevelSettings", false) + 1;
	$.each(data, function (index, settingDetail) {
		var newRow = "<div class='row form-group trialLevelSettings newVariable'>";
		var isDelete = "";
		
		//include delete button if variable is deletable
		if (settingDetail.delete) {
			isDelete = "<span style='cursor: default; font-size: 16px;' class='glyphicon glyphicon-remove-circle' onclick='deleteVariable(1," + 
				settingDetail.variable.cvTermId + ",$(this))'></span>";
		}
		
		//create html elements dynamically
		newRow = newRow + "<div class='col-xs-5 col-md-5'>" + isDelete + 
		"&nbsp;&nbsp;<input class='cvTermIds' type='hidden' id='studyLevelVariables" + ctr + ".variable.cvTermId' name='studyLevelVariables[" + 
		ctr + "].variable.cvTermId' value='" + settingDetail.variable.cvTermId + "' />" + 
		"</td>";
		//newRow = newRow + "<td>" + settingDetail.variable.name + ':' + '<span class="required">*</span>' +  "</td>";
		newRow = newRow + "<span style='word-wrap: break-word'  class='control-label'>" + settingDetail.variable.name + '</span>:' + '' +  "</div>";
		newRow = newRow + "<div class='col-xs-7 col-md-7'>";
		/*
		newRow = newRow + "<input type='hidden' id='studyLevelVariables" + ctr + 
		".value' name='studyLevelVariables[" + ctr + "].value' class='form-control select2' />";
		*/
		var inputHtml = '';
				
		if(settingDetail.variable.widgetType == 'DROPDOWN'){
			inputHtml = createDropdownInput(ctr);
		}else if(settingDetail.variable.widgetType == 'DATE'){
			inputHtml = createDateInput(ctr);
		}else if(settingDetail.variable.widgetType == 'CTEXT'){
			inputHtml = createCharacterTextInput(ctr);
		}else if(settingDetail.variable.widgetType == 'NTEXT'){
			inputHtml = createNumericalTextInput(ctr);
		}else if(settingDetail.variable.widgetType == 'SLIDER'){
			inputHtml = createSliderInput(ctr, settingDetail.variable.minRange, settingDetail.variable.maxRange);
		}
		newRow = newRow + inputHtml;
		
		if (settingDetail.variable.cvTermId == breedingMethodId) {
			//show favorite method
			newRow = newRow + "<div class='possibleValuesDiv'><input type='checkbox' id='studyLevelVariables" + ctr + ".favorite1'" + 
			" name='studyLevelVariables[" + ctr + "].favorite'" +
			" onclick='javascript: toggleMethodDropdown(" + ctr + ");' />" +
			"<input type='hidden' name='_studyLevelVariables[" + ctr + "].favorite' value='on' /> " +
			"<span>&nbsp;&nbsp;" + showFavoriteMethodLabel + "</span></div>" + 
			"<div id='possibleValuesJson" + ctr + "' class='possibleValuesJson' style='display:none'>" + settingDetail.possibleValuesJson + 
			"</div><div id='possibleValuesFavoriteJson" + ctr + "' class='possibleValuesFavoriteJson' style='display:none'>" + 
			settingDetail.possibleValuesFavoriteJson + "</div>";
			
			newRow = newRow + "<span><a href='javascript: openManageMethods();'>" + manageMethodLabel + "</a></span>";
			newRow = newRow + "</div>";
			
		} else if (settingDetail.variable.cvTermId == locationId) {
				//show favorite method
				newRow = newRow + "<div class='possibleValuesDiv'><input type='checkbox' id='studyLevelVariables" + ctr + ".favorite1'" + 
				" name='studyLevelVariables[" + ctr + "].favorite'" +
				" onclick='javascript: toggleLocationDropdown(" + ctr + ");' />" +
				"<input type='hidden' name='_studyLevelVariables[" + ctr + "].favorite' value='on' /> " +
				"<span>&nbsp;&nbsp;" + showFavoriteLocationLabel + "</span></div>" + 
				"<div id='possibleValuesJson" + ctr + "' class='possibleValuesJson' style='display:none'>" + settingDetail.possibleValuesJson + 
				"</div><div id='possibleValuesFavoriteJson" + ctr + "' class='possibleValuesFavoriteJson' style='display:none'>" + 
				settingDetail.possibleValuesFavoriteJson + "</div>";
				
				newRow = newRow + "<span><a href='javascript: openManageLocations();'>" + manageLocationLabel + "</a></span>";
				newRow = newRow + "</div>";
				
		} else {
			newRow = newRow + "</div>";
		}

		$("#trialLevelSettings-dev").append(newRow);
		
		if(settingDetail.variable.widgetType == 'DROPDOWN'){
			//initialize select 2 combo
			initializePossibleValuesCombo(settingDetail.possibleValues, "#" + 
					getJquerySafeId("studyLevelVariables" + ctr + ".value"), false, null);
		}
		ctr++;
	});
	
	initializeDateAndSliderInputs();
}

function createNurseryLevelSettingVariablesOld(data) {
	var ctr = getLastRowIndex("trialLevelSettings", false) + 1;
	$.each(data, function (index, settingDetail) {
		var newRow = "<tr class='newVariable'>";
		var isDelete = "";
		
		//include delete button if variable is deletable
		if (settingDetail.delete) {
			isDelete = "<span style='cursor: default; font-size: 16px;' class='glyphicon glyphicon-remove-circle' onclick='deleteVariable(1," + 
				settingDetail.variable.cvTermId + ",$(this))'></span>";
		}
		
		//create html elements dynamically
		newRow = newRow + "<div class='col-xs-4 col-md-4 1st'>" + isDelete + 
		"<input class='cvTermIds' type='hidden' id='studyLevelVariables" + ctr + ".variable.cvTermId' name='studyLevelVariables[" + 
		ctr + "].variable.cvTermId' value='" + settingDetail.variable.cvTermId + "' />" + 
		"";
		//newRow = newRow + "<td>" + settingDetail.variable.name + ':' + '<span class="required">*</span>' +  "</td>";
		newRow = newRow + "<label class='control-label'>" + settingDetail.variable.name + '</label>:' + '' +  "</div>";
		newRow = newRow + "<div class='col-xs-8 col-md-8 2nd'>";
		/*
		newRow = newRow + "<input type='hidden' id='studyLevelVariables" + ctr + 
		".value' name='studyLevelVariables[" + ctr + "].value' class='form-control select2' />";
		*/
		var inputHtml = '';
				
		if(settingDetail.variable.widgetType == 'DROPDOWN'){
			inputHtml = createDropdownInput(ctr);
		}else if(settingDetail.variable.widgetType == 'DATE'){
			inputHtml = createDateInput(ctr);
		}else if(settingDetail.variable.widgetType == 'CTEXT'){
			inputHtml = createCharacterTextInput(ctr);
		}else if(settingDetail.variable.widgetType == 'NTEXT'){
			inputHtml = createNumericalTextInput(ctr);
		}else if(settingDetail.variable.widgetType == 'SLIDER'){
			inputHtml = createSliderInput(ctr, settingDetail.variable.minRange, settingDetail.variable.maxRange);
		}
		newRow = newRow + inputHtml;
		
		if (settingDetail.variable.cvTermId == breedingMethodId) {
			//show favorite method
			newRow = newRow + "<div class='possibleValuesDiv'><input type='checkbox' id='studyLevelVariables" + ctr + ".favorite1'" + 
			" name='studyLevelVariables[" + ctr + "].favorite'" +
			" onclick='javascript: toggleMethodDropdown(" + ctr + ");' />" +
			"<input type='hidden' name='_studyLevelVariables[" + ctr + "].favorite' value='on' /> " +
			"<span>&nbsp;&nbsp;" + showFavoriteMethodLabel + "</span></div>" + 
			"<div id='possibleValuesJson" + ctr + "' class='possibleValuesJson' style='display:none'>" + settingDetail.possibleValuesJson + 
			"</div><div id='possibleValuesFavoriteJson" + ctr + "' class='possibleValuesFavoriteJson' style='display:none'>" + 
			settingDetail.possibleValuesFavoriteJson + "</div>";
			
			newRow = newRow + "<span><a href='javascript: openManageMethods();'>" + manageMethodLabel + "</a></span>";
			newRow = newRow + "</div>";
			
		} else if (settingDetail.variable.cvTermId == locationId) {
				//show favorite method
				newRow = newRow + "<div class='possibleValuesDiv'><input type='checkbox' id='studyLevelVariables" + ctr + ".favorite1'" + 
				" name='studyLevelVariables[" + ctr + "].favorite'" +
				" onclick='javascript: toggleLocationDropdown(" + ctr + ");' />" +
				"<input type='hidden' name='_studyLevelVariables[" + ctr + "].favorite' value='on' /> " +
				"<span>&nbsp;&nbsp;" + showFavoriteLocationLabel + "</span></div>" + 
				"<div id='possibleValuesJson" + ctr + "' class='possibleValuesJson' style='display:none'>" + settingDetail.possibleValuesJson + 
				"</div><div id='possibleValuesFavoriteJson" + ctr + "' class='possibleValuesFavoriteJson' style='display:none'>" + 
				settingDetail.possibleValuesFavoriteJson + "</div>";
				
				newRow = newRow + "<span><a href='javascript: openManageLocations();'>" + manageLocationLabel + "</a></span>";
				newRow = newRow + "</div>";
				
		} else {
			newRow = newRow + "</div>";
		}

		$("#trialLevelSettings-dev").append(newRow);
		
		if(settingDetail.variable.widgetType == 'DROPDOWN'){
			//initialize select 2 combo
			initializePossibleValuesCombo(settingDetail.possibleValues, "#" + 
					getJquerySafeId("studyLevelVariables" + ctr + ".value"), false, null);
		}
		ctr++;
	});
	
	initializeDateAndSliderInputs();
}

function toggleMethodDropdown(rowIndex) {
	var possibleValues;  
	var showFavorite = $("#" + getJquerySafeId("studyLevelVariables" + rowIndex + ".favorite1")).is(":checked");
	var selectedVal;
	
	//get previously selected value
	if ($("#" + getJquerySafeId("studyLevelVariables" + rowIndex + ".value")).select2("data")) {
		selectedVal = $("#" + getJquerySafeId("studyLevelVariables" + rowIndex + ".value")).select2("data").id;
	}
	
	//reset select2 combo
	initializePossibleValuesCombo([], "#" + 
			getJquerySafeId("studyLevelVariables" + rowIndex + ".value"), false, null);
	
	//get possible values based on checkbox
	if (showFavorite) {
		possibleValues = $("#possibleValuesFavoriteJson" + rowIndex).text();
	} else {
		possibleValues = $("#possibleValuesJson" + rowIndex).text();
	}
	
	//recreate select2 combo
	initializePossibleValuesCombo($.parseJSON(possibleValues), "#" + 
			getJquerySafeId("studyLevelVariables" + rowIndex + ".value"), false, selectedVal);
}


function toggleLocationDropdown(rowIndex) {
	var possibleValues;  
	var showFavorite = $("#" + getJquerySafeId("studyLevelVariables" + rowIndex + ".favorite1")).is(":checked");
	var selectedVal;
	var showAll = true;
	
	//get previously selected value
	if ($("#" + getJquerySafeId("studyLevelVariables" + rowIndex + ".value")).select2("data")) {
		selectedVal = $("#" + getJquerySafeId("studyLevelVariables" + rowIndex + ".value")).select2("data").id;
	}
	
	//reset select2 combo
	initializePossibleValuesCombo([], "#" + 
			getJquerySafeId("studyLevelVariables" + rowIndex + ".value"), false, null);
	
	//get possible values based on checkbox
	if (showFavorite) {
		possibleValues = $("#possibleValuesFavoriteJson" + rowIndex).text();
		showAll = false;
	} else {
		possibleValues = $("#possibleValuesJson" + rowIndex).text();
	}
	
	//recreate select2 combo
	initializePossibleValuesCombo($.parseJSON(possibleValues), "#" + 
			getJquerySafeId("studyLevelVariables" + rowIndex + ".value"), showAll, selectedVal);
}

function createPlotLevelSettingVariables(data) {
	$.each(data, function (index, settingDetail) {
		var length = $("#plotLevelSettings tbody tr").length + 1;
		var className = length % 2 == 1 ? 'even' : 'odd';
		var newRow = "<tr class='newVariable'>";
		var isDelete = "";
		
		if (settingDetail.delete) {
			isDelete = "<span style='cursor: default; font-size: 16px;' class='glyphicon glyphicon-remove-circle' onclick='deleteVariable(2," + 
			settingDetail.variable.cvTermId + ",$(this))'></span>";
		}
		newRow = newRow + "<td style='text-align: center' class='"+className+"'>" + isDelete + 
		"<input class='cvTermIds' type='hidden' id='plotLevelVariables" + (length-1) + ".variable.cvTermId' name='plotLevelVariables[" + 
		(length-1) + "].variable.cvTermId' value='" + settingDetail.variable.cvTermId + "' />" + 
		"</td>";
		newRow = newRow + "<td class='"+className+"'>" + settingDetail.variable.name + "</td>"; 
		newRow = newRow + "<td class='"+className+"'>" + settingDetail.variable.description + "</td>";
		newRow = newRow + "<td class='"+className+"'>" + "<a href='javascript: void(0);' onclick='javascript:showBaselineTraitDetailsModal(" + 
		settingDetail.variable.cvTermId + ");'><span class='glyphicon glyphicon-eye-open'></span></a></td></tr>";
		$("#plotLevelSettings").append(newRow);
	});
}

function createBaselineTraitVariables(data) {
	$.each(data, function (index, settingDetail) {
		var length = $("#baselineTraitSettings tbody tr").length + 1;
		var className = length % 2 == 1 ? 'even' : 'odd';
		var newRow = "<tr class='newVariable'>";
		var isDelete = "";
		
		if (settingDetail.delete) {
			isDelete = "<span style='cursor: default; font-size: 16px;' class='glyphicon glyphicon-remove-circle' onclick='deleteVariable(3," + 
			settingDetail.variable.cvTermId + ",$(this))'></span>";
		}
		
		newRow = newRow + "<td style='text-align: center' class='"+className+"'>" + isDelete + 
		"<input class='cvTermIds' type='hidden' id='baselineTraitVariables" + (length-1) + ".variable.cvTermId' name='baselineTraitVariables[" + 
		(length-1) + "].variable.cvTermId' value='" + settingDetail.variable.cvTermId + "' />" + 
		"</td>";
		newRow = newRow + "<td class='"+className+"'>" + settingDetail.variable.name + "</td>";		
		newRow = newRow + "<td class='"+className+"'>" + settingDetail.variable.description + "</td>"
		newRow = newRow + "<td class='"+className+"'>" + "<a href='javascript: void(0);' onclick='javascript:showBaselineTraitDetailsModal(" + 
		settingDetail.variable.cvTermId + ");'><span class='glyphicon glyphicon-eye-open'></span></a></td></tr>";
		$("#baselineTraitSettings").append(newRow);
	});
}
function createTrialEnvironmentVariables(data) {
	$.each(data, function (index, settingDetail) {
		var length = $("#trialEnvironmentLevelSettings tbody tr").length + 1;
		var className = length % 2 == 1 ? 'even' : 'odd';
		var newRow = "<tr class='newVariable'>";
		var isDelete = "";
		
		if (settingDetail.delete) {
			isDelete = "<span style='cursor: default; font-size: 16px;' class='glyphicon glyphicon-remove-circle' onclick='deleteVariable(4," + 
			settingDetail.variable.cvTermId + ",$(this))'></span>";
		}
		
		newRow = newRow + "<td style='text-align: center' class='"+className+"'>" + isDelete + 
		"<input class='cvTermIds' type='hidden' id='trialLevelVariables" + (length-1) + ".variable.cvTermId' name='trialLevelVariables[" + 
		(length-1) + "].variable.cvTermId' value='" + settingDetail.variable.cvTermId + "' />" + 
		"</td>";
		newRow = newRow + "<td class='"+className+"'>" + settingDetail.variable.name + "</td>";		
		newRow = newRow + "<td class='"+className+"'>" + settingDetail.variable.description + "</td>"
		newRow = newRow + "<td class='"+className+"'>" + "<a href='javascript: void(0);' onclick='javascript:showBaselineTraitDetailsModal(" + 
		settingDetail.variable.cvTermId + ");'><span class='glyphicon glyphicon-eye-open'></span></a></td></tr>";
		$("#trialEnvironmentLevelSettings").append(newRow);
	});
}

function sortByKey(array, key) {
    return array.sort(function(a, b) {
        var x = a[key].toLowerCase(); var y = b[key].toLowerCase();
        return ((x < y) ? -1 : ((x > y) ? 1 : 0));
    });
}

function initializePossibleValuesCombo(possibleValues, name, isLocation, defaultValue) {
	var possibleValues_obj = [];
	var defaultJsonVal = null;
	
	$.each(possibleValues, function(index, value) {
		var jsonVal;
		if (value.id != undefined) {
			jsonVal = { 'id' : value.key,
					  'text' : value.name
				};
		} else if (value.locid != undefined){
			jsonVal = { 'id' : value.locid,
					  'text' : value.lname
				};
		} else {
			jsonVal = { 'id' : value.mid,
					  'text' : value.mname
				};
		}
		
		possibleValues_obj.push(jsonVal);  
		if(defaultValue != null && defaultValue != '' && 
				((defaultValue == value.key || defaultValue == value.locid || defaultValue == value.mid) || 
				 (defaultValue == value.name || defaultValue == value.lname || defaultValue == value.mname))){
			defaultJsonVal = jsonVal;
		}
		
	});
	
	possibleValues_obj = sortByKey(possibleValues_obj, "text");

	if (isLocation) {
		$(name).select2({
			minimumInputLength: 2,
			query: function (query) {	
		      var data = {results: possibleValues_obj}, i, j, s;
		      // return the array that matches
		      data.results = $.grep(data.results,function(item,index) {
		        return ($.fn.select2.defaults.matcher(query.term,item.text));
		      
		      });
		      if (data.results.length === 0){
		    	  data.results.unshift({id:query.term,text:query.term});	        	 
		      }
		      
		        query.callback(data);
		    }
	    });
	} else {
		$(name).select2({
			query: function (query) {	
		      var data = {results: possibleValues_obj}, i, j, s;
		      // return the array that matches
		      data.results = $.grep(data.results,function(item,index) {
		        return ($.fn.select2.defaults.matcher(query.term,item.text));
		      
		      });
		      if (data.results.length === 0){
		    	  data.results.unshift({id:query.term,text:query.term});	        	 
		      }
		      
		        query.callback(data);
		    }
	    });
	}
	
	if(defaultJsonVal != null){
		//console.log(defaultValue);
		//console.log(defaultJsonVal);
		$(name).select2('data', defaultJsonVal).trigger('change');
	}
}

function deleteVariable(variableType, variableId, deleteButton) {
	//remove row from UI
	deleteButton.parent().parent().remove();

	//remove row from session
	Spinner.toggle();
	$.ajax({
		url: "/Fieldbook/TrialManager/manageTrialSettings/deleteVariable/" + variableType + "/" + variableId,
		cache: false,
		type: "POST",
		success: function() {
			Spinner.toggle();
		}
	});
	
	//reinstantiate counters of ids and names
	sortVariableIdsAndNames(variableType);
	inputChange=true;
}

function sortVariableIdsAndNames(variableType) {
	switch (variableType) {
	case 1:
		var reg = new RegExp("studyLevelVariables[0-9]+", "g");
		var reg2 = new RegExp("studyLevelVariables\[[0-9]+\]", "g");
		$.each($(".nurseryLevelSettings"), function (index, row) {						
			//get currently selected value of select2 dropdown
			var selectedVal = null;
			var oldSelect2 = row.innerHTML.match(reg)[0];
		    if ($("#" + getJquerySafeId(oldSelect2 + ".value")).select2("data")) {
			   selectedVal = $("#" + getJquerySafeId(oldSelect2 + ".value")).select2("data").id;
		    }
		    
		    //if dropdown is for location or method, check if show favorite is checked
		    var isFavoriteChecked = "";
		    if ($("#" + getJquerySafeId(oldSelect2 + ".favorite1")).length != 0) {
		    	isFavoriteChecked = $("#" + getJquerySafeId(oldSelect2 + ".favorite1")).is(":checked");
		    }
		    
		    //change the ids and names of the objects 
		    row.innerHTML = row.innerHTML.replace(reg, "studyLevelVariables" + index);
			row.innerHTML = row.innerHTML.replace(reg2, "studyLevelVariables[" + index + "]");
			
			//delete the existing select2 object and recreate the select2 combo and checkbox/links for location/method
			if (row.innerHTML.indexOf("select2") > -1) {
				recreateSelect2Combo(index, row, selectedVal, isFavoriteChecked);
			}
		});
		break;
	case 2:
		var reg = new RegExp("plotLevelVariables[0-9]+", "g")
		var reg2 = new RegExp("plotLevelVariables\[[0-9]+\]", "g")
		$.each($("#plotLevelSettings tbody tr"), function (index, row) {
			row.innerHTML = row.innerHTML.replace(reg, "plotLevelVariables" + index);
			row.innerHTML = row.innerHTML.replace(reg2, "plotLevelVariables[" + index + "]");
		});
		break;
	case 4:
		var reg = new RegExp("trialLevelVariables[0-9]+", "g")
		var reg2 = new RegExp("trialLevelVariables\[[0-9]+\]", "g")
		$.each($("#trialEnvironmentLevelSettings tbody tr"), function (index, row) {
			row.innerHTML = row.innerHTML.replace(reg, "trialLevelVariables" + index);
			row.innerHTML = row.innerHTML.replace(reg2, "trialLevelVariables[" + index + "]");
		});
		break;	
	default:
		var reg = new RegExp("baselineTraitVariables[0-9]+", "g")
		var reg2 = new RegExp("baselineTraitVariables\[[0-9]+\]", "g")
		$.each($("#baselineTraitSettings tbody tr"), function (index, row) {
			row.innerHTML = row.innerHTML.replace(reg, "baselineTraitVariables" + index);
			row.innerHTML = row.innerHTML.replace(reg2, "baselineTraitVariables[" + index + "]");
		});
	}
}

function recreateSelect2Combo(index, row, selectedVal, isFavoriteChecked) {
	//get the possible values of the variable
	var possibleValuesJson = $($(row).find(".possibleValuesJson")).text();
	var possibleValuesFavoriteJson = $($(row).find(".possibleValuesFavoriteJson")).text();
	var cvTermId = $($(row).find('.1st').find("#" 
			+ getJquerySafeId("studyLevelVariables" + index + ".variable.cvTermId"))).val();
	
	//hidden field for select2 
	var newCell = "<input type='hidden' id='studyLevelVariables" + index + 
	".value' name='studyLevelVariables[" + index + "].value' class='form-control select2' />";
	
	//newCell = newCell + "<div class='div-select-val' style='display: none'>" +  + "</div>"
	
	//div containing the possible values
	newCell = newCell + "<div id='possibleValuesJson" + index + "' class='possibleValuesJson' style='display:none'>" + 
		possibleValuesJson + "</div>";
	
	//div containing the favorite possible values
	newCell = newCell + "<div id='possibleValuesFavoriteJson" + index + "' class='possibleValuesFavoriteJson' style='display:none'>" + 
		possibleValuesFavoriteJson + "</div>";
	
	//div containing checkbox and label for location and method
	var methodName = "toggleMethodDropdown";
	var favoriteLabel = showFavoriteMethodLabel;
	var managePopupLabel =  manageMethodLabel;
	var manageMethodName = "openManageMethods";
	var isChecked = "";
	var showAll = true;
	
	//set possibleValues to favorite possible values
	if (isFavoriteChecked) {
		possibleValuesJson = possibleValuesFavoriteJson;
		isChecked = "checked='checked'";
		showAll = false;
	}
	
	//set values for location
	if (parseInt(cvTermId) == parseInt(locationId)) {
		methodName = "toggleLocationDropdown";
		favoriteLabel = showFavoriteLocationLabel;
		managePopupLabel = manageLocationLabel;
		manageMethodName = "openManageLocations";
	}
	
	//add checkbox and manage location/method links
	if (parseInt(cvTermId) == parseInt(breedingMethodId) || parseInt(cvTermId) == parseInt(locationId)) {
		newCell = newCell + "<div class='possibleValuesDiv'><input type='checkbox' id='studyLevelVariables" + index + ".favorite1'" + 
		" name='studyLevelVariables[" + index + "].favorite'" +
		" onclick='javascript: " + methodName + "(" + index + ");' " + isChecked +  " />" +
		"<input type='hidden' name='_studyLevelVariables[" + index + "].favorite' value='on' /> " +
		"<span>&nbsp;&nbsp;" + favoriteLabel + "</span></div>";
		
		newCell = newCell + "<span><a href='javascript: " + manageMethodName + "();'>" + managePopupLabel + "</a></span>";
	}
	
	$($(row).find(".2nd")).html(newCell);
	
	//recreate the select2 object
	if (parseInt(cvTermId) == parseInt(locationId)) {
	    initializePossibleValuesCombo($.parseJSON(possibleValuesJson), 
 			"#" + getJquerySafeId("studyLevelVariables" + index + ".value"), showAll, selectedVal);
	} else if (parseInt(cvTermId) == parseInt(breedingMethodId)){
		initializePossibleValuesCombo($.parseJSON(possibleValuesJson), 
	 			"#" + getJquerySafeId("studyLevelVariables" + index + ".value"), false, selectedVal);
	} else {
		initializePossibleValuesCombo($.parseJSON(possibleValuesJson), 
			"#" + getJquerySafeId("studyLevelVariables" + index + ".value"), false, selectedVal);
	}
}

function hideDeleteConfirmation(){
	$('#delete-settings-confirmation').modal('hide');
}
function deleteNurserySettings(){
	var templateSettingsId = $('#selectedSettingId').val();
	if(templateSettingsId > 0){
		$('#delete-settings-confirmation').modal('hide');
		//doAjaxMainSubmit('page-message', deleteTemplateSettingSuccess, "/Fieldbook/TrialManager/manageTrialSettings/delete/" + templateSettingsId);
		
		Spinner.toggle();
		
		$.ajax({
			url: "/Fieldbook/TrialManager/manageTrialSettings/delete/" + templateSettingsId,
			type: "POST", 	
			cache: false,
			success: function (html) {
				//we just paste the whole html
				$('.container .row').first().html(html);
				
			    showSuccessfulMessage('page-message', deleteTemplateSettingSuccess);	
			    moveToTopScreen();
				Spinner.toggle();
			}
		});
			
	}else{
		alert('show error mesage');
	}
}

function clearSettings(){
	var templateSettingsId = $('#selectedSettingId').val();
	//window.location.hash = "/Fieldbook/TrialManager/manageTrialSettings/clearSettings/"+templateSettingsId+"?t=";
	
	Spinner.toggle();	
		$.ajax({
			url: "/Fieldbook/TrialManager/manageTrialSettings/clearSettings/"+templateSettingsId,
			type: "GET", 	
			cache: false,
			success: function (html) {
				//we just paste the whole html
				$('.container .row').first().html(html);
				moveToTopScreen();
				Spinner.toggle();
			}
		});
		
}
function loadNurserySettings(templateSettingsId) {
//alert($("#newVariablesList").html());
//var serializedData = $("#saveAdvanceNurseryForm").serialize();
	//window.location.hash = "/Fieldbook/TrialManager/manageTrialSettings/view/"+templateSettingsId;
	
	Spinner.toggle();
	$.ajax({
		url: "/Fieldbook/TrialManager/manageTrialSettings/view/" + templateSettingsId,
		type: "GET", 	
		cache: false,				
		success: function (html) {
			//we just paste the whole html
			$('.container .row').first().html(html);
		},
		error: function(jqXHR, textStatus, errorThrown){
			console.log("The following error occured: " + textStatus, errorThrown); 
		},
		complete: function() {
			Spinner.toggle();
		}
	});
}
function addNewSettings(){
	//window.location.hash = "/Fieldbook/TrialManager/manageTrialSettings/addNewSettings";
	
	Spinner.toggle();
	
	$.ajax({
		url: "/Fieldbook/TrialManager/manageTrialSettings/addNewSettings",
		type: "GET",
		cache: false,
		success: function (html) {
			//we just paste the whole html
			$('.container .row').first().html(html);				
			Spinner.toggle();
		}
	});
	
}
function copySettings() {
	var templateSettingsId = $('#selectedSettingId').val();
	Spinner.toggle();
	$.ajax({
		url: "/Fieldbook/TrialManager/manageTrialSettings/copy/" + templateSettingsId,
		type: "GET", 	
		cache: false,				
		success: function (html) {
			//we just paste the whole html
			$('.container .row').first().html(html);			
			showSuccessfulMessage('page-message', copyTemplateSettingSuccess);	
			Spinner.toggle();
		}
	});
}
function hasDuplicateSettingName(){
	var selectedSettingsId = $('#selectedSettingId').val();
	var settingsName = $('#settingName').val() ;
	var hasDuplicate = false;
	$('#selectedSettingId option').each(function(){
	    if(selectedSettingsId != $(this).val() &&  $(this).html().trim() == settingsName)
	    	hasDuplicate = true;
	})
	return hasDuplicate;
}
function hasEmptyTrialValue(){
	//would only check for the data numeric
	
	var hasError = false;
	var name = '';
	$('.numeric-input').each(function(){
		$(this).val($.trim($(this).val()));
		if(hasError == false && $(this).val() != '' && isNaN($(this).val())){
			hasError = true;
			name = $(this).parent().parent().find('.control-label').html();
			
		}
	});
	if(hasError){
		showErrorMessage('page-message', name + " " + nurseryNumericError);
	}
		
	return hasError;
	
}
function doSaveSettings(){
	$('#settingName').val($('#settingName').val().trim());
	if($('#settingName').val() == ''){
		showErrorMessage('page-message', templateSettingNameError);
		moveToTopScreen();
	return false;
	}else if(hasDuplicateSettingName()){
		showErrorMessage('page-message', templateSettingNameErrorUnique);
		moveToTopScreen();
	return false;
	} else if(hasEmptyTrialValue()){
		//showErrorMessage('page-message', nurseryLevelValueEmpty);
		moveToTopScreen();
		return false;
	}  else if(!validateStartEndDate('trialLevelSettings')){
		moveToTopScreen();
		return false; 		
	} else{ 		
		doAjaxMainSubmit('page-message', saveTemplateSettingSuccess, null);
		moveToTopScreen();
		/*
	Spinner.toggle();
	var $form = $("#saveNurserySettingsForm");
	serializedData = $form.serialize();
	
		$.ajax({
			url: "/Fieldbook/TrialManager/manageTrialSettings/save/",
			type: "POST", 	
			data: serializedData,
			success: function (html) {
				//we just paste the whole html
				$('.container .row').first().html(html);
				
			    showSuccessfulMessage('page-message', saveTemplateSettingSuccess);				
				Spinner.toggle();
			}
		}); 
		*/
	}
				
}

function showBaselineTraitDetailsModal(id) {
	if(id != ''){
		Spinner.toggle();
		$.ajax({
			url: "/Fieldbook/TrialManager/manageTrialSettings/showVariableDetails/" + id,
			type: "GET",
			cache: false,
			success: function (data) {
				populateBaselineTraits($.parseJSON(data));
				$("#baselineTraitDetails").modal("toggle");
			},
			error: function(jqXHR, textStatus, errorThrown){
				console.log("The following error occured: " + textStatus, errorThrown); 
			},
			complete: function() {
				Spinner.toggle();
			}
		});
	}
}

function populateBaselineTraits(standardVariable) {
	if (standardVariable != null) {
		$("#traitClass").html(checkIfNull(standardVariable.traitClass));
		$("#property").html(checkIfNull(standardVariable.property));
		$("#method").html(checkIfNull(standardVariable.method));
		$("#scale").html(checkIfNull(standardVariable.scale));
		$("#dataType").html(checkIfNull(standardVariable.dataType));
		$("#role").html(checkIfNull(standardVariable.role));
		$("#cropOntologyId").html(checkIfNull(standardVariable.cropOntologyId));
		//$('#trialDetailsTitle').html(headerTitle + " " + );
	} else {
		$("#traitClass").html("");
		$("#property").html("");
		$("#method").html("");
		$("#scale").html("");
		$("#dataType").html("");
		$("#role").html("");
		$("#cropOntologyId").html("");
	}
}

function checkIfNull(object) {
	if (object != null) {
		return object;
	} else {
		return "";
	}
}

function createSliderInput(ctr, minVal, maxVal){
	
	return "<input data-slider-orientation='horizontal' data-slider-selection='after' type='text' data-step='0.1' data-min='"+minVal+"' data-max='"+maxVal+"' id='studyLevelVariables" + ctr + 
	".value' name='studyLevelVariables[" + ctr + "].value' class='form-control numeric-range-input' />";
}
function createDropdownInput(ctr){
	 return "<input type='hidden' id='studyLevelVariables" + ctr + 
		".value' name='studyLevelVariables[" + ctr + "].value' class='form-control select2' />";
}
function createDateInput(ctr){	
	 return "<input type='text' id='studyLevelVariables" + ctr + 
		".value' name='studyLevelVariables[" + ctr + "].value' class='form-control date-input' />";
	 
}
function createNumericalTextInput(ctr){
	return "<input type='text' id='studyLevelVariables" + ctr + 
	".value' name='studyLevelVariables[" + ctr + "].value' class='form-control numeric-input' />";
}
function createCharacterTextInput(ctr){
	return "<input type='text' id='studyLevelVariables" + ctr + 
	".value' name='studyLevelVariables[" + ctr + "].value' class='form-control character-input' />";

}
function initializeDateAndSliderInputs(){
	if($('.date-input').length > 0){
		$('.date-input').each(function(){
			$(this).datepicker({'format': 'yyyymmdd'}).on('changeDate', function(ev) {
		
			$(this).datepicker('hide');
		})
		});
	}
	
	if($('.numeric-range-input').length > 0){
		
		$('.numeric-range-input').each(function(){
		//console.log($(this).val());
		//console.log(parseFloat($(this).data('min')));
		//console.log(parseFloat($(this).val()));
		var currentVal  = $(this).val() == '' ? parseFloat($(this).data('min')) : parseFloat($(this).val());
		//console.log(currentVal);
			$(this).slider({
				min: parseFloat($(this).data('min')),
				max: parseFloat($(this).data('max')),
				step: parseFloat($(this).data('step')),
				value: currentVal,
				formater: function(value) {
					return 'Value: ' + value;
				}
			});
		});
	}				
}

function checkPlantsSelected() {
	if (parseInt($("#plotsWithPlantsSelected").val()) == 0) {
		showErrorMessage('page-message',msgEmptyListError);
		$("#lineChoice1").prop("checked", true);
	}
}

function doResetNurserySettings(){
	$('#reset-settings-confirmation').modal('hide');
	addNewSettings();
}
function validateReset(){
	if(inputChange == true || ($('.newVariable') != null && $('.newVariable').length > 0)){
		$('#reset-settings-confirmation').modal('show');
	}else{
		doResetNurserySettings();	
	}
	
}

function loadTrialSettingsForCreate(templateSettingsId) {
 	Spinner.toggle();
	var $form = $("#createTrialForm");
	
	var serializedData = $form.serialize();

	$.ajax({
		url: "/Fieldbook/TrialManager/createTrial/view/" + templateSettingsId,
		type: "POST",
		data: serializedData,
		cache: false,
		timeout: 70000,
		success: function (html) {
			//we just paste the whole html
			//$('.container .row').first().html(html);
			$("#chooseSettingsDiv").html(html);
			$("#showFavoriteLocationForAll").removeAttr('disabled');
		    $("#trialInstances").removeAttr('disabled');
		},
		error: function(jqXHR, textStatus, errorThrown){
			console.log("The following error occured: " + textStatus, errorThrown); 
		},
		complete: function() {
			Spinner.toggle();
		}
	});
}

function displayGermplasmListTree(treeName) {
	$("#" + treeName).dynatree({
		title: treeName,
		checkbox: false,
		noLink: false,
		autoFocus: false,
		imagePath: "../img/",
		activeVisible: true,
		initAjax: {url: "loadInitGermplasmTree",
			dataType: "json"
		},
		onLazyRead: function(node) {
			node.appendAjax({
				url: "expandGermplasmTree/" + node.data.key,
				dataType: "json",
				success: function(node) {
					//do nothing
				},
				error: function(node, XMLHttpRequest, textStatus, errorThrown) {
					console.log("The following error occured: " + textStatus, errorThrown); 
				},
				cache: false
			});
		},
		classNames: {
			container: "fbtree-container",
			expander: "fbtree-expander",
			nodeIcon: "fbtree-icon",
			combinedIconPrefix: "fbtree-ico-",
			focused: "fbtree-focused",
			active: "fbtree-active"
		},
		onActivate: function(node) {
			if (node.data.isFolder == false) {
 				displayGermplasmDetails(node.data.key);
			}
		}
	});
}

function displayGermplasmDetails(listId) {
	Spinner.toggle();
	$.ajax({
		url: "importGermplasmList/displayGermplasmDetails/" + listId,
		type: "GET",
		cache: false,
		success: function(html) {
			$("#imported-germplasm-list").html(html);	
			Spinner.toggle();
		}
		
	});
}

function openUsePreviousTrialModal() {
	$("#selectedTrial").select2("destroy");
	$("#selectedTrial").val("")
	$("#selectedTrial").select2();
	$("#usePreviousTrialModal").modal("show");
}

function chooseSelectedTrial() {
	var nurseryId = $("#selectedTrial").val();
	var url = "/Fieldbook/TrialManager/manageTrialSettings/trial/";
		
	if ($("#chooseSettingsDiv").length != 0) {
		url = "/Fieldbook/TrialManager/createTrial/trial/";
	}
	
	$("#usePreviousTrialModal").modal("hide");
	Spinner.toggle();
	$.ajax({
		url: url + nurseryId,
        type: "GET",
        cache: false,
        data: "",
        success: function(html) {
        	if ($("#chooseSettingsDiv").length != 0) {
        		$("#chooseSettingsDiv").html(html);
        	} else {
        		$('.container .row').first().html(html);
        	}
        	Spinner.toggle();
        }
	})
}

function validateCreateTrial() {
	var hasError = false;
	var name;
	var customMessage = '';
	if ($("#selectedSettingId").val() == '0') {
		hasError = true;
		name = $("#settingsSelectLabel").text();
		customMessage = requiredSettingErrorMessage;
	}
	else if ($("#folderId").val() == '') {
		hasError = true;
		name = $("#folderLabel").text();
	}
	else if ($("#fieldLayoutRandom").val() == '') {
		hasError = true;
		name = $("#expDesignLabel").text();
	}
	/*else if ($("#imported-germplasm-list").html().indexOf("GID") > -1) {
		hasError = true;
		name = $("#germplasmLabel").text();
	}*/
	else {
		
		var cvTermId;
		$('.nurseryLevelVariableIdClass').each(function(){
			if (!hasError) {
				cvTermId = $(this).val();
				
				if ($.inArray(cvTermId, requiredFields) > -1) {
					if ($(this).parent().find(".form-control").hasClass("select2") && $(this).parent().find(".form-control").select2("data")) {
						idname = $(this).parent().find(".form-control").attr("id");
						//console.log(idname);
						value = $("#" + getJquerySafeId(idname)).select2("data").text;
					}
					else {
						value = $(this).parent().find(".form-control").val();
					}
					value = $.trim(value);
					if (!value) {
						name = $(this).parent().parent().find(".control-label").html();
						hasError = true;
					}
				}
			}
			
		});
	}
	
	if (hasError){
		var errMsg = requiredErrorMessage + ": " + name.replace('*', '').replace(":", "");
		if(customMessage != '')
			errMsg = customMessage;
		showErrorMessage('page-message', errMsg);
		return false;
	}
	return true;
}
function reloadCheckTypeDropDown(addOnChange){
	Spinner.toggle();
	var currentCheckId = $('#checkId').val();
	$.ajax(
    	{ url: "/Fieldbook/NurseryManager/importGermplasmList/getAllCheckTypes",
           type: "GET",
           cache: false,
           data: "",
           success: function(data) {	        	   
        		   //recreate the select2 combos to get updated list of locations
        		   $('#checkId').select2('destroy');
        		   $('#checkValue').val("");
        		   initializeCheckTypeSelect2($.parseJSON(data.allCheckTypes), [], addOnChange, currentCheckId, getJquerySafeId('checkId'));	   
        	   	   Spinner.toggle();
           }
         }
     );
}
function initializeCheckTypeSelect2(suggestions, suggestions_obj, addOnChange, currentFieldId, comboName) {
	var defaultData = null;
	
	if (suggestions != null) {
		$.each(suggestions, function( index, value ) {
			if (comboName == "comboCheckCode") {
				dataObj = { 'id' : value.id,
					  'text' : value.name,
					  'description' : value.description};  
			} else {
				dataObj = { 'id' : value.id,
					  'text' : value.description,
					  'description' : value.description};
			}
			suggestions_obj.push(dataObj);
			if (comboName != "comboCheckCode") {
				var specificVal = '';
				if($('#'+getJquerySafeId(comboName)).select2('data') != null)
					specificVal = $('#'+getJquerySafeId(comboName)).select2('data').text;
				if(defaultData == null){
					if(currentFieldId != '' && currentFieldId == dataObj.id){
						defaultData = dataObj;
					}else if(currentFieldId == '' && 'CHECK' == value.name){
						defaultData = dataObj;
					}
				}
				
			}
		});
	} else {
		$.each(suggestions_obj, function( index, value ) {
			if(currentFieldId != '' && currentFieldId == value.id){
				defaultData = value;
			}
		});
	}
	//if combo to create is one of the ontology combos, add an onchange event to populate the description based on the selected value
	if (comboName == "comboCheckCode") {
		$('#'+comboName).select2({
			query: function (query) {	
		          var data = {results: sortByKey(suggestions_obj, "text")}, i, j, s;
		          // return the array that matches
		          data.results = $.grep(data.results,function(item,index) {
		            return ($.fn.select2.defaults.matcher(query.term,item.text));
		          });
		          if (data.results.length === 0){
		        	  data.results.unshift({id:query.term,text:query.term});	        	 
		          } 
		            query.callback(data);
		        }
	    }).on("change", function(){
	    	if ($("#comboCheckCode").select2("data")) {
	    		if ($("#comboCheckCode").select2("data").id == $("#comboCheckCode").select2("data").text) {
	    			$("#manageCheckValue").val("");
	    	    	$("#updateCheckTypes").hide();
	    			$("#deleteCheckTypes").hide();
	    			$("#addCheckTypes").show();
	    		} else {
	    			$("#manageCheckValue").val($("#comboCheckCode").select2("data").description);
	    	    	$("#updateCheckTypes").show();
	    			$("#deleteCheckTypes").show();
	    			$("#addCheckTypes").hide();
	    		}
	    	}
	    });
	} else {
		$('#'+comboName).select2({
	        query: function (query) {
	          var data = {results: sortByKey(suggestions_obj, "text")}, i, j, s;
	          // return the array that matches
	          data.results = $.grep(data.results,function(item,index) {
	            return ($.fn.select2.defaults.matcher(query.term,item.text)); 
	          });
	            query.callback(data);
	        }
	    });
		$('#checkId').val('')
		if(addOnChange){
			
			$('#'+getJquerySafeId('checkId')).on("change", function (){
		    	
		    	$('#'+getJquerySafeId("checkValue")).val($('#'+getJquerySafeId("checkId")).select2('data').text);
		    	
		    });
		}
	}
	//console.log(defaultData);
	if(defaultData != null){		
		$('#'+comboName).select2('data', defaultData).trigger('change');
	}
		
}
function showManageCheckTypePopup(){
	$('#page-check-message-modal').html("");
	resetButtonsAndFields();
	//recreatePopupLocationCombo();
	$('#manageCheckTypesModal').modal({ backdrop: 'static', keyboard: false });		   	
}

function resetButtonsAndFields() {
	$("#manageCheckValue").val("");
	$("#comboCheckCode").select2("val", "");
	$("#updateCheckTypes").hide();
	$("#deleteCheckTypes").hide();
	$("#addCheckTypes").show();
}

function addUpdateCheckType(operation) {
	if (validateCheckFields()) {
		var $form = $("#manageCheckValue,#comboCheckCode");	
		var serializedData = $form.serialize();
		Spinner.toggle();
		$.ajax({
			url: "/Fieldbook/NurseryManager/importGermplasmList/addUpdateCheckType/" + operation,
			type: "POST",
            data: serializedData,
            cache: false,
         	success: function(data) {
      	    if (data.success == "1") {
      	    	//reload dropdown
      	    	reloadCheckTypeList(data.checkTypes, operation);
      	    	showCheckTypeMessage(data.successMessage);
      	    } else {
      	    	showCheckTypeErrorMessage(data.error);
      	    }
         		Spinner.toggle();
     	}
		});
	}
}

function validateCheckFields(){
	if (checkTypes_obj.length == 0 && checkTypes != null) {
		$.each(checkTypes, function (index, item){
			checkTypes_obj.push({ 'id' : item.id,
			  'text' : item.name,
			  'description' : item.description
			});
		});
	}
	
	if (!$("#comboCheckCode").select2("data")) {
		showCheckTypeErrorMessage(codeRequiredError);
		return false;
	} else if ($("#manageCheckValue").val() == "") {
		showCheckTypeErrorMessage(valueRequiredError);
		return false;
	} else if (!isValueUnique()) {
		showCheckTypeErrorMessage(valueNotUniqueError);
		return false;
	}
	
	return true;
}

function isValueUnique() {
	var isUnique = true;
	$.each(checkTypes_obj, function(index, item) {
		if (item.description == $("#manageCheckValue").val() && item.id != $("#comboCheckCode").select2("data").id) {
			isUnique = false;
			return false;
		}
	});
	return isUnique;
}

function showCheckTypeErrorMessage(message) {
	$('#page-check-message-modal').html("<div class='alert alert-danger'>"+ message +"</div>");
}

function showCheckTypeMessage(message) {
	$('#page-check-message-modal').html("<div class='alert alert-success'>"+ message +"</div>");
}

function deleteCheckType() {
	if ($("manageCheckCode").select2("data")) {
		
		var $form = $("#manageCheckValue,#comboCheckCode");	
		var serializedData = $form.serialize();
		Spinner.toggle();
		$.ajax({
			url: "/Fieldbook/NurseryManager/importGermplasmList/deleteCheckType",
			type: "POST",
            data: serializedData,
            cache: false,
         	success: function(data) {
         		if (data.success == "1"){
         			reloadCheckTypeList(data.checkTypes, 3);
         			showCheckTypeMessage(data.successMessage);
         			resetButtonsAndFields();
         		} else {
         			showCheckTypeErrorMessage(data.error);
         		}
         		Spinner.toggle();
     	}
		});
	} else {
		showCheckTypeErrorMessage(noCheckSelected);
	}
}

function reloadCheckTypeList(data, operation) {
	var selectedValue = 0;
	
	checkTypes_obj = [];
	
	if (data != null) {
	$.each($.parseJSON(data), function( index, value ) {
		checkTypes_obj.push({ 'id' : value.id,
			  'text' : value.name,
			  'description' : value.description
		});  
	});
	}
	
	if (operation == 2) {
		//update
		selectedValue = getIdOfValue($("#manageCheckValue").val());
	} 
	
	$("#manageCheckValue").val("");
	initializeCheckTypeSelect2(null, [], false, 0, "comboCheckCode");
	initializeCheckTypeSelect2(null, checkTypes_obj, false, selectedValue, "comboCheckCode");
}

function getIdOfValue(value) {
	var id = 0;
	$.each(checkTypes_obj, function (index, item){
		if (item.description == value) {
			id = item.id;
			return false;
		}
	});
	return id;
}

function recreateSelect2ComboMultiple(index, row, selectedVal) {
	$.each($(row).children("td"), function (cellIndex, cell) {
		//set trial instance no.
		
		if (parseInt($($(cell).children(".cvTermIds")).val()) == parseInt(trialInstanceId)) {
			$($(cell).children(".trialInstanceNo")).text(index+1);
			$($(cell).children("#"+getJquerySafeId("trialEnvironmentValues"+ index + cellIndex +".name"))).val(index+1);
		}
		
		if (cell.innerHTML.indexOf("select2") > -1) {
			//get the possible values of the variable
			var possibleValuesJson = $($(cell).find(".possibleValuesJsonTrial")).text();
			var possibleValuesFavoriteJson = $($(cell).find(".possibleValuesFavoriteJsonTrial")).text();
			
			var cvTermId = $($(cell).children("#" 
					+ getJquerySafeId("trialEnvironmentValues" + index + cellIndex + ".id"))).val();
			
			var newCell = "<input class='cvTermIds trialLevelVariableIdClass' type='hidden' id='trialEnvironmentValues" + 
				index + cellIndex + ".id' name='trialEnvironmentValues[" + index + "][" + cellIndex + "].id' value='" + cvTermId + "' />";

			//hidden field for select2 
			newCell = newCell + "<input type='hidden' id='trialEnvironmentValues" + index + cellIndex +
			".name' name='trialEnvironmentValues[" + index + "][" + cellIndex + "].name' class='form-control select2' />";
			
			//div containing the possible values
			newCell = newCell + "<div id='possibleValuesJsonTrial" + index + "a" + cellIndex + "' class='possibleValuesJsonTrial' style='display:none'>" + 
				possibleValuesJson + "</div>";
			
			//div containing the favorite possible values
			newCell = newCell + "<div id='possibleValuesFavoriteJsonTrial" + index + "a" + cellIndex + "' class='possibleValuesFavoriteJsonTrial' style='display:none'>" + 
				possibleValuesFavoriteJson + "</div>";
			
			var isFavoriteChecked = $("#showFavoriteLocationForAll").is(":checked");
			var showAll = true;
			//set possibleValues to favorite possible values
			if (isFavoriteChecked && parseInt(cvTermId) == parseInt(locationId)) {
				possibleValuesJson = possibleValuesFavoriteJson;
				showAll = false;
			}
						
			cell.innerHTML = newCell;
			
			//recreate the select2 object
			if (parseInt(cvTermId) == parseInt(locationId)) {
			    initializePossibleValuesCombo($.parseJSON(possibleValuesJson), 
		 			"#" + getJquerySafeId("trialEnvironmentValues" + index + cellIndex +".name"), showAll, null);
			} else {
				initializePossibleValuesCombo($.parseJSON(possibleValuesJson), 
					"#" + getJquerySafeId("trialEnvironmentValues" + index + cellIndex +".name"), false, null);
			}
		}
	});
}

function editTrialInstances() {
	if($("#trialInstancesTable tbody tr").length < $("#trialInstances").val()) {
		addTrialInstances();
	} else if ($("#trialInstancesTable tbody tr").length > $("#trialInstances").val()) {
		removeTrialInstances();
	}
}

function addTrialInstances() {
	var reg = new RegExp("trialEnvironmentValues0", "g");
	var reg2 = new RegExp("trialEnvironmentValues\[[0-9]+\]", "g");
	var reg3 = new RegExp("possibleValuesJsonTrial0a", "g");
	var reg4 = new RegExp("possibleValuesFavoriteJsonTrial0a", "g");

	for (var i = $("#trialInstancesTable tbody tr").length; i < $("#trialInstances").val(); i++) {
		var cells = $("#trialInstancesTable tbody tr").get(0).innerHTML.replace(reg, "trialEnvironmentValues" + i);
		cells = cells.replace(reg2, "trialEnvironmentValues[" + i + "]");
		cells = cells.replace(reg3, "possibleValuesJsonTrial" + i + "a");
		cells = cells.replace(reg4, "possibleValuesFavoriteJsonTrial" + i + "a");
		
		newRow = "<tr>" + cells + "</tr>";
		$("#trialInstancesTable tbody").append(newRow);
		
		if (newRow.indexOf("select2") > -1) {
			recreateSelect2ComboMultiple(i, $("#trialInstancesTable tbody tr:last"), null);
		}
	}
	
}

function removeTrialInstances() {
	for (var i = $("#trialInstancesTable tbody tr").length; i > $("#trialInstances").val(); i--) {
		$("#trialInstancesTable tbody tr:last").remove();
	}
}

function toggleLocationDropdownForAll(){
	$.each($("#trialInstancesTable tbody tr"), function (index, row){
		$.each($(row).children("td"), function (cellIndex, cell) {
			if ($($(cell).children(".cvTermIds")).val() == locationId) {
				//get the possible values of the variable
				var possibleValuesJson = $($(cell).find(".possibleValuesJsonTrial")).text();
				var possibleValuesFavoriteJson = $($(cell).find(".possibleValuesFavoriteJsonTrial")).text();
				var isFavoriteChecked = $("#showFavoriteLocationForAll").is(":checked");
				var showAll = true;
				var selectedVal = null;
				
				//get previously selected value
				if ($("#" + getJquerySafeId("trialEnvironmentValues" + index + cellIndex + ".name")).select2("data")) {
					selectedVal = $("#" + getJquerySafeId("trialEnvironmentValues" + index + cellIndex + ".name")).select2("data").id;
				}

				//set possibleValues to favorite possible values
				if (isFavoriteChecked) {
					possibleValuesJson = possibleValuesFavoriteJson;
					showAll = false;
				}

				initializePossibleValuesCombo([], 
			 			"#" + getJquerySafeId("trialEnvironmentValues" + index + cellIndex +".name"), showAll, null);
				
				initializePossibleValuesCombo($.parseJSON(possibleValuesJson), 
			 			"#" + getJquerySafeId("trialEnvironmentValues" + index + cellIndex +".name"), showAll, selectedVal);
				
			}
		});
	});
}
