package com.efficio.etl.web;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.web.bean.FileUploadForm;
import com.efficio.etl.web.bean.UserSelection;
import com.efficio.etl.web.controller.angular.AngularOpenSheetController;
import com.efficio.etl.web.validators.FileUploadFormValidator;
import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.Constants;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.exceptions.PhenotypeException;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(ImportObservationsController.URL)
public class ImportObservationsController extends AbstractBaseETLController {

	public static final String URL = "/etl/workbook/importObservations";
	private static final Logger LOG = LoggerFactory.getLogger(ImportObservationsController.class);
	private static final String UNMAPPED_HEADERS = "unmappedHeaders";
	private static final String MAPPED_TRAITS = "mappedTraits";

	@Resource(name = "etlUserSelection")
	private UserSelection userSelection;

	@Resource
	private ETLService etlService;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	private DataImportService dataImportService;

	@Autowired
	private FieldbookService fieldbookService;

	private boolean hasErrors;

	@Override
	public String getContentName() {
		if (this.hasErrors) {
			return "etl/validateProjectData";
		}
		return "etl/importObservations";
	}

	@Override
	public UserSelection getUserSelection() {
		return this.userSelection;
	}

	@RequestMapping(value = "/{confirmDiscard}", method = RequestMethod.GET)
	public String processImport(
		@ModelAttribute("uploadForm") final FileUploadForm uploadForm, @PathVariable final int confirmDiscard,
		final Model model, final HttpSession session, final HttpServletRequest request) {
		final List<String> errors = new ArrayList<>();

		org.generationcp.middleware.domain.etl.Workbook importData = null;

		String programUUID = null;
		try {

			programUUID = this.contextUtil.getCurrentProgramUUID();
			final Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);
			final boolean isMeansDataImport =
				this.userSelection.getDatasetType() != null && this.userSelection.getDatasetType().intValue() == DatasetTypeEnum.MEANS_DATA.getId();

			importData = this.etlService.createWorkbookFromUserSelection(this.userSelection, isMeansDataImport);

			this.dataImportService.populatePossibleValuesForCategoricalVariates(importData.getVariates(), programUUID);

			final List<MeasurementVariable> studyHeaders = new ArrayList<>(importData.getFactors());
			studyHeaders.addAll(importData.getVariates());

			final List<String> fileHeaders =
				this.etlService.retrieveColumnHeaders(workbook, this.userSelection, this.etlService.headersContainsObsUnitId(importData));

			final Map<String, List<Message>> mismatchErrors =
				this.etlService.checkForMismatchedHeaders(fileHeaders, studyHeaders, isMeansDataImport);

			final boolean isWorkbookHasObservationRecords =
				this.etlService.isWorkbookHasObservationRecords(this.userSelection, errors, workbook);
			final boolean isObservationOverMaxLimit = this.etlService.isObservationOverMaximumLimit(this.userSelection, errors, workbook);

			if (mismatchErrors != null && !mismatchErrors.isEmpty()) {
				for (final Map.Entry<String, List<Message>> entry : mismatchErrors.entrySet()) {
					errors.addAll(this.etlService.convertMessageList(entry.getValue()));
				}
			} else if (isWorkbookHasObservationRecords && !isObservationOverMaxLimit) {

				this.dataImportService.removeLocationNameVariableIfExists(importData);
				this.dataImportService.assignLocationIdVariableToEnvironmentDetailSection(importData);
				importData.setObservations(
					this.etlService.extractExcelFileData(workbook, this.userSelection, importData, confirmDiscard == 1 ? true : false));

				// there is now an expectation after the validate project data
				// step
				final Map<String, List<Message>> projectDataErrors = this.etlService.validateProjectData(importData, programUUID);

				if (projectDataErrors != null) {
					for (final Map.Entry<String, List<Message>> entry : projectDataErrors.entrySet()) {
						errors.addAll(this.etlService.convertMessageList(entry.getValue()));
					}
				}
			}

			final List<Message> messages = new ArrayList<>();
			this.dataImportService.checkForInvalidGids(importData, messages, programUUID);
			errors.addAll(this.etlService.convertMessageList(messages));

		} catch (final Exception e) {
			ImportObservationsController.LOG.error(e.getMessage(), e);
			final List<Message> error = new ArrayList<>();
			error.add(new Message(Constants.MESSAGE_KEY_GENERIC_ERROR));
			errors.addAll(this.etlService.convertMessageList(error));

		}

		model.addAttribute("errors", errors);
		this.hasErrors = !errors.isEmpty();
		model.addAttribute("hasErrors", this.hasErrors);

		if (this.hasErrors) {
			return this.getContentName();
		} else {
			return this.confirmImport(model, importData, programUUID);
		}
	}

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public String uploadFile(@ModelAttribute("uploadForm") final FileUploadForm uploadForm, final BindingResult result, final Model model) {
		final FileUploadFormValidator validator = new FileUploadFormValidator();
		validator.validate(uploadForm, result);
		this.hasErrors = result.hasErrors();
		if (this.hasErrors) {
			return this.getContentName();
		} else {
			try {
				final String tempFileName = this.etlService.storeUserWorkbook(uploadForm.getFile().getInputStream());
				this.userSelection.setServerFileName(tempFileName);
				this.userSelection.setActualFileName(uploadForm.getFile().getOriginalFilename());
			} catch (final IOException e) {
				ImportObservationsController.LOG.error(e.getMessage(), e);
				result.reject("uploadForm.file", "Error occurred while uploading file.");
			}
			return "redirect:" + AngularOpenSheetController.URL;
		}
	}

	public String confirmImport(
		final Model model, final org.generationcp.middleware.domain.etl.Workbook importData,
		final String programUUID) {
		final List<String> errors = new ArrayList<>();
		try {
			final org.generationcp.middleware.domain.etl.Workbook referenceWorkbook = this.dataImportService
				.parseWorkbookDescriptionSheet(
					this.etlService.retrieveCurrentWorkbook(this.userSelection),
					this.contextUtil.getCurrentWorkbenchUserId());
			importData.setConstants(referenceWorkbook.getConstants());
			importData.setConditions(referenceWorkbook.getConditions());
			this.dataImportService.addExptDesignVariableIfNotExists(importData, importData.getFactors(), programUUID);
			this.dataImportService.addLocationIDVariableIfNotExists(importData, importData.getFactors(), programUUID);
			this.dataImportService.addEntryTypeVariableIfNotExists(importData, programUUID);
			this.dataImportService.assignLocationIdVariableToEnvironmentDetailSection(importData);
			this.dataImportService.removeLocationNameVariableIfExists(importData);
			this.dataImportService.populatePossibleValuesForCategoricalVariates(importData.getConditions(), programUUID);
			this.etlService.saveProjectData(importData, programUUID);

		} catch (final PhenotypeException e) {
			ImportObservationsController.LOG.error(e.getMessage(), e);
			errors.add(e.getMessage().replaceAll("\n", "<br>"));
		} catch (final Exception e) {
			ImportObservationsController.LOG.error(e.getMessage(), e);
			errors.add(e.getMessage());
		} finally {
			model.addAttribute("errors", errors);
			this.hasErrors = !errors.isEmpty();
			model.addAttribute("hasErrors", this.hasErrors);
		}

		return super.show(model);

	}

	@ResponseBody
	@RequestMapping(value = "/getMappingData/{newVariables}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public Map<String, List<DesignHeaderItem>> getMappingData(@PathVariable final List<String> newVariables) {
		final Map<String, List<DesignHeaderItem>> mappingData = new HashMap<>();

		final List<DesignHeaderItem> listNewVariables = this.createDesignHeaderItemList(newVariables);

		final List<DesignHeaderItem> newVariablesMapped = new ArrayList<DesignHeaderItem>();
		final List<DesignHeaderItem> updatedNewVariables = this.updateMapping(listNewVariables);
		newVariablesMapped.addAll(CollectionUtils.subtract(listNewVariables, updatedNewVariables));

		mappingData.put(
			ImportObservationsController.UNMAPPED_HEADERS, newVariablesMapped);
		mappingData.put(
			ImportObservationsController.MAPPED_TRAITS, updatedNewVariables);

		return mappingData;
	}

	private List<DesignHeaderItem> createDesignHeaderItemList(final List<String> newVariables) {
		final List<DesignHeaderItem> listNewVariables = new ArrayList<>();
		int columnIndex = 0;
		for (final String headerName : newVariables) {
			final DesignHeaderItem headerItem = new DesignHeaderItem();
			headerItem.setName(headerName);
			headerItem.setColumnIndex(columnIndex);
			listNewVariables.add(headerItem);

			columnIndex++;
		}
		return listNewVariables;
	}

	protected List<DesignHeaderItem> updateMapping(final List<DesignHeaderItem> mappedHeaders) {
		final List<DesignHeaderItem> newMappingResults = new ArrayList<>();

		for (final DesignHeaderItem item : mappedHeaders) {
			final StandardVariable stdVar =
				this.fieldbookService.getStandardVariableByName(item.getName(), this.contextUtil.getCurrentProgramUUID());

			if (stdVar != null) {
				stdVar.setPhenotypicType(PhenotypicType.VARIATE);
				item.setVariable(stdVar);
				newMappingResults.add(item);
			}
		}

		return newMappingResults;
	}

}
