package it.unibz.krdb.sql.api;

import java.util.LinkedList;
import java.util.Queue;

public class BooleanValueExpression extends AbstractValueExpression {
	
	private static final long serialVersionUID = 8604408531111471739L;
	
	/**
	 * Collection of custom boolean expressions.
	 */
	private Queue<Object> cache = new LinkedList<Object>();
	
	@Override
	public void putSpecification(Object obj) {
		cache.add(obj);
	}
	
	public Queue<Object> getSpecification() {
		return cache;
	}	

	@Override
	public String toString() {
		String str = "";
		for (Object obj : cache) {
			str += obj.toString();
			str += " ";
		}
		return str;
	}
}
