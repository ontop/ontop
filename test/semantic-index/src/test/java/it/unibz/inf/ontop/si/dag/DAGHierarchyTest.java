package it.unibz.inf.ontop.si.dag;

/*
 * #%L
 * ontop-quest-owlapi
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


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import junit.framework.TestCase;

import java.util.Set;

import static it.unibz.inf.ontop.utils.SITestingTools.getIRI;
import static it.unibz.inf.ontop.utils.SITestingTools.loadOntologyFromFileAndClassify;

public class DAGHierarchyTest extends TestCase {
	/**
	 * A -> B, B -> {E, F}, {C, D} -> {E, F} with A, B, C, D, E, F are atomic
	 * concepts.
	 */
	private static final String inputFile1 = "src/test/resources/test/dag/test-class-hierarchy.owl";

	/**
	 * P -> Q, Q -> {T, U}, {R, S} -> {T, U} with P, Q, R, S, T, U are atomic
	 * roles.
	 */
	private static final String inputFile2 = "src/test/resources/test/dag/test-role-hierarchy.owl";
	
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

		ClassifiedTBox dag = loadOntologyFromFileAndClassify(inputFile1);
		// generate named DAG
		TestClassifiedTBoxImpl_OnNamedDAG namedReasoner = new TestClassifiedTBoxImpl_OnNamedDAG(dag);

		EquivalencesDAG<ClassExpression> classes = namedReasoner.classesDAG();
		
		ClassExpression A = dag.classes().get(getIRI(ontoURI, "A"));
		ClassExpression B = dag.classes().get(getIRI(ontoURI, "B"));
		ClassExpression C = dag.classes().get(getIRI(ontoURI, "C"));
		ClassExpression D = dag.classes().get(getIRI(ontoURI, "D"));
		ClassExpression E = dag.classes().get(getIRI(ontoURI, "E"));
		ClassExpression F = dag.classes().get(getIRI(ontoURI, "F"));
		
		/**
		 * The initial node is Node A.
		 */
		Equivalences<ClassExpression> initialNode = classes.getVertex(A);
		Set<Equivalences<ClassExpression>> descendants = classes.getSub(initialNode);

		assertEquals(descendants.size(), 1);  // getDescendants is reflexive

		/**
		 * The initial node is Node B.
		 */
		initialNode = classes.getVertex(B);
		descendants = classes.getSub(initialNode);

		assertEquals(2, descendants.size());  // getDescendants is reflexive

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

		assertTrue(descendants.contains(new Equivalences<>(ImmutableSet.of(C, D)))); // getDescendants is reflexive

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
		assertEquals(6, sizeOf(descendants)); // getDescendants is reflexive

		assertTrue(descendants.contains(classes.getVertex(A)));
		assertTrue(descendants.contains(classes.getVertex(B)));
		assertTrue(descendants.contains(classes.getVertex(C)));
		assertTrue(descendants.contains(classes.getVertex(D)));
		
		assertTrue(descendants.contains(new Equivalences<>(ImmutableSet.of(E, F)))); // getDescendants is reflexive
	}

	/**
	 * List all the ancestors of a class in the TBox with having equivalent
	 * classes into account.
	 */
	public void testAncestorClasses() throws Exception {
		final String ontoURI = "http://obda.inf.unibz.it/ontologies/test-class-hierarchy.owl#";

		ClassifiedTBox dag = loadOntologyFromFileAndClassify(inputFile1);
		// generate named DAG
		TestClassifiedTBoxImpl_OnNamedDAG namedReasoner = new TestClassifiedTBoxImpl_OnNamedDAG(dag);

		EquivalencesDAG<ClassExpression> classes = namedReasoner.classesDAG();
		
		ClassExpression A = dag.classes().get(getIRI(ontoURI, "A"));
		ClassExpression B = dag.classes().get(getIRI(ontoURI, "B"));
		ClassExpression C = dag.classes().get(getIRI(ontoURI, "C"));
		ClassExpression D = dag.classes().get(getIRI(ontoURI, "D"));
		ClassExpression E = dag.classes().get(getIRI(ontoURI, "E"));
		ClassExpression F = dag.classes().get(getIRI(ontoURI, "F"));
	
		/**
		 * The initial node is Node A.
		 */

		Equivalences<ClassExpression> initialNode = classes.getVertex(A);
		Set<Equivalences<ClassExpression>> ancestors = classes.getSuper(initialNode);
		assertEquals(4, sizeOf(ancestors));   // ancestors is now reflexive

		assertTrue(ancestors.contains(classes.getVertex(B)));
															// class
		assertTrue(ancestors.contains(classes.getVertex(E)));
		assertTrue(ancestors.contains(classes.getVertex(F)));

		/**
		 * The initial node is Node B.
		 */
		initialNode = classes.getVertex(B);
		ancestors = classes.getSuper(initialNode);
		assertEquals(3, sizeOf(ancestors)); // ancestors is now reflexive

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
		assertEquals(4, sizeOf(ancestors)); // ancestors is now reflexive

		assertTrue(ancestors.contains(new Equivalences<>(ImmutableSet.of(C, D)))); // ancestor is reflexive now
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

		assertEquals(1, ancestors.size());

		assertTrue(ancestors.contains(new Equivalences<>(ImmutableSet.of(E, F))));// ancestor is reflexive now
	}

	/**
	 * List all the descendants of a role in the TBox with having equivalent
	 * roles into account.
	 */
	public void testDescendantRoles() throws Exception {
		final String ontoURI = "http://obda.inf.unibz.it/ontologies/test-role-hierarchy.owl#";

		ClassifiedTBox dag = loadOntologyFromFileAndClassify(inputFile2);
		// generate named DAG
		TestClassifiedTBoxImpl_OnNamedDAG namedReasoner = new TestClassifiedTBoxImpl_OnNamedDAG(dag);

		EquivalencesDAG<ObjectPropertyExpression> properties = namedReasoner.objectPropertiesDAG();
		
		ObjectPropertyExpression P = dag.objectProperties().get(getIRI(ontoURI, "P"));
		ObjectPropertyExpression S = dag.objectProperties().get(getIRI(ontoURI, "S"));
		ObjectPropertyExpression R = dag.objectProperties().get(getIRI(ontoURI, "R"));
		ObjectPropertyExpression Q = dag.objectProperties().get(getIRI(ontoURI, "Q"));
		ObjectPropertyExpression T = dag.objectProperties().get(getIRI(ontoURI, "T"));
		ObjectPropertyExpression U = dag.objectProperties().get(getIRI(ontoURI, "U"));
		
		
		/**
		 * The initial node is Node P.
		 */
		Equivalences<ObjectPropertyExpression> initialNode = properties.getVertex(P);
		Set<Equivalences<ObjectPropertyExpression>> descendants = properties.getSub(initialNode);
		assertEquals(1, descendants.size());  // getDescendants is reflexive

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

		assertEquals(1, descendants.size());

		assertTrue(descendants.contains(new Equivalences<>(ImmutableSet.of(R, S)))); // getDescendants is reflexive

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
		assertEquals(6, sizeOf(descendants));  // getDescendants is reflexive
		
		assertTrue(descendants.contains(properties.getVertex(P)));
		assertTrue(descendants.contains(properties.getVertex(Q)));
		assertTrue(descendants.contains(properties.getVertex(R)));
		assertTrue(descendants.contains(properties.getVertex(S)));
		assertTrue(descendants.contains(new Equivalences<>(ImmutableSet.of(T, U))));// getDescendants is reflexive
	}

	/**
	 * List all the ancestors of a role in the TBox with having equivalent roles
	 * into account.
	 */
	public void testAncestorRoles() throws Exception {
		final String ontoURI = "http://obda.inf.unibz.it/ontologies/test-role-hierarchy.owl#";

		ClassifiedTBox dag = loadOntologyFromFileAndClassify(inputFile2);
		// generate named DAG
		TestClassifiedTBoxImpl_OnNamedDAG namedReasoner = new TestClassifiedTBoxImpl_OnNamedDAG(dag);
		
		EquivalencesDAG<ObjectPropertyExpression> properties = namedReasoner.objectPropertiesDAG();
		
		ObjectPropertyExpression P = dag.objectProperties().get(getIRI(ontoURI, "P"));
		ObjectPropertyExpression S = dag.objectProperties().get(getIRI(ontoURI, "S"));
		ObjectPropertyExpression R = dag.objectProperties().get(getIRI(ontoURI, "R"));
		ObjectPropertyExpression Q = dag.objectProperties().get(getIRI(ontoURI, "Q"));
		ObjectPropertyExpression T = dag.objectProperties().get(getIRI(ontoURI, "T"));
		ObjectPropertyExpression U = dag.objectProperties().get(getIRI(ontoURI, "U"));
	
		/**
		 * The initial node is Node P.
		 */
		Equivalences<ObjectPropertyExpression> initialNode = properties.getVertex(P);
		Set<Equivalences<ObjectPropertyExpression>> ancestors = properties.getSuper(initialNode);
		assertEquals(4, sizeOf(ancestors)); // ancestor is reflexive now

		assertTrue(ancestors.contains(properties.getVertex(Q)));
		assertTrue(ancestors.contains(properties.getVertex(T)));
		assertTrue(ancestors.contains(properties.getVertex(U)));

		/**
		 * The initial node is Node Q.
		 */
		initialNode = properties.getVertex(Q);
		ancestors = properties.getSuper(initialNode);
		assertEquals(3, sizeOf(ancestors)); // ancestor is reflexive now

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
		assertEquals(4, sizeOf(ancestors)); // ancestor is reflexive now

		assertTrue(ancestors.contains(new Equivalences<>(ImmutableSet.of(R, S)))); // ancestor is reflexive now
		
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
		assertEquals(1, ancestors.size());

		assertTrue(ancestors.contains(new Equivalences<>(ImmutableSet.of(T, U))));		// ancestor is reflexive now
	}
}
