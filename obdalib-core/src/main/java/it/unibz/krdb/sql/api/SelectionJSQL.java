package it.unibz.krdb.sql.api;

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
