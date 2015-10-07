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
 * Compares the string representation of a value expression to a pattern.
 */
public class StrStarts extends BinaryValueOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueExpr string;
	private ValueExpr start;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public StrStarts() {
	}

	public StrStarts(ValueExpr string, ValueExpr starts) {
		super(string, starts);
		setStrArgs(string,starts);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ValueExpr getArg() {
		return super.getLeftArg();
	}

	public void setArg(ValueExpr leftArg) {
		super.setLeftArg(leftArg);
	}

	public ValueExpr getStartArg() {
		return super.getRightArg();
	}

	public void setStartArg(ValueExpr rightArg) {
		super.setRightArg(rightArg);
	}

	public void setStrArgs(ValueExpr string, ValueExpr start) {
		this.string = string;
		this.start = start;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof StrStarts && super.equals(other);
	}



}
