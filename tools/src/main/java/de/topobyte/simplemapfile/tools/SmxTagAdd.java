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
import javax.xml.transform.TransformerException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import de.topobyte.simplemapfile.core.EntityFile;
import de.topobyte.simplemapfile.xml.FileReader;
import de.topobyte.simplemapfile.xml.FileWriter;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;
import de.topobyte.utilities.apache.commons.cli.parsing.ArgumentHelper;
import de.topobyte.utilities.apache.commons.cli.parsing.ArgumentParseException;
import de.topobyte.utilities.apache.commons.cli.parsing.BooleanOption;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class SmxTagAdd
{

	final static Logger logger = LoggerFactory.getLogger(SmxTagAdd.class);

	private static final String HELP_MESSAGE = "SmxTagAdd [args]";

	private static final String OPTION_INPUT = "input";
	private static final String OPTION_INPUT_ADD = "add";
	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_REPLACE = "replace";

	/**
	 * Add tags of one file to those of another.
	 */
	public static void main(String args[])
	{
		Options options = new Options();
		OptionHelper.addL(options, OPTION_INPUT, true, true, "file",
				"a smx input file");
		OptionHelper.addL(options, OPTION_INPUT_ADD, true, true, "file",
				"a smx input file");
		OptionHelper.addL(options, OPTION_OUTPUT, true, true, "file",
				"a smx output file");
		OptionHelper.addL(options, OPTION_REPLACE, true, false, "boolean",
				"whether to replace existing tags");

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
		String argInputAdd = commandLine.getOptionValue(OPTION_INPUT_ADD);
		String argOutput = commandLine.getOptionValue(OPTION_OUTPUT);

		boolean replace = false;
		BooleanOption replaceOption = null;
		try {
			replaceOption = ArgumentHelper.getBoolean(commandLine,
					OPTION_REPLACE);
			replace = replaceOption.getValue();
		} catch (ArgumentParseException e) {
			logger.error("unable to parse '" + OPTION_REPLACE + "' argument");
			System.exit(1);
		}

		// read input file
		EntityFile entityFile = null;
		EntityFile entityFileAdd = null;
		try {
			entityFile = FileReader.read(argInput);
		} catch (IOException e) {
			logger.debug("unable to load entity: " + argInput);
		} catch (ParserConfigurationException e) {
			logger.debug("unable to load entity: " + argInput);
		} catch (SAXException e) {
			logger.debug("unable to load entity: " + argInput);
		}
		try {
			entityFileAdd = FileReader.read(argInputAdd);
		} catch (IOException e) {
			logger.debug("unable to load entity: " + argInputAdd);
		} catch (ParserConfigurationException e) {
			logger.debug("unable to load entity: " + argInputAdd);
		} catch (SAXException e) {
			logger.debug("unable to load entity: " + argInputAdd);
		}
		if (entityFile == null || entityFileAdd == null) {
			return;
		}

		// work on files
		Map<String, String> tags = entityFile.getTags();
		Map<String, String> tagsAdd = entityFileAdd.getTags();

		for (String key : tagsAdd.keySet()) {
			String val = tags.get(key);
			String valAdd = tagsAdd.get(key);
			if (val == null) {
				entityFile.addTag(key, valAdd);
			} else {
				if (!val.equals(valAdd)) {
					if (replace) {
						entityFile.addTag(key, valAdd);
					}
				}
			}
		}

		try {
			FileWriter.write(entityFile, argOutput);
		} catch (IOException e) {
			logger.debug("unable to store entity: " + argOutput, e);
		} catch (TransformerException e) {
			logger.error("unable to store entity: " + argOutput, e);
		} catch (ParserConfigurationException e) {
			logger.error("unable to store entity: " + argOutput, e);
		}
	}

}
