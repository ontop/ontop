
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

import org.junit.Test;

import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.ObjectSomeValuesFrom;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyVocabulary;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.EquivalencesDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.krdb.obda.quest.dag.TestTBoxReasonerImpl_OnGraph;
import junit.framework.TestCase;


public class DAGChainTest extends TestCase {

	private static <T> int sizeOf(Set<Equivalences<T>> set) {
		int size = 0;
		for(Equivalences<T> e: set){
			size += e.size();
		}
		return size;
	}

	@Test
	public void test_simple_isa() {        
		OntologyVocabulary vb = OntologyFactoryImpl.getInstance().createVocabulary();
		OClass ac = vb.createClass("a");
		OClass bc = vb.createClass("b");
		OClass cc = vb.createClass("c");

		Ontology ontology = OntologyFactoryImpl.getInstance().createOntology(vb);

		ontology.addSubClassOfAxiom(bc, ac);
		ontology.addSubClassOfAxiom(cc, bc);

		TBoxReasonerImpl reasoner0 = new TBoxReasonerImpl(ontology);
		TBoxReasoner reasoner = TBoxReasonerImpl.getChainReasoner(reasoner0);
		EquivalencesDAG<ClassExpression> classes = reasoner.getClassDAG();
		
		Equivalences<ClassExpression> ac0 = classes.getVertex(ac);
		Equivalences<ClassExpression> bc0 = classes.getVertex(bc);
		Equivalences<ClassExpression> cc0 = classes.getVertex(cc);
		
		assertTrue(classes.getSub(ac0).contains(bc0));
		assertTrue(classes.getSub(ac0).contains(cc0));
		assertEquals(sizeOf(classes.getSub(ac0)), 3); // getDescendants is reflexive

		assertTrue(classes.getSub(bc0).contains(cc0));
		assertEquals(sizeOf(classes.getSub(bc0)), 2);  // getDescendants is reflexive
		assertEquals(sizeOf(classes.getSub(cc0)), 1);  // getDescendants is reflexive
	}

	public void test_exists_simple() {
		
        OntologyVocabulary vb = OntologyFactoryImpl.getInstance().createVocabulary();
		ObjectPropertyExpression rprop = vb.createObjectProperty("r");
		OClass ac = vb.createClass("a");
		OClass cc = vb.createClass("c");
		
		Ontology ontology = OntologyFactoryImpl.getInstance().createOntology(vb);

		ObjectPropertyExpression riprop = rprop.getInverse();
		ObjectSomeValuesFrom er = rprop.getDomain();
		ObjectSomeValuesFrom ier = riprop.getDomain();

		ontology.addSubClassOfAxiom(er, ac);
		ontology.addSubClassOfAxiom(cc, ier);
		
		//generate Graph
		TBoxReasonerImpl res0 = new  TBoxReasonerImpl(ontology);
		//DefaultDirectedGraph<Description,DefaultEdge> res1 = res0.getGraph();

		
		
//		for (Description nodes: res.vertexSet()) {
//			System.out.println("---- " + nodes);
//		}
		
		TestTBoxReasonerImpl_OnGraph reasoner = new TestTBoxReasonerImpl_OnGraph(res0);
		reasoner.convertIntoChainDAG();

		EquivalencesDAG<ClassExpression> classes = reasoner.getClassDAG();

		Equivalences<ClassExpression> ac0 = classes.getVertex(ac);
		Equivalences<ClassExpression> cc0 = classes.getVertex(cc);
		Equivalences<ClassExpression> er0 = classes.getVertex(er);
		Equivalences<ClassExpression> ier0 = classes.getVertex(ier);
		
		
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

        OntologyVocabulary vb = OntologyFactoryImpl.getInstance().createVocabulary();
		OClass ac = vb.createClass("a");
		OClass cc = vb.createClass("c");
		OClass bc = vb.createClass("b");
		OClass dc = vb.createClass("d");
		ObjectPropertyExpression rprop = vb.createObjectProperty("r");
        
		Ontology ontology = OntologyFactoryImpl.getInstance().createOntology(vb);

		ObjectPropertyExpression riprop = rprop.getInverse();
		ObjectSomeValuesFrom er = rprop.getDomain();
		ObjectSomeValuesFrom ier = riprop.getDomain();

		ontology.addSubClassOfAxiom(er, ac);
		ontology.addSubClassOfAxiom(cc, ier);
		ontology.addSubClassOfAxiom(bc, er);
		ontology.addSubClassOfAxiom(ier, dc);

		//DAGImpl dag221 = DAGBuilder.getDAG(ontology);
		//TBoxReasonerImpl reasoner221 = new TBoxReasonerImpl(dag221);
		//DAGImpl dagChain221 = reasoner221.getChainDAG();
		TBoxReasonerImpl reasoner0 = new TBoxReasonerImpl(ontology);
		TBoxReasoner reasoner = TBoxReasonerImpl.getChainReasoner(reasoner0);

		EquivalencesDAG<ClassExpression> classes = reasoner.getClassDAG();
		
		Equivalences<ClassExpression> ac0 = classes.getVertex(ac);
		Equivalences<ClassExpression> bc0 = classes.getVertex(bc);
		Equivalences<ClassExpression> cc0 = classes.getVertex(cc);
		Equivalences<ClassExpression> dc0 = classes.getVertex(dc);
		Equivalences<ClassExpression> er0 = classes.getVertex(er);
		Equivalences<ClassExpression> ier0 = classes.getVertex(ier);
		
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

