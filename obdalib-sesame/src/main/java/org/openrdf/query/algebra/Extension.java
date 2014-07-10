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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * An extension operator that can be used to add bindings to solutions whose
 * values are defined by {@link ValueExpr value expressions}.
 */
public class Extension extends UnaryTupleOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private List<ExtensionElem> elements = new ArrayList<ExtensionElem>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Extension() {
	}

	public Extension(TupleExpr arg) {
		super(arg);
	}

	public Extension(TupleExpr arg, ExtensionElem... elements) {
		this(arg);
		addElements(elements);
	}

	public Extension(TupleExpr arg, Iterable<ExtensionElem> elements) {
		this(arg);
		addElements(elements);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public List<ExtensionElem> getElements() {
		return elements;
	}

	public void setElements(Iterable<ExtensionElem> elements) {
		this.elements.clear();
		addElements(elements);
	}

	public void addElements(ExtensionElem... elements) {
		for (ExtensionElem pe : elements) {
			addElement(pe);
		}
	}

	public void addElements(Iterable<ExtensionElem> elements) {
		for (ExtensionElem pe : elements) {
			addElement(pe);
		}
	}

	public void addElement(ExtensionElem pe) {
		elements.add(pe);
		pe.setParentNode(this);
	}

	@Override
	public Set<String> getBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>(arg.getBindingNames());

		for (ExtensionElem pe : elements) {
			bindingNames.add(pe.getName());
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
		for (ExtensionElem elem : elements) {
			elem.visit(visitor);
		}

		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (replaceNodeInList(elements, current, replacement)) {
			return;
		}
		super.replaceChildNode(current, replacement);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Extension && super.equals(other)) {
			Extension o = (Extension)other;
			return elements.equals(o.getElements());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ elements.hashCode();
	}

	@Override
	public Extension clone() {
		Extension clone = (Extension)super.clone();

		clone.elements = new ArrayList<ExtensionElem>(getElements().size());
		for (ExtensionElem elem : getElements()) {
			clone.addElement(elem.clone());
		}

		return clone;
	}
}
