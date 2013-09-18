/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.protege4.gui.treemodels;

import it.unibz.krdb.obda.protege4.gui.treemodels.QueryTreeElement;
import it.unibz.krdb.obda.protege4.gui.treemodels.TreeElement;

import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

public class QueryGroupTreeElement extends DefaultMutableTreeNode implements TreeElement {

	private static final long serialVersionUID = 7496292557025215559L;

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

	/**
	 * Removes a query from the group and returns the removed query, or null if
	 * the query was not found in this group.
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
	 * Searches a specific query and returns the object query else returns null.
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
