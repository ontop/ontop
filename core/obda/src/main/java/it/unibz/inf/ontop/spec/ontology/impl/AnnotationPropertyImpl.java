package it.unibz.inf.ontop.spec.ontology.impl;

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

import it.unibz.inf.ontop.spec.ontology.AnnotationProperty;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.simple.SimpleRDF;

/**
 * Represents AnnotationPropertyExpression from the OWL 2 QL Specification
 * 
 * AnnotationProperty := IRI
 * 
 *
 * 
 * @author Roman Kontchakov
 *
 */

public class AnnotationPropertyImpl implements AnnotationProperty {

	private final String name;
	private IRI iri;

	AnnotationPropertyImpl(String name) {
		this.name = name;
		this.iri = new SimpleRDF().createIRI(name);
	}

	@Override
	public IRI getIRI() {
		return iri;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		if (obj instanceof AnnotationPropertyImpl) {
			AnnotationPropertyImpl other = (AnnotationPropertyImpl) obj;
			return name.equals(other.name);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public String toString() {
		return name;
	}
}
