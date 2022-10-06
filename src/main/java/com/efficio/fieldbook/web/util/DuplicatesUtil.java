
package com.efficio.fieldbook.web.util;

import com.google.common.collect.Lists;
import org.generationcp.middleware.ruleengine.pojo.ImportedCross;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DuplicatesUtil {

	public static final String SEPARATOR = "/";
	
	public static final Logger LOG = LoggerFactory.getLogger(DuplicatesUtil.class);

	private DuplicatesUtil() {
		// private constructor for utility class
	}

	public static void processDuplicatesAndReciprocals(ImportedCrossesList parseResults) {
		if (parseResults != null) {
			DuplicatesUtil.detectDuplicationsAndReciprocalsFromImportedCrosses(parseResults);
		}
	}

	private static void setDuplicatePrefixAndEntriesForDuplicates(List<ImportedCross> importedCrossList, String prefix) {
		List<Integer> entries = new ArrayList<>();
		DuplicatesUtil.getAllEntries(importedCrossList, entries);
		for (ImportedCross importedCross : importedCrossList) {
			if (DuplicatesUtil.canStillSetDuplicateNotes(importedCross, prefix)) {
				importedCross.setDuplicatePrefix(prefix);
				DuplicatesUtil.setDuplicateEntries(importedCross, entries);
			}
		}
	}

	private static boolean canStillSetDuplicateNotes(ImportedCross importedCross, String prefix) {
		return importedCross.getDuplicatePrefix() == null
				|| (importedCross.isPlotDupe() && ImportedCross.PLOT_DUPE_PREFIX.equals(prefix))
				|| (importedCross.isPedigreeDupe() && ImportedCross.PEDIGREE_DUPE_PREFIX.equals(prefix))
				|| (importedCross.isPlotRecip() && ImportedCross.PLOT_RECIP_PREFIX.equals(prefix))
				|| (importedCross.isPedigreeRecip() && ImportedCross.PEDIGREE_RECIP_PREFIX.equals(prefix));
	}

	private static void setDuplicateNotesBasedOnPrefixandEntries(ImportedCross importedCross) {
		if (importedCross.getDuplicatePrefix() != null) {
			importedCross.setDuplicate(importedCross.getDuplicatePrefix()
					+ DuplicatesUtil.getCommaSeparatedEntryIdsOfDuplicates(importedCross.getDuplicateEntries()));
		}
	}

	private static void setDuplicateEntries(ImportedCross importedCross, Collection<Integer> entries) {
		if (importedCross.getDuplicateEntries() == null) {
			importedCross.setDuplicateEntries(new TreeSet<>());
		}
		for (Integer entryNumber : entries) {
			if (importedCross.getEntryNumber().equals(entryNumber)) {
				continue;
			}
			importedCross.getDuplicateEntries().add(entryNumber);
		}
	}

	private static String getCommaSeparatedEntryIdsOfDuplicates(Collection<Integer> entries) {
		String entryIDCSV = "";
		for (Integer entryId : entries) {
			entryIDCSV += entryId + ", ";
		}
		return entryIDCSV.substring(0, entryIDCSV.length() - 2);
	}

	private static void getAllEntries(Collection<ImportedCross> importedCrossList, List<Integer> entries) {
		for (ImportedCross importedCross : importedCrossList) {
			entries.add(importedCross.getEntryNumber());
		}
	}

	/**
	 * Function to detect Plot Dupe, Pedigree Dupe, Plot Recip and Pedigree Recip
	 * It will set duplications & reciprocals information in duplicate element of imported crosses object
	 * @param importedCrossesList list of imported crosses
	 */
	private static void detectDuplicationsAndReciprocalsFromImportedCrosses(ImportedCrossesList importedCrossesList) {
		for (ImportedCross importedCrossMain : importedCrossesList.getImportedCrosses()) {
			if (importedCrossMain.getDuplicate() != null) {
				continue;
			}

			final Integer nFemalePlotNo = importedCrossMain.getFemalePlotNo();
			final Integer nFemaleGid = Integer.parseInt(importedCrossMain.getFemaleGid());

			final List<Integer> nMalePlotNo = importedCrossMain.getMalePlotNos();
			final List<Integer> nMaleGid = importedCrossMain.getMaleGids();

			final StringBuilder plotDupePrefix = new StringBuilder(ImportedCross.PLOT_DUPE_PREFIX);
			final StringBuilder pedigreeDupePrefix = new StringBuilder(ImportedCross.PEDIGREE_DUPE_PREFIX);
			final StringBuilder plotRecipPrefix = new StringBuilder(ImportedCross.PLOT_RECIP_PREFIX);
			final StringBuilder pedigreeRecipPrefix = new StringBuilder(ImportedCross.PEDIGREE_RECIP_PREFIX);

			for (ImportedCross possibleDuplicatesAndReciprocals : importedCrossesList.getImportedCrosses()) {
				if (!Objects.equals(importedCrossMain.getEntryNumber(), possibleDuplicatesAndReciprocals.getEntryNumber())) {

					final Integer femaleGidExcludingMain = Integer.parseInt(possibleDuplicatesAndReciprocals.getFemaleGid());
					final Integer femalePlotNoExcludingMain = possibleDuplicatesAndReciprocals.getFemalePlotNo();

					final List<Integer> maleGidExcludingMain = possibleDuplicatesAndReciprocals.getMaleGids();
					final List<Integer> malePlotNoExcludingMain = possibleDuplicatesAndReciprocals.getMalePlotNos();

					boolean containsAllGids =
						maleGidExcludingMain.stream().allMatch(gid -> nMaleGid.contains(gid))
							&& nMaleGid.stream().allMatch(gid -> maleGidExcludingMain.contains(gid));

					boolean containsAllPlots =
						malePlotNoExcludingMain.stream().allMatch(plotNo -> nMalePlotNo.contains(plotNo))
							&& nMalePlotNo.stream().allMatch(plotNo -> malePlotNoExcludingMain.contains(plotNo));

					// Duplicate scenario
					if (femaleGidExcludingMain.equals(nFemaleGid) && containsAllGids) {
						if (Objects.equals(femalePlotNoExcludingMain, nFemalePlotNo) && containsAllPlots) {
							// Plot Dupe
							DuplicatesUtil.setDuplicatePrefixAndEntriesForDuplicates(
								Lists.newArrayList(possibleDuplicatesAndReciprocals),
								ImportedCross.PLOT_DUPE_PREFIX);
							plotDupePrefix.append(possibleDuplicatesAndReciprocals.getEntryNumber() + ", ");
						} else {
							// Pedigree Dupe
							DuplicatesUtil.setDuplicatePrefixAndEntriesForDuplicates(
								Lists.newArrayList(possibleDuplicatesAndReciprocals),
								ImportedCross.PEDIGREE_DUPE_PREFIX);
							pedigreeDupePrefix.append(possibleDuplicatesAndReciprocals.getEntryNumber() + ", ");
						}
						if (importedCrossMain.getDuplicateEntries() == null) {
							importedCrossMain.setDuplicateEntries(new TreeSet<>());
						}
						importedCrossMain.getDuplicateEntries().add(possibleDuplicatesAndReciprocals.getEntryNumber());
						DuplicatesUtil.setDuplicateNotesBasedOnPrefixandEntries(importedCrossMain);
					}

					if (possibleDuplicatesAndReciprocals.getMalePlotNos().size() > 1 || importedCrossMain.getMalePlotNos().size() > 1) {
						continue;
					}
					// Reciprocal scenario
					if (Objects.equals(femaleGidExcludingMain, nMaleGid.get(0)) && Objects
						.equals(maleGidExcludingMain.get(0), nFemaleGid)) {
						if (femalePlotNoExcludingMain.equals(nMalePlotNo.get(0)) && malePlotNoExcludingMain.get(0).equals(nFemalePlotNo)) {
							// Plot Reciprocal
							List<Integer> plotReciprocalEntries = new ArrayList<>();
							DuplicatesUtil.getAllEntries(Lists.newArrayList(possibleDuplicatesAndReciprocals), plotReciprocalEntries);
							importedCrossMain.setDuplicatePrefix(ImportedCross.PLOT_RECIP_PREFIX);
							DuplicatesUtil.setDuplicateEntries(possibleDuplicatesAndReciprocals, plotReciprocalEntries);
							plotRecipPrefix.append(possibleDuplicatesAndReciprocals.getEntryNumber() + ", ");
						} else {
							// Pedigree Reciprocal
							List<Integer> pedigreeReciprocalEntries = new ArrayList<>();
							DuplicatesUtil.getAllEntries(Lists.newArrayList(possibleDuplicatesAndReciprocals), pedigreeReciprocalEntries);
							importedCrossMain.setDuplicatePrefix(ImportedCross.PEDIGREE_RECIP_PREFIX);
							DuplicatesUtil.setDuplicateEntries(possibleDuplicatesAndReciprocals, pedigreeReciprocalEntries);
							pedigreeRecipPrefix.append(possibleDuplicatesAndReciprocals.getEntryNumber() + ", ");
						}
						if (importedCrossMain.getDuplicateEntries() == null) {
							importedCrossMain.setDuplicateEntries(new TreeSet<>());
						}
						importedCrossMain.getDuplicateEntries().add(possibleDuplicatesAndReciprocals.getEntryNumber());
						DuplicatesUtil.setDuplicateNotesBasedOnPrefixandEntries(importedCrossMain);
					}
				}
			}

			final String plotDupePrefixFinal = DuplicatesUtil.removeCommaAndPipeFromEnd(plotDupePrefix.toString());
			final String pedigreeDupePrefixFinal = DuplicatesUtil.removeCommaAndPipeFromEnd(pedigreeDupePrefix.toString());
			final String plotRecipPrefixFinal = DuplicatesUtil.removeCommaAndPipeFromEnd(plotRecipPrefix.toString());
			final String pedigreeRecipPrefixFinal = DuplicatesUtil.removeCommaAndPipeFromEnd(pedigreeRecipPrefix.toString());

			String duplicateString = "";

			duplicateString = DuplicatesUtil.buildDuplicateString(plotDupePrefixFinal, ImportedCross.PLOT_DUPE_PREFIX);
			duplicateString =
					duplicateString + DuplicatesUtil.buildDuplicateString(pedigreeDupePrefixFinal, ImportedCross.PEDIGREE_DUPE_PREFIX);
			duplicateString =
					duplicateString + DuplicatesUtil.buildDuplicateString(plotRecipPrefixFinal, ImportedCross.PLOT_RECIP_PREFIX);
			duplicateString =
					duplicateString + DuplicatesUtil.buildDuplicateString(pedigreeRecipPrefixFinal, ImportedCross.PEDIGREE_RECIP_PREFIX);

			duplicateString = DuplicatesUtil.removeCommaAndPipeFromEnd(duplicateString);
			
			LOG.info("EntryID : " + importedCrossMain.getEntryNumber() + " : " + duplicateString);

			importedCrossMain.setDuplicate(duplicateString);
		}
	}

	/**
	 * Function to remove comma ',' or pipe '|' character at the end of the string
	 * Ex. last recip info will contain pipe like this Pedigree Recip: 5, 6 |
	 * @param prefixString string which contains comma or pipe character at the end
	 * @return string from which we have removed comma or pipe from end
	 */
	private static String removeCommaAndPipeFromEnd(final String prefixString) {
		String prefixFinal = prefixString;
		if(prefixString.endsWith(", ")) {
			int lastIndexOfComma = prefixString.lastIndexOf(", ");
			prefixFinal = prefixString.substring(0, lastIndexOfComma);
		} else if(prefixString.endsWith(" | ")) {
			int lastIndexOfPipe = prefixString.lastIndexOf(" | ");
			prefixFinal = prefixString.substring(0, lastIndexOfPipe);
		}

		return prefixFinal;
	}

	/**
	 * Function to build duplicate string which will be set in duplicate element of imported crosses object
	 * @param prefix contains information about duplication and reciprocals ex. Plot Dupe: 2, 3
	 * @param compareValue will decide if we need to append pipe character at the end or not
	 * If compareValue has no information then no need to append pipe character
	 * @return duplicateString which contains information about duplication & reciprocals ex. Plot Dupe: 2 | Pedigree Recip: 5, 6
	 */
	private static String buildDuplicateString(final String prefix, final String compareValue) {
		String duplicateString = "";
		if (!prefix.equals(compareValue)) {
			duplicateString = prefix + " | ";
		}

		return duplicateString;
	}
}
