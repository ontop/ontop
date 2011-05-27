/**
 * Author: Mariano Rodriguez Muro Free University of Bozen-Bolzano
 * rodriguez@inf.unibz.it
 */
package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.AtomUnifier;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.Substitution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mariano Rodriguez Muro
 * 
 */
public class AutomaticMGUGenerationTests extends TestCase {

	private AtomUnifier					unifier		= null;
	private AutomaticMGUTestDataGenerator	generator	= null;
	private Logger						log			= LoggerFactory.getLogger(AutomaticMGUGenerationTests.class);

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		/*
		 * TODO modify the API so that function symbols for object terms use the
		 * Predicate class instead of FunctionSymbol class
		 */

		unifier = new AtomUnifier();
		generator = new AutomaticMGUTestDataGenerator();

	}

	/**
	 * Test method for
	 * {@link it.unibz.krdb.obda.owlrefplatform.core.basicoperations.AtomUnifier#getMGU(it.unibz.krdb.obda.model.Atom, it.unibz.krdb.obda.model.Atom)}
	 * .
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetMGUAtomAtomBoolean() throws Exception {
		log.debug("Testing computation of MGUs");
		File inputFile = new File("src/test/java/org/obda/reformulation/tests/mgu-computation-test-cases.txt");
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
			List<Atom> atoms = generator.getAtoms(atomsstr);
			List<Substitution> expectedmgu = generator.getMGU(mgustr);

			AtomUnifier unifier = new AtomUnifier();
			List<Substitution> computedmgu = new LinkedList<Substitution>();
			Exception expectedException = null;

			Map<Variable, Term> mgu = unifier.getMGU(atoms.get(0), atoms.get(1));
			if (mgu == null) {
				computedmgu = null;
			} else {
				for (Term var : mgu.keySet()) {
					computedmgu.add(new Substitution(var, mgu.get(var)));
				}
			}

			log.debug("Computed MGU: {}", computedmgu);

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
//	@Test
//	public void testGetMGUCQIEIntIntBoolean() {
//		("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for
//	 * {@link org.obda.reformulation.dllite.AtomUnifier#applySubstitution(org.obda.query.domain.CQIE, org.obda.reformulation.dllite.Substitution)}
//	 * .
//	 */
//	@Test
//	public void testApplySubstitution() {
//		fail("Not yet implemented"); // TODO
//	}

}
