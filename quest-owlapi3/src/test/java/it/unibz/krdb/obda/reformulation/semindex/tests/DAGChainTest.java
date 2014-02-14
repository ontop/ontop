/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */

package it.unibz.krdb.obda.reformulation.semindex.tests;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.EquivalencesDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Test_TBoxReasonerImplOnGraph;

import org.jgrapht.graph.DefaultEdge;

import junit.framework.TestCase;


public class DAGChainTest extends TestCase {

	SemanticIndexHelper						helper				= new SemanticIndexHelper();

	private static final OBDADataFactory	predicateFactory	= OBDADataFactoryImpl.getInstance();
	private static final OntologyFactory	descFactory			= new OntologyFactoryImpl();

	public void test_simple_isa() {
		Ontology ontology = OntologyFactoryImpl.getInstance().createOntology("");

		Predicate a = predicateFactory.getPredicate("a", 1);
		Predicate b = predicateFactory.getPredicate("b", 1);
		Predicate c = predicateFactory.getPredicate("c", 1);

		OClass ac = descFactory.createClass(a);
		OClass bc = descFactory.createClass(b);
		OClass cc = descFactory.createClass(c);

		ontology.addConcept(ac.getPredicate());
		ontology.addConcept(bc.getPredicate());
		ontology.addConcept(cc.getPredicate());

		ontology.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(bc, ac));
		ontology.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(cc, bc));

		//TBoxReasonerImpl reasoner0 = new TBoxReasonerImpl(ontology);
		TBoxReasonerImpl reasoner = TBoxReasonerImpl.getChainReasoner(ontology);
		EquivalencesDAG<BasicClassDescription> classes = reasoner.getClasses();
		
		Equivalences<BasicClassDescription> ac0 = classes.getVertex(ac);
		Equivalences<BasicClassDescription> bc0 = classes.getVertex(bc);
		Equivalences<BasicClassDescription> cc0 = classes.getVertex(cc);
		
		assertTrue(classes.getSub(ac0).contains(bc0));
		assertTrue(classes.getSub(ac0).contains(cc0));
		int numDescendants=0;
		for(Equivalences<BasicClassDescription> equiDescendants: classes.getSub(ac0)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 3); // getDescendants is reflexive

		assertTrue(classes.getSub(bc0).contains(cc0));
		numDescendants=0;
		for(Equivalences<BasicClassDescription> equiDescendants: classes.getSub(bc0)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 2);  // getDescendants is reflexive
		numDescendants=0;
		for(Equivalences<BasicClassDescription> equiDescendants: classes.getSub(cc0)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 1);  // getDescendants is reflexive
	}

	public void test_exists_simple() {
		Ontology ontology = OntologyFactoryImpl.getInstance().createOntology("");

		Predicate a = predicateFactory.getClassPredicate("a");
		Predicate r = predicateFactory.getObjectPropertyPredicate("r");
		Predicate c = predicateFactory.getClassPredicate("c");
		OClass ac = descFactory.createClass(a);
		PropertySomeRestriction er = descFactory.getPropertySomeRestriction(r, false);
		PropertySomeRestriction ier = descFactory.getPropertySomeRestriction(r, true);
		OClass cc = descFactory.createClass(c);

		ontology.addConcept(ac.getPredicate());
		ontology.addConcept(cc.getPredicate());

		ontology.addRole(er.getPredicate());
		ontology.addRole(ier.getPredicate());
		
		System.out.println(er);
		System.out.println(ac);
		System.out.println(cc);
		System.out.println(ier);

		ontology.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(er, ac));
		ontology.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(cc, ier));
		
		//generate Graph
		TBoxReasonerImpl res0 = new  TBoxReasonerImpl(ontology);
		//DefaultDirectedGraph<Description,DefaultEdge> res1 = res0.getGraph();

		
		
//		for (Description nodes: res.vertexSet()) {
//			System.out.println("---- " + nodes);
//		}
		
		Test_TBoxReasonerImplOnGraph reasoner= new Test_TBoxReasonerImplOnGraph(res0);
		reasoner.convertIntoChainDAG();

		
		
		assertTrue(reasoner.getSubClasses(ac).contains(reasoner.getEquivalences(er)));
		assertTrue(reasoner.getSubClasses(ac).contains(reasoner.getEquivalences(ier)));
		assertTrue(reasoner.getSubClasses(ac).contains(reasoner.getEquivalences(cc)));
		int numDescendants=0;
		for(Equivalences<BasicClassDescription> equiDescendants: reasoner.getSubClasses(ac)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 4);  // getDescendants is reflexive

		assertTrue(reasoner.getSubClasses(er).contains(reasoner.getEquivalences(cc)));
		numDescendants=0;
		for(Equivalences<BasicClassDescription> equiDescendants: reasoner.getSubClasses(er)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 2);  // getDescendants is reflexive

		assertTrue(reasoner.getSubClasses(ier).contains(reasoner.getEquivalences(cc)));
		numDescendants=0;
		for(Equivalences<BasicClassDescription> equiDescendants: reasoner.getSubClasses(ier)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 2);  // getDescendants is reflexive
		numDescendants=0;
		for(Equivalences<BasicClassDescription> equiDescendants: reasoner.getSubClasses(cc)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 1);  // getDescendants is reflexive
	}

	public void test_exists_complex() {

		Ontology ontology = OntologyFactoryImpl.getInstance().createOntology("");

		Predicate a = predicateFactory.getPredicate("a", 1);
		Predicate r = predicateFactory.getPredicate("r", 2);
		Predicate c = predicateFactory.getPredicate("c", 1);
		Predicate b = predicateFactory.getPredicate("b", 1);
		Predicate d = predicateFactory.getPredicate("d", 1);

		OClass ac = descFactory.createClass(a);
		PropertySomeRestriction er = descFactory.getPropertySomeRestriction(r, false);
		PropertySomeRestriction ier = descFactory.getPropertySomeRestriction(r, true);
		OClass cc = descFactory.createClass(c);
		OClass bc = descFactory.createClass(b);
		OClass dc = descFactory.createClass(d);

		ontology.addConcept(ac.getPredicate());
		ontology.addConcept(cc.getPredicate());
		ontology.addConcept(bc.getPredicate());
		ontology.addConcept(dc.getPredicate());

		ontology.addRole(er.getPredicate());
		ontology.addRole(ier.getPredicate());

		ontology.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(er, ac));
		ontology.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(cc, ier));
		ontology.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(bc, er));
		ontology.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(ier, dc));

		//DAGImpl dag221 = DAGBuilder.getDAG(ontology);
		//TBoxReasonerImpl reasoner221 = new TBoxReasonerImpl(dag221);
		//DAGImpl dagChain221 = reasoner221.getChainDAG();
		TBoxReasonerImpl reasoner = TBoxReasonerImpl.getChainReasoner(ontology);

		EquivalencesDAG<BasicClassDescription> classes = reasoner.getClasses();
		
		Equivalences<BasicClassDescription> ac0 = classes.getVertex(ac);
		Equivalences<BasicClassDescription> bc0 = classes.getVertex(bc);
		Equivalences<BasicClassDescription> cc0 = classes.getVertex(cc);
		Equivalences<BasicClassDescription> dc0 = classes.getVertex(dc);
		Equivalences<BasicClassDescription> er0 = classes.getVertex(er);
		Equivalences<BasicClassDescription> ier0 = classes.getVertex(ier);
		
		assertTrue(classes.getSub(ac0).contains(er0));
		assertTrue(classes.getSub(ac0).contains(ier0));
		assertTrue(classes.getSub(ac0).contains(cc0));
		assertTrue(classes.getSub(ac0).contains(bc0));
		int numDescendants=0;
		for(Equivalences<BasicClassDescription> equiDescendants: classes.getSub(ac0)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 5);  // getDescendants is reflexive

		assertTrue(classes.getSub(dc0).contains(er0));
		assertTrue(classes.getSub(dc0).contains(ier0));
		assertTrue(classes.getSub(dc0).contains(cc0));
		assertTrue(classes.getSub(dc0).contains(bc0));
		numDescendants=0;
		for(Equivalences<BasicClassDescription> equiDescendants: classes.getSub(dc0)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 5);  // getDescendants is reflexive

		assertTrue(classes.getSub(er0).contains(bc0));
		assertTrue(classes.getSub(er0).contains(cc0));
		numDescendants=0;
		for(Equivalences<BasicClassDescription> equiDescendants: classes.getSub(er0)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 3);  // getDescendants is reflexive

		assertTrue(classes.getSub(ier0).contains(bc0));
		assertTrue(classes.getSub(ier0).contains(cc0));
		numDescendants=0;
		for(Equivalences<BasicClassDescription> equiDescendants: classes.getSub(ier0)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 3);  // getDescendants is reflexive

		numDescendants=0;
		for(Equivalences<BasicClassDescription> equiDescendants: classes.getSub(bc0)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 1);  // getDescendants is reflexive
		numDescendants=0;
		for(Equivalences<BasicClassDescription> equiDescendants: classes.getSub(cc0)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 1);  // getDescendants is reflexive

	}

	

}

