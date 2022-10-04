package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.trial.bean.AdvancingStudy;
import org.generationcp.middleware.ruleengine.pojo.AdvancingSource;
import org.generationcp.middleware.spring.util.ComponentFactory;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class DeprecatedExpressionDataProcessorFactory implements ComponentFactory<DeprecatedExpressionDataProcessor> {
    
    private List<DeprecatedExpressionDataProcessor> dataProcessorList;

    public DeprecatedExpressionDataProcessorFactory() {
        dataProcessorList = new ArrayList<>();
    }

    @Override
    public void addComponent(DeprecatedExpressionDataProcessor deprecatedExpressionDataProcessor) {
        dataProcessorList.add(deprecatedExpressionDataProcessor);
    }

    public List<DeprecatedExpressionDataProcessor> getDataProcessorList() {
        return dataProcessorList;
    }

    public DeprecatedExpressionDataProcessor retrieveExecutorProcessor() {
        // DEV NOTE : in the future, we could possibly streamline the data processing flow by providing
        // a different processor that performs filtering. e.g. specify that some processors should only be used for
        // a target crop / program, etc
        return new ExecuteAllAvailableDataProcessor();
    }
    
    class ExecuteAllAvailableDataProcessor implements DeprecatedExpressionDataProcessor {
        @Override
        public void processEnvironmentLevelData(AdvancingSource source, Workbook workbook, AdvancingStudy nurseryInfo, Study study) throws FieldbookException {
            for (DeprecatedExpressionDataProcessor deprecatedExpressionDataProcessor : dataProcessorList) {
                deprecatedExpressionDataProcessor.processEnvironmentLevelData(source, workbook, nurseryInfo, study);
            }
        }

        @Override
        public void processPlotLevelData(AdvancingSource source, MeasurementRow row) throws FieldbookException {
            for (DeprecatedExpressionDataProcessor deprecatedExpressionDataProcessor : dataProcessorList) {
                deprecatedExpressionDataProcessor.processPlotLevelData(source, row);
            }
        }
    }
}
