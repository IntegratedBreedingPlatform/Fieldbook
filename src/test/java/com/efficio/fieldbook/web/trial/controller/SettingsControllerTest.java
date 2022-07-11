
package com.efficio.fieldbook.web.trial.controller;

import com.efficio.fieldbook.service.FieldbookServiceImpl;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.data.initializer.SettingDetailTestDataInitializer;
import org.junit.Assert;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.utils.test.UnitTestDaoIDGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class SettingsControllerTest {

	private static final String TRAIT_DESCRIPTION = "Ears Selected";

	private static final String TRAIT_NAME = "nEarsSel";

	private Variable testVariable;

	private ValueReference testValueReference;

	/**
	 * Class under test (SettingsController) is an abstract class so using a dummy impl to invoke methods for testing.
	 */
	class SettingsControllerUnitTestEnabler extends SettingsController {

		@Override
		public String getContentName() {
			return null;
		}
	}

	private final SettingsControllerUnitTestEnabler controller = new SettingsControllerUnitTestEnabler();

	@Mock
	private org.generationcp.commons.spring.util.ContextUtil contextUtil;

	@Mock
	private OntologyVariableDataManager variableDataManager;

	@Mock
	private FieldbookServiceImpl fieldbookService;
	
	@Mock
	private OntologyService ontologyService;

	private final String programUUID = UUID.randomUUID().toString();

	@Before
	public void setUp() {
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(this.programUUID);
		this.controller.setContextUtil(this.contextUtil);
		this.controller.setVariableDataManager(this.variableDataManager);
		this.controller.setFieldbookService(this.fieldbookService);
		this.controller.setOntologyService(this.ontologyService);

		this.createTestVariable();
		Mockito.when(this.variableDataManager.getVariable(Matchers.any(String.class), Matchers.any(Integer.class), Matchers.anyBoolean()))
				.thenReturn(this.testVariable);
		Mockito.when(this.fieldbookService.getAllPossibleValues(Matchers.anyInt())).thenReturn(Arrays.asList(this.testValueReference));
		Mockito.when(
				this.fieldbookService.getAllPossibleValuesFavorite(Matchers.anyInt(), Matchers.any(String.class), Matchers.anyBoolean()))
				.thenReturn(Arrays.asList(this.testValueReference));
	}

	private List<SettingDetail> createSettingDetailVariables() {
		final List<SettingDetail> variables = new ArrayList<>();
		variables.add(this.createSettingDetail(TermId.TRIAL_INSTANCE_FACTOR.getId(), ""));
		variables.add(this.createSettingDetail(TermId.PI_NAME.getId(), ""));
		variables.add(this.createSettingDetail(TermId.PI_ID.getId(), ""));
		variables.add(this.createSettingDetail(TermId.CHECK_INTERVAL.getId(), ""));
		variables.add(this.createSettingDetail(TermId.CHECK_PLAN.getId(), ""));
		variables.add(this.createSettingDetail(TermId.CHECK_START.getId(), ""));
		return variables;
	}

	private SettingDetail createSettingDetail(final Integer cvTermId, final String value) {
		final SettingVariable variable = new SettingVariable();
		variable.setProperty(RandomStringUtils.randomAlphabetic(20));
		variable.setCvTermId(cvTermId);
		final SettingDetail settingDetail = new SettingDetail(variable, null, value, false);
		return settingDetail;
	}

	public static void checkVariableSecionIdModelAttributes(final ExtendedModelMap model) {
		Assert.assertEquals(VariableType.TRAIT.getId(), model.get("baselineTraitsSegment"));
		Assert.assertEquals(VariableType.SELECTION_METHOD.getId(), model.get("selectionVariatesSegment"));
		Assert.assertEquals(VariableType.STUDY_DETAIL.getId(), model.get("studyLevelDetailType"));
		Assert.assertEquals(VariableType.GERMPLASM_DESCRIPTOR.getId(), model.get("plotLevelDetailType"));
		Assert.assertEquals(VariableType.ENVIRONMENT_CONDITION.getId(), model.get("studyConditionsType"));
	}

	@Test
	public void testCreateSettingDetailWithVariableType() {
		ContextHolder.setCurrentCrop("maize");
		this.createTestProject();

		final String alias = "nEarsSel_Local";
		final SettingDetail settingDetail =
				this.controller.createSettingDetailWithVariableType(this.testVariable.getId(), alias, VariableType.SELECTION_METHOD);
		Assert.assertEquals("Error in Role for settingDetail", VariableType.SELECTION_METHOD.getRole().name(),
				settingDetail.getRole().name());
		Assert.assertEquals("Error in Variable Type", VariableType.SELECTION_METHOD, settingDetail.getVariableType());
		Assert.assertEquals("Expecting variable alias to be used but was not.", alias, settingDetail.getVariable().getName());
		Assert.assertEquals("Expecting variable description to be used but was not.", SettingsControllerTest.TRAIT_DESCRIPTION,
				settingDetail.getVariable().getDescription());
		Assert.assertNull("Error in Value", settingDetail.getValue());

		Assert.assertTrue("Error in Name of PossibleValuesToJson",
				settingDetail.getPossibleValuesJson().contains(this.testValueReference.getName()));
		Assert.assertTrue("Error in Description of PossibleValuesToJson",
				settingDetail.getPossibleValuesJson().contains(this.testValueReference.getDescription()));
		Assert.assertTrue("Error in Key of PossibleValuesToJson",
				settingDetail.getPossibleValuesJson().contains(this.testValueReference.getKey()));
		Assert.assertTrue("Error in Name of PossibleValuesFavoriteToJson",
				settingDetail.getPossibleValuesFavoriteJson().contains(this.testValueReference.getName()));
		Assert.assertTrue("Error in Description of PossibleValuesFavoriteToJson",
				settingDetail.getPossibleValuesFavoriteJson().contains(this.testValueReference.getDescription()));
		Assert.assertTrue("Error in Key of PossibleValuesFavoriteToJson",
				settingDetail.getPossibleValuesFavoriteJson().contains(this.testValueReference.getKey()));

		Mockito.verify(this.variableDataManager, Mockito.times(1)).getVariable(this.contextUtil.getCurrentProgramUUID(),
				this.testVariable.getId(), false);
		Mockito.verify(this.fieldbookService, Mockito.times(1)).getAllPossibleValues(this.testVariable.getId());
		Mockito.verify(this.contextUtil, Mockito.times(1)).getProjectInContext();
		Mockito.verify(this.fieldbookService, Mockito.times(1)).getAllPossibleValuesFavorite(this.testVariable.getId(),
				this.controller.getCurrentProject().getUniqueID(), false);
	}

	@Test
	public void testCreateSettingDetailWithVariableTypeWhenAliasIsNull() {
		ContextHolder.setCurrentCrop("maize");
		this.createTestProject();

		final SettingDetail settingDetail =
				this.controller.createSettingDetailWithVariableType(this.testVariable.getId(), null, VariableType.SELECTION_METHOD);
		Assert.assertEquals("Error in Role for settingDetail", VariableType.SELECTION_METHOD.getRole().name(),
				settingDetail.getRole().name());
		Assert.assertEquals("Error in Variable Type", VariableType.SELECTION_METHOD, settingDetail.getVariableType());
		Assert.assertEquals("Expecting variable's standard name to be used since alias is null but was not.",
				SettingsControllerTest.TRAIT_NAME, settingDetail.getVariable().getName());
		Assert.assertEquals("Expecting variable description to be used but was not.", SettingsControllerTest.TRAIT_DESCRIPTION,
				settingDetail.getVariable().getDescription());
		Assert.assertNull("Error in Value", settingDetail.getValue());
	}

	@Test
	public void testCreateSettingDetailWithVariableTypeWhenAliasIsEmpty() {
		ContextHolder.setCurrentCrop("maize");
		this.createTestProject();

		final SettingDetail settingDetail =
				this.controller.createSettingDetailWithVariableType(this.testVariable.getId(), "", VariableType.SELECTION_METHOD);
		Assert.assertEquals("Error in Role for settingDetail", VariableType.SELECTION_METHOD.getRole().name(),
				settingDetail.getRole().name());
		Assert.assertEquals("Error in Variable Type", VariableType.SELECTION_METHOD, settingDetail.getVariableType());
		Assert.assertEquals("Expecting variable's standard name to be used since alias is empty but was not.",
				SettingsControllerTest.TRAIT_NAME, settingDetail.getVariable().getName());
		Assert.assertEquals("Expecting variable description to be used but was not.", SettingsControllerTest.TRAIT_DESCRIPTION,
				settingDetail.getVariable().getDescription());
		Assert.assertNull("Error in Value", settingDetail.getValue());
	}
	
	@Test
	public void testRemoveSelectionVariatesFromTraitsWithoutSelectionVariate() {
		final List<SettingDetail> variables = this.createSettingDetailVariables();
		final String originalProperty = "Pórtúgêsê Própêrty";
		final int originalSize = variables.size();
		final List<String> properties = new ArrayList<>();
		for (final SettingDetail detail : variables) {
			properties.add(HtmlUtils.htmlUnescape(detail.getVariable().getProperty()));
		}
		Mockito.when(this.ontologyService.getProperty(Matchers.anyString()))
				.thenReturn(new org.generationcp.middleware.domain.oms.Property(new Term(TermId.LOCATION_ID.getId(), TermId.LOCATION_ID.name(), "definition")));

		this.controller.removeSelectionVariatesFromTraits(variables);
		Assert.assertEquals(originalSize, variables.size());
		final ArgumentCaptor<String> propertiesCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(this.ontologyService, Mockito.times(originalSize)).getProperty(propertiesCaptor.capture());
		Assert.assertEquals(properties, propertiesCaptor.getAllValues());
	}

	@Test
	public void testAddDeletedTreatmentFactorInDeletedPlotLevelList() {
		final SettingDetail settingDetail = SettingDetailTestDataInitializer.createSettingDetail(TermId.TREATMENT_MEAN.getId(), TermId.TREATMENT_MEAN.name(), "2", PhenotypicType.TRIAL_DESIGN);
		final UserSelection userSelection = new UserSelection();
		userSelection.setPlotsLevelList(Arrays.asList(settingDetail));
		this.controller.setUserSelection(userSelection);

		this.controller.addDeletedTreatmentFactorInDeletedPlotLevelList(settingDetail);
		Assert.assertEquals(1, userSelection.getDeletedPlotLevelList().size());

		final SettingDetail deletedSettingDetail = userSelection.getDeletedPlotLevelList().get(0);
		Assert.assertEquals(settingDetail.getVariable().getCvTermId(), deletedSettingDetail.getVariable().getCvTermId());
		Assert.assertEquals(settingDetail.getVariable().getName(), deletedSettingDetail.getVariable().getName());
	}
	
	@Test
	public void testRemoveSelectionVariatesFromTraitsWithSelectionVariate() {
		final List<SettingDetail> variables = this.createSettingDetailVariables();
		final int originalSize = variables.size();
		final List<SettingDetail> expectedVariables = new ArrayList<>();
		for (final SettingDetail detail : variables) {
			final String property = HtmlUtils.htmlUnescape(detail.getVariable().getProperty());
			expectedVariables.add(detail);
			Mockito.when(this.ontologyService.getProperty(property)).thenReturn(new org.generationcp.middleware.domain.oms.Property(
					new Term(TermId.LOCATION_ID.getId(), TermId.LOCATION_ID.name(), "definition")));
		}
		// Setup first setting detail as selection variate
		final SettingDetail firstVariable = variables.get(0);
		Mockito.when(this.ontologyService.getProperty(HtmlUtils.htmlUnescape(firstVariable.getVariable().getProperty())))
				.thenReturn(new org.generationcp.middleware.domain.oms.Property(
						new Term(TermId.BREEDING_METHOD_PROP.getId(), TermId.BREEDING_METHOD_PROP.name(), "definition")));
		expectedVariables.remove(firstVariable);
		
		this.controller.removeSelectionVariatesFromTraits(variables);
		// Expecting first variable to have been removed
		Assert.assertEquals(originalSize - 1, variables.size());
		Assert.assertFalse(variables.contains(firstVariable));
	}

	private void createTestProject() {
		final Project project = new Project();
		project.setUniqueID(this.programUUID);

		Mockito.when(this.controller.getCurrentProject()).thenReturn(project);
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(project);
	}

	private void createTestVariable() {
		final org.generationcp.middleware.domain.ontology.Method method = new org.generationcp.middleware.domain.ontology.Method();
		method.setId(UnitTestDaoIDGenerator.generateId(Method.class));
		method.setName("Method Name");

		final Property property = new Property();
		property.setName("Property Name");
		property.setCropOntologyId("CO:501");
		property.addClass("Class1");
		property.addClass("Class2");

		final Scale scale = new Scale();
		scale.setId(UnitTestDaoIDGenerator.generateId(Scale.class));
		scale.setName("Scale Name");
		scale.setDataType(DataType.NUMERIC_VARIABLE);
		scale.setMinValue("5");
		scale.setMaxValue("500");

		this.testVariable = new Variable();
		this.testVariable.setId(UnitTestDaoIDGenerator.generateId(Variable.class));
		this.testVariable.setMinValue("10");
		this.testVariable.setMaxValue("100");
		this.testVariable.setName(SettingsControllerTest.TRAIT_NAME);
		this.testVariable.setDefinition(SettingsControllerTest.TRAIT_DESCRIPTION);
		this.testVariable.setObsolete(false);
		this.testVariable.setObservations(-1);
		this.testVariable.setStudies(-1);
		this.testVariable.setIsFavorite(false);
		this.testVariable.setMethod(method);
		this.testVariable.setProperty(property);
		this.testVariable.setScale(scale);

		this.testValueReference = new ValueReference();
		this.testValueReference.setKey("1");
		this.testValueReference.setName("Value Reference Name");
		this.testValueReference.setDescription("Value Reference Description");
	}

}
