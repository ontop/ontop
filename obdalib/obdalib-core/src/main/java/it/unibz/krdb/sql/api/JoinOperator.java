package it.unibz.krdb.sql.api;

import java.util.LinkedList;
import java.util.Queue;

/**
 * An abstract base class for join operator.
 */
public abstract class JoinOperator extends Operator {

	/**
	 * Collection of known conditions and the boolean operators.
	 */
	protected LinkedList<Object> conditions;
	
	public JoinOperator() {
		conditions = new LinkedList<Object>();
	}
	
	/**
	 * Inserts a boolean condition to the list. A condition must not
	 * succeed another boolean condition.
	 * 
	 * @param predicate
	 * 			The boolean condition in the form of comparison
	 * 			predicate.
	 * @throws Exception An exception is thrown if a boolean condition
	 * immediately succeed another boolean condition.
	 */
	public void addCondition(ComparisonPredicate predicate) throws Exception {
		if (!conditions.isEmpty()) {
			Object obj = conditions.peekLast();
			if (obj instanceof ComparisonPredicate) {
				throw new Exception("Illegal conditional expression!");
			}
		}
		conditions.add(predicate);
	}
	
	/**
	 * Inserts a boolean operator among boolean conditions. A boolean
	 * operator must not succeed another boolean operator.
	 * 
	 * @param op
	 * 			A {@link LogicalOperator} object.
	 * @throws Exception An exception is thrown if a boolean operator
	 * immediately succeed another boolean operator.
	 * @see {@link AndOperator}, {@link OrOperator}
	 */
	public void addCondition(LogicalOperator op) throws Exception {
		if (!conditions.isEmpty()) {
			Object obj = conditions.peekLast();
			if (!(obj instanceof LogicalOperator)) {
				conditions.add(op);
			}
		}
		throw new Exception("Illegal conditional expression!");
	}
	
	/**
	 * Copies the input boolean specification into the list.
	 * 
	 * @param specification
	 * 			The collection of conditions and boolean operator.
	 * @throws Exception An exception is thrown if it violates the
	 * rule for adding boolean conditions or boolean operators.
	 */
	public void copy(Queue<Object> specification) throws Exception {
		conditions.clear();  // clear the list.
		for (Object obj : specification) {
			if (obj instanceof ComparisonPredicate) {
				addCondition((ComparisonPredicate)obj);
			}
			else {
				addCondition((LogicalOperator)obj);
			}
		}
	}
	
	/**
	 * Updates the conditions list in this selection. Any existing
	 * conditions are going to be replaced by the new specification.
	 * 
	 * @param specification
	 * 			The new collection of conditions and boolean operator.
	 * @throws Exception An exception is thrown if it violates the
	 * rule for adding boolean conditions or boolean operators.
	 */
	public void update(Queue<Object> specification) throws Exception {
		conditions.clear();
		copy(specification);
	}
	
	/**
	 * Returns the number of boolean conditions.
	 */
	public int conditionSize() {
		return (conditions.size()/2) + 1;
	}
	
	/**
	 * Returns the boolean condition in a specific order.
	 * The initial order starts at 0 index.
	 * 
	 * @param index
	 * 			The specific order.
	 */
	public ComparisonPredicate getCondition(int index) {
		if (index > 0) {
			index = index + 1;
		}
		return (ComparisonPredicate)conditions.get(index);
	}
	
	/**
	 * Returns the boolean operator in a specific order.
	 * The initial order starts at 0 index.
	 * 
	 * @param index
	 * 			The specific order.
	 */
	public String getLogicalOperator(int index) {
		if (index == 0) {
			index = 1;
		}
		index = index + 2;
		return (String)conditions.get(index);
	}
	
	public abstract String toString();
}
