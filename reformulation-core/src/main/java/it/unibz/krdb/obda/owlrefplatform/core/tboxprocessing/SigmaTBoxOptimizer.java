/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing;

import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAGBuilder;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAGImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prune Ontology for redundant assertions based on dependencies
 */
public class SigmaTBoxOptimizer {

	private final TBoxReasonerImpl reasonerIsa;
	private final TBoxReasonerImpl reasonerSigmaChain;
	private final TBoxReasonerImpl reasonerIsaChain;

	private static final OntologyFactory	descFactory = OntologyFactoryImpl.getInstance();
	private static final Logger		log					= LoggerFactory.getLogger(SigmaTBoxOptimizer.class);

	private Ontology				originalOntology	= null;

	public SigmaTBoxOptimizer(Ontology isat, Ontology sigmat) {
		this.originalOntology = isat;
		
		reasonerIsa = new TBoxReasonerImpl(isat);
		
		TBoxReasonerImpl reasonerSigma = new TBoxReasonerImpl(sigmat);
		
		DAGImpl isaChain = reasonerIsa.getChainDAG();
		reasonerIsaChain = new TBoxReasonerImpl(isaChain);
		
		DAGImpl sigmaChain =  reasonerSigma.getChainDAG();
		reasonerSigmaChain = new TBoxReasonerImpl(sigmaChain);
	}

	public Ontology getReducedOntology() {
		Ontology reformulationOntology = descFactory.createOntology("http://it.unibz.krdb/obda/auxontology");
		reformulationOntology.addEntities(originalOntology.getVocabulary());

		reformulationOntology.addAssertions(reduce());
		return reformulationOntology;
	}

	public List<Axiom> reduce() {
		log.debug("Starting semantic-reduction");
		List<Axiom> rv = new LinkedList<Axiom>();

		for(Set<Description> nodes: reasonerIsa.getNodes()) {
			Description firstNode = nodes.iterator().next();
			Description node = reasonerIsa.getRepresentativeFor(firstNode);
			for (Set<Description> descendants: reasonerIsa.getDescendants(node)) {
					Description firstDescendant = descendants.iterator().next();
					Description descendant = reasonerIsa.getRepresentativeFor(firstDescendant);
					if (!descendant.equals(node)) {
					/*
					 * Creating subClassOf or subPropertyOf axioms
					 */
						if (descendant instanceof ClassDescription) {
							if (!check_redundant(node, descendant)) {
								rv.add(descFactory.createSubClassAxiom((ClassDescription) descendant, (ClassDescription) node));
							}
						} 
						else {
							if (!check_redundant_role((Property)node,(Property)descendant)) {
								rv.add(descFactory.createSubPropertyAxiom((Property) descendant, (Property) node));
							}
						}
					}
			}
			Set<Description> equivalents = reasonerIsa.getEquivalences(node);

			for (Description equivalent : equivalents) {
				if (!equivalent.equals(node)) {
					if (node instanceof ClassDescription) {
						if (!check_redundant(equivalent, node)) {
							rv.add(descFactory.createSubClassAxiom((ClassDescription) node, (ClassDescription) equivalent));
						}
					} 
					else {
						if (!check_redundant_role((Property)equivalent,(Property)node)) {
							rv.add(descFactory.createSubPropertyAxiom((Property) node, (Property) equivalent));
						}
					}
					
					if (equivalent instanceof ClassDescription) {
						if (!check_redundant(node, equivalent)) {
							rv.add(descFactory.createSubClassAxiom((ClassDescription) equivalent, (ClassDescription) node));
						}
					} 
					else {
						if (!check_redundant_role((Property)node, (Property)equivalent)) {
							rv.add(descFactory.createSubPropertyAxiom((Property) equivalent, (Property) node));
						}
					}
				}
			}
		}
//		log.debug("Finished semantic-reduction.");
		return rv;
	}

	private boolean check_redundant_role(Property parent, Property child) {

		if (check_directly_redundant_role(parent, child))
			return true;
		else {
//			log.debug("Not directly redundant role {} {}", parent, child);
			for (Set<Description> children_prime : reasonerIsa.getDirectChildren(parent)) {
				Property child_prime = (Property) children_prime.iterator().next();

				if (!child_prime.equals(child) && check_directly_redundant_role(child_prime, child)
						&& !check_redundant(child_prime, parent)) {
					return true;
				}
			}
		}
//		log.debug("Not redundant role {} {}", parent, child);

		return false;
	}

	private boolean check_directly_redundant_role(Property parent, Property child) {

		PropertySomeRestriction existParentDesc = 
				descFactory.getPropertySomeRestriction(parent.getPredicate(), parent.isInverse());
		PropertySomeRestriction existChildDesc = 
				descFactory.getPropertySomeRestriction(child.getPredicate(), child.isInverse());

		Description exists_parent = reasonerIsa.getNode(existParentDesc);
		Description exists_child = reasonerIsa.getNode(existChildDesc);

		return check_directly_redundant(parent, child) && check_directly_redundant(exists_parent, exists_child);
	}

	private boolean check_redundant(Description parent, Description child) {
		if (check_directly_redundant(parent, child))
			return true;
		else {
			for (Set<Description> children_prime : reasonerIsa.getDirectChildren(parent)) {
			Description child_prime = children_prime.iterator().next();

				if (!child_prime.equals(child) && check_directly_redundant(child_prime, child) && 
						!check_redundant(child_prime, parent)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean check_directly_redundant(Description parent, Description child) {
		Description sp = reasonerSigmaChain.getNode(parent);
		Description sc = reasonerSigmaChain.getNode(child);
		Description tc = reasonerIsaChain.getNode(child);

		if (sp == null || sc == null || tc == null) {
			return false;
		}
		
		Set<Set<Description>> spChildren =  reasonerSigmaChain.getDirectChildren(sp);
		Set<Description> scEquivalent = reasonerSigmaChain.getEquivalences(sc);
		Set<Set<Description>> scChildren = reasonerSigmaChain.getDescendants(sc);
		Set<Set<Description>> tcChildren = reasonerIsaChain.getDescendants(tc);

		boolean redundant = spChildren.contains(scEquivalent) && scChildren.containsAll(tcChildren);
		return redundant;
	}

}
