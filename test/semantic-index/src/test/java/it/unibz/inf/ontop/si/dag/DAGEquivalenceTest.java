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


import it.unibz.inf.ontop.si.repository.impl.Interval;
import it.unibz.inf.ontop.si.repository.impl.SemanticIndexBuilder;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import junit.framework.TestCase;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.util.List;

import static it.unibz.inf.ontop.utils.SITestingTools.OWLAPI_TRANSLATOR;


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
		String testURI = "http://it.unibz.inf/obda/ontologies/test.owl#";
		ClassifiedTBox dag = DAGEquivalenceTest.loadOntologyFromFileAndClassify(testEquivalenceClasses);

		SemanticIndexBuilder engine = new SemanticIndexBuilder(dag);
		List<Interval> nodeInterval = engine.getRange((OClass)dag.classesDAG()
					.getVertex(dag.classes().get(testURI + "B1")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		Interval interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getRange((OClass)dag.classesDAG()
				.getVertex(dag.classes().get(testURI + "B2")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getRange((OClass)dag.classesDAG()
				.getVertex(dag.classes().get(testURI + "B3")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getRange((OClass)dag.classesDAG()
				.getVertex(dag.classes().get(testURI + "A1")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getRange((OClass)dag.classesDAG()
				.getVertex(dag.classes().get(testURI + "A2")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getRange((OClass)dag.classesDAG()
				.getVertex(dag.classes().get(testURI + "A3")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);
	}

	public void testIntervalsRoles() throws Exception {
		String testURI = "http://it.unibz.inf/obda/ontologies/Ontology1314774461138.owl#";
		ClassifiedTBox dag = DAGEquivalenceTest.loadOntologyFromFileAndClassify(testEquivalenceRoles);
		// generate named DAG
		SemanticIndexBuilder engine = new SemanticIndexBuilder(dag);

		List<Interval> nodeInterval = engine.getRange(dag.objectPropertiesDAG()
				.getVertex(dag.objectProperties().get(testURI + "R1")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		Interval interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getRange(dag.objectPropertiesDAG()
				.getVertex(dag.objectProperties().get(testURI + "R2")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getRange(dag.objectPropertiesDAG()
				.getVertex(dag.objectProperties().get(testURI + "R3")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getRange(dag.objectPropertiesDAG()
				.getVertex(dag.objectProperties().get(testURI + "S1")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getRange(dag.objectPropertiesDAG()
				.getVertex(dag.objectProperties().get(testURI + "S2")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getRange(dag.objectPropertiesDAG()
				.getVertex(dag.objectProperties().get(testURI + "S3")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);
	}

	public void testIntervalsRolesWithInverse() throws Exception {
		String testURI = "http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#";
		ClassifiedTBox dag = DAGEquivalenceTest.loadOntologyFromFileAndClassify(testEquivalenceRolesInverse);
		// generate named DAG
		SemanticIndexBuilder engine = new SemanticIndexBuilder(dag);
		
		List<Interval> nodeInterval = engine.getRange(dag.objectPropertiesDAG()
				.getVertex(dag.objectProperties().get(testURI + "A1")).getRepresentative()).getIntervals();

		assertEquals(nodeInterval.size(), 1);
		Interval interval = nodeInterval.get(0);
		assertEquals(1, interval.getStart());
		assertEquals(3, interval.getEnd());

		EquivalencesDAG<ObjectPropertyExpression> properties = dag.objectPropertiesDAG();
		
		ObjectPropertyExpression d = properties.getVertex(dag.objectProperties().get(testURI + "A2")).getRepresentative();
		assertTrue(d.equals(dag.objectProperties().get(testURI + "A1").getInverse()));

		nodeInterval = engine.getRange(dag.objectPropertiesDAG()
				.getVertex(dag.objectProperties().get(testURI + "A3")).getRepresentative()).getIntervals();
		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(1, interval.getStart());
		assertEquals(3, interval.getEnd());

		nodeInterval = engine.getRange(dag.objectPropertiesDAG()
				.getVertex(dag.objectProperties().get(testURI + "C1")).getRepresentative()).getIntervals();
		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(3, interval.getStart());
		assertEquals(3, interval.getEnd());

		d = properties.getVertex(dag.objectProperties().get(testURI + "C2")).getRepresentative();
		assertTrue(d.equals(properties.getVertex(dag.objectProperties().get(testURI + "C1").getInverse()).getRepresentative()));

		nodeInterval = engine.getRange(dag.objectPropertiesDAG()
				.getVertex(dag.objectProperties().get(testURI + "C3")).getRepresentative()).getIntervals();
		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(3, interval.getStart());
		assertEquals(3, interval.getEnd());

		nodeInterval = engine.getRange(dag.objectPropertiesDAG()
				.getVertex(dag.objectProperties().get(testURI + "B1")).getRepresentative()).getIntervals();
		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);

		assertEquals(2, interval.getStart());
		assertEquals(3, interval.getEnd());

		d = properties.getVertex(dag.objectProperties().get(testURI + "B2")).getRepresentative();
		assertTrue(d.equals(properties.getVertex(dag.objectProperties().get(testURI + "B3").getInverse()).getRepresentative()));

		nodeInterval = engine.getRange(dag.objectPropertiesDAG()
				.getVertex(dag.objectProperties().get(testURI + "B3")).getRepresentative()).getIntervals();
		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(2, interval.getStart());
		assertEquals(3, interval.getEnd());
	}


	/**
	 * USE FOR TESTS ONLY
	 *
	 * @param filename
	 * @return
	 * @throws OWLOntologyCreationException
	 */

	public static ClassifiedTBox loadOntologyFromFileAndClassify(String filename) throws OWLOntologyCreationException {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owl = man.loadOntologyFromOntologyDocument(new File(filename));
		Ontology onto = OWLAPI_TRANSLATOR.translateAndClassify(owl);
		return onto.tbox();
	}
}
