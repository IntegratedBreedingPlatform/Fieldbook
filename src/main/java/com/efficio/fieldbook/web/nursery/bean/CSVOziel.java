/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.nursery.bean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class was copied from CIMMYT's Fieldbook Code.
 */
public class CSVOziel {

	private static final Logger LOG = LoggerFactory.getLogger(CSVOziel.class);

	private final Workbook workbook;
	private final List<MeasurementRow> observations;
	private final List<MeasurementVariable> headers;
	private final List<MeasurementVariable> variateHeaders;
	private final boolean isDataKapture;

	private String stringTraitToEvaluate = "GY";
	private MeasurementVariable selectedTrait;
	private final List<MeasurementRow> trialObservations;

	public CSVOziel(Workbook workbook, List<MeasurementRow> observations, List<MeasurementRow> trialObservations) {
		this(workbook, observations, trialObservations, false);
	}

	public CSVOziel(Workbook workbook, List<MeasurementRow> observations, List<MeasurementRow> trialObservations, boolean isDataKapture) {
		this.workbook = workbook;
		this.headers = workbook.getMeasurementDatasetVariables();
		this.observations = observations;
		this.variateHeaders = workbook.arrangeMeasurementVariables(workbook.getVariates());
		this.trialObservations = trialObservations;
		this.isDataKapture = isDataKapture;
	}

	public void writeColums(CsvWriter csvOutput, int columnas) {
		for (int i = 0; i < columnas; i++) {
			String cad = null;
			try {
				csvOutput.write(cad);
			} catch (IOException ex) {
				CSVOziel.LOG.error("writeColums was not successful", ex);
			}

		}
	}

	public void writeRows(CsvWriter csvOutput, int rows) {
		try {
			for (int j = 0; j < rows; j++) {
				this.writeColums(csvOutput, 129);
				csvOutput.endRecord();
			}
		} catch (IOException ex) {
			CSVOziel.LOG.error("writeRows was not successful", ex);
		}
	}

	public void writeTraitsFromObservations(CsvWriter csvOutput) {
		try {
			int tot = 0;

			for (MeasurementVariable variate : this.variateHeaders) {
				csvOutput.write(variate.getName());
				tot++;
			}

			if (!this.isDataKapture) {
				this.writeColums(csvOutput, 104 - tot);
				csvOutput.write("IBFB");
			}

		} catch (IOException ex) {
			CSVOziel.LOG.error("writeTraitsFromObservations was not successful", ex);
		}
	}

	public void writeTraitsR(CsvWriter csvOutput) {
		try {
			for (MeasurementVariable variate : this.variateHeaders) {
				String valor = variate.getName();
				if (!valor.equals(this.stringTraitToEvaluate)) {

					if (valor.isEmpty()) {
						valor = ".";
					}
					csvOutput.write(valor);
				}
			}

		} catch (IOException ex) {
			CSVOziel.LOG.error("writeTraitsR was not successful", ex);
		}
	}

	public void writeDATA(CsvWriter csvOutput, OntologyService ontologyService) {
		int tot = this.variateHeaders.size();

		try {

			Map<Long, String> map = new HashMap<Long, String>();
			for (MeasurementRow row : this.trialObservations) {
				map.put(row.getLocationId(),
						WorkbookUtil.getValueByIdInRow(row.getMeasurementVariables(), TermId.TRIAL_INSTANCE_FACTOR.getId(), row));
			}

			for (MeasurementRow row : this.observations) {
				csvOutput.write(this.getDisplayValue(map.get(row.getLocationId())));
				this.writeToCsvOutput(csvOutput, row);

				String plot = WorkbookUtil.getValueByIdInRow(this.headers, TermId.PLOT_NO.getId(), row);
				if (plot == null || "".equals(plot.trim())) {
					plot = WorkbookUtil.getValueByIdInRow(this.headers, TermId.PLOT_NNO.getId(), row);
				}
				csvOutput.write(plot);
				csvOutput.write(WorkbookUtil.getValueByIdInRow(this.headers, TermId.ENTRY_NO.getId(), row));
				this.writeColums(csvOutput, 2);
				csvOutput.write(WorkbookUtil.getValueByIdInRow(this.headers, TermId.DESIG.getId(), row));
				this.writeColums(csvOutput, 15);
				csvOutput.write(WorkbookUtil.getValueByIdInRow(this.headers, TermId.GID.getId(), row));
				this.writeColums(csvOutput, 2);

				String propertyName = "";
				try {
					propertyName = ontologyService.getProperty(TermId.BREEDING_METHOD_PROP.getId()).getName();
				} catch (MiddlewareQueryException e) {
					CSVOziel.LOG.error("Write data was not successful", e);
				}

				for (MeasurementVariable variate : this.variateHeaders) {
					String valor = variate.getName();
					if (!valor.equals(this.stringTraitToEvaluate)) {
						try {
							if (variate.getPossibleValues() != null && !variate.getPossibleValues().isEmpty()
									&& !variate.getProperty().equals(propertyName)) {
								csvOutput.write(ExportImportStudyUtil.getCategoricalCellValue(row.getMeasurementDataValue(valor),
										variate.getPossibleValues()));
							} else if (variate.getProperty().equals(propertyName)) {
								csvOutput.write(row.getMeasurementData(valor).getValue());
							} else {
								csvOutput.write(row.getMeasurementDataValue(valor));
							}
						} catch (NullPointerException ex) {
							String cad = ".";
							csvOutput.write(cad);
						}
					}

				}

				this.writeColums(csvOutput, 104 - tot);
				csvOutput.write("END");
				csvOutput.endRecord();
			}
		} catch (IOException ex) {
			CSVOziel.LOG.error("Write data was not successful", ex);
		}

	}

	public void writeDATAR(CsvWriter csvOutput, OntologyService ontologyService) {
		try {

			Map<Long, String> map = new HashMap<Long, String>();
			for (MeasurementRow row : this.trialObservations) {
				map.put(row.getLocationId(),
						WorkbookUtil.getValueByIdInRow(row.getMeasurementVariables(), TermId.TRIAL_INSTANCE_FACTOR.getId(), row));
			}

			String propertyName = "";
			try {
				propertyName = ontologyService.getProperty(TermId.BREEDING_METHOD_PROP.getId()).getName();
			} catch (MiddlewareQueryException e) {
				CSVOziel.LOG.error("Write property data name was not successful", e);
			}

			for (MeasurementRow mRow : this.observations) {
				csvOutput.write(this.getDisplayValue(map.get(mRow.getLocationId())));
				this.writeToCsvOutput(csvOutput, mRow);

				csvOutput.write(WorkbookUtil.getValueByIdInRow(this.headers, TermId.ENTRY_NO.getId(), mRow));
				try {
					if (this.selectedTrait != null) {
						if (this.selectedTrait.getProperty().equals(propertyName)) {
							String value = WorkbookUtil.getCodeValueByIdInRow(this.headers, this.selectedTrait.getTermId(), mRow);
							csvOutput.write(value);
						} else {
							String value = WorkbookUtil.getValueByIdInRow(this.headers, this.selectedTrait.getTermId(), mRow);
							if (this.selectedTrait != null && this.selectedTrait.getPossibleValues() != null
									&& !this.selectedTrait.getPossibleValues().isEmpty()) {
								csvOutput
										.write(ExportImportStudyUtil.getCategoricalCellValue(value, this.selectedTrait.getPossibleValues()));
							} else {
								csvOutput.write(value);
							}
						}
					}
				} catch (NullPointerException ex) {
					String cad = ".";

					csvOutput.write(cad);
				}

				for (MeasurementVariable variate : this.variateHeaders) {
					String valor = variate.getName();

					if (!valor.equals(this.stringTraitToEvaluate)) {
						try {
							if (variate.getPossibleValues() != null && !variate.getPossibleValues().isEmpty()
									&& !variate.getProperty().equals(propertyName)) {
								csvOutput.write(ExportImportStudyUtil.getCategoricalCellValue(
										mRow.getMeasurementDataValue(variate.getName()), variate.getPossibleValues()));
							} else if (variate.getProperty().equals(propertyName)) {
								csvOutput.write(mRow.getMeasurementData(variate.getName()).getValue());
							} else {
								csvOutput.write(mRow.getMeasurementDataValue(variate.getName()));
							}
						} catch (NullPointerException ex) {
							String cad = ".";
							csvOutput.write(cad);
						}
					}

				}

				csvOutput.endRecord();
			}
		} catch (IOException ex) {
			CSVOziel.LOG.error("Write data R was not successful", ex);
		}

	}

	private CsvWriter writeToCsvOutput(CsvWriter csvOutput, MeasurementRow mRow) throws IOException {
		if (this.workbook.isNursery()) {
			csvOutput.write("1");
			csvOutput.write("1");
		} else {
			csvOutput.write(WorkbookUtil.getValueByIdInRow(this.headers, TermId.REP_NO.getId(), mRow));
			csvOutput.write(WorkbookUtil.getValueByIdInRow(this.headers, TermId.BLOCK_NO.getId(), mRow));
		}

		return csvOutput;
	}

	public void readDATAnew(File file, OntologyService ontologyService, FieldbookService fieldbookMiddlewareService) {

		List<String> titulos = new ArrayList<String>();
		int add = 0;
		String before = "";
		String actual = "";

		try {
			CsvReader csvReader = new CsvReader(file.toString());
			csvReader.readHeaders();
			String[] headers = csvReader.getHeaders();

			for (int i = 26; i < headers.length - 1; i++) {
				String titulo = headers[i];
				if (!titulo.equals("")) {
					titulos.add(titulo);
				}
			}

			for (int i = 0; i < 23; i++) {
				csvReader.skipRecord();
			}

			int myrow = 0;
			while (csvReader.readRecord()) {

				String dataOfTraits = "";
				before = actual;
				String trial = csvReader.get("Trial");
				String plot = csvReader.get("Plot");
				String entry = csvReader.get("Entry");

				actual = entry;

				if (before.equals(entry)) {
					add++;
				} else {
					add = 0;
				}

				try {
					int trialNumber = 1;
					if (trial != null && NumberUtils.isNumber(trial)) {
						if (trial.indexOf(".") > -1) {
							trialNumber = Integer.parseInt(trial.substring(0, trial.indexOf(".")));
						} else {
							trialNumber = Integer.parseInt(trial);
						}
					}
					myrow = this.findRow(trialNumber, Integer.parseInt(plot));
				} catch (NumberFormatException ex) {
					return;
				}

				// get name of breeding method property and get all methods
				String propertyName = "";
				List<Method> methods = new ArrayList<Method>();
				try {
					methods = fieldbookMiddlewareService.getAllBreedingMethods(false);
					propertyName = ontologyService.getProperty(TermId.BREEDING_METHOD_PROP.getId()).getName();
				} catch (MiddlewareQueryException e) {
					CSVOziel.LOG.error("Read data of breeding method was not successful", e);
				}

				// create a map for methods
				HashMap<String, Method> methodMap = new HashMap<String, Method>();
				if (methods != null) {
					for (Method method : methods) {
						methodMap.put(method.getMcode(), method);
					}
				}

				for (int i = 0; i < titulos.size(); i++) {
					String head = titulos.get(i);
					int col = this.buscaCol(head);
					if (col >= 0) {
						String data = csvReader.get(head);
						this.setObservationData(head, myrow + add, data, propertyName, methodMap);
						dataOfTraits = dataOfTraits + " " + data;
					} else {
						col = this.buscaCol(head);
						String data = csvReader.get(head);
						this.setObservationData(head, myrow + add, data, propertyName, methodMap);
						dataOfTraits = dataOfTraits + " " + data;
					}
				}
			}
			csvReader.close();
		} catch (IOException e) {
			CSVOziel.LOG.error("Read data of breeding method was not successful", e);
		}
	}

	private int findRow(int trial, int plot) {
		int row = 0;

		String plotLabel = this.getLabel(TermId.PLOT_NO.getId());
		if (plotLabel == null) {
			plotLabel = this.getLabel(TermId.PLOT_NNO.getId());
		}

		if (this.observations != null) {
			boolean match = false;
			List<MeasurementVariable> variables = this.observations.get(0).getMeasurementVariables();
			for (MeasurementRow mRow : this.observations) {
				String plotValueStr = mRow.getMeasurementDataValue(plotLabel);
				String trialValueStr = WorkbookUtil.getValueByIdInRow(variables, TermId.TRIAL_INSTANCE_FACTOR.getId(), mRow);
				if (plotValueStr != null && NumberUtils.isNumber(plotValueStr)) {
					int plotValue = Integer.valueOf(plotValueStr);
					if (plotValue == plot) {
						match = true;
					}
				}
				if (match) {
					if (trialValueStr != null && NumberUtils.isNumber(trialValueStr)) {
						int trialValue = Integer.valueOf(trialValueStr);
						if (trialValue == trial) {
							return row;
						} else {
							match = false;
						}
					} else {
						return row;
					}
				}
				row++;
			}
		}

		return row;
	}

	public boolean isValid(File file) {
		boolean isvalid = false;
		try {
			CsvReader csvReader = new CsvReader(file.toString());
			csvReader.readHeaders();
			String[] headers = csvReader.getHeaders();

			if (headers[headers.length - 1].equals("IBFB")) {
				isvalid = true;
			} else {
				isvalid = false;
			}
		} catch (IOException ex) {
			CSVOziel.LOG.error("isValid was not successful", ex);
		}
		return isvalid;
	}

	private int buscaCol(String head) {
		int col = -1;

		int index = 0;
		for (MeasurementVariable mVar : this.headers) {
			if (mVar.getName().equalsIgnoreCase(head)) {
				return index;
			}
			index++;
		}

		return col;
	}

	public void defineTraitToEvaluate(String stringTraitToEval) {
		this.stringTraitToEvaluate = stringTraitToEval;
	}

	private void setObservationData(String label, int rowIndex, String value, String propertyName, HashMap<String, Method> methodMap) {
		if (rowIndex < this.observations.size()) {
			MeasurementRow row = this.observations.get(rowIndex);
			for (MeasurementData data : row.getDataList()) {
				if (data.getLabel().equals(label)) {
					if (data.getMeasurementVariable().getPossibleValues() != null
							&& !data.getMeasurementVariable().getPossibleValues().isEmpty()) {

						// if breeding method id, use value as it is already in id format, if bm_code or any variable with breeding method
						// property, get id using code
						if (data.getMeasurementVariable().getTermId() == TermId.BREEDING_METHOD_VARIATE.getId()) {
							data.setValue(value);
						} else if (data.getMeasurementVariable().getTermId() == TermId.BREEDING_METHOD_VARIATE_CODE.getId()
								|| data.getMeasurementVariable().getProperty().equals(propertyName)) {
							String newValue = methodMap.get(value) == null ? "" : String.valueOf(methodMap.get(value).getMid());
							data.setValue(newValue);
						} else {
							data.setValue(ExportImportStudyUtil.getCategoricalIdCellValue(value, data.getMeasurementVariable()
									.getPossibleValues()));
						}

						if (data.getMeasurementVariable().getProperty().equals(propertyName) && !data.getValue().isEmpty()
								&& !NumberUtils.isNumber(data.getValue())) {
							data.setValue("");
						}

						if (data != null && data.getValue() != null && "".equals(data.getValue())) {
							data.setcValueId(null);
							data.setValue(null);
						} else if (data.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()) {
							data.setcValueId(data.getValue());
							data.setValue(data.getValue());
						}
					} else {
						if (!"N".equalsIgnoreCase(data.getDataType()) || "N".equalsIgnoreCase(data.getDataType()) && value != null
								&& NumberUtils.isNumber(value)) {

							data.setValue(value);
						} else {
							data.setValue(null);
						}
					}
					break;
				}
			}
		}
	}

	private String getLabel(int termId) {
		for (MeasurementVariable mVar : this.headers) {
			if (mVar.getTermId() == termId) {
				return mVar.getName();
			}
		}
		return null;
	}

	public void setSelectedTrait(MeasurementVariable selectedTrait) {
		this.selectedTrait = selectedTrait;
	}

	private String getDisplayValue(String value) {
		return value != null ? value : "";
	}

	public String getStringTraitToEvaluate() {
		return this.stringTraitToEvaluate;
	}

	// Start copied from CSVFileManager (old Fb)
	public void writeDataDataKapture(CsvWriter csvOutput) {
		Map<Long, String> map = new HashMap<Long, String>();
		for (MeasurementRow row : this.trialObservations) {
			map.put(row.getLocationId(),
					WorkbookUtil.getValueByIdInRow(row.getMeasurementVariables(), TermId.TRIAL_INSTANCE_FACTOR.getId(), row));
		}

		/**
		 * Type
		 */
		String strStudyType = this.workbook.getStudyDetails().getStudyType().toString();
		String trialNumber = "";
		String strLocationName = "";
		String strCycle = "";

		/**
		 * TrialNumber, Location Name, Cycle
		 */
		for (MeasurementVariable condition : this.workbook.getStudyConditions()) {
			if ("TID".equalsIgnoreCase(condition.getName())) {
				trialNumber = condition.getValue();
			} else if ("LID".equalsIgnoreCase(condition.getName())) {
				strLocationName = condition.getValue();
			} else if ("Cycle".equalsIgnoreCase(condition.getName())) {
				strCycle = condition.getValue();
			}
		}

		try {
			for (MeasurementRow row : this.observations) {

				/**
				 * Site
				 */
				csvOutput.write(strLocationName);
				/**
				 * Type
				 */
				csvOutput.write(strStudyType);
				/**
				 * Year (cycle)
				 */
				csvOutput.write(strCycle);
				/**
				 * TrialNumber
				 */
				if (trialNumber == null || "".equals(trialNumber)) {
					csvOutput.write(map.get(row.getLocationId()));
				} else {
					csvOutput.write(trialNumber);
				}

				/*
				 * El row y column es una manera de dividir el campo como un plano cartesiano. Esa manera solo aplica en experimentos de
				 * otros paises. Aqui no se maneja de la misma forma. Mientras no haya forma de realizarlo se imprime una constante.
				 */
				csvOutput.write("1"); // row
				csvOutput.write("1"); // column

				/**
				 * plotBarCode
				 */
				String plot = WorkbookUtil.getValueByIdInRow(this.headers, TermId.PLOT_NO.getId(), row);
				if (plot == null || "".equals(plot.trim())) {
					plot = WorkbookUtil.getValueByIdInRow(this.headers, TermId.PLOT_NNO.getId(), row);
				}
				csvOutput.write(plot);

				/**
				 * GID
				 */
				String gid = WorkbookUtil.getValueByIdInRow(this.headers, TermId.GID.getId(), row);
				csvOutput.write(gid);

				/**
				 * Genotype
				 */
				// es el Nombre, no necesariament se encuentra en las entradas del germoplasma
				csvOutput.write("");

				/**
				 * Pedigree
				 */
				String desig = WorkbookUtil.getValueByIdInRow(this.headers, TermId.DESIG.getId(), row);
				csvOutput.write(desig);

				/**
				 * Rep
				 */
				// Actualmente se guarda la ocurrencia, pero no necesariamente debe ser asi.
				// se acordo con Celso dejar ahi la occurencia.
				if (this.workbook.isNursery()) {
					csvOutput.write("1");
				} else {
					String rep = WorkbookUtil.getValueByIdInRow(this.headers, TermId.REP_NO.getId(), row);
					csvOutput.write(rep);
				}

				for (int z = 0; z < this.variateHeaders.size(); z++) {
					csvOutput.write("");
				}

				csvOutput.endRecord();
			}
		} catch (IOException ex) {
			CSVOziel.LOG.error("ERROR AL GENERAR DATA CSV " + ex, ex);
		}
	}

	public void writeTraitsDataKapture(CsvWriter csvOutput) {

		try {

			for (MeasurementVariable variate : this.variateHeaders) {
				String strTraitName = "";
				String strTrailValRule = "";
				String strDataType = "";

				strDataType = variate.getDataTypeDisplay();
				strTraitName = variate.getName();

				/**
				 * Trait Name
				 */
				csvOutput.write(strTraitName);

				/**
				 * Trait Value Rule
				 */
				if (variate.getMinRange() != null && variate.getMaxRange() != null) {
					strTrailValRule = variate.getMinRange().toString() + ".." + variate.getMaxRange().toString();
				}
				csvOutput.write(strTrailValRule);

				/**
				 * Data Type
				 */
				csvOutput.write(strDataType);

				/**
				 * Auto Progress Field Length
				 */
				csvOutput.write("1");
				/**
				 * Is Days Trait
				 */
				csvOutput.write("1");
				/**
				 * DateStamp
				 */
				csvOutput.write("1");
				/**
				 * Trait Units
				 */
				csvOutput.write("1");
				/**
				 * Connection
				 */
				csvOutput.write("0");

				csvOutput.endRecord();
			}
		} catch (IOException ex) {
			CSVOziel.LOG.error("Error al generar el archivo csv: " + ex, ex);
		}
	}

	public void readDATACapture(File file, OntologyService ontologyService, FieldbookService fieldbookMiddlewareService) {

		try {
			CsvReader csvReader = new CsvReader(file.toString());
			csvReader.readHeaders();
			
			while (csvReader.readRecord()) {

				// get name of breeding method property and get all methods
				String propertyName = "";
				List<Method> methods = new ArrayList<Method>();
				try {
					methods = fieldbookMiddlewareService.getAllBreedingMethods(false);
					propertyName = ontologyService.getProperty(TermId.BREEDING_METHOD_PROP.getId()).getName();
				} catch (MiddlewareQueryException e) {
					CSVOziel.LOG.error("Read data of breeding method was not successful", e);
				}

				// create a map for methods
				HashMap<String, Method> methodMap = new HashMap<String, Method>();
				if (methods != null) {
					for (Method method : methods) {
						methodMap.put(method.getMcode(), method);
					}
				}

				for (MeasurementVariable variate : this.variateHeaders) {
					String csvTrial = csvReader.get("TrialNumber");
					String csvPlot = csvReader.get("PlotBarCode");
					int trial = 1;
					if (csvTrial != null && NumberUtils.isNumber(csvTrial)) {
						trial = Integer.parseInt(csvTrial);
					}
					int plot = 1;
					if (csvPlot != null && NumberUtils.isNumber(csvPlot)) {
						plot = Integer.parseInt(csvPlot);
					}
					int rowNum = this.findRow(trial, plot);
					if (rowNum > -1) {
						String value = csvReader.get(variate.getName());
						this.setObservationData(variate.getName(), rowNum, value, propertyName, methodMap);
					}
				}

			}
			csvReader.close();

		} catch (FileNotFoundException ex) {
			CSVOziel.LOG.error("FILE NOT FOUND. readDATAcsv. " + ex);

		} catch (IOException e) {
			CSVOziel.LOG.error("IO EXCEPTION. readDATAcsv. " + e);
		}
	}
	// end copied from CSVFileManager (old Fb)
}
