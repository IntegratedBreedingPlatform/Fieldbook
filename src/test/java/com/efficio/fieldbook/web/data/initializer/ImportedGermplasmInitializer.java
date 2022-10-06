package com.efficio.fieldbook.web.data.initializer;

import org.generationcp.middleware.ruleengine.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;

import java.util.ArrayList;
import java.util.List;

public class ImportedGermplasmInitializer {

	public static List<ImportedGermplasm> createImportedGermplasmList(final int entryNo) {
		final List<ImportedGermplasm> importedGermplasmList = new ArrayList<>();
		for (int x = 0; x < DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES; x++) {
			importedGermplasmList.add(createImportedGermplasm(entryNo + x));
		}

		return importedGermplasmList;
	}
	public static List<ImportedGermplasm> createImportedGermplasmList() {
		final List<ImportedGermplasm> importedGermplasmList = new ArrayList<>();
		for (int x = 1; x <= DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES; x++) {
			importedGermplasmList.add(createImportedGermplasm(x));
		}

		return importedGermplasmList;
	}


	public static ImportedGermplasm createImportedGermplasm(final int entryNo) {
		final ImportedGermplasm importedGermplasm = new ImportedGermplasm();
		importedGermplasm.setEntryNumber(entryNo);
		importedGermplasm.setEntryCode(String.valueOf(entryNo));
		importedGermplasm.setDesig("DESIG" + entryNo);
		importedGermplasm.setSource("SOURCE" + entryNo);
		importedGermplasm.setBreedingMethodId(0);
		importedGermplasm.setEntryTypeValue("");
		importedGermplasm.setGid("");
		importedGermplasm.setEntryTypeCategoricalID(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId());
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
