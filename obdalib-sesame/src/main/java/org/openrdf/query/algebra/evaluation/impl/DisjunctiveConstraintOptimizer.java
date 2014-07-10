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
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * A query optimizer that optimize disjunctive constraints on tuple expressions.
 * Currently, this optimizer {@link Union unions} a clone of the underlying
 * tuple expression with the original expression for each {@link SameTerm}
 * operator, moving the SameTerm to the cloned tuple expression.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class DisjunctiveConstraintOptimizer implements QueryOptimizer {

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new OrSameTermOptimizer());
	}

	protected static class OrSameTermOptimizer extends QueryModelVisitorBase<RuntimeException> {

		@Override
		public void meet(Filter filter) {
			if (filter.getCondition() instanceof Or && containsSameTerm(filter.getCondition())) {
				Or orNode = (Or)filter.getCondition();
				TupleExpr filterArg = filter.getArg();

				ValueExpr leftConstraint = orNode.getLeftArg();
				ValueExpr rightConstraint = orNode.getRightArg();

				// remove filter
				filter.replaceWith(filterArg);

				// Push UNION down below other filters to avoid cloning them
				TupleExpr node = findNotFilter(filterArg);

				Filter leftFilter = new Filter(node.clone(), leftConstraint);
				Filter rightFilter = new Filter(node.clone(), rightConstraint);
				Union union = new Union(leftFilter, rightFilter);
				node.replaceWith(union);

				filter.getParentNode().visit(this);
			}
			else {
				super.meet(filter);
			}
		}

		private TupleExpr findNotFilter(TupleExpr node) {
			if (node instanceof Filter) {
				return findNotFilter(((Filter)node).getArg());
			}
			return node;
		}

		private boolean containsSameTerm(ValueExpr node) {
			if (node instanceof SameTerm) {
				return true;
			}
			if (node instanceof Or) {
				Or or = (Or)node;
				boolean left = containsSameTerm(or.getLeftArg());
				return left || containsSameTerm(or.getRightArg());
			}
			if (node instanceof And) {
				And and = (And)node;
				boolean left = containsSameTerm(and.getLeftArg());
				return left || containsSameTerm(and.getRightArg());
			}
			return false;
		}
	}
}
