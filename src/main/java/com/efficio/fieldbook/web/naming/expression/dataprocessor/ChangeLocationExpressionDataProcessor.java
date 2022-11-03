package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import com.efficio.fieldbook.util.FieldbookException;
import org.generationcp.middleware.ruleengine.pojo.DeprecatedAdvancingSource;
import com.efficio.fieldbook.web.trial.bean.AdvancingStudy;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.springframework.stereotype.Component;

@Deprecated
@Component
public class ChangeLocationExpressionDataProcessor implements ExpressionDataProcessor {

    @Override
    public void processEnvironmentLevelData(DeprecatedAdvancingSource source, Workbook workbook, AdvancingStudy nurseryInfo, Study study) throws FieldbookException {
        String locationIdString = nurseryInfo.getHarvestLocationId();
        Integer locationId = StringUtils.isEmpty(locationIdString) ? null : Integer.valueOf(locationIdString);

        source.setHarvestLocationId(locationId);
    }

    @Override
    public void processPlotLevelData(DeprecatedAdvancingSource source, MeasurementRow row) throws FieldbookException {
		if(source.getTrailInstanceObservationMeasurementRow() != null &&
                source.getTrailInstanceObservationMeasurementRow().getDataList() != null &&  !source.getTrailInstanceObservationMeasurementRow().getDataList().isEmpty()){
            for(MeasurementData measurementData : source.getTrailInstanceObservationMeasurementRow().getDataList()){
                if(measurementData.getMeasurementVariable().getTermId() == TermId.LOCATION_ID.getId()){
                    String locationIdString = measurementData.getValue();
                    Integer locationId = StringUtils.isEmpty(locationIdString) ? null : Integer.valueOf(locationIdString);
                    source.setHarvestLocationId(locationId);
                    break;
                }
            }
        }
    }
}
