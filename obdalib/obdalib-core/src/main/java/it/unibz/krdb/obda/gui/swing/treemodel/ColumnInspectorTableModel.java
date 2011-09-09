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
package it.unibz.krdb.obda.gui.swing.treemodel;

import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.api.Attribute;
import it.unibz.krdb.sql.api.TablePrimary;

import java.util.ArrayList;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * This class takes a JDBC ResultSet object and implements the TableModel
 * interface in terms of it so that a Swing JTable component can display the
 * contents of the ResultSet. Note that it requires a scrollable JDBC 2.0
 * ResultSet. Also note that it provides read-only access to the results
 */
public class ColumnInspectorTableModel implements TableModel {

	private static final int NUM_COLS = 4;
	private static final String[] COLUMN_NAMES = {
		"Attribute", "Type", "Primary Key", "Nullable"
	};
	
	private ArrayList<Attribute> attributes;
	
	public ColumnInspectorTableModel(DBMetadata metadata, String tableName) {
		TablePrimary table = metadata.getTable(tableName);
		attributes = table.getAttributeList();
	}

	@Override
	public int getColumnCount() {
		return NUM_COLS;
	}

	@Override
	public int getRowCount() {		
		return attributes.size();
	}

	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}

	@Override
	public Class getColumnClass(int column) {
		return String.class;
	}

	@Override
	public Object getValueAt(int row, int column) {
		
		Attribute attr = attributes.get(row);
		
		switch(column) {
			case 0: return attr.name;
			case 1: return attr.type;
			case 2: return (attr.bPrimaryKey)? "Yes" : "No";
			case 3: return (attr.canNull==0)? "No" : "Yes";
		}
		return "";
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		// Does nothing.
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
		// Does nothing.
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		// Does nothing.
	}
}
