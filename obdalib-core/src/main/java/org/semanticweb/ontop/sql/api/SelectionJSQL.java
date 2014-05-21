package org.semanticweb.ontop.sql.api;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
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
import java.util.Queue;

import net.sf.jsqlparser.expression.Expression;

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
	 * Inserts an expression to the list. 
	 * We handle in Selection BinaryExpression, IsNUllExpression and INExpression
	 * 
	 * @param obj
	 * 			The binary condition in the form of BinaryExpression.
	 * 
	 */
	public void addCondition(Expression obj){
		
		conditions= obj;
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
			addCondition((Expression)obj);
			
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


}
