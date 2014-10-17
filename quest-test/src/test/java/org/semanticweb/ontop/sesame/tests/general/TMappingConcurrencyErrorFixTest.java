package org.semanticweb.ontop.sesame.tests.general;

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

import junit.framework.TestCase;

import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWL;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLConnection;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLFactory;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLResultSet;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLStatement;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * Tests that TMapping does not return error in case of symmetric properties.
 * Use to check that no concurrency error appears. 
 */
public class TMappingConcurrencyErrorFixTest extends TestCase {

	private QuestOWLConnection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/exampleTMapping.owl";
	final String obdafile = "src/test/resources/exampleTMapping.obda";
	private QuestOWL reasoner;

	@Override
	public void setUp() throws Exception {
		
		
		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));
	
		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FULL_METADATA, QuestConstants.FALSE);
		// Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory(new File(obdafile), p);

		reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		conn = reasoner.getConnection();

		
	}


	public void tearDown() throws Exception{
		conn.close();
		reasoner.dispose();
	}
	

	
	private String runTests(String query) throws Exception {
		QuestOWLStatement st = conn.createStatement();
		String retval=null;
		try {
			QuestOWLResultSet rs = st.executeTuple(query);
			assertTrue(rs.nextRow());
			OWLIndividual ind1 =	rs.getOWLIndividual("y")	 ;
			retval = ind1.toString();
		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {
				st.close();
				assertTrue(false);
			}
			conn.close();
			reasoner.dispose();
		}
		return retval;
	}

	/**
	 * Test use of quoted mixed case table name
	 * @throws Exception
	 */
	public void test() throws Exception {
		String query = "PREFIX  : <http://www.semanticweb.org/sarah/ontologies/2014/4/untitled-ontology-73#> SELECT ?y WHERE { ?y a :Man }";
		String val = runTests(query);
		
	}
	


			
}
