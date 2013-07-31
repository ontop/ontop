/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.ontology.impl;

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.ontology.BinaryAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;

import java.util.HashSet;
import java.util.Set;

public class DataPropertyAssertionImpl implements DataPropertyAssertion, BinaryAssertion {

	private static final long serialVersionUID = -8174920394359563293L;
	private Predicate role;
	private ValueConstant o2;
	private ObjectConstant o1;

	DataPropertyAssertionImpl(Predicate attribute, ObjectConstant o1, ValueConstant o2) {
		this.role = attribute;
		this.o1 = o1;
		this.o2 = o2;
	}

	@Override
	public ObjectConstant getObject() {
		return o1;
	}

	@Override
	public ValueConstant getValue() {
		return o2;
	}

	@Override
	public Predicate getAttribute() {
		return role;
	}

	public String toString() {
		return role.toString() + "(" + o1.toString() + ", " + o2.toString() + ")";
	}

	@Override
	public Set<Predicate> getReferencedEntities() {
		Set<Predicate> res = new HashSet<Predicate>();
		res.add(role);
		return res;
	}

	@Override
	public int getArity() {
		return 2;
	}

	@Override
	public Constant getValue1() {
		return getObject();
	}

	@Override
	public Constant getValue2() {
		return getValue();
	}

	@Override
	public Predicate getPredicate() {
		return role;
	}
}
