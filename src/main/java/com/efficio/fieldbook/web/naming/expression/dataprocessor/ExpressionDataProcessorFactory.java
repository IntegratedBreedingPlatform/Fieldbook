package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.trial.bean.AdvancingStudy;
import org.generationcp.middleware.ruleengine.pojo.AdvancingSource;
import org.generationcp.commons.spring.util.ComponentFactory;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;

import java.util.ArrayList;
import java.util.List;

public class ExpressionDataProcessorFactory implements ComponentFactory<ExpressionDataProcessor> {
    
    private List<ExpressionDataProcessor> dataProcessorList;

    public ExpressionDataProcessorFactory() {
        dataProcessorList = new ArrayList<>();
    }

    @Override
    public void addComponent(ExpressionDataProcessor expressionDataProcessor) {
        dataProcessorList.add(expressionDataProcessor);
    }

    public List<ExpressionDataProcessor> getDataProcessorList() {
        return dataProcessorList;
    }

    public ExpressionDataProcessor retrieveExecutorProcessor() {
        // DEV NOTE : in the future, we could possibly streamline the data processing flow by providing
        // a different processor that performs filtering. e.g. specify that some processors should only be used for
        // a target crop / program, etc
        return new ExecuteAllAvailableDataProcessor();
    }
    
    class ExecuteAllAvailableDataProcessor implements ExpressionDataProcessor {
        @Override
        public void processEnvironmentLevelData(AdvancingSource source, Workbook workbook, AdvancingStudy nurseryInfo, Study study) throws FieldbookException {
            for (ExpressionDataProcessor expressionDataProcessor : dataProcessorList) {
                expressionDataProcessor.processEnvironmentLevelData(source, workbook, nurseryInfo, study);
            }
        }

        @Override
        public void processPlotLevelData(AdvancingSource source, MeasurementRow row) throws FieldbookException {
            for (ExpressionDataProcessor expressionDataProcessor : dataProcessorList) {
                expressionDataProcessor.processPlotLevelData(source, row);
            }
        }
    }
}
