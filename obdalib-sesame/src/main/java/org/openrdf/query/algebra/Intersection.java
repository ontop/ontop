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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The INTERSECT set operator, which returns the intersection of the result sets
 * of two tuple expressions.
 */
public class Intersection extends BinaryTupleOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Intersection() {
	}

	/**
	 * Creates a new intersection operator that operates on the two specified
	 * arguments.
	 * 
	 * @param leftArg
	 *        The left argument of the intersection operator.
	 * @param rightArg
	 *        The right argument of the intersection operator.
	 */
	public Intersection(TupleExpr leftArg, TupleExpr rightArg) {
		super(leftArg, rightArg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Set<String> getBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>(16);
		bindingNames.addAll(getLeftArg().getBindingNames());
		bindingNames.retainAll(getRightArg().getBindingNames());
		return bindingNames;
	}

	public Set<String> getAssuredBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>(16);
		bindingNames.addAll(getLeftArg().getAssuredBindingNames());
		bindingNames.retainAll(getRightArg().getAssuredBindingNames());
		return bindingNames;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Intersection && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "Intersection".hashCode();
	}

	@Override
	public Intersection clone() {
		return (Intersection)super.clone();
	}
}
