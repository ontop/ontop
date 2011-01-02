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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;

import org.obda.owlrefplatform.core.ComplexMappingUnfolder;
import org.obda.owlrefplatform.core.MappingViewManager;
import org.obda.query.domain.Atom;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.DatalogProgram;
import org.obda.query.domain.Predicate;
import org.obda.query.domain.Term;
import org.obda.query.domain.imp.AtomImpl;
import org.obda.query.domain.imp.BasicPredicateFactoryImpl;
import org.obda.query.domain.imp.CQIEImpl;
import org.obda.query.domain.imp.DatalogProgramImpl;
import org.obda.query.domain.imp.ObjectVariableImpl;
import org.obda.query.domain.imp.TermFactoryImpl;
import org.obda.query.domain.imp.VariableImpl;

public class PartialEvaluationTest extends TestCase {

	
	/* Test C has one mapping C(p(x)) <-- Aux(a,y)
	 * The datalog program is q(x) :- C(x),C(x)
	 * 
	 * the expected partial evaluation should be q(p(aux1_0_1) :- Aux1(aux1_0_1, aux1_1_0),Aux1(aux1_0_1,aux1_1_1))
	 */
	
	public void test_C_px() throws Exception{
		
		try {
			//----- The Mapping ----
			OBDAMappingAxiom ax = new RDBMSOBDAMappingAxiom("id0"); 
			RDBMSSQLQuery sqlqeruy = new RDBMSSQLQuery("SELECT term1 as x from table1", null);
			QueryTerm v = new VariableTerm("x");
			ArrayList<QueryTerm> l = new ArrayList<QueryTerm>();
			l.add(v);
			QueryTerm qt = new FunctionTerm(URI.create("p"), l);
			NamedConcept con = new NamedConcept(URI.create("C"));
			QueryAtom qa = new ConceptQueryAtom(con, qt);
			ConjunctiveQuery cq = new ConjunctiveQuery();
			cq.addQueryAtom(qa);
			ax.setSourceQuery(sqlqeruy);
			ax.setTargetQuery(cq);
			List<OBDAMappingAxiom> maplist = new Vector<OBDAMappingAxiom>();
			maplist.add(ax);
			
			
			//--- The DatalogProgram
			Term qt1 = TermFactoryImpl.getInstance().createVariable("x");
			Vector<Term> tlist = new Vector<Term>();
			tlist.add(qt1);
			Predicate predicate = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("C"), 1);
			Atom qa1 = new AtomImpl(predicate, tlist);
			
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
			
			Vector<Atom> vex = new Vector<Atom>();
			vex.add(qa1);
			vex.add(qa2);
			CQIEImpl imp = new CQIEImpl(qah, vex, false);			
			DatalogProgram dp = new DatalogProgramImpl();
			dp.appendRule(imp);
			
			//--- MappingViewManager and Unfolder
			MappingViewManager viewMan = new MappingViewManager(maplist);
			ComplexMappingUnfolder cmu = new ComplexMappingUnfolder(maplist, viewMan);
			DatalogProgram result = cmu.unfold(dp);
			
			//---Checking the result 
			CQIE res1 = result.getRules().get(0);
			List<Atom> body = res1.getBody();
			assertEquals(2,body.size());
			Atom atom_1 = body.get(0);
			Atom atom_2 = body.get(1);
			assertEquals("http://obda.org/reformulation/auxPredicate#Aux1", body.get(0).getPredicate().getName().toString());
			assertEquals("http://obda.org/reformulation/auxPredicate#Aux1", body.get(1).getPredicate().getName().toString());
			assertEquals(1, atom_1.getArity());
			assertEquals(1, atom_2.getArity());
			List<Term> t1 = atom_1.getTerms();
			List<Term> t2 = atom_2.getTerms();
			assertEquals("aux1_0_1", t1.get(0).getName());
			assertEquals("aux1_0_1", t2.get(0).getName());
			
			Atom head = res1.getHead();
			List<Term> ht = head.getTerms();
			assertEquals(1, ht.size());
			ObjectVariableImpl ov = (ObjectVariableImpl) ht.get(0);
			List<Term> vars = ov.getTerms();
			
			assertEquals(1, vars.size());
			assertEquals("aux1_0_1", vars.get(0).getName());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void test_R_px_py() throws Exception{
		
		try {
			//----- The Mapping ----
			OBDAMappingAxiom ax = new RDBMSOBDAMappingAxiom("id0"); 
			RDBMSSQLQuery sqlqeruy = new RDBMSSQLQuery("SELECT term1 as x, term2 as y from table1", null);
			QueryTerm v1 = new VariableTerm("x");
			ArrayList<QueryTerm> l1 = new ArrayList<QueryTerm>();
			l1.add(v1);
			QueryTerm v2 = new VariableTerm("y");
			ArrayList<QueryTerm> l2 = new ArrayList<QueryTerm>();
			l2.add(v2);
			QueryTerm qt11 = new FunctionTerm(URI.create("p"), l1);
			QueryTerm qt21 = new FunctionTerm(URI.create("p"), l2);
			NamedProperty con = new ObjectProperty(URI.create("R"));
			QueryAtom qa = new BinaryQueryAtom(con, qt11, qt21);
			ConjunctiveQuery cq = new ConjunctiveQuery();
			cq.addQueryAtom(qa);
			ax.setSourceQuery(sqlqeruy);
			ax.setTargetQuery(cq);
			List<OBDAMappingAxiom> maplist = new Vector<OBDAMappingAxiom>();
			maplist.add(ax);
			
			
			//--- The DatalogProgram
			Term qt1x = TermFactoryImpl.getInstance().createVariable("x");
			Term qt1y = TermFactoryImpl.getInstance().createVariable("y");
			Vector<Term> tlist = new Vector<Term>();
			tlist.add(qt1x);
			tlist.add(qt1y);
			Predicate predicate = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("R"), 2);
			Atom qa1 = new AtomImpl(predicate, tlist);
			
			Term qt2x = TermFactoryImpl.getInstance().createVariable("x");
			Term qt2y = TermFactoryImpl.getInstance().createVariable("y");
			Vector<Term> tlist2 = new Vector<Term>();
			tlist2.add(qt2x);
			tlist2.add(qt2y);
			Predicate predicate2 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("R"), 2);
			Atom qa2 = new AtomImpl(predicate2, tlist2);
			
			Term qt3x = TermFactoryImpl.getInstance().createVariable("x");
			Term qt3y = TermFactoryImpl.getInstance().createVariable("y");
			Vector<Term> tlist3 = new Vector<Term>();
			tlist3.add(qt3x);
			tlist3.add(qt3y);
			Predicate predicate3 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("q"), 2);
			Atom qah =new AtomImpl(predicate3, tlist3);
			
			Vector<Atom> vex = new Vector<Atom>();
			vex.add(qa1);
			vex.add(qa2);
			CQIEImpl imp = new CQIEImpl(qah, vex, false);			
			DatalogProgram dp = new DatalogProgramImpl();
			dp.appendRule(imp);
			
			//--- MappingViewManager and Unfolder
			MappingViewManager viewMan = new MappingViewManager(maplist);
			ComplexMappingUnfolder cmu = new ComplexMappingUnfolder(maplist, viewMan);
			DatalogProgram result = cmu.unfold(dp);
			
			//---Checking the result 
			CQIE res1 = result.getRules().get(0);
			List<Atom> body = res1.getBody();
			assertEquals(2,body.size());
			Atom atom_1 = body.get(0);
			Atom atom_2 = body.get(1);
			assertEquals("http://obda.org/reformulation/auxPredicate#Aux1", body.get(0).getPredicate().getName().toString());
			assertEquals("http://obda.org/reformulation/auxPredicate#Aux1", body.get(1).getPredicate().getName().toString());
			assertEquals(2, atom_1.getArity());
			assertEquals(2, atom_2.getArity());
			List<Term> t1 = atom_1.getTerms();
			List<Term> t2 = atom_2.getTerms();
			assertEquals("aux1_0_1", t1.get(0).getName());
			assertEquals("aux1_1_1", t1.get(1).getName());
			assertEquals("aux1_0_1", t2.get(0).getName());
			assertEquals("aux1_1_1", t2.get(1).getName());
			
			Atom head = res1.getHead();
			List<Term> ht = head.getTerms();
			assertEquals(2, ht.size());
			ObjectVariableImpl ov = (ObjectVariableImpl) ht.get(0);
			List<Term> vars = ov.getTerms();
			assertEquals(1, vars.size());
			assertEquals("aux1_0_1", vars.get(0).getName());
			ObjectVariableImpl ov2 = (ObjectVariableImpl) ht.get(1);
			List<Term> vars2 = ov2.getTerms();
			assertEquals(1, vars2.size());
			assertEquals("aux1_1_1", vars2.get(0).getName());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void test_U_px_y() throws Exception{
		
		try {
			//----- The Mapping ----
			OBDAMappingAxiom ax = new RDBMSOBDAMappingAxiom("id0"); 
			RDBMSSQLQuery sqlqeruy = new RDBMSSQLQuery("SELECT term1 as x, term2 as y from table1", null);
			QueryTerm v1 = new VariableTerm("x");
			ArrayList<QueryTerm> l1 = new ArrayList<QueryTerm>();
			l1.add(v1);
			QueryTerm v2 = new VariableTerm("y");
			QueryTerm qt11 = new FunctionTerm(URI.create("p"), l1);
			NamedProperty con = new DataProperty(URI.create("U"));
			QueryAtom qa = new BinaryQueryAtom(con, qt11, v2);
			ConjunctiveQuery cq = new ConjunctiveQuery();
			cq.addQueryAtom(qa);
			ax.setSourceQuery(sqlqeruy);
			ax.setTargetQuery(cq);
			List<OBDAMappingAxiom> maplist = new Vector<OBDAMappingAxiom>();
			maplist.add(ax);
			
			
			//--- The DatalogProgram
			Term qt1x = TermFactoryImpl.getInstance().createVariable("x");
			Term qt1y = TermFactoryImpl.getInstance().createVariable("y");
			Vector<Term> tlist = new Vector<Term>();
			tlist.add(qt1x);
			tlist.add(qt1y);
			Predicate predicate = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("U"), 2);
			Atom qa1 = new AtomImpl(predicate, tlist);
			
			Term qt2x = TermFactoryImpl.getInstance().createVariable("x");
			Term qt2y = TermFactoryImpl.getInstance().createVariable("y");
			Vector<Term> tlist2 = new Vector<Term>();
			tlist2.add(qt2x);
			tlist2.add(qt2y);
			Predicate predicate2 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("U"), 2);
			Atom qa2 = new AtomImpl(predicate2, tlist2);
			
			Term qt3x = TermFactoryImpl.getInstance().createVariable("x");
			Term qt3y = TermFactoryImpl.getInstance().createVariable("y");
			Vector<Term> tlist3 = new Vector<Term>();
			tlist3.add(qt3x);
			tlist3.add(qt3y);
			Predicate predicate3 = BasicPredicateFactoryImpl.getInstance().getPredicate(URI.create("q"), 2);
			Atom qah =new AtomImpl(predicate3, tlist3);
			
			Vector<Atom> vex = new Vector<Atom>();
			vex.add(qa1);
			vex.add(qa2);
			CQIEImpl imp = new CQIEImpl(qah, vex, false);			
			DatalogProgram dp = new DatalogProgramImpl();
			dp.appendRule(imp);
			
			//--- MappingViewManager and Unfolder
			MappingViewManager viewMan = new MappingViewManager(maplist);
			ComplexMappingUnfolder cmu = new ComplexMappingUnfolder(maplist, viewMan);
			DatalogProgram result = cmu.unfold(dp);
			
			//---Checking the result 
			CQIE res1 = result.getRules().get(0);
			List<Atom> body = res1.getBody();
			assertEquals(2,body.size());
			Atom atom_1 = body.get(0);
			Atom atom_2 = body.get(1);
			assertEquals("http://obda.org/reformulation/auxPredicate#Aux1", body.get(0).getPredicate().getName().toString());
			assertEquals("http://obda.org/reformulation/auxPredicate#Aux1", body.get(1).getPredicate().getName().toString());
			assertEquals(2, atom_1.getArity());
			assertEquals(2, atom_2.getArity());
			List<Term> t1 = atom_1.getTerms();
			List<Term> t2 = atom_2.getTerms();
			assertEquals("aux1_0_1", t1.get(0).getName());
			assertEquals("aux1_1_1", t1.get(1).getName());
			assertEquals("aux1_0_1", t2.get(0).getName());
			assertEquals("aux1_1_1", t2.get(1).getName());
			
			Atom head = res1.getHead();
			List<Term> ht = head.getTerms();
			assertEquals(2, ht.size());
			ObjectVariableImpl ov = (ObjectVariableImpl) ht.get(0);
			List<Term> vars = ov.getTerms();
			assertEquals(1, vars.size());
			assertEquals("aux1_0_1", vars.get(0).getName());
			VariableImpl ov2 = (VariableImpl) ht.get(1);
			assertEquals("aux1_1_1", ov2.getName());
			
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(false, true);
		}
	}
}
