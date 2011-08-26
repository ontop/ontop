package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQCUtilities;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.PositiveInclusionApplicator;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicDescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DLLiterConceptInclusionImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DLLiterOntologyImpl;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Before;

public class CQCUtilitiesTest extends TestCase {

	CQIE						initialquery1	= null;

	PositiveInclusionApplicator	piapplicator	= new PositiveInclusionApplicator();

	OBDADataFactory				pfac			= OBDADataFactoryImpl.getInstance();
	OBDADataFactory				tfac			= OBDADataFactoryImpl.getInstance();

	Predicate					r				= pfac.getPredicate(URI.create("R"), 2);
	Predicate					s				= pfac.getPredicate(URI.create("S"), 3);
	Predicate					q				= pfac.getPredicate(URI.create("q"), 5);

	Term						x				= tfac.getVariable("x");
	Term						y				= tfac.getVariable("y");
	Term						c1				= tfac.getURIConstant(URI.create("URI1"));
	Term						c2				= tfac.getValueConstant("m");

	Term						u1				= tfac.getNondistinguishedVariable();
	Term						u2				= tfac.getNondistinguishedVariable();

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
		headTerms.add(tfac.getFunctionalTerm(pfac.getPredicate(URI.create("f"), 2), fterms1));

		Atom head = tfac.getAtom(q, headTerms);

		List<Atom> body = new LinkedList<Atom>();

		List<Term> atomTerms1 = new LinkedList<Term>();
		atomTerms1.add(x);
		atomTerms1.add(y);
		body.add(tfac.getAtom(r, atomTerms1));

		List<Term> atomTerms2 = new LinkedList<Term>();
		atomTerms2.add(c2);
		List<Term> fterms2 = new LinkedList<Term>();
		fterms2.add(x);
		atomTerms2.add(tfac.getFunctionalTerm(pfac.getPredicate(URI.create("f"), 1), fterms2));
		atomTerms2.add(y);
		body.add(tfac.getAtom(s, atomTerms2));

		initialquery1 = tfac.getCQIE(head, body);
	}

	public void testGrounding() {
		CQCUtilities cqcutil = new CQCUtilities(initialquery1);
		CQIE groundedcq = cqcutil.getCanonicalQuery(initialquery1);

		List<Term> head = groundedcq.getHead().getTerms();
		assertTrue(head.get(0).equals(tfac.getValueConstant("CANx1")));
		assertTrue(head.get(1).equals(tfac.getURIConstant(URI.create("URI1"))));
		assertTrue(head.get(2).equals(tfac.getValueConstant("m")));
		assertTrue(head.get(3).equals(tfac.getValueConstant("CANy2")));
		FunctionalTermImpl f1 = (FunctionalTermImpl) head.get(4);
		assertTrue(f1.getTerms().get(0).equals(tfac.getValueConstant("CANx1")));
		assertTrue(f1.getTerms().get(1).equals(tfac.getValueConstant("CANy2")));

		head = ((Atom) groundedcq.getBody().get(0)).getTerms();
		assertTrue(head.get(0).equals(tfac.getValueConstant("CANx1")));
		assertTrue(head.get(1).equals(tfac.getValueConstant("CANy2")));

		head = ((Atom) groundedcq.getBody().get(1)).getTerms();
		assertTrue(head.get(0).equals(tfac.getValueConstant("m")));
		f1 = (FunctionalTermImpl) head.get(1);
		assertTrue(f1.getTerms().get(0).equals(tfac.getValueConstant("CANx1")));
		assertTrue(head.get(2).equals(tfac.getValueConstant("CANy2")));
	}

	public void testContainment1() {

		// Query 1 - q(x,y) :- R(x,y), R(y,z)

		List<Term> headTerms = new LinkedList<Term>();
		headTerms.add(x);
		headTerms.add(y);

		Atom head = tfac.getAtom(pfac.getPredicate(URI.create("q"), 2), headTerms);

		List<Atom> body = new LinkedList<Atom>();

		List<Term> terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("x"));
		terms.add(tfac.getVariable("y"));
		body.add(tfac.getAtom(pfac.getPredicate(URI.create("R"), 2), terms));

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("y"));
		terms.add(tfac.getVariable("z"));
		body.add(tfac.getAtom(pfac.getPredicate(URI.create("R"), 2), terms));

		CQIE q1 = tfac.getCQIE(head, body);

		// Query 2 - q(y,y) :- R(y,y)

		headTerms = new LinkedList<Term>();
		headTerms.add(tfac.getVariable("y"));
		headTerms.add(tfac.getVariable("y"));

		head = tfac.getAtom(pfac.getPredicate(URI.create("q"), 2), headTerms);

		body = new LinkedList<Atom>();

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("y"));
		terms.add(tfac.getVariable("y"));
		body.add(tfac.getAtom(pfac.getPredicate(URI.create("R"), 2), terms));

		CQIE q2 = tfac.getCQIE(head, body);

		// Query 3 - q(m,n) :- R(m,n)

		headTerms = new LinkedList<Term>();
		headTerms.add(tfac.getVariable("m"));
		headTerms.add(tfac.getVariable("n"));

		head = tfac.getAtom(pfac.getPredicate(URI.create("q"), 2), headTerms);

		body = new LinkedList<Atom>();

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("m"));
		terms.add(tfac.getVariable("n"));
		body.add(tfac.getAtom(pfac.getPredicate(URI.create("R"), 2), terms));

		CQIE q3 = tfac.getCQIE(head, body);

		// Query 4 - q(m,n) :- S(m,n) R(m,n)

		headTerms = new LinkedList<Term>();
		headTerms.add(tfac.getVariable("m"));
		headTerms.add(tfac.getVariable("n"));

		head = tfac.getAtom(pfac.getPredicate(URI.create("q"), 2), headTerms);

		body = new LinkedList<Atom>();

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("m"));
		terms.add(tfac.getVariable("n"));
		body.add(tfac.getAtom(pfac.getPredicate(URI.create("S"), 2), terms));

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("m"));
		terms.add(tfac.getVariable("n"));
		body.add(tfac.getAtom(pfac.getPredicate(URI.create("R"), 2), terms));

		CQIE q4 = tfac.getCQIE(head, body);

		// Query 5 - q() :- S(x,y)

		head = pfac.getAtom(pfac.getPredicate(URI.create("q"), 0), new LinkedList<Term>());
		body = new LinkedList<Atom>();
		body.add(pfac.getAtom(pfac.getPredicate(URI.create("S"), 2), pfac.getVariable("x"), pfac.getVariable("y")));

		CQIE q5 = pfac.getCQIE(head, body);

		// Query 6 - q() :- S(_,_))

		head = pfac.getAtom(pfac.getPredicate(URI.create("q"), 0), new LinkedList<Term>());
		body = new LinkedList<Atom>();
		body.add(pfac.getAtom(pfac.getPredicate(URI.create("S"), 2), pfac.getNondistinguishedVariable(), pfac.getNondistinguishedVariable()));

		CQIE q6 = pfac.getCQIE(head, body);

		// Query 7 - q(x,y) :- R(x,y), P(x,_)

		head = pfac.getAtom(pfac.getPredicate(URI.create("q"), 2), pfac.getVariable("x"), pfac.getVariable("y"));
		body = new LinkedList<Atom>();
		body.add(pfac.getAtom(pfac.getPredicate(URI.create("R"), 2), pfac.getVariable("x"), pfac.getVariable("y")));
		body.add(pfac.getAtom(pfac.getPredicate(URI.create("P"), 2), pfac.getVariable("y"), pfac.getNondistinguishedVariable()));

		CQIE q7 = pfac.getCQIE(head, body);

		// Query 8 - q(x,y) :- R(x,y), P(_,_)

		head = pfac.getAtom(pfac.getPredicate(URI.create("q"), 2), pfac.getVariable("x"), pfac.getVariable("y"));
		body = new LinkedList<Atom>();
		body.add(pfac.getAtom(pfac.getPredicate(URI.create("R"), 2), pfac.getVariable("x"), pfac.getVariable("y")));
		body.add(pfac.getAtom(pfac.getPredicate(URI.create("P"), 2), pfac.getNondistinguishedVariable(), pfac.getNondistinguishedVariable()));

		CQIE q8 = pfac.getCQIE(head, body);

		// Checking containment 5 in 6 and viceversa

		CQCUtilities cqcu = new CQCUtilities(q6);
		assertTrue(cqcu.isContainedIn(q5));

		cqcu = new CQCUtilities(q5);
		assertTrue(cqcu.isContainedIn(q6));

		// checking containment of 7 in 8
		cqcu = new CQCUtilities(q7);
		assertTrue(cqcu.isContainedIn(q8));

		// checking non-containment of 8 in 7
		cqcu = new CQCUtilities(q8);
		assertFalse(cqcu.isContainedIn(q7));

		// Checking contaiment q2 <= q1

		cqcu = new CQCUtilities(q2);
		assertTrue(cqcu.isContainedIn(q1));

		// Checking contaiment q1 <= q2

		cqcu = new CQCUtilities(q1);
		assertFalse(cqcu.isContainedIn(q2));

		// Checking contaiment q1 <= q3

		cqcu = new CQCUtilities(q1);
		assertTrue(cqcu.isContainedIn(q3));

		// Checking contaiment q3 <= q1

		cqcu = new CQCUtilities(q3);
		assertFalse(cqcu.isContainedIn(q1));

		// Checking contaiment q1 <= q4

		cqcu = new CQCUtilities(q1);
		assertFalse(cqcu.isContainedIn(q4));

		// Checking contaiment q4 <= q1

		cqcu = new CQCUtilities(q4);
		assertFalse(cqcu.isContainedIn(q1));
	}

	public void testSyntacticContainmentCheck() {
		// Query 1 - q(x) :- R(x,y), R(y,z), A(x)
		// Query 2 - q(x) :- R(x,y)
		// Query 3 - q(x) :- A(x)

		List<Term> headTerms = new LinkedList<Term>();
		headTerms.add(x);

		Atom head = tfac.getAtom(pfac.getPredicate(URI.create("q"), 1), headTerms);

		List<Atom> body = new LinkedList<Atom>();

		List<Term> terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("x"));
		terms.add(tfac.getVariable("y"));
		body.add(tfac.getAtom(pfac.getPredicate(URI.create("R"), 2), terms));

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("y"));
		terms.add(tfac.getVariable("z"));
		body.add(tfac.getAtom(pfac.getPredicate(URI.create("R"), 2), terms));

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("x"));
		body.add(tfac.getAtom(pfac.getPredicate(URI.create("A"), 1), terms));

		CQIE q1 = tfac.getCQIE(head, body);

		// Query 2 - q(x) :- R(x,y)

		headTerms = new LinkedList<Term>();
		headTerms.add(tfac.getVariable("x"));

		head = tfac.getAtom(pfac.getPredicate(URI.create("q"), 1), headTerms);

		body = new LinkedList<Atom>();

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("x"));
		terms.add(tfac.getVariable("y"));
		body.add(tfac.getAtom(pfac.getPredicate(URI.create("R"), 2), terms));

		CQIE q2 = tfac.getCQIE(head, body);

		// Query 3 - q(x) :- A(x)

		headTerms = new LinkedList<Term>();
		headTerms.add(tfac.getVariable("x"));

		head = tfac.getAtom(pfac.getPredicate(URI.create("q"), 1), headTerms);

		body = new LinkedList<Atom>();

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("x"));
		body.add(tfac.getAtom(pfac.getPredicate(URI.create("A"), 1), terms));

		CQIE q3 = tfac.getCQIE(head, body);

		assertTrue(CQCUtilities.isContainedInSyntactic(q1, q2));

		assertTrue(CQCUtilities.isContainedInSyntactic(q1, q3));

		assertFalse(CQCUtilities.isContainedInSyntactic(q2, q1));

		assertFalse(CQCUtilities.isContainedInSyntactic(q3, q1));

	}

	public void testRemovalOfSyntacticContainmentCheck() {
		/*
		 * Putting all queries in a list, in the end, query 1 must be removed
		 */

		// Query 1 - q(x) :- R(x,y), R(y,z), A(x)
		// Query 2 - q(x) :- R(x,y)
		// Query 3 - q(x) :- A(x)

		List<Term> headTerms = new LinkedList<Term>();
		headTerms.add(x);

		Atom head = tfac.getAtom(pfac.getPredicate(URI.create("q"), 1), headTerms);

		List<Atom> body = new LinkedList<Atom>();

		List<Term> terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("x"));
		terms.add(tfac.getVariable("y"));
		body.add(tfac.getAtom(pfac.getPredicate(URI.create("R"), 2), terms));

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("y"));
		terms.add(tfac.getVariable("z"));
		body.add(tfac.getAtom(pfac.getPredicate(URI.create("R"), 2), terms));

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("x"));
		body.add(tfac.getAtom(pfac.getPredicate(URI.create("A"), 1), terms));

		CQIE q1 = tfac.getCQIE(head, body);

		// Query 2 - q(x) :- R(x,y)

		headTerms = new LinkedList<Term>();
		headTerms.add(tfac.getVariable("x"));

		head = tfac.getAtom(pfac.getPredicate(URI.create("q"), 1), headTerms);

		body = new LinkedList<Atom>();

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("x"));
		terms.add(tfac.getVariable("y"));
		body.add(tfac.getAtom(pfac.getPredicate(URI.create("R"), 2), terms));

		CQIE q2 = tfac.getCQIE(head, body);

		// Query 3 - q(x) :- A(x)

		headTerms = new LinkedList<Term>();
		headTerms.add(tfac.getVariable("x"));

		head = tfac.getAtom(pfac.getPredicate(URI.create("q"), 1), headTerms);

		body = new LinkedList<Atom>();

		terms = new LinkedList<Term>();
		terms.add(tfac.getVariable("x"));
		body.add(tfac.getAtom(pfac.getPredicate(URI.create("A"), 1), terms));

		CQIE q3 = tfac.getCQIE(head, body);

		LinkedList<CQIE> queries = new LinkedList<CQIE>();
		queries.add(q1);
		queries.add(q2);
		CQCUtilities.removeContainedQueriesSyntacticSorter(queries, true);

		assertTrue(queries.size() == 1);
		assertTrue(queries.contains(q2));

		queries = new LinkedList<CQIE>();
		queries.add(q1);
		queries.add(q3);
		CQCUtilities.removeContainedQueriesSyntacticSorter(queries, true);

		assertTrue(queries.size() == 1);
		assertTrue(queries.contains(q3));

		queries = new LinkedList<CQIE>();
		queries.add(q2);
		queries.add(q3);
		CQCUtilities.removeContainedQueriesSyntacticSorter(queries, true);

		assertTrue(queries.size() == 2);
		assertTrue(queries.contains(q2));
		assertTrue(queries.contains(q3));

		queries = new LinkedList<CQIE>();
		queries.add(q1);
		queries.add(q2);
		queries.add(q3);
		CQCUtilities.removeContainedQueriesSyntacticSorter(queries, true);

		assertTrue(queries.size() == 2);
		assertTrue(queries.contains(q2));
		assertTrue(queries.contains(q3));
	}

	public void testSemanticContainment() {
		BasicDescriptionFactory dfac = new BasicDescriptionFactory();

		/* we allways assert true = isContainedIn(q1, q2) */

		{
			// q(x) :- A(x), q(y) :- C(y), with A ISA C
			DLLiterOntologyImpl sigma = new DLLiterOntologyImpl(URI.create("test"));
			ConceptDescription left = dfac.getAtomicConceptDescription(tfac.getPredicate(URI.create("A"), 1));
			ConceptDescription right = dfac.getAtomicConceptDescription(tfac.getPredicate(URI.create("C"), 1));
			DLLiterConceptInclusionImpl inclusion = new DLLiterConceptInclusionImpl(left, right);
			sigma.addAssertion(inclusion);

			Atom head1 = tfac.getAtom(tfac.getPredicate(URI.create("q"), 1), tfac.getVariable("x"));
			Atom body1 = tfac.getAtom(tfac.getPredicate(URI.create("A"), 1), tfac.getVariable("x"));
			CQIE query1 = tfac.getCQIE(head1, body1);

			Atom head2 = tfac.getAtom(tfac.getPredicate(URI.create("q"), 1), tfac.getVariable("y"));
			Atom body2 = tfac.getAtom(tfac.getPredicate(URI.create("C"), 1), tfac.getVariable("y"));
			CQIE query2 = tfac.getCQIE(head2, body2);

			CQCUtilities cqcutil1 = new CQCUtilities(query1, sigma);
			assertTrue(cqcutil1.isContainedIn(query2));

			CQCUtilities cqcutil2 = new CQCUtilities(query2, sigma);
			assertFalse(cqcutil2.isContainedIn(query1));
		}

		{
			// q(x) :- A(x), q(y) :- R(y,z), with A ISA exists R
			DLLiterOntologyImpl sigma = new DLLiterOntologyImpl(URI.create("test"));
			ConceptDescription left = dfac.getAtomicConceptDescription(tfac.getPredicate(URI.create("A"), 1));
			ConceptDescription right = dfac.getExistentialConceptDescription(tfac.getPredicate(URI.create("R"), 2), false);
			DLLiterConceptInclusionImpl inclusion = new DLLiterConceptInclusionImpl(left, right);
			sigma.addAssertion(inclusion);

			Atom head1 = tfac.getAtom(tfac.getPredicate(URI.create("q"), 1), tfac.getVariable("x"));
			Atom body1 = tfac.getAtom(tfac.getPredicate(URI.create("A"), 1), tfac.getVariable("x"));
			CQIE query1 = tfac.getCQIE(head1, body1);

			Atom head2 = tfac.getAtom(tfac.getPredicate(URI.create("q"), 1), tfac.getVariable("y"));
			Atom body2 = tfac.getAtom(tfac.getPredicate(URI.create("R"), 2), tfac.getVariable("y"), tfac.getVariable("z"));
			CQIE query2 = tfac.getCQIE(head2, body2);

			CQCUtilities cqcutil1 = new CQCUtilities(query1, sigma);
			assertTrue(cqcutil1.isContainedIn(query2));

			CQCUtilities cqcutil2 = new CQCUtilities(query2, sigma);
			assertFalse(cqcutil2.isContainedIn(query1));
		}

		{
			// q(x) :- A(x), q(y) :- R(z,y), with A ISA exists inv(R)
			DLLiterOntologyImpl sigma = new DLLiterOntologyImpl(URI.create("test"));
			ConceptDescription left = dfac.getAtomicConceptDescription(tfac.getPredicate(URI.create("A"), 1));
			ConceptDescription right = dfac.getExistentialConceptDescription(tfac.getPredicate(URI.create("R"), 2), true);
			DLLiterConceptInclusionImpl inclusion = new DLLiterConceptInclusionImpl(left, right);
			sigma.addAssertion(inclusion);

			Atom head1 = tfac.getAtom(tfac.getPredicate(URI.create("q"), 1), tfac.getVariable("x"));
			Atom body1 = tfac.getAtom(tfac.getPredicate(URI.create("A"), 1), tfac.getVariable("x"));
			CQIE query1 = tfac.getCQIE(head1, body1);

			Atom head2 = tfac.getAtom(tfac.getPredicate(URI.create("q"), 1), tfac.getVariable("y"));
			Atom body2 = tfac.getAtom(tfac.getPredicate(URI.create("R"), 2), tfac.getVariable("z"), tfac.getVariable("y"));
			CQIE query2 = tfac.getCQIE(head2, body2);

			CQCUtilities cqcutil1 = new CQCUtilities(query1, sigma);
			assertTrue(cqcutil1.isContainedIn(query2));

			CQCUtilities cqcutil2 = new CQCUtilities(query2, sigma);
			assertFalse(cqcutil2.isContainedIn(query1));
		}

		{
			// q(x) :- R(x,y), q(z) :- A(z), with exists R ISA A
			DLLiterOntologyImpl sigma = new DLLiterOntologyImpl(URI.create("test"));
			ConceptDescription left = dfac.getExistentialConceptDescription(tfac.getPredicate(URI.create("R"), 2), false);
			ConceptDescription right = dfac.getAtomicConceptDescription(tfac.getPredicate(URI.create("A"), 1));

			DLLiterConceptInclusionImpl inclusion = new DLLiterConceptInclusionImpl(left, right);
			sigma.addAssertion(inclusion);

			Atom head1 = tfac.getAtom(tfac.getPredicate(URI.create("q"), 1), tfac.getVariable("x"));
			Atom body1 = tfac.getAtom(tfac.getPredicate(URI.create("R"), 2), tfac.getVariable("x"), tfac.getVariable("y"));
			CQIE query1 = tfac.getCQIE(head1, body1);

			Atom head2 = tfac.getAtom(tfac.getPredicate(URI.create("q"), 1), tfac.getVariable("z"));
			Atom body2 = tfac.getAtom(tfac.getPredicate(URI.create("A"), 1), tfac.getVariable("z"));
			CQIE query2 = tfac.getCQIE(head2, body2);

			CQCUtilities cqcutil1 = new CQCUtilities(query1, sigma);
			assertTrue(cqcutil1.isContainedIn(query2));

			CQCUtilities cqcutil2 = new CQCUtilities(query2, sigma);
			assertFalse(cqcutil2.isContainedIn(query1));
		}

		{
			// q(y) :- R(x,y), q(z) :- A(z), with exists inv(R) ISA A
			DLLiterOntologyImpl sigma = new DLLiterOntologyImpl(URI.create("test"));
			ConceptDescription left = dfac.getExistentialConceptDescription(tfac.getPredicate(URI.create("R"), 2), true);
			ConceptDescription right = dfac.getAtomicConceptDescription(tfac.getPredicate(URI.create("A"), 1));

			DLLiterConceptInclusionImpl inclusion = new DLLiterConceptInclusionImpl(left, right);
			sigma.addAssertion(inclusion);

			Atom head1 = tfac.getAtom(tfac.getPredicate(URI.create("q"), 1), tfac.getVariable("y"));
			Atom body1 = tfac.getAtom(tfac.getPredicate(URI.create("R"), 2), tfac.getVariable("x"), tfac.getVariable("y"));
			CQIE query1 = tfac.getCQIE(head1, body1);

			Atom head2 = tfac.getAtom(tfac.getPredicate(URI.create("q"), 1), tfac.getVariable("z"));
			Atom body2 = tfac.getAtom(tfac.getPredicate(URI.create("A"), 1), tfac.getVariable("z"));
			CQIE query2 = tfac.getCQIE(head2, body2);

			CQCUtilities cqcutil1 = new CQCUtilities(query1, sigma);
			assertTrue(cqcutil1.isContainedIn(query2));

			CQCUtilities cqcutil2 = new CQCUtilities(query2, sigma);
			assertFalse(cqcutil2.isContainedIn(query1));
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
}
