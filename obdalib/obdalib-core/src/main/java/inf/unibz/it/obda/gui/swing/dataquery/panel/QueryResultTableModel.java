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
package inf.unibz.it.obda.gui.swing.dataquery.panel;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;


public class QueryResultTableModel extends DefaultTableModel {
	//public ArrayList<ArrayList<String>> results = null;
	
	public QueryResultTableModel(Vector data, Vector columnnames) {
		super(data, columnnames);
		//this.results = results;
	}

//	public void addTableModelListener(TableModelListener l) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	public Class<?> getColumnClass(int columnIndex) {
//		return String.class;
//	}
//
//	public int getColumnCount() {
//		return results.get(0).size();
//	}
//
//	public String getColumnName(int columnIndex) {
//		// TODO Auto-generated method stub
//		return results.get(0).get(columnIndex);
//	}
//
//	public int getRowCount() {
//		// TODO Auto-generated method stub
//		return results.size() - 1;
//	}
//
//	public Object getValueAt(int rowIndex, int columnIndex) {
//		// TODO Auto-generated method stub
//		return results.get(rowIndex + 1).get(columnIndex);
//	}
//
//	public boolean isCellEditable(int rowIndex, int columnIndex) {
//		// TODO Auto-generated method stub
//		return false;
//	}

//	public void removeTableModelListener(TableModelListener l) {
//		// TODO Auto-generated method stub
//		
//	}

//	public void setValueAt(Object value, int rowIndex, int columnIndex) {
//		// TODO Auto-generated method stub
//		
//	}

}
