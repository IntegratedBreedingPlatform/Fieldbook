package com.efficio.etl.service;

import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
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
	DataImportService dummyDataService;

	@Mock
	OntologyService dummyOntologyService;

	@Mock
	OntologyDataManager dummyManager;

	@Mock
	StudyDataManager studyDataManager;

	@Mock
	WorkbenchDataManager workbenchDataManager;

	@Mock
	GermplasmDataManager germplasmDataManager;

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
	public WorkbenchDataManager getWorkbenchDataManagerBean() {
		return workbenchDataManager;
	}

	@Bean
	public GermplasmDataManager getGermplasmDataManagerBean() {
		return germplasmDataManager;
	}

}
