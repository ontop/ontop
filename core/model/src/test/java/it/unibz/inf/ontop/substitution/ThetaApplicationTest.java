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

import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.model.term.impl.FunctionalTermImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.impl.SingletonSubstitution;
import it.unibz.inf.ontop.substitution.impl.SubstitutionImpl;
import it.unibz.inf.ontop.substitution.impl.SubstitutionUtilities;
import junit.framework.TestCase;

import static it.unibz.inf.ontop.OntopModelTestingTools.*;


public class ThetaApplicationTest extends TestCase {

	/*
	 * tests the application of given thteas to a given CQIE scenario settings
	 * are as follows Thetas: t/x, uri(p)/y and "elf"/z The original atom:
	 * A(x,y,z,p(x),p("con","st")) The expected result:
	 * A(t,uri(p),"elf",p(t),p("con","st"))
	 */
	public void test_1() {

		Term t1 = TERM_FACTORY.getVariable("x");
		Term t2 = TERM_FACTORY.getVariable("y");
		Term t3 = TERM_FACTORY.getVariable("z");

		Term t4 = TERM_FACTORY.getVariable("x");
		List<Term> vars = new Vector<Term>();
		vars.add(t4);
		Predicate fs = new OntopModelTestPredicate("p", vars.size());
		FunctionalTermImpl ot = (FunctionalTermImpl) TERM_FACTORY.getFunction(fs, vars);

		Term t5 = TERM_FACTORY.getConstantLiteral("con");
		Term t51 = TERM_FACTORY.getConstantLiteral("st");
		List<Term> vars5 = new Vector<Term>();
		vars5.add(t5);
		vars5.add(t51);
		Predicate fs2 = new OntopModelTestPredicate("p", vars5.size());
		FunctionalTermImpl ot2 = (FunctionalTermImpl) TERM_FACTORY.getFunction(fs2, vars5);

		Predicate pred1 = new OntopModelTestPredicate("A", 5);
		List<Term> terms1 = new Vector<Term>();
		terms1.add(t1);
		terms1.add(t2);
		terms1.add(t3);
		terms1.add(ot);
		terms1.add(ot2);
		Function atom1 = TERM_FACTORY.getFunction(pred1, terms1);
		List<Function> body = new Vector<Function>();
		body.add(atom1);

		Variable t7 = TERM_FACTORY.getVariable("x");
		Term t6 = TERM_FACTORY.getVariable("t");
		Variable t8 = TERM_FACTORY.getVariable("z");
		Term t9 = TERM_FACTORY.getConstantLiteral("elf");
		Term t10 = TERM_FACTORY.getVariable("x");
		Variable t11 = TERM_FACTORY.getVariable("y");
		Term t12 = TERM_FACTORY.getVariable("p");
		List<Term> vars3 = new Vector<Term>();
		vars3.add(t12);
		Predicate fs3 = new OntopModelTestPredicate("uri", vars3.size());
		FunctionalTermImpl otx = (FunctionalTermImpl) TERM_FACTORY.getFunction(fs3, vars3);

		Predicate head = new OntopModelTestPredicate("q", 1);
		List<Term> terms2 = new Vector<Term>();
		terms2.add(t10);
		Function h = TERM_FACTORY.getFunction(head, terms2);

		CQIE query = DATALOG_FACTORY.getCQIE(h, body);

        SingletonSubstitution s1 = new SingletonSubstitution(t7, t6);
        SingletonSubstitution s2 = new SingletonSubstitution(t8, t9);
        SingletonSubstitution s3 = new SingletonSubstitution(t11, otx);

        Map<Variable, Term> entries = new HashMap<>();
		entries.put(s1.getVariable(), s1.getTerm());
		entries.put(s2.getVariable(), s2.getTerm());
		entries.put(s3.getVariable(), s3.getTerm());
        Substitution mgu = new SubstitutionImpl(entries, TERM_FACTORY);


		SubstitutionUtilities substitutionUtilities = new SubstitutionUtilities(TERM_FACTORY);
		CQIE newquery = substitutionUtilities.applySubstitution(query, mgu);

		List<Function> newbody = newquery.getBody();
		assertEquals(1, newbody.size());

		Function a = (Function) newbody.get(0);
		List<Term> terms = a.getTerms();
		assertEquals(5, terms.size());

		Variable term1 = (Variable) terms.get(0);
		Function term2 = (Function) terms.get(1);
		ValueConstant term3 = (ValueConstant) terms.get(2);
		Function term4 = (Function) terms.get(3);
		Function term5 = (Function) terms.get(4);

		assertEquals("t", term1.getName());
		assertEquals("elf", term3.getValue());

		List<Term> para_t2 = term2.getTerms();
		List<Term> para_t4 = term4.getTerms();
		List<Term> para_t5 = term5.getTerms();

		assertEquals(1, para_t2.size());
		assertEquals(1, para_t4.size());
		assertEquals(2, para_t5.size());

		assertEquals("p", ((Variable) para_t2.get(0)).getName());
		assertEquals("t", ((Variable) para_t4.get(0)).getName());
		assertEquals("con", ((ValueConstant) para_t5.get(0)).getValue());
		assertEquals("st", ((ValueConstant) para_t5.get(1)).getValue());

	}

	public void test_2() throws Exception {

		// Term qt1 = TERM_FACTORY.createVariable("a");
		// Term qt2 = TERM_FACTORY.createVariable("b");
		// Term qt3 = TERM_FACTORY.createVariable("c");
		//
		//
		// Predicate pred1 = TERM_FACTORY.createPredicate("A", 1);
		// List<Term> terms1 = new Vector<Term>();
		// terms1.add(qt1);
		// AtomImpl a1 = new AtomImpl(pred1, terms1);
		//
		// Predicate pred2 = TERM_FACTORY.createPredicate("B", 1);
		// List<Term> terms2 = new Vector<Term>();
		// terms1.add(qt2);
		// AtomImpl a2 = new AtomImpl(pred2, terms2);
		//
		// Predicate pred3 = TERM_FACTORY.createPredicate("C", 1);
		// List<Term> terms3 = new Vector<Term>();
		// terms3.add(qt3);
		// AtomImpl a3 = new AtomImpl(pred3, terms3);
		//
		// LinkedList<Function> body = new LinkedList<Function>();
		//
		// Predicate predh = TERM_FACTORY.createPredicate("q", 1);
		// List<Term> termsh = new Vector<Term>();
		// termsh.add(qt1);
		// termsh.add(qt2);
		// termsh.add(qt3);
		// AtomImpl h = new AtomImpl(predh, termsh);
		//
		//
		// CQIE query = new CQIEImpl(h, body, false);
		//
		//
		//
		//
		// MappingViewManager viewMan = new MappingViewManager(vex);
		// ComplexMappingUnfolder cmu = new ComplexMappingUnfolder(vex,
		// viewMan);
		//
		// Term t1 = TERM_FACTORY.createVariable("x");
		// Predicate pred1 = TERM_FACTORY.createPredicate("A", 1);
		// List<Term> terms1 = new Vector<Term>();
		// terms1.add(t1);
		// AtomImpl atom1 = new AtomImpl(pred1, terms1);
		// Term t2 = TERM_FACTORY.createVariable("x");
		// Predicate pred2 = TERM_FACTORY.createPredicate("A", 1);
		// List<Term> terms2 = new Vector<Term>();
		// terms2.add(t2);
		// AtomImpl atom2 = new AtomImpl(pred2, terms2);
		// Term ht = TERM_FACTORY.createVariable("x");
		// Predicate pred3 = TERM_FACTORY.createPredicate("q", 1);
		// List<Term> terms3 = new Vector<Term>();
		// terms3.add(ht);
		// AtomImpl head = new AtomImpl(pred3, terms3);
		// Vector<Function> body = new Vector<Function>();
		// body.add(atom1);
		// body.add(atom2);
		// CQIE q = new CQIEImpl(head, body, false);
		//
		// Function fresh = cmu.getFreshAuxPredicatAtom(ax, q, 1);
		// List<Term> terms = fresh.getTerms();
		// assertEquals(3, terms.size());
		//
		// VariableImpl term1 = (VariableImpl) terms.get(0);
		// VariableImpl term2 = (VariableImpl) terms.get(1);
		// VariableImpl term3 = (VariableImpl) terms.get(2);
		//
		// assertEquals("aux1_0_0", term1.getName());
		// assertEquals("aux1_1_0", term2.getName());
		// assertEquals("aux1_2_0", term3.getName());
		//
		// body.remove(0);
		// body.add(0,fresh);
		//
		// Function fresh2 = cmu.getFreshAuxPredicatAtom(ax, q, 2);
		// List<Term> termsk = fresh2.getTerms();
		// assertEquals(3, terms.size());
		//
		// VariableImpl term12 = (VariableImpl) termsk.get(0);
		// VariableImpl term22 = (VariableImpl) termsk.get(1);
		// VariableImpl term32 = (VariableImpl) termsk.get(2);
		//
		// assertEquals("aux1_0_1", term12.getName());
		// assertEquals("aux1_1_1", term22.getName());
		// assertEquals("aux1_2_1", term32.getName());
	}
}
