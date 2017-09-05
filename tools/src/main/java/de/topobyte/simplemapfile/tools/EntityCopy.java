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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import de.topobyte.simplemapfile.core.EntityFile;
import de.topobyte.simplemapfile.xml.SmxFileReader;
import de.topobyte.simplemapfile.xml.SmxFileWriter;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class EntityCopy
{

	final static Logger logger = LoggerFactory.getLogger(EntityCopy.class);

	private static final String HELP_MESSAGE = "EntityCopy [args] input output";

	/**
	 * Read a smx-file and write it to a new file.
	 */
	public static void main(String args[])
	{
		Options options = new Options();

		CommandLine line = null;
		try {
			line = new DefaultParser().parse(options, args);
		} catch (ParseException e) {
			System.out
					.println("unable to parse command line: " + e.getMessage());
			new HelpFormatter().printHelp(HELP_MESSAGE, options);
			System.exit(1);
		}
		if (line == null) {
			return;
		}

		String[] list = line.getArgs();
		if (list.length != 2) {
			new HelpFormatter().printHelp(HELP_MESSAGE, options);
			System.exit(1);
		}

		List<String> filenames = new ArrayList<>();
		for (int i = 0; i < list.length; i++) {
			String filename = list[i];
			filenames.add(filename);
		}

		// read input file

		String filenameInput = filenames.get(0);
		EntityFile entityFile = null;
		try {
			entityFile = SmxFileReader.read(filenameInput);
		} catch (IOException e) {
			logger.debug("unable to load entity: " + filenameInput);
		} catch (ParserConfigurationException e) {
			logger.debug("unable to load entity: " + filenameInput);
		} catch (SAXException e) {
			logger.debug("unable to load entity: " + filenameInput);
		}
		if (entityFile == null) {
			return;
		}

		// write output file

		String filenameOutput = filenames.get(1);

		try {
			SmxFileWriter.write(entityFile, filenameOutput);
		} catch (IOException e) {
			logger.debug("unable to store entity: " + filenameInput);
		} catch (TransformerException e) {
			logger.debug("unable to store entity: " + filenameInput);
		} catch (ParserConfigurationException e) {
			logger.debug("unable to store entity: " + filenameInput);
		}
	}

}
