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
public class Like extends UnaryValueOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String pattern;

	private boolean caseSensitive;

	/**
	 * Operational pattern, equal to pattern but converted to lower case when not
	 * case sensitive.
	 */
	private String opPattern;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Like() {
	}

	public Like(ValueExpr expr, String pattern, boolean caseSensitive) {
		super(expr);
		setPattern(pattern, caseSensitive);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setPattern(String pattern, boolean caseSensitive) {
		assert pattern != null : "pattern must not be null";
		this.pattern = pattern;
		this.caseSensitive = caseSensitive;
		opPattern = caseSensitive ? pattern : pattern.toLowerCase();
	}

	public String getPattern() {
		return pattern;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public String getOpPattern() {
		return opPattern;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public String getSignature() {
		StringBuilder sb = new StringBuilder(128);

		sb.append(super.getSignature());
		sb.append(" \"");
		sb.append(pattern);
		sb.append("\"");

		if (caseSensitive) {
			sb.append(" IGNORE CASE");
		}

		return sb.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Like && super.equals(other)) {
			Like o = (Like)other;
			return caseSensitive == o.isCaseSensitive() && opPattern.equals(o.getOpPattern());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ opPattern.hashCode();
	}

	@Override
	public Like clone() {
		return (Like)super.clone();
	}
}
