package it.unibz.krdb.obda.ontology.impl;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.PropertyAssertion;
import it.unibz.krdb.obda.ontology.Property;

import java.util.HashSet;
import java.util.Set;

public class PropertyAssertionImpl implements PropertyAssertion {

	private static final long serialVersionUID = -8834975903851540150L;
	
	private final Property role;
	private final Constant o2;
	private final ObjectConstant o1;

	PropertyAssertionImpl(Property role, ObjectConstant o1, Constant o2) {
		this.role = role;
		this.o1 = o1;
		this.o2 = o2;
	}

	@Override
	public String toString() {
		return role.toString() + "(" + o1.toString() + ", " + o2.toString() + ")";
	}

	@Override
	public Set<Predicate> getReferencedEntities() {
		Set<Predicate> res = new HashSet<Predicate>();
		res.add(role.getPredicate());
		return res;
	}

	@Override
	public ObjectConstant getValue1() {
		return o1;
	}

	@Override
	public Constant getValue2() {
		return o2;
	}

	@Override
	public Property getProperty() {
		return role;
	}
}
