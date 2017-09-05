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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class SmxIndex
{

	private List<SmxIndexEntry> entries = new ArrayList<>();

	public void add(SmxIndexEntry entry)
	{
		entries.add(entry);
	}

	public List<SmxIndexEntry> getEntries()
	{
		return Collections.unmodifiableList(entries);
	}

	public void write(DataOutput out) throws IOException
	{
		out.writeInt(entries.size());
		for (int i = 0; i < entries.size(); i++) {
			SmxIndexEntry entry = entries.get(i);
			entry.write(out);
		}
	}

	public static SmxIndex read(DataInput in)
			throws IOException, ClassNotFoundException
	{
		SmxIndex index = new SmxIndex();
		int n = in.readInt();
		for (int i = 0; i < n; i++) {
			SmxIndexEntry entry = SmxIndexEntry.read(in);
			index.entries.add(entry);
		}
		return index;
	}

}
