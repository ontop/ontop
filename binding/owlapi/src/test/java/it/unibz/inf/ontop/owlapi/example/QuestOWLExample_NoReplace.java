package it.unibz.inf.ontop.owlapi.example;

/*
 * #%L
 * ontop-quest-owlapi
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
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.owlapi.OntopOWLEngine;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.impl.SimpleOntopOWLEngine;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLObject;

/*
 * Use the sample database using H2 from
 * https://github.com/ontop/ontop/wiki/InstallingTutorialDatabases
 *
 * Please use the pre-bundled H2 server from the above link
 *
 */
public class QuestOWLExample_NoReplace {


    final String owlfile = "src/test/resources/example/exampleBooks.owl";
    final String obdafile = "src/test/resources/example/exampleBooks.obda";
    final String propertiesfile = "src/test/resources/example/exampleBooks.properties";

    public void runQuery() throws Exception {

		/*
         * Create the instance of Quest OWL reasoner.
		 */
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(owlfile)
                .nativeOntopMappingFile(obdafile)
                .propertyFile(propertiesfile)
                .enableTestMode()
                .build();

        /*
         * Get the book information that is stored in the database
		 */

        try (OntopOWLEngine reasoner = new SimpleOntopOWLEngine(config);
             OntopOWLConnection conn = reasoner.getConnection();
             OntopOWLStatement st = conn.createStatement()) {
            String sparqlQuery = "PREFIX : <http://meraka/moss/exampleBooks.owl#> \n" +
                    " SELECT DISTINCT ?x ?title ?author ?genre ?edition \n" +
                    " WHERE { ?x a :Book; :title ?title; :genre ?genre; :writtenBy ?y; :hasEdition ?z. \n" +
                    "         ?y a :Author; :name ?author. \n" +
                    "         ?z a :Edition; :editionNumber ?edition }";

            long t1 = System.currentTimeMillis();
            TupleOWLResultSet rs = st.executeSelectQuery(sparqlQuery);
            while (rs.hasNext()) {
                final OWLBindingSet bindingSet = rs.next();
                for (String name : rs.getSignature()) {
                    OWLObject binding = bindingSet.getOWLObject(name);
                    System.out.print(ToStringRenderer.getInstance().render(binding) + ", ");
                }
                System.out.print("\n");
            }
            rs.close();
            long t2 = System.currentTimeMillis();

            /*
             * Print the query summary
             */
            IQ executableQuery = st.getExecutableQuery(sparqlQuery);

            System.out.println();
            System.out.println("The input SPARQL query:");
            System.out.println("=======================");
            System.out.println(sparqlQuery);
            System.out.println();

            System.out.println("The output SQL query:");
            System.out.println("=====================");
            System.out.println(executableQuery.toString());

            System.out.println("Query Execution Time:");
            System.out.println("=====================");
            System.out.println((t2 - t1) + "ms");

        }
    }


    /**
     * Main client program
     */
    public static void main(String[] args) {
        try {
            QuestOWLExample_NoReplace example = new QuestOWLExample_NoReplace();
            example.runQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
