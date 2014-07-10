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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.util.iterators.Iterators;

/**
 * A tuple operator that groups tuples that have a specific set of equivalent
 * variable bindings, and that can apply aggregate functions on the grouped
 * results.
 * 
 * @author David Huynh
 * @author Arjohn Kampman
 */
public class Group extends UnaryTupleOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Set<String> groupBindings = new LinkedHashSet<String>();

	private List<GroupElem> groupElements = new ArrayList<GroupElem>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Group(TupleExpr arg) {
		super(arg);
	}

	public Group(TupleExpr arg, Iterable<String> groupBindingNames) {
		this(arg);
		setGroupBindingNames(groupBindingNames);
	}

	public Group(TupleExpr arg, Iterable<String> groupBindingNames, Iterable<GroupElem> groupElements) {
		this(arg, groupBindingNames);
		setGroupElements(groupElements);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Set<String> getGroupBindingNames() {
		return Collections.unmodifiableSet(groupBindings);
	}

	public void addGroupBindingName(String bindingName) {
		groupBindings.add(bindingName);
	}

	public void setGroupBindingNames(Iterable<String> bindingNames) {
		groupBindings.clear();
		Iterators.addAll(bindingNames.iterator(), groupBindings);
	}

	public List<GroupElem> getGroupElements() {
		return Collections.unmodifiableList(groupElements);
	}

	public void addGroupElement(GroupElem groupElem) {
		groupElements.add(groupElem);
	}

	public void setGroupElements(Iterable<GroupElem> elements) {
		this.groupElements.clear();
		Iterators.addAll(elements.iterator(), this.groupElements);
	}

	public Set<String> getAggregateBindingNames() {
		Set<String> bindings = new HashSet<String>();

		for (GroupElem binding : groupElements) {
			bindings.add(binding.getName());
		}

		return bindings;
	}

	@Override
	public Set<String> getBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>();

		bindingNames.addAll(getGroupBindingNames());
		bindingNames.addAll(getAggregateBindingNames());

		return bindingNames;
	}

	@Override
	public Set<String> getAssuredBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>();

		bindingNames.addAll(getGroupBindingNames());
		bindingNames.retainAll(getArg().getAssuredBindingNames());

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
		super.visitChildren(visitor);

		for (GroupElem ge : groupElements) {
			ge.visit(visitor);
		}
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {

		if (replaceNodeInList(groupElements, current, replacement)) {
			return;
		}
		super.replaceChildNode(current, replacement);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Group && super.equals(other)) {
			Group o = (Group)other;
			return groupBindings.equals(o.getGroupBindingNames())
					&& groupElements.equals(o.getGroupElements());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ groupBindings.hashCode() ^ groupElements.hashCode();
	}

	@Override
	public Group clone() {
		Group clone = (Group)super.clone();

		clone.groupBindings = new LinkedHashSet<String>(getGroupBindingNames());

		clone.groupElements = new ArrayList<GroupElem>(getGroupElements().size());
		for (GroupElem ge : getGroupElements()) {
			clone.addGroupElement(ge.clone());
		}

		return clone;
	}
	
	@Override
	public String getSignature() {
		StringBuilder b = new StringBuilder();
		b.append(this.getClass().getSimpleName());
		b.append(" (");
		
		Set<String> bindingNames = getGroupBindingNames();
		int count = 0;
		for (String name: bindingNames) {
			b.append(name);
			count++;
			if (count < bindingNames.size()) {
				b.append(", ");
			}
		}
		b.append(")");
		return b.toString();
	}
}
