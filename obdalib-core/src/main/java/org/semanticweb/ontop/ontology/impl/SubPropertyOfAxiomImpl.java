package org.semanticweb.ontop.ontology.impl;

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

import org.semanticweb.ontop.ontology.PropertyExpression;
import org.semanticweb.ontop.ontology.SubPropertyOfAxiom;

public class SubPropertyOfAxiomImpl implements SubPropertyOfAxiom {

	private static final long serialVersionUID = -3020225654321319941L;

	private final PropertyExpression including; // right-hand side
	private final PropertyExpression included;	
	private final String string;
	
	SubPropertyOfAxiomImpl(PropertyExpression subDesc, PropertyExpression superDesc) {
		included = subDesc;
		including = superDesc;
		StringBuilder bf = new StringBuilder();
		bf.append(included.toString());
		bf.append(" ISA ");
		bf.append(including.toString());
		string = bf.toString();
	}

	@Override
	public PropertyExpression getSub() {
		return included;
	}

	@Override
	public PropertyExpression getSuper() {
		return including;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SubPropertyOfAxiomImpl) {
			SubPropertyOfAxiomImpl inc2 = (SubPropertyOfAxiomImpl) obj;
			return including.equals(inc2.including) && included.equals(inc2.included);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return string.hashCode();
	}

	@Override
	public String toString() {
		return string;
	}
}
