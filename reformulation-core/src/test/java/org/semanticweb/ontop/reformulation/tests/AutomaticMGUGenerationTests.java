package org.semanticweb.ontop.reformulation.tests;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Substitution;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Unifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mariano Rodriguez Muro
 * 
 */
public class AutomaticMGUGenerationTests extends TestCase {

	private UnifierUtilities					unifier		= null;
	private AutomaticMGUTestDataGenerator	generator	= null;
	private Logger						log			= LoggerFactory.getLogger(AutomaticMGUGenerationTests.class);

	/**
	 * @throws java.lang.Exception
	 */
	
	public void setUp() throws Exception {
		/*
		 * TODO modify the API so that function symbols for object terms use the
		 * Predicate class instead of FunctionSymbol class
		 */

		unifier = new UnifierUtilities();
		generator = new AutomaticMGUTestDataGenerator();

	}

	/**
	 * Test method for
	 * {@link org.semanticweb.ontop.owlrefplatform.core.basicoperations.Unifier#getMGU(it.unibz.krdb.obda.model.Atom, it.unibz.krdb.obda.model.Atom)}
	 * .
	 * 
	 * @throws Exception
	 */
	
	public void testGetMGUAtomAtomBoolean() throws Exception {
		log.debug("Testing computation of MGUs");
		File inputFile = new File("src/test/java/org/semanticweb/ontop/reformulation/tests/mgu-computation-test-cases.txt");
		BufferedReader in = new BufferedReader(new FileReader(inputFile));

		String testcase = in.readLine();
		int casecounter = 0;
		while (testcase != null) {
			if (testcase.trim().equals("") || testcase.charAt(0) == '%') {
				/* we read a comment, skip it */
				testcase = in.readLine();
				continue;
			}
			log.debug("case: {}", testcase);
			String input = testcase;
			String atomsstr = input.split("=")[0].trim();
			String mgustr = input.split("=")[1].trim();
			List<Function> atoms = generator.getAtoms(atomsstr);
			List<Substitution> expectedmgu = generator.getMGU(mgustr);

			List<Substitution> computedmgu = new LinkedList<Substitution>();
			Exception expectedException = null;

			Unifier mgu = Unifier.getMGU(atoms.get(0), atoms.get(1));
			if (mgu == null) {
				computedmgu = null;
			} else {
				for (VariableImpl var : mgu.keySet()) {
					computedmgu.add(new Substitution(var, mgu.get(var)));
				}
			}

			log.debug("Expected MGU: {}", expectedmgu);

			if (expectedmgu == null) {
				assertTrue(computedmgu == null);
			} else {
				assertTrue(computedmgu != null);
				assertTrue(computedmgu.size() == expectedmgu.size());
				assertTrue(generator.compareUnifiers(expectedmgu, computedmgu));

			}
			casecounter += 1;
			testcase = in.readLine();
		}
		log.info("Suceffully executed {} test cases for MGU computation");
	}

//	/**
//	 * Test method for
//	 * {@link org.obda.reformulation.dllite.AtomUnifier#getMGU(org.obda.query.domain.CQIE, int, int, boolean)}
//	 * .
//	 */
//	
//	public void testGetMGUCQIEIntIntBoolean() {
//		("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for
//	 * {@link org.obda.reformulation.dllite.AtomUnifier#applySubstitution(org.obda.query.domain.CQIE, org.obda.reformulation.dllite.Substitution)}
//	 * .
//	 */
//	
//	public void testApplySubstitution() {
//		fail("Not yet implemented"); // TODO
//	}

}
