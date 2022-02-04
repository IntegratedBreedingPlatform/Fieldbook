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

package com.efficio.fieldbook.web.common.form;

// TODO: Auto-generated Javadoc


/**
 * The Class SaveListForm.
 */
public class SaveListForm {

	/**
	 * The list name.
	 */
	private String listName;

	/**
	 * The list description.
	 */
	private String listDescription;

	/**
	 * The list type.
	 */
	private String listType;

	/**
	 * The list date.
	 */
	private String listDate;

	/**
	 * The list notes.
	 */
	private String listNotes;

	// to be use for dynamic of advance list
	private String listIdentifier;

	private String parentId;

	private String germplasmListType;

	// to be used for saving of stock lists
	private int sourceListId;

	private String listOwner;

	private boolean omitAlertedCrosses;

	public String getParentId() {
		return this.parentId;
	}

	public void setParentId(final String parentId) {
		this.parentId = parentId;
	}

	public String getListIdentifier() {
		return this.listIdentifier;
	}

	public void setListIdentifier(final String listIdentifier) {
		this.listIdentifier = listIdentifier;
	}

	/**
	 * Gets the list name.
	 *
	 * @return the list name
	 */
	public String getListName() {
		return this.listName;
	}

	/**
	 * Sets the list name.
	 *
	 * @param listName the new list name
	 */
	public void setListName(final String listName) {
		this.listName = listName;
	}

	/**
	 * Gets the list description.
	 *
	 * @return the list description
	 */
	public String getListDescription() {
		return this.listDescription;
	}

	/**
	 * Sets the list description.
	 *
	 * @param listDescription the new list description
	 */
	public void setListDescription(final String listDescription) {
		this.listDescription = listDescription;
	}

	/**
	 * Gets the list type.
	 *
	 * @return the list type
	 */
	public String getListType() {
		return this.listType;
	}

	/**
	 * Sets the list type.
	 *
	 * @param listType the new list type
	 */
	public void setListType(final String listType) {
		this.listType = listType;
	}

	/**
	 * Gets the list date.
	 *
	 * @return the list date
	 */
	public String getListDate() {
		return this.listDate;
	}

	/**
	 * Sets the list date.
	 *
	 * @param listDate the new list date
	 */
	public void setListDate(final String listDate) {
		this.listDate = listDate;
	}

	/**
	 * Gets the list notes.
	 *
	 * @return the list notes
	 */
	public String getListNotes() {
		return this.listNotes;
	}

	/**
	 * Sets the list notes.
	 *
	 * @param listNotes the new list notes
	 */
	public void setListNotes(final String listNotes) {
		this.listNotes = listNotes;
	}

	public String getGermplasmListType() {
		return this.germplasmListType;
	}

	public void setGermplasmListType(final String germplasmListType) {
		this.germplasmListType = germplasmListType;
	}

	public int getSourceListId() {
		return this.sourceListId;
	}

	public void setSourceListId(final int sourceListId) {
		this.sourceListId = sourceListId;
	}

	public String getListOwner() {
		return this.listOwner;
	}

	public void setListOwner(final String listOwner) {
		this.listOwner = listOwner;
	}

	public boolean getOmitAlertedCrosses() {
		return this.omitAlertedCrosses;
	}

	public void setOmitAlertedCrosses(final boolean omitAlertedCrosses) {
		this.omitAlertedCrosses = omitAlertedCrosses;
	}
}
