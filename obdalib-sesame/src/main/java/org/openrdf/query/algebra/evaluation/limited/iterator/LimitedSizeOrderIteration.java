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

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.atomic.AtomicLong;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.iterator.OrderIterator;

/**
 * @author Jerven Bolleman, SIB Swiss Institute of Bioinformatics
 */
public class LimitedSizeOrderIteration extends OrderIterator {

	private final AtomicLong used;

	private final long maxSize;

	/**
	 * @param iter
	 * @param comparator
	 */
	public LimitedSizeOrderIteration(CloseableIteration<BindingSet, QueryEvaluationException> iter,
			Comparator<BindingSet> comparator, AtomicLong used, long maxSize)
	{
		this(iter, comparator, Integer.MAX_VALUE, false, used, maxSize);
	}

	public LimitedSizeOrderIteration(CloseableIteration<BindingSet, QueryEvaluationException> iter,
			Comparator<BindingSet> comparator, long limit, boolean distinct, AtomicLong used, long maxSize)
	{
		super(iter, comparator, limit, distinct);
		this.used = used;
		this.maxSize = maxSize;
	}

	@Override
	protected void removeLast(Collection<BindingSet> lastResults) {
		super.removeLast(lastResults);
		used.decrementAndGet();
	}

	@Override
	protected Collection<BindingSet> remove(NavigableMap<BindingSet, Collection<BindingSet>> map,
			BindingSet lastKey)
	{
		Collection<BindingSet> col = map.get(lastKey);
		col.remove(lastKey);
		used.addAndGet(-(col.size() + 1));
		return col;
	}

	@Override
	protected boolean add(BindingSet next, Collection<BindingSet> list)
		throws QueryEvaluationException
	{

		return LimitedSizeIteratorUtil.add(next, list, used, maxSize);
	}

	@Override
	protected Collection<BindingSet> put(Map<BindingSet, Collection<BindingSet>> map, BindingSet next,
			Collection<BindingSet> list) throws QueryEvaluationException
	{
		Collection<BindingSet> put = super.put(map, next, list);
		if (put != null && used.incrementAndGet() > maxSize){
			throw new QueryEvaluationException("Size limited reached inside order operator query, max size is:"+maxSize);
		}
		return put;
	}
}
