package it.unibz.krdb.obda.unfolding;


import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.ComplexMappingUnfolder;
import it.unibz.krdb.obda.owlrefplatform.core.viewmanager.MappingViewManager;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

public class BasicUnfoldingTests extends TestCase {

	ComplexMappingUnfolder unfolder = null;
	MappingViewManager viewman = null;
	List<OBDAMappingAxiom> mappings = null;
	
	
	public void setUp() throws Exception {
		mappings = new LinkedList<OBDAMappingAxiom>();
		/**
		 * Setting up 3 mappings
		 * 
		 * select x from t -> B(b($x))
		 */
		
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		Atom a = fac.getAtom(fac.getPredicate(URI.create("B"), 1), fac.getFunctionalTerm(fac.getPredicate(URI.create("b"),1), fac.getVariable("x")));
		Atom q = fac.getAtom(fac.getPredicate(URI.create("q"), 1), fac.getVariable("x"));
		mappings.add(fac.getRDBMSMappingAxiom("select x from t", fac.getCQIE(q, a)));
		
		/***
		 * select x y from -> E(p(?x),b(?y))
		 */
		a = fac.getAtom(fac.getPredicate(URI.create("E"), 2), fac.getFunctionalTerm(fac.getPredicate(URI.create("p"),1), fac.getVariable("x")), fac.getFunctionalTerm(fac.getPredicate(URI.create("b"),1), fac.getVariable("y")));
		q = fac.getAtom(fac.getPredicate(URI.create("q"), 2), fac.getVariable("x"), fac.getVariable("y"));
		mappings.add(fac.getRDBMSMappingAxiom("select x, y from", fac.getCQIE(q, a)));
		
		/***
		 * select x from m-> A(b(?x),m(?x))
		 */
		a = fac.getAtom(fac.getPredicate(URI.create("A"), 2), fac.getFunctionalTerm(fac.getPredicate(URI.create("b"),1), fac.getVariable("x")), fac.getFunctionalTerm(fac.getPredicate(URI.create("m"),1), fac.getVariable("x")));
		q = fac.getAtom(fac.getPredicate(URI.create("q"), 2), fac.getVariable("x"), fac.getVariable("y"));
		mappings.add(fac.getRDBMSMappingAxiom("select x from m", fac.getCQIE(q, a)));
		
		viewman = new MappingViewManager(mappings);
		unfolder = new ComplexMappingUnfolder(mappings, viewman);
		
	}
	
	public void testUnfolding1() throws Exception {
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		
		/*
		 * q(?x,?m) :- A(?x,?m), E(?z,?x)
		 */
		CQIE query = fac.getCQIE(fac.getAtom(fac.getPredicate(URI.create("q"), 2), fac.getVariable("x"), fac.getVariable("m")), fac.getAtom(fac.getPredicate(URI.create("A"), 2), fac.getVariable("x"), fac.getVariable("m")));
		query.getBody().add(fac.getAtom(fac.getPredicate(URI.create("E"), 2), fac.getVariable("z"), fac.getVariable("x")));
		
		DatalogProgram p = fac.getDatalogProgram(query);
		DatalogProgram p2 = unfolder.unfold(p);
		
		assertTrue(p2.toString(), p2.getRules().size() == 1);
		
		Function t1 = (Function)p2.getRules().get(0).getHead().getTerms().get(0);
		Function t2 = (Function)p2.getRules().get(0).getHead().getTerms().get(1);
		
		assertTrue(t1.toString(), t1.equals(fac.getFunctionalTerm(fac.getPredicate(URI.create("b"),1), fac.getVariable("aux3_0_0"))));
		assertTrue(t2.toString(), t2.equals(fac.getFunctionalTerm(fac.getPredicate(URI.create("m"),1), fac.getVariable("aux3_0_0"))));
		
	}
	
	public void testUnfolding2() throws Exception {
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		
		/*
		 * q(?x,?m) :- A(?x,?m), B(?x)
		 */
		CQIE query = fac.getCQIE(fac.getAtom(fac.getPredicate(URI.create("q"), 2), fac.getVariable("x"), fac.getVariable("m")), fac.getAtom(fac.getPredicate(URI.create("A"), 2), fac.getVariable("x"), fac.getVariable("m")));
		query.getBody().add(fac.getAtom(fac.getPredicate(URI.create("B"), 1), fac.getVariable("x")));
		
		DatalogProgram p = fac.getDatalogProgram(query);
		DatalogProgram p2 = unfolder.unfold(p);
		
		assertTrue(p2.toString(), p2.getRules().size() == 1);
		
		Function t1 = (Function)p2.getRules().get(0).getHead().getTerms().get(0);
		Function t2 = (Function)p2.getRules().get(0).getHead().getTerms().get(1);
		
		assertTrue(t1.toString(), t1.equals(fac.getFunctionalTerm(fac.getPredicate(URI.create("b"),1), fac.getVariable("aux3_0_0"))));
		assertTrue(t2.toString(), t2.equals(fac.getFunctionalTerm(fac.getPredicate(URI.create("m"),1), fac.getVariable("aux3_0_0"))));
		
	}
	
	public void testUnfolding3() throws Exception {
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		
		/*
		 * q(?x,?m) :- E(?z,?x), A(?x,?m)
		 */
		CQIE query = fac.getCQIE(fac.getAtom(fac.getPredicate(URI.create("q"), 2), fac.getVariable("x"), fac.getVariable("m")),fac.getAtom(fac.getPredicate(URI.create("E"), 2), fac.getVariable("z"), fac.getVariable("x")));
		query.getBody().add( fac.getAtom(fac.getPredicate(URI.create("A"), 2), fac.getVariable("x"), fac.getVariable("m")));
		
		DatalogProgram p = fac.getDatalogProgram(query);
		DatalogProgram p2 = unfolder.unfold(p);
		
		assertTrue(p2.toString(), p2.getRules().size() == 1);
		
		Function t1 = (Function)p2.getRules().get(0).getHead().getTerms().get(0);
		Function t2 = (Function)p2.getRules().get(0).getHead().getTerms().get(1);
		
		assertTrue(t1.toString(), t1.equals(fac.getFunctionalTerm(fac.getPredicate(URI.create("b"),1), fac.getVariable("aux2_1_0"))));
		assertTrue(t2.toString(), t2.equals(fac.getFunctionalTerm(fac.getPredicate(URI.create("m"),1), fac.getVariable("aux2_1_0"))));
		
	}
	
	public void testUnfolding4() throws Exception {
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		
		/*
		 * q(?x,?m) :- B(?x), A(?x,?m)
		 */
		CQIE query = fac.getCQIE(fac.getAtom(fac.getPredicate(URI.create("q"), 2), fac.getVariable("x"), fac.getVariable("m")), fac.getAtom(fac.getPredicate(URI.create("B"), 1), fac.getVariable("x")));
		query.getBody().add(fac.getAtom(fac.getPredicate(URI.create("A"), 2), fac.getVariable("x"), fac.getVariable("m")));
		
		DatalogProgram p = fac.getDatalogProgram(query);
		DatalogProgram p2 = unfolder.unfold(p);
		
		assertTrue(p2.toString(), p2.getRules().size() == 1);
		
		Function t1 = (Function)p2.getRules().get(0).getHead().getTerms().get(0);
		Function t2 = (Function)p2.getRules().get(0).getHead().getTerms().get(1);
		
		assertTrue(t1.toString(), t1.equals(fac.getFunctionalTerm(fac.getPredicate(URI.create("b"),1), fac.getVariable("aux1_0_0"))));
		assertTrue(t2.toString(), t2.equals(fac.getFunctionalTerm(fac.getPredicate(URI.create("m"),1), fac.getVariable("aux1_0_0"))));
	}	
	
	public void testFreshRule5() {
		/***
		 * Rule 1 is of the form 
		 * 
		 * A(b(aux3_0), m(aux3_0)) :- http://obda.org/reformulation/auxPredicate#Aux3(aux3_0)
		 * 
		 * if I ask for fresh rules it should give me
		 * 
		 * A(b(aux3_0_0), m(aux3_0_0)) :- http://obda.org/reformulation/auxPredicate#Aux3(aux3_0)
		 * next  
		 * A(b(aux3_0_1), m(aux3_0_1)) :- http://obda.org/reformulation/auxPredicate#Aux3(aux3_0)
		 * 
		 */
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

		DatalogProgram mapprogram = unfolder.getCompilationOfMappings();
		
		CQIE  fresh = unfolder.getFreshRule(mapprogram.getRules().get(2), 0);
		Function t1 = (Function)fresh.getHead().getTerms().get(0);
		Function t2 = (Function)fresh.getHead().getTerms().get(1);
		
		assertTrue(t1.toString(), t1.equals(fac.getFunctionalTerm(fac.getPredicate(URI.create("b"),1), fac.getVariable("aux3_0_0"))));
		assertTrue(t2.toString(), t2.equals(fac.getFunctionalTerm(fac.getPredicate(URI.create("m"),1), fac.getVariable("aux3_0_0"))));
		
		
		 fresh = unfolder.getFreshRule(mapprogram.getRules().get(2), 1);
		t1 = (Function)fresh.getHead().getTerms().get(0);
		 t2 = (Function)fresh.getHead().getTerms().get(1);
		
		assertTrue(t1.toString(), t1.equals(fac.getFunctionalTerm(fac.getPredicate(URI.create("b"),1), fac.getVariable("aux3_0_1"))));
		assertTrue(t2.toString(), t2.equals(fac.getFunctionalTerm(fac.getPredicate(URI.create("m"),1), fac.getVariable("aux3_0_1"))));
		
	}

}
