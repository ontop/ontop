package org.obda.owlrefplatform.core.viewmanager;

import inf.unibz.it.obda.model.Atom;


/**
 * The view manager interface should be implemented by any class which is used to
 * establish a connection between data log queries and source queries. The Source generator
 * uses the information provided by it to translate datalog programs into source queries 
 * 
 * @author Manfred Gerstgrasser
 *
 */

public interface ViewManager {
	
	/**
	 * Translates the given atom into the alias which was used for it during
	 * some other processes, like abox dump, etc. Might not always necessary to
	 * implement
	 * 
	 * @param atom the atom
	 * @return the alias of the atom
	 * @throws Exception
	 */
	public String getTranslatedName(Atom atom) throws Exception;
	
	/**
	 * Stores the original query head, so that also the source generator 
	 * knows the original variable names, which otherwise might get lost during the
	 * rewriting and unfolding process. Might not always necessary to
	 * implement
	 * @param head original head of the input query
	 */
	public void storeOrgQueryHead(Atom head);
}
