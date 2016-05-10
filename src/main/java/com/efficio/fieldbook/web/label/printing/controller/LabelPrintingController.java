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

package com.efficio.fieldbook.web.label.printing.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.context.ContextConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.commons.pojo.CustomReportType;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.CustomReportTypeUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.commons.util.StringUtil;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.presets.StandardPreset;
import org.generationcp.middleware.reports.BuildReportException;
import org.generationcp.middleware.reports.Reporter;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.ReportService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.WebUtils;

import com.efficio.fieldbook.service.api.LabelPrintingService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.label.printing.bean.LabelPrintingPresets;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.label.printing.constant.LabelPrintingFileTypes;
import com.efficio.fieldbook.web.label.printing.form.LabelPrintingForm;
import com.efficio.fieldbook.web.label.printing.xml.BarcodeLabelPrintingSetting;
import com.efficio.fieldbook.web.label.printing.xml.CSVExcelLabelPrintingSetting;
import com.efficio.fieldbook.web.label.printing.xml.LabelPrintingSetting;
import com.efficio.fieldbook.web.label.printing.xml.PDFLabelPrintingSetting;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SessionUtility;
import com.efficio.fieldbook.web.util.SettingsUtil;
import net.sf.jasperreports.engine.JRException;

/**
 * The Class LabelPrintingController.
 *
 * This class would handle the label printing for the pdf and excel generation.
 */
@Controller @RequestMapping({LabelPrintingController.URL})
public class LabelPrintingController extends AbstractBaseFieldbookController {

	/**
	 * The Constant URL.
	 */
	public static final String URL = "/LabelPrinting/specifyLabelDetails";
	static final String IS_SUCCESS = "isSuccess";
	private static final String AVAILABLE_FIELDS = "availableFields";
	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(LabelPrintingController.class);
	/**
	 * The Constant BUFFER_SIZE.
	 */
	private static final int BUFFER_SIZE = 4096 * 4;
	/**
	 * The user label printing.
	 */
	@Resource
	private UserLabelPrinting userLabelPrinting;
	/**
	 * The fieldbook middleware service.
	 */
	@Resource
	private FieldbookService fieldbookMiddlewareService;
	/**
	 * The label printing service.
	 */
	@Resource
	private LabelPrintingService labelPrintingService;
	/**
	 * The user fieldmap.
	 */
	@Resource
	private UserFieldmap userFieldmap;
	/**
	 * The message source.
	 */
	@Resource
	private ResourceBundleMessageSource messageSource;

	@Resource
	private UserSelection userSelection;

	@Resource
	private CrossExpansionProperties crossExpansionProperties;
	@Resource
	private WorkbenchService workbenchService;
	@Resource
	private ContextUtil contextUtil;
	@Resource
	private ReportService reportService;

	@Resource
	private GermplasmListManager germplasmListManager;

	/**
	 * Show trial label details.
	 *
	 * @param form    the form
	 * @param model   the model
	 * @param session the session
	 * @param id      the id
	 * @param locale  the locale
	 * @return the string
	 */
	@RequestMapping(value = "/trial/{id}", method = RequestMethod.GET)
	public String showTrialLabelDetails(@ModelAttribute("labelPrintingForm") LabelPrintingForm form, Model model, HttpServletRequest req,
			HttpSession session, @PathVariable int id, Locale locale) {

		SessionUtility.clearSessionData(session,
				new String[] {SessionUtility.LABEL_PRINTING_SESSION_NAME, SessionUtility.FIELDMAP_SESSION_NAME,
						SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});
		Study study = null;
		List<FieldMapInfo> fieldMapInfoList = null;
		FieldMapInfo fieldMapInfo = null;
		boolean hasFieldMap = false;
		try {
			study = this.fieldbookMiddlewareService.getStudy(id);
			List<Integer> ids = new ArrayList<>();
			ids.add(id);
			fieldMapInfoList = this.fieldbookMiddlewareService.getFieldMapInfoOfTrial(ids, this.crossExpansionProperties);

			for (FieldMapInfo fieldMapInfoDetail : fieldMapInfoList) {
				fieldMapInfo = fieldMapInfoDetail;
				hasFieldMap = this.labelPrintingService.checkAndSetFieldmapProperties(this.userLabelPrinting, fieldMapInfoDetail);
			}
		} catch (MiddlewareException e) {
			LabelPrintingController.LOG.error(e.getMessage(), e);
		}
		this.userLabelPrinting.setStudyId(id);
		this.userLabelPrinting.setStudy(study);
		this.userLabelPrinting.setFieldMapInfo(fieldMapInfo);
		this.userLabelPrinting.setBarcodeNeeded("0");
		this.userLabelPrinting.setIncludeColumnHeadinginNonPdf("1");
		this.userLabelPrinting.setNumberOfLabelPerRow("3");
		this.userLabelPrinting.setIsTrial(true);

		this.userLabelPrinting.setFilename(this.generateDefaultFilename(this.userLabelPrinting, true));
		form.setUserLabelPrinting(this.userLabelPrinting);

		model.addAttribute(LabelPrintingController.AVAILABLE_FIELDS,
				this.labelPrintingService.getAvailableLabelFieldsForStudy(true, hasFieldMap, locale, id));

		form.setIsTrial(true);
		return super.show(model);
	}

	/**
	 * Show nursery label details.
	 *
	 * @param form    the form
	 * @param model   the model
	 * @param session the session
	 * @param id      the id
	 * @param locale  the locale
	 * @return the string
	 */
	@RequestMapping(value = "/nursery/{id}", method = RequestMethod.GET)
	public String showNurseryLabelDetails(@ModelAttribute("labelPrintingForm") LabelPrintingForm form, Model model, HttpServletRequest req,
			HttpSession session, @PathVariable int id, Locale locale) {
		SessionUtility.clearSessionData(session,
				new String[] {SessionUtility.LABEL_PRINTING_SESSION_NAME, SessionUtility.FIELDMAP_SESSION_NAME,
						SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});
		Study study = null;
		List<FieldMapInfo> fieldMapInfoList = null;
		FieldMapInfo fieldMapInfo = null;
		boolean hasFieldMap = false;
		try {
			study = this.fieldbookMiddlewareService.getStudy(id);
			List<Integer> ids = new ArrayList<>();
			ids.add(id);
			fieldMapInfoList = this.fieldbookMiddlewareService.getFieldMapInfoOfNursery(ids, this.crossExpansionProperties);
			for (FieldMapInfo fieldMapInfoDetail : fieldMapInfoList) {
				fieldMapInfo = fieldMapInfoDetail;
				hasFieldMap = this.labelPrintingService.checkAndSetFieldmapProperties(this.userLabelPrinting, fieldMapInfoDetail);
			}
		} catch (MiddlewareException e) {
			LabelPrintingController.LOG.error(e.getMessage(), e);
		}
		this.userLabelPrinting.setStudyId(id);
		this.userLabelPrinting.setStudy(study);
		this.userLabelPrinting.setFieldMapInfo(fieldMapInfo);
		this.userLabelPrinting.setBarcodeNeeded("0");
		this.userLabelPrinting.setIncludeColumnHeadinginNonPdf("1");
		this.userLabelPrinting.setNumberOfLabelPerRow("3");
		this.userLabelPrinting.setIsTrial(false);

		this.userLabelPrinting.setFilename(this.generateDefaultFilename(this.userLabelPrinting, false));
		form.setUserLabelPrinting(this.userLabelPrinting);
		model.addAttribute(LabelPrintingController.AVAILABLE_FIELDS,
				this.labelPrintingService.getAvailableLabelFieldsForStudy(false, hasFieldMap, locale, id));
		form.setIsTrial(false);
		return super.show(model);
	}

	/**
	 * Show fieldmap label details.
	 *
	 * @param form    the form
	 * @param model   the model
	 * @param session the session
	 * @param locale  the locale
	 * @return the string
	 */
	@RequestMapping(value = "/fieldmap", method = RequestMethod.GET)
	public String showFieldmapLabelDetails(@ModelAttribute("labelPrintingForm") LabelPrintingForm form, Model model, HttpSession session,
			Locale locale) {
		List<FieldMapInfo> fieldMapInfoList = this.userFieldmap.getSelectedFieldMaps();

		// sets the initial fieldMapInfo from fieldMapInfoList
		// this will be used later for the generation of labels in label
		// printing
		FieldMapInfo fieldMapInfo = fieldMapInfoList.get(0);
		this.userLabelPrinting.setStudyId(null);
		this.userLabelPrinting.setFieldMapInfo(fieldMapInfo);
		this.userLabelPrinting.setFieldMapInfoList(fieldMapInfoList);
		this.userLabelPrinting.setBarcodeNeeded("0");
		this.userLabelPrinting.setIncludeColumnHeadinginNonPdf("1");
		this.userLabelPrinting.setNumberOfLabelPerRow("3");

		this.userLabelPrinting.setFirstBarcodeField("");
		this.userLabelPrinting.setSecondBarcodeField("");
		this.userLabelPrinting.setThirdBarcodeField("");
		this.userLabelPrinting.setFieldMapsExisting(true);

		this.userLabelPrinting.setFilename(this.generateDefaultFilename(this.userLabelPrinting, this.userFieldmap.isTrial()));
		form.setUserLabelPrinting(this.userLabelPrinting);

		model.addAttribute(LabelPrintingController.AVAILABLE_FIELDS,
				this.labelPrintingService.getAvailableLabelFieldsForFieldMap(this.userFieldmap.isTrial(), true, locale));

		form.setIsTrial(this.userFieldmap.isTrial());

		return super.show(model);
	}

	@RequestMapping(value = "/stock/{id}", method = RequestMethod.GET)
	public String showStockListLabelDetails(@ModelAttribute("labelPrintingForm") LabelPrintingForm form, Model model, HttpSession session, @PathVariable int id, Locale locale) {

		SessionUtility.clearSessionData(session,
				new String[] {SessionUtility.LABEL_PRINTING_SESSION_NAME,
						SessionUtility.FIELDMAP_SESSION_NAME,
						SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});

		// retrieve the stock list
		GermplasmList stockList = this.germplasmListManager.getGermplasmListById(id);

		Study study = this.fieldbookMiddlewareService.getStudy(stockList.getProjectId());
		List<Integer> ids = new ArrayList<>();
		ids.add(stockList.getProjectId());

		List<FieldMapInfo> fieldMapInfoList;

		if(Objects.equals(study.getType(), StudyType.T)){
			fieldMapInfoList = this.fieldbookMiddlewareService.getFieldMapInfoOfTrial(ids, this.crossExpansionProperties);
		}else {
			fieldMapInfoList = this.fieldbookMiddlewareService.getFieldMapInfoOfNursery(ids, this.crossExpansionProperties);
		}

		List<InventoryDetails> inventoryDetails = this.labelPrintingService.getInventoryDetails(stockList.getId());

		for (FieldMapInfo fieldMapInfoDetail : fieldMapInfoList) {
			if(Objects.equals(study.getType(), StudyType.T)){
				this.userLabelPrinting.setFieldMapInfo(fieldMapInfoDetail,inventoryDetails);
			}
			else{
				this.userLabelPrinting.setFieldMapInfo(fieldMapInfoDetail);
			}

			this.labelPrintingService.checkAndSetFieldmapProperties(this.userLabelPrinting, fieldMapInfoDetail);
		}

		this.userLabelPrinting.setStudy(study);
		this.userLabelPrinting.setBarcodeNeeded("0");
		this.userLabelPrinting.setIncludeColumnHeadinginNonPdf("1");
		this.userLabelPrinting.setNumberOfLabelPerRow("3");
		this.userLabelPrinting.setIsStockList(true);
		this.userLabelPrinting.setStockListId(stockList.getId());
		this.userLabelPrinting.setStockListTypeName(stockList.getType());
		this.userLabelPrinting.setInventoryDetailsList(inventoryDetails);
		this.userLabelPrinting.setFilename(this.generateDefaultFilename(this.userLabelPrinting, false));
		form.setUserLabelPrinting(this.userLabelPrinting);
		model.addAttribute(
				LabelPrintingController.AVAILABLE_FIELDS,
				this.labelPrintingService.getAvailableLabelFieldsForStockList(
						this.labelPrintingService.getStockListType(stockList.getType()), locale, study.getType(), stockList.getProjectId()));

		if(Objects.equals(study.getType(), StudyType.T)) {
			form.setIsTrial(true);
			this.userLabelPrinting.setIsTrial(true);
		}else {
			form.setIsTrial(false);
			this.userLabelPrinting.setIsTrial(false);
		}

		form.setIsStockList(true);

		return super.show(model);
	}

	/**
	 * Generate default filename.
	 *
	 * @param userLabelPrinting the user label printing
	 * @param isTrial           the is trial
	 * @return the string
	 */
	private String generateDefaultFilename(UserLabelPrinting userLabelPrinting, boolean isTrial) {
		String currentDate = DateUtil.getCurrentDateAsStringValue();
		String fileName = "Labels-for-" + userLabelPrinting.getName();

		if (isTrial) {
			if (this.userLabelPrinting.getFieldMapInfoList() != null) {
				fileName = "Trial-Field-Map-Labels-" + currentDate;
			} else {
				// changed selected name to block name for now
				fileName += "-" + userLabelPrinting.getNumberOfInstances() + "-" + currentDate;
			}
		} else {
			if (this.userLabelPrinting.getFieldMapInfoList() != null) {
				fileName = "Nursery-Field-Map-Labels-" + currentDate;
			} else {
				// changed selected name to block name for now
				fileName += "-" + currentDate;
			}
		}
		fileName = SettingsUtil.cleanSheetAndFileName(fileName);

		return fileName;
	}

	/**
	 * Export file.
	 *
	 * @param response the response
	 * @return the string
	 */
	@RequestMapping(value = "/download", method = RequestMethod.GET)
	public ResponseEntity<FileSystemResource> exportFile(HttpServletRequest req) throws UnsupportedEncodingException {

		String filename = this.userLabelPrinting.getFilename();
		String absoluteLocation = this.userLabelPrinting.getFilenameDLLocation();

		return FieldbookUtil.createResponseEntityForFileDownload(absoluteLocation, filename);

	}

	/**
	 * Submits the details.
	 *
	 * @param form the form
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	public Map<String, Object> submitDetails(@ModelAttribute("labelPrintingForm") LabelPrintingForm form) {

		this.userLabelPrinting.setBarcodeNeeded(form.getUserLabelPrinting().getBarcodeNeeded());
		this.userLabelPrinting.setSizeOfLabelSheet(form.getUserLabelPrinting().getSizeOfLabelSheet());
		this.userLabelPrinting.setNumberOfLabelPerRow(form.getUserLabelPrinting().getNumberOfLabelPerRow());
		this.userLabelPrinting.setNumberOfRowsPerPageOfLabel(form.getUserLabelPrinting().getNumberOfRowsPerPageOfLabel());
		this.userLabelPrinting.setLeftSelectedLabelFields(form.getUserLabelPrinting().getLeftSelectedLabelFields());
		this.userLabelPrinting.setRightSelectedLabelFields(form.getUserLabelPrinting().getRightSelectedLabelFields());
		this.userLabelPrinting.setMainSelectedLabelFields(form.getUserLabelPrinting().getMainSelectedLabelFields());
		this.userLabelPrinting.setIncludeColumnHeadinginNonPdf(form.getUserLabelPrinting().getIncludeColumnHeadinginNonPdf());
		this.userLabelPrinting.setSettingsName(form.getUserLabelPrinting().getSettingsName());
		this.userLabelPrinting.setFirstBarcodeField(form.getUserLabelPrinting().getFirstBarcodeField());
		this.userLabelPrinting.setSecondBarcodeField(form.getUserLabelPrinting().getSecondBarcodeField());
		this.userLabelPrinting.setThirdBarcodeField(form.getUserLabelPrinting().getThirdBarcodeField());
		this.userLabelPrinting.setFilename(form.getUserLabelPrinting().getFilename());
		this.userLabelPrinting.setGenerateType(form.getUserLabelPrinting().getGenerateType());

		// add validation for the file name
		if (!FileUtils.isFilenameValid(this.userLabelPrinting.getFilename())) {
			Map<String, Object> results = new HashMap<>();
			results.put(LabelPrintingController.IS_SUCCESS, 0);
			results.put(AppConstants.MESSAGE.getString(),
					this.messageSource.getMessage("common.error.invalid.filename.windows", new Object[] {}, Locale.getDefault()));

			return results;
		}

		Workbook workbook = this.userSelection.getWorkbook();

		if (workbook != null) {
			String selectedLabelFields = this.getSelectedLabelFields(this.userLabelPrinting);
			this.labelPrintingService.populateUserSpecifiedLabelFields(this.userLabelPrinting.getFieldMapInfo().getDatasets().get(0)
					.getTrialInstances(), workbook, selectedLabelFields, form.getIsTrial(), form.getIsStockList(), this.userLabelPrinting);
		}

		List<FieldMapInfo> fieldMapInfoList = this.userLabelPrinting.getFieldMapInfoList();

		List<StudyTrialInstanceInfo> trialInstances;

		if (fieldMapInfoList != null) {
			trialInstances = this.generateTrialInstancesFromSelectedFieldMaps(fieldMapInfoList, form);
		} else {
			// initial implementation of BMS-186 will be for single studies
			// only, not for cases where multiple studies participating in a
			// single fieldmap
			trialInstances = this.generateTrialInstancesFromFieldMap();

			for (StudyTrialInstanceInfo trialInstance : trialInstances) {
				FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo = trialInstance.getTrialInstance();
				fieldMapTrialInstanceInfo.setLocationName(fieldMapTrialInstanceInfo.getSiteName());
			}
		}

		return this.generateLabels(trialInstances, form.isCustomReport());
	}

	protected String getSelectedLabelFields(UserLabelPrinting userLabelPrinting) {
		String selectedLabelFields = "";
		if (userLabelPrinting.getGenerateType().equalsIgnoreCase(AppConstants.LABEL_PRINTING_PDF.getString())) {
			selectedLabelFields = userLabelPrinting.getLeftSelectedLabelFields() + "," + userLabelPrinting.getRightSelectedLabelFields();
		} else {
			selectedLabelFields = userLabelPrinting.getMainSelectedLabelFields();
		}
		return selectedLabelFields;
	}

	Map<String, Object> generateLabels(List<StudyTrialInstanceInfo> trialInstances, boolean isCustomReport) {
		Map<String, Object> results = new HashMap<>();

		try {
			if (isCustomReport) {
				generateLabelForCustomReports(results);
			} else {
				generateLabelForLabelTypes(trialInstances, results);
			}

		} catch (IOException | MiddlewareException | JRException | BuildReportException e) {
			LabelPrintingController.LOG.error(e.getMessage(), e);
			results.put(LabelPrintingController.IS_SUCCESS, 0);
			results.put(AppConstants.MESSAGE.getString(), e.getMessage());
		} catch (LabelPrintingException e) {
			LabelPrintingController.LOG.error(e.getMessage(), e);
			results.put(LabelPrintingController.IS_SUCCESS, 0);
			Locale locale = LocaleContextHolder.getLocale();

			if (e.getErrorCode() != null) {
				results.put(AppConstants.MESSAGE.getString(),
						this.messageSource.getMessage(e.getErrorCode(), new String[] {e.getLabelError()}, locale));
			} else if (e.getCause() != null) {
				results.put(AppConstants.MESSAGE.getString(), e.getCause().getMessage());
			}

		}
		return results;
	}

	void generateLabelForLabelTypes(List<StudyTrialInstanceInfo> trialInstances, Map<String, Object> results)
			throws LabelPrintingException {
		final String fileName;
		final LabelPrintingFileTypes selectedLabelPrintingType =
				LabelPrintingFileTypes.getFileTypeByIndex(this.userLabelPrinting.getGenerateType());
		final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

		if (selectedLabelPrintingType.isValid()) {
			this.getFileNameAndSetFileLocations(selectedLabelPrintingType.getExtension());

			fileName = this.labelPrintingService
					.generateLabels(selectedLabelPrintingType.getFormIndex(), trialInstances, this.userLabelPrinting, byteStream);

			results.put(LabelPrintingController.IS_SUCCESS, 1);
			results.put("fileName", fileName);

		} else {
			final String errorMsg = this.messageSource
					.getMessage("label.printing.cannot.generate.invalid.type", new String[] {}, LocaleContextHolder.getLocale());

			LabelPrintingController.LOG.error(errorMsg);
			results.put(LabelPrintingController.IS_SUCCESS, 0);
			results.put(AppConstants.MESSAGE.getString(), errorMsg);
		}

	}

	void generateLabelForCustomReports(Map<String, Object> results) throws JRException, IOException, BuildReportException {
		final Integer studyId = this.userLabelPrinting.getStudyId();
		final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

		final Reporter rep = this.reportService
				.getStreamReport(this.userLabelPrinting.getGenerateType(), studyId, this.contextUtil.getProjectInContext().getProjectName(),
						byteStream);

		// additionally creates the file in 'target' folder, for human
		// validation ;)
		final String fileName = rep.getFileName();

		this.getFileNameAndSetFileLocations("." + rep.getFileExtension());

		Files.write(Paths.get(this.userLabelPrinting.getFilenameDLLocation()), byteStream.toByteArray());

		this.userLabelPrinting.setFilename(fileName);

		results.put(LabelPrintingController.IS_SUCCESS, 1);
		results.put("fileName", fileName);

	}

	@ResponseBody
	@RequestMapping(value = "/presets/list", method = RequestMethod.GET)
	public List<LabelPrintingPresets> getLabelPrintingPresets(HttpServletRequest request) {
		ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO);

		try {
			return this.labelPrintingService.getAllLabelPrintingPresets(contextInfo.getSelectedProjectId().intValue());
		} catch (LabelPrintingException e) {
			LabelPrintingController.LOG.error(e.getMessage(), e);
		}

		return new ArrayList<>();
	}

	@ResponseBody
	@RequestMapping(value = "/custom/reports", method = RequestMethod.GET)
	public List<CustomReportType> getLabelPrintingCustomReports() {
		List<CustomReportType> customReportTypes = new ArrayList<>();
		try {
			if (this.userLabelPrinting.getStudyId() != null) {
				List<StandardPreset> standardPresetList = this.workbenchService
						.getStandardPresetByCrop(this.workbenchService.getFieldbookWebTool().getToolId().intValue(),
								this.contextUtil.getProjectInContext().getCropType().getCropName().toLowerCase(),
								ToolSection.FB_LBL_PRINT_CUSTOM_REPORT.name());
				// we need to convert the standard preset for custom report type
				// to custom report type pojo
				for (int index = 0; index < standardPresetList.size(); index++) {
					customReportTypes.addAll(CustomReportTypeUtil
							.readReportConfiguration(standardPresetList.get(index), this.crossExpansionProperties.getProfile()));
				}
			}
		} catch (MiddlewareQueryException e) {
			LabelPrintingController.LOG.error(e.getMessage(), e);
		}

		return customReportTypes;
	}

	@ResponseBody
	@RequestMapping(value = "/presets/{presetType}/{presetId}", method = RequestMethod.GET, produces = "application/json")
	public LabelPrintingSetting getLabelPrintingSetting(@PathVariable int presetType, @PathVariable int presetId,
			HttpServletRequest request) {
		try {
			final Unmarshaller parseXML = JAXBContext.newInstance(LabelPrintingSetting.class).createUnmarshaller();

			// retrieve appropriate setting
			String xmlToRead = this.labelPrintingService.getLabelPrintingPresetConfig(presetId, presetType);

			return (LabelPrintingSetting) parseXML.unmarshal(new StringReader(xmlToRead));

		} catch (JAXBException e) {
			LabelPrintingController.LOG.error(this.messageSource
					.getMessage("label.printing.error.parsing.preset.xml", new String[] {}, LocaleContextHolder.getLocale()), e);

		} catch (LabelPrintingException e) {
			final String labelError = this.messageSource.getMessage(e.getLabelError(), new String[] {}, LocaleContextHolder.getLocale());

			LabelPrintingController.LOG
					.error(this.messageSource.getMessage(e.getErrorCode(), new String[] {labelError}, LocaleContextHolder.getLocale()), e);
		}

		return new LabelPrintingSetting();
	}

	/**
	 * Search program-preset,
	 *
	 * @param presetName
	 * @param request
	 * @return list of presets that matches presetName
	 */
	@ResponseBody
	@RequestMapping(value = "/presets/searchLabelPrintingPresetByName", method = RequestMethod.GET)
	public List<LabelPrintingPresets> searchLabelPrintingPresetByName(@RequestParam("name") String presetName, HttpServletRequest request) {
		ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO);

		try {

			List<LabelPrintingPresets> standardPresets = this.labelPrintingService
					.getAllLabelPrintingPresetsByName(presetName, contextInfo.getSelectedProjectId().intValue(),
							LabelPrintingPresets.STANDARD_PRESET);

			if (!standardPresets.isEmpty()) {
				return standardPresets;
			} else {
				return this.labelPrintingService.getAllLabelPrintingPresetsByName(presetName, contextInfo.getSelectedProjectId().intValue(),
						LabelPrintingPresets.PROGRAM_PRESET);
			}
		} catch (MiddlewareQueryException e) {
			LabelPrintingController.LOG.error(e.getMessage(), e);
			return new ArrayList<>();
		}

	}

	/**
	 * Delete's program preset
	 *
	 * @param programPresetId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/presets/delete", method = RequestMethod.GET)
	public Boolean deleteLabelPrintingPreset(@RequestParam("programPresetId") Integer programPresetId) {

		try {
			this.labelPrintingService.deleteProgramPreset(programPresetId);

			return true;

		} catch (MiddlewareQueryException e) {
			LabelPrintingController.LOG.error(e.getMessage(), e);
		}

		return false;
	}

	/**
	 * Saves the label printing setting. Note that the fields should be pre-validated before calling this service
	 *
	 * @param labelPrintingPresetSetting
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/presets/save", method = RequestMethod.POST)
	public Boolean saveLabelPrintingSetting(@ModelAttribute("labelPrintingForm") LabelPrintingForm labelPrintingPresetSetting,
			HttpServletRequest request) {
		UserLabelPrinting rawSettings = labelPrintingPresetSetting.getUserLabelPrinting();

		// save or update
		try {
			final ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO);

			this.labelPrintingService.saveOrUpdateLabelPrintingPresetConfig(rawSettings.getSettingsName(),
					this.transformLabelPrintingSettingsToXML(rawSettings), contextInfo.getSelectedProjectId().intValue());

		} catch (MiddlewareQueryException e) {
			LabelPrintingController.LOG.error(e.getMessage(), e);
			return false;
		}

		return true;
	}

	@ResponseBody
	@RequestMapping(value = "/presets/isModified/{presetType}/{presetId}", method = RequestMethod.POST)
	public Boolean isLabelPrintingIsModified(@ModelAttribute("labelPrintingForm") LabelPrintingForm labelPrintingPresetSetting,
			@PathVariable Integer presetType, @PathVariable Integer presetId, HttpServletRequest request) {
		LabelPrintingSetting lbSetting = this.getLabelPrintingSetting(presetType, presetId, request);
		LabelPrintingSetting modifiedSetting;
		final Unmarshaller parseXML;
		try {

			parseXML = JAXBContext.newInstance(LabelPrintingSetting.class).createUnmarshaller();

			// retrieve appropriate setting
			String xmlToRead = this.transformLabelPrintingSettingsToXML(labelPrintingPresetSetting.getUserLabelPrinting());

			modifiedSetting = (LabelPrintingSetting) parseXML.unmarshal(new StringReader(xmlToRead));

			return !modifiedSetting.equals(lbSetting);

		} catch (JAXBException e) {
			LabelPrintingController.LOG.error(e.getMessage(), e);
		}

		return false;
	}

	/**
	 * @param rawSettings
	 * @return
	 */
	private String transformLabelPrintingSettingsToXML(UserLabelPrinting rawSettings) {
		// Preparation, convert the form into appropriate pojos for easy access
		CSVExcelLabelPrintingSetting nonPDFSettings = null;
		PDFLabelPrintingSetting pdfSettings = null;
		BarcodeLabelPrintingSetting barcodeSettings = new BarcodeLabelPrintingSetting("1".equals(rawSettings.getBarcodeNeeded()), "Barcode",
				StringUtil.stringify(new String[] {rawSettings.getFirstBarcodeField(), rawSettings.getSecondBarcodeField(),
						rawSettings.getThirdBarcodeField()}, ","));

		if (AppConstants.LABEL_PRINTING_PDF.getString().equals(rawSettings.getGenerateType())) {
			pdfSettings = new PDFLabelPrintingSetting(rawSettings.getSizeOfLabelSheet(),
					Integer.parseInt(rawSettings.getNumberOfRowsPerPageOfLabel(), 10), rawSettings.getLeftSelectedLabelFields(),
					rawSettings.getRightSelectedLabelFields());
		} else {
			nonPDFSettings = new CSVExcelLabelPrintingSetting("1".equals(rawSettings.getIncludeColumnHeadinginNonPdf()),
					rawSettings.getMainSelectedLabelFields());
		}

		// get the xml value
		String xmlConfig = "";
		try {
			xmlConfig = this.generateXMLFromLabelPrintingSettings(rawSettings.getSettingsName(),
					LabelPrintingFileTypes.getFileTypeByIndex(rawSettings.getGenerateType()).getType(), nonPDFSettings, pdfSettings,
					barcodeSettings);
		} catch (JAXBException e) {
			LabelPrintingController.LOG.error(e.getMessage(), e);
		}

		return xmlConfig;
	}

	private String generateXMLFromLabelPrintingSettings(String name, String outputType, CSVExcelLabelPrintingSetting csvSettings,
			PDFLabelPrintingSetting pdfSettings, BarcodeLabelPrintingSetting barcodeSettings) throws JAXBException {
		LabelPrintingSetting labelPrintingSetting = new LabelPrintingSetting(name, outputType, csvSettings, pdfSettings, barcodeSettings);

		JAXBContext context = JAXBContext.newInstance(LabelPrintingSetting.class);
		Marshaller marshaller = context.createMarshaller();
		StringWriter writer = new StringWriter();
		marshaller.marshal(labelPrintingSetting, writer);

		return writer.toString();
	}

	private String getFileNameAndSetFileLocations(String extension) {
		String fileName = this.userLabelPrinting.getFilename().replaceAll(" ", "-") + extension;
		fileName = FileUtils.sanitizeFileName(fileName);
		String fileNameLocation = this.fieldbookProperties.getUploadDirectory() + File.separator + fileName;

		this.userLabelPrinting.setFilenameDL(fileName);
		this.userLabelPrinting.setFilenameDLLocation(fileNameLocation);
		return fileName;
	}

	/**
	 * Generate trial instances from field map.
	 *
	 * @return the list
	 */
	private List<StudyTrialInstanceInfo> generateTrialInstancesFromFieldMap() {
		List<FieldMapTrialInstanceInfo> trialInstances = this.userLabelPrinting.getFieldMapInfo().getDatasets().get(0).getTrialInstances();
		List<StudyTrialInstanceInfo> studyTrial = new ArrayList<>();

		for (FieldMapTrialInstanceInfo trialInstance : trialInstances) {
			StudyTrialInstanceInfo studyTrialInstance =
					new StudyTrialInstanceInfo(trialInstance, this.userLabelPrinting.getFieldMapInfo().getFieldbookName());
			studyTrial.add(studyTrialInstance);
		}
		return studyTrial;
	}

	/**
	 * Generate trial instances from selected field maps.
	 *
	 * @param fieldMapInfoList the field map info list
	 * @param form             the form
	 * @return the list
	 */
	private List<StudyTrialInstanceInfo> generateTrialInstancesFromSelectedFieldMaps(List<FieldMapInfo> fieldMapInfoList,
			LabelPrintingForm form) {
		List<StudyTrialInstanceInfo> trialInstances = new ArrayList<>();
		String[] fieldMapOrder = form.getUserLabelPrinting().getOrder().split(",");
		for (String fieldmap : fieldMapOrder) {
			String[] fieldMapGroup = fieldmap.split("\\|");
			int order = Integer.parseInt(fieldMapGroup[0]);
			int studyId = Integer.parseInt(fieldMapGroup[1]);
			int datasetId = Integer.parseInt(fieldMapGroup[2]);
			int geolocationId = Integer.parseInt(fieldMapGroup[3]);

			for (FieldMapInfo fieldMapInfo : fieldMapInfoList) {
				if (fieldMapInfo.getFieldbookId().equals(studyId)) {
					fieldMapInfo.getDataSet(datasetId).getTrialInstance(geolocationId).setOrder(order);
					StudyTrialInstanceInfo trialInstance =
							new StudyTrialInstanceInfo(fieldMapInfo.getDataSet(datasetId).getTrialInstance(geolocationId),
									fieldMapInfo.getFieldbookName());
					if (this.userFieldmap.getBlockName() != null && this.userFieldmap.getLocationName() != null) {
						trialInstance.getTrialInstance().setBlockName(this.userFieldmap.getBlockName());
						trialInstance.getTrialInstance().setFieldName(this.userFieldmap.getFieldName());
						trialInstance.getTrialInstance().setLocationName(this.userFieldmap.getLocationName());
					}
					trialInstances.add(trialInstance);
					break;
				}
			}
		}

		Collections.sort(trialInstances, new Comparator<StudyTrialInstanceInfo>() {

			@Override
			public int compare(StudyTrialInstanceInfo o1, StudyTrialInstanceInfo o2) {
				return o1.getTrialInstance().getOrder().compareTo(o2.getTrialInstance().getOrder());
			}
		});

		return trialInstances;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName ()
	 */
	@Override
	public String getContentName() {
		return "LabelPrinting/specifyLabelDetails";
	}

	/**
	 * Sets the user label printing.
	 *
	 * @param userLabelPrinting the new user label printing
	 */
	public void setUserLabelPrinting(UserLabelPrinting userLabelPrinting) {
		this.userLabelPrinting = userLabelPrinting;
	}

	public void setWorkbenchService(WorkbenchService workbenchService) {
		this.workbenchService = workbenchService;
	}

	@Override
	public void setContextUtil(ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

	public CrossExpansionProperties getCrossExpansionProperties() {
		return this.crossExpansionProperties;
	}

	public void setCrossExpansionProperties(CrossExpansionProperties crossExpansionProperties) {
		this.crossExpansionProperties = crossExpansionProperties;
	}

	/**
	 * Enable setting of reportService so we can inject dependency in tests runtime
	 *
	 * @param reportService
	 */
	void setReportService(ReportService reportService) {
		this.reportService = reportService;
	}

}
