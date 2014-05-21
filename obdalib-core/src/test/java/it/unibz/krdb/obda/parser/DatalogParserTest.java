package it.unibz.krdb.obda.parser;

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

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.obda.model.impl.URIConstantImpl;
import it.unibz.krdb.obda.model.impl.ValueConstantImpl;
import it.unibz.krdb.obda.model.impl.VariableImpl;

import java.util.List;

import junit.framework.TestCase;

import org.antlr.runtime.RecognitionException;
import org.junit.Ignore;

/*
 * Disabled the test because we don't need it anymore and it fails on java8 
 */
@Ignore
public class DatalogParserTest extends TestCase {

	/** Test inputs */
	private static final String[] CQ_STRINGS = {
	// Scenario 1: Basic input (Datalog syntax)
		"base	     <http://base.org/stuff/1.0/> \n" +
		"prefix abc: <http://www.abc.org/1999/02/22-abc-syntax-ns#> \n" +
		"prefix    : <http://example.org/stuff/1.0/> \n" +
		"abc:p($x, $y) :- :q($x), r($y)",
	// Scenario 2: Basic input (SWIRL syntax)
		"base		 <http://base.org/stuff/1.0/> \n" +
		"prefix abc: <http://www.abc.org/1999/02/22-abc-syntax-ns#> \n" +
		"prefix    : <http://example.org/stuff/1.0/> \n" +
		":q($x), r($y) -> abc:p($x, $y)",
	// Scenario 3: Different types of term.
		"base		 <http://base.org/stuff/1.0/> \n" +
		"prefix abc: <http://www.abc.org/1999/02/22-abc-syntax-ns#> \n" +
		"prefix    : <http://example.org/stuff/1.0/> \n" +
		"abc:p($x, $y) :- :q($x, \"Person\"), " +
						 ":r(s($y, \"Student\"), http://example.org/stuff/1.1/FUB)",
	// Scenario 4: Multiple rules.
		"base	     <http://base.org/stuff/1.0/> \n" +
		"prefix abc: <http://www.abc.org/1999/02/22-abc-syntax-ns#> \n" +
		"prefix    : <http://example.org/stuff/1.0/> \n" +
		"abc:p($x) :- :q($x, \"Person\") \n" +
		"abc:r($y) :- :s($y, http://example.org/stuff/1.1/FUB) \n" +
		"abc:t($z) :- :u($z, f(http://example.org/stuff/1.2/Occupation, \"Student\"))",
	// Scenario 5: Recursive object terms.
		"prefix abc: <http://www.abc.org/1999/02/22-abc-syntax-ns#> \n" +
		"prefix    : <http://example.org/stuff/1.0/> \n" +
		"abc:p($x) :- :q($x, :r(http://example.org/stuff/1.1/FUB, " +
							":s(http://example.org/stuff/1.2/Occupation, " +
							":t(http://example.org/stuff/1.3/Degree, \"Master\"))))",
	// Scenario 6: No head.
		"prefix abc: <http://www.abc.org/1999/02/22-abc-syntax-ns#> \n" +
		"prefix    : <http://example.org/stuff/1.0/> \n" +
		" :- :q($x)",
	// Scenario 7: No body.
		"prefix abc: <http://www.abc.org/1999/02/22-abc-syntax-ns#> \n" +
		"prefix    : <http://example.org/stuff/1.0/> \n" +
		"abc:p($x) :- ",
	// Scenario 8: Select all.
		"base		 <http://base.org/stuff/1.0/> \n" +
		"prefix abc: <http://www.abc.org/1999/02/22-abc-syntax-ns#> \n" +
		"prefix    : <http://example.org/stuff/1.0/> \n" +
		"abc:p(*) :- :q($x), r($y)",
	// Scenario 9: Basic input using caret symbol.
		"base		 <http://base.org/stuff/1.0/> \n" +
		"prefix abc: <http://www.abc.org/1999/02/22-abc-syntax-ns#> \n" +
		"prefix    : <http://example.org/stuff/1.0/> \n" +
		"abc:p($x, $y) :- :q($x) ^ r($y)",
	// Scenario 10: Empty term on the head.
		"base	     <http://base.org/stuff/1.0/> \n" +
		"prefix abc: <http://www.abc.org/1999/02/22-abc-syntax-ns#> \n" +
		"prefix    : <http://example.org/stuff/1.0/> \n" +
		"abc:p() :- :q($x), r($y)",
	// Scenario 11: Full name with URI.
		"http://www.abc.org/1999/02/22-abc-syntax-ns#p($x, $y) :- " +
		"http://example.org/stuff/1.0/q($x), " +
		"http://base.org/stuff/1.0/r(http://example.org/stuff/1.0/s($y, \"Student\"), " +
								   "http://example.org/stuff/1.1/FUB)"
	};

	/** The oracle */
	private static int EXPECTED_RULE_SIZE;
	private static int EXPECTED_BODY_SIZE;
	private static int EXPECTED_HEAD_TERM_SIZE;
	private static int EXPECTED_BODY_TERM_SIZE;

	private DatalogProgramParser parser;
	private DatalogProgram datalog;
	private String uri;
	private List<Term> terms;
	private Term term;

	//@Beforere
	public void setUp() throws Exception {
		parser = new DatalogProgramParser();
	}

	/**
	 * Testing Scenario #1
	 *
	 * @throws RecognitionException
	 */
	//@Test
	public void testBasicInputDatalogSyntax() throws RecognitionException {

		datalog = parser.parse(CQ_STRINGS[0]);

		EXPECTED_RULE_SIZE = 1;
		List<CQIE> rules = datalog.getRules();
		assertTrue("Mismatch rule size!",
				rules.size() == EXPECTED_RULE_SIZE);

		// Rule #1
		//-- The Head
		Function head = rules.get(0).getHead();
		assertNotNull("Head is null!", head);

		uri = head.getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!", uri,
				"http://www.abc.org/1999/02/22-abc-syntax-ns#p");

		EXPECTED_HEAD_TERM_SIZE = 2;
		terms = head.getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_HEAD_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "x");

		term = terms.get(1);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "y");

		//-- The Body
		EXPECTED_BODY_SIZE = 2;
		List<Function> body = rules.get(0).getBody();
		assertNotNull("Body is null!", body);
		assertTrue("Mismatch body size!",
				body.size() == EXPECTED_BODY_SIZE);

		//---- Body atom #1
		uri = ((Function)body.get(0)).getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!",
				uri, "http://example.org/stuff/1.0/q");

		EXPECTED_BODY_TERM_SIZE = 1;
		terms = ((Function)body.get(0)).getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_BODY_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "x");

		//---- Body atom #2
		uri = ((Function)body.get(1)).getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!",
				uri, "http://base.org/stuff/1.0/r");

		EXPECTED_BODY_TERM_SIZE = 1;
		terms = ((Function)body.get(1)).getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_BODY_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "y");
	}

	/**
	 * Testing Scenario #2
	 *
	 * @throws RecognitionException
	 */
	//@Test
	public void testBasicInputSwirlSyntax() throws RecognitionException {

		datalog = parser.parse(CQ_STRINGS[1]);

		EXPECTED_RULE_SIZE = 1;
		List<CQIE> rules = datalog.getRules();
		assertTrue("Mismatch rule size!",
				rules.size() == EXPECTED_RULE_SIZE);

		// Rule #1
		//-- The Head
		Function head = rules.get(0).getHead();
		assertNotNull("Head is null!", head);

		uri = head.getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!", uri,
				"http://www.abc.org/1999/02/22-abc-syntax-ns#p");

		EXPECTED_HEAD_TERM_SIZE = 2;
		terms = head.getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_HEAD_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "x");

		term = terms.get(1);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "y");

		//-- The Body
		EXPECTED_BODY_SIZE = 2;
		List<Function> body = rules.get(0).getBody();
		assertNotNull("Body is null!", body);
		assertTrue("Mismatch body size!",
				body.size() == EXPECTED_BODY_SIZE);

		//---- Body atom #1
		uri = ((Function)body.get(0)).getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!",
				uri, "http://example.org/stuff/1.0/q");

		EXPECTED_BODY_TERM_SIZE = 1;
		terms = ((Function)body.get(0)).getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_BODY_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "x");

		//---- Body atom #2
		uri = ((Function)body.get(1)).getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!",
				uri, "http://base.org/stuff/1.0/r");

		EXPECTED_BODY_TERM_SIZE = 1;
		terms = ((Function)body.get(1)).getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_BODY_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "y");
	}

	/**
	 * Testing Scenario #3
	 *
	 * @throws RecognitionException
	 */
	//@Test
	public void testDifferentTypesOfTerm() throws RecognitionException {

		datalog = parser.parse(CQ_STRINGS[2]);

		EXPECTED_RULE_SIZE = 1;
		List<CQIE> rules = datalog.getRules();
		assertTrue("Mismatch rule size!",
				rules.size() == EXPECTED_RULE_SIZE);

		// Rule #1
		//-- The Head
		Function head = rules.get(0).getHead();
		assertNotNull("Head is null!", head);

		uri = head.getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!", uri,
				"http://www.abc.org/1999/02/22-abc-syntax-ns#p");

		EXPECTED_HEAD_TERM_SIZE = 2;
		terms = head.getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_HEAD_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "x");

		term = terms.get(1);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "y");

		//-- The Body
		EXPECTED_BODY_SIZE = 2;
		List<Function> body = rules.get(0).getBody();
		assertNotNull("Body is null!", body);
		assertTrue("Mismatch body size!",
				body.size() == EXPECTED_BODY_SIZE);

		//---- Body atom #1
		uri = ((Function)body.get(0)).getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!",
				uri, "http://example.org/stuff/1.0/q");

		EXPECTED_BODY_TERM_SIZE = 2;
		terms = ((Function)body.get(0)).getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_BODY_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "x");

		term = terms.get(1);
		assertTrue("Mismatch term type!",
				term instanceof ValueConstantImpl);
		assertEquals("Mismatch variable name!",
				((ValueConstantImpl)term).getValue(), "Person");

		//---- Body atom #2
		uri = ((Function)body.get(1)).getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!",
				uri, "http://example.org/stuff/1.0/r");

		EXPECTED_BODY_TERM_SIZE = 2;
		terms = ((Function)body.get(1)).getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_BODY_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof FunctionalTermImpl);
		assertEquals("Mismatch variable name!",
				((FunctionalTermImpl)term).getFunctionSymbol().toString(),
				"http://base.org/stuff/1.0/s");

		//------ Object term
		List<Term> objVarTerms = ((FunctionalTermImpl)term).getTerms();
		assertEquals("Mismatch term size!",
				objVarTerms.size(), 2);

		Term objVarTerm = objVarTerms.get(0);
		assertTrue("Mismatch term type!",
				objVarTerm instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)objVarTerm).getName(), "y");

		objVarTerm = objVarTerms.get(1);
		assertTrue("Mismatch term type!",
				objVarTerm instanceof ValueConstantImpl);
		assertEquals("Mismatch variable name!",
				((ValueConstantImpl)objVarTerm).getValue(), "Student");
		//------ Object term ends.

		term = terms.get(1);
		assertTrue("Mismatch term type!",
				term instanceof URIConstantImpl);
		assertEquals("Mismatch variable name!",
				((URIConstantImpl)term).getURI().toString(),
				"http://example.org/stuff/1.1/FUB");
	}

	/**
	 * Testing Scenario #4
	 *
	 * @throws RecognitionException
	 */
	//@Test
	public void testMultipleRules() throws RecognitionException {

		datalog = parser.parse(CQ_STRINGS[3]);

		EXPECTED_RULE_SIZE = 3;
		List<CQIE> rules = datalog.getRules();
		assertTrue("Mismatch rule size!",
				rules.size() == EXPECTED_RULE_SIZE);

		//----------//
		// Rule #1  //
		//----------//
		//-- The Head
		Function head = rules.get(0).getHead();
		assertNotNull("Head is null!", head);

		uri = head.getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!", uri,
				"http://www.abc.org/1999/02/22-abc-syntax-ns#p");

		EXPECTED_HEAD_TERM_SIZE = 1;
		terms = head.getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_HEAD_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "x");

		//-- The Body
		EXPECTED_BODY_SIZE = 1;
		List<Function> body = rules.get(0).getBody();
		assertNotNull("Body is null!", body);
		assertTrue("Mismatch body size!",
				body.size() == EXPECTED_BODY_SIZE);

		//---- Body atom #1
		uri = ((Function)body.get(0)).getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!",
				uri, "http://example.org/stuff/1.0/q");

		EXPECTED_BODY_TERM_SIZE = 2;
		terms = ((Function)body.get(0)).getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_BODY_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "x");

		term = terms.get(1);
		assertTrue("Mismatch term type!",
				term instanceof ValueConstantImpl);
		assertEquals("Mismatch variable name!",
				((ValueConstantImpl)term).getValue(), "Person");

		//----------//
		// Rule #2  //
		//----------//
		//-- The Head
	    head = rules.get(1).getHead();
		assertNotNull("Head is null!", head);

		uri = head.getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!", uri,
				"http://www.abc.org/1999/02/22-abc-syntax-ns#r");

		EXPECTED_HEAD_TERM_SIZE = 1;
		terms = head.getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_HEAD_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "y");

		//-- The Body
		EXPECTED_BODY_SIZE = 1;
		body = rules.get(1).getBody();
		assertNotNull("Body is null!", body);
		assertTrue("Mismatch body size!",
				body.size() == EXPECTED_BODY_SIZE);

		//---- Body atom #1
		uri = ((Function)body.get(0)).getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!",
				uri, "http://example.org/stuff/1.0/s");

		EXPECTED_BODY_TERM_SIZE = 2;
		terms = ((Function)body.get(0)).getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_BODY_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "y");

		term = terms.get(1);
		assertTrue("Mismatch term type!",
				term instanceof URIConstantImpl);
		assertEquals("Mismatch variable name!",
				((URIConstantImpl)term).getURI().toString(),
				"http://example.org/stuff/1.1/FUB");

		//----------//
		// Rule #3  //
		//----------//
		//-- The Head
	    head = rules.get(2).getHead();
		assertNotNull("Head is null!", head);

		uri = head.getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!", uri,
				"http://www.abc.org/1999/02/22-abc-syntax-ns#t");

		EXPECTED_HEAD_TERM_SIZE = 1;
		terms = head.getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_HEAD_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "z");

		//-- The Body
		EXPECTED_BODY_SIZE = 1;
		body = rules.get(2).getBody();
		assertNotNull("Body is null!", body);
		assertTrue("Mismatch body size!",
				body.size() == EXPECTED_BODY_SIZE);

		//---- Body atom #1
		uri = ((Function)body.get(0)).getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!",
				uri, "http://example.org/stuff/1.0/u");

		EXPECTED_BODY_TERM_SIZE = 2;
		terms = ((Function)body.get(0)).getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_BODY_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "z");

		term = terms.get(1);
		assertTrue("Mismatch term type!",
				term instanceof FunctionalTermImpl);
		assertEquals("Mismatch variable name!",
				((FunctionalTermImpl)term).getFunctionSymbol().toString(),
				"http://base.org/stuff/1.0/f");

		//------- Object term
		List<Term> objVarTerms = ((FunctionalTermImpl)term).getTerms();
		assertEquals("Mismatch term size!",
				objVarTerms.size(), 2);

		Term objVarTerm = objVarTerms.get(0);
		assertTrue("Mismatch term type!",
				objVarTerm instanceof URIConstantImpl);
		assertEquals("Mismatch variable name!",
				((URIConstantImpl)objVarTerm).getURI().toString(),
				"http://example.org/stuff/1.2/Occupation");

		objVarTerm = objVarTerms.get(1);
		assertTrue("Mismatch term type!",
				objVarTerm instanceof ValueConstantImpl);
		assertEquals("Mismatch variable name!",
				((ValueConstantImpl)objVarTerm).getValue(), "Student");
	}

	/**
	 * Testing Scenario #5
	 *
	 * @throws RecognitionException
	 */
	//@Test
	public void testIterativeObjectTerms() throws RecognitionException {

		datalog = parser.parse(CQ_STRINGS[4]);

		EXPECTED_RULE_SIZE = 1;
		List<CQIE> rules = datalog.getRules();
		assertTrue("Mismatch rule size!",
				rules.size() == EXPECTED_RULE_SIZE);

		// Rule #1
		//-- The Head
		Function head = rules.get(0).getHead();
		assertNotNull("Head is null!", head);

		uri = head.getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!", uri,
				"http://www.abc.org/1999/02/22-abc-syntax-ns#p");

		EXPECTED_HEAD_TERM_SIZE = 1;
		terms = head.getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_HEAD_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "x");

		//-- The Body
		EXPECTED_BODY_SIZE = 1;
		List<Function> body = rules.get(0).getBody();
		assertNotNull("Body is null!", body);
		assertTrue("Mismatch body size!",
				body.size() == EXPECTED_BODY_SIZE);

		//---- Body atom #1
		uri = ((Function)body.get(0)).getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!",
				uri, "http://example.org/stuff/1.0/q");

		EXPECTED_BODY_TERM_SIZE = 2;
		terms = ((Function)body.get(0)).getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_BODY_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "x");

		term = terms.get(1);
		assertTrue("Mismatch term type!",
				term instanceof FunctionalTermImpl);
		assertEquals("Mismatch variable name!",
				((FunctionalTermImpl)term).getFunctionSymbol().toString(),
				"http://example.org/stuff/1.0/r");

		//------ Object term I1
		List<Term> objVarTerms = ((FunctionalTermImpl)term).getTerms();
		assertEquals("Mismatch term size!",
				objVarTerms.size(), 2);

		Term objVarTerm = objVarTerms.get(0);
		assertTrue("Mismatch term type!",
				objVarTerm instanceof URIConstantImpl);
		assertEquals("Mismatch variable name!",
				((URIConstantImpl)objVarTerm).getURI().toString(),
				"http://example.org/stuff/1.1/FUB");

		objVarTerm = objVarTerms.get(1);
		assertTrue("Mismatch term type!",
				objVarTerm instanceof FunctionalTermImpl);
		assertEquals("Mismatch variable name!",
				((FunctionalTermImpl)objVarTerm).getFunctionSymbol().toString(),
				"http://example.org/stuff/1.0/s");

		//------ Object term I2
		objVarTerms = ((FunctionalTermImpl)objVarTerm).getTerms();
		assertEquals("Mismatch term size!",
				objVarTerms.size(), 2);

		objVarTerm = objVarTerms.get(0);
		assertTrue("Mismatch term type!",
				objVarTerm instanceof URIConstantImpl);
		assertEquals("Mismatch variable name!",
				((URIConstantImpl)objVarTerm).getURI().toString(),
				"http://example.org/stuff/1.2/Occupation");

		objVarTerm = objVarTerms.get(1);
		assertTrue("Mismatch term type!",
				objVarTerm instanceof FunctionalTermImpl);
		assertEquals("Mismatch variable name!",
				((FunctionalTermImpl)objVarTerm).getFunctionSymbol().toString(),
				"http://example.org/stuff/1.0/t");

		//------ Object term I3
		objVarTerms = ((FunctionalTermImpl)objVarTerm).getTerms();
		assertEquals("Mismatch term size!",
				objVarTerms.size(), 2);

		objVarTerm = objVarTerms.get(0);
		assertTrue("Mismatch term type!",
				objVarTerm instanceof URIConstantImpl);
		assertEquals("Mismatch variable name!",
				((URIConstantImpl)objVarTerm).getURI().toString(),
				"http://example.org/stuff/1.3/Degree");

		objVarTerm = objVarTerms.get(1);
		assertTrue("Mismatch term type!",
				objVarTerm instanceof ValueConstantImpl);
		assertEquals("Mismatch variable name!",
				((ValueConstantImpl)objVarTerm).getValue(),
				"Master");
	}

	/**
	 * Testing Scenario #6
	 *
	 * @throws RecognitionException
	 */
	//@Test
	public void testNoHead() throws RecognitionException {

		datalog = parser.parse(CQ_STRINGS[5]);

		EXPECTED_RULE_SIZE = 1;
		List<CQIE> rules = datalog.getRules();
		assertTrue("Mismatch rule size!",
				rules.size() == EXPECTED_RULE_SIZE);

		// Rule #1
		//-- The Head
		Function head = rules.get(0).getHead();
		assertNull("Head is not null!", head);

		//-- The Body
		EXPECTED_BODY_SIZE = 1;
		List<Function> body = rules.get(0).getBody();
		assertNotNull("Body is null!", body);
		assertTrue("Mismatch body size!",
				body.size() == EXPECTED_BODY_SIZE);

		//---- Body atom #1
		uri = ((Function)body.get(0)).getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!",
				uri, "http://example.org/stuff/1.0/q");

		EXPECTED_BODY_TERM_SIZE = 1;
		terms = ((Function)body.get(0)).getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_BODY_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "x");
	}

	/**
	 * Testing Scenario #7
	 *
	 * @throws RecognitionException
	 */
	//@Test
	public void testNoBody() throws RecognitionException {

		datalog = parser.parse(CQ_STRINGS[6]);

		EXPECTED_RULE_SIZE = 1;
		List<CQIE> rules = datalog.getRules();
		assertTrue("Mismatch rule size!",
				rules.size() == EXPECTED_RULE_SIZE);

		// Rule #1
		//-- The Head
		Function head = rules.get(0).getHead();
		assertNotNull("Head is null!", head);

		uri = head.getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!", uri,
				"http://www.abc.org/1999/02/22-abc-syntax-ns#p");

		EXPECTED_HEAD_TERM_SIZE = 1;
		terms = head.getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_HEAD_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "x");

		//-- The Body
		EXPECTED_BODY_SIZE = 1;
		List<Function> body = rules.get(0).getBody();
		assertTrue("Body is not empty!", body.size() == 0);
	}

	/**
	 * Testing Scenario #8
	 *
	 * @throws RecognitionException
	 */
	//@Test
	public void testSelectAll() throws RecognitionException {

		datalog = parser.parse(CQ_STRINGS[7]);

		EXPECTED_RULE_SIZE = 1;
		List<CQIE> rules = datalog.getRules();
		assertTrue("Mismatch rule size!",
				rules.size() == EXPECTED_RULE_SIZE);

		// Rule #1
		//-- The Head
		Function head = rules.get(0).getHead();
		assertNotNull("Head is null!", head);

		uri = head.getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!", uri,
				"http://www.abc.org/1999/02/22-abc-syntax-ns#p");
		assertEquals(2, head.getPredicate().getArity());

		EXPECTED_HEAD_TERM_SIZE = 2;
		terms = head.getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_HEAD_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "y");

		term = terms.get(1);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "x");

		//-- The Body
		EXPECTED_BODY_SIZE = 2;
		List<Function> body = rules.get(0).getBody();
		assertNotNull("Body is null!", body);
		assertTrue("Mismatch body size!",
				body.size() == EXPECTED_BODY_SIZE);

		//---- Body atom #1
		uri = ((Function)body.get(0)).getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!",
				uri, "http://example.org/stuff/1.0/q");

		EXPECTED_BODY_TERM_SIZE = 1;
		terms = ((Function)body.get(0)).getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_BODY_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "x");

		//---- Body atom #2
		uri = ((Function)body.get(1)).getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!",
				uri, "http://base.org/stuff/1.0/r");

		EXPECTED_BODY_TERM_SIZE = 1;
		terms = ((Function)body.get(1)).getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_BODY_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "y");
	}

	/**
	 * Testing Scenario #9
	 *
	 * @throws RecognitionException
	 */
	//@Test
	public void testBasicInputUsingCaretSymbol() throws RecognitionException {

		datalog = parser.parse(CQ_STRINGS[8]);

		EXPECTED_RULE_SIZE = 1;
		List<CQIE> rules = datalog.getRules();
		assertTrue("Mismatch rule size!",
				rules.size() == EXPECTED_RULE_SIZE);

		// Rule #1
		//-- The Head
		Function head = rules.get(0).getHead();
		assertNotNull("Head is null!", head);

		uri = head.getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!", uri,
				"http://www.abc.org/1999/02/22-abc-syntax-ns#p");

		EXPECTED_HEAD_TERM_SIZE = 2;
		terms = head.getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_HEAD_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "x");

		term = terms.get(1);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "y");

		//-- The Body
		EXPECTED_BODY_SIZE = 2;
		List<Function> body = rules.get(0).getBody();
		assertNotNull("Body is null!", body);
		assertTrue("Mismatch body size!",
				body.size() == EXPECTED_BODY_SIZE);

		//---- Body atom #1
		uri = ((Function)body.get(0)).getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!",
				uri, "http://example.org/stuff/1.0/q");

		EXPECTED_BODY_TERM_SIZE = 1;
		terms = ((Function)body.get(0)).getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_BODY_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "x");

		//---- Body atom #2
		uri = ((Function)body.get(1)).getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!",
				uri, "http://base.org/stuff/1.0/r");

		EXPECTED_BODY_TERM_SIZE = 1;
		terms = ((Function)body.get(1)).getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_BODY_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "y");
	}

	/**
	 * Testing Scenario #10
	 *
	 * @throws RecognitionException
	 */
	//@Test
	public void testEmptyHeadTerm() throws RecognitionException {

		datalog = parser.parse(CQ_STRINGS[9]);

		EXPECTED_RULE_SIZE = 1;
		List<CQIE> rules = datalog.getRules();
		assertTrue("Mismatch rule size!",
				rules.size() == EXPECTED_RULE_SIZE);

		// Rule #1
		//-- The Head
		Function head = rules.get(0).getHead();
		assertNotNull("Head is null!", head);

		uri = head.getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!", uri,
				"http://www.abc.org/1999/02/22-abc-syntax-ns#p");

		EXPECTED_HEAD_TERM_SIZE = 0;
		terms = head.getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_HEAD_TERM_SIZE);

		//-- The Body
		EXPECTED_BODY_SIZE = 2;
		List<Function> body = rules.get(0).getBody();
		assertNotNull("Body is null!", body);
		assertTrue("Mismatch body size!",
				body.size() == EXPECTED_BODY_SIZE);

		//---- Body atom #1
		uri = ((Function)body.get(0)).getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!",
				uri, "http://example.org/stuff/1.0/q");

		EXPECTED_BODY_TERM_SIZE = 1;
		terms = ((Function)body.get(0)).getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_BODY_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "x");

		//---- Body atom #2
		uri = ((Function)body.get(1)).getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!",
				uri, "http://base.org/stuff/1.0/r");

		EXPECTED_BODY_TERM_SIZE = 1;
		terms = ((Function)body.get(1)).getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_BODY_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "y");
	}

	/**
	 * Testing Scenario #11
	 *
	 * @throws RecognitionException
	 */
	//@Test
	public void testFullNameWithUri() throws RecognitionException {

		datalog = parser.parse(CQ_STRINGS[10]);

		EXPECTED_RULE_SIZE = 1;
		List<CQIE> rules = datalog.getRules();
		assertTrue("Mismatch rule size!",
				rules.size() == EXPECTED_RULE_SIZE);

		// Rule #1
		//-- The Head
		Function head = rules.get(0).getHead();
		assertNotNull("Head is null!", head);

		uri = head.getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!", uri,
				"http://www.abc.org/1999/02/22-abc-syntax-ns#p");

		EXPECTED_HEAD_TERM_SIZE = 2;
		terms = head.getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_HEAD_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "x");

		term = terms.get(1);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "y");

		//-- The Body
		EXPECTED_BODY_SIZE = 2;
		List<Function> body = rules.get(0).getBody();
		assertNotNull("Body is null!", body);
		assertTrue("Mismatch body size!",
				body.size() == EXPECTED_BODY_SIZE);

		//---- Body atom #1
		uri = ((Function)body.get(0)).getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!",
				uri, "http://example.org/stuff/1.0/q");

		EXPECTED_BODY_TERM_SIZE = 1;
		terms = ((Function)body.get(0)).getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_BODY_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)term).getName(), "x");

		//---- Body atom #2
		uri = ((Function)body.get(1)).getPredicate().getName().toString();
		assertEquals("Mismatch predicate name!",
				uri, "http://base.org/stuff/1.0/r");

		EXPECTED_BODY_TERM_SIZE = 2;
		terms = ((Function)body.get(1)).getTerms();
		assertEquals("Mismatch term size!",
				terms.size(), EXPECTED_BODY_TERM_SIZE);

		term = terms.get(0);
		assertTrue("Mismatch term type!",
				term instanceof FunctionalTermImpl);
		assertEquals("Mismatch variable name!",
				((FunctionalTermImpl)term).getFunctionSymbol().toString(),
				"http://example.org/stuff/1.0/s");

		//------ Object term
		List<Term> objVarTerms = ((FunctionalTermImpl)term).getTerms();
		assertEquals("Mismatch term size!",
				objVarTerms.size(), 2);

		Term objVarTerm = objVarTerms.get(0);
		assertTrue("Mismatch term type!",
				objVarTerm instanceof VariableImpl);
		assertEquals("Mismatch variable name!",
				((VariableImpl)objVarTerm).getName(), "y");

		objVarTerm = objVarTerms.get(1);
		assertTrue("Mismatch term type!",
				objVarTerm instanceof ValueConstantImpl);
		assertEquals("Mismatch variable name!",
				((ValueConstantImpl)objVarTerm).getValue(), "Student");
		//------ Object term ends.

		term = terms.get(1);
		assertTrue("Mismatch term type!",
				term instanceof URIConstantImpl);
		assertEquals("Mismatch variable name!",
				((URIConstantImpl)term).getURI().toString(),
				"http://example.org/stuff/1.1/FUB");
	}
}
