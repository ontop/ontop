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

import org.semanticweb.ontop.model.BNodePredicate;

public class BNodePredicateImpl extends PredicateImpl implements BNodePredicate {

	private static final long serialVersionUID = -1546325236776439443L;
	
	// The name of the function that creates URI's in Quest
	public static final String QUEST_BNODE = "BNODE";

	public BNodePredicateImpl(int arity) {
		// TODO: BAD CODE! Predicate shouldn't store the arity and the type.
		super(QUEST_BNODE, arity, null);
	}

	@Override
	public BNodePredicateImpl clone() {
		return this;
	}
}
