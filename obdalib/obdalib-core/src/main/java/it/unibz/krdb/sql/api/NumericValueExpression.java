package it.unibz.krdb.sql.api;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class NumericValueExpression extends AbstractValueExpression {

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
