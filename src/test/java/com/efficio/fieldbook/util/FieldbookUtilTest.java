
package com.efficio.fieldbook.util;

import org.generationcp.middleware.ruleengine.pojo.ImportedCross;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class FieldbookUtilTest {

	@Test
	public void testGetColumnOrderListIfThereAreParameters() {
		String columnOrderDelimited = "[\"1100\", \"1900\"]";
		List<Integer> columnOrderList = FieldbookUtil.getColumnOrderList(columnOrderDelimited);
		Assert.assertEquals("Should have 2 integer list", 2, columnOrderList.size());
	}

	@Test
	public void testGetColumnOrderListIfThereAreNoParameters() {
		String columnOrderDelimited = "[ ]";
		List<Integer> columnOrderList = FieldbookUtil.getColumnOrderList(columnOrderDelimited);
		Assert.assertEquals("Should have 0 integer list", 0, columnOrderList.size());
	}

	@Test
	public void testSetColumnOrderingOnWorkbook() {
		Workbook workbook = new Workbook();
		String columnOrderDelimited = "[\"1100\", \"1900\"]";
		FieldbookUtil.setColumnOrderingOnWorkbook(workbook, columnOrderDelimited);
		List<Integer> orderedTermIds = workbook.getColumnOrderedLists();
		Assert.assertEquals("1st element should have term id 1100", 1100, orderedTermIds.get(0).intValue());
		Assert.assertEquals("2nd element should have term id 1900", 1900, orderedTermIds.get(1).intValue());
	}

	@Test
	public void testIsPlotDuplicateNonFirstInstanceIfNotFirstInstance() {
		ImportedCross crosses = new ImportedCross();
		crosses.setDuplicatePrefix(ImportedCross.PLOT_DUPE_PREFIX);
		crosses.setEntryNumber(6);
		Set<Integer> dupeEntries = new TreeSet<>();
		dupeEntries.add(5);
		dupeEntries.add(7);
		crosses.setDuplicateEntries(dupeEntries);
		Assert.assertTrue("Should return true since its not the first instance", FieldbookUtil.isPlotDuplicateNonFirstInstance(crosses));
	}

	@Test
	public void testisPlotDuplicateNonFirstInstanceIfFirstInstance() {
		ImportedCross crosses = new ImportedCross();
		crosses.setDuplicatePrefix(ImportedCross.PLOT_DUPE_PREFIX);
		crosses.setEntryNumber(2);
		Set<Integer> dupeEntries = new TreeSet<>();
		dupeEntries.add(5);
		dupeEntries.add(7);
		crosses.setDuplicateEntries(dupeEntries);
		Assert.assertFalse("Should return false since its not the first instance", FieldbookUtil.isPlotDuplicateNonFirstInstance(crosses));
	}

	@Test
	public void testisPlotDuplicateNonFirstInstanceIfPedigreeDupe() {
		ImportedCross crosses = new ImportedCross();
		crosses.setDuplicatePrefix(ImportedCross.PEDIGREE_DUPE_PREFIX);
		crosses.setEntryNumber(2);
		Set<Integer> dupeEntries = new TreeSet<>();
		dupeEntries.add(5);
		dupeEntries.add(7);
		crosses.setDuplicateEntries(dupeEntries);
		Assert.assertFalse("Should return false since its a pedigree dupe", FieldbookUtil.isPlotDuplicateNonFirstInstance(crosses));
	}

	@Test
	public void testisPlotDuplicateNonFirstInstanceIfPlotDupeButNoLitOfDuplicate() {
		ImportedCross crosses = new ImportedCross();
		crosses.setDuplicatePrefix(ImportedCross.PLOT_DUPE_PREFIX);
		crosses.setEntryNumber(2);
		crosses.setDuplicateEntries(null);
		Assert.assertFalse("Should return false since its it has no list of duplicate entries",
				FieldbookUtil.isPlotDuplicateNonFirstInstance(crosses));
	}

	@Test
	public void testMergeCrossesPlotDuplicateDataIfThereIsDuplicate() {
		ImportedCross crosses1 = new ImportedCross();
		crosses1.setDuplicatePrefix(ImportedCross.PLOT_DUPE_PREFIX);
		crosses1.setEntryNumber(1);
		ImportedCross crosses2 = new ImportedCross();
		crosses2.setDuplicatePrefix(ImportedCross.PLOT_DUPE_PREFIX);
		crosses2.setEntryNumber(2);
		crosses2.setGid("11223");
		crosses2.setCross("Cross 11223");
		ImportedCross crosses3 = new ImportedCross();
		crosses3.setDuplicatePrefix(ImportedCross.PLOT_DUPE_PREFIX);
		crosses3.setEntryNumber(3);
		Set<Integer> dupeEntries = new TreeSet<>();
		dupeEntries.add(2);
		dupeEntries.add(7);
		crosses3.setDuplicateEntries(dupeEntries);
		List<ImportedCross> importedCrossList = new ArrayList<>();
		importedCrossList.add(crosses1);
		importedCrossList.add(crosses2);
		importedCrossList.add(crosses3);
		FieldbookUtil.mergeCrossesPlotDuplicateData(crosses3, importedCrossList);
		Assert.assertEquals("Gid should be the same as crosses 2 since its the duplicate", crosses2.getGid(), crosses3.getGid());
		Assert.assertEquals("Cross should be the same as crosses 2 since its the duplicate", crosses2.getCross(), crosses3.getCross());
	}

	@Test
	public void testMergeCrossesPlotDuplicateDataIfThereIsNoDuplicate() {
		ImportedCross crosses1 = new ImportedCross();
		crosses1.setDuplicatePrefix(ImportedCross.PLOT_DUPE_PREFIX);
		crosses1.setEntryNumber(1);
		ImportedCross crosses2 = new ImportedCross();
		crosses2.setDuplicatePrefix(ImportedCross.PLOT_DUPE_PREFIX);
		crosses2.setEntryNumber(2);
		crosses2.setGid("11223");
		crosses2.setCross("Cross 11223");
		ImportedCross crosses3 = new ImportedCross();
		crosses3.setDuplicatePrefix(ImportedCross.PLOT_DUPE_PREFIX);
		crosses3.setEntryNumber(3);
		crosses3.setGid("555");
		crosses3.setDuplicateEntries(null);
		List<ImportedCross> importedCrossList = new ArrayList<>();
		importedCrossList.add(crosses1);
		importedCrossList.add(crosses2);
		importedCrossList.add(crosses3);
		FieldbookUtil.mergeCrossesPlotDuplicateData(crosses3, importedCrossList);
		Assert.assertEquals("Gid should still be the same since there is no duplicate", "555", crosses3.getGid());
	}

	@Test
	public void testIsContinueCrossingMergeWhenMergeConditionIsFalse() {
		ImportedCross crosses3 = new ImportedCross();
		crosses3.setDuplicatePrefix(ImportedCross.PLOT_DUPE_PREFIX);
		crosses3.setEntryNumber(3);
		Set<Integer> dupeEntries = new TreeSet<>();
		dupeEntries.add(4);
		dupeEntries.add(7);
		crosses3.setDuplicateEntries(dupeEntries);
		Assert.assertFalse("Should return false since there is no plot duplicate",
				FieldbookUtil.isContinueCrossingMerge(false, false, crosses3));
		Assert.assertFalse("Should return false since there is preserve plot duplicate true",
				FieldbookUtil.isContinueCrossingMerge(true, true, crosses3));
		Assert.assertFalse("Should return false since duplicate entries is non first instance",
				FieldbookUtil.isContinueCrossingMerge(true, true, crosses3));
	}

	@Test
	public void testIsContinueCrossingMergeWhenMergeConditionIsTrue() {
		ImportedCross crosses3 = new ImportedCross();
		crosses3.setDuplicatePrefix(ImportedCross.PLOT_DUPE_PREFIX);
		crosses3.setEntryNumber(3);
		Set<Integer> dupeEntries = new TreeSet<>();
		dupeEntries.add(2);
		dupeEntries.add(7);
		crosses3.setDuplicateEntries(dupeEntries);
		Assert.assertTrue("Should return true since there is plot duplicate, preserve plot duplicate is false and non first instance",
				FieldbookUtil.isContinueCrossingMerge(true, false, crosses3));
	}

	@Test
	public void testIsFieldmapColOrRange() {
		MeasurementVariable var = new MeasurementVariable();
		var.setTermId(TermId.COLUMN_NO.getId());

		Assert.assertTrue("Should return true since its COL", FieldbookUtil.isFieldmapColOrRange(var));

		var.setTermId(TermId.RANGE_NO.getId());
		Assert.assertTrue("Should return true since its RANGE", FieldbookUtil.isFieldmapColOrRange(var));

		var.setTermId(TermId.BLOCK_ID.getId());
		Assert.assertFalse("Should return false since its not col and range", FieldbookUtil.isFieldmapColOrRange(var));
	}

	@Test
	public void testCreateResponseEntityForFileDownload() throws UnsupportedEncodingException {
		final String filename = "testFile.xls";
		ResponseEntity<FileSystemResource> result = FieldbookUtil.createResponseEntityForFileDownload(filename, filename);

		Assert.assertEquals("Make sure we get a http success", HttpStatus.OK, result.getStatusCode());

		Assert.assertNotNull("Make sure Content-disposition header exists", result.getHeaders().get(FileUtils.CONTENT_DISPOSITION));
		Assert.assertNotNull("Make sure we have a Content-Type header",result.getHeaders().get(FileUtils.CONTENT_TYPE));
		Assert.assertNotNull("Make sure we have a Content-Type header that contains at least 1 value", result.getHeaders().get(FileUtils.CONTENT_TYPE).get(0));

		// Were not testing the mime type detection here, see a separate unit test for FileUTils.detectMimeType(...)
		Assert.assertTrue("Make sure tht content-type header has a charset", result.getHeaders().get(FileUtils.CONTENT_TYPE).get(0).contains("charset=utf-8"));
	}

}
