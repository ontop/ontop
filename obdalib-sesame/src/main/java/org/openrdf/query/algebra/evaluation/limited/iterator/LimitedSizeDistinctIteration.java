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
package org.openrdf.query.algebra.evaluation.limited.iterator;

import java.util.concurrent.atomic.AtomicLong;

import info.aduna.iteration.DistinctIteration;
import info.aduna.iteration.Iteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;

/**
 * @author Jerven Bolleman, SIB Swiss Institute of Bioinformatics
 */
public class LimitedSizeDistinctIteration extends DistinctIteration<BindingSet, QueryEvaluationException> {

	private final AtomicLong used;

	private final long maxSize;

	/**
	 * @param iter
	 */
	public LimitedSizeDistinctIteration(
			Iteration<? extends BindingSet, ? extends QueryEvaluationException> iter, AtomicLong used,
			long maxSize)
	{
		super(iter);
		this.used = used;
		this.maxSize = maxSize;
	}

	@Override
	protected boolean add(BindingSet object)
		throws QueryEvaluationException
	{
		boolean add = super.add(object);
		if (add && used.incrementAndGet() > maxSize)
			throw new QueryEvaluationException("Size limited reached inside query operator.");
		return add;
	}

}
