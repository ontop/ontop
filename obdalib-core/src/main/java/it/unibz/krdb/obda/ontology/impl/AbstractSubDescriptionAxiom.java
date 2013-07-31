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
import it.unibz.krdb.obda.ontology.DataType;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeClassRestriction;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.SubDescriptionAxiom;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractSubDescriptionAxiom implements SubDescriptionAxiom {

	private static final long serialVersionUID = 5901203153410196148L;

	protected Description including = null; // righthand side

	protected Description included = null;

	private String string = null;

	private int hash = 0;

	public AbstractSubDescriptionAxiom(Description subDesc, Description superDesc) {
		if (subDesc == null || superDesc == null) {
			throw new RuntimeException("Recieved null in concept inclusion");
		}
		included = subDesc;
		including = superDesc;
		string = toString();
		hash = string.hashCode();
	}

	@Override
	public Set<Predicate> getReferencedEntities() {
		Set<Predicate> res = new HashSet<Predicate>();
		for (Predicate p : getPredicates(included)) {
			res.add(p);
		}
		for (Predicate p : getPredicates(including)) {
			res.add(p);
		}
		return res;
	}

	private Set<Predicate> getPredicates(Description desc) {
		Set<Predicate> preds = new HashSet<Predicate>();
		if (desc instanceof Property) {
			preds.add(((Property) desc).getPredicate());
		} else if (desc instanceof OClass) {
			preds.add(((OClass) desc).getPredicate());
		} else if (desc instanceof PropertySomeRestriction) {
			preds.add(((PropertySomeRestriction) desc).getPredicate());
		} else if (desc instanceof PropertySomeClassRestriction) {
			preds.add(((PropertySomeClassRestriction) desc).getPredicate());
			preds.add(((PropertySomeClassRestriction) desc).getFiller().getPredicate());
		} else if (desc instanceof DataType) {
			preds.add(((DataType) desc).getPredicate());
		} else {
			throw new UnsupportedOperationException("Cant understand: " + desc.toString());
		}
		return preds;
	}

	public int hashCode() {
		return hash;
	}

	public String toString() {
		if (string != null) {
			return string;
		}
		StringBuilder bf = new StringBuilder();
		bf.append(included.toString());
		bf.append(" ISA ");
		bf.append(including.toString());
		return bf.toString();
	}
}
