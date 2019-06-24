package com.efficio.fieldbook.web.common.controller.derived_variables;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.data.initializer.SettingDetailTestDataInitializer;
import com.google.common.base.Optional;
import org.apache.commons.lang.math.RandomUtils;
import org.fest.util.Collections;
import org.generationcp.commons.derivedvariable.DerivedVariableProcessor;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.pojos.dms.Phenotype;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.generationcp.middleware.service.api.study.StudyService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DerivedVariableControllerTest {

	public static final int STUDY_ID = 111;
	public static final int VARIABLE1_TERMID = 123;
	public static final int VARIABLE2_TERMID = 456;
	public static final int VARIABLE3_TERMID = 789;
	public static final int VARIABLE4_TERMID = 999;
	public static final int VARIABLE5_TERMID = 20439; // MRFVInc_Cmp_pct
	public static final int DATE_TERM1 = 8630;
	public static final int DATE_TERM2 = 8830;
	public static final int TARGET_VARIABLE_TERMID = 321;

	private static final String INVALID_REQUEST = "invalid request";
	private static final String NOT_FOUND = "not found";
	private static final String ENGINE_EXCEPTION = "engine exception";
	private static final String MISSING_VARIABLES = "missing variables";
	private static final String DATE_PARSING_EXCEPTION = "date parsing exception";

	private static final String TERM_VALUE_1 = "1000";
	private static final String TERM_VALUE_2 = "12.5";
	private static final String TERM_VALUE_3 = "10";
	private static final String DATE_TERM1_VALUE = "20180101";
	private static final String DATE_TERM2_VALUE = "20180101";

	private static final String FORMULA = "({{" + VARIABLE1_TERMID + "}}/100)*((100-{{" + VARIABLE2_TERMID + "}})/(100-12.5))*(10/{{"
		+ VARIABLE3_TERMID + "}}) + fn:daysdiff({{" + DATE_TERM1 + "}},{{" + DATE_TERM2 + "}})";
	private static final String FORMULA_RESULT = "10";

	private static final Locale locale = Locale.getDefault();

	private FormulaDto formulaDTO;

	@Mock
	private UserSelection studySelection;

	@Mock
	private MessageSource messageSource;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private FormulaService formulaService;

	@Mock
	private StudyService studyService;

	@Mock
	private DerivedVariableProcessor processor;

	@InjectMocks
	private DerivedVariableController derivedVariableController;

	@Before
	public void init() {

		final Workbook workbook = new Workbook();
		workbook.setStudyDetails(new StudyDetails());
		workbook.getStudyDetails().setId(STUDY_ID);

		final List<MeasurementRow> observations = new ArrayList<>();
		final MeasurementRow measurementRow = this.createMeasurementRowTestData();
		observations.add(measurementRow);

		final MeasurementRow secondRow = this.createMeasurementRowTestData();
		final MeasurementData secondRowTarget = secondRow.getDataList().get(0);
		secondRowTarget.setValue(FORMULA_RESULT);
		secondRowTarget.setValueStatus(Phenotype.ValueStatus.OUT_OF_SYNC);
		observations.add(secondRow);

		final List<MeasurementRow> observationsCopy = new ArrayList<>(observations);

		workbook.setObservations(observations);
		workbook.setOriginalObservations(observations);

		when(this.studySelection.getBaselineTraitsList()).thenReturn(this.createSettingDetails());
		when(this.studySelection.getWorkbook()).thenReturn(workbook);

		when(this.messageSource.getMessage("study.execute.calculation.invalid.request", null, locale)).thenReturn(INVALID_REQUEST);
		when(this.messageSource.getMessage("study.execute.calculation.formula.not.found", null, locale)).thenReturn(NOT_FOUND);
		when(this.messageSource.getMessage("study.execute.calculation.engine.exception", null, locale)).thenReturn(ENGINE_EXCEPTION);
		when(this.messageSource.getMessage("study.execute.calculation.parsing.exception", null, locale)).thenReturn(DATE_PARSING_EXCEPTION);

		doAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(final InvocationOnMock invocationOnMock) throws Throwable {
				workbook.setObservations(observationsCopy);
				workbook.setOriginalObservations(observationsCopy);
				return true;
			}
		}).when(this.fieldbookMiddlewareService).loadAllObservations(workbook);

		final Optional<FormulaDto> formulaOptional = Mockito.mock(Optional.class);
		Mockito.when(formulaOptional.isPresent()).thenReturn(true);
		when(this.formulaService.getByTargetId(anyInt())).thenReturn(formulaOptional);

		this.formulaDTO = new FormulaDto();
		this.formulaDTO.setDefinition(FORMULA);
		final ArrayList<FormulaVariable> inputs = new ArrayList<>();
		inputs.add(new FormulaVariable(VARIABLE1_TERMID, String.valueOf(VARIABLE1_TERMID), TARGET_VARIABLE_TERMID));
		inputs.add(new FormulaVariable(VARIABLE2_TERMID, String.valueOf(VARIABLE2_TERMID), TARGET_VARIABLE_TERMID));
		inputs.add(new FormulaVariable(VARIABLE3_TERMID, String.valueOf(VARIABLE3_TERMID), TARGET_VARIABLE_TERMID));
		inputs.add(new FormulaVariable(DATE_TERM1, String.valueOf(DATE_TERM1), TARGET_VARIABLE_TERMID));
		inputs.add(new FormulaVariable(DATE_TERM2, String.valueOf(DATE_TERM2), TARGET_VARIABLE_TERMID));
		formulaDTO.setInputs(inputs);
		formulaDTO.setTarget(new FormulaVariable(Integer.valueOf(TARGET_VARIABLE_TERMID), "", null));
		//noinspection OptionalGetWithoutIsPresent
		Mockito.when(formulaOptional.get()).thenReturn(formulaDTO);
	}

	@Test
	public void testExecute() {
		final BindingResult bindingResult = Mockito.mock(BindingResult.class);
		final CalculateVariableRequest request = new CalculateVariableRequest();
		final List<Integer> locationIds = new ArrayList<>();
		locationIds.add(RandomUtils.nextInt());
		request.setGeoLocationIds(locationIds);
		request.setVariableId(RandomUtils.nextInt());

		final ResponseEntity<Map<String, Object>> response = this.derivedVariableController.execute(request, bindingResult);

		Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void testExecute_InvalidRequest() {
		final BindingResult bindingResult = Mockito.mock(BindingResult.class);
		final CalculateVariableRequest request = new CalculateVariableRequest();

		final ResponseEntity<Map<String, Object>> response = this.derivedVariableController.execute(request, bindingResult);

		Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		Assert.assertEquals(INVALID_REQUEST, response.getBody().get("errorMessage"));
	}

	@Test
	public void testExecute_FormulaNotFound() {
		final BindingResult bindingResult = Mockito.mock(BindingResult.class);
		final CalculateVariableRequest request = new CalculateVariableRequest();
		final List<Integer> locationIds = new ArrayList<>();
		locationIds.add(RandomUtils.nextInt());
		request.setGeoLocationIds(locationIds);
		request.setVariableId(RandomUtils.nextInt());

		when(this.formulaService.getByTargetId(anyInt())).thenReturn(Optional.<FormulaDto>absent());

		final ResponseEntity<Map<String, Object>> response = this.derivedVariableController.execute(request, bindingResult);

		Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		Assert.assertEquals(NOT_FOUND, response.getBody().get("errorMessage"));
	}

	@Test
	public void testExecute_Evaluate() {
		final BindingResult bindingResult = Mockito.mock(BindingResult.class);
		final CalculateVariableRequest request = new CalculateVariableRequest();
		final int locationId = RandomUtils.nextInt();
		final List<Integer> locationIds = new ArrayList<>();
		locationIds.add(locationId);
		request.setGeoLocationIds(locationIds);
		request.setVariableId(RandomUtils.nextInt());
		for (final MeasurementRow observation : this.studySelection.getWorkbook().getObservations()) {
			observation.setLocationId(locationId);
		}

		when(this.processor.evaluateFormula(anyString(), any(Map.class))).thenReturn(FORMULA_RESULT);

		final ResponseEntity<Map<String, Object>> response = this.derivedVariableController.execute(request, bindingResult);

		for (final MeasurementRow measurementRow : this.studySelection.getWorkbook().getObservations()) {
			final MeasurementData target = measurementRow.getMeasurementData(TARGET_VARIABLE_TERMID);
			Assert.assertEquals(FORMULA_RESULT, target.getValue());
			Assert.assertThat(target.getValueStatus(), nullValue());
		}

		Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
		Assert.assertFalse("Should not have missing data", response.getBody().containsKey("inputMissingData"));
	}

	@Test
	public void testExecute_EvaluateMissingData() {
		final BindingResult bindingResult = Mockito.mock(BindingResult.class);
		final CalculateVariableRequest request = new CalculateVariableRequest();
		final int locationId = RandomUtils.nextInt();
		final List<Integer> locationIds = new ArrayList<>();
		locationIds.add(locationId);
		request.setGeoLocationIds(locationIds);
		request.setVariableId(RandomUtils.nextInt());
		final List<MeasurementRow> observations = this.studySelection.getWorkbook().getObservations();
		final MeasurementData measurementData = observations.get(0).getMeasurementData(Integer.valueOf(VARIABLE1_TERMID));
		measurementData.setValue("");

		for (final MeasurementRow observation : this.studySelection.getWorkbook().getObservations()) {
			observation.setLocationId(locationId);
		}

		when(this.processor.evaluateFormula(anyString(), any(Map.class))).thenReturn(FORMULA_RESULT);

		final ResponseEntity<Map<String, Object>> response = this.derivedVariableController.execute(request, bindingResult);

		Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
		Assert.assertTrue(response.getBody().containsKey("inputMissingData"));
	}

	@Test
	public void testExecute_EvaluateMissingVariable() {
		final BindingResult bindingResult = Mockito.mock(BindingResult.class);
		final CalculateVariableRequest request = new CalculateVariableRequest();
		final int locationId = RandomUtils.nextInt();
		final List<Integer> locationIds = new ArrayList<>();
		locationIds.add(locationId);
		request.setGeoLocationIds(locationIds);
		request.setVariableId(RandomUtils.nextInt());

		for (final MeasurementRow observation : this.studySelection.getWorkbook().getObservations()) {
			observation.setLocationId(locationId);
		}

		when(this.studySelection.getBaselineTraitsList()).thenReturn(java.util.Collections.<SettingDetail>emptyList());
		when(this.messageSource.getMessage(
			Matchers.eq("study.execute.calculation.missing.variables"),
			Matchers.any(String[].class), Matchers.eq(locale))).thenReturn(MISSING_VARIABLES);

		final ResponseEntity<Map<String, Object>> response = this.derivedVariableController.execute(request, bindingResult);

		Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		Assert.assertEquals(MISSING_VARIABLES, response.getBody().get("errorMessage"));
	}

	@Test
	public void testExecute_EvaluateException() {
		final BindingResult bindingResult = Mockito.mock(BindingResult.class);
		final CalculateVariableRequest request = new CalculateVariableRequest();
		final int locationId = RandomUtils.nextInt();
		final List<Integer> locationIds = new ArrayList<>();
		locationIds.add(locationId);
		request.setGeoLocationIds(locationIds);
		request.setVariableId(RandomUtils.nextInt());
		for (final MeasurementRow observation : this.studySelection.getWorkbook().getObservations()) {
			observation.setLocationId(locationId);
		}

		when(this.processor.evaluateFormula(anyString(), any(Map.class))).thenThrow(new RuntimeException());

		final ResponseEntity<Map<String, Object>> response = this.derivedVariableController.execute(request, bindingResult);

		Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		Assert.assertEquals(ENGINE_EXCEPTION, response.getBody().get("errorMessage"));
	}

	@Test
	public void testExecute_DateParsingException() {
		final BindingResult bindingResult = Mockito.mock(BindingResult.class);
		final CalculateVariableRequest request = new CalculateVariableRequest();
		final int locationId = RandomUtils.nextInt();
		final List<Integer> locationIds = new ArrayList<>();
		locationIds.add(locationId);
		request.setGeoLocationIds(locationIds);
		request.setVariableId(RandomUtils.nextInt());
		for (final MeasurementRow observation : this.studySelection.getWorkbook().getObservations()) {
			observation.setLocationId(locationId);
			observation.getMeasurementData(DATE_TERM1).setValue("2018-03-27");
		}

		final ResponseEntity<Map<String, Object>> response = this.derivedVariableController.execute(request, bindingResult);

		Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		Assert.assertEquals(DATE_PARSING_EXCEPTION, response.getBody().get("errorMessage"));
	}

	@Test
	public void testDependencyVariableHasMeasurementData() {

		final List<Integer> idsToBeRemoved = Collections.list(VARIABLE3_TERMID);
		final Set<Integer> variableIdsOfTraitsInStudy = this.derivedVariableController.getAllPossibleInputVariables(1);

		when(this.formulaService.getAllFormulaVariables(variableIdsOfTraitsInStudy)).thenReturn(this.createFormulaVariables());
		when(this.studyService.hasMeasurementDataEntered(ArgumentMatchers.<List<Integer>>any(), Matchers.eq(STUDY_ID))).thenReturn(true);

		final ResponseEntity<Boolean> result = this.derivedVariableController.dependencyVariableHasMeasurementData(idsToBeRemoved);

		final ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

		verify(this.studyService).hasMeasurementDataEntered(captor.capture(), Matchers.eq(STUDY_ID));

		final List<Integer> derivedVariablesDependencies = captor.getValue();

		assertTrue(derivedVariablesDependencies.contains(VARIABLE3_TERMID));
		assertTrue(result.getBody());

	}

	@Test
	public void testDependencyVariableHasMeasurementDataDerivedVariableAndItsDepencyVariablesAreRemoved() {

		final List<Integer> idsToBeRemoved = Collections.list(VARIABLE3_TERMID, VARIABLE1_TERMID);
		final Set<Integer> variableIdsOfTraitsInStudy = this.derivedVariableController.getAllPossibleInputVariables(1);

		when(this.formulaService.getAllFormulaVariables(variableIdsOfTraitsInStudy)).thenReturn(this.createFormulaVariables());

		final ResponseEntity<Boolean> result = this.derivedVariableController.dependencyVariableHasMeasurementData(idsToBeRemoved);

		verify(this.studyService, Mockito.times(0)).hasMeasurementDataEntered(ArgumentMatchers.<List<Integer>>any(), anyInt());

		assertFalse(result.getBody());

	}

	@Test
	public void testGetVariableIdsOfTraitsInStudy() {

		final Set<Integer> result = this.derivedVariableController.getAllPossibleInputVariables(1);
		assertTrue(result.contains(VARIABLE1_TERMID));
		assertTrue(result.contains(VARIABLE2_TERMID));

	}

	private List<SettingDetail> createSettingDetails() {

		final List<SettingDetail> settingDetails = new ArrayList<SettingDetail>();

		settingDetails.add(SettingDetailTestDataInitializer.createSettingDetail(VARIABLE1_TERMID, "VARIABLE1", "", "TRIAL"));
		settingDetails.add(SettingDetailTestDataInitializer.createSettingDetail(VARIABLE2_TERMID, "VARIABLE2", "", "TRIAL"));
		settingDetails.add(SettingDetailTestDataInitializer.createSettingDetail(VARIABLE3_TERMID, "VARIABLE3", "", "TRIAL"));
		settingDetails.add(SettingDetailTestDataInitializer.createSettingDetail(VARIABLE4_TERMID, "VARIABLE4", "", "TRIAL"));
		settingDetails.add(SettingDetailTestDataInitializer.createSettingDetail(DATE_TERM1, "VARIABLE5", "", "TRIAL"));
		settingDetails.add(SettingDetailTestDataInitializer.createSettingDetail(DATE_TERM2, "VARIABLE6", "", "TRIAL"));

		return settingDetails;

	}

	private Set<FormulaVariable> createFormulaVariables() {

		final Set<FormulaVariable> formulaVariables = new HashSet<>();

		final FormulaVariable formulaVariable1 = new FormulaVariable();
		formulaVariable1.setId(VARIABLE3_TERMID);
		formulaVariable1.setName("VARIABLE3");
		formulaVariable1.setTargetTermId(VARIABLE1_TERMID);

		final FormulaVariable formulaVariable2 = new FormulaVariable();
		formulaVariable2.setId(VARIABLE4_TERMID);
		formulaVariable2.setName("VARIABLE4");
		formulaVariable2.setTargetTermId(VARIABLE2_TERMID);

		formulaVariables.add(formulaVariable1);
		formulaVariables.add(formulaVariable2);

		return formulaVariables;

	}

	private MeasurementRow createMeasurementRowTestData() {
		final MeasurementRow measurementRow = new MeasurementRow();
		measurementRow.setDataList(this.createMeasurementDataListTestData());
		return measurementRow;
	}

	private List<MeasurementData> createMeasurementDataListTestData() {
		final List<MeasurementData> measurementDataList = new ArrayList<>();
		measurementDataList.add(this.createMeasurementDataTestData(String.valueOf(TARGET_VARIABLE_TERMID), null, null));
		measurementDataList.add(this.createMeasurementDataTestData(String.valueOf(VARIABLE1_TERMID), TERM_VALUE_1, null));
		measurementDataList.add(this.createMeasurementDataTestData(String.valueOf(VARIABLE2_TERMID), TERM_VALUE_2, null));
		measurementDataList.add(this.createMeasurementDataTestData(String.valueOf(VARIABLE3_TERMID), TERM_VALUE_3, null));
		measurementDataList.add(this.createMeasurementDataTestData(String.valueOf(VARIABLE5_TERMID), "", ""));
		final MeasurementData dateData1 = this.createMeasurementDataTestData(String.valueOf(DATE_TERM1), DATE_TERM1_VALUE, null);
		dateData1.getMeasurementVariable().setDataTypeId(TermId.DATE_VARIABLE.getId());
		measurementDataList.add(dateData1);
		final MeasurementData dateData2 = this.createMeasurementDataTestData(String.valueOf(DATE_TERM2), DATE_TERM2_VALUE, null);
		dateData2.getMeasurementVariable().setDataTypeId(TermId.DATE_VARIABLE.getId());
		measurementDataList.add(dateData2);
		return measurementDataList;
	}

	private MeasurementData createMeasurementDataTestData(final String label, final String value, final String cValueId) {
		final MeasurementData measurementData = new MeasurementData();
		measurementData.setLabel(label);
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(Integer.valueOf(label));
		measurementVariable.setPossibleValues(new ArrayList<ValueReference>());
		measurementVariable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		measurementData.setMeasurementVariable(measurementVariable);
		measurementData.setValue(value);
		measurementData.setcValueId(cValueId);
		return measurementData;
	}

}
