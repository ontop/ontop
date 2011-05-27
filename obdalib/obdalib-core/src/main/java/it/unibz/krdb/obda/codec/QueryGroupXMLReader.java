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
package it.unibz.krdb.obda.codec;

//TODO: Refactor so that this is actually an instance of a codec
/*import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryGroupTreeElement;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryTreeElement;*/
import it.unibz.krdb.obda.queryanswering.QueryControllerGroup;
import it.unibz.krdb.obda.queryanswering.QueryControllerQuery;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class QueryGroupXMLReader {
	
	
	public QueryControllerQuery readQuery(Element dom_query) {
		if (!dom_query.getNodeName().equals("Query"))
			return null;
		String id = dom_query.getAttribute("id");
		String text = dom_query.getAttribute("text");
		QueryControllerQuery query = new QueryControllerQuery(id);
		query.setQuery(text);
		return query;
	}
	

	public QueryControllerGroup readQueryGroup(Element dom_group) {
		if (!dom_group.getNodeName().equals("QueryGroup"))
			return null;
		String id = dom_group.getAttribute("id");
		QueryControllerGroup group = new QueryControllerGroup(id);
		NodeList queries = dom_group.getElementsByTagName("Query");
		for (int i = 0; i < queries.getLength(); i++) {
			Element dom_query = (Element)queries.item(i);
			QueryControllerQuery query = readQuery(dom_query);
			group.addQuery(query);
		}
		return group;
	}
}
