/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.obda.partialEvaluation.test;

import inf.unibz.it.dl.domain.DataProperty;
import inf.unibz.it.dl.domain.NamedConcept;
import inf.unibz.it.dl.domain.NamedProperty;
import inf.unibz.it.dl.domain.ObjectProperty;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSOBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.ucq.domain.BinaryQueryAtom;
import inf.unibz.it.ucq.domain.ConceptQueryAtom;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.FunctionTerm;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.domain.VariableTerm;
import inf.unibz.it.ucq.parser.exception.QueryParseException;

import java.net.URI;
import java.util.ArrayList;
import java.util.Vector;

import junit.framework.TestCase;

import org.obda.owlrefplatform.core.ComplexMappingUnfolder;
import org.obda.owlrefplatform.core.MappingViewManager;
import org.obda.query.domain.Atom;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.Predicate;
import org.obda.query.domain.Term;
import org.obda.query.domain.imp.AtomImpl;
import org.obda.query.domain.imp.BasicPredicateFactoryImpl;
import org.obda.query.domain.imp.CQIEImpl;
import org.obda.query.domain.imp.ObjectVariableImpl;
import org.obda.query.domain.imp.TermFactoryImpl;
import org.obda.query.domain.imp.UndistinguishedVariable;
import org.obda.query.domain.imp.VariableImpl;

public class MappingToRuleTester extends TestCase {
	
	//C(p(x))
	public void test_1() throws QueryParseException{
		
		OBDAMappingAxiom ax = new RDBMSOBDAMappingAxiom("id0"); 
		RDBMSSQLQuery sqlqeruy = new RDBMSSQLQuery("SELECT term1 as x from table1");
		Term v = new VariableTerm("x");
		ArrayList<QueryTerm> l = new ArrayList<QueryTerm>();
		l.add(v);
		QueryTerm qt = new FunctionTerm(URI.create("p"), l);
		NamedConcept con = new NamedConcept(URI.create("C"));
		QueryAtom qa = new ConceptQueryAtom(con, qt);
		ConjunctiveQuery cq = new ConjunctiveQuery();
		cq.addQueryAtom(qa);
		ax.setSourceQuery(sqlqeruy);
		ax.setTargetQuery(cq);
		
		Term qt2 = TermFactoryImpl.getInstance().createVariable("x");
		Vector<Term> tlist2 = new Vector<Term>();
		tlist2.add(qt2);
		Predicate predicate2 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("C"), 1);
		Atom qa2 = new AtomImpl(predicate2, tlist2);
		
		Term qt3 = TermFactoryImpl.getInstance().createVariable("x");
		Vector<Term> tlist3 = new Vector<Term>();
		tlist3.add(qt3);
		Predicate predicate3 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("q"), 1);
		Atom qah =new AtomImpl(predicate3, tlist3);
		
		Vector<Atom> body = new Vector<Atom>();
		body.add(qa2);
		CQIEImpl imp = new CQIEImpl(qah, body, false);
		
		Vector<OBDAMappingAxiom> maplist = new Vector<OBDAMappingAxiom>();
		maplist.add(ax);
		MappingViewManager viewMan = new MappingViewManager(maplist);
		ComplexMappingUnfolder cmu = new ComplexMappingUnfolder(maplist, viewMan);
		
		CQIE newquery = cmu.getFreshRuleFromMapping(ax, imp, 0);
		
		Atom head = newquery.getHead();
		Predicate p = head.getPredicate();
		assertEquals(con.getUri().toString(),p.getName().toString());
		assertEquals(1,head.getArity());
		ObjectVariableImpl term = (ObjectVariableImpl) head.getTerms().get(0);
		assertEquals("p",term.getName());
		assertEquals(1,term.getTerms().size());
		Term t = term.getTerms().get(0);
		assertEquals("aux1_0_0", t.getName());
	}
	
	//C(p(x,x))
	public void test_2() throws QueryParseException{
		
		OBDAMappingAxiom ax = new RDBMSOBDAMappingAxiom("id0"); 
		RDBMSSQLQuery sqlqeruy = new RDBMSSQLQuery("SELECT term1 as x from table1", null);
		QueryTerm v = new VariableTerm("x");
		QueryTerm v2 = new VariableTerm("x");
		ArrayList<QueryTerm> l = new ArrayList<QueryTerm>();
		l.add(v);
		l.add(v2);
		QueryTerm qt = new FunctionTerm(URI.create("p"), l);
		NamedConcept con = new NamedConcept(URI.create("C"));
		QueryAtom qa = new ConceptQueryAtom(con, qt);
		ConjunctiveQuery cq = new ConjunctiveQuery();
		cq.addQueryAtom(qa);
		ax.setSourceQuery(sqlqeruy);
		ax.setTargetQuery(cq);
		
		Term qt2 = TermFactoryImpl.getInstance().createVariable("x");
		Vector<Term> tlist2 = new Vector<Term>();
		tlist2.add(qt2);
		Predicate predicate2 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("C"), 1);
		Atom qa2 = new AtomImpl(predicate2, tlist2);
		
		Term qt3 = TermFactoryImpl.getInstance().createVariable("x");
		Vector<Term> tlist3 = new Vector<Term>();
		tlist3.add(qt3);
		Predicate predicate3 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("q"), 1);
		Atom qah =new AtomImpl(predicate3, tlist3);
		
		Vector<Atom> body = new Vector<Atom>();
		body.add(qa2);
		CQIEImpl imp = new CQIEImpl(qah, body, false);
		
		Vector<OBDAMappingAxiom> maplist = new Vector<OBDAMappingAxiom>();
		maplist.add(ax);
		MappingViewManager viewMan = new MappingViewManager(maplist);
		ComplexMappingUnfolder cmu = new ComplexMappingUnfolder(maplist, viewMan);
		
		CQIE newquery = cmu.getFreshRuleFromMapping(ax, imp, 0);
		
		Atom head = newquery.getHead();
		Predicate p = head.getPredicate();
		assertEquals(con.getUri().toString(),p.getName().toString());
		assertEquals(1,head.getArity());
		ObjectVariableImpl term = (ObjectVariableImpl) head.getTerms().get(0);
		assertEquals("p",term.getName());
		assertEquals(2,term.getTerms().size());
		Term t1 = term.getTerms().get(0);
		Term t2 = term.getTerms().get(1);
		assertEquals("aux1_0_0", t1.getName());
		assertEquals("aux1_0_0", t2.getName());
	}

	//C(p(x,y))
	public void test_3() throws QueryParseException{
		
		OBDAMappingAxiom ax = new RDBMSOBDAMappingAxiom("id0"); 
		RDBMSSQLQuery sqlqeruy = new RDBMSSQLQuery("SELECT term1 as x, term2 as y from table1", null);
		QueryTerm v = new VariableTerm("x");
		QueryTerm v2 = new VariableTerm("y");
		ArrayList<QueryTerm> l = new ArrayList<QueryTerm>();
		l.add(v);
		l.add(v2);
		QueryTerm qt = new FunctionTerm(URI.create("p"), l);
		NamedConcept con = new NamedConcept(URI.create("C"));
		QueryAtom qa = new ConceptQueryAtom(con, qt);
		ConjunctiveQuery cq = new ConjunctiveQuery();
		cq.addQueryAtom(qa);
		ax.setSourceQuery(sqlqeruy);
		ax.setTargetQuery(cq);
		
		Term qt2 = TermFactoryImpl.getInstance().createVariable("x");
		Vector<Term> tlist2 = new Vector<Term>();
		tlist2.add(qt2);
		Predicate predicate2 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("C"), 1);
		Atom qa2 = new AtomImpl(predicate2, tlist2);
		
		Term qt3 = TermFactoryImpl.getInstance().createVariable("x");
		Vector<Term> tlist3 = new Vector<Term>();
		tlist3.add(qt3);
		Predicate predicate3 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("q"), 1);
		Atom qah =new AtomImpl(predicate3, tlist3);
		
		Vector<Atom> body = new Vector<Atom>();
		body.add(qa2);
		CQIEImpl imp = new CQIEImpl(qah, body, false);
		
		Vector<OBDAMappingAxiom> maplist = new Vector<OBDAMappingAxiom>();
		maplist.add(ax);
		MappingViewManager viewMan = new MappingViewManager(maplist);
		ComplexMappingUnfolder cmu = new ComplexMappingUnfolder(maplist, viewMan);
		
		CQIE newquery = cmu.getFreshRuleFromMapping(ax, imp, 0);
		
		Atom head = newquery.getHead();
		Predicate p = head.getPredicate();
		assertEquals(con.getUri().toString(),p.getName().toString());
		assertEquals(1,head.getArity());
		ObjectVariableImpl term = (ObjectVariableImpl) head.getTerms().get(0);
		assertEquals("p",term.getName());
		assertEquals(2,term.getTerms().size());
		Term t1 = term.getTerms().get(0);
		Term t2 = term.getTerms().get(1);
		assertEquals("aux1_0_0", t1.getName());
		assertEquals("aux1_1_0", t2.getName());
	}
	
	//R(p(x),p(x))
	public void test_4() throws QueryParseException{
		
		OBDAMappingAxiom ax = new RDBMSOBDAMappingAxiom("id0"); 
		RDBMSSQLQuery sqlqeruy = new RDBMSSQLQuery("SELECT term1 as x, term2 as y from table1", null);
		QueryTerm v = new VariableTerm("x");
		QueryTerm v2 = new VariableTerm("x");
		ArrayList<QueryTerm> l1 = new ArrayList<QueryTerm>();
		ArrayList<QueryTerm> l2 = new ArrayList<QueryTerm>();
		l1.add(v);
		l2.add(v2);
		QueryTerm qt1 = new FunctionTerm(URI.create("p"), l1);
		QueryTerm qt = new FunctionTerm(URI.create("p"), l2);
		NamedProperty con = new ObjectProperty(URI.create("R"));
		QueryAtom qa = new BinaryQueryAtom(con, qt1, qt);
		ConjunctiveQuery cq = new ConjunctiveQuery();
		cq.addQueryAtom(qa);
		ax.setSourceQuery(sqlqeruy);
		ax.setTargetQuery(cq);
		
		Term qt2 = TermFactoryImpl.getInstance().createVariable("x");
		Vector<Term> tlist2 = new Vector<Term>();
		tlist2.add(qt2);
		Predicate predicate2 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("C"), 1);
		Atom qa2 = new AtomImpl(predicate2, tlist2);
		
		Term qt3 = TermFactoryImpl.getInstance().createVariable("x");
		Vector<Term> tlist3 = new Vector<Term>();
		tlist3.add(qt3);
		Predicate predicate3 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("q"), 1);
		Atom qah =new AtomImpl(predicate3, tlist3);
		
		Vector<Atom> body = new Vector<Atom>();
		body.add(qa2);
		CQIEImpl imp = new CQIEImpl(qah, body, false);
		
		Vector<OBDAMappingAxiom> maplist = new Vector<OBDAMappingAxiom>();
		maplist.add(ax);
		MappingViewManager viewMan = new MappingViewManager(maplist);
		ComplexMappingUnfolder cmu = new ComplexMappingUnfolder(maplist, viewMan);
		
		CQIE newquery = cmu.getFreshRuleFromMapping(ax, imp, 0);
		
		Atom head = newquery.getHead();
		Predicate p = head.getPredicate();
		assertEquals(con.getUri().toString(),p.getName().toString());
		assertEquals(2,head.getArity());
		ObjectVariableImpl term1 = (ObjectVariableImpl) head.getTerms().get(0);
		ObjectVariableImpl term2 = (ObjectVariableImpl) head.getTerms().get(1);
		assertEquals("p",term1.getName());
		assertEquals("p",term2.getName());
		assertEquals(1,term1.getTerms().size());
		assertEquals(1,term2.getTerms().size());
		Term t1 = term1.getTerms().get(0);
		Term t2 = term2.getTerms().get(0);
		assertEquals("aux1_0_0", t1.getName());
		assertEquals("aux1_0_0", t2.getName());
	}
	
	//R(p(x),p(y))
	public void test_5() throws QueryParseException{
		
		OBDAMappingAxiom ax = new RDBMSOBDAMappingAxiom("id0"); 
		RDBMSSQLQuery sqlqeruy = new RDBMSSQLQuery("SELECT term1 as x, term2 as y from table1", null);
		QueryTerm v = new VariableTerm("x");
		QueryTerm v2 = new VariableTerm("y");
		ArrayList<QueryTerm> l1 = new ArrayList<QueryTerm>();
		ArrayList<QueryTerm> l2 = new ArrayList<QueryTerm>();
		l1.add(v);
		l2.add(v2);
		QueryTerm qt1 = new FunctionTerm(URI.create("p"), l1);
		QueryTerm qt = new FunctionTerm(URI.create("p"), l2);
		NamedProperty con = new ObjectProperty(URI.create("R"));
		QueryAtom qa = new BinaryQueryAtom(con, qt1, qt);
		ConjunctiveQuery cq = new ConjunctiveQuery();
		cq.addQueryAtom(qa);
		ax.setSourceQuery(sqlqeruy);
		ax.setTargetQuery(cq);
		
		Term qt2 = TermFactoryImpl.getInstance().createVariable("x");
		Vector<Term> tlist2 = new Vector<Term>();
		tlist2.add(qt2);
		Predicate predicate2 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("C"), 1);
		Atom qa2 = new AtomImpl(predicate2, tlist2);
		
		Term qt3 = TermFactoryImpl.getInstance().createVariable("x");
		Vector<Term> tlist3 = new Vector<Term>();
		tlist3.add(qt3);
		Predicate predicate3 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("q"), 1);
		Atom qah =new AtomImpl(predicate3, tlist3);
		
		Vector<Atom> body = new Vector<Atom>();
		body.add(qa2);
		CQIEImpl imp = new CQIEImpl(qah, body, false);
		
		Vector<OBDAMappingAxiom> maplist = new Vector<OBDAMappingAxiom>();
		maplist.add(ax);
		MappingViewManager viewMan = new MappingViewManager(maplist);
		ComplexMappingUnfolder cmu = new ComplexMappingUnfolder(maplist, viewMan);
		
		CQIE newquery = cmu.getFreshRuleFromMapping(ax, imp, 0);
		
		Atom head = newquery.getHead();
		Predicate p = head.getPredicate();
		assertEquals(con.getUri().toString(),p.getName().toString());
		assertEquals(2,head.getArity());
		ObjectVariableImpl term1 = (ObjectVariableImpl) head.getTerms().get(0);
		ObjectVariableImpl term2 = (ObjectVariableImpl) head.getTerms().get(1);
		assertEquals("p",term1.getName());
		assertEquals("p",term2.getName());
		assertEquals(1,term1.getTerms().size());
		assertEquals(1,term2.getTerms().size());
		Term t1 = term1.getTerms().get(0);
		Term t2 = term2.getTerms().get(0);
		assertEquals("aux1_0_0", t1.getName());
		assertEquals("aux1_1_0", t2.getName());
	}
	
	//R(p(x),q(x))
	public void test_6() throws QueryParseException{
		
		OBDAMappingAxiom ax = new RDBMSOBDAMappingAxiom("id0"); 
		RDBMSSQLQuery sqlqeruy = new RDBMSSQLQuery("SELECT term1 as x, term2 as y from table1", null);
		QueryTerm v = new VariableTerm("x");
		QueryTerm v2 = new VariableTerm("x");
		ArrayList<QueryTerm> l1 = new ArrayList<QueryTerm>();
		ArrayList<QueryTerm> l2 = new ArrayList<QueryTerm>();
		l1.add(v);
		l2.add(v2);
		QueryTerm qt1 = new FunctionTerm(URI.create("p"), l1);
		QueryTerm qt = new FunctionTerm(URI.create("q"), l2);
		NamedProperty con = new ObjectProperty(URI.create("R"));
		QueryAtom qa = new BinaryQueryAtom(con, qt1, qt);
		ConjunctiveQuery cq = new ConjunctiveQuery();
		cq.addQueryAtom(qa);
		ax.setSourceQuery(sqlqeruy);
		ax.setTargetQuery(cq);
		
		Term qt2 = TermFactoryImpl.getInstance().createVariable("x");
		Vector<Term> tlist2 = new Vector<Term>();
		tlist2.add(qt2);
		Predicate predicate2 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("C"), 1);
		Atom qa2 = new AtomImpl(predicate2, tlist2);
		
		Term qt3 = TermFactoryImpl.getInstance().createVariable("x");
		Vector<Term> tlist3 = new Vector<Term>();
		tlist3.add(qt3);
		Predicate predicate3 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("q"), 1);
		Atom qah =new AtomImpl(predicate3, tlist3);
		
		Vector<Atom> body = new Vector<Atom>();
		body.add(qa2);
		CQIEImpl imp = new CQIEImpl(qah, body, false);
		
		Vector<OBDAMappingAxiom> maplist = new Vector<OBDAMappingAxiom>();
		maplist.add(ax);
		MappingViewManager viewMan = new MappingViewManager(maplist);
		ComplexMappingUnfolder cmu = new ComplexMappingUnfolder(maplist, viewMan);
		
		CQIE newquery = cmu.getFreshRuleFromMapping(ax, imp, 0);
		
		Atom head = newquery.getHead();
		Predicate p = head.getPredicate();
		assertEquals(con.getUri().toString(),p.getName().toString());
		assertEquals(2,head.getArity());
		ObjectVariableImpl term1 = (ObjectVariableImpl) head.getTerms().get(0);
		ObjectVariableImpl term2 = (ObjectVariableImpl) head.getTerms().get(1);
		assertEquals("p",term1.getName());
		assertEquals("q",term2.getName());
		assertEquals(1,term1.getTerms().size());
		assertEquals(1,term2.getTerms().size());
		Term t1 = term1.getTerms().get(0);
		Term t2 = term2.getTerms().get(0);
		assertEquals("aux1_0_0", t1.getName());
		assertEquals("aux1_0_0", t2.getName());
	}
	
	//R(p(x),q(y))
	public void test_7() throws QueryParseException{
		
		OBDAMappingAxiom ax = new RDBMSOBDAMappingAxiom("id0"); 
		RDBMSSQLQuery sqlqeruy = new RDBMSSQLQuery("SELECT term1 as x, term2 as y from table1", null);
		QueryTerm v = new VariableTerm("x");
		QueryTerm v2 = new VariableTerm("y");
		ArrayList<QueryTerm> l1 = new ArrayList<QueryTerm>();
		ArrayList<QueryTerm> l2 = new ArrayList<QueryTerm>();
		l1.add(v);
		l2.add(v2);
		QueryTerm qt1 = new FunctionTerm(URI.create("p"), l1);
		QueryTerm qt = new FunctionTerm(URI.create("q"), l2);
		NamedProperty con = new ObjectProperty(URI.create("R"));
		QueryAtom qa = new BinaryQueryAtom(con, qt1, qt);
		ConjunctiveQuery cq = new ConjunctiveQuery();
		cq.addQueryAtom(qa);
		ax.setSourceQuery(sqlqeruy);
		ax.setTargetQuery(cq);
		
		Term qt2 = TermFactoryImpl.getInstance().createVariable("x");
		Vector<Term> tlist2 = new Vector<Term>();
		tlist2.add(qt2);
		Predicate predicate2 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("C"), 1);
		Atom qa2 = new AtomImpl(predicate2, tlist2);
		
		Term qt3 = TermFactoryImpl.getInstance().createVariable("x");
		Vector<Term> tlist3 = new Vector<Term>();
		tlist3.add(qt3);
		Predicate predicate3 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("q"), 1);
		Atom qah =new AtomImpl(predicate3, tlist3);
		
		Vector<Atom> body = new Vector<Atom>();
		body.add(qa2);
		CQIEImpl imp = new CQIEImpl(qah, body, false);
		
		Vector<OBDAMappingAxiom> maplist = new Vector<OBDAMappingAxiom>();
		maplist.add(ax);
		MappingViewManager viewMan = new MappingViewManager(maplist);
		ComplexMappingUnfolder cmu = new ComplexMappingUnfolder(maplist, viewMan);
		
		CQIE newquery = cmu.getFreshRuleFromMapping(ax, imp, 0);
		
		Atom head = newquery.getHead();
		Predicate p = head.getPredicate();
		assertEquals(con.getUri().toString(),p.getName().toString());
		assertEquals(2,head.getArity());
		ObjectVariableImpl term1 = (ObjectVariableImpl) head.getTerms().get(0);
		ObjectVariableImpl term2 = (ObjectVariableImpl) head.getTerms().get(1);
		assertEquals("p",term1.getName());
		assertEquals("q",term2.getName());
		assertEquals(1,term1.getTerms().size());
		assertEquals(1,term2.getTerms().size());
		Term t1 = term1.getTerms().get(0);
		Term t2 = term2.getTerms().get(0);
		assertEquals("aux1_0_0", t1.getName());
		assertEquals("aux1_1_0", t2.getName());
	}
	
	//R(p(x,z),q(y))
	public void test_10() throws QueryParseException{
		
		OBDAMappingAxiom ax = new RDBMSOBDAMappingAxiom("id0"); 
		RDBMSSQLQuery sqlqeruy = new RDBMSSQLQuery("SELECT term1 as x, term2 as y from table1", null);
		QueryTerm v = new VariableTerm("x");
		QueryTerm v2 = new VariableTerm("y");
		QueryTerm v3 = new VariableTerm("z");
		ArrayList<QueryTerm> l1 = new ArrayList<QueryTerm>();
		ArrayList<QueryTerm> l2 = new ArrayList<QueryTerm>();
		l1.add(v);
		l1.add(v3);
		l2.add(v2);
		QueryTerm qt1 = new FunctionTerm(URI.create("p"), l1);
		QueryTerm qt = new FunctionTerm(URI.create("q"), l2);
		NamedProperty con = new ObjectProperty(URI.create("R"));
		QueryAtom qa = new BinaryQueryAtom(con, qt1, qt);
		ConjunctiveQuery cq = new ConjunctiveQuery();
		cq.addQueryAtom(qa);
		ax.setSourceQuery(sqlqeruy);
		ax.setTargetQuery(cq);
		
		Term qt2 = TermFactoryImpl.getInstance().createVariable("x");
		Vector<Term> tlist2 = new Vector<Term>();
		tlist2.add(qt2);
		Predicate predicate2 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("C"), 1);
		Atom qa2 = new AtomImpl(predicate2, tlist2);
		
		Term qt3 = TermFactoryImpl.getInstance().createVariable("x");
		Vector<Term> tlist3 = new Vector<Term>();
		tlist3.add(qt3);
		Predicate predicate3 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("q"), 1);
		Atom qah =new AtomImpl(predicate3, tlist3);
		
		Vector<Atom> body = new Vector<Atom>();
		body.add(qa2);
		CQIEImpl imp = new CQIEImpl(qah, body, false);
		
		Vector<OBDAMappingAxiom> maplist = new Vector<OBDAMappingAxiom>();
		maplist.add(ax);
		MappingViewManager viewMan = new MappingViewManager(maplist);
		ComplexMappingUnfolder cmu = new ComplexMappingUnfolder(maplist, viewMan);
		
		CQIE newquery = cmu.getFreshRuleFromMapping(ax, imp, 0);
		
		Atom head = newquery.getHead();
		Predicate p = head.getPredicate();
		assertEquals(con.getUri().toString(),p.getName().toString());
		assertEquals(2,head.getArity());
		ObjectVariableImpl term1 = (ObjectVariableImpl) head.getTerms().get(0);
		ObjectVariableImpl term2 = (ObjectVariableImpl) head.getTerms().get(1);
		assertEquals("p",term1.getName());
		assertEquals("q",term2.getName());
		assertEquals(2,term1.getTerms().size());
		assertEquals(1,term2.getTerms().size());
		Term t1 = term1.getTerms().get(0);
		Term t3 = term1.getTerms().get(1);
		Term t2 = term2.getTerms().get(0);
		assertEquals("aux1_0_0", t1.getName());
		assertEquals("aux1_2_0", t2.getName());
		assertEquals("aux1_1_0", t3.getName());
	}
	
	//U(p(x),y)
	public void test_8() throws QueryParseException{
		
		OBDAMappingAxiom ax = new RDBMSOBDAMappingAxiom("id0"); 
		RDBMSSQLQuery sqlqeruy = new RDBMSSQLQuery("SELECT term1 as x, term2 as y from table1", null);
		QueryTerm v = new VariableTerm("x");
		QueryTerm v2 = new VariableTerm("y");
		ArrayList<QueryTerm> l1 = new ArrayList<QueryTerm>();
		l1.add(v);
		QueryTerm qt1 = new FunctionTerm(URI.create("p"), l1);
		NamedProperty con = new DataProperty(URI.create("U"));
		QueryAtom qa = new BinaryQueryAtom(con, qt1, v2);
		ConjunctiveQuery cq = new ConjunctiveQuery();
		cq.addQueryAtom(qa);
		ax.setSourceQuery(sqlqeruy);
		ax.setTargetQuery(cq);
		
		Term qt2 = TermFactoryImpl.getInstance().createVariable("x");
		Vector<Term> tlist2 = new Vector<Term>();
		tlist2.add(qt2);
		Predicate predicate2 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("C"), 1);
		Atom qa2 = new AtomImpl(predicate2, tlist2);
		
		Term qt3 = TermFactoryImpl.getInstance().createVariable("x");
		Vector<Term> tlist3 = new Vector<Term>();
		tlist3.add(qt3);
		Predicate predicate3 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("q"), 1);
		Atom qah =new AtomImpl(predicate3, tlist3);
		
		Vector<Atom> body = new Vector<Atom>();
		body.add(qa2);
		CQIEImpl imp = new CQIEImpl(qah, body, false);
		
		Vector<OBDAMappingAxiom> maplist = new Vector<OBDAMappingAxiom>();
		maplist.add(ax);
		MappingViewManager viewMan = new MappingViewManager(maplist);
		ComplexMappingUnfolder cmu = new ComplexMappingUnfolder(maplist, viewMan);
		
		CQIE newquery = cmu.getFreshRuleFromMapping(ax, imp, 0);
		
		Atom head = newquery.getHead();
		Predicate p = head.getPredicate();
		assertEquals(con.getUri().toString(),p.getName().toString());
		assertEquals(2,head.getArity());
		ObjectVariableImpl term1 = (ObjectVariableImpl) head.getTerms().get(0);
		VariableImpl term2 = (VariableImpl) head.getTerms().get(1);
		assertEquals("p",term1.getName());
		assertEquals("aux1_1_0",term2.getName());
		assertEquals(1,term1.getTerms().size());
		Term t1 = term1.getTerms().get(0);
		assertEquals("aux1_0_0", t1.getName());
	}
	
	//U(#,#)
	public void test_9() throws QueryParseException{
		
		OBDAMappingAxiom ax = new RDBMSOBDAMappingAxiom("id0"); 
		RDBMSSQLQuery sqlqeruy = new RDBMSSQLQuery("SELECT term1 as x, term2 as y from table1", null);
		QueryTerm v = new VariableTerm("x");
		QueryTerm v2 = new VariableTerm("y");
		ArrayList<QueryTerm> l1 = new ArrayList<QueryTerm>();
		l1.add(v);
		QueryTerm qt1 = new FunctionTerm(URI.create("p"), l1);
		NamedProperty con = new DataProperty(URI.create("U"));
		QueryAtom qa = new BinaryQueryAtom(con, qt1, v2);
		ConjunctiveQuery cq = new ConjunctiveQuery();
		cq.addQueryAtom(qa);
		ax.setSourceQuery(sqlqeruy);
		ax.setTargetQuery(cq);
		
		Term qt2 =new UndistinguishedVariable();
		Vector<Term> tlist2 = new Vector<Term>();
		tlist2.add(qt2);
		Predicate predicate2 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("C"), 1);
		Atom qa2 = new AtomImpl(predicate2, tlist2);
		
		Term qt3 = new UndistinguishedVariable();
		Vector<Term> tlist3 = new Vector<Term>();
		tlist3.add(qt3);
		Predicate predicate3 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("q"), 1);
		Atom qah =new AtomImpl(predicate3, tlist3);
		
		Vector<Atom> body = new Vector<Atom>();
		body.add(qa2);
		CQIEImpl imp = new CQIEImpl(qah, body, false);
		
		Vector<OBDAMappingAxiom> maplist = new Vector<OBDAMappingAxiom>();
		maplist.add(ax);
		MappingViewManager viewMan = new MappingViewManager(maplist);
		ComplexMappingUnfolder cmu = new ComplexMappingUnfolder(maplist, viewMan);
		
		CQIE newquery = cmu.getFreshRuleFromMapping(ax, imp, 0);
		
		Atom head = newquery.getHead();
		Predicate p = head.getPredicate();
		assertEquals(con.getUri().toString(),p.getName().toString());
		assertEquals(2,head.getArity());
		ObjectVariableImpl term1 = (ObjectVariableImpl) head.getTerms().get(0);
		VariableImpl term2 = (VariableImpl) head.getTerms().get(1);
		assertEquals("p",term1.getName());
		assertEquals("aux1_1_0",term2.getName());
		assertEquals(1,term1.getTerms().size());
		Term t1 = term1.getTerms().get(0);
		assertEquals("aux1_0_0", t1.getName());
	}
	
	//R(#,#)
	public void test_12() throws QueryParseException{
		
		OBDAMappingAxiom ax = new RDBMSOBDAMappingAxiom("id0"); 
		RDBMSSQLQuery sqlqeruy = new RDBMSSQLQuery("SELECT term1 as x, term2 as y from table1", null);
		QueryTerm v = new VariableTerm("x");
		QueryTerm v2 = new VariableTerm("y");
		ArrayList<QueryTerm> l1 = new ArrayList<QueryTerm>();
		ArrayList<QueryTerm> l2 = new ArrayList<QueryTerm>();
		l1.add(v);
		l2.add(v2);
		QueryTerm qt1 = new FunctionTerm(URI.create("p"), l1);
		QueryTerm qt = new FunctionTerm(URI.create("q"), l2);
		NamedProperty con = new ObjectProperty(URI.create("R"));
		QueryAtom qa = new BinaryQueryAtom(con, qt1, qt);
		ConjunctiveQuery cq = new ConjunctiveQuery();
		cq.addQueryAtom(qa);
		ax.setSourceQuery(sqlqeruy);
		ax.setTargetQuery(cq);
		
		Term qt2 = new UndistinguishedVariable();
		Vector<Term> tlist2 = new Vector<Term>();
		tlist2.add(qt2);
		Predicate predicate2 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("C"), 1);
		Atom qa2 = new AtomImpl(predicate2, tlist2);
		
		Term qt3 =  new UndistinguishedVariable();
		Vector<Term> tlist3 = new Vector<Term>();
		tlist3.add(qt3);
		Predicate predicate3 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("q"), 1);
		Atom qah =new AtomImpl(predicate3, tlist3);
		
		Vector<Atom> body = new Vector<Atom>();
		body.add(qa2);
		CQIEImpl imp = new CQIEImpl(qah, body, false);
		
		Vector<OBDAMappingAxiom> maplist = new Vector<OBDAMappingAxiom>();
		maplist.add(ax);
		MappingViewManager viewMan = new MappingViewManager(maplist);
		ComplexMappingUnfolder cmu = new ComplexMappingUnfolder(maplist, viewMan);
		
		CQIE newquery = cmu.getFreshRuleFromMapping(ax, imp, 0);
		
		Atom head = newquery.getHead();
		Predicate p = head.getPredicate();
		assertEquals(con.getUri().toString(),p.getName().toString());
		assertEquals(2,head.getArity());
		ObjectVariableImpl term1 = (ObjectVariableImpl) head.getTerms().get(0);
		ObjectVariableImpl term2 = (ObjectVariableImpl) head.getTerms().get(1);
		assertEquals("p",term1.getName());
		assertEquals("q",term2.getName());
		assertEquals(1,term1.getTerms().size());
		assertEquals(1,term2.getTerms().size());
		Term t1 = term1.getTerms().get(0);
		Term t2 = term2.getTerms().get(0);
		assertEquals("aux1_0_0", t1.getName());
		assertEquals("aux1_1_0", t2.getName());
	}
}
