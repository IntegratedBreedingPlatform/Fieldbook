/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.web.inventory.form;

import java.util.List;

import org.generationcp.middleware.domain.inventory.InventoryDetails;


// TODO: Auto-generated Javadoc
/**
 * The Class SeedStoreForm.
 */
public class SeedStoreForm {
	 
    /** The inventory list. */
    private List<InventoryDetails> inventoryList;
	 
    //for pagination
    /** The paginated inventory list. */
    private List<InventoryDetails> paginatedInventoryList;     
    
    /** The current page. */
    private int currentPage;
    
    /** The total pages. */
    private int totalPages;
    
    /** The result per page. */
    private int resultPerPage = 100;    
    
    /** The location id. */
    private int inventoryLocationId;
    
    /** The scale id. */
    private int inventoryScaleId;
    
    /** The comments. */
    private String inventoryComments;
    
    /** The gid list. */
    private String gidList;
    
    /** The entry Id list */
    private String entryIdList;
    
    /** The list id. */
    private Integer listId;
    
    /** The total number of germplasms. */
    private int totalNumberOfGermplasms;
    
    /** The amount. */
    private Double amount;
    
     
    
    /**
     * Gets the list id.
     *
     * @return the list id
     */
    public Integer getListId() {
		return listId;
	}

	/**
	 * Sets the list id.
	 *
	 * @param listId the new list id
	 */
	public void setListId(Integer listId) {
		this.listId = listId;
	}

	/**
	 * Gets the total number of germplasms.
	 *
	 * @return the total number of germplasms
	 */
	public int getTotalNumberOfGermplasms() {
    	if(inventoryList != null) {
    		return inventoryList.size();
    	}
    	return 0;
	}

	/**
	 * Sets the total number of germplasms.
	 *
	 * @param totalNumberOfGermplasms the new total number of germplasms
	 */
	public void setTotalNumberOfGermplasms(int totalNumberOfGermplasms) {
		this.totalNumberOfGermplasms = totalNumberOfGermplasms;
	}

	/**
     * Gets the result per page.
     *
     * @return the result per page
     */
    public int getResultPerPage() {
        return resultPerPage;
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
    public int getTotalPages(){
        if(inventoryList != null && !inventoryList.isEmpty()){           
            totalPages = (int) Math.ceil((inventoryList.size() * 1f) / getResultPerPage()); 
        }else{
            totalPages = 0;
        }
        return totalPages;
    }
        
    /**
     * Gets the current page.
     *
     * @return the current page
     */
    public int getCurrentPage() {
        return currentPage;
    }
    
    /**
     * Sets the current page.
     *
     * @param currentPage the new current page
     */
    public void setCurrentPage(int currentPage) {
        
	        //assumption is there are nursery list already
	        if(inventoryList != null && !inventoryList.isEmpty()){
	            int totalItemsPerPage = getResultPerPage();
	            int start = (currentPage - 1) * totalItemsPerPage;
	            int end = start + totalItemsPerPage;
	            if(inventoryList.size() < end){
	                end = inventoryList.size();
	            }
	            this.paginatedInventoryList = inventoryList.subList(start, end);
	            this.currentPage = currentPage;
	        }else{
	            this.currentPage = 0;
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
	 * Gets the inventory list.
	 *
	 * @return the inventory list
	 */
	public List<InventoryDetails> getInventoryList() {
		return inventoryList;
	}

	/**
	 * Sets the inventory list.
	 *
	 * @param inventoryList the new inventory list
	 */
	public void setInventoryList(List<InventoryDetails> inventoryList) {
		this.inventoryList = inventoryList;
	}

	/**
	 * Gets the paginated inventory list.
	 *
	 * @return the paginated inventory list
	 */
	public List<InventoryDetails> getPaginatedInventoryList() {
		return paginatedInventoryList;
	}

	/**
	 * Sets the paginated inventory list.
	 *
	 * @param paginatedInventoryList the new paginated inventory list
	 */
	public void setPaginatedInventoryList(
			List<InventoryDetails> paginatedInventoryList) {
		this.paginatedInventoryList = paginatedInventoryList;
	}

    

    /**
     * Gets the inventory location id.
     *
     * @return the inventory location id
     */
    public int getInventoryLocationId() {
		return inventoryLocationId;
	}

	/**
	 * Sets the inventory location id.
	 *
	 * @param inventoryLocationId the new inventory location id
	 */
	public void setInventoryLocationId(int inventoryLocationId) {
		this.inventoryLocationId = inventoryLocationId;
	}

	/**
	 * Gets the inventory scale id.
	 *
	 * @return the inventory scale id
	 */
	public int getInventoryScaleId() {
		return inventoryScaleId;
	}

	/**
	 * Sets the inventory scale id.
	 *
	 * @param inventoryScaleId the new inventory scale id
	 */
	public void setInventoryScaleId(int inventoryScaleId) {
		this.inventoryScaleId = inventoryScaleId;
	}

	/**
	 * Gets the inventory comments.
	 *
	 * @return the inventory comments
	 */
	public String getInventoryComments() {
		return inventoryComments;
	}

	/**
	 * Sets the inventory comments.
	 *
	 * @param inventoryComments the new inventory comments
	 */
	public void setInventoryComments(String inventoryComments) {
		this.inventoryComments = inventoryComments;
	}

	/**
     * Gets the gid list.
     *
     * @return the gidList
     */
    public String getGidList() {
        return gidList;
    }

    /**
     * Sets the gid list.
     *
     * @param gidList the gidList to set
     */
    public void setGidList(String gidList) {
        this.gidList = gidList;
    }

	public String getEntryIdList() {
		return entryIdList;
	}

	public void setEntryIdList(String entryIdList) {
		this.entryIdList = entryIdList;
	}

	/**
	 * Gets the amount.
	 *
	 * @return the amount
	 */
	public Double getAmount() {
		return amount;
	}

	/**
	 * Sets the amount.
	 *
	 * @param amount the new amount
	 */
	public void setAmount(Double amount) {
		this.amount = amount;
	}

}
