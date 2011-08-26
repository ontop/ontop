package it.unibz.krdb.obda.owlrefplatform.core.dag;

import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Axiom;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Class;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OntologyFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicDescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.SubClassAxiomImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyImpl;
import it.unibz.krdb.obda.owlrefplatform.core.translator.OWLAPI2Translator;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DAGConstructor {

	private static final OBDADataFactory	predicateFactory	= OBDADataFactoryImpl.getInstance();
	private static final OntologyFactory	descFactory			= new BasicDescriptionFactory();

	public static DAG getISADAG(Ontology ontology) {
		return new DAG(ontology);
	}

	public static DAG getSigma(Ontology ontology) {

		Ontology sigma = new OntologyImpl(URI.create(""));

		for (Axiom assertion : ontology.getAssertions()) {
			if (assertion instanceof SubClassAxiomImpl) {
				SubClassAxiomImpl inclusion = (SubClassAxiomImpl) assertion;
				Description parent = inclusion.getSuper();
				Description child = inclusion.getSub();
				if (parent instanceof PropertySomeDescription) {
					continue;
				}
			}
			sigma.addAssertion(assertion);
		}

		sigma.addConcepts(new ArrayList<ClassDescription>(ontology.getConcepts()));
		sigma.addRoles(new ArrayList<Property>(ontology.getRoles()));
		sigma.saturate();
		return getISADAG(sigma);
	}

	public static Ontology getSigmaOntology(Ontology ontology) {

		Ontology sigma = new OntologyImpl(URI.create("sigma"));

		for (Axiom assertion : ontology.getAssertions()) {
			if (assertion instanceof SubClassAxiomImpl) {
				SubClassAxiomImpl inclusion = (SubClassAxiomImpl) assertion;
				Description parent = inclusion.getSuper();
				Description child = inclusion.getSub();
				if (parent instanceof PropertySomeDescription) {
					continue;
				}
			}
			sigma.addAssertion(assertion);
		}

		sigma.addConcepts(new ArrayList<ClassDescription>(ontology.getConcepts()));
		sigma.addRoles(new ArrayList<Property>(ontology.getRoles()));

		return sigma;
	}

	public List<OBDAMappingAxiom> getMappings(DAG dag) throws DuplicateMappingException {
		return null;
		// return SemanticIndexMappingGenerator.build(dag);
	}

	public DAGChain getTChainDAG(DAG dag) {

		return new DAGChain(dag);
	}

	public Set<Axiom> getSigmaChainDAG(Set<Axiom> assertions) {
		return null;
	}

	public static DAG filterPureISA(DAG dag) {

		Map<Description, DAGNode> classes = new LinkedHashMap<Description, DAGNode>();
		Map<Description, DAGNode> roles = new LinkedHashMap<Description, DAGNode>();

		for (DAGNode node : dag.getClasses()) {

			if (node.getDescription() instanceof PropertySomeDescription || node.getDescription().equals(DAG.thingConcept)) {
				continue;
			}

			DAGNode newNode = classes.get(node.getDescription());
			if (newNode == null) {
				newNode = new DAGNode(node.getDescription());
				newNode.setIndex(node.getIndex());
				newNode.getRange().addRange(node.getRange());
				newNode.equivalents = new LinkedList<DAGNode>(node.equivalents);
				classes.put(node.getDescription(), newNode);
			}

			for (DAGNode child : node.getChildren()) {
				if (child.getDescription() instanceof PropertySomeDescription) {
					continue;
				}
				DAGNode newChild = classes.get(child.getDescription());
				if (newChild == null) {
					newChild = new DAGNode(child.getDescription());
					newChild.equivalents = new LinkedList<DAGNode>(child.equivalents);
					classes.put(child.getDescription(), newChild);
				}

				if (!newChild.getDescription().equals(newNode.getDescription())) {
					newChild.getParents().add(newNode);
					newNode.getChildren().add(newChild);
				}
			}
		}

		for (DAGNode node : dag.getRoles()) {
			Property nodeDesc = (Property) node.getDescription();

			if (nodeDesc.getPredicate().getName().toString().startsWith(OWLAPI2Translator.AUXROLEURI)) {
				continue;
			}

			if (nodeDesc.isInverse()) {
				Property posNode = descFactory.getRoleDescription(nodeDesc.getPredicate(), false);
				DAGNode newNode = roles.get(posNode);
				if (newNode == null) {
					newNode = new DAGNode(posNode);
					roles.put(posNode, newNode);
				}
				continue;
			}

			DAGNode newNode = roles.get(nodeDesc);

			if (newNode == null) {
				newNode = new DAGNode(nodeDesc);
				newNode.equivalents = new LinkedList<DAGNode>(node.equivalents);
				roles.put(nodeDesc, newNode);
			}
			for (DAGNode child : node.getChildren()) {
				Property childDesc = (Property) child.getDescription();
				if (childDesc.getPredicate().getName().toString().startsWith(OWLAPI2Translator.AUXROLEURI)) {
					continue;
				}
				if (childDesc.isInverse()) {
					Property posChild = descFactory.getRoleDescription(childDesc.getPredicate(), false);
					DAGNode newChild = roles.get(posChild);
					if (newChild == null) {
						newChild = new DAGNode(posChild);
						roles.put(posChild, newChild);
					}
					continue;
				}

				DAGNode newChild = roles.get(childDesc);
				if (newChild == null) {
					newChild = new DAGNode(childDesc);
					newChild.equivalents = new LinkedList<DAGNode>(child.equivalents);
					roles.put(childDesc, newChild);
				}
				if (!newChild.getDescription().equals(newNode.getDescription())) {
					newChild.getParents().add(newNode);
					newNode.getChildren().add(newChild);
				}
			}
		}
		Map<Description, Description> newEquivalentMappings = new HashMap<Description, Description>();
		for (Description desc : dag.equi_mappings.keySet()) {
			Description key = makePositive(desc);
			Description val = makePositive(dag.equi_mappings.get(desc));

			if (key != null && val != null)
				newEquivalentMappings.put(key, val);

		}
		DAG newDag = new DAG(classes, roles, newEquivalentMappings);

		return newDag;
	}

	private static Description makePositive(Description desc) {

		if (desc instanceof Property) {
			Property roleKey = (Property) desc;
			return descFactory.getRoleDescription(roleKey.getPredicate(), false);

		} else if (desc instanceof Class) {
			return desc;
		}

		// Simple DAG should not contain ER nodes
		return null;

	}

}
