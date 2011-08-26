package it.unibz.krdb.obda.owlrefplatform;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.AnonymousVariable;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.DLRPerfectReformulator;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryRewriter;

import java.net.URI;
import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;

import org.junit.Test;

public class UnificationTest2 extends TestCase {

	/**
	 * Test method for
	 * {@link it.unibz.krdb.obda.owlrefplatform.core.reformulation.DLRPerfectReformulator#rewrite(org.obda.query.domain.Query)}
	 * .
	 *
	 * Check if MGU generation/application works properly with multiple atoms sharing variables
	 *
	 * q(x,y) :- R(x,#) R(#,y), S(x,#)
	 *
	 * @throws Exception
	 */
	@Test
	public void test_1() throws Exception {

		OBDADataFactory factory = OBDADataFactoryImpl.getInstance();
		OBDADataFactory predFac = OBDADataFactoryImpl.getInstance();
		OBDADataFactory tfac = OBDADataFactoryImpl.getInstance();

		Term t1 = factory.getVariable("x");
		Term t2 = factory.getVariable("y");
		Term t3 = factory.getVariable("x");

		Predicate r1 = predFac.getPredicate(URI.create("R"), 2);
		Predicate r2 = predFac.getPredicate(URI.create("R"), 2);
		Predicate s = predFac.getPredicate(URI.create("S"), 2);
		Predicate p = predFac.getPredicate(URI.create("p"), 2);

		List<Term> terms1 = new Vector<Term>();
		terms1.add(t1);
		terms1.add(factory.getNondistinguishedVariable());
		List<Term> terms2 = new Vector<Term>();
		terms2.add(factory.getNondistinguishedVariable());
		terms2.add(t2);
		List<Term> terms3 = new Vector<Term>();
		terms3.add(t3);
		terms3.add(factory.getNondistinguishedVariable());
		List<Term> terms4 = new Vector<Term>();
		terms4.add(t3.clone());
		terms4.add(t2.clone());

		Atom a1 = tfac.getAtom(r1, terms1);
		Atom a2 = tfac.getAtom(r2, terms2);
		Atom a3 = tfac.getAtom(s, terms3);
		Atom head = tfac.getAtom(p, terms4);

		List<Atom> body = new Vector<Atom>();
		body.add(a1);
		body.add(a2);
		body.add(a3);
		CQIE query = tfac.getCQIE(head, body);
		DatalogProgram prog = tfac.getDatalogProgram();
		prog.appendRule(query);

//		List<Assertion> list = new Vector<Assertion>();
		QueryRewriter rew = new DLRPerfectReformulator();
		DatalogProgram aux = (DatalogProgram) rew.rewrite(prog);

		assertEquals(2, aux.getRules().size());
		// note: aux.getRules().get(0) should be the original one
		CQIE cq = aux.getRules().get(1);
		List<Atom> newbody = cq.getBody();

		assertEquals(2, newbody.size());
		Atom at1 = newbody.get(0);
		Atom at2 = newbody.get(1);

		Term term1 = ((Atom) at1).getTerms().get(0);
		Term term2 = ((Atom) at1).getTerms().get(1);
		Term term3 = ((Atom) at2).getTerms().get(0);
		Term term4 = ((Atom) at2).getTerms().get(1);

		assertEquals("x", ((Variable) term1).getName());
		assertEquals("y", ((Variable) term2).getName());
		assertEquals("x", ((Variable) term3).getName());
		assertTrue(term4 instanceof AnonymousVariable);

	}

}
