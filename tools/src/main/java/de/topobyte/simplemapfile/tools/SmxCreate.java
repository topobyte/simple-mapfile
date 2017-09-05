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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

import de.topobyte.simplemapfile.core.EntityFile;
import de.topobyte.simplemapfile.utils.PolygonLoader;
import de.topobyte.simplemapfile.xml.SmxFileWriter;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class SmxCreate
{

	final static Logger logger = LoggerFactory.getLogger(SmxCreate.class);

	private static final String HELP_MESSAGE = "SmxCreate [args]";

	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_GEOMETRY = "geometry";
	private static final String OPTION_TAGS = "tags";

	/**
	 * Set the geometry of a smx file from a geometry file.
	 */
	public static void main(String args[])
	{
		Options options = new Options();
		OptionHelper.addL(options, OPTION_OUTPUT, true, true, "file",
				"a smx output file");
		OptionHelper.addL(options, OPTION_GEOMETRY, true, true, "file",
				"the geometry file to set the geometry from");
		OptionHelper.addL(options, OPTION_TAGS, true, false, "string",
				"key1=value1;key2=value2;...keyN=valueN");

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

		String argOutput = commandLine.getOptionValue(OPTION_OUTPUT);
		String argGeometry = commandLine.getOptionValue(OPTION_GEOMETRY);
		String argTags = commandLine.getOptionValue(OPTION_TAGS);

		// create new entity file
		EntityFile entityFile = new EntityFile();

		// read geometry
		Geometry geometry = null;
		try {
			geometry = PolygonLoader.readPolygon(argGeometry);
		} catch (IOException e) {
			logger.debug("unable to load geometry: " + argGeometry);
		}
		if (geometry == null) {
			return;
		}

		// set geometry
		entityFile.setGeometry(geometry);

		if (argTags != null) {
			// parse tags
			String[] tags = argTags.split(";");
			for (String tag : tags) {
				String[] parts = tag.split("=");
				if (parts.length != 2) {
					logger.warn("ignoring: " + tag);
					continue;
				}
				entityFile.addTag(parts[0], parts[1]);
			}
		}

		// write output file
		try {
			SmxFileWriter.write(entityFile, argOutput);
		} catch (IOException e) {
			logger.debug("unable to store entity: " + argOutput);
		} catch (TransformerException e) {
			logger.debug("unable to store entity: " + argOutput);
		} catch (ParserConfigurationException e) {
			logger.debug("unable to store entity: " + argOutput);
		}
	}

}
