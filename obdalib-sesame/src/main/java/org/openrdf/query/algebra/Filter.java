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
package org.openrdf.query.algebra;

/**
 * The FILTER operator, as defined in <a
 * href="http://www.w3.org/TR/rdf-sparql-query/#defn_algFilter">SPARQL Query
 * Language for RDF</a>. The FILTER operator filters specific results from the
 * underlying tuple expression based on a configurable condition.
 * 
 * @author Arjohn Kampman
 */
public class Filter extends UnaryTupleOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueExpr condition;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Filter() {
	}

	public Filter(TupleExpr arg, ValueExpr condition) {
		super(arg);
		setCondition(condition);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ValueExpr getCondition() {
		return condition;
	}

	public void setCondition(ValueExpr condition) {
		assert condition != null : "condition must not be null";
		condition.setParentNode(this);
		this.condition = condition;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		condition.visit(visitor);
		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (condition == current) {
			setCondition((ValueExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Filter && super.equals(other)) {
			Filter o = (Filter)other;
			return condition.equals(o.getCondition());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ condition.hashCode();
	}

	@Override
	public Filter clone() {
		Filter clone = (Filter)super.clone();
		clone.setCondition(getCondition().clone());
		return clone;
	}
}
