package it.unibz.krdb.odba;

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

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.*;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/***
 * Tests that jtds jdbc driver for SQL Server returns the datatypes correctly
 */
public class JTDSDatatypesTest {

	private OBDADataFactory fac;
	private QuestOWLConnection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlFile = "src/test/resources/datatype/datatypesjtds.owl";
	final String obdaFile = "src/test/resources/datatype/datatypejtds.obda";
	private QuestOWL reasoner;
	private Connection sqlConnection;

	@Before
	public void setUp() throws Exception {

		fac = OBDADataFactoryImpl.getInstance();

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlFile)));

		// Loading the OBDA data
		obdaModel = fac.getOBDAModel();

		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdaFile);
		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

		factory.setPreferenceHolder(p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());
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

