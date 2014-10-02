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

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TreeWitnessReasonerLite
 * 
 * a simple reasoner for DL-Lite that computes and gives 
 *       - subconcepts for a given concept 
 *       - subproperties for a given property
 * 
 * @author Roman Kontchakov
 *
 */


public class TreeWitnessReasonerCache {
	private final TBoxReasoner reasoner;
	
	// caching for predicate symbols 
	private final Map<Predicate, Set<BasicClassDescription>> predicateSubconcepts;
	private final Map<Predicate, Set<Property>> predicateSubproperties;
	private final Map<Predicate, Set<Property>> predicateSubpropertiesInv;

	// tree witness generators of the ontology (i.e., positive occurrences of \exists R.B)
	private final Collection<TreeWitnessGenerator> generators;

	private static final OntologyFactory ontFactory = OntologyFactoryImpl.getInstance();
	private static final Logger log = LoggerFactory.getLogger(TreeWitnessReasonerCache.class);	

	public static final OClass owlThing = ontFactory.createClass("http://www.w3.org/TR/2004/REC-owl-semantics-20040210/#owl_Thing");	
	
	
	public TreeWitnessReasonerCache(TBoxReasoner reasoner) {
		
		this.reasoner = reasoner;

		Map<ClassDescription, TreeWitnessGenerator> gens = new HashMap<ClassDescription, TreeWitnessGenerator>();
		
		predicateSubconcepts = new HashMap<Predicate, Set<BasicClassDescription>>();
		predicateSubproperties = new HashMap<Predicate, Set<Property>>();
		predicateSubpropertiesInv = new HashMap<Predicate, Set<Property>>();
		
		// COLLECT GENERATING CONCEPTS (together with their declared subclasses)
		// TODO: improve the algorithm
		for (Equivalences<BasicClassDescription> set : reasoner.getClasses()) {
			Set<Equivalences<BasicClassDescription>> subClasses = reasoner.getClasses().getSub(set);
			boolean couldBeGenerating = set.size() > 1 || subClasses.size() > 1; 
			for (BasicClassDescription concept : set) {
				if (concept instanceof PropertySomeRestriction && couldBeGenerating) {
					PropertySomeRestriction some = (PropertySomeRestriction)concept;
					TreeWitnessGenerator twg = gens.get(some);
					if (twg == null) {
						twg = new TreeWitnessGenerator(this, ontFactory.createObjectProperty(some.getPredicate().getName(), some.isInverse()), owlThing);			
						gens.put(concept, twg);
					}
					for (Equivalences<BasicClassDescription> subClassSet : subClasses) {
						for (BasicClassDescription subConcept : subClassSet) {
							if (!subConcept.equals(concept)) {
								twg.addConcept(subConcept);
								log.debug("GENERATING CI: {} <= {}", subConcept, some);
							}
						}
					}
				}				
			}
		}

		generators = gens.values();
	
	}
	
	/**
	 * getSubConcepts
	 * 
	 * @param con is a basic class description (concept name or \exists R)
	 * @return the set of subsconcepts of B
	 */
	
	public Set<BasicClassDescription> getSubConcepts(BasicClassDescription con) {
		return reasoner.getClasses().getSubRepresentatives(con);
	}

	public Set<BasicClassDescription> getSubConcepts(Predicate pred) {
		Set<BasicClassDescription> s = predicateSubconcepts.get(pred);
		if (s == null) {
			s = getSubConcepts(ontFactory.createClass(pred));
			predicateSubconcepts.put(pred, s);
		}
		return s;
	}

	public IntersectionOfConceptSets getSubConcepts(Collection<Function> atoms) {
		IntersectionOfConceptSets subc = new IntersectionOfConceptSets();
		for (Function a : atoms) {
			 if (a.getArity() != 1)
				 return IntersectionOfConceptSets.EMPTY;   // binary predicates R(x,x) cannot be matched to the anonymous part

			 if (!subc.intersect(getSubConcepts(a.getFunctionSymbol())))
				 return IntersectionOfConceptSets.EMPTY;
		}
		return subc;
	}
	
	
	/**
	 *  getSubproperties
	 *  
	 * @param prop is a property (property name or its inverse)
	 * @return the set of subproperties
	 */
	
	public Set<Property> getSubProperties(Predicate pred, boolean inverse) {
		Map<Predicate, Set<Property>> cache = (inverse ? predicateSubpropertiesInv : predicateSubproperties);
		Set<Property> s = cache.get(pred);
		if (s == null) {
			s = reasoner.getProperties().getSubRepresentatives(ontFactory.createProperty(pred, inverse));
			cache.put(pred, s);
		}
		return s;
	}
	
	public boolean isMoreSpecific(Function a1, Function a2) {
		if (a1 == a2 || a1.equals(a2))
			return true;

		if ((a2.getArity() == 1) && (a1.getArity() == 1)) {
			if (a1.getTerm(0).equals(a2.getTerm(0))) {
				Set<BasicClassDescription> subconcepts = getSubConcepts(a2.getFunctionSymbol());
				if (subconcepts.contains(ontFactory.createClass(a1.getFunctionSymbol()))) {
					log.debug("{} IS MORE SPECIFIC (1-1) THAN {}", a1, a2);
					return true;
				}
			}
		}
		else if ((a1.getArity() == 2) && (a2.getArity() == 1)) { // MOST USEFUL
			Term a2term = a2.getTerm(0);
			if (a1.getTerm(0).equals(a2term)) {
				PropertySomeRestriction prop = ontFactory.getPropertySomeRestriction(a1.getFunctionSymbol(), false);
				if (getSubConcepts(a2.getFunctionSymbol()).contains(prop)) {
					log.debug("{} IS MORE SPECIFIC (2-1) THAN ", a1, a2);
					return true;
				}
			}
			if (a1.getTerm(1).equals(a2term)) {
				PropertySomeRestriction prop = ontFactory.getPropertySomeRestriction(a1.getFunctionSymbol(), true);
				if (getSubConcepts(a2.getFunctionSymbol()).contains(prop)) {
					log.debug("{} IS MORE SPECIFIC (2-1) THAN {}", a1, a2);
					return true;
				}
			}
		}
		// TODO: implement the (2-2) check
		
		return false;
	}
	
	
	/**
	 * getGenerators
	 * 
	 * @return the collection of all tree witness generators for the ontology
	 */
	
	public Collection<TreeWitnessGenerator> getGenerators() {
		return generators;
	}
	
	
	
	/**
	 * computes intersections of sets of properties
	 * 
	 * internal representation: the downward-saturated set of properties (including all sub-properties)
	 * 
	 * @author roman
	 *
	 */
	
	public static class IntersectionOfProperties {
		private Set<Property> set;
		
		public IntersectionOfProperties() {
		}
		
		public IntersectionOfProperties(Set<Property> set2) {
			set = (set2 == null) ? null : new HashSet<Property>(set2);
		}
		
		public boolean intersect(Set<Property> subp) {
			if (set == null) // first atom
				set = new HashSet<Property>(subp);
			else
				set.retainAll(subp);
			
			if (set.isEmpty()) {
				set = Collections.EMPTY_SET;
				return false;
			}
			else
				return true;
		}
		
		public void clear() {
			set = null;
		}
		
		public Set<Property> get() {
			return set;
		}
		
		@Override
		public String toString() {
			return ((set == null) ? "properties TOP" : "properties " + set.toString());
		}
	}
	
	
	
	
	public static class IntersectionOfConceptSets {
		public static final IntersectionOfConceptSets EMPTY = new IntersectionOfConceptSets(Collections.EMPTY_SET);
		
		private Set<BasicClassDescription> set;
		
		public IntersectionOfConceptSets() {			
		}

		public IntersectionOfConceptSets(Set<BasicClassDescription> subc) {			
			set = (subc == null) ?  null : (subc.isEmpty() ? Collections.EMPTY_SET : new HashSet<BasicClassDescription>(subc));
		}

		public IntersectionOfConceptSets(IntersectionOfConceptSets s2) {
			this(s2.set);
		}
		
		public boolean intersect(IntersectionOfConceptSets subc) {
			// null denotes \top
			if (subc.set == null) 
				return !isEmpty();
			
			return intersect(subc.set);
		}	

		public boolean intersect(Set<BasicClassDescription> subc) {
			// intersection with the empty set is empty
			if (subc.isEmpty()) {
				set = Collections.EMPTY_SET;
				return false;
			}

			if (set == null) { // the intersection has not been initialised
				set = new HashSet<BasicClassDescription>(subc);
				return true;
			}

			set.retainAll(subc);
			if (set.isEmpty()) {
				set = Collections.EMPTY_SET;
				return false;
			}
			return true;
		}	
		
		public boolean isEmpty() {
			return ((set != null) && set.isEmpty());
		}
		
		public Set<BasicClassDescription> get() {
			return set;
		}
		
		public void clear() {
			set = null;
		}
	}	
}
