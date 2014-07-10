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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIteratorIteration;
import info.aduna.lang.ObjectUtil;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.AggregateOperator;
import org.openrdf.query.algebra.AggregateOperatorBase;
import org.openrdf.query.algebra.Avg;
import org.openrdf.query.algebra.Count;
import org.openrdf.query.algebra.Group;
import org.openrdf.query.algebra.GroupConcat;
import org.openrdf.query.algebra.GroupElem;
import org.openrdf.query.algebra.MathExpr.MathOp;
import org.openrdf.query.algebra.Max;
import org.openrdf.query.algebra.Min;
import org.openrdf.query.algebra.Sample;
import org.openrdf.query.algebra.Sum;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.util.MathUtil;
import org.openrdf.query.algebra.evaluation.util.ValueComparator;
import org.openrdf.query.impl.EmptyBindingSet;

/**
 * @author David Huynh
 * @author Arjohn Kampman
 * @author Jeen Broekstra
 * @author James Leigh
 */
public class GroupIterator extends CloseableIteratorIteration<BindingSet, QueryEvaluationException> {

	/*-----------*
	 * Constants *
	 *-----------*/

	private final ValueFactoryImpl vf = ValueFactoryImpl.getInstance();

	private final EvaluationStrategy strategy;

	private final BindingSet parentBindings;

	private final Group group;

	private boolean initialized = false;

	private final Object lock = new Object();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public GroupIterator(EvaluationStrategy strategy, Group group, BindingSet parentBindings)
		throws QueryEvaluationException
	{
		this.strategy = strategy;
		this.group = group;
		this.parentBindings = parentBindings;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public boolean hasNext()
		throws QueryEvaluationException
	{
		if (!initialized) {
			synchronized (lock) {
				if (!initialized) {
					super.setIterator(createIterator());
					initialized = true;
				}
			}
		}
		return super.hasNext();
	}

	@Override
	public BindingSet next()
		throws QueryEvaluationException
	{
		if (!initialized) {
			synchronized (lock) {
				if (!initialized) {
					super.setIterator(createIterator());
					initialized = true;
				}
			}
		}
		return super.next();
	}

	private Iterator<BindingSet> createIterator()
		throws QueryEvaluationException
	{
		Collection<Entry> entries = buildEntries();
		Collection<BindingSet> bindingSets = new LinkedList<BindingSet>();

		for (Entry entry : entries) {
			QueryBindingSet sol = new QueryBindingSet(parentBindings);

			for (String name : group.getGroupBindingNames()) {
				BindingSet prototype = entry.getPrototype();
				if (prototype != null) {
					Value value = prototype.getValue(name);
					if (value != null) {
						// Potentially overwrites bindings from super
						sol.setBinding(name, value);
					}
				}
			}

			entry.bindSolution(sol);

			bindingSets.add(sol);
		}

		return bindingSets.iterator();
	}

	private Collection<Entry> buildEntries()
		throws QueryEvaluationException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> iter;
		iter = strategy.evaluate(group.getArg(), parentBindings);

		try {
			Map<Key, Entry> entries = new LinkedHashMap<Key, Entry>();

			if (!iter.hasNext()) {
				// no solutions, still need to process aggregates to produce a
				// zero-result.
				entries.put(new Key(new EmptyBindingSet()), new Entry(new EmptyBindingSet()));
			}

			while (iter.hasNext()) {
				BindingSet sol;
				try {
					sol = iter.next();
				}
				catch (NoSuchElementException e) {
					break; // closed
				}
				Key key = new Key(sol);
				Entry entry = entries.get(key);

				if (entry == null) {
					entry = new Entry(sol);
					entries.put(key, entry);
				}

				entry.addSolution(sol);
			}

			return entries.values();
		}
		finally {
			iter.close();
		}

	}

	/**
	 * A unique key for a set of existing bindings.
	 * 
	 * @author David Huynh
	 */
	protected class Key {

		private BindingSet bindingSet;

		private int hash;

		public Key(BindingSet bindingSet) {
			this.bindingSet = bindingSet;

			for (String name : group.getGroupBindingNames()) {
				Value value = bindingSet.getValue(name);
				if (value != null) {
					this.hash ^= value.hashCode();
				}
			}
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof Key && other.hashCode() == hash) {
				BindingSet otherSolution = ((Key)other).bindingSet;

				for (String name : group.getGroupBindingNames()) {
					Value v1 = bindingSet.getValue(name);
					Value v2 = otherSolution.getValue(name);

					if (!ObjectUtil.nullEquals(v1, v2)) {
						return false;
					}
				}

				return true;
			}

			return false;
		}
	}

	private class Entry {

		private BindingSet prototype;

		private Map<String, Aggregate> aggregates;

		public Entry(BindingSet prototype)
			throws ValueExprEvaluationException, QueryEvaluationException
		{
			this.prototype = prototype;
			this.aggregates = new LinkedHashMap<String, Aggregate>();
			for (GroupElem ge : group.getGroupElements()) {
				Aggregate create = create(ge.getOperator());
				if (create != null) {
					aggregates.put(ge.getName(), create);
				}
			}
		}

		public BindingSet getPrototype() {
			return prototype;
		}

		public void addSolution(BindingSet bindingSet)
			throws QueryEvaluationException
		{
			for (Aggregate aggregate : aggregates.values()) {
				aggregate.processAggregate(bindingSet);
			}
		}

		public void bindSolution(QueryBindingSet sol)
			throws QueryEvaluationException
		{
			for (String name : aggregates.keySet()) {
				try {
					Value value = aggregates.get(name).getValue();
					if (value != null) {
						// Potentially overwrites bindings from super
						sol.setBinding(name, value);
					}
				}
				catch (ValueExprEvaluationException ex) {
					// There was a type error when calculating the value of the
					// aggregate.
					// We silently ignore the error, resulting in no result value
					// being bound.
				}
			}
		}

		private Aggregate create(AggregateOperator operator)
			throws ValueExprEvaluationException, QueryEvaluationException
		{
			if (operator instanceof Count) {
				return new CountAggregate((Count)operator);
			}
			else if (operator instanceof Min) {
				return new MinAggregate((Min)operator);
			}
			else if (operator instanceof Max) {
				return new MaxAggregate((Max)operator);
			}
			else if (operator instanceof Sum) {
				return new SumAggregate((Sum)operator);
			}
			else if (operator instanceof Avg) {
				return new AvgAggregate((Avg)operator);
			}
			else if (operator instanceof Sample) {
				return new SampleAggregate((Sample)operator);
			}
			else if (operator instanceof GroupConcat) {
				return new ConcatAggregate((GroupConcat)operator);
			}
			return null;
		}
	}

	private abstract class Aggregate {

		private final Set<Value> distinctValues;

		private final ValueExpr arg;

		public Aggregate(AggregateOperatorBase operator) {
			this.arg = operator.getArg();
			if (operator.isDistinct()) {
				distinctValues = new HashSet<Value>();
			}
			else {
				distinctValues = null;
			}
		}

		public abstract Value getValue()
			throws ValueExprEvaluationException;

		public abstract void processAggregate(BindingSet bindingSet)
			throws QueryEvaluationException;

		protected boolean distinctValue(Value value) {
			return distinctValues == null || distinctValues.add(value);
		}

		protected ValueExpr getArg() {
			return arg;
		}

		protected Value evaluate(BindingSet s)
			throws QueryEvaluationException
		{
			try {
				return strategy.evaluate(getArg(), s);
			}
			catch (ValueExprEvaluationException e) {
				return null; // treat missing or invalid expressions as null
			}
		}
	}

	private class CountAggregate extends Aggregate {

		private long count = 0;

		private final Set<BindingSet> distinctBindingSets;

		public CountAggregate(Count operator) {
			super(operator);

			// for a wildcarded count with a DISTINCT clause we need to filter on
			// distinct bindingsets rather than individual values.
			if (operator.isDistinct() && getArg() == null) {
				distinctBindingSets = new HashSet<BindingSet>();
			}
			else {
				distinctBindingSets = null;
			}
		}

		@Override
		public void processAggregate(BindingSet s)
			throws QueryEvaluationException
		{
			if (getArg() != null) {
				Value value = evaluate(s);
				if (value != null && distinctValue(value)) {
					count++;
				}
			}
			else {
				// wildcard count
				if (distinctBindingSet(s)) {
					count++;
				}
			}
		}

		protected boolean distinctBindingSet(BindingSet s) {
			return this.distinctBindingSets == null || distinctBindingSets.add(s);
		}
		
		@Override
		public Value getValue() {
			return vf.createLiteral(Long.toString(count), XMLSchema.INTEGER);
		}
	}

	private class MinAggregate extends Aggregate {

		private final ValueComparator comparator = new ValueComparator();

		private Value min = null;

		public MinAggregate(Min operator) {
			super(operator);
		}

		@Override
		public void processAggregate(BindingSet s)
			throws QueryEvaluationException
		{
			Value v = evaluate(s);
			if (distinctValue(v)) {
				if (min == null) {
					min = v;
				}
				else if (comparator.compare(v, min) < 0) {
					min = v;
				}
			}
		}

		@Override
		public Value getValue() {
			return min;
		}
	}

	private class MaxAggregate extends Aggregate {

		private final ValueComparator comparator = new ValueComparator();

		private Value max = null;

		public MaxAggregate(Max operator) {
			super(operator);
		}

		@Override
		public void processAggregate(BindingSet s)
			throws QueryEvaluationException
		{
			Value v = evaluate(s);
			if (distinctValue(v)) {
				if (max == null) {
					max = v;
				}
				else if (comparator.compare(v, max) > 0) {
					max = v;
				}
			}
		}

		@Override
		public Value getValue() {
			return max;
		}
	}

	private class SumAggregate extends Aggregate {

		private Literal sum = vf.createLiteral("0", XMLSchema.INTEGER);

		private ValueExprEvaluationException typeError = null;

		public SumAggregate(Sum operator) {
			super(operator);
		}

		@Override
		public void processAggregate(BindingSet s)
			throws QueryEvaluationException
		{
			if (typeError != null) {
				// halt further processing if a type error has been raised
				return;
			}

			Value v = evaluate(s);
			if (distinctValue(v)) {
				if (v instanceof Literal) {
					Literal nextLiteral = (Literal)v;
					if (nextLiteral.getDatatype() != null
							&& XMLDatatypeUtil.isNumericDatatype(nextLiteral.getDatatype()))
					{
						sum = MathUtil.compute(sum, nextLiteral, MathOp.PLUS);
					}
					else {
						typeError = new ValueExprEvaluationException("not a number: " + v);
					}
				}
				else if (v != null) {
					typeError = new ValueExprEvaluationException("not a number: " + v);
				}
			}
		}

		@Override
		public Value getValue()
			throws ValueExprEvaluationException
		{
			if (typeError != null) {
				throw typeError;
			}

			return sum;
		}
	}

	private class AvgAggregate extends Aggregate {

		private long count = 0;

		private Literal sum = vf.createLiteral("0", XMLSchema.INTEGER);

		private ValueExprEvaluationException typeError = null;

		public AvgAggregate(Avg operator) {
			super(operator);
		}

		@Override
		public void processAggregate(BindingSet s)
			throws QueryEvaluationException
		{
			if (typeError != null) {
				// Prevent calculating the aggregate further if a type error has
				// occured.
				return;
			}

			Value v = evaluate(s);
			if (distinctValue(v)) {
				if (v instanceof Literal) {
					Literal nextLiteral = (Literal)v;
					// check if the literal is numeric.
					if (nextLiteral.getDatatype() != null
							&& XMLDatatypeUtil.isNumericDatatype(nextLiteral.getDatatype()))
					{
						sum = MathUtil.compute(sum, nextLiteral, MathOp.PLUS);
					}
					else {
						typeError = new ValueExprEvaluationException("not a number: " + v);
					}
					count++;
				}
				else if (v != null) {
					// we do not actually throw the exception yet, but record it and
					// stop further processing. The exception will be thrown when
					// getValue() is invoked.
					typeError = new ValueExprEvaluationException("not a number: " + v);
				}
			}
		}

		@Override
		public Value getValue()
			throws ValueExprEvaluationException
		{
			if (typeError != null) {
				// a type error occurred while processing the aggregate, throw it
				// now.
				throw typeError;
			}

			if (count == 0) {
				return vf.createLiteral(0.0d);
			}

			Literal sizeLit = vf.createLiteral(count);
			return MathUtil.compute(sum, sizeLit, MathOp.DIVIDE);
		}
	}

	private class SampleAggregate extends Aggregate {

		private Value sample = null;

		private Random random;

		public SampleAggregate(Sample operator) {
			super(operator);
			random = new Random(System.currentTimeMillis());
		}

		@Override
		public void processAggregate(BindingSet s)
			throws QueryEvaluationException
		{
			// we flip a coin to determine if we keep the current value or set a
			// new value to report.
			if (sample == null || random.nextFloat() < 0.5f) {
				sample = evaluate(s);
			}
		}

		@Override
		public Value getValue() {
			return sample;
		}
	}

	private class ConcatAggregate extends Aggregate {

		private StringBuilder concatenated = new StringBuilder();

		private String separator = " ";

		public ConcatAggregate(GroupConcat groupConcatOp)
			throws ValueExprEvaluationException, QueryEvaluationException
		{
			super(groupConcatOp);
			ValueExpr separatorExpr = groupConcatOp.getSeparator();
			if (separatorExpr != null) {
				Value separatorValue = strategy.evaluate(separatorExpr, parentBindings);
				separator = separatorValue.stringValue();
			}
		}

		@Override
		public void processAggregate(BindingSet s)
			throws QueryEvaluationException
		{
			Value v = evaluate(s);
			if (v != null && distinctValue(v)) {
				concatenated.append(v.stringValue());
				concatenated.append(separator);
			}
		}

		@Override
		public Value getValue() {
			if (concatenated.length() == 0) {
				return vf.createLiteral("");
			}

			// remove separator at the end.
			int len = concatenated.length() - separator.length();
			return vf.createLiteral(concatenated.substring(0, len));
		}
	}
}
