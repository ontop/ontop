package it.unibz.krdb.obda.owlapi3;

import it.unibz.krdb.obda.io.PrefixManager;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLPropertyAssertionObject;

public class OWLResultSetTableModel implements TableModel {

	private OWLResultSet results;
	private int numcols;
	private int numrows;
	private int fetchSizeLimit;
	private boolean isHideUri;
	private boolean isFetchAll;

	// Tabular data for exporting result
	private Vector<String[]> tabularData;
	// Vector data for presenting result to table GUI
	private Vector<String[]> resultsTable;
	
	private Vector<TableModelListener> listener;

	private PrefixManager prefixman;

	private final int INITIAL_FETCH_SIZE = 100;
	private final int NEXT_FETCH_SIZE = 100;

	/**
	 * This constructor creates a TableModel from a ResultSet. It is package
	 * private because it is only intended to be used by
	 * ResultSetTableModelFactory, which is what you should use to obtain a
	 * ResultSetTableModel
	 */
	public OWLResultSetTableModel(OWLResultSet results, PrefixManager prefixman, 
			boolean hideUri, boolean fetchAll, int fetchSizeLimit) throws OWLException {
		this.results = results;
		this.prefixman = prefixman;
		this.isHideUri = hideUri;
		this.isFetchAll = fetchAll;
		this.fetchSizeLimit = fetchSizeLimit;

		numcols = results.getColumCount();
		numrows = 0;
		
		resultsTable = new Vector<String[]>();
		listener = new Vector<TableModelListener>();
		
		int fetchSize = fetchSizeLimit;
		if (needFetchMore()) {
			fetchSize = INITIAL_FETCH_SIZE;
		}
		fetchRows(fetchSize);
	}
	
	private void fetchRows(int size) throws OWLException {
		if (results == null) {
			return;
		}
		if (size != 0) {
			int counter = 0;
			while (results.nextRow()) {
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
				resultsTable.add(crow);
				counter++;
				updateRowCount();
				
				// Determine if the loop should stop now
				if (counter == size) {
					break;
				}
			}
		}
	}

	private void updateRowCount() {
		numrows++;
	}

	/**
	 * Fetch all the tuples returned by the result set.
	 */
	public List<String[]> getTabularData() throws OWLException {
		if (tabularData == null) {
			tabularData = new Vector<String[]>();
			String[] columnName = results.getSignature().toArray(new String[numcols]);		
			// Append the column names
			tabularData.add(columnName);
			// Append first the already fetched tuples
			tabularData.addAll(resultsTable); 
			// Append the rest
			while (results.nextRow()) {
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
		try {
			results.close();
		} catch (OWLException e) {
			// NO-OP
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
		if (needFetchMore()) {
			checkNextRowAvailability(row);
		}
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

	/**
	 * Determine if the table need to fetch more tuples.
	 */
	public boolean needFetchMore() {
		return isFetchingAll() || fetchSizeLimit > INITIAL_FETCH_SIZE;
	}
	
	private boolean isFetchingAll() {
		return isFetchAll;
	}
	
	private void checkNextRowAvailability(int currentRowNumber) {
		try {
			int nextRowNumber = currentRowNumber + getRowCount() / 4;
			if (nextRowNumber >= getRowCount()) {
				if (isFetchingAll()) {
					fetchRows(NEXT_FETCH_SIZE);
				} else {
					int remainder = fetchSizeLimit - getRowCount();
					int c = remainder / NEXT_FETCH_SIZE;
					if (c != 0) {
						fetchRows(NEXT_FETCH_SIZE);
					} else {
						fetchRows(remainder);
					}
				}				
				fireModelChangedEvent();
			}
		} catch (OWLException e) {
			// NO-OP
		}
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
		Iterator<TableModelListener> it = listener.iterator();
		while (it.hasNext()) {
			it.next().tableChanged(new TableModelEvent(this));
		}
	}
}
