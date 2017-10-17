package it.unibz.inf.ontop.model.term.impl;

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

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.functionsymbol.DatatypePredicate;
import it.unibz.inf.ontop.model.type.TermType;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

//import com.hp.hpl.jena.iri.IRI;

public class DatatypePredicateImpl extends PredicateImpl implements DatatypePredicate {

	private static final long serialVersionUID = -6678449661465775977L;
	private final IRI iri;

	/**
	 * Constructs a datatype predicate with one term. This is a usual construct
	 * where the type of the term represents the datatype itself.
	 * 
	 * @param iri
	 * 			The predicate IRI.
	 * @param type
	 * 			The datatype that the term holds.
	 */
	public DatatypePredicateImpl(@Nonnull IRI iri, @Nonnull TermType type) {
		super(iri.getIRIString(), 1, ImmutableList.of(type));
		this.iri = iri;
	}
	
	/**
	 * Construct a datatype predicate with two or more terms. The first term
	 * used to hold the value and the others are for any additional information.
	 * An example for using this constructor is the rdfs:Literal(value, lang).
	 * The predicate uses the second term to put the language tag.
	 * 
	 * @param iri
	 * 			The predicate IRI.
	 * @param types
	 * 			The datatypes that each term holds.
	 */
	public DatatypePredicateImpl(@Nonnull IRI iri, @Nonnull  ImmutableList<TermType> types) {
		super(iri.getIRIString(), types.size(), types);
		this.iri = iri;
	}
	
	@Override
	public DatatypePredicateImpl clone() {
		return this;
	}

	@Override
	public IRI getIRI() {
		return iri;
	}
}
