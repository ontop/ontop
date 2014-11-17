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

import org.semanticweb.ontop.model.URIConstant;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.Predicate.COL_TYPE;

/**
 * Provides a storage to put the URI constant.
 */
public class URIConstantImpl extends AbstractLiteral implements URIConstant {

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
		return this.iristr;
	}

	@Override
	public URIConstant clone() {
		return this;
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

	@Override
	public COL_TYPE getType() {
		return COL_TYPE.OBJECT;
	}

	@Override
	public String getValue() {
		return iristr;
	}

	@Override
	public String getLanguage() {
		return null;
	}
}
