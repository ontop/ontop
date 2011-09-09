package it.unibz.krdb.sql.api;

import java.util.LinkedList;
import java.util.Queue;

public abstract class JoinOperator extends Operator {

	protected LinkedList<Object> conditions = new LinkedList<Object>();
	
	public JoinOperator() {
		// Does nothing
	}
	
	public void addCondition(ComparisonPredicate predicate) {
		if (!conditions.isEmpty()) {
			Object obj = conditions.peekLast();
			if (obj instanceof ComparisonPredicate) {
				return; // Should throw an error.
			}
		}
		conditions.add(predicate);
	}
	
	public void addCondition(LogicalOperator op) {
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
				addCondition((ComparisonPredicate)obj);
			}
			else {
				addCondition((LogicalOperator)obj);
			}
		}
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
	
	public abstract String toString();
}
