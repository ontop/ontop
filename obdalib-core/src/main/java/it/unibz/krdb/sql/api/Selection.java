/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.sql.api;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Selection implements Serializable{
	
	private static final long serialVersionUID = -8204850346562980466L;
	
	/**
	 * Collection of boolean conditions and boolean operators.
	 */
	private LinkedList<ICondition> conditions;
	
	public Selection() { 
		conditions = new LinkedList<ICondition>();
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
	 * Inserts a IS NULL or IS NOT NULL condition to the list.
	 * 
	 * @param predicate
	 * 			The null predicate.
	 * @throws Exception An exception is thrown if a boolean condition
	 * immediately succeed another boolean condition.
	 */
	public void addCondition(NullPredicate predicate) throws Exception {
		if (!conditions.isEmpty()) {
			Object obj = conditions.peekLast();
			if (obj instanceof ComparisonPredicate) {
				throw new Exception("Illegal conditional expression!");
			}
		}
		conditions.add(predicate);
	}
	
	/**
	 * Inserts a boolean algebra predicate among boolean conditions.
	 * 
	 * @param pred
	 * 			A {@link BooleanAlgebraPredicate} object.
	 * @see {@link AndOperator}, {@link OrOperator}, {@link LeftParenthesis}, {@link RightParenthesis
	 */
	public void addOperator(BooleanAlgebraPredicate pred) throws Exception {
		conditions.add(pred);
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
		for (Object obj : specification) {
			if (obj instanceof ComparisonPredicate) {
				addCondition((ComparisonPredicate) obj);
			}
			else if (obj instanceof NullPredicate) {
				addCondition((NullPredicate) obj);
			}
			else if (obj instanceof BooleanAlgebraPredicate) {
				addOperator((BooleanAlgebraPredicate) obj);
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
		return (ComparisonPredicate) conditions.get(index);
	}
	
	public List<ICondition> getRawConditions() {
		return conditions;
	}
	
	/**
	 * Returns the object inside the condition expression list, 
	 * it can be either a condition or a logical operator (e.g., and, or).
	 * 
	 * @param index
	 * 			The index of the list.
	 */
	public Object getObject(int index) {
		return conditions.get(index);
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
	
	public Selection clone(){
		 Selection newSelection = new Selection();
		 newSelection.conditions = new LinkedList<ICondition>(conditions);
		 return newSelection;
	}
}
