
package com.efficio.fieldbook.service;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.bean.PossibleValuesCache;
import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.LocationTestDataInitializer;
import org.generationcp.middleware.data.initializer.StandardVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.VariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.service.api.user.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class FieldbookServiceTest {

	private static final String LABBR = "labbr";
	private static final String METHOD_DESCRIPTION = "Method Description 5";
	private static final String LOCATION_NAME = "Loc1";
	private static final String PROGRAMUUID = "1000001";

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Mock
	private UserService userService;

	@InjectMocks
	private FieldbookServiceImpl fieldbookServiceImpl;

	@Mock
	private OntologyService ontologyService;

	private MeasurementVariable locationVariable;
	private MeasurementVariable nonLocationVariable;

	private PossibleValuesCache possibleValuesCache;

	@Before
	public void setUp() throws MiddlewareException {
		final List<Location> allLocation = new ArrayList<>();
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(FieldbookServiceTest.PROGRAMUUID);
		allLocation.add(LocationTestDataInitializer.createLocation(1, FieldbookServiceTest.LOCATION_NAME));
		allLocation.add(LocationTestDataInitializer.createLocation(2, "Loc2"));
		Mockito.when(this.fieldbookMiddlewareService.getAllLocations())
				.thenReturn(allLocation);
		Mockito.when(this.fieldbookMiddlewareService.getAllBreedingLocations()).thenReturn(new ArrayList<>());

		this.fieldbookServiceImpl.setFieldbookMiddlewareService(this.fieldbookMiddlewareService);
		this.fieldbookServiceImpl.setOntologyService(this.ontologyService);
		this.possibleValuesCache = new PossibleValuesCache();
		this.fieldbookServiceImpl.setPossibleValuesCache(this.possibleValuesCache);
		this.fieldbookServiceImpl.setOntologyVariableDataManager(this.ontologyVariableDataManager);
		this.fieldbookServiceImpl.setContextUtil(this.contextUtil);
		this.fieldbookServiceImpl.setUserService(this.userService);

		final List<ValueReference> possibleValues = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			possibleValues.add(new ValueReference(i, "Name: " + i));
		}

		this.locationVariable = new MeasurementVariable();
		this.nonLocationVariable = new MeasurementVariable();

		this.locationVariable.setTermId(TermId.LOCATION_ID.getId());
		this.nonLocationVariable.setTermId(TermId.PI_ID.getId());
		this.nonLocationVariable.setPossibleValues(possibleValues);

		this.fieldbookServiceImpl.setContextUtil(this.contextUtil);
	}

	@Test
	public void testGetVariablePossibleValuesWhenVariableIsNonLocation() {
		final List<ValueReference> resultPossibleValues = this.fieldbookServiceImpl
				.getVariablePossibleValues(this.nonLocationVariable);
		Assert.assertEquals(
				"The results of get all possible values for the non-location should return a total of 5 records", 5,
				resultPossibleValues.size());
	}

	@Test
	public void testGetAllLocations() {
		final List<ValueReference> resultPossibleValues = this.fieldbookServiceImpl.getLocations(false);
		Assert.assertEquals("First possible value should have an id of 1 as per our test data", Integer.valueOf(1),
				resultPossibleValues.get(0).getId());
		Assert.assertEquals("Second possible value should have an id of 2 as per our test data", Integer.valueOf(2),
				resultPossibleValues.get(1).getId());
		Assert.assertEquals("There should only be 2 records as per our test data", 2, resultPossibleValues.size());
	}

	@Test
	public void testGetAllPossibleValuesWhenIdIsLocationAndGetAllRecordsIsFalse() {
		final Variable variable = VariableTestDataInitializer.createVariable(DataType.LOCATION);
		Mockito.when(this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(),
				this.locationVariable.getTermId(), true)).thenReturn(variable);

		final List<ValueReference> resultPossibleValues = this.fieldbookServiceImpl
				.getAllPossibleValues(this.locationVariable.getTermId(), false);
		Assert.assertEquals("First possible value should have an id of 1 as per our test data", Integer.valueOf(1),
				resultPossibleValues.get(0).getId());
		Assert.assertEquals("Second possible value should have an id of 2 as per our test data", Integer.valueOf(2),
				resultPossibleValues.get(1).getId());
		Assert.assertEquals("There should only be 2 records as per our test data", 2, resultPossibleValues.size());
	}

	@Test
	public void testGetAllPossibleValuesWhenIdIsLocationAndGetAllRecordsIsTrue() {
		final Variable variable = VariableTestDataInitializer.createVariable(DataType.LOCATION);
		Mockito.when(this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(),
				this.locationVariable.getTermId(), true)).thenReturn(variable);

		final List<ValueReference> resultPossibleValues = this.fieldbookServiceImpl
				.getAllPossibleValues(this.locationVariable.getTermId(), true);
		Assert.assertEquals("There should be no records as per our test data", 0, resultPossibleValues.size());
	}

	@Test
	public void testGetAllPossibleValuesWhenIdIsNonLocation() {
		final Variable variable = VariableTestDataInitializer.createVariable(DataType.CATEGORICAL_VARIABLE);
		this.possibleValuesCache.addPossibleValuesByDataType(DataType.CATEGORICAL_VARIABLE,
				this.nonLocationVariable.getPossibleValues());

		Mockito.when(this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(),
				this.nonLocationVariable.getTermId(), true)).thenReturn(variable);

		final List<ValueReference> resultPossibleValues = this.fieldbookServiceImpl
				.getAllPossibleValues(this.nonLocationVariable.getTermId(), false);
		Assert.assertEquals("There should be 1 record as per our test data", 5, resultPossibleValues.size());
		Assert.assertEquals("First possible value should have an id of 200 as per our test data", Integer.valueOf(0),
				resultPossibleValues.get(0).getId());
	}

	@Test
	public void testSaveStudyColumnOrderingIfStudyIdIsNull() throws MiddlewareException {
		final FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		final FieldbookService api = Mockito.mock(FieldbookService.class);
		fieldbookService.setFieldbookMiddlewareService(api);
		final Integer studyId = null;
		final String columnOrderDelimited = "";
		fieldbookService.saveStudyColumnOrdering(studyId, columnOrderDelimited,
			Mockito.mock(Workbook.class));
		Mockito.verify(api, Mockito.times(0))
			.saveStudyColumnOrdering(ArgumentMatchers.any(Integer.class), ArgumentMatchers.<List<Integer>>any());
	}

	@Test
	public void testSaveStudyColumnOrderingIfStudyIdIsNotNullAndColumnOrderListIsEmpty() throws MiddlewareException {
		final FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		final FieldbookService api = Mockito.mock(FieldbookService.class);
		fieldbookService.setFieldbookMiddlewareService(api);
		final Integer studyId = 7;
		final String columnOrderDelimited = "";
		final Workbook workbook = Mockito.mock(Workbook.class);
		fieldbookService.saveStudyColumnOrdering(studyId, columnOrderDelimited, workbook);
		Mockito.verify(api, Mockito.times(0))
			.saveStudyColumnOrdering(ArgumentMatchers.any(Integer.class), ArgumentMatchers.<List<Integer>>any());
		Mockito.verify(api, Mockito.times(1)).setOrderVariableByRank(workbook);
	}

	@Test
	public void testSaveStudyColumnOrderingIfStudyIdIsNotNullAndColumnOrderListIsNotEmpty() throws MiddlewareException {
		final FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		final FieldbookService api = Mockito.mock(FieldbookService.class);
		fieldbookService.setFieldbookMiddlewareService(api);
		final Integer studyId = 7;
		final String columnOrderDelimited = "[\"1100\", \"1900\"]";

		fieldbookService.saveStudyColumnOrdering(studyId, columnOrderDelimited,
			Mockito.mock(Workbook.class));
		Mockito.verify(api, Mockito.times(1))
			.saveStudyColumnOrdering(ArgumentMatchers.any(Integer.class), ArgumentMatchers.<List<Integer>>any());
	}

	@Test
	public void testGetBreedingMethodByCode() throws MiddlewareQueryException {
		final FieldbookServiceImpl fieldbookService = new FieldbookServiceImpl();
		final FieldbookService fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		fieldbookService.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		final ContextUtil contextUtil = Mockito.mock(ContextUtil.class);
		fieldbookService.setContextUtil(contextUtil);
		final String code = "AGB1";
		final Method method = this.createMethod();
		Mockito.doReturn(method).when(fieldbookMiddlewareService).getMethodByCode(code);
		final String actualValue = fieldbookService.getBreedingMethodByCode(code);
		final String expected = method.getMname() + " - " + method.getMcode();
		Assert.assertEquals("Expecting to return " + expected + " but returned " + actualValue, expected, actualValue);

	}

	private Method createMethod() {
		final Method method = new Method();
		method.setMname("Accession into genebank");
		method.setMcode("AGB1");
		return method;
	}

	@Test
	public void testAddMeasurementVariableToList() {

		final MeasurementVariable measurementVariableToAdd = new MeasurementVariable();
		measurementVariableToAdd.setTermId(TermId.OBS_UNIT_ID.getId());
		measurementVariableToAdd.setName(TermId.OBS_UNIT_ID.name());

		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		this.fieldbookServiceImpl.addMeasurementVariableToList(measurementVariableToAdd, measurementVariables);

		final MeasurementVariable obsUnitIdMeasurementVariabe = measurementVariables.get(0);

		Assert.assertNotNull(obsUnitIdMeasurementVariabe);
		Assert.assertEquals(TermId.OBS_UNIT_ID.getId(), obsUnitIdMeasurementVariabe.getTermId());
		Assert.assertEquals(TermId.OBS_UNIT_ID.name(), obsUnitIdMeasurementVariabe.getName());

	}

	@Test
	public void testaddSTUDY_UIDVariableToWorkbookConditionsFalse() {
		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(1, new StudyTypeDto("T"), "Sample Study", 1,
				false);
		// Set lists to empty lists for easier testing
		workbook.setConditions(new ArrayList<>());
		workbook.setFactors(new ArrayList<>());
		workbook.getObservations().get(0).setDataList(new ArrayList<>());

		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(ArgumentMatchers.eq(TermId.OBS_UNIT_ID.getId()),
			ArgumentMatchers.anyString()))
				.thenReturn(StandardVariableTestDataInitializer.createStandardVariable(TermId.OBS_UNIT_ID.getId(),
						TermId.OBS_UNIT_ID.name()));

		this.fieldbookServiceImpl.addStudyUUIDConditionAndObsUnitIDFactorToWorkbook(workbook, false);

		final MeasurementVariable obsUnitIdVariable = workbook.getFactors().get(0);
		Assert.assertEquals(TermId.OBS_UNIT_ID.getId(), obsUnitIdVariable.getTermId());
		Assert.assertEquals(TermId.OBS_UNIT_ID.name(), obsUnitIdVariable.getName());

		// Observation Unit id should not be added in the datalist
		Assert.assertTrue(workbook.getObservations().get(0).getDataList().isEmpty());
	}

	@Test
	public void testAddStudyUUIDConditionAndObsUnitIDFactorToWorkbookTrue() {
		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(1, new StudyTypeDto("T"), "Sample Study", 1,
				false);
		// Set lists to empty lists for easier testing
		workbook.setConditions(new ArrayList<>());
		workbook.setFactors(new ArrayList<>());
		workbook.getObservations().get(0).setDataList(new ArrayList<>());

		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(ArgumentMatchers.eq(TermId.OBS_UNIT_ID.getId()),
			ArgumentMatchers.anyString()))
				.thenReturn(StandardVariableTestDataInitializer.createStandardVariable(TermId.OBS_UNIT_ID.getId(),
						TermId.OBS_UNIT_ID.name()));

		this.fieldbookServiceImpl.addStudyUUIDConditionAndObsUnitIDFactorToWorkbook(workbook, true);

		final MeasurementVariable obsUnitIdVariable = workbook.getFactors().get(0);
		Assert.assertEquals(TermId.OBS_UNIT_ID.getId(), obsUnitIdVariable.getTermId());
		Assert.assertEquals(TermId.OBS_UNIT_ID.name(), obsUnitIdVariable.getName());

		// Observation Unit id should be added in the datalist
		Assert.assertFalse(workbook.getObservations().get(0).getDataList().isEmpty());
		final MeasurementData mData = workbook.getObservations().get(0).getDataList().get(0);
		Assert.assertEquals(TermId.OBS_UNIT_ID.name(), mData.getLabel());
		Assert.assertTrue(mData.getValue().isEmpty());
		Assert.assertEquals(obsUnitIdVariable, mData.getMeasurementVariable());
	}

	@Test
	public void testAddMeasurementVariableToMeasurementRows() {

		final MeasurementVariable measurementVariableToAdd = new MeasurementVariable();
		measurementVariableToAdd.setTermId(TermId.OBS_UNIT_ID.getId());
		measurementVariableToAdd.setName(TermId.OBS_UNIT_ID.name());

		final List<MeasurementRow> measurementRows = new ArrayList<>();
		final MeasurementRow measurementRow = new MeasurementRow();
		measurementRow.setDataList(new ArrayList<>());
		measurementRows.add(measurementRow);

		this.fieldbookServiceImpl.addMeasurementVariableToMeasurementRows(measurementVariableToAdd, measurementRows);

		final List<MeasurementData> measurementDataList = measurementRows.get(0).getDataList();
		final MeasurementData obsUnitIdMeasurementData = measurementDataList.get(0);

		Assert.assertNotNull(
				"Expecting that OBS_UNIT_ID measurementData is added in the measurementData list of the measurement",
				obsUnitIdMeasurementData);
		Assert.assertEquals(TermId.OBS_UNIT_ID.getId(), obsUnitIdMeasurementData.getMeasurementVariable().getTermId());
		Assert.assertEquals(TermId.OBS_UNIT_ID.name(), obsUnitIdMeasurementData.getLabel());

	}

	@Test
	public void testIsVariableExistsInList() {

		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final MeasurementVariable obsUnitIdMeasurementVariable = new MeasurementVariable();

		obsUnitIdMeasurementVariable.setName(TermId.OBS_UNIT_ID.name());
		obsUnitIdMeasurementVariable.setTermId(TermId.OBS_UNIT_ID.getId());

		measurementVariables.add(obsUnitIdMeasurementVariable);

		Assert.assertTrue("Expecting that OBS_UNIT_ID variable exists in the list",
				this.fieldbookServiceImpl.isVariableExistsInList(TermId.OBS_UNIT_ID.getId(), measurementVariables));
		Assert.assertFalse("Expecting that ENTRY_NO variable does not exist in the list",
				this.fieldbookServiceImpl.isVariableExistsInList(TermId.ENTRY_NO.getId(), measurementVariables));
	}

	@Test
	public void testgetDisplayNameWithLABBR() {
		final Location location = LocationTestDataInitializer.createLocationWithLabbr(1,
				FieldbookServiceTest.LOCATION_NAME, FieldbookServiceTest.LABBR);
		final String displayName = FieldbookServiceTest.LOCATION_NAME + " - (" + FieldbookServiceTest.LABBR + ")";
		final String result = this.fieldbookServiceImpl.getDisplayName(location);
		Assert.assertEquals("The result's value should be " + displayName, displayName, result);
	}

	@Test
	public void testgetDisplayNameWithoutLABBR() {
		final Location location = LocationTestDataInitializer.createLocation(1, FieldbookServiceTest.LOCATION_NAME);
		final String result = this.fieldbookServiceImpl.getDisplayName(location);
		Assert.assertEquals("The result's value should be " + FieldbookServiceTest.LOCATION_NAME,
				FieldbookServiceTest.LOCATION_NAME, result);
	}

	@Test
	public void testPairVariableAddOperation(){
		final UserSelection userSelection = new UserSelection();
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, new StudyTypeDto("N"));
		workbook.setConditions(this.getConditions(TermId.PI_ID.getId(), Operation.ADD));
		userSelection.setWorkbook(workbook);

		final StandardVariable piName = StandardVariableTestDataInitializer.createStandardVariable(TermId.PI_NAME.getId(), "PI_NAME");
		piName.setPhenotypicType(PhenotypicType.STUDY);

		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(
                TermId.PI_NAME.getId(), this.contextUtil.getCurrentProgramUUID())).thenReturn(piName);
		this.fieldbookServiceImpl.createIdNameVariablePairs(userSelection.getWorkbook(), new ArrayList<>(),
				AppConstants.ID_NAME_COMBINATION.getString(), true);

		Assert.assertEquals(2, userSelection.getWorkbook().getConditions().size());
		Assert.assertEquals(userSelection.getWorkbook().getConditions().get(0).getOperation(), userSelection.getWorkbook().getConditions().get(0).getOperation());
	}

	@Test
	public void testPairVariableUpdateOperation(){
		final UserSelection userSelection = new UserSelection();
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, new StudyTypeDto("N"));
		workbook.setConditions(this.getConditions(TermId.PI_ID.getId(), Operation.UPDATE));
		userSelection.setWorkbook(workbook);

		final StandardVariable piName = StandardVariableTestDataInitializer.createStandardVariable(TermId.PI_NAME.getId(), "PI_NAME");
		piName.setPhenotypicType(PhenotypicType.STUDY);

		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(
                TermId.PI_NAME.getId(), this.contextUtil.getCurrentProgramUUID())).thenReturn(piName);
		this.fieldbookServiceImpl.createIdNameVariablePairs(userSelection.getWorkbook(), new ArrayList<>(),
				AppConstants.ID_NAME_COMBINATION.getString(), true);

		Assert.assertEquals(2, userSelection.getWorkbook().getConditions().size());
		Assert.assertEquals(userSelection.getWorkbook().getConditions().get(0).getOperation(), userSelection.getWorkbook().getConditions().get(0).getOperation());
	}

	@Test
	public void testPairVariableDeleteOperation(){
		final UserSelection userSelection = new UserSelection();
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, new StudyTypeDto("N"));
		workbook.setConditions(this.getConditions(TermId.PI_ID.getId(), Operation.DELETE));
		userSelection.setWorkbook(workbook);

		final StandardVariable piName = StandardVariableTestDataInitializer.createStandardVariable(TermId.PI_NAME.getId(), "PI_NAME");
		piName.setPhenotypicType(PhenotypicType.STUDY);

		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(
                TermId.PI_NAME.getId(), this.contextUtil.getCurrentProgramUUID())).thenReturn(piName);
		this.fieldbookServiceImpl.createIdNameVariablePairs(userSelection.getWorkbook(), new ArrayList<>(),
				AppConstants.ID_NAME_COMBINATION.getString(), true);
		//Pair Name variable not added
		Assert.assertEquals(1, userSelection.getWorkbook().getConditions().size());
	}

	private MeasurementVariable getMeasurementVariableForCategoricalVariable(final int termId, final Operation operation) {
		final MeasurementVariable variable =
				new MeasurementVariable(termId, "PI_ID", "TRIAL NUMBER", WorkbookDataUtil.NUMBER,
						WorkbookDataUtil.ENUMERATED, WorkbookDataUtil.TRIAL_INSTANCE, WorkbookDataUtil.NUMERIC, "", WorkbookDataUtil.TRIAL);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		variable.setPossibleValues(this.getValueReferenceListWithKey());
		variable.setRole(PhenotypicType.STUDY);
		variable.setOperation(operation);
		return variable;
	}

	private List<ValueReference> getValueReferenceList() {
		final List<ValueReference> possibleValues = new ArrayList<>();

		for (int i = 0; i < 5; i++) {
			final ValueReference possibleValue = new ValueReference(i, String.valueOf(i));
			possibleValues.add(possibleValue);
		}
		return possibleValues;
	}

	private List<ValueReference> getValueReferenceListWithKey() {
		final List<ValueReference> possibleValues = new ArrayList<>();

		for (int i = 0; i < 5; i++) {
			final ValueReference possibleValue = new ValueReference(i, String.valueOf(i));
			possibleValue.setKey(RandomStringUtils.random(3));
			possibleValues.add(possibleValue);
		}
		return possibleValues;
	}

	private MeasurementVariable getMeasurementVariableForBreedingMethodVariable(final Operation operation) {
		final MeasurementVariable variable =
			new MeasurementVariable(TermId.BREEDING_METHOD_CODE.getId(), "BREEDING_METHOD_CODE", "BREEDING_METHOD_CODE", WorkbookDataUtil.NUMBER,
				WorkbookDataUtil.ENUMERATED, WorkbookDataUtil.TRIAL_INSTANCE, WorkbookDataUtil.NUMERIC, "", WorkbookDataUtil.TRIAL);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		variable.setPossibleValues(this.getValueReferenceList());
		variable.setRole(PhenotypicType.STUDY);
		variable.setOperation(operation);
		variable.setValue(variable.getPossibleValues().get(0).getKey());

		return variable;
	}

	private List<MeasurementVariable> getConditions(final int cvtermId, final Operation operation){
		final List<MeasurementVariable> conditionsList = new ArrayList<>();
		if(cvtermId == TermId.BREEDING_METHOD_CODE.getId()) {
			conditionsList.add(this.getMeasurementVariableForBreedingMethodVariable(operation));
		}else{
			conditionsList.add(this.getMeasurementVariableForCategoricalVariable(cvtermId, operation));
		}
		return  conditionsList;
	}

	@Test
	public void testPairVariableCodeAndNameAddOperation(){
		final UserSelection userSelection = new UserSelection();
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, new StudyTypeDto("N"));
		workbook.setConditions(this.getConditions(TermId.BREEDING_METHOD_CODE.getId(), Operation.ADD));
		userSelection.setWorkbook(workbook);

		final StandardVariable breedingMethodName = StandardVariableTestDataInitializer.createStandardVariable(TermId.BREEDING_METHOD.getId(), "BREEDING_METHOD");
		breedingMethodName.setPhenotypicType(PhenotypicType.STUDY);

		final Method breedingMethod = new Method(40, "DER", "G", "SLF", "Self and Bulk",
			"Selfing a Single Plant or population and bulk seed", 0, -1, 1, 0, 1490, 1, 0, 19980708);

		Mockito.when(this.fieldbookMiddlewareService.getMethodByCode(Mockito.anyString())).thenReturn(breedingMethod);

		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(
			TermId.BREEDING_METHOD.getId(), this.contextUtil.getCurrentProgramUUID())).thenReturn(breedingMethodName);

		this.fieldbookServiceImpl.createIdCodeNameVariablePairs(userSelection.getWorkbook(),
			AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());

		Assert.assertEquals(2, userSelection.getWorkbook().getConditions().size());
		for(final MeasurementVariable measurementVariable : userSelection.getWorkbook().getConditions()){
			Assert.assertTrue("TermId must include BREEDING_METHOD and BREEDING_METHOD_CODE ",measurementVariable.getTermId() == TermId.BREEDING_METHOD.getId() || measurementVariable.getTermId() == TermId.BREEDING_METHOD_CODE.getId());
			Assert.assertTrue("Operation is ADD", measurementVariable.getOperation().equals(Operation.ADD));
		}
	}

	@Test
	public void testPairVariableCodeAndNameUpdateOperation(){
		final UserSelection userSelection = new UserSelection();
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, new StudyTypeDto("N"));
		workbook.setConditions(this.getConditions(TermId.BREEDING_METHOD_CODE.getId(), Operation.UPDATE));
		userSelection.setWorkbook(workbook);

		final StandardVariable breedingMethodName = StandardVariableTestDataInitializer.createStandardVariable(TermId.BREEDING_METHOD.getId(), "BREEDING_METHOD");
		breedingMethodName.setPhenotypicType(PhenotypicType.STUDY);

		final Method breedingMethod = new Method(40, "DER", "G", "SLF", "Self and Bulk",
			"Selfing a Single Plant or population and bulk seed", 0, -1, 1, 0, 1490, 1, 0, 19980708);

		Mockito.when(this.fieldbookMiddlewareService.getMethodByCode(Mockito.anyString())).thenReturn(breedingMethod);

		this.fieldbookServiceImpl.createIdCodeNameVariablePairs(userSelection.getWorkbook(),
			AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());

		Assert.assertEquals(1, userSelection.getWorkbook().getConditions().size());
		for(final MeasurementVariable measurementVariable : userSelection.getWorkbook().getConditions()){
			Assert.assertTrue("TermId must include BREEDING_METHOD_CODE ",measurementVariable.getTermId() == TermId.BREEDING_METHOD_CODE.getId());
			Assert.assertTrue("Operation is UPDATE", measurementVariable.getOperation().equals(Operation.UPDATE));
		}
	}

	@Test
	public void testPairVariableCodeAndNameDeleteOperation(){
		final UserSelection userSelection = new UserSelection();
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, new StudyTypeDto("N"));
		workbook.setConditions(this.getConditions(TermId.BREEDING_METHOD_CODE.getId(), Operation.DELETE));
		userSelection.setWorkbook(workbook);

		final StandardVariable breedingMethodName = StandardVariableTestDataInitializer.createStandardVariable(TermId.BREEDING_METHOD.getId(), "BREEDING_METHOD");
		breedingMethodName.setPhenotypicType(PhenotypicType.STUDY);

		final Method breedingMethod = new Method(40, "DER", "G", "SLF", "Self and Bulk",
			"Selfing a Single Plant or population and bulk seed", 0, -1, 1, 0, 1490, 1, 0, 19980708);

		Mockito.when(this.fieldbookMiddlewareService.getMethodByCode(Mockito.anyString())).thenReturn(breedingMethod);

		this.fieldbookServiceImpl.createIdCodeNameVariablePairs(userSelection.getWorkbook(),
			AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());

		Assert.assertEquals(1, userSelection.getWorkbook().getConditions().size());
		System.out.println(userSelection.getWorkbook().getConditions());
		for(final MeasurementVariable measurementVariable : userSelection.getWorkbook().getConditions()){
			Assert.assertTrue("TermId must include BREEDING_METHOD_CODE ",measurementVariable.getTermId() == TermId.BREEDING_METHOD_CODE.getId());
			Assert.assertTrue("Operation is DELETE", measurementVariable.getOperation().equals(Operation.DELETE));

		}
	}
}
