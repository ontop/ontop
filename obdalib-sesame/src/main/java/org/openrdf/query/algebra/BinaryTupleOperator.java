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
 * An abstract superclass for binary tuple operators which, by definition, has
 * two arguments.
 */
public abstract class BinaryTupleOperator extends QueryModelNodeBase implements TupleExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The operator's left argument.
	 */
	protected TupleExpr leftArg;

	/**
	 * The operator's right argument.
	 */
	protected TupleExpr rightArg;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public BinaryTupleOperator() {
	}

	/**
	 * Creates a new binary tuple operator.
	 * 
	 * @param leftArg
	 *        The operator's left argument, must not be <tt>null</tt>.
	 * @param rightArg
	 *        The operator's right argument, must not be <tt>null</tt>.
	 */
	public BinaryTupleOperator(TupleExpr leftArg, TupleExpr rightArg) {
		setLeftArg(leftArg);
		setRightArg(rightArg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the left argument of this binary tuple operator.
	 * 
	 * @return The operator's left argument.
	 */
	public TupleExpr getLeftArg() {
		return leftArg;
	}

	/**
	 * Sets the left argument of this binary tuple operator.
	 * 
	 * @param leftArg
	 *        The (new) left argument for this operator, must not be
	 *        <tt>null</tt>.
	 */
	public void setLeftArg(TupleExpr leftArg) {
		assert leftArg != null : "leftArg must not be null";
		leftArg.setParentNode(this);
		this.leftArg = leftArg;
	}

	/**
	 * Gets the right argument of this binary tuple operator.
	 * 
	 * @return The operator's right argument.
	 */
	public TupleExpr getRightArg() {
		return rightArg;
	}

	/**
	 * Sets the right argument of this binary tuple operator.
	 * 
	 * @param rightArg
	 *        The (new) right argument for this operator, must not be
	 *        <tt>null</tt>.
	 */
	public void setRightArg(TupleExpr rightArg) {
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
			setLeftArg((TupleExpr)replacement);
		}
		else if (rightArg == current) {
			setRightArg((TupleExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof BinaryTupleOperator) {
			BinaryTupleOperator o = (BinaryTupleOperator)other;
			return leftArg.equals(o.getLeftArg()) && rightArg.equals(o.getRightArg());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return leftArg.hashCode() ^ rightArg.hashCode();
	}

	@Override
	public BinaryTupleOperator clone() {
		BinaryTupleOperator clone = (BinaryTupleOperator)super.clone();
		clone.setLeftArg(getLeftArg().clone());
		clone.setRightArg(getRightArg().clone());
		return clone;
	}
}
