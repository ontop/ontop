package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

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

import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.ObjectSomeValuesFrom;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Intersection;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeWitnessGenerator {
	private final ObjectPropertyExpression property;
	private final OClass filler;

	private final Set<ClassExpression> concepts = new HashSet<ClassExpression>();
	private Set<ClassExpression> subconcepts;
	private ObjectSomeValuesFrom existsRinv;

	private final TBoxReasoner reasoner;

	private static final Logger log = LoggerFactory.getLogger(TreeWitnessGenerator.class);	
	private static final OntologyFactory ontFactory = OntologyFactoryImpl.getInstance();
	
	public TreeWitnessGenerator(TBoxReasoner reasoner, ObjectPropertyExpression property, OClass filler) {
		this.reasoner = reasoner;
		this.property = property;
		this.filler = filler;
	}

	// TODO: replace by OntologyVocabulary
	public static final OClass owlThing = ontFactory.createClass("http://www.w3.org/TR/2004/REC-owl-semantics-20040210/#owl_Thing");	
	
	// tree witness generators of the ontology (i.e., positive occurrences of \exists R.B)

	public static Collection<TreeWitnessGenerator> getTreeWitnessGenerators(TBoxReasoner reasoner) {
		
		Map<ClassExpression, TreeWitnessGenerator> gens = new HashMap<ClassExpression, TreeWitnessGenerator>();				

		// COLLECT GENERATING CONCEPTS (together with their declared subclasses)
		// TODO: improve the algorithm
		for (Equivalences<ClassExpression> set : reasoner.getClasses()) {
			Set<Equivalences<ClassExpression>> subClasses = reasoner.getClasses().getSub(set);
			boolean couldBeGenerating = set.size() > 1 || subClasses.size() > 1; 
			for (ClassExpression concept : set) {
				if (concept instanceof ObjectSomeValuesFrom && couldBeGenerating) {
					ObjectSomeValuesFrom some = (ObjectSomeValuesFrom)concept;
					TreeWitnessGenerator twg = gens.get(some);
					if (twg == null) {
						twg = new TreeWitnessGenerator(reasoner, some.getProperty(), owlThing);			
						gens.put(concept, twg);
					}
					for (Equivalences<ClassExpression> subClassSet : subClasses) {
						for (ClassExpression subConcept : subClassSet) {
							if (!subConcept.equals(concept)) {
								twg.concepts.add(subConcept);
								log.debug("GENERATING CI: {} <= {}", subConcept, some);
							}
						}
					}
				}				
			}
		}

		return gens.values();
		
	}
	
	
	public static Set<ClassExpression> getMaximalBasicConcepts(Collection<TreeWitnessGenerator> gens, TBoxReasoner reasoner) {
		Set<ClassExpression> concepts = new HashSet<ClassExpression>();
		for (TreeWitnessGenerator twg : gens) 
			concepts.addAll(twg.concepts);

		if (concepts.isEmpty())
			return concepts;
		
		if (concepts.size() == 1 && concepts.iterator().next() instanceof OClass)
			return concepts;
		
		log.debug("MORE THAN ONE GENERATING CONCEPT: {}", concepts);
		// add all sub-concepts of all \exists R
		Set<ClassExpression> extension = new HashSet<ClassExpression>();
		for (ClassExpression b : concepts) 
			if (b instanceof ObjectSomeValuesFrom)
				extension.addAll(reasoner.getClasses().getSubRepresentatives(b));
		concepts.addAll(extension);
		
		// use all concept names to subsume their sub-concepts
		{
			boolean modified = true; 
			while (modified) {
				modified = false;
				for (ClassExpression b : concepts) 
					if (b instanceof OClass) {
						Set<ClassExpression> bsubconcepts = reasoner.getClasses().getSubRepresentatives(b);
						Iterator<ClassExpression> i = concepts.iterator();
						while (i.hasNext()) {
							ClassExpression bp = i.next();
							if ((b != bp) && bsubconcepts.contains(bp)) { 
								i.remove();
								modified = true;
							}
						}
						if (modified)
							break;
					}
			}
		}
		
		// use all \exists R to subsume their sub-concepts of the form \exists R
		{
			boolean modified = true;
			while (modified) {
				modified = false;
				for (ClassExpression b : concepts) 
					if (b instanceof ObjectSomeValuesFrom) {
						ObjectSomeValuesFrom some = (ObjectSomeValuesFrom)b;
						ObjectPropertyExpression prop = (ObjectPropertyExpression) some.getProperty();
						Set<ObjectPropertyExpression> bsubproperties = reasoner.getObjectProperties().getSubRepresentatives(prop);
						Iterator<ClassExpression> i = concepts.iterator();
						while (i.hasNext()) {
							ClassExpression bp = i.next();
							if ((b != bp) && (bp instanceof ObjectSomeValuesFrom)) {
								ObjectSomeValuesFrom somep = (ObjectSomeValuesFrom)bp;
								ObjectPropertyExpression propp = somep.getProperty();
								
								if (bsubproperties.contains(propp)) {
									i.remove();
									modified = true;
								}
							}
						}
						if (modified)
							break;
					}
			}
		}
		
		return concepts;
	}
	
	
	public Set<ClassExpression> getSubConcepts() {
		if (subconcepts == null) {
			subconcepts = new HashSet<ClassExpression>();
			for (ClassExpression con : concepts)
				subconcepts.addAll(reasoner.getClasses().getSubRepresentatives(con));
		}
		return subconcepts;
	}
	
	
	public ObjectPropertyExpression getProperty() {
		return property;
	}
	
	private void ensureExistsRinv() {
		if (existsRinv == null) {
			if (property instanceof ObjectPropertyExpression) {
				ObjectPropertyExpression inv = ((ObjectPropertyExpression)property).getInverse();			
				existsRinv = ontFactory.createPropertySomeRestriction(inv);	
			}
//			else {
//				DataPropertyExpression inv = ((DataPropertyExpression)property).getInverse();			
//				existsRinv = ontFactory.createPropertySomeRestriction(inv);					
//			}
		}		
	}

	public boolean endPointEntailsAnyOf(Set<ClassExpression> subc) {
		ensureExistsRinv();
		return subc.contains(existsRinv) || subc.contains(filler);
	}
	
	public boolean endPointEntailsAnyOf(Intersection<ClassExpression> subc) {
		if (subc.isTop())
			return true;
		
		ensureExistsRinv();
		
		return subc.subsumes(existsRinv) || subc.subsumes(filler);
	}
	
	@Override 
	public String toString() {
		return "tw-generator E" + property.toString() + "." + filler.toString();
	}
	
	@Override
	public int hashCode() {
		return property.hashCode() ^ filler.hashCode();
	}
	
	@Override 
	public boolean equals(Object other) {
		if (other instanceof TreeWitnessGenerator) {
			TreeWitnessGenerator o = (TreeWitnessGenerator)other;
			return (this.property.equals(o.property) && this.filler.equals(o.filler));		
		}
		return false;
	}
}
