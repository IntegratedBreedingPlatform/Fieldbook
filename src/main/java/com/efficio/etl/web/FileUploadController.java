
package com.efficio.etl.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.HTTPSessionUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.operation.parser.WorkbookParser;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.web.bean.FileUploadForm;
import com.efficio.etl.web.bean.UserSelection;
import com.efficio.etl.web.validators.FileUploadFormValidator;
import com.efficio.fieldbook.service.api.FieldbookService;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */

@Controller
@RequestMapping({ "/etl", FileUploadController.URL })
public class FileUploadController extends AbstractBaseETLController {

	public static final String URL = "/etl/fileUpload";

	private static final Logger LOG = LoggerFactory.getLogger(FileUploadController.class);

	protected static final String STATUS_CODE = "statusCode";

	protected static final String ERROR_TYPE = "errorType";

	protected static final String STATUS_MESSAGE = "statusMessage";

	private static final String UPLOAD_FORM_FILE = "uploadForm.file";
	public static final String STATUS_CODE_HAS_OUT_OF_BOUNDS = "2";
	public static final String STATUS_CODE_SUCCESSFUL = "1";
	public static final String STATUS_CODE_HAS_ERROR = "-1";

	@Resource
	private FieldbookService fieldbookService;

	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Resource
	private ETLService etlService;

	@Resource(name = "etlUserSelection")
	private UserSelection userSelection;

	@Resource
	private HTTPSessionUtil httpSessionUtil;

	@Resource
	private DataImportService dataImportService;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private ResourceBundleMessageSource messageSource;

	@Resource
	private ContextUtil contextUtil;

	private final Map<String, String> returnMessage = new HashMap<>();

	@RequestMapping(method = RequestMethod.GET)
	public String show(@ModelAttribute("uploadForm") final FileUploadForm uploadForm, final Model model,
			final HttpSession session) {
		this.httpSessionUtil.clearSessionData(session, new String[] { HTTPSessionUtil.USER_SELECTION_SESSION_NAME });

		return super.show(model);
	}

	@RequestMapping(method = RequestMethod.POST)
	public String uploadFile(@ModelAttribute("uploadForm") final FileUploadForm uploadForm, final BindingResult result,
			final Model model) {
		final FileUploadFormValidator validator = new FileUploadFormValidator();
		validator.validate(uploadForm, result);

		if (result.hasErrors()) {
			/**
			 * Return the user back to form to show errors
			 */
			return this.getContentName();
		} else {

			try {
				final String tempFileName = this.etlService.storeUserWorkbook(uploadForm.getFile().getInputStream());
				this.userSelection.setServerFileName(tempFileName);
				this.userSelection.setActualFileName(uploadForm.getFile().getOriginalFilename());
			} catch (final IOException e) {
				FileUploadController.LOG.error(e.getMessage(), e);
				result.reject(FileUploadController.UPLOAD_FORM_FILE, "Error occurred while uploading file.");
			}

			if ("fieldbook".equalsIgnoreCase(uploadForm.getImportType())) {
				model.addAttribute("fileName", this.userSelection.getActualFileName());
				return "etl/fileUploadFieldbook";

			} else {
				try {
					this.etlService.retrieveCurrentWorkbookWithValidation(this.userSelection);
					return "redirect:workbook/step2";
				} catch (final IOException e) {
					FileUploadController.LOG.error(e.getMessage(), e);
					result.reject(FileUploadController.UPLOAD_FORM_FILE, "Error occurred while reading Excel file");
				} catch (final WorkbookParserException e) {
					FileUploadController.LOG.error(e.getMessage(), e);
					result.reject(FileUploadController.UPLOAD_FORM_FILE,
							this.etlService.convertMessageList(e.getErrorMessages()).get(0));
				}
			}

			// at this point, we can assume that program has reached an error
			// condition. we return user to the form

			return this.getContentName();
		}
	}

	@ResponseBody
	@RequestMapping(value = "startProcess/{confirmDiscard}", method = RequestMethod.POST)
	public Map<String, String> startProcess(@PathVariable final int confirmDiscard, final HttpSession session,
			final HttpServletRequest request, final HttpServletResponse response, final Model model) {
		// HTTP 1.1
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		// HTTP 1.0
		response.setHeader("Pragma", "no-cache");
		// Proxies
		response.setDateHeader("Expires", 0);

		this.returnMessage.put(FileUploadController.STATUS_CODE, "0");
		this.returnMessage.put(FileUploadController.STATUS_MESSAGE, "Import has started.");

		try {

			final String programUUID = this.contextUtil.getCurrentProgramUUID();
			org.generationcp.middleware.domain.etl.Workbook wb;

			wb = this.dataImportService.parseWorkbook(this.etlService.retrieveCurrentWorkbookAsFile(this.userSelection),
					programUUID, confirmDiscard == 1 ? true : false, new WorkbookParser());

			// The entry type id should be saved in the db instead of the entry
			// type name
			this.convertEntryTypeNameToID(programUUID, wb.getObservations(),
					this.etlService.retrieveAvailableEntryTypes(programUUID));

			final MeasurementVariable plotIdMeasurementVariable = this.fieldbookService.createMeasurementVariable(
					String.valueOf(TermId.PLOT_ID.getId()), "", Operation.ADD, PhenotypicType.GERMPLASM);
			plotIdMeasurementVariable.setFactor(true);
			// PLOT_ID is not required in processing the fieldbook data file,
			// but we need to add it in the background
			// if it is not available as it is necessary in displaying the
			// plot_id in measurements
			this.fieldbookService.addMeasurementVariableToList(plotIdMeasurementVariable, wb.getFactors());

			// It is important to add the PLOT_ID measurement data in
			// measurement rows to make sure that variables
			// in Workbook match the variables in measurement rows. This will
			// initially creates blank values for PLOT_ID
			// but the generation of plot IDs will be handled during the saving
			// of Workbook.
			this.fieldbookService.addMeasurementVariableToMeasurementRows(plotIdMeasurementVariable,
					wb.getObservations());

			this.fieldbookService.addSTUDY_UIDVariableToWorkbookConditions(wb.getConditions());

			this.dataImportService.saveDataset(wb, programUUID,
					this.contextUtil.getProjectInContext().getCropType().getPlotCodePrefix());

			this.httpSessionUtil.clearSessionData(session,
					new String[] { HTTPSessionUtil.USER_SELECTION_SESSION_NAME });

			this.returnMessage.clear();
			this.returnMessage.put(FileUploadController.STATUS_CODE, FileUploadController.STATUS_CODE_SUCCESSFUL);
			this.returnMessage.put(FileUploadController.STATUS_MESSAGE, "Import is done.");

		} catch (final WorkbookParserException e) {

			FileUploadController.LOG.error(e.getMessage(), e);
			this.returnMessage.clear();
			this.returnMessage.put(FileUploadController.STATUS_CODE, FileUploadController.STATUS_CODE_HAS_ERROR);
			this.returnMessage.put(FileUploadController.STATUS_MESSAGE, e.getMessage());
			this.returnMessage.put(FileUploadController.ERROR_TYPE, e.getClass().getSimpleName());

		} catch (final IOException e) {
			FileUploadController.LOG.error(e.getMessage(), e);
			this.returnMessage.clear();
			this.returnMessage.put(FileUploadController.STATUS_CODE, FileUploadController.STATUS_CODE_HAS_ERROR);
			this.returnMessage.put(FileUploadController.STATUS_MESSAGE, "An error occurred while reading the file.");
			this.returnMessage.put(FileUploadController.ERROR_TYPE, e.getClass().getSimpleName());
		}

		return this.returnMessage;

	}

	public void convertEntryTypeNameToID(final String programUUID, final List<MeasurementRow> observations,
			final Map<String, Integer> availableEntryTypes) {
		final Map<String, MeasurementVariable> mVarMap = new HashMap<>();
		for (final MeasurementRow row : observations) {
			for (final MeasurementVariable mvar : row.getMeasurementVariables()) {
				final String variableName = mvar.getName();
				if (!mVarMap.containsKey(variableName)) {
					final MeasurementVariable measurementVariable = this.fieldbookMiddlewareService
							.getMeasurementVariableByPropertyScaleMethodAndRole(mvar.getProperty(), mvar.getScale(),
									mvar.getMethod(), mvar.getRole(), programUUID);
					mVarMap.put(variableName, measurementVariable);
				}
				if (mVarMap.get(variableName) != null
						&& mVarMap.get(variableName).getTermId() == TermId.ENTRY_TYPE.getId()) {
					final String value = row.getMeasurementData(variableName).getValue();
					row.getMeasurementData(variableName).setValue(availableEntryTypes.get(value).toString());
				}
			}
		}
	}

	@ResponseBody
	@RequestMapping(value = "validateAndParseWorkbook", method = RequestMethod.POST)
	public Map<String, String> validateAndParseWorkbook(final HttpSession session, final HttpServletRequest request,
			final HttpServletResponse response, final Model model) {

		// HTTP 1.1
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		// HTTP 1.0
		response.setHeader("Pragma", "no-cache");
		// Proxies
		response.setDateHeader("Expires", 0);

		try {

			final String programUUID = this.contextUtil.getCurrentProgramUUID();
			final Workbook workbook = this.dataImportService.strictParseWorkbook(
					this.etlService.retrieveCurrentWorkbookAsFile(this.userSelection), programUUID);

			if (workbook.hasOutOfBoundsData()) {
				this.returnMessage.put(FileUploadController.STATUS_CODE,
						FileUploadController.STATUS_CODE_HAS_OUT_OF_BOUNDS);
				this.returnMessage.put(FileUploadController.STATUS_MESSAGE, "");
			} else {
				this.returnMessage.put(FileUploadController.STATUS_CODE, FileUploadController.STATUS_CODE_SUCCESSFUL);
				this.returnMessage.put(FileUploadController.STATUS_MESSAGE, "");
			}

		} catch (final IOException e) {

			FileUploadController.LOG.error(e.getMessage(), e);

			this.returnMessage.clear();
			this.returnMessage.put(FileUploadController.STATUS_CODE, FileUploadController.STATUS_CODE_HAS_ERROR);
			this.returnMessage.put(FileUploadController.STATUS_MESSAGE, e.getMessage());
			this.returnMessage.put(FileUploadController.ERROR_TYPE, "IOException");

		} catch (final WorkbookParserException e) {

			FileUploadController.LOG.error(e.getMessage(), e);
			Boolean isMaxLimitException = false;
			final StringBuilder builder = new StringBuilder();
			builder.append("The system detected format errors in the file:<br/><br/>");
			if (e.getErrorMessages() != null) {
				for (final Message m : e.getErrorMessages()) {
					if (m != null) {
						try {
							builder.append(this.messageSource.getMessage(m.getMessageKey(), m.getMessageParams(), null)
									+ "<br />");
							if ("error.observation.over.maximum.limit".equals(m.getMessageKey())
									|| "error.file.is.too.large".equals(m.getMessageKey())) {
								isMaxLimitException = true;
							}
						} catch (final Exception ex) {
							FileUploadController.LOG.error(ex.getMessage(), ex);
						}
					}
				}
			} else {
				builder.append(e.getMessage());
			}

			this.returnMessage.clear();
			this.returnMessage.put(FileUploadController.STATUS_CODE, FileUploadController.STATUS_CODE_HAS_ERROR);
			this.returnMessage.put(FileUploadController.STATUS_MESSAGE, builder.toString());
			if (isMaxLimitException) {
				this.returnMessage.put(FileUploadController.ERROR_TYPE, "WorkbookParserException-OverMaxLimit");
			} else {
				this.returnMessage.put(FileUploadController.ERROR_TYPE, "WorkbookParserException");
			}

		} catch (final Exception e) {
			FileUploadController.LOG.error(e.getMessage(), e);

			this.returnMessage.clear();
			this.returnMessage.put(FileUploadController.STATUS_CODE, FileUploadController.STATUS_CODE_HAS_ERROR);
			this.returnMessage.put(FileUploadController.STATUS_MESSAGE, e.getMessage());
			this.returnMessage.put(FileUploadController.ERROR_TYPE, "Exception");

		}
		return this.returnMessage;

	}

	@Override
	public String getContentName() {
		return "etl/fileUpload";
	}

	@ModelAttribute("form")
	public FileUploadForm getForm() {
		return new FileUploadForm();
	}

	public void setEtlService(final ETLService etlService) {
		this.etlService = etlService;
	}

	public void setUserSelection(final UserSelection userSelection) {
		this.userSelection = userSelection;
	}

	@Override
	public UserSelection getUserSelection() {
		return this.userSelection;
	}
}
