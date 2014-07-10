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
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * @author Arjohn Kampman
 */
public class IterativeEvaluationOptimizer implements QueryOptimizer {

	public IterativeEvaluationOptimizer() {
	}

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new IEOVisitor());
	}

	protected static class IEOVisitor extends QueryModelVisitorBase<RuntimeException> {

		@Override
		public void meet(Union union) {
			super.meet(union);

			TupleExpr leftArg = union.getLeftArg();
			TupleExpr rightArg = union.getRightArg();

			if (leftArg instanceof Join && rightArg instanceof Join) {
				Join leftJoinArg = (Join)leftArg;
				Join rightJoin = (Join)rightArg;

				if (leftJoinArg.getLeftArg().equals(rightJoin.getLeftArg())) {
					// factor out the left-most join argument
					Join newJoin = new Join();
					union.replaceWith(newJoin);
					newJoin.setLeftArg(leftJoinArg.getLeftArg());
					newJoin.setRightArg(union);
					union.setLeftArg(leftJoinArg.getRightArg());
					union.setRightArg(rightJoin.getRightArg());

					union.visit(this);
				}
			}
		}
	}
}