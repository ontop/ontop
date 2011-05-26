package inf.unibz.it.obda.gui.swing.table;

import inf.unibz.it.obda.exception.QueryResultException;
import inf.unibz.it.obda.model.Constant;
import inf.unibz.it.obda.queryanswering.QueryResultSet;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class IncrementalQueryResultSetTableModel implements TableModel{

	QueryResultSet		results;
	int		numcols, numrows, fetchsize;

	Vector<String[]> 	resultsTable	= null;
	HashSet<String> mergeSet = null;
	private Vector<TableModelListener> listener = null;

	boolean isAfterLast = false;

	/**
	 * This constructor creates a TableModel from a ResultSet. It is package
	 * private because it is only intended to be used by
	 * ResultSetTableModelFactory, which is what you should use to obtain a
	 * ResultSetTableModel
	 */
	public IncrementalQueryResultSetTableModel(QueryResultSet results) throws QueryResultException {
		try {
			this.results = results;
			numcols = results.getColumCount();
			fetchsize = results.getFetchSize();
			if(fetchsize == 0){//means it is disabled
				fetchsize = 100;
			}
			resultsTable= new Vector<String[]>();
			listener = new Vector<TableModelListener>();
			mergeSet = new HashSet<String>();
			int i = 0;
			while (i < fetchsize && !isAfterLast) {

				if(results.nextRow()){

					String[] crow = new String[numcols];
					for (int j = 0; j < numcols; j++) {
						crow[j] = results.getAsString(j+1);
					}
					resultsTable.add(crow);
					i += 1;
				}
				else {
					isAfterLast = true;
				}

			}
			numrows = i;
		} catch (SQLException e) {
			throw new QueryResultException(e);
		}


	}

	/**
	 * Call this when done with the table model. It closes the ResultSet and the
	 * Statement object used to create it.
	 */
	public void close() {

			try {
				results.close();
			} catch (SQLException e) {
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
			if(signature != null && column < signature.size()){
				return results.getSignature().get(column);
			}else{
				return "";
			}
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

			try {
				if(row + 5 > numrows && !isAfterLast){
					fetchMoreTuples();
					fireModelChangedEvent();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

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
		listener.add(l);
	}

	public void removeTableModelListener(TableModelListener l) {
		listener.remove(l);
	}

	private boolean add(Constant crow[]){

		String row ="";
		for(int i=0; i< crow.length; i++){
			row = row + crow[i].toString();
		}
		return mergeSet.add(row);
	}

	private void fetchMoreTuples() throws QueryResultException{
		try {
			int i = 0;
			while (i < fetchsize && !isAfterLast) {

				if(results.nextRow()){
					String[] crow = new String[numcols];
					for (int j = 0; j < numcols; j++) {
						crow[j] = results.getAsString(j+1);
					}
					resultsTable.add(crow);
					i += 1;
				}
				else {
					isAfterLast = true;
				}

			}
			numrows = numrows+ i;
		} catch (SQLException e) {
			throw new QueryResultException(e);
		}
	}

	private void fireModelChangedEvent(){

		Iterator<TableModelListener> it = listener.iterator();
		while(it.hasNext()){
			it.next().tableChanged(new TableModelEvent(this));
		}
	}

}
