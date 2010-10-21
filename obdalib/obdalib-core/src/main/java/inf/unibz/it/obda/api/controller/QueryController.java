/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro. All rights reserved.
 * 
 * The OBDA-API is licensed under the terms of the Lesser General Public License
 * v.3 (see OBDAAPI_LICENSE.txt for details). The components of this work
 * include:
 * 
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, b)
 * third-party components licensed under terms that may be different from those
 * of the LGPL. Information about such licenses can be found in the file named
 * OBDAAPI_3DPARTY-LICENSES.txt.
 */
package inf.unibz.it.obda.api.controller;

import inf.unibz.it.obda.codec.xml.query.XMLReader;
import inf.unibz.it.obda.codec.xml.query.XMLRenderer;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryControllerGroup;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryControllerQuery;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryControllerTreeModel;

import java.util.Iterator;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/***
 * Controller for the query manager
 */
public class QueryController {

	private static QueryController			instance		= null;
	private Vector<QueryControllerEntity>	collection		= null;
	private Vector<QueryControllerListener>	listeners		= null;
	private QueryControllerTreeModel		treemodel		= null;
	private boolean							eventDisabled	= false;

	public QueryController() {
		collection = new Vector<QueryControllerEntity>();
		listeners = new Vector<QueryControllerListener>();
		this.treemodel = new QueryControllerTreeModel(this);
		addListener(treemodel);
	}

	public QueryControllerTreeModel getTreeModel() {
		return treemodel;
	}

	public void addListener(QueryControllerListener listener) {
		if (listeners.contains(listener))
			return;
		listeners.add(listener);
	}

	public void removeListener(QueryControllerListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Creates a new group and adds it to the vector QueryControllerEntity
	 */
	public void createGroup(String group_name) {
		if (getElementPosition(group_name) == -1) {
			QueryControllerGroup group = new QueryControllerGroup(group_name);
			collection.add(group);
			fireElementAdded(group);
		} else {
			System.out.println("Group already exists!");
		}
	}

	/**
	 * Removes a group from the vector QueryControllerEntity
	 */
	public void removeGroup(String group_name) {
		for (Iterator<QueryControllerEntity> iterator = collection.iterator(); iterator.hasNext();) {
			Object temporal = iterator.next();
			if (!(temporal instanceof QueryControllerGroup))
				continue;
			QueryControllerGroup element = (QueryControllerGroup) temporal;
			if (element instanceof QueryControllerGroup) {
				QueryControllerGroup group = (QueryControllerGroup) element;
				if (group.getID().equals(group_name)) {
					collection.remove(group);
					fireElementRemoved(group);
					return;
				}
			}
		}
	}

	/**
	 * Creates a new query and adds it to the vector QueryControllerEntity
	 */
	public QueryControllerQuery addQuery(String querystr, String id) {
		QueryControllerQuery query = null;
		if (getElementPosition(id) == -1) {
			query = new QueryControllerQuery(id);
			query.setQuery(querystr);
			collection.add(query);
			fireElementAdded(query);
		} else {
			System.out.println("Query already exists!");
		}
		return query;
	}

	/**
	 * Removes all the elements from the vector QueryControllerEntity
	 */
	public void removeAllQueriesAndGroups() {
		Vector<QueryControllerEntity> elements = getElements();
		for (QueryControllerEntity treeElement : elements) {
			fireElementRemoved(treeElement);
		}
		collection.removeAllElements();
	}

	/**
	 * Creates a new query into a group and adds it to the vector
	 * QueryControllerEntity
	 */
	public QueryControllerQuery addQuery(String querystr, String id, String groupid) {
		QueryControllerQuery query = null;
		if (getElementPosition(id) == -1) {
			query = new QueryControllerQuery(id);
			query.setQuery(querystr);
			QueryControllerGroup group = getGroup(groupid);
			group.addQuery(query);
			fireElementAdded(query, group);
		} else {
			System.out.println("Query already exists!");
		}
		return query;
	}

	/**
	 * Removes a query from the vector QueryControllerEntity
	 */
	public void removeQuery(String id) {
		int index = getElementPosition(id);
		QueryControllerEntity element = (QueryControllerEntity) collection.get(index);

		if (element instanceof QueryControllerQuery) {
			collection.remove(index);
			fireElementRemoved(element);
			return;
		} else {
			QueryControllerGroup group = (QueryControllerGroup) element;
			Vector<QueryControllerQuery> queries_ingroup = group.getQueries();
			for (QueryControllerQuery query : queries_ingroup) {
				if (query.getID().equals(id)) {
					fireElementRemoved(group.removeQuery(query.getID()), group);
					return;
				}
			}
		}
	}

	public void fromDOM(Element idconstraints) {
		NodeList xml_elements = idconstraints.getChildNodes();
		XMLReader xml_reader = new XMLReader();

		for (int i = 0; i < xml_elements.getLength(); i++) {
			Node node = xml_elements.item(i);
			if (node instanceof Element) {
				Element element = (Element) xml_elements.item(i);
				if (element.getNodeName().equals("Query")) {
					QueryControllerQuery query = xml_reader.readQuery(element);
					addQuery(query.getQuery(), query.getID());
				} else if ((element.getNodeName().equals("QueryGroup"))) {
					QueryControllerGroup group = xml_reader.readQueryGroup(element);
					createGroup(group.getID());
					Vector<QueryControllerQuery> queries = group.getQueries();
					for (QueryControllerQuery query : queries) {
						addQuery(query.getQuery(), query.getID(), group.getID());
					}
				}
			}
		}
	}

	public Element toDOM(Element parent) {
		Document doc = parent.getOwnerDocument();
		XMLRenderer xmlrendrer = new XMLRenderer();
		Element savedqueries = doc.createElement("SavedQueries");
		for (QueryControllerEntity element : collection) {
			Element xmlconstraint = xmlrendrer.render(savedqueries, element);
			savedqueries.appendChild(xmlconstraint);
		}
		return savedqueries;
	}

	/**
	 * Searches a group and returns the object else returns null
	 */
	public QueryControllerGroup getGroup(String groupid) {
		int index = getElementPosition(groupid);
		if (index == -1)
			return null;
		QueryControllerGroup group = (QueryControllerGroup) collection.get(index);
		return group;
	}

	/***************************************************************************
	 * Returns the index of the element in the vector. If its is a query and the
	 * query is found inside a query group. The position of the group is
	 * returned instead.
	 * 
	 * @param element_id
	 * @return
	 */
	public int getElementPosition(String element_id) {
		int index = -1;
		for (int i = 0; i < collection.size(); i++) {
			QueryControllerEntity element = (QueryControllerEntity) collection.get(i);

			if (element instanceof QueryControllerQuery) {
				QueryControllerQuery query = (QueryControllerQuery) element;
				if (query.getID().equals(element_id)) {
					index = i;
					break;
				}
			}

			if (element instanceof QueryControllerGroup) {
				QueryControllerGroup group = (QueryControllerGroup) element;
				if (group.getID().equals(element_id)) {
					index = i;
					break;
				}
				/***************************************************************
				 * Searching inside the group.
				 */
				else {

					Vector<QueryControllerQuery> queries_ingroup = group.getQueries();
					for (QueryControllerQuery query : queries_ingroup) {
						if (query.getID().equals(element_id)) {
							index = i;
							break;
						}
					}
				}
			}
		}
		return index;
	}

	public Vector<QueryControllerEntity> getElements() {
		return this.collection;
	}

	/**
	 * Returns all the groups added
	 */
	public Vector<QueryControllerGroup> getGroups() {
		Vector<QueryControllerGroup> groups = new Vector<QueryControllerGroup>();
		for (QueryControllerEntity element : collection) {
			if (element instanceof QueryControllerGroup) {
				groups.add((QueryControllerGroup) element);
			}
		}
		return groups;
	}

	public void fireElementAdded(QueryControllerEntity element) {
		if (!eventDisabled) {
			for (QueryControllerListener listener : listeners) {
				listener.elementAdded(element);
			}
		}
	}

	public void fireElementAdded(QueryControllerQuery query, QueryControllerGroup group) {
		if (!eventDisabled) {
			for (QueryControllerListener listener : listeners) {
				listener.elementAdded(query, group);
			}
		}
	}

	public void fireElementRemoved(QueryControllerEntity element) {
		if (element instanceof QueryControllerGroup || element instanceof QueryControllerQuery) {
			if (!eventDisabled) {
				for (QueryControllerListener listener : listeners) {
					listener.elementRemoved(element);
				}
			}
		}
	}

	public void fireElementRemoved(QueryControllerQuery query, QueryControllerGroup group) {
		if (!eventDisabled) {
			for (QueryControllerListener listener : listeners) {
				listener.elementRemoved(query, group);
			}
		}
	}

	public void fireElementChanged(QueryControllerQuery query) {
		if (!eventDisabled) {
			for (QueryControllerListener listener : listeners) {
				listener.elementChanged(query);
			}
		}
	}

	public void fireElementChanged(QueryControllerQuery query, QueryControllerGroup group) {
		if (!eventDisabled) {
			for (QueryControllerListener listener : listeners) {
				listener.elementChanged(query, group);
			}
		}
	}

	public void setEventsDisabled(boolean value) {
		eventDisabled = value;
		return;
	}

	public boolean getEventsDisabled() {
		return eventDisabled;
	}
}
