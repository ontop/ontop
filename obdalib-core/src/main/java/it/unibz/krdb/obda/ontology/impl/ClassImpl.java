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
import it.unibz.krdb.obda.ontology.OClass;

public class ClassImpl implements OClass {

	private static final long serialVersionUID = -4930755519806785384L;

	private Predicate predicate;

	private String str;

	ClassImpl(Predicate p) {
		this.predicate = p;
		str = predicate.toString();
	}

	public Predicate getPredicate() {
		return predicate;
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof ClassImpl)) {
			return false;
		}
		ClassImpl concept2 = (ClassImpl) obj;
		return (predicate.equals(concept2.getPredicate()));
	}

	public String toString() {
		return str;
	}
}
