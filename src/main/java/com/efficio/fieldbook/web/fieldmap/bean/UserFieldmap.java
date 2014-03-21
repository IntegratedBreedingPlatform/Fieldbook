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
package com.efficio.fieldbook.web.fieldmap.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.util.AppConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class UserFieldmap.
 *
 * Session variable being use to hold information for the creating of the fieldmaps.
 */
public class UserFieldmap  implements Serializable {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The trial id or nursery id. */
    private Integer studyId;
    
    /** The selected dataset id. */
    private Integer selectedDatasetId;
    
    /** The selected geolocation id. */
    private Integer selectedGeolocationId;
        
    /** The number of entries. */
    private Long numberOfEntries;
    
    /** The number of reps. */
    private Long numberOfReps;
    
    /** The total number of plots. */
    private Long totalNumberOfPlots;
    
    /** The field location id. */
    private int fieldLocationId;
    
    /** The field name. */
    private String fieldName;
    
    /** The block name. */
    private String blockName;

    /** The field id. */
    private Integer fieldId;
    
    /** The block id. */
    private Integer blockId;
    
    /** The number of rows in block. */
    private int numberOfRowsInBlock;
    
    /** The number of ranges in block. */
    private int numberOfRangesInBlock;
    
    /** The number of rows per plot. */
    private int numberOfRowsPerPlot;
    
    /** The planting order. */
    private int plantingOrder;
    
    /** The is trial. */
    private boolean isTrial;
    
    /** The entry numbers. */
    private List<String> entryNumbers;
    
    /** The germplasm names. */
    private List<String> germplasmNames;
    
    /** The reps. */
    private List<Integer> reps;
    
    /** The starting column. */
    private int startingColumn = 1; // we default to 1
    
    /** The starting range. */
    private int startingRange = 1; //we default to 1
    
    /** The fieldmap. */
    private Plot[][] fieldmap;
    
    /** The field map labels. */
    private List<FieldMapLabel> fieldMapLabels;
    
    /** The location name. */
    private String locationName;
    
    /** The field map info. */
    private List<FieldMapInfo> fieldMapInfo;
    
    /** The selected field maps. */
    private List<FieldMapInfo> selectedFieldMaps;
    
    private List<FieldMapInfo> selectedFieldMapsToBeAdded;
    
    /** The order. */
    private String order;

    /** The machine row capacity. */
    private Integer machineRowCapacity;
    
    /** The selected fieldmap list. */
    private SelectedFieldmapList selectedFieldmapList;
    private SelectedFieldmapList selectedFieldmapListToBeAdded;
    
    /** The is generated. */
    private boolean isGenerated;
    /** The deleted plot coordinates in (row, range) format.  */
    private List<String> deletedPlots;

    /** The is new. */
    private boolean isNew;
    
    /**
     * Checks if is new.
     *
     * @return true, if is new
     */
    public boolean isNew() {
		return isNew;
	}

	/**
	 * Sets the new.
	 *
	 * @param isNew the new new
	 */
	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	/**
     * Instantiates a new user fieldmap.
     */
    public UserFieldmap(){
        
    }
    
    /**
     * Instantiates a new user fieldmap.
     *
     * @param fieldMapInfo the field map info
     * @param isTrial the is trial
     */
    public UserFieldmap(List<FieldMapInfo> fieldMapInfo, boolean isTrial){
        setUserFieldmapInfo(fieldMapInfo, isTrial);
    }
    
    /**
     * Checks if is generated.
     *
     * @return true, if is generated
     */
    public boolean isGenerated() {
        return isGenerated;
    }
    
    /**
     * Sets the generated.
     *
     * @param isGenerated the new generated
     */
    public void setGenerated(boolean isGenerated) {
        this.isGenerated = isGenerated;
    }

    /**
     * Sets the user fieldmap info.
     *
     * @param fieldMapInfoList the field map info list
     * @param isTrial the is trial
     */
    public void setUserFieldmapInfo(List<FieldMapInfo> fieldMapInfoList, boolean isTrial){
        
        if (getSelectedDatasetId() != null && getSelectedGeolocationId() != null) {
            
            int datasetId = getSelectedDatasetId();
            int trialInstanceId = getSelectedGeolocationId();
            
            for (FieldMapInfo fieldMapInfo : fieldMapInfoList) {
                if (fieldMapInfo.getDataSet(datasetId) != null) {
                    FieldMapTrialInstanceInfo info = 
                            fieldMapInfo.getDataSet(datasetId).getTrialInstance(trialInstanceId);
                    //setFieldMapLabels(info.getFieldMapLabels());
                    setFieldMapLabels(getAllSelectedFieldMapLabels(false));
                    setNumberOfEntries(info.getEntryCount());
                    setNumberOfReps(info.getRepCount());
                    setTotalNumberOfPlots(info.getPlotCount());
                    setStudyId(fieldMapInfo.getFieldbookId());
                }
            }            
        }
        
        setTrial(isTrial);
        if(isTrial){
            setNumberOfRowsPerPlot(2);
        }else{
            setNumberOfRowsPerPlot(1);
        }
        setFieldMapInfo(fieldMapInfoList);
        
    }
    
    /**
     * Gets the all selected field map labels.
     *
     * @param isSorted the is sorted
     * @return the all selected field map labels
     */
    public List<FieldMapLabel> getAllSelectedFieldMapLabels(boolean isSorted) {
        List<FieldMapLabel> allLabels = new ArrayList<FieldMapLabel>();
        
        if (isSorted) {
            if (getSelectedFieldmapList() != null && !getSelectedFieldmapList().isEmpty()) {
                for (SelectedFieldmapRow row : getSelectedFieldmapList().getRows()) {
                    FieldMapTrialInstanceInfo trial = 
                            getSelectedTrialInstanceByDatasetIdAndGeolocationId(row.getDatasetId(), 
                                    row.getGeolocationId());
                    allLabels.addAll(trial.getFieldMapLabels());
                }
            }
        }
        else {
            if (getSelectedFieldMaps() != null && !getSelectedFieldMaps().isEmpty()) {
                for (FieldMapInfo info : getSelectedFieldMaps()) {
                    if (info.getDatasets() != null && !info.getDatasets().isEmpty()) {
                        for (FieldMapDatasetInfo dataset : info.getDatasets()) {
                            if (dataset.getTrialInstances() != null) {
                                for (FieldMapTrialInstanceInfo trial : dataset.getTrialInstances()) {
                                    if (trial.getFieldMapLabels() != null 
                                            && !trial.getFieldMapLabels().isEmpty()) {
                                        allLabels.addAll(trial.getFieldMapLabels());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return allLabels;
    }
    
    public List<FieldMapLabel> getAllSelectedFieldMapLabelsToBeAdded(boolean isSorted) {
        List<FieldMapLabel> allLabels = new ArrayList<FieldMapLabel>();
        
        if (isSorted) {
            if (getSelectedFieldmapListToBeAdded() != null && !getSelectedFieldmapListToBeAdded().isEmpty()) {
                for (SelectedFieldmapRow row : getSelectedFieldmapListToBeAdded().getRows()) {
                    FieldMapTrialInstanceInfo trial = 
                            getSelectedTrialInstanceByDatasetIdAndGeolocationId(row.getDatasetId(), 
                                    row.getGeolocationId());
                    allLabels.addAll(trial.getFieldMapLabels());
                }
            }
        }
        else {
            if (getSelectedFieldmapListToBeAdded() != null && !getSelectedFieldmapListToBeAdded().isEmpty()) {
                for (FieldMapInfo info : getSelectedFieldMapsToBeAdded()) {
                    if (info.getDatasets() != null && !info.getDatasets().isEmpty()) {
                        for (FieldMapDatasetInfo dataset : info.getDatasets()) {
                            if (dataset.getTrialInstances() != null) {
                                for (FieldMapTrialInstanceInfo trial : dataset.getTrialInstances()) {
                                    if (trial.getFieldMapLabels() != null 
                                            && !trial.getFieldMapLabels().isEmpty()) {
                                        allLabels.addAll(trial.getFieldMapLabels());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        for(FieldMapLabel fieldMapLabel :allLabels){
        	fieldMapLabel.setColumn(null);
        	fieldMapLabel.setRange(null);
        }
        return allLabels;
    }

    /**
     * Gets the field map info.
     *
     * @return the field map info
     */
    public List<FieldMapInfo> getFieldMapInfo() {
        return fieldMapInfo;
    }
    
    /**
     * Sets the field map info.
     *
     * @param fieldMapInfo the new field map info
     */
    public void setFieldMapInfo(List<FieldMapInfo> fieldMapInfo) {
        this.fieldMapInfo = fieldMapInfo;
    }
    
    /**
     * Gets the entry numbers.
     *
     * @return the entry numbers
     */
    public List<String> getEntryNumbers() {
        return entryNumbers;
    }

    /**
     * Sets the entry numbers.
     *
     * @param entryNumbers the new entry numbers
     */
    public void setEntryNumbers(List<String> entryNumbers) {
        this.entryNumbers = entryNumbers;
    }

    /**
     * Gets the germplasm names.
     *
     * @return the germplasm names
     */
    public List<String> getGermplasmNames() {
        return germplasmNames;
    }

    /**
     * Sets the germplasm names.
     *
     * @param germplasmNames the new germplasm names
     */
    public void setGermplasmNames(List<String> germplasmNames) {
        this.germplasmNames = germplasmNames;
    }

    /**
     * Gets the reps.
     *
     * @return the reps
     */
    public List<Integer> getReps() {
        return reps;
    }

    /**
     * Sets the reps.
     *
     * @param reps the new reps
     */
    public void setReps(List<Integer> reps) {
        this.reps = reps;
    }

    /**
     * Checks if is trial.
     *
     * @return true, if is trial
     */
    public boolean isTrial() {
        return isTrial;
    }
    
    /**
     * Sets the trial.
     *
     * @param isTrial the new trial
     */
    public void setTrial(boolean isTrial) {
        this.isTrial = isTrial;
    }
    
    /**
     * Gets the number of entries.
     *
     * @return the number of entries
     */
    public Long getNumberOfEntries() {
        return numberOfEntries;
    }
    
    /**
     * Sets the number of entries.
     *
     * @param numberOfEntries the new number of entries
     */
    public void setNumberOfEntries(Long numberOfEntries) {
        this.numberOfEntries = numberOfEntries;
    }
    
    /**
     * Gets the number of reps.
     *
     * @return the number of reps
     */
    public Long getNumberOfReps() {
        return numberOfReps;
    }
    
    /**
     * Sets the number of reps.
     *
     * @param numberOfReps the new number of reps
     */
    public void setNumberOfReps(Long numberOfReps) {
        this.numberOfReps = numberOfReps;
    }
    
    /**
     * Gets the total number of plots.
     *
     * @return the total number of plots
     */
    public Long getTotalNumberOfPlots() {
        return totalNumberOfPlots;
    }
    
    /**
     * Sets the total number of plots.
     *
     * @param totalNumberOfPlots the new total number of plots
     */
    public void setTotalNumberOfPlots(Long totalNumberOfPlots) {
        this.totalNumberOfPlots = totalNumberOfPlots;
    }
    
    /**
     * Gets the field location id.
     *
     * @return the field location id
     */
    public int getFieldLocationId() {
        return fieldLocationId;
    }
    
    /**
     * Sets the field location id.
     *
     * @param fieldLocationId the new field location id
     */
    public void setFieldLocationId(int fieldLocationId) {
        this.fieldLocationId = fieldLocationId;
    }
    
    /**
     * Gets the field name.
     *
     * @return the field name
     */
    public String getFieldName() {
        return fieldName;
    }
    
    /**
     * Sets the field name.
     *
     * @param fieldName the new field name
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    /**
     * Gets the block name.
     *
     * @return the block name
     */
    public String getBlockName() {
        return blockName;
    }
    
    /**
     * Sets the block name.
     *
     * @param blockName the new block name
     */
    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }
    
    /**
     * Gets the number of rows in block.
     *
     * @return the number of rows in block
     */
    public int getNumberOfRowsInBlock() {
        return numberOfRowsInBlock;
    }
    
    /**
     * Gets the number of columns in block.
     *
     * @return the number of columns in block
     */
    public int getNumberOfColumnsInBlock() {
    	return  getNumberOfRowsInBlock() / getNumberOfRowsPerPlot();
    }
    
    /**
     * Gets the block capacity string.
     *
     * @param messageSource the message source
     * @return the block capacity string
     */
    public String getBlockCapacityString(ResourceBundleMessageSource messageSource){
    	// 10 Columns, 10 Ranges
    	Locale locale = LocaleContextHolder.getLocale();
    	String columns = messageSource.getMessage("fieldmap.label.rows", null, locale);
        String ranges = messageSource.getMessage("fieldmap.label.ranges", null, locale);
    	return this.getNumberOfRowsInBlock() + " " + columns + ", " 
                + getNumberOfRangesInBlock()+ " " + ranges;
    }
    
    /**
     * Gets the starting coordinate string.
     *
     * @param messageSource the message source
     * @return the starting coordinate string
     */
    public String getStartingCoordinateString(ResourceBundleMessageSource messageSource) {
    	// Column 1, Range 1
    	Locale locale = LocaleContextHolder.getLocale();
    	String column = messageSource.getMessage("fieldmap.label.capitalized.column", null, locale);
    	String range = messageSource.getMessage("fieldmap.label.capitalized.range", null, locale);
    	return column + " " + getStartingColumn() + ", " + range + " " + getStartingRange();
    }
    
    /**
     * Gets the planting order string.
     *
     * @param messageSource the message source
     * @return the planting order string
     * @throws FieldbookException the fieldbook exception
     */
    public String getPlantingOrderString(ResourceBundleMessageSource messageSource) 
            throws FieldbookException{
    	Locale locale = LocaleContextHolder.getLocale();
    	if (plantingOrder == AppConstants.ROW_COLUMN.getInt()){
    		return messageSource.getMessage("fieldmap.planting.order.row.column", null, locale);
    	} else if (plantingOrder == AppConstants.SERPENTINE.getInt()){
    		return messageSource.getMessage("fieldmap.planting.order.serpentine", null, locale);
    	}
    	throw new FieldbookException("Invalid planting order.");
	}    
    
    /**
     * Checks if is serpentine.
     *
     * @return true, if is serpentine
     */
    public boolean isSerpentine(){
    	if (plantingOrder == AppConstants.SERPENTINE.getInt()){
    		return true;
    	}
    	return false;
    }
    
    /**
     * Gets the selected trial instance by dataset id and geolocation id.
     *
     * @param datasetId the dataset id
     * @param geolocationId the geolocation id
     * @return the selected trial instance by dataset id and geolocation id
     */
    public FieldMapTrialInstanceInfo getSelectedTrialInstanceByDatasetIdAndGeolocationId(
            int datasetId, int geolocationId) {
        if (getSelectedFieldMaps() != null) {
            for (FieldMapInfo info : getSelectedFieldMaps()) {
                if (info.getDatasets() != null) {
                    for (FieldMapDatasetInfo dataset : info.getDatasets()) {
                        if (dataset.getDatasetId().equals(datasetId)) {
                            if (dataset.getTrialInstances() != null) {
                                for (FieldMapTrialInstanceInfo trial : dataset.getTrialInstances()) {
                                    if (trial.getGeolocationId().equals(geolocationId)) {
                                        return trial;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    public FieldMapTrialInstanceInfo getAnySelectedTrialInstance() {
        if (getSelectedFieldMaps() != null) {
        	FieldMapInfo info = getSelectedFieldMaps().get(getSelectedFieldMaps().size()-1);
            //for (FieldMapInfo info : getSelectedFieldMaps()) {
                if (info.getDatasets() != null) {
                	FieldMapDatasetInfo dataset = info.getDatasets().get(info.getDatasets().size()-1);
                    //for (FieldMapDatasetInfo dataset : info.getDatasets()) {
                        if (dataset.getTrialInstances() != null) {
                        	
                            for (FieldMapTrialInstanceInfo trial : dataset.getTrialInstances()) {
                            	if (trial.getBlockId() != null) {
                            		return trial;
                            	}
                            }
                            
                        	//return dataset.getTrialInstances().get(dataset.getTrialInstances().size()-1);
                        }
                    //}
                }
            //}
        }
        return null;
    }

    /**
     * Sets the number of rows in block.
     *
     * @param numberOfRowsInBlock the new number of rows in block
     */
    public void setNumberOfRowsInBlock(int numberOfRowsInBlock) {
        this.numberOfRowsInBlock = numberOfRowsInBlock;
    }
    
    /**
     * Sets the number of rows in block.
     *
     * @param numberOfColumnsInBlock the number of columns in block
     * @param rowsPerPlot the rows per plot
     */
    public void setNumberOfRowsInBlock(int numberOfColumnsInBlock, int rowsPerPlot) {
        this.numberOfRowsInBlock = numberOfColumnsInBlock * rowsPerPlot;
    }

    /**
     * Gets the number of ranges in block.
     *
     * @return the number of ranges in block
     */
    public int getNumberOfRangesInBlock() {
        return numberOfRangesInBlock;
    }
    
    /**
     * Sets the number of ranges in block.
     *
     * @param numberOfRangesInBlock the new number of ranges in block
     */
    public void setNumberOfRangesInBlock(int numberOfRangesInBlock) {
        this.numberOfRangesInBlock = numberOfRangesInBlock;
    }
    
    /**
     * Gets the number of rows per plot.
     *
     * @return the number of rows per plot
     */
    public int getNumberOfRowsPerPlot() {
        return numberOfRowsPerPlot;
    }
    
    /**
     * Sets the number of rows per plot.
     *
     * @param numberOfRowsPerPlot the new number of rows per plot
     */
    public void setNumberOfRowsPerPlot(int numberOfRowsPerPlot) {
        this.numberOfRowsPerPlot = numberOfRowsPerPlot;
    }
    
    /**
     * Gets the planting order.
     *
     * @return the planting order
     */
    public int getPlantingOrder() {
        return plantingOrder;
    }
    
    /**
     * Sets the planting order.
     *
     * @param plantingOrder the new planting order
     */
    public void setPlantingOrder(int plantingOrder) {
        this.plantingOrder = plantingOrder;
    }
    
    /**
     * Gets the starting column.
     *
     * @return the starting column
     */
    public int getStartingColumn() {
        return startingColumn;
    }
    
    /**
     * Sets the starting column.
     *
     * @param startingColumn the new starting column
     */
    public void setStartingColumn(int startingColumn) {
        this.startingColumn= startingColumn;
    }
    
    /**
     * Gets the starting range.
     *
     * @return the starting range
     */
    public int getStartingRange() {
        return startingRange;
    }
    
    /**
     * Sets the starting range.
     *
     * @param startingRange the new starting range
     */
    public void setStartingRange(int startingRange) {
        this.startingRange = startingRange;
    }
    
    /**
     * Gets the fieldmap.
     *
     * @return the fieldmap
     */
    public Plot[][] getFieldmap() {
        return fieldmap;
    }
    
    /**
     * Sets the fieldmap.
     *
     * @param fieldmap the new fieldmap
     */
    public void setFieldmap(Plot[][] fieldmap) {
        this.fieldmap = fieldmap;
    }
    
    /**
     * Gets the field map labels.
     *
     * @return the field map labels
     */
    public List<FieldMapLabel> getFieldMapLabels() {
        return fieldMapLabels;
    }
    
    /**
     * Sets the field map labels.
     *
     * @param fieldMapLabels the new field map labels
     */
    public void setFieldMapLabels(List<FieldMapLabel> fieldMapLabels) {
        this.fieldMapLabels = fieldMapLabels;
    }

    /**
     * Gets the location name.
     *
     * @return the location name
     */
    public String getLocationName() {
        return locationName;
    }
    
    /**
     * Sets the location name.
     *
     * @param locationName the new location name
     */
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
    
    /**
     * Gets the study id.
     *
     * @return the studyId
     */
    public Integer getStudyId() {
        return studyId;
    }
    
    /**
     * Sets the study id.
     *
     * @param studyId the studyId to set
     */
    public void setStudyId(Integer studyId) {
        this.studyId = studyId;
    }
    
    /**
     * Gets the selected dataset id.
     *
     * @return the selectedDatasetId
     */
    public Integer getSelectedDatasetId() {
        return selectedDatasetId;
    }
    
    /**
     * Sets the selected dataset id.
     *
     * @param selectedDatasetId the selectedDatasetId to set
     */
    public void setSelectedDatasetId(Integer selectedDatasetId) {
        this.selectedDatasetId = selectedDatasetId;
    }
    
    /**
     * Gets the selected geolocation id.
     *
     * @return the selectedGeolocationId
     */
    public Integer getSelectedGeolocationId() {
        return selectedGeolocationId;
    }
    
    /**
     * Sets the selected geolocation id.
     *
     * @param selectedGeolocationId the selectedGeolocationId to set
     */
    public void setSelectedGeolocationId(Integer selectedGeolocationId) {
        this.selectedGeolocationId = selectedGeolocationId;
    }
    
    /**
     * Gets the selected field maps.
     *
     * @return the selectedFieldMaps
     */
    public List<FieldMapInfo> getSelectedFieldMaps() {
        return selectedFieldMaps;
    }

    /**
     * Gets the order.
     *
     * @return the order
     */
    public String getOrder() {
        return order;
    }
    
    /**
     * Sets the order.
     *
     * @param order the new order
     */
    public void setOrder(String order) {
        this.order = order;
    }

    /**
     * Sets the selected field maps.
     *
     * @param selectedFieldMaps the selectedFieldMaps to set
     */
    public void setSelectedFieldMaps(List<FieldMapInfo> selectedFieldMaps) {
        this.selectedFieldMaps = selectedFieldMaps;
    }

    /**
     * Gets the machine row capacity.
     *
     * @return the machineRowCapacity
     */
    public Integer getMachineRowCapacity() {
        return machineRowCapacity;
    }
    
    /**
     * Sets the machine row capacity.
     *
     * @param machineRowCapacity the machineRowCapacity to set
     */
    public void setMachineRowCapacity(Integer machineRowCapacity) {
        this.machineRowCapacity = machineRowCapacity;
    }

    /**
     * Gets the total number of selected plots.
     *
     * @return the total number of selected plots
     */
    public long getTotalNumberOfSelectedPlots() {
        long total = 0;
        List<FieldMapInfo> fieldMapTemp = getSelectedFieldMapsToBeAdded();
        if(fieldMapTemp == null)
        	fieldMapTemp = getSelectedFieldMaps();
        for (FieldMapInfo info : fieldMapTemp) {
            for (FieldMapDatasetInfo dataset : info.getDatasets()) {
                for (FieldMapTrialInstanceInfo trial : dataset.getTrialInstances()) {
                    if (isTrial()) {
                        total += trial.getPlotCount();
                    } else {
                        total += trial.getEntryCount();
                    }
                }
            }
        }
        
        return total;
    }

    /**
     * Gets the selected fieldmap list.
     *
     * @return the selectedFieldmapList
     */
    public SelectedFieldmapList getSelectedFieldmapList() {
        return this.selectedFieldmapList;
    }
    
    /**
     * Sets the selected fieldmap list.
     *
     * @param selectedFieldmapList the selectedFieldmapList to set
     */
    public void setSelectedFieldmapList(SelectedFieldmapList selectedFieldmapList) {
        this.selectedFieldmapList = selectedFieldmapList;
    }

	/**
	 * Gets the field id.
	 *
	 * @return the field id
	 */
	public Integer getFieldId() {
		return fieldId;
	}

	/**
	 * Sets the field id.
	 *
	 * @param fieldId the new field id
	 */
	public void setFieldId(Integer fieldId) {
		this.fieldId = fieldId;
	}

	/**
	 * Gets the block id.
	 *
	 * @return the block id
	 */
	public Integer getBlockId() {
		return blockId;
	}

	/**
	 * Sets the block id.
	 *
	 * @param blockId the new block id
	 */
	public void setBlockId(Integer blockId) {
		this.blockId = blockId;
	}

	/**
	 * Gets the deleted plots.
	 *
	 * @return the deletedPlots
	 */
	public List<String> getDeletedPlots() {
		return deletedPlots;
	}

	/**
	 * Sets the deleted plots.
	 *
	 * @param deletedPlots the deletedPlots to set
	 */
	public void setDeletedPlots(List<String> deletedPlots) {
		this.deletedPlots = deletedPlots;
	}

	public List<FieldMapInfo> getSelectedFieldMapsToBeAdded() {
		return selectedFieldMapsToBeAdded;
	}

	public void setSelectedFieldMapsToBeAdded(
			List<FieldMapInfo> selectedFieldMapsToBeAdded) {
		this.selectedFieldMapsToBeAdded = selectedFieldMapsToBeAdded;
	}

	public SelectedFieldmapList getSelectedFieldmapListToBeAdded() {
		return selectedFieldmapListToBeAdded;
	}

	public void setSelectedFieldmapListToBeAdded(
			SelectedFieldmapList selectedFieldmapListToBeAdded) {
		this.selectedFieldmapListToBeAdded = selectedFieldmapListToBeAdded;
	}
    
}
