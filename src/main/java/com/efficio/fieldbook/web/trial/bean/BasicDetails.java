
package com.efficio.fieldbook.web.trial.bean;

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

	public BasicDetails() {
		this.basicDetails = new HashMap<>();
	}

	public Map<String, String> getBasicDetails() {
		return this.basicDetails;
	}

	public void setBasicDetails(Map<String, String> basicDetails) {
		this.basicDetails = basicDetails;
	}

	public Integer getFolderId() {
		return this.folderId;
	}

	public void setFolderId(Integer folderId) {
		this.folderId = folderId;
	}

	public String getFolderName() {
		return this.folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public String getFolderNameLabel() {
		return this.folderNameLabel;
	}

	public void setFolderNameLabel(String folderNameLabel) {
		this.folderNameLabel = folderNameLabel;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Integer getUserID() {
		return this.userID;
	}

	public void setUserID(Integer userID) {
		this.userID = userID;
	}

	public Integer getStudyID() {
		return this.studyID;
	}

	public void setStudyID(Integer studyID) {
		this.studyID = studyID;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}
}
