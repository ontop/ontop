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
import info.aduna.iteration.ConvertingIteration;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;

public class ProjectionIterator extends ConvertingIteration<BindingSet, BindingSet, QueryEvaluationException>
{

	/*-----------*
	 * Constants *
	 *-----------*/

	private final Projection projection;

	private final BindingSet parentBindings;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ProjectionIterator(Projection projection,
			CloseableIteration<BindingSet, QueryEvaluationException> iter, BindingSet parentBindings)
		throws QueryEvaluationException
	{
		super(iter);
		this.projection = projection;
		this.parentBindings = parentBindings;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected BindingSet convert(BindingSet sourceBindings)
		throws QueryEvaluationException
	{

		return project(projection.getProjectionElemList(), sourceBindings, parentBindings);
	}

	public static BindingSet project(ProjectionElemList projElemList, BindingSet sourceBindings,
			BindingSet parentBindings)
	{
		QueryBindingSet resultBindings = new QueryBindingSet(parentBindings);

		for (ProjectionElem pe : projElemList.getElements()) {
			Value targetValue = sourceBindings.getValue(pe.getSourceName());
			if (targetValue != null) {
				// Potentially overwrites bindings from super
				resultBindings.setBinding(pe.getTargetName(), targetValue);
			}
		}

		return resultBindings;
	}
}
