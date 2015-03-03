package org.semanticweb.ontop.model.impl;

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

import org.semanticweb.ontop.model.AlgebraOperatorPredicate;

public class AlgebraOperatorPredicateImpl extends PredicateImpl implements AlgebraOperatorPredicate {

	private static final long serialVersionUID = 9152448029926695852L;

	/**
	 * Constructs a datatype predicate with one term. This is a usual construct
	 * where the type of the term represents the datatype itself.
	 * 
	 * @param name
	 *            The predicate name.
	 * @param type
	 *            The datatype that the term holds.
	 */
	protected AlgebraOperatorPredicateImpl(String name) {
		// TODO (ROMAN): why does it have arity two but one STRING argument?
		super(name, 2, new COL_TYPE[] { COL_TYPE.STRING });
	}


	@Override
	public AlgebraOperatorPredicateImpl clone() {
		return this;
	}
}
