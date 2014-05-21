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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.semanticweb.ontop.model.Variable;

public class VariableImpl extends AbstractLiteral implements Variable {

	private static final long serialVersionUID = 5723075311798541659L;

	private final String name;

	private final int identifier;

	protected VariableImpl(String name) {
		if (name == null) {
			throw new RuntimeException("Variable name cannot be null");
		}
		this.name = name;
		this.identifier = name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof VariableImpl)) {
			return false;
		}
		VariableImpl name2 = (VariableImpl) obj;
		return this.identifier == name2.identifier;
	}

	@Override
	public int hashCode() {
		return identifier;
	}

	@Override
	public String getName() {
		return name;
	}

	// TODO this method seems to be tied to some semantics, if we modified it,
	// things become slow and maybe wrong we must make sure that this is not the
	// case
	@Override
	public String toString() {
		return TermUtil.toString(this);
	}

	@Override
	public Variable clone() {
		return this;
	}

	@Override
	public Set<Variable> getReferencedVariables() {
		return Collections.singleton((Variable)this);
	}
	
	@Override
	public Map<Variable, Integer> getVariableCount() {
		Map<Variable,Integer> count =  new HashMap<Variable,Integer>();
		count.put(this, 1);
		return count;
	}
	

}
