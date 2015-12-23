
package com.efficio.fieldbook.service.internal;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.efficio.fieldbook.service.internal.impl.BVDesignRunner;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExpDesignUtil;

public class BVDesignRunnerTest {

	@Test
	public void testGetXMLStringForRandomizedCompleteBlockDesign() {
		List<String> treatmentFactor = new ArrayList<>();
		treatmentFactor.add("ENTRY_NO");
		treatmentFactor.add("FERTILIZER");

		List<String> levels = new ArrayList<>();
		levels.add("24");
		levels.add("3");

		MainDesign mainDesign = ExpDesignUtil.createRandomizedCompleteBlockDesign("6", "Reps", "Plots", 301, 201, treatmentFactor, levels, "");

		String expectedString =
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Templates><Template name=\"RandomizedBlock\">"
						+ "<Parameter name=\""
						+ ExpDesignUtil.SEED_PARAM
						+ "\" value=\":seedValue\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.NBLOCKS_PARAM
						+ "\" value=\"6\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.BLOCKFACTOR_PARAM
						+ "\" value=\"Reps\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.PLOTFACTOR_PARAM
						+ "\" value=\"Plots\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.INITIAL_PLOT_NUMBER_PARAM
						+ "\" value=\"301\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.INITIAL_TREATMENT_NUMBER_PARAM
						+ "\" value=\"201\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.TREATMENTFACTORS_PARAM
						+ "\"><ListItem value=\"ENTRY_NO\"/><ListItem value=\"FERTILIZER\"/></Parameter>"
						+ "<Parameter name=\"levels\"><ListItem value=\"24\"/><ListItem value=\"3\"/></Parameter>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.TIMELIMIT_PARAM
						+ "\" value=\""
						+ AppConstants.EXP_DESIGN_TIME_LIMIT.getString()
						+ "\"/>"
						+ "<Parameter name=\"" + ExpDesignUtil.OUTPUTFILE_PARAM + "\" value=\":outputFile\"/></Template></Templates>";

		BVDesignRunner runner = new BVDesignRunner();
		String xmlString = runner.getXMLStringForDesign(mainDesign);

		this.assertXMLStringEqualsExpected(mainDesign, expectedString, xmlString);
	}

	@Test
	public void testGetXMLStringForResolvableIncompleteBlockDesign() {
		MainDesign mainDesign =
				ExpDesignUtil.createResolvableIncompleteBlockDesign("6", "24", "2", "Treat", "Reps", "Subblocks", "Plots", 301, null, "0", "", "", false);

		String expectedString =
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Templates><Template name=\"ResolvableIncompleteBlock\">"
						+ "<Parameter name=\""
						+ ExpDesignUtil.SEED_PARAM
						+ "\" value=\":seedValue\"/><Parameter name=\""
						+ ExpDesignUtil.BLOCKSIZE_PARAM
						+ "\" value=\"6\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.NTREATMENTS_PARAM
						+ "\" value=\"24\"/><Parameter name=\""
						+ ExpDesignUtil.NREPLICATES_PARAM
						+ "\" value=\"2\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.TREATMENTFACTOR_PARAM
						+ "\" value=\"Treat\"/><Parameter name=\""
						+ ExpDesignUtil.REPLICATEFACTOR_PARAM
						+ "\" value=\"Reps\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.BLOCKFACTOR_PARAM
						+ "\" value=\"Subblocks\"/><Parameter name=\""
						+ ExpDesignUtil.PLOTFACTOR_PARAM
						+ "\" value=\"Plots\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.INITIAL_PLOT_NUMBER_PARAM
						+ "\" value=\"301\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.NBLATIN_PARAM
						+ "\" value=\"0\"/><Parameter name=\""
						+ ExpDesignUtil.TIMELIMIT_PARAM
						+ "\" value=\""
						+ AppConstants.EXP_DESIGN_TIME_LIMIT.getString()
						+ "\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.OUTPUTFILE_PARAM
						+ "\" value=\":outputFile\"/></Template></Templates>";

		BVDesignRunner runner = new BVDesignRunner();
		String xmlString = runner.getXMLStringForDesign(mainDesign);

		this.assertXMLStringEqualsExpected(mainDesign, expectedString, xmlString);
	}

	@Test
	public void testGetXMLStringForResolvableRowColExpDesign() {
		MainDesign mainDesign = ExpDesignUtil.createResolvableRowColDesign("50", "2", "5", "10", "Treat", "Reps", "Rows", "Columns",
				"Plots", 301, null, "0", "0", "", "", false);

		String expectedString =
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Templates><Template name=\"ResolvableRowColumn\">"
						+ "<Parameter name=\""
						+ ExpDesignUtil.SEED_PARAM
						+ "\" value=\":seedValue\"/><Parameter name=\""
						+ ExpDesignUtil.NTREATMENTS_PARAM
						+ "\" value=\"50\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.NREPLICATES_PARAM
						+ "\" value=\"2\"/><Parameter name=\""
						+ ExpDesignUtil.NROWS_PARAM
						+ "\" value=\"5\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.NCOLUMNS_PARAM
						+ "\" value=\"10\"/><Parameter name=\""
						+ ExpDesignUtil.TREATMENTFACTOR_PARAM
						+ "\" value=\"Treat\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.REPLICATEFACTOR_PARAM
						+ "\" value=\"Reps\"/><Parameter name=\""
						+ ExpDesignUtil.ROWFACTOR_PARAM
						+ "\" value=\"Rows\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.COLUMNFACTOR_PARAM
						+ "\" value=\"Columns\"/><Parameter name=\""
						+ ExpDesignUtil.PLOTFACTOR_PARAM
						+ "\" value=\"Plots\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.INITIAL_PLOT_NUMBER_PARAM
						+ "\" value=\"301\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.NRLATIN_PARAM
						+ "\" value=\"0\"/><Parameter name=\""
						+ ExpDesignUtil.NCLATIN_PARAM
						+ "\" value=\"0\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.TIMELIMIT_PARAM
						+ "\" value=\""
						+ AppConstants.EXP_DESIGN_TIME_LIMIT.getString()
						+ "\"/>"
						+ "<Parameter name=\"" + ExpDesignUtil.OUTPUTFILE_PARAM + "\" value=\":outputFile\"/></Template></Templates>";

		BVDesignRunner runner = new BVDesignRunner();
		String xmlString = runner.getXMLStringForDesign(mainDesign);

		this.assertXMLStringEqualsExpected(mainDesign, expectedString, xmlString);
	}


	@Test
	public void testGetXMLStringForResolvableIncompleteBlockDesignWithEntryNumber() {
		MainDesign mainDesign = ExpDesignUtil.createResolvableIncompleteBlockDesign("6", "24", "2", "ENTRY_NO", "Reps", "Subblocks", "Plots", 301, 245, "0", "", "", false);

		String expectedString =
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Templates><Template name=\"ResolvableIncompleteBlock\">"
						+ "<Parameter name=\""
						+ ExpDesignUtil.SEED_PARAM
						+ "\" value=\":seedValue\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.BLOCKSIZE_PARAM
						+ "\" value=\"6\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.NTREATMENTS_PARAM
						+ "\" value=\"24\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.NREPLICATES_PARAM
						+ "\" value=\"2\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.TREATMENTFACTOR_PARAM
						+ "\" value=\"ENTRY_NO\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.INITIAL_TREATMENT_NUMBER_PARAM
						+ "\" value=\"245\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.REPLICATEFACTOR_PARAM
						+ "\" value=\"Reps\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.BLOCKFACTOR_PARAM
						+ "\" value=\"Subblocks\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.PLOTFACTOR_PARAM
						+ "\" value=\"Plots\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.INITIAL_PLOT_NUMBER_PARAM
						+ "\" value=\"301\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.NBLATIN_PARAM
						+ "\" value=\"0\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.TIMELIMIT_PARAM
						+ "\" value=\"" + AppConstants.EXP_DESIGN_TIME_LIMIT.getString() + "\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.OUTPUTFILE_PARAM
						+ "\" value=\":outputFile\"/></Template></Templates>";

		BVDesignRunner runner = new BVDesignRunner();
		String xmlString = runner.getXMLStringForDesign(mainDesign);

		this.assertXMLStringEqualsExpected(mainDesign, expectedString, xmlString);
	}

	@Test
	public void testGetXMLStringForResolvableRowColumnDesignWithEntryNumber() {
		MainDesign mainDesign = ExpDesignUtil.createResolvableRowColDesign("24", "2", "5", "10", "ENTRY_NO", "Reps", "Rows", "Columns",
				"Plots", 301, 245, "0", "0", "", "", false);

		String expectedString =
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Templates><Template name=\"ResolvableRowColumn\">"
						+ "<Parameter name=\""
						+ ExpDesignUtil.SEED_PARAM
						+ "\" value=\":seedValue\"/><Parameter name=\""
						+ ExpDesignUtil.NTREATMENTS_PARAM
						+ "\" value=\"24\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.NREPLICATES_PARAM
						+ "\" value=\"2\"/><Parameter name=\""
						+ ExpDesignUtil.NROWS_PARAM
						+ "\" value=\"5\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.NCOLUMNS_PARAM
						+ "\" value=\"10\"/><Parameter name=\""
						+ ExpDesignUtil.TREATMENTFACTOR_PARAM
						+ "\" value=\"ENTRY_NO\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.INITIAL_TREATMENT_NUMBER_PARAM
						+ "\" value=\"245\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.REPLICATEFACTOR_PARAM
						+ "\" value=\"Reps\"/><Parameter name=\""
						+ ExpDesignUtil.ROWFACTOR_PARAM
						+ "\" value=\"Rows\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.COLUMNFACTOR_PARAM
						+ "\" value=\"Columns\"/><Parameter name=\""
						+ ExpDesignUtil.PLOTFACTOR_PARAM
						+ "\" value=\"Plots\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.INITIAL_PLOT_NUMBER_PARAM
						+ "\" value=\"301\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.NRLATIN_PARAM
						+ "\" value=\"0\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.NCLATIN_PARAM
						+ "\" value=\"0\"/>"
						+ "<Parameter name=\""
						+ ExpDesignUtil.TIMELIMIT_PARAM
						+ "\" value=\""
						+ AppConstants.EXP_DESIGN_TIME_LIMIT.getString()
						+ "\"/>"
						+ "<Parameter name=\"" + ExpDesignUtil.OUTPUTFILE_PARAM + "\" value=\":outputFile\"/></Template></Templates>";

		BVDesignRunner runner = new BVDesignRunner();
		String xmlString = runner.getXMLStringForDesign(mainDesign);
		this.assertXMLStringEqualsExpected(mainDesign, expectedString, xmlString);
	}

	private void assertXMLStringEqualsExpected(MainDesign mainDesign, String expectedString, String xmlString) {
		String outputFile = mainDesign.getDesign().getParameterValue(ExpDesignUtil.OUTPUTFILE_PARAM);
		String outputFileMillisecs = outputFile.replace(BVDesignRunner.BV_PREFIX + BVDesignRunner.CSV_EXTENSION, "");
		String seedValue = this.getSeedValue(outputFileMillisecs);
		expectedString = expectedString.replace(":seedValue", seedValue);
		expectedString = expectedString.replace(":outputFile", outputFile);

		Assert.assertEquals(expectedString, xmlString);
	}

	private String getSeedValue(String currentTimeMillis) {
		String seedValue = currentTimeMillis;
		if (Long.parseLong(currentTimeMillis) > Integer.MAX_VALUE) {
			seedValue = seedValue.substring(seedValue.length() - 9);
		}
		return seedValue;
	}
}
