package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertyFunctionalAxiom;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;

public class PropertyFunctionalAxiomImpl implements PropertyFunctionalAxiom{

	private Property role = null;
	
	PropertyFunctionalAxiomImpl(Property role) {
		this.role = role;
	}
	
}
