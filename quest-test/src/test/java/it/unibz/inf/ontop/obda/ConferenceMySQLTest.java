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

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWL;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLFactory;
import org.junit.Test;
import org.semanticweb.owlapi.reasoner.ReasonerInternalException;


import java.io.File;

/**
 * Test mysql jdbc driver.
 * The mappings do not correspond to the table in the database
 * (uppercase difference) : an error should be returned by the system.
 *
 */

public class ConferenceMySQLTest {

    final String owlFile = "src/test/resources/conference/ontology5.owl";
    final String obdaFile = "src/test/resources/conference/ontology5.obda";

	private void runTests(String query) throws Exception {

        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(owlFile)
				.nativeOntopMappingFile(new File(obdaFile))
				.build();
        QuestOWL reasoner = factory.createReasoner(config);
	}




	@Test(expected = ReasonerInternalException.class)
	public void testWrongMappings() throws Exception {
        String query1 = "PREFIX : <http://myproject.org/odbs#> SELECT ?x ?y\n" +
                "WHERE {\n" +
                "   ?x :LcontainsT ?y\n" +
                "}";

		runTests(query1);
	}


}
