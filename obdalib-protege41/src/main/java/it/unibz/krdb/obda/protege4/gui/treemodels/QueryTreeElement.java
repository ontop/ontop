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

import it.unibz.krdb.obda.protege4.gui.treemodels.TreeElement;

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
