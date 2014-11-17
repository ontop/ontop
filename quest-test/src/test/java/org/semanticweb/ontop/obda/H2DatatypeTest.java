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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.ontop.io.ModelIOManager;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/***
 * Tests that h2 datatypes
 */
public class H2DatatypeTest {

	private OBDADataFactory fac;
	private QuestOWLConnection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/datatype/datatypes.owl";
	final String obdafile = "src/test/resources/datatype/datetime-h2.obda";
	private QuestOWL reasoner;
	private Connection sqlConnection;

	@Before
	public void setUp() throws Exception {

			 sqlConnection= DriverManager.getConnection("jdbc:h2:mem:datatypes","sa", "");
			    java.sql.Statement s = sqlConnection.createStatement();
			  
			    try {
			    	String text = new Scanner( new File("src/test/resources/datatype/h2-datatypes.sql") ).useDelimiter("\\A").next();
			    	s.execute(text);
			    	//Server.startWebServer(sqlConnection);
			    	 
			    } catch(SQLException sqle) {
			        System.out.println("Exception in creating db from script");
			    }
			   
			    s.close();
		
		
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

		// Now we are ready for querying
		conn = reasoner.getConnection();

		
	}


	@After
	public void tearDown() throws Exception{
		conn.close();
		reasoner.dispose();
		if (!sqlConnection.isClosed()) {
			java.sql.Statement s = sqlConnection.createStatement();
			try {
				s.execute("DROP ALL OBJECTS DELETE FILES");
			} catch (SQLException sqle) {
				System.out.println("Table not found, not dropping");
			} finally {
				s.close();
				sqlConnection.close();
			}
		}
	}
	

	
	private String runTests(String query) throws Exception {
		QuestOWLStatement st = conn.createStatement();
		String retval;
		try {
			QuestOWLResultSet rs = st.executeTuple(query);
			assertTrue(rs.nextRow());
			OWLObject ind1 =	rs.getOWLObject("y")	 ;
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
	 * Test use of date
	 * @throws Exception
	 */
	@Test
	public void testDate() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> SELECT ?x ?y\n" +
                "WHERE {\n" +
                "   ?x a :Row; :hasDate ?y\n" +
                "   FILTER ( ?y = \"2013-03-18\"^^xsd:date ) .\n" +
                "}";
		String val = runTests(query);
		assertEquals("\"2013-03-18\"", val);
	}

    @Test
    public void testDate2() throws Exception {
        String query =  "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> SELECT ?y\n" +
                "WHERE {\n" +
                "   ?y a :Row; :hasDate \"2013-03-18\"^^xsd:date\n" +
                "}";
        String val = runTests(query);
        assertEquals("<http://ontop.inf.unibz.it/test/datatypes#datetime-1>", val);
    }




}

