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

import org.openrdf.query.algebra.ArbitraryLengthPath;
import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.BindingSetAssignment;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.ZeroLengthPath;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * Supplies various query model statistics to the query engine/optimizer.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class EvaluationStatistics {

	protected CardinalityCalculator cc;

	public synchronized double getCardinality(TupleExpr expr) {
		if (cc == null) {
			cc = createCardinalityCalculator();
		}

		expr.visit(cc);
		return cc.getCardinality();
	}

	protected CardinalityCalculator createCardinalityCalculator() {
		return new CardinalityCalculator();
	}

	/*-----------------------------------*
	 * Inner class CardinalityCalculator *
	 *-----------------------------------*/

	protected static class CardinalityCalculator extends QueryModelVisitorBase<RuntimeException> {

		protected double cardinality;

		public double getCardinality() {
			return cardinality;
		}

		@Override
		public void meet(EmptySet node) {
			cardinality = 0;
		}

		@Override
		public void meet(SingletonSet node) {
			cardinality = 1;
		}

		@Override
		public void meet(BindingSetAssignment node) {
			cardinality = 0;
		}

		@Override
		public void meet(ZeroLengthPath node) {
			// cardinality is the same as that of a statement pattern with three unbound vars. 
			cardinality = 1000.0;
		}

		@Override
		public void meet(ArbitraryLengthPath node) {

			List<Var> vars = new ArrayList<Var>();
			vars.add(node.getSubjectVar());
			vars.add(node.getObjectVar());

			int constantVarCount = countConstantVars(vars);
			double unboundVarFactor = (double)(node.getBindingNames().size() - constantVarCount)
					/ node.getBindingNames().size();
			
			cardinality = Math.pow(1000.0, unboundVarFactor);
		}

		@Override
		public void meet(Service node) {
			if (!node.getServiceRef().hasValue()) {
				// the URI is not available, may be computed in the course of the
				// query
				// => use high cost to order the SERVICE node late in the query plan
				cardinality = 100000;
			}
			else {
				ServiceNodeAnalyzer serviceAnalyzer = new ServiceNodeAnalyzer();
				node.visitChildren(serviceAnalyzer);
				int count = serviceAnalyzer.getStatementCount();

				// more than one free variable in a single triple pattern
				if (count == 1 && node.getServiceVars().size() > 1) {
					cardinality = 100 + node.getServiceVars().size(); // TODO (should
																						// be higher
																						// than other
																						// simple
																						// stmts)
				}
				else {
					// only very selective statements should be better than this
					// => evaluate service expressions first
					cardinality = 1 + (node.getServiceVars().size() * 0.1);
				}
			}
		}

		@Override
		public void meet(StatementPattern sp) {
			cardinality = getCardinality(sp);
		}

		protected double getCardinality(StatementPattern sp) {
			List<Var> vars = sp.getVarList();
			int constantVarCount = countConstantVars(vars);
			double unboundVarFactor = (double)(vars.size() - constantVarCount) / vars.size();
			return Math.pow(1000.0, unboundVarFactor);
		}

		protected int countConstantVars(Iterable<Var> vars) {
			int constantVarCount = 0;

			for (Var var : vars) {
				if (var.hasValue()) {
					constantVarCount++;
				}
			}

			return constantVarCount;
		}

		@Override
		public void meet(Join node) {
			node.getLeftArg().visit(this);
			double leftArgCost = this.cardinality;

			node.getRightArg().visit(this);
			cardinality *= leftArgCost;
		}

		@Override
		public void meet(LeftJoin node) {
			node.getLeftArg().visit(this);
			double leftArgCost = this.cardinality;

			node.getRightArg().visit(this);
			cardinality *= leftArgCost;
		}

		@Override
		protected void meetBinaryTupleOperator(BinaryTupleOperator node) {
			node.getLeftArg().visit(this);
			double leftArgCost = this.cardinality;

			node.getRightArg().visit(this);
			cardinality += leftArgCost;
		}

		@Override
		protected void meetUnaryTupleOperator(UnaryTupleOperator node) {
			node.getArg().visit(this);
		}

		@Override
		protected void meetNode(QueryModelNode node) {
			if (node instanceof ExternalSet) {
				meetExternalSet((ExternalSet)node);
			}
			else {
				throw new IllegalArgumentException("Unhandled node type: " + node.getClass());
			}
		}

		protected void meetExternalSet(ExternalSet node) {
			cardinality = node.cardinality();
		}
	}

	// count the number of triple patterns
	private static class ServiceNodeAnalyzer extends QueryModelVisitorBase<RuntimeException> {

		private int count = 0;

		public int getStatementCount() {
			return count;
		}

		@Override
		public void meet(StatementPattern node)
			throws RuntimeException
		{
			count++;
		}
	};
}
