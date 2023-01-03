package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.SettingsService;
import com.efficio.fieldbook.web.common.bean.PropertyTreeSummary;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.google.common.base.Optional;
import org.fest.util.Collections;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.ontology.api.OntologyPropertyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.efficio.fieldbook.web.common.bean.UserSelection;

import org.junit.Assert;

@RunWith(MockitoJUnitRunner.class)
public class ManageSettingsControllerTest {

	private static final int STUDY_ID = 2020;
	public static final int TEST_VARIABLE_ID_0 = 1234;
	public static final String PROGRAM_UUID = "749782348-823asdasd-782364casd";

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Mock
	private OntologyPropertyDataManager ontologyPropertyDataManager;

	@Mock
	private UserSelection userSelection;

	@Mock
	private FormulaService formulaService;

	@Mock
	private SettingsService settingsService;

	@Mock
	protected FieldbookService fieldbookService;

	@Mock
	protected ContextUtil contextUtil;

	@InjectMocks
	private ManageSettingsController controller;

	@Before
	public void init() {


		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(PROGRAM_UUID);
	}

	@Test
	@Ignore(value ="BMS-1571. Ignoring temporarily. Please fix the failures and remove @Ignore.")
	public void testDeleteVariableMultiple() throws Exception {
		// we just have to make sure that the original deleteVariable(@PathVariable int mode, @PathVariable int variableId) is called
		// unit test for that method should be done separately
		final ManageSettingsController spyController = Mockito.spy(this.controller);
		Mockito.doReturn("").when(spyController).deleteVariable(Matchers.anyInt(), Matchers.anyInt());

		Assert.assertTrue("this should always return true regardless of input",
			spyController.deleteVariable(VariableType.TRAIT.getId(), Arrays.asList(ManageSettingsControllerTest.TEST_VARIABLE_ID_0)));

		Mockito.verify(spyController).deleteVariable(VariableType.TRAIT.getId(), ManageSettingsControllerTest.TEST_VARIABLE_ID_0);
	}

	@Test
	public void testAddSettings() throws Exception {

		final int cvTermId = 123;

		final CreateTrialForm createTrialForm = new CreateTrialForm();
		final SettingVariable settingVariable = new SettingVariable();

		settingVariable.setCvTermId(cvTermId);
		createTrialForm.setSelectedVariables(Collections.list(settingVariable));

		final FormulaDto formulaDto = new FormulaDto();

		final ValueReference location1 = new ValueReference(1, "Location1");
		final ValueReference location2 = new ValueReference(2, "Location2");

		final List<ValueReference> possibleValues = Collections.list(location1, location2);
		final List<ValueReference> allValues = Collections.list(location1, location2);

		Mockito.when(this.formulaService.getByTargetId(cvTermId)).thenReturn(Optional.of(formulaDto));
		Mockito.when(this.fieldbookService.getAllPossibleValues(cvTermId)).thenReturn(possibleValues);
		Mockito.when(this.fieldbookService.getAllPossibleValuesWithFilter(cvTermId, false)).thenReturn(allValues);

		// Add the variable in TRAIT settings.
		this.controller.addSettings(createTrialForm, VariableType.TRAIT.getId());

		Mockito.verify(this.settingsService).populateSettingVariable(settingVariable);

		final ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

		Mockito.verify(this.settingsService).addNewSettingDetails(Mockito.eq(VariableType.TRAIT.getId()), captor.capture());

		final List<SettingDetail> addedSettingDetails = captor.getValue();
		final SettingDetail addedSettingDetail = addedSettingDetails.get(0);

		Assert.assertSame(formulaDto, addedSettingDetail.getVariable().getFormula());
		Assert.assertSame(Operation.ADD, addedSettingDetail.getVariable().getOperation());
		Assert.assertSame(allValues, addedSettingDetail.getAllValues());
		Assert.assertNotNull(addedSettingDetail.getPossibleValuesJson());
		Assert.assertNotNull(addedSettingDetail.getAllValuesJson());

	}

	@Test
	public void testGetOntologyPropertiesByVariableType() {

		final Integer[] variableTypes = new Integer[] {VariableType.TRAIT.getId()};
		final String[] classes = new String[] {""};
		final Property property = this.createProperty();
		final Variable variable = new Variable();
		variable.setFormula(this.createFormula());

		Mockito.when(this.ontologyPropertyDataManager
				.getAllPropertiesWithClassAndVariableType(Matchers.eq(classes), Mockito.eq(new String[] {"Trait"})))
				.thenReturn(Collections.list(property));
		Mockito.when(this.ontologyVariableDataManager.getWithFilter(Mockito.any(VariableFilter.class))).thenReturn(Collections.list(variable));

		final List<PropertyTreeSummary> propertyTreeSummary =
				this.controller.getOntologyPropertiesByVariableType(variableTypes, classes, false);

		final PropertyTreeSummary result = propertyTreeSummary.get(0);

		Assert.assertEquals(property.getId(), result.getPropertyId().intValue());
		Assert.assertFalse(result.getStandardVariables().isEmpty());
		Assert.assertEquals(variable, result.getStandardVariables().get(0));
		Assert.assertEquals("Variable1 + Variable2", result.getStandardVariables().get(0).getFormula().getDefinition());
		Mockito.verify(this.ontologyVariableDataManager, Mockito.times(0))
				.processTreatmentFactorHasPairValue(Mockito.anyListOf(Variable.class), Mockito.anyListOf(Integer.class));
	}

	@Test
	public void testGetOntologyPropertiesByVariableTypeVariableTypeHasTreatmentFactor() {

		final Integer[] variableTypes = new Integer[] {VariableType.TREATMENT_FACTOR.getId()};
		final String[] classes = new String[] {""};
		final Property property = this.createProperty();
		final Variable variable = new Variable();
		variable.setFormula(this.createFormula());

		Mockito.when(this.ontologyPropertyDataManager
				.getAllPropertiesWithClassAndVariableType(Matchers.eq(classes), Mockito.eq(new String[] {"Treatment Factor"})))
				.thenReturn(Collections.list(property));
		Mockito.when(this.ontologyVariableDataManager.getWithFilter(Mockito.any(VariableFilter.class))).thenReturn(Collections.list(variable));

		final List<PropertyTreeSummary> propertyTreeSummary =
				this.controller.getOntologyPropertiesByVariableType(variableTypes, classes, false);

		final PropertyTreeSummary result = propertyTreeSummary.get(0);

		Assert.assertEquals(property.getId(), result.getPropertyId().intValue());
		Assert.assertFalse(result.getStandardVariables().isEmpty());
		Assert.assertEquals(variable, result.getStandardVariables().get(0));
		Assert.assertEquals("Variable1 + Variable2", result.getStandardVariables().get(0).getFormula().getDefinition());
		Mockito.verify(this.ontologyVariableDataManager)
				.processTreatmentFactorHasPairValue(Mockito.anyListOf(Variable.class), Mockito.anyListOf(Integer.class));
	}

	@Test
	public void testGetOntologyPropertiesByRole() {

		final Integer roleId = TermId.TRIAL_ENVIRONMENT_INFO_STORAGE.getId();

		final ArgumentCaptor<VariableFilter> captorVariableFilter = ArgumentCaptor.forClass(VariableFilter.class);
		final Property property = this.createProperty();
		final Variable variable = new Variable();
		variable.setFormula(this.createFormula());

		Mockito.when(this.ontologyPropertyDataManager
				.getAllPropertiesWithClassAndVariableType(Mockito.<String[]>any(), Mockito.eq(new String[] {"Environment Detail"})))
				.thenReturn(Collections.list(property));
		Mockito.when(this.ontologyVariableDataManager.getWithFilter(Mockito.any(VariableFilter.class))).thenReturn(Collections.list(variable));

		final List<PropertyTreeSummary> propertyTreeSummary =
				this.controller.getOntologyPropertiesByRole(roleId);

		final PropertyTreeSummary result = propertyTreeSummary.get(0);

		Mockito.verify(this.ontologyVariableDataManager, Mockito.times(1)).getWithFilter(captorVariableFilter.capture());
		final VariableFilter variableFilterVal = captorVariableFilter.getValue();
		final List<Integer> excludedVariableIds = variableFilterVal.getExcludedVariableIds();

		Assert.assertTrue("There should be no excluded variables", excludedVariableIds.isEmpty());
		Assert.assertEquals(property.getId(), result.getPropertyId().intValue());
		Assert.assertFalse(result.getStandardVariables().isEmpty());
		Assert.assertEquals(variable, result.getStandardVariables().get(0));
		Assert.assertEquals("Variable1 + Variable2", result.getStandardVariables().get(0).getFormula().getDefinition());
		Mockito.verify(this.ontologyVariableDataManager, Mockito.times(0))
				.processTreatmentFactorHasPairValue(Mockito.anyListOf(Variable.class), Mockito.anyListOf(Integer.class));

	}

	private FormulaDto createFormula() {

		final List<FormulaVariable> formulaVariables = new ArrayList<>();
		final FormulaVariable formulaVariable1 = new FormulaVariable();
		formulaVariable1.setId(1111);
		formulaVariable1.setName("Variable1");
		final FormulaVariable formulaVariable2 = new FormulaVariable();
		formulaVariable2.setId(2222);
		formulaVariable2.setName("Variable2");
		formulaVariables.add(formulaVariable1);
		formulaVariables.add(formulaVariable2);

		final FormulaDto formulaDto = new FormulaDto();
		formulaDto.setDefinition("{{1111}} + {{2222}}");
		formulaDto.setInputs(formulaVariables);

		return formulaDto;
	}

	private Property createProperty() {
		final Property property = new Property();
		property.setName("Some Property");
		property.setId(123);
		return property;
	}
}
