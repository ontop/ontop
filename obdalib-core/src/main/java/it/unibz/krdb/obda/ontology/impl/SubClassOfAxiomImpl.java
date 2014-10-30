package it.unibz.krdb.obda.ontology.impl;

import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.SubClassOfAxiom;

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



public class SubClassOfAxiomImpl implements SubClassOfAxiom {

	private static final long serialVersionUID = -7590338987239580423L;

	private final BasicClassDescription including; // right-hand side
	private final BasicClassDescription included;
	private final String string;
	
	SubClassOfAxiomImpl(BasicClassDescription subDesc, BasicClassDescription superDesc) {
		included = subDesc;
		including = superDesc;
		StringBuilder bf = new StringBuilder();
		bf.append(included.toString());
		bf.append(" ISA ");
		bf.append(including.toString());
		string = bf.toString();
	}

	@Override
	public BasicClassDescription getSub() {
		return included;
	}

	@Override
	public BasicClassDescription getSuper() {
		return including;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SubClassOfAxiomImpl) {
			SubClassOfAxiomImpl inc2 = (SubClassOfAxiomImpl) obj;
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
