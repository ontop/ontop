package it.unibz.inf.ontop.protege.gui.treemodels;

/*
 * #%L
 * ontop-protege
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

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class ResultSetTableModel implements TableModel {


	ResultSet set; // The ResultSet to interpret

	ResultSetMetaData metadata; // Additional information about the results

	int numcols; // How many rows and columns in the table

	Vector<Vector<String>> results = null;

	Vector<TableModelListener> listener = null;


	/**
	 * This constructor creates a TableModel from a ResultSet. It is package
	 * private because it is only intended to be used by
	 * ResultSetTableModelFactory, which is what you should use to obtain a
	 * ResultSetTableModel
	 */
	public ResultSetTableModel(ResultSet set) throws SQLException {
		this.set = set; // Save the results
		metadata = set.getMetaData(); // Get metadata on them
		numcols = metadata.getColumnCount(); // How many columns?
		listener = new Vector<TableModelListener>();

		results = new Vector<Vector<String>>();
		int i=1;
		while(set.next()){
			Vector<String> aux = new Vector<String>();
			for(int j=1;j<=numcols;j++){
				String s = "";
				Object ob = set.getObject(j);
				if(ob == null){
					s = "null";
				}else{
					s = ob.toString();
				}
				aux.add(s);
			}
			results.add(aux);
			i++;
		}

	}

	/**
	 * Call this when done with the table model. It closes the ResultSet and the
	 * Statement object used to create it.
	 */
	public void close() {
		try {
			Statement statement = set.getStatement();
			if (statement!=null && !statement.isClosed())
				statement.close();
			// Normally not necessary (according to the JDBC standard)
			if (set!=null && !set.isClosed())
				set.close();
		} catch (SQLException e) {
			// NO-OP
		}

	}

	/** Automatically close when we're garbage collected */
	@Override
	protected void finalize() {
		close();
	}

	// These two TableModel methods return the size of the table
	@Override
	public int getColumnCount() {
		return numcols;
	}

	@Override
	public int getRowCount() {
		return results.size();
	}

	// This TableModel method returns columns names from the ResultSetMetaData
	@Override
	public String getColumnName(int column) {
		try {
			return metadata.getColumnLabel(column + 1);
		} catch (SQLException e) {
			e.printStackTrace(System.err);
			return e.toString();
		}
	}

	// This TableModel method specifies the data type for each column.
	// We could map SQL types to Java types, but for this example, we'll just
	// convert all the returned data to strings.
	@Override
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
	@Override
	public Object getValueAt(int row, int column) {
		Vector<String> aux = results.get(row);
		return aux.get(column);
	}

	// Our table isn't editable
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	// Since its not editable, we don't need to implement these methods
	@Override
	public void setValueAt(Object value, int row, int column) {
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
		listener.add(l);
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		listener.remove(l);
	}

	
}
