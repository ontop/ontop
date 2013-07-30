/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.ontology.impl;

import it.unibz.krdb.obda.ontology.Property;

public class SubPropertyAxiomImpl extends AbstractSubDescriptionAxiom {

	private static final long serialVersionUID = -3020225654321319941L;

	SubPropertyAxiomImpl(Property included, Property including) {
		super(included, including);
	}

	public Property getSub() {
		return (Property) included;
	}

	public Property getSuper() {
		return (Property) including;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof SubPropertyAxiomImpl)) {
			return false;
		}
		SubPropertyAxiomImpl inc2 = (SubPropertyAxiomImpl) obj;
		if (!including.equals(inc2.including)) {
			return false;
		}
		return (included.equals(inc2.included));
	}
}
