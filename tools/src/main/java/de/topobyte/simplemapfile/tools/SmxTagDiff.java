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

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import de.topobyte.simplemapfile.core.EntityFile;
import de.topobyte.simplemapfile.xml.SmxFileReader;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class SmxTagDiff
{

	final static Logger logger = LoggerFactory.getLogger(SmxTagDiff.class);

	private static final String HELP_MESSAGE = "SmxTagDiff [args]";

	private static final String OPTION_INPUT1 = "input1";
	private static final String OPTION_INPUT2 = "input2";

	/**
	 * Print differences in tags of two files
	 */
	public static void main(String args[])
	{
		Options options = new Options();
		OptionHelper.addL(options, OPTION_INPUT1, true, true, "file",
				"a smx input file");
		OptionHelper.addL(options, OPTION_INPUT2, true, true, "file",
				"a smx input file");

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

		String argInput1 = commandLine.getOptionValue(OPTION_INPUT1);
		String argInput2 = commandLine.getOptionValue(OPTION_INPUT2);

		// read input file
		EntityFile entityFile1 = null;
		EntityFile entityFile2 = null;
		try {
			entityFile1 = SmxFileReader.read(argInput1);
		} catch (IOException e) {
			logger.debug("unable to load entity: " + argInput1);
		} catch (ParserConfigurationException e) {
			logger.debug("unable to load entity: " + argInput1);
		} catch (SAXException e) {
			logger.debug("unable to load entity: " + argInput1);
		}
		try {
			entityFile2 = SmxFileReader.read(argInput2);
		} catch (IOException e) {
			logger.debug("unable to load entity: " + argInput2);
		} catch (ParserConfigurationException e) {
			logger.debug("unable to load entity: " + argInput2);
		} catch (SAXException e) {
			logger.debug("unable to load entity: " + argInput2);
		}
		if (entityFile1 == null || entityFile2 == null) {
			return;
		}

		// examine files
		Map<String, String> tags1 = entityFile1.getTags();
		Map<String, String> tags2 = entityFile2.getTags();

		printOnlys(tags1, tags2, "1");
		printOnlys(tags2, tags1, "2");
		printDiffs(tags1, tags2);
	}

	private static void printOnlys(Map<String, String> tags1,
			Map<String, String> tags2, String direction)
	{
		for (String key : tags1.keySet()) {
			String val2 = tags2.get(key);
			if (val2 == null) {
				System.out.println("only in " + direction + ": " + key);
				continue;
			}
		}
	}

	private static void printDiffs(Map<String, String> tags1,
			Map<String, String> tags2)
	{
		for (String key : tags1.keySet()) {
			String val1 = tags1.get(key);
			String val2 = tags2.get(key);
			if (val2 == null) {
				continue;
			}
			if (!val1.equals(val2)) {
				System.out.println(
						String.format("values differ for key '%s': '%s' - '%s'",
								key, val1, val2));
			}
		}
	}

}
