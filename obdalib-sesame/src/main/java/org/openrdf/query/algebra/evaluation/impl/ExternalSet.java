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

import java.util.Collections;
import java.util.Set;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.QueryModelNodeBase;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.query.algebra.TupleExpr;

/**
 * @author James Leigh
 */
public abstract class ExternalSet extends QueryModelNodeBase implements TupleExpr {

	private static final long serialVersionUID = 3903453394409442226L;

	public Set<String> getBindingNames() {
		return Collections.emptySet();
	}

	public Set<String> getAssuredBindingNames() {
		return Collections.emptySet();
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meetOther(this);
	}

	@Override
	public ExternalSet clone() {
		return (ExternalSet)super.clone();
	}

	public double cardinality() {
		return 1;
	}

	public abstract CloseableIteration<BindingSet, QueryEvaluationException> evaluate(BindingSet bindings)
		throws QueryEvaluationException;

}
