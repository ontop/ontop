package it.unibz.krdb.obda.ontology.impl;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertyFunctionalAxiom;

import java.util.Collections;
import java.util.Set;

public class PropertyFunctionalAxiomImpl implements PropertyFunctionalAxiom{

	private static final long serialVersionUID = 6020134666314925589L;
	
	private Property role = null;
	
	PropertyFunctionalAxiomImpl(Property role) {
		this.role = role;
	}

	@Override
	public Set<Predicate> getReferencedEntities() {
		return Collections.singleton(role.getPredicate());
	}	
}
