/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.parser;

import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.ViewDefinition;
import it.unibz.krdb.sql.api.Attribute;
import it.unibz.krdb.sql.api.VisitedQuery;
import it.unibz.krdb.sql.api.QueryTree;
import it.unibz.krdb.sql.api.Relation;
import it.unibz.krdb.sql.api.RelationJSQL;
import it.unibz.krdb.sql.api.TablePrimary;

import java.util.ArrayList;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.openrdf.query.parser.QueryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLQueryTranslator {

	private DBMetadata dbMetaData;
	
	//This field will contain all the target SQL from the 
	//mappings that could not be parsered by the parser.
	private ArrayList<ViewDefinition> viewDefinitions;
	
	private static int id_counter;
	
	private static Logger log = LoggerFactory.getLogger(SQLQueryTranslator.class);
	
	public SQLQueryTranslator(DBMetadata dbMetaData) {
		this.dbMetaData = dbMetaData;
		id_counter = 0;		
	}
	
	/*
	 * This constructor is used when the tables names and schemas are taken from the mappings
	 */
	public SQLQueryTranslator() {
		this.viewDefinitions = new ArrayList<ViewDefinition>();
		this.dbMetaData = null;
		id_counter = 0;		
	}
	
	/*
	 *  Returns all the target SQL from the 
	 *  mappings that could not be parsered by the parser.
	 */
	public ArrayList<ViewDefinition> getViewDefinitions(){
		return this.viewDefinitions;
	}

	/**
	 * Called from ParsedMapping. Returns the query tree, even if there were 
	 * parsing errors. This is because the ParsedMapping only need the table names,
	 * and it needs them, especially in the cases like "select *", that are treated like parsing
	 * errors, but are treated by preprocessProjection
	 * 
	 * @param query The sql query to be parsed
	 * @return A QueryTree (possible with null values and errors)
	 */
	public VisitedQuery constructParserNoView(String query){
		return constructParser(query, false);
	}
//	public QueryTree constructQueryTreeNoView(String query){
//		return constructQueryTree(query, false);
//	}
	


	/**
	 * Called from MappingAnalyzer:createLookupTable. Returns the parsed query, or, if there are
	 * syntax error, the name of a generated view, even if there were 
	 * parsing errors. 
	 * 
	 * @param query The sql query to be parsed
	 * @return A ParsedQuery (possible just the name of a generated view)
	 */
	public VisitedQuery constructParser(String query) {
		return constructParser(query, true);
	}
	
//	public QueryTree constructQueryTree(String query) {
//		return constructQueryTree(query, true);
//	}
	
	
	private VisitedQuery constructParser (String query, boolean generateViews){
		boolean errors=false;
		VisitedQuery queryParser = null;
		try {
			queryParser = new VisitedQuery(query);
			
		
			
		} catch (JSQLParserException e) {
			if(e.getCause() instanceof ParseException)
				log.warn("Parse exception, it is not possible to use any SQL reserved keywords "+ e.getCause().getMessage());
			errors=true;
			log.warn("The following query couldn't be parsed. This means Quest will need to use nested subqueries (views) to use this mappings. This is not good for SQL performance, specially in MySQL. Try to simplify your query to allow Quest to parse it. If you think this query is already simple and should be parsed by Quest, please contact the authors. \nQuery: '{}'", query);
		}
		
		if (queryParser == null || (errors && generateViews) )
			queryParser = createView(query);
		
		return queryParser;
		
		
	}
		
//	private QueryTree constructQueryTree(String query, boolean generateViews) {
//		ANTLRStringStream inputStream = new ANTLRStringStream(query);
//		SQL99Lexer lexer = new SQL99Lexer(inputStream);
//		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
//		SQL99Parser parser = new SQL99Parser(tokenStream);
//
//		QueryTree queryTree = null;
//		try {
//			queryTree = parser.parse();
//		} catch (RecognitionException e) {
//			// Does nothing
//		}
//		
//		if (queryTree == null || (parser.getNumberOfSyntaxErrors() != 0 && generateViews)) {
//			log.warn("The following query couldn't be parsed. This means Quest will need to use nested subqueries (views) to use this mappings. This is not good for SQL performance, specially in MySQL. Try to simplify your query to allow Quest to parse it. If you think this query is already simple and should be parsed by Quest, please contact the authors. \nQuery: '{}'", query);
//			queryTree = createView(query);
//		}		
//		return queryTree;
//	}
	
	private VisitedQuery createView(String query){
		
		String viewName = String.format("view_%s", id_counter++);
		
		ViewDefinition vd = createViewDefintion(viewName, query);
		
		if(dbMetaData != null)
			dbMetaData.add(vd);
		else
			viewDefinitions.add(vd);
		
		VisitedQuery vt = createViewParsed(viewName, query);
		return vt;
	}
	
//	private QueryTree createView(String query) {
//		String viewName = String.format("view_%s", id_counter++);
//		
//		ViewDefinition vd = createViewDefintion(viewName, query);
//		
//		if(dbMetaData != null)
//			dbMetaData.add(vd);
//		else
//			viewDefinitions.add(vd);
//		
//		QueryTree vt = createViewTree(viewName, query);
//		return vt;
//	}
		
	private ViewDefinition createViewDefintion(String viewName, String query) {
		int start = 6; // the keyword 'select'
		int end = query.toLowerCase().indexOf("from");		
		
		if (end == -1) {
			throw new RuntimeException("Error parsing SQL query: Couldn't find FROM clause");
		}
		String projection = query.substring(start, end).trim();
		String[] columns = projection.split(",");
		
		ViewDefinition viewDefinition = new ViewDefinition();
		viewDefinition.setName(viewName);
		viewDefinition.copy(query);		
		for (int i = 0; i < columns.length; i++) {
			String columnName = columns[i].trim();
			
			/*
			 * Remove any identifier quotes
			 * Example:
			 * 		INPUT: "table"."column"
			 * 		OUTPUT: table.column
			 */
			if (columnName.contains("\"")) {
				columnName = columnName.replaceAll("\"", "");
			} else if (columnName.contains("`")) {
				columnName = columnName.replaceAll("`", "");
			} else if (columnName.contains("[") && columnName.contains("]")) {
				columnName = columnName.replaceAll("[", "").replaceAll("]", "");
			}

			/*
			 * Get only the short name if the column name uses qualified name.
			 * Example:
			 * 		INPUT: table.column
			 * 		OUTPUT: column
			 */
			if (columnName.contains(".")) {
				columnName = columnName.substring(columnName.lastIndexOf(".")+1, columnName.length()); // get only the name
			}
			
			/*
			 * Take the alias name if the column name has it.
			 */
			if (columnName.contains(" as ")) { // has an alias
				columnName = columnName.split(" as ")[1].trim();
			}			
			viewDefinition.setAttribute(i+1, new Attribute(columnName)); // the attribute index always start at 1
		}
		return viewDefinition;
	}
	
	/*
	 * To create a view, I start building a new select statement and add the viewName information in a table in the FROMitem expression
	 * We create a query that looks like SELECT * FROM viewName
	 */
	private VisitedQuery createViewParsed(String viewName, String query) {		
		Table view = new Table(null, viewName);
		PlainSelect body = new PlainSelect();
		body.setFromItem(view);
		ArrayList<SelectItem> list = new ArrayList<SelectItem>();
		list.add(new AllColumns());
		body.setSelectItems(list);

		Select select= new Select();
		select.setSelectBody(body);
		VisitedQuery queryParsed = null;
		try {
			queryParsed = new VisitedQuery(select);
		} catch (JSQLParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return queryParsed;
	}
	
//	private QueryTree createViewTree(String viewName, String query) {		
//		TablePrimary view = new TablePrimary("", viewName, viewName);
//		QueryTree queryTree = new QueryTree(new Relation(view));
//
//		return queryTree;
//	}
}
