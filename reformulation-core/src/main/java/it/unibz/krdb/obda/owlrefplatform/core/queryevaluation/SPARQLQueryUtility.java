/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

public class SPARQLQueryUtility {
	
	private String query;
	
	private static final String ASK_KEYWORD = "ask";
	private static final String SELECT_KEYWORD = "select";
	private static final String CONSTRUCT_KEYWORD = "construct";
	private static final String DESCRIBE_KEYWORD = "describe";
	
	public SPARQLQueryUtility(String query) {
		this.query = query;
	}
	
	public SPARQLQueryUtility() {
	}
	
	public String getQueryString() {
		return query;
	}
	
	public boolean isAskQuery() {
		return query.toLowerCase().contains(ASK_KEYWORD);
	}
	
	public boolean isSelectQuery() {
		return query.toLowerCase().contains(SELECT_KEYWORD);
	}
	
	public boolean isConstructQuery() {
		return query.toLowerCase().contains(CONSTRUCT_KEYWORD);
	}
	
	public boolean isDescribeQuery() {
		return query.toLowerCase().contains(DESCRIBE_KEYWORD);
	}
	
	public static boolean isAskQuery(String query) {
		return query.toLowerCase().contains(ASK_KEYWORD);
	}
	
	public static boolean isSelectQuery(String query) {
		return query.toLowerCase().contains(SELECT_KEYWORD);
	}
	
	public static boolean isConstructQuery(String query) {
		return query.toLowerCase().contains(CONSTRUCT_KEYWORD);
	}
	
	public static boolean isDescribeQuery(String query) {
		return query.toLowerCase().contains(DESCRIBE_KEYWORD);
	}

	public static boolean isVarDescribe(String strquery) {
		if (strquery.contains("where"))
		{
			if (strquery.indexOf('?') < strquery.indexOf("where"))
				return true;
		}
		else
		{
			if (strquery.contains("?"))
				return true;
		}
		return false;
	}

	public static String getDescribeURI(String strquery) {
		int firstIdx = strquery.indexOf('<');
		int lastIdx = strquery.indexOf('>');
		String uri = strquery.substring(firstIdx+1, lastIdx);
		return uri;
	}

	public static boolean isURIDescribe(String strquery) {
		if (strquery.contains("where"))
		{
			if (strquery.indexOf('<') < strquery.indexOf("where"))
				return true;
		}
		else
		{
			if (strquery.contains("<") && strquery.contains(">"))
				return true;
		}
		return false;
	 }

	public static String getSelectVarDescribe(String strquery) {
		
		String strlower = strquery.toLowerCase();
		if (strlower.contains("describe"))
		{
			StringBuilder bf = new StringBuilder();
			
			int idx1 = strlower.indexOf("describe");
			int idx2 = idx1 + 8;
			
			if (idx1 > 0)
				bf.append(strquery.substring(0, idx1));
			bf.append(" SELECT DISTINCT ");
			bf.append(strquery.substring(idx2));
				
			strquery = bf.toString();
		}
		return strquery;
	}

	public static String getConstructObjQuery(String constant) {
			return "CONSTRUCT { ?s ?p <" + constant
					+ "> } WHERE { ?s ?p <" + constant + "> }";
	}

	public static String getConstructSubjQuery(String constant) {
		return "CONSTRUCT { <" + constant + "> ?p ?o} WHERE { <"
				+ constant + "> ?p ?o}";
	}

	public static String getSelectFromConstruct(String strquery){
		String strlower = strquery.toLowerCase();
		// Lets assume it IS Construct query and we dont need to check
			StringBuilder bf = new StringBuilder();
			int idx_con = strlower.indexOf("construct");
			int idx_where = strlower.indexOf("where");
			bf.append(strquery.substring(0, idx_con));
			bf.append(" SELECT * ");
			bf.append(strquery.substring(idx_where));
			strquery = bf.toString();
		return strquery;
	}
}
