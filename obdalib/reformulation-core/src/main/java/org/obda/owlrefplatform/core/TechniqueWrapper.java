package org.obda.owlrefplatform.core;

import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.queryanswering.Statement;

import java.net.URI;
import java.util.Set;

import org.obda.owlrefplatform.core.ontology.DLLiterOntology;

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
	public Statement getStatement(String query) throws Exception;
	public void updateOntology(DLLiterOntology onto, Set<URI> uris);
	public void updateDataSource(DataSource ds);

}
