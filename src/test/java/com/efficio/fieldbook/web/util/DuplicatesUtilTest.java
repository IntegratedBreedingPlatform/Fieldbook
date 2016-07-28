
package com.efficio.fieldbook.web.util;

import java.util.HashMap;
import java.util.Map;

import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.middleware.util.Debug;
import org.junit.Assert;
import org.junit.Test;

public class DuplicatesUtilTest {

	/**
	 * Test to verify processDuplicates() method works as expected or not
	 * This will build required test data and assert each imported crosses.
	 * Each Imported crosses should have valid duplicate string.
	 * Ex. For EntryId=1, Plot Dupe: 15 | Pedigree Dupe: 12, 13 | Pedigree Recip: 10, 14
	 */
	@Test
	public void testProcessDuplicates() {
		Map<String, String> plotNoToGidTestData = this.createPlotNoToGidTestData();
		ImportedCrossesList parseResults = this.createImportedCrossesListTestData(plotNoToGidTestData);

		Debug.println("BEFORE: ");
		this.debugTestData(parseResults);
		DuplicatesUtil.processDuplicates(parseResults);
		Debug.println("AFTER: ");
		this.debugTestData(parseResults);
		int entryId = 1;
		final String pipeCharacter = " | ";
		for (ImportedCrosses importedCrosses : parseResults.getImportedCrosses()) {
			switch (entryId) {
				case 1:
					Assert.assertEquals(ImportedCrosses.PLOT_DUPE_PREFIX + "15" + pipeCharacter + ImportedCrosses.PEDIGREE_DUPE_PREFIX + "12, 13" + pipeCharacter +
							ImportedCrosses.PEDIGREE_RECIP_PREFIX + "10, 14", importedCrosses.getDuplicate());
					break;
				case 2:
					Assert.assertEquals(ImportedCrosses.PLOT_DUPE_PREFIX + "15" + pipeCharacter + ImportedCrosses.PEDIGREE_DUPE_PREFIX + "12, 13" + pipeCharacter +
							ImportedCrosses.PEDIGREE_RECIP_PREFIX + "10, 14", importedCrosses.getDuplicate());
					break;
				case 3:
					Assert.assertEquals(ImportedCrosses.PLOT_DUPE_PREFIX + "15" + pipeCharacter + ImportedCrosses.PEDIGREE_DUPE_PREFIX + "12, 13" + pipeCharacter +
							ImportedCrosses.PEDIGREE_RECIP_PREFIX + "10, 14", importedCrosses.getDuplicate());
					break;
				case 4:
					Assert.assertEquals(ImportedCrosses.PLOT_DUPE_PREFIX + "15" + pipeCharacter + ImportedCrosses.PEDIGREE_DUPE_PREFIX + "12, 13" + pipeCharacter +
							ImportedCrosses.PEDIGREE_RECIP_PREFIX + "10, 14", importedCrosses.getDuplicate());
					break;
				case 5:
					Assert.assertEquals(ImportedCrosses.PLOT_DUPE_PREFIX + "15" + pipeCharacter + ImportedCrosses.PEDIGREE_DUPE_PREFIX + "12, 13" + pipeCharacter +
							ImportedCrosses.PLOT_RECIP_PREFIX + "6" + pipeCharacter + ImportedCrosses.PEDIGREE_RECIP_PREFIX + "11", importedCrosses.getDuplicate());
					break;
				case 6:
					Assert.assertEquals(ImportedCrosses.PLOT_DUPE_PREFIX + "15" + pipeCharacter + ImportedCrosses.PEDIGREE_DUPE_PREFIX + "12, 13" + pipeCharacter +
							ImportedCrosses.PLOT_RECIP_PREFIX + "5", importedCrosses.getDuplicate());
					break;
				case 7:
					Assert.assertEquals(ImportedCrosses.PLOT_DUPE_PREFIX + "15" + pipeCharacter + ImportedCrosses.PEDIGREE_DUPE_PREFIX + "12, 13" + pipeCharacter +
							ImportedCrosses.PEDIGREE_RECIP_PREFIX + "10, 14", importedCrosses.getDuplicate());
					break;
				case 8:
					Assert.assertEquals(ImportedCrosses.PLOT_DUPE_PREFIX + "15" + pipeCharacter + ImportedCrosses.PEDIGREE_DUPE_PREFIX + "12, 13" + pipeCharacter +
							ImportedCrosses.PEDIGREE_RECIP_PREFIX + "9", importedCrosses.getDuplicate());
					break;
				case 9:
					Assert.assertEquals(ImportedCrosses.PLOT_DUPE_PREFIX + "15" + pipeCharacter + ImportedCrosses.PEDIGREE_DUPE_PREFIX + "12, 13" + pipeCharacter +
							ImportedCrosses.PEDIGREE_RECIP_PREFIX + "8", importedCrosses.getDuplicate());
					break;
				case 10:
					Assert.assertEquals(ImportedCrosses.PLOT_DUPE_PREFIX + "15" + pipeCharacter + ImportedCrosses.PEDIGREE_DUPE_PREFIX + "12, 13" + pipeCharacter +
							ImportedCrosses.PEDIGREE_RECIP_PREFIX + "1, 2, 3, 4, 7", importedCrosses.getDuplicate());
					break;
				case 11:
					Assert.assertEquals(ImportedCrosses.PLOT_DUPE_PREFIX + "15" + pipeCharacter + ImportedCrosses.PEDIGREE_DUPE_PREFIX + "12, 13" + pipeCharacter +
							ImportedCrosses.PEDIGREE_RECIP_PREFIX + "5", importedCrosses.getDuplicate());
					break;
				case 12:
					Assert.assertEquals(ImportedCrosses.PLOT_DUPE_PREFIX + "15" + pipeCharacter + ImportedCrosses.PEDIGREE_DUPE_PREFIX + "13" + pipeCharacter +
							ImportedCrosses.PLOT_RECIP_PREFIX + "13", importedCrosses.getDuplicate());
					break;
				case 13:
					Assert.assertEquals(ImportedCrosses.PLOT_DUPE_PREFIX + "15" + pipeCharacter + ImportedCrosses.PEDIGREE_DUPE_PREFIX + "12" + pipeCharacter +
							ImportedCrosses.PLOT_RECIP_PREFIX + "12", importedCrosses.getDuplicate());
					break;
				case 14:
					Assert.assertEquals(ImportedCrosses.PLOT_DUPE_PREFIX + "15" + pipeCharacter + ImportedCrosses.PEDIGREE_DUPE_PREFIX + "12, 13" + pipeCharacter +
							ImportedCrosses.PEDIGREE_RECIP_PREFIX + "1, 2, 3, 4, 7", importedCrosses.getDuplicate());
					break;
				default:
					Assert.assertEquals(ImportedCrosses.PEDIGREE_DUPE_PREFIX + "12, 13", importedCrosses.getDuplicate());
			}
			entryId++;
		}
	}

	/**
	 * Create test data using Map which builds Plot Number and its Gid data
	 * @return plotNoToGidTestData which contains plot number and its gid data
	 */
	private Map<String, String> createPlotNoToGidTestData() {
		Map<String, String> plotNoToGidTestData = new HashMap<String, String>();
		plotNoToGidTestData.put("1", "1");
		plotNoToGidTestData.put("2", "1");
		plotNoToGidTestData.put("3", "3");
		plotNoToGidTestData.put("4", "3");
		plotNoToGidTestData.put("5", "5");
		plotNoToGidTestData.put("6", "6");
		plotNoToGidTestData.put("7", "3");
		plotNoToGidTestData.put("8", "4");
		plotNoToGidTestData.put("9", "4");
		plotNoToGidTestData.put("10", "1");
		plotNoToGidTestData.put("11", "2");
		plotNoToGidTestData.put("12", "1");
		plotNoToGidTestData.put("13", "2");
		plotNoToGidTestData.put("14", "1");
		plotNoToGidTestData.put("15", "6");
		return plotNoToGidTestData;
	}

	/**
	 * Make ImportedCrossesList which contains information about crosses
	 * This will build female and male gid, study name, female and male designation
	 * @param plotNoToGidTestData used to get gid from plot number
	 * @return importedCrossesList
	 */
	private ImportedCrossesList createImportedCrossesListTestData(Map<String, String> plotNoToGidTestData) {
		ImportedCrossesList importedCrossesList = new ImportedCrossesList();
		for (int i = 1; i <= 15; i++) {
			String femalePlotNo = Integer.toString(i);
			String malePlotNo = Integer.toString(i);
			switch (i) {
				case 1:
				case 2:
					femalePlotNo = "3";
					break;
				case 3:
					femalePlotNo = "4";
					malePlotNo = "2";
					break;
				case 4:
					malePlotNo = "1";
					break;
				case 5:
					malePlotNo = "6";
					break;
				case 6:
					malePlotNo = "5";
					break;
				case 7:
					malePlotNo = "12";
					break;
				case 8:
					femalePlotNo = "13";
					break;
				case 9:
					malePlotNo = "11";
					break;
				case 10:
					malePlotNo = "7";
					break;
				case 11:
					femalePlotNo = "15";
					malePlotNo = "5";
					break;
				case 12:
					femalePlotNo = "13";
					malePlotNo = "11";
					break;
				case 13:
					femalePlotNo = "11";
					malePlotNo = "13";
					break;
				case 14:
					malePlotNo = "7";
					break;

			}
			String femaleGid = plotNoToGidTestData.get(femalePlotNo);
			String maleGid = plotNoToGidTestData.get(malePlotNo);
			String femaleStudyName = "FNursery";
			String maleStudyName = "MNursery";
			String femaleDesig = "DESIG" + femaleGid;
			String maleDesig = "DESIG" + maleGid;
			String source = femaleStudyName + ":" + i + " " + DuplicatesUtil.SEPARATOR + " " + maleStudyName + ":" + (i + 10);
			importedCrossesList.addImportedCrosses(this.createImportedCrossesTestData(femaleDesig, maleDesig, femaleGid, maleGid, i,
					source, femalePlotNo, malePlotNo));
		}
		return importedCrossesList;
	}

	private void debugTestData(ImportedCrossesList importedCrossesList) {
		for (ImportedCrosses importedCrosses : importedCrossesList.getImportedCrosses()) {
			Debug.println(5, this.reviewImportedCrosses(importedCrosses));
		}
	}

	private String reviewImportedCrosses(ImportedCrosses importedCrosses) {
		return "ENTRY=" + importedCrosses.getEntryId() + "\t" + "FEMALE_PLOTNO=" + importedCrosses.getFemalePlotNo() + "\t"
				+ "MALE_PLOTNO=" + importedCrosses.getMalePlotNo() + "\t" + "FEMALE_GID=" + importedCrosses.getFemaleGid() + "\t"
				+ "MALE_GID=" + importedCrosses.getMaleGid() + "\t" + "PARENTAGE=" + importedCrosses.getCross() + "\t" + "DUPLICATE="
				+ importedCrosses.getDuplicate();
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
	private ImportedCrosses createImportedCrossesTestData(String femaleDesig, String maleDesig, String femaleGid, String maleGid,
			Integer entryId, String source, String femalePlotNo, String malePlotNo) {
		ImportedCrosses importedCrosses = new ImportedCrosses();
		importedCrosses.setFemaleDesig(femaleDesig);
		importedCrosses.setMaleDesig(maleDesig);
		importedCrosses.setMaleGid(maleGid);
		importedCrosses.setFemaleGid(femaleGid);
		importedCrosses.setEntryId(entryId);
		importedCrosses.setCross(importedCrosses.getFemaleDesig() + DuplicatesUtil.SEPARATOR + importedCrosses.getMaleDesig());
		importedCrosses.setSource(source);
		importedCrosses.setEntryCode(String.valueOf(entryId));
		importedCrosses.setFemalePlotNo(femalePlotNo);
		importedCrosses.setMalePlotNo(malePlotNo);
		return importedCrosses;
	}
}
