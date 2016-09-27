
package com.efficio.fieldbook.service.internal.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.efficio.fieldbook.web.experimentdesign.ExperimentDesignGenerator;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.service.internal.DesignRunner;
import com.efficio.fieldbook.web.trial.bean.BVDesignOutput;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;

public class BVDesignRunner implements DesignRunner {

	public static final String BV_PREFIX = "-bv";
	public static final String CSV_EXTENSION = ".csv";

	private static final Logger LOG = LoggerFactory.getLogger(BVDesignRunner.class);
	private static String XML_EXTENSION = ".xml";
	private static String BREEDING_VIEW_EXE = "BreedingView.exe";
	private static String BVDESIGN_EXE = "BVDesign.exe";

	@Override
	public BVDesignOutput runBVDesign(WorkbenchService workbenchService, FieldbookProperties fieldbookProperties, MainDesign design)
			throws IOException {

		String bvDesignLocation = BVDesignRunner.getBreedingViewExeLocation(workbenchService);
		int returnCode = -1;
		if (bvDesignLocation != null && design != null && design.getDesign() != null) {
			String xml = this.getXMLStringForDesign(design);

			String filepath = BVDesignRunner.writeToFile(xml, fieldbookProperties);

			ProcessBuilder pb = new ProcessBuilder(bvDesignLocation, "-i" + filepath);
			Process p = pb.start();
			try {
				InputStreamReader isr = new InputStreamReader(p.getInputStream());
				BufferedReader br = new BufferedReader(isr);

				String lineRead;
				while ((lineRead = br.readLine()) != null) {
					BVDesignRunner.LOG.debug(lineRead);
				}

				returnCode = p.waitFor();
				// add here the code to parse the csv file
			} catch (InterruptedException e) {
				BVDesignRunner.LOG.error(e.getMessage(), e);
			} finally {
				if (p != null) {
					// missing these was causing the mass amounts of open 'files'
					p.getInputStream().close();
					p.getOutputStream().close();
					p.getErrorStream().close();
				}
			}
		}
		BVDesignOutput output = new BVDesignOutput(returnCode);
		if (returnCode == 0) {

			File outputFile = new File(design.getDesign().getParameterValue(ExperimentDesignGenerator.OUTPUTFILE_PARAM));
			FileReader fileReader = new FileReader(outputFile);
			CSVReader reader = new CSVReader(fileReader);
			List<String[]> myEntries = reader.readAll();
			output.setResults(myEntries);
			fileReader.close();
			reader.close();
			outputFile.delete();

		}
		return output;
	}

	public String getXMLStringForDesign(MainDesign design) {
		String xml = "";
		Long currentTimeMillis = System.currentTimeMillis();
		String outputFilePath = currentTimeMillis + BVDesignRunner.BV_PREFIX + BVDesignRunner.CSV_EXTENSION;

		design.getDesign().setParameterValue(ExperimentDesignGenerator.OUTPUTFILE_PARAM, outputFilePath);
		design.getDesign().setParameterValue(ExperimentDesignGenerator.SEED_PARAM, this.getSeedValue(currentTimeMillis));

		try {
			xml = ExpDesignUtil.getXmlStringForSetting(design);
		} catch (JAXBException e) {
			BVDesignRunner.LOG.error(e.getMessage(), e);
		}
		return xml;
	}

	private String getSeedValue(Long currentTimeMillis) {
		String seedValue = Long.toString(currentTimeMillis);
		if (currentTimeMillis > Integer.MAX_VALUE) {
			seedValue = seedValue.substring(seedValue.length() - 9);
		}
		return seedValue;
	}

	private static String getBreedingViewExeLocation(WorkbenchService workbenchService) {
		String bvDesignLocation = null;
		Tool bvTool = null;
		try {
			bvTool = workbenchService.getToolWithName(AppConstants.TOOL_NAME_BREEDING_VIEW.getString());
		} catch (MiddlewareQueryException e) {
			BVDesignRunner.LOG.error(e.getMessage(), e);
		}
		if (bvTool != null) {
			// write xml to temp file
			File absoluteToolFile = new File(bvTool.getPath()).getAbsoluteFile();
			bvDesignLocation = absoluteToolFile.getAbsolutePath().replaceAll(BVDesignRunner.BREEDING_VIEW_EXE, BVDesignRunner.BVDESIGN_EXE);
		}
		return bvDesignLocation;
	}

	private static String writeToFile(String xml, FieldbookProperties fieldbookProperties) {
		String filenamePath = BVDesignRunner.generateBVFilePath(BVDesignRunner.XML_EXTENSION, fieldbookProperties);
		try {

			File file = new File(filenamePath);
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			output.write(xml);
			output.close();
			filenamePath = file.getAbsolutePath();
		} catch (IOException e) {
			BVDesignRunner.LOG.error(e.getMessage(), e);
		}
		return filenamePath;
	}

	private static String generateBVFilePath(String extensionFilename, FieldbookProperties fieldbookProperties) {
		String filename = BVDesignRunner.generateBVFileName(extensionFilename);
		String filenamePath = fieldbookProperties.getUploadDirectory() + File.separator + filename;
		File f = new File(filenamePath);
		return f.getAbsolutePath();
	}

	private static String generateBVFileName(String extensionFileName) {
		return System.currentTimeMillis() + BVDesignRunner.BV_PREFIX + extensionFileName;
	}

}
