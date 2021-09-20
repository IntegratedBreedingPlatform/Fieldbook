package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.data.initializer.MeasurementTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class KsuFieldbookUtilTest {

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private OntologyService ontologyService;

	// TERM_ID
	private static final int HGRAMINC_E_PCT_ID = 18180;
	private static final int ACDTOL_E_1TO_5_ID = 18180;
	private static final int ANT_DATE_YMD_ID = 18180;
	private static final int R_SECA_SEV_E_00_TO_99_TEXT_ID = 18180;

	// NAME
	private static final String HGRAMINC_E_PCT_NAME = "HgramInc_E_pct";
	private static final String ACDTOL_E_1TO_5_NAME = "AcdTol _E_1to5";
	private static final String ANT_DATE_YMD_NAME = "Ant_date_ymd";
	private static final String R_SECA_SEV_E_00_TO_99_TEXT_NAME = "RSecaSev_E_00to99text";

	// DESCRIPTION
	private static final String HGRAMINC_E_PCT_DESCRIPTION = "Barley Helminthosporium leaf stripe incidence-BY-HgramInc Estimation-IN-%";
	private static final String ACDTOL_E_1TO_5_DESCRIPTION = "Acid soil tolerance-BY-AcdTol Estimation-IN-1-5 TOL scale";
	private static final String ANT_DATE_YMD_DESCRIPTION = "Anthesis time-BY-Ant DS65 date Estimation-IN-yyyymmdd";
	private static final String R_SECA_SEV_E_00_TO_99_TEXT_DESCRIPTION = "Barley scald severity-BY-Double digit Estimation-IN-Double digit (00-99) + text";

	// SCALES
	private static final String PERCENTAGE_SCALE = "%";
	private static final String SCALE_1_5 = "1-5 TOL scale";
	private static final String YYYYMMDD_SCALE = "yyyymmdd";
	private static final String R_SECA_SEV_E_00_TO_99_TEXT_SCALE = "Double digit (00-99) + text";

	// METHODS
	private static final String HGRAMINC_E_PCT_METHOD = "HgramInc Estimation";
	private static final String ACDTOL_E_1TO_5_METHOD = "AcdTol Estimation";
	private static final String ANT_DATE_YMD_METHOD = "Ant DS65 date Estimation";
	private static final String R_SECA_SEV_E_00_TO_99_TEXT_METHOD = "Double digit Estimation";

	// PROPERTIES
	private static final String HGRAMINC_E_PCT_PROPERTY = "Barley Helminthosporium leaf stripe incidence";
	private static final String ACDTOL_E_1TO_5_PROPERTY = "Acid soil tolerance";
	private static final String ANT_DATE_YMD_PROPERTY = "Anthesis time";
	private static final String R_SECA_SEV_E_00_TO_99_TEXT_PROPERTY = "Barley scald severity";

	// DATA TYPES
	private static final String NUMBER = "numeric";
	private static final String CATEGORICAL = "categorical";
	private static final String DATE = "date";
	private static final String CHARACTER = "Character";
	private static final String TEXT = "text";

	// LABELS
	private static final String STUDY = "STUDY";
	private static final String PLOT = "PLOT";

	private static final String NUMERIC = "N";

	private static final String NUMERIC_VALUE = "1";
	private static final String DATE_VALUE = "20171010";

	@Test
	public void testIsValidHeaderNamesReturnsTrueIfAllRequiredColumnsArePresent() {
		final String[] headerNames = { "PLOT_NO", "ENTRY_NO", "DESIGNATION", "GID", "OBS_UNIT_ID" };
		Assert.assertTrue(
				"Expecting that the headers are valid when all of the required column are present but didn't.",
				KsuFieldbookUtil.isValidHeaderNames(headerNames));
	}

	@Test
	public void testIsValidHeaderNamesReturnsFalseIfAtLeastOneOfTheRequiredColumnsIsNotPresent() {
		final String[] headerNames = { "PLOT_NO", "ENTRY_NO", "DESIGNATION" };
		Assert.assertFalse(
				"Expecting that the headers are not valid if at least one of the required column is not present but didn't.",
				KsuFieldbookUtil.isValidHeaderNames(headerNames));
	}

	@Test
	public void testGetLabelFromKsuRequiredColumnTermIdPresentInEnum() {
		final MeasurementTestDataInitializer measurementTestDataInitializer = new MeasurementTestDataInitializer();
		final MeasurementVariable mVar = measurementTestDataInitializer
				.createMeasurementVariable(TermId.ENTRY_NO.getId(), 1);
		final String label = KsuFieldbookUtil.getLabelFromKsuRequiredColumn(mVar);
		Assert.assertEquals("The label should be ENTRY_NO", "ENTRY_NO", label);

	}

	@Test
	public void testGetLabelFromKsuRequiredColumnTermIdNotPresentInEnum() {
		final MeasurementTestDataInitializer measurementTestDataInitializer = new MeasurementTestDataInitializer();
		final MeasurementVariable mVar = measurementTestDataInitializer.createMeasurementVariable(1, 1);
		final String label = KsuFieldbookUtil.getLabelFromKsuRequiredColumn(mVar);
		Assert.assertEquals("The label should be " + mVar.getName(), mVar.getName(), label);

	}

	@Test
	public void testConvertTraitsData() {

		final List<MeasurementVariable> traits = this.initializeTraitTest();
		final List<List<String>> traitsList = KsuFieldbookUtil.convertTraitsData(traits,
				this.fieldbookMiddlewareService, this.ontologyService);
		MatcherAssert.assertThat(traitsList, IsCollectionWithSize.hasSize(5));
		MatcherAssert.assertThat(KsuFieldbookUtilTest.NUMBER, Is.is(CoreMatchers.equalTo(traitsList.get(1).get(1))));
		MatcherAssert.assertThat(KsuFieldbookUtilTest.CATEGORICAL,
				Is.is(CoreMatchers.equalTo(traitsList.get(2).get(1))));
		MatcherAssert.assertThat(KsuFieldbookUtilTest.DATE, Is.is(CoreMatchers.equalTo(traitsList.get(3).get(1))));
		MatcherAssert.assertThat(KsuFieldbookUtilTest.TEXT, Is.is(CoreMatchers.equalTo(traitsList.get(4).get(1))));

	}

	@Test
	public void testConvertWorkbookData() {
		final List<MeasurementRow> observations = WorkbookDataUtil.createNewObservations(10);
		final List<MeasurementVariable> variables = WorkbookDataUtil.createFactors();
		final List<List<String>> table = KsuFieldbookUtil.convertWorkbookData(observations, variables);
		Assert.assertNotNull("The table should not be null", table);
		Assert.assertEquals("The table size should be 11, the header row + the number of observations", 11,
				table.size());
	}

	@Test
	public void testGetHeaderNames() {
		
		final List<MeasurementVariable> variables = WorkbookDataUtil.createFactors();
		final MeasurementVariable check = MeasurementVariableTestDataInitializer.createMeasurementVariableWithName(TermId.ENTRY_TYPE.getId(), TermId.ENTRY_TYPE.name());
		variables.add(check);
		
		final List<String> headers = KsuFieldbookUtil.getHeaderNames(variables);
		
		Assert.assertNotNull("The headers should not be null", headers);
		Assert.assertEquals("The number of headers should be 6", 6, headers.size());
		Assert.assertFalse(check.getName() + " should not be included in the headers list.", headers.contains(check.getName()));
		Assert.assertTrue(TermId.CROSS.name() + " should be included in the headers list.", headers.contains(TermId.CROSS.name()));
		
		// The variables list contains ENTRY, GID, DESIG, CROSS, SEED SOURCE, PLOT, BLOCK, REP,	and CHECK; only the following should be included in the resulting headers list
		Assert.assertTrue(WorkbookDataUtil.ENTRY + " should be included in the headers list.", headers.contains(WorkbookDataUtil.ENTRY));
		Assert.assertTrue(WorkbookDataUtil.GID + " should be included in the headers list.", headers.contains(WorkbookDataUtil.GID));
		Assert.assertTrue(WorkbookDataUtil.DESIG + " should be included in the headers list.", headers.contains(WorkbookDataUtil.DESIG));
		Assert.assertTrue(WorkbookDataUtil.PLOT + " should be included in the headers list.", headers.contains(WorkbookDataUtil.PLOT));
		Assert.assertTrue(TermId.PLOT_NO.name() + " should be included in the headers list.", headers.contains(TermId.PLOT_NO.name()));
	}

	@Test
	public void testGetDataTypeDescription() {
		final MeasurementVariable mvar = MeasurementVariableTestDataInitializer.createMeasurementVariable(
				KsuFieldbookUtilTest.R_SECA_SEV_E_00_TO_99_TEXT_ID,
				KsuFieldbookUtilTest.R_SECA_SEV_E_00_TO_99_TEXT_NAME,
				KsuFieldbookUtilTest.R_SECA_SEV_E_00_TO_99_TEXT_DESCRIPTION,
				KsuFieldbookUtilTest.R_SECA_SEV_E_00_TO_99_TEXT_SCALE,
				KsuFieldbookUtilTest.R_SECA_SEV_E_00_TO_99_TEXT_METHOD,
				KsuFieldbookUtilTest.R_SECA_SEV_E_00_TO_99_TEXT_PROPERTY, KsuFieldbookUtilTest.TEXT, "",
				KsuFieldbookUtilTest.STUDY, TermId.CHARACTER_VARIABLE.getId(), PhenotypicType.VARIATE);
		final String dataTypeDescription = KsuFieldbookUtil.getDataTypeDescription(mvar);
		Assert.assertEquals("The data type description should be text", "text", dataTypeDescription);
	}

	@Test
	public void testGetLabelFromKsuRequiredColumnNotRequired() {
		final MeasurementVariable mvar = MeasurementVariableTestDataInitializer.createMeasurementVariable(
				KsuFieldbookUtilTest.R_SECA_SEV_E_00_TO_99_TEXT_ID,
				KsuFieldbookUtilTest.R_SECA_SEV_E_00_TO_99_TEXT_NAME,
				KsuFieldbookUtilTest.R_SECA_SEV_E_00_TO_99_TEXT_DESCRIPTION,
				KsuFieldbookUtilTest.R_SECA_SEV_E_00_TO_99_TEXT_SCALE,
				KsuFieldbookUtilTest.R_SECA_SEV_E_00_TO_99_TEXT_METHOD,
				KsuFieldbookUtilTest.R_SECA_SEV_E_00_TO_99_TEXT_PROPERTY, KsuFieldbookUtilTest.TEXT, "",
				KsuFieldbookUtilTest.STUDY, TermId.CHARACTER_VARIABLE.getId(), PhenotypicType.VARIATE);
		final String label = KsuFieldbookUtil.getLabelFromKsuRequiredColumn(mvar);
		Assert.assertEquals("The label should be " + mvar.getName(), mvar.getName(), label);
	}

	@Test
	public void testGetLabelFromKsuRequiredColumnIsRequired() {
		final MeasurementVariable mvar = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.GID.getId(), "1");
		final String label = KsuFieldbookUtil.getLabelFromKsuRequiredColumn(mvar);
		Assert.assertEquals("The label should be GID", "GID", label);
	}

	private List<MeasurementVariable> initializeListOfTraits() {
		final List<MeasurementVariable> variates = new ArrayList<>();

		MeasurementVariable measurementVariable = MeasurementVariableTestDataInitializer.createMeasurementVariable(
				KsuFieldbookUtilTest.HGRAMINC_E_PCT_ID, KsuFieldbookUtilTest.HGRAMINC_E_PCT_NAME,
				KsuFieldbookUtilTest.HGRAMINC_E_PCT_DESCRIPTION, KsuFieldbookUtilTest.PERCENTAGE_SCALE,
				KsuFieldbookUtilTest.HGRAMINC_E_PCT_METHOD, KsuFieldbookUtilTest.HGRAMINC_E_PCT_PROPERTY,
				KsuFieldbookUtilTest.NUMERIC, KsuFieldbookUtilTest.NUMERIC_VALUE, KsuFieldbookUtilTest.PLOT,
				TermId.NUMERIC_VARIABLE.getId(), PhenotypicType.VARIATE);

		variates.add(measurementVariable);

		measurementVariable = MeasurementVariableTestDataInitializer.createMeasurementVariable(
				KsuFieldbookUtilTest.ACDTOL_E_1TO_5_ID, KsuFieldbookUtilTest.ACDTOL_E_1TO_5_NAME,
				KsuFieldbookUtilTest.ACDTOL_E_1TO_5_DESCRIPTION, KsuFieldbookUtilTest.SCALE_1_5,
				KsuFieldbookUtilTest.ACDTOL_E_1TO_5_METHOD, KsuFieldbookUtilTest.ACDTOL_E_1TO_5_PROPERTY,
				KsuFieldbookUtilTest.CHARACTER, KsuFieldbookUtilTest.NUMERIC_VALUE, KsuFieldbookUtilTest.PLOT,
				TermId.CATEGORICAL_VARIABLE.getId(), PhenotypicType.VARIATE);

		variates.add(measurementVariable);

		measurementVariable = MeasurementVariableTestDataInitializer.createMeasurementVariable(
				KsuFieldbookUtilTest.ANT_DATE_YMD_ID, KsuFieldbookUtilTest.ANT_DATE_YMD_NAME,
				KsuFieldbookUtilTest.ANT_DATE_YMD_DESCRIPTION, KsuFieldbookUtilTest.YYYYMMDD_SCALE,
				KsuFieldbookUtilTest.ANT_DATE_YMD_METHOD, KsuFieldbookUtilTest.ANT_DATE_YMD_PROPERTY,
				KsuFieldbookUtilTest.DATE, KsuFieldbookUtilTest.DATE_VALUE, KsuFieldbookUtilTest.STUDY,
				TermId.DATE_VARIABLE.getId(), PhenotypicType.VARIATE);
		variates.add(measurementVariable);

		measurementVariable = MeasurementVariableTestDataInitializer.createMeasurementVariable(
				KsuFieldbookUtilTest.R_SECA_SEV_E_00_TO_99_TEXT_ID,
				KsuFieldbookUtilTest.R_SECA_SEV_E_00_TO_99_TEXT_NAME,
				KsuFieldbookUtilTest.R_SECA_SEV_E_00_TO_99_TEXT_DESCRIPTION,
				KsuFieldbookUtilTest.R_SECA_SEV_E_00_TO_99_TEXT_SCALE,
				KsuFieldbookUtilTest.R_SECA_SEV_E_00_TO_99_TEXT_METHOD,
				KsuFieldbookUtilTest.R_SECA_SEV_E_00_TO_99_TEXT_PROPERTY, KsuFieldbookUtilTest.TEXT, "",
				KsuFieldbookUtilTest.STUDY, TermId.CHARACTER_VARIABLE.getId(), PhenotypicType.VARIATE);
		variates.add(measurementVariable);

		return variates;
	}

	private List<MeasurementVariable> initializeTraitTest() {
		final List<MeasurementVariable> traits = this.initializeListOfTraits();
		final Method breedingMethod = new Method(40, "DER", "G", "SLF", "Self and Bulk",
				"Selfing a Single Plant or population and bulk seed", 0, -1, 1, 0, 1490, 1, 0, 19980708);

		final List<Method> methodList = Lists.newArrayList();
		methodList.add(breedingMethod);
		final Term term = new Term();
		term.setId(2);
		term.setName("Breeding method");
		term.setDefinition("Breeding method");
		final Property property = new Property(term);
		Mockito.when(this.fieldbookMiddlewareService.getAllBreedingMethods(false)).thenReturn(methodList);
		Mockito.when(this.ontologyService.getProperty(TermId.BREEDING_METHOD_PROP.getId())).thenReturn(property);

		return traits;
	}
}
