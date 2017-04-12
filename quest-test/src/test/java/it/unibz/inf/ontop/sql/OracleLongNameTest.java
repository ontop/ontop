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

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlrefplatform.owlapi.OntopOWLConnection;
import it.unibz.inf.ontop.owlrefplatform.owlapi.OntopOWLStatement;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWL;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLFactory;
import org.junit.After;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

import java.io.File;

/***
 * Oracle long name.
 */
public class OracleLongNameTest {

	private OntopOWLConnection conn;
	
	final String owlfile = "resources/oraclesql/o.owl";
	final String obdafile1 = "resources/oraclesql/o1.obda";
	final String propertyfile = "resources/oraclesql/o1.properties";
	private QuestOWL reasoner;

	@After
	public void tearDown() throws Exception{
		conn.close();
		reasoner.dispose();
	}
	

	private void runQuery(String varName) throws OWLException{

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile(new File(obdafile1))
				.ontologyFile(owlfile)
				.propertyFile(propertyfile)
				.enableTestMode()
				.build();
        reasoner = factory.createReasoner(config);

		// Now we are ready for querying
		conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> " +
				"SELECT " + varName + " WHERE { " + varName  + " a :Country}";
		
		OntopOWLStatement st = conn.createStatement();
		st.executeTuple(query);
	}
	
	
	/**
	 * Short variable name
	 */
	@Test
	public void testShortVarName() throws Exception {
		runQuery("?x");
	}

	/**
	 * Short variable name
	 */
	@Test
	public void testLongVarName() throws Exception {
		runQuery("?veryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongVarName");
	}
}

