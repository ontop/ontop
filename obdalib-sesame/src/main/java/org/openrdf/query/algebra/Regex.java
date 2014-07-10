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
public class Regex extends BinaryValueOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueExpr flagsArg;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Regex() {
	}

	public Regex(ValueExpr expr, ValueExpr pattern, ValueExpr flags) {
		super(expr, pattern);
		setFlagsArg(flags);
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

	public ValueExpr getPatternArg() {
		return super.getRightArg();
	}

	public void setPatternArg(ValueExpr rightArg) {
		super.setRightArg(rightArg);
	}

	public void setFlagsArg(ValueExpr flags) {
		this.flagsArg = flags;
	}

	public ValueExpr getFlagsArg() {
		return flagsArg;
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
		super.visitChildren(visitor);
		if (flagsArg != null) {
			flagsArg.visit(visitor);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Regex && super.equals(other)) {
			Regex o = (Regex)other;
			return nullEquals(flagsArg, o.getFlagsArg());
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode() ^ "Regex".hashCode();
		if (flagsArg != null) {
			result ^= flagsArg.hashCode();
		}
		return result;
	}

	@Override
	public Regex clone() {
		Regex clone = (Regex)super.clone();
		if (flagsArg != null) {
			clone.setFlagsArg(flagsArg.clone());
		}
		return clone;
	}
}
