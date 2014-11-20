package org.semanticweb.ontop.partialEvaluation.test;

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


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import junit.framework.TestCase;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.ValueConstant;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.impl.FunctionalTermImpl;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.SingletonSubstitution;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Substitution;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.SubstitutionImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.SubstitutionUtilities;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.UnifierUtilities;


public class ThetaApplicationTest extends TestCase {

	OBDADataFactory	termFactory	= OBDADataFactoryImpl.getInstance();
	OBDADataFactory predFactory	= OBDADataFactoryImpl.getInstance();

	/*
	 * tests the application of given thteas to a given CQIE scenario settings
	 * are as follows Thetas: t/x, uri(p)/y and "elf"/z The original atom:
	 * A(x,y,z,p(x),p("con","st")) The expected result:
	 * A(t,uri(p),"elf",p(t),p("con","st"))
	 */
	public void test_1() {

		Term t1 = termFactory.getVariable("x");
		Term t2 = termFactory.getVariable("y");
		Term t3 = termFactory.getVariable("z");

		Term t4 = termFactory.getVariable("x");
		List<Term> vars = new Vector<Term>();
		vars.add(t4);
		Predicate fs = predFactory.getPredicate("p", vars.size());
		FunctionalTermImpl ot = (FunctionalTermImpl) termFactory.getFunction(fs, vars);

		Term t5 = termFactory.getConstantLiteral("con");
		Term t51 = termFactory.getConstantLiteral("st");
		List<Term> vars5 = new Vector<Term>();
		vars5.add(t5);
		vars5.add(t51);
		Predicate fs2 = predFactory.getPredicate("p", vars5.size());
		FunctionalTermImpl ot2 = (FunctionalTermImpl) termFactory.getFunction(fs2, vars5);

		Predicate pred1 = predFactory.getPredicate("A", 5);
		List<Term> terms1 = new Vector<Term>();
		terms1.add(t1);
		terms1.add(t2);
		terms1.add(t3);
		terms1.add(ot);
		terms1.add(ot2);
		Function atom1 = predFactory.getFunction(pred1, terms1);
		List<Function> body = new Vector<Function>();
		body.add(atom1);

		VariableImpl t7 = (VariableImpl)termFactory.getVariable("x");
		Term t6 = termFactory.getVariable("t");
		VariableImpl t8 = (VariableImpl)termFactory.getVariable("z");
		Term t9 = termFactory.getConstantLiteral("elf");
		Term t10 = termFactory.getVariable("x");
		VariableImpl t11 = (VariableImpl)termFactory.getVariable("y");
		Term t12 = termFactory.getVariable("p");
		List<Term> vars3 = new Vector<Term>();
		vars3.add(t12);
		Predicate fs3 = predFactory.getPredicate("uri", vars3.size());
		FunctionalTermImpl otx = (FunctionalTermImpl) termFactory.getFunction(fs3, vars3);

		Predicate head = predFactory.getPredicate("q", 1);
		List<Term> terms2 = new Vector<Term>();
		terms2.add(t10);
		Function h = predFactory.getFunction(head, terms2);

		CQIE query = predFactory.getCQIE(h, body);

        SingletonSubstitution s1 = new SingletonSubstitution(t7, t6);
        SingletonSubstitution s2 = new SingletonSubstitution(t8, t9);
        SingletonSubstitution s3 = new SingletonSubstitution(t11, otx);

        Map<VariableImpl, Term> entries = new HashMap<>();
		entries.put(s1.getVariable(), s1.getTerm());
		entries.put(s2.getVariable(), s2.getTerm());
		entries.put(s3.getVariable(), s3.getTerm());
        Substitution mgu = new SubstitutionImpl(entries);

		UnifierUtilities unifier = new UnifierUtilities();
		CQIE newquery = SubstitutionUtilities.applySubstitution(query, mgu);

		List<Function> newbody = newquery.getBody();
		assertEquals(1, newbody.size());

		Function a = (Function) newbody.get(0);
		List<Term> terms = a.getTerms();
		assertEquals(5, terms.size());

		VariableImpl term1 = (VariableImpl) terms.get(0);
		FunctionalTermImpl term2 = (FunctionalTermImpl) terms.get(1);
		ValueConstant term3 = (ValueConstant) terms.get(2);
		FunctionalTermImpl term4 = (FunctionalTermImpl) terms.get(3);
		FunctionalTermImpl term5 = (FunctionalTermImpl) terms.get(4);

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

		// Term qt1 = termFactory.createVariable("a");
		// Term qt2 = termFactory.createVariable("b");
		// Term qt3 = termFactory.createVariable("c");
		//
		//
		// Predicate pred1 = predFactory.createPredicate("A", 1);
		// List<Term> terms1 = new Vector<Term>();
		// terms1.add(qt1);
		// AtomImpl a1 = new AtomImpl(pred1, terms1);
		//
		// Predicate pred2 = predFactory.createPredicate("B", 1);
		// List<Term> terms2 = new Vector<Term>();
		// terms1.add(qt2);
		// AtomImpl a2 = new AtomImpl(pred2, terms2);
		//
		// Predicate pred3 = predFactory.createPredicate("C", 1);
		// List<Term> terms3 = new Vector<Term>();
		// terms3.add(qt3);
		// AtomImpl a3 = new AtomImpl(pred3, terms3);
		//
		// LinkedList<Function> body = new LinkedList<Function>();
		//
		// Predicate predh = predFactory.createPredicate("q", 1);
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
		// Term t1 = termFactory.createVariable("x");
		// Predicate pred1 = predFactory.createPredicate("A", 1);
		// List<Term> terms1 = new Vector<Term>();
		// terms1.add(t1);
		// AtomImpl atom1 = new AtomImpl(pred1, terms1);
		// Term t2 = termFactory.createVariable("x");
		// Predicate pred2 = predFactory.createPredicate("A", 1);
		// List<Term> terms2 = new Vector<Term>();
		// terms2.add(t2);
		// AtomImpl atom2 = new AtomImpl(pred2, terms2);
		// Term ht = termFactory.createVariable("x");
		// Predicate pred3 = predFactory.createPredicate("q", 1);
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
