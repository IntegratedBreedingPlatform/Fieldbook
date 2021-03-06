
package com.efficio.fieldbook.web.common.bean;

import java.util.List;

public class GermplasmChangeDetail {

	private int index;
	private String originalDesig;
	private String originalGid;
	private String newDesig;
	private String newGid;
	private String message;
	private int status;
	private List<Integer> matchingGids;
	private Integer selectedGid;
	private String trialInstanceNumber;
	private String entryNumber;
	private String plotNumber;
	private int nameType;
	private int importLocationId;
	private int importMethodId;
	private String importDate;

	public GermplasmChangeDetail() {
	}

	public GermplasmChangeDetail(int index, String originalDesig, String originalGid, String newDesig, String newGid,
			String trialInstanceNumber, String entryNumber, String plotNumber) {
		super();
		this.index = index;
		this.originalDesig = originalDesig;
		this.originalGid = originalGid;
		this.newDesig = newDesig;
		this.newGid = newGid;
		this.trialInstanceNumber = trialInstanceNumber;
		this.entryNumber = entryNumber;
		this.plotNumber = plotNumber;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return this.index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @return the originalDesig
	 */
	public String getOriginalDesig() {
		return this.originalDesig;
	}

	/**
	 * @param originalDesig the originalDesig to set
	 */
	public void setOriginalDesig(String originalDesig) {
		this.originalDesig = originalDesig;
	}

	/**
	 * @return the originalGid
	 */
	public String getOriginalGid() {
		return this.originalGid;
	}

	/**
	 * @param originalGid the originalGid to set
	 */
	public void setOriginalGid(String originalGid) {
		this.originalGid = originalGid;
	}

	/**
	 * @return the newDesig
	 */
	public String getNewDesig() {
		return this.newDesig;
	}

	/**
	 * @param newDesig the newDesig to set
	 */
	public void setNewDesig(String newDesig) {
		this.newDesig = newDesig;
	}

	/**
	 * @return the newGid
	 */
	public String getNewGid() {
		return this.newGid;
	}

	/**
	 * @param newGid the newGid to set
	 */
	public void setNewGid(String newGid) {
		this.newGid = newGid;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return this.status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return the matchingGids
	 */
	public List<Integer> getMatchingGids() {
		return this.matchingGids;
	}

	/**
	 * @param matchingGids the matchingGids to set
	 */
	public void setMatchingGids(List<Integer> matchingGids) {
		this.matchingGids = matchingGids;
	}

	/**
	 * @return the selectedGid
	 */
	public Integer getSelectedGid() {
		return this.selectedGid;
	}

	/**
	 * @param selectedGid the selectedGid to set
	 */
	public void setSelectedGid(Integer selectedGid) {
		this.selectedGid = selectedGid;
	}

	/**
	 * @return the trialInstanceNumber
	 */
	public String getTrialInstanceNumber() {
		return this.trialInstanceNumber;
	}

	/**
	 * @param trialInstanceNumber the trialInstanceNumber to set
	 */
	public void setTrialInstanceNumber(String trialInstanceNumber) {
		this.trialInstanceNumber = trialInstanceNumber;
	}

	/**
	 * @return the entryNumber
	 */
	public String getEntryNumber() {
		return this.entryNumber;
	}

	/**
	 * @param entryNumber the entryNumber to set
	 */
	public void setEntryNumber(String entryNumber) {
		this.entryNumber = entryNumber;
	}

	/**
	 * @return the plotNumber
	 */
	public String getPlotNumber() {
		return this.plotNumber;
	}

	/**
	 * @param plotNumber the plotNumber to set
	 */
	public void setPlotNumber(String plotNumber) {
		this.plotNumber = plotNumber;
	}

	public int getNameType() {
		return this.nameType;
	}

	public void setNameType(int nameType) {
		this.nameType = nameType;
	}

	public int getImportLocationId() {
		return this.importLocationId;
	}

	public void setImportLocationId(int importLocationId) {
		this.importLocationId = importLocationId;
	}

	public int getImportMethodId() {
		return this.importMethodId;
	}

	public void setImportMethodId(int importMethodId) {
		this.importMethodId = importMethodId;
	}

	public String getImportDate() {
		return this.importDate;
	}

	public void setImportDate(String importDate) {
		this.importDate = importDate;
	}

}
