package it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing;

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

import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.PropertyExpression;
import it.unibz.krdb.obda.ontology.SomeValuesFrom;
import it.unibz.krdb.obda.ontology.SubClassExpression;
import it.unibz.krdb.obda.ontology.SubClassOfAxiom;
import it.unibz.krdb.obda.ontology.SubPropertyOfAxiom;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prune Ontology for redundant assertions based on dependencies
 */
public class SigmaTBoxOptimizer {

	private final TBoxReasoner isa;
	private final TBoxReasoner isaChain;
	private final TBoxReasoner sigmaChain;

	private static final OntologyFactory fac = OntologyFactoryImpl.getInstance();
	private static final Logger	log	= LoggerFactory.getLogger(SigmaTBoxOptimizer.class);

	private Ontology optimizedTBox = null;

	public SigmaTBoxOptimizer(TBoxReasoner isa) {		
		this.isa = isa;
		
		isaChain = TBoxReasonerImpl.getChainReasoner((TBoxReasonerImpl)isa);
	
		TBoxReasonerImpl sigma = new TBoxReasonerImpl(TBoxReasonerToOntology.getOntology(isa, true));						
		
		sigmaChain = TBoxReasonerImpl.getChainReasoner(sigma);
	}

	// USED IN ONE TEST (SemanticReductionTest, with the empty Sigma)
	@Deprecated 
	public SigmaTBoxOptimizer(TBoxReasoner isa, TBoxReasonerImpl s) {		
		this.isa = isa;
		
		isaChain = TBoxReasonerImpl.getChainReasoner((TBoxReasonerImpl)isa);

		TBoxReasonerImpl sigma = new TBoxReasonerImpl(OntologyFactoryImpl.getInstance().createOntology());						
		
		sigmaChain = TBoxReasonerImpl.getChainReasoner(sigma);
	}
	
	public Ontology getReducedOntology() {
		if (optimizedTBox == null) {
			optimizedTBox = fac.createOntology();
			//optimizedTBox.addEntities(vocabulary);

			log.debug("Starting semantic-reduction");

			TBoxTraversal.traverse(isa, new TBoxTraverseListener() {

				@Override
				public void onInclusion(PropertyExpression sub, PropertyExpression sup) {
					if (sub != sup) {
						if (!check_redundant_role(sup, sub)) {
							SubPropertyOfAxiom axiom = fac.createSubPropertyAxiom(sub, sup);
							optimizedTBox.addAxiom(axiom);
						}
					}
				}

				@Override
				public void onInclusion(BasicClassDescription sub, BasicClassDescription sup) {
					if (sub != sup) {
						if (!sup.equals(sub) && !check_redundant(sup, sub))  {
							SubClassOfAxiom axiom = fac.createSubClassAxiom((SubClassExpression)sub, sup);
							optimizedTBox.addAxiom(axiom);
						}
					}
				}
			});
		}
		return optimizedTBox;
	}

	
	
	
	
	
	
	
	
	private boolean check_redundant_role(PropertyExpression parent, PropertyExpression child) {

		if (check_directly_redundant_role(parent, child))
			return true;
		else {
//			log.debug("Not directly redundant role {} {}", parent, child);
			for (Equivalences<PropertyExpression> children_prime : isa.getProperties().getDirectSub(isa.getProperties().getVertex(parent))) {
				PropertyExpression child_prime = children_prime.getRepresentative();

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

	private boolean check_directly_redundant_role(PropertyExpression parent, PropertyExpression child) {

		SomeValuesFrom existParentDesc = fac.createPropertySomeRestriction(parent);
		SomeValuesFrom existChildDesc = fac.createPropertySomeRestriction(child);

		return check_directly_redundant(parent, child) && 
				check_directly_redundant(existParentDesc, existChildDesc);
	}

	private boolean check_redundant(PropertyExpression parent, PropertyExpression child) {
		if (check_directly_redundant(parent, child))
			return true;
		else {
			for (Equivalences<PropertyExpression> children_prime : isa.getProperties().getDirectSub(isa.getProperties().getVertex(parent))) {
				PropertyExpression child_prime = children_prime.getRepresentative();

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
	
	private boolean check_directly_redundant(PropertyExpression parent, PropertyExpression child) {
		
		Equivalences<PropertyExpression> sp = sigmaChain.getProperties().getVertex(parent);
		Equivalences<PropertyExpression> sc = sigmaChain.getProperties().getVertex(child);
		
		// if one of them is not in the respective DAG
		if (sp == null || sc == null) 
			return false;

		Set<Equivalences<PropertyExpression>> spChildren =  sigmaChain.getProperties().getDirectSub(sp);
		
		if (!spChildren.contains(sc))
			return false;
		
		
		
		Equivalences<PropertyExpression> tc = isaChain.getProperties().getVertex(child);
		// if one of them is not in the respective DAG
		if (tc == null) 
			return false;
		
		Set<Equivalences<PropertyExpression>> scChildren = sigmaChain.getProperties().getSub(sc);
		Set<Equivalences<PropertyExpression>> tcChildren = isaChain.getProperties().getSub(tc);

		return scChildren.containsAll(tcChildren);
	}
	
	private boolean check_directly_redundant(BasicClassDescription parent, BasicClassDescription child) {
		
		Equivalences<BasicClassDescription> sp = sigmaChain.getClasses().getVertex(parent);
		Equivalences<BasicClassDescription> sc = sigmaChain.getClasses().getVertex(child);
		
		// if one of them is not in the respective DAG
		if (sp == null || sc == null) 
			return false;

		Set<Equivalences<BasicClassDescription>> spChildren =  sigmaChain.getClasses().getDirectSub(sp);
		
		if (!spChildren.contains(sc))
			return false;
		
		
		
		Equivalences<BasicClassDescription> tc = isaChain.getClasses().getVertex(child);
		// if one of them is not in the respective DAG
		if (tc == null) 
			return false;
		
		Set<Equivalences<BasicClassDescription>> scChildren = sigmaChain.getClasses().getSub(sc);
		Set<Equivalences<BasicClassDescription>> tcChildren = isaChain.getClasses().getSub(tc);

		return scChildren.containsAll(tcChildren);
	}
}
