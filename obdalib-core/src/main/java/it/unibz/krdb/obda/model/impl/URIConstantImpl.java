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

import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.Variable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Provides a storage to put the URI constant.
 */
public class URIConstantImpl implements URIConstant {

	private static final long serialVersionUID = -1263974895010238519L;
	
	private final int identifier;
	private final String iristr;

	protected URIConstantImpl(String iri) {
		this.iristr = iri;
		this.identifier = iri.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof URIConstantImpl)) {
			return false;
		}
		URIConstantImpl uri2 = (URIConstantImpl) obj;
		return this.identifier == uri2.identifier;
	}

	@Override
	public int hashCode() {
		return identifier;
	}

	@Override
	public String getURI() {
		return iristr;
	}

	@Override
	public URIConstant clone() {
		return this;
	}

	@Override
	public String toString() {
		return "<" + iristr + ">";
	}

	@Override
	public Set<Variable> getReferencedVariables() {
		return Collections.emptySet();
	}

	@Override
	public List<Variable> getReferencedVariablesList() {
		return Collections.emptyList();
	}

	@Override
	public COL_TYPE getType() {
		return COL_TYPE.OBJECT;
	}

	@Deprecated
	public String getValue() {
		return iristr;
	}
}
