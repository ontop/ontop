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
 * A ValueExpr with a constant value.
 */
public class ValueConstant extends QueryModelNodeBase implements ValueExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Value value;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ValueConstant() {
	}

	public ValueConstant(Value value) {
		setValue(value);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		assert value != null : "value must not be null";
		this.value = value;
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
		sb.append(" (value=");
		sb.append(value.toString());
		sb.append(")");

		return sb.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ValueConstant) {
			ValueConstant o = (ValueConstant)other;
			return value.equals(o.getValue());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public ValueConstant clone() {
		return (ValueConstant)super.clone();
	}
}
