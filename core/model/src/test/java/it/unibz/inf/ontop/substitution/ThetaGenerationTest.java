package it.unibz.inf.ontop.substitution;

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

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.impl.FunctionalTermImpl;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.vocabulary.XSD;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import junit.framework.TestCase;

import static it.unibz.inf.ontop.OntopModelTestingTools.*;


public class ThetaGenerationTest extends TestCase {

	private static final String SUBQUERY_PRED_PREFIX = "ontopSubquery";

	private Vector<Map.Entry<Variable, ImmutableTerm>> getMGUAsVector(Term t1, Term t2) {
		Map<Variable, ImmutableTerm> mgu = UNIFIER_UTILITIES.getMGU(
				ImmutableList.of(IMMUTABILITY_TOOLS.convertIntoImmutableTerm(t1)),
				ImmutableList.of(IMMUTABILITY_TOOLS.convertIntoImmutableTerm(t2)));
		Vector<Map.Entry<Variable, ImmutableTerm>> computedmgu;
		if (mgu == null) {
			computedmgu = null;
		}
		else {
			computedmgu = new Vector<>(mgu.entrySet());
		}
		return computedmgu;
	}


	//A(x),A(x)
	public void test_1(){

		try {
			Term t1 = TERM_FACTORY.getVariable("x");
			Term t2 = TERM_FACTORY.getVariable("x");

			Predicate pred1 = createClassLikePredicate("A");
			List<Term> terms1 = new Vector<>();
			terms1.add(t1);
			Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

			Predicate pred2 = createClassLikePredicate("A");
			List<Term> terms2 = new Vector<>();
			terms2.add(t2);
			Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

			Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
			assertEquals(0, s.size());
		}
		catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}

	}

	//A(x),A(y)
	public void test_2(){

		try {
			Term t1 = TERM_FACTORY.getVariable("x");
			Term t2 = TERM_FACTORY.getVariable("y");

			Predicate pred1 = createClassLikePredicate("A");
			List<Term> terms1 = new Vector<>();
			terms1.add(t1);
			Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

			Predicate pred2 = createClassLikePredicate("A");
			List<Term> terms2 = new Vector<>();
			terms2.add(t2);
			Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

			Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
			assertEquals(1, s.size());

			Map.Entry<Variable, ImmutableTerm> s0 = s.get(0);
			ImmutableTerm t = s0.getValue();
			Variable v = s0.getKey();

			assertEquals("y", ((Variable) t).getName());
			assertEquals("x", v.getName());
		}
		catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}
	}

	private static FunctionSymbol createClassLikePredicate(String name) {
		return new OntopModelTestPredicate(SUBQUERY_PRED_PREFIX + name, 1);
	}



	//A(x),A('y')
	public void test_3(){

		
			Term t1 = TERM_FACTORY.getVariable("x");
			Term t2 = TERM_FACTORY.getRDFLiteralConstant("y", XSD.STRING);

			Predicate pred1 = createClassLikePredicate("A");
			List<Term> terms1 = new Vector<>();
			terms1.add(t1);
			Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

			Predicate pred2 = createClassLikePredicate("A");
			List<Term> terms2 = new Vector<>();
			terms2.add(t2);
			Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

			Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
			assertEquals(1, s.size());

			Map.Entry<Variable, ImmutableTerm> s0 = s.get(0);
			RDFLiteralConstant t = (RDFLiteralConstant) s0.getValue();
			Variable v = s0.getKey();

			assertEquals("y", t.getValue());
			assertEquals("x", v.getName());
		
	}

		//A(x),A('p(y)')
	public void test_4(){

//		try {
//			Term t1 = TERM_FACTORY.createVariable("x");
//			ValueConstant t2 = TERM_FACTORY.createValueConstant("y");
//			List<ValueConstant> list = new Vector<ValueConstant>();
//			list.add(t2);
//			Term ft = TERM_FACTORY.createObjectConstant(TERM_FACTORY.getFunctionSymbol("p"), list);
//
//			Predicate pred1 = TERM_FACTORY.createPredicate("A", 1);
//			List<Term> terms1 = new Vector<Term>();
//			terms1.add(t1);
//			Function atom1 = TERM_FACTORY.getFunctionalTerm(pred1, terms1);
//
//			Predicate pred2 = TERM_FACTORY.createPredicate("A", 1);
//			List<Term> terms2 = new Vector<Term>();
//			terms2.add(ft);
//			Function atom2 = TERM_FACTORY.getFunctionalTerm(pred2, terms2);
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

			Term t2 = TERM_FACTORY.getVariable("x");
			Term t1 = TERM_FACTORY.getRDFLiteralConstant("y", XSD.STRING);

			Predicate pred1 = createClassLikePredicate("A");
			List<Term> terms1 = new Vector<>();
			terms1.add(t1);
			Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

			Predicate pred2 = createClassLikePredicate("A");
			List<Term> terms2 = new Vector<>();
			terms2.add(t2);
			Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

			Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
			assertEquals(1, s.size());

			Map.Entry<Variable, ImmutableTerm> s0 = s.get(0);
			RDFLiteralConstant t = (RDFLiteralConstant) s0.getValue();
			Variable v = s0.getKey();

			assertEquals(t + " y", "y", t.getValue());
			assertEquals(t + " x", "x", v.getName());
	}

	//A('y'),A('y')
	public void test_6(){


			Term t2 = TERM_FACTORY.getRDFLiteralConstant("y", XSD.STRING);
			Term t1 = TERM_FACTORY.getRDFLiteralConstant("y", XSD.STRING);

			Predicate pred1 = createClassLikePredicate("A");
			List<Term> terms1 = new Vector<>();
			terms1.add(t1);
			Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

			Predicate pred2 = createClassLikePredicate("A");
			List<Term> terms2 = new Vector<>();
			terms2.add(t2);
			Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

			Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
			assertEquals(0, s.size());
	}

	//A('y'),A('p(x)')
	public void test_7(){

//		try {
//
//			Term t1 = TERM_FACTORY.createValueConstant("y");
//
//			ValueConstant t2 = TERM_FACTORY.createValueConstant("y");
//			List<ValueConstant> list = new Vector<ValueConstant>();
//			list.add(t2);
//			Term ft = TERM_FACTORY.createObjectConstant(TERM_FACTORY.getFunctionSymbol("p"), list);
//
//			Predicate pred1 = TERM_FACTORY.createPredicate("A", 1);
//			List<Term> terms1 = new Vector<Term>();
//			terms1.add(t1);
//			Function atom1 = TERM_FACTORY.getFunctionalTerm(pred1, terms1);
//
//			Predicate pred2 = TERM_FACTORY.createPredicate("A", 1);
//			List<Term> terms2 = new Vector<Term>();
//			terms2.add(ft);
//			Function atom2 = TERM_FACTORY.getFunctionalTerm(pred2, terms2);
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
			Term t2 = TERM_FACTORY.getRDFLiteralConstant("x", XSD.STRING);
			Term t1 = TERM_FACTORY.getRDFLiteralConstant("y", XSD.STRING);

			Predicate pred1 = createClassLikePredicate("A");
			List<Term> terms1 = new Vector<>();
			terms1.add(t1);
			Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

			Predicate pred2 = createClassLikePredicate("A");
			List<Term> terms2 = new Vector<>();
			terms2.add(t2);
			Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

			Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
			assertNull(s);
		}
		catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}
	}

	//A('y'),A(p(x))
	public void test_9(){

		try {
			Term t1 = TERM_FACTORY.getRDFLiteralConstant("y", XSD.STRING);
			Term t2 = TERM_FACTORY.getVariable("y");
			List<Term> vars = new Vector<>();
			vars.add(t2);
			Predicate fs = new OntopModelTestPredicate("p", vars.size());
			FunctionalTermImpl ot =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs, vars);
			Predicate pred1 = createClassLikePredicate("A");
			List<Term> terms1 = new Vector<>();
			terms1.add(t1);
			Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

			Predicate pred2 = createClassLikePredicate("A");
			List<Term> terms2 = new Vector<Term>();
			terms2.add(ot);
			Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

			Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
			assertNull(s);
		}
		catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}
	}

	//A(p(x)), A(x)
	public void test_10(){

		Term t = TERM_FACTORY.getVariable("x");
		List<Term> vars = new Vector<Term>();
		vars.add(t);
		Predicate fs = new OntopModelTestPredicate("p", vars.size());
		FunctionalTermImpl ot =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs, vars);
		Term t2 = TERM_FACTORY.getVariable("x");

		Predicate pred1 = createClassLikePredicate("A");
		List<Term> terms1 = new Vector<Term>();
		terms1.add(ot);
		Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

		Predicate pred2 = createClassLikePredicate("A");
		List<Term> terms2 = new Vector<Term>();
		terms2.add(t2);
		Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertNull(s);
	}

	//A(p(x)), A(y)
	public void test_11(){

		Term t = TERM_FACTORY.getVariable("x");
		List<Term> vars = new Vector<>();
		vars.add(t);
		Predicate fs = new OntopModelTestPredicate("p", vars.size());
		FunctionalTermImpl ot =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs, vars);
		Term t2 = TERM_FACTORY.getVariable("y");

		Predicate pred1 = createClassLikePredicate("A");
		List<Term> terms1 = new Vector<>();
		terms1.add(ot);
		Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

		Predicate pred2 = createClassLikePredicate("A");
		List<Term> terms2 = new Vector<>();
		terms2.add(t2);
		Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertEquals(1, s.size());

		Map.Entry<Variable, ImmutableTerm> sub = s.get(0);
		ImmutableFunctionalTerm term = (ImmutableFunctionalTerm) sub.getValue();
		List<? extends ImmutableTerm> para = term.getTerms();
		Variable var = sub.getKey();

		assertEquals("y", var.getName());
		assertEquals(1, para.size());
		assertEquals("x", ((Variable) para.get(0)).getName());

	}

	//A(p(x)), A(q(x))
	public void test_12(){

		Term t1 = TERM_FACTORY.getVariable("x");
		List<Term> vars1 = new Vector<Term>();
		vars1.add(t1);
		Predicate fs1 = new OntopModelTestPredicate("p", vars1.size());
		Function ot1 = TERM_FACTORY.getFunction(fs1, vars1);
		Term t2 = TERM_FACTORY.getVariable("x");
		List<Term> vars2 = new Vector<>();
		vars2.add(t2);
		Predicate fs2 = new OntopModelTestPredicate("q", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs2, vars2);

		Predicate pred1 = createClassLikePredicate("A");
		List<Term> terms1 = new Vector<>();
		terms1.add(ot1);
		Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

		Predicate pred2 = createClassLikePredicate("A");
		List<Term> terms2 = new Vector<>();
		terms2.add(ot2);
		Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertNull(s);
	}

	//A(p(x)), A(p(x))
	public void test_13(){

		Term t1 = TERM_FACTORY.getVariable("x");
		List<Term> vars1 = new Vector<>();
		vars1.add(t1);
		Predicate fs1 = new OntopModelTestPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs1, vars1);
		Term t2 = TERM_FACTORY.getVariable("x");
		List<Term> vars2 = new Vector<>();
		vars2.add(t2);
		Predicate fs2 = new OntopModelTestPredicate("p", vars1.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs2, vars2);

		Predicate pred1 = createClassLikePredicate("A");
		List<Term> terms1 = new Vector<>();
		terms1.add(ot1);
		Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

		Predicate pred2 = createClassLikePredicate("A");
		List<Term> terms2 = new Vector<>();
		terms2.add(ot2);
		Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertEquals(0, s.size());
	}

	//A(p(x)), A(p(y))
	public void test_14(){

		Term t1 = TERM_FACTORY.getVariable("x");
		List<Term> vars1 = new Vector<>();
		vars1.add(t1);
		Predicate fs1 = new OntopModelTestPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs1, vars1);
		Term t2 = TERM_FACTORY.getVariable("y");
		List<Term> vars2 = new Vector<>();
		vars2.add(t2);
		Predicate fs2 = new OntopModelTestPredicate("p", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs2, vars2);

		Predicate pred1 = createClassLikePredicate("A");
		List<Term> terms1 = new Vector<>();
		terms1.add(ot1);
		Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

		Predicate pred2 = createClassLikePredicate("A");
		List<Term> terms2 = new Vector<>();
		terms2.add(ot2);
		Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertEquals(1, s.size());

		Map.Entry<Variable, ImmutableTerm> sub = s.get(0);
		ImmutableTerm term = sub.getValue();
		Variable var = sub.getKey();

		assertEquals("y", ((Variable) term).getName());
		assertEquals("x", var.getName());
	}

	//A(p(x)), A(p(y,z))
	public void test_15(){

		Term t1 = TERM_FACTORY.getVariable("x");
		List<Term> vars1 = new Vector<>();
		vars1.add(t1);
		Predicate fs1 = new OntopModelTestPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs1, vars1);
		Term t2 = TERM_FACTORY.getVariable("y");
		Term t3 = TERM_FACTORY.getVariable("z");
		List<Term> vars2 = new Vector<>();
		vars2.add(t2);
		vars2.add(t3);
		Predicate fs2 = new OntopModelTestPredicate("p", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs2, vars2);

		Predicate pred1 = createClassLikePredicate("A");
		List<Term> terms1 = new Vector<>();
		terms1.add(ot1);
		Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

		Predicate pred2 = createClassLikePredicate("A");
		List<Term> terms2 = new Vector<>();
		terms2.add(ot2);
		Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertNull(s);
	}

	//A(p(x)), A(p('123'))
	public void test_16(){

		Term t1 = TERM_FACTORY.getVariable("x");
		List<Term> vars1 = new Vector<>();
		vars1.add(t1);
		Predicate fs1 = new OntopModelTestPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs1, vars1);
		Term t2 = TERM_FACTORY.getRDFLiteralConstant("123", XSD.STRING);
		List<Term> vars2 = new Vector<>();
		vars2.add(t2);
		Predicate fs2 = new OntopModelTestPredicate("p", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs2, vars2);

		Predicate pred1 = new OntopModelTestPredicate("A", 1);
		List<Term> terms1 = new Vector<>();
		terms1.add(ot1);
		Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

		Predicate pred2 = new OntopModelTestPredicate("A", 1);
		List<Term> terms2 = new Vector<>();
		terms2.add(ot2);
		Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertEquals(1, s.size());

		Map.Entry<Variable, ImmutableTerm> sub = s.get(0);
		RDFLiteralConstant term = (RDFLiteralConstant) sub.getValue();
		Variable var = sub.getKey();

		assertEquals("123", term.getValue());
		assertEquals("x", var.getName());
	}

	//A(p(x)), A(p('123',z))
	public void test_17(){

		Term t1 = TERM_FACTORY.getVariable("x");
		List<Term> vars1 = new Vector<>();
		vars1.add(t1);
		Predicate fs1 = new OntopModelTestPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs1, vars1);
		Term t2 = TERM_FACTORY.getRDFLiteralConstant("123", XSD.STRING);
		Term t3 = TERM_FACTORY.getVariable("z");
		List<Term> vars2 = new Vector<>();
		vars2.add(t2);
		vars2.add(t3);
		Predicate fs2 = new OntopModelTestPredicate("p", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs2, vars2);

		Predicate pred1 = createClassLikePredicate("A");
		List<Term> terms1 = new Vector<>();
		terms1.add(ot1);
		Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

		Predicate pred2 = createClassLikePredicate("A");
		List<Term> terms2 = new Vector<>();
		terms2.add(ot2);
		Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertNull(s);
	}

	//A(p(x)), A(q('123'))
	public void test_18(){

		Term t1 = TERM_FACTORY.getVariable("x");
		List<Term> vars1 = new Vector<>();
		vars1.add(t1);
		Predicate fs1 = new OntopModelTestPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs1, vars1);
		Term t2 = TERM_FACTORY.getRDFLiteralConstant("123", XSD.STRING);
		List<Term> vars2 = new Vector<>();
		vars2.add(t2);
		Predicate fs2 = new OntopModelTestPredicate("q", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs2, vars2);

		Predicate pred1 = createClassLikePredicate("A");
		List<Term> terms1 = new Vector<>();
		terms1.add(ot1);
		Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

		Predicate pred2 = createClassLikePredicate("A");
		List<Term> terms2 = new Vector<>();
		terms2.add(ot2);
		Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertNull(s);

	}

	//A(p(x,z)), A(p('123'))
	public void test_19(){

		Term t1 = TERM_FACTORY.getVariable("x");
		Term t3 = TERM_FACTORY.getVariable("z");
		List<Term> vars1 = new Vector<>();
		vars1.add(t1);
		vars1.add(t3);
		Predicate fs1 = new OntopModelTestPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs1, vars1);
		Term t2 = TERM_FACTORY.getRDFLiteralConstant("123", XSD.STRING);
		List<Term> vars2 = new Vector<>();
		vars2.add(t2);
		Predicate fs2 = new OntopModelTestPredicate("q", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs2, vars2);

		Predicate pred1 = createClassLikePredicate("A");
		List<Term> terms1 = new Vector<>();
		terms1.add(ot1);
		Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

		Predicate pred2 = createClassLikePredicate("A");
		List<Term> terms2 = new Vector<>();
		terms2.add(ot2);
		Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertNull(s);
	}

	//A(x), A(p(x))
	public void test_20(){

		Term t = TERM_FACTORY.getVariable("x");
		List<Term> vars = new Vector<>();
		vars.add(t);
		Predicate fs = new OntopModelTestPredicate("p", vars.size());
		FunctionalTermImpl ot =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs, vars);
		Term t2 = TERM_FACTORY.getVariable("x");

		Predicate pred1 = createClassLikePredicate("A");
		List<Term> terms1 = new Vector<>();
		terms1.add(t2);
		Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

		Predicate pred2 = createClassLikePredicate("A");
		List<Term> terms2 = new Vector<>();
		terms2.add(ot);
		Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertNull(s);
	}

	//A(y), A(p(x))
	public void test_21(){

		Term t = TERM_FACTORY.getVariable("x");
		List<Term> vars = new Vector<>();
		vars.add(t);
		Predicate fs = new OntopModelTestPredicate("p", vars.size());
		FunctionalTermImpl ot =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs, vars);
		Term t2 = TERM_FACTORY.getVariable("y");

		Predicate pred1 = createClassLikePredicate("A");
		Function atom1 = TERM_FACTORY.getFunction(pred1, t2);

		Predicate pred2 = createClassLikePredicate("A");
		Function atom2 = TERM_FACTORY.getFunction(pred2, ot);

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertEquals(1, s.size());

		Map.Entry<Variable, ImmutableTerm> sub = s.get(0);
		ImmutableFunctionalTerm term = (ImmutableFunctionalTerm) sub.getValue();
		ImmutableList<? extends ImmutableTerm> para = term.getTerms();
		Variable var = sub.getKey();

		assertEquals("y", var.getName());
		assertEquals(1, para.size());
		assertEquals("x", ((Variable) para.get(0)).getName());

	}

	//A(q(x)), A(p(x))
	public void test_22(){

		Term t1 = TERM_FACTORY.getVariable("x");
		List<Term> vars1 = new Vector<>();
		vars1.add(t1);
		Predicate fs1 = new OntopModelTestPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs1, vars1);
		Term t2 = TERM_FACTORY.getVariable("x");
		List<Term> vars2 = new Vector<>();
		vars2.add(t2);
		Predicate fs2 = new OntopModelTestPredicate("q", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs2, vars2);

		Predicate pred1 = createClassLikePredicate("A");
		List<Term> terms1 = new Vector<>();
		terms1.add(ot2);
		Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

		Predicate pred2 = createClassLikePredicate("A");
		List<Term> terms2 = new Vector<>();
		terms2.add(ot1);
		Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertNull(s);
	}

	//A(p(y)), A(p(x))
	public void test_24(){

		Term t1 = TERM_FACTORY.getVariable("x");
		List<Term> vars1 = new Vector<>();
		vars1.add(t1);
		Predicate fs1 = new OntopModelTestPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs1, vars1);
		Term t2 = TERM_FACTORY.getVariable("y");
		List<Term> vars2 = new Vector<>();
		vars2.add(t2);
		Predicate fs2 = new OntopModelTestPredicate("p", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs2, vars2);

		Predicate pred1 = createClassLikePredicate("A");
		List<Term> terms1 = new Vector<>();
		terms1.add(ot2);
		Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

		Predicate pred2 = createClassLikePredicate("A");
		List<Term> terms2 = new Vector<>();
		terms2.add(ot1);
		Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertEquals(1, s.size());

		Map.Entry<Variable, ImmutableTerm> sub = s.get(0);
		ImmutableTerm term = sub.getValue();
		Variable var = sub.getKey();

		assertEquals("x", ((Variable) term).getName());
		assertEquals("y", var.getName());
	}

	// A(p(y,z)), A(p(x))
	public void test_25(){

		Term t1 = TERM_FACTORY.getVariable("x");
		List<Term> vars1 = new Vector<>();
		vars1.add(t1);
		Predicate fs1 = new OntopModelTestPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs1, vars1);
		Term t2 = TERM_FACTORY.getVariable("y");
		Term t3 = TERM_FACTORY.getVariable("z");
		List<Term> vars2 = new Vector<>();
		vars2.add(t2);
		vars2.add(t3);
		Predicate fs2 = new OntopModelTestPredicate("p", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs2, vars2);

		Predicate pred1 = createClassLikePredicate("A");
		List<Term> terms1 = new Vector<>();
		terms1.add(ot2);
		Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

		Predicate pred2 = createClassLikePredicate("A");
		List<Term> terms2 = new Vector<>();
		terms2.add(ot1);
		Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertNull(s);
	}

	//A(p('123')), A(p(x))
	public void test_26(){

		Term t1 = TERM_FACTORY.getVariable("x");
		List<Term> vars1 = new Vector<>();
		vars1.add(t1);
		Predicate fs1 = new OntopModelTestPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs1, vars1);
		Term t2 = TERM_FACTORY.getRDFLiteralConstant("123", XSD.STRING);
		List<Term> vars2 = new Vector<>();
		vars2.add(t2);
		Predicate fs2 = new OntopModelTestPredicate("p", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs2, vars2);

		Predicate pred1 = createClassLikePredicate("A");
		List<Term> terms1 = new Vector<>();
		terms1.add(ot2);
		Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

		Predicate pred2 = createClassLikePredicate("A");
		List<Term> terms2 = new Vector<>();
		terms2.add(ot1);
		Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertEquals(1, s.size());

		Map.Entry<Variable, ImmutableTerm> sub = s.get(0);
		RDFLiteralConstant term = (RDFLiteralConstant) sub.getValue();
		Variable var = sub.getKey();

		assertEquals("123", term.getValue());
		assertEquals("x", var.getName());
	}

	//A(p('123',z)),A(p(x))
	public void test_27(){

		Term t1 = TERM_FACTORY.getVariable("x");
		List<Term> vars1 = new Vector<>();
		vars1.add(t1);
		Predicate fs1 = new OntopModelTestPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs1, vars1);
		Term t2 = TERM_FACTORY.getRDFLiteralConstant("123", XSD.STRING);
		Term t3 = TERM_FACTORY.getVariable("z");
		List<Term> vars2 = new Vector<>();
		vars2.add(t2);
		vars2.add(t3);
		Predicate fs2 = new OntopModelTestPredicate("p", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs2, vars2);

		Predicate pred1 = createClassLikePredicate("A");
		List<Term> terms1 = new Vector<>();
		terms1.add(ot2);
		Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

		Predicate pred2 = createClassLikePredicate("A");
		List<Term> terms2 = new Vector<>();
		terms2.add(ot1);
		Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertNull(s);
	}

	//A(q('123')),A(p(x))
	public void test_28(){

		Term t1 = TERM_FACTORY.getVariable("x");
		List<Term> vars1 = new Vector<>();
		vars1.add(t1);
		Predicate fs1 = new OntopModelTestPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs1, vars1);
		Term t2 = TERM_FACTORY.getRDFLiteralConstant("123", XSD.STRING);
		List<Term> vars2 = new Vector<>();
		vars2.add(t2);
		Predicate fs2 = new OntopModelTestPredicate("q", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs2, vars2);

		Predicate pred1 = createClassLikePredicate("A");
		List<Term> terms1 = new Vector<>();
		terms1.add(ot2);
		Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

		Predicate pred2 = createClassLikePredicate("A");
		List<Term> terms2 = new Vector<>();
		terms2.add(ot1);
		Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertNull(s);
	}

	//A(p('123')),A(p(x,z))
	public void test_29(){

		Term t1 = TERM_FACTORY.getVariable("x");
		Term t3 = TERM_FACTORY.getVariable("z");
		List<Term> vars1 = new Vector<>();
		vars1.add(t1);
		vars1.add(t3);
		Predicate fs1 = new OntopModelTestPredicate("p", vars1.size());
		FunctionalTermImpl ot1 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs1, vars1);
		Term t2 = TERM_FACTORY.getRDFLiteralConstant("123", XSD.STRING);
		List<Term> vars2 = new Vector<>();
		vars2.add(t2);
		Predicate fs2 = new OntopModelTestPredicate("q", vars2.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) TERM_FACTORY.getFunction(fs2, vars2);

		Predicate pred1 = createClassLikePredicate("A");
		List<Term> terms1 = new Vector<>();
		terms1.add(ot2);
		Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

		Predicate pred2 = createClassLikePredicate("A");
		List<Term> terms2 = new Vector<>();
		terms2.add(ot1);
		Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertNull(s);
	}

	//A(#),A(#)
	// ROMAN: removed the test which does not make any sense without anonymous variables
	public void non_test_32(){

		try {
			Term t1 = TERM_FACTORY.getVariable("w1");
			Term t2 = TERM_FACTORY.getVariable("w2");

			Predicate pred1 = createClassLikePredicate("A");
			List<Term> terms1 = new Vector<>();
			terms1.add(t1);
			Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

			Predicate pred2 = createClassLikePredicate("A");
			List<Term> terms2 = new Vector<>();
			terms2.add(t2);
			Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

			Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
			assertEquals(0, s.size());
		}
		catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}

	}

	//A(x),A(#) 
	// ROMAN: removed the test which does not make any sense without anonymous variables
	public void non_test_33(){

		try {
			Term t1 = TERM_FACTORY.getVariable("x");
			Term t2 = TERM_FACTORY.getVariable("w1");

			Predicate pred1 = createClassLikePredicate("A");
			List<Term> terms1 = new Vector<>();
			terms1.add(t1);
			Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);

			Predicate pred2 = createClassLikePredicate("A");
			List<Term> terms2 = new Vector<>();
			terms2.add(t2);
			Function atom2 = TERM_FACTORY.getFunction(pred2, terms2);

			Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
			assertEquals(0, s.size());
		}
		catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}
	}
}
