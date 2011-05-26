package org.obda.partialEvaluation.test;

import inf.unibz.it.obda.api.controller.OBDADataFactory;
import inf.unibz.it.obda.model.impl.AtomImpl;
import inf.unibz.it.obda.model.impl.CQIEImpl;
import inf.unibz.it.obda.model.impl.FunctionalTermImpl;
import inf.unibz.it.obda.model.impl.OBDADataFactoryImpl;
import inf.unibz.it.obda.model.impl.VariableImpl;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import junit.framework.TestCase;

import org.obda.owlrefplatform.core.basicoperations.AtomUnifier;
import org.obda.owlrefplatform.core.basicoperations.Substitution;
import org.obda.query.domain.Atom;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.Predicate;
import org.obda.query.domain.Term;
import org.obda.query.domain.ValueConstant;
import org.obda.query.domain.Variable;

public class ThetaApplicationTest extends TestCase {

	OBDADataFactory termFactory =  OBDADataFactoryImpl.getInstance();
	OBDADataFactory predFactory = OBDADataFactoryImpl.getInstance();

	/*tests the application of given thteas to a given CQIE
	  scenario settings are as follows
	  Thetas: t/x, uri(p)/y and "elf"/z
	  The original atom: A(x,y,z,p(x),p("con","st"))
	  The expected result: A(t,uri(p),"elf",p(t),p("con","st"))
	  */
	public void test_1(){

		Term t1 = termFactory.createVariable("x");
		Term t2 = termFactory.createVariable("y");
		Term t3 = termFactory.createVariable("z");

		Term t4 = termFactory.createVariable("x");
		List<Term> vars = new Vector<Term>();
		vars.add(t4);
		Predicate fs = predFactory.createPredicate(URI.create("p"), vars.size());
		FunctionalTermImpl ot = (FunctionalTermImpl) termFactory.createFunctionalTerm(fs, vars);

		Term t5 = termFactory.createValueConstant("con");
		Term t51 = termFactory.createValueConstant("st");
		List<Term> vars5 = new Vector<Term>();
		vars5.add(t5);
		vars5.add(t51);
		Predicate fs2 = predFactory.createPredicate(URI.create("p"), vars5.size());
		FunctionalTermImpl ot2 =(FunctionalTermImpl) termFactory.createFunctionalTerm(fs2, vars5);

		Predicate pred1 = predFactory.createPredicate(URI.create("A"), 5);
		List<Term> terms1 = new Vector<Term>();
		terms1.add(t1);
		terms1.add(t2);
		terms1.add(t3);
		terms1.add(ot);
		terms1.add(ot2);
		AtomImpl atom1 = new AtomImpl(pred1, terms1);
		Vector<Atom> body = new Vector<Atom>();
		body.add(atom1);

		Term t7 = termFactory.createVariable("x");
		Term t6 = termFactory.createVariable("t");
		Term t8 = termFactory.createVariable("z");
		Term t9 = termFactory.createValueConstant("elf");
		Term t10 = termFactory.createVariable("x");
		Term t11 = termFactory.createVariable("y");
		Term t12 = termFactory.createVariable("p");
		List<Term> vars3 = new Vector<Term>();
		vars3.add(t12);
		Predicate fs3 = predFactory.createPredicate(URI.create("uri"), vars3.size());
		FunctionalTermImpl otx = (FunctionalTermImpl) termFactory.createFunctionalTerm(fs3, vars3);

		Predicate head = predFactory.createPredicate(URI.create("q"), 1);
		List<Term> terms2 = new Vector<Term>();
		terms2.add(t10);
		AtomImpl h = new AtomImpl(head, terms2);

		CQIE query = new CQIEImpl(h, body, false);

		Substitution s1 = new Substitution(t7, t6);
		Substitution s2 = new Substitution(t8, t9);
		Substitution s3 = new Substitution(t11, otx);

		Map<Variable, Term> mgu = new HashMap<Variable, Term>();
		mgu.put((Variable)s1.getVariable(), s1.getTerm());
		mgu.put((Variable)s2.getVariable(), s2.getTerm());
		mgu.put((Variable)s3.getVariable(), s3.getTerm());

		AtomUnifier unifier = new AtomUnifier();
		CQIE newquery = unifier.applyUnifier(query, mgu);

		List<Atom> newbody = newquery.getBody();
		assertEquals(1, newbody.size());

		Atom a = newbody.get(0);
		List<Term> terms = a.getTerms();
		assertEquals(5,terms.size());

		VariableImpl term1 = (VariableImpl) terms.get(0);
		FunctionalTermImpl term2 = (FunctionalTermImpl) terms.get(1);
		ValueConstant term3 = (ValueConstant) terms.get(2);
		FunctionalTermImpl term4 = (FunctionalTermImpl) terms.get(3);
		FunctionalTermImpl term5 = (FunctionalTermImpl) terms.get(4);

		assertEquals("t", term1.getName());
		assertEquals("elf", term3.getName());

		List<Term> para_t2 = term2.getTerms();
		List<Term> para_t4 = term4.getTerms();
		List<Term> para_t5 = term5.getTerms();

		assertEquals(1, para_t2.size());
		assertEquals(1, para_t4.size());
		assertEquals(2, para_t5.size());

		assertEquals("p",para_t2.get(0).getName());
		assertEquals("t",para_t4.get(0).getName());
		assertEquals("con",para_t5.get(0).getName());
		assertEquals("st",para_t5.get(1).getName());

	}

	public void test_2() throws Exception{

//		Term qt1 = termFactory.createVariable("a");
//		Term qt2 =  termFactory.createVariable("b");
//		Term qt3 =  termFactory.createVariable("c");
//
//
//		Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
//		List<Term> terms1 = new Vector<Term>();
//		terms1.add(qt1);
//		AtomImpl a1 = new AtomImpl(pred1, terms1);
//
//		Predicate pred2 = predFactory.createPredicate(URI.create("B"), 1);
//		List<Term> terms2 = new Vector<Term>();
//		terms1.add(qt2);
//		AtomImpl a2 = new AtomImpl(pred2, terms2);
//
//		Predicate pred3 = predFactory.createPredicate(URI.create("C"), 1);
//		List<Term> terms3 = new Vector<Term>();
//		terms3.add(qt3);
//		AtomImpl a3 = new AtomImpl(pred3, terms3);
//
//		LinkedList<Atom> body = new LinkedList<Atom>();
//
//		Predicate predh = predFactory.createPredicate(URI.create("q"), 1);
//		List<Term> termsh = new Vector<Term>();
//		termsh.add(qt1);
//		termsh.add(qt2);
//		termsh.add(qt3);
//		AtomImpl h = new AtomImpl(predh, termsh);
//
//
//		CQIE query = new CQIEImpl(h, body, false);
//
//
//
//
//		MappingViewManager viewMan = new MappingViewManager(vex);
//		ComplexMappingUnfolder cmu = new ComplexMappingUnfolder(vex, viewMan);
//
//		Term t1 = termFactory.createVariable("x");
//		Predicate pred1 = predFactory.createPredicate(URI.create("A"), 1);
//		List<Term> terms1 = new Vector<Term>();
//		terms1.add(t1);
//		AtomImpl atom1 = new AtomImpl(pred1, terms1);
//		Term t2 = termFactory.createVariable("x");
//		Predicate pred2 = predFactory.createPredicate(URI.create("A"), 1);
//		List<Term> terms2 = new Vector<Term>();
//		terms2.add(t2);
//		AtomImpl atom2 = new AtomImpl(pred2, terms2);
//		Term ht = termFactory.createVariable("x");
//		Predicate pred3 = predFactory.createPredicate(URI.create("q"), 1);
//		List<Term> terms3 = new Vector<Term>();
//		terms3.add(ht);
//		AtomImpl head = new AtomImpl(pred3, terms3);
//		Vector<Atom> body = new Vector<Atom>();
//		body.add(atom1);
//		body.add(atom2);
//		CQIE q = new CQIEImpl(head, body, false);
//
//		Atom fresh = cmu.getFreshAuxPredicatAtom(ax, q, 1);
//		List<Term> terms = fresh.getTerms();
//		assertEquals(3, terms.size());
//
//		VariableImpl term1 = (VariableImpl) terms.get(0);
//		VariableImpl term2 = (VariableImpl) terms.get(1);
//		VariableImpl term3 = (VariableImpl) terms.get(2);
//
//		assertEquals("aux1_0_0", term1.getName());
//		assertEquals("aux1_1_0", term2.getName());
//		assertEquals("aux1_2_0", term3.getName());
//
//		body.remove(0);
//		body.add(0,fresh);
//
//		Atom fresh2 = cmu.getFreshAuxPredicatAtom(ax, q, 2);
//		List<Term> termsk = fresh2.getTerms();
//		assertEquals(3, terms.size());
//
//		VariableImpl term12 = (VariableImpl) termsk.get(0);
//		VariableImpl term22 = (VariableImpl) termsk.get(1);
//		VariableImpl term32 = (VariableImpl) termsk.get(2);
//
//		assertEquals("aux1_0_1", term12.getName());
//		assertEquals("aux1_1_1", term22.getName());
//		assertEquals("aux1_2_1", term32.getName());
	}
}
