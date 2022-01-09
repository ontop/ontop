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
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibz.inf.ontop.OntopModelTestingTools.SUBSTITUTION_FACTORY;
import static it.unibz.inf.ontop.OntopModelTestingTools.TERM_FACTORY;

/**
 * @author Mariano Rodriguez Muro
 * 
 */
public class AutomaticMGUGenerationTests extends TestCase {

	private ImmutableUnificationTools unifier;
	private AutomaticMGUTestDataGenerator	generator;
	private Logger						log			= LoggerFactory.getLogger(AutomaticMGUGenerationTests.class);

	public void setUp() throws Exception {
		/*
		 * TODO modify the API so that function symbols for object terms use the
		 * Predicate class instead of FunctionSymbol class
		 */

		unifier = new ImmutableUnificationTools(TERM_FACTORY, SUBSTITUTION_FACTORY);
		generator = new AutomaticMGUTestDataGenerator();
	}

	public void testGetMGUAtomAtomBoolean() throws Exception {
		log.debug("Testing computation of MGUs");
		File inputFile = new File("src/test/java/it/unibz/inf/ontop/substitution/mgu-computation-test-cases.txt");
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
			List<ImmutableTerm> atoms = generator.getAtoms(atomsstr);
			List<Map.Entry<Variable, ImmutableTerm>> expectedmgu = generator.getMGU(mgustr);
			List<Map.Entry<Variable, ImmutableTerm>> computedmgu = new ArrayList<>();

			Optional<ImmutableSubstitution<ImmutableTerm>> mgu = unifier.computeMGU(ImmutableList.of(atoms.get(0)), ImmutableList.of(atoms.get(1)));
			if (!mgu.isPresent()) {
				computedmgu = null;
			} else {
				computedmgu.addAll(mgu.get().getImmutableMap().entrySet());
			}

			log.debug("Expected MGU: {}", expectedmgu);

			if (expectedmgu == null) {
				assertNull(computedmgu);
			} else {
				assertNotNull(computedmgu);
				assertEquals(computedmgu.size(), expectedmgu.size());
				assertTrue(generator.compareUnifiers(expectedmgu, computedmgu));

			}
			casecounter += 1;
			testcase = in.readLine();
		}
		in.close();
		log.info("Successfully executed {} test cases for MGU computation", casecounter);
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
//	 */
//	
//	public void testApplySubstitution() {
//		fail("Not yet implemented"); // TODO
//	}

}
