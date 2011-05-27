package it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration;

import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.owlrefplatform.core.viewmanager.ViewManager;

import java.util.List;


/**
 * A general interface which should be use to implement new
 * source query generation which can than be integrated in to a technique 
 * wrapper
 * 
 * @author Manfred Gerstgrasser
 *
 */

public interface SourceQueryGenerator {

	/**
	 * Translates the given datalog program into a source query, which
	 * can later be evaluated by a evaluation engine.
	 * 
	 * @param query the datalog program
	 * @return the souce query
	 * @throws Exception
	 */
	public String generateSourceQuery(DatalogProgram query, List<String> signature) throws Exception;
	/**
	 * Updates the current view manager with the new given parameters
	 * @param man the new prefix manager
	 * @param onto the new dlliter ontology
	 * @param uris the set of URIs of the ontologies integrated into the dlliter ontology
	 */
//	public void update(PrefixManager man, DLLiterOntology onto, Set<URI> uris);
	
	public ViewManager getViewManager();
}