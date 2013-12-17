/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.unfolding;

import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;

import java.io.Serializable;

/**
 * This interface should be implemented by any class which implements an
 * unfolding Mechanism which should be integrated into a technique wrapper
 * 
 * @author Manfred Gerstgrasser, mrezk
 * 
 */

public interface UnfoldingMechanism extends Serializable {

	/**
	 * unfolds the the given datalog program
	 * 
	 * @param query
	 *            the query
	 * @return the unfolded query
	 * @throws Exception
	 */
	@Deprecated
	public DatalogProgram unfold(DatalogProgram query, String targetPredicate)
			throws OBDAException;

	/**
	 * unfolds the the given datalog program applying partial evaluation. It will do the partial evaluation following 
	 * a specified strategy, for instance, bottom up or top down.
	 * @param query
	 * @param targetPredicate
	 * @param strategy
	 * @return
	 * @throws OBDAException
	 */
	public DatalogProgram unfold(DatalogProgram query, String targetPredicate, String strategy)
			throws OBDAException;

}
