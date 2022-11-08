package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.trial.bean.AdvancingStudy;
import org.generationcp.middleware.ruleengine.pojo.DeprecatedAdvancingSource;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;

public interface ExpressionDataProcessor {

    /**
     * Method that should be implemented if the expression needs environment level data. The required data
     * should be set into the provided AdvancingSource  object
     *
     * @param source
     * @param workbook
     * @param nurseryInfo
     * @param study
     */
    void processEnvironmentLevelData(DeprecatedAdvancingSource source, Workbook workbook, AdvancingStudy nurseryInfo,
                                            Study study) throws FieldbookException;

    /**
     * Method that should be implemented if the expression needs plot level data. The required data should be set
     * into the provided AdvancingSource object
     *
     * @param source
     * @param row
     */
    void processPlotLevelData(DeprecatedAdvancingSource source, MeasurementRow row) throws FieldbookException;
}
