/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
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
