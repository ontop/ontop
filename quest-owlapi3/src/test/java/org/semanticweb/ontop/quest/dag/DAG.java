package org.semanticweb.ontop.quest.dag;

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

import org.semanticweb.ontop.ontology.ClassExpression;
import org.semanticweb.ontop.ontology.DataPropertyExpression;
import org.semanticweb.ontop.ontology.DataPropertyRangeExpression;
import org.semanticweb.ontop.ontology.DataRangeExpression;
import org.semanticweb.ontop.ontology.DataSomeValuesFrom;
import org.semanticweb.ontop.ontology.Description;
import org.semanticweb.ontop.ontology.OClass;
import org.semanticweb.ontop.ontology.ObjectPropertyExpression;
import org.semanticweb.ontop.ontology.ObjectSomeValuesFrom;
import org.semanticweb.ontop.ontology.Ontology;
import org.semanticweb.ontop.ontology.OntologyFactory;
import org.semanticweb.ontop.ontology.impl.OntologyFactoryImpl;
import org.semanticweb.ontop.ontology.BinaryAxiom;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.SemanticIndexRange;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

@Deprecated
public class DAG implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9208872698083322721L;

	private int index_counter = 1;

	public final static SemanticIndexRange NULL_RANGE = new SemanticIndexRange(-1, -1);

	public final static int NULL_INDEX = -1;

	public Map<Description, Description> equi_mappings = new HashMap<Description, Description>();

	public final Map<Description, DAGNode> classes;

	public final Map<Description, DAGNode> roles;

	public final Map<Description, DAGNode> allnodes;

	private static final OntologyFactory descFactory = OntologyFactoryImpl.getInstance();

	// public final static String thingStr =
	// "http://www.w3.org/2002/07/owl#Thing";
	// public final static URI thingUri = URI.create(thingStr);
	// public final static Predicate thingPred =
	// predicateFactory.getPredicate(thingUri, 1);
	// public final static ClassDescription thingConcept =
	// descFactory.createClass(thingPred);
	// public final DAGNode thing = new DAGNode(thingConcept);

	/**
	 * Build the DAG from the ontology
	 * 
	 * @param ontology
	 *            ontology that contain TBox assertions for the DAG
	 */
	public DAG(Ontology ontology) {

		int rolenodes = (ontology.getVocabulary().getObjectProperties().size() + ontology.getVocabulary().getDataProperties().size()) * 2;

		int classnodes = ontology.getVocabulary().getClasses().size() + rolenodes * 2;

		classes = new LinkedHashMap<Description, DAGNode>(classnodes * 2);
		roles = new LinkedHashMap<Description, DAGNode>(rolenodes * 2);

		allnodes = new HashMap<Description, DAGNode>((rolenodes + classnodes) * 2);

		// classes.put(thingConcept, thing);

		for (OClass concept : ontology.getVocabulary().getClasses()) {
			DAGNode node = new DAGNode(concept);

			// if (!concept.equals(thingConcept)) {
			// addParent(node, thing);
			classes.put(concept, node);

			allnodes.put(concept, node);
		}

		/*
		 * For each role we add nodes for its inverse, its domain and its range
		 */
		
		for (ObjectPropertyExpression role : ontology.getVocabulary().getObjectProperties()) {
			DAGNode rolenode = new DAGNode(role);

			roles.put(role, rolenode);

			ObjectPropertyExpression roleInv = role.getInverse();
			DAGNode rolenodeinv = new DAGNode(roleInv);
			roles.put(roleInv, rolenodeinv);

			ObjectSomeValuesFrom existsRole = role.getDomain();
			ObjectSomeValuesFrom existsRoleInv = roleInv.getDomain();
			DAGNode existsNode = new DAGNode(existsRole);
			DAGNode existsNodeInv = new DAGNode(existsRoleInv);
			classes.put(existsRole, existsNode);
			classes.put(existsRoleInv, existsNodeInv);

			allnodes.put(role, rolenode);
			allnodes.put(existsRole, existsNode);
			allnodes.put(existsRoleInv, existsNodeInv);
			allnodes.put(roleInv, rolenodeinv);

			// addParent(existsNode, thing);
			// addParent(existsNodeInv, thing);
		}
		for (DataPropertyExpression role : ontology.getVocabulary().getDataProperties()) {
			DAGNode rolenode = new DAGNode(role);

			roles.put(role, rolenode);

			//DataPropertyExpression roleInv = role.getInverse();
			//DAGNode rolenodeinv = new DAGNode(roleInv);
			//roles.put(roleInv, rolenodeinv);

			DataSomeValuesFrom existsRole = role.getDomain(); // descFactory.createPropertySomeRestriction(role);
			DataPropertyRangeExpression existsRoleInv = role.getRange(); //descFactory.createDataPropertyRange(role);
					//.createPropertySomeRestriction(roleInv);
			DAGNode existsNode = new DAGNode(existsRole);
			DAGNode existsNodeInv = new DAGNode(existsRoleInv);
			classes.put(existsRole, existsNode);
			classes.put(existsRoleInv, existsNodeInv);

			allnodes.put(role, rolenode);
			allnodes.put(existsRole, existsNode);
			allnodes.put(existsRoleInv, existsNodeInv);
			//allnodes.put(roleInv, rolenodeinv);

			// addParent(existsNode, thing);
			// addParent(existsNodeInv, thing);
		}

		for (BinaryAxiom<ClassExpression> clsIncl : ontology.getSubClassAxioms()) {
			ClassExpression parent = clsIncl.getSuper();
			ClassExpression child = clsIncl.getSub();

			addClassEdge(parent, child);
		} 
		for (BinaryAxiom<DataRangeExpression> clsIncl : ontology.getSubDataRangeAxioms()) {
			DataRangeExpression parent = clsIncl.getSuper();
			DataRangeExpression child = clsIncl.getSub();

			addClassEdge(parent, child);
		} 
		for (BinaryAxiom<ObjectPropertyExpression> roleIncl : ontology.getSubObjectPropertyAxioms()) {
			ObjectPropertyExpression parent = roleIncl.getSuper();
			ObjectPropertyExpression child = roleIncl.getSub();

			// This adds the direct edge and the inverse, e.g., R ISA S and
			// R- ISA S-,
			// R- ISA S and R ISA S-
			addRoleEdge(parent, child);
		}
		for (BinaryAxiom<DataPropertyExpression> roleIncl : ontology.getSubDataPropertyAxioms()) {
			DataPropertyExpression parent = roleIncl.getSuper();
			DataPropertyExpression child = roleIncl.getSub();

			// This adds the direct edge 
			addRoleEdge(parent, child);
		}
		// clean();
	}

	private void addParent(DAGNode child, DAGNode parent) {
		if (!child.getDescription().equals(parent.getDescription())) {
			child.getParents().add(parent);
			parent.getChildren().add(child);
		}
	}

	public DAG(Map<Description, DAGNode> classes, Map<Description, DAGNode> roles, Map<Description, Description> equiMap,
			Map<Description, DAGNode> allnodes) {
		this.classes = classes;
		this.roles = roles;
		this.equi_mappings = equiMap;
		this.allnodes = allnodes;
	}

	private void addClassEdge(ClassExpression parent, ClassExpression child) {

		DAGNode parentNode;
		if (classes.containsKey(parent)) {
			parentNode = classes.get(parent);
		} else {
			parentNode = new DAGNode(parent);
			classes.put(parent, parentNode);

			allnodes.put(parent, parentNode);
		}
		DAGNode childNode;
		if (classes.containsKey(child)) {
			childNode = classes.get(child);
		} else {
			childNode = new DAGNode(child);
			classes.put(child, childNode);

			allnodes.put(child, childNode);
		}
		addParent(childNode, parentNode);
	}
	
	private void addClassEdge(DataRangeExpression parent, DataRangeExpression child) {

		DAGNode parentNode;
		if (classes.containsKey(parent)) {
			parentNode = classes.get(parent);
		} else {
			parentNode = new DAGNode(parent);
			classes.put(parent, parentNode);

			allnodes.put(parent, parentNode);
		}
		DAGNode childNode;
		if (classes.containsKey(child)) {
			childNode = classes.get(child);
		} else {
			childNode = new DAGNode(child);
			classes.put(child, childNode);

			allnodes.put(child, childNode);
		}
		addParent(childNode, parentNode);

	}
	

	private void addRoleEdge(ObjectPropertyExpression parent, ObjectPropertyExpression child) {
		addRoleEdgeSingle(parent, child);

		addRoleEdgeSingle(parent.getInverse(), child.getInverse());
	}
	
	private void addRoleEdge(DataPropertyExpression parent, DataPropertyExpression child) {
		addRoleEdgeSingle(parent, child);

		//addRoleEdgeSingle(parent.getInverse(), child.getInverse());
	}

	private void addRoleEdgeSingle(ObjectPropertyExpression parent, ObjectPropertyExpression child) {
		DAGNode parentNode = roles.get(parent);
		if (parentNode == null) {
			parentNode = new DAGNode(parent);
			roles.put(parent, parentNode);

			allnodes.put(parent, parentNode);
		}

		DAGNode childNode = roles.get(child);
		if (childNode == null) {
			childNode = new DAGNode(child);
			roles.put(child, childNode);

			allnodes.put(parent, parentNode);
		}
		addParent(childNode, parentNode);

		ObjectSomeValuesFrom existsParent = parent.getDomain();
		ObjectSomeValuesFrom existChild = child.getDomain();

		addClassEdge(existsParent, existChild);
		// addClassEdge(thingConcept, existsParent);

	}
	
	private void addRoleEdgeSingle(DataPropertyExpression parent, DataPropertyExpression child) {
		DAGNode parentNode = roles.get(parent);
		if (parentNode == null) {
			parentNode = new DAGNode(parent);
			roles.put(parent, parentNode);

			allnodes.put(parent, parentNode);
		}

		DAGNode childNode = roles.get(child);
		if (childNode == null) {
			childNode = new DAGNode(child);
			roles.put(child, childNode);

			allnodes.put(parent, parentNode);
		}
		addParent(childNode, parentNode);

		DataSomeValuesFrom existsParent = parent.getDomain(); // descFactory.createPropertySomeRestriction(parent);
		DataSomeValuesFrom existChild = child.getDomain(); // escFactory.createPropertySomeRestriction(child);

		addClassEdge(existsParent, existChild);
		// addClassEdge(thingConcept, existsParent);

	}

	public void clean() {

		/*
		 * First we remove all cycles in roles, not that while doing so we might
		 * also need to colapse some nodes in the class hierarchy, i.e., those
		 * for \exists R and \exists R-
		 */
		DAGOperations.removeCycles(roles, equi_mappings, this);
		DAGOperations.computeTransitiveReduct(roles);

		DAGOperations.removeCycles(classes, equi_mappings, this);
		DAGOperations.computeTransitiveReduct(classes);

		DAGOperations.buildAncestors(roles);
		DAGOperations.buildAncestors(classes);
		
		DAGOperations.buildDescendants(roles);
		DAGOperations.buildDescendants(classes);
	}

	public void index() {

		LinkedList<DAGNode> roots = new LinkedList<DAGNode>();

		for (DAGNode n : classes.values()) {
			if (n.getParents().isEmpty()) {
				roots.add(n);
			}
		}
		for (DAGNode n : roles.values()) {
			if (n.getParents().isEmpty()) {
				roots.add(n);
			}
		}

		for (DAGNode node : roots) {
			indexNode(node);
		}
		for (DAGNode node : roots) {
			mergeRangeNode(node);
		}
	}

	private void mergeRangeNode(DAGNode node) {

		for (DAGNode ch : node.getChildren()) {
			if (ch != node) {
				mergeRangeNode(ch);
				node.getRange().addRange(ch.getRange());
			}

		}
	}

	private void indexNode(DAGNode node) {

		if (node.getIndex() == NULL_INDEX) {
			node.setIndex(index_counter);
			node.setRange(new SemanticIndexRange(index_counter, index_counter));
			index_counter++;
		} else {
			return;
		}
		for (DAGNode ch : node.getChildren()) {
			if (ch != node) {
				indexNode(ch);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		for (DAGNode node : classes.values()) {
			res.append(node);
			res.append("\n");
		}
		for (DAGNode node : roles.values()) {
			res.append(node);
			res.append("\n");
		}
		return res.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (this.getClass() != other.getClass())
			return false;

		DAG otherDAG = (DAG) other;
		return this.classes.equals(otherDAG.classes) && this.roles.equals(otherDAG.roles);
	}

	@Override
	public int hashCode() {
		int result = 17;
		result += 37 * result + this.classes.hashCode();
		result += 37 * result + this.roles.hashCode();
		return result;
	}

	public Collection<DAGNode> getClasses() {
		return classes.values();
	}

	public Collection<DAGNode> getRoles() {
		return roles.values();
	}

	/***
	 * Returns the nodes of this DAG considering the equivalence maps.
	 * 
	 * @param conceptDescription
	 * @return
	 */
	public DAGNode getClassNode(ClassExpression conceptDescription) {
		DAGNode rv = classes.get(conceptDescription);
		if (rv == null) {
			rv = classes.get(equi_mappings.get(conceptDescription));
		}
		return rv;
	}

	/***
	 * Returns the nodes of this DAG considering the equivalence maps.
	 * 
	 * Note, this method is NOT SAFE with respecto equivalences of inverses. If
	 * R- is equivalent to S, then R will be removed. Asking for R will give you
	 * the S node, however, it should not be used directly, since its S- that
	 * should be used. This method should return NULL in such cases, and the
	 * caller should use the equi_mappings directly to get the proper
	 * equivalence, realize that it must get the node for S and it must be used
	 * in an inverse way.
	 * 
	 * @param conceptDescription
	 * @return
	 */
	public DAGNode getRoleNode(ObjectPropertyExpression roleDescription) {
		DAGNode rv = roles.get(roleDescription);
		if (rv == null) {
			rv = roles.get(equi_mappings.get(roleDescription));
		}
		return rv;
	}
	public DAGNode getRoleNode(DataPropertyExpression roleDescription) {
		DAGNode rv = roles.get(roleDescription);
		if (rv == null) {
			rv = roles.get(equi_mappings.get(roleDescription));
		}
		return rv;
	}

	/***
	 * Returns the node associated to this description. It doesnt take into
	 * account equivalences.
	 * 
	 * @param description
	 * @return
	 */
	public DAGNode getNode(Description description) {
		DAGNode n = allnodes.get(description);
		if (n == null)
			return allnodes.get(equi_mappings.get(description));
		return n;

	}

}
