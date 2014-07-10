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

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.Difference;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Intersection;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * A query optimizer that prunes query model trees by removing superfluous parts
 * and/or by reducing complex parts with simpler parts.
 * 
 * @author Arjohn Kampman
 * @deprecated Replaced by {@link QueryModelNormalizer}.
 */
@Deprecated
public class QueryModelPruner implements QueryOptimizer {

	public QueryModelPruner() {
	}

	/**
	 * Applies generally applicable optimizations: path expressions are sorted
	 * from more to less specific.
	 * 
	 * @param tupleExpr
	 */
	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new TreeSanitizer());
	}

	protected static class TreeSanitizer extends QueryModelVisitorBase<RuntimeException> {

		@Override
		public void meet(Join join) {
			super.meet(join);

			TupleExpr leftArg = join.getLeftArg();
			TupleExpr rightArg = join.getRightArg();

			if (leftArg instanceof EmptySet || rightArg instanceof EmptySet) {
				join.replaceWith(new EmptySet());
			}
			else if (leftArg instanceof SingletonSet) {
				join.replaceWith(rightArg);
			}
			else if (rightArg instanceof SingletonSet) {
				join.replaceWith(leftArg);
			}
		}

		@Override
		public void meet(LeftJoin leftJoin) {
			super.meet(leftJoin);

			TupleExpr leftArg = leftJoin.getLeftArg();
			TupleExpr rightArg = leftJoin.getRightArg();
			ValueExpr condition = leftJoin.getCondition();

			if (leftArg instanceof EmptySet) {
				leftJoin.replaceWith(leftArg);
			}
			else if (rightArg instanceof EmptySet) {
				leftJoin.replaceWith(leftArg);
			}
			else if (rightArg instanceof SingletonSet) {
				leftJoin.replaceWith(leftArg);
			}
			else if (condition instanceof ValueConstant) {
				boolean conditionValue;
				try {
					conditionValue = QueryEvaluationUtil.getEffectiveBooleanValue(((ValueConstant)condition).getValue());
				}
				catch (ValueExprEvaluationException e) {
					conditionValue = false;
				}

				if (conditionValue == false) {
					// Constraint is always false
					leftJoin.replaceWith(leftArg);
				}
				else {
					leftJoin.setCondition(null);
				}
			}
		}

		@Override
		public void meet(Union union) {
			super.meet(union);

			TupleExpr leftArg = union.getLeftArg();
			TupleExpr rightArg = union.getRightArg();

			if (leftArg instanceof EmptySet) {
				union.replaceWith(rightArg);
			}
			else if (rightArg instanceof EmptySet) {
				union.replaceWith(leftArg);
			}
			else if (leftArg instanceof SingletonSet && rightArg instanceof SingletonSet) {
				union.replaceWith(leftArg);
			}
		}

		@Override
		public void meet(Difference difference) {
			super.meet(difference);

			TupleExpr leftArg = difference.getLeftArg();
			TupleExpr rightArg = difference.getRightArg();

			if (leftArg instanceof EmptySet) {
				difference.replaceWith(leftArg);
			}
			else if (rightArg instanceof EmptySet) {
				difference.replaceWith(leftArg);
			}
			else if (leftArg instanceof SingletonSet && rightArg instanceof SingletonSet) {
				difference.replaceWith(new EmptySet());
			}
		}

		@Override
		public void meet(Intersection intersection) {
			super.meet(intersection);

			TupleExpr leftArg = intersection.getLeftArg();
			TupleExpr rightArg = intersection.getRightArg();

			if (leftArg instanceof EmptySet || rightArg instanceof EmptySet) {
				intersection.replaceWith(new EmptySet());
			}
		}
	}
}
