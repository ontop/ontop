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
 * The BOUND function, as defined in <a
 * href="http://www.w3.org/TR/rdf-sparql-query/#func-bound">SPARQL Query
 * Language for RDF</a>; checks if a variable is bound.
 * 
 * @author Arjohn Kampman
 */
public class Bound extends QueryModelNodeBase implements ValueExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The operator's argument.
	 */
	protected Var arg;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Bound() {
	}

	public Bound(Var arg) {
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
	public Var getArg() {
		return arg;
	}

	/**
	 * Sets the argument of this unary value operator.
	 * 
	 * @param arg
	 *        The (new) argument for this operator, must not be <tt>null</tt>.
	 */
	public void setArg(Var arg) {
		assert arg != null : "arg must not be null";
		arg.setParentNode(this);
		this.arg = arg;
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
		arg.visit(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (arg == current) {
			setArg((Var)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Bound) {
			Bound o = (Bound)other;
			return arg.equals(o.getArg());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return arg.hashCode() ^ "Bound".hashCode();
	}

	@Override
	public Bound clone() {
		Bound clone = (Bound)super.clone();
		clone.setArg(getArg().clone());
		return clone;
	}
}
