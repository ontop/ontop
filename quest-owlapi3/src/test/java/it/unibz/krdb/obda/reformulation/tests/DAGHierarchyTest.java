package it.unibz.krdb.obda.reformulation.tests;

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



import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.EquivalencesDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.krdb.obda.quest.dag.TestTBoxReasonerImpl_OnNamedDAG;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class DAGHierarchyTest extends TestCase {
	/**
	 * A -> B, B -> {E, F}, {C, D} -> {E, F} with A, B, C, D, E, F are atomic
	 * concepts.
	 */
	private final String inputFile1 = "src/test/resources/test/dag/test-class-hierarchy.owl";

	/**
	 * P -> Q, Q -> {T, U}, {R, S} -> {T, U} with P, Q, R, S, T, U are atomic
	 * roles.
	 */
	private final String inputFile2 = "src/test/resources/test/dag/test-role-hierarchy.owl";

	private static Ontology loadOntology(String filename) throws Exception  {
		OWLAPI3Translator t = new OWLAPI3Translator();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owlonto = man.loadOntologyFromOntologyDocument(new File(filename));
		Ontology onto = t.translate(owlonto);
		return onto;
	}
	
	private static <T> int sizeOf(Set<Equivalences<T>> set) {
		int size = 0;
		for (Equivalences<T> d : set)
			size += d.size();
		return size;
	}
	
	/**
	 * List all the descendants of a class in the TBox with having equivalent
	 * classes into account.
	 */
	public void testDescendantClasses() throws Exception {
		final String ontoURI = "http://obda.inf.unibz.it/ontologies/test-class-hierarchy.owl#";

		Ontology onto = loadOntology(inputFile1);

		// generate DAG
		TBoxReasoner dag = new TBoxReasonerImpl(onto);
		// generate named DAG
		TestTBoxReasonerImpl_OnNamedDAG namedReasoner = new TestTBoxReasonerImpl_OnNamedDAG(dag);

		EquivalencesDAG<BasicClassDescription> classes = namedReasoner.getClasses();
		
		OntologyFactory ofac = OntologyFactoryImpl.getInstance();

		BasicClassDescription A = ofac.createClass(ontoURI + "A");
		BasicClassDescription B = ofac.createClass(ontoURI + "B");
		BasicClassDescription C = ofac.createClass(ontoURI + "C");
		BasicClassDescription D = ofac.createClass(ontoURI + "D");
		BasicClassDescription E = ofac.createClass(ontoURI + "E");
		BasicClassDescription F = ofac.createClass(ontoURI + "F");
		
		/**
		 * The initial node is Node A.
		 */
		Equivalences<BasicClassDescription> initialNode = classes.getVertex(A);
		Set<Equivalences<BasicClassDescription>> descendants = classes.getSub(initialNode);

		assertEquals(descendants.size(), 1);  // getDescendants is reflexive

		/**
		 * The initial node is Node B.
		 */
		initialNode = classes.getVertex(B);
		descendants = classes.getSub(initialNode);

		assertEquals(descendants.size(), 2);  // getDescendants is reflexive

		assertTrue(descendants.contains(classes.getVertex(A)));

		/**
		 * The initial node is Node C.
		 */
		// There is no test for this node because the API will always suggest
		// Node C is not
		// exist and it has been replaced by Node D (i.e., Class C is equivalent
		// with Class D)

		/**
		 * The initial node is Node D.
		 */
		initialNode = classes.getVertex(D);
		descendants = classes.getSub(initialNode);

		assertEquals(descendants.size(), 1);

		Set<BasicClassDescription> equivalents = new HashSet<BasicClassDescription>();
		equivalents.add(C);
		equivalents.add(D); // getDescendants is reflexive
		assertTrue(descendants.contains(new Equivalences<BasicClassDescription>(equivalents)));

		/**
		 * The initial node is Node E.
		 */
		// There is no test for this node because the API will always suggest
		// Node E is not
		// exist and it has been replaced by Node F (i.e., Class E is equivalent
		// with Class F)

		/**
		 * The initial node is Node F.
		 */
		initialNode = classes.getVertex(F);
		descendants = classes.getSub(initialNode);
		assertEquals(sizeOf(descendants), 6); // getDescendants is reflexive

		assertTrue(descendants.contains(classes.getVertex(A)));
		assertTrue(descendants.contains(classes.getVertex(B)));
		assertTrue(descendants.contains(classes.getVertex(C)));
		assertTrue(descendants.contains(classes.getVertex(D)));
		
		equivalents = new HashSet<BasicClassDescription>();
		equivalents.add(E);
		equivalents.add(F); // getDescendants is reflexive
		assertTrue(descendants.contains(new Equivalences<BasicClassDescription>(equivalents)));
	}

	/**
	 * List all the ancestors of a class in the TBox with having equivalent
	 * classes into account.
	 */
	public void testAncestorClasses() throws Exception {
		final String ontoURI = "http://obda.inf.unibz.it/ontologies/test-class-hierarchy.owl#";

		Ontology onto = loadOntology(inputFile1);

		// generate DAG
		TBoxReasoner dag = new TBoxReasonerImpl(onto);
		// generate named DAG
		TestTBoxReasonerImpl_OnNamedDAG namedReasoner = new TestTBoxReasonerImpl_OnNamedDAG(dag);

		EquivalencesDAG<BasicClassDescription> classes = namedReasoner.getClasses();
		
		OntologyFactory ofac = OntologyFactoryImpl.getInstance();

		BasicClassDescription A = ofac.createClass(ontoURI + "A");
		BasicClassDescription B = ofac.createClass(ontoURI + "B");
		BasicClassDescription C = ofac.createClass(ontoURI + "C");
		BasicClassDescription D = ofac.createClass(ontoURI + "D");
		BasicClassDescription E = ofac.createClass(ontoURI + "E");
		BasicClassDescription F = ofac.createClass(ontoURI + "F");
	
		/**
		 * The initial node is Node A.
		 */

		Equivalences<BasicClassDescription> initialNode = classes.getVertex(A);
		Set<Equivalences<BasicClassDescription>> ancestors = classes.getSuper(initialNode);
		assertEquals(sizeOf(ancestors), 4);   // ancestors is now reflexive

		assertTrue(ancestors.contains(classes.getVertex(B)));
															// class
		assertTrue(ancestors.contains(classes.getVertex(E)));
		assertTrue(ancestors.contains(classes.getVertex(F)));

		/**
		 * The initial node is Node B.
		 */
		initialNode = classes.getVertex(B);
		ancestors = classes.getSuper(initialNode);
		assertEquals(sizeOf(ancestors), 3); // ancestors is now refelxive

		assertTrue(ancestors.contains(classes.getVertex(F)));
		assertTrue(ancestors.contains(classes.getVertex(E)));

		/**
		 * The initial node is Node C.
		 */
		// There is no test for this node because the API will always suggest
		// Node C is not
		// exist and it has been replaced by Node D (i.e., Class C is equivalent
		// with Class D)

		/**
		 * The initial node is Node D.
		 */
		initialNode = classes.getVertex(D);
		ancestors = classes.getSuper(initialNode);
		assertEquals(sizeOf(ancestors), 4); // ancestors is now refelxive

		Set<BasicClassDescription> equivalents = new HashSet<BasicClassDescription>();
		equivalents.add(C);
		equivalents.add(D);  // ancestor is reflexive now
		assertTrue(ancestors.contains(new Equivalences<BasicClassDescription>(equivalents)));
		assertTrue(ancestors.contains(classes.getVertex(E)));
		assertTrue(ancestors.contains(classes.getVertex(F)));

		/**
		 * The initial node is Node E.
		 */
		// There is no test for this node because the API will always suggest
		// Node E is not
		// exist and it has been replaced by Node F (i.e., Class E is equivalent
		// with Class F)

		/**
		 * The initial node is Node F.
		 */
		initialNode = classes.getVertex(F);
		ancestors = classes.getSuper(initialNode);

		assertEquals(ancestors.size(), 1);

		equivalents = new HashSet<BasicClassDescription>();
		equivalents.add(E);
		equivalents.add(F);  // ancestor is reflexive now
		assertTrue(ancestors.contains(new Equivalences<BasicClassDescription>(equivalents)));
	}

	/**
	 * List all the descendants of a role in the TBox with having equivalent
	 * roles into account.
	 */
	public void testDescendantRoles() throws Exception {
		final String ontoURI = "http://obda.inf.unibz.it/ontologies/test-role-hierarchy.owl#";

		Ontology onto = loadOntology(inputFile2);
		
		// generate DAG
		TBoxReasoner dag = new TBoxReasonerImpl(onto);
		// generate named DAG
		TestTBoxReasonerImpl_OnNamedDAG namedReasoner = new TestTBoxReasonerImpl_OnNamedDAG(dag);

		EquivalencesDAG<Property> properties = namedReasoner.getProperties();
		
		OntologyFactory ofac = OntologyFactoryImpl.getInstance();

		Property P = ofac.createObjectProperty(ontoURI + "P");
		Property S = ofac.createObjectProperty(ontoURI + "S");
		Property R = ofac.createObjectProperty(ontoURI + "R");
		Property Q = ofac.createObjectProperty(ontoURI + "Q");
		Property T = ofac.createObjectProperty(ontoURI + "T");
		Property U = ofac.createObjectProperty(ontoURI + "U");
		
		
		/**
		 * The initial node is Node P.
		 */
		Equivalences<Property> initialNode = properties.getVertex(P);
		Set<Equivalences<Property>> descendants = properties.getSub(initialNode);
		assertEquals(descendants.size(), 1);  // getDescendants is reflexive

		/**
		 * The initial node is Node Q.
		 */
		initialNode = properties.getVertex(Q);
		descendants = properties.getSub(initialNode);
		
		assertEquals(descendants.size(), 2);  // getDescendants is reflexive
		assertTrue(descendants.contains(properties.getVertex(P)));

		/**
		 * The initial node is Node R.
		 */
		// There is no test for this node because the API will always suggest
		// Node R is not
		// exist and it has been replaced by Node S (i.e., Role R is equivalent
		// with Role S)

		/**
		 * The initial node is Node S.
		 */
		initialNode = properties.getVertex(S);
		descendants = properties.getSub(initialNode);

		assertEquals(descendants.size(), 1);

		Set<Property> equivalents = new HashSet<Property>();
		equivalents.add(R);
		equivalents.add(S); // getDescendants is reflexive
		assertTrue(descendants.contains(new Equivalences<Property>(equivalents)));

		/**
		 * The initial node is Node T.
		 */
		// There is no test for this node because the API will always suggest
		// Node T is not
		// exist and it has been replaced by Node U (i.e., Role T is equivalent
		// with Role U)

		/**
		 * The initial node is Node U.
		 */
		initialNode = properties.getVertex(U);
		descendants = properties.getSub(initialNode);
		assertEquals(sizeOf(descendants), 6);  // getDescendants is reflexive
		
		assertTrue(descendants.contains(properties.getVertex(P)));
		assertTrue(descendants.contains(properties.getVertex(Q)));
		assertTrue(descendants.contains(properties.getVertex(R)));
		assertTrue(descendants.contains(properties.getVertex(S)));
		equivalents = new HashSet<Property>();
		equivalents.add(T);													// role
		equivalents.add(U); // getDescendants is reflexive
		assertTrue(descendants.contains(new Equivalences<Property>(equivalents)));
	}

	/**
	 * List all the ancestors of a role in the TBox with having equivalent roles
	 * into account.
	 */
	public void testAncestorRoles() throws Exception {
		final String ontoURI = "http://obda.inf.unibz.it/ontologies/test-role-hierarchy.owl#";

		Ontology onto = loadOntology(inputFile2);

		// generate DAG
		TBoxReasoner dag = new TBoxReasonerImpl(onto);
		// generate named DAG
		TestTBoxReasonerImpl_OnNamedDAG namedReasoner = new TestTBoxReasonerImpl_OnNamedDAG(dag);
		
		EquivalencesDAG<Property> properties = namedReasoner.getProperties();
		
		OntologyFactory ofac = OntologyFactoryImpl.getInstance();

		Property P = ofac.createObjectProperty(ontoURI + "P");
		Property S = ofac.createObjectProperty(ontoURI + "S");
		Property R = ofac.createObjectProperty(ontoURI + "R");
		Property Q = ofac.createObjectProperty(ontoURI + "Q");
		Property T = ofac.createObjectProperty(ontoURI + "T");
		Property U = ofac.createObjectProperty(ontoURI + "U");
	
		/**
		 * The initial node is Node P.
		 */
		Equivalences<Property> initialNode = properties.getVertex(P);
		Set<Equivalences<Property>> ancestors = properties.getSuper(initialNode);
		assertEquals(sizeOf(ancestors), 4); // ancestor is reflexive now

		assertTrue(ancestors.contains(properties.getVertex(Q)));
		assertTrue(ancestors.contains(properties.getVertex(T)));
		assertTrue(ancestors.contains(properties.getVertex(U)));

		/**
		 * The initial node is Node Q.
		 */
		initialNode = properties.getVertex(Q);
		ancestors = properties.getSuper(initialNode);
		assertEquals(sizeOf(ancestors), 3); // ancestor is reflexive now

		assertTrue(ancestors.contains(properties.getVertex(T)));
		assertTrue(ancestors.contains(properties.getVertex(U)));

		/**
		 * The initial node is Node R.
		 */
		// There is no test for this node because the API will always suggest
		// Node R is not
		// exist and it has been replaced by Node S (i.e., Role R is equivalent
		// with Role S)

		/**
		 * The initial node is Node S.
		 */
		initialNode = properties.getVertex(S);
		ancestors = properties.getSuper(initialNode);
		assertEquals(sizeOf(ancestors),4); // ancestor is reflexive now

		Set<Property> equivalents = new HashSet<Property>();
		equivalents.add(R);
		equivalents.add(S); // ancestor is reflexive now
		assertTrue(ancestors.contains(new Equivalences<Property>(equivalents)));
		
		assertTrue(ancestors.contains(properties.getVertex(T)));
		assertTrue(ancestors.contains(properties.getVertex(U)));

		/**
		 * The initial node is Node T.
		 */
		// There is no test for this node because the API will always suggest
		// Node T is not
		// exist and it has been replaced by Node U (i.e., Role T is equivalent
		// with Role U)

		/**
		 * The initial node is Node U.
		 */
		initialNode = properties.getVertex(U);
		ancestors = properties.getSuper(initialNode);
		assertEquals(ancestors.size(), 1);

		equivalents = new HashSet<Property>();
		equivalents.add(T); 
		equivalents.add(U); // ancestor is reflexive now
		assertTrue(ancestors.contains(new Equivalences<Property>(equivalents)));		
	}
}
