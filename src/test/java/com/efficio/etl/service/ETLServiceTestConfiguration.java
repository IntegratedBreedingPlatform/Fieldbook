package com.efficio.etl.service;

import org.generationcp.middleware.api.tool.ToolService;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.api.role.RoleService;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.OntologyService;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

/**
 * Created by clarysabel on 1/7/19.
 */
@Configuration
@ImportResource("com/efficio/etl/service/ETLServiceTest-context.xml")
@Profile("etl-service-test")
public class ETLServiceTestConfiguration {

	@Mock
	private DataImportService dummyDataService;

	@Mock
	private OntologyService dummyOntologyService;

	@Mock
	private OntologyDataManager dummyManager;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private RoleService roleService;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private ToolService toolService;

	public ETLServiceTestConfiguration() {
		MockitoAnnotations.initMocks(this);
	}

	@Bean
	public DataImportService getDataImportServiceBean() {
		return dummyDataService;
	}

	@Bean
	public OntologyService getOntologyServiceBean() {
		return dummyOntologyService;
	}

	@Bean
	public OntologyDataManager getOntologyDataManagerBean() {
		return dummyManager;
	}

	@Bean
	public StudyDataManager getStudyDataManagerBean() {
		return studyDataManager;
	}

	@Bean
	public RoleService getRoleServiceBean() {
		return roleService;
	}

	@Bean
	public GermplasmDataManager getGermplasmDataManagerBean() {
		return germplasmDataManager;
	}

	@Bean
	public ToolService getToolService() {
		return this.toolService;
	}

}
