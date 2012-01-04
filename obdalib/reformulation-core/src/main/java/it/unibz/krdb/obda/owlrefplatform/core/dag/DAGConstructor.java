package it.unibz.krdb.obda.owlrefplatform.core.dag;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Axiom;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OntologyFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.SubClassAxiomImpl;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;



public class DAGConstructor {

	private static final OBDADataFactory predicateFactory = OBDADataFactoryImpl.getInstance();
	private static final OntologyFactory descFactory = new OntologyFactoryImpl();

	public static DAG getISADAG(Ontology ontology) {
		return new DAG(ontology);
	}

	public static DAG getSigma(Ontology ontology) {

		Ontology sigma = descFactory.createOntology(URI.create(""));
		sigma.addConcepts(ontology.getConcepts());
		sigma.addRoles(ontology.getRoles());
		for (Axiom assertion : ontology.getAssertions()) {

			if (assertion instanceof SubClassAxiomImpl) {
				SubClassAxiomImpl inclusion = (SubClassAxiomImpl) assertion;
				Description parent = inclusion.getSuper();
				Description child = inclusion.getSub();
				if (parent instanceof PropertySomeRestriction) {
					continue;
				}
			}

			sigma.addAssertion(assertion);
		}

		sigma.saturate();
		return getISADAG(sigma);
	}

	/***
	 * DONT USE, BUGGY
	 * 
	 * @param ontology
	 * @return
	 */
	@Deprecated
	public static Ontology getSigmaOntology(Ontology ontology) {

		Ontology sigma = descFactory.createOntology(URI.create("sigma"));
		sigma.addConcepts(ontology.getConcepts());
		sigma.addRoles(ontology.getRoles());

		for (Axiom assertion : ontology.getAssertions()) {
			if (assertion instanceof SubClassAxiomImpl) {
				SubClassAxiomImpl inclusion = (SubClassAxiomImpl) assertion;
				Description parent = inclusion.getSuper();
				Description child = inclusion.getSub();
				if (parent instanceof PropertySomeRestriction) {
					continue;
				}
			}
			sigma.addAssertion(assertion);
		}

		return sigma;
	}

	public static Ontology getSigmaOntology(DAG dag) {

		Ontology sigma = descFactory.createOntology(URI.create("sigma"));

		DAGEdgeIterator edgeiterator = new DAGEdgeIterator(dag);
		OntologyFactory fac = OntologyFactoryImpl.getInstance();

		while (edgeiterator.hasNext()) {
			Edge edge = edgeiterator.next();
			if (edge.getLeft().getDescription() instanceof ClassDescription) {
				ClassDescription sub = (ClassDescription) edge.getLeft().getDescription();
				ClassDescription superp = (ClassDescription) edge.getRight().getDescription();
				if (superp instanceof PropertySomeRestriction)
					continue;

				Axiom ax = fac.createSubClassAxiom(sub, superp);
				sigma.addEntities(ax.getReferencedEntities());
				sigma.addAssertion(ax);
			} else {
				Property sub = (Property) edge.getLeft().getDescription();
				Property superp = (Property) edge.getRight().getDescription();

				Axiom ax = fac.createSubPropertyAxiom(sub, superp);
				sigma.addEntities(ax.getReferencedEntities());

				sigma.addAssertion(ax);
			}
		}

		return sigma;
	}

	public static DAG filterPureISA(DAG dag) {

		Map<Description, DAGNode> classes = new LinkedHashMap<Description, DAGNode>();
		Map<Description, DAGNode> roles = new LinkedHashMap<Description, DAGNode>();
		Map<Description, DAGNode> allnodes = new LinkedHashMap<Description, DAGNode>();

		for (DAGNode node : dag.getClasses()) {

			if (node.getDescription() instanceof PropertySomeRestriction) {
				continue;
			}

			DAGNode newNode = classes.get(node.getDescription());
			if (newNode == null) {
				newNode = new DAGNode(node.getDescription());
				newNode.setIndex(node.getIndex());
				newNode.getRange().addRange(node.getRange());
				newNode.equivalents = new LinkedHashSet<DAGNode>(node.equivalents);
				classes.put(node.getDescription(), newNode);
				allnodes.put(node.getDescription(), newNode);
			}

			for (DAGNode child : node.getChildren()) {
				if (child.getDescription() instanceof PropertySomeRestriction) {
					continue;
				}
				DAGNode newChild = classes.get(child.getDescription());
				if (newChild == null) {
					newChild = new DAGNode(child.getDescription());
					newChild.equivalents = new LinkedHashSet<DAGNode>(child.equivalents);
					classes.put(child.getDescription(), newChild);
					allnodes.put(child.getDescription(), newChild);
				}

				if (!newChild.getDescription().equals(newNode.getDescription())) {
					newChild.getParents().add(newNode);
					newNode.getChildren().add(newChild);
				}
			}
		}

		for (DAGNode node : dag.getRoles()) {
			Property nodeDesc = (Property) node.getDescription();

			if (nodeDesc.getPredicate().getName().toString().startsWith(OntologyImpl.AUXROLEURI)) {
				continue;
			}

			if (nodeDesc.isInverse()) {
				Property posNode = descFactory.createProperty(nodeDesc.getPredicate(), false);
				DAGNode newNode = roles.get(posNode);
				if (newNode == null) {
					newNode = new DAGNode(posNode);
					roles.put(posNode, newNode);
					allnodes.put(posNode, newNode);
				}
				continue;
			}

			DAGNode newNode = roles.get(nodeDesc);

			if (newNode == null) {
				newNode = new DAGNode(nodeDesc);
				newNode.equivalents = new LinkedHashSet<DAGNode>(node.equivalents);
				roles.put(nodeDesc, newNode);
				allnodes.put(nodeDesc, newNode);
			}
			for (DAGNode child : node.getChildren()) {
				Property childDesc = (Property) child.getDescription();
				if (childDesc.getPredicate().getName().toString().startsWith(OntologyImpl.AUXROLEURI)) {
					continue;
				}
				if (childDesc.isInverse()) {
					Property posChild = descFactory.createProperty(childDesc.getPredicate(), false);
					DAGNode newChild = roles.get(posChild);
					if (newChild == null) {
						newChild = new DAGNode(posChild);
						roles.put(posChild, newChild);
						allnodes.put(posChild, newChild);
					}
					continue;
				}

				DAGNode newChild = roles.get(childDesc);
				if (newChild == null) {
					newChild = new DAGNode(childDesc);
					newChild.equivalents = new LinkedHashSet<DAGNode>(child.equivalents);
					roles.put(childDesc, newChild);
					allnodes.put(childDesc, newChild);
				}
				if (!newChild.getDescription().equals(newNode.getDescription())) {
					newChild.getParents().add(newNode);
					newNode.getChildren().add(newChild);
				}
			}
		}
		Map<Description, Description> newEquivalentMappings = new HashMap<Description, Description>();
		for (Description desc : dag.equi_mappings.keySet()) {
			// Description key = makePositive(desc);
			// Description val = makePositive(dag.equi_mappings.get(desc));

			Description key = desc;
			Description val = dag.equi_mappings.get(desc);

			if (key != null && val != null)
				newEquivalentMappings.put(key, val);

		}
		DAG newDag = new DAG(classes, roles, newEquivalentMappings, allnodes);

		return newDag;
	}

}
