/***
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
package inf.unibz.it.ucq.swing;

import inf.unibz.it.ucq.domain.QueryResult;
import inf.unibz.it.ucq.exception.QueryResultException;

import java.util.HashSet;
import java.util.Vector;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.obda.query.domain.Constant;
import org.obda.query.domain.imp.TermFactoryImpl;
import org.obda.query.domain.imp.ValueConstantImpl;

import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.datatype.xsd.XSDatatype;

/**
 * This class takes a JDBC ResultSet object and implements the TableModel
 * interface in terms of it so that a Swing JTable component can display the
 * contents of the ResultSet. Note that it requires a scrollable JDBC 2.0
 * ResultSet. Also note that it provides read-only access to the results
 */
public class QueryResultTableModel implements TableModel {
	QueryResult		results;				// The ResultSet to interpret

	// ResultSetMetaData metadata; // Additional information about the results

	int				numcols, numrows;		// How many rows and columns in the
											// table

	Vector<Constant[]> 	resultsTable	= null;
	HashSet<String> mergeSet = null;

	private final TermFactoryImpl termFactory = TermFactoryImpl.getInstance();

	/**
	 * This constructor creates a TableModel from a ResultSet. It is package
	 * private because it is only intended to be used by
	 * ResultSetTableModelFactory, which is what you should use to obtain a
	 * ResultSetTableModel
	 */
	public QueryResultTableModel(QueryResult results) throws QueryResultException {
		this.results = results; // Save the results
		// metadata = results.getMetaData(); // Get metadata on them
		numcols = results.getColumnCount();
//		numrows = results.getRowsCount();
		resultsTable= new Vector<Constant[]>();
		mergeSet = new HashSet<String>();

		// Filling up a temporal result holder (remove this by adding
		// browsability to resultset)
		int i = 0;
		while (results.nextRow()) {
			Constant[] crow = new ValueConstantImpl[numcols];
			for (int j = 0; j < numcols; j++) {
				XSDatatype stringType = StringType.theInstance;
				crow[j] = termFactory.createValueConstant(results.getConstantFromColumn(j).toString(), stringType);
			}
			resultsTable.add(crow);
			i += 1;

		}
		numrows = resultsTable.size();

	}

	/**
	 * Call this when done with the table model. It closes the ResultSet and the
	 * Statement object used to create it.
	 */
	public void close() {
		try {
			results.close();
		} catch (QueryResultException e) {
			throw new RuntimeException(e);
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
			if(column < signature.size()){
				return results.getSignature().get(column);
			}else{
				return "";
			}
		} catch (QueryResultException e) {
			e.printStackTrace();
			return "NULL";
		}catch (Exception e) {
			e.printStackTrace();
			return "NULL";
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
//		try {
//			Constant c = results.getConstantFromColumn(column); // Go to the
//																// specified row
//			return c.toString(); // Convert it to a string
			return resultsTable.get(row)[column].toString();
//		} catch (QueryResultException e) {
//			e.printStackTrace(System.err);
//			return e.toString();
//		}
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

	private boolean add(Constant crow[]){

		String row ="";
		for(int i=0; i< crow.length; i++){
			row = row + crow[i].toString();
		}
		return mergeSet.add(row);
	}
}
