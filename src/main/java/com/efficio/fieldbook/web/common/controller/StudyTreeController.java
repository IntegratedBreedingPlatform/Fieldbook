
package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.commons.service.UserTreeStateService;
import org.generationcp.commons.util.TreeViewUtil;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping(StudyTreeController.URL)
public class StudyTreeController extends AbstractBaseFieldbookController {

	private static final Logger LOG = LoggerFactory.getLogger(StudyTreeController.class);
	public static final String URL = "/StudyTreeManager";
	public static final String LOCAL = "LOCAL";
	private static final String HAS_OBSERVATIONS = "hasObservations";
	public static final String IS_SUCCESS = "isSuccess";
	public static final String MESSAGE = "message";
	public static final String NEW_FOLDER_ID = "newFolderId";

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private StudyDataManager studyDataManager;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Resource
	private UserTreeStateService userTreeStateService;

	@ResponseBody
	@RequestMapping(value = "/loadInitialTree/{isFolderOnly}", method = RequestMethod.GET)
	public String loadInitialTree(@PathVariable final String isFolderOnly) {
		try {
			final List<TreeNode> rootNodes = new ArrayList<>();
			final String localName = AppConstants.STUDIES.getString();
			final TreeNode localTreeNode = new TreeNode(StudyTreeController.LOCAL, localName, true, "lead",
					AppConstants.FOLDER_ICON_PNG.getString(), this.getCurrentProgramUUID());
			rootNodes.add(localTreeNode);
			return TreeViewUtil.convertTreeViewToJson(rootNodes);
		} catch (final Exception e) {
			StudyTreeController.LOG.error(e.getMessage(), e);
		}
		return "[]";
	}

	private List<TreeNode> getChildNodes(final String parentKey, final boolean isFolderOnly) {
		List<TreeNode> childNodes = new ArrayList<>();
		if (parentKey != null && !"".equals(parentKey)) {
			try {
				if (StudyTreeController.LOCAL.equals(parentKey)) {
					final List<Reference> rootFolders = this.studyDataManager.getRootFolders(this.getCurrentProgramUUID());
					childNodes = TreeViewUtil.convertStudyFolderReferencesToTreeView(rootFolders, true, isFolderOnly);
				} else if (NumberUtils.isNumber(parentKey)) {
					childNodes = this.getChildrenTreeNodes(parentKey, isFolderOnly);
				} else {
					StudyTreeController.LOG.error("parentKey = " + parentKey + " is not a number");
				}
			} catch (final Exception e) {
				StudyTreeController.LOG.error(e.getMessage(), e);
			}
		}
		return childNodes;
	}

	private List<TreeNode> getChildrenTreeNodes(final String parentKey, final boolean isFolderOnly) {
		final int parentId = Integer.parseInt(parentKey);
		final List<Reference> folders = this.studyDataManager.getChildrenOfFolder(parentId, this.getCurrentProgramUUID());

		final List<TreeNode> childNodes = TreeViewUtil.convertStudyFolderReferencesToTreeView(folders, true, isFolderOnly);
		return childNodes;
	}

	@ResponseBody
	@RequestMapping(value = "/expandTree/{parentKey}/{isFolderOnly}", method = RequestMethod.GET)
	public String expandTree(@PathVariable final String parentKey, @PathVariable final String isFolderOnly) {
		final boolean isFolderOnlyBool = "1".equalsIgnoreCase(isFolderOnly) ? true : false;
		try {
			final List<TreeNode> childNodes = this.getChildNodes(parentKey, isFolderOnlyBool);
			return TreeViewUtil.convertTreeViewToJson(childNodes);
		} catch (final Exception e) {
			StudyTreeController.LOG.error(e.getMessage(), e);
		}
		return "[]";
	}

	@ResponseBody
	@RequestMapping(value = "/retrieveChildren/{parentKey}/{isFolderOnly}", method = RequestMethod.GET)
	public String retrieveChildren(@PathVariable final String parentKey, @PathVariable final String isFolderOnly) {
		final boolean isFolderOnlyBool = "1".equalsIgnoreCase(isFolderOnly) ? true : false;
		try {
			if (StudyTreeController.LOCAL.equals(parentKey)) {
				return this.getRootFolders(isFolderOnlyBool);
			} else if (NumberUtils.isNumber(parentKey)) {

				final int parentId = Integer.parseInt(parentKey);
				final List<Reference> folders =
						this.studyDataManager.getChildrenOfFolder(parentId, this.getCurrentProgramUUID());
				return TreeViewUtil.convertStudyFolderReferencesToJson(folders, true, isFolderOnlyBool);

			} else {
				StudyTreeController.LOG.error("parentKey = " + parentKey + " is not a number");
			}

		} catch (final Exception e) {
			StudyTreeController.LOG.error(e.getMessage(), e);
		}

		return "[]";
	}

	private String getRootFolders(final boolean isFolderOnly) {
		try {
			final List<Reference> rootFolders =
					this.studyDataManager.getRootFolders(this.getCurrentProgramUUID());
			return TreeViewUtil.convertStudyFolderReferencesToJson(rootFolders, true, isFolderOnly);
		} catch (final Exception e) {
			StudyTreeController.LOG.error(e.getMessage(), e);
		}
		return "[]";
	}

	@ResponseBody
	@RequestMapping(value = "/has/observations/{studyId}", method = RequestMethod.GET)
	public Map<String, String> hasObservations(@PathVariable final int studyId) {
		final Map<String, String> dataResults = new HashMap<>();

		final int datasetId;
		try {
			datasetId = this.fieldbookMiddlewareService.getMeasurementDatasetId(studyId);
			final long observationCount = this.fieldbookMiddlewareService.countObservations(datasetId);
			if (observationCount > 0) {
				dataResults.put(StudyTreeController.HAS_OBSERVATIONS, "1");
			} else {
				dataResults.put(StudyTreeController.HAS_OBSERVATIONS, "0");
			}
		} catch (final MiddlewareException e) {
			StudyTreeController.LOG.error(e.getMessage(), e);
			dataResults.put(StudyTreeController.HAS_OBSERVATIONS, "0");
		}

		return dataResults;
	}

	@ResponseBody
	@RequestMapping(value = "/isNameUnique", method = RequestMethod.POST)
	public Map<String, Object> isNameUnique(final HttpServletRequest req) {
		final String studyId = req.getParameter("studyId");
		final String studyName = req.getParameter("name");
		final Integer studyIdInt = Integer.valueOf(studyId);

		final Map<String, Object> resultsMap = new HashMap<>();
		try {

			final Integer studyIdDb = this.fieldbookMiddlewareService.getProjectIdByNameAndProgramUUID(HtmlUtils.htmlEscape(studyName),
					this.getCurrentProgramUUID());

			if (studyIdDb == null) {
				// meaning there is no study
				resultsMap.put(StudyTreeController.IS_SUCCESS, "1");
			} else if (studyIdInt.intValue() == 0 && studyIdDb != 0) {
				// meaning new
				resultsMap.put(StudyTreeController.IS_SUCCESS, "0");
			} else if (studyIdInt.intValue() == studyIdDb) {
				resultsMap.put(StudyTreeController.IS_SUCCESS, "1");
			}

		} catch (final Exception e) {
			StudyTreeController.LOG.error(e.getMessage(), e);
			resultsMap.put(StudyTreeController.IS_SUCCESS, "0");
			resultsMap.put(StudyTreeController.MESSAGE, e.getMessage());
		}
		return resultsMap;

	}

	@ResponseBody
	@RequestMapping(value = "/addStudyFolder", method = RequestMethod.POST)
	public Map<String, Object> addStudyFolder(final HttpServletRequest req) {

		final Map<String, Object> resultsMap = new HashMap<>();

		try {

			final TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {

				@Override
				protected void doInTransactionWithoutResult(final TransactionStatus status) {

					final String parentKey = req.getParameter("parentFolderId");
					final String folderName = req.getParameter("folderName");
					final Locale locale = LocaleContextHolder.getLocale();

					if (folderName.equalsIgnoreCase(AppConstants.STUDIES.getString())) {
						throw new MiddlewareQueryException(
								StudyTreeController.this.messageSource.getMessage("folder.name.not.unique", null, locale));
					}
					Integer parentFolderId = Integer.parseInt(parentKey);
					if (StudyTreeController.this.studyDataManager.isStudy(parentFolderId)) {
						final DmsProject project = StudyTreeController.this.studyDataManager.getParentFolder(parentFolderId);
						if (project == null) {
							throw new MiddlewareQueryException("Parent folder cannot be null");
						}
						parentFolderId = project.getProjectId();
					}
					final int newFolderId = StudyTreeController.this.studyDataManager.addSubFolder(parentFolderId, folderName, folderName,
							StudyTreeController.this.getCurrentProgramUUID(), folderName);
					resultsMap.put(StudyTreeController.IS_SUCCESS, "1");
					resultsMap.put(StudyTreeController.NEW_FOLDER_ID, Integer.toString(newFolderId));

				}
			});

		} catch (final Exception e) {

			StudyTreeController.LOG.error(e.getMessage(), e);
			resultsMap.put(StudyTreeController.IS_SUCCESS, "0");
			resultsMap.put(StudyTreeController.MESSAGE, e.getMessage());

		}
		return resultsMap;

	}

	@ResponseBody
	@RequestMapping(value = "/renameStudyFolder", method = RequestMethod.POST)
	public Map<String, Object> renameStudyFolder(final HttpServletRequest req) {
		final Map<String, Object> resultsMap = new HashMap<>();
		final Locale locale = LocaleContextHolder.getLocale();
		try {
			final String newFolderName = req.getParameter("newFolderName");
			final String folderId = req.getParameter("folderId");
			if (newFolderName.equalsIgnoreCase(AppConstants.STUDIES.getString())) {
				throw new MiddlewareQueryException(this.messageSource.getMessage("folder.name.not.unique", null, locale));
			}
			this.studyDataManager.renameSubFolder(newFolderName, Integer.parseInt(folderId), this.getCurrentProgramUUID());
			resultsMap.put(StudyTreeController.IS_SUCCESS, "1");
		} catch (final MiddlewareQueryException e) {
			StudyTreeController.LOG.error(e.getMessage(), e);
			resultsMap.put(StudyTreeController.IS_SUCCESS, "0");
			resultsMap.put(StudyTreeController.MESSAGE, e.getMessage());
		}
		return resultsMap;
	}

	@ResponseBody
	@RequestMapping(value = "/renameStudy", method = RequestMethod.POST)
	public Map<String, Object> renameStudy(final HttpServletRequest req) {
		final Map<String, Object> resultsMap = new HashMap<>();
		try {
			final String newStudyName = req.getParameter("newStudyName");
			final String studyId = req.getParameter("studyId");
			this.studyDataManager.renameStudy(newStudyName, Integer.parseInt(studyId), this.getCurrentProgramUUID());
			resultsMap.put(StudyTreeController.IS_SUCCESS, "1");
		} catch (final MiddlewareQueryException e) {
			StudyTreeController.LOG.error(e.getMessage(), e);
			resultsMap.put(StudyTreeController.IS_SUCCESS, "0");
			resultsMap.put(StudyTreeController.MESSAGE, e.getMessage());
		}
		return resultsMap;
	}

	@ResponseBody
	@RequestMapping(value = "/deleteStudyFolder", method = RequestMethod.POST)
	public Map<String, Object> deleteStudyListFolder(final HttpServletRequest req) {
		final Map<String, Object> resultsMap = new HashMap<>();
		final Locale locale = LocaleContextHolder.getLocale();
		try {
			final String folderId = req.getParameter("folderId");
			this.studyDataManager.deleteEmptyFolder(Integer.parseInt(folderId), this.getCurrentProgramUUID());
			resultsMap.put(StudyTreeController.IS_SUCCESS, "1");
		} catch (final MiddlewareQueryException e) {
			StudyTreeController.LOG.error(e.getMessage(), e);
			resultsMap.put(StudyTreeController.IS_SUCCESS, "0");
			resultsMap.put(StudyTreeController.MESSAGE,
					this.messageSource.getMessage("browse.nursery.delete.folder.has.children", null, locale));

		}
		return resultsMap;
	}

	@ResponseBody
	@RequestMapping(value = "/isFolderEmpty/{folderId}", method = RequestMethod.POST)
	public Map<String, Object> isFolderEmpty(@RequestBody final Map<String, String> data, @PathVariable final String folderId) {
		final String folderName = data.get("folderName");
		final Map<String, Object> resultsMap = new HashMap<>();
		final Locale locale = LocaleContextHolder.getLocale();
		boolean isFolderEmpty = this.studyDataManager.isFolderEmpty(Integer.parseInt(folderId), this.getCurrentProgramUUID());
		if (isFolderEmpty) {
			resultsMap.put(StudyTreeController.IS_SUCCESS, "1");
		} else {
			resultsMap.put(StudyTreeController.IS_SUCCESS, "0");
			isFolderEmpty = this.studyDataManager.isFolderEmpty(Integer.parseInt(folderId), this.getCurrentProgramUUID());
			final String message;
			if (!isFolderEmpty) {
				message = "browse.study.delete.folder.not.empty";
			} else {
				message = "browse.study.delete.folder.contains.studies";
			}
			resultsMap.put(StudyTreeController.MESSAGE, this.messageSource.getMessage(message, new Object[] {folderName}, locale));
		}
		return resultsMap;
	}

	@ResponseBody
	@RequestMapping(value = "/moveStudyFolder", method = RequestMethod.POST)
	public Map<String, Object> moveStudyFolder(final HttpServletRequest req) {
		final String sourceId = req.getParameter("sourceId");
		final String targetId = req.getParameter("targetId");
		final Map<String, Object> resultsMap = new HashMap<>();
		try {
			this.studyDataManager.moveDmsProject(Integer.parseInt(sourceId), Integer.parseInt(targetId));
			resultsMap.put(StudyTreeController.IS_SUCCESS, "1");
		} catch (final MiddlewareQueryException e) {
			StudyTreeController.LOG.error(e.getMessage(), e);
			resultsMap.put(StudyTreeController.IS_SUCCESS, "0");
			resultsMap.put(StudyTreeController.MESSAGE, e.getMessage());
		}
		return resultsMap;
	}

	@ResponseBody
	@RequestMapping(value = "/save/state/{type}")
	public String saveTreeState(@PathVariable final String type, @RequestParam(value = "expandedNodes[]") final String[] expandedNodes) {
		StudyTreeController.LOG.debug("Save the debug nodes");
		String status = "OK";
		try {
			this.userTreeStateService
				.saveOrUpdateUserProgramTreeState(this.contextUtil.getCurrentWorkbenchUserId(), this.getCurrentProgramUUID(), type,
					Arrays.asList(expandedNodes));
		} catch (final MiddlewareQueryException e) {
			StudyTreeController.LOG.error(e.getMessage(), e);
			status = "ERROR";
		}
		return status;
	}

	@ResponseBody
	@RequestMapping(value = "/retrieve/state/{type}", method = RequestMethod.GET)
	public String retrieveTreeState(@PathVariable final String type) {

		final List<String> stateList;
		final int userID = this.contextUtil.getCurrentWorkbenchUserId();
		final String programUUID = this.getCurrentProgramUUID();
		stateList = this.userTreeStateService.getUserProgramTreeStateByUserIdProgramUuidAndType(userID, programUUID, type);

		return super.convertObjectToJson(stateList);
	}

	protected String getCurrentProgramUUID() {
		return this.contextUtil.getCurrentProgramUUID();
	}

	void setFieldbookMiddlewareService(final FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	void setStudyDataManager(final StudyDataManager studyDataManager) {
		this.studyDataManager = studyDataManager;
	}

	void setMessageSource(final MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public String getContentName() {
		return null;
	}

	void setRequest(final HttpServletRequest request) {
		this.request = request;
	}
}
