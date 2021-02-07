package it.unibz.inf.ontop.protege.gui.models;

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
import java.util.ArrayList;
import java.util.List;

public class ResultSetTableModel implements TableModel {

	private final List<List<String>> results = new ArrayList<>();
	private final List<String> columns = new ArrayList<>();

	/**
	 * This constructor creates a TableModel from a ResultSet. It is package
	 * private because it is only intended to be used by
	 * ResultSetTableModelFactory, which is what you should use to obtain a
	 * ResultSetTableModel
	 */
	public ResultSetTableModel(ResultSet set) throws SQLException {
		ResultSetMetaData metadata = set.getMetaData();
		int numcols = metadata.getColumnCount();
		for (int i = 1; i <= numcols; i++) {
			columns.add(metadata.getColumnLabel(i));
		}
		while (set.next()) {
			List<String> row = new ArrayList<>();
			for(int i = 1; i <= numcols; i++) {
				Object ob = set.getObject(i);
				String s = (ob == null) ? "null" : ob.toString();
				row.add(s);
			}
			results.add(row);
		}
	}

	// These two TableModel methods return the size of the table
	@Override
	public int getColumnCount() {
		return columns.size();
	}

	@Override
	public int getRowCount() {
		return results.size();
	}

	// This TableModel method returns columns names from the ResultSetMetaData
	@Override
	public String getColumnName(int column) {
		return columns.get(column);
	}

	// This TableModel method specifies the data type for each column.
	// We could map SQL types to Java types, but for this example, we'll just
	// convert all the returned data to strings.
	@Override
	public Class<?> getColumnClass(int column) {
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
		List<String> aux = results.get(row);
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
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
	}
}
