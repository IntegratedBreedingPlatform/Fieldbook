package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.trial.bean.AdvancingStudy;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.ruleengine.pojo.AdvancingSource;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.Location;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Deprecated
@Component
public class LocationAbbreviationExpressionDataProcessor implements ExpressionDataProcessor {

    @Resource
    private LocationService locationService;

    @Override
    public void processEnvironmentLevelData(AdvancingSource source, Workbook workbook, AdvancingStudy nurseryInfo, Study study) throws FieldbookException {
        String locationAbbreviation = nurseryInfo.getHarvestLocationAbbreviation();
        source.setLocationAbbreviation(locationAbbreviation);
    }

    @Override
    public void processPlotLevelData(AdvancingSource source, MeasurementRow row) throws FieldbookException {
        if(source.getTrailInstanceObservationMeasurementRow() != null &&
                source.getTrailInstanceObservationMeasurementRow().getDataList() != null &&  !source.getTrailInstanceObservationMeasurementRow().getDataList().isEmpty()){
                for(MeasurementData measurementData : source.getTrailInstanceObservationMeasurementRow().getDataList()){
                if(measurementData.getMeasurementVariable().getTermId() == TermId.LOCATION_ID.getId()){
                    String locationAbbreviation = null;
                    String locationIdString = measurementData.getValue();
                    Integer locationId = StringUtils.isEmpty(locationIdString) ? null : Integer.valueOf(locationIdString);
                    if(locationId != null){
                        Location location = locationService.getLocationByID(locationId);
                        locationAbbreviation = location.getLabbr();
                    }
                    source.setLocationAbbreviation(locationAbbreviation);
                    break;
                }
            }
        }
    }
}
