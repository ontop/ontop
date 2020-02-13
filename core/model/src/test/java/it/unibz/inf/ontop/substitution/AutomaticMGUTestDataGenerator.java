package it.unibz.inf.ontop.substitution;

/*
 * #%L
 * ontop-reformulation-core
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.vocabulary.XSD;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static it.unibz.inf.ontop.OntopModelTestingTools.*;


/***
 * This is an auxiliary class for the MGU generation test. This class is in
 * charge of taking a test case string, e.g.,
 *
 * A(x) | A(f(y,z)) = {x/f(y,z)}
 *
 * And producing the objects that are needed to execute the query, i.e., atoms,
 * terms, expected mgu list, etc.
 *
 * @author Mariano Rodriguez Muro
 *
 */
public class AutomaticMGUTestDataGenerator {

	/***
	 * Checks if all substitutions in unifier1 are also in unifier2.
	 *
	 * @param unifier1
	 * @param unifier2
	 * @return
	 */
	public boolean compareUnifiers(List<Map.Entry<Variable, ImmutableTerm>> unifier1, List<Map.Entry<Variable, ImmutableTerm>> unifier2) {
		if (unifier1.size() != unifier2.size())
			return false;

		for (int i = 0; i < unifier1.size(); i++) {
			boolean result = false;
			for (int j = 0; j < unifier2.size(); j++) {
				result = result || compareSubstitutions(unifier1.get(i), unifier2.get(j));
			}
			if (!result) {
				/*
				 * we couldn't find a substitution in unifier2 that matched the
				 * current substitution in unifier1
				 */
				return false;
			}
		}
		/*
		 * We found a match for every substitution in unifier1
		 */
		return true;
	}

	/***
	 * Checks if two substitutions are equal (syntactic)
	 *
	 * @param s1
	 * @param s2
	 * @return
	 */
	public boolean compareSubstitutions(Map.Entry<Variable, ImmutableTerm> s1, Map.Entry<Variable, ImmutableTerm> s2) {
		boolean equalVars = s1.getKey().toString().equals(s2.getKey().toString());
		boolean equalTerms = s1.getValue().toString().equals(s2.getValue().toString());
		return equalVars && equalTerms;
	}

	/***
	 * Gets list of substitutions encoded in the string mgustr. mgustr is
	 * normally used to encode the expected MGU for a test.
	 *
	 * @param mgustr
	 * @return
	 */
	public List<Map.Entry<Variable, ImmutableTerm>> getMGU(String mgustr) {
		if (mgustr.trim().equals("NULL"))
			return null;

		mgustr = mgustr.substring(1, mgustr.length() - 1);
		String[] mguStrings = mgustr.split(" ");

		List<Map.Entry<Variable, ImmutableTerm>> mgu = new ArrayList<>();
		for (int i = 0; i < mguStrings.length; i++) {
			String string = mguStrings[i];
			if (string.equals(""))
				continue;
			String[] elements = string.split("/");
			Map.Entry<Variable, ImmutableTerm> s = Maps.immutableEntry((Variable) getTerm(elements[0]), getTerm(elements[1]));
			mgu.add(s);
		}
		return mgu;
	}


	/***
	 * Gets the list of size 2, of the two atoms in the string atomstr. Only
	 * supports 2 atoms!.
	 *
	 * @param atomstrs
	 * @return
	 */
	public List<ImmutableTerm> getAtoms(String atomstrs) {
		atomstrs = atomstrs.trim();
		List<ImmutableTerm> atoms = new ArrayList<>();
		String[] atomstr = atomstrs.split("\\|");
		String str1 = atomstr[0].trim();
		ImmutableTerm atom1 = getAtom(str1);
		atoms.add(atom1);
		String str2 = atomstr[1].trim();
		ImmutableTerm atom2 = getAtom(str2);
		atoms.add(atom2);
		return atoms;
	}

	public ImmutableTerm getAtom(String atomstr) {
		String termstr = atomstr.substring(2, atomstr.length() - 1);
		List<ImmutableTerm> terms = new ArrayList<>();

		String[] termstra = termstr.split(" ");
		for (int i = 0; i < termstra.length; i++) {
			terms.add(getTerm(termstra[i].trim()));
		}
		ImmutableTerm atom = TERM_FACTORY.getImmutableFunctionalTerm(
				new OntopModelTestFunctionSymbol(atomstr.substring(0, 1), terms.size()),
				ImmutableList.copyOf(terms));
		return atom;
	}

	public ImmutableTerm getTerm(String termstrs) {
		// List<Term> terms = new ArrayList<Term>();
		// String[] termstra = termstrs.split(",");
		// for (int i = 0; i < termstra.length; i++) {

		String termstr = termstrs.trim();

		if (termstr.indexOf('(') != -1) {
			String[] subtermstr = termstr.substring(2, termstrs.length() - 1).split(",");
			List<ImmutableTerm> fuctTerms = new ArrayList<>();
			for (int i = 0; i < subtermstr.length; i++) {
				fuctTerms.add(getTerm(subtermstr[i]));
			}
			FunctionSymbol fs = new OntopModelTestFunctionSymbol(termstr.substring(0, 1), fuctTerms.size());
			return TERM_FACTORY.getImmutableFunctionalTerm(fs, ImmutableList.copyOf(fuctTerms));
		}
		else if (termstr.charAt(0) == '"') {
			return TERM_FACTORY.getRDFLiteralConstant(termstr.substring(1, termstr.length() - 1), XSD.STRING);
		}
		else if (termstr.charAt(0) == '<') {
			return TERM_FACTORY.getConstantIRI(RDF_FACTORY.createIRI(termstr.substring(1, termstr.length() - 1)));
//		} else if (termstr.equals("#")) {
//			return TERM_FACTORY.getVariableNondistinguished();
		}
		else {
			return TERM_FACTORY.getVariable(termstr);
			/* variable */
		}
		// }

	}
}
