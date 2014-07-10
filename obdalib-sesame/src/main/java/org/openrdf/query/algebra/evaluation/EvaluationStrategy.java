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
package org.openrdf.query.algebra.evaluation;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;

/**
 * Evaluates {@link TupleExpr}s and {@link ValueExpr}s.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public interface EvaluationStrategy {

	/**
	 * Evaluates the tuple expression against the supplied triple source with the
	 * specified set of variable bindings as input.
	 * 
	 * @param expr
	 *        The Tuple Expression to evaluate
	 * @param bindings
	 *        The variables bindings to use for evaluating the expression, if
	 *        applicable.
	 * @return A closeable iterator over the variable binding sets that match the
	 *         tuple expression.
	 */
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(TupleExpr expr,
			BindingSet bindings)
		throws QueryEvaluationException;

	/**
	 * Gets the value of this expression.
	 * 
	 * @param bindings
	 *        The variables bindings to use for evaluating the expression, if
	 *        applicable.
	 * @return The Value that this expression evaluates to, or <tt>null</tt> if
	 *         the expression could not be evaluated.
	 */
	public Value evaluate(ValueExpr expr, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException;

	/**
	 * Evaluates the boolean expression on the supplied TripleSource object.
	 * 
	 * @param bindings
	 *        The variables bindings to use for evaluating the expression, if
	 *        applicable.
	 * @return The result of the evaluation.
	 * @throws ValueExprEvaluationException
	 *         If the value expression could not be evaluated, for example when
	 *         comparing two incompatible operands. When thrown, the result of
	 *         the boolean expression is neither <tt>true</tt> nor
	 *         <tt>false</tt>, but unknown.
	 */
	public boolean isTrue(ValueExpr expr, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException;
}
