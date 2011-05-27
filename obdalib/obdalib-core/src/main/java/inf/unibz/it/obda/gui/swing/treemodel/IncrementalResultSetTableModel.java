package inf.unibz.it.obda.gui.swing.treemodel;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class IncrementalResultSetTableModel implements TableModel {

	
	ResultSet set; // The ResultSet to interpret

	ResultSetMetaData metadata; // Additional information about the results

	int numcols, numrows, fetchsize; // How many rows and columns in the table
	
	Vector<Vector<String>> results = null; 
	
	Vector<TableModelListener> listener = null;
	
	boolean isAfterLast = false;

	/**
	 * This constructor creates a TableModel from a ResultSet. It is package
	 * private because it is only intended to be used by
	 * ResultSetTableModelFactory, which is what you should use to obtain a
	 * ResultSetTableModel
	 */
	public IncrementalResultSetTableModel(ResultSet set) throws SQLException {
		this.set = set; // Save the results
		metadata = set.getMetaData(); // Get metadata on them
		numcols = metadata.getColumnCount(); // How many columns?
		listener = new Vector<TableModelListener>();
		numrows = 100;
		fetchsize = 100;
		
		results = new Vector<Vector<String>>();
		int i=1;
		while( i<= fetchsize && set.next()){
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
		numrows = i-1;
		if(numrows < fetchsize){
			isAfterLast = true;
		}
	}

	/**
	 * Call this when done with the table model. It closes the ResultSet and the
	 * Statement object used to create it.
	 */
	public void close() {
//		try {
//			set.getStatement().close();
//			set.close();
//		} catch (SQLException e) {
//			e.printStackTrace(System.err);
//		}
	}

	/** Automatically close when we're garbage collected */
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
			return metadata.getColumnLabel(column + 1);
		} catch (SQLException e) {
			e.printStackTrace(System.err);
			return e.toString();
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
		
		
		try {
			if(row + 2 >= numrows && !isAfterLast){
				fetchMoreResults();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Vector<String> aux = results.get(row);
		return aux.get(column);
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
	
	private void fetchMoreResults() throws Exception{
		int i=1;
		while( i<= fetchsize && !isAfterLast){
			
			if(set.next()){
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
			}else{
				isAfterLast = true;
			}
		}
		numrows = numrows+ i-1;
		fireTableChanged();
	}
	
	private void fireTableChanged(){
		Iterator<TableModelListener> it = listener.iterator();
		while(it.hasNext()){
			TableModelListener l = it.next();
			l.tableChanged(new TableModelEvent(this));
		}
	}
	
}
