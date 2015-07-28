package org.semanticweb.ontop.utils;

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


import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.parser.SQLQueryParser;

import org.semanticweb.ontop.sql.DBMetadata;
import org.semanticweb.ontop.sql.ViewDefinition;
import org.semanticweb.ontop.sql.api.ParsedSQLQuery;
import org.semanticweb.ontop.sql.api.RelationJSQL;

import net.sf.jsqlparser.JSQLParserException;



/**
 * This class is in charge of parsing the mappings and obtaining the list of schema names 
 * and tables.Afterwards from this list we obtain the metadata (not in this class).
 * @author Dag
 *
 */
public class MappingParser {
	
	private List<OBDAMappingAxiom> mappingList;
	private SQLQueryParser sqlQueryParser;
	private List<ParsedMapping> parsedMappings;
	private List<RelationJSQL> realTables; // Tables that are not view definitions
	
	public MappingParser(Connection conn, ArrayList<OBDAMappingAxiom> mappingAxioms) throws SQLException{
		this.mappingList = mappingAxioms;
		this.sqlQueryParser = new SQLQueryParser(conn);
		this.parsedMappings = this.parseMappings();
	}
	


	/**
	 * Called by Quest when fetching non-full metadata
	 * Only metadata for the tables in this list is extracted by Quest
	 * 
	 * @return The tables (same as getTables)
	 * @throws JSQLParserException 
	 */
	public List<RelationJSQL> getRealTables() throws JSQLParserException{
		if(this.realTables == null){
			List<RelationJSQL> _realTables = this.getTables();
//			List<RelationJSQL> removeThese = new ArrayList<>();
//			for(ViewDefinition vd : sqlQueryParser.getViewDefinitions()){
//				for(RelationJSQL rel : _realTables){
//					if(rel.getFullName().equals(vd.getName()))
//						removeThese.add(rel);
//				}
//			}
//			for(RelationJSQL remRel : removeThese){
//				_realTables.remove(remRel);
//			}
			this.realTables = _realTables;
		}
		return this.realTables;
	}
	
	/**
	 * Returns the list of parsed mapping objects.
	 * "Parsed" only means that sql part is parsed
	 * Called by Quest.setuprepository
	 * 
	 * @return
	 */
	public List<ParsedMapping> getParsedMappings(){
		return parsedMappings;
	}
	
	public List<RelationJSQL> getTables() throws JSQLParserException{
		List<RelationJSQL> tables = new ArrayList<>();
		for(ParsedMapping pm : parsedMappings){
			ParsedSQLQuery query = pm.getSourceQueryParsed();
			List<RelationJSQL> queryTables = query.getTables();
			for(RelationJSQL table : queryTables){
				if (!(tables.contains(table))){
					tables.add(table);
				}
			}
		}
		return tables;
	}
	
	
	
	/**
	 * 	Parses the mappingList (Actually, only the source sql is parsed.)
	 * This is necessary to separate the parsing, such that this can be done before the
	 * table schema extraction
	 * 
	 * @return List of parsed mappings
	 */
	private ArrayList<ParsedMapping> parseMappings() {
		LinkedList<String> errorMessage = new LinkedList<String>();
		ArrayList<ParsedMapping> parsedMappings = new ArrayList<ParsedMapping>();
		for (OBDAMappingAxiom axiom : this.mappingList) {
			try {
				ParsedMapping parsed = new ParsedMapping(axiom, sqlQueryParser);
				parsedMappings.add(parsed);
			} catch (Exception e) {
				errorMessage.add("Error in mapping with id: " + axiom.getId() + " \n Description: "
						+ e.getMessage() + " \nMapping: [" + axiom.toString() + "]");
				
			}
		}
		if (errorMessage.size() > 0) {
			StringBuilder errors = new StringBuilder();
			for (String error: errorMessage) {
				errors.append(error + "\n");
			}
			final String msg = "There was an error parsing the following mappings. Please correct the issue(s) to continue.\n" + errors.toString();
			RuntimeException r = new RuntimeException(msg);
			throw r;
		}
		return parsedMappings;
				
	}
	

}
