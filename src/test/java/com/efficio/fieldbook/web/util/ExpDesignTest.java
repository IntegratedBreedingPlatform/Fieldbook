
package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.Operation;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.service.RandomizeCompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableIncompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableRowColumnDesignService;
import com.efficio.fieldbook.web.trial.bean.BVDesignOutput;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;

@Ignore
public class ExpDesignTest extends AbstractBaseIntegrationTest {

	@Autowired
	private WorkbenchService workbenchService;
	@Autowired
	private FieldbookProperties fieldbookProperties;
	@Autowired
	private ResolvableIncompleteBlockDesignService resolveIncompleteBlockDesign;
	@Autowired
	private ResolvableRowColumnDesignService resolveRowColumn;
	@Autowired
	private RandomizeCompleteBlockDesignService randomizeBlockDesign;
	@Resource
	public org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;
	@Resource
	public FieldbookService fieldbookService;
	private static final String PROGRAM_UUID = "12345678";

	@Test
	public void testResolvableIncompleteBlockExpDesignRunToBvDesign() {

		MainDesign mainDesign = ExpDesignUtil.createResolvableIncompleteBlockDesign("6", "24", "2", "Treat", "Reps",
				"Subblocks", "Plots", 301, null, "0", "", "", false);

		try {
			BVDesignOutput output = this.fieldbookService.runBVDesign(this.workbenchService, this.fieldbookProperties, mainDesign);
			Assert.assertEquals(output.isSuccess(), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testResolvableRowColExpDesignRunToBvDesign() {

		MainDesign mainDesign = ExpDesignUtil.createResolvableRowColDesign("50", "2", "5", "10", "Treat", "Reps", "Rows", "Columns",
				"Plots", 301, null, "0", "0", "", "", false);

		try {
			BVDesignOutput output = this.fieldbookService.runBVDesign(this.workbenchService, this.fieldbookProperties, mainDesign);
			Assert.assertEquals(output.isSuccess(), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testRandomizeCompleteBlockDesignExpDesignRunToBvDesign() {

		List<String> treatmentFactor = new ArrayList<String>();
		treatmentFactor.add("ENTRY_NO");
		treatmentFactor.add("FERTILIZER");

		List<String> levels = new ArrayList<String>();
		levels.add("24");
		levels.add("3");

		MainDesign mainDesign = ExpDesignUtil.createRandomizedCompleteBlockDesign("6", "Reps", "Plots", 301, treatmentFactor, levels, "");

		try {
			BVDesignOutput output = this.fieldbookService.runBVDesign(this.workbenchService, this.fieldbookProperties, mainDesign);
			Assert.assertEquals(output.isSuccess(), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testResolvableIncompleteBlockDesignService() {

		try {
			List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", 24);
			List<MeasurementVariable> trialVariables = new ArrayList<MeasurementVariable>();
			trialVariables.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(),PROGRAM_UUID), Operation.ADD,
					this.fieldbookService));

			List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.GID.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.DESIG.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();
			variates.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(20368,PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			ExpDesignParameterUi param = new ExpDesignParameterUi();
			param.setBlockSize("6");
			param.setReplicationsCount("2");
			param.setNoOfEnvironments("1");

			ExpDesignValidationOutput output = this.resolveIncompleteBlockDesign.validate(param, germplasmList);
			Assert.assertEquals(true, output.isValid());

			List<MeasurementRow> measurementRowList =
					this.resolveIncompleteBlockDesign
							.generateDesign(germplasmList, param, trialVariables, factors, factors, variates, null);
			for (MeasurementRow measurementRow : measurementRowList) {
				System.out.println(measurementRow.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testResolvableIncompleteBlockLatinizedAdjacentDesignService() {

		try {
			List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", 24);
			List<MeasurementVariable> trialVariables = new ArrayList<MeasurementVariable>();
			trialVariables.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(),PROGRAM_UUID), Operation.ADD,
					this.fieldbookService));

			List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.GID.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.DESIG.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();
			variates.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(20368,PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			ExpDesignParameterUi param = new ExpDesignParameterUi();
			param.setBlockSize("4");
			param.setReplicationsCount("4");
			param.setNoOfEnvironments("1");
			param.setUseLatenized(true);
			param.setReplicationsArrangement(Integer.valueOf(3));
			param.setReplatinGroups("1,1,2");
			param.setNblatin("3"); // should be less than or equal the block level (ntreatment / blocksize)

			ExpDesignValidationOutput output = this.resolveIncompleteBlockDesign.validate(param, germplasmList);
			Assert.assertEquals(true, output.isValid());

			List<MeasurementRow> measurementRowList =
					this.resolveIncompleteBlockDesign
							.generateDesign(germplasmList, param, trialVariables, factors, factors, variates, null);
			for (MeasurementRow measurementRow : measurementRowList) {
				System.out.println(measurementRow.toString());
			}
			Assert.assertEquals(96, measurementRowList.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testResolvableIncompleteBlockLatinizedRowsDesignService() {

		try {
			List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", 24);
			List<MeasurementVariable> trialVariables = new ArrayList<MeasurementVariable>();
			trialVariables.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(),PROGRAM_UUID), Operation.ADD,
					this.fieldbookService));

			List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.GID.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.DESIG.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();
			variates.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(20368,PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			ExpDesignParameterUi param = new ExpDesignParameterUi();
			param.setBlockSize("4");
			param.setReplicationsCount("4");
			param.setNoOfEnvironments("1");
			param.setUseLatenized(true);
			param.setReplicationsArrangement(Integer.valueOf(2));
			param.setReplatinGroups("1,1,1,1");
			param.setNblatin("3"); // should be less than or equal the block level (ntreatment / blocksize)

			ExpDesignValidationOutput output = this.resolveIncompleteBlockDesign.validate(param, germplasmList);
			Assert.assertEquals(true, output.isValid());

			List<MeasurementRow> measurementRowList =
					this.resolveIncompleteBlockDesign
							.generateDesign(germplasmList, param, trialVariables, factors, factors, variates, null);
			Assert.assertEquals(96, measurementRowList.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testResolvableIncompleteBlockLatinizedColsDesignService() {

		try {
			List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", 24);
			List<MeasurementVariable> trialVariables = new ArrayList<MeasurementVariable>();
			trialVariables.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(),PROGRAM_UUID), Operation.ADD,
					this.fieldbookService));

			List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.GID.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.DESIG.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();
			variates.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(20368,PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			ExpDesignParameterUi param = new ExpDesignParameterUi();
			param.setBlockSize("4");
			param.setReplicationsCount("4");
			param.setNoOfEnvironments("1");
			param.setUseLatenized(true);
			param.setReplicationsArrangement(Integer.valueOf(1));
			param.setReplatinGroups("4");
			param.setNblatin("3"); // should be less than or equal the block level (ntreatment / blocksize)

			ExpDesignValidationOutput output = this.resolveIncompleteBlockDesign.validate(param, germplasmList);
			Assert.assertEquals(true, output.isValid());

			List<MeasurementRow> measurementRowList =
					this.resolveIncompleteBlockDesign
							.generateDesign(germplasmList, param, trialVariables, factors, factors, variates, null);
			Assert.assertEquals(96, measurementRowList.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testResolvableRowColumnDesignService() {

		try {
			List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", 200);

			ExpDesignParameterUi param = new ExpDesignParameterUi();
			param.setRowsPerReplications("2");
			param.setColsPerReplications("100");
			param.setReplicationsCount("2");
			param.setNoOfEnvironments("2");

			// number of replicates should be 2 or more
			List<MeasurementVariable> trialVariables = new ArrayList<MeasurementVariable>();
			trialVariables.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(),PROGRAM_UUID), Operation.ADD,
					this.fieldbookService));

			List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.GID.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.DESIG.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();
			variates.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(20368,PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			ExpDesignValidationOutput output = this.resolveRowColumn.validate(param, germplasmList);
			Assert.assertEquals(true, output.isValid());

			List<MeasurementRow> measurementRowList =
					this.resolveRowColumn.generateDesign(germplasmList, param, trialVariables, factors, factors, variates, null);
			for (MeasurementRow measurementRow : measurementRowList) {
				System.out.println(measurementRow.toString());
			}

			Assert.assertEquals(800, measurementRowList.size());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testResolvableRowColumnAdjacentDesignService() {

		try {
			List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", 200);

			ExpDesignParameterUi param = new ExpDesignParameterUi();
			param.setRowsPerReplications("4");
			param.setColsPerReplications("50");
			param.setReplicationsCount("2");
			param.setNoOfEnvironments("2");
			param.setUseLatenized(true);
			param.setReplicationsArrangement(Integer.valueOf(3));
			param.setReplatinGroups("1,1");
			param.setNrlatin("2");
			param.setNclatin("2");

			// number of replicates should be 2 or more
			List<MeasurementVariable> trialVariables = new ArrayList<MeasurementVariable>();
			trialVariables.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(),PROGRAM_UUID), Operation.ADD,
					this.fieldbookService));

			List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.GID.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.DESIG.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();
			variates.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(20368,PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			ExpDesignValidationOutput output = this.resolveRowColumn.validate(param, germplasmList);
			Assert.assertEquals(true, output.isValid());

			List<MeasurementRow> measurementRowList =
					this.resolveRowColumn.generateDesign(germplasmList, param, trialVariables, factors, factors, variates, null);

			Assert.assertEquals(800, measurementRowList.size());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testResolvableRowColumnColsDesignService() {

		try {
			List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", 200);

			ExpDesignParameterUi param = new ExpDesignParameterUi();
			param.setRowsPerReplications("4");
			param.setColsPerReplications("50");
			param.setReplicationsCount("2");
			param.setNoOfEnvironments("2");
			param.setUseLatenized(true);
			param.setReplicationsArrangement(Integer.valueOf(1));
			param.setReplatinGroups("2");
			param.setNrlatin("2");
			param.setNclatin("2");

			// number of replicates should be 2 or more
			List<MeasurementVariable> trialVariables = new ArrayList<MeasurementVariable>();
			trialVariables.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(),PROGRAM_UUID), Operation.ADD,
					this.fieldbookService));

			List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.GID.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.DESIG.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();
			variates.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(20368,PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			ExpDesignValidationOutput output = this.resolveRowColumn.validate(param, germplasmList);
			Assert.assertEquals(true, output.isValid());

			List<MeasurementRow> measurementRowList =
					this.resolveRowColumn.generateDesign(germplasmList, param, trialVariables, factors, factors, variates, null);

			Assert.assertEquals(800, measurementRowList.size());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testResolvableRowColumnRowsDesignService() {

		try {
			List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", 200);

			ExpDesignParameterUi param = new ExpDesignParameterUi();
			param.setRowsPerReplications("4");
			param.setColsPerReplications("50");
			param.setReplicationsCount("3");
			param.setNoOfEnvironments("2");
			param.setUseLatenized(true);
			param.setReplicationsArrangement(Integer.valueOf(2));
			param.setReplatinGroups("1,1");
			param.setNrlatin("3");
			param.setNclatin("2");

			// number of replicates should be 2 or more
			List<MeasurementVariable> trialVariables = new ArrayList<MeasurementVariable>();
			trialVariables.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(),PROGRAM_UUID), Operation.ADD,
					this.fieldbookService));

			List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.GID.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.DESIG.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();
			variates.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(20368,PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			ExpDesignValidationOutput output = this.resolveRowColumn.validate(param, germplasmList);
			Assert.assertEquals(true, output.isValid());

			List<MeasurementRow> measurementRowList =
					this.resolveRowColumn.generateDesign(germplasmList, param, trialVariables, factors, factors, variates, null);

			Assert.assertEquals(1200, measurementRowList.size());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testRandomizeCompleteBlockDesignService() {

		try {
			List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", 200);

			List<TreatmentVariable> treatmentVarList = new ArrayList<TreatmentVariable>();

			TreatmentVariable treatmentVar1 = new TreatmentVariable();
			treatmentVar1.setLevelVariable(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(8284,PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			treatmentVar1.setValueVariable(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(8282,PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			TreatmentVariable treatmentVar2 = new TreatmentVariable();
			treatmentVar2.setLevelVariable(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(8377,PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			treatmentVar2.setValueVariable(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(8263,PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			treatmentVarList.add(treatmentVar2);

			ExpDesignParameterUi param = new ExpDesignParameterUi();
			param.setReplicationsCount("2");
			param.setNoOfEnvironments("2");

			Map<String, Map<String, List<String>>> treatmentFactorValues = new HashMap<String, Map<String, List<String>>>(); // Key - CVTerm
																																// ID , List
																																// of values
			Map<String, List<String>> treatmentData = new HashMap<String, List<String>>();
			treatmentData.put("labels", Arrays.asList("100", "200", "300"));

			treatmentFactorValues.put("8284", treatmentData);
			treatmentFactorValues.put("8377", treatmentData);

			param.setTreatmentFactorsData(treatmentFactorValues);

			// number of replicates should be 2 or more
			List<MeasurementVariable> trialVariables = new ArrayList<MeasurementVariable>();
			trialVariables.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(),PROGRAM_UUID), Operation.ADD,
					this.fieldbookService));

			List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.GID.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.DESIG.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();
			variates.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(20368,PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			ExpDesignValidationOutput output = this.randomizeBlockDesign.validate(param, germplasmList);
			Assert.assertEquals(true, output.isValid());

			List<MeasurementRow> measurementRowList =
					this.randomizeBlockDesign.generateDesign(germplasmList, param, trialVariables, factors, factors, variates,
							treatmentVarList);

			Assert.assertEquals(7200, measurementRowList.size());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<ImportedGermplasm> createGermplasmList(String prefix, int size) {
		List<ImportedGermplasm> list = new ArrayList<ImportedGermplasm>();

		for (int i = 0; i < size; i++) {
			ImportedGermplasm germplasm = new ImportedGermplasm(i + 1, prefix + (i + 1), null);
			list.add(germplasm);
		}

		return list;
	}

}
