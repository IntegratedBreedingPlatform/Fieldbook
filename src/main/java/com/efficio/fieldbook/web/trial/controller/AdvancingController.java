/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.trial.controller;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.TableHeader;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.naming.impl.AdvancingSourceListFactory;
import com.efficio.fieldbook.web.naming.service.NamingConventionService;
import com.efficio.fieldbook.web.trial.bean.AdvanceType;
import com.efficio.fieldbook.web.trial.bean.AdvancingStudy;
import com.efficio.fieldbook.web.trial.form.AdvancingStudyForm;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.pojo.AdvanceGermplasmChangeDetail;
import org.generationcp.commons.pojo.AdvancingSource;
import org.generationcp.commons.pojo.AdvancingSourceList;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.ruleengine.generator.SeedSourceGenerator;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.MethodType;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.study.StudyInstanceService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.generationcp.middleware.util.TimerWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.generationcp.middleware.service.api.dataset.ObservationUnitUtils.fromMeasurementRow;

@Controller
@RequestMapping(AdvancingController.URL)
public class AdvancingController extends AbstractBaseFieldbookController {

	private static final String UNIQUE_ID = "uniqueId";

	/**
	 * The Constant URL.
	 */
	protected static final String URL = "/StudyManager/advance/study";

	private static final String SAVE_ADVANCE_STUDY_PAGE_TEMPLATE = "StudyManager/saveAdvanceStudy";

	private static final String TABLE_HEADER_LIST = "tableHeaderList";

	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AdvancingController.class);

	private static final String IS_SUCCESS = "isSuccess";

	private static final String LIST_SIZE = "listSize";

	private static final String MESSAGE = "message";

	/**
	 * The fieldbook middleware service.
	 */
	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private UserSelection userSelection;

	@Resource
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Resource
	private MessageSource messageSource;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private DatasetService datasetService;

	@Resource
	private NamingConventionService namingConventionService;

	@Resource
	private AdvancingSourceListFactory advancingSourceListFactory;

	@Resource
	private SeedSourceGenerator seedSourceGenerator;

	@Resource
	private StudyDataManager studyDataManager;

	@Resource
	private StudyInstanceService studyInstanceService;

	private int advanceLinesThreshold;

	@Override
	public String getContentName() {
		return "StudyManager/advanceStudyModal";
	}

	@ModelAttribute("projectID")
	public String getProgramID() {
		return this.getCurrentProjectId();
	}

	/**
	 * Post advance Study.
	 *
	 * @param form the form
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	public Map<String, Object> postAdvanceStudy(@ModelAttribute("advancingStudyForm") final AdvancingStudyForm form) {

		final Map<String, Object> results = new HashMap<>();
		final Study study = this.fieldbookMiddlewareService.getStudy(Integer.parseInt(form.getStudyId()));
		final String lineSelected = form.getLineSelected() != null ? form.getLineSelected().trim() : null;
		final String harvestLocationAbbreviation =
			form.getHarvestLocationAbbreviation() != null ? form.getHarvestLocationAbbreviation() : "";

		final AdvancingStudy advancingStudy =
			new AdvancingStudy(study, form.getMethodChoice(), form.getLineChoice(), lineSelected, form.getHarvestDate(),
				form.getHarvestLocationId(),
				harvestLocationAbbreviation, form.getAdvanceBreedingMethodId(), form.getAllPlotsChoice(), form.getLineVariateId(),
				form.getMethodVariateId(), form.getPlotVariateId(),
				false, form.getSelectedTrialInstances(), form.getSelectedReplications(),
				AdvanceType.fromLowerCaseName(form.getAdvanceType()));
		final boolean observationsLoaded = this.fieldbookMiddlewareService.loadObservations(this.userSelection.getWorkbook(),
			advancingStudy.getSelectedTrialInstances().stream().map(i -> Integer.valueOf(i)).collect(Collectors.toList()),
			advancingStudy.getSelectedReplications() != null ?
				advancingStudy.getSelectedReplications().stream().map(i -> Integer.parseInt(i)).collect(Collectors.toList()) : null);

		try {

			if (advancingStudy.getMethodChoice() != null && !advancingStudy.getMethodChoice().isEmpty()) {
				final Method method = this.fieldbookMiddlewareService.getMethodById(Integer.parseInt(advancingStudy.getBreedingMethodId()));
				if ("GEN".equals(method.getMtype())) {
					form.setErrorInAdvance(this.messageSource.getMessage("study.save.advance.error.generative.method",
						new String[] {}, LocaleContextHolder.getLocale()));
					form.setGermplasmList(new ArrayList<>());
					form.setEntries(0);
					results.put(AdvancingController.IS_SUCCESS, "0");
					results.put(AdvancingController.LIST_SIZE, 0);
					results.put(AdvancingController.MESSAGE, form.getErrorInAdvance());

					return results;
				}
			}

			final List<AdvanceGermplasmChangeDetail> changeDetails = new ArrayList<>();
			final AdvancingSourceList list = this.getAdvancingSourceList(advancingStudy);
			final List<ImportedGermplasm> importedGermplasmList = this.createAdvanceList(advancingStudy, changeDetails, list);
			final long id = DateUtil.getCurrentDate().getTime();
			this.getPaginationListSelection().addAdvanceDetails(Long.toString(id), form);
			this.userSelection.setImportedAdvancedGermplasmList(importedGermplasmList);
			form.setGermplasmList(importedGermplasmList);
			form.setEntries(importedGermplasmList.size());
			form.setAdvancingSourceItems(list.getRows());
			form.changePage(1);
			form.setUniqueId(id);

			results.put(AdvancingController.IS_SUCCESS, "1");
			results.put(AdvancingController.LIST_SIZE, importedGermplasmList.size());
			results.put("advanceGermplasmChangeDetails", changeDetails);
			results.put(AdvancingController.UNIQUE_ID, id);

		} catch (final MiddlewareException | RuleException | FieldbookException e) {
			AdvancingController.LOG.error(e.getMessage(), e);
			form.setErrorInAdvance(this.messageSource.getMessage(e.getMessage(),
				new String[] {}, LocaleContextHolder.getLocale()));
			form.setGermplasmList(new ArrayList<>());
			form.setEntries(0);
			results.put(AdvancingController.IS_SUCCESS, "0");
			results.put(AdvancingController.LIST_SIZE, 0);
			results.put(AdvancingController.MESSAGE, form.getErrorInAdvance());
		} finally {
			// Important to clear out the observations collection from user session, once we are done with it to keep heap memory under
			// control. For large trials/nurseries the observations collection can be huge.
			if (observationsLoaded) {
				this.userSelection.getWorkbook().getObservations().clear();
			}
		}
		return results;
	}

	private List<ImportedGermplasm> createAdvanceList(final AdvancingStudy advanceInfo,
		final List<AdvanceGermplasmChangeDetail> changeDetails, final AdvancingSourceList list)
		throws RuleException {
		this.updatePlantsSelectedIfNecessary(list, advanceInfo);

		for (final AdvancingSource source : list.getRows()) {
			if (source.getChangeDetail() != null) {
				changeDetails.add(source.getChangeDetail());
			}
		}

		final List<ImportedGermplasm> germplasmList = this.generateGermplasmList(list, advanceInfo);
		this.namingConventionService.generateAdvanceListNames(list.getRows(), advanceInfo.isCheckAdvanceLinesUnique(), germplasmList);

		return germplasmList;
	}

	private AdvancingSourceList getAdvancingSourceList(final AdvancingStudy advanceInfo) throws FieldbookException {
		final Map<Integer, Method> breedingMethodMap = new HashMap<>();
		final Map<String, Method> breedingMethodCodeMap = new HashMap<>();
		final List<Method> methodList = this.fieldbookMiddlewareService.getAllBreedingMethods(false);

		for (final Method method : methodList) {
			breedingMethodMap.put(method.getMid(), method);
			breedingMethodCodeMap.put(method.getMcode(), method);
		}

		return this.createAdvancingSourceList(advanceInfo, breedingMethodMap, breedingMethodCodeMap);
	}

	List<ImportedGermplasm> generateGermplasmList(final AdvancingSourceList rows, final AdvancingStudy advancingParameters) {

		final List<ImportedGermplasm> list = new ArrayList<>();
		int index = 1;
		final TimerWatch timer = new TimerWatch("advance");
		final Map<String, String> locationIdNameMap =
			this.studyDataManager.createInstanceLocationIdToNameMapFromStudy(this.userSelection.getWorkbook().getStudyDetails().getId());
		final Map<Integer, StudyInstance> studyInstanceMap =
			this.studyInstanceService.getStudyInstances(advancingParameters.getStudy().getId()).stream()
				.collect(Collectors.toMap(StudyInstance::getInstanceNumber, i -> i));

		final List<MeasurementVariable> environmentVariables =
			this.datasetService.getObservationSetVariables(this.userSelection.getWorkbook().getTrialDatasetId(), Collections.singletonList(
				VariableType.ENVIRONMENT_DETAIL.getId()));
		final Map<String, Integer> keySequenceMap = new HashMap<>();
		for (final AdvancingSource row : rows.getRows()) {
			if (row.getGermplasm() != null && !row.isCheck() && row.getPlantsSelected() != null && row.getBreedingMethod() != null
				&& row.getPlantsSelected() > 0 && row.getBreedingMethod().isBulkingMethod() != null) {
				row.setKeySequenceMap(keySequenceMap);

				// if change detail object is created due to a duplicate being encountered somewhere during processing, provide a
				// reference index
				if (row.getChangeDetail() != null) {
					// index - 1 is used because Java uses 0-based referencing
					row.getChangeDetail().setIndex(index - 1);
				}

				// One plot may result in multiple plants/ears selected depending on selection method.
				int selectionNumber = row.getCurrentMaxSequence() + 1;
				final Iterator<SampleDTO> sampleIterator = row.getSamples().iterator();

				final int iterationCount = row.isBulk() ? 1 : row.getPlantsSelected();
				for (int i = 0; i < iterationCount; i++) {
					String sampleNo = null;
					if (sampleIterator.hasNext()) {
						sampleNo = String.valueOf(sampleIterator.next().getSampleNumber());
					}
					this.addImportedGermplasmToList(list, row, row.getBreedingMethod(), index++, selectionNumber,
						advancingParameters, sampleNo, locationIdNameMap, studyInstanceMap, environmentVariables);
					selectionNumber++;
				}

			}
		}
		timer.stop();
		return list;
	}

	protected void addImportedGermplasmToList(final List<ImportedGermplasm> list, final AdvancingSource source,
		final Method breedingMethod, final int index, final int selectionNumber,
		final AdvancingStudy advancingParameters, final String plantNo, final Map<String, String> locationIdNameMap,
		final Map<Integer, StudyInstance> studyInstanceMap,
		final List<MeasurementVariable> environmentVariables) {

		String selectionNumberToApply = null;
		final boolean allPlotsSelected = "1".equals(advancingParameters.getAllPlotsChoice());
		if (source.isBulk()) {
			if (allPlotsSelected) {
				selectionNumberToApply = null;
			} else {
				selectionNumberToApply = String.valueOf(source.getPlantsSelected());
			}
		} else {
			selectionNumberToApply = String.valueOf(selectionNumber);
		}

		// StudyId + InstanceNo -> Get the Instance with the locationId and pass a locations map by id and get the abbreviation

		// set the seed source string for the new Germplasm
		final String seedSource = this.seedSourceGenerator
			.generateSeedSource(fromMeasurementRow(
				this.userSelection.getWorkbook().getTrialObservationByTrialInstanceNo(Integer.valueOf(source.getTrialInstanceNumber()))),
				this.userSelection.getWorkbook().getConditions(), selectionNumberToApply, source.getPlotNumber(),
				this.userSelection.getWorkbook().getStudyName(), plantNo, locationIdNameMap, studyInstanceMap, environmentVariables);

		// Use index as germplasm name for now
		final ImportedGermplasm germplasm =
			new ImportedGermplasm(index, String.valueOf(index), null /* gid */
				, source.getGermplasm().getCross(), seedSource,
				FieldbookUtil.generateEntryCode(index), null /* check */
				, breedingMethod.getMid());

		// assign parentage etc for the new Germplasm
		final Integer sourceGid = source.getGermplasm().getGid() != null ? Integer.parseInt(source.getGermplasm().getGid()) : -1;
		final Integer gnpgs = source.getGermplasm().getGnpgs() != null ? source.getGermplasm().getGnpgs() : -1;
		this.assignGermplasmAttributes(germplasm, sourceGid, gnpgs, source.getGermplasm().getGpid1(), source.getGermplasm().getGpid2(),
			source.getSourceMethod(), breedingMethod);

		// assign grouping based on parentage

		// check to see if a group ID (MGID) exists in the parent for this Germplasm, and set
		// newly created germplasm if part of a group ( > 0 )
		if (source.getGermplasm().getMgid() != null && source.getGermplasm().getMgid() > 0) {
			germplasm.setMgid(source.getGermplasm().getMgid());
		}

		germplasm.setTrialInstanceNumber(source.getTrialInstanceNumber());
		germplasm.setReplicationNumber(source.getReplicationNumber());
		germplasm.setPlotNumber(source.getPlotNumber());
		germplasm.setLocationId(source.getHarvestLocationId());
		if (plantNo != null) {
			germplasm.setPlantNumber(plantNo);
		}

		list.add(germplasm);
	}

	private void assignGermplasmAttributes(final ImportedGermplasm germplasm, final Integer sourceGid, final Integer sourceGnpgs,
		final Integer sourceGpid1, final Integer sourceGpid2, final Method sourceMethod, final Method breedingMethod) {

		if ((sourceMethod != null && sourceMethod.getMtype() != null
			&& AppConstants.METHOD_TYPE_GEN.getString().equals(sourceMethod.getMtype())) || sourceGnpgs < 0 &&
			(sourceGpid1 != null && sourceGpid1.equals(0)) && (sourceGpid2 != null && sourceGpid2.equals(0))) {

			germplasm.setGpid1(sourceGid);
		} else {
			germplasm.setGpid1(sourceGpid1);
		}

		germplasm.setGpid2(sourceGid);

		if (breedingMethod != null) {
			germplasm.setGnpgs(breedingMethod.getMprgn());
		}
	}

	private AdvancingSourceList createAdvancingSourceList(final AdvancingStudy advanceInfo,
		final Map<Integer, Method> breedingMethodMap, final Map<String, Method> breedingMethodCodeMap) throws FieldbookException {

		final Study study = advanceInfo.getStudy();
		Workbook workbook = this.userSelection.getWorkbook();
		if (workbook == null) {
			workbook = this.fieldbookMiddlewareService.getStudyDataSet(study.getId());
		}
		return this.advancingSourceListFactory
			.createAdvancingSourceList(workbook, advanceInfo, study, breedingMethodMap, breedingMethodCodeMap);
	}

	private void updatePlantsSelectedIfNecessary(final AdvancingSourceList list, final AdvancingStudy info) {
		boolean lineChoiceSame = info.getLineChoice() != null && "1".equals(info.getLineChoice());
		final boolean allPlotsChoice = info.getAllPlotsChoice() != null && "1".equals(info.getAllPlotsChoice());
		int plantsSelected = 0;
		if (info.getLineSelected() != null && NumberUtils.isNumber(info.getLineSelected())) {
			plantsSelected = Integer.parseInt(info.getLineSelected());
		} else {
			lineChoiceSame = false;
		}
		if (list != null && list.getRows() != null && !list.getRows().isEmpty() && (lineChoiceSame && plantsSelected > 0
			|| allPlotsChoice)) {
			for (final AdvancingSource row : list.getRows()) {
				if (!row.isBulk() && lineChoiceSame) {
					row.setPlantsSelected(plantsSelected);
				} else if (row.isBulk() && allPlotsChoice) {
					// set it to 1, it does not matter since it's bulked
					row.setPlantsSelected(1);
				}
			}
		}
	}

	@ResponseBody
	@RequestMapping(value = "/apply/change/details", method = RequestMethod.POST)
	public Map<String, Object> applyChangeDetails(@RequestParam(value = "data") final String userResponses) throws IOException {
		final Map<String, Object> results = new HashMap<>();
		final ObjectMapper objectMapper = new ObjectMapper();
		final AdvanceGermplasmChangeDetail[] responseDetails = objectMapper.readValue(userResponses, AdvanceGermplasmChangeDetail[].class);
		final List<ImportedGermplasm> importedGermplasmListTemp = this.userSelection.getImportedAdvancedGermplasmList();
		final List<Integer> deletedEntryNumbers = new ArrayList<>();
		for (final AdvanceGermplasmChangeDetail responseDetail : responseDetails) {
			if (responseDetail.getIndex() < importedGermplasmListTemp.size()) {
				final ImportedGermplasm importedGermplasm = importedGermplasmListTemp.get(responseDetail.getIndex());
				if (responseDetail.getStatus() == 1) {
					// add germplasm name to gid
					// we need to delete
					deletedEntryNumbers.add(importedGermplasm.getEntryNumber());
				} else if (responseDetail.getStatus() == 3) {
					// choose gids
					importedGermplasm.setDesig(responseDetail.getNewAdvanceName());
					final List<Name> names = importedGermplasm.getNames();
					if (names != null) {
						// set the first value, for now, we're expecting only 1 records.
						// this was a list because in the past, we can have more than 1 names, but this was changed
						names.get(0).setNval(responseDetail.getNewAdvanceName());
					}
				}
			}
		}
		// now we need to delete all marked deleted
		int index = 1;
		for (final Iterator<ImportedGermplasm> iterator = importedGermplasmListTemp.iterator(); iterator.hasNext(); ) {
			final ImportedGermplasm germplasm = iterator.next();
			if (deletedEntryNumbers.contains(germplasm.getEntryNumber())) {
				iterator.remove();
			} else {
				germplasm.setEntryNumber(index++);
			}
		}
		this.userSelection.setImportedAdvancedGermplasmList(importedGermplasmListTemp);
		results.put(AdvancingController.IS_SUCCESS, "1");
		results.put(AdvancingController.LIST_SIZE, importedGermplasmListTemp.size());
		return results;
	}

	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public String showAdvanceStudy(@ModelAttribute("advancingStudyForm") final AdvancingStudyForm form, final Model model,
		final HttpServletRequest req) throws MiddlewareQueryException {

		try {
			/* The imported germplasm list. */
			final List<ImportedGermplasm> importedGermplasmList = this.userSelection.getImportedAdvancedGermplasmList();
			form.setGermplasmList(importedGermplasmList);
			form.setEntries(importedGermplasmList.size());
			form.changePage(1);
			final String uniqueId = req.getParameter(AdvancingController.UNIQUE_ID);
			form.setUniqueId(Long.valueOf(uniqueId));

			final List<Map<String, Object>> dataTableDataList = this.setupAdvanceReviewDataList(importedGermplasmList);

			model.addAttribute("advanceDataList", dataTableDataList);
			model.addAttribute(AdvancingController.TABLE_HEADER_LIST, this.getAdvancedStudyTableHeader());
			model.addAttribute("advanceLinesThreshold", this.advanceLinesThreshold);
		} catch (final Exception e) {
			AdvancingController.LOG.error(e.getMessage(), e);
			form.setErrorInAdvance(e.getMessage());
			form.setGermplasmList(new ArrayList<>());
			form.setEntries(0);
		}

		return super.showAjaxPage(model, AdvancingController.SAVE_ADVANCE_STUDY_PAGE_TEMPLATE);
	}

	@RequestMapping(value = "/delete/entries", method = RequestMethod.POST)
	public String deleteAdvanceStudyEntries(@ModelAttribute("advancingStudyForm") final AdvancingStudyForm form,
		final Model model, final HttpServletRequest req) throws MiddlewareQueryException {

		try {
			/* The imported germplasm list. */
			List<ImportedGermplasm> importedGermplasmList = this.userSelection.getImportedAdvancedGermplasmList();

			final String entryNumbers = req.getParameter("entryNums");
			final String[] entries = entryNumbers.split(",");
			importedGermplasmList = this.deleteImportedGermplasmEntries(importedGermplasmList, entries);
			this.userSelection.setImportedAdvancedGermplasmList(importedGermplasmList);

			form.setGermplasmList(importedGermplasmList);
			form.setEntries(importedGermplasmList.size());
			form.changePage(1);
			final String uniqueId = req.getParameter(AdvancingController.UNIQUE_ID);
			form.setUniqueId(Long.valueOf(uniqueId));

			// Update plant selected of the advancing sources based on deleted entries
			this.updateRemovedSelectedPlant(uniqueId, entries);

			final List<Map<String, Object>> dataTableDataList = this.setupAdvanceReviewDataList(importedGermplasmList);
			// remove the entry numbers

			model.addAttribute("advanceDataList", dataTableDataList);
			model.addAttribute(AdvancingController.TABLE_HEADER_LIST, this.getAdvancedStudyTableHeader());
		} catch (final Exception e) {
			AdvancingController.LOG.error(e.getMessage(), e);
			form.setErrorInAdvance(e.getMessage());
			form.setGermplasmList(new ArrayList<>());
			form.setEntries(0);
		}

		return super.showAjaxPage(model, AdvancingController.SAVE_ADVANCE_STUDY_PAGE_TEMPLATE);
	}

	protected List<ImportedGermplasm> deleteImportedGermplasmEntries(final List<ImportedGermplasm> importedGermplasmList,
		final String[] entries) {
		for (final String entryNumber : entries) {
			boolean isFound = false;
			int i = 0;
			for (i = 0; i < importedGermplasmList.size(); i++) {
				final ImportedGermplasm germplasm = importedGermplasmList.get(i);
				if (germplasm.getEntryNumber().toString().equalsIgnoreCase(entryNumber)) {
					isFound = true;
					break;
				}
			}
			if (isFound) {
				importedGermplasmList.remove(i);
			}
		}
		// now we need to set the entry id again
		for (int i = 0; i < importedGermplasmList.size(); i++) {
			final int newEntryNumber = i + 1;
			importedGermplasmList.get(i).setEntryNumber(newEntryNumber);
			importedGermplasmList.get(i).setEntryCode(FieldbookUtil.generateEntryCode(newEntryNumber));

		}

		return importedGermplasmList;
	}

	protected List<Map<String, Object>> setupAdvanceReviewDataList(final List<ImportedGermplasm> importedGermplasmList) {
		final List<Map<String, Object>> dataTableDataList = new ArrayList<>();
		for (final ImportedGermplasm germplasm : importedGermplasmList) {
			final Map<String, Object> dataMap = new HashMap<>();
			dataMap.put("desig", germplasm.getDesig());
			dataMap.put("gid", ImportedGermplasm.GID_PENDING);
			dataMap.put("entry", germplasm.getEntryNumber());
			dataMap.put("source", germplasm.getSource());
			dataMap.put("parentage", germplasm.getCross());

			dataMap.put("trialInstanceNumber", germplasm.getTrialInstanceNumber());
			dataMap.put("replicationNumber", germplasm.getReplicationNumber());

			dataTableDataList.add(dataMap);
		}
		return dataTableDataList;
	}

	protected List<TableHeader> getAdvancedStudyTableHeader() {
		final List<TableHeader> tableHeaderList = new ArrayList<>();

		tableHeaderList.add(new TableHeader(ColumnLabels.ENTRY_ID.getTermNameFromOntology(this.ontologyDataManager), "entry"));
		tableHeaderList.add(new TableHeader(ColumnLabels.DESIGNATION.getTermNameFromOntology(this.ontologyDataManager), "desig"));
		tableHeaderList.add(new TableHeader(ColumnLabels.PARENTAGE.getTermNameFromOntology(this.ontologyDataManager), "parentage"));
		tableHeaderList.add(new TableHeader(ColumnLabels.GID.getTermNameFromOntology(this.ontologyDataManager), "gid"));
		tableHeaderList.add(new TableHeader("GERMPLASM_ORIGIN", "source"));

		tableHeaderList.add(new TableHeader(ColumnLabels.TRIAL_INSTANCE.getTermNameFromOntology(this.ontologyDataManager),
			"trialInstanceNumber"));
		tableHeaderList.add(new TableHeader(ColumnLabels.REP_NO.getTermNameFromOntology(this.ontologyDataManager), "replicationNumber"));

		return tableHeaderList;
	}

	private List<StandardVariableReference> filterVariablesByProperty(final List<SettingDetail> variables, final String propertyName) {
		final List<StandardVariableReference> list = new ArrayList<>();
		if (variables != null && !variables.isEmpty()) {
			for (final SettingDetail detail : variables) {
				if (detail.getVariable() != null && detail.getVariable().getProperty() != null
					&& propertyName.equalsIgnoreCase(detail.getVariable().getProperty())) {
					list.add(new StandardVariableReference(detail.getVariable().getCvTermId(), detail.getVariable().getName(), detail
						.getVariable().getDescription()));
				}
			}
		}
		return list;
	}

	@ResponseBody
	@RequestMapping(value = "/countPlots/{ids}", method = RequestMethod.GET)
	public int countPlots(@PathVariable final String ids) throws MiddlewareQueryException {
		final String[] idList = ids.split(",");
		final List<Integer> idParams = new ArrayList<>();
		for (final String id : idList) {
			if (NumberUtils.isNumber(id)) {
				idParams.add(Double.valueOf(id).intValue());
			}
		}
		return this.fieldbookMiddlewareService.countPlotsWithRecordedVariatesInDataset(this.userSelection.getWorkbook()
			.getMeasurementDatesetId(), idParams);
	}

	@ResponseBody
	@RequestMapping(value = "/checkForNonMaintenanceAndDerivativeMethods/{id}", method = RequestMethod.GET)
	public Map<String, String> checkForNonMaintenanceAndDerivativeMethods(@PathVariable final Integer id,
		@RequestParam final Set<String> trialInstances) throws MiddlewareQueryException {
		final Map<String, String> result = new HashMap<>();
		final List<Method> methods = this.studyDataManager.getMethodsFromExperiments(this.userSelection.getWorkbook()
			.getMeasurementDatesetId(), id, new ArrayList<>(trialInstances));
		final Set<String> nonAdvancingMethods = methods.stream().filter(method ->
			!MethodType.getAdvancingMethodTypes().contains(method.getMtype())).map(Method::getMcode).collect(Collectors.toSet());
		if (!CollectionUtils.isEmpty(nonAdvancingMethods)) {
			result.put("errors", this.messageSource.getMessage("error.advancing.study.non.maintenance.derivative.method",
				new String[] {StringUtils.join(nonAdvancingMethods, ", ")}, LocaleContextHolder.getLocale()));
		}
		return result;
	}

	@ResponseBody
	@RequestMapping(value = "/checkMethodTypeMode/{methodVariateId}", method = RequestMethod.GET)
	public String checkMethodTypeMode(@PathVariable final int methodVariateId, @RequestParam final Set<String> trialInstances)
		throws MiddlewareQueryException {
		final List<Method> methods = this.studyDataManager.getMethodsFromExperiments(this.userSelection.getWorkbook()
			.getMeasurementDatesetId(), methodVariateId, new ArrayList<>(trialInstances));

		if (!methods.isEmpty()) {
			boolean isBulk = false;
			boolean isLine = false;
			for (final Method method : methods) {
				if (method.isBulkingMethod() != null && method.isBulkingMethod()) {
					isBulk = true;
				} else if (method.isBulkingMethod() != null && !method.isBulkingMethod()) {
					isLine = true;
				}
				if (isBulk && isLine) {
					return "MIXED";
				}
			}
			if (isBulk) {
				return "BULK";
			} else {
				return "LINE";
			}
		}
		final Locale locale = LocaleContextHolder.getLocale();
		String name = "";
		for (final MeasurementVariable variable : this.userSelection.getWorkbook().getAllVariables()) {
			if (variable.getTermId() == methodVariateId) {
				name = variable.getName();
				break;
			}
		}
		return this.messageSource.getMessage("error.advancing.study.empty.method", new String[] {name}, locale);
	}

	protected SettingDetail createSettingDetailWithVariableType(final int id, final String alias, final VariableType variableType) {
		final Variable variable = this.variableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(), id, false);

		String variableName = variable.getName();
		if (alias != null && !alias.isEmpty()) {
			variableName = alias;
		}

		final Property property = variable.getProperty();
		final Scale scale = variable.getScale();
		final org.generationcp.middleware.domain.ontology.Method method = variable.getMethod();

		final Double minValue = variable.getMinValue() == null ? null : Double.parseDouble(variable.getMinValue());
		final Double maxValue = variable.getMaxValue() == null ? null : Double.parseDouble(variable.getMaxValue());

		final SettingVariable settingVariable = new SettingVariable(variableName, variable.getDefinition(),
			variable.getProperty().getName(), scale.getName(), method.getName(), variableType.getRole().name(),
			scale.getDataType().getName(), scale.getDataType().getId(), minValue, maxValue);

		// NOTE: Using variable type which is used in project properties
		settingVariable.setVariableTypes(Collections.singleton(variableType));

		settingVariable.setCvTermId(variable.getId());
		settingVariable.setCropOntologyId(property.getCropOntologyId());

		if (variable.getFormula() != null) {
			settingVariable.setFormula(variable.getFormula());
		}

		if (!property.getClasses().isEmpty()) {
			settingVariable.setTraitClass(property.getClasses().iterator().next());
		}

		settingVariable.setOperation(Operation.ADD);
		final List<ValueReference> possibleValues = this.fieldbookService.getAllPossibleValues(id);

		final SettingDetail settingDetail = new SettingDetail(settingVariable, possibleValues, null, false);
		settingDetail.setRole(variableType.getRole());
		settingDetail.setVariableType(variableType);

		if (id == TermId.BREEDING_METHOD_ID.getId() || id == TermId.BREEDING_METHOD_CODE.getId()) {
			settingDetail.setValue(AppConstants.PLEASE_CHOOSE.getString());
		}
		settingDetail.setPossibleValuesToJson(possibleValues);
		final List<ValueReference> possibleValuesFavorite =
			this.fieldbookService.getAllPossibleValuesFavorite(id, this.getCurrentProject().getUniqueID(), false);
		settingDetail.setPossibleValuesFavorite(possibleValuesFavorite);
		settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
		return settingDetail;
	}

	@RequestMapping(value = "/selectEnvironmentModal", method = RequestMethod.GET)
	public String selectEnvironmentModal(final Model model) {
		return super.showAjaxPage(model, "StudyManager/selectEnvironmentModal");
	}

	public void updateRemovedSelectedPlant(final String uniqueId, final String[] deletedEntries) {
		final List<AdvancingSource> sources = this.getPaginationListSelection().getAdvanceDetails(uniqueId).getAdvancingSourceItems();
		if (!CollectionUtils.isEmpty(sources)) {
			int index = 1;
			final List<String> deletedEntriesList = Arrays.asList(deletedEntries);
			for (final AdvancingSource source : sources) {
				int startPlantSelected = source.getPlantsSelected() == null ? 0 : source.getPlantsSelected();
				for (int i=1; i <= startPlantSelected; i ++) {
					int newPlantSelected = source.getPlantsSelected() == null ? 0 : source.getPlantsSelected();
					if (deletedEntriesList.contains(String.valueOf(index))) {
						if (newPlantSelected - 1 >= 0) {
							source.setPlantsSelected(newPlantSelected - 1);
						}
					}
					index++;
				}
			}
		}
	}

	public int getAdvanceLinesThreshold() {
		return this.advanceLinesThreshold;
	}

	public void setAdvanceLinesThreshold(final int advanceLinesThreshold) {
		this.advanceLinesThreshold = advanceLinesThreshold;
	}
}
