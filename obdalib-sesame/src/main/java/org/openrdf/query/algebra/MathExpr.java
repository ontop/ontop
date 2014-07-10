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
 * A mathematical expression consisting an operator and two arguments.
 */
public class MathExpr extends BinaryValueOperator {

	/*---------------*
	 * enum Operator *
	 *---------------*/

	public enum MathOp {
		PLUS("+"),
		MINUS("-"),
		MULTIPLY("*"),
		DIVIDE("/");

		private String symbol;

		MathOp(String symbol) {
			this.symbol = symbol;
		}

		public String getSymbol() {
			return symbol;
		}
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private MathOp operator;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public MathExpr() {
	}

	public MathExpr(ValueExpr leftArg, ValueExpr rightArg, MathOp operator) {
		super(leftArg, rightArg);
		setOperator(operator);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public MathOp getOperator() {
		return operator;
	}

	public void setOperator(MathOp operator) {
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
		if (other instanceof MathExpr && super.equals(other)) {
			MathExpr o = (MathExpr)other;
			return operator.equals(o.getOperator());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ operator.hashCode();
	}

	@Override
	public MathExpr clone() {
		return (MathExpr)super.clone();
	}
}
