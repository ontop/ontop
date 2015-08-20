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

import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataRangeExpression;
import it.unibz.krdb.obda.ontology.DataSomeValuesFrom;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.ObjectSomeValuesFrom;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.OntologyVocabulary;
import it.unibz.krdb.obda.ontology.impl.DatatypeImpl;
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
	
		sigmaChain = TBoxReasonerImpl.getChainReasoner(TBoxReasonerImpl.getSigmaReasoner(isa));
	}

	// USED IN ONE TEST (SemanticReductionTest, with the empty Sigma)
	@Deprecated 
	public SigmaTBoxOptimizer(TBoxReasoner isa, TBoxReasonerImpl s) {		
		this.isa = isa;
		
		isaChain = TBoxReasonerImpl.getChainReasoner((TBoxReasonerImpl)isa);

		OntologyVocabulary voc = OntologyFactoryImpl.getInstance().createVocabulary();
		Ontology ont = OntologyFactoryImpl.getInstance().createOntology(voc);
		TBoxReasoner emptyReasoner = TBoxReasonerImpl.create(ont);
		
		sigmaChain = TBoxReasonerImpl.getChainReasoner(emptyReasoner);
	}
	
	public Ontology getReducedOntology() {
		if (optimizedTBox == null) {
			OntologyVocabulary voc = fac.createVocabulary();
			
			optimizedTBox = fac.createOntology(voc);
			//optimizedTBox.addEntities(vocabulary);

			log.debug("Starting semantic-reduction");

			TBoxTraversal.traverse(isa, new TBoxTraverseListener() {

				@Override
				public void onInclusion(ObjectPropertyExpression sub, ObjectPropertyExpression sup) {
					if (!check_redundant_role(sup, sub)) {
						optimizedTBox.addSubPropertyOfAxiomWithReferencedEntities(sub, sup);
					}
				}
				@Override
				public void onInclusion(DataPropertyExpression sub, DataPropertyExpression sup) {
					if (!check_redundant_role(sup, sub)) {
						optimizedTBox.addSubPropertyOfAxiomWithReferencedEntities(sub, sup);
					}
				}

				@Override
				public void onInclusion(DataRangeExpression sub, DataRangeExpression sup) {
					if (!sup.equals(sub) && !check_redundant(sup, sub))  {
						optimizedTBox.addSubClassOfAxiomWithReferencedEntities(sub, sup);
					}
				}
				public void onInclusion(ClassExpression sub, ClassExpression sup) {
					if (!sup.equals(sub) && !check_redundant(sup, sub))  {
						optimizedTBox.addSubClassOfAxiomWithReferencedEntities(sub, sup);
					}
				}
			});
		}
		return optimizedTBox;
	}

	
	
	
	
	
	
	
	
	private boolean check_redundant_role(ObjectPropertyExpression parent, ObjectPropertyExpression child) {

		if (check_directly_redundant_role(parent, child))
			return true;
		else {
//			log.debug("Not directly redundant role {} {}", parent, child);
			for (Equivalences<ObjectPropertyExpression> children_prime : 
							isa.getObjectPropertyDAG().getDirectSub(isa.getObjectPropertyDAG().getVertex(parent))) {
				ObjectPropertyExpression child_prime = children_prime.getRepresentative();

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

	private boolean check_redundant_role(DataPropertyExpression parent, DataPropertyExpression child) {

		if (check_directly_redundant_role(parent, child))
			return true;
		else {
//			log.debug("Not directly redundant role {} {}", parent, child);
			for (Equivalences<DataPropertyExpression> children_prime : 
							isa.getDataPropertyDAG().getDirectSub(isa.getDataPropertyDAG().getVertex(parent))) {
				DataPropertyExpression child_prime = children_prime.getRepresentative();

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
	
	private boolean check_directly_redundant_role(ObjectPropertyExpression parent, ObjectPropertyExpression child) {

		ObjectSomeValuesFrom existParentDesc = parent.getDomain();
		ObjectSomeValuesFrom existChildDesc = child.getDomain();

		return check_directly_redundant(parent, child) && 
				check_directly_redundant(existParentDesc, existChildDesc);
	}

	private boolean check_directly_redundant_role(DataPropertyExpression parent, DataPropertyExpression child) {

		DataSomeValuesFrom existParentDesc = parent.getDomainRestriction(DatatypeImpl.rdfsLiteral);
		DataSomeValuesFrom existChildDesc = child.getDomainRestriction(DatatypeImpl.rdfsLiteral);

		return check_directly_redundant(parent, child) && 
				check_directly_redundant(existParentDesc, existChildDesc);
	}
	
	private boolean check_redundant(ObjectPropertyExpression parent, ObjectPropertyExpression child) {
		if (check_directly_redundant(parent, child))
			return true;
		else {
			for (Equivalences<ObjectPropertyExpression> children_prime : 
						isa.getObjectPropertyDAG().getDirectSub(isa.getObjectPropertyDAG().getVertex(parent))) {
				ObjectPropertyExpression child_prime = children_prime.getRepresentative();

				if (!child_prime.equals(child) && 
						check_directly_redundant(child_prime, child) && 
						!check_redundant(child_prime, parent)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean check_redundant(DataPropertyExpression parent, DataPropertyExpression child) {
		if (check_directly_redundant(parent, child))
			return true;
		else {
			for (Equivalences<DataPropertyExpression> children_prime : 
							isa.getDataPropertyDAG().getDirectSub(isa.getDataPropertyDAG().getVertex(parent))) {
				DataPropertyExpression child_prime = children_prime.getRepresentative();

				if (!child_prime.equals(child) && 
						check_directly_redundant(child_prime, child) && 
						!check_redundant(child_prime, parent)) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	private boolean check_redundant(ClassExpression parent, ClassExpression child) {
		if (check_directly_redundant(parent, child))
			return true;
		else {
			for (Equivalences<ClassExpression> children_prime : isa.getClassDAG().getDirectSub(isa.getClassDAG().getVertex(parent))) {
				ClassExpression child_prime = children_prime.getRepresentative();

				if (!child_prime.equals(child) && 
						check_directly_redundant(child_prime, child) && 
						!check_redundant(child_prime, parent)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean check_redundant(DataRangeExpression parent, DataRangeExpression child) {
		if (check_directly_redundant(parent, child))
			return true;
		else {
			for (Equivalences<DataRangeExpression> children_prime : isa.getDataRangeDAG().getDirectSub(isa.getDataRangeDAG().getVertex(parent))) {
				DataRangeExpression child_prime = children_prime.getRepresentative();

				if (!child_prime.equals(child) && 
						check_directly_redundant(child_prime, child) && 
						!check_redundant(child_prime, parent)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean check_directly_redundant(ObjectPropertyExpression parent, ObjectPropertyExpression child) {
		
		Equivalences<ObjectPropertyExpression> sp = sigmaChain.getObjectPropertyDAG().getVertex(parent);
		Equivalences<ObjectPropertyExpression> sc = sigmaChain.getObjectPropertyDAG().getVertex(child);
		
		// if one of them is not in the respective DAG
		if (sp == null || sc == null) 
			return false;

		Set<Equivalences<ObjectPropertyExpression>> spChildren =  sigmaChain.getObjectPropertyDAG().getDirectSub(sp);
		
		if (!spChildren.contains(sc))
			return false;
		
		
		
		Equivalences<ObjectPropertyExpression> tc = isaChain.getObjectPropertyDAG().getVertex(child);
		// if one of them is not in the respective DAG
		if (tc == null) 
			return false;
		
		Set<Equivalences<ObjectPropertyExpression>> scChildren = sigmaChain.getObjectPropertyDAG().getSub(sc);
		Set<Equivalences<ObjectPropertyExpression>> tcChildren = isaChain.getObjectPropertyDAG().getSub(tc);

		return scChildren.containsAll(tcChildren);
	}

	
	private boolean check_directly_redundant(DataPropertyExpression parent, DataPropertyExpression child) {
		
		Equivalences<DataPropertyExpression> sp = sigmaChain.getDataPropertyDAG().getVertex(parent);
		Equivalences<DataPropertyExpression> sc = sigmaChain.getDataPropertyDAG().getVertex(child);
		
		// if one of them is not in the respective DAG
		if (sp == null || sc == null) 
			return false;

		Set<Equivalences<DataPropertyExpression>> spChildren =  sigmaChain.getDataPropertyDAG().getDirectSub(sp);
		
		if (!spChildren.contains(sc))
			return false;
		
		
		
		Equivalences<DataPropertyExpression> tc = isaChain.getDataPropertyDAG().getVertex(child);
		// if one of them is not in the respective DAG
		if (tc == null) 
			return false;
		
		Set<Equivalences<DataPropertyExpression>> scChildren = sigmaChain.getDataPropertyDAG().getSub(sc);
		Set<Equivalences<DataPropertyExpression>> tcChildren = isaChain.getDataPropertyDAG().getSub(tc);

		return scChildren.containsAll(tcChildren);
	}
	
	private boolean check_directly_redundant(ClassExpression parent, ClassExpression child) {
		
		Equivalences<ClassExpression> sp = sigmaChain.getClassDAG().getVertex(parent);
		Equivalences<ClassExpression> sc = sigmaChain.getClassDAG().getVertex(child);
		
		// if one of them is not in the respective DAG
		if (sp == null || sc == null) 
			return false;

		Set<Equivalences<ClassExpression>> spChildren =  sigmaChain.getClassDAG().getDirectSub(sp);
		
		if (!spChildren.contains(sc))
			return false;
		
		
		
		Equivalences<ClassExpression> tc = isaChain.getClassDAG().getVertex(child);
		// if one of them is not in the respective DAG
		if (tc == null) 
			return false;
		
		Set<Equivalences<ClassExpression>> scChildren = sigmaChain.getClassDAG().getSub(sc);
		Set<Equivalences<ClassExpression>> tcChildren = isaChain.getClassDAG().getSub(tc);

		return scChildren.containsAll(tcChildren);
	}
	
	private boolean check_directly_redundant(DataRangeExpression parent, DataRangeExpression child) {
		
		Equivalences<DataRangeExpression> sp = sigmaChain.getDataRangeDAG().getVertex(parent);
		Equivalences<DataRangeExpression> sc = sigmaChain.getDataRangeDAG().getVertex(child);
		
		// if one of them is not in the respective DAG
		if (sp == null || sc == null) 
			return false;

		Set<Equivalences<DataRangeExpression>> spChildren =  sigmaChain.getDataRangeDAG().getDirectSub(sp);
		
		if (!spChildren.contains(sc))
			return false;
		
		
		
		Equivalences<DataRangeExpression> tc = isaChain.getDataRangeDAG().getVertex(child);
		// if one of them is not in the respective DAG
		if (tc == null) 
			return false;
		
		Set<Equivalences<DataRangeExpression>> scChildren = sigmaChain.getDataRangeDAG().getSub(sc);
		Set<Equivalences<DataRangeExpression>> tcChildren = isaChain.getDataRangeDAG().getSub(tc);

		return scChildren.containsAll(tcChildren);
	}
	
}
