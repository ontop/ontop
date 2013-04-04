package it.unibz.krdb.obda.codec;

import it.unibz.krdb.obda.querymanager.QueryControllerGroup;
import it.unibz.krdb.obda.querymanager.QueryControllerQuery;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Note: This is a legacy code. Do not use instances of this class. This code
 * is used by the old test cases which needed to be updated.
 */
public class QueryGroupXMLReader {
	
	public QueryControllerQuery readQuery(Element dom_query) {
		if (!dom_query.getNodeName().equals("Query")) {
			return null;
		}
		String id = dom_query.getAttribute("id");
		String text = dom_query.getAttribute("text");
		QueryControllerQuery query = new QueryControllerQuery(id);
		query.setQuery(text);
		return query;
	}
	
	public QueryControllerGroup readQueryGroup(Element dom_group) {
		if (!dom_group.getNodeName().equals("QueryGroup")) {
			return null;
		}
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
