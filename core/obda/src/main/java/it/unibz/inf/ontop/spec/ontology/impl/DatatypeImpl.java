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

import it.unibz.inf.ontop.model.vocabulary.RDFS;
import it.unibz.inf.ontop.spec.ontology.Datatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

public class DatatypeImpl implements Datatype {

	private final IRI iri;

	public static final  Datatype rdfsLiteral; 
	
	static {
	    rdfsLiteral = new DatatypeImpl(RDFS.LITERAL);
	}
	
	DatatypeImpl(@Nonnull IRI iri) {
		this.iri = iri;
	}

	@Override
	public IRI getIRI() {
		return iri;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DatatypeImpl) {
			DatatypeImpl type2 = (DatatypeImpl) obj;
			return (iri.equals(type2.getIRI()));
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return iri.hashCode();
	}
	
	@Override
	public String toString() {
		return iri.getIRIString();
	}
	
}
