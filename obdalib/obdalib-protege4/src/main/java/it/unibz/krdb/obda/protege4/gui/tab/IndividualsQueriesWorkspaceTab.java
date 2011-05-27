package it.unibz.krdb.obda.protege4.gui.tab;

import it.unibz.krdb.obda.protege4.gui.view.query.QueryInterfaceViewComponent;
import it.unibz.krdb.obda.protege4.gui.view.query.QueryManagerViewComponent;

import java.util.HashSet;
import java.util.Iterator;

import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;

public class IndividualsQueriesWorkspaceTab extends OWLWorkspaceViewsTab {

	private HashSet<QueryManagerViewComponent>	queryManagerComponent	= null;
	private HashSet<QueryInterfaceViewComponent>	queryInterfaceComponent	= null;

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
	public void addQueryInterface(QueryInterfaceViewComponent newQueryInterface) {
		if (queryManagerComponent == null) {
			queryManagerComponent = new HashSet<QueryManagerViewComponent>();
		}
		for (Iterator<QueryManagerViewComponent> iterator = queryManagerComponent.iterator(); iterator.hasNext();) {
			try {
				QueryManagerViewComponent manager = (QueryManagerViewComponent) iterator.next();
				manager.addListener(newQueryInterface);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (queryInterfaceComponent == null) {
			queryInterfaceComponent = new HashSet<QueryInterfaceViewComponent>();
		}
		queryInterfaceComponent.add(newQueryInterface);
	}

	/***************************************************************************
	 * Adds a new query manager to the list of query managers and registers
	 * every Query View present as a listener to this new manager
	 * 
	 * @param newQueryManager
	 */
	public void addQueryManager(QueryManagerViewComponent newQueryManager) {
		if (queryInterfaceComponent == null) {
			queryInterfaceComponent = new HashSet<QueryInterfaceViewComponent>();
		}
		for (Iterator<QueryInterfaceViewComponent> iterator = queryInterfaceComponent.iterator(); iterator.hasNext();) {
			try {
				QueryInterfaceViewComponent cinterface = (QueryInterfaceViewComponent) iterator.next();
				newQueryManager.addListener(cinterface);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (queryManagerComponent == null) {
			queryManagerComponent = new HashSet<QueryManagerViewComponent>();
		}
		queryManagerComponent.add(newQueryManager);
	}
}
