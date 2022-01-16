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


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.si.repository.impl.Interval;
import it.unibz.inf.ontop.si.repository.impl.SemanticIndexBuilder;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import junit.framework.TestCase;

import java.util.List;

import static it.unibz.inf.ontop.utils.SITestingTools.loadOntologyFromFileAndClassify;
import static it.unibz.inf.ontop.utils.SITestingTools.getIRI;

public class DAGEquivalenceTest extends TestCase {

	/**
	 * R1 = R2^- = R3, S1 = S2^- = S3, R1 ISA S1
	 */
	private static final String testEquivalenceRoles = "src/test/resources/test/dag/role-equivalence.owl";

	/**
	 * A1 = A2^- = A3, B1 = B2^- = B3, C1 = C2^- = C3, C1 ISA B1 ISA A1
	 */
	private static final String testEquivalenceRolesInverse = "src/test/resources/test/dag/test-equivalence-roles-inverse.owl";

	/**
	 * A1 = A2 = A3, B1 = B2 = B3, B1 ISA A1
	 */
	private static final String testEquivalenceClasses = "src/test/resources/test/dag/test-equivalence-classes.owl";

	public void testIndexClasses() throws Exception {
		String testURI = "http://it.unibz.inf/obda/ontologies/test.owl#";
		ClassifiedTBox dag = loadOntologyFromFileAndClassify(testEquivalenceClasses);

		SemanticIndexBuilder engine = new SemanticIndexBuilder(dag);
		List<Interval> nodeInterval = engine.getRange((OClass)dag.classesDAG()
					.getVertex(dag.classes().get(getIRI(testURI, "B1"))).getRepresentative()).getIntervals();
		assertEquals(ImmutableList.of(new Interval(2, 2)), nodeInterval);

		nodeInterval = engine.getRange((OClass)dag.classesDAG()
				.getVertex(dag.classes().get(getIRI(testURI, "B2"))).getRepresentative()).getIntervals();
		assertEquals(ImmutableList.of(new Interval(2, 2)), nodeInterval);

		nodeInterval = engine.getRange((OClass)dag.classesDAG()
				.getVertex(dag.classes().get(getIRI(testURI, "B3"))).getRepresentative()).getIntervals();
		assertEquals(ImmutableList.of(new Interval(2, 2)), nodeInterval);

		nodeInterval = engine.getRange((OClass)dag.classesDAG()
				.getVertex(dag.classes().get(getIRI(testURI, "A1"))).getRepresentative()).getIntervals();
		assertEquals(ImmutableList.of(new Interval(1, 2)), nodeInterval);

		nodeInterval = engine.getRange((OClass)dag.classesDAG()
				.getVertex(dag.classes().get(getIRI(testURI, "A2"))).getRepresentative()).getIntervals();
		assertEquals(ImmutableList.of(new Interval(1, 2)), nodeInterval);

		nodeInterval = engine.getRange((OClass)dag.classesDAG()
				.getVertex(dag.classes().get(getIRI(testURI, "A3"))).getRepresentative()).getIntervals();
		assertEquals(ImmutableList.of(new Interval(1, 2)), nodeInterval);
	}

	public void testIntervalsRoles() throws Exception {
		String testURI = "http://it.unibz.inf/obda/ontologies/Ontology1314774461138.owl#";
		ClassifiedTBox dag = loadOntologyFromFileAndClassify(testEquivalenceRoles);
		// generate named DAG
		SemanticIndexBuilder engine = new SemanticIndexBuilder(dag);

		List<Interval> nodeInterval = engine.getRange(dag.objectPropertiesDAG()
				.getVertex(dag.objectProperties().get(getIRI(testURI, "R1"))).getRepresentative()).getIntervals();
		assertEquals(ImmutableList.of(new Interval(2, 2)), nodeInterval);

		nodeInterval = engine.getRange(dag.objectPropertiesDAG()
				.getVertex(dag.objectProperties().get(getIRI(testURI, "R2"))).getRepresentative()).getIntervals();
		assertEquals(ImmutableList.of(new Interval(2, 2)), nodeInterval);

		nodeInterval = engine.getRange(dag.objectPropertiesDAG()
				.getVertex(dag.objectProperties().get(getIRI(testURI, "R3"))).getRepresentative()).getIntervals();
		assertEquals(ImmutableList.of(new Interval(2, 2)), nodeInterval);

		nodeInterval = engine.getRange(dag.objectPropertiesDAG()
				.getVertex(dag.objectProperties().get(getIRI(testURI, "S1"))).getRepresentative()).getIntervals();
		assertEquals(ImmutableList.of(new Interval(1, 2)), nodeInterval);

		nodeInterval = engine.getRange(dag.objectPropertiesDAG()
				.getVertex(dag.objectProperties().get(getIRI(testURI, "S2"))).getRepresentative()).getIntervals();
		assertEquals(ImmutableList.of(new Interval(1, 2)), nodeInterval);

		nodeInterval = engine.getRange(dag.objectPropertiesDAG()
				.getVertex(dag.objectProperties().get(getIRI(testURI, "S3"))).getRepresentative()).getIntervals();
		assertEquals(ImmutableList.of(new Interval(1, 2)), nodeInterval);
	}

	public void testIntervalsRolesWithInverse() throws Exception {
		String testURI = "http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#";
		ClassifiedTBox dag = loadOntologyFromFileAndClassify(testEquivalenceRolesInverse);
		// generate named DAG
		SemanticIndexBuilder engine = new SemanticIndexBuilder(dag);
		EquivalencesDAG<ObjectPropertyExpression> properties = dag.objectPropertiesDAG();


		List<Interval> nodeInterval = engine.getRange(properties
				.getVertex(dag.objectProperties().get(getIRI(testURI, "A1"))).getRepresentative()).getIntervals();
		assertEquals(ImmutableList.of(new Interval(1, 3)), nodeInterval);

		assertEquals(dag.objectProperties().get(getIRI(testURI, "A1")).getInverse(),
				properties.getVertex(dag.objectProperties().get(getIRI(testURI, "A2"))).getRepresentative());

		nodeInterval = engine.getRange(properties
				.getVertex(dag.objectProperties().get(getIRI(testURI, "A3"))).getRepresentative()).getIntervals();
		assertEquals(ImmutableList.of(new Interval(1, 3)), nodeInterval);

		nodeInterval = engine.getRange(properties
				.getVertex(dag.objectProperties().get(getIRI(testURI, "C1"))).getRepresentative()).getIntervals();
		assertEquals(ImmutableList.of(new Interval(3, 3)), nodeInterval);

		assertEquals(properties.getVertex(dag.objectProperties().get(getIRI(testURI, "C1")).getInverse()).getRepresentative(),
				properties.getVertex(dag.objectProperties().get(getIRI(testURI, "C2"))).getRepresentative());

		nodeInterval = engine.getRange(properties
				.getVertex(dag.objectProperties().get(getIRI(testURI, "C3"))).getRepresentative()).getIntervals();
		assertEquals(ImmutableList.of(new Interval(3, 3)), nodeInterval);

		nodeInterval = engine.getRange(properties
				.getVertex(dag.objectProperties().get(getIRI(testURI, "B1"))).getRepresentative()).getIntervals();
		assertEquals(ImmutableList.of(new Interval(2, 3)), nodeInterval);

		assertEquals(properties.getVertex(dag.objectProperties().get(getIRI(testURI, "B3")).getInverse()).getRepresentative(),
					properties.getVertex(dag.objectProperties().get(getIRI(testURI, "B2"))).getRepresentative());

		nodeInterval = engine.getRange(properties
				.getVertex(dag.objectProperties().get(getIRI(testURI, "B3"))).getRepresentative()).getIntervals();
		assertEquals(ImmutableList.of(new Interval(2, 3)), nodeInterval);
	}
}
