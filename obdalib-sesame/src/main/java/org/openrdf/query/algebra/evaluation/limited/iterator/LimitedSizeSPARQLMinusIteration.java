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

import info.aduna.iteration.Iteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.iterator.SPARQLMinusIteration;

/**
 * @author Jerven Bolleman, SIB Swiss Institute of Bioinformatics
 */
public class LimitedSizeSPARQLMinusIteration extends SPARQLMinusIteration<QueryEvaluationException> {

	private AtomicLong used;

	private long maxSize;

	/**
	 * Creates a new MinusIteration that returns the results of the left argument
	 * minus the results of the right argument. By default, duplicates are
	 * <em>not</em> filtered from the results.
	 * 
	 * @param leftArg
	 *        An Iteration containing the main set of elements.
	 * @param rightArg
	 *        An Iteration containing the set of elements that should be filtered
	 *        from the main set. * @param used An atomic long used to monitor how
	 *        many elements are in the set collections.
	 * @param used
	 *        An atomic long used to monitor how many elements are in the set
	 *        collections.
	 * @param maxSize
	 *        Maximum size allowed by the sum of all collections used by the
	 *        LimitedSizeQueryEvaluatlion.
	 */
	public LimitedSizeSPARQLMinusIteration(Iteration<BindingSet, QueryEvaluationException> leftArg,
			Iteration<BindingSet, QueryEvaluationException> rightArg, AtomicLong used, long maxSize)
	{
		this(leftArg, rightArg, false, used, maxSize);
	}

	/**
	 * Creates a new SPARQLMinusIteration that returns the results of the left argument
	 * minus the results of the right argument.
	 * 
	 * @param leftArg
	 *        An Iteration containing the main set of elements.
	 * @param rightArg
	 *        An Iteration containing the set of elements that should be filtered
	 *        from the main set.
	 * @param distinct
	 *        Flag indicating whether duplicate elements should be filtered from
	 *        the result.
	 * @param used
	 *        An atomic long used to monitor how many elements are in the set
	 *        collections.
	 * @param maxSize
	 *        Maximum size allowed by the sum of all collections used by the
	 *        LimitedSizeQueryEvaluatlion.
	 */
	public LimitedSizeSPARQLMinusIteration(Iteration<BindingSet, QueryEvaluationException> leftArg,
			Iteration<BindingSet, QueryEvaluationException> rightArg, boolean distinct, AtomicLong used,
			long maxSize)
	{
		super(leftArg, rightArg, distinct);
		this.used = used;
		this.maxSize = maxSize;
	}
	
	@Override
	protected Set<BindingSet> makeSet(Iteration<BindingSet, QueryEvaluationException> rightArg2) 
			throws QueryEvaluationException {
		return LimitedSizeIteratorUtil.addAll(rightArg2, makeSet(),used, maxSize);
	}

	/**
	 * After closing the set is cleared and any "used" capacity for collections is returned.
	 */
	@Override
	protected void handleClose()
		throws QueryEvaluationException
	{
		long size = clearExcludeSet();
		super.handleClose();
		used.addAndGet(-size);
	}


}
