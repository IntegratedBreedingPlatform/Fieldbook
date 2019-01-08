
package com.efficio.fieldbook.web.common.controller;

import static org.hamcrest.Matchers.hasSize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.MeasurementDataTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementRowTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.ProjectPropertyTestDataInitializer;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.dms.Phenotype;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.MeasurementVariableDto;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.StudyService;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.trial.service.ValidationService;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class TrialMeasurementsControllerTest {

	private static final String CROSS_VALUE = "ABC12/XYZ34";
	private static final String STOCK_ID_VALUE = "STCK-123";
	private static final String DATA = "data";
	private static final String RECORDS_FILTERED = "recordsFiltered";
	private static final String RECORDS_TOTAL = "recordsTotal";
	private static final String TRIAL_INSTANCE = "TRIAL_INSTANCE";
	private static final String DESIGNATION = "DESIGNATION";
	private static final String DRAW = "draw";
	private static final String SORT_ORDER = "sortOrder";
	private static final String SORT_BY = "sortBy";
	private static final String PAGE_SIZE = "pageSize";
	private static final String PAGE_NUMBER = "pageNumber";
	private static final String IS_CATEGORICAL_DESCRIPTION_VIEW = "isCategoricalDescriptionView";
	private static final String VALUE = "value";
	private static final String INDEX = "index";
	private static final String TERM_ID = "termId";
	private static final String IS_DISCARD = "isDiscard";
	private static final String EXPERIMENT_ID = "experimentId";
	private static final String CROSS = "CROSS";
	private static final String STOCK_ID = "StockID";
	private static final String ALEUCOL_1_5_TRAIT_NAME = "ALEUCOL_1_5";
	private static final int ALEUCOL_1_5_TERM_ID = 123;
	private static final String LOCAL = "-Local";

	@InjectMocks
	private TrialMeasurementsController measurementsController;
	private MeasurementDataTestDataInitializer measurementDataTestDataInitializer;

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Mock
	private StudyService studyService;

	@Mock
	private OntologyDataManager ontologyDataManager;
	
	@Mock
	private OntologyService ontologyService;

	@Mock
	private PaginationListSelection paginationListSelection;
	
	@Mock
	private ValidationService validationService;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private UserSelection userSelection;

	private List<MeasurementVariable> measurementVariables;

	private final MeasurementDto measurementText = new MeasurementDto(new MeasurementVariableDto(1, "NOTES"), 1,
			"Text Notes", null);
	private final MeasurementDto measurementNumeric = new MeasurementDto(new MeasurementVariableDto(2, "Grain Yield"),
			2, "500", null);
	private final MeasurementDto measurementCategorical = new MeasurementDto(
			new MeasurementVariableDto(3, "CategoricalTrait"), 3, "CategoryValue1", null);

	private final TermId[] standardFactors = { TermId.GID, TermId.ENTRY_NO, TermId.ENTRY_TYPE, TermId.ENTRY_CODE,
			TermId.PLOT_NO, TermId.OBS_UNIT_ID, TermId.BLOCK_NO, TermId.REP_NO, TermId.ROW, TermId.COL,
			TermId.FIELDMAP_COLUMN, TermId.FIELDMAP_RANGE };

	@Before
	public void setUp() {
		this.measurementDataTestDataInitializer = new MeasurementDataTestDataInitializer();
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_NO.getId()))
				.thenReturn(new Term(TermId.ENTRY_NO.getId(), TermId.ENTRY_NO.name(), "Definition"));
	}

	@Test
	public void testCopyMeasurementValue() {

		final MeasurementRow origRow = new MeasurementRow();
		origRow.setDataList(this.generateTestDataList());
		final MeasurementRow valueRow = new MeasurementRow();
		valueRow.setDataList(this.generateTestDataList());

		this.measurementsController.copyMeasurementValue(origRow, valueRow);

		for (int x = 0; x < origRow.getDataList().size(); x++) {
			if (!origRow.getDataList().get(x).getMeasurementVariable().isFactor()) {
				MatcherAssert.assertThat(
						"The origRow's measurement value must be equal to the valueRow's measurement value if the variable is not a factor",
						origRow.getDataList().get(x).getValue(),
						Is.is(CoreMatchers.equalTo(valueRow.getDataList().get(x).getValue())));
			} else {
				MatcherAssert.assertThat(
						"The origRow's measurement value must not equal to the valueRow's measurement value if the variable is a factor",
						origRow.getDataList().get(x).getValue(),
						CoreMatchers.not(CoreMatchers.equalTo(valueRow.getDataList().get(x).getValue())));
			}

		}

	}

	@Test
	public void testCopyMeasurementValueNullEmptyPossibleValues() {

		final MeasurementRow origRow = new MeasurementRow();
		origRow.setDataList(this.generateTestDataList());
		final MeasurementRow valueRow = new MeasurementRow();
		valueRow.setDataList(this.generateTestDataList());

		final MeasurementData nullData = new MeasurementData();
		nullData.setcValueId(null);
		nullData.setDataType(null);
		nullData.setEditable(false);
		nullData.setLabel(null);
		nullData.setPhenotypeId(null);
		nullData.setValue(null);

		final MeasurementVariable measurementVariable = new MeasurementVariable();
		final List<ValueReference> possibleValues = new ArrayList<>();
		measurementVariable.setPossibleValues(possibleValues);
		nullData.setMeasurementVariable(measurementVariable);

		origRow.getDataList().add(nullData);
		valueRow.getDataList().add(nullData);

		this.measurementsController.copyMeasurementValue(origRow, valueRow);

		for (int x = 0; x < origRow.getDataList().size(); x++) {
			if (!origRow.getDataList().get(x).getMeasurementVariable().isFactor()) {
				MatcherAssert.assertThat(
						"The origRow's measurement value must be equal to the valueRow's measurement value if the variable is not a factor",
						origRow.getDataList().get(x).getValue(),
						Is.is(CoreMatchers.equalTo(valueRow.getDataList().get(x).getValue())));
			} else {
				MatcherAssert.assertThat(
						"The origRow's measurement value must not equal to the valueRow's measurement value if the variable is a factor",
						origRow.getDataList().get(x).getValue(),
						CoreMatchers.not(CoreMatchers.equalTo(valueRow.getDataList().get(x).getValue())));
			}

		}

	}

	@Test
	public void testCopyMeasurementValueNullNullPossibleValuesAndValueIsNotEmpty() {

		final MeasurementRow origRow = new MeasurementRow();
		origRow.setDataList(this.generateTestDataList());
		final MeasurementRow valueRow = new MeasurementRow();
		valueRow.setDataList(this.generateTestDataList());

		final MeasurementData data = new MeasurementData();
		data.setcValueId("1234");
		data.setDataType(null);
		data.setEditable(false);
		data.setLabel(null);
		data.setPhenotypeId(null);
		data.setValue(null);

		final MeasurementData data2 = new MeasurementData();
		data2.setcValueId(null);
		data2.setDataType(null);
		data2.setEditable(false);
		data2.setLabel(null);
		data2.setPhenotypeId(null);
		data2.setValue("jjasd");

		final MeasurementVariable measurementVariable = new MeasurementVariable();
		final List<ValueReference> possibleValues = new ArrayList<>();
		possibleValues.add(new ValueReference());
		measurementVariable.setPossibleValues(possibleValues);
		data.setMeasurementVariable(measurementVariable);

		origRow.getDataList().add(data);
		valueRow.getDataList().add(data2);

		this.measurementsController.copyMeasurementValue(origRow, valueRow);

		for (int x = 0; x < origRow.getDataList().size(); x++) {
			if (!origRow.getDataList().get(x).getMeasurementVariable().isFactor()) {
				MatcherAssert.assertThat(
						"The origRow's measurement value must be equal to the valueRow's measurement value if the variable is not a factor",
						origRow.getDataList().get(x).getValue(),
						Is.is(CoreMatchers.equalTo(valueRow.getDataList().get(x).getValue())));
			} else {
				MatcherAssert.assertThat(
						"The origRow's measurement value must not equal to the valueRow's measurement value if the variable is a factor",
						origRow.getDataList().get(x).getValue(),
						CoreMatchers.not(CoreMatchers.equalTo(valueRow.getDataList().get(x).getValue())));
			}

		}

	}

	@Test
	public void testCopyMeasurementValueWithCustomCategoricalValue() {

		final MeasurementRow origRow = new MeasurementRow();
		origRow.setDataList(this.generateTestDataList());

		final List<ValueReference> possibleValues = new ArrayList<>();
		possibleValues.add(new ValueReference());
		possibleValues.add(new ValueReference());
		possibleValues.get(0).setId(1);
		possibleValues.get(0).setKey("1");
		possibleValues.get(1).setId(2);
		possibleValues.get(1).setKey(origRow.getDataList().get(0).getValue());

		origRow.getDataList().get(0).getMeasurementVariable().setPossibleValues(possibleValues);

		final MeasurementRow valueRow = new MeasurementRow();
		valueRow.setDataList(this.generateTestDataList());
		valueRow.getDataList().get(0).setAccepted(true);

		this.measurementsController.copyMeasurementValue(origRow, valueRow, true);
		MatcherAssert.assertThat(origRow.getDataList().get(0).getIsCustomCategoricalValue(), Is.is(true));

	}

	private List<MeasurementData> generateTestDataList() {

		final List<MeasurementData> dataList = new ArrayList<>();

		for (int x = 0; x < 10; x++) {
			final MeasurementData data = new MeasurementData();
			data.setcValueId(UUID.randomUUID().toString());
			data.setDataType(UUID.randomUUID().toString());
			data.setEditable(true);
			data.setLabel(UUID.randomUUID().toString());
			data.setPhenotypeId(x);
			data.setValue(UUID.randomUUID().toString());
			data.setMeasurementVariable(new MeasurementVariable());
			dataList.add(data);
		}

		final MeasurementData nullData = new MeasurementData();
		nullData.setcValueId(null);
		nullData.setDataType(null);
		nullData.setEditable(false);
		nullData.setLabel(null);
		nullData.setPhenotypeId(null);
		nullData.setValue(null);

		final MeasurementVariable measurementVariable = new MeasurementVariable();
		final List<ValueReference> possibleValues = new ArrayList<>();
		possibleValues.add(new ValueReference());
		measurementVariable.setPossibleValues(possibleValues);
		nullData.setMeasurementVariable(measurementVariable);
		dataList.add(nullData);

		final MeasurementData emptyData = new MeasurementData();
		emptyData.setcValueId("");
		emptyData.setDataType("");
		emptyData.setEditable(false);
		emptyData.setLabel("");
		emptyData.setPhenotypeId(0);
		emptyData.setValue("");
		emptyData.setMeasurementVariable(measurementVariable);
		dataList.add(emptyData);

		final MeasurementData measurementDataOfAFactor = new MeasurementData();
		final MeasurementVariable measurementVariableOfAFactor = new MeasurementVariable();
		measurementVariableOfAFactor.setFactor(true);
		measurementDataOfAFactor.setcValueId(UUID.randomUUID().toString());
		measurementDataOfAFactor.setDataType(UUID.randomUUID().toString());
		measurementDataOfAFactor.setEditable(false);
		measurementDataOfAFactor.setLabel(UUID.randomUUID().toString());
		measurementDataOfAFactor.setPhenotypeId(0);
		measurementDataOfAFactor.setValue(UUID.randomUUID().toString());
		measurementDataOfAFactor.setMeasurementVariable(measurementVariableOfAFactor);
		dataList.add(measurementDataOfAFactor);

		return dataList;
	}

	@Test
	public void testEditExperimentCells() throws MiddlewareQueryException {
		final int termId = 2000;
		final int experimentId = 1;
		final ExtendedModelMap model = new ExtendedModelMap();
		final UserSelection userSelection = new UserSelection();
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));

		final Variable variableText = new Variable();
		final Scale scaleText = new Scale();
		scaleText.setDataType(DataType.CHARACTER_VARIABLE);
		variableText.setScale(scaleText);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Matchers.anyString(), Matchers.eq(termId),
				Matchers.eq(true))).thenReturn(variableText);
		this.measurementsController.setUserSelection(userSelection);
		this.measurementsController.editExperimentCells(experimentId, termId, null, model);
		MatcherAssert.assertThat(TermId.CATEGORICAL_VARIABLE.getId(),
				Is.is(CoreMatchers.equalTo(model.get("categoricalVarId"))));
		MatcherAssert.assertThat(TermId.DATE_VARIABLE.getId(), Is.is(CoreMatchers.equalTo(model.get("dateVarId"))));
		MatcherAssert.assertThat(TermId.NUMERIC_VARIABLE.getId(),
				Is.is(CoreMatchers.equalTo(model.get("numericVarId"))));
		MatcherAssert.assertThat(variableText, Is.is(CoreMatchers.equalTo(model.get("variable"))));
		MatcherAssert.assertThat(experimentId,
				Is.is(CoreMatchers.equalTo(model.get(TrialMeasurementsControllerTest.EXPERIMENT_ID))));
		MatcherAssert.assertThat((List<?>) model.get("possibleValues"), hasSize(0));
		MatcherAssert.assertThat("", Is.is(CoreMatchers.equalTo(model.get("phenotypeId"))));
		MatcherAssert.assertThat("", Is.is(CoreMatchers.equalTo(model.get("phenotypeValue"))));
	}

	@Test
	public void testEditExperimentCellsImportPreview() throws MiddlewareQueryException {
		final int termId = 2000;
		final int experimentId = 1;
		final ExtendedModelMap model = new ExtendedModelMap();
		final UserSelection userSelection = new UserSelection();
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));

		final Variable variableText = new Variable();
		final Scale scaleText = new Scale();
		scaleText.setDataType(DataType.CHARACTER_VARIABLE);
		variableText.setScale(scaleText);

		final List<MeasurementRow> measurementRowList = new ArrayList<>();
		MeasurementRow row = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createMeasurementData(1000, "TestVarName1", "1st",
				TermId.CHARACTER_VARIABLE));
		row.setDataList(dataList);
		measurementRowList.add(row);
		row = new MeasurementRow();
		dataList = new ArrayList<>();
		final String phenotpevalue = "2nd";
		dataList.add(this.measurementDataTestDataInitializer.createCategoricalMeasurementData(termId, "TestVarName2",
				phenotpevalue, new ArrayList<ValueReference>()));
		row.setDataList(dataList);
		measurementRowList.add(row);

		userSelection.setMeasurementRowList(measurementRowList);

		this.measurementsController.setUserSelection(userSelection);
		this.measurementsController.editExperimentCells(experimentId, termId, model);
		MatcherAssert.assertThat(TermId.CATEGORICAL_VARIABLE.getId(),
				Is.is(CoreMatchers.equalTo(model.get("categoricalVarId"))));
		MatcherAssert.assertThat(TermId.DATE_VARIABLE.getId(), Is.is(CoreMatchers.equalTo(model.get("dateVarId"))));
		MatcherAssert.assertThat(TermId.NUMERIC_VARIABLE.getId(),
				Is.is(CoreMatchers.equalTo(model.get("numericVarId"))));
		MatcherAssert.assertThat((List<?>) model.get("possibleValues"), hasSize(0));
		MatcherAssert.assertThat(0, Is.is(CoreMatchers.equalTo(model.get("phenotypeId"))));
		MatcherAssert.assertThat(phenotpevalue, Is.is(CoreMatchers.equalTo(model.get("phenotypeValue"))));
	}

	@Test
	public void testUpdateExperimentCellDataIfNotDiscard() {
		final int termId = 2000;
		final String newValue = "new value";
		final UserSelection userSelection = new UserSelection();
		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(1234);
		workbook.setStudyDetails(studyDetails);
		workbook.setVariates(new ArrayList<MeasurementVariable>());
		userSelection.setWorkbook(workbook);
		this.measurementsController.setUserSelection(userSelection);

		final ValidationService mockValidationService = Mockito.mock(ValidationService.class);
		Mockito.when(mockValidationService.validateObservationValue(Matchers.any(Variable.class), Matchers.anyString()))
				.thenReturn(true);
		this.measurementsController.setValidationService(mockValidationService);

		final Variable variableText = new Variable();
		final Scale scaleText = new Scale();
		scaleText.setDataType(DataType.CHARACTER_VARIABLE);
		variableText.setScale(scaleText);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Matchers.anyString(), Matchers.eq(termId),
				Matchers.eq(true))).thenReturn(variableText);

		final Map<String, String> data = new HashMap<String, String>();
		data.put(TrialMeasurementsControllerTest.EXPERIMENT_ID, "1");
		data.put(TrialMeasurementsControllerTest.TERM_ID, Integer.toString(termId));
		data.put(TrialMeasurementsControllerTest.VALUE, newValue);

		final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameter(TrialMeasurementsControllerTest.IS_DISCARD)).thenReturn("0");

		final Map<String, Object> results = this.measurementsController.updateExperimentCellData(data, req);

		MatcherAssert.assertThat("1", Is.is(CoreMatchers.equalTo(results.get(TrialMeasurementsController.SUCCESS))));
		MatcherAssert.assertThat(results.containsKey(TrialMeasurementsController.DATA), Is.is(true));

		// Validation and saving of phenotype must occur when isDiscard flag is
		// off.
		Mockito.verify(mockValidationService).validateObservationValue(variableText, newValue);
		Mockito.verify(this.studyDataManager).saveOrUpdatePhenotypeValue(Matchers.anyInt(), Matchers.anyInt(),
				Matchers.anyString(), Matchers.any(Phenotype.class), Matchers.anyInt(), Matchers.any(Phenotype.ValueStatus.class));

	}

	@Test
	public void testUpdateExperimentCellDataIfNotDiscardInvalidButKeep() {
		final int termId = 2000;
		final String newValue = "new value";
		final UserSelection userSelection = new UserSelection();
		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(1234);
		workbook.setStudyDetails(studyDetails);
		workbook.setVariates(new ArrayList<MeasurementVariable>());
		userSelection.setWorkbook(workbook);
		this.measurementsController.setUserSelection(userSelection);

		final ValidationService mockValidationService = Mockito.mock(ValidationService.class);
		Mockito.when(mockValidationService.validateObservationValue(Matchers.any(Variable.class), Matchers.anyString()))
				.thenReturn(true);
		this.measurementsController.setValidationService(mockValidationService);

		final Variable variableText = new Variable();
		final Scale scaleText = new Scale();
		scaleText.setDataType(DataType.CHARACTER_VARIABLE);
		variableText.setScale(scaleText);
		Mockito.when(this.ontologyVariableDataManager.getVariable(ArgumentMatchers.<String>isNull(), Matchers.eq(termId),
				Matchers.eq(true))).thenReturn(variableText);

		final Map<String, String> data = new HashMap<String, String>();
		data.put(TrialMeasurementsControllerTest.EXPERIMENT_ID, "1");
		data.put(TrialMeasurementsControllerTest.TERM_ID, Integer.toString(termId));
		data.put(TrialMeasurementsControllerTest.VALUE, newValue);

		final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameter(TrialMeasurementsControllerTest.IS_DISCARD)).thenReturn("0");
		Mockito.when(req.getParameter("invalidButKeep")).thenReturn("1");

		final Map<String, Object> results = this.measurementsController.updateExperimentCellData(data, req);

		MatcherAssert.assertThat("1", Is.is(CoreMatchers.equalTo(results.get(TrialMeasurementsController.SUCCESS))));
		MatcherAssert.assertThat(results.containsKey(TrialMeasurementsController.DATA), Is.is(true));

		// Validation step should not be invoked when there is a signal to keep
		// the value even if it is invalid.
		Mockito.verify(mockValidationService, Mockito.never()).validateObservationValue(variableText, newValue);
		// But save step must be invoked.
		Mockito.verify(this.studyDataManager).saveOrUpdatePhenotypeValue(Matchers.anyInt(), Matchers.anyInt(),
				Matchers.anyString(), ArgumentMatchers.<Phenotype>isNull(), Matchers.anyInt(), ArgumentMatchers.<Phenotype.ValueStatus>isNull());
	}

	@Test
	public void testUpdateExperimentCellDataIfDiscard() {
		final int termId = 2000;
		final String newValue = "new value";
		final UserSelection userSelection = new UserSelection();

		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(1234);
		workbook.setStudyDetails(studyDetails);
		userSelection.setWorkbook(workbook);
		this.measurementsController.setUserSelection(userSelection);

		final ValidationService mockValidationService = Mockito.mock(ValidationService.class);
		Mockito.when(mockValidationService.validateObservationValue(Matchers.any(Variable.class), Matchers.anyString()))
				.thenReturn(true);

		this.measurementsController.setValidationService(mockValidationService);

		final Variable variableText = new Variable();
		final Scale scaleText = new Scale();
		scaleText.setDataType(DataType.CHARACTER_VARIABLE);
		variableText.setScale(scaleText);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Matchers.anyString(), Matchers.eq(termId),
				Matchers.eq(true))).thenReturn(variableText);
		final Map<String, String> data = new HashMap<String, String>();
		data.put(TrialMeasurementsControllerTest.EXPERIMENT_ID, "1");
		data.put(TrialMeasurementsControllerTest.TERM_ID, Integer.toString(termId));
		data.put(TrialMeasurementsControllerTest.VALUE, newValue);

		final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameter(TrialMeasurementsControllerTest.IS_DISCARD)).thenReturn("1");

		final Map<String, Object> results = this.measurementsController.updateExperimentCellData(data, req);

		MatcherAssert.assertThat("1", Is.is(CoreMatchers.equalTo(results.get(TrialMeasurementsController.SUCCESS))));
		MatcherAssert.assertThat(results.containsKey(TrialMeasurementsController.DATA), Is.is(true));

		// Validation and saving of phenotype must NOT occur when isDiscard flag
		// is on.
		Mockito.verify(mockValidationService, Mockito.never()).validateObservationValue(variableText, newValue);
		Mockito.verify(this.studyDataManager, Mockito.never()).saveOrUpdatePhenotypeValue(Matchers.anyInt(),
				Matchers.anyInt(), Matchers.anyString(), Matchers.any(Phenotype.class), Matchers.anyInt(), Matchers.any(Phenotype.ValueStatus.class));
	}

	@Test
	public void testMarkExperimentCellDataAsAccepted() {
		final int termId = 2000;
		final UserSelection userSelection = new UserSelection();
		final List<MeasurementRow> measurementRowList = new ArrayList<>();
		MeasurementRow row = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createMeasurementData(1000, "TestVarName1", "1st",
				TermId.CHARACTER_VARIABLE));

		row.setDataList(dataList);
		measurementRowList.add(row);
		row = new MeasurementRow();
		dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createCategoricalMeasurementData(termId, "TestVarName2",
				"2nd", new ArrayList<ValueReference>()));
		row.setDataList(dataList);
		measurementRowList.add(row);
		userSelection.setMeasurementRowList(measurementRowList);
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));
		this.measurementsController.setUserSelection(userSelection);
		this.measurementsController.setValidationService(Mockito.mock(ValidationService.class));
		final Map<String, String> data = new HashMap<>();

		data.put(TrialMeasurementsControllerTest.INDEX, "1");
		data.put(TrialMeasurementsControllerTest.TERM_ID, Integer.toString(termId));

		final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);

		final Map<String, Object> results = this.measurementsController.markExperimentCellDataAsAccepted(data,
				req);

		@SuppressWarnings("unchecked")
		final Map<String, Object> dataMap = (Map<String, Object>) results.get(TrialMeasurementsControllerTest.DATA);

		MatcherAssert.assertThat("The Accepted flag should be true",
				(boolean) ((Object[]) dataMap.get("TestVarName2"))[2], Is.is(true));

	}

	@Test
	public void testMarkExperimentCellDataAsAcceptedForNumeric() {
		final int termId = 2000;
		final UserSelection userSelection = new UserSelection();
		final List<MeasurementRow> measurementRowList = new ArrayList<>();
		MeasurementRow row = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createMeasurementData(1000, "TestVarName1", "1st",
				TermId.CHARACTER_VARIABLE));
		row.setDataList(dataList);
		measurementRowList.add(row);
		row = new MeasurementRow();
		dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createMeasurementData(termId, "TestVarName2", "1",
				TermId.NUMERIC_VARIABLE));
		row.setDataList(dataList);
		measurementRowList.add(row);
		userSelection.setMeasurementRowList(measurementRowList);
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));
		this.measurementsController.setUserSelection(userSelection);
		this.measurementsController.setValidationService(Mockito.mock(ValidationService.class));
		final Map<String, String> data = new HashMap<>();

		data.put(TrialMeasurementsControllerTest.INDEX, "1");
		data.put(TrialMeasurementsControllerTest.TERM_ID, Integer.toString(termId));

		final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);

		final Map<String, Object> results = this.measurementsController.markExperimentCellDataAsAccepted(data,
				req);

		@SuppressWarnings("unchecked")
		final Map<String, Object> dataMap = (Map<String, Object>) results.get(TrialMeasurementsControllerTest.DATA);

		MatcherAssert.assertThat("The Accepted flag should be true",
				(boolean) ((Object[]) dataMap.get("TestVarName2"))[1], Is.is(true));

	}

	@Test
	public void testMarkAllExperimentDataAsAccepted() {
		final int termId = 2000;
		final UserSelection userSelection = new UserSelection();
		final List<MeasurementRow> measurementRowList = new ArrayList<>();
		MeasurementRow row = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createMeasurementData(1000, "TestVarName1", "1st",
				TermId.CHARACTER_VARIABLE));
		row.setDataList(dataList);
		measurementRowList.add(row);
		row = new MeasurementRow();
		dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createCategoricalMeasurementData(termId, "TestVarName2",
				"2nd", new ArrayList<ValueReference>()));
		row.setDataList(dataList);
		measurementRowList.add(row);
		dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createCategoricalMeasurementData(termId, "TestVarName3",
				"3rd", new ArrayList<ValueReference>()));
		row.setDataList(dataList);
		measurementRowList.add(row);

		userSelection.setMeasurementRowList(measurementRowList);
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));

		this.measurementsController.setUserSelection(userSelection);
		this.measurementsController.markAllExperimentDataAsAccepted();

		for (final MeasurementRow measurementRow : userSelection.getMeasurementRowList()) {
			if (measurementRow != null && measurementRow.getMeasurementVariables() != null) {
				for (final MeasurementData var : measurementRow.getDataList()) {
					if (var != null && !StringUtils.isEmpty(var.getValue())
							&& (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()
									|| !var.getMeasurementVariable().getPossibleValues().isEmpty())) {
						Assert.assertTrue(var.isAccepted());
						Assert.assertTrue(var.getIsCustomCategoricalValue());
					} else {
						Assert.assertFalse(var.isAccepted());
						Assert.assertFalse(var.getIsCustomCategoricalValue());
					}
				}
			}
		}

	}

	@Test
	public void testMarkAllExperimentDataAsMissing() {
		final int termId = 2000;
		final UserSelection userSelection = new UserSelection();
		final List<MeasurementRow> measurementRowList = new ArrayList<>();
		MeasurementRow row = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createMeasurementData(1000, "TestVarName1", "1st",
				TermId.CHARACTER_VARIABLE));
		row.setDataList(dataList);
		measurementRowList.add(row);
		row = new MeasurementRow();
		dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createCategoricalMeasurementData(termId, "TestVarName2",
				"2nd", new ArrayList<ValueReference>()));
		row.setDataList(dataList);
		measurementRowList.add(row);
		dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createCategoricalMeasurementData(termId, "TestVarName3",
				"3rd", new ArrayList<ValueReference>()));

		row.setDataList(dataList);
		measurementRowList.add(row);

		userSelection.setMeasurementRowList(measurementRowList);
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));

		this.measurementsController.setUserSelection(userSelection);
		this.measurementsController.markAllExperimentDataAsMissing();

		for (final MeasurementRow measurementRow : userSelection.getMeasurementRowList()) {
			if (measurementRow != null && measurementRow.getMeasurementVariables() != null) {
				for (final MeasurementData var : measurementRow.getDataList()) {
					if (var != null) {
						if (var != null && !StringUtils.isEmpty(var.getValue())
								&& (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()
										|| !var.getMeasurementVariable().getPossibleValues().isEmpty())) {
							MatcherAssert.assertThat(var.isAccepted(), Is.is(true));
							if (this.measurementsController.isCategoricalValueOutOfBounds(var.getcValueId(),
									var.getValue(), var.getMeasurementVariable().getPossibleValues())) {
								MatcherAssert.assertThat(MeasurementData.MISSING_VALUE,
										Is.is(CoreMatchers.equalTo(var.getValue())));
							} else {
								MatcherAssert.assertThat("0",
										Is.is(CoreMatchers.not(CoreMatchers.equalTo(var.getValue()))));
							}
						} else {
							MatcherAssert.assertThat(true, Is.is(CoreMatchers.not(var.isAccepted())));
						}
					}
				}
			}
		}

	}

	@Test
	public void testIsCategoricalValueOutOfBounds() {
		final List<ValueReference> possibleValues = new ArrayList<>();
		possibleValues.add(new ValueReference());
		possibleValues.add(new ValueReference());
		possibleValues.get(0).setId(1);
		possibleValues.get(0).setKey("1");
		possibleValues.get(1).setId(2);
		possibleValues.get(1).setKey("2");

		MatcherAssert.assertThat("2 is in possible values so the return value should be false", true, Is.is(CoreMatchers
				.not(this.measurementsController.isCategoricalValueOutOfBounds("2", "", possibleValues))));
		MatcherAssert.assertThat("3 is NOT in possible values so the return value should be true",
				this.measurementsController.isCategoricalValueOutOfBounds("3", "", possibleValues), Is.is(true));
		MatcherAssert.assertThat("2 is in possible values so the return value should be false", true, Is.is(CoreMatchers
				.not(this.measurementsController.isCategoricalValueOutOfBounds(null, "2", possibleValues))));
		MatcherAssert.assertThat("3 is NOT in possible values so the return value should be true",
				this.measurementsController.isCategoricalValueOutOfBounds(null, "3", possibleValues), Is.is(true));
	}

	@Test
	public void testIsNumericalValueOutOfBoundsWhenThereIsRange() {
		final MeasurementVariable var = new MeasurementVariable();
		var.setMinRange(Double.valueOf("1"));
		var.setMaxRange(Double.valueOf("10"));
		MatcherAssert.assertThat("Should return false since 2 is not out of range", true,
				Is.is(CoreMatchers.not(this.measurementsController.isNumericalValueOutOfBounds("2", var))));
		MatcherAssert.assertThat("Should return true since 21 is out of range",
				this.measurementsController.isNumericalValueOutOfBounds("21", var));
	}

	@Test
	public void testIsNumericalValueOutOfBoundsWhenThereIsNoRange() {
		final MeasurementVariable var = new MeasurementVariable();

		MatcherAssert.assertThat("Should return false since 2 is not out of range", true,
				Is.is(CoreMatchers.not(this.measurementsController.isNumericalValueOutOfBounds("2", var))));
		MatcherAssert.assertThat("Should return false since 21 is not out of range", true,
				Is.is(CoreMatchers.not(this.measurementsController.isNumericalValueOutOfBounds("21", var))));
	}

	@Test
	public void testSetCategoricalDisplayType() throws Exception {
		// default case, api call does not include a value for
		// showCategoricalDescriptionView, since the
		// initial value for the isCategoricalDescriptionView is FALSE, the
		// session value will be toggled
		final HttpSession session = Mockito.mock(HttpSession.class);
		Mockito.when(session.getAttribute(TrialMeasurementsControllerTest.IS_CATEGORICAL_DESCRIPTION_VIEW))
				.thenReturn(Boolean.FALSE);

		final Boolean result = this.measurementsController.setCategoricalDisplayType(null, session);
		Mockito.verify(session, Mockito.times(1))
				.setAttribute(TrialMeasurementsControllerTest.IS_CATEGORICAL_DESCRIPTION_VIEW, Boolean.TRUE);
		MatcherAssert.assertThat("should be true", result);
	}

	@Test
	public void testSetCategoricalDisplayTypeWithForcedCategoricalDisplayValue() throws Exception {
		// Api call includes a value for showCategoricalDescriptionView, we set
		// the session to this value then
		// return this
		final HttpSession session = Mockito.mock(HttpSession.class);
		Mockito.when(session.getAttribute(TrialMeasurementsControllerTest.IS_CATEGORICAL_DESCRIPTION_VIEW))
				.thenReturn(Boolean.FALSE);

		final Boolean result = this.measurementsController.setCategoricalDisplayType(Boolean.FALSE, session);
		Mockito.verify(session, Mockito.times(1))
				.setAttribute(TrialMeasurementsControllerTest.IS_CATEGORICAL_DESCRIPTION_VIEW, Boolean.FALSE);
		MatcherAssert.assertThat("should be false", true, Is.is(CoreMatchers.not(result)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetPlotMeasurementsPaginated() {
		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter(TrialMeasurementsControllerTest.PAGE_NUMBER, "1");
		request.addParameter(TrialMeasurementsControllerTest.PAGE_SIZE, "10");
		request.addParameter(TrialMeasurementsControllerTest.SORT_BY, String.valueOf(TermId.ENTRY_NO.getId()));
		request.addParameter(TrialMeasurementsControllerTest.SORT_ORDER, "desc");

		final String drawParamValue = "drawParamValue";
		request.addParameter(TrialMeasurementsControllerTest.DRAW, drawParamValue);

		final boolean useDifferentLocalNames = false;
		this.setupMeasurementVariablesInMockWorkbook(useDifferentLocalNames);

		final int recordsCount = 1;
		final TermSummary category1 = new TermSummary(111, this.measurementCategorical.getVariableValue(),
				"CategoryValue1Definition");
		// Add CROSS and STOCK measurements
		final boolean doAddNewGermplasmDescriptors = true;
		final List<ObservationDto> observations = this.setupTestObservations(recordsCount, category1,
				doAddNewGermplasmDescriptors);

		this.measurementsController.setContextUtil(Mockito.mock(ContextUtil.class));

		// Method to test
		final Map<String, Object> plotMeasurementsPaginated = this.measurementsController.getPlotMeasurementsPaginated(1, 1,
				new CreateTrialForm(), Mockito.mock(Model.class), request);


		// Expecting 4 keys returned by main map: draw, recordsTotal,
		// recordsFiltered, data
		MatcherAssert.assertThat("Expected a non-null map as return value.", plotMeasurementsPaginated,
				Is.is(CoreMatchers.not(CoreMatchers.nullValue())));

		MatcherAssert.assertThat("Expected number of entries in the map did not match.", 4,
				Is.is(CoreMatchers.equalTo(plotMeasurementsPaginated.size())));

		MatcherAssert.assertThat("'draw' parameter should be returned in map as per value of request parameter 'draw'.",
				drawParamValue,
				Is.is(CoreMatchers.equalTo(plotMeasurementsPaginated.get(TrialMeasurementsControllerTest.DRAW))));
		MatcherAssert.assertThat(
				"Record count should be returned as per what is returned by studyService.countTotalObservationUnits()",
				recordsCount, Is.is(CoreMatchers
						.equalTo(plotMeasurementsPaginated.get(TrialMeasurementsControllerTest.RECORDS_TOTAL))));
		MatcherAssert.assertThat("Records filtered should be returned as per number of plots on page.",
				observations.size(), Is.is(CoreMatchers
						.equalTo(plotMeasurementsPaginated.get(TrialMeasurementsControllerTest.RECORDS_FILTERED))));
		final List<Map<String, Object>> allMeasurementData = (List<Map<String, Object>>) plotMeasurementsPaginated
				.get(TrialMeasurementsControllerTest.DATA);
		MatcherAssert.assertThat("Expected a non-null data map.", allMeasurementData,
				Is.is(CoreMatchers.not(CoreMatchers.nullValue())));

		final Map<String, Object> onePlotMeasurementData = allMeasurementData.get(0);
		final ObservationDto observationDto = observations.get(0);

		// Verify the factor names and values were included properly in data map
		MatcherAssert.assertThat(String.valueOf(observationDto.getMeasurementId()),
				Is.is(CoreMatchers.equalTo(onePlotMeasurementData.get(TrialMeasurementsControllerTest.EXPERIMENT_ID))));
		final boolean isGidDesigFactorsIncluded = true;
		this.verifyCorrectValuesForFactors(onePlotMeasurementData, observationDto, isGidDesigFactorsIncluded,
				doAddNewGermplasmDescriptors, useDifferentLocalNames);

		this.verifyCorrectValuesForTraits(category1, onePlotMeasurementData);
		final ArgumentCaptor<Integer> pageNumberArg = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<Integer> pageSizeArg = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<String> sortByArg = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<String> sortOrderArg = ArgumentCaptor.forClass(String.class);

		// Verify pagination-related arguments passed to studyService
		Mockito.verify(this.studyService).getObservations(Matchers.anyInt(), Matchers.anyInt(), pageNumberArg.capture(),
				pageSizeArg.capture(), sortByArg.capture(), sortOrderArg.capture());
		MatcherAssert.assertThat(new Integer(1), Is.is(CoreMatchers.equalTo(pageNumberArg.getValue())));
		MatcherAssert.assertThat(new Integer(10), Is.is(CoreMatchers.equalTo(pageSizeArg.getValue())));
		MatcherAssert.assertThat(TermId.ENTRY_NO.name(), Is.is(CoreMatchers.equalTo(sortByArg.getValue())));
		MatcherAssert.assertThat("desc", Is.is(CoreMatchers.equalTo(sortOrderArg.getValue())));
	}

	private void verifyCorrectValuesForFactors(final Map<String, Object> onePlotMeasurementData,
			final ObservationDto observationDto, final boolean isGidDesigFactorsIncluded,
			final boolean isNewGermplasmDescriptorsAdded, final boolean useDifferentLocalNames) {
		// there are tests where GID and DESIGNATION variable headers are not
		// expected to be present
		if (isGidDesigFactorsIncluded) {
			final String designationMapKey = useDifferentLocalNames
					? TrialMeasurementsControllerTest.DESIGNATION + TrialMeasurementsControllerTest.LOCAL
					: TrialMeasurementsControllerTest.DESIGNATION;
			MatcherAssert.assertThat(observationDto.getDesignation(),
					Is.is(CoreMatchers.equalTo(onePlotMeasurementData.get(designationMapKey))));
			final String gidMapKey = useDifferentLocalNames ? TermId.GID.name() + TrialMeasurementsControllerTest.LOCAL
					: TermId.GID.name();
			MatcherAssert.assertThat(observationDto.getGid(),
					Is.is(CoreMatchers.equalTo(onePlotMeasurementData.get(gidMapKey))));
		}

		final String entryNoMapKey = useDifferentLocalNames
				? TermId.ENTRY_NO.name() + TrialMeasurementsControllerTest.LOCAL : TermId.ENTRY_NO.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] { observationDto.getEntryNo(), false },
				(Object[]) onePlotMeasurementData.get(entryNoMapKey)), Is.is(true));

		final String entryCodeMapKey = useDifferentLocalNames
				? TermId.ENTRY_CODE.name() + TrialMeasurementsControllerTest.LOCAL : TermId.ENTRY_CODE.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] { observationDto.getEntryCode(), false },
				(Object[]) onePlotMeasurementData.get(entryCodeMapKey)), Is.is(true));

		if (isNewGermplasmDescriptorsAdded) {
			MatcherAssert.assertThat(
					Arrays.equals(new Object[] { TrialMeasurementsControllerTest.STOCK_ID_VALUE },
							(Object[]) onePlotMeasurementData.get(TrialMeasurementsControllerTest.STOCK_ID)),
					Is.is(true));
			MatcherAssert.assertThat(Arrays.equals(new Object[] { TrialMeasurementsControllerTest.CROSS_VALUE },
					(Object[]) onePlotMeasurementData.get(TrialMeasurementsControllerTest.CROSS)), Is.is(true));
		}

		final String entryTypeMapKey = useDifferentLocalNames
				? TermId.ENTRY_TYPE.name() + TrialMeasurementsControllerTest.LOCAL : TermId.ENTRY_TYPE.name();
		MatcherAssert.assertThat(
				Arrays.equals(new Object[] { observationDto.getEntryType(), observationDto.getEntryType(), false },
						(Object[]) onePlotMeasurementData.get(entryTypeMapKey)),
				Is.is(true));

		final String plotNoMapKey = useDifferentLocalNames
				? TermId.PLOT_NO.name() + TrialMeasurementsControllerTest.LOCAL : TermId.PLOT_NO.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] { observationDto.getPlotNumber(), false },
				(Object[]) onePlotMeasurementData.get(plotNoMapKey)), Is.is(true));

		final String blockNoMapKey = useDifferentLocalNames
				? TermId.BLOCK_NO.name() + TrialMeasurementsControllerTest.LOCAL : TermId.BLOCK_NO.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] { observationDto.getBlockNumber(), false },
				(Object[]) onePlotMeasurementData.get(blockNoMapKey)), Is.is(true));

		final String repNoMapKey = useDifferentLocalNames ? TermId.REP_NO.name() + TrialMeasurementsControllerTest.LOCAL
				: TermId.REP_NO.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] { observationDto.getRepitionNumber(), false },
				(Object[]) onePlotMeasurementData.get(repNoMapKey)), Is.is(true));

		final String trialInstanceMapKey = useDifferentLocalNames
				? TrialMeasurementsControllerTest.TRIAL_INSTANCE + TrialMeasurementsControllerTest.LOCAL
				: TrialMeasurementsControllerTest.TRIAL_INSTANCE;
		MatcherAssert.assertThat(Arrays.equals(new Object[] { observationDto.getTrialInstance(), false },
				(Object[]) onePlotMeasurementData.get(trialInstanceMapKey)), Is.is(true));

		final String rowMapKey = useDifferentLocalNames ? TermId.ROW.name() + TrialMeasurementsControllerTest.LOCAL
				: TermId.ROW.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] { observationDto.getRowNumber(), false },
				(Object[]) onePlotMeasurementData.get(rowMapKey)), Is.is(true));

		final String colMapKey = useDifferentLocalNames ? TermId.COL.name() + TrialMeasurementsControllerTest.LOCAL
				: TermId.COL.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] { observationDto.getColumnNumber(), false },
				(Object[]) onePlotMeasurementData.get(colMapKey)), Is.is(true));

		final String obsUnitIdMapKey = useDifferentLocalNames
				? TermId.OBS_UNIT_ID.name() + TrialMeasurementsControllerTest.LOCAL : TermId.OBS_UNIT_ID.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] { observationDto.getObsUnitId(), false },
				(Object[]) onePlotMeasurementData.get(obsUnitIdMapKey)), Is.is(true));

		final String fieldMapColumnMapKey = useDifferentLocalNames
				? TermId.FIELDMAP_COLUMN.name() + TrialMeasurementsControllerTest.LOCAL : TermId.FIELDMAP_COLUMN.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] { observationDto.getFieldMapColumn(), false },
				(Object[]) onePlotMeasurementData.get(fieldMapColumnMapKey)), Is.is(true));

		final String fieldMapRangeMapKey = useDifferentLocalNames
				? TermId.FIELDMAP_RANGE.name() + TrialMeasurementsControllerTest.LOCAL : TermId.FIELDMAP_COLUMN.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] { observationDto.getFieldMapRange(), false },
				(Object[]) onePlotMeasurementData.get(fieldMapRangeMapKey)), Is.is(true));
	}

	private List<ObservationDto> setupTestObservations(final int recordsCount, final TermSummary category1,
			final boolean doAddNewGermplasmDescriptors) {
		final List<MeasurementDto> measurements = Lists.newArrayList(this.measurementText, this.measurementNumeric,
				this.measurementCategorical);
		final ObservationDto testObservationDto = new ObservationDto(123, "1", "Test Entry", 300, "CML123", "5",
				"Entry Code", "2", "10", "3", measurements);

		if (doAddNewGermplasmDescriptors) {
			testObservationDto.additionalGermplasmDescriptor(TrialMeasurementsControllerTest.STOCK_ID,
					TrialMeasurementsControllerTest.STOCK_ID_VALUE);
			testObservationDto.additionalGermplasmDescriptor(TrialMeasurementsControllerTest.CROSS,
					TrialMeasurementsControllerTest.CROSS_VALUE);
		}

		testObservationDto.setRowNumber("11");
		testObservationDto.setColumnNumber("22");
		testObservationDto.setObsUnitId("9CVRPNHaSlCE1");

		final List<ObservationDto> observations = Lists.newArrayList(testObservationDto);
		Mockito.when(this.studyService.getObservations(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt(),
				Matchers.anyInt(), Matchers.anyString(), Matchers.anyString())).thenReturn(observations);

		Mockito.when(this.studyService.countTotalObservationUnits(Matchers.anyInt(), Matchers.anyInt()))
				.thenReturn(recordsCount);
		this.measurementsController.setStudyService(this.studyService);

		final Variable variableText = new Variable();
		final Scale scaleText = new Scale();
		scaleText.setDataType(DataType.CHARACTER_VARIABLE);
		variableText.setScale(scaleText);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Matchers.anyString(),
				Matchers.eq(this.measurementText.getMeasurementVariable().getId()), Matchers.eq(true))).thenReturn(variableText);

		final Variable variableNumeric = new Variable();
		final Scale scaleNumeric = new Scale();
		scaleNumeric.setDataType(DataType.NUMERIC_VARIABLE);
		variableNumeric.setScale(scaleNumeric);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Matchers.anyString(),
				Matchers.eq(this.measurementNumeric.getMeasurementVariable().getId()), Matchers.eq(true)
				)).thenReturn(variableNumeric);

		final Variable variableCategorical = new Variable();
		final Scale scaleCategorical = new Scale();
		scaleCategorical.setDataType(DataType.CATEGORICAL_VARIABLE);
		scaleCategorical.addCategory(category1);
		variableCategorical.setScale(scaleCategorical);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Matchers.anyString(),
				Matchers.eq(this.measurementCategorical.getMeasurementVariable().getId()), Matchers.eq(true)
				)).thenReturn(variableCategorical);
		return observations;
	}

	private void setupMeasurementVariablesInMockWorkbook(final boolean useDifferentLocalName) {
		final UserSelection userSelection = new UserSelection();
		final Workbook workbook = Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class);
		userSelection.setWorkbook(workbook);

		this.measurementVariables = new ArrayList<>();
		final String trait1Name = this.measurementText.getMeasurementVariable().getName();
		this.measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(
				this.measurementText.getMeasurementVariable().getId(),
				useDifferentLocalName ? trait1Name + TrialMeasurementsControllerTest.LOCAL : trait1Name, null));
		final String trait2Name = this.measurementNumeric.getMeasurementVariable().getName();
		this.measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(
				this.measurementNumeric.getMeasurementVariable().getId(),
				useDifferentLocalName ? trait2Name + TrialMeasurementsControllerTest.LOCAL : trait2Name, null));
		final String trait3Name = this.measurementCategorical.getMeasurementVariable().getName();
		this.measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(
				this.measurementCategorical.getMeasurementVariable().getId(),
				useDifferentLocalName ? trait3Name + TrialMeasurementsControllerTest.LOCAL : trait3Name, null));

		this.measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(
				TrialMeasurementsControllerTest.ALEUCOL_1_5_TERM_ID,
				TrialMeasurementsControllerTest.ALEUCOL_1_5_TRAIT_NAME, null));

		this.measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(
				this.measurementCategorical.getMeasurementVariable().getId(),
				useDifferentLocalName ? trait3Name + TrialMeasurementsControllerTest.LOCAL : trait3Name, null));

		for (final TermId term : this.standardFactors) {
			this.measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(term.getId(),
					useDifferentLocalName ? term.name() + TrialMeasurementsControllerTest.LOCAL : term.name(), null));
		}
		this.measurementVariables
				.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.DESIG.getId(),
						useDifferentLocalName
								? TrialMeasurementsControllerTest.DESIGNATION + TrialMeasurementsControllerTest.LOCAL
								: TrialMeasurementsControllerTest.DESIGNATION,
						null));
		this.measurementVariables.add(
				MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(),
						useDifferentLocalName
								? TrialMeasurementsControllerTest.TRIAL_INSTANCE + TrialMeasurementsControllerTest.LOCAL
								: TrialMeasurementsControllerTest.TRIAL_INSTANCE,
						null));

		Mockito.when(workbook.getMeasurementDatasetVariablesView()).thenReturn(this.measurementVariables);
		this.measurementsController.setUserSelection(userSelection);
	}

	@Test
	public void testUpdateTraits() throws WorkbookParserException {
		final UserSelection userSelection = new UserSelection();
		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook();
		userSelection.setWorkbook(workbook);
		this.measurementsController.setUserSelection(userSelection );
		final CreateTrialForm form = new CreateTrialForm();

		final Map<String, String> resultMap = this.measurementsController.updateTraits(form);

		Assert.assertEquals("1", resultMap.get(TrialMeasurementsController.STATUS));
		Mockito.verify(this.validationService).validateObservationValues(workbook);
		Mockito.verify(this.fieldbookMiddlewareService).saveMeasurementRows(workbook,
				this.contextUtil.getCurrentProgramUUID(), true);
	}

	@Test
	public void testCreateNameToAliasMap() {
		final Workbook workbook = Mockito.mock(Workbook.class);
		Mockito.when(this.userSelection.getWorkbook()).thenReturn(workbook);
		Mockito.when(workbook.getMeasurementDatasetVariablesView())
				.thenReturn(Arrays.asList(MeasurementVariableTestDataInitializer
						.createMeasurementVariable(TermId.PLOT_CODE.getId(), TermId.PLOT_CODE.name(), "1-1")));
		Mockito.when(this.fieldbookMiddlewareService.getMeasurementDatasetId(Matchers.anyInt(), Matchers.anyString()))
				.thenReturn(1);
		final String alias = "PlotCode";
		Mockito.when(this.ontologyDataManager.getProjectPropertiesByProjectId(Matchers.anyInt())).thenReturn(Arrays
				.asList(ProjectPropertyTestDataInitializer.createProjectProperty(alias, TermId.PLOT_CODE.getId())));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.PLOT_CODE.getId()))
				.thenReturn(new Term(TermId.PLOT_CODE.getId(), TermId.PLOT_CODE.name(), TermId.PLOT_CODE.name()));

		final Map<String, String> nameToAliasMap = this.measurementsController.createNameToAliasMap(1);
		Assert.assertEquals(1, nameToAliasMap.size());
		Assert.assertTrue(nameToAliasMap.keySet().contains(TermId.PLOT_CODE.name()));
		Assert.assertEquals(alias, nameToAliasMap.get(TermId.PLOT_CODE.name()));
	}

	@Test
	public void testRoundNumericValues() {
		final MeasurementVariable measurementVariable = MeasurementVariableTestDataInitializer.createMeasurementVariable();
		measurementVariable.setVariableType(VariableType.TRAIT);
		measurementVariable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());

		// Value: 1.99999 - round up
		List<MeasurementRow> measurementRows = MeasurementRowTestDataInitializer.createMeasurementRowList(1, "numeric", "1.99999", measurementVariable);
		this.measurementsController.roundNumericValues(measurementRows);
		Assert.assertEquals("2", measurementRows.get(0).getDataList().get(0).getValue());

		// Value: 1.44444 - round down
		measurementRows = MeasurementRowTestDataInitializer.createMeasurementRowList(1, "numeric", "1.44444", measurementVariable);
		this.measurementsController.roundNumericValues(measurementRows);
		Assert.assertEquals("1.4444", measurementRows.get(0).getDataList().get(0).getValue());

		// Value: 1.44445 - round up
		measurementRows = MeasurementRowTestDataInitializer.createMeasurementRowList(1, "numeric", "1.44445", measurementVariable);
		this.measurementsController.roundNumericValues(measurementRows);
		Assert.assertEquals("1.4445", measurementRows.get(0).getDataList().get(0).getValue());

		// Value: 2 - no rounding needed
		measurementRows = MeasurementRowTestDataInitializer.createMeasurementRowList(1, "numeric", "2", measurementVariable);
		this.measurementsController.roundNumericValues(measurementRows);
		Assert.assertEquals("2", measurementRows.get(0).getDataList().get(0).getValue());

		// Value: 0.4 - no rounding needed
		measurementRows = MeasurementRowTestDataInitializer.createMeasurementRowList(1, "numeric", "0.4", measurementVariable);
		this.measurementsController.roundNumericValues(measurementRows);
		Assert.assertEquals("0.4", measurementRows.get(0).getDataList().get(0).getValue());

		// Value: missing - no rounding needed
		measurementRows = MeasurementRowTestDataInitializer.createMeasurementRowList(1, "numeric", TrialMeasurementsController.MISSING_VALUE, measurementVariable);
		this.measurementsController.roundNumericValues(measurementRows);
		Assert.assertEquals(TrialMeasurementsController.MISSING_VALUE, measurementRows.get(0).getDataList().get(0).getValue());
	}

	@Test
	public void testViewStudyAjax() {
		final CreateTrialForm form = new CreateTrialForm();
		final Model model = Mockito.mock(Model.class);
		Mockito.when(this.fieldbookMiddlewareService.getCompleteDataset(1)).thenReturn(WorkbookTestDataInitializer.getTestWorkbook());
		this.measurementsController.setPaginationListSelection(this.paginationListSelection);
		this.measurementsController.viewStudyAjax(form, model, 1, 1);
		Assert.assertNotNull(form.getMeasurementRowList());
		Assert.assertNotNull(form.getMeasurementVariables());
		Mockito.verify(this.fieldbookMiddlewareService).getCompleteDataset(1);
		Mockito.verify(this.fieldbookService).setAllPossibleValuesInWorkbook(Matchers.any(Workbook.class));
		Mockito.verify(this.paginationListSelection).addReviewDetailsList(String.valueOf(1), form.getMeasurementRowList());
		Mockito.verify(this.paginationListSelection).addReviewVariableList(String.valueOf(1), form.getMeasurementVariables());
	}
	
	@Test
	public void testChangeLocationIdToName() {
        final BiMap<String, String> locationNameMap = HashBiMap.create();
        locationNameMap.put("9015", "INT WATER MANAGEMENT INSTITUTE");
        Mockito.when(this.studyDataManager.createInstanceLocationIdToNameMapFromStudy(1)).thenReturn(locationNameMap);
        final MeasurementVariable locationVariable = MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.LOCATION_ID.getId(), "9015");
		final List<MeasurementRow> measurementRowList = MeasurementRowTestDataInitializer.createMeasurementRowList(TermId.LOCATION_ID.getId(), TermId.LOCATION_ID.name(), "9015", locationVariable);
		final Map<String, MeasurementVariable> measurementDatasetVariablesMap = new HashMap<>();
		measurementDatasetVariablesMap.put(String.valueOf(TermId.LOCATION_ID.getId()), locationVariable);
		this.measurementsController.changeLocationIdToName(measurementRowList, measurementDatasetVariablesMap, 1);
		final MeasurementData data = measurementRowList.get(0).getDataList().get(0);
		Assert.assertEquals("INT WATER MANAGEMENT INSTITUTE", data.getValue());
	}

	private void verifyCorrectValuesForTraits(final TermSummary category1, final Map<String, Object> dataMap) {
		// Character Trait
		MatcherAssert
				.assertThat(
						Arrays.equals(
								new Object[] { this.measurementText.getVariableValue(),
										this.measurementText.getPhenotypeId(), this.measurementText.getValueStatus() },
								(Object[]) dataMap.get(this.measurementText.getMeasurementVariable().getName())),
						Is.is(true));

		// Numeric Trait
		MatcherAssert.assertThat(
				Arrays.equals(
						new Object[] { this.measurementNumeric.getVariableValue(), true,
								this.measurementNumeric.getPhenotypeId(), this.measurementNumeric.getValueStatus() },
						(Object[]) dataMap.get(this.measurementNumeric.getMeasurementVariable().getName())),
				Is.is(true));

		// Categorical Trait
		MatcherAssert.assertThat(
				Arrays.equals(
						new Object[] { category1.getName(), category1.getDefinition(), true,
								this.measurementCategorical.getPhenotypeId(), this.measurementCategorical.getValueStatus() },
						(Object[]) dataMap.get(this.measurementCategorical.getMeasurementVariable().getName())),
				Is.is(true));
	}
}
