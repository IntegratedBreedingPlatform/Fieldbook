package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.trial.bean.AdvancingStudy;
import org.generationcp.middleware.ruleengine.pojo.DeprecatedAdvancingSource;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@Deprecated
@RunWith(MockitoJUnitRunner.class)
public class SelectionTraitExpressionDataProcessorTest {

    public static final String TEST_PROGRAM_UUID = "ABCD";
    public static final Integer TEST_TERM_ID = 1;

    @Mock
    private OntologyVariableDataManager ontologyVariableDataManager;

    @Mock
    private ContextUtil contextUtil;

    @InjectMocks
    private SelectionTraitExpressionDataProcessor unitUnderTest;

    @Before
    public void setUp() throws Exception {
        Mockito.when(contextUtil.getCurrentProgramUUID()).thenReturn(TEST_PROGRAM_UUID);
    }

    @Test
    public void testRetrieveEnvironmentalValueStudyDetail() {
        final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyTypeDto.getNurseryDto());
        final String testValue = "test";
        final MeasurementVariable detail = new MeasurementVariable();
        detail.setProperty(SelectionTraitExpressionDataProcessor.SELECTION_TRAIT_PROPERTY);
        detail.setPossibleValues(Lists.newArrayList(new ValueReference(1,"name","test")));
        detail.setValue(testValue);
        detail.setTermId(1);

        // study details are placed within the conditions portion of the workbook
        workbook.getConditions().add(detail);

        final DeprecatedAdvancingSource source = Mockito.mock(DeprecatedAdvancingSource.class);
        final AdvancingStudy advancingStudy = Mockito.mock(AdvancingStudy.class);
        final Study study = Mockito.mock(Study.class);

        Mockito.when(ontologyVariableDataManager.retrieveVariableCategoricalNameValue(TEST_PROGRAM_UUID, TEST_TERM_ID, Integer.parseInt("1"), true)).thenReturn(testValue);

        unitUnderTest.processEnvironmentLevelData(source, workbook, advancingStudy, study);
        Mockito.verify(source).setSelectionTraitValue(testValue);

    }

    @Test
    public void testRetrieveEnvironmentalValueStudyCondition() {
        final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyTypeDto.getNurseryDto());
        final String testValue = "test";
        final MeasurementVariable detail = new MeasurementVariable();
        detail.setProperty(SelectionTraitExpressionDataProcessor.SELECTION_TRAIT_PROPERTY);
        detail.setPossibleValues(Lists.newArrayList(new ValueReference(1,"name","test")));
        detail.setValue(testValue);
        detail.setTermId(1);

        // advancingStudy conditions are placed within the constants section of the workbook
        workbook.getConstants().add(detail);

        final DeprecatedAdvancingSource source = Mockito.mock(DeprecatedAdvancingSource.class);
        final AdvancingStudy advancingStudy = Mockito.mock(AdvancingStudy.class);
        final Study study = Mockito.mock(Study.class);

        Mockito.when(ontologyVariableDataManager.retrieveVariableCategoricalNameValue(TEST_PROGRAM_UUID, TEST_TERM_ID, Integer.parseInt("1"), true)).thenReturn(testValue);

        unitUnderTest.processEnvironmentLevelData(source, workbook, advancingStudy, study);
        Mockito.verify(source).setSelectionTraitValue(testValue);

    }

    @Test
    public void testExtractCategoricalValueNonOutofBounds() {
        final String categoricalValue = "test";
        final String testCategoricalValueID = "1";
		Mockito.when(ontologyVariableDataManager.retrieveVariableCategoricalNameValue(TEST_PROGRAM_UUID, TEST_TERM_ID, Integer.parseInt(testCategoricalValueID), true)).thenReturn(categoricalValue);

        final String output = unitUnderTest.extractValue(testCategoricalValueID, TEST_TERM_ID);

        Assert.assertEquals("Unable to properly extract the value of a categorical value given the categorical value ID", categoricalValue, output);
    }

    @Test
    public void testExtractCategoricalValueOutOfBoundsNumeric() {
        final String categoricalValue = "1";

		Mockito.when(ontologyVariableDataManager.retrieveVariableCategoricalNameValue(TEST_PROGRAM_UUID, TEST_TERM_ID, Integer.parseInt(categoricalValue), true)).thenReturn(categoricalValue);

        final String output = unitUnderTest.extractValue(categoricalValue, TEST_TERM_ID);

        Assert.assertEquals("Unable to properly return the value of a numeric out of bounds value for a categorical variable", categoricalValue, output);
    }

    @Test
    public void testExtractCategoricalValueOutOfBoundsNonNumeric() {
        final String categoricalValue = "OK";

        final String output = unitUnderTest.extractValue(categoricalValue, TEST_TERM_ID);

        Assert.assertEquals("Unable to properly return the value of a non numeric out of bounds value for a categorical variable", categoricalValue, output);
    }

    @Test
    public void testProcessPlotLevelDataWithMeasurementDataForNursery(){
        final String testValue = "test";

        final MeasurementRow measurementRow = new MeasurementRow();
        setMeasurementRow(measurementRow,1,"name","test",unitUnderTest.SELECTION_TRAIT_PROPERTY);

        final DeprecatedAdvancingSource source = Mockito.mock(DeprecatedAdvancingSource.class);

        Mockito.when(ontologyVariableDataManager.retrieveVariableCategoricalNameValue(TEST_PROGRAM_UUID, TEST_TERM_ID, Integer.parseInt("1"), true)).thenReturn(testValue);

        unitUnderTest.processPlotLevelData(source, measurementRow);
        Mockito.verify(source).setSelectionTraitValue(testValue);

    }

    @Test
    public void testProcessPlotLevelDataWithMeasurementDataForStudyWithSamples(){
        final String testValue = "test";

        final MeasurementRow measurementRow = new MeasurementRow();
        setMeasurementRow(measurementRow,1,"name","test",unitUnderTest.SELECTION_TRAIT_PROPERTY);
        setMeasurementRow(measurementRow,-2,"SAMPLES","samples description",null);

        final DeprecatedAdvancingSource source = Mockito.mock(DeprecatedAdvancingSource.class);

        Mockito.when(ontologyVariableDataManager.retrieveVariableCategoricalNameValue(TEST_PROGRAM_UUID, TEST_TERM_ID, Integer.parseInt("1"), true)).thenReturn(testValue);

        unitUnderTest.processPlotLevelData(source, measurementRow);
        Mockito.verify(source).setSelectionTraitValue(testValue);

    }

    private void setMeasurementRow(final MeasurementRow measurementRow, final Integer id, final String name,
        final String description,final String property) {
        final List<MeasurementData> dataList = new ArrayList<>();
        final MeasurementData selectionTraitData = new MeasurementData();
        selectionTraitData.setValue("test");

        final MeasurementVariable variable = new MeasurementVariable();
        variable.setTermId(id);
        variable.setPossibleValues(Lists.newArrayList(new ValueReference(id,name,description)));
        if (null != property) {
            variable.setProperty(property);
        }
        selectionTraitData.setMeasurementVariable(variable);
        dataList.add(selectionTraitData);
        if (null != measurementRow.getDataList()) {
            measurementRow.getDataList().add(selectionTraitData);
        } else {
            measurementRow.setDataList(dataList);
        }
    }
}
