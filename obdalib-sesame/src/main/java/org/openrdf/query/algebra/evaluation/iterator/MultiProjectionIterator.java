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

import java.util.Arrays;
import java.util.List;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.LookAheadIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.ProjectionElemList;

/**
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class MultiProjectionIterator extends LookAheadIteration<BindingSet, QueryEvaluationException> {

	/*------------*
	 * Attributes *
	 *------------*/

	private final List<ProjectionElemList> projections;

	private final CloseableIteration<BindingSet, QueryEvaluationException> iter;

	private final BindingSet parentBindings;

	private final BindingSet[] previousBindings;

	private BindingSet currentBindings;

	private volatile int nextProjectionIdx;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public MultiProjectionIterator(MultiProjection multiProjection,
			CloseableIteration<BindingSet, QueryEvaluationException> iter, BindingSet bindings)
	{
		this.projections = multiProjection.getProjections();
		this.iter = iter;
		this.parentBindings = bindings;
		this.previousBindings = new BindingSet[projections.size()];

		// initialize out-of-range to enforce a fetch of the first result upon
		// first use
		nextProjectionIdx = projections.size();
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected BindingSet getNextElement()
		throws QueryEvaluationException
	{
		while (true) {
			int projIdx = nextProjectionIdx;

			if (projIdx < projections.size()) {
				// Apply next projection in the list
				ProjectionElemList projection = projections.get(projIdx);
				BindingSet result = ProjectionIterator.project(projection, currentBindings, parentBindings);

				nextProjectionIdx++;

				// ignore duplicates
				if (!result.equals(previousBindings[projIdx])) {
					previousBindings[projIdx] = result;
					return result;
				}
			}
			else if (iter.hasNext()) {
				// Continue with the next result
				currentBindings = iter.next();
				nextProjectionIdx = 0;
			}
			else {
				// no more results
				return null;
			}
		}
	}

	@Override
	protected void handleClose()
		throws QueryEvaluationException
	{
		super.handleClose();
		iter.close();
		nextProjectionIdx = projections.size();
		Arrays.fill(previousBindings, null);
	}
}
