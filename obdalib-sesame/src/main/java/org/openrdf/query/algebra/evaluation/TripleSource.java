/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.query.algebra.evaluation;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryEvaluationException;

/**
 * A triple source that can be queried for (the existence of) certain triples in
 * certain contexts. This interface defines the methods that are needed by the
 * Sail Query Model to be able to evaluate itself.
 */
public interface TripleSource {

	/**
	 * Gets all statements that have a specific subject, predicate and/or object.
	 * All three parameters may be null to indicate wildcards. Optionally a (set
	 * of) context(s) may be specified in which case the result will be
	 * restricted to statements matching one or more of the specified contexts.
	 * 
	 * @param subj
	 *        A Resource specifying the subject, or <tt>null</tt> for a
	 *        wildcard.
	 * @param pred
	 *        A URI specifying the predicate, or <tt>null</tt> for a wildcard.
	 * @param obj
	 *        A Value specifying the object, or <tt>null</tt> for a wildcard.
	 * @param contexts
	 *        The context(s) to get the statements from. Note that this parameter
	 *        is a vararg and as such is optional. If no contexts are supplied
	 *        the method operates on the entire repository.
	 * @return An iterator over the relevant statements.
	 * @throws QueryEvaluationException
	 *         If the triple source failed to get the statements.
	 */
	public CloseableIteration<? extends Statement, QueryEvaluationException> getStatements(Resource subj,
			URI pred, Value obj, Resource... contexts)
		throws QueryEvaluationException;

	/**
	 * Gets a ValueFactory object that can be used to create URI-, blank node-
	 * and literal objects.
	 * 
	 * @return a ValueFactory object for this TripleSource.
	 */
	public ValueFactory getValueFactory();
}
