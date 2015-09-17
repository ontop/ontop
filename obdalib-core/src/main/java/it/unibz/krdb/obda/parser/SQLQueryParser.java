package it.unibz.krdb.obda.parser;

/*
 * #%L
 * ontop-obdalib-core
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

import it.unibz.krdb.sql.Attribute;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.ViewDefinition;
import it.unibz.krdb.sql.api.ParsedSQLQuery;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLQueryParser {

	private DBMetadata dbMetaData;
	private Connection connection;
	private String database;
	
	//This field will contain all the target SQL from the 
	//mappings that could not be parsed by the parser.
//	private ArrayList<ViewDefinition> viewDefinitions;
	
	private static int id_counter;
	
	private static Logger log = LoggerFactory.getLogger(SQLQueryParser.class);
	
	public SQLQueryParser(DBMetadata dbMetaData) {
		connection=null;
		database=dbMetaData.getDriverName();
 		this.dbMetaData = dbMetaData;
		id_counter = 0;		
	}
	
	/*
	 * This constructor is used when the tables names and schemas are taken from the mappings
	 * in MappingParser
	 */
	
	public SQLQueryParser(Connection conn) throws SQLException {
		connection=conn;
		database=connection.getMetaData().getDriverName();
//		this.viewDefinitions = new ArrayList<ViewDefinition>();
		this.dbMetaData = null;
		id_counter = 0;		
	}
	
	public SQLQueryParser() {
		database=null;
		connection=null;
//		this.viewDefinitions = new ArrayList<ViewDefinition>();
		this.dbMetaData = null;
		id_counter = 0;		
	}
	
	/*
	 *  Returns all the target SQL from the 
	 *  mappings that could not be parsed by the parser.
	 */
//	public ArrayList<ViewDefinition> getViewDefinitions(){
//		return this.viewDefinitions;
//	}

	/**
	 * Called from ParsedMapping. Returns the query, even if there were 
	 * parsing errors.
	 * 
	 * @param query The sql query to be parsed
	 * @return A VisitedQuery (possible with null values)
	 */
	public ParsedSQLQuery parseShallowly(String query){
		return parse(query, false);
	}
	

	/**
	 * Called from MappingAnalyzer:createLookupTable. Returns the parsed query, or, if there are
	 * syntax error, the name of a generated view, even if there were 
	 * parsing errors. 
	 * 
	 * @param query The sql query to be parsed
	 * @return A ParsedQuery (or a SELECT * FROM table with the generated view)
	 */
	public ParsedSQLQuery parseDeeply(String query) {
		return parse(query, true);
	}


    /**
     * @param query
     * @param deeply <ul>
     *               <li>
     *               <p/>
     *               If true, (i) deepParsing columns, (ii) create a view if an error is generated,
     *               and (iii) store information about the different part of the query
     *               </li>
     *               <li>
     *               Otherwise the query is simply parsed and we do not check for unsupported
     *               </li>
     *               </ul>
     * @return
     */
    private ParsedSQLQuery parse(String query, boolean deeply){
		boolean errors=false;
		ParsedSQLQuery queryParser = null;
		
		
		try {
		/*
		 * 
		 */
			queryParser = new ParsedSQLQuery(query,deeply);
			
		} catch (JSQLParserException e) 
		{
			if(e.getCause() instanceof ParseException)
				log.warn("Parse exception, check no SQL reserved keywords have been used "+ e.getCause().getMessage());
			errors=true;
			
		}
		
		if (queryParser == null || (errors && deeply) )
		{
			log.warn("The following query couldn't be parsed. This means Quest will need to use nested subqueries (views) to use this mappings. This is not good for SQL performance, specially in MySQL. Try to simplify your query to allow Quest to parse it. If you think this query is already simple and should be parsed by Quest, please contact the authors. \nQuery: '{}'", query);
			queryParser = createView(query);
		}
		return queryParser;
		
		
	}

	private ParsedSQLQuery createView(String query){
		
		String viewName = String.format("view_%s", id_counter++);
		
		if(database!=null){
		ViewDefinition vd = createViewDefinition(viewName, query);
		
		if(dbMetaData != null)
			dbMetaData.add(vd);
//		else
//			viewDefinitions.add(vd);
		}
		
		ParsedSQLQuery vt = createViewParsed(viewName, query);
		return vt;
	}
	
	
	/*
	 * To create a view, I start building a new select statement and add the viewName information in a table in the FROM item expression
	 * We create a query that looks like SELECT * FROM viewName
	 */
	private ParsedSQLQuery createViewParsed(String viewName, String query) {
		
		/*
		 * Create a new SELECT statement containing the viewTable in the FROM clause
		 */
		
		PlainSelect body = new PlainSelect();
		
		//create SELECT *
		ArrayList<SelectItem> list = new ArrayList<SelectItem>();
		list.add(new AllColumns());
		body.setSelectItems(list); 
		
		// create FROM viewTable
		Table viewTable = new Table(null, viewName);
		body.setFromItem(viewTable);
		
		Select select= new Select();
		select.setSelectBody(body);
		
		ParsedSQLQuery queryParsed = null;
		try {
			queryParsed = new ParsedSQLQuery(select,false);
			
		} catch (JSQLParserException e) {
			if(e.getCause() instanceof ParseException)
				log.warn("Parse exception, check no SQL reserved keywords have been used "+ e.getCause().getMessage());
		}

		return queryParsed;
	}
	
	
	private ViewDefinition createViewDefinition(String viewName, String query) {

        ParsedSQLQuery queryParser = null;
        boolean supported = true;
        boolean uppercase = false;

        //TODO: we could use the sql adapter, but it's in the package reformulation-core
        if (database.contains("Oracle") || database.contains("DB2") || database.contains("H2") || database.contains("HSQL")) {
            // If the database engine is Oracle, H2 or DB2 unquoted columns are changed in uppercase
            uppercase = true;
        }

        try {
            queryParser = new ParsedSQLQuery(query,false);
        } 
        catch (JSQLParserException e) {
            supported = false;
        }

        ViewDefinition viewDefinition = new ViewDefinition(viewName, query);
       
        if (supported) {
            List<String> columns = queryParser.getColumns();
            for (String columnName : columns) {
                if (!ParsedSQLQuery.pQuotes.matcher(columnName).matches()) { //if it is not quoted, change it in uppercase when needed

                    if (uppercase)
                        columnName = columnName.toUpperCase();
                    else
                      columnName = columnName.toLowerCase();
                }
				else { // if quoted remove the quotes
					columnName= columnName.substring(1, columnName.length()-1);
				}

                viewDefinition.addAttribute(new Attribute(viewDefinition, columnName, 0, false, null));
            }
        }
        else {
            int start = 6; // the keyword 'select'
			boolean quoted;

            int end = query.toLowerCase().indexOf("from");

            if (end == -1) {
                throw new RuntimeException("Error parsing SQL query: Couldn't find FROM clause");
            }

            String projection = query.substring(start, end).trim();

            //split where comma is present but not inside parenthesis
		    String[] columns = projection.split(",+(?!.*\\))");
//            String[] columns = projection.split(",+(?![^\\(]*\\))");


            for (String col : columns) {
                quoted = false;
                String columnName = col.trim();
			
    			/*
    			 * Take the alias name if the column name has it.
    			 */
                final String[] aliasSplitters = new String[] { " as ",  " AS " };

                for (String aliasSplitter : aliasSplitters) {
                    if (columnName.contains(aliasSplitter)) { // has an alias
                        columnName = columnName.split(aliasSplitter)[1].trim();
                        break;
                    }
                }
                ////split where space is present but not inside single quotes
                if (columnName.contains(" "))
                    columnName = columnName.split("\\s+(?![^'\"]*')")[1].trim();
                
    			/*
    			 * Remove any identifier quotes
    			 * Example:
    			 * 		INPUT: "table"."column"
    			 * 		OUTPUT: table.column
    			 */
                Pattern pattern = Pattern.compile("[\"`\\[].*[\"`\\]]");
                Matcher matcher = pattern.matcher(columnName);
                if (matcher.find()) {
                    columnName = columnName.replaceAll("[\\[\\]\"`]", "");
                    quoted = true;
                }
			

    			/*
    			 * Get only the short name if the column name uses qualified name.
    			 * Example:
    			 * 		INPUT: table.column
    			 * 		OUTPUT: column
    			 */
                
                if (columnName.contains(".")) {
                    columnName = columnName.substring(columnName.lastIndexOf(".") + 1, columnName.length()); // get only the name
                }

                if (!quoted) {
                    if (uppercase)
                        columnName = columnName.toUpperCase();
                    else
                        columnName = columnName.toLowerCase();
                }

                // the attribute index always start at 1
                viewDefinition.addAttribute(new Attribute(viewDefinition, columnName, 0, false, null)); 
            }
        }
        
        return viewDefinition;
	}

}
