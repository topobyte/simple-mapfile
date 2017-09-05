// Copyright 2017 Sebastian Kuerten
//
// This file is part of simple-mapfile.
//
// simple-mapfile is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// simple-mapfile is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with simple-mapfile. If not, see <http://www.gnu.org/licenses/>.

package de.topobyte.simplemapfile.tools;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.OutputStreamOutStream;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTWriter;

import de.topobyte.simplemapfile.core.EntityFile;
import de.topobyte.simplemapfile.xml.FileReader;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class SmxExportGeometry
{

	private static final String HELP_MESSAGE = "SmxExportGeometry [args] -input INPUT -output OUTPUT";

	private static final String OPTION_INPUT = "input";
	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_FORMAT = "format";

	final static Logger logger = LoggerFactory
			.getLogger(SmxExportGeometry.class);

	private static enum FileFormat {
		WKB,
		WKT
	}

	public static void main(String[] args)
	{
		Options options = new Options();
		OptionHelper.addL(options, OPTION_INPUT, true, true, "file",
				"a smx input file");
		OptionHelper.addL(options, OPTION_OUTPUT, true, true, "file",
				"the output file");
		OptionHelper.addL(options, OPTION_FORMAT, true, false,
				"output format (wkb, wkt)");

		CommandLine commandLine = null;
		try {
			commandLine = new DefaultParser().parse(options, args);
		} catch (ParseException e) {
			System.out
					.println("unable to parse command line: " + e.getMessage());
			new HelpFormatter().printHelp(HELP_MESSAGE, options);
			System.exit(1);
		}
		if (commandLine == null) {
			return;
		}

		String argInput = commandLine.getOptionValue(OPTION_INPUT);
		String argOutput = commandLine.getOptionValue(OPTION_OUTPUT);

		FileFormat fileFormat = null;
		if (commandLine.hasOption(OPTION_FORMAT)) {
			String value = commandLine.getOptionValue(OPTION_FORMAT);
			if (value.equals("wkb")) {
				fileFormat = FileFormat.WKB;
			} else if (value.equals("wkt")) {
				fileFormat = FileFormat.WKT;
			} else {
				System.out.println("format not supported");
				System.exit(1);
			}
		}

		SmxExportGeometry exporter = new SmxExportGeometry();
		exporter.prepare(argInput, argOutput, fileFormat);
		exporter.execute();
	}

	private String argInput;
	private String argOutput;
	private FileFormat fileFormat;

	private void prepare(String argInput, String argOutput,
			FileFormat fileFormat)
	{
		this.argInput = argInput;
		this.argOutput = argOutput;
		this.fileFormat = fileFormat;
	}

	private void execute()
	{
		logger.info("loading selection boundary");
		Geometry boundary = null;
		try {
			EntityFile entity = FileReader.read(argInput);
			boundary = entity.getGeometry();
		} catch (IOException e) {
			logger.error(
					"unable to read geometry, IOException: " + e.getMessage());
			System.exit(1);
		} catch (ParserConfigurationException e) {
			logger.error(
					"unable to read geometry, ParserConfigurationException: "
							+ e.getMessage());
			System.exit(1);
		} catch (SAXException e) {
			logger.error(
					"unable to read geometry, SAXException: " + e.getMessage());
			System.exit(1);
		}

		try {
			if (fileFormat == FileFormat.WKB) {
				WKBWriter wkbWriter = new WKBWriter();
				FileOutputStream stream = new FileOutputStream(argOutput);
				wkbWriter.write(boundary, new OutputStreamOutStream(stream));
				stream.close();
			} else if (fileFormat == FileFormat.WKT) {
				WKTWriter wktWriter = new WKTWriter();
				FileWriter fileWriter = new FileWriter(argOutput);
				wktWriter.write(boundary, fileWriter);
				fileWriter.close();
			}
		} catch (IOException e) {
			logger.error("unable to serialize, IOException: " + e.getMessage());
			System.exit(1);
		}
	}

}
