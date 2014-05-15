package org.semanticweb.ontop.protege4.gui.treemodels;

/*
 * #%L
 * ontop-protege4
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen Bolzano.
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

import java.util.List;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.impl.CQIEImpl;
import org.semanticweb.ontop.model.impl.FunctionalTermImpl;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.protege4.gui.treemodels.TreeModelFilter;

/**
 * This filter receives a string and returns true if any mapping contains the
 * functor in some of the atoms in the head
 */
public class MappingFunctorTreeModelFilter extends TreeModelFilter<OBDAMappingAxiom> {

	public MappingFunctorTreeModelFilter() {
		super.bNegation = false;
	}

	@Override
	public boolean match(OBDAMappingAxiom object) {
		final CQIE headquery = (CQIEImpl) object.getTargetQuery();
		final List<Function> atoms = headquery.getBody();

		boolean isMatch = false;
		for (String keyword : vecKeyword) {
			for (int i = 0; i < atoms.size(); i++) {
				Function predicate = (Function) atoms.get(i);
				isMatch = isMatch || match(keyword.trim(), predicate);
			}
			if (isMatch) {
				break; // end loop if a match is found!
			}
		}
		return (bNegation ? !isMatch : isMatch);
	}

	/** A helper method to check a match */
	public static boolean match(String keyword, Function predicate) {
		List<Term> queryTerms = predicate.getTerms();
		for (int j = 0; j < queryTerms.size(); j++) {
			Term term = queryTerms.get(j);
			if (term instanceof FunctionalTermImpl) {
				FunctionalTermImpl functionTerm = (FunctionalTermImpl) term;
				if (functionTerm.getFunctionSymbol().toString().indexOf(keyword) != -1) { // match found!
					return true;
				}
			}
			if (term instanceof VariableImpl) {
				VariableImpl variableTerm = (VariableImpl) term;
				if (variableTerm.getName().indexOf(keyword) != -1) { // match found!
					return true;
				}
			}
		}
		return false;
	}
}
