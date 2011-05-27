package it.unibz.krdb.obda.owlrefplatform.core.unfolding;

import it.unibz.krdb.obda.model.DatalogProgram;


/**
 * This interface should be implemented by any class which implements an unfolding
 * Mechanism which should be integrated into a technique wrapper
 * 
 * @author Manfred Gerstgrasser
 *
 */

public interface UnfoldingMechanism {

	/**
	 * unfolds the the given datalog program
	 * 
	 * @param query the query
	 * @return the unfolded query
	 * @throws Exception
	 */
	public DatalogProgram unfold(DatalogProgram query) throws Exception;
}
