package org.semanticweb.ontop.quest.dag;

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



import java.io.File;

import junit.framework.TestCase;

import org.semanticweb.ontop.ontology.Ontology;
import org.semanticweb.ontop.ontology.OntologyFactory;
import org.semanticweb.ontop.ontology.impl.OntologyFactoryImpl;
import org.semanticweb.ontop.owlapi3.OWLAPI3TranslatorUtility;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class R_TBoxReasonerImpl extends TestCase {

	/**
	 * R1 = R2^- = R3, S1 = S2^- = S3, R1 ISA S1
	 */
	private final String testIndexClasses = "src/test/resources/test/dag/tbox-reasoner-impl.owl";

	public void setUp() {
		// NO-OP
	}

	public void testIndexClasses() throws Exception {
		String testURI = "http://it.unibz.krdb/obda/ontologies/test.owl#";
		OWLAPI3TranslatorUtility t = new OWLAPI3TranslatorUtility();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owlonto = man.loadOntologyFromOntologyDocument(new File(testIndexClasses));
		Ontology onto = OWLAPI3TranslatorUtility.translate(owlonto);

		// generate DAG
		TBoxReasoner dag = new TBoxReasonerImpl(onto);
/*		
		SemanticIndexBuilder engine = new SemanticIndexBuilder(dag);
		List<Interval> nodeInterval = engine.getIntervals(ofac
				.createClass(testURI + "B1"));

		assertEquals(nodeInterval.size(), 1);
		Interval interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getIntervals(ofac.createClass(testURI + "B2"));

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getIntervals(ofac.createClass(testURI + "B3"));

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getIntervals(ofac.createClass(testURI + "A1"));

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getIntervals(ofac.createClass(testURI + "A2"));

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);

		nodeInterval = engine.getIntervals(ofac.createClass(testURI + "A3"));

		assertEquals(nodeInterval.size(), 1);
		interval = nodeInterval.get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);
*/		
	}
}
