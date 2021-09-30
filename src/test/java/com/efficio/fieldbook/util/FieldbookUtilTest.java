
package com.efficio.fieldbook.util;

import org.generationcp.commons.parsing.pojo.ImportedCross;
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
