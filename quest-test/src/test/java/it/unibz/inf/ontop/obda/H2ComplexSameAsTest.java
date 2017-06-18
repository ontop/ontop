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

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.io.ModelIOManager;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import jdk.nashorn.internal.ir.annotations.Immutable;
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
import java.util.ArrayList;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
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
		QuestOWLConfiguration config;

		config = QuestOWLConfiguration.builder().obdaModel(obdaModel).sameAsMappings(true).build();


		reasoner = (QuestOWL) factory.createReasoner(ontology, config);

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



	private ArrayList runTests(String query, boolean sameAs) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();

		QuestOWLConfiguration config = QuestOWLConfiguration.builder().obdaModel(obdaModel).sameAsMappings(sameAs).build();

		reasoner = (QuestOWL) factory.createReasoner(ontology, config);

		// Now we are ready for querying
		conn = reasoner.getConnection();

		QuestOWLStatement st = conn.createStatement();
		ArrayList<String> retVal = new ArrayList<>();
		try {
			QuestOWLResultSet rs = st.executeTuple(query);
			while(rs.nextRow()) {
				for (String s : rs.getSignature()) {
					OWLObject binding = rs.getOWLObject(s);

					String rendering = ToStringRenderer.getInstance().getRendering(binding);
					retVal.add(rendering);
					log.debug((s + ":  " + rendering));

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
		return retVal;

	}



	@Test
    public void testSameAs1() throws Exception {
        String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> " +
                "SELECT DISTINCT ?x\n" +
                "WHERE {\n" +
                "   ?x  a :Wellbore . \n" +
                "}";

        final ImmutableSet<String> results = ImmutableSet.<String>copyOf(runTests(query, true));

        ImmutableSet<String> expectedResults =
                ImmutableSet.<String>builder()
                        .add("<http://ontop.inf.unibz.it/test/wellbore/Katian>")
                        .add("<http://ontop.inf.unibz.it/test/wellbore/Bill>")
                        .add("<http://ontop.inf.unibz.it/test/wellbore#uri1-1>")
                        .add("<http://ontop.inf.unibz.it/test/wellbore#uri1-2>")
                        .add("<http://ontop.inf.unibz.it/test/wellbore#uri2-1>")
                        .add("<http://ontop.inf.unibz.it/test/wellbore#uri2-2>")
                        .add("<http://ontop.inf.unibz.it/test/wellbore#uri2-3>")
                        .add("<http://ontop.inf.unibz.it/test/wellbore#uri3-1>")
                        .add("<http://ontop.inf.unibz.it/test/wellbore#uri3-2>")
                        .add("<http://ontop.inf.unibz.it/test/wellbore#uri3-3>")
                        .add("<http://ontop.inf.unibz.it/test/wellbore#uri3-4>")
                        .build();
        assertEquals(expectedResults.size(), results.size() );
		assertEquals(expectedResults, results);

    }

	@Test
	public void testNoSameAs1() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> " +
                "SELECT DISTINCT ?x\n" +
				"WHERE {\n" +
				" {  ?x  a :Wellbore . \n" +
				"} UNION {?x owl:sameAs [ a :Wellbore ] }} ";

		ArrayList<String> results = runTests(query, false);
		assertEquals(11, results.size() );
	}

	@Test
	public void testSameAs2() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> SELECT ?x \n" +
                "WHERE {\n" +
                "   ?x  a :Wellbore . \n" +
                "   ?x  :hasName ?y . \n" +
                "}";

		ArrayList<String> results = runTests(query, true);
		assertEquals(54, results.size() );

	}

    @Test
    public void testSameAs3() throws Exception {
        String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> \n" +
                "SELECT * WHERE { ?x a :Wellbore .\n" +
                " ?x :hasName ?y .\n" +
                " ?x :isActive ?z .}\n";

		ArrayList<String> results = runTests(query, true);
		assertEquals(294, results.size() );


	}

	@Test
	public void testSameAs3b() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> \n" +
				"SELECT * WHERE { ?x a :Wellbore .\n" +
				" ?x :isActive ?z .}\n";

		ArrayList<String> results = runTests(query, true);
		assertEquals(80, results.size() );

	}

    @Test
    public void testSameAs4() throws Exception {
        String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> \n" +
                "SELECT * WHERE { ?x a :Wellbore .\n" +
                " ?x :isHappy ?z .}\n";

		ArrayList<String> results = runTests(query, true);
		assertEquals(46, results.size() );

    }

    @Test
    public void testSameAs5() throws Exception {
        String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> \n" +
                "SELECT DISTINCT  * WHERE { " +
                " ?x :hasOwner ?y .}\n";

		ArrayList<String> results = runTests(query, true);
		assertEquals(24, results.size() );
    }

	@Test
	public void testSameAs6() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> \n" +
				"SELECT DISTINCT * WHERE { " +
				"?x  a :Wellbore ." +
				" ?x :hasOwner ?y .}\n";

		ArrayList<String> results = runTests(query, true);
		assertEquals(24, results.size() );
	}









    }

