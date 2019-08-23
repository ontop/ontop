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

import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import org.apache.commons.rdf.api.IRI;

import java.util.stream.Stream;


/**
 * Provides a storage to put the URI constant.
 */
public class IRIConstantImpl implements IRIConstant {

	private final int identifier;
	private final IRI iri;
	private final ObjectRDFType type;

	protected IRIConstantImpl(IRI iri, TypeFactory typeFactory) {
		this.iri = iri;
		this.identifier = iri.hashCode();
		this.type = typeFactory.getIRITermType();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof IRIConstantImpl)) {
			return false;
		}
		IRIConstantImpl uri2 = (IRIConstantImpl) obj;
		return this.identifier == uri2.identifier;
	}

	@Override
	public int hashCode() {
		return identifier;
	}

	@Override
	public IRI getIRI() {
		return iri;
	}

	@Override
	public IRIConstant clone() {
		return this;
	}

	@Override
	public boolean isGround() {
		return true;
	}

	@Override
	public Stream<Variable> getVariableStream() {
		return Stream.of();
	}

	@Override
	public String toString() {
		return "<" + iri.getIRIString() + ">";
	}

	@Override
	public ObjectRDFType getType() {
		return type;
	}

	@Deprecated
	public String getValue() {
		return iri.getIRIString();
	}
}
