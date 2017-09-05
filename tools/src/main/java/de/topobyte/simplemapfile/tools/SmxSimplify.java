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
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

import de.topobyte.simplemapfile.core.EntityFile;
import de.topobyte.simplemapfile.xml.SmxFileReader;
import de.topobyte.simplemapfile.xml.SmxFileWriter;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class SmxSimplify
{

	final static Logger logger = LoggerFactory.getLogger(SmxSimplify.class);

	private static final String HELP_MESSAGE = "SmxSimplify [args]";

	private static final String OPTION_INPUT = "input";
	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_TOLERANCE = "tolerance";

	/**
	 * Filter that performs the buffer operation
	 */
	public static void main(String args[])
	{
		// @formatter:off
		Options options = new Options();
		OptionHelper.addL(options, OPTION_INPUT, true, false, "file", "a smx input file");
		OptionHelper.addL(options, OPTION_OUTPUT, true, false, "file", "a smx output file");
		OptionHelper.addL(options, OPTION_TOLERANCE, true, true, "file", "the tolrance to use as a parameter for the simplification algorithm");
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
		String argTolerance = commandLine.getOptionValue(OPTION_TOLERANCE);

		// read input file
		EntityFile entityFile = null;

		if (argInput == null) {
			try {
				entityFile = SmxFileReader.read(System.in);
			} catch (SAXException e) {
				logger.error("unable to load entity", e);
			} catch (IOException e) {
				logger.error("unable to load entity", e);
			} catch (ParserConfigurationException e) {
				logger.error("unable to load entity", e);
			}
		} else {
			try {
				entityFile = SmxFileReader.read(argInput);
			} catch (IOException e) {
				logger.error("unable to load entity: " + argInput, e);
			} catch (ParserConfigurationException e) {
				logger.error("unable to load entity: " + argInput, e);
			} catch (SAXException e) {
				logger.error("unable to load entity: " + argInput, e);
			}
		}

		if (entityFile == null) {
			// unable to proceed here
			logger.error("no input available");
			System.exit(1);
		}

		// parse distance
		double tolerance = 0.0;
		try {
			tolerance = Double.parseDouble(argTolerance);
		} catch (NumberFormatException e) {
			logger.debug("unable to parse tolerance: " + argTolerance);
		}

		// create simplified version
		Geometry geometry = entityFile.getGeometry();
		TopologyPreservingSimplifier simplifier = new TopologyPreservingSimplifier(
				geometry);
		simplifier.setDistanceTolerance(tolerance);
		Geometry result = simplifier.getResultGeometry();

		// create entity file
		EntityFile outputFile = new EntityFile();

		// set tags
		Map<String, String> tags = entityFile.getTags();
		for (Entry<String, String> tag : tags.entrySet()) {
			outputFile.addTag(tag.getKey(), tag.getValue());
		}

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
