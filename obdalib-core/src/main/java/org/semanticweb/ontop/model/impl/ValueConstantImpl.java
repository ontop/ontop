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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.ValueConstant;
import org.semanticweb.ontop.model.Variable;

public class ValueConstantImpl extends AbstractLiteral implements ValueConstant {

	private static final long serialVersionUID = 8031338451909170400L;

	private final String value;

	private final String language;

	private final Predicate.COL_TYPE type;
	
	private int hashcode = -1;

	/**
	 * The default constructor.
	 * 
	 * @param value
	 *            the constant value.
	 * @param type
	 *            the constant type.
	 */
	protected ValueConstantImpl(String value, Predicate.COL_TYPE type) {
		this(value, null, type);
	}

	protected ValueConstantImpl(String value, String language, Predicate.COL_TYPE type) {
		this.value = value;
		this.language = language;
		this.type = type;
		this.hashcode = toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ValueConstantImpl)) {
			return false;
		}
		ValueConstantImpl value2 = (ValueConstantImpl) obj;
		return this.hashCode() == value2.hashCode();
	}

	@Override
	public int hashCode() {		
		return hashcode;
	}

	@Override
	public ValueConstant clone() {
		return this;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String getLanguage() {
		return language;
	}

	@Override
	public Predicate.COL_TYPE getType() {
		return type;
	}

	@Override
	public String toString() {
		return TermUtil.toString(this);
	}

	@Override
	public Set<Variable> getReferencedVariables() {
		return new LinkedHashSet<Variable>();
	}
/*
	@Override
	public Map<Variable, Integer> getVariableCount() {
		return new HashMap<Variable, Integer>();
	}
*/
}
