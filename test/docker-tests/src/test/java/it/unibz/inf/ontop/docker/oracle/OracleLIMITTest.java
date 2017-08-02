package it.unibz.inf.ontop.docker.oracle;

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


import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.answering.reformulation.impl.SQLExecutableQuery;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/***
 * Tests that the SPARQL LIMIT statement is correctly translated to WHERE ROWNUM <= x in oracle
 * Tests with both valid versions of the oracle driverClass string in the SourceDeclaration of the obda file
 */
public class OracleLIMITTest {
	private OntopOWLConnection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OWLOntology ontology;
	
	final String owlFile = "/oracle/oraclesql/o.owl";
	final String obdaFile1 = "/oracle/oraclesql/o1.obda";
	final String obdaFile2 = "/oracle/oraclesql/o2.obda";
	final String propertyFile = "/oracle/oracle.properties";

	final String owlFileName =  this.getClass().getResource(owlFile).toString();
	final String obdaFile1Name =  this.getClass().getResource(obdaFile1).toString();
	final String obdaFile2Name =  this.getClass().getResource(obdaFile2).toString();
	final String propertyFileName =  this.getClass().getResource(propertyFile).toString();


	private OntopOWLReasoner reasoner;

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception{
		conn.close();
		reasoner.dispose();
	}
	

	private void runQuery(String obdaFileName) throws OWLException, IOException,
            InvalidMappingException, DuplicateMappingException {

		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();

		OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(owlFileName)
				.nativeOntopMappingFile(obdaFileName)
				.enableFullMetadataExtraction(false)
				.enableTestMode()
				.propertyFile(propertyFileName)
				.build();

		reasoner = factory.createReasoner(configuration);

		// Now we are ready for querying
		conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x a :Country} LIMIT 10";
		
		OntopOWLStatement st = conn.createStatement();
		String sql = ((SQLExecutableQuery)st.getExecutableQuery(query)).getSQL();;
		boolean m = sql.matches("(?ms)(.*)WHERE ROWNUM <= 10(.*)");
		assertTrue(m);
	}
	
	
	/**
	 * Test that LIMIT is correctly translated to WHERE ROWNUM <= 10 in oracle
	 * 
	 * when the driverClass is oracle.jdbc.OracleDriver in the obdafile
	 * @throws Exception
	 */
	@Test
	public void testWithShortDriverString() throws Exception {
		runQuery(obdaFile1Name);
	}

	
	/**
	 * Test that LIMIT is correctly translated to WHERE ROWNUM <= 10 in oracle
	 * 
	 * when the driverClass is oracle.jdbc.driver.OracleDriver in the obdafile
	 * @throws Exception
	 */
	@Test
	public void testWithLongDriverString() throws Exception {
		runQuery(obdaFile2Name);
	}

	
}

