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

import java.util.Collections;
import java.util.Set;

import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.ontology.Property;
import org.semanticweb.ontop.ontology.PropertyFunctionalAxiom;

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
