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
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
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
public class QuestOWLExample {


    final String owlfile = "src/test/resources/example/exampleBooks.owl";
    final String obdafile = "src/test/resources/example/exampleBooks.obda";
    final String propertiesfile = "src/test/resources/example/exampleBooks.properties";

    public void runQuery() throws Exception {

		/*
         * Create the instance of Quest OWL reasoner.
		 */
        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdafile)
                .ontologyFile(owlfile)
                .propertyFile(propertiesfile)
                .enableTestMode()
                .build();
        OntopOWLReasoner reasoner = factory.createReasoner(config);

		/*
         * Get the book information that is stored in the database
		 */
        String sparqlQuery = "PREFIX : <http://meraka/moss/exampleBooks.owl#> \n" +
                " SELECT DISTINCT ?x ?title ?author ?genre ?edition \n" +
                " WHERE { ?x a :Book; :title ?title; :genre ?genre; :writtenBy ?y; :hasEdition ?z. \n" +
                "         ?y a :Author; :name ?author. \n" +
                "         ?z a :Edition; :editionNumber ?edition }";

        try (/*
              * Prepare the data connection for querying.
		 	 */
             OntopOWLConnection conn = reasoner.getConnection();
             OntopOWLStatement st = conn.createStatement()) {

            long t1 = System.currentTimeMillis();
            TupleOWLResultSet rs = st.executeSelectQuery(sparqlQuery);
            while (rs.hasNext()) {
                final OWLBindingSet bindingSet = rs.next();
                for (String name: rs.getSignature()) {
                    OWLObject binding = bindingSet.getOWLObject(name);
                    System.out.print(ToStringRenderer.getInstance().getRendering(binding) + ", ");
                }
                System.out.print("\n");
            }
            rs.close();
            long t2 = System.currentTimeMillis();

			/*
             * Print the query summary
			 */
            String sqlQuery = st.getExecutableQuery(sparqlQuery).toString();

            System.out.println();
            System.out.println("The input SPARQL query:");
            System.out.println("=======================");
            System.out.println(sparqlQuery);
            System.out.println();

            System.out.println("The output SQL query:");
            System.out.println("=====================");
            System.out.println(sqlQuery);

            System.out.println("Query Execution Time:");
            System.out.println("=====================");
            System.out.println((t2 - t1) + "ms");

        } finally {
            reasoner.dispose();
        }
    }


    /**
     * Main client program
     */
    public static void main(String[] args) {
        try {
            QuestOWLExample example = new QuestOWLExample();
            example.runQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
