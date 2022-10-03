
package com.efficio.fieldbook.web.common.service;

import com.efficio.fieldbook.web.common.exception.InvalidInputException;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.middleware.ruleengine.pojo.ImportedCross;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.middleware.ruleengine.settings.CrossNameSetting;
import org.generationcp.middleware.ruleengine.settings.CrossSetting;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.pojos.Germplasm;
import org.springframework.web.multipart.MultipartFile;

public interface CrossingService {

	ImportedCrossesList parseFile(MultipartFile file) throws FileParsingException;

	String getCross(Germplasm germplasm, ImportedCross crosses, String separator, String cropName);

	boolean applyCrossSetting(CrossSetting crossSetting, ImportedCrossesList importedCrossesList, Workbook workbook);

	boolean applyCrossSettingWithNamingRules(CrossSetting crossSetting, ImportedCrossesList importedCrossesList, Integer userId, Workbook
			workbook);

	void processCrossBreedingMethod(CrossSetting crossSetting, ImportedCrossesList importedCrossesList);

	void populateSeedSource(ImportedCrossesList importedCrossesList, final Workbook workbook);

	String getNextNameInSequence(final CrossNameSetting setting) throws InvalidInputException;
}
