package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.trial.bean.AdvancingStudy;
import com.google.common.collect.Lists;
import org.generationcp.middleware.ruleengine.pojo.AdvancingSource;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.Location;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

@Deprecated
@RunWith(MockitoJUnitRunner.class)
public class LocationAbbreviationExpressionDataProcessorTest {

    @Mock
    private LocationService locationService;

    @InjectMocks
    private LocationAbbreviationExpressionDataProcessor locationAbbreviationExpressionDataProcessor;


    @Test
    public void testProcessEnvironmentLevelDataWithHarvestLocationAbbr() throws FieldbookException {

        final AdvancingSource source = Mockito.mock(AdvancingSource.class);
        final AdvancingStudy advancingStudy = new AdvancingStudy();
        advancingStudy.setHarvestLocationAbbreviation("abbr");

        locationAbbreviationExpressionDataProcessor.processEnvironmentLevelData(source, null, advancingStudy, null);
        Mockito.verify(source).setLocationAbbreviation("abbr");
    }

    @Test
    public void testProcessEnvironmentLevelDataWithNoHarvestLocationAbbr() throws FieldbookException {

        final AdvancingSource source = Mockito.mock(AdvancingSource.class);
        final AdvancingStudy advancingStudy = new AdvancingStudy();

        locationAbbreviationExpressionDataProcessor.processEnvironmentLevelData(source, null, advancingStudy, null);
        Mockito.verify(source).setLocationAbbreviation(null);

    }

    @Test
    public void testProcessPlotLevelDataWithLocationMeasurementData() throws FieldbookException {
        final AdvancingSource source = Mockito.mock(AdvancingSource.class);

        final MeasurementRow measurementRow = Mockito.mock(MeasurementRow.class);
        final List<MeasurementData> listMeasurementData = Lists.newArrayList();
        final MeasurementData measurementData = new MeasurementData();
        measurementData.setValue("11");
        final MeasurementVariable measurementVariable = new MeasurementVariable();
        measurementVariable.setTermId(TermId.LOCATION_ID.getId());
        measurementData.setMeasurementVariable(measurementVariable);
        listMeasurementData.add(measurementData);

        final Location location = new Location();
        location.setLabbr("IND");

        Mockito.when(measurementRow.getDataList()).thenReturn(listMeasurementData);
        Mockito.when(source.getTrailInstanceObservation()).thenReturn(measurementRow);
        Mockito.when(locationService.getLocationByID(11)).thenReturn(location);

        locationAbbreviationExpressionDataProcessor.processPlotLevelData(source, null);

        Mockito.verify(source).setLocationAbbreviation("IND");
    }

    @Test
    public void testProcessPlotLevelDataWithoutLocationMeasurementData() throws FieldbookException {
        final AdvancingSource source = Mockito.mock(AdvancingSource.class);
        final MeasurementRow measurementRow = Mockito.mock(MeasurementRow.class);
        final List<MeasurementData> listMeasurementData = Lists.newArrayList();
        final MeasurementData measurementData = new MeasurementData();
        measurementData.setValue("11");
        final MeasurementVariable measurementVariable = new MeasurementVariable();
        measurementVariable.setTermId(TermId.LOCATION_ABBR.getId());
        measurementData.setMeasurementVariable(measurementVariable);
        listMeasurementData.add(measurementData);

        Mockito.when(measurementRow.getDataList()).thenReturn(listMeasurementData);
        Mockito.when(source.getTrailInstanceObservation()).thenReturn(measurementRow);

        locationAbbreviationExpressionDataProcessor.processPlotLevelData(source, null);

        Mockito.verify(source, Mockito.never()).setLocationAbbreviation("IND");
    }

    @Test
    public void testProcessPlotLevelDataWithNoMeasurementData() throws Exception {
        final AdvancingSource source = Mockito.mock(AdvancingSource.class);
        final MeasurementRow measurementRow = Mockito.mock(MeasurementRow.class);
        Mockito.when(measurementRow.getDataList()).thenReturn(null);
        Mockito.when(source.getTrailInstanceObservation()).thenReturn(measurementRow);

        locationAbbreviationExpressionDataProcessor.processPlotLevelData(source, null);

        Mockito.verify(source, Mockito.never()).setLocationAbbreviation("IND");
    }
}
