package inf.unibz.it.ucq.dig11.swing.table;

import inf.unibz.it.ucq.dig11.DIG12Coupler;

import javax.swing.table.AbstractTableModel;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/*******************************************************************************
 * A table model that can be linked to a DOM Element containing the bindings for
 * a conjunctive query, eql query or any query whose result is a binding
 * response.
 * 
 * This model is not meant to be changed by external entities and it expects the
 * related Bindings XML DOM element not to change either.
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class BindingsTableModel extends AbstractTableModel {
	private Element	dom_binding	= null;
	int columns = -1;
	int rows = -1;

	public BindingsTableModel(Element dom_bindings) {
		this.dom_binding = dom_bindings;
	}

	public int getColumnCount() {
		if (columns != -1)
			return columns;
		Element head = (Element) dom_binding.getElementsByTagName(DIG12Coupler.ASKS_QUERY_HEAD).item(0);
		if (head == null)
			return 0;
		columns = head.getElementsByTagName(DIG12Coupler.ASKS_QUERY_INDVAR).getLength();
		return columns;
	}

	public int getRowCount() {
		if (rows != -1) {
			return rows;
		}
		rows = dom_binding.getElementsByTagName(DIG12Coupler.RESPONSES_BINDINGS_TUPLE).getLength();
		return rows;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
//		Element head = (Element) dom_binding.getElementsByTagName(DIG12Coupler.ASKS_QUERY_HEAD).item(0);
		NodeList tuples = dom_binding.getElementsByTagName(DIG12Coupler.RESPONSES_BINDINGS_TUPLE);
		if ((tuples == null)||(tuples.getLength() < rowIndex))
			return null;
		Element row = null;
		try {
			row = (Element)tuples.item(rowIndex);
		} catch (Exception e) {
			return null;
		}
		Element valueNode = null;
		try {
			NodeList values = row.getElementsByTagName(DIG12Coupler.ASKS_QUERY_INDIVIDUAL);
			valueNode = (Element)values.item(columnIndex);
		} catch (Exception e) {
			return null;
		}
		String value = valueNode.getAttribute(DIG12Coupler.ASKS_QUERY_NAME);
		return value;
	}
}
