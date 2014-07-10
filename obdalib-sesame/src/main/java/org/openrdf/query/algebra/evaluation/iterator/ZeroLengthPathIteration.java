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

import java.util.HashSet;
import java.util.Set;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.LookAheadIteration;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.StatementPattern.Scope;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;

public class ZeroLengthPathIteration extends LookAheadIteration<BindingSet, QueryEvaluationException> {

	private QueryBindingSet result;

	private Var subjectVar;

	private Var objVar;

	private Value subj;

	private Value obj;

	private BindingSet bindings;

	private CloseableIteration<BindingSet, QueryEvaluationException> subjectIter;

	private CloseableIteration<BindingSet, QueryEvaluationException> objectIter;

	private Set<Value> reportedValues;

	private Var contextVar;

	private final EvaluationStrategyImpl evaluationStrategyImpl;

	public ZeroLengthPathIteration(EvaluationStrategyImpl evaluationStrategyImpl, Var subjectVar, Var objVar,
			Value subj, Value obj, Var contextVar, BindingSet bindings)
	{
		this.evaluationStrategyImpl = evaluationStrategyImpl;
		result = new QueryBindingSet(bindings);
		this.subjectVar = subjectVar;
		this.objVar = objVar;
		this.contextVar = contextVar;
		this.subj = subj;
		this.obj = obj;
		this.bindings = bindings;

		if (subj != null && obj == null) {
			result.addBinding(objVar.getName(), subj);
		}

		if (obj != null && subj == null) {
			result.addBinding(subjectVar.getName(), obj);
		}
		reportedValues = makeSet();
	}

	@Override
	protected BindingSet getNextElement()
		throws QueryEvaluationException
	{
		if (subj == null && obj == null) {
			if (this.subjectIter == null) {
				subjectIter = createSubjectIteration();
			}

			while (subjectIter.hasNext()) {
				QueryBindingSet next = new QueryBindingSet(subjectIter.next());

				Value v = next.getValue(subjectVar.getName());

				if (add(reportedValues, v)) {
					next.addBinding(objVar.getName(), v);
					return next;
				}
			}

			if (this.objectIter == null) {
				objectIter = createObjectIteration();
			}
			while (objectIter.hasNext()) {
				QueryBindingSet next = new QueryBindingSet(objectIter.next());

				Value v = next.getValue(objVar.getName());

				if (add(reportedValues, v)) {
					next.addBinding(subjectVar.getName(), v);
					return next;
				}
			}
		}
		else {
			QueryBindingSet next = result;
			result = null;
			return next;
		}

		// if we're done, throw away the cached list of values to avoid hogging
		// resources
		reportedValues = null;
		return null;
	}

	/**
	 * add param v to the set reportedValues2
	 * 
	 * @param reportedValues2
	 * @param v
	 * @return true if v added to set and not yet present
	 */
	protected boolean add(Set<Value> reportedValues2, Value v)
		throws QueryEvaluationException
	{
		return reportedValues2.add(v);
	}

	private CloseableIteration<BindingSet, QueryEvaluationException> createSubjectIteration()
		throws QueryEvaluationException
	{
		Var predicate = createAnonVar("zero-length-internal-pred");
		Var endVar = createAnonVar("zero-length-internal-end");

		StatementPattern subjects = new StatementPattern(subjectVar, predicate, endVar);

		if (contextVar != null) {
			subjects.setScope(Scope.NAMED_CONTEXTS);
			subjects.setContextVar(contextVar);
		}
		CloseableIteration<BindingSet, QueryEvaluationException> iter = evaluationStrategyImpl.evaluate(
				subjects, bindings);

		return iter;
	}

	private CloseableIteration<BindingSet, QueryEvaluationException> createObjectIteration()
		throws QueryEvaluationException
	{
		Var startVar = createAnonVar("zero-length-internal-start");
		Var predicate = createAnonVar("zero-length-internal-pred");

		StatementPattern subjects = new StatementPattern(startVar, predicate, objVar);
		if (contextVar != null) {
			subjects.setScope(Scope.NAMED_CONTEXTS);
			subjects.setContextVar(contextVar);
		}
		CloseableIteration<BindingSet, QueryEvaluationException> iter = evaluationStrategyImpl.evaluate(
				subjects, bindings);

		return iter;
	}

	/**
	 * @return
	 */
	private Set<Value> makeSet() {
		return new HashSet<Value>();
	}

	public Var createAnonVar(String varName) {
		Var var = new Var(varName);
		var.setAnonymous(true);
		return var;
	}
}