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

import org.openrdf.query.algebra.Compare.CompareOp;

/**
 */
public class CompareAll extends CompareSubQueryValueOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private CompareOp operator;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public CompareAll() {
	}

	public CompareAll(ValueExpr valueExpr, TupleExpr subQuery, CompareOp operator) {
		super(valueExpr, subQuery);
		setOperator(operator);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public CompareOp getOperator() {
		return operator;
	}

	public void setOperator(CompareOp operator) {
		assert operator != null : "operator must not be null";
		this.operator = operator;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public String getSignature() {
		return super.getSignature() + " (" + operator.getSymbol() + ")";
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof CompareAll && super.equals(other)) {
			CompareAll o = (CompareAll)other;
			return operator.equals(o.getOperator());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ operator.hashCode() ^ "CompareAll".hashCode();
	}

	@Override
	public CompareAll clone() {
		return (CompareAll)super.clone();
	}
}
