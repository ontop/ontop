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
 * IsNumeric - Boolean operator determining if the supplied expression
 * represents a numeric value.
 * 
 * @author Jeen
 */
public class IsNumeric extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public IsNumeric() {
	}

	public IsNumeric(ValueExpr arg) {
		super(arg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof IsNumeric && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "IsNumeric".hashCode();
	}

	@Override
	public IsNumeric clone() {
		return (IsNumeric)super.clone();
	}
}
