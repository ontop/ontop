/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.gui.swing.treemodel;

import javax.swing.tree.DefaultMutableTreeNode;

public class QueryTreeElement extends DefaultMutableTreeNode implements TreeElement {

	private static final long serialVersionUID = -5221902062065891204L;
	private String id = "";
	private String query = "";

	public QueryTreeElement(String id, String query) {
		this.id = id;
		setQuery(query);
		setAllowsChildren(false);
	}

	public String getID() {
		return id;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getQuery() {
		return query;
	}

	public String getNodeName() {
		return id + ": " + query.toString();
	}

	public String toString() {
		return getNodeName();
	}

	@Override
	public Object getUserObject() {
		return getNodeName();
	}
}
