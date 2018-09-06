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

import it.unibz.inf.ontop.model.term.functionsymbol.URITemplatePredicate;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.IntStream;

public class URITemplatePredicateImpl extends FunctionSymbolImpl implements URITemplatePredicate {

	// The name of the function that creates URI's in Quest
	private static final String URI_PREFIX = "URI";
	private final ObjectRDFType type;

	protected URITemplatePredicateImpl(int arity, String suffix, TypeFactory typeFactory) {
		super(URI_PREFIX  + arity + suffix, IntStream.range(0, arity)
				.boxed()
				// TODO: require strings
				.map(i -> typeFactory.getAbstractAtomicTermType())
				.collect(ImmutableCollectors.toList()));
		type = typeFactory.getIRITermType();
	}
	
	@Override
	public URITemplatePredicateImpl clone() {
		return this;
	}

	@Override
	public RDFTermType getReturnedType() {
		return type;
	}
}
