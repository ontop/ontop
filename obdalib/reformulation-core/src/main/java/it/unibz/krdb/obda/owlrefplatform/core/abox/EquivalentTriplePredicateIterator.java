package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.Iterator;
import java.util.Map;

public class EquivalentTriplePredicateIterator implements Iterator<Assertion> {

	private Iterator<Assertion> originalIterator;
	private Map<Predicate, Description> equivalenceMap;
	
	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	
	public EquivalentTriplePredicateIterator(Iterator<Assertion> iterator, Map<Predicate, Description> equivalences) {
		originalIterator = iterator;
		equivalenceMap = equivalences;
	}
	
	@Override
	public boolean hasNext() {
		return originalIterator.hasNext();
	}

	@Override
	public Assertion next() {
		Assertion assertion = originalIterator.next();
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

	@Override
	public void remove() {
		originalIterator.remove();
	}
}
