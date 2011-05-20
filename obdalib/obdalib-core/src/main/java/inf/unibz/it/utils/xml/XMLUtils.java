/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro.
 * All rights reserved.
 *
 * The OBDA-API is licensed under the terms of the Lesser General Public
 * License v.3 (see OBDAAPI_LICENSE.txt for details). The components of this
 * work include:
 *
 * a) The OBDA-API developed by the author and licensed under the LGPL; and,
 * b) third-party components licensed under terms that may be different from
 *   those of the LGPL.  Information about such licenses can be found in the
 *   file named OBDAAPI_3DPARTY-LICENSES.txt.
 */
package inf.unibz.it.utils.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Set;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;

public class XMLUtils {

	private XMLUtils() {

	}

	public static void saveDocumentToXMLFile(Document doc, HashMap<String, String> prefixes, String filename)
			throws FileNotFoundException, IOException {
		File file = new File(filename);
		saveDocumentToXMLFile(doc, prefixes, file);
	}

	public static void saveDocumentToXMLFile(Document doc, HashMap<String, String> prefixes, File file)
			throws FileNotFoundException, IOException {

		File tmpFile = prepare(doc);
		String entityFragment = prepare(prefixes);

		// Input
		BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(tmpFile)));

		// Output
	    PrintWriter out = new PrintWriter(new FileOutputStream(file));

		String cursor = "";
		int line = 1;
		int TARGET_LINE = 2;  // after then <?xml ... ?> declaration
		while ((cursor = in.readLine()) != null) {
			if (line == TARGET_LINE)
				out.println(entityFragment);

		    out.println(cursor);

		    line++;
		}

		out.flush();
		out.close();
		in.close();

		tmpFile.delete();
	}
	
	public static void saveDocumentToXMLFile(Document doc, File file)
	throws FileNotFoundException, IOException {

		saveDocumentToXMLFile(doc, new HashMap<String, String>(), file);
	}

	private static File prepare(Document doc) throws IOException {

		File fOut = new File("$$$$$$.tmp");

		FileOutputStream outStream = new FileOutputStream(fOut);

		OutputFormat outFormat = new OutputFormat();
		outFormat.setMethod("xml");
		outFormat.setIndenting(true);
		outFormat.setIndent(2);
		outFormat.setLineWidth(0);

		XMLSerializer serializer = new XMLSerializer(outStream, outFormat);
		serializer.serialize(doc);
		outStream.close();

		return fOut;
	}

	private static String prepare(HashMap<String, String> prefixes) {

		String doctype = "<!DOCTYPE OBDA [\n";
		Set<String> prefixIds = prefixes.keySet();
		for (String id : prefixIds) {
			if (!(id.equals("version") ||
				  id.equals("xml:base") ||
				  id.equals("xmlns") ||
				  id.equals("rdf") ||
				  id.equals("rdfs") ||
				  id.equals("owl") ||
				  id.equals("xsd"))) {
				String uri = prefixes.get(id);
				doctype += "   <!ENTITY " + id + " '" +  uri + "'>\n";
			}
		}
		doctype += "]>";

		return doctype;
	}
}
