
package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.bean.ExportTrialInstanceBean;
import com.efficio.fieldbook.web.util.SettingsUtil;
import net.sf.jasperreports.engine.JRException;
import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.pojo.CustomReportType;
import org.generationcp.commons.reports.service.JasperReportService;
import org.generationcp.commons.service.GermplasmExportService;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.reports.BuildReportException;
import org.generationcp.middleware.reports.Reporter;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.ReportService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
@RequestMapping(ExportStudyController.URL)
public class ExportStudyController extends AbstractBaseFieldbookController {

	static final String CONTENT_TYPE = "contentType";
	private static final String FILENAME = "filename";
	private static final String OUTPUT_FILENAME = "outputFilename";
	private static final String ERROR_MESSAGE = "errorMessage";
	static final String IS_SUCCESS = "isSuccess";
	private static final Logger LOG = LoggerFactory.getLogger(ExportStudyController.class);
	public static final String URL = "/ExportManager";
	private static final String EXPORT_TRIAL_INSTANCE = "Common/includes/exportTrialInstance";

	@Resource
	private UserSelection studySelection;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	@Resource
	private ReportService reportService;

	@Resource
	private MessageSource messageSource;

	@Resource
	private JasperReportService jasperReportService;

	@Resource
	private StudyDataManager studyDataManager;

	@Override
	public String getContentName() {
		return null;
	}

	@RequestMapping(value = "/download/file", method = RequestMethod.GET)
	public ResponseEntity<FileSystemResource> downloadFile(final HttpServletRequest req) throws UnsupportedEncodingException {

		final String outputFilename =
				new String(req.getParameter(ExportStudyController.OUTPUT_FILENAME).getBytes(StandardCharsets.ISO_8859_1),
					StandardCharsets.UTF_8);
		final String filename = new String(req.getParameter(ExportStudyController.FILENAME).getBytes(StandardCharsets.ISO_8859_1),
			StandardCharsets.UTF_8);

		return FieldbookUtil.createResponseEntityForFileDownload(outputFilename, filename);

	}

	@ResponseBody
	@RequestMapping(value = "/export/custom/report", method = RequestMethod.POST)
	public String exportCustomReport(@RequestBody final Map<String, String> data, final HttpServletRequest req,
			final HttpServletResponse response) {
		final String studyId = this.getStudyId(data);
		final String reportCode = data.get("customReportCode");
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String fileName = "";
		String outputFilename = "";
		final Reporter rep;
		final Map<String, Object> results = new HashMap<>();
		try {

			rep = this.reportService.getStreamReport(reportCode, Integer.parseInt(studyId),
					this.contextUtil.getProjectInContext().getProjectName(), baos);

			fileName = rep.getFileName();
			outputFilename = this.fieldbookProperties.getUploadDirectory() + File.separator + fileName;

			final File reportFile = new File(outputFilename);
			baos.writeTo(new FileOutputStream(reportFile));

			results.put(ExportStudyController.IS_SUCCESS, true);
			results.put(ExportStudyController.OUTPUT_FILENAME, outputFilename);
			results.put(ExportStudyController.FILENAME, SettingsUtil.cleanSheetAndFileName(fileName));
			results.put(ExportStudyController.CONTENT_TYPE, response.getContentType());

		} catch (final NumberFormatException | JRException | IOException | BuildReportException e) {
			ExportStudyController.LOG.error(e.getMessage(), e);
			results.put(ExportStudyController.IS_SUCCESS, false);
			results.put(ExportStudyController.ERROR_MESSAGE, this.messageSource.getMessage("export.study.error", null, Locale.ENGLISH));
		}

		return super.convertObjectToJson(results);
	}

	protected String getStudyId(final Map<String, String> data) {
		return data.get("studyExportId");
	}

	protected UserSelection getUserSelection() {
		return this.studySelection;
	}


	/**
	 * Load initial germplasm tree.
	 *
	 * @return the string
	 */
	@RequestMapping(value = "/trial/instances/{studyId}", method = RequestMethod.GET)
	public String saveList(@PathVariable final int studyId, final Model model, final HttpSession session) {

		final List<ExportTrialInstanceBean> trialInstances = new ArrayList<>();
		final List<Integer> trialIds = new ArrayList<>();
		trialIds.add(studyId);
		final List<FieldMapInfo> fieldMapInfoList;

		fieldMapInfoList = this.fieldbookMiddlewareService.getFieldMapInfoOfTrial(trialIds, this.crossExpansionProperties);

		if (fieldMapInfoList != null && fieldMapInfoList.get(0).getDatasets() != null
				&& fieldMapInfoList.get(0).getDatasets().get(0).getTrialInstances() != null) {
			for (int i = 0; i < fieldMapInfoList.get(0).getDatasets().get(0).getTrialInstances().size(); i++) {
				final FieldMapTrialInstanceInfo info = fieldMapInfoList.get(0).getDatasets().get(0).getTrialInstances().get(i);
				trialInstances.add(new ExportTrialInstanceBean(info.getTrialInstanceNo(), info.getLocationName(), info.getInstanceId()));
			}
		}
		model.addAttribute("trialInstances", trialInstances);
		return super.showAjaxPage(model, ExportStudyController.EXPORT_TRIAL_INSTANCE);
	}

	@ResponseBody
	@RequestMapping(value = "/custom/{studyId}/reports", method = RequestMethod.GET)
	public List<CustomReportType> getCustomReports(@PathVariable final int studyId) {
		final StudyDetails studyDetails = this.studyDataManager.getStudyDetails(studyId);
		// DO NOT remove this condition. Reports are organized based on the study type
		// It needs to be discussed with IBP whenever they want to bring custom reports back
		if (StudyTypeDto.NURSERY_NAME.equalsIgnoreCase(studyDetails.getStudyType().getName())) {
			return this.getCustomReportTypes(ToolSection.FB_NURSE_MGR_CUSTOM_REPORT.name());
		} else if (StudyTypeDto.TRIAL_NAME.equalsIgnoreCase(studyDetails.getStudyType().getName())) {
			return this.getCustomReportTypes(ToolSection.FB_TRIAL_MGR_CUSTOM_REPORT.name());
		}

		return new ArrayList<>();
	}

	private List<CustomReportType> getCustomReportTypes(final String name) {
		return this.jasperReportService.getCustomReportTypes(name, ToolName.FIELDBOOK_WEB.getName());
	}

	protected void setUserSelection(final UserSelection userSelection) {
		this.studySelection = userSelection;
	}



	protected void setFieldbookMiddlewareService(final FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}


	public void setCrossExpansionProperties(final CrossExpansionProperties crossExpansionProperties) {
		this.crossExpansionProperties = crossExpansionProperties;
	}
}
