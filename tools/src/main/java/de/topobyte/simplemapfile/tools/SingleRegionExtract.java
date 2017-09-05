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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.slimjars.dist.gnu.trove.map.hash.TLongObjectHashMap;
import com.vividsolutions.jts.geom.Geometry;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import de.topobyte.osm4j.geometry.GeometryBuilder;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.simplemapfile.core.EntityFile;
import de.topobyte.simplemapfile.xml.SmxFileWriter;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

/**
 * This class reads an osm file into memory. It then stores all nodes and ways
 * into lookup structures so that a geometry may be constructed from a relation.
 * It then attempts to build a geometry from the first relation found in the
 * file, creates an EntityFile from it, adds all tags the original relation has,
 * and stores it into the specified file.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class SingleRegionExtract
{

	private static final String HELP_MESSAGE = "SingleRegionExtract [args] -input INPUT -output OUTPUT";

	private static final String OPTION_INPUT = "input";
	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_ID = "id";

	public static void main(String[] args) throws IOException
	{
		Options options = new Options();
		OptionHelper.addL(options, OPTION_INPUT, true, true, "file",
				"an osm file to read entities from");
		OptionHelper.addL(options, OPTION_OUTPUT, true, true, "file",
				"the output file to store the object in");
		OptionHelper.addL(options, OPTION_ID, true, false, "long id",
				"the id of the relation to extract");

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
		String argId = commandLine.getOptionValue(OPTION_ID);

		long id = -1;
		if (argId != null) {
			try {
				id = Long.parseLong(argId);
			} catch (NumberFormatException e) {
				System.out.println("unable to parse id argument");
				System.exit(1);
			}
		}

		SingleRegionExtract extractor = new SingleRegionExtract();
		extractor.prepare(argInput, argOutput, id);
		extractor.execute();
	}

	private String argInput;
	private String argOutput;
	private long id;

	private OsmEntityProvider entityProvider;

	private void prepare(String argInput, String argOutput, long id)
	{
		this.argInput = argInput;
		this.argOutput = argOutput;
		this.id = id;
	}

	private void execute() throws IOException
	{
		TLongObjectHashMap<OsmNode> nodes = new TLongObjectHashMap<>();
		TLongObjectHashMap<OsmWay> ways = new TLongObjectHashMap<>();
		TLongObjectHashMap<OsmRelation> relations = new TLongObjectHashMap<>();

		List<OsmRelation> relationList = new ArrayList<>();

		InMemoryMapDataSet data = new InMemoryMapDataSet();
		data.setNodes(nodes);
		data.setWays(ways);
		data.setRelations(relations);
		entityProvider = data;

		InputStream is = new BufferedInputStream(new FileInputStream(argInput));
		OsmIterator iterator = OsmIoUtils.setupOsmIterator(is, FileFormat.TBO,
				false);

		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			if (container.getType() == EntityType.Node) {
				OsmNode node = (OsmNode) container.getEntity();
				nodes.put(node.getId(), node);
			} else if (container.getType() == EntityType.Way) {
				OsmWay way = (OsmWay) container.getEntity();
				ways.put(way.getId(), way);
			} else if (container.getType() == EntityType.Relation) {
				OsmRelation relation = (OsmRelation) container.getEntity();
				relations.put(relation.getId(), relation);
				relationList.add(relation);
			}
		}

		if (relationList.size() == 0) {
			System.out.println("no relation found.");
			System.exit(1);
		}

		if (id >= 0) {
			for (int i = 0; i < relationList.size(); i++) {
				OsmRelation relation = relationList.get(i);
				if (relation.getId() != id) {
					continue;
				}
				System.out.println(
						String.format("%d: %s", i, relation.toString()));
				Geometry region;

				try {
					region = new GeometryBuilder().build(relation,
							entityProvider);
				} catch (EntityNotFoundException e) {
					System.out.println(
							"unable to build region: " + e.getMessage());
					continue;
				}

				export(region, relation);
				return;
			}

		} else {
			for (int i = 0; i < relationList.size(); i++) {
				OsmRelation relation = relationList.get(i);
				System.out.println(
						String.format("%d: %s", i, relation.toString()));
				Geometry region;

				try {
					region = new GeometryBuilder().build(relation,
							entityProvider);
				} catch (EntityNotFoundException e) {
					System.out.println(
							"unable to build region: " + e.getMessage());
					continue;
				}

				export(region, relation);
				return;
			}
		}

	}

	private void export(Geometry region, OsmRelation relation)
	{
		EntityFile entityFile = new EntityFile();
		entityFile.setGeometry(region);

		for (int i = 0; i < relation.getNumberOfTags(); i++) {
			OsmTag tag = relation.getTag(i);
			entityFile.addTag(tag.getKey(), tag.getValue());
		}

		try {
			SmxFileWriter.write(entityFile, new File(argOutput));
		} catch (IOException e) {
			System.out.println(
					"unable to serialize, IOException: " + e.getMessage());
		} catch (TransformerException e) {
			System.out.println("unable to serialize, TransformerException: "
					+ e.getMessage());
		} catch (ParserConfigurationException e) {
			System.out.println(
					"unable to serialize, ParserConfigurationException: "
							+ e.getMessage());
		}
	}

}
