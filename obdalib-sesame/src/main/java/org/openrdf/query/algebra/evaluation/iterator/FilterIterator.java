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

import java.util.Set;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.FilterIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.SubQueryValueOperator;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

public class FilterIterator extends FilterIteration<BindingSet, QueryEvaluationException> {

	/*-----------*
	 * Constants *
	 *-----------*/

	private final Filter filter;

	private final EvaluationStrategy strategy;

	/**
	 * The set of binding names that are "in scope" for the filter. The filter
	 * must not include bindings that are (only) included because of the
	 * depth-first evaluation strategy in the evaluation of the constraint.
	 */
	private final Set<String> scopeBindingNames;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public FilterIterator(Filter filter, CloseableIteration<BindingSet, QueryEvaluationException> iter,
			EvaluationStrategy strategy)
		throws QueryEvaluationException
	{
		super(iter);
		this.filter = filter;
		this.strategy = strategy;
		this.scopeBindingNames = filter.getBindingNames();
		
	}

	/*---------*
	 * Methods *
	 *---------*/

	private boolean isPartOfSubQuery(QueryModelNode node) {
		if (node instanceof SubQueryValueOperator) {
			return true;
		}
		
		QueryModelNode parent = node.getParentNode();
		if (parent == null) {
			return false;
		}
		else {
			return isPartOfSubQuery(parent);
		}
	}
	
	@Override
	protected boolean accept(BindingSet bindings)
		throws QueryEvaluationException
	{
		try {
			// Limit the bindings to the ones that are in scope for this filter
			QueryBindingSet scopeBindings = new QueryBindingSet(bindings);
			
			// FIXME J1 scopeBindingNames should include bindings from superquery if the filter
			// is part of a subquery. This is a workaround: we should fix the settings of scopeBindingNames, 
			// rather than skipping the limiting of bindings.
			if (!isPartOfSubQuery(filter)) {
				scopeBindings.retainAll(scopeBindingNames);
			}

			return strategy.isTrue(filter.getCondition(), scopeBindings);
		}
		catch (ValueExprEvaluationException e) {
			// failed to evaluate condition
			return false;
		}
	}
}
