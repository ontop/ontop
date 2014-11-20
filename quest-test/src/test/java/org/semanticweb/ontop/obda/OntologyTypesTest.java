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
import java.net.URI;
import java.sql.Connection;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.ontop.io.ModelIOManager;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.*;
import org.semanticweb.ontop.r2rml.R2RMLReader;
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

	private OBDADataFactory fac;
	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlFile = "src/test/resources/ontologyType/dataPropertiesOntologyType.owl";
	final String obdaFile = "src/test/resources/ontologyType/dataPropertiesOntologyType.obda";
    final String r2rmlFile = "src/test/resources/ontologyType/dataPropertiesPrettyType.ttl";
	final String obdaErroredFile = "src/test/resources/ontologyType/erroredOntologyType.obda";

	@Before
	public void setUp() throws Exception {

		fac = OBDADataFactoryImpl.getInstance();
		
		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlFile)));


		
	}

	private void runTests(Properties p, String query, int numberResults) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

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

		// Loading the OBDA data
		obdaModel = fac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdaFile);
				
		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		
		//no value in the mapping
		//xsd:long in the ontology, asking for the general case we will not have any result
		String query1 = "PREFIX : <http://www.company.com/ARES#>" +
				"select * {?x :number ?y. FILTER(datatype(?y) = xsd:long)}";

		runTests(p, query1, 0);
//
//        //no value in the mapping
        //xsd:long in the ontology
        String query1b = "PREFIX : <http://www.company.com/ARES#>" +
                "select * {?x :number ?y. FILTER(datatype(?y) = xsd:long)}";

        runTests(p, query1b, 3);

		//no value in the mapping
		//xsd:string in the ontology
		String query2 = "PREFIX : <http://www.company.com/ARES#>" +
				"select * {?x :assayName ?y. FILTER(datatype(?y) = xsd:string)}";

		runTests(p, query2, 3);

		//no value in the ontology
		//rdfs:Literal in the mapping
		String query3 = "PREFIX : <http://www.company.com/ARES#>" +
				"select * {?x :hasDepartment ?y. FILTER(datatype(?y) = rdfs:Literal)}";

		runTests(p, query3, 3);

		//no value in the ontology
		//no value in the mapping
		//value in the oracle database is decimal
		String query4 = "PREFIX : <http://www.company.com/ARES#>" +
						"select * {?x :AssayID ?y. FILTER(datatype(?y) = xsd:decimal)}";

		runTests(p, query4, 3);

        // no value in the ontology
        //value in the mapping is xsd:long

        String query5 = "PREFIX franz: <http://www.franz.com/>" +
                "select * {?x  franz:solrDocid ?y. FILTER(datatype(?y) = xsd:long)}";

        runTests(p, query5, 3);

        // no value in the ontology
        //value in the mapping is xsd:positiveInteger

        String query6 = "PREFIX : <http://www.company.com/ARES#>" +
                "select * {?x :hasSection ?y. FILTER(datatype(?y) = xsd:positiveInteger)}";

        runTests(p, query6, 3);

		
	}

    @Test
    public void testOntologyTypeR2rml() throws Exception {
        String jdbcurl = "jdbc:oracle:thin:@//10.7.20.91:1521/xe";
        String username = "system";
        String password = "obdaps83";
        String driverclass = "oracle.jdbc.driver.OracleDriver";

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

        OBDADataFactory f = OBDADataFactoryImpl.getInstance();
        // String sourceUrl = "http://example.org/customOBDA";
        URI obdaURI = new File(r2rmlFile).toURI();
        String sourceUrl = obdaURI.toString();
        OBDADataSource dataSource = f.getJDBCDataSource(sourceUrl, jdbcurl,
                username, password, driverclass);

        log.info("Loading r2rml file");
        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory();

        factory.setPreferenceHolder(p);

        R2RMLReader reader = new R2RMLReader(r2rmlFile);

        obdaModel = reader.readModel(dataSource);

        //no value in the mapping
        //xsd:long in the ontology, asking for the general case we will not have any result
        String query1 = "PREFIX : <http://www.company.com/ARES#>" +
                "select * {?x :number ?y. FILTER(datatype(?y) = xsd:long)}";

        runTests(p, query1, 0);
//
//        //no value in the mapping
        //xsd:long in the ontology
        String query1b = "PREFIX : <http://www.company.com/ARES#>" +
                "select * {?x :number ?y. FILTER(datatype(?y) = xsd:long)}";

        runTests(p, query1b, 3);

        //no value in the mapping
        //xsd:string in the ontology
        String query2 = "PREFIX : <http://www.company.com/ARES#>" +
                "select * {?x :assayName ?y. FILTER(datatype(?y) = xsd:string)}";

        runTests(p, query2, 3);

        //no value in the ontology
        //rdfs:Literal in the mapping
        String query3 = "PREFIX : <http://www.company.com/ARES#>" +
                "select * {?x :hasDepartment ?y. FILTER(datatype(?y) = rdfs:Literal)}";

        runTests(p, query3, 3);

        //no value in the ontology
        //no value in the mapping
        //value in the oracle database is decimal
        String query4 = "PREFIX : <http://www.company.com/ARES#>" +
                "select * {?x :AssayID ?y. FILTER(datatype(?y) = xsd:decimal)}";

        runTests(p, query4, 3);

        // no value in the ontology
        //value in the mapping is xsd:long

        String query5 = "PREFIX franz: <http://www.franz.com/>" +
                "select * {?x  franz:solrDocid ?y. FILTER(datatype(?y) = xsd:long)}";

        runTests(p, query5, 3);

        // no value in the ontology
        //value in the mapping is xsd:positiveInteger

        String query6 = "PREFIX : <http://www.company.com/ARES#>" +
                "select * {?x :hasSection ?y. FILTER(datatype(?y) = xsd:positiveInteger)}";

        runTests(p, query6, 3);
    }

	@Test	
	// Ontology datatype http://www.w3.org/2001/XMLSchema#integer for http://www.company.com/ARES#hasARESID
	// does not correspond to datatype http://www.w3.org/2001/XMLSchema#string in mappings
	public void failedMapping()  throws Exception  {
		// Loading the OBDA data
		obdaModel = fac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdaErroredFile);
		
		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		
		try {
			// Creating a new instance of the reasoner
			QuestOWLFactory factory = new QuestOWLFactory();
			factory.setOBDAController(obdaModel);

			factory.setPreferenceHolder(p);

			QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());
			
		} catch (Exception e) {
           
            
            assertEquals(e.getCause().getClass().getCanonicalName(), "org.semanticweb.ontop.model.OBDAException" );


		} 
	}

}
