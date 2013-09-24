/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.parser;

import java.util.ArrayList;

import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.ViewDefinition;
import it.unibz.krdb.sql.api.Attribute;
import it.unibz.krdb.sql.api.QueryTree;
import it.unibz.krdb.sql.api.Relation;
import it.unibz.krdb.sql.api.TablePrimary;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
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
		id_counter = 0;		
	}
	
	/*
	 *  Returns all the target SQL from the 
	 *  mappings that could not be parsered by the parser.
	 */
	public ArrayList<ViewDefinition> getViewDefinitions(){
		return this.viewDefinitions;
	}

	public QueryTree contructQueryTree(String query) {
		ANTLRStringStream inputStream = new ANTLRStringStream(query);
		SQL99Lexer lexer = new SQL99Lexer(inputStream);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		SQL99Parser parser = new SQL99Parser(tokenStream);

		QueryTree queryTree = null;
		try {
			queryTree = parser.parse();
		} catch (RecognitionException e) {
			// Does nothing
		}
		
		if (parser.getNumberOfSyntaxErrors() != 0) {
			log.warn("The following query couldn't be parsed. This means Quest will need to use nested subqueries (views) to use this mappings. This is not good for SQL performance, specially in MySQL. Try to simplify your query to allow Quest to parse it. If you think this query is already simple and should be parsed by Quest, please contact the authors. \nQuery: '{}'", query);
			queryTree = createView(query);
		}		
		return queryTree;
	}
	
	private QueryTree createView(String query) {
		String viewName = String.format("view_%s", id_counter++);
		
		ViewDefinition vd = createViewDefintion(viewName, query);
		dbMetaData.add(vd);
		
		QueryTree vt = createViewTree(viewName, query);
		return vt;
	}
		
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
	
	private QueryTree createViewTree(String viewName, String query) {		
		TablePrimary view = new TablePrimary(viewName);
		QueryTree queryTree = new QueryTree(new Relation(view));

		return queryTree;
	}
}
