/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.NamedDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Test_TBoxReasonerImplOnNamedDAG;

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
		Test_TBoxReasonerImplOnNamedDAG namedReasoner = new Test_TBoxReasonerImplOnNamedDAG(dag);

		final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

		/**
		 * The initial node is Node A.
		 */
		BasicClassDescription initialNode = ofac.createClass(ontoURI + "A");
		Set<Equivalences<BasicClassDescription>> descendants = namedReasoner.getSubClasses(initialNode);

		assertEquals(descendants.size(), 1);  // getDescendants is reflexive

		/**
		 * The initial node is Node B.
		 */
		initialNode = ofac.createClass(ontoURI + "B");
		descendants = namedReasoner.getSubClasses(initialNode);

		assertEquals(descendants.size(), 2);  // getDescendants is reflexive

		BasicClassDescription A = ofac.createClass(ontoURI + "A");
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
		descendants = namedReasoner.getSubClasses(initialNode);

		assertEquals(descendants.size(), 1);

		BasicClassDescription C = ofac.createClass(ontoURI + "C");
		Set<BasicClassDescription> equivalents = new HashSet<BasicClassDescription>();
		equivalents.add(C);
		equivalents.add(initialNode); // getDescendants is reflexive
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
		initialNode = ofac.createClass(ontoURI + "F");
		descendants = namedReasoner.getSubClasses(initialNode);
		int size = 0;
		for (Equivalences<BasicClassDescription> d : descendants)
			size += d.size();

		assertEquals(size, 6); // getDescendants is reflexive


		A = ofac.createClass(ontoURI + "A");
		assertTrue(descendants.contains(namedReasoner.getEquivalences(A)));
		BasicClassDescription B = ofac.createClass(ontoURI + "B");
		assertTrue(descendants.contains(namedReasoner.getEquivalences(B)));
		C = ofac.createClass(ontoURI + "C"); // equivalent class
		assertTrue(descendants.contains(namedReasoner.getEquivalences(C)));
		BasicClassDescription D = ofac.createClass(ontoURI + "D");
		assertTrue(descendants.contains(namedReasoner.getEquivalences(D)));
		BasicClassDescription E = ofac.createClass(ontoURI + "E"); // equivalent class
		equivalents = new HashSet<BasicClassDescription>();
		equivalents.add(E);
		equivalents.add(initialNode); // getDescendants is reflexive
		assertTrue(descendants.contains(new Equivalences<BasicClassDescription>(equivalents)));
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
		Test_TBoxReasonerImplOnNamedDAG namedReasoner = new Test_TBoxReasonerImplOnNamedDAG(dag);

		final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

		/**
		 * The initial node is Node A.
		 */

		BasicClassDescription initialNode = ofac.createClass(ontoURI + "A");
		Set<Equivalences<BasicClassDescription>> ancestors = namedReasoner.getSuperClasses(initialNode);

		int size = 0;
		for (Equivalences<BasicClassDescription> a : ancestors)
			size += a.size();

		assertEquals(size, 4);   // ancestors is now reflexive

		BasicClassDescription B = ofac.createClass(ontoURI + "B");
		assertTrue(ancestors.contains(namedReasoner.getEquivalences(B)));
		BasicClassDescription E = ofac.createClass(ontoURI + "E"); // equivalent
															// class
		assertTrue(ancestors.contains(namedReasoner.getEquivalences(E)));
		BasicClassDescription F = ofac.createClass(ontoURI + "F");
		assertTrue(ancestors.contains(namedReasoner.getEquivalences(F)));

		/**
		 * The initial node is Node B.
		 */
		initialNode = ofac.createClass(ontoURI + "B");
		ancestors = namedReasoner.getSuperClasses(initialNode);

		size = 0;
		for (Equivalences<BasicClassDescription> a : ancestors)
			size += a.size();

		assertEquals(size, 3); // ancestors is now refelxive

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
		ancestors = namedReasoner.getSuperClasses(initialNode);

		size = 0;
		for (Equivalences<BasicClassDescription> a : ancestors)
			size += a.size();

		assertEquals(size, 4); // ancestors is now refelxive

		BasicClassDescription C = ofac.createClass(ontoURI + "C"); // equivalent
															// class
		Set<BasicClassDescription> equivalents = new HashSet<BasicClassDescription>();
		equivalents.add(C);
		equivalents.add(initialNode);  // ancestor is reflexive now
		assertTrue(ancestors.contains(new Equivalences<BasicClassDescription>(equivalents)));
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
		ancestors = namedReasoner.getSuperClasses(initialNode);

		assertEquals(ancestors.size(), 1);

		E = ofac.createClass(ontoURI + "E"); // equivalent class
		equivalents = new HashSet<BasicClassDescription>();
		equivalents.add(E);
		equivalents.add(initialNode);  // ancestor is reflexive now
		assertTrue(ancestors.contains(new Equivalences<BasicClassDescription>(equivalents)));
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
		Test_TBoxReasonerImplOnNamedDAG namedReasoner = new Test_TBoxReasonerImplOnNamedDAG(dag);

		final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

		/**
		 * The initial node is Node P.
		 */
		Property initialNode = ofac.createObjectProperty(ontoURI + "P");
		Set<Equivalences<Property>> descendants = namedReasoner.getSubProperties(initialNode);

		assertEquals(descendants.size(), 1);  // getDescendants is reflexive

		/**
		 * The initial node is Node Q.
		 */
		initialNode = ofac.createObjectProperty(ontoURI + "Q");
		descendants = namedReasoner.getSubProperties(initialNode);
		
		assertEquals(descendants.size(), 2);  // getDescendants is reflexive

		Property P = ofac.createObjectProperty(ontoURI + "P");
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
		descendants = namedReasoner.getSubProperties(initialNode);

		assertEquals(descendants.size(), 1);

		Property R = ofac.createObjectProperty(ontoURI + "R");
		Set<Property> equivalents = new HashSet<Property>();
		equivalents.add(R);
		equivalents.add(initialNode); // getDescendants is reflexive
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
		initialNode = ofac.createObjectProperty(ontoURI+ "U");
		descendants = namedReasoner.getSubProperties(initialNode);

		int size = 0;
		for (Equivalences<Property> d : descendants)
			size += d.size();

		assertEquals(size, 6);  // getDescendants is reflexive
		
		P = ofac.createObjectProperty(ontoURI + "P");
		assertTrue(descendants.contains(namedReasoner.getEquivalences(P)));
		Property Q = ofac.createObjectProperty(ontoURI + "Q");
		assertTrue(descendants.contains(namedReasoner.getEquivalences(Q)));
		R = ofac.createObjectProperty(ontoURI + "R"); // equivalent
																	// role
		assertTrue(descendants.contains(namedReasoner.getEquivalences(R)));
		Property S = ofac.createObjectProperty(ontoURI + "S");
		assertTrue(descendants.contains(namedReasoner.getEquivalences(S)));
		Property T = ofac.createObjectProperty(ontoURI + "T"); // equivalent
		equivalents = new HashSet<Property>();
		equivalents.add(T);													// role
		equivalents.add(initialNode); // getDescendants is reflexive
		assertTrue(descendants.contains(new Equivalences<Property>(equivalents)));
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
		Test_TBoxReasonerImplOnNamedDAG namedReasoner = new Test_TBoxReasonerImplOnNamedDAG(dag);
		
		final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

		/**
		 * The initial node is Node P.
		 */
		Property initialNode = ofac
				.createObjectProperty(ontoURI + "P");
		Set<Equivalences<Property>> ancestors = namedReasoner.getSuperProperties(initialNode);

		int size = 0;
		for (Equivalences<Property> a : ancestors)
			size += a.size();

		assertEquals(size, 4); // ancestor is reflexive now

		Property Q = ofac.createObjectProperty(ontoURI + "Q");
		assertTrue(ancestors.contains(namedReasoner.getEquivalences(Q)));
		Property T = ofac.createObjectProperty(ontoURI + "T"); // equivalent
																			// role
		assertTrue(ancestors.contains(namedReasoner.getEquivalences(T)));
		Property U = ofac.createObjectProperty(ontoURI + "U");
		assertTrue(ancestors.contains(namedReasoner.getEquivalences(U)));

		/**
		 * The initial node is Node Q.
		 */
		initialNode = ofac.createObjectProperty(ontoURI + "Q");
		ancestors = namedReasoner.getSuperProperties(initialNode);

		size = 0;
		for (Equivalences<Property> a : ancestors)
			size += a.size();

		assertEquals(size, 3); // ancestor is reflexive now

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
		ancestors = namedReasoner.getSuperProperties(initialNode);

		size = 0;
		for (Equivalences<Property> a : ancestors)
			size += a.size();

		assertEquals(size,4); // ancestor is reflexive now

		Property R = ofac.createObjectProperty(ontoURI + "R"); // equivalent
																			// role
		Set<Property> equivalents = new HashSet<Property>();
		equivalents.add(R);
		equivalents.add(initialNode); // ancestor is reflexive now
		assertTrue(ancestors.contains(new Equivalences<Property>(equivalents)));
		T = ofac.createObjectProperty(ontoURI + "T"); // equivalent
																	// role
		
		assertTrue(ancestors.contains(namedReasoner.getEquivalences(T)));
		U = ofac.createObjectProperty(ontoURI + "U");
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
		initialNode = ofac.createObjectProperty(ontoURI + "U");
		ancestors = namedReasoner.getSuperProperties(initialNode);

		assertEquals(ancestors.size(), 1);

		T = ofac.createObjectProperty(ontoURI + "T"); // equivalent
																	// role
		equivalents = new HashSet<Property>();
		equivalents.add(T); 
		equivalents.add(initialNode); // ancestor is reflexive now
		assertTrue(ancestors.contains(new Equivalences<Property>(equivalents)));
		
	}
}
