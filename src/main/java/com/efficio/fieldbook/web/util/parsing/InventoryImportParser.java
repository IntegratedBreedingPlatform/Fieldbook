package com.efficio.fieldbook.web.util.parsing;

import com.efficio.fieldbook.util.parsing.AbstractExcelFileParser;
import com.efficio.fieldbook.util.parsing.WorkbookRowConverter;
import com.efficio.fieldbook.util.parsing.validation.NonEmptyValidator;
import com.efficio.fieldbook.util.parsing.validation.ParseValidationMap;
import com.efficio.fieldbook.util.parsing.validation.ValueRangeValidator;
import com.efficio.fieldbook.util.parsing.validation.ValueTypeValidator;
import com.efficio.fieldbook.web.common.exception.FileParsingException;
import com.efficio.fieldbook.web.nursery.bean.ImportedInventoryList;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */
public class InventoryImportParser extends AbstractExcelFileParser<ImportedInventoryList> {

	private static final Logger LOG = LoggerFactory.getLogger(InventoryImportParser.class);

	public static final String ALL_LOCATION_VALUES_REQUIRED = "inventory.import.parsing.validation.error.blank.location.value";
	public static final String INVALID_HEADERS = "common.parsing.invalid.headers";

	// aside from defining the expected value of the header labels, we are also defining here the order in which they appear
	enum InventoryHeaderLabels {
		ENTRY,
		DESIGNATION,
		PARENTAGE,
		GID,
		SOURCE,
		LOCATION,
		AMOUNT,
		SCALE,
		COMMENT
	}

	public static final int INVENTORY_SHEET = 0;
	public static String[] HEADER_LABEL_ARRAY = convertEnumToStringArray();

	public static int[] INVENTORY_SPECIFIC_COLUMNS =
			new int[] {
					InventoryHeaderLabels.LOCATION.ordinal(),
					InventoryHeaderLabels.AMOUNT.ordinal(),
					InventoryHeaderLabels.SCALE.ordinal()};

	private int currentParseIndex = 0;

	private ImportedInventoryList importedInventoryList;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private OntologyService ontologyService;

	private List<Location> locations;

	private List<Scale> scales;

	@Override public ImportedInventoryList parseWorkbook(Workbook workbook)
			throws FileParsingException {

		this.workbook = workbook;

		validateFileHeader();

		parseInventoryDetails();

		return importedInventoryList;
	}

	protected void validateFileHeader() throws FileParsingException {
		if (isHeaderInvalid(currentParseIndex++, INVENTORY_SHEET, HEADER_LABEL_ARRAY)) {
			throw new FileParsingException(INVALID_HEADERS);
		}
	}

	protected void parseInventoryDetails() throws FileParsingException {
		WorkbookRowConverter<InventoryDetails> inventoryDetailsConverter = new WorkbookRowConverter<InventoryDetails>(
				workbook, currentParseIndex, INVENTORY_SHEET, HEADER_LABEL_ARRAY.length, HEADER_LABEL_ARRAY) {
			@Override public InventoryDetails convertToObject(Map<Integer, String> rowValues) throws FileParsingException{

				// TODO: provide feature for mapping of columns to a different column order
				Integer gid = Integer.parseInt(rowValues.get(InventoryHeaderLabels.GID.ordinal()));
				Integer entryId = Integer.parseInt(rowValues.get(InventoryHeaderLabels.ENTRY.ordinal()));
				String name = rowValues.get(InventoryHeaderLabels.DESIGNATION.ordinal());
				String parentage = rowValues.get(InventoryHeaderLabels.PARENTAGE.ordinal());
				String source = rowValues.get(InventoryHeaderLabels.SOURCE.ordinal());
				String locationAbbr = rowValues.get(InventoryHeaderLabels.LOCATION.ordinal());
				String amountString = rowValues.get(InventoryHeaderLabels.AMOUNT.ordinal());
				String scale = rowValues.get(InventoryHeaderLabels.SCALE.ordinal());
				String comment = rowValues.get(InventoryHeaderLabels.COMMENT.ordinal());

				// perform some row-based validation
				// TODO: determine if row based validation is common occurrence, and if so model it as part of the hierarchy
				boolean inventorySpecificValuePresent = false;
				boolean inventorySpecificValueEmpty = false;

				for (int i : INVENTORY_SPECIFIC_COLUMNS) {
					if (rowValues.get(i).isEmpty()) {
						inventorySpecificValueEmpty = true;
					} else {
						inventorySpecificValuePresent = true;
					}
				}

				if (inventorySpecificValueEmpty && inventorySpecificValuePresent) {
					throw new FileParsingException(ALL_LOCATION_VALUES_REQUIRED, getCurrentIndex(), null, null);
				}


				InventoryDetails details = new InventoryDetails();
				details.setGid(gid);
				details.setEntryId(entryId);
				details.setGermplasmName(name);
				details.setParentage(parentage);
				details.setSource(source);

				if (!StringUtils.isEmpty(locationAbbr)) {
					Location location = findLocationForLocationName(locationAbbr);
					details.setLocationAbbr(locationAbbr);
					details.setLocationId(location.getLocid());
					details.setLocationName(location.getLname());
				}

				if (!StringUtils.isEmpty(scale)) {
					Scale scaleItem = findScaleForScaleName(scale);
					details.setScaleName(scale);
					details.setScaleId(scaleItem.getId());

				}

				details.setComment(StringUtils.isEmpty(comment) ? null : comment);
				details.setAmount(StringUtils.isEmpty(amountString) ? null : Double.parseDouble(amountString));

				return details;
			}
		};

		inventoryDetailsConverter.setValidationMap(setupIndividualColumnValidation());

		List<InventoryDetails> detailList = inventoryDetailsConverter.convertWorkbookRowsToObject(
				new WorkbookRowConverter.ContinueTillBlank());

		importedInventoryList = new ImportedInventoryList(detailList, this.originalFilename);
	}

	protected Location findLocationForLocationName(String locationName) {
		for (Location location : locations) {
			if (location.getLabbr().equalsIgnoreCase(locationName)) {
				return location;
			}
		}

		return null;
	}

	protected Scale findScaleForScaleName(String scaleName) {
		for (Scale scale : scales) {
			if (scale.getName().equalsIgnoreCase(scaleName)) {
				return scale;
			}
		}

		return null;
	}

	protected ParseValidationMap setupIndividualColumnValidation() {
		ParseValidationMap validationMap = new ParseValidationMap();

		// validation for ENTRY column
		validationMap.addValidation(InventoryHeaderLabels.ENTRY.ordinal(), new ValueTypeValidator(Integer.class));
		validationMap.addValidation(InventoryHeaderLabels.ENTRY.ordinal(), new NonEmptyValidator());

		validationMap.addValidation(InventoryHeaderLabels.LOCATION.ordinal(), new ValueRangeValidator(buildAllowedLocationsList()));
		validationMap.addValidation(InventoryHeaderLabels.AMOUNT.ordinal(), new ValueTypeValidator(Double.class));

		validationMap.addValidation(InventoryHeaderLabels.SCALE.ordinal(), new ValueRangeValidator(buildAllowedScaleList()));

		return validationMap;
	}

	protected List<String> buildAllowedLocationsList() {
		List<String> locationList = new ArrayList<>();

		try {
			locations = fieldbookMiddlewareService.getAllSeedLocations();

			if (locations != null) {
				for (Location location : locations) {
					locationList.add(location.getLabbr());
				}
			}
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}

		return locationList;
	}

	protected List<String> buildAllowedScaleList() {
		List<String> allowedScales = new ArrayList<>();

		try {
			scales = ontologyService.getAllInventoryScales();

			if (scales != null) {
				for (Scale scale : scales) {
					allowedScales.add(scale.getName());
				}
			}
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}

		return allowedScales;
	}


	// FIXME for extraction to a generic utility
	public static String[] convertEnumToStringArray() {
		InventoryHeaderLabels[] labels = InventoryHeaderLabels.values();
		String[] stringArray = new String[labels.length];


		for (InventoryHeaderLabels label : labels) {
			stringArray[label.ordinal()] = label.name();
		}

		return stringArray;
	}

}