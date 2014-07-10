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
 * @author Arjohn Kampman
 */
public class OrderElem extends QueryModelNodeBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueExpr expr;

	private boolean ascending = true;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public OrderElem() {
	}

	public OrderElem(ValueExpr expr) {
		this(expr, true);
	}

	public OrderElem(ValueExpr expr, boolean ascending) {
		setExpr(expr);
		setAscending(ascending);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ValueExpr getExpr() {
		return expr;
	}

	public void setExpr(ValueExpr expr) {
		assert expr != null : "expr must not be null";
		expr.setParentNode(this);
		this.expr = expr;
	}

	public boolean isAscending() {
		return ascending;
	}

	public void setAscending(boolean ascending) {
		this.ascending = ascending;
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
		expr.visit(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (expr == current) {
			setExpr((ValueExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public String getSignature() {
		return super.getSignature() + " (" + (ascending ? "ASC" : "DESC") + ")";
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof OrderElem) {
			OrderElem o = (OrderElem)other;
			return ascending == o.isAscending() && expr.equals(o.getExpr());
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = expr.hashCode();
		if (ascending) {
			result = ~result;
		}
		return result;
	}

	@Override
	public OrderElem clone() {
		OrderElem clone = (OrderElem)super.clone();
		clone.setExpr(getExpr().clone());
		return clone;
	}
}
