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
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAGBuilder;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAGImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
 
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/***
 * An optimizer that will eliminate equivalences implied by the ontology,
 * simplifying the vocabulary of the ontology. This allows to reduce the number
 * of inferences implied by the onotlogy and eliminating redundancy. The output
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

	private Ontology optimalTBox = null;
	private Map<Predicate, Description> equivalenceMap  = null;
	private TBoxReasonerImpl reasoner;
	private Set<Predicate> originalVocabulary;

	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

	public EquivalenceTBoxOptimizer(Ontology tbox) {
		originalVocabulary = tbox.getVocabulary();
		DAGImpl dag = DAGBuilder.getDAG(tbox);
		reasoner = new TBoxReasonerImpl(dag);
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
	 * 
	 *
	 */
	public void optimize() {

		getEquivalenceMap();
		
		optimalTBox = ofac.createOntology();
		
		for(Set<Description> nodeClass : reasoner.getNodes()) {
			Description firstNode = nodeClass.iterator().next();
			Description node = reasoner.getRepresentativeFor(firstNode);
			
			for (Set<Description> descendants : reasoner.getDescendants(node)) {
				Description firstDescendant = descendants.iterator().next();
				Description descendant = reasoner.getRepresentativeFor(firstDescendant);

				if (!descendant.equals(node))  // exclude trivial inclusions
					addToTBox(optimalTBox, descendant, node);
			}
			for (Description equivalent : reasoner.getEquivalences(node)) {
				if (!equivalent.equals(node)) {
					Predicate equivalentp = null;
					if (equivalent instanceof Property)
						equivalentp = ((Property)equivalent).getPredicate();
					else if (equivalent instanceof OClass)
						equivalentp = ((OClass)equivalent).getPredicate();
					else if (equivalent instanceof PropertySomeRestriction)
						equivalentp = ((PropertySomeRestriction)equivalent).getPredicate();
					
					// add an equivalence axiom ONLY IF the symbol does not appear in the EquivalenceMap
					if ((equivalentp == null) || !equivalenceMap.containsKey(equivalentp)) {
						addToTBox(optimalTBox, node, equivalent);					
						addToTBox(optimalTBox, equivalent, node);
					}
				}
			}
		}

		// Last, add references to all the vocabulary of the original TBox
		
		Set<Predicate> extraVocabulary = new HashSet<Predicate>();
		extraVocabulary.addAll(originalVocabulary);
		extraVocabulary.removeAll(equivalenceMap.keySet());
		optimalTBox.addEntities(extraVocabulary);
	}

	private static void addToTBox(Ontology o, Description s, Description t) {
		Axiom axiom;
		if (s instanceof ClassDescription) {
			axiom = ofac.createSubClassAxiom((ClassDescription) s, (ClassDescription) t);
		} 
		else {
			axiom = ofac.createSubPropertyAxiom((Property) s, (Property) t);
		}
		o.addEntities(axiom.getReferencedEntities());
		o.addAssertion(axiom);	
	}
	
	public Ontology getOptimalTBox() {
		return this.optimalTBox;
	}

	public Map<Predicate, Description> getEquivalenceMap() {
		
		if (equivalenceMap == null) {
			equivalenceMap = new HashMap<Predicate, Description>();

			for(Set<Description> nodeClass : reasoner.getNodes()) {
				Description firstNode = nodeClass.iterator().next();
				Description node = reasoner.getRepresentativeFor(firstNode);
				
				Set<Description> equivalents = reasoner.getEquivalences(node);
				
				if (node instanceof Property) {
					for (Description equivalent : equivalents) {
						if (equivalent.equals(node)) 
							continue;

						Property prop = (Property) node;
						Property inverseProp = ofac.createProperty(prop.getPredicate(), !prop.isInverse());

						Property equiProp = (Property) equivalent;
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
				else { // is a class 
					for (Description equivalent : equivalents) {
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
		}
		return equivalenceMap;
	}
}
