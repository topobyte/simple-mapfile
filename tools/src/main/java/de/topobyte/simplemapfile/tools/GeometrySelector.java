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

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.TopologyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import de.topobyte.melon.paths.PathUtil;
import de.topobyte.simplemapfile.core.EntityFile;
import de.topobyte.simplemapfile.index.SmxIndex;
import de.topobyte.simplemapfile.index.SmxIndexEntry;
import de.topobyte.simplemapfile.utils.PolygonLoader;
import de.topobyte.simplemapfile.xml.SmxFileReader;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

/**
 * This tool selects from a set of files those geometries that are being covered
 * by a denoted boundary b to a certain degree and copies those matching
 * geometry files to an output directory.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class GeometrySelector
{

	static final Logger logger = LoggerFactory
			.getLogger(GeometrySelector.class);

	private static final double DEFAULT_THRESHOLD = 0.9;

	private static final String HELP_MESSAGE = "GeometrySelector [args] <files...>";

	private static final String OPTION_BOUNDARY = "boundary";
	private static final String OPTION_THRESHOLD = "threshold";
	private static final String OPTION_OUTPUT = "output";

	/**
	 * @param args
	 *            the program arguments
	 */
	public static void main(String[] args) throws IOException
	{
		Options options = new Options();
		OptionHelper.addL(options, OPTION_BOUNDARY, true, true,
				"a boundary to use for selection of files");
		OptionHelper.addL(options, OPTION_THRESHOLD, true, false,
				"a threshold to use in coverage predicate "
						+ "(this value is the relative coverage necessary "
						+ "to include a given geometry)");
		OptionHelper.addL(options, OPTION_OUTPUT, true, true,
				"a directory to copy the selected files to");

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

		String[] inputFiles = line.getArgs();
		if (inputFiles.length == 0) {
			new HelpFormatter().printHelp(HELP_MESSAGE, options);
			System.exit(1);
		}

		String boundaryFile = line.getOptionValue(OPTION_BOUNDARY);
		String outputDirectoryPath = line.getOptionValue(OPTION_OUTPUT);

		Path outputDirectory = Paths.get(outputDirectoryPath);
		if (!Files.exists(outputDirectory)) {
			Files.createDirectories(outputDirectory);
		}
		if (!Files.exists(outputDirectory) || !Files.isWritable(outputDirectory)
				|| !Files.isDirectory(outputDirectory)) {
			System.out.println("unable to create or write to output directory");
			System.exit(1);
		}

		double threshold = DEFAULT_THRESHOLD;
		if (line.hasOption(OPTION_THRESHOLD)) {
			String thresholdArg = line.getOptionValue(OPTION_THRESHOLD);
			try {
				threshold = Double.parseDouble(thresholdArg);
			} catch (NumberFormatException e) {
				System.out.println("unable to parse threshold");
				System.exit(1);
			}
		}

		/*
		 * read boundary geometry
		 */

		logger.info("loading selection boundary");
		Geometry boundary = null;
		try {
			EntityFile entity = SmxFileReader.read(boundaryFile);
			boundary = entity.getGeometry();
		} catch (IOException e) {
			logger.warn("unable to read geometry as smx, IOException: "
					+ boundaryFile);
		} catch (ParserConfigurationException e) {
			logger.warn(
					"unable to read geometry as smx, ParserConfigurationException: "
							+ boundaryFile);
		} catch (SAXException e) {
			logger.warn("unable to read geometry as smx, SAXException: "
					+ boundaryFile);
		}
		if (boundary == null) {
			try {
				boundary = PolygonLoader.readPolygon(boundaryFile);
			} catch (IOException e) {
				logger.warn(
						"unable to read geometry as geometry file, IOException: "
								+ boundaryFile);
			}
		}
		if (boundary == null) {
			logger.error("unable to load boundary");
			System.exit(1);
		}

		/*
		 * iterate files
		 */

		Envelope box = boundary.getEnvelopeInternal();

		logger.info("iterating files");
		for (String filename : inputFiles) {
			Path file = Paths.get(filename);
			if (Files.isRegularFile(file)) {
				handle(boundary, threshold, file, outputDirectory);
			} else if (Files.isDirectory(file)) {
				Path index = file
						.resolve(SmxCreateIndex.DEFAULT_INDEX_FILENAME);
				boolean indexWorked = false;

				if (Files.exists(index)) {
					try {
						InputStream fis = Files.newInputStream(index);
						DataInputStream dis = new DataInputStream(fis);
						SmxIndex smxIndex = SmxIndex.read(dis);
						indexWorked = true;

						for (SmxIndexEntry entry : smxIndex.getEntries()) {
							Envelope entryBox = entry.getBBox().toEnvelope();
							if (!entryBox.intersects(box)) {
								continue;
							}
							Path child = file.resolve(entry.getName());
							handle(boundary, threshold, child, outputDirectory);
						}
					} catch (FileNotFoundException e) {
						System.err.println("index not found: " + index);
					} catch (IOException e) {
						System.err.println("IO error with index: '" + index
								+ "', message is: " + e.getMessage());
					} catch (ClassNotFoundException e) {
						System.err.println("Class not found error"
								+ " with index: '" + index + ", message is: "
								+ e.getMessage());
					}
				}

				if (!indexWorked) {
					// no index, just iterate files
					for (Path child : PathUtil.list(file)) {
						handle(boundary, threshold, child, outputDirectory);
					}
				}
			}
		}
	}

	private static void handle(Geometry boundary, double threshold, Path file,
			Path outputDirectory)
	{
		Path name = file.getFileName();

		boolean take = take(boundary, file, threshold);

		if (!take) {
			return;
		}

		Path outFile = outputDirectory.resolve(name);
		try {
			logger.info("copying " + name + " to " + outFile);
			Files.copy(file, outFile, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logger.debug("unable to copy from " + name + " to " + outFile);
		}
	}

	private static boolean take(Geometry boundary, Path file, double threshold)
	{
		try {
			EntityFile entity = SmxFileReader.read(file);
			Geometry geometry = entity.getGeometry();
			logger.debug(geometry.getGeometryType());
			double area = geometry.getArea();
			logger.debug("area: " + area);
			if (!geometry.intersects(boundary)) {
				return false;
			}
			Geometry intersection = boundary.intersection(geometry);
			double iarea = intersection.getArea();
			logger.debug("intersection area: " + iarea);
			double relative = iarea / area;
			logger.debug("relative: " + relative);
			if (relative > threshold) {
				return true;
			}

		} catch (IOException e) {
			logger.info("unable to read geometry: " + file);
		} catch (TopologyException e) {
			logger.info("TopologyException: " + e.getMessage());
		} catch (ParserConfigurationException e) {
			logger.info("ParserConfigurationException: " + e.getMessage());
		} catch (SAXException e) {
			logger.info("SAXException: " + e.getMessage());
		}
		return false;
	}

}
