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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.iterator.BottomUpJoinIterator;

/**
 * @author Jerven Bolleman, SIB Swiss Institute of Bioinformatics
 */
public class LimitedSizeBottomUpJoinIterator extends BottomUpJoinIterator {

	private static final String SIZE_LIMIT_REACHED = "Size limited reached inside bottom up join operator, max size is:";
	private AtomicLong used;

	private long maxSize;

	/**
	 * @param limitedSizeEvaluationStrategy
	 * @param join
	 * @param bindings
	 * @param used
	 * @param maxSize
	 * @throws QueryEvaluationException
	 */
	public LimitedSizeBottomUpJoinIterator(EvaluationStrategy limitedSizeEvaluationStrategy,
			Join join, BindingSet bindings, AtomicLong used, long maxSize)
		throws QueryEvaluationException
	{
		super(limitedSizeEvaluationStrategy, join, bindings);
		this.used = used;
		this.maxSize = maxSize;
	}

	@Override
	protected void addAll(List<BindingSet> hashTableValues, List<BindingSet> values)
		throws QueryEvaluationException
	{
		Iterator<BindingSet> iter = values.iterator();
		while (iter.hasNext()) {
			if (hashTableValues.add(iter.next()) && used.incrementAndGet() > maxSize) {
				throw new QueryEvaluationException(SIZE_LIMIT_REACHED+maxSize);
			}
		}
	}

	@Override
	protected void add(List<BindingSet> leftArgResults, BindingSet b)
		throws QueryEvaluationException
	{
		if (leftArgResults.add(b) && used.incrementAndGet() > maxSize) {
			throw new QueryEvaluationException(SIZE_LIMIT_REACHED+maxSize);
		}
	}

	@Override
	protected BindingSet removeFirstElement(List<BindingSet> list)
		throws QueryEvaluationException
	{
		used.decrementAndGet();
		return super.removeFirstElement(list);
	}

	@Override
	protected void put(Map<BindingSet, List<BindingSet>> hashTable, BindingSet hashKey,
			List<BindingSet> hashValue)
		throws QueryEvaluationException
	{
		List<BindingSet> put = hashTable.put(hashKey, hashValue);
		if (put == null && used.incrementAndGet() > maxSize) {
			throw new QueryEvaluationException(SIZE_LIMIT_REACHED+maxSize);
		}
	}

	@Override
	protected void handleClose()
		throws QueryEvaluationException
	{
		long htvSize = clearHashTable();
		super.handleClose();
		used.addAndGet(-htvSize);
	}

	

}
