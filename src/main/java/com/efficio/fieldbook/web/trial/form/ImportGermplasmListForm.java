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

import org.generationcp.middleware.ruleengine.pojo.ImportedGermplasm;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.web.common.bean.SettingDetail;

/**
 * The Class ImportGermplasmListForm.
 */
public class ImportGermplasmListForm {

	/** The file. */
	private MultipartFile file;

	/** The has error. */
	private String hasError;

	/** The imported germplasm. */
	private List<ImportedGermplasm> importedGermplasm;

	/** The imported check germplasm. */
	private List<ImportedGermplasm> importedCheckGermplasm;
	// for pagination
	/** The paginated imported germplasm. */
	private List<ImportedGermplasm> paginatedImportedGermplasm;

	/** The paginated imported check germplasm. */
	private List<ImportedGermplasm> paginatedImportedCheckGermplasm;

	private List<SettingDetail> checkVariables;

	/** The current page. */
	private int currentPage;

	/** The current check page. */
	private int currentCheckPage;

	/** The total pages. */
	private int totalPages;

	/** The total check pages. */
	private int totalCheckPages;

	/** The result per page. */
	private int resultPerPage = 100;

	/** The method ids. */
	private Integer[] check;

	/** The choose specify check. */
	private String chooseSpecifyCheck;

	/** The check value. */
	private String checkValue;

	/** The check id. */
	private Integer checkId;

	/** The manage check code. */
	private String manageCheckCode;

	/** The manage check value. */
	private String manageCheckValue;

	/** The total germplasms. */
	private int totalGermplasms;

	/** The last dragged checks list. */
	private String lastDraggedChecksList = "0";

	/** The last dragged primary list. */
	private String lastDraggedPrimaryList;

	/** The selected check. */
	private String[] selectedCheck;

	/** The key for overwrite. */
	private Integer keyForOverwrite;

	/** The last check source primary. */
	private int lastCheckSourcePrimary;

	private String columnOrders;

	private String startingPlotNo;

	private Integer germplasmListId;
	private Integer studyId;

	/**
	 * Gets the selected check.
	 *
	 * @return the selected check
	 */
	public String[] getSelectedCheck() {
		return this.selectedCheck;
	}

	/**
	 * Sets the selected check.
	 *
	 * @param selectedCheck the new selected check
	 */
	public void setSelectedCheck(String[] selectedCheck) {
		this.selectedCheck = selectedCheck;
	}

	/**
	 * Gets the last dragged checks list.
	 *
	 * @return the last dragged checks list
	 */
	public String getLastDraggedChecksList() {
		return this.lastDraggedChecksList;
	}

	/**
	 * Sets the last dragged checks list.
	 *
	 * @param lastDraggedChecksList the new last dragged checks list
	 */
	public void setLastDraggedChecksList(String lastDraggedChecksList) {
		this.lastDraggedChecksList = lastDraggedChecksList;
	}

	/**
	 * Gets the total germplasms.
	 *
	 * @return the total germplasms
	 */
	public int getTotalGermplasms() {
		if (this.importedGermplasm != null) {
			return this.importedGermplasm.size();
		}
		return 0;
	}

	/**
	 * Sets the total germplasms.
	 *
	 * @param totalGermplasms the new total germplasms
	 */
	public void setTotalGermplasms(int totalGermplasms) {
		this.totalGermplasms = totalGermplasms;
	}

	/**
	 * Gets the check value.
	 *
	 * @return the check value
	 */
	public String getCheckValue() {
		return this.checkValue;
	}

	/**
	 * Sets the check value.
	 *
	 * @param checkValue the new check value
	 */
	public void setCheckValue(String checkValue) {
		this.checkValue = checkValue;
	}

	/**
	 * Gets the check id.
	 *
	 * @return the check id
	 */
	public Integer getCheckId() {
		return this.checkId;
	}

	/**
	 * Sets the check id.
	 *
	 * @param checkId the new check id
	 */
	public void setCheckId(Integer checkId) {
		this.checkId = checkId;
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
	 * Gets the total pages.
	 *
	 * @return the total pages
	 */
	public int getTotalPages() {
		if (this.importedGermplasm != null && !this.importedGermplasm.isEmpty()) {
			this.totalPages = (int) Math.ceil(this.importedGermplasm.size() * 1f / this.getResultPerPage());
		} else {
			this.totalPages = 0;
		}
		return this.totalPages;
	}

	/**
	 * Gets the current check page.
	 *
	 * @return the current check page
	 */
	public int getCurrentCheckPage() {
		return this.currentCheckPage;
	}

	/**
	 * Sets the current check page.
	 *
	 * @param currentCheckPage the new current check page
	 */
	public void setCurrentCheckPage(int currentCheckPage) {
		this.currentCheckPage = currentCheckPage;
	}

	/**
	 * Gets the total check pages.
	 *
	 * @return the total check pages
	 */
	public int getTotalCheckPages() {
		if (this.importedCheckGermplasm != null && !this.importedCheckGermplasm.isEmpty()) {
			this.totalCheckPages = (int) Math.ceil(this.importedCheckGermplasm.size() * 1f / this.getResultPerPage());
		} else {
			this.totalCheckPages = 0;
		}
		return this.totalCheckPages;
	}

	/**
	 * Sets the total check pages.
	 *
	 * @param totalCheckPages the new total check pages
	 */
	public void setTotalCheckPages(int totalCheckPages) {
		this.totalCheckPages = totalCheckPages;
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
	 * Change page.
	 *
	 * @param currentPage the current page
	 */
	public void changePage(int currentPage) {
		// assumption is there is an imported germplasm already
		if (this.importedGermplasm != null && !this.importedGermplasm.isEmpty()) {
			int totalItemsPerPage = this.getResultPerPage();
			int start = (currentPage - 1) * totalItemsPerPage;
			int end = start + totalItemsPerPage;
			if (this.importedGermplasm.size() < end) {
				end = this.importedGermplasm.size();
			}
			this.paginatedImportedGermplasm = this.importedGermplasm.subList(start, end);
			this.currentPage = currentPage;
		} else {
			this.currentPage = 0;
		}
	}

	/**
	 * Change check page.
	 *
	 * @param currentCheckPage the current check page
	 */
	public void changeCheckPage(int currentCheckPage) {
		// assumption is there is an imported germplasm already
		if (this.importedCheckGermplasm != null && !this.importedCheckGermplasm.isEmpty()) {
			int totalItemsPerPage = this.getResultPerPage();
			int start = (currentCheckPage - 1) * totalItemsPerPage;
			int end = start + totalItemsPerPage;
			if (this.importedCheckGermplasm.size() < end) {
				end = this.importedCheckGermplasm.size();
			}
			this.paginatedImportedCheckGermplasm = this.importedCheckGermplasm.subList(start, end);
			this.currentCheckPage = currentCheckPage;
		} else {
			this.currentCheckPage = 0;
		}
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
	 * Gets the paginated imported germplasm.
	 *
	 * @return the paginated imported germplasm
	 */
	public List<ImportedGermplasm> getPaginatedImportedGermplasm() {
		return this.paginatedImportedGermplasm;
	}

	/**
	 * Sets the paginated imported germplasm.
	 *
	 * @param paginatedImportedGermplasm the new paginated imported germplasm
	 */
	public void setPaginatedImportedGermplasm(List<ImportedGermplasm> paginatedImportedGermplasm) {
		this.paginatedImportedGermplasm = paginatedImportedGermplasm;
	}

	// end of pagination code

	/**
	 * Gets the imported germplasm.
	 *
	 * @return the imported germplasm
	 */
	public List<ImportedGermplasm> getImportedGermplasm() {
		return this.importedGermplasm;
	}

	/**
	 * Sets the imported germplasm.
	 *
	 * @param importedGermplasm the new imported germplasm
	 */
	public void setImportedGermplasm(List<ImportedGermplasm> importedGermplasm) {
		this.importedGermplasm = importedGermplasm;
	}

	/**
	 * Instantiates a new import germplasm list form.
	 */
	public ImportGermplasmListForm() {
		this.setHasError("0");
	}

	/**
	 * Gets the checks for error.
	 *
	 * @return the checks for error
	 */
	public String getHasError() {
		return this.hasError;
	}

	/**
	 * Sets the checks for error.
	 *
	 * @param hasError the new checks for error
	 */
	public void setHasError(String hasError) {
		this.hasError = hasError;
	}

	/**
	 * Gets the file.
	 *
	 * @return the file
	 */
	public MultipartFile getFile() {
		return this.file;
	}

	/**
	 * Sets the file.
	 *
	 * @param file the new file
	 */
	public void setFile(MultipartFile file) {
		this.file = file;
	}

	/**
	 * Gets the check.
	 *
	 * @return the check
	 */
	public Integer[] getCheck() {
		return this.check;
	}

	/**
	 * Sets the check.
	 *
	 * @param check the new check
	 */
	public void setCheck(Integer[] check) {
		this.check = check;
	}

	/**
	 * Gets the choose specify check.
	 *
	 * @return the choose specify check
	 */
	public String getChooseSpecifyCheck() {
		return this.chooseSpecifyCheck;
	}

	/**
	 * Sets the choose specify check.
	 *
	 * @param chooseSpecifyCheck the new choose specify check
	 */
	public void setChooseSpecifyCheck(String chooseSpecifyCheck) {
		this.chooseSpecifyCheck = chooseSpecifyCheck;
	}

	/**
	 * Gets the manage check code.
	 *
	 * @return the manage check code
	 */
	public String getManageCheckCode() {
		return this.manageCheckCode;
	}

	/**
	 * Sets the manage check code.
	 *
	 * @param manageCheckCode the new manage check code
	 */
	public void setManageCheckCode(String manageCheckCode) {
		this.manageCheckCode = manageCheckCode;
	}

	/**
	 * Gets the manage check value.
	 *
	 * @return the manage check value
	 */
	public String getManageCheckValue() {
		return this.manageCheckValue;
	}

	/**
	 * Sets the manage check value.
	 *
	 * @param manageCheckValue the new manage check value
	 */
	public void setManageCheckValue(String manageCheckValue) {
		this.manageCheckValue = manageCheckValue;
	}

	/**
	 * Gets the imported check germplasm.
	 *
	 * @return the imported check germplasm
	 */
	public List<ImportedGermplasm> getImportedCheckGermplasm() {
		return this.importedCheckGermplasm;
	}

	/**
	 * Sets the imported check germplasm.
	 *
	 * @param importedCheckGermplasm the new imported check germplasm
	 */
	public void setImportedCheckGermplasm(List<ImportedGermplasm> importedCheckGermplasm) {
		this.importedCheckGermplasm = importedCheckGermplasm;
	}

	/**
	 * Gets the paginated imported check germplasm.
	 *
	 * @return the paginated imported check germplasm
	 */
	public List<ImportedGermplasm> getPaginatedImportedCheckGermplasm() {
		return this.paginatedImportedCheckGermplasm;
	}

	/**
	 * Sets the paginated imported check germplasm.
	 *
	 * @param paginatedImportedCheckGermplasm the new paginated imported check germplasm
	 */
	public void setPaginatedImportedCheckGermplasm(List<ImportedGermplasm> paginatedImportedCheckGermplasm) {
		this.paginatedImportedCheckGermplasm = paginatedImportedCheckGermplasm;
	}

	/**
	 * Gets the key for overwrite.
	 *
	 * @return the key for overwrite
	 */
	public Integer getKeyForOverwrite() {
		return this.keyForOverwrite;
	}

	/**
	 * Sets the key for overwrite.
	 *
	 * @param keyForOverwrite the new key for overwrite
	 */
	public void setKeyForOverwrite(Integer keyForOverwrite) {
		this.keyForOverwrite = keyForOverwrite;
	}

	/**
	 * Gets the last check source primary.
	 *
	 * @return the last check source primary
	 */
	public int getLastCheckSourcePrimary() {
		return this.lastCheckSourcePrimary;
	}

	/**
	 * Sets the last check source primary.
	 *
	 * @param lastCheckSourcePrimary the new last check source primary
	 */
	public void setLastCheckSourcePrimary(int lastCheckSourcePrimary) {
		this.lastCheckSourcePrimary = lastCheckSourcePrimary;
	}

	/**
	 * Gets the last dragged primary list.
	 *
	 * @return the last dragged primary list
	 */
	public String getLastDraggedPrimaryList() {
		return this.lastDraggedPrimaryList;
	}

	/**
	 * Sets the last dragged primary list.
	 *
	 * @param lastDraggedPrimaryList the new last dragged primary list
	 */
	public void setLastDraggedPrimaryList(String lastDraggedPrimaryList) {
		this.lastDraggedPrimaryList = lastDraggedPrimaryList;
	}

	public List<SettingDetail> getCheckVariables() {
		return this.checkVariables;
	}

	public void setCheckVariables(List<SettingDetail> checkVariables) {
		this.checkVariables = checkVariables;
	}

	public String getColumnOrders() {
		return this.columnOrders;
	}

	public void setColumnOrders(String columnOrders) {
		this.columnOrders = columnOrders;
	}

	public String getStartingPlotNo() {
		return startingPlotNo;
	}

	public void setStartingPlotNo(String startingPlotNo) {
		this.startingPlotNo = startingPlotNo;
	}

	public Integer getGermplasmListId() {
		return this.germplasmListId;
	}

	public void setGermplasmListId(final Integer germplasmListId) {
		this.germplasmListId = germplasmListId;
	}

	public Integer getStudyId() {
		return this.studyId;
	}

	public void setStudyId(Integer studyId) {
		this.studyId = studyId;
	}
}
