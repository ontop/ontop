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

import gr.uoa.di.madgik.sesame.functions.SpatialOverlapFunc;

import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * A query optimizer that replaces {@link Compare} operators with
 * {@link SameTerm}s, if possible.
 * 
 * @author Arjohn Kampman
 */
public class CompareOptimizer implements QueryOptimizer {

	/**
	 * Applies generally applicable optimizations to the supplied query: variable
	 * assignments are inlined.
	 */
	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new CompareVisitor());
	}

	protected static class CompareVisitor extends QueryModelVisitorBase<RuntimeException> {

		@Override
		public void meet(Compare compare) {
			super.meet(compare);

			if (compare.getOperator() == CompareOp.EQ) {
				ValueExpr leftArg = compare.getLeftArg();
				ValueExpr rightArg = compare.getRightArg();

				boolean leftIsVar = isVar(leftArg);
				boolean rightIsVar = isVar(rightArg);
				boolean leftIsResource = isResource(leftArg);
				boolean rightIsResource = isResource(rightArg);

				if (leftIsVar && rightIsResource || leftIsResource && rightIsVar || leftIsResource
						&& rightIsResource)
				{
					SameTerm sameTerm = new SameTerm(leftArg, rightArg);
					compare.replaceWith(sameTerm);
				}
			}
		}

		protected boolean isVar(ValueExpr valueExpr) {
			if (valueExpr instanceof Var) {
				return true;
			}

			return false;
		}

		protected boolean isResource(ValueExpr valueExpr) {
			if (valueExpr instanceof ValueConstant) {
				Value value = ((ValueConstant)valueExpr).getValue();
				return value instanceof Resource;
			}

			if (valueExpr instanceof Var) {
				Value value = ((Var)valueExpr).getValue();
				return value instanceof Resource;
			}

			return false;
		}


	}
}
