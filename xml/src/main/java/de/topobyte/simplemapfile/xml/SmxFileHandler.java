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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64InputStream;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.InputStreamInStream;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.topobyte.simplemapfile.core.EntityFile;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class SmxFileHandler extends DefaultHandler
{

	final static Logger logger = LoggerFactory.getLogger(SmxFileHandler.class);

	private EntityFile entityFile = new EntityFile();

	public EntityFile getEntity()
	{
		return entityFile;
	}

	private GeometryType geometryType;
	private boolean gatherBase64 = false;
	private StringBuilder strb;

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
	{
		gatherBase64 = false;

		if (qName.equals("entity")) {
		} else if (qName.equals("tag")) {
			String valK = attributes.getValue("k");
			String valV = attributes.getValue("v");
			entityFile.addTag(valK, valV);
		} else if (qName.equals("geometry")) {
			String valType = attributes.getValue("type");
			GeometryType type = GeometryType.switcher.get(valType);
			geometryType = type;
			strb = new StringBuilder();
			if (type != null) {
				switch (type) {
				case WKB_BASE64:
					gatherBase64 = true;
					break;
				case JSG_BASE64:
					gatherBase64 = true;
					break;
				}
			}
		} else {
			System.out.println(qName);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException
	{
		if (qName.equals("geometry")) {
			if (geometryType == GeometryType.WKB_BASE64) {
				String base64 = strb.toString();
				byte[] bytes = Base64.decodeBase64(base64);
				WKBReader wkbReader = new WKBReader();
				try {
					Geometry geometry = wkbReader.read(new InputStreamInStream(
							new ByteArrayInputStream(bytes)));
					entityFile.setGeometry(geometry);
				} catch (IOException e) {
					logger.warn("unable to read wkb, IOException: "
							+ e.getMessage());
				} catch (ParseException e) {
					logger.warn("unable to read wkb, ParseException: "
							+ e.getMessage());
				} catch (IllegalArgumentException e) {
					logger.warn("unable to read wkb, IllegalArgumentException: "
							+ e.getMessage());
				}
			} else if (geometryType == GeometryType.JSG_BASE64) {
				String base64 = strb.toString();
				InputStream is = new ByteArrayInputStream(base64.getBytes());
				Base64InputStream base64is = new Base64InputStream(is);
				try (ObjectInputStream ois = new ObjectInputStream(base64is)) {
					Geometry geometry = (Geometry) ois.readObject();
					entityFile.setGeometry(geometry);
				} catch (IOException e) {
					logger.warn("unable to read jsg, IOException: "
							+ e.getMessage());
				} catch (ClassNotFoundException e) {
					logger.warn("unable to read jsg, ClassNotFoundException: "
							+ e.getMessage());
				}
			}
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		if (gatherBase64) {
			strb.append(ch, start, length);
		}
	}

}
