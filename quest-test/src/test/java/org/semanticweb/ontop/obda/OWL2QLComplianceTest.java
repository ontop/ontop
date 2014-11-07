package org.semanticweb.ontop.obda;


/*
 * #%L
 * ontop-test
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.ontop.io.ModelIOManager;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.ontology.Ontology;
import org.semanticweb.ontop.owlapi3.OWLAPI3Translator;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWL;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;


/***
 * Test returns  empty concepts and roles, based on the mappings.
 * Given an ontology, which is connected to a database via mappings,
 * generate a suitable set of queries that test if there are empty concepts,
 *  concepts that are no populated to anything.
 */

public class OWL2QLComplianceTest {

	private OBDADataFactory fac;

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/owl2ql/simple.owl";
	final String obdafile = "src/test/resources/owl2ql/simple.obda";
	
	private QuestOWL reasoner;
	private Ontology onto;

	@Before
	public void setUp() throws Exception {

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

		// Loading the OBDA data
		fac = OBDADataFactoryImpl.getInstance();
		obdaModel = fac.getOBDAModel();

		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FULL_METADATA, QuestConstants.FALSE);
		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		OWLAPI3Translator translator = new OWLAPI3Translator();

		onto = translator.translate(ontology);
	}

	@After
	public void tearDown() throws Exception {
		reasoner.dispose();
	}



	/**
	 * Test class and property disjointness
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDisjointness() throws Exception {
		System.out.println(onto.getDisjointClassesAxioms());
		assertEquals(1, onto.getDisjointClassesAxioms().size());

		System.out.println(onto.getDisjointPropertiesAxioms());
		assertEquals(1, onto.getDisjointPropertiesAxioms().size());
	}

}
