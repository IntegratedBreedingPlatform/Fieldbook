
package com.efficio.fieldbook.web.util;

import org.generationcp.commons.util.WorkbenchAppPathResolver;

public class FieldbookProperties {

	private String programLocationsUrl;
	private String programBreedingMethodsUrl;
	
	private String uploadDirectory;
	private Integer maxNumOfSubObsSetsPerStudy;
	private Integer maxNumOfSubObsPerParentUnit;
	private String staBrappUrl;

	public String getProgramLocationsUrl() {
		return WorkbenchAppPathResolver.getFullWebAddress(this.programLocationsUrl);
	}

	public void setProgramLocationsUrl(final String programLocationsUrl) {
		this.programLocationsUrl = programLocationsUrl;
	}

	public String getProgramBreedingMethodsUrl() {
		return WorkbenchAppPathResolver.getFullWebAddress(this.programBreedingMethodsUrl);
	}

	public void setProgramBreedingMethodsUrl(final String programBreedingMethodsUrl) {
		this.programBreedingMethodsUrl = programBreedingMethodsUrl;
	}

	public String getUploadDirectory() {
		return this.uploadDirectory;
	}

	public void setUploadDirectory(final String uploadDirectory) {
		this.uploadDirectory = uploadDirectory;
	}

	public Integer getMaxNumOfSubObsSetsPerStudy() {
		return this.maxNumOfSubObsSetsPerStudy;
	}

	public void setMaxNumOfSubObsSetsPerStudy(final Integer maxNumOfSubObsSetsPerStudy) {
		this.maxNumOfSubObsSetsPerStudy = maxNumOfSubObsSetsPerStudy;
	}

	public Integer getMaxNumOfSubObsPerParentUnit() {
		return this.maxNumOfSubObsPerParentUnit;
	}

	public void setMaxNumOfSubObsPerParentUnit(final Integer maxNumOfSubObsPerParentUnit) {
		this.maxNumOfSubObsPerParentUnit = maxNumOfSubObsPerParentUnit;
	}

	public String getStaBrappUrl() {
		return staBrappUrl;
	}

	public void setStaBrappUrl(final String staBrappUrl) {
		this.staBrappUrl = staBrappUrl;
	}
}
