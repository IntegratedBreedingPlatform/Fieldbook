package com.efficio.fieldbook.web.nursery.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.ErrorCode;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.efficio.fieldbook.web.AbstractBaseControllerTest;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;

public class CreateNurseryControllerTest extends AbstractBaseControllerTest {
	
	@Autowired
	private CreateNurseryController controller;
	
	@Autowired
	private UserSelection userSelection;
	
	@Resource
	private FieldbookService fieldbookMiddlewareService;
	
	@Test
	public void testGetReturnsCorrectModelAndView() throws Exception {
		
		ModelAndView mav = request(CreateNurseryController.URL, HttpMethod.GET.name());
		
		ModelAndViewAssert.assertViewName(mav, CreateNurseryController.BASE_TEMPLATE_NAME);
		ModelAndViewAssert.assertModelAttributeValue(mav, 
				AbstractBaseFieldbookController.TEMPLATE_NAME_ATTRIBUTE, "/NurseryManager/createNursery");
		
		Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
		ModelAndViewAssert.assertModelAttributeAvailable(mav, "createNurseryForm");	
	}
	
	@Test
	public void testUseExistingNursery() throws Exception {
		fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		Mockito.when(fieldbookMiddlewareService.getStudyVariableSettings(1, true))
			.thenThrow(new MiddlewareQueryException(ErrorCode.STUDY_FORMAT_INVALID.getCode(), "The term you entered is invalid"));
		controller.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		
		ModelAndView mav = request(CreateNurseryController.URL + "/nursery/1", HttpMethod.GET.name());
		
		Assert.assertEquals("Expected HttpStatus OK but got " + response.getStatus() + " instead.", 
				HttpStatus.OK.value(), response.getStatus());
		ModelAndViewAssert.assertModelAttributeAvailable(mav, "createNurseryForm");
	}
	
	@Test
	public void testAddErrorMessageToResult() throws Exception {
		CreateNurseryForm form = new CreateNurseryForm();

		controller.addErrorMessageToResult(form,
				new MiddlewareQueryException(ErrorCode.STUDY_FORMAT_INVALID.getCode(), "The term you entered is invalid"));
		
		Assert.assertEquals("Expecting error but did not get one", "1", form.getHasError());
	}
	
	@Test
	public void testSubmitWithMinimumRequiredFields() throws MiddlewareQueryException {
		
		CreateNurseryForm form = new CreateNurseryForm();

		SettingDetail studyName = new SettingDetail();
		SettingVariable studyNameVariable = new SettingVariable();
		studyNameVariable.setCvTermId(TermId.STUDY_NAME.getId());
		studyName.setVariable(studyNameVariable);
				
		SettingDetail studyObj = new SettingDetail();
		SettingVariable studyObjVariable = new SettingVariable();
		studyObjVariable.setCvTermId(TermId.STUDY_OBJECTIVE.getId());
		studyObj.setVariable(studyObjVariable);
		
		List<SettingDetail> basicDetails = Arrays.asList(studyName, studyObj);
		form.setBasicDetails(basicDetails);
		userSelection.setBasicDetails(basicDetails);
		
		this.userSelection.setStudyLevelConditions(new ArrayList<SettingDetail>());
		
		String submitResponse = controller.submit(form, new ExtendedModelMap());
		Assert.assertEquals("success", submitResponse);
	}
	
	@Test 
	public void testSetLocationVariableValueWhenVariableIsLocationAndHasANonEmptyValue() throws MiddlewareQueryException{
		CreateNurseryController createNurseryController = new CreateNurseryController();
		org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService = Mockito.mock(org.generationcp.middleware.service.api.FieldbookService.class);
		createNurseryController.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		String locationId = "1001";
		
		Location location = new Location();		
		location.setLocid(Integer.parseInt(locationId));
		
		Mockito.when(fieldbookMiddlewareService.getLocationById(Integer.parseInt(locationId))).thenReturn(location);
		SettingDetail detail = new SettingDetail();
		MeasurementVariable var = new MeasurementVariable();
		var.setTermId(TermId.LOCATION_ID.getId());
		var.setValue(locationId);
		createNurseryController.setLocationVariableValue(detail, var);
		Assert.assertEquals("Should be able to set the value in the setting detail if its a location",locationId, detail.getValue());
	}
	
	@Test 
	public void testSetLocationVariableValueWhenVariableIsLocationAndHasAnEmptyValue() throws MiddlewareQueryException{
		CreateNurseryController createNurseryController = new CreateNurseryController();
		org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService = Mockito.mock(org.generationcp.middleware.service.api.FieldbookService.class);
		createNurseryController.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		String locationId = "1001";
		
		Location location = new Location();		
		location.setLocid(Integer.parseInt(locationId));
		
		Mockito.when(fieldbookMiddlewareService.getLocationById(Integer.parseInt(locationId))).thenReturn(location);
		SettingDetail detail = new SettingDetail();
		MeasurementVariable var = new MeasurementVariable();
		var.setTermId(TermId.LOCATION_ID.getId());
		var.setValue("");
		createNurseryController.setLocationVariableValue(detail, var);
		Assert.assertEquals("Setting detail should have an empty value since the variable did not have a value itself","", detail.getValue());
	}
	
	@Test
	public void testSetSettingDetailsValueFromVariableWhenVariableLocationIdIsANonEmptyValue() throws NumberFormatException, MiddlewareQueryException{
		CreateNurseryController createNurseryController = new CreateNurseryController();
		org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService = Mockito.mock(org.generationcp.middleware.service.api.FieldbookService.class);
		createNurseryController.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		String locationId = "1001";
		Location location = new Location();		
		location.setLocid(Integer.parseInt(locationId));
		
		Mockito.when(fieldbookMiddlewareService.getLocationById(Integer.parseInt(locationId))).thenReturn(location);
	
		SettingDetail detail = new SettingDetail();
		MeasurementVariable var = new MeasurementVariable();
		var.setTermId(TermId.LOCATION_ID.getId());
		var.setValue(locationId);
		
		createNurseryController.setSettingDetailsValueFromVariable(var, detail);
		Assert.assertEquals("Should be able to set the value in the setting detail if its a location",locationId, detail.getValue());
	}
	
	@Test
	public void testSetSettingDetailsValueFromVariableWhenVariableLocationIdIsAnEmptyValue() throws NumberFormatException, MiddlewareQueryException{
		CreateNurseryController createNurseryController = new CreateNurseryController();
		org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService = Mockito.mock(org.generationcp.middleware.service.api.FieldbookService.class);
		createNurseryController.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		String locationId = "1001";
		Location location = new Location();		
		location.setLocid(Integer.parseInt(locationId));
		
		Mockito.when(fieldbookMiddlewareService.getLocationById(Integer.parseInt(locationId))).thenReturn(location);
	
		SettingDetail detail = new SettingDetail();
		MeasurementVariable var = new MeasurementVariable();
		var.setTermId(TermId.LOCATION_ID.getId());
		var.setValue("");
		
		createNurseryController.setSettingDetailsValueFromVariable(var, detail);
		Assert.assertEquals("Setting detail should have an empty value if the location variable is an empty value", "", detail.getValue());
	}
	
	@Test
	public void testSetSettingDetailsValueFromVariableWhenVariableBreedingMethodCodeIsANonEmptyValue() throws NumberFormatException, MiddlewareQueryException{
		CreateNurseryController createNurseryController = new CreateNurseryController();
		org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService = Mockito.mock(org.generationcp.middleware.service.api.FieldbookService.class);
		createNurseryController.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		String bmCode = "TST_CODE";
		Integer bmId = 10001;
		Method method = new Method();		
		method.setMcode(bmCode);
		method.setMid(bmId);
		
		Mockito.when(fieldbookMiddlewareService.getMethodByCode(bmCode)).thenReturn(method);
	
		SettingDetail detail = new SettingDetail();
		MeasurementVariable var = new MeasurementVariable();
		var.setTermId(TermId.BREEDING_METHOD_CODE.getId());
		var.setValue(bmCode);
		
		createNurseryController.setSettingDetailsValueFromVariable(var, detail);
		Assert.assertEquals("Setting detail should the corresponding id of the matching method for the method code", bmId.toString(), detail.getValue());
	}
}
