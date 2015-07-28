package org.semanticweb.ontop.reformulation.tests;

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

import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.ValueConstant;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.model.impl.FunctionalTermImpl;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.SingletonSubstitution;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Substitution;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.semanticweb.ontop.owlrefplatform.core.basicoperations.UnifierUtilities;
import junit.framework.TestCase;

//import com.hp.hpl.jena.iri.IRIFactory;


public class ThetaGenerationTest extends TestCase {

	OBDADataFactory termFactory =  OBDADataFactoryImpl.getInstance();
	OBDADataFactory tfac =  OBDADataFactoryImpl.getInstance();
	OBDADataFactory predFactory = OBDADataFactoryImpl.getInstance();

	private Vector<SingletonSubstitution> getMGUAsVector(Substitution mgu) {
		Vector<SingletonSubstitution> computedmgu = new Vector<>();
		if (mgu == null) {
			computedmgu = null;
		} else {
			for (Map.Entry<Variable,Term> m : mgu.getMap().entrySet()) {
				computedmgu.add(new SingletonSubstitution(m.getKey(), m.getValue()));
			}
		}
		return computedmgu;

	}

	//A(x),A(x)
	public void test_1(){

		try {
			Term t1 = termFactory.getVariable("x");
			Term t2 = termFactory.getVariable("x");

			Predicate pred1 = predFactory.getClassPredicate("A");
			List<Term> terms1 = new Vector<Term>();
			terms1.add(t1);
			Function atom1 = tfac.getFunction(pred1, terms1);

			Predicate pred2 = predFactory.getClassPredicate("A");
			List<Term> terms2 = new Vector<Term>();
			terms2.add(t2);
			Function atom2 = tfac.getFunction(pred2, terms2);

			Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
			assertEquals(0, s.size());
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}

	}

	//A(x),A(y)
	public void test_2(){

		try {
			Term t1 = termFactory.getVariable("x");
			Term t2 = termFactory.getVariable("y");

			Predicate pred1 = predFactory.getClassPredicate("A");
			List<Term> terms1 = new Vector<Term>();
			terms1.add(t1);
			Function atom1 = tfac.getFunction(pred1, terms1);

			Predicate pred2 = predFactory.getClassPredicate("A");
			List<Term> terms2 = new Vector<Term>();
			terms2.add(t2);
			Function atom2 = tfac.getFunction(pred2, terms2);

			Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
			assertEquals(1, s.size());

			SingletonSubstitution s0 = s.get(0);
			Term t = s0.getTerm();
			Term v = s0.getVariable();

			assertEquals("y", ((Variable) t).getName());
			assertEquals("x", ((Variable) v).getName());
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}
	}

	//A(x),A('y')
	public void test_3(){

		
			Term t1 = termFactory.getVariable("x");
			Term t2 = termFactory.getConstantLiteral("y");

			Predicate pred1 = predFactory.getClassPredicate("A");
			List<Term> terms1 = new Vector<Term>();
			terms1.add(t1);
			Function atom1 = tfac.getFunction(pred1, terms1);

			Predicate pred2 = predFactory.getClassPredicate("A");
			List<Term> terms2 = new Vector<Term>();
			terms2.add(t2);
			Function atom2 = tfac.getFunction(pred2, terms2);

			Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
			assertEquals(1, s.size());

			SingletonSubstitution s0 = s.get(0);
			ValueConstant t = (ValueConstant) s0.getTerm();
			Term v = s0.getVariable();

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
//			Function atom1 = tfac.getFunctionalTerm(pred1, terms1);
//
//			Predicate pred2 = predFactory.createPredicate("A", 1);
//			List<Term> terms2 = new Vector<Term>();
//			terms2.add(ft);
//			Function atom2 = tfac.getFunctionalTerm(pred2, terms2);
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

			Term t2 = termFactory.getVariable("x");
			Term t1 = termFactory.getConstantLiteral("y");

			Predicate pred1 = predFactory.getClassPredicate("A");
			List<Term> terms1 = new Vector<Term>();
			terms1.add(t1);
			Function atom1 = tfac.getFunction(pred1, terms1);

			Predicate pred2 = predFactory.getClassPredicate("A");
			List<Term> terms2 = new Vector<Term>();
			terms2.add(t2);
			Function atom2 = tfac.getFunction(pred2, terms2);

			Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
			assertEquals(1, s.size());

			SingletonSubstitution s0 = s.get(0);
			ValueConstant t = (ValueConstant) s0.getTerm();
			Term v = s0.getVariable();

			assertEquals(t + " y", "y", ((ValueConstant) t).getValue());
			assertEquals(t + " x", "x", ((Variable) v).getName());
	}

	//A('y'),A('y')
	public void test_6(){


			Term t2 = termFactory.getConstantLiteral("y");
			Term t1 = termFactory.getConstantLiteral("y");

			Predicate pred1 = predFactory.getClassPredicate("A");
			List<Term> terms1 = new Vector<Term>();
			terms1.add(t1);
			Function atom1 = tfac.getFunction(pred1, terms1);

			Predicate pred2 = predFactory.getClassPredicate("A");
			List<Term> terms2 = new Vector<Term>();
			terms2.add(t2);
			Function atom2 = tfac.getFunction(pred2, terms2);

			Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
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
//			Function atom1 = tfac.getFunctionalTerm(pred1, terms1);
//
//			Predicate pred2 = predFactory.createPredicate("A", 1);
//			List<Term> terms2 = new Vector<Term>();
//			terms2.add(ft);
//			Function atom2 = tfac.getFunctionalTerm(pred2, terms2);
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
			Term t2 = termFactory.getConstantLiteral("x");
			Term t1 = termFactory.getConstantLiteral("y");

			Predicate pred1 = predFactory.getClassPredicate("A");
			List<Term> terms1 = new Vector<Term>();
			terms1.add(t1);
			Function atom1 = tfac.getFunction(pred1, terms1);

			Predicate pred2 = predFactory.getClassPredicate("A");
			List<Term> terms2 = new Vector<Term>();
			terms2.add(t2);
			Function atom2 = tfac.getFunction(pred2, terms2);

			Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
			assertEquals(null, s);
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}
	}

	//A('y'),A(p(x))
	public void test_9(){

		try {
			Term t1 = termFactory.getConstantLiteral("y");
			Term t2 = termFactory.getVariable("y");
			List<Term> vars = new Vector<Term>();
			vars.add(t2);
			Predicate fs = predFactory.getPredicate("p", vars.size());
			FunctionalTermImpl ot =(FunctionalTermImpl) termFactory.getFunction(fs, vars);
			Predicate pred1 = predFactory.getClassPredicate("A");
			List<Term> terms1 = new Vector<Term>();
			terms1.add(t1);
			Function atom1 = tfac.getFunction(pred1, terms1);

			Predicate pred2 = predFactory.getClassPredicate("A");
			List<Term> terms2 = new Vector<Term>();
			terms2.add(ot);
			Function atom2 = tfac.getFunction(pred2, terms2);

			Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
			assertEquals(null, s);
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}
	}

	//A(p(x)), A(x)
	public void test_10(){

		Term t = termFactory.getVariable("x");
		List<Term> vars = new Vector<Term>();
		vars.add(t);
		Predicate fs = predFactory.getPredicate("p", vars.size());
		FunctionalTermImpl ot =(FunctionalTermImpl) termFactory.getFunction(fs, vars);
		Term t2 = termFactory.getVariable("x");

		Predicate pred1 = predFactory.getClassPredicate("A");
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot);
		Function atom1 = tfac.getFunction(pred1, terms1);

		Predicate pred2 = predFactory.getClassPredicate("A");
		List<Term> terms2 = new Vector<Term>();
		terms2.add(t2);
		Function atom2 = tfac.getFunction(pred2, terms2);

		Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
		assertEquals(null, s);
	}

	//A(p(x)), A(y)
	public void test_11(){

		Term t = termFactory.getVariable("x");
		List<Term> vars = new Vector<Term>();
		vars.add(t);
		Predicate fs = predFactory.getPredicate("p", vars.size());
		FunctionalTermImpl ot =(FunctionalTermImpl) termFactory.getFunction(fs, vars);
		Term t2 = termFactory.getVariable("y");

		Predicate pred1 = predFactory.getClassPredicate("A");
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot);
		Function atom1 = tfac.getFunction(pred1, terms1);

		Predicate pred2 = predFactory.getClassPredicate("A");
		List<Term> terms2 = new Vector<Term>();
		terms2.add(t2);
		Function atom2 = tfac.getFunction(pred2, terms2);

		Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
		assertEquals(1, s.size());

		SingletonSubstitution sub = s.get(0);
		FunctionalTermImpl term = (FunctionalTermImpl) sub.getTerm();
		List<Term> para = term.getTerms();
		Term var = sub.getVariable();

		assertEquals("y", ((Variable) var).getName());
		assertEquals(1, para.size());
		assertEquals("x", ((Variable) para.get(0)).getName());

	}

	//A(p(x)), A(q(x))
	public void test_12(){

		Term t1 = termFactory.getVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunction(fs1, vars1);
		Term t2 = termFactory.getVariable("x");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		Predicate fs2 = predFactory.getPredicate("q", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunction(fs2, vars2);

		Predicate pred1 = predFactory.getClassPredicate("A");
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot1);
		Function atom1 = tfac.getFunction(pred1, terms1);

		Predicate pred2 = predFactory.getClassPredicate("A");
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot2);
		Function atom2 = tfac.getFunction(pred2, terms2);

		Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
		assertEquals(null, s);
	}

	//A(p(x)), A(p(x))
	public void test_13(){

		Term t1 = termFactory.getVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunction(fs1, vars1);
		Term t2 = termFactory.getVariable("x");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		Predicate fs2 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunction(fs2, vars2);

		Predicate pred1 = predFactory.getClassPredicate("A");
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot1);
		Function atom1 = tfac.getFunction(pred1, terms1);

		Predicate pred2 = predFactory.getClassPredicate("A");
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot2);
		Function atom2 = tfac.getFunction(pred2, terms2);

		Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
		assertEquals(0, s.size());
	}

	//A(p(x)), A(p(y))
	public void test_14(){

		Term t1 = termFactory.getVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunction(fs1, vars1);
		Term t2 = termFactory.getVariable("y");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		Predicate fs2 = predFactory.getPredicate("p", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunction(fs2, vars2);

		Predicate pred1 = predFactory.getClassPredicate("A");
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot1);
		Function atom1 = tfac.getFunction(pred1, terms1);

		Predicate pred2 = predFactory.getClassPredicate("A");
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot2);
		Function atom2 = tfac.getFunction(pred2, terms2);

		Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
		assertEquals(1, s.size());

		SingletonSubstitution sub = s.get(0);
		Term term = sub.getTerm();
		Term var = sub.getVariable();

		assertEquals("y", ((Variable) term).getName());
		assertEquals("x", ((Variable) var).getName());
	}

	//A(p(x)), A(p(y,z))
	public void test_15(){

		Term t1 = termFactory.getVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunction(fs1, vars1);
		Term t2 = termFactory.getVariable("y");
		Term t3 = termFactory.getVariable("z");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		vars2.add(t3);
		Predicate fs2 = predFactory.getPredicate("p", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunction(fs2, vars2);

		Predicate pred1 = predFactory.getClassPredicate("A");
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot1);
		Function atom1 = tfac.getFunction(pred1, terms1);

		Predicate pred2 = predFactory.getClassPredicate("A");
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot2);
		Function atom2 = tfac.getFunction(pred2, terms2);

		Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
		assertEquals(null, s);
	}

	//A(p(x)), A(p('123'))
	public void test_16(){

		Term t1 = termFactory.getVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunction(fs1, vars1);
		Term t2 = termFactory.getConstantLiteral("123");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		Predicate fs2 = predFactory.getPredicate("p", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunction(fs2, vars2);

		Predicate pred1 = predFactory.getPredicate("A", 1);
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot1);
		Function atom1 = tfac.getFunction(pred1, terms1);

		Predicate pred2 = predFactory.getPredicate("A", 1);
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot2);
		Function atom2 = tfac.getFunction(pred2, terms2);

		Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
		assertEquals(1, s.size());

		SingletonSubstitution sub = s.get(0);
		ValueConstant term = (ValueConstant) sub.getTerm();
		Term var = sub.getVariable();

		assertEquals("123", term.getValue());
		assertEquals("x", ((Variable) var).getName());
	}

	//A(p(x)), A(p('123',z))
	public void test_17(){

		Term t1 = termFactory.getVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunction(fs1, vars1);
		Term t2 = termFactory.getConstantLiteral("123");
		Term t3 = termFactory.getVariable("z");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		vars2.add(t3);
		Predicate fs2 = predFactory.getPredicate("p", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunction(fs2, vars2);

		Predicate pred1 = predFactory.getClassPredicate("A");
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot1);
		Function atom1 = tfac.getFunction(pred1, terms1);

		Predicate pred2 = predFactory.getClassPredicate("A");
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot2);
		Function atom2 = tfac.getFunction(pred2, terms2);

		Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
		assertEquals(null, s);
	}

	//A(p(x)), A(q('123'))
	public void test_18(){

		Term t1 = termFactory.getVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunction(fs1, vars1);
		Term t2 = termFactory.getConstantLiteral("123");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		Predicate fs2 = predFactory.getPredicate("q", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunction(fs2, vars2);

		Predicate pred1 = predFactory.getClassPredicate("A");
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot1);
		Function atom1 = tfac.getFunction(pred1, terms1);

		Predicate pred2 = predFactory.getClassPredicate("A");
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot2);
		Function atom2 = tfac.getFunction(pred2, terms2);

		Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
		assertEquals(null, s);

	}

	//A(p(x,z)), A(p('123'))
	public void test_19(){

		Term t1 = termFactory.getVariable("x");
		Term t3 = termFactory.getVariable("z");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		vars1.add(t3);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunction(fs1, vars1);
		Term t2 = termFactory.getConstantLiteral("123");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		Predicate fs2 = predFactory.getPredicate("q", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunction(fs2, vars2);

		Predicate pred1 = predFactory.getClassPredicate("A");
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot1);
		Function atom1 = tfac.getFunction(pred1, terms1);

		Predicate pred2 = predFactory.getClassPredicate("A");
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot2);
		Function atom2 = tfac.getFunction(pred2, terms2);

		Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
		assertEquals(null, s);

	}

	//A(x), A(p(x))
	public void test_20(){

		Term t = termFactory.getVariable("x");
		List<Term> vars = new Vector<Term>();
		vars.add(t);
		Predicate fs = predFactory.getPredicate("p", vars.size());
		FunctionalTermImpl ot =(FunctionalTermImpl) termFactory.getFunction(fs, vars);
		Term t2 = termFactory.getVariable("x");

		Predicate pred1 = predFactory.getClassPredicate("A");
		List<Term> terms1 = new Vector<Term>();
		terms1.add(t2);
		Function atom1 = tfac.getFunction(pred1, terms1);

		Predicate pred2 = predFactory.getClassPredicate("A");
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot);
		Function atom2 = tfac.getFunction(pred2, terms2);

		Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
		assertEquals(null, s);
	}

	//A(y), A(p(x))
	public void test_21(){

		Term t = termFactory.getVariable("x");
		List<Term> vars = new Vector<Term>();
		vars.add(t);
		Predicate fs = predFactory.getPredicate("p", vars.size());
		FunctionalTermImpl ot =(FunctionalTermImpl) termFactory.getFunction(fs, vars);
		Term t2 = termFactory.getVariable("y");

		Predicate pred1 = predFactory.getClassPredicate("A");
		List<Term> terms1 = new Vector<Term>();
		terms1.add(t2);
		Function atom1 = tfac.getFunction(pred1, terms1);

		Predicate pred2 = predFactory.getClassPredicate("A");
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot);
		Function atom2 = tfac.getFunction(pred2, terms2);

		Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
		assertEquals(1, s.size());

		SingletonSubstitution sub = s.get(0);
		FunctionalTermImpl term = (FunctionalTermImpl) sub.getTerm();
		List<Term> para = term.getTerms();
		Term var = sub.getVariable();

		assertEquals("y", ((Variable) var).getName());
		assertEquals(1, para.size());
		assertEquals("x", ((Variable) para.get(0)).getName());

	}

	//A(q(x)), A(p(x))
	public void test_22(){

		Term t1 = termFactory.getVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunction(fs1, vars1);
		Term t2 = termFactory.getVariable("x");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		Predicate fs2 = predFactory.getPredicate("q", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunction(fs2, vars2);

		Predicate pred1 = predFactory.getClassPredicate("A");
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot2);
		Function atom1 = tfac.getFunction(pred1, terms1);

		Predicate pred2 = predFactory.getClassPredicate("A");
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot1);
		Function atom2 = tfac.getFunction(pred2, terms2);

		Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
		assertEquals(null, s);
	}

	//A(p(y)), A(p(x))
	public void test_24(){

		Term t1 = termFactory.getVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunction(fs1, vars1);
		Term t2 = termFactory.getVariable("y");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		Predicate fs2 = predFactory.getPredicate("p", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunction(fs2, vars2);

		Predicate pred1 = predFactory.getClassPredicate("A");
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot2);
		Function atom1 = tfac.getFunction(pred1, terms1);

		Predicate pred2 = predFactory.getClassPredicate("A");
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot1);
		Function atom2 = tfac.getFunction(pred2, terms2);

		Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
		assertEquals(1, s.size());

		SingletonSubstitution sub = s.get(0);
		Term term = sub.getTerm();
		Term var = sub.getVariable();

		assertEquals("x", ((Variable) term).getName());
		assertEquals("y", ((Variable) var).getName());
	}

	// A(p(y,z)), A(p(x))
	public void test_25(){

		Term t1 = termFactory.getVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunction(fs1, vars1);
		Term t2 = termFactory.getVariable("y");
		Term t3 = termFactory.getVariable("z");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		vars2.add(t3);
		Predicate fs2 = predFactory.getPredicate("p", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunction(fs2, vars2);

		Predicate pred1 = predFactory.getClassPredicate("A");
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot2);
		Function atom1 = tfac.getFunction(pred1, terms1);

		Predicate pred2 = predFactory.getClassPredicate("A");
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot1);
		Function atom2 = tfac.getFunction(pred2, terms2);

		Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
		assertEquals(null, s);
	}

	//A(p('123')), A(p(x))
	public void test_26(){

		Term t1 = termFactory.getVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunction(fs1, vars1);
		Term t2 = termFactory.getConstantLiteral("123");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		Predicate fs2 = predFactory.getPredicate("p", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunction(fs2, vars2);

		Predicate pred1 = predFactory.getClassPredicate("A");
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot2);
		Function atom1 = tfac.getFunction(pred1, terms1);

		Predicate pred2 = predFactory.getClassPredicate("A");
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot1);
		Function atom2 = tfac.getFunction(pred2, terms2);

		Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
		assertEquals(1, s.size());

		SingletonSubstitution sub = s.get(0);
		ValueConstant term = (ValueConstant) sub.getTerm();
		Term var = sub.getVariable();

		assertEquals("123", term.getValue());
		assertEquals("x", ((Variable) var).getName());
	}

	//A(p('123',z)),A(p(x))
	public void test_27(){

		Term t1 = termFactory.getVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunction(fs1, vars1);
		Term t2 = termFactory.getConstantLiteral("123");
		Term t3 = termFactory.getVariable("z");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		vars2.add(t3);
		Predicate fs2 = predFactory.getPredicate("p", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunction(fs2, vars2);

		Predicate pred1 = predFactory.getClassPredicate("A");
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot2);
		Function atom1 = tfac.getFunction(pred1, terms1);

		Predicate pred2 = predFactory.getClassPredicate("A");
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot1);
		Function atom2 = tfac.getFunction(pred2, terms2);

		Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
		assertEquals(null, s);
	}

	//A(q('123')),A(p(x))
	public void test_28(){

		Term t1 = termFactory.getVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunction(fs1, vars1);
		Term t2 = termFactory.getConstantLiteral("123");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		Predicate fs2 = predFactory.getPredicate("q", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunction(fs2, vars2);

		Predicate pred1 = predFactory.getClassPredicate("A");
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot2);
		Function atom1 = tfac.getFunction(pred1, terms1);

		Predicate pred2 = predFactory.getClassPredicate("A");
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot1);
		Function atom2 = tfac.getFunction(pred2, terms2);

		Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
		assertEquals(null, s);

	}

	//A(p('123')),A(p(x,z))
	public void test_29(){

		Term t1 = termFactory.getVariable("x");
		Term t3 = termFactory.getVariable("z");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		vars1.add(t3);
		Predicate fs1 = predFactory.getPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) termFactory.getFunction(fs1, vars1);
		Term t2 = termFactory.getConstantLiteral("123");
		List<Term> vars2 = new Vector<Term>();
		vars2.add(t2);
		Predicate fs2 = predFactory.getPredicate("q", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.getFunction(fs2, vars2);

		Predicate pred1 = predFactory.getClassPredicate("A");
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot2);
		Function atom1 = tfac.getFunction(pred1, terms1);

		Predicate pred2 = predFactory.getClassPredicate("A");
		List<Term> terms2 = new Vector<Term>();
		terms2.add(ot1);
		Function atom2 = tfac.getFunction(pred2, terms2);

		Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
		assertEquals(null, s);

	}

	//A(#),A(#)
	// ROMAN: removed the test which does not make any sense without anonymous variables
	public void non_test_32(){

		try {
			Term t1 = termFactory.getVariable("w1");
			Term t2 = termFactory.getVariable("w2");

			Predicate pred1 = predFactory.getClassPredicate("A");
			List<Term> terms1 = new Vector<Term>();
			terms1.add(t1);
			Function atom1 = tfac.getFunction(pred1, terms1);

			Predicate pred2 = predFactory.getClassPredicate("A");
			List<Term> terms2 = new Vector<Term>();
			terms2.add(t2);
			Function atom2 = tfac.getFunction(pred2, terms2);

			Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
			assertEquals(0, s.size());
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}

	}

	//A(x),A(#) 
	// ROMAN: removed the test which does not make any sense without anonymous variables
	public void non_test_33(){

		try {
			Term t1 = termFactory.getVariable("x");
			Term t2 = termFactory.getVariable("w1");

			Predicate pred1 = predFactory.getClassPredicate("A");
			List<Term> terms1 = new Vector<Term>();
			terms1.add(t1);
			Function atom1 = tfac.getFunction(pred1, terms1);

			Predicate pred2 = predFactory.getClassPredicate("A");
			List<Term> terms2 = new Vector<Term>();
			terms2.add(t2);
			Function atom2 = tfac.getFunction(pred2, terms2);

			Vector<SingletonSubstitution> s = getMGUAsVector(UnifierUtilities.getMGU(atom1, atom2));
			assertEquals(0, s.size());
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}
	}

}
