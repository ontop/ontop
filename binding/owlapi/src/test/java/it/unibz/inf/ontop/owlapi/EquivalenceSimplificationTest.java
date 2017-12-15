package it.unibz.inf.ontop.owlapi;

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


import it.unibz.inf.ontop.spec.ontology.ClassExpression;
import it.unibz.inf.ontop.spec.ontology.ObjectPropertyExpression;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorUtility;
import it.unibz.inf.ontop.spec.ontology.EquivalencesDAG;
import it.unibz.inf.ontop.spec.ontology.impl.EquivalencesDAGImpl;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;
import it.unibz.inf.ontop.spec.ontology.impl.TBoxReasonerImpl;
import junit.framework.TestCase;

public class EquivalenceSimplificationTest extends TestCase {

	private final String testURI = "http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#";
	private final String path = "src/test/resources/test/equivalence/";

	public void test_equivalence_namedclasses() throws Exception {

		/*
		 * The ontology contains classes A1 = A2 = A3 >= B1 = B2 = B3 >= C1 = C2 = C3
		 */

		Ontology ontology = OWLAPITranslatorUtility.loadOntologyFromFile(path + "test_401.owl");
		TBoxReasoner simple = TBoxReasonerImpl.create(ontology);

		EquivalencesDAGImpl<ClassExpression> classDAG = (EquivalencesDAGImpl<ClassExpression>)simple.classes().dag();
		EquivalencesDAGImpl<ObjectPropertyExpression> propDAG = (EquivalencesDAGImpl<ObjectPropertyExpression>)simple.objectProperties().dag();
		
		assertEquals(3, classDAG.vertexSetSize()); // A1, B1, C1
		assertEquals(0, propDAG.vertexSetSize()); // no properties
		assertEquals(2, classDAG.edgeSetSize());  // A1 <- B1 <- C1
		assertEquals(0, propDAG.edgeSetSize());  // no properties

		ClassExpression A1 = ontology.getClass(testURI + "A1");
		ClassExpression B1 = ontology.getClass(testURI + "B1");
		ClassExpression C1 = ontology.getClass(testURI + "C1");
		ClassExpression A2 = ontology.getClass(testURI + "A2");
		ClassExpression A3 = ontology.getClass(testURI + "A3");
		ClassExpression B2 = ontology.getClass(testURI + "B2");
		ClassExpression B3 = ontology.getClass(testURI + "B3");
		ClassExpression C2 = ontology.getClass(testURI + "C2");
		ClassExpression C3 = ontology.getClass(testURI + "C3");

		EquivalencesDAG<ClassExpression> classes = simple.classes().dag();
		assertEquals(classes.getCanonicalForm(A1), A1);
		assertEquals(classes.getCanonicalForm(B1), B1);
		assertEquals(classes.getCanonicalForm(C1), C1);
		assertEquals(classes.getCanonicalForm(A2), A1);
		assertEquals(classes.getCanonicalForm(A3), A1);
		assertEquals(classes.getCanonicalForm(B2), B1);
		assertEquals(classes.getCanonicalForm(B3), B1);
		assertEquals(classes.getCanonicalForm(C2), C1);
		assertEquals(classes.getCanonicalForm(C3), C1);
	}
	
	
	public void test_equivalence_namedproperties() throws Exception {

		/*
		 * The ontology contains object properties A1 = A2 = A3 >= B1 = B2 = B3 >= C1 = C2 = C3
		 */

		Ontology ontology = OWLAPITranslatorUtility.loadOntologyFromFile(path + "test_402.owl");
		TBoxReasoner simple = TBoxReasonerImpl.create(ontology);

		EquivalencesDAGImpl<ClassExpression> classDAG = (EquivalencesDAGImpl<ClassExpression>)simple.classes().dag();
		EquivalencesDAGImpl<ObjectPropertyExpression> propDAG = (EquivalencesDAGImpl<ObjectPropertyExpression>)simple.objectProperties().dag();
		
		// \exists A1, \exists A1^-,  \exists B1, \exists B1^-,  \exists C1, \exists C1^-
		assertEquals(6, classDAG.vertexSetSize()); 
		assertEquals(6, propDAG.vertexSetSize()); // A1, A1^-, B1, B1^-, C1, C1^- 
		// \exists A1 <- \exists B1 <- \exists C1, \exists A1^- <- \exists B1^- <- \exists C1^-
		assertEquals(4, classDAG.edgeSetSize()); 
		assertEquals(4, classDAG.edgeSetSize()); // A1 <- B1 <- C1, A1^- <- B1^- <- C1^-

		
		ObjectPropertyExpression A1 = ontology.getObjectProperty(testURI + "A1");
		ObjectPropertyExpression B1 = ontology.getObjectProperty(testURI + "B1");
		ObjectPropertyExpression C1 = ontology.getObjectProperty(testURI + "C1");
		ObjectPropertyExpression A2 = ontology.getObjectProperty(testURI + "A2");
		ObjectPropertyExpression A3 = ontology.getObjectProperty(testURI + "A3");
		ObjectPropertyExpression B2 = ontology.getObjectProperty(testURI + "B2");
		ObjectPropertyExpression B3 = ontology.getObjectProperty(testURI + "B3");
		ObjectPropertyExpression C2 = ontology.getObjectProperty(testURI + "C2");
		ObjectPropertyExpression C3 = ontology.getObjectProperty(testURI + "C3");

		EquivalencesDAG<ObjectPropertyExpression> ops = simple.objectProperties().dag();
		assertEquals(ops.getCanonicalForm(A1), A1);
		assertEquals(ops.getCanonicalForm(B1), B1);
		assertEquals(ops.getCanonicalForm(C1), C1);
		assertEquals(ops.getCanonicalForm(A2), A1);
		assertEquals(ops.getCanonicalForm(A3), A1);
		assertEquals(ops.getCanonicalForm(B2), B1);
		assertEquals(ops.getCanonicalForm(B3), B1); 
		assertEquals(ops.getCanonicalForm(C2), C1);
		assertEquals(ops.getCanonicalForm(C3), C1);
	}
	
	
	public void test_equivalence_namedclassesandexists() throws Exception {

		/*
		 * The ontology contains object properties M, R, S
		 * and classes A1 = A3 = \exists R <= B1 = B3 = \exists S^- <= C1 = C3 = \exists M
		 */

		Ontology ontology = OWLAPITranslatorUtility.loadOntologyFromFile(path + "test_403.owl");
		TBoxReasoner simple = TBoxReasonerImpl.create(ontology);
		
		EquivalencesDAGImpl<ClassExpression> classDAG = (EquivalencesDAGImpl<ClassExpression>)simple.classes().dag();
		EquivalencesDAGImpl<ObjectPropertyExpression> propDAG = (EquivalencesDAGImpl<ObjectPropertyExpression>)simple.objectProperties().dag();
		
		assertEquals(6, propDAG.vertexSetSize()); // M, M^-, R, R^-, S, S^-
		assertEquals(6, classDAG.vertexSetSize()); // A1, B1, C1, \exists R^-, \exists S, \exists M^-
		assertEquals(0, propDAG.edgeSetSize()); // 
		assertEquals(2, classDAG.edgeSetSize()); // A1 <- B1 <- C1

		ClassExpression A1 = ontology.getClass(testURI + "A1");
		ClassExpression B1 = ontology.getClass(testURI + "B1");
		ClassExpression C1 = ontology.getClass(testURI + "C1");
		ClassExpression A3 = ontology.getClass(testURI + "A3");
		ClassExpression B3 = ontology.getClass(testURI + "B3");
		ClassExpression C3 = ontology.getClass(testURI + "C3");

		EquivalencesDAG<ClassExpression> classes = simple.classes().dag();
		assertEquals(classes.getCanonicalForm(A1), A1);
		assertEquals(classes.getCanonicalForm(B1), B1);
		assertEquals(classes.getCanonicalForm(C1), C1);
		assertEquals(classes.getCanonicalForm(A3), A1);
		assertEquals(classes.getCanonicalForm(B3), B1);
		assertEquals(classes.getCanonicalForm(C3), C1);		
	}
	
	public void test_equivalence_namedproperties_and_inverses() throws Exception {

		/*
		 * The ontology contains object properties A1 = A2^- = A3 >= B1 = B2^- = B3 >= C1 = C2^- = C3
		 */

		Ontology ontology = OWLAPITranslatorUtility.loadOntologyFromFile(path + "test_404.owl");
		TBoxReasoner simple = TBoxReasonerImpl.create(ontology);

		EquivalencesDAGImpl<ClassExpression> classDAG = (EquivalencesDAGImpl<ClassExpression>)simple.classes().dag();
		EquivalencesDAGImpl<ObjectPropertyExpression> propDAG = (EquivalencesDAGImpl<ObjectPropertyExpression>)simple.objectProperties().dag();
		
		assertEquals(6, classDAG.vertexSetSize()); // A1, A1^-, B1, B1^-, C1, C1^-
		assertEquals(6, propDAG.vertexSetSize()); // 
		assertEquals(4, classDAG.edgeSetSize()); // A1 >= B1 >= C1, A1^- >= B1^- >= C1^-
		assertEquals(4, propDAG.edgeSetSize()); //

		ObjectPropertyExpression A1 = ontology.getObjectProperty(testURI + "A1");
		ObjectPropertyExpression B1 = ontology.getObjectProperty(testURI + "B1");
		ObjectPropertyExpression C1 = ontology.getObjectProperty(testURI + "C1");
		ObjectPropertyExpression A2 = ontology.getObjectProperty(testURI + "A2");
		ObjectPropertyExpression A3 = ontology.getObjectProperty(testURI + "A3");
		ObjectPropertyExpression B2 = ontology.getObjectProperty(testURI + "B2");
		ObjectPropertyExpression B3 = ontology.getObjectProperty(testURI + "B3");
		ObjectPropertyExpression C2 = ontology.getObjectProperty(testURI + "C2");
		ObjectPropertyExpression C3 = ontology.getObjectProperty(testURI + "C3");

		EquivalencesDAG<ObjectPropertyExpression> ops = simple.objectProperties().dag();
		assertEquals(ops.getCanonicalForm(A1), A1);
		assertEquals(ops.getCanonicalForm(B1), B1);
		assertEquals(ops.getCanonicalForm(C1), C1);
		assertEquals(ops.getCanonicalForm(A2), A1.getInverse());
		assertEquals(ops.getCanonicalForm(A3), A1);
		assertEquals(ops.getCanonicalForm(B2), B1.getInverse());
		assertEquals(ops.getCanonicalForm(B3), B1); 
		assertEquals(ops.getCanonicalForm(C2), C1.getInverse());
		assertEquals(ops.getCanonicalForm(C3), C1);
	}

}
