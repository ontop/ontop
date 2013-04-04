package it.unibz.krdb.sql.api;

import java.util.LinkedList;
import java.util.Queue;

/**
 * An abstract base class for join operator.
 */
public class JoinOperator extends Operator {

	private static final long serialVersionUID = 144042107662518288L;
	
	public static final int JOIN = 0;
	public static final int INNER_JOIN = 1;
	public static final int LEFT_JOIN = 2;
	public static final int RIGHT_JOIN = 3;
	public static final int FULL_JOIN = 4;
	public static final int LEFT_OUTER_JOIN = 5;
	public static final int RIGHT_OUTER_JOIN = 6;
	public static final int FULL_OUTER_JOIN = 7;
	public static final int CROSS_JOIN = 8;
	
	private int joinType = JoinOperator.JOIN; // by default

	/**
	 * Collection of known conditions and the boolean operators.
	 */
	private LinkedList<Object> conditions = new LinkedList<Object>();
	
	public JoinOperator() {
		// Does nothing.
	}
	
	public JoinOperator(int joinType) {
		setType(joinType);
	}
	
	public void setType(int type) {
		joinType = type;
	}
	
	public int getType() {
		return joinType;
	}
	
	public String getName() {
		switch(joinType) {
			case JOIN: return "join";
			case INNER_JOIN: return "inner join";
			case LEFT_JOIN: return "left join";
			case RIGHT_JOIN: return "right join";
			case FULL_JOIN: return "full join";
			case LEFT_OUTER_JOIN: return "left outer join";
			case RIGHT_OUTER_JOIN: return "right outer join";
			case FULL_OUTER_JOIN: return "full outer join";
			case CROSS_JOIN: return ", ";
			default: return "join";
		}
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
			if (obj instanceof LogicalOperator) {
				throw new Exception("Illegal conditional expression!");
			}
		}
		conditions.add(op);
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
		index = index * 2;
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
		index = (index * 2) + 1;
		return (String)conditions.get(index);
	}
	
	@Override
	public String toString() {
		String str = "%s " + getName() + " %s";
		
		str += " ";
		str += (joinType != CROSS_JOIN) ? "on" : "";

		for (Object obj : conditions) {
			str += " ";
			str += obj.toString();
		}		
		return str;
	}
}
