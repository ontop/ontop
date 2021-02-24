package it.unibz.inf.ontop.owlapi;

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

import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.Class;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.*;

public class InconsistencyCheckingTest {

	private OntopOWLReasoner reasoner;
	private OWLOntology ontology;
	private OWLOntologyManager manager;
	
	private static final String prefix = "http://www.example.org/";
	private final OWLClass c1 = Class(IRI.create(prefix + "Male"));
	private final OWLClass c2 = Class(IRI.create(prefix + "Female"));

	private final OWLObjectProperty r1 = ObjectProperty(IRI.create(prefix + "hasMother"));
	private final OWLObjectProperty r2 = ObjectProperty(IRI.create(prefix + "hasFather"));

	private final OWLDataProperty d1 = DataProperty(IRI.create(prefix + "hasAgeFirst"));
	private final OWLDataProperty d2 = DataProperty(IRI.create(prefix + "hasAge"));

	private final OWLNamedIndividual a = NamedIndividual(IRI.create(prefix + "a"));
	private final OWLNamedIndividual b = NamedIndividual(IRI.create(prefix + "b"));
	private final OWLNamedIndividual c = NamedIndividual(IRI.create(prefix + "c"));
	
	@Before
	public void setUp() throws Exception {
		manager = OWLManager.createOWLOntologyManager();
		ontology = Ontology(manager, //
				Declaration(c1),
				Declaration(c2),
				Declaration(r1), //
				Declaration(r2), //
				Declaration(d1), //
				Declaration(d2)
		);
	}
	
	private void startReasoner() throws Exception {
		Properties properties = new Properties();

		try (OntopSemanticIndexLoader siLoader = OntopSemanticIndexLoader.loadOntologyIndividuals(ontology, properties)) {
			OntopOWLFactory ontopOWLFactory = OntopOWLFactory.defaultFactory();
			reasoner = ontopOWLFactory.createReasoner(siLoader.getConfiguration());
		}
	}
	
	@Test
	public void testInitialConsistency() throws Exception {
		//initially the ontology is consistent
		startReasoner();
		assertTrue(reasoner.isConsistent());
	}
	
	@Test
	public void testDisjointClassInconsistency() throws Exception {

		//Male(a), Female(a), disjoint(Male, Female)
		manager.addAxiom(ontology, ClassAssertion(c1, a));
		manager.addAxiom(ontology, ClassAssertion(c2, a));
		manager.addAxiom(ontology, DisjointClasses(c1, c2));
		
		startReasoner();
		
		assertFalse(reasoner.isConsistent());
	} 
	
	@Test
	public void testDisjointObjectPropInconsistency() throws Exception {

		//hasMother(a, b), hasFather(a,b), disjoint(hasMother, hasFather)
		manager.addAxiom(ontology,ObjectPropertyAssertion(r1, a, b)); //
		manager.addAxiom(ontology,ObjectPropertyAssertion(r2, a, b)); //
		manager.addAxiom(ontology, DisjointObjectProperties(r1, r2));
		
		startReasoner();
		
		assertFalse(reasoner.isConsistent());
	}
	
	@Test
	public void testDisjointDataPropInconsistency() throws Exception {

		//hasAgeFirst(a, 21), hasAge(a, 21), disjoint(hasAgeFirst, hasAge)
		manager.addAxiom(ontology, DataPropertyAssertion(d1, a, Literal(21)));
		manager.addAxiom(ontology, DataPropertyAssertion(d2, a, Literal(21)));
		manager.addAxiom(ontology, DisjointDataProperties(d1, d2));
		
		startReasoner();
		
		assertFalse(reasoner.isConsistent());
	} 
	
	@Test
	public void testFunctionalObjPropInconsistency() throws Exception {

		//hasMother(a,b), hasMother(a,c), func(hasMother)
		manager.addAxiom(ontology,ObjectPropertyAssertion(r1, a, b)); //
		manager.addAxiom(ontology,ObjectPropertyAssertion(r1, a, c)); //
		manager.addAxiom(ontology, FunctionalObjectProperty(r1));
		
		startReasoner();
		
		assertFalse(reasoner.isConsistent());
	} 
	
	@Test
	public void testFunctionalDataPropInconsistency() throws Exception {

		//hasAge(a, 18), hasAge(a, 21), func(hasAge)
		manager.addAxiom(ontology, DataPropertyAssertion(d1, a, Literal(18)));
		manager.addAxiom(ontology, DataPropertyAssertion(d1, a, Literal(21)));
		manager.addAxiom(ontology, FunctionalDataProperty(d1));
		
		startReasoner();
		
		assertFalse(reasoner.isConsistent());
	} 
}
