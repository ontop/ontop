package org.semanticweb.ontop.ontology.impl;

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

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.ontop.model.Constant;
import org.semanticweb.ontop.model.ObjectConstant;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.ontology.ClassAssertion;
import org.semanticweb.ontop.ontology.UnaryAssertion;

public class ClassAssertionImpl implements ClassAssertion, UnaryAssertion {

	private static final long serialVersionUID = 5689712345023046811L;

	private ObjectConstant object = null;

	private Predicate concept = null;

	ClassAssertionImpl(Predicate concept, ObjectConstant object) {
		this.object = object;
		this.concept = concept;
	}

	@Override
	public ObjectConstant getObject() {
		return object;
	}

	@Override
	public Predicate getConcept() {
		return concept;
	}

	public String toString() {
		return concept.toString() + "(" + object.toString() + ")";
	}

	@Override
	public Set<Predicate> getReferencedEntities() {
		Set<Predicate> res = new HashSet<Predicate>();
		res.add(concept);
		return res;
	}

	@Override
	public int getArity() {
		return 1;
	}

	@Override
	public Constant getValue() {
		return getObject();
	}

	@Override
	public Predicate getPredicate() {
		return concept;
	}
}
