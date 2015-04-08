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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
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

import static org.junit.Assert.assertTrue;

/***
 * Test same as using h2 simple database on wellbores
 */
public class H2ComplexSameAsTest {

	private OBDADataFactory fac;
	private QuestOWLConnection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/sameAs/wellbores-complex.owl";
	final String obdafile = "src/test/resources/sameAs/wellbores-complex.obda";
	private QuestOWL reasoner;
	private Connection sqlConnection;

	@Before
	public void setUp() throws Exception {

			 sqlConnection= DriverManager.getConnection("jdbc:h2:mem:wellboresComplex","sa", "");
			    java.sql.Statement s = sqlConnection.createStatement();
			  
//			    try {
			    	String text = new Scanner( new File("src/test/resources/sameAs/wellbore-complex-h2.sql") ).useDelimiter("\\A").next();
			    	s.execute(text);
//			    	Server.startWebServer(sqlConnection);
			    	 
//			    } catch(SQLException sqle) {
//			        System.out.println("Exception in creating db from script");
//			    }
			   
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
	

	
	private void runTests(String query) throws Exception {
		QuestOWLStatement st = conn.createStatement();
		String retval;
		try {
			QuestOWLResultSet rs = st.executeTuple(query);
			while(rs.nextRow()) {
                for (String s : rs.getSignature()) {
                    log.debug(s + ":  " + rs.getOWLObject(s));

                }
            }

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

	}


    @Test
    public void testSameAs1() throws Exception {
        String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> SELECT ?x\n" +
                "WHERE {\n" +
                "   ?x  a :Wellbore . \n" +
                "}";

        runTests(query);

    }

	@Test
	public void testSameAs2() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> SELECT ?x ?y\n" +
                "WHERE {\n" +
                "   ?x  a :Wellbore . \n" +
                "   ?x  :hasName ?y . \n" +
                "}";

		 runTests(query);

	}

    @Test
    public void testSameAs3() throws Exception {
        String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> \n" +
                "SELECT * WHERE { ?x a :Wellbore .\n" +
                " ?x :hasName ?y .\n" +
                " ?x :isActive ?z .}\n";

        runTests(query);

    }

    @Test
    public void testSameAs4() throws Exception {
        String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> \n" +
                "SELECT * WHERE { ?x a :Wellbore .\n" +
                " ?x :isHappy ?z .}\n";

        runTests(query);

    }

    @Test
    public void testSameAs5() throws Exception {
        String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> \n" +
                "SELECT * WHERE { " +
                "?x a :Wellbore .\n" +
                " ?x :hasOwner ?y .}\n";

        runTests(query);

    }










    }

