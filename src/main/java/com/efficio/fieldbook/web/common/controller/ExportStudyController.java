
package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.bean.ExportTrialInstanceBean;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	private MessageSource messageSource;

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
