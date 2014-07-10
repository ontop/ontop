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

public abstract class SubQueryValueOperator extends QueryModelNodeBase implements ValueExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	protected TupleExpr subQuery;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SubQueryValueOperator() {
	}

	public SubQueryValueOperator(TupleExpr subQuery) {
		setSubQuery(subQuery);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public TupleExpr getSubQuery() {
		return subQuery;
	}

	public void setSubQuery(TupleExpr subQuery) {
		assert subQuery != null : "subQuery must not be null";
		subQuery.setParentNode(this);
		this.subQuery = subQuery;
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		subQuery.visit(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (subQuery == current) {
			setSubQuery((TupleExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof SubQueryValueOperator) {
			SubQueryValueOperator o = (SubQueryValueOperator)other;
			return subQuery.equals(o.getSubQuery());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return subQuery.hashCode();
	}

	@Override
	public SubQueryValueOperator clone() {
		SubQueryValueOperator clone = (SubQueryValueOperator)super.clone();
		clone.setSubQuery(getSubQuery().clone());
		return clone;
	}
}
