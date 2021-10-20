
package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.controller.ExportStudyEntriesController;
import com.efficio.fieldbook.web.common.service.ExportStudyEntriesService;
import com.efficio.fieldbook.web.study.germplasm.StudyEntryTransformer;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportRow;
import org.generationcp.commons.pojo.GermplasmListExportInputValues;
import org.generationcp.commons.service.GermplasmExportService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.dao.germplasmlist.GermplasmListDAO;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.interfaces.GermplasmExportSource;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Configurable
public class ExportStudyEntriesServiceImpl implements ExportStudyEntriesService {

	@Resource
	private OntologyService ontologyService;

	@Resource
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private UserSelection userSelection;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	private GermplasmExportService germplasmExportService;

	@Resource
	private StudyEntryService studyEntryService;

	@Resource
	private StudyEntryTransformer studyEntryTransformer;

	public ExportStudyEntriesServiceImpl() {

	}

	@Override
	public void exportAsExcelFile(final int studyId, final String fileNamePath, final Map<String, Boolean> visibleColumns)
		throws GermplasmListExporterException {

		try {

			final List<StudyEntryDto> studyEntryDtoList = this.studyEntryService.getStudyEntries(studyId);
			final List<GermplasmExportSource> germplasmlistData =
				this.studyEntryTransformer.tranformToGermplasmExportSource(studyEntryDtoList);

			final GermplasmListExportInputValues input = new GermplasmListExportInputValues();
			final GermplasmList germplasmList = new GermplasmList();

			input.setFileName(fileNamePath);

			input.setGermplasmList(germplasmList);

			input.setListData(germplasmlistData);

			input.setOwnerName(this.fieldbookMiddlewareService.getOwnerListName(germplasmList.getUserId()));

			final Integer currentLocalIbdbUserId = this.contextUtil.getCurrentWorkbenchUserId();
			input.setCurrentLocalIbdbUserId(currentLocalIbdbUserId);
			input.setExporterName(this.fieldbookMiddlewareService.getOwnerListName(currentLocalIbdbUserId));
			germplasmList.setUserId(currentLocalIbdbUserId);
			input.setVisibleColumnMap(visibleColumns);

			// Get the variables that will be put into the Inventory Section
			input.setInventoryVariableMap(this.extractInventoryVariableMapFromVisibleColumns(visibleColumns));

			// We do not need the inventory variables in visibleColumns anymore so we have to remove them, since variables in Inventory Section will come from
			// GermplasmListExportInputValues.InventoryVariableMap.
			this.removeInventoryVariableMapFromVisibleColumns(visibleColumns);

			input.setColumnTermMap(this.generateColumnStandardVariableMap(visibleColumns));

			this.germplasmExportService.generateGermplasmListExcelFile(input);

		} catch (final MiddlewareQueryException e) {
			throw new GermplasmListExporterException("Error with exporting study germplasm list to XLS File.", e);
		}

	}

	@Override
	public void exportAsCSVFile(final int studyId, final String fileNamePath, final Map<String, Boolean> visibleColumns)
		throws GermplasmListExporterException {

		final List<ExportRow> exportRows = this.getExportColumnValuesFromTable(studyId);
		final List<ExportColumnHeader> exportColumnHeaders = this.getExportColumnHeadersFromTable(visibleColumns);

		try {

			this.germplasmExportService.generateCSVFile(exportRows, exportColumnHeaders, fileNamePath);

		} catch (final IOException e) {
			throw new GermplasmListExporterException("Error with exporting study germplasm list to CSV File.", e);
		}

	}

	/**
	 * Extracts the inventory variables from the visibleColumns map.
	 *
	 * @param visibleColumns
	 * @return
	 */
	Map<Integer, Variable> extractInventoryVariableMapFromVisibleColumns(final Map<String, Boolean> visibleColumns) {

		final Map<Integer, Variable> inventontoryVariableMap = new HashMap<>();

		final Iterator<Map.Entry<String, Boolean>> iterator = visibleColumns.entrySet().iterator();

		while (iterator.hasNext()) {

			final Map.Entry<String, Boolean> entry = iterator.next();
			final String termId = entry.getKey();
			final Boolean isVisible = entry.getValue();
			if (isVisible && isInventoryVariable(termId)) {
				addVariableToMap(inventontoryVariableMap, Integer.valueOf(termId));
			}
		}
		return inventontoryVariableMap;

	}

	/**
	 * Removes inventory variables from the visibleColumns map.
	 *
	 * @param visibleColumns
	 * @return
	 */
	void removeInventoryVariableMapFromVisibleColumns(final Map<String, Boolean> visibleColumns) {

		final Iterator<Map.Entry<String, Boolean>> iterator = visibleColumns.entrySet().iterator();

		while (iterator.hasNext()) {

			final Map.Entry<String, Boolean> entry = iterator.next();
			final String termId = entry.getKey();
			if (isInventoryVariable(termId)) {
				iterator.remove();
			}
		}

	}

	boolean isInventoryVariable(final String termId) {
		return termId.equals(String.valueOf(TermId.SEED_AMOUNT_G.getId()));
	}

	void addVariableToMap(final Map<Integer, Variable> variableMap, final int termId) {

		final Variable variable =
			this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(), termId, false);
		if (variable != null) {
			variableMap.put(variable.getId(), variable);
		}

	}

	private Map<Integer, Term> generateColumnStandardVariableMap(final Map<String, Boolean> visibleColumnMap) {

		final Map<Integer, Term> standardVariableMap = new HashMap<>();

		if (this.userSelection.getPlotsLevelList() != null) {
			for (final SettingDetail settingDetail : this.userSelection.getPlotsLevelList()) {
				final Boolean isVisible = visibleColumnMap.get(settingDetail.getVariable().getCvTermId().toString());
				if (!settingDetail.isHidden() && isVisible != null && isVisible) {
					final Integer variableId = settingDetail.getVariable().getCvTermId();
					final Variable variable =
						this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(), variableId, false);
					standardVariableMap.put(variableId, variable);
				}
			}
		}

		return standardVariableMap;
	}



	protected List<ExportColumnHeader> getExportColumnHeadersFromTable(final Map<String, Boolean> visibleColumns) {

		final List<ExportColumnHeader> exportColumnHeaders = new ArrayList<>();

		final List<SettingDetail> factorsList = this.userSelection.getPlotsLevelList();

		for (final SettingDetail settingDetail : factorsList) {
			final Boolean isExist = visibleColumns.get(settingDetail.getVariable().getCvTermId().toString());
			if (!settingDetail.isHidden() && isExist != null && isExist == Boolean.TRUE) {
				exportColumnHeaders.add(
					new ExportColumnHeader(settingDetail.getVariable().getCvTermId(), settingDetail.getVariable().getName(), true));
			}
		}

		return exportColumnHeaders;
	}

	protected List<ExportRow> getExportColumnValuesFromTable(final int studyId) {

		final List<ExportRow> exportColumnValues = new ArrayList<>();

		// FIXME: Find a way to get the germplasm factors by not using userSelection
		final List<SettingDetail> factorsList = this.userSelection.getPlotsLevelList();
		final List<StudyEntryDto> studyEntries = this.studyEntryService.getStudyEntries(studyId);
		final Map<Integer, String> checkTypesNameMap =
			this.ontologyService.getStandardVariable(TermId.ENTRY_TYPE.getId(), this.contextUtil.getCurrentProgramUUID())
				.getEnumerations().stream().collect(Collectors.toMap(Enumeration::getId, Enumeration::getName));

		for (final StudyEntryDto studyEntryDto : studyEntries) {
			final ExportRow row = new ExportRow();

			for (final SettingDetail settingDetail : factorsList) {
				final Integer termId = settingDetail.getVariable().getCvTermId();
				row.addColumnValue(termId,
					this.getGermplasmPropertyValue(settingDetail.getVariable().getCvTermId().toString(), studyEntryDto,
						checkTypesNameMap));
			}

			exportColumnValues.add(row);
		}

		return exportColumnValues;
	}

	protected String getGermplasmPropertyValue(final String termId, final StudyEntryDto studyEntryDto,
		final Map<Integer, String> checkTypesNameMap) {
		String value = "";
		if (termId != null && NumberUtils.isNumber(termId)) {
			final Integer term = Integer.valueOf(termId);
			if (term.intValue() == TermId.GID.getId()) {
				value = studyEntryDto.getGid().toString();
			} else if (term.intValue() == TermId.ENTRY_CODE.getId()) {
				value = studyEntryDto.getEntryCode();
			} else if (term.intValue() == TermId.ENTRY_NO.getId()) {
				value = studyEntryDto.getEntryNumber().toString();
			} else if (term.intValue() == TermId.SOURCE.getId() || term.intValue() == TermId.GERMPLASM_SOURCE.getId()) {
				value = studyEntryDto.getStudyEntryPropertyValue(TermId.SEED_SOURCE.getId()).orElse("");
			} else if (term.intValue() == TermId.CROSS.getId()) {
				value = studyEntryDto.getStudyEntryPropertyValue(TermId.CROSS.getId()).orElse("");
			} else if (term.intValue() == TermId.DESIG.getId()) {
				value = studyEntryDto.getDesignation();
			} else if (term.intValue() == TermId.ENTRY_TYPE.getId()) {
				// get the code of ENTRY_TYPE - CATEGORICAL FACTOR
				final Optional<String> entryTypeString = studyEntryDto.getStudyEntryPropertyValue(TermId.ENTRY_TYPE.getId());
				final Integer entryTypeCategoricalId = entryTypeString.isPresent()? Integer.valueOf(entryTypeString.get()) : 0;
				value = checkTypesNameMap.getOrDefault(entryTypeCategoricalId, "");
			} else if (term == TermId.GROUPGID.getId()) {
				value = studyEntryDto.getStudyEntryPropertyValue(TermId.GROUPGID.getId()).orElse("");
			}
		}
		return value;
	}


	protected UserSelection getUserSelection() {
		return this.userSelection;
	}

	protected void setUserSelection(final UserSelection userSelection) {
		this.userSelection = userSelection;
	}

	protected FieldbookService getFieldbookMiddlewareService() {
		return this.fieldbookMiddlewareService;
	}

	protected void setFieldbookMiddlewareService(final FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	protected OntologyService getOntologyService() {
		return this.ontologyService;
	}

	protected void setOntologyService(final OntologyService ontologyService) {
		this.ontologyService = ontologyService;
	}

	protected void setExportListTypeFromOriginalGermplasm(final GermplasmList list) throws MiddlewareQueryException {
		if (list != null && list.getListRef() != null) {
			final GermplasmList origList = this.fieldbookMiddlewareService.getGermplasmListById(list.getListRef());

			if (origList != null) {
				if (origList.getStatus() != null && origList.getStatus().intValue() != GermplasmListDAO.STATUS_DELETED.intValue()) {
					list.setType(origList.getType());
				} else {
					list.setType(ExportStudyEntriesController.GERPLASM_TYPE_LST);
				}
			}
		}
	}
}
