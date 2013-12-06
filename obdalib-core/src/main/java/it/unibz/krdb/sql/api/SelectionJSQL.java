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
import java.util.Queue;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;

public class SelectionJSQL implements Serializable{
	
	private static final long serialVersionUID = -8204850346562980466L;
	
	/**
	 * Collection of expressions.
	 */
	private Expression conditions;
	
	public SelectionJSQL() { 
		conditions=null;
	}
	
	/**
	 * Inserts a binary expression to the list. 
	 * 
	 * @param predicate
	 * 			The binary condition in the form of BinaryExpression.
	 * 
	 */
	public void addCondition(BinaryExpression predicate){
		
		conditions= predicate;
	}
	
	/**
	 * Inserts a IS NULL or IS NOT NULL condition to the list.
	 * 
	 * @param predicate
	 * 			The null predicate.
	 * 
	 */
	public void addCondition(IsNullExpression predicate) {
		
		conditions =predicate;
	}
	
	public void addCondition(InExpression predicate){
		
		conditions = predicate;
	}
	
	/**
	 * Copies the input boolean specification into the list.
	 * 
	 * @param specification
	 * 			The collection of conditions and boolean operator.
	 *
	 */
	public void copy(Queue<Object> specification)  {
		for (Object obj : specification) {
			if (obj instanceof BinaryExpression) {
				addCondition((BinaryExpression) obj);
			}
			else if (obj instanceof IsNullExpression) {
				addCondition((IsNullExpression) obj);
			}
			
		}
	}
	
	public Expression getRawConditions() {
		return conditions;
	}
	
	
	@Override
	public String toString() {
		String str = "where";
			
			str += " ";
			str += conditions.toString();
		
		return str;
	}

	public void addCondition(AnyComparisonExpression predicate) {
		
		AnyComparison comparison = new AnyComparison(predicate.getSubSelect());
		conditions = comparison;
		
	}

	public void addCondition(AllComparisonExpression predicate) {
		
		AllComparison comparison = new AllComparison(predicate.getSubSelect());
		conditions =comparison;
		
	}
}
