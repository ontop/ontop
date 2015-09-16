package org.semanticweb.ontop.obda;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.ontop.injection.OBDAProperties;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.*;
import org.semanticweb.ontop.r2rml.R2RMLMappingParser;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test if the datatypes are assigned correctly.
 * Add datatype from the ontology, modifying extendTypeswithMetadata. First it adds value from ontology than it compares the ontology datatype with the mapping datatype, if the 2 value are not equal it throws an error.
 * If no information is present in the ontology or in the mappings it adds datatype from database.
 * NOTE: xsd:string and rdfs:Literal are different.
 * 
 */

public class OntologyTypesTest{

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OWLOntology ontology;

	final String owlFile = "src/test/resources/ontologyType/dataPropertiesOntologyType.owl";
	final String obdaFile = "src/test/resources/ontologyType/dataPropertiesOntologyType.obda";
    final String r2rmlFile = "src/test/resources/ontologyType/dataPropertiesPrettyType.ttl";
	final String obdaErroredFile = "src/test/resources/ontologyType/erroredOntologyType.obda";

	@Before
	public void setUp() throws Exception {
		
		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlFile)));
	}

	private void runTests(Properties p, String query, int numberResults, String mappingFile) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory(new File(mappingFile), new QuestPreferences(p));

		QuestOWL reasoner = factory.createReasoner(ontology, new SimpleConfiguration());

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
		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setProperty(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setProperty(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		
		//no value in the mapping
		//xsd:long in the ontology, asking for the general case we will not have any result
		String query1 = "PREFIX : <http://www.company.com/ARES#>" +
				"select * {?x :number ?y. FILTER(datatype(?y) = xsd:integer)}";

		runTests(p, query1, 0, obdaFile);
//
//        //no value in the mapping
        //xsd:long in the ontology
        String query1b = "PREFIX : <http://www.company.com/ARES#>" +
                "select * {?x :number ?y. FILTER(datatype(?y) = xsd:long)}";

        runTests(p, query1b, 3, obdaFile);

		//no value in the mapping
		//xsd:string in the ontology
		String query2 = "PREFIX : <http://www.company.com/ARES#>" +
				"select * {?x :assayName ?y. FILTER(datatype(?y) = xsd:string)}";

		runTests(p, query2, 3, obdaFile);

		//no value in the ontology
		//rdfs:Literal in the mapping
		String query3 = "PREFIX : <http://www.company.com/ARES#>" +
				"select * {?x :hasDepartment ?y. FILTER(datatype(?y) = rdfs:Literal)}";

		runTests(p, query3, 3, obdaFile);

		//no value in the ontology
		//no value in the mapping
		//value in the oracle database is decimal
		String query4 = "PREFIX : <http://www.company.com/ARES#>" +
						"select * {?x :AssayID ?y. FILTER(datatype(?y) = xsd:decimal)}";

		runTests(p, query4, 3, obdaFile);

        // no value in the ontology
        //value in the mapping is xsd:long

        String query5 = "PREFIX franz: <http://www.franz.com/>" +
                "select * {?x  franz:solrDocid ?y. FILTER(datatype(?y) = xsd:long)}";

        runTests(p, query5, 3, obdaFile);

        // no value in the ontology
        //value in the mapping is xsd:positiveInteger

        String query6 = "PREFIX : <http://www.company.com/ARES#>" +
                "select * {?x :hasSection ?y. FILTER(datatype(?y) = xsd:positiveInteger)}";

        runTests(p, query6, 3, obdaFile);

		
	}

    @Test
    public void testOntologyTypeR2rml() throws Exception {
        String jdbcurl = "jdbc:oracle:thin:@//10.7.20.91:1521/xe";
        String username = "system";
        String password = "obdaps83";
        String driverclass = "oracle.jdbc.driver.OracleDriver";

        Properties p = new Properties();
        p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setProperty(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setProperty(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");


		p.setProperty(OBDAProperties.DB_NAME, jdbcurl);
		p.setProperty(OBDAProperties.DB_USER, username);
		p.setProperty(OBDAProperties.DB_PASSWORD, password);
		p.setProperty(OBDAProperties.JDBC_DRIVER, driverclass);
		p.setProperty(MappingParser.class.getCanonicalName(), R2RMLMappingParser.class.getCanonicalName());

        //no value in the mapping
        //xsd:long in the ontology, asking for the general case we will not have any result
        String query1 = "PREFIX : <http://www.company.com/ARES#>" +
                "select * {?x :number ?y. FILTER(datatype(?y) = xsd:integer)}";

        runTests(p, query1, 0, r2rmlFile);
//
//        //no value in the mapping
        //xsd:long in the ontology
        String query1b = "PREFIX : <http://www.company.com/ARES#>" +
                "select * {?x :number ?y. FILTER(datatype(?y) = xsd:long)}";

        runTests(p, query1b, 3, r2rmlFile);

        //no value in the mapping
        //xsd:string in the ontology
        String query2 = "PREFIX : <http://www.company.com/ARES#>" +
                "select * {?x :assayName ?y. FILTER(datatype(?y) = xsd:string)}";

        runTests(p, query2, 3, r2rmlFile);

        //no value in the ontology
        //rdfs:Literal in the mapping
        String query3 = "PREFIX : <http://www.company.com/ARES#>" +
                "select * {?x :hasDepartment ?y. FILTER(datatype(?y) = rdfs:Literal)}";

        runTests(p, query3, 3, r2rmlFile);

        //no value in the ontology
        //no value in the mapping
        //value in the oracle database is decimal
        String query4 = "PREFIX : <http://www.company.com/ARES#>" +
                "select * {?x :AssayID ?y. FILTER(datatype(?y) = xsd:decimal)}";

        runTests(p, query4, 3, r2rmlFile);

        // no value in the ontology
        //value in the mapping is xsd:long

        String query5 = "PREFIX franz: <http://www.franz.com/>" +
                "select * {?x  franz:solrDocid ?y. FILTER(datatype(?y) = xsd:long)}";

        runTests(p, query5, 3, r2rmlFile);

        // no value in the ontology
        //value in the mapping is xsd:positiveInteger

        String query6 = "PREFIX : <http://www.company.com/ARES#>" +
                "select * {?x :hasSection ?y. FILTER(datatype(?y) = xsd:positiveInteger)}";

        runTests(p, query6, 3, r2rmlFile);
    }

	@Test
	// Ontology datatype http://www.w3.org/2001/XMLSchema#integer for http://www.company.com/ARES#hasARESID
	// does not correspond to datatype http://www.w3.org/2001/XMLSchema#string in mappings
	public void failedMapping()  throws Exception  {
		Properties p = new Properties();
		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setProperty(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setProperty(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

		try {
			// Creating a new instance of the reasoner
			QuestOWLFactory factory = new QuestOWLFactory(new File(obdaFile), new QuestPreferences(p));
			factory.createReasoner(ontology, new SimpleConfiguration());

		} catch (Exception e) {
            assertEquals(e.getCause().getClass(), OBDAException.class);
		}
	}

}
