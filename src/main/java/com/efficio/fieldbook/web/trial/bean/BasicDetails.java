
package com.efficio.fieldbook.web.trial.bean;

import org.generationcp.middleware.domain.study.StudyTypeDto;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 7/8/2014 Time: 5:14 PM
 */
public class BasicDetails implements TabInfoBean {

	private Map<String, String> basicDetails;

	private Integer folderId;
	private String folderName;
	private String folderNameLabel;
	private String userName;
	private Integer userID;
	private Integer studyID;
	private String description;
	private StudyTypeDto studyType;
	private String startDate;
	private String endDate;
	private String studyUpdate;
	private String objective;
	private String studyName;
	private String createdBy;  
	private Boolean isLocked;

	public BasicDetails() {
		this.basicDetails = new HashMap<>();
	}

	public Map<String, String> getBasicDetails() {
		return this.basicDetails;
	}

	public void setBasicDetails(final Map<String, String> basicDetails) {
		this.basicDetails = basicDetails;
	}

	public Integer getFolderId() {
		return this.folderId;
	}

	public void setFolderId(final Integer folderId) {
		this.folderId = folderId;
	}

	public String getFolderName() {
		return this.folderName;
	}

	public void setFolderName(final String folderName) {
		this.folderName = folderName;
	}

	public String getFolderNameLabel() {
		return this.folderNameLabel;
	}

	public void setFolderNameLabel(final String folderNameLabel) {
		this.folderNameLabel = folderNameLabel;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(final String userName) {
		this.userName = userName;
	}

	public Integer getUserID() {
		return this.userID;
	}

	public void setUserID(final Integer userID) {
		this.userID = userID;
	}

	public Integer getStudyID() {
		return this.studyID;
	}

	public void setStudyID(final Integer studyID) {
		this.studyID = studyID;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}


	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(final String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return this.endDate;
	}

	public void setEndDate(final String endDate) {
		this.endDate = endDate;
	}

	public String getStudyUpdate() {
		return this.studyUpdate;
	}

	public void setStudyUpdate(final String studyUpdate) {
		this.studyUpdate = studyUpdate;
	}

	public String getObjective() {
		return objective;
	}

	public void setObjective(final String objective) {
		this.objective = objective;
	}

	public String getStudyName() {
		return studyName;
	}

	public void setStudyName(final String studyName) {
		this.studyName = studyName;
	}

	public String getCreatedBy() {
		return this.createdBy;
	}

	public void setCreatedBy(final String createdBy) {
		this.createdBy = createdBy;
	}

	public void setStudyType(final StudyTypeDto studyType) {
		this.studyType = studyType;
	}

	public StudyTypeDto getStudyType() {
		return studyType;
	}

	
	public Boolean getIsLocked() {
		return isLocked;
	}

	
	public void setisLocked(Boolean isLocked) {
		this.isLocked = isLocked;
	}

}
