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

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import java.util.Map;

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

	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

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

	public static TBoxReasoner getOptimalTBox0(final TBoxReasoner reasoner) {
		
		return TBoxReasonerImpl.getEquivalenceSimplifiedReasoner(reasoner);
/*		
		final Ontology optimizedTBox = ofac.createOntology();
		//final Set<Predicate> extraVocabulary = new HashSet<Predicate>();
		//extraVocabulary.addAll(originalVocabulary);
		//extraVocabulary.removeAll(equivalenceMap.keySet());
		
		final Map<Predicate, Property> propertyMap = TBoxReasonerImpl.getPropertyEquivalenceMap(reasoner);
		final Map<Predicate, OClass> classMap = TBoxReasonerImpl.getClassEquivalenceMap(reasoner);
		
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
			
			public boolean isInMap(BasicClassDescription desc) {
				if (desc instanceof OClass) {
					Predicate pred = ((OClass)desc).getPredicate();
					OClass rep = classMap.get(pred);
					if (rep != null) 
						return true;
				}
				else if (desc instanceof PropertySomeRestriction) {
					Predicate pred = ((PropertySomeRestriction)desc).getPredicate();
					Property rep = propertyMap.get(pred);
					if (rep != null) 
						return true;
				}
				return false;
			}
			public boolean isInMap(Property desc) {
				//Predicate pred = VocabularyExtractor.getPredicate(desc);				
				//return ((pred != null) && equivalenceMap.containsKey(pred));			
				Property rep = propertyMap.get(desc.getPredicate());
				if (rep != null) 
					return true;
				
				return false;
			}
		});

		return new TBoxReasonerImpl(optimizedTBox, classMap, propertyMap);		
*/	
	}
}
