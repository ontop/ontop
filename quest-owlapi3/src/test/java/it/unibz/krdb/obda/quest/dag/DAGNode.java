package it.unibz.krdb.obda.quest.dag;

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
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataPropertyRangeExpression;
import it.unibz.krdb.obda.ontology.DataSomeValuesFrom;
import it.unibz.krdb.obda.ontology.Datatype;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.ObjectSomeValuesFrom;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.SemanticIndexRange;

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

	private String string = "";

	private int hashcode = 0;

	private boolean hashNeedsUpdate = true;

	private boolean stringNeedsUpdate = true;

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
		
		if (description instanceof ObjectSomeValuesFrom) {
			bf.append("E");
			ObjectSomeValuesFrom prop = (ObjectSomeValuesFrom)description;
			ObjectPropertyExpression ope = prop.getProperty();
			bf.append(ope.getName());
			if (ope.isInverse())
				bf.append("^-");
		} 
		else if (description instanceof DataSomeValuesFrom) {
			bf.append("E");
			DataSomeValuesFrom prop = (DataSomeValuesFrom)description;
			DataPropertyExpression dpe = prop.getProperty();
			bf.append(dpe.getName());
		} 
		else if (description instanceof OClass) {
			OClass ce = (OClass) description;
			bf.append(ce.getName());	
		} 
		else if (description instanceof ObjectPropertyExpression) {		
			ObjectPropertyExpression ope = (ObjectPropertyExpression) description;
			bf.append(ope.getName());
			
			if (ope.isInverse()) 
				bf.append("^-");
		} 
		else if (description instanceof DataPropertyExpression) {			
			DataPropertyExpression dpe = (DataPropertyExpression) description;
			bf.append(dpe.getName());
		} 
		else if (description instanceof DataPropertyRangeExpression) {
			DataPropertyExpression dpe = ((DataPropertyRangeExpression) description).getProperty();
			bf.append(dpe.getName());			
			bf.append("^-");
		}
		else if (description instanceof Datatype) {
		
			Datatype datatype = (Datatype) description;
			bf.append(datatype.toString());
			
		} 
		else {
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
