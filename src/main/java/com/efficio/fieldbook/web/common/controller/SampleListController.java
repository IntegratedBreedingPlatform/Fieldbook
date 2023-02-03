package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.common.bean.TableHeader;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.security.AuthorizationService;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.SampleList;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.generationcp.middleware.service.api.SampleListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Transactional
@Controller
@RequestMapping(SampleListController.URL)
public class SampleListController {

	private static final Logger LOG = LoggerFactory.getLogger(SampleListController.class);

	public static final String URL = "/sample/list";
	private static final String SAVED_FINAL_LIST = "/StudyManager/savedFinalList";
	protected static final String TABLE_HEADER_LIST = "tableHeaderList";
	protected static final String IS_SUBOBSERVATION = "isSubobservation";
	protected static final String SAMPLE_LIST = "sampleList";
	private static final String SAMPLE_NAME = "sample.list.sample.name";
	private static final String TAKEN_BY = "sample.list.taken.by";
	private static final String SAMPLING_DATE = "sample.list.sampling.date";
	private static final String SAMPLE_UID = "sample.list.sample.uid";
	private static final String PLATE_ID = "sample.list.sample.plate.id";
	private static final String WELL = "sample.list.sample.well";
	private static final String OBS_UNIT_ID = "sample.list.obs.unit.id";
	private static final String PLOT_NO = "sample.list.plot.no";
	private static final String DESIGNATION = "seed.entry.designation";
	private static final String SAMPLE_NO = "sample.list.sample.no";
	protected static final String TOTAL_NUMBER_OF_GERMPLASMS = "totalNumberOfGermplasms";
	private static final String SAMPLE_ENTRY = "sample.list.sample.entry.no";

	@Resource
	private MessageSource messageSource;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private SampleListService sampleListService;

	@Autowired
	protected AuthorizationService authorizationService;

	@RequestMapping(value = "/sampleList/{listId}", method = RequestMethod.GET)
	public String displaySampleList(@PathVariable final Integer listId, final HttpServletRequest req, final Model model) {
		this.processSampleList(listId, req, model);
		return SampleListController.SAVED_FINAL_LIST;
	}

	private void processSampleList(final Integer listId, final HttpServletRequest req, final Model model) {
		try {

			final SampleList sampleList = this.sampleListService.getSampleList(listId);
			final String name = sampleList.getListName();
			final String notes = sampleList.getNotes();
			final String type = sampleList.getType().name();
			final List<SampleDetailsDTO> sampleDetailsDTOs = this.sampleListService.getSampleDetailsDTOs(listId);
			final String subObservationVariableName = this.sampleListService.getObservationVariableName(listId);
			model.addAttribute(SampleListController.SAMPLE_LIST, sampleDetailsDTOs);
			model.addAttribute(SampleListController.TOTAL_NUMBER_OF_GERMPLASMS, sampleDetailsDTOs.size());

			final boolean isSubobservation = !StringUtils.equals(TermId.PLOT_NO.name(), subObservationVariableName);
			model.addAttribute(SampleListController.TABLE_HEADER_LIST,
				this.getSampleListTableHeaders(subObservationVariableName, isSubobservation));
			model.addAttribute(SampleListController.IS_SUBOBSERVATION, isSubobservation);

			model.addAttribute("listId", listId);
			model.addAttribute("listName", name);
			model.addAttribute("listNotes", notes);
			model.addAttribute("listType", type);

			model.addAttribute("hasManageStudiesPermission",
				this.authorizationService.isSuperAdminUser()
					|| this.authorizationService.hasAnyAuthority(PermissionsEnum.MANAGE_STUDIES_PERMISSIONS));
		} catch (final MiddlewareQueryException e) {
			SampleListController.LOG.error(e.getMessage(), e);
		}
	}

	private List<TableHeader> getSampleListTableHeaders(final String subObservationVariableName, final boolean isSubobservation) {
		final Locale locale = LocaleContextHolder.getLocale();
		final List<TableHeader> tableHeaderList = new ArrayList<>();

		tableHeaderList.add(new TableHeader(this.messageSource.getMessage(SampleListController.SAMPLE_ENTRY, null, locale),
			this.messageSource.getMessage(SampleListController.SAMPLE_ENTRY, null, locale)));
		this.getCommonHeaders(locale, tableHeaderList);
		tableHeaderList.add(new TableHeader(subObservationVariableName, subObservationVariableName));
		// only add separate PLOT_NO on subobservations where parent plot no will be retrieved
		if (isSubobservation) {
			tableHeaderList.add(new TableHeader(this.messageSource.getMessage(SampleListController.PLOT_NO, null, locale),
				this.messageSource.getMessage(SampleListController.PLOT_NO, null, locale)));
		}
		tableHeaderList.add(new TableHeader(this.messageSource.getMessage(SampleListController.SAMPLE_NO, null, locale),
			this.messageSource.getMessage(SampleListController.SAMPLE_NO, null, locale)));
		tableHeaderList.add(new TableHeader(this.messageSource.getMessage(SampleListController.SAMPLE_NAME, null, locale),
			this.messageSource.getMessage(SampleListController.SAMPLE_NAME, null, locale)));
		tableHeaderList.add(new TableHeader(this.messageSource.getMessage(SampleListController.TAKEN_BY, null, locale),
			this.messageSource.getMessage(SampleListController.TAKEN_BY, null, locale)));
		tableHeaderList.add(new TableHeader(this.messageSource.getMessage(SampleListController.SAMPLING_DATE, null, locale),
			this.messageSource.getMessage(SampleListController.SAMPLING_DATE, null, locale)));
		tableHeaderList.add(new TableHeader(this.messageSource.getMessage(SampleListController.SAMPLE_UID, null, locale),
			this.messageSource.getMessage(SampleListController.SAMPLE_UID, null, locale), false));
		tableHeaderList.add(new TableHeader(this.messageSource.getMessage(SampleListController.PLATE_ID, null, locale),
			this.messageSource.getMessage(SampleListController.PLATE_ID, null, locale), false));
		tableHeaderList.add(new TableHeader(this.messageSource.getMessage(SampleListController.WELL, null, locale),
			this.messageSource.getMessage(SampleListController.WELL, null, locale), false));
		tableHeaderList.add(new TableHeader(this.messageSource.getMessage(SampleListController.OBS_UNIT_ID, null, locale),
			this.messageSource.getMessage(SampleListController.OBS_UNIT_ID, null, locale), false));

		return tableHeaderList;
	}

	private void getCommonHeaders(final Locale locale, final List<TableHeader> tableHeaderList) {
		tableHeaderList.add(new TableHeader(ColumnLabels.DESIGNATION.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage(SampleListController.DESIGNATION, null, locale)));
	}

}
