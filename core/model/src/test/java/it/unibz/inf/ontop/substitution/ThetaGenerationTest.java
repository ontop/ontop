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
import it.unibz.inf.ontop.model.vocabulary.XSD;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;

import junit.framework.TestCase;

import static it.unibz.inf.ontop.OntopModelTestingTools.*;


public class ThetaGenerationTest extends TestCase {

	private static final String SUBQUERY_PRED_PREFIX = "ontopSubquery";

	private Vector<Map.Entry<Variable, ImmutableTerm>> getMGUAsVector(ImmutableFunctionalTerm t1, ImmutableFunctionalTerm t2) {
		Optional<ImmutableSubstitution<ImmutableTerm>> mgu = UNIFICATION_TOOLS.computeMGU(
				ImmutableList.of(t1), ImmutableList.of(t2));
		return mgu.map(s -> new Vector<>(s.getImmutableMap().entrySet())).orElse(null);
	}

	private static FunctionSymbol createClassLikePredicate(String name) {
		return new OntopModelTestFunctionSymbol(SUBQUERY_PRED_PREFIX + name, 1);
	}




	//A(x),A(x)
	public void test_1(){
			ImmutableTerm t1 = TERM_FACTORY.getVariable("x");
			ImmutableTerm t2 = TERM_FACTORY.getVariable("x");

			FunctionSymbol pred1 = createClassLikePredicate("A");
			ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
					ImmutableList.of(t1));

			FunctionSymbol pred2 = createClassLikePredicate("A");
			ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
					ImmutableList.of(t2));

			Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
			assertEquals(0, s.size());
	}

	//A(x),A(y)
	public void test_2(){
			ImmutableTerm t1 = TERM_FACTORY.getVariable("x");
			ImmutableTerm t2 = TERM_FACTORY.getVariable("y");

			FunctionSymbol pred1 = createClassLikePredicate("A");
			ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
					ImmutableList.of(t1));

			FunctionSymbol pred2 = createClassLikePredicate("A");
			ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
					ImmutableList.of(t2));

			Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
			assertEquals(1, s.size());

			Map.Entry<Variable, ImmutableTerm> s0 = s.get(0);
			ImmutableTerm t = s0.getValue();
			Variable v = s0.getKey();

			assertEquals("y", ((Variable) t).getName());
			assertEquals("x", v.getName());
	}


	//A(x),A('y')
	public void test_3(){
		ImmutableTerm t1 = TERM_FACTORY.getVariable("x");
		ImmutableTerm t2 = TERM_FACTORY.getRDFLiteralConstant("y", XSD.STRING);

		FunctionSymbol pred1 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
				ImmutableList.of(t1));

		FunctionSymbol pred2 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
				ImmutableList.of(t2));

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
		ImmutableTerm t2 = TERM_FACTORY.getVariable("x");
		ImmutableTerm t1 = TERM_FACTORY.getRDFLiteralConstant("y", XSD.STRING);

		FunctionSymbol pred1 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
				ImmutableList.of(t1));

		FunctionSymbol pred2 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
				ImmutableList.of(t2));

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


		ImmutableTerm t2 = TERM_FACTORY.getRDFLiteralConstant("y", XSD.STRING);
		ImmutableTerm t1 = TERM_FACTORY.getRDFLiteralConstant("y", XSD.STRING);

		FunctionSymbol pred1 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
				ImmutableList.of(t1));

		FunctionSymbol pred2 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
				ImmutableList.of(t2));

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
			ImmutableTerm t2 = TERM_FACTORY.getRDFLiteralConstant("x", XSD.STRING);
			ImmutableTerm t1 = TERM_FACTORY.getRDFLiteralConstant("y", XSD.STRING);

			FunctionSymbol pred1 = createClassLikePredicate("A");
			ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
					ImmutableList.of(t1));

			FunctionSymbol pred2 = createClassLikePredicate("A");
			ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
					ImmutableList.of(t2));

			Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
			assertNull(s);
	}

	//A('y'),A(p(x))
	public void test_9(){
			ImmutableTerm t1 = TERM_FACTORY.getRDFLiteralConstant("y", XSD.STRING);
			ImmutableTerm t2 = TERM_FACTORY.getVariable("y");

			FunctionSymbol fs = new OntopModelTestFunctionSymbol("p", 1);
			ImmutableFunctionalTerm ot = TERM_FACTORY.getImmutableFunctionalTerm(fs,
					ImmutableList.of(t2));

			FunctionSymbol pred1 = createClassLikePredicate("A");
			ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
					ImmutableList.of(t1));

			FunctionSymbol pred2 = createClassLikePredicate("A");
			ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
					ImmutableList.of(ot));

			Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
			assertNull(s);
	}

	//A(p(x)), A(x)
	public void test_10(){

		ImmutableTerm t = TERM_FACTORY.getVariable("x");
		FunctionSymbol fs = new OntopModelTestFunctionSymbol("p", 1);
		ImmutableFunctionalTerm ot = TERM_FACTORY.getImmutableFunctionalTerm(fs, ImmutableList.of(t));

		ImmutableTerm t2 = TERM_FACTORY.getVariable("x");

		FunctionSymbol pred1 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
				ImmutableList.of(ot));

		FunctionSymbol pred2 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
				ImmutableList.of(t2));

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertNull(s);
	}

	//A(p(x)), A(y)
	public void test_11(){

		ImmutableTerm t = TERM_FACTORY.getVariable("x");
		FunctionSymbol fs = new OntopModelTestFunctionSymbol("p", 1);
		ImmutableFunctionalTerm ot = TERM_FACTORY.getImmutableFunctionalTerm(fs,
				ImmutableList.of(t));
		ImmutableTerm t2 = TERM_FACTORY.getVariable("y");

		FunctionSymbol pred1 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
				ImmutableList.of(ot));

		FunctionSymbol pred2 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
				ImmutableList.of(t2));

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

		ImmutableTerm t1 = TERM_FACTORY.getVariable("x");
		FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 1);
		ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1,
				ImmutableList.of(t1));

		ImmutableTerm t2 = TERM_FACTORY.getVariable("x");
		FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("q", 1);
		ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2,
				ImmutableList.of(t2));

		FunctionSymbol pred1 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
				ImmutableList.of(ot1));

		FunctionSymbol pred2 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
				ImmutableList.of(ot2));

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertNull(s);
	}

	//A(p(x)), A(p(x))
	public void test_13(){

		ImmutableTerm t1 = TERM_FACTORY.getVariable("x");
		FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 1);
		ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1,
				ImmutableList.of(t1));

		ImmutableTerm t2 = TERM_FACTORY.getVariable("x");
		FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 1);
		ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2,
				ImmutableList.of(t2));

		FunctionSymbol pred1 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
				ImmutableList.of(ot1));

		FunctionSymbol pred2 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
				ImmutableList.of(ot2));

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertEquals(0, s.size());
	}

	//A(p(x)), A(p(y))
	public void test_14(){

		ImmutableTerm t1 = TERM_FACTORY.getVariable("x");
		FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 1);
		ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1,
				ImmutableList.of(t1));

		ImmutableTerm t2 = TERM_FACTORY.getVariable("y");
		FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 1);
		ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2,
				ImmutableList.of(t2));

		FunctionSymbol pred1 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
				ImmutableList.of(ot1));

		FunctionSymbol pred2 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
				ImmutableList.of(ot2));

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

		ImmutableTerm t1 = TERM_FACTORY.getVariable("x");
		FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 1);
		ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1,
				ImmutableList.of(t1));

		ImmutableTerm t2 = TERM_FACTORY.getVariable("y");
		ImmutableTerm t3 = TERM_FACTORY.getVariable("z");
		FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 2);
		ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2,
				ImmutableList.of(t2, t3));

		FunctionSymbol pred1 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
				ImmutableList.of(ot1));

		FunctionSymbol pred2 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
				ImmutableList.of(ot2));

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertNull(s);
	}

	//A(p(x)), A(p('123'))
	public void test_16(){

		ImmutableTerm t1 = TERM_FACTORY.getVariable("x");
		FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 1);
		ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1,
				ImmutableList.of(t1));

		ImmutableTerm t2 = TERM_FACTORY.getRDFLiteralConstant("123", XSD.STRING);
		FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 1);
		ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2,
				ImmutableList.of(t2));

		FunctionSymbol pred1 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
				ImmutableList.of(ot1));

		FunctionSymbol pred2 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
				ImmutableList.of(ot2));

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

		ImmutableTerm t1 = TERM_FACTORY.getVariable("x");
		FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 1);
		ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1,
				ImmutableList.of(t1));

		ImmutableTerm t2 = TERM_FACTORY.getRDFLiteralConstant("123", XSD.STRING);
		ImmutableTerm t3 = TERM_FACTORY.getVariable("z");
		FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 2);
		ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2,
				ImmutableList.of(t2, t3));

		FunctionSymbol pred1 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
				ImmutableList.of(ot1));

		FunctionSymbol pred2 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
				ImmutableList.of(ot2));

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertNull(s);
	}

	//A(p(x)), A(q('123'))
	public void test_18(){

		ImmutableTerm t1 = TERM_FACTORY.getVariable("x");
		FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 1);
		ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1,
				ImmutableList.of(t1));

		ImmutableTerm t2 = TERM_FACTORY.getRDFLiteralConstant("123", XSD.STRING);
		FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("q", 1);
		ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2,
				ImmutableList.of(t2));

		FunctionSymbol pred1 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
				ImmutableList.of(ot1));

		FunctionSymbol pred2 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
				ImmutableList.of(ot2));

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertNull(s);

	}

	//A(p(x,z)), A(p('123'))
	public void test_19(){


		ImmutableTerm t1 = TERM_FACTORY.getVariable("x");
		ImmutableTerm t3 = TERM_FACTORY.getVariable("z");
		FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 2);
		ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1,
				ImmutableList.of(t1, t3));

		ImmutableTerm t2 = TERM_FACTORY.getRDFLiteralConstant("123", XSD.STRING);
		FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 1);
		ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2,
				ImmutableList.of(t2));

		FunctionSymbol pred1 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
				ImmutableList.of(ot1));

		FunctionSymbol pred2 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
				ImmutableList.of(ot2));

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertNull(s);
	}

	//A(x), A(p(x))
	public void test_20(){

		ImmutableTerm t1 = TERM_FACTORY.getVariable("x");

		ImmutableTerm t2 = TERM_FACTORY.getVariable("x");
		FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 1);
		ImmutableFunctionalTerm ot = TERM_FACTORY.getImmutableFunctionalTerm(fs2,
				ImmutableList.of(t2));

		FunctionSymbol pred1 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
				ImmutableList.of(t1));

		FunctionSymbol pred2 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
				ImmutableList.of(ot));

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertNull(s);
	}

	//A(y), A(p(x))
	public void test_21(){

		ImmutableTerm t1 = TERM_FACTORY.getVariable("y");

		ImmutableTerm t2 = TERM_FACTORY.getVariable("x");
		FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 1);
		ImmutableFunctionalTerm ot = TERM_FACTORY.getImmutableFunctionalTerm(fs2,
				ImmutableList.of(t2));

		FunctionSymbol pred1 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
				ImmutableList.of(t1));

		FunctionSymbol pred2 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
				ImmutableList.of(ot));

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

		ImmutableTerm t1 = TERM_FACTORY.getVariable("x");
		FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("q", 1);
		ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1,
				ImmutableList.of(t1));

		ImmutableTerm t2 = TERM_FACTORY.getVariable("x");
		FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 1);
		ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2,
				ImmutableList.of(t2));

		FunctionSymbol pred1 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
				ImmutableList.of(ot1));

		FunctionSymbol pred2 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
				ImmutableList.of(ot2));

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertNull(s);
	}

	//A(p(y)), A(p(x))
	public void test_24(){

		ImmutableTerm t1 = TERM_FACTORY.getVariable("y");
		FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 1);
		ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1,
				ImmutableList.of(t1));

		ImmutableTerm t2 = TERM_FACTORY.getVariable("x");
		FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 1);
		ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2,
				ImmutableList.of(t2));

		FunctionSymbol pred1 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
				ImmutableList.of(ot1));

		FunctionSymbol pred2 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
				ImmutableList.of(ot2));

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

		ImmutableTerm t1 = TERM_FACTORY.getVariable("y");
		ImmutableTerm t3 = TERM_FACTORY.getVariable("z");
		FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 2);
		ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1,
				ImmutableList.of(t1, t3));

		ImmutableTerm t2 = TERM_FACTORY.getVariable("x");
		FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 1);
		ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2,
				ImmutableList.of(t2));

		FunctionSymbol pred1 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
				ImmutableList.of(ot1));

		FunctionSymbol pred2 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
				ImmutableList.of(ot2));

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertNull(s);
	}

	//A(p('123')), A(p(x))
	public void test_26(){

		ImmutableTerm t1 = TERM_FACTORY.getRDFLiteralConstant("123", XSD.STRING);
		FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 1);
		ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1,
				ImmutableList.of(t1));

		ImmutableTerm t2 = TERM_FACTORY.getVariable("x");
		FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 1);
		ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2,
				ImmutableList.of(t2));

		FunctionSymbol pred1 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
				ImmutableList.of(ot1));

		FunctionSymbol pred2 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
				ImmutableList.of(ot2));

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

		ImmutableTerm t1 = TERM_FACTORY.getRDFLiteralConstant("123", XSD.STRING);
		ImmutableTerm t3 = TERM_FACTORY.getVariable("x");
		FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 2);
		ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1,
				ImmutableList.of(t1, t3));

		ImmutableTerm t2 = TERM_FACTORY.getVariable("x");
		FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 1);
		ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2,
				ImmutableList.of(t2));

		FunctionSymbol pred1 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
				ImmutableList.of(ot1));

		FunctionSymbol pred2 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
				ImmutableList.of(ot2));

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertNull(s);
	}

	//A(q('123')),A(p(x))
	public void test_28(){

		ImmutableTerm t1 = TERM_FACTORY.getRDFLiteralConstant("123", XSD.STRING);
		FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("q", 1);
		ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1,
				ImmutableList.of(t1));

		ImmutableTerm t2 = TERM_FACTORY.getVariable("x");
		FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 1);
		ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2,
				ImmutableList.of(t2));

		FunctionSymbol pred1 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
				ImmutableList.of(ot1));

		FunctionSymbol pred2 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
				ImmutableList.of(ot2));

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertNull(s);
	}

	//A(p('123')),A(p(x,z))
	public void test_29(){

		ImmutableTerm t1 = TERM_FACTORY.getRDFLiteralConstant("123", XSD.STRING);
		FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 1);
		ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1,
				ImmutableList.of(t1));

		ImmutableTerm t2 = TERM_FACTORY.getVariable("x");
		ImmutableTerm t3 = TERM_FACTORY.getVariable("z");
		FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 2);
		ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2,
				ImmutableList.of(t2, t3));

		FunctionSymbol pred1 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom1 = TERM_FACTORY.getImmutableFunctionalTerm(pred1,
				ImmutableList.of(ot1));

		FunctionSymbol pred2 = createClassLikePredicate("A");
		ImmutableFunctionalTerm atom2 = TERM_FACTORY.getImmutableFunctionalTerm(pred2,
				ImmutableList.of(ot2));

		Vector<Map.Entry<Variable, ImmutableTerm>> s = getMGUAsVector(atom1, atom2);
		assertNull(s);
	}
}
