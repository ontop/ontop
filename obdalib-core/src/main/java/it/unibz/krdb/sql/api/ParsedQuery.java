/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.sql.api;

import it.unibz.krdb.obda.parser.AggregationJSQL;
import it.unibz.krdb.obda.parser.AggregationVisitor;
import it.unibz.krdb.obda.parser.AliasMapVisitor;
import it.unibz.krdb.obda.parser.JoinConditionVisitor;
import it.unibz.krdb.obda.parser.ProjectionVisitor;
import it.unibz.krdb.obda.parser.SelectionVisitor;
import it.unibz.krdb.obda.parser.TablesNameVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;



/**
 * A  structure to store the parsed SQL query string. 
 * It returns the information about the query using the visitor classes
 */
public class ParsedQuery {

	private static final long serialVersionUID = -4590590361733833782L;

	private String query; 
	private Statement stm; //the parsed query
	 
	private Select select;
	
	private RelationJSQL view;
	
	
	/**
	 * Constructs an empty query.
	 */
	public ParsedQuery() {
		super();
	}

	
	/**
	 * Parse a query given as a String
	 * @param queryString the SQL query to parse
	 * @throws JSQLParserException 
	 */
	
	public ParsedQuery(String queryString) throws JSQLParserException {
		query = queryString;
	 
	
			stm = CCJSqlParserUtil.parse(query);
			if (stm instanceof Select) {
				select = (Select)stm;
				
			}

		
	}
	
	public ParsedQuery(RelationJSQL value) {
		
		view = value;
		
	}

	
	
	public String toString() {
		return select.toString(); 
	}
	
	/**
	 * Returns all the tables in this query tree.
	 */
	public ArrayList<RelationJSQL> getTableSet() {
		TablesNameVisitor tnp = new TablesNameVisitor();
		return tnp.getTableList(select);
	}
	
	/**
	 * Get the string construction of alias name. 
	 */
	public HashMap<String, String> getAliasMap() {
		AliasMapVisitor aliasV = new AliasMapVisitor();
		return aliasV.getAliasMap(select);
	}

	/**
	 * Get the string construction of the join condition. The string has the
	 * format of "VAR1=VAR2".
	 */
	public ArrayList<String> getJoinCondition() {
		JoinConditionVisitor joinCV = new JoinConditionVisitor();
		return joinCV.getJoinConditions(select);
	}

	/**
	 * Get the object construction for the WHERE clause.
	 */
	public SelectionJSQL getSelection() {
		SelectionVisitor sel= new SelectionVisitor();
		return sel.getSelection(select);
	}
	
	/**
	 * Get the object construction for the SELECT clause.
	 */
	public ProjectionJSQL getProjection() {
		ProjectionVisitor proj = new ProjectionVisitor();
		return proj.getProjection(select);
	}
	

	
	/**
	 * Constructs the GROUP BY statement based on the Aggregation
	 * object.
	 */
	public AggregationJSQL getGroupByClause() {
		AggregationVisitor agg = new AggregationVisitor();
		
			AggregationJSQL groupByClause;
			groupByClause = agg.getAggregation(select);
			
		
		return groupByClause;
	}
	
}
