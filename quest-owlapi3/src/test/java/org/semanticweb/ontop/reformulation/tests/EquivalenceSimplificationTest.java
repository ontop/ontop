package org.semanticweb.ontop.reformulation.tests;

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


import junit.framework.TestCase;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.ontology.Ontology;
import org.semanticweb.ontop.ontology.OntologyFactory;
import org.semanticweb.ontop.ontology.impl.OntologyFactoryImpl;
import org.semanticweb.ontop.owlapi3.OWLAPI3Translator;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import org.semanticweb.ontop.owlrefplatform.core.tboxprocessing.TBoxReasonerToOntology;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;

public class EquivalenceSimplificationTest extends TestCase {

	final String	path	= "src/test/resources/test/equivalence/";

	public void test_equivalence_namedclasses() throws Exception {

		/*
		 * The ontology contains A1 = A2 = A3, B1 ISA A1, B1 = B2 = B3, this
		 * gives 9 inferences and R1 = R2 = R3, S1 ISA R1, S1 = S2 = S3, this
		 * gives 36 inferences (counting inverse related inferences, and exist
		 * related inferences. Total, 45 inferences
		 */

		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		File file = new File(path + "test_401.owl");
		OWLOntology owlonto = man.loadOntologyFromOntologyDocument(file);
		OWLAPI3Translator translator = new OWLAPI3Translator();
		Ontology ontology = translator.translate(owlonto);

		TBoxReasoner reasoner = new TBoxReasonerImpl(ontology);
		TBoxReasoner simple = TBoxReasonerImpl.getEquivalenceSimplifiedReasoner(reasoner);
		Ontology simpleonto = TBoxReasonerToOntology.getOntology(simple);

		assertEquals(3, simpleonto.getVocabulary().getClasses().size());
		assertEquals(0, simpleonto.getVocabulary().getObjectProperties().size());
		System.out.println(simpleonto.getSubClassAxioms());
		System.out.println(simpleonto.getSubPropertyAxioms());
		assertEquals(3, simpleonto.getSubClassAxioms().size() + simpleonto.getSubPropertyAxioms().size());
//		assertEquals(3, simpleonto.getVocabulary().size());

		OntologyFactory ofac = OntologyFactoryImpl.getInstance();
		OBDADataFactory odfac = OBDADataFactoryImpl.getInstance();

		//assertEquals(6, eqMap.keySetSize());
		assertFalse(simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A1")) != null);
		assertFalse(simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B1")) != null);
		assertFalse(simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C1")) != null);
		assertTrue(simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A2")) != null);
		assertTrue(simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A3")) != null);
		assertTrue(simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B2")) != null);
		assertTrue(simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B3")) != null); // Roman: instead of B1
		assertTrue(simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C2")) != null);
		assertTrue(simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C3")) != null);
		
		assertEquals(ofac.createClass("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A1"),simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A2")));
		assertEquals(ofac.createClass("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A1"),simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A3")));
		assertEquals(ofac.createClass("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B1"),simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B2"))); // Roman: B3 -> B1
		assertEquals(ofac.createClass("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B1"),simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B3"))); // Roman: B3 <-> B1
		assertEquals(ofac.createClass("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C1"),simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C2")));
		assertEquals(ofac.createClass("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C1"),simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C3")));

	}
	
	
	public void test_equivalence_namedproperties() throws Exception {

		/*
		 * The ontology contains A1 = A2 = A3, B1 ISA A1, B1 = B2 = B3, this
		 * gives 9 inferences and R1 = R2 = R3, S1 ISA R1, S1 = S2 = S3, this
		 * gives 36 inferences (counting inverse related inferences, and exist
		 * related inferences. Total, 45 inferences
		 */

		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		File file = new File(path + "test_402.owl");
		OWLOntology owlonto = man.loadOntologyFromOntologyDocument(file);
		OWLAPI3Translator translator = new OWLAPI3Translator();
		Ontology ontology = translator.translate(owlonto);
		
		TBoxReasoner reasoner = new TBoxReasonerImpl(ontology);
		TBoxReasoner simple = TBoxReasonerImpl.getEquivalenceSimplifiedReasoner(reasoner);
		Ontology simpleonto = TBoxReasonerToOntology.getOntology(simple);

		assertEquals(0, simpleonto.getVocabulary().getClasses().size());
		assertEquals(3, simpleonto.getVocabulary().getObjectProperties().size());
		assertEquals(12,  simpleonto.getSubClassAxioms().size() + simpleonto.getSubPropertyAxioms().size());
//		assertEquals(3, simpleonto.getVocabulary().size());

		OntologyFactory ofac = OntologyFactoryImpl.getInstance();
		OBDADataFactory odfac = OBDADataFactoryImpl.getInstance();
		
		//assertEquals(6, eqMap.keySetSize());
		assertFalse(simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A1")) != null);
		assertFalse(simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B1")) != null);
		assertFalse(simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C1")) != null);
		assertTrue(simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A2")) != null);
		assertTrue(simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A3")) != null);
		assertTrue(simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B2")) != null);
		assertTrue(simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B3")) != null); // ROMAN: B1 and B3 ARE SYMMETRIC
		assertTrue(simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C2")) != null);
		assertTrue(simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C3")) != null);
		
		assertEquals(ofac.createObjectProperty("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A1"),simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A2")));
		assertEquals(ofac.createObjectProperty("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A1"),simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A3")));
		assertEquals(ofac.createObjectProperty("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B1"),simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B2"))); // ROMAN: B3 -> B1
		assertEquals(ofac.createObjectProperty("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B1"),simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B3"))); // ROMAN: B3 <-> B1
		assertEquals(ofac.createObjectProperty("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C1"),simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C2")));
		assertEquals(ofac.createObjectProperty("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C1"),simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C3")));
	}
	
	
	public void test_equivalence_namedclassesandexists() throws Exception {

		/*
		 * The ontology contains A1 = A2 = A3, B1 ISA A1, B1 = B2 = B3, this
		 * gives 9 inferences and R1 = R2 = R3, S1 ISA R1, S1 = S2 = S3, this
		 * gives 36 inferences (counting inverse related inferences, and exist
		 * related inferences. Total, 45 inferences
		 */

		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		File file = new File(path + "test_403.owl");
		OWLOntology owlonto = man.loadOntologyFromOntologyDocument(file);
		OWLAPI3Translator translator = new OWLAPI3Translator();
		Ontology ontology = translator.translate(owlonto);

		TBoxReasoner reasoner = new TBoxReasonerImpl(ontology);
		TBoxReasoner simple = TBoxReasonerImpl.getEquivalenceSimplifiedReasoner(reasoner);
		Ontology simpleonto = TBoxReasonerToOntology.getOntology(simple);

		assertEquals(simpleonto.getVocabulary().getClasses().toString(), 3, simpleonto.getVocabulary().getClasses().size());
		assertEquals(3, simpleonto.getVocabulary().getObjectProperties().size());
		assertEquals(3, simpleonto.getVocabulary().getClasses().size());
		assertEquals(9,  simpleonto.getSubClassAxioms().size() + simpleonto.getSubPropertyAxioms().size());
//		assertEquals(6, simpleonto.getVocabulary().size());

		OntologyFactory ofac = OntologyFactoryImpl.getInstance();
		OBDADataFactory odfac = OBDADataFactoryImpl.getInstance();

		//assertEquals(3, eqMap.keySetSize());
		assertFalse(simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A1")) != null);
		assertFalse(simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B1")) != null);
		assertFalse(simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C1")) != null);
		assertFalse(simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A2")) != null);
		assertFalse(simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B2")) != null);
		assertFalse(simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C2")) != null);
		assertTrue(simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A3")) != null);
		assertTrue(simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B3")) != null); // Roman: instead of B1
		assertTrue(simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C3")) != null);
		
		assertEquals(ofac.createClass("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A1"),simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A3")));
		assertEquals(ofac.createClass("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B1"),simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B3"))); // Roman B1 <-> B3
		assertEquals(ofac.createClass("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C1"),simple.getClassRepresentative(odfac.getClassPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C3")));
		
	}
	
	public void test_equivalence_namedproperties_and_inverses() throws Exception {

		/*
		 * The ontology contains A1 = A2 = A3, B1 ISA A1, B1 = B2 = B3, this
		 * gives 9 inferences and R1 = R2 = R3, S1 ISA R1, S1 = S2 = S3, this
		 * gives 36 inferences (counting inverse related inferences, and exist
		 * related inferences. Total, 45 inferences
		 */

		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		File file = new File(path + "test_404.owl");
		OWLOntology owlonto = man.loadOntologyFromOntologyDocument(file);
		OWLAPI3Translator translator = new OWLAPI3Translator();
		Ontology ontology = translator.translate(owlonto);

		TBoxReasoner reasoner = new TBoxReasonerImpl(ontology);
		TBoxReasoner simple = TBoxReasonerImpl.getEquivalenceSimplifiedReasoner(reasoner);
		Ontology simpleonto = TBoxReasonerToOntology.getOntology(simple);

		assertEquals(12,  simpleonto.getSubClassAxioms().size() + simpleonto.getSubPropertyAxioms().size());
		assertEquals(0, simpleonto.getVocabulary().getClasses().size());
		assertEquals(3, simpleonto.getVocabulary().getObjectProperties().size());
//		assertEquals(3, simpleonto.getVocabulary().size());

		OntologyFactory ofac = OntologyFactoryImpl.getInstance();
		OBDADataFactory odfac = OBDADataFactoryImpl.getInstance();

		//assertEquals(6, eqMap.keySetSize());
		assertFalse(simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A1")) != null);
		assertFalse(simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B1")) != null);
		assertFalse(simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C1")) != null);
		assertTrue(simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A2")) != null);
		assertTrue(simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A3")) != null);
		assertTrue(simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B2")) != null);
		assertTrue(simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B3")) != null); // ROMAN: again, B1 and B3 are symmetric
		assertTrue(simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C2")) != null);
		assertTrue(simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C3")) != null);
		
		assertEquals(ofac.createObjectProperty("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A1").getInverse(),simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A2")));
		assertEquals(ofac.createObjectProperty("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A1"),simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#A3")));
		assertEquals(ofac.createObjectProperty("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B1").getInverse(),simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B2"))); // B3 -> B1
		assertEquals(ofac.createObjectProperty("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B1"),simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#B3")));  //  B1 <-> B3
		assertEquals(ofac.createObjectProperty("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C1").getInverse(),simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C2")));
		assertEquals(ofac.createObjectProperty("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C1"),simple.getPropertyRepresentative(odfac.getObjectPropertyPredicate("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#C3")));
	}

}
