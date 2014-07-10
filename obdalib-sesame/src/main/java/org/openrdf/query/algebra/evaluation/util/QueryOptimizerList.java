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
package org.openrdf.query.algebra.evaluation.util;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;

/**
 * A query optimizer that contains a list of other query optimizers, which are
 * called consecutively when the list's {@link #optimize(TupleExpr, Dataset, BindingSet)}
 * method is called.
 * 
 * @author Arjohn Kampman
 */
public class QueryOptimizerList implements QueryOptimizer {

	protected List<QueryOptimizer> optimizers;

	public QueryOptimizerList() {
		this.optimizers = new ArrayList<QueryOptimizer>(8);
	}

	public QueryOptimizerList(List<QueryOptimizer> optimizers) {
		this.optimizers = new ArrayList<QueryOptimizer>(optimizers);
	}

	public QueryOptimizerList(QueryOptimizer... optimizers) {
		this.optimizers = new ArrayList<QueryOptimizer>(optimizers.length);
		for (QueryOptimizer optimizer : optimizers) {
			this.optimizers.add(optimizer);
		}
	}

	public void add(QueryOptimizer optimizer) {
		optimizers.add(optimizer);
	}

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		for (QueryOptimizer optimizer : optimizers) {
			optimizer.optimize(tupleExpr, dataset, bindings);
		}
	}
}
