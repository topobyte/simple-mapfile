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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.topobyte.simplemapfile.core.EntityFile;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class SmxFileWriter
{

	private static int INDENT = 8;
	private static int LINE_WIDTH = 76;

	private static String INDENT_AMOUNT = String.format("%d", INDENT);
	private static String INDENT_STRING;
	static {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < INDENT; i++) {
			builder.append(" ");
		}
		INDENT_STRING = builder.toString();
	}

	public static void write(EntityFile entityFile, String filename)
			throws TransformerException, ParserConfigurationException,
			IOException
	{
		write(entityFile, new File(filename));
	}

	public static void write(EntityFile entityFile, File file)
			throws TransformerException, ParserConfigurationException,
			IOException
	{
		FileOutputStream fos = new FileOutputStream(file);
		StreamResult streamResult = new StreamResult(fos);
		write(entityFile, streamResult);
		fos.close();
	}

	public static void write(EntityFile entityFile, OutputStream output)
			throws TransformerException, ParserConfigurationException
	{
		StreamResult streamResult = new StreamResult(output);
		write(entityFile, streamResult);
	}

	private static void write(EntityFile entityFile, StreamResult streamResult)
			throws TransformerException, ParserConfigurationException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();

		createDocument(document, entityFile);

		SAXTransformerFactory saxFactory = (SAXTransformerFactory) TransformerFactory
				.newInstance();
		TransformerHandler handler = saxFactory.newTransformerHandler();
		Transformer serializer = handler.getTransformer();
		serializer.setOutputProperty(OutputKeys.INDENT, "yes");
		serializer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", INDENT_AMOUNT);

		handler.setResult(streamResult);

		DOMSource domSource = new DOMSource(document.getDocumentElement());
		serializer.transform(domSource, streamResult);
	}

	private static void createDocument(Document document, EntityFile entityFile)
	{
		Element entity = document.createElement("entity");
		document.appendChild(entity);

		// add tags

		Map<String, String> tags = entityFile.getTags();
		for (String key : tags.keySet()) {
			String value = tags.get(key);
			Element tag = document.createElement("tag");
			entity.appendChild(tag);
			tag.setAttribute("k", key);
			tag.setAttribute("v", value);
		}

		// add geometry

		Element geometry = document.createElement("geometry");
		entity.appendChild(geometry);
		geometry.setAttribute("type", "wkb-base64");

		Geometry geom = entityFile.getGeometry();
		String base64 = createTextRepresentation(geom);
		String formatted = format(base64);
		geometry.setTextContent(formatted);
	}

	private static String createTextRepresentation(Geometry geom)
	{
		WKBWriter wkbWriter = new WKBWriter();
		byte[] bytes = wkbWriter.write(geom);
		String base64String = Base64.encodeBase64String(bytes);
		return base64String;
	}

	private static String format(String text)
	{
		StringBuilder strb = new StringBuilder();
		String newLine = System.getProperty("line.separator");
		int length = text.length();
		int size = LINE_WIDTH;
		strb.append(newLine);
		for (int i = 0; i < length; i += size) {
			int end = i + size;
			if (end >= length) {
				end = length;
			}
			String line = text.substring(i, end);
			strb.append(line);
			strb.append(newLine);
		}
		strb.append(INDENT_STRING);
		return strb.toString();
	}

}
