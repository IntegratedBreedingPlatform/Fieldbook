
package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.csvreader.CsvWriter;
import com.efficio.fieldbook.web.common.service.KsuCsvExportStudyService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.efficio.fieldbook.web.util.KsuFieldbookUtil;
import com.efficio.fieldbook.web.util.ZipUtil;

@Service
@Transactional
public class KsuCsvExportStudyServiceImpl implements KsuCsvExportStudyService {

	private static final Logger LOG = LoggerFactory.getLogger(KsuCsvExportStudyServiceImpl.class);

	@Resource
	private FieldbookProperties fieldbookProperties;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private OntologyService ontologyService;

	@Override
	public String export(Workbook workbook, String filename, List<Integer> instances) {

		List<String> filenameList = new ArrayList<String>();

		int fileExtensionIndex = filename.lastIndexOf(".");
		String studyName = filename.substring(0, fileExtensionIndex);

		CsvWriter csvWriter = null;
		int fileCount = instances.size();
		for (Integer index : instances) {
			try {
				String filenamePath =
						this.fieldbookProperties.getUploadDirectory() + File.separator + studyName
								+ (fileCount > 1 ? "-" + String.valueOf(index) : "") + filename.substring(fileExtensionIndex);
				List<Integer> indexes = new ArrayList<Integer>();
				indexes.add(index);
				List<MeasurementRow> observations =
						ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getExportArrangedObservations(), indexes);
				List<List<String>> dataTable =
						KsuFieldbookUtil.convertWorkbookData(observations, workbook.getMeasurementDatasetVariables(),workbook.getStudyDetails().getStudyName());

				csvWriter = new CsvWriter(new FileWriter(filenamePath, false), ',');
				for (List<String> row : dataTable) {
					for (String cell : row) {
						csvWriter.write(cell);
					}
					csvWriter.endRecord();
				}
				filenameList.add(filenamePath);

			} catch (IOException e) {
				KsuCsvExportStudyServiceImpl.LOG.error("ERROR in KSU CSV Export Study", e);

			} finally {
				if (csvWriter != null) {
					csvWriter.close();
				}
			}

		}

		String traitFilenamePath =
				this.fieldbookProperties.getUploadDirectory() + File.separator + studyName + "-Traits"
						+ AppConstants.EXPORT_KSU_TRAITS_SUFFIX.getString();
		KsuFieldbookUtil.writeTraits(workbook.getVariates(), traitFilenamePath, this.fieldbookMiddlewareService, this.ontologyService);
		filenameList.add(traitFilenamePath);

		String outputFilename =
				this.fieldbookProperties.getUploadDirectory() + File.separator
						+ filename.replaceAll(AppConstants.EXPORT_CSV_SUFFIX.getString(), "") + AppConstants.ZIP_FILE_SUFFIX.getString();
		ZipUtil.zipIt(outputFilename, filenameList);

		return outputFilename;
	}

}
