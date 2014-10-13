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
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

	private static final OntologyFactory ontFactory = OntologyFactoryImpl.getInstance();
	
	public TreeWitnessReasonerCache(TBoxReasoner reasoner) {
		
		this.reasoner = reasoner;

		predicateSubconcepts = new HashMap<Predicate, Set<BasicClassDescription>>();
		predicateSubproperties = new HashMap<Predicate, Set<Property>>();
		predicateSubpropertiesInv = new HashMap<Predicate, Set<Property>>();
		
	
	}
	
	/**
	 * getSubConcepts
	 * 
	 * @param con is a basic class description (concept name or \exists R)
	 * @return the set of subsconcepts of B
	 */
	

	public IntersectionOfConceptSets getSubConcepts(Collection<Function> atoms) {
		IntersectionOfConceptSets subc = new IntersectionOfConceptSets();
		for (Function a : atoms) {
			 if (a.getArity() != 1)
				 return IntersectionOfConceptSets.EMPTY;   // binary predicates R(x,x) cannot be matched to the anonymous part

			 Predicate pred = a.getFunctionSymbol();
			 Set<BasicClassDescription> s = predicateSubconcepts.get(pred);
			 if (s == null) {
				 s = reasoner.getClasses().getSubRepresentatives(ontFactory.createClass(pred));
				predicateSubconcepts.put(pred, s);
			 }
			 
			 if (!subc.intersect(s))
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
