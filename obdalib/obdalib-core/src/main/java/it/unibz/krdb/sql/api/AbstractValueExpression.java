package it.unibz.krdb.sql.api;

import java.util.ArrayList;

public abstract class AbstractValueExpression implements IValueExpression {
	
	protected ArrayList<ColumnReference> factors = new ArrayList<ColumnReference>();
	
	public void add(String schema, String table, String column) {
		add(new ColumnReference(schema, table, column));
	}
	
	public void add(ColumnReference column) {
		factors.add(column);
	}
	
	public ColumnReference get(int index) {
		return factors.get(index);
	}
	
	public ArrayList<ColumnReference> getAll() {
		return factors;
	}
	
	public abstract void putSpecification(Object obj);
	
	public abstract String toString();
}
