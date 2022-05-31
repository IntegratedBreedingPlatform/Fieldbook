package com.efficio.fieldbook.web.study.germplasm;

import org.apache.commons.lang.StringUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.interfaces.GermplasmExportSource;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryPropertyData;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Configurable
// FIXME: IBP-3697 Is it possible to use ModelMapper instead???
public class StudyEntryTransformer {

	public StudyEntryTransformer() {
		// this constructor is necessary for aop proxy
	}

	@Resource
	private OntologyService ontologyService;

	@Resource
	private ContextUtil contextUtil;

	public List<ImportedGermplasm> tranformToImportedGermplasm(final List<StudyEntryDto> studyEntries) {

		final Map<Integer, String> checkTypesDescriptionMap =
			this.ontologyService.getStandardVariable(TermId.ENTRY_TYPE.getId(), this.contextUtil.getCurrentProgramUUID())
				.getEnumerations().stream().collect(Collectors.toMap(Enumeration::getId, Enumeration::getDescription));

		final List<ImportedGermplasm> importedGermplasmList = new ArrayList<>();
		if (studyEntries != null && !studyEntries.isEmpty()) {
			for (final StudyEntryDto studyEntryDto : studyEntries) {
				final ImportedGermplasm importedGermplasm = new ImportedGermplasm();
				importedGermplasm.setId(studyEntryDto.getEntryId());
				final Optional<String> entryType = studyEntryDto.getStudyEntryPropertyValue(TermId.ENTRY_TYPE.getId());
				if (entryType.isPresent()) {
					final Integer entryTypeCategoricalId = Integer.valueOf(entryType.get());
					importedGermplasm.setEntryTypeName(checkTypesDescriptionMap.getOrDefault(entryTypeCategoricalId, ""));
					importedGermplasm.setEntryTypeValue(entryType.get());
					importedGermplasm.setEntryTypeCategoricalID(entryTypeCategoricalId);
				}
				importedGermplasm.setCross(studyEntryDto.getCross());
				importedGermplasm.setDesig(studyEntryDto.getDesignation());
				importedGermplasm.setEntryCode(studyEntryDto.getStudyEntryPropertyValue(TermId.ENTRY_CODE.getId()).orElse(""));
				importedGermplasm.setEntryNumber(studyEntryDto.getEntryNumber());
				importedGermplasm.setGid(studyEntryDto.getGid().toString());
				final Optional<Integer> groupGid =  Optional.ofNullable(studyEntryDto.getGroupGid());
				if (groupGid.isPresent()) {
					final Integer mgid = Integer.valueOf(groupGid.get());
					importedGermplasm.setMgid(mgid);
					importedGermplasm.setGroupId(mgid);
				}
				//FIXME: The GroupName is the Preferred Name of the groupGID
				//importedGermplasm.setGroupName(studyEntryDto.getStudyEntryPropertyValue(TermId.CROSS.getId()).orElse("")); //
				importedGermplasm.setIndex(studyEntryDto.getEntryNumber());
				importedGermplasmList.add(importedGermplasm);
			}
		}
		return importedGermplasmList;
	}

}
