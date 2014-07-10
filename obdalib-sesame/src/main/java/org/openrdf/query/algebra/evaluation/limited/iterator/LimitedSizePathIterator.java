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

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.StatementPattern.Scope;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.algebra.evaluation.iterator.PathIteration;

/**
 * @author Jerven Bolleman, SIB Swiss Institute of Bioinformatics
 */
public class LimitedSizePathIterator extends PathIteration {

	private final AtomicLong used;
	private final long maxSize;

	/**
	 * @param evaluationStrategyImpl
	 * @param scope
	 * @param startVar
	 * @param pathExpression
	 * @param endVar
	 * @param contextVar
	 * @param minLength
	 * @param bindings
	 * @throws QueryEvaluationException
	 */
	public LimitedSizePathIterator(EvaluationStrategyImpl evaluationStrategyImpl, Scope scope, Var startVar,
			TupleExpr pathExpression, Var endVar, Var contextVar, long minLength, BindingSet bindings,
			AtomicLong used, long maxSize)
		throws QueryEvaluationException
	{
		super(evaluationStrategyImpl, scope, startVar, pathExpression, endVar, contextVar, minLength, bindings);
		this.used = used;
		this.maxSize = maxSize;
	}

	@Override
	protected boolean add(Set<ValuePair> valueSet, ValuePair vp)
		throws QueryEvaluationException
	{
		return LimitedSizeIteratorUtil.<ValuePair>add(vp, valueSet, used, maxSize);
	}

}
