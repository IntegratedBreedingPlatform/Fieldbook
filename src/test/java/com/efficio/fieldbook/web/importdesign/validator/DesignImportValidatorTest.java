package com.efficio.fieldbook.web.importdesign.validator;

import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.data.initializer.DesignImportTestDataInitializer;
import com.efficio.fieldbook.web.data.initializer.ImportedGermplasmInitializer;
import com.efficio.fieldbook.web.importdesign.service.DesignImportService;
import com.efficio.fieldbook.web.study.germplasm.StudyEntryTransformer;
import junit.framework.Assert;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class DesignImportValidatorTest {

	@Mock
	private UserSelection userSelection;

	@Mock
	private MessageSource messageSource;

	@Mock
	private OntologyScaleDataManager ontologyScaleDataManager;

	@Mock
	private DesignImportService designImportService;

	@Mock
	private StudyEntryService studyEntryService;

	@Mock
	private StudyEntryTransformer studyEntryTransformer;

	@InjectMocks
	private DesignImportValidator designImportValidator;

	private DesignImportData designImportData;

	@Before
	public void setUp() throws DesignValidationException {
		this.designImportData = DesignImportTestDataInitializer.createDesignImportData(1, 1);

		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook();
		Mockito.when(this.userSelection.getWorkbook()).thenReturn(workbook);
		final StudyEntryDto studyEntryDto = Mockito.mock(StudyEntryDto.class);
		final List<StudyEntryDto> studyEntries = Collections.singletonList(studyEntryDto);
		workbook.getStudyDetails().setId(1);
		Mockito.when(this.studyEntryService.getStudyEntries(workbook.getStudyDetails().getId())).thenReturn(studyEntries);
		Mockito.when(this.studyEntryTransformer.tranformToImportedGermplasm(studyEntries))
			.thenReturn(ImportedGermplasmInitializer.createImportedGermplasmList());
		final DesignHeaderItem headerItem = DesignImportTestDataInitializer
			.filterDesignHeaderItemsByTermId(TermId.ENTRY_NO, this.designImportData.getMappedHeaders().get(PhenotypicType.ENTRY_DETAIL));
		Mockito.doReturn(headerItem).when(this.designImportService).validateIfStandardVariableExists(
			this.designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId().get(PhenotypicType.ENTRY_DETAIL),
			"design.import.error.entry.no.is.required", TermId.ENTRY_NO);

	}

	@Test
	public void testValidateDesignData() {
		try {

			this.designImportValidator.validateDesignData(this.designImportData);

		} catch (final DesignValidationException e) {

			Assert.fail("The data should pass the validateDesignData test");
		}

	}

	@Test
	public void testValidateEntryNumbers() {

		final DesignImportData testData = DesignImportTestDataInitializer.createDesignImportData(1, 1, 5);
		final DesignHeaderItem entryNoHeader = DesignImportTestDataInitializer
			.filterDesignHeaderItemsByTermId(TermId.ENTRY_NO, testData.getMappedHeaders().get(PhenotypicType.ENTRY_DETAIL));

		try {

			this.designImportValidator.validateEntryNumbers(entryNoHeader, testData.getRowDataMap());

		} catch (final DesignValidationException e) {

			Assert.fail("The data should pass the validateGermplasmEntriesFromShouldMatchTheGermplasmList test");
		}

	}

	@Test
	public void testValidateEntryNumbers_SomeEntriesMatch() {

		final DesignImportData testData = DesignImportTestDataInitializer.createDesignImportData(1, 1, 2);
		final DesignHeaderItem entryNoHeader = DesignImportTestDataInitializer
			.filterDesignHeaderItemsByTermId(TermId.ENTRY_NO, testData.getMappedHeaders().get(PhenotypicType.ENTRY_DETAIL));

		try {

			this.designImportValidator.validateEntryNumbers(entryNoHeader, this.designImportData.getRowDataMap());

		} catch (final DesignValidationException e) {

			Assert.fail("The data should pass the validateGermplasmEntriesFromShouldMatchTheGermplasmList test");
		}

	}

	@Test
	public void testValidateEntryNumbers_SomeEntriesDoNotMatch() {

		final DesignImportData testData = DesignImportTestDataInitializer.createDesignImportData(1, 1, 10);
		final DesignHeaderItem entryNoHeader = DesignImportTestDataInitializer
			.filterDesignHeaderItemsByTermId(TermId.ENTRY_NO, testData.getMappedHeaders().get(PhenotypicType.ENTRY_DETAIL));

		try {

			this.designImportValidator.validateEntryNumbers(entryNoHeader, this.designImportData.getRowDataMap());

		} catch (final DesignValidationException e) {

			Assert.assertEquals(e.getMessage(), "entries do not match");
		}

	}

	@Test
	public void testValidateIfPlotNumberIsUnique() throws DesignValidationException {

		final DesignHeaderItem trialInstanceHeaderItem = this.designImportService.validateIfStandardVariableExists(
			this.designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId().get(PhenotypicType.TRIAL_ENVIRONMENT),
			"Error", TermId.TRIAL_INSTANCE_FACTOR);
		final Map<String, Map<Integer, List<String>>> csvMap =
			this.designImportService.groupCsvRowsIntoTrialInstance(trialInstanceHeaderItem, this.designImportData.getRowDataMap());

		try {

			this.designImportValidator
				.validateIfPlotNumberIsUniquePerInstance(
					this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN),
					csvMap);

		} catch (final DesignValidationException e) {

			Assert.fail("The list should pass the validateIfPlotNumberIsUnique test");
		}

	}

	@Test
	public void testValidateIfPlotNumberIsUniquePerInstance() {

		final DesignHeaderItem trialInstanceHeaderItem = DesignImportTestDataInitializer
			.filterDesignHeaderItemsByTermId(
				TermId.TRIAL_INSTANCE_FACTOR,
				this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));
		final DesignHeaderItem plotNoHeaderItem = DesignImportTestDataInitializer
			.filterDesignHeaderItemsByTermId(TermId.PLOT_NO, this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN));

		final Map<Integer, List<String>> csvData = this.designImportData.getRowDataMap();
		csvData.get(0).set(plotNoHeaderItem.getColumnIndex(), "1");
		csvData.get(1).set(plotNoHeaderItem.getColumnIndex(), "1");
		csvData.get(2).set(plotNoHeaderItem.getColumnIndex(), "1");

		final Map<String, Map<Integer, List<String>>> csvMap =
			DesignImportTestDataInitializer.groupCsvRowsIntoTrialInstance(trialInstanceHeaderItem, csvData);

		try {

			this.designImportValidator
				.validateIfPlotNumberIsUniquePerInstance(
					this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN),
					csvMap);

			Assert.fail("The list shouldn't pass the validateIfPlotNumberIsUniquePerInstance test");

		} catch (final DesignValidationException e) {

		}

	}

	@Test
	public void testValidateColumnValuesForImportDesignWithOutInvalidValue() {
		final DesignImportData designImportData = DesignImportTestDataInitializer.createDesignImportData(1, 1);
		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = designImportData.getMappedHeaders();
		try {
			this.designImportValidator.validateColumnValues(designImportData.getRowDataMap(), mappedHeaders);
		} catch (final DesignValidationException e) {
			Assert.fail(
				"Expecting that there is no exception thrown for the validation of test design import data with no invalid values.");
		}
	}

	@Test
	public void testRetrieveDesignHeaderItemsBasedOnDataType() {
		final DesignImportData designImportData = DesignImportTestDataInitializer.createDesignImportData(1, 1);
		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = designImportData.getMappedHeaders();

		final List<DesignHeaderItem> numericDesignHeaderItems =
			this.designImportValidator.retrieveDesignHeaderItemsBasedOnDataType(mappedHeaders, TermId.NUMERIC_VARIABLE.getId());
		Assert.assertEquals(DesignImportTestDataInitializer.NO_OF_NUMERIC_VARIABLES, numericDesignHeaderItems.size());

		final List<DesignHeaderItem> characterDesignHeaderItems =
			this.designImportValidator.retrieveDesignHeaderItemsBasedOnDataType(mappedHeaders, TermId.CHARACTER_VARIABLE.getId());
		Assert.assertEquals(DesignImportTestDataInitializer.NO_OF_CHARACTER_VARIABLES, characterDesignHeaderItems.size());

		this.designImportValidator.retrieveDesignHeaderItemsBasedOnDataType(mappedHeaders, TermId.CATEGORICAL_VARIABLE.getId());
		Assert.assertEquals(DesignImportTestDataInitializer.NO_OF_CATEGORICAL_VARIABLES, characterDesignHeaderItems.size());

	}

}
