package com.efficio.fieldbook.web.data.initializer;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;

import java.util.ArrayList;
import java.util.List;

public class ImportedGermplasmMainInfoInitializer {

	public static ImportedGermplasmMainInfo createImportedGermplasmMainInfo() {
		final ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		final ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
		importedGermplasmList.setImportedGermplasms(createImportedGermplasmList());
		mainInfo.setImportedGermplasmList(importedGermplasmList);
		return mainInfo;
	}

	public static List<ImportedGermplasm> createImportedGermplasmList() {
		final List<ImportedGermplasm> importedGermplasmList = new ArrayList<>();
		for (int x = 1; x <= DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES; x++) {
			importedGermplasmList.add(createImportedGermplasm(x));
		}

		return importedGermplasmList;
	}

	public static ImportedGermplasmMainInfo createImportedGermplasmMainInfo(final int startingEntryNo) {
		final ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		final ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
		importedGermplasmList.setImportedGermplasms(createImportedGermplasmList(startingEntryNo));
		mainInfo.setImportedGermplasmList(importedGermplasmList);
		return mainInfo;
	}

	public static List<ImportedGermplasm> createImportedGermplasmList(final int startingEntryNo) {
		final List<ImportedGermplasm> importedGermplasmList = new ArrayList<>();
		for (int x = 0; x < DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES; x++) {
			importedGermplasmList.add(createImportedGermplasm(x + startingEntryNo));
		}

		return importedGermplasmList;
	}

	public static ImportedGermplasm createImportedGermplasm(final int entryNo) {
		final ImportedGermplasm importedGermplasm = new ImportedGermplasm();
		importedGermplasm.setEntryId(entryNo);
		importedGermplasm.setEntryCode(String.valueOf(entryNo));
		importedGermplasm.setDesig("DESIG" + entryNo);
		importedGermplasm.setSource("SOURCE" + entryNo);
		importedGermplasm.setBreedingMethodId(0);
		importedGermplasm.setEntryTypeValue("");
		importedGermplasm.setGid("");
		importedGermplasm.setEntryTypeCategoricalID(0);
		importedGermplasm.setEntryTypeName("");
		importedGermplasm.setCross("");
		importedGermplasm.setGnpgs(0);
		importedGermplasm.setGpid1(0);
		importedGermplasm.setGpid2(0);
		importedGermplasm.setGroupName("");
		importedGermplasm.setIndex(0);
		importedGermplasm.setNames(null);

		return importedGermplasm;
	}
}
