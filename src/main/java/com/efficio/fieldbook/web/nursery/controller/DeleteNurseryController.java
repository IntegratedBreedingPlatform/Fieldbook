
package com.efficio.fieldbook.web.nursery.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.UnpermittedDeletionException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

@Controller
@RequestMapping(DeleteNurseryController.URL)
public class DeleteNurseryController extends AbstractBaseFieldbookController {

	public static final String IS_SUCCESS = "isSuccess";

	private static final Logger LOG = LoggerFactory.getLogger(DeleteNurseryController.class);

	public static final String URL = "/NurseryManager/deleteNursery";

	public static final String STUDY_DELETE_NOT_PERMITTED = "study.delete.not.permitted";

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private MessageSource messageSource;
	
	@Resource
	private GermplasmListManager germplasmListManager;

	@Override
	public String getContentName() {
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/{studyId}/{studyType}", method = RequestMethod.POST)
	public Map<String, Object> submitDelete(@PathVariable int studyId, @PathVariable String studyType, Model model, HttpSession session, Locale locale)
			throws MiddlewareException {
		Map<String, Object> results = new HashMap<String, Object>();
		try {
			this.fieldbookMiddlewareService.deleteStudy(studyId, this.contextUtil.getCurrentUserLocalId());
			
			//Set germplasm list status to deleted
			List<GermplasmList> germplasmLists = null;
			if("N".equals(studyType)){
				germplasmLists = this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, GermplasmListType.NURSERY);
				
				//Also set the status of checklist to deleted
				List<GermplasmList> checkGermplasmLists = this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, GermplasmListType.CHECK);
				this.deleteGermplasmList(checkGermplasmLists);
			} else {
				germplasmLists = this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, GermplasmListType.TRIAL);
			}
			
			this.deleteGermplasmList(germplasmLists);
			
			results.put(IS_SUCCESS, "1");

		} catch (UnpermittedDeletionException ude) {
			DeleteNurseryController.LOG.error(ude.getMessage(), ude);
			Integer studyUserId = this.fieldbookMiddlewareService.getStudy(studyId).getUser();
			results.put(IS_SUCCESS, "0");
			results.put("message", this.messageSource.getMessage(DeleteNurseryController.STUDY_DELETE_NOT_PERMITTED,
					new String[] {this.fieldbookMiddlewareService.getOwnerListName(studyUserId)}, locale));
		} catch (Exception e) {
			DeleteNurseryController.LOG.error(e.getMessage(), e);
			results.put(IS_SUCCESS, "0");
		}

		return results;
	}

	private void deleteGermplasmList(List<GermplasmList> germplasmLists) {
		if(germplasmLists != null && !germplasmLists.isEmpty()) {
			this.germplasmListManager.deleteGermplasmList(germplasmLists.get(0));
		}
	}
}
