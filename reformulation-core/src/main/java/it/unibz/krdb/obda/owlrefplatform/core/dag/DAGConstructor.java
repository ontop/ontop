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

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.OntologyImpl;
import it.unibz.krdb.obda.ontology.impl.SubClassAxiomImpl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;




@Deprecated
public class DAGConstructor {

	private static final OBDADataFactory predicateFactory = OBDADataFactoryImpl.getInstance();
	private static final OntologyFactory descFactory = OntologyFactoryImpl.getInstance();

	public static DAG getISADAG(Ontology ontology) {
		return new DAG(ontology);
	}

	public static DAG getSigma(Ontology ontology) {

		Ontology sigma = descFactory.createOntology("");
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

		//sigma.saturate();
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

		Ontology sigma = descFactory.createOntology("sigma");
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

		Ontology sigma = descFactory.createOntology("sigma");

		DAGEdgeIterator edgeiterator = new DAGEdgeIterator(dag);
		OntologyFactory fac = OntologyFactoryImpl.getInstance();

		while (edgeiterator.hasNext()) {
			Edge edge = edgeiterator.next();
			if (edge.getLeft().getDescription() instanceof BasicClassDescription) {
				BasicClassDescription sub = (BasicClassDescription) edge.getLeft().getDescription();
				BasicClassDescription superp = (BasicClassDescription) edge.getRight().getDescription();
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
