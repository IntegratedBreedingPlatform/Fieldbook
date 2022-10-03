package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.common.bean.CrossImportSettings;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.CrossingTemplateExportException;
import com.efficio.fieldbook.web.common.exception.InvalidInputException;
import com.efficio.fieldbook.web.common.form.ImportCrossesForm;
import com.efficio.fieldbook.web.common.service.CrossingService;
import com.efficio.fieldbook.web.common.service.impl.CrossingTemplateExcelExporter;
import com.efficio.fieldbook.web.trial.controller.SettingsController;
import com.efficio.fieldbook.web.util.CrossesListUtil;
import com.efficio.fieldbook.web.util.DuplicatesUtil;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.middleware.ruleengine.pojo.ImportedCross;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.middleware.ruleengine.pojo.ImportedGermplasmParent;
import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.service.SettingsPresetService;
import org.generationcp.middleware.ruleengine.settings.CrossSetting;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.program.ProgramService;
import org.generationcp.middleware.api.tool.ToolService;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.PresetService;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.MethodType;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.UDTableType;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.generationcp.middleware.service.api.user.UserService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping(CrossingSettingsController.URL)
public class CrossingSettingsController extends SettingsController {

	public static final String URL = "/crosses";
	static final int YEAR_INTERVAL = 10;
	public static final String ID = "id";
	public static final String TEXT = "text";
	static final String SUCCESS_KEY = "success";

	private static final Logger LOG = LoggerFactory.getLogger(CrossingSettingsController.class);
	static final String IS_SUCCESS = "isSuccess";
	private static final String HAS_PLOT_DUPLICATE = "hasPlotDuplicate";
	private static final String CHOOSING_LIST_OWNER_NEEDED = "isChoosingListOwnerNeeded";
	public static final String ERROR = "error";

	private static final String OUTPUT_FILENAME = "outputFilename";
	private static final String FILENAME = "filename";

	@Autowired
	private CrossExpansionProperties crossExpansionProperties;

	@Resource
	private PresetService presetService;

	@Resource
	private SettingsPresetService settingsPresetService;

	@Resource
	private UserSelection studySelection;

	@Resource
	private CrossingService crossingService;

	@Resource
	private CrossingTemplateExcelExporter crossingTemplateExcelExporter;

	@Resource
	private MessageSource messageSource;

	@Resource
	private CrossesListUtil crossesListUtil;

	@Resource
	private GermplasmDataManager germplasmDataManager;

	@Resource
	private ToolService toolService;

	@Resource
	private UserService userService;

	@Resource
	private ProgramService programService;

	@Resource
	private BreedingMethodService breedingMethodService;

	/**
	 * The germplasm list manager.
	 */
	@Resource
	private GermplasmListManager germplasmListManager;

	@Resource
	private StudyEntryService studyEntryService;

	@Override
	public String getContentName() {
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/retrieveSettings", method = RequestMethod.GET, produces = "application/json")
	public List<CrossImportSettings> getAvailableCrossImportSettings() {
		final List<CrossImportSettings> settings = new ArrayList<>();

		try {

			final int fieldbookToolId = this.toolService.getToolWithName(ToolName.FIELDBOOK_WEB.getName()).getToolId().intValue();

			final List<ProgramPreset> presets = this.presetService
					.getProgramPresetFromProgramAndTool(this.getCurrentProgramID(), fieldbookToolId,
							ToolSection.FBK_CROSS_IMPORT.name());

			for (final ProgramPreset preset : presets) {
				final CrossSetting crossSetting =
						(CrossSetting) this.settingsPresetService.convertPresetFromXmlString(preset.getConfiguration(), CrossSetting.class);
				final CrossImportSettings importSettings = new CrossImportSettings();
				importSettings.populate(crossSetting);
				importSettings.setProgramPresetId(preset.getProgramPresetId());
				settings.add(importSettings);
			}

		} catch (final MiddlewareQueryException | JAXBException e) {
			CrossingSettingsController.LOG.error(e.getMessage(), e);
		}

		return settings;
	}

	@ResponseBody
	@RequestMapping(value = "/submitAndSaveSetting", method = RequestMethod.POST, consumes = "application/json",
			produces = "application/json")
	public Map<String, Object> submitAndSaveCrossSettings(@RequestBody final CrossSetting settings) {
		final Map<String, Object> returnVal = new HashMap<>();
		try {
			this.saveCrossSetting(settings, this.getCurrentProgramID());
			return this.submitCrossSettings(settings);
		} catch (final MiddlewareQueryException | JAXBException e) {
			CrossingSettingsController.LOG.error(e.getMessage(), e);
		}

		returnVal.put(CrossingSettingsController.SUCCESS_KEY, "0");
		return returnVal;
	}

	@ResponseBody
	@RequestMapping(value = "/deleteSetting/{programPresetId}", method = RequestMethod.DELETE)
	public Map<String, Object> deleteSetting(@PathVariable final Integer programPresetId) {
		final Map<String, Object> returnVal = new HashMap<>();

		this.deleteCrossSetting(programPresetId);

		returnVal.put(CrossingSettingsController.SUCCESS_KEY, "1");
		return returnVal;
	}

	@ResponseBody
	@RequestMapping(value = "/generateSequenceValue", method = RequestMethod.POST, consumes = "application/json",
			produces = "application/json")
	public Map<String, String> generateSequenceValue(@RequestBody final CrossSetting setting, final HttpServletRequest request) {
		final Map<String, String> returnVal = new HashMap<>();

		try {
			synchronized (CrossingSettingsController.class) {
				final String sequenceValue = this.crossingService.getNextNameInSequence(setting.getCrossNameSetting());
				returnVal.put(CrossingSettingsController.SUCCESS_KEY, "1");
				returnVal.put("sequenceValue", sequenceValue);
			}
		} catch (final InvalidInputException e) {
			CrossingSettingsController.LOG.error(e.getMessage(), e);
			returnVal.put(CrossingSettingsController.SUCCESS_KEY, "0");
			returnVal.put(ERROR, e.getMessage());
		} catch (final RuntimeException e) {
			CrossingSettingsController.LOG.error(e.getMessage(), e);
			returnVal.put(CrossingSettingsController.SUCCESS_KEY, "0");
			returnVal.put(ERROR,
					this.messageSource.getMessage("error.no.next.name.in.sequence", new Object[] {}, LocaleContextHolder.getLocale()));
		}

		return returnVal;
	}

	@ResponseBody
	@RequestMapping(value = "/submit", method = RequestMethod.POST)
	public Map<String, Object> submitCrossSettings(@RequestBody final CrossSetting settings) {
		final Map<String, Object> returnVal = new HashMap<>();

		this.studySelection.setCrossSettings(settings);
		returnVal.put(CrossingSettingsController.SUCCESS_KEY, 1);
		return returnVal;
	}

	@ResponseBody
	@RequestMapping(value = "/getHarvestYears", method = RequestMethod.GET)
	public List<String> getHarvestYears() {
		final List<String> years = new ArrayList<>();

		final Calendar cal = DateUtil.getCalendarInstance();

		final int currentYear = cal.get(Calendar.YEAR);

		//the years should include + 10 years, current year and - 10 years
		for (int year = currentYear + YEAR_INTERVAL; year >= currentYear - YEAR_INTERVAL; year--) {
			years.add(Integer.toString(year));
		}

		return years;
	}

	@ResponseBody
	@RequestMapping(value = "/getHarvestMonths", method = RequestMethod.GET)
	public List<Map<String, String>> getHarvestMonths() {
		final List<Map<String, String>> monthList = new ArrayList<>();

		final String[] monthLabels = DateFormatSymbols.getInstance().getMonths();
		int i = 1;
		for (final String monthLabel : monthLabels) {
			if (monthLabel.isEmpty()) {
				continue;
			}

			String textValue = Integer.toString(i++);
			if (textValue.length() == 1) {
				textValue = "0" + textValue;
			}

			final Map<String, String> monthMap = new HashMap<>();
			monthMap.put(CrossingSettingsController.ID, textValue);
			monthMap.put(CrossingSettingsController.TEXT, monthLabel);

			monthList.add(monthMap);
		}

		return monthList;

	}

	/**
	 * Validates the Breeding Methods in the import file
	 *
	 * @return a JSON result object
	 */
	@ResponseBody
	@RequestMapping(value = "/validateBreedingMethods", method = RequestMethod.GET)
	public Map<String, Object> validateBreedingMethods(@RequestParam(required = false) final Integer breedingMethodId) {
		final Map<String, Object> out = new HashMap<>();

		if(breedingMethodId == null) {
			final Set<String> breedingMethods = this.studySelection.getImportedCrossesList().getImportedCrosses().stream()
				.filter(cross -> !StringUtils.isEmpty(cross.getRawBreedingMethod())).map(ImportedCross::getRawBreedingMethod).collect(
					Collectors.toSet());
			if(!CollectionUtils.isEmpty(breedingMethods)) {
				final BreedingMethodSearchRequest breedingMethodSearchRequest = new BreedingMethodSearchRequest();
				breedingMethodSearchRequest.setMethodAbbreviations(new ArrayList<>(breedingMethods));
				breedingMethodSearchRequest.setMethodTypes(Collections.singletonList(MethodType.GENERATIVE.getCode()));
				final List<BreedingMethodDTO> generativeBreedingMethodDtos = this.breedingMethodService
					.searchBreedingMethods(breedingMethodSearchRequest, null, null);

				if (generativeBreedingMethodDtos.size() != breedingMethods.size()) {
					final List<String> generativeBreedingMethodCodes = generativeBreedingMethodDtos.stream().map(BreedingMethodDTO::getCode)
						.collect(Collectors.toList());
					final List<String> nonGenerativeBreedingMethodCodes = breedingMethods.stream()
						.filter(method -> !generativeBreedingMethodCodes.contains(method)).collect(Collectors.toList());
					out.put(CrossingSettingsController.ERROR, this.messageSource.getMessage("error.crossing.non.generative.method",
						new String[] {StringUtils.join(nonGenerativeBreedingMethodCodes, ", ")}, LocaleContextHolder.getLocale()));
					return out;
				}

				final List<String> methodCodesWithOneMPRGN = generativeBreedingMethodDtos.stream()
					.filter(dto -> dto.getNumberOfProgenitors() == 1).map(BreedingMethodDTO::getCode).collect(Collectors.toList());
				if (!CollectionUtils.isEmpty(methodCodesWithOneMPRGN)) {
					out.put(CrossingSettingsController.ERROR, this.messageSource.getMessage("error.crossing.method.mprgn.equals.one",
						new String[] {StringUtils.join(methodCodesWithOneMPRGN, ", ")}, LocaleContextHolder.getLocale()));
				}

				final List<String> methodCodesWithBlankPrefix = generativeBreedingMethodDtos.stream()
					.filter(dto -> StringUtils.isBlank(dto.getPrefix())).map(BreedingMethodDTO::getCode).collect(Collectors.toList());
				if (!CollectionUtils.isEmpty(methodCodesWithBlankPrefix)) {
					out.put(CrossingSettingsController.ERROR, this.messageSource.getMessage("error.crossing.method.blank.prefix",
						new String[] {StringUtils.join(methodCodesWithBlankPrefix, ", ")}, LocaleContextHolder.getLocale()));
				}
			}
		} else {
			final Method breedingMethod = this.germplasmDataManager.getMethodByID(breedingMethodId);
			if(!MethodType.GENERATIVE.getCode().equals(breedingMethod.getMtype())) {
				out.put(CrossingSettingsController.ERROR, this.messageSource.getMessage("error.crossing.selected.non.generative.method",
					new String[] {breedingMethod.getMcode()}, LocaleContextHolder.getLocale()));
			} else if (breedingMethod.getMprgn() == 1 ) {
				out.put(CrossingSettingsController.ERROR, this.messageSource.getMessage("error.crossing.selected.method.mprgn.equals.one",
					new String[] {breedingMethod.getMcode()}, LocaleContextHolder.getLocale()));
			} else if (StringUtils.isBlank(breedingMethod.getPrefix())) {
				out.put(CrossingSettingsController.ERROR, this.messageSource.getMessage("error.crossing.selected.method.blank.prefix",
					new String[] {breedingMethod.getMcode()}, LocaleContextHolder.getLocale()));
			}
		}

		return out;
	}

	/**
	 * Validates if current study can perform an export
	 *
	 * @return a JSON result object
	 */
	@ResponseBody
	@RequestMapping(value = "/export", method = RequestMethod.GET)
	public Map<String, Object> doCrossingExport() {
		final Map<String, Object> out = new HashMap<>();
		try {
			Integer studyId = this.studySelection.getWorkbook().getStudyDetails().getId();
			if (studyId == null && this.studySelection.getWorkbook().getStudyDetails() != null) {
				studyId = this.studySelection.getWorkbook().getStudyDetails().getId();
			}

			final Integer currentUserId = this.contextUtil.getCurrentWorkbenchUserId();

			final FileExportInfo exportInfo =
					this.crossingTemplateExcelExporter.export(studyId, this.studySelection.getWorkbook().getStudyName(), currentUserId);

			out.put(CrossingSettingsController.IS_SUCCESS, Boolean.TRUE);
			out.put(OUTPUT_FILENAME, exportInfo.getFilePath());
			out.put(FILENAME, exportInfo.getDownloadFileName());

		} catch (final CrossingTemplateExportException | NullPointerException e) {
			CrossingSettingsController.LOG.debug(e.getMessage(), e);

			out.put(CrossingSettingsController.IS_SUCCESS, Boolean.FALSE);
			out.put("errorMessage", this.messageSource
					.getMessage(e.getMessage(), new String[] {}, "cannot export a crossing template", LocaleContextHolder.getLocale()));
		}

		return out;
	}

	@ResponseBody
	@RequestMapping(value = "/download/file", method = RequestMethod.POST, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<FileSystemResource> download(final HttpServletRequest req) throws UnsupportedEncodingException {
		final String outputFilename =
				new String(req.getParameter(CrossingSettingsController.OUTPUT_FILENAME).getBytes( StandardCharsets.ISO_8859_1 ),
					StandardCharsets.UTF_8 );
		final String filename = new String(req.getParameter(CrossingSettingsController.FILENAME).getBytes( StandardCharsets.ISO_8859_1 ),
			StandardCharsets.UTF_8 );

		return FieldbookUtil.createResponseEntityForFileDownload(outputFilename, filename);
	}

	@ResponseBody
	@RequestMapping(value = "/germplasm", method = RequestMethod.POST)
	public String importFile(final Model model, @ModelAttribute("importCrossesForm") final ImportCrossesForm form) {

		final Map<String, Object> resultsMap = new HashMap<>();

		// 1. PARSE the file into an ImportCrosses List
		try {
			final ImportedCrossesList parseResults = this.crossingService.parseFile(form.getFile());
			// 2. Process duplicates and set to ImportedCrossesList
			DuplicatesUtil.processDuplicatesAndReciprocals(parseResults);
			// 3. Store the crosses to study selection if all validated

			this.setParentsInformation(parseResults.getImportedCrosses());

			parseResults.setType(GermplasmListType.F1IMP.toString());

			this.studySelection.setImportedCrossesList(parseResults);

			resultsMap.put(CrossingSettingsController.IS_SUCCESS, 1);
			resultsMap.put(CrossingSettingsController.HAS_PLOT_DUPLICATE, parseResults.hasPlotDuplicate());

			if (!parseResults.getWarningMessages().isEmpty()) {
				resultsMap.put("warnings", parseResults.getWarningMessages());
			}

			// if no User is set we need to ask the User for the input via chooseUser modal dialog
			if (parseResults.getUserId() == null) {
				resultsMap.put(CrossingSettingsController.CHOOSING_LIST_OWNER_NEEDED, 1);
			} else {
				resultsMap.put(CrossingSettingsController.CHOOSING_LIST_OWNER_NEEDED, 0);
			}

			resultsMap.put("hasHybridMethod", this.checkForHybridMethods(parseResults.getImportedCrosses()));
		} catch (final FileParsingException e) {
			CrossingSettingsController.LOG.error(e.getMessage(), e);
			resultsMap.put(CrossingSettingsController.IS_SUCCESS, 0);
			resultsMap.put(ERROR, new String[] {e.getMessage()});
		}

		return super.convertObjectToJson(resultsMap);
	}

	boolean checkForHybridMethods(final List<ImportedCross> importedCrosses) {
		final List<String> hybridMethods =
				this.germplasmDataManager.getMethodCodeByMethodIds(this.crossExpansionProperties.getHybridBreedingMethods());
		for (final ImportedCross importedCross : importedCrosses) {
			if (hybridMethods.contains(importedCross.getRawBreedingMethod().toUpperCase())) {
				return true;
			}
		}
		return false;
	}

	@ResponseBody
	@RequestMapping(value = "/getHybridMethods", method = RequestMethod.GET)
	public Set<Integer> getHybridMethods() {
		return this.crossExpansionProperties.getHybridBreedingMethods();
	}

	@ResponseBody
	@RequestMapping(value = "/getLocationIdFromFirstEnviroment", method = RequestMethod.GET)
	public String getLocationIdFromEnviroment() {
		return this.studySelection.getWorkbook().getTrialObservations().get(0).getDataList().stream()
			.filter(measurementData -> measurementData.getMeasurementVariable().getTermId() == TermId.LOCATION_ID.getId()).findFirst().get()
			.getValue();
	}

	/** This is used for the Review Imported Cross (no temporary list created yet as in Design Crosses **/
	@ResponseBody
	@RequestMapping(value = "/getImportedCrossesList/{checkExistingCrosses}", method = RequestMethod.GET)
	public Map<String, Object> getImportedCrossesList(@PathVariable final boolean checkExistingCrosses) {
		final Map<String, Object> responseMap = new HashMap<>();
		final ImportedCrossesList importedCrossesList = this.studySelection.getImportedCrossesList();

		if (null == importedCrossesList) {
			return responseMap;
		}
		final UserDefinedField importedCrossUserDefinedField = this.germplasmDataManager
				.getUserDefinedFieldByTableTypeAndCode(UDTableType.LISTNMS_LISTTYPE.getTable(), UDTableType.LISTNMS_LISTTYPE.getType(),
						GermplasmListType.F1IMP.name());

		importedCrossesList.setTitle(importedCrossUserDefinedField.getFdesc());

		this.crossingService.processCrossBreedingMethod(this.studySelection.getCrossSettings(), importedCrossesList);

		// TODO decouple save and apply settings and then replace this for for the apply settings method
		this.crossingService.populateSeedSource(importedCrossesList, this.userSelection.getWorkbook());

		final List<Map<String, Object>> masterList = new ArrayList<>();
		final List<String> tableHeaderList = this.crossesListUtil.getTableHeaders();

		for (final ImportedCross cross : importedCrossesList.getImportedCrosses()) {
			masterList.add(this.crossesListUtil.generateCrossesTableWithDuplicationNotes(tableHeaderList, cross, checkExistingCrosses));
		}

		responseMap.put(CrossesListUtil.TABLE_HEADER_LIST, tableHeaderList);
		responseMap.put(CrossesListUtil.LIST_DATA_TABLE, masterList);
		responseMap.put(CrossingSettingsController.IS_SUCCESS, 1);

		responseMap.put(CrossesListUtil.IS_IMPORT, true);
		return responseMap;
	}

	@ResponseBody
	@RequestMapping(value = "/getExistingCrossesList/{femaleGID}/{maleGIDs}/{gid}", method = RequestMethod.GET)
	public Map<String, Object> getExistingCrossesList(@PathVariable final Integer femaleGID, @PathVariable final List<Integer> maleGIDs,
		@PathVariable final String gid) {
		final Map<String, Object> responseMap = new HashMap<>();

		final List<Map<String, Object>> masterList = new ArrayList<>();
		final List<String> tableHeaderList = Arrays.asList(ColumnLabels.GID.getName(), ColumnLabels.DESIGNATION.getName());
		final Optional<Integer> optionalGid = gid.equals("null") ? Optional.empty(): Optional.of(Integer.valueOf(gid));
		final List<Germplasm> existingCrosses = this.germplasmDataManager.getExistingCrosses(Integer.valueOf(femaleGID), maleGIDs, optionalGid);
		for(final Germplasm existingCross: existingCrosses) {
			final Map<String, Object> dataMap = new HashMap<>();
			dataMap.put(ColumnLabels.GID.getName(), existingCross.getGid());
			dataMap.put(ColumnLabels.DESIGNATION.getName(), existingCross.getGermplasmPreferredName());
			masterList.add(dataMap);
		}

		responseMap.put(CrossesListUtil.TABLE_HEADER_LIST, tableHeaderList);
		responseMap.put(CrossesListUtil.LIST_DATA_TABLE, masterList);
		responseMap.put(CrossingSettingsController.IS_SUCCESS, 1);

		responseMap.put(CrossesListUtil.IS_IMPORT, true);
		return responseMap;
	}

	@ResponseBody
	@RequestMapping(value = "/getCurrentProgramMembers", method = RequestMethod.GET, produces = "application/json")
	public Map<String, Person> getCurrentProgramMembers() {
		// we need to convert Integer to String because angular doest work with numbers as options for select

		final String cropName = this.contextUtil.getProjectInContext().getCropType().getCropName();

		final Map<String, Person> currentProgramMembers = new HashMap<>();
		final Long projectId = this.programService.getProjectByUuidAndCrop(this.getCurrentProgramID(), cropName).getProjectId();

		//TODO Verify if it is possible to return a UserDto instead of a Map
		final List<WorkbenchUser> users = this.userService.getUsersByProjectId(projectId);
		for (final WorkbenchUser user : users) {
			currentProgramMembers.put(String.valueOf(user.getUserid()), user.getPerson());
		}
		return currentProgramMembers;
	}

	@ResponseBody
	@RequestMapping(value = "/getCurrentUser", method = RequestMethod.GET)
	public String getCurrentWorkbenchUser() {
		return String.valueOf(this.contextUtil.getCurrentWorkbenchUserId());
	}

	@ResponseBody
	@RequestMapping(value = "/submitListOwner", method = RequestMethod.POST)
	public Map<String, Object> submitListOwner(@RequestBody final String workbenchUserId) {
		final Map<String, Object> returnVal = new HashMap<>();
		final int workbenchUID;
		try {
			workbenchUID = Integer.parseInt(workbenchUserId);
		} catch (final Exception e) {
			CrossingSettingsController.LOG.error(e.getMessage(), e);
			final Map<String, Object> resultsMap = new HashMap<>();
			resultsMap.put(CrossingSettingsController.IS_SUCCESS, 0);
			final String localisedErrorMessage = this.messageSource
					.getMessage("error.submit.list.owner.wrong.format", new String[] {}, "Could not associate User id with the list",
							LocaleContextHolder.getLocale());
			resultsMap.put(ERROR, new String[] {localisedErrorMessage});
			return resultsMap;
		}

		this.studySelection.getImportedCrossesList().setUserId(workbenchUID);
		returnVal.put(CrossingSettingsController.IS_SUCCESS, 1);
		return returnVal;
	}

	@ResponseBody
	@RequestMapping(value = "/deleteCrossList/{createdCrossesListId}", method = RequestMethod.DELETE)
	public Map<String, Object> deleteCrossList(@PathVariable final Integer createdCrossesListId) {
		final Map<String, Object> responseMap = new HashMap<>();

		this.germplasmListManager.deleteGermplasmListByListIdPhysically(createdCrossesListId);

		responseMap.put(CrossingSettingsController.IS_SUCCESS, 1);
		return responseMap;
	}

	@ResponseBody
	@RequestMapping(value = "/getImportedCrossesList/{checkExistingCrosses}/{createdCrossesListId}", method = RequestMethod.GET)
	public Map<String, Object> getImportedCrossesList(@PathVariable final boolean checkExistingCrosses,
		@PathVariable final String createdCrossesListId) {
		final Map<String, Object> responseMap = new HashMap<>();
		final List<Map<String, Object>> masterList = new ArrayList<>();
		final List<ImportedCross> importedCrosses = new ArrayList<>();
		final Map<Integer, ImportedCross> importedCrossesMap = new HashMap<>();

		final Integer crossesListId = Integer.parseInt(createdCrossesListId);
		final List<GermplasmListData> germplasmListDataList = this.germplasmListManager.retrieveGermplasmListDataWithParents(crossesListId);
		final Integer studyId = this.studySelection.getWorkbook().getStudyDetails().getId();
		final Map<Integer, StudyEntryDto> plotEntriesMap = this.studyEntryService.getPlotEntriesMap(studyId, Collections.emptySet());
		final String studyName = this.studySelection.getWorkbook().getStudyDetails().getStudyName();
		final List<String> tableHeaderList = this.crossesListUtil.getTableHeaders();
		for (final GermplasmListData listData : germplasmListDataList) {
			final ImportedCross importedCross = this.crossesListUtil.convertGermplasmListDataToImportedCrosses(listData, studyName, plotEntriesMap);
			if (importedCross.getGid() == null) {
				responseMap.put(CrossingSettingsController.IS_SUCCESS, 0);
				final String localisedErrorMessage = this.messageSource.getMessage("error.germplasm.record.already.exists", new String[] {},
						"Cross germplasm record must already exist in database when using crossing manager to create crosses in Studies",
						LocaleContextHolder.getLocale());
				responseMap.put(ERROR, new String[] {localisedErrorMessage});
				return responseMap;
			}

			importedCrosses.add(importedCross);
			importedCrossesMap.put(importedCross.getEntryNumber(), importedCross);
		}
		final ImportedCrossesList importedCrossesList = new ImportedCrossesList();
		final GermplasmList germplasmList = this.germplasmListManager.getGermplasmListById(crossesListId);
		importedCrossesList.setImportedGermplasms(importedCrosses);
		importedCrossesList.setType(germplasmList.getType());
		importedCrossesList.setUserId(germplasmList.getUserId());
		this.userSelection.setImportedCrossesList(importedCrossesList);

		this.crossingService.processCrossBreedingMethod(this.studySelection.getCrossSettings(), importedCrossesList);
		for (final ImportedCross cross : importedCrossesList.getImportedCrosses()) {
			masterList.add(this.crossesListUtil.generateCrossesTableWithDuplicationNotes(tableHeaderList, cross, checkExistingCrosses));
		}

		final UserDefinedField createdCrossUserDefinedField = this.germplasmDataManager
				.getUserDefinedFieldByTableTypeAndCode(UDTableType.LISTNMS_LISTTYPE.getTable(), UDTableType.LISTNMS_LISTTYPE.getType(),
						GermplasmListType.F1CRT.name());
		importedCrossesList.setTitle(createdCrossUserDefinedField.getFdesc());

		importedCrossesList.setDate(DateUtil.getCurrentDate());

		for (final Map<String, Object> map : masterList) {
			final Integer entryId = (Integer) map.get(tableHeaderList.get(CrossesListUtil.ENTRY_INDEX));
			final String breedingMethodIndex = tableHeaderList.get(CrossesListUtil.BREEDING_METHOD_INDEX);
			final String seedSourceIndex = tableHeaderList.get(CrossesListUtil.SOURCE_INDEX);

			map.put(breedingMethodIndex, importedCrossesMap.get(entryId).getBreedingMethodName());
			map.put(seedSourceIndex, importedCrossesMap.get(entryId).getSource());

		}

		responseMap.put(CrossesListUtil.TABLE_HEADER_LIST, tableHeaderList);
		responseMap.put(CrossesListUtil.LIST_DATA_TABLE, masterList);
		responseMap.put(CrossingSettingsController.IS_SUCCESS, 1);
		responseMap.put(CrossesListUtil.IS_IMPORT, false);
		return responseMap;
	}

	void deleteCrossSetting(final int programPresetId) {
		this.presetService.deleteProgramPreset(programPresetId);
	}

	private void saveCrossSetting(final CrossSetting setting, final String programUUID) throws JAXBException {

		final int fieldbookToolId = this.toolService.getToolWithName(ToolName.FIELDBOOK_WEB.getName()).getToolId().intValue();
		final List<ProgramPreset> presets = this.presetService
				.getProgramPresetFromProgramAndTool(programUUID, fieldbookToolId, ToolSection.FBK_CROSS_IMPORT.name());

		boolean found = false;
		ProgramPreset forSaving = null;
		for (final ProgramPreset preset : presets) {
			if (preset.getName().equals(setting.getName())) {
				preset.setConfiguration(this.settingsPresetService.convertPresetSettingToXml(setting, CrossSetting.class));
				found = true;
				forSaving = preset;
				break;
			}
		}

		if (!found) {
			forSaving = new ProgramPreset();
			forSaving.setName(setting.getName());
			forSaving.setToolId(this.toolService.getToolWithName(ToolName.FIELDBOOK_WEB.getName()).getToolId().intValue());
			forSaving.setProgramUuid(programUUID);
			forSaving.setToolSection(ToolSection.FBK_CROSS_IMPORT.name());
			forSaving.setConfiguration(this.settingsPresetService.convertPresetSettingToXml(setting, CrossSetting.class));
		}

		this.presetService.saveOrUpdateProgramPreset(forSaving);
	}

	String getCurrentProgramID() {
		return this.contextUtil.getCurrentProgramUUID();
	}

	void setCrossesListUtil(final CrossesListUtil crossesListUtil) {
		this.crossesListUtil = crossesListUtil;
	}

	void setParentsInformation(final List<ImportedCross> importedCrossList) {
		final List<Integer> maleGidList = new ArrayList<>();
		for (final ImportedCross cross : importedCrossList) {
			maleGidList.addAll(cross.getMaleGids());
		}

		final Collection<Integer> femaleGidList = Collections2.transform(importedCrossList, new Function<ImportedCross, Integer>() {

			@Override
			public Integer apply(final ImportedCross input) {
				return Integer.parseInt(input.getFemaleGid());
			}
		});

		final List<Integer> gidList = new ArrayList<>();
		gidList.addAll(maleGidList);
		gidList.addAll(femaleGidList);

		final ImmutableList<Integer> listWithNoDuplicates = ImmutableSet.copyOf(gidList).asList();

		final Map<Integer, String[]> pedigreeMap = this.germplasmDataManager.getParentsInfoByGIDList(listWithNoDuplicates);
		for (final ImportedCross importedCross : importedCrossList) {
			final int femaleGid = Integer.parseInt(importedCross.getFemaleGid());
			final String[] femaleInfo = pedigreeMap.get(femaleGid);
			importedCross.getFemaleParent().setPedigree(femaleInfo[0]);
			importedCross.getFemaleParent().setCross(femaleInfo[1]);

			for (final ImportedGermplasmParent maleParent : importedCross.getMaleParents()) {
				final int maleGid = maleParent.getGid();
				maleParent.setPedigree(pedigreeMap.get(maleGid)[0]);
				maleParent.setCross(pedigreeMap.get(maleGid)[1]);
			}
		}

	}

}
