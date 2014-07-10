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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.LookAheadIteration;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.impl.EmptyBindingSet;

/**
 * Join Iterator that executes a basic bottom-up hash-join algorithm. To be used
 * in cases where interleaved iteration joining is not appropriate (e.g. when
 * the join arguments are subselects).
 * 
 * @author jeen
 */
public class BottomUpJoinIterator extends LookAheadIteration<BindingSet, QueryEvaluationException> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final CloseableIteration<BindingSet, QueryEvaluationException> leftIter;

	private volatile CloseableIteration<BindingSet, QueryEvaluationException> rightIter;

	private List<BindingSet> scanList;

	private CloseableIteration<BindingSet, QueryEvaluationException> restIter;

	private Map<BindingSet, List<BindingSet>> hashTable;

	private Set<String> joinAttributes;

	private BindingSet currentScanElem;

	private List<BindingSet> hashTableValues;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public BottomUpJoinIterator(EvaluationStrategy strategy, Join join, BindingSet bindings)
		throws QueryEvaluationException
	{
		leftIter = strategy.evaluate(join.getLeftArg(), bindings);
		rightIter = strategy.evaluate(join.getRightArg(), bindings);

		joinAttributes = join.getLeftArg().getBindingNames();
		joinAttributes.retainAll(join.getRightArg().getBindingNames());

		hashTable = null;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected BindingSet getNextElement()
		throws QueryEvaluationException
	{
		if (hashTable == null) {
			setupHashTable();
		}

		while (currentScanElem == null) {
			if (scanList.size() > 0) {
				currentScanElem = removeFirstElement(scanList);
			}
			else {
				if (restIter.hasNext()) {
					currentScanElem = restIter.next();
				}
				else {
					// no more elements available
					return null;
				}
			}

			if (currentScanElem instanceof EmptyBindingSet) {
				// the empty bindingset should be merged with all bindingset in the
				// hash table
				hashTableValues = makeList();
				for (Map.Entry<BindingSet, List<BindingSet>> key : hashTable.entrySet()) {
					addAll(hashTableValues, key.getValue());
				}
			}
			else {
				BindingSet key = calcKey(currentScanElem, joinAttributes);

				if (hashTable.containsKey(key)) {
					hashTableValues = makeList(hashTable.get(key));
				}
				else {
					currentScanElem = null;
					hashTableValues = null;
				}
			}
		}

		BindingSet nextHashTableValue = removeFirstElement(hashTableValues);

		QueryBindingSet result = new QueryBindingSet(currentScanElem);

		for (String name : nextHashTableValue.getBindingNames()) {
			Binding b = nextHashTableValue.getBinding(name);
			if (!result.hasBinding(name)) {
				result.addBinding(b);
			}
		}

		if (hashTableValues.size() == 0) {
			// we've exhausted the current scanlist entry
			currentScanElem = null;
			hashTableValues = null;
		}

		return result;
	}

	@Override
	protected void handleClose()
		throws QueryEvaluationException
	{
		super.handleClose();

		leftIter.close();
		rightIter.close();

		hashTable = null;
		hashTableValues = null;
		scanList = null;
	}

	/**
	 * @return the size that the hashtable had before clearing it.
	 */
	protected long clearHashTable() {
		int size = hashTable.size();
		hashTable.clear();
		return size;
	}

	private BindingSet calcKey(BindingSet bindings, Set<String> commonVars) {
		QueryBindingSet q = new QueryBindingSet();
		for (String varName : commonVars) {
			Binding b = bindings.getBinding(varName);
			if (b != null) {
				q.addBinding(b);
			}
		}
		return q;
	}

	private void setupHashTable()
		throws QueryEvaluationException
	{

		hashTable = makeMap();

		List<BindingSet> leftArgResults = makeList();
		List<BindingSet> rightArgResults = makeList();

		while (leftIter.hasNext() && rightIter.hasNext()) {
			add(leftArgResults, leftIter.next());
			add(rightArgResults, rightIter.next());
		}

		List<BindingSet> smallestResult = null;

		if (leftIter.hasNext()) { // leftArg is the greater relation
			smallestResult = rightArgResults;
			scanList = leftArgResults;
			restIter = leftIter;
		}
		else { // rightArg is the greater relation (or they are equal)
			smallestResult = leftArgResults;
			scanList = rightArgResults;
			restIter = rightIter;
		}

		// create the hash table for our join
		for (BindingSet b : smallestResult) {
			BindingSet hashKey = calcKey(b, joinAttributes);

			List<BindingSet> hashValue = null;
			if (hashTable.containsKey(hashKey)) {
				hashValue = hashTable.get(hashKey);
			}
			else {
				hashValue = makeList();
			}
			add(hashValue, b);
			put(hashTable, hashKey, hashValue);
		}

	}

	protected void put(Map<BindingSet, List<BindingSet>> hashTable, BindingSet hashKey,
			List<BindingSet> hashValue)
		throws QueryEvaluationException
	{
		hashTable.put(hashKey, hashValue);
	}

	protected void addAll(List<BindingSet> hashTableValues, List<BindingSet> values)
		throws QueryEvaluationException
	{
		hashTableValues.addAll(values);
	}

	protected void add(List<BindingSet> leftArgResults, BindingSet b)
		throws QueryEvaluationException
	{
		leftArgResults.add(b);
	}

	/**
	 * Utility methods to make it easier to inserted custom store dependent maps
	 * 
	 * @return map
	 */
	protected Map<BindingSet, List<BindingSet>> makeMap() {
		return new HashMap<BindingSet, List<BindingSet>>();
	}

	/**
	 * Utility methods to make it easier to inserted custom store dependent list
	 * 
	 * @return list
	 */
	protected List<BindingSet> makeList() {
		return new ArrayList<BindingSet>();
	}

	/**
	 * Utility methods to make it easier to inserted custom store dependent list
	 * 
	 * @return list
	 */
	protected List<BindingSet> makeList(List<BindingSet> key) {
		return new ArrayList<BindingSet>(key);
	}

	/**
	 * Remove the first (0 index) element from a BindingSet list.
	 * 
	 * @param list
	 *        which is worked on.
	 * @return the removed BindingSet
	 */
	protected BindingSet removeFirstElement(List<BindingSet> list)
		throws QueryEvaluationException
	{
		return list.remove(0);
	}
}
