
package it.unibz.krdb.obda.reformulation.semindex.tests;

/*
 * #%L
 * ontop-quest-owlapi3
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


import java.util.Set;

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
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.krdb.obda.quest.dag.TestTBoxReasonerImpl_OnGraph;
import junit.framework.TestCase;


public class DAGChainTest extends TestCase {

	SemanticIndexHelper						helper				= new SemanticIndexHelper();

	private static final OBDADataFactory	predicateFactory	= OBDADataFactoryImpl.getInstance();
	private static final OntologyFactory	descFactory			= OntologyFactoryImpl.getInstance();

	private static <T> int sizeOf(Set<Equivalences<T>> set) {
		int size = 0;
		for(Equivalences<T> e: set){
			size += e.size();
		}
		return size;
	}
	
	public void test_simple_isa() {
		Ontology ontology = OntologyFactoryImpl.getInstance().createOntology("");

		Predicate a = predicateFactory.getPredicate("a", 1);
		Predicate b = predicateFactory.getPredicate("b", 1);
		Predicate c = predicateFactory.getPredicate("c", 1);

		OClass ac = descFactory.createClass(a);
		OClass bc = descFactory.createClass(b);
		OClass cc = descFactory.createClass(c);

		ontology.addAssertionWithEntities(OntologyFactoryImpl.getInstance().createSubClassAxiom(bc, ac));
		ontology.addAssertionWithEntities(OntologyFactoryImpl.getInstance().createSubClassAxiom(cc, bc));

		TBoxReasonerImpl reasoner0 = new TBoxReasonerImpl(ontology);
		TBoxReasoner reasoner = TBoxReasonerImpl.getChainReasoner(reasoner0);
		EquivalencesDAG<BasicClassDescription> classes = reasoner.getClasses();
		
		Equivalences<BasicClassDescription> ac0 = classes.getVertex(ac);
		Equivalences<BasicClassDescription> bc0 = classes.getVertex(bc);
		Equivalences<BasicClassDescription> cc0 = classes.getVertex(cc);
		
		assertTrue(classes.getSub(ac0).contains(bc0));
		assertTrue(classes.getSub(ac0).contains(cc0));
		assertEquals(sizeOf(classes.getSub(ac0)), 3); // getDescendants is reflexive

		assertTrue(classes.getSub(bc0).contains(cc0));
		assertEquals(sizeOf(classes.getSub(bc0)), 2);  // getDescendants is reflexive
		assertEquals(sizeOf(classes.getSub(cc0)), 1);  // getDescendants is reflexive
	}

	public void test_exists_simple() {
		Ontology ontology = OntologyFactoryImpl.getInstance().createOntology("");

		Predicate a = predicateFactory.getClassPredicate("a");
		Predicate r = predicateFactory.getObjectPropertyPredicate("r");
		Predicate c = predicateFactory.getClassPredicate("c");
		OClass ac = descFactory.createClass(a);
		PropertySomeRestriction er = descFactory.createPropertySomeRestriction(r, false);
		PropertySomeRestriction ier = descFactory.createPropertySomeRestriction(r, true);
		OClass cc = descFactory.createClass(c);

		ontology.addAssertionWithEntities(OntologyFactoryImpl.getInstance().createSubClassAxiom(er, ac));
		ontology.addAssertionWithEntities(OntologyFactoryImpl.getInstance().createSubClassAxiom(cc, ier));
		
		//generate Graph
		TBoxReasonerImpl res0 = new  TBoxReasonerImpl(ontology);
		//DefaultDirectedGraph<Description,DefaultEdge> res1 = res0.getGraph();

		
		
//		for (Description nodes: res.vertexSet()) {
//			System.out.println("---- " + nodes);
//		}
		
		TestTBoxReasonerImpl_OnGraph reasoner = new TestTBoxReasonerImpl_OnGraph(res0);
		reasoner.convertIntoChainDAG();

		EquivalencesDAG<BasicClassDescription> classes = reasoner.getClasses();

		Equivalences<BasicClassDescription> ac0 = classes.getVertex(ac);
		Equivalences<BasicClassDescription> cc0 = classes.getVertex(cc);
		Equivalences<BasicClassDescription> er0 = classes.getVertex(er);
		Equivalences<BasicClassDescription> ier0 = classes.getVertex(ier);
		
		
		assertTrue(classes.getSub(ac0).contains(er0));
		assertTrue(classes.getSub(ac0).contains(ier0));
		assertTrue(classes.getSub(ac0).contains(cc0));
		assertEquals(sizeOf(classes.getSub(ac0)), 4);  // getDescendants is reflexive

		assertTrue(classes.getSub(er0).contains(cc0));
		assertEquals(sizeOf(classes.getSub(er0)), 2);  // getDescendants is reflexive

		assertTrue(classes.getSub(ier0).contains(cc0));
		assertEquals(sizeOf(classes.getSub(ier0)), 2);  // getDescendants is reflexive
		assertEquals(sizeOf(classes.getSub(cc0)), 1);  // getDescendants is reflexive
	}

	public void test_exists_complex() {

		Ontology ontology = OntologyFactoryImpl.getInstance().createOntology("");

		Predicate a = predicateFactory.getPredicate("a", 1);
		Predicate r = predicateFactory.getPredicate("r", 2);
		Predicate c = predicateFactory.getPredicate("c", 1);
		Predicate b = predicateFactory.getPredicate("b", 1);
		Predicate d = predicateFactory.getPredicate("d", 1);

		OClass ac = descFactory.createClass(a);
		PropertySomeRestriction er = descFactory.createPropertySomeRestriction(r, false);
		PropertySomeRestriction ier = descFactory.createPropertySomeRestriction(r, true);
		OClass cc = descFactory.createClass(c);
		OClass bc = descFactory.createClass(b);
		OClass dc = descFactory.createClass(d);

		ontology.addAssertionWithEntities(OntologyFactoryImpl.getInstance().createSubClassAxiom(er, ac));
		ontology.addAssertionWithEntities(OntologyFactoryImpl.getInstance().createSubClassAxiom(cc, ier));
		ontology.addAssertionWithEntities(OntologyFactoryImpl.getInstance().createSubClassAxiom(bc, er));
		ontology.addAssertionWithEntities(OntologyFactoryImpl.getInstance().createSubClassAxiom(ier, dc));

		//DAGImpl dag221 = DAGBuilder.getDAG(ontology);
		//TBoxReasonerImpl reasoner221 = new TBoxReasonerImpl(dag221);
		//DAGImpl dagChain221 = reasoner221.getChainDAG();
		TBoxReasonerImpl resoner0 = new TBoxReasonerImpl(ontology);
		TBoxReasoner reasoner = TBoxReasonerImpl.getChainReasoner(resoner0);

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
		assertEquals(sizeOf(classes.getSub(ac0)), 5);  // getDescendants is reflexive

		assertTrue(classes.getSub(dc0).contains(er0));
		assertTrue(classes.getSub(dc0).contains(ier0));
		assertTrue(classes.getSub(dc0).contains(cc0));
		assertTrue(classes.getSub(dc0).contains(bc0));
		assertEquals(sizeOf(classes.getSub(dc0)), 5);  // getDescendants is reflexive

		assertTrue(classes.getSub(er0).contains(bc0));
		assertTrue(classes.getSub(er0).contains(cc0));
		assertEquals(sizeOf(classes.getSub(er0)), 3);  // getDescendants is reflexive

		assertTrue(classes.getSub(ier0).contains(bc0));
		assertTrue(classes.getSub(ier0).contains(cc0));
		assertEquals(sizeOf(classes.getSub(ier0)), 3);  // getDescendants is reflexive

		assertEquals(sizeOf(classes.getSub(bc0)), 1);  // getDescendants is reflexive
		assertEquals(sizeOf(classes.getSub(cc0)), 1);  // getDescendants is reflexive
	}
}

