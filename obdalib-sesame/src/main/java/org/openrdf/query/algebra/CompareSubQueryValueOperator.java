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

public abstract class CompareSubQueryValueOperator extends SubQueryValueOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	protected ValueExpr arg;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public CompareSubQueryValueOperator() {
	}

	public CompareSubQueryValueOperator(ValueExpr valueExpr, TupleExpr subQuery) {
		super(subQuery);
		setArg(valueExpr);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ValueExpr getArg() {
		return arg;
	}

	public void setArg(ValueExpr arg) {
		assert arg != null : "arg must not be null";
		arg.setParentNode(this);
		this.arg = arg;
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		arg.visit(visitor);
		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (arg == current) {
			setArg((ValueExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof CompareSubQueryValueOperator && super.equals(other)) {
			CompareSubQueryValueOperator o = (CompareSubQueryValueOperator)other;
			return arg.equals(o.getArg());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ arg.hashCode();
	}

	@Override
	public CompareSubQueryValueOperator clone() {
		CompareSubQueryValueOperator clone = (CompareSubQueryValueOperator)super.clone();
		clone.setArg(getArg().clone());
		return clone;
	}
}
