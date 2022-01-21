/*
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 * *****************************************************************************
 */

package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.SaveListForm;
import com.efficio.fieldbook.web.common.service.CrossingService;
import com.efficio.fieldbook.web.common.service.impl.CrossingServiceImpl;
import com.efficio.fieldbook.web.naming.service.NamingConventionService;
import com.efficio.fieldbook.web.trial.bean.AdvancingStudy;
import com.efficio.fieldbook.web.trial.form.AdvancingStudyForm;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.parsing.pojo.ImportedCross;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.pojo.AdvancingSource;
import org.generationcp.commons.pojo.AdvancingSourceList;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.commons.pojo.treeview.TreeTableNode;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.ruleengine.RulesNotConfiguredException;
import org.generationcp.commons.service.UserTreeStateService;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.util.TreeViewUtil;
import org.generationcp.commons.workbook.generator.RowColumnType;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Attribute;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.GermplasmStudySourceType;
import org.generationcp.middleware.pojos.ListMetadata;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceInput;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The Class GermplasmTreeController.
 */
@Controller
@RequestMapping(value = "/ListTreeManager")
@Transactional
public class GermplasmTreeController extends AbstractBaseFieldbookController {

	/**
	 * The default folder open state stored when closing the germplasm lists
	 * browser.
	 */
	static final String DEFAULT_STATE_SAVED_FOR_GERMPLASM_LIST = "Lists";

	private static final String COMMON_SAVE_GERMPLASM_LIST = "Common/saveGermplasmList";

	private static final String GERMPLASM_LIST_TYPES = "germplasmListTypes";
	public static final Integer LOCKED_LIST_STATUS = 101;
	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(GermplasmTreeController.class);

	private static final String GERMPLASM_LIST_TABLE_PAGE = "Common/includes/list/listTable";
	public static final String GERMPLASM_LIST_ROOT_NODES = "listRootNodes";
	private static final String GERMPLASM_LIST_TABLE_ROWS_PAGE = "Common/includes/list/listTableRows";
	public static final String GERMPLASM_LIST_CHILD_NODES = "listChildNodes";
	protected static final String PROGRAM_LISTS = "LISTS";
	protected static final String CROP_LISTS = "CROPLISTS";

	public static final String GERMPLASM_LIST_TYPE_ADVANCE = "advance";
	public static final String GERMPLASM_LIST_TYPE_CROSS = "cross";
	public static final String NODE_NONE = "None";
	/**
	 * The Constant BATCH_SIZE.
	 */
	public static final int BATCH_SIZE = 500;
	public static final String DATE_TIME_FORMAT = "yyyyMMdd";

	/**
	 * The germplasm list manager.
	 */
	@Resource
	private GermplasmListManager germplasmListManager;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private CrossingService crossingService;

	@Resource
	private NamingConventionService namingConventionService;

	@Resource
	private GermplasmStudySourceService germplasmStudySourceService;

	@Resource
	private DatasetService datasetService;

	@Resource
	private GermplasmService germplasmService;

	static final String NAME_NOT_UNIQUE = "Name not unique";
	private static final String HAS_CHILDREN = "Folder has children";
	private static final String FOLDER = "FOLDER";

	static final String IS_SUCCESS = "isSuccess";

	private static final String MESSAGE = "message";

	static final String DATE_FORMAT = DATE_TIME_FORMAT;

	@Resource
	private ResourceBundleMessageSource messageSource;

	@Resource
	private UserSelection userSelection;

	@Resource
	private UserTreeStateService userTreeStateService;

	@Resource
	private GermplasmDataManager germplasmDataManager;

	@Resource
	private OntologyDataManager ontologyDataManager;

	/**
	 * Load initial germplasm tree.
	 *
	 * @return the string
	 */
	@RequestMapping(value = "/saveList/{listIdentifier}", method = RequestMethod.GET)
	public String saveList(@ModelAttribute("saveListForm") final SaveListForm form, @PathVariable final String listIdentifier,
		final Model model) {

		try {
			form.setListDate(DateUtil.getCurrentDateInUIFormat());
			form.setListIdentifier(listIdentifier);
			final String listOwner = this.fieldbookMiddlewareService.getOwnerListName(this.contextUtil.getCurrentWorkbenchUserId());
			form.setListOwner(listOwner);
			final List<UserDefinedField> germplasmListTypes = this.germplasmListManager.getGermplasmListTypes();
			form.setListType(AppConstants.GERMPLASM_LIST_TYPE_HARVEST.getString());
			model.addAttribute(GermplasmTreeController.GERMPLASM_LIST_TYPES, germplasmListTypes);

		} catch (final Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
		}

		return super.showAjaxPage(model, GermplasmTreeController.COMMON_SAVE_GERMPLASM_LIST);
	}

	protected class GermplasmListResult {

		private Integer germplasmListId;
		private Boolean isTrimed;
		private Boolean isNamesChanged;

		public GermplasmListResult() {
			// do nothing
		}

		public Boolean getIsTrimed() {
			return this.isTrimed;
		}

		public Integer getGermplasmListId() {
			return this.germplasmListId;
		}

		public GermplasmListResult withIsTrimmed(final Boolean isTrimmed) {
			this.isTrimed = isTrimmed;
			return this;
		}

		public GermplasmListResult withGermplasmListId(final Integer germplasmListId) {
			this.germplasmListId = germplasmListId;
			return this;
		}

		public GermplasmListResult withNamesChanged(final Boolean isNamesChanged) {
			this.isNamesChanged = isNamesChanged;
			return this;
		}

		public Boolean getIsNamesChanged() {
			return this.isNamesChanged;
		}

	}

	/**
	 * Load initial germplasm tree.
	 *
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/saveList", method = RequestMethod.POST)
	public Map<String, Object> savePost(@ModelAttribute("saveListForm") final SaveListForm form, final Model model) {
		final Map<String, Object> results = new HashMap<>();

		try {
			final String trimmedListName = form.getListName().trim();
			final GermplasmList germplasmListIsNew =
				this.fieldbookMiddlewareService.getGermplasmListByName(trimmedListName, this.getCurrentProgramUUID());
			final List<GermplasmStudySourceInput> germplasmStudySourceList = new ArrayList<>();
			if (germplasmListIsNew == null && !this.isSimilarToRootFolderName(trimmedListName)) {
				final GermplasmListResult result = this.saveGermplasmList(form, germplasmStudySourceList);
				final Integer germplasmListId = result.getGermplasmListId();
				this.germplasmStudySourceService.saveGermplasmStudySources(germplasmStudySourceList);
				results.put(GermplasmTreeController.IS_SUCCESS, 1);
				results.put("germplasmListId", germplasmListId);
				results.put("uniqueId", form.getListIdentifier().isEmpty() ? "0" : form.getListIdentifier());
				results.put("listName", form.getListName());
				results.put("isTrimed", result.getIsTrimed() ? 1 : 0);
				results.put("isNamesChanged", result.getIsNamesChanged() ? 1 : 0);

			} else {
				results.put(GermplasmTreeController.IS_SUCCESS, 0);
				results.put(GermplasmTreeController.MESSAGE,
					this.messageSource.getMessage("germplasm.save.list.name.unique.error", null, LocaleContextHolder.getLocale()));
			}
		} catch (final RulesNotConfiguredException rnce) {
			GermplasmTreeController.LOG.error(rnce.getMessage(), rnce);
			results.put(GermplasmTreeController.IS_SUCCESS, 0);
			results.put(GermplasmTreeController.MESSAGE, rnce.getMessage());
		} catch (final RuleException re) {
			GermplasmTreeController.LOG.error(re.getMessage(), re);
			results.put(GermplasmTreeController.IS_SUCCESS, 0);
			results.put(GermplasmTreeController.MESSAGE,
				this.messageSource.getMessage("germplasm.naming.failed", null, LocaleContextHolder.getLocale()));
		} catch (final Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
			results.put(GermplasmTreeController.IS_SUCCESS, 0);
			results.put(GermplasmTreeController.MESSAGE, e.getMessage());
		}

		return results;
	}

	protected GermplasmListResult saveGermplasmList(final SaveListForm form, final List<GermplasmStudySourceInput> germplasmStudySourceList)
		throws RuleException {

		final List<Pair<Germplasm, GermplasmListData>> listDataItems = new ArrayList<>();
		final Integer currentUserId = this.getCurrentIbdbUserId();
		final GermplasmList germplasmList = this.createGermplasmList(form, currentUserId);

		if (GermplasmTreeController.GERMPLASM_LIST_TYPE_ADVANCE.equals(form.getGermplasmListType())) {
			final AdvancingStudyForm advancingStudyForm = this.getPaginationListSelection().getAdvanceDetails(form.getListIdentifier());
			final List<Pair<Germplasm, List<Name>>> germplasms = new ArrayList<>();
			final List<Pair<Germplasm, List<Attribute>>> germplasmAttributes = new ArrayList<>();

			final boolean isNamesChanged =
				this.populateGermplasmListDataFromAdvanced(germplasmList, advancingStudyForm, germplasms, listDataItems,
					germplasmAttributes);
			final Integer germplasmListId = this.fieldbookMiddlewareService
				.saveNurseryAdvanceGermplasmList(germplasms, listDataItems, germplasmList, germplasmAttributes,
					this.contextUtil.getProjectInContext().getCropType());
			this.createGermplasmStudySourcesFromSavedAdvanceListEntries(advancingStudyForm.getGermplasmList(), listDataItems,
				germplasmStudySourceList);

			return new GermplasmListResult().withGermplasmListId(germplasmListId).withIsTrimmed(false).withNamesChanged(isNamesChanged);

		} else if (GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS.equals(form.getGermplasmListType())) {
			final CrossSetting crossSetting = this.userSelection.getCrossSettings();
			final ImportedCrossesList importedCrossesList = this.userSelection.getImportedCrossesList();

			final Boolean isTrimmed =
				this.applyNamingSettingToCrosses(listDataItems, germplasmList, crossSetting, importedCrossesList, germplasmStudySourceList,
					form.getOmitAlertedCrosses());

			if (listDataItems.isEmpty()) {
				throw new MiddlewareException("No crosses to save.");
			} else {
				// Set imported user as owner of the list
				germplasmList.setUserId(importedCrossesList.getUserId());

				final Integer germplasmListId = this.fieldbookMiddlewareService
					.saveGermplasmList(this.contextUtil.getProjectInContext().getCropType().getCropName(), listDataItems, germplasmList,
						crossSetting.isApplyNewGroupToPreviousCrosses());

				return new GermplasmListResult().withGermplasmListId(germplasmListId).withIsTrimmed(isTrimmed).withNamesChanged(false);
			}

		} else {
			throw new IllegalArgumentException("Unknown germplasm list type supplied when saving germplasm list");
		}

	}

	void createGermplasmStudySourcesFromSavedAdvanceListEntries(final List<ImportedGermplasm> listEntries,
		final List<Pair<Germplasm, GermplasmListData>> savedListDataItems, final List<GermplasmStudySourceInput> germplasmStudySourceList) {
		final Integer studyId = this.userSelection.getWorkbook().getStudyDetails().getId();
		final ListIterator<Pair<Germplasm, GermplasmListData>> listDataIterator = savedListDataItems.listIterator();
		final ListIterator<ImportedGermplasm> listEntriesIterator = listEntries.listIterator();
		final Set<Integer> sourcePlotNumbers =
			listEntries.stream().map(g -> Integer.valueOf(g.getPlotNumber())).collect(Collectors.toSet());
		final Set<Integer> trialInstances =
			listEntries.stream().map(g -> Integer.valueOf(g.getTrialInstanceNumber())).collect(Collectors.toSet());
		final Table<Integer, Integer, Integer> observationUnitIdsTable =
			this.datasetService.getTrialNumberPlotNumberObservationUnitIdTable(this.userSelection.getWorkbook().getMeasurementDatesetId(),
				trialInstances, sourcePlotNumbers);
		while (listEntriesIterator.hasNext()) {
			final ImportedGermplasm advanceEntry = listEntriesIterator.next();
			final GermplasmListData listData = listDataIterator.next().getRight();
			final Integer plotNumber = Integer.valueOf(advanceEntry.getPlotNumber());
			final Integer trialInstance = Integer.valueOf(advanceEntry.getTrialInstanceNumber());
			germplasmStudySourceList.add(
				new GermplasmStudySourceInput(listData.getGid(), studyId, observationUnitIdsTable.get(trialInstance, plotNumber),
					GermplasmStudySourceType.ADVANCE));
		}

	}

	private void checkForEmptyDesigNames(final List<ImportedCross> importedCrosses) throws RulesNotConfiguredException {
		boolean valid = true;
		for (final ImportedCross importedCross : importedCrosses) {
			if (StringUtils.isEmpty(importedCross.getDesig())) {
				valid = false;
			}
		}
		if (!valid) {
			throw new RulesNotConfiguredException(this.messageSource
				.getMessage("error.save.cross.rules.not.configured", null, "The rules" + " were not configured",
					LocaleContextHolder.getLocale()));
		}
	}

	/**
	 * Apply the naming setting to the crosses depending whether manual setting or rules based on the breeding method were selected
	 * Save the germplasm, names and attributes. Build the list items and germplasm_study_source records to be saved
	 *
	 * @param listDataItems
	 * @param germplasmList
	 * @param crossSetting
	 * @param importedCrossesList
	 * @return
	 * @throws RuleException
	 */
	private Boolean applyNamingSettingToCrosses(final List<Pair<Germplasm, GermplasmListData>> listDataItems,
		final GermplasmList germplasmList, final CrossSetting crossSetting, final ImportedCrossesList importedCrossesList,
		final List<GermplasmStudySourceInput> germplasmStudySourceList, final boolean omitAlertedCrosses)
		throws RuleException {

		boolean isTrimed;
		if (crossSetting.isUseManualSettingsForNaming()) {
			// this line of code is where the creation of new germplasm takes place
			isTrimed = this.crossingService
				.applyCrossSetting(crossSetting, importedCrossesList, this.userSelection.getWorkbook());
			isTrimed = isTrimed || this.createGermplasmListDataAndGermplasmStudySource(germplasmList, listDataItems,
				importedCrossesList.getImportedCrosses(), germplasmStudySourceList, omitAlertedCrosses);
		} else {
			final ImportedCrossesList importedCrossesListWithNamingSettings = this.applyNamingRules(importedCrossesList);
			// this line of code is where the creation of new germplasm takes place
			isTrimed = this.crossingService
				.applyCrossSettingWithNamingRules(crossSetting, importedCrossesListWithNamingSettings, this.getCurrentIbdbUserId(),
					this.userSelection.getWorkbook());
			isTrimed = isTrimed || this
				.createGermplasmListDataAndGermplasmStudySource(germplasmList, listDataItems,
					importedCrossesListWithNamingSettings.getImportedCrosses(), germplasmStudySourceList, omitAlertedCrosses);
		}
		this.checkForEmptyDesigNames(importedCrossesList.getImportedCrosses());
		return isTrimed;
	}

	protected ImportedCrossesList applyNamingRules(final ImportedCrossesList importedCrossesList)
		throws RuleException {

		final List<AdvancingSource> advancingSources = new ArrayList<>();
		final List<Integer> gids = new ArrayList<>();
		final List<ImportedCross> importedCrosses = importedCrossesList.getImportedCrosses();

		for (final ImportedCross cross : importedCrosses) {

			this.assignCrossNames(cross);
			advancingSources.add(this.createAdvancingSource(cross));
			if (cross.getGid() != null && NumberUtils.isNumber(cross.getGid())) {
				gids.add(Integer.valueOf(cross.getGid()));
			}
		}

		final AdvancingSourceList advancingSourceList = new AdvancingSourceList();
		advancingSourceList.setRows(advancingSources);

		final AdvancingStudy advancingParameters = new AdvancingStudy();
		advancingParameters.setCheckAdvanceLinesUnique(true);

		final List<ImportedCross> crosses = this.namingConventionService
			.generateCrossesList(importedCrosses, advancingSourceList, advancingParameters, this.userSelection.getWorkbook(), gids);

		importedCrossesList.setImportedGermplasms(crosses);
		this.userSelection.setImportedCrossesList(importedCrossesList);

		return importedCrossesList;
	}

	protected void assignCrossNames(final ImportedCross cross) {
		final Name name = new Name();
		name.setNstat(1);
		name.setNval(cross.getCross());
		final List<Name> names = new ArrayList<>();
		names.add(name);
		cross.setNames(names);
	}

	protected AdvancingSource createAdvancingSource(final ImportedCross cross) {
		final AdvancingSource advancingSource = new AdvancingSource(cross);
		// TODO add trial instance number
		final Workbook workbook = this.userSelection.getWorkbook();
		advancingSource.setStudyId(workbook.getStudyDetails().getId());
		advancingSource.setEnvironmentDatasetId(workbook.getTrialDatasetId());
		advancingSource.setConditions(workbook.getConditions());
		advancingSource.setStudyType(workbook.getStudyDetails().getStudyType());
		advancingSource.setBreedingMethodId(cross.getBreedingMethodId());
		return advancingSource;
	}

	/**
	 * Load initial germplasm tree for crosses.
	 *
	 * @return the string
	 */
	@RequestMapping(value = "/saveCrossesList", method = RequestMethod.GET)
	public String saveList(@ModelAttribute("saveListForm") final SaveListForm form, final Model model) {

		try {
			String listName = "";
			String listDescription = "";
			String listType = AppConstants.GERMPLASM_LIST_TYPE_GENERIC_LIST.getString();
			String listDate = DateUtil.getCurrentDateInUIFormat();
			String listOwner = this.fieldbookMiddlewareService.getOwnerListName(this.contextUtil.getCurrentWorkbenchUserId());
			if (this.userSelection.getImportedCrossesList() != null) {
				listName = this.userSelection.getImportedCrossesList().getName();
				listDescription = this.userSelection.getImportedCrossesList().getTitle();
				listType = this.userSelection.getImportedCrossesList().getType();
				listDate = DateUtil.getDateInUIFormat(this.userSelection.getImportedCrossesList().getDate());
				listOwner = this.fieldbookMiddlewareService.getOwnerListName(this.userSelection.getImportedCrossesList().getUserId());
			}

			form.setListName(listName);
			form.setListDescription(listDescription);
			form.setListType(listType);
			form.setListDate(listDate);
			form.setListOwner(listOwner);

			final List<UserDefinedField> germplasmListTypes = this.germplasmListManager.getGermplasmListTypes();
			model.addAttribute(GermplasmTreeController.GERMPLASM_LIST_TYPES, germplasmListTypes);

		} catch (final Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
			throw e;
		}

		return super.showAjaxPage(model, GermplasmTreeController.COMMON_SAVE_GERMPLASM_LIST);
	}

	GermplasmList createGermplasmList(final SaveListForm saveListForm, final Integer currentUserId) {

		// Create germplasm list
		final String listName = saveListForm.getListName().trim();
		final String listType = saveListForm.getListType();

		final String description = saveListForm.getListDescription();
		GermplasmList parent = null;
		Integer parentId = null;
		GermplasmList gpList = null;

		if (saveListForm.getParentId() != null && !GermplasmTreeController.PROGRAM_LISTS.equals(saveListForm.getParentId())
			&& !GermplasmTreeController.CROP_LISTS.equals(saveListForm.getParentId())) {
			parentId = Integer.valueOf(saveListForm.getParentId());
			gpList = this.germplasmListManager.getGermplasmListById(parentId);
		}

		if (gpList != null && gpList.isFolder()) {
			parent = gpList;
		}

		final Integer status = 1;
		final Long dateLong = Long.valueOf(DateUtil.convertToDBDateFormat(TermId.DATE_VARIABLE.getId(), saveListForm.getListDate()));

		final GermplasmList germplasmList =
			new GermplasmList(null, listName, dateLong, listType, currentUserId, description, parent, status,
				saveListForm.getListNotes(), null);

		// If the germplasm list is saved in 'Crop lists' folder, the programUUID should be null
		// so that the germplasm list will be accessible to all programs of the same crop.
		if (GermplasmTreeController.CROP_LISTS.equals(saveListForm.getParentId())) {
			// list should be locked by default if it is saved in 'Crop lists' folder.
			germplasmList.setStatus(LOCKED_LIST_STATUS);
		} else {
			germplasmList.setProgramUUID(this.getCurrentProgramUUID());
		}

		return germplasmList;

	}

	private boolean createGermplasmListDataAndGermplasmStudySource(final GermplasmList germplasmList,
		final List<Pair<Germplasm, GermplasmListData>> listDataItems, final List<ImportedCross> importedGermplasmList,
		final List<GermplasmStudySourceInput> germplasmStudySourceList,
		final boolean omitAlertedCrosses) {

		boolean isTrimed = false;
		final Integer studyId = this.userSelection.getWorkbook().getStudyDetails().getId();
		// Take the plot of the female parent as the source observation unit. It is possible for female plot #s to be null if source is from another study
		final Set<Integer> sourcePlotNumbers =
			importedGermplasmList.stream().filter(g -> Objects.nonNull(g.getFemalePlotNo())).map(ImportedCross::getFemalePlotNo)
				.collect(Collectors.toSet());
		Table<Integer, Integer, Integer> observationUnitIdsTable = HashBasedTable.create();
		if (!CollectionUtils.isEmpty(sourcePlotNumbers)) {
			// Take the observation units from the first trial instance
			observationUnitIdsTable = this.datasetService.getTrialNumberPlotNumberObservationUnitIdTable(
				this.userSelection.getWorkbook().getMeasurementDatesetId(),
				Collections.singleton(1), sourcePlotNumbers);
		}

		Integer entryNumber = 0;
		for (final ImportedCross importedCross : importedGermplasmList) {

			// Skip cross that is already existing in the database
			final Optional<Integer> optionalGid =
				importedCross.getGid() == null ? Optional.empty() : Optional.of(Integer.valueOf(importedCross.getGid()));
			if (omitAlertedCrosses && this.germplasmDataManager.hasExistingCrosses(Integer.valueOf(importedCross.getFemaleGid()),
				importedCross.getMaleGids(),
				optionalGid)) {
				continue;
			}
			entryNumber++;
			final Integer gid = importedCross.getGid() != null ? Integer.valueOf(importedCross.getGid()) : null;

			final Germplasm germplasm = new Germplasm();
			germplasm.setGid(gid);
			germplasm.setMethodId(importedCross.getBreedingMethodId());

			// Create list data items to save - Map<Germplasm,
			// GermplasmListData>
			final String seedSource = importedCross.getSource();
			final String designation = importedCross.getDesig();
			final String notes = importedCross.getNotes();
			final Integer crossingDate = importedCross.getCrossingDate();
			String groupName = importedCross.getCross();

			// Common germplasm list data fields
			final Integer listDataId = importedCross.getId();
			// null will be set for new records
			final Integer listDataStatus = 0;
			final Integer localRecordId = 0;

			if (groupName == null) {
				// Default value if null
				groupName = "-";
			}

			if (groupName.length() > CrossingServiceImpl.MAX_CROSS_NAME_SIZE) {
				groupName = groupName.substring(0, CrossingServiceImpl.MAX_CROSS_NAME_SIZE - 1);
				groupName = groupName + CrossingServiceImpl.TRUNCATED;
				isTrimed = true;
			}

			final GermplasmListData listData =
				this.createGermplasmListData(germplasmList, gid, entryNumber, String.valueOf(entryNumber), seedSource, designation, notes,
					crossingDate,
					groupName, listDataId, listDataStatus, localRecordId);

			listDataItems.add(new ImmutablePair<>(germplasm, listData));
			// Observation Units, by female plot number, are from the first trial instance
			// It's possible for female plot numbers to be null if female parent was from another study
			final Integer femalePlotNo = importedCross.getFemalePlotNo();
			final Integer sourcePlotNo = femalePlotNo != null ? observationUnitIdsTable.get(1, femalePlotNo) : null;
			germplasmStudySourceList.add(new GermplasmStudySourceInput(gid, studyId, sourcePlotNo, GermplasmStudySourceType.CROSS));
		}
		return isTrimed;
	}

	private GermplasmListData createGermplasmListData(final GermplasmList germplasmList, final Integer gid, final Integer entryId,
		final String entryCode, final String seedSource, final String designation, final String notes, final Integer crossingDate,
		final String groupName, final Integer listDataId, final Integer listDataStatus, final Integer localRecordId) {
		return new GermplasmListData(listDataId, germplasmList, gid, entryId, entryCode, seedSource, designation, groupName, listDataStatus,
			localRecordId, notes, crossingDate);
	}

	/**
	 * Creates the nursery advance germplasm list.
	 *
	 * @param form          the form
	 * @param germplasms    the germplasms
	 * @param listDataItems the list data items
	 * @return true if any of the final names generated have changed from the previewed ones
	 */

	Boolean populateGermplasmListDataFromAdvanced(final GermplasmList germplasmList, final AdvancingStudyForm form,
		final List<Pair<Germplasm, List<Name>>> germplasms, final List<Pair<Germplasm, GermplasmListData>> listDataItems,
		final List<Pair<Germplasm, List<Attribute>>> germplasmAttributes) throws RuleException {
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
		final String harvestDate = LocalDate.now().format(formatter);

		// Common germplasm fields
		final Integer lgid = 0;
		final Integer gDate = DateUtil.getCurrentDateAsIntegerValue();

		// Common germplasm list data fields
		final Integer listDataId = null;

		// Common name fields
		final Integer nRef = 0;

		final Integer plotCodeVariableId = this.germplasmService.getPlotCodeField().getId();
		final Integer plotNumberVariableId = this.getVariableId("PLOT_NUMBER_AP_text");
		final Integer trialInstanceVariableId = this.getVariableId("INSTANCE_NUMBER_AP_text");
		final Integer repNumberVariableId = this.getVariableId("REP_NUMBER_AP_text");
		final Integer plantNumberVariableId = this.getVariableId("PLANT_NUMBER_AP_text");

		final List<ImportedGermplasm> advanceItems = form.getGermplasmList();
		// Save the previewed names, then recompute names (the sequences might have moved from the time of preview)
		final List<String> previewedNamesList = advanceItems.stream().map(ImportedGermplasm::getDesig).collect(Collectors.toList());
		form.getAdvancingSourceItems().forEach(item -> item.setDesignationIsPreviewOnly(false));
		this.namingConventionService.generateAdvanceListNames(form.getAdvancingSourceItems(), false, advanceItems);

		// Create germplasms to save - Map<Germplasm, List<Name>>
		for (final ImportedGermplasm importedGermplasm : advanceItems) {
			Integer gid = null;

			if (importedGermplasm.getGid() != null) {
				gid = Integer.valueOf(importedGermplasm.getGid());
			}

			Integer locationId = 0;
			// old manage nursery used to have an input to specify harvest location
			// we are keeping this in case that functionality is added again
			if (!StringUtils.isBlank(form.getHarvestLocationId())) {
				locationId = Integer.valueOf(form.getHarvestLocationId());
			}
			if (locationId == 0 && importedGermplasm.getLocationId() != null) {
				locationId = importedGermplasm.getLocationId();
			}

			final List<Name> names = importedGermplasm.getNames();
			Name preferredName = names.get(0);

			for (final Name name : names) {

				name.setLocationId(locationId);
				name.setNdate(gDate);
				name.setReferenceId(nRef);

				// If crop == CIMMYT WHEAT (crop with more than one name saved)
				// Germplasm name is the Names entry with NType = 1027, NVal =
				// table.desig, NStat = 0
				if (name.getNstat() == 0 && name.getTypeId() == GermplasmNameType.UNRESOLVED_NAME.getUserDefinedFieldID()) {
					preferredName = name;
				}
			}

			final Integer trueGdate = !"".equals(harvestDate.trim()) ? Integer.valueOf(harvestDate) : gDate;
			final Germplasm germplasm;
			germplasm =
				new Germplasm(gid, importedGermplasm.getBreedingMethodId(), importedGermplasm.getGnpgs(), importedGermplasm.getGpid1(),
					importedGermplasm.getGpid2(), lgid, locationId, trueGdate, preferredName);
			final Integer mgid = importedGermplasm.getMgid() == null ? 0 : importedGermplasm.getMgid();
			germplasm.setMgid(mgid);
			germplasms.add(new ImmutablePair<>(germplasm, names));

			// Create list data items to save - Map<Germplasm, GermplasmListData>
			final String groupName = importedGermplasm.getCross() != null ? importedGermplasm.getCross() : "-";

			final GermplasmListData listData =
				new GermplasmListData(listDataId, germplasmList, gid, importedGermplasm.getEntryNumber(), importedGermplasm.getEntryCode(),
					importedGermplasm.getSource(), importedGermplasm.getDesig(), groupName, 0, 0);

			listDataItems.add(new ImmutablePair<>(germplasm, listData));

			final List<Attribute> attributesPerGermplasm = Lists.newArrayList();
			// Add the seed source/origin attribute (which is generated based on
			// format strings configured in crossing.properties) to the
			// originAttribute gid will be set when saving once gid is known
			final Attribute originAttribute =
				this.createAttributeObject(importedGermplasm.getSource(), plotCodeVariableId, locationId, gDate);
			attributesPerGermplasm.add(originAttribute);

			final String plotNumberString = importedGermplasm.getPlotNumber();
			final Attribute plotNumberAttribute =
				this.createAttributeObject(plotNumberString, plotNumberVariableId, locationId, gDate);
			attributesPerGermplasm.add(plotNumberAttribute);

			// Adding Instance number and replication number as
			// attributes of germplasm for trial advancing
			final String replicationNumber = importedGermplasm.getReplicationNumber();
			if (StringUtils.isNotBlank(replicationNumber)) {
				final Attribute repNoAttribute = this.createAttributeObject(replicationNumber, repNumberVariableId, locationId, gDate);
				attributesPerGermplasm.add(repNoAttribute);
			}

			final Attribute instanceNoAttribute =
				this.createAttributeObject(importedGermplasm.getTrialInstanceNumber(), trialInstanceVariableId, locationId,
					gDate);
			attributesPerGermplasm.add(instanceNoAttribute);

			if (importedGermplasm.getPlantNumber() != null) {
				final Attribute plantNoAttribute =
					this.createAttributeObject(importedGermplasm.getPlantNumber(), plantNumberVariableId, locationId, gDate);
				attributesPerGermplasm.add(plantNoAttribute);
			}

			germplasmAttributes.add(new ImmutablePair<>(germplasm, Lists.newArrayList(attributesPerGermplasm)));
		}

		// Check if any of the names have changed from the previewed names
		final List<String> finalNamesList = advanceItems.stream().map(ImportedGermplasm::getDesig).collect(Collectors.toList());
		return !previewedNamesList.equals(finalNamesList);
	}

	private Integer getVariableId(final String name) {
		//FIXME Handle NPE, as of now we are hardcoding that the required variables cant be edited, so it should never get NPE
		return this.ontologyDataManager.findTermByName(name, CvId.VARIABLES.getId()).getId();
	}

	private Attribute createAttributeObject(final String attributeValue, final Integer typeId,
		final Integer locationId, final Integer gDate) {
		final Attribute originAttribute = new Attribute();
		originAttribute.setAval(attributeValue);
		originAttribute.setTypeId(typeId);
		originAttribute.setAdate(gDate);
		originAttribute.setLocationId(locationId);
		return originAttribute;
	}

	/**
	 * Load initial germplasm tree.
	 *
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/loadInitGermplasmTree/{isFolderOnly}", method = RequestMethod.GET)
	public String loadInitialGermplasmTree(@PathVariable final String isFolderOnly) {
		final List<TreeNode> rootNodes = new ArrayList<>();
		rootNodes.add(new TreeNode(GermplasmTreeController.CROP_LISTS, AppConstants.CROP_LISTS.getString(), true, "lead",
			AppConstants.FOLDER_ICON_PNG.getString(), null));
		rootNodes.add(new TreeNode(GermplasmTreeController.PROGRAM_LISTS, AppConstants.PROGRAM_LISTS.getString(), true, "lead",
			AppConstants.FOLDER_ICON_PNG.getString(), this.getCurrentProgramUUID()));
		return TreeViewUtil.convertTreeViewToJson(rootNodes);

	}

	@ResponseBody
	@RequestMapping(value = "/getPreferredName/{gid}", method = RequestMethod.GET)
	public String getPreferredName(@PathVariable final String gid) {
		return this.germplasmDataManager.getPreferredNameValueByGID(Integer.valueOf(gid));
	}

	/**
	 * Load initial germplasm tree table.
	 *
	 * @return the string
	 */
	@RequestMapping(value = "/loadInitTreeTable", method = RequestMethod.GET)
	public String loadInitialGermplasmTreeTable(final Model model) {
		final List<TreeTableNode> rootNodes = new ArrayList<>();
		final TreeTableNode programListsNode =
			new TreeTableNode(GermplasmTreeController.PROGRAM_LISTS, AppConstants.PROGRAM_LISTS.getString(), null, null, null, null,
				"1");
		rootNodes.add(programListsNode);
		model.addAttribute(GermplasmTreeController.GERMPLASM_LIST_ROOT_NODES, rootNodes);
		return super.showAjaxPage(model, GermplasmTreeController.GERMPLASM_LIST_TABLE_PAGE);
	}

	protected List<GermplasmList> getGermplasmListChildren(final String id, final String programUUID) {
		List<GermplasmList> children = new ArrayList<>();
		if (GermplasmTreeController.PROGRAM_LISTS.equals(id)) {
			children = this.germplasmListManager.getAllTopLevelLists(programUUID);
		} else if (GermplasmTreeController.CROP_LISTS.equals(id)) {
			children = this.germplasmListManager.getAllTopLevelLists(null);
		} else if (NumberUtils.isNumber(id)) {
			final int parentId = Integer.parseInt(id);
			children = this.germplasmListManager
				.getGermplasmListByParentFolderIdBatched(parentId, programUUID, GermplasmTreeController.BATCH_SIZE);
		} else {
			GermplasmTreeController.LOG.error("germplasm id = " + id + " is not a number");
		}
		return children;
	}

	protected List<TreeTableNode> getGermplasmListFolderChildNodes(final TreeTableNode node, final String programUUID) {
		final List<TreeTableNode> childNodes = this.getGermplasmListFolderChildNodes(node.getId(), programUUID);
		if (childNodes != null) {
			node.setNumOfChildren(Integer.toString(childNodes.size()));
		} else {
			node.setNumOfChildren("0");
		}
		return childNodes;
	}

	protected List<TreeTableNode> getGermplasmListFolderChildNodes(final String id, final String programUUID) {
		List<TreeTableNode> childNodes = new ArrayList<>();
		if (id != null && !"".equals(id)) {
			childNodes = this.getGermplasmFolderChildrenNode(id, programUUID);
		}
		return childNodes;
	}

	private List<TreeNode> getGermplasmChildNodes(final String parentKey, final boolean isFolderOnly, final String programUUID) {
		if (!(parentKey != null && !"".equals(parentKey))) {
			return new ArrayList<>();
		}

		final List<GermplasmList> rootLists;
		if (GermplasmTreeController.PROGRAM_LISTS.equals(parentKey)) {
			rootLists = this.germplasmListManager.getAllTopLevelLists(programUUID);
		} else if (GermplasmTreeController.CROP_LISTS.equals(parentKey)) {
			rootLists = this.germplasmListManager.getAllTopLevelLists(null);
		} else if (NumberUtils.isNumber(parentKey)) {
			rootLists = this.getGermplasmChildrenNode(parentKey, programUUID);
		} else {
			throw new IllegalStateException("Add a message");
		}

		final List<UserDefinedField> listTypes = this.germplasmDataManager
			.getUserDefinedFieldByFieldTableNameAndType(RowColumnType.LIST_TYPE.getFtable(), RowColumnType.LIST_TYPE.getFtype());

		final List<TreeNode> childNodes = TreeViewUtil.convertGermplasmListToTreeView(rootLists, isFolderOnly, listTypes);

		final Map<Integer, ListMetadata> allListMetaData = this.germplasmListManager.getGermplasmListMetadata(rootLists);

		for (final TreeNode newNode : childNodes) {
			final ListMetadata nodeMetaData = allListMetaData.get(Integer.parseInt(newNode.getKey()));
			if (nodeMetaData != null && nodeMetaData.getNumberOfChildren() > 0) {
				newNode.setIsLazy(true);
			}
		}
		return childNodes;
	}

	private List<GermplasmList> getGermplasmChildrenNode(final String parentKey, final String programUUID) {
		final int parentId = Integer.parseInt(parentKey);
		return this.germplasmListManager.getGermplasmListByParentFolderIdBatched(parentId, programUUID, GermplasmTreeController.BATCH_SIZE);
	}

	private List<TreeTableNode> getGermplasmFolderChildrenNode(final String id, final String programUUID) {
		return TreeViewUtil
			.convertGermplasmListToTreeTableNodes(id, this.getGermplasmListChildren(id, programUUID), this.germplasmListManager,
				this.germplasmDataManager);
	}

	/**
	 * Load initial germplasm tree.
	 *
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/germplasm/list/header/details/{listId}", method = RequestMethod.GET)
	public Map<String, Object> getGermplasmListHeaderDetails(@PathVariable final int listId) {
		final Map<String, Object> dataResults = new HashMap<>();
		try {
			final GermplasmList germplasmList = this.fieldbookMiddlewareService.getGermplasmListById(listId);
			dataResults.put("name", germplasmList.getName());
			dataResults.put("description", germplasmList.getDescription());
			final Integer listRef = germplasmList.getListRef();
			if (listRef != null) {
				final GermplasmList parentGermplasmList = this.fieldbookMiddlewareService.getGermplasmListById(listRef);
				dataResults.put("type", this.getTypeString(parentGermplasmList.getType()));
			} else {
				dataResults.put("type", this.getTypeString(germplasmList.getType()));
			}

			String statusValue = "Unlocked List";
			if (germplasmList.getStatus() >= 100) {
				statusValue = "Locked List";
			}

			dataResults.put("status", statusValue);
			dataResults.put("date", germplasmList.getDate());
			dataResults.put("owner", this.fieldbookMiddlewareService.getOwnerListName(germplasmList.getUserId()));
			dataResults.put("notes", germplasmList.getNotes());
			dataResults.put("totalEntries", this.fieldbookMiddlewareService.countGermplasmListDataByListId(listId));

		} catch (final Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
		}

		return dataResults;
	}

	private String getTypeString(final String typeCode) {
		try {
			final List<UserDefinedField> listTypes = this.germplasmListManager.getGermplasmListTypes();

			for (final UserDefinedField listType : listTypes) {
				if (typeCode.equals(listType.getFcode())) {
					return listType.getFname();
				}
			}
		} catch (final MiddlewareQueryException ex) {
			GermplasmTreeController.LOG.error("Error in getting list types.", ex);
			return "Error in getting list types.";
		}

		return "Germplasm List";
	}

	/**
	 * Expand germplasm list folder.
	 *
	 * @param id the germplasm list ID
	 * @return the response page
	 */
	@RequestMapping(value = "/expandGermplasmListFolder/{id}", method = RequestMethod.GET)
	public String expandGermplasmListFolder(@PathVariable final String id, final Model model) {
		try {
			final List<TreeTableNode> childNodes = this.getGermplasmListFolderChildNodes(id, this.getCurrentProgramUUID());
			model.addAttribute(GermplasmTreeController.GERMPLASM_LIST_CHILD_NODES, childNodes);
		} catch (final Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
		}

		return super.showAjaxPage(model, GermplasmTreeController.GERMPLASM_LIST_TABLE_ROWS_PAGE);
	}

	/**
	 * Expand germplasm tree.
	 *
	 * @param parentKey the parent key
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/expandGermplasmTree/{parentKey}/{isFolderOnly}", method = RequestMethod.GET)
	public String expandGermplasmTree(@PathVariable final String parentKey, @PathVariable final String isFolderOnly) {
		final boolean isFolderOnlyBool = "1".equalsIgnoreCase(isFolderOnly);
		try {
			final List<TreeNode> childNodes = this.getGermplasmChildNodes(parentKey, isFolderOnlyBool, this.getCurrentProgramUUID());
			return TreeViewUtil.convertTreeViewToJson(childNodes);
		} catch (final Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
		}

		return "[]";
	}

	@ResponseBody
	@RequestMapping(value = "/expandGermplasmTree/{parentKey}", method = RequestMethod.GET)
	public String expandGermplasmAllTree(@PathVariable final String parentKey) {
		return this.expandGermplasmTree(parentKey, "0");
	}

	void checkIfUnique(final String folderName, final String programUUID) {
		final String trimmedName = folderName.trim();
		final List<GermplasmList> duplicate = this.germplasmListManager.getGermplasmListByName(trimmedName, programUUID, 0, 1, null);
		if (duplicate != null && !duplicate.isEmpty()) {
			throw new MiddlewareException(GermplasmTreeController.NAME_NOT_UNIQUE);
		}
		if (this.isSimilarToRootFolderName(trimmedName)) {
			throw new MiddlewareException(GermplasmTreeController.NAME_NOT_UNIQUE);
		}
	}

	protected boolean isSimilarToRootFolderName(final String itemName) {
		return itemName.equalsIgnoreCase(AppConstants.PROGRAM_LISTS.getString());

	}

	@ResponseBody
	@RequestMapping(value = "/addGermplasmFolder", method = RequestMethod.POST)
	public Map<String, Object> addGermplasmFolder(final HttpServletRequest req) {
		final String id = req.getParameter("parentFolderId");
		final String folderName = req.getParameter("folderName");
		final Map<String, Object> resultsMap = new HashMap<>();

		GermplasmList gpList = null;
		GermplasmList newList = null;
		try {
			final String programUUID = this.getCurrentProgramUUID();
			this.checkIfUnique(folderName, programUUID);
			final Integer userId = this.getCurrentIbdbUserId();

			if (id == null) {
				newList = new GermplasmList(null, folderName,
					Long.valueOf(new SimpleDateFormat(GermplasmTreeController.DATE_FORMAT).format(Calendar.getInstance().getTime())),
					GermplasmTreeController.FOLDER, userId, folderName, null, 0);
			} else {
				gpList = this.germplasmListManager.getGermplasmListById(Integer.parseInt(id));

				if (gpList != null && !gpList.isFolder()) {
					GermplasmList parent = null;

					parent = gpList.getParent();

					if (parent == null) {
						newList = new GermplasmList(null, folderName, Long.valueOf(
							new SimpleDateFormat(GermplasmTreeController.DATE_FORMAT).format(Calendar.getInstance().getTime())),
							GermplasmTreeController.FOLDER, userId, folderName, null, 0);
					} else {
						newList = new GermplasmList(null, folderName, Long.valueOf(
							new SimpleDateFormat(GermplasmTreeController.DATE_FORMAT).format(Calendar.getInstance().getTime())),
							GermplasmTreeController.FOLDER, userId, folderName, parent, 0);
					}
				} else {
					newList = new GermplasmList(null, folderName, Long.valueOf(
						new SimpleDateFormat(GermplasmTreeController.DATE_FORMAT).format(Calendar.getInstance().getTime())),
						GermplasmTreeController.FOLDER, userId, folderName, gpList, 0);
				}

			}

			newList.setDescription(folderName);
			newList.setProgramUUID(programUUID);
			final Integer germplasmListFolderId = this.germplasmListManager.addGermplasmList(newList);
			resultsMap.put("id", germplasmListFolderId);
			resultsMap.put(GermplasmTreeController.IS_SUCCESS, "1");
		} catch (final Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
			resultsMap.put(GermplasmTreeController.IS_SUCCESS, "0");
			resultsMap.put(GermplasmTreeController.MESSAGE, e.getMessage());
		}
		return resultsMap;
	}

	@ResponseBody
	@RequestMapping(value = "/renameGermplasmFolder", method = RequestMethod.POST)
	public Map<String, Object> renameStudyFolder(final HttpServletRequest req) {
		final Map<String, Object> resultsMap = new HashMap<>();
		final String newName = req.getParameter("newFolderName");
		final String folderId = req.getParameter("folderId");

		try {

			final GermplasmList gpList = this.germplasmListManager.getGermplasmListById(Integer.parseInt(folderId));

			this.checkIfUnique(newName, this.getCurrentProgramUUID());
			gpList.setName(newName);

			this.germplasmListManager.updateGermplasmList(gpList);

			resultsMap.put(GermplasmTreeController.IS_SUCCESS, "1");
		} catch (final Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
			resultsMap.put(GermplasmTreeController.IS_SUCCESS, "0");
			resultsMap.put(GermplasmTreeController.MESSAGE, e.getMessage());
		}
		return resultsMap;
	}

	public boolean hasChildren(final Integer id, final String programUUID) {
		return !this.germplasmListManager.getGermplasmListByParentFolderId(id, programUUID).isEmpty();
	}

	@ResponseBody
	@RequestMapping(value = "/deleteGermplasmFolder", method = RequestMethod.POST)
	public Map<String, Object> deleteGermplasmFolder(final HttpServletRequest req) {
		final Map<String, Object> resultsMap = new HashMap<>();

		final GermplasmList gpList;
		final String folderId = req.getParameter("folderId");
		try {
			gpList = this.germplasmListManager.getGermplasmListById(Integer.parseInt(folderId));

			if (this.hasChildren(gpList.getId(), this.getCurrentProgramUUID())) {
				throw new MiddlewareException(GermplasmTreeController.HAS_CHILDREN);
			}
			this.germplasmListManager.deleteGermplasmList(gpList);
			resultsMap.put(GermplasmTreeController.IS_SUCCESS, "1");
		} catch (final Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
			resultsMap.put(GermplasmTreeController.IS_SUCCESS, "0");
			resultsMap.put(GermplasmTreeController.MESSAGE, e.getMessage());
		}
		return resultsMap;
	}

	@ResponseBody
	@RequestMapping(value = "/moveGermplasmFolder", method = RequestMethod.POST)
	public Map<String, Object> moveStudyFolder(final HttpServletRequest req) {
		final String sourceId = req.getParameter("sourceId");
		final String targetId = req.getParameter("targetId");

		final Map<String, Object> resultsMap = new HashMap<>();

		try {

			final GermplasmList gpList = this.germplasmListManager.getGermplasmListById(Integer.parseInt(sourceId));

			if (targetId == null || PROGRAM_LISTS.equals(targetId) || CROP_LISTS.equals(targetId)) {
				gpList.setParent(null);
			} else {
				final GermplasmList parent = this.germplasmListManager.getGermplasmListById(Integer.parseInt(targetId));
				gpList.setParent(parent);
			}

			if (CROP_LISTS.equals(targetId)) {
				gpList.setProgramUUID(null);
				gpList.setStatus(LOCKED_LIST_STATUS);
			} else {
				gpList.setProgramUUID(this.contextUtil.getCurrentProgramUUID());
			}

			this.germplasmListManager.updateGermplasmList(gpList);

		} catch (final Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
		}

		return resultsMap;
	}

	@ResponseBody
	@RequestMapping(value = "/save/state/{type}")
	public String saveTreeState(@PathVariable final String type, @RequestParam(value = "expandedNodes[]") final String[] expandedNodes) {
		GermplasmTreeController.LOG.debug("Save the debug nodes");
		final List<String> states = new ArrayList<>();
		String status = "OK";
		try {

			if (!GermplasmTreeController.NODE_NONE.equalsIgnoreCase(expandedNodes[0])) {
				for (int index = 0; index < expandedNodes.length; index++) {
					states.add(expandedNodes[index]);
				}
			}

			if (states.isEmpty()) {
				states.add(GermplasmTreeController.DEFAULT_STATE_SAVED_FOR_GERMPLASM_LIST);
			}

			this.userTreeStateService
				.saveOrUpdateUserProgramTreeState(this.contextUtil.getCurrentWorkbenchUserId(), this.getCurrentProgramUUID(), type, states);
		} catch (final MiddlewareQueryException e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
			status = "ERROR";
		}
		return status;
	}

	@ResponseBody
	@RequestMapping(value = "/retrieve/state/{type}/{saveMode}", method = RequestMethod.GET)
	public String retrieveTreeState(@PathVariable final String type, @PathVariable final Boolean saveMode) {

		final List<String> stateList;
		final int userID = this.contextUtil.getCurrentWorkbenchUserId();
		final String programUUID = this.getCurrentProgramUUID();
		if (saveMode) {
			stateList = this.userTreeStateService.getUserProgramTreeStateForSaveList(userID, programUUID);
		} else {
			stateList = this.userTreeStateService.getUserProgramTreeStateByUserIdProgramUuidAndType(userID, programUUID, type);
		}

		return super.convertObjectToJson(stateList);
	}

	protected String getCurrentProgramUUID() {
		return this.contextUtil.getCurrentProgramUUID();
	}

	@Override
	public String getContentName() {
		return null;
	}

	protected void setFieldbookMiddlewareService(final FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	protected void setUserSelection(final UserSelection userSelection) {
		this.userSelection = userSelection;
	}

	void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	protected void setGermplasmListManager(final GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}
}
