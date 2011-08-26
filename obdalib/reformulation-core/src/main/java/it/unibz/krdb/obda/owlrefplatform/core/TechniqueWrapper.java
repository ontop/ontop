package it.unibz.krdb.obda.owlrefplatform.core;

import it.unibz.krdb.obda.model.OBDAStatement;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DLLiterOntology;

/**
 * The technique wrapper interface can be used to implement different
 * combinations of rewriting unfolding and evaluation techniques
 * 
 */

public interface TechniqueWrapper {

	/**
	 * Returns a answer statement for the given query
	 * 
	 * @param query
	 *            the query
	 * @return the answer statement
	 * @throws Exception
	 */
	public OBDAStatement getStatement() throws Exception;

	public void dispose();

	public void loadDependencies(DLLiterOntology onto);

}
