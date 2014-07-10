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
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import info.aduna.iteration.Iteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;

/**
 * @author Jerven Bolleman, SIB Swiss Institute of Bioinformatics
 */
public class LimitedSizeIteratorUtil {

	/**
	 * 
	 * @param arg2 the iteration with elements to add to the includeSet
	 * @param includeSet the set that should have all unique elements of arg2
	 * @param used the collection size counter of all collections used in answering a query
	 * @param maxSize the point at which we throw a new query exception
	 * @return the includeSet 
	 * @throws QueryEvaluationException trigerred when maxSize is smaller than the used value
	 */
	public static Set<BindingSet> addAll(Iteration<? extends BindingSet, ? extends QueryEvaluationException> arg2,
			Set<BindingSet> includeSet, AtomicLong used, long maxSize)
		throws QueryEvaluationException
	{
		while (arg2.hasNext()) {
			if (includeSet.add(arg2.next()) && used.incrementAndGet() > maxSize)
				throw new QueryEvaluationException("Size limited reached inside intersect operator");
		}
		return includeSet;
	}

	/**
	 * @param object
	 * 		 object to put in set if not there already.
	 * @param excludeSet
	 * 		 set that we need to store object in.
	 * @param used
	 *        AtomicLong tracking how many elements we have in storage.
	 * @param maxSize
	 * @throws QueryEvaluationException
	 *         when the object is added to the set and the total elements in all
	 *         limited size collections exceed the allowed maxSize.
	 */
	public static <V> boolean add(V object, Collection<V> excludeSet, AtomicLong used, long maxSize)
		throws QueryEvaluationException
	{
		boolean add = excludeSet.add(object);
		if (add && used.incrementAndGet() > maxSize)
			throw new QueryEvaluationException("Size limited reached inside query operator.");
		return add;
	}
}
