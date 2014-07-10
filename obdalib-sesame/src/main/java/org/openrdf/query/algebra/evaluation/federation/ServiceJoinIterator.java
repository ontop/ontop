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
package org.openrdf.query.algebra.evaluation.federation;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;

/**
 * Iterator for efficient SERVICE evaluation (vectored). SERVICE is the right
 * handside argument of this join.
 * 
 * @author Andreas Schwarte
 */
public class ServiceJoinIterator extends JoinExecutorBase<BindingSet> {

	protected Service service;

	protected EvaluationStrategy strategy;

	/**
	 * Construct a service join iteration to use vectored evaluation. The
	 * constructor automatically starts evaluation.
	 * 
	 * @param leftIter
	 * @param service
	 * @param bindings
	 * @param strategy
	 * @throws QueryEvaluationException
	 */
	public ServiceJoinIterator(CloseableIteration<BindingSet, QueryEvaluationException> leftIter,
			Service service, BindingSet bindings, EvaluationStrategy strategy)
		throws QueryEvaluationException
	{
		super(leftIter, service, bindings);
		this.service = service;
		this.strategy = strategy;
		run();
	}

	@Override
	protected void handleBindings()
		throws Exception
	{
		Var serviceRef = service.getServiceRef();

		String serviceUri;
		if (serviceRef.hasValue())
			serviceUri = serviceRef.getValue().stringValue();
		else {
			// case 2: the service ref is not defined beforehand
			// => use a fallback to the naive evaluation.
			// exceptions occurring here must NOT be silenced!
			while (!closed && leftIter.hasNext()) {
				addResult(strategy.evaluate(service, leftIter.next()));
			}
			return;
		}

		// use vectored evaluation
		FederatedService fs = FederatedServiceManager.getInstance().getService(serviceUri);
		addResult(fs.evaluate(service, leftIter, service.getBaseURI()));
	}
}
