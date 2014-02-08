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
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/***
 * An optimizer that will eliminate equivalences implied by the ontology,
 * simplifying the vocabulary of the ontology. This allows to reduce the number
 * of inferences implied by the ontology and eliminating redundancy. The output
 * is two components: a "equivalence map" that functional mapping from class
 * (property) expression to class (property) that can be used to retrieve the
 * class (property) of the optimized ontology that should be used instead of one
 * class (property) that has been removed; a TBox T' that has a simpler
 * vocabulary.
 * 
 * 
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class EquivalenceTBoxOptimizer {

	private Ontology optimizedTBox = null;
	private Map<Predicate, Description> equivalenceMap  = null;
	private TBoxReasonerImpl reasoner;
	private Set<Predicate> originalVocabulary;

	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

	public EquivalenceTBoxOptimizer(Ontology tbox) {
		originalVocabulary = tbox.getVocabulary();
		reasoner = new TBoxReasonerImpl(tbox);
	}

	/**
	 * the EquivalenceMap maps predicates to the representatives of their equivalence class (in TBox)
	 * 
	 * it contains 
	 * 		- an entry for each property name other than the representative of an equivalence class 
	 * 				(or its inverse)
	 * 		- an entry for each class name other than the representative of its equivalence class
	 */
	
	public Map<Predicate, Description> getEquivalenceMap() {
		
		if (equivalenceMap == null) {
			equivalenceMap = new HashMap<Predicate, Description>();

			for(Equivalences<Property> nodes : reasoner.getProperties()) {
				Property prop = nodes.getRepresentative();
				
				for (Property equiProp : nodes) {
					if (equiProp.equals(prop)) 
						continue;

					Property inverseProp = ofac.createProperty(prop.getPredicate(), !prop.isInverse());
					if (equiProp.equals(inverseProp))
						continue;         // no map entry if the property coincides with its inverse

					// if the property is different from its inverse, an entry is created 
					// (taking the inverses into account)
					if (equiProp.isInverse()) 
						equivalenceMap.put(equiProp.getPredicate(), inverseProp);
					else 
						equivalenceMap.put(equiProp.getPredicate(), prop);
				}
			}
			
			for(Equivalences<BasicClassDescription> nodes : reasoner.getClasses()) {
				BasicClassDescription node = nodes.getRepresentative();
				for (BasicClassDescription equivalent : nodes) {
					if (equivalent.equals(node)) 
						continue;

					if (equivalent instanceof OClass) {
						// an entry is created for a named class
						OClass equiClass = (OClass) equivalent;
						equivalenceMap.put(equiClass.getPredicate(), node);
					}
				}
			}			
		}
		return equivalenceMap;
	}

	/***
	 * Optimize will compute the implied hierarchy of the given ontology and 
	 * remove any cycles (compute equivalence
	 * classes). Then for each equivalent set (of classes/roles) it will keep
	 * only one representative and replace reference to any other node in the
	 * equivalence set with reference to the representative. The equivalences
	 * will be kept in an equivalence map, that relates classes/properties with
	 * its equivalent. Note that the equivalent of a class can only be another
	 * class, an the equivalent of a property can be another property, or the
	 * inverse of a property.
	 */

	public Ontology getOptimalTBox() {
		
		if (optimizedTBox == null) {
			
			optimizedTBox = ofac.createOntology();
			getEquivalenceMap();
			
			TBoxTraversal.traverse(reasoner, new TBoxTraverseListener() {

				@Override
				public void onInclusion(Property sub, Property sup) {
					// add an equivalence axiom ONLY IF the symbol does not appear in the EquivalenceMap
					if (!isInMap(sub) && !isInMap(sup)) {
						Axiom axiom = ofac.createSubPropertyAxiom(sub, sup);
						optimizedTBox.addEntities(axiom.getReferencedEntities());
						optimizedTBox.addAssertion(axiom);	
					}
				}

				@Override
				public void onInclusion(BasicClassDescription sub, BasicClassDescription sup) {
					// add an equivalence axiom ONLY IF the symbol does not appear in the EquivalenceMap
					if (!isInMap(sub) && !isInMap(sup)) {
						Axiom axiom = ofac.createSubClassAxiom(sub, sup);
						optimizedTBox.addEntities(axiom.getReferencedEntities());
						optimizedTBox.addAssertion(axiom);	
					}
				}
				
				public boolean isInMap(Description desc) {
					Predicate pred = VocabularyExtractor.getPredicate(desc);				
					return ((pred != null) && equivalenceMap.containsKey(pred));			
				}
			});

			// Last, add references to all the vocabulary of the original TBox
			
			Set<Predicate> extraVocabulary = new HashSet<Predicate>();
			extraVocabulary.addAll(originalVocabulary);
			extraVocabulary.removeAll(equivalenceMap.keySet());
			optimizedTBox.addEntities(extraVocabulary);
		}
		
		return optimizedTBox;
	}
	
	public void optimize() {
	}
}
