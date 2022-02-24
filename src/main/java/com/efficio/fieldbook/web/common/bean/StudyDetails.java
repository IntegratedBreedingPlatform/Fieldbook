/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.common.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.etl.ExperimentalDesignVariable;

/**
 * The Class StudyDetails.
 *
 * @author Chezka Camille Arevalo
 */
public class StudyDetails {

	private Integer id;

	/** The program_uuid */
	private String programUUID;

	/** The name. */
	private String name;

	/** The title. */
	private String title;

	/** The objective. */
	private String objective;

	/** The start date. */
	private String startDate;

	/** The end date. */
	private String endDate;

	/** The principal investigator. */
	private String principalInvestigator;

	/** The site name. */
	private String siteName;
	
	private Boolean isLocked;
	
	private Integer ownerId;

	private List<SettingDetail> basicStudyDetails;

	private List<SettingDetail> managementDetails;

	private List<SettingDetail> studyConditionDetails;

	private List<SettingDetail> factorDetails;

	private List<SettingDetail> variateDetails;

	private List<SettingDetail> selectionVariateDetails;

	private List<SettingDetail> environmentManagementDetails;

	private List<SettingDetail> germplasmDescriptors;

	private List<SettingDetail> entryDetails;


	private List<TreatmentFactorDetail> treatmentFactorDetails;

	private boolean hasMeasurements;

	private int numberOfEnvironments;

	private ExperimentalDesignVariable experimentalDesignDetails;

	private Map<Integer, SettingDetail> factorsMap;

	private String errorMessage;

	/**
	 * Instantiates a new study details.
	 */
	public StudyDetails() {

	}

	/**
	 * Instantiates a new study details.
	 *
	 * @param name the name
	 * @param title the title
	 * @param objective the objective
	 * @param startDate the start date
	 * @param endDate the end date
	 * @param principalInvestigator the principal investigator
	 * @param siteName the site name
	 */
	public StudyDetails(
		final String name, final String title, final String objective, final String startDate, final String endDate, final String principalInvestigator,
			final String siteName) {
		this.name = name;
		this.title = title;
		this.objective = objective;
		this.startDate = startDate;
		this.endDate = endDate;
		this.principalInvestigator = principalInvestigator;
		this.siteName = siteName;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Gets the title.
	 *
	 * @return the title
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Sets the title.
	 *
	 * @param title the new title
	 */
	public void setTitle(final String title) {
		this.title = title;
	}

	/**
	 * Gets the objective.
	 *
	 * @return the objective
	 */
	public String getObjective() {
		return this.objective;
	}

	/**
	 * Sets the objective.
	 *
	 * @param objective the new objective
	 */
	public void setObjective(final String objective) {
		this.objective = objective;
	}

	/**
	 * Gets the start date.
	 *
	 * @return the start date
	 */
	public String getStartDate() {
		return this.startDate;
	}

	/**
	 * Sets the start date.
	 *
	 * @param startDate the new start date
	 */
	public void setStartDate(final String startDate) {
		this.startDate = startDate;
	}

	/**
	 * Gets the end date.
	 *
	 * @return the end date
	 */
	public String getEndDate() {
		return this.endDate;
	}

	/**
	 * Sets the end date.
	 *
	 * @param endDate the new end date
	 */
	public void setEndDate(final String endDate) {
		this.endDate = endDate;
	}

	/**
	 * Gets the principal investigator.
	 *
	 * @return the principal investigator
	 */
	public String getPrincipalInvestigator() {
		return this.principalInvestigator;
	}

	/**
	 * Sets the principal investigator.
	 *
	 * @param principalInvestigator the new principal investigator
	 */
	public void setPrincipalInvestigator(final String principalInvestigator) {
		this.principalInvestigator = principalInvestigator;
	}

	/**
	 * Gets the site name.
	 *
	 * @return the site name
	 */
	public String getSiteName() {
		return this.siteName;
	}

	/**
	 * Sets the site name.
	 *
	 * @param siteName the new site name
	 */
	public void setSiteName(final String siteName) {
		this.siteName = siteName;
	}

	/**
	 * @return the basicStudyDetails
	 */
	public List<SettingDetail> getBasicStudyDetails() {
		return this.basicStudyDetails;
	}

	/**
	 * @param basicStudyDetails the basicStudyDetails to set
	 */
	public void setBasicStudyDetails(final List<SettingDetail> basicStudyDetails) {
		this.basicStudyDetails = basicStudyDetails;
	}

	/**
	 * @return the managementDetails
	 */
	public List<SettingDetail> getManagementDetails() {
		return this.managementDetails;
	}

	/**
	 * @param managementDetails the managementDetails to set
	 */
	public void setManagementDetails(final List<SettingDetail> managementDetails) {
		this.managementDetails = managementDetails;
	}

	/**
	 * @return the factorDetails
	 */
	public List<SettingDetail> getFactorDetails() {
		return this.factorDetails;
	}

	/**
	 * @param factorDetails the factorDetails to set
	 */
	public void setFactorDetails(final List<SettingDetail> factorDetails) {
		this.factorDetails = factorDetails;
	}

	/**
	 * @return the variateDetails
	 */
	public List<SettingDetail> getVariateDetails() {
		return this.variateDetails;
	}

	/**
	 * @param variateDetails the variateDetails to set
	 */
	public void setVariateDetails(final List<SettingDetail> variateDetails) {
		this.variateDetails = variateDetails;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return this.id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(final Integer id) {
		this.id = id;
	}

	/**
	 * @return the studyConditionDetails
	 */
	public List<SettingDetail> getStudyConditionDetails() {
		return this.studyConditionDetails;
	}

	/**
	 * @param studyConditionDetails the studyConditionDetails to set
	 */
	public void setStudyConditionDetails(final List<SettingDetail> studyConditionDetails) {
		this.studyConditionDetails = studyConditionDetails;
	}

	/**
	 * @return the selectionVariateDetails
	 */
	public List<SettingDetail> getSelectionVariateDetails() {
		return this.selectionVariateDetails;
	}

	/**
	 * @param selectionVariateDetails the selectionVariateDetails to set
	 */
	public void setSelectionVariateDetails(final List<SettingDetail> selectionVariateDetails) {
		this.selectionVariateDetails = selectionVariateDetails;
	}

	/**
	 * @return the hasMeasurements
	 */
	public boolean isHasMeasurements() {
		return this.hasMeasurements;
	}

	/**
	 * @param hasMeasurements the hasMeasurements to set
	 */
	public void setHasMeasurements(final boolean hasMeasurements) {
		this.hasMeasurements = hasMeasurements;
	}

	/**
	 * @return the environmentManagementDetails
	 */
	public List<SettingDetail> getEnvironmentManagementDetails() {
		return this.environmentManagementDetails;
	}

	/**
	 * @param environmentManagementDetails the environmentManagementDetails to set
	 */
	public void setEnvironmentManagementDetails(final List<SettingDetail> environmentManagementDetails) {
		this.environmentManagementDetails = environmentManagementDetails;
	}

	/**
	 * @return the germplasmDescriptors
	 */
	public List<SettingDetail> getGermplasmDescriptors() {
		return this.germplasmDescriptors;
	}

	/**
	 * @param germplasmDescriptors the germplasmDescriptors to set
	 */
	public void setGermplasmDescriptors(final List<SettingDetail> germplasmDescriptors) {
		this.germplasmDescriptors = germplasmDescriptors;
	}

	/**
	 * @return the treatmentFactorDetails
	 */
	public List<TreatmentFactorDetail> getTreatmentFactorDetails() {
		return this.treatmentFactorDetails;
	}

	/**
	 * @param treatmentFactorDetails the treatmentFactorDetails to set
	 */
	public void setTreatmentFactorDetails(final List<TreatmentFactorDetail> treatmentFactorDetails) {
		this.treatmentFactorDetails = treatmentFactorDetails;
	}

	/**
	 * @return the numberOfEnvironments
	 */
	public int getNumberOfEnvironments() {
		return this.numberOfEnvironments;
	}

	/**
	 * @param numberOfEnvironments the numberOfEnvironments to set
	 */
	public void setNumberOfEnvironments(final int numberOfEnvironments) {
		this.numberOfEnvironments = numberOfEnvironments;
	}

	/**
	 * @return the experimentalDesignDetails
	 */
	public ExperimentalDesignVariable getExperimentalDesignDetails() {
		return this.experimentalDesignDetails;
	}

	/**
	 * @param experimentalDesignDetails the experimentalDesignDetails to set
	 */
	public void setExperimentalDesignDetails(final ExperimentalDesignVariable experimentalDesignDetails) {
		this.experimentalDesignDetails = experimentalDesignDetails;
	}

	public List<SettingDetail> getEntryDetails() {
		return this.entryDetails;
	}

	public void setEntryDetails(final List<SettingDetail> entryDetails) {
		this.entryDetails = entryDetails;
	}

	public Map<Integer, SettingDetail> getFactorsMap() {
		if (this.factorsMap == null) {
			this.factorsMap = new HashMap<Integer, SettingDetail>();
			if (this.factorDetails != null) {
				this.buildFactorsMapFromSettingDetailsList(this.factorDetails);
			}
			if (this.germplasmDescriptors != null) {
				this.buildFactorsMapFromSettingDetailsList(this.germplasmDescriptors);
			}
			if (this.entryDetails != null) {
				this.buildFactorsMapFromSettingDetailsList(this.entryDetails);
			}
		}
		return this.factorsMap;
	}

	private void buildFactorsMapFromSettingDetailsList(final List<SettingDetail> settingDetails) {
		for (final SettingDetail settingDetail : settingDetails) {
			if (settingDetail.getVariable() != null) {
				this.factorsMap.put(settingDetail.getVariable().getCvTermId(), settingDetail);
			}
		}
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}

	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getProgramUUID() {
		return this.programUUID;
	}

	public void setProgramUUID(final String programUUID) {
		this.programUUID = programUUID;
	}

	
	public Boolean getIsLocked() {
		return isLocked;
	}

	
	public void setIsLocked(Boolean isLocked) {
		this.isLocked = isLocked;
	}

	
	public Integer getOwnerId() {
		return ownerId;
	}

	
	public void setOwnerId(Integer ownerId) {
		this.ownerId = ownerId;
	}

}
