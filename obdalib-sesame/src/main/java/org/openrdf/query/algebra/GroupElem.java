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
 * A tuple operator that groups tuples that have a specific set of equivalent
 * variable bindings, and that can apply aggregate functions on the grouped
 * results.
 * 
 * @author David Huynh
 * @author Arjohn Kampman
 */
public class GroupElem extends QueryModelNodeBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String name;

	private AggregateOperator operator;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public GroupElem(String name, AggregateOperator operator) {
		setName(name);
		setOperator(operator);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getName() {
		return name;
	}

	public void setName(String name) {
		assert name != null : "name must not be null";
		this.name = name;
	}

	public AggregateOperator getOperator() {
		return operator;
	}

	public void setOperator(AggregateOperator operator) {
		assert operator != null : "operator must not be null";
		this.operator = operator;
		operator.setParentNode(this);
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
		operator.visit(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (operator == current) {
			setOperator((AggregateOperator)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof GroupElem) {
			GroupElem o = (GroupElem)other;
			return name.equals(o.getName()) && operator.equals(o.getOperator());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode() ^ operator.hashCode();
	}

	@Override
	public GroupElem clone() {
		GroupElem clone = (GroupElem)super.clone();
		clone.setOperator(getOperator().clone());
		return clone;
	}
}
