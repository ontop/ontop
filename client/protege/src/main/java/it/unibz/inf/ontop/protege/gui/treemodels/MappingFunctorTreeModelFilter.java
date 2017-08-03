package it.unibz.inf.ontop.protege.gui.treemodels;

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

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.model.term.impl.FunctionalTermImpl;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.List;

/**
 * This filter receives a string and returns true if any mapping contains the
 * functor in some of the atoms in the head
 */
public class MappingFunctorTreeModelFilter extends TreeModelFilter<SQLPPTriplesMap> {

	public MappingFunctorTreeModelFilter() {
		super.bNegation = false;
	}

	@Override
	public boolean match(SQLPPTriplesMap object) {
		ImmutableList<ImmutableFunctionalTerm> atoms = object.getTargetAtoms();

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
			if (term instanceof Variable) {
				Variable variableTerm = (Variable) term;
				if (variableTerm.getName().indexOf(keyword) != -1) { // match found!
					return true;
				}
			}
		}
		return false;
	}
}
