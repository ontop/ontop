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

import it.unibz.inf.ontop.model.term.functionsymbol.BNodePredicate;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.IntStream;

public class BNodePredicateImpl extends FunctionSymbolImpl implements BNodePredicate {
	
	// The name of the function that creates URI's in Quest
	private static final String QUEST_BNODE = "BNODE";
	private final ObjectRDFType type;

	protected BNodePredicateImpl(int arity, TypeFactory typeFactory) {
		super(QUEST_BNODE, IntStream.range(0, arity)
				.boxed()
				// TODO: require strings
				.map(i -> typeFactory.getAbstractAtomicTermType())
				.collect(ImmutableCollectors.toList()));
		type = typeFactory.getBlankNodeType();
	}

	@Override
	public BNodePredicateImpl clone() {
		return this;
	}

	@Override
	public ObjectRDFType getReturnedType() {
		return type;
	}
}
