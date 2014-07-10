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
package org.openrdf.query.algebra.evaluation.iterator;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.FilterIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryResults;

/**
 * @author Arjohn Kampman
 * @deprecated
 */
@Deprecated
public class CompatibleBindingSetFilter extends FilterIteration<BindingSet, QueryEvaluationException> {

	private final BindingSet inputBindings;

	public CompatibleBindingSetFilter(CloseableIteration<BindingSet, QueryEvaluationException> iter,
			BindingSet inputBindings)
	{
		super(iter);

		assert inputBindings != null;
		this.inputBindings = inputBindings;
	}

	@Override
	protected boolean accept(BindingSet outputBindings)
		throws QueryEvaluationException
	{
		return QueryResults.bindingSetsCompatible(inputBindings, outputBindings);
	}
}
