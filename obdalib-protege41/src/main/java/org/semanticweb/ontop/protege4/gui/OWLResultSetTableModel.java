package org.semanticweb.ontop.protege4.gui;

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

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.semanticweb.ontop.io.PrefixManager;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLResultSet;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLPropertyAssertionObject;

public class OWLResultSetTableModel implements TableModel {

	private QuestOWLResultSet results;
	private int numcols;
	private int numrows;
	private int fetchSizeLimit;
	private boolean isHideUri;
	private boolean isFetchAll;

	// True while table is fetching sql results
	private boolean isFetching = false;
	// Set to true to signal the fetching thread to stop
	private boolean stopFetching = false;

	// The thread where the rows are fetched
	Thread rowFetcher;

	// Tabular data for exporting result
	private Vector<String[]> tabularData;
	// Vector data for presenting result to table GUI
	private Vector<String[]> resultsTable;

	private Vector<TableModelListener> listener;

	private PrefixManager prefixman;


	/**
	 * This constructor creates a TableModel from a ResultSet. It is package
	 * private because it is only intended to be used by
	 * ResultSetTableModelFactory, which is what you should use to obtain a
	 * ResultSetTableModel
	 */
	public OWLResultSetTableModel(QuestOWLResultSet results, PrefixManager prefixman, 
			boolean hideUri, boolean fetchAll, int fetchSizeLimit) throws OWLException {
		this.results = results;
		this.prefixman = prefixman;
		this.isHideUri = hideUri;
		this.isFetchAll = fetchAll;
		this.fetchSizeLimit = fetchSizeLimit;
		this.isFetching = true;
		this.stopFetching = false;

		numcols = results.getColumnCount();
		numrows = 0;

		resultsTable = new Vector<String[]>();
		listener = new Vector<TableModelListener>();

		fetchRowsAsync();
	}

	private void fetchRowsAsync() throws OWLException{
		rowFetcher = new Thread(){
			public void run() {
				try {
					fetchRows(fetchSizeLimit);
				} catch (Exception e){
					if(!stopFetching){
						JOptionPane.showMessageDialog(
								null,
								"Error when fetching results. Aborting. " + e.toString());
					} 
					e.printStackTrace();
				} finally {
					isFetching = false;
				}

			}
		};
		rowFetcher.start();
	}

	/**
	 * Returns whether the table is still being populated by SQL fetched from the result object. 
	 * Called from the QueryInterfacePanel to decide whether to continue updating the result count
	 * @return
	 */
	public boolean isFetching(){
		return this.isFetching;
	}

	private void fetchRows(int size) throws OWLException, InterruptedException {
		if (results == null) {
			return;
		}

		for (int rows_fetched = 0; results.nextRow() && !stopFetching && (isFetchingAll() || rows_fetched < size);rows_fetched++) {
			String[] crow = new String[numcols];
			for (int j = 0; j < numcols; j++) {
				if(stopFetching)
					break;
				OWLPropertyAssertionObject constant = results
						.getOWLPropertyAssertionObject(j + 1);
				if (constant != null) {
					crow[j] = constant.toString();
				}
				else {
					crow[j] = "";
				}
			}
			if(!stopFetching){
				resultsTable.add(crow);
				this.updateRowCount();
				this.fireModelChangedEvent();
			}
		}
		isFetching = false;
	}

	private void updateRowCount() {
		numrows++;

	}

	/**
	 * Fetch all the tuples returned by the result set.
	 */
	public List<String[]> getTabularData() throws OWLException, InterruptedException {
		if (tabularData == null) {
			tabularData = new Vector<String[]>();
			String[] columnName = results.getSignature().toArray(new String[numcols]);		
			// Append the column names
			tabularData.add(columnName);
			while(this.isFetching){
				Thread.sleep(10);
			}
			if(stopFetching)
				return null;
			// Append first the already fetched tuples
			tabularData.addAll(resultsTable); 
			// Append the rest
			while (!stopFetching && results.nextRow()) {
				String[] crow = new String[numcols];
				for (int j = 0; j < numcols; j++) {
					OWLPropertyAssertionObject constant = results
							.getOWLPropertyAssertionObject(j + 1);
					if (constant != null) {
						crow[j] = constant.toString();
					}
					else {
						crow[j] = "";
					}
				}
				tabularData.add(crow);
			}
		}
		return tabularData;
	}

	/**
	 * Call this when done with the table model. It closes the ResultSet and the
	 * Statement object used to create it.
	 */
	public void close() {
		stopFetching = true;
		if(rowFetcher != null)
			rowFetcher.interrupt();
		try {
			results.close();
		} catch (OWLException e) {
			// TODO: Handle this in a reasonable manner
			System.out.println(e);
		}
	}
	

	
	/** Automatically close when we're garbage collected */
	@Override
	protected void finalize() {
		close();
	}

	// These two TableModel methods return the size of the table
	public int getColumnCount() {
		return numcols;
	}

	public int getRowCount() {
		return numrows;
	}

	// This TableModel method returns columns names from the ResultSetMetaData
	public String getColumnName(int column) {
		try {
			java.util.List<String> signature = results.getSignature();
			if (signature != null && column < signature.size()) {
				return results.getSignature().get(column);
			} else {
				return "";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "ERROR";
		}
	}

	// This TableModel method specifies the data type for each column.
	// We could map SQL types to Java types, but for this example, we'll just
	// convert all the returned data to strings.
	public Class getColumnClass(int column) {
		return String.class;
	}

	/**
	 * This is the key method of TableModel: it returns the value at each cell
	 * of the table. We use strings in this case. If anything goes wrong, we
	 * return the exception as a string, so it will be displayed in the table.
	 * Note that SQL row and column numbers start at 1, but TableModel column
	 * numbers start at 0.
	 */
	public Object getValueAt(int row, int column) {
		String value = resultsTable.get(row)[column];
		if (value == null) {
			return "";
		}
		else if (isHideUri) {
			return prefixman.getShortForm(value);
		} else {
			return value;
		}
	}



	private boolean isFetchingAll() {
		return isFetchAll;
	}



	// Our table isn't editable
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	// Since its not editable, we don't need to implement these methods
	public void setValueAt(Object value, int row, int column) {
		// NO-OP
	}

	public void addTableModelListener(TableModelListener l) {
		listener.add(l);
	}

	public void removeTableModelListener(TableModelListener l) {
		listener.remove(l);
	}


	private void fireModelChangedEvent() {
		for(int i = 0; !stopFetching && i < listener.size(); i++) {
			TableModelListener tl = listener.get(i);
			synchronized (tl){
				tl.tableChanged(new TableModelEvent(this));
			}
		}
	}
}
