/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.AnonymousVariable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.DLRPerfectReformulator;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryRewriter;

import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;

public class UnificationTest2 extends TestCase {

	/**
	 * Test method for
	 * {@link DLRPerfectReformulator#rewrite(org.obda.query.domain.Query)}
	 * .
	 * 
	 * Check if MGU generation/application works properly with multiple atoms
	 * sharing variables
	 * 
	 * q(x,y) :- R(x,#) R(#,y), S(x,#)
	 * 
	 * @throws Exception
	 */

	public void test_1() throws Exception {

		OBDADataFactory factory = OBDADataFactoryImpl.getInstance();
		OBDADataFactory predFac = OBDADataFactoryImpl.getInstance();
		OBDADataFactory tfac = OBDADataFactoryImpl.getInstance();

		Term t1 = factory.getVariable("x");
		Term t2 = factory.getVariable("y");
		Term t3 = factory.getVariable("x");

		Predicate r1 = predFac.getPredicate("R", 2);
		Predicate r2 = predFac.getPredicate("R", 2);
		Predicate s = predFac.getPredicate("S", 2);
		Predicate p = predFac.getPredicate("p", 2);

		List<Term> terms1 = new Vector<Term>();
		terms1.add(t1);
		terms1.add(factory.getVariableNondistinguished());
		List<Term> terms2 = new Vector<Term>();
		terms2.add(factory.getVariableNondistinguished());
		terms2.add(t2);
		List<Term> terms3 = new Vector<Term>();
		terms3.add(t3);
		terms3.add(factory.getVariableNondistinguished());
		List<Term> terms4 = new Vector<Term>();
		terms4.add(t3.clone());
		terms4.add(t2.clone());

		Function a1 = tfac.getFunction(r1, terms1);
		Function a2 = tfac.getFunction(r2, terms2);
		Function a3 = tfac.getFunction(s, terms3);
		Function head = tfac.getFunction(p, terms4);

		List<Function> body = new Vector<Function>();
		body.add(a1);
		body.add(a2);
		body.add(a3);
		CQIE query = tfac.getCQIE(head, body);
		DatalogProgram prog = tfac.getDatalogProgram();
		prog.appendRule(query);

		// List<Assertion> list = new Vector<Assertion>();
		QueryRewriter rew = new DLRPerfectReformulator();
		DatalogProgram aux = (DatalogProgram) rew.rewrite(prog);

		assertEquals(2, aux.getRules().size());
		// note: aux.getRules().get(0) should be the original one
		CQIE cq = aux.getRules().get(1);
		List<Function> newbody = cq.getBody();

		assertEquals(2, newbody.size());
		Function at1 = newbody.get(0);
		Function at2 = newbody.get(1);

		Term term1 = ((Function) at1).getTerms().get(0);
		Term term2 = ((Function) at1).getTerms().get(1);
		Term term3 = ((Function) at2).getTerms().get(0);
		Term term4 = ((Function) at2).getTerms().get(1);

		assertEquals("x", ((Variable) term1).getName());
		assertEquals("y", ((Variable) term2).getName());
		assertEquals("x", ((Variable) term3).getName());
		assertTrue(term4 instanceof AnonymousVariable);

	}

}
