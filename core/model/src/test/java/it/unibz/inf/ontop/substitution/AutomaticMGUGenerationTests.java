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
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibz.inf.ontop.OntopModelTestingTools.*;
import static it.unibz.inf.ontop.OntopModelTestingTools.TERM_FACTORY;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Mariano Rodriguez Muro
 * 
 */
public class AutomaticMGUGenerationTests  {

	private static ImmutableUnificationTools unifier;
	private final Logger						log			= LoggerFactory.getLogger(AutomaticMGUGenerationTests.class);

	@BeforeAll
	public static void setUp() {
		/*
		 * TODO modify the API so that function symbols for object terms use the
		 * Predicate class instead of FunctionSymbol class
		 */

		unifier = new ImmutableUnificationTools(SUBSTITUTION_FACTORY);
	}

	@Test
	public void testGetMGUAtomAtomBoolean() throws Exception {
		log.debug("Testing computation of MGUs");
		File inputFile = new File("src/test/java/it/unibz/inf/ontop/substitution/mgu-computation-test-cases.txt");
		try (BufferedReader in = new BufferedReader(new FileReader(inputFile))) {
			String testcase;
			int casecounter = 0;
			while ((testcase = in.readLine()) != null) {
				if (testcase.trim().equals("") || testcase.charAt(0) == '%') {
					continue; // we read a comment, skip it
				}
				log.debug("case: {}", testcase);
				String atomsstr = testcase.split("=")[0].trim();
				String mgustr = testcase.split("=")[1].trim();

				List<ImmutableTerm> atoms = getAtoms(atomsstr);
				Optional<ImmutableSubstitution<ImmutableTerm>> expectedmgu = getMGU(mgustr);
				Optional<ImmutableSubstitution<ImmutableTerm>> mgu = unifier.computeMGU(ImmutableList.of(atoms.get(0)), ImmutableList.of(atoms.get(1)));
				log.debug("Expected MGU: {}", expectedmgu);
				assertEquals(expectedmgu, mgu);
				casecounter++;
			}
			log.info("Successfully executed {} test cases for MGU computation", casecounter);
		}
	}


	/***
	 * Gets list of substitutions encoded in the string mgustr. mgustr is
	 * normally used to encode the expected MGU for a test.
	 *
	 * @param mgustr
	 * @return
	 */
	private static Optional<ImmutableSubstitution<ImmutableTerm>> getMGU(String mgustr) {
		if (mgustr.trim().equals("NULL"))
			return Optional.empty();

		mgustr = mgustr.substring(1, mgustr.length() - 1);
		String[] mguStrings = mgustr.split(" ");

		ImmutableMap.Builder<Variable, ImmutableTerm> builder = ImmutableMap.builder();
		for (String string : mguStrings) {
			if (string.equals(""))
				continue;
			String[] elements = string.split("/");
			builder.put((Variable) getTerm(elements[0]), getTerm(elements[1]));
		}
		ImmutableMap<Variable, ImmutableTerm> map = builder.build();
		return Optional.of(SUBSTITUTION_FACTORY.getSubstitutionFromStream(map.entrySet().stream(), Map.Entry::getKey, Map.Entry::getValue));
	}


	/***
	 * Gets the list of size 2, of the two atoms in the string atomstr. Only
	 * supports 2 atoms!.
	 *
	 * @param atomstrs
	 * @return
	 */
	private static ImmutableList<ImmutableTerm> getAtoms(String atomstrs) {
		String[] atomstr = atomstrs.trim().split("\\|");
		ImmutableTerm atom1 = getAtom(atomstr[0].trim());
		ImmutableTerm atom2 = getAtom(atomstr[1].trim());
		return ImmutableList.of(atom1, atom2);
	}

	private static ImmutableTerm getAtom(String atomstr) {
		String termstr = atomstr.substring(2, atomstr.length() - 1);
		List<ImmutableTerm> terms = new ArrayList<>();

		String[] termstra = termstr.split(" ");
		for (String s : termstra) {
			terms.add(getTerm(s.trim()));
		}
		return TERM_FACTORY.getImmutableFunctionalTerm(
				new OntopModelTestFunctionSymbol(atomstr.substring(0, 1), terms.size()),
				ImmutableList.copyOf(terms));
	}

	private static ImmutableTerm getTerm(String termstrs) {
		// List<Term> terms = new ArrayList<Term>();
		// String[] termstra = termstrs.split(",");
		// for (int i = 0; i < termstra.length; i++) {

		String termstr = termstrs.trim();

		if (termstr.indexOf('(') != -1) {
			String[] subtermstr = termstr.substring(2, termstrs.length() - 1).split(",");
			List<ImmutableTerm> fuctTerms = new ArrayList<>();
			for (String s : subtermstr) {
				fuctTerms.add(getTerm(s));
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
