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

import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.QuotedID;
import it.unibz.krdb.sql.QuotedIDFactory;
import it.unibz.krdb.sql.QuotedIDFactoryStandardSQL;
import it.unibz.krdb.sql.ViewDefinition;
import it.unibz.krdb.sql.api.ParsedSQLQuery;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLQueryParser {

	public static final String QUERY_NOT_SUPPORTED = "Query not yet supported";

	private final DBMetadata dbMetaData;
	
	private static int id_counter;
	
	private static Logger log = LoggerFactory.getLogger(SQLQueryParser.class);
	
	// ONLY FOR DEEP PARSING
	public SQLQueryParser(DBMetadata dbMetaData) {
 		this.dbMetaData = dbMetaData;
	}
		
	// ONLY FOR SHALLOW PARSING
	public SQLQueryParser() {
		dbMetaData = null;
	}
	
	/**
	 * Called from ParsedMapping. Returns the query, even if there were 
	 * parsing errors.
	 * 
	 * @param query The sql query to be parsed
	 * @return A VisitedQuery (possible with null values)
	 */
	public ParsedSQLQuery parseShallowly(String query) {
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
    private ParsedSQLQuery parse(String query, boolean deeply) {
		boolean errors = false;
		ParsedSQLQuery queryParser = null;
		
		try {
			queryParser = new ParsedSQLQuery(query, deeply);
		} 
		catch (JSQLParserException e) {
			if (e.getCause() instanceof ParseException)
				log.warn("Parse exception, check no SQL reserved keywords have been used "+ e.getCause().getMessage());
			errors = true;
		}
		
		if (queryParser == null || (errors && deeply)) {
			log.warn("The following query couldn't be parsed. " +
					"This means Quest will need to use nested subqueries (views) to use this mappings. " +
					"This is not good for SQL performance, specially in MySQL. " + 
					"Try to simplify your query to allow Quest to parse it. " + 
					"If you think this query is already simple and should be parsed by Quest, " +
					"please contact the authors. \nQuery: '{}'", query);
			
			String viewName = String.format("view_%s", id_counter++);
			
			// ONLY IN DEEP PARSING
			if (dbMetaData != null) {
				ViewDefinition vd = createViewDefinition(viewName, query, dbMetaData.getQuotedIDFactory());
				dbMetaData.add(vd);
			}
			
			queryParser = createViewParsed(viewName, query);	
		}
		return queryParser;
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
		
		// create SELECT *
		List<SelectItem> list = new ArrayList<>(1);
		list.add(new AllColumns());
		body.setSelectItems(list); 
		
		// create FROM viewTable
		Table viewTable = new Table(null, viewName);
		body.setFromItem(viewTable);
		
		Select select = new Select();
		select.setSelectBody(body);
		
		ParsedSQLQuery queryParsed = null;
		try {
			queryParsed = new ParsedSQLQuery(select, false);
		} 
		catch (JSQLParserException e) {
			if(e.getCause() instanceof ParseException)
				log.warn("Parse exception, check no SQL reserved keywords have been used "+ e.getCause().getMessage());
		}

		return queryParsed;
	}
	
	
	private ViewDefinition createViewDefinition(String viewName, String query, QuotedIDFactory idfac) {

        ParsedSQLQuery queryParser = null;
        boolean supported = true;
        try {
            queryParser = new ParsedSQLQuery(query, false);
        } 
        catch (JSQLParserException e) {
            supported = false;
        }

        ViewDefinition viewDefinition = new ViewDefinition(idfac.createRelationFromString(viewName), query);
       
        if (supported) {
            List<String> columns = queryParser.getColumns();
            for (String columnName : columns) {
            	QuotedID columnId = idfac.createFromString(columnName);
 
                viewDefinition.addAttribute(columnId, 0, null, false);
            }
        }
        else {
            int start = "select".length(); 
            int end = query.toLowerCase().indexOf("from");
            if (end == -1) 
                throw new RuntimeException("Error parsing SQL query: Couldn't find FROM clause");
         

            String projection = query.substring(start, end).trim();

            //split where comma is present but not inside parenthesis
		    String[] columns = projection.split(",+(?!.*\\))");
//            String[] columns = projection.split(",+(?![^\\(]*\\))");


            for (String col : columns) {
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
                
    			// Get only the short name if the column name uses qualified name.
    			// Example: table.column -> column
                if (columnName.contains(".")) {
                    columnName = columnName.substring(columnName.lastIndexOf(".") + 1); // get only the name
                }

                QuotedID columnId = idfac.createFromString(columnName);

                viewDefinition.addAttribute(columnId, 0, null, false); 
            }
        }
        
        return viewDefinition;
	}
}
