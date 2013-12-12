package it.unibz.krdb.sql.api;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class NumericValueExpression extends AbstractValueExpression {

	private static final long serialVersionUID = -8877712308149412381L;
	
	/**
	 * Collection of custom numerical expression.
	 */
	private Queue<Object> cache = new LinkedList<Object>();
	
	@Override
	public void putSpecification(Object obj) {
		cache.add(obj);
		if (obj instanceof ColumnReference) {
			super.add((ColumnReference)obj);
		}
	}
	
	public Queue<Object> getSpecification() {
		return cache;
	}
	
	/**
	 * Retrieves all the {@link ColumnReference} objects that
	 * are used in this expression.
	 */
	public ArrayList<ColumnReference> getColumns() {
		ArrayList<ColumnReference> columns = new ArrayList<ColumnReference>();
		for (Object obj : cache) {
			if (obj instanceof ColumnReference) {
				columns.add((ColumnReference)obj);
			}
		}
		return columns;
	}

	@Override
	public String toString() {
		String str = "(";
		for (Object obj : cache) {
			str += obj.toString();
		}
		str += ")";
			
		return str;
	}
}
