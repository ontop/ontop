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

import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import info.aduna.iteration.IntersectIteration;
import info.aduna.iteration.Iteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;

/**
 * @author Jerven Bolleman, SIB Swiss Institute of Bioinformatics
 */
public class LimitedSizeIntersectIteration extends IntersectIteration<BindingSet, QueryEvaluationException> {

	private final AtomicLong used;

	private final long maxSize;

	/**
	 * Creates a new IntersectIteration that returns the intersection of the
	 * results of two Iterations. By default, duplicates are <em>not</em>
	 * filtered from the results.
	 * 
	 * @param arg1
	 *        An Iteration containing the first set of elements.
	 * @param arg2
	 *        An Iteration containing the second set of elements.
	 * @param used
	 *        An atomic long used to monitor how many elements are in the set
	 *        collections.
	 * @param maxSize
	 *        Maximum size allowed by the sum of all collections used by the
	 *        LimitedSizeQueryEvaluatlion.
	 */
	public LimitedSizeIntersectIteration(
			Iteration<? extends BindingSet, ? extends QueryEvaluationException> arg1,
			Iteration<? extends BindingSet, ? extends QueryEvaluationException> arg2, AtomicLong used,
			long maxSize)
	{
		this(arg1, arg2, false, used, maxSize);

	}

	public LimitedSizeIntersectIteration(
			Iteration<? extends BindingSet, ? extends QueryEvaluationException> arg1,
			Iteration<? extends BindingSet, ? extends QueryEvaluationException> arg2, boolean distinct,
			AtomicLong used, long maxSize)
	{
		super(arg1, arg2, distinct);
		this.used = used;
		this.maxSize = maxSize;
	}

	@Override
	public Set<BindingSet> addSecondSet(
			Iteration<? extends BindingSet, ? extends QueryEvaluationException> arg2, Set<BindingSet> set)
		throws QueryEvaluationException
	{

		LimitedSizeIteratorUtil.addAll(arg2, set, used, maxSize);
		return set;
	}

	/**
	 * After closing the set is cleared and any "used" capacity for collections
	 * is returned.
	 */
	@Override
	protected void handleClose()
		throws QueryEvaluationException
	{
		
		long size = clearIncludeSet();
		used.addAndGet(-size);
		super.handleClose();
	}

}
