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
package inf.unibz.it.obda.gui.swing.queryhistory;

import java.util.LinkedList;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;


public class QueryhistoryController {

	private static QueryhistoryController instance = null;
	private int maxNrOfQueries = 8;
	private LinkedList<DefaultMutableTreeNode> queries = null;
	private Vector<QueryhistoryControllerListener> listeners = null;
	private QueryhistoryTreeModel model = null;
	
	private QueryhistoryController(){
		
		queries = new LinkedList<DefaultMutableTreeNode> ();
		listeners = new Vector<QueryhistoryControllerListener> ();
		model = new QueryhistoryTreeModel();
		addListener(model);
	}
	
	public QueryhistoryTreeModel getTreeModel() {
		return model;
	}

	public void addListener(QueryhistoryControllerListener listener) {
		
		listeners.add(listener);
	}

	public void removeListener(QueryhistoryControllerListener listener) {
		listeners.remove(listener);
	}
	
	public static QueryhistoryController getInstance (){
		
		if (instance == null){
			
			instance = new QueryhistoryController();
		}
		return instance;
	}
	
	public void addQuery(String query){
		
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(query);
		
		if(queries.size() < maxNrOfQueries){

			queries.addLast(node);
			for (QueryhistoryControllerListener listener : listeners) {
				listener.queryAdded(node);
			}
		}else{
			
			DefaultMutableTreeNode n = queries.getFirst();
			queries.removeFirst();
			for (QueryhistoryControllerListener listener : listeners) {
				listener.queryRemoved(n);
			}
			
			queries.addLast(node);
			for (QueryhistoryControllerListener listener : listeners) {
				listener.queryAdded(node);
			}
		}
	}
	
	public void removeQuery(DefaultMutableTreeNode node){
		
		queries.remove(node);
		for (QueryhistoryControllerListener listener : listeners) {
			listener.queryRemoved(node);
		}
	}

	public int getMaxNrOfQueries() {
		return maxNrOfQueries;
	}

	public void setMaxNrOfQueries(int maxNrOfQueries) {
		this.maxNrOfQueries = maxNrOfQueries;
		
		while (queries.size() > maxNrOfQueries){
			
			DefaultMutableTreeNode n = queries.getFirst();
			queries.removeFirst();
			for (QueryhistoryControllerListener listener : listeners) {
				listener.queryRemoved(n);
			}
		}
	}

	public LinkedList<DefaultMutableTreeNode> getQueries() {
		return queries;
	}


	public Vector<QueryhistoryControllerListener> getListeners() {
		return listeners;
	}

	
	
}
