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
 * The MINUS set operator, which returns the result of the left tuple
 * expression, except for the results that are also returned by the right tuple
 * expression.
 */
public class Difference extends BinaryTupleOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Difference() {
	}

	/**
	 * Creates a new minus operator that operates on the two specified arguments.
	 * 
	 * @param leftArg
	 *        The left argument of the minus operator.
	 * @param rightArg
	 *        The right argument of the minus operator.
	 */
	public Difference(TupleExpr leftArg, TupleExpr rightArg) {
		super(leftArg, rightArg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Set<String> getBindingNames() {
		return getLeftArg().getBindingNames();
	}

	public Set<String> getAssuredBindingNames() {
		return getLeftArg().getAssuredBindingNames();
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Difference && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "Difference".hashCode();
	}

	@Override
	public Difference clone() {
		return (Difference)super.clone();
	}
}
