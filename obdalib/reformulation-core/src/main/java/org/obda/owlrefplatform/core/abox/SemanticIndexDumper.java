package org.obda.owlrefplatform.core.abox;

import org.semanticweb.owl.model.OWLOntology;

/**
 * Dump ABox using SemanticIndex approach 
 * 
 * @author Sergejs Pugacs
 *
 */
public class SemanticIndexDumper {
	
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
		
	}
	
	/**
	 * Dump the SemanticIndex and the ABox in DB 
	 */
	public void materialize() {
		
	}

}
