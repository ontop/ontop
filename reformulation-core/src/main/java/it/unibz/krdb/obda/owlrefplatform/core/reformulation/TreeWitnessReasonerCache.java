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
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Intersection;
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
	

	public Intersection<BasicClassDescription> getSubConcepts(Collection<Function> atoms) {
		Intersection<BasicClassDescription> subc = new Intersection<BasicClassDescription>();
		for (Function a : atoms) {
			 if (a.getArity() != 1) {
				 subc.setToBottom();   // binary predicates R(x,x) cannot be matched to the anonymous part
				 break;
			 }
			 
			 Predicate pred = a.getFunctionSymbol();
			 Set<BasicClassDescription> s = predicateSubconcepts.get(pred);
			 if (s == null) {
				 s = reasoner.getClasses().getSubRepresentatives(ontFactory.createClass(pred));
				predicateSubconcepts.put(pred, s);
			 }
			 
			 subc.intersectWith(s);
			 if (subc.isBottom())
				 break;
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
	
}
