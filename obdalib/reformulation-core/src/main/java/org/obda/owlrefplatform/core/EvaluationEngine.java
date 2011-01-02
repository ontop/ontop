package org.obda.owlrefplatform.core;

import inf.unibz.it.obda.domain.DataSource;

import java.sql.ResultSet;

/**
 * An interface representing the Evaluation engine for a Technique wrapper.
 * The main task of such an evaluation engine is to evaluate the rewritten
 * and unfolded data log program over the data source.
 * 
 * @author Manfred Gerstgrasser
 *
 */

public interface EvaluationEngine {

	/**
	 * Evalutes the given string over the data source
	 * 
	 * @param sql the query
	 * @return the result set
	 * @throws Exception
	 */
	public ResultSet execute(String sql) throws Exception;
	
	/**
	 * Updates the current data source with the given one, i.e. after 
	 * the update queries will be evaluated over the new data source.
	 * 
	 * @param ds the new data source
	 */
	public void update(DataSource ds);
}
