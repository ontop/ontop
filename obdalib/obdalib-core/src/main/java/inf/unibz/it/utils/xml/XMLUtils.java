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


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public class XMLUtils {

	private XMLUtils() {

	}

	public static void saveDocumentToXMLFile(Document doc, String filename) throws FileNotFoundException, IOException {
		File file = new File(filename);
		saveDocumentToXMLFile(doc, file);
	}

	public static void saveDocumentToXMLFile(Document doc, File file) throws FileNotFoundException, IOException {
		FileOutputStream fos = new FileOutputStream(file);
		// XERCES 1 or 2 additionnal classes.
		OutputFormat of = new OutputFormat("XML","ISO-8859-1",true);
		of.setIndent(2);
		of.setIndenting(true);
		//of.setDoctype(null,"users.dtd");
		XMLSerializer serializer = new XMLSerializer(fos,of);
		// As a DOM Serializer
		serializer.asDOMSerializer();
		Element docroot = doc.getDocumentElement();
		serializer.serialize( docroot );
		fos.close();
	}


}
