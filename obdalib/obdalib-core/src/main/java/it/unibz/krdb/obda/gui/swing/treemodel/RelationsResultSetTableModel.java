/*******************************************************************************
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
package it.unibz.krdb.obda.gui.swing.treemodel;

import it.unibz.krdb.obda.model.DataSource;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * This class takes a JDBC ResultSet object and implements the TableModel
 * interface in terms of it so that a Swing JTable component can display the
 * contents of the ResultSet. Note that it requires a scrollable JDBC 2.0
 * ResultSet. Also note that it provides read-only access to the results
 */
public class RelationsResultSetTableModel implements TableModel {
	// ResultSet results; // The
	// ResultSet
	// to
	// interpret

	ResultSetMetaData			metadata;	// Additional
	// information
	// about
	// the
	// results

	int							numcols, numrows;	// How
	// many
	// rows
	// and
	// columns
	// in the table

	ResultSetTableModelFactory	f;

	// HashMap<String, Integer> rowCount = new HashMap<String, Integer>();

	// HashMap<Integer, String> tableNames = new HashMap<Integer, String>();

	String[]					rowCount	= null;

	String[]					tableNames	= null;
	
	private Vector<TableModelListener> listeners = null;
	
	DataSource source = null;

	/**
	 * This constructor creates a TableModel from a ResultSet. It is package
	 * private because it is only intended to be used by
	 * ResultSetTableModelFactory, which is what you should use to obtain a
	 * ResultSetTableModel
	 */
	public RelationsResultSetTableModel(ResultSet results, DataSource ds) throws SQLException {

		source = ds;
		listeners = new Vector<TableModelListener>();
		// this.results = results; // Save the results
		metadata = results.getMetaData(); // Get metadata on them
		numcols = metadata.getColumnCount(); // How many columns?

		ArrayList<String> tempNames = new ArrayList<String>();
		numrows = 0;
		while (results.next()) {
			numrows = numrows + 1;
//			if (!driver.equals("com.ibm.db2.jcc.DB2Driver")) {
				tempNames.add(results.getString("TABLE_NAME"));
//			} else {
//				tempNames.add(results.getString("NAME"));
//			}
		}
		tableNames = new String[numrows];
		Iterator<String> it = tempNames.iterator();
		int i = 0;
		while (it.hasNext()) {
			tableNames[i] = it.next();
			i = i + 1;
		}
		// results.last(); // Move to last row
		// numrows = results.getRow(); // How many rows?

		rowCount = new String[numrows];
		for (i = 0; i < numrows; i++) {
//			// results.absolute(i + 1); // Go to the specified row
			 rowCount[i] = "???";
//			// Integer.valueOf(factory.getRelationsRowCount(results.getString("TABLE_NAME")));
//			try {
//				rowCount[i] = Integer.valueOf(JDBCConnectionManager.getJDBCConnectionManager().getRelationsRowCount(tableNames[i], source));
//			} catch (NumberFormatException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}

	}

	/**
	 * Call this when done with the table model. It closes the ResultSet and the
	 * Statement object used to create it.
	 */
	public void close() {
		try {
			// results.getStatement().close();
		} catch (Exception e) {
			// e.printStackTrace(System.err);
		}
		;
	}

	/** Automatically close when we're garbage collected */
	protected void finalize() {
		// close();
	}

	public int getColumnCount() {
		return 2;
	}

	public int getRowCount() {
		return numrows;

	}

	public String getColumnName(int column) {
		if (column == 0)
			return "Relation Name";
		if (column == 1)
			return "Row Count";
		return "Nonexistent";
	}

	public Class getColumnClass(int column) {
		return String.class;
	}

	public Object getValueAt(int row, int column) {
		if (!((row >= 0) && (column >= 0))) {
			return "";
		}
		try {
			// results.absolute(row + 1); // Go to the specified row
			if (column == 0) {
				// return results.getString("TABLE_NAME");
				return tableNames[row];
			}
			if (column == 1) {
				{
					// return 0;
					try {
						String count = rowCount[row];
						return count;
						// if (count)
						// return factory.getRelationsRowCount((String)
						// results.getString("TABLE_NAME"));
					} catch (Exception e) {
						e.printStackTrace(System.err);
						return e.getMessage();
					}
				}
			} else
				return "ERROR";
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return e.toString();
		}
	}

	// Our table isn't editable
	public boolean isCellEditable(int row, int column) {
		return false;
	}


	public void setValueAt(Object value, int row, int column) {
		rowCount[row] = value.toString();
		fireTableChanged(row);
	}

	public void addTableModelListener(TableModelListener l) {
		listeners.add(l);
	}

	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}
	
	private void fireTableChanged(int row){
		Iterator<TableModelListener> it = listeners.iterator();
		while(it.hasNext()){
			it.next().tableChanged(new TableModelEvent(this, row));
		}
	}
}
