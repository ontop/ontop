package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

/*
 * #%L
 * ontop-reformulation-core
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

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;

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

	public static boolean isAskQuery(ParsedQuery query) {
		return (query instanceof ParsedBooleanQuery);
	}
	
	public static boolean isSelectQuery(ParsedQuery query) {
		return (query instanceof ParsedTupleQuery);
	}
	
	public static boolean isConstructQuery(ParsedQuery query) {
		return (query instanceof ParsedGraphQuery) && query.getSourceString().toLowerCase().contains(CONSTRUCT_KEYWORD);
	}
	
	public static boolean isDescribeQuery(ParsedQuery query) {
		return (query instanceof ParsedGraphQuery) && query.getSourceString().toLowerCase().contains(DESCRIBE_KEYWORD);
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
		int describeIdx = strquery.toLowerCase().indexOf("describe");
		String uri = "";
		try{
		org.openrdf.query.parser.sparql.SPARQLParser parser = new SPARQLParser();
			ParsedQuery q = parser.parseQuery(strquery, "http://example.org");
			TupleExpr expr = q.getTupleExpr();
			String sign = expr.toString();
			//ValueConstant (value=http://example.org/db2/neoplasm/1)
			if (sign.contains("ValueConstant")) {
				int idx = sign.indexOf("ValueConstant");
				int first = sign.indexOf('=', idx) +1;
				int last = sign.indexOf(')', first);
				uri = sign.substring(first, last);
			}
		} catch (MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (uri.isEmpty()) {
			int firstIdx = strquery.indexOf('<', describeIdx);
			int lastIdx = strquery.indexOf('>', describeIdx);
			uri = strquery.substring(firstIdx+1, lastIdx);
		}
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
	
	public static String getSelectObjQuery(String constant) {
		return "SELECT * WHERE { ?s ?p <" + constant + "> }";
}

	public static String getSelectSubjQuery(String constant) {
		return "SELECT * WHERE { <" + constant + "> ?p ?o}";
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
