package it.unibz.krdb.obda.utils;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.parser.SQL99Lexer;
import it.unibz.krdb.obda.parser.SQL99Parser;
import it.unibz.krdb.obda.parser.SQLQueryTranslator;
import it.unibz.krdb.sql.api.QueryTree;

/**
 * Contains the target query and query tree (parsed source part, sql) of a mapping
 * 
 * This is in a separate class, such that the parsing can be done before metadata extraction,
 * but independently of mapping analysis.
 * 
 * @author Dag Hovland
 *
 */
public class ParsedMapping {

	QueryTree sourceQueryTree;
	OBDAMappingAxiom axiom;
	
	public ParsedMapping(OBDAMappingAxiom axiom, SQLQueryTranslator translator){
		this.axiom = axiom;
		OBDASQLQuery sourceQuery = (OBDASQLQuery) axiom.getSourceQuery();

		// Construct the SQL query tree from the source query
		QueryTree queryTree = translator.contructQueryTree(sourceQuery.toString());
		this.sourceQueryTree = queryTree;
	}
	
	/**
	 * This returns the querytree constructed from the source query
	 * @return
	 */
	public QueryTree getSourceQueryTree(){
		return this.sourceQueryTree;
	}
	
	
	
	/**
	 * This returns the source query
	 * @return
	 */
	public OBDAQuery getSourceQuery(){
		return axiom.getSourceQuery();
	}
	
	/**
	 * This returns the same target query as in the original axiom / mapping
	 * @return
	 */
	public CQIE getTargetQuery(){
		return (CQIE) axiom.getTargetQuery();
	}
	
	/**
	 * 	This is the same as axiom.getId() on the mapping that was parsed
	 * to create this object
	 * @return
	 */
	public String getId(){
		return axiom.getId();
	}
	
	@Override
	public String toString(){
		return axiom.toString();
	}
	

}
