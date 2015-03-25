package it.unibz.krdb.obda.model.impl;

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

import it.unibz.krdb.obda.model.Variable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class AnonymousVariable implements Variable {

	private static final long serialVersionUID = 6099056787768897902L;

	private static final String DEFAULT_NAME = "_";
	private static final int identifier = -4000;

	private AnonymousVariable() {
		// NO-OP
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof AnonymousVariable)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return identifier;
	}

	@Override
	public String getName() {
		return DEFAULT_NAME;
	}

	@Override
	public Variable clone() {
		return this;
	}

	@Override
	public String toString() {
		return DEFAULT_NAME;
	}

	@Override
	public Set<Variable> getReferencedVariables() {
		return Collections.emptySet();
	}

	@Override
	public List<Variable> getReferencedVariablesList() {
		return Collections.emptyList();
	}
}
