
package com.efficio.fieldbook.web.util;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.parsing.pojo.ImportedCross;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmParent;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.germplasm.GermplasmParent;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CrossesListUtil {

	static final String MULTIPARENT_BEGIN_CHAR = "[";
	static final String MULTIPARENT_END_CHAR = "]";
	public static final String TABLE_HEADER_LIST = "tableHeaderList";
	public static final String LIST_DATA_TABLE = "listDataTable";
	public static final String IS_IMPORT = "isImport";
	private static final String BREEDING_METHOD_PENDING = "Pending";
	public static final int ENTRY_INDEX = 1;
	public static final int PARENTAGE_INDEX = 2;
	public static final int DUPLICATE_INDEX = 3;
	public static final int FEMALE_PEDIGREE = 4;
	static final int FEMALE_CROSS = 5;
	public static final int MALE_PEDIGREE = 6;
	static final int MALE_CROSS = 7;
	public static final int BREEDING_METHOD_INDEX = 8;
	public static final int SOURCE_INDEX = 9;
	public static final int FGID_INDEX = 10;
	public static final int MGID_INDEX = 11;
	static final String ALERTS = "ALERTS";

	@Autowired
	private OntologyDataManager ontologyDataManager;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	private static final String DEFAULT_SEPARATOR = "/";

	public ImportedCross convertGermplasmListDataToImportedCrosses(final GermplasmListData crossesData, final String studyName, final Map<Integer, StudyEntryDto> plotEntriesMap) {
		final ImportedCross importedCross = new ImportedCross();
		importedCross.setId(crossesData.getId());
		importedCross.setEntryNumber(crossesData.getEntryId());
		importedCross.setGid(crossesData.getGid() != null ? Integer.toString(crossesData.getGid()) : null);
		importedCross.setSource(crossesData.getSeedSource());

		final GermplasmParent femaleParentFromDB = crossesData.getFemaleParent();
		final Integer femaleParentGid = femaleParentFromDB.getGid();
		final ImportedGermplasmParent femaleParent = new ImportedGermplasmParent(femaleParentGid, femaleParentFromDB.getDesignation(), femaleParentFromDB.getPedigree());
		femaleParent.setCross(femaleParent.getDesignation());
		femaleParent.setStudyName(studyName);
		femaleParent.setPlotNo(this.getParentPlotNo(femaleParentGid, plotEntriesMap));
		importedCross.setFemaleParent(femaleParent);

		final List<ImportedGermplasmParent> maleParents = new ArrayList<>();
		for (final GermplasmParent maleParentFromDB : crossesData.getMaleParents()) {
			final Integer maleParentGid = maleParentFromDB.getGid();
			final ImportedGermplasmParent maleParent = new ImportedGermplasmParent(maleParentGid, maleParentFromDB.getDesignation(), maleParentFromDB.getPedigree());
			maleParent.setCross(maleParent.getDesignation());
			maleParent.setStudyName(studyName);
			maleParent.setPlotNo(this.getParentPlotNo(maleParentGid, plotEntriesMap));
			maleParents.add(maleParent);
		}
		importedCross.setMaleParents(maleParents);
		importedCross.setCross(femaleParent.getDesignation() + CrossesListUtil.DEFAULT_SEPARATOR + this.concatenateMaleParentsValue(this.getDesignationsList(crossesData.getMaleParents())));

		return importedCross;
	}

	// Look at the study germplasm list with plot to find plot number assigned to the male/female parent germplasm of the cross.
	Integer getParentPlotNo(final Integer parentGid, final Map<Integer, StudyEntryDto> plotEntriesMap) {
		// If the parent is unknown or not from the study, parent plot number is null
		Integer parentPlotNo = null;
		if (!parentGid.equals(0)) {
			for (final Map.Entry<Integer, StudyEntryDto> row : plotEntriesMap.entrySet()) {
				final String plotNumber = row.getKey().toString();
				final Integer gid = row.getValue().getGid();
				if (gid != null && gid.equals(parentGid) && plotNumber != null) {
					parentPlotNo = Integer.valueOf(plotNumber);
				}
			}
		}
		return parentPlotNo;
	}

	public Map<String, Object> generateCrossesTableWithDuplicationNotes(final List<String> tableHeaderList,
		final ImportedCross importedCross, final boolean checkExistingCrosses) {

		final Map<String, Object> dataMap = new HashMap<>();
		if(checkExistingCrosses) {
			final Optional<Integer> optionalGid = importedCross.getGid() == null? Optional.empty(): Optional.of(Integer.valueOf(importedCross.getGid()));
			dataMap.put(
				ALERTS, this.germplasmDataManager
					.hasExistingCrosses(Integer.valueOf(importedCross.getFemaleGid()), importedCross.getMaleGids(),
						optionalGid));
		} else  {
			dataMap.put(ALERTS, false);
		}
		dataMap.put(tableHeaderList.get(CrossesListUtil.ENTRY_INDEX), importedCross.getEntryNumber());
		dataMap.put(tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX), importedCross.getCross());
		dataMap.put(ColumnLabels.DUPLICATE.name(), importedCross.getDuplicate());
		dataMap.put(tableHeaderList.get(CrossesListUtil.FEMALE_PEDIGREE), importedCross.getFemalePedigree());
		dataMap.put(ColumnLabels.FEMALE_PARENT.name(), importedCross.getFemaleCross());
		dataMap.put(tableHeaderList.get(CrossesListUtil.MALE_PEDIGREE), this.concatenateMaleParentsValue(importedCross.getMalePedigree()));
		dataMap.put(ColumnLabels.MALE_PARENT.name(), importedCross.getMaleCross());

		//shows BREEDING_METHOD as "Pending" if method is not defined in import crossing file
		String breedingMethodName = importedCross.getBreedingMethodName();
		if (StringUtils.isBlank(breedingMethodName)) {
			breedingMethodName = importedCross.getRawBreedingMethod();
		}
		//shows BREEDING_METHOD as "Pending" if method is not defined in import crossing file
		if (StringUtils.isBlank(breedingMethodName)) {
			breedingMethodName = BREEDING_METHOD_PENDING;
		}

		dataMap.put(tableHeaderList.get(CrossesListUtil.BREEDING_METHOD_INDEX), breedingMethodName);
		dataMap.put(tableHeaderList.get(CrossesListUtil.SOURCE_INDEX), importedCross.getSource());
		dataMap.put(ColumnLabels.FGID.name(), importedCross.getFemaleGid());
		dataMap.put(ColumnLabels.MGID.name(), importedCross.getMaleGids());
		dataMap.put(ColumnLabels.BREEDING_METHOD_NUMBER.getName(), importedCross.getBreedingMethodId());
		dataMap.put(ColumnLabels.GID.name(), importedCross.getGid());
		return dataMap;
	}

	private String getTermNameFromOntology(final ColumnLabels columnLabels) {
		return columnLabels.getTermNameFromOntology(this.ontologyDataManager);
	}

	public void setOntologyDataManager(final OntologyDataManager ontologyDataManager) {
		this.ontologyDataManager = ontologyDataManager;
	}

	public List<String> getTableHeaders() {

		final List<String> tableHeaderList = new ArrayList<>();
		tableHeaderList.add(ALERTS);
		tableHeaderList.add("#");
		tableHeaderList.add("CROSS");
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.DUPLICATE));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.CROSS_FEMALE_GID));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.FEMALE_PARENT));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.CROSS_MALE_GID));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.MALE_PARENT));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.BREEDING_METHOD_NAME));
		tableHeaderList.add(ColumnLabels.GERMPLASM_ORIGIN.name());
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.FGID));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.MGID));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.BREEDING_METHOD_NUMBER));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.GID));
		return tableHeaderList;
	}

	private List<String> getDesignationsList(final List<GermplasmParent> parents) {
		return parents.stream().map(GermplasmParent::getDesignation).collect(Collectors.toList());
	}

	private String concatenateMaleParentsValue(final List<String> list) {
		if (list.size() == 1) {
			return list.get(0);
		}
		return MULTIPARENT_BEGIN_CHAR + StringUtils.join(list, ",") + MULTIPARENT_END_CHAR;
	}

	void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

}
