package it.unibz.krdb.obda.owlapi3;

import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.model.Constant;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLPropertyAssertionObject;

public class OWLResultSetTableModel implements TableModel {

	OWLResultSet results;
	int numcols, numrows, fetchsize;

	Vector<String[]> resultsTable = null;
	HashSet<String> mergeSet = null;
	private Vector<TableModelListener> listener = null;

	PrefixManager prefixman = null;

	boolean isAfterLast = false;

	boolean useshortform;

	/**
	 * This constructor creates a TableModel from a ResultSet. It is package
	 * private because it is only intended to be used by
	 * ResultSetTableModelFactory, which is what you should use to obtain a
	 * ResultSetTableModel
	 */
	public OWLResultSetTableModel(OWLResultSet results,
			PrefixManager prefixman, boolean useshortform) throws OWLException {
		this.prefixman = prefixman;
		this.useshortform = useshortform;

		this.results = results;
		numcols = results.getColumCount();
		fetchsize = results.getFetchSize();
		if (fetchsize == 0) {// means it is disabled
			fetchsize = 100;
		}
		resultsTable = new Vector<String[]>();
		listener = new Vector<TableModelListener>();
		mergeSet = new HashSet<String>();
		int i = 0;
		while (i < fetchsize && !isAfterLast) {

			if (results.nextRow()) {

				String[] crow = new String[numcols];
				for (int j = 0; j < numcols; j++) {
					OWLPropertyAssertionObject constant = results
							.getOWLPropertyAssertionObject(j + 1);
					if (constant != null)
						crow[j] = constant.toString();
					else
						crow[j] = "";

				}
				resultsTable.add(crow);
				i += 1;
			} else {
				isAfterLast = true;
			}

		}
		numrows = i;

	}

	public List<String[]> getTabularData() {
		List<String[]> tabularData = new ArrayList<String[]>();
		try {
			String[] columnName = results.getSignature().toArray(
					new String[results.getSignature().size()]);
			tabularData.add(columnName);
			tabularData.addAll(resultsTable);
		} catch (OWLException e) {
			e.printStackTrace();
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
		// try {
		// Constant c = results.getConstantFromColumn(column); // Go to the
		// // specified row
		// return c.toString(); // Convert it to a string

		try {
			if (row + 5 > numrows && !isAfterLast) {
				fetchMoreTuples();
				fireModelChangedEvent();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		String value = resultsTable.get(row)[column];
		if (value == null)
			return "";
		else if (useshortform) {
			return prefixman.getShortForm(value);
		} else {
			return value;
		}

		// } catch (QueryResultException e) {
		// e.printStackTrace(System.err);
		// return e.toString();
		// }
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

	private boolean add(Constant crow[]) {

		String row = "";
		for (int i = 0; i < crow.length; i++) {
			row = row + crow[i].toString();
		}
		return mergeSet.add(row);
	}

	private void fetchMoreTuples() throws OWLException {

		int i = 0;
		while (i < fetchsize && !isAfterLast) {

			if (results.nextRow()) {
				String[] crow = new String[numcols];
				for (int j = 0; j < numcols; j++) {
					OWLPropertyAssertionObject constant = results
							.getOWLPropertyAssertionObject(j + 1);
					if (constant != null)
						crow[j] = constant.toString();
					else
						crow[j] = null;
				}
				resultsTable.add(crow);
				i += 1;
			} else {
				isAfterLast = true;
			}

		}
		numrows = numrows + i;

	}

	private void fireModelChangedEvent() {

		Iterator<TableModelListener> it = listener.iterator();
		while (it.hasNext()) {
			it.next().tableChanged(new TableModelEvent(this));
		}
	}

}
