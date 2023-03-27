package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.common.bean.TableHeader;
import org.generationcp.commons.security.AuthorizationService;
import org.generationcp.middleware.api.genotype.SampleGenotypeService;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.generationcp.middleware.enumeration.SampleListType;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.SampleList;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.generationcp.middleware.service.api.SampleListService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SampleListControllerTest {

	private static final int TEST_SAMPLE_LIST_ID = 1;
	private static final String TEST_LIST_NAME = "listSampleName";

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private MessageSource messageSource;

	@Mock
	private SampleListService sampleListService;

	@Mock
	private SampleGenotypeService sampleGenotypeService;

	@Mock
	private AuthorizationService authorizationService;

	@InjectMocks
	private final SampleListController slc = Mockito.spy(new SampleListController());

	@Test
	public void testDisplaySampleList() {
		final SampleList sampleList = Mockito.mock(SampleList.class);
		Mockito.when(sampleList.getListName()).thenReturn(SampleListControllerTest.TEST_LIST_NAME);
		Mockito.when(sampleList.getNotes()).thenReturn(SampleListControllerTest.TEST_LIST_NAME);
		Mockito.when(sampleList.getType()).thenReturn(SampleListType.SAMPLE_LIST);

		Mockito.doReturn(sampleList).when(this.sampleListService).getSampleList(SampleListControllerTest.TEST_SAMPLE_LIST_ID);

		final List<SampleDetailsDTO> sampleDetailsDTOs = this.buildSampleDetailsList(10);
		Mockito.doReturn(sampleDetailsDTOs).when(this.sampleListService).getSampleDetailsDTOs(SampleListControllerTest.TEST_SAMPLE_LIST_ID);
		Mockito.doReturn(true).when(this.authorizationService).hasAnyAuthority(PermissionsEnum.EXPORT_FILE_SAMPLE_LIST_PERMISSIONS);
		Mockito.doReturn(true).when(this.authorizationService).hasAnyAuthority(PermissionsEnum.DELETE_SAMPLES_PERMISSIONS);

		final Model model = Mockito.mock(Model.class);
		this.slc.displaySampleList(SampleListControllerTest.TEST_SAMPLE_LIST_ID, Mockito.mock(HttpServletRequest.class), model);
		Mockito.verify(model).addAttribute(SampleListController.SAMPLE_LIST, sampleDetailsDTOs);
		Mockito.verify(model).addAttribute(SampleListController.TOTAL_NUMBER_OF_GERMPLASMS, sampleDetailsDTOs.size());
		Mockito.verify(model).addAttribute(Matchers.eq(SampleListController.TABLE_HEADER_LIST), Matchers.anyListOf(TableHeader.class));
		Mockito.verify(model).addAttribute("listId", SampleListControllerTest.TEST_SAMPLE_LIST_ID);
		Mockito.verify(model).addAttribute("listName", sampleList.getListName());
		Mockito.verify(model).addAttribute("listNotes", sampleList.getNotes());
		Mockito.verify(model).addAttribute("listType", sampleList.getType().name());
		Mockito.verify(model).addAttribute("hasExportSampleListPermission", true);
		Mockito.verify(model).addAttribute("hasDeleteSamplePermission", true);
		Mockito.verify(model).addAttribute("showImportGenotypes", true);

	}

	private List<SampleDetailsDTO> buildSampleDetailsList(final int numOfsamples) {
		final List<SampleDetailsDTO> sampleDetailsDTOs = new ArrayList<>();
		for (int i = 0; i < numOfsamples; i++) {
			final SampleDetailsDTO sample = new SampleDetailsDTO();
			sample.setGid(i);
			sample.setEntryNo(i);
			sampleDetailsDTOs.add(sample);
		}
		return sampleDetailsDTOs;
	}
}
