package com.efficio.etl.web.controller.angular;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.service.impl.ETLServiceImpl;
import com.efficio.etl.web.AbstractBaseETLController;
import com.efficio.etl.web.bean.ConsolidatedStepForm;
import com.efficio.etl.web.bean.RowDTO;
import com.efficio.etl.web.bean.SheetDTO;
import com.efficio.etl.web.bean.StudyDetailsForm;
import com.efficio.etl.web.bean.UserSelection;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.util.StudyPermissionValidator;
import org.generationcp.middleware.domain.dms.DatasetTypeDTO;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.etl.Constants;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping(AngularSelectSheetController.URL)
public class AngularSelectSheetController extends AbstractBaseETLController {

	private static final Logger LOG = LoggerFactory.getLogger(AngularSelectSheetController.class);
	public static final String URL = "/etl/workbook/step2";

	private static final int ROW_COUNT_PER_SCREEN = 10;
	private static final int MAX_DISPLAY_CHARACTER_PER_ROW = 60;
	private static final int FIELDBOOK_DEFAULT_STUDY_ID = 1;

	private static final SimpleDateFormat DATE_PICKER_FORMAT = DateUtil
		.getSimpleDateFormat(DateUtil.FRONTEND_DATE_FORMAT_2);
	private static final SimpleDateFormat DB_FORMAT = DateUtil.getSimpleDateFormat(DateUtil.DATE_AS_NUMBER_FORMAT);
	private static final String ADD_TO_NEW_STUDY = "add.to.new.study";
	private static final String DESCRIPTION = "Description";
	private static final String OBSERVATION = "Observation";

	@Resource
	private ETLService etlService;

	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Resource(name = "etlUserSelection")
	private UserSelection userSelection;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	private StudyDataManager studyDataManager;

	@Resource
	private StudyPermissionValidator studyPermissionValidator;

	@Resource
	private DataImportService dataImportService;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private DatasetTypeService datasetTypeService;

	@Override
	public String getContentName() {
		return "etl/angular/angularSelectSheet";
	}

	@Override
	public UserSelection getUserSelection() {
		return this.userSelection;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String show(final Model model, final HttpServletRequest request) {

		model.addAttribute("displayedRows", AngularSelectSheetController.ROW_COUNT_PER_SCREEN);
		final List<StudyDetails> previousStudies = this.getPreviousStudies(model);

		try {
			for (final StudyDetails previousStudy : previousStudies) {
				if (!StringUtils.isEmpty(previousStudy.getStartDate())) {
					final Date date = AngularSelectSheetController.DB_FORMAT.parse(previousStudy.getStartDate());
					previousStudy.setStartDate(AngularSelectSheetController.DATE_PICKER_FORMAT.format(date));
				}

				if (!StringUtils.isEmpty(previousStudy.getEndDate())) {
					final Date date = AngularSelectSheetController.DB_FORMAT.parse(previousStudy.getEndDate());
					previousStudy.setEndDate(AngularSelectSheetController.DATE_PICKER_FORMAT.format(date));
				}
			}

		} catch (final ParseException e) {
			AngularSelectSheetController.LOG.error(e.getMessage(), e);
		}

		if ((this.userSelection.getStudyId() == null || this.userSelection.getStudyId() == 0
			|| this.userSelection.getStudyId() == AngularSelectSheetController.FIELDBOOK_DEFAULT_STUDY_ID)
			&& !StringUtils.isEmpty(this.userSelection.getStudyName())) {
			this.addStudyDetails(previousStudies);
		} else {
			try {
				if (!this.populateStudyDetailsIfFieldbookFormat(previousStudies, model)) {
					final StudyDetails newStudy = new StudyDetails();
					newStudy.setId(0);
					newStudy.setLabel(
						this.etlService.convertMessage(new Message(AngularSelectSheetController.ADD_TO_NEW_STUDY)));
					previousStudies.add(newStudy);
				}
			} catch (final IOException e) {
				AngularSelectSheetController.LOG.error(e.getMessage(), e);
			}
		}

		model.addAttribute("previousStudies", previousStudies);

		// reset mapped headers
		this.userSelection.clearMeasurementVariables();

		return super.show(model);
	}

	private void addStudyDetails(final List<StudyDetails> previousStudies) {
		final StudyDetails details = new StudyDetails();
		details.setId(this.userSelection.getStudyId() != null ? this.userSelection.getStudyId() : 0);
		details.setStudyName(this.userSelection.getStudyName());
		details.setDescription(this.userSelection.getStudyDescription());
		details.setStartDate(ETLServiceImpl.formatDate(this.userSelection.getStudyStartDate()));
		details.setEndDate(ETLServiceImpl.formatDate(this.userSelection.getStudyEndDate()));
		details.setStudyUpdate(ETLServiceImpl.formatDate(this.userSelection.getStudyUpdate()));
		details.setObjective(this.userSelection.getStudyObjective());
		details.setEndDate(this.userSelection.getStudyEndDate());
		details.setStartDate(this.userSelection.getStudyStartDate());
		details.setStudyType(this.studyDataManager.getStudyTypeByName(this.userSelection.getStudyType()));
		details.setLabel(this.etlService.convertMessage(new Message(AngularSelectSheetController.ADD_TO_NEW_STUDY)));
		previousStudies.add(details);
	}

	private boolean populateStudyDetailsIfFieldbookFormat(final List<StudyDetails> previousStudies, final Model model)
		throws IOException {
		boolean addedNewStudy = false;
		// check if fieldbook format by checking 1st sheet
		boolean inFieldbookFormat = false;
		final Workbook wb = this.etlService.retrieveCurrentWorkbook(this.userSelection);
		if (wb.getNumberOfSheets() > 1) {
			final Sheet sheet1 = wb.getSheetAt(ETLServiceImpl.DESCRIPTION_SHEET);
			final Sheet sheet2 = wb.getSheetAt(ETLServiceImpl.OBSERVATION_SHEET);
			if (sheet1 != null && AngularSelectSheetController.DESCRIPTION.equalsIgnoreCase(sheet1.getSheetName()) && sheet2 != null
				&& AngularSelectSheetController.OBSERVATION.equalsIgnoreCase(sheet2.getSheetName())) {
				inFieldbookFormat = true;
			}
			if (inFieldbookFormat) {
				this.userSelection.setSelectedSheet(ETLServiceImpl.OBSERVATION_SHEET);
				this.userSelection.setHeaderRowIndex(0);
				final List<String> fileHeaders = this.etlService.retrieveColumnHeaders(wb, this.userSelection,
					Boolean.FALSE);
				if (fileHeaders != null) {
					this.userSelection.setHeaderRowDisplayText(StringUtils.join(fileHeaders, ','));
				}
				StudyDetails studyDetails = this.etlService.readStudyDetails(sheet1);
				if (studyDetails != null) {
					final String studyName = studyDetails.getStudyName();
					StudyDetails previousStudy = null;
					// check if study name already exists
					if (studyName != null) {
						for (final StudyDetails s : previousStudies) {
							if (studyName.equals(s.getStudyName())) {
								previousStudy = s;
							}
						}
					}
					if (previousStudy == null) {
						// set a temporary study id - 0 must not be used as it
						// is used for user-defined study name
						studyDetails.setId(AngularSelectSheetController.FIELDBOOK_DEFAULT_STUDY_ID);
						studyDetails.setLabel(this.etlService
							.convertMessage(new Message(AngularSelectSheetController.ADD_TO_NEW_STUDY)));
						// format dates
						final String oldFormat = "yyyyMMdd";
						final String newFormat = "MM/dd/yyyy";
						studyDetails.setStartDate(
							ETLServiceImpl.formatDate(studyDetails.getStartDate(), oldFormat, newFormat));
						studyDetails
							.setEndDate(ETLServiceImpl.formatDate(studyDetails.getEndDate(), oldFormat, newFormat));
						// add study to list
						previousStudies.add(studyDetails);
						addedNewStudy = true;
					} else {
						studyDetails = previousStudy;
					}
					if (StringUtils.isEmpty(this.userSelection.getStudyName())) {
						// update user selection
						this.userSelection.setStudyId(studyDetails.getId());
						this.userSelection.setStudyName(studyName);
						this.userSelection.setStudyDescription(studyDetails.getDescription());
						this.userSelection.setStudyObjective(studyDetails.getObjective());
						this.userSelection.setStudyStartDate(studyDetails.getStartDate());
						this.userSelection.setStudyEndDate(studyDetails.getEndDate());
						this.userSelection.setStudyUpdate(studyDetails.getStudyUpdate());
						this.userSelection.setStudyType(
							studyDetails.getStudyType() == null ? "" : studyDetails.getStudyType().getName());
						// update form
						final ConsolidatedStepForm form = this.getSelectRowsForm();
						model.addAttribute("form", form);
					}
				}
			}
		} else {
			this.userSelection.setSelectedSheet(0);
			// update form
			final ConsolidatedStepForm form = this.getSelectRowsForm();
			model.addAttribute("form", form);
		}

		return addedNewStudy;
	}

	// added support for parameterized sheet index
	@ResponseBody
	@RequestMapping(value = "/displayRow", params = "list=true")
	public List<RowDTO> getUpdatedRowDisplayHTML(
		@RequestParam(value = "lastRowIndex") final Integer lastRowIndex,
		@RequestParam(value = "startRowIndex", required = false) final Integer startRow,
		@RequestParam(value = "selectedSheetIndex") final Integer selectedSheetIndex) {

		try {
			final Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);

			Integer finalStartRow = startRow;
			if (startRow == null) {
				finalStartRow = 0;
			}

			final int count = this.etlService.getAvailableRowsForDisplay(workbook, selectedSheetIndex);

			Integer finalLastRowIndex = lastRowIndex;
			if (lastRowIndex > count) {
				finalLastRowIndex = count;
			}

			return this.etlService.retrieveRowInformation(workbook, selectedSheetIndex, finalStartRow,
				finalLastRowIndex, AngularSelectSheetController.MAX_DISPLAY_CHARACTER_PER_ROW);

		} catch (final IOException e) {
			AngularSelectSheetController.LOG.error(e.getMessage(), e);
			return new ArrayList<>();
		}
	}

	// changed row count implem to have parameterized selected sheet index
	@ResponseBody
	@RequestMapping(value = "/displayRow", params = "count=true")
	public Map<String, Object> getMaximumRowDisplayCount(
		@RequestParam(value = "selectedSheetIndex") final Integer selectedSheetIndex) {
		final Map<String, Object> returnValue = new HashMap<>();
		try {
			final Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);
			final Integer count = this.etlService.getAvailableRowsForDisplay(workbook, selectedSheetIndex);
			returnValue.put("value", count);
			returnValue.put("status", "ok");
		} catch (final IOException e) {
			AngularSelectSheetController.LOG.error(e.getMessage(), e);
			returnValue.put("status", "error");
		}

		return returnValue;
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	public Map<String, Object> processForm(
		@RequestBody final ConsolidatedStepForm form,
		final HttpServletRequest request) throws IOException, WorkbookParserException {
		final List<Message> messageList = this.validate(form);

		if (!messageList.isEmpty()) {
			return this.wrapFormResult(this.etlService.convertMessageList(messageList));
		}
		// transfer form data to user selection object
		this.userSelection.setSelectedSheet(form.getSelectedSheetIndex());
		this.userSelection.setHeaderRowIndex(form.getHeaderRowIndex());
		this.userSelection.setHeaderRowDisplayText(form.getHeaderRowDisplayText());
		this.userSelection.setStudyName(form.getStudyDetails().getStudyName());
		this.userSelection.setStudyDescription(form.getStudyDetails().getStudyDescription());
		this.userSelection.setStudyObjective(form.getStudyDetails().getObjective());
		this.userSelection.setStudyStartDate(form.getStudyDetails().getStartDate());
		this.userSelection.setStudyEndDate(form.getStudyDetails().getEndDate());
		this.userSelection.setStudyType(form.getStudyDetails().getStudyType());
		this.userSelection.setDatasetType(form.getDatasetType());
		// routing logic for existing study vs new study details
		final Integer studyId = form.getStudyDetails().getStudyId();
		this.userSelection.setStudyId(studyId);
		if (studyId != null && studyId != 0) {
			final List<String> errors = new ArrayList<>();
			Map<String, List<Message>> mismatchErrors = null;
			final boolean isMeansDataImport = this.userSelection.getDatasetType() != null
				&& this.userSelection.getDatasetType() == DatasetTypeEnum.MEANS_DATA.getId();

			try {
				// check if the selected dataset still has no mapped headers
				if (isMeansDataImport) {
					if (!this.etlService.hasMeansDataset(studyId)) {
						return this.wrapFormResult(AngularMapOntologyController.URL, request);
					}
				} else {
					if (!this.etlService.hasMeasurementEffectDataset(studyId)) {
						return this.wrapFormResult(AngularMapOntologyController.URL, request);
					}
				}

				mismatchErrors = this.checkForMismatchedHeaders(errors, mismatchErrors, isMeansDataImport);

				if (mismatchErrors != null && !mismatchErrors.isEmpty()) {
					for (final Map.Entry<String, List<Message>> entry : mismatchErrors.entrySet()) {
						errors.addAll(this.etlService.convertMessageList(entry.getValue()));
					}
					return this.wrapFormResult(errors);
				} else {
					return this.wrapFormResult(AngularOpenSheetController.URL, request);
				}

			} catch (final Exception e) {
				AngularSelectSheetController.LOG.error(e.getMessage(), e);
				final List<Message> error = new ArrayList<>();
				error.add(new Message(Constants.MESSAGE_KEY_GENERIC_ERROR));
				errors.addAll(this.etlService.convertMessageList(error));
				return this.wrapFormResult(errors);
			}
		} else {
			return this.wrapFormResult(AngularMapOntologyController.URL, request);
		}

	}

	List<Message> validate(final ConsolidatedStepForm form) throws IOException, WorkbookParserException {
		final List<Message> messages = this.validateFormInput(form);
		if (!messages.isEmpty()) {
			return messages;
		}
		return this.validateConditions();
	}

	List<Message> validateConditions() throws IOException, WorkbookParserException {
		final List<Message> messageList = new ArrayList<>();
		final Workbook wb = this.etlService.retrieveCurrentWorkbook(this.userSelection);
		if (wb.getNumberOfSheets() > 1) {
			final Sheet sheet1 = wb.getSheetAt(ETLServiceImpl.DESCRIPTION_SHEET);
			if (AngularSelectSheetController.DESCRIPTION.equalsIgnoreCase(sheet1.getSheetName())) {
				final org.generationcp.middleware.domain.etl.Workbook referenceWorkbook = this.dataImportService
					.parseWorkbookDescriptionSheet(wb, this.contextUtil.getCurrentWorkbenchUserId());

				final List<String> variablesWithWrongPSM = new ArrayList<>();
				for (final MeasurementVariable measurementVariable : referenceWorkbook.getConditions()) {
					final Set<StandardVariable> standardVariables = this.ontologyDataManager.findStandardVariablesByNameOrSynonym(
						measurementVariable.getName(),
						this.contextUtil.getCurrentProgramUUID());
					if (!CollectionUtils.isEmpty(standardVariables) && this.variableHasWrongPSM(measurementVariable, standardVariables)) {
						variablesWithWrongPSM.add(measurementVariable.getName());
					}
				}
				if (!variablesWithWrongPSM.isEmpty()) {
					messageList.add(new Message("error.variable.wrong.psm", StringUtils.join(variablesWithWrongPSM, ", ")));
				}
			}
		}
		return messageList;
	}

	private boolean variableHasWrongPSM(final MeasurementVariable measurementVariable, final Set<StandardVariable> standardVariables) {
		//standardVariables set contains only one element.
		for (final StandardVariable variable : standardVariables) {
			return (!variable.getProperty().getName().equalsIgnoreCase(measurementVariable.getProperty())
				|| !variable.getMethod().getName().equalsIgnoreCase(measurementVariable.getMethod())
				|| !variable.getScale().getName().equalsIgnoreCase(measurementVariable.getScale()));
		}
		return false;
	}

	List<Message> validateFormInput(final ConsolidatedStepForm form) {
		final String startDateString = form.getStudyDetails().getStartDate();
		final String endDateString = form.getStudyDetails().getEndDate();
		Date startDate = null;
		final Date endDate;
		final List<Message> messageList = new ArrayList<>();

		try {
			if (!StringUtils.isEmpty(startDateString)) {
				// check if date is later than current date
				startDate = AngularSelectSheetController.DATE_PICKER_FORMAT.parse(startDateString);
				if (startDate.after(new Date())) {
					messageList.add(new Message("error.start.is.after.current.date"));
				}
			}

			if (!StringUtils.isEmpty(endDateString)) {
				endDate = AngularSelectSheetController.DATE_PICKER_FORMAT.parse(endDateString);

				if (startDate == null) {
					messageList.add(new Message("error.date.startdate.required"));
				} else if (endDate.before(startDate)) {
					messageList.add(new Message("error.date.enddate.invalid"));
				}

			}

		} catch (final ParseException e) {
			AngularSelectSheetController.LOG.error(e.getMessage(), e);
		}
		return messageList;
	}

	private Map<String, List<Message>> checkForMismatchedHeaders(
		final List<String> errors,
		final Map<String, List<Message>> mismatchErrors, final boolean isMeansDataImport) {
		try {
			// TODO : refactor validation logic to avoid duplication with
			// ImportObservationsController
			final Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);
			final org.generationcp.middleware.domain.etl.Workbook importData = this.etlService
				.retrieveAndSetProjectOntology(this.userSelection, isMeansDataImport);

			final List<MeasurementVariable> studyHeaders = importData.getFactors();

			final List<String> fileHeaders = this.etlService.retrieveColumnHeaders(workbook, this.userSelection,
				this.etlService.headersContainsObsUnitId(importData));

			return this.etlService.checkForMismatchedHeaders(fileHeaders, studyHeaders, isMeansDataImport);
		} catch (final Exception e) {
			AngularSelectSheetController.LOG.error(e.getMessage(), e);
			final List<Message> error = new ArrayList<>();
			error.add(new Message(Constants.MESSAGE_KEY_GENERIC_ERROR));
			errors.addAll(this.etlService.convertMessageList(error));
		}
		return mismatchErrors;
	}

	@ModelAttribute("form")
	public ConsolidatedStepForm getSelectRowsForm() {
		final ConsolidatedStepForm consolidatedForm = new ConsolidatedStepForm();
		consolidatedForm.setSelectedSheetIndex(this.userSelection.getSelectedSheet());

		consolidatedForm.setHeaderRowIndex(this.userSelection.getHeaderRowIndex());
		consolidatedForm.setHeaderRowDisplayText(this.userSelection.getHeaderRowDisplayText());
		consolidatedForm.setDatasetType(this.userSelection.getDatasetType() != null
			? this.userSelection.getDatasetType() : DatasetTypeEnum.PLOT_DATA.getId());

		final StudyDetailsForm studyDetailsForm = new StudyDetailsForm();
		studyDetailsForm.setStudyName(this.userSelection.getStudyName());
		studyDetailsForm.setStudyDescription(this.userSelection.getStudyDescription());
		studyDetailsForm.setObjective(this.userSelection.getStudyObjective());
		studyDetailsForm.setStartDate(this.userSelection.getStudyStartDate());
		studyDetailsForm.setEndDate(this.userSelection.getStudyEndDate());
		studyDetailsForm.setStudyType(this.userSelection.getStudyType());
		studyDetailsForm.setStudyId(this.userSelection.getStudyId());
		studyDetailsForm
			.setStudyType(this.userSelection.getStudyType() == null ? "" : this.userSelection.getStudyType());

		consolidatedForm.setStudyDetails(studyDetailsForm);

		return consolidatedForm;
	}

	@ModelAttribute("sheetList")
	public List<SheetDTO> getSheets() {
		try {
			final Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);

			return this.etlService.retrieveSheetInformation(workbook);

		} catch (final IOException e) {
			AngularSelectSheetController.LOG.error(e.getMessage(), e);
		}

		return new ArrayList<>();
	}

	@ModelAttribute("studyTypeList")
	public Map<String, String> getStudyTypes() {

		final Map<String, String> studyTypes = new HashMap<>();

		for (final StudyTypeDto type : this.studyDataManager.getAllVisibleStudyTypes()) {
			studyTypes.put(type.getName(), type.getLabel());
		}

		return studyTypes;
	}

	@ModelAttribute("datasetTypeList")
	public Map<Integer, String> getDatasetTypes() {

		final DatasetTypeDTO plotDatasetType = this.datasetTypeService.getDatasetTypeById(DatasetTypeEnum.PLOT_DATA.getId());
		final DatasetTypeDTO meansDatasetType = this.datasetTypeService.getDatasetTypeById(DatasetTypeEnum.MEANS_DATA.getId());

		final Map<Integer, String> datasetTypes = new HashMap<>();
		datasetTypes.put(DatasetTypeEnum.PLOT_DATA.getId(), plotDatasetType.getDescription());
		datasetTypes.put(DatasetTypeEnum.MEANS_DATA.getId(), meansDatasetType.getDescription());
		return datasetTypes;
	}

	List<StudyDetails> getPreviousStudies(final Model model) {
		final List<String> restrictedStudies = new ArrayList<>();
		final List<StudyDetails> finalStudies = new ArrayList<>();
		final List<StudyDetails> existingStudies = this.etlService.retrieveExistingStudyDetails(this.contextUtil.getCurrentProgramUUID());
		for (final StudyDetails study : existingStudies) {
			final StudyReference reference = new StudyReference(study.getId(), study.getStudyName());
			reference.setIsLocked(study.getIsLocked());
			final String createdBy = study.getCreatedBy();
			if (createdBy != null) {
				reference.setOwnerId(Integer.valueOf(createdBy));
			}
			if (!this.studyPermissionValidator.userLacksPermissionForStudy(reference)) {
				finalStudies.add(study);
			} else {
				restrictedStudies.add(study.getStudyName());
			}
		}
		model.addAttribute("restrictedStudies", restrictedStudies);
		return finalStudies;
	}
}
