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
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.GraphBuilderImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.GraphImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

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

		TBoxReasonerImpl reasoner= new TBoxReasonerImpl(ontology, false);
		DAG res = reasoner.getDAG();
		reasoner.getChainDAG();

		assertTrue(reasoner.getDescendants(ac, false).contains(reasoner.getEquivalences(bc, false)));
		assertTrue(reasoner.getDescendants(ac, false).contains(reasoner.getEquivalences(cc, false)));
		int numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(ac, false)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 2);

		assertTrue(reasoner.getDescendants(bc, false).contains(reasoner.getEquivalences(cc, false)));
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(bc, false)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 1);
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(cc, false)){
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
		GraphBuilderImpl change= new GraphBuilderImpl(ontology);
		
		GraphImpl res = (GraphImpl) change.getGraph();

		
		
//		for (Description nodes: res.vertexSet()) {
//			System.out.println("---- " + nodes);
//		}
		
		TBoxReasonerImpl reasoner= new TBoxReasonerImpl(res);
		reasoner.getChainDAG();

		
		
		assertTrue(reasoner.getDescendants(ac, false).contains(reasoner.getEquivalences(er, false)));
		assertTrue(reasoner.getDescendants(ac, false).contains(reasoner.getEquivalences(ier, false)));
		assertTrue(reasoner.getDescendants(ac, false).contains(reasoner.getEquivalences(cc, false)));
		int numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(ac, false)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 3);

		assertTrue(reasoner.getDescendants(er, false).contains(reasoner.getEquivalences(cc, false)));
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(er, false)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 1);

		assertTrue(reasoner.getDescendants(ier, false).contains(reasoner.getEquivalences(cc, false)));
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(ier, false)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 1);
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(cc, false)){
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

		TBoxReasonerImpl reasoner= new TBoxReasonerImpl(ontology, false);
		reasoner.getChainDAG();

		assertTrue(reasoner.getDescendants(ac, false).contains(reasoner.getEquivalences(er, false)));
		assertTrue(reasoner.getDescendants(ac, false).contains(reasoner.getEquivalences(ier, false)));
		assertTrue(reasoner.getDescendants(ac, false).contains(reasoner.getEquivalences(cc, false)));
		assertTrue(reasoner.getDescendants(ac, false).contains(reasoner.getEquivalences(bc, false)));
		int numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(ac, false)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 4);

		assertTrue(reasoner.getDescendants(dc, false).contains(reasoner.getEquivalences(er, false)));
		assertTrue(reasoner.getDescendants(dc, false).contains(reasoner.getEquivalences(ier, false)));
		assertTrue(reasoner.getDescendants(dc, false).contains(reasoner.getEquivalences(cc, false)));
		assertTrue(reasoner.getDescendants(dc, false).contains(reasoner.getEquivalences(bc, false)));
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(dc, false)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 4);

		assertTrue(reasoner.getDescendants(er, false).contains(reasoner.getEquivalences(bc, false)));
		assertTrue(reasoner.getDescendants(er, false).contains(reasoner.getEquivalences(cc, false)));
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(er, false)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 2);

		assertTrue(reasoner.getDescendants(ier, false).contains(reasoner.getEquivalences(bc, false)));
		assertTrue(reasoner.getDescendants(ier, false).contains(reasoner.getEquivalences(cc, false)));
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(ier, false)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 2);

		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(bc, false)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 0);
		numDescendants=0;
		for(Set<Description> equiDescendants: reasoner.getDescendants(cc, false)){
			numDescendants+=equiDescendants.size();
		}
		assertEquals(numDescendants, 0);

	}

	

}

