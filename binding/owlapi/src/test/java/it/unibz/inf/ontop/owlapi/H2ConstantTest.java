package it.unibz.inf.ontop.owlapi;

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

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLObject;
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
 * Tests constants
 */
public class H2ConstantTest {

	private OWLConnection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());

	final String owlfile = "src/test/resources/constant/mappingConstants.owl";
	final String obdafile = "src/test/resources/constant/mappingConstants.obda";
	private OntopOWLReasoner reasoner;
	private Connection sqlConnection;
	private final String url = "jdbc:h2:mem:questjunitdb";
	private final String username = "sa";
	private final String password = "";

	@Before
	public void setUp() throws Exception {

			 sqlConnection= DriverManager.getConnection(url,username, password);
			    java.sql.Statement s = sqlConnection.createStatement();
			  
			    try {
			    	String text = new Scanner( new File("src/test/resources/constant/constantsDatabase-h2.sql") ).useDelimiter("\\A").next();
			    	s.execute(text);
			    	//Server.startWebServer(sqlConnection);
			    	 
			    } catch(SQLException sqle) {
			        System.out.println("Exception in creating db from script");
			    }
			   
			    s.close();


		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
		OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile(obdafile)
				.ontologyFile(owlfile)
				.enableFullMetadataExtraction(false)
				.jdbcUrl(url)
				.jdbcUser(username)
				.jdbcPassword(password)
				.enableTestMode()
				.build();
        reasoner = factory.createReasoner(config);

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
		OWLStatement st = conn.createStatement();
		String retval;
		try {
			TupleOWLResultSet rs = st.executeSelectQuery(query);
			assertTrue(rs.hasNext());
            final OWLBindingSet bindingSet = rs.next();

            OWLObject ind1 = bindingSet.getOWLObject("y")	 ;
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
	 * Test use of constants
	 * @throws Exception
	 */
	@Test
	public void testConstantDouble() throws Exception {
		String query =  "PREFIX : <http://www.semanticweb.org/smallDatabase#> SELECT ?x ?y\n" +
                "WHERE {\n" +
                "   ?x a :Company; :hasNetworth ?y\n" +
                "}";
		String val = runTests(query);
		assertEquals("\"1234.5678\"^^xsd:double", val);

	}

	@Test
	public void testConstantInteger() throws Exception {
		String query =  "PREFIX : <http://www.semanticweb.org/smallDatabase#> SELECT ?x ?y\n" +
				"WHERE {\n" +
				"   ?x a :Address; :hasNumber ?y\n" +
				"}";
		String val = runTests(query);
		assertEquals("\"35\"^^xsd:integer", val);

	}

	@Test
	public void testConstantBoolean() throws Exception {
		String query =  "PREFIX : <http://www.semanticweb.org/smallDatabase#> SELECT ?x ?y\n" +
				"WHERE {\n" +
				"   ?x a :Company; :hasSupplier ?y\n" +
				"}";
		String val = runTests(query);
		assertEquals("\"true\"^^xsd:boolean", val);

	}

	@Test
	public void testConstantDecimal() throws Exception {
		String query =  "PREFIX : <http://www.semanticweb.org/smallDatabase#> SELECT ?x ?y\n" +
				"WHERE {\n" +
				"   ?x a :Company; :hasMarketShares ?y\n" +
				"}";
		String val = runTests(query);
		assertEquals("\"1.000433564392849540\"^^xsd:decimal", val);

	}





}

