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

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/***
 * Tests that jtds jdbc driver for SQL Server returns the datatypes correctly
 */
public class JTDSDatatypesTest {

	private QuestOWLConnection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OWLOntology ontology;

	final String owlFile = "src/test/resources/datatype/datatypesjtds.owl";
	final String obdaFile = "src/test/resources/datatype/datatypejtds.obda";

	@Before
	public void setUp() throws Exception {

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlFile)));

		Properties p = new Properties();
		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setProperty(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setProperty(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

		QuestOWLFactory factory = new QuestOWLFactory(new File(obdaFile), new QuestPreferences(p));

		QuestOWL reasoner = factory.createReasoner(ontology, new SimpleConfiguration());
	    // Now we are ready for querying
		conn = reasoner.getConnection();


	}



	

	
	private String runTests(String query) throws Exception {
		QuestOWLStatement st = conn.createStatement();
		String retval="";
		try {
			QuestOWLResultSet rs = st.executeTuple(query);
//			while(rs.nextRow()) {
				rs.nextRow();
				OWLObject ind1 = rs.getOWLObject("y");
				retval = ind1.toString();
//			}
		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {
				st.close();
				assertTrue(false);
			}
			conn.close();

		}
		return retval;
	}



    /**
	 * Test use of datetime with jtds driver
	 * @throws Exception
	 */
	@Test
	public void testDatetime() throws Exception {

		String query =  "PREFIX : <http://knova.ru/adventureWorks.owl#>\n" +
				"SELECT DISTINCT ?x ?y { ?x :SpecialOffer_ModifiedDate ?y }";
		String val = runTests(query);
		assertEquals("\"2005-05-02T00:00:00.0\"^^xsd:dateTime", val);
	}





}

