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

import java.util.Iterator;
import java.util.List;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ConvertingIteration;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;

/**
 * Inserts original bindings into the result, uses ?__rowIdx to resolve original
 * bindings. See {@link ServiceJoinIterator} and {@link SPARQLFederatedService}.
 * 
 * @author Andreas Schwarte
 */
public class ServiceJoinConversionIteration extends
		ConvertingIteration<BindingSet, BindingSet, QueryEvaluationException> {

	protected final List<BindingSet> bindings;

	public ServiceJoinConversionIteration(
			CloseableIteration<BindingSet, QueryEvaluationException> iter,
			List<BindingSet> bindings) {
		super(iter);
		this.bindings = bindings;
	}

	@Override
	protected BindingSet convert(BindingSet bIn)
			throws QueryEvaluationException {

		// overestimate the capacity
		QueryBindingSet res = new QueryBindingSet(bIn.size() + bindings.size());

		int bIndex = -1;
		Iterator<Binding> bIter = bIn.iterator();
		while (bIter.hasNext()) {
			Binding b = bIter.next();
			String name = b.getName();
			if (name.equals("__rowIdx")) {
				bIndex = Integer.parseInt(b.getValue().stringValue());
				continue;
			}
			res.addBinding(b.getName(), b.getValue());
		}
		
		// should never occur: in such case we would have to create the cross product (which
		// is dealt with in another place)
		if (bIndex == -1)
			throw new QueryEvaluationException("Invalid join. Probably this is due to non-standard behavior of the SPARQL endpoint. " +
					"Please report to the developers.");
		
		res.addAll(bindings.get(bIndex));
		return res;
	}
}
