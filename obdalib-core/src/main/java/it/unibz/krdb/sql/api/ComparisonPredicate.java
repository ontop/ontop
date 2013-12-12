package it.unibz.krdb.sql.api;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
