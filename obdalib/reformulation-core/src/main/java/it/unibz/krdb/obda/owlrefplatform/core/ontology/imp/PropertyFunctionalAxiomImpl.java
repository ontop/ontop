package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertyFunctionalAxiom;

import java.util.Collections;
import java.util.Set;

public class PropertyFunctionalAxiomImpl implements PropertyFunctionalAxiom{

	private Property role = null;
	
	PropertyFunctionalAxiomImpl(Property role) {
		this.role = role;
	}

	@Override
	public Set<Predicate> getReferencedEntities() {
		return Collections.singleton(role.getPredicate());
	}
	
}
