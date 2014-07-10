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
 * An abstract superclass for unary value operators which, by definition, has
 * one argument.
 */
public abstract class UnaryValueOperator extends QueryModelNodeBase implements ValueExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The operator's argument.
	 */
	protected ValueExpr arg;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new empty unary value operator.
	 */
	public UnaryValueOperator() {
	}

	/**
	 * Creates a new unary value operator.
	 * 
	 * @param arg
	 *        The operator's argument, must not be <tt>null</tt>.
	 */
	public UnaryValueOperator(ValueExpr arg) {
		setArg(arg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the argument of this unary value operator.
	 * 
	 * @return The operator's argument.
	 */
	public ValueExpr getArg() {
		return arg;
	}

	/**
	 * Sets the argument of this unary value operator.
	 * 
	 * @param arg
	 *        The (new) argument for this operator, must not be <tt>null</tt>.
	 */
	public void setArg(ValueExpr arg) {
		assert arg != null : "arg must not be null";
		arg.setParentNode(this);
		this.arg = arg;
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		if (arg != null) {
			arg.visit(visitor);
		}
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
		if (other instanceof UnaryValueOperator) {
			UnaryValueOperator o = (UnaryValueOperator)other;
			return (arg == null && o.getArg() == null) || (arg != null && arg.equals(o.getArg()));
		}

		return false;
	}

	@Override
	public int hashCode() {
		return arg.hashCode();
	}

	@Override
	public UnaryValueOperator clone() {
		UnaryValueOperator clone = (UnaryValueOperator)super.clone();
		if (getArg() != null) {
			clone.setArg(getArg().clone());
		}
		return clone;
	}
}
