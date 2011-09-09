package it.unibz.krdb.sql.api;

import java.util.LinkedList;
import java.util.Queue;

public class BooleanValueExpression extends AbstractValueExpression {

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
