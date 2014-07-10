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
import org.openrdf.query.algebra.AggregateOperator;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

public class ExtensionIterator extends ConvertingIteration<BindingSet, BindingSet, QueryEvaluationException> {

	private final Extension extension;

	private final EvaluationStrategy strategy;

	public ExtensionIterator(Extension extension,
			CloseableIteration<BindingSet, QueryEvaluationException> iter, EvaluationStrategy strategy)
		throws QueryEvaluationException
	{
		super(iter);
		this.extension = extension;
		this.strategy = strategy;
	}

	@Override
	public BindingSet convert(BindingSet sourceBindings)
		throws QueryEvaluationException
	{
		QueryBindingSet targetBindings = new QueryBindingSet(sourceBindings);

		for (ExtensionElem extElem : extension.getElements()) {
			ValueExpr expr = extElem.getExpr();
			if (!(expr instanceof AggregateOperator)) {
				try {
					// we evaluate each extension element over the targetbindings, so that bindings from
					// a previous extension element in this same extension can be used by other extension elements. 
					// e.g. if a projection contains (?a + ?b as ?c) (?c * 2 as ?d)
					Value targetValue = strategy.evaluate(extElem.getExpr(), targetBindings);

					if (targetValue != null) {
						// Potentially overwrites bindings from super
						targetBindings.setBinding(extElem.getName(), targetValue);
					}
				}
				catch (ValueExprEvaluationException e) {
					// silently ignore type errors in extension arguments. They should not cause the 
					// query to fail but just result in no additional binding.
				}
			}
		}

		return targetBindings;
	}
}
