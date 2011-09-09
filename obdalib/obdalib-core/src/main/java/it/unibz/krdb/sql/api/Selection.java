package it.unibz.krdb.sql.api;

import java.util.LinkedList;
import java.util.Queue;

public class Selection {
	
	private LinkedList<Object> conditions;
	
	public Selection() { 
		conditions = new LinkedList<Object>();
	}
		
	public void add(ComparisonPredicate predicate) {
		if (!conditions.isEmpty()) {
			Object obj = conditions.peekLast();
			if (obj instanceof ComparisonPredicate) {
				return; // Should throw an error.
			}
		}
		conditions.add(predicate);
	}
	
	public void addOperator(LogicalOperator op) {
		if (!conditions.isEmpty()) {
			Object obj = conditions.peekLast();
			if (!(obj instanceof LogicalOperator)) {
				conditions.add(op);
			}
		}
		return; // Should throw an error.
	}
	
	public void copy(Queue<Object> specification) {
		for (Object obj : specification) {
			if (obj instanceof ComparisonPredicate) {
				add((ComparisonPredicate)obj);
			}
			else {
				addOperator((LogicalOperator)obj);
			}
		}
	}
	
	public void update(Queue<Object> specification) {
		conditions.clear();
		copy(specification);
	}
	
	public int conditionSize() {
		return (conditions.size()/2) + 1;
	}
	
	public ComparisonPredicate getPredicate(int index) {
		if (index > 0) {
			index = index + 1;
		}
		return (ComparisonPredicate)conditions.get(index);
	}
	
	public String getLogicalOperator(int index) {
		if (index == 0) {
			index = 1;
		}
		index = index + 2;
		return (String)conditions.get(index);
	}
	
	@Override
	public String toString() {
		String str = "where";
		
		for (Object obj : conditions) {
			str += " ";
			str += obj.toString();
		}
		return str;
	}
}