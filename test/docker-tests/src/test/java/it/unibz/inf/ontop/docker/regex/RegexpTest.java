package it.unibz.inf.ontop.docker.regex;

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

import com.google.common.collect.Lists;
import it.unibz.inf.ontop.docker.service.QuestSPARQLRewriterTest;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/***
 * Tests that the system can handle the SPARQL "regex" FILTER
 * This is a parameterized class, see junit documentation
 * Tests oracle, mysql, postgres and mssql
 */
@RunWith(org.junit.runners.Parameterized.class)
public class RegexpTest extends TestCase {

	// TODO We need to extend this test to import the contents of the mappings
	// into OWL and repeat everything taking form OWL

	private OWLConnection conn;

	private OWLOntology ontology;

	private static final String ROOT_LOCATION = "/testcases-docker/virtual-mode/stockexchange/simplecq/";
	private static final String owlfile = ROOT_LOCATION + "stockexchange.owl";
	private String obdafile;
	private String propertyfile;
	
	private OntopOWLReasoner reasoner;
	private Connection sqlConnection;
	private boolean isH2;
	private final boolean acceptFlags;

	/**
	 * Constructor is necessary for parameterized test
	 */
	public RegexpTest(String database, boolean isH2, boolean acceptFlags){
		this.obdafile = ROOT_LOCATION +"stockexchange-" + database + ".obda";
		this.propertyfile = ROOT_LOCATION +"stockexchange-" + database + ".properties";
		this.isH2 = isH2;
		this.acceptFlags = acceptFlags;
	}


	/**
	 * Returns the obda files for the different database engines
	 */
	@Parameters
	public static Collection<Object[]> getObdaFiles(){
		return Arrays.asList(new Object[][] {
				{"h2", true, true},
				// TODO: enable flags for MySQL >= 8
				 {"mysql", false, false },
				 {"pgsql", false, false},
				 {"oracle", false, false },
				 //no support for mssql and db2
				 });
	}

	
	private void createH2Database() throws Exception {
		try {
			sqlConnection = DriverManager.getConnection("jdbc:h2:mem:questrepository","fish", "fish");
			java.sql.Statement s = sqlConnection.createStatement();

			try {
				String text = new Scanner( new File("src/test/resources/dump/stockexchange-create-h2.sql") ).useDelimiter("\\A").next();
				s.execute(text);
				//Server.startWebServer(sqlConnection);
				
			} catch(SQLException sqle) {
				System.out.println("Exception in creating db from script");
				sqle.printStackTrace();
				throw sqle;
			}

			s.close();
		} catch (Exception exc) {
			try {
				deleteH2Database();
			} catch (Exception e2) {
				e2.printStackTrace();
				throw e2;
			}
		}	
	}
	
	@Override
	@Before
	public void setUp() throws Exception {
		if(this.isH2)
			this.createH2Database();

		final URL owlFileUrl = QuestSPARQLRewriterTest.class.getResource(owlfile);
		final URL obdaFileUrl = QuestSPARQLRewriterTest.class.getResource(obdafile);
		final URL propertyFileUrl = QuestSPARQLRewriterTest.class.getResource(propertyfile);
		// Creating a new instance of the reasoner
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile(obdaFileUrl.toString())
				.propertyFile(propertyFileUrl.toString())
				.ontologyFile(owlFileUrl)
				.build();
        reasoner = factory.createReasoner(config);

		// Now we are ready for querying
		conn = reasoner.getConnection();

		
	}
	
	@After
	public void tearDown() throws Exception{
		conn.close();
		reasoner.dispose();
		if(this.isH2)
			deleteH2Database();
	}
	
	private void deleteH2Database() throws Exception {
		if (!sqlConnection.isClosed()) {
			try (java.sql.Statement s = sqlConnection.createStatement()) {
				s.execute("DROP ALL OBJECTS DELETE FILES");
			}
			finally {
				sqlConnection.close();
			}
		}
	}
	

	private String runTest(OWLStatement st, String query, boolean hasResult) throws Exception {
		String retval;
		TupleOWLResultSet rs = st.executeSelectQuery(query);
		if(hasResult){
			assertTrue(rs.hasNext());
            final OWLBindingSet bindingSet = rs.next();
            OWLIndividual ind1 = bindingSet.getOWLIndividual("x");
			retval = ind1.toString();
		} else {
			assertFalse(rs.hasNext());
			retval = "";
		}

		return retval;
	}

	/**
	 * Tests the use of SPARQL like
	 * @throws Exception
	 */
	@Test
	public void testSparql2sqlRegex() throws Exception {
		List<String> queries = Lists.newArrayList(
				"'J[ano]*'",
				"'^J[ano]*$'",
				"'J'");
		if (acceptFlags) {
			queries.add("'j[ANO]*', 'i'");
			queries.add("'^J[ano]*$', 'm'");
		}

		for (String regex : queries){
			try (OWLStatement st = conn.createStatement()) {
				String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT DISTINCT ?x WHERE { ?x a :StockBroker. ?x :firstName ?name. FILTER regex (?name, " + regex + ")}";
				String broker = runTest(st, query, true);
				assertEquals(broker, "<http://www.owl-ontologies.com/Ontology1207768242.owl#person-112>");
			}
		}
		String[] wrongs = {
				"'^j[ANO]*$'",
				"'j[ANO]*'"
				};
		for (String regex : wrongs){
			try (OWLStatement st = conn.createStatement()) {
				String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT DISTINCT ?x WHERE { ?x a :StockBroker. ?x :firstName ?name. FILTER regex (?name, " + regex + ")}";
				String res = runTest(st, query, false);
				assertEquals(res, "");
			}
		}
	}

}
