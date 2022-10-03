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

package com.efficio.fieldbook.web.trial.form;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.generationcp.middleware.ruleengine.pojo.ImportedGermplasm;
import org.generationcp.middleware.ruleengine.pojo.AdvancingSource;
import org.generationcp.middleware.domain.oms.StandardVariableReference;

/**
 * The Class AdvancingStudyForm.
 */
public class AdvancingStudyForm {

	/** The method choice. */
	private String methodChoice;

	/** The line choice. */
	private String lineChoice;

	/** The line selected. */
	private String lineSelected;

	/** The harvest date. */
	private String harvestDate;

	/** The harvest location. */
	private String harvestLocation;

	/** The field location id all. */
	private String harvestLocationIdAll;

	/** The field location id breeding. */
	private String harvestLocationIdBreeding;

	/** The field location id breeding. */
	private String harvestLocationIdBreedingFavorites;

	/** The field location id favorite. */
	private String harvestLocationIdFavorite;

	/** The harvest location id. */
	private String harvestLocationId;

	/** The harvest location name. */
	private String harvestLocationName;

	/** The harvest location abbreviation. */
	private String harvestLocationAbbreviation;

	/** The default method id. */
	private String defaultMethodId;

	/** The breeding method id. */
	private String advanceBreedingMethodId;

	/** The field location id all. */
	private String methodIdAll;

	/** The field location id Derivative And Maintenance */
	private String methodIdDerivativeAndMaintenance;

	/** The field location id Derivative And Maintenance Favorite */
	private String methodIdDerivativeAndMaintenanceFavorite;

	/** The field location id favorite. */
	private String methodIdFavorite;

	/** The project id. */
	private String projectId;

	/** The nursery advance name. */
	private String nurseryAdvanceName;

	/** The nursery advance description. */
	private String nurseryAdvanceDescription;

	/** The entries. */
	private int entries;

	/** The germplasm list. */
	private List<ImportedGermplasm> germplasmList;

	/** The paginated germplasm list. */
	private List<ImportedGermplasm> paginatedGermplasmList;

	/** The current page. */
	private int currentPage;

	/** The total pages. */
	private int totalPages;

	/** The result per page. */
	private int resultPerPage = 100;

	/** The plots with plants selected. */
	private int plotsWithPlantsSelected;

	/** The location url. */
	private String locationUrl;

	/** The breeding method url. */
	private String breedingMethodUrl;

	/** The study id. */
	private String studyId;

	private Integer methodVariateId;

	private Integer lineVariateId;

	private List<StandardVariableReference> methodVariates;

	private List<StandardVariableReference> lineVariates;

	private String harvestYear;

    private String harvestMonth;

	private Long uniqueId;

	private String allPlotsChoice;

	private List<StandardVariableReference> plotVariates;

	private Integer plotVariateId;

	private String errorInAdvance;

	private Set<String> selectedTrialInstances;

    private Set<String> selectedReplications;

	private String advanceType;

	private List<AdvancingSource> advancingSourceItems;

    
	public Set<String> getSelectedTrialInstances() {
		return this.selectedTrialInstances;
	}

	public void setSelectedTrialInstances(Set<String> selectedTrialInstances) {
		this.selectedTrialInstances = selectedTrialInstances;
	}

	
	
	public Set<String> getSelectedReplications() {
		return this.selectedReplications;
	}

	

	public void setSelectedReplications(Set<String> selectedReplications) {
		this.selectedReplications = selectedReplications;
	}

	/**
	 * Gets the method id all.
	 *
	 * @return the method id all
	 */
	public String getMethodIdAll() {
		return this.methodIdAll;
	}

	/**
	 * Sets the method id all.
	 *
	 * @param methodIdAll the new method id all
	 */
	public void setMethodIdAll(String methodIdAll) {
		this.methodIdAll = methodIdAll;
	}

	/**
	 * @return the methodIdDerivativeAndMaintenance
	 */
	public String getMethodIdDerivativeAndMaintenance() {
		return methodIdDerivativeAndMaintenance;
	}

	/**
	 * @param methodIdDerivativeAndMaintenance the methodIdDerivativeAndMaintenance to set
	 */
	public void setMethodIdDerivativeAndMaintenance(String methodIdDerivativeAndMaintenance) {
		this.methodIdDerivativeAndMaintenance = methodIdDerivativeAndMaintenance;
	}

	/**
	 * @return the methodIdDerivativeAndMaintenanceFavorite
	 */
	public String getMethodIdDerivativeAndMaintenanceFavorite() {
		return methodIdDerivativeAndMaintenanceFavorite;
	}

	/**
	 * @param methodIdDerivativeAndMaintenanceFavorite the methodIdDerivativeAndMaintenanceFavorite to set
	 */
	public void setMethodIdDerivativeAndMaintenanceFavorite(String methodIdDerivativeAndMaintenanceFavorite) {
		this.methodIdDerivativeAndMaintenanceFavorite = methodIdDerivativeAndMaintenanceFavorite;
	}

	/**
	 * Gets the method id favorite.
	 *
	 * @return the method id favorite
	 */
	public String getMethodIdFavorite() {
		return this.methodIdFavorite;
	}

	/**
	 * Sets the method id favorite.
	 *
	 * @param methodIdFavorite the new method id favorite
	 */
	public void setMethodIdFavorite(String methodIdFavorite) {
		this.methodIdFavorite = methodIdFavorite;
	}

	/**
	 * Gets the harvest location abbreviation.
	 *
	 * @return the harvest location abbreviation
	 */
	public String getHarvestLocationAbbreviation() {
		return this.harvestLocationAbbreviation;
	}

	/**
	 * Sets the harvest location abbreviation.
	 *
	 * @param harvestLocationAbbreviation the new harvest location abbreviation
	 */
	public void setHarvestLocationAbbreviation(String harvestLocationAbbreviation) {
		this.harvestLocationAbbreviation = harvestLocationAbbreviation;
	}

	/**
	 * Gets the default method id.
	 *
	 * @return the default method id
	 */
	public String getDefaultMethodId() {
		return this.defaultMethodId;
	}

	/**
	 * Sets the default method id.
	 *
	 * @param defaultMethodId the new default method id
	 */
	public void setDefaultMethodId(String defaultMethodId) {
		this.defaultMethodId = defaultMethodId;
	}

	/**
	 * Gets the harvest location id all.
	 *
	 * @return the harvest location id all
	 */
	public String getHarvestLocationIdAll() {
		return this.harvestLocationIdAll;
	}

	/**
	 * Sets the harvest location id all.
	 *
	 * @param harvestLocationIdAll the new harvest location id all
	 */
	public void setHarvestLocationIdAll(String harvestLocationIdAll) {
		this.harvestLocationIdAll = harvestLocationIdAll;
	}

	/**
	 * @return the harvestLocationIdBreeding
	 */
	public String getHarvestLocationIdBreeding() {
		return harvestLocationIdBreeding;
	}

	/**
	 * @param harvestLocationIdBreeding the harvestLocationIdBreeding to set
	 */
	public void setHarvestLocationIdBreeding(String harvestLocationIdBreeding) {
		this.harvestLocationIdBreeding = harvestLocationIdBreeding;
	}

	/**
	 * @return the harvestLocationIdBreedingFavorites
	 */
	public String getHarvestLocationIdBreedingFavorites() {
		return harvestLocationIdBreedingFavorites;
	}


	/**
	 * @param harvestLocationIdBreedingFavorites the harvestLocationIdBreedingFavorites to set
	 */
	public void setHarvestLocationIdBreedingFavorites(String harvestLocationIdBreedingFavorites) {
		this.harvestLocationIdBreedingFavorites = harvestLocationIdBreedingFavorites;
	}

	/**
	 * Gets the harvest location id favorite.
	 *
	 * @return the harvest location id favorite
	 */
	public String getHarvestLocationIdFavorite() {
		return this.harvestLocationIdFavorite;
	}

	/**
	 * Sets the harvest location id favorite.
	 *
	 * @param harvestLocationIdFavorite the new harvest location id favorite
	 */
	public void setHarvestLocationIdFavorite(String harvestLocationIdFavorite) {
		this.harvestLocationIdFavorite = harvestLocationIdFavorite;
	}

	/**
	 * Gets the harvest location id.
	 *
	 * @return the harvest location id
	 */
	public String getHarvestLocationId() {
		return this.harvestLocationId;
	}

	/**
	 * Sets the harvest location id.
	 *
	 * @param harvestLocationId the new harvest location id
	 */
	public void setHarvestLocationId(String harvestLocationId) {
		this.harvestLocationId = harvestLocationId;
	}

	/**
	 * Gets the harvest location name.
	 *
	 * @return the harvest location name
	 */
	public String getHarvestLocationName() {
		return this.harvestLocationName;
	}

	/**
	 * Sets the harvest location name.
	 *
	 * @param harvestLocationName the new harvest location name
	 */
	public void setHarvestLocationName(String harvestLocationName) {
		this.harvestLocationName = harvestLocationName;
	}

	/**
	 * Gets the harvest date.
	 *
	 * @return the harvest date
	 */
	public String getHarvestDate() {
		return this.harvestDate;
	}

	/**
	 * Sets the harvest date.
	 *
	 * @param harvestDate the new harvest date
	 */
	public void setHarvestDate(String harvestDate) {
		this.harvestDate = harvestDate;
	}

	/**
	 * Gets the harvest location.
	 *
	 * @return the harvest location
	 */
	public String getHarvestLocation() {
		return this.harvestLocation;
	}

	/**
	 * Sets the harvest location.
	 *
	 * @param harvestLocation the new harvest location
	 */
	public void setHarvestLocation(String harvestLocation) {
		this.harvestLocation = harvestLocation;
	}

	/**
	 * Gets the breeding method id.
	 *
	 * @return the breeding method id
	 */
	public String getAdvanceBreedingMethodId() {
		return this.advanceBreedingMethodId;
	}

	/**
	 * Sets the breeding method id.
	 *
	 * @param breedingMethodId the new breeding method id
	 */
	public void setAdvanceBreedingMethodId(String breedingMethodId) {
		this.advanceBreedingMethodId = breedingMethodId;
	}

	/**
	 * Gets the method choice.
	 *
	 * @return the method choice
	 */
	public String getMethodChoice() {
		return this.methodChoice;
	}

	/**
	 * Sets the method choice.
	 *
	 * @param methodChoice the new method choice
	 */
	public void setMethodChoice(String methodChoice) {
		this.methodChoice = methodChoice;
	}

	/**
	 * Gets the line choice.
	 *
	 * @return the line choice
	 */
	public String getLineChoice() {
		return this.lineChoice;
	}

	/**
	 * Sets the line choice.
	 *
	 * @param lineChoice the new line choice
	 */
	public void setLineChoice(String lineChoice) {
		this.lineChoice = lineChoice;
	}

	/**
	 * Gets the line selected.
	 *
	 * @return the line selected
	 */
	public String getLineSelected() {
		return this.lineSelected;
	}

	/**
	 * Sets the line selected.
	 *
	 * @param lineSelected the new line selected
	 */
	public void setLineSelected(String lineSelected) {
		this.lineSelected = lineSelected;
	}

	/**
	 * Gets the project id.
	 *
	 * @return the project id
	 */
	public String getProjectId() {
		return this.projectId;
	}

	/**
	 * Sets the project id.
	 *
	 * @param projectId the new project id
	 */
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	/**
	 * Gets the nursery advance name.
	 *
	 * @return the nursery advance name
	 */
	public String getNurseryAdvanceName() {
		return this.nurseryAdvanceName;
	}

	/**
	 * Sets the nursery advance name.
	 *
	 * @param nurseryAdvanceName the new nursery advance name
	 */
	public void setNurseryAdvanceName(String nurseryAdvanceName) {
		this.nurseryAdvanceName = nurseryAdvanceName;
	}

	/**
	 * Gets the nursery advance description.
	 *
	 * @return the nursery advance description
	 */
	public String getNurseryAdvanceDescription() {
		return this.nurseryAdvanceDescription;
	}

	/**
	 * Sets the nursery advance description.
	 *
	 * @param nurseryAdvanceDescription the new nursery advance description
	 */
	public void setNurseryAdvanceDescription(String nurseryAdvanceDescription) {
		this.nurseryAdvanceDescription = nurseryAdvanceDescription;
	}

	/**
	 * Gets the entries.
	 *
	 * @return the entries
	 */
	public int getEntries() {
		return this.entries;
	}

	/**
	 * Sets the entries.
	 *
	 * @param entries the new entries
	 */
	public void setEntries(int entries) {
		this.entries = entries;
	}

	/**
	 * Gets the germplasm list.
	 *
	 * @return the germplasm list
	 */
	public List<ImportedGermplasm> getGermplasmList() {
		return this.germplasmList;
	}

	/**
	 * Sets the germplasm list.
	 *
	 * @param germplasmList the new germplasm list
	 */
	public void setGermplasmList(List<ImportedGermplasm> germplasmList) {
		this.germplasmList = germplasmList;
	}

	/**
	 * Gets the paginated germplasm list.
	 *
	 * @return the paginated germplasm list
	 */
	public List<ImportedGermplasm> getPaginatedGermplasmList() {
		return this.paginatedGermplasmList;
	}

	/**
	 * Sets the paginated germplasm list.
	 *
	 * @param paginatedGermplasmList the new paginated germplasm list
	 */
	public void setPaginatedGermplasmList(List<ImportedGermplasm> paginatedGermplasmList) {
		this.paginatedGermplasmList = paginatedGermplasmList;
	}

	/**
	 * Gets the current page.
	 *
	 * @return the current page
	 */
	public int getCurrentPage() {
		return this.currentPage;
	}

	/**
	 * Sets the current page.
	 *
	 * @param currentPage the new current page
	 */
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	/**
	 * Gets the total pages.
	 *
	 * @return the total pages
	 */
	public int getTotalPages() {
		if (this.germplasmList != null && !this.germplasmList.isEmpty()) {
			this.totalPages = (int) Math.ceil(this.germplasmList.size() * 1f / this.getResultPerPage());
		} else {
			this.totalPages = 0;
		}
		return this.totalPages;
	}

	/**
	 * Sets the total pages.
	 *
	 * @param totalPages the new total pages
	 */
	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	/**
	 * Gets the result per page.
	 *
	 * @return the result per page
	 */
	public int getResultPerPage() {
		return this.resultPerPage;
	}

	/**
	 * Sets the result per page.
	 *
	 * @param resultPerPage the new result per page
	 */
	public void setResultPerPage(int resultPerPage) {
		this.resultPerPage = resultPerPage;
	}

	/**
	 * Change page.
	 *
	 * @param currentPage the current page
	 */
	public void changePage(int currentPage) {
		// assumption is there is an imported germplasm already
		if (this.germplasmList != null && !this.germplasmList.isEmpty()) {
			int totalItemsPerPage = this.getResultPerPage();
			int start = (currentPage - 1) * totalItemsPerPage;
			int end = start + totalItemsPerPage;
			if (this.germplasmList.size() < end) {
				end = this.germplasmList.size();
			}
			this.paginatedGermplasmList = this.germplasmList.subList(start, end);
			this.currentPage = currentPage;
		} else {
			this.currentPage = 0;
		}
	}

	/**
	 * Gets the plots with plants selected.
	 *
	 * @return the plots with plants selected
	 */
	public int getPlotsWithPlantsSelected() {
		return this.plotsWithPlantsSelected;
	}

	/**
	 * Sets the plots with plants selected.
	 *
	 * @param plotsWithPlantsSelected the new plots with plants selected
	 */
	public void setPlotsWithPlantsSelected(int plotsWithPlantsSelected) {
		this.plotsWithPlantsSelected = plotsWithPlantsSelected;
	}

	/**
	 * Gets the location url.
	 *
	 * @return the location url
	 */
	public String getLocationUrl() {
		return this.locationUrl;
	}

	/**
	 * Sets the location url.
	 *
	 * @param locationUrl the new location url
	 */
	public void setLocationUrl(String locationUrl) {
		this.locationUrl = locationUrl;
	}

	/**
	 * Gets the breeding method url.
	 *
	 * @return the breeding method url
	 */
	public String getBreedingMethodUrl() {
		return this.breedingMethodUrl;
	}

	/**
	 * Sets the breeding method url.
	 *
	 * @param breedingMethodUrl the new breeding method url
	 */
	public void setBreedingMethodUrl(String breedingMethodUrl) {
		this.breedingMethodUrl = breedingMethodUrl;
	}

	/**
	 * Gets the nursery id.
	 *
	 * @return the nursery id
	 */
	public String getStudyId() {
		return this.studyId;
	}

	/**
	 * Sets the study id.
	 *
	 * @param studyId the new study id
	 */
	public void setStudyId(final String studyId) {
		this.studyId = studyId;
	}

	/**
	 * @return the methodVariateId
	 */
	public Integer getMethodVariateId() {
		return this.methodVariateId;
	}

	/**
	 * @param methodVariateId the methodVariateId to set
	 */
	public void setMethodVariateId(Integer methodVariateId) {
		this.methodVariateId = methodVariateId;
	}

	/**
	 * @return the lineVariateId
	 */
	public Integer getLineVariateId() {
		return this.lineVariateId;
	}

	/**
	 * @param lineVariateId the lineVariateId to set
	 */
	public void setLineVariateId(Integer lineVariateId) {
		this.lineVariateId = lineVariateId;
	}

	/**
	 * @return the methodVariates
	 */
	public List<StandardVariableReference> getMethodVariates() {
		return this.methodVariates;
	}

	/**
	 * @param methodVariates the methodVariates to set
	 */
	public void setMethodVariates(List<StandardVariableReference> methodVariates) {
		this.methodVariates = methodVariates;
	}

	/**
	 * @return the lineVariates
	 */
	public List<StandardVariableReference> getLineVariates() {
		return this.lineVariates;
	}

	/**
	 * @param lineVariates the lineVariates to set
	 */
	public void setLineVariates(List<StandardVariableReference> lineVariates) {
		this.lineVariates = lineVariates;
	}

	/**
	 * @return the harvestYear
	 */
	public String getHarvestYear() {
		return this.harvestYear;
	}

	/**
	 * @param harvestYear the harvestYear to set
	 */
	public void setHarvestYear(String harvestYear) {
		this.harvestYear = harvestYear;
	}

	/**
	 * @return the harvestMonth
	 */
	public String getHarvestMonth() {
		return this.harvestMonth;
	}

	/**
	 * @param harvestMonth the harvestMonth to set
	 */
	public void setHarvestMonth(String harvestMonth) {
		this.harvestMonth = harvestMonth;
	}

	/**
	 * @return the uniqueId
	 */
	public Long getUniqueId() {
		return this.uniqueId;
	}

	/**
	 * @param uniqueId the uniqueId to set
	 */
	public void setUniqueId(Long uniqueId) {
		this.uniqueId = uniqueId;
	}

	/**
	 * @return the allPlotsChoice
	 */
	public String getAllPlotsChoice() {
		return this.allPlotsChoice;
	}

	/**
	 * @param allPlotsChoice the allPlotsChoice to set
	 */
	public void setAllPlotsChoice(String allPlotsChoice) {
		this.allPlotsChoice = allPlotsChoice;
	}

	/**
	 * @return the plotVariates
	 */
	public List<StandardVariableReference> getPlotVariates() {
		return this.plotVariates;
	}

	/**
	 * @param plotVariates the plotVariates to set
	 */
	public void setPlotVariates(List<StandardVariableReference> plotVariates) {
		this.plotVariates = plotVariates;
	}

	/**
	 * @return the plotVariateId
	 */
	public Integer getPlotVariateId() {
		return this.plotVariateId;
	}

	/**
	 * @param plotVariateId the plotVariateId to set
	 */
	public void setPlotVariateId(Integer plotVariateId) {
		this.plotVariateId = plotVariateId;
	}

	/**
	 * @return the errorInAdvance
	 */
	public String getErrorInAdvance() {
		return this.errorInAdvance;
	}

	/**
	 * @param errorInAdvance the errorInAdvance to set
	 */
	public void setErrorInAdvance(String errorInAdvance) {
		this.errorInAdvance = errorInAdvance;
	}

	public String getAdvanceType() {
		return advanceType;
	}

	public void setAdvanceType(String advanceType) {
		this.advanceType = advanceType;
	}

	public List<AdvancingSource> getAdvancingSourceItems() {
		return advancingSourceItems;
	}

	public void setAdvancingSourceItems(List<AdvancingSource> advancingSourceItems) {
		this.advancingSourceItems = advancingSourceItems;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
