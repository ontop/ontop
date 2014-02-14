/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prune Ontology for redundant assertions based on dependencies
 */
public class SigmaTBoxOptimizer {

	private final TBoxReasonerImpl isa;
	private final TBoxReasonerImpl isaChain;
	private final TBoxReasonerImpl sigmaChain;

	private static final OntologyFactory fac = OntologyFactoryImpl.getInstance();
	private static final Logger	log	= LoggerFactory.getLogger(SigmaTBoxOptimizer.class);

	private Set<Predicate> vocabulary;
	private Ontology optimizedTBox = null;

	public SigmaTBoxOptimizer(Ontology isat, Ontology sigmat) {
		
		vocabulary = isat.getVocabulary();
		
		isa = new TBoxReasonerImpl(isat);
		//DAGImpl isaChainDAG = isa.getChainDAG();
		isaChain = TBoxReasonerImpl.getChainReasoner(isat);
		
		//TBoxReasonerImpl reasonerSigma = new TBoxReasonerImpl(sigmat);		
		//DAGImpl sigmaChainDAG =  reasonerSigma.getChainDAG();
		sigmaChain = TBoxReasonerImpl.getChainReasoner(sigmat);
	}

	public Ontology getReducedOntology() {
		if (optimizedTBox == null) {
			optimizedTBox = fac.createOntology("http://it.unibz.krdb/obda/auxontology");
			optimizedTBox.addEntities(vocabulary);

			log.debug("Starting semantic-reduction");

			TBoxTraversal.traverse(isa, new TBoxTraverseListener() {

				@Override
				public void onInclusion(Property sub, Property sup) {
					if (!check_redundant_role(sup, sub)) 
						optimizedTBox.addAssertion(fac.createSubPropertyAxiom(sub, sup));
				}

				@Override
				public void onInclusion(BasicClassDescription sub, BasicClassDescription sup) {
					if (!check_redundant(sup, sub)) 
						optimizedTBox.addAssertion(fac.createSubClassAxiom(sub, sup));
				}
			});
		}
		return optimizedTBox;
	}

	
	
	
	
	
	
	
	
	private boolean check_redundant_role(Property parent, Property child) {

		if (check_directly_redundant_role(parent, child))
			return true;
		else {
//			log.debug("Not directly redundant role {} {}", parent, child);
			for (Equivalences<Property> children_prime : isa.getProperties().getDirectSub(isa.getProperties().getVertex(parent))) {
				Property child_prime = children_prime.getRepresentative();

				if (!child_prime.equals(child) && 
						check_directly_redundant_role(child_prime, child) && 
						!check_redundant(child_prime, parent)) {
					return true;
				}
			}
		}
//		log.debug("Not redundant role {} {}", parent, child);

		return false;
	}

	private boolean check_directly_redundant_role(Property parent, Property child) {

		PropertySomeRestriction existParentDesc = 
				fac.getPropertySomeRestriction(parent.getPredicate(), parent.isInverse());
		PropertySomeRestriction existChildDesc = 
				fac.getPropertySomeRestriction(child.getPredicate(), child.isInverse());

		return check_directly_redundant(parent, child) && 
				check_directly_redundant(existParentDesc, existChildDesc);
	}

	private boolean check_redundant(Property parent, Property child) {
		if (check_directly_redundant(parent, child))
			return true;
		else {
			for (Equivalences<Property> children_prime : isa.getProperties().getDirectSub(isa.getProperties().getVertex(parent))) {
				Property child_prime = children_prime.getRepresentative();

				if (!child_prime.equals(child) && 
						check_directly_redundant(child_prime, child) && 
						!check_redundant(child_prime, parent)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean check_redundant(BasicClassDescription parent, BasicClassDescription child) {
		if (check_directly_redundant(parent, child))
			return true;
		else {
			for (Equivalences<BasicClassDescription> children_prime : isa.getClasses().getDirectSub(isa.getClasses().getVertex(parent))) {
				BasicClassDescription child_prime = children_prime.getRepresentative();

				if (!child_prime.equals(child) && 
						check_directly_redundant(child_prime, child) && 
						!check_redundant(child_prime, parent)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean check_directly_redundant(Property parent, Property child) {
		
		Property sp = (Property)sigmaChain.getRepresentativeFor(parent);
		Property sc = (Property)sigmaChain.getRepresentativeFor(child);
		
		// if one of them is not in the respective DAG
		if (sp == null || sc == null) 
			return false;

		Set<Equivalences<Property>> spChildren =  sigmaChain.getProperties().getDirectSub(sigmaChain.getProperties().getVertex(sp));
		Equivalences<Property> scEquivalent = sigmaChain.getEquivalences(sc);
		
		if (!spChildren.contains(scEquivalent))
			return false;
		
		
		
		Property tc = (Property)isaChain.getRepresentativeFor(child);
		// if one of them is not in the respective DAG
		if (tc == null) 
			return false;
		
		Set<Equivalences<Property>> scChildren = sigmaChain.getProperties().getSub(sigmaChain.getProperties().getVertex(sc));
		Set<Equivalences<Property>> tcChildren = isaChain.getProperties().getSub(isaChain.getProperties().getVertex(tc));

		return scChildren.containsAll(tcChildren);
	}
	
	private boolean check_directly_redundant(BasicClassDescription parent, BasicClassDescription child) {
		
		BasicClassDescription sp = (BasicClassDescription)sigmaChain.getRepresentativeFor(parent);
		BasicClassDescription sc = (BasicClassDescription)sigmaChain.getRepresentativeFor(child);
		
		// if one of them is not in the respective DAG
		if (sp == null || sc == null) 
			return false;

		Set<Equivalences<BasicClassDescription>> spChildren =  sigmaChain.getClasses().getDirectSub(sigmaChain.getClasses().getVertex(sp));
		Equivalences<BasicClassDescription> scEquivalent = sigmaChain.getEquivalences(sc);
		
		if (!spChildren.contains(scEquivalent))
			return false;
		
		
		
		BasicClassDescription tc = (BasicClassDescription)isaChain.getRepresentativeFor(child);
		// if one of them is not in the respective DAG
		if (tc == null) 
			return false;
		
		Set<Equivalences<BasicClassDescription>> scChildren = sigmaChain.getClasses().getSub(sigmaChain.getClasses().getVertex(sc));
		Set<Equivalences<BasicClassDescription>> tcChildren = isaChain.getClasses().getSub(isaChain.getClasses().getVertex(tc));

		return scChildren.containsAll(tcChildren);
	}
	
	
	
	
	
	public static Ontology getSigmaOntology(TBoxReasonerImpl reasoner) {

		final Ontology sigma = fac.createOntology("sigma");

		TBoxTraversal.traverse(reasoner, new TBoxTraverseListener() {
			
			@Override
			public void onInclusion(Property sub, Property sup) {
				Axiom ax = fac.createSubPropertyAxiom(sub, sup);
				sigma.addEntities(ax.getReferencedEntities());
				sigma.addAssertion(ax);						
			}

			@Override
			public void onInclusion(BasicClassDescription sub, BasicClassDescription sup) {
				if (!(sup instanceof PropertySomeRestriction)) {
					Axiom ax = fac.createSubClassAxiom(sub, sup);
					sigma.addEntities(ax.getReferencedEntities());
					sigma.addAssertion(ax);						
				}
			}
		});
		
		return sigma;
	}
}
