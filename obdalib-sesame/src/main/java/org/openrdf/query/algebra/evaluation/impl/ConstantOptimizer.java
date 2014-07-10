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
package org.openrdf.query.algebra.evaluation.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.Value;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.BinaryValueOperator;
import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.If;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryValueOperator;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;
import org.openrdf.query.algebra.evaluation.function.numeric.Rand;
import org.openrdf.query.algebra.evaluation.function.rdfterm.STRUUID;
import org.openrdf.query.algebra.evaluation.function.rdfterm.UUID;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.impl.EmptyBindingSet;

/**
 * A query optimizer that optimizes constant value expressions.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 */
public class ConstantOptimizer implements QueryOptimizer {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected final EvaluationStrategy strategy;

	public ConstantOptimizer(EvaluationStrategy strategy) {
		this.strategy = strategy;
	}

	/**
	 * Applies generally applicable optimizations to the supplied query: variable
	 * assignments are inlined.
	 */
	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new ConstantVisitor());
	}

	protected class ConstantVisitor extends QueryModelVisitorBase<RuntimeException> {

		@Override
		public void meet(Or or) {
			or.visitChildren(this);

			try {
				if (isConstant(or.getLeftArg()) && isConstant(or.getRightArg())) {
					boolean value = strategy.isTrue(or, EmptyBindingSet.getInstance());
					or.replaceWith(new ValueConstant(BooleanLiteralImpl.valueOf(value)));
				}
				else if (isConstant(or.getLeftArg())) {
					boolean leftIsTrue = strategy.isTrue(or.getLeftArg(), EmptyBindingSet.getInstance());
					if (leftIsTrue) {
						or.replaceWith(new ValueConstant(BooleanLiteralImpl.TRUE));
					}
					else {
						or.replaceWith(or.getRightArg());
					}
				}
				else if (isConstant(or.getRightArg())) {
					boolean rightIsTrue = strategy.isTrue(or.getRightArg(), EmptyBindingSet.getInstance());
					if (rightIsTrue) {
						or.replaceWith(new ValueConstant(BooleanLiteralImpl.TRUE));
					}
					else {
						or.replaceWith(or.getLeftArg());
					}
				}
			}
			catch (ValueExprEvaluationException e) {
				// TODO: incompatible values types(?), remove the affected part of
				// the query tree
				logger.debug("Failed to evaluate BinaryValueOperator with two constant arguments", e);
			}
			catch (QueryEvaluationException e) {
				logger.error("Query evaluation exception caught", e);
			}
		}

		@Override
		public void meet(And and) {
			and.visitChildren(this);

			try {
				if (isConstant(and.getLeftArg()) && isConstant(and.getRightArg())) {
					boolean value = strategy.isTrue(and, EmptyBindingSet.getInstance());
					and.replaceWith(new ValueConstant(BooleanLiteralImpl.valueOf(value)));
				}
				else if (isConstant(and.getLeftArg())) {
					boolean leftIsTrue = strategy.isTrue(and.getLeftArg(), EmptyBindingSet.getInstance());
					if (leftIsTrue) {
						and.replaceWith(and.getRightArg());
					}
					else {
						and.replaceWith(new ValueConstant(BooleanLiteralImpl.FALSE));
					}
				}
				else if (isConstant(and.getRightArg())) {
					boolean rightIsTrue = strategy.isTrue(and.getRightArg(), EmptyBindingSet.getInstance());
					if (rightIsTrue) {
						and.replaceWith(and.getLeftArg());
					}
					else {
						and.replaceWith(new ValueConstant(BooleanLiteralImpl.FALSE));
					}
				}
			}
			catch (ValueExprEvaluationException e) {
				// TODO: incompatible values types(?), remove the affected part of
				// the query tree
				logger.debug("Failed to evaluate BinaryValueOperator with two constant arguments", e);
			}
			catch (QueryEvaluationException e) {
				logger.error("Query evaluation exception caught", e);
			}
		}

		@Override
		protected void meetBinaryValueOperator(BinaryValueOperator binaryValueOp) {
			super.meetBinaryValueOperator(binaryValueOp);

			if (isConstant(binaryValueOp.getLeftArg()) && isConstant(binaryValueOp.getRightArg())) {
				try {
					Value value = strategy.evaluate(binaryValueOp, EmptyBindingSet.getInstance());
					binaryValueOp.replaceWith(new ValueConstant(value));
				}
				catch (ValueExprEvaluationException e) {
					// TODO: incompatible values types(?), remove the affected part
					// of the query tree
					logger.debug("Failed to evaluate BinaryValueOperator with two constant arguments", e);
				}
				catch (QueryEvaluationException e) {
					logger.error("Query evaluation exception caught", e);
				}
			}
		}

		@Override
		protected void meetUnaryValueOperator(UnaryValueOperator unaryValueOp) {
			super.meetUnaryValueOperator(unaryValueOp);

			if (isConstant(unaryValueOp.getArg())) {
				try {
					Value value = strategy.evaluate(unaryValueOp, EmptyBindingSet.getInstance());
					unaryValueOp.replaceWith(new ValueConstant(value));
				}
				catch (ValueExprEvaluationException e) {
					// TODO: incompatible values types(?), remove the affected part
					// of the query tree
					logger.debug("Failed to evaluate UnaryValueOperator with a constant argument", e);
				}
				catch (QueryEvaluationException e) {
					logger.error("Query evaluation exception caught", e);
				}
			}
		}

		@Override
		public void meet(FunctionCall functionCall) {
			super.meet(functionCall);

			List<ValueExpr> args = functionCall.getArgs();

			if (args.size() == 0) {
				/* SPARQL has two types of zero-arg function. One are proper 'constant' functions like NOW() which generate a single value
				 *  for the entire query and which can be safely optimized to a constant. Other functions, like RAND(), UUID() and STRUUID(), 
				 *  are a special case: they are expected to yield a new value on every call, and can therefore not be replaced by a constant.
				 */
				if (!isConstantZeroArgFunction(functionCall)) {
					return;
				}
			}
			else {
				for (ValueExpr arg : args) {
					if (!isConstant(arg)) {
						return;
					}
				}
			}

			// All arguments are constant

			try {
				Value value = strategy.evaluate(functionCall, EmptyBindingSet.getInstance());
				functionCall.replaceWith(new ValueConstant(value));
			}
			catch (ValueExprEvaluationException e) {
				// TODO: incompatible values types(?), remove the affected part of
				// the query tree
				logger.debug("Failed to evaluate BinaryValueOperator with two constant arguments", e);
			}
			catch (QueryEvaluationException e) {
				logger.error("Query evaluation exception caught", e);
			}
		}

		/**
		 * Determines if the provided zero-arg function is a function that should
		 * return a constant value for the entire query execution (e.g NOW()), or
		 * if it should generate a new value for every call (e.g. RAND()).
		 * 
		 * @param functionCall
		 *        a zero-arg function call.
		 * @return <code>true<code> iff the provided function returns a constant value for the query execution, <code>false</code>
		 *         otherwise.
		 */
		private boolean isConstantZeroArgFunction(FunctionCall functionCall) {
			Function function = FunctionRegistry.getInstance().get(functionCall.getURI());

			// we treat constant functions as the 'regular case' and make
			// exceptions for specific SPARQL built-in functions that require
			// different treatment.
			if (function instanceof Rand || function instanceof UUID || function instanceof STRUUID) {
				return false;
			}

			return true;
		}

		@Override
		public void meet(Bound bound) {
			super.meet(bound);

			if (bound.getArg().hasValue()) {
				// variable is always bound
				bound.replaceWith(new ValueConstant(BooleanLiteralImpl.TRUE));
			}
		}

		@Override
		public void meet(If node) {
			super.meet(node);

			if (isConstant(node.getCondition())) {
				try {
					if (strategy.isTrue(node.getCondition(), EmptyBindingSet.getInstance())) {
						node.replaceWith(node.getResult());
					}
					else {
						node.replaceWith(node.getAlternative());
					}
				}
				catch (ValueExprEvaluationException e) {
					logger.debug("Failed to evaluate UnaryValueOperator with a constant argument", e);
				}
				catch (QueryEvaluationException e) {
					logger.error("Query evaluation exception caught", e);
				}
			}
		}

		/**
		 * Override meetBinaryValueOperator
		 */
		@Override
		public void meet(Regex node) {
			super.meetNode(node);

			if (isConstant(node.getArg()) && isConstant(node.getPatternArg()) && isConstant(node.getFlagsArg()))
			{
				try {
					Value value = strategy.evaluate(node, EmptyBindingSet.getInstance());
					node.replaceWith(new ValueConstant(value));
				}
				catch (ValueExprEvaluationException e) {
					logger.debug("Failed to evaluate BinaryValueOperator with two constant arguments", e);
				}
				catch (QueryEvaluationException e) {
					logger.error("Query evaluation exception caught", e);
				}
			}
		}

		private boolean isConstant(ValueExpr expr) {
			return expr instanceof ValueConstant || expr instanceof Var && ((Var)expr).hasValue();
		}
	}
}
