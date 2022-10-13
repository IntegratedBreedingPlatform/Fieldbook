package com.efficio.fieldbook.util;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.middleware.ruleengine.pojo.ImportedCross;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public class FieldbookUtil {

	public static final String ISO_8859_1 = "iso-8859-1";
	private static final Logger LOG = LoggerFactory.getLogger(FieldbookUtil.class);
	public static final String DESCRIPTION = "Description";
	private static final String START_DATE = "startDate";
	private static final String END_DATE = "endDate";
	private static final String STUDY_UPDATE = "studyUpdate";
	private static final String OBJECTIVE = "Objective";
	private static final String STUDY_NAME = "Name";
	private static final String CREATED_BY = "createdBy";
	public static final String STUDYTYPE="StudyType";

	private static FieldbookUtil instance;

	static {
		FieldbookUtil.instance = new FieldbookUtil();
	}

	private FieldbookUtil() {
		// empty constructor
	}

	public static FieldbookUtil getInstance() {
		return FieldbookUtil.instance;
	}

	public static List<Integer> getColumnOrderList(final String columnOrders) {
		if (columnOrders != null && !"".equalsIgnoreCase(columnOrders)) {
			try {
				final ObjectMapper mapper = new ObjectMapper();
				final Integer[] columnsOrderList;
				columnsOrderList = mapper.readValue(columnOrders, Integer[].class);
				return Arrays.asList(columnsOrderList);
			} catch (final JsonParseException e) {
				FieldbookUtil.LOG.error(e.getMessage(), e);
			} catch (final JsonMappingException e) {
				FieldbookUtil.LOG.error(e.getMessage(), e);
			} catch (final IOException e) {
				FieldbookUtil.LOG.error(e.getMessage(), e);
			}

		}
		return new ArrayList<>();
	}

	public static void setColumnOrderingOnWorkbook(final Workbook workbook, final String columnOrderDelimited) {
		final List<Integer> columnOrdersList = FieldbookUtil.getColumnOrderList(columnOrderDelimited);
		if (!columnOrdersList.isEmpty()) {
			workbook.setColumnOrderedLists(columnOrdersList);
		}
	}

	public static String generateEntryCode(final int index) {
		return AppConstants.ENTRY_CODE_PREFIX.getString() + String.format("%04d", index);
	}

	public static boolean isPlotDuplicateNonFirstInstance(final ImportedCross crosses) {
		if (crosses.isPlotDupe() && crosses.getDuplicateEntries() != null && crosses.getEntryNumber() > crosses.getDuplicateEntries().iterator()
				.next()) {
			return true;
		}
		return false;
	}

	public static void mergeCrossesPlotDuplicateData(final ImportedCross crosses, final List<ImportedCross> importedGermplasmList) {
		if (FieldbookUtil.isPlotDuplicateNonFirstInstance(crosses)) {
			// get the 1st instance of duplicate from the list
			final Integer firstInstanceDuplicate = crosses.getDuplicateEntries().iterator().next();
			// needed to minus 1 since a list is 0 based
			final ImportedCross firstInstanceCrossGermplasm = importedGermplasmList.get(firstInstanceDuplicate - 1);
			crosses.setGid(firstInstanceCrossGermplasm.getGid());
			crosses.setCross(firstInstanceCrossGermplasm.getCross());
			crosses.setDesig(firstInstanceCrossGermplasm.getDesig());
		}
	}

	public static boolean isContinueCrossingMerge(
		final boolean hasPlotDuplicate, final boolean isPreservePlotDuplicate, final ImportedCross cross) {
		if (hasPlotDuplicate && !isPreservePlotDuplicate && FieldbookUtil.isPlotDuplicateNonFirstInstance(cross)) {
			return true;
		}
		return false;
	}


	public static List<Integer> getFilterForMeansAndStatisticalVars() {

		final List<Integer> isAIds = new ArrayList<>();
		final StringTokenizer token = new StringTokenizer(AppConstants.FILTER_MEAN_AND_STATISCAL_VARIABLES_IS_A_IDS.getString(), ",");
		while (token.hasMoreTokens()) {
			isAIds.add(Integer.valueOf(token.nextToken()));
		}
		return isAIds;
	}

	public static boolean isFieldmapColOrRange(final MeasurementVariable var) {
		if (var.getTermId() == TermId.COLUMN_NO.getId() || var.getTermId() == TermId.RANGE_NO.getId()) {
			return true;
		}
		return false;
	}

	/**
	 * Creates ResponseEntity to download a file from a controller.
	 *
	 * @param fileWithFullPath  - path of the file to be downloaded
	 * @param filename  - the filename that will be set in the http response header
	 * @return
	 */
	public static ResponseEntity<FileSystemResource> createResponseEntityForFileDownload(final String fileWithFullPath, final String filename) throws
		UnsupportedEncodingException {
		final HttpHeaders respHeaders = new HttpHeaders();

		final File resource = new File(fileWithFullPath);
		final FileSystemResource fileSystemResource = new FileSystemResource(resource);

		final String mimeType = FileUtils.detectMimeType(filename);
		final String sanitizedFilename = FileUtils.sanitizeFileName(filename);

		respHeaders.set(FileUtils.CONTENT_TYPE,String.format("%s;charset=utf-8",mimeType));
		respHeaders.set(FileUtils.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"; filename*=utf-8\'\'%s", sanitizedFilename, FileUtils.encodeFilenameForDownload(sanitizedFilename)));

		return new ResponseEntity<>(fileSystemResource, respHeaders, HttpStatus.OK);

	}

	public List<Integer> buildVariableIDList(final String idList) {
		final List<Integer> requiredVariables = new ArrayList<>();
		final StringTokenizer token = new StringTokenizer(idList, ",");
		while (token.hasMoreTokens()) {
			final String s = token.nextToken();
			// FIXME BMS-4397
			if (!DESCRIPTION.equals(s) && !START_DATE.equals(s) && !END_DATE.equals(s) && !STUDY_UPDATE.equals(s) && !OBJECTIVE.equals(s)
				&& !STUDY_NAME.equals(s) && !CREATED_BY.equals(s) && !STUDYTYPE.equals(s)) {
				requiredVariables.add(Integer.valueOf(s));
			}
		}
		return requiredVariables;
	}
}
