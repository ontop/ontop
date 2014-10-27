package it.unibz.krdb.obda.owlrefplatform.core.dag;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.DataType;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Sergejs Pugacs
 */
@Deprecated
public class DAGNode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3207267860221176982L;

	private final Description description;

	private SemanticIndexRange range = DAG.NULL_RANGE;
	private int index = DAG.NULL_INDEX;

	private Set<DAGNode> parents = new LinkedHashSet<DAGNode>();
	private Set<DAGNode> children = new LinkedHashSet<DAGNode>();

	private Set<DAGNode> ancestors = new LinkedHashSet<DAGNode>();
	private Set<DAGNode> descendants = new LinkedHashSet<DAGNode>();
	
	public Set<DAGNode> equivalents = new LinkedHashSet<DAGNode>();

	String string = "";

	int hashcode = 0;

	boolean hashNeedsUpdate = true;

	boolean stringNeedsUpdate = true;

	public DAGNode(Description description) {
		this.description = description;
		computeHash();
		computeString();
	}

	private void computeHash() {
		if (!hashNeedsUpdate)
			return;

		hashcode = description != null ? description.hashCode() : 0;
		hashcode = 31 * hashcode + (range != null ? range.hashCode() : 0);
		hashcode = 31 * hashcode + index;

		hashNeedsUpdate = false;
	}

	private void computeString() {
		if (!stringNeedsUpdate)
			return;
		StringBuilder bf = new StringBuilder();
		bf.append("DAGNode{");
		if (description instanceof PropertySomeRestriction) {
			bf.append("E");
			Predicate p = ((PropertySomeRestriction) description).getPredicate();
			String fragment = p.getName().toString();//.getFragment();
			if (fragment != null)
				bf.append(fragment);
			else
				bf.append(p.getName());
			if (((PropertySomeRestriction) description).isInverse())
				bf.append("^-");
		} else if (description instanceof OClass) {
			
			Predicate p = ((OClass) description).getPredicate();
			String fragment = p.getName().toString();//.getFragment();
			if (fragment != null)
				bf.append(fragment);
			else
				bf.append(p.getName());
			
		} else if (description instanceof Property) {
			
			Predicate p = ((Property) description).getPredicate();
			String fragment = p.getName().toString();//.getFragment();
			if (fragment != null)
				bf.append(fragment);
			else
				bf.append(p.getName());
			
			if (((Property) description).isInverse()) {
				bf.append("^-");
			}
			
		} else if (description instanceof DataType) {
		
			DataType datatype = (DataType) description;
			bf.append(datatype.toString());
			
		} else {
			throw new IllegalArgumentException("Invalid description for a node. Description: " + description);
		}

		// bf.append(description);
		bf.append(", range=");
		bf.append(range);
		bf.append(", index=");
		bf.append(index);
		bf.append('}');

		string = bf.toString();
		stringNeedsUpdate = false;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (this.getClass() != other.getClass())
			return false;

		DAGNode otherNode = (DAGNode) other;
		return this.description.equals(otherNode.description) && this.range.equals(otherNode.range) && this.index == otherNode.index;
	}

	@Override
	public String toString() {
		computeString();
		return string;
	}

	@Override
	public int hashCode() {
		computeHash();
		return hashcode;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
		hashNeedsUpdate = true;
		stringNeedsUpdate = true;
	}

	public Set<DAGNode> getParents() {
		return parents;
	}

	public void setRange(SemanticIndexRange range) {
		this.range = range;
		hashNeedsUpdate = true;
		stringNeedsUpdate = true;
	}

	public SemanticIndexRange getRange() {
		return this.range;
	}

	public Set<DAGNode> getChildren() {
		return children;
	}

	public Collection<DAGNode> getEquivalents() {
		return equivalents;
	}

	public Set<DAGNode> getAncestors() {
		return ancestors;
	}
	
	public Set<DAGNode> getDescendants() {
		return descendants;
	}

	public Description getDescription() {
		return description;
	}
}
