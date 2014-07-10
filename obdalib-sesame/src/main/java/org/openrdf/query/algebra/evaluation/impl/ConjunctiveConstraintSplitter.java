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

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.algebra.helpers.VarNameCollector;

/**
 * Splits conjunctive constraints into seperate constraints.
 * 
 * @author Arjohn Kampman
 */
public class ConjunctiveConstraintSplitter implements QueryOptimizer {

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new ConstraintVisitor(tupleExpr));
	}

	protected static class ConstraintVisitor extends QueryModelVisitorBase<RuntimeException> {

		protected final TupleExpr tupleExpr;

		public ConstraintVisitor(TupleExpr tupleExpr) {
			this.tupleExpr = tupleExpr;
		}

		@Override
		public void meet(Filter filter)
		{
			super.meet(filter);

			List<ValueExpr> conjunctiveConstraints = new ArrayList<ValueExpr>(16);
			getConjunctiveConstraints(filter.getCondition(), conjunctiveConstraints);

			TupleExpr filterArg = filter.getArg();

			for (int i = conjunctiveConstraints.size() - 1; i >= 1; i--) {
				Filter newFilter = new Filter(filterArg, conjunctiveConstraints.get(i));
				filterArg = newFilter;
			}

			filter.setCondition(conjunctiveConstraints.get(0));
			filter.setArg(filterArg);
		}

		@Override
		public void meet(LeftJoin node) {
			super.meet(node);

			if (node.getCondition() != null) {
				List<ValueExpr> conjunctiveConstraints = new ArrayList<ValueExpr>(16);
				getConjunctiveConstraints(node.getCondition(), conjunctiveConstraints);
	
				TupleExpr arg = node.getRightArg();
				ValueExpr condition = null;
	
				for (ValueExpr constraint : conjunctiveConstraints) {
					if (isWithinBindingScope(constraint, arg)) {
						arg = new Filter(arg, constraint);
					} else if (condition == null) {
						condition = constraint;
					} else {
						condition = new And(condition, constraint);
					}
				}
	
				node.setCondition(condition);
				node.setRightArg(arg);
			}
		}

		protected void getConjunctiveConstraints(ValueExpr valueExpr, List<ValueExpr> conjunctiveConstraints) {
			if (valueExpr instanceof And) {
				And and = (And)valueExpr;
				getConjunctiveConstraints(and.getLeftArg(), conjunctiveConstraints);
				getConjunctiveConstraints(and.getRightArg(), conjunctiveConstraints);
			}
			else {
				conjunctiveConstraints.add(valueExpr);
			}
		}

		private boolean isWithinBindingScope(ValueExpr condition, TupleExpr node) {
			return node.getBindingNames().containsAll(VarNameCollector.process(condition));
		}
	}
}
