package it.unibz.krdb.obda.owlrefplatform.core;

import java.util.Collection;
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
	
	public Assertion getNormal(Assertion assertion) {
		if (assertion instanceof ClassAssertion) {
			ClassAssertion ca = (ClassAssertion) assertion;
			Predicate concept = ca.getConcept();
			ObjectConstant object = ca.getObject();
			
			Description description = equivalenceMap.get(concept);
			if (description != null) {
				return ofac.createClassAssertion(((OClass) description).getPredicate(), object);
			}			
		} else if (assertion instanceof ObjectPropertyAssertion) {
			ObjectPropertyAssertion opa = (ObjectPropertyAssertion) assertion;
			Predicate role = opa.getRole();
			ObjectConstant object1 = opa.getFirstObject();
			ObjectConstant object2 = opa.getSecondObject();
			
			Description description = equivalenceMap.get(role);
			if (description != null) {
				Property property = (Property) description;
				if (property.isInverse()) {
					return ofac.createObjectPropertyAssertion(property.getPredicate(), object2, object1);
				} else {
					return ofac.createObjectPropertyAssertion(property.getPredicate(), object1, object2);
				}
			}
		} else if (assertion instanceof DataPropertyAssertion) {
			DataPropertyAssertion dpa = (DataPropertyAssertion) assertion;
			Predicate attribute = dpa.getAttribute();
			ObjectConstant object = dpa.getObject();
			ValueConstant constant = dpa.getValue();
			
			Description description = equivalenceMap.get(attribute);
			if (description != null) {
				return ofac.createDataPropertyAssertion(((Property) description).getPredicate(), object, constant);
			}
		}
		return assertion;
	}
	
	// TESTS ONLY
	public boolean containsKey(Predicate p) {
		return equivalenceMap.containsKey(p);
	}
	
	// TESTS ONLY
	public int keySetSize() {
		return equivalenceMap.keySet().size();
	}

	// TO BE REMOVED
	@Deprecated
	public Description getValue(Predicate p) {
		return equivalenceMap.get(p);
	}
	
	// TO BE REMOVED: USED ONLY ONCE
	@Deprecated
	public Set<Predicate> keySet() {
		return equivalenceMap.keySet();
	}	
	
	// TO BE REMOVED
	@Deprecated
	public Map<Predicate, Description> getInternalMap() {
		return equivalenceMap;
	}
	
	public Function getNormal(Function atom) {
		Predicate p = atom.getPredicate();
		Function newatom = null;
		if (p.getArity() == 1) {
//			Description description = fac.createClass(p);
			Description equivalent = equivalenceMap.get(p);
			if (equivalent == null)
				newatom = atom;
			else {
				newatom = dfac.getFunction(((OClass) equivalent).getPredicate(), atom.getTerms());

			}
		} else {
//			Description description = fac.createProperty(p);
			Description equivalent = equivalenceMap.get(p);
			if (equivalent == null)
				newatom = atom;
			else {
				Property equiprop = (Property) equivalent;
				if (!equiprop.isInverse()) {
					newatom = dfac.getFunction(equiprop.getPredicate(), atom.getTerms());
				} else {
					newatom = dfac.getFunction(equiprop.getPredicate(), atom.getTerms().get(1), atom.getTerm(0));
				}
			}
		}
		return newatom;
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
