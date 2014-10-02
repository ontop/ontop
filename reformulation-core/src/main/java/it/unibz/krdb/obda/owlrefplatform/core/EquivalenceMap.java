package it.unibz.krdb.obda.owlrefplatform.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;

public class EquivalenceMap {

	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
	
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
	
	
	public Function getNormal(Function atom) {
		Predicate p = atom.getPredicate();
		
		if (p.getArity() == 1) {
			OClass equivalent = getClassRepresentative(p);
			if (equivalent != null)
				return dfac.getFunction(equivalent.getPredicate(), atom.getTerms());
		} 
		else {
			Property equivalent = getPropertyRepresentative(p);
			if (equivalent != null) {
				if (!equivalent.isInverse()) 
					return dfac.getFunction(equivalent.getPredicate(), atom.getTerms());
				else 
					return dfac.getFunction(equivalent.getPredicate(), atom.getTerm(1), atom.getTerm(0));
			}
		}
		return atom;
	}

	// used in EquivalentTriplePredicateIterator
	
	public Assertion getNormal(Assertion assertion) {
		if (assertion instanceof ClassAssertion) {
			ClassAssertion ca = (ClassAssertion) assertion;
			Predicate concept = ca.getConcept();
			OClass description = getClassRepresentative(concept);
			
			if (description != null) {
				ObjectConstant object = ca.getObject();
				return ofac.createClassAssertion(description.getPredicate(), object);
			}			
		} 
		else if (assertion instanceof ObjectPropertyAssertion) {
			ObjectPropertyAssertion opa = (ObjectPropertyAssertion) assertion;
			Predicate role = opa.getRole();
			Property property = getPropertyRepresentative(role);
			
			if (property != null) {
				ObjectConstant object1 = opa.getFirstObject();
				ObjectConstant object2 = opa.getSecondObject();
				if (property.isInverse()) {
					return ofac.createObjectPropertyAssertion(property.getPredicate(), object2, object1);
				} else {
					return ofac.createObjectPropertyAssertion(property.getPredicate(), object1, object2);
				}
			}
		} 
		else if (assertion instanceof DataPropertyAssertion) {
			DataPropertyAssertion dpa = (DataPropertyAssertion) assertion;
			Predicate attribute = dpa.getAttribute();
			Property property = getPropertyRepresentative(attribute);
			
			if (property != null) {
				ObjectConstant object = dpa.getObject();
				ValueConstant constant = dpa.getValue();
				return ofac.createDataPropertyAssertion(property.getPredicate(), object, constant);
			}
		}
		return assertion;
	}
	
	
	
	/* ALTERNATIVE FROM QueryVocabularyValidator
	Description equivalent = equivalences.get(atom.getFunctionSymbol());
	if (equivalent == null) {
		// Nothing to replace 
		continue;
	}
	Function newatom = null;

	if (equivalent instanceof OClass) {
		newatom = fac.getFunction(((OClass) equivalent).getPredicate(), atom.getTerm(0));
	} else if (equivalent instanceof Property) {
		Property equiproperty = (Property) equivalent;
		if (!equiproperty.isInverse()) {
			newatom = fac.getFunction(equiproperty.getPredicate(), atom.getTerm(0), atom.getTerm(1));
		} else {
			newatom = fac.getFunction(equiproperty.getPredicate(), atom.getTerm(1), atom.getTerm(0));
		}
	}
	*/

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
