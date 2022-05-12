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
				importedGermplasm.setCross(studyEntryDto.getStudyEntryPropertyValue(TermId.CROSS.getId()).orElse(""));
				importedGermplasm.setDesig(studyEntryDto.getDesignation());
				// TODO: should get entry code from properties?
//				importedGermplasm.setEntryCode(studyEntryDto.getEntryCode());
				importedGermplasm.setEntryNumber(studyEntryDto.getEntryNumber());
				importedGermplasm.setGid(studyEntryDto.getGid().toString());
				final Optional<String> groupGid = studyEntryDto.getStudyEntryPropertyValue(TermId.GROUPGID.getId());
				if (groupGid.isPresent()) {
					final Integer mgid = Integer.valueOf(groupGid.get());
					importedGermplasm.setMgid(mgid);
					importedGermplasm.setGroupId(mgid);
				}
				importedGermplasm.setGroupName(studyEntryDto.getStudyEntryPropertyValue(TermId.CROSS.getId()).orElse(""));
				importedGermplasm.setIndex(studyEntryDto.getEntryNumber());
				importedGermplasmList.add(importedGermplasm);
			}
		}
		return importedGermplasmList;
	}

	public List<GermplasmExportSource> tranformToGermplasmExportSource(final List<StudyEntryDto> studyEntries) {

		final Map<Integer, String> checkTypesDescriptionMap =
			this.ontologyService.getStandardVariable(TermId.ENTRY_TYPE.getId(), this.contextUtil.getCurrentProgramUUID())
				.getEnumerations().stream().collect(Collectors.toMap(Enumeration::getId, Enumeration::getDescription));

		final List<GermplasmExportSource> germplasmExportSourceList = new ArrayList<>();
		if (studyEntries != null && !studyEntries.isEmpty()) {
			for (final StudyEntryDto studyEntryDto : studyEntries) {
				final GermplasmExportSource germplasmExportSource = new GermplasmExportSource() {

					@Override
					public Integer getGermplasmId() {
						return studyEntryDto.getGid();
					}

					@Override
					public Integer getCheckType() {
						final Optional<String> entryType = studyEntryDto.getStudyEntryPropertyValue(TermId.ENTRY_TYPE.getId());
						return  entryType.isPresent()? Integer.valueOf(entryType.get()) : 0;
					}

					@Override
					public String getCheckTypeDescription() {
						return checkTypesDescriptionMap.getOrDefault(getCheckType(), "");
					}

					@Override
					public Integer getEntryId() {
						return studyEntryDto.getEntryNumber();
					}

					@Override
					public String getEntryCode() {
						// TODO: should get entry code from properties?
						return null;
					}

					@Override
					public String getSeedSource() {
						return null; //studyEntryDto.getStudyEntryPropertyValue(TermId.SEED_SOURCE.getId()).orElse("");
					}

					@Override
					public String getDesignation() {
						return studyEntryDto.getDesignation();
					}

					@Override
					public String getGroupName() {
						return studyEntryDto.getStudyEntryPropertyValue(TermId.CROSS.getId()).orElse("");
					}

					@Override
					public String getFemaleParentDesignation() {
						return null;
					}

					@Override
					public Integer getFemaleGid() {
						return null;
					}

					@Override
					public String getMaleParentDesignation() {
						return null;
					}

					@Override
					public Integer getMaleGid() {
						return null;
					}

					@Override
					public String getStockIDs() {
						return StringUtils.EMPTY;
					}

					@Override
					public String getSeedAmount() {
						return null;
					}

					@Override
					public Integer getGroupId() {
						final Optional<String> groupGid = studyEntryDto.getStudyEntryPropertyValue(TermId.GROUPGID.getId());
						return  groupGid.isPresent()? Integer.valueOf(groupGid.get()) : 0;
					}

					@Override
					public String getNotes() {
						return null;
					}

					@Override
					public Integer getListDataId() {
						return null;
					}
				};

				germplasmExportSourceList.add(germplasmExportSource);
			}
		}

		return germplasmExportSourceList;
	}

	public List<StudyEntryDto> transformToStudyEntryDto(final List<ImportedGermplasm> importedGermplasmList) {

		final List<StudyEntryDto> list = new ArrayList<>();
		for (final ImportedGermplasm importedGermplasm : importedGermplasmList) {
			final StudyEntryDto dto = new StudyEntryDto();
			dto.setDesignation(importedGermplasm.getDesig());
			dto.setGid(Integer.valueOf(importedGermplasm.getGid()));
			dto.setEntryNumber(importedGermplasm.getEntryNumber());
			dto.getProperties().put(TermId.ENTRY_TYPE.getId(),
				new StudyEntryPropertyData(null, TermId.ENTRY_TYPE.getId(), null, importedGermplasm.getEntryTypeCategoricalID()));
			dto.getProperties().put(TermId.CROSS.getId(),
					new StudyEntryPropertyData(null, TermId.CROSS.getId(), String.valueOf(importedGermplasm.getCross()), null));
			dto.getProperties().put(TermId.GROUPGID.getId(),
				new StudyEntryPropertyData(null, TermId.GROUPGID.getId(), String.valueOf(importedGermplasm.getGroupId()), null));
			dto.getProperties().put(TermId.ENTRY_CODE.getId(),
				new StudyEntryPropertyData(null, TermId.ENTRY_CODE.getId(), String.valueOf(importedGermplasm.getEntryCode()), null));
			list.add(dto);
		}
		return list;
	}

}
