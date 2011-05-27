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
package inf.unibz.it.obda.gui.swing.treemodel;

import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

public class QueryGroupTreeElement extends DefaultMutableTreeNode implements
		TreeElement {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 7496292557025215559L;

	private Vector<QueryTreeElement> queries = null;

	private String group_id = "";

	public QueryGroupTreeElement(String group_id) {
		this.setID(group_id);

	}

	public void setID(String group_id) {
		this.group_id = group_id;
	}

	public String getID() {
		return group_id;
	}

	public String getNodeName() {
		return group_id;
	}

	public void addQuery(QueryTreeElement query) {
		queries.add(query);
	}

	public void removeQuery(QueryTreeElement query) {
		queries.remove(query);
	}

	/***************************************************************************
	 * Removes a query from the group and returns the removed query, or null if
	 * the query was not found in this group.
	 * 
	 * @param query_id
	 * @return
	 */
	public QueryTreeElement removeQuery(String query_id) {
		for (QueryTreeElement query : queries) {
			if (query.getID().equals(query_id)) {
				queries.remove(query);
				return query;
			}
		}
		return null;
	}

	public Vector<QueryTreeElement> getQueries() {
		return queries;
	}

	/**
	 * Searches a specific query and returns the object query else returns null
	 * 
	 * @param id
	 * @return
	 */
	public QueryTreeElement getQuery(String id) {
		for (QueryTreeElement query : queries) {
			if (query.getID().equals(id)) {
				return query;
			}
		}
		return null;
	}

	public String toString() {
		return getNodeName();
	}

	@Override
	public Object getUserObject() {
		return getNodeName();
	}

}
