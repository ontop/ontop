package it.unibz.inf.ontop.sql;

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
import java.io.IOException;

import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWL;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConnection;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLFactory;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.io.InvalidDataSourceException;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.owlrefplatform.core.SQLExecutableQuery;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertTrue;

/***
 * Tests that the SPARQL LIMIT statement is correctly translated to WHERE ROWNUM <= x in oracle
 * Tests with both valid versions of the oracle driverClass string in the SourceDeclaration of the obda file
 */
public class OracleLIMITTest  {
	private QuestOWLConnection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OWLOntology ontology;
	
	final String owlfile = "resources/oraclesql/o.owl";
	final String obdafile1 = "resources/oraclesql/o1.obda";
	final String obdafile2 = "resources/oraclesql/o2.obda";
	private QuestOWL reasoner;

	@Before
	public void setUp() throws Exception {
		
		
		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));
	}

	@After
	public void tearDown() throws Exception{
		conn.close();
		reasoner.dispose();
	}
	

	private void runQuery(String obdaFileName) throws OBDAException, OWLException, IOException,
            InvalidMappingException, DuplicateMappingException, InvalidDataSourceException {

		QuestOWLFactory factory = new QuestOWLFactory();

		QuestConfiguration configuration = QuestConfiguration.defaultBuilder()
				.ontologyFile(owlfile)
				.nativeOntopMappingFile(obdaFileName)
				.enableFullMetadataExtraction(false)
				.build();

		reasoner = factory.createReasoner(configuration);

		// Now we are ready for querying
		conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x a :Country} LIMIT 10";
		
		QuestOWLStatement st = conn.createStatement();
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
		runQuery(obdafile1);
	}

	
	/**
	 * Test that LIMIT is correctly translated to WHERE ROWNUM <= 10 in oracle
	 * 
	 * when the driverClass is oracle.jdbc.driver.OracleDriver in the obdafile
	 * @throws Exception
	 */
	@Test
	public void testWithLongDriverString() throws Exception {
		runQuery(obdafile2);
	}

	
}

