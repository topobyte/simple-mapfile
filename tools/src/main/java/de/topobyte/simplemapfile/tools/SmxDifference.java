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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import de.topobyte.simplemapfile.core.EntityFile;
import de.topobyte.simplemapfile.xml.SmxFileReader;
import de.topobyte.simplemapfile.xml.SmxFileWriter;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class SmxDifference
{

	final static Logger logger = LoggerFactory.getLogger(SmxDifference.class);

	private static final String HELP_MESSAGE = "SmxDifference [args]";

	private static final String OPTION_INPUT = "input";
	private static final String OPTION_OUTPUT = "output";

	/**
	 * Filter that performs the union operation
	 */
	public static void main(String args[])
	{
		// @formatter:off
		Options options = new Options();
		OptionHelper.addL(options, OPTION_INPUT, true, true, "file", "a smx input file");
		OptionHelper.addL(options, OPTION_OUTPUT, true, false, "file", "a smx output file");
		// @formatter:on

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

		String[] list = commandLine.getArgs();
		if (list.length < 1) {
			new HelpFormatter().printHelp(HELP_MESSAGE, options);
			System.exit(1);
		}

		// read input file
		EntityFile entityFile = null;
		try {
			entityFile = SmxFileReader.read(argInput);
		} catch (IOException e) {
			logger.error("unable to load entity: " + argInput, e);
		} catch (ParserConfigurationException e) {
			logger.error("unable to load entity: " + argInput, e);
		} catch (SAXException e) {
			logger.error("unable to load entity: " + argInput, e);
		}

		if (entityFile == null) {
			// unable to proceed here
			logger.error("no input available");
			System.exit(1);
		}

		// read input files
		List<EntityFile> entityFiles = new ArrayList<>();

		for (String filename : list) {
			try {
				EntityFile entity = SmxFileReader.read(filename);
				entityFiles.add(entity);
			} catch (IOException e) {
				logger.error("unable to load entity: " + filename, e);
			} catch (ParserConfigurationException e) {
				logger.error("unable to load entity: " + filename, e);
			} catch (SAXException e) {
				logger.error("unable to load entity: " + filename, e);
			}
		}

		if (entityFiles.size() == 0) {
			// unable to proceed here
			logger.error("no input available");
			System.exit(1);
		}

		// create difference
		List<Geometry> regions = new ArrayList<>();
		for (EntityFile entity : entityFiles) {
			regions.add(entity.getGeometry());
		}

		Geometry result = entityFile.getGeometry();
		for (Geometry region : regions) {
			result = result.difference(region);
		}

		// create entity file
		EntityFile outputFile = new EntityFile();

		// TODO: what tags should be added to the output?
		// outputFile.addTag(tag.getKey(), tag.getValue());

		// set geometry
		outputFile.setGeometry(result);

		// write output file
		if (argOutput == null) {
			try {
				SmxFileWriter.write(outputFile, System.out);
			} catch (TransformerException e) {
				logger.error("unable to store entity: " + argOutput, e);
			} catch (ParserConfigurationException e) {
				logger.error("unable to store entity: " + argOutput, e);
			}
		} else {
			try {
				SmxFileWriter.write(outputFile, argOutput);
			} catch (IOException e) {
				logger.debug("unable to store entity: " + argOutput, e);
			} catch (TransformerException e) {
				logger.error("unable to store entity: " + argOutput, e);
			} catch (ParserConfigurationException e) {
				logger.error("unable to store entity: " + argOutput, e);
			}
		}
	}

}
