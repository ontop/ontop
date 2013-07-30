/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.Substitution;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.Unifier;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import junit.framework.TestCase;

import com.hp.hpl.jena.iri.IRIFactory;


public class ThetaGenerationTest extends TestCase {

	OBDADataFactory termFactory =  OBDADataFactoryImpl.getInstance();
	OBDADataFactory tfac =  OBDADataFactoryImpl.getInstance();
	OBDADataFactory predFactory = OBDADataFactoryImpl.getInstance();
	IRIFactory ifac = OBDADataFactoryImpl.getIRIFactory();

	private Vector<Substitution> getMGUAsVector(Map<Variable, NewLiteral> mgu) {
		Vector<Substitution> computedmgu = new Vector<Substitution>();
		if (mgu == null) {
			computedmgu = null;
		} else {
			for (NewLiteral var : mgu.keySet()) {
				computedmgu.add(new Substitution(var, mgu.get(var)));
			}
		}
		return computedmgu;

	}

	//A(x),A(x)
	public void test_1(){

		try {
			NewLiteral t1 = termFactory.getVariable("x");
			NewLiteral t2 = termFactory.getVariable("x");

			Predicate pred1 = predFactory.getPredicate("A", 1);
			List<NewLiteral> terms1 = new Vector<NewLiteral>();
			terms1.add(t1);
			Atom atom1 = tfac.getAtom(pred1, terms1);

			Predicate pred2 = predFactory.getPredicate("A", 1);
			List<NewLiteral> terms2 = new Vector<NewLiteral>();
			terms2.add(t2);
			Atom atom2 = tfac.getAtom(pred2, terms2);

			Unifier unifier = new Unifier();
			Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
			assertEquals(0, s.size());
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}

	}

	//A(x),A(y)
	public void test_2(){

		try {
			NewLiteral t1 = termFactory.getVariable("x");
			NewLiteral t2 = termFactory.getVariable("y");

			Predicate pred1 = predFactory.getPredicate("A", 1);
			List<NewLiteral> terms1 = new Vector<NewLiteral>();
			terms1.add(t1);
			Atom atom1 = tfac.getAtom(pred1, terms1);

			Predicate pred2 = predFactory.getPredicate("A", 1);
			List<NewLiteral> terms2 = new Vector<NewLiteral>();
			terms2.add(t2);
			Atom atom2 = tfac.getAtom(pred2, terms2);

			Unifier unifier = new Unifier();
			Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
			assertEquals(1, s.size());

			Substitution s0 = s.get(0);
			NewLiteral t = s0.getTerm();
			NewLiteral v = s0.getVariable();

			assertEquals("y", ((Variable) t).getName());
			assertEquals("x", ((Variable) v).getName());
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}
	}

	//A(x),A('y')
	public void test_3(){

		
			NewLiteral t1 = termFactory.getVariable("x");
			NewLiteral t2 = termFactory.getValueConstant("y");

			Predicate pred1 = predFactory.getPredicate("A", 1);
			List<NewLiteral> terms1 = new Vector<NewLiteral>();
			terms1.add(t1);
			Atom atom1 = tfac.getAtom(pred1, terms1);

			Predicate pred2 = predFactory.getPredicate("A", 1);
			List<NewLiteral> terms2 = new Vector<NewLiteral>();
			terms2.add(t2);
			Atom atom2 = tfac.getAtom(pred2, terms2);

			Unifier unifier = new Unifier();
			Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
			assertEquals(1, s.size());

			Substitution s0 = s.get(0);
			ValueConstant t = (ValueConstant) s0.getTerm();
			NewLiteral v = s0.getVariable();

			assertEquals("y", t.getValue());
			assertEquals("x", ((Variable) v).getName());
		
	}

		//A(x),A('p(y)')
	public void test_4(){

//		try {
//			Term t1 = termFactory.createVariable("x");
//			ValueConstant t2 = termFactory.createValueConstant("y");
//			List<ValueConstant> list = new Vector<ValueConstant>();
//			list.add(t2);
//			Term ft = termFactory.createObjectConstant(termFactory.getFunctionSymbol("p"), list);
//
//			Predicate pred1 = predFactory.createPredicate("A", 1);
//			List<Term> terms1 = new Vector<Term>();
//			terms1.add(t1);
//			Atom atom1 = tfac.getAtom(pred1, terms1);
//
//			Predicate pred2 = predFactory.createPredicate("A", 1);
//			List<Term> terms2 = new Vector<Term>();
//			terms2.add(ft);
//			Atom atom2 = tfac.getAtom(pred2, terms2);
//
//			AtomUnifier unifier = new AtomUnifier();
//			Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
//			assertEquals(1, s.size());
//
//			Substitution s0 = s.get(0);
//			ObjectConstantImpl t = (ObjectConstantImpl) s0.getTerm();
//			Term v = s0.getVariable();
//
//			assertEquals("p(y)", t.getName());
//			assertEquals("x", v.getName());
//		} catch (Exception e) {
//			e.printStackTrace();
//			assertEquals(false, true);
//		}
	}

	//A('y'),A(x)
	public void test_5(){

			NewLiteral t2 = termFactory.getVariable("x");
			NewLiteral t1 = termFactory.getValueConstant("y");

			Predicate pred1 = predFactory.getPredicate("A", 1);
			List<NewLiteral> terms1 = new Vector<NewLiteral>();
			terms1.add(t1);
			Atom atom1 = tfac.getAtom(pred1, terms1);

			Predicate pred2 = predFactory.getPredicate("A", 1);
			List<NewLiteral> terms2 = new Vector<NewLiteral>();
			terms2.add(t2);
			Atom atom2 = tfac.getAtom(pred2, terms2);

			Unifier unifier = new Unifier();
			Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
			assertEquals(1, s.size());

			Substitution s0 = s.get(0);
			ValueConstant t = (ValueConstant) s0.getTerm();
			NewLiteral v = s0.getVariable();

			assertEquals(t + " y", "y", ((ValueConstant) t).getValue());
			assertEquals(t + " x", "x", ((Variable) v).getName());
	}

	//A('y'),A('y')
	public void test_6(){


			NewLiteral t2 = termFactory.getValueConstant("y");
			NewLiteral t1 = termFactory.getValueConstant("y");

			Predicate pred1 = predFactory.getPredicate("A", 1);
			List<NewLiteral> terms1 = new Vector<NewLiteral>();
			terms1.add(t1);
			Atom atom1 = tfac.getAtom(pred1, terms1);

			Predicate pred2 = predFactory.getPredicate("A", 1);
			List<NewLiteral> terms2 = new Vector<NewLiteral>();
			terms2.add(t2);
			Atom atom2 = tfac.getAtom(pred2, terms2);

			Unifier unifier = new Unifier();
			Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
			assertEquals(0, s.size());
		
	}

	//A('y'),A('p(x)')
	public void test_7(){

//		try {
//
//			Term t1 = termFactory.createValueConstant("y");
//
//			ValueConstant t2 = termFactory.createValueConstant("y");
//			List<ValueConstant> list = new Vector<ValueConstant>();
//			list.add(t2);
//			Term ft = termFactory.createObjectConstant(termFactory.getFunctionSymbol("p"), list);
//
//			Predicate pred1 = predFactory.createPredicate("A", 1);
//			List<Term> terms1 = new Vector<Term>();
//			terms1.add(t1);
//			Atom atom1 = tfac.getAtom(pred1, terms1);
//
//			Predicate pred2 = predFactory.createPredicate("A", 1);
//			List<Term> terms2 = new Vector<Term>();
//			terms2.add(ft);
//			Atom atom2 = tfac.getAtom(pred2, terms2);
//
//			AtomUnifier unifier = new AtomUnifier();
//			Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
//			assertEquals(null, s);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			assertEquals(false, true);
//		}
	}

	//A('y'),A('x')
	public void test_8(){

		try {
			NewLiteral t2 = termFactory.getValueConstant("x");
			NewLiteral t1 = termFactory.getValueConstant("y");

			Predicate pred1 = predFactory.getPredicate("A", 1);
			List<NewLiteral> terms1 = new Vector<NewLiteral>();
			terms1.add(t1);
			Atom atom1 = tfac.getAtom(pred1, terms1);

			Predicate pred2 = predFactory.getPredicate("A", 1);
			List<NewLiteral> terms2 = new Vector<NewLiteral>();
			terms2.add(t2);
			Atom atom2 = tfac.getAtom(pred2, terms2);

			Unifier unifier = new Unifier();
			Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
			assertEquals(null, s);
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}
	}

	//A('y'),A(p(x))
	public void test_9(){

		try {
			NewLiteral t1 = termFactory.getValueConstant("y");
			NewLiteral t2 = termFactory.getVariable("y");
			List<NewLiteral> vars = new Vector<NewLiteral>();
			vars.add(t2);
			Predicate fs = predFactory.getPredicate("p", vars.size());
			FunctionalTermImpl ot =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs, vars);
			Predicate pred1 = predFactory.getPredicate("A", 1);
			List<NewLiteral> terms1 = new Vector<NewLiteral>();
			terms1.add(t1);
			Atom atom1 = tfac.getAtom(pred1, terms1);

			Predicate pred2 = predFactory.getPredicate("A", 1);
			List<NewLiteral> terms2 = new Vector<NewLiteral>();
			terms2.add(ot);
			Atom atom2 = tfac.getAtom(pred2, terms2);

			Unifier unifier = new Unifier();
			Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
			assertEquals(null, s);
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}
	}

	//A(p(x)), A(x)
	public void test_10(){

		NewLiteral t = termFactory.getVariable("x");
		List<NewLiteral> vars = new Vector<NewLiteral>();
		vars.add(t);
		Predicate fs = predFactory.getPredicate("p", vars.size());
		FunctionalTermImpl ot =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs, vars);
		NewLiteral t2 = termFactory.getVariable("x");

		Predicate pred1 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms1 = new Vector<NewLiteral>();
		terms1.add(ot);
		Atom atom1 = tfac.getAtom(pred1, terms1);

		Predicate pred2 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms2 = new Vector<NewLiteral>();
		terms2.add(t2);
		Atom atom2 = tfac.getAtom(pred2, terms2);

		Unifier unifier = new Unifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(null, s);
	}

	//A(p(x)), A(y)
	public void test_11(){

		NewLiteral t = termFactory.getVariable("x");
		List<NewLiteral> vars = new Vector<NewLiteral>();
		vars.add(t);
		Predicate fs = predFactory.getPredicate("p", vars.size());
		FunctionalTermImpl ot =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs, vars);
		NewLiteral t2 = termFactory.getVariable("y");

		Predicate pred1 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms1 = new Vector<NewLiteral>();
		terms1.add(ot);
		Atom atom1 = tfac.getAtom(pred1, terms1);

		Predicate pred2 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms2 = new Vector<NewLiteral>();
		terms2.add(t2);
		Atom atom2 = tfac.getAtom(pred2, terms2);

		Unifier unifier = new Unifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(1, s.size());

		Substitution sub = s.get(0);
		FunctionalTermImpl term = (FunctionalTermImpl) sub.getTerm();
		List<NewLiteral> para = term.getTerms();
		NewLiteral var = sub.getVariable();

		assertEquals("y", ((Variable) var).getName());
		assertEquals(1, para.size());
		assertEquals("x", ((Variable) para.get(0)).getName());

	}

	//A(p(x)), A(q(x))
	public void test_12(){

		NewLiteral t1 = termFactory.getVariable("x");
		List<NewLiteral> vars1 = new Vector<NewLiteral>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs1, vars1);
		NewLiteral t2 = termFactory.getVariable("x");
		List<NewLiteral> vars2 = new Vector<NewLiteral>();
		vars2.add(t2);
		Predicate fs2 = predFactory.getPredicate("q", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms1 = new Vector<NewLiteral>();
		terms1.add(ot1);
		Atom atom1 = tfac.getAtom(pred1, terms1);

		Predicate pred2 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms2 = new Vector<NewLiteral>();
		terms2.add(ot2);
		Atom atom2 = tfac.getAtom(pred2, terms2);

		Unifier unifier = new Unifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(null, s);
	}

	//A(p(x)), A(p(x))
	public void test_13(){

		NewLiteral t1 = termFactory.getVariable("x");
		List<NewLiteral> vars1 = new Vector<NewLiteral>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs1, vars1);
		NewLiteral t2 = termFactory.getVariable("x");
		List<NewLiteral> vars2 = new Vector<NewLiteral>();
		vars2.add(t2);
		Predicate fs2 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms1 = new Vector<NewLiteral>();
		terms1.add(ot1);
		Atom atom1 = tfac.getAtom(pred1, terms1);

		Predicate pred2 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms2 = new Vector<NewLiteral>();
		terms2.add(ot2);
		Atom atom2 = tfac.getAtom(pred2, terms2);

		Unifier unifier = new Unifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(0, s.size());
	}

	//A(p(x)), A(p(y))
	public void test_14(){

		NewLiteral t1 = termFactory.getVariable("x");
		List<NewLiteral> vars1 = new Vector<NewLiteral>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs1, vars1);
		NewLiteral t2 = termFactory.getVariable("y");
		List<NewLiteral> vars2 = new Vector<NewLiteral>();
		vars2.add(t2);
		Predicate fs2 = predFactory.getPredicate("p", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms1 = new Vector<NewLiteral>();
		terms1.add(ot1);
		Atom atom1 = tfac.getAtom(pred1, terms1);

		Predicate pred2 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms2 = new Vector<NewLiteral>();
		terms2.add(ot2);
		Atom atom2 = tfac.getAtom(pred2, terms2);

		Unifier unifier = new Unifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(1, s.size());

		Substitution sub = s.get(0);
		NewLiteral term = sub.getTerm();
		NewLiteral var = sub.getVariable();

		assertEquals("y", ((Variable) term).getName());
		assertEquals("x", ((Variable) var).getName());
	}

	//A(p(x)), A(p(y,z))
	public void test_15(){

		NewLiteral t1 = termFactory.getVariable("x");
		List<NewLiteral> vars1 = new Vector<NewLiteral>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs1, vars1);
		NewLiteral t2 = termFactory.getVariable("y");
		NewLiteral t3 = termFactory.getVariable("z");
		List<NewLiteral> vars2 = new Vector<NewLiteral>();
		vars2.add(t2);
		vars2.add(t3);
		Predicate fs2 = predFactory.getPredicate("p", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms1 = new Vector<NewLiteral>();
		terms1.add(ot1);
		Atom atom1 = tfac.getAtom(pred1, terms1);

		Predicate pred2 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms2 = new Vector<NewLiteral>();
		terms2.add(ot2);
		Atom atom2 = tfac.getAtom(pred2, terms2);

		Unifier unifier = new Unifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(null, s);
	}

	//A(p(x)), A(p('123'))
	public void test_16(){

		NewLiteral t1 = termFactory.getVariable("x");
		List<NewLiteral> vars1 = new Vector<NewLiteral>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs1, vars1);
		NewLiteral t2 = termFactory.getValueConstant("123");
		List<NewLiteral> vars2 = new Vector<NewLiteral>();
		vars2.add(t2);
		Predicate fs2 = predFactory.getPredicate("p", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms1 = new Vector<NewLiteral>();
		terms1.add(ot1);
		Atom atom1 = tfac.getAtom(pred1, terms1);

		Predicate pred2 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms2 = new Vector<NewLiteral>();
		terms2.add(ot2);
		Atom atom2 = tfac.getAtom(pred2, terms2);

		Unifier unifier = new Unifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(1, s.size());

		Substitution sub = s.get(0);
		ValueConstant term = (ValueConstant) sub.getTerm();
		NewLiteral var = sub.getVariable();

		assertEquals("123", term.getValue());
		assertEquals("x", ((Variable) var).getName());
	}

	//A(p(x)), A(p('123',z))
	public void test_17(){

		NewLiteral t1 = termFactory.getVariable("x");
		List<NewLiteral> vars1 = new Vector<NewLiteral>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs1, vars1);
		NewLiteral t2 = termFactory.getValueConstant("123");
		NewLiteral t3 = termFactory.getVariable("z");
		List<NewLiteral> vars2 = new Vector<NewLiteral>();
		vars2.add(t2);
		vars2.add(t3);
		Predicate fs2 = predFactory.getPredicate("p", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms1 = new Vector<NewLiteral>();
		terms1.add(ot1);
		Atom atom1 = tfac.getAtom(pred1, terms1);

		Predicate pred2 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms2 = new Vector<NewLiteral>();
		terms2.add(ot2);
		Atom atom2 = tfac.getAtom(pred2, terms2);

		Unifier unifier = new Unifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(null, s);
	}

	//A(p(x)), A(q('123'))
	public void test_18(){

		NewLiteral t1 = termFactory.getVariable("x");
		List<NewLiteral> vars1 = new Vector<NewLiteral>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs1, vars1);
		NewLiteral t2 = termFactory.getValueConstant("123");
		List<NewLiteral> vars2 = new Vector<NewLiteral>();
		vars2.add(t2);
		Predicate fs2 = predFactory.getPredicate("q", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms1 = new Vector<NewLiteral>();
		terms1.add(ot1);
		Atom atom1 = tfac.getAtom(pred1, terms1);

		Predicate pred2 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms2 = new Vector<NewLiteral>();
		terms2.add(ot2);
		Atom atom2 = tfac.getAtom(pred2, terms2);

		Unifier unifier = new Unifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(null, s);

	}

	//A(p(x,z)), A(p('123'))
	public void test_19(){

		NewLiteral t1 = termFactory.getVariable("x");
		NewLiteral t3 = termFactory.getVariable("z");
		List<NewLiteral> vars1 = new Vector<NewLiteral>();
		vars1.add(t1);
		vars1.add(t3);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs1, vars1);
		NewLiteral t2 = termFactory.getValueConstant("123");
		List<NewLiteral> vars2 = new Vector<NewLiteral>();
		vars2.add(t2);
		Predicate fs2 = predFactory.getPredicate("q", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms1 = new Vector<NewLiteral>();
		terms1.add(ot1);
		Atom atom1 = tfac.getAtom(pred1, terms1);

		Predicate pred2 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms2 = new Vector<NewLiteral>();
		terms2.add(ot2);
		Atom atom2 = tfac.getAtom(pred2, terms2);

		Unifier unifier = new Unifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(null, s);

	}

	//A(x), A(p(x))
	public void test_20(){

		NewLiteral t = termFactory.getVariable("x");
		List<NewLiteral> vars = new Vector<NewLiteral>();
		vars.add(t);
		Predicate fs = predFactory.getPredicate("p", vars.size());
		FunctionalTermImpl ot =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs, vars);
		NewLiteral t2 = termFactory.getVariable("x");

		Predicate pred1 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms1 = new Vector<NewLiteral>();
		terms1.add(t2);
		Atom atom1 = tfac.getAtom(pred1, terms1);

		Predicate pred2 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms2 = new Vector<NewLiteral>();
		terms2.add(ot);
		Atom atom2 = tfac.getAtom(pred2, terms2);

		Unifier unifier = new Unifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(null, s);
	}

	//A(y), A(p(x))
	public void test_21(){

		NewLiteral t = termFactory.getVariable("x");
		List<NewLiteral> vars = new Vector<NewLiteral>();
		vars.add(t);
		Predicate fs = predFactory.getPredicate("p", vars.size());
		FunctionalTermImpl ot =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs, vars);
		NewLiteral t2 = termFactory.getVariable("y");

		Predicate pred1 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms1 = new Vector<NewLiteral>();
		terms1.add(t2);
		Atom atom1 = tfac.getAtom(pred1, terms1);

		Predicate pred2 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms2 = new Vector<NewLiteral>();
		terms2.add(ot);
		Atom atom2 = tfac.getAtom(pred2, terms2);

		Unifier unifier = new Unifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(1, s.size());

		Substitution sub = s.get(0);
		FunctionalTermImpl term = (FunctionalTermImpl) sub.getTerm();
		List<NewLiteral> para = term.getTerms();
		NewLiteral var = sub.getVariable();

		assertEquals("y", ((Variable) var).getName());
		assertEquals(1, para.size());
		assertEquals("x", ((Variable) para.get(0)).getName());

	}

	//A(q(x)), A(p(x))
	public void test_22(){

		NewLiteral t1 = termFactory.getVariable("x");
		List<NewLiteral> vars1 = new Vector<NewLiteral>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs1, vars1);
		NewLiteral t2 = termFactory.getVariable("x");
		List<NewLiteral> vars2 = new Vector<NewLiteral>();
		vars2.add(t2);
		Predicate fs2 = predFactory.getPredicate("q", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms1 = new Vector<NewLiteral>();
		terms1.add(ot2);
		Atom atom1 = tfac.getAtom(pred1, terms1);

		Predicate pred2 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms2 = new Vector<NewLiteral>();
		terms2.add(ot1);
		Atom atom2 = tfac.getAtom(pred2, terms2);

		Unifier unifier = new Unifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(null, s);
	}

	//A(p(y)), A(p(x))
	public void test_24(){

		NewLiteral t1 = termFactory.getVariable("x");
		List<NewLiteral> vars1 = new Vector<NewLiteral>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs1, vars1);
		NewLiteral t2 = termFactory.getVariable("y");
		List<NewLiteral> vars2 = new Vector<NewLiteral>();
		vars2.add(t2);
		Predicate fs2 = predFactory.getPredicate("p", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms1 = new Vector<NewLiteral>();
		terms1.add(ot2);
		Atom atom1 = tfac.getAtom(pred1, terms1);

		Predicate pred2 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms2 = new Vector<NewLiteral>();
		terms2.add(ot1);
		Atom atom2 = tfac.getAtom(pred2, terms2);

		Unifier unifier = new Unifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(1, s.size());

		Substitution sub = s.get(0);
		NewLiteral term = sub.getTerm();
		NewLiteral var = sub.getVariable();

		assertEquals("x", ((Variable) term).getName());
		assertEquals("y", ((Variable) var).getName());
	}

	// A(p(y,z)), A(p(x))
	public void test_25(){

		NewLiteral t1 = termFactory.getVariable("x");
		List<NewLiteral> vars1 = new Vector<NewLiteral>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs1, vars1);
		NewLiteral t2 = termFactory.getVariable("y");
		NewLiteral t3 = termFactory.getVariable("z");
		List<NewLiteral> vars2 = new Vector<NewLiteral>();
		vars2.add(t2);
		vars2.add(t3);
		Predicate fs2 = predFactory.getPredicate("p", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms1 = new Vector<NewLiteral>();
		terms1.add(ot2);
		Atom atom1 = tfac.getAtom(pred1, terms1);

		Predicate pred2 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms2 = new Vector<NewLiteral>();
		terms2.add(ot1);
		Atom atom2 = tfac.getAtom(pred2, terms2);

		Unifier unifier = new Unifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(null, s);
	}

	//A(p('123')), A(p(x))
	public void test_26(){

		NewLiteral t1 = termFactory.getVariable("x");
		List<NewLiteral> vars1 = new Vector<NewLiteral>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs1, vars1);
		NewLiteral t2 = termFactory.getValueConstant("123");
		List<NewLiteral> vars2 = new Vector<NewLiteral>();
		vars2.add(t2);
		Predicate fs2 = predFactory.getPredicate("p", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms1 = new Vector<NewLiteral>();
		terms1.add(ot2);
		Atom atom1 = tfac.getAtom(pred1, terms1);

		Predicate pred2 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms2 = new Vector<NewLiteral>();
		terms2.add(ot1);
		Atom atom2 = tfac.getAtom(pred2, terms2);

		Unifier unifier = new Unifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(1, s.size());

		Substitution sub = s.get(0);
		ValueConstant term = (ValueConstant) sub.getTerm();
		NewLiteral var = sub.getVariable();

		assertEquals("123", term.getValue());
		assertEquals("x", ((Variable) var).getName());
	}

	//A(p('123',z)),A(p(x))
	public void test_27(){

		NewLiteral t1 = termFactory.getVariable("x");
		List<NewLiteral> vars1 = new Vector<NewLiteral>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs1, vars1);
		NewLiteral t2 = termFactory.getValueConstant("123");
		NewLiteral t3 = termFactory.getVariable("z");
		List<NewLiteral> vars2 = new Vector<NewLiteral>();
		vars2.add(t2);
		vars2.add(t3);
		Predicate fs2 = predFactory.getPredicate("p", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms1 = new Vector<NewLiteral>();
		terms1.add(ot2);
		Atom atom1 = tfac.getAtom(pred1, terms1);

		Predicate pred2 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms2 = new Vector<NewLiteral>();
		terms2.add(ot1);
		Atom atom2 = tfac.getAtom(pred2, terms2);

		Unifier unifier = new Unifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(null, s);
	}

	//A(q('123')),A(p(x))
	public void test_28(){

		NewLiteral t1 = termFactory.getVariable("x");
		List<NewLiteral> vars1 = new Vector<NewLiteral>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs1, vars1);
		NewLiteral t2 = termFactory.getValueConstant("123");
		List<NewLiteral> vars2 = new Vector<NewLiteral>();
		vars2.add(t2);
		Predicate fs2 = predFactory.getPredicate("q", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms1 = new Vector<NewLiteral>();
		terms1.add(ot2);
		Atom atom1 = tfac.getAtom(pred1, terms1);

		Predicate pred2 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms2 = new Vector<NewLiteral>();
		terms2.add(ot1);
		Atom atom2 = tfac.getAtom(pred2, terms2);

		Unifier unifier = new Unifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(null, s);

	}

	//A(p('123')),A(p(x,z))
	public void test_29(){

		NewLiteral t1 = termFactory.getVariable("x");
		NewLiteral t3 = termFactory.getVariable("z");
		List<NewLiteral> vars1 = new Vector<NewLiteral>();
		vars1.add(t1);
		vars1.add(t3);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs1, vars1);
		NewLiteral t2 = termFactory.getValueConstant("123");
		List<NewLiteral> vars2 = new Vector<NewLiteral>();
		vars2.add(t2);
		Predicate fs2 = predFactory.getPredicate("q", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms1 = new Vector<NewLiteral>();
		terms1.add(ot2);
		Atom atom1 = tfac.getAtom(pred1, terms1);

		Predicate pred2 = predFactory.getPredicate("A", 1);
		List<NewLiteral> terms2 = new Vector<NewLiteral>();
		terms2.add(ot1);
		Atom atom2 = tfac.getAtom(pred2, terms2);

		Unifier unifier = new Unifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(null, s);

	}

	//A(#),A(#)
	public void test_32(){

		try {
			NewLiteral t1 = termFactory.getNondistinguishedVariable();
			NewLiteral t2 = termFactory.getNondistinguishedVariable();

			Predicate pred1 = predFactory.getPredicate("A", 1);
			List<NewLiteral> terms1 = new Vector<NewLiteral>();
			terms1.add(t1);
			Atom atom1 = tfac.getAtom(pred1, terms1);

			Predicate pred2 = predFactory.getPredicate("A", 1);
			List<NewLiteral> terms2 = new Vector<NewLiteral>();
			terms2.add(t2);
			Atom atom2 = tfac.getAtom(pred2, terms2);

			Unifier unifier = new Unifier();
			Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
			assertEquals(0, s.size());
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}

	}

	//A(x),A(#)
	public void test_33(){

		try {
			NewLiteral t1 = termFactory.getVariable("x");
			NewLiteral t2 = termFactory.getNondistinguishedVariable();

			Predicate pred1 = predFactory.getPredicate("A", 1);
			List<NewLiteral> terms1 = new Vector<NewLiteral>();
			terms1.add(t1);
			Atom atom1 = tfac.getAtom(pred1, terms1);

			Predicate pred2 = predFactory.getPredicate("A", 1);
			List<NewLiteral> terms2 = new Vector<NewLiteral>();
			terms2.add(t2);
			Atom atom2 = tfac.getAtom(pred2, terms2);

			Unifier unifier = new Unifier();
			Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
			assertEquals(0, s.size());
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}
	}

}
