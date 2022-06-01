
package com.efficio.fieldbook.web.util;

public class FieldbookProperties {

	private String uploadDirectory;
	private Integer maxNumOfSubObsSetsPerStudy;
	private Integer maxNumOfSubObsPerParentUnit;
	private String staBrappUrl;

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
