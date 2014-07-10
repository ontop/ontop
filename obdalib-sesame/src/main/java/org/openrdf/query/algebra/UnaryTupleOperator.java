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

import java.util.Set;

/**
 * An abstract superclass for unary tuple operators which, by definition, has
 * one argument.
 */
public abstract class UnaryTupleOperator extends QueryModelNodeBase implements TupleExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The operator's argument.
	 */
	protected TupleExpr arg;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public UnaryTupleOperator() {
	}

	/**
	 * Creates a new unary tuple operator.
	 * 
	 * @param arg
	 *        The operator's argument, must not be <tt>null</tt>.
	 */
	public UnaryTupleOperator(TupleExpr arg) {
		setArg(arg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the argument of this unary tuple operator.
	 * 
	 * @return The operator's argument.
	 */
	public TupleExpr getArg() {
		return arg;
	}

	/**
	 * Sets the argument of this unary tuple operator.
	 * 
	 * @param arg
	 *        The (new) argument for this operator, must not be <tt>null</tt>.
	 */
	public void setArg(TupleExpr arg) {
		assert arg != null : "arg must not be null";
		arg.setParentNode(this);
		this.arg = arg;
	}

	public Set<String> getBindingNames() {
		return getArg().getBindingNames();
	}

	public Set<String> getAssuredBindingNames() {
		return getArg().getAssuredBindingNames();
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		arg.visit(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (arg == current) {
			setArg((TupleExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof UnaryTupleOperator) {
			UnaryTupleOperator o = (UnaryTupleOperator)other;
			return arg.equals(o.getArg());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return arg.hashCode();
	}

	@Override
	public UnaryTupleOperator clone() {
		UnaryTupleOperator clone = (UnaryTupleOperator)super.clone();
		clone.setArg(getArg().clone());
		return clone;
	}
}
