
package com.efficio.fieldbook.web.nursery.controller;

import java.util.ArrayList;
import java.util.Random;

import junit.framework.Assert;

import org.generationcp.commons.context.ContextConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.VariableConstraints;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.Model;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.FieldbookProperties;

/**
 * Created by cyrus on 21/10/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateNurseryControllerTest {

	private final String TEST_PROG_UUID = "UUID_STRING";

	private MockHttpServletRequest request;
	private MockHttpSession session;

	@Mock
	private CreateNurseryForm createNurseryForm;

	@Mock
	private ImportGermplasmListForm importGermplasmListForm;

	@Mock
	private Model model;

	@Mock
	private ContextInfo contextInfo;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private UserSelection userSelection;

	@Mock
	private WorkbenchService workbenchService;

	@Mock
	private FieldbookService fieldbookService;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	protected FieldbookProperties fieldbookProperties;

	@InjectMocks
	private final CreateNurseryController controller = new CreateNurseryController();

	@Before
	public void setUp() throws Exception {
		this.request = new MockHttpServletRequest();
		this.session = (MockHttpSession) this.request.getSession();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(this.request));

		final Project project = new Project();
		project.setProjectId(1L);
		final CropType cropType = new CropType();
		cropType.setCropName("Test");
		project.setCropType(cropType);
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(project);
		this.session.setAttribute(ContextConstants.SESSION_ATTR_CONTEXT_INFO, this.contextInfo);

		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(this.TEST_PROG_UUID);
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(Matchers.anyInt(), Matchers.eq(this.TEST_PROG_UUID))).thenReturn(
				this.createTestVariable());

	}

	@Test
	public void testShow() throws Exception {
		this.controller.show(this.createNurseryForm, this.importGermplasmListForm, this.model, this.session, this.request);

		final ArgumentCaptor<Integer> traitArg = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<Integer> selectionMethodArg = ArgumentCaptor.forClass(Integer.class);

		// make sure we have set the model attributes correctly
		Mockito.verify(this.model, Mockito.times(1)).addAttribute(Matchers.eq("baselineTraitsSegment"), traitArg.capture());
		Mockito.verify(this.model, Mockito.times(1)).addAttribute(Matchers.eq("selectionVariatesSegment"), selectionMethodArg.capture());
		Assert.assertEquals(VariableType.TRAIT.getId(), traitArg.getValue());
		Assert.assertEquals(VariableType.SELECTION_METHOD.getId(), selectionMethodArg.getValue());

	}

	private StandardVariable createTestVariable() {
		final StandardVariable stdVariable = new StandardVariable();
		stdVariable.setName("variable name " + new Random().nextInt(10000));
		stdVariable.setDescription("variable description");
		stdVariable.setProperty(new Term(2002, "User", "Database user"));
		stdVariable.setMethod(new Term(4030, "Assigned", "Term, name or id assigned"));
		stdVariable.setScale(new Term(61220, "DBCV", "Controlled vocabulary from a database"));
		stdVariable.setDataType(new Term(1120, "Character variable", "variable with char values"));
		stdVariable.setIsA(new Term(1050, "Study condition", "Study condition class"));
		stdVariable.setEnumerations(new ArrayList<Enumeration>());
		stdVariable.getEnumerations().add(new Enumeration(10000, "N", "Nursery", 1));
		stdVariable.getEnumerations().add(new Enumeration(10001, "HB", "Hybridization nursery", 2));
		stdVariable.getEnumerations().add(new Enumeration(10002, "PN", "Pedigree nursery", 3));
		stdVariable.setConstraints(new VariableConstraints(100.0, 999.0));
		stdVariable.setCropOntologyId("CROP-TEST");

		return stdVariable;
	}

}
