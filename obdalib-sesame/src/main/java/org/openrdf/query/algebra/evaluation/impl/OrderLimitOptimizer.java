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
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * Moves the Order node above the Projection when variables are projected.
 * 
 * @author James Leigh
 */
public class OrderLimitOptimizer implements QueryOptimizer {

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new OrderOptimizer());
	}

	protected static class OrderOptimizer extends QueryModelVisitorBase<RuntimeException> {

		private boolean variablesProjected = true;

		private Projection projection;

		@Override
		public void meet(Projection node) {
			projection = node;
			node.getArg().visit(this);
			projection = null;
		}

		@Override
		public void meet(Order node) {
			for (OrderElem e : node.getElements()) {
				e.visit(this);
			}
			if (variablesProjected) {
				QueryModelNode parent = node.getParentNode();
				if (projection == parent) {
					node.replaceWith(node.getArg().clone());
					node.setArg(projection.clone());
					Order replacement = node.clone();
					projection.replaceWith(replacement);
					QueryModelNode distinct = replacement.getParentNode();
					if (distinct instanceof Distinct) {
						distinct.replaceWith(new Reduced(replacement.clone()));
					}
				}
			}
		}

		@Override
		public void meet(Var node) {
			if (projection != null) {
				boolean projected = false;
				for (ProjectionElem e : projection.getProjectionElemList().getElements()) {
					String source = e.getSourceName();
					String target = e.getTargetName();
					if (node.getName().equals(source) && node.getName().equals(target)) {
						projected = true;
						break;
					}
				}
				if (!projected) {
					variablesProjected = false;
				}
			}
		}

	}
}
