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
public class VisitedQuery {

	private static final long serialVersionUID = -4590590361733833782L;

	private String query; 
	private Statement stm;
	 
	private Select select; //the parsed query
	
	private RelationJSQL view;
	
	
	/**
	 * Constructs an empty query.
	 */
	public VisitedQuery() {
		super();
	}

	
	/**
	 * Parse a query given as a String
	 * @param queryString the SQL query to parse
	 * @throws JSQLParserException 
	 */
	
	public VisitedQuery(String queryString) throws JSQLParserException {
		query = queryString;
	 
	
			stm = CCJSqlParserUtil.parse(query);
			if (stm instanceof Select) {
				select = (Select)stm;
				
			}
			//catch exception about wrong inserted columns
			else 
				throw new JSQLParserException("The inserted query is not a SELECT statement");

		
	}
	
	public VisitedQuery(Statement statement) throws JSQLParserException{
		
		query = statement.toString();
		 
		
		stm = CCJSqlParserUtil.parse(query);
		if (stm instanceof Select) {
			select = (Select)stm;
			
		}
		//catch exception about wrong inserted columns
		else 
			throw new JSQLParserException("The inserted query is not a SELECT statement");
//		if (statement instanceof Select) {
//			Select newStatement = new Select();
//			newStatement.setSelectBody(((Select) statement).getSelectBody());
//			select = newStatement;
//			stm= newStatement;
//			query= newStatement.toString();
//			
//		}
//		else 
//			throw new JSQLParserException("The inserted query is not a SELECT statement");
	}
	
//	public ParsedQuery(RelationJSQL value) {
//		
//		view = value;
//		
//	}

	@Override
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
	 * Set the object construction for the SELECT clause, 
	 * modifying the current statement
	 * @param projection
	 */
	
	public void setProjection(ProjectionJSQL projection) {
		ProjectionVisitor proj = new ProjectionVisitor();
		proj.setProjection(select, projection);
	}
	
	/**
	 * Set the object construction for the WHERE clause, 
	 * modifying the current statement
	 * @param selection
	 */
	
	public void setSelection(SelectionJSQL selection) {
		SelectionVisitor sel = new SelectionVisitor();
		sel.setSelection(select, selection);
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
	
	public Statement getStatement(){
		return select;
	}
	
	public void setStatement(Select statement){
		select= statement;
	}
	
}
