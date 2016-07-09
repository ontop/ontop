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

import it.unibz.inf.ontop.injection.OBDAProperties;
import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test if the datatypes are assigned correctly.
 * Add datatype from the ontology, modifying extendTypeswithMetadata. First it adds value from ontology than it compares the ontology datatype with the mapping datatype, if the 2 value are not equal it throws an error.
 * If no information is present in the ontology or in the mappings it adds datatype from database.
 * NOTE: xsd:string and rdfs:Literal are different.
 * 
 */

public class OntologyTypesTest {

	Logger log = LoggerFactory.getLogger(this.getClass());

	final String owlFile = "src/test/resources/ontologyType/dataPropertiesOntologyType.owl";
	final String obdaFile = "src/test/resources/ontologyType/dataPropertiesOntologyType.obda";
    final String r2rmlFile = "src/test/resources/ontologyType/dataPropertiesPrettyType.ttl";
	final String obdaErroredFile = "src/test/resources/ontologyType/erroredOntologyType.obda";

	private void runTests(boolean isR2rml, Properties p, String query, int numberResults,
						  String mappingFile) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
        QuestConfiguration.Builder configBuilder = QuestConfiguration.defaultBuilder()
				.properties(p)
				.ontologyFile(owlFile);
		
		if (isR2rml)
			configBuilder.r2rmlMappingFile(mappingFile);
		else
			configBuilder.nativeOntopMappingFile(mappingFile);
		
        QuestOWL reasoner = factory.createReasoner(configBuilder.build());

		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();

		
		try {
			executeQueryAssertResults(query, st, numberResults);
			
		} catch (Exception e) {
            st.close();
            e.printStackTrace();
            assertTrue(false);


		} finally {

			conn.close();
			reasoner.dispose();
		}
	}
	
	private void executeQueryAssertResults(String query, QuestOWLStatement st, int expectedRows) throws Exception {
		QuestOWLResultSet rs = st.executeTuple(query);
		int count = 0;
		while (rs.nextRow()) {
			count++;
			for (int i = 1; i <= rs.getColumnCount(); i++) {
				System.out.print(rs.getSignature().get(i-1));
				System.out.print("=" + rs.getOWLObject(i));
				System.out.print(" ");
			}
			System.out.println();
		}
		rs.close();
		assertEquals(expectedRows, count);
	}


	@Test
	public void testOntologyType() throws Exception {
		Properties p = new Properties();
		
		//no value in the mapping
		//xsd:long in the ontology, asking for the general case we will not have any result
		String query1 = "PREFIX : <http://www.company.com/ARES#>" +
				"select * {?x :number ?y. FILTER(datatype(?y) = xsd:integer)}";

		runTests(false, p, query1, 0, obdaFile);
//
//        //no value in the mapping
        //xsd:long in the ontology
        String query1b = "PREFIX : <http://www.company.com/ARES#>" +
                "select * {?x :number ?y. FILTER(datatype(?y) = xsd:long)}";

        runTests(false, p, query1b, 3, obdaFile);

		//no value in the mapping
		//xsd:string in the ontology
		String query2 = "PREFIX : <http://www.company.com/ARES#>" +
				"select * {?x :assayName ?y. FILTER(datatype(?y) = xsd:string)}";

		runTests(false, p, query2, 3, obdaFile);

		//no value in the ontology
		//rdfs:Literal in the mapping
		String query3 = "PREFIX : <http://www.company.com/ARES#>" +
				"select * {?x :hasDepartment ?y. FILTER(datatype(?y) = rdfs:Literal)}";

		runTests(false, p, query3, 3, obdaFile);

		//no value in the ontology
		//no value in the mapping
		//value in the oracle database is decimal
		String query4 = "PREFIX : <http://www.company.com/ARES#>" +
						"select * {?x :AssayID ?y. FILTER(datatype(?y) = xsd:decimal)}";

		runTests(false, p, query4, 3, obdaFile);

        // no value in the ontology
        //value in the mapping is xsd:long

        String query5 = "PREFIX franz: <http://www.franz.com/>" +
                "select * {?x  franz:solrDocid ?y. FILTER(datatype(?y) = xsd:long)}";

        runTests(false, p, query5, 3, obdaFile);

        // no value in the ontology
        //value in the mapping is xsd:positiveInteger

        String query6 = "PREFIX : <http://www.company.com/ARES#>" +
                "select * {?x :hasSection ?y. FILTER(datatype(?y) = xsd:positiveInteger)}";

        runTests(false, p, query6, 3, obdaFile);

		
	}

    @Test
    public void testOntologyTypeR2rml() throws Exception {
        String jdbcurl = "jdbc:oracle:thin:@//10.7.20.91:1521/xe";
        String username = "system";
        String password = "obdaps83";
        String driverclass = "oracle.jdbc.driver.OracleDriver";

        Properties p = new Properties();
		p.setProperty(OBDAProperties.DB_NAME, jdbcurl);
		p.setProperty(OBDAProperties.DB_USER, username);
		p.setProperty(OBDAProperties.DB_PASSWORD, password);
		p.setProperty(OBDAProperties.JDBC_DRIVER, driverclass);

        //no value in the mapping
        //xsd:long in the ontology, asking for the general case we will not have any result
        String query1 = "PREFIX : <http://www.company.com/ARES#>" +
                "select * {?x :number ?y. FILTER(datatype(?y) = xsd:integer)}";

        runTests(true, p, query1, 0, r2rmlFile);
//
//        //no value in the mapping
        //xsd:long in the ontology
        String query1b = "PREFIX : <http://www.company.com/ARES#>" +
                "select * {?x :number ?y. FILTER(datatype(?y) = xsd:long)}";

        runTests(true, p, query1b, 3, r2rmlFile);

        //no value in the mapping
        //xsd:string in the ontology
        String query2 = "PREFIX : <http://www.company.com/ARES#>" +
                "select * {?x :assayName ?y. FILTER(datatype(?y) = xsd:string)}";

        runTests(true, p, query2, 3, r2rmlFile);

        //no value in the ontology
        //rdfs:Literal in the mapping
        String query3 = "PREFIX : <http://www.company.com/ARES#>" +
                "select * {?x :hasDepartment ?y. FILTER(datatype(?y) = rdfs:Literal)}";

        runTests(true, p, query3, 3, r2rmlFile);

        //no value in the ontology
        //no value in the mapping
        //value in the oracle database is decimal
        String query4 = "PREFIX : <http://www.company.com/ARES#>" +
                "select * {?x :AssayID ?y. FILTER(datatype(?y) = xsd:decimal)}";

        runTests(true, p, query4, 3, r2rmlFile);

        // no value in the ontology
        //value in the mapping is xsd:long

        String query5 = "PREFIX franz: <http://www.franz.com/>" +
                "select * {?x  franz:solrDocid ?y. FILTER(datatype(?y) = xsd:long)}";

        runTests(true, p, query5, 3, r2rmlFile);

        // no value in the ontology
        //value in the mapping is xsd:positiveInteger

        String query6 = "PREFIX : <http://www.company.com/ARES#>" +
                "select * {?x :hasSection ?y. FILTER(datatype(?y) = xsd:positiveInteger)}";

        runTests(true, p, query6, 3, r2rmlFile);
    }

	@Test
	// Ontology datatype http://www.w3.org/2001/XMLSchema#integer for http://www.company.com/ARES#hasARESID
	// does not correspond to datatype http://www.w3.org/2001/XMLSchema#string in mappings
	public void failedMapping()  throws Exception  {
		try {
			// Creating a new instance of the reasoner
	        QuestOWLFactory factory = new QuestOWLFactory();
	        QuestConfiguration config = QuestConfiguration.defaultBuilder()
					.nativeOntopMappingFile(new File(obdaErroredFile))
					.ontologyFile(owlFile)
					.build();
	        QuestOWL reasoner = factory.createReasoner(config);


		} catch (Exception e) {


            assertEquals(e.getCause().getClass().getCanonicalName(), "it.unibz.inf.ontop.model.OBDAException" );


		}
	}

}
