package it.unibz.inf.ontop.answering.reformulation.rewriting;

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

import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyBuilderImpl;
import it.unibz.inf.ontop.datalog.impl.CQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.datalog.LinearInclusionDependencies;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static it.unibz.inf.ontop.utils.ReformulationTestingTools.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CQCUtilitiesTest {

	CQIE initialquery1 = null;

	Term x = TERM_FACTORY.getVariable("x");
	Term y = TERM_FACTORY.getVariable("y");
	Term c1 = TERM_FACTORY.getConstantURI("URI1");
	Term c2 = TERM_FACTORY.getConstantLiteral("m");

	private Function getFunction(String name, List<Term> terms) {
		return TERM_FACTORY.getFunction(TERM_FACTORY.getPredicate(name, terms.size()), terms);
	}
	
	private Function getFunction(String name, Term term) {
		return getFunction(name, Collections.singletonList(term));
	}
	
	private Function getFunction(String name, Term term1, Term term2) {
		List<Term> list = new ArrayList<>(2);
		list.add(term1);
		list.add(term2);
		return getFunction(name, list);
	}
	
    @Before
	public void setUp() throws Exception {
		/*
		 * Creating the query:
		 * 
		 * q(x, <URI1>, 'm', y, f(x,y)) :- R(x,y), S('m', f(x), y)
		 * 
		 * Should generate
		 * 
		 * q('CANx1', <URI1>, 'm', 'CANy2', f('CANx1','CANy2')) :-
		 * R('CANx1','CANy2'), S('m', f('CANx1'), 'CANy')
		 */
		List<Term> headTerms = new LinkedList<Term>();
		headTerms.add(x);
		headTerms.add(c1);
		headTerms.add(c2);
		headTerms.add(y);
		List<Term> fterms1 = new LinkedList<Term>();
		fterms1.add(x);
		fterms1.add(y);
		headTerms.add(getFunction("f", fterms1));

		Function head = getFunction("q", headTerms);

		List<Function> body = new LinkedList<>();

		List<Term> atomTerms1 = new LinkedList<>();
		atomTerms1.add(x);
		atomTerms1.add(y);
		body.add(getFunction("R", atomTerms1));

		List<Term> atomTerms2 = new LinkedList<Term>();
		atomTerms2.add(c2);
		List<Term> fterms2 = new LinkedList<Term>();
		fterms2.add(x);
		atomTerms2.add(getFunction("f", fterms2));
		atomTerms2.add(y);
		body.add(getFunction("S", atomTerms2));

		initialquery1 = DATALOG_FACTORY.getCQIE(head, body);
	}
/*
 * ROMAN: commented out because there is no freeze anymore
    @Test
	public void testGrounding() {
    	CQContainmentCheckUnderLIDs.FreezeCQ c2cq = new CQContainmentCheckUnderLIDs.FreezeCQ(initialquery1.getHead(), initialquery1.getBody());

		List<Term> head = c2cq.getHead().getTerms();
		 	
    	final String CANx1 = ((ValueConstant)head.get(0)).getValue(); //    "f0" if standalone (f46 in travis)
    	final String CANy2 = ((ValueConstant)head.get(3)).getValue(); //    "f1" if standalone (f47 in travis)
		
		assertTrue(head.get(0).equals(TERM_FACTORY.getConstantLiteral(CANx1)));
		assertTrue(head.get(1).equals(TERM_FACTORY.getConstantURI("URI1")));
		assertTrue(head.get(2).equals(TERM_FACTORY.getConstantLiteral("m")));
		assertTrue(head.get(3).equals(TERM_FACTORY.getConstantLiteral(CANy2)));
		FunctionalTermImpl f1 = (FunctionalTermImpl) head.get(4);
		assertTrue(f1.getTerms().get(0).equals(TERM_FACTORY.getConstantLiteral(CANx1)));
		assertTrue(f1.getTerms().get(1).equals(TERM_FACTORY.getConstantLiteral(CANy2)));

		head = c2cq.getBodyAtoms(r).get(0).getTerms();
		assertTrue(head.get(0).equals(TERM_FACTORY.getConstantLiteral(CANx1)));
		assertTrue(head.get(1).equals(TERM_FACTORY.getConstantLiteral(CANy2)));

		head = c2cq.getBodyAtoms(s).get(0).getTerms();
		assertTrue(head.get(0).equals(TERM_FACTORY.getConstantLiteral("m")));
		f1 = (FunctionalTermImpl) head.get(1);
		assertTrue(f1.getTerms().get(0).equals(TERM_FACTORY.getConstantLiteral(CANx1)));
		assertTrue(head.get(2).equals(TERM_FACTORY.getConstantLiteral(CANy2)));
	}
*/
    @Test
	public void testContainment1() {

		// Query 1 - q(x,y) :- R(x,y), R(y,z)

		Function head = getFunction("q", x, y);

		List<Function> body = new LinkedList<Function>();

		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("R"), TERM_FACTORY.getVariable("x"), TERM_FACTORY.getVariable("y")));

		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("R"), TERM_FACTORY.getVariable("y"), TERM_FACTORY.getVariable("z")));

		CQIE q1 = DATALOG_FACTORY.getCQIE(head, body);

		// Query 2 - q(y,y) :- R(y,y)

		head = getFunction("q", TERM_FACTORY.getVariable("y"), TERM_FACTORY.getVariable("y"));

		body = new LinkedList<Function>();

		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("R"), TERM_FACTORY.getVariable("y"), TERM_FACTORY.getVariable("y")));

		CQIE q2 = DATALOG_FACTORY.getCQIE(head, body);

		// Query 3 - q(m,n) :- R(m,n)

		head = getFunction("q", TERM_FACTORY.getVariable("m"), TERM_FACTORY.getVariable("n"));
		body = new LinkedList<>();

		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("R"), TERM_FACTORY.getVariable("m"), TERM_FACTORY.getVariable("n")));

		CQIE q3 = DATALOG_FACTORY.getCQIE(head, body);

		// Query 4 - q(m,n) :- S(m,n) R(m,n)

		head = getFunction("q", TERM_FACTORY.getVariable("m"), TERM_FACTORY.getVariable("n"));

		body = new LinkedList<>();

		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("S"), TERM_FACTORY.getVariable("m"), TERM_FACTORY.getVariable("n")));

		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("R"), TERM_FACTORY.getVariable("m"), TERM_FACTORY.getVariable("n")));

		CQIE q4 = DATALOG_FACTORY.getCQIE(head, body);

		// Query 5 - q() :- S(x,y)

		head = getFunction("q", new LinkedList<Term>());
		body = new LinkedList<Function>();
		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("S"), TERM_FACTORY.getVariable("x"), TERM_FACTORY.getVariable("y")));

		CQIE q5 = DATALOG_FACTORY.getCQIE(head, body);

		// Query 6 - q() :- S(_,_))

		head = getFunction("q", new LinkedList<Term>());
		body = new LinkedList<Function>();
		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("S"), TERM_FACTORY.getVariable("w1"), TERM_FACTORY.getVariable("w2")));

		CQIE q6 = DATALOG_FACTORY.getCQIE(head, body);

		// Query 7 - q(x,y) :- R(x,y), P(y,_)

		head = getFunction("q", TERM_FACTORY.getVariable("x"), TERM_FACTORY.getVariable("y"));
		body = new LinkedList<Function>();
		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("R"),
				TERM_FACTORY.getVariable("x"), TERM_FACTORY.getVariable("y")));
		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("P"),
				TERM_FACTORY.getVariable("y"), TERM_FACTORY.getVariable("w1")));

		CQIE q7 = DATALOG_FACTORY.getCQIE(head, body);

		// Query 8 - q(x,y) :- R(x,y), P(_,_)

		head = getFunction("q", TERM_FACTORY.getVariable("x"), TERM_FACTORY.getVariable("y"));
		body = new LinkedList<Function>();
		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("R"),
				TERM_FACTORY.getVariable("x"), TERM_FACTORY.getVariable("y")));
		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("P"),
				TERM_FACTORY.getVariable("w1"), TERM_FACTORY.getVariable("w2")));

		CQIE q8 = DATALOG_FACTORY.getCQIE(head, body);

		// Query 9 - q() :- R(x,m), R(x,y), S(m,n), S(y,z),T(n,o),T(z,x)

		head = getFunction("q", new LinkedList<Term>());
		body = new LinkedList<Function>();
		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("R"), TERM_FACTORY.getVariable("x"), TERM_FACTORY.getVariable("m")));
		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("R"), TERM_FACTORY.getVariable("x"), TERM_FACTORY.getVariable("y")));
		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("S"), TERM_FACTORY.getVariable("m"), TERM_FACTORY.getVariable("n")));
		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("S"), TERM_FACTORY.getVariable("y"), TERM_FACTORY.getVariable("z")));
		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("T"), TERM_FACTORY.getVariable("n"), TERM_FACTORY.getVariable("o")));
		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("T"), TERM_FACTORY.getVariable("z"), TERM_FACTORY.getVariable("x")));

		CQIE q9 = DATALOG_FACTORY.getCQIE(head, body);

		// Query 10 - q() :- R(i,j), S(j,k), T(k,i)

		head = getFunction("q", new LinkedList<Term>());
		body = new LinkedList<Function>();
		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("R"), TERM_FACTORY.getVariable("i"), TERM_FACTORY.getVariable("j")));
		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("S"), TERM_FACTORY.getVariable("j"), TERM_FACTORY.getVariable("k")));
		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("T"), TERM_FACTORY.getVariable("k"), TERM_FACTORY.getVariable("i")));

		CQIE q10 = DATALOG_FACTORY.getCQIE(head, body);

		// Checking containment 5 in 6 and viceversa

		CQContainmentCheckUnderLIDs cqcu = new CQContainmentCheckUnderLIDs(DATALOG_FACTORY, UNIFIER_UTILITIES,
				SUBSTITUTION_UTILITIES, TERM_FACTORY);
		
		assertTrue(cqcu.isContainedIn(q6, q5));

		assertTrue(cqcu.isContainedIn(q5, q6));

		// checking containment of 7 in 8
		assertTrue(cqcu.isContainedIn(q7, q8));

		// checking non-containment of 8 in 7
		assertFalse(cqcu.isContainedIn(q8, q7));

		// Checking contaiment q2 <= q1
		assertTrue(cqcu.isContainedIn(q2, q1));

		// Checking contaiment q1 <= q2
		assertFalse(cqcu.isContainedIn(q1, q2));

		// Checking contaiment q1 <= q3
		assertTrue(cqcu.isContainedIn(q1, q3));

		// Checking contaiment q3 <= q1
		assertFalse(cqcu.isContainedIn(q3, q1));

		// Checking contaiment q1 <= q4
		assertFalse(cqcu.isContainedIn(q1, q4));

		// Checking contaiment q4 <= q1
		assertFalse(cqcu.isContainedIn(q4, q1));
		
		
		// Checking containment q9 <= q10 true
		assertTrue(cqcu.isContainedIn(q9, q10));
		
		// Checking containment q10 <= q9 true
		assertTrue(cqcu.isContainedIn(q10, q9));
	}

    @Test
	public void testSyntacticContainmentCheck() {
		// Query 1 - q(x) :- R(x,y), R(y,z), A(x)
		// Query 2 - q(x) :- R(x,y)
		// Query 3 - q(x) :- A(x)

		Function head = getFunction("q", x);

		List<Function> body = new LinkedList<>();

		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("R"), TERM_FACTORY.getVariable("x"), TERM_FACTORY.getVariable("y")));

		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("R"), TERM_FACTORY.getVariable("y"), TERM_FACTORY.getVariable("z")));

		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getClassPredicate("A"), TERM_FACTORY.getVariable("x")));

		CQIE q1 = DATALOG_FACTORY.getCQIE(head, body);

		// Query 2 - q(x) :- R(x,y)

		head = getFunction("q", TERM_FACTORY.getVariable("x"));

		body = new LinkedList<>();

		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("R"), TERM_FACTORY.getVariable("x"), TERM_FACTORY.getVariable("y")));

		CQIE q2 = DATALOG_FACTORY.getCQIE(head, body);

		// Query 3 - q(x) :- A(x)

		head = getFunction("q", TERM_FACTORY.getVariable("x"));

		body = new LinkedList<>();

		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getClassPredicate("A"), TERM_FACTORY.getVariable("x")));

		CQIE q3 = DATALOG_FACTORY.getCQIE(head, body);

		assertTrue(CQC_UTILITIES.SYNTACTIC_CHECK.isContainedIn(q1, q2));

		assertTrue(CQC_UTILITIES.SYNTACTIC_CHECK.isContainedIn(q1, q3));

		assertFalse(CQC_UTILITIES.SYNTACTIC_CHECK.isContainedIn(q2, q1));

		assertFalse(CQC_UTILITIES.SYNTACTIC_CHECK.isContainedIn(q3, q1));

	}

    @Test
	public void testRemovalOfSyntacticContainmentCheck() {
		/*
		 * Putting all queries in a list, in the end, query 1 must be removed
		 */

		// Query 1 - q(x) :- R(x,y), R(y,z), A(x)
		// Query 2 - q(x) :- R(x,y)
		// Query 3 - q(x) :- A(x)

		Function head = getFunction("q", x);

		List<Function> body = new LinkedList<>();

		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("R"), TERM_FACTORY.getVariable("x"), TERM_FACTORY.getVariable("y")));

		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("R"), TERM_FACTORY.getVariable("y"), TERM_FACTORY.getVariable("z")));

		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getClassPredicate("A"), TERM_FACTORY.getVariable("x")));

		CQIE q1 = DATALOG_FACTORY.getCQIE(head, body);

		// Query 2 - q(x) :- R(x,y)

		head = getFunction("q", TERM_FACTORY.getVariable("x"));
		body = new LinkedList<>();

		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("R"), TERM_FACTORY.getVariable("x"), TERM_FACTORY.getVariable("y")));

		CQIE q2 = DATALOG_FACTORY.getCQIE(head, body);

		// Query 3 - q(x) :- A(x)

		head = getFunction("q", TERM_FACTORY.getVariable("x"));
		body = new LinkedList<>();

		body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getClassPredicate("A"), TERM_FACTORY.getVariable("x")));

		CQIE q3 = DATALOG_FACTORY.getCQIE(head, body);

		LinkedList<CQIE> queries = new LinkedList<CQIE>();
		queries.add(q1);
		queries.add(q2);
		CQC_UTILITIES.removeContainedQueries(queries, CQC_UTILITIES.SYNTACTIC_CHECK);

		assertTrue(queries.size() == 1);
		assertTrue(queries.contains(q2));

		queries = new LinkedList<CQIE>();
		queries.add(q1);
		queries.add(q3);
		CQC_UTILITIES.removeContainedQueries(queries, CQC_UTILITIES.SYNTACTIC_CHECK);

		assertTrue(queries.size() == 1);
		assertTrue(queries.contains(q3));

		queries = new LinkedList<CQIE>();
		queries.add(q2);
		queries.add(q3);
		CQC_UTILITIES.removeContainedQueries(queries, CQC_UTILITIES.SYNTACTIC_CHECK);

		assertTrue(queries.size() == 2);
		assertTrue(queries.contains(q2));
		assertTrue(queries.contains(q3));

		queries = new LinkedList<CQIE>();
		queries.add(q1);
		queries.add(q2);
		queries.add(q3);
		CQC_UTILITIES.removeContainedQueries(queries, CQC_UTILITIES.SYNTACTIC_CHECK);

		assertTrue(queries.size() == 2);
		assertTrue(queries.contains(q2));
		assertTrue(queries.contains(q3));
	}

    @Test
	public void testSemanticContainment() throws Exception {

		/* we always assert true = isContainedIn(q1, q2) */

		{
			// q(x) :- A(x), q(y) :- C(y), with A ISA C
            OntologyBuilder builder = OntologyBuilderImpl.builder();
            OClass left = builder.declareClass("A");
            OClass right = builder.declareClass("C");
            builder.addSubClassOfAxiom(left, right);

			ClassifiedTBox sigma = builder.build().tbox();

			Function head1 = getFunction("q", Collections.<Term>singletonList(TERM_FACTORY.getVariable("x")));
			Function body1 = TERM_FACTORY.getFunction(ATOM_FACTORY.getClassPredicate("A"), TERM_FACTORY.getVariable("x"));
			CQIE query1 = DATALOG_FACTORY.getCQIE(head1, body1);

			Function head2 = getFunction("q", Collections.<Term>singletonList(TERM_FACTORY.getVariable("y")));
			Function body2 = TERM_FACTORY.getFunction(ATOM_FACTORY.getClassPredicate("C"), TERM_FACTORY.getVariable("y"));
			CQIE query2 = DATALOG_FACTORY.getCQIE(head2, body2);

			
			LinearInclusionDependencies dep = INCLUSION_DEPENDENCY_TOOLS.getABoxDependencies(sigma, false);
			
			CQContainmentCheckUnderLIDs cqc = new CQContainmentCheckUnderLIDs(dep, DATALOG_FACTORY, UNIFIER_UTILITIES,
					SUBSTITUTION_UTILITIES, TERM_FACTORY);
			
			assertTrue(cqc.isContainedIn(query1, query2));
			
			assertFalse(cqc.isContainedIn(query2, query1));
		}

		{
			// q(x) :- A(x), q(y) :- R(y,z), with A ISA exists R
            OntologyBuilder builder = OntologyBuilderImpl.builder();
            OClass left = builder.declareClass("A");
            ObjectPropertyExpression pright = builder.declareObjectProperty("R");

			ObjectSomeValuesFrom right = pright.getDomain();
			builder.addSubClassOfAxiom(left, right);
			ClassifiedTBox sigma = builder.build().tbox();

			Function head1 = getFunction("q", TERM_FACTORY.getVariable("x"));
			Function body1 = TERM_FACTORY.getFunction(ATOM_FACTORY.getClassPredicate("A"), TERM_FACTORY.getVariable("x"));
			CQIE query1 = DATALOG_FACTORY.getCQIE(head1, body1);

			Function head2 = getFunction("q", TERM_FACTORY.getVariable("y"));
			Function body2 = TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("R"),
					TERM_FACTORY.getVariable("y"), TERM_FACTORY.getVariable("z"));
			CQIE query2 = DATALOG_FACTORY.getCQIE(head2, body2);

			LinearInclusionDependencies dep = INCLUSION_DEPENDENCY_TOOLS.getABoxDependencies(sigma, false);

			CQContainmentCheckUnderLIDs cqc = new CQContainmentCheckUnderLIDs(dep, DATALOG_FACTORY, UNIFIER_UTILITIES,
					SUBSTITUTION_UTILITIES, TERM_FACTORY);
			
			assertTrue(cqc.isContainedIn(query1, query2));
			
			assertFalse(cqc.isContainedIn(query2, query1));
		}

		{
			// q(x) :- A(x), q(y) :- R(z,y), with A ISA exists inv(R)
            OntologyBuilder builder = OntologyBuilderImpl.builder();
            OClass left = builder.declareClass("A");
            ObjectPropertyExpression pright = builder.declareObjectProperty("R").getInverse();

			ObjectSomeValuesFrom right = pright.getDomain();
			builder.addSubClassOfAxiom(left, right);
			ClassifiedTBox sigma = builder.build().tbox();

			Function head1 = getFunction("q", TERM_FACTORY.getVariable("x"));
			Function body1 = TERM_FACTORY.getFunction(ATOM_FACTORY.getClassPredicate("A"), TERM_FACTORY.getVariable("x"));
			CQIE query1 = DATALOG_FACTORY.getCQIE(head1, body1);

			Function head2 = getFunction("q", TERM_FACTORY.getVariable("y"));
			Function body2 = TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("R"),
					TERM_FACTORY.getVariable("z"), TERM_FACTORY.getVariable("y"));
			CQIE query2 = DATALOG_FACTORY.getCQIE(head2, body2);

			LinearInclusionDependencies dep = INCLUSION_DEPENDENCY_TOOLS.getABoxDependencies(sigma, false);
			
			CQContainmentCheckUnderLIDs cqc = new CQContainmentCheckUnderLIDs(dep, DATALOG_FACTORY, UNIFIER_UTILITIES,
					SUBSTITUTION_UTILITIES, TERM_FACTORY);
			
			assertTrue(cqc.isContainedIn(query1, query2));
			
			assertFalse(cqc.isContainedIn(query2, query1));
		}

		{
			// q(x) :- R(x,y), q(z) :- A(z), with exists R ISA A
            OntologyBuilder builder = OntologyBuilderImpl.builder();
            OClass right = builder.declareClass("A");
            ObjectPropertyExpression pleft = builder.declareObjectProperty("R");

			ObjectSomeValuesFrom left = pleft.getDomain();
			builder.addSubClassOfAxiom(left, right);
			ClassifiedTBox sigma = builder.build().tbox();

			Function head1 = getFunction("q", TERM_FACTORY.getVariable("x"));
			Function body1 = TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("R"),
					TERM_FACTORY.getVariable("x"), TERM_FACTORY.getVariable("y"));
			CQIE query1 = DATALOG_FACTORY.getCQIE(head1, body1);

			Function head2 = getFunction("q", TERM_FACTORY.getVariable("z"));
			Function body2 = TERM_FACTORY.getFunction(ATOM_FACTORY.getClassPredicate("A"), TERM_FACTORY.getVariable("z"));
			CQIE query2 = DATALOG_FACTORY.getCQIE(head2, body2);

			LinearInclusionDependencies dep = INCLUSION_DEPENDENCY_TOOLS.getABoxDependencies(sigma, false);

			CQContainmentCheckUnderLIDs cqc = new CQContainmentCheckUnderLIDs(dep, DATALOG_FACTORY, UNIFIER_UTILITIES,
					SUBSTITUTION_UTILITIES, TERM_FACTORY);
			
			assertTrue(cqc.isContainedIn(query1, query2));
			
			assertFalse(cqc.isContainedIn(query2, query1));
		}

		{
			// q(y) :- R(x,y), q(z) :- A(z), with exists inv(R) ISA A

            OntologyBuilder builder = OntologyBuilderImpl.builder();
            OClass right = builder.declareClass("A");
            ObjectPropertyExpression pleft = builder.declareObjectProperty("R").getInverse();

			ObjectSomeValuesFrom left = pleft.getDomain();
			builder.addSubClassOfAxiom(left, right);
			ClassifiedTBox sigma = builder.build().tbox();

			Function head1 = getFunction("q", TERM_FACTORY.getVariable("y"));
			Function body1 = TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("R"),
					TERM_FACTORY.getVariable("x"), TERM_FACTORY.getVariable("y"));
			CQIE query1 = DATALOG_FACTORY.getCQIE(head1, body1);

			Function head2 = getFunction("q", TERM_FACTORY.getVariable("z"));
			Function body2 = TERM_FACTORY.getFunction(ATOM_FACTORY.getClassPredicate("A"), TERM_FACTORY.getVariable("z"));
			CQIE query2 = DATALOG_FACTORY.getCQIE(head2, body2);

			LinearInclusionDependencies dep = INCLUSION_DEPENDENCY_TOOLS.getABoxDependencies(sigma, false);

			CQContainmentCheckUnderLIDs cqc = new CQContainmentCheckUnderLIDs(dep, DATALOG_FACTORY, UNIFIER_UTILITIES,
					SUBSTITUTION_UTILITIES, TERM_FACTORY);
			
			assertTrue(cqc.isContainedIn(query1, query2));
			
			assertFalse(cqc.isContainedIn(query2, query1));
		}

		// q(x) :- A(x), q(y) :- C(y), with A ISA B, B ISA C

		// q(x) :- A(x), q(y) :- C(y), with A ISA exists R, exists R ISA C

		// q(x) :- A(x), q(y) :- C(y), with A ISA exists inv(R), exists inv(R)
		// ISA C

		// q(x,y) :- R(x,y), q(s,t) :- S(s,t), with R ISA S

		// q(x,y) :- R(x,y), q(s,t) :- S(s,t), with R ISA M, M ISA S

		// q(x,y) :- R(x,y), q(s,t) :- S(s,t), with R ISA inv(M), inv(M) ISA S

		// q(x,y) :- R(x,y), q(s,t) :- S(s,t), with inv(R) ISA M, M ISA inv(S)

	}

    //Facts should not be removed by the CQC_UTILITIES
    @Test
    public void testFacts() throws Exception {

        // q(x) :- , q(x) :- R(x,y), A(x)

        OntologyBuilder builder = OntologyBuilderImpl.builder();
        OClass left = builder.declareClass("A");
        ObjectPropertyExpression pleft = builder.declareObjectProperty("R");

        ObjectSomeValuesFrom right = pleft.getDomain();
        builder.addSubClassOfAxiom(left, right);
		ClassifiedTBox sigma = builder.build().tbox();

        // Query 1 q(x) :- R(x,y), A(x)
        Function head = getFunction("q", x);

        List<Function> body = new LinkedList<>();

        body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate("R"),
				TERM_FACTORY.getVariable("x"), TERM_FACTORY.getVariable("y")));

        body.add(TERM_FACTORY.getFunction(ATOM_FACTORY.getClassPredicate("A"), TERM_FACTORY.getVariable("x")));

        CQIE query1 = DATALOG_FACTORY.getCQIE(head, body);

        // Query 2 q(x) :- (with empty body)

        head = getFunction("q", TERM_FACTORY.getVariable("x"));
        body = new LinkedList<>();
        CQIE query2 = DATALOG_FACTORY.getCQIE(head, body);

		LinearInclusionDependencies dep = INCLUSION_DEPENDENCY_TOOLS.getABoxDependencies(sigma, false);
		CQContainmentCheckUnderLIDs cqc = new CQContainmentCheckUnderLIDs(dep, DATALOG_FACTORY, UNIFIER_UTILITIES,
				SUBSTITUTION_UTILITIES, TERM_FACTORY);
				
        assertTrue(cqc.isContainedIn(query1, query2));  // ROMAN: changed from False

        assertFalse(cqc.isContainedIn(query2, query1));

        assertTrue(CQC_UTILITIES.SYNTACTIC_CHECK.isContainedIn(query1, query2)); // ROMAN: changed from False
        
        assertFalse(CQC_UTILITIES.SYNTACTIC_CHECK.isContainedIn(query2, query1));
    }
}
