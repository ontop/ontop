package it.unibz.krdb.obda.codec;

import it.unibz.krdb.obda.querymanager.QueryControllerEntity;
import it.unibz.krdb.obda.querymanager.QueryControllerGroup;
import it.unibz.krdb.obda.querymanager.QueryControllerQuery;

import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Note: This is a legacy code. Do not use instances of this class. This code
 * is used by the old test cases which needed to be updated.
 */
public class QueryGroupXMLRenderer {
	public Element render(Element parent, QueryControllerQuery query) {
		Document doc = parent.getOwnerDocument();
		Element query_element = doc.createElement("Query");
		query_element.setAttribute("id", query.getID());
		query_element.setAttribute("text", query.getQuery());
		return query_element;
	}

	public Element render(Element parent, QueryControllerGroup group) {
		Document doc = parent.getOwnerDocument();
		Element group_element = doc.createElement("QueryGroup");
		group_element.setAttribute("id", group.getID());
		Vector<QueryControllerQuery> queries = group.getQueries();
		for (QueryControllerQuery query : queries) {
			Element query_element = render(group_element, query);
			group_element.appendChild(query_element);
		}
		return group_element;
	}

	public Element render(Element parent, QueryControllerEntity element) {
		if (element instanceof QueryControllerQuery) {
			return render(parent, (QueryControllerQuery) element);
		} else if (element instanceof QueryControllerGroup) {
			return render(parent, (QueryControllerGroup) element);
		}
		return null;
	}
}
