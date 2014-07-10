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

import java.util.Set;

/**
 * A generalized projection (allowing the bindings to be renamed) on a tuple
 * expression.
 */
public class Projection extends UnaryTupleOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private ProjectionElemList projElemList = new ProjectionElemList();

	private Var projectionContext = null;
	
	/*--------------*
	 * Constructors *
	 *--------------*/

	public Projection() {
	}

	public Projection(TupleExpr arg) {
		super(arg);
	}

	public Projection(TupleExpr arg, ProjectionElemList elements) {
		this(arg);
		setProjectionElemList(elements);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ProjectionElemList getProjectionElemList() {
		return projElemList;
	}

	public void setProjectionElemList(ProjectionElemList projElemList) {
		this.projElemList = projElemList;
		projElemList.setParentNode(this);
	}

	@Override
	public Set<String> getBindingNames() {
		return projElemList.getTargetNames();
	}

	@Override
	public Set<String> getAssuredBindingNames() {
		// Return all target binding names for which the source binding is assured
		// by the argument
		return projElemList.getTargetNamesFor(getArg().getAssuredBindingNames());
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
		projElemList.visit(visitor);
		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (projElemList == current) {
			setProjectionElemList((ProjectionElemList)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Projection && super.equals(other)) {
			Projection o = (Projection)other;
			return projElemList.equals(o.getProjectionElemList());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ projElemList.hashCode();
	}

	@Override
	public Projection clone() {
		Projection clone = (Projection)super.clone();
		clone.setProjectionElemList(getProjectionElemList().clone());
		return clone;
	}

	/**
	 * @return Returns the projectionContext.
	 */
	public Var getProjectionContext() {
		return projectionContext;
	}

	/**
	 * @param projectionContext The projectionContext to set.
	 */
	public void setProjectionContext(Var projectionContext) {
		this.projectionContext = projectionContext;
	}

}
