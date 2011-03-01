package org.obda.owlrefplatform.core.abox;

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
	private final Logger log = LoggerFactory.getLogger(SemanticIndexDumper.class);
	private SemanticIndexBuilder indexBuilder;
	
	public SemanticIndexDumper() {
		// TODO: Check if SemanticIndex already present in DB and load it

	}
	
	/**
	 * Drop and create all tables 
	 */
	private void recreateDB() {
		
	}
	
	/**
	 * Construct the SemanticIndex for ontology
	 */
	private void buildSemanticIndex(OWLOntology ontology) {
		indexBuilder = new SemanticIndexBuilder();
	}
	
	/**
	 * Dump the SemanticIndex and the ABox in DB 
	 */
	public void materialize() {
		
	}

}
