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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.DelayedIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.LookAheadIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;

/**
 * Sorts the input and optionally applies limit and distinct.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 */
public class OrderIterator extends DelayedIteration<BindingSet, QueryEvaluationException> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final CloseableIteration<BindingSet, QueryEvaluationException> iter;

	private final Comparator<BindingSet> comparator;

	private final long limit;

	private final boolean distinct;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public OrderIterator(CloseableIteration<BindingSet, QueryEvaluationException> iter,
			Comparator<BindingSet> comparator)
	{
		this(iter, comparator, Integer.MAX_VALUE, false);
	}

	public OrderIterator(CloseableIteration<BindingSet, QueryEvaluationException> iter,
			Comparator<BindingSet> comparator, long limit, boolean distinct)
	{
		this.iter = iter;
		this.comparator = comparator;
		this.limit = limit;
		this.distinct = distinct;
	}

	/*---------*
	 * Methods *
	 *---------*/

	protected Iteration<BindingSet, QueryEvaluationException> createIteration()
		throws QueryEvaluationException
	{
		NavigableMap<BindingSet, Collection<BindingSet>> map = makeOrderedMap(comparator);

		int size = 0;

		try {
			while (iter.hasNext()) {
				BindingSet next = iter.next();

				// Add this binding set if the limit hasn't been reached yet, or if
				// it is sorted before the current lowest value
				if (size < limit || comparator.compare(next, map.lastKey()) < 0) {
					Collection<BindingSet> list = map.get(next);
					if (list == null) {
						list = distinct ? makeOrderedSet() : makeList();
						put(map, next, list);
					}

					if (add(next, list)) {
						size++;
					}

					if (size > limit) {
						// Discard binding set that is currently sorted last
						BindingSet lastKey = map.lastKey();
						Collection<BindingSet> lastResults = map.get(lastKey);

						assert !lastResults.isEmpty();

						removeLast(lastResults);

						size--;

						if (lastResults.isEmpty()) {
							remove(map, lastKey);
						}
					}
				}
			}
		}
		finally {
			iter.close();
		}

		final Iterator<Collection<BindingSet>> values = map.values().iterator();

		return new LookAheadIteration<BindingSet, QueryEvaluationException>() {

			// Initialize with empty iteration so that var is never null
			private volatile Iterator<BindingSet> iterator = Collections.<BindingSet> emptyList().iterator();

			protected BindingSet getNextElement() {
				while (!iterator.hasNext() && values.hasNext()) {
					iterator = values.next().iterator();
				}
				if (iterator.hasNext()) {
					return iterator.next();
				}
				return null;
			}
		};
	}

	protected List<BindingSet> makeList() {
		return new LinkedList<BindingSet>();
	}

	/**
	 * This is used when distinct is set too true.
	 * @return a new Set may be store specific.
	 */
	protected Set<BindingSet> makeOrderedSet() {
		return new LinkedHashSet<BindingSet>();
	}

	protected void removeLast(Collection<BindingSet> lastResults) {
		if (lastResults instanceof LinkedList<?>) {
			((LinkedList<BindingSet>)lastResults).removeLast();
		} else if (lastResults instanceof List<?>){
			((List<BindingSet>)lastResults).remove(lastResults.size() - 1);
		} else {
			Iterator<BindingSet> iter = lastResults.iterator();
			while (iter.hasNext()) {
				iter.next();
			}
			iter.remove();
		}
	}

	protected Collection<BindingSet> remove(NavigableMap<BindingSet, Collection<BindingSet>> map,
			BindingSet lastKey)
	{
		return map.remove(lastKey);
	}

	protected boolean add(BindingSet next, Collection<BindingSet> list)
		throws QueryEvaluationException
	{
		return list.add(next);
	}

	protected Collection<BindingSet> put(Map<BindingSet, Collection<BindingSet>> map, BindingSet next,
			Collection<BindingSet> list)
		throws QueryEvaluationException
	{
		return map.put(next, list);
	}

	protected NavigableMap<BindingSet, Collection<BindingSet>> makeOrderedMap(Comparator<BindingSet> comparator2)
	{
		return new TreeMap<BindingSet, Collection<BindingSet>>(comparator);
	}

	@Override
	public void remove()
		throws QueryEvaluationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected void handleClose()
		throws QueryEvaluationException
	{
		iter.close();
		super.handleClose();
	}
}
