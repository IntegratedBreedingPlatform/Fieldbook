package com.efficio.etl.web.controller.angular;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.web.AbstractBaseETLController;
import com.efficio.etl.web.bean.FileUploadForm;
import com.efficio.etl.web.bean.UserSelection;
import com.efficio.etl.web.bean.VariableDTO;
import com.efficio.etl.web.validators.FileUploadFormValidator;
import com.efficio.fieldbook.service.api.FieldbookService;
import com.google.common.base.Optional;
import org.apache.poi.ss.usermodel.Workbook;
import org.fest.util.Collections;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.WorkbenchAppPathResolver;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.Constants;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.util.Message;
import org.generationcp.middleware.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */

@Controller
@RequestMapping(AngularMapOntologyController.URL)
public class AngularMapOntologyController extends AbstractBaseETLController {

	public static final String URL = "/etl/workbook/mapOntology";
	private static final Logger LOG = LoggerFactory.getLogger(AngularMapOntologyController.class);
	static final String ERROR_HEADER_NO_MAPPING = "error.header.no.mapping";
	static final String ERROR_DUPLICATE_LOCAL_VARIABLE = "error.duplicate.local.variable";
	static final String ERROR_LOCATION_ID_DOESNT_EXISTS = "error.location.id.doesnt.exists";
	static final String INVALID_MEANS_IMPORT_VARIABLE = "error.invalid.means.import.variable";
	private static final List<Integer> INVALID_VARIABLES_FOR_MEANS_IMPORT = Arrays.asList(TermId.ENTRY_TYPE.getId(),
		TermId.REP_NO.getId(), TermId.PLOT_NO.getId());

	@Resource
	private FieldbookService fieldbookService;

	@Resource(name = "etlUserSelection")
	private UserSelection userSelection;

	@Resource
	private ETLService etlService;

	@Resource
	private DataImportService dataImportService;

	@Resource
	private ContextUtil contextUtil;

	@Override
	public String getContentName() {
		return "etl/angular/angularMapHeaders";
	}

	@Override
	public UserSelection getUserSelection() {
		return this.userSelection;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String show(final Model model, final HttpServletRequest request) {

		try {
			final Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);
			final List<String> headers = this.etlService.retrieveColumnHeaders(workbook, this.userSelection, Boolean.FALSE);

			final Map<PhenotypicType, List<VariableDTO>> headerMap =
					this.etlService.prepareInitialCategorization(headers, this.userSelection);

			// null key is used to store variabledtos not initially mapped to
			// any standard variable (and thus, any phenotypic type)
			model.addAttribute("headerList", headerMap.get(null));
			model.addAttribute("trialEnvironmentList", headerMap.get(PhenotypicType.TRIAL_ENVIRONMENT));
			model.addAttribute("trialDesignList", headerMap.get(PhenotypicType.TRIAL_DESIGN));
			model.addAttribute("variateList", headerMap.get(PhenotypicType.VARIATE));
			model.addAttribute("germplasmList", headerMap.get(PhenotypicType.GERMPLASM));
			model.addAttribute("entryDetailList", headerMap.get(PhenotypicType.ENTRY_DETAIL));
			model.addAttribute("datasetType", this.userSelection.getDatasetType());

			model.addAttribute("fieldbookWebLink",
					WorkbenchAppPathResolver.getFullWebAddress(this.etlService.getFieldbookWebTool().getPath()));

			final Map<String, List<Integer>> tmp = new HashMap<>();
			for (final PhenotypicType type : PhenotypicType.values()) {
				tmp.put(type.toString(), type.getTypeStorages());
			}
			model.addAttribute("roleList", tmp);

			// total header count is populated as a function of get header list
			model.addAttribute("totalHeaderCount", headers.size());

			final int allocatedCount = this.getTotalAllocatedCount(headerMap);
			model.addAttribute("allocatedCount", allocatedCount);
		} catch (final IOException e) {
			AngularMapOntologyController.LOG.error(e.getMessage(), e);
		}

		return super.show(model, true, request);
	}

	private int getTotalAllocatedCount(final Map<PhenotypicType, List<VariableDTO>> headerMap) {
		int allocatedCount = 0;

		allocatedCount += this.getAllocatedCount(headerMap, PhenotypicType.TRIAL_ENVIRONMENT);
		allocatedCount += this.getAllocatedCount(headerMap, PhenotypicType.TRIAL_DESIGN);
		allocatedCount += this.getAllocatedCount(headerMap, PhenotypicType.VARIATE);
		allocatedCount += this.getAllocatedCount(headerMap, PhenotypicType.GERMPLASM);
		allocatedCount += this.getAllocatedCount(headerMap, PhenotypicType.ENTRY_DETAIL);

		return allocatedCount;
	}

	private int getAllocatedCount(final Map<PhenotypicType, List<VariableDTO>> headerMap, final PhenotypicType type) {
		final List<VariableDTO> variableDTOList = headerMap.get(type);

		if (variableDTOList == null) {
			return 0;
		} else {
			return variableDTOList.size();
		}
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	public Map<String, List<String>> processImport(@RequestBody final VariableDTO[] variables) {

		try {

			this.userSelection.clearMeasurementVariables();

			this.etlService.mergeVariableData(variables, this.userSelection, true);

			final org.generationcp.middleware.domain.etl.Workbook importData = this.etlService.convertToWorkbook(this.userSelection);

			final Map<String, List<Message>> messages = this.etlService.validateProjectOntology(importData);
			final Map<String, List<String>> proxy = new HashMap<>();

			final Set<String> vars = new HashSet<>();
			final boolean isMeansDataImport = this.userSelection.getDatasetType() != null &&
				this.userSelection.getDatasetType() == DatasetTypeEnum.MEANS_DATA.getId();
			for (final VariableDTO variable : variables) {
				final Message message = new Message("");
				message.setMessageParams(new String[] {variable.getHeaderName()});
				final List<Message> messageList = new ArrayList<>();
				messageList.add(message);
				if (variable.getId() == null) {
					message.setMessageKey(ERROR_HEADER_NO_MAPPING);
					proxy.put(variable.getHeaderName(), this.etlService.convertMessageList(messageList));
				}
				if (!vars.add(variable.getHeaderName())) {// duplicate
					message.setMessageKey(ERROR_DUPLICATE_LOCAL_VARIABLE);
					proxy.put(variable.getHeaderName() + ":" + variable.getId(), this.etlService.convertMessageList(messageList));
				}
				if(isMeansDataImport && INVALID_VARIABLES_FOR_MEANS_IMPORT.contains(variable.getId())) {
					message.setMessageKey(INVALID_MEANS_IMPORT_VARIABLE);
					proxy.put(variable.getHeaderName() + ":" + variable.getId(), this.etlService.convertMessageList(messageList));
				}
			}

			// If Location Name variable is present in the imported file, then the Location ID variable is required.
			if (this.checkIfLocationIdVariableExists(importData)) {
				final Message message = new Message(ERROR_LOCATION_ID_DOESNT_EXISTS);
				final List<Message> messageList = new ArrayList<>();
				messageList.add(message);
				proxy.put("", this.etlService.convertMessageList(messageList));
			}

			// Validate Trial Instance Value
			if (!Collections.isEmpty(importData.getConditions())) {
				this.validateTrialInstanceValue(importData.getConditions(), messages);
			}

			if (messages != null) {
				for (final Map.Entry<String, List<Message>> entry : messages.entrySet()) {
					proxy.put(entry.getKey(), this.etlService.convertMessageList(entry.getValue()));
				}
			}

			return proxy;

		} catch (final Exception e) {
			AngularMapOntologyController.LOG.error(e.getMessage(), e);
			final Map<String, List<String>> errorMap = new HashMap<>();
			final List<Message> error = new ArrayList<>();
			error.add(new Message(Constants.MESSAGE_KEY_GENERIC_ERROR));
			errorMap.put(Constants.GLOBAL, this.etlService.convertMessageList(error));
			return errorMap;
		}
	}

	boolean checkIfLocationIdVariableExists(final org.generationcp.middleware.domain.etl.Workbook importData)
			throws IOException, WorkbookParserException {

		final org.generationcp.middleware.domain.etl.Workbook referenceWorkbook = this.dataImportService
				.parseWorkbookDescriptionSheet(this.etlService.retrieveCurrentWorkbook(this.userSelection),
						this.contextUtil.getCurrentWorkbenchUserId());
		importData.setConstants(referenceWorkbook.getConstants());
		importData.setConditions(referenceWorkbook.getConditions());

		final List<MeasurementVariable> measurementVariables = new ArrayList<>();

		// The location name/id can be found on either conditions
		// or factors section of the fieldbook file if the study is Trial
		measurementVariables.addAll(importData.getConditions());
		measurementVariables.addAll(importData.getFactors());

		final boolean isLocationIDVariableExists =
			this.dataImportService.findMeasurementVariableByTermId(TermId.LOCATION_ID.getId(), measurementVariables).isPresent();
		final boolean isLocationNameVariableExists =
			this.dataImportService.findMeasurementVariableByTermId(TermId.TRIAL_LOCATION.getId(), measurementVariables).isPresent();

		// If Location Name variable is present in the imported file, then the Location ID variable is required.
		return isLocationNameVariableExists && !isLocationIDVariableExists;

	}

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public String uploadFile(@ModelAttribute("uploadForm") final FileUploadForm uploadForm, final BindingResult result, final Model model) {
		final FileUploadFormValidator validator = new FileUploadFormValidator();
		validator.validate(uploadForm, result);

		if (result.hasErrors()) {
			return this.getContentName();
		} else {
			try {
				final String tempFileName = this.etlService.storeUserWorkbook(uploadForm.getFile().getInputStream());
				this.userSelection.setServerFileName(tempFileName);
				this.userSelection.setActualFileName(uploadForm.getFile().getOriginalFilename());
			} catch (final IOException e) {
				AngularMapOntologyController.LOG.error(e.getMessage(), e);
				result.reject("uploadForm.file", "Error occurred while uploading file.");
			}
			return "redirect:" + AngularMapOntologyController.URL;
		}
	}

	@ResponseBody
	@RequestMapping(value = "/confirm/{maintainHeaderMapping}", method = RequestMethod.POST)
	public Map<String, Object> confirmImport(@RequestBody final VariableDTO[] variables, @PathVariable final boolean maintainHeaderMapping, final HttpSession session,
			final HttpServletRequest request) {

		try {

			this.userSelection.clearMeasurementVariables();

			final Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);
			this.etlService.mergeVariableData(variables, this.userSelection, maintainHeaderMapping);
			final org.generationcp.middleware.domain.etl.Workbook importData = this.etlService.convertToWorkbook(this.userSelection);

			final org.generationcp.middleware.domain.etl.Workbook referenceWorkbook = this.dataImportService
					.parseWorkbookDescriptionSheet(this.etlService.retrieveCurrentWorkbook(this.userSelection),
							this.contextUtil.getCurrentWorkbenchUserId());
			importData.setConstants(referenceWorkbook.getConstants());
			importData.setConditions(referenceWorkbook.getConditions());

			this.dataImportService.addLocationIDVariableIfNotExists(importData, importData.getFactors(), this.contextUtil.getCurrentProgramUUID());
			this.dataImportService.addEntryTypeVariableIfNotExists(importData, this.contextUtil.getCurrentProgramUUID());
			this.processExperimentalDesign(importData, workbook);
			this.dataImportService.assignLocationIdVariableToEnvironmentDetailSection(importData);
			this.dataImportService.removeLocationNameVariableIfExists(importData);
			this.fieldbookService.addStudyUUIDConditionAndObsUnitIDFactorToWorkbook(importData, false);

			this.etlService.saveProjectOntology(importData, this.contextUtil.getCurrentProgramUUID());
			this.userSelection.setStudyId(importData.getStudyDetails().getId());
			this.userSelection.setTrialDatasetId(importData.getTrialDatasetId());
			this.userSelection.setMeasurementDatasetId(importData.getMeasurementDatesetId());
			this.userSelection.setMeansDatasetId(importData.getMeansDatasetId());

			return this.wrapFormResult(AngularOpenSheetController.URL, request);

		} catch (final WorkbookParserException e) {
			AngularMapOntologyController.LOG.error(e.getMessage(), e);
			return this.wrapFormResult(Arrays.asList(e.getMessage()));
		} catch (final Exception e) {
			AngularMapOntologyController.LOG.error(e.getMessage(), e);
			final List<Message> error = new ArrayList<>();
			error.add(new Message(Constants.MESSAGE_KEY_GENERIC_ERROR));
			final List<String> errorMessages = this.etlService.convertMessageList(error);
			return this.wrapFormResult(errorMessages);
		}

	}

	void processExperimentalDesign(final org.generationcp.middleware.domain.etl.Workbook importData, final Workbook workbook)  throws WorkbookParserException{
		final List<String> headers = this.etlService.retrieveColumnHeaders(workbook, this.userSelection, false);
		final List<MeasurementVariable> variableList = importData.getFactors();
		int exptDesignColumnIndex = -1;
		for (final MeasurementVariable measurementVariable : variableList) {
			if(TermId.EXPERIMENT_DESIGN_FACTOR.getId() == measurementVariable.getTermId()) {
				 exptDesignColumnIndex = headers.indexOf(measurementVariable.getName());
			}
		}
		final String valueFromObsevations = this.etlService.getExperimentalDesignValueFromObservationSheet(workbook, this.userSelection, exptDesignColumnIndex);
		this.dataImportService.processExperimentalDesign(importData, this.contextUtil.getCurrentProgramUUID(), valueFromObsevations);
	}

	@ModelAttribute("uploadForm")
	public FileUploadForm getForm() {
		return new FileUploadForm();
	}

	public void validateTrialInstanceValue(final List<MeasurementVariable> conditions, final Map<String, List<Message>> errorMessages) {
		for (final MeasurementVariable varCondition : conditions) {
			if (varCondition.getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
				final Optional<Message> message = Util.validateVariableValues(varCondition, varCondition.getValue());
				if (message.isPresent()) {
					errorMessages.putIfAbsent(Constants.INVALID_TRIAL, new ArrayList<>());
					errorMessages.get(Constants.INVALID_TRIAL).add(message.get());
				}
			}

		}
	}
}
