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

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.Test;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	final String owlFile = "/oracle/ontologyType/dataPropertiesOntologyType.owl";
	final String obdaFile = "/oracle/ontologyType/dataPropertiesOntologyType.obda";
	final String propertyFile = "/oracle/oracle.properties";
    final String r2rmlFile = "/oracle/ontologyType/dataPropertiesPrettyType.ttl";
	final String obdaErroredFile = "/oracle/ontologyType/erroredOntologyType.obda";

	final String owlFileName =  this.getClass().getResource(owlFile).toString();
	final String r2rmlFileName =  this.getClass().getResource(r2rmlFile).toString();
	final String obdaFileName =  this.getClass().getResource(obdaFile).toString();
	final String obdaErroredFileName =  this.getClass().getResource(obdaErroredFile).toString();
	final String propertyFileName =  this.getClass().getResource(propertyFile).toString();

	private void runTests(boolean isR2rml, String query, int numberResults) throws Exception {

		// Creating a new instance of the reasoner
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
		OntopSQLOWLAPIConfiguration.Builder configBuilder = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.propertyFile(propertyFileName)
				.enableTestMode()
				.ontologyFile(owlFileName);


		if (isR2rml)
			configBuilder.r2rmlMappingFile(r2rmlFileName);
		else
			configBuilder.nativeOntopMappingFile(obdaFileName);

		OntopOWLReasoner reasoner = factory.createReasoner(configBuilder.build());

		// Now we are ready for querying
		OWLConnection conn = reasoner.getConnection();
		OWLStatement st = conn.createStatement();


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

	private void executeQueryAssertResults(String query, OWLStatement st, int expectedRows) throws Exception {
		TupleOWLResultSet rs = st.executeSelectQuery(query);
		int count = 0;
		while (rs.hasNext()) {
			count++;
            final OWLBindingSet next = rs.next();
			for (int i = 1; i <= rs.getColumnCount(); i++) {
				String bindingName = rs.getSignature().get(i - 1);
				log.info(bindingName);
                log.info("=" + next.getOWLObject(bindingName));
				log.info(" ");
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
				"select DISTINCT * {?x :number ?y. FILTER(datatype(?y) = xsd:integer)}";

		runTests(false, query1, 0);
//
//        //no value in the mapping
		//xsd:long in the ontology
		String query1b = "PREFIX : <http://www.company.com/ARES#>" +
				"select DISTINCT * {?x :number ?y. FILTER(datatype(?y) = xsd:long)}";

		runTests(false, query1b, 3);

		//no value in the mapping
		//xsd:string in the ontology
		String query2 = "PREFIX : <http://www.company.com/ARES#>" +
				"select DISTINCT * {?x :assayName ?y. FILTER(datatype(?y) = xsd:string)}";

		runTests(false, query2, 3);

		//no value in the ontology
		//rdfs:Literal in the mapping
		String query3 = "PREFIX : <http://www.company.com/ARES#>" +
				"select DISTINCT * {?x :hasDepartment ?y. FILTER(datatype(?y) = xsd:string)}";

		runTests(false, query3, 3);

		//no value in the ontology
		//no value in the mapping
		//value in the oracle database is decimal
		String query4 = "PREFIX : <http://www.company.com/ARES#>" +
				"select DISTINCT * {?x :AssayID ?y. FILTER(datatype(?y) = xsd:decimal)}";

		runTests(false, query4, 3);

		// no value in the ontology
		//value in the mapping is xsd:long

		String query5 = "PREFIX franz: <http://www.franz.com/>" +
				"select DISTINCT * {?x  franz:solrDocid ?y. FILTER(datatype(?y) = xsd:long)}";

		runTests(false, query5, 3);

		// no value in the ontology
		//value in the mapping is xsd:positiveInteger

		String query6 = "PREFIX : <http://www.company.com/ARES#>" +
				"select DISTINCT * {?x :hasSection ?y. FILTER(datatype(?y) = xsd:positiveInteger)}";

		runTests(false, query6, 3);


	}

	@Test
	public void testOntologyTypeR2rml() throws Exception {

		//no value in the mapping
		//xsd:long in the ontology, asking for the general case we will not have any result
		String query1 = "PREFIX : <http://www.company.com/ARES#>" +
				"select  DISTINCT * {?x :number ?y. FILTER(datatype(?y) = xsd:integer)}";

		runTests(true, query1, 0);
//
//        //no value in the mapping
		//xsd:long in the ontology
		String query1b = "PREFIX : <http://www.company.com/ARES#>" +
				"select DISTINCT * {?x :number ?y. FILTER(datatype(?y) = xsd:long)}";

		runTests(true, query1b, 3);

		//no value in the mapping
		//xsd:string in the ontology
		String query2 = "PREFIX : <http://www.company.com/ARES#>" +
				"select DISTINCT * {?x :assayName ?y. FILTER(datatype(?y) = xsd:string)}";

		runTests(true, query2, 3);

		//no value in the ontology
		//rdfs:Literal in the mapping
		String query3 = "PREFIX : <http://www.company.com/ARES#>" +
				"select DISTINCT * {?x :hasDepartment ?y. FILTER(datatype(?y) = xsd:string)}";

		runTests(true, query3, 3);

		//no value in the ontology
		//no value in the mapping
		//value in the oracle database is decimal
		String query4 = "PREFIX : <http://www.company.com/ARES#>" +
				"select DISTINCT * {?x :AssayID ?y. FILTER(datatype(?y) = xsd:decimal)}";

		runTests(true,  query4, 3);

		// no value in the ontology
		//value in the mapping is xsd:long

		String query5 = "PREFIX franz: <http://www.franz.com/>" +
				"select DISTINCT * {?x  franz:solrDocid ?y. FILTER(datatype(?y) = xsd:long)}";

		runTests(true,  query5, 3);

		// no value in the ontology
		//value in the mapping is xsd:positiveInteger

		String query6 = "PREFIX : <http://www.company.com/ARES#>" +
				"select DISTINCT * {?x :hasSection ?y. FILTER(datatype(?y) = xsd:positiveInteger)}";

		runTests(true, query6, 3);
	}

	@Test
	// Ontology datatype http://www.w3.org/2001/XMLSchema#integer for http://www.company.com/ARES#hasARESID
	// does not correspond to datatype http://www.w3.org/2001/XMLSchema#string in mappings
	public void failedMapping()  throws Exception  {
		try {
			// Creating a new instance of the reasoner
			OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
			OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
					.nativeOntopMappingFile(obdaErroredFileName)
					.ontologyFile(owlFileName)
					.enableTestMode()
					.propertyFile(propertyFileName)
					.build();
			OntopOWLReasoner reasoner = factory.createReasoner(config);


		} catch (Exception e) {


			assertTrue(e instanceof IllegalConfigurationException);
			log.debug(e.getMessage());


		}
	}


}
