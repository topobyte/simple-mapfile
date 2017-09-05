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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import de.topobyte.adt.geo.BBox;
import de.topobyte.simplemapfile.core.EntityFile;
import de.topobyte.simplemapfile.index.SmxIndex;
import de.topobyte.simplemapfile.index.SmxIndexEntry;
import de.topobyte.simplemapfile.xml.FileReader;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class SmxCreateIndex
{

	final static Logger logger = LoggerFactory.getLogger(SmxCreateIndex.class);

	public static final String DEFAULT_INDEX_FILENAME = "smx.index";

	private static final String HELP_MESSAGE = "SmxCreateIndex [args]";

	private static final String OPTION_INPUT = "input";
	private static final String OPTION_OUTPUT = "output";

	/**
	 * Set the geometry of a smx file from a geometry file.
	 */
	public static void main(String args[])
	{
		Options options = new Options();
		OptionHelper.addL(options, OPTION_OUTPUT, true, false, "file",
				"an index output file");
		OptionHelper.addL(options, OPTION_INPUT, true, true, "directory",
				"an input directory with smx files");

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

		/*
		 * set up paths
		 */

		File dirInput = new File(argInput);
		if (!dirInput.exists()) {
			fail("input directory does not exist");
		}
		if (!dirInput.isDirectory()) {
			fail("input directory is not actually a directory");
		}

		File output = null;
		if (argOutput == null) {
			output = new File(dirInput, DEFAULT_INDEX_FILENAME);
		} else {
			output = new File(argOutput);
		}

		/*
		 * start working
		 */

		SmxIndex index = new SmxIndex();

		File[] files = dirInput.listFiles();
		for (File file : files) {
			String name = file.getName();
			EntityFile smx;
			try {
				smx = FileReader.read(file);
			} catch (Exception e) {
				System.err.println("unable to read file: " + name);
				continue;
			}
			Geometry geometry = smx.getGeometry();
			Envelope envelope = geometry.getEnvelopeInternal();
			index.add(new SmxIndexEntry(new BBox(envelope), name));
		}

		try {
			FileOutputStream fos = new FileOutputStream(output);
			DataOutputStream dos = new DataOutputStream(fos);
			index.write(dos);
			dos.close();
		} catch (FileNotFoundException e) {
			fail("output file not found");
		} catch (IOException e) {
			fail("IO error while writing index: " + e.getMessage());
		}
	}

	private static void fail(String message)
	{
		System.out.println(message);
		System.exit(1);
	}

}
