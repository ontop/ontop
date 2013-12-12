package it.unibz.krdb.obda.utils;

import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.parser.SQLQueryTranslator;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.ViewDefinition;
import it.unibz.krdb.sql.api.VisitedQuery;
import it.unibz.krdb.sql.api.QueryTree;
import it.unibz.krdb.sql.api.Relation;
import it.unibz.krdb.sql.api.RelationJSQL;

import java.util.ArrayList;
import java.util.LinkedList;



/**
 * This class is in charge of parsing the mappings and obtaining the list of schema names 
 * and tables.Afterwards from this list we obtain the metadata (not in this class).
 * @author Dag
 *
 */
public class MappingParser {
	
	private ArrayList<OBDAMappingAxiom> mappingList;
	private SQLQueryTranslator translator;
	private ArrayList<ParsedMapping> parsedMappings;
	private ArrayList<RelationJSQL> realTables; // Tables that are not view definitions
	
	public MappingParser(ArrayList<OBDAMappingAxiom> mappingList){
		this.mappingList = mappingList;
		this.translator = new SQLQueryTranslator();
		this.parsedMappings = this.parseMappings();
	}
	

	/**
	 * Called by getOracleMetaData
	 * 
	 * @return The tables (same as getTables) but without those that are created by the sqltranslator as view definitions
	 */
	public ArrayList<RelationJSQL> getRealTables(){
		if(this.realTables == null){
			ArrayList<RelationJSQL> _realTables = this.getTables();
			ArrayList<RelationJSQL> removeThese = new ArrayList<RelationJSQL>();
			for(ViewDefinition vd : translator.getViewDefinitions()){
				for(RelationJSQL rel : _realTables){
					if(rel.getName().equals(vd.getName()))
						removeThese.add(rel);
				}
			}
			for(RelationJSQL remRel : removeThese){
				_realTables.remove(remRel);
			}
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
	public ArrayList<ParsedMapping> getParsedMappings(){
		return parsedMappings;
	}
	
	public ArrayList<RelationJSQL> getTables(){
		ArrayList<RelationJSQL> tables = new ArrayList<RelationJSQL>();
		for(ParsedMapping pm : parsedMappings){
			VisitedQuery query = pm.getSourceQueryParsed();
			ArrayList<RelationJSQL> queryTables = query.getTableSet();
			for(RelationJSQL table : queryTables){
				if (!(tables.contains(table))){
					tables.add(table);
				}
			}
		}
		return tables;
	}
	
	
	
	
	/**
	 * Adds the view definitions created by the SQLQueryTranslator during parsing to the metadata
	 * 
	 * This must be separated out, since the parsing must be done before metadata extraction
	 */
	public void addViewDefs(DBMetadata metadata){
		for (ViewDefinition vd : translator.getViewDefinitions()){
			metadata.add(vd);
		}
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
				ParsedMapping parsed = new ParsedMapping(axiom, translator);
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
