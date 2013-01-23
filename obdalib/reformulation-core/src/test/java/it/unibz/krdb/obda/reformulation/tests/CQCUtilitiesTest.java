package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.SubClassAxiomImpl;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQCUtilities;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.PositiveInclusionApplicator;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

public class CQCUtilitiesTest extends TestCase {

	CQIE initialquery1 = null;

	PositiveInclusionApplicator piapplicator = new PositiveInclusionApplicator();

	OBDADataFactory pfac = OBDADataFactoryImpl.getInstance();
	OBDADataFactory tfac = OBDADataFactoryImpl.getInstance();

	Predicate r = pfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT });
	Predicate s = pfac.getPredicate(OBDADataFactoryImpl.getIRI("S"), 3, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT, COL_TYPE.OBJECT });
	Predicate q = pfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 5, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT, COL_TYPE.OBJECT,
			COL_TYPE.OBJECT, COL_TYPE.OBJECT });

	NewLiteral x = tfac.getVariable("x");
	NewLiteral y = tfac.getVariable("y");
	NewLiteral c1 = tfac.getURIConstant(OBDADataFactoryImpl.getIRI("URI1"));
	NewLiteral c2 = tfac.getValueConstant("m");

	NewLiteral u1 = tfac.getNondistinguishedVariable();
	NewLiteral u2 = tfac.getNondistinguishedVariable();

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
		List<NewLiteral> headTerms = new LinkedList<NewLiteral>();
		headTerms.add(x);
		headTerms.add(c1);
		headTerms.add(c2);
		headTerms.add(y);
		List<NewLiteral> fterms1 = new LinkedList<NewLiteral>();
		fterms1.add(x);
		fterms1.add(y);
		headTerms.add(tfac.getFunctionalTerm(pfac.getPredicate(OBDADataFactoryImpl.getIRI("f"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }),
				fterms1));

		Atom head = tfac.getAtom(q, headTerms);

		List<Atom> body = new LinkedList<Atom>();

		List<NewLiteral> atomTerms1 = new LinkedList<NewLiteral>();
		atomTerms1.add(x);
		atomTerms1.add(y);
		body.add(tfac.getAtom(r, atomTerms1));

		List<NewLiteral> atomTerms2 = new LinkedList<NewLiteral>();
		atomTerms2.add(c2);
		List<NewLiteral> fterms2 = new LinkedList<NewLiteral>();
		fterms2.add(x);
		atomTerms2.add(tfac.getFunctionalTerm(pfac.getPredicate(OBDADataFactoryImpl.getIRI("f"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), fterms2));
		atomTerms2.add(y);
		body.add(tfac.getAtom(s, atomTerms2));

		initialquery1 = tfac.getCQIE(head, body);
	}

	public void testGrounding() {
		Ontology sigma = null;
		CQCUtilities cqcutil = new CQCUtilities(initialquery1, sigma);
		CQIE groundedcq = cqcutil.getCanonicalQuery(initialquery1);

		List<NewLiteral> head = groundedcq.getHead().getTerms();
		assertTrue(head.get(0).equals(tfac.getValueConstant("CANx1")));
		assertTrue(head.get(1).equals(tfac.getURIConstant(OBDADataFactoryImpl.getIRI("URI1"))));
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

		List<NewLiteral> headTerms = new LinkedList<NewLiteral>();
		headTerms.add(x);
		headTerms.add(y);

		Atom head = tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }), headTerms);

		List<Atom> body = new LinkedList<Atom>();

		List<NewLiteral> terms = new LinkedList<NewLiteral>();
		terms.add(tfac.getVariable("x"));
		terms.add(tfac.getVariable("y"));
		body.add(tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }), terms));

		terms = new LinkedList<NewLiteral>();
		terms.add(tfac.getVariable("y"));
		terms.add(tfac.getVariable("z"));
		body.add(tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }), terms));

		CQIE q1 = tfac.getCQIE(head, body);

		// Query 2 - q(y,y) :- R(y,y)

		headTerms = new LinkedList<NewLiteral>();
		headTerms.add(tfac.getVariable("y"));
		headTerms.add(tfac.getVariable("y"));

		head = tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }), headTerms);

		body = new LinkedList<Atom>();

		terms = new LinkedList<NewLiteral>();
		terms.add(tfac.getVariable("y"));
		terms.add(tfac.getVariable("y"));
		body.add(tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }), terms));

		CQIE q2 = tfac.getCQIE(head, body);

		// Query 3 - q(m,n) :- R(m,n)

		headTerms = new LinkedList<NewLiteral>();
		headTerms.add(tfac.getVariable("m"));
		headTerms.add(tfac.getVariable("n"));

		head = tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }), headTerms);

		body = new LinkedList<Atom>();

		terms = new LinkedList<NewLiteral>();
		terms.add(tfac.getVariable("m"));
		terms.add(tfac.getVariable("n"));
		body.add(tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }), terms));

		CQIE q3 = tfac.getCQIE(head, body);

		// Query 4 - q(m,n) :- S(m,n) R(m,n)

		headTerms = new LinkedList<NewLiteral>();
		headTerms.add(tfac.getVariable("m"));
		headTerms.add(tfac.getVariable("n"));

		head = tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }), headTerms);

		body = new LinkedList<Atom>();

		terms = new LinkedList<NewLiteral>();
		terms.add(tfac.getVariable("m"));
		terms.add(tfac.getVariable("n"));
		body.add(tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("S"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }), terms));

		terms = new LinkedList<NewLiteral>();
		terms.add(tfac.getVariable("m"));
		terms.add(tfac.getVariable("n"));
		body.add(tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }), terms));

		CQIE q4 = tfac.getCQIE(head, body);

		// Query 5 - q() :- S(x,y)

		head = pfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 0, null), new LinkedList<NewLiteral>());
		body = new LinkedList<Atom>();
		body.add(pfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("S"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }),
				pfac.getVariable("x"), pfac.getVariable("y")));

		CQIE q5 = pfac.getCQIE(head, body);

		// Query 6 - q() :- S(_,_))

		head = pfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 0, null), new LinkedList<NewLiteral>());
		body = new LinkedList<Atom>();
		body.add(pfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("S"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }),
				pfac.getNondistinguishedVariable(), pfac.getNondistinguishedVariable()));

		CQIE q6 = pfac.getCQIE(head, body);

		// Query 7 - q(x,y) :- R(x,y), P(y,_)

		head = pfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }),
				pfac.getVariable("x"), pfac.getVariable("y"));
		body = new LinkedList<Atom>();
		body.add(pfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }),
				pfac.getVariable("x"), pfac.getVariable("y")));
		body.add(pfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("P"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }),
				pfac.getVariable("y"), pfac.getNondistinguishedVariable()));

		CQIE q7 = pfac.getCQIE(head, body);

		// Query 8 - q(x,y) :- R(x,y), P(_,_)

		head = pfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }),
				pfac.getVariable("x"), pfac.getVariable("y"));
		body = new LinkedList<Atom>();
		body.add(pfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }),
				pfac.getVariable("x"), pfac.getVariable("y")));
		body.add(pfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("P"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }),
				pfac.getNondistinguishedVariable(), pfac.getNondistinguishedVariable()));

		CQIE q8 = pfac.getCQIE(head, body);

		// Query 9 - q() :- R(x,m), R(x,y), S(m,n), S(y,z),T(n,o),T(z,x)

		head = pfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 0, null), new LinkedList<NewLiteral>());
		body = new LinkedList<Atom>();
		body.add(pfac.getAtom(pfac.getObjectPropertyPredicate("R"), pfac.getVariable("x"), pfac.getVariable("m")));
		body.add(pfac.getAtom(pfac.getObjectPropertyPredicate("R"), pfac.getVariable("x"), pfac.getVariable("y")));
		body.add(pfac.getAtom(pfac.getObjectPropertyPredicate("S"), pfac.getVariable("m"), pfac.getVariable("n")));
		body.add(pfac.getAtom(pfac.getObjectPropertyPredicate("S"), pfac.getVariable("y"), pfac.getVariable("z")));
		body.add(pfac.getAtom(pfac.getObjectPropertyPredicate("T"), pfac.getVariable("n"), pfac.getVariable("o")));
		body.add(pfac.getAtom(pfac.getObjectPropertyPredicate("T"), pfac.getVariable("z"), pfac.getVariable("x")));

		CQIE q9 = pfac.getCQIE(head, body);

		// Query 10 - q() :- R(i,j), S(j,k), T(k,i)

		head = pfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 0, null), new LinkedList<NewLiteral>());
		body = new LinkedList<Atom>();
		body.add(pfac.getAtom(pfac.getObjectPropertyPredicate("R"), pfac.getVariable("i"), pfac.getVariable("j")));
		body.add(pfac.getAtom(pfac.getObjectPropertyPredicate("S"), pfac.getVariable("j"), pfac.getVariable("k")));
		body.add(pfac.getAtom(pfac.getObjectPropertyPredicate("T"), pfac.getVariable("k"), pfac.getVariable("i")));

		CQIE q10 = pfac.getCQIE(head, body);
		Ontology sigma = null;

		// Checking containment 5 in 6 and viceversa

		CQCUtilities cqcu = new CQCUtilities(q6, sigma);
		assertTrue(cqcu.isContainedIn(q5));

		cqcu = new CQCUtilities(q5, sigma);
		assertTrue(cqcu.isContainedIn(q6));

		// checking containment of 7 in 8
		cqcu = new CQCUtilities(q7, sigma);
		assertTrue(cqcu.isContainedIn(q8));

		// checking non-containment of 8 in 7
		cqcu = new CQCUtilities(q8, sigma);
		assertFalse(cqcu.isContainedIn(q7));

		// Checking contaiment q2 <= q1

		cqcu = new CQCUtilities(q2, sigma);
		assertTrue(cqcu.isContainedIn(q1));

		// Checking contaiment q1 <= q2

		cqcu = new CQCUtilities(q1, sigma);
		assertFalse(cqcu.isContainedIn(q2));

		// Checking contaiment q1 <= q3

		cqcu = new CQCUtilities(q1, sigma);
		assertTrue(cqcu.isContainedIn(q3));

		// Checking contaiment q3 <= q1

		cqcu = new CQCUtilities(q3, sigma);
		assertFalse(cqcu.isContainedIn(q1));

		// Checking contaiment q1 <= q4

		cqcu = new CQCUtilities(q1, sigma);
		assertFalse(cqcu.isContainedIn(q4));

		// Checking contaiment q4 <= q1

		cqcu = new CQCUtilities(q4, sigma);
		assertFalse(cqcu.isContainedIn(q1));
		
		
		// Checking containment q9 <= q10 true
		cqcu = new CQCUtilities(q9, sigma);
		assertTrue(cqcu.isContainedIn(q10));
		
		// Checking containment q10 <= q9 true
		cqcu = new CQCUtilities(q10, sigma);
		assertTrue(cqcu.isContainedIn(q9));
	}

	public void testSyntacticContainmentCheck() {
		// Query 1 - q(x) :- R(x,y), R(y,z), A(x)
		// Query 2 - q(x) :- R(x,y)
		// Query 3 - q(x) :- A(x)

		List<NewLiteral> headTerms = new LinkedList<NewLiteral>();
		headTerms.add(x);

		Atom head = tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), headTerms);

		List<Atom> body = new LinkedList<Atom>();

		List<NewLiteral> terms = new LinkedList<NewLiteral>();
		terms.add(tfac.getVariable("x"));
		terms.add(tfac.getVariable("y"));
		body.add(tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }), terms));

		terms = new LinkedList<NewLiteral>();
		terms.add(tfac.getVariable("y"));
		terms.add(tfac.getVariable("z"));
		body.add(tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }), terms));

		terms = new LinkedList<NewLiteral>();
		terms.add(tfac.getVariable("x"));
		body.add(tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("A"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), terms));

		CQIE q1 = tfac.getCQIE(head, body);

		// Query 2 - q(x) :- R(x,y)

		headTerms = new LinkedList<NewLiteral>();
		headTerms.add(tfac.getVariable("x"));

		head = tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), headTerms);

		body = new LinkedList<Atom>();

		terms = new LinkedList<NewLiteral>();
		terms.add(tfac.getVariable("x"));
		terms.add(tfac.getVariable("y"));
		body.add(tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }), terms));

		CQIE q2 = tfac.getCQIE(head, body);

		// Query 3 - q(x) :- A(x)

		headTerms = new LinkedList<NewLiteral>();
		headTerms.add(tfac.getVariable("x"));

		head = tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), headTerms);

		body = new LinkedList<Atom>();

		terms = new LinkedList<NewLiteral>();
		terms.add(tfac.getVariable("x"));
		body.add(tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("A"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), terms));

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

		List<NewLiteral> headTerms = new LinkedList<NewLiteral>();
		headTerms.add(x);

		Atom head = tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), headTerms);

		List<Atom> body = new LinkedList<Atom>();

		List<NewLiteral> terms = new LinkedList<NewLiteral>();
		terms.add(tfac.getVariable("x"));
		terms.add(tfac.getVariable("y"));
		body.add(tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }), terms));

		terms = new LinkedList<NewLiteral>();
		terms.add(tfac.getVariable("y"));
		terms.add(tfac.getVariable("z"));
		body.add(tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }), terms));

		terms = new LinkedList<NewLiteral>();
		terms.add(tfac.getVariable("x"));
		body.add(tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("A"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), terms));

		CQIE q1 = tfac.getCQIE(head, body);

		// Query 2 - q(x) :- R(x,y)

		headTerms = new LinkedList<NewLiteral>();
		headTerms.add(tfac.getVariable("x"));

		head = tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), headTerms);

		body = new LinkedList<Atom>();

		terms = new LinkedList<NewLiteral>();
		terms.add(tfac.getVariable("x"));
		terms.add(tfac.getVariable("y"));
		body.add(tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }), terms));

		CQIE q2 = tfac.getCQIE(head, body);

		// Query 3 - q(x) :- A(x)

		headTerms = new LinkedList<NewLiteral>();
		headTerms.add(tfac.getVariable("x"));

		head = tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), headTerms);

		body = new LinkedList<Atom>();

		terms = new LinkedList<NewLiteral>();
		terms.add(tfac.getVariable("x"));
		body.add(tfac.getAtom(pfac.getPredicate(OBDADataFactoryImpl.getIRI("A"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), terms));

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
		OntologyFactoryImpl dfac = new OntologyFactoryImpl();

		/* we allways assert true = isContainedIn(q1, q2) */

		{
			// q(x) :- A(x), q(y) :- C(y), with A ISA C
			Ontology sigma = OntologyFactoryImpl.getInstance().createOntology(OBDADataFactoryImpl.getIRI("test"));
			ClassDescription left = dfac.createClass(tfac.getPredicate(OBDADataFactoryImpl.getIRI("A"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }));
			ClassDescription right = dfac.createClass(tfac.getPredicate(OBDADataFactoryImpl.getIRI("C"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }));
			SubClassAxiomImpl inclusion = (SubClassAxiomImpl) OntologyFactoryImpl.getInstance().createSubClassAxiom(left, right);
			sigma.addConcept(tfac.getPredicate(OBDADataFactoryImpl.getIRI("A"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }));
			sigma.addConcept(tfac.getPredicate(OBDADataFactoryImpl.getIRI("C"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }));

			sigma.addAssertion(inclusion);

			Atom head1 = tfac.getAtom(tfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("x"));
			Atom body1 = tfac.getAtom(tfac.getPredicate(OBDADataFactoryImpl.getIRI("A"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("x"));
			CQIE query1 = tfac.getCQIE(head1, body1);

			Atom head2 = tfac.getAtom(tfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("y"));
			Atom body2 = tfac.getAtom(tfac.getPredicate(OBDADataFactoryImpl.getIRI("C"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("y"));
			CQIE query2 = tfac.getCQIE(head2, body2);

			CQCUtilities cqcutil1 = new CQCUtilities(query1, sigma);
			assertTrue(cqcutil1.isContainedIn(query2));

			CQCUtilities cqcutil2 = new CQCUtilities(query2, sigma);
			assertFalse(cqcutil2.isContainedIn(query1));
		}

		{
			// q(x) :- A(x), q(y) :- R(y,z), with A ISA exists R
			Ontology sigma = OntologyFactoryImpl.getInstance().createOntology(OBDADataFactoryImpl.getIRI("test"));
			ClassDescription left = dfac.createClass(tfac.getPredicate(OBDADataFactoryImpl.getIRI("A"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }));
			ClassDescription right = dfac.getPropertySomeRestriction(
					tfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }), false);
			SubClassAxiomImpl inclusion = (SubClassAxiomImpl) OntologyFactoryImpl.getInstance().createSubClassAxiom(left, right);

			sigma.addConcept(tfac.getPredicate(OBDADataFactoryImpl.getIRI("A"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }));
			sigma.addRole(tfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }));

			sigma.addAssertion(inclusion);

			Atom head1 = tfac.getAtom(tfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("x"));
			Atom body1 = tfac.getAtom(tfac.getPredicate(OBDADataFactoryImpl.getIRI("A"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("x"));
			CQIE query1 = tfac.getCQIE(head1, body1);

			Atom head2 = tfac.getAtom(tfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("y"));
			Atom body2 = tfac.getAtom(tfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }),
					tfac.getVariable("y"), tfac.getVariable("z"));
			CQIE query2 = tfac.getCQIE(head2, body2);

			CQCUtilities cqcutil1 = new CQCUtilities(query1, sigma);
			assertTrue(cqcutil1.isContainedIn(query2));

			CQCUtilities cqcutil2 = new CQCUtilities(query2, sigma);
			assertFalse(cqcutil2.isContainedIn(query1));
		}

		{
			// q(x) :- A(x), q(y) :- R(z,y), with A ISA exists inv(R)
			Ontology sigma = OntologyFactoryImpl.getInstance().createOntology(OBDADataFactoryImpl.getIRI("test"));
			ClassDescription left = dfac.createClass(tfac.getPredicate(OBDADataFactoryImpl.getIRI("A"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }));
			ClassDescription right = dfac.getPropertySomeRestriction(
					tfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }), true);
			SubClassAxiomImpl inclusion = (SubClassAxiomImpl) OntologyFactoryImpl.getInstance().createSubClassAxiom(left, right);

			sigma.addConcept(tfac.getPredicate(OBDADataFactoryImpl.getIRI("A"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }));
			sigma.addRole(tfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }));

			sigma.addAssertion(inclusion);

			Atom head1 = tfac.getAtom(tfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("x"));
			Atom body1 = tfac.getAtom(tfac.getPredicate(OBDADataFactoryImpl.getIRI("A"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("x"));
			CQIE query1 = tfac.getCQIE(head1, body1);

			Atom head2 = tfac.getAtom(tfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("y"));
			Atom body2 = tfac.getAtom(tfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }),
					tfac.getVariable("z"), tfac.getVariable("y"));
			CQIE query2 = tfac.getCQIE(head2, body2);

			CQCUtilities cqcutil1 = new CQCUtilities(query1, sigma);
			assertTrue(cqcutil1.isContainedIn(query2));

			CQCUtilities cqcutil2 = new CQCUtilities(query2, sigma);
			assertFalse(cqcutil2.isContainedIn(query1));
		}

		{
			// q(x) :- R(x,y), q(z) :- A(z), with exists R ISA A
			Ontology sigma = OntologyFactoryImpl.getInstance().createOntology(OBDADataFactoryImpl.getIRI("test"));
			ClassDescription left = dfac.getPropertySomeRestriction(
					tfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }), false);
			ClassDescription right = dfac.createClass(tfac.getPredicate(OBDADataFactoryImpl.getIRI("A"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }));

			SubClassAxiomImpl inclusion = (SubClassAxiomImpl) OntologyFactoryImpl.getInstance().createSubClassAxiom(left, right);

			sigma.addConcept(tfac.getPredicate(OBDADataFactoryImpl.getIRI("A"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }));
			sigma.addRole(tfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }));

			sigma.addAssertion(inclusion);

			Atom head1 = tfac.getAtom(tfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("x"));
			Atom body1 = tfac.getAtom(tfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }),
					tfac.getVariable("x"), tfac.getVariable("y"));
			CQIE query1 = tfac.getCQIE(head1, body1);

			Atom head2 = tfac.getAtom(tfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("z"));
			Atom body2 = tfac.getAtom(tfac.getPredicate(OBDADataFactoryImpl.getIRI("A"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("z"));
			CQIE query2 = tfac.getCQIE(head2, body2);

			CQCUtilities cqcutil1 = new CQCUtilities(query1, sigma);
			assertTrue(cqcutil1.isContainedIn(query2));

			CQCUtilities cqcutil2 = new CQCUtilities(query2, sigma);
			assertFalse(cqcutil2.isContainedIn(query1));
		}

		{
			// q(y) :- R(x,y), q(z) :- A(z), with exists inv(R) ISA A
			Ontology sigma = OntologyFactoryImpl.getInstance().createOntology(OBDADataFactoryImpl.getIRI("test"));
			ClassDescription left = dfac.getPropertySomeRestriction(
					tfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }), true);
			ClassDescription right = dfac.createClass(tfac.getPredicate(OBDADataFactoryImpl.getIRI("A"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }));

			SubClassAxiomImpl inclusion = (SubClassAxiomImpl) OntologyFactoryImpl.getInstance().createSubClassAxiom(left, right);

			sigma.addConcept(tfac.getPredicate(OBDADataFactoryImpl.getIRI("A"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }));
			sigma.addRole(tfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }));

			sigma.addAssertion(inclusion);

			Atom head1 = tfac.getAtom(tfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("y"));
			Atom body1 = tfac.getAtom(tfac.getPredicate(OBDADataFactoryImpl.getIRI("R"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT }),
					tfac.getVariable("x"), tfac.getVariable("y"));
			CQIE query1 = tfac.getCQIE(head1, body1);

			Atom head2 = tfac.getAtom(tfac.getPredicate(OBDADataFactoryImpl.getIRI("q"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("z"));
			Atom body2 = tfac.getAtom(tfac.getPredicate(OBDADataFactoryImpl.getIRI("A"), 1, new COL_TYPE[] { COL_TYPE.OBJECT }), tfac.getVariable("z"));
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
