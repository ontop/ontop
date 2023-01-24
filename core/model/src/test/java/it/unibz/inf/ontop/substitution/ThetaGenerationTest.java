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
import org.junit.jupiter.api.Test;

import java.util.Optional;


import static it.unibz.inf.ontop.OntopModelTestingTools.*;
import static org.junit.jupiter.api.Assertions.*;


public class ThetaGenerationTest {

    //A(x),A(x)
    @Test
    public void test_1() {
        ImmutableTerm t1 = TERM_FACTORY.getVariable("x");
        ImmutableTerm t2 = TERM_FACTORY.getVariable("x");

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(t1), ImmutableList.of(t2));
        assertTrue(s.get().isEmpty());
    }

    //A(x),A(y)
    @Test
    public void test_2() {
        Variable t1 = TERM_FACTORY.getVariable("x");
        Variable t2 = TERM_FACTORY.getVariable("y");

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(t1), ImmutableList.of(t2));
        assertEquals(SUBSTITUTION_FACTORY.getSubstitution(t1, t2), s.get());
    }


    //A(x),A('y')
    @Test
    public void test_3() {
        Variable t1 = TERM_FACTORY.getVariable("x");
        RDFLiteralConstant t2 = TERM_FACTORY.getRDFLiteralConstant("y", XSD.STRING);

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(t1), ImmutableList.of(t2));
        assertEquals(SUBSTITUTION_FACTORY.getSubstitution(t1, t2), s.get());
    }

    //A(x),A('p(y)')
    public void test_4() {

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
    @Test
    public void test_5() {
        Variable t2 = TERM_FACTORY.getVariable("x");
        RDFLiteralConstant t1 = TERM_FACTORY.getRDFLiteralConstant("y", XSD.STRING);

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(t1), ImmutableList.of(t2));
        assertEquals(SUBSTITUTION_FACTORY.getSubstitution(t2, t1), s.get());
    }

    //A('y'),A('y')
    @Test
    public void test_6() {
        RDFLiteralConstant t2 = TERM_FACTORY.getRDFLiteralConstant("y", XSD.STRING);
        RDFLiteralConstant t1 = TERM_FACTORY.getRDFLiteralConstant("y", XSD.STRING);

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(t1), ImmutableList.of(t2));
        assertTrue(s.get().isEmpty());
    }

    //A('y'),A('p(x)')
    public void test_7() {

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
    @Test
    public void test_8() {
        RDFLiteralConstant t2 = TERM_FACTORY.getRDFLiteralConstant("x", XSD.STRING);
        RDFLiteralConstant t1 = TERM_FACTORY.getRDFLiteralConstant("y", XSD.STRING);

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(t1), ImmutableList.of(t2));
        assertFalse(s.isPresent());
    }

    //A('y'),A(p(x))
    @Test
    public void test_9() {
        RDFLiteralConstant t1 = TERM_FACTORY.getRDFLiteralConstant("y", XSD.STRING);
        Variable t2 = TERM_FACTORY.getVariable("y");
        FunctionSymbol fs = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot = TERM_FACTORY.getImmutableFunctionalTerm(fs, ImmutableList.of(t2));

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(t1), ImmutableList.of(ot));
        assertFalse(s.isPresent());
    }

    //A(p(x)), A(x)
    @Test
    public void test_10() {
        Variable t = TERM_FACTORY.getVariable("x");
        FunctionSymbol fs = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot = TERM_FACTORY.getImmutableFunctionalTerm(fs, ImmutableList.of(t));
        Variable t2 = TERM_FACTORY.getVariable("x");

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(ot), ImmutableList.of(t2));
        assertFalse(s.isPresent());
    }

    //A(p(x)), A(y)
    @Test
    public void test_11() {
        Variable t = TERM_FACTORY.getVariable("x");
        FunctionSymbol fs = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot = TERM_FACTORY.getImmutableFunctionalTerm(fs, ImmutableList.of(t));
        Variable t2 = TERM_FACTORY.getVariable("y");

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(ot), ImmutableList.of(t2));
        assertEquals(SUBSTITUTION_FACTORY.getSubstitution(t2, ot), s.get());
    }

    //A(p(x)), A(q(x))
    @Test
    public void test_12() {
        Variable t1 = TERM_FACTORY.getVariable("x");
        FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1, ImmutableList.of(t1));

        Variable t2 = TERM_FACTORY.getVariable("x");
        FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("q", 1);
        ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2, ImmutableList.of(t2));

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(ot1), ImmutableList.of(ot2));
        assertFalse(s.isPresent());
    }

    //A(p(x)), A(p(x))
    @Test
    public void test_13() {
        Variable t1 = TERM_FACTORY.getVariable("x");
        FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1, ImmutableList.of(t1));

        Variable t2 = TERM_FACTORY.getVariable("x");
        FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2, ImmutableList.of(t2));

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(ot1), ImmutableList.of(ot2));
        assertTrue(s.get().isEmpty());
    }

    //A(p(x)), A(p(y))
    @Test
    public void test_14() {
        Variable t1 = TERM_FACTORY.getVariable("x");
        FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1, ImmutableList.of(t1));

        Variable t2 = TERM_FACTORY.getVariable("y");
        FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2, ImmutableList.of(t2));

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(ot1), ImmutableList.of(ot2));
        assertEquals(SUBSTITUTION_FACTORY.getSubstitution(t1, t2), s.get());
    }

    //A(p(x)), A(p(y,z))
    @Test
    public void test_15() {
        Variable t1 = TERM_FACTORY.getVariable("x");
        FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1, ImmutableList.of(t1));

        Variable t2 = TERM_FACTORY.getVariable("y");
        Variable t3 = TERM_FACTORY.getVariable("z");
        FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 2);
        ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2, ImmutableList.of(t2, t3));

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(ot1), ImmutableList.of(ot2));
        assertFalse(s.isPresent());
    }

    //A(p(x)), A(p('123'))
    @Test
    public void test_16() {
        Variable t1 = TERM_FACTORY.getVariable("x");
        FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1, ImmutableList.of(t1));

        RDFLiteralConstant t2 = TERM_FACTORY.getRDFLiteralConstant("123", XSD.STRING);
        FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2, ImmutableList.of(t2));

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(ot1), ImmutableList.of(ot2));
        assertEquals(SUBSTITUTION_FACTORY.getSubstitution(t1, t2), s.get());
    }

    //A(p(x)), A(p('123',z))
    @Test
    public void test_17() {
        Variable t1 = TERM_FACTORY.getVariable("x");
        FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1, ImmutableList.of(t1));

        RDFLiteralConstant t2 = TERM_FACTORY.getRDFLiteralConstant("123", XSD.STRING);
        ImmutableTerm t3 = TERM_FACTORY.getVariable("z");
        FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 2);
        ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2, ImmutableList.of(t2, t3));

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(ot1), ImmutableList.of(ot2));
        assertFalse(s.isPresent());
    }

    //A(p(x)), A(q('123'))
    @Test
    public void test_18() {
        Variable t1 = TERM_FACTORY.getVariable("x");
        FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1, ImmutableList.of(t1));

        RDFLiteralConstant t2 = TERM_FACTORY.getRDFLiteralConstant("123", XSD.STRING);
        FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("q", 1);
        ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2, ImmutableList.of(t2));

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(ot1), ImmutableList.of(ot2));
        assertFalse(s.isPresent());
    }

    //A(p(x,z)), A(p('123'))
    @Test
    public void test_19() {
        Variable t1 = TERM_FACTORY.getVariable("x");
        Variable t3 = TERM_FACTORY.getVariable("z");
        FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 2);
        ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1, ImmutableList.of(t1, t3));

        ImmutableTerm t2 = TERM_FACTORY.getRDFLiteralConstant("123", XSD.STRING);
        FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2, ImmutableList.of(t2));

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(ot1), ImmutableList.of(ot2));
        assertFalse(s.isPresent());
    }

    //A(x), A(p(x))
    @Test
    public void test_20() {
        Variable t1 = TERM_FACTORY.getVariable("x");

        Variable t2 = TERM_FACTORY.getVariable("x");
        FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot = TERM_FACTORY.getImmutableFunctionalTerm(fs2, ImmutableList.of(t2));

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(t1), ImmutableList.of(ot));
        assertFalse(s.isPresent());
    }

    //A(y), A(p(x))
    @Test
    public void test_21() {
        Variable t1 = TERM_FACTORY.getVariable("y");
        Variable t2 = TERM_FACTORY.getVariable("x");
        FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot = TERM_FACTORY.getImmutableFunctionalTerm(fs2, ImmutableList.of(t2));

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(t1), ImmutableList.of(ot));
        assertEquals(SUBSTITUTION_FACTORY.getSubstitution(t1, ot), s.get());
    }

    //A(q(x)), A(p(x))
    @Test
    public void test_22() {
        Variable t1 = TERM_FACTORY.getVariable("x");
        FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("q", 1);
        ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1, ImmutableList.of(t1));

        Variable t2 = TERM_FACTORY.getVariable("x");
        FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2, ImmutableList.of(t2));

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(ot1), ImmutableList.of(ot2));
        assertFalse(s.isPresent());
    }

    //A(p(y)), A(p(x))
    @Test
    public void test_24() {
        Variable t1 = TERM_FACTORY.getVariable("y");
        FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1, ImmutableList.of(t1));

        Variable t2 = TERM_FACTORY.getVariable("x");
        FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2, ImmutableList.of(t2));

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(ot1), ImmutableList.of(ot2));
        assertEquals(SUBSTITUTION_FACTORY.getSubstitution(t1, t2), s.get());
    }

    // A(p(y,z)), A(p(x))
    @Test
    public void test_25() {
        Variable t1 = TERM_FACTORY.getVariable("y");
        Variable t3 = TERM_FACTORY.getVariable("z");
        FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 2);
        ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1, ImmutableList.of(t1, t3));

        ImmutableTerm t2 = TERM_FACTORY.getVariable("x");
        FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2, ImmutableList.of(t2));

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(ot1), ImmutableList.of(ot2));
        assertFalse(s.isPresent());
    }

    //A(p('123')), A(p(x))
    @Test
    public void test_26() {
        ImmutableTerm t1 = TERM_FACTORY.getRDFLiteralConstant("123", XSD.STRING);
        FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1, ImmutableList.of(t1));

        Variable t2 = TERM_FACTORY.getVariable("x");
        FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2, ImmutableList.of(t2));

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(ot1), ImmutableList.of(ot2));
        assertEquals(SUBSTITUTION_FACTORY.getSubstitution(t2, t1), s.get());
    }

    //A(p('123',z)),A(p(x))
    @Test
    public void test_27() {
        ImmutableTerm t1 = TERM_FACTORY.getRDFLiteralConstant("123", XSD.STRING);
        ImmutableTerm t3 = TERM_FACTORY.getVariable("x");
        FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 2);
        ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1, ImmutableList.of(t1, t3));

        ImmutableTerm t2 = TERM_FACTORY.getVariable("x");
        FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2, ImmutableList.of(t2));

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(ot1), ImmutableList.of(ot2));
        assertFalse(s.isPresent());
    }

    //A(q('123')),A(p(x))
    @Test
    public void test_28() {
        ImmutableTerm t1 = TERM_FACTORY.getRDFLiteralConstant("123", XSD.STRING);
        FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("q", 1);
        ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1, ImmutableList.of(t1));

        ImmutableTerm t2 = TERM_FACTORY.getVariable("x");
        FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2, ImmutableList.of(t2));

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(ot1), ImmutableList.of(ot2));
        assertFalse(s.isPresent());
    }

    //A(p('123')),A(p(x,z))
    @Test
    public void test_29() {

        ImmutableTerm t1 = TERM_FACTORY.getRDFLiteralConstant("123", XSD.STRING);
        FunctionSymbol fs1 = new OntopModelTestFunctionSymbol("p", 1);
        ImmutableFunctionalTerm ot1 = TERM_FACTORY.getImmutableFunctionalTerm(fs1, ImmutableList.of(t1));

        Variable t2 = TERM_FACTORY.getVariable("x");
        Variable t3 = TERM_FACTORY.getVariable("z");
        FunctionSymbol fs2 = new OntopModelTestFunctionSymbol("p", 2);
        ImmutableFunctionalTerm ot2 = TERM_FACTORY.getImmutableFunctionalTerm(fs2, ImmutableList.of(t2, t3));

        Optional<ImmutableSubstitution<ImmutableTerm>> s = UNIFICATION_TOOLS.computeMGU(ImmutableList.of(ot1), ImmutableList.of(ot2));
        assertFalse(s.isPresent());
    }
}
