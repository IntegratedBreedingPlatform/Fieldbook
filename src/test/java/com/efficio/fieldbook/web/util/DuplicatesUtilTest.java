
package com.efficio.fieldbook.web.util;

import java.util.HashMap;
import java.util.Map;

import org.generationcp.middleware.ruleengine.pojo.ImportedCross;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.middleware.ruleengine.pojo.ImportedGermplasmParent;
import org.generationcp.middleware.util.Debug;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

public class DuplicatesUtilTest {

	/**
	 * Test to verify processDuplicates() method works as expected or not
	 * This will build required test data and assert each imported crosses.
	 * Each Imported crosses should have valid duplicate string.
	 * Ex. For EntryId=1, Plot Dupe: 15 | Pedigree Dupe: 12, 13 | Pedigree Recip: 10, 14
	 * 
	 * SEE src/test/resources/DowntonDupesRecipsResults.png for explanation of test case
	 */
	@Test
	public void testProcessDuplicates() {
		Map<Integer, Integer> plotNoToGidTestData = this.createPlotNoToGidTestData();
		ImportedCrossesList parseResults = this.createImportedCrossesListTestData(plotNoToGidTestData);

		Debug.println("BEFORE: ");
		this.debugTestData(parseResults);
		DuplicatesUtil.processDuplicatesAndReciprocals(parseResults);
		Debug.println("AFTER: ");
		this.debugTestData(parseResults);
		int entryId = 1;
		final String pipeCharacter = " | ";
		for (ImportedCross importedCross : parseResults.getImportedCrosses()) {
			switch (entryId) {
				case 1:
					Assert.assertEquals(
						ImportedCross.PLOT_DUPE_PREFIX + "2" + pipeCharacter + ImportedCross.PEDIGREE_DUPE_PREFIX + "5" + pipeCharacter +
							ImportedCross.PLOT_RECIP_PREFIX + "3", importedCross.getDuplicate());
					break;
				case 2:
					Assert.assertEquals(
						ImportedCross.PLOT_DUPE_PREFIX + "1" + pipeCharacter + ImportedCross.PEDIGREE_DUPE_PREFIX + "5" + pipeCharacter +
							ImportedCross.PLOT_RECIP_PREFIX + "3", importedCross.getDuplicate());
				break;
				case 3:
					Assert.assertEquals(ImportedCross.PLOT_RECIP_PREFIX + "1, 2"+ pipeCharacter + ImportedCross.PEDIGREE_RECIP_PREFIX + "5", importedCross
						.getDuplicate());
				break;
				case 4:
					Assert.assertEquals(ImportedCross.PEDIGREE_DUPE_PREFIX + "6" + pipeCharacter + ImportedCross.PEDIGREE_RECIP_PREFIX + "7", importedCross
						.getDuplicate());
					break;
				case 5:
					Assert.assertEquals(ImportedCross.PEDIGREE_DUPE_PREFIX + "1, 2" + pipeCharacter + ImportedCross.PEDIGREE_RECIP_PREFIX + "3", importedCross
						.getDuplicate());
					break;
				case 6:
					Assert.assertEquals(ImportedCross.PEDIGREE_DUPE_PREFIX + "4" + pipeCharacter + ImportedCross.PLOT_RECIP_PREFIX + "7", importedCross
						.getDuplicate());
					break;
				case 7:
					Assert.assertEquals(ImportedCross.PLOT_RECIP_PREFIX + "6" + pipeCharacter + ImportedCross.PEDIGREE_RECIP_PREFIX + "4", importedCross
						.getDuplicate());
					break;

				default:
					Assert.assertEquals(ImportedCross.PEDIGREE_DUPE_PREFIX + "12, 13", importedCross.getDuplicate());
			}
			entryId++;
		}
	}

	/**
	 * Create test data using Map which builds Plot Number and its Gid data
	 * @return plotNoToGidTestData which contains plot number and its gid data
	 */
	private Map<Integer, Integer> createPlotNoToGidTestData() {
		Map<Integer, Integer> plotNoToGidTestData = new HashMap<Integer, Integer>();
		plotNoToGidTestData.put(1, 123);
		plotNoToGidTestData.put(8, 456);
		plotNoToGidTestData.put(11, 789);
		plotNoToGidTestData.put(16, 456);
		return plotNoToGidTestData;
	}

	/**
	 * Make ImportedCrossesList which contains information about crosses
	 * This will build female and male gid, study name, female and male designation
	 * @param plotNoToGidTestData used to get gid from plot number
	 * @return importedCrossesList
	 */
	private ImportedCrossesList createImportedCrossesListTestData(Map<Integer, Integer> plotNoToGidTestData) {
		ImportedCrossesList importedCrossesList = new ImportedCrossesList();
		for (int i = 1; i <= 7; i++) {
			Integer femalePlotNo = i;
			Integer malePlotNo = i;
			switch (i) {
				case 1:
					femalePlotNo = 16;
					malePlotNo = 1;
					break;
				case 2:
					femalePlotNo = 16;
					malePlotNo = 1;
					break;
				case 3:
					femalePlotNo = 1;
					malePlotNo = 16;
					break;
				case 4:
					femalePlotNo = 16;
					malePlotNo = 11;
					break;
				case 5:
					femalePlotNo = 8;
					malePlotNo = 1;
					break;
				case 6:
					femalePlotNo = 8;
					malePlotNo = 11;
					break;
				case 7:
					femalePlotNo = 11;
					malePlotNo = 8;
					break;

			}
			Integer femaleGid = plotNoToGidTestData.get(femalePlotNo);
			Integer maleGid = plotNoToGidTestData.get(malePlotNo);
			String femaleStudyName = "FNursery";
			String maleStudyName = "MNursery";
			String femaleDesig = femaleGid.toString();
			String maleDesig =  maleGid.toString();
			String source = femaleStudyName + ":" + i + " " + DuplicatesUtil.SEPARATOR + " " + maleStudyName + ":" + (i + 10);
			importedCrossesList.addImportedCrosses(this.createImportedCrossesTestData(femaleDesig, maleDesig, femaleGid, maleGid, i,
					source, femalePlotNo, malePlotNo));
		}
		return importedCrossesList;
	}

	private void debugTestData(ImportedCrossesList importedCrossesList) {
		for (ImportedCross importedCross : importedCrossesList.getImportedCrosses()) {
			Debug.println(5, this.reviewImportedCrosses(importedCross));
		}
	}

	private String reviewImportedCrosses(ImportedCross importedCross) {
		return "ENTRY=" + importedCross.getEntryNumber() + "\t" + "FEMALE_PLOTNO=" + importedCross.getFemalePlotNo() + "\t"
				+ "MALE_PLOTNO=" + importedCross.getMalePlotNos().get(0) + "\t" + "FEMALE_GID=" + importedCross.getFemaleGid() + "\t"
				+ "MALE_GID=" + importedCross.getMaleGids().get(0) + "\t" + "PARENTAGE=" + importedCross.getCross() + "\t" + "DUPLICATE="
				+ importedCross.getDuplicate();
	}

	/**
	 * Method to set data in Imported Crosses
	 * @param femaleDesig Female Designation
	 * @param maleDesig Male Designation
	 * @param femaleGid Female Gid
	 * @param maleGid Male Gid
	 * @param entryId Entry Id
	 * @param source source
	 * @param femalePlotNo Female Plot Number
	 * @param malePlotNo Male Plot Number
	 * @return importedCrosses
	 */
	private ImportedCross createImportedCrossesTestData(String femaleDesig, String maleDesig, Integer femaleGid, Integer maleGid,
			Integer entryId, String source, Integer femalePlotNo, Integer malePlotNo) {
		ImportedCross importedCross = new ImportedCross();
		
		final ImportedGermplasmParent femaleParent = new ImportedGermplasmParent(femaleGid, femaleDesig, "");
		femaleParent.setPlotNo(femalePlotNo);
		importedCross.setFemaleParent(femaleParent);
		final ImportedGermplasmParent maleParent = new ImportedGermplasmParent(maleGid, maleDesig, "");
		maleParent.setPlotNo(malePlotNo);
		importedCross.setMaleParents(Lists.newArrayList(maleParent));
		importedCross.setEntryNumber(entryId);
		importedCross.setCross(importedCross.getFemaleDesignation() + DuplicatesUtil.SEPARATOR + importedCross.getMaleDesignationsAsString());
		importedCross.setSource(source);
		importedCross.setEntryCode(String.valueOf(entryId));
		return importedCross;
	}
}
