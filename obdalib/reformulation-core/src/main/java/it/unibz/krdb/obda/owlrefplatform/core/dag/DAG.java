package it.unibz.krdb.obda.owlrefplatform.core.dag;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Axiom;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OntologyFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.SubClassAxiomImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.SubPropertyAxiomImpl;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DAG {

	private final Logger					log					= LoggerFactory.getLogger(DAG.class);

	private int								index_counter		= 1;

	public final static SemanticIndexRange	NULL_RANGE			= new SemanticIndexRange(-1, -1);
	public final static int					NULL_INDEX			= -1;

	public Map<Description, Description>	equi_mappings		= new HashMap<Description, Description>();

	private final Map<Description, DAGNode>	classes;
	private final Map<Description, DAGNode>	roles;

	private static final OBDADataFactory	predicateFactory	= OBDADataFactoryImpl.getInstance();
	private static final OntologyFactory	descFactory			= new OntologyFactoryImpl();

//	public final static String				thingStr			= "http://www.w3.org/2002/07/owl#Thing";
//	public final static URI					thingUri			= URI.create(thingStr);
//	public final static Predicate			thingPred			= predicateFactory.getPredicate(thingUri, 1);
//	public final static ClassDescription	thingConcept		= descFactory.createClass(thingPred);
//	public final DAGNode					thing				= new DAGNode(thingConcept);

	/**
	 * Build the DAG from the ontology
	 * 
	 * @param ontology
	 *            ontology that contain TBox assertions for the DAG
	 */
	public DAG(Ontology ontology) {

		classes = new LinkedHashMap<Description, DAGNode>(ontology.getConcepts().size());
		roles = new LinkedHashMap<Description, DAGNode>(ontology.getRoles().size());

//		classes.put(thingConcept, thing);

		for (ClassDescription concept : ontology.getConcepts()) {
			DAGNode node = new DAGNode(concept);

//			if (!concept.equals(thingConcept)) {
//				addParent(node, thing);
				classes.put(concept, node);
//			}
		}
		
		/*
		 * For each role we add nodes for its inverse, its domain and its range
		 * 
		 */
		for (Property role : ontology.getRoles()) {
			roles.put(role, new DAGNode(role));

			Property roleInv = descFactory.createProperty(role.getPredicate(), !role.isInverse());
			roles.put(roleInv, new DAGNode(roleInv));

			PropertySomeRestriction existsRole = descFactory.getPropertySomeRestriction(role.getPredicate(), role.isInverse());
			PropertySomeRestriction existsRoleInv = descFactory.getPropertySomeRestriction(role.getPredicate(), !role.isInverse());
			DAGNode existsNode = new DAGNode(existsRole);
			DAGNode existsNodeInv = new DAGNode(existsRoleInv);
			classes.put(existsRole, existsNode);
			classes.put(existsRoleInv, existsNodeInv);

//			addParent(existsNode, thing);
//			addParent(existsNodeInv, thing);
		}

		for (Axiom assertion : ontology.getAssertions()) {

			if (assertion instanceof SubClassAxiomImpl) {
				SubClassAxiomImpl clsIncl = (SubClassAxiomImpl) assertion;
				ClassDescription parent = clsIncl.getSuper();
				ClassDescription child = clsIncl.getSub();

				addClassEdge(parent, child);
			} else if (assertion instanceof SubPropertyAxiomImpl) {
				SubPropertyAxiomImpl roleIncl = (SubPropertyAxiomImpl) assertion;
				Property parent = roleIncl.getSuper();
				Property child = roleIncl.getSub();

				// This adds the direct edge and the inverse, e.g., R ISA S and R- ISA S-,
				// R- ISA S and R ISA S-
				addRoleEdge(parent, child);
			}
		}
//		clean();
	}

	private void addParent(DAGNode child, DAGNode parent) {
		if (!child.getDescription().equals(parent.getDescription())) {
			child.getParents().add(parent);
			parent.getChildren().add(child);
		}
	}

	public DAG(Map<Description, DAGNode> classes, Map<Description, DAGNode> roles, Map<Description, Description> equiMap) {
		this.classes = classes;
		this.roles = roles;
		this.equi_mappings = equiMap;
	}

	private void addClassEdge(ClassDescription parent, ClassDescription child) {

		DAGNode parentNode;
		if (classes.containsKey(parent)) {
			parentNode = classes.get(parent);
		} else {
			parentNode = new DAGNode(parent);
			classes.put(parent, parentNode);
		}
		DAGNode childNode;
		if (classes.containsKey(child)) {
			childNode = classes.get(child);
		} else {
			childNode = new DAGNode(child);
			classes.put(child, childNode);
		}
		addParent(childNode, parentNode);
//		addParent(parentNode, thing);

	}

	private void addRoleEdge(Property parent, Property child) {
		addRoleEdgeSingle(parent, child);

		addRoleEdgeSingle(descFactory.createProperty(parent.getPredicate(), !parent.isInverse()),
				descFactory.createProperty(child.getPredicate(), !child.isInverse()));
	}

	private void addRoleEdgeSingle(Property parent, Property child) {
		DAGNode parentNode = roles.get(parent);
		if (parentNode == null) {
			parentNode = new DAGNode(parent);
			roles.put(parent, parentNode);
		}

		DAGNode childNode = roles.get(child);
		if (childNode == null) {
			childNode = new DAGNode(child);
			roles.put(child, childNode);
		}
		addParent(childNode, parentNode);

		ClassDescription existsParent = descFactory.getPropertySomeRestriction(parent.getPredicate(), parent.isInverse());

		ClassDescription existChild = descFactory.getPropertySomeRestriction(child.getPredicate(), child.isInverse());

		addClassEdge(existsParent, existChild);
//		addClassEdge(thingConcept, existsParent);

	}

	public void clean() {
		DAGOperations.removeCycles(classes, equi_mappings);
		DAGOperations.computeTransitiveReduct(classes);

		DAGOperations.removeCycles(roles, equi_mappings);
		DAGOperations.computeTransitiveReduct(roles);

		DAGOperations.buildDescendants(classes);
		DAGOperations.buildDescendants(roles);
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

	public DAGNode getClassNode(ClassDescription conceptDescription) {
		DAGNode rv = classes.get(conceptDescription);
		if (rv == null) {
			rv = classes.get(equi_mappings.get(conceptDescription));
		}
		return rv;
	}

	public DAGNode getRoleNode(Property roleDescription) {
		DAGNode rv = roles.get(roleDescription);
		if (rv == null) {
			rv = roles.get(equi_mappings.get(roleDescription));
		}
		return rv;
	}

	public Iterator<Edge> getTransitiveEdgeIterator() {
		return null;
	}

}
