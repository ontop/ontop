package it.unibz.krdb.obda.owlrefplatform.core;

import it.unibz.krdb.obda.model.Statement;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DLLiterOntology;

/**
 * The technique wrapper interface can be used to implement different 
 * combinations of rewriting unfolding and evaluation techniques
 * 
 * @author Manfred Gerstgrasser
 *
 */

public interface TechniqueWrapper {

	/**
	 * Returns a answer statement for the given query
	 * @param query the query
	 * @return the answer statement 
	 * @throws Exception
	 */
	public Statement getStatement() throws Exception;
//	public void updateOntology(DLLiterOntology onto, Set<URI> uris);
//	public void updateDataSource(DataSource ds);
	
	public void dispose();
	
	public void loadDependencies(DLLiterOntology onto);
	

}
