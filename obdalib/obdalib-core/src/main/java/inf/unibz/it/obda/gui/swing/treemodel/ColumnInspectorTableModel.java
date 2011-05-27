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

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * This class takes a JDBC ResultSet object and implements the TableModel
 * interface in terms of it so that a Swing JTable component can display the
 * contents of the ResultSet. Note that it requires a scrollable JDBC 2.0
 * ResultSet. Also note that it provides read-only access to the results
 */
public class ColumnInspectorTableModel implements TableModel {
	// ResultSet results; // The ResultSet to interpret

	ResultSetMetaData	metadata;	// Additional information about the
	// results

	int					numcols, numrows;	// How many rows and columns

	// in the table

	// ResultSetTableModelFactory factory;

	/**
	 * This constructor creates a TableModel from a ResultSet. It is package
	 * private because it is only intended to be used by
	 * ResultSetTableModelFactory, which is what you should use to obtain a
	 * ResultSetTableModel
	 */
	public ColumnInspectorTableModel(ResultSetMetaData meta) throws SQLException {
		// this.factory = parentFactory;
		// this.results = results; // Save the results
		metadata = meta; // Get metadata on them
		numcols = 4; // How many columns?
		// results.last(); // Move to last row
		numrows = metadata.getColumnCount(); // How many rows?

	}

	/**
	 * Call this when done with the table model. It closes the ResultSet and the
	 * Statement object used to create it.
	 */
	public void close() {
		// try {
		//		
		// } catch (SQLException e) {
		// e.printStackTrace(System.err);
		// }
		// ;
	}

	/** Automatically close when we're garbage collected */
	protected void finalize() {
		// close();
	}

	public int getColumnCount() {
		return 4;
	}

	public int getRowCount() {
		return numrows;

	}

	public String getColumnName(int column) {
		if (column == 0)
			return "Attribute";
		if (column == 1)
			return "Type";
		if (column == 2)
			return "Autoincrement";
		if (column == 3)
			return "Null";
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
			if (column == 0)
				return metadata.getColumnName(row + 1);
			if (column == 1) {

				return metadata.getColumnTypeName(row + 1);
				// return 0;
				// try {
				// return factory.getRelationsRowCount((String)
				// results.getObject(1));
				// } catch (Exception e) {
				// e.printStackTrace(System.err);
				// return e.getMessage();
				// }

			} if (column == 2) {
				return metadata.isAutoIncrement(row + 1);
			} if (column == 3) {
				return metadata.isNullable(row + 1);
			} else
				return "ERROR";
		} catch (SQLException e) {
			e.printStackTrace(System.err);
			return e.toString();
		}
	}

	// Our table isn't editable
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	// Since its not editable, we don't need to implement these methods
	public void setValueAt(Object value, int row, int column) {
	}

	public void addTableModelListener(TableModelListener l) {
	}

	public void removeTableModelListener(TableModelListener l) {
	}
}
