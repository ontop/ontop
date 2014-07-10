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

import java.util.Collection;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.SingletonIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.evaluation.federation.FederatedService.QueryType;
import org.openrdf.query.algebra.evaluation.iterator.SilentIteration;
import org.openrdf.query.impl.EmptyBindingSet;

/**
 * Fallback join handler, if the block join can not be performed, e.g. because
 * the BINDINGS clause is not supported by the endpoint. Gets a materialized
 * collection of bindings as input, and has to evaluate the join.
 * 
 * @author Andreas Schwarte
 */
public class ServiceFallbackIteration extends JoinExecutorBase<BindingSet> {

	protected final Service service;

	protected final String preparedQuery;

	protected final FederatedService federatedService;

	protected final Collection<BindingSet> bindings;

	public ServiceFallbackIteration(Service service, String preparedQuery, Collection<BindingSet> bindings,
			FederatedService federatedService)
		throws QueryEvaluationException
	{
		super(null, null, EmptyBindingSet.getInstance());
		this.service = service;
		this.preparedQuery = preparedQuery;
		this.bindings = bindings;
		this.federatedService = federatedService;
		run();
	}

	@Override
	protected void handleBindings()
		throws Exception
	{

		// NOTE: we do not have to care about SILENT services, as this
		// iteration by itself is wrapped in a silentiteration

		// handle each prepared query individually and add the result to this
		// iteration
		for (BindingSet b : bindings) {
			try {
				CloseableIteration<BindingSet, QueryEvaluationException> result = federatedService.evaluate(
						preparedQuery, b, service.getBaseURI(), QueryType.SELECT, service);
				result = service.isSilent() ? new SilentIteration(result) : result;
				addResult(result);
			} 
			catch (QueryEvaluationException e) {
				// suppress exceptions if silent
				if (service.isSilent()) {
					addResult(new SingletonIteration<BindingSet, QueryEvaluationException>(b));
				} else {
					throw e;
				}
			}			
			catch (RuntimeException e) {
				// suppress special exceptions (e.g. UndeclaredThrowable with wrapped
				// QueryEval) if silent
				if (service.isSilent()) {
					addResult(new SingletonIteration<BindingSet, QueryEvaluationException>(b));
				}
				else {
					throw e;
				}
			}
		}

	}

}
