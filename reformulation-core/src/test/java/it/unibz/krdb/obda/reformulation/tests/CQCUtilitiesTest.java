package it.unibz.krdb.obda.reformulation.tests;

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

import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.ObjectSomeValuesFrom;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.OntologyVocabulary;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQCUtilities;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQContainmentCheckUnderLIDs;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.LinearInclusionDependencies;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CQCUtilitiesTest {

	CQIE initialquery1 = null;

	//PositiveInclusionApplicator piapplicator = new PositiveInclusionApplicator();

	OBDADataFactory pfac = OBDADataFactoryImpl.getInstance();
	OBDADataFactory tfac = OBDADataFactoryImpl.getInstance();

	Predicate r = pfac.getPredicate("R", new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT });
	Predicate s = pfac.getPredicate("S", new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT, COL_TYPE.OBJECT });
	Predicate q = pfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT, COL_TYPE.OBJECT,
			COL_TYPE.OBJECT, COL_TYPE.OBJECT });

	Term x = tfac.getVariable("x");
	Term y = tfac.getVariable("y");
	Term c1 = tfac.getConstantURI("URI1");
	Term c2 = tfac.getConstantLiteral("m");

//	Term u1 = tfac.getVariableNondistinguished();
//	Term u2 = tfac.getVariableNondistinguished();

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
		headTerms.add(tfac.getFunction(pfac.getPredicate("f", new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }),
				fterms1));

		Function head = tfac.getFunction(q, headTerms);

		List<Function> body = new LinkedList<Function>();

		List<Term> atomTerms1 = new LinkedList<Term>();
		atomTerms1.add(x);
		atomTerms1.add(y);
		body.add(tfac.getFunction(r, atomTerms1));

		List<Term> atomTerms2 = new LinkedList<Term>();
		atomTerms2.add(c2);
		List<Term> fterms2 = new LinkedList<Term>();
		fterms2.add(x);
		atomTerms2.add(tfac.getFunction(pfac.getPredicate("f", new COL_TYPE[] { COL_TYPE.OBJECT }), fterms2));
		atomTerms2.add(y);
		body.add(tfac.getFunction(s, atomTerms2));

		initialquery1 = tfac.getCQIE(head, body);
	}
/*
 * ROMAN: commented out because there is no freeze anymore
    @Test
	public void testGrounding() {
    	CQContainmentCheckUnderLIDs.FreezeCQ c2cq = new CQContainmentCheckUnderLIDs.FreezeCQ(initialquery1.getHead(), initialquery1.getBody());

		List<Term> head = c2cq.getHead().getTerms();
		 	
    	final String CANx1 = ((ValueConstant)head.get(0)).getValue(); //    "f0" if standalone (f46 in travis)
    	final String CANy2 = ((ValueConstant)head.get(3)).getValue(); //    "f1" if standalone (f47 in travis)
		
		assertTrue(head.get(0).equals(tfac.getConstantLiteral(CANx1)));
		assertTrue(head.get(1).equals(tfac.getConstantURI("URI1")));
		assertTrue(head.get(2).equals(tfac.getConstantLiteral("m")));
		assertTrue(head.get(3).equals(tfac.getConstantLiteral(CANy2)));
		FunctionalTermImpl f1 = (FunctionalTermImpl) head.get(4);
		assertTrue(f1.getTerms().get(0).equals(tfac.getConstantLiteral(CANx1)));
		assertTrue(f1.getTerms().get(1).equals(tfac.getConstantLiteral(CANy2)));

		head = c2cq.getBodyAtoms(r).get(0).getTerms();
		assertTrue(head.get(0).equals(tfac.getConstantLiteral(CANx1)));
		assertTrue(head.get(1).equals(tfac.getConstantLiteral(CANy2)));

		head = c2cq.getBodyAtoms(s).get(0).getTerms();
		assertTrue(head.get(0).equals(tfac.getConstantLiteral("m")));
		f1 = (FunctionalTermImpl) head.get(1);
		assertTrue(f1.getTerms().get(0).equals(tfac.getConstantLiteral(CANx1)));
		assertTrue(head.get(2).equals(tfac.getConstantLiteral(CANy2)));
	}
*/
    @Test
	public void testContainment1() {

		// Query 1 - q(x,y) :- R(x,y), R(y,z)

		List<Term> headTerms = new LinkedList<Term>();
		headTerms.add(x);
		headTerms.add(y);

		Function head = tfac.getFunction(pfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }), headTerms);

		List<Function> body = new LinkedList<Function>();

		List<Term> terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("x"));
		terms.add(tfac.getVariable("y"));
		body.add(tfac.getFunction(pfac.getObjectPropertyPredicate("R"), terms));

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("y"));
		terms.add(tfac.getVariable("z"));
		body.add(tfac.getFunction(pfac.getObjectPropertyPredicate("R"), terms));

		CQIE q1 = tfac.getCQIE(head, body);

		// Query 2 - q(y,y) :- R(y,y)

		headTerms = new LinkedList<Term>();
		headTerms.add(tfac.getVariable("y"));
		headTerms.add(tfac.getVariable("y"));

		head = tfac.getFunction(pfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }), headTerms);

		body = new LinkedList<Function>();

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("y"));
		terms.add(tfac.getVariable("y"));
		body.add(tfac.getFunction(pfac.getObjectPropertyPredicate("R"), terms));

		CQIE q2 = tfac.getCQIE(head, body);

		// Query 3 - q(m,n) :- R(m,n)

		headTerms = new LinkedList<Term>();
		headTerms.add(tfac.getVariable("m"));
		headTerms.add(tfac.getVariable("n"));

		head = tfac.getFunction(pfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }), headTerms);

		body = new LinkedList<Function>();

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("m"));
		terms.add(tfac.getVariable("n"));
		body.add(tfac.getFunction(pfac.getObjectPropertyPredicate("R"), terms));

		CQIE q3 = tfac.getCQIE(head, body);

		// Query 4 - q(m,n) :- S(m,n) R(m,n)

		headTerms = new LinkedList<Term>();
		headTerms.add(tfac.getVariable("m"));
		headTerms.add(tfac.getVariable("n"));

		head = tfac.getFunction(pfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }), headTerms);

		body = new LinkedList<Function>();

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("m"));
		terms.add(tfac.getVariable("n"));
		body.add(tfac.getFunction(pfac.getObjectPropertyPredicate("S"), terms));

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("m"));
		terms.add(tfac.getVariable("n"));
		body.add(tfac.getFunction(pfac.getObjectPropertyPredicate("R"), terms));

		CQIE q4 = tfac.getCQIE(head, body);

		// Query 5 - q() :- S(x,y)

		head = pfac.getFunction(pfac.getPredicate("q", new COL_TYPE[] {}), new LinkedList<Term>());
		body = new LinkedList<Function>();
		body.add(pfac.getFunction(pfac.getObjectPropertyPredicate("S"), pfac.getVariable("x"), pfac.getVariable("y")));

		CQIE q5 = pfac.getCQIE(head, body);

		// Query 6 - q() :- S(_,_))

		head = pfac.getFunction(pfac.getPredicate("q", new COL_TYPE[] {}), new LinkedList<Term>());
		body = new LinkedList<Function>();
		body.add(pfac.getFunction(pfac.getObjectPropertyPredicate("S"), pfac.getVariable("w1"), pfac.getVariable("w2")));

		CQIE q6 = pfac.getCQIE(head, body);

		// Query 7 - q(x,y) :- R(x,y), P(y,_)

		head = pfac.getFunction(pfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }),
				pfac.getVariable("x"), pfac.getVariable("y"));
		body = new LinkedList<Function>();
		body.add(pfac.getFunction(pfac.getObjectPropertyPredicate("R"),
				pfac.getVariable("x"), pfac.getVariable("y")));
		body.add(pfac.getFunction(pfac.getObjectPropertyPredicate("P"),
				pfac.getVariable("y"), pfac.getVariable("w1")));

		CQIE q7 = pfac.getCQIE(head, body);

		// Query 8 - q(x,y) :- R(x,y), P(_,_)

		head = pfac.getFunction(pfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }),
				pfac.getVariable("x"), pfac.getVariable("y"));
		body = new LinkedList<Function>();
		body.add(pfac.getFunction(pfac.getObjectPropertyPredicate("R"),
				pfac.getVariable("x"), pfac.getVariable("y")));
		body.add(pfac.getFunction(pfac.getObjectPropertyPredicate("P"),
				pfac.getVariable("w1"), pfac.getVariable("w2")));

		CQIE q8 = pfac.getCQIE(head, body);

		// Query 9 - q() :- R(x,m), R(x,y), S(m,n), S(y,z),T(n,o),T(z,x)

		head = pfac.getFunction(pfac.getPredicate("q", new COL_TYPE[] {}), new LinkedList<Term>());
		body = new LinkedList<Function>();
		body.add(pfac.getFunction(pfac.getObjectPropertyPredicate("R"), pfac.getVariable("x"), pfac.getVariable("m")));
		body.add(pfac.getFunction(pfac.getObjectPropertyPredicate("R"), pfac.getVariable("x"), pfac.getVariable("y")));
		body.add(pfac.getFunction(pfac.getObjectPropertyPredicate("S"), pfac.getVariable("m"), pfac.getVariable("n")));
		body.add(pfac.getFunction(pfac.getObjectPropertyPredicate("S"), pfac.getVariable("y"), pfac.getVariable("z")));
		body.add(pfac.getFunction(pfac.getObjectPropertyPredicate("T"), pfac.getVariable("n"), pfac.getVariable("o")));
		body.add(pfac.getFunction(pfac.getObjectPropertyPredicate("T"), pfac.getVariable("z"), pfac.getVariable("x")));

		CQIE q9 = pfac.getCQIE(head, body);

		// Query 10 - q() :- R(i,j), S(j,k), T(k,i)

		head = pfac.getFunction(pfac.getPredicate("q", new COL_TYPE[] {}), new LinkedList<Term>());
		body = new LinkedList<Function>();
		body.add(pfac.getFunction(pfac.getObjectPropertyPredicate("R"), pfac.getVariable("i"), pfac.getVariable("j")));
		body.add(pfac.getFunction(pfac.getObjectPropertyPredicate("S"), pfac.getVariable("j"), pfac.getVariable("k")));
		body.add(pfac.getFunction(pfac.getObjectPropertyPredicate("T"), pfac.getVariable("k"), pfac.getVariable("i")));

		CQIE q10 = pfac.getCQIE(head, body);

		// Checking containment 5 in 6 and viceversa

		CQContainmentCheckUnderLIDs cqcu = new CQContainmentCheckUnderLIDs();
		
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

		List<Term> headTerms = new LinkedList<Term>();
		headTerms.add(x);

		Function head = tfac.getFunction(pfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT }), headTerms);

		List<Function> body = new LinkedList<Function>();

		List<Term> terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("x"));
		terms.add(tfac.getVariable("y"));
		body.add(tfac.getFunction(pfac.getObjectPropertyPredicate("R"), terms));

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("y"));
		terms.add(tfac.getVariable("z"));
		body.add(tfac.getFunction(pfac.getObjectPropertyPredicate("R"), terms));

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("x"));
		body.add(tfac.getFunction(pfac.getClassPredicate("A"), terms));

		CQIE q1 = tfac.getCQIE(head, body);

		// Query 2 - q(x) :- R(x,y)

		headTerms = new LinkedList<Term>();
		headTerms.add(tfac.getVariable("x"));

		head = tfac.getFunction(pfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT }), headTerms);

		body = new LinkedList<Function>();

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("x"));
		terms.add(tfac.getVariable("y"));
		body.add(tfac.getFunction(pfac.getObjectPropertyPredicate("R"), terms));

		CQIE q2 = tfac.getCQIE(head, body);

		// Query 3 - q(x) :- A(x)

		headTerms = new LinkedList<Term>();
		headTerms.add(tfac.getVariable("x"));

		head = tfac.getFunction(pfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT }), headTerms);

		body = new LinkedList<Function>();

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("x"));
		body.add(tfac.getFunction(pfac.getClassPredicate("A"), terms));

		CQIE q3 = tfac.getCQIE(head, body);

		assertTrue(CQCUtilities.SYNTACTIC_CHECK.isContainedIn(q1, q2));

		assertTrue(CQCUtilities.SYNTACTIC_CHECK.isContainedIn(q1, q3));

		assertFalse(CQCUtilities.SYNTACTIC_CHECK.isContainedIn(q2, q1));

		assertFalse(CQCUtilities.SYNTACTIC_CHECK.isContainedIn(q3, q1));

	}

    @Test
	public void testRemovalOfSyntacticContainmentCheck() {
		/*
		 * Putting all queries in a list, in the end, query 1 must be removed
		 */

		// Query 1 - q(x) :- R(x,y), R(y,z), A(x)
		// Query 2 - q(x) :- R(x,y)
		// Query 3 - q(x) :- A(x)

		List<Term> headTerms = new LinkedList<Term>();
		headTerms.add(x);

		Function head = tfac.getFunction(pfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT }), headTerms);

		List<Function> body = new LinkedList<Function>();

		List<Term> terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("x"));
		terms.add(tfac.getVariable("y"));
		body.add(tfac.getFunction(pfac.getObjectPropertyPredicate("R"), terms));

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("y"));
		terms.add(tfac.getVariable("z"));
		body.add(tfac.getFunction(pfac.getObjectPropertyPredicate("R"), terms));

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("x"));
		body.add(tfac.getFunction(pfac.getClassPredicate("A"), terms));

		CQIE q1 = tfac.getCQIE(head, body);

		// Query 2 - q(x) :- R(x,y)

		headTerms = new LinkedList<Term>();
		headTerms.add(tfac.getVariable("x"));

		head = tfac.getFunction(pfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT }), headTerms);

		body = new LinkedList<Function>();

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("x"));
		terms.add(tfac.getVariable("y"));
		body.add(tfac.getFunction(pfac.getObjectPropertyPredicate("R"), terms));

		CQIE q2 = tfac.getCQIE(head, body);

		// Query 3 - q(x) :- A(x)

		headTerms = new LinkedList<Term>();
		headTerms.add(tfac.getVariable("x"));

		head = tfac.getFunction(pfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT }), headTerms);

		body = new LinkedList<Function>();

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("x"));
		body.add(tfac.getFunction(pfac.getClassPredicate("A"), terms));

		CQIE q3 = tfac.getCQIE(head, body);

		LinkedList<CQIE> queries = new LinkedList<CQIE>();
		queries.add(q1);
		queries.add(q2);
		CQCUtilities.removeContainedQueries(queries, CQCUtilities.SYNTACTIC_CHECK);

		assertTrue(queries.size() == 1);
		assertTrue(queries.contains(q2));

		queries = new LinkedList<CQIE>();
		queries.add(q1);
		queries.add(q3);
		CQCUtilities.removeContainedQueries(queries, CQCUtilities.SYNTACTIC_CHECK);

		assertTrue(queries.size() == 1);
		assertTrue(queries.contains(q3));

		queries = new LinkedList<CQIE>();
		queries.add(q2);
		queries.add(q3);
		CQCUtilities.removeContainedQueries(queries, CQCUtilities.SYNTACTIC_CHECK);

		assertTrue(queries.size() == 2);
		assertTrue(queries.contains(q2));
		assertTrue(queries.contains(q3));

		queries = new LinkedList<CQIE>();
		queries.add(q1);
		queries.add(q2);
		queries.add(q3);
		CQCUtilities.removeContainedQueries(queries, CQCUtilities.SYNTACTIC_CHECK);

		assertTrue(queries.size() == 2);
		assertTrue(queries.contains(q2));
		assertTrue(queries.contains(q3));
	}

    @Test
	public void testSemanticContainment() throws Exception {
		OntologyFactory dfac = OntologyFactoryImpl.getInstance();

		/* we always assert true = isContainedIn(q1, q2) */

		{
			// q(x) :- A(x), q(y) :- C(y), with A ISA C
			
	        OntologyVocabulary vb = dfac.createVocabulary();
			OClass left = vb.createClass("A");
			OClass right = vb.createClass("C");
	        
			Ontology sigma = dfac.createOntology(vb);
			sigma.addSubClassOfAxiom(left, right);

			Function head1 = tfac.getFunction(tfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("x"));
			Function body1 = tfac.getFunction(tfac.getClassPredicate("A"), tfac.getVariable("x"));
			CQIE query1 = tfac.getCQIE(head1, body1);

			Function head2 = tfac.getFunction(tfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("y"));
			Function body2 = tfac.getFunction(tfac.getClassPredicate("C"), tfac.getVariable("y"));
			CQIE query2 = tfac.getCQIE(head2, body2);

			
			LinearInclusionDependencies dep = LinearInclusionDependencies.getABoxDependencies(TBoxReasonerImpl.create(sigma), false);
			
			CQContainmentCheckUnderLIDs cqc = new CQContainmentCheckUnderLIDs(dep);
			
			assertTrue(cqc.isContainedIn(query1, query2));
			
			assertFalse(cqc.isContainedIn(query2, query1));
		}

		{
			// q(x) :- A(x), q(y) :- R(y,z), with A ISA exists R
	        OntologyVocabulary vb = dfac.createVocabulary();
	        OClass left = vb.createClass("A");
			ObjectPropertyExpression pright = vb.createObjectProperty("R");
			
			Ontology sigma = dfac.createOntology(vb);
			ObjectSomeValuesFrom right = pright.getDomain();
			sigma.addSubClassOfAxiom(left, right);

			Function head1 = tfac.getFunction(tfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("x"));
			Function body1 = tfac.getFunction(tfac.getClassPredicate("A"), tfac.getVariable("x"));
			CQIE query1 = tfac.getCQIE(head1, body1);

			Function head2 = tfac.getFunction(tfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("y"));
			Function body2 = tfac.getFunction(tfac.getObjectPropertyPredicate("R"),
					tfac.getVariable("y"), tfac.getVariable("z"));
			CQIE query2 = tfac.getCQIE(head2, body2);

			LinearInclusionDependencies dep = LinearInclusionDependencies.getABoxDependencies(TBoxReasonerImpl.create(sigma), false);

			CQContainmentCheckUnderLIDs cqc = new CQContainmentCheckUnderLIDs(dep);
			
			assertTrue(cqc.isContainedIn(query1, query2));
			
			assertFalse(cqc.isContainedIn(query2, query1));
		}

		{
			// q(x) :- A(x), q(y) :- R(z,y), with A ISA exists inv(R)
	        OntologyVocabulary vb = dfac.createVocabulary();			
			OClass left = vb.createClass("A");
			ObjectPropertyExpression pright = vb.createObjectProperty("R").getInverse();
						
			Ontology sigma = dfac.createOntology(vb);
			ObjectSomeValuesFrom right = pright.getDomain();
			sigma.addSubClassOfAxiom(left, right);

			Function head1 = tfac.getFunction(tfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("x"));
			Function body1 = tfac.getFunction(tfac.getClassPredicate("A"), tfac.getVariable("x"));
			CQIE query1 = tfac.getCQIE(head1, body1);

			Function head2 = tfac.getFunction(tfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("y"));
			Function body2 = tfac.getFunction(tfac.getObjectPropertyPredicate("R"),
					tfac.getVariable("z"), tfac.getVariable("y"));
			CQIE query2 = tfac.getCQIE(head2, body2);

			LinearInclusionDependencies dep = LinearInclusionDependencies.getABoxDependencies(TBoxReasonerImpl.create(sigma), false);
			
			CQContainmentCheckUnderLIDs cqc = new CQContainmentCheckUnderLIDs(dep);
			
			assertTrue(cqc.isContainedIn(query1, query2));
			
			assertFalse(cqc.isContainedIn(query2, query1));
		}

		{
			// q(x) :- R(x,y), q(z) :- A(z), with exists R ISA A
	        OntologyVocabulary vb = dfac.createVocabulary();			
			ObjectPropertyExpression pleft = vb.createObjectProperty("R");
			OClass right = vb.createClass("A");
			
			Ontology sigma = dfac.createOntology(vb);
			ObjectSomeValuesFrom left = pleft.getDomain();
			sigma.addSubClassOfAxiom(left, right);

			Function head1 = tfac.getFunction(tfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("x"));
			Function body1 = tfac.getFunction(tfac.getObjectPropertyPredicate("R"),
					tfac.getVariable("x"), tfac.getVariable("y"));
			CQIE query1 = tfac.getCQIE(head1, body1);

			Function head2 = tfac.getFunction(tfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("z"));
			Function body2 = tfac.getFunction(tfac.getClassPredicate("A"), tfac.getVariable("z"));
			CQIE query2 = tfac.getCQIE(head2, body2);

			LinearInclusionDependencies dep = LinearInclusionDependencies.getABoxDependencies(TBoxReasonerImpl.create(sigma), false);

			CQContainmentCheckUnderLIDs cqc = new CQContainmentCheckUnderLIDs(dep);
			
			assertTrue(cqc.isContainedIn(query1, query2));
			
			assertFalse(cqc.isContainedIn(query2, query1));
		}

		{
			// q(y) :- R(x,y), q(z) :- A(z), with exists inv(R) ISA A
			
	        OntologyVocabulary vb = dfac.createVocabulary();
			OClass right = vb.createClass("A");
			ObjectPropertyExpression pleft = vb.createObjectProperty("R").getInverse();
	        
			Ontology sigma = dfac.createOntology(vb);			
			ObjectSomeValuesFrom left = pleft.getDomain();
			sigma.addSubClassOfAxiom(left, right);

			Function head1 = tfac.getFunction(tfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("y"));
			Function body1 = tfac.getFunction(tfac.getObjectPropertyPredicate("R"),
					tfac.getVariable("x"), tfac.getVariable("y"));
			CQIE query1 = tfac.getCQIE(head1, body1);

			Function head2 = tfac.getFunction(tfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("z"));
			Function body2 = tfac.getFunction(tfac.getClassPredicate("A"), tfac.getVariable("z"));
			CQIE query2 = tfac.getCQIE(head2, body2);

			LinearInclusionDependencies dep = LinearInclusionDependencies.getABoxDependencies(TBoxReasonerImpl.create(sigma), false);

			CQContainmentCheckUnderLIDs cqc = new CQContainmentCheckUnderLIDs(dep);
			
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

    //Facts should not be removed by the CQCUtilities
    @Test
    public void testFacts() throws Exception {

        OntologyFactory dfac = OntologyFactoryImpl.getInstance();

        // q(x) :- , q(x) :- R(x,y), A(x)

        OntologyVocabulary vb = dfac.createVocabulary();
        OClass left = vb.createClass("A");
        ObjectPropertyExpression pleft = vb.createObjectProperty("R");
        
        Ontology sigma = dfac.createOntology(vb);
        ObjectSomeValuesFrom right = pleft.getDomain();
        sigma.addSubClassOfAxiom(left, right);


        // Query 1 q(x) :- R(x,y), A(x)
        List<Term> headTerms = new LinkedList<Term>();
        headTerms.add(x);

        Function head = tfac.getFunction(pfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT }), headTerms);

        List<Function> body = new LinkedList<Function>();

        List<Term> terms = new LinkedList<Term>();
        terms.add(tfac.getVariable("x"));
        terms.add(tfac.getVariable("y"));
        body.add(tfac.getFunction(pfac.getObjectPropertyPredicate("R"), terms));

        terms = new LinkedList<Term>();
        terms.add(tfac.getVariable("x"));
        body.add(tfac.getFunction(pfac.getClassPredicate("A"), terms));

        CQIE query1 = tfac.getCQIE(head, body);

        // Query 2 q(x) :- (with empty body)

        headTerms = new LinkedList<Term>();
        headTerms.add(tfac.getVariable("x"));
        head = tfac.getFunction(pfac.getPredicate("q", new COL_TYPE[] { COL_TYPE.OBJECT }), headTerms);
        body = new LinkedList<Function>();
        CQIE query2 = tfac.getCQIE(head, body);

		LinearInclusionDependencies dep = LinearInclusionDependencies.getABoxDependencies(TBoxReasonerImpl.create(sigma), false);
		CQContainmentCheckUnderLIDs cqc = new CQContainmentCheckUnderLIDs(dep);
				
        assertTrue(cqc.isContainedIn(query1, query2));  // ROMAN: changed from False

        assertFalse(cqc.isContainedIn(query2, query1));

        assertTrue(CQCUtilities.SYNTACTIC_CHECK.isContainedIn(query1, query2)); // ROMAN: changed from False
        
        assertFalse(CQCUtilities.SYNTACTIC_CHECK.isContainedIn(query2, query1));
    }
}
