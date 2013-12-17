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
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAGBuilder;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAGImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxGraph;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImplOnGraph;

import java.util.Set;

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

		DAGImpl res = DAGBuilder.getDAG(ontology);
		TBoxReasonerImpl reasoner = new TBoxReasonerImpl(res);
		reasoner.convertIntoChainDAG();

		assertTrue(reasoner.getDescendants(ac).contains(reasoner.getEquivalences(bc)));
		assertTrue(reasoner.getDescendants(ac).contains(reasoner.getEquivalences(cc)));
		int numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(ac)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 2);

		assertTrue(reasoner.getDescendants(bc).contains(reasoner.getEquivalences(cc)));
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(bc)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 1);
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(cc)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 0);
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
		TBoxGraph res = TBoxGraph.getGraph(ontology);

		
		
//		for (Description nodes: res.vertexSet()) {
//			System.out.println("---- " + nodes);
//		}
		
		TBoxReasonerImplOnGraph reasoner= new TBoxReasonerImplOnGraph(res);
		reasoner.convertIntoChainDAG();

		
		
		assertTrue(reasoner.getDescendants(ac).contains(reasoner.getEquivalences(er)));
		assertTrue(reasoner.getDescendants(ac).contains(reasoner.getEquivalences(ier)));
		assertTrue(reasoner.getDescendants(ac).contains(reasoner.getEquivalences(cc)));
		int numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(ac)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 3);

		assertTrue(reasoner.getDescendants(er).contains(reasoner.getEquivalences(cc)));
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(er)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 1);

		assertTrue(reasoner.getDescendants(ier).contains(reasoner.getEquivalences(cc)));
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(ier)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 1);
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(cc)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 0);
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

		DAGImpl dag221 = DAGBuilder.getDAG(ontology);
		TBoxReasonerImpl reasoner= new TBoxReasonerImpl(dag221);
		reasoner.convertIntoChainDAG();

		assertTrue(reasoner.getDescendants(ac).contains(reasoner.getEquivalences(er)));
		assertTrue(reasoner.getDescendants(ac).contains(reasoner.getEquivalences(ier)));
		assertTrue(reasoner.getDescendants(ac).contains(reasoner.getEquivalences(cc)));
		assertTrue(reasoner.getDescendants(ac).contains(reasoner.getEquivalences(bc)));
		int numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(ac)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 4);

		assertTrue(reasoner.getDescendants(dc).contains(reasoner.getEquivalences(er)));
		assertTrue(reasoner.getDescendants(dc).contains(reasoner.getEquivalences(ier)));
		assertTrue(reasoner.getDescendants(dc).contains(reasoner.getEquivalences(cc)));
		assertTrue(reasoner.getDescendants(dc).contains(reasoner.getEquivalences(bc)));
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(dc)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 4);

		assertTrue(reasoner.getDescendants(er).contains(reasoner.getEquivalences(bc)));
		assertTrue(reasoner.getDescendants(er).contains(reasoner.getEquivalences(cc)));
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(er)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 2);

		assertTrue(reasoner.getDescendants(ier).contains(reasoner.getEquivalences(bc)));
		assertTrue(reasoner.getDescendants(ier).contains(reasoner.getEquivalences(cc)));
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(ier)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 2);

		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(bc)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 0);
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(cc)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 0);

	}

	

}

