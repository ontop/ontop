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

import org.openrdf.model.Value;

/**
 * A variable that can contain a Value.
 */
public class Var extends QueryModelNodeBase implements ValueExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String name;

	private Value value;

	private boolean anonymous = false;

	private boolean constant = false;
	
	/*--------------*
	 * Constructors *
	 *--------------*/

	public Var() {
	}

	public Var(String name) {
		setName(name);
	}

	public Var(String name, Value value) {
		this(name);
		setValue(value);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setAnonymous(boolean anonymous) {
		this.anonymous = anonymous;
	}

	public boolean isAnonymous() {
		return anonymous;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(Value value) {
		this.value = value;
	}

	public boolean hasValue() {
		return value != null;
	}

	public Value getValue() {
		return value;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public String getSignature() {
		StringBuilder sb = new StringBuilder(64);

		sb.append(super.getSignature());

		sb.append(" (name=").append(name);

		if (value != null) {
			sb.append(", value=").append(value.toString());
		}

		if (anonymous) {
			sb.append(", anonymous");
		}

		sb.append(")");

		return sb.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Var) {
			Var o = (Var)other;
			return name.equals(o.getName()) && nullEquals(value, o.getValue()) && anonymous == o.isAnonymous();
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		if (value != null) {
			result ^= value.hashCode();
		}
		if (anonymous) {
			result = ~result;
		}
		return result;
	}

	@Override
	public Var clone() {
		return (Var)super.clone();
	}

	/**
	 * @return Returns the constant.
	 */
	public boolean isConstant() {
		return constant;
	}

	/**
	 * @param constant The constant to set.
	 */
	public void setConstant(boolean constant) {
		this.constant = constant;
	}
}
