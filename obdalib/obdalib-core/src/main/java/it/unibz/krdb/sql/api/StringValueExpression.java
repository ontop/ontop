package it.unibz.krdb.sql.api;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class StringValueExpression extends AbstractValueExpression {

	public static final String CONCAT_OP = "||";
	
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
			if (obj instanceof String) {
				if (!((String)obj).equals(CONCAT_OP)) {
					str += "'" + obj.toString() + "'"; // to delimit the string
				}
			}
			str += obj.toString();
		}
		str += ")";
			
		return str;
	}
}
