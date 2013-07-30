/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGNode;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGOperations;

import java.io.File;
import java.util.Set;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class DAGHierarchyTest extends TestCase {
	/**
	 * A -> B, B -> {E, F}, {C, D} -> {E, F} with A, B, C, D, E, F are atomic concepts.
	 */
	private final String inputFile1 = "src/test/resources/test/dag/test-class-hierarchy.owl";
	
	/**
	 * P -> Q, Q -> {T, U}, {R, S} -> {T, U} with P, Q, R, S, T, U are atomic roles.
	 */
	private final String inputFile2 = "src/test/resources/test/dag/test-role-hierarchy.owl";

	/**
	 * List all the descendants of a class in the TBox with having equivalent classes into account.
	 */
	public void testDescendantClasses() throws Exception {
		final String ontoURI = "http://obda.inf.unibz.it/ontologies/test-class-hierarchy.owl#";
		OWLAPI3Translator t = new OWLAPI3Translator();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owlonto = man.loadOntologyFromOntologyDocument(new File(inputFile1));
		Ontology onto = t.translate(owlonto);
		DAG dag = DAGConstructor.getISADAG(onto);

		DAG pureIsa = DAGConstructor.filterPureISA(dag);
		pureIsa.clean();
		pureIsa.index();

		DAGOperations.buildDescendants(pureIsa);
			
		final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

		/** 
		 * The initial node is Node A.
		 */
		DAGNode initialNode = pureIsa.getClassNode(ofac.createClass(ontoURI + "A"));
		Set<DAGNode> descendants = initialNode.getDescendants();
		
		assertEquals(descendants.size(), 0);
		
		/** 
		 * The initial node is Node B.
		 */
		initialNode = pureIsa.getClassNode(ofac.createClass(ontoURI + "B"));
		descendants = initialNode.getDescendants();
		
		assertEquals(descendants.size(), 1);
		
		DAGNode A = pureIsa.getClassNode(ofac.createClass(ontoURI + "A"));
		assertTrue(descendants.contains(A));
		
		/**
		 * The initial node is Node C.
		 */
		// There is no test for this node because the API will always suggest Node C is not
		// exist and it has been replaced by Node D (i.e., Class C is equivalent with Class D)
		
		/** 
		 * The initial node is Node D.
		 */
		initialNode = pureIsa.getClassNode(ofac.createClass(ontoURI + "D"));
		descendants = initialNode.getDescendants();
		
		assertEquals(descendants.size(), 1);
		
		DAGNode C = new DAGNode(ofac.createClass(ontoURI + "C"));
		assertTrue(descendants.contains(C));
		
		/** 
		 * The initial node is Node E.
		 */
		// There is no test for this node because the API will always suggest Node E is not
		// exist and it has been replaced by Node F (i.e., Class E is equivalent with Class F)
		
		/** 
		 * The initial node is Node F.
		 */
		initialNode = pureIsa.getClassNode(ofac.createClass(ontoURI + "F"));
		descendants = initialNode.getDescendants();
		
		assertEquals(descendants.size(), 5);
		
		A = pureIsa.getClassNode(ofac.createClass(ontoURI + "A"));
		assertTrue(descendants.contains(A));
		DAGNode B = pureIsa.getClassNode(ofac.createClass(ontoURI + "B"));
		assertTrue(descendants.contains(B));
		C = new DAGNode(ofac.createClass(ontoURI + "C")); // equivalent class
		assertTrue(descendants.contains(C));
		DAGNode D = pureIsa.getClassNode(ofac.createClass(ontoURI + "D"));
		assertTrue(descendants.contains(D));
		DAGNode E = new DAGNode(ofac.createClass(ontoURI + "E")); // equivalent class
		assertTrue(descendants.contains(E));
	}

	/**
	 * List all the ancestors of a class in the TBox with having equivalent classes into account.
	 */
	public void testAncestorClasses() throws Exception {
		final String ontoURI = "http://obda.inf.unibz.it/ontologies/test-class-hierarchy.owl#";
		OWLAPI3Translator t = new OWLAPI3Translator();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owlonto = man.loadOntologyFromOntologyDocument(new File(inputFile1));
		Ontology onto = t.translate(owlonto);
		DAG dag = DAGConstructor.getISADAG(onto);

		DAG pureIsa = DAGConstructor.filterPureISA(dag);
		pureIsa.clean();
		pureIsa.index();

		DAGOperations.buildAncestors(pureIsa);
		
		final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

		/** 
		 * The initial node is Node A.
		 */
		DAGNode initialNode = pureIsa.getClassNode(ofac.createClass(ontoURI + "A"));
		Set<DAGNode> ancestors = initialNode.getAncestors();
		
		assertEquals(ancestors.size(), 3);
		
		DAGNode B = pureIsa.getClassNode(ofac.createClass(ontoURI + "B"));
		assertTrue(ancestors.contains(B));
		DAGNode E = new DAGNode(ofac.createClass(ontoURI + "E")); // equivalent class
		assertTrue(ancestors.contains(E));
		DAGNode F = pureIsa.getClassNode(ofac.createClass(ontoURI + "F"));
		assertTrue(ancestors.contains(F));		
		
		/** 
		 * The initial node is Node B.
		 */
		initialNode = pureIsa.getClassNode(ofac.createClass(ontoURI + "B"));
		ancestors = initialNode.getAncestors();
		
		assertEquals(ancestors.size(), 2);
		
		F = pureIsa.getClassNode(ofac.createClass(ontoURI + "F"));
		assertTrue(ancestors.contains(F));		
		E = new DAGNode(ofac.createClass(ontoURI + "E")); // equivalent class
		assertTrue(ancestors.contains(E));
		
		/**
		 * The initial node is Node C.
		 */
		// There is no test for this node because the API will always suggest Node C is not
		// exist and it has been replaced by Node D (i.e., Class C is equivalent with Class D)
		
		/** 
		 * The initial node is Node D.
		 */
		initialNode = pureIsa.getClassNode(ofac.createClass(ontoURI + "D"));
		ancestors = initialNode.getAncestors();
		
		assertEquals(ancestors.size(), 3);
		
		DAGNode C = new DAGNode(ofac.createClass(ontoURI + "C")); // equivalent class
		assertTrue(ancestors.contains(C));
		E = new DAGNode(ofac.createClass(ontoURI + "E")); // equivalent class
		assertTrue(ancestors.contains(E));
		F = pureIsa.getClassNode(ofac.createClass(ontoURI + "F"));
		assertTrue(ancestors.contains(F));
		
		/** 
		 * The initial node is Node E.
		 */
		// There is no test for this node because the API will always suggest Node E is not
		// exist and it has been replaced by Node F (i.e., Class E is equivalent with Class F)
		
		/** 
		 * The initial node is Node F.
		 */
		initialNode = pureIsa.getClassNode(ofac.createClass(ontoURI + "F"));
		ancestors = initialNode.getAncestors();
		
		assertEquals(ancestors.size(), 1);
		
		E = new DAGNode(ofac.createClass(ontoURI + "E")); // equivalent class
		assertTrue(ancestors.contains(E));
	}
	
	/**
	 * List all the descendants of a role in the TBox with having equivalent roles into account.
	 */
	public void testDescendantRoles() throws Exception {	
		final String ontoURI = "http://obda.inf.unibz.it/ontologies/test-role-hierarchy.owl#";
		OWLAPI3Translator t = new OWLAPI3Translator();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owlonto = man.loadOntologyFromOntologyDocument(new File(inputFile2));
		Ontology onto = t.translate(owlonto);
		DAG dag = DAGConstructor.getISADAG(onto);

		DAG pureIsa = DAGConstructor.filterPureISA(dag);
		pureIsa.clean();
		pureIsa.index();

		DAGOperations.buildDescendants(pureIsa);
			
		final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

		/** 
		 * The initial node is Node P.
		 */
		DAGNode initialNode = pureIsa.getRoleNode(ofac.createObjectProperty(ontoURI + "P"));
		Set<DAGNode> descendants = initialNode.getDescendants();
		
		assertEquals(descendants.size(), 0);
		
		/** 
		 * The initial node is Node Q.
		 */
		initialNode = pureIsa.getRoleNode(ofac.createObjectProperty(ontoURI + "Q"));
		descendants = initialNode.getDescendants();
		
		assertEquals(descendants.size(), 1);
		
		DAGNode P = pureIsa.getRoleNode(ofac.createObjectProperty(ontoURI + "P"));
		assertTrue(descendants.contains(P));
		
		/**
		 * The initial node is Node R.
		 */
		// There is no test for this node because the API will always suggest Node R is not
		// exist and it has been replaced by Node S (i.e., Role R is equivalent with Role S)
		
		/** 
		 * The initial node is Node S.
		 */
		initialNode = pureIsa.getRoleNode(ofac.createObjectProperty(ontoURI + "S"));
		descendants = initialNode.getDescendants();
		
		assertEquals(descendants.size(), 1);
		
		DAGNode R = new DAGNode(ofac.createObjectProperty(ontoURI + "R"));
		assertTrue(descendants.contains(R));
		
		/** 
		 * The initial node is Node T.
		 */
		// There is no test for this node because the API will always suggest Node T is not
		// exist and it has been replaced by Node U (i.e., Role T is equivalent with Role U)
		
		/** 
		 * The initial node is Node U.
		 */
		initialNode = pureIsa.getRoleNode(ofac.createObjectProperty(ontoURI + "U"));
		descendants = initialNode.getDescendants();
		
		assertEquals(descendants.size(), 5);
		
		P = pureIsa.getRoleNode(ofac.createObjectProperty(ontoURI + "P"));
		assertTrue(descendants.contains(P));
		DAGNode Q = pureIsa.getRoleNode(ofac.createObjectProperty(ontoURI + "Q"));
		assertTrue(descendants.contains(Q));
		R = new DAGNode(ofac.createObjectProperty(ontoURI + "R")); // equivalent role
		assertTrue(descendants.contains(R));
		DAGNode S = pureIsa.getRoleNode(ofac.createObjectProperty(ontoURI + "S"));
		assertTrue(descendants.contains(S));
		DAGNode T = new DAGNode(ofac.createObjectProperty(ontoURI + "T")); // equivalent role
		assertTrue(descendants.contains(T));
	}

	/**
	 * List all the ancestors of a role in the TBox with having equivalent roles into account.
	 */
	public void testAncestorRoles() throws Exception {
		final String ontoURI = "http://obda.inf.unibz.it/ontologies/test-role-hierarchy.owl#";
		OWLAPI3Translator t = new OWLAPI3Translator();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owlonto = man.loadOntologyFromOntologyDocument(new File(inputFile2));
		Ontology onto = t.translate(owlonto);
		DAG dag = DAGConstructor.getISADAG(onto);

		DAG pureIsa = DAGConstructor.filterPureISA(dag);
		pureIsa.clean();
		pureIsa.index();

		DAGOperations.buildAncestors(pureIsa);
		
		final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

		/** 
		 * The initial node is Node P.
		 */
		DAGNode initialNode = pureIsa.getRoleNode(ofac.createObjectProperty(ontoURI + "P"));
		Set<DAGNode> ancestors = initialNode.getAncestors();
		
		assertEquals(ancestors.size(), 3);
		
		DAGNode Q = pureIsa.getRoleNode(ofac.createObjectProperty(ontoURI + "Q"));
		assertTrue(ancestors.contains(Q));
		DAGNode T = new DAGNode(ofac.createObjectProperty(ontoURI + "T")); // equivalent role
		assertTrue(ancestors.contains(T));
		DAGNode U = pureIsa.getRoleNode(ofac.createObjectProperty(ontoURI + "U"));
		assertTrue(ancestors.contains(U));		
		
		/** 
		 * The initial node is Node Q.
		 */
		initialNode = pureIsa.getRoleNode(ofac.createObjectProperty(ontoURI + "Q"));
		ancestors = initialNode.getAncestors();
		
		assertEquals(ancestors.size(), 2);
		
		T = new DAGNode(ofac.createObjectProperty(ontoURI + "T")); // equivalent role
		assertTrue(ancestors.contains(T));
		U = pureIsa.getRoleNode(ofac.createObjectProperty(ontoURI + "U"));
		assertTrue(ancestors.contains(U));		
		
		/**
		 * The initial node is Node R.
		 */
		// There is no test for this node because the API will always suggest Node R is not
		// exist and it has been replaced by Node S (i.e., Role R is equivalent with Role S)
		
		/** 
		 * The initial node is Node S.
		 */
		initialNode = pureIsa.getRoleNode(ofac.createObjectProperty(ontoURI + "S"));
		ancestors = initialNode.getAncestors();
		
		assertEquals(ancestors.size(), 3);
		
		DAGNode R = new DAGNode(ofac.createObjectProperty(ontoURI + "R")); // equivalent role
		assertTrue(ancestors.contains(R));
		T = new DAGNode(ofac.createObjectProperty(ontoURI + "T")); // equivalent role
		assertTrue(ancestors.contains(T));
		U = pureIsa.getRoleNode(ofac.createObjectProperty(ontoURI + "U"));
		assertTrue(ancestors.contains(U));
		
		/** 
		 * The initial node is Node T.
		 */
		// There is no test for this node because the API will always suggest Node T is not
		// exist and it has been replaced by Node U (i.e., Role T is equivalent with Role U)
		
		/** 
		 * The initial node is Node U.
		 */
		initialNode = pureIsa.getRoleNode(ofac.createObjectProperty(ontoURI + "U"));
		ancestors = initialNode.getAncestors();
		
		assertEquals(ancestors.size(), 1);
		
		T = new DAGNode(ofac.createObjectProperty(ontoURI + "T")); // equivalent role
		assertTrue(ancestors.contains(T));
	}
}
