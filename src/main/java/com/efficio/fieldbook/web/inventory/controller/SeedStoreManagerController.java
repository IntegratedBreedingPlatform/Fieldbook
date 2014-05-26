package com.efficio.fieldbook.web.inventory.controller;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.inventory.bean.SeedSelection;
import com.efficio.fieldbook.web.inventory.form.SeedStoreForm;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmList;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmMainInfo;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.nursery.form.ManageNurseriesForm;
import com.efficio.fieldbook.web.nursery.form.NurseryDetailsForm;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.LotsResult;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.InventoryService;
import org.generationcp.middleware.service.api.OntologyService;

/**
 * The Class ManageNurseriesController.
 */
@Controller
@RequestMapping({SeedStoreManagerController.URL})
public class SeedStoreManagerController extends AbstractBaseFieldbookController{
    
    private static final Logger LOG = LoggerFactory.getLogger(SeedStoreManagerController.class);
    
    /** The Constant URL. */
    public static final String URL = "/SeedStoreManager";
    public static final String PAGINATION_TEMPLATE = "/Inventory/seedInventoryPagination";

    @Resource
    private FieldbookService fieldbookMiddlewareService;
    
    @Resource
    private InventoryService inventoryMiddlewareService;
    
    @Resource
    private WorkbenchService workbenchService;
    
    /** The message source. */
    @Autowired
    public MessageSource messageSource;
    
    /** The user selection. */
    @Resource
    private SeedSelection seedSelection;
    
    /** The ontology service. */
    @Resource
    private OntologyService ontologyService;
    
    /**
     * Gets the data types.
     *
     * @return the data types
     */
    @ModelAttribute("locationList")
    public List<Location> getLocationList() {
        try {
            List<Location> dataTypesOrig = fieldbookMiddlewareService.getAllLocations();
            List<Location> dataTypes = dataTypesOrig;
            
            return dataTypes;
        }catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }

        return null;
    }
    /**
     * Gets the favorite location list.
     *
     * @return the favorite location list
     */
    @ModelAttribute("favoriteLocationList")
    public List<Location> getFavoriteLocationList() {
        try {
            
            List<Long> locationsIds = workbenchService
                            .getFavoriteProjectLocationIds(getCurrentProjectId());
            List<Location> dataTypes = fieldbookMiddlewareService
                                .getFavoriteLocationByProjectId(locationsIds);
            
            return dataTypes;
        }catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }

        return null;
    }
    
    @ModelAttribute("scaleList")
    public List<Scale> getScaleList() {
        try {
            return ontologyService.getAllScales();
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Shows the manage nurseries screen
     *
     * @param manageNurseriesForm the manage nurseries form
     * @param model the model
     * @param session the session
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("seedStoreForm") SeedStoreForm form, Model model, HttpSession session) {
    	 //session.invalidate();
    	 /*
        try {
           
        }
        catch (MiddlewareQueryException e){
            LOG.error(e.getMessage(), e);
        }
        */
    	return super.show(model);
    }    
    @RequestMapping(value="/ajax", method = RequestMethod.GET)
    public String showAjax(@ModelAttribute("seedStoreForm") SeedStoreForm form, Model model, HttpSession session) {
    	
    	return super.showAjaxPage(model, "Inventory/addLotsModal");
    }
    
    @RequestMapping(value="/displayGermplasmDetails/{listId}", method = RequestMethod.GET)
    public String displayGermplasmDetails(@PathVariable Integer listId,  @ModelAttribute("seedStoreForm") SeedStoreForm form,
            Model model) {
        
        try {
                        
            List<InventoryDetails> inventoryDetailList = inventoryMiddlewareService.getInventoryDetailsByGermplasmList(listId);
            
            getSeedSelection().setInventoryList(inventoryDetailList);
            form.setInventoryList(inventoryDetailList);
            //form.changePage(1);
            form.setCurrentPage(1);
            
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return super.showAjaxPage(model, PAGINATION_TEMPLATE);
    }
    
    @RequestMapping(value="/advance/displayGermplasmDetails/{listId}", method = RequestMethod.GET)
    public String displayAdvanceGermplasmDetails(@PathVariable Integer listId,  @ModelAttribute("seedStoreForm") SeedStoreForm form,
            Model model) {
        
        try {
                        
            List<InventoryDetails> inventoryDetailList = inventoryMiddlewareService.getInventoryDetailsByGermplasmList(listId);
            this.getPaginationListSelection().addFinalAdvancedList(listId.toString(), inventoryDetailList);
            
            getSeedSelection().setInventoryList(inventoryDetailList);
            form.setListId(listId.toString());
            form.setInventoryList(inventoryDetailList);
            //form.changePage(1);
            form.setCurrentPage(1);
            form.setGidList(Integer.toString(listId));
            
            
            
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return super.showAjaxPage(model, "/NurseryManager/savedFinalAdvanceList");
    }
    
    /**
     * Get for the pagination of the list
     *
     * @param form the form
     * @param model the model
     * @return the string
     */
    @RequestMapping(value="/page/{pageNum}", method = RequestMethod.GET)
    public String getPaginatedList(@PathVariable int pageNum
            , @ModelAttribute("seedStoreForm") SeedStoreForm form, Model model) {
        List<InventoryDetails> inventoryDetailsList = getSeedSelection().getInventoryList();
        if(inventoryDetailsList != null){
            form.setInventoryList(inventoryDetailsList);
            form.setCurrentPage(pageNum);
        }
        return super.showAjaxPage(model, PAGINATION_TEMPLATE);
    }
    
    @RequestMapping(value="/page/advance/{pageNum}", method = RequestMethod.GET)
    public String getAdvancePaginatedList(@PathVariable int pageNum
            , @ModelAttribute("seedStoreForm") SeedStoreForm form, Model model, HttpServletRequest req) {
    	String listIdentifier = req.getParameter("listIdentifier");
    	
        List<InventoryDetails> inventoryDetailsList = getPaginationListSelection().getFinalAdvancedList(listIdentifier);
        if(inventoryDetailsList != null){
            form.setInventoryList(inventoryDetailsList);
            form.setCurrentPage(pageNum);
        }
        form.setListId(listIdentifier);
        return super.showAjaxPage(model, "/Inventory/seedAdvanceInventoryPagination");
    }

    private String createUnsavedGidList(List<Integer> gids) {
        StringBuilder gidList = new StringBuilder();
        int counter = 0;
        for (Integer gid: gids) {
            for (InventoryDetails inventory : seedSelection.getInventoryList()) {
                if (inventory.getGid().equals(gid)) {
                    gidList.append(inventory.getGermplasmName());
                    break;
                }
            }
            if (counter < gids.size()-1) {
                gidList.append(", ");
            }
            counter++;
        }
        return gidList.toString();
    }
    
    @ResponseBody
    @RequestMapping(value="/save/lots", method = RequestMethod.POST)
    public Map<String, Object> saveLots(@ModelAttribute("seedStoreForm") SeedStoreForm form,
            Model model, Locale local) {
        Map<String, Object> result = new HashMap<String, Object>();
        List<Integer> gidList = new ArrayList<Integer>();

        for (String gid : form.getGidList().split(",")) {
            gidList.add(Integer.parseInt(gid));
        }
        
        try {
            LotsResult lotsResult = inventoryMiddlewareService.addLots(gidList, form.getInventoryLocationId(), form.getInventoryScaleId(), form.getInventoryComments(), workbenchService.getCurrentIbdbUserId(this.getCurrentProjectId()));
            if (gidList.size() == lotsResult.getGidsSkipped().size()) {
                result.put("message", messageSource
                        .getMessage("seed.inventory.add.lot.all.combinations.exist", null, local));
                result.put("success", 0);
            } else if (lotsResult.getGidsSkipped().size() != 0) {
                String germplasmNames = createUnsavedGidList(lotsResult.getGidsSkipped());
                result.put("message", messageSource
                        .getMessage("seed.inventory.add.lot.save.success.with.warning", new Object[] {germplasmNames}, local));
                result.put("success", 1);
            } else {
                result.put("message", messageSource
                        .getMessage("seed.inventory.add.lot.save.success", null, local));
                result.put("success", 1);
            }
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
            result.put("message", "error: " + e.getMessage());
            result.put("success", 0);
        }
        
        return result;
    }
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "Inventory/main";
    }
    
    /**
     * Gets the form.
     *
     * @return the form
     */
    @ModelAttribute("form")
    public SeedStoreForm getForm() {
        return new SeedStoreForm();
    }
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getUserSelection()
     */
    public SeedSelection getSeedSelection() {
        return this.seedSelection;
    }
    
   
}