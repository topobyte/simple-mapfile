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

package de.topobyte.simplemapfile.core;

import java.util.Map;
import java.util.TreeMap;

import org.locationtech.jts.geom.Geometry;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class EntityFile
{

	private Map<String, String> tags = new TreeMap<>();
	private Geometry geometry = null;

	public void addTag(String key, String value)
	{
		tags.put(key, value);
	}

	public Map<String, String> getTags()
	{
		return tags;
	}

	public Geometry getGeometry()
	{
		return geometry;
	}

	public void setGeometry(Geometry geometry)
	{
		this.geometry = geometry;
	}

}
