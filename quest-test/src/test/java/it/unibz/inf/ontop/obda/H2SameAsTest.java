package it.unibz.inf.ontop.obda;

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

import it.unibz.inf.ontop.io.ModelIOManager;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.owlapi3.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
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
public class H2SameAsTest {

	private OBDADataFactory fac;
	private QuestOWLConnection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/sameAs/wellbores.owl";
	final String obdafile = "src/test/resources/sameAs/wellbores.obda";
	private QuestOWL reasoner;
	private Connection sqlConnection;

	@Before
	public void setUp() throws Exception {

			 sqlConnection= DriverManager.getConnection("jdbc:h2:mem:wellbores","sa", "");
			    java.sql.Statement s = sqlConnection.createStatement();
			  
//			    try {
			    	String text = new Scanner( new File("src/test/resources/sameAs/wellbore-h2.sql") ).useDelimiter("\\A").next();
			    	s.execute(text);
			    	//Server.startWebServer(sqlConnection);
			    	 
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
	

	
	private void runTests(String query, boolean sameAs) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();

		QuestOWLConfiguration config = QuestOWLConfiguration.builder().obdaModel(obdaModel).sameAsMappings(sameAs).build();

		reasoner = (QuestOWL) factory.createReasoner(ontology, config);

		// Now we are ready for querying
		conn = reasoner.getConnection();

		QuestOWLStatement st = conn.createStatement();
		String retval;
		try {
			QuestOWLResultSet rs = st.executeTuple(query);
			while(rs.nextRow()) {
                for (String s : rs.getSignature()) {
					OWLObject binding = rs.getOWLObject(s);
					log.debug((s + ":  " + ToStringRenderer.getInstance().getRendering(binding)));

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



    /**
	 * Test use of sameAs
     * the expected results
     * 911 'Amerigo'
     * 911 Aleksi
     * 1 Aleksi
     * 1 'Amerigo'
     * 992 'Luis'
     * 993 'Sagrada Familia'
     * 2 'Eljas'
	 * @throws Exception
	 */
	@Test
	public void testSameAs1() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> SELECT ?x ?y\n" +
                "WHERE {\n" +
                "   ?x  :hasName ?y . \n" +
                "}";

		 runTests(query, true);

	}

	@Test
	public void testNoSameAs1() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#>" +
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
				"SELECT ?x ?y WHERE { { ?x :hasName ?y .} UNION {?x owl:sameAs [ :hasName ?y]} }\n";

		// Bind (?n ?y)
		runTests(query, false);

	}

    /**
     * Test use of sameAs
     * the expected results
     * 911 'Amerigo' 13
     * 1 Aleksi 13
     * 2 'Eljas' 100
	 * Results as testNoSameAs2a()
	 * what we get is
	 * 911 'Amerigo' 13
	 * 911 Aleksi 13
	 * 1 'Amerigo' 13
	 * 1 Aleksi 13
	 * 2 'Eljas' 100
	 * Results as testNoSameAs2b()
     * @throws Exception
     */

    @Test
    public void testSameAs2() throws Exception {
        String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> SELECT ?x ?y ?z\n" +
                "WHERE {\n" +
                "   ?x  :hasName ?y . \n" +
                "   ?x  :hasValue ?z . \n " +

                "}";

         runTests(query, true);

    }

	@Test
	public void testNoSameAs2a() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#>" +
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
				"SELECT ?x ?y ?z WHERE { { ?x :hasName ?y .  ?x  :hasValue ?z . } UNION {?x owl:sameAs [ :hasName ?y ; :hasValue ?z ]} }\n";

		// Bind (?n ?y)
		runTests(query, false);

	}

	@Test
	public void testNoSameAs2b() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#>" +
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
				"SELECT ?x ?y ?z WHERE { { ?x :hasName ?y .  ?x  :hasValue ?z . } UNION {?x owl:sameAs [ :hasName ?y ] . ?x :hasValue ?z } UNION {?x :hasName ?y . ?x owl:sameAs [ :hasValue ?z ]}  UNION {?x owl:sameAs  [ :hasName ?y ]. ?x owl:sameAs [ :hasValue ?z ]} }\n";

		// Bind (?n ?y)
		runTests(query, false);

	}

//	@Test
	public void testSameAs3() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> SELECT ?x ?y\n" +
				"WHERE {\n" +
				"   ?x :hasOwner ?y . \n" +
				"}";

		runTests(query, true);

	}


//	@Test
    public void testNoSameAs3() throws Exception {
        String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#>  \n" +
                "SELECT ?x ?y WHERE { { ?x :hasOwner ?y . } UNION {?x :hasOwner [owl:samesAs ?y]} UNION {?x owl:sameAs [:hasOwner ?y]  } UNION {?x owl:sameAs [ owl:hasOwner [owl:samesAs ?y]]} }";

        runTests(query, false);

    }













}

