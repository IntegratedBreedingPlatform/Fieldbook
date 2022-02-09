
package com.efficio.fieldbook.web.util;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class ExportImportStudyUtil {

	private static final Logger LOG = LoggerFactory.getLogger(ExportImportStudyUtil.class);

	private static final Integer[] REQUIRED_COLUMNS = {TermId.PLOT_NO.getId(), TermId.ENTRY_NO.getId(), TermId.DESIG.getId()};

	private ExportImportStudyUtil() {
		// do nothing
	}

	/**
	 * Retrieves the standard value of categorical variables from its list of possible values
	 * 
	 * @param idValue - id value of one of the possible values of categorical variable (i.e 1 - Low, 2 - Medium, 3 - High; 1, 2 and 3 are
	 *        the possible id values)
	 * @param possibleValues - list of possible values of a categorical variable to search
	 * @return actual value based on description or name of the variable that represents the possible value that is searched
	 */
	public static String getCategoricalCellValue(final String idValue, final List<ValueReference> possibleValues) {
		// With the New Data Table, the idValue will contain the long text instead of the id.
		if (idValue != null && possibleValues != null && !possibleValues.isEmpty()) {
			for (final ValueReference possibleValue : possibleValues) {
				if (idValue.equalsIgnoreCase(possibleValue.getDescription())) {
					return possibleValue.getName();
				}
			}
		}
		// just in case an id was passed, but this won't be the case most of the time
		if (idValue != null && NumberUtils.isNumber(idValue)) {
			for (final ValueReference ref : possibleValues) {
				// Needs to convert to double to facilitate retrieving decimal value from categorical values
				if (Double.valueOf(ref.getId()).equals(Double.valueOf(idValue))) {
					return ref.getName();
				}
			}
		}
		return idValue;
	}

	public static String getCategoricalIdCellValue(final String description, final List<ValueReference> possibleValues) {
		return ExportImportStudyUtil.getCategoricalIdCellValue(description, possibleValues, false);
	}

	public static String getCategoricalIdCellValue(final String description, final List<ValueReference> possibleValues,
			final boolean isReturnOriginalValue) {
		if (description != null) {
			for (final ValueReference possibleValue : possibleValues) {
				if (description.equalsIgnoreCase(possibleValue.getName())) {
					return possibleValue.getId().toString();
				}
			}
		}
		return isReturnOriginalValue ? description : "";
	}

	public static List<Integer> getLocationIdsFromTrialInstances(final Workbook workbook, final List<Integer> instances) {
		final List<Integer> locationIds = new ArrayList<Integer>();

		final List<MeasurementVariable> trialVariables = workbook.getTrialVariables();
		String label = null;
		final List<MeasurementRow> trialObservations = workbook.getTrialObservations();
		if (trialVariables != null && instances != null && !instances.isEmpty()) {
			for (final MeasurementVariable trialVariable : trialVariables) {
				if (trialVariable.getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
					label = trialVariable.getName();
					break;
				}
			}
			if (trialObservations != null && label != null) {
				for (final MeasurementRow trialObservation : trialObservations) {
					final String trialInstanceString = trialObservation.getMeasurementDataValue(label);
					if (trialInstanceString != null && NumberUtils.isNumber(trialInstanceString)) {
						final int trialInstanceNumber = Double.valueOf(trialInstanceString).intValue();
						if (instances != null && instances.indexOf(Integer.valueOf(trialInstanceNumber)) != -1) {
							locationIds.add((int) trialObservation.getLocationId());
						}
					}
				}
			}
		}

		if (locationIds.isEmpty() && trialObservations != null) {
			for (final MeasurementRow trialObservation : trialObservations) {
				locationIds.add((int) trialObservation.getLocationId());
			}
		}

		return locationIds;
	}

	public static List<MeasurementRow> getApplicableObservations(final Workbook workbook, final List<MeasurementRow> observations,
			final List<Integer> instances) {
		List<MeasurementRow> rows = null;
		if (instances != null && !instances.isEmpty()) {
			rows = new ArrayList<MeasurementRow>();
			final List<Integer> locationIds = ExportImportStudyUtil.getLocationIdsFromTrialInstances(workbook, instances);
			for (final MeasurementRow row : observations) {
				if (locationIds.contains((int) row.getLocationId())) {
					rows.add(row);
				}
			}
		} else {
			rows = workbook.getObservations();
		}
		return rows;
	}

	public static String getSiteNameOfTrialInstance(final MeasurementRow trialObservation,
			final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {
		if (trialObservation != null && trialObservation.getMeasurementVariables() != null) {
			for (final MeasurementData data : trialObservation.getDataList()) {
				if (data.getMeasurementVariable().getTermId() == TermId.TRIAL_LOCATION.getId()) {
					return "_" + data.getValue();
				} else if (data.getMeasurementVariable().getTermId() == TermId.LOCATION_ID.getId()) {
					return ExportImportStudyUtil.getSiteNameOfTrialInstanceBasedOnLocationID(fieldbookMiddlewareService, data);
				}
			}
		}
		return "";
	}

	private static String getSiteNameOfTrialInstanceBasedOnLocationID(
			final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, final MeasurementData data) {
		if (data.getValue() != null && !data.getValue().isEmpty() && NumberUtils.isNumber(data.getValue())) {
			return "_" + fieldbookMiddlewareService.getLocationById(Integer.parseInt(data.getValue())).getLname();
		} else {
			return "";
		}
	}

	public static boolean partOfRequiredColumns(final int termId) {
		for (final int id : ExportImportStudyUtil.REQUIRED_COLUMNS) {
			if (termId == id) {
				return true;
			}
		}
		return false;
	}

	// FIXME : eliminate use - if this is called in a loop this is BAD
	public static String getPropertyName(final OntologyService ontologyService) {
		String propertyName = "";
		try {
			propertyName = ontologyService.getProperty(TermId.BREEDING_METHOD_PROP.getId()).getTerm().getName();
		} catch (final MiddlewareQueryException e) {
			ExportImportStudyUtil.LOG.error(e.getMessage(), e);
			throw e;
		}
		return propertyName;
	}

	public static boolean measurementVariableHasValue(final MeasurementData dataCell) {
		return dataCell.getMeasurementVariable() != null && dataCell.getMeasurementVariable().getPossibleValues() != null;
	}

	public static boolean isColumnVisible(final int termId, final List<Integer> visibleColumns) {
		if (visibleColumns == null) {
			return true;
		} else {
			return ExportImportStudyUtil.partOfRequiredColumns(termId) || visibleColumns.contains(termId);
		}
	}

}
