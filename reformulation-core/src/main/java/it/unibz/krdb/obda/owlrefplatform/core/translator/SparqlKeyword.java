/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.translator;

public class SparqlKeyword {
	
	public static final String PREFIX = "PREFIX";
	public static final String SELECT = "SELECT";
	public static final String DISTINCT = "DISTINCT";
	public static final String FROM = "FROM";
	public static final String WHERE = "WHERE";
	public static final String FILTER = "FILTER";
	public static final String UNION = "UNION";
	public static final String OPTIONAL = "OPTIONAL";
	public static final String LIMIT = "LIMIT";
	public static final String OFFSET = "OFFSET";
	public static final String ORDER_BY = "ORDER BY";
	public static final String ASCENDING = "ASC";
	public static final String DESCENDING = "DESC";
	
	// Operator
	public static final String AND = "&&";
	public static final String OR = "||";
	public static final String EQUALS = "=";
	public static final String NOT_EQUALS = "!=";
	public static final String GREATER_THAN = ">";
	public static final String GREATER_THAN_AND_EQUALS = ">=";
	public static final String LESS_THAN = "<";
	public static final String LESS_THAN_AND_EQUALS = "<=";
	public static final String ADD = "+";
	public static final String SUBSTRACT = "-";
	public static final String MULTIPLY = "*";
}
