
package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.Resource;

import org.apache.poi.ss.util.WorkbookUtil;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportColumnValue;
import org.generationcp.commons.service.GermplasmExportService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.ZipUtil;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import com.efficio.fieldbook.web.common.service.ExportAdvanceListService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.efficio.fieldbook.web.util.SettingsUtil;

public class ExportAdvanceListServiceImpl implements ExportAdvanceListService {

	private static final String DEFAULT_AMOUNT_HEADER = "SEED_AMOUNT_G";

	private static final Logger LOG = LoggerFactory.getLogger(ExportAdvanceListServiceImpl.class);

	@Resource
	private FieldbookProperties fieldbookProperties;

	@Resource
	private InventoryService inventoryMiddlewareService;

	@Resource
	public MessageSource messageSource;

	@Resource
	private FieldbookService fieldbookMiddlewareService;
	
	@Resource
	private ContextUtil contextUtil;

	private static final String NO_FILE = "noFile";

	private static final String ADVANCE_LIST_SHEET_NAME = "Advance List";

	private static final String STOCK_LIST_EXPORT_SHEET_NAME = "Inventory List";

	@Override
	public File exportAdvanceGermplasmList(final String delimitedAdvanceGermplasmListIds, final String studyName,
			final GermplasmExportService germplasmExportServiceImpl, final String type) {
		final List<Integer> advanceGermplasmListIds = this.parseDelimitedAdvanceGermplasmListIds(delimitedAdvanceGermplasmListIds);
		final List<String> filenameList = new ArrayList<>();
		String outputFilename = ExportAdvanceListServiceImpl.NO_FILE;
		final String suffix = AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString().equalsIgnoreCase(type)
				? AppConstants.EXPORT_XLS_SUFFIX.getString() : AppConstants.EXPORT_CSV_SUFFIX.getString();

		try {
			for (final Integer advanceGermpasmListId : advanceGermplasmListIds) {
				final List<InventoryDetails> inventoryDetailList =
						this.inventoryMiddlewareService.getInventoryDetailsByGermplasmList(advanceGermpasmListId);
				final GermplasmList germplasmList = this.fieldbookMiddlewareService.getGermplasmListById(advanceGermpasmListId);
				final String advanceListName = germplasmList.getName();
				final String filenamePath = this.getFileNamePath(advanceListName) + suffix;
				final String sheetName = WorkbookUtil.createSafeSheetName(ExportAdvanceListServiceImpl.ADVANCE_LIST_SHEET_NAME);
	
				this.exportList(inventoryDetailList, filenamePath, sheetName, germplasmExportServiceImpl, type, false);
	
				outputFilename = filenamePath;
				filenameList.add(filenamePath);
			}

			if (filenameList.size() > 1) {
				final String fileNameWithoutExtension =  SettingsUtil.cleanSheetAndFileName(studyName + "-" + AppConstants.ADVANCE_ZIP_DEFAULT_FILENAME.getString());
				outputFilename = this.zipFileNameList(fileNameWithoutExtension, filenameList);
			}
		} catch (final IOException | MiddlewareQueryException e) {
			ExportAdvanceListServiceImpl.LOG.error(e.getMessage(), e);
		}

		return new File(outputFilename);
	}

	@Override
	public File exportStockList(final Integer stockListId, final GermplasmExportService germplasmExportServiceImpl) {

		final List<String> filenameList = new ArrayList<>();
		String outputFilename = ExportAdvanceListServiceImpl.NO_FILE;
		final String suffix = AppConstants.EXPORT_XLS_SUFFIX.getString();

		try {
			final GermplasmList germplasmList = this.fieldbookMiddlewareService.getGermplasmListById(stockListId);
			final GermplasmListType germplasmListType = GermplasmListType.valueOf(germplasmList.getType());
			final List<InventoryDetails> inventoryDetailList =
					this.inventoryMiddlewareService.getInventoryListByListDataProjectListId(stockListId);

			final String advanceListName = germplasmList.getName();
			final String filenamePath = this.getFileNamePath(advanceListName) + suffix;
			final String sheetName =
					org.apache.poi.ss.util.WorkbookUtil.createSafeSheetName(ExportAdvanceListServiceImpl.STOCK_LIST_EXPORT_SHEET_NAME);

			this.exportList(inventoryDetailList, filenamePath, sheetName, germplasmExportServiceImpl,
					AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString(), GermplasmListType.isCrosses(germplasmListType));

			outputFilename = filenamePath;
			filenameList.add(filenamePath);
		} catch (IOException | MiddlewareQueryException e) {
			ExportAdvanceListServiceImpl.LOG.error(e.getMessage(), e);
		}

		return new File(outputFilename);
	}

	protected String getFileNamePath(final String name) {
		return this.fieldbookProperties.getUploadDirectory() + File.separator + SettingsUtil.cleanSheetAndFileName(name);
	}

	protected void exportList(final List<InventoryDetails> inventoryDetailList, final String filenamePath, final String sheetName,
			final GermplasmExportService germplasmExportServiceImpl, final String type, final boolean displayCrossRelatedColumns)
			throws IOException {
		final List<ExportColumnHeader> exportColumnHeaders =
				this.generateAdvanceListColumnHeaders(displayCrossRelatedColumns, this.getAmountsHeader(inventoryDetailList));
		if (AppConstants.EXPORT_ADVANCE_NURSERY_EXCEL.getString().equalsIgnoreCase(type)) {
			germplasmExportServiceImpl.generateExcelFileForSingleSheet(
					this.generateAdvanceListColumnValues(inventoryDetailList, exportColumnHeaders), exportColumnHeaders, filenamePath,
					sheetName);
		} else {
			germplasmExportServiceImpl.generateCSVFile(this.generateAdvanceListColumnValues(inventoryDetailList, exportColumnHeaders),
					exportColumnHeaders, filenamePath);
		}
	}

	String getAmountsHeader(final List<InventoryDetails> inventoryDetailList) {
		for (final InventoryDetails inventoryDetails : inventoryDetailList) {
			if (inventoryDetails.getScaleName() != null) {
				return inventoryDetails.getScaleName();
			}
		}
		return ExportAdvanceListServiceImpl.DEFAULT_AMOUNT_HEADER;
	}

	protected String zipFileNameList(final String outputFilename, final List<String> filenameList) throws IOException {
		final ZipUtil zipUtil = new ZipUtil();
		return zipUtil.zipIt(outputFilename, filenameList, this.contextUtil.getProjectInContext(), ToolName.TRIAL_MANAGER_FIELDBOOK_WEB);
	}

	protected List<Integer> parseDelimitedAdvanceGermplasmListIds(final String advancedListIds) {
		final List<Integer> advancedGermplasmListIds = new ArrayList<>();
		final StringTokenizer tokenizer = new StringTokenizer(advancedListIds, "|");
		while (tokenizer.hasMoreTokens()) {
			advancedGermplasmListIds.add(Integer.valueOf(tokenizer.nextToken()));
		}
		return advancedGermplasmListIds;
	}

	protected List<ExportColumnHeader> generateAdvanceListColumnHeaders(final boolean displayCrossRelatedColumns,
			final String amountHeader) {
		final List<ExportColumnHeader> exportColumnHeaders = new ArrayList<>();
		final Locale locale = LocaleContextHolder.getLocale();

		exportColumnHeaders.add(new ExportColumnHeader(TermId.ENTRY_NO.getId(),
				this.messageSource.getMessage("seed.entry.number", null, locale), true, ExportColumnHeader.GREEN));
		exportColumnHeaders.add(new ExportColumnHeader(TermId.DESIG.getId(),
				this.messageSource.getMessage("seed.entry.designation", null, locale), true, ExportColumnHeader.GREEN));
		exportColumnHeaders.add(new ExportColumnHeader(TermId.CROSS.getId(),
				this.messageSource.getMessage("seed.entry.parentage", null, locale), true, ExportColumnHeader.GREEN));
		exportColumnHeaders.add(new ExportColumnHeader(TermId.GID.getId(),
				this.messageSource.getMessage("seed.inventory.gid", null, locale), true, ExportColumnHeader.GREEN));
		exportColumnHeaders.add(new ExportColumnHeader(TermId.SOURCE.getId(),
				this.messageSource.getMessage("seed.inventory.source", null, locale), true, ExportColumnHeader.GREEN));

		if (displayCrossRelatedColumns) {
			exportColumnHeaders.add(new ExportColumnHeader(TermId.DUPLICATE.getId(),
					this.messageSource.getMessage("seed.inventory.duplicate", null, locale), true, ExportColumnHeader.BLUE));
			exportColumnHeaders.add(new ExportColumnHeader(TermId.BULK_WITH.getId(),
					this.messageSource.getMessage("seed.inventory.bulk.with", null, locale), true, ExportColumnHeader.BLUE));
			exportColumnHeaders.add(new ExportColumnHeader(TermId.BULK_COMPL.getId(),
					this.messageSource.getMessage("seed.inventory.bulk.compl", null, locale), true, ExportColumnHeader.BLUE));
		}

		exportColumnHeaders.add(new ExportColumnHeader(TermId.LOCATION_ID.getId(),
				this.messageSource.getMessage("seed.inventory.table.location", null, locale), true, ExportColumnHeader.BLUE));
		// Always use TermId.SEED_AMOUNT_G for inventory amount id to align with expected id in GermplasmExportService in Commons
		exportColumnHeaders.add(new ExportColumnHeader(TermId.SEED_AMOUNT_G.getId(), amountHeader, true, ExportColumnHeader.BLUE));
		exportColumnHeaders.add(new ExportColumnHeader(TermId.STOCKID.getId(),
				this.messageSource.getMessage("seed.inventory.stockid", null, locale), true, ExportColumnHeader.BLUE));
		exportColumnHeaders.add(new ExportColumnHeader(AppConstants.TEMPORARY_INVENTORY_COMMENT.getInt(),
				this.messageSource.getMessage("seed.inventory.comment", null, locale), true, ExportColumnHeader.BLUE));
		return exportColumnHeaders;
	}

	protected List<Map<Integer, ExportColumnValue>> generateAdvanceListColumnValues(final List<InventoryDetails> inventoryDetailList,
			final List<ExportColumnHeader> exportColumnHeaders) {
		final List<Map<Integer, ExportColumnValue>> exportColumnValues = new ArrayList<>();
		for (final InventoryDetails inventoryDetails : inventoryDetailList) {
			final Map<Integer, ExportColumnValue> dataMap = new HashMap<>();
			for (final ExportColumnHeader columnHeader : exportColumnHeaders) {
				dataMap.put(columnHeader.getId(), new ExportColumnValue(columnHeader.getId(),
						this.getInventoryDetailValueInfo(inventoryDetails, columnHeader.getId())));
			}
			exportColumnValues.add(dataMap);
		}
		return exportColumnValues;
	}

	protected String getInventoryDetailValueInfo(final InventoryDetails inventoryDetails, final int columnHeaderId) {
		String val = "";
		if (columnHeaderId == TermId.ENTRY_NO.getId()) {
			val = inventoryDetails.getEntryId().toString();
		} else if (columnHeaderId == TermId.DESIG.getId()) {
			val = inventoryDetails.getGermplasmName();
		} else if (columnHeaderId == TermId.CROSS.getId()) {
			val = inventoryDetails.getParentage();
		} else if (columnHeaderId == TermId.GID.getId()) {
			val = inventoryDetails.getGid() != null ? inventoryDetails.getGid().toString() : "";
		} else if (columnHeaderId == TermId.SOURCE.getId()) {
			val = inventoryDetails.getSource();
		} else if (columnHeaderId == TermId.DUPLICATE.getId()) {
			val = this.getInventoryValue(inventoryDetails.getDuplicate());
		} else if (columnHeaderId == TermId.BULK_WITH.getId()) {
			val = this.getInventoryValue(inventoryDetails.getBulkWith());
		} else if (columnHeaderId == TermId.BULK_COMPL.getId()) {
			val = this.getInventoryValue(inventoryDetails.getBulkCompl());
		} else if (columnHeaderId == TermId.LOCATION_ID.getId()) {
			// in preparation for BMS-143. Export the abbreviation instead of the whole name
			val = inventoryDetails.getLocationAbbr();
		} else if (columnHeaderId == TermId.SEED_AMOUNT_G.getId()) {
			val = this.getInventoryAmount(inventoryDetails);
		} else if (columnHeaderId == TermId.STOCKID.getId()) {
			val = this.getInventoryValue(inventoryDetails.getInventoryID());
		} else if (columnHeaderId == AppConstants.TEMPORARY_INVENTORY_COMMENT.getInt()) {
			val = this.getInventoryValue(inventoryDetails.getComment());
		}
		return val;
	}

	protected String getInventoryValue(final String inventoryValue) {
		return inventoryValue != null ? inventoryValue : "";
	}

	protected String getInventoryAmount(final InventoryDetails inventoryDetails) {
		return inventoryDetails.getAmount() != null ? inventoryDetails.getAmount().toString() : "";
	}

	public void setFieldbookProperties(final FieldbookProperties fieldbookProperties) {
		this.fieldbookProperties = fieldbookProperties;
	}

	public void setInventoryMiddlewareService(final InventoryService inventoryMiddlewareService) {
		this.inventoryMiddlewareService = inventoryMiddlewareService;
	}

	public void setMessageSource(final MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setFieldbookMiddlewareService(final FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

}
