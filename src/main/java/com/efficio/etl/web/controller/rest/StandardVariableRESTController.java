
package com.efficio.etl.web.controller.rest;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.web.bean.VariableDTO;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 3/4/14 Time: 2:03 AM
 */

@Controller
@RequestMapping(value = StandardVariableRESTController.URL)
public class StandardVariableRESTController {

	private static final List<Integer> GERMPLASM_DESCRIPTOR_VARIABLE_IDS_ALLOWED = Arrays.asList(8377, 8250, 8240, 8330, 8340, 8235, 8378, 8201);
	private static final Logger LOG = LoggerFactory.getLogger(StandardVariableRESTController.class);

	public static final String URL = "etl/api/standardVariable";

	@Resource
	private ETLService etlService;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource(name = "ontologyVariableManager")
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Resource
	private ContextUtil contextUtil;

	private static final Map<Integer, VariableFilter> VARIABLE_FILTER_MAP = new HashMap<>();
	private static final VariableFilter MEANS_DATA_VARIABLE_FILTER =
		new org.generationcp.middleware.manager.ontology.daoElements.VariableFilter();
	private static final VariableFilter PLOT_DATA_VARIABLE_FILTER =
		new org.generationcp.middleware.manager.ontology.daoElements.VariableFilter();

	static {

		StandardVariableRESTController.MEANS_DATA_VARIABLE_FILTER.addVariableType(VariableType.ENVIRONMENT_DETAIL);
		StandardVariableRESTController.MEANS_DATA_VARIABLE_FILTER.addVariableType(VariableType.GERMPLASM_DESCRIPTOR);
		StandardVariableRESTController.MEANS_DATA_VARIABLE_FILTER.addVariableType(VariableType.EXPERIMENTAL_DESIGN);
		StandardVariableRESTController.MEANS_DATA_VARIABLE_FILTER.addVariableType(VariableType.TREATMENT_FACTOR);
		StandardVariableRESTController.MEANS_DATA_VARIABLE_FILTER.addVariableType(VariableType.ANALYSIS);

		StandardVariableRESTController.PLOT_DATA_VARIABLE_FILTER.addVariableType(VariableType.ENVIRONMENT_DETAIL);
		StandardVariableRESTController.PLOT_DATA_VARIABLE_FILTER.addVariableType(VariableType.GERMPLASM_DESCRIPTOR);
		StandardVariableRESTController.PLOT_DATA_VARIABLE_FILTER.addVariableType(VariableType.EXPERIMENTAL_DESIGN);
		StandardVariableRESTController.PLOT_DATA_VARIABLE_FILTER.addVariableType(VariableType.TREATMENT_FACTOR);
		StandardVariableRESTController.PLOT_DATA_VARIABLE_FILTER.addVariableType(VariableType.ENVIRONMENT_CONDITION);
		StandardVariableRESTController.PLOT_DATA_VARIABLE_FILTER.addVariableType(VariableType.SELECTION_METHOD);
		StandardVariableRESTController.PLOT_DATA_VARIABLE_FILTER.addVariableType(VariableType.TRAIT);
		StandardVariableRESTController.PLOT_DATA_VARIABLE_FILTER.addVariableType(VariableType.ENTRY_DETAIL);

		StandardVariableRESTController.VARIABLE_FILTER_MAP.put(
			DatasetTypeEnum.MEANS_DATA.getId(),
			StandardVariableRESTController.MEANS_DATA_VARIABLE_FILTER);
		StandardVariableRESTController.VARIABLE_FILTER_MAP.put(
			DatasetTypeEnum.PLOT_DATA.getId(),
			StandardVariableRESTController.PLOT_DATA_VARIABLE_FILTER);

	}

	@ResponseBody
	@RequestMapping("/{id}")
	public VariableDTO getStandardVariableDetails(@PathVariable final int id) {
		if (id == 0) {
			return new VariableDTO();
		}

		return this.etlService.retrieveStandardVariableByID(id);
	}

	@ResponseBody
	@RequestMapping(params = "searchType=name")
	public List<VariableDTO> searchForStandardVariableByName(@RequestParam final String name) {
		final List<VariableDTO> searchResults = new ArrayList<VariableDTO>();
		try {

			// TODO factor logic to ETLService

			final Set<StandardVariable> ontologyResult =
				this.ontologyDataManager.findStandardVariablesByNameOrSynonym(name, this.contextUtil.getCurrentProgramUUID());
			for (final StandardVariable standardVariable : ontologyResult) {
				try {
					final VariableDTO dto = new VariableDTO(standardVariable);
					searchResults.add(dto);
				} catch (final Exception e) {
					LOG.error(e.getMessage(), e);
					// if there's problem with the standard variable's data, dto
					// for it won't be added in search results
				}
			}
		} catch (final MiddlewareException e) {
			LOG.error(e.getMessage(), e);
		}

		return searchResults;
	}

	@ResponseBody
	@RequestMapping(params = "searchType=PMSR")
	public List<VariableDTO> searchForStandardVariableByPropertyScaleMethodRole(
		@RequestParam("property") final String property,
		@RequestParam("scale") final String scale, @RequestParam("method") final String method,
		@RequestParam("phenoType") final String phenoType) {

		// TODO factor logic to ETLService
		final List<VariableDTO> result = new ArrayList<VariableDTO>();

		try {

			final Integer standardVarId = this.ontologyDataManager.getStandardVariableIdByPropertyScaleMethod(property, scale, method);
			StandardVariable var = null;
			if (standardVarId != null) {
				var = this.ontologyDataManager.getStandardVariable(standardVarId, this.contextUtil.getCurrentProgramUUID());
			}

			try {
				if (var != null) {
					result.add(new VariableDTO(var));
				}
			} catch (final Exception e) {
				LOG.error(e.getMessage(), e);
				// no other items need to be done here, as logging of data error
				// is already in the variable dto
			}
		} catch (final MiddlewareException e) {
			LOG.error(e.getMessage(), e);
		}

		return result;
	}

	/**
	 * This API method is used to retrieve all ontology variables valid for importing in the given dataset type (plot data, means data or
	 * none). This is mainly used in "Import Datasets".
	 *
	 * @param datasetTypeId
	 * @return list of VariableDTO
	 */
	@ResponseBody
	@RequestMapping(value = "/datasetType/{datasetTypeId}", method = RequestMethod.GET)
	public List<VariableDTO> retrieveOntologyVariables(@PathVariable final Integer datasetTypeId) {
		final List<VariableDTO> returnVal = new ArrayList<VariableDTO>();
		final VariableFilter middlewareVariableFilter = StandardVariableRESTController.VARIABLE_FILTER_MAP.get(datasetTypeId);
		middlewareVariableFilter.setProgramUuid(this.contextUtil.getCurrentProgramUUID());
		final List<Variable> variables = this.ontologyVariableDataManager.getWithFilter(middlewareVariableFilter);
		for (final Variable variable : variables) {
			if (variable.getVariableTypes().contains(VariableType.GERMPLASM_DESCRIPTOR) &&
				!StandardVariableRESTController.GERMPLASM_DESCRIPTOR_VARIABLE_IDS_ALLOWED.contains(variable.getId())) {
				continue;
			}
			returnVal.add(new VariableDTO(variable));
		}
		return returnVal;
	}

	public void setOntologyVariableDataManager(final OntologyVariableDataManager ontologyVariableDataManager) {
		this.ontologyVariableDataManager = ontologyVariableDataManager;
	}

	public void setContextUtil(final ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

}
