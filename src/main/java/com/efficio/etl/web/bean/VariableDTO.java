
package com.efficio.etl.web.bean;

import java.util.Set;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.etl.web.util.AppConstants;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public class VariableDTO {
	private String alias;
	private String variable;
	private String description;
	private String property;
	private String propertyDescription;
	private Integer propertyId;
	private String scale;
	private String scaleDescription;
	private Integer scaleId;
	private String method;
	private String methodDescription;
	private Integer methodId;
	private String propertyClass;
	private Integer storedInId;
	private Integer dataType;
	private Integer id;
	private String phenotype;

	private Boolean valid;
	private Boolean confirmed;
	private Boolean hasError;
	private String status;

	private String headerName;

	private static final Logger LOG = LoggerFactory.getLogger(VariableDTO.class);

	public VariableDTO() {
	}

	public VariableDTO(final StandardVariable var) throws Exception {
		if (var != null) {
			try {
				this.variable = var.getName();
				this.alias = var.getAlias();

				this.property = var.getProperty().getName();
				this.propertyId = var.getProperty().getId();

				this.scale = var.getScale().getName();
				this.scaleId = var.getScale().getId();

				this.method = var.getMethod().getName();
				this.methodId = var.getMethod().getId();

				this.description = var.getDescription();
				this.propertyDescription = var.getProperty().getDefinition();
				this.scaleDescription = var.getScale().getDefinition();
				this.methodDescription = var.getMethod().getDefinition();

				this.dataType = var.getDataType().getId();
				this.phenotype = this.mapToPhenotype(var.getPhenotypicType());

				this.id = var.getId();
			} catch (final Exception e) {

				// this is to capture cases where there are data problems in the
				// target database
				// log the error, and then leave the error handling to the
				// calling code

				// in general though, standard variables that cause this kind of
				// incident should be considered invalid and be replaced with
				// a blank dto
				VariableDTO.LOG.error("There is a data error on supplied standard variable with id : " + var.getId(), e);
				throw e;
			}
		}
	}

	public VariableDTO(final Variable variable) {
		this.variable = variable.getName();
		this.alias = variable.getAlias();
		this.property = variable.getProperty().getName();
		this.propertyId = variable.getProperty().getId();

		this.scale = variable.getScale().getName();
		this.scaleId = variable.getScale().getId();

		this.method = variable.getMethod().getName();
		this.methodId = variable.getMethod().getId();

		this.description = variable.getDefinition();
		this.propertyDescription = variable.getProperty().getDefinition();
		this.scaleDescription = variable.getScale().getDefinition();
		this.methodDescription = variable.getMethod().getDefinition();

		this.dataType = variable.getScale().getDataType().getId();
		this.phenotype = this.mapToPhenotype(variable.getVariableTypes());

		this.id = variable.getId();
	}

	protected String mapToPhenotype(final Set<VariableType> variableTypes) {
		if (variableTypes != null) {
			for (final VariableType variableType : variableTypes) {
				// we only need to return the first non-null phenotype
				String phenotype = this.mapToPhenotype(variableType.getRole());
				if (phenotype != null) {
					return phenotype;
				}
			}
		}
		return null;
	}

	private String mapToPhenotype(final PhenotypicType role) {
		String mappedPhenotype = null;
		if (role != null) {
			switch (role) {
				case TRIAL_ENVIRONMENT:
					mappedPhenotype = AppConstants.TYPE_TRIAL_ENVIRONMENT;
					break;
				case TRIAL_DESIGN:
					mappedPhenotype = AppConstants.TYPE_TRIAL_DESIGN;
					break;
				case GERMPLASM:
					mappedPhenotype = AppConstants.TYPE_GERMPLASM_ENTRY;
					break;
				case VARIATE:
					mappedPhenotype = AppConstants.TYPE_VARIATE;
					break;
				case ENTRY_DETAIL:
					mappedPhenotype = AppConstants.TYPE_ENTRY_DETAIL;
					break;
				default:
					break;
			}
		}
		return mappedPhenotype;
	}

	public String getVariable() {
		return this.variable;
	}

	public void setVariable(final String variable) {
		this.variable = variable;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getProperty() {
		return this.property;
	}

	public void setProperty(final String property) {
		this.property = property;
	}

	public String getScale() {
		return this.scale;
	}

	public void setScale(final String scale) {
		this.scale = scale;
	}

	public String getMethod() {
		return this.method;
	}

	public void setMethod(final String method) {
		this.method = method;
	}

	public String getPropertyClass() {
		return this.propertyClass;
	}

	public void setPropertyClass(final String propertyClass) {
		this.propertyClass = propertyClass;
	}

	public void setStoredInId(final Integer storedInId) {
		this.storedInId = storedInId;
	}

	public Integer getDataType() {
		return this.dataType;
	}

	public void setDataType(final Integer dataType) {
		this.dataType = dataType;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public String getHeaderName() {
		return this.headerName;
	}

	public void setHeaderName(final String headerName) {
		this.headerName = headerName;
	}

	public Boolean getValid() {
		return this.valid;
	}

	public void setValid(final Boolean valid) {
		this.valid = valid;
	}

	public String getPropertyDescription() {
		return this.propertyDescription;
	}

	public void setPropertyDescription(final String propertyDescription) {
		this.propertyDescription = propertyDescription;
	}

	public String getScaleDescription() {
		return this.scaleDescription;
	}

	public void setScaleDescription(final String scaleDescription) {
		this.scaleDescription = scaleDescription;
	}

	public String getMethodDescription() {
		return this.methodDescription;
	}

	public void setMethodDescription(final String methodDescription) {
		this.methodDescription = methodDescription;
	}

	public Integer getStoredInId() {
		return this.storedInId;
	}

	public void populateMeasurementVariable(final MeasurementVariable var) {
		// this is more important in the wizard option
		var.setTermId(this.id);

		var.setName(this.getHeaderName());
		var.setDescription(this.getDescription());
		var.setMethod(this.getMethod());
		var.setProperty(this.getProperty());
		var.setScale(this.getScale());
		var.setRole(this.getRole());
		if (var.getRole() != null) {
			var.setLabel(var.getRole().getLabelList().get(0));
		}
	}

	private PhenotypicType getRole() {
		if (this.phenotype != null) {
			if (AppConstants.TYPE_TRIAL_ENVIRONMENT.equals(this.phenotype)) {
				return PhenotypicType.TRIAL_ENVIRONMENT;
			} else if (AppConstants.TYPE_TRIAL_DESIGN.equals(this.phenotype)) {
				return PhenotypicType.TRIAL_DESIGN;
			} else if (AppConstants.TYPE_GERMPLASM_ENTRY.equals(this.phenotype)) {
				return PhenotypicType.GERMPLASM;
			} else if (AppConstants.TYPE_VARIATE.equals(this.phenotype)) {
				return PhenotypicType.VARIATE;
			} else if (AppConstants.TYPE_ENTRY_DETAIL.equals(this.phenotype)) {
				return PhenotypicType.ENTRY_DETAIL;
			}
		}
		return null;
	}

	public String getPhenotype() {
		return this.phenotype;
	}

	public void setPhenotype(final String phenotype) {
		this.phenotype = phenotype;
	}

	public Boolean getConfirmed() {
		return this.confirmed;
	}

	public void setConfirmed(final Boolean confirmed) {
		this.confirmed = confirmed;
	}

	public Boolean getHasError() {
		return this.hasError;
	}

	public void setHasError(final Boolean hasError) {
		this.hasError = hasError;
	}

	public Integer getPropertyId() {
		return this.propertyId;
	}

	public void setPropertyId(final Integer propertyId) {
		this.propertyId = propertyId;
	}

	public Integer getScaleId() {
		return this.scaleId;
	}

	public void setScaleId(final Integer scaleId) {
		this.scaleId = scaleId;
	}

	public Integer getMethodId() {
		return this.methodId;
	}

	public void setMethodId(final Integer methodId) {
		this.methodId = methodId;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public String getAlias() {
		return this.alias;
	}

	public void setAlias(final String alias) {
		this.alias = alias;
	}
}
