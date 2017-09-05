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

package de.topobyte.simplemapfile.index;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import de.topobyte.adt.geo.BBox;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class SmxIndexEntry
{

	private BBox bbox;
	private String name;

	private SmxIndexEntry()
	{
		// private empty constructor for deserialization
	}

	public SmxIndexEntry(BBox bbox, String name)
	{
		this.bbox = bbox;
		this.name = name;
	}

	public BBox getBBox()
	{
		return bbox;
	}

	public String getName()
	{
		return name;
	}

	public void write(DataOutput out) throws IOException
	{
		out.writeDouble(bbox.getLon1());
		out.writeDouble(bbox.getLat1());
		out.writeDouble(bbox.getLon2());
		out.writeDouble(bbox.getLat2());
		out.writeUTF(name);
	}

	public static SmxIndexEntry read(DataInput in)
			throws IOException, ClassNotFoundException
	{
		SmxIndexEntry entry = new SmxIndexEntry();
		double lon1 = in.readDouble();
		double lat1 = in.readDouble();
		double lon2 = in.readDouble();
		double lat2 = in.readDouble();
		entry.bbox = new BBox(lon1, lat1, lon2, lat2);
		entry.name = in.readUTF();
		return entry;
	}

}
