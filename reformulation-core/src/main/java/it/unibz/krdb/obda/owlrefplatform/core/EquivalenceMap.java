package it.unibz.krdb.obda.owlrefplatform.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;

public class EquivalenceMap {

	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	
	private final Map<Predicate, OClass> classEquivalenceMap;
	private final Map<Predicate, Property> propertyEquivalenceMap;

	private EquivalenceMap(Map<Predicate, OClass> classEquivalenceMap, Map<Predicate, Property> propertyEquivalenceMap) {
		this.classEquivalenceMap = classEquivalenceMap;
		this.propertyEquivalenceMap = propertyEquivalenceMap;
	}
		
	public OClass getClassRepresentative(Predicate p) {
		return classEquivalenceMap.get(p);
	}
	
	public Property getPropertyRepresentative(Predicate p) {
		return propertyEquivalenceMap.get(p);
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
		
		Map<Predicate, Property> propertyEquivalenceMap = new HashMap<Predicate, Property>();

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
					propertyEquivalenceMap.put(equiProp.getPredicate(), inverseProp);
				else 
					propertyEquivalenceMap.put(equiProp.getPredicate(), prop);
			}
		}

		Map<Predicate, OClass> classEquivalenceMap = new HashMap<Predicate, OClass>();
		
		for(Equivalences<BasicClassDescription> nodes : reasoner.getClasses()) {
			BasicClassDescription node = nodes.getRepresentative();
			for (BasicClassDescription equivalent : nodes) {
				if (equivalent.equals(node)) 
					continue;

				if (equivalent instanceof OClass) {
					// an entry is created for a named class
					OClass equiClass = (OClass) equivalent;
					classEquivalenceMap.put(equiClass.getPredicate(), (OClass)node);
				}
			}
		}			
		
		return new EquivalenceMap(classEquivalenceMap, propertyEquivalenceMap);
	}
	
	public static EquivalenceMap getEmptyEquivalenceMap() {
		return new EquivalenceMap(Collections.<Predicate, OClass> emptyMap(), Collections.<Predicate, Property> emptyMap());
	}
	
}
