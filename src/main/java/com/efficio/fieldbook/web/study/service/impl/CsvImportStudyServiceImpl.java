package com.efficio.fieldbook.web.study.service.impl;

import com.efficio.fieldbook.web.common.bean.ChangeType;
import com.efficio.fieldbook.web.study.service.ImportStudyService;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Transactional
public class CsvImportStudyServiceImpl  extends AbstractCSVImportStudyService implements ImportStudyService {
	
	@Resource
	protected AutowireCapableBeanFactory beanFactory;

    public enum CsvRequiredColumnEnum {
        ENTRY_NO(TermId.ENTRY_NO.getId(), "ENTRY_NO"), PLOT_NO(TermId.PLOT_NO.getId(), "PLOT_NO"), GID(TermId.GID.getId(),
                "GID"), DESIGNATION(TermId.DESIG.getId(), "DESIGNATION"), OBS_UNIT_ID(TermId.OBS_UNIT_ID.getId(), "OBS_UNIT_ID");

        private final Integer id;
        private final String label;

        private static final Map<Integer, CsvRequiredColumnEnum> LOOK_UP = new HashMap<>();

        static {
            for (final CsvRequiredColumnEnum cl : EnumSet.allOf(CsvRequiredColumnEnum.class)) {
                CsvRequiredColumnEnum.LOOK_UP.put(cl.getId(), cl);
            }
        }

        CsvRequiredColumnEnum(final Integer id, final String label) {
            this.id = id;
            this.label = label;
        }

        public Integer getId() {
            return this.id;
        }

        public String getLabel() {
            return this.label;
        }

        public static CsvRequiredColumnEnum get(final Integer id) {
            return CsvRequiredColumnEnum.LOOK_UP.get(id);
        }
    }

    public CsvImportStudyServiceImpl(final Workbook workbook, final String currentFile, final String originalFileName) {
        super(workbook, currentFile, originalFileName);
    }

    @Override
    protected String getLabelFromRequiredColumn(final MeasurementVariable variable) {
        String label = "";

        if (CsvRequiredColumnEnum.get(variable.getTermId()) != null) {
            label = CsvRequiredColumnEnum.get(variable.getTermId()).getLabel();
        }

        if (label.trim().length() > 0) {
            return label;
        }

        return variable.getName();
    }

    @Override
    void validateObservationColumns() throws WorkbookParserException {
        // validate headers
        final String[] rowHeaders = this.parsedData.get(0).toArray(new String[this.parsedData.get(0).size()]);

        if (!this.isValidHeaderNames(rowHeaders)) {
            throw new WorkbookParserException("error.workbook.import.requiredColumnsMissing");
        }
    }

    boolean isValidHeaderNames(final String[] rowHeaders) {
        final List<String> rowHeadersList = Arrays.asList(rowHeaders);

        for (final CsvRequiredColumnEnum column : CsvRequiredColumnEnum.values()) {
            if (!rowHeadersList.contains(column.getLabel().trim())) {
                return false;
            }
        }
        return true;
    }

	@Override
	protected void detectAddedTraitsAndPerformRename(final Set<ChangeType> modes, final List<String> addedVariates,
			final List<String> removedVariates) {
		// NO-OP
		
	}
}
