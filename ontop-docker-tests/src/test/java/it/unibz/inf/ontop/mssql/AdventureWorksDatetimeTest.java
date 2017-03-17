package it.unibz.inf.ontop.mssql;

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
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/***
 * Tests that SQL Server returns the datetimes correctly
 */
public class AdventureWorksDatetimeTest {

	private OntopOWLConnection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());

	final String owlFile = "src/test/resources/mssql/adventureWorks.owl";
	final String obdaFile = "src/test/resources/mssql/adventureWorks.obda";
	final String propertiesFile = "src/test/resources/mssql/adventureWorks.properties";

	@Before
	public void setUp() throws Exception {
		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();

		OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
				//.enableEquivalenceOptimization(true)
				.nativeOntopMappingFile(obdaFile)
				.ontologyFile(owlFile)
				.propertyFile(propertiesFile)
				.enableTestMode()
				.build();

		QuestOWL reasoner = factory.createReasoner(configuration);

		// Now we are ready for querying
		conn = reasoner.getConnection();
	}
	
	private String runTests(String query) throws Exception {
		OntopOWLStatement st = conn.createStatement();
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

