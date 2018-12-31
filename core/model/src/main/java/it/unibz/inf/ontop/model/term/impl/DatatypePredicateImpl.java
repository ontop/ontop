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
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;

import javax.annotation.Nonnull;

//import com.hp.hpl.jena.iri.IRI;

public class DatatypePredicateImpl extends FunctionSymbolImpl implements DatatypePredicate {

	@Nonnull
	private final RDFDatatype returnedType;

	protected DatatypePredicateImpl(@Nonnull RDFDatatype returnedType, @Nonnull TermType argumentType) {
		super(returnedType.toString(), ImmutableList.of(argumentType));
		this.returnedType = returnedType;
	}
	
	@Override
	public DatatypePredicateImpl clone() {
		return this;
	}


	@Override
	public RDFDatatype getReturnedType() {
		return returnedType;
	}
}
