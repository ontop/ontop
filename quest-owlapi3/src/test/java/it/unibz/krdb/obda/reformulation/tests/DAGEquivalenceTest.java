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


import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.OWLAPI3TranslatorUtility;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.EquivalencesDAG;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Interval;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.SemanticIndexBuilder;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class DAGEquivalenceTest extends TestCase {

	/**
	 * R1 = R2^- = R3, S1 = S2^- = S3, R1 ISA S1
	 */
	private final String testEquivalenceRoles = "src/test/resources/test/dag/role-equivalence.owl";

	/**
	 * A1 = A2^- = A3, B1 = B2^- = B3, C1 = C2^- = C3, C1 ISA B1 ISA A1
	 */
	private final String testEquivalenceRolesInverse = "src/test/resources/test/dag/test-equivalence-roles-inverse.owl";

	/**
	 * A1 = A2 = A3, B1 = B2 = B3, B1 ISA A1
	 */
	private final String testEquivalenceClasses = "src/test/resources/test/dag/test-equivalence-classes.owl";

	public void setUp() {
		// NO-OP
	}

	public void testIndexClasses() throws Exception {
		String testURI = "http://it.unibz.krdb/obda/ontologies/test.owl#";
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owlonto = man.loadOntologyFromOntologyDocument(new File(
				testEquivalenceClasses));
		Ontology onto = OWLAPI3TranslatorUtility.translate(owlonto);

		// generate DAG
		TBoxReasoner dag = new TBoxReasonerImpl(onto);
		
		SemanticIndexBuilder engine = new SemanticIndexBuilder(dag);
		List<Interval> nodeInterval = engine.getRange((OClass)dag.getClassDAG()
					.getVertex(onto.getVocabulary().getClass(testURI + "B1")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		Interval interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getRange((OClass)dag.getClassDAG()
				.getVertex(onto.getVocabulary().getClass(testURI + "B2")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getRange((OClass)dag.getClassDAG()
				.getVertex(onto.getVocabulary().getClass(testURI + "B3")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getRange((OClass)dag.getClassDAG()
				.getVertex(onto.getVocabulary().getClass(testURI + "A1")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getRange((OClass)dag.getClassDAG()
				.getVertex(onto.getVocabulary().getClass(testURI + "A2")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getRange((OClass)dag.getClassDAG()
				.getVertex(onto.getVocabulary().getClass(testURI + "A3")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);
	}

	public void testIntervalsRoles() throws Exception {
		String testURI = "http://it.unibz.krdb/obda/ontologies/Ontology1314774461138.owl#";
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owlonto = man.loadOntologyFromOntologyDocument(new File(
				testEquivalenceRoles));
		Ontology onto = OWLAPI3TranslatorUtility.translate(owlonto);
		// generate DAG
		TBoxReasoner dag = new TBoxReasonerImpl(onto);
		// generate named DAG
		SemanticIndexBuilder engine = new SemanticIndexBuilder(dag);
		
		List<Interval> nodeInterval = engine.getRange(dag.getObjectPropertyDAG()
				.getVertex(onto.getVocabulary().getObjectProperty(testURI + "R1")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		Interval interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getRange(dag.getObjectPropertyDAG()
				.getVertex(onto.getVocabulary().getObjectProperty(testURI + "R2")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getRange(dag.getObjectPropertyDAG()
				.getVertex(onto.getVocabulary().getObjectProperty(testURI + "R3")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getRange(dag.getObjectPropertyDAG()
				.getVertex(onto.getVocabulary().getObjectProperty(testURI + "S1")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getRange(dag.getObjectPropertyDAG()
				.getVertex(onto.getVocabulary().getObjectProperty(testURI + "S2")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getRange(dag.getObjectPropertyDAG()
				.getVertex(onto.getVocabulary().getObjectProperty(testURI + "S3")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);
	}

	public void testIntervalsRolesWithInverse() throws Exception {
		String testURI = "http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#";
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owlonto = man.loadOntologyFromOntologyDocument(new File(
				testEquivalenceRolesInverse));
		Ontology onto = OWLAPI3TranslatorUtility.translate(owlonto);
		// generate DAG
		TBoxReasoner dag = new TBoxReasonerImpl(onto);
		// generate named DAG
		SemanticIndexBuilder engine = new SemanticIndexBuilder(dag);
		
		List<Interval> nodeInterval = engine.getRange(dag.getObjectPropertyDAG()
				.getVertex(onto.getVocabulary().getObjectProperty(testURI + "A1")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		Interval interval = nodeInterval.get(0);
		assertEquals(1, interval.getStart());
		assertEquals(3, interval.getEnd());

		EquivalencesDAG<ObjectPropertyExpression> properties = dag.getObjectPropertyDAG();
		
		ObjectPropertyExpression d = properties.getVertex(onto.getVocabulary().getObjectProperty(testURI + "A2")).getRepresentative();
		assertTrue(d.equals(onto.getVocabulary().getObjectProperty(testURI + "A1").getInverse()));

		nodeInterval = engine.getRange(dag.getObjectPropertyDAG()
				.getVertex(onto.getVocabulary().getObjectProperty(testURI + "A3")).getRepresentative()).getIntervals();
		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(1, interval.getStart());
		assertEquals(3, interval.getEnd());

		nodeInterval = engine.getRange(dag.getObjectPropertyDAG()
				.getVertex(onto.getVocabulary().getObjectProperty(testURI + "C1")).getRepresentative()).getIntervals();
		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(3, interval.getStart());
		assertEquals(3, interval.getEnd());

		d = properties.getVertex(onto.getVocabulary().getObjectProperty(testURI + "C2")).getRepresentative();
		assertTrue(d.equals(properties.getVertex(onto.getVocabulary().getObjectProperty(testURI + "C1").getInverse()).getRepresentative()));

		nodeInterval = engine.getRange(dag.getObjectPropertyDAG()
				.getVertex(onto.getVocabulary().getObjectProperty(testURI + "C3")).getRepresentative()).getIntervals();
		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(3, interval.getStart());
		assertEquals(3, interval.getEnd());

		nodeInterval = engine.getRange(dag.getObjectPropertyDAG()
				.getVertex(onto.getVocabulary().getObjectProperty(testURI + "B1")).getRepresentative()).getIntervals();
		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);

		assertEquals(2, interval.getStart());
		assertEquals(3, interval.getEnd());

		d = properties.getVertex(onto.getVocabulary().getObjectProperty(testURI + "B2")).getRepresentative();
		assertTrue(d.equals(properties.getVertex(onto.getVocabulary().getObjectProperty(testURI + "B3").getInverse()).getRepresentative()));

		nodeInterval = engine.getRange(dag.getObjectPropertyDAG()
				.getVertex(onto.getVocabulary().getObjectProperty(testURI + "B3")).getRepresentative()).getIntervals();
		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(2, interval.getStart());
		assertEquals(3, interval.getEnd());
	}
}
