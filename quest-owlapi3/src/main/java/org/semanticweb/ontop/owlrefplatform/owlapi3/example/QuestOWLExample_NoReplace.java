package org.semanticweb.ontop.owlrefplatform.owlapi3.example;

/*
 * #%L
 * ontop-quest-owlapi3
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

import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import java.io.File;
import java.util.Properties;

public class QuestOWLExample_NoReplace {
	
	/*
	 * Use the sample database using H2 from
	 * https://github.com/ontop/ontop/wiki/InstallingTutorialDatabases
	 * 
	 * Please use the pre-bundled H2 server from the above link
	 * 
	 */
	final String owlfile = "src/main/resources/example/exampleBooks.owl";
	final String obdafile = "src/main/resources/example/exampleBooks.obda";

	public void runQuery() throws Exception {

		/*
		 * Load the ontology from an external .owl file.
		 */
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

		/*
		 * Prepare the configuration for the Quest instance. The example below shows the setup for
		 * "Virtual ABox" mode
		 */
		Properties p = new Properties();
		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setProperty(QuestPreferences.SQL_GENERATE_REPLACE, QuestConstants.FALSE);
		QuestPreferences preference = new QuestPreferences(p);

		/*
		 * Create the instance of Quest OWL reasoner.
		 */
		QuestOWLFactory questOwlFactory = new QuestOWLFactory(new File(obdafile), preference);
		QuestOWL reasoner = questOwlFactory.createReasoner(ontology, new SimpleConfiguration());

		/*
		 * Prepare the data connection for querying.
		 */
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();

		/*
		 * Get the book information that is stored in the database
		 */
		String sparqlQuery = 
				"PREFIX : <http://meraka/moss/exampleBooks.owl#> \n" +
				"SELECT DISTINCT ?x ?title ?author ?genre ?edition \n" +
				"WHERE { ?x a :Book; :title ?title; :genre ?genre; :writtenBy ?y; :hasEdition ?z. \n" +
				"		 ?y a :Author; :name ?author. \n" +
				"		 ?z a :Edition; :editionNumber ?edition }";

		try {
            long t1 = System.currentTimeMillis();
			QuestOWLResultSet rs = st.executeTuple(sparqlQuery);
			int columnSize = rs.getColumnCount();
			while (rs.nextRow()) {
				for (int idx = 1; idx <= columnSize; idx++) {
					OWLObject binding = rs.getOWLObject(idx);
					System.out.print(binding.toString() + ", ");
				}
				System.out.print("\n");
			}
			rs.close();
            long t2 = System.currentTimeMillis();
			/*
			 * Print the query summary
			 */
			QuestOWLStatement qst = (QuestOWLStatement) st;
			String sqlQuery = qst.getUnfolding(sparqlQuery);

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
            System.out.println((t2-t1) + "ms");
			
		} finally {
			
			/*
			 * Close connection and resources
			 */
			if (st != null && !st.isClosed()) {
				st.close();
			}
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
			reasoner.dispose();
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
