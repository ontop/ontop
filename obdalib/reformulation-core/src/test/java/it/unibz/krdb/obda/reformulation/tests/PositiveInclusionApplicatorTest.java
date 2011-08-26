package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.PositiveInclusionApplicator;

import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class PositiveInclusionApplicatorTest extends TestCase {

	CQIE						initialquery1	= null;

	CQIE						initialquery2	= null;

	CQIE						initialquery3	= null;

	CQIE						initialquery4	= null;

	CQIE						initialquery5	= null;

	PositiveInclusionApplicator	piapplicator	= new PositiveInclusionApplicator();

	OBDADataFactory				pfac			= OBDADataFactoryImpl.getInstance();
	OBDADataFactory				tfac			= OBDADataFactoryImpl.getInstance();

	Predicate					r				= pfac.getPredicate(URI.create("R"), 2);
	Predicate					s				= pfac.getPredicate(URI.create("S"), 2);
	Predicate					q				= pfac.getPredicate(URI.create("q"), 1);

	Term						x				= tfac.getVariable("x");
	Term						y				= tfac.getVariable("y");
	Term						z				= tfac.getVariable("z");
	Term						m				= tfac.getVariable("m");

	Term						u1				= tfac.getNondistinguishedVariable();
	Term						u2				= tfac.getNondistinguishedVariable();

	@Before
	public void setUp() throws Exception {

		// q(y) :- R(x, y), R(x, z), S(y, m), S(z, m),

		List<Term> terms1 = new LinkedList<Term>();
		terms1.add(x);
		terms1.add(y);
		Atom a1 = tfac.getAtom(r, terms1);

		List<Term> terms2 = new LinkedList<Term>();
		terms2.add(x);
		terms2.add(z);
		Atom a2 = tfac.getAtom(r, terms2);

		List<Term> terms3 = new LinkedList<Term>();
		terms3.add(y);
		terms3.add(m);
		Atom a3 = tfac.getAtom(s, terms3);

		List<Term> terms4 = new LinkedList<Term>();
		terms4.add(z);
		terms4.add(m);
		Atom a4 = tfac.getAtom(s, terms4);

		List<Term> termshead = new LinkedList<Term>();
		termshead.add(x);
		Atom head = tfac.getAtom(q, termshead);

		LinkedList<Atom> body = new LinkedList<Atom>();
		body.add(a1);
		body.add(a2);
		body.add(a3);
		body.add(a4);

		initialquery1 = tfac.getCQIE(head, body);

		terms1 = new LinkedList<Term>();
		terms1.add(x);
		terms1.add(u1);
		a1 = tfac.getAtom(r, terms1);

		terms2 = new LinkedList<Term>();
		terms2.add(x);
		terms2.add(u2);
		a2 = tfac.getAtom(r, terms2);

		body = new LinkedList<Atom>();
		body.add(a1);
		body.add(a2);

		termshead = new LinkedList<Term>();
		termshead.add(x);
		head = tfac.getAtom(q, termshead);

		initialquery2 = tfac.getCQIE(head, body);

		terms1 = new LinkedList<Term>();
		terms1.add(u1);
		terms1.add(x);
		a1 = tfac.getAtom(r, terms1);

		terms2 = new LinkedList<Term>();
		terms2.add(u2);
		terms2.add(x);
		a2 = tfac.getAtom(r, terms2);

		body = new LinkedList<Atom>();
		body.add(a1);
		body.add(a2);

		termshead = new LinkedList<Term>();
		termshead.add(x);
		head = tfac.getAtom(q, termshead);

		initialquery3 = tfac.getCQIE(head, body);

		terms1 = new LinkedList<Term>();
		terms1.add(x);
		terms1.add(u1);
		a1 = tfac.getAtom(r, terms1);

		terms2 = new LinkedList<Term>();
		terms2.add(x);
		terms2.add(y);
		a2 = tfac.getAtom(r, terms2);

		body = new LinkedList<Atom>();
		body.add(a1);
		body.add(a2);

		termshead = new LinkedList<Term>();
		termshead.add(x);
		head = tfac.getAtom(q, termshead);

		initialquery4 = tfac.getCQIE(head, body);

		terms1 = new LinkedList<Term>();
		terms1.add(u1);
		terms1.add(x);
		a1 = tfac.getAtom(r, terms1);

		terms2 = new LinkedList<Term>();
		terms2.add(y);
		terms2.add(x);
		a2 = tfac.getAtom(r, terms2);

		body = new LinkedList<Atom>();
		body.add(a1);
		body.add(a2);

		termshead = new LinkedList<Term>();
		termshead.add(x);
		head = tfac.getAtom(q, termshead);

		initialquery5 = tfac.getCQIE(head, body);

	}

	@Test
	public void testApplyExistentialInclusions() {
		// TODO
	}

	@Test
	public void testSaturateByUnification1() throws Exception {
		HashSet<CQIE> initialset = new HashSet<CQIE>();
		initialset.add(initialquery1);
		Set<CQIE> saturatedset = piapplicator.saturateByUnification(initialset, s, false);
		assertTrue(saturatedset.size() == 2);
	}

	/***
	 * In this case, no unification happens because the we are trying to unify
	 * only atoms that share the left term and that have the S predicate.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSaturateByUnification2() throws Exception {
		HashSet<CQIE> initialset = new HashSet<CQIE>();
		initialset.add(initialquery1);
		Set<CQIE> saturatedset = piapplicator.saturateByUnification(initialset, s, true);
		assertTrue(saturatedset.size() == 1);
	}

	/***
	 * Checking that non distinguisehd variables are also unified
	 * 
	 * q(x) :- R(#, x), R(#, x)
	 * 
	 * should produce
	 * 
	 * q(x) :- R(#, x), R(#, x) q(x) :- R(#, x)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSaturateByUnification3() throws Exception {
		HashSet<CQIE> initialset = new HashSet<CQIE>();
		initialset.add(initialquery2);
		Set<CQIE> saturatedset = piapplicator.saturateByUnification(initialset, r, true);
		assertTrue(saturatedset.size() == 2);
	}

	/***
	 * Checking that non distinguisehd variables are also unified
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSaturateByUnification4() throws Exception {
		HashSet<CQIE> initialset = new HashSet<CQIE>();
		initialset.add(initialquery3);
		Set<CQIE> saturatedset = piapplicator.saturateByUnification(initialset, r, false);
		assertTrue(saturatedset.size() == 2);
	}

	/***
	 * Checking that non distinguisehd variables are also unified
	 * 
	 * q(x) :- R(#, x), R(y, x)
	 * 
	 * should produce
	 * 
	 * q(x) :- R(#, x), R(y, x) q(x) :- R(#, x)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSaturateByUnification5() throws Exception {
		HashSet<CQIE> initialset = new HashSet<CQIE>();
		initialset.add(initialquery4);
		Set<CQIE> saturatedset = piapplicator.saturateByUnification(initialset, r, true);
		assertTrue(saturatedset.size() == 2);
	}

	/***
	 * Checking that non distinguisehd variables are also unified
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSaturateByUnification6() throws Exception {
		HashSet<CQIE> initialset = new HashSet<CQIE>();
		initialset.add(initialquery5);
		Set<CQIE> saturatedset = piapplicator.saturateByUnification(initialset, r, false);
		assertTrue(saturatedset.size() == 2);
	}

	@Test
	public void testApplyExistentialInclusion() {
		// TODO
	}

}
