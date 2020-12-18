package it.unibz.inf.ontop.docker.mysql;

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
import org.junit.Test;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;

/**
 * Test mysql jdbc driver.
 * The mappings do not correspond to the table in the database
 * (uppercase difference) : an error should be returned by the system.
 *
 */

public class ConferenceMySQLTest  {

    private static final String owlFile = "/mysql/conference/ontology5.owl";
	private static final String obdaFile = "/mysql/conference/ontology5.obda";
	private static final String propertyFile = "/mysql/conference/ontology5.properties";

	private void runTests(String query) throws Exception {
		String owlFileName =  this.getClass().getResource(owlFile).toString();
		String obdaFileName =  this.getClass().getResource(obdaFile).toString();
		String propertyFileName =  this.getClass().getResource(propertyFile).toString();

        // Creating a new instance of the reasoner
        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(owlFileName)
				.nativeOntopMappingFile(obdaFileName)
				.propertyFile(propertyFileName)
				.enableTestMode()
				.build();
        OntopOWLReasoner reasoner = factory.createReasoner(config);
	}


	@Test(expected = IllegalConfigurationException.class)
	public void testWrongMappings() throws Exception {
        String query1 = "PREFIX : <http://myproject.org/odbs#> SELECT ?x ?y\n" +
                "WHERE {\n" +
                "   ?x :LcontainsT ?y\n" +
                "}";

		runTests(query1);
	}


}
