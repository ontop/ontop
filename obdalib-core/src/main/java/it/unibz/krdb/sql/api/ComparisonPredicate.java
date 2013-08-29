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

/**
 * The ComparisonPredicate class stores the compared terms
 * and the operator.
 */
public class ComparisonPredicate implements Serializable, IPredicate, ICondition {
	
	private static final long serialVersionUID = 4273645296168992941L;
	
	private IValueExpression left;
	private IValueExpression right;
	private Operator operator;
	
	public enum Operator {
		EQ, NE, GT, LT, GE, LE
	};
	
	public ComparisonPredicate(IValueExpression a, IValueExpression b, Operator op) {
		left = a;
		right = b;
		operator = op;
	}
	
	public IValueExpression[] getValueExpressions() {
		IValueExpression[] values = {left, right};
		return values;
	}
	
	public Operator getOperator() {
		return operator;
	}
	
	public boolean useEqualOperator() {
		return (operator == Operator.EQ)? true : false;
	}
	
	private String opString() {
		switch(operator) {
			case EQ: return "=";
			case NE: return "<>"; 
			case GT: return ">"; 
			case LT: return "<"; 
			case GE: return ">="; 
			case LE: return "<="; 
			default: return "";
		}
	}
	
	@Override
	public String toString() {
		String str = left.toString();
		str += opString();
		str += right.toString();				
		return str;
	}
}
