package it.unibz.krdb.obda.owlrefplatform.owlapi3.example;

/*
 * #%L
 * ontop-quest-owlapi3
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLResultSet;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

public class QuestOWLExample {
	
	/*
	 * Use the sample database using H2 from
	 * https://babbage.inf.unibz.it/trac/obdapublic/wiki/InstallingTutorialDatabases
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
		 * Load the OBDA model from an external .obda file
		 */
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		OBDAModel obdaModel = fac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);

		/*
		 * Prepare the configuration for the Quest instance. The example below shows the setup for
		 * "Virtual ABox" mode
		 */
		QuestPreferences preference = new QuestPreferences();
		preference.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);

		/*
		 * Create the instance of Quest OWL reasoner.
		 */
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);
		factory.setPreferenceHolder(preference);
		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

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
			QuestOWLResultSet rs = st.executeTuple(sparqlQuery);
			int columnSize = rs.getColumCount();
			while (rs.nextRow()) {
				for (int idx = 1; idx <= columnSize; idx++) {
					OWLObject binding = rs.getOWLObject(idx);
					System.out.print(binding.toString() + ", ");
				}
				System.out.print("\n");
			}
			rs.close();

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
			QuestOWLExample example = new QuestOWLExample();
			example.runQuery();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
