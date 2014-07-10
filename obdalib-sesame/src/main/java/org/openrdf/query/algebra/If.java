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
 * The IF function, as defined in SPARQL 1.1 Query.
 * 
 * @author Jeen Broekstra
 */
public class If extends QueryModelNodeBase implements ValueExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The operator's arguments.
	 */
	private ValueExpr condition;

	private ValueExpr result;

	private ValueExpr alternative;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public If() {
	}

	public If(ValueExpr condition) {
		setCondition(condition);
	}

	public If(ValueExpr condition, ValueExpr result) {
		setCondition(condition);
		setResult(result);
	}

	public If(ValueExpr condition, ValueExpr result, ValueExpr alternative) {
		setCondition(condition);
		setResult(result);
		setAlternative(alternative);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the argument of this unary value operator.
	 * 
	 * @return The operator's argument.
	 */
	public ValueExpr getCondition() {
		return condition;
	}

	/**
	 * Sets the condition argument of this unary value operator.
	 * 
	 * @param condition
	 *        The (new) condition argument for this operator, must not be
	 *        <tt>null</tt>.
	 */
	public void setCondition(ValueExpr condition) {
		assert condition != null : "arg must not be null";
		condition.setParentNode(this);
		this.condition = condition;
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
		condition.visit(visitor);
		if (result != null) {
			result.visit(visitor);
		}
		if (alternative != null) {
			alternative.visit(visitor);
		}
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (condition == current) {
			setCondition((ValueExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof If) {
			If o = (If)other;

			boolean equal = condition.equals(o.getCondition());
			if (!equal) {
				return equal;
			}

			equal = (result == null) ? o.getResult() == null : result.equals(o.getResult());
			if (!equal) {
				return equal;
			}

			equal = (alternative == null) ? o.getAlternative() == null : alternative.equals(o.getAlternative());

			return equal;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = condition.hashCode();

		if (result != null) {
			hashCode = hashCode ^ result.hashCode();
		}
		if (alternative != null) {
			hashCode = hashCode ^ alternative.hashCode();
		}

		hashCode = hashCode ^ "If".hashCode();

		return hashCode;
	}

	@Override
	public If clone() {
		If clone = (If)super.clone();
		clone.setCondition(condition.clone());
		if (result != null) {
			clone.setResult(result.clone());
		}
		if (alternative != null) {
			clone.setAlternative(alternative.clone());
		}
		return clone;
	}

	/**
	 * @param result
	 *        The result to set.
	 */
	public void setResult(ValueExpr result) {
		this.result = result;
	}

	/**
	 * @return Returns the result.
	 */
	public ValueExpr getResult() {
		return result;
	}

	/**
	 * @param alternative
	 *        The alternative to set.
	 */
	public void setAlternative(ValueExpr alternative) {
		this.alternative = alternative;
	}

	/**
	 * @return Returns the alternative.
	 */
	public ValueExpr getAlternative() {
		return alternative;
	}
}
