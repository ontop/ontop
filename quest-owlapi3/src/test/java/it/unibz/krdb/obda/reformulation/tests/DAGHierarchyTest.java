/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAGBuilder;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAGImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.EquivalenceClass;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.NamedDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TestTBoxReasonerImplOnNamedDAG;

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

	/**
	 * List all the descendants of a class in the TBox with having equivalent
	 * classes into account.
	 */
	public void testDescendantClasses() throws Exception {
		final String ontoURI = "http://obda.inf.unibz.it/ontologies/test-class-hierarchy.owl#";
		OWLAPI3Translator t = new OWLAPI3Translator();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owlonto = man.loadOntologyFromOntologyDocument(new File(
				inputFile1));
		Ontology onto = t.translate(owlonto);

		// generate DAG
		TBoxReasonerImpl dag = new TBoxReasonerImpl(onto);
		// generate named DAG
		NamedDAG pureIsa = NamedDAG.getNamedDAG(dag);

		TBoxReasoner namedReasoner = new TestTBoxReasonerImplOnNamedDAG(pureIsa);

		final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

		/**
		 * The initial node is Node A.
		 */
		Description initialNode = ofac.createClass(ontoURI + "A");
		Set<EquivalenceClass<Description>> descendants = namedReasoner.getDescendants(initialNode);

		assertEquals(descendants.size(), 0);

		/**
		 * The initial node is Node B.
		 */
		initialNode = ofac.createClass(ontoURI + "B");
		descendants = namedReasoner.getDescendants(initialNode);

		assertEquals(descendants.size(), 1);

		Description A = ofac.createClass(ontoURI + "A");
		assertTrue(descendants.contains(namedReasoner.getEquivalences(A)));

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
		initialNode = ofac.createClass(ontoURI + "D");
		descendants = namedReasoner.getDescendants(initialNode);

		assertEquals(descendants.size(), 1);

		Description C = ofac.createClass(ontoURI + "C");
		Set<Description> equivalents = new HashSet<Description>();
		equivalents.add(C);
		assertTrue(descendants.contains(new EquivalenceClass<Description>(equivalents)));

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
		initialNode = ofac.createClass(ontoURI + "F");
		descendants = namedReasoner.getDescendants(initialNode);
		int size = 0;
		for (EquivalenceClass<Description> d : descendants)

			size += d.size();

		assertEquals(size, 5);

		A = ofac.createClass(ontoURI + "A");
		assertTrue(descendants.contains(namedReasoner.getEquivalences(A)));
		Description B = ofac.createClass(ontoURI + "B");
		assertTrue(descendants.contains(namedReasoner.getEquivalences(B)));
		C = ofac.createClass(ontoURI + "C"); // equivalent class
		assertTrue(descendants.contains(namedReasoner.getEquivalences(C)));
		Description D = ofac.createClass(ontoURI + "D");
		assertTrue(descendants.contains(namedReasoner.getEquivalences(D)));
		Description E = ofac.createClass(ontoURI + "E"); // equivalent class
		equivalents = new HashSet<Description>();
		equivalents.add(E);
		assertTrue(descendants.contains(new EquivalenceClass<Description>(equivalents)));
	}

	/**
	 * List all the ancestors of a class in the TBox with having equivalent
	 * classes into account.
	 */
	public void testAncestorClasses() throws Exception {
		final String ontoURI = "http://obda.inf.unibz.it/ontologies/test-class-hierarchy.owl#";
		OWLAPI3Translator t = new OWLAPI3Translator();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owlonto = man.loadOntologyFromOntologyDocument(new File(
				inputFile1));
		Ontology onto = t.translate(owlonto);

		// generate DAG
		TBoxReasonerImpl dag = new TBoxReasonerImpl(onto);
		// generate named DAG
		NamedDAG pureIsa = NamedDAG.getNamedDAG(dag);

		TBoxReasoner namedReasoner = new TestTBoxReasonerImplOnNamedDAG(pureIsa);

		final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

		/**
		 * The initial node is Node A.
		 */

		Description initialNode = ofac.createClass(ontoURI + "A");
		Set<EquivalenceClass<Description>> ancestors = namedReasoner.getAncestors(initialNode);

		int size = 0;
		for (EquivalenceClass<Description> a : ancestors)

			size += a.size();

		assertEquals(size, 3);

		Description B = ofac.createClass(ontoURI + "B");
		assertTrue(ancestors.contains(namedReasoner.getEquivalences(B)));
		Description E = ofac.createClass(ontoURI + "E"); // equivalent
															// class
		assertTrue(ancestors.contains(namedReasoner.getEquivalences(E)));
		Description F = ofac.createClass(ontoURI + "F");
		assertTrue(ancestors.contains(namedReasoner.getEquivalences(F)));

		/**
		 * The initial node is Node B.
		 */
		initialNode = ofac.createClass(ontoURI + "B");
		ancestors = namedReasoner.getAncestors(initialNode);

		size = 0;
		for (EquivalenceClass<Description> a : ancestors)

			size += a.size();

		assertEquals(size, 2);

		F = ofac.createClass(ontoURI + "F");
		assertTrue(ancestors.contains(namedReasoner.getEquivalences(F)));
		E = ofac.createClass(ontoURI + "E"); // equivalent class
		assertTrue(ancestors.contains(namedReasoner.getEquivalences(E)));

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
		initialNode = ofac.createClass(ontoURI + "D");
		ancestors = namedReasoner.getAncestors(initialNode);

		size = 0;
		for (EquivalenceClass<Description> a : ancestors)

			size += a.size();

		assertEquals(size, 3);

		Description C = ofac.createClass(ontoURI + "C"); // equivalent
															// class
		Set<Description> equivalents = new HashSet<Description>();
		equivalents.add(C);
		assertTrue(ancestors.contains(new EquivalenceClass<Description>(equivalents)));
		E = ofac.createClass(ontoURI + "E"); // equivalent class
		assertTrue(ancestors.contains(namedReasoner.getEquivalences(E)));
		F = ofac.createClass(ontoURI + "F");
		assertTrue(ancestors.contains(namedReasoner.getEquivalences(F)));

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
		initialNode = ofac.createClass(ontoURI + "F");
		ancestors = namedReasoner.getAncestors(initialNode);

		assertEquals(ancestors.size(), 1);

		E = ofac.createClass(ontoURI + "E"); // equivalent class
		equivalents = new HashSet<Description>();
		equivalents.add(E);
		assertTrue(ancestors.contains(new EquivalenceClass<Description>(equivalents)));
	}

	/**
	 * List all the descendants of a role in the TBox with having equivalent
	 * roles into account.
	 */
	public void testDescendantRoles() throws Exception {
		final String ontoURI = "http://obda.inf.unibz.it/ontologies/test-role-hierarchy.owl#";
		OWLAPI3Translator t = new OWLAPI3Translator();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owlonto = man.loadOntologyFromOntologyDocument(new File(
				inputFile2));
		Ontology onto = t.translate(owlonto);
		
		// generate DAG
		TBoxReasonerImpl dag = new TBoxReasonerImpl(onto);
		// generate named DAG
		NamedDAG pureIsa = NamedDAG.getNamedDAG(dag);

		TBoxReasoner namedReasoner = new TestTBoxReasonerImplOnNamedDAG(pureIsa);

		final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

		/**
		 * The initial node is Node P.
		 */
		Description initialNode = ofac.createObjectProperty(ontoURI + "P");
		Set<EquivalenceClass<Description>> descendants = namedReasoner.getDescendants(initialNode);

		assertEquals(descendants.size(), 0);

		/**
		 * The initial node is Node Q.
		 */
		initialNode = ofac.createObjectProperty(ontoURI + "Q");
		descendants = namedReasoner.getDescendants(initialNode);
		
		assertEquals(descendants.size(), 1);

		Description P = ofac.createObjectProperty(ontoURI + "P");
		assertTrue(descendants.contains(namedReasoner.getEquivalences(P)));

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
		initialNode = ofac.createObjectProperty(ontoURI	+ "S");
		descendants = namedReasoner.getDescendants(initialNode);

		assertEquals(descendants.size(), 1);

		Description R = ofac.createObjectProperty(ontoURI + "R");
		Set<Description> equivalents = new HashSet<Description>();
		equivalents.add(R);
		assertTrue(descendants.contains(new EquivalenceClass<Description>(equivalents)));

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
		initialNode = ofac.createObjectProperty(ontoURI+ "U");
		descendants = namedReasoner.getDescendants(initialNode);

		int size = 0;
		for (EquivalenceClass<Description> d : descendants)

			size += d.size();

		assertEquals(size, 5);
		
		P = ofac.createObjectProperty(ontoURI + "P");
		assertTrue(descendants.contains(namedReasoner.getEquivalences(P)));
		Description Q = ofac.createObjectProperty(ontoURI + "Q");
		assertTrue(descendants.contains(namedReasoner.getEquivalences(Q)));
		R = ofac.createObjectProperty(ontoURI + "R"); // equivalent
																	// role
		assertTrue(descendants.contains(namedReasoner.getEquivalences(R)));
		Description S = ofac.createObjectProperty(ontoURI + "S");
		assertTrue(descendants.contains(namedReasoner.getEquivalences(S)));
		Description T = ofac.createObjectProperty(ontoURI + "T"); // equivalent
		 equivalents = new HashSet<Description>();
		equivalents.add(T);													// role
		assertTrue(descendants.contains(new EquivalenceClass<Description>(equivalents)));
	}

	/**
	 * List all the ancestors of a role in the TBox with having equivalent roles
	 * into account.
	 */
	public void testAncestorRoles() throws Exception {
		final String ontoURI = "http://obda.inf.unibz.it/ontologies/test-role-hierarchy.owl#";
		OWLAPI3Translator t = new OWLAPI3Translator();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owlonto = man.loadOntologyFromOntologyDocument(new File(
				inputFile2));
		Ontology onto = t.translate(owlonto);

		// generate DAG
		TBoxReasonerImpl dag = new TBoxReasonerImpl(onto);
		// generate named DAG
		NamedDAG pureIsa = NamedDAG.getNamedDAG(dag);

		TBoxReasoner namedReasoner = new TestTBoxReasonerImplOnNamedDAG(pureIsa);
		
		final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

		/**
		 * The initial node is Node P.
		 */
		Description initialNode = ofac
				.createObjectProperty(ontoURI + "P");
		Set<EquivalenceClass<Description>> ancestors = namedReasoner.getAncestors(initialNode);

		int size = 0;
		for (EquivalenceClass<Description> a : ancestors)

			size += a.size();

		assertEquals(size, 3);

		Description Q = ofac.createObjectProperty(ontoURI + "Q");
		assertTrue(ancestors.contains(namedReasoner.getEquivalences(Q)));
		Description T = ofac.createObjectProperty(ontoURI + "T"); // equivalent
																			// role
		assertTrue(ancestors.contains(namedReasoner.getEquivalences(T)));
		Description U = ofac.createObjectProperty(ontoURI + "U");
		assertTrue(ancestors.contains(namedReasoner.getEquivalences(U)));

		/**
		 * The initial node is Node Q.
		 */
		initialNode = ofac.createObjectProperty(ontoURI+ "Q");
		ancestors = namedReasoner.getAncestors(initialNode);

		size = 0;
		for (EquivalenceClass<Description> a : ancestors)

			size += a.size();

		assertEquals(size, 2);

		T = ofac.createObjectProperty(ontoURI + "T"); // equivalent
																	// role
		
		assertTrue(ancestors.contains(namedReasoner.getEquivalences(T)));
		U = ofac.createObjectProperty(ontoURI + "U");
		assertTrue(ancestors.contains(namedReasoner.getEquivalences(U)));

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
		initialNode = ofac.createObjectProperty(ontoURI+ "S");
		ancestors = namedReasoner.getAncestors(initialNode);

		size = 0;
		for (EquivalenceClass<Description> a : ancestors)

			size += a.size();

		assertEquals(size,3);

		Description R = ofac.createObjectProperty(ontoURI + "R"); // equivalent
																			// role
		Set<Description> equivalents = new HashSet<Description>();
		equivalents.add(R);
		assertTrue(ancestors.contains(new EquivalenceClass<Description>(equivalents)));
		T = ofac.createObjectProperty(ontoURI + "T"); // equivalent
																	// role
		
		assertTrue(ancestors.contains(namedReasoner.getEquivalences(T)));
		U =ofac.createObjectProperty(ontoURI + "U");
		assertTrue(ancestors.contains(namedReasoner.getEquivalences(U)));

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
		initialNode = ofac.createObjectProperty(ontoURI
				+ "U");
		ancestors = namedReasoner.getAncestors(initialNode);

		assertEquals(ancestors.size(), 1);

		T = ofac.createObjectProperty(ontoURI + "T"); // equivalent
																	// role
		equivalents = new HashSet<Description>();
		equivalents.add(T);
		assertTrue(ancestors.contains(new EquivalenceClass<Description>(equivalents)));
		
	}
}
