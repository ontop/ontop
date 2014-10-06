package it.unibz.krdb.obda.owlrefplatform.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;

public class EquivalenceMap {

	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	
	private Map<Predicate, Description> equivalenceMap;

	private EquivalenceMap(Map<Predicate, Description> equivalenceMap) {
		this.equivalenceMap = equivalenceMap;
	}
		
	public OClass getClassRepresentative(Predicate p) {
		return (OClass)equivalenceMap.get(p);
	}
	
	public Property getPropertyRepresentative(Predicate p) {
		return (Property)equivalenceMap.get(p);
	}
	
	
	
	

	/**
	 * the EquivalenceMap maps predicates to the representatives of their equivalence class (in TBox)
	 * 
	 * it contains 
	 * 		- an entry for each property name other than the representative of an equivalence class 
	 * 				(or its inverse)
	 * 		- an entry for each class name other than the representative of its equivalence class
	 */

		
	public static EquivalenceMap getEquivalenceMap(TBoxReasoner reasoner) {
		
		Map<Predicate, Description> equivalenceMap = new HashMap<Predicate, Description>();

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
		
		return new EquivalenceMap(equivalenceMap);
	}
	
	public static EquivalenceMap getEmptyEquivalenceMap() {
		return new EquivalenceMap(Collections.<Predicate, Description> emptyMap());
	}	
}
