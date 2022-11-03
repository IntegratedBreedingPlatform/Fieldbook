package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import com.efficio.fieldbook.web.trial.bean.AdvancingStudy;
import org.generationcp.middleware.ruleengine.pojo.DeprecatedAdvancingSource;
import com.google.common.collect.Lists;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

@Deprecated
public class ChangeLocationExpressionDataProcessorTest {

    ChangeLocationExpressionDataProcessor changeLocationExpressionDataProcessor;

    @Before
    public void setup(){
        changeLocationExpressionDataProcessor = new ChangeLocationExpressionDataProcessor();
    }

    @Test
    public void testProcessEnvironmentLevelDataWithHarvestLocationId() throws Exception {
        final DeprecatedAdvancingSource source = Mockito.mock(DeprecatedAdvancingSource.class);
        final AdvancingStudy advancingStudy = new AdvancingStudy();
        advancingStudy.setHarvestLocationId("205");

        changeLocationExpressionDataProcessor.processEnvironmentLevelData(source, null, advancingStudy, null);
        Mockito.verify(source).setHarvestLocationId(205);
    }

    @Test
    public void testProcessEnvironmentLevelDataWithNoHarvestLocationId() throws Exception {
        final DeprecatedAdvancingSource source = Mockito.mock(DeprecatedAdvancingSource.class);
        final AdvancingStudy advancingStudy = new AdvancingStudy();

        changeLocationExpressionDataProcessor.processEnvironmentLevelData(source, null, advancingStudy, null);
        Mockito.verify(source).setHarvestLocationId(null);
    }

    @Test
    public void testProcessPlotLevelDataWithLocationMeasurementData() throws Exception {
        final DeprecatedAdvancingSource source = Mockito.mock(DeprecatedAdvancingSource.class);
        final MeasurementRow measurementRow = Mockito.mock(MeasurementRow.class);
        final List<MeasurementData> listMeasurementData = Lists.newArrayList();
        final MeasurementData locationId = new MeasurementData();
        locationId.setValue("205");
        final MeasurementVariable locationVariable = new MeasurementVariable();
        locationVariable.setTermId(TermId.LOCATION_ID.getId());
        locationId.setMeasurementVariable(locationVariable);
        listMeasurementData.add(locationId);

        Mockito.when(measurementRow.getDataList()).thenReturn(listMeasurementData);
        Mockito.when(source.getTrailInstanceObservationMeasurementRow()).thenReturn(measurementRow);


        changeLocationExpressionDataProcessor.processPlotLevelData(source, null);

        Mockito.verify(source).setHarvestLocationId(205);
    }

    @Test
    public void testProcessPlotLevelDataWithoutLocationMeasurementData() throws Exception {
        final DeprecatedAdvancingSource source = Mockito.mock(DeprecatedAdvancingSource.class);
        final MeasurementRow measurementRow = Mockito.mock(MeasurementRow.class);
        final List<MeasurementData> listMeasurementData = Lists.newArrayList();
        final MeasurementData locationId = new MeasurementData();
        locationId.setValue("205");
        final MeasurementVariable locationVariable = new MeasurementVariable();
        locationVariable.setTermId(TermId.LOCATION_ABBR.getId());
        locationId.setMeasurementVariable(locationVariable);
        listMeasurementData.add(locationId);

        Mockito.when(measurementRow.getDataList()).thenReturn(listMeasurementData);
        Mockito.when(source.getTrailInstanceObservationMeasurementRow()).thenReturn(measurementRow);


        changeLocationExpressionDataProcessor.processPlotLevelData(source, null);

        Mockito.verify(source, Mockito.never()).setHarvestLocationId(205);
    }

    @Test
    public void testProcessPlotLevelDataWithNoMeasurementData() throws Exception {
        final DeprecatedAdvancingSource source = Mockito.mock(DeprecatedAdvancingSource.class);
        final MeasurementRow measurementRow = Mockito.mock(MeasurementRow.class);
        Mockito.when(measurementRow.getDataList()).thenReturn(null);
        Mockito.when(source.getTrailInstanceObservationMeasurementRow()).thenReturn(measurementRow);


        changeLocationExpressionDataProcessor.processPlotLevelData(source, null);

        Mockito.verify(source, Mockito.never()).setHarvestLocationId(205);
    }
}
