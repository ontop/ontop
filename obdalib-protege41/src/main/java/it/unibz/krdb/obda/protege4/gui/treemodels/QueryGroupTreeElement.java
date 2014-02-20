package it.unibz.krdb.obda.protege4.gui.treemodels;

/*
 * #%L
 * ontop-protege4
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen Bolzano.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
