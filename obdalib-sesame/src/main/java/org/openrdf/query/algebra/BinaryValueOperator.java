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
 * An abstract superclass for binary value operators which, by definition, has
 * two arguments.
 */
public abstract class BinaryValueOperator extends QueryModelNodeBase implements ValueExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The operator's left argument.
	 */
	protected ValueExpr leftArg;

	/**
	 * The operator's right argument.
	 */
	protected ValueExpr rightArg;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public BinaryValueOperator() {
	}

	/**
	 * Creates a new binary value operator.
	 * 
	 * @param leftArg
	 *        The operator's left argument, must not be <tt>null</tt>.
	 * @param rightArg
	 *        The operator's right argument, must not be <tt>null</tt>.
	 */
	public BinaryValueOperator(ValueExpr leftArg, ValueExpr rightArg) {
		setLeftArg(leftArg);
		setRightArg(rightArg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the left argument of this binary value operator.
	 * 
	 * @return The operator's left argument.
	 */
	public ValueExpr getLeftArg() {
		return leftArg;
	}

	/**
	 * Sets the left argument of this binary value operator.
	 * 
	 * @param leftArg
	 *        The (new) left argument for this operator, must not be
	 *        <tt>null</tt>.
	 */
	public void setLeftArg(ValueExpr leftArg) {
		assert leftArg != null : "leftArg must not be null";
		leftArg.setParentNode(this);
		this.leftArg = leftArg;
	}

	/**
	 * Gets the right argument of this binary value operator.
	 * 
	 * @return The operator's right argument.
	 */
	public ValueExpr getRightArg() {
		return rightArg;
	}

	/**
	 * Sets the right argument of this binary value operator.
	 * 
	 * @param rightArg
	 *        The (new) right argument for this operator, must not be
	 *        <tt>null</tt>.
	 */
	public void setRightArg(ValueExpr rightArg) {
		assert rightArg != null : "rightArg must not be null";
		rightArg.setParentNode(this);
		this.rightArg = rightArg;
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		leftArg.visit(visitor);
		rightArg.visit(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (leftArg == current) {
			setLeftArg((ValueExpr)replacement);
		}
		else if (rightArg == current) {
			setRightArg((ValueExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof BinaryValueOperator) {
			BinaryValueOperator o = (BinaryValueOperator)other;
			return leftArg.equals(o.getLeftArg()) && rightArg.equals(o.getRightArg());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return leftArg.hashCode() ^ rightArg.hashCode();
	}

	@Override
	public BinaryValueOperator clone() {
		BinaryValueOperator clone = (BinaryValueOperator)super.clone();
		clone.setLeftArg(getLeftArg().clone());
		clone.setRightArg(getRightArg().clone());
		return clone;
	}
}
