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

package de.topobyte.simplemapfile.xml;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public enum GeometryType {

	WKB_BASE64,
	JSG_BASE64;

	public static Map<String, GeometryType> switcher = new HashMap<>();

	static {
		switcher.put("wkb-base64", WKB_BASE64);
		switcher.put("jsg-base64", JSG_BASE64);
	}

}
