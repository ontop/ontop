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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A "multi-projection" that can produce multiple solutions from a single set of
 * bindings.
 */
public class MultiProjection extends UnaryTupleOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The lists of projections.
	 */
	private List<ProjectionElemList> projections = new ArrayList<ProjectionElemList>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public MultiProjection() {
	}

	public MultiProjection(TupleExpr arg) {
		super(arg);
	}

	public MultiProjection(TupleExpr arg, Iterable<ProjectionElemList> projections) {
		this(arg);
		addProjections(projections);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public List<ProjectionElemList> getProjections() {
		return Collections.unmodifiableList(projections);
	}

	public void setProjections(Iterable<ProjectionElemList> projections) {
		this.projections.clear();
		addProjections(projections);
	}

	public void addProjections(Iterable<ProjectionElemList> projections) {
		for (ProjectionElemList projection : projections) {
			addProjection(projection);
		}
	}

	public void addProjection(ProjectionElemList projection) {
		assert projection != null : "projection must not be null";
		projections.add(projection);
		projection.setParentNode(this);
	}

	@Override
	public Set<String> getBindingNames() {
		Set<String> bindingNames = new HashSet<String>();

		for (ProjectionElemList projElemList : projections) {
			bindingNames.addAll(projElemList.getTargetNames());
		}

		return bindingNames;
	}

	@Override
	public Set<String> getAssuredBindingNames() {
		Set<String> bindingNames = new HashSet<String>();

		if (projections.size() >= 1) {
			Set<String> assuredSourceNames = getArg().getAssuredBindingNames();

			bindingNames.addAll(projections.get(0).getTargetNamesFor(assuredSourceNames));

			for (int i = 1; i < projections.size(); i++) {
				bindingNames.retainAll(projections.get(i).getTargetNamesFor(assuredSourceNames));
			}
		}

		return bindingNames;
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
		for (ProjectionElemList projElemList : projections) {
			projElemList.visit(visitor);
		}

		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (replaceNodeInList(projections, current, replacement)) {
			return;
		}
		super.replaceChildNode(current, replacement);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof MultiProjection && super.equals(other)) {
			MultiProjection o = (MultiProjection)other;
			return projections.equals(o.getProjections());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ projections.hashCode();
	}

	@Override
	public MultiProjection clone() {
		MultiProjection clone = (MultiProjection)super.clone();

		clone.projections = new ArrayList<ProjectionElemList>(getProjections().size());
		for (ProjectionElemList pe : getProjections()) {
			clone.addProjection(pe.clone());
		}

		return clone;
	}
}
