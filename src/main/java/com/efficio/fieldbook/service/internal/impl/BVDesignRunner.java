package com.efficio.fieldbook.service.internal.impl;

import au.com.bytecode.opencsv.CSVReader;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.service.internal.DesignRunner;
import com.efficio.fieldbook.service.internal.ProcessRunner;
import com.efficio.fieldbook.web.experimentdesign.ExperimentDesignGenerator;
import com.efficio.fieldbook.web.trial.bean.bvdesign.BVDesignOutput;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import org.generationcp.commons.pojo.ProcessTimeoutThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class BVDesignRunner implements DesignRunner {

	public static final String BV_PREFIX = "-bv";
	public static final String CSV_EXTENSION = ".csv";

	private static final Logger LOG = LoggerFactory.getLogger(BVDesignRunner.class);
	private static final String XML_EXTENSION = ".xml";

	private ProcessRunner processRunner = new BVDesignProcessRunner();
	private BVDesignOutputReader outputReader = new BVDesignOutputReader();
	private BVDesignXmlInputWriter inputWriter = new BVDesignXmlInputWriter();
	private static long bvDesignRunnerTimeout;

	@Override
	public BVDesignOutput runBVDesign(final WorkbenchService workbenchService, final FieldbookProperties fieldbookProperties,
			final MainDesign design) throws IOException {

		final String bvDesignPath = fieldbookProperties.getBvDesignPath();

		bvDesignRunnerTimeout = 60 * 1000 * Long.valueOf(fieldbookProperties.getBvDesignRunnerTimeout());

		int returnCode = -1;

		if (bvDesignPath != null && design != null && design.getDesign() != null) {

			final String xml = this.getXMLStringForDesign(design);

			final String filepath = this.inputWriter.write(xml, fieldbookProperties);

			returnCode = this.processRunner.run(bvDesignPath, "-i" + filepath);
		}

		final BVDesignOutput output = new BVDesignOutput(returnCode);

		if (returnCode == 0) {
			output.setResults(outputReader.read(design.getDesign().getParameterValue(ExperimentDesignGenerator.OUTPUTFILE_PARAM)));
		}

		return output;
	}

	public String getXMLStringForDesign(final MainDesign design) {
		String xml = "";
		final Long currentTimeMillis = System.currentTimeMillis();
		final String outputFilePath = currentTimeMillis + BVDesignRunner.BV_PREFIX + BVDesignRunner.CSV_EXTENSION;

		design.getDesign().setParameterValue(ExperimentDesignGenerator.OUTPUTFILE_PARAM, outputFilePath);
		design.getDesign().setParameterValue(ExperimentDesignGenerator.SEED_PARAM, this.getSeedValue(currentTimeMillis));

		try {
			xml = ExpDesignUtil.getXmlStringForSetting(design);
		} catch (final JAXBException e) {
			BVDesignRunner.LOG.error(e.getMessage(), e);
		}
		return xml;
	}

	private String getSeedValue(final Long currentTimeMillis) {
		String seedValue = Long.toString(currentTimeMillis);
		if (currentTimeMillis > Integer.MAX_VALUE) {
			seedValue = seedValue.substring(seedValue.length() - 9);
		}
		return seedValue;
	}

	private static String generateBVFilePath(final String extensionFilename, final FieldbookProperties fieldbookProperties) {
		final String filename = BVDesignRunner.generateBVFileName(extensionFilename);
		final String filenamePath = fieldbookProperties.getUploadDirectory() + File.separator + filename;
		final File f = new File(filenamePath);
		return f.getAbsolutePath();
	}

	private static String generateBVFileName(final String extensionFileName) {
		return System.currentTimeMillis() + BVDesignRunner.BV_PREFIX + extensionFileName;
	}

	public void setProcessRunner(final BVDesignProcessRunner processRunner) {
		this.processRunner = processRunner;
	}

	public void setOutputReader(final BVDesignOutputReader outputReader) {
		this.outputReader = outputReader;
	}

	public void setInputWriter(final BVDesignXmlInputWriter inputWriter) {
		this.inputWriter = inputWriter;
	}

	public class BVDesignProcessRunner implements ProcessRunner {

		@Override
		public Integer run(final String... command) throws IOException {

			final Integer returnCode = -1;

			final ProcessBuilder pb = new ProcessBuilder(command);
			final Process p = pb.start();
			// add a timeout for the design runner
			final ProcessTimeoutThread processTimeoutThread = new ProcessTimeoutThread(p, BVDesignRunner.bvDesignRunnerTimeout);
			processTimeoutThread.start();
			try {
				final InputStreamReader isr = new InputStreamReader(p.getInputStream());
				final BufferedReader br = new BufferedReader(isr);

				String lineRead;
				while ((lineRead = br.readLine()) != null) {
					BVDesignRunner.LOG.debug(lineRead);
				}

				return p.waitFor();
			} catch (final InterruptedException e) {
				BVDesignRunner.LOG.error(e.getMessage(), e);
			} finally {
				if (processTimeoutThread != null) {
					// Stop the thread if it's still running
					processTimeoutThread.interrupt();
				}
				if (p != null) {
					// missing these was causing the mass amounts of open 'files'
					p.getInputStream().close();
					p.getOutputStream().close();
					p.getErrorStream().close();
				}
			}

			return returnCode;

		}

		@Override
		public void setDirectory(final String directory) {
			// do nothing
		}
	}


	public class BVDesignOutputReader {

		public List<String[]> read(final String filePath) throws IOException {

			final File outputFile = new File(filePath);
			final FileReader fileReader = new FileReader(outputFile);
			final CSVReader reader = new CSVReader(fileReader);
			final List<String[]> myEntries = reader.readAll();

			fileReader.close();
			reader.close();
			outputFile.delete();

			return myEntries;

		}

	}


	public class BVDesignXmlInputWriter {

		public String write(final String xml, final FieldbookProperties fieldbookProperties) {

			String filenamePath = BVDesignRunner.generateBVFilePath(BVDesignRunner.XML_EXTENSION, fieldbookProperties);
			try {

				final File file = new File(filenamePath);
				final BufferedWriter output = new BufferedWriter(new FileWriter(file));
				output.write(xml);
				output.close();
				filenamePath = file.getAbsolutePath();
			} catch (final IOException e) {
				BVDesignRunner.LOG.error(e.getMessage(), e);
			}

			return filenamePath;
		}

	}

}
