package org.obda.owlrefplatform.core.abox;

import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owl.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dump ABox using SemanticIndex approach
 * 
 * @author Sergejs Pugacs
 * 
 */
public class SemanticIndexDumper {
	private final Logger log = LoggerFactory
			.getLogger(SemanticIndexDumper.class);

	public final static String uri_table = "URI";
	public final static String literal_table = "LITERALS";
	public final static String index_table = "IDX";

	/**
	 * Drop and create all tables
	 */
	private static void recreateDB() {

	}

	/**
	 * Dump the SemanticIndex and the ABox in DB
	 */
	public static void materialize(Set<OWLOntology> ontologies, Connection conn) {
		DAG dag = new DAG(ontologies);
		List<String> stmt = build_insert(dag, ontologies);
		execute_insert(stmt, conn);
	}

	/**
	 * Generate insert statements to populate the database with ABox
	 */
	public static List<String> build_insert(DAG dag, Set<OWLOntology> ontologies) {
		List<String> result = new LinkedList<String>();

		return result;
	}

	/**
	 * Execute insert statments
	 */
	public static void execute_insert(List<String> stmt, Connection conn) {

	}

}
