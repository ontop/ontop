package it.unibz.krdb.obda.protege4.gui.tab;

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

import it.unibz.krdb.obda.protege4.views.QueryInterfaceView;
import it.unibz.krdb.obda.protege4.views.QueryManagerView;

import java.util.HashSet;
import java.util.Iterator;

import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;

public class OBDAQueriesTab extends OWLWorkspaceViewsTab {

	private HashSet<QueryManagerView>	queryManagerComponent	= null;
	private HashSet<QueryInterfaceView>	queryInterfaceComponent	= null;

	/**
	 * 
	 */
	private static final long							serialVersionUID		= 1L;

	public void initialise() {
		super.initialise();
	}

	/***************************************************************************
	 * Registers a new Query interface and adds this interface as a listener to
	 * all registred query managers
	 * 
	 * @param newQueryInterface
	 */
	public void addQueryInterface(QueryInterfaceView newQueryInterface) {
		if (queryManagerComponent == null) {
			queryManagerComponent = new HashSet<QueryManagerView>();
		}
		for (Iterator<QueryManagerView> iterator = queryManagerComponent.iterator(); iterator.hasNext();) {
			try {
				QueryManagerView manager = (QueryManagerView) iterator.next();
				manager.addListener(newQueryInterface);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (queryInterfaceComponent == null) {
			queryInterfaceComponent = new HashSet<QueryInterfaceView>();
		}
		queryInterfaceComponent.add(newQueryInterface);
	}

	/***************************************************************************
	 * Adds a new query manager to the list of query managers and registers
	 * every Query View present as a listener to this new manager
	 * 
	 * @param newQueryManager
	 */
	public void addQueryManager(QueryManagerView newQueryManager) {
		if (queryInterfaceComponent == null) {
			queryInterfaceComponent = new HashSet<QueryInterfaceView>();
		}
		for (Iterator<QueryInterfaceView> iterator = queryInterfaceComponent.iterator(); iterator.hasNext();) {
			try {
				QueryInterfaceView cinterface = (QueryInterfaceView) iterator.next();
				newQueryManager.addListener(cinterface);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (queryManagerComponent == null) {
			queryManagerComponent = new HashSet<QueryManagerView>();
		}
		queryManagerComponent.add(newQueryManager);
	}
}
