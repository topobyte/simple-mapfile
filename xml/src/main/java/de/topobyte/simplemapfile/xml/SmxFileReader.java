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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import de.topobyte.simplemapfile.core.EntityFile;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class SmxFileReader
{

	public static EntityFile read(String filename)
			throws ParserConfigurationException, SAXException, IOException
	{
		File file = new File(filename);
		return read(file);
	}

	public static EntityFile read(File file)
			throws ParserConfigurationException, SAXException, IOException
	{
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		SmxFileHandler handler = new SmxFileHandler();

		if (!file.exists()) {
			throw new FileNotFoundException();
		}

		if (!file.canRead()) {
			throw new IOException("unable to read from specified file");
		}

		if (file.exists() && file.canRead()) {
			parser.parse(file, handler);
		}

		return handler.getEntity();
	}

	public static EntityFile read(InputStream input)
			throws SAXException, IOException, ParserConfigurationException
	{
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		SmxFileHandler handler = new SmxFileHandler();

		parser.parse(input, handler);

		return handler.getEntity();
	}

}
