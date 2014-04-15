package it.unibz.krdb.obda.sparql.entailments;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the simple ontology test-hierarchy-extended for sparql owl entailments.
 * rdfs:subclass, rdfs:subProperty, owl:inverseof owl:equivalentclass owl:equivalentProperty 
 * owl:disjointWith owl:propertyDisjointWith rdfs:domain rdfs:range
 * QuestPreferences has SPARQL_OWL_ENTAILMENT  set to true.
 *
 */
public class GalenVirtualTest extends TestCase {

	private OBDADataFactory fac;
	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;


	 final String owlfile =
	 "src/test/resources/subclass/galen-ians-full-doctored.owl";
	 final String obdafile =
	 "src/test/resources/subclass/galen-ians-full-doctored.obda";

	@Override
	public void setUp() throws Exception {

		/*
		 * Initializing and H2 database with the stock exchange data
		 */
	
		String url = "jdbc:h2:mem:questjunitdb";
		String username = "sa";
		String password = "";

		fac = OBDADataFactoryImpl.getInstance();

		conn = DriverManager.getConnection(url, username, password);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/subclass/create-h2.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}

		st.executeUpdate(bf.toString());
		conn.commit();

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

		// Loading the OBDA data
		obdaModel = fac.getOBDAModel();

		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);

	}

	@Override
	public void tearDown() throws Exception {
		try {
			dropTables();
			conn.close();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}
	}

	private void dropTables() throws SQLException, IOException {

		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/subclass/drop-h2.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}

		st.executeUpdate(bf.toString());
		st.close();
		conn.commit();
	}

	private List<String> runTests(Properties p, String query, String function) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();
		List<String> individuals = new LinkedList<String>();

		StringBuilder bf = new StringBuilder(query);
		try {

			QuestOWLResultSet rs = st.executeTuple(query);
			while (rs.nextRow())
			{
				OWLIndividual ind1 = rs.getOWLIndividual("x");
				OWLIndividual ind2 = rs.getOWLIndividual("y");

				System.out.println(ind1 + " " + function + " " + ind2);
				individuals.add(ind1 + " " + function + " " + ind2);

			}
			return individuals;

		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {
				st.close();
			}
			conn.close();
			reasoner.dispose();
		}
	}

	private List<String> runSingleNamedIndividualTests(Properties p, String query, String function) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();
		List<String> individuals = new LinkedList<String>();

		StringBuilder bf = new StringBuilder(query);
		try {

			QuestOWLResultSet rs = st.executeTuple(query);
			while (rs.nextRow())
			{
				OWLIndividual ind2 = rs.getOWLIndividual("x");

				System.out.println(ind2);
				if (ind2.isNamed())
					individuals.add(ind2.toString());

			}
			return individuals;

		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {
				st.close();
			}
			conn.close();
			reasoner.dispose();
		}
	}

	
	public void testSubClass() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		List<String> queries = new ArrayList<String>();
		
		String queryString = "PREFIX g:<http://www.co-ode.org/ontologies/galen#> SELECT ?x WHERE {g:Infection rdfs:subClassOf ?x}" ;

		queries.add(queryString);	
		
		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();

		try {
			
			for (String query : queries) {
				log.info("---Query " +query);

				QuestOWLResultSet rs = st.executeTuple(query);
				
				while (rs.nextRow())
				{
					OWLIndividual ind1 = rs.getOWLIndividual("x");

					System.out.println(ind1);

				}

				st.close();
			}


		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {
				st.close();

			}
			conn.close();

			reasoner.dispose();
		}
	}
	
	
	
	//does not return any result stackoverflow
	public void testSubDescription() throws Exception {
		
		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		log.info("Find subProperty");
		List<String> individualsProperty = runTests(p, "PREFIX : <http://www.co-ode.org/ontologies/galen#> SELECT * WHERE { ?x rdfs:subPropertyOf ?y }", "rdfs:subPropertyOf");
		assertEquals(48, individualsProperty.size());

		log.info("Find subProperty");
		List<String> property = runSingleNamedIndividualTests(p, "PREFIX : <http://www.co-ode.org/ontologies/galen#> SELECT * WHERE { :isSibling rdfs:subPropertyOf ?x }",
				"rdfs:subPropertyOf");
		assertEquals(4, property.size());

		log.info("Find subClass");
		List<String> individualsClass = runTests(p, "PREFIX : <http://www.co-ode.org/ontologies/galen#> SELECT * WHERE { ?x rdfs:subClassOf ?y }", "rdfs:subClassOf");
		assertEquals(76, individualsClass.size());

		log.info("Find subClass");
		List<String> classes = runSingleNamedIndividualTests(p, "PREFIX : <http://www.co-ode.org/ontologies/galen#> SELECT * WHERE { ?x rdfs:subClassOf :Man }", "rdfs:subClassOf");
		assertEquals(2, classes.size());

	}

	

}
