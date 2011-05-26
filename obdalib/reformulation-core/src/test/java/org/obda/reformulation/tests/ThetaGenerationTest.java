package org.obda.reformulation.tests;

import inf.unibz.it.obda.api.controller.OBDADataFactory;
import inf.unibz.it.obda.model.impl.AtomImpl;
import inf.unibz.it.obda.model.impl.FunctionalTermImpl;
import inf.unibz.it.obda.model.impl.OBDADataFactoryImpl;
import inf.unibz.it.obda.model.impl.TermFactoryImpl;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import junit.framework.TestCase;

import org.obda.owlrefplatform.core.basicoperations.AtomUnifier;
import org.obda.owlrefplatform.core.basicoperations.Substitution;
import org.obda.query.domain.Predicate;
import org.obda.query.domain.Term;
import org.obda.query.domain.ValueConstant;
import org.obda.query.domain.Variable;

public class ThetaGenerationTest extends TestCase {

	org.obda.query.domain.OBDADataFactory termFactory =  TermFactoryImpl.getInstance();
	OBDADataFactory predFactory = OBDADataFactoryImpl.getInstance();

	private Vector<Substitution> getMGUAsVector(Map<Variable, Term> mgu) {
		Vector<Substitution> computedmgu = new Vector<Substitution>();
		if (mgu == null) {
			computedmgu = null;
		} else {
			for (Term var : mgu.keySet()) {
				computedmgu.add(new Substitution(var, mgu.get(var)));
			}
		}
		return computedmgu;

	}

	//A(x),A(x)
	public void test_1(){

		try {
			Term t1 = termFactory.createVariable("x");
			Term t2 = termFactory.createVariable("x");

			Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
			List<Term> terms1 = new Vector<Term>();
			terms1.add(t1);
			AtomImpl atom1 = new AtomImpl(pred1, terms1);

			Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
			List<Term> terms2 = new Vector<Term>();
			terms2.add(t2);
			AtomImpl atom2 = new AtomImpl(pred2, terms2);

			AtomUnifier unifier = new AtomUnifier();
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
			Term t1 = termFactory.createVariable("x");
			Term t2 = termFactory.createVariable("y");

			Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
			List<Term> terms1 = new Vector<Term>();
			terms1.add(t1);
			AtomImpl atom1 = new AtomImpl(pred1, terms1);

			Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
			List<Term> terms2 = new Vector<Term>();
			terms2.add(t2);
			AtomImpl atom2 = new AtomImpl(pred2, terms2);

			AtomUnifier unifier = new AtomUnifier();
			Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
			assertEquals(1, s.size());

			Substitution s0 = s.get(0);
			Term t = s0.getTerm();
			Term v = s0.getVariable();

			assertEquals("y", t.getName());
			assertEquals("x", v.getName());
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}
	}

	//A(x),A('y')
	public void test_3(){

		try {
			Term t1 = termFactory.createVariable("x");
			Term t2 = termFactory.createValueConstant("y");

			Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
			List<Term> terms1 = new Vector<Term>();
			terms1.add(t1);
			AtomImpl atom1 = new AtomImpl(pred1, terms1);

			Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
			List<Term> terms2 = new Vector<Term>();
			terms2.add(t2);
			AtomImpl atom2 = new AtomImpl(pred2, terms2);

			AtomUnifier unifier = new AtomUnifier();
			Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
			assertEquals(1, s.size());

			Substitution s0 = s.get(0);
			ValueConstant t = (ValueConstant) s0.getTerm();
			Term v = s0.getVariable();

			assertEquals("y", t.getName());
			assertEquals("x", v.getName());
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}
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
//			Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
//			List<Term> terms1 = new Vector<Term>();
//			terms1.add(t1);
//			AtomImpl atom1 = new AtomImpl(pred1, terms1);
//
//			Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
//			List<Term> terms2 = new Vector<Term>();
//			terms2.add(ft);
//			AtomImpl atom2 = new AtomImpl(pred2, terms2);
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

		try {
			Term t2 = termFactory.createVariable("x");
			Term t1 = termFactory.createValueConstant("y");

			Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
			List<Term> terms1 = new Vector<Term>();
			terms1.add(t1);
			AtomImpl atom1 = new AtomImpl(pred1, terms1);

			Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
			List<Term> terms2 = new Vector<Term>();
			terms2.add(t2);
			AtomImpl atom2 = new AtomImpl(pred2, terms2);

			AtomUnifier unifier = new AtomUnifier();
			Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
			assertEquals(1, s.size());

			Substitution s0 = s.get(0);
			ValueConstant t = (ValueConstant) s0.getTerm();
			Term v = s0.getVariable();

			assertEquals("y", t.getName());
			assertEquals("x", v.getName());
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}
	}

	//A('y'),A('y')
	public void test_6(){

		try {
			Term t2 = termFactory.createValueConstant("y");
			Term t1 = termFactory.createValueConstant("y");

			Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
			List<Term> terms1 = new Vector<Term>();
			terms1.add(t1);
			AtomImpl atom1 = new AtomImpl(pred1, terms1);

			Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
			List<Term> terms2 = new Vector<Term>();
			terms2.add(t2);
			AtomImpl atom2 = new AtomImpl(pred2, terms2);

			AtomUnifier unifier = new AtomUnifier();
			Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
			assertEquals(0, s.size());
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}
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
//			Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
//			List<Term> terms1 = new Vector<Term>();
//			terms1.add(t1);
//			AtomImpl atom1 = new AtomImpl(pred1, terms1);
//
//			Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
//			List<Term> terms2 = new Vector<Term>();
//			terms2.add(ft);
//			AtomImpl atom2 = new AtomImpl(pred2, terms2);
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
			Term t2 = termFactory.createValueConstant("x");
			Term t1 = termFactory.createValueConstant("y");

			Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
			List<Term> terms1 = new Vector<Term>();
			terms1.add(t1);
			AtomImpl atom1 = new AtomImpl(pred1, terms1);

			Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
			List<Term> terms2 = new Vector<Term>();
			terms2.add(t2);
			AtomImpl atom2 = new AtomImpl(pred2, terms2);

			AtomUnifier unifier = new AtomUnifier();
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
			Term t1 = termFactory.createValueConstant("y");
			Term t2 = termFactory.createVariable("y");
			List<Term> vars = new Vector<Term>();
			vars.add(t2);
			Predicate fs = predFactory.createPredicate(URI.create("p"), vars.size());
			FunctionalTermImpl ot =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs, vars);
			Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
			List<Term> terms1 = new Vector<Term>();
			terms1.add(t1);
			AtomImpl atom1 = new AtomImpl(pred1, terms1);

			Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
			List<Term> terms2 = new Vector<Term>();
			terms2.add(ot);
			AtomImpl atom2 = new AtomImpl(pred2, terms2);

			AtomUnifier unifier = new AtomUnifier();
			Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
			assertEquals(null, s);
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}
	}

	//A(p(x)), A(x)
	public void test_10(){

		Term t = termFactory.createVariable("x");
		List<Term> vars = new Vector<Term>();
		vars.add(t);
		Predicate fs = predFactory.createPredicate(URI.create("p"), vars.size());
		FunctionalTermImpl ot =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs, vars);
		Term t2 = termFactory.createVariable("x");

		Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot);
		AtomImpl atom1 = new AtomImpl(pred1, terms1);

		Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms2 = new Vector<Term>();
		terms2.add(t2);
		AtomImpl atom2 = new AtomImpl(pred2, terms2);

		AtomUnifier unifier = new AtomUnifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(null, s);
	}

	//A(p(x)), A(y)
	public void test_11(){

		Term t = termFactory.createVariable("x");
		List<Term> vars = new Vector<Term>();
		vars.add(t);
		Predicate fs = predFactory.createPredicate(URI.create("p"), vars.size());
		FunctionalTermImpl ot =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs, vars);
		Term t2 = termFactory.createVariable("y");

		Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot);
		AtomImpl atom1 = new AtomImpl(pred1, terms1);

		Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms2 = new Vector<Term>();
		terms2.add(t2);
		AtomImpl atom2 = new AtomImpl(pred2, terms2);

		AtomUnifier unifier = new AtomUnifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(1, s.size());

		Substitution sub = s.get(0);
		FunctionalTermImpl term = (FunctionalTermImpl) sub.getTerm();
		List<Term> para = term.getTerms();
		Term var = sub.getVariable();

		assertEquals("y", var.getName());
		assertEquals(1, para.size());
		assertEquals("x", para.get(0).getName());

	}

	//A(p(x)), A(q(x))
	public void test_12(){

		Term t1 = termFactory.createVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.createPredicate(URI.create("p"), vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs1, vars1);
		Term t2 = termFactory.createVariable("x");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		Predicate fs2 = predFactory.createPredicate(URI.create("q"), vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot1);
		AtomImpl atom1 = new AtomImpl(pred1, terms1);

		Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot2);
		AtomImpl atom2 = new AtomImpl(pred2, terms2);

		AtomUnifier unifier = new AtomUnifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(null, s);
	}

	//A(p(x)), A(p(x))
	public void test_13(){

		Term t1 = termFactory.createVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.createPredicate(URI.create("p"), vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs1, vars1);
		Term t2 = termFactory.createVariable("x");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		Predicate fs2 = predFactory.createPredicate(URI.create("p"), vars1.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot1);
		AtomImpl atom1 = new AtomImpl(pred1, terms1);

		Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot2);
		AtomImpl atom2 = new AtomImpl(pred2, terms2);

		AtomUnifier unifier = new AtomUnifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(0, s.size());
	}

	//A(p(x)), A(p(y))
	public void test_14(){

		Term t1 = termFactory.createVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.createPredicate(URI.create("p"), vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs1, vars1);
		Term t2 = termFactory.createVariable("y");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		Predicate fs2 = predFactory.createPredicate(URI.create("p"), vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot1);
		AtomImpl atom1 = new AtomImpl(pred1, terms1);

		Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot2);
		AtomImpl atom2 = new AtomImpl(pred2, terms2);

		AtomUnifier unifier = new AtomUnifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(1, s.size());

		Substitution sub = s.get(0);
		Term term = sub.getTerm();
		Term var = sub.getVariable();

		assertEquals("y", term.getName());
		assertEquals("x", var.getName());
	}

	//A(p(x)), A(p(y,z))
	public void test_15(){

		Term t1 = termFactory.createVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.createPredicate(URI.create("p"), vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs1, vars1);
		Term t2 = termFactory.createVariable("y");
		Term t3 = termFactory.createVariable("z");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		vars2.add(t3);
		Predicate fs2 = predFactory.createPredicate(URI.create("p"), vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot1);
		AtomImpl atom1 = new AtomImpl(pred1, terms1);

		Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot2);
		AtomImpl atom2 = new AtomImpl(pred2, terms2);

		AtomUnifier unifier = new AtomUnifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(null, s);
	}

	//A(p(x)), A(p('123'))
	public void test_16(){

		Term t1 = termFactory.createVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.createPredicate(URI.create("p"), vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs1, vars1);
		Term t2 = termFactory.createValueConstant("123");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		Predicate fs2 = predFactory.createPredicate(URI.create("p"), vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot1);
		AtomImpl atom1 = new AtomImpl(pred1, terms1);

		Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot2);
		AtomImpl atom2 = new AtomImpl(pred2, terms2);

		AtomUnifier unifier = new AtomUnifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(1, s.size());

		Substitution sub = s.get(0);
		ValueConstant term = (ValueConstant) sub.getTerm();
		Term var = sub.getVariable();

		assertEquals("123", term.getName());
		assertEquals("x", var.getName());
	}

	//A(p(x)), A(p('123',z))
	public void test_17(){

		Term t1 = termFactory.createVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.createPredicate(URI.create("p"), vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs1, vars1);
		Term t2 = termFactory.createValueConstant("123");
		Term t3 = termFactory.createVariable("z");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		vars2.add(t3);
		Predicate fs2 = predFactory.createPredicate(URI.create("p"), vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot1);
		AtomImpl atom1 = new AtomImpl(pred1, terms1);

		Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot2);
		AtomImpl atom2 = new AtomImpl(pred2, terms2);

		AtomUnifier unifier = new AtomUnifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(null, s);
	}

	//A(p(x)), A(q('123'))
	public void test_18(){

		Term t1 = termFactory.createVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.createPredicate(URI.create("p"), vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs1, vars1);
		Term t2 = termFactory.createValueConstant("123");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		Predicate fs2 = predFactory.createPredicate(URI.create("q"), vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot1);
		AtomImpl atom1 = new AtomImpl(pred1, terms1);

		Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot2);
		AtomImpl atom2 = new AtomImpl(pred2, terms2);

		AtomUnifier unifier = new AtomUnifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(null, s);

	}

	//A(p(x,z)), A(p('123'))
	public void test_19(){

		Term t1 = termFactory.createVariable("x");
		Term t3 = termFactory.createVariable("z");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		vars1.add(t3);
		Predicate fs1 = predFactory.createPredicate(URI.create("p"), vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs1, vars1);
		Term t2 = termFactory.createValueConstant("123");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		Predicate fs2 = predFactory.createPredicate(URI.create("q"), vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot1);
		AtomImpl atom1 = new AtomImpl(pred1, terms1);

		Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot2);
		AtomImpl atom2 = new AtomImpl(pred2, terms2);

		AtomUnifier unifier = new AtomUnifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(null, s);

	}

	//A(x), A(p(x))
	public void test_20(){

		Term t = termFactory.createVariable("x");
		List<Term> vars = new Vector<Term>();
		vars.add(t);
		Predicate fs = predFactory.createPredicate(URI.create("p"), vars.size());
		FunctionalTermImpl ot =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs, vars);
		Term t2 = termFactory.createVariable("x");

		Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms1 = new Vector<Term>();
		terms1.add(t2);
		AtomImpl atom1 = new AtomImpl(pred1, terms1);

		Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot);
		AtomImpl atom2 = new AtomImpl(pred2, terms2);

		AtomUnifier unifier = new AtomUnifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(null, s);
	}

	//A(y), A(p(x))
	public void test_21(){

		Term t = termFactory.createVariable("x");
		List<Term> vars = new Vector<Term>();
		vars.add(t);
		Predicate fs = predFactory.createPredicate(URI.create("p"), vars.size());
		FunctionalTermImpl ot =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs, vars);
		Term t2 = termFactory.createVariable("y");

		Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms1 = new Vector<Term>();
		terms1.add(t2);
		AtomImpl atom1 = new AtomImpl(pred1, terms1);

		Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot);
		AtomImpl atom2 = new AtomImpl(pred2, terms2);

		AtomUnifier unifier = new AtomUnifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(1, s.size());

		Substitution sub = s.get(0);
		FunctionalTermImpl term = (FunctionalTermImpl) sub.getTerm();
		List<Term> para = term.getTerms();
		Term var = sub.getVariable();

		assertEquals("y", var.getName());
		assertEquals(1, para.size());
		assertEquals("x", para.get(0).getName());

	}

	//A(q(x)), A(p(x))
	public void test_22(){

		Term t1 = termFactory.createVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.createPredicate(URI.create("p"), vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs1, vars1);
		Term t2 = termFactory.createVariable("x");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		Predicate fs2 = predFactory.createPredicate(URI.create("q"), vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot2);
		AtomImpl atom1 = new AtomImpl(pred1, terms1);

		Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot1);
		AtomImpl atom2 = new AtomImpl(pred2, terms2);

		AtomUnifier unifier = new AtomUnifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(null, s);
	}

	//A(p(y)), A(p(x))
	public void test_24(){

		Term t1 = termFactory.createVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.createPredicate(URI.create("p"), vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs1, vars1);
		Term t2 = termFactory.createVariable("y");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		Predicate fs2 = predFactory.createPredicate(URI.create("p"), vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot2);
		AtomImpl atom1 = new AtomImpl(pred1, terms1);

		Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot1);
		AtomImpl atom2 = new AtomImpl(pred2, terms2);

		AtomUnifier unifier = new AtomUnifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(1, s.size());

		Substitution sub = s.get(0);
		Term term = sub.getTerm();
		Term var = sub.getVariable();

		assertEquals("x", term.getName());
		assertEquals("y", var.getName());
	}

	// A(p(y,z)), A(p(x))
	public void test_25(){

		Term t1 = termFactory.createVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.createPredicate(URI.create("p"), vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs1, vars1);
		Term t2 = termFactory.createVariable("y");
		Term t3 = termFactory.createVariable("z");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		vars2.add(t3);
		Predicate fs2 = predFactory.createPredicate(URI.create("p"), vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot2);
		AtomImpl atom1 = new AtomImpl(pred1, terms1);

		Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot1);
		AtomImpl atom2 = new AtomImpl(pred2, terms2);

		AtomUnifier unifier = new AtomUnifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(null, s);
	}

	//A(p('123')), A(p(x))
	public void test_26(){

		Term t1 = termFactory.createVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.createPredicate(URI.create("p"), vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs1, vars1);
		Term t2 = termFactory.createValueConstant("123");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		Predicate fs2 = predFactory.createPredicate(URI.create("p"), vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot2);
		AtomImpl atom1 = new AtomImpl(pred1, terms1);

		Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot1);
		AtomImpl atom2 = new AtomImpl(pred2, terms2);

		AtomUnifier unifier = new AtomUnifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(1, s.size());

		Substitution sub = s.get(0);
		ValueConstant term = (ValueConstant) sub.getTerm();
		Term var = sub.getVariable();

		assertEquals("123", term.getName());
		assertEquals("x", var.getName());
	}

	//A(p('123',z)),A(p(x))
	public void test_27(){

		Term t1 = termFactory.createVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.createPredicate(URI.create("p"), vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs1, vars1);
		Term t2 = termFactory.createValueConstant("123");
		Term t3 = termFactory.createVariable("z");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		vars2.add(t3);
		Predicate fs2 = predFactory.createPredicate(URI.create("p"), vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot2);
		AtomImpl atom1 = new AtomImpl(pred1, terms1);

		Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot1);
		AtomImpl atom2 = new AtomImpl(pred2, terms2);

		AtomUnifier unifier = new AtomUnifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(null, s);
	}

	//A(q('123')),A(p(x))
	public void test_28(){

		Term t1 = termFactory.createVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.createPredicate(URI.create("p"), vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs1, vars1);
		Term t2 = termFactory.createValueConstant("123");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		Predicate fs2 = predFactory.createPredicate(URI.create("q"), vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot2);
		AtomImpl atom1 = new AtomImpl(pred1, terms1);

		Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot1);
		AtomImpl atom2 = new AtomImpl(pred2, terms2);

		AtomUnifier unifier = new AtomUnifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(null, s);

	}

	//A(p('123')),A(p(x,z))
	public void test_29(){

		Term t1 = termFactory.createVariable("x");
		Term t3 = termFactory.createVariable("z");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		vars1.add(t3);
		Predicate fs1 = predFactory.createPredicate(URI.create("p"), vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs1, vars1);
		Term t2 = termFactory.createValueConstant("123");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		Predicate fs2 = predFactory.createPredicate(URI.create("q"), vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs2, vars2);

		Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot2);
		AtomImpl atom1 = new AtomImpl(pred1, terms1);

		Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot1);
		AtomImpl atom2 = new AtomImpl(pred2, terms2);

		AtomUnifier unifier = new AtomUnifier();
		Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
		assertEquals(null, s);

	}

	//A(#),A(#)
	public void test_32(){

		try {
			Term t1 = termFactory.createUndistinguishedVariable();
			Term t2 = termFactory.createUndistinguishedVariable();

			Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
			List<Term> terms1 = new Vector<Term>();
			terms1.add(t1);
			AtomImpl atom1 = new AtomImpl(pred1, terms1);

			Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
			List<Term> terms2 = new Vector<Term>();
			terms2.add(t2);
			AtomImpl atom2 = new AtomImpl(pred2, terms2);

			AtomUnifier unifier = new AtomUnifier();
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
			Term t1 = termFactory.createVariable("x");
			Term t2 = termFactory.createUndistinguishedVariable();

			Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
			List<Term> terms1 = new Vector<Term>();
			terms1.add(t1);
			AtomImpl atom1 = new AtomImpl(pred1, terms1);

			Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
			List<Term> terms2 = new Vector<Term>();
			terms2.add(t2);
			AtomImpl atom2 = new AtomImpl(pred2, terms2);

			AtomUnifier unifier = new AtomUnifier();
			Vector<Substitution> s = getMGUAsVector(unifier.getMGU(atom1, atom2));
			assertEquals(0, s.size());
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}
	}

}
