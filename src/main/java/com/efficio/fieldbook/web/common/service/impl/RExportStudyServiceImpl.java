/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.csvreader.CsvWriter;
import com.efficio.fieldbook.web.common.service.RExportStudyService;
import com.efficio.fieldbook.web.nursery.bean.CSVOziel;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;

@Service
public class RExportStudyServiceImpl implements RExportStudyService {

	private static final Logger LOG = LoggerFactory.getLogger(RExportStudyServiceImpl.class);

	@Resource
	private FieldbookProperties fieldbookProperties;

	@Resource
	private OntologyService ontologyService;

	@Override
	public String export(Workbook workbook, String outputFile, List<Integer> instances) {
		return this.exportToR(workbook, outputFile, null, instances);
	}

	@Override
	public String exportToR(Workbook workbook, String outputFile, Integer selectedTrait, List<Integer> instances) {
		String outFile = this.fieldbookProperties.getUploadDirectory() + File.separator + outputFile;
		List<MeasurementRow> observations =
				ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getExportArrangedObservations(), instances);
		List<MeasurementRow> trialObservations =
				ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getTrialObservations(), instances);
		CSVOziel csv = new CSVOziel(workbook, observations, trialObservations);
		CsvWriter csvOutput = null;
		try {
			csvOutput = new CsvWriter(new FileWriter(outFile, false), ',');
			csvOutput.write("LOC");
			csvOutput.write("REP");
			csvOutput.write("BLK");
			csvOutput.write("ENTRY");
			csvOutput.write("GY");
			csv.defineTraitToEvaluate(this.getLabel(workbook.getVariates(), selectedTrait));
			csv.setSelectedTrait(this.getMeasurementVariable(workbook.getVariates(), csv.getStringTraitToEvaluate()));
			csv.writeTraitsR(csvOutput);
			csvOutput.endRecord();
			csv.writeDATAR(csvOutput, this.ontologyService);

		} catch (IOException e) {
			RExportStudyServiceImpl.LOG.error("CSV export was not successful", e);

		} finally {
			if (csvOutput != null) {
				csvOutput.close();
			}
		}
		return outFile;
	}

	private String getLabel(List<MeasurementVariable> variables, Integer termId) {
		if (variables != null && termId != null) {
			for (MeasurementVariable variable : variables) {
				if (variable.getTermId() == termId) {
					return variable.getName();
				}
			}
		}
		return null;
	}

	private MeasurementVariable getMeasurementVariable(List<MeasurementVariable> variables, String label) {
		if (variables != null && label != null) {
			for (MeasurementVariable variable : variables) {
				if (variable.getName().equalsIgnoreCase(label)) {
					return variable;
				}
			}
		}
		return null;
	}
}
