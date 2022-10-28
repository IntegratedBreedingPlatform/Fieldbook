
package com.efficio.fieldbook.web.trial.controller;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.common.bean.TableHeader;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.naming.impl.AdvancingSourceListFactory;
import org.generationcp.middleware.ruleengine.naming.service.NamingConventionService;
import com.efficio.fieldbook.web.trial.bean.AdvanceType;
import com.efficio.fieldbook.web.trial.bean.AdvancingStudy;
import com.efficio.fieldbook.web.trial.form.AdvancingStudyForm;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.google.common.collect.Lists;
import org.apache.commons.lang.math.RandomUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.ruleengine.pojo.ImportedGermplasm;
import org.generationcp.middleware.ruleengine.pojo.AdvanceGermplasmChangeDetail;
import org.generationcp.middleware.ruleengine.pojo.AdvancingSource;
import org.generationcp.middleware.ruleengine.pojo.AdvancingSourceList;
import org.generationcp.middleware.ruleengine.RuleException;
import org.generationcp.middleware.ruleengine.generator.SeedSourceGenerator;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.MethodType;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.study.StudyInstanceService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class AdvancingControllerTest {

	private static final String STUDY_NAME = "STUDY:ABC";

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Mock
	private PaginationListSelection paginationListSelection;

	@Mock
	private UserSelection userSelection;

	@Mock
	private MessageSource messageSource;

    @Mock
    private HttpServletRequest request;

    @Mock
    private FieldbookProperties fieldbookProperties;

    @Mock
    private ContextUtil contextUtil;

	@Mock
	private DatasetService datasetService;

	@Mock
	protected OntologyVariableDataManager variableDataManager;

	@Mock
	private SeedSourceGenerator seedSourceGenerator;

	@Mock
	private AdvancingSourceListFactory advancingSourceListFactory;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private NamingConventionService namingConventionService;

	@Mock
	private StudyInstanceService studyInstanceService;

	@InjectMocks
	private final AdvancingController advancingController = Mockito.spy(new AdvancingController());

	@Test
	public void testGetAdvancedNurseryTableHeader_returnsTheValueFromColumLabelDefaultName() throws MiddlewareQueryException {
		final Term fromOntology = new Term();
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		final List<TableHeader> tableHeaderList = this.advancingController.getAdvancedStudyTableHeader();
		Assert.assertEquals("Expecting to return 7 columns but returned " + tableHeaderList.size(), 7, tableHeaderList.size());

		Assert.assertTrue("Expecting to have a column name ENTRY_ID.", this.hasColumnHeader(tableHeaderList, "ENTRY_ID"));
		Assert.assertTrue("Expecting to have a column name DESIGNATION.", this.hasColumnHeader(tableHeaderList, "DESIGNATION"));
		Assert.assertTrue("Expecting to have a column name PARENTAGE.", this.hasColumnHeader(tableHeaderList, "PARENTAGE"));
		Assert.assertTrue("Expecting to have a column name GID.", this.hasColumnHeader(tableHeaderList, "GID"));
		Assert.assertTrue("Expecting to have a column name GERMPLASM_ORIGIN.", this.hasColumnHeader(tableHeaderList, "GERMPLASM_ORIGIN"));
	}

	private boolean hasColumnHeader(final List<TableHeader> tableHeaderList, final String columnName) {
		for (final TableHeader tableHeader : tableHeaderList) {
			if (tableHeader.getColumnName().equals(columnName)) {
				return true;
			}
		}
		return false;
	}

	@Test
	@Ignore("Broken test")
	public void testGetAdvancedNurseryTableHeader_returnsTheValueFromOntology() throws MiddlewareQueryException {
		final Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.TRIAL_INSTANCE_FACTOR.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.REP_NO.getId())).thenReturn(fromOntology);

		final List<TableHeader> tableHeaderList = this.advancingController.getAdvancedStudyTableHeader();
		Assert.assertEquals("Expecting to return 7 columns but returned " + tableHeaderList.size(), 7, tableHeaderList.size());

		for (final TableHeader tableHeader : tableHeaderList) {
			Assert.assertEquals("Expecting name from ontology but didn't.", fromOntology.getName(), tableHeader.getColumnName());
		}
	}

	@Test
	public void testPostAdvanceStudy() throws MiddlewareException, FieldbookException, RuleException {
		final Method method = new Method();
		method.setMtype("DER");
		final AdvancingStudyForm form = this.preparePostAdvanceStudy(method);

		// scenario 1, has a method choice and breeding method not a Generative
		final Map<String, Object> output = this.advancingController.postAdvanceStudy(form);

		Assert.assertEquals("should be successful", "1", output.get("isSuccess"));
		Assert.assertEquals("should have at least 1 imported germplasm list",3, output.get("listSize"));
		Assert.assertNotNull("should have advance germplasm change details", output.get("advanceGermplasmChangeDetails"));
		Assert.assertNotNull("should have generated unique id", output.get("uniqueId"));


		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1)).getMethodById(Integer.valueOf(form.getAdvanceBreedingMethodId()));
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1)).loadObservations(Mockito.any(), ArgumentMatchers.eq(Arrays.asList(1,2,3)), ArgumentMatchers.eq(Collections.singletonList(1)));
		final ArgumentCaptor<AdvancingStudy> advanceInfoCaptor = ArgumentCaptor.forClass(AdvancingStudy.class);
		Mockito.verify(this.advancingSourceListFactory).createAdvancingSourceList(ArgumentMatchers.any(), advanceInfoCaptor.capture(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
		final AdvancingStudy advanceInfo = advanceInfoCaptor.getValue();
		Assert.assertEquals(AdvanceType.fromLowerCaseName(form.getAdvanceType()), advanceInfo.getAdvanceType());
		Assert.assertEquals(form.getSelectedReplications(), advanceInfo.getSelectedReplications());
		Assert.assertEquals(form.getSelectedTrialInstances(), advanceInfo.getSelectedTrialInstances());
		Mockito.verify(this.namingConventionService, Mockito.times(1)).generateAdvanceListNames(ArgumentMatchers.anyList(), ArgumentMatchers.eq(false), ArgumentMatchers.anyList());

	}

	@Test
	public void testPostAdvanceStudy_NullMethodChoice() throws MiddlewareException, FieldbookException, RuleException {

		final AdvancingStudyForm form = this.preparePostAdvanceStudy();
		form.setMethodChoice(null);
		final Map<String, Object> output = this.advancingController.postAdvanceStudy(form);

		Assert.assertEquals("should be successful", "1", output.get("isSuccess"));
		Assert.assertEquals("should have at least 1 imported germplasm list",3, output.get("listSize"));
		Assert.assertNotNull("should have advance germplasm change details", output.get("advanceGermplasmChangeDetails"));
		Assert.assertNotNull("should have generated unique id", output.get("uniqueId"));


		Mockito.verify(this.fieldbookMiddlewareService, Mockito.never()).getMethodById(ArgumentMatchers.anyInt());
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1)).loadObservations(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(this.namingConventionService, Mockito.times(1)).generateAdvanceListNames(ArgumentMatchers.anyList(), ArgumentMatchers.eq(false), ArgumentMatchers.anyList());

	}

	private AdvancingSourceList createTestAdvancingSourceList() {
		final List<AdvancingSource> rows = new ArrayList<>();
		final Method breedingMethod = new Method();
		breedingMethod.setGeneq(TermId.NON_BULKING_BREEDING_METHOD_CLASS.getId());
		for (int i = 1; i <= 3; i++) {
			final AdvancingSource row = new AdvancingSource(this.createImportedGermplasm(i));
			row.setPlotNumber(String.valueOf(i));
			row.setTrialInstanceNumber("1");
			row.setPlantsSelected(1);
			row.setBreedingMethod(breedingMethod);
			rows.add(row);

		}
		final AdvancingSourceList list = new AdvancingSourceList();
		list.setRows(rows);
		return list;
	}

	private ImportedGermplasm createImportedGermplasm(final int gid) {
		final String gidString = String.valueOf(gid);
		final String desig = "ABC" + gid;

		final ImportedGermplasm germplasm = new ImportedGermplasm();
		germplasm.setGid(gidString);
		germplasm.setEntryNumber(gid);
		germplasm.setEntryCode(gidString);
		germplasm.setDesig(desig);
		germplasm.setSource("XYZ:" + gid);
		germplasm.setCross(gid + "/" + (gid + 1));
		germplasm.setSource("Import file");
		germplasm.setLocationId(RandomUtils.nextInt());
		germplasm.setTrialInstanceNumber("1");
		germplasm.setPlotNumber(gidString);

		final Name name = new Name();
		name.setGermplasm(new Germplasm(gid));
		name.setNval(desig);
		name.setNstat(1);
		germplasm.setNames(Collections.singletonList(name));
		return germplasm;
	}

	@Test
	public void testPostAdvanceStudy_ThrowsRuleException() throws MiddlewareException, FieldbookException, RuleException {
		final List<StudyInstance> studyInstances = new ArrayList<>();
		final StudyInstance studyInstance = new StudyInstance();
		studyInstance.setLocationAbbreviation("LABBR");
		studyInstance.setInstanceNumber(1);
		studyInstances.add(studyInstance);

		Mockito.doReturn(studyInstances).when(this.studyInstanceService).getStudyInstances(Mockito.anyInt());

		final Method method = new Method();
		method.setMtype("DER");
		final AdvancingStudyForm form = this.preparePostAdvanceStudy(method);
		Mockito.doThrow(new RuleException("RULE ERROR")).when(this.namingConventionService).generateAdvanceListNames(ArgumentMatchers.anyList(),
				ArgumentMatchers.eq(false), ArgumentMatchers.anyList());


		// scenario 2, has a method throwing exception
		final Map<String, Object> output = this.advancingController.postAdvanceStudy(form);

		Assert.assertEquals("should fail", "0", output.get("isSuccess"));
		Assert.assertEquals("should have at least 0 imported germplasm list", 0, output.get("listSize"));
	}

	@Test
	public void testPostAdvanceStudy_GenerativeMethodError() throws MiddlewareException, FieldbookException {
		final Method method = new Method();
		method.setMtype("GEN");
		final AdvancingStudyForm form = this.preparePostAdvanceStudy(method);

		// scenario 2, has a method throwing exception
		final Map<String, Object> output = this.advancingController.postAdvanceStudy(form);

		Assert.assertEquals("should fail", "0", output.get("isSuccess"));
		Assert.assertEquals("should have at least 0 imported germplasm list", 0, output.get("listSize"));
		Assert.assertEquals("should have error message", "error.message", output.get("message"));
	}

	private AdvancingStudyForm preparePostAdvanceStudy()
		throws MiddlewareException, FieldbookException {

		final int studyId = new Random().nextInt();
		final AdvancingStudyForm form = new AdvancingStudyForm();
		form.setStudyId(Integer.toString(studyId));
		form.setAdvanceType(AdvanceType.SAMPLE.getLowerCaseName());
		form.setSelectedReplications(Collections.singleton("1"));
		form.setSelectedTrialInstances(new HashSet<>(Arrays.asList("1", "2", "3")));
		final Study study = new Study();
		study.setId(studyId);
		Mockito.doReturn(study).when(this.fieldbookMiddlewareService).getStudy(studyId);
		Mockito.doReturn(this.createTestAdvancingSourceList()).when(this.advancingSourceListFactory).createAdvancingSourceList(ArgumentMatchers.any(),
			ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.anyMap(), ArgumentMatchers.anyMap());
		this.prepareMockWorkbook();


		Mockito.doNothing().when(this.paginationListSelection).addAdvanceDetails(ArgumentMatchers.anyString(), ArgumentMatchers.eq(form));

		return form;
	}

	private AdvancingStudyForm preparePostAdvanceStudy(final Method method)
			throws MiddlewareException, FieldbookException {

		final AdvancingStudyForm form = this.preparePostAdvanceStudy();
		form.setMethodChoice("1");
		form.setAdvanceBreedingMethodId("10");

		Mockito.when(this.fieldbookMiddlewareService.getMethodById(ArgumentMatchers.anyInt())).thenReturn(method);
		Mockito.when(this.messageSource.getMessage(ArgumentMatchers.eq("study.save.advance.error.generative.method"),
				ArgumentMatchers.any(String[].class), ArgumentMatchers.eq(LocaleContextHolder.getLocale()))).thenReturn("error.message");

		return form;
	}

	@Test
	public void testDeleteImportedGermplasmEntriesIfDeleted() {
		List<ImportedGermplasm> importedGermplasms = new ArrayList<ImportedGermplasm>();
		for (int i = 0; i < 10; i++) {
			final ImportedGermplasm germplasm = new ImportedGermplasm();
			germplasm.setEntryNumber(i);
			importedGermplasms.add(germplasm);
		}
		final AdvancingSourceList list = this.getAdvancingSourceList(importedGermplasms);
		final AdvancingStudyForm advancingStudyForm = new AdvancingStudyForm();
		advancingStudyForm.setUniqueId(1L);
		advancingStudyForm.setAdvancingSourceItems(list.getRows());
		Mockito.when(this.advancingController.getPaginationListSelection().getAdvanceDetails(ArgumentMatchers.anyString())).thenReturn(advancingStudyForm);

		final String[] entries = {"1", "2", "3"};
		importedGermplasms = this.advancingController.deleteImportedGermplasmEntries(importedGermplasms, entries);
		this.advancingController.updateRemovedSelectedPlant("1", entries);
		final List<AdvancingSource> sources = this.advancingController.getPaginationListSelection().getAdvanceDetails("1").getAdvancingSourceItems();
		Assert.assertEquals("Should have a total of 7 germplasms remaining", 7, importedGermplasms.size());
		Assert.assertEquals(0, sources.get(0).getPlantsSelected().intValue());
		Assert.assertEquals(1, sources.get(1).getPlantsSelected().intValue());

	}

	private List<ImportedGermplasm> generateGermplasm() {
		final List<ImportedGermplasm> importedGermplasms = new ArrayList<ImportedGermplasm>();
		for (int i = 0; i < 10; i++) {
			final ImportedGermplasm germplasm = new ImportedGermplasm();
			germplasm.setEntryNumber(i);
            germplasm.setDesig("Design"+i);
            germplasm.setGid(i+"");
            germplasm.setSource("Source"+i);
            germplasm.setCross("Cross"+i);
			importedGermplasms.add(germplasm);
		}
		return importedGermplasms;
	}

	@Test
	public void testDeleteImportedGermplasmEntriesIfNoneDeleted() {
		List<ImportedGermplasm> importedGermplasms = this.generateGermplasm();
		final String[] entries = {};
		importedGermplasms = this.advancingController.deleteImportedGermplasmEntries(importedGermplasms, entries);
		Assert.assertEquals("Should have a total of 10 germplasms, since nothing is deleted", 10, importedGermplasms.size());
	}

	@Test
	public void testSetupAdvanceReviewDataList() {
		final List<ImportedGermplasm> importedGermplasms = this.generateGermplasm();
		final List<Map<String, Object>> mapInfos = this.advancingController.setupAdvanceReviewDataList(importedGermplasms);
		Assert.assertEquals("Should have the same number of records", importedGermplasms.size(), mapInfos.size());
	}


	@Test
    public void testApplyChangeDetailsSuccess() throws IOException {
        final List<ImportedGermplasm> importedGermplasmList = this.generateGermplasm();
        Mockito.when(this.userSelection.getImportedAdvancedGermplasmList()).thenReturn(importedGermplasmList);

        final AdvanceGermplasmChangeDetail[] advanceGermplasmChangeDetailArray = this.generateAdvanceGermPlasmChangeDetails();
        final ObjectMapper mapper = new ObjectMapper();
        final String responseData = mapper.writeValueAsString(advanceGermplasmChangeDetailArray);


        final Map<String, Object> response = this.advancingController.applyChangeDetails(responseData);
        Assert.assertEquals("1",response.get("isSuccess"));
        Assert.assertEquals(9,response.get("listSize"));

    }

    private AdvanceGermplasmChangeDetail[] generateAdvanceGermPlasmChangeDetails(){
        final List<AdvanceGermplasmChangeDetail> advanceGermplasmChangeDetailList = Lists.newArrayList();

        final AdvanceGermplasmChangeDetail advanceGermplasmChangeDetail = new AdvanceGermplasmChangeDetail(1,1,"newAdvanceName","oldAdvanceName");

        advanceGermplasmChangeDetailList.add(advanceGermplasmChangeDetail);

        final AdvanceGermplasmChangeDetail advanceGermplasmChangeDetailUpdated = new AdvanceGermplasmChangeDetail(3,1,"newAdvanceName","oldAdvanceName");

        advanceGermplasmChangeDetailList.add(advanceGermplasmChangeDetailUpdated);

        return advanceGermplasmChangeDetailList.toArray(new AdvanceGermplasmChangeDetail[advanceGermplasmChangeDetailList.size()]);
    }

    @Test
    public void testShowAdvanceNurserySuccess(){
        final List<ImportedGermplasm> importedGermplasmList = this.generateGermplasm();
        Mockito.when(this.userSelection.getImportedAdvancedGermplasmList()).thenReturn(importedGermplasmList);

        final Term fromOntology = new Term();
        Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
        Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
        Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
        Mockito.when(this.ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);

        Mockito.when(this.request.getParameter("uniqueId")).thenReturn("123");

        final AdvancingStudyForm form = new AdvancingStudyForm();
        final Model model = new ExtendedModelMap();

        final String templateUrl = this.advancingController.showAdvanceStudy(form, model,this.request);
        Assert.assertEquals("StudyManager/saveAdvanceStudy",templateUrl);
        final Map<String,Object> modelMap = model.asMap();
        final List<Map<String, Object>> listOfGermPlasm = (List<Map<String, Object>>)modelMap.get("advanceDataList");
        Assert.assertEquals(10,listOfGermPlasm.size());

        this.assertGermPlasmList(listOfGermPlasm,0);

        Assert.assertEquals(10,form.getEntries());
        Assert.assertEquals(123l,form.getUniqueId().longValue());

    }

    @Test
    public void testShowAdvanceNurseryThrowNumberFormatException(){
        final AdvancingStudyForm form = new AdvancingStudyForm();
        final String templateUrl = this.advancingController.showAdvanceStudy(form,null, this.request);

        Assert.assertEquals(0,form.getEntries());
        Assert.assertEquals(0,form.getGermplasmList().size());

    }

    @Test
    public void testDeleteAdvanceNurseryEntriesSuccess(){

        final List<ImportedGermplasm> importedGermplasmList = this.generateGermplasm();
        Mockito.when(this.userSelection.getImportedAdvancedGermplasmList()).thenReturn(importedGermplasmList);

        Mockito.when(this.request.getParameter("entryNums")).thenReturn("0,1,2");

        Mockito.when(this.request.getParameter("uniqueId")).thenReturn("123");

        final AdvancingStudyForm form = new AdvancingStudyForm();
        final Model model = new ExtendedModelMap();

		final AdvancingSourceList list = this.getAdvancingSourceList(importedGermplasmList);
		form.setUniqueId(1L);
		form.setAdvancingSourceItems(list.getRows());
		Mockito.when(this.advancingController.getPaginationListSelection().getAdvanceDetails(ArgumentMatchers.anyString())).thenReturn(form);

        final String templateUrl = this.advancingController.deleteAdvanceStudyEntries(form, model,this.request);

        Assert.assertEquals("StudyManager/saveAdvanceStudy",templateUrl);
        final Map<String,Object> modelMap = model.asMap();
        final List<Map<String, Object>> listOfGermPlasm = (List<Map<String, Object>>)modelMap.get("advanceDataList");
        Assert.assertEquals(7,listOfGermPlasm.size());
        this.assertGermPlasmList(listOfGermPlasm,3);
        Assert.assertEquals(7,form.getEntries());
        Assert.assertEquals(123l,form.getUniqueId().longValue());

    }

    @Test
    public void testDeleteAdvanceNurseryEntriesSuccessThrowNumberFormatException(){
        final AdvancingStudyForm form = new AdvancingStudyForm();
        final String templateUrl = this.advancingController.deleteAdvanceStudyEntries(form,null,this.request);

        Assert.assertEquals(0,form.getEntries());
        Assert.assertEquals(0,form.getGermplasmList().size());

    }

    @Test
    public void testCountPlotsSuccess(){
        final Workbook workBook = new Workbook();
        workBook.setMeasurementDatesetId(2);
        Mockito.when(this.userSelection.getWorkbook()).thenReturn(workBook);
        Mockito.when(this.fieldbookMiddlewareService.countPlotsWithRecordedVariatesInDataset(Mockito.anyInt(),Mockito.isA(List.class))).thenReturn(new Integer(2));
        final int plotCount = this.advancingController.countPlots("1,2");
        Assert.assertEquals(2,plotCount);

    }

    @Test
    public void testCheckMethodTypeModeLineSuccess(){
        final Workbook workBook = new Workbook();
		final int plotDatasetId = new Random().nextInt();
		workBook.setMeasurementDatesetId(plotDatasetId);

		final List<MeasurementRow> observations = this.generateMeasurementRows();
        workBook.setObservations(observations);
        Mockito.when(this.userSelection.getWorkbook()).thenReturn(workBook);

        final List<Method> methods = Lists.newArrayList();
        final Method nonBulkMethod = new Method();
        nonBulkMethod.setGeneq(1510);
        methods.add(nonBulkMethod);
		final int methodVariateId = 12;
		final Set<String> trialInstances = Collections.singleton("1");
		Mockito.when(this.studyDataManager.getMethodsFromExperiments(plotDatasetId, methodVariateId, new ArrayList<>(trialInstances))).thenReturn(methods);
		final String methodType = this.advancingController.checkMethodTypeMode(methodVariateId,trialInstances);
		Assert.assertEquals("LINE",methodType);
    }

    @Test
    public void testCheckMethodTypeModeBulkSuccess(){
        final Workbook workBook = new Workbook();
		final int plotDatasetId = new Random().nextInt();
		workBook.setMeasurementDatesetId(plotDatasetId);
        final List<MeasurementRow> observations = this.generateMeasurementRows();
        workBook.setObservations(observations);
        Mockito.when(this.userSelection.getWorkbook()).thenReturn(workBook);

        final List<Method> methods = Lists.newArrayList();
        final Method bulkMethod = new Method();
        bulkMethod.setGeneq(1490);
        methods.add(bulkMethod);

		final int methodVariateId = 12;
		final Set<String> trialInstances = Collections.singleton("1");
		Mockito.when(this.studyDataManager.getMethodsFromExperiments(plotDatasetId, methodVariateId, new ArrayList<>(trialInstances))).thenReturn(methods);
		final String methodType = this.advancingController.checkMethodTypeMode(methodVariateId,trialInstances);
        Assert.assertEquals("BULK",methodType);
    }

    @Test
    public void testCheckMethodTypeModeMixedSuccess(){
        final Workbook workBook = new Workbook();
		final int plotDatasetId = new Random().nextInt();
		workBook.setMeasurementDatesetId(plotDatasetId);
        final List<MeasurementRow> observations = this.generateMeasurementRows();
        workBook.setObservations(observations);
        Mockito.when(this.userSelection.getWorkbook()).thenReturn(workBook);

        final List<Method> methods = Lists.newArrayList();

        final Method nonBulkMethod = new Method();
        nonBulkMethod.setGeneq(1490);
        methods.add(nonBulkMethod);

        final Method bulkMethod = new Method();
        bulkMethod.setGeneq(1510);
        methods.add(bulkMethod);


		final int methodVariateId = 12;
		final Set<String> trialInstances = Collections.singleton("1");
		Mockito.when(this.studyDataManager.getMethodsFromExperiments(plotDatasetId, methodVariateId, new ArrayList<>(trialInstances))).thenReturn(methods);
		final String methodType = this.advancingController.checkMethodTypeMode(methodVariateId, trialInstances);
        Assert.assertEquals("MIXED",methodType);
    }

    @Test
    public void testCheckMethodTypeError(){
        final Workbook workBook = new Workbook();
		final int plotDatasetId = new Random().nextInt();
		workBook.setMeasurementDatesetId(plotDatasetId);
        final List<MeasurementRow> observations = Lists.newArrayList();
        workBook.setObservations(observations);

        Mockito.when(this.userSelection.getWorkbook()).thenReturn(workBook);
        Mockito.when(this.messageSource.getMessage(Mockito.isA(String.class),Mockito.any(Object[].class),Mockito.isA(Locale.class))).thenReturn("The nursery has no methods defined under");

        final String methodType = this.advancingController.checkMethodTypeMode(12, Collections.singleton("1"));
        Assert.assertTrue(methodType.contains("The nursery has no methods defined under"));
    }

    private void assertGermPlasmList( final List<Map<String, Object>> listOfGermPlasm ,int position){
        for(final Map<String,Object> entryMap : listOfGermPlasm){
            Assert.assertEquals("Design"+position,entryMap.get("desig"));
            Assert.assertEquals("Source"+position,entryMap.get("source"));
            Assert.assertEquals("Cross"+position,entryMap.get("parentage"));
            Assert.assertEquals("Pending",entryMap.get("gid"));
            position++;
        }
    }

    private List<MeasurementRow> generateMeasurementRows(){
        final List<MeasurementRow> observations = Lists.newArrayList();
        final MeasurementRow row1 = new MeasurementRow();

        final List<MeasurementData> row1Data = Lists.newArrayList();
        final MeasurementData measurementData = new MeasurementData();
        measurementData.setcValueId("13");
        final MeasurementVariable measurementVariable = new MeasurementVariable("name", "description", "scale", "method", "property", "dataType",
                "value", "label");
        measurementVariable.setTermId(12);

        measurementData.setMeasurementVariable(measurementVariable);
        row1Data.add(measurementData);
        row1.setDataList(row1Data);

        observations.add(row1);
        return observations;
    }

	private Variable createSelectionVariable() {
		final Variable variable =new Variable();
		variable.setName("NPSEL");
		variable.setDefinition("Number of plants selected - counted (number)");
		variable.addVariableType(VariableType.SELECTION_METHOD);
		variable.setAllowsFormula(false);
		variable.setId(205);
		final Scale scale = new Scale();
		scale.setDataType(DataType.NUMERIC_VARIABLE);
		scale.setName("Number");
		scale.setDefinition("Number");
		scale.setId(6040);
		scale.setVocabularyId(1030);
		scale.setObsolete(false);

		final Property property = new Property();
		property.setName("Breeding method");
		property.setId(2600);
		property.setVocabularyId(1010);
		property.setDefinition("");
		property.setObsolete(false);
		property.addClass("Breedingprocess");

		final org.generationcp.middleware.domain.ontology.Method method = new org.generationcp.middleware.domain.ontology.Method();

		method.setName("Counted");
		method.setDefinition("Counting method");
		method.setObsolete(false);
		method.setId(4080);
		method.setId(1020);

		variable.setScale(scale);
		variable.setProperty(property);
		variable.setMethod(method);
		return variable;
	}

	@Test
	public void testCheckForNonMaintenanceAndDerivativeMethods() {
		final Workbook workbook = new Workbook();
		workbook.setMeasurementDatesetId(1);
		final Set<String> trialInstances = new HashSet<>(Arrays.asList("1", "2"));
		Mockito.when(this.userSelection.getWorkbook()).thenReturn(workbook);
		final Method method = new Method();
		method.setMtype(MethodType.GENERATIVE.getCode());
		Mockito.when(this.studyDataManager.getMethodsFromExperiments(1, 1, new ArrayList<>(trialInstances)))
			.thenReturn(Collections.singletonList(method));
		final String errorMessage = "error.advancing.study.non.maintenance.derivative.method";
		Mockito.when(this.messageSource.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.any(String[].class),
			ArgumentMatchers.eq(LocaleContextHolder.getLocale()))).thenReturn(errorMessage);

		final Map<String, String> result = this.advancingController.checkForNonMaintenanceAndDerivativeMethods(1, trialInstances);
		Assert.assertEquals(errorMessage, result.get("errors"));

	}

	@Test
	public void testGenerateGermplasmList() throws MiddlewareQueryException {

		final List<StudyInstance> studyInstances = new ArrayList<>();
		final StudyInstance studyInstance = new StudyInstance();
		studyInstance.setLocationAbbreviation("LABBR");
		studyInstance.setInstanceNumber(1);
		studyInstances.add(studyInstance);

		Mockito.doReturn(studyInstances).when(this.studyInstanceService).getStudyInstances(Mockito.anyInt());


		final AdvancingSourceList rows = new AdvancingSourceList();
		rows.setRows(new ArrayList<>());

		// Set up Advancing sources
		final AdvancingSource advancingSource = new AdvancingSource();
		advancingSource.setNames(new ArrayList<>());

		// Germplasm
		final ImportedGermplasm ig = new ImportedGermplasm();
		ig.setEntryNumber(1);
		ig.setDesig("BARRA DE ORO DULCE");
		ig.setGid("133");
		ig.setCross("BARRA DE ORO DULCE");
		ig.setBreedingMethodId(31);
		ig.setGpid1(0);
		ig.setGpid2(0);
		ig.setGnpgs(-1);
		advancingSource.setStudyId(1);
		advancingSource.setGermplasm(ig);

		// Names
		final Name sourceGermplasmName = new Name(133);
		sourceGermplasmName.setGermplasm(new Germplasm(133));
		sourceGermplasmName.setTypeId(6);
		sourceGermplasmName.setNstat(1);
		sourceGermplasmName.setNval("BARRA DE ORO DULCE");
		sourceGermplasmName.setLocationId(9);
		sourceGermplasmName.setNdate(19860501);
		sourceGermplasmName.setReferenceId(1);
		advancingSource.setStudyId(1);
		advancingSource.getNames().add(sourceGermplasmName);

		final Method breedingMethod =
				new Method(40, "DER", "G", "SLF", "Self and Bulk", "Selfing a Single Plant or population and bulk seed", 0, -1, 1, 0,
						TermId.NON_BULKING_BREEDING_METHOD_CLASS.getId(), 1, 0, 19980708);
		breedingMethod.setSnametype(5);
		breedingMethod.setSeparator("-");
		breedingMethod.setPrefix("B");
		breedingMethod.setCount("");

		advancingSource.setBreedingMethod(breedingMethod);
		advancingSource.setPlantsSelected(2);
		advancingSource.setPlotNumber("2");
		advancingSource.setBulk(false);
		advancingSource.setStudyName("Test One");
		advancingSource.setSeason("201412");
		advancingSource.setCurrentMaxSequence(0);
		advancingSource.setTrialInstanceNumber("1");
		rows.getRows().add(advancingSource);

		final String testSeedSource1 = "MEX-DrySeason-N1-1-1";
		Mockito.when(this.seedSourceGenerator.
				generateSeedSource(ArgumentMatchers.any(),
						ArgumentMatchers.any(),
						ArgumentMatchers.eq("1"), ArgumentMatchers.eq("2"), ArgumentMatchers.eq(STUDY_NAME),
					ArgumentMatchers.isNull(), ArgumentMatchers.anyMap(), ArgumentMatchers.anyMap(), ArgumentMatchers.anyList()))
			.thenReturn(testSeedSource1);
		final String testSeedSource2 = "MEX-DrySeason-N1-1-2";
		Mockito.when(this.seedSourceGenerator.
				generateSeedSource(ArgumentMatchers.any(),
						ArgumentMatchers.anyList(),
						ArgumentMatchers.eq("2"), ArgumentMatchers.eq("2"), ArgumentMatchers.eq(STUDY_NAME),
					ArgumentMatchers.isNull(), ArgumentMatchers.anyMap(), ArgumentMatchers.anyMap(), ArgumentMatchers.anyList()))
			.thenReturn(testSeedSource2);

		this.prepareMockWorkbook();

		final AdvancingStudy advancingParameters = new AdvancingStudy();
		advancingParameters.setCheckAdvanceLinesUnique(false);
		final Study study = new Study();
		study.setId(1);
		advancingParameters.setStudy(study);
		final List<ImportedGermplasm> igList = this.advancingController.generateGermplasmList(rows, advancingParameters);
		org.junit.Assert.assertNotNull(igList);
		org.junit.Assert.assertFalse(igList.isEmpty());
		org.junit.Assert.assertEquals(2, igList.size());

		// germplasm1
		final ImportedGermplasm advanceResult1 = igList.get(0);
		org.junit.Assert.assertEquals(new Integer(1), advanceResult1.getEntryNumber());
		org.junit.Assert.assertEquals("1", advanceResult1.getDesig());
		org.junit.Assert.assertNull(advanceResult1.getGid());
		org.junit.Assert.assertEquals(ig.getCross(), advanceResult1.getCross());
		org.junit.Assert.assertEquals(testSeedSource1, advanceResult1.getSource());
		org.junit.Assert.assertEquals("E0001", advanceResult1.getEntryCode());
		org.junit.Assert.assertEquals(new Integer(40), advanceResult1.getBreedingMethodId());
		org.junit.Assert.assertEquals(new Integer(133), advanceResult1.getGpid1());
		org.junit.Assert.assertEquals(new Integer(133), advanceResult1.getGpid2());

		// germplasm2
		final ImportedGermplasm advanceResult2 = igList.get(1);
		org.junit.Assert.assertEquals(new Integer(2), advanceResult2.getEntryNumber());
		org.junit.Assert.assertEquals("2", advanceResult2.getDesig());
		org.junit.Assert.assertNull(advanceResult2.getGid());
		org.junit.Assert.assertEquals(ig.getCross(), advanceResult2.getCross());
		org.junit.Assert.assertEquals(testSeedSource2, advanceResult2.getSource());
		org.junit.Assert.assertEquals("E0002", advanceResult2.getEntryCode());
		org.junit.Assert.assertEquals(new Integer(40), advanceResult2.getBreedingMethodId());
		org.junit.Assert.assertEquals(new Integer(133), advanceResult2.getGpid1());
		org.junit.Assert.assertEquals(new Integer(133), advanceResult2.getGpid2());

	}

	private void prepareMockWorkbook() {
		final Workbook workbook = Mockito.mock(Workbook.class);
		final StudyDetails studyDetails = Mockito.mock(StudyDetails.class);
		Mockito.doReturn(new Random().nextInt()).when(studyDetails).getId();
		Mockito.doReturn(STUDY_NAME).when(workbook).getStudyName();
		final MeasurementRow row = Mockito.mock(MeasurementRow.class);
		Mockito.when(workbook.getStudyDetails()).thenReturn(studyDetails);
		Mockito.when(this.userSelection.getWorkbook()).thenReturn(workbook);
		Mockito.when(workbook.getTrialObservationByTrialInstanceNo(1)).thenReturn(row);

	}

	private AdvancingSourceList getAdvancingSourceList(final List<ImportedGermplasm> germplasmList) {

		final AdvancingSourceList rows = new AdvancingSourceList();
		rows.setRows(new ArrayList<>());
		final int size = (germplasmList.size() / 2) % 1 == 0 ? germplasmList.size() / 2 : 1;

		for (int i = 0; i < size; i++) {
			// Set up Advancing sources
			final AdvancingSource advancingSource = new AdvancingSource();
			advancingSource.setNames(new ArrayList<>());
			advancingSource.setStudyId(1);
			advancingSource.setGermplasm(germplasmList.get(i));

			// Names
			final Name sourceGermplasmName = new Name(133);
			sourceGermplasmName.setGermplasm(new Germplasm(133));
			sourceGermplasmName.setTypeId(6);
			sourceGermplasmName.setNstat(1);
			sourceGermplasmName.setNval("BARRA DE ORO DULCE");
			sourceGermplasmName.setLocationId(9);
			sourceGermplasmName.setNdate(19860501);
			sourceGermplasmName.setReferenceId(1);
			advancingSource.setStudyId(1);
			advancingSource.getNames().add(sourceGermplasmName);

			final Method breedingMethod =
					new Method(40, "DER", "G", "SLF", "Self and Bulk", "Selfing a Single Plant or population and bulk seed", 0, -1, 1, 0,
							TermId.NON_BULKING_BREEDING_METHOD_CLASS.getId(), 1, 0, 19980708);
			breedingMethod.setSnametype(5);
			breedingMethod.setSeparator("-");
			breedingMethod.setPrefix("B");
			breedingMethod.setCount("");

			advancingSource.setBreedingMethod(breedingMethod);
			advancingSource.setPlantsSelected(2);
			advancingSource.setPlotNumber("2");
			advancingSource.setBulk(false);
			advancingSource.setStudyName("Test One");
			advancingSource.setSeason("201412");
			advancingSource.setCurrentMaxSequence(0);
			advancingSource.setTrialInstanceNumber("1");
			rows.getRows().add(advancingSource);
		}
		return rows;
	}
}
