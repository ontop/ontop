/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
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
